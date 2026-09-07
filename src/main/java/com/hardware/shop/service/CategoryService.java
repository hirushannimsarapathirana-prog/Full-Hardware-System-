package com.hardware.shop.service;

import com.hardware.shop.dao.CategoryDAO;
import com.hardware.shop.model.Category;

import java.sql.SQLException;
import java.util.List;

public class CategoryService {

    private final CategoryDAO categoryDAO;

    public CategoryService() {
        this.categoryDAO = new CategoryDAO();
    }

    public boolean save(Category category) throws SQLException {
        validate(category);
        return categoryDAO.save(category);
    }

    public List<Category> findAll() throws SQLException {
        return categoryDAO.findAll();
    }

    public Category findById(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Category ID must be greater than 0.");
        }

        return categoryDAO.findById(id);
    }

    public boolean update(Category category) throws SQLException {
        if (category == null || category.getId() <= 0) {
            throw new IllegalArgumentException("Valid category ID is required.");
        }

        validate(category);
        return categoryDAO.update(category);
    }

    public boolean delete(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Category ID must be greater than 0.");
        }

        return categoryDAO.delete(id);
    }

    private void validate(Category category) {

        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null.");
        }

        if (category.getCategoryName() == null || category.getCategoryName().isBlank()) {
            throw new IllegalArgumentException("Category name is required.");
        }
    }
}