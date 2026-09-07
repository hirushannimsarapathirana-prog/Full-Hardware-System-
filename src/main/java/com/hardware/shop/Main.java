package com.hardware.shop;

import com.hardware.shop.controller.*;
import com.sun.net.httpserver.HttpServer;
import com.hardware.shop.controller.SupplierController;
import com.hardware.shop.controller.PurchaseController;
import com.hardware.shop.controller.SaleController;
import com.hardware.shop.controller.CustomerPaymentController;
import com.hardware.shop.controller.CustomerBalanceController;
import com.hardware.shop.controller.SupplierPaymentController;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.hardware.shop.controller.AuthController;

public class Main {

    private static final int PORT = 8080;

    public static void main(String[] args) {

        try {

            HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);

            server.createContext("/api/health", exchange -> {

                String response = """
                        {
                            "status": "UP",
                            "message": "Hardware Shop API is running"
                        }
                        """;

                exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");

                byte[] responseBytes = response.getBytes();

                exchange.sendResponseHeaders(200, responseBytes.length);

                exchange.getResponseBody().write(responseBytes);
                exchange.getResponseBody().close();
            });

            server.createContext("/api/products", new ProductController());
            server.createContext("/api/categories", new CategoryController());
            server.createContext("/api/units", new UnitController());
            server.createContext("/api/brands", new BrandController());
            server.createContext("/api/customers", new CustomerController());
            server.createContext("/api/suppliers", new SupplierController());
            server.createContext("/api/auth", new AuthController());
            server.createContext("/api/purchases", new PurchaseController());
            server.createContext("/api/sales", new SaleController());
            server.createContext("/api/customer-payments", new CustomerPaymentController());
            server.createContext("/api/customer-balance", new CustomerBalanceController());
            server.createContext("/api/supplier-payments", new SupplierPaymentController());
            server.createContext("/api/supplier-balance", new SupplierBalanceController());
            server.createContext("/api/sales-returns", new SalesReturnController());
            server.createContext("/api/purchase-returns", new PurchaseReturnController());
            server.createContext("/api/expenses", new ExpenseController());

            server.start();

            System.out.println("=================================");
            System.out.println("HARDWARE SHOP API SERVER STARTED");
            System.out.println("Port: " + PORT);
            System.out.println("Health: http://localhost:" + PORT + "/api/health");
            System.out.println("Products: http://localhost:" + PORT + "/api/products");
            System.out.println("=================================");

        } catch (IOException e) {

            System.out.println("Failed to start API server.");
            e.printStackTrace();
        }
    }
}