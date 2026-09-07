package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.dto.DashboardResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DashboardDAO {

    public DashboardResponse getDashboard() throws SQLException {

        DashboardResponse response = new DashboardResponse();

        try (Connection connection = DBConnection.getConnection()) {

            response.setTodaySales(getDouble(connection, """
                    SELECT COALESCE(SUM(total_amount), 0)
                    FROM sales
                    WHERE DATE(sale_date) = CURDATE()
                    AND sale_status = 'COMPLETED'
                    """));

            response.setTodayPurchases(getDouble(connection, """
                    SELECT COALESCE(SUM(total_amount), 0)
                    FROM purchases
                    WHERE DATE(purchase_date) = CURDATE()
                    """));

            response.setTodayExpenses(getDouble(connection, """
                    SELECT COALESCE(SUM(amount), 0)
                    FROM expenses
                    WHERE DATE(expense_date) = CURDATE()
                    """));

            response.setTotalProducts(getLong(connection, """
                    SELECT COUNT(*)
                    FROM products
                    WHERE status = 'ACTIVE'
                    """));

            response.setLowStockProducts(getLong(connection, """
                    SELECT COUNT(*)
                    FROM products p
                    INNER JOIN product_stock ps
                        ON ps.product_id = p.id
                    WHERE p.status = 'ACTIVE'
                    AND ps.quantity <= p.minimum_stock
                    """));

            response.setCustomerOutstanding(getDouble(connection, """
                    SELECT GREATEST(
                        COALESCE((
                            SELECT SUM(s.due_amount)
                            FROM sales s
                            WHERE s.customer_id IS NOT NULL
                            AND s.payment_status IN ('CREDIT', 'PARTIAL')
                            AND s.sale_status = 'COMPLETED'
                        ), 0)
                        -
                        COALESCE((
                            SELECT SUM(cp.amount)
                            FROM customer_payments cp
                        ), 0)
                        -
                        COALESCE((
                            SELECT SUM(sr.total_amount)
                            FROM sales_returns sr
                            WHERE sr.refund_method = 'CREDIT'
                        ), 0),
                        0
                    )
                    """));

            response.setSupplierOutstanding(getDouble(connection, """
                    SELECT GREATEST(
                        COALESCE((
                            SELECT SUM(p.due_amount)
                            FROM purchases p
                            WHERE p.payment_status IN ('CREDIT', 'PARTIAL')
                        ), 0)
                        -
                        COALESCE((
                            SELECT SUM(sp.amount)
                            FROM supplier_payments sp
                        ), 0)
                        -
                        COALESCE((
                            SELECT SUM(pr.total_amount)
                            FROM purchase_returns pr
                            WHERE pr.refund_method = 'CREDIT'
                        ), 0),
                        0
                    )
                    """));

            response.setMonthlySales(getDouble(connection, """
                    SELECT COALESCE(SUM(total_amount), 0)
                    FROM sales
                    WHERE sale_status = 'COMPLETED'
                    AND YEAR(sale_date) = YEAR(CURDATE())
                    AND MONTH(sale_date) = MONTH(CURDATE())
                    """));

            response.setMonthlyProfit(calculateMonthlyProfit(connection));
        }

        return response;
    }

    private double calculateMonthlyProfit(Connection connection) throws SQLException {

        String sql = """
                SELECT COALESCE(
                    (
                        SELECT SUM(si.total_price)
                        FROM sale_items si
                        INNER JOIN sales s
                            ON s.id = si.sale_id
                        WHERE s.sale_status = 'COMPLETED'
                        AND YEAR(s.sale_date) = YEAR(CURDATE())
                        AND MONTH(s.sale_date) = MONTH(CURDATE())
                    ),
                    0
                )
                -
                COALESCE(
                    (
                        SELECT SUM(si.quantity * p.purchase_price)
                        FROM sale_items si
                        INNER JOIN sales s
                            ON s.id = si.sale_id
                        INNER JOIN products p
                            ON p.id = si.product_id
                        WHERE s.sale_status = 'COMPLETED'
                        AND YEAR(s.sale_date) = YEAR(CURDATE())
                        AND MONTH(s.sale_date) = MONTH(CURDATE())
                    ),
                    0
                )
                -
                COALESCE(
                    (
                        SELECT SUM(e.amount)
                        FROM expenses e
                        WHERE YEAR(e.expense_date) = YEAR(CURDATE())
                        AND MONTH(e.expense_date) = MONTH(CURDATE())
                    ),
                    0
                )
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        }

        return 0;
    }

    private double getDouble(Connection connection, String sql) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getDouble(1);
            }
        }

        return 0;
    }

    private long getLong(Connection connection, String sql) throws SQLException {

        try (PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
        }

        return 0;
    }
}