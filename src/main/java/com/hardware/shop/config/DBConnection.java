package com.hardware.shop.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnection {

    private static final String URL = "jdbc:mysql://127.0.0.1:3306/hardware_shop?useSSL=false&serverTimezone=Asia/Colombo";

    private static final String USER = "root";
    private static final String PASSWORD = "admin";

    private DBConnection() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}