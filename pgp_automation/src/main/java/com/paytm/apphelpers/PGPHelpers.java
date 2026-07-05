package com.paytm.apphelpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.billproxy.CardTokenizeCardNumberV1API;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.GetEMIDetails;
import com.paytm.api.ppbl.Ppbl;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.MerchantManager;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.getEMIDetails.request.GetEMIDetailsRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.createUPILink.CreateUpiLinkResponse;
import com.paytm.dto.createUPILink.response.Body;
import com.paytm.dto.masterRefund.MasterRefundBody;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.saveCard.BinDetails;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.framework.api.CustomRequestSpecBuilder;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CheckoutJsCheckoutMerchantElementPage;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.pg.crypto.CryptoUtils;
import com.paytm.pg.crypto.Encryption;
import com.paytm.pg.crypto.EncryptionFactory;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.api.MappingService.GetMerchPreferenceInfo;
import com.paytm.utils.merchant.api.MappingService.GetMerchantExtendedInfo;
import com.paytm.utils.merchant.dto.bo.PGPlusBODTO;
import com.paytm.utils.merchant.dto.cachecardtoken.request.Name;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.MerchantEMI;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.MerchantPrefInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.merchantPrefDTO.MerchantPreferenceInfos;
import com.paytm.utils.merchant.dto.refund.ExtendInfo;
import com.paytm.utils.merchant.dto.refund.SubWalletAmount;
import com.paytm.utils.merchant.helpers.GetMerchantHelper;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import com.paytm.utils.merchant.util.PGPUtil;
import com.paytm.utils.merchant.util.addMoneyExpressEncryp.Helpher;
import com.paytm.utils.merchant.util.exception.pgpException.NoResultFoundException;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.apache.commons.codec.binary.Base64;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.json.simple.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.JavascriptExecutor;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Optional;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.paytm.LocalConfig.JWT_CLIENT_SECRET_COFT_THEIA;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

import static com.paytm.appconstants.Constants.PGPAPIResourcePath;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.framework.reporting.Reporter.report;

public class PGPHelpers {

    public static void pause(int seconds)
    {
            report.info("Pause for [" + seconds + "] seconds", new Object[0]);

            try {
                Thread.sleep((long)(seconds * 1000));
            } catch (InterruptedException var3) {
                var3.printStackTrace();

        }

    }

    public static String renewSubscription(String orderId, String mid, double txnAmount, String subsId) {
        report.info("Executing Renew Subscription API");
        DriverManager.setCaptureScreenShot(false);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String respMsg = PGPUtil.renewSubscription(LocalConfig.PGP_HOST, orderId, mid, txnAmount, subsId);
        report.info("Returns response message: " + respMsg);
        DriverManager.setCaptureScreenShot(true);
        return respMsg;

    }

    public static String getMerchantKey(String mid) {
        return getMerchantKey(mid, "key");
    }

    public static String getMerchantKey(String mid, String keyType) {
        for (String key : PGPBaseTest.MERCHANT_MAP.keySet()) {
            Map<String, Object> tempMap = PGPBaseTest.MERCHANT_MAP.get(key);
            if (tempMap.containsValue(mid)) {
                return null != tempMap.get(keyType) ? tempMap.get(keyType).toString() : "";
            }
        }
        return MerchantManager.getMerchantKey(mid);
    }


    ///////////////////////////////////////////////////////////

    public static Map<String, Object> getFromALIPAY_USER(String userId) {
        String dbQuery = "select * from oldpg_paytm_user where paytm_id='" + userId + "'";
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }

    public static Map<String, Object> getFromALIPAY_MERCHANT(String merchantId) {
        String dbQuery = "select * from alipay_paytm_merchant where paytm_merchant_id='" + merchantId + "'";
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }


