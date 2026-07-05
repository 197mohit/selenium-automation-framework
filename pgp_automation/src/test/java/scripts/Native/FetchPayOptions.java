package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.CardDetails;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.InternationalSavedCard;

@Owner("Deepak")
public class FetchPayOptions extends PGPBaseTest implements InternationalSavedCard {

    @BeforeTest
    public void disableScreenShotCapture() {
        DriverManager.setCaptureScreenShot(false);
    }

    @Test(description = "Verify success case when valid txn_token is passed.")
    public void TC_FPI_001() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .containsExactlyInAnyOrder("CREDIT_CARD", "DEBIT_CARD", "NET_BANKING", "UPI", "EMI");
    }

    @Test(description = "Verify Session expired case when valid txn_token is passed and Txn_token is deleted from redis.")
    public void TC_FPI_002() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_CLUSTER_URI).del(txnToken);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg"))
                .isEqualTo("Your Session has expired.");
    }

    @Test(description = "Verify instruments Types when SSO token is passed")
    public void TC_FPI_026() throws Exception {
        User user = userManager.getForWrite(Label.PPBL, Label.POSTPAID);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.Hybrid).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD", "DEBIT_CARD", "NET_BANKING", "UPI", "BALANCE", "EMI", "PPBL");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.savedInstruments.findAll { savedInstruments -> savedInstruments.isDisabled.status == status }.cardDetails.cardId")
                .get(0)).isEqualTo(SavedCardHelpers.getSavedCardId(user, 0));
    }

    @Test(description = "Verify instruments Types when SSO token is not passed")
    public void TC_FPI_027() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .containsExactlyInAnyOrder("CREDIT_CARD", "DEBIT_CARD", "NET_BANKING", "UPI", "EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments"))
                .isEqualTo("[]");
    }

    @Test(description = "Verify Only CC is returned as saved Instruments, pay methods and BALANCE is not returned when CC is passed in instrument Types and  SSO token is passed.")
    public void TC_FPI_030() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"VISA"}, "CREDIT_CARD");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .containsExactlyInAnyOrder("CREDIT_CARD");
    }

    @Test(description = "Verify that savedInstruments is empty if CC is passed in instrument Types when SSO token is not passed")
    public void TC_FPI_031() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getCreditCardNumber());
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"VISA"}, "CREDIT_CARD");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .containsExactlyInAnyOrder("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments"))
                .isEqualTo("[]");
    }

    @Test(description = "Verify that null value returned in paymodes and SavedInstruments when CC is passed in enabledPaymentModes and disabledPaymentModes as well when SSO token is not passed.")
    public void TC_FPI_036() throws Exception {
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{}, "CREDIT_CARD");
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{}, "CREDIT_CARD");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode})
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .setCustId(CommonHelpers.generateOrderId())
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(
                fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes")).isEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments")).isEmpty();
    }

    @Test(description = "Verify Saved pay options and paymodes are returned null when CC is passed in enable paymode and disabledPaymentModes as well when SSO token is passed.")
    public void TC_FPI_037() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getDebitCardNumber());
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{}, "CREDIT_CARD");
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{}, "CREDIT_CARD");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode})
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes")).isEmpty();
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments")).isEmpty();
    }

    //pending
    @Test(description = "Verify only CC SavedInstruments are returned when CC is passed in enabled paymodes and ALL is passed in SavedInstrument types when SSO token is passed.")
    public void TC_FPI_038() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getDebitCardNumber());
        String dcCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String ccCardId = SavedCardHelpers.getSavedCardId(user, 1);
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{}, "CREDIT_CARD");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .containsExactlyInAnyOrder("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.savedInstruments.findAll { savedInstruments -> savedInstruments.isDisabled.status == status }.cardDetails.cardId"))
                .containsExactlyInAnyOrder(ccCardId);
    }

    // As discussed TC_FPI_040 is no more applicable

    @Test(description = "Verify all specified saved cards are returned when passing multiple values in saved card instrument")
    public void TC_FPI_041() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getDebitCardNumber());
        String ccCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String dcCardId = SavedCardHelpers.getSavedCardId(user, 1);

        String cardIndex1 = SavedCardHelpers.getCIN(ccCardId);
        String cardIndex2 = SavedCardHelpers.getCIN(dcCardId);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                // .setSavedInstrumentsTypes(new String[]{"CC", "DC"})
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.savedInstruments.findAll { savedInstruments -> savedInstruments.isDisabled.status == status }.cardDetails.cardId"))
                .containsExactlyInAnyOrder(cardIndex1, cardIndex2);
    }

    @Test(description = "Verify null value must be returned in saved instruments when passed values in savedInstruments which are not saved on user")
    public void TC_FPI_042() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, paymentDTO.getExpYear(), paymentDTO.getExpMonth(),
                paymentDTO.getDebitCardNumber());
        String ccCardId = SavedCardHelpers.getSavedCardId(user, 0);
        String dcCardId = SavedCardHelpers.getSavedCardId(user, 1);
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"WEB"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList("body.merchantPayOption.savedInstruments").size())
                .isZero();
    }

    //disabling test case as objective was not clear to manual team
