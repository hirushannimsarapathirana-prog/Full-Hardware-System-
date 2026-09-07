package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.model.SalesReturn;
import com.hardware.shop.model.SalesReturnItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SalesReturnDAO {

    public boolean createSalesReturn(SalesReturn salesReturn, List<SalesReturnItem> items) throws SQLException {

        String saleSql = """
                SELECT customer_id, sale_status
                FROM sales
                WHERE id = ?
                FOR UPDATE
                """;

        String saleItemSql = """
                SELECT COALESCE(SUM(quantity), 0) AS sold_quantity
                FROM sale_items
                WHERE sale_id = ?
                AND product_id = ?
                """;

        String returnedItemSql = """
                SELECT COALESCE(SUM(sri.quantity), 0) AS returned_quantity
                FROM sales_return_items sri
                INNER JOIN sales_returns sr
                    ON sr.id = sri.return_id
                WHERE sr.sale_id = ?
                AND sri.product_id = ?
                """;

        String returnSql = """
                INSERT INTO sales_returns (
                    return_number,
                    sale_id,
                    customer_id,
                    total_amount,
                    refund_method,
                    reason
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String itemSql = """
                INSERT INTO sales_return_items (
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
                VALUES (
                    ?,
                    'SALE_RETURN',
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
                    'SALE_REFUND',
                    ?,
                    ?,
                    ?
                )
                """;

        try (Connection connection = DBConnection.getConnection()) {

            try {

                connection.setAutoCommit(false);

                /*
                 * =========================================================
                 * 1. Validate and lock original sale
                 * =========================================================
                 */

                Integer saleCustomerId = null;
                String saleStatus = null;

                try (PreparedStatement statement = connection.prepareStatement(saleSql)) {

                    statement.setLong(1, salesReturn.getSaleId());

                    try (ResultSet resultSet = statement.executeQuery()) {

                        if (!resultSet.next()) {

                            throw new IllegalArgumentException("Sale not found for sale ID " + salesReturn.getSaleId());
                        }

                        int customerId = resultSet.getInt("customer_id");

                        if (!resultSet.wasNull()) {
                            saleCustomerId = customerId;
                        }

                        saleStatus = resultSet.getString("sale_status");
                    }
                }

                /*
                 * Only completed sales can be returned.
                 */
                if (!"COMPLETED".equalsIgnoreCase(saleStatus)) {

                    throw new IllegalArgumentException("Only completed sales can be returned.");
                }

                /*
                 * Customer ID must match the original sale.
                 */
                if (saleCustomerId == null) {

                    if (salesReturn.getCustomerId() != null) {

                        throw new IllegalArgumentException("Customer ID does not match the original sale.");
                    }

                } else {

                    if (salesReturn.getCustomerId() == null || !saleCustomerId.equals(salesReturn.getCustomerId())) {

                        throw new IllegalArgumentException("Customer ID does not match the original sale.");
                    }
                }

                /*
                 * =========================================================
                 * 2. Prevent duplicate product lines in the same request
                 * =========================================================
                 */

                Map<Integer, Double> requestedQuantities = new HashMap<>();

                for (SalesReturnItem item : items) {

                    requestedQuantities.merge(item.getProductId(), item.getQuantity(), Double::sum);
                }

                /*
                 * =========================================================
                 * 3. Validate every product and return quantity
                 * =========================================================
                 */

                for (Map.Entry<Integer, Double> entry : requestedQuantities.entrySet()) {

                    int productId = entry.getKey();

                    double requestedQuantity = entry.getValue();

                    /*
                     * Original sold quantity.
                     */
                    double soldQuantity = 0;

                    try (PreparedStatement statement = connection.prepareStatement(saleItemSql)) {

                        statement.setLong(1, salesReturn.getSaleId());

                        statement.setInt(2, productId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                            if (resultSet.next()) {

                                soldQuantity = resultSet.getDouble("sold_quantity");
                            }
                        }
                    }

                    /*
                     * Product must have been sold in this sale.
                     */
                    if (soldQuantity <= 0) {

                        throw new IllegalArgumentException("Product ID " + productId + " was not sold in sale ID " + salesReturn.getSaleId());
                    }

                    /*
                     * Quantity already returned.
                     */
                    double alreadyReturnedQuantity = 0;

                    try (PreparedStatement statement = connection.prepareStatement(returnedItemSql)) {

                        statement.setLong(1, salesReturn.getSaleId());

                        statement.setInt(2, productId);

                        try (ResultSet resultSet = statement.executeQuery()) {

                            if (resultSet.next()) {

                                alreadyReturnedQuantity = resultSet.getDouble("returned_quantity");
                            }
                        }
                    }

                    /*
                     * Remaining quantity that can still be returned.
                     */
                    double remainingQuantity = soldQuantity - alreadyReturnedQuantity;

                    /*
                     * Prevent over-return.
                     */
                    if (requestedQuantity > remainingQuantity) {

                        throw new IllegalArgumentException("Return quantity exceeds the remaining " + "returnable quantity for product ID " + productId + ". Remaining quantity: " + remainingQuantity);
                    }
                }

                /*
                 * =========================================================
                 * 4. Create sales return header
                 * =========================================================
                 */

                long returnId;

                try (PreparedStatement statement = connection.prepareStatement(returnSql, Statement.RETURN_GENERATED_KEYS)) {

                    statement.setString(1, salesReturn.getReturnNumber());

                    statement.setLong(2, salesReturn.getSaleId());

                    if (salesReturn.getCustomerId() == null) {

                        statement.setNull(3, java.sql.Types.INTEGER);

                    } else {

                        statement.setInt(3, salesReturn.getCustomerId());
                    }

                    statement.setDouble(4, salesReturn.getTotalAmount());

                    statement.setString(5, salesReturn.getRefundMethod());

                    statement.setString(6, salesReturn.getReason());

                    int affectedRows = statement.executeUpdate();

                    if (affectedRows == 0) {

                        throw new SQLException("Creating sales return failed.");
                    }

                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {

                        if (!generatedKeys.next()) {

                            throw new SQLException("Creating sales return failed: " + "no ID obtained.");
                        }

                        returnId = generatedKeys.getLong(1);
                    }
                }

                /*
                 * =========================================================
                 * 5. Insert items and update stock
                 * =========================================================
                 */

                for (SalesReturnItem item : items) {

                    /*
                     * Lock stock row.
                     */
                    try (PreparedStatement statement = connection.prepareStatement(stockSql)) {

                        statement.setInt(1, item.getProductId());

                        try (ResultSet resultSet = statement.executeQuery()) {

                            if (!resultSet.next()) {

                                throw new SQLException("Stock record not found for " + "product ID " + item.getProductId());
                            }
                        }
                    }

                    /*
                     * Insert return item.
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
                     * Add returned quantity back to stock.
                     */
                    try (PreparedStatement statement = connection.prepareStatement(updateStockSql)) {

                        statement.setDouble(1, item.getQuantity());

                        statement.setInt(2, item.getProductId());

                        int affectedRows = statement.executeUpdate();

                        if (affectedRows == 0) {

                            throw new SQLException("Failed to update stock for product ID " + item.getProductId());
                        }
                    }

                    /*
                     * Record stock movement.
                     */
                    try (PreparedStatement statement = connection.prepareStatement(movementSql)) {

                        statement.setInt(1, item.getProductId());

                        statement.setDouble(2, item.getQuantity());

                        statement.setLong(3, returnId);

                        statement.setString(4, "Stock returned from sales return " + returnId);

                        statement.executeUpdate();
                    }
                }

                /*
                 * =========================================================
                 * 6. Record refund transaction
                 * =========================================================
                 *
                 * CASH / CARD / BANK_TRANSFER
                 * -> SALE_REFUND transaction
                 *
                 * CREDIT
                 * -> No cash transaction.
                 *    Customer balance handles the adjustment.
                 */

                if (salesReturn.getTotalAmount() > 0 && !"CREDIT".equalsIgnoreCase(salesReturn.getRefundMethod())) {

                    try (PreparedStatement statement = connection.prepareStatement(cashTransactionSql)) {

                        statement.setLong(1, returnId);

                        statement.setDouble(2, salesReturn.getTotalAmount());

                        statement.setString(3, "Refund for sales return " + returnId);

                        statement.executeUpdate();
                    }
                }

                /*
                 * =========================================================
                 * 7. Commit transaction
                 * =========================================================
                 */

                connection.commit();

                return true;

            } catch (Exception e) {

                connection.rollback();

                throw e;
            }
        }
    }

    public List<SalesReturn> findAll() throws SQLException {

        String sql = """
                SELECT
                    id,
                    return_number,
                    sale_id,
                    customer_id,
                    return_date,
                    total_amount,
                    refund_method,
                    reason
                FROM sales_returns
                ORDER BY id DESC
                """;

        List<SalesReturn> returns = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                returns.add(mapSalesReturn(resultSet));
            }
        }

        return returns;
    }

    public SalesReturn findById(long id) throws SQLException {

        String sql = """
                SELECT
                    id,
                    return_number,
                    sale_id,
                    customer_id,
                    return_date,
                    total_amount,
                    refund_method,
                    reason
                FROM sales_returns
                WHERE id = ?
                """;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {

                if (resultSet.next()) {

                    return mapSalesReturn(resultSet);
                }
            }
        }

        return null;
    }

    public List<SalesReturnItem> findItemsByReturnId(long returnId) throws SQLException {

        String sql = """
                SELECT
                    id,
                    return_id,
                    product_id,
                    quantity,
                    unit_price,
                    total_price
                FROM sales_return_items
                WHERE return_id = ?
                ORDER BY id
                """;

        List<SalesReturnItem> items = new ArrayList<>();

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, returnId);

            try (ResultSet resultSet = statement.executeQuery()) {

                while (resultSet.next()) {

                    items.add(mapSalesReturnItem(resultSet));
                }
            }
        }

        return items;
    }

    private SalesReturn mapSalesReturn(ResultSet resultSet) throws SQLException {

        SalesReturn salesReturn = new SalesReturn();

        salesReturn.setId(resultSet.getLong("id"));

        salesReturn.setReturnNumber(resultSet.getString("return_number"));

        salesReturn.setSaleId(resultSet.getLong("sale_id"));

        int customerId = resultSet.getInt("customer_id");

        if (resultSet.wasNull()) {

            salesReturn.setCustomerId(null);

        } else {

            salesReturn.setCustomerId(customerId);
        }

        salesReturn.setReturnDate(resultSet.getString("return_date"));

        salesReturn.setTotalAmount(resultSet.getDouble("total_amount"));

        salesReturn.setRefundMethod(resultSet.getString("refund_method"));

        salesReturn.setReason(resultSet.getString("reason"));

        return salesReturn;
    }

    private SalesReturnItem mapSalesReturnItem(ResultSet resultSet) throws SQLException {

        SalesReturnItem item = new SalesReturnItem();

        item.setId(resultSet.getLong("id"));

        item.setReturnId(resultSet.getLong("return_id"));

        item.setProductId(resultSet.getInt("product_id"));

        item.setQuantity(resultSet.getDouble("quantity"));

        item.setUnitPrice(resultSet.getDouble("unit_price"));

        item.setTotalPrice(resultSet.getDouble("total_price"));

        return item;
    }
}