    public static String getCIN(String expiryMonth,String expiryYear,String cardNumber)
    {
        String body= "{\n" +
                "  \"request\": {\n" +
                "    \"body\": {\n" +
                "      \"cardNo\": \""+cardNumber+"\",\n" +
                "      \"cardScheme\": \"\",\n" +
                "      \"cardType\": \"\",\n" +
                "      \"cvv2\": \"220\",\n" +
                "      \"expiryMonth\": \""+expiryMonth+"\",\n" +
                "      \"expiryYear\": \""+expiryYear+"\",\n" +
                "      \"holderMobileNo\": \"86-13312345678\",\n" +
                "      \"holderName\": {\n" +
                "        \"firstName\": \"james\",\n" +
                "        \"lastName\": \"shen\"\n" +
                "      },\n" +
                "      \"instBranchId\": \"\",\n" +
                "      \"instId\": \"\",\n" +
                "      \"instNetworkCode\": \"ISOCARD\",\n" +
                "      \"instNetworkType\": \"ISOCARD\",\n" +
                "      \"otp\": \"135892\"\n" +
                "    },\n" +
                "    \"head\": {\n" +
                "      \"accessToken\": \"234567a\",\n" +
                "      \"clientId\": \"2016030715243903536806\",\n" +
                "      \"clientSecret\": \"ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5\",\n" +
                "      \"function\": \"alipayplus.user.asset.cacheCard\",\n" +
                "      \"reqMsgId\": \"1509959990\",\n" +
                "      \"reqTime\": \"2017-02-24T07:20:01-00:00\",\n" +
                "      \"reserve\": \"\",\n" +
                "      \"version\": \"1.1.5\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"signature\": \"signature string\"\n" +
                "}";

        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.POST);
        api.getRequestSpecBuilder().setContentType(ContentType.JSON);
        api.getRequestSpecBuilder().setBaseUri(LocalConfig.ALIPAY);
        api.getRequestSpecBuilder().setBasePath(Constants.Alipay.CACHE_CARD);
        api.getRequestSpecBuilder().setBody(body);
        Assertions.assertThat(api.execute().jsonPath().getString("response.body.resultInfo.resultCode")).as("Cache card API not success").isEqualTo("SUCCESS");
        String cardIndexNo = api.execute().jsonPath().getString("response.body.cardIndexNo");
        return cardIndexNo;
    }

    public static String saveCardAtAlipayUserBind(String userId, String expiryMonth, String expiryYear, String cardNumber)
    {
            String alipayUserId = getFromALIPAY_USER(userId).get("oldpg_id").toString();
            String cardIndexNumber = getCIN(expiryMonth,expiryYear,cardNumber);

        String body = "{\n" +
                "    \"request\": {\n" +
                "        \"head\": {\n" +
                "            \"version\": \"fixed-a\",\n" +
                "            \"function\": \"alipayplus.user.asset.bindAsset\",\n" +
                "            \"clientId\": \"2016030715243903536806\",\n" +
                "            \"reqMsgId\": \"f5842b4e567f4a3fabebfdf6e559db97new11novalocal\",\n" +
                "            \"reqTime\": \"2020-02-19T14:40:45+05:30\",\n" +
                "            \"clientSecret\": \"ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5\"\n" +
                "        },\n" +
                "        \"body\": {\n" +
                "            \"userId\": \""+alipayUserId+"\",\n" +
                "            \"instNetworkType\": \"ISOCARD\",\n" +
                "            \"cardIndexNo\": \""+cardIndexNumber+"\",\n" +
                "            \"bizType\": \"PAYMENT_ASSET\",\n" +
                "            \"lastSuccessfulUsedTime\": \"2020-02-19 09:04:37.000\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"signature\": \"r/SKUhZnxy8gsPW80cXnuYV6jLcRhkmF/JhZJnaO5YwbWikgmSHq/bI2/FRAFz82GnK3EaIcQJDFFwWnGyqrzYUk9NwFNVHbwtUr8oGYEkKyFi26c1/xjgtFQkekKidKosb7QsYXgxMsBlIawElFSkFA6wJAa5U748vajYuCN3xbSE+ucKXIFwIvWHHo7JVaxWS1dZ/gQjvwMP5SYhUjg/mLIVw9WrBqnNsaZyFKExlUkkLqu4hnPtokkLOZvDsQgzhjZDl46AkPkdf2eQEt18Xx+U9m5AS2BJPsDSXELjLdc3NLrKtlcM7W1zJSUmrm4PC7a5Hrrbu66/U80/Y/pw==\"\n" +
                "}";

        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.POST);
        api.getRequestSpecBuilder().setContentType(ContentType.JSON);
        api.getRequestSpecBuilder().setBaseUri(LocalConfig.ALIPAY);
        api.getRequestSpecBuilder().setBasePath(Constants.Alipay.USER_BIND_ASSET);
        api.getRequestSpecBuilder().setBody(body);
        Assertions.assertThat(api.execute().jsonPath().getString("response.body.resultInfo.resultCode")).as("User bind asset API not success").isNotEqualTo("FAILURE");
        return cardIndexNumber;
    }

    public static String saveCardAtAlipayMerchantBind(String mid,String custId, String expiryMonth, String expiryYear, String cardNumber)
    {
        String cardIndexNumber = getCIN(expiryMonth,expiryYear,cardNumber);
        String alipayPaytmMerchantId = getFromALIPAY_MERCHANT(mid).get("alipay_merchant_id").toString();
        String body = "{\n" +
                "    \"request\": {\n" +
                "        \"head\": {\n" +
                "            \"version\": \"fixed-a\",\n" +
                "            \"function\": \"alipayplus.merchant.asset.bindAsset\",\n" +
                "            \"clientId\": \"2016030715243903536806\",\n" +
                "            \"reqMsgId\": \"60c0517549d2424490934348d98b1426new11novalocal\",\n" +
                "            \"reqTime\": \"2020-02-19T14:41:12+05:30\",\n" +
                "            \"clientSecret\": \"ifUJTbL6DnHwYU2xumZ9EEOmCm75wub5\"\n" +
                "        },\n" +
                "        \"body\": {\n" +
                "            \"merchantId\": \""+alipayPaytmMerchantId+"\",\n" +
                "            \"externalUserId\": \""+custId+"\",\n" +
                "            \"instNetworkType\": \"ISOCARD\",\n" +
                "            \"cardIndexNo\": \""+cardIndexNumber+"\",\n" +
                "            \"bizType\": \"MERCHANT_CUSTOMER_ASSET\",\n" +
                "            \"lastSuccessfulUsedTime\": \"2020-02-19 09:04:40.000\"\n" +
                "        }\n" +
                "    },\n" +
                "    \"signature\": \"nHvJjAFaQXh3NsYR9fTpRkpv6afO3q0DoZRVmzgJee9wkqw2BcQFl45kS9RyYO6/+4WmE67wghFQ4K4FNuJG/QAxbPALOR14lKy9L8hrzhgJiOTNyA2TIaAnftADvSWVoeK618huJj1uG5WEF91vdxctRZ+luQNgl9Ma68fHEpA26Yl7bAXzmBnkVINs8PGPsc2akn3+raOGsAtKpxPBW2kg7PkaUiWfsqH/sTk87HOMRwCGSlbvumhR9CjTuW3XpSGybH5WIXlRPfvfpu3reCNF0YQTThVOmW9PSAKIpfIuNHkoDxoK3j2EyxujWG78f3x/0o4hvY0jSwkg+J0Q8Q==\"\n" +
                "}";

        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.POST);

        api.getRequestSpecBuilder().setContentType(ContentType.JSON);
        api.getRequestSpecBuilder().setBaseUri(LocalConfig.ALIPAY);
        api.getRequestSpecBuilder().setBasePath(Constants.Alipay.MERCHANT_BIND_ASSET);
        api.getRequestSpecBuilder().setBody(body);
        Response response= api.execute();
        Assertions.assertThat(response.statusCode()).isEqualTo(200);
        Assertions.assertThat(response.jsonPath().getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");

        return cardIndexNumber;

    }

    public static Response getCardsAlipayUserAsset(String userId)
    {
        String alipayUserId = getFromALIPAY_USER(userId).get("oldpg_id").toString();
        List<String> cardIndexNumberList = new ArrayList();
       String body = "{\n" +
               "    \"userId\": \""+alipayUserId+"\",\n" +
               "    \"contactBizType\": \"PAYMENT_ASSET\",\n" +
               "    \"includeExpired\": true\n" +
               "}";

        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.POST);
        api.getRequestSpecBuilder().setContentType(ContentType.JSON);
        api.getRequestSpecBuilder().setBaseUri(LocalConfig.SUPERGW_LITE);
        api.getRequestSpecBuilder().setBasePath(Constants.Alipay.USER_QUERYBYFILTER);
        api.getRequestSpecBuilder().addHeader("Authorization", "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
        api.getRequestSpecBuilder().setBody(body);
        Response response = api.execute();
        Assertions.assertThat(response.jsonPath().getString("status")).as("User Query by filter API not success").isEqualTo("SUCCESS");

        return response;

    }


    public static Response getCardsAlipayMerchantAsset(String mid,String custId) {
        String alipayPaytmMerchantId = getFromALIPAY_MERCHANT(mid).get("alipay_merchant_id").toString();
        String body = "{\n" +
                "    \"merchantId\": \"" + alipayPaytmMerchantId + "\",\n" +
                "    \"externalUserId\": \"" + custId + "\",\n" +
                "    \"contactBizType\": \"MERCHANT_CUSTOMER_ASSET\",\n" +
                "    \"includeExpired\": true\n" +
                "}";

        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.POST);
        api.getRequestSpecBuilder().setContentType(ContentType.JSON);
        api.getRequestSpecBuilder().setBaseUri(LocalConfig.SUPERGW_LITE);
        api.getRequestSpecBuilder().setBasePath(Constants.Alipay.MERCHANT_QUERYBYFILTER);
        api.getRequestSpecBuilder().addHeader("Authorization", "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
        api.getRequestSpecBuilder().setBody(body);
        Response response = api.execute();
        Assertions.assertThat(response.jsonPath().getString("status")).as("User Query by filter API not success").isEqualTo("SUCCESS");

        return response;
    }


    public static void deleteAlipayAssetUser(String userId)
    {
        String alipayUserId = getFromALIPAY_USER(userId).get("oldpg_id").toString();
        List<String> cardIndexNumberList = new ArrayList<>();
        Response userResponse = getCardsAlipayUserAsset(userId);
        List<String> savedCCCIN =  null!=userResponse.jsonPath().getList("assetInfos.CC.cardIndexNo")?userResponse.jsonPath().getList("assetInfos.CC.cardIndexNo") : new ArrayList() ;
        List<String> savedDCCIN = null!=userResponse.jsonPath().getList("assetInfos.DC.cardIndexNo")?userResponse.jsonPath().getList("assetInfos.DC.cardIndexNo") : new ArrayList() ;

        cardIndexNumberList.addAll(savedCCCIN); //All CC saved
        cardIndexNumberList.addAll(savedDCCIN); //All DC saved


        for(String cardIndexNumber :  cardIndexNumberList) {
            String body = "{\n" +
                    "    \"cardIndexNo\": \"" + cardIndexNumber + "\",\n" +
                    "    \"envInfo\": {\n" +
                    "        \"clientIp\": \"127.0.0.1\",\n" +
                    "        \"terminalType\": \"WEB\"\n" +
                    "    },\n" +
                    "    \"userId\": \"" + alipayUserId + "\"\n" +
                    "}";

            BaseApi api = new BaseApiV2();
            api.setMethod(BaseApi.MethodType.POST);
            api.getRequestSpecBuilder().setContentType(ContentType.JSON);
            api.getRequestSpecBuilder().setBaseUri(LocalConfig.SUPERGW_LITE);
            api.getRequestSpecBuilder().setBasePath(Constants.Alipay.DELETE_USER_ASSET);
            report.info("Deleting Card Index Number : " + cardIndexNumber);
            api.getRequestSpecBuilder().setBody(body);
            api.getRequestSpecBuilder().addHeader("Authorization", "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
            Response response = api.execute();
            Assertions.assertThat(response.jsonPath().getString("status")).as("Delete User asset API not success").isNotEqualTo("FAILURE");
        }
    }

    public static void deleteAlipayAssetMerchant(String mid,String custId) {
        String alipayPaytmMerchantId = getFromALIPAY_MERCHANT(mid).get("alipay_merchant_id").toString();
        List<String> cardIndexNumberList = new ArrayList<>();

        Response queryMerchantResponse = getCardsAlipayMerchantAsset(mid,custId);
        List<String> savedCCCIN = null != queryMerchantResponse.jsonPath().getList("assetInfos.CC.cardIndexNo") ? queryMerchantResponse.jsonPath().getList("assetInfos.CC.cardIndexNo") : new ArrayList();
        List<String> savedDCCIN = null != queryMerchantResponse.jsonPath().getList("assetInfos.DC.cardIndexNo") ? queryMerchantResponse.jsonPath().getList("assetInfos.DC.cardIndexNo") : new ArrayList();

        cardIndexNumberList.addAll(savedCCCIN); //All CC saved
        cardIndexNumberList.addAll(savedDCCIN); //All DC saved

        for(String cardIndexNumber :  cardIndexNumberList) {

            String body = "{\n" +
                    "  \"cardIndexNo\":\"" + cardIndexNumber + "\",\n" +
                    "  \"merchantId\":\"" + alipayPaytmMerchantId + "\",\n" +
                    "  \"externalUserId\":\"" + custId + "\",\n" +
                    "  \"envInfo\":{\n" +
                    "    \"terminalType\":\"WEB\"\n" +
                    "  }\n" +
                    "}";

            BaseApi api = new BaseApiV2();
            api.setMethod(BaseApi.MethodType.POST);
            api.getRequestSpecBuilder().setContentType(ContentType.JSON);
            api.getRequestSpecBuilder().setBaseUri(LocalConfig.SUPERGW_LITE);
            api.getRequestSpecBuilder().setBasePath(Constants.Alipay.DELETE_CUSTOMER_ASSET);
            api.getRequestSpecBuilder().setBody(body);
            api.getRequestSpecBuilder().addHeader("Authorization", "Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");

            Response response = api.execute();
            Assertions.assertThat(response.jsonPath().getString("status")).as("Delete merchant asset API not success").isNotEqualTo("FAILURE");

        }
    }


    public static void deleteSavedCardByTokenType(User user, String saveCardId)
    {
        String body = "{\n" +
                "    \"head\":{\n" +
                "        \"tokenType\":\"SSO\"\n" +
                "    },\n" +
                "    \"body\":{\n" +
                "        \"savedCardId\":\""+saveCardId+"\"\n" +
                "    }\n" +
                "}";

        BaseApi api = new BaseApiV2();
        api.setMethod(BaseApi.MethodType.POST);

        api.getRequestSpecBuilder().setContentType(ContentType.JSON);
        api.getRequestSpecBuilder().setBaseUri(com.paytm.utils.merchant.Constants.PGP_HOST);
        api.getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_DELETE_SAVEDCARDBYTOKENTYPE);
        api.getRequestSpecBuilder().setBody(body);
        api.getRequestSpecBuilder().addHeader("token",user.ssoToken());
        Assertions.assertThat(api.execute().jsonPath().getString("response.body.resultInfo.resultCode")).as("Delete Token API not success").isEqualTo("SUCCESS");

    }



    public static TxnStatus validateSuccessTxnStatus(OrderDTO orderDTO, String payMode, String bankName, String gatewayName)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(gatewayName)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(bankName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }


