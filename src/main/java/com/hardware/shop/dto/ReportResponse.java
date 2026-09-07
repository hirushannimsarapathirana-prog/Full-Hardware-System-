package com.hardware.shop.dto;

import java.util.ArrayList;
import java.util.List;

public class ReportResponse {

    private String reportType;
    private String fromDate;
    private String toDate;

    private double totalSales;
    private double totalPurchases;
    private double totalExpenses;
    private double totalProfit;
    private double totalPaid;
    private double totalDue;

    private double totalStockQuantity;
    private double totalStockValue;
    private int lowStockCount;

    private double totalCreditSales;
    private double totalCustomerPayments;
    private double totalCreditReturns;
    private double totalOutstanding;

    private double totalCreditPurchases;
    private double totalSupplierPayments;
    private double totalPurchaseReturns;
    private double totalSupplierOutstanding;

    // Product-wise Sales totals
    private double totalQuantitySold;
    private double totalGrossSales;
    private double totalDiscount;
    private double totalNetSales;
    private double totalCost;
    private double totalProductProfit;
    private double totalProfitMargin;

    private List<StockReportItem> items = new ArrayList<>();
    private List<CustomerOutstandingItem> customers = new ArrayList<>();
    private List<SupplierOutstandingItem> suppliers = new ArrayList<>();
    private List<ProductSalesItem> productSales = new ArrayList<>();

    public String getReportType() {
        return reportType;
    }

    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public double getTotalSales() {
        return totalSales;
    }

    public void setTotalSales(double totalSales) {
        this.totalSales = totalSales;
    }

    public double getTotalPurchases() {
        return totalPurchases;
    }

    public void setTotalPurchases(double totalPurchases) {
        this.totalPurchases = totalPurchases;
    }

    public double getTotalExpenses() {
        return totalExpenses;
    }

