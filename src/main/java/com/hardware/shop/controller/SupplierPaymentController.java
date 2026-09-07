package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.SupplierPayment;
import com.hardware.shop.service.SupplierPaymentService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SupplierPaymentController implements HttpHandler {

    private final SupplierPaymentService supplierPaymentService;
    private final Gson gson;

    public SupplierPaymentController() {

        this.supplierPaymentService = new SupplierPaymentService();

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

            if ("POST".equalsIgnoreCase(method) && path.equals("/api/supplier-payments")) {

                handleCreatePayment(exchange);

            } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/supplier-payments")) {

                handleGetAllPayments(exchange);

            } else if ("GET".equalsIgnoreCase(method)) {

                handleGetSupplierPayments(exchange, path);

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

    private void handleCreatePayment(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        SupplierPayment payment = gson.fromJson(requestBody, SupplierPayment.class);

        if (payment == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        boolean created = supplierPaymentService.createPayment(payment);

        if (created) {

            sendResponse(exchange, 201, "{\"message\":\"Supplier payment created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create supplier payment\"}");
        }
    }

    private void handleGetAllPayments(HttpExchange exchange) throws IOException {

        try {

            List<SupplierPayment> payments = supplierPaymentService.findAll();

            String response = gson.toJson(payments);

            sendResponse(exchange, 200, response);

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Failed to retrieve supplier payments\"}");
        }
    }

    private void handleGetSupplierPayments(HttpExchange exchange, String path) throws Exception {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            sendResponse(exchange, 400, "{\"error\":\"Invalid supplier payment URL\"}");

            return;
        }

        int supplierId;

        try {

            supplierId = Integer.parseInt(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Supplier ID must be a valid number.");
        }

        List<SupplierPayment> payments = supplierPaymentService.findBySupplierId(supplierId);

        String response = gson.toJson(payments);

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