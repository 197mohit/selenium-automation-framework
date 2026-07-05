package com.paytm.dto.NativeDTO.InitTxn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

public class InitTxnDTO {
    private Body body;
    private Head head;

    public Body getBody() {
        return body;
    }

    public void setBody(Body body) {
        this.body = body;
    }

    public Head getHead() {
        return head;
    }

    public void setHead(Head head) {
        this.head = head;
    }

    public InitTxnDTO(Builder builder) {
        this.body = new Body(
                builder.callbackUrl,
                builder.websiteName,
                builder.disablePaymentMode,
                builder.enablePaymentMode,
                //new  DisablePaymentMode[]{new  DisablePaymentMode(builder.disabledChannels,  builder.disablePaymentMode)},
                //new  EnablePaymentMode[]{new  EnablePaymentMode(builder.enabledChannels,  builder.enablePaymentMode)},
                builder.requestType,
                builder.ssoToken,
                builder.mid,
                new UserInfo(builder.custId, builder.lastName, builder.email, builder.firstName, builder.mobile),
                builder.promoCode,
                new TxnAmount(builder.txnValue,builder.currency),
                builder.emiOption,
                builder.orderId,
                builder.cardTokenRequired,
                builder.extendInfo,
                builder.allowUnverifiedAccount,
                builder.validateAccountNumber,
                builder.aggrMid,
                builder.subscriptionPaymentMode, builder.subscriptionAmountType,
                builder.subscriptionMaxAmount, builder.subscriptionFrequency,
                builder.subscriptionFrequencyUnit, builder.subscriptionExpiryDate,
                builder.subscriptionEnableRetry, builder.subscriptionGraceDays,
                builder.subscriptionStartDate, builder.subscriptionRetryCount, builder.isNativeAddMoney,
                builder.subsPPIOnly,
                builder.emiId,
                builder.riskFeeDetails,
                builder.peonUrl,
                builder.appInvokeDevice,
                builder.simplifiedPaymentOffers,
                builder.paymentOffersApplied,
                builder.aggType,
                builder.offlineFlow,
                builder.orderPricingInfo,
                builder.mandateType,
                builder.mandateAuthMode,
                builder.addMoneyFeeAppliedOnWallet,
                builder.accountNumber,
                builder.cardHash,
                builder.splitSettlementInfo,
                builder.simplifiedSubvention,
                builder.simplifiedUnifiedOffers,
                builder.mandateAccountDetails,
                builder.paymodeSequence,
                builder.vanInfo,
                builder.autoRefund,
                builder.cashierAdditionalInfo,
                builder.ultimateBeneficiaryDetails)
                .setEmiSubventionToken(builder.emiSubventionToken)
                .setPayableAmount(builder.payableAmount)
                .setChargeAmount(builder.chargeAmount)
                .setRiskExtendInfo(builder.riskExtendInfo)
                .setTpvInfos(builder.tpvInfos)
                .setPayerAccountDetails(builder.payerAccountDetails)
                .setAdditionalInfo(builder.additionalInfo)
                .setMutualFundFeedInfo(builder.mutualFundFeedInfo)
                .setUnifiedOffersToken(builder.unifiedOffersToken);

        this.head = new Head(builder.channelId, builder.signature, builder.clientId, builder.version);
    }

    public InitTxnDTO() {
    }

    @Override
    public String toString() {
        return "ClassPojo  [body  =  " + body.toString() + ",  Head  =  " + head.toString() + "]";
    }

    public String orderFromBody() {
        return this.getBody().getOrderId();
    }

    public String txnAmountFromBody() {
        return this.getBody().getTxnAmount().getValue();
    }

