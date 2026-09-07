package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.CustomerPayment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerPaymentDAO {

    public boolean createPayment(CustomerPayment payment) throws SQLException {

        String paymentSql = """
                INSERT INTO customer_payments (
                    customer_id,
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
                    'CUSTOMER_PAYMENT',
                    ?,
                    ?,
                    ?
                )
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                long paymentId;

                // Create customer payment
                try (PreparedStatement statement = connection.prepareStatement(paymentSql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setInt(1, payment.getCustomerId());

                    statement.setDouble(2, payment.getAmount());

                    statement.setString(3, payment.getPaymentMethod());

                    statement.setString(4, payment.getReferenceNumber());

                    statement.setString(5, payment.getNotes());

                    int affectedRows = statement.executeUpdate();

                    if (affectedRows == 0) {

                        throw new SQLException("Creating customer payment failed.");
                    }

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {

                            throw new SQLException("Creating customer payment failed: no ID obtained.");
                        }

                        paymentId = generatedKeys.getLong(1);
                    }
                }

                // Create cash transaction
                try (PreparedStatement statement = connection.prepareStatement(cashTransactionSql)) {

                    statement.setLong(1, paymentId);

                    statement.setDouble(2, payment.getAmount());

                    statement.setString(3, "Customer payment received - Payment ID " + paymentId);

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

    public List<CustomerPayment> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    customer_id,
                    amount,
                    payment_method,
                    payment_date,
                    reference_number,
                    notes
                FROM customer_payments
                ORDER BY id DESC
                """;

        List<CustomerPayment> payments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                payments.add(mapPayment(resultSet));
            }
        }

        return payments;
    }

    public List<CustomerPayment> findByCustomerId(int customerId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    customer_id,
                    amount,
                    payment_method,
                    payment_date,
                    reference_number,
                    notes
                FROM customer_payments
                WHERE customer_id = ?
                ORDER BY id DESC
                """;

        List<CustomerPayment> payments = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    payments.add(mapPayment(resultSet));
                }
            }
        }

        return payments;
    }

    private CustomerPayment mapPayment(ResultSet resultSet) throws SQLException {

        CustomerPayment payment = new CustomerPayment();

        payment.setId(resultSet.getLong("id"));

        payment.setCustomerId(resultSet.getInt("customer_id"));

        payment.setAmount(resultSet.getDouble("amount"));

        payment.setPaymentMethod(resultSet.getString("payment_method"));

        payment.setPaymentDate(resultSet.getString("payment_date"));

        payment.setReferenceNumber(resultSet.getString("reference_number"));

        payment.setNotes(resultSet.getString("notes"));

        return payment;
    }
}