package com.paytm.api.merchant.migration;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class CreateMerchant extends BaseApi{


    public CreateMerchant() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("x-real-ip","127.0.0.1");
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MerchantMigration.CREATE_MERCHANT);

    }

    protected String requestTemplatePath() {
        return "merchantMigration/createMerchant/createMerchantRequestBody.json";
    }

    String pg2Request="{\"createMerReq\": {\n" +
            "\"ACTION\": \"Submit for Approval\",\n" +
            "\"CREATED_BY\": \"sobeer_sales\",\n" +
            "\"DOCS_DETAILS\": {\n" +
            "\"DETAILED_LIST\": [\n" +
            "\"PAN CARD\"\n" +
            "]\n" +
            "},\n" +
            "\"MERCHANT_DETAILS\": {\n" +
            "\"ACCOUNT_FOR\": \"unifiedMerchantPanel\",\n" +
            "\"ACCOUNT_PRIMARY\": \"FALSE\",\n" +
            "\"ADDRESS1\": \"E-16\",\n" +
            "\"ADDRESS2\": \"Sector9\",\n" +
            "\"ADDRESS3\": \"NewVijayNagar\",\n" +
            "\"API_DISABLED\": \"TRUE\",\n" +
            "\"BUSINESS_NAME\": \"VishalG\",\n" +
            "\"BUSINESS_TYPE\": \"INDIVIDUAL\",\n" +
            "\"BW_CONFIG\": {\n" +
            "\"AUTO\": \"TRUE\",\n" +
            "\"TRANSFER_MODE\": \"M2B\",\n" +
            "\"TRIGGER_MODE\": \"TIME_INTERVAL\",\n" +
            "\"TRIGGER_VALUE\": \"1\"\n" +
            "},\n" +
            "\"BW_ENABLED\": \"FALSE\",\n" +
            "\"MERCHANT_INDUSTRY_TYPE\": \"BIG\",\n" +
            "\"CALLBACK_URL_ENABLED\": \"TRUE\",\n" +
            "\"CAN_EDIT_PMOBILE\": \"TRUE\",\n" +
            "\"CATEGORY\": \"BFSI\",\n" +
            "\"CITY\": \"Ghaziabad\",\n" +
            "\"COMM_STAT_SELECT\": \"3\",\n" +
            "\"COMMISSION_TYPE\": \"MONTHLY\",\n" +
            "\"CONVENIENCE_FEE_TYPE\": \"1\",\n" +
            "\"COUNTRY\": \"India\",\n" +
            "\"CURRENCY\": \"INR\",\n" +
            "\"CUSTOM\": \"CUSTOM\",\n" +
            "\"CUSTOM_NAME\": \"qa8Aut\",\n" +
            "\"EMAIL_CONSUMER\": \"TRUE\",\n" +
            "\"FIRST_NAME\": \"NehaMadan\",\n" +
            "\"INDUSTRY_TYPE\": \"Retail\",\n" +
            "\"IS_COMMISSION\": \"TRUE\",\n" +
            "\"KYC_AUTHORIZED_SIGNATORY_NAME\": \"Neha\",\n" +
            "\"KYC_AUTHORIZED_SIGNATORY_PAN_NO\": \"BPOPS3447K\",\n" +
            "\"KYC_BANK_ACCOUNT_HOLDER_NAME\": \"Amritansh\",\n" +
            "\"KYC_BANK_ACCOUNT_NO\": \"601148562499\",\n" +
            "\"KYC_BANK_NAME\": \"PAYTM\",\n" +
            "\"KYC_BUSINESS_IFSC_NO\": \"PYTM0123456\",\n" +
            "\"KYC_BUSINESS_PAN_NO\": \"BOOPS3447K\",\n" +
            "\"LAST_NAME\": \"Madan\",\n" +
            "\"MERCHANT_NAME\": \"VishalF\",\n" +
            "\"MERCHANT_TYPE\": \"NonSD\",\n" +
            "\"MID\": \"\",\n" +
            "\"MOBILE_NUMBER\": \"8512005349\",\n" +
            "\"NUMBER_OF_RETRY\": \"2\",\n" +
            "\"OB_CHANNEL\": \"UMP_WEB\",\n" +
            "\"OFFLINE_ENABLED\": \"FALSE\",\n" +
            "\"ONLINE_SETTLEMENT\": \"FALSE\",\n" +
            "\"P_EMAIL\": \"neha.madan@paytm.com\",\n" +
            "\"P2M_ENABLED\": \"false\",\n" +
            "\"P2P_MERCHANT\": \"TRUE\",\n" +
            "\"PHONE_NUMBER\": \"8512005349\",\n" +
            "\"PIN\": \"201009\",\n" +
            "\"POST_APPROVED_NB\": \"FALSE\",\n" +
            "\"PPI_LIMITED_MERCHANT\": \"0\",\n" +
            "\"PRE_APPROVED_NB\": \"FALSE\",\n" +
            "\"PROFILE_ID\": \"1\",\n" +
            "\"REFUND_TO_BANK_ENABLED\": \"TRUE\",\n" +
            "\"REQUEST_ID\": \"07nov2022001202\",\n" +
            "\"REQUEST_NAME\": \"DP2Web\",\n" +
            "\"REQUEST_TYPE_NAME\": \"DEFAULT\",\n" +
            "\"SAME_AS_BUSINESS_ADDR\": \"TRUE\",\n" +
            "\"SIZE_OF_KEY\": \"16\",\n" +
            "\"SOLUTION_TYPE\": \"OFFLINE\",\n" +
            "\"SOURCE_ID\": \"OE\",\n" +
            "\"STATE\": \"Uttar Pradesh\",\n" +
            "\"STATIC_PREF\": [\n" +
            "\"PG2_DATA_MIGRATION\",\n" +
            "\"FULL_PG2_TRAFFIC_ENABLED\",\n" +
            "\"GST_EXEMPTED\"\n" +
            "],\n" +
            "\"STORE_CARD_DETAILS\": \"NO\",\n" +
            "\"SUB_CATEGORY\": \"Loans\",\n" +
            "\"TRANSACTION_WISE_SETTLEMENT\": \"FALSE\",\n" +
            "\"USER_NAME\": \"PTMUAT00845\",\n" +
            "\"VALID_FROM\": \"05/31/2018\",\n" +
            "\"VALID_TO\": \"05/31/2021\",\n" +
            "\"WALLET_ONLY_ENABLED\": \"FALSE\",\n" +
            "\"WALLET_RECHARGE_OPT\": \"NO_RECHARGE\"\n" +
            "},\n" +
            "    \"URL_DETAILS\": [\n" +
            "      {\n" +
            "        \"WEBSITE_NAME\": \"retail\",\n" +
            "        \"REQUEST_URL\": \"https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse\",\n" +
            "        \"RESPONSE_URL\": \"https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse\",\n" +
            "        \"PEON_URL\": \"https://pgp-automation.paytm.in/mockbank/peon\",\n" +
            "        \"IMAGE_NAME\": \"paytm_log\"\n" +
            "      }\n" +
            "    ],\n" +
            "    \"DOCS_DETAILS\": {\n" +
            "      \"DETAILED_LIST\": [\n" +
            "        \"PAN CARD\"\n" +
            "      ]\n" +
            "    }\n" +
            "  },\n" +
            "  \"configVelocity\": {\n" +
            "    \"VELOCITIES\": [\n" +
            "      {\n" +
            "        \"VELOCITY_TYPE\": \"PER_MID\",\n" +
            "        \"VELOCITY_DETAILS\": {\n" +
            "          \"MAX_AMT_PER_DAY\": \"-1\",\n" +
            "          \"MAX_AMT_PER_MONTH\": \"-1\"\n" +
            "        }\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"configureMbidAndInstrument\": {\n" +
            "    \"configureMerchantCommission\": {\n" +
            "      \"ACTION\": \"EDIT\",\n" +
            "      \"COMMISSION\": {\n" +
            "        \"FEE_TYPE\": \"simple\",\n" +
            "        \"PERCENT_COMMISSION\": \"1.00\",\n" +
            "        \"COMMISSION_TYPE_BOTH\": \"FALSE\"\n" +
            "      }\n" +
            "    },\n" +
            "    \"TXN_TYPES\": [\n" +
            "      {\n" +
            "        \"TXN_TYPE\": \"Payments\",\n" +
            "        \"PAY_MODES\": [\n" +
            "          {\n" +
            "            \"PAY_MODE\": \"PPI\",\n" +
            "            \"COMMISSION\": {\n" +
            "              \"FEE_TYPE\": \"simple\",\n" +
            "              \"PERCENT_COMMISSION\": \"2.00\",\n" +
            "              \"COMMISSION_TYPE_BOTH\": \"FALSE\"\n" +
            "            },\n" +
            "            \"BANKS\": [\n" +
            "\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"TXN_TYPE\": \"EDC\",\n" +
            "        \"PAY_MODES\": [\n" +
            "          {\n" +
            "            \"PAY_MODE\": \"CC\",\n" +
            "            \"COMMISSION\": {\n" +
            "              \"FEE_TYPE\": \"simple\",\n" +
            "              \"PERCENT_COMMISSION\": \"2.00\",\n" +
            "              \"COMMISSION_TYPE_BOTH\": \"FALSE\"\n" +
            "            },\n" +
            "            \"FEE_RATE_FACTORS\": [\n" +
            "              {\n" +
            "                \"FEE_LEVEL\": {\n" +
            "                  \"IS_CORP_CARD\": \"TRUE\"\n" +
            "                },\n" +
            "                \"COMMISSION\": {\n" +
            "                  \"FEE_TYPE\": \"simple\",\n" +
            "                  \"PERCENT_COMMISSION\": \"1.25\",\n" +
            "                  \"COMMISSION_TYPE_BOTH\": \"FALSE\"\n" +
            "                }\n" +
            "              }\n" +
            "            ],\n" +
            "            \"BANKS\": [\n" +
            "\n" +
            "            ]\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"CUST_ID\": \"1001822960\"\n" +
            "}";
    public CreateMerchant(String isPG2) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("x-real-ip","127.0.0.1");
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getPG2Request());
        getRequestSpecBuilder().setBasePath(Constants.MerchantMigration.CREATE_MERCHANT);

    }
    public String getPG2Request() {return pg2Request;}



    public CreateMerchant  buildRequest(String requestId){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_ID",requestId);
       // setContext(".createMerReq.MERCHANT_DETAILS.REQUEST_TYPE_NAME", variable2)
        return this;
    }


    public CreateMerchant setRequestID(String requestID){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_ID",requestID);
        return this;
    }

    public CreateMerchant setRequestType(String requestType){
        setContext("createMerReq.MERCHANT_DETAILS.REQUEST_TYPE_NAME",requestType);
        return this;
    }

    public CreateMerchant setCommisionFeeType(String feeType, String percentCommission, String commissionTypeBoth ){
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.FEE_TYPE",feeType);
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.PERCENT_COMMISSION",percentCommission);
        setContext("configureMbidAndInstrument.configureMerchantCommission.COMMISSION.COMMISSION_TYPE_BOTH",commissionTypeBoth);
        return this;
    }

    public CreateMerchant setPaymodes(String paymode){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[0].PAY_MODE",paymode);
        return this;
    }

    public CreateMerchant setPaymodewithBank(String paymode, String bank){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].PAY_MODE",paymode);
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].BANKS[0].BANK", bank);
        return this;
    }

    public CreateMerchant setPaymode(String paymode){
        setContext("configureMbidAndInstrument.TXN_TYPES[0].PAY_MODES[1].PAY_MODE",paymode);
        return this;
    }

    public CreateMerchant setPaymode(String paymode,String feetype,double percent_comm,String comm_type,int paymodeSeq,String bank,int txntypeseq){
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].PAY_MODE",paymode);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.FEE_TYPE",feetype);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.PERCENT_COMMISSION",percent_comm);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.COMMISSION_TYPE_BOTH",comm_type);
        return this;
    }

    public CreateMerchant setStaticPreferences(String preferences){
        ArrayList<String> arr = new ArrayList<>();
        arr.add(preferences);
        setContext("createMerReq.MERCHANT_DETAILS.STATIC_PREF", arr);
        return this;
    }

    public CreateMerchant setSettleFreeze(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.SETTLE_FREEZE",preferences);
        return this;
    }

    public CreateMerchant setAggregatorEnable(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.AGGREGATOR_ENABLE",preferences);
        return this;
    }

    public CreateMerchant setIsAggregator(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.IS_AGGREGATOR",preferences);
        return this;
    }

    public CreateMerchant setOnlineSettlement(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.ONLINE_SETTLEMENT",preferences);
        return this;
    }

    public CreateMerchant setPreference(String preference){
        setContext("createMerReq.MERCHANT_DETAILS.PREFERENCE."+preference+"","True");
        return this;
    }

    public CreateMerchant setNoOfRetry(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.NUMBER_OF_RETRY",preferences);
        return this;
    }


    public CreateMerchant setSolutionType(String preferences){
        setContext("createMerReq.MERCHANT_DETAILS.SOLUTION_TYPE",preferences);
        return this;
    }


    public CreateMerchant setMerchantType(String merchantType){
        setContext("createMerReq.MERCHANT_DETAILS.MERCHANT_TYPE",merchantType);
        return this;
    }

    public CreateMerchant setBWEnabled(String merchantType){
        setContext("createMerReq.MERCHANT_DETAILS.BW_ENABLED",merchantType);
        return this;
    }

    public CreateMerchant setMerchantPPILimit(String merchantPPILimit){
        setContext("createMerReq.MERCHANT_DETAILS.PPI_LIMITED_MERCHANT",merchantPPILimit);
        return this;
    }

    public CreateMerchant setMobileNumber(String mobileNumber){
        setContext("createMerReq.MERCHANT_DETAILS.MOBILE_NUMBER",mobileNumber);
        return this;
    }

    public CreateMerchant setPhoneNumber(String phoneNumber){
        setContext("createMerReq.MERCHANT_DETAILS.PHONE_NUMBER",phoneNumber);
        return this;
    }

    public CreateMerchant setCustId(String custId){
        setContext("CUST_ID",custId);
        return this;
    }

    public CreateMerchant setTxnType(String txnType,int seq){
        setContext("configureMbidAndInstrument.TXN_TYPES["+seq+"].TXN_TYPE",txnType);
        return this;
    }
