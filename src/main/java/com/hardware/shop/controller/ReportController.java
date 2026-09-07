package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.dto.ReportResponse;
import com.hardware.shop.service.ReportService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

public class ReportController implements HttpHandler {

    private final ReportService reportService;
    private final Gson gson;

    public ReportController() {

        this.reportService = new ReportService();

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

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {

                sendResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");

                return;
            }

            String path = exchange.getRequestURI().getPath();

            String query = exchange.getRequestURI().getQuery();

            ReportResponse response;

            // ====================================================
            // DAILY SALES
            // ====================================================

            if (path.endsWith("/daily-sales")) {

                response = reportService.getDailySales(getDateParameter(query, "date"));

                // ====================================================
                // MONTHLY SALES
                // ====================================================

            } else if (path.endsWith("/monthly-sales")) {

                response = reportService.getMonthlySales(getIntParameter(query, "year"), getIntParameter(query, "month"));

                // ====================================================
                // PROFIT
                // ====================================================

            } else if (path.endsWith("/profit")) {

                response = reportService.getProfit(getDateParameter(query, "fromDate"), getDateParameter(query, "toDate"));

                // ====================================================
                // PURCHASES
                // ====================================================

            } else if (path.endsWith("/purchases")) {

                response = reportService.getPurchases(getDateParameter(query, "fromDate"), getDateParameter(query, "toDate"));

                // ====================================================
                // PRODUCT-WISE SALES
                // ====================================================

            } else if (path.endsWith("/product-sales")) {

                response = reportService.getProductSales(getDateParameter(query, "fromDate"), getDateParameter(query, "toDate"));

                // ====================================================
                // STOCK
                // ====================================================

            } else if (path.endsWith("/stock")) {

                response = reportService.getStock();

                // ====================================================
                // LOW STOCK
                // ====================================================

            } else if (path.endsWith("/low-stock")) {

                response = reportService.getLowStock();

                // ====================================================
                // CUSTOMER OUTSTANDING
                // ====================================================

            } else if (path.endsWith("/customer-outstanding")) {

                response = reportService.getCustomerOutstanding();

                // ====================================================
                // SUPPLIER OUTSTANDING
                // ====================================================

            } else if (path.endsWith("/supplier-outstanding")) {

                response = reportService.getSupplierOutstanding();

                // ====================================================
                // NOT FOUND
                // ====================================================

            } else {

                sendResponse(exchange, 404, "{\"error\":\"Report endpoint not found\"}");

                return;
            }

            sendResponse(exchange, 200, gson.toJson(response));

        } catch (IllegalArgumentException e) {

            sendResponse(exchange, 400, gson.toJson(new ErrorResponse(e.getMessage())));

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
        }
    }

    // ============================================================
    // DATE PARAMETER
    // ============================================================

    private LocalDate getDateParameter(String query, String parameter) {

        String value = getQueryParameter(query, parameter);

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(parameter + " is required.");
        }

        try {

            return LocalDate.parse(value);

        } catch (Exception e) {

            throw new IllegalArgumentException("Invalid " + parameter + ". Use yyyy-MM-dd.");
        }
    }

    // ============================================================
    // INTEGER PARAMETER
    // ============================================================

    private int getIntParameter(String query, String parameter) {

        String value = getQueryParameter(query, parameter);

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(parameter + " is required.");
        }

        try {

            return Integer.parseInt(value);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Invalid " + parameter + ".");
        }
    }

    // ============================================================
    // QUERY PARAMETER
    // ============================================================

    private String getQueryParameter(String query, String parameter) {

        if (query == null || query.isBlank()) {

            return null;
        }

        String[] parameters = query.split("&");

        for (String item : parameters) {

            String[] pair = item.split("=", 2);

            if (pair.length == 2 && pair[0].equals(parameter)) {

                return pair[1];
            }
        }

        return null;
    }

    // ============================================================
    // RESPONSE
    // ============================================================

    private void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, responseBytes.length);

        exchange.getResponseBody().write(responseBytes);

        exchange.getResponseBody().close();
    }

    // ============================================================
    // CORS
    // ============================================================

    private void addCorsHeaders(HttpExchange exchange) {

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    }

    // ============================================================
    // ERROR RESPONSE
    // ============================================================

    private static class ErrorResponse {

        private final String error;

        private ErrorResponse(String error) {

            this.error = error;
        }

        public String getError() {

            return error;
        }
    }
}