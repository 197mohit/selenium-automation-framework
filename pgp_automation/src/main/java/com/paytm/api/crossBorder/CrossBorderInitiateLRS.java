package com.paytm.api.crossBorder;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

/**
 * Cross-border LRS-style initiate (lighter body: no {@code goods} / {@code shippingInfo} / bank block),
 * same {@code CaptureNotify}-style template + {@code setContext} pattern as
 * {@link CrossBorderInitiate}.
 */
public class CrossBorderInitiateLRS extends BaseApi {

    private final String requestTemplate = "{"
            + "\"body\":{"
            + "\"requestType\":\"Payment\","
            + "\"mid\":\"qa14mi72298828923314\","
            + "\"orderId\":\"PACB_146\","
            + "\"websiteName\":\"retail\","
            + "\"txnAmount\":{\"value\":\"26500.00\",\"currency\":\"INR\"},"
            + "\"userInfo\":{"
            + "\"custId\":\"375c437edee24e1186695e4c1df87d22\","
            + "\"mobile\":\"7017658313\","
            + "\"email\":\"test@paytm.com\","
            + "\"firstName\":\"Rahul\","
            + "\"lastName\":\"Gupta\","
            + "\"address\":\"House-225\","
            + "\"pincode\":\"201303\","
            + "\"city\":\"Gautam Buddha Nagar\","
            + "\"state\":\"Uttar Pradesh\","
            + "\"countryName\":\"India\","
            + "\"countryCode\":\"IN\","
            + "\"pan\":\"DPWPG6194M\","
            + "\"dob\":\"1995-10-05\""
            + "},"
            + "\"callbackUrl\":\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\","
            + "\"invoiceDetails\":{"
            + "\"invoiceId\":\"PACB_146\","
            + "\"invoiceDate\":\"20260422\""
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

    public CrossBorderInitiateLRS() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBody(getCrossBorderInitiateLRSRequest());
        getRequestSpecBuilder().setBasePath(Constants.NativeAPIResourcePath.INIT_TXN);
    }

    public String getCrossBorderInitiateLRSRequest() {
        return requestTemplate;
    }

    public CrossBorderInitiateLRS setMid(String mid) {
        setContext("body.mid", mid);
        return this;
    }

    public CrossBorderInitiateLRS setOrderId(String orderId) {
        setContext("body.orderId", orderId);
        return this;
    }

    public CrossBorderInitiateLRS setRequestType(String requestType) {
        setContext("body.requestType", requestType);
        return this;
    }

    public CrossBorderInitiateLRS setWebsiteName(String websiteName) {
        setContext("body.websiteName", websiteName);
        return this;
    }

    public CrossBorderInitiateLRS setTxnAmountValue(String value) {
        setContext("body.txnAmount.value", value);
        return this;
    }

    public CrossBorderInitiateLRS setTxnAmountCurrency(String currency) {
        setContext("body.txnAmount.currency", currency);
        return this;
    }

    public CrossBorderInitiateLRS setCustId(String custId) {
        setContext("body.userInfo.custId", custId);
        return this;
    }

    public CrossBorderInitiateLRS setUserMobile(String mobile) {
        setContext("body.userInfo.mobile", mobile);
        return this;
    }

    public CrossBorderInitiateLRS setUserEmail(String email) {
        setContext("body.userInfo.email", email);
        return this;
    }

    public CrossBorderInitiateLRS setUserFirstName(String firstName) {
        setContext("body.userInfo.firstName", firstName);
        return this;
    }

    public CrossBorderInitiateLRS setUserLastName(String lastName) {
        setContext("body.userInfo.lastName", lastName);
        return this;
    }

    public CrossBorderInitiateLRS setUserAddress(String address) {
        setContext("body.userInfo.address", address);
        return this;
    }

    public CrossBorderInitiateLRS setUserPincode(String pincode) {
        setContext("body.userInfo.pincode", pincode);
        return this;
    }

    public CrossBorderInitiateLRS setUserCity(String city) {
        setContext("body.userInfo.city", city);
        return this;
    }

    public CrossBorderInitiateLRS setUserState(String state) {
        setContext("body.userInfo.state", state);
        return this;
    }

    public CrossBorderInitiateLRS setUserCountryName(String countryName) {
        setContext("body.userInfo.countryName", countryName);
        return this;
    }

    public CrossBorderInitiateLRS setUserCountryCode(String countryCode) {
        setContext("body.userInfo.countryCode", countryCode);
        return this;
    }

    public CrossBorderInitiateLRS setUserPan(String pan) {
        setContext("body.userInfo.pan", pan);
        return this;
    }

    public CrossBorderInitiateLRS setUserDob(String dob) {
        setContext("body.userInfo.dob", dob);
        return this;
    }

    public CrossBorderInitiateLRS setCallbackUrl(String callbackUrl) {
        setContext("body.callbackUrl", callbackUrl);
        return this;
    }

    public CrossBorderInitiateLRS setInvoiceId(String invoiceId) {
        setContext("body.invoiceDetails.invoiceId", invoiceId);
        return this;
    }

    public CrossBorderInitiateLRS setInvoiceDate(String invoiceDate) {
        setContext("body.invoiceDetails.invoiceDate", invoiceDate);
        return this;
    }

    public CrossBorderInitiateLRS setHeadClientId(String clientId) {
        setContext("head.clientId", clientId);
        return this;
    }

    public CrossBorderInitiateLRS setHeadVersion(String version) {
        setContext("head.version", version);
        return this;
    }

    public CrossBorderInitiateLRS setRequestTimestamp(String requestTimestamp) {
        setContext("head.requestTimestamp", requestTimestamp);
        return this;
    }

    public CrossBorderInitiateLRS setChannelId(String channelId) {
        setContext("head.channelId", channelId);
        return this;
    }

    public CrossBorderInitiateLRS setSignature(String signature) {
        setContext("head.signature", signature);
        return this;
    }
}
