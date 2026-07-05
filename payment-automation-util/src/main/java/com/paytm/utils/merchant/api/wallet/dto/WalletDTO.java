package com.paytm.utils.merchant.api.wallet.dto;

public class WalletDTO {

    private RequestDTO request;
    private String metadata;
    private String ipAddress;
    private String platformName;
    private String operationType;

    // Getter Methods

    public RequestDTO getRequest() {
        return request;
    }

    public String getMetadata() {
        return metadata;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getOperationType() {
        return operationType;
    }

    // Setter Methods

    public WalletDTO setRequest(RequestDTO request) {
        this.request = request;
        return this;
    }

    public WalletDTO setMetadata(String metadata) {
        this.metadata = metadata;
        return this;
    }

    public WalletDTO setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    public WalletDTO setPlatformName(String platformName) {
        this.platformName = platformName;
        return this;
    }

    public WalletDTO setOperationType(String operationType) {
        this.operationType = operationType;
        return this;
    }

}
