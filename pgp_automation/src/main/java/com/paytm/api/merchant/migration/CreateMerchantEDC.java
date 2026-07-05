package com.paytm.api.merchant.migration;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;


public class CreateMerchantEDC extends BaseApi{


    public CreateMerchantEDC() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("x-real-ip","127.0.0.1");
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MerchantMigration.CREATE_MERCHANT);

    }

    protected String requestTemplatePath() {
        return "merchantMigration/CreateMerchant/CreateMerchantEDCRequestBody.json";
    }


    public CreateMerchantEDC  buildRequest(String requestId){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_ID",requestId);
        // setContext(".createMerReq.MERCHANT_DETAILS.REQUEST_TYPE_NAME", variable2)
        return this;
    }


    public CreateMerchantEDC setRequestID(String requestID){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_ID",requestID);
        return this;
    }

    public CreateMerchantEDC setRequestType(String requestType){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_TYPE_NAME",requestType);
        return this;
    }

    public CreateMerchantEDC setCommisionFeeType(String feeType, String percentCommission, String commissionTypeBoth ){
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.FEE_TYPE",feeType);
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.PERCENT_COMMISSION",percentCommission);
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.COMMISSION_TYPE_BOTH",commissionTypeBoth);
        return this;
    }

    public CreateMerchantEDC setPaymodes(String paymode){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[0].PAY_MODE",paymode);
        return this;
    }

    public CreateMerchantEDC setPaymodewithBank(String paymode, String bank){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].PAY_MODE",paymode);
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].BANKS[0].BANK", bank);
        return this;
    }

    public CreateMerchantEDC setPaymode(String paymode){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].PAY_MODE",paymode);
        return this;
    }

    public CreateMerchantEDC setPaymode(String paymode,String feetype,double percent_comm,String comm_type,int paymodeSeq,String bank,int txntypeseq){
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].PAY_MODE",paymode);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.FEE_TYPE",feetype);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.PERCENT_COMMISSION",percent_comm);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.COMMISSION_TYPE_BOTH",comm_type);
        return this;
    }

    public CreateMerchantEDC setStaticPreferences(String preferences){
        ArrayList<String> arr = new ArrayList<>();
        arr.add(preferences);
        setContext("createMerReq.MERCHANT_DETAILS.STATIC_PREF", arr);
        return this;
    }

    public CreateMerchantEDC setSettleFreeze(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.SETTLE_FREEZE",preferences);
        return this;
    }

    public CreateMerchantEDC setAggregatorEnable(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.AGGREGATOR_ENABLE",preferences);
        return this;
    }

    public CreateMerchantEDC setOnlineSettlement(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.ONLINE_SETTLEMENT",preferences);
        return this;
    }

    public CreateMerchantEDC setPreference(String preference){
        setContext("createMerReq.MERCHANT_DETAILS.PREFERENCE."+preference+"","True");
        return this;
    }

    public CreateMerchantEDC setNoOfRetry(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.NUMBER_OF_RETRY",preferences);
        return this;
    }


    public CreateMerchantEDC setSolutionType(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.SOLUTION_TYPE",preferences);
        return this;
    }


    public CreateMerchantEDC setMerchantType(String merchantType){
        setContext("createMerReq.MERCHANT_DETAILS.MERCHANT_TYPE",merchantType);
        return this;
    }


    public CreateMerchantEDC setMerchantPPILimit(String merchantPPILimit){
        setContext("createMerReq.MERCHANT_DETAILS.PPI_LIMITED_MERCHANT",merchantPPILimit);
        return this;
    }

    public CreateMerchantEDC setMobileNumber(String mobileNumber){
        setContext("createMerReq.MERCHANT_DETAILS.MOBILE_NUMBER",mobileNumber);
        return this;
    }

    public CreateMerchantEDC setPhoneNumber(String phoneNumber){
        setContext("createMerReq.MERCHANT_DETAILS.PHONE_NUMBER",phoneNumber);
        return this;
    }

    public CreateMerchantEDC setCustId(String custId){
        setContext("CUST_ID",custId);
        return this;
    }

    public CreateMerchantEDC setTxnType(String txnType,int seq){
        setContext("configureMbidAndInstrument.TXN_TYPES["+seq+"].TXN_TYPE",txnType);
        return this;
    }










}
