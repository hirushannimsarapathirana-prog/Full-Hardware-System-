package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    public boolean save(Product product) throws SQLException {

        String productSql = """
                INSERT INTO products (
                    sku,
                    barcode,
                    product_name,
                    category_id,
                    brand_id,
                    unit_id,
                    purchase_price,
                    selling_price,
                    wholesale_price,
                    minimum_stock,
                    description,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        String stockSql = """
                INSERT INTO product_stock (
                    product_id,
                    quantity
                )
                VALUES (?, 0)
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                int productId;

                try (PreparedStatement statement = connection.prepareStatement(productSql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, product.getSku());
                    statement.setString(2, product.getBarcode());
                    statement.setString(3, product.getProductName());
                    statement.setInt(4, product.getCategoryId());

                    if (product.getBrandId() == null) {
                        statement.setNull(5, Types.INTEGER);
                    } else {
                        statement.setInt(5, product.getBrandId());
                    }

                    statement.setInt(6, product.getUnitId());
                    statement.setDouble(7, product.getPurchasePrice());
                    statement.setDouble(8, product.getSellingPrice());
                    statement.setDouble(9, product.getWholesalePrice());
                    statement.setDouble(10, product.getMinimumStock());
                    statement.setString(11, product.getDescription());
                    statement.setString(12, product.getStatus());

                    int affectedRows = statement.executeUpdate();

                    if (affectedRows == 0) {
                        throw new SQLException("Creating product failed.");
                    }

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {
                            throw new SQLException("Creating product failed: no ID obtained.");
                        }

                        productId = generatedKeys.getInt(1);
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(stockSql)) {

                    statement.setInt(1, productId);
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

    public List<Product> findAll() throws SQLException {

        List<Product> products = new ArrayList<>();

        String sql = "SELECT * FROM products ORDER BY id DESC";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql); ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                products.add(mapResultSet(resultSet));
            }
        }

        return products;
    }

    public Product findById(int id) throws SQLException {

        String sql = "SELECT * FROM products WHERE id = ?";

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

    public boolean update(Product product) throws SQLException {

        String sql = """
                UPDATE products SET
                    sku = ?,
                    barcode = ?,
                    product_name = ?,
                    category_id = ?,
                    brand_id = ?,
                    unit_id = ?,
                    purchase_price = ?,
                    selling_price = ?,
                    wholesale_price = ?,
                    minimum_stock = ?,
                    description = ?,
                    status = ?
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, product.getSku());
            statement.setString(2, product.getBarcode());
            statement.setString(3, product.getProductName());
            statement.setInt(4, product.getCategoryId());

            if (product.getBrandId() == null) {
                statement.setNull(5, Types.INTEGER);
            } else {
                statement.setInt(5, product.getBrandId());
            }

            statement.setInt(6, product.getUnitId());
            statement.setDouble(7, product.getPurchasePrice());
            statement.setDouble(8, product.getSellingPrice());
            statement.setDouble(9, product.getWholesalePrice());
            statement.setDouble(10, product.getMinimumStock());
            statement.setString(11, product.getDescription());
            statement.setString(12, product.getStatus());
            statement.setInt(13, product.getId());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {

        String sql = "DELETE FROM products WHERE id = ?";

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            return statement.executeUpdate() > 0;
        }
    }

    private Product mapResultSet(ResultSet resultSet) throws SQLException {

        Product product = new Product();

        product.setId(resultSet.getInt("id"));
        product.setSku(resultSet.getString("sku"));
        product.setBarcode(resultSet.getString("barcode"));
        product.setProductName(resultSet.getString("product_name"));
        product.setCategoryId(resultSet.getInt("category_id"));

        int brandId = resultSet.getInt("brand_id");
        product.setBrandId(resultSet.wasNull() ? null : brandId);

        product.setUnitId(resultSet.getInt("unit_id"));
        product.setPurchasePrice(resultSet.getDouble("purchase_price"));
        product.setSellingPrice(resultSet.getDouble("selling_price"));
        product.setWholesalePrice(resultSet.getDouble("wholesale_price"));
        product.setMinimumStock(resultSet.getDouble("minimum_stock"));
        product.setDescription(resultSet.getString("description"));
        product.setStatus(resultSet.getString("status"));

        return product;
    }
}