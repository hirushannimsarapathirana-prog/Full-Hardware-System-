package com.hardware.shop.dto;

import com.hardware.shop.model.Sale;
import com.hardware.shop.model.SaleItem;

import java.util.List;

public class SaleDetailsResponse {

    private Sale sale;
    private List<SaleItem> items;

    public SaleDetailsResponse() {
    }

    public SaleDetailsResponse(Sale sale, List<SaleItem> items) {
        this.sale = sale;
        this.items = items;
    }

    public Sale getSale() {
        return sale;
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public List<SaleItem> getItems() {
        return items;
    }

    public void setItems(List<SaleItem> items) {
        this.items = items;
    }
}