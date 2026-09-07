package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAO {

    public boolean save(Supplier supplier) throws SQLException {

        String sql = """
                INSERT INTO suppliers (
                    supplier_code,
                    supplier_name,
                    phone,
                    email,
                    address,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, supplier.getSupplierCode());
            statement.setString(2, supplier.getSupplierName());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getAddress());
            statement.setString(6, supplier.getStatus());

            return statement.executeUpdate() > 0;
        }
    }

    public List<Supplier> findAll() throws SQLException {

        List<Supplier> suppliers = new ArrayList<>();

        String sql = "SELECT * FROM suppliers ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                suppliers.add(mapResultSet(resultSet));
            }
        }

        return suppliers;
    }

    public Supplier findById(int id) throws SQLException {

        String sql = "SELECT * FROM suppliers WHERE id = ?";

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

    public boolean update(Supplier supplier) throws SQLException {

        String sql = """
                UPDATE suppliers SET
                    supplier_code = ?,
                    supplier_name = ?,
                    phone = ?,
                    email = ?,
                    address = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, supplier.getSupplierCode());
            statement.setString(2, supplier.getSupplierName());
            statement.setString(3, supplier.getPhone());
            statement.setString(4, supplier.getEmail());
            statement.setString(5, supplier.getAddress());
            statement.setString(6, supplier.getStatus());
            statement.setInt(7, supplier.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM suppliers WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Supplier mapResultSet(ResultSet resultSet) throws SQLException {

        Supplier supplier = new Supplier();

        supplier.setId(resultSet.getInt("id"));
        supplier.setSupplierCode(resultSet.getString("supplier_code"));
        supplier.setSupplierName(resultSet.getString("supplier_name"));
        supplier.setPhone(resultSet.getString("phone"));
        supplier.setEmail(resultSet.getString("email"));
        supplier.setAddress(resultSet.getString("address"));
        supplier.setStatus(resultSet.getString("status"));

        return supplier;
    }
}