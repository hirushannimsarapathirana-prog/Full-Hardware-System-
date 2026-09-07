package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Sale;
import com.hardware.shop.model.SaleItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SaleDAO {

    // =========================================================
    // CREATE SALE
    // =========================================================

    public boolean createSale(Sale sale, List<SaleItem> items, String paymentMethod) throws SQLException {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Sale must contain at least one item.");
        }

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                long saleId = insertSale(connection, sale);

                insertSaleItemsAndUpdateStock(connection, saleId, items);

                if (sale.getPaidAmount() > 0) {

                    insertSalePayment(connection, saleId, sale.getPaidAmount(), paymentMethod);

                    insertCashTransaction(connection, saleId, sale.getPaidAmount());
                }

                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }


    // =========================================================
    // INSERT SALE
    // =========================================================

    private long insertSale(Connection connection, Sale sale) throws SQLException {

        String sql = """
                INSERT INTO sales (
                    invoice_number,
                    customer_id,
                    subtotal,
                    discount,
                    total_amount,
                    paid_amount,
                    due_amount,
                    payment_status,
                    sale_status,
                    notes
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, sale.getInvoiceNumber());

            if (sale.getCustomerId() == null) {

                statement.setNull(2, java.sql.Types.INTEGER);

            } else {

                statement.setInt(2, sale.getCustomerId());
            }

            statement.setDouble(3, sale.getSubtotal());

            statement.setDouble(4, sale.getDiscount());

            statement.setDouble(5, sale.getTotalAmount());

            statement.setDouble(6, sale.getPaidAmount());

            statement.setDouble(7, sale.getDueAmount());

            statement.setString(8, sale.getPaymentStatus());

            statement.setString(9, sale.getSaleStatus());

            statement.setString(10, sale.getNotes());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {

                throw new SQLException("Creating sale failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {

                    throw new SQLException("Creating sale failed: no ID obtained.");
                }

                return generatedKeys.getLong(1);
            }
        }
    }


    // =========================================================
    // INSERT SALE ITEMS + UPDATE STOCK
    // =========================================================

    private void insertSaleItemsAndUpdateStock(Connection connection, long saleId, List<SaleItem> items) throws SQLException {

        String itemSql = """
                INSERT INTO sale_items (
                    sale_id,
                    product_id,
                    quantity,
                    unit_price,
                    discount,
                    total_price
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String stockSelectSql = """
                SELECT quantity
                FROM product_stock
                WHERE product_id = ?
                FOR UPDATE
                """;

        String stockUpdateSql = """
                UPDATE product_stock
                SET quantity = quantity - ?
                WHERE product_id = ?
                """;

        String movementSql = """
                INSERT INTO stock_movements (
                    product_id,
                    movement_type,
                    quantity,
                    reference_id,
                    notes
                )
                VALUES (?, 'SALE', ?, ?, ?)
                """;

        try (PreparedStatement itemStatement = connection.prepareStatement(itemSql);

             PreparedStatement stockSelectStatement = connection.prepareStatement(stockSelectSql);

             PreparedStatement stockUpdateStatement = connection.prepareStatement(stockUpdateSql);

             PreparedStatement movementStatement = connection.prepareStatement(movementSql)) {

            for (SaleItem item : items) {

                // ---------------------------------------------
                // Check current stock
                // ---------------------------------------------

                stockSelectStatement.setInt(1, item.getProductId());

                double currentStock;

                try (ResultSet resultSet = stockSelectStatement.executeQuery()) {

                    if (!resultSet.next()) {

                        throw new SQLException("Stock record not found for product ID: " + item.getProductId());
                    }

                    currentStock = resultSet.getDouble("quantity");
                }

                // ---------------------------------------------
                // Check sufficient stock
                // ---------------------------------------------

                if (currentStock < item.getQuantity()) {

                    throw new IllegalArgumentException("Insufficient stock for product ID: " + item.getProductId() + ". Available: " + currentStock + ", Requested: " + item.getQuantity());
                }

                // ---------------------------------------------
                // Insert sale item
                // ---------------------------------------------

                itemStatement.setLong(1, saleId);

                itemStatement.setInt(2, item.getProductId());

                itemStatement.setDouble(3, item.getQuantity());

                itemStatement.setDouble(4, item.getUnitPrice());

                itemStatement.setDouble(5, item.getDiscount());

                itemStatement.setDouble(6, item.getTotalPrice());

                itemStatement.executeUpdate();

                // ---------------------------------------------
                // Decrease stock
                // ---------------------------------------------

                stockUpdateStatement.setDouble(1, item.getQuantity());

                stockUpdateStatement.setInt(2, item.getProductId());

                int stockUpdated = stockUpdateStatement.executeUpdate();

                if (stockUpdated == 0) {

                    throw new SQLException("Failed to update stock for product ID: " + item.getProductId());
                }

                // ---------------------------------------------
                // Stock movement
                // ---------------------------------------------

                movementStatement.setInt(1, item.getProductId());

                movementStatement.setDouble(2, item.getQuantity());

                movementStatement.setLong(3, saleId);

                movementStatement.setString(4, "Stock issued for sale");

                movementStatement.executeUpdate();
            }
        }
    }


    // =========================================================
    // INSERT SALE PAYMENT
    // =========================================================

    private void insertSalePayment(Connection connection, long saleId, double amount, String paymentMethod) throws SQLException {

        String sql = """
                INSERT INTO sale_payments (
                    sale_id,
                    amount,
                    payment_method
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, saleId);

            statement.setDouble(2, amount);

            statement.setString(3, paymentMethod);

            statement.executeUpdate();
        }
    }


    // =========================================================
    // INSERT CASH TRANSACTION
    // =========================================================

    private void insertCashTransaction(Connection connection, long saleId, double amount) throws SQLException {

        String sql = """
                INSERT INTO cash_transactions (
                    transaction_type,
                    reference_id,
                    amount,
                    description
                )
                VALUES ('SALE', ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, saleId);

            statement.setDouble(2, amount);

            statement.setString(3, "Payment received for sale " + saleId);

            statement.executeUpdate();
        }
    }


    // =========================================================
    // GET ALL SALES
    // =========================================================

    public List<Sale> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    invoice_number,
                    customer_id,
                    sale_date,
                    subtotal,
                    discount,
                    total_amount,
                    paid_amount,
                    due_amount,
                    payment_status,
                    sale_status,
                    notes
                FROM sales
                ORDER BY id DESC
                """;

        List<Sale> sales = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                sales.add(mapSale(resultSet));
            }
        }

        return sales;
    }


    // =========================================================
    // GET SALE BY ID
    // =========================================================

    public Sale findById(long id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    invoice_number,
                    customer_id,
                    sale_date,
                    subtotal,
                    discount,
                    total_amount,
                    paid_amount,
                    due_amount,
                    payment_status,
                    sale_status,
                    notes
                FROM sales
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapSale(resultSet);
                }
            }
        }

        return null;
    }


    // =========================================================
    // GET SALE ITEMS BY SALE ID
    // =========================================================

    public List<SaleItem> findItemsBySaleId(long saleId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    sale_id,
                    product_id,
                    quantity,
                    unit_price,
                    discount,
                    total_price
                FROM sale_items
                WHERE sale_id = ?
                ORDER BY id
                """;

        List<SaleItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, saleId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    SaleItem item = new SaleItem();

                    item.setId(resultSet.getLong("id"));

                    item.setSaleId(resultSet.getLong("sale_id"));

                    item.setProductId(resultSet.getInt("product_id"));

                    item.setQuantity(resultSet.getDouble("quantity"));

                    item.setUnitPrice(resultSet.getDouble("unit_price"));

                    item.setDiscount(resultSet.getDouble("discount"));

                    item.setTotalPrice(resultSet.getDouble("total_price"));

                    items.add(item);
                }
            }
        }

        return items;
    }


    // =========================================================
    // MAP RESULT SET → SALE
    // =========================================================

    private Sale mapSale(ResultSet resultSet) throws SQLException {

        Sale sale = new Sale();

        sale.setId(resultSet.getLong("id"));

        sale.setInvoiceNumber(resultSet.getString("invoice_number"));

        int customerId = resultSet.getInt("customer_id");

        if (resultSet.wasNull()) {

            sale.setCustomerId(null);

        } else {

            sale.setCustomerId(customerId);
        }

        sale.setSaleDate(resultSet.getString("sale_date"));

        sale.setSubtotal(resultSet.getDouble("subtotal"));

        sale.setDiscount(resultSet.getDouble("discount"));

        sale.setTotalAmount(resultSet.getDouble("total_amount"));

        sale.setPaidAmount(resultSet.getDouble("paid_amount"));

        sale.setDueAmount(resultSet.getDouble("due_amount"));

        sale.setPaymentStatus(resultSet.getString("payment_status"));

        sale.setSaleStatus(resultSet.getString("sale_status"));

        sale.setNotes(resultSet.getString("notes"));

        return sale;
    }
}