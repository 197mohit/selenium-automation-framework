package com.paytm.api.linkAPI;

import com.paytm.LocalConfig;
import com.paytm.api.CallBackApi;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.*;

import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.luaj.vm2.ast.Str;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.FetchBinDetailResponse;
import com.paytm.api.AOA.AddGateway;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CreateNewLink extends BaseApi {

    public static String getExpiryDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Calendar c = Calendar.getInstance();
        String date = sdf.format(c.getTime());
        String output=CommonHelpers.addDays(date,"dd/MM/yyyy",5);
        return output;
    }
    public static String getSubsExpiryDate(){
        SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd");
        Calendar c1=Calendar.getInstance();
        String date=sdf1.format(c1.getTime());
        String output=CommonHelpers.addDays(date,"yyyy-MM-dd",5);
        return output;
    }

    String request = "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhbW91bnQiOiIyMy4wMCIsImlzcyI6IlVNUCIsIm1heFBheW1lbnRzQWxsb3dlZCI6IjEwIiwibWlkIj\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "         \"buttonDetails\": {\n" +
            "                \"buttonType\": null,\n" +
            "                \"buttonTitle\": null,\n" +
            "                \"buttonStyle\": null,\n" +
            "                \"buttonSize\": null,\n" +
            "                \"sourceApp\": null\n" +
            "            },\n" +
            "        \"redirectionUrlSuccess\": null,\n" +
            "        \"redirectionUrlFailure\": null,\n" +
            "        \"customPaymentSuccessMessage\": null,\n" +
            "        \"customPaymentFailureMessage\": null,\n" +
            "      \n" +
            "        \"mid\": \"testli61258254741921\",\n" +
            "        \"linkName\": \"TestingLink\",\n" +
            "        \"linkDescription\": \"123\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"invoiceId\": \"70nir023456789010201\",\n" +
            "        \"expiryDate\": "+getExpiryDate()+",\n" +
            "        \"amount\": \"200\",\n" +
            "        \"isActive\": \"true\",\n" +
            "        \"sendSms\": \"true\",\n" +
            "        \"sendEmail\": \"true\",\n" +
            "        \"customerId\":\"3454\",\n" +
            "        \"customerContact\": {\n" +
            "            \"customerName\": \"nirottam\",\n" +
            "        \"customerEmail\": \"nirottam.singh@paytm.com\",\n" +
            "            \"customerMobile\": \"7014107741\"\n" +
            "        },\n" +
            "        \"statusCallbackUrl\": \"https://example.test/test\",\n" +
            "        \"invoicePhoneNo\": \"9784974874\",\n" +
            "        \"invoiceEmail\": \"nimit.bhatia@paytm.com\",\n" +
            "        \"invoiceDetails\": [\n" +
            "            {\n" +
            "                \"productName\": \"laptop\",\n" +
            "                \"productCode\": \"P101\",\n" +
            "                \"noOfUnits\": \"1\",\n" +
            "                \"perUnitAmount\": 200,\n" +
            "                \"perUnitTax\": [\n" +
            "                    {\n" +
            "                        \"taxName\": \"SGST\",\n" +
            "                        \"taxAmount\": \"0.0\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ],\n" +
            "  \"additionalMFInfo\": {\n" +
            "        \"ref1\": \"abc\",\n" +
            "        \"ref2\": \"qwe\",\n" +
            "        \"ref3\": \"asdf\",\n" +
            "        \"ref4\": \"wed\",\n" +
            "        \"ref5\": \"wed\",\n" +
            "        \"ref6\": \"wed\",\n" +
            "        \"ref7\": \"wed\",\n" +
            "        \"ref8\": \"wed\",\n" +
            "        \"ref9\": \"ref9_value\",\n" +
            "        \"ref10\": \"ref10_value\",\n" +
            "        \"ref11\": \"ref11_value\",\n" +
            "        \"ref12\": \"ref12_value\"\n" +
            "        },\n" +
            "        \"reminderDetails\": null,\n" +
            "        \"subscriptionDetails\": {\n" +
            "            \"planId\": \"null\",\n" +
            "            \"subscriptionExpiry\": \"null\",\n" +
            "            \"renewalAmount\": \"null\",\n" +
            "            \"txnAmount\": \"null\"" +
            "         },\n" +
            "        \"bindLinkIdMobile\": null\n" +
            "    }\n" +
            "}";


    String RequestEnableDisable = "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhbW91bnQiOiIyMy4wMCIsImlzcyI6IlVNUCIsIm1heFBheW1lbnRzQWxsb3dlZCI6IjEwIiwibWlkIj\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "         \"buttonDetails\": null,\n" +
            "        \"redirectionUrlSuccess\": null,\n" +
            "        \"redirectionUrlFailure\": null,\n" +
            "        \"customPaymentSuccessMessage\": null,\n" +
            "        \"customPaymentFailureMessage\": null,\n" +
            "      \n" +
            "        \"mid\": \"testli61258254741921\",\n" +
            "        \"linkName\": \"TestingLink\",\n" +
            "        \"linkDescription\": \"123\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"invoiceId\": \"70nir023456789010201\",\n" +
            "        \"expiryDate\": "+getExpiryDate()+",\n" +
            "        \"amount\": \"200\",\n" +
            "        \"isActive\": \"true\",\n" +
            "        \"sendSms\": \"false\",\n" +
            "        \"sendEmail\": \"false\",\n" +
            "        \"customerId\":\"3454\",\n" +
            "        \"enablePaymentMode\": [\n" +
            "            {\n" +
            "                \"mode\": \"\",\n" +
            "                \"channels\": []\n" +
            "            },\n" +
            "            {\n" +
            "                \"mode\": \"\",\n" +
            "                \"channels\": []\n" +
            "            }\n" +

            "        ],\n" +
            "        \"disablePaymentMode\": [\n" +
            "            {\n" +
            "                \"mode\": \"\",\n" +
            "                \"channels\": []\n" +
            "            }\n" +
            "        ],\n" +
            "        \"statusCallbackUrl\": \"https://example.test/test\",\n" +
            "        \"invoicePhoneNo\": \"9784974874\",\n" +
            "        \"invoiceEmail\": \"nimit.bhatia@paytm.com\",\n" +
            "        \"invoiceDetails\": [\n" +
            "            {\n" +
            "                \"productName\": \"laptop\",\n" +
            "                \"productCode\": \"P101\",\n" +
            "                \"noOfUnits\": \"1\",\n" +
            "                \"perUnitAmount\": 200,\n" +
            "                \"perUnitTax\": [\n" +
            "                    {\n" +
            "                        \"taxName\": \"SGST\",\n" +
            "                        \"taxAmount\": \"0.0\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ],\n" +
            "\"buttonDetails\": {\n" +
            "        \"buttonType\": \"Donate\",\n" +
            "                \"buttonTitle\": \"Donate for corona\",\n" +
            "                \"buttonStyle\": \"Bold\",\n" +
            "                \"buttonSize\": \"14\",\n" +
            "                \"sourceApp\": \"https://example.test/test\"\n" +
            "    },\n" +
            "  \"additionalMFInfo\": {\n" +
            "        \"ref1\": \"abc\",\n" +
            "        \"ref2\": \"qwe\",\n" +
            "        \"ref3\": \"asdf\",\n" +
            "        \"ref4\": \"wed\",\n" +
            "        \"ref5\": \"wed\",\n" +
            "        \"ref6\": \"wed\",\n" +
            "        \"ref7\": \"wed\",\n" +
            "        \"ref8\": \"wed\",\n" +
            "        \"ref9\": \"ref9_value\",\n" +
            "        \"ref10\": \"ref10_value\",\n" +
            "        \"ref11\": \"ref11_value\",\n" +
            "        \"ref12\": \"ref12_value\"\n" +
            "        },\n" +
            "        \"reminderDetails\": null,\n" +
            "        \"subscriptionDetails\": null,\n" +
            "        \"bindLinkIdMobile\": null\n" +
            "    }\n" +
            "}";



    public CreateNewLink() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public CreateNewLink(List<String> enablePaymodes, List<String>disablePaymodes) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getRequest_EnableDisable());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        if(enablePaymodes.size()>0){
            for(int i=0;i<enablePaymodes.size();i++){
                setContext("body.enablePaymentMode["+i+"].mode",enablePaymodes.get(i));
            }
        }
        if(disablePaymodes.size()>0){
            for(int i=0;i<disablePaymodes.size();i++){
                setContext("body.disablePaymentMode["+i+"].mode",disablePaymodes.get(i));
            }
        }
    }

    public String getRequest() {return request;}


    public String getRequest_EnableDisable(){
        return RequestEnableDisable;
    }


    public  CreateNewLink buildRequest(String mid, String type,String amount) {
        if(type.equals("INVOICE")){
            String invoiceId = Integer.toString(CommonHelpers.getRandomWithSize(7));
            setContext("body.invoiceId",invoiceId);
            setContext("body.amount",amount);
            setContext("body.invoiceDetails[0].perUnitAmount",amount);
            setContext("body.invoiceDetails[0].perUnitTax[0].taxAmount","0.0");
        }
        if(type.equals("PAYMENT_BUTTON")){
            setContext("body.buttonDetails.buttonType","Donate");
            setContext("body.buttonDetails.buttonTitle","Donate for corona");
            setContext("body.buttonDetails.buttonStyle","Bold");
            setContext("body.buttonDetails.buttonSize","14");
            setContext("body.buttonDetails.sourceApp","https://example.test/test");
            setContext("body.customPaymentSuccessMessage","Thanks for donation");
            setContext("body.customPaymentFailureMessage","Sorry for failure");
            setContext("body.redirectionUrlSuccess","https://example.test/test");
            setContext("body.redirectionUrlFailure","https://example.test/test");
        }
        else{
            setContext("body.buttonDetails",null);
        }
        setContext("body.linkType",type);
        setContext("body.mid",mid);
        setContext("body.amount",amount);
        setContext("body.linkName","TestingLink");
        setContext("body.customerId","3454");
        setContext("body.linkDescription","123");
        return this;
    }

    public CreateNewLink buildRequest(String mid, String redirectionUrl) {
        setContext("body.mid", mid);
        setContext("body.redirectionUrlSuccess", redirectionUrl);
        setContext("body.redirectionUrlFailure", redirectionUrl);
        setContext("body.amount", "2");
        return this;
    }

    public  CreateNewLink setMFInfo(String transactionType, String validateAccountNumber, String allowUnverifiedAccount, String accountNumber) {
        setContext("body.transactionType",transactionType);
        setContext("body.validateAccountNumber", validateAccountNumber);
        setContext("body.allowUnverifiedAccount",allowUnverifiedAccount);
        setContext("body.accountNumber",accountNumber);
        setContext("body.additionalMFInfo.ref1","def");
        setContext("body.additionalMFInfo.ref2","qwe");
        setContext("body.additionalMFInfo.ref3","asdf");
        setContext("body.additionalMFInfo.ref4","wed");
        return this;
    }

    String EMI_REQUEST = new StringBuilder().append("{\"head\": {\"timestamp\": \"1539601338741\",\"clientId\": \"UMP\",\"version\": \"v2\",\"channelId\": \"\",\"tokenType\": \"JWT\",\"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJVTVAiLCJtaWQiOiJ0ZXN0bGkyNDI5NjcxNzc1MzAyMyIsImludm9pY2VFbWFpbCI6Im5pbWl0LmJoYXRpYUBwYXl0bS5jb20iLCJzdGF0dXNDYWxsYmFja1VybCI6Imh0dHBzOi8vZXhhbXBsZS50ZXN0L3Rlc3QiLCJpc0FjdGl2ZSI6InRydWUiLCJiaW5kTGlua0lkTW9iaWxlIjoibnVsbCIsInRlbXBsYXRlSWQiOiJudWxsIiwicGFydGlhbFBheW1lbnQiOiJudWxsIiwibGlua05hbWUiOiJGSVhFRCIsImV4cGlyeURhdGUiOiIwMi8wNy8yMDIyIiwidmFsaWRhdGVBY2NvdW50TnVtYmVyIjoibnVsbCIsInNlbmRFbWFpbCI6InRydWUiLCJjdXN0b21lckNvbnRhY3QiOiJ7XCJjdXN0b21lck5hbWVcIjpcInBheXRtX2NvbnRhY3ROb1wiLFwiY3VzdG9tZXJFbWFpbFwiOlwidmFuc2hpa2EuY2hhdWhhbkBwYXl0bS5jb21cIixcImN1c3RvbWVyTW9iaWxlXCI6XCI4NTEyMDA1MzQ5XCJ9IiwiYWxsb3dVbnZlcmlmaWVkQWNjb3VudCI6Im51bGwiLCJpbnZvaWNlUGhvbmVObyI6Ijg1MTIwMDUzNDkiLCJsaW5rRGVzY3JpcHRpb24iOiJsaW5rcGF5bWVudCIsInJlZGlyZWN0aW9uVXJsU3VjY2VzcyI6Im51bGwiLCJwYXJlbnRNaWQiOiJudWxsIiwicmVtaW5kZXJEZXRhaWxzIjoibnVsbCIsImN1c3RvbWVySWQiOiJudWxsIiwicmVkaXJlY3Rpb25VcmxGYWlsdXJlIjoibnVsbCIsImludm9pY2VEZXRhaWxzIjoiW3tcInByb2R1Y3ROYW1lXCI6XCJsYXB0b3BcIixcInByb2R1Y3RDb2RlXCI6XCJQMTAxXCIsXCJub09mVW5pdHNcIjpcIjFcIixcInBlclVuaXRBbW91bnRcIjoxLFwicGVyVW5pdFRheFwiOlt7XCJ0YXhOYW1lXCI6XCJTR1NUXCIsXCJ0YXhBbW91bnRcIjpcIjEuMFwifV19XSIsImN1c3RvbVBheW1lbnRGYWlsdXJlTWVzc2FnZSI6Im51bGwiLCJtZXJjaGFudFJlcXVlc3RJZCI6IlVVSUQiLCJjdXN0b21QYXltZW50U3VjY2Vzc01lc3NhZ2UiOiJudWxsIiwiYW1vdW50IjoiMiIsIm1heFBheW1lbnRzQWxsb3dlZCI6Im51bGwiLCJhY2NvdW50TnVtYmVyIjoibnVsbCIsImJ1dHRvbkRldGFpbHMiOiJudWxsIiwidHJhbnNhY3Rpb25UeXBlIjoibnVsbCIsInNpbXBsaWZpZWRTdWJ2ZW50aW9uIjoibnVsbCIsImFkZGl0aW9uYWxNRkluZm8iOiJudWxsIiwic3Vic2NyaXB0aW9uRGV0YWlscyI6Im51bGwiLCJzZW5kU21zIjoidHJ1ZSIsImxpbmtUeXBlIjoiRklYRUQiLCJpbnZvaWNlSWQiOiIxMjM0NTExMyJ9.gRwi6W64xL_38bPihatbHqWC866xqF9isdcDXM5F6i4\"},\"body\": {\"buttonDetails\": null,\"redirectionUrlSuccess\": null,\"redirectionUrlFailure\": null,\"customPaymentSuccessMessage\": null,\"customPaymentFailureMessage\": null,\"merchantRequestId\": \"UUID\",\"partialPayment\": null,\"mid\": \"testli24296717753023\",\"parentMid\": null,\"linkName\": \"FIXED\",\"linkDescription\": \"linkpayment\",\"linkType\": \"FIXED\"," + "\"invoiceId\": \"12345113\", " + "\"expiryDate\": "+getExpiryDate()+"," + " \"amount\": \"2000\", \"isActive\": \"true\", \"sendSms\": \"true\", \"sendEmail\": \"true\",\"customerContact\": {\"customerName\": \"paytm_contactNo\",\"customerEmail\": \"vanshika.chauhan@paytm.com\", \"customerMobile\": \"8512005349\"},\"statusCallbackUrl\": \"https://example.test/test\",\"invoicePhoneNo\": \"8512005349\",\"invoiceEmail\": \"nimit.bhatia@paytm.com\",\"invoiceDetails\": [{\"productName\": \"laptop\",\"productCode\": \"P101\",\"noOfUnits\": \"1\",\"perUnitAmount\": 1,\"perUnitTax\": [ {\"taxName\": \"SGST\",\"taxAmount\": \"1.0\" }]}],\"reminderDetails\": null, \"maxPaymentsAllowed\": null,\"validateAccountNumber\": null, \"allowUnverifiedAccount\": null, \"accountNumber\": null,\"additionalMFInfo\": null, \"transactionType\": null,\"customerId\": null,\"simplifiedSubvention\": {\"subventionAmount\":\"2000\",\"selectPlanOnCashierPage\": true,\"items\": [ {\"verticalId\": \"51\",\"isEmiEnabled\": true,\"isPhysical\": true,\"quantity\": 1,\"productId\": \"321067334\",\"price\": 2000,\"brandId\": \"saumsung1\",\"categoryList\": [\"34634883\"],\"model\": \"m12\", \"merchantId\": \"1152435\",\"id\": \"1\"}]},\"subscriptionDetails\": null,\"bindLinkIdMobile\": null,\"templateId\": null}}\n").toString();
    public CreateNewLink(String subventionAmount,String customerId,String selectPlanOnCashierPage, List<String> items) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getRequest_EMI());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        List<String> categoryList=new ArrayList<>();
        setContext("body.simplifiedSubvention.selectPlanOnCashierPage",selectPlanOnCashierPage);
        setContext("body.simplifiedSubvention.subventionAmount",subventionAmount);
        setContext("body.simplifiedSubvention.customerId",customerId);
        if(!items.isEmpty()) {
            if(items.get(7)!="")
                categoryList.add(items.get(7));
            setContext("body.simplifiedSubvention.items[0].verticalId", items.get(0));
            setContext("body.simplifiedSubvention.items[0].isEmiEnabled", items.get(1));
            setContext("body.simplifiedSubvention.items[0].isPhysical", items.get(2));
            setContext("body.simplifiedSubvention.items[0].quantity", items.get(3));
            setContext("body.simplifiedSubvention.items[0].productId", items.get(4));
            setContext("body.simplifiedSubvention.items[0].price", items.get(5));
            setContext("body.simplifiedSubvention.items[0].brandId", items.get(6));
            setContext("body.simplifiedSubvention.items[0].categoryList", categoryList);
            setContext("body.simplifiedSubvention.items[0].model", items.get(8));
            setContext("body.simplifiedSubvention.items[0].merchantId", items.get(9));
            setContext("body.simplifiedSubvention.items[0].id", items.get(10));
        }
        else {
            setContext("body.simplifiedSubvention.items",items);
        }
    }
    public String getRequest_EMI(){
        return EMI_REQUEST;
    }

    String LinkSplitSettlement= "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhbW91bnQiOiIxMDAiLCJpc3MiOiJVTVAiLCJtYXhQYXltZW50c0FsbG93ZWQiOiIxMCIsIm1pZCI6IkRpcGVuZDAxMTA3OTA4ODI4OTAxIiwic3RhdHVzQ2FsbGJhY2tVcmwiOiJodHRwczovL3N0YWdlLXdlYmFwcC5wYXl0bS5pbi9wZW9uLnBocD9zdWJzY3JpcHRpb249MSIsImlzQWN0aXZlIjoidHJ1ZSIsImxpbmtOYW1lIjoieW9vIiwicGFydGlhbFBheW1lbnQiOiJmYWxzZSIsImV4cGlyeURhdGUiOiIxOC8wMi8yMDIxIiwicmVzZWxsZXJJZCI6IkRpcGVuZDAxMTA3OTA4ODI4OTAxIiwic2VuZEVtYWlsIjoidHJ1ZSIsInNlbmRTbXMiOiJ0cnVlIiwiY3VzdG9tZXJDb250YWN0Ijoie1wiY3VzdG9tZXJOYW1lXCI6XCJuaWRoaVwiLFwiY3VzdG9tZXJFbWFpbFwiOlwibmlkaGkua2FscmFAcGF5dG0uY29tXCIsXCJjdXN0b21lck1vYmlsZVwiOlwiODQ0Nzg4NTA4OFwifSIsImludm9pY2VQaG9uZU5vIjoiOTk5OTk5OTk5OSIsImxpbmtEZXNjcmlwdGlvbiI6InBhcnR5IiwibGlua1R5cGUiOiJGSVhFRCIsImludm9pY2VJZCI6IjI1OTgiLCJpbnZvaWNlRGV0YWlscyI6Ilt7XCJwcm9kdWN0TmFtZVwiOlwibGFQXCIsXCJwcm9kdWN0Q29kZVwiOlwiUDEwMVwiLFwibm9PZlVuaXRzXCI6XCIxXCIsXCJwZXJVbml0QW1vdW50XCI6OTAsXCJwZXJVbml0VGF4XCI6W3tcInRheE5hbWVcIjpcIlNHU1RcIixcInRheEFtb3VudFwiOlwiMTAuMFwifV19XSIsIm1lcmNoYW50UmVxdWVzdElkIjoia2FybSJ9.W2MSOpG1LQVb-oqPgbjDGuYbO5yNA9MxWWNABJgO26c\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"merchantRequestId\": \"karm\",\n" +
            "        \"mid\": \"Splits50781612685970\",\n" +
            "        \"linkName\": \"Abhishek\",\n" +
            "        \"linkDescription\": \"party\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"invoiceId\": \"5090\",\n" +
            "        \"amount\": \"10\",\n" +
            "        \"expiryDate\": \"18/12/2027\",\n" +
            "        \"isActive\": \"true\",\n" +
            "        \"sendSms\": \"true\",\n" +
            "        \"sendEmail\": \"true\",\n" +
            "        \"customerContact\": {\n" +
            "            \"customerName\": \"abhishek\",\n" +
            "            \"customerEmail\": \"abhishek7.gupta@paytm.com\",\n" +
            "            \"customerMobile\": \"7275339423\"\n" +
            "        },\n" +
            "        \"statusCallbackUrl\": \"https://stage-webapp.paytm.in/peon.php?subscription=1\",\n" +
            "        \"maxPaymentsAllowed\": \"10\",\n" +
            "        \"linkNotes\": \"Some Notes\",\n" +
            "        \"splitSettlementInfo\": {\n" +
            "            \"splitMethod\": \"AMOUNT\",\n" +
            "            \"splitInfo\": [\n" +
            "               \n" +
            "                {\n" +
            "                    \"mid\": \"216820000008158453470\",\n" +
            "                    \"amount\": {\n" +
            "                        \"value\": \"5.00\",\n" +
            "                        \"currency\": \"INR\"\n" +
            "                    },\n" +
            "                    \"extendInfo\": \"{\\\"ref1\\\": \\\"ref1\\\",\\\"ref2\\\": \\\"ref2\\\",\\\"ref3\\\": \\\"ref3\\\",\\\"ref4\\\": \\\"ref4\\\"}\"\n" +
            "                },\n" +
            "                 {\n" +
            "                    \"mid\": \"216820000008158450440\",\n" +
            "                    \"amount\": {\n" +
            "                        \"value\": \"2.00\",\n" +
            "                        \"currency\": \"INR\"\n" +
            "                    },\n" +
            "                    \"extendInfo\": \"{\\\"ref1\\\": \\\"ref1\\\",\\\"ref2\\\": \\\"ref2\\\",\\\"ref3\\\": \\\"ref3\\\",\\\"ref4\\\": \\\"ref4\\\"}\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";

    public CreateNewLink(String Agg_mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getLinkSplitSettlement());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setContext("body.mid", Agg_mid);
    }

    public String getLinkSplitSettlement(){
        return LinkSplitSettlement;
    }

    public  CreateNewLink buildRequest(String mid, String type,String amount, String OperationOrigin) {
        getRequestSpecBuilder().addQueryParam("OPERATION_ORIGIN", OperationOrigin);
        setContext("body.linkType",type);
        setContext("body.mid",mid);
        setContext("body.amount",amount);
        setContext("body.linkName","Anushka");
        setContext("body.customerId","3454");
        setContext("body.linkDescription","Safety Tips");
        return this;
    }

    String EdcLink="{\n" +
            "    \"head\" : {\n" +
            "    \"version\" : \"2.2.6\",\n" +
            "    \"osVersion\" : null,\n" +
            "    \"firmwareVersion\" : null,\n" +
            "    \"clientId\" : \"PAXA9023453\",\n" +
            "    \"mac\" : \"81E396A8F2F26068\",\n" +
            "    \"macKsn\" : \"FFEE0101011D124000CE\",\n" +
            "    \"isP2PEDevice\" : true\n" +
            "   },\n" +
            "  \"body\" : {\n" +
            "    \"merchantRequestId\": \"karm123987\",\n" +
            "    \"mid\" : \"qa12ma76210339078579\",\n" +
            "    \"tid\" : \"14010067\",\n" +
            "    \"time\" : \"164000\",\n" +
            "    \"date\" : \"0530\",\n" +
            "    \"year\" : \"2023\",\n" +
            "    \"linkName\":\"edcaaaaaaaaaa\",\n" +
            "    \"linkType\": \"FIXED\",\n" +
            "    \"amount\" : \"170559\",\n" +
            "    \"customerContact\" : {\n" +
            "      \"customerMobile\" : \"7070584769\"\n" +
            "    },\n" +
            "    \"sendSms\" : false,\n" +
            "    \"extendInfo\" : {\n" +
            "          \"tipAmount\":\"10000\"\n" +
            "     },\n" +
            "    \"linkDescription\" : \"tesy\",\n" +
            "    \"txnType\" : \"LINK\",\n" +
            "    \"edcEmiFields\" : {\n" +
            "      \"isNewEmiFlow\" : \"true\",\n" +
            "      \"terminalType\" : \"SYSTEM\",\n" +
            "      \"productCode\" : \"51051000100000000046\",\n" +
            "      \"bankCode\" : \"HDFC\",\n" +
            "      \"bankName\" : \"HDFC\",\n" +
            "       \"paymentRequesterDetails\": {\"name\" : \"Himanshu Arora\",\"mobileNumber\": \"9988776655\"},\n"+
            "      \"bankImgUrl\" : \"https://staticgw4.paytm.in/native/bank/HDFC.png\",\n" +
            "      \"cardType\" : \"CREDIT_CARD\",\n" +
            "      \"brandId\" : \"18260\",\n" +
            "      \"brandName\" : \"Bosch\",\n" +
            "      \"categoryId\" : \"208541\",\n" +
            "      \"categoryName\" : \"Washing Machine & Dryer\",\n" +
            "      \"productId\" : \"330839140\",\n" +
            "      \"productName\" : \"Bosch Test 5\",\n" +
            "      \"productAmount\" : \"300000\",\n" +
            "      \"loanAmount\" : \"2848.46\",\n" +
            "      \"model\" : \"Bosch Test 5\",\n" +
            "      \"verticalId\" : \"178\",\n" +
            "      \"isEmiEnabled\" : \"1\",\n" +
            "      \"ean\" : \"NA\",\n" +
            "      \"quantity\" : \"1\",\n" +
            "      \"validationMode\" : \"0\",\n" +
            "      \"validationKey\" : \"Serial No./ IMEI\",\n" +
            "      \"validationValue\" : \"asf\",\n" +
            "      \"skuCode\" : \"Bosch Test 5\",\n" +
            "      \"kybId\" : \"Asljdklk\" ,\n" +
            "      \"bankInvoiceNumber\" : \"2222222\", \n" +
            "      \"brandInvoiceNumber\" : \"asd\",\n" +
            "      \"validationSkipFlag\" : false,\n" +
            "      \"couponCode\" : \"HIMANSHU\", \n" +
            "      \"ftiSupportedPlan\" : true,\n" +
            "      \"ftiPercentage\" : \"75.0\",\n" +
            "      \"emiChannelDetail\" : {\n" +
            "        \"planId\" : \"23034565\",\n" +
            "        \"pgPlanId\" : \"HDFC|3\",\n" +
            "        \"interestRate\" : \"15.0\",\n" +
            "        \"emiMonths\" : \"3\",\n" +
            "        \"emiAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"97332\"\n" +
            "        },\n" +
            "        \"totalAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"291997\"\n" +
            "        },\n" +
            "        \"interestAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"7151\"\n" +
            "        },\n" +
            "        \"effectiveAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"7151\"\n" +
            "        },\n" +
            "        \"deferredDetails\" : {\n" +
            "            \"type\" : \"PERCENTAGE\",\n" +
            "            \"value\" : \"25.0\",\n" +
            "            \"amount\" : \"1900\"\n" +
            "        },\n" +
            "        \"offerDetails\" : [ {\n" +
            "          \"amount\" : {\n" +
            "            \"currency\" : \"INR\",\n" +
            "            \"value\" : \"715456\"\n" +
            "          },\n" +
            "          \"offerId\" : \"94368\",\n" +
            "          \"type\" : \"discount\",\n" +
            "          \"offerContributorType\" :\"BRAND\" \n" +
            "        } ],\n" +
            "        \"message\" : \"\",\n" +
            "        \"bankOfferDetails\" : [ {\n" +
            "          \"amount\" : {\n" +
            "            \"currency\" : \"INR\",\n" +
            "            \"value\" : \"8000\"\n" +
            "          },\n" +
            "          \"offerId\" : \"2131321\",\n" +
            "          \"type\" : \"discount\",\n" +
            "          \"offerContributorType\" :\"BRAND\" \n" +
            "        },\n" +
            "        {\n" +
            "          \"amount\" : {\n" +
            "            \"currency\" : \"INR\",\n" +
            "            \"value\" : \"8000\"\n" +
            "          },\n" +
            "          \"offerId\" : \"2131321\",\n" +
            "          \"type\" : \"discount\",\n" +
            "          \"offerContributorType\" :\"BRAND\" \n" +
            "        } ],\n" +
            "        \"isBankManagedDiscount\" : false\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public CreateNewLink(String mid,String couponCode) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getRequest_EdcLink());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest_EdcLink() {return EdcLink;}

    public  CreateNewLink buildRequest(String mid, String type,String amount,String linkDescription, String planId) {
        if(type.equals("SUBSCRIPTION_LINK"))
        {
            setContext("body.mid", mid);
            setContext("body.linkType", type);
            setContext("body.linkDescription", linkDescription);
            setContext("body.linkName", "Himanshu");
            setContext("body.subscriptionDetails.subscriptionExpiry",getSubsExpiryDate());
            setContext("body.subscriptionDetails.txnAmount", amount);
            setContext("body.subscriptionDetails.planId",planId);
            setContext("body.subscriptionDetails.renewalAmount","10");
            return this;


        }
        else {
            setContext("body.mid", mid);
            setContext("body.linkType", type);
            setContext("body.amount", amount);
            setContext("body.linkDescription", linkDescription);
            setContext("body.linkName", "Himanshu");
            return this;
        }
    }

    String BrandEmiLinkRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"merchantRequestId\": \"14005046\",\n" +
            "        \"mid\": \"AutoSK61033004738699\",\n" +
            "        \"linkName\": \"DCxgHA9799679139614314005046\",\n" +
            "        \"linkDescription\": \"NEW CREATE\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"amount\": \"4600\",\n" +
            "        \"expiryDate\": \"14/12/2023 15:08:45\",\n" +
            "        \"sendSms\": true,\n" +
            "        \"sendEmail\": true,\n" +
            "        \"customerContact\": {\n" +
            "            \"customerMobile\": \"9993498777\",\n" +
            "            \"customerEmail\": \"abhishek13.gupta@paytm.com\"\n" +
            "        },\n" +
            "        \"maxPaymentsAllowed\": 10,\n" +
            "        \"edcEmiFields\": {\n" +
            "            \"bankCode\": \"AXIS\",\n" +
            "            \"bankName\": \"AXIS\",\n" +
            "            \"bankImgUrl\": \"https://staticgw1.paytm.in/native/bank/AXIS.png\",\n" +
            "            \"cardType\": \"CREDIT_CARD\",\n" +
            "            \"brandId\": \"18084\",\n" +
            "            \"brandName\": \"AXIS\",\n" +
            "            \"categoryId\": \"66781\",\n" +
            "            \"categoryName\": \"Smart Phones\",\n" +
            "            \"productId\": \"1234585968\",\n" +
            "            \"productAmount\": \"5000.0\",\n" +
            "            \"productName\": \"New_test_product(1)\",\n" +
            "            \"model\": \"Asus1\",\n" +
            "            \"verticalId\": \"18\",\n" +
            "            \"isEmiEnabled\": \"1\",\n" +
            "            \"quantity\": \"1\",\n" +
            "            \"validationMode\": \"2\",\n" +
            "            \"validationKey\": \"Serial No./ IMEI\",\n" +
            "            \"validationValue\": \"64789\",\n" +
            "            \"skuCode\": \"Asus Test 3\",\n" +
            "            \"kybId\": \"123456\",\n" +
            "            \"brandInvoiceNumber\": \"706048\",\n" +
            "            \"validationSkipFlag\": true,\n" +
            "            \"emiChannelDetail\": {\n" +
            "                \"pgPlanId\": \"AXIS|3\",\n" +
            "                \"planId\": \"302084593581427712\",\n" +
            "                \"interestRate\": \"5.0\",\n" +
            "                \"emiMonths\": \"3\",\n" +
            "                \"emiAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"1546.13\"\n" +
            "                },\n" +
            "                \"totalAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"4638.39\"\n" +
            "                },\n" +
            "                \"interestAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"38.39\"\n" +
            "                },\n" +
            "                \"message\": \"\",\n" +
            "                \"offerDetails\": [\n" +
            "                    {\n" +
            "                        \"offerId\": \"1040020\",\n" +
            "                        \"type\": \"DISCOUNT\",\n" +
            "                        \"amount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"400.0\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"bankOfferDetails\": [\n" +
            "                    {\n" +
            "                        \"offerId\": \"\",\n" +
            "                        \"type\": \"cashback\",\n" +
            "                        \"amount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"100.0\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        },\n" +
            "        \"edcLink\": true\n" +
            "    }\n" +
            "}";

    public CreateNewLink(Boolean isBrandEmi) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getBrandEmiLinkRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getBrandEmiLinkRequest() {return BrandEmiLinkRequest;}

    String BankEmiLinkRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"merchantRequestId\": \"14005046\",\n" +
            "        \"mid\": \"AutoSK61033004738699\",\n" +
            "        \"linkName\": \"DCxgHA9799679139614314005046\",\n" +
            "        \"linkDescription\": \"NEW CREATE\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"amount\": \"4600\",\n" +
            "        \"expiryDate\": \"14/12/2029 15:08:45\",\n" +
            "        \"sendSms\": true,\n" +
            "        \"sendEmail\": true,\n" +
            "        \"customerContact\": {\n" +
            "            \"customerMobile\": \"9993498777\",\n" +
            "            \"customerEmail\": \"abhishek13.gupta@paytm.com\"\n" +
            "        },\n" +
            "        \"maxPaymentsAllowed\": 10,\n" +
            "        \"edcEmiFields\": {\n" +
            "            \"bankCode\": \"HDFC\",\n" +
            "            \"bankName\": \"HDFC BANK\",\n" +
            "            \"bankImgUrl\": \"https://staticgw1.paytmpayments.com/native/bank/HDFC.png\",\n" +
            "            \"cardType\": \"CREDIT_CARD\",\n" +
            "            \"productAmount\": \"5000.0\",\n" +
            "            \"productCode\": \"51051000100000000046\",\n" +
            "            \"quantity\": \"1\",\n" +
            "            \"kybId\": \"123456\",\n" +
            "            \"brandInvoiceNumber\": \"706048\",\n" +
            "            \"validationSkipFlag\": true,\n" +
            "            \"emiChannelDetail\": {\n" +
            "                \"pgPlanId\": \"HDFC|3\",\n" +
            "                \"interestRate\": \"5.0\",\n" +
            "                \"emiMonths\": \"3\",\n" +
            "                \"emiAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"1680.57\"\n" +
            "                },\n" +
            "                \"totalAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"5041.71\"\n" +
            "                },\n" +
            "                \"interestAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"41.71\"\n" +
            "                },\n" +
            "                \"message\": \"\",\n" +
            "                \"offerDetails\": [\n" +
            "                    {\n" +
            "                        \"offerId\": \"1950265187\",\n" +
            "                        \"type\": \"cashback\",\n" +
            "                        \"amount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"500.0\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                ],\n" +

            "            }\n" +
            "        },\n" +
            "        \"edcLink\": true\n" +
            "    }\n" +
            "}";

            String BankEmiLinkRequestNew = "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"merchantRequestId\": \"97001036\",\n" +
            "        \"mid\": \"qa12re33456769079011\",\n" +
            "        \"linkName\": \"DCxgHA9799679139614314005046\",\n" +
            "        \"linkDescription\": \"test\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"amount\": \"26872.65\",\n" +
            "        \"expiryDate\": \"14/12/2029 15:08:45\",\n" +
            "        \"sendSms\": true,\n" +
            "        \"sendEmail\": true,\n" +
            "        \"customerContact\": {\n" +
            "            \"customerMobile\": \"9895958788\",\n" +
            "            \"customerEmail\": \"abhishek13.gupta@paytm.com\"\n" +
            "        },\n" +
            "        \"maxPaymentsAllowed\": 10,\n" +
            "        \"edcEmiFields\": {\n" +
            "            \"bankCode\": \"HDFC\",\n" +
            "            \"bankName\": \"HDFC Bank\",\n" +
            "            \"bankImgUrl\": \"https://staticgw1.paytmpayments.com/native/bank/HDFC.png\",\n" +
            "            \"cardType\": \"CREDIT_CARD\",\n" +
            "            \"bankInvoiceNumber\": \"3345678(455556\",\n" +
            "            \"validationSkipFlag\": true,\n" +
            "            \"emiChannelDetail\": {\n" +
            "                \"bankOfferDetails\": [\n" +
            "                    {\n" +
            "                        \"amount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"1134.03\"\n" +
            "                        },\n" +
            "                        \"offerContributorType\": \"MERCHANT\",\n" +
            "                        \"offerId\": \"2478081\",\n" +
            "                        \"type\": \"discount\"\n" +
            "                    },\n" +
            "                ],\n" +
            "                \"effectiveAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"25824.45\"\n" +
            "                },\n" +
            "                \"emiAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"8608.15\"\n" +
            "                },\n" +
            "                \"emiMonths\": \"3\",\n" +
            "                \"interestAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"85.83\"\n" +
            "                },\n" +
            "                \"interestRate\": \"2.0\",\n" +
            "                \"isBankManagedDiscount\": false,\n" +
            "                \"message\": \"\",\n" +
            "                \"pgPlanId\": \"HDFC|3\",\n" +
            "                \"planId\": \"306054581678728192\",\n" +
            "                \"totalAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"25824.45\"\n" +
            "                }\n" +
            "            },\n" +
            "            \"productAmount\": \"26872.65\",\n" +
            "            \"productCode\": \"51051000100000000046\",\n" +
            "            \"quantity\": \"1\",\n" +
            "            \"kybId\": \"123456\",\n" +
            "            \"loanAmount\": \"25738.62\",\n" +
            "            \"ftiSupportedPlan\": \"false\",\n" +
            "            \"isNewEmiFlow\": \"true\",\n" +
            "        },\n" +
            "        \"edcLink\": true\n" +
            "    }\n" +
            "}";

    public CreateNewLink(Boolean isBankEmi,Boolean isBrandEmi) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getBankEmiLinkRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getBankEmiLinkRequest() {return BankEmiLinkRequestNew;}

    String udfFixedLink= "{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJhbW91bnQiOiIyMy4wMCIsImlzcyI6IlVNUCIsIm1heFBheW1lbnRzQWxsb3dlZCI6IjEwIiwibWlkIjoiUlVQQVlCNTg1Mzk4MzE3MDkxMDMiLCJzdGF0dXNDYWxsYmFja1VybCI6Imh0dHBzOi8vc3RhZ2Utd2ViYXBwLnBheXRtLmluL3Blb24ucGhwP3N1YnNjcmlwdGlvbj0xIiwiaXNBY3RpdmUiOiJ0cnVlIiwibGlua05hbWUiOiJ5b28iLCJwYXJ0aWFsUGF5bWVudCI6InRydWUiLCJzZW5kRW1haWwiOiJ0cnVlIiwic2VuZFNtcyI6InRydWUiLCJjdXN0b21lckNvbnRhY3QiOiJ7XCJjdXN0b21lck5hbWVcIjpcIm5pZGhpXCIsXCJjdXN0b21lckVtYWlsXCI6XCJwcmFndW4uYXJvcmFAcGF5dG0uY29tXCIsXCJjdXN0b21lck1vYmlsZVwiOlwiODQ0Nzg4NTA4OFwifSIsImludm9pY2VQaG9uZU5vIjoiOTk5OTk5OTk5OSIsImxpbmtEZXNjcmlwdGlvbiI6InBhcnR5IiwicmVtaW5kZXJEZXRhaWxzIjoie1wiZGF5c0FmdGVySXNzdWVEYXRlXCI6WzFdLFwiZGF5c0JlZm9yZUV4cGlyeVwiOlsxXSxcImNoYW5uZWxzXCI6W1wiU01TXCIsXCJQVVNIXCIsXCJFTUFJTFwiXX0iLCJsaW5rVHlwZSI6IkZJWEVEIiwiaW52b2ljZUlkIjoiMzAwMyIsImludm9pY2VEZXRhaWxzIjoiW3tcInByb2R1Y3ROYW1lXCI6XCJsYVBcIixcInByb2R1Y3RDb2RlXCI6XCJQMTAxXCIsXCJub09mVW5pdHNcIjpcIjFcIixcInBlclVuaXRBbW91bnRcIjo5MCxcInBlclVuaXRUYXhcIjpbe1widGF4TmFtZVwiOlwiU0dTVFwiLFwidGF4QW1vdW50XCI6XCIxMC4wXCJ9XX1dIiwibWVyY2hhbnRSZXF1ZXN0SWQiOiJrYXJtIn0.QcnYcWR6Z2ubDV6dKyKLdsnRFnP5vDMuIoQrmZKsNxo\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"merchantRequestId\": \"avs\",\n" +
            "        \"mid\": \"qa11id51337035364092\",\n" +
            "        \"linkName\": \"hello\",\n" +
            "        \"linkDescription\": \"hello\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"invoiceId\": \"6799430444404\",\n" +
            "        \"amount\": \"1\",\n" +
            "        \"linkNotes\": \"SomeNotes\",\n" +
            "        \"expiryDate\": \"20/11/2023\",\n" +
            "        \"sendSms\": false,\n" +
            "        \"sendEmail\": false,\n" +
            "        \"invoiceDetails\": [\n" +
            "            {\n" +
            "                \"productName\": \"laP\",\n" +
            "                \"productCode\": \"P101\",\n" +
            "                \"noOfUnits\": \"1\",\n" +
            "                \"perUnitAmount\": 4,\n" +
            "                \"perUnitTax\": [\n" +
            "                    {\n" +
            "                        \"taxName\": \"SGST\",\n" +
            "                        \"taxAmount\": \"1\"\n" +
            "                    }\n" +
            "                ]\n" +
            "            }\n" +
            "        ],\n" +
            "        \"additionalInfo\": {\n" +
            "            \"udf2\":\"dfghudf3:,.90()\",\n" +
            "            \"udf3\":\"dfghudf3:,.90()\"\n" +
            "        }\n" +
            "        \n" +
            "        \n" +
            "    }\n" +
            "    \n" +
            "}";

    public CreateNewLink(String linkType, String udf1,String udf2,String udf3) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getUdfLink());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getUdfLink() {return udfFixedLink;}

    private String mid;
    String EdcEMIPaymentLink="{\n" +
            "    \"head\" : {\n" +
            "    \"version\" : \"2.2.6\",\n" +
            "    \"osVersion\" : null,\n" +
            "    \"firmwareVersion\" : null,\n" +
            "    \"clientId\" : \"PAXA9023453\",\n" +
            "    \"mac\" : \"81E396A8F2F26068\",\n" +
            "    \"macKsn\" : \"FFEE0101011D124000CE\",\n" +
            "    \"isP2PEDevice\" : true\n" +
            "   },\n" +
            "  \"body\" : {\n" +
            "    \"merchantRequestId\": \"karm123987\",\n" +
            "    \"mid\" : "+mid+",\n" +
            "    \"tid\" : \"14010067\",\n" +
            "    \"time\" : \"164000\",\n" +
            "    \"date\" : \"0530\",\n" +
            "    \"year\" : \"2023\",\n" +
            "    \"linkName\":\"AKEMI20351713655403097001036\",\n" +
            "    \"linkType\": \"FIXED\",\n" +
            "    \"amount\" : \"170559\",\n" +
            "    \"customerContact\" : {\n" +
            "      \"customerMobile\" : \"7070584769\"\n" +
            "    },\n" +
            "    \"sendSms\" : false,\n" +
            "    \"extendInfo\" : {\n" +
            "          \"tipAmount\":\"10000\"\n" +
            "     },\n" +
            "    \"linkDescription\" : \"tesy\",\n" +
            "    \"txnType\" : \"LINK\",\n" +
            "    \"edcEmiFields\" : {\n" +
            "      \"isNewEmiFlow\" : \"true\",\n" +
            "      \"terminalType\" : \"SYSTEM\",\n" +
            "      \"productCode\" : \"51051000100000000046\",\n" +
            "      \"bankCode\" : \"HDFC\",\n" +
            "      \"bankName\" : \"HDFC Bank\",\n" +
            "      \"bankImgUrl\" : \"https://staticgw4.paytm.in/native/bank/HDFC.png\",\n" +
            "      \"cardType\" : \"DEBIT_CARD\",\n" +
            "      \"productAmount\" : \"300000\",\n" +
            "      \"loanAmount\" : \"2848.46\",\n" +
            "      \"emiChannelDetail\" : {\n" +
            "        \"planId\" : \"23034565\",\n" +
            "        \"pgPlanId\" : \"HDFC|3\",\n" +
            "        \"interestRate\" : \"15.0\",\n" +
            "        \"emiMonths\" : \"3\",\n" +
            "        \"emiAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"97332\"\n" +
            "        },\n" +
            "        \"totalAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"291997\"\n" +
            "        },\n" +
            "        \"interestAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"7151\"\n" +
            "        },\n" +
            "        \"effectiveAmount\" : {\n" +
            "          \"currency\" : \"INR\",\n" +
            "          \"value\" : \"7151\"\n" +
            "        },\n" +
            "        \"deferredDetails\" : {\n" +
            "            \"type\" : \"PERCENTAGE\",\n" +
            "            \"value\" : \"25.0\",\n" +
            "            \"amount\" : \"1900\"\n" +
            "        },\n" +
            "        \"offerDetails\" : [ {\n" +
            "          \"amount\" : {\n" +
            "            \"currency\" : \"INR\",\n" +
            "            \"value\" : \"715456\"\n" +
            "          },\n" +
            "          \"offerId\" : \"94368\",\n" +
            "          \"type\" : \"discount\",\n" +
            "          \"offerContributorType\" :\"BRAND\" \n" +
            "        } ],\n" +
            "        \"message\" : \"\",\n" +
            "        \"bankOfferDetails\" : [ {\n" +
            "          \"amount\" : {\n" +
            "            \"currency\" : \"INR\",\n" +
            "            \"value\" : \"8000\"\n" +
            "          },\n" +
            "          \"offerId\" : \"2131321\",\n" +
            "          \"type\" : \"discount\",\n" +
            "          \"offerContributorType\" :\"BRAND\" \n" +
            "        },\n" +
            "        {\n" +
            "          \"amount\" : {\n" +
            "            \"currency\" : \"INR\",\n" +
            "            \"value\" : \"8000\"\n" +
            "          },\n" +
            "          \"offerId\" : \"2131321\",\n" +
            "          \"type\" : \"discount\",\n" +
            "          \"offerContributorType\" :\"BRAND\" \n" +
            "        } ],\n" +
            "        \"isBankManagedDiscount\" : false\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";

    public String getEdcEMIPaymentLink(){ return EdcEMIPaymentLink; }


    public CreateNewLink(String mid,String couponCode,String paymode) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(getEdcEMIPaymentLink());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setContext("body.mid",mid);
        setContext("body.edcEmiFields.cardType",paymode);

    }

    String brandEmiLinkRequest="{\n" +
            "    \"head\": {\n" +
            "        \"timestamp\": \"1539601338741\",\n" +
            "        \"clientId\": \"UMP\",\n" +
            "        \"version\": \"v2\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"signature\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"merchantRequestId\": \"97001036\",\n" +
            "        \"mid\": \"qa12re33456769079011\",\n" +
            "        \"linkName\": \"TestingLink\",\n" +
            "        \"linkDescription\": \"123\",\n" +
            "        \"linkType\": \"FIXED\",\n" +
            "        \"amount\": \"13800.0\",\n" +
            "        \"expiryDate\": \"14/12/2029 15:08:45\",\n" +
            "        \"sendSms\": true,\n" +
            "        \"sendEmail\": true,\n" +
            "        \"customerContact\": {\n" +
            "            \"customerMobile\": \"9993498777\",\n" +
            "            \"customerEmail\": \"abhishek13.gupta@paytm.com\"\n" +
            "        },\n" +
            "        \"maxPaymentsAllowed\": 10,\n" +
            "        \"edcEmiFields\": {\n" +
            "            \"bankCode\": \"HDFC\",\n" +
            "            \"bankImgUrl\": \"https://staticgw3.paytmpayments.com/native/bank/HDFC.png\",\n" +
            "            \"bankName\": \"HDFC Bank\",\n" +
            "            \"brandId\": \"1707\",\n" +
            "            \"brandInvoiceNumber\": \"568688899999666\",\n" +
            "            \"brandName\": \"Apple\",\n" +
            "            \"cardType\": \"CREDIT_CARD\",\n" +
            "            \"categoryId\": \"66781\",\n" +
            "            \"categoryName\": \"Smart Phones\",\n" +
            "            \"couponCode\": \"\",\n" +
            "            \"ean\": \"978014102662\",\n" +
            "            \"emiChannelDetail\": {\n" +
            "                \"bankOfferDetails\": [\n" +
            "                    {\n" +
            "                        \"amount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"1200.0\"\n" +
            "                        },\n" +
            "                        \"offerContributorType\": \"BRAND\",\n" +
            "                        \"offerId\": \"2478091\",\n" +
            "                        \"type\": \"discount\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"amount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"448.50\"\n" +
            "                        },\n" +
            "                        \"offerContributorType\": \"MERCHANT\",\n" +
            "                        \"offerId\": \"2478089\",\n" +
            "                        \"type\": \"cashback\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"effectiveAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"13766.73\"\n" +
            "                },\n" +
            "                \"emiAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"1579.47\"\n" +
            "                },\n" +
            "                \"emiMonths\": \"9\",\n" +
            "                \"interestAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"849.24\"\n" +
            "                },\n" +
            "                \"interestRate\": \"15.0\",\n" +
            "                \"isBankManagedDiscount\": false,\n" +
            "                \"message\": \"\",\n" +
            "                \"offerDetails\": [\n" +
            "                    {\n" +
            "                        \"amount\": {\n" +
            "                            \"currency\": \"INR\",\n" +
            "                            \"value\": \"434.01\"\n" +
            "                        },\n" +
            "                        \"offerContributorType\": \"BRAND\",\n" +
            "                        \"offerId\": \"2437198\",\n" +
            "                        \"type\": \"DISCOUNT\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"pgPlanId\": \"HDFC|9\",\n" +
            "                \"planId\": \"307312565796316171\",\n" +
            "                \"totalAmount\": {\n" +
            "                    \"currency\": \"INR\",\n" +
            "                    \"value\": \"14215.23\"\n" +
            "                }\n" +
            "            },\n" +
            "            \"ftiSupportedPlan\": false,\n" +
            "            \"isEmiEnabled\": \"1\",\n" +
            "            \"isNewEmiFlow\": true,\n" +
            "            \"loanAmount\": \"13365.99\",\n" +
            "            \"model\": \"g531gt-bq002t_94\",\n" +
            "            \"productAmount\": \"15000.0\",\n" +
            "            \"productId\": \"1234586284\",\n" +
            "            \"productName\": \"Test Product Apple_94\",\n" +
            "            \"quantity\": \"1\",\n" +
            "            \"skuCode\": \"g531gt-bq002t_94\",\n" +
            "            \"validationKey\": \"Serial No./ IMEI\",\n" +
            "            \"productCode\":\"51051000100000000046\",\n" +
            "            \"discoverability\": \"offline\",\n" +
            "            \"validationMode\": \"0\",\n" +
            "            \"validationSkipFlag\": false,\n" +
            "             \"kybId\":\"123456\",\n" +
            "            \"validationValue\": \"55667778888\",\n" +
            "            \"verticalId\": \"18\"\n" +
            "        },\n" +
            "        \"edcLink\": true,\n" +
            "        \"buttonDetails\": null,\n" +
            "        \"customerId\": \"3454\"\n" +
            "    }\n" +
            "}";
    public String brandEmiLinkRequest(){ return brandEmiLinkRequest; }

    public  CreateNewLink(Boolean isbrandEMI,String mid) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.CREATE_LINK);
        getRequestSpecBuilder().setBody(brandEmiLinkRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
}

