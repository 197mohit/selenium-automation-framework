package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory.BankMandate;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.api.BaseApi;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.response.Response;


/**
 * Created By ankur2.agarwal
 */
public class ProcessTransactionV1 extends BaseApi {

    private static final String URL = "/theia/api/v1/processTransaction";
    /** {@link OrderDTO#getPAYMENT_TYPE_ID()} value for native bank mandate flows. */
    public static final String PAYMENT_TYPE_BANK_MANDATE = "BANK_MANDATE";
    private Response response;

    /**
     * Bank-mandate-only helper: builds {@link ProcessTxnV1Request} from {@link BankMandate}. Other pay modes must use
     * {@link #ProcessTransactionV1(ProcessTxnV1Request)} so mandate-specific fields are never applied.
     *
     * @throws IllegalArgumentException if the built order’s {@link OrderDTO#getPAYMENT_TYPE_ID()} is not {@link #PAYMENT_TYPE_BANK_MANDATE}
     */
    public ProcessTransactionV1(BankMandate bankMandate) {
        this(requestForBankMandate(requireBankMandateOrder(bankMandate.build())));
    }

    /**
     * Same as {@link #ProcessTransactionV1(BankMandate)} when you already hold a built {@link OrderDTO}.
     *
     * @throws IllegalArgumentException if {@link OrderDTO#getPAYMENT_TYPE_ID()} is not {@link #PAYMENT_TYPE_BANK_MANDATE}
     */
    public ProcessTransactionV1(OrderDTO orderDTO) {
        this(requestForBankMandate(requireBankMandateOrder(orderDTO)));
    }

    private static OrderDTO requireBankMandateOrder(OrderDTO orderDTO) {
        if (!isBankMandateOrder(orderDTO)) {
            throw new IllegalArgumentException(
                    "Bank-mandate PTC wiring applies only when PAYMENT_TYPE_ID is "
                            + PAYMENT_TYPE_BANK_MANDATE
                            + ". For all other flows use new ProcessTransactionV1(ProcessTxnV1Request).");
        }
        return orderDTO;
    }

    private static boolean isBankMandateOrder(OrderDTO orderDTO) {
        return orderDTO != null && PAYMENT_TYPE_BANK_MANDATE.equals(orderDTO.getPAYMENT_TYPE_ID());
    }

    /** PTC body uses a single account-type field; prefer camelCase {@link OrderDTO#getAccountType()} when set. */
    private static String accountTypeForPtc(OrderDTO orderDTO) {
        String camel = orderDTO.getAccountType();
        if (camel != null && !camel.isEmpty()) {
            return camel;
        }
        return orderDTO.getACCOUNT_TYPE();
    }

    private static ProcessTxnV1Request requestForBankMandate(OrderDTO orderDTO) {
        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(
                orderDTO.getMID(), orderDTO.getTXN_TOKEN(), orderDTO.getORDER_ID())
                .setPaymentMode(orderDTO.getPAYMENT_TYPE_ID() != null ? orderDTO.getPAYMENT_TYPE_ID() : "BANK_MANDATE")
                .setChannelCode(orderDTO.getChannelCode())
                .setChannelId(orderDTO.getCHANNEL_ID())
                .setAuthMode(orderDTO.getAUTH_MODE())
                .setMandateAuthMode(orderDTO.getMandateAuthMode())
                // Both keys used by bank-mandate PTC: OrderDTO.account_number vs OrderDTO.accountNumber
                .setAccountNumber(orderDTO.getAccount_number())
                .setAccountNumberCamel(orderDTO.getAccountNumber())
                .setBankIfsc(orderDTO.getBankIfsc())
                .setUserNameCamel(orderDTO.getUserName())
                .setAccountHolderName(orderDTO.getUserName())
                .setAccountTypeCamel(accountTypeForPtc(orderDTO))
                .setWebsite(orderDTO.getWEBSITE() != null ? orderDTO.getWEBSITE() : "retail")
                .setMerchantKey(orderDTO.getMerchantKey());
        String custId = orderDTO.getCUST_ID();
        if (custId != null && !custId.isEmpty()) {
            builder.setCustId(custId);
        }
        String payerAccount = orderDTO.getPayerAccount();
        if (payerAccount != null && !payerAccount.isEmpty()) {
            builder.setPayerAccount(payerAccount);
        }
        String riskExtendInfo = orderDTO.getRiskExtendInfo();
        if (riskExtendInfo != null && !riskExtendInfo.isEmpty()) {
            builder.setRiskExtendInfo(riskExtendInfo);
        }
        return builder.build();
    }

