package com.hardware.shop.service;

import com.hardware.shop.dao.SupplierPaymentDAO;
import com.hardware.shop.model.SupplierPayment;

import java.sql.SQLException;
import java.util.List;

public class SupplierPaymentService {

    private final SupplierPaymentDAO supplierPaymentDAO;

    public SupplierPaymentService() {
        this.supplierPaymentDAO = new SupplierPaymentDAO();
    }

    public boolean createPayment(SupplierPayment payment) throws SQLException {

        validatePayment(payment);

        return supplierPaymentDAO.createPayment(payment);
    }

    public List<SupplierPayment> findAll() throws SQLException {

        return supplierPaymentDAO.findAll();
    }

    public List<SupplierPayment> findBySupplierId(int supplierId) throws SQLException {

        if (supplierId <= 0) {

            throw new IllegalArgumentException("Invalid supplier ID.");
        }

        return supplierPaymentDAO.findBySupplierId(supplierId);
    }

    private void validatePayment(SupplierPayment payment) {

        if (payment == null) {

            throw new IllegalArgumentException("Supplier payment cannot be null.");
        }

        if (payment.getSupplierId() <= 0) {

            throw new IllegalArgumentException("Valid supplier ID is required.");
        }

        if (payment.getAmount() <= 0) {

            throw new IllegalArgumentException("Payment amount must be greater than 0.");
        }

        if (payment.getPaymentMethod() == null || payment.getPaymentMethod().isBlank()) {

            throw new IllegalArgumentException("Payment method is required.");
        }

        if (!payment.getPaymentMethod().equals("CASH") && !payment.getPaymentMethod().equals("CARD") && !payment.getPaymentMethod().equals("BANK_TRANSFER")) {

            throw new IllegalArgumentException("Invalid payment method.");
        }
    }
}
