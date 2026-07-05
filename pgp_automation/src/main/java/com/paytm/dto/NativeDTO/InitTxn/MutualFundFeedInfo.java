package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "mf_txn_id",
        "mf_bse_bank_code",
        "mf_internal_ref",
        "mf_amc_code",
        "mf_bse_member_id",
        "mf_ucc",
        "mf_scheme_category_id",
        "mf_client_txn_id",
        "mf_ifsc",
        "mf_bank_account",
        "mf_investment_type",
        "mf_upir"
})
public class MutualFundFeedInfo implements Serializable {

    @JsonProperty("mf_txn_id")
    private String mfTxnId;
    @JsonProperty("mf_bse_bank_code")
    private String mfBseBankCode;
    @JsonProperty("mf_internal_ref")
    private String mfInternalRef;
    @JsonProperty("mf_amc_code")
    private String mfAmcCode;
    @JsonProperty("mf_bse_member_id")
    private String mfBseMemberId;
    @JsonProperty("mf_ucc")
    private String mfUcc;
    @JsonProperty("mf_scheme_category_id")
    private String mfSchemeCategoryId;
    @JsonProperty("mf_client_txn_id")
    private String mfClientTxnId;
    @JsonProperty("mf_ifsc")
    private String mfIfsc;
    @JsonProperty("mf_bank_account")
    private String mfBankAccount;
    @JsonProperty("mf_investment_type")
    private String mfInvestmentType;
    @JsonProperty("mf_upir")
    private String mfUpir;

    private final static long serialVersionUID = -7783736183013740033L;

    public MutualFundFeedInfo() {
    }

    @JsonProperty("mf_txn_id")
    public String getMfTxnId() {
        return mfTxnId;
    }

    @JsonProperty("mf_txn_id")
    public MutualFundFeedInfo setMfTxnId(String mfTxnId) {
        this.mfTxnId = mfTxnId;
        return this;
    }

    @JsonProperty("mf_bse_bank_code")
    public String getMfBseBankCode() {
        return mfBseBankCode;
    }

    @JsonProperty("mf_bse_bank_code")
    public MutualFundFeedInfo setMfBseBankCode(String mfBseBankCode) {
        this.mfBseBankCode = mfBseBankCode;
        return this;
    }

    @JsonProperty("mf_internal_ref")
    public String getMfInternalRef() {
        return mfInternalRef;
    }

    @JsonProperty("mf_internal_ref")
    public MutualFundFeedInfo setMfInternalRef(String mfInternalRef) {
        this.mfInternalRef = mfInternalRef;
        return this;
    }

    @JsonProperty("mf_amc_code")
    public String getMfAmcCode() {
        return mfAmcCode;
    }

    @JsonProperty("mf_amc_code")
    public MutualFundFeedInfo setMfAmcCode(String mfAmcCode) {
        this.mfAmcCode = mfAmcCode;
        return this;
    }

    @JsonProperty("mf_bse_member_id")
    public String getMfBseMemberId() {
        return mfBseMemberId;
    }

    @JsonProperty("mf_bse_member_id")
    public MutualFundFeedInfo setMfBseMemberId(String mfBseMemberId) {
        this.mfBseMemberId = mfBseMemberId;
        return this;
    }

    @JsonProperty("mf_ucc")
    public String getMfUcc() {
        return mfUcc;
    }

    @JsonProperty("mf_ucc")
    public MutualFundFeedInfo setMfUcc(String mfUcc) {
        this.mfUcc = mfUcc;
        return this;
    }

    @JsonProperty("mf_scheme_category_id")
    public String getMfSchemeCategoryId() {
        return mfSchemeCategoryId;
    }

    @JsonProperty("mf_scheme_category_id")
    public MutualFundFeedInfo setMfSchemeCategoryId(String mfSchemeCategoryId) {
        this.mfSchemeCategoryId = mfSchemeCategoryId;
        return this;
    }

    @JsonProperty("mf_client_txn_id")
    public String getMfClientTxnId() {
        return mfClientTxnId;
    }

    @JsonProperty("mf_client_txn_id")
    public MutualFundFeedInfo setMfClientTxnId(String mfClientTxnId) {
        this.mfClientTxnId = mfClientTxnId;
        return this;
    }

    @JsonProperty("mf_ifsc")
    public String getMfIfsc() {
        return mfIfsc;
    }

    @JsonProperty("mf_ifsc")
    public MutualFundFeedInfo setMfIfsc(String mfIfsc) {
        this.mfIfsc = mfIfsc;
        return this;
    }

    @JsonProperty("mf_bank_account")
    public String getMfBankAccount() {
        return mfBankAccount;
    }

    @JsonProperty("mf_bank_account")
    public MutualFundFeedInfo setMfBankAccount(String mfBankAccount) {
        this.mfBankAccount = mfBankAccount;
        return this;
    }

    @JsonProperty("mf_investment_type")
    public String getMfInvestmentType() {
        return mfInvestmentType;
    }

    @JsonProperty("mf_investment_type")
    public MutualFundFeedInfo setMfInvestmentType(String mfInvestmentType) {
        this.mfInvestmentType = mfInvestmentType;
        return this;
    }

    @JsonProperty("mf_upir")
    public String getMfUpir() {
        return mfUpir;
    }

    @JsonProperty("mf_upir")
    public MutualFundFeedInfo setMfUpir(String mfUpir) {
        this.mfUpir = mfUpir;
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("mf_txn_id", mfTxnId)
                .append("mf_bse_bank_code", mfBseBankCode)
                .append("mf_internal_ref", mfInternalRef)
                .append("mf_amc_code", mfAmcCode)
                .append("mf_bse_member_id", mfBseMemberId)
                .append("mf_ucc", mfUcc)
                .append("mf_scheme_category_id", mfSchemeCategoryId)
                .append("mf_client_txn_id", mfClientTxnId)
                .append("mf_ifsc", mfIfsc)
                .append("mf_bank_account", mfBankAccount)
                .append("mf_investment_type", mfInvestmentType)
                .append("mf_upir", mfUpir)
                .toString();
    }
}
