package com.hardware.shop.service;

import com.hardware.shop.dao.ProductDAO;
import com.hardware.shop.model.Product;
import java.sql.SQLException;
import java.util.List;

public class ProductService {

    private final ProductDAO productDAO;

    public ProductService() {
        this.productDAO = new ProductDAO();
    }

    public boolean save(Product product) throws SQLException {
        validate(product);
        return productDAO.save(product);
    }

    public List<Product> findAll() throws SQLException {
        return productDAO.findAll();
    }

    public Product findById(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0.");
        }
        return productDAO.findById(id);
    }

    public boolean update(Product product) throws SQLException {
        if (product.getId() <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0.");
        }
        validate(product);
        return productDAO.update(product);
    }

    public boolean delete(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Product ID must be greater than 0.");
        }
        return productDAO.delete(id);
    }

    private void validate(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }

        if (product.getSku() == null || product.getSku().isBlank()) {
            throw new IllegalArgumentException("SKU is required.");
        }

        if (product.getProductName() == null || product.getProductName().isBlank()) {
            throw new IllegalArgumentException("Product name is required.");
        }

        if (product.getCategoryId() <= 0) {
            throw new IllegalArgumentException("Valid category is required.");
        }

        if (product.getUnitId() <= 0) {
            throw new IllegalArgumentException("Valid unit is required.");
        }

        if (product.getPurchasePrice() < 0) {
            throw new IllegalArgumentException("Purchase price cannot be negative.");
        }

        if (product.getSellingPrice() < 0) {
            throw new IllegalArgumentException("Selling price cannot be negative.");
        }

        if (product.getWholesalePrice() < 0) {
            throw new IllegalArgumentException("Wholesale price cannot be negative.");
        }

        if (product.getMinimumStock() < 0) {
            throw new IllegalArgumentException("Minimum stock cannot be negative.");
        }
    }
}