//    @Test(description = "Verify data of extended info", enabled = false)
    public void TC_FPI_043() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID)
                .setChannelId(null).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        throw new SkipException("Extend info details required");
    }

    @Test(description = "Verify behaviour of addMoneyPayMethods")
    public void TC_FPI_056() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_ADDNPAY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.addMoneyPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD", "DEBIT_CARD", "NET_BANKING", "UPI");
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify saved VPA if UPI is enabled. ", enabled = false)
    //As discussed with mohit functionality is not valid any more.
    public void TC_FPI_057(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        performUPITxn(theme, user, MerchantType.NATIVE_HYBRID);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                "body.merchantPayOption.savedInstruments.findAll { savedInstruments -> savedInstruments.isDisabled.status == status }.cardDetails.cardType"))
                .containsExactlyInAnyOrder("UPI");
    }

    @Test(description = "Verify Top 5 net banking banks should be returned.")
    public void TC_FPI_058() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("payMode", "Net Banking").getList(
                "body.merchantPayOption.paymentModes.find { paymentModes -> paymentModes.displayName == payMode }.payChannelOptions")).hasSize(1);
    }

    @Test(description = "Verify failure when only wallet enabled on merchant and SSO token is not passed in create transaction request ")
    public void TC_FPI_059() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_WALLET_ONLY).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("payMode", "BALANCE").getList(
                "body.merchantPayOption.paymentModes.find { paymentModes -> paymentModes.paymentMode == payMode }.payChannelOptions.isDisabled.status")
                .get(0).toString()).isEqualToIgnoringCase("true");
    }

    @Test(description = "Verify icoUrl is not null in response ")
    public void TC_FPI_060() throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("payMode", "NET_BANKING").getList(
                "body.merchantPayOption.paymentModes.find { it.paymentMode == payMode }.payChannelOptions.iconUrl")).hasSize(2);

    }

    @Parameters({"theme"})
    @Test(description = "Verify SavedVPA and UPI as paymode should not be returned case when passing UPI in disabledPaymodes")
    public void TC_FPI_067(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);

        performUPITxn(theme, user, MerchantType.NATIVE_HYBRID);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), MerchantType.NATIVE_HYBRID)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("UPI")})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getList(
                "body.merchantPayOption.paymentModes.displayName")).doesNotContain("BHIM UPI");
        Assertions.assertThat(fetchPaymentOptionsJson.getList(
                "body.merchantPayOption.savedInstruments")).hasSize(0);
    }

    @Test(description = "Verify BALANCE paymethod is false when SSO Token is not available in initiate transaction")
    public void TC_FPI_066_1() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", MerchantType.NATIVE_HYBRID)
                .setCustId(CommonHelpers.generateOrderId())
                .build();
        String txnToken = InitTxn.executeInitTxn(initTxnDTO).getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = FetchPaymentOption.executeFetchPaymtOption(
                MerchantType.NATIVE_HYBRID.getId(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);

        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getMerchantPayOption().getSavedInstruments()).isEmpty();
    }

    @Test(description = "Verify savedInstrument is null when SSO Token is not available in initiate transaction")
    public void TC_FP1_066_2() {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", MerchantType.NATIVE_HYBRID)
                .setCustId(CommonHelpers.generateOrderId())
                .build();
        String txnToken = InitTxn.executeInitTxn(initTxnDTO).getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = FetchPaymentOption.executeFetchPaymtOption(
                MerchantType.NATIVE_HYBRID.getId(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);

        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getMerchantPayOption().getSavedInstruments()).isEmpty();
    }

    @Test(description = "Validate saved cards in fetchPaymentOptions api when card is saved on custId and MID")
    public void TC_FP1_067() throws Exception {
        String custId = CommonHelpers.generateOrderId();
        SavedCardHelpers helper = new SavedCardHelpers();
        String cardNumber = new PaymentDTO().getCreditCardNumber();
        SaveCardResponseBase resp = helper.saveCard_custId_mId(cardNumber, custId, MerchantType.Hybrid.getId(), new PaymentDTO().getExpMonth() + new PaymentDTO().getExpYear());

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", MerchantType.Hybrid)
                .setCustId(custId)
                .build();

        String txnToken = InitTxn.executeInitTxn(initTxnDTO).getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = FetchPaymentOption.executeFetchPaymtOption(
                MerchantType.Hybrid.getId(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);

        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getMerchantPayOption().getSavedInstruments()).as("SavedInstruments is not null").isNotNull();
        Assertions.assertThat(fetchPaymentOptResponseDTO.getBody().getMerchantPayOption().getSavedInstruments()).as("SavedInstruments size mismatch").hasSize(1);
        CardDetails cardDetails = fetchPaymentOptResponseDTO.getBody().getMerchantPayOption().getSavedInstruments().get(0).getCardDetails();
        Assertions.assertThat(cardDetails.getCardId()).as("Card Id is null").isNotNull();
        Assertions.assertThat(cardDetails.getCardType()).as("CARD_TYPE mismatch").isEqualTo("CREDIT_CARD");
        Assertions.assertThat(cardDetails.getFirstSixDigit()).as("firstSixDigit mismatch").isEqualTo(cardNumber.substring(0, 6));
        Assertions.assertThat(cardDetails.getLastFourDigit()).as("lastFourDigit mismatch").isEqualTo(cardNumber.substring(cardNumber.length() - 4));
        Assertions.assertThat(cardDetails.getStatus()).as("status mismatch").isEqualTo("1");
    }

    private void performUPITxn(String theme, User user, MerchantType merchantType) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme).build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.UPI);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateStatus("TXN_SUCCESS").AssertAll();
    }

    @Test(description = "Validate merchant logo url is returned in fetchPayoptions when merchant logo is set for merchant")
    public void merchantLogoUrlPresent() throws Exception {
        //User user = userManager.getForWrite(Label.BASIC);
        //   SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.LOGOMerchant).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantDetails.merchantLogo")).contains("14900056265068");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantDetails.merchantLogo")).isEqualTo("https://merchant-static.paytm.com/merchant-dashboard/logo/14900056265068/org/logo");
    }

    @Test(description = "Validate merchant logo url is not returned in fetchPayoptions when merchant logo is not set for merchant")
    public void merchantLogoUrlNotPresent() throws Exception {
        //User user = userManager.getForWrite(Label.BASIC);
        //   SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchantType.NATIVE_HYBRID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath jsonPath = initTxn.execute().jsonPath();
        String txnToken = jsonPath.getString("body.txnToken");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.merchantDetails.merchantLogo")).isNull();
    }


    @Test(description = "Native : PG side: Non Logged In Flow : Verify that international card is visible in fpo on international supported MID")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardNonLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.disableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, internationalMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardId);

    }


    @Test(description = "Native P+ side : Non Logged In Flow  : Verify that international card is visible on fpo on international supported MID")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardNonLoggedInFlowPPlusSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

       SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;

        //Deleting for Merchant on P+
        SavedCardHelpers.deleteSavedCardsAlipay(internationalMerchant.getId(),custId);

        //Adding for MID CustId on P+
       String cin = SavedCardHelpers.addCardAlipay(internationalMerchant.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from P+  should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cin);



    }

    @Test(description = "Native : PG side : Logged In Flow : Verify that international card is visible on fpo on international supported MID")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.disableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for User on PGPDB
       SavedCardHelpers.deleteSavedCard(user);

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String cardId = SavedCardHelpers.getSavedCardId(user,0);
        String cardIndex = SavedCardHelpers.getCIN(cardId);
        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardIndex);

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardIndex);

    }


    @Test(description = "P+ side : Logged In Flow : Verify that international card is visible on cashier page on international supported MID & SUCCESS txn using International Card CIN")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardLoggedInFlowPPlus(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for User on P +
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for User on  P +
        String cin = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cin);

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cin);

        paymentDTO.setSavedCardId(cin);

        OrderDTO orderDTO = new OrderFactory.Native(internationalMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();

        //PTC

        checkoutPage.createNativeOrder(orderDTO, true);
        PGPHelpers.validateSuccessResponsePage(orderDTO,internationalMerchant,Constants.Gateway.IHDF.toString(), Constants.Bank.HDFC_ONLY.toString(), "CC");
        PGPHelpers.validateSuccessTxnStatus(orderDTO,"CC", Constants.Bank.HDFC_ONLY.toString(), Constants.Gateway.IHDF.toString());
    }

    @Test(description = "PG side : Logged In & Non Logged In Flow : Verify that international card is not visible on cashier page on international non supported MID")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void internationalCardNotVisibleLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = MerchantType.PGOnly;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.disableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for User on PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

    // Saved Cards are coming from P+ Side as cards have been migrated and related FF4j Flags are enabled, Hence disabling this Test Case
