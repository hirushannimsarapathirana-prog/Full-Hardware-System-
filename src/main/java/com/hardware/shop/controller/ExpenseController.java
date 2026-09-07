package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.model.Expense;
import com.hardware.shop.service.ExpenseService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExpenseController implements HttpHandler {

    private final ExpenseService expenseService;
    private final Gson gson;

    public ExpenseController() {
        this.expenseService = new ExpenseService();
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

            if ("POST".equalsIgnoreCase(method) && path.equals("/api/expenses")) {

                handleCreateExpense(exchange);

            } else if ("GET".equalsIgnoreCase(method) && path.equals("/api/expenses")) {

                handleGetAllExpenses(exchange);

            } else if ("GET".equalsIgnoreCase(method)) {

                handleGetExpenseById(exchange, path);

            } else if ("PUT".equalsIgnoreCase(method)) {

                handleUpdateExpense(exchange, path);

            } else if ("DELETE".equalsIgnoreCase(method)) {

                handleDeleteExpense(exchange, path);

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


    private void handleCreateExpense(HttpExchange exchange) throws Exception {

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Expense expense = gson.fromJson(requestBody, Expense.class);

        if (expense == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        boolean created = expenseService.createExpense(expense);

        if (created) {

            sendResponse(exchange, 201, "{\"message\":\"Expense created successfully\"}");

        } else {

            sendResponse(exchange, 500, "{\"error\":\"Failed to create expense\"}");
        }
    }


    private void handleGetAllExpenses(HttpExchange exchange) throws Exception {

        List<Expense> expenses = expenseService.findAll();

        sendResponse(exchange, 200, gson.toJson(expenses));
    }


    private void handleGetExpenseById(HttpExchange exchange, String path) throws Exception {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            sendResponse(exchange, 400, "{\"error\":\"Invalid expense URL\"}");

            return;
        }

        long expenseId;

        try {

            expenseId = Long.parseLong(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Expense ID must be a valid number.");
        }

        Expense expense = expenseService.findById(expenseId);

        if (expense == null) {

            sendResponse(exchange, 404, "{\"error\":\"Expense not found\"}");

            return;
        }

        sendResponse(exchange, 200, gson.toJson(expense));
    }


    private void handleUpdateExpense(HttpExchange exchange, String path) throws Exception {

        long expenseId = extractId(path);

        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        Expense expense = gson.fromJson(requestBody, Expense.class);

        if (expense == null) {

            throw new IllegalArgumentException("Invalid request body.");
        }

        expense.setId(expenseId);

        boolean updated = expenseService.updateExpense(expense);

        if (!updated) {

            sendResponse(exchange, 404, "{\"error\":\"Expense not found\"}");

            return;
        }

        sendResponse(exchange, 200, "{\"message\":\"Expense updated successfully\"}");
    }


    private void handleDeleteExpense(HttpExchange exchange, String path) throws Exception {

        long expenseId = extractId(path);

        boolean deleted = expenseService.deleteExpense(expenseId);

        if (!deleted) {

            sendResponse(exchange, 404, "{\"error\":\"Expense not found\"}");

            return;
        }

        sendResponse(exchange, 200, "{\"message\":\"Expense deleted successfully\"}");
    }


    private long extractId(String path) {

        String[] pathParts = path.split("/");

        if (pathParts.length != 4) {

            throw new IllegalArgumentException("Invalid expense URL.");
        }

        try {

            return Long.parseLong(pathParts[3]);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Expense ID must be a valid number.");
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