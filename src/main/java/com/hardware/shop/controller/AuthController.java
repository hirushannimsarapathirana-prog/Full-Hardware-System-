package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.User;
import com.hardware.shop.service.UserService;
import com.hardware.shop.util.AuthSessionManager;
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
    public void handle(
            HttpExchange exchange
    ) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(
                exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(
                    204,
                    -1
            );

            exchange.close();
            return;
        }

        try {

            String path =
                    exchange.getRequestURI()
                            .getPath();

            String method =
                    exchange.getRequestMethod();

            /*
             * PUBLIC LOGIN
             */
            if (path.equals("/api/auth/login")
                    && "POST".equalsIgnoreCase(method)) {

                handleLogin(exchange);
                return;
            }

            /*
             * PROTECTED REGISTER
             *
             * Because this is an Admin-only system,
             * only an authenticated Admin can create
             * another user.
             */
            if (path.equals("/api/auth/register")
                    && "POST".equalsIgnoreCase(method)) {

                if (!isAuthenticated(exchange)) {

                    sendError(
                            exchange,
                            401,
                            "Unauthorized. Admin login token is required."
                    );

                    return;
                }

                handleRegister(exchange);
                return;
            }

            /*
             * PROTECTED LOGOUT
             */
            if (path.equals("/api/auth/logout")
                    && "POST".equalsIgnoreCase(method)) {

                handleLogout(exchange);
                return;
            }

            sendError(
                    exchange,
                    404,
                    "Endpoint not found."
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    exchange,
                    400,
                    e.getMessage()
            );

        } catch (Exception e) {

            e.printStackTrace();

            sendError(
                    exchange,
                    500,
                    "Internal server error."
            );
        }
    }

    private void handleLogin(
            HttpExchange exchange
    ) throws Exception {

        String requestBody =
                new String(
                        exchange.getRequestBody()
                                .readAllBytes(),
                        StandardCharsets.UTF_8
                );

        if (requestBody.isBlank()) {

            sendError(
                    exchange,
                    400,
                    "Request body is required."
            );

            return;
        }

        LoginRequest request =
                gson.fromJson(
                        requestBody,
                        LoginRequest.class
                );

        if (request == null
                || request.username == null
                || request.password == null
                || request.username.isBlank()
                || request.password.isBlank()) {

            sendError(
                    exchange,
                    400,
                    "Username and password are required."
            );

            return;
        }

        User user =
                userService.login(
                        request.username.trim(),
                        request.password
                );

        if (user == null) {

            sendError(
                    exchange,
                    401,
                    "Invalid username or password."
            );

            return;
        }

        /*
         * Only ACTIVE users can login.
         */
        if (user.getStatus() == null
                || !"ACTIVE".equalsIgnoreCase(
                user.getStatus()
        )) {

            sendError(
                    exchange,
                    403,
                    "User account is inactive."
            );

            return;
        }

        /*
         * This system has one role only:
         * ADMIN.
         */
        String token =
                AuthSessionManager.createSession(
                        user.getId(),
                        user.getUsername()
                );

        LoginResponse response =
                new LoginResponse();

        response.success = true;
        response.message =
                "Login successful.";
        response.token = token;
        response.userId = user.getId();
        response.username =
                user.getUsername();
        response.fullName =
                user.getFullName();
        response.email =
                user.getEmail();
        response.phone =
                user.getPhone();
        response.role = "ADMIN";

        /*
         * Never expose password hash.
         */
        user.setPasswordHash(null);

        sendJson(
                exchange,
                200,
                response
        );
    }

    private void handleRegister(
            HttpExchange exchange
    ) throws Exception {

        String requestBody =
                new String(
                        exchange.getRequestBody()
                                .readAllBytes(),
                        StandardCharsets.UTF_8
                );

        if (requestBody.isBlank()) {

            sendError(
                    exchange,
                    400,
                    "Request body is required."
            );

            return;
        }

        RegisterRequest request =
                gson.fromJson(
                        requestBody,
                        RegisterRequest.class
                );

        if (request == null) {

            sendError(
                    exchange,
                    400,
                    "Invalid request."
            );

            return;
        }

        if (request.username == null
                || request.username.isBlank()) {

            sendError(
                    exchange,
                    400,
                    "Username is required."
            );

            return;
        }

        if (request.password == null
                || request.password.isBlank()) {

            sendError(
                    exchange,
                    400,
                    "Password is required."
            );

            return;
        }

        if (request.fullName == null
                || request.fullName.isBlank()) {

            sendError(
                    exchange,
                    400,
                    "Full name is required."
            );

            return;
        }

        User user = new User();

        user.setUsername(
                request.username.trim()
        );

        user.setFullName(
                request.fullName.trim()
        );

        user.setEmail(
                request.email
        );

        user.setPhone(
                request.phone
        );

        user.setStatus("ACTIVE");

        boolean saved =
                userService.register(
                        user,
                        request.password
                );

        if (saved) {

            sendResponse(
                    exchange,
                    201,
                    "{\"message\":\"User registered successfully\"}"
            );

        } else {

            sendError(
                    exchange,
                    500,
                    "Failed to register user."
            );
        }
    }

    private void handleLogout(
            HttpExchange exchange
    ) throws IOException {

        String token =
                getBearerToken(exchange);

        if (!AuthSessionManager.isValidToken(
                token
        )) {

            sendError(
                    exchange,
                    401,
                    "Invalid or missing authentication token."
            );

            return;
        }

        AuthSessionManager.removeSession(
                token
        );

        sendResponse(
                exchange,
                200,
                "{\"success\":true,\"message\":\"Logout successful\"}"
        );
    }

    private boolean isAuthenticated(
            HttpExchange exchange
    ) {

        String token =
                getBearerToken(exchange);

        return AuthSessionManager.isValidToken(
                token
        );
    }

    private String getBearerToken(
            HttpExchange exchange
    ) {

        String authorization =
                exchange.getRequestHeaders()
                        .getFirst("Authorization");

        if (authorization == null
                || authorization.isBlank()) {

            return null;
        }

        if (!authorization
                .regionMatches(
                        true,
                        0,
                        "Bearer ",
                        0,
                        7
                )) {

            return null;
        }

        String token =
                authorization.substring(7).trim();

        return token.isBlank()
                ? null
                : token;
    }

    private void sendJson(
            HttpExchange exchange,
            int statusCode,
            Object object
    ) throws IOException {

        sendResponse(
                exchange,
                statusCode,
                gson.toJson(object)
        );
    }

    private void sendError(
            HttpExchange exchange,
            int statusCode,
            String message
    ) throws IOException {

        ErrorResponse response =
                new ErrorResponse();

        response.error = message;

        sendJson(
                exchange,
                statusCode,
                response
        );
    }

    private void sendResponse(
            HttpExchange exchange,
            int statusCode,
            String response
    ) throws IOException {

        byte[] responseBytes =
                response.getBytes(
                        StandardCharsets.UTF_8
                );

        exchange.getResponseHeaders().set(
                "Content-Type",
                "application/json; charset=UTF-8"
        );

        exchange.sendResponseHeaders(
                statusCode,
                responseBytes.length
        );

        exchange.getResponseBody().write(
                responseBytes
        );

        exchange.getResponseBody().close();
    }

    private void addCorsHeaders(
            HttpExchange exchange
    ) {

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Origin",
                "*"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Headers",
                "Content-Type, Authorization"
        );

        exchange.getResponseHeaders().set(
                "Access-Control-Allow-Methods",
                "GET,POST,PUT,DELETE,OPTIONS"
        );
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

    private static class LoginResponse {

        boolean success;
        String message;
        String token;
        int userId;
        String username;
        String fullName;
        String email;
        String phone;
        String role;
    }

    private static class ErrorResponse {

        String error;
    }
}