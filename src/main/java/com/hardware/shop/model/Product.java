package com.hardware.shop.model;

public class Product {

    private int id;
    private String sku;
    private String barcode;
    private String productName;
    private int categoryId;
    private Integer brandId;
    private int unitId;
    private double purchasePrice;
    private double sellingPrice;
    private double wholesalePrice;
    private double minimumStock;
    private String description;
    private String status;

    public Product() {
    }

    public Product(int id, String sku, String barcode, String productName, int categoryId, Integer brandId, int unitId, double purchasePrice, double sellingPrice, double wholesalePrice, double minimumStock, String description, String status) {
        this.id = id;
        this.sku = sku;
        this.barcode = barcode;
        this.productName = productName;
        this.categoryId = categoryId;
        this.brandId = brandId;
        this.unitId = unitId;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.wholesalePrice = wholesalePrice;
        this.minimumStock = minimumStock;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }

    public int getUnitId() {
        return unitId;
    }

    public void setUnitId(int unitId) {
        this.unitId = unitId;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public void setPurchasePrice(double purchasePrice) {
        this.purchasePrice = purchasePrice;
    }

    public double getSellingPrice() {
        return sellingPrice;
    }

    public void setSellingPrice(double sellingPrice) {
        this.sellingPrice = sellingPrice;
    }

    public double getWholesalePrice() {
        return wholesalePrice;
    }

    public void setWholesalePrice(double wholesalePrice) {
        this.wholesalePrice = wholesalePrice;
    }

    public double getMinimumStock() {
        return minimumStock;
    }

    public void setMinimumStock(double minimumStock) {
        this.minimumStock = minimumStock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}