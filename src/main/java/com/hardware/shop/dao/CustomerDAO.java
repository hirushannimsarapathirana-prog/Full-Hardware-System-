package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Customer;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerDAO {

    public boolean save(Customer customer) throws SQLException {

        String sql = """
                INSERT INTO customers (
                    customer_code,
                    customer_name,
                    phone,
                    email,
                    address,
                    credit_limit,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerCode());
            statement.setString(2, customer.getCustomerName());
            statement.setString(3, customer.getPhone());
            statement.setString(4, customer.getEmail());
            statement.setString(5, customer.getAddress());
            statement.setDouble(6, customer.getCreditLimit());
            statement.setString(7, customer.getStatus());

            return statement.executeUpdate() > 0;
        }
    }

    public List<Customer> findAll() throws SQLException {

        List<Customer> customers = new ArrayList<>();

        String sql = "SELECT * FROM customers ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                customers.add(mapResultSet(resultSet));
            }
        }

        return customers;
    }

    public Customer findById(int id) throws SQLException {

        String sql = "SELECT * FROM customers WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        }

        return null;
    }

    public boolean update(Customer customer) throws SQLException {

        String sql = """
                UPDATE customers SET
                    customer_code = ?,
                    customer_name = ?,
                    phone = ?,
                    email = ?,
                    address = ?,
                    credit_limit = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getCustomerCode());
            statement.setString(2, customer.getCustomerName());
            statement.setString(3, customer.getPhone());
            statement.setString(4, customer.getEmail());
            statement.setString(5, customer.getAddress());
            statement.setDouble(6, customer.getCreditLimit());
            statement.setString(7, customer.getStatus());
            statement.setInt(8, customer.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM customers WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Customer mapResultSet(ResultSet resultSet) throws SQLException {

        Customer customer = new Customer();

        customer.setId(resultSet.getInt("id"));
        customer.setCustomerCode(resultSet.getString("customer_code"));
        customer.setCustomerName(resultSet.getString("customer_name"));
        customer.setPhone(resultSet.getString("phone"));
        customer.setEmail(resultSet.getString("email"));
        customer.setAddress(resultSet.getString("address"));
        customer.setCreditLimit(resultSet.getDouble("credit_limit"));
        customer.setStatus(resultSet.getString("status"));

        return customer;
    }
}