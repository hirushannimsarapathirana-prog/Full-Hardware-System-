package com.hardware.shop.controller;

import com.google.gson.Gson;
import com.hardware.shop.dto.DashboardResponse;
import com.hardware.shop.service.DashboardService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class DashboardController implements HttpHandler {

    private final DashboardService dashboardService;
    private final Gson gson;

    public DashboardController() {
        this.dashboardService = new DashboardService();
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

            DashboardResponse response = dashboardService.getDashboard();

            sendResponse(exchange, 200, gson.toJson(response));

        } catch (Exception e) {

            e.printStackTrace();

            sendResponse(exchange, 500, "{\"error\":\"Internal server error\"}");
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
}