    public static class Builder {
        private String callbackUrl;
        private String websiteName;
        private String unifiedOffersToken;
        private EnablePaymentMode[] enablePaymentMode;
        private DisablePaymentMode[] disablePaymentMode;
        private String requestType;
        private String promoCode;
        private String applyAvailablePromo;
        private String validatePromo;
        private String mandateType;
        /**
         * Optional. Defaults to null. When unset, it is omitted from the init-txn JSON body
         * ({@link Body} is {@code @JsonInclude(NON_NULL)}), so existing flows and tests for
         * other paymodes are unchanged unless they call {@link #setMandateAuthMode(String)}.
         */
        private String mandateAuthMode;
        private String isNativeAddMoney;
        private String txnValue;
        private String currency;
        private String orderId;
        private String custId;
        private String lastName;
        private String email;
        private String firstName;
        private String mobile;
        private String channelId;
        private String emiId;
        private String signature;
        private String clientId;
        private String version;
        private String ssoToken;
        private String mid;
        private String aggType;
        private String offlineFlow;
        private OrderPricingInfo orderPricingInfo;
        private String emiSubventionToken = null;
        private String emiOption;
        private String cardTokenRequired;
        private String payerAccount;
        private String merchantKey;
        private String validateAccountNumber;  //  Mutual  Fund  changes
        private String aggrMid;        //  Mutual  Fund  Changes
        private String allowUnverifiedAccount;  //  Mutual  Fund  Changes
        private ExtendInfo extendInfo;
        private String subscriptionPaymentMode;
        private String subscriptionAmountType;
        private String subscriptionMaxAmount;
        private String subscriptionFrequency;
        private String subscriptionFrequencyUnit;
        private String subscriptionExpiryDate;
        private String subscriptionEnableRetry;
        private String subscriptionGraceDays;
        private String subscriptionStartDate;
        private String subscriptionRetryCount;
        private String subsPPIOnly;
        private RiskFeeDetails riskFeeDetails;
        private String peonUrl;
        private String appInvokeDevice;
        private String accountNumber;
        private TxnAmount payableAmount = null;
        private TxnAmount chargeAmount = null;
        private SimplifiedPaymentOffers simplifiedPaymentOffers = null;
        private PaymentOffersApplied paymentOffersApplied = null;
        private SimplifiedSubvention simplifiedSubvention = null;
        private SimplifiedUnifiedOffers simplifiedUnifiedOffers=null;
        private VanInfo vanInfo = null;
        private List<TpvInfo> tpvInfos;
        private List<PayerAccountDetail> payerAccountDetails;
        private AdditionalInfo additionalInfo;
        private MutualFundFeedInfo mutualFundFeedInfo;
        private String ifscCode;
        private String status;
        private String bankName;
        private String accountHolderName;
        private String accountType;
        private String nbin;
        private Boolean autoRefund;
        private Boolean addMoneyFeeAppliedOnWallet;
        private String cardHash;
        private MandateAccountDetails mandateAccountDetails;
        private SplitSettlementInfo splitSettlementInfo;
        private RiskExtendInfo riskExtendInfo;
        private String paymodeSequence;
        private UltimateBeneficiaryDetails ultimateBeneficiaryDetails = null;
        private AffordabilityInfo affordabilityInfo;
        private CashierAdditionalInfo cashierAdditionalInfo;
        private String ifsc;
        private String allowedTpap;
        private String encryptedParams;
        private String encKeyId;
        /** When non-null after build overlays, replaces {@link Body} goods placeholder. */
        private Good[] goodsOverride;
        /** When non-null after build overlays, replaces {@link Body} shipping placeholder. */
        private ShippingInfo[] shippingInfoOverride;
        /** When set with invoice date, {@link #build()} attaches {@link InvoiceDetails} with {@code invoiceId == orderId}. */
        private InvoiceDetails invoiceDetails;
        /** Cross-border overlay: invoice date only; invoiceId always set to generated {@link #orderId} at build time. */
        private String invoiceDate;
        private String userAddress;
        private String userPincode;
        private String userCity;
        private String userState;
        private String userCountryName;
        private String userCountryCode;
        private String userPan;
        private String userDob;
        private String userIeCode;
        private BankAccount userBankAccount;

        public Builder(String ssoToken, MerchantType mid) {
            this(mid.getId(), mid.getKey(), ssoToken);
        }

        public Builder(MerchantType mid) {
            this(mid.getId(), mid.getKey());
        }

        public Builder(String mId, String mKey, String ssoToken) {
            this.ssoToken = ssoToken;
            this.mid = mId;
            this.callbackUrl = "https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse";
            this.websiteName = "retail";
            this.disablePaymentMode = null;
            this.enablePaymentMode = null;
            this.mandateAccountDetails = null;
            this.requestType = "Payment";
            this.promoCode = null;
            this.mandateType = "";
            this.txnValue = "1";
            this.currency = "INR";
            this.orderId = CommonHelpers.generateOrderId();
            this.custId = CommonHelpers.generateOrderId();
            this.lastName = "test";
            this.email = "test@paytm.com";
            this.firstName = "test";
            this.mobile = "7017658313";
            this.channelId = "WEB";
            this.signature = null;
            this.clientId = "C11";
            this.version = "v1";
            this.emiOption = "";
            this.merchantKey = mKey;
            this.validateAccountNumber = "true";
            this.allowUnverifiedAccount = "false";
            this.aggrMid = "";
            this.payerAccount = "test@paytm.com";
            this.isNativeAddMoney="false";
            this.subscriptionExpiryDate=CommonHelpers.getDate().plusYears(3L).minusMonths(4L).toString();
            this.subscriptionEnableRetry="1";
            this.subscriptionRetryCount="3";
            this.emiId="";
            //  this.riskFeeDetails = new RiskFeeDetails();
            this.peonUrl="";
            this.extendInfo = null;
            this.accountNumber=null;
            this.paymodeSequence = "";

        }

        /**
         * Same defaults as {@link #Builder(String, String, String)} with optional overlays from
         * {@link CrossBorderInitPayload}: invoice date, goods/shipping overrides, enriched user identity and bank/IEC fields.
         * {@link #orderId} and {@link #custId} always remain {@link CommonHelpers#generateOrderId()} from the base constructor —
         * the payload cannot override them. {@link InvoiceDetails#getInvoiceId()} on the wire is always set to {@code orderId}
         * when an invoice date is supplied (via payload or {@link #setInvoiceDetails(InvoiceDetails)} date only).
         */
        public Builder(String mId, String mKey, String ssoToken, CrossBorderInitPayload crossBorderPayload) {
            this(mId, mKey, ssoToken);
            overlayCrossBorderInit(crossBorderPayload);
        }

