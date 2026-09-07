package com.hardware.shop.service;

import com.hardware.shop.dao.SupplierDAO;
import com.hardware.shop.model.Supplier;

import java.sql.SQLException;
import java.util.List;

public class SupplierService {

    private final SupplierDAO supplierDAO;

    public SupplierService() {
        this.supplierDAO = new SupplierDAO();
    }

    public boolean save(Supplier supplier) throws SQLException {
        validate(supplier);
        return supplierDAO.save(supplier);
    }

    public List<Supplier> findAll() throws SQLException {
        return supplierDAO.findAll();
    }

    public Supplier findById(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Supplier ID must be greater than 0.");
        }

        return supplierDAO.findById(id);
    }

    public boolean update(Supplier supplier) throws SQLException {

        if (supplier == null || supplier.getId() <= 0) {
            throw new IllegalArgumentException("Valid supplier ID is required.");
        }

        validate(supplier);

        return supplierDAO.update(supplier);
    }

    public boolean delete(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Supplier ID must be greater than 0.");
        }

        return supplierDAO.delete(id);
    }

    private void validate(Supplier supplier) {

        if (supplier == null) {
            throw new IllegalArgumentException("Supplier cannot be null.");
        }

        if (supplier.getSupplierCode() == null || supplier.getSupplierCode().isBlank()) {

            throw new IllegalArgumentException("Supplier code is required.");
        }

        if (supplier.getSupplierName() == null || supplier.getSupplierName().isBlank()) {

            throw new IllegalArgumentException("Supplier name is required.");
        }
    }
}