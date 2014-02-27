package com.tinkerpop.blueprints.impls.rexster;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tinkerpop.blueprints.Vertex;

import java.util.Queue;

import static com.tinkerpop.blueprints.impls.rexster.util.JacksonUtil.optArray;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
class RexsterVertexIterable extends RexsterElementIterable<Vertex> {

    public RexsterVertexIterable(final String uri, final RexsterGraph graph) {
        super(uri, graph);
    }

    protected void fillBuffer(final Queue<Vertex> queue, final int start, final int end) {
        final ObjectNode object = RestHelper.get(this.uri + this.createSeparator() + RexsterTokens.REXSTER_OFFSET_START + RexsterTokens.EQUALS + start + RexsterTokens.AND + RexsterTokens.REXSTER_OFFSET_END + RexsterTokens.EQUALS + end);

        ArrayNode array = optArray(object, RexsterTokens.RESULTS);
        for(JsonNode node : array) {
            queue.add(new RexsterVertex(node, this.graph));
        }
    }
}
