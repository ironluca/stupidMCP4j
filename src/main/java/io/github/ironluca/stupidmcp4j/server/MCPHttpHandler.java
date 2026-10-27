package io.github.ironluca.stupidmcp4j.server;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

class MCPHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        try {
            // 1. Inspect the HTTP method.
            String method = exchange.getRequestMethod();

            if (!"POST".equalsIgnoreCase(method)) {
                exchange.getResponseHeaders().set("Allow", "POST");
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            // 2. Inspect request headers.
            String contentType =
                    exchange.getRequestHeaders().getFirst("Content-Type");

            if (contentType == null ||
                    !contentType.toLowerCase().startsWith("application/json")) {
                sendResponse(exchange, 415, "Unsupported Media Type");
                return;
            }

            // 3. Read the request body.
            String requestBody;

            try (InputStream input = exchange.getRequestBody()) {
                requestBody = new String(
                        input.readAllBytes(),
                        StandardCharsets.UTF_8);
            }

            // 4. Process the request.
            // Replace this placeholder with your JSON-RPC/MCP dispatcher.
            String responseBody =
                    "{\"jsonrpc\":\"2.0\",\"result\":{},\"id\":1}";

            // 5. Send the response.
            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json; charset=utf-8");

            sendResponse(exchange, 200, responseBody);

        } finally {
            // 6. Release the exchange resources.
            exchange.close();
        }
    }

    private void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String responseBody) throws IOException {

        byte[] responseBytes =
                responseBody.getBytes(StandardCharsets.UTF_8);

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        try (OutputStream output = exchange.getResponseBody()) {
            output.write(responseBytes);
        }
    }
}

