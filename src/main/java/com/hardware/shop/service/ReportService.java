package com.hardware.shop.service;

import com.hardware.shop.dao.ReportDAO;
import com.hardware.shop.dto.ReportResponse;

import java.sql.SQLException;
import java.time.LocalDate;

public class ReportService {

    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public ReportResponse getDailySales(LocalDate date) throws SQLException {

        if (date == null) {
            throw new IllegalArgumentException("Date is required.");
        }

        return reportDAO.getDailySales(date);
    }

    public ReportResponse getMonthlySales(int year, int month) throws SQLException {

        if (year < 2000 || year > 2100) {

            throw new IllegalArgumentException("Invalid year.");
        }

        if (month < 1 || month > 12) {

            throw new IllegalArgumentException("Invalid month.");
        }

        return reportDAO.getMonthlySales(year, month);
    }

    public ReportResponse getProfit(LocalDate fromDate, LocalDate toDate) throws SQLException {

        validateDateRange(fromDate, toDate);

        return reportDAO.getProfit(fromDate, toDate);
    }

    public ReportResponse getPurchases(LocalDate fromDate, LocalDate toDate) throws SQLException {

        validateDateRange(fromDate, toDate);

        return reportDAO.getPurchases(fromDate, toDate);
    }

    public ReportResponse getProductSales(LocalDate fromDate, LocalDate toDate) throws SQLException {

        validateDateRange(fromDate, toDate);

        return reportDAO.getProductSales(fromDate, toDate);
    }

    public ReportResponse getStock() throws SQLException {

        return reportDAO.getStock();
    }

    public ReportResponse getLowStock() throws SQLException {

        return reportDAO.getLowStock();
    }

    public ReportResponse getCustomerOutstanding() throws SQLException {

        return reportDAO.getCustomerOutstanding();
    }

    public ReportResponse getSupplierOutstanding() throws SQLException {

        return reportDAO.getSupplierOutstanding();
    }

    private void validateDateRange(LocalDate fromDate, LocalDate toDate) {

        if (fromDate == null || toDate == null) {

            throw new IllegalArgumentException("From date and to date are required.");
        }

        if (fromDate.isAfter(toDate)) {

            throw new IllegalArgumentException("From date cannot be after to date.");
        }
    }
}