        private void overlayCrossBorderInit(CrossBorderInitPayload p) {
            if (p == null) {
                return;
            }
            if (p.txnValue != null) {
                this.txnValue = p.txnValue;
            }
            if (p.txnCurrency != null) {
                this.currency = p.txnCurrency;
            }
            if (p.callbackUrl != null) {
                this.callbackUrl = p.callbackUrl;
            }
            if (p.websiteName != null) {
                this.websiteName = p.websiteName;
            }
            if (p.mobile != null) {
                this.mobile = p.mobile;
            }
            if (p.email != null) {
                this.email = p.email;
            }
            if (p.firstName != null) {
                this.firstName = p.firstName;
            }
            if (p.lastName != null) {
                this.lastName = p.lastName;
            }
            if (p.invoiceDate != null) {
                this.invoiceDate = p.invoiceDate;
            }
            if (p.goods != null) {
                this.goodsOverride = p.goods;
            }
            if (p.shippingInfo != null) {
                this.shippingInfoOverride = p.shippingInfo;
            }
            if (p.userAddress != null) {
                this.userAddress = p.userAddress;
            }
            if (p.userPincode != null) {
                this.userPincode = p.userPincode;
            }
            if (p.userCity != null) {
                this.userCity = p.userCity;
            }
            if (p.userState != null) {
                this.userState = p.userState;
            }
            if (p.userCountryName != null) {
                this.userCountryName = p.userCountryName;
            }
            if (p.userCountryCode != null) {
                this.userCountryCode = p.userCountryCode;
            }
            if (p.userPan != null) {
                this.userPan = p.userPan;
            }
            if (p.userDob != null) {
                this.userDob = p.userDob;
            }
            if (p.userIeCode != null) {
                this.userIeCode = p.userIeCode;
            }
            if (p.userBankAccount != null) {
                this.userBankAccount = p.userBankAccount;
            }
        }

        /**
         * Merges optional invoice / goods / shipping / enriched user onto {@link Body#getUserInfo()} before checksum.
         */
        private void applyOptionalInitTxnPayload(InitTxnDTO initTxnDTO) {
            Body body = initTxnDTO.getBody();
            if (body == null) {
                return;
            }
            String resolvedInvoiceDate = null;
            if (invoiceDetails != null && invoiceDetails.getInvoiceDate() != null) {
                resolvedInvoiceDate = invoiceDetails.getInvoiceDate();
            } else if (invoiceDate != null) {
                resolvedInvoiceDate = invoiceDate;
            }
            if (resolvedInvoiceDate != null) {
                InvoiceDetails merged = new InvoiceDetails();
                merged.setInvoiceId(body.getOrderId());
                merged.setInvoiceDate(resolvedInvoiceDate);
                body.setInvoiceDetails(merged);
            }
            if (goodsOverride != null) {
                body.setGoods(goodsOverride);
            }
            if (shippingInfoOverride != null) {
                body.setShippingInfo(shippingInfoOverride);
            }
            UserInfo userInfo = body.getUserInfo();
            if (userInfo == null) {
                return;
            }
            if (userAddress != null) {
                userInfo.setAddress(userAddress);
            }
            if (userPincode != null) {
                userInfo.setPincode(userPincode);
            }
            if (userCity != null) {
                userInfo.setCity(userCity);
            }
            if (userState != null) {
                userInfo.setState(userState);
            }
            if (userCountryName != null) {
                userInfo.setCountryName(userCountryName);
            }
            if (userCountryCode != null) {
                userInfo.setCountryCode(userCountryCode);
            }
            if (userPan != null) {
                userInfo.setPan(userPan);
            }
            if (userDob != null) {
                userInfo.setDob(userDob);
            }
            if (userIeCode != null) {
                userInfo.setIeCode(userIeCode);
            }
            if (userBankAccount != null) {
                userInfo.setBankAccount(userBankAccount);
            }
        }

        public Builder(String mId, String mKey ,List<String> allowedTpaps) {
            this.mid = mId;
            this.callbackUrl = "https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse";
            this.websiteName = "retail";
            this.disablePaymentMode = null;
            this.enablePaymentMode = null;
            this.mandateAccountDetails = null;
            this.requestType = "Payment";
            this.promoCode = null;
            this.mandateType = "";
            this.txnValue = "1";
            this.currency = "INR";
            this.orderId = CommonHelpers.generateOrderId();
            this.custId = CommonHelpers.generateOrderId();
            this.lastName = "test";
            this.email = "test@paytm.com";
            this.firstName = "test";
            this.mobile = "7017658313";
            this.channelId = "WEB";
            this.signature = null;
            this.clientId = "C11";
            this.version = "v1";
            this.emiOption = "";
            this.merchantKey = mKey;
            this.validateAccountNumber = "true";
            this.allowUnverifiedAccount = "false";
            this.aggrMid = "";
            this.payerAccount = "test@paytm.com";
            this.isNativeAddMoney="false";
            this.subscriptionExpiryDate=CommonHelpers.getDate().plusYears(3L).minusMonths(4L).toString();
            this.subscriptionEnableRetry="1";
            this.subscriptionRetryCount="3";
            this.emiId="";
            //  this.riskFeeDetails = new RiskFeeDetails();
            this.peonUrl="";
            this.extendInfo = null;
            this.accountNumber=null;
            this.paymodeSequence = "";
            String allowedTpapString = String.join(", ", allowedTpap);
            this.allowedTpap=allowedTpapString;

        }

