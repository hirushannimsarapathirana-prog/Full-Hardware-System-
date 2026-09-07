package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.Customer;
import com.hardware.shop.service.CustomerService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CustomerController implements HttpHandler {

    private final CustomerService customerService;
    private final Gson gson;

    public CustomerController() {
        this.customerService = new CustomerService();
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
        String basePath = "/api/customers";

        if (path.equals(basePath) || path.equals(basePath + "/")) {

            List<Customer> customers = customerService.findAll();

            sendResponse(exchange, 200, gson.toJson(customers));

        } else {

            String idText = path.substring((basePath + "/").length());

            int id = Integer.parseInt(idText);

            Customer customer = customerService.findById(id);

            if (customer == null) {

                sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}");

                return;
            }

            sendResponse(exchange, 200, gson.toJson(customer));
        }
    }

    private void handlePost(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Customer customer = gson.fromJson(requestBody, Customer.class);

        boolean saved = customerService.save(customer);

        if (saved) {

            sendResponse(exchange, 201, "{\"message\":\"Customer created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create customer\"}");
        }
    }

    private void handlePut(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/customers/";

        String idText = path.substring(basePath.length());

        int id = Integer.parseInt(idText);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Customer customer = gson.fromJson(requestBody, Customer.class);

        customer.setId(id);

        boolean updated = customerService.update(customer);

        if (updated) {

            sendResponse(exchange, 200, "{\"message\":\"Customer updated successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}");
        }
    }

    private void handleDelete(HttpExchange exchange) throws Exception {

        String path = exchange.getRequestURI().getPath();
        String basePath = "/api/customers/";

        String idText = path.substring(basePath.length());

        int id = Integer.parseInt(idText);

        boolean deleted = customerService.delete(id);

        if (deleted) {

            sendResponse(exchange, 200, "{\"message\":\"Customer deleted successfully\"}");

        } else {

            sendResponse(exchange, 404, "{\"error\":\"Customer not found\"}");
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