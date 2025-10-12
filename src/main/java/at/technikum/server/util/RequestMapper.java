package at.technikum.server.util;

import at.technikum.server.http.Request;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestMapper {

    public Request fromExchange(HttpExchange exchange) {
        Request request = new Request();
        request.setMethod(exchange.getRequestMethod());
        request.setPath(exchange.getRequestURI().getPath());

                // Collect headers (first value; keys lower-case)
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, List<String>> headerEntry : exchange.getRequestHeaders().entrySet()) {
            String key = headerEntry.getKey();
            if (key == null) continue;
            List<String> values = headerEntry.getValue();
            if (values != null && !values.isEmpty()) {
                headers.put(key.toLowerCase(), values.get(0));
            }
        }
        request.setHeaders(headers);

        // Read body safely (also fine for GET with empty body)
        String body = readBody(exchange);
        request.setBody(body);

        return request;
    }

    private String readBody(HttpExchange exchange) {
        // Determine charset from Content-Type header, default to UTF-8
        Charset charset = charsetFromContentType(exchange);
        // If there is no body, return null (keep prior semantics of showing <leer>)
        try (InputStream is = exchange.getRequestBody();
             ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            if (is == null) {
                return null;
            }
            byte[] data = new byte[4096];
            int nRead;
            boolean hasData = false;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                if (nRead > 0) {
                    hasData = true;
                    buffer.write(data, 0, nRead);
                }
            }
            if (!hasData) {
                return null; // no body sent
            }
            byte[] bytes = buffer.toByteArray();
            return new String(bytes, charset);
        } catch (IOException e) {
            // On read errors, return null to avoid breaking the request mapping
            return null;
        }
    }

    private Charset charsetFromContentType(HttpExchange exchange) {
        List<String> headers = exchange.getRequestHeaders().get("Content-Type");
        if (headers != null) {
            for (String h : headers) {
                if (h != null) {
                    String lower = h.toLowerCase();
                    int i = lower.indexOf("charset=");
                    if (i >= 0) {
                        String cs = lower.substring(i + 8).trim();
                        // Remove trailing parameters if any
                        int sc = cs.indexOf(';');
                        if (sc >= 0) cs = cs.substring(0, sc).trim();
                        try {
                            return Charset.forName(cs.toUpperCase());
                        } catch (Exception ignore) {
                            // fall back
                        }
                    }
                }
            }
        }
        return StandardCharsets.UTF_8;
    }
}
