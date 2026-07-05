package com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO;

public class promoContext {
     public String cart;

    public String getCart() {
        return cart;
    }

    public void setCart(String id, String brandId, String categoryId , String price) {
        String cart = "{\"items\":{\"{$id\":{\"price\":$price,\"product\":{\"id\":$id,\"brand_id\":$brandId,\"category_ids\":[$categoryId]}}}}";
        cart = cart.replace("$price", price).replace("$id", id)
                .replace("$brandId", brandId).replace("$categoryId", categoryId);
        this.cart = cart;
    }
}
