package com.paytm.dto;

public class AddMoneyDTO {

    public final AddMoneyRequestDTO request;
    public final String metadata;
    public final String ipAddress;
    public final String platformName;
    public final String operationType;

    private AddMoneyDTO(AddMoneyDTOBuilder addMoneyDTOBuilder) {
        this.request = addMoneyDTOBuilder.request;
        this.metadata = addMoneyDTOBuilder.metadata;
        this.ipAddress = addMoneyDTOBuilder.ipAddress;
        this.platformName = addMoneyDTOBuilder.platformName;
        this.operationType = addMoneyDTOBuilder.operationType;
    }

    public static class AddMoneyDTOBuilder {
        AddMoneyRequestDTO request;
        String metadata = "AutomationTesting";
        String ipAddress = "127.0.0.1";
        String platformName = "PayTM";
        String operationType = "ADD_MONEY_CASH_TXN";


        public AddMoneyDTOBuilder setRequest(AddMoneyRequestDTO request) {
            this.request = request;
            return this;
        }

        public AddMoneyDTOBuilder setMetadata(String metadata) {
            this.metadata = metadata;
            return this;
        }

        public AddMoneyDTOBuilder setIpAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public AddMoneyDTOBuilder setPlatformName(String platformName) {
            this.platformName = platformName;
            return this;
        }

        public AddMoneyDTOBuilder setOperationType(String operationType) {
            this.operationType = operationType;
            return this;
        }

        public AddMoneyDTO build() {
            return new AddMoneyDTO(this);
        }
    }

}
