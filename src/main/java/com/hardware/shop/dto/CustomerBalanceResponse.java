package com.hardware.shop.dto;

public class CustomerBalanceResponse {

    private int customerId;
    private double totalCreditSales;
    private double totalPayments;
    private double outstandingBalance;

    public CustomerBalanceResponse() {
    }

    public CustomerBalanceResponse(int customerId, double totalCreditSales, double totalPayments, double outstandingBalance) {
        this.customerId = customerId;
        this.totalCreditSales = totalCreditSales;
        this.totalPayments = totalPayments;
        this.outstandingBalance = outstandingBalance;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public double getTotalCreditSales() {
        return totalCreditSales;
    }

    public void setTotalCreditSales(double totalCreditSales) {
        this.totalCreditSales = totalCreditSales;
    }

    public double getTotalPayments() {
        return totalPayments;
    }

    public void setTotalPayments(double totalPayments) {
        this.totalPayments = totalPayments;
    }

    public double getOutstandingBalance() {
        return outstandingBalance;
    }

    public void setOutstandingBalance(double outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }
}