    public ProcessTransactionV1(ProcessTxnV1Request processTxnV1Request) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(URL);
        getRequestSpecBuilder().addQueryParam("mid", processTxnV1Request.getBody().getMid());
        getRequestSpecBuilder().addQueryParam("orderId", processTxnV1Request.getBody().getOrderId());
        getRequestSpecBuilder().setBody(processTxnV1Request);
    }

    public  ProcessTransactionV1(ProcessTxnV1Request processTxnV1Request , String deviceManufacturer, String deviceName, String deviceIdentifier) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(URL);
        getRequestSpecBuilder().addQueryParam("mid", processTxnV1Request.getBody().getMid());
        getRequestSpecBuilder().addQueryParam("orderId", processTxnV1Request.getBody().getOrderId());
        getRequestSpecBuilder().addQueryParam("deviceIdentifier",deviceIdentifier);
        getRequestSpecBuilder().addQueryParam("deviceManufacturer",deviceManufacturer);
        getRequestSpecBuilder().addQueryParam("deviceName",deviceName);
        getRequestSpecBuilder().setBody(processTxnV1Request);
    }

    public ProcessTransactionV1(ProcessTxnV1Request processTxnV1Request, String orderId) {
        if(orderId==null) {
            setMethod(BaseApi.MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(URL);
            getRequestSpecBuilder().setBody(processTxnV1Request);
        }
        else
        {
            setMethod(BaseApi.MethodType.POST);
            getRequestSpecBuilder().setContentType(ContentType.JSON);
            getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
            getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
            getRequestSpecBuilder().setBasePath(URL);
            getRequestSpecBuilder().addQueryParam("orderId", orderId);
            getRequestSpecBuilder().setBody(processTxnV1Request);
        }
    }

    public ProcessTransactionV1(ProcessTxnV1Request processTxnV1Request,String deviceIdentifier,String osVersion,String simSubscriptionId,Double lat,Double longitude,String networkType,String version,String playStore,String xIntegrity,String xDebStatus,String xAppRid,String xSimSubId) {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(URL);
        getRequestSpecBuilder().addHeader("x-integrity",xIntegrity);
        getRequestSpecBuilder().addHeader("x-deb-status",xDebStatus);
        getRequestSpecBuilder().addHeader("x-app-rid",xAppRid);
        getRequestSpecBuilder().addHeader("x-sim-sub-id",xSimSubId);
        getRequestSpecBuilder().addQueryParam("mid", processTxnV1Request.getBody().getMid());
        getRequestSpecBuilder().addQueryParam("orderId", processTxnV1Request.getBody().getOrderId());
        getRequestSpecBuilder().addQueryParam("deviceIdentifier",deviceIdentifier);
        getRequestSpecBuilder().addQueryParam("osVersion",osVersion);
        getRequestSpecBuilder().addQueryParam("simSubscriptionId",simSubscriptionId);
        getRequestSpecBuilder().addQueryParam("lat",lat);
        getRequestSpecBuilder().addQueryParam("long",longitude);
        getRequestSpecBuilder().addQueryParam("networkType",networkType);
        getRequestSpecBuilder().addQueryParam("playStore",playStore);
        getRequestSpecBuilder().addQueryParam("version",version);
        getRequestSpecBuilder().setBody(processTxnV1Request);
    }
}