package com.paytm.utils.merchant.api.wallet.dto;

public class RequestDTO {
    private String adminGuid;
    private float adminId;
    private String amount;
    private String appliedToNewUsers;
    private String applyVerifiedUser;
    private String bankGateway;
    private String blockingAmount;
    private String blockingReason;
    private String currencyCode;
    private String custId;
    private String industryType;
    private String merchantGuid;
    private String merchantOrderId;
    private String payeeEmailId;
    private String payeePhoneNumber;
    private String pgTxnId;
    private String refrenceNo;
    private String requestType;
    private boolean requestWithoutTxn;
    private boolean skipRefill;
    private String totalAmount;
    private String walletGuid;
    private String walletName;
    private String bankName;
    private String bankTxnId;
    private String binNumber;
    private String cardType;
    private String comment;
    private String destination;
    private String gatewayName;
    private String mid;
    private String paymentMethod;
    private String paymentMode;
    private String pgResponseCode;
    private String pgResponseMessage;
    private String targetMobileNumber;
    private String txnAmount;
    private String txnCurrency;
    private String txnStatus;


    // Getter Methods

    public String getAdminGuid() {
        return adminGuid;
    }

    public float getAdminId() {
        return adminId;
    }

    public String getAmount() {
        return amount;
    }

    public String getAppliedToNewUsers() {
        return appliedToNewUsers;
    }

    public String getApplyVerifiedUser() {
        return applyVerifiedUser;
    }

    public String getBankGateway() {
        return bankGateway;
    }

    public String getBlockingAmount() {
        return blockingAmount;
    }

    public String getBlockingReason() {
        return blockingReason;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCustId() {
        return custId;
    }

    public String getIndustryType() {
        return industryType;
    }

    public String getMerchantGuid() {
        return merchantGuid;
    }

    public String getMerchantOrderId() {
        return merchantOrderId;
    }

    public String getPayeeEmailId() {
        return payeeEmailId;
    }

    public String getPayeePhoneNumber() {
        return payeePhoneNumber;
    }

    public String getPgTxnId() {
        return pgTxnId;
    }

    public String getRefrenceNo() {
        return refrenceNo;
    }

    public String getRequestType() {
        return requestType;
    }

    public boolean getRequestWithoutTxn() {
        return requestWithoutTxn;
    }

    public boolean getSkipRefill() {
        return skipRefill;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public String getWalletGuid() {
        return walletGuid;
    }

    public String getWalletName() {
        return walletName;
    }

    public String getBankName() {
        return bankName;
    }

    public String getBankTxnId() {
        return bankTxnId;
    }

    public String getBinNumber() {
        return binNumber;
    }

    public String getCardType() {
        return cardType;
    }

    public String getComment() {
        return comment;
    }

    public String getDestination() {
        return destination;
    }

    public String getGatewayName() {
        return gatewayName;
    }

    public String getMid() {
        return mid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public String getPgResponseCode() {
        return pgResponseCode;
    }

    public String getPgResponseMessage() {
        return pgResponseMessage;
    }

    public String getTargetMobileNumber() {
        return targetMobileNumber;
    }

    public String getTxnAmount() {
        return txnAmount;
    }

    public String getTxnCurrency() {
        return txnCurrency;
    }

    public String getTxnStatus() {
        return txnStatus;
    }

    // Setter Methods

    public RequestDTO setAdminGuid(String adminGuid) {
        this.adminGuid = adminGuid;
        return this;
    }

    public RequestDTO setAdminId(float adminId) {
        this.adminId = adminId;
        return this;
    }

    public RequestDTO setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public RequestDTO setAppliedToNewUsers(String appliedToNewUsers) {
        this.appliedToNewUsers = appliedToNewUsers;
        return this;
    }

    public RequestDTO setApplyVerifiedUser(String applyVerifiedUser) {
        this.applyVerifiedUser = applyVerifiedUser;
        return this;
    }

    public RequestDTO setBankGateway(String bankGateway) {
        this.bankGateway = bankGateway;
        return this;
    }

    public RequestDTO setBlockingAmount(String blockingAmount) {
        this.blockingAmount = blockingAmount;
        return this;
    }

    public RequestDTO setBlockingReason(String blockingReason) {
        this.blockingReason = blockingReason;
        return this;
    }

    public RequestDTO setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
        return this;
    }

    public RequestDTO setCustId(String custId) {
        this.custId = custId;
        return this;
    }

    public RequestDTO setIndustryType(String industryType) {
        this.industryType = industryType;
        return this;
    }

    public RequestDTO setMerchantGuid(String merchantGuid) {
        this.merchantGuid = merchantGuid;
        return this;
    }

    public RequestDTO setMerchantOrderId(String merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
        return this;
    }

    public RequestDTO setPayeeEmailId(String payeeEmailId) {
        this.payeeEmailId = payeeEmailId;
        return this;
    }

    public RequestDTO setPayeePhoneNumber(String payeePhoneNumber) {
        this.payeePhoneNumber = payeePhoneNumber;
        return this;
    }

    public RequestDTO setPgTxnId(String pgTxnId) {
        this.pgTxnId = pgTxnId;
        return this;
    }

    public RequestDTO setRefrenceNo(String refrenceNo) {
        this.refrenceNo = refrenceNo;
        return this;
    }

    public RequestDTO setRequestType(String requestType) {
        this.requestType = requestType;
        return this;
    }

    public RequestDTO setRequestWithoutTxn(boolean requestWithoutTxn) {
        this.requestWithoutTxn = requestWithoutTxn;
        return this;
    }

    public RequestDTO setSkipRefill(boolean skipRefill) {
        this.skipRefill = skipRefill;
        return this;
    }

    public RequestDTO setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
        return this;
    }

    public RequestDTO setWalletGuid(String walletGuid) {
        this.walletGuid = walletGuid;
        return this;
    }

    public RequestDTO setWalletName(String walletName) {
        this.walletName = walletName;
        return this;
    }

    public RequestDTO setBankName(String bankName) {
        this.bankName = bankName;
        return this;
    }

    public RequestDTO setBankTxnId(String bankTxnId) {
        this.bankTxnId = bankTxnId;
        return this;
    }

    public RequestDTO setBinNumber(String binNumber) {
        this.binNumber = binNumber;
        return this;
    }

    public RequestDTO setCardType(String cardType) {
        this.cardType = cardType;
        return this;
    }

    public RequestDTO setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public RequestDTO setDestination(String destination) {
        this.destination = destination;
        return this;
    }

    public RequestDTO setGatewayName(String gatewayName) {
        this.gatewayName = gatewayName;
        return this;
    }

    public RequestDTO setMid(String mid) {
        this.mid = mid;
        return this;
    }

    public RequestDTO setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
        return this;
    }

    public RequestDTO setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
        return this;
    }

    public RequestDTO setPgResponseCode(String pgResponseCode) {
        this.pgResponseCode = pgResponseCode;
        return this;
    }

    public RequestDTO setPgResponseMessage(String pgResponseMessage) {
        this.pgResponseMessage = pgResponseMessage;
        return this;
    }

    public RequestDTO setTargetMobileNumber(String targetMobileNumber) {
        this.targetMobileNumber = targetMobileNumber;
        return this;
    }

    public RequestDTO setTxnAmount(String txnAmount) {
        this.txnAmount = txnAmount;
        return this;
    }

    public RequestDTO setTxnCurrency(String txnCurrency) {
        this.txnCurrency = txnCurrency;
        return this;
    }

    public RequestDTO setTxnStatus(String txnStatus) {
        this.txnStatus = txnStatus;
        return this;
    }
}