////////////////////////////////////////////////////////////////////////////////////////////////


    public static String executeToFetchStatusInPaymentDetails(String subsId,String orderId)
    {
        for(int i=0; i<6; i++){
            String response = getStatusInPaymentDetails(subsId,orderId);
            if("ACTIVE" == response){
                return response;
            }
            try{
                Thread.sleep(1000);
            }catch (Exception e){
            }
        }
        return getStatusInPaymentDetails(subsId,orderId);
    }


    public static String getStatusInPaymentDetails(String subsId,String orderId)
    {
        Reporter.report.info("<br>Fetching acquirement Id", new Object[0]);
        DriverManager.setCaptureScreenShot(false);
        String query="";
        if(LocalConfig.ENV_NAME=="AUTO") {
             query = "SELECT * FROM subscription_payment_details WHERE subscription_id=" + subsId + " and order_id='" + orderId + "';";
        }
        else{
             query = "SELECT * FROM SUBS_ACC.subscription_payment_details WHERE subscription_id=" + subsId + " and order_id='" + orderId + "';";
        }
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query);
        if (result.size() == 0) {
            DriverManager.setCaptureScreenShot(true);
            return null;
        } else {
            String status = (String)((Map)result.get(0)).get("status");
            Reporter.report.info("<br>Acquirement Id: " + status, new Object[0]);
            DriverManager.setCaptureScreenShot(true);
            return status;
        }

    }

    public static String executeUntilSubsContractNotFound(String getValue,String subsId, String orderId){
        return executeUntilSubsContractNotFound(getValue,LocalConfig.PGP_DB_CONNECTION_URL,subsId,orderId);
    }

    private static String executeUntilSubsContractNotFound(String getValue,String PGPDBUrl, String subsId, String orderId) {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException var7) {
            var7.printStackTrace();
        }
        String fetchedValue = getSubscriptionContractInfo(getValue,PGPDBUrl, subsId, orderId);
        if (fetchedValue == null || fetchedValue.isEmpty()) {
            for(int i = 0; i < 6; ++i) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException var6) {
                    var6.printStackTrace();
                }
                fetchedValue = getSubscriptionContractInfo(getValue,PGPDBUrl, subsId, orderId);
                if (fetchedValue != null && !fetchedValue.isEmpty()) {
                    return fetchedValue;
                }
            }
        }
        return fetchedValue;
    }


    private static String getSubscriptionContractInfo(String getValue,String PGPDBUrl,String subsId, String orderId) {
        Reporter.report.info("Fetching " + getValue + " :");
        DriverManager.setCaptureScreenShot(false);
        String query="";
        if(LocalConfig.ENV_NAME=="AUTO") {
             query = "SELECT * FROM subscription_contract_v2 WHERE subscription_id=" + subsId + " and order_id='" + orderId + "';";
        }
        else{
             query = "SELECT * FROM SUBS_ACC.subscription_contract_v2 WHERE subscription_id=" + subsId + " and order_id='" + orderId + "';";
        }
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl,query);
        if (result.size() == 0) {
            DriverManager.setCaptureScreenShot(true);
            return null;
        } else {
            if(!result.get(0).containsKey(getValue))
                throw new SkipException("Column " + getValue + " not found in subscription_contract_v2 table");

            if(result.get(0).get(getValue) == null)
                return null;

            return result.get(0).getOrDefault(getValue, "").toString();
        }
    }

    /**
     *
     * @param getValue mention column_name
     * @param subsId
     * @param orderId
     * @return
     */
    public static String executeUntilSubsPaymentInfoNotFound(String getValue,String subsId, String orderId){
        return executeUntilSubsPaymentInfoNotFound(getValue,LocalConfig.PGP_DB_CONNECTION_URL,subsId,orderId);
    }

    @Step("executeUntilSubsPaymentInfoNotFound")
    private static String executeUntilSubsPaymentInfoNotFound(String getValue,String PGPDBUrl, String subsId, String orderId) {
        try {
            Thread.sleep(2000L);
        } catch (InterruptedException var7) {
            var7.printStackTrace();
        }
        String fetchedValue = getSubscriptionPaymentInfo(getValue,PGPDBUrl, subsId, orderId);
        if (fetchedValue == null || fetchedValue.isEmpty()||fetchedValue.equals("INIT")) {
            for(int i = 0; i < 24; ++i) {
                try {
                    Thread.sleep(5000L);
                } catch (InterruptedException var6) {
                    var6.printStackTrace();
                }

                fetchedValue = getSubscriptionPaymentInfo(getValue,PGPDBUrl, subsId, orderId);
                if (fetchedValue != null && !fetchedValue.isEmpty()) {
                    return fetchedValue;
                }
            }
        }
        return fetchedValue;
    }


    /**
     *
     * @param getValue column name mentioned in the table
     * @param PGPDBUrl
     * @param subsId
     * @param orderId
     * @return
     */
    private static String getSubscriptionPaymentInfo(String getValue,String PGPDBUrl,String subsId, String orderId) {
        //Reporter.report.info("Fetching " + getValue + " :");
        DriverManager.setCaptureScreenShot(false);
        String query="";
        if(LocalConfig.ENV_NAME=="AUTO") {
             query = "SELECT * FROM subscription_payment_details WHERE subscription_id=" + subsId + " and order_id='" + orderId + "';";
        }
        else{
            query = "SELECT * FROM SUBS_ACC.subscription_payment_details WHERE subscription_id=" + subsId + " and order_id='" + orderId + "';";
        }
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
        if (result.size() == 0) {
            DriverManager.setCaptureScreenShot(true);
            return null;
        } else {
            if(!result.get(0).containsKey(getValue))
                throw new SkipException("Column "+ getValue+ " not found in subscription_payment_details table");

            if(result.get(0).get(getValue) == null)
                return null;

            return result.get(0).getOrDefault(getValue, "").toString();
        }
    }


    @Deprecated
    public static Response refundStatusUntilPending(String mid, String orderId, String refId, String refundAmount, String txnId, String postConvFlag) {
        return PGPUtil.refundStatusUntilPending(LocalConfig.PGP_HOST, mid, orderId, refId, refundAmount, txnId, postConvFlag);
    }

    @Step("Initiate REFUND request")
    public static Response initiateRefundRequest(String mid, String merchantKey, String orderId, String refId, String refundAmount, String txnId, String postConvFlag) throws PGPException {
        return PGPUtil.executeRefund_checksum(LocalConfig.PGP_HOST, mid, merchantKey, orderId, refId, refundAmount, txnId, postConvFlag);
    }


    public static Response executeMasterRefund(String mid, String merchantKey, String orderId, String refId, String refundAmount, String txnId, String postConvFlag) {
        String txnType;
        if (postConvFlag == null || postConvFlag.isEmpty()) {
            txnType = "REFUND";
        } else {
            txnType = postConvFlag;
        }
        MasterRefundBody body = (new MasterRefundBody()).setMid(mid).setOrderId(orderId).setRefId(refId).setTxnId(txnId).setRefundAmount(refundAmount).setTxnType("REFUND");

        TreeMap<String, String> t = new TreeMap<>();
        t.put("MID", mid);
        t.put("ORDERID", orderId);
        t.put("REFID", refId);
        t.put("TXNID", txnId);
        t.put("REFUNDAMOUNT", refundAmount);
        t.put("TXNTYPE", "REFUND");

        String checksum = PGPUtil.getChecksum(merchantKey, t);
        body.setChecksum(checksum);
        MasterRefund masterRefund = new MasterRefund(body);
        return masterRefund.execute();
    }

    public static Response masterRefund_FoodWallet_CheckSum(String mid, String merchantKey, String orderId, String refId, String refundAmount, String txnId, String postConvFlag, SubWalletAmount subWalletDetail) {
        String txnType;
        if (postConvFlag == null || postConvFlag.isEmpty()) {
            txnType = "REFUND";
        } else {
            txnType = postConvFlag;
        }
        MasterRefundBody body = (new MasterRefundBody()).setMid(mid).setOrderId(orderId).setRefId(refId).setTxnId(txnId).setRefundAmount(refundAmount).setTxnType("REFUND").setSubWalletAmount(subWalletDetail);
        String checksum = "";
        String subWalletDetailString = null;
        try {
            subWalletDetailString = (new ObjectMapper()).writeValueAsString(subWalletDetail);
        } catch (JsonProcessingException var21) {
            var21.printStackTrace();
        }
        String checksumString = mid + "|" + orderId + "|" + txnId + "|" + txnType + "|" + refundAmount + "|" + subWalletDetailString.replaceAll("\"", "\\\\\"");
        checksum = PGPUtil.getChecksum(merchantKey, checksumString);
        body.setChecksum(checksum);
        MasterRefund masterRefund = new MasterRefund(body);
        return masterRefund.execute();
    }


    @Step("Initiate ASYNC_REFUND request")
    public static Response initiateAsyncRefund(String mid, String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("") String comments, SubWalletAmount subWalletAmount) throws PGPException {
        return PGPUtil.asyncRefundUntilPending(LocalConfig.PGP_HOST, mid, merchantKey, orderId, refId, txnId, refundAmount, txnType, comments, subWalletAmount);
    }

    /**
     * {@code POST /refund/api/v1/async/refund} with item-level {@code refundItems} (e.g. productId / itemRefundAmount),
     * same contract as curl: head with empty clientId + checksum, body includes refund array. Uses {@link LocalConfig#PGP_HOST}.
     *
     * @param refundItemsJsonArray JSON array string, e.g. {@code [{"itemId":"0","productId":"LS27CG550EW","itemRefundAmount":"200"}]}
     * @param refId                 pass null/blank to auto-generate a new IN-prefixed ref id
     */
    @Step("Initiate ASYNC_REFUND with refundItems (item / product lines)")
    public static Response initiateAsyncRefundWithRefundItemLines(
            Constants.MerchantType merchant,
            String orderId,
            String refId,
            String txnId,
            String refundAmount,
            String refundItemsJsonArray) {
        SyncRefund syncRefund = new SyncRefund();
        return RestAssured.given()
                .spec(syncRefund.reqSpecAsyncRefundWithRefundItemLines(
                        merchant, orderId, txnId, refId, refundAmount, refundItemsJsonArray))
                .post();
    }

    @Step("Initiate ASYNC_REFUND request")
    public static Response initiateAsyncRefundExtendInfo(String mid, String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("") String comments, SubWalletAmount subWalletAmount, ExtendInfo extendInfo) throws PGPException {
        return PGPUtil.asyncRefundWithExtendInfo(LocalConfig.PGP_HOST, mid, merchantKey, orderId, refId, txnId, refundAmount, txnType, comments, subWalletAmount,extendInfo);
    }

    //

    @Step("Initiate ASYNC_REFUND request Phase 2 with JWT")
    public static Response initiateAsyncRefundJWT(String mid, String orderId, String refId, String txnId, String refundAmount, String txnType,Boolean disableMerchantDebitRetry, String comments,String jwtToken) throws PGPException {
        return PGPUtil.asyncRefundJWT(LocalConfig.PGP_HOST, mid, orderId, refId, txnId, refundAmount, txnType,disableMerchantDebitRetry, comments, jwtToken);
    }

    @Step("Initiate ASYNC_REFUND request with aggMID")
    public static Response initiateAsyncRefundWithAggMID(String mid, String aggMID , String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("") String comments, SubWalletAmount subWalletAmount) throws PGPException {
        return PGPUtil.asyncRefundWithAggMidUntilPending(LocalConfig.PGP_HOST, mid,aggMID, merchantKey, orderId, refId, txnId, refundAmount, txnType, comments, subWalletAmount);
    }


    @Step("Initiate ASYNC_REFUND IMPS")
    public static Response initiateAsyncRefundImps(String mid, String merchantKey, String orderId, String refId, String txnId, String refundAmount, @Optional("REFUND") String txnType, @Optional("") String comments,String token,String preferredDestination ) throws PGPException {
        return PGPUtil.asyncRefundIMPSUntilPending(LocalConfig.PGP_HOST, mid, merchantKey, orderId, refId, txnId, refundAmount, txnType, comments,token,preferredDestination);
    }


    @Step("Initiate ASYNC_REFUND request Phase 2 with JWT")
    public static Response initiatesyncRefundJWT(String mid, String orderId, String refId, String txnId, String refundAmount, String txnType,Boolean disableMerchantDebitRetry, String comments,String jwtToken) throws PGPException {
        return PGPUtil.syncRefundJWT(LocalConfig.PGP_HOST, mid, orderId, refId, txnId, refundAmount, txnType,disableMerchantDebitRetry, comments, jwtToken);
    }

    @Step("Fetch BO PANEL ENTRIES")
    public static Response fetchBOEntries(PGPlusBODTO pgPlusBODTO) throws PGPException {
        return PGPUtil.pgPlusBO(LocalConfig.PGP_HOST,pgPlusBODTO);
    }


    @Step("Initiate Cache Card Token request")
    public static Response generateCacheCardToken(String mid, String merchantKey, String vpa, String account, String ifscCode, Name name, String mobNum,String requestId) throws PGPException {
        return PGPUtil.cacheCardToken(LocalConfig.PGP_HOST, mid, merchantKey, vpa, account, ifscCode, name, mobNum, requestId);
    }

    @Step("Initiate Async Refund IMPS request")
    public static Response asyncRefundIMPS(String mid, String merchantKey, String orderId, String refId, String txnId, String refundAmount,@Optional("REFUND") String txnType, @Optional("comments") String comments,String token,String preferredDestination) throws PGPException {
        return PGPUtil.asyncRefundIMPSUntilPending(LocalConfig.PGP_HOST, mid, merchantKey, orderId, refId, txnId, refundAmount, txnType, comments,token,preferredDestination);
    }

    @Step("Get REFUND_STATUS (/HANDLER_INTERNAL/getRefundStatus)")
    public static RefundStatusHelper getRefundStatus(String mid, String merchantKey, String refId, boolean isSecure) throws PGPException {
        return new RefundStatusHelper(mid, merchantKey, refId, isSecure);
    }

    @Step("Get REFUND_STATUS (/refund/api/v1/refundStatus)")
    public static RefundStatusV1Helper getRefundStatusV1(Constants.MerchantType merchantType,
                                                         String orderId, String refId,
                                                         boolean untilPending) throws PGPException {
        return new RefundStatusV1Helper(merchantType, orderId, refId, untilPending);
    }

    @Step("Get REFUND_STATUS (/refund/api/v1/refundStatus) with JWT")
    public static RefundStatusV1Helper getRefundStatusV1JWT(Constants.MerchantType merchantType,
                                                         String orderId, String refId,
                                                         String tokenType, String token,
                                                         boolean untilPending) throws PGPException {
        return new RefundStatusV1Helper(merchantType.getId(),orderId,refId,tokenType,token,untilPending);
    }

    @Step("Get REFUND_STATUS (/refund/api/v1/refundStatus)")
    public static RefundStatusV1Helper getRefundStatusV1(String mid, String orderId, String refId, String merchantKey,
                                                         boolean untilPending) throws PGPException {
        return new RefundStatusV1Helper(mid, orderId, refId, merchantKey, untilPending);
    }

    @Step("Get /merchant-status/getTxnStatus")
    public static TxnStatus getTxnStatus(String mid, String orderId) {
        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        return txnStatus.executeUntilNotPending();
    }

    public static Response getTxnStatusResponse(String mid, String orderId) {
        report.info("Executing Txn Status API");
        DriverManager.setCaptureScreenShot(false);
        Response response = PGPUtil.getTxnStatusResponse(LocalConfig.PGP_HOST, mid, orderId);
        DriverManager.setCaptureScreenShot(true);
        return response;
    }

    @Deprecated
    public static Response getTxnStatusListResponse(String mid, String orderId, String txnType) {
        report.info("Executing Txn Status List API");
        DriverManager.setCaptureScreenShot(false);
        Response response = PGPUtil.getTxnStatusListResponse(LocalConfig.PGP_HOST, mid, orderId, txnType);
        DriverManager.setCaptureScreenShot(true);
        return response;
    }

    public static Response executeTxnStatusList(String mid, String merchantKey, String orderId, String txnType) {
        return PGPUtil.executeTxnStatusList(LocalConfig.PGP_HOST, mid, merchantKey, orderId, txnType);
    }

    public static String getEncryptedPaymentDetails(String merchantKey, String paymentType, PaymentDTO paymentDTO) {
        try {
            switch (paymentType) {
                case "CC":
                case "SC":
                case "CREDIT_CARD":
                case "SAVED_CARD":
                    if (paymentDTO.getSavedCardId() == null) {
                        return PGPUtil.getEncryptedPaymentDetails(merchantKey, paymentDTO.getCreditCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCvvNumber());
                    } else {
                        return PGPUtil.getEncryptedPaymentDetails(merchantKey, paymentDTO.getSavedCardId(), paymentDTO.getCvvNumber());
                    }
                case "DC":
                case "DEBIT_CARD":
                    if (paymentDTO.getSavedCardId() == null) {
                        return PGPUtil.getEncryptedPaymentDetails(merchantKey, paymentDTO.getDebitCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCvvNumber());
                    } else {
                        return PGPUtil.getEncryptedPaymentDetails(merchantKey, paymentDTO.getSavedCardId(), paymentDTO.getCvvNumber());
                    }
                case "NB":
                case "NET_BANKING":
                case "BALANCE":
                case "UPI":
                    return "";
                default:
                    DriverManager.setCaptureScreenShot(true);
                    throw new SkipException("Invalid paymentType passed as parameter");
            }
        } catch (NoResultFoundException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }


    public static String getFormattedPaymentDetails(String paymentType, PaymentDTO paymentDTO) {
        switch (paymentType) {
            case "CC":
            case "SC":
            case "CREDIT_CARD":
            case "ADVANCE_DEPOSIT_ACCOUNT":
            case "SAVED_CARD":
                if (paymentDTO.getSavedCardId() == null) {
                    return PGPUtil.getFormattedPaymentDetails(paymentDTO.getCreditCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCvvNumber());
                } else {
                    return PGPUtil.getFormattedPaymentDetails(paymentDTO.getSavedCardId(), paymentDTO.getCvvNumber());
                }
            case "DC":
            case "DEBIT_CARD":
                if (paymentDTO.getSavedCardId() == null) {
                    return PGPUtil.getFormattedPaymentDetails(paymentDTO.getDebitCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCvvNumber());
                } else {
                    return PGPUtil.getFormattedPaymentDetails(paymentDTO.getSavedCardId(), paymentDTO.getCvvNumber());
                }
            case "NB":
            case "NET_BANKING":
            case "BALANCE":
            case "UPI":
            case "PPBL":
                return "";
            case "EMI":
                if (paymentDTO.getSavedCardId() == null) {
                    return PGPUtil.getFormattedPaymentDetails(paymentDTO.getEmiCard(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCvvNumber());
                } else {
                    return PGPUtil.getFormattedPaymentDetails(paymentDTO.getSavedCardId(), paymentDTO.getCvvNumber());
                }

            default:
                DriverManager.setCaptureScreenShot(true);
                throw new SkipException("Invalid paymentType passed as parameter: " + paymentType);
        }
    }

    public static String getEncryptedPaymentDetailsForExpress(String custId, String mid, String paymentType, PaymentDTO paymentDTO) {
        switch (paymentType) {
            case "CC":
            case "SC":
            case "CREDIT_CARD":
            case "SAVED_CARD":
                if (paymentDTO.getSavedCardId() == null) {
                    return PGPUtil.getEncryptedPaymentDetailsForExpress(LocalConfig.PGP_HOST, paymentDTO.getCreditCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCvvNumber(), custId, mid);
                } else {
                    return PGPUtil.getEncryptedPaymentDetailsForExpress(LocalConfig.PGP_HOST, custId, mid, paymentDTO.getSavedCardId(), paymentDTO.getCvvNumber());
                }
            case "DC":
            case "DEBIT_CARD":
                if (paymentDTO.getSavedCardId() == null) {
                    return PGPUtil.getEncryptedPaymentDetailsForExpress(LocalConfig.PGP_HOST, paymentDTO.getDebitCardNumber(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCvvNumber(), custId, mid);
                } else {
                    return PGPUtil.getEncryptedPaymentDetailsForExpress(LocalConfig.PGP_HOST, custId, mid, paymentDTO.getSavedCardId(), paymentDTO.getCvvNumber());
                }
            case "NB":
            case "NET_BANKING":
            case "BALANCE":
            case "PPI":
            case "UPI":
                return "";
            default:
                throw new SkipException("Invalid paymentType passed as parameter");
        }
    }

    public static String executeUntilAcquirementIdNotFound(String subsId, String orderId) {
        return PGPUtil.executeUntilAcquirementIdNotFound(LocalConfig.PGP_DB_CONNECTION_URL, subsId, orderId);
        // Decreasing time period.
//        for(int i=0; i<6; i++){
//            String res = PGPUtil.executeUntilAcquirementIdNotFound(LocalConfig.PGP_DB_CONNECTION_URL, subsId, orderId);
//            if(null != res){
//                return res;
//            }
//            try{
//                Thread.sleep(1000);
//            }catch (Exception e){
//            }
//        }
//        return PGPUtil.executeUntilAcquirementIdNotFound(LocalConfig.PGP_DB_CONNECTION_URL, subsId, orderId);
    }


    public static String getSavedCardId(String subsId) {
        try {
            return PGPUtil.getSavedCardId(LocalConfig.PGP_DB_CONNECTION_URL, subsId);
        } catch (NoResultFoundException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

    public static String getSubsDate(String subsId) {
        try {
            return PGPUtil.getSubsDate(LocalConfig.PGP_DB_CONNECTION_URL, subsId);
        } catch (NoResultFoundException e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

    public static String getSubsStatus(String orderId) {
        String query="";
        if(LocalConfig.ENV_NAME=="AUTO") {
            query = "SELECT * FROM PGPDB.subscription_contract_v2 scv WHERE order_id = '" + orderId + "' ORDER BY created_date DESC limit 0, 1;";
        }
        else{
            query = "SELECT * FROM SUBS_ACC.subscription_contract_v2 scv WHERE order_id = '" + orderId + "' ORDER BY created_date DESC limit 0, 1;";
        }
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query);
        if (result.size() == 0) {
            throw new NoResultFoundException("Record not found");
        }
        return result.get(0).get("status").toString();
    }

    public static String getSubsMetadata(String orderId) {
        String query="";
        if(LocalConfig.ENV_NAME=="AUTO") {
             query = "SELECT * FROM PGPDB.subscription_contract_v2 scv WHERE order_id = '" + orderId + "' ORDER BY created_date DESC limit 0, 1;";
        }
        else{
            query = "SELECT * FROM SUBS_ACC.subscription_contract_v2 scv WHERE order_id = '" + orderId + "' ORDER BY created_date DESC limit 0, 1;";
        }
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query);
        if (result.size() == 0) {
            throw new NoResultFoundException("Record not found");
        }
        return result.get(0).get("metadata").toString();
    }

    private static OrderDTO getChecksum_orderDTO(OrderDTO orderDTO) {
        ObjectMapper oMapper = new ObjectMapper();
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        String merchantKey = orderDTO.getMerchantKey();
        orderDTO.setMerchantKey(null);
        orderDTO.setChecksum(null);
        String checksum = "";

        try {
            HashMap<String, String> map = oMapper.convertValue(orderDTO, HashMap.class);
            TreeMap<String, String> treeMap = new TreeMap();
            treeMap.putAll(map);
            checksum = PGPUtil.getChecksum(merchantKey, treeMap);
            orderDTO.setChecksum(checksum);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return orderDTO;
    }

    public static Response executeProcessTransaction(OrderDTO orderDTO) {
        DriverManager.setCaptureScreenShot(false);
        ObjectMapper oMapper = new ObjectMapper();
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        oMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        orderDTO = getChecksum_orderDTO(orderDTO);
        ProcessTransaction processTransaction = new ProcessTransaction();
        HashMap<String, String> formData = oMapper.convertValue(orderDTO, HashMap.class);
        processTransaction.getRequestSpecBuilder().addFormParams(formData);
        Response response = processTransaction.execute();
        DriverManager.setCaptureScreenShot(true);
        return response;
    }

    public static String getExpiredMID() {
        report.info("Fetching any expired mid");
        DriverManager.setCaptureScreenShot(false);
        try {
            String expiredMid = PGPUtil.getExpiredMID(LocalConfig.PANEL_DB_CONNECTION_URL);
            report.info("Expired mid: " + expiredMid);
            return expiredMid;
        } catch (NoResultFoundException e) {
            throw new SkipException(e.getMessage(), e);
        } finally {
            DriverManager.setCaptureScreenShot(true);
        }
    }

    public static void modifySubscriptionStartDate(long subscriptionId, LocalDateTime newSubscriptionStartDate) {
        report.info("modifying subscription start date");
        PGPUtil.modifySubscriptionStartDate(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newSubscriptionStartDate);
    }

    public static void modifySubscriptionEndDate(long subscriptionId, LocalDateTime newSubscriptionEndDate) {
        report.info("modifying subscription end date");
        PGPUtil.modifySubscriptionEndDate(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newSubscriptionEndDate);
    }

    public static void modifySubscriptionDueDate(long subscriptionId, LocalDateTime newSubscriptionDueDate) {
        report.info("modifying subscription Due date");
        PGPUtil.modifySubscriptionDueDate(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newSubscriptionDueDate);
    }

    public static void modifySubscriptionPaymentCreateDate(long subscriptionId, LocalDateTime newSubscriptionCreateDate) {
        report.info("modifying subscription Payment create date");
        PGPUtil.modifySubscriptionPaymentCreateDate(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newSubscriptionCreateDate);
    }

    public static void modifySubscriptionPaymentCreateDate(long subscriptionId, LocalDateTime newSubscriptionCreateDate,String PaymentType) {
        report.info("modifying subscription Payment create date");
        PGPUtil.modifySubscriptionPaymentCreateDate(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newSubscriptionCreateDate ,PaymentType);
    }

    public static void modifySubscriptionPaymentUpdateDate(long referenceId, LocalDateTime newSubscriptionUpdateDate) {
        report.info("modifying subscription Payment update date");
        PGPUtil.modifySubscriptionPaymentUpdateDate(LocalConfig.PGP_DB_CONNECTION_URL, referenceId, newSubscriptionUpdateDate);
    }
    public static void modifySubscriptionPaymentUpdateDate(long referenceId, LocalDateTime newSubscriptionUpdateDate ,String paymentType) {
        report.info("modifying subscription Payment update date");
        PGPUtil.modifySubscriptionPaymentUpdateDate(LocalConfig.PGP_DB_CONNECTION_URL, referenceId, newSubscriptionUpdateDate,paymentType);
    }
    public static void modifySubscriptionPaymentUpdateDate(String PGPDBUrl, long subscriptionId, LocalDateTime newSubscriptionUpdateDate, String paymentType) {
       String query="";
        if(LocalConfig.ENV_NAME=="AUTO") {
             query = "UPDATE subscription_payment_details SET updated_date='" + newSubscriptionUpdateDate + "' where subscription_id = '" + subscriptionId + "' and payment_type ='" + paymentType + "';";
        }
        else{
            query = "UPDATE SUBS_ACC.subscription_payment_details SET updated_date='" + newSubscriptionUpdateDate + "' where subscription_id = '" + subscriptionId + "' and payment_type ='" + paymentType + "';";
        }
        DatabaseUtil.getInstance().executeUpdateQuery(PGPDBUrl, query);
    }
    public static void modifySubscriptionPreNotifyDate(long subscriptionId, LocalDateTime newNotifyDate) {
        report.info("modifying subscription pre notify date");
        PGPUtil.modifySubscriptionPreNotifyDate(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newNotifyDate);
    }

    public static void modifySubscriptionPreNotifyStatus(long referenceId, String Status) {
        report.info("modifying subscription prenotify status");
        PGPUtil.modifySubscriptionPreNotifyStatus(LocalConfig.PGP_DB_CONNECTION_URL, referenceId, Status);
    }


    public static void modifySubscriptionPreNotifyTxnDate(long referenceId, LocalDateTime newNotifyTxnDate) {
        report.info("modifying subscription prenotify Transaction Date");
        PGPUtil.modifySubscriptionPreNotifyTxnDate(LocalConfig.PGP_DB_CONNECTION_URL, referenceId, newNotifyTxnDate);
    }

    public static void modifySubscriptionUpidetailCreateTime(long subscriptionId, LocalDateTime newSubscriptionEndDate) {
        report.info("modifying subscription upi detail create time");
        PGPUtil.modifySubscriptionUpidetailCreateTime(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newSubscriptionEndDate);
    }

    public static void modifySubscriptionUpidetailUpdateTime(long subscriptionId, LocalDateTime newSubscriptionEndDate) {
        report.info("modifying subscription upi update time");
        PGPUtil.modifySubscriptionUpidetailUpdateTime(LocalConfig.PGP_DB_CONNECTION_URL, subscriptionId, newSubscriptionEndDate);
    }

    public static String getCCBillPaymentToken(String cardNo, String ssoToken) {
        try {
            report.info("Executing CardTokenizeCardNumberV1 API");
            final Response response = new CardTokenizeCardNumberV1API(cardNo, ssoToken).execute();
            String cardToken = response.jsonPath().getString("body.cardToken");
            report.info("CC Bill Payment Card Token: " + cardToken);
            if (response.statusCode() != 200 || cardToken == null || "".equals(cardToken)) {
                throw new RuntimeException("CardTokenizeCardNumberV1 API has not returned correct resposne");
            }
            return cardToken;
            } catch (Throwable e) {
            throw new SkipException("Exception in returning card token", e);
        }
    }

    public static String completeHDFCCardTxn(String bankFromHtml) {
        Document doc = Jsoup.parse(bankFromHtml);
        String instaUrl = doc.getElementsByAttributeValue("name", "TermUrl").val();

        String mdValue = doc.getElementsByAttributeValue("name", "MD").val();
        String actionUrl = doc.getElementsByAttribute("ACTION").attr("ACTION");
        String[] newURL = actionUrl.split("=");
        String trackId = newURL[1].replaceAll("[^0-9]", "");
        String amt = newURL[2].replaceAll("[^.0-9]", "");
        String cardnum = newURL[3].replaceAll("[^0-9]]", "");
        String paReqValue = "<paresInternal>eJzNWVmvo0iyfr+/4qjm0d3N6oWWzxk\"+\n" +
                "\"lOzZgdoxfrtjMDjZgwPz6m7arqs90l+ZOt0ajsYQMkZGREZER8WXA9u9TVb4NcdtlTf3+BfsF/fIW12ETZXXy/sW2+J83X/7+8T9bK23jmDXj8NbGH1sl7jo/id+y6P0L3aGDaurXahMIuHAK0jEAyy6rRV55//Kx1YARd0/G/8VRbIWhOIZRGLbE4djXZT/gqr/gW+TbIxTfhqlf9x9bP7zSkvpBEgS+JrfI18dtFbcS+0GtKWqzRV4PW+S3Wdrtc\"+\n" +
                "\"ddBRacs+pD4njozycFdbOxmbTNjfGos5TDbR/t9izw4tpHfxx/f1HtD179i+K8EFP2kby8PcaBqblA2vkJRdIt8Jm2hT1ros/sHsVxtke9P23i6NHX8mLRFvt9vkd+0u/j1B/rpt0IpaOSDurWOH9s+qz5phRG/ktivSyjrSd92vd/fug9vi3y924b+MHwAAGjGAPjO8HTJKAw790rw+kFrnyzbOMw+0CVUCv4/Z4EyadqsT6uHqv9I2CIPVZDnNn5\"+\n" +
                "\"szSyp4WJt/AbDpu7ev6R9f/kVQcZx/GUkfmnaBMGhIQhKIZAh6rLkb19es+JIqs/Nn5rG+HVTZ6FfZrPfw8BQ4j5torfvuv1IjGU8JGGIwTE/Q1E/hxhZ//ygoAS2/IJ8suBfkfZ7pdrO/7lLfewhyIjP8WOj4zfbkN6//O2P4c1mSdz1f2Wdb2u8JDh+eYs/nHu3YUnOx7nK7pe3W1nmxM4SKCoq4MZ+5twi33WD9785/5PxL8Y+XRGLYrHasePKv\"+\n" +
                "\"3mihWu+2OKigjjyOGgxG9F5hqPIUVxE7qrZt\tNOSk5dmv+89d6Cd5OAEh3U984WT3Purmy6OaNWmJhFzp/ZshSPrureDZK6HjVt3nnpspwZF2um2xFV/WM/D5HSEOCjFOIU7SsSCpjkogyNGOlVgVXqvuWTVFcRUOdqmU65+UTBWbKVeehxpe559Ma4v+QY7n1bMwtszsRf4ZCuFAZHJeNaLWZIvGH3B4fXh4K2uLacy2P4omsjQRFc82wAsVqSTeeS\"+\n" +
                "\"y4brMqBHrOLztY7nKF6YRSitzZwynu60sebI70UujkW4b/0I31XBzuxNCHz0XNIOChifFVZbrAktcmzED/f395fRPjt7u4/ufDv/jEqVYv/dfd0zc9tkZZgOsSIok8ebMMDR7SMAo0SCRDHAYD4Q0hldtdxfvaHjRNsu1PY6s7u32zUlKh1AFOifTOhhDi5MVUAgAszk6VRjHUSbWAjKdqA4NGovmTzsb5SZ5Bv2L1lm78nQJcS4x3SV6Ou5u3tG4B\"+\n" +
                "\"PgyDRjags+476qlxPFziFO57/Ko71I3xZRGCXiso+ssN5Wuf1RTSXDYAMd6OCc/mfQuJFTMd5e1xKm0QpNH1uJwhU3uas7NKmtjB7550Mjf0cYzy2kKQF82TIpgHZ3eq/iLYugjlzzXlLixVz03TcOKh9Fi3L3f2W3a5CSywP9mN4epKLQL2rFLg6pLAtx72Fv4LjfxM3C++YIteNsyaS0UnPtXP8yKvnn4+mXreJq9qkw9QhmThMseejLmVTClgGB\"+\n" +
                "\"1jga6DQAp0ewIHuN70MA91BmIZtfjhVub2GbaS4DBkfvtbrlRy6jHIHQuF0+g3DFhBLCgq2q4EkVSY7AqrsQyXzWqR16D+NAZ0y6lJ40mrdPhcnaq9d2w7wxiXf287i/0fQbp7BL+5Pd8wqRMiZzL5QqVzcCjplVFop292MhLDb1SV8tJVEOY7b6OWWJTZGu9cSMyu9DAOdRecZDXoPMMu4vLVikJe12n3BCYbRFFbhySDcWMqJ5Xi4EoNvL1RqPTR\"+\n" +
                "\"OJmsRln8wa9iGk77yDbHQ8Lg5xoXTgDRsITVzIBDMB9mjOZEFGYs5I3wVRN3Oa01GjPpfxkaoK+unvZ/javrVbXChs5YeRFZ3vfhoWsWTLKiRRPurdmVZAoYNjheZKIBswberbokk6Slk44ntZD6HfjpPDhuNM9aT96NK3bogIEQXBTNBLBSr5Tg0eoo1w/4//m4VQvwxgPCTDKyec5e0Gqvs8pg3o3BMJ4i6py9lw1lSsV+oYqH/MDc5mfjtIojjBeDDSn6WTkG2ATG+XqmOSg55eVvsPNRovEOIq1lGxGFjx5LaCLCA2kEbAMfU+etDMtiYwiCYjCZMnu</paresInternal>" +
                "<trackid>" + trackId + "</trackid><amt>" + amt + "</amt><cardnum>" + cardnum + "</cardnum>";


        BaseApi baseApi = new BaseApiV2();
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        baseApi.setMethod(BaseApi.MethodType.POST);
        requestSpecBuilder.setContentType(ContentType.URLENC);
        requestSpecBuilder.setBaseUri(instaUrl);
        requestSpecBuilder.addParam("MD", mdValue);
        requestSpecBuilder.addParam("PaRes", paReqValue);
        Response response = baseApi.execute();
        return response.asString();
    }

    public static String completePPBLNBTxn(String bankFromHtml) {
        Document doc = Jsoup.parse(bankFromHtml);
        String actionUrl = doc.getElementsByAttribute("ACTION").attr("ACTION");
        String extSerialNo = doc.getElementsByAttributeValue("name", "extSerialNo").val();
        BaseApi baseApi = new BaseApiV2();
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        baseApi.setMethod(BaseApi.MethodType.POST);
        requestSpecBuilder.setContentType(ContentType.URLENC);
        requestSpecBuilder.setBaseUri(actionUrl);
        requestSpecBuilder.addParam("extSerialNo", extSerialNo);
        Response response = baseApi.execute();
        return response.asString();
    }


    public static String completeICICINBTxn(String bankFromHtml) {
        String bankUrl = bankFromHtml.split("'")[11];
        String SBMTTYPE = bankFromHtml.split("'")[19];
        String MD = bankFromHtml.split("'")[25];
        String PID = bankFromHtml.split("'")[31];
        String ES = bankFromHtml.split("'")[37];
        String SPID = bankFromHtml.split("'")[43];
        BaseApi baseApi = new BaseApiV2();
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        baseApi.setMethod(BaseApi.MethodType.POST);
        requestSpecBuilder.setContentType(ContentType.URLENC);
        requestSpecBuilder.setBaseUri(bankUrl);
        requestSpecBuilder.addParam("SBMTTYPE", SBMTTYPE);
        requestSpecBuilder.addParam("MD", MD);
        requestSpecBuilder.addParam("PID", PID);
        requestSpecBuilder.addParam("ES", ES);
        requestSpecBuilder.addParam("SPID", SPID);
        Response response = baseApi.execute();
        return NBResp(response.asString());
    }

    private static String NBResp(String bankFromHtml) {
        String instaUrl = bankFromHtml.split("\"")[7];
        String ES = bankFromHtml.split("\"")[29];
        BaseApi baseApi = new BaseApiV2();
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        baseApi.setMethod(BaseApi.MethodType.POST);
        requestSpecBuilder.setContentType(ContentType.URLENC);
        requestSpecBuilder.setBaseUri(instaUrl);
        requestSpecBuilder.addParam("ES", ES);
        Response response = baseApi.execute();
        return response.asString();
    }

    public static Float checkPPBLBalance(User user) throws Exception {
        Ppbl ppbl = new Ppbl();
        Response response = ppbl.CheckBalance(user).execute();
        Float res1 = response.jsonPath().param("value", "EFFAVL").get("response.accountBalancesList.find { accountBalancesList->accountBalancesList.balanceType==value }.amount");
        return res1;
    }

    public static String getMerchantName(String MID) {
        Reporter.report.info("Fetching merchant name");
        DriverManager.setCaptureScreenShot(false);
        String query = "select MERCHANT_NAME from ENTITY_INFO where MID=" + MID + " ";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(Constants.DBConnectionURL.PG_DB_CONNECTION_URL, query);
        if (result.size() == 0) {
            throw new RuntimeException("Record not found");
        }
        String merchantNmae = (String) result.get(0).get("acquirement_id");
        if (merchantNmae == null || merchantNmae.isEmpty()) {
            throw new RuntimeException("merchantNmae is either null or empty: " + merchantNmae);
        }
        Reporter.report.info("Merchant Name is: " + merchantNmae);
        DriverManager.setCaptureScreenShot(true);
        return merchantNmae;
    }

    public static BinDetails getBinDetails(String binNumber) throws Exception {
        BaseApi baseApi = new BaseApiV2();
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        baseApi.setMethod(BaseApi.MethodType.GET);
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST);
        requestSpecBuilder.setBasePath(PGPAPIResourcePath.BIN_DETAILS + binNumber);
        Response response = baseApi.execute();
        ObjectMapper object = new ObjectMapper();
        BinDetails binDetails = object.readValue(response.asString(), BinDetails.class);
        return binDetails;
    }

    public static BinDetails getBinDetailsApi(String binNumber) throws Exception {
        BaseApi baseApi = new BaseApiV2();
        CustomRequestSpecBuilder requestSpecBuilder = baseApi.getRequestSpecBuilder();
        baseApi.setMethod(BaseApi.MethodType.GET);
        requestSpecBuilder.setBaseUri(LocalConfig.PGP_HOST);
        requestSpecBuilder.setBasePath(PGPAPIResourcePath.BIN_DETAILS_API + binNumber);
        Response response = baseApi.execute();
        ObjectMapper object = new ObjectMapper();
        BinDetails binDetails = object.readValue(response.asString(), BinDetails.class);
        return binDetails;
    }

    public static String getNativeChecksum(String merchantKey, Object dtoBody) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "";
        try {
            jsonString = mapper.writeValueAsString(dtoBody);
            String checksum = PGPUtil.getChecksum(merchantKey, jsonString);
            return checksum;
        } catch (Exception ex) {
            Reporter.report.info("Error occurred while generating checksum");
        }
        return "";
    }

    @Step
    public static void validateRefundAllowedWithChecksum(String mid) {
        MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(mid);
        int isApiRefundAllowed = merchExtendedInfo.getExtendedInfo().getIsApiRefundAllowed();
        if (isApiRefundAllowed != 1)
            throw new SkipException("isApiRefundAllowed is " + isApiRefundAllowed + " for mid: " + mid);
        int isSecured = merchExtendedInfo.getExtendedInfo().getSecureStatusEnabled();
//        if (isSecured != 1)
//            throw new SkipException("secureStatusEnabled is " + isSecured + " for mid: " + mid);
    }

    @Step
    public static void validateBinInResponseEnabled(String mid){
        MerchantPrefInfo merchantPreferenceInfos = GetMerchPreferenceInfo.executeGetMercPreferenceInfo(mid);
        String isBinInResponseEnabled = merchantPreferenceInfos.getMerchantPreferenceInfos().stream().filter(pref -> {return pref.getPrefType().equalsIgnoreCase("BIN_IN_RESPONSE");}).findFirst().get().getPrefValue();
        if(isBinInResponseEnabled.equalsIgnoreCase("N"))
            throw new SkipException("BIN_IN_RESPONSE is :" + isBinInResponseEnabled + "for mid" + mid);
    }

    @Step
    public static void validateBinInResponseDisabled(String mid){
        MerchantPrefInfo merchantPreferenceInfos = GetMerchPreferenceInfo.executeGetMercPreferenceInfo(mid);
        String isBinInResponseEnabled = merchantPreferenceInfos.getMerchantPreferenceInfos().stream().filter(pref -> {return pref.getPrefType().equalsIgnoreCase("BIN_IN_RESPONSE");}).findFirst().get().getPrefValue();
        if(isBinInResponseEnabled.equalsIgnoreCase("Y"))
            throw new SkipException("BIN_IN_RESPONSE is :" + isBinInResponseEnabled + "for mid" + mid);
    }

    @Step("Validate emi avail for mid: {0}")
    public static MerchantEMI validateEmiAvail(String mid) {
        MerchantEMI merchantEMI = new GetMerchantHelper(mid).getMerchantEmiInfo();
        String resultCode = merchantEMI.getResultInfo().getResultCode();
        if (!resultCode.equalsIgnoreCase("SUCCESS")) {
            throw new SkipException("EMI not available on mid");
        }
        return merchantEMI;
    }


    public static String getEncryptedPaymentDetailsForExpressAddMoney(String paymentType, PaymentDTO paymentDTO) {
        try {
            String fileName = "public.key";
            switch (paymentType) {
                case "CC":
                    if (paymentDTO.getSavedCardId() == null) {
                        return Helpher.getEncryptedPayment(paymentDTO.getCreditCardNumber() + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear(), fileName);
                    } else {
                        return Helpher.getEncryptedPayment(paymentDTO.getSavedCardId() + "|" + paymentDTO.getCvvNumber(), fileName);
                    }
                case "DC":
                    if (paymentDTO.getSavedCardId() == null) {
                        return Helpher.getEncryptedPayment(paymentDTO.getDebitCardNumber() + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear(), fileName);
                    } else {
                        return Helpher.getEncryptedPayment(paymentDTO.getSavedCardId() + "|" + paymentDTO.getCvvNumber(), fileName);
                    }
                case "NB":
                case "BALANCE":
                case "UPI":
                    return "";
                default:
                    throw new SkipException("Invalid paymentType passed as parameter");
            }
        } catch (Exception e) {
            throw new SkipException(e.getMessage(), e);
        }
    }

    public static Map<String, String> parseUpiIntentDeepLink(String deeplink) {
        String[] temp = deeplink.split("&");
        Map<String, String> map = new HashMap<>();
        for(String content : temp) {
            String key = content.split("=")[0];
            if(key.contains("upi://pay?"))
                key = key.replace("upi://pay?", "");
            String value = content.split("=")[1];
            map.put(key, value);
        }
        return map;
    }

    public static Response generateUpiIntentPayRequest(UPIIntentRequestDTO upiIntentRequestDTO) {
        UpiIntentRequestAPI upiIntentRequestAPI = new UpiIntentRequestAPI(upiIntentRequestDTO);
        return upiIntentRequestAPI.execute();
    }


    public static List<Map<String,String>> getEMIDetails(String mid, Constants.Bank bankCode)
    {
       return getEMIDetails(mid, bankCode.toString());
    }

    public static List<Map<String,String>> getEMIDetails(String mid, String channelCode)
    {
        Map<String,String> jwtTokenMap=new HashMap<>();
        jwtTokenMap.put("mid",mid);
        String signature = PGPHelpers.createJsonWebToken(jwtTokenMap, PGPHelpers.ISSUER.subvention,LocalConfig.JWT_EMI_KEY);
        GetEMIDetailsRequest getEMIDetailsRequest = new GetEMIDetailsRequest(signature,mid);
        Response response = new GetEMIDetails(getEMIDetailsRequest, mid).execute();
        List<Map<String,String>> emiDetailsList=response.jsonPath().getList("body.emiDetails.find {it.channelCode =='"+channelCode+"'}.emiChannelInfos");
        return emiDetailsList;
    }

    public static Response generateUpiIntentPayRequest(CreateUpiLinkResponse createUpiLinkResponse, String txnAmount, String mid, Constants.Intent_Callback intentCallback) {
        Body body = createUpiLinkResponse.getBody();
        String deepLink = body.getDeepLink();
        Map<String, String> request = parseUpiIntentDeepLink(deepLink);
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(txnAmount)
                .setBankRRN("1111")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setChannelCode("Paytm")
                .setOrderId(createUpiLinkResponse.getBody().getOrderId())
                .setMobileNumber("")
                .setExternalSerialNo(request.get("tr"))
                .setTxnStatus(intentCallback.getStatus())
                .setResponseCode(intentCallback.getRespCode())
                .setResponseMessage(intentCallback.getRespmsg())
                .setMid(mid);
        UpiIntentRequestAPI upiIntentRequestAPI = new UpiIntentRequestAPI(upiIntentRequestDTO);
        return upiIntentRequestAPI.execute();
    }




    public static boolean validate_MerchantPreference(String mid, String prefName, String prefValue) {
        MerchantPrefInfo merchantPrefInfo = GetMerchPreferenceInfo.executeGetMercPreferenceInfo(mid);
        boolean status = false;
        if(!"00000".equalsIgnoreCase(merchantPrefInfo.getResultInfo().getResultCode()))
            throw new SkipException("Getting failure in mapping-service/merchant/get/preference/info API");
                                                                                            
        List<MerchantPreferenceInfos> merchantPreferenceInfosList = merchantPrefInfo.getMerchantPreferenceInfos();

        for(MerchantPreferenceInfos mrchntPrfInfo : merchantPreferenceInfosList) {
            if(prefName.equalsIgnoreCase(mrchntPrfInfo.getPrefType()) &&
                    prefValue.equalsIgnoreCase(mrchntPrfInfo.getPrefValue())) {
                return true;
            }
        }
        throw new SkipException(prefName+ " is mismatch or not available in merchantPref info");
    }

    public static FetchPaymentOptResponseDTO executeFetchPaymentOpt(String mid, String orderId, FetchPaymentOptionsDTO fetchPaymentOptionsDTO, boolean generateOrderId) throws IOException {
        FetchPaymentOption fetchPaymentOption;
        if(!generateOrderId)
         fetchPaymentOption = new FetchPaymentOption(mid, orderId, fetchPaymentOptionsDTO);
        else
            fetchPaymentOption = new FetchPaymentOption(mid,fetchPaymentOptionsDTO);

        Response response = fetchPaymentOption.execute();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        return mapper.readValue(jsonObject.toJSONString(), FetchPaymentOptResponseDTO.class);
    }

    public static ProcessTxnV1Response executeProcessTxn(ProcessTxnV1Request processTxnV1Request) throws IOException {
        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransaction.execute();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(response.jsonPath().get());
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES);
        return mapper.readValue(jsonObject.toJSONString(), ProcessTxnV1Response.class);
    }

  public  enum ISSUER {
        ts,
        subvention,
        PAYTMBANK,
       supergw,
      theiavalidatevpa;
    }

    public static String
    createJsonWebToken(Map<String, String> jwtClaims, ISSUER issuer, String jwtKey) {
        JWTCreator.Builder builder = JWT.create().withIssuer(issuer.toString());
        String jwtToken = null;
        Iterator var3 = jwtClaims.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var3.next();
            builder.withClaim((String)entry.getKey(), (String)entry.getValue());
        }

        try {
            jwtToken = builder.sign(Algorithm.HMAC256(jwtKey));
        } catch (JWTCreationException | UnsupportedEncodingException | IllegalArgumentException var5) {
            var5.printStackTrace();
        }

        return jwtToken;
    }

    /**
     *
     * @param srcAttribute value of src attribute in string format
     * @return Text string embedded by QRcode image
     */
    public static String getQRCodeString(String srcAttribute) throws PGPException {
        String qrCodedTxt = srcAttribute;
        BufferedImage bufferedImage;
        try {
            if(isBase64Encoded(srcAttribute)) {
                qrCodedTxt = srcAttribute.replace("data:image/png;base64,", "");
                byte[] decoded = Base64.decodeBase64(qrCodedTxt);
                bufferedImage = ImageIO.read(new ByteArrayInputStream(decoded));
            } else {
                bufferedImage = ImageIO.read(new URL(qrCodedTxt));
            }
            LuminanceSource source = new BufferedImageLuminanceSource(bufferedImage);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result = new MultiFormatReader().decode(bitmap);
            String decodedText = result.getText();
            if(decodedText.isEmpty())
                return "";
            else
                return decodedText;

        }catch (IOException e) {
            throw new PGPException("Error occurred while reading qr image", e);
        } catch (NotFoundException e) {
            throw new PGPException("Error occurred while decoding qr image", e);
        }
    }

    /**
     *
     * @param srcAttribute value of src attribute in string format
     * @return Text string embedded by QRcode image
     */
    public static String getWalletQRCodeString(String srcAttribute) {

        String decodedQRTxt = getQRCodeString(srcAttribute);
        if(decodedQRTxt.contains("upi://pay")) {
            Map<String, String> map = parseUpiIntentDeepLink(decodedQRTxt);
            return map.get("paytmqr");
        } else
            return decodedQRTxt;
    }

    private static boolean isBase64Encoded(String srcAtt) {
        if(srcAtt.contains("base64"))
            return true;
        else
            return false;
    }

    public static void launchNewTab() {
        String a = "window.open('about:blank','_blank');";
        ((JavascriptExecutor)DriverManager.getDriver()).executeScript(a);
    }

    public static void switchToNewTab() {

        ArrayList<String> tabs = null;
        PGPHelpers.launchNewTab();
        tabs = new ArrayList<String>(DriverManager.getDriver().getWindowHandles());
        if (tabs.size() == 1)
            throw new SkipException("Unable to launch new browser tab");
        DriverManager.getDriver().switchTo().window(tabs.get(1));
    }

    public static void closeNewTab() {
        ArrayList<String> tabs = null;
        tabs = new ArrayList<String>(DriverManager.getDriver().getWindowHandles());
        DriverManager.getDriver().close();
        DriverManager.getDriver().switchTo().window(tabs.get(0));
    }

    /**
     * @author : Samar Aswal
     * @param PGPDBUrl : Database URL
     * @param identifier : identifier whose status you want to get
     * @param subIdentifier : subIdentifier whose status you want to get
     * @return int
     */
    public int getLogoStatus(String PGPDBUrl, String identifier, String subIdentifier) {
        report.info("Getting logo status");
        String query;
        if(subIdentifier == null || subIdentifier.isEmpty()) {
            query = "SELECT STATUS FROM PAYTMPGDB.PAYTM_LOGOS WHERE IDENTIFIER = '" + identifier + "'";
        }
        else{
            query = "SELECT STATUS FROM PAYTMPGDB.PAYTM_LOGOS WHERE IDENTIFIER = '" + subIdentifier + "'";
        }
        int status = 0;
        if(query != null && !query.isEmpty()) {
            List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
            status = (int)result.get(0).get("STATUS");
        }
        org.testng.Reporter.log("Status of " + identifier + " = " + status, true);
        return status;
    }

    public static List<String> getBankWhereDisplayNameNotEqualsToBankName(String PGPDBUrl, String mandateType) throws Exception {
        report.info("Getting Bank where Bank Display Name is not same as Bank name");
        String query;
        query ="SELECT BANK_CODE from BANK_MASTER where (MANDATE_TYPE ='" + mandateType + "' AND BANK_NAME != BANK_DISPLAY_NAME);";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
        List<String> bankCodes = new ArrayList<>();
        for(Map<String, Object> m : result){
            bankCodes.add((String)m.get("BANK_CODE"));
        }
        if(bankCodes.isEmpty()){
            throw new Exception("Records with different Display Name and Bank Name Doesn't Exist");
        }
        org.testng.Reporter.log("Bank Channel Codes:"+ bankCodes, true);
        return bankCodes;
    }

    public static String getBankDisplayName(String PGPDBUrl, String mandateType, String channelCode) {
        report.info("Getting Bank Display Name");
        String query;
        query ="SELECT BANK_DISPLAY_NAME from PGPDB.BANK_MASTER where BANK_CODE = '" +channelCode+"' AND MANDATE_TYPE = '"+mandateType+"'";
        String bankDisplayName = null;
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
        bankDisplayName = (String) result.get(0).get("BANK_DISPLAY_NAME");
        org.testng.Reporter.log("Bank Display Name of Channel Code:"+ channelCode + " and Mandatetype:" + mandateType + " is " + bankDisplayName, true);
        return bankDisplayName;
    }


    public static Map<String, Object> getSettlementTransferStatusFromDB(String payoutId) {

        String query ="SELECT * from SETTLEMENT_TRANSFER WHERE payout_id='"+ payoutId +"'";
        List<Map<String, Object>> settlementTransferDB = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.STS_DB, query);
        System.out.println(settlementTransferDB.get(0));
        org.testng.Reporter.log("Settlement fund transfer database status is "+ settlementTransferDB.get(0).get("transaction_status") + " with status code " + settlementTransferDB.get(0).get("sts_status_code") + " and gateway id is " + settlementTransferDB.get(0).get("gateway_id"), true);
        return settlementTransferDB.get(0);
    }

    public static List<Map<String, Object>> getResponseCodeMappingData(String PGPDBUrl, String respcode) throws Exception {
        report.info("Getting ResponseCodeMappingData");
        String query;
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

        String dm = null;
        if(respcode == null || respcode.isEmpty()) {
            throw new Exception("Response Code note Provided");
        }
        else{
            query = "SELECT * FROM PGPDB.RESPONSE_CODE_MAPPING where DISPLAY_MESSAGE IS NOT NULL AND PAYTM_RESPCODE = " + respcode + ";";
        }
        if(query != null && !query.isEmpty()) {
            result = DatabaseUtil.getInstance().executeSelectQuery(PGPDBUrl, query);
        }
        return result;
    }

    public static boolean verifyKhataMerchantcheckSum(String masterKey, String paramap, String responseCheckSumString)
            throws Exception {

        boolean isValidChecksum = false;
        StringBuilder response = new StringBuilder(paramap.concat("|"));
        Encryption encryption = EncryptionFactory.getEncryptionInstance("AES");

        String decryptedString = encryption.decrypt(responseCheckSumString, masterKey);

        String randomStr = getLastNChars(decryptedString, 4);
        String payTmCheckSumHash = calculateRequestCheckSum(randomStr, response.toString());

        if (null != decryptedString && null != payTmCheckSumHash) {
            if (decryptedString.equals(payTmCheckSumHash)) {
                isValidChecksum = true;
            }

        }
        return isValidChecksum;
    }
    public static String getLastNChars(String inputString, int subStringLength) {
        if (null != inputString && inputString.length() > 0) {
            int length = inputString.length();
            if (length <= subStringLength) {
                return inputString;
            }
            int startIndex = length - subStringLength;
            return inputString.substring(startIndex);
        } else {
            return "";
        }
    }

    private static String calculateRequestCheckSum(String randomStr, String checkSumString) throws Exception {

        String reqCheckSumValue = checkSumString;
        String checkSumHash = CryptoUtils.getSHA256(reqCheckSumValue.concat(randomStr));
        checkSumHash = checkSumHash.concat(randomStr);
        return checkSumHash;
    }

    public static void assertSMSPrefEnabled(String mid)
    {
        MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(mid);
        String merchantSMS = merchExtendedInfo.getExtendedInfo().getMerchCommPref();
        int userSMS = merchExtendedInfo.getExtendedInfo().getCustCommPref();
        Assertions.assertThat(merchantSMS).as("Can not send SMS to merchant as merchCommPref value is: " +merchantSMS).isEqualTo("19"); //SMS enabled
        Assertions.assertThat(userSMS).as("User SMS should be disabled but found : " +userSMS).isEqualTo(39); //SMS disabled
    }

    public static void assertRefundSuccessNotifyPeon(String mid)
    {
        String preference = "REFUND_SUCCESS_PEON_ENABLED";
        PGPHelpers.validate_MerchantPreference(mid, preference, "Y");
    }

    public static boolean validatePTCResponseChecksum(Map txnInforesponse, String merchantKey){
        TreeMap<String, String> map = new TreeMap<>();
        AtomicReference<String> checksum = new AtomicReference<>("");
        Set<String> kset = txnInforesponse.keySet();
        Iterator<String> itr = kset.iterator();
        while (itr.hasNext()==true){
            String key = itr.next();
            String value = (String) txnInforesponse.get(key);
            if ("CHECKSUMHASH".equalsIgnoreCase(key)) {
                checksum.set(value.replaceAll(" ", "+"));
            } else {
                map.put(key, value);
            }
        }
        return PGPUtil.isChecksumValid(merchantKey, map, checksum.get());
    }


    public static ResponsePage validateSuccessResponsePage(OrderDTO orderDTO, Constants.MerchantType merchantType, String gatewayName, String bankName,String payMode)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(gatewayName)
                .validateBankName(bankName)
                .validateCheckSum(merchantType.getKey())
                .assertAll();

        return responsePage;
    }

    public static ResponsePage validateSuccessResponsePage(OrderDTO orderDTO, Constants.MerchantType merchantType, String gatewayName, String bankName,String payMode, Boolean isSplitTransaction)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(gatewayName)
                .validateBankName(bankName)
                .validateSplitSettlementInfoWithEscape(isSplitTransaction)
                .validateCheckSum(merchantType.getKey())
                .assertAll();

        return responsePage;
    }


    public static void validateSuccessPeon(OrderDTO orderDTO, String bankName, String gatewayName, String payMode)
    {

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals(bankName),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(gatewayName),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();

    }

    public static String Base64Decode(String encodedString){
        byte[] byteArray = Base64.decodeBase64(encodedString.getBytes());
        String decodedString = new String(byteArray);
        return decodedString;
    }

    static int counter=0;
    public static void isCheckoutJsV5FPOCalled(String orderId) throws InterruptedException {
        counter++;
        if (counter%10==0) {
            String versionGrep = " grep \"" + orderId + "\" /paytm/logs/theia.log" + " | grep \"fetchPaymentOptionsV5\"";
            String v5FPO = getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENT_OPTION, versionGrep);
            System.out.println("logs are----" + v5FPO);
            Assertions.assertThat(v5FPO).contains("version=v5");
        }
    }
    public static void assertSMSPrefEnabledForStaticQR(String mid)
    {
        MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(mid);
        String merchantSMS = merchExtendedInfo.getExtendedInfo().getMerchCommPref();
        int userSMS = merchExtendedInfo.getExtendedInfo().getCustCommPref();
        Assertions.assertThat(merchantSMS).as("Can not send SMS to merchant as merchCommPref value is: " +merchantSMS).isEqualTo("7"); //SMS enabled
        Assertions.assertThat(userSMS).as("User SMS should be disabled but found : " +userSMS).isEqualTo(7); //SMS disabled
    }

    public static void assertRefundSuccessNotifyPresence(String orderID) throws InterruptedException {
        String grepcmd = "grep " + orderID + " /paytm/logs/notificationQueueHandler.log | grep 'ACQUIRING_ORDER_REFUND_NOTIFY' | grep 'request'";
        Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd), s -> !"".equals(s));
