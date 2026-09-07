package com.hardware.shop;

import com.hardware.shop.controller.*;
import com.hardware.shop.util.AuthenticatedHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        /*
         * PUBLIC ENDPOINTS
         */

        server.createContext("/api/health", Main::health);

        server.createContext("/api/auth", new AuthController());

        /*
         * PROTECTED ENDPOINTS
         */

        server.createContext("/api/products", new AuthenticatedHandler(new ProductController()));

        server.createContext("/api/categories", new AuthenticatedHandler(new CategoryController()));

        server.createContext("/api/units", new AuthenticatedHandler(new UnitController()));

        server.createContext("/api/brands", new AuthenticatedHandler(new BrandController()));

        server.createContext("/api/customers", new AuthenticatedHandler(new CustomerController()));

        server.createContext("/api/suppliers", new AuthenticatedHandler(new SupplierController()));

        server.createContext("/api/purchases", new AuthenticatedHandler(new PurchaseController()));

        server.createContext("/api/sales", new AuthenticatedHandler(new SaleController()));

        server.createContext("/api/customer-payments", new AuthenticatedHandler(new CustomerPaymentController()));

        server.createContext("/api/customer-balance", new AuthenticatedHandler(new CustomerBalanceController()));

        server.createContext("/api/supplier-payments", new AuthenticatedHandler(new SupplierPaymentController()));

        server.createContext("/api/supplier-balance", new AuthenticatedHandler(new SupplierBalanceController()));

        server.createContext("/api/sales-returns", new AuthenticatedHandler(new SalesReturnController()));

        server.createContext("/api/purchase-returns", new AuthenticatedHandler(new PurchaseReturnController()));

        server.createContext("/api/expenses", new AuthenticatedHandler(new ExpenseController()));

        server.createContext("/api/dashboard", new AuthenticatedHandler(new DashboardController()));

        server.createContext("/api/reports", new AuthenticatedHandler(new ReportController()));

        server.createContext("/api/users", new AuthenticatedHandler(new UserController()));

        server.setExecutor(null);

        server.start();

        System.out.println("Hardware Shop Backend started.");

        System.out.println("Server: http://localhost:8080");

        System.out.println("Health: http://localhost:8080/api/health");

        System.out.println("Authentication: /api/auth/login");

        System.out.println("Protected APIs require: " + "Authorization: Bearer <token>");
    }

    private static void health(com.sun.net.httpserver.HttpExchange exchange) throws IOException {

        addCorsHeaders(exchange);

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {

            exchange.sendResponseHeaders(204, -1);

            exchange.close();
            return;
        }

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {

            sendHealthResponse(exchange, 405, "{\"error\":\"Method not allowed\"}");

            return;
        }

        sendHealthResponse(exchange, 200, "{\"status\":\"OK\",\"service\":\"Hardware Shop Backend\"}");
    }

    private static void sendHealthResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode, String response) throws IOException {

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

        exchange.sendResponseHeaders(statusCode, bytes.length);

        try (OutputStream output = exchange.getResponseBody()) {

            output.write(bytes);
        }
    }

    private static void addCorsHeaders(com.sun.net.httpserver.HttpExchange exchange) {

        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");

        exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");

        exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
    }
}