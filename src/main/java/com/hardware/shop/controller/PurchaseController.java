package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.dto.PurchaseDetailsResponse;
import com.hardware.shop.model.Purchase;
import com.hardware.shop.model.PurchaseItem;
import com.hardware.shop.service.PurchaseService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class PurchaseController implements HttpHandler {

    private final PurchaseService purchaseService;
    private final Gson gson;

    public PurchaseController() {

        this.purchaseService = new PurchaseService();

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

                handleGet(exchange, path);

            } else if ("POST".equalsIgnoreCase(method)) {

                handleCreatePurchase(exchange);

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

    private void handleGet(HttpExchange exchange, String path) throws Exception {

        String basePath = "/api/purchases";

        // GET /api/purchases
        if (path.equals(basePath) || path.equals(basePath + "/")) {

            List<Purchase> purchases = purchaseService.findAll();

            sendResponse(exchange, 200, gson.toJson(purchases));

            return;
        }

        // GET /api/purchases/{id}

        String idText = path.substring((basePath + "/").length());

        long id = Long.parseLong(idText);

        PurchaseDetailsResponse details = purchaseService.findDetails(id);

        if (details == null) {

            sendResponse(exchange, 404, "{\"error\":\"Purchase not found\"}");

            return;
        }

        sendResponse(exchange, 200, gson.toJson(details));
    }

    private void handleCreatePurchase(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        PurchaseRequest request = gson.fromJson(requestBody, PurchaseRequest.class);

        if (request == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        boolean created = purchaseService.createPurchase(request.purchase, request.items, request.paymentMethod);

        if (created) {

            sendResponse(exchange, 201, "{\"message\":\"Purchase created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create purchase\"}");
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

    private static class PurchaseRequest {

        Purchase purchase;

        List<PurchaseItem> items;

        String paymentMethod;
    }
}