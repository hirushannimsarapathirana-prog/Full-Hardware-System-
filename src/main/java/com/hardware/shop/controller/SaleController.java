package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.dto.SaleDetailsResponse;
import com.hardware.shop.model.Sale;
import com.hardware.shop.model.SaleItem;
import com.hardware.shop.service.SaleService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SaleController implements HttpHandler {

    private final SaleService saleService;
    private final Gson gson;

    public SaleController() {

        this.saleService = new SaleService();
        this.gson = new Gson();
    }


    // =========================================================
    // HANDLE REQUEST
    // =========================================================

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        // -----------------------------------------------------
        // OPTIONS
        // -----------------------------------------------------

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);

            exchange.close();

            return;
        }


        try {

            String method = exchange.getRequestMethod();

            String path = exchange.getRequestURI().getPath();

            // -------------------------------------------------
            // POST /api/sales
            // -------------------------------------------------

            if ("POST".equalsIgnoreCase(method)) {

                handleCreateSale(exchange);

            }

            // -------------------------------------------------
            // GET /api/sales
            // -------------------------------------------------

            else if ("GET".equalsIgnoreCase(method) && path.equals("/api/sales")) {

                handleGetAllSales(exchange);

            }

            // -------------------------------------------------
            // GET /api/sales/{id}
            // -------------------------------------------------

            else if ("GET".equalsIgnoreCase(method)) {

                handleGetSaleById(exchange, path);

            }

            // -------------------------------------------------
            // Method not allowed
            // -------------------------------------------------

            else {

                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");
            }

        } catch (IllegalArgumentException e) {

            sendResponse(exchange, 400, "{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }


    // =========================================================
    // CREATE SALE
    // =========================================================

    private void handleCreateSale(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        SaleRequest request = gson.fromJson(requestBody, SaleRequest.class);

        if (request == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        boolean created = saleService.createSale(request.sale, request.items, request.paymentMethod);

        if (created) {

            sendResponse(exchange, 201, "{\"message\":\"Sale created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create sale\"}");
        }
    }


    // =========================================================
    // GET ALL SALES
    // =========================================================

    private void handleGetAllSales(HttpExchange exchange) throws IOException {

        try {

            List<Sale> sales = saleService.findAll();

            String response = gson.toJson(sales);

            sendResponse(exchange, 200, response);

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Failed to retrieve sales\"}");
        }
    }


    // =========================================================
    // GET SALE BY ID
    // =========================================================

    private void handleGetSaleById(HttpExchange exchange, String path) throws Exception {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            sendResponse(exchange, 400, "{\"error\":\"Invalid sale URL\"}");

            return;
        }

        long saleId;

        try {

            saleId = Long.parseLong(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Sale ID must be a valid number.");
        }


        SaleDetailsResponse details = saleService.findDetails(saleId);

        if (details == null) {

            sendResponse(exchange, 404, "{\"error\":\"Sale not found\"}");

            return;
        }

        String response = gson.toJson(details);

        sendResponse(exchange, 200, response);
    }


    // =========================================================
    // SEND RESPONSE
    // =========================================================

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        exchange.getResponseBody().write(responseBytes);

        exchange.getResponseBody().close();
    }


    // =========================================================
    // CORS HEADERS
    // =========================================================

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    }


    // =========================================================
    // ESCAPE JSON
    // =========================================================

    private String escapeJson(String text) {

        if (text == null) {
            return "";
        }

        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }


    // =========================================================
    // REQUEST DTO
    // =========================================================

    private static class SaleRequest {

        Sale sale;

        List<SaleItem> items;

        String paymentMethod;
    }
}