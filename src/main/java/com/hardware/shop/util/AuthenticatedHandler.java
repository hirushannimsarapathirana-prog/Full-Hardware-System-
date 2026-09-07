package com.hardware.shop.util;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthenticatedHandler implements HttpHandler {

    private final HttpHandler delegate;

    public AuthenticatedHandler(HttpHandler delegate) {

        this.delegate = delegate;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);

            exchange.close();
            return;
        }

        String authorization = exchange.getRequestHeaders().getFirst("Authorization");

        String token = extractBearerToken(authorization);

        if (!AuthSessionManager.isValidToken(token)) {

            sendUnauthorized(exchange);

            return;
        }

        delegate.handle(exchange);
    }

    private String extractBearerToken(String authorization) {

        if (authorization == null || authorization.isBlank()) {

            return null;
        }

        if (!authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {

            return null;
        }

        String token = authorization.substring(7).trim();

        return token.isBlank() ? null : token;
    }

    private void sendUnauthorized(HttpExchange exchange) throws IOException {

        String response = "{\"error\":\"Unauthorized. Valid Admin login token is required.\"}";

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(401, bytes.length);

        exchange.getResponseBody().write(bytes);
        exchange.getResponseBody().close();
    }

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    }
}