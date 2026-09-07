package com.hardware.shop.service;

import com.hardware.shop.dao.PurchaseDAO;
import com.hardware.shop.model.Purchase;
import com.hardware.shop.model.PurchaseItem;
import com.hardware.shop.dto.PurchaseDetailsResponse;
import java.util.ArrayList;
import java.sql.SQLException;
import java.util.List;

public class PurchaseService {

    private final PurchaseDAO purchaseDAO;

    public PurchaseService() {
        this.purchaseDAO = new PurchaseDAO();
    }

    public boolean createPurchase(Purchase purchase, List<PurchaseItem> items, String paymentMethod) throws SQLException {

        validatePurchase(purchase);
        validateItems(items);
        validatePayment(purchase, paymentMethod);

        calculateTotals(purchase, items);

        return purchaseDAO.createPurchase(purchase, items, paymentMethod);
    }

    public List<Purchase> findAll() throws SQLException {

        return purchaseDAO.findAll();
    }

    public Purchase findById(long id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Purchase ID must be greater than 0.");
        }

        return purchaseDAO.findById(id);
    }

    private void validatePurchase(Purchase purchase) {

        if (purchase == null) {

            throw new IllegalArgumentException("Purchase cannot be null.");
        }

        if (purchase.getPurchaseNumber() == null || purchase.getPurchaseNumber().isBlank()) {

            throw new IllegalArgumentException("Purchase number is required.");
        }

        if (purchase.getSupplierId() <= 0) {

            throw new IllegalArgumentException("Valid supplier is required.");
        }

        if (purchase.getDiscount() < 0) {

            throw new IllegalArgumentException("Discount cannot be negative.");
        }

        if (purchase.getPaidAmount() < 0) {

            throw new IllegalArgumentException("Paid amount cannot be negative.");
        }
    }

    private void validateItems(List<PurchaseItem> items) {

        if (items == null || items.isEmpty()) {

            throw new IllegalArgumentException("Purchase must contain at least one item.");
        }

        for (PurchaseItem item : items) {

            if (item == null) {

                throw new IllegalArgumentException("Purchase item cannot be null.");
            }

            if (item.getProductId() <= 0) {

                throw new IllegalArgumentException("Valid product is required.");
            }

            if (item.getQuantity() <= 0) {

                throw new IllegalArgumentException("Quantity must be greater than 0.");
            }

            if (item.getUnitPrice() < 0) {

                throw new IllegalArgumentException("Unit price cannot be negative.");
            }

            if (item.getDiscount() < 0) {

                throw new IllegalArgumentException("Item discount cannot be negative.");
            }

            double calculatedTotal = (item.getQuantity() * item.getUnitPrice()) - item.getDiscount();

            if (calculatedTotal < 0) {

                throw new IllegalArgumentException("Item total cannot be negative.");
            }

            item.setTotalPrice(calculatedTotal);
        }
    }

    private void calculateTotals(Purchase purchase, List<PurchaseItem> items) {

        double subtotal = 0;

        for (PurchaseItem item : items) {

            subtotal += item.getTotalPrice();
        }

        double totalAmount = subtotal - purchase.getDiscount();

        if (totalAmount < 0) {

            throw new IllegalArgumentException("Purchase total cannot be negative.");
        }

        if (purchase.getPaidAmount() > totalAmount) {

            throw new IllegalArgumentException("Paid amount cannot be greater than total amount.");
        }

        double dueAmount = totalAmount - purchase.getPaidAmount();

        purchase.setSubtotal(subtotal);

        purchase.setTotalAmount(totalAmount);

        purchase.setDueAmount(dueAmount);

        if (dueAmount == 0) {

            purchase.setPaymentStatus("PAID");

        } else if (purchase.getPaidAmount() > 0) {

            purchase.setPaymentStatus("PARTIAL");

        } else {

            purchase.setPaymentStatus("CREDIT");
        }
    }

    private void validatePayment(Purchase purchase, String paymentMethod) {

        if (purchase.getPaidAmount() > 0) {

            if (paymentMethod == null || paymentMethod.isBlank()) {

                throw new IllegalArgumentException("Payment method is required.");
            }

            if (!paymentMethod.equals("CASH") && !paymentMethod.equals("CARD") && !paymentMethod.equals("BANK_TRANSFER")) {

                throw new IllegalArgumentException("Invalid payment method.");
            }
        }
    }
    public PurchaseDetailsResponse findDetails(
            long id
    ) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException(
                    "Purchase ID must be greater than 0."
            );
        }

        Purchase purchase =
                purchaseDAO.findById(id);

        if (purchase == null) {
            return null;
        }

        List<PurchaseItem> items =
                purchaseDAO.findItemsByPurchaseId(id);

        return new PurchaseDetailsResponse(
                purchase,
                items
        );
    }
}