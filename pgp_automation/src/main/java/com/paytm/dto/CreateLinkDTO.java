package com.paytm.dto;

public class CreateLinkDTO {
    private String mid;
    private String linkName;
    private String linkType;
    private String expiryDate;
    private String dueDate;
    private Double penaltyFee;
    private Double amount;
    private String invoiceId;

    public String getMid() {
        return mid;
    }

    public CreateLinkDTO setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public String getLinkName() {
        return linkName;
    }

    public CreateLinkDTO setLinkName(String linkName) {
        this.linkName = linkName;
        return this;
    }

    public String getLinkType() {
        return linkType;
    }

    public CreateLinkDTO setLinkType(String linkType) {
        this.linkType = linkType;
        return this;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public CreateLinkDTO setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
        return this;
    }

    public String getDueDate() {
        return dueDate;
    }

    public CreateLinkDTO setDueDate(String dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    public Double getPenaltyFee() {
        return penaltyFee;
    }

    public CreateLinkDTO setPenaltyFee(Double penaltyFee) {
        this.penaltyFee = penaltyFee;
        return this;
    }

    public Double getAmount() {
        return amount;
    }

    public CreateLinkDTO setAmount(Double amount) {
        this.amount = amount;
        return this;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public CreateLinkDTO setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
        return this;
    }
}
