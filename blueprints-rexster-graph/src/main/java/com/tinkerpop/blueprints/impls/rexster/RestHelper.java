package com.tinkerpop.blueprints.impls.rexster;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tinkerpop.blueprints.util.io.graphson.GraphSONTokens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import static com.tinkerpop.blueprints.impls.rexster.util.JacksonUtil.getJsonNode;
import static com.tinkerpop.blueprints.impls.rexster.util.JacksonUtil.optArray;
import static com.tinkerpop.blueprints.impls.rexster.util.JacksonUtil.optObject;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 * @author Stephen Mallette (http://stephen.genoprime.com)
 */
final class RestHelper {

    public static RexsterAuthentication Authentication;
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";

    static ObjectNode get(final String uri) {
        try {
            final URLConnection connection = createConnection(uri, null, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON);
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            return getJsonNode(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    static ArrayNode getResultArray(final String uri) {
        return optArray(RestHelper.get(safeUri(uri)), RexsterTokens.RESULTS);
    }

    static ObjectNode getResultObject(final String uri) {
        return optObject(RestHelper.get(safeUri(uri)), RexsterTokens.RESULTS);
    }

    static ArrayNode postResultArray(final String uri, final ObjectNode json) {
        return optArray(post(uri, json.toString(), RexsterTokens.APPLICATION_JSON,
                RexsterTokens.APPLICATION_JSON, false), RexsterTokens.RESULTS);
    }

    static ObjectNode postResultObject(final String uri) {
        return optObject(post(uri, postData(uri), null, RexsterTokens.APPLICATION_JSON, false), RexsterTokens.RESULTS);
    }

    static ObjectNode postResultObject(final String uri, final ObjectNode json) {
        return optObject(post(uri, json.toString(), RexsterTokens.APPLICATION_REXSTER_TYPED_JSON,
                RexsterTokens.APPLICATION_REXSTER_TYPED_JSON, false), RexsterTokens.RESULTS);
    }

    static void post(final String uri) {
        post(uri, postData(uri), null, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON, false);
    }

    static void delete(final String uri) {
        act(uri, DELETE, null, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON);
    }

    static void put(final String uri) {
        act(uri, PUT, null, RexsterTokens.APPLICATION_REXSTER_TYPED_JSON);
    }

    static Object typeCast(final String type, final Object value) {
        if (type.equals(GraphSONTokens.TYPE_STRING))
            return value.toString();
        else if (type.equals(GraphSONTokens.TYPE_BOOLEAN))
            return Boolean.valueOf(value.toString());
        else if (type.equals(GraphSONTokens.TYPE_INTEGER))
            return Integer.valueOf(value.toString());
        else if (type.equals(GraphSONTokens.TYPE_LONG))
            return Long.valueOf(value.toString());
        else if (type.equals(GraphSONTokens.TYPE_DOUBLE))
            return Double.valueOf(value.toString());
        else if (type.equals(GraphSONTokens.TYPE_FLOAT))
            return Float.valueOf(value.toString());
        else
            return value;
    }

    static String uriCast(final Object value) {
        if (value == null)
            return "(null,\"\")";
        if (value instanceof Boolean)
            return "(" + GraphSONTokens.TYPE_BOOLEAN + "," + value + ")";
        else if (value instanceof Integer)
            return "(" + GraphSONTokens.TYPE_INTEGER + "," + value + ")";
        else if (value instanceof Long)
            return "(" + GraphSONTokens.TYPE_LONG + "," + value + ")";
        else if (value instanceof Float)
            return "(" + GraphSONTokens.TYPE_FLOAT + "," + value + ")";
        else if (value instanceof Double)
            return "(" + GraphSONTokens.TYPE_DOUBLE + "," + value + ")";
        else
            return "(s," + value.toString() + ")";

    }

    static String encode(final Object id) {
        if (id instanceof String)
            return URLEncoder.encode(id.toString());
        else
            return id.toString();
    }

    private static void act(final String uri, final String verb, final String contentType,
                            final String accept) {
        try {
            final HttpURLConnection connection = createConnection(uri, contentType, accept);
            connection.setRequestMethod(verb);
            new InputStreamReader(connection.getInputStream()).close();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static HttpURLConnection createConnection(final String uri, final String contentType,
                                                      final String accept) throws IOException {
        final URL url = new URL(safeUri(uri));
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        if (contentType != null) {
            connection.setRequestProperty("Content-Type", contentType);
        }

        if (accept != null) {
            connection.setRequestProperty(RexsterTokens.ACCEPT, accept);
        }

        if (Authentication.isAuthenticationEnabled()) {
            connection.setRequestProperty(RexsterTokens.AUTHORIZATION, Authentication.getAuthenticationHeaderValue());
        }
        return connection;
    }

    private static ObjectNode post(final String uri, final String postData, final String contentType,
                                   final String accept, final boolean noResult) {
        try {
            final HttpURLConnection connection = createConnection(uri, contentType, accept);
            connection.setDoOutput(true);

            final OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
            writer.write(postData); // post data with Content-Length automatically set
            writer.close();

            if (noResult) {
                new InputStreamReader(connection.getInputStream()).close();
                return null;
            } else {
                return getJsonNode(connection.getInputStream());
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private static String postUri(final String uri) {
        String url = "";
        final String safeUri = safeUri(uri);
        final int sep = safeUri.indexOf("?");
        if (sep == -1)
            url = safeUri;
        else
            url = safeUri.substring(0, sep);
        return url;
    }

    private static String postData(final String uri) {
        String data = null;
        final String safeUri = safeUri(uri);
        final int sep = safeUri.indexOf("?");
        if (sep == -1)
            data = "";
        else {
            data = safeUri.substring(sep + 1);
        }
        return data;
    }

    private static String safeUri(String uri) {
        // todo: make this way more safe
        return uri.replace(" ", "%20");
    }

    private static String convertStreamToString(final InputStream is) throws Exception {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        final StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line + "\n");
        }
        is.close();
        return sb.toString();
    }
}