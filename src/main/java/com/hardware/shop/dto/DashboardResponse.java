package com.hardware.shop.dto;

public class DashboardResponse {

    private double todaySales;
    private double todayPurchases;
    private double todayExpenses;

    private long totalProducts;
    private long lowStockProducts;

    private double customerOutstanding;
    private double supplierOutstanding;

    private double monthlySales;
    private double monthlyProfit;

    public DashboardResponse() {
    }

    public double getTodaySales() {
        return todaySales;
    }

    public void setTodaySales(double todaySales) {
        this.todaySales = todaySales;
    }

    public double getTodayPurchases() {
        return todayPurchases;
    }

    public void setTodayPurchases(double todayPurchases) {
        this.todayPurchases = todayPurchases;
    }

    public double getTodayExpenses() {
        return todayExpenses;
    }

    public void setTodayExpenses(double todayExpenses) {
        this.todayExpenses = todayExpenses;
    }

    public long getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(long totalProducts) {
        this.totalProducts = totalProducts;
    }

    public long getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(long lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public double getCustomerOutstanding() {
        return customerOutstanding;
    }

    public void setCustomerOutstanding(double customerOutstanding) {
        this.customerOutstanding = customerOutstanding;
    }

    public double getSupplierOutstanding() {
        return supplierOutstanding;
    }

    public void setSupplierOutstanding(double supplierOutstanding) {
        this.supplierOutstanding = supplierOutstanding;
    }

    public double getMonthlySales() {
        return monthlySales;
    }

    public void setMonthlySales(double monthlySales) {
        this.monthlySales = monthlySales;
    }

    public double getMonthlyProfit() {
        return monthlyProfit;
    }

    public void setMonthlyProfit(double monthlyProfit) {
        this.monthlyProfit = monthlyProfit;
    }
}