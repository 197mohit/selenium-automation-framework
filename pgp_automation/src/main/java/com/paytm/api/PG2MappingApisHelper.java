package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;

import java.util.HashMap;
import java.util.Map;
import java.util.*;
import com.google.gson.JsonObject;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.LinkPaymentLoginPage;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.restassured.path.json.JsonPath;
import org.apache.tools.ant.taskdefs.Sleep;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.asserts.Assertion;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class PG2MappingApisHelper {
    public void verifyMerchantAttributeKey(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("aesKey");
        ExpectedResponse.add("userKey");
        ExpectedResponse.add("sharedSecret");
        ExpectedResponse.add("utilCode");
        ExpectedResponse.add("catCode");
        ExpectedResponse.add("name");
        ExpectedResponse.add("catDesc");
        ExpectedResponse.add("clientId");
        ExpectedResponse.add("merchantAccRef");
        ExpectedResponse.add("subUserEmail");
        ExpectedResponse.add("corpConfigId");
        ExpectedResponse.add("clientSecret");

        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyQueryMerchantPreferenceInfo(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("merchantPreferenceInfos");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyMerchantGetPreferenceinfosext(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("restStatus");
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultResp");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantAttributeKeyWithPaymode(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantKeys");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyGetMbid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("mbId");
        ExpectedResponse.add("mpId");
        ExpectedResponse.add("key");
        ExpectedResponse.add("parameter");
        ExpectedResponse.add("bankId");
        ExpectedResponse.add("payMethodId");
        ExpectedResponse.add("authModeId");
        ExpectedResponse.add("status");
        ExpectedResponse.add("emi");
        ExpectedResponse.add("instantSettlementVpa");
        ExpectedResponse.add("instantSettlement");
        ExpectedResponse.add("tid3DS2EncKey");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyMerchantGetExtendedInfo(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("extendedInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyMerchantProfile(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("mccCodes");
        ExpectedResponse.add("logoUrl");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("englishName");
        ExpectedResponse.add("localName");
        ExpectedResponse.add("certificateType");
        ExpectedResponse.add("certificateNo");
        ExpectedResponse.add("certificateUrls");
        ExpectedResponse.add("certificateExpiryDate");
        ExpectedResponse.add("registeredAddress");
        ExpectedResponse.add("officeAddress");
        ExpectedResponse.add("officeTelephone");
        ExpectedResponse.add("faxTelephone");
        ExpectedResponse.add("corporateOfficialName");
        ExpectedResponse.add("corporateCertificateType");
        ExpectedResponse.add("corporateCertificateNo");
        ExpectedResponse.add("contactOfficialName");
        ExpectedResponse.add("contactMobileNo");
        ExpectedResponse.add("contactTelephone");
        ExpectedResponse.add("contactEmail");
        ExpectedResponse.add("createdTime");
        ExpectedResponse.add("merchantStatus");
        ExpectedResponse.add("certifyStatus");
        ExpectedResponse.add("category");
        ExpectedResponse.add("subCategory");
        ExpectedResponse.add("mcc");
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("isAggregatorMerchant");
        ExpectedResponse.add("aesKey");
        ExpectedResponse.add("offlinePostConvenience");
        ExpectedResponse.add("merchantQrTag");
        ExpectedResponse.add("ppiLimit");
        ExpectedResponse.add("merchantBankName");
        ExpectedResponse.add("postPaidOnAddNPay");
        ExpectedResponse.add("merchantSolutiontype");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyQueryMerchantMigrationContractDetails(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("contractDetailList");
        ExpectedResponse.add("paytmResultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyGetEntityurlinformid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("postBackurl");
        ExpectedResponse.add("urlTypeId");
        ExpectedResponse.add("status");
        ExpectedResponse.add("comments");
        ExpectedResponse.add("createdOn");
        ExpectedResponse.add("modifiedOn");
        ExpectedResponse.add("websiteName");
        ExpectedResponse.add("requestName");
        ExpectedResponse.add("notificationStatusUrl");
        ExpectedResponse.add("imageName");
        ExpectedResponse.add("imageData");
        ExpectedResponse.add("mid");
        ExpectedResponse.add("refundUrl");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantData(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("alipayId");
        ExpectedResponse.add("alipayWalletId");
        ExpectedResponse.add("contractPayload");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("businessName");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("entityId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantApiUrlInfo(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("merchantApiUrlInfoList");

        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyMerchantThematicDetail(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("restStatus");
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultResp");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantDeviceDetailV2TID(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        Assert.assertNotNull(withDrawJson1.getString("response.tid"));
        Assert.assertNotNull(withDrawJson1.getString("response.mid"));
        Assert.assertNotNull(withDrawJson1.getString("response.monthlyRental"));
        Assert.assertNotNull(withDrawJson1.getString("response.tmsTid"));
        Assert.assertNotNull(withDrawJson1.getString("response.modelName"));
        Assert.assertNotNull(withDrawJson1.getString("response.vendorName"));
        Assert.assertNotNull(withDrawJson1.getString("response.tmsStatus"));
        Assert.assertNotNull(withDrawJson1.getString("response.tmsStatusMsg"));
        Assert.assertNotNull(withDrawJson1.getString("response.terminalStatus"));
        Assert.assertNotNull(withDrawJson1.getString("response.terminalStatusMsg"));
        Assert.assertNotNull(withDrawJson1.getString("response.isUsed"));
        Assert.assertNotNull(withDrawJson1.getString("response.createdDate"));
        Assert.assertNotNull(withDrawJson1.getString("response.modifiedDate"));
        Assert.assertNotNull(withDrawJson1.getString("response.merchantName"));
        Assert.assertNotNull(withDrawJson1.getString("response.merchantCategory"));
        Assert.assertNotNull(withDrawJson1.getString("response.merchantSubCategory"));
        Assert.assertNotNull(withDrawJson1.getString("response.terminalLatitude"));
        Assert.assertNotNull(withDrawJson1.getString("response.terminalLongitude"));
        Assert.assertNotNull(withDrawJson1.getString("response.addressOne"));
        Assert.assertNotNull(withDrawJson1.getString("response.city"));
        Assert.assertNotNull(withDrawJson1.getString("response.stateName"));
        Assert.assertNotNull(withDrawJson1.getString("response.countryName"));
        Assert.assertNotNull(withDrawJson1.getString("response.zipcode"));
        Assert.assertNotNull(withDrawJson1.getString("response.kybId"));

    }

    public void verifyMerchantDeviceDetailTIDAndbankName(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        Assert.assertNotNull(withDrawJson1.getString("response.tid"));
        Assert.assertNotNull(withDrawJson1.getString("response.mid"));


    }

    public void verifyMerchantDeviceDetailWithTID(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("data");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        Assert.assertNotNull(withDrawJson1.getString("data.id"));
        Assert.assertNotNull(withDrawJson1.getString("data.tid"));
        Assert.assertNotNull(withDrawJson1.getString("data.mid"));


    }

    public void verifyLogoCobrandingDetails(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("mid");
        ExpectedResponse.add("posProvider");
        ExpectedResponse.add("brandingReq");
        ExpectedResponse.add("brandReq");
        ExpectedResponse.add("logoUMP");
        ExpectedResponse.add("logoEDC");
        ExpectedResponse.add("logoP4BL");
        ExpectedResponse.add("logoP4BH");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        Assert.assertNotNull(withDrawJson1.getString("mid"));
        Assert.assertNotNull(withDrawJson1.getString("posProvider"));
        Assert.assertNotNull(withDrawJson1.getString("brandingReq"));
        Assert.assertNotNull(withDrawJson1.getString("brandReq"));
        Assert.assertNotNull(withDrawJson1.getString("logoUMP"));
        Assert.assertNotNull(withDrawJson1.getString("logoEDC"));
        Assert.assertNotNull(withDrawJson1.getString("logoP4BL"));
        Assert.assertNotNull(withDrawJson1.getString("logoP4BH"));
    }
    public void verifyMerchantLogoInfo(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantBusinessName");
        ExpectedResponse.add("merchantDisplayName");
        ExpectedResponse.add("merchantImageName");
        ExpectedResponse.add("paytmMid");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        Assert.assertNotNull(withDrawJson1.getString("merchantBusinessName"));
        Assert.assertNotNull(withDrawJson1.getString("merchantDisplayName"));
        Assert.assertNotNull(withDrawJson1.getString("paytmMid"));
        Assert.assertNotNull(withDrawJson1.getString("response.resultCode"));
        Assert.assertNotNull(withDrawJson1.getString("response.resultStatus"));
        Assert.assertNotNull(withDrawJson1.getString("response.messaage"));

    }
    public void verifyMerchantLogoCobrandingWithChannel(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        Assert.assertNotNull(withDrawJson1.getString("response.mid"));
        Assert.assertNotNull(withDrawJson1.getString("response.cobrandingBank"));
        Assert.assertNotNull(withDrawJson1.getString("response.logo"));
        Assert.assertNotNull(withDrawJson1.getString("response.cobrandingModelType"));
        Assert.assertNotNull(withDrawJson1.getString("resultInfo.resultStatus"));
        Assert.assertNotNull(withDrawJson1.getString("resultInfo.messaage"));
        Assert.assertNotNull(withDrawJson1.getString("resultInfo.resultCode"));

    }
    public void verifyGetAliPayId(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("oldpgId");
        ExpectedResponse.add("oldpgWalletId");
        ExpectedResponse.add("contractPayload");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("businessName");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("entityId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }


    }
    public void verifyMerchantIdmap(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();

        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("alipayId");
        ExpectedResponse.add("guid");
        ExpectedResponse.add("ssoId");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("alipayWalletId");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("enityId");
        ExpectedResponse.add("pg2DirectOnboarding");
        ExpectedResponse.add("businessName");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }


    }

    public void verifyGetEntityurlinformidV2(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("postBackurl");
        ExpectedResponse.add("urlTypeId");
        ExpectedResponse.add("status");
        ExpectedResponse.add("comments");
        ExpectedResponse.add("createdOn");
        ExpectedResponse.add("modifiedOn");
        ExpectedResponse.add("websiteName");
        ExpectedResponse.add("requestName");
        ExpectedResponse.add("notificationStatusUrl");
        ExpectedResponse.add("imageName");
        ExpectedResponse.add("imageData");
        ExpectedResponse.add("mid");
        ExpectedResponse.add("refundUrl");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }


    public void verifyMerchantAttributeAdditional(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("sapCode");
        ExpectedResponse.add("ifscCode");
        ExpectedResponse.add("panVerified");
        ExpectedResponse.add("accountNumber");
        ExpectedResponse.add("accountHolderName");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("isAggregator");
        ExpectedResponse.add("aggregatorMerchantId");
        ExpectedResponse.add("category");
        ExpectedResponse.add("subCategory");
        ExpectedResponse.add("mccCode");
        ExpectedResponse.add("customRefundRetry");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantAttributeGetSubscriptionDetail(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("subscriptionMaxLimit");
        ExpectedResponse.add("subscriptionCountLimit");
        ExpectedResponse.add("subscriptionOnDemandFlag");
        ExpectedResponse.add("subscriptionCreationCallBack");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyQueryMerchantAcquiring(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void verifyMerchantQueryAcquiring(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyQueryMerchantAcquiringPaymode(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantDeviceDetailsBankslistTid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("data");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyUserGetMerchantExtendedInfo(Map<String, String> actualResponse, Map<String, String> extndinfoactualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("extendedInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        HashSet<String>ExpectedExtndinfoResponse=new HashSet<>();
        HashSet<String>ExtndinfoactualResponse=new HashSet<>();
        ExpectedExtndinfoResponse.add("communicationContact");
        ExpectedExtndinfoResponse.add("isDownloaded");
        ExpectedExtndinfoResponse.add("numberOfRetry");
        ExpectedExtndinfoResponse.add("businessName");
        ExpectedExtndinfoResponse.add("platformType");
        ExpectedExtndinfoResponse.add("invoiceEmail");
        ExpectedExtndinfoResponse.add("isMerchant");
        ExpectedExtndinfoResponse.add("bankAccNo");
        ExpectedExtndinfoResponse.add("merchantName");
        ExpectedExtndinfoResponse.add("signedTime");
        ExpectedExtndinfoResponse.add("contactMname");
        ExpectedExtndinfoResponse.add("urbanAirshipHash");
        ExpectedExtndinfoResponse.add("oldpgMid");
        ExpectedExtndinfoResponse.add("additionalEmails");
        ExpectedExtndinfoResponse.add("signatoryName");
        ExpectedExtndinfoResponse.add("secondaryLastname");
        ExpectedExtndinfoResponse.add("primaryEmail");
        ExpectedExtndinfoResponse.add("merchantWebForcedTheme");
        ExpectedExtndinfoResponse.add("contactMobile");
        ExpectedExtndinfoResponse.add("panNoPersonal");
        ExpectedExtndinfoResponse.add("entityKey");
        ExpectedExtndinfoResponse.add("contactLname");
        ExpectedExtndinfoResponse.add("callbackUrlEnabled");
        ExpectedExtndinfoResponse.add("merchantLimit");
        ExpectedExtndinfoResponse.add("ONPAYTM");
        ExpectedExtndinfoResponse.add("peonRequestType");
        ExpectedExtndinfoResponse.add("peonServiceName");
        ExpectedExtndinfoResponse.add("paymentInvoiceEmail");
        ExpectedExtndinfoResponse.add("idProofnoPersonal");
        ExpectedExtndinfoResponse.add("status");
        ExpectedExtndinfoResponse.add("s2sCallbackEnabled");
        ExpectedExtndinfoResponse.add("secondaryEmail");
        ExpectedExtndinfoResponse.add("kycStatus");
        ExpectedExtndinfoResponse.add("isApiRefundAllowed");
        ExpectedExtndinfoResponse.add("chargeBackEmails");
        ExpectedExtndinfoResponse.add("gstin");
        ExpectedExtndinfoResponse.add("isPeonEnable");
        ExpectedExtndinfoResponse.add("urbanAirshipEnabled");
        ExpectedExtndinfoResponse.add("isOtpThemeEnabled");
        ExpectedExtndinfoResponse.add("aggregatorMid");
        ExpectedExtndinfoResponse.add("blocked");
        ExpectedExtndinfoResponse.add("secondaryPhoneno");
        ExpectedExtndinfoResponse.add("custRefCommPref");
        ExpectedExtndinfoResponse.add("ifscCode");
        ExpectedExtndinfoResponse.add("paymentInvoiceMobile");
        ExpectedExtndinfoResponse.add("addProofnoPersonal");
        ExpectedExtndinfoResponse.add("maxAmountForComplexRefund");
        ExpectedExtndinfoResponse.add("sap");
        ExpectedExtndinfoResponse.add("custCommPref");
        ExpectedExtndinfoResponse.add("keySize");
        ExpectedExtndinfoResponse.add("walletRechargeRnabled");
        ExpectedExtndinfoResponse.add("eciStatus");
        ExpectedExtndinfoResponse.add("entityId");
        ExpectedExtndinfoResponse.add("panNoBusiness");
        ExpectedExtndinfoResponse.add("merchRefCommPref");
        ExpectedExtndinfoResponse.add("userId");
        ExpectedExtndinfoResponse.add("secureStatusEnabled");
        ExpectedExtndinfoResponse.add("productCode");
        ExpectedExtndinfoResponse.add("kybId");
        ExpectedExtndinfoResponse.add("walletEnabled");
        ExpectedExtndinfoResponse.add("contactFname");
        ExpectedExtndinfoResponse.add("comment");
        ExpectedExtndinfoResponse.add("merchCommPref");
        ExpectedExtndinfoResponse.add("secondaryFirstname");
        ExpectedExtndinfoResponse.add("minPartialRenewalPercentage");
        ExpectedExtndinfoResponse.add("merchantWapForcedTheme");
        ExpectedExtndinfoResponse.add("secondaryMobileno");
        ExpectedExtndinfoResponse.add("oldpgMid");
        Iterator<String> Extkeys = extndinfoactualResponse.keySet().iterator();
        while (Extkeys.hasNext()) {
            String key1 = Extkeys.next();
            ExtndinfoactualResponse.add(key1);
        }
        System.out.println("Actual extendinfo Api Response " + ExtndinfoactualResponse);
        if (!ExpectedExtndinfoResponse.equals(ExtndinfoactualResponse)) {
            System.out.println("Expected extendinfo Api Response " + ExpectedExtndinfoResponse );
            Assert.fail("Extend info Response Schema is not matched");
        }
    }

    public void verifyMerchantV1Api(Map<String, String> actualResponse,JsonPath withDrawJson1 ) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        Assert.assertNotNull(withDrawJson1.getString("response.paytmId"));
        Assert.assertNotNull(withDrawJson1.getString("response.oldpgId"));
        Assert.assertNotNull(withDrawJson1.getString("response.guid"));
        Assert.assertNotNull(withDrawJson1.getString("response.officialName"));
        Assert.assertNotNull(withDrawJson1.getString("response.merchantType"));
        Assert.assertNotNull(withDrawJson1.getString("response.industryTypeId"));
        Assert.assertNotNull(withDrawJson1.getString("response.entityId"));
    }

    public void verifyPG2Routes(String mid) throws InterruptedException {
        Thread.sleep(20000);
        //String grepcmd="";
        //if(LocalConfig.ENV_NAME=="QA14" || LocalConfig.ENV_NAME=="QA12"){
            //String grepcmd = "grep \"" + "\"" + LocalConfig.MAPPING_LOGS + " | " +
                   // "grep \"" + mid + "\" | grep \"Traffic routes to Merchant Center for this merchantId " + mid + "\"";
        //}
           String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
                    "grep \"" + mid + "\" | grep \"traffic routes to PG2 for this merchantId " + mid + "\"";

        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);

        System.out.print("logs are--+"+mappingServiceLogs);
//        Assertions.assertThat(mappingServiceLogs).contains("traffic routes to PG2 for this merchantId "+mid);

    }
    public void verifyMerchantCenterRoutes(String mid) throws InterruptedException {
        Thread.sleep(30000);
        //String grepcmd="";
        //if(LocalConfig.ENV_NAME=="QA14" || LocalConfig.ENV_NAME=="QA12") {
             //String grepcmd = "grep \"" + "\"" + LocalConfig.MAPPING_LOGS + " | " +
                   // "grep \"" + mid + "\" | grep \"Traffic routes to Merchant Center for this merchantId " + mid + "\"";
       // }

            String  grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
                    "grep \"" + mid + "\" | grep \"Traffic routes to Merchant Center for this merchantId "+mid+"\"";


        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);

//        System.out.println("logs are--+"+mappingServiceLogs);
//        Assertions.assertThat(mappingServiceLogs).contains("Traffic routes to Merchant Center for this merchantId "+mid);

    }
    public void verifyPlateformPlusFallback(String mid,String reqId) throws InterruptedException {
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
                "grep \"" + reqId + "\" | grep \"Target URL for grafana mapping to merchant center \"";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        System.out.println("logs are--"+mappingServiceLogs);
        if(mappingServiceLogs=="" || mappingServiceLogs==null){
            Assert.fail(" Please Check Logs Manually For This Api For Fallback Confirmation");
        }
    }



    public void verifyContractBasicsDetailsResponse(JsonPath withDrawJson1){
        System.out.println("contractBasics List Size is ");
        int s=withDrawJson1.getList("response.contractBasics").size();
        System.out.println("contractBasics List Size is "+s);
        for(int i=0;i<s;i++){
            Assert.assertNotNull(withDrawJson1.getString("response.contractBasics["+ i +"].contractId"));
            Assert.assertNotNull(withDrawJson1.getString("response.contractBasics["+ i +"].merchantId"));
            Assert.assertNotNull(withDrawJson1.getString("response.contractBasics["+ i +"].contractStatus"));
            Assert.assertNotNull(withDrawJson1.getString("response.contractBasics["+ i +"].effectType"));
        }

    }

    public void verifyMerchantInfoList(JsonPath withDrawJson1){
        int s=withDrawJson1.getList("merchantInfoList").size();
        System.out.println("merchantInfoList List Size is "+s);
        for(int i=0;i<s;i++){
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].paytmId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].alipayId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].officialName"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].industryTypeId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].merchantType"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].entityId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].businessName"));
            String pg2DirectOnboarding=withDrawJson1.getString("merchantInfoList["+ i +"].pg2DirectOnboarding");
        }

    }

    public void verifyContractBasics(List<JSONObject> actualResponse, JsonPath withDrawJson1) {
        int s=actualResponse.size();
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("contractId");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("productCode");
        ExpectedResponse.add("productName");
        ExpectedResponse.add("productVersion");
        ExpectedResponse.add("signedTime");
        ExpectedResponse.add("effectTime");
        ExpectedResponse.add("expiryTime");
        ExpectedResponse.add("lastModifier");
        ExpectedResponse.add("contractStatus");
        ExpectedResponse.add("effectType");
        ExpectedResponse.add("expiryType");
        ExpectedResponse.add("memo");
        ExpectedResponse.add("externalContractId");
        ExpectedResponse.add("createdTime");
        ExpectedResponse.add("modifiedTime");
        ExpectedResponse.add("transId");
        System.out.println("Actual Api Response " + actualResponse);
        for(int i=0;i<s;i++){
            Assert.assertNotNull(withDrawJson1.getString("resultResp.contractBasics["+ i +"].contractId"));
            Assert.assertNotNull(withDrawJson1.getString("resultResp.contractBasics["+ i +"].merchantId"));
            Assert.assertNotNull(withDrawJson1.getString("resultResp.contractBasics["+ i +"].contractStatus"));
            Assert.assertNotNull(withDrawJson1.getString("resultResp.contractBasics["+ i +"].effectType"));
        }


    }



    public void verifyCommonV1GetContractPaymentInfo(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }



    public void verifyQueryContractItem(Map<String, String> actualResponse,  Map<String, String> actualResponseinfo) {
        HashSet<String> ExpectedResponse = new HashSet<>();
        HashSet<String> ActualResponse = new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        HashSet<String>ExpectedinfoResponse=new HashSet<>();
        HashSet<String>ActualResponseinfo=new HashSet<>();
        ExpectedinfoResponse.add("resultInfo");
        ExpectedinfoResponse.add("totalPage");
        ExpectedinfoResponse.add("contractBasics");
        Iterator<String> Extkeys = actualResponseinfo.keySet().iterator();
        while (Extkeys.hasNext()) {
            String key1 = Extkeys.next();
            ActualResponseinfo.add(key1);
        }
        System.out.println("Actual result of Response " + ActualResponseinfo);
        if (!ExpectedinfoResponse.equals(ActualResponseinfo)) {
            System.out.println("Expected extendinfo Api Response " + ExpectedinfoResponse );
            Assert.fail("Response Info Schema is not matched");
        }
    }
    public void verifyContractBasicsResponseJson(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("contractId");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("productCode");
        ExpectedResponse.add("productName");
        ExpectedResponse.add("productVersion");
        ExpectedResponse.add("signedTime");
        ExpectedResponse.add("effectTime");
        ExpectedResponse.add("expiryTime");
        ExpectedResponse.add("lastModifier");
        ExpectedResponse.add("contractStatus");
        ExpectedResponse.add("effectType");
        ExpectedResponse.add("expiryType");
        ExpectedResponse.add("memo");
        ExpectedResponse.add("externalContractId");
        ExpectedResponse.add("createdTime");
        ExpectedResponse.add("modifiedTime");
        ExpectedResponse.add("transId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public  String fetchRetryCountValueFromDB(String mid) throws InterruptedException {
        Thread.sleep(30000);
        String DBQuery="SELECT * FROM ENTITY_INFO ei where MID= '"+mid+"';";
        String number_of_retry= DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "NUMBER_OF_RETRY");
        System.out.println("No of Retry are "+number_of_retry);
        return number_of_retry;
    }

    public  int fetchAcquiringConfigInfosSize(String entityId) throws InterruptedException {
        Thread.sleep(30000);
        String DBQuery="SELECT * FROM ENTITY_CHANNEL_INFO eci where ENTITY_ID= '"+entityId+"';";
        List<Map<String, Object>> resultList= DbQueriesUtil.selectFromPaytmPGDB(DBQuery);
        System.out.println("record is "+resultList);
        return resultList.size();
    }
   public void verifyWrongMidResponseInLogs() throws InterruptedException {
       Thread.sleep(30000);
       String mid="ABCD";
       String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service.log  | " +
               "grep \"" + mid + "\"";
       String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
       System.out.println("Wrong mid logs are--"+mappingServiceLogs);
       Assertions.assertThat(mappingServiceLogs).contains("Routing the request to P+ System for this merchantId in case of error from PG2  ABCD and exception failure");
   }

   public void verifyOnpaytmFalgValueWithDB(String entityId,String ONPAYTM_ACTUAL) throws InterruptedException {
       String DBQuery="select * from ENTITY_DEMOGRAPHICS where ENTITY_ID= '"+entityId+"';";
       String sub_category= DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "SUB_CATEGORY");
       System.out.println("SUB_CATEGORY VALUE IN DB "+ sub_category);
       HashMap<String,String>expectedSubCategory=new HashMap<>();
       expectedSubCategory.put("Recharge","true");
       expectedSubCategory.put("Marketplace","true");
       expectedSubCategory.put("Wallet Add Money","true");
       expectedSubCategory.put("P2B Wallet","true");
       expectedSubCategory.put("Paytm Education","true");
       expectedSubCategory.put("Paytm Utility","true");
       expectedSubCategory.put("Paytm BFSI","true");
       expectedSubCategory.put("Finance Team","true");
       expectedSubCategory.put("Paytm Transport","true");
       String ONPAYTM="false";
       if(expectedSubCategory.containsKey(sub_category)){
           ONPAYTM="true";
       }
       Assert.assertEquals(ONPAYTM,ONPAYTM_ACTUAL);
   }


    public void VerifyBankmasterdetailsPaymode(Map<String, String> actualResponse) {
        int s=actualResponse.size();
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("bankMasterDetailsList");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyBankMasterDetailsList(JsonPath withDrawJson1){
        System.out.println("BankMasterDetailsList List Size is ");
        int s=withDrawJson1.getList("bankMasterDetailsList").size();
        System.out.println("BankMasterDetailsList List Size is "+s);
        for(int i=0;i<s;i++){
            Assert.assertNotNull(withDrawJson1.getString("bankMasterDetailsList["+ i +"].bankId"));
            Assert.assertNotNull(withDrawJson1.getString("bankMasterDetailsList["+ i +"].bankCode"));
            Assert.assertNotNull(withDrawJson1.getString("bankMasterDetailsList["+ i +"].bankName"));
            Assert.assertEquals(withDrawJson1.getString("bankMasterDetailsList["+ i +"].payMode"), "NB");
        }

    }

    public void VerifyBankdetailsAlipaycode(Map<String, String> actualResponse) {
        int s=actualResponse.size();
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankId");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("extIfscCode");
        ExpectedResponse.add("bankDisplayName");
        ExpectedResponse.add("bankKey");
        ExpectedResponse.add("oldpgBankCode");
        ExpectedResponse.add("bankWebLogo");
        ExpectedResponse.add("bankWapLogo");
        ExpectedResponse.add("status");
        ExpectedResponse.add("bankMandate");
        ExpectedResponse.add("standardBankCode");
        ExpectedResponse.add("mandateNetBanking");
        ExpectedResponse.add("mandateDebitCard");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("displayOrder");
        ExpectedResponse.add("extendedInfo");
        ExpectedResponse.add("bankShortName");
        //ExpectedResponse.add("oldpgBankCode");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetBanksDetailslistfromCodes(Map<String, String> actualResponse) {
        int s=actualResponse.size();
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("bankIds");
        ExpectedResponse.add("bankMasterDetailsList");
        ExpectedResponse.add("notFound");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyGetResponsecodedetailsPaytmResponseCode(Map<String, String> actualResponse) {
        int s=actualResponse.size();
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("paytmResponseCode");
        ExpectedResponse.add("resultCodeId");
        ExpectedResponse.add("resultCode");
        ExpectedResponse.add("resultStatus");
        ExpectedResponse.add("remark");
        ExpectedResponse.add("retryPossible");
        ExpectedResponse.add("displayMessage");
        ExpectedResponse.add("messageAndRetryDetails");
        ExpectedResponse.add("subPaytmResponseCode");
        ExpectedResponse.add("responseCode");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }


    public void VerifyGetBankdetailsBankCode(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankId");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("extIfscCode");
        ExpectedResponse.add("bankDisplayName");
        ExpectedResponse.add("bankKey");
        ExpectedResponse.add("oldpgBankCode");
        ExpectedResponse.add("bankWebLogo");
        ExpectedResponse.add("bankWapLogo");
        ExpectedResponse.add("status");
        ExpectedResponse.add("bankMandate");
        ExpectedResponse.add("standardBankCode");
        ExpectedResponse.add("mandateNetBanking");
        ExpectedResponse.add("mandateDebitCard");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("displayOrder");
        ExpectedResponse.add("extendedInfo");
        ExpectedResponse.add("bankShortName");
       // ExpectedResponse.add("oldpgBankCode");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }


    public void VerifyGetResponsecodedetailsResultcode(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("paytmResponseCode");
        ExpectedResponse.add("resultCodeId");
        ExpectedResponse.add("resultCode");
        ExpectedResponse.add("resultStatus");
        ExpectedResponse.add("remark");
        ExpectedResponse.add("retryPossible");
        ExpectedResponse.add("displayMessage");
        ExpectedResponse.add("messageAndRetryDetails");
        ExpectedResponse.add("subPaytmResponseCode");
        ExpectedResponse.add("responseCode");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetNotificationTemplate(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("notificationTemplateInfos");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyUserV1TypePaytmId(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetMerchantlogoinfoV2Mid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantBusinessName");
        ExpectedResponse.add("merchantDisplayName");
        ExpectedResponse.add("merchantImageName");
        ExpectedResponse.add("paytmMid");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyCommonV1Get(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("signature");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyMerchantAgentGetAgentInfoIdType(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("parentMid");
        ExpectedResponse.add("childMid");
        ExpectedResponse.add("agentId");
        ExpectedResponse.add("status");
        ExpectedResponse.add("extendedInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetGlobalConfigACQUIRER_CURRENCY_IICPC1IN(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyNotificationFetchTemplateConfiguration(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetLimitMerchantTypePPI_LIMIT_1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantLimits");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetPspSchema(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyDccSupportedCurrencyListAcquirerIICTC1IN(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("acquirer");
        ExpectedResponse.add("currencies");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetBankresponsecodesbankCodepayModeService(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("service");
        ExpectedResponse.add("bankResponseCodes");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyBankresponsecodesSchema(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("service");
        ExpectedResponse.add("bankResponseCode");
        ExpectedResponse.add("paytmResponseCode");
        ExpectedResponse.add("statusType");
        ExpectedResponse.add("bankMessage");
        ExpectedResponse.add("description");
        ExpectedResponse.add("platformResponseCode");
        ExpectedResponse.add("matchExpression");
        ExpectedResponse.add("isActive");
        ExpectedResponse.add("createdOn");
        ExpectedResponse.add("updatedOn");
        ExpectedResponse.add("maxRetryCount");
        ExpectedResponse.add("retryDelay");
        ExpectedResponse.add("retryService");
        ExpectedResponse.add("extensionInfo");
        ExpectedResponse.add("retryDetails");
        ExpectedResponse.add("retriable");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetMerchantStaticConfigMid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetEmiOnDcEligibilityBy(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("emiOnDcEnable");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyDeviceDetailsTidtid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("data");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyDataOfDeviceDetailsTid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("tid");
        ExpectedResponse.add("mid");
        ExpectedResponse.add("monthlyRental");
        ExpectedResponse.add("serialNo");
        ExpectedResponse.add("tmsTid");
        ExpectedResponse.add("modelName");
        ExpectedResponse.add("vendorName");
        ExpectedResponse.add("bankTid");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankZdk");
        ExpectedResponse.add("bankZpk");
        ExpectedResponse.add("hsmZdk");
        ExpectedResponse.add("hsmZpk");
        ExpectedResponse.add("instrument");
        ExpectedResponse.add("bankStatus");
        ExpectedResponse.add("bankStatusMsg");
        ExpectedResponse.add("tmsStatus");
        ExpectedResponse.add("tmsStatusMsg");
        ExpectedResponse.add("terminalStatus");
        ExpectedResponse.add("terminalStatusMsg");
        ExpectedResponse.add("isUsed");
        ExpectedResponse.add("createdDate");
        ExpectedResponse.add("modifiedDate");
        ExpectedResponse.add("merchantName");
        ExpectedResponse.add("merchantDisplayName");
        ExpectedResponse.add("bankMid");
        ExpectedResponse.add("merchantCategory");
        ExpectedResponse.add("merchantSubCategory");
        ExpectedResponse.add("terminalLatitude");
        ExpectedResponse.add("terminalLongitude");
        ExpectedResponse.add("addressOne");
        ExpectedResponse.add("addressTwo");
        ExpectedResponse.add("industryType");
        ExpectedResponse.add("addressThree");
        ExpectedResponse.add("city");
        ExpectedResponse.add("stateName");
        ExpectedResponse.add("countryName");
        ExpectedResponse.add("zipcode");
        ExpectedResponse.add("mccCode");
        ExpectedResponse.add("mccCardOverride");
        ExpectedResponse.add("kybId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyDeviceDetailsV2Tid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyResponseOfDeviceDetailsV2Tid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("tid");
        ExpectedResponse.add("mid");
        ExpectedResponse.add("monthlyRental");
        ExpectedResponse.add("serialNo");
        ExpectedResponse.add("tmsTid");
        ExpectedResponse.add("modelName");
        ExpectedResponse.add("vendorName");
        ExpectedResponse.add("tmsStatus");
        ExpectedResponse.add("tmsStatusMsg");
        ExpectedResponse.add("terminalStatus");
        ExpectedResponse.add("terminalStatusMsg");
        ExpectedResponse.add("isUsed");
        ExpectedResponse.add("createdDate");
        ExpectedResponse.add("modifiedDate");
        ExpectedResponse.add("merchantName");
        ExpectedResponse.add("merchantCategory");
        ExpectedResponse.add("merchantSubCategory");
        ExpectedResponse.add("terminalLatitude");
        ExpectedResponse.add("terminalLongitude");
        ExpectedResponse.add("addressOne");
        ExpectedResponse.add("addressTwo");
        ExpectedResponse.add("industryType");
        ExpectedResponse.add("addressThree");
        ExpectedResponse.add("city");
        ExpectedResponse.add("stateName");
        ExpectedResponse.add("countryName");
        ExpectedResponse.add("zipcode");
        ExpectedResponse.add("kybId");
        ExpectedResponse.add("ecrCallBackUrl");
        ExpectedResponse.add("instrument");
        //ExpectedResponse.add("mccCardOverride");

        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyDeviceDetailsTidBankname(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyResponseOfDeviceDetailsTidBankname(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("tid");
        ExpectedResponse.add("mid");
        ExpectedResponse.add("monthlyRental");
        ExpectedResponse.add("serialNo");
        ExpectedResponse.add("tmsTid");
        ExpectedResponse.add("modelName");
        ExpectedResponse.add("vendorName");
        ExpectedResponse.add("tmsStatus");
        ExpectedResponse.add("tmsStatusMsg");
        ExpectedResponse.add("terminalStatus");
        ExpectedResponse.add("terminalStatusMsg");
        ExpectedResponse.add("isUsed");
        ExpectedResponse.add("createdDate");
        ExpectedResponse.add("modifiedDate");
        ExpectedResponse.add("merchantName");
        ExpectedResponse.add("merchantCategory");
        ExpectedResponse.add("merchantSubCategory");
        ExpectedResponse.add("terminalLatitude");
        ExpectedResponse.add("terminalLongitude");
        ExpectedResponse.add("addressOne");
        ExpectedResponse.add("addressTwo");
        ExpectedResponse.add("industryType");
        ExpectedResponse.add("addressThree");
        ExpectedResponse.add("city");
        ExpectedResponse.add("stateName");
        ExpectedResponse.add("countryName");
        ExpectedResponse.add("zipcode");
        ExpectedResponse.add("kybId");
        ExpectedResponse.add("ecrCallBackUrl");
        ExpectedResponse.add("bankTid");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankZdk");
        ExpectedResponse.add("bankZpk");
        ExpectedResponse.add("hsmZdk");
        ExpectedResponse.add("hsmZpk");
        ExpectedResponse.add("bankStatus");
        ExpectedResponse.add("bankStatusMsg");
        ExpectedResponse.add("bankMid");
        ExpectedResponse.add("mccCode");
        ExpectedResponse.add("merchantIndustryType");
        ExpectedResponse.add("edcIndustryType");
        ExpectedResponse.add("instrument");
        ExpectedResponse.add("mccCardOverride");



        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetFormatterDetails(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("payMethod");
        ExpectedResponse.add("formatterName");
        ExpectedResponse.add("status");
        ExpectedResponse.add("params");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void Verifyusergetusermid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("mid");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyEntityurlinfoformid(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("postBackurl");
        ExpectedResponse.add("urlTypeId");
        ExpectedResponse.add("status");
        ExpectedResponse.add("comments");
        ExpectedResponse.add("createdOn");
        ExpectedResponse.add("modifiedOn");
        ExpectedResponse.add("websiteName");
        ExpectedResponse.add("requestName");
        ExpectedResponse.add("notificationStatusUrl");
        ExpectedResponse.add("imageName");
        ExpectedResponse.add("imageData");
        ExpectedResponse.add("mid");
        ExpectedResponse.add("refundUrl");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetCardnetworkdetails(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("cardNetworkDetailsList");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetPaymethoddetails(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("payMethodDetailsList");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void Verifyfetchlogo(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultResp");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void Verifymerchantidmap(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("oldpgId");
        ExpectedResponse.add("guid");
        ExpectedResponse.add("ssoId");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("oldpgWalletId");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("entityId");
        ExpectedResponse.add("pg2OnboardedMerchant");
        ExpectedResponse.add("businessName");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void Verifyfetchentityignoreparams(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paramsList");
        ExpectedResponse.add("successfullyProcessed");
        ExpectedResponse.add("entityId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }


    public void verifyGetMerchantResponsecodedetailsPaytmResponseCode(Map<String, String> actualResponse) {
        int s=actualResponse.size();
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("resultInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantAttributeKeywithmidandidtype(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("aesKey");
        ExpectedResponse.add("userKey");
        ExpectedResponse.add("sharedSecret");
        ExpectedResponse.add("utilCode");
        ExpectedResponse.add("catCode");
        ExpectedResponse.add("name");
        ExpectedResponse.add("catDesc");
        ExpectedResponse.add("clientId");
        ExpectedResponse.add("merchantAccRef");
        ExpectedResponse.add("subUserEmail");
        ExpectedResponse.add("corpConfigId");
        ExpectedResponse.add("clientSecret");


        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantAttributeKeywithmididtypeandpaymode(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantKeys");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }



// Audit L2 Apis

    public void verifyMerchantV3Api(Map<String, String> actualResponse,JsonPath withDrawJson1 ) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantGetExtendedInfoV3(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("extendedInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyGetPaytmidV1OldpgId(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("oldpgId");
        ExpectedResponse.add("oldpgWalletId");
        ExpectedResponse.add("contractPayload");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("businessName");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("entityId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantLogoInfoV1OldpgId(Map<String, String> actualResponse,JsonPath withDrawJson1) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("merchantBusinessName");
        ExpectedResponse.add("merchantDisplayName");
        ExpectedResponse.add("merchantImageName");
        ExpectedResponse.add("paytmMid");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetBankdetailsBankCodev1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankId");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("extIfscCode");
        ExpectedResponse.add("bankDisplayName");
        ExpectedResponse.add("bankKey");
        ExpectedResponse.add("oldpgBankCode");
        ExpectedResponse.add("bankWebLogo");
        ExpectedResponse.add("bankWapLogo");
        ExpectedResponse.add("status");
        ExpectedResponse.add("bankMandate");
        ExpectedResponse.add("standardBankCode");
        ExpectedResponse.add("mandateNetBanking");
        ExpectedResponse.add("mandateDebitCard");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("displayOrder");
        ExpectedResponse.add("extendedInfo");
        ExpectedResponse.add("bankShortName");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void Verifymerchantidmapv3(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("oldpgId");
        ExpectedResponse.add("guid");
        ExpectedResponse.add("ssoId");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("oldpgWalletId");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("entityId");
        ExpectedResponse.add("pg2OnboardedMerchant");
        ExpectedResponse.add("businessName");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetBankdetailsUserIdOld(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankId");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("extIfscCode");
        ExpectedResponse.add("bankDisplayName");
        ExpectedResponse.add("bankKey");
        ExpectedResponse.add("alipayBankCode");
        ExpectedResponse.add("bankWebLogo");
        ExpectedResponse.add("bankWapLogo");
        ExpectedResponse.add("status");
        ExpectedResponse.add("bankMandate");
        ExpectedResponse.add("standardBankCode");
        ExpectedResponse.add("mandateNetBanking");
        ExpectedResponse.add("mandateDebitCard");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("displayOrder");
        ExpectedResponse.add("extendedInfo");
        ExpectedResponse.add("bankShortName");
        ExpectedResponse.add("oldpgBankCode");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetBankdetailsUserIdONewv1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankId");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("extIfscCode");
        ExpectedResponse.add("bankDisplayName");
        ExpectedResponse.add("bankKey");
        ExpectedResponse.add("oldpgBankCode");
        ExpectedResponse.add("bankWebLogo");
        ExpectedResponse.add("bankWapLogo");
        ExpectedResponse.add("status");
        ExpectedResponse.add("bankMandate");
        ExpectedResponse.add("standardBankCode");
        ExpectedResponse.add("mandateNetBanking");
        ExpectedResponse.add("mandateDebitCard");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("displayOrder");
        ExpectedResponse.add("extendedInfo");
        ExpectedResponse.add("bankShortName");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyGetVendorSplitDetailsV3(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyQueryMerchantExtendedInfoV3(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("extendedInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyUserGetMerchantExtendedInfoV3(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("extendedInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyMerchantInfoListV3(JsonPath withDrawJson1){
        int s=withDrawJson1.getList("merchantInfoList").size();
        System.out.println("merchantInfoList List Size is "+s);
        for(int i=0;i<s;i++){
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].paytmId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].oldpgId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].officialName"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].industryTypeId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].merchantType"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].entityId"));
            Assert.assertNotNull(withDrawJson1.getString("merchantInfoList["+ i +"].businessName"));
            Assert.assertNull(withDrawJson1.getString("merchantInfoList["+ i +"].pg2OnboardedMerchant"));
        }

    }

    public void VerifyMerchantGetExtendedInfoV4(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("merchantId");
        ExpectedResponse.add("extendedInfo");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void UserV3(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void MappingV3(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("paytmResultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void GetBanksdetailslistfromidsV1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("bankIds");
        ExpectedResponse.add("bankMasterDetailsList");
        ExpectedResponse.add("notFound");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void GetV1Bankmasterdetails(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("bankMasterDetailsList");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void GetBankdetailsOldpgcode(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("bankId");
        ExpectedResponse.add("bankName");
        ExpectedResponse.add("bankCode");
        ExpectedResponse.add("extIfscCode");
        ExpectedResponse.add("bankDisplayName");
        ExpectedResponse.add("bankKey");
        ExpectedResponse.add("oldpgBankCode");
        ExpectedResponse.add("bankWebLogo");
        ExpectedResponse.add("bankWapLogo");
        ExpectedResponse.add("status");
        ExpectedResponse.add("bankMandate");
        ExpectedResponse.add("standardBankCode");
        ExpectedResponse.add("mandateNetBanking");
        ExpectedResponse.add("mandateDebitCard");
        ExpectedResponse.add("payMode");
        ExpectedResponse.add("displayOrder");
        ExpectedResponse.add("extendedInfo");
        ExpectedResponse.add("bankShortName");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void VerifyCommonV1GetMerchant(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("response");
        ExpectedResponse.add("signature");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void GetMerchantdataPaytmidV1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("oldpgId");
        ExpectedResponse.add("oldpgWalletId");
        ExpectedResponse.add("contractPayload");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("businessName");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("entityId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void GetMerchantdataNameV1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("paytmId");
        ExpectedResponse.add("paytmWalletId");
        ExpectedResponse.add("oldpgId");
        ExpectedResponse.add("oldpgWalletId");
        ExpectedResponse.add("contractPayload");
        ExpectedResponse.add("officialName");
        ExpectedResponse.add("industryTypeId");
        ExpectedResponse.add("businessName");
        ExpectedResponse.add("merchantType");
        ExpectedResponse.add("entityId");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void GetLookupfromidV1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("id");
        ExpectedResponse.add("name");
        ExpectedResponse.add("category");
        ExpectedResponse.add("oldpgCode");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }
    public void VerifyQueryMerchantMigrationDetailsV1(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("MERCHANT-EMI-INFO");
        ExpectedResponse.add("MERCHANT-EXTENDED-INFO");
        ExpectedResponse.add("CONTRACT-AVALABLE");
        //ExpectedResponse.add("CONTRACT-DETAIL-307035146265847808");
        ExpectedResponse.add("MERCHANT-MAPPING-INFO");
        ExpectedResponse.add("MERCHANT-PREFERENCE-INFO");
        ExpectedResponse.add("CONTRACT-DETAIL-2023021551170110016800438447404");
        ExpectedResponse.add("CONTRACT-DETAIL-402294692098192384");
        ExpectedResponse.add("MERCHANT-ACQUIRING-INFO");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void NewQueryMerchantMigrationContractDetails(Map<String, String> actualResponse) {
        HashSet<String>ExpectedResponse=new HashSet<>();
        HashSet<String>ActualResponse=new HashSet<>();
        ExpectedResponse.add("MERCHANT-EMI-INFO");
        ExpectedResponse.add("MERCHANT-EXTENDED-INFO");
        ExpectedResponse.add("CONTRACT-AVALABLE");
        ExpectedResponse.add("CONTRACT-DETAIL-307035146265847808");
        ExpectedResponse.add("MERCHANT-MAPPING-INFO");
        ExpectedResponse.add("MERCHANT-PREFERENCE-INFO");
        ExpectedResponse.add("CONTRACT-DETAIL-2023022251170110016800442808902");
        ExpectedResponse.add("CONTRACT-DETAIL-308083347480767488");
        ExpectedResponse.add("MERCHANT-ACQUIRING-INFO");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
    }

    public void verifyEntityEdcChannelInfo(Map<String, String> actualResponse, JsonPath withDrawJson1) {
        HashSet<String> ExpectedResponse = new HashSet<>();
        HashSet<String> ActualResponse = new HashSet<>();
        ExpectedResponse.add("resultInfo");
        ExpectedResponse.add("response");
        Iterator<String> keys = actualResponse.keySet().iterator();
        System.out.println("Actual Api Response " + actualResponse);
        while (keys.hasNext()) {
            String key = keys.next();
            ActualResponse.add(key);
        }
        if (!ExpectedResponse.equals(ActualResponse)) {
            Assert.fail("Api Response Schema is not matched");
        }
        if (!withDrawJson1.getString("response.entityEdcChannelInfos").equals("")) {
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.id"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.mid"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.tid"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelInfoId"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.mbid"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.fileType"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.extMid"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.status"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.businessName"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.legalName"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.pgMid"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.merchantKey"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.registrationDate"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.meCode"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.tid"));
            Assert.assertNotNull(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.vpa"));

        }
    }
}
