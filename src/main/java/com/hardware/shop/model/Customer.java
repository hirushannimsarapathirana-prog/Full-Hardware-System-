package com.hardware.shop.model;

public class Customer {

    private int id;
    private String customerCode;
    private String customerName;
    private String phone;
    private String email;
    private String address;
    private double creditLimit;
    private String status;

    public Customer() {
    }

    public Customer(int id, String customerCode, String customerName, String phone, String email, String address, double creditLimit, String status) {
        this.id = id;
        this.customerCode = customerCode;
        this.customerName = customerName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.creditLimit = creditLimit;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public double getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(double creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}