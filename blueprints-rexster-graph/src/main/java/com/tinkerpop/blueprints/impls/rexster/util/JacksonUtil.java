package com.tinkerpop.blueprints.impls.rexster.util;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 */
public class JacksonUtil {

    public static ArrayNode optArray(JsonNode parent, String property) {
        JsonNode node = parent.get(property);
        return node == null || !node.isArray() ? null : (ArrayNode)node;
    }

    public static ObjectNode optObject(JsonNode parent, String property) {
        JsonNode node = parent.get(property);
        return node == null || !node.isObject() ? null : (ObjectNode)node;
    }

    public static String optText(JsonNode parent, String property) {
        JsonNode result = parent.get(property);
        return result == null ? null : result.asText();
    }

    public static Long optLong(JsonNode parent, String property) {
        JsonNode result = parent.get(property);
        return result == null ? null : result.asLong();
    }

    public static Integer optInt(JsonNode parent, String property) {
        JsonNode result = parent.get(property);
        return result == null ? null : result.asInt();
    }

    public static ObjectNode toObjectNode(Map<String, Object> toMap) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(toMap);
    }

    public static ObjectNode getJsonNode(InputStream inputStream) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = new JsonFactory();
        JsonParser parser = factory.createParser(inputStream);
        return mapper.readTree(parser);
    }
}
