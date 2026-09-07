package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.User;

import java.sql.*;

public class UserDAO {

    public boolean save(User user) throws SQLException {

        String sql = """
                INSERT INTO users (
                    username,
                    password_hash,
                    full_name,
                    email,
                    phone,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, user.getUsername());
            statement.setString(2, user.getPasswordHash());
            statement.setString(3, user.getFullName());
            statement.setString(4, user.getEmail());
            statement.setString(5, user.getPhone());
            statement.setString(6, user.getStatus());

            return statement.executeUpdate() > 0;
        }
    }

    public User findByUsername(String username) throws SQLException {

        String sql = """
                SELECT
                    id,
                    username,
                    password_hash,
                    full_name,
                    email,
                    phone,
                    status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {
                    return mapResultSet(resultSet);
                }
            }
        }

        return null;
    }

    public User findById(int id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    username,
                    password_hash,
                    full_name,
                    email,
                    phone,
                    status
                FROM users
                WHERE id = ?
                """;

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

    private User mapResultSet(ResultSet resultSet) throws SQLException {

        User user = new User();

        user.setId(resultSet.getInt("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFullName(resultSet.getString("full_name"));
        user.setEmail(resultSet.getString("email"));
        user.setPhone(resultSet.getString("phone"));
        user.setStatus(resultSet.getString("status"));

        return user;
    }
}