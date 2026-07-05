package scripts.subscription.postpaid;

import com.paytm.api.AOA.AddAcquiring;
import com.paytm.api.PreNotify;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.api.subscription.CheckStatus;
import com.paytm.api.subscription.PreNotifyStatus;
import com.paytm.api.subscription.StatusModify;
import com.paytm.api.subscription.SubscriptionCancel;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import io.qameta.allure.Feature;
import io.qameta.allure.Features;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.tools.ant.taskdefs.condition.Or;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.luaj.vm2.ast.Str;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.PostpaidSubscription.PostpaidRenewSubs;
import scripts.UPISubsMandate.UPIEnhancedTests;
import scripts.UPISubsMandate.UPIRenewSubs;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.POOJA;
import static com.paytm.appconstants.Constants.Owner.PRIYANKA;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

public class subscriptionOnPostpaid extends PGPBaseTest {


    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription creation with postpaid payment mode")
    public void SubscriptionCreationWithPostpaid(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PAYTMCC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Send PreNotify for subscription with postpaid ")
    public void PreNotifyForPostpaidSubscription(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBSCRIPTION_POSTPAID, "11", subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Postpaid available in FPO response when ENABLE_POSTPAID_ON_SUBSCRIPTIONS preference is added on merchant")
    public void EnablePostpaidOnSubscriptionFPOResponse(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId(), "ENABLE_POSTPAID_ON_SUBSCRIPTIONS", "Y");

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("11")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin("9871142201","888888");
        String postpaidPaymode = cashierPage.paymentContainer().getText();
        Assert.assertTrue(postpaidPaymode.contains("Paytm Postpaid"));
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Postpaid not available in FPO response for TXN Amount > 15000")
    public void PostpaidNotAvailableForTXNAmtGreaterThan15000(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        PGPHelpers.validate_MerchantPreference(Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId(), "ENABLE_POSTPAID_ON_SUBSCRIPTIONS", "Y");

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("15001")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespCode("01")
                .validateRespMsg("Amount exceeds maximum allowed amount for postpaid subscription, please try with another payment instrument.");

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription creation with postpaid payment mode with month frequency")
    public void SubscriptionCreationWithPostpaidMonth(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }


    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Payload pushed in Topic POSTPAID_STATUS_NOTIFY_TOPIC for subscription creation event ")
    public void payloadPushedToKafkaTopicForPostpaidSubs(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subscriptionLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString());
        Assert.assertTrue(subscriptionLogs.contains("Subscription payload pushed to kafka topic : POSTPAID_STATUS_NOTIFY_TOPIC"));
        Assert.assertTrue(subscriptionLogs.contains("ACTIVATE"));

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription created Successfully when PAYTM_DIGITAL_CREDIT passed in request")
    public void PostpaidSubsCreateSubs(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setPAYMENT_MODE_ONLY("PAYTM_DIGITAL_CREDIT")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String subscriptionLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString());
        Assert.assertTrue(subscriptionLogs.contains("Subscription created Successfully"));

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "OTP validation is required for postpaid subscription")
    public void PostpaidSubsOTPValidation(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setPAYMENT_MODE_ONLY("PAYTM_DIGITAL_CREDIT")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxPhoneNumber().assertVisible();
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription check status for postpaid transaction")
    public void SubscriptionCheckStatusPostpaid(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(mid)
                .setOrderID(orderID)
                .setSubsId(subsId)
                .execute().jsonPath();

      Assertions.assertThat(checkStatusResponse.getString("body.lastOrderStatus")).isEqualTo("SUCCESS");
      Assertions.assertThat(checkStatusResponse.getString("body.orderId")).isEqualTo(orderID);
      Assertions.assertThat(checkStatusResponse.getString("body.frequencyUnit")).isEqualTo("ONDEMAND");
      Assertions.assertThat(checkStatusResponse.getString("body.subsPaymentInstDetails.paymentMode")).isEqualTo("PAYTM_DIGITAL_CREDIT");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription checkStatus for subscription in INIT status")
    public void SubscriptionCheckStatusForINITSubs(@Optional("enhancedwap_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(mid)
                .setOrderID(orderID)
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.lastOrderStatus")).isEqualTo("PENDING");
        Assertions.assertThat(checkStatusResponse.getString("body.orderId")).isEqualTo(orderID);
        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("INIT");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription checkStatus for invalid subsID")
    public void SubscriptionCheckStatusForInvalidSubsID(@Optional("enhancedwap_revamp") String theme) throws Exception {

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String subsId = "12345";
        String OrderID = CommonHelpers.generateOrderId();
        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(mid)
                .setOrderID(OrderID)
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(checkStatusResponse.getString("body.resultInfo.status")).isEqualTo("FAILURE");
        Assertions.assertThat(checkStatusResponse.getString("body.resultInfo.message")).isEqualTo("Subscription Not Found");
    }


    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription cancel for postpaid transaction")
    public void SubscriptionCancelPostpaid(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        SubscriptionCancel subscriptionCancel = new SubscriptionCancel();
        JsonPath SubscriptionCancelResponse = subscriptionCancel
                .setMID(mid)
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(SubscriptionCancelResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(SubscriptionCancelResponse.getString("body.resultInfo.message")).isEqualTo("Subscription is unsubscribed successfully");
        Assertions.assertThat(SubscriptionCancelResponse.getString("body.subsId")).isEqualTo(subsId);

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription cancel for invalid subsID")
    public void SubscriptionCancelForInvalidSubsID(@Optional("enhancedwap_revamp") String theme) throws Exception {

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();

        String subsId = "12345";

        SubscriptionCancel subscriptionCancel = new SubscriptionCancel();
        JsonPath SubscriptionCancelResponse = subscriptionCancel
                .setMID(mid)
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(SubscriptionCancelResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(SubscriptionCancelResponse.getString("body.resultInfo.message")).isEqualTo("NO CONTENT FOUND ");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription cancel payload pushed to topic POSTPAID_STATUS_NOTIFY_TOPIC")
    public void SubscriptionCancelPOSTPAID_STATUS_NOTIFY_TOPIC(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        SubscriptionCancel subscriptionCancel = new SubscriptionCancel();
        JsonPath SubscriptionCancelResponse = subscriptionCancel
                .setMID(mid)
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(SubscriptionCancelResponse.getString("body.resultInfo.message")).isEqualTo("Subscription is unsubscribed successfully");

        String subscriptionLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString());
        Assert.assertTrue(subscriptionLogs.contains("Subscription payload pushed to kafka topic : POSTPAID_STATUS_NOTIFY_TOPIC"));

        String subscriptionLogsKafka = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString(),"POSTPAID_STATUS_NOTIFY_TOPIC");
        Assert.assertTrue(subscriptionLogsKafka.contains("CANCELLED"));
        Assert.assertTrue(subscriptionLogsKafka.contains("\"status\":\"REJECT\""));
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription checkStatus for cancelled transaction")
    public void SubscriptionCheckStatusForCancelled(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        SubscriptionCancel subscriptionCancel = new SubscriptionCancel();
        JsonPath SubscriptionCancelResponse = subscriptionCancel
                .setMID(mid)
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(SubscriptionCancelResponse.getString("body.resultInfo.message")).isEqualTo("Subscription is unsubscribed successfully");

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(mid)
                .setOrderID(orderID)
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(checkStatusResponse.getString("body.lastOrderStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(checkStatusResponse.getString("body.orderId")).isEqualTo(orderID);
        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("REJECT");
        Assertions.assertThat(checkStatusResponse.getString("body.subStatus")).isEqualTo("USER_CANCELLED");

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify postpaid subscription modify response for status: SUSPENDED")
    public void SubscriptionModifySUSPENDED(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("SUSPENDED")
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify check status response for SUSPENDED postpaid subscription")
    public void SubscriptionCheckStatusForSUSPENDED(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("SUSPENDED")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(mid)
                .setOrderID(orderID)
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(checkStatusResponse.getString("body.lastOrderStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(checkStatusResponse.getString("body.orderId")).isEqualTo(orderID);
        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("SUSPENDED");
        Assertions.assertThat(checkStatusResponse.getString("body.subStatus")).isEqualTo("MERCHANT_SUSPENDED");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify payload to POSTPAID_STATUS_NOTIFY_TOPIC with eventType : PAUSED for SUSPENDED subscription ")
    public void SubscriptionPayloadToPOSTPAID_STATUS_NOTIFY_TOPICforSUSPENDED(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("SUSPENDED")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

        String subscriptionLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString());
        Assert.assertTrue(subscriptionLogs.contains("Subscription payload pushed to kafka topic : POSTPAID_STATUS_NOTIFY_TOPIC"));

        String subscriptionLogsKafka = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString(),"POSTPAID_STATUS_NOTIFY_TOPIC");
        Assert.assertTrue(subscriptionLogsKafka.contains("PAUSED"));
        Assert.assertTrue(subscriptionLogsKafka.contains("\"status\":\"SUSPENDED\""));
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Activate suspended postpaid subscription with status modify")
    public void SubscriptionActivateForSuspendedSubs(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("SUSPENDED")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

        StatusModify statusModifyActive = new StatusModify();
        JsonPath StatusModifyActiveResponse = statusModifyActive
                .setMID(mid)
                .setStatus("ACTIVE")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyActiveResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify checkStatus response for activate suspended postpaid subscription")
    public void SubscriptionCheckStatusForActivateFromSuspended(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("SUSPENDED")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

        StatusModify statusModifyActive = new StatusModify();
        JsonPath StatusModifyActiveResponse = statusModifyActive
                .setMID(mid)
                .setStatus("ACTIVE")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyActiveResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

        CheckStatus checkStatus = new CheckStatus();
        JsonPath checkStatusResponse = checkStatus
                .setMID(mid)
                .setOrderID(orderID)
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(checkStatusResponse.getString("body.lastOrderStatus")).isEqualTo("SUCCESS");
        Assertions.assertThat(checkStatusResponse.getString("body.orderId")).isEqualTo(orderID);
        Assertions.assertThat(checkStatusResponse.getString("body.status")).isEqualTo("ACTIVE");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify payload is pushed to POSTPAID_STATUS_NOTIFY_TOPIC with eventType : ACTIVATE when SUSPENDED subs is activated")
    public void SubscriptionPayloadPushedToPOSTPAID_STATUS_NOTIFY_TOPICForACTIVATE(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("SUSPENDED")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

        StatusModify statusModifyActive = new StatusModify();
        JsonPath StatusModifyActiveResponse = statusModifyActive
                .setMID(mid)
                .setStatus("ACTIVE")
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(StatusModifyActiveResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");

        String subscriptionLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString());
        Assert.assertTrue(subscriptionLogs.contains("Subscription payload pushed to kafka topic : POSTPAID_STATUS_NOTIFY_TOPIC"));

        String subscriptionLogsKafka = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.subscription, orderDTO.getORDER_ID().toString(),"POSTPAID_STATUS_NOTIFY_TOPIC");
        Assert.assertTrue(subscriptionLogsKafka.contains("ACTIVATE"));
        Assert.assertTrue(subscriptionLogsKafka.contains("\"status\":\"ACTIVE\""));
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Subscription modify for invalid subsID")
    public void SubscriptionModifyForInvalidSubsID(@Optional("enhancedwap_revamp") String theme) throws Exception {

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();

        String subsId = "12345";

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("SUSPENDED")
                .setSubsId(subsId)
                .execute().jsonPath();

       Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("FAILURE");
       Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.message")).isEqualTo("System Error.");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify Status modify response for incorrect status passed")
    public void SubscriptionStatusModifyWithIncorrectStatus(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        String subsId = txnStatus.execute().jsonPath().getString("SUBS_ID");

        StatusModify statusModify = new StatusModify();
        JsonPath StatusModifyResponse = statusModify
                .setMID(mid)
                .setStatus("TESTED")
                .setSubsId(subsId)
                .execute().jsonPath();

        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.status")).isEqualTo("FAILURE");
        Assertions.assertThat(StatusModifyResponse.getString("body.resultInfo.message")).isEqualTo("Invalid status");

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "PreNotifyStatus for subscription with postpaid ")
    public void PreNotifyStatusForPostpaidSubscription(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201", "888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String referenceId = CommonHelpers.generateOrderId();
        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1) ;

        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBSCRIPTION_POSTPAID, "11", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");

        String mid = Constants.MerchantType.SUBSCRIPTION_POSTPAID.getId();
        String orderID = orderDTO.getORDER_ID();

        PreNotifyStatus preNotifyStatus = new PreNotifyStatus();
        JsonPath PreNotifyStatusResponse = preNotifyStatus
                .setMID(mid)
                .setReferenceId(referenceId)
                .setSubsId(subsId)
                .execute().jsonPath();
        Assertions.assertThat(PreNotifyStatusResponse.getString("body.resultInfo.status")).isEqualTo("SUCCESS");
        Assertions.assertThat(PreNotifyStatusResponse.getString("body.txnMessage")).isEqualTo("subscription for postpaid mobile bill");
        Assertions.assertThat(PreNotifyStatusResponse.getString("body.referenceId")).isEqualTo(referenceId);

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "subscription with postpaid acquirerName in payment request to postpaid")
    public void acquirerNameForPostpaidSubscriptionPaymentRequest(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201", "888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subscriptionLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID().toString(), "Payment Request");
        Assert.assertTrue(subscriptionLogs.contains("acquirerName"));

    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "subscription with postpaid settlementType in payment request to postpaid")
    public void settlementTypeForPostpaidSubscriptionPaymentRequest(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201", "888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String subscriptionLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderDTO.getORDER_ID().toString(), "Payment Request");
        Assert.assertTrue(subscriptionLogs.contains("settlementType"));
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Renew for subscription with postpaid ")
    public void RenewForPostpaidSubscription(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201", "888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        String referenceId = CommonHelpers.generateOrderId();
        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1) ;

        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBSCRIPTION_POSTPAID, "11", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        PostpaidRenewSubs.modifyNotifyDatesInDB(paytmRefId);
        String orderId = PostpaidRenewSubs.executeRenewalAndFetchOrderId(merchant, subsId, "11", "");
        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
    }


    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Renew Activated in DB for subscription with postpaid ")
    public void RenewActiveInDBForPostpaidSubscription(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201", "888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        String referenceId = CommonHelpers.generateOrderId();
        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1) ;

        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBSCRIPTION_POSTPAID, "11", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        PostpaidRenewSubs.modifyNotifyDatesInDB(paytmRefId);
       String orderId = PostpaidRenewSubs.executeRenewalAndFetchOrderId(merchant, subsId, "11", "");
       Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(subsId, orderId)).isNotNull();
       Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(subsId, orderId)).isEqualTo("ACTIVE");      }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Renewal fail when PDN for txnDate not available in subscription with postpaid ")
    public void renewFailForFutureDatePDNPostpaidSubscription(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201", "888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        String referenceId = CommonHelpers.generateOrderId();
        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        String date = CommonUtils.getdate("dd-MM-yyyy");
        String txnDate = CommonHelpers.addDays(date, "dd-MM-yyyy", 1) ;

        PreNotify preNotify = new PreNotify(Constants.MerchantType.SUBSCRIPTION_POSTPAID, "11", subsId ,txnDate,referenceId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS");

        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "11")
                .setRequestType("")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();

        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo("Prenotify not found for subscriptionId of this transaction date and amount");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result Message mismatch").isEqualTo("F");
    }

    @Owner(POOJA)
    @Parameters({"theme"})
    @Test(description = "Renewal fail when PDN not sent in subscription with postpaid ")
    public void renewFailForNoPDNPostpaidSubscription(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("1000")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(cashierPage.rememberMeCheckbox().isElementPresent())
        {
            cashierPage.rememberMeCheckbox().unCheck();
        }
        cashierPage.signin("9871142201", "888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "11")
                .setRequestType("")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();

        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch").isEqualTo("Prenotify not found for subscriptionId of this transaction date and amount");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result Message mismatch").isEqualTo("F");
    }

    @Owner(Constants.Owner.AKSHAT)
    @Feature("PGP-48530")
    @Parameters({"theme"})
    @Test(description = "BankTransactionId returned for Subs PostPaid txn ")
    public void PostPaidSubs_BankTxnId_returned(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_POSTPAID;

        OrderDTO orderDTO = new OrderFactory.PospaidSubs(merchant, theme)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin("9871142201","888888");
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PAYTMCC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PAYTMCC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .AssertAll();

    }
}
