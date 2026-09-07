package com.hardware.shop.dto;

import com.hardware.shop.model.PurchaseReturn;
import com.hardware.shop.model.PurchaseReturnItem;

import java.util.List;

public class PurchaseReturnDetailsResponse {

    private PurchaseReturn purchaseReturn;
    private List<PurchaseReturnItem> items;

    public PurchaseReturnDetailsResponse() {
    }

    public PurchaseReturnDetailsResponse(PurchaseReturn purchaseReturn, List<PurchaseReturnItem> items) {
        this.purchaseReturn = purchaseReturn;
        this.items = items;
    }

    public PurchaseReturn getPurchaseReturn() {
        return purchaseReturn;
    }

    public void setPurchaseReturn(PurchaseReturn purchaseReturn) {
        this.purchaseReturn = purchaseReturn;
    }

    public List<PurchaseReturnItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseReturnItem> items) {
        this.items = items;
    }
}