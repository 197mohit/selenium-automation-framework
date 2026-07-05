package com.paytm.dto.PpblDTO;

import com.paytm.framework.utils.CommonUtils;

public class PpblAddMoneyDTO {

    private String amount;
    private String beneficiaryName;
    private String currency;
    private boolean customDataFlag;
    private String middlewareTransactionId;
    private String remitterAccount;
    private String reportCode;
    private String sourceAccount;
    private String sourceTransactionDate;
    private String sourceTransactionParticulars;
    private String targetAccount;
    private String targetTransactionDate;
    private String targetTransactionParticulars;
    private String tellerBranch;
    private String tellerUser;
    private CustomData customData;

    public PpblAddMoneyDTO(String amount, String transId, String targetAcc, CustomData customData) {
        setAmount(amount);
        setBeneficiaryName("");
        setCurrency("INR");
        setCustomData(customData);
        setCustomDataFlag(true);
        setMiddlewareTransactionId(transId);
        setRemitterAccount("19744210000001");
        setReportCode("70230");
        setSourceAccount("919007097458");
        setTargetTransactionDate(CommonUtils.getdate("yyyy-MM-dd") + "T01:46:27.087");
        setSourceTransactionDate("");
        setTargetAccount(targetAcc);
        setTargetTransactionDate(CommonUtils.getdate("yyyy-MM-dd") + "T01:46:27.087");
        setTargetTransactionParticulars("");
        setTellerBranch("");
        setTargetAccount("");
    }

    public PpblAddMoneyDTO(String amount, String transId, String targetAcc) {

        new PpblAddMoneyDTO(amount, transId, targetAcc, new CustomData(transId));
    }

    public String getAmount() {
        return amount;
    }

    public PpblAddMoneyDTO setAmount(String amount) {
        this.amount = amount;
        return this;
    }

    public String getBeneficiaryName() {
        return beneficiaryName;
    }

    public PpblAddMoneyDTO setBeneficiaryName(String beneficiaryName) {
        this.beneficiaryName = beneficiaryName;
        return this;
    }

    public String getCurrency() {
        return currency;
    }

    public PpblAddMoneyDTO setCurrency(String currency) {
        this.currency = currency;
        return this;
    }

    public boolean isCustomDataFlag() {
        return customDataFlag;
    }

    public PpblAddMoneyDTO setCustomDataFlag(boolean customDataFlag) {
        this.customDataFlag = customDataFlag;
        return this;
    }

    public String getMiddlewareTransactionId() {
        return middlewareTransactionId;
    }

    public PpblAddMoneyDTO setMiddlewareTransactionId(String middlewareTransactionId) {
        this.middlewareTransactionId = middlewareTransactionId;
        return this;
    }

    public String getRemitterAccount() {
        return remitterAccount;
    }

    public PpblAddMoneyDTO setRemitterAccount(String remitterAccount) {
        this.remitterAccount = remitterAccount;
        return this;
    }

    public String getReportCode() {
        return reportCode;
    }

    public PpblAddMoneyDTO setReportCode(String reportCode) {
        this.reportCode = reportCode;
        return this;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public PpblAddMoneyDTO setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
        return this;
    }

    public String getSourceTransactionDate() {
        return sourceTransactionDate;
    }

    public PpblAddMoneyDTO setSourceTransactionDate(String sourceTransactionDate) {
        this.sourceTransactionDate = sourceTransactionDate;
        return this;
    }

    public String getSourceTransactionParticulars() {
        return sourceTransactionParticulars;
    }

    public PpblAddMoneyDTO setSourceTransactionParticulars(String sourceTransactionParticulars) {
        this.sourceTransactionParticulars = sourceTransactionParticulars;
        return this;
    }

    public String getTargetAccount() {
        return targetAccount;
    }

    public PpblAddMoneyDTO setTargetAccount(String targetAccount) {
        this.targetAccount = targetAccount;
        return this;
    }

    public String getTargetTransactionDate() {
        return targetTransactionDate;
    }

    public PpblAddMoneyDTO setTargetTransactionDate(String targetTransactionDate) {
        this.targetTransactionDate = targetTransactionDate;
        return this;
    }

    public String getTargetTransactionParticulars() {
        return targetTransactionParticulars;
    }

    public PpblAddMoneyDTO setTargetTransactionParticulars(String targetTransactionParticulars) {
        this.targetTransactionParticulars = targetTransactionParticulars;
        return this;
    }

    public String getTellerBranch() {
        return tellerBranch;
    }

    public PpblAddMoneyDTO setTellerBranch(String tellerBranch) {
        this.tellerBranch = tellerBranch;
        return this;
    }

    public String getTellerUser() {
        return tellerUser;
    }

    public PpblAddMoneyDTO setTellerUser(String tellerUser) {
        this.tellerUser = tellerUser;
        return this;
    }

    public CustomData getCustomData() {
        return customData;
    }

    public PpblAddMoneyDTO setCustomData(CustomData customData) {
        this.customData = customData;
        return this;
    }


}
