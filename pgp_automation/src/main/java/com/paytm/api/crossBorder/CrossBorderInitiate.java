package com.paytm.api.crossBorder;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * Cross-border style initiate payload (native body + head) using the same template / {@code setContext}
 * pattern as {@link com.paytm.api.notification.CaptureNotify}.
 * <p>
 * Default base path is {@link Constants.NativeAPIResourcePath#INIT_TXN}; switch to a dedicated
 * cross-border path in {@link Constants} if the platform exposes a separate URL.
 */
public class CrossBorderInitiate extends BaseApi {

    private final String requestTemplate = "{"
            + "\"body\":{"
            + "\"requestType\":\"Payment\","
            + "\"mid\":\"qa14mi80241822847457\","
            + "\"orderId\":\"PACB_112\","
            + "\"websiteName\":\"retail\","
            + "\"txnAmount\":{\"value\":\"100\",\"currency\":\"INR\"},"
            + "\"userInfo\":{"
            + "\"custId\":\"375c437edee24e1186695e4c1df87d22\","
            + "\"mobile\":\"7017658313\","
            + "\"email\":\"test@paytm.com\","
            + "\"firstName\":\"Rahul\","
            + "\"lastName\":\"Gupta\","
            + "\"address\":\"Skymark One, Sector 98\","
            + "\"pincode\":\"201303\","
            + "\"city\":\"Gautam Buddha Nagar\","
            + "\"state\":\"Uttar Pradesh\","
            + "\"countryName\":\"India\","
            + "\"countryCode\":\"IN\","
            + "\"pan\":\"DPWPG6194M\","
            + "\"bankAccount\":{"
            + "\"accountNumber\":\"928827383\","
            + "\"name\":\"ICICI\","
            + "\"ifsc\":\"ICI39OH20\","
            + "\"stateCode\":\"UP\","
            + "\"countryCode\":\"IN\""
            + "},"
            + "\"ieCode\":\"ABCDE1234F\""
            + "},"
            + "\"callbackUrl\":\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\","
            + "\"goods\":[{"
            + "\"merchantGoodsId\":\"24525635625623\","
            + "\"merchantShippingId\":\"564314314574327545\","
            + "\"snapshotUrl\":\"http://snap.url.com\","
            + "\"description\":\"Luggage Bag\","
            + "\"category\":\"travelling/subway\","
            + "\"quantity\":\"3.2\","
            + "\"unit\":\"Kg\","
            + "\"price\":{\"currency\":\"INR\",\"value\":\"1000\"},"
            + "\"productSku\":\"LSKD911084\","
            + "\"productName\":\"Luggage Bag\","
            + "\"productCode\":\"LSKD911084\","
            + "\"hsnCode\":\"42021201\""
            + "}],"
            + "\"shippingInfo\":[{"
            + "\"chargeAmount\":{\"currency\":\"INR\",\"value\":\"1\"},"
            + "\"lastName\":\"Li\","
            + "\"trackingNo\":\"64643143132\","
            + "\"countryName\":\"JP\","
            + "\"merchantShippingId\":\"564314314574327545\","
            + "\"cityName\":\"Atlanta\","
            + "\"address1\":\"137 W San Bernardino\","
            + "\"address2\":\"4114 Sepulveda\","
            + "\"email\":\"abc@gmail.com\","
            + "\"zipCode\":\"310001\","
            + "\"stateName\":\"GA\","
            + "\"carrier\":\"Federal Express\","
            + "\"firstName\":\"Jim\","
            + "\"mobileNo\":\"13765443223\","
            + "\"shipmentDate\":\"20260325\","
            + "\"shipmentFrom\":\"Ohio\","
            + "\"shipmentTo\":\"Delhi\","
            + "\"consignerName\":\"James\","
            + "\"consignerAddress\":\"137 W San Bernardino\","
            + "\"deliveryAgent\":\"Test\","
            + "\"packingMode\":\"Carton\""
            + "}],"
            + "\"invoiceDetails\":{"
            + "\"invoiceId\":\"PACB_112\","
            + "\"invoiceDate\":\"20260409\""
            + "}"
            + "},"
            + "\"head\":{"
            + "\"clientId\":\"C11\","
            + "\"version\":\"v1\","
            + "\"requestTimestamp\":\"Time\","
            + "\"channelId\":\"WEB\","
            + "\"signature\":\"KKa2THU2OmLvC6WwzdX5NBSczOnBYUJ/RCvHJmeOkksagVhdgSqVm5EKqvIwujwiY8yRQrdDID9XvwAZ6srRiBd+qW7Q33XfLKQNNMb30Xk=\""
            + "}"
            + "}";

    public CrossBorderInitiate() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getCrossBorderInitiateRequest());
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);
    }

    public String getCrossBorderInitiateRequest() {
        return requestTemplate;
    }

    public CrossBorderInitiate setMid(String mid) {
        setContext("body.mid", mid);
        return this;
    }

    public CrossBorderInitiate setOrderId(String orderId) {
        setContext("body.orderId", orderId);
        return this;
    }

    public CrossBorderInitiate setRequestType(String requestType) {
        setContext("body.requestType", requestType);
        return this;
    }

    public CrossBorderInitiate setWebsiteName(String websiteName) {
        setContext("body.websiteName", websiteName);
        return this;
    }

    public CrossBorderInitiate setTxnAmountValue(String value) {
        setContext("body.txnAmount.value", value);
        return this;
    }

    public CrossBorderInitiate setTxnAmountCurrency(String currency) {
        setContext("body.txnAmount.currency", currency);
        return this;
    }

    public CrossBorderInitiate setCustId(String custId) {
        setContext("body.userInfo.custId", custId);
        return this;
    }

    public CrossBorderInitiate setUserMobile(String mobile) {
        setContext("body.userInfo.mobile", mobile);
        return this;
    }

    public CrossBorderInitiate setUserEmail(String email) {
        setContext("body.userInfo.email", email);
        return this;
    }

    public CrossBorderInitiate setUserFirstName(String firstName) {
        setContext("body.userInfo.firstName", firstName);
        return this;
    }

    public CrossBorderInitiate setUserLastName(String lastName) {
        setContext("body.userInfo.lastName", lastName);
        return this;
    }

    public CrossBorderInitiate setCallbackUrl(String callbackUrl) {
        setContext("body.callbackUrl", callbackUrl);
        return this;
    }

    public CrossBorderInitiate setHsnCode(String hsnCode) {
        setContext("body.goods[0].hsnCode", hsnCode);
        return this;
    }

    public CrossBorderInitiate setInvoiceId(String invoiceId) {
        setContext("body.invoiceDetails.invoiceId", invoiceId);
        return this;
    }

    public CrossBorderInitiate setInvoiceDate(String invoiceDate) {
        setContext("body.invoiceDetails.invoiceDate", invoiceDate);
        return this;
    }

    public CrossBorderInitiate setHeadClientId(String clientId) {
        setContext("head.clientId", clientId);
        return this;
    }

    public CrossBorderInitiate setHeadVersion(String version) {
        setContext("head.version", version);
        return this;
    }

    public CrossBorderInitiate setRequestTimestamp(String requestTimestamp) {
        setContext("head.requestTimestamp", requestTimestamp);
        return this;
    }

    public CrossBorderInitiate setChannelId(String channelId) {
        setContext("head.channelId", channelId);
        return this;
    }

    public CrossBorderInitiate setSignature(String signature) {
        setContext("head.signature", signature);
        return this;
    }
}
