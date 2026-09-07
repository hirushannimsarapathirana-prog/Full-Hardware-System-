package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.dto.SupplierBalanceResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SupplierBalanceDAO {

    public SupplierBalanceResponse getSupplierBalance(int supplierId) throws SQLException {

        String purchasesSql = """
                SELECT COALESCE(
                    SUM(due_amount),
                    0
                ) AS total_credit_purchases
                FROM purchases
                WHERE supplier_id = ?
                """;

        String paymentsSql = """
                SELECT COALESCE(
                    SUM(amount),
                    0
                ) AS total_payments
                FROM supplier_payments
                WHERE supplier_id = ?
                """;

        String returnsSql = """
                SELECT COALESCE(
                    SUM(total_amount),
                    0
                ) AS total_credit_returns
                FROM purchase_returns
                WHERE supplier_id = ?
                AND refund_method = 'CREDIT'
                """;

        double totalCreditPurchases = 0;
        double totalPayments = 0;
        double totalCreditReturns = 0;

        try (Connection connection = DBConnection.getConnection()) {

            // 1. Total credit purchases
            try (PreparedStatement statement = connection.prepareStatement(purchasesSql)) {

                statement.setInt(1, supplierId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {

                        totalCreditPurchases = resultSet.getDouble("total_credit_purchases");
                    }
                }
            }

            // 2. Total supplier payments
            try (PreparedStatement statement = connection.prepareStatement(paymentsSql)) {

                statement.setInt(1, supplierId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {

                        totalPayments = resultSet.getDouble("total_payments");
                    }
                }
            }

            // 3. Total CREDIT purchase returns
            try (PreparedStatement statement = connection.prepareStatement(returnsSql)) {

                statement.setInt(1, supplierId);

                try (ResultSet resultSet = statement.executeQuery()) {

                    if (resultSet.next()) {

                        totalCreditReturns = resultSet.getDouble("total_credit_returns");
                    }
                }
            }
        }

        /*
         * Supplier outstanding calculation:
         *
         * Credit Purchases
         * - Supplier Payments
         * - Credit Purchase Returns
         */
        double outstandingBalance = totalCreditPurchases - totalPayments - totalCreditReturns;

        if (outstandingBalance < 0) {

            outstandingBalance = 0;
        }

        return new SupplierBalanceResponse(supplierId, totalCreditPurchases, totalPayments, outstandingBalance);
    }
}