package com.paytm.appconstants;

public interface FF4JFeatures {

    //LPV and Cashier Pay changes
    String FETCH_SAVED_CARD_FROM_PLATFORM_FOR_MID_CUSTID = "fetchSavedcardFromPlatformForMidCustId";
    String FETCH_SAVED_CARD_FROM_PLATFORM_FOR_USERID = "fetchSavedcardFromPlatformForUserId";
    String SAVE_CARD_AT_PLATFORM_ON_MID_CUSTID = "saveCardAtPlatformOnMidCustId";
    String SAVE_CARD_AT_PLATFORM_ON_USERID = "saveCardAtPlatformOnUserId";
    String THEIA_SEND_CIN_AND_8BIN_HASH_TO_PROMO = "theia.sendCINAnd8BinHashToPromo";
    
    String RETURN_SAVED_CARDS_FROM_PLATFORM_FOR_MIDCUSTID = "returnSavedCardsFromPlatformForMidCustId";
    String RETURN_SAVED_CARDS_FROM_PLATFORM_FOR_USERID = "returnSavedCardsFromPlatformForUserId";
    String SHORT_CIRCUIT_SAVED_CARD_SERVICE_READ_FOR_MID_CUSTID = "shortCircuitSavedCardServiceReadForMidCustId";
    String SHORT_CIRCUIT_SAVED_CARD_SERVICE_READ_FOR_USERID = "shortCircuitSavedCardServiceReadForUserId";
    String THEIA_FILTER_PLATFORM_SAVED_ASSETS = "theia.filterPlatformSavedAssets";

    //Subs changes
    String FETCH_SAVE_CARD_FROM_PLATFORM_FOR_SUBS = "fetchSaveCardfromPlatformForSubs";
    String FETCH_CARD_INFO_FROM_PLATFORM_FOR_SUBS = "fetchCardInfoFromPlatfromForSubs";
    
    String BLOCKING_FILTER_SAVED_ASSETS_FROM_PLATFORM_FOR_SUBS = "blockingFilterSavedAssetsFromPlatformForSubs";

    //CC Bill Proxy changes
    String QUERY_NON_SENSITIVE_FOR_CCBILL_PAYMENT = "queryNonSensitiveForCCBillPayment";
    String BILLPROXY_SHORT_CIRCUIT_READ_FROM_REDIS_CACHE = "billproxy.shortcircuitReadFromRedisCache";
    String BILLPROXY_SHORT_CIRCUIT_UPDATE_IN_BILL_PAYMENT = "billproxy.shortCircuitUpdateInBillPayment";
    
    //Saved card sevice(Open APIs) changes
    String SC_FETCH_FROM_PLATFORM_FOR_MID_CUSTID = "scFetchFromPlatformForMidCustid";
    String SC_FETCH_FROM_PLATFORM_FOR_USERID = "scFetchFromPlatformForUserId";
    String SC_FETCH_FROM_PLATFORM_PERCENTAGE_FEATURE = "scFetchFromPlatformPercentageFeature";
    String SC_FETCH_FROM_PLATFORM_REPLACE_ASTRICK_TO_FEATURE ="scFetchFromPlatformReplaceAstrickToXFeature";
    String SC_PLATFORMSAVEDCARDUSERID="sc_returnSavedCardFromPlatformForUserId";
    String SC_ADDITIONALINFO="scAddAdditionalInfoInOpenApi";

    String SC_FETCH_FROM_PLATFORM_FOR_MID = "scFetchFromPlatformForMid";
    String THEIA_GETMERCHANT_API_URL_INFO = "theia-srv.getMerchantApiUrlInfo";
    String THEIA_UIMICROSERVICE_ENHANCEDFLOW_FEATURE="theia.uimicroservice.enhancedflow.feature";
    String THEIA_UIMICROSERVICE_RISKFLOW_FEATURE="theia.uimicroservice.riskflow.feature";
    String THEIA_UIMICROSERVICE_GVCONSENTFLOW_FEATURE="theia.uimicroservice.gvconsentflow.feature";
    String THEIA_ADD_MONEY_SOURCE_CONSULT_ENABLE= "theia.add.money.source.consult.enable";
    String THEIA_BLACKLIST_LPV_ACCESS_TOKEN= "theia.blacklistLPVfromFPOV2WithAccessToken";
    String APP_INVOKE_PHASE2 = "theia.autoAppInvokePhase2";
    String APP_INVOKE_PHASE3 = "theia.v3.appinvoke.support.feature";

    //flag is enabled on Prod
    String CREATE_ORDER_IN_INTTXN= "createOrderinIntTxn";
    String PREPAID_CARD= "prepaidCard";


    String THEIA_V1_TXN_STATUS_UPI_POLLING="theia.Use.V1.Txn.Status.For.UPI.Polling";

    String EXEMPT_MIDLIST_FROM_CLOSEORDER_WITH_PENDING_STATUS = "exemptMidListFromCloseOrderWithPendingStatus";
    String THEIA_FETCHBINDETAILFORNATIVEOTPELIGIBILITY = "theia.enableFetchBinDetailsFromBinCenterForNativeOtpEligibility";
    String LAST_FOUR_DIGITS_IN_STATUS = "merchantStatus.mapMaskedAssetNoToMaskedCardNo";
    String THEIA_ENABLE_AXIS_VPA_VALIDATE="theia.enableAxisVpaValidate";
    String THEIA_ENABLE_AXIS_VPA_VALIDATE_ON_STANDARD_FLOW= "theia.enableAxisVpaValidateOnStandardFlow";
    String PASS_CALLBACK_URL_FOR_ONETIME_FLOW="theia.passCallbackurlForOnetimeFlow";
    String INSTA_ENABLE_YBL_FAILURE_REQAUTH = "INSTA_ENABLE_YBL_FAILURE_REQAUTH";

    // Payment Service DQR changes
    String PAYMENT_SERVICE_ADD_ACQUIREMENT_ID_IN_DQR = "payment.service.add.acquirement.id.in.dqr";

    String THEIA_UPIPSP_CLOSE_ORDER_FOR_SUBSCRIPTION_RISK_REJECT = "theia.upipsp.closeOrderForSubscriptionRiskReject";
    String THEIA_UPIPSP_CLOSE_ORDER_FOR_ALLOWED_TPAPS = "theia.upipsp.closeOrderForAllowedTpaps";
}
