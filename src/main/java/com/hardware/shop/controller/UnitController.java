package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.Unit;
import com.hardware.shop.service.UnitService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class UnitController implements HttpHandler {

    private final UnitService unitService;
    private final Gson gson;

    public UnitController() {
        this.unitService = new UnitService();
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
        String basePath = "/api/units";

        if (path.equals(basePath) || path.equals(basePath + "/")) {

            List<Unit> units = unitService.findAll();

            sendResponse(exchange, 200, gson.toJson(units));

        } else {

            String idText = path.substring((basePath + "/").length());
            int id = Integer.parseInt(idText);

            Unit unit = unitService.findById(id);

            if (unit == null) {

                sendResponse(exchange, 404, "{\"error\":\"Unit not found\"}");

                return;
            }

            sendResponse(exchange, 200, gson.toJson(unit));
        }
    }

    private void handlePost(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Unit unit = gson.fromJson(requestBody, Unit.class);

        boolean saved = unitService.save(unit);

        if (saved) {

            sendResponse(exchange, 201, "{\"message\":\"Unit created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create unit\"}");
        }
    }

    private void handlePut(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/units/";

        String idText = path.substring(basePath.length());
        int id = Integer.parseInt(idText);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Unit unit = gson.fromJson(requestBody, Unit.class);

        unit.setId(id);

        boolean updated = unitService.update(unit);

        if (updated) {

            sendResponse(exchange, 200, "{\"message\":\"Unit updated successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Unit not found\"}");
        }
    }

    private void handleDelete(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/units/";

        String idText = path.substring(basePath.length());
        int id = Integer.parseInt(idText);

        boolean deleted = unitService.delete(id);

        if (deleted) {

            sendResponse(exchange, 200, "{\"message\":\"Unit deleted successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Unit not found\"}");
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
