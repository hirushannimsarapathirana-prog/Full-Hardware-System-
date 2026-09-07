package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.User;
import com.hardware.shop.service.UserService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UserController implements HttpHandler {

    private final UserService userService;
    private final Gson gson;

    public UserController() {

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

            String method = exchange.getRequestMethod();

            String path = exchange.getRequestURI().getPath();

            String basePath = "/api/users";

            if (!path.equals(basePath) && !path.startsWith(basePath + "/")) {

                sendError(exchange, 404, "User endpoint not found.");

                return;
            }

            if ("GET".equalsIgnoreCase(method)) {

                handleGet(exchange, path, basePath);

                return;
            }

            if ("POST".equalsIgnoreCase(method) && path.equals(basePath)) {

                handleCreate(exchange);
                return;
            }

            if ("PUT".equalsIgnoreCase(method)) {

                handleUpdate(exchange, path, basePath);

                return;
            }

            if ("DELETE".equalsIgnoreCase(method)) {

                handleDelete(exchange, path, basePath);

                return;
            }

            sendError(exchange, 405, "Method not allowed.");

        } catch (IllegalArgumentException e) {

            sendError(exchange, 400, e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            sendError(exchange, 500, "Internal server error.");
        }
    }

    private void handleGet(HttpExchange exchange, String path, String basePath) throws Exception {

        /*
         * GET /api/users
         *
         * Return all users.
         */
        if (path.equals(basePath)) {

            List<User> users = userService.findAll();

            /*
             * Never expose password hashes.
             */
            for (User user : users) {

                user.setPasswordHash(null);
            }

            sendJson(exchange, 200, users);

            return;
        }

        /*
         * GET /api/users/{id}
         *
         * Return one user.
         */
        int id = extractId(path, basePath);

        User user = userService.findById(id);

        if (user == null) {

            sendError(exchange, 404, "User not found.");

            return;
        }

        /*
         * Never expose password hash.
         */
        user.setPasswordHash(null);

        sendJson(exchange, 200, user);
    }

    private void handleCreate(HttpExchange exchange) throws Exception {

        String body = readBody(exchange);

        if (body.isBlank()) {

            sendError(exchange, 400, "Request body is required.");

            return;
        }

        CreateUserRequest request = gson.fromJson(body, CreateUserRequest.class);

        if (request == null) {

            sendError(exchange, 400, "Invalid request.");

            return;
        }

        if (request.username == null || request.username.isBlank()) {

            sendError(exchange, 400, "Username is required.");

            return;
        }

        if (request.password == null || request.password.isBlank()) {

            sendError(exchange, 400, "Password is required.");

            return;
        }

        if (request.fullName == null || request.fullName.isBlank()) {

            sendError(exchange, 400, "Full name is required.");

            return;
        }

        User user = new User();

        user.setUsername(request.username.trim());

        user.setFullName(request.fullName.trim());

        user.setEmail(request.email);

        user.setPhone(request.phone);

        user.setStatus(request.status == null || request.status.isBlank() ? "ACTIVE" : request.status);

        boolean saved = userService.register(user, request.password);

        if (!saved) {

            sendError(exchange, 500, "Failed to create user.");

            return;
        }

        /*
         * Never expose password hash.
         */
        user.setPasswordHash(null);

        sendJson(exchange, 201, user);
    }

    private void handleUpdate(HttpExchange exchange, String path, String basePath) throws Exception {

        int id = extractId(path, basePath);

        String body = readBody(exchange);

        if (body.isBlank()) {

            sendError(exchange, 400, "Request body is required.");

            return;
        }

        UpdateUserRequest request = gson.fromJson(body, UpdateUserRequest.class);

        if (request == null) {

            sendError(exchange, 400, "Invalid request.");

            return;
        }

        User user = new User();

        user.setUsername(request.username);

        user.setFullName(request.fullName);

        user.setEmail(request.email);

        user.setPhone(request.phone);

        user.setStatus(request.status);

        boolean updated = userService.update(id, user, request.password);

        if (!updated) {

            sendError(exchange, 404, "User not found.");

            return;
        }

        User updatedUser = userService.findById(id);

        if (updatedUser != null) {

            /*
             * Never expose password hash.
             */
            updatedUser.setPasswordHash(null);
        }

        sendJson(exchange, 200, updatedUser);
    }

    private void handleDelete(HttpExchange exchange, String path, String basePath) throws Exception {

        int id = extractId(path, basePath);

        boolean deleted = userService.delete(id);

        if (!deleted) {

            sendError(exchange, 404, "User not found.");

            return;
        }

        sendJson(exchange, 200, new MessageResponse(true, "User deleted successfully."));
    }

    private int extractId(String path, String basePath) {

        String idPart = path.substring(basePath.length() + 1);

        if (idPart.isBlank() || idPart.contains("/")) {

            throw new IllegalArgumentException("Invalid user ID.");
        }

        try {

            return Integer.parseInt(idPart);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Invalid user ID.");
        }
    }

    private String readBody(HttpExchange exchange) throws IOException {

        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void sendJson(HttpExchange exchange, int statusCode, Object object) throws IOException {

        sendResponse(exchange, statusCode, gson.toJson(object));
    }

    private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {

        sendJson(exchange, statusCode, new ErrorResponse(message));
    }

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (var output = exchange.getResponseBody()) {

            output.write(bytes);
        }
    }

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    }

    private static class CreateUserRequest {

        String username;
        String password;
        String fullName;
        String email;
        String phone;
        String status;
    }

    private static class UpdateUserRequest {

        String username;
        String password;
        String fullName;
        String email;
        String phone;
        String status;
    }

    private static class ErrorResponse {

        String error;

        ErrorResponse(String error) {
            this.error = error;
        }
    }

    private static class MessageResponse {

        boolean success;
        String message;

        MessageResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
