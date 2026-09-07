package com.hardware.shop.dto;

import com.hardware.shop.model.SalesReturn;
import com.hardware.shop.model.SalesReturnItem;

import java.util.List;

public class SalesReturnDetailsResponse {

    private SalesReturn salesReturn;
    private List<SalesReturnItem> items;

    public SalesReturnDetailsResponse() {
    }

    public SalesReturnDetailsResponse(SalesReturn salesReturn, List<SalesReturnItem> items) {
        this.salesReturn = salesReturn;
        this.items = items;
    }

    public SalesReturn getSalesReturn() {
        return salesReturn;
    }

    public void setSalesReturn(SalesReturn salesReturn) {
        this.salesReturn = salesReturn;
    }

    public List<SalesReturnItem> getItems() {
        return items;
    }

    public void setItems(List<SalesReturnItem> items) {
        this.items = items;
    }
}