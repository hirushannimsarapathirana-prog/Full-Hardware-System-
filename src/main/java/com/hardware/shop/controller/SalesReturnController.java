package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.dto.SalesReturnDetailsResponse;
import com.hardware.shop.model.SalesReturn;
import com.hardware.shop.model.SalesReturnItem;
import com.hardware.shop.service.SalesReturnService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SalesReturnController implements HttpHandler {

    private final SalesReturnService salesReturnService;
    private final Gson gson;

    public SalesReturnController() {

        this.salesReturnService = new SalesReturnService();

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

            if ("POST".equalsIgnoreCase(method) && path.equals("/api/sales-returns")) {

                handleCreateReturn(exchange);

            } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/sales-returns")) {

                handleGetAllReturns(exchange);

            } else if ("GET".equalsIgnoreCase(method)) {

                handleGetReturnById(exchange, path);

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

    private void handleCreateReturn(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        SalesReturnRequest request = gson.fromJson(requestBody, SalesReturnRequest.class);

        if (request == null || request.salesReturn == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        boolean created = salesReturnService.createSalesReturn(request.salesReturn, request.items);

        if (created) {

            sendResponse(exchange, 201, "{\"message\":\"Sales return created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create sales return\"}");
        }
    }

    private void handleGetAllReturns(HttpExchange exchange) throws Exception {

        List<SalesReturn> returns = salesReturnService.findAll();

        String response = gson.toJson(returns);

        sendResponse(exchange, 200, response);
    }

    private void handleGetReturnById(HttpExchange exchange, String path) throws Exception {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            sendResponse(exchange, 400, "{\"error\":\"Invalid sales return URL\"}");

            return;
        }

        long returnId;

        try {

            returnId = Long.parseLong(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Return ID must be a valid number.");
        }

        SalesReturn salesReturn = salesReturnService.findById(returnId);

        if (salesReturn == null) {

            sendResponse(exchange, 404, "{\"error\":\"Sales return not found\"}");

            return;
        }

        List<SalesReturnItem> items = salesReturnService.findItemsByReturnId(returnId);

        SalesReturnDetailsResponse response = new SalesReturnDetailsResponse(salesReturn, items);

        sendResponse(exchange, 200, gson.toJson(response));
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

    private static class SalesReturnRequest {

        SalesReturn salesReturn;
        List<SalesReturnItem> items;
    }
}