package com.example.diplomathesisfinalcollection.dto;

import java.util.List;

public class CheckoutRequest {
    private List<CartItemRequest> items;

    public List<CartItemRequest> getItems() { return items; }
    public void setItems(List<CartItemRequest> items) { this.items = items; }
}
