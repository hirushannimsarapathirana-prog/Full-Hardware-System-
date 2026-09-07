package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Brand;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BrandDAO {

    public boolean save(Brand brand) throws SQLException {

        String sql = "INSERT INTO brands (brand_name, description) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, brand.getBrandName());
            statement.setString(2, brand.getDescription());

            return statement.executeUpdate() > 0;
        }
    }

    public List<Brand> findAll() throws SQLException {

        List<Brand> brands = new ArrayList<>();

        String sql = "SELECT * FROM brands ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                Brand brand = new Brand();

                brand.setId(resultSet.getInt("id"));
                brand.setBrandName(resultSet.getString("brand_name"));
                brand.setDescription(resultSet.getString("description"));

                brands.add(brand);
            }
        }

        return brands;
    }

    public Brand findById(int id) throws SQLException {

        String sql = "SELECT * FROM brands WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    Brand brand = new Brand();

                    brand.setId(resultSet.getInt("id"));
                    brand.setBrandName(resultSet.getString("brand_name"));
                    brand.setDescription(resultSet.getString("description"));

                    return brand;
                }
            }
        }

        return null;
    }

    public boolean update(Brand brand) throws SQLException {

        String sql = "UPDATE brands SET brand_name = ?, description = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, brand.getBrandName());
            statement.setString(2, brand.getDescription());
            statement.setInt(3, brand.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM brands WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }
}