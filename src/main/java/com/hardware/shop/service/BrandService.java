package com.hardware.shop.service;

import com.hardware.shop.dao.BrandDAO;
import com.hardware.shop.model.Brand;

import java.sql.SQLException;
import java.util.List;

public class BrandService {

    private final BrandDAO brandDAO;

    public BrandService() {
        this.brandDAO = new BrandDAO();
    }

    public boolean save(Brand brand) throws SQLException {
        validate(brand);
        return brandDAO.save(brand);
    }

    public List<Brand> findAll() throws SQLException {
        return brandDAO.findAll();
    }

    public Brand findById(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Brand ID must be greater than 0.");
        }

        return brandDAO.findById(id);
    }

    public boolean update(Brand brand) throws SQLException {

        if (brand == null || brand.getId() <= 0) {
            throw new IllegalArgumentException("Valid brand ID is required.");
        }

        validate(brand);

        return brandDAO.update(brand);
    }

    public boolean delete(int id) throws SQLException {

        if (id <= 0) {
            throw new IllegalArgumentException("Brand ID must be greater than 0.");
        }

        return brandDAO.delete(id);
    }

    private void validate(Brand brand) {

        if (brand == null) {
            throw new IllegalArgumentException("Brand cannot be null.");
        }

        if (brand.getBrandName() == null || brand.getBrandName().isBlank()) {

            throw new IllegalArgumentException("Brand name is required.");
        }
    }
}