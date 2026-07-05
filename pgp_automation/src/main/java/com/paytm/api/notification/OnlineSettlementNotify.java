    package com.paytm.api.notification;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.DbQueries;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;

import java.util.List;
import java.util.Map;

    public class OnlineSettlementNotify extends BaseApi {

        String request = "{\n" +
                "  \"request\": {\n" +
                "    \"head\": {\n" +
                "      \"version\": \"1.1.4\",\n" +
                "      \"function\": \"alipayplus.settlement.settlementNotify\",\n" +
                "      \"clientId\": \"2016030715243903536806\",\n" +
                "      \"reqTime\": \"2022-01-27T16:52:03+05:30\",\n" +
                "      \"reqMsgId\": \"87e809cc-43c2-490d-87ce-0566ef9fd2b2\"\n" +
                "    },\n" +
                "    \"body\": {\n" +
                "      \"bankStatus\": \"PENDING\",\n" +
                "      \"cancelAmount\": 0.0,\n" +
                "      \"cancelCount\": 0,\n" +
                "      \"cancelMerchantCommission\": 0.0,\n" +
                "      \"cancelServiceTax\": 0.0,\n" +
                "      \"chargeBackAmount\": 0.0,\n" +
                "      \"chargeBackCount\": 0,\n" +
                "      \"commission\": 0.0,\n" +
                "      \"creationDate\": \"2022-01-18T00:14:53+05:30\",\n" +
                "      \"errorCode\": \"{failureReason}\",\n" +
                "      \"grossAmt\": 4290.0,\n" +
                "      \"ifscCode\": \"RATN0000000\",\n" +
                "      \"maskedAccountNumber\": \"409000**7762\",\n" +
                "      \"merchantId\": \"{mid}\",\n" +
                "      \"merchantSolutionType\": \"ONLINE\",\n" +
                "      \"modificationDate\": \"2022-01-18T16:52:03+05:30\",\n" +
                "      \"netAmount\": 42900.0,\n" +
                "      \"notificationStatus\": \"FAILED\",\n" +
                "      \"notifyMerchant\": true,\n" +
                "      \"preUnsettleAmount\": 0.0,\n" +
                "      \"reconId\": \"ALL20220118216820000583281918981\",\n" +
                "      \"refundWithdrawAmount\": 0.0,\n" +
                "      \"refundWithdrawCount\": 0,\n" +
                "      \"repaymentAmount\": 0.0,\n" +
                "      \"repaymentCount\": 0,\n" +
                "      \"requestId\": \"PENDING-10-20220118211213813400168981087358857\",\n" +
                "      \"serviceTax\": 0.0,\n" +
                "      \"settleAmount\": \"42900\",\n" +
                "      \"settleSchedulingStrategyCode\": \"NORMAL\",\n" +
                "      \"settleType\": \"{settleType}\",\n" +
                "      \"settlementDate\": \"20220118\",\n" +
                "      \"txnAmount\": 4290.0,\n" +
                "      \"txnCount\": 1,\n" +
                "      \"unsettleAmount\": 0.0,\n" +
                "      \"utr\": \"\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"signature\": \"7b0bc008d3a1319aa2307be602b16fb0cd7b5ccb0786fc911b60e122c53fac9f\"\n" +
                "}";
        public OnlineSettlementNotify(String mid, String failureReason,String settleType)
        {
            setMethod(MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().setAccept(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            setRequest(mid,failureReason,settleType);
            getRequestSpecBuilder().setBasePath(Constants.NotificationService.REALTIME_SETTLEMENT_NOTIFY);
            getRequestSpecBuilder().setBody(request);
        }
        private Map<String, Object> getFrom_ALIPAY_MERCHANT_ByMid(String mid) {
            String dbQuery = DbQueries.SELECT_FROM_ALIPAY_PAYTM_MERCHANT(mid);
            List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
            if (resultList.isEmpty())
                Assertions.fail("No result found in DB for Query: " + dbQuery);
            return resultList.get(0);
        }
        public void setRequest(String mid, String failureReason,String settleType)
        {
            Map<String, Object> dbResult = getFrom_ALIPAY_MERCHANT_ByMid(mid);
            String alipayId = dbResult.get("alipay_merchant_id").toString();
           // request=request.replace("{mid}",alipayId)
            request=request.replace("{mid}",mid)
                    .replace("{failureReason}",failureReason)
                    .replace("{settleType}", settleType);
        }

        public OnlineSettlementNotify(String mid, String settleType){
            setMethod(MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().setAccept(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(Constants.NotificationService.REALTIME_SETTLEMENT_NOTIFY);
            getRequestSpecBuilder().setBody(getRequest());
            getRequestSpecBuilder().addHeader("Content-Type","application/json");
            setContext("request.head.clientId", mid);
            setContext("request.body.settleType", settleType);
        }
        public String getRequest() {return request;}
    }