package com.paytm.api.theia.OfferWidgetJS;

import java.util.Map;

public class OfferWidgetDiscovery {

    private String root;
    private Map<String, Object> data;
    private Map<String, Object> merchant;

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getMerchant() {
        return merchant;
    }

    public void setMerchant(Map<String, Object> merchant) {
        this.merchant = merchant;
    }
}