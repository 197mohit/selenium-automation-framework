package com.paytm.apphelpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.paytm.LocalConfig;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.nativeAPI.SubscriptionCreate;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.Body;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.PaymentModes;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.SavedInstruments;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.NativeDTO.fetchBinDetails.response.FetchBinDetailResponse;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import redis.clients.jedis.Jedis;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.dto.NativeDTO.OfferApply.OfferApplyDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NativeHelpers {
    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env="+LocalConfig.ENV_NAME+"&ttype=hold&jsonresp=";
    public static String Validate_InitTxn(InitTxnDTO initTxnDTO) {
        String txnToken;
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        if (StringUtils.contains(response.jsonPath().getString("body.resultInfo.resultCode"), "1001")) {
            String resultCode = response.jsonPath().get("body.resultInfo.resultCode").toString();
            return resultCode;
        } else {
            txnToken = response.jsonPath().getString("body.txnToken");
            Assertions.assertThat(txnToken).withFailMessage("Txn token is %s", txnToken).isNotNull();
        }
        return txnToken;
    }

    public static <T> T convertRespToObject(Response response, Class<T> valueType) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        T t = null;
        try {
            t = mapper.readValue(jsonObject.toJSONString(), valueType);
        } catch (IOException e) {
            throw new PGPException("Change in "+ valueType.getName()+ " DTO", e);
        }
        return t;
    }

    public static InitTxnResponseDTO initiateNativeSubscription(InitTxnDTO initTxnDTO) {

        SubscriptionCreate subscriptionCreate = new SubscriptionCreate(initTxnDTO);
        Response response = subscriptionCreate.execute();
        Assertions.assertThat(response.getStatusCode())
                .as("Status code is not equal to 200")
                .isEqualTo(200);
        InitTxnResponseDTO responseDTO = convertRespToObject(response, InitTxnResponseDTO.class);
        return responseDTO;
    }

    @Step("Execute processTxn /theia/api/v1/processTransaction")
    public static ProcessTxnV1Response executeProcessTxnV1(ProcessTxnV1Request processTxnV1Request) {
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.getStatusCode())
                .as("Status code is not equal to 200")
                .isEqualTo(200);
        ProcessTxnV1Response processTxnV1Response = convertRespToObject(response, ProcessTxnV1Response.class);
        return processTxnV1Response;
    }

    public static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(String txnToken, String mid, String orderId) {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        Assertions.assertThat(response.getStatusCode())
                .as("response status code mismatch")
                .isEqualTo(200);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = convertRespToObject(response, FetchPaymentOptResponseDTO.class);
        return fetchPaymentOptResponseDTO;
    }

    public static FetchBinDetailResponse fetchBinDetailResponse(String txnToken, String mid, String orderId, String binNum) {
        return fetchBinDetailResponse(txnToken, mid, orderId, binNum, null);
    }

    /**
     * @param requestType optional (e.g. {@code NATIVE_SUBSCRIPTION}); when {@code null}, body matches legacy fetchBinDetail without {@code requestType}.
     */
    public static FetchBinDetailResponse fetchBinDetailResponse(String txnToken, String mid, String orderId, String binNum, String requestType) {
        FetchBinDetailsRequest.Builder builder = new FetchBinDetailsRequest.Builder(txnToken, binNum);
        if (requestType != null) {
            builder.setRequestType(requestType);
        }
        FetchBinDetailsRequest request = builder.build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, mid, orderId);
        Response response = fetchBinDetail.execute();
        Assertions.assertThat(response.getStatusCode())
                .as("response status code mismatch")
                .isEqualTo(200);
        FetchBinDetailResponse fetchBinDetailResponse = convertRespToObject(response, FetchBinDetailResponse.class);
        return fetchBinDetailResponse;
    }

    public static boolean isFetchPaymentOptionStatusMatched(FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO, String payMethod, boolean isDisabledStatus) {
        Body body = fetchPaymentOptResponseDTO.getBody();
        boolean isPayMethodAvailable = false;
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getMerchantPayOption().getPaymentModes();
        for(PaymentModes paymentMode : paymentModes) {
            if(payMethod.equalsIgnoreCase(paymentMode.getPaymentMode())) {
                if(Boolean.toString(isDisabledStatus).equalsIgnoreCase(paymentMode.getIsDisabled().getStatus()))
                    return true;
            }
        }
        return false;
    }

    public static boolean isSavedFetchPaymentOptionStatusMatched(FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO, String cardId, boolean isDisabledStatus) {
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<SavedInstruments> savedInstrumentsList = body.getMerchantPayOption().getSavedInstruments();
        for(SavedInstruments savedInstrument : savedInstrumentsList) {
            if(cardId.equalsIgnoreCase(savedInstrument.getCardDetails().getCardId())) {
                if (Boolean.toString(isDisabledStatus).equalsIgnoreCase(savedInstrument.getIsDisabled().getStatus()))
                    return true;
            }
        }

        return false;
    }

    public static boolean isAddMoneyFetchPaymentOptStatusMatched(FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO, String payMethod, boolean isDisabledStatus) {
        Body body = fetchPaymentOptResponseDTO.getBody();
        Assertions.assertThat(body.getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("0000");
        List<PaymentModes> paymentModes = body.getAddMoneyPayOption().getPaymentModes();
        for(PaymentModes paymentMode : paymentModes) {
            if(payMethod.equalsIgnoreCase(paymentMode.getPaymentMode())) {
                if(Boolean.toString(isDisabledStatus).equalsIgnoreCase(paymentMode.getIsDisabled().getStatus()))
                    return true;
            }
        }
        return false;
    }

    public static void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {

        final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);

        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
    }

    public static void submitJsonFormInBrowser(String jsonForm) {
        final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
    }

    

    public static void assertRedisKeysNotPresent(String txnToken){
        String redisUrl = LocalConfig.SESSION_REDIS_URI;
        Jedis jedis = RedisUtil.getInstance().getConnection(redisUrl);
        Map<String, String> myKeys = jedis.hgetAll(txnToken);
        Assertions.assertThat(myKeys.keySet()).doesNotContain("cashierInfo", "entityPaymentOption", "userDetails", "extendInfo", "initiateTxnResponse");
    }


    public static Response Validate_OfferApply(OfferApplyDTO offerApplyDTO) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String requestBody = mapper.writeValueAsString(offerApplyDTO);
        OfferApply offerApply = new OfferApply(requestBody);
        Response response = offerApply.execute();
        return response;
    }
    

}
