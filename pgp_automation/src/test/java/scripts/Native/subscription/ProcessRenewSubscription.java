package scripts.Native.subscription;

import com.paytm.ServerConfigProvider;
import com.paytm.api.CreateSubscription;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.api.nativeAPI.SubsMandateCallback;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.api.PreNotify;
import io.restassured.response.Response;
import java.time.LocalDateTime;

@Owner("Tarun")
@Owners(author = "Tarun", qa = "Ankur")
public class ProcessRenewSubscription extends PGPBaseTest {

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=MONTH")
    public void TC_NRS001_ProcessRenewSubs_VAR_CC_MONTH(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success native renew subs when " +
//            "PAYMENT_MODE=DC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=MONTH")
    public void TC_NRS001_ProcessRenewSubs_VAR_DC_MONTH(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.DEBIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirement_id is null").isNotNull();
        softAssertions.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify success native renew subs when " +
            "PAYMENT_MODE=PPI and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=MONTH")
    public void TC_NRS001_ProcessRenewSubs_VAR_PPI_MONTH(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.BALANCE)
                .pay();

        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subscription.getSubsId()), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subscription.getSubsId(), PreviousDate);
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subscription.getSubsId());
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subscription.getSubsId()), LocalDateTime.now());
        modifySubsDatesInDB(subscription.getSubsId(), frequencyReqDate);
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");

        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=FIX and FREQ_UNIT=MONTH")
    public void TC_NRS002_ProcessRenewSubs_FIX_CC_MONTH(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify failure of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=FIX and FREQ_UNIT=MONTH and renew with different amount")
    public void TC_NRS002_Fail_ProcessRenewSubs_FIX_CC_MONTH(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), "5.0")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("905");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Payment request amount does not match the Subscription contract");
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=MONTH and renew with less than max amount")
    public void TC_NRS003_ProcessRenewSubs_LessAmount_VAR_CC_MONTH(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), "5")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("trans_amount", subsId, orderId))
                .as("trans_amount mismatch").isEqualToIgnoringCase("500.0");
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify failure of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=MONTH and renew with greater than max amount")
    public void TC_NRS003_FailProcessRenewSubs_MorethanMaxAmount_VAR_CC_MONTH(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), "11")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("913");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Payment request amount is not allowed as per the subscription contract");
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=DAY")
    public void TC_NRS004_ProcessRenewSubs_VAR_CC_DAY(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=YEAR")
    public void TC_NRS005_ProcessRenewSubs_VAR_CC_YEAR(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK")
    public void TC_NRS006_ProcessRenewSubs_VAR_CC_WEEK(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=QUARTER")
    public void TC_NRS007_ProcessRenewSubs_VAR_CC_QUARTER(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=BI_MONTHLY")
    public void TC_NRS008_ProcessRenewSubs_VAR_CC_BI_MONTHLY(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=SEMI_ANNUALLY")
    public void TC_NRS009_ProcessRenewSubs_VAR_CC_SEMI_ANNUALLY(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=ONDEMAND")
    public void TC_NRS010_ProcessRenewSubs_VAR_CC_ONDEMAND(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }

//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify success of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=null and FREQ=0 and START_DATE=''")
    public void TC_NRS011_ProcessRenewSubs_VAR_CC_WEEK_startDateblank(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays(null)
                .setSubscriptionStartDate("")
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();
        String startDate = subscription.getSubsStartDate();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }

//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify fail of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=0 and FREQ=0 and START_DATE=is greater than today date")
    public void TC_NRS012_ProcessRenewSubs_VAR_CC_WEEK_startDateGreaterThanToday(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().plusDays(1).toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("1104");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Validation failed");
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
 //   @Issue("PGP-15557")
 //   @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify fail of native renew subs when " +
 //           "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=0 and FREQ=1 and txnAmount is 0")
    public void TC_NRS013_ProcessRenewSubs_VAR_CC_WEEK_txnAmt0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("501");
        softAssertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("System Error");
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify fail of native renew subs when " +
//            "PAYMENT_MODE=CC and AMOUNT_TYPE=VARIABLE and FREQ_UNIT=WEEK and GRACE_DAY=0 and FREQ=2")
    public void TC_NRS014_VAR_CC_WEEK_executeRenewMorethanOnce(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("2")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .pay();
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");

        RenewSubscriptionDTO renewSubscriptionDTO1 = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        JsonPath jsonPath1 = new RenewSubscription(renewSubscriptionDTO1).execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus"))
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("F");
        softAssertions.assertThat(jsonPath1.getString("body.resultInfo.resultCode"))
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("931");
        softAssertions.assertThat(jsonPath1.getString("body.resultInfo.resultMsg"))
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Payment request is already pending in this frequency cycle");
        softAssertions.assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify that a subscription is created with blank paymode and proceed with CC, fetchPayoptions and renewal")
    public void blankWithCC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
               // .setAppInvokeDevice("3P")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify that a subscription is created with blank paymode and proceed with DC, fetchPayoptions and renewal")
    public void subsBlankWithDC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
              //  .setAppInvokeDevice("3P")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.DEBIT_CARD)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify that a subscription is created with blank paymode and proceed with PPI, fetchPayoptions and renewal")
    public void subsBlankWithPPI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String txnAmount = "1.0";
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                //  .setAppInvokeDevice("3P")
                .build();
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.BALANCE)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay();
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        LocalDateTime PreviousDate = LocalDateTime.now().minusMonths(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subscription.getSubsId()), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subscription.getSubsId(), PreviousDate);
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subscription.getSubsId());
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusMonths(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subscription.getSubsId()), LocalDateTime.now());
        modifySubsDatesInDB(subscription.getSubsId(), frequencyReqDate);
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }

    //Getting Incorrect passcode(Tried with 1234, 5335) TODO manual qa - Akshat
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that a subscription is created with blank paymode and proceed with PPBL, fetchPayoptions and renewal")
    public void subsWithPPBL(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                //  .setAppInvokeDevice("3P")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.PPBL)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify that a subscription is created with CC paymode and proceed with CC, fetchPayoptions and renewal")
    public void subsWithCCAndFPO(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                //  .setAppInvokeDevice("3P")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }


    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify that a subscription is created with DC paymode and proceed with DC, fetchPayoptions and renewal")
    public void subsWithDCAndFPO(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                //  .setAppInvokeDevice("3P")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.DEBIT_CARD)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }

    //Getting Incorrect passcode(Tried with 1234, 5335) TODO manual qa - Akshat
    @Parameters({"isNativePlus"})
    @Test(description = "Verify that a subscription is created with PPBL paymode and proceed with PPBL, fetchPayoptions and renewal")
    public void subsWithPPBLAndFPO(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPBL")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                //  .setAppInvokeDevice("3P")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.PPBL)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();
    }


    //Deprecated CC/DC Flow after SI Hub Release
//    @Parameters({"isNativePlus"})
//    @Test(enabled = false, description = "Verify that subscription payment is successful without passing subscriptionId in PTC")
    public void subsPassedWithoutSubsIdInPTC(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                //  .setAppInvokeDevice("3P")
                .build();

        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.CREDIT_CARD)
                .fetchPayOptions(merchant,initTxnDTO.orderFromBody())
                .pay("");


    }


    //PGP-21763 Verify Different Subscription Error Messages

    @Test(description = "Verify that message Length Validation failed for Subscription ID")
    public void verifySubsIDLengthValidationFailedMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), "", initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("Result Code mismatch")
                .isEqualToIgnoringCase("501");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch")
                .isEqualToIgnoringCase("Length Validation failed for Subscription ID");
    }

    @Test(description = "Verify that message Length Validation failed for Transaction Amount" )
    public void verifyTxnAmountLengthValidationFailedMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId)
                .setAUTH_MODE("USRPWD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("Result Code mismatch")
                .isEqualToIgnoringCase("501");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch")
                .isEqualToIgnoringCase("Transaction Amount is mandatory");
    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner("Jai")
//    @Test(enabled = false, description = "Verify that Invalid SavedCardID is displayed ")
    public void verifyInvalidSavedCardIDMessage() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String savedCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("FIX")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("2")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        paymentDTO.setSavedCardId(savedCardId);
        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId, paymentDTO)
                .setAUTH_MODE("3D")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName("HDFC")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        SavedCardHelpers.deleteSavedCard(user);
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("F");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode"))
                .as("Result Code mismatch")
                .isEqualToIgnoringCase("313");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg"))
                .as("Result Message mismatch")
                .isEqualToIgnoringCase("Invalid SavedCardID");
    }

    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }


    //Pre Debit Date
    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.TARUN)
