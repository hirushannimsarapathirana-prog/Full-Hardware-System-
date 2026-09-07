package com.hardware.shop.service;

import com.hardware.shop.dao.DashboardDAO;
import com.hardware.shop.dto.DashboardResponse;

import java.sql.SQLException;

public class DashboardService {

    private final DashboardDAO dashboardDAO;

    public DashboardService() {
        this.dashboardDAO = new DashboardDAO();
    }

    public DashboardResponse getDashboard() throws SQLException {

        return dashboardDAO.getDashboard();
    }
}