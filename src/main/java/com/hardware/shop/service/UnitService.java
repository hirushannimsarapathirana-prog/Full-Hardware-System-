package com.hardware.shop.service;

import com.hardware.shop.dao.UnitDAO;
import com.hardware.shop.model.Unit;

import java.sql.SQLException;
import java.util.List;

public class UnitService {

    private final UnitDAO unitDAO;

    public UnitService() {
        this.unitDAO = new UnitDAO();
    }

    public boolean save(Unit unit) throws SQLException {
        validate(unit);
        return unitDAO.save(unit);
    }

    public List<Unit> findAll() throws SQLException {
        return unitDAO.findAll();
    }

    public Unit findById(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Unit ID must be greater than 0.");
        }

        return unitDAO.findById(id);
    }

    public boolean update(Unit unit) throws SQLException {
        if (unit == null || unit.getId() <= 0) {
            throw new IllegalArgumentException("Valid unit ID is required.");
        }

        validate(unit);
        return unitDAO.update(unit);
    }

    public boolean delete(int id) throws SQLException {
        if (id <= 0) {
            throw new IllegalArgumentException("Unit ID must be greater than 0.");
        }

        return unitDAO.delete(id);
    }

    private void validate(Unit unit) {

        if (unit == null) {
            throw new IllegalArgumentException("Unit cannot be null.");
        }

        if (unit.getUnitName() == null || unit.getUnitName().isBlank()) {
            throw new IllegalArgumentException("Unit name is required.");
        }

        if (unit.getShortName() == null || unit.getShortName().isBlank()) {
            throw new IllegalArgumentException("Short name is required.");
        }
    }
}