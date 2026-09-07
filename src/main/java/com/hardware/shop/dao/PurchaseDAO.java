package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.Purchase;
import com.hardware.shop.model.PurchaseItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PurchaseDAO {

    public boolean createPurchase(Purchase purchase, List<PurchaseItem> items, String paymentMethod) throws SQLException {

        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Purchase must contain at least one item.");
        }

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                long purchaseId = insertPurchase(connection, purchase);

                insertPurchaseItemsAndUpdateStock(connection, purchaseId, items);

                if (purchase.getPaidAmount() > 0) {

                    insertPurchasePayment(connection, purchaseId, purchase.getPaidAmount(), paymentMethod);

                    insertCashTransaction(connection, purchaseId, purchase.getPaidAmount());
                }

                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }

    private long insertPurchase(Connection connection, Purchase purchase) throws SQLException {

        String sql = """
                INSERT INTO purchases (
                    purchase_number,
                    supplier_id,
                    subtotal,
                    discount,
                    total_amount,
                    paid_amount,
                    due_amount,
                    payment_status,
                    notes
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, purchase.getPurchaseNumber());

            statement.setInt(2, purchase.getSupplierId());

            statement.setDouble(3, purchase.getSubtotal());

            statement.setDouble(4, purchase.getDiscount());

            statement.setDouble(5, purchase.getTotalAmount());

            statement.setDouble(6, purchase.getPaidAmount());

            statement.setDouble(7, purchase.getDueAmount());

            statement.setString(8, purchase.getPaymentStatus());

            statement.setString(9, purchase.getNotes());

            int affectedRows = statement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating purchase failed.");
            }

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                if (!generatedKeys.next()) {
                    throw new SQLException("Creating purchase failed: no ID obtained.");
                }

                return generatedKeys.getLong(1);
            }
        }
    }

    private void insertPurchaseItemsAndUpdateStock(Connection connection, long purchaseId, List<PurchaseItem> items) throws SQLException {

        String itemSql = """
                INSERT INTO purchase_items (
                    purchase_id,
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
                SET quantity = quantity + ?
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
                VALUES (?, 'PURCHASE', ?, ?, ?)
                """;

        try (PreparedStatement itemStatement = connection.prepareStatement(itemSql);

             PreparedStatement stockSelectStatement = connection.prepareStatement(stockSelectSql);

             PreparedStatement stockUpdateStatement = connection.prepareStatement(stockUpdateSql);

             PreparedStatement movementStatement = connection.prepareStatement(movementSql)) {

            for (PurchaseItem item : items) {

                // Save purchase item
                itemStatement.setLong(1, purchaseId);

                itemStatement.setInt(2, item.getProductId());

                itemStatement.setDouble(3, item.getQuantity());

                itemStatement.setDouble(4, item.getUnitPrice());

                itemStatement.setDouble(5, item.getDiscount());

                itemStatement.setDouble(6, item.getTotalPrice());

                itemStatement.executeUpdate();

                // Lock stock row
                stockSelectStatement.setInt(1, item.getProductId());

                try (ResultSet resultSet = stockSelectStatement.executeQuery()) {

                    if (!resultSet.next()) {

                        throw new SQLException("Stock record not found for product ID: " + item.getProductId());
                    }
                }

                // Increase stock
                stockUpdateStatement.setDouble(1, item.getQuantity());

                stockUpdateStatement.setInt(2, item.getProductId());

                int stockUpdated = stockUpdateStatement.executeUpdate();

                if (stockUpdated == 0) {

                    throw new SQLException("Failed to update stock for product ID: " + item.getProductId());
                }

                // Create stock movement
                movementStatement.setInt(1, item.getProductId());

                movementStatement.setDouble(2, item.getQuantity());

                movementStatement.setLong(3, purchaseId);

                movementStatement.setString(4, "Stock received from purchase");

                movementStatement.executeUpdate();
            }
        }
    }

    private void insertPurchasePayment(Connection connection, long purchaseId, double amount, String paymentMethod) throws SQLException {

        String sql = """
                INSERT INTO purchase_payments (
                    purchase_id,
                    amount,
                    payment_method
                )
                VALUES (?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, purchaseId);

            statement.setDouble(2, amount);

            statement.setString(3, paymentMethod);

            statement.executeUpdate();
        }
    }

    private void insertCashTransaction(Connection connection, long purchaseId, double amount) throws SQLException {

        String sql = """
                INSERT INTO cash_transactions (
                    transaction_type,
                    reference_id,
                    amount,
                    description
                )
                VALUES ('PURCHASE_PAYMENT', ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, purchaseId);

            statement.setDouble(2, amount);

            statement.setString(3, "Payment for purchase " + purchaseId);

            statement.executeUpdate();
        }
    }

    public List<Purchase> findAll() throws SQLException {

        List<Purchase> purchases = new ArrayList<>();

        String sql = """
                SELECT
                    id,
                    purchase_number,
                    supplier_id,
                    purchase_date,
                    subtotal,
                    discount,
                    total_amount,
                    paid_amount,
                    due_amount,
                    payment_status,
                    notes
                FROM purchases
                ORDER BY id DESC
                """;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                purchases.add(mapPurchase(resultSet));
            }
        }

        return purchases;
    }

    public Purchase findById(long id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    purchase_number,
                    supplier_id,
                    purchase_date,
                    subtotal,
                    discount,
                    total_amount,
                    paid_amount,
                    due_amount,
                    payment_status,
                    notes
                FROM purchases
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapPurchase(resultSet);
                }
            }
        }

        return null;
    }
    public List<PurchaseItem> findItemsByPurchaseId(
            long purchaseId
    ) throws SQLException {

        List<PurchaseItem> items = new ArrayList<>();

        String sql = """
            SELECT
                id,
                purchase_id,
                product_id,
                quantity,
                unit_price,
                discount,
                total_price
            FROM purchase_items
            WHERE purchase_id = ?
            ORDER BY id
            """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setLong(
                    1,
                    purchaseId
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    PurchaseItem item =
                            new PurchaseItem();

                    item.setId(
                            resultSet.getLong("id")
                    );

                    item.setPurchaseId(
                            resultSet.getLong("purchase_id")
                    );

                    item.setProductId(
                            resultSet.getInt("product_id")
                    );

                    item.setQuantity(
                            resultSet.getDouble("quantity")
                    );

                    item.setUnitPrice(
                            resultSet.getDouble("unit_price")
                    );

                    item.setDiscount(
                            resultSet.getDouble("discount")
                    );

                    item.setTotalPrice(
                            resultSet.getDouble("total_price")
                    );

                    items.add(item);
                }
            }
        }

        return items;
    }

    private Purchase mapPurchase(ResultSet resultSet) throws SQLException {

        Purchase purchase = new Purchase();

        purchase.setId(resultSet.getLong("id"));

        purchase.setPurchaseNumber(resultSet.getString("purchase_number"));

        purchase.setSupplierId(resultSet.getInt("supplier_id"));

        purchase.setPurchaseDate(resultSet.getString("purchase_date"));

        purchase.setSubtotal(resultSet.getDouble("subtotal"));

        purchase.setDiscount(resultSet.getDouble("discount"));

        purchase.setTotalAmount(resultSet.getDouble("total_amount"));

        purchase.setPaidAmount(resultSet.getDouble("paid_amount"));

        purchase.setDueAmount(resultSet.getDouble("due_amount"));

        purchase.setPaymentStatus(resultSet.getString("payment_status"));

        purchase.setNotes(resultSet.getString("notes"));

        return purchase;
    }
}