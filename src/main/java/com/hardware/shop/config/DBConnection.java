package com.hardware.shop.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL = getEnv("DB_URL", "jdbc:mysql://127.0.0.1:3306/hardware_shop" + "?useSSL=false&serverTimezone=Asia/Colombo");

    private static final String USER = getEnv("DB_USER", "root");

    private static final String PASSWORD = getEnv("DB_PASSWORD", "admin");

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {

        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String getEnv(String name, String defaultValue) {

        String value = System.getenv(name);

        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        return value;
    }

}