package com.hardware.shop.service;

import com.hardware.shop.dao.SupplierBalanceDAO;
import com.hardware.shop.dto.SupplierBalanceResponse;

import java.sql.SQLException;

public class SupplierBalanceService {

    private final SupplierBalanceDAO supplierBalanceDAO;

    public SupplierBalanceService() {
        this.supplierBalanceDAO = new SupplierBalanceDAO();
    }

    public SupplierBalanceResponse getSupplierBalance(int supplierId) throws SQLException {

        if (supplierId <= 0) {

            throw new IllegalArgumentException("Invalid supplier ID.");
        }

        return supplierBalanceDAO.getSupplierBalance(supplierId);
    }
}