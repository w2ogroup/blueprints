package com.tinkerpop.blueprints.impls.rexster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.util.ElementHelper;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import static com.tinkerpop.blueprints.impls.rexster.util.JacksonUtil.optObject;
import static com.tinkerpop.blueprints.impls.rexster.util.JacksonUtil.optText;
import static com.tinkerpop.blueprints.impls.rexster.util.JacksonUtil.toObjectNode;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
abstract class RexsterElement implements Element {

    protected final Object id;
    protected final RexsterGraph graph;

    public RexsterElement(final JsonNode rawElement, final RexsterGraph graph) {
        this.id = rawElement.get(RexsterTokens._ID);
        this.graph = graph;
    }

    public Object getId() {
        return this.id;
    }

    public Set<String> getPropertyKeys() {
        ObjectNode rawElement;

        if (this instanceof Vertex)
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()));
        else
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()));

        Set<String> keys = new HashSet<String>();
        Iterator keyIterator = rawElement.fieldNames();
        while (keyIterator.hasNext()) {
            keys.add((String) keyIterator.next());
        }

        keys.remove(RexsterTokens._TYPE);
        keys.remove(RexsterTokens._LABEL);
        keys.remove(RexsterTokens._ID);
        keys.remove(RexsterTokens._OUTE);
        keys.remove(RexsterTokens._INE);
        keys.remove(RexsterTokens._OUTV);
        keys.remove(RexsterTokens._INV);
        return keys;
    }

    public void remove() {
        if (this instanceof Vertex)
            this.graph.removeVertex((Vertex) this);
        else
            this.graph.removeEdge((Edge) this);
    }

    @SuppressWarnings("unchecked")
    public <T> T getProperty(final String key) {
        ObjectNode rawElement;
        if (this instanceof Vertex)
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);
        else
            rawElement = RestHelper.getResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RexsterTokens.REXSTER_SHOW_TYPES_EQUALS_TRUE);

        ObjectNode typedProperty = optObject(rawElement, key);
        if (null != typedProperty)
            return (T) RestHelper.typeCast(optText(typedProperty, RexsterTokens.TYPE), typedProperty.get(RexsterTokens.VALUE));
        else
            return null;
    }

    public void setProperty(final String key, final Object value) {
        ElementHelper.validateProperty(this, key, value);
        if (key.startsWith(RexsterTokens.UNDERSCORE))
            throw new RuntimeException("RexsterGraph does not support property keys that start with underscore");

        final Map<String, Object> data = new HashMap<String, Object>();
        data.put(key, RestHelper.uriCast(value));
        final ObjectNode json = toObjectNode(data);

        if (this instanceof Vertex) {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()), json);
        } else {
            RestHelper.postResultObject(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()), json);
        }
    }

    public int hashCode() {
        return this.getId().hashCode();
    }

    public <T> T removeProperty(final String key) {

        Object object = this.getProperty(key);

        if (this instanceof Vertex)
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_VERTICES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key));
        else
            RestHelper.delete(this.graph.getGraphURI() + RexsterTokens.SLASH_EDGES_SLASH + RestHelper.encode(this.getId()) + RexsterTokens.QUESTION + RestHelper.encode(key));

        return (T) object;
    }

    public boolean equals(final Object object) {
        return ElementHelper.areEqual(this, object);
    }

}
