package com.paytm.api.MappingService;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class VendorDetails extends BaseApi {

    String request ="{\n" +
            "    \"requestType\": \"VENDOR_MERCHANT\",\n" +
            "    \"linkedMainMerchantId\": \"{MID}\",\n" +
            "    \"settleCycle\": \"T+1\",\n" +
            "    \"merchantInfo\": {\n" +
            "        \"entityId\": null,\n" +
            "        \"s2sCallbackEnabled\": null,\n" +
            "        \"secondaryEmail\": null,\n" +
            "        \"kycStatus\": null,\n" +
            "        \"isDownloaded\": null,\n" +
            "        \"numberOfRetry\": null,\n" +
            "        \"invoiceEmail\": null,\n" +
            "        \"bankAccNo\": null,\n" +
            "        \"comment\": null,\n" +
            "        \"signedTime\": null,\n" +
            "        \"status\": null,\n" +
            "        \"contactMname\": null,\n" +
            "        \"addProofnoPersonal\": null,\n" +
            "        \"merchantName\": null,\n" +
            "        \"isPeonEnable\": null,\n" +
            "        \"signatoryName\": null,\n" +
            "        \"ifscCode\": null,\n" +
            "        \"primaryEmail\": \"vendor123@gmail.com\",\n" +
            "        \"secondaryMobileno\": null,\n" +
            "        \"contactMobile\": \"9912345678\",\n" +
            "        \"sap\": null,\n" +
            "        \"custCommPref\": null,\n" +
            "        \"keySize\": null,\n" +
            "        \"businessName\": null,\n" +
            "        \"panNoPersonal\": null,\n" +
            "        \"entityKey\": null,\n" +
            "        \"eciStatus\": null,\n" +
            "        \"idProofnoPersonal\": null,\n" +
            "        \"contactLname\": \"Merchant4\",\n" +
            "        \"panNoBusiness\": null,\n" +
            "        \"callbackUrlEnabled\": null,\n" +
            "        \"secondaryLastname\": null,\n" +
            "        \"walletEnabled\": null,\n" +
            "        \"secondaryPhoneno\": null,\n" +
            "        \"walletRechargeRnabled\": null,\n" +
            "        \"contactFname\": \"Test\",\n" +
            "        \"merchCommPref\": null,\n" +
            "        \"productCode\": null,\n" +
            "        \"secondaryFirstname\": null,\n" +
            "        \"merchRefCommPref\": null,\n" +
            "        \"custRefCommPref\": null,\n" +
            "        \"isOtpThemeEnabled\": null,\n" +
            "        \"isApiRefundAllowed\": null,\n" +
            "        \"maxAmountForComplexRefund\": null,\n" +
            "        \"peonRequestType\": null,\n" +
            "        \"peonServiceName\": null,\n" +
            "        \"merchantWebForcedTheme\": null,\n" +
            "        \"merchantWapForcedTheme\": null,\n" +
            "        \"secureStatusEnabled\": null,\n" +
            "        \"urbanAirshipHash\": null,\n" +
            "        \"urbanAirshipEnabled\": false,\n" +
            "        \"platformType\": null,\n" +
            "        \"minPartialRenewalPercentage\": null,\n" +
            "        \"blocked\": false,\n" +
            "        \"aggregatorMid\": null,\n" +
            "        \"additionalEmails\": null,\n" +
            "        \"alipayMid\": null,\n" +
            "        \"isMerchant\": null,\n" +
            "        \"merchantLimit\": null,\n" +
            "        \"userId\": null,\n" +
            "        \"gstin\": null,\n" +
            "        \"paymentInvoiceMobile\": null,\n" +
            "        \"paymentInvoiceEmail\": null,\n" +
            "        \"ONPAYTM\": false\n" +
            "    },\n" +
            "    \"merchantKycInfo\": {\n" +
            "        \"mid\": null,\n" +
            "        \"entityId\": null,\n" +
            "        \"panNoBusiness\": \"\",\n" +
            "        \"bankAccNo\": \"1234567898765\",\n" +
            "        \"kycStatus\": null,\n" +
            "        \"signatoryName\": \"Nitin\",\n" +
            "        \"ifscCode\": \"HDFC0000123\",\n" +
            "        \"panNoPersonal\": \"\",\n" +
            "        \"addProofNoPersonal\": \"TEST123N#\",\n" +
            "        \"idProofNoPersonal\": \"\",\n" +
            "        \"createdBy\": null,\n" +
            "        \"createdDate\": null,\n" +
            "        \"modifiedBy\": null,\n" +
            "        \"modifiedDate\": null,\n" +
            "        \"bankAccountName\": \"nitin\",\n" +
            "        \"bankName\": \"ICICI\",\n" +
            "        \"gstin\": \"kycGSTIN\",\n" +
            "        \"billingPos\": null\n" +
            "    }\n" +
            "}";


    public VendorDetails setRequest(String mid)
    {
        this.request = request.replace("{MID}",mid);
        return this;
    }

    public String getRequest()
    {
        return request;
    }
    public VendorDetails(String paytmMerchantId)

    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.MERCHANT_VENDOR_DETAILS);
        setRequest(paytmMerchantId);
        getRequestSpecBuilder().setBody(getRequest());

    }

    public VendorDetails buildRequest(String mid) {
        setContext("linkedMainMerchantId", mid);
        return this;
    }

}
