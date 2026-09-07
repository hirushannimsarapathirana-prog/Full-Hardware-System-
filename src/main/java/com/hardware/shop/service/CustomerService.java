package com.hardware.shop.service;

import com.hardware.shop.dao.CustomerDAO;
import com.hardware.shop.model.Customer;

import java.sql.SQLException;
import java.util.List;

public class CustomerService {

    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public boolean save(Customer customer) throws SQLException {
        validate(customer);
        return customerDAO.save(customer);
    }

    public List<Customer> findAll() throws SQLException {
        return customerDAO.findAll();
    }

    public Customer findById(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be greater than 0.");
        }

        return customerDAO.findById(id);
    }

    public boolean update(Customer customer) throws SQLException {

        if (customer == null || customer.getId() <= 0) {
            throw new IllegalArgumentException("Valid customer ID is required.");
        }

        validate(customer);

        return customerDAO.update(customer);
    }

    public boolean delete(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Customer ID must be greater than 0.");
        }

        return customerDAO.delete(id);
    }

    private void validate(Customer customer) {

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null.");
        }

        if (customer.getCustomerCode() == null || customer.getCustomerCode().isBlank()) {

            throw new IllegalArgumentException("Customer code is required.");
        }

        if (customer.getCustomerName() == null || customer.getCustomerName().isBlank()) {

            throw new IllegalArgumentException("Customer name is required.");
        }

        if (customer.getCreditLimit() < 0) {
            throw new IllegalArgumentException("Credit limit cannot be negative.");
        }
    }
}