package com.hardware.shop.model;

public class SalesReturn {

    private long id;
    private String returnNumber;
    private long saleId;
    private Integer customerId;
    private String returnDate;
    private double totalAmount;
    private String refundMethod;
    private String reason;

    public SalesReturn() {
    }

    public SalesReturn(long id, String returnNumber, long saleId, Integer customerId, String returnDate, double totalAmount, String refundMethod, String reason) {
        this.id = id;
        this.returnNumber = returnNumber;
        this.saleId = saleId;
        this.customerId = customerId;
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

    public long getSaleId() {
        return saleId;
    }

    public void setSaleId(long saleId) {
        this.saleId = saleId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
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