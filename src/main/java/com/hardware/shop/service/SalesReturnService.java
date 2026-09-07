package com.hardware.shop.service;

import com.hardware.shop.dao.SalesReturnDAO;
import com.hardware.shop.model.SalesReturn;
import com.hardware.shop.model.SalesReturnItem;

import java.sql.SQLException;
import java.util.List;

public class SalesReturnService {

    private final SalesReturnDAO salesReturnDAO;

    public SalesReturnService() {
        this.salesReturnDAO = new SalesReturnDAO();
    }

    public boolean createSalesReturn(SalesReturn salesReturn, List<SalesReturnItem> items) throws SQLException {

        validateSalesReturn(salesReturn, items);

        calculateTotals(salesReturn, items);

        return salesReturnDAO.createSalesReturn(salesReturn, items);
    }

    public List<SalesReturn> findAll() throws SQLException {

        return salesReturnDAO.findAll();
    }

    public SalesReturn findById(long id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Invalid sales return ID.");
        }

        return salesReturnDAO.findById(id);
    }

    public List<SalesReturnItem> findItemsByReturnId(long returnId) throws SQLException {

        if (returnId <= 0) {
            throw new IllegalArgumentException("Invalid sales return ID.");
        }

        return salesReturnDAO.findItemsByReturnId(returnId);
    }

    private void validateSalesReturn(SalesReturn salesReturn, List<SalesReturnItem> items) {

        if (salesReturn == null) {
            throw new IllegalArgumentException("Sales return cannot be null.");
        }

        if (salesReturn.getReturnNumber() == null || salesReturn.getReturnNumber().isBlank()) {

            throw new IllegalArgumentException("Return number is required.");
        }

        if (salesReturn.getSaleId() <= 0) {

            throw new IllegalArgumentException("Valid sale ID is required.");
        }

        if (salesReturn.getRefundMethod() == null || salesReturn.getRefundMethod().isBlank()) {

            throw new IllegalArgumentException("Refund method is required.");
        }

        String refundMethod = salesReturn.getRefundMethod();

        if (!refundMethod.equals("CASH") && !refundMethod.equals("CARD") && !refundMethod.equals("BANK_TRANSFER") && !refundMethod.equals("CREDIT")) {

            throw new IllegalArgumentException("Invalid refund method.");
        }

        if (items == null || items.isEmpty()) {

            throw new IllegalArgumentException("At least one return item is required.");
        }

        for (SalesReturnItem item : items) {

            if (item == null) {

                throw new IllegalArgumentException("Return item cannot be null.");
            }

            if (item.getProductId() <= 0) {

                throw new IllegalArgumentException("Valid product ID is required.");
            }

            if (item.getQuantity() <= 0) {

                throw new IllegalArgumentException("Return quantity must be greater than 0.");
            }

            if (item.getUnitPrice() < 0) {

                throw new IllegalArgumentException("Unit price cannot be negative.");
            }
        }
    }

    private void calculateTotals(SalesReturn salesReturn, List<SalesReturnItem> items) {

        double totalAmount = 0;

        for (SalesReturnItem item : items) {

            double itemTotal = item.getQuantity() * item.getUnitPrice();

            item.setTotalPrice(itemTotal);

            totalAmount += itemTotal;
        }

        salesReturn.setTotalAmount(totalAmount);
    }
}