package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.CustomerPayment;
import com.hardware.shop.service.CustomerPaymentService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CustomerPaymentController implements HttpHandler {

    private final CustomerPaymentService customerPaymentService;
    private final Gson gson;

    public CustomerPaymentController() {

        this.customerPaymentService = new CustomerPaymentService();

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
            // POST /api/customer-payments
            // -------------------------------------------------

            if ("POST".equalsIgnoreCase(method) && path.equals("/api/customer-payments")) {

                handleCreatePayment(exchange);
            }


            // -------------------------------------------------
            // GET /api/customer-payments
            // -------------------------------------------------

            else if ("GET".equalsIgnoreCase(method) && path.equals("/api/customer-payments")) {

                handleGetAllPayments(exchange);
            }


            // -------------------------------------------------
            // GET /api/customer-payments/{customerId}
            // -------------------------------------------------

            else if ("GET".equalsIgnoreCase(method)) {

                handleGetCustomerPayments(exchange, path);
            }


            // -------------------------------------------------
            // METHOD NOT ALLOWED
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
    // CREATE PAYMENT
    // =========================================================

    private void handleCreatePayment(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        CustomerPayment payment = gson.fromJson(requestBody, CustomerPayment.class);

        if (payment == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        boolean created = customerPaymentService.createPayment(payment);

        if (created) {

            sendResponse(exchange, 201, "{\"message\":\"Customer payment created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create customer payment\"}");
        }
    }


    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    private void handleGetAllPayments(HttpExchange exchange) throws IOException {

        try {

            List<CustomerPayment> payments = customerPaymentService.findAll();

            String response = gson.toJson(payments);

            sendResponse(exchange, 200, response);

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Failed to retrieve customer payments\"}");
        }
    }


    // =========================================================
    // GET PAYMENTS BY CUSTOMER
    // =========================================================

    private void handleGetCustomerPayments(HttpExchange exchange, String path) throws Exception {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            sendResponse(exchange, 400, "{\"error\":\"Invalid customer payment URL\"}");

            return;
        }

        int customerId;

        try {

            customerId = Integer.parseInt(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Customer ID must be a valid number.");
        }

        List<CustomerPayment> payments = customerPaymentService.findByCustomerId(customerId);

        String response = gson.toJson(payments);

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
}