//        String logs=LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd);
//        Assert.assertNotNull(logs);

    }

    public String getPassThroughExtendedInfo(String str){
        String PassThroughExtendInfo="passThroughExtendInfo";
        int idx = str.indexOf(PassThroughExtendInfo);
        idx=idx+PassThroughExtendInfo.length()+5;
        int i=idx;
        String reqStr="";

        while(i<str.length()){
            char check=str.charAt(i);
            if(check=='='){
                break;
            }
            reqStr=reqStr+str.charAt(i);
            i=i+1;
        }
        reqStr=reqStr+"==";
        return reqStr;
    }
    public String getPayerCmid(String str){
        String payerCmid="payerCmid";
        int idx = str.indexOf(payerCmid);
        idx=idx+payerCmid.length()+3;
        int i=idx;
        String reqStr="";

        while(i<str.length()){
            char check=str.charAt(i);
            if(check=='"'){
                break;
            }
            reqStr=reqStr+str.charAt(i);
            i=i+1;
        }
        return reqStr;
    }

    public static String createTokenForDealsTxn(String mid, String orderId, String txnAmount) throws UnsupportedEncodingException {
         String token = JWT.create()
                 .withClaim("iss","ts")
                 .withClaim("mid", mid)
                 .withClaim("orderId", orderId)
                 .withClaim("txnAmount", txnAmount)
                 .sign(Algorithm.HMAC256(LocalConfig.JWT_CLIENT_SECRET_DEALS_THEIA));
            return token;
    }

    public static String createTokenForGetPaymentStatusAPI(String mid, String orderId) throws UnsupportedEncodingException {
        String token = JWT.create()
                .withClaim("iss","cart")
                .withClaim("mid", mid)
                .withClaim("orderId", orderId)
                .sign(Algorithm.HMAC256(LocalConfig.JWT_CLIENT_SECRET_DEALS_THEIA));
        return token;
    }
    public static String createTokenForLiteMerchantDetails(Map<String, String> jwtClaims) throws UnsupportedEncodingException {
        String jwtToken = null;
        JWTCreator.Builder builder = JWT.create();
        Iterator var3 = jwtClaims.entrySet().iterator();

        while(var3.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var3.next();
            builder.withClaim((String)entry.getKey(), (String)entry.getValue());
        }

        try {
            jwtToken = builder.sign(Algorithm.HMAC256(LocalConfig.LITE_MERCHANT_DETAILS_JWT_KEY));
        } catch (JWTCreationException | UnsupportedEncodingException | IllegalArgumentException var5) {
            var5.printStackTrace();
        }

        return jwtToken;
    }

    public static String TraceIdGenerator(){
        String randomHex = UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String traceId = "Root=1-QA-" + randomHex;
        return traceId;
    }

    public static String openJs(String JsType, InitTxnDTO initTxnDTO, String txnToken, Constants.MerchantType mid,String payMode) throws IOException {
        CheckoutJsCheckoutPage checkoutPage =new CheckoutJsCheckoutPage();
        CheckoutJsCheckoutMerchantElementPage elementPage =new CheckoutJsCheckoutMerchantElementPage();
        CheckoutPage checkoutRedirectionPage = new CheckoutPage();

        if (JsType.toLowerCase().equals("checkoutjs")) {
            MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, "checkoutjs_web_revamp");
            config.data.setToken(txnToken);
            checkoutPage.createCheckoutJsOrder(config);
            return "checkoutjs_web_revamp";
        } else if(JsType.toLowerCase().equals("elementjs")){
            MerchantConfig config = elementPage.loadMerchantConfig(initTxnDTO, "checkoutjse_web_revamp");
            config.data.setToken(txnToken);
            elementPage.createCheckoutJsOrder(config);
            elementPage.createAndInvokePaymode(payMode);
            return "checkoutjse_web_revamp";
        }else if(JsType.toLowerCase().equals("appinvoke")){
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(mid, initTxnDTO.orderFromBody(), txnToken).build();
            checkoutRedirectionPage.createAppInvokeOrder(orderDTO);
            return "checkoutjs_web_revamp";
        }
        else if(JsType.toLowerCase().equals("redirection")){
            return "checkoutjs_web_revamp";
        }
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, JsType);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        return "checkoutjs_web_revamp";
    }
    public static String createTokenForDeviceImeiAndSource(String mid, String orderId, String txnAmount) throws UnsupportedEncodingException {
        String token = JWT.create()
                .withClaim("iss","ts")
                .withClaim("mid", mid)
                .withClaim("orderId", orderId)
                .withClaim("txnAmount", txnAmount)
                .sign(Algorithm.HMAC256(LocalConfig.JWT_CLIENT_SECRET_PAYTM_EMI_APP_THEIA));
           return token;
   }

    public static String extractFieldWithRegex(String jsonString, String pattern) {
        Pattern regexPattern = Pattern.compile(pattern);
        Matcher matcher = regexPattern.matcher(jsonString);

        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
    public static String decodeBase64(String base64String) {
        Base64 base64Url = new Base64(true);
        return new String(base64Url.decode(base64String));
    }
}
