package com.hardware.shop.model;

public class PurchaseReturn {

    private long id;
    private String returnNumber;
    private long purchaseId;
    private int supplierId;
    private String returnDate;
    private double totalAmount;
    private String refundMethod;
    private String reason;

    public PurchaseReturn() {
    }

    public PurchaseReturn(long id, String returnNumber, long purchaseId, int supplierId, String returnDate, double totalAmount, String refundMethod, String reason) {
        this.id = id;
        this.returnNumber = returnNumber;
        this.purchaseId = purchaseId;
        this.supplierId = supplierId;
        this.returnDate = returnDate;
        this.totalAmount = totalAmount;
        this.refundMethod = refundMethod;
        this.reason = reason;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(String returnNumber) {
        this.returnNumber = returnNumber;
    }

    public long getPurchaseId() {
        return purchaseId;
    }

    public void setPurchaseId(long purchaseId) {
        this.purchaseId = purchaseId;
    }

    public int getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(int supplierId) {
        this.supplierId = supplierId;
    }

    public String getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(String returnDate) {
        this.returnDate = returnDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getRefundMethod() {
        return refundMethod;
    }

    public void setRefundMethod(String refundMethod) {
        this.refundMethod = refundMethod;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}