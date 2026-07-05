package com.paytm.dto;

public class AddMoneyRequestDTO {

    public final String merchantGuid;
    public final String merchantOrderId;
    public final String walletName;
    public final String walletGuid;
    public final String payeeEmailId;
    public final String payeePhoneNumber;
    public final String amount;
    public final String currencyCode;
    public final String pgTxnId;
    public final String applyVerifiedUser;
    public final String appliedToNewUsers;

    private AddMoneyRequestDTO(AddMoneyRequestDTOBuilder requestBuilder) {
        this.merchantGuid = requestBuilder.merchantGuid;
        this.merchantOrderId = requestBuilder.merchantOrderId;
        this.walletName = requestBuilder.walletName;
        this.walletGuid = requestBuilder.walletGuid;
        this.payeeEmailId = requestBuilder.payeeEmailId;
        this.payeePhoneNumber = requestBuilder.payeePhoneNumber;
        this.amount = requestBuilder.amount;
        this.currencyCode = requestBuilder.currencyCode;
        this.pgTxnId = requestBuilder.pgTxnId;
        this.applyVerifiedUser = requestBuilder.applyVerifiedUser;
        this.appliedToNewUsers = requestBuilder.appliedToNewUsers;
    }

    public static class AddMoneyRequestDTOBuilder {

        String merchantGuid = "125FD26C-4D98-11E2-B20C-E89A8FF309EA";
        String merchantOrderId = Long.toString(System.currentTimeMillis());
        String walletName = "AutomationTesting";
        String walletGuid = "1BE2E64B-8596-4553-BC30-5687B29EF7A8";
        String payeeEmailId = "sandeep.kumar@paytm.com";
        String payeePhoneNumber;
        String amount;
        String currencyCode = "INR";
        String pgTxnId = Long.toString(System.currentTimeMillis());
        String applyVerifiedUser = "0";
        String appliedToNewUsers = "Y";

        public AddMoneyRequestDTOBuilder setMerchantGuid(String merchantGuid) {
            this.merchantGuid = merchantGuid;
            return this;
        }

        public AddMoneyRequestDTOBuilder setMerchantOrderId(String merchantOrderId) {
            this.merchantOrderId = merchantOrderId;
            return this;
        }

        public AddMoneyRequestDTOBuilder setWalletName(String walletName) {
            this.walletName = walletName;
            return this;
        }

        public AddMoneyRequestDTOBuilder setWalletGuid(String walletGuid) {
            this.walletGuid = walletGuid;
            return this;
        }

        public AddMoneyRequestDTOBuilder setPayeeEmailId(String payeeEmailId) {
            this.payeeEmailId = payeeEmailId;
            return this;
        }

        public AddMoneyRequestDTOBuilder setPayeePhoneNumber(String payeePhoneNumber) {
            this.payeePhoneNumber = payeePhoneNumber;
            return this;
        }

        public AddMoneyRequestDTOBuilder setAmount(String amount) {
            this.amount = amount;
            return this;
        }

        public AddMoneyRequestDTOBuilder setCurrencyCode(String currencyCode) {
            this.currencyCode = currencyCode;
            return this;
        }

        public AddMoneyRequestDTOBuilder setPgTxnId(String pgTxnId) {
            this.pgTxnId = pgTxnId;
            return this;
        }

        public AddMoneyRequestDTOBuilder setApplyVerifiedUser(String applyVerifiedUser) {
            this.applyVerifiedUser = applyVerifiedUser;
            return this;
        }

        public AddMoneyRequestDTOBuilder setAppliedToNewUsers(String appliedToNewUsers) {
            this.appliedToNewUsers = appliedToNewUsers;
            return this;
        }

        public AddMoneyRequestDTO build() {
            return new AddMoneyRequestDTO(this);
        }

    }
}