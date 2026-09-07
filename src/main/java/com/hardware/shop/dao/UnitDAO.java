package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Unit;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UnitDAO {

    public boolean save(Unit unit) throws SQLException {
        String sql = "INSERT INTO units (unit_name, short_name) VALUES (?, ?)";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, unit.getUnitName());
            statement.setString(2, unit.getShortName());

            return statement.executeUpdate() > 0;
        }
    }

    public List<Unit> findAll() throws SQLException {
        List<Unit> units = new ArrayList<>();
        String sql = "SELECT * FROM units ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                Unit unit = new Unit();
                unit.setId(resultSet.getInt("id"));
                unit.setUnitName(resultSet.getString("unit_name"));
                unit.setShortName(resultSet.getString("short_name"));
                units.add(unit);
            }
        }

        return units;
    }

    public Unit findById(int id) throws SQLException {
        String sql = "SELECT * FROM units WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Unit unit = new Unit();
                    unit.setId(resultSet.getInt("id"));
                    unit.setUnitName(resultSet.getString("unit_name"));
                    unit.setShortName(resultSet.getString("short_name"));
                    return unit;
                }
            }
        }

        return null;
    }

    public boolean update(Unit unit) throws SQLException {
        String sql = "UPDATE units SET unit_name = ?, short_name = ? WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, unit.getUnitName());
            statement.setString(2, unit.getShortName());
            statement.setInt(3, unit.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        String sql = "DELETE FROM units WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }
}
