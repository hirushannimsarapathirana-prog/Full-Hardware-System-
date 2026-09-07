package com.hardware.shop.service;

import com.hardware.shop.dao.SaleDAO;
import com.hardware.shop.dto.SaleDetailsResponse;
import com.hardware.shop.model.Sale;
import com.hardware.shop.model.SaleItem;

import java.sql.SQLException;
import java.util.List;

public class SaleService {

    private final SaleDAO saleDAO;

    public SaleService() {
        this.saleDAO = new SaleDAO();
    }


    // =========================================================
    // CREATE SALE
    // =========================================================

    public boolean createSale(Sale sale, List<SaleItem> items, String paymentMethod) throws SQLException {

        validateSale(sale);

        validateItems(items);

        calculateTotals(sale, items);

        validatePayment(sale, paymentMethod);

        return saleDAO.createSale(sale, items, paymentMethod);
    }


    // =========================================================
    // GET ALL SALES
    // =========================================================

    public List<Sale> findAll() throws SQLException {

        return saleDAO.findAll();
    }


    // =========================================================
    // GET SALE BY ID
    // =========================================================

    public Sale findById(long id) throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException("Invalid sale ID.");
        }

        return saleDAO.findById(id);
    }


    // =========================================================
    // GET SALE DETAILS
    // =========================================================

    public SaleDetailsResponse findDetails(long id) throws SQLException {

        if (id <= 0) {

            throw new IllegalArgumentException("Invalid sale ID.");
        }

        Sale sale = saleDAO.findById(id);

        if (sale == null) {

            return null;
        }

        List<SaleItem> items = saleDAO.findItemsBySaleId(id);

        return new SaleDetailsResponse(sale, items);
    }


    // =========================================================
    // VALIDATE SALE
    // =========================================================

    private void validateSale(Sale sale) {

        if (sale == null) {

            throw new IllegalArgumentException("Sale cannot be null.");
        }

        if (sale.getInvoiceNumber() == null || sale.getInvoiceNumber().isBlank()) {

            throw new IllegalArgumentException("Invoice number is required.");
        }

        if (sale.getDiscount() < 0) {

            throw new IllegalArgumentException("Discount cannot be negative.");
        }

        if (sale.getPaidAmount() < 0) {

            throw new IllegalArgumentException("Paid amount cannot be negative.");
        }

        if (sale.getSaleStatus() == null || sale.getSaleStatus().isBlank()) {

            sale.setSaleStatus("COMPLETED");
        }

        if (!sale.getSaleStatus().equals("COMPLETED") && !sale.getSaleStatus().equals("CANCELLED")) {

            throw new IllegalArgumentException("Invalid sale status.");
        }

        if (sale.getSaleStatus().equals("CANCELLED")) {

            throw new IllegalArgumentException("New sales cannot be created as CANCELLED.");
        }
    }


    // =========================================================
    // VALIDATE SALE ITEMS
    // =========================================================

    private void validateItems(List<SaleItem> items) {

        if (items == null || items.isEmpty()) {

            throw new IllegalArgumentException("Sale must contain at least one item.");
        }

        for (SaleItem item : items) {

            if (item == null) {

                throw new IllegalArgumentException("Sale item cannot be null.");
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


    // =========================================================
    // CALCULATE SALE TOTALS
    // =========================================================

    private void calculateTotals(Sale sale, List<SaleItem> items) {

        double subtotal = 0;

        for (SaleItem item : items) {

            subtotal += item.getTotalPrice();
        }

        double totalAmount = subtotal - sale.getDiscount();

        if (totalAmount < 0) {

            throw new IllegalArgumentException("Sale total cannot be negative.");
        }

        if (sale.getPaidAmount() > totalAmount) {

            throw new IllegalArgumentException("Paid amount cannot be greater than total amount.");
        }

        double dueAmount = totalAmount - sale.getPaidAmount();

        sale.setSubtotal(subtotal);

        sale.setTotalAmount(totalAmount);

        sale.setDueAmount(dueAmount);


        // -----------------------------------------------------
        // Determine payment status
        // -----------------------------------------------------

        if (dueAmount == 0) {

            sale.setPaymentStatus("PAID");

        } else if (sale.getPaidAmount() > 0) {

            sale.setPaymentStatus("PARTIAL");

        } else {

            sale.setPaymentStatus("CREDIT");
        }
    }


    // =========================================================
    // VALIDATE PAYMENT
    // =========================================================

    private void validatePayment(Sale sale, String paymentMethod) {

        if (sale.getPaidAmount() > 0) {

            if (paymentMethod == null || paymentMethod.isBlank()) {

                throw new IllegalArgumentException("Payment method is required.");
            }

            if (!paymentMethod.equals("CASH") && !paymentMethod.equals("CARD") && !paymentMethod.equals("BANK_TRANSFER")) {

                throw new IllegalArgumentException("Invalid payment method.");
            }
        }


        // -----------------------------------------------------
        // Credit sale requires customer
        // -----------------------------------------------------

        if (sale.getDueAmount() > 0) {

            if (sale.getCustomerId() == null || sale.getCustomerId() <= 0) {

                throw new IllegalArgumentException("Customer is required for credit sales.");
            }
        }
    }
}