//    @Feature("PGP-27716")
//    @Test(enabled = false, description = "Verify that a MONTH subscription is successfully renewed with debitDate and it's order is created with CC paymode")
    public void verifyRenewCCMONTHDD(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId,paymentDTO)
                .setAUTH_MODE("3D")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        // First renew to increase subs_due_date

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        // Wait so that contract_v2 sub_due_date is updated

        PGPHelpers.pause(8);
        String subsDate = PGPHelpers.getSubsDate(subsId);

        //Second renew with debitDate
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .setDebitDate(subsDate)
                .build();
         RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
         JsonPath renew2 =renewSubscription.execute().jsonPath();

        Assertions.assertThat(renew2.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        //Order should be created
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\" ";
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("SUCCESS");

    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.TARUN)
//    @Feature("PGP-27716")
//    @Test(enabled = false, description = "Verify that a QUARTER subscription is successfully renewed with debitDate and it's order is created with DC paymode")
    public void verifyRenewQUARTERDDDC(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("DC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("QUARTER")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.DEBIT_CARD, subsId,paymentDTO)
                .setAUTH_MODE("otp")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSubsid(subsId)
                .AssertAll();

        //First renew to increase subs_due_date

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        // Wait so that contract_v2 sub_due_date is updated

        PGPHelpers.pause(8);
        String subsDate = PGPHelpers.getSubsDate(subsId);

        //Second renew with debitDate
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .setDebitDate(subsDate)
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renew2 =renewSubscription.execute().jsonPath();

        Assertions.assertThat(renew2.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        //Order should be created
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\" ";
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("SUCCESS");

    }


    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-27716")
    @Test(description = "Verify that a WEEK subscription is successfully renewed with debitDate and it's order is created with DC paymode")
    public void verifyRenewWEEKDDDC(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_ADDNPAY;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubsPPIOnly("Y")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("WEEK")
                .setSubscriptionGraceDays("5")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        WalletHelpers.modifyBalance(user,Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()));
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.SUBSCRIPTION_ADDNPAY, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.BALANCE, subsId,paymentDTO)
                .setAUTH_MODE("USRPWD")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateSubsid(subsId)
                .AssertAll();

        WalletHelpers.modifyBalance(user,Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()));
        LocalDateTime PreviousDate = LocalDateTime.now().minusWeeks(1).plusDays(2);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now().plusDays(2));
        modifySubsDatesInDB(subsId, PreviousDate);
        PreNotify preNotify = new PreNotify(merchant, initTxnDTO.txnAmountFromBody(), subsId);
        Response response = preNotify.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.status"))
                .isEqualTo("SUCCESS").as("PreNotify is Not successful for Given Dates");
        String paytmRefId = response.jsonPath().getString("body.paytmReferenceId");
        modifyNotifyDatesInDB(paytmRefId);
        LocalDateTime frequencyReqDate = LocalDateTime.now().minusWeeks(1);
        PGPHelpers.modifySubscriptionDueDate(Long.valueOf(subsId), LocalDateTime.now());
        modifySubsDatesInDB(subsId, frequencyReqDate);
        //First renew to increase subs_due_date

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.getBody().getTxnAmount().getValue())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        // Wait so that contract_v2 sub_due_date is updated

        PGPHelpers.pause(8);
        String subsDate = PGPHelpers.getSubsDate(subsId);

        WalletHelpers.modifyBalance(user,Double.valueOf(initTxnDTO.getBody().getTxnAmount().getValue()));

        //Second renew with debitDate
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.getBody().getTxnAmount().getValue())
                .setMerchantKey(merchant.getKey())
                .setDebitDate(subsDate)
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renew2 =renewSubscription.execute().jsonPath();

        Assertions.assertThat(renew2.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        //Order should be created
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\" ";
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("SUCCESS");

    }


    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-27716")
    @Test(description = "Verify that a BIMONTHLY subscription is successfully renewed with debitDate and it's order is created with PPBL")
    public void verifyRenewBIMONTHLYDDCC(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("BI_MONTHLY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PPBL, subsId, paymentDTO)
                .setAUTH_MODE("MPIN")
                .setMpin("1234")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateBankName(Constants.Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateSubsid(subsId)
                .AssertAll();

        // First renew to increase subs_due_date

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        // Wait so that contract_v2 sub_due_date is updated

        PGPHelpers.pause(8);
        String subsDate = PGPHelpers.getSubsDate(subsId);

        //Second renew with debitDate
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .setDebitDate(subsDate)
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renew2 =renewSubscription.execute().jsonPath();

        Assertions.assertThat(renew2.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        //Order should be created
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\" ";
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("SUCCESS");

    }
    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.TARUN)
//    @Feature("PGP-27716")
//    @Test(enabled = false, description = "Verify that a SEMI ANNUALLY subscription is successfully renewed with debitDate and it's order is created with CC paymode")
    public void verifyRenewSEMIANNUALLYDDCC(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("SEMI_ANNUALLY")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId,paymentDTO)
                .setAUTH_MODE("3D")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        // First renew to increase subs_due_date

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        // Wait so that contract_v2 sub_due_date is updated

        PGPHelpers.pause(8);
        String subsDate = PGPHelpers.getSubsDate(subsId);

        //Second renew with debitDate
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .setDebitDate(subsDate)
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renew2 =renewSubscription.execute().jsonPath();

        Assertions.assertThat(renew2.getString("body.resultInfo.resultMsg")).isEqualTo("The Debit Date is invalid");


    }

    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-27716")
    @Test(description = "Verify that a ONDEMAND subscription is successfully renewed with debitDate and it's order is created with BM paymode")
    public void verifyRenewBMONDEMANDDD(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();

        JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");

        // First renew to increase subs_due_date

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "1.0")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        // Wait so that contract_v2 sub_due_date is updated

        PGPHelpers.pause(8);
        String subsDate = PGPHelpers.getSubsDate(subsId);

        //Second renew with debitDate
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "1.0")
                .setMerchantKey(merchant.getKey())
                .setDebitDate(subsDate)
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renew2 =renewSubscription.execute().jsonPath();

        Assertions.assertThat(renew2.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        //Order should be created
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\" ";
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("SUCCESS");

    }

    //Deprecated CC/DC Flow after SI Hub Release
