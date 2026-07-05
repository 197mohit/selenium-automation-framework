package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.TransactionStatusV1.TransactionStatusV1DTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;

import java.util.TreeMap;

import static io.restassured.config.EncoderConfig.encoderConfig;
import static org.awaitility.Awaitility.await;

public class TransactionStatusV1API extends BaseApi {

    private SoftAssertions softly = new SoftAssertions();
    private final String endPoint = "/theia/v1/transactionStatus";

    public TransactionStatusV1API(TransactionStatusV1DTO transactionStatusV1DTO) {

        getRequestSpecBuilder().setAccept(ContentType.JSON)
                .setBasePath(endPoint)
                .setBaseUri(Constants.PGP_HOST)
                .setContentType("application/json")
                .addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator())
                .setBody(transactionStatusV1DTO)
                .setConfig(RestAssured.config.encoderConfig(encoderConfig().appendDefaultContentCharsetToContentTypeIfUndefined(false)));
        setMethod(MethodType.POST);
    }

    public Response executeUntilNotPending() {
        try {
        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("body.txnInfo.STATUS")).isNotEqualToIgnoringCase("PENDING"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was not to be :- PENDING  but found to be "+this.execute().jsonPath().getString("body.txnInfo.STATUS"));
        }
        return this.execute();
    }

    public Response executeUntilPollingRequiredIsFalse() {
        try {
        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.FIVE_MINUTES).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("body.isPollingRequired")).isNotEqualToIgnoringCase("true"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was isPollingRequired is not  :- true  but found to be "+this.execute().jsonPath().getString("body.isPollingRequired"));
        }
        return this.execute();
    }

    public void validateChecksum(Response response, OrderDTO orderDTO, String key){
        TreeMap<String, String> treemap = new TreeMap<>();
        treemap.put("MID", orderDTO.getMID());
        treemap.put("ORDERID", orderDTO.getORDER_ID());
        PGPUtil.isChecksumValid(key,treemap,response.jsonPath().getString("body.txnInfo.CHECKSUMHASH"));
    }
}
