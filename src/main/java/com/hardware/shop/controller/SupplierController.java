package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.Supplier;
import com.hardware.shop.service.SupplierService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SupplierController implements HttpHandler {

    private final SupplierService supplierService;
    private final Gson gson;

    public SupplierController() {
        this.supplierService = new SupplierService();
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

            switch (method) {
                case "GET" -> handleGet(exchange);
                case "POST" -> handlePost(exchange);
                case "PUT" -> handlePut(exchange);
                case "DELETE" -> handleDelete(exchange);
                default -> sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }

        } catch (IllegalArgumentException e) {

            sendResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    private void handleGet(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/suppliers";

        if (path.equals(basePath) || path.equals(basePath + "/")) {

            List<Supplier> suppliers = supplierService.findAll();

            sendResponse(exchange, 200, gson.toJson(suppliers));

        } else {

            String idText = path.substring((basePath + "/").length());

            int id = Integer.parseInt(idText);

            Supplier supplier = supplierService.findById(id);

            if (supplier == null) {

                sendResponse(exchange, 404, "{\"error\":\"Supplier not found\"}");

                return;
            }

            sendResponse(exchange, 200, gson.toJson(supplier));
        }
    }

    private void handlePost(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Supplier supplier = gson.fromJson(requestBody, Supplier.class);

        boolean saved = supplierService.save(supplier);

        if (saved) {

            sendResponse(exchange, 201, "{\"message\":\"Supplier created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create supplier\"}");
        }
    }

    private void handlePut(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/suppliers/";

        String idText = path.substring(basePath.length());

        int id = Integer.parseInt(idText);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Supplier supplier = gson.fromJson(requestBody, Supplier.class);

        supplier.setId(id);

        boolean updated = supplierService.update(supplier);

        if (updated) {

            sendResponse(exchange, 200, "{\"message\":\"Supplier updated successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Supplier not found\"}");
        }
    }

    private void handleDelete(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/suppliers/";

        String idText = path.substring(basePath.length());

        int id = Integer.parseInt(idText);

        boolean deleted = supplierService.delete(id);

        if (deleted) {

            sendResponse(exchange, 200, "{\"message\":\"Supplier deleted successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Supplier not found\"}");
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
}