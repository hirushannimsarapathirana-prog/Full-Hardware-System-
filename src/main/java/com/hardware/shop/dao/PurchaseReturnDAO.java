package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.PurchaseReturn;
import com.hardware.shop.model.PurchaseReturnItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PurchaseReturnDAO {

    public boolean createPurchaseReturn(PurchaseReturn purchaseReturn, List<PurchaseReturnItem> items) throws SQLException {

        String purchaseSql = """
                SELECT supplier_id
                FROM purchases
                WHERE id = ?
                FOR UPDATE
                """;

        String purchaseItemSql = """
                SELECT COALESCE(SUM(quantity), 0) AS purchased_quantity
                FROM purchase_items
                WHERE purchase_id = ?
                AND product_id = ?
                """;

        String returnedItemSql = """
                SELECT COALESCE(SUM(pri.quantity), 0) AS returned_quantity
                FROM purchase_return_items pri
                INNER JOIN purchase_returns pr
                    ON pr.id = pri.return_id
                WHERE pr.purchase_id = ?
                AND pri.product_id = ?
                """;

        String returnSql = """
                INSERT INTO purchase_returns (
                    return_number,
                    purchase_id,
                    supplier_id,
                    total_amount,
                    refund_method,
                    reason
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String itemSql = """
                INSERT INTO purchase_return_items (
                    return_id,
                    product_id,
                    quantity,
                    unit_price,
                    total_price
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        String stockSql = """
                SELECT quantity
                FROM product_stock
                WHERE product_id = ?
                FOR UPDATE
                """;

        String updateStockSql = """
                UPDATE product_stock
                SET quantity = quantity - ?
                WHERE product_id = ?
                AND quantity >= ?
                """;

        String movementSql = """
                INSERT INTO stock_movements (
                    product_id,
                    movement_type,
                    quantity,
                    reference_id,
                    notes
                )
                VALUES (
                    ?,
                    'PURCHASE_RETURN',
                    ?,
                    ?,
                    ?
                )
                """;

        String cashTransactionSql = """
                INSERT INTO cash_transactions (
                    transaction_type,
                    reference_id,
                    amount,
                    description
                )
                VALUES (
                    'PURCHASE_REFUND',
                    ?,
                    ?,
                    ?
                )
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                /*
                 * 1. Validate and lock original purchase
                 */

                int originalSupplierId;

                try (PreparedStatement statement = connection.prepareStatement(purchaseSql)) {

                    statement.setLong(1, purchaseReturn.getPurchaseId());

                    try (ResultSet resultSet = statement.executeQuery()) {

                        if (!resultSet.next()) {

                            throw new IllegalArgumentException("Purchase not found for purchase ID " + purchaseReturn.getPurchaseId());
                        }

                        originalSupplierId = resultSet.getInt("supplier_id");
                    }
                }

                /*
                 * Supplier must match original purchase
                 */

                if (originalSupplierId != purchaseReturn.getSupplierId()) {

                    throw new IllegalArgumentException("Supplier ID does not match the original purchase.");
                }

                /*
                 * 2. Prevent duplicate product lines
                 *    from bypassing quantity validation
                 */

                Map<Integer, Double> requestedQuantities = new HashMap<>();

                for (PurchaseReturnItem item : items) {

                    requestedQuantities.merge(item.getProductId(), item.getQuantity(), Double::sum);
                }

                /*
                 * 3. Validate every product against
                 *    original purchase quantity
                 */

                for (Map.Entry<Integer, Double> entry : requestedQuantities.entrySet()) {

                    int productId = entry.getKey();

                    double requestedQuantity = entry.getValue();

                    /*
                     * Find originally purchased quantity
                     */

                    double purchasedQuantity = 0;

                    try (PreparedStatement statement = connection.prepareStatement(purchaseItemSql)) {

                        statement.setLong(1, purchaseReturn.getPurchaseId());

                        statement.setInt(2, productId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                            if (resultSet.next()) {

                                purchasedQuantity = resultSet.getDouble("purchased_quantity");
                            }
                        }
                    }

                    if (purchasedQuantity <= 0) {

                        throw new IllegalArgumentException("Product ID " + productId + " was not purchased in purchase ID " + purchaseReturn.getPurchaseId());
                    }

                    /*
                     * Find quantity already returned
                     */

                    double alreadyReturnedQuantity = 0;

                    try (PreparedStatement statement = connection.prepareStatement(returnedItemSql)) {

                        statement.setLong(1, purchaseReturn.getPurchaseId());

                        statement.setInt(2, productId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                            if (resultSet.next()) {

                                alreadyReturnedQuantity = resultSet.getDouble("returned_quantity");
                            }
                        }
                    }

                    /*
                     * Calculate remaining returnable quantity
                     */

                    double remainingQuantity = purchasedQuantity - alreadyReturnedQuantity;

                    if (requestedQuantity > remainingQuantity) {

                        throw new IllegalArgumentException("Return quantity exceeds the remaining " + "returnable quantity for product ID " + productId + ". Remaining quantity: " + remainingQuantity);
                    }
                }

                /*
                 * 4. Create purchase return header
                 */

                long returnId;

                try (PreparedStatement statement = connection.prepareStatement(returnSql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, purchaseReturn.getReturnNumber());

                    statement.setLong(2, purchaseReturn.getPurchaseId());

                    statement.setInt(3, purchaseReturn.getSupplierId());

                    statement.setDouble(4, purchaseReturn.getTotalAmount());

                    statement.setString(5, purchaseReturn.getRefundMethod());

                    statement.setString(6, purchaseReturn.getReason());

                    int affectedRows = statement.executeUpdate();

                    if (affectedRows == 0) {

                        throw new SQLException("Creating purchase return failed.");
                    }

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {

                            throw new SQLException("Creating purchase return failed: " + "no ID obtained.");
                        }

                        returnId = generatedKeys.getLong(1);
                    }
                }

                /*
                 * 5. Insert items and reduce stock
                 */

                for (PurchaseReturnItem item : items) {

                    /*
                     * Lock stock row
                     */

                    try (PreparedStatement statement = connection.prepareStatement(stockSql)) {

                        statement.setInt(1, item.getProductId());

                        try (ResultSet resultSet = statement.executeQuery()) {

                            if (!resultSet.next()) {

                                throw new SQLException("Stock record not found for " + "product ID " + item.getProductId());
                            }

                            double currentStock = resultSet.getDouble("quantity");

                            if (currentStock < item.getQuantity()) {

                                throw new IllegalArgumentException("Insufficient stock for product ID " + item.getProductId() + ". Current stock: " + currentStock);
                            }
                        }
                    }

                    /*
                     * Insert return item
                     */

                    try (PreparedStatement statement = connection.prepareStatement(itemSql)) {

                        statement.setLong(1, returnId);

                        statement.setInt(2, item.getProductId());

                        statement.setDouble(3, item.getQuantity());

                        statement.setDouble(4, item.getUnitPrice());

                        statement.setDouble(5, item.getTotalPrice());

                        statement.executeUpdate();
                    }

                    /*
                     * Reduce stock
                     */

                    try (PreparedStatement statement = connection.prepareStatement(updateStockSql)) {

                        statement.setDouble(1, item.getQuantity());

                        statement.setInt(2, item.getProductId());

                        statement.setDouble(3, item.getQuantity());

                        int affectedRows = statement.executeUpdate();

                        if (affectedRows == 0) {

                            throw new SQLException("Failed to reduce stock for product ID " + item.getProductId());
                        }
                    }

                    /*
                     * Record stock movement
                     */

                    try (PreparedStatement statement = connection.prepareStatement(movementSql)) {

                        statement.setInt(1, item.getProductId());

                        statement.setDouble(2, item.getQuantity());

                        statement.setLong(3, returnId);

                        statement.setString(4, "Stock returned to supplier - " + "Purchase Return " + returnId);

                        statement.executeUpdate();
                    }
                }

                /*
                 * 6. Record supplier refund
                 *
                 * CREDIT return does not create cash transaction.
                 * CASH/CARD/BANK_TRANSFER creates PURCHASE_REFUND.
                 */

                if (purchaseReturn.getTotalAmount() > 0 && !"CREDIT".equalsIgnoreCase(purchaseReturn.getRefundMethod())) {

                    try (PreparedStatement statement = connection.prepareStatement(cashTransactionSql)) {

                        statement.setLong(1, returnId);

                        statement.setDouble(2, purchaseReturn.getTotalAmount());

                        statement.setString(3, "Supplier refund received - " + "Purchase Return " + returnId);

                        statement.executeUpdate();
                    }
                }

                /*
                 * 7. Commit transaction
                 */

                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }

    public List<PurchaseReturn> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    return_number,
                    purchase_id,
                    supplier_id,
                    return_date,
                    total_amount,
                    refund_method,
                    reason
                FROM purchase_returns
                ORDER BY id DESC
                """;

        List<PurchaseReturn> returns = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                returns.add(mapPurchaseReturn(resultSet));
            }
        }

        return returns;
    }

    public PurchaseReturn findById(long id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    return_number,
                    purchase_id,
                    supplier_id,
                    return_date,
                    total_amount,
                    refund_method,
                    reason
                FROM purchase_returns
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapPurchaseReturn(resultSet);
                }
            }
        }

        return null;
    }

    public List<PurchaseReturnItem> findItemsByReturnId(long returnId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    return_id,
                    product_id,
                    quantity,
                    unit_price,
                    total_price
                FROM purchase_return_items
                WHERE return_id = ?
                ORDER BY id
                """;

        List<PurchaseReturnItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, returnId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    items.add(mapPurchaseReturnItem(resultSet));
                }
            }
        }

        return items;
    }

    private PurchaseReturn mapPurchaseReturn(ResultSet resultSet) throws SQLException {

        PurchaseReturn purchaseReturn = new PurchaseReturn();

        purchaseReturn.setId(resultSet.getLong("id"));

        purchaseReturn.setReturnNumber(resultSet.getString("return_number"));

        purchaseReturn.setPurchaseId(resultSet.getLong("purchase_id"));

        purchaseReturn.setSupplierId(resultSet.getInt("supplier_id"));

        purchaseReturn.setReturnDate(resultSet.getString("return_date"));

        purchaseReturn.setTotalAmount(resultSet.getDouble("total_amount"));

        purchaseReturn.setRefundMethod(resultSet.getString("refund_method"));

        purchaseReturn.setReason(resultSet.getString("reason"));

        return purchaseReturn;
    }

    private PurchaseReturnItem mapPurchaseReturnItem(ResultSet resultSet) throws SQLException {

        PurchaseReturnItem item = new PurchaseReturnItem();

        item.setId(resultSet.getLong("id"));

        item.setReturnId(resultSet.getLong("return_id"));

        item.setProductId(resultSet.getInt("product_id"));

        item.setQuantity(resultSet.getDouble("quantity"));

        item.setUnitPrice(resultSet.getDouble("unit_price"));

        item.setTotalPrice(resultSet.getDouble("total_price"));

        return item;
    }
}