    public void setTotalExpenses(double totalExpenses) {
        this.totalExpenses = totalExpenses;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public double getTotalPaid() {
        return totalPaid;
    }

    public void setTotalPaid(double totalPaid) {
        this.totalPaid = totalPaid;
    }

    public double getTotalDue() {
        return totalDue;
    }

    public void setTotalDue(double totalDue) {
        this.totalDue = totalDue;
    }

    public double getTotalStockQuantity() {
        return totalStockQuantity;
    }

    public void setTotalStockQuantity(double totalStockQuantity) {
        this.totalStockQuantity = totalStockQuantity;
    }

    public double getTotalStockValue() {
        return totalStockValue;
    }

    public void setTotalStockValue(double totalStockValue) {
        this.totalStockValue = totalStockValue;
    }

    public int getLowStockCount() {
        return lowStockCount;
    }

    public void setLowStockCount(int lowStockCount) {
        this.lowStockCount = lowStockCount;
    }

    public double getTotalCreditSales() {
        return totalCreditSales;
    }

    public void setTotalCreditSales(double totalCreditSales) {
        this.totalCreditSales = totalCreditSales;
    }

    public double getTotalCustomerPayments() {
        return totalCustomerPayments;
    }

    public void setTotalCustomerPayments(double totalCustomerPayments) {
        this.totalCustomerPayments = totalCustomerPayments;
    }

    public double getTotalCreditReturns() {
        return totalCreditReturns;
    }

    public void setTotalCreditReturns(double totalCreditReturns) {
        this.totalCreditReturns = totalCreditReturns;
    }

    public double getTotalOutstanding() {
        return totalOutstanding;
    }

    public void setTotalOutstanding(double totalOutstanding) {
        this.totalOutstanding = totalOutstanding;
    }

    public double getTotalCreditPurchases() {
        return totalCreditPurchases;
    }

    public void setTotalCreditPurchases(double totalCreditPurchases) {
        this.totalCreditPurchases = totalCreditPurchases;
    }

    public double getTotalSupplierPayments() {
        return totalSupplierPayments;
    }

    public void setTotalSupplierPayments(double totalSupplierPayments) {
        this.totalSupplierPayments = totalSupplierPayments;
    }

    public double getTotalPurchaseReturns() {
        return totalPurchaseReturns;
    }

    public void setTotalPurchaseReturns(double totalPurchaseReturns) {
        this.totalPurchaseReturns = totalPurchaseReturns;
    }

    public double getTotalSupplierOutstanding() {
        return totalSupplierOutstanding;
    }

    public void setTotalSupplierOutstanding(double totalSupplierOutstanding) {
        this.totalSupplierOutstanding = totalSupplierOutstanding;
    }

    public double getTotalQuantitySold() {
        return totalQuantitySold;
    }

    public void setTotalQuantitySold(double totalQuantitySold) {
        this.totalQuantitySold = totalQuantitySold;
    }

    public double getTotalGrossSales() {
        return totalGrossSales;
    }

    public void setTotalGrossSales(double totalGrossSales) {
        this.totalGrossSales = totalGrossSales;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getTotalNetSales() {
        return totalNetSales;
    }

    public void setTotalNetSales(double totalNetSales) {
        this.totalNetSales = totalNetSales;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public double getTotalProductProfit() {
        return totalProductProfit;
    }

    public void setTotalProductProfit(double totalProductProfit) {
        this.totalProductProfit = totalProductProfit;
    }

    public double getTotalProfitMargin() {
        return totalProfitMargin;
    }

    public void setTotalProfitMargin(double totalProfitMargin) {
        this.totalProfitMargin = totalProfitMargin;
    }

    public List<StockReportItem> getItems() {
        return items;
    }

    public void setItems(List<StockReportItem> items) {
        this.items = items;
    }

    public List<CustomerOutstandingItem> getCustomers() {
        return customers;
    }

    public void setCustomers(List<CustomerOutstandingItem> customers) {
        this.customers = customers;
    }

    public List<SupplierOutstandingItem> getSuppliers() {
        return suppliers;
    }

    public void setSuppliers(List<SupplierOutstandingItem> suppliers) {
        this.suppliers = suppliers;
    }

    public List<ProductSalesItem> getProductSales() {
        return productSales;
    }

    public void setProductSales(List<ProductSalesItem> productSales) {
        this.productSales = productSales;
    }

    // ============================================================
    // STOCK REPORT ITEM
    // ============================================================

    public static class StockReportItem {

        private int productId;
        private String sku;
        private String barcode;
        private String productName;
        private String categoryName;
        private String brandName;
        private String unitName;

        private double quantity;
        private double minimumStock;
        private double purchasePrice;
        private double sellingPrice;
        private double stockValue;

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
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

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }

        public String getUnitName() {
            return unitName;
        }

        public void setUnitName(String unitName) {
            this.unitName = unitName;
        }

        public double getQuantity() {
            return quantity;
        }

        public void setQuantity(double quantity) {
            this.quantity = quantity;
        }

        public double getMinimumStock() {
            return minimumStock;
        }

        public void setMinimumStock(double minimumStock) {
            this.minimumStock = minimumStock;
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

        public double getStockValue() {
            return stockValue;
        }

        public void setStockValue(double stockValue) {
            this.stockValue = stockValue;
        }
    }

    // ============================================================
    // CUSTOMER OUTSTANDING ITEM
    // ============================================================

    public static class CustomerOutstandingItem {

        private int customerId;
        private String customerCode;
        private String customerName;
        private String phone;

        private double creditSales;
        private double payments;
        private double creditReturns;
        private double outstanding;

        public int getCustomerId() {
            return customerId;
        }

        public void setCustomerId(int customerId) {
            this.customerId = customerId;
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

        public double getCreditSales() {
            return creditSales;
        }

        public void setCreditSales(double creditSales) {
            this.creditSales = creditSales;
        }

        public double getPayments() {
            return payments;
        }

        public void setPayments(double payments) {
            this.payments = payments;
        }

        public double getCreditReturns() {
            return creditReturns;
        }

        public void setCreditReturns(double creditReturns) {
            this.creditReturns = creditReturns;
        }

        public double getOutstanding() {
            return outstanding;
        }

        public void setOutstanding(double outstanding) {
            this.outstanding = outstanding;
        }
    }

    // ============================================================
    // SUPPLIER OUTSTANDING ITEM
    // ============================================================

    public static class SupplierOutstandingItem {

        private int supplierId;
        private String supplierCode;
        private String supplierName;
        private String phone;

        private double creditPurchases;
        private double payments;
        private double purchaseReturns;
        private double outstanding;

        public int getSupplierId() {
            return supplierId;
        }

        public void setSupplierId(int supplierId) {
            this.supplierId = supplierId;
        }

        public String getSupplierCode() {
            return supplierCode;
        }

        public void setSupplierCode(String supplierCode) {
            this.supplierCode = supplierCode;
        }

        public String getSupplierName() {
            return supplierName;
        }

        public void setSupplierName(String supplierName) {
            this.supplierName = supplierName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public double getCreditPurchases() {
            return creditPurchases;
        }

        public void setCreditPurchases(double creditPurchases) {
            this.creditPurchases = creditPurchases;
        }

        public double getPayments() {
            return payments;
        }

        public void setPayments(double payments) {
            this.payments = payments;
        }

        public double getPurchaseReturns() {
            return purchaseReturns;
        }

        public void setPurchaseReturns(double purchaseReturns) {
            this.purchaseReturns = purchaseReturns;
        }

        public double getOutstanding() {
            return outstanding;
        }

        public void setOutstanding(double outstanding) {
            this.outstanding = outstanding;
        }
    }

    // ============================================================
    // PRODUCT SALES ITEM
    // ============================================================

    public static class ProductSalesItem {

        private int productId;
        private String sku;
        private String barcode;
        private String productName;
        private String categoryName;
        private String brandName;
        private String unitName;

        private double quantitySold;
        private double grossSales;
        private double discount;
        private double netSales;
        private double cost;
        private double profit;
        private double profitMargin;

        public int getProductId() {
            return productId;
        }

        public void setProductId(int productId) {
            this.productId = productId;
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

        public String getCategoryName() {
            return categoryName;
        }

        public void setCategoryName(String categoryName) {
            this.categoryName = categoryName;
        }

        public String getBrandName() {
            return brandName;
        }

        public void setBrandName(String brandName) {
            this.brandName = brandName;
        }

        public String getUnitName() {
            return unitName;
        }

        public void setUnitName(String unitName) {
            this.unitName = unitName;
        }

        public double getQuantitySold() {
            return quantitySold;
        }

        public void setQuantitySold(double quantitySold) {
            this.quantitySold = quantitySold;
        }

        public double getGrossSales() {
            return grossSales;
        }

        public void setGrossSales(double grossSales) {
            this.grossSales = grossSales;
        }

        public double getDiscount() {
            return discount;
        }

        public void setDiscount(double discount) {
            this.discount = discount;
        }

        public double getNetSales() {
            return netSales;
        }

        public void setNetSales(double netSales) {
            this.netSales = netSales;
        }

        public double getCost() {
            return cost;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public double getProfit() {
            return profit;
        }

        public void setProfit(double profit) {
            this.profit = profit;
        }

        public double getProfitMargin() {
            return profitMargin;
        }

        public void setProfitMargin(double profitMargin) {
            this.profitMargin = profitMargin;
        }
    }
}