package com.paytm.framework.api_v2;

import ch.qos.logback.classic.Logger;
import com.paytm.framework.api_v2.filter.RestApiLoggingFilter_V2Base;
import com.paytm.framework.utils.PollingPredicate_V2;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.awaitility.core.ConditionTimeoutException;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

/**
 * When ever new API needs to executed <br>
 * create new object of ApiExecutor class and execute the API based on the usage
 *
 * @author ankur2.agarwal
 */
public class ApiExecutor_V2Base implements IApi_V2 {
    private static final Logger LOGGER = (Logger) LoggerFactory.getLogger(ApiExecutor_V2Base.class);
    private ApiBuilder_V2Base baseApi = null;
    private Response response = null;

    public ApiExecutor_V2Base(IApiRequest_V2 request) {
        Object reqBody = null;
        if (request.requestBody() == null) {
            reqBody = request.build();
        } else
            reqBody = request.requestBody();

        ApiBuilder_V2Base api = new ApiBuilder_V2Base();
        api.setMethod(request.apiMethod());
        api.getRequestSpecBuilder().setContentType(request.contentType());
        api.getRequestSpecBuilder().setBaseUri(request.baseUrl());
        api.getRequestSpecBuilder().setBasePath(request.basePath());
        api.getRequestSpecBuilder().addHeaders(request.headers());
        if (reqBody != null)
            api.getRequestSpecBuilder().setBody(reqBody);
        api.getRequestSpecBuilder().addFilters(Arrays.asList(new RestApiLoggingFilter_V2Base()));
        api.getRequestSpecBuilder().addQueryParams(request.queryParam());
        this.baseApi = api;

    }

    /**
     * Filter is commented in this Executor method.
     *
     * @param request
     * @param temp
     */
    public ApiExecutor_V2Base(IApiRequest_V2 request, String temp) {
        Object reqBody = null;
        if (request.requestBody() == null) {
            reqBody = request.build();
        } else
            reqBody = request.requestBody();

        ApiBuilder_V2Base api = new ApiBuilder_V2Base();
        api.setMethod(request.apiMethod());
        api.getRequestSpecBuilder().setContentType(request.contentType());
        api.getRequestSpecBuilder().setBaseUri(request.baseUrl());
        api.getRequestSpecBuilder().setBasePath(request.basePath());
        api.getRequestSpecBuilder().addHeaders(request.headers());
        if (reqBody != null)
            api.getRequestSpecBuilder().setBody(reqBody);
        api.getRequestSpecBuilder().addQueryParams(request.queryParam());
        this.baseApi = api;
    }

    @Override
    public ApiResponse_V2Base execute() {
        this.response = this.baseApi.execute();
        return new ApiResponse_V2Base(this.response);
    }

    /**
     * @param maxDuration       max time till which api re-executed
     * @param pollInterval      duration in which next api executed
     * @param pollDelay         delay duration for which next poll waits
     * @param responsePredicate
     * @return
     */
    public ApiResponse_V2Base execute_waitAndPoll(Duration maxDuration, Duration pollInterval, Duration pollDelay, Predicate<ApiResponse_V2Base> responsePredicate) {

        try {
            long maxDuration_ms = maxDuration.getValueInMS();
            long pollDelay_ms = pollDelay.getValueInMS();
            long pollInterval_ms = pollInterval.getValueInMS();

            ApiResponse_V2Base resp = new PollingPredicate_V2<ApiResponse_V2Base>(checkStatus(),
                    responsePredicate, java.time.Duration.ofMillis(maxDuration_ms), java.time.Duration.ofMillis(pollInterval_ms)).evaluate();

            assert null != resp : "Getting null response in provided timelines";

            return resp;
        } catch (ConditionTimeoutException e) {
            LOGGER.error("Provided condition not matched with API response", e);
            Assertions.fail("Provided condition not matched with API response", e);
        } catch (Exception e) {
            LOGGER.error("Provided condition not matched with API response", e);
            Assertions.fail("Provided condition not matched with API response", e);
        }
        return null;
    }


    private Callable<ApiResponse_V2Base> checkStatus() {
        LOGGER.info("Starting api polling model ........");
        final int[] counter = {0};
        return new Callable<ApiResponse_V2Base>() {
            @Override
            public ApiResponse_V2Base call() throws Exception {
                LOGGER.info("api polling count: " + counter[0]++);
                return execute();
            }
        };
    }

    /**
     * @param responsePredicate
     * @return
     */
    public ApiResponse_V2Base execute_waitAndPoll(Predicate<ApiResponse_V2Base> responsePredicate) {
        return execute_waitAndPoll(Duration.ONE_MINUTE, Duration.TWO_SECONDS, Duration.ONE_SECOND, responsePredicate);
    }

/*    public ApiResponse execute_withSleep(Duration sleepDuration)
    {
        return )
    }*/


    public void appTraceId() {
        this.response.header("app-trace-id");
    }

    public Response getResponse() {
        return this.response;
    }


}
