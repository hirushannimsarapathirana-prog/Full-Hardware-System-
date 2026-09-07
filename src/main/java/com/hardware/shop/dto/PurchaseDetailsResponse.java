package com.hardware.shop.dto;

import com.hardware.shop.model.Purchase;
import com.hardware.shop.model.PurchaseItem;

import java.util.List;

public class PurchaseDetailsResponse {

    private Purchase purchase;
    private List<PurchaseItem> items;

    public PurchaseDetailsResponse() {
    }

    public PurchaseDetailsResponse(Purchase purchase, List<PurchaseItem> items) {
        this.purchase = purchase;
        this.items = items;
    }

    public Purchase getPurchase() {
        return purchase;
    }

    public void setPurchase(Purchase purchase) {
        this.purchase = purchase;
    }

    public List<PurchaseItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseItem> items) {
        this.items = items;
    }
}