package com.paytm.api.merchant.migration;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;


public class EditMerchant extends CreateMerchant{


    public EditMerchant() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MerchantMigration.EDIT_MERCHANT);

    }

    protected String requestTemplatePath() {
        return "merchantMigration/createMerchant/createMerchantRequestBody.json";
    }


    public EditMerchant buildRequest(String requestId){

        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_ID",requestId);
       // setContext(".createMerReq.MERCHANT_DETAILS.REQUEST_TYPE_NAME", variable2)
        return this;
    }


    public EditMerchant setRequestID(String requestID){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_ID",requestID);
        return this;
    }

    public EditMerchant setMID(String mid){
        setContext("createMerReq.MERCHANT_DETAILS.MID",mid);
        return this;
    }

    public EditMerchant setRequestType(String requestType){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_TYPE_NAME",requestType);
        return this;
    }

    public EditMerchant setCommisionFeeType(String feeType, String percentCommission, String commissionTypeBoth ){
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.FEE_TYPE",feeType);
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.PERCENT_COMMISSION",percentCommission);
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.COMMISSION_TYPE_BOTH",commissionTypeBoth);
        return this;
    }


    public EditMerchant setPaymodes(String paymode){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].PAY_MODE",paymode);
        return this;
    }

    public EditMerchant setPaymodes(String paymode1, String paymode2){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[0].PAY_MODE",paymode1);
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].PAY_MODE",paymode1);
        return this;
    }
    public CreateMerchant setTxnType(String txnType,int seq){
        setContext("configureMbidAndInstrument.TXN_TYPES["+seq+"].TXN_TYPE",txnType);
        return this;
    }
    public CreateMerchant setPaymode(String paymode,String feetype,double percent_comm,String comm_type,int paymodeSeq,String bank,int txntypeseq){
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].PAY_MODE",paymode);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.FEE_TYPE",feetype);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.PERCENT_COMMISSION",percent_comm);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.COMMISSION_TYPE_BOTH",comm_type);
        return this;
    }

}