        public Builder(String mId, String mKey) {
            this.mid = mId;
            this.callbackUrl = "https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse";
            this.websiteName = "retail";
            this.disablePaymentMode = null;
            this.enablePaymentMode = null;
            this.mandateAccountDetails = null;
            this.requestType = "Payment";
            this.promoCode = null;
            this.mandateType = "";
            this.txnValue = "1";
            this.currency = "INR";
            this.orderId = CommonHelpers.generateOrderId();
            this.custId = CommonHelpers.generateOrderId();
            this.lastName = "test";
            this.email = "test@paytm.com";
            this.firstName = "test";
            this.mobile = "7017658313";
            this.channelId = "WEB";
            this.signature = null;
            this.clientId = "C11";
            this.version = "v1";
            this.emiOption = "";
            this.merchantKey = mKey;
            this.validateAccountNumber = "true";
            this.allowUnverifiedAccount = "false";
            this.aggrMid = "";
            this.payerAccount = "test@paytm.com";
            this.isNativeAddMoney="false";
            this.subscriptionExpiryDate=CommonHelpers.getDate().plusYears(3L).minusMonths(4L).toString();
            this.subscriptionEnableRetry="1";
            this.subscriptionRetryCount="3";
            this.emiId="";
            //  this.riskFeeDetails = new RiskFeeDetails();
            this.peonUrl="";
            this.extendInfo = null;
            this.accountNumber=null;
            this.paymodeSequence = "";

        }

        public Builder(String ssoToken,MerchantType mid, String unifiedOffersToken) {
            this(ssoToken, mid);
            this.unifiedOffersToken=unifiedOffersToken;
        }

        public Builder(String ssoToken, MerchantType mid,Integer Test) { this(mid.getId(), ssoToken,Test);
        }

        public Builder(String mId, String ssoToken,Integer test) {
            this.ssoToken = ssoToken;
            this.mid = mId;
            this.callbackUrl = "https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse";
            this.websiteName = "retail";
            this.requestType = "Payment";
            this.txnValue = "1";
            this.currency = "INR";
            this.orderId = CommonHelpers.generateOrderId();
            this.custId = CommonHelpers.generateOrderId();
            this.lastName = "test";
            this.email = "test@paytm.com";
            this.firstName = "test";
            this.mobile = "7017658313";
            this.channelId = "WEB";
            this.signature = null;
            this.clientId = "C11";
            this.version = "v1";
            this.emiOption = "";
            this.validateAccountNumber = "true";
            this.allowUnverifiedAccount = "false";
            this.aggrMid = "";
            this.payerAccount = "test@paytm.com";
            this.peonUrl="";
            this.extendInfo = null;
            this.accountNumber=null;
            this.paymodeSequence = "";

        }

        /* overloading constructor for mutual fund transactions
           Author : Rajesh Kumar
           date : 07/12/2018
         * */

        public Builder(String ssoToken, MerchantType mid, String validateAccountNumber, String allowUnverifiedAccount, String aggrMid) {

            this(ssoToken, mid);
            this.validateAccountNumber = validateAccountNumber;
            this.allowUnverifiedAccount = allowUnverifiedAccount;
            this.aggrMid = aggrMid;
            this.extendInfo = null;
        }

        public Builder(String ssoToken, MerchantType mid, PaymentOffersApplied paymentOffersApplied) {
            this(ssoToken, mid);
            this.paymentOffersApplied = paymentOffersApplied;
        }

        public Builder(String ssoToken, MerchantType mid, SimplifiedPaymentOffers simplifiedPaymentOffers) {
            this(ssoToken, mid);
            this.simplifiedPaymentOffers = simplifiedPaymentOffers;
        }

        public Builder(String ssoToken, MerchantType mid, PaymentOffersApplied paymentOffersApplied,SimplifiedPaymentOffers simplifiedPaymentOffers) {
            this(ssoToken, mid);
            this.paymentOffersApplied = paymentOffersApplied;
            this.simplifiedPaymentOffers = simplifiedPaymentOffers;
        }



        public Builder(MerchantType mid, String orderId, String ssoToken) {
            this(ssoToken, mid);
            this.mid = mid.getId();
            this.orderId = orderId;
        }

        public Builder(String ssoToken, MerchantType mid, VanInfo vanInfo, List<TpvInfo> tpvInfo) {
                this(ssoToken, mid);
                this.vanInfo = vanInfo;
                this.tpvInfos=tpvInfo;

        }
        public Builder(String ssoToken, MerchantType mid, SimplifiedSubvention simplifiedSubvention){
            this(ssoToken, mid);
            this.simplifiedSubvention= simplifiedSubvention;
        }
        public Builder(String ssoToken, MerchantType mid, SimplifiedUnifiedOffers simplifiedUnifiedOffers) {
            this(ssoToken, mid);
            this.simplifiedUnifiedOffers = simplifiedUnifiedOffers;
        }

        public Builder(String ssoToken, MerchantType mid, UltimateBeneficiaryDetails ultimateBeneficiaryDetails){
            this(ssoToken, mid);
            this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
        }
        public List<TpvInfo> getTpvInfo() {
            return tpvInfos;
        }

        public Builder setTpvInfo(List<TpvInfo> tpvInfo) {
            this.tpvInfos = tpvInfo;
            return this;
        }

