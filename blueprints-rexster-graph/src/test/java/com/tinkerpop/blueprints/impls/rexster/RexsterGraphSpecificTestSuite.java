package com.tinkerpop.blueprints.impls.rexster;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.TestSuite;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.GraphTest;
import junit.framework.Assert;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class RexsterGraphSpecificTestSuite extends TestSuite {

    public RexsterGraphSpecificTestSuite() {
    }

    public RexsterGraphSpecificTestSuite(final GraphTest graphTest) {
        super(graphTest);
    }

    public void testEncoding() throws Exception {
        final String doTest = System.getProperty("testRexsterGraph", "true");
        if (doTest.equals("true")) {
            final Graph g = graphTest.generateGraph();
            ((RexsterGraphTest) graphTest).resetGraph();

            final Vertex v = g.addVertex(null);
            v.setProperty("test", "déja-vu");

            Assert.assertEquals("déja-vu", g.getVertex(v.getId()).getProperty("test"));
        }
    }

    public void testOuterParens() throws Exception {
        final String doTest = System.getProperty("testRexsterGraph", "true");
        if (doTest.equals("true")) {
            final Graph g = graphTest.generateGraph();
            ((RexsterGraphTest) graphTest).resetGraph();

            final Vertex v = g.addVertex(null);
            v.setProperty("test", "(sometext)");

            Assert.assertEquals("(sometext)", g.getVertex(v.getId()).getProperty("test"));
        }
    }

    public void testGremlin() throws Exception {
        final String doTest = System.getProperty("testRexsterGraph", "true");
        if (doTest.equals("true")) {
            final RexsterGraph g = (RexsterGraph) graphTest.generateGraph();
            ((RexsterGraphTest) graphTest).resetGraph();

            final ArrayNode additionResults = g.execute("1+1");
            Assert.assertEquals(2, additionResults.get(0).asInt());

            g.addVertex("1");
            g.addVertex("2");
            g.addVertex("3");

            final ArrayNode graphResults = g.execute("g.V.count()");
            Assert.assertEquals(3, graphResults.get(0).asInt());

            final Map<String, Object> args = new HashMap<String, Object>() {{
                put("x", "2");
            }};

            final ArrayNode paramResults = g.execute("g.v(x)._().count()", args);
            Assert.assertEquals(1, paramResults.get(0).asInt());

            final ArrayNode jsonParamResults = g.execute("g.v(x)._().count()", "{\"x\":\"2\"}");
            Assert.assertEquals(1, jsonParamResults.get(0).asInt());

        }
    }

}