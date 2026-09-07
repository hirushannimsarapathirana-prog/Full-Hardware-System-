package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.User;
import com.hardware.shop.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class AuthController implements HttpHandler {

    private final UserService userService;
    private final Gson gson;

    public AuthController() {
        this.userService = new UserService();
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return;
        }

        try {
            String path = exchange.getRequestURI().getPath();

            if (path.equals("/api/auth/login") && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                handleLogin(exchange);

            } else if (path.equals("/api/auth/register") && "POST".equalsIgnoreCase(exchange.getRequestMethod())) {

                handleRegister(exchange);

            } else {

                sendResponse(exchange, 404, "{\"error\":\"Endpoint not found\"}");
            }

        } catch (IllegalArgumentException e) {

            sendResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleLogin(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        LoginRequest request = gson.fromJson(requestBody, LoginRequest.class);

        User user = userService.login(request.username, request.password);

        user.setPasswordHash(null);

        sendResponse(exchange, 200, gson.toJson(user));
    }

    private void handleRegister(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        RegisterRequest request = gson.fromJson(requestBody, RegisterRequest.class);

        User user = new User();

        user.setUsername(request.username);
        user.setFullName(request.fullName);
        user.setEmail(request.email);
        user.setPhone(request.phone);
        user.setStatus("ACTIVE");

        boolean saved = userService.register(user, request.password);

        if (saved) {

            sendResponse(exchange, 201, "{\"message\":\"User registered successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to register user\"}");
        }
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        exchange.getResponseBody().write(responseBytes);
        exchange.getResponseBody().close();
    }

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    }

    private String escapeJson(String text) {

        if (text == null) {
            return "";
        }

        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private static class LoginRequest {
        String username;
        String password;
    }

    private static class RegisterRequest {
        String username;
        String password;
        String fullName;
        String email;
        String phone;
    }
}