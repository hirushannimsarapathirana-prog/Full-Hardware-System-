package com.hardware.shop.dao;

import com.hardware.shop.config.DBConnection;
import com.hardware.shop.dto.ReportResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class ReportDAO {

    // ============================================================
    // DAILY SALES
    // ============================================================

    public ReportResponse getDailySales(LocalDate date) throws SQLException {

        String sql = """
                SELECT COALESCE(SUM(total_amount), 0) AS total_sales
                FROM sales
                WHERE DATE(sale_date) = ?
                AND sale_status = 'COMPLETED'
                """;

        ReportResponse response = new ReportResponse();

        response.setReportType("DAILY_SALES");
        response.setFromDate(date.toString());
        response.setToDate(date.toString());

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(date));

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    response.setTotalSales(rs.getDouble("total_sales"));
                }
            }
        }

        return response;
    }

    // ============================================================
    // MONTHLY SALES
    // ============================================================

    public ReportResponse getMonthlySales(int year, int month) throws SQLException {

        String sql = """
                SELECT COALESCE(SUM(total_amount), 0)
                    AS total_sales
                FROM sales
                WHERE YEAR(sale_date) = ?
                AND MONTH(sale_date) = ?
                AND sale_status = 'COMPLETED'
                """;

        ReportResponse response = new ReportResponse();

        response.setReportType("MONTHLY_SALES");

        response.setFromDate(String.format("%04d-%02d-01", year, month));

        response.setToDate(String.format("%04d-%02d", year, month));

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, year);
            statement.setInt(2, month);

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    response.setTotalSales(rs.getDouble("total_sales"));
                }
            }
        }

        return response;
    }

    // ============================================================
    // PROFIT
    // ============================================================

    public ReportResponse getProfit(LocalDate fromDate, LocalDate toDate) throws SQLException {

        ReportResponse response = new ReportResponse();

        response.setReportType("PROFIT");
        response.setFromDate(fromDate.toString());
        response.setToDate(toDate.toString());

        String salesSql = """
                SELECT
                    COALESCE(SUM(si.total_price), 0)
                        AS sales_total,
                
                    COALESCE(
                        SUM(
                            si.quantity * p.purchase_price
                        ),
                        0
                    ) AS cost_total
                
                FROM sale_items si
                
                INNER JOIN sales s
                    ON s.id = si.sale_id
                
                INNER JOIN products p
                    ON p.id = si.product_id
                
                WHERE DATE(s.sale_date)
                    BETWEEN ? AND ?
                
                AND s.sale_status = 'COMPLETED'
                """;

        String expenseSql = """
                SELECT COALESCE(SUM(amount), 0)
                    AS expense_total
                FROM expenses
                WHERE DATE(expense_date)
                    BETWEEN ? AND ?
                """;

        try (Connection connection = DBConnection.getConnection()) {

            double salesTotal = 0;
            double costTotal = 0;
            double expenseTotal = 0;

            try (PreparedStatement statement = connection.prepareStatement(salesSql)) {

                statement.setDate(1, java.sql.Date.valueOf(fromDate));

                statement.setDate(2, java.sql.Date.valueOf(toDate));

                try (ResultSet rs = statement.executeQuery()) {

                    if (rs.next()) {

                        salesTotal = rs.getDouble("sales_total");

                        costTotal = rs.getDouble("cost_total");
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(expenseSql)) {

                statement.setDate(1, java.sql.Date.valueOf(fromDate));

                statement.setDate(2, java.sql.Date.valueOf(toDate));

                try (ResultSet rs = statement.executeQuery()) {

                    if (rs.next()) {

                        expenseTotal = rs.getDouble("expense_total");
                    }
                }
            }

            response.setTotalSales(salesTotal);
            response.setTotalExpenses(expenseTotal);

            response.setTotalProfit(salesTotal - costTotal - expenseTotal);
        }

        return response;
    }

    // ============================================================
    // PURCHASES
    // ============================================================

    public ReportResponse getPurchases(LocalDate fromDate, LocalDate toDate) throws SQLException {

        String sql = """
                SELECT
                    COALESCE(SUM(total_amount), 0)
                        AS total_purchases,
                
                    COALESCE(SUM(paid_amount), 0)
                        AS paid_amount,
                
                    COALESCE(SUM(due_amount), 0)
                        AS due_amount
                
                FROM purchases
                
                WHERE DATE(purchase_date)
                    BETWEEN ? AND ?
                """;

        ReportResponse response = new ReportResponse();

        response.setReportType("PURCHASES");
        response.setFromDate(fromDate.toString());
        response.setToDate(toDate.toString());

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(fromDate));

            statement.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = statement.executeQuery()) {

                if (rs.next()) {

                    response.setTotalPurchases(rs.getDouble("total_purchases"));

                    response.setTotalPaid(rs.getDouble("paid_amount"));

                    response.setTotalDue(rs.getDouble("due_amount"));
                }
            }
        }

        return response;
    }

    // ============================================================
    // PRODUCT-WISE SALES
    // ============================================================

    public ReportResponse getProductSales(LocalDate fromDate, LocalDate toDate) throws SQLException {

        String sql = """
                SELECT
                
                    p.id AS product_id,
                    p.sku,
                    p.barcode,
                    p.product_name,
                
                    c.category_name,
                    b.brand_name,
                    u.unit_name,
                
                    COALESCE(
                        SUM(si.quantity),
                        0
                    ) AS quantity_sold,
                
                    COALESCE(
                        SUM(
                            si.quantity * si.unit_price
                        ),
                        0
                    ) AS gross_sales,
                
                    COALESCE(
                        SUM(si.discount),
                        0
                    ) AS discount,
                
                    COALESCE(
                        SUM(si.total_price),
                        0
                    ) AS net_sales,
                
                    COALESCE(
                        SUM(
                            si.quantity * p.purchase_price
                        ),
                        0
                    ) AS cost
                
                FROM sale_items si
                
                INNER JOIN sales s
                    ON s.id = si.sale_id
                
                INNER JOIN products p
                    ON p.id = si.product_id
                
                LEFT JOIN categories c
                    ON c.id = p.category_id
                
                LEFT JOIN brands b
                    ON b.id = p.brand_id
                
                LEFT JOIN units u
                    ON u.id = p.unit_id
                
                WHERE DATE(s.sale_date)
                    BETWEEN ? AND ?
                
                AND s.sale_status = 'COMPLETED'
                
                GROUP BY
                    p.id,
                    p.sku,
                    p.barcode,
                    p.product_name,
                    c.category_name,
                    b.brand_name,
                    u.unit_name
                
                ORDER BY
                    net_sales DESC,
                    p.product_name ASC
                """;

        ReportResponse response = new ReportResponse();

        response.setReportType("PRODUCT_SALES");
        response.setFromDate(fromDate.toString());
        response.setToDate(toDate.toString());

        double totalQuantitySold = 0;
        double totalGrossSales = 0;
        double totalDiscount = 0;
        double totalNetSales = 0;
        double totalCost = 0;
        double totalProfit = 0;

        try (Connection connection = DBConnection.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(fromDate));

            statement.setDate(2, java.sql.Date.valueOf(toDate));

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {

                    double quantitySold = rs.getDouble("quantity_sold");

                    double grossSales = rs.getDouble("gross_sales");

                    double discount = rs.getDouble("discount");

                    double netSales = rs.getDouble("net_sales");

                    double cost = rs.getDouble("cost");

                    double profit = netSales - cost;

                    double profitMargin = 0;

                    if (netSales > 0) {

                        profitMargin = (profit / netSales) * 100;
                    }

                    ReportResponse.ProductSalesItem item = new ReportResponse.ProductSalesItem();

                    item.setProductId(rs.getInt("product_id"));

                    item.setSku(rs.getString("sku"));

                    item.setBarcode(rs.getString("barcode"));

                    item.setProductName(rs.getString("product_name"));

                    item.setCategoryName(rs.getString("category_name"));

                    item.setBrandName(rs.getString("brand_name"));

                    item.setUnitName(rs.getString("unit_name"));

                    item.setQuantitySold(quantitySold);

                    item.setGrossSales(grossSales);

                    item.setDiscount(discount);

                    item.setNetSales(netSales);

                    item.setCost(cost);

                    item.setProfit(profit);

                    item.setProfitMargin(profitMargin);

                    response.getProductSales().add(item);

                    totalQuantitySold += quantitySold;
                    totalGrossSales += grossSales;
                    totalDiscount += discount;
                    totalNetSales += netSales;
                    totalCost += cost;
                    totalProfit += profit;
                }
            }
        }

        double totalProfitMargin = 0;

        if (totalNetSales > 0) {

            totalProfitMargin = (totalProfit / totalNetSales) * 100;
        }

        response.setTotalQuantitySold(totalQuantitySold);

        response.setTotalGrossSales(totalGrossSales);

        response.setTotalDiscount(totalDiscount);

        response.setTotalNetSales(totalNetSales);

        response.setTotalCost(totalCost);

        response.setTotalProductProfit(totalProfit);

        response.setTotalProfitMargin(totalProfitMargin);

        return response;
    }

    // ============================================================
    // STOCK
    // ============================================================

    public ReportResponse getStock() throws SQLException {

        return buildStockResponse(stockSql(false), "STOCK");
    }

    // ============================================================
    // LOW STOCK
    // ============================================================

    public ReportResponse getLowStock() throws SQLException {

        return buildStockResponse(stockSql(true), "LOW_STOCK");
    }

    private String stockSql(boolean lowStock) {

        String condition = lowStock ? """
                AND COALESCE(ps.quantity, 0)
                    <= p.minimum_stock
                """ : "";

        return """
                SELECT
                
                    p.id AS product_id,
                    p.sku,
                    p.barcode,
                    p.product_name,
                
                    c.category_name,
                    b.brand_name,
                    u.unit_name,
                
                    COALESCE(ps.quantity, 0)
                        AS quantity,
                
                    p.minimum_stock,
                    p.purchase_price,
                    p.selling_price,
                
                    (
                        COALESCE(ps.quantity, 0)
                        * p.purchase_price
                    ) AS stock_value
                
                FROM products p
                
                LEFT JOIN product_stock ps
                    ON ps.product_id = p.id
                
                LEFT JOIN categories c
                    ON c.id = p.category_id
                
                LEFT JOIN brands b
                    ON b.id = p.brand_id
                
                LEFT JOIN units u
                    ON u.id = p.unit_id
                
                WHERE p.status = 'ACTIVE'
                """ + condition + """
                ORDER BY
                    quantity ASC,
                    p.product_name ASC
                """;
    }

    private ReportResponse buildStockResponse(String sql, String reportType) throws SQLException {

        ReportResponse response = new ReportResponse();

        response.setReportType(reportType);

        double totalQuantity = 0;
        double totalValue = 0;
        int count = 0;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                ReportResponse.StockReportItem item = new ReportResponse.StockReportItem();

                item.setProductId(rs.getInt("product_id"));

                item.setSku(rs.getString("sku"));

                item.setBarcode(rs.getString("barcode"));

                item.setProductName(rs.getString("product_name"));

                item.setCategoryName(rs.getString("category_name"));

                item.setBrandName(rs.getString("brand_name"));

                item.setUnitName(rs.getString("unit_name"));

                item.setQuantity(rs.getDouble("quantity"));

                item.setMinimumStock(rs.getDouble("minimum_stock"));

                item.setPurchasePrice(rs.getDouble("purchase_price"));

                item.setSellingPrice(rs.getDouble("selling_price"));

                item.setStockValue(rs.getDouble("stock_value"));

                response.getItems().add(item);

                totalQuantity += rs.getDouble("quantity");

                totalValue += rs.getDouble("stock_value");

                count++;
            }
        }

        response.setTotalStockQuantity(totalQuantity);

        response.setTotalStockValue(totalValue);

        if ("LOW_STOCK".equals(reportType)) {

            response.setLowStockCount(count);
        }

        return response;
    }

    // ============================================================
    // CUSTOMER OUTSTANDING
    // ============================================================

    public ReportResponse getCustomerOutstanding() throws SQLException {

        String sql = """
                SELECT
                
                    c.id AS customer_id,
                    c.customer_code,
                    c.customer_name,
                    c.phone,
                
                    COALESCE(
                        (
                            SELECT SUM(s.total_amount)
                            FROM sales s
                
                            WHERE s.customer_id = c.id
                
                            AND s.payment_status IN
                                ('CREDIT', 'PARTIAL')
                
                            AND s.sale_status = 'COMPLETED'
                        ),
                        0
                    ) AS credit_sales,
                
                    COALESCE(
                        (
                            SELECT SUM(cp.amount)
                            FROM customer_payments cp
                
                            WHERE cp.customer_id = c.id
                        ),
                        0
                    ) AS payments,
                
                    COALESCE(
                        (
                            SELECT SUM(sr.total_amount)
                            FROM sales_returns sr
                
                            WHERE sr.customer_id = c.id
                
                            AND sr.refund_method = 'CREDIT'
                        ),
                        0
                    ) AS credit_returns
                
                FROM customers c
                
                WHERE c.status = 'ACTIVE'
                
                ORDER BY c.customer_name
                """;

        ReportResponse response = new ReportResponse();

        response.setReportType("CUSTOMER_OUTSTANDING");

        double totalCreditSales = 0;
        double totalPayments = 0;
        double totalCreditReturns = 0;
        double totalOutstanding = 0;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                double creditSales = rs.getDouble("credit_sales");

                double payments = rs.getDouble("payments");

                double creditReturns = rs.getDouble("credit_returns");

                double outstanding = creditSales - payments - creditReturns;

                totalCreditSales += creditSales;
                totalPayments += payments;
                totalCreditReturns += creditReturns;

                if (outstanding <= 0) {
                    continue;
                }

                ReportResponse.CustomerOutstandingItem item = new ReportResponse.CustomerOutstandingItem();

                item.setCustomerId(rs.getInt("customer_id"));

                item.setCustomerCode(rs.getString("customer_code"));

                item.setCustomerName(rs.getString("customer_name"));

                item.setPhone(rs.getString("phone"));

                item.setCreditSales(creditSales);

                item.setPayments(payments);

                item.setCreditReturns(creditReturns);

                item.setOutstanding(outstanding);

                response.getCustomers().add(item);

                totalOutstanding += outstanding;
            }
        }

        response.setTotalCreditSales(totalCreditSales);

        response.setTotalCustomerPayments(totalPayments);

        response.setTotalCreditReturns(totalCreditReturns);

        response.setTotalOutstanding(totalOutstanding);

        return response;
    }

    // ============================================================
    // SUPPLIER OUTSTANDING
    // ============================================================

    public ReportResponse getSupplierOutstanding() throws SQLException {

        String sql = """
                SELECT
                
                    s.id AS supplier_id,
                    s.supplier_code,
                    s.supplier_name,
                    s.phone,
                
                    COALESCE(
                        (
                            SELECT SUM(p.due_amount)
                            FROM purchases p
                
                            WHERE p.supplier_id = s.id
                
                            AND p.payment_status IN
                                ('CREDIT', 'PARTIAL')
                        ),
                        0
                    ) AS credit_purchases_due,
                
                    COALESCE(
                        (
                            SELECT SUM(sp.amount)
                            FROM supplier_payments sp
                
                            WHERE sp.supplier_id = s.id
                        ),
                        0
                    ) AS supplier_payments,
                
                    COALESCE(
                        (
                            SELECT SUM(pr.total_amount)
                            FROM purchase_returns pr
                
                            WHERE pr.supplier_id = s.id
                
                            AND pr.refund_method = 'CREDIT'
                        ),
                        0
                    ) AS purchase_returns
                
                FROM suppliers s
                
                WHERE s.status = 'ACTIVE'
                
                ORDER BY s.supplier_name
                """;

        ReportResponse response = new ReportResponse();

        response.setReportType("SUPPLIER_OUTSTANDING");

        double totalCreditPurchases = 0;
        double totalPayments = 0;
        double totalReturns = 0;
        double totalOutstanding = 0;

        try (Connection connection = DBConnection.getConnection();

             PreparedStatement statement = connection.prepareStatement(sql);

             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {

                double creditPurchases = rs.getDouble("credit_purchases_due");

                double payments = rs.getDouble("supplier_payments");

                double returns = rs.getDouble("purchase_returns");

                double outstanding = creditPurchases - payments - returns;

                totalCreditPurchases += creditPurchases;

                totalPayments += payments;

                totalReturns += returns;

                if (outstanding <= 0) {
                    continue;
                }

                ReportResponse.SupplierOutstandingItem item = new ReportResponse.SupplierOutstandingItem();

                item.setSupplierId(rs.getInt("supplier_id"));

                item.setSupplierCode(rs.getString("supplier_code"));

                item.setSupplierName(rs.getString("supplier_name"));

                item.setPhone(rs.getString("phone"));

                item.setCreditPurchases(creditPurchases);

                item.setPayments(payments);

                item.setPurchaseReturns(returns);

                item.setOutstanding(outstanding);

                response.getSuppliers().add(item);

                totalOutstanding += outstanding;
            }
        }

        response.setTotalCreditPurchases(totalCreditPurchases);

        response.setTotalSupplierPayments(totalPayments);

        response.setTotalPurchaseReturns(totalReturns);

        response.setTotalSupplierOutstanding(totalOutstanding);

        return response;
    }
}