//    @Owner(Constants.Owner.TARUN)
//    @Feature("PGP-27716")
//    @Test(enabled = false, description = "Verify that a DAY subscription is successfully renewed with debitDate and it's order is created with CC paymode")
    public void verifyRenewCCDAYDD(@Optional("false") Boolean isNativePlus) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);

        Constants.MerchantType merchant = Constants.MerchantType.Subscription_PGOnly;
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("CC")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("DAY")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.SubscriptionNative(Constants.MerchantType.Subscription_PGOnly, initTxnDTO.getBody().getOrderId(), txnToken, PayMethodType.CREDIT_CARD, subsId,paymentDTO)
                .setAUTH_MODE("3D")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateSubsid(subsId)
                .AssertAll();

        // First renew to increase subs_due_date

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        // Wait so that contract_v2 sub_due_date is updated

        PGPHelpers.pause(8);
        String subsDate = PGPHelpers.getSubsDate(subsId);

        //Second renew with debitDate
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "2.0")
                .setMerchantKey(merchant.getKey())
                .setDebitDate(subsDate)
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath renew2 =renewSubscription.execute().jsonPath();

        Assertions.assertThat(renew2.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");

        //Order should be created
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() +"\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"RESPONSE\" ";
        String theiaFacadeLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("SUCCESS");


    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag on success native renew subs when PAYMENT_MODE=PPI")
    public void VerifyFlexiTrueInSubsContractV2ForSubsRenewalWithPPI() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String SubscriptionStartDate = CommonHelpers.getDate().toString();
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("PPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("1")
                .setSubscriptionStartDate(SubscriptionStartDate)
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));
        CreateSubscription subscription = new CreateSubscription(initTxnDTO, merchant)
                .paymethodType(PayMethodType.BALANCE)
                .pay();

        WalletHelpers.modifyBalance(user, Double.valueOf(initTxnDTO.txnAmountFromBody()));
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subscription.getSubsId(), initTxnDTO.txnAmountFromBody())
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        JsonPath jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");

        String subsId = subscription.getSubsId();
        String orderId = renewSubscriptionDTO.getBody().getOrderId();

        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch").isEqualToIgnoringCase("RENEWAL");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch").isEqualToIgnoringCase("ACTIVE");
        softAssertions
                .assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("acquirement_id", subsId, orderId))
                .as("acquirementId is null").isNotNull();
        softAssertions.assertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, initTxnDTO.orderFromBody()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-29589")
    @Test(description = "Verify Flexi:true flag on success native renew subs when PAYMENT_MODE=BANK_MANADATE")
    public void VerifyFlexiTrueInSubsContractV2ForSubsRenewalWithBankMandate() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FLEXI_SUBSCRIPTION;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();

        JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");

        RenewSubscriptionDTO renewSubscriptionDTOO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, "1.0")
                .setMerchantKey(merchant.getKey())
                .build();
        RenewSubscription renewSubscription0 = new RenewSubscription(renewSubscriptionDTOO);
        JsonPath renew = renewSubscription0.execute().jsonPath();

        Assertions.assertThat(renew.getString("body.resultInfo.resultMsg")).isEqualTo("Subscription Txn accepted.");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata", subsId, initTxnDTO.orderFromBody()))
                .as("flexi flag is expected to be true")
                .contains("\"flexi\":true");
    }
    public static void modifySubsDatesInDB(String subsId, LocalDateTime FreqDate) {
        PGPHelpers.modifySubscriptionStartDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionPaymentCreateDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionPaymentUpdateDate(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionUpidetailCreateTime(Long.valueOf(subsId), FreqDate);
        PGPHelpers.modifySubscriptionUpidetailUpdateTime(Long.valueOf(subsId), FreqDate);
    }
    public static void modifyNotifyDatesInDB(String paytmRefId) {
        PGPHelpers.modifySubscriptionPreNotifyStatus(Long.valueOf(paytmRefId), "1");
        PGPHelpers.modifySubscriptionPreNotifyDate(Long.valueOf(paytmRefId), LocalDateTime.now().minusDays(2));
        PGPHelpers.modifySubscriptionPreNotifyTxnDate(Long.valueOf(paytmRefId), LocalDateTime.now());
    }

}
