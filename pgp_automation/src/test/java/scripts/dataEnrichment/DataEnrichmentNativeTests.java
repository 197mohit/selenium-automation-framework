package scripts.dataEnrichment;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.nativeAPI.FetchEMIDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.api.theia.FetchCardDetails;
import com.paytm.api.theia.FetchEMIPaymentChannels;
import com.paytm.api.theia.emiSubvention.ApiV1Tenure;
import com.paytm.api.theia.emiSubvention.ApiV1Validate;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchCardDetailsDTO.FetchCardDetailsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.ApiV1TenureRequest;
import com.paytm.dto.emiSubvention.ApiV1Tenure.request.Filters;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.ApiV1ValidateRequest;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.OfferDetails;
import com.paytm.dto.emiSubvention.ApiV1Validate.request.PaymentDetails;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.RiskExtendInfo;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.pgplus.common.enums.CardTypeEnum;
import com.paytm.pgplus.common.enums.PayMethod;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Issue;
import io.qameta.allure.Issues;
import io.qameta.allure.Owner;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.ResponseSpecification;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static com.paytm.appconstants.Constants.Owner.ARSH;
import static org.hamcrest.Matchers.*;

/**
 * Created by Arsh Gupta on 20/01/21.
 */
@Owner(ARSH)
public class DataEnrichmentNativeTests extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String COPPAYRequest;
    protected final ThreadLocal<FetchPaymentOptResponseDTO> fetchPaymentOptResponseDTO = new ThreadLocal<>();

    private static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, boolean generateOrderId, String mid) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(mid)
                .setToken(user.ssoToken())
                .build();
        return PGPHelpers.executeFetchPaymentOpt(mid, "", fetchPaymentOptionsDTO, generateOrderId);
    }

    private static FetchPaymentOptResponseDTO fetchPaymentOptionResponse(User user, boolean generateOrderId, Double amount, String mid) throws IOException {
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(generateOrderId))
                .setMid(mid)
                .setToken(user.ssoToken())
                .setAmount(amount)
                .build();
        return PGPHelpers.executeFetchPaymentOpt(mid, "", fetchPaymentOptionsDTO, generateOrderId);
    }

    private FetchPaymentOptResponseDTO fetchPaymentOpt(User user, String paymentMode, String amount, String mid) throws IOException {
        FetchPaymentOptResponseDTO fetchPaymentOptResponse;
        if (amount.equals("")) {
            fetchPaymentOptResponse = fetchPaymentOptionResponse(user, true, mid);
        } else {
            fetchPaymentOptResponse = fetchPaymentOptionResponse(user, true, Double.valueOf(amount), mid);
        }
        System.out.println(fetchPaymentOptResponse.getBody().getOrderId());
        boolean status = QRHelper.validatePaymentModeEnabled(fetchPaymentOptResponse, paymentMode, false);
        Assertions.assertThat(status).as(paymentMode + " is not enabled on fetchPaymentOption API").isTrue();
        return fetchPaymentOptResponse;
    }

    private final static ResponseSpecification SUCCESS_RESPONSE = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectBody("body.resultInfo.resultStatus", equalToIgnoringCase("s"))
            .expectBody("body.resultInfo.resultCode", equalToIgnoringCase("0000"))
            .expectBody("body.resultInfo.resultMsg", equalToIgnoringCase("Success"))
            .build();

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Risk Parameters going in PAY request which are passed in v1/ptc")
    public void verifyRiskParametersPassedInV1PTC(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setDeviceId("1234").setUserLBSLatitude("28.65").setUserLBSLongitude("77.30").setAppVersion("8.16.2").setVersionCode("85474983")
                .setOsType("Windows").setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                .setScreenResolution("1440X2160").setIsRooted("false").setDeviceModel("Xiaomi-Samsung").setDeviceIMEI("0901234567890234").setBrowserType("Internet Explorer")
                .setBrowserVersion("1.2.434").setDeviceManufacturer("Oppo").setLanguage("Hi").setTimeZone("IST").setCookieId("1897").setRouterMac("c0-9f-e1-ee-2c-56")
                .setChannelId("WAP").setBusinessFlow("CUSTOM_CHECKOUT").setOperationType("PAYMENT").setPlatform("mWeb").setDeviceType("Desktop").setOsVersion("9.0.7")
                .setHybridPlatform("phonegap").setHybridPlatformVersion("83.34349j84.43").setGender("MALE").setMerchantType("offus");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateNativeRiskExtendInfoParameters(jsonPath, riskExtendInfo);

        DataEnrichmentValidations.validatePaymentTypeFieldInPaymentBizInfoInNativeFlow(jsonPath);//Phase 1.1 fields validation

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("BizPayRequest").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Risk Parameters going in COP request which are passed in v1/ptc")
    public void verifyRiskParametersPassedInV1PTCCOP(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setDeviceId("1234").setUserLBSLatitude("28.65").setUserLBSLongitude("77.30").setAppVersion("8.16.2").setVersionCode("85474983")
                .setOsType("Windows").setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                .setScreenResolution("1440X2160").setIsRooted("true").setDeviceModel("Xiaomi-Samsung").setDeviceIMEI("0901234567890234").setBrowserType("Internet Explorer")
                .setBrowserVersion("1.2.434").setDeviceManufacturer("Oppo").setLanguage("Hi").setTimeZone("IST").setCookieId("1897").setRouterMac("c0-9f-e1-ee-2c-56")
                .setChannelId("WAP").setBusinessFlow("CUSTOM_CHECKOUT").setOperationType("PAYMENT").setPlatform("mWeb").setDeviceType("Desktop").setOsVersion("9.0.7")
                .setHybridPlatform("phonegap").setHybridPlatformVersion("83.34349j84.43").setGender("FEMALE").setMerchantType("offus");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateNativeRiskExtendInfoParameters(jsonPath, riskExtendInfo);

        DataEnrichmentValidations.validatePaymentTypeFieldInPaymentBizInfoInNativeFlow(jsonPath);//Phase 1.1 fields validation

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("CreateOrderAndPayRequestBean").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Risk Parameters going in Deferred Native flow")
    public void verifyRiskParametersInDeferredNative(@Optional("true") Boolean isNativePlus) throws Exception {
        String paymentMode = "CREDIT_CARD";
        String txnAmount = "2.00";

        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = fetchPaymentOpt(user, paymentMode, txnAmount, merchantType.getId());
        String orderId = fetchPaymentOptResponse.getBody().getOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setDeviceId("1234").setUserLBSLatitude("28.65").setUserLBSLongitude("77.30").setAppVersion("8.16.2").setVersionCode("85474983")
                .setOsType("Windows").setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                .setScreenResolution("1440X2160").setIsRooted("false").setDeviceModel("Xiaomi-Samsung").setDeviceIMEI("0901234567890234").setBrowserType("Internet Explorer")
                .setBrowserVersion("1.2.434").setDeviceManufacturer("Oppo").setLanguage("Hi").setTimeZone("IST").setCookieId("1897").setRouterMac("c0-9f-e1-ee-2c-56")
                .setChannelId("WAP").setBusinessFlow("STANDARD").setOperationType("PAYMENT").setPlatform("mWeb").setDeviceType("Desktop").setOsVersion("9.0.7")
                .setHybridPlatform("phonegap").setHybridPlatformVersion("83.34349j84.43").setGender("MALE").setMerchantType("offus");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo(riskExtendInfo.toString())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateNativeRiskExtendInfoParameters(jsonPath, riskExtendInfo);

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("BizPayRequest").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when risk parameters' length is breached, parameters are not sent in PAY request")
    public void verifyLengthBreachScenario(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setUserLBSLatitude("20.5937 Nasdfghja").setUserLBSLongitude("20.5937 Nasdfghjb").setDeviceId("85437258094752497243597243058234057243095724389754239074239859243")
                .setAppVersion("10.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2")
                .setVersionCode("85474983854749831")
                .setOsType("IOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/Andr")
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macint")
                .setScreenResolution("14402160X14402160").setIsRooted("trueer").setDeviceModel("Xiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXia")
                .setDeviceIMEI("09012345678902345").setBrowserType("Internet ExplorerInternet Explore").setBrowserVersion("74.0.3729.13674.0.3729.13.35.33.4")
                .setDeviceManufacturer("Oppo Vivo Xiaomi Apple Oneplus xi").setLanguage("HindiHindiiHindii").setTimeZone("ISTGMT+05:30ISTGMT+05:30ISTGMT_05")
                .setCookieId("18971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189")
                .setRouterMac("10.12.12.12.12.5410.12.12.12.12.5").setChannelId("WEBWAPAPP").setBusinessFlow("STANDARDSTANDARDA").setOperationType("PAYMENTPAYM")
                .setPlatform("WEBWAPAPP").setDeviceType("phonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedeskt")
                .setOsVersion("11.2.011.2.011.2.").setHybridPlatform("phonegapphonegapphonegapphonegapp")
                .setHybridPlatformVersion("83.34349j84.4383.34349j84.4383.34").setGender("MALEFEMALEOTHERS").setMerchantType("ONUSOFFUSONUSOFFUSONUSOFFUSONUSOF");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateLengthBreachOfParameters(jsonPath, riskExtendInfo);

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("BizPayRequest").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when risk parameters' length is breached, parameters are not sent in COP request")
    public void verifyLengthBreachScenarioCOP(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setUserLBSLatitude("20.5937 Nasdfghja").setUserLBSLongitude("20.5937 Nasdfghjb").setDeviceId("85437258094752497243597243058234057243095724389754239074239859243")
                .setAppVersion("10.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2")
                .setVersionCode("85474983854749831")
                .setOsType("IOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/Andr")
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macint")
                .setScreenResolution("14402160X14402160").setIsRooted("trueer").setDeviceModel("Xiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXia")
                .setDeviceIMEI("09012345678902345").setBrowserType("Internet ExplorerInternet Explore").setBrowserVersion("74.0.3729.13674.0.3729.13.35.33.4")
                .setDeviceManufacturer("Oppo Vivo Xiaomi Apple Oneplus xi").setLanguage("HindiHindiiHindii").setTimeZone("ISTGMT+05:30ISTGMT+05:30ISTGMT_05")
                .setCookieId("18971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189")
                .setRouterMac("10.12.12.12.12.5410.12.12.12.12.5").setChannelId("WEBWAPAPP").setBusinessFlow("STANDARDSTANDARDA").setOperationType("PAYMENTPAYM")
                .setPlatform("WEBWAPAPP").setDeviceType("phonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedeskt")
                .setOsVersion("11.2.011.2.011.2.").setHybridPlatform("phonegapphonegapphonegapphonegapp")
                .setHybridPlatformVersion("83.34349j84.4383.34349j84.4383.34").setGender("MALEFEMALEOTHERS").setMerchantType("ONUSOFFUSONUSOFFUSONUSOFFUSONUSOF");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateLengthBreachOfParameters(jsonPath, riskExtendInfo);

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("CreateOrderAndPayRequestBean").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when risk parameters are sent blank, parameters are not sent in PAY request")
    public void verifyBlankParametersScenario(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setUserLBSLatitude("").setUserLBSLongitude("").setDeviceId("").setAppVersion("").setVersionCode("").setOsType("").setUserAgent("")
                .setScreenResolution("").setIsRooted("").setDeviceModel("").setDeviceIMEI("").setBrowserType("").setBrowserVersion("")
                .setDeviceManufacturer("").setLanguage("").setTimeZone("").setCookieId("").setRouterMac("").setChannelId("").setBusinessFlow("")
                .setOperationType("").setPlatform("").setDeviceType("").setOsVersion("").setHybridPlatform("")
                .setHybridPlatformVersion("").setGender("").setMerchantType("");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateBlankParameters(jsonPath, riskExtendInfo);

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("BizPayRequest").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when risk parameters are sent blank, parameters are not sent in COP request")
    public void verifyBlankParametersScenarioCOP(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setUserLBSLatitude("").setUserLBSLongitude("").setDeviceId("").setAppVersion("").setVersionCode("").setOsType("").setUserAgent("")
                .setScreenResolution("").setIsRooted("").setDeviceModel("").setDeviceIMEI("").setBrowserType("").setBrowserVersion("")
                .setDeviceManufacturer("").setLanguage("").setTimeZone("").setCookieId("").setRouterMac("").setChannelId("").setBusinessFlow("")
                .setOperationType("").setPlatform("").setDeviceType("").setOsVersion("").setHybridPlatform("")
                .setHybridPlatformVersion("").setGender("").setMerchantType("");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateBlankParameters(jsonPath, riskExtendInfo);

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("CreateOrderAndPayRequestBean").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when some extra parameters are sent, they are sent in riskExtendInfo of PAY request")
    public void verifyExtraParametersWithLengthBreachScenario(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setExtraParameters("scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|displayName:Gaurav+corner|mode:recentBeneficiary")
                .setUserLBSLatitude("20.5937 Nasdfghja").setUserLBSLongitude("20.5937 Nasdfghjb").setDeviceId("85437258094752497243597243058234057243095724389754239074239859243")
                .setAppVersion("10.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2")
                .setVersionCode("85474983854749831")
                .setOsType("IOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/Andr")
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macint")
                .setScreenResolution("14402160X14402160").setIsRooted("trueer").setDeviceModel("Xiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXia")
                .setDeviceIMEI("09012345678902345").setBrowserType("Internet ExplorerInternet Explore").setBrowserVersion("74.0.3729.13674.0.3729.13.35.33.4")
                .setDeviceManufacturer("Oppo Vivo Xiaomi Apple Oneplus xi").setLanguage("HindiHindiiHindii").setTimeZone("ISTGMT+05:30ISTGMT+05:30ISTGMT_05")
                .setCookieId("18971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189")
                .setRouterMac("10.12.12.12.12.5410.12.12.12.12.5").setChannelId("WEBWAPAPP").setBusinessFlow("STANDARDSTANDARDA").setOperationType("PAYMENTPAYM")
                .setPlatform("WEBWAPAPP").setDeviceType("phonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedeskt")
                .setOsVersion("11.2.011.2.011.2.").setHybridPlatform("phonegapphonegapphonegapphonegapp")
                .setHybridPlatformVersion("83.34349j84.4383.34349j84.4383.34").setGender("MALEFEMALEOTHERS").setMerchantType("ONUSOFFUSONUSOFFUSONUSOFFUSONUSOF");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateLengthBreachOfParameters(jsonPath, riskExtendInfo);

        DataEnrichmentValidations.validateExtraParameters(jsonPath);

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("BizPayRequest").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when some extra parameters are sent, they are sent in riskExtendInfo of COP request")
    public void verifyExtraParametersWithLengthBreachScenarioCOP(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setExtraParameters("scanType:active|isContact:false|otpReadFlag:false|contactCreateTime:0|displayName:Gaurav+corner|mode:recentBeneficiary")
                .setUserLBSLatitude("20.5937 Nasdfghja").setUserLBSLongitude("20.5937 Nasdfghjb").setDeviceId("85437258094752497243597243058234057243095724389754239074239859243")
                .setAppVersion("10.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2110.2")
                .setVersionCode("85474983854749831")
                .setOsType("IOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/AndroidIOS/Andr")
                .setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36Mozilla/5.0 (Macint")
                .setScreenResolution("14402160X14402160").setIsRooted("trueer").setDeviceModel("Xiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXiaomi-SamsungXia")
                .setDeviceIMEI("09012345678902345").setBrowserType("Internet ExplorerInternet Explore").setBrowserVersion("74.0.3729.13674.0.3729.13.35.33.4")
                .setDeviceManufacturer("Oppo Vivo Xiaomi Apple Oneplus xi").setLanguage("HindiHindiiHindii").setTimeZone("ISTGMT+05:30ISTGMT+05:30ISTGMT_05")
                .setCookieId("18971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189718971897189")
                .setRouterMac("10.12.12.12.12.5410.12.12.12.12.5").setChannelId("WEBWAPAPP").setBusinessFlow("STANDARDSTANDARDA").setOperationType("PAYMENTPAYM")
                .setPlatform("WEBWAPAPP").setDeviceType("phonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedesktoptablephonedeskt")
                .setOsVersion("11.2.011.2.011.2.").setHybridPlatform("phonegapphonegapphonegapphonegapp")
                .setHybridPlatformVersion("83.34349j84.4383.34349j84.4383.34").setGender("MALEFEMALEOTHERS").setMerchantType("ONUSOFFUSONUSOFFUSONUSOFFUSONUSOF");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateLengthBreachOfParameters(jsonPath, riskExtendInfo);

        DataEnrichmentValidations.validateExtraParameters(jsonPath);

        //COP/PAY request pushed in Kafka Topic logs Validation
        String cmdToFetchKafkaLogger = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia.log | grep 'Sending request to kafka topic'";
        System.out.println(cmdToFetchKafkaLogger);
        String KafkaTopicPushLogger = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchKafkaLogger), s -> !"".equals(s));
        Assertions.assertThat(KafkaTopicPushLogger).contains("CreateOrderAndPayRequestBean").as("Data is not pushed in Kafka topic");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify risk parameters in case of add money transaction")
    public void verifyMerchantAddMoneyScenario(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getDebitCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO, addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        RiskExtendInfo riskExtendInfo = new RiskExtendInfo();
        riskExtendInfo.setDeviceId("1234").setUserLBSLatitude("28.65").setUserLBSLongitude("77.30").setAppVersion("8.16.2").setVersionCode("85474983")
                .setOsType("Windows").setUserAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_0_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36")
                .setScreenResolution("1440X2160").setIsRooted("false").setDeviceModel("Xiaomi-Samsung").setDeviceIMEI("0901234567890234").setBrowserType("Internet Explorer")
                .setBrowserVersion("1.2.434").setDeviceManufacturer("Oppo").setLanguage("Hi").setTimeZone("IST").setCookieId("1897").setRouterMac("c0-9f-e1-ee-2c-56")
                .setChannelId("WAP").setBusinessFlow("STANDARD").setOperationType("PAYMENT").setPlatform("mWeb").setDeviceType("Desktop").setOsVersion("9.0.7")
                .setHybridPlatform("phonegap").setHybridPlatformVersion("83.34349j84.43").setGender("MALE").setMerchantType("offus");
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .setREQUEST_TYPE("Add_Money")
                    .setRiskExtendInfo(riskExtendInfo.toString())
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .setREQUEST_TYPE("Add_Money")
                    .setRisk_extended_info(riskExtendInfo.toString())
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateNativeRiskExtendInfoParameters(jsonPath, riskExtendInfo);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when blank riskExtendInfo is sent blank in v1/ptc, parameters are not sent in PAY request")
    public void verifyBlankRiskExtendInfoScenario(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo("")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info("")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateBlankRiskExtendInfoScenario(jsonPath);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when blank riskExtendInfo is sent blank in v1/ptc, parameters are not sent in COP request")
    public void verifyBlankRiskExtendInfoScenarioCOP(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo("")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info("")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateBlankRiskExtendInfoScenario(jsonPath);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when only 1 parameter is sent in v1/ptc, only that parameter is sent in PAY request")
    public void verifyOnlyOneRiskExtendInfoParameterSentScenario(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo("scanType:active")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info("scanType:active")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateOnlyOneRiskExtendInfoParameterSent(jsonPath);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify when only 1 parameter is sent in v1/ptc, only that parameter is sent in COP request")
    public void verifyOnlyOneRiskExtendInfoParameterSentScenarioCOP(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo("scanType:active")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info("scanType:active")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateOnlyOneRiskExtendInfoParameterSent(jsonPath);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    /////////////////////////////////////////////////////clientIp field validation test cases//////////////////////////////////////////////////////////////////

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in FundTopUp API")
    public void IPv6clientIpFundTopUp(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setIsNativeAddMoney("true")
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);//passing clientIp in IPv6 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
        String cmdToFetchFundTopUpRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'FUND_USER_TOPUP_FROM_MERCHANT' | grep 'REQUEST'";
        String fundTopUpRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchFundTopUpRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(fundTopUpRequest);
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isEqualTo(IPv6clientIp).as("clientIp mismatch");
        OrderDTO orderDTO;
        orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in CreateOrder API")
    public void IPv6clientIpCreateOrder(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);//passing clientIp in IPv6 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
        String cmdToFetchCreateOrderRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER' | grep 'REQUEST'";
        String fetchCreateOrderRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCreateOrderRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(fetchCreateOrderRequest);
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isEqualTo(IPv6clientIp).as("clientIp mismatch");//Validation for clientIp as per current handling
        OrderDTO orderDTO;
        orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in PAY API")
    public void IPv6clientIpCashierPay(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210807921559|645|012024")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateClientIpField(jsonPath, IPv6clientIp);//Validation for clientIp as per current handling
    }


    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv6 format clientIp is sent in COP API")
    public void IPv6clientIpCOP(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        String IPv6clientIp = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210807921559|645|012024")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv6clientIp);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateClientIpField(jsonPath, IPv6clientIp);//Validation for clientIp as per current handling
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in FundTopUp API")
    public void IPv4clientIpFundTopUp(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setIsNativeAddMoney("true")
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);//passing clientIp in IPv4 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
        String cmdToFetchFundTopUpRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'FUND_USER_TOPUP_FROM_MERCHANT' | grep 'REQUEST'";
        String fundTopUpRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchFundTopUpRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(fundTopUpRequest);
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isEqualTo(IPv4clientIp).as("clientIp values does not match");
        OrderDTO orderDTO;
        orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in CreateOrder API")
    public void IPv4clientIpCreateOrder(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), initTxnDTO);
        initTxn.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);//passing clientIp in IPv4 format
        String txnToken = initTxn.execute().jsonPath().getString("body.txnToken");
        String cmdToFetchCreateOrderRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER' | grep 'REQUEST'";
        String fetchCreateOrderRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCreateOrderRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(fetchCreateOrderRequest);
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.clientIp")).isEqualTo(IPv4clientIp).as("clientIp values does not match");
        OrderDTO orderDTO;
        orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in PAY API")
    public void IPv4clientIpCashierPay(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_ONLY;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210807921559|645|012024")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateClientIpField(jsonPath, IPv4clientIp);
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify transaction does not fail when IPv4 format clientIp is sent in COP API")
    public void IPv4clientIpCOP(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        String IPv4clientIp = "49.36.131.88";
        User user = userManager.getForRead(Label.PRIORITY);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType.getId(), txnToken, initTxnDTO.getBody().getOrderId())
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210807921559|645|012024")
                .setAuthMode("otp")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.getRequestSpecBuilder().addHeader("X-Forwarded-For", IPv4clientIp);
        Response response = processTransactionV1.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000").as("Bank Form failure");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success").as("Bank Form failure");

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        DataEnrichmentValidations.validateClientIpField(jsonPath, IPv4clientIp);
    }


    /////////////////////////////////osType, browserType, deviceModel, deviceManufacturer fields validation test cases///////////////////////////////////////


    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify merchantType field is going as OFFUS for Offus merchant transaction")
    public void validateMerchantTypeFieldForOffusMerchant(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PaytmExpress_Hybrid_Offus;

        Boolean isOffus = new MigrationDetails(merchantType.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOffus).as("Merchant is not offus").isEqualTo(false);//checking if the merchant is offus or not

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("9")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo("scanType:active")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info("scanType:active")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isEqualTo("OFFUS").as("merchantType mismatch");

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        Assertions.assertThat(jsonPath1.getString("merchantType")).isEqualTo("OFFUS").as("merchantType mismatch");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify merchantType field is going as ONUS for Onus merchant transaction")
    public void validateMerchantTypeFieldForOnusMerchant(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SUBSCRIPTION_PGONLY_RETRY;

        Boolean isOnus = new MigrationDetails(merchantType.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOnus).as("Merchant is not onus").isEqualTo(true);//checking if the merchant is onus or not

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("9")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .setRiskExtendInfo("scanType:active")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .setRisk_extended_info("scanType:active")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isEqualTo("ONUS").as("merchantType mismatch");

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        Assertions.assertThat(jsonPath1.getString("merchantType")).isEqualTo("ONUS").as("merchantType mismatch");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify merchantType field is going as OFFUS for Offus merchant transaction in COP request")
    public void validateMerchantTypeFieldForOffusMerchantCOP(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        User user = userManager.getForRead(Label.BASIC);
        Boolean isOffus = new MigrationDetails(merchantType.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOffus).as("Merchant is not offus").isEqualTo(false);//checking if the merchant is offus or not

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("9")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .setRiskExtendInfo("scanType:active")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                    .setRisk_extended_info("scanType:active")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isEqualTo("OFFUS").as("merchantType mismatch");

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        Assertions.assertThat(jsonPath1.getString("merchantType")).isEqualTo("OFFUS").as("merchantType mismatch");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify merchantType field is going as ONUS for Onus merchant transaction in COP request")
    public void validateMerchantTypeFieldForOnusMerchantCOP(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_2;
        Boolean isOnus = new MigrationDetails(merchantType.getId()).execute().jsonPath().getBoolean("MERCHANT-EXTENDED-INFO.extendedInfo.ONPAYTM");
        Assertions.assertThat(isOnus).as("Merchant is not onus").isEqualTo(true);//checking if the merchant is onus or not

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("9")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRiskExtendInfo("scanType:active")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                    .setAUTH_MODE("USRPSWD")
                    .setChannelCode("ICICI")
                    .setRisk_extended_info("scanType:active")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.envInfo.merchantType")).isEqualTo("ONUS").as("merchantType mismatch");

        String extendInfo = jsonPath.getString("REQUEST.request.body.envInfo.extendInfo");
        JsonPath jsonPath1 = new JsonPath(extendInfo);

        Assertions.assertThat(jsonPath1.getString("merchantType")).isEqualTo("ONUS").as("merchantType mismatch");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    /////////////////////////////////////////////paymentBizInfo json object fields validation cases///////////////////////////////////////////////////////

    @Owner(ARSH)
    @Parameters("isNativePlus")
    @Test(description = "Verify paymentBizInfo parameters in PAY request for VISA credit card")
    public void validatePaymentBizInfoForVISACreditCard(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_3;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_CREDIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                    .setRiskExtendInfo("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                    .setRisk_extended_info("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        SoftAssertions softAssertions = new SoftAssertions();
        List<Object> paymentAuthenticationFlows = jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows");
        softAssertions.assertThat(paymentAuthenticationFlows).isEmpty();
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isEqualTo("false").as("Card is not Indian");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isEqualTo("false").as("card is saved, not new card");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isEqualTo(CardTypeEnum.VISA.getName()).as("cardType mismatch");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo(PayMethod.CREDIT_CARD.getMethod()).as("paymentMode mismatch");
        softAssertions.assertAll();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters("isNativePlus")
    @Test(description = "Verify paymentBizInfo parameters in COP request for VISA credit card")
    public void validatePaymentBizInfoForVISACreditCardCOP(@Optional("true") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_CREDIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                    .setRiskExtendInfo("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                    .setRisk_extended_info("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        SoftAssertions softAssertions = new SoftAssertions();
        List<Object> paymentAuthenticationFlows = jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows");
        softAssertions.assertThat(paymentAuthenticationFlows).isEmpty();
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isEqualTo("false").as("Card is not Indian");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isEqualTo("false").as("card is saved, not new card");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isEqualTo(CardTypeEnum.VISA.getName()).as("cardType mismatch");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo(PayMethod.CREDIT_CARD.getMethod()).as("paymentMode mismatch");
        softAssertions.assertAll();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters("isNativePlus")
    @Test(description = "Verify paymentBizInfo parameters in PAY request for Master debit card")
    public void validatePaymentBizInfoForMASTERDebitCard(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                    .setRiskExtendInfo("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                    .setRisk_extended_info("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        SoftAssertions softAssertions = new SoftAssertions();
        List<Object> paymentAuthenticationFlows = jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows");
        softAssertions.assertThat(paymentAuthenticationFlows).isEmpty();
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isEqualTo("false").as("Card is not Indian");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isEqualTo("false").as("card is saved, not new card");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isEqualTo(CardTypeEnum.MASTER.getName()).as("cardType mismatch");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo(PayMethod.DEBIT_CARD.getMethod()).as("paymentMode mismatch");
        softAssertions.assertAll();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters("isNativePlus")
    @Test(description = "Verify paymentBizInfo parameters in COP request for Master debit card")
    public void validatePaymentBizInfoForMASTERDebitCardCOP(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO;
        if (isNativePlus) {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                    .setRiskExtendInfo("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        } else {
            orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                    .setRisk_extended_info("businessFlow:CUSTOM_CHECKOUT")
                    .build();
        }
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        SoftAssertions softAssertions = new SoftAssertions();
        List<Object> paymentAuthenticationFlows = jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows");
        softAssertions.assertThat(paymentAuthenticationFlows).isEmpty();
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isEqualTo("false").as("Card is not Indian");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isEqualTo("false").as("card is saved, not new card");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isEqualTo(CardTypeEnum.MASTER.getName()).as("cardType mismatch");
        softAssertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo(PayMethod.DEBIT_CARD.getMethod()).as("paymentMode mismatch");
        softAssertions.assertAll();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo for Paytm Postpaid payment mode")
    public void validatePaymentBizInfoForPostPaid(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType mid = Constants.MerchantType.POSTPAIDANDUPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(mid, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("ssoToken").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("PAYTM_DIGITAL_CREDIT").as("payMethod mismatch");
        List<Object> payMethodDetails = jsonPath.getList("REQUEST.request.body.paymentBizInfo.payMethodDetails");
        Assertions.assertThat(payMethodDetails).as("payMethodDetails list is not empty").isEmpty();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo for Paytm Postpaid payment mode in COP request")
    public void validatePaymentBizInfoForPostPaidCOP(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        User user = userManager.getForRead(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("ssoToken").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("PAYTM_DIGITAL_CREDIT").as("payMethod mismatch");
        List<Object> payMethodDetails = jsonPath.getList("REQUEST.request.body.paymentBizInfo.payMethodDetails");
        Assertions.assertThat(payMethodDetails).as("payMethodDetails list is not empty").isEmpty();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo for Paytm Wallet payment mode")
    public void validatePaymentBizInfoForBalance(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(Constants.MerchantType.PGOnly, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("ssoToken").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("BALANCE").as("payMethod mismatch");
        List<Object> payMethodDetails = jsonPath.getList("REQUEST.request.body.paymentBizInfo.payMethodDetails");
        Assertions.assertThat(payMethodDetails).as("payMethodDetails list is not empty").isEmpty();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo for Paytm Wallet payment mode in COP request")
    public void validatePaymentBizInfoForBalanceCOP(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.
                Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("ssoToken").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("BALANCE").as("payMethod mismatch");
        List<Object> payMethodDetails = jsonPath.getList("REQUEST.request.body.paymentBizInfo.payMethodDetails");
        Assertions.assertThat(payMethodDetails).as("payMethodDetails list is not empty").isEmpty();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo parameters for UPI Collect Transaction")
    @Issues({@Issue("PGP-30462"), @Issue("PGP-30512")})
    //currently bankCode, bankName and PSPApp parameters are going as null, above issues are still open for discussion
    public void verifyPaymentBizInfoForUPICollect(@Optional("true") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String[] vpa = orderDTO.getPayerAccount().split("@");
        String expectedVpaChannel = vpa[1];
        System.out.println(expectedVpaChannel);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("collect").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("UPI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("UPI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.vpaChannel")).isEqualTo(expectedVpaChannel).as("payMethod mismatch");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo parameters for UPI Collect Transaction in COP request")
    @Issues({@Issue("PGP-30462"), @Issue("PGP-30512")})
    //currently bankCode, bankName and PSPApp parameters are going as null, above issues are still open for discussion
    public void verifyPaymentBizInfoForUPICollectCOP(@Optional("true") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@okhdfc").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String[] vpa = orderDTO.getPayerAccount().split("@");
        String expectedVpaChannel = vpa[1];
        System.out.println(expectedVpaChannel);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("collect").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("UPI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("UPI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.vpaChannel")).isEqualTo(expectedVpaChannel).as("payMethod mismatch");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo parameters for NB in COP request")
    public void verifyPaymentBizInfoParametersForNBCOP(@Optional("true") Boolean isNativePlus) {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setAUTH_MODE("USRPSWD")
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String cmdToFetchCOPRequest = "grep " + initTxnDTO.orderFromBody() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);

        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].authFlow")).isEqualTo("redirectToBank").as("authFlow mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows[0].payMethod")).isEqualTo("NET_BANKING").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.payMethodDetails")).as("List is not empty").isEmpty();//as per the current handling

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Issue("PGP-30466")//tenure parameter is not coming, needs to be fixed
    @Test(description = "verify paymentBizInfo parameters for EMI payment mode")
    public void verifyPaymentBizInfoForEMI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.HYBRID_PEON_DISABLED;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2")
                .setWebsiteName("nonmatchingwebsite")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest(txnToken, "HDFC");
        Response res = new FetchEMIDetail(fetchEMIDetailRequest, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody()).execute();
        JsonPath path = res.jsonPath();
        String emiPlanId = path.get("body.emiDetail.emiChannelInfos[0].planId");
        String interestRate = path.get("body.emiDetail.emiChannelInfos[0].interestRate");

        OrderDTO orderDTO = new OrderFactory.
                Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setWEBSITE("nonmatchingwebsite")
                .setCardInfo("|" + paymentDTO.getCreditCardNumber() + "|618|092023")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID")).isEqualTo(emiPlanId).as("emiPlanId mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Issue("PGP-30466")//tenure parameter is not coming, needs to be fixed
    @Test(description = "verify paymentBizInfo parameters for EMI payment mode in COP request")
    public void verifyPaymentBizInfoForEMICOP(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_2;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest(txnToken, "HDFC");
        Response res = new FetchEMIDetail(fetchEMIDetailRequest, initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody()).execute();
        JsonPath path = res.jsonPath();
        String emiPlanId = path.get("body.emiDetail.emiChannelInfos[0].planId");
        String interestRate = path.get("body.emiDetail.emiChannelInfos[0].interestRate");

        OrderDTO orderDTO = new OrderFactory.
                Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setCardInfo("|" + paymentDTO.getCreditCardNumber() + "|618|092023")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID")).isEqualTo(emiPlanId).as("emiPlanId mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("EMI")
                .assertAll();
    }

    @Owner(ARSH)
    @Issue("PGP-30543")
    @Parameters({"isNativePlus"})
    @Test(description = "verify paymentBizInfo parameters for EMI_DC payment mode")
    public void verifyPaymentBizInfoForEMI_DC(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.EMIDC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchEMIPaymentChannels fetchEMIPaymentChannels = new FetchEMIPaymentChannels(merchantType.getId(), initTxnDTO.orderFromBody(), txnToken);
        JsonPath path = fetchEMIPaymentChannels.execute().jsonPath();
        String planId = path.get("body.emiPayOption.payChannelOptions[1].emiChannelInfos[0].planId");
        String interestRate = path.get("body.emiPayOption.payChannelOptions[1].emiChannelInfos[0].interestRate");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("|" + PaymentDTO.ICICI_DEBIT_CARD_NUMBER + "|111|122024")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setPlanId("ICICI|3")
                .setEmiType("DEBIT_CARD")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        String JSON_POST_URL = LocalConfig.JSON_POST_URL;
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());

        String cmdToFetchCOPRequest = "grep " + initTxnDTO.getBody().getOrderId() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");//as per current handling, needs to be fixed, expected : EMI_DC
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID")).isEqualTo(planId).as("emiPlanId mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Issue("PGP-30543")
    @Test(description = "verify paymentBizInfo parameters for EMI_DC payment mode in COP request")
    public void verifyPaymentBizInfoForEMI_DCCOP(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.EMIDC);
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_2;
        PaymentDTO Emi_Dc = new PaymentDTO();
    //    SavedCardHelpers.deleteSavedCard(user);
        Emi_Dc.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER).setBankName("ICICI Bank Debit Card").setMonth(3);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchEMIPaymentChannels fetchEMIPaymentChannels = new FetchEMIPaymentChannels(merchantType.getId(), initTxnDTO.orderFromBody(), txnToken);
        JsonPath path = fetchEMIPaymentChannels.execute().jsonPath();
        String planId = path.get("body.emiPayOption.payChannelOptions[0].emiChannelInfos[2].planId");
        String interestRate = path.get("body.emiPayOption.payChannelOptions[0].emiChannelInfos[2].interestRate");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        fetchPaymentOption.execute();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, Emi_Dc, PayMethodType.EMI)
                .setPlanId(planId)
                .setCHANNEL_ID("ICICI")
                .setEMI_TYPE("DEBIT_CARD")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");//as per current handling, needs to be fixed, expected : EMI_DC
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID")).isEqualTo(planId).as("emiPlanId mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Issue("PGP-30466")//Some parameters are missing on which dev will provide fix in 2nd phase
    @Test(description = "verify paymentBizInfo parameters for old EMI Subvention")
    public void verifyPaymentBizInfoForOldEMISubvention(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.XIAOMI1;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String planId = new ApiV1Tenure(merchantType.getId(), req).execute()
                .then().spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath().getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setCustomerId(unqId)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .build();
        JsonPath validateResp = new ApiV1Validate(merchantType.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");
        planId = validateResp.getString("body.planId");
        String interestRate = validateResp.getString("body.interest");
        String discount = validateResp.getString("body.gratifications[0].value");
        String tenure = validateResp.getString("body.interval");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount("4"))
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");


        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, new PaymentDTO(), PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setCardInfo("|" + paymentDTO.getCreditCardNumber() + "|618|092023")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.discount")).isEqualTo(discount).as("Discount mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID")).isEqualTo(planId).as("emiPlanId mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.tenure")).isEqualTo(tenure).as("Tenure mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getPayableAmount().getValue())
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Issue("PGP-30466")//Some parameters are missing on which dev will provide fix in 2nd phase
    @Test(description = "verify paymentBizInfo parameters for old EMI Subvention in COP request")
    public void verifyPaymentBizInfoForOldEMISubventionCOP(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_2;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        User user = userManager.getForRead(Label.BASIC);
        String unqId = CommonHelpers.generateOrderId();
        ApiV1TenureRequest req = new ApiV1TenureRequest.Builder()
                .setMid(merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setFilters(new Filters()
                        .setBankCode("HDFC")
                        .setCardType("CREDIT_CARD"))
                .build();

        String planId = new ApiV1Tenure(merchantType.getId(), req).execute()
                .then().spec(SUCCESS_RESPONSE)
                .body("body.planDetails.emiType", hasItems("SUBVENTION"))
                .extract().jsonPath().getString("body.planDetails.find{it.emiType == 'SUBVENTION'}.planId");
        BigInteger intplanid= new BigInteger(planId);
        ApiV1ValidateRequest req2 = new ApiV1ValidateRequest.Builder(user.ssoToken(), "SSO", merchantType.getId())
                .setMerchantKey(merchantType.getKey())
                .setPlanId(intplanid)
                .setCustomerId(unqId)
                .setPaymentDetails(
                        new PaymentDetails()
                                .setTotalTransactionAmount("5")
                                .setCardNumber("|" + PGPHelpers.getFormattedPaymentDetails("EMI", paymentDTO)))
                .setGenerateTokenForIntent(true)
                .build();
        JsonPath validateResp = new ApiV1Validate(merchantType.getId(), req2).execute()
                .then()
                .spec(SUCCESS_RESPONSE)
                .body("body", hasKey("emiSubventionToken"))
                .extract().jsonPath();
        String emiSubventionToken = validateResp.getString("body.emiSubventionToken");
        String pgPlanId = validateResp.getString("body.pgPlanId");
        planId = validateResp.getString("body.planId");
        String interestRate = validateResp.getString("body.interest");
        String discount = validateResp.getString("body.gratifications[0].value");
        String tenure = validateResp.getString("body.interval");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setEmiSubventionToken(emiSubventionToken)
                .setTxnValue("5")
                .setPayableAmount(new TxnAmount("4"))
                .build();
        String txnToken = new InitTxn(initTxnDTO).execute().then()
                .spec(SUCCESS_RESPONSE)
                .body("body.txnToken", not(empty()))
                .extract().jsonPath().getString("body.txnToken");


        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.EMI)
                .setChannelCode(Constants.Bank.HDFC.toString())
                .setPlanId(pgPlanId)
                .setAUTH_MODE("otp")
                .setREQUEST_TYPE("NATIVE")
                .setWEBSITE("retail")
                .setCardInfo("|" + paymentDTO.getCreditCardNumber() + "|618|092023")
                .setINDUSTRY_TYPE_ID("Retail").build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.discount")).isEqualTo(discount).as("Discount mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID")).isEqualTo(planId).as("emiPlanId mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.tenure")).isEqualTo(tenure).as("Tenure mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.getBody().getPayableAmount().getValue())
                .assertAll();
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Issue("PGP-30508")//Still an open issue, dev to provide fix
    @Test(description = "Verify paymentBizInfo parameters for Old Promo with cashback transaction")
    public void verifyPaymentBizInfoForOldPromoWithCashbackTxn(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.XIAOMI1;
        User user = userManager.getForRead(Label.BASIC);

        Merchant merchant = new Merchant(merchantType.getId(), true);
        String txnAmt = "100";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnAmt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchantType.getId());
        apiV1ApplyPromo
                .setContext("body.promocode", "cashback")
                .setContext("head.token", user.ssoToken())
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));
        Response response = apiV1ApplyPromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnAmt)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        String actualOfferCode = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerCode");
        String actualDiscount = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].discount");
        String actualOfferStatus = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerStatus");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmt)
                .assertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());

        JsonPath jsonPath1 = new JsonPath(peonResponse.getPaymentPromoCheckoutData());
        String expectedOfferStatus = jsonPath1.getString("status");
        String expectedOfferCode = jsonPath1.getString("promocode");
        String expectedPromoDiscount = jsonPath1.getString("savings[0].savings");
        double amount = Double.parseDouble(expectedPromoDiscount) * 100;
        int cashbackAmount = (int) amount;

        //Validations
        Assertions.assertThat(actualOfferCode).isEqualTo(expectedOfferCode).as("offerCode mismatch");
        Assertions.assertThat(actualDiscount).isEqualTo(String.valueOf(cashbackAmount)).as("cashback amount mismatch");//As per current handling, need to be fixed
        Assertions.assertThat(actualOfferStatus).isEqualTo(expectedOfferStatus).as("offerStatus mismatch");

    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Issue("PGP-30508")//Still an open issue, dev to provide fix
    @Test(description = "Verify paymentBizInfo parameters for Old Promo with cashback transaction in COP request")
    public void verifyPaymentBizInfoForOldPromoWithCashbackTxnCOP(@Optional("true") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.DATA_ENRICHMENT_MID_1;
        User user = userManager.getForRead(Label.BASIC);

        Merchant merchant = new Merchant(merchantType.getId(), true);
        String txnAmt = "100";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnAmt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.ICICI_CC_CARD);

        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchantType.getId());
        apiV1ApplyPromo
                .setContext("body.promocode", "cashback")
                .setContext("head.token", user.ssoToken())
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));
        Response response = apiV1ApplyPromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnAmt)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        String actualOfferCode = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerCode");
        String actualDiscount = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].discount");
        String actualOfferStatus = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerStatus");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmt)
                .assertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());

        JsonPath jsonPath1 = new JsonPath(peonResponse.getPaymentPromoCheckoutData());
        String expectedOfferStatus = jsonPath1.getString("status");
        String expectedOfferCode = jsonPath1.getString("promocode");
        String expectedPromoDiscount = jsonPath1.getString("savings[0].savings");
        double amount = Double.parseDouble(expectedPromoDiscount) * 100;
        int cashbackAmount = (int) amount;

        //Validations
        Assertions.assertThat(actualOfferCode).isEqualTo(expectedOfferCode).as("offerCode mismatch");
        Assertions.assertThat(actualDiscount).isEqualTo(String.valueOf(cashbackAmount)).as("cashback amount mismatch");//As per current handling, need to be fixed
        Assertions.assertThat(actualOfferStatus).isEqualTo(expectedOfferStatus).as("offerStatus mismatch");
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo parameters for Apply Promo data and Simplified Amount EMI Subvention transaction")
    public void verifyPaymentBizInfoParametersForApplyPromoAndSimplifiedAmountEmiSubvention(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.XIAOMI4;

        Merchant merchant = new Merchant(merchantType.getId(), true);
        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String, String>> emiDetails;
        emiDetails = PGPHelpers.getEMIDetails(merchant.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        String interestRate = emiDetails.get(0).get("interestRate");
        String txnAmt = "10";
        Promo promo = new Promo();
        merchant.getPromos().add(promo);
        ApiV1ApplyPromo apiV1ApplyPromo = new ApiV1ApplyPromo(merchantType.getId());

        Map<String, Object> paymentOption = new HashMap<>();
        paymentOption.put("transactionAmount", txnAmt);
        paymentOption.put("payMethod", "CREDIT_CARD");
        paymentOption.put("cardNo", PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);

        apiV1ApplyPromo
                .setContext("body.promocode", "discount")
                .setContext("head.token", user.ssoToken())
                .setContext("body.paymentOptions", Arrays.asList(paymentOption));
        Response response = apiV1ApplyPromo.execute();

        HashMap<String, Object> paymentOffersAppliedResponse = response.jsonPath().get("body.paymentOffer");

        PaymentOffersApplied paymentOffersApplied = new PaymentOffersApplied(paymentOffersAppliedResponse);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, paymentOffersApplied)
                .setTxnValue(txnAmt)
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, "1", new OfferDetails().setOfferId("123456")))
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String txnToken = iniJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setEMI_TYPE("CREDIT_CARD")
                .setCardInfo("|" + paymentDTO.getCreditCardNumber() + "|618|092023")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations for EMI related parameters
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        //Actual values of EMI Subvention Parameters
        String actualEMIDiscount = jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.discount");
        String actualPlanID = jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID");
        String actualTenure = jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.tenure");

        //Actual values of Promo Parameters
        String actualOfferCode = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerCode");
        String actualDiscount = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].discount");
        String actualOfferStatus = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerStatus");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());

        //Expected values of EMI subvention parameters
        JsonPath jsonPath1 = new JsonPath(peonResponse.getEmiSubventionInfo());
        String expectedPlanID = jsonPath1.getString("planId");
        String expectedTenure = jsonPath1.getString("tenure");
        String expectedEMIDiscount = jsonPath1.getString("gratificationDiscount");

        //Expected values of Promo parameters
        JsonPath jsonPath2 = new JsonPath(peonResponse.getPaymentPromoCheckoutData());
        String expectedOfferStatus = jsonPath2.getString("status");
        String expectedOfferCode = jsonPath2.getString("promocode");
        String expectedPromoDiscount = jsonPath2.getString("savings[0].savings");
        double amount = Double.parseDouble(expectedPromoDiscount) * 100;
        int cashbackAmount = (int) amount;

        //Validation for EMI subvention parameters
        Assertions.assertThat(actualEMIDiscount).isEqualTo(expectedEMIDiscount).as("EMI discount mismatch");
        Assertions.assertThat(actualPlanID).isEqualTo(expectedPlanID).as("planId mismatch");
        Assertions.assertThat(actualTenure).isEqualTo(expectedTenure).as("tenure mismatch");

        //Validations for promo parameters
        Assertions.assertThat(actualOfferCode).isEqualTo(expectedOfferCode).as("offerCode mismatch");
        Assertions.assertThat(actualDiscount).isEqualTo(String.valueOf(cashbackAmount)).as("cashback amount mismatch");//As per current handling, need to be fixed
        Assertions.assertThat(actualOfferStatus).isEqualTo(expectedOfferStatus).as("offerStatus mismatch");
    }

    @Owner(ARSH)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify paymentBizInfo parameters for Simplified Promo and Simplified Item EMI Subvention transaction")
    public void verifyPaymentBizInfoParametersForSimplifiedPromoAndSimplifiedItemEmiSubvention(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.XIAOMI4;

        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);

        User user = userManager.getForWrite(Label.BASIC);
        List<Map<String, String>> emiDetails;
        emiDetails = PGPHelpers.getEMIDetails(merchantType.getId(), "HDFC");
        String emiId = emiDetails.get(0).get("emiId");
        String emiPlanId = emiDetails.get(0).get("planId");
        String interestRate = emiDetails.get(0).get("interestRate");

        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item();
        item.setPrice("10");
        items.add(item);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .setSimplifiedSubvention(new SimplifiedSubvention(UUID.randomUUID().toString(), emiId, items))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptResponseDTO.set(FetchPaymentOption.executeFetchPaymtOption(
                merchantType.getId(), orderId, fetchPaymentOptionsDTO));
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, paymentDTO, PayMethodType.EMI)
                .setPlanId(emiPlanId)
                .setCardInfo("|" + paymentDTO.getCreditCardNumber() + "|618|092023")
                .setEMI_TYPE("CREDIT_CARD")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        String cmdToFetchCOPRequest = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST' | grep 'envInfo'";
        System.out.println(cmdToFetchCOPRequest);
        COPPAYRequest = Awaitility.await().until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchCOPRequest), s -> !"".equals(s));
        JsonPath jsonPath = new JsonPath(COPPAYRequest);

        //Validations for EMI related parameters
        Assertions.assertThat(jsonPath.getList("REQUEST.request.body.paymentBizInfo.paymentAuthenticationFlows")).as("List is not empty").isEmpty();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethod")).isEqualTo("EMI").as("payMethod mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isInternational")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.isSavedCard")).isNotBlank();
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.InterestRate")).isEqualTo(interestRate).as("interestRate mismatch");
        Assertions.assertThat(jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.network")).isNotBlank();

        //Actual values of EMI Subvention Parameters
        String actualEMIDiscount = jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.discount");
        String actualPlanID = jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.planID");
        String actualTenure = jsonPath.getString("REQUEST.request.body.paymentBizInfo.payMethodDetails[0].payMethodDetails.tenure");

        //Actual values of Promo Parameters
        String actualOfferCode = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerCode");
        String actualDiscount = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].discount");
        String actualOfferStatus = jsonPath.getString("REQUEST.request.body.paymentBizInfo.offerDetails[0].offerStatus");

        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());

        //Expected values of EMI subvention parameters
        JsonPath jsonPath1 = new JsonPath(peonResponse.getEmiSubventionInfo());
        String expectedPlanID = jsonPath1.getString("planId");
        String expectedTenure = jsonPath1.getString("tenure");
        String expectedEMIDiscount = jsonPath1.getString("gratificationDiscount");

        //Expected values of Promo parameters
        JsonPath jsonPath2 = new JsonPath(peonResponse.getPaymentPromoCheckoutData());
        String expectedOfferStatus = jsonPath2.getString("status");
        String expectedOfferCode = jsonPath2.getString("promocode");
        String expectedPromoDiscount = jsonPath2.getString("savings[0].savings");
        double amount = Double.parseDouble(expectedPromoDiscount) * 100;
        int cashbackAmount = (int) amount;

        //Validation for EMI subvention parameters
        Assertions.assertThat(actualEMIDiscount).isEqualTo(expectedEMIDiscount).as("EMI discount mismatch");
        Assertions.assertThat(actualPlanID).isEqualTo(expectedPlanID).as("planId mismatch");
        Assertions.assertThat(actualTenure).isEqualTo(expectedTenure).as("tenure mismatch");

        //Validations for promo parameters
        Assertions.assertThat(actualOfferCode).isEqualTo(expectedOfferCode).as("offerCode mismatch");
        Assertions.assertThat(actualDiscount).isEqualTo(String.valueOf(cashbackAmount)).as("cashback amount mismatch");//As per current handling, need to be fixed
        Assertions.assertThat(actualOfferStatus).isEqualTo(expectedOfferStatus).as("offerStatus mismatch");
    }
}