package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.dto.CustomerBalanceResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerBalanceDAO {

    public CustomerBalanceResponse getCustomerBalance(int customerId) throws SQLException {

        String salesSql = """
                SELECT COALESCE(
                    SUM(due_amount),
                    0
                ) AS total_credit_sales
                FROM sales
                WHERE customer_id = ?
                AND sale_status = 'COMPLETED'
                """;

        String paymentsSql = """
                SELECT COALESCE(
                    SUM(amount),
                    0
                ) AS total_payments
                FROM customer_payments
                WHERE customer_id = ?
                """;

        String returnsSql = """
                SELECT COALESCE(
                    SUM(total_amount),
                    0
                ) AS total_credit_returns
                FROM sales_returns
                WHERE customer_id = ?
                AND refund_method = 'CREDIT'
                """;

        double totalCreditSales = 0;
        double totalPayments = 0;
        double totalCreditReturns = 0;

        try (Connection connection = DBConnection.getConnection()) {

            // 1. Total credit sales
            try (PreparedStatement statement = connection.prepareStatement(salesSql)) {

                statement.setInt(1, customerId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        totalCreditSales = resultSet.getDouble("total_credit_sales");
                    }
                }
            }

            // 2. Total customer payments
            try (PreparedStatement statement = connection.prepareStatement(paymentsSql)) {

                statement.setInt(1, customerId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        totalPayments = resultSet.getDouble("total_payments");
                    }
                }
            }

            // 3. Total credit sales returns
            try (PreparedStatement statement = connection.prepareStatement(returnsSql)) {

                statement.setInt(1, customerId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {
                        totalCreditReturns = resultSet.getDouble("total_credit_returns");
                    }
                }
            }
        }

        // Outstanding =
        // Credit Sales - Customer Payments - Credit Returns
        double outstandingBalance = totalCreditSales - totalPayments - totalCreditReturns;

        // Prevent negative outstanding balance
        if (outstandingBalance < 0) {
            outstandingBalance = 0;
        }

        return new CustomerBalanceResponse(customerId, totalCreditSales, totalPayments, outstandingBalance);
    }
}