//    @Test(enabled = false, description = "PG side : Logged In & Non Logged In Flow : Verify that international card is not visible on cashier page on international non supported MID")
//    @Feature("PGP-23196")
 //   @Owner("Tarun")
 //   @Epic(Constants.Sprint.SPRINT33_2)
//    @Description("Automation JIRA : PGP-26960")
//    @Override
    public void internationalCardNotVisibleLoggedInFlowPPlus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType internationalMerchant = MerchantType.PGOnly;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), internationalMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(internationalMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(internationalMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Card Id from PGPDB should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

    @Test(description = "P + , PGP side : Recon Success : Bajaj fn card is getting filtered from both the sides")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void bajajFinservFilteringAlipay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType bajajfinemi = MerchantType.BAJAJFINEMI;
        SavedCardHelpers.assertStoreCardPrefEnabled(bajajfinemi);

        //MID/CustId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForMidCustId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");

        //UserId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForUserId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForUserId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for user on PG side
        SavedCardHelpers.deleteSavedCard(user);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on PGP Side
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding for MID CustId on PGP side
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, bajajfinemi.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(bajajfinemi.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), bajajfinemi)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Bajaj Fn Card should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(bajajfinemi.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(bajajfinemi.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Bajaj Fn Card should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

    @Test(description = "PG side: Verify that prepaid card should not be visible on cashier page if mid doesnt supoort it")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void prepaidCardNotVisibleUnsupportedMidPG(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType nonPrepaidMerchant = MerchantType.Hybrid;
        SavedCardHelpers.assertStoreCardPrefEnabled(nonPrepaidMerchant);
        PGPHelpers.validate_MerchantPreference(nonPrepaidMerchant.getId(),"PREPAID_CARD","N");

        SavedCardHelpers.disableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Deleting for User on  PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, nonPrepaidMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nonPrepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid Card should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(nonPrepaidMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(nonPrepaidMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Prepaid Card should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

    @Test(description = "P + : Verify that prepaid card should not be visible on cashier page if mid doesnt supoort it. No matter weather fetched from P+ or PG end.")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void prepaidCardNotVisibleUnsupportedMid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType nonPrepaidMerchant = MerchantType.Hybrid;
        SavedCardHelpers.assertStoreCardPrefEnabled(nonPrepaidMerchant);
        PGPHelpers.validate_MerchantPreference(nonPrepaidMerchant.getId(),"PREPAID_CARD","N");

        SavedCardHelpers.enableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(nonPrepaidMerchant.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), nonPrepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid card should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(nonPrepaidMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(nonPrepaidMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Prepaid Card should not be visible
        Assertions.assertThat(fetchPaymentOptResponse.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).isNullOrEmpty();


    }

    @Test(description = "PG side: Verify that prepaid card should  be visible in FPO if mid supoorts it")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void prepaidCardVisibleSupportedMidPG(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType prepaidMerchant = MerchantType.MASKED_MOBILE_ENABLED;
        SavedCardHelpers.assertStoreCardPrefEnabled(prepaidMerchant);
        PGPHelpers.validate_MerchantPreference(prepaidMerchant.getId(),"PREPAID_CARD","Y");

        SavedCardHelpers.disableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Deleting for User on  PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, prepaidMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), prepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid Card should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).containsOnlyOnce(cardId);


    }

    @Test(description = "P + : Verify that prepaid card should  be visible on cashier page if mid supoorts it")
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Constants.Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Override
    public void prepaidCardVisibleSupportedMid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType prepaidMerchant = MerchantType.MASKED_MOBILE_ENABLED;
        SavedCardHelpers.assertStoreCardPrefEnabled(prepaidMerchant);
        PGPHelpers.validate_MerchantPreference(prepaidMerchant.getId(),"PREPAID_CARD","Y");

        SavedCardHelpers.enableAllSavedCardFlags();

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
       String cinDC1 = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Adding for MID CustId on P+
       String cinDC2 = SavedCardHelpers.addCardAlipay(prepaidMerchant.getId(),custId, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), prepaidMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO with txnToken

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //Prepaid Card should be visible
        Assertions.assertThat(fpoWithoutUser.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinDC1,cinDC2);

        //FPO with SSO

        FetchPaymentOptionsDTO fetchUserDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(prepaidMerchant.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(txnAmount.toString())
                .build();

        FetchPaymentOption fetchSSO = new FetchPaymentOption(prepaidMerchant.getId(),fetchUserDTO);
        JsonPath fetchPaymentOptResponse = fetchSSO.execute().jsonPath();

        //Prepaid Card should be visible
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinDC1,cinDC2);

    }
    @Owner(Constants.Owner.AKSHAT)
    @Epic("PGP-32288")
    @Test(description = "Verify that cardType and bankName is displayed in FPO response for saved cards ")
    public void cardType_bankName_inNativeSavedCards() throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Double txnAmount = 1.0;

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_CONSENT_PG;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, "12", "2030", "4718650100010336");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(merchant.getId(), initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoResponse = response.jsonPath();
        Assertions.assertThat
                (fpoResponse.getString("body.merchantPayOption.savedInstruments.bankName"))
                .as("param missing")
                .isNotEmpty();
        Assertions.assertThat(fpoResponse.getString("body.merchantPayOption.savedInstruments.cardType"))
                .as("param missing")
                .isNotEmpty();

    }
}