package com.hardware.shop.dto;

public class SupplierBalanceResponse {

    private int supplierId;
    private double totalCreditPurchases;
    private double totalPayments;
    private double outstandingBalance;

    public SupplierBalanceResponse() {
    }

    public SupplierBalanceResponse(int supplierId, double totalCreditPurchases, double totalPayments, double outstandingBalance) {
        this.supplierId = supplierId;
        this.totalCreditPurchases = totalCreditPurchases;
        this.totalPayments = totalPayments;
        this.outstandingBalance = outstandingBalance;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public double getTotalCreditPurchases() {
        return totalCreditPurchases;
    }

    public void setTotalCreditPurchases(double totalCreditPurchases) {
        this.totalCreditPurchases = totalCreditPurchases;
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