//    public CreateMerchant setInstantSettlementType(String preference){
//        setContext("createMerReq.MERCHANT_DETAILS.INSTANT_SETTLEMENT_TYPE",preference);
//        return this;
//    }

    public CreateMerchant setRefundToBankEnabled(String refundToBankEnabled){
        setContext("createMerReq.MERCHANT_DETAILS.REFUND_TO_BANK_ENABLED",refundToBankEnabled);
        return this;
    }
    public CreateMerchant setBWManualType(String BWManualType){
        setContext("createMerReq.MERCHANT_DETAILS.BW_MANUAL_TYPE",BWManualType);
        return this;
    }

    public CreateMerchant setMerchantIndustryType(String MerchantIndustryType){
        setContext("createMerReq.MERCHANT_DETAILS.MERCHANT_INDUSTRY_TYPE",MerchantIndustryType);
        return this;
    }




    public CreateMerchant setKYCBankName(String KYCBankName){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_BANK_NAME",KYCBankName);
        return this;
    }
    public CreateMerchant setKYCBankAccountHolderName(String KYCBankAccountHolderName){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_BANK_ACCOUNT_HOLDER_NAME",KYCBankAccountHolderName);
        return this;
    }
    public CreateMerchant setKYCBankAccounNo(String KYCBankAccounNo){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_BANK_ACCOUNT_NO",KYCBankAccounNo);
        return this;
    }
    public CreateMerchant setKYCBusinessPanNo(String KYCBusinessPanNo){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_BUSINESS_PAN_NO",KYCBusinessPanNo);
        return this;
    }
    public CreateMerchant setKYCBusinessGstin(String KYCBusinessGstin){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_BUSINESS_GSTIN",KYCBusinessGstin);
        return this;
    }
    public CreateMerchant setKYCBusinessIfscNo(String KYCBusinessIfscNo){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_BUSINESS_IFSC_NO",KYCBusinessIfscNo);
        return this;
    }
    public CreateMerchant setKYCAuthorizedSignatoryName(String KYCAuthorizedSignatoryName){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_AUTHORIZED_SIGNATORY_NAME",KYCAuthorizedSignatoryName);
        return this;
    }
    public CreateMerchant setKYCAuthorizedSignatoryIdProofNo(String KYCAuthorizedSignatoryIdProofNo){
        setContext("createMerReq.MERCHANT_DETAILS.KYC_AUTHORIZED_SIGNATORY_ID_PROOF_NO",KYCAuthorizedSignatoryIdProofNo);
        return this;
    }

    public CreateMerchant setBillingAddrSameAsBusinessAddr(String BillingAddrSameAsBusinessAddr){
        setContext("createMerReq.MERCHANT_DETAILS.BILLING_ADDR_SAME_AS_BUSINESS_ADDR",BillingAddrSameAsBusinessAddr);
        return this;
    }
    public CreateMerchant setCommStatSelect(String CommStatSelect){
        setContext("createMerReq.MERCHANT_DETAILS.COMM_STAT_SELECT",CommStatSelect);
        return this;
    }
    public CreateMerchant setEmailMerchant(String EmailMerchant){
        setContext("createMerReq.MERCHANT_DETAILS.EMAIL_MERCHANT",EmailMerchant);
        return this;
    }
    public CreateMerchant setEmailConsumer(String EmailConsumer){
        setContext("createMerReq.MERCHANT_DETAILS.EMAIL_CONSUMER",EmailConsumer);
        return this;
    }
    public CreateMerchant setWebsiteName(String WebsiteName){
        setContext("createMerReq.MERCHANT_DETAILS.WEBSITE_NAME",WebsiteName);
        return this;
    }
    public CreateMerchant setApiDisabled(String ApiDisabled){
        setContext("createMerReq.MERCHANT_DETAILS.API_DISABLED",ApiDisabled);
        return this;
    }
    public CreateMerchant setWalletOnlyEnabled(String WalletOnlyEnabled){
        setContext("createMerReq.MERCHANT_DETAILS.WALLET_ONLY_ENABLED",WalletOnlyEnabled);
        return this;
    }


    public CreateMerchant setPaymodesWithFeeFactors(String paymode, String feetype, double percent_comm, String comm_type, int paymodeSeq, String bank, int txntypeseq, HashMap<String,String>feeLevel){
        System.out.println("inside the methods------");
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].PAY_MODE",paymode);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.FEE_TYPE",feetype);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.PERCENT_COMMISSION",percent_comm);
        setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].COMMISSION.COMMISSION_TYPE_BOTH",comm_type);
        Iterator<String> keys = feeLevel.keySet().iterator();
        while (keys.hasNext()){
            String name = keys.next();
            String value=feeLevel.get(name);
            setContext("configureMbidAndInstrument.TXN_TYPES[" + txntypeseq + "].PAY_MODES[" + paymodeSeq + "].FEE_RATE_FACTORS[" + paymodeSeq + "].FEE_LEVEL."+name+"", value);
        }

           setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].FEE_RATE_FACTORS["+paymodeSeq+"].COMMISSION.FEE_TYPE","simple");
           setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].FEE_RATE_FACTORS["+paymodeSeq+"].COMMISSION.PERCENT_COMMISSION","0.04");
           setContext("configureMbidAndInstrument.TXN_TYPES["+txntypeseq+"].PAY_MODES["+paymodeSeq+"].FEE_RATE_FACTORS["+paymodeSeq+"].COMMISSION.COMMISSION_TYPE_BOTH","FALSE");
        return this;
    }

    public CreateMerchant setCustomName(String MerchantIndustryType){
        setContext("createMerReq.MERCHANT_DETAILS.CUSTOM_NAME",MerchantIndustryType);
        return this;
    }

    public CreateMerchant setMultipleStaticpref(List<String >staticPref){
        setContext("createMerReq.MERCHANT_DETAILS.STATIC_PREF",staticPref);
        return this;
    }

    public CreateMerchant setTransactionWiseSettlement(String transactionWiseSettlement){
        setContext("createMerReq.MERCHANT_DETAILS.TRANSACTION_WISE_SETTLEMENT",transactionWiseSettlement);
        return this;
    }


}
