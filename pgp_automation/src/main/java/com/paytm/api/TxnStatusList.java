package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants.PGPAPIResourcePath;
import com.paytm.framework.api.BaseApi;
import com.paytm.pages.TxnStatusResponse;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;

import java.util.TreeMap;

import static org.awaitility.Awaitility.await;


public class TxnStatusList extends BaseApi {

    private Response response;

    public TxnStatusResponse txnStatusResponse;
    private SoftAssertions validateSoftly = new SoftAssertions();

    public Response getTxnStatusList(final String mid, final String orderId) {
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", mid);
        treemap.put("ORDERID", orderId);

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(PGPAPIResourcePath.TXN_STATUS_LIST);

        getRequestSpecBuilder().setBody(treemap);
        int timeOutInSeconds = 60;
        long startTime = System.currentTimeMillis();

        while(System.currentTimeMillis() - startTime < (long)(timeOutInSeconds * 1000)) {
            response = this.execute();
            if (response.statusCode() != 200) {
                throw new PGPException("Exception in Txn Status List Merchant API");
            }

            if (response.jsonPath().get("TXN_LIST.STATUS") != null) {
                if (!response.jsonPath().getString("TXN_LIST.STATUS").equals("PENDING")) {
                    return response;
                }

                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException var11) {
                    var11.printStackTrace();
                }
            }
        }

        return response;

    }
    public TxnStatusList executeUntilNotPending(Duration duration) {
        try {
        await().pollInterval(Duration.FIVE_SECONDS).atMost(duration).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("STATUS")).isNotEqualToIgnoringCase("PENDING"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was not to be :- PENDING  but found to be "+this.execute().jsonPath().getString("STATUS"));
        }
        return this;
    }

    public TxnStatusList executeUntilNotPending() {
        try {
        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("STATUS")).isNotEqualToIgnoringCase("PENDING"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was not to be :- PENDING  but found to be "+this.execute().jsonPath().getString("STATUS"));
        }
        return this;
    }

    public TxnStatusList validateTxnId(String expected) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.TXNID").get(0))
                .as("TXNID mismatch")
                .isEqualTo(expected);
        return this;
    }


    public TxnStatusList validateBankTxnIdNonEmpty() {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.BANKTXNID").get(0))
                .as("BANKTXNID mismatch")
                .isNotNull().isNotEqualTo("");
        return this;
    }

    public TxnStatusList validateOrderId(String orderId) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.ORDERID").get(0))
                .as("ORDERID mismatch")
                .isEqualTo(orderId);
        return this;
    }

    public TxnStatusList validateTxnAmount(String txnAmount) {

        validateSoftly.assertThat(Double.parseDouble(response.jsonPath().getList("TXN_LIST.TXNAMOUNT").get(0).toString()))
                .as("TxnAmount mismatch")
                .isEqualTo(Double.parseDouble(txnAmount));
        return this;
    }

    public TxnStatusList validateStatus(String status) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.STATUS").get(0))
                .as("Status mismatch")
                .isEqualTo(status);
        return this;
    }

    public TxnStatusList validateTXNTYPE(String txnType) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.TXNTYPE").get(0))
                .as("TXNTYPE mismatch")
                .isEqualTo(txnType);
        return this;
    }

    public TxnStatusList validateGatewayName(String gatewayName) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.GATEWAYNAME").get(0))
                .as("GATEWAYNAME mismatch")
                .isEqualTo(gatewayName);
        return this;
    }

    public TxnStatusList validateResponseCode(String respCode) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.RESPCODE").get(0))
                .as("RESPCODE mismatch")
                .isEqualTo(respCode);
        return this;
    }

    public TxnStatusList validateResponseMessage(String respMsg) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.RESPMSG").get(0))
                .as("RESPMSG mismatch")
                .isEqualTo(respMsg);
        return this;
    }

    public TxnStatusList validateBANKNAME(String bankName) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.BANKNAME").get(0))
                .as("BANKNAME mismatch")
                .isEqualTo(bankName);
        return this;
    }

    public TxnStatusList validateMID(String mid) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.MID").get(0))
                .as("MID mismatch")
                .isEqualTo(mid);
        return this;
    }

    public TxnStatusList validatePaymentMode(String paymentMode) {
        System.out.println("value is-----"+response.jsonPath().getList("TXN_LIST.PAYMENTMODE").get(0));
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.PAYMENTMODE").get(0))
                .as("PAYMENTMODE mismatch")
                .isEqualTo(paymentMode);
        return this;
    }

    public TxnStatusList validateREFUNDAMT(String refundAmt) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.REFUNDAMT").get(0))
                .as("REFUNDAMT mismatch")
                .isEqualTo(refundAmt);
        return this;
    }

    public TxnStatusList validateRefundId(String refundId) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.REFUNDID").get(0))
                .as("Refund Id mismatch")
                .isEqualTo(refundId);
        return this;
    }

    public TxnStatusList validateChildTxnId(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].TXNID").get(index))
                .as("Child Txn TxnId Mismatch")
                .isEqualTo(expected);

        return this;
    }


   public TxnStatusList validateChildPayMode(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].PAYMENTMODE").get(index))
                .as("ChidTxnList PAYMENTMODE mismatch")
                .isEqualTo(expected);
        return this;
    }


    public TxnStatusList validateChildTxnTXNAMOUNT(String expected1,String expected2) {
        validateSoftly.assertThat((response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].TXNAMOUNT").toString()))
                .as("ChidTxnList TXNAMOUNT mismatch").contains(expected1,expected2);

        return this;
    }

    public TxnStatusList validateChildTxnGATEWAYNAME(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].GATEWAYNAME").get(index))
                .as("ChidTxnList GATEWAYNAME mismatch")
                .isEqualTo(expected);
        return this;
    }

    public TxnStatusList validateChildTxnBANKTXNID(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].BANKTXNID").get(index))
                .as("ChidTxnList BANKTXNID mismatch")
                .isEqualTo(expected);
        return this;
    }

    public TxnStatusList validateChildTxnBANKNAME(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].BANKTXNID").get(index))
                .as("ChidTxnList BANKTXNID mismatch")
                .isEqualTo(expected);
        return this;
    }

    public TxnStatusList validateChildTxnSTATUS(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].STATUS").get(index))
                .as("ChidTxnList STATUS mismatch")
                .isEqualTo(expected);
        return this;
    }

    public TxnStatusList validateChildTxnCIN(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].cardIndexNo").get(index))
                .as("ChidTxnList cardIndexNo mismatch")
                .isEqualTo(expected);
        return this;
    }

    public TxnStatusList validateChildTxnMaskedNo(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("TXN_LIST.CHILDTXNLIST[0].maskedCardNo").get(index))
                .as("ChidTxnList maskedCardNo mismatch")
                .isEqualTo(expected);
        return this;
    }




    public void assertAll() {
        validateSoftly.assertAll();
    }

}
