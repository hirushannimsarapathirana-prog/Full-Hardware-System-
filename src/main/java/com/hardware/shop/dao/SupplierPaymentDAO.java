package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.SupplierPayment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SupplierPaymentDAO {

    public boolean createPayment(SupplierPayment payment) throws SQLException {

        String paymentSql = """
                INSERT INTO supplier_payments (
                    supplier_id,
                    amount,
                    payment_method,
                    reference_number,
                    notes
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        String cashTransactionSql = """
                INSERT INTO cash_transactions (
                    transaction_type,
                    reference_id,
                    amount,
                    description
                )
                VALUES (
                    'PURCHASE_PAYMENT',
                    ?,
                    ?,
                    ?
                )
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                long paymentId;

                // Create supplier payment
                try (PreparedStatement statement = connection.prepareStatement(paymentSql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, payment.getSupplierId());

                    statement.setDouble(2, payment.getAmount());

                    statement.setString(3, payment.getPaymentMethod());

                    statement.setString(4, payment.getReferenceNumber());

                    statement.setString(5, payment.getNotes());

                    int affectedRows = statement.executeUpdate();

                    if (affectedRows == 0) {

                        throw new SQLException("Creating supplier payment failed.");
                    }

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {

                            throw new SQLException("Creating supplier payment failed: no ID obtained.");
                        }

                        paymentId = generatedKeys.getLong(1);
                    }
                }

                // Create cash transaction
                try (PreparedStatement statement = connection.prepareStatement(cashTransactionSql)) {

                    statement.setLong(1, paymentId);

                    statement.setDouble(2, payment.getAmount());

                    statement.setString(3, "Supplier payment made - Payment ID " + paymentId);

                    statement.executeUpdate();
                }

                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }

    public List<SupplierPayment> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    supplier_id,
                    amount,
                    payment_method,
                    payment_date,
                    reference_number,
                    notes
                FROM supplier_payments
                ORDER BY id DESC
                """;

        List<SupplierPayment> payments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                payments.add(mapPayment(resultSet));
            }
        }

        return payments;
    }

    public List<SupplierPayment> findBySupplierId(int supplierId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    supplier_id,
                    amount,
                    payment_method,
                    payment_date,
                    reference_number,
                    notes
                FROM supplier_payments
                WHERE supplier_id = ?
                ORDER BY id DESC
                """;

        List<SupplierPayment> payments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, supplierId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    payments.add(mapPayment(resultSet));
                }
            }
        }

        return payments;
    }

    private SupplierPayment mapPayment(ResultSet resultSet) throws SQLException {

        SupplierPayment payment = new SupplierPayment();

        payment.setId(resultSet.getLong("id"));

        payment.setSupplierId(resultSet.getInt("supplier_id"));

        payment.setAmount(resultSet.getDouble("amount"));

        payment.setPaymentMethod(resultSet.getString("payment_method"));

        payment.setPaymentDate(resultSet.getString("payment_date"));

        payment.setReferenceNumber(resultSet.getString("reference_number"));

        payment.setNotes(resultSet.getString("notes"));

        return payment;
    }
}