package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.dto.PurchaseReturnDetailsResponse;
import com.hardware.shop.model.PurchaseReturn;
import com.hardware.shop.model.PurchaseReturnItem;
import com.hardware.shop.service.PurchaseReturnService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PurchaseReturnController implements HttpHandler {

    private final PurchaseReturnService purchaseReturnService;
    private final Gson gson;

    public PurchaseReturnController() {
        this.purchaseReturnService = new PurchaseReturnService();
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

            if ("POST".equalsIgnoreCase(method) && path.equals("/api/purchase-returns")) {

                handleCreateReturn(exchange);

            } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/purchase-returns")) {

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

        PurchaseReturnRequest request = gson.fromJson(requestBody, PurchaseReturnRequest.class);

        if (request == null || request.purchaseReturn == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        boolean created = purchaseReturnService.createPurchaseReturn(request.purchaseReturn, request.items);

        if (created) {

            sendResponse(exchange, 201, "{\"message\":\"Purchase return created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create purchase return\"}");
        }
    }

    private void handleGetAllReturns(HttpExchange exchange) throws Exception {

        List<PurchaseReturn> returns = purchaseReturnService.findAll();

        sendResponse(exchange, 200, gson.toJson(returns));
    }

    private void handleGetReturnById(HttpExchange exchange, String path) throws Exception {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            sendResponse(exchange, 400, "{\"error\":\"Invalid purchase return URL\"}");

            return;
        }

        long returnId;

        try {

            returnId = Long.parseLong(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Return ID must be a valid number.");
        }

        PurchaseReturn purchaseReturn = purchaseReturnService.findById(returnId);

        if (purchaseReturn == null) {

            sendResponse(exchange, 404, "{\"error\":\"Purchase return not found\"}");

            return;
        }

        List<PurchaseReturnItem> items = purchaseReturnService.findItemsByReturnId(returnId);

        PurchaseReturnDetailsResponse response = new PurchaseReturnDetailsResponse(purchaseReturn, items);

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

    private static class PurchaseReturnRequest {

        PurchaseReturn purchaseReturn;

        List<PurchaseReturnItem> items;
    }
}