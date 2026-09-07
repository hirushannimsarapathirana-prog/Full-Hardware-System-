package com.hardware.shop.service;

import com.hardware.shop.dao.CustomerPaymentDAO;
import com.hardware.shop.model.CustomerPayment;

import java.sql.SQLException;
import java.util.List;

public class CustomerPaymentService {

    private final CustomerPaymentDAO customerPaymentDAO;

    public CustomerPaymentService() {
        this.customerPaymentDAO = new CustomerPaymentDAO();
    }


    // =========================================================
    // CREATE CUSTOMER PAYMENT
    // =========================================================

    public boolean createPayment(CustomerPayment payment) throws SQLException {

        validatePayment(payment);

        return customerPaymentDAO.createPayment(payment);
    }


    // =========================================================
    // GET ALL PAYMENTS
    // =========================================================

    public List<CustomerPayment> findAll() throws SQLException {

        return customerPaymentDAO.findAll();
    }


    // =========================================================
    // GET PAYMENTS BY CUSTOMER
    // =========================================================

    public List<CustomerPayment> findByCustomerId(int customerId) throws SQLException {

        if (customerId <= 0) {

            throw new IllegalArgumentException("Invalid customer ID.");
        }

        return customerPaymentDAO.findByCustomerId(customerId);
    }


    // =========================================================
    // VALIDATE PAYMENT
    // =========================================================

    private void validatePayment(CustomerPayment payment) {

        if (payment == null) {

            throw new IllegalArgumentException("Customer payment cannot be null.");
        }

        if (payment.getCustomerId() <= 0) {

            throw new IllegalArgumentException("Valid customer ID is required.");
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
