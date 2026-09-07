package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.dto.CustomerBalanceResponse;
import com.hardware.shop.service.CustomerBalanceService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CustomerBalanceController implements HttpHandler {

    private final CustomerBalanceService customerBalanceService;
    private final Gson gson;

    public CustomerBalanceController() {

        this.customerBalanceService = new CustomerBalanceService();

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

            if ("GET".equalsIgnoreCase(method)) {

                handleGetBalance(exchange, path);

            } else {

                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }

        } catch (IllegalArgumentException e) {

            sendResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleGetBalance(HttpExchange exchange, String path) throws Exception {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            sendResponse(exchange, 400, "{\"error\":\"Invalid customer balance URL\"}");

            return;
        }

        int customerId;

        try {

            customerId = Integer.parseInt(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Customer ID must be a valid number.");
        }

        CustomerBalanceResponse balance = customerBalanceService.getCustomerBalance(customerId);

        String response = gson.toJson(balance);

        sendResponse(exchange, 200, response);
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
}