        public Builder setPayerAccountDetails(List<PayerAccountDetail> payerAccountDetails) {
            this.payerAccountDetails = payerAccountDetails;
            return this;
        }

        public Builder setPayerAccountDetails(PayerAccountDetail... payerAccountDetails) {
            this.payerAccountDetails = Arrays.asList(payerAccountDetails);
            return this;
        }

        public Builder setAdditionalInfo(AdditionalInfo additionalInfo) {
            this.additionalInfo = additionalInfo;
            return this;
        }

        public Builder setMutualFundFeedInfo(MutualFundFeedInfo mutualFundFeedInfo) {
            this.mutualFundFeedInfo = mutualFundFeedInfo;
            return this;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public String getIfscCode(String ifscCode) {
            return ifscCode;
        }

        public Builder setIfscCode(String ifscCode) {
            this.ifscCode = ifscCode;
            return this;
        }

        public String getStatus(String statu) {
            return status;
        }

        public Builder setStatus(String status) {
            this.status = status;
            return this;
        }

        public String getAllowedTpap()
        {
            return allowedTpap;
        }

        public Builder setAllowedTpap(String allowedTpap)
        {
            this.allowedTpap=allowedTpap;
            return this;
        }

        public String getBankName(String bankName) {
            return bankName;
        }

        public Builder setBankName(String bankName) {
            this.bankName = bankName;
            return this;
        }

        public String getAccountHolderName(String accountHolderName) {
            return accountHolderName;
        }

        public Builder setAccountHolderName(String accountHolderName) {
            this.accountHolderName = accountHolderName;
            return this;
        }

        public String getAccountType(String accountType) {
            return accountType;
        }

        public Builder setAccountType(String accountType) {
            this.accountType = accountType;
            return  this;
        }

        public String getNbin(String nbin) {
            return nbin;
        }

        public Builder setNbin(String nbin) {
            this.nbin = nbin;
            return this;
        }

        public Builder setEmiSubventionToken(String emiSubventionToken) {
            this.emiSubventionToken = emiSubventionToken;
            return this;
        }

        public Builder(String ssoToken, MerchantType mid, VanInfo vanInfo) {
            this(ssoToken, mid);
            this.vanInfo = vanInfo;
        }
        public TxnAmount getPayableAmount() {
            return payableAmount;
        }

        public Builder setPayableAmount(TxnAmount payableAmount) {
            this.payableAmount = payableAmount;
            return this;
        }

        public TxnAmount getChargeAmount() {
            return chargeAmount;
        }

        public Builder setChargeAmount(TxnAmount chargeAmount) {
            this.chargeAmount = chargeAmount;
            return this;
        }

        public Builder setChargeAmount(String value) {
            this.chargeAmount = new TxnAmount(value, "INR");
            return this;
        }

        public String getCardHash() {
            return cardHash;
        }

        public Builder setCardHash(String cardHash) {
            this.cardHash = cardHash;
            return this;
        }

        public SplitSettlementInfo getSplitSettlementInfo() {
            return splitSettlementInfo;
        }

        public Builder setSplitSettlementInfo(SplitSettlementInfo splitSettlementInfo) {
            this.splitSettlementInfo = splitSettlementInfo;
            return this;
        }


        public RiskExtendInfo getRiskExtendInfo() {
            return riskExtendInfo;
        }

        public Builder setRiskExtendInfo(RiskExtendInfo riskExtendInfo) {
            this.riskExtendInfo = riskExtendInfo;
            return this;
        }

        public String getSubsPPIOnly() {
            return subsPPIOnly;
        }

        public Builder setSubsPPIOnly(String subsPPIOnly) {
            this.subsPPIOnly = subsPPIOnly;
            return this;
        }

        public String getSubscriptionPaymentMode() {
            return subscriptionPaymentMode;
        }

        public Builder setSubscriptionPaymentMode(String subscriptionPaymentMode) {
            this.subscriptionPaymentMode = subscriptionPaymentMode;
            return this;
        }

        public String getSubscriptionAmountType() {
            return subscriptionAmountType;
        }

        public Builder setSubscriptionAmountType(String subscriptionAmountType) {
            this.subscriptionAmountType = subscriptionAmountType;
            return this;
        }

        public SimplifiedSubvention getSimplifiedSubvention() {
            return simplifiedSubvention;
        }

        public Builder setSimplifiedSubvention(SimplifiedSubvention simplifiedSubvention) {
            this.simplifiedSubvention = simplifiedSubvention;
            return this;
        }

        public SimplifiedUnifiedOffers getSimplifiedUnifiedOffers() {
            return simplifiedUnifiedOffers;
        }

        public Builder setSimplifiedUnifiedOffers(SimplifiedUnifiedOffers simplifiedUnifiedOffers) {
            this.simplifiedUnifiedOffers = simplifiedUnifiedOffers;
            return this;
        }

        public Builder setUltimateBeneficiaryDetails(UltimateBeneficiaryDetails ultimateBeneficiaryDetails) {
            this.ultimateBeneficiaryDetails = ultimateBeneficiaryDetails;
            return this;
        }

        public Builder setMandateAccountDetails(MandateAccountDetails mandateAccountDetails) {
            this.mandateAccountDetails = mandateAccountDetails;
            return this;
        }

        public String getAggType() {
            return aggType;
        }

        public Builder setAggType(String aggType) {
            this.aggType = aggType;
            return this;
        }

        public String getOfflineFlow() {
            return offlineFlow;
        }

        public Builder setOfflineFlow(String offlineFlow) {
            this.offlineFlow = offlineFlow;
            return this;
        }

        @JsonProperty("orderPricingInfo")
        public OrderPricingInfo getOrderPricingInfo() {
            return orderPricingInfo;
        }

        @JsonProperty("orderPricingInfo")
        public Builder setOrderPricingInfo(OrderPricingInfo orderPricingInfo) {
            this.orderPricingInfo = orderPricingInfo;
            return this;
        }

        public String getSubscriptionMaxAmount() {
            return subscriptionMaxAmount;
        }

        public Builder setSubscriptionMaxAmount(String subscriptionMaxAmount) {
            this.subscriptionMaxAmount = subscriptionMaxAmount;
            return this;
        }

        public String getSubscriptionFrequency() {
            return subscriptionFrequency;
        }

        public Builder setSubscriptionFrequency(String subscriptionFrequency) {
            this.subscriptionFrequency = subscriptionFrequency;
            return this;
        }

        public Builder setEmiId(String emiId) {
            this.emiId = emiId;
            return this;
        }

        public String getSubscriptionFrequencyUnit() {
            return subscriptionFrequencyUnit;
        }

        public Builder setSubscriptionFrequencyUnit(String subscriptionFrequencyUnit) {
            this.subscriptionFrequencyUnit = subscriptionFrequencyUnit;
            return this;
        }

        public String getSubscriptionExpiryDate() {
            return subscriptionExpiryDate;
        }

        public Builder setSubscriptionExpiryDate(String subscriptionExpiryDate) {
            this.subscriptionExpiryDate = subscriptionExpiryDate;
            return this;
        }

        public String getSubscriptionEnableRetry() {
            return subscriptionEnableRetry;
        }

        public Builder setSubscriptionEnableRetry(String subscriptionEnableRetry) {
            this.subscriptionEnableRetry = subscriptionEnableRetry;
            return this;
        }

        public String getSubscriptionGraceDays() {
            return subscriptionGraceDays;
        }

        public Builder setSubscriptionGraceDays(String subscriptionGraceDays) {
            this.subscriptionGraceDays = subscriptionGraceDays;
            return this;
        }

        public String getSubscriptionStartDate() {
            return subscriptionStartDate;
        }

        public Builder setSubscriptionStartDate(String subscriptionStartDate) {
            this.subscriptionStartDate = subscriptionStartDate;
            return this;
        }

        public String getSubscriptionRetryCount() {
            return subscriptionRetryCount;
        }

        public String getAppInvokeDevice()
        {
            return appInvokeDevice;
        }


        public Builder setSubscriptionRetryCount(String subscriptionRetryCount) {
            this.subscriptionRetryCount = subscriptionRetryCount;
            return this;
        }


        public Builder setCardTokenRequired(String cardTokenRequired) {
            this.cardTokenRequired = cardTokenRequired;
            return this;
        }

        public Builder setIsNativeAddMoney(String isNativeAddMoney) {
            this.isNativeAddMoney = isNativeAddMoney;
            return this;
        }

        public Builder setPayerAccount(String payerAccount) {
            this.payerAccount = payerAccount;
            return this;
        }

        public Builder setCallbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder setWebsiteName(String websiteName) {
            this.websiteName = websiteName;
            return this;
        }

        public Builder setDisablePaymentMode(DisablePaymentMode[] disablePaymentMode) {
            this.disablePaymentMode = disablePaymentMode;
            return this;
        }

        public Builder setEnablePaymentMode(EnablePaymentMode[] enablePaymentMode) {
            this.enablePaymentMode = enablePaymentMode;
            return this;
        }

        public Builder setExtendInfo(ExtendInfo extendInfo) {
            this.extendInfo = extendInfo;
            return this;
        }

        public Builder setRequestType(String requestType) {
            this.requestType = requestType;
            return this;
        }

        public Builder setPromoCode(String promoCode) {
            this.promoCode = promoCode;
            return this;
        }

        public Builder setmandateType(String mandateType) {
            this.mandateType = mandateType;
            return this;
        }

        /**
         * Sets mandate auth mode for bank-mandate–related init requests. Leave unset (null)
         * for all non–bank-mandate scenarios so serialized payloads match historical behavior.
         */
        public Builder setMandateAuthMode(String mandateAuthMode) {
            this.mandateAuthMode = mandateAuthMode;
            return this;
        }

        public Builder setTxnValue(String txnValue) {
            this.txnValue = txnValue;
            return this;
        }

        public Builder setCurrency(String currency) {
            this.currency = currency;
            return this;
        }

        public Builder setOrderId(String orderId) {
            this.orderId = orderId;
            return this;
        }

        public Builder setCustId(String custId) {
            this.custId = custId;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setMobile(String mobile) {
            this.mobile = mobile;
            return this;
        }

        public Builder setChannelId(String channelId) {
            this.channelId = channelId;
            return this;
        }

        public Builder setValidateAccountNumber(String validateAccountNumber) {
            this.validateAccountNumber = validateAccountNumber;
            return this;
        }

        public Builder setAllowUnverifiedAccount(String allowUnverifiedAccount) {
            this.allowUnverifiedAccount = allowUnverifiedAccount;
            return this;
        }
        public Builder setAccountNumber(String AccountNumber) {
            this.accountNumber = AccountNumber;
            return this;
        }

        public Boolean getAddMoneyFeeAppliedOnWallet()
        {
            return addMoneyFeeAppliedOnWallet;
        }

        public Builder setAddMoneyFeeAppliedOnWallet(Boolean addMoneyFeeAppliedOnWallet)
        {
            this.addMoneyFeeAppliedOnWallet = addMoneyFeeAppliedOnWallet;
            return this;
        }
        public Builder setAffordabilityInfo(AffordabilityInfo affordabilityInfo) {
            this.affordabilityInfo = affordabilityInfo;
            return this;
        }



        @Deprecated
        public Builder setSignature(String signature) {
            this.signature = signature;
            return this;
        }

        @Deprecated
        public Builder setClientId(String clientId) {
            this.clientId = clientId;
            return this;
        }

        @Deprecated
        public Builder setVersion(String version) {
            this.version = version;
            return this;
        }

        public Builder setSsoToken(String ssoToken) {
            this.ssoToken = ssoToken;
            return this;
        }
        public String getUnifiedOffersToken() {
            return unifiedOffersToken;
        }
        public Builder setUnifiedOffersToken(String unifiedOffersToken) {
            this.unifiedOffersToken = unifiedOffersToken;
            return this;
        }

        public Builder setMid(String mid) {
            this.mid = mid;
            return this;
        }

        // Mutual Fund change


        public Builder setMerchantKey(String merchantKey) {
            this.merchantKey = merchantKey;
            return this;
        }

        public Builder setAggrMid(String aggrMid) {
            this.aggrMid = aggrMid;
            return this;
        }

        public Builder setpeonUrl(String peonUrl) {
            this.peonUrl = peonUrl;
            return this;
        }

        public Builder setAppInvokeDevice(String appInvokeDevice)
        {
            this.appInvokeDevice = appInvokeDevice;
            return this;
        }

        public Builder setSimplifiedPaymentOffers(SimplifiedPaymentOffers simplifiedPaymentOffers){
            this.simplifiedPaymentOffers = simplifiedPaymentOffers;
            return this;
        }

        public Builder setVanInfo(VanInfo vanInfo){
            this.vanInfo = vanInfo;
            return this;
        }

        public  Builder setAutoRefund(Boolean autoRefund){
            this.autoRefund = autoRefund;
            return this;
        }

        public Builder setPaymentOffersApplied(PaymentOffersApplied paymentOffersApplied){
            this.paymentOffersApplied = paymentOffersApplied;
            return this;
        }

        public Builder setPaymodeSequence(String paymodeSequence){
            this.paymodeSequence = paymodeSequence;
            return this;
        }

        public Builder setCashierAdditionalInfo(CashierAdditionalInfo cashierAdditionalInfo) {
            this.cashierAdditionalInfo = cashierAdditionalInfo;
            return this;
        }

        public Builder setEncryptedParams(String encryptedParams) {
            this.encryptedParams = encryptedParams;
            return this;
        }

        public Builder setEncKeyId(String encKeyId) {
            this.encKeyId = encKeyId;
            return this;
        }

        @Deprecated
        public Builder setChecksum(String checksum) {
            TreeMap<Object, String> checksumMap = new TreeMap<>();
            checksumMap.put("requestType", "Payment");
            checksumMap.put("mId", mid);
            checksumMap.put("orderId", this.orderId);
            checksumMap.put("websiteName", this.websiteName);
            this.signature = checksum;
            return this;
        }
        public String getIfsc(String ifsc) {
            return ifsc;
        }
        public Builder ifsc(String ifsc) {
            this.ifsc = ifsc;
            return this;
        }


        public Builder setEmiOption(String emiOption) {
            this.emiOption = emiOption;
            return this;
        }

        public String getEmiId() {
            return emiId;
        }


        //risk params added in initiate txn

        public Builder setRiskFeeDetails(RiskFeeDetails riskFeeDetails) {
            this.riskFeeDetails = riskFeeDetails;
            return this;
        }


        public RiskFeeDetails getRiskFeeDetails() {
            return riskFeeDetails;
        }

        /**
         * At {@link #build()}, only {@link InvoiceDetails#getInvoiceDate()} is used; {@code invoiceId}
         * is always replaced with generated {@link #orderId} (never pass a standalone invoice id here).
         */
        public Builder setInvoiceDetails(InvoiceDetails invoiceDetails) {
            this.invoiceDetails = invoiceDetails;
            return this;
        }

        public Builder setGoods(Good[] goods) {
            this.goodsOverride = goods;
            return this;
        }

        public Builder setShippingInfo(ShippingInfo[] shippingInfo) {
            this.shippingInfoOverride = shippingInfo;
            return this;
        }

        public Builder setUserAddress(String userAddress) {
            this.userAddress = userAddress;
            return this;
        }

        public Builder setUserPincode(String userPincode) {
            this.userPincode = userPincode;
            return this;
        }

        public Builder setUserCity(String userCity) {
            this.userCity = userCity;
            return this;
        }

        public Builder setUserState(String userState) {
            this.userState = userState;
            return this;
        }

        public Builder setUserCountryName(String userCountryName) {
            this.userCountryName = userCountryName;
            return this;
        }

        public Builder setUserCountryCode(String userCountryCode) {
            this.userCountryCode = userCountryCode;
            return this;
        }

        public Builder setUserPan(String userPan) {
            this.userPan = userPan;
            return this;
        }

        public Builder setUserDob(String userDob) {
            this.userDob = userDob;
            return this;
        }

        public Builder setUserIeCode(String userIeCode) {
            this.userIeCode = userIeCode;
            return this;
        }

        public Builder setUserBankAccount(BankAccount userBankAccount) {
            this.userBankAccount = userBankAccount;
            return this;
        }

        public InitTxnDTO build() {
            InitTxnDTO initTxnDTO = new InitTxnDTO(this);
            if (this.encryptedParams != null) {
                initTxnDTO.getBody().setEncryptedParams(this.encryptedParams);
            }
            if (this.encKeyId != null) {
                initTxnDTO.getBody().setEncKeyId(this.encKeyId);
            }
            applyOptionalInitTxnPayload(initTxnDTO);
            System.out.println("Merchant Key " + this.merchantKey);
            String checksum = PGPHelpers.getNativeChecksum(this.merchantKey, initTxnDTO.getBody());
            initTxnDTO.getHead().setSignature(checksum);
            return initTxnDTO;
        }
        public InitTxnDTO buildWithJwt(String client, String jwtToken) {
            InitTxnDTO initTxnDTO = new InitTxnDTO(this);
            applyOptionalInitTxnPayload(initTxnDTO);
            initTxnDTO.getHead().setClientId(client);
            initTxnDTO.getHead().setToken(jwtToken);
            initTxnDTO.getHead().setTokenType("JWT");
            return initTxnDTO;
        }

        //khatabook
        public InitTxnDTO build(String aggkey) {
            InitTxnDTO initTxnDTO = new InitTxnDTO(this);
            applyOptionalInitTxnPayload(initTxnDTO);
            String checksum = PGPHelpers.getNativeChecksum(aggkey, initTxnDTO.getBody());
            initTxnDTO.getHead().setSignature(checksum);
            return initTxnDTO;
        }

    }

    /**
     * Cross-border enriched initiate inputs (mirrors export sample minus fields owned elsewhere):
     * <ul>
     *   <li>{@code mid}: {@link Builder} first argument ({@link Builder#Builder(String, String, String, CrossBorderInitPayload)})</li>
     *   <li>{@code orderId},{@code userInfo.custId}: always {@link CommonHelpers#generateOrderId()} (not in payload)</li>
     *   <li>{@code requestType}: Payment default</li>
     *   <li>{@code txnAmount}: {@link #txnValue}, {@link #txnCurrency}</li>
     *   <li>{@code userInfo}: name, contacts, address, PAN, optional dob, IEC, bankAccount from payload fields;
     *       {@code paytmSsoToken} is builder {@code ssoToken}; {@code invoiceDetails.invoiceId} is always the generated {@code orderId}</li>
     *   <li>{@code invoiceDetails.invoiceDate}: {@link #invoiceDate}; any {@code invoiceId} supplied elsewhere is overwritten at build</li>
     *   <li>{@code goods},{@code shippingInfo},{@code callbackUrl},{@code websiteName}</li>
     *   <li>{@code head.*}: WEB, C11, v1, requestTimestamp; checksum at build time</li>
     * </ul>
     */
    public static final class CrossBorderInitPayload {
        public final String txnValue;
        public final String txnCurrency;
        public final String invoiceDate;
        public final Good[] goods;
        public final ShippingInfo[] shippingInfo;
        public final String mobile;
        public final String email;
        public final String firstName;
        public final String lastName;
        public final String callbackUrl;
        public final String websiteName;
        public final String userAddress;
        public final String userPincode;
        public final String userCity;
        public final String userState;
        public final String userCountryName;
        public final String userCountryCode;
        public final String userPan;
        public final String userDob;
        public final String userIeCode;
        public final BankAccount userBankAccount;

        public CrossBorderInitPayload(String txnValue, String txnCurrency, String invoiceDate,
                Good[] goods, ShippingInfo[] shippingInfo,
                String mobile, String email, String firstName, String lastName,
                String callbackUrl, String websiteName,
                String userAddress, String userPincode, String userCity, String userState,
                String userCountryName, String userCountryCode, String userPan, String userDob,
                String userIeCode, BankAccount userBankAccount) {
            this.txnValue = txnValue;
            this.txnCurrency = txnCurrency;
            this.invoiceDate = invoiceDate;
            this.goods = goods;
            this.shippingInfo = shippingInfo;
            this.mobile = mobile;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
            this.callbackUrl = callbackUrl;
            this.websiteName = websiteName;
            this.userAddress = userAddress;
            this.userPincode = userPincode;
            this.userCity = userCity;
            this.userState = userState;
            this.userCountryName = userCountryName;
            this.userCountryCode = userCountryCode;
            this.userPan = userPan;
            this.userDob = userDob;
            this.userIeCode = userIeCode;
            this.userBankAccount = userBankAccount;
        }
    }

}
