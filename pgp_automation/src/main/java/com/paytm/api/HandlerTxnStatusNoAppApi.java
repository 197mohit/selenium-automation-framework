package com.paytm.api;

import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.HANDLERINTERNALTXNSTATUS.HandlerInternalTxnstatusNoAPPDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.Constants;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;

import static org.awaitility.Awaitility.await;


public class HandlerTxnStatusNoAppApi extends BaseApi {

    String endPoint;

    public HandlerTxnStatusNoAppApi(HandlerInternalTxnstatusNoAPPDTO handlerInternalTxnstatusNoAPPDTO) {
        endPoint = "/merchant-status/HANDLER_INTERNAL/TXNSTATUS";
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(Constants.PGP_HOST);
        getRequestSpecBuilder().setBasePath(endPoint);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBody(handlerInternalTxnstatusNoAPPDTO);
    }


    public Response executeUntilNotPending() {
        try {
        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() -> Assertions.assertThat(this.execute().jsonPath().getString("STATUS")).isNotEqualToIgnoringCase("PENDING"));
        } catch (ConditionTimeoutException e) {
            Assertions.fail("Expected was not to be :- PENDING  but found to be "+this.execute().jsonPath().getString("STATUS"));
        }
        return this.execute();
    }
}
