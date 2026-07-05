package com.paytm.appconstants;

import com.paytm.LocalConfig;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.pgplus.common.enums.ResultCode;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Constants {
    public interface Owner {
        String JAI = "Jai";
        String TARUN = "Tarun";
        String GAGANDEEP = "Gagandeep";
        String ANKUR = "Ankur";
        String DEEPAK = "Deepak";
        String KARMVIR = "Karmvir";
        String ANKIT = "Ankit";
        String PULKIT = "Pulkit";
        String SAMAR = "Samar";
        String ARSH = "Arsh";
        String SURBHI = "Surbhi";
        String MAYURI = "Mayuri";
        String ROHIT = "Rohit";
        String AKSHAT_NAYAK = "AKSHAT NAYAK";
        String ESHANI = "eshani";
        String SHUBHAM = "Shubham";
        String SRISHTI = "srishti";
        String BHARAT = "Bharat";
        String PRIYANSHI = "Priyanshi";
        String ABHAY = "Abhay";
        String VIDHI = "Vidhi";
        String POONAM = "Poonam";
        String AJEESH = "Ajeesh";
        String AKSHAT = "Akshat";
        String SRINIVAS="SRINIVAS";
        String PAYAL = "Payal";
        String GAURAV = "Gaurav";
        String AAYUSH = "Aayush";
        String POOJA = "Pooja";
        String SOURAV = "Sourav";
        String RAJKUMAR = "Rajkumar";
        String PAREEKSHITH = "Pareekshith";
        String HIMANSHU = "Himanshu";
        String ABHISHEK_TEWARI = "Abhishek Tewari";
        String PUSHKAL = "Pushkal Singh";
        String PUSPA="Puspa";
        String ABHISHEK_KULKARNI = "Abhishek Kulkarni";
        String CHETAN = "Chetan deshpande";
        String RITIK = "Ritik";
        String SAGAR = "SAGAR";
        String VAIBHAV = "Vaibhav Tyagi";
        String CHAKSHU = "Chakshu Singhal";
        String ROHIT_SHARMA = "ROHIT_SHARMA";
        String ASHISH_JASWAL = "Ashish";
        String VIKASH_VERMA ="Vikash Verma";
        String ROUNAK = "ROUNAK RAJ SINGH";
        String HARSHITA = "Harshita";
        String PRIYANKA = "Priyanka";
        String VISHNU_SHEKAR = "VISHNU_SHEKAR";
        String TAMANA_TATHAN = "Tamana";
        String PRAMOD_KUMAR= "Pramod Kumar";
        String Amanpreet= "Amanpreet";
        String DEVENDRA_SINGH = "DEVENDRA SINGH";
        String ABHISHEK_VERMA = "ABHISHEK VERMA";
        String PRAGYA_KURELE ="PRAGYA";
        String RAHUL_KANT = "RAHUL KANT";
        String MAYANK_BHARSHIV ="MAYANK BHARSHIV";
        String RUPASANANDA = "RUPASANANDA SA";
        String MANISH_MISHRA="Manish";
        String SHWETANK="Shwetank";
        String Abhishek_Gupta="Abhishek Gupta";
        String RONIKA = "Ronika";
        String ANUSHKA_GOLDI = "Anushka Goldi";
        String MONIKA_NAGARIA = "Monika Nagaria";
        String UPAMA = "Upama";

        String SATWIK_SHARMA = "Satwik Sharma";
        String MEHUL_GUPTA = "Mehul Gupta";
        String LOKESH_SAXENA ="Lokesh Saxena";
        String MOHIT_KHARE = "Mohit Khare";
        String NITISH_DHAWAN = "Nitish Dhawan";


    }

    public static final String ACCOUNT_NO_RETURNED_IN_FETCH_PPBL_BALANCE_API_MOCK = "919599711105";
    public static final long FF4J_INTERNAL_CACHE_TIMEOUT = 5000L;
    public static final double PPBL_ACCOUNT_BALANCE = 3000D;
    public static final String BAJAJ_FINSERV_BANK_EMI = "Bajaj Finserv EMI Card";

    //Promo Codes
    public enum promoCode {
        WALLET_PROMO("WALLETPROMO"),
        CC_PROMO("CCPROMOAUTO"),
        DC_PROMO("DCPROMOAUTOM"),
        NB_PROMO("NBPROMOAUTO"),
        RESTRICTED_CC_PROMO("RESCCPROMO"),
        RESTRICTED_DC_PROMO("RESDCPROMO"),
        RESTRICTED_PPI_PROMO("RESPPIPROMO"),
        EMI_PROMO_CODE("MYAPPLE");
        private String promo;

        promoCode(String promoCode) {
            this.promo = promoCode;
        }

        public String toString() {
            return this.promo;
        }

    }

    public enum Intent_Callback {
        SUCCESS("001", "SUCCESS", "success"),
        FAIL("009", "FAIL", "payment Failure"),
        SYSTEM_ERROR("010", "SYSTEM_ERROR", "payment fail due to some technical error");

        private String respCode, status, respmsg;

        Intent_Callback(String respCode, String status, String respmsg) {
            this.respCode = respCode;
            this.status = status;
            this.respmsg = respmsg;
        }

        public String getRespCode() {
            return this.respCode;
        }

        public String getStatus() {
            return this.status;
        }

        public String getRespmsg() {
            return this.respmsg;
        }
    }

    public enum TXNSTATUS {
        TXN_SUCCESS("TXN_SUCCESS"),
        TXN_FAILURE("TXN_FAILURE"),
        PENDING("PENDING"),
        BANK_TXN_FAILURE("BANK_TXN_FAILURE");
        private String txnStatus;

        TXNSTATUS(String txnStatus) {
            this.txnStatus = txnStatus;
        }

        @Override
        public String toString() {
            return txnStatus;
        }
    }

    public enum ResponseCode {
        TXN_FAILURE("810", "ORDER IS CLOSE."),
        RBI_LIMIT_EXCEEDED("3001", "Add money not allowed or failed"),
        BANK_TXN_FAILURE("227", "Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same"),
        INVALID_SSO_TOKEN("2004", "SSO Token is invalid"),
        FGW_USER_CANCEL_PAYTM_PAGE("227", "User cancelled the transaction from 3D secure/OTP page"),
        FGW_OTP_VALIDATION_FAILED("227", "Looks like OTP entered was incorrect. Please try again."),
        DQR_EXPIRED("0001", "QR Code has been expired, Please ask merchant to share the new QR code"),
        TXN_STATUS_PENDING("402", "Looks like the payment is not complete. Please wait while we confirm the status with your bank."),
        MERCHANT_STATUS_TXN_PENDING("402","We are processing your transaction."),
        PTC_TXN_PENDING("0001", "User has not completed transaction."),
        PAYMENT_FAILED("810", "Payment failed due to a technical error. Please try after some time."),
        INVALID_ORDERID("334", "Invalid Order Id."),
        REFUND_PENDING("601", "Refund request was raised for this transaction. But it is pending state"),
        INVALID_REFUND_REQUEST("600", "Invalid refund request or restricted by bank"),
        TXN_FAILURE_INVALIDMID("239", "Merchant does not exist."),
        TXN_SUCCESS("01", "Txn Success"),
        CLOSEORDERV2_TXN_PENDING("141", "User has not completed transaction."),
        WALLET_INSUFFICIENT_BALANCE("235", "Wallet balance Insufficient"),
        PAYMENT_DECLINED_BY_BANK("227","Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");


        private final String respCode;
        private final String respMsg;

        ResponseCode(String respCode, String respMsg) {
            this.respCode = respCode;
            this.respMsg = respMsg;
        }

        public String getRespCode() {
            return respCode;
        }

        public String getRespMsg() {
            return respMsg;
        }
    }

    public enum MerchantContracts {
        RecurringAcquiringProd("51051000100000000004"),
        AuthCaptureAcquiringProd("51051000100000000003"),
        StandardAcquiringProdByJSChargePayee("51051000100000000024"),
        SeamlessPaymentAcquiringProd("51051000100000000010"),
        StandardDirectPayAcquiringProd("51051000100000000001");

        private String key;

        MerchantContracts(String key) {
            this.key = key;
        }

        public String toString() {
            return this.key;
        }
    }

    public enum Module {
        EMI,
        RetryPmt,
        Subscription,
        RenewSubscription,
        Seamless,
        SeamlessNative,
        PostConvenience,
        Refund,
        UPI,
        PaytmExpress,
        TopupExpress,
        IVRFastForward
    }

    public enum TransactionType {
        PGOnly,
        Hybrid,
        AddMoney,
        AddMoneyMP,
        AddnPay,
        COD,
        WalletOnly,
        IVRFastForward
    }

    public enum InstErrorCode {

        FGW_ACCOUNT_CLOSED,
        FGW_INVALID_ACCOUNT_OR_IFSC,
        FGW_ACCOUNT_BLOCKED,
        FGW_ACCOUNT_NO,
        FGW_AMOUNT_LIMIT_EXCEEDED,
        FGW_ACCOUNT_IS_NRE,
        FGW_MERCHANT_IS_NRE_ACCOUNT,
        FGW_INVALID_NBIN,
        FGW_BANK_FAIL_SPECIFIC_REASON,
        DEFAULT,
        OTHERS

    }

    public enum NotificationStatus {
        SUCCESS,
        FAIL,
        PENDING,
        PENDING_TO_SUCCESS,
        PENDING_TO_FAIL

    }
    public static enum MerchantType {
        CHARGE_AMOUNT_INITIATE_MID,
        QR_DISABLED,
        DYNAMICQR,
        PCF_GV_UPI_INTENT,
        GV_UPI_INTENT,
        HDDOMERCHANT,
        PGONLY_EMI_MIN_MAX,
        PGOnly,
        AddMoney_CheckoutOnEnhanced,
        REFUND_IMPSHYBRID,
        REFUND_IMPSPGONLY,
        LOGOMerchant,
        EMI,
        EMIOnly,
        EMIOnly_DC,
        AddnPay,
        WalletOnly,
        Hybrid,
        BAJAJFINEMI,
        EMIDISABLEDMERCHANT,
        COD,
        PGOnly_NativeOldFlow,
        EMI_DC,
        SUBSCRIPTION_WALLET_LIMIT,
        REFUNDIMPS_HYBRID,
        PaytmExpress_Hybrid_Onus,
        PaytmExpress_Hybrid_Offus,
        PCF_ONUS,
        PG_OFFUS,
        EMISubvention,
        EMI_SUBVENTION,
        EMI_DC_CC,
        PG2_Deals,
        EMI_SUBVENTION_OFFERS,
        IVR,
        LINK_TRANSACTION,
        POS_ID,
        @Deprecated
        PaytmExpress_Hybrid_PayModeDisabled,
        @Deprecated
        PaytmExpress_Hybrid_CCPayModeDisabled,
        @Deprecated
        PaytmExpress_AddnPay_Onus,
        @Deprecated
        PaytmExpress_AddnPay_Offus,
        TopUpExpress_Onus,
        @Deprecated
        TopUpExpress_Offus,
        Seamless_Hybrid_Onus,
        Seamless_Hybrid_Offus,
        @Deprecated
        Seamless_Hybrid_PayModeDisabled,
        SeamlessNative_Hybrid_Onus,
        @Deprecated
        SeamlessNative_Hybrid_Offus,
        @Deprecated
        SeamlessNative_Hybrid_PayModeDisabled,
        //        SeamlessNative_Hybrid_CCPayModeDisabled,
        AddMoney,
        AddMoneyMP,
        PGOnly_Retry,
        Hybrid_Retry,
        AddnPay_Retry,
        //        BRAND_NAME,
        AddnPay_Disabled,
        PostConvMerchant,
        FastForward,
        Subscription_PGOnly,
        SUBSCRIPTION_PPI,
        SUBSCRIPTION_WALLET_ONLY,
        SUBSCRIPTION_PGONLY_RETRY,
        SUBSCRIPTION_PPI_RETRY,
        SUBSCRIPTION_AUTO_REFUND,
        HYBPEON,
        ADDNPAYPEON,
        WALLETPEON,
        ADD_MONEY_ONLY,
        @Deprecated
        ADD_MONEY_WITH_RETRY,
        NonMigrated,
        LICENCE_EXPIRED_MERCHANT,
        NATIVE_HYBRID,
        NATIVE_ADDNPAY,
        PG2_JS_Checkout_Paytm_Domain,
        NATIVE_EMI,
        NATIVE_WALLET_ONLY,
        @Deprecated
        ALLPAYMODE,
        PPBLYONLY,
        PPBL_PAYTMCC_VPA,
        ADD_N_PAY_WITHOUT_CC_DC,
        NATIVE_PROMO_HYBRID,
        NATIVE_HYBRID_RETRY,
        NATIVE_HDFO,
        ICICI_IDEBIT_ENABLED,
        MUTUAL_FUND,
        MUTUAL_FUND_AGGR,
        FOOD_WALLET_PAYTMCC,
        NON_MUTUAL_FUND,
        PGOnly_RateLimit,
        PGOnly_RateLimit2,
        FOOD_MERCHANT_HYB,
        FOOD_MERCHANT_ADDNPAY,
        Irctc_binIrcId,
        FLAT_PCF,
        NETBANK_PCF,
        PPBL_NB_PCF,
        AMEX_PCF,
        WALLETOnly_PCF,
        PGOnly_Pcf,
        POSTCONV_DEFAULT,
        POSTCONV_WALLET_ONLY,
        UPI_INTENT,
        UPI_INTENTONLY,
        UPI_INTENT_PrefDisabled,
        HDFO_ADDNPAY,
        BANK_MANDATE,
        SUBS_BANK_MANDATE_MID,
        SUBS_PPBL_MID,
        ADVANCE_DEPOSIT,
        SUBSCRIPTION_ADDNPAY,
        BANK_MANDATE_WO_ONDEMAND,
        BANK_MANDATE_LIMIT_1,
        BANK_MANDATE_NATIVE,
        BANK_MANDATE_MFSIP,
        PPBLC_ONLY,
        ONLY_CC,
        WALLET_UPI_MID,
        LOGIN_STRIP_MID,
        Hybrid_WithoutEMI,
        NATIVE_RISK,
        IRCTC_WalletOnly,
        IRCTC_Wallet_withPRN,
        SPLIT_SETTLEMENT_PGONLY,
        SPLIT_SETTLEMENT_ADDNPAY,
        SUBSCRIPTION_PGONLY_PEON_DISABLED,
        PGONLY_COD_PEON_DISABLED,
        ADDNPAY_PEON_DISABLED,
        HYBRID_PEON_DISABLED,
        WALLET_ONLY_PEON_DISABLED,
        NATIVE_HDFO_PEON_DISABLED,
        ICIO_CC_Enabled_Merchant,
        ICIO_CC_Enabled_Merchant_Retry,
        ZEST_MONEY,
        BIN_IN_RESPONSE_NATIVE,
        BIN_IN_RESPONSE_DISABLED,
        BIN_IN_RESPONSE_ADDNPAY,
        BIN_IN_RESPONSE_HYBRID,
        BOB_ENCRYPTED,
        MLV,
        PGOnly_Retry_PG2_RTDD,
        FOOD_MERCHANT_ADDNPAY_PG2_RTDD,
        PWP_HYBRID_PG2_RTDD,
        WalletOnly_PG2_RTDD,
        HDFC_UPI_COLLECT_PG2_RTDD,
        PGOnly_PG2_RTDD,
        AddnPay_PG2_RTDD,
        MASKED_MOBILE_ENABLED_PG2_RTDD,
        ADD_MONEY_PCF_PG2_RTDD,
        NETBANK_PCF_PG2_RTDD,
        CORPORATE_CARD_PREPAID_PG2_RTDD,
        HDFC_UPI_COLLECT_RETRY_PG2_RTDD,
        LOYALTY_POINTS_PG2_RTDD,
        WALLETOnly_PCF_PG2_RTDD,
        MDR_INTL_MERC_PG2_RTDD,
        ALLPAYMODE_PG2_RTDD,
        MDR_PCF_PG2_RTDD,
        SEAMLESS_MDR_PCF_PG2_RTDD,
        PCF_MDR_PG2_RTDD,
        MDR_PCF_code147_PG2_RTDD,
        CUSTOMFEEPCF_PG2_RTDD,
        PCF_MERCHANT_PG2_RTDD,
        RTDD_PG2_RTDD,
        PWP_DEFAULT_PG2_RTDD,
        PWP_HYBRID_RETRY_PG2_RTDD,
        PCF_WITH_PCF_FEE_INFO_PREF_DISABLED,
        DAILY,
        WEEKLY,
        MONTHLY,
        ADD_DAILY,
        ADD_WEEKLY,
        ADD_MONTHLY,
        HYB_DAILY,
        HYB_WEEKLY,
        HYB_MONTHLY,
        MGV_HYBRID,
        Qrcode_ID,
        MGV_ADDNPAY,
        BOB_ENCRYPTED_VAULT,
        LOGIN_STRIP_DISABLED,
        MGV_AGGREGATOR_CHILD,
        MGV_WITHOUT_WALLET,
        BLACKLISTED_MERCHANT,
        ADDNPAY_RETRYCASES,
        Redirectional_Native,
        STATIC_PEON_DISABLED,
        MASKED_MOBILE_ENABLED,
        ADD_MONEY_PCF,
        MGV_AGGREGATOR,
        UPI_COLLECT_NATIVE,
        PG2_QR_NEGATIVE_PREF_OFF,
        PG2_IOS_UPI_INTENT,
        PPBL_UPI_COLLECT,
        HDFC_UPI_COLLECT,
        UPI_COLLECT_NATIVEMFSIP,
        HDFC_UPI_COLLECT_RETRY,
        DYNAMICQR_DIRECT_HDFO,
        DYNAMICQR_DIRECT_ICIO,
        STATICQR_DIRECT_HDFO,
        STATICQR_DIRECT_ICIO,
        CLIENT_MAPPED_MERCHANT,
        THEMATIC,
        DYNAMIC_DAILY,
        DYNAMIC_WEEKLY,
        DYNAMIC_MONTHLY,
        OTP_INJECT,
        OTP_INJECT_RETRY,
        INVALID_WEBPAY_URL,
        BANK_SECURITY_FORM_MERCHANT_HYBRID,
        BANK_SECURITY_FORM_MERCHANT_ADDNPAY,
        LOYALTY_POINTS,
        EDC_PAY_CONFIRM,
        PRIORITY_DEFAULT_MERCHANT,
        PRIORITY_HYBRID_MERCHANT,
        PRIORITY_ADDPAY_MERCHANT,
        UPI_CONSENT_PG,
        UPI_CONSENT_HYB,
        UPI_CONSENT_ADD,
        PWP_HYBRID,
        PWP_DEFAULT,
        PWP_HYBRID_RETRY,
        PWP_HDFO_DIRECT,
        PWP_MF,
        PCF_SYNC_REFUND,
        MDR_PCF,
        PCF_MDR,
        SEAMLESS_MDR_PCF,
        UPI_NATIVE_SUBS,
        UPI_MUTUAL_MF,
        MF_CHECKOUT_ON_ENHANCED,
        ST_CHECKOUT_ON_ENHANCED,
        MF_CHECKOUT_ON_ENHANCED_AGG,
        WALLET_SETTLEMENT,
        STOCK_TRADE,
        CORPORATE_CARD_ONLY,
        CORPORATE_CARD_PREPAID,
        PCF_CUSTOM_RATE,
        MDR_CUSTOM_RATE,
        INSTANT_SETTLEMENT,
        AGGR_INSTANT_SETTLEMENT,
        INSTANT_WO_SETTLEMENT_PAYMODE,
        EDCQR,
        PG2_UPI,
        ONLINE_SETTLEMENT,
        ONLINE_SETTLEMENT_AGG,
        WITHOUT_REQ_TYPE,
        JIO,
        ODISHA_DYNAMIC_WRAPPER,
        MAPPING_EMPTY_RESPONSE,
        OFFERMID,
        EDC_RETRY,
        PAYTMONLY_QR,
        PRIORITY_LIST,
        DEFAULT_PRIORITY_LIST,
        LINK_BASED_MERCHANT_WITH_PPBLC,
        EDC_HDFC_CC,
        ADDnPAY_DISABLE,
        PPI1,
        PPI2,
        BASIC_PREFERENCES,
        XIAOMI1,
        XIAOMI2,
        XIAOMI3,
        XIAOMI4,
        PPBL_ONBOARDING,
        BLOCK_BULK_APPLY,
        AUTOINVOKE,
        PPI2_MF,
        HYBRID_DISCOUNT,
        UPI_SUBS,
        QR_NEGATIVE_PREF_ON,
        QR_NEGATIVE_PREF_OFF,
        THEMATIC_PREFERENCE,
        NON_VERIFED,
        EMI_DC_MERCHANT,
        DIFFERENT_ROI_EMI,
        CUSTOMFEEPCF,
        NEWHYBRID,
        DEALS_MID,
        DATA_ENRICHMENT_MID_1,
        DATA_ENRICHMENT_MID_2,
        DATA_ENRICHMENT_MID_3,
        MAESTRO_CARD,
        NATIVEJSON_N,
        BANKTRANSFER_MERCHANT_CONTROL,
        BANKTRANSFER_PAYTM_CONTROL,
        BANKTRANSFER_DISABLED,
        BANKTRANSFER_PCF_MERCHANT,
        BANKTRANSFER_PCF_PAYTM,
        BANKTRANSFER_RETRY,
        NOCHECKSUM_DCSUBVENTION,
        NOCHECKSUM_PCF,
        FLEXI_SUBSCRIPTION,
        EMI_LIMIT,
        SIHUB_Subs,
        SUBSCRIPTION_UPI,
        LOGO_MERCHANT,
        LOGO_MID,
        BANKOFFER_ON_PCF,
        SUBSCRIPTION,
        ONE_RETRY,
        NEW_CHECKOUTJS,
        UPI,
        DISABLED_LOGIN_STRIP_FLAG_ENABLED,
        DISABLED_LOGIN_STRIP_PREF,
        IDEMPOTENT,
        VERSION_CHANGE,
        Hybrid_Txn,
        AUTO_APP,
        ADDnPAY_CCLIMIT,
        PG2_COMMON_MERCHANT,
        CHECKOUT_ON_REDIRECTION,
        AddnPay_refund,
        Hybrid_S,
        Addnpay,
        FD_PAYMODE,
        PEON_ENABLED,
        SUBSCRIPTION_BCHJS,
        PG_JS_RETRY,
        Parent_Mid,
        Child_Mid,
        PCF_VERIFFY,
        Refund_auto,
        VPA_RETRY,
        SEAMLESS_PREPAID,
        UNGROUPED_PAYMODES_FLAG_ENABLED,
        Instrument_Categorization,
        MUTUALFUND_AGGR,
        MUTUALFUND,
        UPI_COLLECT,
        NO_CHANGE,
        WebviewSupportOnJS,
        DIRECT_OTP_JS,
        MDR_PCF_code147,
        WebviewSupport_FlagOff,
        OTP_BANK_PAGE,
        POSTPAID_BLOCK_ALLOW_PREF_N,
        POSTPAID_BLOCK_PREF_Y,
        POSTPAID_BLOCK_ALLOW_PREF_Y,
        POSTPAID_ALLOW_ALLOW_PREF_Y,
        UPI_RETRY_ENABLED,
        MINIMAL_PROMO_MERCHANT_Y,
        MINIMAL_PROMO_MERCHANT_N,
        MERCHANT_UPI_PPI_CC_DC_SUBS,
        AppInvoke,
        UPICollectSavedVPA,
        DCC_PCF,
        POSTPAIDANDUPI,
        DISABLE,
        Static_True_Recent_True,
        Static_False_Recent_True,
        Static_True_Recent_False,
        Static_False_Recent_False,
        JS_Checkout_Paytm_Domain,
        HDFO,
        HYBRID_MID,
        Meghalaya_Merchant,
        AOA_MERCHANT,
        AOA_MERCHANT_PG,
        AOA_MERCHANT_HDFO,
        MerchantWithdrawSettlement,
        MerchantAutoWithdrawSettlement,
        MerchantOnlineSettlement,
        TWSSettlementMerchant,
        REFUND_HYBRID,
        BW_InstantSettlement,
        BW_Only,
        NonBW_OS,
        UPIPUSHNEW,
        UPIPUSHAAP,
        UPIPUSHHYB,
        MIGRATIONDETAIL,
        UPIPUSHPG2,
        UPIPUSHPG2FF4JNOPREFYES,
        UPIPG2FF4JDISABLED,
        PG2FF4JYESPREFNO,
        LINK_PGONLY,
        LINK_PGONLY_MF,
        LINK_PGONLY_ST,
        Bank_Transfer_MF_Parent,
        Bank_Transfer_MF_Child,
        LINK_OFFLINE,
        STAGING_MID1,
        LINK_SKIPLOGIN,
        LINK_SKIPLOGIN_ONLINE,
        LINK_ALLIN_ONE_OFFLINE,
        LINK_ALLIN_ONE,
        AOA_VISA,
        AOA_PREPAID,
        AOA_ISSUINGBANK,
        AOA_UPIPAYMETHOD,
        NULL_MERCHANT,
        BRAND_EMI,
        ENABLE_DISABLE_PAYMODE,
        ENABLE_DISABLE_PAYMENT_BUTTON,
        ENABLE_DISABLE_OFFLINE_LINK,
        LINK_PGONLY_OFFLINE,
        LINK_SD,
        LINK_SD_OFFLINE,
        PPBL_NB,
        QR_LOGIN_PREFERENCE_N,
        QR_LOGIN_PREFERENCE_Y,
        MCC_CODE_MERCH,
        WALLET_2FA_DISABLED,
        SHOPPERSTOP_CO_BRANDED,
        LINK_SPLIT_SETTLEMENT_AGG,
        AOA_SUBS_UPI,
        Result_Code_Disabled,
        Result_Code_Enable,
        PCF_MERCHANT,
        PG2_CO_THEN_PAY_FULL_TRAFFIC_Y,
        LOGIN_STRIP,
        PAYTM_LOGO,
        MDR_INTL_MERC,
        QR_MERCHANT,
        STS_MID,
        SUBS_ALL_PAYMODES,
        BANK_MANDATE_BM,
        BRAND_BO_DISC_HDFC,
        BRAND_MERCHANT_BO_DISC_HDFC,
        BRAND_BO_DISC_BAJAJ,
        DEACTIVATED_WALLET,
        DTO_FIELDS_ALIPAYMID,
        BRAND_BO_DISC_BAJAJ_HDFC,
        MISMATCHED_INPUT,
        NO_REQUEST_TYPE_MID,
        RESELLER_MID,
        PG2_COP_FULL_TRAFFIC_Y,
        PG2_FF4J_THEIAENABLE_YES_REFUND_NO,
        WALLET_OFFER_ONUS,
        MASKED_MID,
        EDC_SUCCESS_DC,
        LOYALTY_POINT,
        FETCH_PAYMODES_STATUS,
        POSTPAID_TRUE,
        ICON_ON_MERCHANT_TC15,
        ICON_ON_MERCHANT_TC14,
        ICON_ON_MERCHANT_TC05,
        ICON_ON_MERCHANT_TC06,
        ICON_ON_MERCHANT_TC08,
        TXNTIME,
        STAGING_MID,
        THEME_ENABLED,
        THEME_OVERWRITE_ENABLED_1,
        THEME_OVERWRITE_ENABLED_2,
        PG2_COP_FULL_TRAFFIC_Y_Retry,
        COFT_MERCHANT,
        COFT_MERCHANT_3P,
        ADD_NEW_UPI,
        ADD_NEW_UPI_ONUS,
        APOSTROPHE_MERCHANT,
        PG2_ENABLED_CO_THEN_PAY,
        ICICI_COBRANDING_CHILD_MID,
        ICICI_COBRANDING_PARENT_MID,
        PPBL_PAYTMCC_NOVPA,
        LINK_SD_PPI3,
        LINK_SD_PPI1,
        PG2_CO_THEN_PAY_FULL_TRAFFIC_Y_Retry,
        PAYMENT_BUTTON_OFF,
        PAYMENT_BUTTON_ON,
        PAYMENT_BUTTON_DISABLE_OFF,
         SAVE_DEFAULT_SETTINGS,
        SUBS_UI_TEXT,
        MAX_AMOUNT_CHECK,
        Parent_MID_ICICI,
        Child_MID_ICICI,
        ADD_N_PAY_OVERRIDE,
        LINK_TXN_PG2,
        ADDNPAY_OVERRIDE_SUBS,
        EDC_LINK_ONLINE,
        TWOFA_POSTPAID,
        TWOFA_POSTPAID1,
        TWOFA_NO_POSTPAID,
        IOS_UPI_INTENT,
        COP,
        IOS_INTENT_NOPREF,
        AOA_PEON,
        UNIQUE_EMI_PLANS,
        ITEM_LEVEL_PROMO,
        LOCATIONPOPUPTRUE,
        LOCATIONPOPUPFALSE,
        PG2_COP_Full_Traffic_NoRENTPref,
        COFT_ONUS_MERCHANT,
        RESELLER_COBRANDING_MID,
        CHILD_COBRANDING_PREF_ENABLED,
        CHILD_COBRANDING_MID,
        ADDNPAY_LIMIT,
        PG2_CC_DC,
        IRCTC_Country_Code,
        KFS_HDFC,

        Payment_Adapter_Config_PCF_MID,
        Payment_Adapter_Config_MDRPCF_MID,
        Payment_Adapter_Config_MDR_MID,
        PG2_ENABLED_PCF_Platform_MID,
        PG2_ENABLED_MDRPCF_Platform_MID,
        FULL_PG2_TRAFFIC_ENABLED_COP_PCF_TXN_SLAB_MID,
        FULL_PG2_TRAFFIC_ENABLED_COP_MDRPCF_TXN_SLAB_MID,
        FULL_PG2_TRAFFIC_ENABLED_COTP_PCF_PLATFORM_TXN_SLAB_MID,
        FULL_PG2_TRAFFIC_ENABLED_COTP_MDRPCF_PLATFORM_TXN_SLAB_MID,
        PG2_CC_FULL_TRAFFIC_ENABLED,
        PG2_CO_Then_Pay_Full_Traffic_NoRENTPref,
        PG2_CO_Then_Pay_Full_Traffic_NoRentPref,
        Edc_Emi,
        PG2_CC_PG2_ENABLED,
        ADD_N_PAY_FREEZE,
        HYBRID_FREEZE,
        POSTPAID_ERROR_MSG,
        Instrument_Categorization_BOSS,
        CUSTOMISATION_ON_CASHIERPAGE,
        RAILWIRE_PARENT_MID,
        RAILWIRE_CHILD_MID,
        WALLET_ONLY_INSUFFICIENT_BAL,
        COFT_ERROR_MERCHANT_ONUS,
        COFT_ERROR_MERCHANT,
        DARK_THEME,
        Notification_Merchant,
        PAYTM_EXPRESS_ADDMONEY,
        EDC_EMI_MERCH,
        MDR_PCF_MERCH,
        ADD_MONEY_MERCH,
        ADD_MONEY_HDFC,
        BANK_TRANSFER_MERCH,
        ADD_MONEY_WALLET_MERCH,
        ADD_MONEY_ALLPAYMODE_MERCH,
        QR_Subscription_Merchant,
        QR_ENABLED_MERCHANT,
        QR_ENABLED_MERCHANT_JS,
        DCC_PG2_PCF,
        DCC_PG2_USD,
        PENNYDROP,
        ADD_MONEY_SURCHARGE,
        SURCHARGE_FUNDS_FAILURE,
        SUBS_REFUND_FALSE,
        AUTOREFUND_FALSE,
        NON_UPI_MERCHANT,
        UPI_INTENT_RETRY_SUBS,
        PG2_SUBS_NATIVE,
        PG2_SUBS_MF,
        DCC_PG2_MDR,
        PG2_COP,
        THEME_OVERWRITE_ENABLED,
        COBRANDED_DEPRIORITISE_DC,
        PG2_TopUpExpress_Onus,
        PGOnly_PG2_Refund,
        PG2_Refund_auto,
        REFUND_IMPSPGONLY_PG2,
        NATIVE_HYBRID_PG2_Refund,
        FLAT_PCF_Pg2_Refund,
        AddnPay_PG2_Refund,
        PGOnly_Retry_PG2_Refund,
        WalletOnly_PG2_Refund,
        HDFC_UPI_COLLECT_Pg2_Refund,
        PPBLYONLY_PG2_Refund,
        Mapping_PG2_Attribute,
        EMI_DebitCard,
        ZEST_MONEY2,
        Mapping_PG2_AttributeWithoutPaymode,

        Mapping_PG2_AttributeWithoutPaymodes,
        POSTPAID_BLOCK_ALLOW_PREF_N_PG2,
        PG2_COD,
        PG2_PCF_QR,
        PG2_AMEX_EMI,
        PG2_SUBS_MFSIP,

        SUBSCRIPTION_PG2_LATEST_ALL,
        SUBS_QUARTER,
        MF_SIP_NEW_MID,
        Mapping_PG2_MID,
        MID_FETCHLOGOFROMBOSSPANEL,


        Notification_RTDD,
        Subs_HDFC_CheckoutJS,
        NATIVE_PROMO_PEON,
        pushCloseNotify,

        HIGH_PRIORITY_SMS,
        WALLET_DISABLED_UPI_ENABLED,

        HIDE_NB,
        CancelAllowed,
        JUSPAY_PAYMODE,
        EMI_ON_TOKEN,
        ADVANCE_FILTER_BOSS,
        SMS_BIFURCATION_NOTIF,
        MERCHANT_WITH_LIMIS,

        Subs_PCF_fix,
        SIP_PCF_fix,

        FETCH_PAYMODES_STATUS_WITHTXNAMOUNT,
        ZERO_WALLET_BALANCE,
        WALLET_ONLY_MER,
        ZERO_WALL_BAL,
        BANK_MANDATE_NPCI,
        Subscription_Pg2_MID1,
        Subscription_Pg2_MID2,
        Subscription_Pg2_MID3,
        Subscription_Pg2_MID4,
        Subscription_Pg2_MID5,
        MF_SIP_Pg2_MID1,
        MF_SIP_Pg2_MID2,
        MF_SIP_Pg2_MID3,
        ULTIMATE_BENE,
        Prodcode20_101,
        Prodcode20_02,
        PG2_Deals_COTP,
        Attribute_key_mid1,
        Attribute_key_mid2,
        WALINTER_ONLINE_NULL,
        WALINTER_ONLINE_SMALL,
        WALINTER_OFFLINE_SMALL,
        WALINTER_OFFLINE_NULL,
        WALINTER_OFFLINE_BIG,
        WALINTER_OFFLINE_BIG1,
        WALINTER_FF4J_OFF,
        SUBSCRIPTION_PG2_LATEST_ALL_TXN,
        AddNPAYPG2,
        BFF_LAYERED_FPO,
        PG2_Deals_AddnPay,
        SUPERCASH_ONUS,
        SUPERCASH_OFFUS,
        INSTANT_SETTLEMENT_MID1,
        INSTANT_SETTLEMENT_MID2,
        INSTANT_SETTLEMENT_MID3,
        INSTANT_SETTLEMENT_MID4,
        EDC_QR_CIN,
        MAX_AMOUNT_CHECK1,
        COFT_THEIA_ONUS,
        COFT_THEIA_OFFUS,
        CVV_LESS_MID,
        Arn_Mid,
        ICICI_COBRANDING_PARENT,
        ICICI_COBRANDING_CHILD,
        UPI_LITE_CC,
        PAYMENT_BUTTON_ON04,
        UPI_ERROR_MSG,
        TIP_AMOUNT,
        Alternate_ID_Offus,
        Alternate_ID_Onus,
        ADDNPAY_MCC_ADDMONEY,
        ONUS_ADDMONEY_MERCHANT,
        ADDNPAY_MCC_ADDMONEY_FF4J_OFF,
        ADD_MONEY_EXPRESS_HOTFIX,
        ADD_MONEY_EXPRESS_HOTFIX_SUCCESS,
        OFFLINE_WHITELISTED,
        OFFLINE_WHITELISTED_OFF,
        OFFLINE_WHITELISTED_API_Y,
        OFFLINE_WHITELISTED_OFF_API_Y,
        EmiInfo_COP,
        AUTOLOGIN_MID,
        CBR_MID,
        Deferred_Subs_MID,
        Deferred_Subs_MID_1,
        CobrandedFPO,
        PPBL_VAULT_MID,
        PMALL_VAUTL_MID,
        EDC_LINK,
        UPILITE,
        SINGLETXN_LINK,
        EMI_DISCOVERY,
        EMI_NEW_FLOW,
        EMI_OLD_FLOW,
        SUBSCRIPTION_POSTPAID,
        UPI_CC_ADDNPAY,
        UPI_CC_ADDNPAY_OFF,
        UPI_CC_ADDNPAY_NO,
        PURE_HYBRID,
        ZEST_MONEY_MID,
        UPI_INTENT_RETRY_OPTIMISE,
        UPI_INTENT_RETRY,
        AddNpayHybrid,
        HybridCheckoutJS,
        DEALS_PURE_HYBRID,
        STORE_CASH,
        OFFLINE_SMALL_STATIC_LIMIT_MID,
        OFFLINE_NULL_STATIC_LIMIT_MID,
        OFFLINE_BIG_STATIC_LIMIT_MID,
        HPCL,
        ONLINE_STATIC_LIMIT_MID,
        LITE_LIMIT_COTP,
        UPILITE_LIMIT,
        SIMPLIFIED_OFFERS,
        SUBS_Prenotify_MID,
        PGOnly_PCE_MID,
        PCEM_PCE_MID,
        AND_OFFER_MID,
        BAJAJFN_MID,
        EMI_STATUSQUERY,
        TPV_MID,
        GV_SPLIT_ADDMONEY,
        GV_SPLIT_LV,
        GV_SPLIT_OCL_GVC_ENABLED,
        GV_SPLIT_ADDMONEY_GVC_ENABLED,
        GV_SPLIT_LV_GVC_ENABLED,
        GV_SPLIT_PMALL,
        Vendor_MID,
        GlobalOffersSuperCash,
        GlobalOffersDisabledForUPI,
        MID_CLIENT_ID,
        UDF2_LINK,
        ISSUER_CONTROL_MERCHANT,
        FPO_BALANCE_MIGRATION,
        UPICC_BLACKLIST,
        UPICC_BLACKLIST_NEW,
        UPICCADDANDPAY,
        UPICC_ONUSENABLE,
        UPICC_RALES_DISABLE,
        UPICC_ADDNPAY,
        UPICC_ONUS,
        FPO_BALANCE_MIGRATION_FF4J_DISABLED,
        EMI_V1_CHECKOUT,
        WALLET_REACTIVATION_ADDNPAY,
        Mapping_Status_Flag,
        UPI_CC_LIMIT,
        UPI_CC_LIMITUI,
        UPI_PUSH_LIMIT_COTP,
        UPI_PUSH_TXNLIMIT_COTP,
        DIGIPOS_LINK,
        Attribute_key_mid3,
        STORE_CASH_BANKOFFER,
        CCB_ONUS,
        CCB_OFFUS_GVE,
        CCB_OFFUS_TRID_CONFIGURED,
        CCB_OFFUS_TRID_NOT_CONFIGURED,
        SUPER_ROUTER_MERCHANT,
        NOTIFICATION_ONLINE_MERCHANT,
        UI_TEXTMSG_SCANNPAY,
        UI_TEXTMSG_LOGINQR,
        UI_TEXTMSG_LOGINOTP,
        UI_TEXTMSG_LOGINQR_UPI,
        UI_TEXTMSG_LOGINQR_SavedCard,
        UPIINTENT_DISABLE,
        UPIINTENT_DISABLE_ADDNPAY,
        UPIPPI_WALLET_ENABLED_MID,
        UPIPPI_WALLET_UPI_CC_ENABLED_MID,
        UPIPPI_WALLET_DISABLED_UPI_CC_ENABLED_MID,
        UPIPPI_WALLET_DISABLED_UPI_CC_DISABLED_MID,
        UPIPPI_WALLET_ENABLED_UPI_CC_ENABLED_FLAG_OFF_MID,
        UPIPPI_WALLET_ENABLED_FLAG_OFF_MID,
        UPIPPI_WALLET_DISABLED_UPI_CC_ENABLED_FLAG_OFF_MID,
        UPIPPI_WALLET_DISABLED_UPI_CC_DISABLED_FLAG_OFF_MID,
        UPIPPI_WALLET_ENABLED_WITH_LIMIT_MID,
        UPIPPI_WALLET_ENABLED_WITH_LIMIT_UPI_CC_ENABLED_MID,
        UPIPPI_WALLET_ENABLED_WITH_LIMIT_UPI_CC_ENABLED_WITH_LIMIT_MID,
        UPIPPI_WALLET_ENABLED_COTP_MID,
        UPIPPI_WALLET_UPI_CC_ENABLED_COTP_MID,
        UPIPPI_WALLET_DISABLED_UPI_CC_ENABLED_COTP_MID,
        WALLET_DISABLED_MID,
        ADDANDPAY_AND_CC_DISABLED_MID,
        ADDANDPAY_DISABLED_MID,
        CC_DISABLED_MID,
        UPI_COLLECT_LIMIT,
        UPI_INTENT_LIMIT,
        UPI_PPI_WALLET,
        UPI_CC_PPI_WALLET_Eligibility,
        UPI_COLLECT_INTENT_LIMIT,
        UPI_OVERALL_LIMIT,
        NOTIFICATION_ONLINE_MERCHANT_PUSH,
        EDC_QR_MERCHANT,
        Mapping_PG2_MID_ENTITY_EDC_CHANNEL_INFO_MID_POSITIVE,
        ISSUER_TOKEN_3P,
        ISSUER_TOKEN_3P_DISABLE,
        BAJAJFN_MID_DBD,
        PG_MID_CLIENT,
        DLF_PEON_MERCHANT,
        CREDITLINE_ELIGIBILITY,
        CREDITLINE_WALLET_ELIGIBILITY,
        CREDITLINE_ELIGIBILITY_LOCALE,
        UPI_CREDITLINE_LIMIT,
        UPI_CREDITLINE_WALLET_LIMIT,
        UPI_CREDITLINE_LIMIT_LOCALE,
        UPI_SUPERCASH,
        UPI_SUPERCASH_FF4J_DISABLE,
        UPI_SUPERCASH_ONLINE,
        UPI_CREDITLINE_ELIGIBLE,
        UPI_CREDITLINEANDWALLET_ELIGIBLE,
        UPI_CREDITLINE_LOCALE,
        UPI_PUSH_INTENT,
        UPI_PUSHEXPRESS_DISABLE,
        CUST_ID_PROMO,
        APPLY_OFFER_MID,
        YES_BANK_SQR,
        AXIS_BANK_SQR,
        CHAT_BE_NOTIFICATION_MID,
        P4B_NOTIFICATION_MID,
        HDFC_Subs_Collect,
        AXIS_BANK_WEB_DQR,
        UPI_INTENT_MWEB,
        LINK_MID,
        RISK_REFUND_MID,
        LINK_SUBS_MID,
        SHORTCUT_OFFUS,
        SHORTCUT_ONUS,
        DCC_MID,
        MDR_PCF_DCC_MID,
        LITE_DETAIL_MID,
        LITE_DETAIL_DQR,
        OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_ENABLED_MID,
        OFFLINE_SMALL_UPI_CC_RISK_BLACKLIST_DISABLED_MID,
        OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_ENABLED_MID,
        OFFLINE_NULL_UPI_CC_RISK_BLACKLIST_DISABLED_MID,
        OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_ENABLED_MID,
        OFFLINE_BIG_UPI_CC_RISK_BLACKLIST_DISABLED_MID,
        ONLINE_UPI_CC_RISK_BLACKLIST_ENABLED_MID,
        ONLINE_UPI_CC_RISK_BLACKLIST_DISABLED_MID,
        ONUS_UPI_CC_RISK_BLACKLIST_ENABLED_MID,
        ONUS_UPI_CC_RISK_BLACKLIST_DISABLED_MID,
        ONLINE_ONUS,
        OCIL_YES,
        ONUS_NO_UPI_SUBPAYMODE_PREF_ENABLED,
        MCC_5816_MID,
        MCC_5816_MID_FF4J_OFF,
        MCC_4814_MID,
        MCC_4814_MID_FF4J_OFF,
        EMI_EDC_LINK_MID,
        EMI_EDC_LINK_MID_NEW,
        ACQUIRING_REFUND_SYSTEM_ERROR_MID,
        BIN_IN_RESPONSE,
        PCF_MERCHANT1,
        DCC_IAXI_MID,
        PCF_PLATFORM_MID,
        EMI_REG_CUSTOM_DISABLE,
        EMI_REG_CUSTOM_ENABLE,
        EMI_REG_CUSTOM_DISABLE_MASTER,
        EMI_REG_CUSTOM_ENABLE_MASTER,
        EMI_REG_CUSTOM_DISABLE_DINERS,
        EMI_REG_CUSTOM_ENABLE_DINERS,
        EMI_REG_6DIGIT_BIN,
        EMI_REG_8DIGIT_BIN,
        EMI_REG_PCF,
        EMI_REG_MINIMAL_PROMO,
        EMI_REG_MINIMAL_SUBVENTION,
        EMI_REG_MINIMAL_PROMO_SUBVENTION,
        OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID,
        OFFLINE_SMALL_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID,
        OFFLINE_SMALL_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID,
        OFFLINE_SMALL_CREDITLINE_ON_UPI_RAILS_ENABLED_MID,
        OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID,
        OFFLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID,
        OFFLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID,
        OFFLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID,
        ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_ENABLED_MID,
        ONLINE_BIG_UPI_CREDITLINE_SUPERBLACKLISTED_DISABLED_MID,
        ONLINE_BIG_UPI_CREDITLINE_BLACKLISTED_ENABLED_MID,
        ONLINE_BIG_CREDITLINE_ON_UPI_RAILS_ENABLED_MID,
        UPICC_DISABLED_SMALL_MERCHANT,
        UPICC_DISABLED_BIG_MERCHANT,
        UPI_PPIWALLET_DIABLED_BIG_MERCHANT,
        UPI_PPIWALLET_DIABLED_SMALL_MERCHANT,
        CREDITLINE_DISABLED_SMALL_MERCHANT,
        CREDITLINE_DISABLED_BIG_MERCHANT,
        OFFLINE_MID_VALIDATE_VPA,
        ALL_SUBTYPE_DISABLED_MERCHANT,
        DCC_PR_MID,
        EMI_REG_FPO_UPI_CC_LITE,
        EMI_REG_RESTRICT_OFFER,
        DEALS_FLOW_MID,
        EMI_REG_UPI_OFFERS_VPA_MID,
        POS_ID_OCIL_YES_PTYES_MID_VPA,
        POS_ID_OCIL_YES_PTYES_MID_WHATSAPP,
        CALLBACK_IN_THEIA_ORDERPAY,
        CALLBACK_NOT_IN_THEIA_ORDERPAY,
        UPI_VOUCHER_ENABLED_MID,
        MDR_FEE_ON_UPI_SUBTYPE,
        MDR_PLATFORM_FEE_ON_UPI_SUBTYPE,
        PCF_FEE_ON_UPI_SUBTYPE,
        PCF_PLATFORM_FEE_ON_UPI_SUBTYPE,
        MDR_PCF_FEE_ON_UPI_SUBTYPE,
        MDR_PCF_PLATFORM_FEE_ON_UPI_SUBTYPE,
        CONVENIENCE_FEE_ON_UPI_SUBTYPE,
        PLATFORM_FEE_ON_UPI_SUBTYPE,
        CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE,
        EMI_REG_HIDE_SAVED_CARD_MID,
        MDR_PCF_FLAT_FEE_ON_UPI_SUBTYPE,
        TPV_MERCHANT,
        Axis_OTM,
        AXIS_SBMD_ONDEMAND,
        AXIS_SBMD_ONDEMAND_SIP,
        NON_TPV_MERCHANT,
        ALLOWED_TPAP_MERCHANT,
        ALLOWED_TPAP_MERCHANT_PPSL,
        HDFC_ISSUER_TOKEN_GUEST_CHECKOUT,
        AXIS_ISSUER_TOKEN_GUEST_CHECKOUT,
        ICICI_ISSUER_TOKEN_GUEST_CHECKOUT,
        BAJAJFN_CARDLESS,
        DQR_UPI_ACQ_ID_IN_DEEPLINK,
        DQR_UPI_ACQ_ID_IN_DEEPLINK_FF4J_OFF,
        UPI_ONLY,
        // AI-Generated: 2025-01-02 - Refactoring: Converted MID constants to all caps for consistency
        DCC_CONVERSION_RATE_MID,
        REQAUTH_FLOW_UPI_ACQ_ID_MID,
        REQAUTH_FLOW_UPI_CONFEE_MID,
        DCC_CONVERSION_RATE_MID_CITY_ZIP_CODE,
        REQAUTH_FLOW_UPI_SUBSCRIPTION_MID,
        DCC_CONVERSION_RATE_MID_ONLY_ZIP_CODE,
        MDR_FEE_ON_HDFC_UPI_SUBTYPE,
        PCF_FEE_ON_HDFC_UPI_SUBTYPE,
        PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE,
        MDR_PCF_FEE_ON_HDFC_UPI_SUBTYPE,
        MDR_PCF_PLATFORM_FEE_ON_HDFC_UPI_SUBTYPE,
        MGV_MERCHANT,
        EDC_INSTA_ERROR_MID,
        DCC_SWIFT_MID,
        MCC_MID,
        PLE_DEALS_MID,
        FULL_SWIPE_OFFER_MID,
        PTAB_CREDITLINE_MID,
        PRE_AUTH_CAPTURE_MID,
        Payment_Links_MID,
        INSTA_NB_MID,
        KYB_LIMIT_MID,
        PAYMENT_LINKS_MID_UPI,
        SUPER_PREMIUM_COP,
        PREMIUM_COP,
        STANDARD_COP,
        OTHERS_COP,
        SUPER_PREMIUM_COTP,
        PREMIUM_COTP,
        STANDARD_COTP,
        OTHERS_COTP,
        PREMIUM_COP_PCF,
        import_crossborder,
        OFFERS_MID,
        UPI_PTAB_MID,
        PAYMENT_LINKS_OFFLINE_MID_UPI,
        Native_MF_ParentA,
        Native_MF_ChildA1;
        private String mId, mKey, pCode_001,mVpa;

        private static Map<String, MerchantType> midMapping= new HashMap<>();
        static {
            PGPBaseTest.getMerchants();
            PGPBaseTest.MERCHANT_MAP.forEach((K,V)->{
                String mid= null != V && null != V.get("id")? V.get("id").toString() : "";
                midMapping.put(mid, MerchantType.valueOf(K));
            });
        }

        MerchantType() {
        }

        public static MerchantType getByMid(String mid){
            return midMapping.get(mid);
        }


        public String getId() {
            Map<String, Object> merchantMap = PGPBaseTest.MERCHANT_MAP.get(name());
            this.mId = null != merchantMap && null != merchantMap.get("id") ? merchantMap.get("id").toString() : "";
            return mId;
        }
        public String getProductCode() {
            Map<String, Object> merchantMap = PGPBaseTest.MERCHANT_MAP.get(name());
            this.pCode_001 = null != merchantMap && null != merchantMap.get("productCode") ? merchantMap.get("productCode").toString() : "";
            return pCode_001;
        }

        public String getKey() {
            Map<String, Object> merchantMap = PGPBaseTest.MERCHANT_MAP.get(name());
            this.mKey = null != merchantMap && null != merchantMap.get("key") ? merchantMap.get("key").toString() : "";
            return mKey;
        }

        public String getVpa() {
            Map<String, Object> merchantMap = PGPBaseTest.MERCHANT_MAP.get(name());
            this.mVpa = null != merchantMap && null != merchantMap.get("vpa") ? merchantMap.get("vpa").toString() : "";
            return mVpa;
        }

        public String getProperty(String propertyName) {
            Map<String, Object> merchantMap = PGPBaseTest.MERCHANT_MAP.get(name());
            return null != merchantMap && null != merchantMap.get(propertyName) ? merchantMap.get(propertyName).toString() : "";
        }

        public Map<String, String> getPreferences(){
            Map<String, Object> merchantMap = PGPBaseTest.MERCHANT_MAP.get(name());
            if(null == merchantMap || null == merchantMap.get("preferences"))
                return Collections.EMPTY_MAP;
            List prefList = (List) merchantMap.get("preferences");
            if(!prefList.isEmpty())
                return (Map) prefList.get(0);
            return Collections.EMPTY_MAP;
        }

        public Map<String, String> getExtendInfo(){
            Map<String, Object> merchantMap = PGPBaseTest.MERCHANT_MAP.get(name());
            if(null == merchantMap || null == merchantMap.get("extendInfos"))
                return Collections.EMPTY_MAP;
            List prefList = (List) merchantMap.get("extendInfos");
            if(!prefList.isEmpty())
                return (Map) prefList.get(0);
            return Collections.EMPTY_MAP;
        }


    }

    public enum INFO {
        PAYMENT_DECLINED_BY_BANK("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        private final String infoMsg;

        INFO(String infoMsg) {
            this.infoMsg = infoMsg;
        }

        @Override
        public String toString() {
            return infoMsg;
        }
    }

    public enum PayMode {
        COD("COD"),
        CC("Credit Card"),
        CC_WITH_SAVECARD("CC_WITH_SAVECARD"),
        DC("Debit Card"),
        DC_WITH_SAVECARD("DC_WITH_SAVECARD"),
        NB("Net Banking"),
        IMPS("IMPS"),
        ATM("ATM"),
        EMI("EMI"),
        SAVED_CARD("Saved Details"),
        SAVED_UPI("Saved Details"),
        UPI("UPI"),
        WALLET("Wallet"),
        WALLET_PASSCODE("WalletPasscode"),
        PAYTM_DIGITAL_CARD("Paytm Digital Card"),
        DC_WITH_ATMPIN("DC with ATM pin"),
        UPI_PUSH("Paytm UPI"),
        PPBL("Paytm Payments Bank"),
        EMI_SAVED_CARD("EMI Saved Card"),
        POSTPAID_ONBOARDING("Paytm Digital Card"),
        ADVANCE_DEPOSIT_ACCOUNT("Paytm Advance Deposit"),
        ZEST("ZEST money"),
        MGV("Merchant Gift Voucher"),
        BANK_MANDATE("BANK_MANDATE"),
        SUBS_PPBL_MID("WALLET"),
        SAVED_BANK_MANDATE("SAVE_BANK_MANDATE"),
        EMI_DC("EMI"),
        CC_WITH_SINGLE_PAYMODE("CC with single paymode"),
        UPI_WITH_SINGLE_PAYMODE("UPI with single paymode"),
        NB_WITH_SINGLE_PAYMODE("NB with single paymode");

        private final String paymentMode;

        PayMode(String paymentMode) {
            this.paymentMode = paymentMode;
        }

        @Override
        public String toString() {
            return paymentMode;
        }
    }

    public enum ValidationType {
        NOT_PRESENT,
        EMPTY,
        NON_EMPTY
    }

    public enum Bank {
        HDFC("HDFC"),
        HDFCSC("HDFC"),   //Saved Card HDFC Bank Name in Response
        AXIS("Axis Bank"),
        AXISSC("Axis Bank"),   //Saved Card Axis Bank Name in Response
        HDFC_ONLY("HDFC"),
        ICICI("ICICI"),
        ICICINB("ICICI Bank"),
        ICI0("ICIO"),
        SBI("SBI"),
        SBI_FULL("State Bank of India"),
        BOB("BOB"),
        BAJAJFN("Bajaj Finserv Ltd."),
        BAJAJFNEMI("BAJAJ FINSERV EMI CARD"),
        AMEX("American Express"),
        ZEST("ZEST"),
        ZESTNB("ZestMoney"),
        PPBL("PPBL"),
        HDFCBANK("HDFC Bank"),
        PPBT("PPBT"),
        HDFU("HDFU"),
        NKMB("NKMB"),
        CANARA("CANARA"),
        INDS("INDS");

        private final String bankName;

        Bank(String bankName) {
            this.bankName = bankName;
        }

        @Override
        public String toString() {
            return bankName;
        }
    }

    public enum Gateway {
        HDFC("HDFC"),
        HDFO("HDFO"),
        HDDO("HDDO"),
        ICICI("ICICI"),
        ICICO("ICICI Bank"),
        SBI("SBI"),
        BOB("BOB"),
        PAYTMCC("PAYTMCC"),
        PPBLC("PPBLC"),
        BAJAJFN("BAJAJFN"),
        ZEST("ZEST"),
        AMEX("AMEX"),
        PPBL("PPBL"),
        ICIE("ICIE"),
        KOTAK("NKMB"),
        PAYTM("PAYTM"),
        IHDF("IHDF"),
        PPBEX("PPBEX"),
        WALLET("WALLET"),
        PPBT("PPBT"),
        YESSQR("YESFC1IN"),
        AXIF("AXIF"),
        PTYBLI("PTYBLI"),
        PTYBLC("PTYBLC"),
        PTYES("PTYEC1IN"),
        PTAB("PTAB"),
        HDFU("HDFU");


        private final String gatewayName;

        Gateway(String gatewayName) {
            this.gatewayName = gatewayName;
        }

        @Override
        public String toString() {
            return gatewayName;
        }
    }

    public enum Channelcode {
        HDFC("HDFC Bank"),
        BAJAJFN("BAJAJFN");

        private final String channelCode;

        Channelcode(String channelCode) { this.channelCode = channelCode; }

        @Override
        public String toString() {return channelCode;}
    }


    //saved API end points
    public static class savedCard {
        public static final String SAVEDCARD_OPEN_API_SERVICE = "/savedcardservice/savedcardOpenAPIService";
        public static final String SAVEDCARD_OPEN_API_SERVICE_BY_CARDTYPE_SSOTOKEN = "/savedcardservice/savedcardOpenAPIService/savedcardsBycardTypeSsoToken";
        public static final String SAVEDCARD_SERVICE_BASE_URL_V1 = "/savedcardservice/savedCardService/v1";
        public static final String SAVEDCARD_DELETE_SAVEDCARDBYTOKENTYPE="/savedcardservice/savedcardOpenAPIService/deleteSavedCardByTokenType";
        public static final String GET_SAVED_CARD_BY_CARD_ID = "/get/savedcard/userId/cardId/{userId}/{cardId}";
        public static final String GET_SAVED_CARD_BY_USER_ID = "/get/savedcard/userId/{userId}";
        public static final String SAVE_TRUSTED_CARD_DETAIL = "/add/savedcard/trustedCard";
        public static final String SAVE_CARD_DETAILS_IN_CACHE = "/add/savedcard/cache";
        public static final String SAVE_TRANSACTION_CARD_DETAIL = "/add/savecard/transaction";
        public static final String SAVE_CARD_BY_USER_ID = "/add/savedcard/userId";
        public static final String SAVE_CARD_BY_MID_CUSTID = "/add/savecard/mId/custId";
        public static final String GET_SAVED_CARD_BY_USER_ID_AND_STATUS = "/get/savedcard/userId/status/{userId}/{status}";
        public static final String GET_MID_CUSTID_USERID_CARD_DETAIL = "/get/savecard/mId/custId/userId/{mId}/{custId}/{userId}";
        public static final String DELETE_SAVEDCARD_BY_CARDID_USERID_MID_CUSTID = "/delete/savedcard/cardId/userId/mId/custId/{cardId}/{userId}/{mId}/{custId}";
        public static final String DELETE_SAVED_CARD_BY_USERID_AND_CARD_ID = "/delete/savedcard/userId/cardId/{userId}/{cardId}";
        public static final String DELETE_CACHE_CARD_DETAIL = "/delete/savedcard/cache/{transactionId}";
        public static final String GET_SAVEDCARD_ON_SSOTOKEN = "/get/savecard/ssoToken/{ssoToken}";
        public static final String GETSAVEDCARDBYTOKENTYPE = "/v1/savedcardsByTokenType";
        public static final String GET_SAVEDCARD_BY_MID_CUSTID_USERID_AND_CARDID = "/get/savecard/cardId/mId/custId/userId/{cardId}/{mId}/{custId}/{userId}";
        public static final String GET_SAVEDCARD_ON_CUSTID_MID = "/get/savedcard/custId/mId/checkSum/{custId}/{mId}/{checkSum}";
        public static final String GET_SAVEDCARD_ON_MID_CUSTID_SSOTOKEN = "/get/savecard/custId/mId/ssoToken/checkSum/{custId}/{mId}/{ssoToken}/{checkSum}";
        public static final String DELETE_SAVEDCARD_ON_CUSTID_MID_CARDID = "/delete/savedcard/custId/mId/cardId/checkSum/{custId}/{mId}/{cardId}/{checkSum}";
        public static final String DELETE_SAVEDCARD_ON_SSOTOKEN_CARDID = "/delete/savedcard/ssoToken/cardId/{ssoToken}/{cardId}";

        public static final String MERCHANT_V1_GET_SAVEDCARD = "/savedcardservice/merchant/v1/get/card";
        public static final String HANDLER_INTERNAL_BIN_INFO = "/HANDLER_INTERNAL/BIN_INFO";
        public static final String GET_PG_SAVEDCARD_ON_CUSTID_MID = "/savedcardservice/savedCardService/pg/get/savedcard/custId/mId/{custid}/{mid}";
        public static final String REFUND_ACCOUNT_ADD = "/savedcardservice/refund/account/add";
        public static final String REFUND_ACCOUNT_QUERY = "/savedcardservice/refund/account/query";
        public static final String REFUND_ACCOUNT_REMOVE = "/savedcardservice/refund/account/remove";

        public static final String SAVEDCARD_SERVICE_HANDLER_INTERNAL_BIN_INFO = "/savedcardservice/HANDLER_INTERNAL/BIN_INFO";
        public static final String SAVEDCARD_SERVICE_HANDLER_INTERNAL_DELETE_BIN = "/savedcardservice/HANDLER_INTERNAL/DEL_BIN";
        public static final String SAVEDCARD_SERVICE_MERCHANT_COFT_CARDS = "savedcardservice/merchant/coft/cards";
        public static final String SAVEDCARD_SERVICE_CARD_DELETE = "savedcardservice/card/delete";
        public static final String SAVEDCARD_SERVICE_USER_COFT_CARDS = "savedcardservice/user/cards";
        public static final String SAVECARD_SERVICE_GET_GCIN = "savedcardservice/savedcardOpenAPIService/v1/getGcin";
        public static final String GET_BIN_BULK_HASH = "/savedcardservice/savedcardOpenAPIService/card/getBinBulkHash";

        public static final String CARD_CENTER_FETCH_ALL = "user/cards/v2/fetch-all";
        public static final String DELETE_USER_CARD= "/user/cards/delete";
    }

    public static class PagePath {
        public static final String COMMON_CHECKOUT_PAGE_PATH = "/theia/merchantcheckout";
        public static final String COMMON_CASHIER_PAGE_PATH = "theia/processTransaction";
        public static final String COMMON_RESPONSE_PAGE_PATH = "/MerchantSite/bankResponse";
        public static final String FETCH_DCC_PAGE_PATH = "/theia/fetchDccPage?mid={mid}&orderId={orderId}";
        public static final String FETCH_DCC_BANKPAGE_PATH = "/dcc/redirectICICIPay";
    }

    public static class WalletAPIResourcePath {
        public static final String GET_BALANCE = "/wallet-web/checkBalance";
        public static final String WITHDRAW = "/wallet-web/withdraw";
        public static final String ADD_MONEY_CASH = "/wallet-web/addMoneyCash";
        public static final String BLOCK_AMOUNT = "/wallet-web/blockDisputeTxn";
        public static final String TRANSIT_CHECKBALANCE = "/service/checkUserBalance";
        public static final String TRANSIT_WITHDRAW = "/wallet-web/v1/debit";
        public static final String TRANSIT_ADDFUNDSTOSUBWALLET = "/wallet-web/addFundsToSubWallet";
        public static final String CONFIRMATION_URL = "/wallet-web/paymentConfirmation";
    }

    public static class SavedCardAPIResourcePath {
        public static final String SAVE_CARD_DETAILS = "/savedcardservice/savedCardService/v1/add/savedcard/trustedCard";
        public static final String FETCH_SAVED_CARDS = "/savedcardservice/savedCardService/v1/get/savedcard/userId/";
        public static final String DELETE_SAVED_CARD = "/savedcardservice/savedCardService/v1/delete/savedcard/userId/cardId/";
    }

    public static class MappingService {
        public static final String MERCHANT_EXTENDED_INFO = "/mapping-service/merchant/get/extended/info/v3/{mid}";
        public static final String GET_CONTRACT_PAYMENT_INFO = "/mapping-service/common/v1/get/contract/paymentInfo/{mid}/BALANCE/{contractID}";
        public static final String LOOK_UP_PAYMENT_MODE = "/mapping-service/get/lookup/PAYMENT_MODE/{paymodeType}";
        public static final String GET_MERCH_PREFERENCE_INFO = "/mapping-service/merchant/get/preference/info/{mid}";
        public static final String GET_ENTITY_URL_INFO = "/mapping-service/get/entityurlinformid/{mid}/{reqType}/{websiteName}";
        public static final String GET_ENTITY_URL_INFO_V2 = "/mapping-service/get/entityurlinformid/v2/{mid}/{reqType}/{websiteName}";
        public static final String GET_PAYTM_PROPERTIES = "/mapping-service/get/paytmproperties/{propName}";
        public static final String GET_ENTITY_OFFER_DETAIL = "/mapping-service/get/entityofferdetailsformid/{mid}/{channel}/{websiteName}";
        public static final String GET_BANK_DETAILS = "/mapping-service/get/bankdetails/v1/{bankCode}";
        public static final String ADD_MERCHANT_PREFRENCE_INFO_EXT = "/mapping-service/merchant/add/preferenceinfosext/info/{mid}";
        public static final String GET_MERCHANT_PREFRENCE_INFO_EXT = "/mapping-service/merchant/get/preferenceinfosext/{mid}";
        public static final String ADD_MERCHANT_PREFRENCE_INFO = "/mapping-service/merchant/add/preference/info";
        public static final String MERCHANT_PROFILE = "/mapping-service/merchant/profile/{mid}/{id}";
        public static final String MERCHANT_ATTRIBUTE_PREFERENCE = "/mapping-service/merchant/attribute/preference/{mid}/{id}";
        public static final String MERCHANT_ATTRIBUTE_UPIKEYS = "/mapping-service/merchant/attribute/merchantUpiKeys/{mid}";
        public static final String GET_USER_V1 = "/mapping-service/user/v3/{id}/{user}";
        public static final String MERCHANT_MIGRATION_DETAILS = "/mapping-service/query/merchant/migration/details/{mid}/true";
        public static final String MERCHANT_ATTRIBUTE_KEY = "/mapping-service/merchant/attribute/key/{mid}/{id}";
        public static final String GET_MERCHANT_V1 = "/mapping-service/merchant/v3/{id}/{mid}";
        public static final String GET_MERCHANT_IDMAP = "/mapping-service/merchant/idmap/v3/{mid}/{id}";
        public static final String MERCHANT_CONTRACT_DETAILS = "mapping-service/query/merchant/contract/detail/{contractId}";
        public static final String MERCHANT_VENDOR_DETAILS = "/mapping-service/migration/migrate/merchant";
        public static final String GET_MERCHANT_REQUEST_TYPES = "/mapping-service/get/merchantRequestTypes/{midType}/{paytmMid}";
        public static final String GET_MERCHANT_AGENTINFO = "/mapping-service/merchantAgent/get/agentInfo/{id}/{type}";
        public static final String REMOVE_REDIS_KEY = "/mapping-service/common/v1/remove/{redisKey}";
        public static final String GET_RESPONSE_CODE = "/mapping-service/get/responsecodedetails/{paytmResponseCode}";
        public static final String GET_MERCHANT_LOGO_INFO_FROM_ALIPAY_MID = "/mapping-service/get/merchantLogoInfo/v1/{oldpgMid}";
        public static final String GET_MERCHANT_KYC_INFO = "/mapping-service/get/merchantKycInfo/{merchantId}";
        public static final String GET_DYNAMIC_WRAPPER_CONFIGS = "/mapping-service/get/dynamicWrapperConfigs";
        public static final String GET_CUSTOM_PAYLOAD_DATA = "/mapping-service/get/customPayload/{merchantId}";
        public static final String GET_BANK_DETAILS_LIST_FROM_BANK_CODES = "/mapping-service/get/banksdetailslistfromCodes/v1/{bankCodes}";
        public static final String GET_MERCHANT_LOGO_INFO_FROM_MID = "/mapping-service/get/merchantlogoinfo/v1/{merchantId}";
        public static final String GET_MERCHANT_LOGO_INFO_FROM_MID_V2 = "/mapping-service/get/merchantlogoinfo/v2/{merchantId}";
        public static final String GET_AGGREGATOR_BANK_DETAILS = "/mapping-service/get/aggregator/{bankId}/{entityId}";
        public static final String GET_FORMATTER = "/mapping-service/get/formatter/{bankCode}/{payMethod}";
        public static final String GET_BANK_URL_INFO = "/mapping-service/get/bankurlinfo/{bankId}/{payMethodId}/{channelId}";
        public static final String GET_BANK_DETAILS_FROM_ID = "/mapping-service/get/bankdetailsfromid/v1/{bankId}";
        public static final String GET_BANK_DETAILS_LIST_FROM_ID = "/mapping-service/get/banksdetailslistfromids/v1/{bankIds}";
        public static final String GET_PAY_METHOD_DETAILS = "/mapping-service/get/paymethodDetails/payMethod";
        public static final String GET_BANKS_DETAILS = "/mapping-service/get/bankDetails/v1/bank";
        public static final String GET_ALL_BANK_DETAILS = "/mapping-service/get/v1/bankmasterdetails";
        public static final String GET_ALL_BANK_DETAILS_BY_PAY_MODE = "/mapping-service/get/bankmasterdetails/v1/paymode/{payMode}";
        public static final String GET_MERCHANT_API_URL_INFO="mapping-service/get/merchantapiurlinfo/{PLATFORM}/{MID}";
        public static final String GET_CARD_NETWORK_DETAILS = "/mapping-service/get/cardNetworkDetails/cardNetwork";
        public static final String GET_LOOK_UP = "/mapping-service/get/lookup/v1/{category}/{channelName}";
        public static final String GET_LOOKUP_FROM_ID = "/mapping-service/get/lookupfromid/v1/{id}";
        public static final String GET_LOOKUP_DATA_FROM_ID = "/mapping-service/get/lookupdatafromid/{id}";
        public static final String GET_LOOK_UP_DATA_FROM_CATEGORY_AND_SUB_CATEGORY = "/mapping-service/get/lookupdatafromcategory/{category}/{subCategory}";
        public static final String GET_LOOK_UP_DATA_FROM_CATEGORY_AND_SUB_CATEGORY_AND_NAME = "/mapping-service/get/lookupdatafromcategoryandname/{category}/{subCategory}/{name}";
        public static final String GET_LIMIT_MERCHANT = "/mapping-service/get/limit/merchantType/{key}";
        public static final String GET_FETCH_ENTITY_IGNORE_PARAMS = "/mapping-service/get/fetchEntityIgnoreParams/{entityId}";
        public static final String AOA_MERCHANT_ADD ="mapping-service/v2/merchant/add";
        public static final String AOA_ADD_GATEWAY = "mapping-service/aoa/merchant/gateway/config/add";
        public static final String AOA_ADD_ACQUIRING = "/mapping-service/merchant/v2/add/acquiring";
        public static final String AOA_GET_GATEWAY = "mapping-service/aoa/merchant/get/gateway/config/getByAoaMid/{mid}";
        public static final String ADD_CONTRACT = "/mapping-service/merchant/v2/add/contract";
        public static final String QUERY_ACQUIRING = "/mapping-service/query/merchant/v2/acquiring";
        public static final String DELETE_ACQUIRING = "/mapping-service/merchant/v2/delete/acquiring";
        public static final String UPDATE_EMI ="/mapping-service/merchant/update/emi";
        public static final String GET_EMI_DETAILS ="/mapping-service/query/merchant/emi/{mid}";
        public static final String MERCHANT_MIGRATION_CONTRACT_DETAILS = "mapping-service/query/merchant/migration/contract/details/v2/{mid}";


        public static final String TOKEN_INFO = "/mapping-service/get/bankcard/token/{token}";
        public static final String GET_BRAND_EMI = "/mapping-service/query/merchant/brand/emi/{mid}";
        public static final String REMOVE_CACHE_ATTRIBUTE_PREFERENCE = "/mapping-service/common/v1/remove/ATTRIBUTE_PREF_{p+mid}";
        public static final String GET_TEMPLATE_GLOBAL_CONFIG = "/mapping-service/notification/fetch/template/configuration?clientName=boss-panel";
        public static final String GET_EMAIL_TEMPLATE = "/mapping-service/notification/email/template/config/{mid}?clientName=boss-panel";
        public static final String GET_SMS_TEMPLATE = "/mapping-service/notification/sms/template/config/{mid}?clientName=boss-panel";
        public static final String CREATE_EDIT_EMAIL_TEMPLATE = "/mapping-service/notification/email/template?clientName=boss-panel";
        public static final String CREATE_EDIT_SMS_TEMPLATE = "/mapping-service/notification/sms/template?clientName=boss-panel";
        public static final String VALIDATE_ALIPAY_ID="/mapping-service/get/oldpgid/{mid}";
        public static final String MERCHANT_LIST = "/mapping-service/merchant/v3/paytm/merchantIdList";;
        public static final String FETCH_ALIPAY= "/mapping-service/get/oldpgId/{MID}";
        public static final String V1_GATEWAY= "/mapping-service/v1/gateways";
        public static final String MERCHANT_MPA_BALANCE= "/mapping-service/query/merchant/mpa/balance/{MID}";
        public static final String CONTACT_DETAILS= "/mapping-service/query/merchant/contract/detail/{CONT_DETAIL}";
        public static final String EMI_DC_DETAILS= "/mapping-service/get/emiOnDcEligibilityBy?contact={contact}&bankName={bankName}";
        public static final String MCC_CODE= "/mapping-service/eos/merchant/device/details/tid/bankName/{TID}/{BANK}";
        public static final String RESELLER_MID_DETAIL= "mapping-service/reseller/master/mid/{MID}";
        public static final String FETCH_VALID_BINS = "mapping-service/get/fetchValidBins/{bankId}";
        public static final String FETCH_VALID_BINS_WITH_ISOFFUS = "mapping-service/get/fetchValidBins/{bankId}/{ISOFUS}";
        public static final String Alt_BIN_DETAILS = "/mapping-service/get/bankcard/v1/bin/{bin}";
        public static final String Alt_BIN_DETAILS_API = "/mapping-service/get/bankcard/bin/{bin}";
        public static final String GLOBAL_CONFIG= "mapping-service/get/global/config";
    }

    public static interface VANProxy {
        String VAN_PROXY_PREFIX = "/vanproxy";
        String VAN_PROXY_CREATE = VAN_PROXY_PREFIX + "/api/v1/van";
        String VAN_PROXY_QUERY = VAN_PROXY_PREFIX + "/api/v1/van/query";
        String VAN_PROXY_PAYMENT = VAN_PROXY_PREFIX + "/api/v1/payment/callback/PPBL";
        String VAN_PROXY_SEARCH= VAN_PROXY_PREFIX + "/api/v1/van/search";
        String VAN_PROXY_UPDATE=VAN_PROXY_PREFIX+"/api/v1/van/update";
        String VAN_PROXY_DISABLE=VAN_PROXY_PREFIX+"/api/v1/van/disable";
    }

    public interface BillProxy {
        String BILL_PROXY_URI_PREFIX = "/billproxy";
        String CARD_TOKENIZE_CARD_NO_V1 = BILL_PROXY_URI_PREFIX + "/api/v1/cardNumber/cc/cardTokenize/request";
        String FETCH_BIN_REQUEST_V5 = BILL_PROXY_URI_PREFIX + "/api/v5/cc/fetchBin/request";
        String SAVED_CARD_BY_TOKEN_TYPE = "/savedcardservice/savedcardOpenAPIService/v1/savedcardsByTokenType";
        String CARD_TOKENIZE_CIN = BILL_PROXY_URI_PREFIX + "/api/v1/cin/cardTokenize/request";
        String FETCH_CIN_REQUEST = BILL_PROXY_URI_PREFIX + "/v1/fetch/cin/request";
        String FETCH_BIN = BILL_PROXY_URI_PREFIX + "/v1/fetch/binDetails/binNumber/sso/request";
        String CARD_DETAILS_USING_SSO = BILL_PROXY_URI_PREFIX +"/v2/fetch/cardDetails/cardNumber/sso/request";
        String CARD_DETAILS_USING_CIN = BILL_PROXY_URI_PREFIX +"/v2/fetch/cardDetails/cin/sig/request";
        String CARD_DETAILS_USING_TIN = BILL_PROXY_URI_PREFIX +"/v1/fetch/cardDetails/tin/sig/request";
        String FULFILMENT_USING_CIN=BILL_PROXY_URI_PREFIX +"/v1/fulfilment/cin/request";
        String FULFILMENT_USING_TIN=BILL_PROXY_URI_PREFIX +"/v1/fulfilment/tin/request";
        String ELIGIBILITY_USING_CIN=BILL_PROXY_URI_PREFIX +"/v1/eligibility/cin/request";
        String ELIGIBILITY_USING_TIN=BILL_PROXY_URI_PREFIX +"/v1/eligibility/tin/request";
        String STATUS_QUERY_USING_CIN=BILL_PROXY_URI_PREFIX +"/v1/status/cin/request";
        String STATUS_QUERY_USING_TIN=BILL_PROXY_URI_PREFIX +"/v1/status/tin/request";
        String FETCH_BIN_DETAIL = "/theia/api/v1/fetchBinDetail";

        String SSO_REQUEST_USING_TIN = BILL_PROXY_URI_PREFIX + "/v1/fetch/cardDetails/tin/sso/request";
        String SIG_REQUEST_USING_TIN = BILL_PROXY_URI_PREFIX + "/v1/fetch/cardDetails/tin/sig/request";
    }

    public static class PGPAPIResourcePath {
        public static final String GET_CARD_TOKEN = "/theia/PAYTM_EXPRESS/getCardToken";
        public static final String PROCESS_TXN = "/theia/processTransaction";
        public static final String IVR_FAST_FWD = "/theia/HANDLER_IVR/CLW_APP_PAY";
        public static final String FAST_FWD = "/theia/HANDLER_IVR/CLW_APP_PAY/APP";
        public static final String REFUND = "/refund/HANDLER_INTERNAL/REFUND";
        public static final String MASTERREFUND = "/refund/HANDLER_INTERNAL/MASTERREFUND";
        public static final String MASTER_REFUND_STATUS = "/refund/HANDLER_INTERNAL/MASTER_REFUND_STATUS";
        public static final String REFUND_STATUS = "/refund/HANDLER_INTERNAL/REFUND_STATUS";
        public static final String CLOSE_ORDER_V1= "/theia/api/v1/closeOrder";
        public static final String CLOSE_ORDER_V2 = "/theia/api/v2/closeOrder";
        public static final String FETCH_MERCHANT_CONFIG = "/theia/api/v1/fetchMerchantConfig";
        public static final String UPI_PSP_VALIDATE_VPA="/upi-psp-processor/validate/vpa";
        @Deprecated
        /**
         * This deprecated from pgp_automation as this API does not support checksum.
         */
        public static final String TXNSTATUS = "/merchant-status/HANDLER_INTERNAL/TXNSTATUS";
        public static final String TXN_STATUS_APP = "/merchant-status/HANDLER_INTERNAL/TXNSTATUS/APP";
        public static final String TXN_STATUS_LIST = "/merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST";
        public static final String TXN_STATUS_LIST_APP = "/merchant-status/HANDLER_INTERNAL/TXNSTATUSLIST/APP";

        public static final String ORDER_STATUS = "/merchant-status/v2/order/status";
        public static final String V3_ORDER_STATUS= "v3/order/status";
        public static final String RECOVERY_STATUS = "/merchant-status/v1/recovery/status";
        public static final String V5_ORDER_STATUS = "/merchant-status/v5/order/status";
        public static final String TXNSTATUS_CHECKSUM = "/merchant-status/getTxnStatus";
        public static final String GET_PAYMENT_STATUS = "/merchant-status/api/v1/getPaymentStatus";
        public static final String NATIVE_TXNSTATUS= "/merchant-status/HANDLER_INTERNAL/TXNSTATUS";
        public static final String GET_BILL_CARD_TOKEN = "/theia/PAYTM_EXPRESS/getCardToken/billPayment";
        public static final String GET_LINK = "/link/linkApi/paymentLink/";
        public static final String FETCH_UPI_OPTIONS ="/theia/api/v1/fetchUPIOptions";
        public static final String BIN_DETAILS = "/mapping-service/get/bankcard/v1/bin/";
        public static final String BIN_DETAILS_API = "/mapping-service/get/bankcard/bin/";
        public static final String CREATE_LINK = "/link/create";
        public static final String PAYMENT_SERVICE = "/paymentservices/qr/create";
        public static final String Send_Payment_Request = "/paymentservices/sendpaymentrequest";
        public static final String PS_PRE_AUTH = "/paymentservices/order/v2/preAuth";
        public static final String PS_CAPTURE = "/paymentservices/order/v2/capture";
        public static final String CREATE_TOKEN = "/theia/api/v1/token/create";
        public static final String FETCH_PUBLIC_KEY = "/theia/api/v1/fetchPublicKey";
        public static final String CANCEL_TXN = "/theia/cancelTransaction";
        public static final String GET_LINK_INFO = "/link/linkApi/linkInfo/{linkId}/{linkName}";
        public static final String LINKS_REPORT_DOWNLOAD = "/link/linksReportDownload/";
        public static final String LINK_FETCH = "/link/fetch";

        public static final String Save_Default_Settings = "/link/saveDefaultSettings";
        public static final String FETCH_TRANSACTION = "/link/fetchTransaction";
        public static final String GENERATE_TXN_TOKEN = "/link/generateTxnToken";
        public static final String GET_LINK_DETAIL = "/link/getLinkDetail";
        public static final String EXPIRE_LINK = "/link/expire";
        public static final String ARCHIVE_LINK = "/link/archive";
        public static final String RESENDNOTIFICATION_LINK = "/link/resendNotification";
        public static final String SAVEUPDATETEMPLATE = "/link/form/saveUpdateTemplate";
        public static final String FETCHTEMPLATE = "/link/form/fetchTemplates";
        public static final String DELETEEMPLATE = "/link/form/deleteTemplate";

        public static final String UPDATE_LINK= "/link/update";
        public static final String IS_NEW_MERCHANT_LINK= "/link/isNewMerchant";
        public static final String FETCH_TRANSACTION_LINK= "/link/fetchTransaction";

        public static final String SUMMARY_LINK= "/link/summary";
        public static final String SUBMIT_USER_FORM= "/link/form/submitUserForm";

        public static final String SAVE_OR_UPDATE_LINK="/link/saveDefaultSettings";
        public static final String FETCH_DEFAULT_SETTINGS="/link/fetchDefaultSettings";
        public static final String FETCH_TRANSACTION_V1= "/link/fetchTransaction";
        public static final String GENERATE_QR= "/link/generateQR";

        public static final String ES= "/internal/bsearch";
        public static final String BANK_MANDATE_LIST ="/subscription/mandate/v2/bankList";
        public static final String STATIC_PREFERENCE_API= "/api/v1/merchant/staticpref";
        public static final String SET_PREF_DATA = "/pgmc/setPrefData/";
        public static final String UPI_PSP_PROCESSOR_PAYMENT_STATUS= "/upi-psp-processor/payment/status";
        public static final String UPI_PSP_PROCESSOR_API_V1_CONSULT_FEE= "/upi-psp-processor/api/v1/consult/fee";

    }

    public static class NativeAPIResourcePath {
        public static final String IMEI = "/theia/api/v1/imei";
        public static final String INIT_TXN = "/theia/api/v1/initiateTransaction";
        public static final String NEW_INIT_TXN_INTENT_URL = "/theia/initiate/processTransaction";
        public static final String GUEST_FETCH_PAYMENT_OPTION_V1 = "/theia/api/v1/guest/fetchPaymentOptions";
        public static final String FETCH_PAYMENT_OPTION_V1 = "/theia/api/v1/fetchPaymentOptions";
        public static final String FETCH_PAYMENT_OPTIONS_V2 = "/theia/api/v2/fetchPaymentOptions";
        public static final String FETCH_PAYMENT_OPTIONS_V4 = "/theia/api/v4/fetchPaymentOptions";
        public static final String FETCH_PAYMENT_OPTIONS_V5 = "/theia/api/v5/fetchPaymentOptions";
        public static final String FETCH_BIN_DETAIL = "/theia/api/v1/fetchBinDetail";
        public static final String FETCH_BIN_DETAIL_GUEST = "/theia/api/v1/guest/fetchBinDetail";
        public static final String FETCH_VPA_DETAIL = "/theia/api/v1/fetchVpaDetails";
        public static final String V4_VALIDATE_VPA="/theia/api/v4/vpa/validate";
        public static final String AXIS_INTENT_CALLBACK="/axis/upiIntentTxn";
        public static final String AXIU_INSTA_CALLBACK_URL="/instaproxy/bankresponse/AXIU/UPI/RESPONSE";
        public static final String PTYBLI_INTENT_CALLBACK="/PTYBL_UPI/intent/callback";
        public static final String UPI_PSP_PAYMENT = "/theia/v1/order/pay/upipsp";
        public static final String SEND_OTP = "/theia/api/v1/login/sendOtp";
        public static final String SEND_OTP_V4 = "/theia/api/v4/login/sendOtp";
        public static final String VALIDATE_OTP = "/theia/api/v1/login/validateOtp";
        public static final String VALIDATE_OTP_V2 = "/theia/api/v2/login/validateOtp";
        public static final String LOGOUT_USER_V2 = "/theia/api/v2/logout/user";
        public static final String FETCH_EMI_DETAIL = "/theia/api/v1/fetchEMIDetail";
        public static final String GET_EMI_DETAILS = "/theia/api/v1/getEmiDetails";
        public static final String CHECK_EMI_ELIGIBILITY = "/theia/api/v1/checkEMIEligibility";
        public static final String FETCH_BALANCE = "/theia/api/v1/fetchBalanceInfo";
        public static final String FETCH_PROMO_DETAIL = "/theia/api/v1/fetchPromoCodeDetail";
        public static final String INIT_SUBSCRIPTION = "/theia/api/v1/subscription/create";
        public static final String RENEW_SUBSCRIPTION = "/theia/api/v1/subscription/renew";
        public static final String CANCEL_SUBSCRIPTION = "/subscription/subscription/cancel";
        public static final String FETCH_PCF_DETAIL = "/theia/api/v1/fetchPcfDetails";
        public static final String THEIA_CANCEL_TRANSACTION = "/theia/cancelTransaction?mid={mid}&orderId={orderId}";
        public static final String EMI_SUBVENTION_BANKS = "/theia/api/v1/emiSubvention/banks";
        public static final String EMI_SUBVENTION_TENURE = "/theia/api/v1/emiSubvention/tenures";
        public static final String EMI_SUBVENTION_VALIDATE = "/theia/api/v1/emiSubvention/validateEmi";
        public static final String APPLY_PROMO_V1 = "theia/api/v1/applyPromo";
        public static final String APPLY_PROMO_V2 = "theia/api/v2/applyPromo";
        public static final String ITEM_APPLY_PROMO_V2 = "theia/api/v1/item/level/applyPromo";
        public static final String FETCH_ALL_PAYMENT_OFFERS = "theia/api/v1/fetchAllPaymentOffers";
        public static final String FETCH_CARD_INDEX_NO = "/theia/api/v1/fetchCardIndexNo";
        public static final String FETCH_EMI_PAYMENT_CHANNELS = "/theia/api/v1/fetchEMIPaymentChannels";
        public static final String FETCH_MERCHANT_INFO_V1 = "/theia/api/v1/fetchMerchantInfo";
        public static final String FETCH_MERCHANT_INFO_V2 = "/theia/api/v2/fetchMerchantInfo";
        public static final String FETCH_MERCHANT_USER_INFO = "/theia/api/v1/fetchMerchantUserInfo";
        public static final String FETCH_NB_PAYMENT_CHANNELS = "/theia/api/v1/fetchNBPaymentChannels";
        public static final String FETCH_PAYMENT_PROMOTION_ATTRIBUTES = "/theia/api/v1/fetchPaymentPromotionAttributes";
        public static final String FETCH_QR_PAYMENT_DETAILS = "theia/api/v1/fetchQRPaymentDetails";
        public static final String FETCH_QR_PAYMENT_DETAILS_V2 = "theia/api/v2/fetchQRPaymentDetails";
        public static final String PROCESS_TXN = "/theia/api/v1/processTransaction";
        public static final String SHOW_PAYMENT_PAGE = "/theia/api/v1/showPaymentPage";
        public static final String SUBMIT_KYC = "/theia/api/v1/submitKYC";
        public static final String UPDATE_TXN_DETAIL = "/theia/api/v1/updateTransactionDetail";
        public static final String VALIDATE_AND_FETCH_MERCHANT_INFO = "/theia/api/v1/validateAndFetchMerchantInfo";
        public static final String VPA_VALIDATE = "/theia/api/v1/vpa/validate";
        public static final String FETCH_CARD_DETAILS = "/theia/api/v1/fetchCardDetails";
        public static final String VALIDATE_REF_ID = "/theia/api/validateRefId";
        public static final String CHECKUPI_ACCOUNT = "/theia/api/v1/checkUPIAccountExist";
        public static final String SHOW_LINK_PAYMENT_PAGE = "/theia/api/v1/showLinkPaymentPage?mid={mid}&orderId={orderId}&txnToken={txnToken}";
        public static final String Link_Payment_SendOTP = "/theia/linkPayment/generateSendOTP";
        public static final String Link_Payment_Validate_OTP = "/theia/linkPayment/validateSendOTP";
        public static final String FETCH_USER_PAYMENT_OFFERS = "/theia/api/v1/fetchUserPaymentOffers";
        public static final String APPLY_SUPERCASH_OFFERS="/theia/api/v1/applySupercashOffers";
        public static final String FETCH_USER_PAYMENT_MODES = "/theia/api/v1/fetchUserPaymentModeStatus";
        public static final String MANDATE_PAYMENT_RESP = "/instaproxy/secureresponse/PPBL/BANK_MANDATE/PAYMENT/RESP";
        public static final String MBIDLIMIT_EMIPLAN = "/mapping-service/get/mbidlimit/emiPlan";
        public static final String STS_FUND_TRANSFER = "/sts/settlement/transfer/funds";
        public static final String FETCH_PAYMENT_OPTIONS_LITE = "/theia/api/v1/fetchPaymentOptionsLite";
        public static final String FETCH_PSP_APPS = "/theia/api/v1/fetchPspApps";
        public static final String OFFER_APPLY = "/theia/api/v1/offerApply";
        public static final String OFFER_DISCOVERY = "/theia/api/v1/offerDiscovery";
        public static final String THEIA_V1_GENERATEESN="/theia/v1/generateEsn";
        public static final String THEIA_ORDER_PAY="/theia/api/v1/order/pay";
        public static final String V1_Lite_MerchantDetails="/theia/api/v1/lite/merchantDetails";
        public static final String INSTAPROXY_SECURERESPONSE_PTYBLI_UPI_PUSH_RESP="/instaproxy/secureresponse/PTYBLI/UPI/PUSH/RESP";
        public static final String INSTAPROXY_SECURERESPONSE_PTYL_UPI_PUSH_RESP="/instaproxy/secureresponse/PTYL/UPI/PUSH/RESP";
        public static final String INSTAPROXY_SECURERESPONSE_HDFC_UPI_RESP="instaproxy/bankresponse/HDFC/UPI/RESP";
        public static final String THEIA_API_V1_QR_CREATE="/theia/api/v1/qr/create";


    }

    public static class Refund {
        public static final String ASYNC_REFUND = "/refund/api/v1/async/refund";
        public static final String REFUND_STATUS = "/refund/api/v1/refundStatus";
        public static final String REFUND_ACCOUNT_VALIDATE = "/refund/api/v1/account/validate";
        public static final String REFUND_VIEW_CONSULT = "/refund/view/consult";
        public static final String SYNC_REFUND =  "/refund/api/v1/refund/apply/sync";
        public static final String REFUND_REVERSAL = "/refund/api/v1/reversal";

        public static final String CUSTOM_REFUND= "/refund/api/v1/refund/apply/custom/sync";
    }

    public static class QR {
        public static final String GENERATE_QR = "/qrcode/v4/generateQrCode";
        public static final String FETCH_QR = "/qrcode/getQRCodeInfo";
        public static final String EDIT_QR = "/qrcode/v4/editQrCodeDetails ";

    }

    public static class Mockbank {
        public static final String AUTO_REFUND_PEON = "/mockbank/autorefund/peon";
        public static final String SMS_PRIMARY = "/mockbank/primary/sms";
        public static final String REFUND_SUCCESS_PEON = "/mockbank/refundsuccessnotify/peon";
        public static final String UPI_PREDICATE_URL = "/mockbank/predicate/upi/user/profile";
        public static final String UPI_PREDICATE_GETACCOUNTDETAILS = "/mockbank/predicate/get/getdetailbyaccountid";
    }

    public static class NotificationService {
        public static final String WITHDRAW_NOTIFY = "/pgproxy-notification/alipayplus/fund/merchant/withdrawNotify";
        public static final String REALTIME_SETTLEMENT_NOTIFY = "/pgproxy-notification/alipayplus/settlement/realtime/merchantNotify";
        public static final String GET_TEMPLATE_BODY = "/mapping-service/notification/template/serviceType/SETTLEMENT/category/{category}/notificationType/{notifType}/recipient/MERCHANT/txnStatus/{txnStatus}";
        public static final String GIFTVOUCHER_EXPIRY = "/pgproxy-notification/alipayplus/promotion/giftvoucher/expirynotify";
        public static final String IVR_NOTIFY = "/pgproxy-notification/external/ext/communication/ivr/notify";
        public static final String LOYALITY_POINTS_EXPIRY = "/pgproxy-notification/alipayplus/promotion/loyaltypoints/expiryNotify";
        public static final String REFUND_SUCCESS_NOTIFY= "/pgproxy-notification/alipayplus/acquiring/refund/refundSuccessNotify";
        public static final String ARN_NOTIFY="/pgproxy-notification/pg2/arnUpdateNotify";
        public static final String CAPTURE_NOTIFY="/pgproxy-notification/oldpg/acquiring/order/captureNotify";
        public static final String DEDUCTION_NOTIFY="/pgproxy-notification/oldpg/acquiring/order/deductionNotify";
     //   public static final String VOID_NOTIFY="/pgproxy-notification/oldpg/acquiring/order/voidNotify";
        public static final String VOID_NOTIFY="/pgproxy-notification/test/kafkaFlow/kafka-trans-qa6.pg2nonprod.paytm.com:9092/TP_S_1212_EC_EVENTLOG_2007";
        public static final String FUNDBACK_NOTIFY="/pgproxy-notification/oldpg/acquiring/order/fundBackNotify";
        public static final String MERCHANT_NOTIFY="/pgproxy-notification/oldpg/settlement/realtime/merchantNotify";
        public static final String SETTLE_NOTIFY="/pgproxy-notification/test/kafkaFlow/kafka-trans-qa6.pg2nonprod.paytm.com:9092/TOPIC_SETTLEMENT_NOTIFY";
        public static final String CLOSE_NOTIFY="pgproxy-notification/test/kafkaFlow/kafka-trans-qa4.pg2nonprod.paytm.com:9092/TP_S_1212_EC_EVENTLOG_2002";
        public static final String PAYMENT_NOTIFY="/pgproxy-notification/test/paymentNotifyFlow";

    }

    public static class Alipay {

        //Fetch Card From P +
        public static final String USER_QUERYBYFILTER= "/user/assets/queryByFilter";
        public static final String USER_TOKEN_MODIFY = "/user/token/bin/modify";           //Deprecated
        public static final String MERCHANT_QUERYBYFILTER= "/merchant/asset/query/customerAssets";
        public static final String USER_TOKEN_BIN_QUERY="/user/token/bin/query";            //Deprecated

        //Fetch Card Saved on the basis of cacheCardToken
        public static final String QUERY_NONSENSITIVITY_INFO= "/asset/query/nonSensitiveInfo";

        //Save Card At P+
        public static final String USER_BIND_ASSET= "/alipayplus/user/asset/bindAsset.htm";
        public static final String MERCHANT_BIND_ASSET= "/alipayplus/merchant/asset/bindAsset.htm";

        //Get CIN
        public static final String CACHE_CARD = "/alipayplus/user/asset/cacheCard.htm";

        //Delete Card At P+ End
        public static final String DELETE_CUSTOMER_ASSET= "/merchant/asset/delete/customerAsset";
        public static final String DELETE_USER_ASSET= "/user/asset/delete";

        //Bin Query
        public static final String BIN_QUERY = "/user/card/bin/query";

        //Bin Modify
        public static final String BIN_MODIFY = "user/card/bin/modify";

        //Bin Create
        public static final String BIN_CREATE = "user/card/bin/create";

        public static final String MID_MISC_INFO= "/pgmc-adapter/api/v2/get/mid/misc/info/";

    }


    public static class InstaProxyService {
        public static final String WITHDRAW_REQUEST = "/instaproxy/withdraw/request.htm";
        public static final String STATUS_QUERY= "/instaproxy/withdraw/status/query.htm";
        public static final String REQ_VAL="/instaproxy/bankresponse/{bankcode}/UPI/ReqValAdd";
        public static final String REQ_AUTH_CREATE_ORDER ="/instaproxy/bankresponse/{bankcode}/UPI/CreateOrder";
        public static final String JUSTPAY_CALLBACK= "/instaproxy/bankresponse/PTYES/UPI/callback";
        public static final String JUSTPAY_CALLBACK_API=  "/mockbank/PTYES/callback";
        public static final String CREATE_ORDER=  "/instaproxy/bankresponse/PTYES/UPI/CreateOrder";
        public static final String REQ_AUTH_CREATE_ORDER_V2 = "/instaproxy/bankresponse/{bankcode}/UPI/v2/CreateOrder";
        public static final String REQ_AUTH_CREATE_ORDER_V3 = "/instaproxy/bankresponse/{bankcode}/UPI/v3/CreateOrder";
        public static final String INSTAPROXY_UPI_PAYMENT_REQUEST = "/instaproxy/pg2/upi/payment/request.htm";
    }

    public static class OfflineTxn {
        public static final String FETCH_PAYMENT_INSTRUMENT = "/theia/fetchPaymentInstruments";
    }

    public static class AuthAPIresource {
        public static final String OAUTH2 = "/oauth2";
        public static final String AUTHORIZE = "/oauth2/authorize";
        public static final String TOKEN = "/oauth2/token";
        public static final String USERTOKENS = "/oauth2/usertokens";
        public static final String RESOURCE_USER = "resource/user";
        public static final String REGISTERUSER = "/v3/api/register";
        public static final String VALIDATEREGISTERUSER = "/v3/api/register/validate";
    }

    public static class GetMrchntDetailResourcepath {
        public static final String FETCH_MERCHANT = MappingService.MERCHANT_MIGRATION_DETAILS;
    }

    public static class PgPlusBo {
        public static final String SEARCH_TRANSACTION = "/pg-plus-bo/search/transaction";
        public static final String CRERATE_DISPUTE = "/pg-plus-bo/dispute/createV1";
        public static final String UPLOAD_PROOF = "pg-plus-bo/api/v1/dispute/action";
    }


    public static class DBConnectionURL {
        public static final String PGP_DB_CONNECTION_URL = LocalConfig.PGP_DB_CONNECTION_URL;
        public static final String AUTH_DB_CONNECTION_URL = LocalConfig.AUTH_DB_CONNECTION_URL;
        public static final String WALLET_DB_CONNECTION_URL = LocalConfig.WALLET_DB_CONNECTION_URL;
        public static final String PANEL_DB_CONNECTION_URL = LocalConfig.PANEL_DB_CONNECTION_URL;
        public static final String PG_DB_CONNECTION_URL = LocalConfig.PG_DB_CONNECTION_URL;
        public static final String MOCK_KAFKA_DB = LocalConfig.MOCK_KAFKA_DB;

    }
    public static class MerchantMigration {
        public static final String CREATE_MERCHANT = "/admin/app/api/v3/submitCreateMerchantAPIRequest";
          public static final String EDIT_MERCHANT = "/admin/app/api/v3/submitEditMerchantAPIRequest";
    }

    public static class Theme {
        public static final String MERCHANT = "merchant";
        public static final String MERCHANT3 = "merchant3";
        public static final String MERCHANT4 = "merchant4";
        public static final String MERCHANT5 = "merchant5";
        public static final String MERCHANTLOW = "merchantlow";
        public static final String MERCHANTLOW5 = "merchantlow5";
        public static final String ENHANCEDWAP = "enhancedwap";
        public static final String ENHANCED_WEB = "enhancedweb";
        public static final String MERCHANTLOWCCDD = "merchantlow|ccdc";
        public static final String CHECKOUTJS_WEB = "checkoutjs_web";
        public static final String CHECKOUTJS_WAP = "checkoutjs_wap";
        public static final String CHECKOUTJSWEB_REVAMP = "checkoutjsweb_revamp";//Revamp phase 1
        public static final String CHECKOUTJSWAP_REVAMP = "checkoutjswap_revamp";//Revamp phase 1
        public static final String CHECKOUTJS_WEB_REVAMP = "checkoutjs_web_revamp";//Revamp phase 2
        public static final String CHECKOUTJS_WAP_REVAMP = "checkoutjs_wap_revamp";//Revamp phase 2
        public static final String CHECKOUTJS_WEB_REVAMP_2 = "checkoutjs_web_revamp_2";
        public static final String CHECKOUTJSE_WEB = "checkoutjse_web";
        public static final String CHECKOUTJSE_WEB_REVAMP = "checkoutjse_web_revamp";
        public static final String ENHANCEDWEB_REVAMP = "enhanced_web_revamp";  //Revamp phase 1
        public static final String ENHANCEDWAP_REVAMP = "enhanced_wap_revamp"; //Revamp phase 1
        public static final String ENHANCED_WEB_REVAMP = "enhancedweb_revamp";  //Revamp phase 2
        public static final String ENHANCED_WAP_REVAMP = "enhancedwap_revamp";  //Revamp phase 2
        public static final String LIGHTENING_WEB_REVAMP = "lightening_web_revamp";  //Lightening Checkout
    }

    public enum UITheme {
        DARK_THEME("{\"REDIRECTION\":{\"mobile\":{\"HEADERPANEL\":\"#5BF738\",\"HEADERTEXT\":\"#241414\",\"PAYBUTTONBG\":\"#EFEF35\",\"PAYBUTTONTEXT\":\"#57574E\",\"BODYBG\":\"#DDBCE9\",\"BODYTEXT\":\"#EF0909\",\"BOTTOMBG\":\"#3871F7\",\"LABEL\":\"Dark theme\",\"NAME\":\"DARK\",\"IMAGE\":\"dark\",\"isSelected\":true},\"web\":{\"HEADERPANEL\":\"#5BF738\",\"HEADERTEXT\":\"#241414\",\"PAYBUTTONBG\":\"#EFEF35\",\"PAYBUTTONTEXT\":\"#57574E\",\"BODYBG\":\"#DDBCE9\",\"BODYTEXT\":\"#EF0909\",\"BOTTOMBG\":\"#3871F7\",\"LABEL\":\"Dark theme\",\"NAME\":\"DARK\",\"IMAGE\":\"dark\",\"isSelected\":true}},\"CHECKOUT\":{\"root\":\"\",\"style\":{\"bodyBackgroundColor\":\"#000a1e\",\"bodyColor\":\"#FFFFFF\",\"themeBackgroundColor\":\"#00b9f5\",\"themeColor\":\"#FFFFFF\",\"headerBackgroundColor\":\"#3871F7\",\"headerColor\":\"#FFFFFF\",\"errorColor\":\"\",\"successColor\":\"\"},\"jsFile\":\"\",\"data\":{\"orderId\":\"\",\"token\":\"\",\"tokenType\":\"SSO\",\"mid\":\"\",\"userDetail\":{\"mobileNumber\":121212,\"name\":\"\"}},\"merchant\":{\"name\":\"\",\"logo\":\"\",\"redirect\":true,\"callbackUrl\":\"\",\"hidePaytmBranding\":false},\"mapClientMessage\":{},\"labels\":{},\"payMode\":{\"labels\":{},\"filter\":{\"include\":[],\"exclude\":[]},\"order\":[]},\"handler\":{}}}"),
        LIGHT_THEME(""),
        CUSTOM_THEME("{\"REDIRECTION\":{\"mobile\":{\"HEADERPANEL\":\"#8b572a\",\"HEADERTEXT\":\"#7ed321\",\"PAYBUTTONBG\":\"#bd10e0\",\"PAYBUTTONTEXT\":\"#f8e71c\",\"BODYBG\":\"#FFFFFF\",\"BODYTEXT\":\"#4a90e2\",\"BOTTOMBG\":\"#8b572a\",\"LABEL\":\"Custom theme\",\"NAME\":\"CUSTOM\",\"IMAGE\":\"custom\",\"isSelected\":true},\"web\":{\"HEADERPANEL\":\"#8b572a\",\"HEADERTEXT\":\"#7ed321\",\"PAYBUTTONBG\":\"#bd10e0\",\"PAYBUTTONTEXT\":\"#f8e71c\",\"BODYBG\":\"#FFFFFF\",\"BODYTEXT\":\"#4a90e2\",\"BOTTOMBG\":\"#8b572a\",\"LABEL\":\"Custom theme\",\"NAME\":\"CUSTOM\",\"IMAGE\":\"custom\",\"isSelected\":true}},\"CHECKOUT\":{\"root\":\"\",\"style\":{\"bodyBackgroundColor\":\"#FFFFFF\",\"bodyColor\":\"#4a90e2\",\"themeBackgroundColor\":\"#bd10e0\",\"themeColor\":\"#f8e71c\",\"headerBackgroundColor\":\"#8b572a\",\"headerColor\":\"#7ed321\",\"errorColor\":\"\",\"successColor\":\"\"},\"jsFile\":\"\",\"data\":{\"orderId\":\"\",\"token\":\"\",\"tokenType\":\"SSO\",\"mid\":\"\",\"userDetail\":{\"mobileNumber\":121212,\"name\":\"\"}},\"merchant\":{\"name\":\"\",\"logo\":\"\",\"redirect\":true,\"callbackUrl\":\"\",\"hidePaytmBranding\":false},\"mapClientMessage\":{},\"labels\":{},\"payMode\":{\"labels\":{},\"filter\":{\"include\":[],\"exclude\":[]},\"order\":[]},\"handler\":{}}}");

        private String theme;

        UITheme(String theme) {
            this.theme = theme;
        }

        public String get() {
            return this.theme;
        }
    }

    public static class Sprint {
        public static final String SPRINT_THEMATIC = "Sprint-Thematic";
        public static final String SPRINT27_1 = "Sprint-27.1";
        public static final String SPRINT28_2 = "Sprint-28.2";
        public static final String SPRINT29_2 = "Sprint-29.2";
        public static final String SPRINT30_1 = "Sprint-30.1";
        public static final String SPRINT30_2 = "Sprint-30.2";
        public static final String SPRINT31_1 = "Sprint-31.1";
        public static final String SPRINT31_2 = "Sprint-31.2";
        public static final String SPRINT32_1 = "Sprint-32.1";
        public static final String SPRINT32_2 = "Sprint-32.2";
        public static final String SPRINT32_3 = "Sprint-32.3";
        public static final String SPRINT33_2 = "Sprint-33.2";
        public static final String SPRINT34_1 = "Sprint-34.1";
        public static final String SPRINT34_2 = "Sprint-34.2";
        public static final String SPRINT36_3 = "Sprint-36.3";
        public static final String SPRINT37_0 = "Sprint-37.0";
        public static final String OTP_INJECTION="Sprint-OTP_Injection";
    }

    public static class logoUrl{
        public static final String uploadLogo = "/mapping-service/logo/update/";
        public static final String checkLogo = "/mapping-service/logo/checkLogo/";
        public static final String fetchLogo = "/mapping-service/logo/fetch/";
        public static final String deleteLogo = "/mapping-service/logo/delete/";
    }

    public static class merchantpgpui{
        public static final String GET_MERCHANTPGPUI_THEME = "/merchantpgpui/theme/{mid}";
        public static final String GET_MERCHANTPGPUI_THEME_V2 = "/merchantpgpui/theme/v2/{mid}";
        public static final String CHANGE_VERSION ="merchantpgpui/admin/checkoutjs/version/{num}" ;
        public static final String GET_VERSION = "/merchantpgpui/admin/checkoutjs/mid/version/{num}";
        public static final String LOCALE_UPDATE = "/merchantpgpui/admin/locale/add/en-IN";

    }

    public enum FormType {
        SKIPLOGIN("SKIPLOGIN"),
        PAYMENTFORM("PAYMENTFORM");
        private final String formType;
        FormType(String formType) {
            this.formType = formType;
        }
        @Override
        public String toString() {
            return formType;
        }
    }

    public static class BossAPI{
        public static final String MERCHANT_DETAILS = "/api/v1/merchant/details?searchBy=phoneNo&searchValue={phoneNumber}";
        public static final String BLOCK_UNBLOCK_STATUS = "/api/v1/blockUnblock/status";
        public static final String PRE_AUTH = "/api/v1/merchant/preAuthEdc/{mid}/addPreference?isStandardPreAuthEdcEnabled={value}";


    }

    public static class coftCenter {
        public static final String TOKENIZE_CARD = "/coft-center/coft/merchant/{mid}/tokenize";
        public static final String TOKENIZE_CARD_V2 = "/coft-center/coft/merchant/{mid}/v2/tokenize";
        public static final String FETCH_TOKEN_DETAILS = "/coft-center/coft/merchant/{mid}/getTokenDetail";
        public static final String GENERATE_TOKEN_DATA = "/coft-center/coft/merchant/{mid}/token/generateTokenData";
        public static final String MODIFY_TOKEN_STATUS = "/coft-center/coft/merchant/{mid}/token/modifyStatus";
        public static final String FETCH_PENDING_TOKENS = "/coft-center/coft/internal/fetchPendingTokens";
        public static final String MODIFY_TOKENS = "/coft-center/coft/merchant/{mid}/tokens/modifyStatus";
        public static final String TOKENIZE_DIRECT_CARD = "/coft-center/coft/merchant/{mid}/tokenize/direct";
        public static final String EXTEND_GENERATE_TOKEN_DATA = "/coft-center/coft/merchant/{mid}/token/extend/generateTokenData";
        public static final String CARD_TO_PAR ="/coft-center/get/panUniqueReference";
        public static final String FETCH_ALL ="/user/cards/fetch-all";
        public static final String FETCH_ALL_V2 ="/user/cards/v2/fetch-all";
    }

    public enum TokenizationConsent {
        YES("1"),
        NO("0");
        private String userConsent;

        TokenizationConsent(String userConsent) {
            this.userConsent = userConsent;
        }

        public String get() {
            return this.userConsent;
        }
    }

    public enum CardScheme {
        MASTERCARD("MASTER"),
        VISA("VISA");
        private String cardScheme;

        CardScheme(String cardScheme) {
            this.cardScheme = cardScheme;
        }

        public String get() {
            return this.cardScheme;
        }
    }

    public enum CardSource {
        CARD_ON_FILE("CARD_ON_FILE"),
        MANUAL_ENTERED("MANUAL_ENTERED"),
        CARD_SCAN("CARD_SCAN");
        private String cardSource;

        CardSource(String cardSource) {
            this.cardSource = cardSource;
        }

        public String get() {
            return this.cardSource;
        }
    }

    public enum RequestedBy {
        CARDHOLDER("CARDHOLDER"),
        MERCHANT("MERCHANT"),
        TOKEN_REQUESTOR("TOKEN_REQUESTOR");
        private String requestedBy;

        RequestedBy(String requestedBy) {
            this.requestedBy = requestedBy;
        }

        public String get() {
            return this.requestedBy;
        }
    }

    public enum ReasonCode {
        SUSPECTED_FRAUD("SUSPECTED_FRAUD"),
        OTHER("OTHER"),
        CUSTOMER_CONFIRMED("CUSTOMER_CONFIRMED");
        private String reasonCode;

        ReasonCode(String reasonCode) {
            this.reasonCode = reasonCode;
        }

        public String get() {
            return this.reasonCode;
        }
    }

    public enum TokenStatus {
        SUSPENDED("SUSPENDED"),
        DEAD("DEAD"),
        ACTIVE("ACTIVE"),
        INIT("INIT"),
        FAILED("FAILED");
        private String tokenStatus;

        TokenStatus(String tokenStatus) {
            this.tokenStatus = tokenStatus;
        }

        public String get() {
            return this.tokenStatus;
        }
    }

    public enum TokenFields {
        ID("_id"),
        TOKEN_BIN("tokenBin"),
        TOKEN("token"),
        TOKEN_REFERENCE_NUMBER("tokenReferenceNumber"),
        PAN("primaryAccountRefNumber"),
        TOKEN_STATUS("tokenState"),
        CUST_ID("custId"),
        USER_ID("userId");

        private String tokenFields;

        TokenFields(String tokenFields) {
            this.tokenFields = tokenFields;
        }

        public String get() {
            return this.tokenFields;
        }
    }

    public enum subscriptionRevamp {
        TO_BE_PAID_NOW("To be paid now"),
        RECURRING_BILL_FREQUENCY("Recurring Bill Frequency"),
        RECURRING_BILL_AMOUNT("Recurring Bill Amount");
        private final String SubsRevamp;
        subscriptionRevamp(String SubsRevamp) {
            this.SubsRevamp = SubsRevamp;
        }
        @Override
        public String toString() {
            return SubsRevamp;
        }
    }

    public enum subscriptionDetailsUI{
        AMOUNT_TO_BE_PAID_NOW("Amount to be Paid Now"),
        RECURRING_BILL_FREQUENCY("Recurring Bill Frequency"),
        RECURRING_BILL_AMOUNT("Recurring Bill Amount*"),
        FREQUENCY("Frequency"),
        VALIDITY("Validity"),
        NEXT_PAYMENT("Next Payment");
        private final String SubsDetailsUI;
        subscriptionDetailsUI(String SubsDetailsUI) {
            this.SubsDetailsUI = SubsDetailsUI;
        }

        public String toString() {
            return SubsDetailsUI;
        }
    }

    public enum CardToParRequestType {
        TIN("TIN"),
        CIN("CIN"),
        GCIN("GCIN"),
        PAR("PAR"),
        PAN("PAN");
        private String requestType;

        CardToParRequestType(String requestType) {
            this.requestType = requestType;
        }

        public String get() {
            return this.requestType;
        }
    }

    public enum VAULTIDENTIFIER{
        PPBL("PPBL"),
        OCL("OCL"),
        PMALL("PMALL");

        private String vaultType;

        VAULTIDENTIFIER(String vaultType) {
            this.vaultType = vaultType;
        }

        public String get() {
            return this.vaultType;
        }

    }

    public static class MappingServicePG2APIS {
        public static final String QUERY_MERCH_PREF_INFO = "/mapping-service/query/merchant/preference/info/{mid}";
        public static final String MERCHANT_GET_PREFERENCEINFOTEXT = "/mapping-service/merchant/get/preferenceinfosext/{mid}";
        public static final String MERCHANT_GET_PREFERENCE_INFO = "/mapping-service/merchant/get/preference/info/{mid}";
        public static final String MERCHANT_ATTRIBUTE_ADDITIONAL = "/mapping-service/merchant/attribute/additional/{mid}/{idType}";
        public static final String MERCHANT_ATTRIBUTE_GET_SUBSCRIPTION_DETAIL = "/mapping-service/merchant/attribute/get/subscription/detail/{mid}/{idType}";
        public static final String QUERY_MERCHANT_ACQUIRING_MID = "/mapping-service/query/merchant/acquiring/{mid}";
        public static final String MERCHANT_QUERY_ACQUIRING = "/mapping-service/merchant/query/acquiring/{mid}/{paymethod}";
        public static final String QUERY_MERCHANT_ACQUIRING = "/mapping-service/query/merchant/acquiring/{mid}/{paymethod}";
        public static final String MERCHANT_QUERY_CONTRACT_ITEM = "/mapping-service/merchant/query/contract/item/{merchantId}/{contractStatus}/{pageNum}/{pageSize}";
        public static final String QUERY_MERCHANT_CONTRACT_ITEM = "/mapping-service/query/merchant/contract/item/{merchantId}/{contractStatus}/{pageNum}/{pageSize}";
        public static final String MERCHANT_QUERY_CONTRACT_DETAIL = "/mapping-service/merchant/query/contract/detail/{merchantId}/{contractStatus}/{productCode}";
        public static final String MERCHANT_QUERY_CONTRACT_DETAIL_PG = "/pgmc-adapter/api/v1/details/merchant/query/contract/ByMid/{merchantId}";
        public static final String QUERY_MERCHANT_CONTRACT_DETAIL = "/mapping-service/query/merchant/contract/detail/{merchantId}/{contractStatus}/{productCode}";
        public static final String QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL = "/mapping-service/query/merchant/migration/contract/details/{merchantId}";
        public static final String QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL_PG = "/pgmc-adapter/api/v1/details/merchant/query/contract/ByMid/{merchantId}";
        public static final String COMMON_V1_GET_CONTRACT_PAYMENTINFO = "/mapping-service/common/v1/get/contract/paymentInfo/{merchantId}/{paymentMethod}/{productCode}";
        public static final String MERCHANT_PROFILE = "/mapping-service/merchant/profile/{merchantId}/{idType}";
        public static final String MERCHANT_GET_EXTENDED_INFO = "/mapping-service/merchant/get/extended/info/v3/{mid}/true";
        public static final String USER_GETMERCHANTEXTENDEDINFO = "/mapping-service/user/getMerchantExtendedInfo/v3/{userid}";
        public static final String MERCHANT_V1 = "/mapping-service/merchant/v3/{type}/{merchantId}";
        public static final String MERCHANT_IDMAP = "/mapping-service/merchant/idmap/v3/{merchantId}/{idType}";
        public static final String GET_ALIPAYID = "/mapping-service/get/oldpgId/{merchantId}";
        public static final String GET_PAYTMID = "/mapping-service/get/paytmid/v1/{oldpgId}";
        public static final String LOGO_COBRANDING_MID = "/mapping-service/logo/cobranding/mid/{mid}/channel/{channel}";
        public static final String GET_MERCHANTLOGOINFO_V2 = "/mapping-service/get/merchantlogoinfo/v2/{mid}";
        public static final String LOGO_COBRANDING_DETAILS = "/mapping-service/logo/cobranding/details/{mid}";
        public static final String EOS_MERCHANT_DEVICE_DETAILS_BANKSLIST_TID = "/mapping-service/eos/merchant/device/details/bankslist/tid/{tid}";
        public static final String EOS_MERCHANT_DEVICE_DETAILS_TID = "/mapping-service/eos/merchant/device/details/tid/{tid}";
        public static final String EOS_MERCHANT_DEVICE_DETAILS_TID_BANKNAME = "/mapping-service/eos/merchant/device/details/tid/bankName/{tid}/{bankName}";
        public static final String EOS_MERCHANT_DEVICE_DETAILS_V2_TID = "/mapping-service/eos/merchant/device/details/v2/tid/{tid}";
        public static final String MERCHANT_GET_THEMATIC_DETAILS = "/mapping-service/merchant/get/thematic/details/{mid}";
        public static final String GET_ENTITYURLINFOMID = "/mapping-service/get/entityurlinformid/{mid}/{urlType}/{websiteName}";
        public static final String GET_ENTITYURLINFOMID_V2 = "/mapping-service/get/entityurlinformid/v2/{mid}/{urlType}/{websiteName}";
        public static final String GET_MBID = "/mapping-service/get/mbid/{mid}/{bankId}/{payMethodId}/{authModeId}";
        public static final String GET_MERCHANTAPIURLINFO = "/mapping-service/get/merchantapiurlinfo/{midType}/{mid}";
        public static final String GET_MERCHANTDATA_PAYTMID = "/mapping-service/get/merchantdata/paytmid/{mid}";
        public static final String MERCHANT_ATTRIBUTE_KEY = "/mapping-service/merchant/attribute/key/{mid}/{idType}";
        public static final String MERCHANT_ATTRIBUTE_KEY_PAYMODE = "/mapping-service/merchant/attribute/key/{mid}/{idType}/paymode/{payMode}";
        public static final String MERCHANT_ATTRIBUTE_KEY_CLIENTID= "/mapping-service/merchant/attribute/key/{mid}/{idType}/{clientId}";
        public static final String MERCHANT_V3_MERCHANTIDLIST= "/mapping-service/merchant/v3/{type}/merchantIdList";
        public static final String MERCHANT_V3_MERCHANTLIST= "/mapping-service/merchant/v3/{type}/merchantIdList";
        public static final String MERCHANT_V2_QUERY_CONTRACT_ITEM= "/mapping-service/merchant/v2/query/contract/item";
        public static final String PAYMENT_INFO_FEE= "/mapping-service/common/v1/list/paymentInfoFee/{mid}/{productCode}";
        public static final String QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL_V2 = "/mapping-service/query/merchant/migration/contract/details/v2/{merchantId}";
        public static final String ENTITY_EDC_CHANNEL_INFO = "/mapping-service/entityEdcChannelInfo/mid/{merchantId}";


    }

    public static class MappingServiceDrop1APIS {
        public static final String BANKMASTERDETAILS_PAYMODE = "/mapping-service/get/bankmasterdetails/v1/paymode/{paymode}";
        public static final String BANKDETAILS_ALIPAYCODE = "/mapping-service/get/bankdetails/oldpgcode/{oldpgBankCode}";
        public static final String GETBANKDETAILS_LIST_CODES = "/mapping-service/get/banksdetailslistfromCodes/v1/{bankCodes}";
        public static final String GETBANKDETAILS_BANKCODE = "/mapping-service/get/bankdetails/v1/{bankCode}";
        public static final String GET_RESPONSECODEDETAILS = "/mapping-service/get/responsecodedetails/{paytmResponseCode}";
        public static final String GET_RESPONSECODEDETAILS_RESULTCODE = "/mapping-service/get/responsecodedetails/resultcode/{resultCode}";
        public static final String NOTIFICATION_GET_NOTIFICATION_TEMPLATE = "/mapping-service/notification/get/notification/template";
        public static final String USER_V1_TYPE_PAYTMID = "/mapping-service/user/v1/{type}/{paytmId}";
        public static final String USER_V1_TYPE_PAYTMID_WILLCREATE = "/mapping-service/user/v3/{type}/{paytmId}/{willCreate}";
        public static final String GET_MERCHANTLOGOINFO_V2_FETCHLOGOFROMBOSSPANEL = "/mapping-service/get/merchantlogoinfo/v2/{mid}";
        public static final String GET_MERCHANTLOGOINFO_V2 = "/mapping-service/get/merchantlogoinfo/v2/{mid}";
        public static final String COMMON_V1_GET = "/mapping-service/common/v1/get/merchant";
        public static final String MERCHANTAGENT_GET_AGENTINFO_ID_TYPE = "/mapping-service/merchantAgent/get/agentInfo/{id}/{type}";
        public static final String GET_GLOBAL_CONFIG_ACQUIRER_CURRENCY_IICPC1IN = "/mapping-service/get/global/config/ACQUIRER_CURRENCY_IICPC1IN";
        public static final String NOTIFICATION_FETCH_TEMPLATE_CONFIGURATION = "/mapping-service/notification/fetch/template/configuration";
        public static final String GET_LIMIT_MERCHANTTYPE_PPI_LIMIT_1 = "/mapping-service/get/limit/merchantType/PPI_LIMIT_1";
        public static final String GET_PSP_SCHEMA = "/mapping-service/get/pspSchema";
        public static final String DCC_SUPPORTED_CURRENCYLIST_ACQUIRER_IICTC1IN = "/mapping-service/dcc/supportedCurrencyList/acquirer/IICTC1IN";

    }

    public static class MappingServiceDrop2APIS {
        public static final String GET_BANKRESPONSECODES_BANKCODES_PAYMODES_SERVICES = "/mapping-service/get/bankresponsecodes/{bankCode}/{payMode}/{service}";
        public static final String GET_MERCHANT_STATIC_CONFIG_MID = "/mapping-service/get/merchant/static/config/{mid}";
        public static final String GET_EMI_ON_DC_ELIGIBILITY_BY = "/mapping-service/get/emiOnDcEligibilityBy";
        public static final String DEVICE_DETAILS_TID = "/mapping-service/eos/merchant/device/details/tid/{tid}";
        public static final String DEVICE_DETAILS_BANKLIST_TID = "/mapping-service/eos/merchant/device/details/bankslist/tid/{tid}";
        public static final String DEVICE_DETAILS_V2_TID = "/mapping-service/eos/merchant/device/details/v2/tid/{tid}";
        public static final String DEVICE_DETAILS_TID_BANKNAME= "/mapping-service/eos/merchant/device/details/tid/bankName/{tid}/{bankName}";

    }


    public static class MappingServiceDrop3APIS {

        public static final String GET_FORMATTER_DETAILS = "/mapping-service/get/formatter/{Bankcode}/{paymethod}";

        public static final String USER_GET_USERMID = "/mapping-service/user/getUserMid/{userid}";
        public static final String ENTITYURL_INFO_FOR_MID = "/mapping-service/get/entityurlinformid/v2/{mid}/{urlType}/{websiteName}";
        public static final String GET_CARD_NETWORK_DETAILS = "/mapping-service/get/cardNetworkDetails/cardNetwork";

        public static final String GET_PAY_METHOD_DETAILS = "/mapping-service/get/paymethodDetails/payMethod";

        public static final String GET_FETCH_LOGO = "/mapping-service/logo/fetch/{logotype}/{identifier}";

        public static final String GET_MERCHANT_IDMAP = "/mapping-service/merchant/idmap/v3/{mid}/{idtype}";

        public static final String FETCH_ENTITY_IGNORE_PARAMS = "/mapping-service/get/fetchEntityIgnoreParams/{entityid}";

        public static final String GET_MERCHANTRESPONSECODEDETAILS = "/mapping-service/merchantResponseCode/mid/resultCode/{mid}/{Resultcode}";

        public static final String MERCHANT_ATTRIBUTE_KEY_WITH_MID_AND_IDTYPE= "/mapping-service/merchant/attribute/key/{mid}/{idType}";

        public static final String MERCHANT_ATTRIBUTE_KEY_WITH_MID_IDTYPE_AND_PAYMODE= "/mapping-service/merchant/attribute/key/{mid}/{idType}/paymode/{paymode}";

    }

    public class BinCenter{
        public static final String CARD_BIN = "/bin-center/v1/card/bin/{bin}/query?ignoreCache=true";
        public static final String MODIFY_BIN = "bin-center/v1/update/bin/sync";
        //public static final String MODIFY_BIN = "bin-center/v1/update/bin/sync?updatedBy={{$randomFirstName}}&fileName={{$randomFileName}}";
    }

    public static class MappingServiceAuditL2APIS {
        public static final String MERCHANT_V3 = "/mapping-service/merchant/v3/{type}/{merchantId}";
        public static final String MERCHANT_GET_EXTENDED_INFO_V3 = "/mapping-service/merchant/get/extended/info/v3/{mid}";
        public static final String GET_PAYTMID_V1_OLDPGID = "/mapping-service/get/paytmid/v1/{oldpgId}";
        public static final String GET_OLDPGID = "/mapping-service/get/oldpgId/{merchantId}";
        public static final String MERCHANTLOGOINFO_V1_OLDPGID = "/mapping-service/get/merchantLogoInfo/v1/{oldpgMid}";
        public static final String MERCHANT_GET_EXTENDED_INFO_V3_MID_CLIENTID = "/mapping-service/merchant/get/extended/info/v3/{mid}/{clientId}";
    }
    public static class SubscriptionService{
        public static final String SUBSCRIPTION_CHECK_STATUS = "/subscription/subscription/checkStatus";
        public static final String SUBSCRIPTION_CANCEL = "/subscription/subscription/cancel";
        public static final String SUBSCRIPTION_STATUS_MODIFY = "/subscription/subscription/status/modify";
        public static final String SUBSCRIPTION_PRENOTIFY_STATUS = "/subscription/preNotify/status";
        public static final String HDFC_INTENT_PAY = "/mockbank/generate/hdfc/intent/pay";
    }

    public static class MappingServiceAuditChangesNew {
        public static final String GETBANKDETAILSV1_BANKCODE = "/mapping-service/get/bankdetails/v1/{bankCode}";

        public static final String GET_MERCHANT_IDMAP_V3 = "/mapping-service/merchant/idmap/v3/{mid}/{idtype}";

        //public static final String GETBANKDETAILS_USERID_OLD = "/mapping-service/get/bankdetailsfromid/{userid}";

        public static final String GETBANKDETAILS_USERID_NEW_V1 = "/mapping-service/get/bankdetailsfromid/v1/{userid}";
    }
    public static class MappingServiceAudit3APIS {
        public static final String GET_VENDOR_SPLITDETAILS_V3 ="/mapping-service/get/vendor/splitDetails/v3/{mid}";
        public static final String QUERY_MERCHANT_EXTENDEDINFO_V3 ="/mapping-service/query/merchant/extended/info/v3/{mid}";
        public static final String USER_GET_MERCHANT_EXTENDEDINFO_V3 ="/mapping-service/user/getMerchantExtendedInfo/v3/{userid}";
        public static final String MERCHANT_V3_MERCHANTLIST= "/mapping-service/merchant/v3/{type}/merchantIdList";
        public static final String MERCHANT_GET_EXTENDEDINFO_V4 ="/mapping-service/merchant/get/extended/info/v4/{mid}";
        public static final String USER_V3 ="/mapping-service/user/v3/{type}/{user}";
        public static final String MAPPING_V3 ="/mapping-service/query/merchant/mapping/v3/{type}/{mid}";
        public static final String USER_V3_WILLCREATE ="/mapping-service/user/v3/{type}/{user}/{willCreate}";
        public static final String GET_BANKDETAILSFROMMIDS_V1 ="/mapping-service/get/banksdetailslistfromids/v1/{bankIds}";
        public static final String GET_V1_BANKMASTERDEATAILS ="/mapping-service/get/v1/bankmasterdetails";
        public static final String GET_BANKMASTERDEATAILS_V1_PAYMODE ="/mapping-service/get/bankmasterdetails/v1/paymode/{paymode}";
        public static final String GET_BANKDEATAILSFROMCODES_V1 ="/mapping-service/get/banksdetailslistfromCodes/v1/{bankcodes}";
        public static final String GET_BANKDEATAILSOLDPGCODES_V1 ="/mapping-service/get/bankdetails/oldpgcode/{bankcodes}";
        public static final String COMMON_V1_GET_MERCHANT ="/mapping-service/common/v1/get/merchant";
        public static final String GET_MERCHANTDATA_PAYTMID_V1 ="/mapping-service/get/merchantdata/paytmid/v1/{mid}";
        public static final String GET_MERCHANTDATA_NAME_V1 ="/mapping-service/get/merchantdata/name/v1/{name}";
        public static final String GET_LOOKUPFROMID_V1 ="/mapping-service/get/lookupfromid/v1/{Id}";
        public static final String GET_LOOKUPPREFERENCESV1 ="/mapping-service/get/lookup/v1/{category}/{channelName}";
        public static final String QUERY_MERCHANT_MIGRATIONDETAIL_V1 ="/mapping-service/query/merchant/migration/details/v1/{mid}/true";

    }

    public static class KibanaIP {
        public static final String KIBANA_ORIGIN = "http://10.170.7.59";
        public static final String KIBANA_REFERER = "http://10.170.7.59/app/discover";
        public static final String KIBANA_VERSION = "7.17.7";
    }

    public static class CheckoutJsReferer {
        public static final String CHECKOUTJS_REFERER = LocalConfig.PGP_HOST + "/checkoutjs/1459/assets/iframes/dummy-frame.html";
    }
        public static class AxisVPAs
        {
            public static final String validVpa = "paytm.uat@axis";
            public static final String invalidVpa = "invalid.uat@axis";
            public static final String genericErrorVpa = "generic.uat@axis";
            public static final String validMobile= "9999999999";
            public static final String invalidMobile= "0000000000";
            public static final String genericErrorMobile= "1111111111";

    }
    public enum MessageAssert {
        CONVENIENCE_CHARGES("It includes Convenience charges"),
        CONVENIENCE_FEE("Convenience feewill be charged"),
        COOKIE_NAME("pg_login"),
        EMPTY_CREDIT_CARD_NUMBER("ENTER CREDIT CARD NUMBER"),
        EMPTY_CVV_NUMBER("CVV/SECURITY CODE"),
        EMPTY_EXPIRY_DATE("EXPIRY DATE"),
        INSUFFICIENT_BALANCE_PAYMENT("You do not have enough balance for this payment"),
        INSUFFICIENT_BALANCE("Insufficient Wallet Balance"),
        INVALID_CARD_NUMBER("Please enter a valid card number"),
        INVALID_CVV("CVV is Invalid"),
        INVALID_EXPIRY("Invalid Expiry Date"),
        INVALID_LOGIN_CREDENTIALS("Please enter valid Username and Password"),
        INVALID_PAYMENT_DETAILS("Entered payment details are not applicable for this subscription type"),
        INVALID_VPA("Invalid VPA, Try Again"),
        INVALID_UPI("Invalid UPI ID."),
        PAYTM_BALANCE("Paytm Balance"),
        POSTPAID_EMPTY_PASSCODE("To complete the payment enter your Paytm Passcode"),
        POSTPAID_INCORRECT_PASSCODE("Invalid credentials"),
        PROMO_DISCOUNT("discount"),
        SOMETHING_WENT_WRONG("Something went wrong, please try using another payment mode"),
        UPI_NUMBER_ERROR("UPI Number does not exist"),
        VALID_OTP("Please Enter Valid Otp"),
        WRONG_OTP("Transaction declined due to Wrong OTP entered. Please try again or ask customer to contact their bank"),
        ZERO_EMI("Zero/Low Cost EMI Available"),
        PAY_PostPaid("Pay with Paytm Postpaid");

        private final String messageAssert;

        MessageAssert(String messageAssert) {
            this.messageAssert = messageAssert;
        }
        @Override
        public String toString() {
            return messageAssert;
        }
    }
    public enum convenienceFeeElements{
        bankAccountTitle("UPI on accounts:"),
        bankAccountSubTitle("(Current/ savings account)"),
        bankAccountFee("bankAccountFee"),
        subTypeCreditCardTitle("Credit Card on UPI:"),
        creditCardFee("creditCardFee"),
        subTypePPIWalletTitle("Prepaid Instruments on UPI:"),
        ppiWalletFee("ppiWalletFee"),
        subTypeCreditLineTitle("Credit Line on UPI:"),
        creditLineFee("creditLineFee"),
        convFeeHeading("Convenience Fees"),
        convFeeText("Convenience fees are fees applied by PG to end customers as per payment instrument to facilitate payment services to end users efficiently"),
        platformFeeHeading("Platform Fee"),
        platformFeeText("Platform fees refer to charges levied by PG on end customers. These fees are instrument agnostic and cover the infrastructure costs of maintaining Platform"),
        platformFeeAmount("Platform fee amount"),
        convFeeHeading2( "Fees will be applied"),
        convFeeHeading3( "Fees might be applied");
        private final String convenienceFeeElements;
        convenienceFeeElements(String convenienceFeeElements) { this.convenienceFeeElements= convenienceFeeElements;}
        @Override
        public String toString() {
            return convenienceFeeElements;
        }
    }
    public enum Source {
        PG_LINK("PG_LINK");

        private final String value;

        Source(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
        public static final String PAYTM_LOVES_EMI_THEIA_CLIENT_ID = "paytm-emi-app";
    public enum ResultCode
    {
        SUCCESS("001","S","success","SUCCESS",""),
        SUCCESS_SUBS("001","S","Success","0000",""),
        FAIL("009","F","payment Failure","FAIL",""),
        RISK_REJECT("009", "F", "risk reject", "FAIL","00000011"),
        QR_SUCCESS("QR_0001", "SUCCESS", "SUCCESS", "SUCCESS",""),
        SYSTEM_ERROR("010", "F", "payment fail due to some technical error", "SYSTEM_ERROR","");

        private String resultCodeId;
        private String resultStatus;
        private String resultMsg;
        private String code;
        private String subResultCodeId;

        /**
         * @param resultCodeId
         * @param resultStatus
         * @param resultMsg
         * @param code
         * @param subResultCodeId
         */
        private ResultCode(String resultCodeId, String resultStatus, String resultMsg, String code,String subResultCodeId) {
            this.resultCodeId = resultCodeId;
            this.resultStatus = resultStatus;
            this.resultMsg = resultMsg;
            this.code = code;
            this.subResultCodeId=subResultCodeId;
        }

        /**
         * @return the resultCodeId
         */
        public String getResultCodeId() {
            return resultCodeId;
        }

        /**
         * @return the resultStatus
         */
        public String getResultStatus() {
            return resultStatus;
        }

        /**
         * @return the resultMsg
         */
        public String getResultMsg() {
            return resultMsg;
        }

        /**
         * @return the resultCode
         */
        public String getCode() {
            return code;
        }

        /**
         * @return the subResultCodeId
         */
        public String getSubResultCodeId() {
            return subResultCodeId;
        }
    }

    public enum assetType {
        ALT_TOKEN("ALT_TOKEN"),
        COFT_TOKEN("COFT_TOKEN"),
        ISO_CARD("ISO_CARD");

        private String assetType;

        assetType(String assetType) {
            this.assetType = assetType;
        }

        public String toString() {
            return this.assetType;
        }

    }

}
