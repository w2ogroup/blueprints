package com.tinkerpop.blueprints.impls.neo4j;

import com.tinkerpop.blueprints.*;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.configuration.MapConfiguration;
import org.neo4j.graphdb.*;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.Bootstrapper;
import org.neo4j.server.WrappingNeoServerBootstrapper;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.ThirdPartyJaxRsPackage;
import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * A Blueprints implementation of the graph database Neo4j that creates native neo4j API endpoints (http://neo4j.org)
 *
 * @author Matt Franklin (https://github.com/mfranklin)
 */
public class Neo4jApiSupportedGraph extends Neo4jGraph implements TransactionalGraph, IndexableGraph, KeyIndexableGraph, MetaGraph<GraphDatabaseService> {
    private static final Logger logger = Logger.getLogger(Neo4jGraph.class.getName());

    private Bootstrapper bootstrapper;

    public Neo4jApiSupportedGraph(final String directory) {
        this(directory, null);
    }

    public Neo4jApiSupportedGraph(final GraphDatabaseService rawGraph) {
        super(rawGraph);
    }

    public Neo4jApiSupportedGraph(final GraphDatabaseService rawGraph, boolean fresh) {
        super(rawGraph, fresh);
    }

    public Neo4jApiSupportedGraph(final String directory, final Map<String, String> configuration) {
        logger.info("Starting API supported Neo4j graph");
        boolean fresh = !new File(directory).exists();
        try {
            if (null != configuration) {
                this.rawGraph = new EmbeddedGraphDatabase(directory, configuration);
                this.bootstrapper = new WrappingNeoServerBootstrapper((GraphDatabaseAPI)this.rawGraph, getConfigurator(configuration));
            } else {
                this.rawGraph = new EmbeddedGraphDatabase(directory);
                this.bootstrapper = new WrappingNeoServerBootstrapper((GraphDatabaseAPI)this.rawGraph);
            }

            if (fresh)
                this.freshLoad();

            this.loadKeyIndices();
            bootstrapper.start();

        } catch (Exception e) {
            logger.throwing("Error starting server", "", e);
            if (this.rawGraph != null) {
                this.shutdown();
            }
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Neo4jApiSupportedGraph(final Configuration configuration) {
        this(configuration.getString("blueprints.neo4j.directory", null),
                ConfigurationConverter.getMap(configuration.subset("blueprints.neo4j.conf")));
    }

    public void shutdown() {
        try {
            this.commit();
        } catch (TransactionFailureException e) {
            // TODO: inspect why certain transactions fail
        }
        this.bootstrapper.stop();
        this.rawGraph.shutdown();
    }

    private Configurator.Adapter getConfigurator(final Map<String, String> configuration) {
        return new Configurator.Adapter() {
            @Override
            public Configuration configuration() {
                return new MapConfiguration(configuration);
            }

            @Override
            public Map<String, String> getDatabaseTuningProperties() {
                return configuration;
            }

            @Override
            public Set<ThirdPartyJaxRsPackage> getThirdpartyJaxRsPackages() {
                return new HashSet<ThirdPartyJaxRsPackage>();
            }
        };
    }
}
