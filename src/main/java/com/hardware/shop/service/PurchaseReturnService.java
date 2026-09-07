package com.hardware.shop.service;

import com.hardware.shop.dao.PurchaseReturnDAO;
import com.hardware.shop.model.PurchaseReturn;
import com.hardware.shop.model.PurchaseReturnItem;

import java.sql.SQLException;
import java.util.List;

public class PurchaseReturnService {

    private final PurchaseReturnDAO purchaseReturnDAO;

    public PurchaseReturnService() {
        this.purchaseReturnDAO = new PurchaseReturnDAO();
    }

    public boolean createPurchaseReturn(PurchaseReturn purchaseReturn, List<PurchaseReturnItem> items) throws SQLException {

        validatePurchaseReturn(purchaseReturn, items);

        calculateTotals(purchaseReturn, items);

        return purchaseReturnDAO.createPurchaseReturn(purchaseReturn, items);
    }

    public List<PurchaseReturn> findAll() throws SQLException {

        return purchaseReturnDAO.findAll();
    }

    public PurchaseReturn findById(long id) throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException("Invalid purchase return ID.");
        }

        return purchaseReturnDAO.findById(id);
    }

    public List<PurchaseReturnItem> findItemsByReturnId(long returnId) throws SQLException {

        if (returnId <= 0) {

            throw new IllegalArgumentException("Invalid purchase return ID.");
        }

        return purchaseReturnDAO.findItemsByReturnId(returnId);
    }

    private void validatePurchaseReturn(PurchaseReturn purchaseReturn, List<PurchaseReturnItem> items) {

        if (purchaseReturn == null) {

            throw new IllegalArgumentException("Purchase return cannot be null.");
        }

        if (purchaseReturn.getReturnNumber() == null || purchaseReturn.getReturnNumber().isBlank()) {

            throw new IllegalArgumentException("Return number is required.");
        }

        if (purchaseReturn.getPurchaseId() <= 0) {

            throw new IllegalArgumentException("Valid purchase ID is required.");
        }

        if (purchaseReturn.getSupplierId() <= 0) {

            throw new IllegalArgumentException("Valid supplier ID is required.");
        }

        if (purchaseReturn.getRefundMethod() == null || purchaseReturn.getRefundMethod().isBlank()) {

            throw new IllegalArgumentException("Refund method is required.");
        }

        String refundMethod = purchaseReturn.getRefundMethod();

        if (!refundMethod.equals("CASH") && !refundMethod.equals("BANK_TRANSFER") && !refundMethod.equals("CREDIT")) {

            throw new IllegalArgumentException("Invalid refund method.");
        }

        if (items == null || items.isEmpty()) {

            throw new IllegalArgumentException("At least one return item is required.");
        }

        for (PurchaseReturnItem item : items) {

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

    private void calculateTotals(PurchaseReturn purchaseReturn, List<PurchaseReturnItem> items) {

        double totalAmount = 0;

        for (PurchaseReturnItem item : items) {

            double itemTotal = item.getQuantity() * item.getUnitPrice();

            item.setTotalPrice(itemTotal);

            totalAmount += itemTotal;
        }

        purchaseReturn.setTotalAmount(totalAmount);
    }
}