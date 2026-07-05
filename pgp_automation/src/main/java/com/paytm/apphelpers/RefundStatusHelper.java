package com.paytm.apphelpers;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import jdk.dynalink.linker.support.Guards;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.awaitility.Awaitility.with;

/**
 * Created by ankuragarwal on 14/12/18
 */
public class RefundStatusHelper {

    private Response response;
    private SoftAssertions validateSoftly = new SoftAssertions();
    private List<Map<String, Object>> list;
    private static final String REFUND_SUCESS_MSG = "Refund Successfull";
    private static final String TXN_SUCCESS = "TXN_SUCCESS";
    private static final String TXN_FAILURE = "TXN_FAILURE";
    private static final String RESP_CODE_SUCCESS = "10";

    Set<String> parameters = new ConcurrentSkipListSet<>();

    private SoftAssertions softly = new SoftAssertions();




    /**
     * @param mid
     * @param merchantKey
     * @param refId
     * @param isSecured   if true then MASTER_REFUND_STATUS (/refund/HANDLER_INTERNAL/getMasterRefundStatus) will be executed <br>
     *                    if false then REFUND_STATUS (/refund/HANDLER_INTERNAL/REFUND_STATUS) will be executed
     */
    public RefundStatusHelper(String mid, String merchantKey, String refId, boolean isSecured) throws PGPException {
        /*
        try{
            Thread.sleep(30000);
        }
        catch (Exception e){
            throw new AssertionError("Refund Status not avaialble after wait of 10 sec");
        }       */

        with().pollInSameThread().await()
                .pollInterval(Duration.FIVE_SECONDS)
                .atMost(Duration.TWO_MINUTES)
                .untilAsserted(() -> Assertions.assertThat(PGPUtil.executeRefundStatus(LocalConfig.PGP_HOST, mid, merchantKey, refId, isSecured).jsonPath().getList("REFUND_LIST.GATEWAY")).isNotNull());
        response = PGPUtil.executeRefundStatus(LocalConfig.PGP_HOST, mid, merchantKey, refId, isSecured);
        try {
            response.jsonPath().get("REFUND_LIST.STATUS").toString();
        } catch (Exception e) {
            throw new AssertionError("Refund Status not available in response of REFUND_STATUS");
        }
    }

    @Step
    public RefundStatusHelper validateSuccessRefund() {
        validateStatus(TXN_SUCCESS, 0);
        validateRespCode(RESP_CODE_SUCCESS, 0);
        validateRESPMSG(REFUND_SUCESS_MSG, 0);
        return this;
    }


    @Step
    public RefundStatusHelper validateFailureRefund(String respCode_exp, String respMsg_exp) {
        validateStatus(TXN_FAILURE, 0);
        validateRespCode(respCode_exp, 0);
        validateRESPMSG(respMsg_exp, 0);
        return this;
    }

    @Step
    public RefundStatusHelper validateTxnId(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.TXNID").get(index).toString())
                .as("TXNID mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateBANKTXNIDIsNotNull(int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.BANKTXNID").get(index).toString())
                .as("BANKTXNID mismatch")
                .isNotNull();
        return this;
    }
    @Step
    public RefundStatusHelper validateRRNCodeIsNotNull(int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.rrnCode").get(index).toString())
                .as("rrnCode mismatch")
                .isNotNull();
        return this;
    }

    @Step
    public RefundStatusHelper validateREFUNDIDIsNotNull(int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.REFUNDID").get(index).toString())
                .as("REFUNDID mismatch")
                .isNotNull();
        return this;
    }

    @Step
    public RefundStatusHelper validateOrderId(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.ORDERID").get(index).toString())
                .as("ORDER_ID mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateTxnAmount(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.TXNAMOUNT").get(index).toString())
                .as("TXNAMOUNT mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateStatus(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.STATUS").get(index).toString())
                .as("STATUS mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateRespCode(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.RESPCODE").get(index).toString())
                .as("RESPCODE mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateRESPMSG(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.RESPMSG").get(index).toString())
                .as("RESPMSG mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateMID(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.MID").get(index).toString())
                .as("MID mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateREFUNDAMOUNT(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.REFUNDAMOUNT").get(index).toString())
                .as("REFUNDAMOUNT mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateREFUNDAMOUNT(Double expected, int index) {
        validateSoftly.assertThat(Double.valueOf(response.jsonPath().getList("REFUND_LIST.REFUNDAMOUNT").get(index).toString()))
                .as("REFUNDAMOUNT mismatch")
                .isEqualTo(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateTOTALREFUNDAMT(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.TOTALREFUNDAMT").get(index).toString())
                .as("TOTALREFUNDAMT mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateTOTALREFUNDAMT(Double expected, int index) {
        validateSoftly.assertThat(Double.valueOf(response.jsonPath().getList("REFUND_LIST.TOTALREFUNDAMT").get(index).toString()))
                .as("TOTALREFUNDAMT mismatch")
                .isEqualTo(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateREFID(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.REFID").get(index).toString())
                .as("REFID mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateGATEWAY(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.GATEWAY").get(index).toString())
                .as("GATEWAY mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validatePAYMENTMODE(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.PAYMENTMODE").get(index).toString())
                .as("PAYMENTMODE mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateREFUNDID(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.REFUNDID").get(index).toString())
                .as("REFUNDID mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateREFUNDTYPE(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.REFUNDTYPE").get(index).toString())
                .as("REFUNDTYPE mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }

    @Step
    public RefundStatusHelper validateBANKTXNID(String expected, int index) {
        validateSoftly.assertThat(response.jsonPath().getList("REFUND_LIST.BANKTXNID").get(index).toString())
                .as("BANKTXNID mismatch")
                .isEqualToIgnoringCase(expected);
        return this;
    }


    public void assertAll() {
        validateSoftly.assertAll();
    }

    public List<Map<String, Object>> getRefundList() {
        if (response.jsonPath().getList("REFUND_LIST").size() == 0)
            throw new AssertionError("No Refunds available in the reponse of REFUND_STATUS");

        List<Map<String, Object>> list = new ArrayList<>();
        for (Object obj : response.jsonPath().getList("REFUND_LIST")) {
            Map<String, Object> map = (Map<String, Object>) obj;
            list.add(map);
        }
        this.list = list;
        return list;
    }

    public Map<String, Object> getRefundBy(String key, Object value) {
        getRefundList();
        for (Map<String, Object> map : list) {
            if (map.containsKey(key) && map.containsValue(value)) {
                return map;
            }
        }
        return Collections.emptyMap();
    }

    public RefundStatusHelper validate(Map map, String expectedKey, Object expectedValue) {
        validateSoftly.assertThat(map.containsKey(expectedKey))
                .as("REFUND_LIST does not contains key: "+ expectedKey)
                .isTrue();
        validateSoftly.assertThat(map.containsValue(expectedValue))
                .as("REFUND_LIST does not contains value: "+ expectedValue)
                .isTrue();
        return this;
    }


    
}


