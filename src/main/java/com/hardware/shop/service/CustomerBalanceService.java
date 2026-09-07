package com.hardware.shop.service;

import com.hardware.shop.dao.CustomerBalanceDAO;
import com.hardware.shop.dto.CustomerBalanceResponse;

import java.sql.SQLException;

public class CustomerBalanceService {

    private final CustomerBalanceDAO customerBalanceDAO;

    public CustomerBalanceService() {
        this.customerBalanceDAO = new CustomerBalanceDAO();
    }

    public CustomerBalanceResponse getCustomerBalance(int customerId) throws SQLException {

        if (customerId <= 0) {

            throw new IllegalArgumentException("Invalid customer ID.");
        }

        CustomerBalanceResponse balance = customerBalanceDAO.getCustomerBalance(customerId);

        return balance;
    }
}