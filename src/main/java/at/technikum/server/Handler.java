package at.technikum.server;

import at.technikum.application.common.Application;
import at.technikum.application.common.ExceptionMapper;
import at.technikum.server.http.Request;
import at.technikum.server.http.Response;
import at.technikum.server.util.RequestMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;

public class Handler implements HttpHandler {

    private final Application application;
    private final RequestMapper requestMapper;

    public Handler(Application application, RequestMapper requestMapper) {
        this.application = application;
        this.requestMapper = requestMapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Request request = requestMapper.fromExchange(exchange);
        Response response;
        try {
            response = application.handle(request);
        } catch (Exception e) {
            response = ExceptionMapper.toResponse(e);
        }
        send(exchange, response);
        // create Request object
        // give Request to Application
        // receive Response object
        // send Response to client
    }

    private void send(HttpExchange exchange, Response response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", response.getContentType());
        byte[] bytes = response.getBodyBytes(); // nutzt nun Bytes direkt
        if (bytes == null) {
            bytes = new byte[0];
        }
        exchange.sendResponseHeaders(response.getStatusCode(), bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
