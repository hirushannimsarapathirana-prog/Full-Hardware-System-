package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.Brand;
import com.hardware.shop.service.BrandService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BrandController implements HttpHandler {

    private final BrandService brandService;
    private final Gson gson;

    public BrandController() {
        this.brandService = new BrandService();
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
        String basePath = "/api/brands";

        if (path.equals(basePath) || path.equals(basePath + "/")) {

            List<Brand> brands = brandService.findAll();

            sendResponse(exchange, 200, gson.toJson(brands));

        } else {

            String idText = path.substring((basePath + "/").length());

            int id = Integer.parseInt(idText);

            Brand brand = brandService.findById(id);

            if (brand == null) {

                sendResponse(exchange, 404, "{\"error\":\"Brand not found\"}");

                return;
            }

            sendResponse(exchange, 200, gson.toJson(brand));
        }
    }

    private void handlePost(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Brand brand = gson.fromJson(requestBody, Brand.class);

        boolean saved = brandService.save(brand);

        if (saved) {

            sendResponse(exchange, 201, "{\"message\":\"Brand created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create brand\"}");
        }
    }

    private void handlePut(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/brands/";

        String idText = path.substring(basePath.length());

        int id = Integer.parseInt(idText);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Brand brand = gson.fromJson(requestBody, Brand.class);

        brand.setId(id);

        boolean updated = brandService.update(brand);

        if (updated) {

            sendResponse(exchange, 200, "{\"message\":\"Brand updated successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Brand not found\"}");
        }
    }

    private void handleDelete(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/brands/";

        String idText = path.substring(basePath.length());

        int id = Integer.parseInt(idText);

        boolean deleted = brandService.delete(id);

        if (deleted) {

            sendResponse(exchange, 200, "{\"message\":\"Brand deleted successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Brand not found\"}");
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