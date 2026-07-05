package scripts.Native.checkoutjs;


import com.paytm.CreateToken;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;

import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.ui.element.Select;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.appconstants.Constants.Owner.AKSHAT_NAYAK;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;


public class CheckoutJsEMITests extends PGPBaseTest {

    private final CheckoutJsCheckoutPage checkoutjsEmiPage = new CheckoutJsCheckoutPage("emi");
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
    //working if checksum is disabled ; need to add helper in checkout.html to handle checksum


    @Owner("Eshani")
    @Parameters({"theme"})
    @Test(description = "verify successful item based emi on checkout js using access token")
    public void ItemBasedEMIOnCheckoutJs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.NOCHECKSUM_DCSUBVENTION;

        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        String refId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + "10";


        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String accesstoken = jsonpath.getString("body.accessToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(accesstoken, merchantType)
                .setTxnValue("100")
                .build();

        MerchantConfig config = checkoutjsEmiPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(accesstoken);
        config.emiSubvention.setreferenceId(refId);

        checkoutjsEmiPage.createCheckoutJsOrder(config);

        String txnAmount = config.data.amount;

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("wallet should not be selected").isFalse();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchantType.getId())
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateTxnAmount(Constants.ValidationType.NON_EMPTY)
                .validateResponsePageParameters()
                .assertAll();

        String orderId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID);
        String expectedTxnAmt = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT);

        Assert.assertNotEquals(txnAmount, expectedTxnAmt);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnAmount(expectedTxnAmt)
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }


    @Owner("Eshani")
    @Parameters({"theme"})
    @Test(description = "verify successful Amount based emi on checkout js using access token")
    public void AmountBasedEMIOnCheckoutJs(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.NOCHECKSUM_DCSUBVENTION;

        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);

        String refId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + "10";

        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String accesstoken = jsonpath.getString("body.accessToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(accesstoken, merchantType)
                .setTxnValue("20")
                .build();

        MerchantConfig config = checkoutjsEmiPage.loadMerchantConfigAmountStrategy(initTxnDTO, theme);
        config.data.setToken(accesstoken);
        config.emiSubvention.setreferenceId(refId);

        checkoutjsEmiPage.createCheckoutJsOrder(config);

        String txnAmount = config.data.amount;

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("wallet should not be selected").isFalse();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(merchantType.getId())
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateTxnAmount(Constants.ValidationType.NON_EMPTY)
                .validateResponsePageParameters()
                .assertAll();

        String orderId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID);
        String expectedTxnAmt = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT);

        Assert.assertNotEquals(txnAmount, expectedTxnAmt);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnAmount(Constants.ValidationType.NON_EMPTY)
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }


    @Owner("Eshani")
    @Parameters({"theme"})
    @Test(description = "PGP-24569_Verify that error pops up when wallet and emi both are selected for hybrid mid")
    public void VerifyErrorPopUponCashierPage(@Optional("checkoutjs_web") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;

        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.modifyBalance(user, 10.00);

        String refId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + "10";

        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String accesstoken = jsonpath.getString("body.accessToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(accesstoken, merchantType)
                .setTxnValue("100")
                .build();

        MerchantConfig config = checkoutPage.loadMerchantConfigAmountStrategy(initTxnDTO, theme);
        config.data.setToken(accesstoken);
        config.data.setTokenType("ACCESS");
        config.emiSubvention.setreferenceId(refId);

        checkoutPage.createCheckoutJsOrder(config);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        //Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet is not selected by default").isTrue();
        cashierPage.tabEMI().click();

        Assertions.assertThat(cashierPage.walletEmiErrorMsgPopUp().isDisplayed()).as("Paytm Balance and EMI cannot be used together for this payment");
        cashierPage.CrossButtonOnEntercardpage().click();

        Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("Wallet is not disabled").isFalse();

    }

    //    @Owner(Constants.Owner.PUSPA)
//    @Feature("PGP-24569")
//    @Parameters({"theme"})
//    @Test(description = "EMI subvention to be supported on the blink checkout",enabled = false)
    public void verifyUIAmountBasedEMISubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(AccessToken, merchantType)
                .setTxnValue("2")
                .build();
        MerchantConfig config = checkoutjsEmiPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(AccessToken);
        config.emiSubvention.setstrategy("AMOUNT_BASED");
        config.emiSubvention.setitemsNullforAmountBasedTxn();
        config.emiSubvention.setsubventionAmount("2");
        config.emiSubvention.setreferenceId(refId);
        checkoutjsEmiPage.createCheckoutJsOrder(config);
        String txnAmount = config.data.amount;
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.getNoCostEmiDiscountText().getText()).isEqualTo("No Cost EMI available. Get ₹1 Discount");
        cashierPage.proceedBtn().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        Assertions.assertThat(cashierPage.verifyNoCostEmiAppliedText().getText()).isEqualTo("EMI offer of ₹1 Discount successfully applied");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID);
        String expectedTxnAmt = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT);
        Assertions.notIn(txnAmount, expectedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnAmount(expectedTxnAmt)
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    //    @Owner(Constants.Owner.PUSPA)
//    @Feature("PGP-24569")
//    @Parameters({"theme"})
//    @Test(description = "EMI subvention to be supported on the blink checkout",enabled = false)
    public void verifyUIItemBasedEMISubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        String refId = UUID.randomUUID().toString().substring(0, 18);
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String AccessToken = jsonpath.getString("body.accessToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(AccessToken, merchantType)
                .setTxnValue("100")
                .build();
        MerchantConfig config = checkoutjsEmiPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(AccessToken);
        config.emiSubvention.setreferenceId(refId);
        checkoutjsEmiPage.createCheckoutJsOrder(config);
        String txnAmount = config.data.amount;
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().click();
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.getNoCostEmiDiscountText().getText()).isEqualTo("No Cost EMI available. Get ₹1 Discount");
        cashierPage.proceedBtn().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        Assertions.assertThat(cashierPage.verifyNoCostEmiAppliedText().getText()).isEqualTo("EMI offer of ₹1 Discount successfully applied");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID);
        String expectedTxnAmt = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT);
        Assertions.notIn(txnAmount, expectedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnAmount(expectedTxnAmt)
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-35652")
    @Parameters({"theme"})
    @Test(description = " No EMI subvention offer visibility on the payment page for standard EMI")
    public void verifyEMISubventionStrip_ForStandardEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body")).doesNotContain("emiSubventionBanks");
        cashierPage.getEMIStrip().assertNotVisible();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-35652")
    @Parameters({"theme"})
    @Test(description = "EMI subvention offer visibility on the payment page based on emiSubventionBank")
    public void verifyEMISubventionStrip_ForAmountBasedEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "1", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiSubventionBanks.emiTypes.type")).contains("SUBVENTION");
        Assertions.assertThat(cashierPage.getEMIStrip().getText()).isEqualTo("Zero/Low Cost EMI Available");


    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-35652")
    @Parameters({"theme"})
    @Test(description = "EMI subvention offer visibility on the payment page based on emiSubventionBank")
    public void verifyEMISubventionStrip_ForItemBasedEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.emiSubventionBanks.emiTypes.type")).contains("SUBVENTION");
        Assertions.assertThat(cashierPage.getEMIStrip().getText()).isEqualTo("Zero/Low Cost EMI Available");

    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify successful EMI subvention TXN in checkout js for item based 0 cost emi")
    public void EMISubCheckoutJsItembased01(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi")
    public void EMISubventionAmountBasedSuccess01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("AMOUNT_BASED");
        //config.emiSubvention.setitemsNullforAmountBasedTxn();
        //config.emiSubvention.setsubventionAmount("1");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based 0 cost emi")
    public void EMISubventionAmountBasedSuccess02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "20", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        // config.emiSubvention.setstrategy("AMOUNT_BASED");
        // config.emiSubvention.setitemsNullforAmountBasedTxn();
        // config.emiSubvention.setsubventionAmount("1");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi + promo discount")
    public void EMISubventionAmountBasedSuccess03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("AMOUNT_BASED");
        //config.emiSubvention.setitemsNullforAmountBasedTxn();
        // config.emiSubvention.setsubventionAmount("1");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double discountedAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) * 5 / 100;
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount - discountedAmount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify successful EMI subvention TXN in checkout js for item based 0 cost emi + promo discount")
    public void EMISubCheckoutJsItembased02(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double discountedAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) * 5 / 100;
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount - discountedAmount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify the error message when EMI is not supported on provided card")
    public void EMISubCheckoutJsItembased03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", categoryIds, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.INVALID_CARD2);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.cardNotSupported().getText()).isEqualTo("EMI not available on this card. Please choose another card");
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that subvention amount and promo discount are visible on select plan page")
    public void EMISubCheckoutJsItembased04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        cashierPage.SubventionDetails().assertVisible();
        cashierPage.PromoDetails().assertVisible();
        cashierPage.EMIDetails().assertVisible();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that subvention amount and promo discount are visible on final page/conert to emi")
    public void EMISubCheckoutJsItembased05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        Double subventiondiscount = 1.0;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        // config.emiSubvention.setstrategy("ITEM_BASED");
        Double discountedAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) * 5 / 100;
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount - discountedAmount;
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.SubventionDetailsOnConvertToEMIPage().assertVisible();
        cashierPage.PromoDetailsOnConvertToEMIPage().assertVisible();
        String Loantext = cashierPage.LoanAmount().getText();
        String amount = Loantext.substring(Loantext.length() - 2, Loantext.length());
        Assertions.assertThat(amount).isEqualTo(String.valueOf(finalTxnAmount.intValue()));

    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that success txn when user press cross button from convert to emi page and again fill the emi details and pay")
    public void EMISubCheckoutJsItembased06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.crossButtonOnConvertToEMIPage().click();
        cashierPage.CrossButtonOnEntercardpage().click();
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that success txn when user press cross button from enter card number page and again fill the emi details and pay")
    public void EMISubCheckoutJsItembased07(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        // config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.CrossButtonOnEntercardpage().click();
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.RITIK)
    @Feature("PGP-37496")
    @Parameters({"theme"})
    @Test(description = "Verify text script for special handling for brand emi cases for HDFC/SBI Bank when bank contribution is greater than 0 i.e.  " +
            "amountBearer\": {\n" +
            " \"brand\": 2.0, \"merchant\": 0.0, \n" +
            "\"platform\": 0.0 } ")
    public void verifyTextScriptBrandEmiCases_forHDFCBank_whenBankContributionGreaterThanZero(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.BRAND_BO_DISC_HDFC;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "70", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("151.30")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        MerchantConfig config = checkoutjsEmiPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN")
                .setToken(txnToken);
        config.emiSubvention.setstrategy("AMOUNT_BASED")
                .setitemsNullforAmountBasedTxn()
                .setsubventionAmount("151.30");
        checkoutjsEmiPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.getNoteStrip().waitUntilVisible();
        Assertions.assertThat(cashierPage.getNoteStrip().getText()).isEqualTo("Note: Your bank will reverse this transaction and book the loan for ₹149.30 in 3~4 working days.");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String orderId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID);
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateTxnAmount("151.30")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(merchantType.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that when user click on View All to see bank for EMI then Zero/low cost emi and standard emi should be separated")
    public void EMISubCheckoutJsItembased08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        // config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.ViewAll().click();
        cashierPage.waitUntilLoads();
        cashierPage.ZeroLowCostEmiTab().assertVisible();
        cashierPage.StandardEmi().assertVisible();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi + promo cashback")
    public void EMISubventionAmountBasedSuccess04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(true);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("cashback").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        // config.emiSubvention.setstrategy("AMOUNT_BASED");
        // config.emiSubvention.setitemsNullforAmountBasedTxn();
        //config.emiSubvention.setsubventionAmount("1");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that T&C button should be visible and clickable on select EMI plan page")
    public void EMISubCheckoutJsItembased09(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.tabEMI().click();
        cashierPage.fillEMICardDetails(paymentDTO);
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.TnCButton1().assertVisible();
        cashierPage.TnCButton1().assertClickable();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify the EMI subvention success txn with saved card when subvention is item based")
    public void EMISubCheckoutJsItembased10(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPICheckoutjsV5().click();
        Assertions.assertThat(cashierPage.SavedcardEMISubventionStrip().getText()).isEqualTo("Zero/Low Cost EMI Available");
        //  Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("wallet should not be selected").isFalse();
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD, paymentDTO);
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify the EMI subvention success txn with saved card when subvention is amount based")
    public void EMISubventionAmountBasedSuccess05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getEmiCard());
        Constants.MerchantType merchantType = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        config.emiSubvention.setstrategy("AMOUNT_BASED");
        config.emiSubvention.setitemsNullforAmountBasedTxn();
        config.emiSubvention.setsubventionAmount("1");
        checkoutjsEmiPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPICheckoutjsV5().click();
        Assertions.assertThat(cashierPage.SavedcardEMISubventionStrip().getText()).isEqualTo("Zero/Low Cost EMI Available");
        //  Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).as("wallet should not be selected").isFalse();
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD, paymentDTO);
        Double subventiondiscount = 2.0; // for amount based emi subvention discount is of 2 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify that when user click on View All and select Zero/Low cost emi bank and pay using the same")
    public void EMISubCheckoutJsItembased11(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        //config.emiSubvention.setstrategy("ITEM_BASED");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();
        Double subventiondiscount = 1.0; // for item based emi subvention discount is of 1 Rs
        Double finalTxnAmount = Double.valueOf(initTxnDTO.txnAmountFromBody()) - subventiondiscount;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalTxnAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-40131")
    @Parameters({"theme"})
    @Test(description = " No EMI subvention offer visibility on the payment page for standard EMI")
    public void noEmiStripVisible_ForStandardEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMISubvention;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body")).doesNotContain("emiSubventionBanks");
        cashierPage.getEMIStrip().assertNotVisible();
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-37719")
    @Parameters({"theme"})
    @Test(description = " Emi Subvention offer visible on multiple cards")
    public void multiOfferEMISubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.emiSubventionOfferStrip().getText()).isEqualTo("Zero/Low Cost EMI on multiple Cards");

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-37719")
    @Parameters({"theme"})
    @Test(description = " Emi Subvention offer visible on multiple cards")
    public void multiOfferEMISubvention_offerPage(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.viewAllOffersAvialable().click();
        cashierPage.offerPage().waitUntilVisible();
        Assertions.assertThat(cashierPage.offerPage().getText()).isEqualTo("Zero/Low Cost EMI on multiple Cards");
        Assertions.assertThat(cashierPage.offerText().getText()).isEqualTo("Available on different tenures");
        cashierPage.crossButtonOnConvertToEMIPage();

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-37719")
    @Parameters({"theme"})
    @Test(description = " Emi Subvention offer visible on multi cards")
    public void multiOfferEMISubvention_offerPage2(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.viewAllOffersAvialable().click();
        cashierPage.offerTC().click();
        cashierPage.emiSubventionOfferAvialbaleOnCard().waitUntilVisible();
        Assertions.assertThat(cashierPage.emiSubventionOfferAvialbaleOnCard().getText()).isEqualTo("Zero/Low Cost EMI on multiple Cards");
        cashierPage.emiTC().click();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-37719")
    @Parameters({"theme"})
    @Test(description = " Emi Subvention offer visible on single cards")
    public void singleOfferEMISubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_DISCOVERY;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant, simplifiedPaymentOffers)
                .setTxnValue("600")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String actualText = cashierPage.emiSubventionOfferStripNEW().getText().split("\n")[0].trim();
        Assertions.assertThat(actualText).isEqualTo("6 Exclusive Offers for You");


    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-37719")
    @Parameters({"theme"})
    @Test(description = " Emi Subvention offer visible on single cards")
    public void singleOfferEMISubvention_offerPage(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMISubvention;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.viewAllOffersAvialable().click();
        cashierPage.offerPage().waitUntilVisible();
        Assertions.assertThat(cashierPage.offerPage().getText()).isEqualTo("Zero/Low Cost EMI on HDFC Bank Credit Card");
        Assertions.assertThat(cashierPage.offerText().getText()).isEqualTo("Available on different tenures");
        cashierPage.crossButtonOnConvertToEMIPage();

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-37719")
    @Parameters({"theme"})
    @Test(description = " Emi Subvention offer visible on single cards")
    public void singleOfferEMISubvention_offerPage2(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMISubvention;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant, simplifiedPaymentOffers)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.viewAllOffersAvialable().click();
        cashierPage.offerTC().click();
        cashierPage.emiSubventionOfferAvialbaleOnCard().waitUntilVisible();
        Assertions.assertThat(cashierPage.emiSubventionOfferAvialbaleOnCard().getText()).isEqualTo("Zero/Low Cost EMI on HDFC Bank Credit Card");
        cashierPage.emiTC().click();
    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-41729")
    @Parameters({"theme"})
    @Test(description = "EMI subvention to be supported for Access Token in bank select flow")
    public void verifyEMISupportforAccessTokenbankSelect(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NOCHECKSUM_DCSUBVENTION;
        User user = userManager.getForRead(Label.BASIC);
        String refId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + "10";
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String accesstoken = jsonpath.getString("body.accessToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(accesstoken, merchantType)
                .setTxnValue("5")
                .build();
        MerchantConfig config = checkoutjsEmiPage.loadMerchantConfigAmountStrategy(initTxnDTO, theme);
        config.data.setToken(accesstoken);
        config.emiSubvention.setreferenceId(refId);
        checkoutjsEmiPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-41729")
    @Parameters({"theme"})
    @Test(description = "EMI subvention to be supported for Access Token in new Card flow")
    public void verifyEMISupportforAccessTokennewCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NOCHECKSUM_DCSUBVENTION;
        User user = userManager.getForRead(Label.BASIC);
        String refId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + "10";
        CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String accesstoken = jsonpath.getString("body.accessToken");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(accesstoken, merchantType)
                .setTxnValue("5")
                .build();
        MerchantConfig config = checkoutjsEmiPage.loadMerchantConfigAmountStrategy(initTxnDTO, theme);
        config.data.setToken(accesstoken);
        config.emiSubvention.setreferenceId(refId);
        checkoutjsEmiPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COP for EMI txn ")
    public void ValidateFieldsForEMICCForCOP(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_AMEX_EMI)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"200.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"1.66\"");

    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COTP for EMI txn ")
    public void ValidateFieldsForEMICCForCOTP(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGONLY_EMI_MIN_MAX)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"200.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"0.67\"");

    }


    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COP for EMI txn ")
    public void ValidateFieldsForEMIDCForCOP(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_AMEX_EMI)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"200.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"1.66\"");

    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COTP for EMI txn ")
    public void ValidateFieldsForEMIDCForCOTP(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGONLY_EMI_MIN_MAX)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"200.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"0.67\"");

    }


    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi + promo discount")
    public void ValidateFieldsForEMICCPromoAndSubventionForCOP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTEMI91").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "200", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"188.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"2.0\"");
    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "EMI subvention success txn on checkoutjs for amount based low cost emi + promo discount")
    public void ValidateFieldsForEMICCPromoAndSubventionForCOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTPG2EMI49").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "200", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"179.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"1.0\"");
    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COP for EMI Subvention txn")
    public void ValidateFieldsForEMICCSubventionForCOP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;

        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "200", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("200")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"198.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"2.0\"");
    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COTP for EMI Subvention txn")
    public void ValidateFieldsForEMICCSubventionForCOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;

        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "200", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("200")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"199.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"1.0\"");
    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COP for EMI Promo txn")
    public void ValidateFieldsForEMICCPromoForCOP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTEMI90").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"190.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"11.66\"");
    }

    @Owner(MAYURI)
    @Feature("PGP-44050")
    @Parameters({"theme"})
    @Test(description = "Validate originalPrice,interest,loanAmount in COTP for EMI Promo txn")
    public void ValidateFieldsForEMICCPromoForCOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTPG2EMI49").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_PAY_ORDER");
        Assertions.assertThat(logs).contains("\"originalPrice\":\"200\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"180.00\"");
        Assertions.assertThat(logs).contains("\"interest\":\"20.67\"");
    }


    @Owner(MAYURI)
    @Feature("PGP-42454")
    @Parameters({"theme"})
    @Test(description = "Verify v2/applyPromo request body should send new key tenure= -1 alog with existing details and response should get applicable tenures array for amount subvention + promo discount")
    public void ValidateApplyPromoForEMICCPromoAndSubventionForCOP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTEMI91").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "200", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.pause(1);

        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);

        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
        String applyPromoRequestLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "applyPromo request");
        int payMethodIndex = applyPromoRequestLogs.indexOf("payMethod=EMI");
        String tenureLogs = applyPromoRequestLogs.substring(payMethodIndex);
        Assertions.assertThat(tenureLogs).contains("tenure=-1");
        String applyPromoResponseLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "returning response for applyPromo V2");
        int payMethodIndexForResponse = applyPromoResponseLogs.indexOf("payMethod=EMI");
        String applicableTenuresLogs = applyPromoResponseLogs.substring(payMethodIndexForResponse);
        Assertions.assertThat(applicableTenuresLogs).contains("applicableTenures=[]");
        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("188.0")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MAYURI)
    @Feature("PGP-42454")
    @Parameters({"theme"})
    @Test(description = "Verify v2/applyPromo request body should send new key tenure= -1 alog with existing details and response should get applicable tenures array for amount subvention + promo discount")
    public void ValidateApplyPromoForEMICCPromoAndSubventionForCOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTPG2EMI49DIS").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "200", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.pause(1);

        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);

        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
        String applyPromoRequestLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "applyPromo request");
        int payMethodIndex = applyPromoRequestLogs.indexOf("payMethod=EMI");
        String tenureLogs = applyPromoRequestLogs.substring(payMethodIndex);
        Assertions.assertThat(tenureLogs).contains("tenure=-1");
        String applyPromoResponseLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "returning response for applyPromo V2");
        int payMethodIndexForResponse = applyPromoResponseLogs.indexOf("payMethod=EMI");
        String applicableTenuresLogs = applyPromoResponseLogs.substring(payMethodIndexForResponse);
        Assertions.assertThat(applicableTenuresLogs).contains("applicableTenures=[]");
        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("179.0")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MAYURI)
    @Feature("PGP-42454")
    @Parameters({"theme"})
    @Test(description = "Verify v2/applyPromo request body should send new key tenure= -1 alog with existing details and response should get applicable tenures array for promo discount")
    public void ValidateApplyPromoForEMICCPromoForCOP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_AMEX_EMI;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTEMI91").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.pause(1);

        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);

        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
        String applyPromoRequestLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "applyPromo request");
        int payMethodIndex = applyPromoRequestLogs.indexOf("payMethod=EMI");
        String tenureLogs = applyPromoRequestLogs.substring(payMethodIndex);
        Assertions.assertThat(tenureLogs).contains("tenure=-1");
        String applyPromoResponseLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "returning response for applyPromo V2");
        int payMethodIndexForResponse = applyPromoResponseLogs.indexOf("payMethod=EMI");
        String applicableTenuresLogs = applyPromoResponseLogs.substring(payMethodIndexForResponse);
        Assertions.assertThat(applicableTenuresLogs).contains("applicableTenures=[]");
        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("190.0")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MAYURI)
    @Feature("PGP-42454")
    @Parameters({"theme"})
    @Test(description = "Verify v2/applyPromo request body should send new key tenure= -1 alog with existing details and response should get applicable tenures array for promo discount")
    public void ValidateApplyPromoForEMICCPromoForCOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGONLY_EMI_MIN_MAX;
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("TESTPG2EMI49").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.pause(1);

        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);

        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
        String applyPromoRequestLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "applyPromo request");
        int payMethodIndex = applyPromoRequestLogs.indexOf("payMethod=EMI");
        String tenureLogs = applyPromoRequestLogs.substring(payMethodIndex);
        Assertions.assertThat(tenureLogs).contains("tenure=-1");
        String applyPromoResponseLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, initTxnDTO.getBody().getOrderId(), "returning response for applyPromo V2");
        int payMethodIndexForResponse = applyPromoResponseLogs.indexOf("payMethod=EMI");
        String applicableTenuresLogs = applyPromoResponseLogs.substring(payMethodIndexForResponse);
        Assertions.assertThat(applicableTenuresLogs).contains("applicableTenures=[]");
        cashierPage.payButton().assertVisible();
        cashierPage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("180.0")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-47723")
    @Parameters({"theme"})
    @Test(description = "Test that v1/checkout api should call for emi with Promo and subvention txn amount based")
    public void Testv1CheckoutApiCalledForEmiPromoSubventionTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        String txnAmount = "1100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "1100", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("promoCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("subventionCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("productId").isNotNull();
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION", "/ats/v1/order/checkout");

    }

    @Owner("Karmvir")
    @Feature("PGP-47723")
    @Parameters({"theme"})
    @Test(description = "Test that v1/checkout api should call for emi with Promo amount based and subvention is item based txn amount based")
    public void Testv1CheckoutApiCalledForEmiPromoAmountBasedSubventionItemTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        String txnAmount = "1100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        ArrayList<SimplifiedSubvention.Item> items = new ArrayList();
        ArrayList<String> catagoryList = new ArrayList();
        catagoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "18084", catagoryList, "1", "1100", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("promoCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("subventionCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("\"productId\":\"321067334\",");
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION", "/ats/v1/order/checkout");

    }

    @Owner("Karmvir")
    @Feature("PGP-47723")
    @Parameters({"theme"})
    @Test(description = "Test that v1/checkout api should call for emi with subvention txn amount based")
    public void Testv1CheckoutApiCalledForEmiSubventionTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String txnAmount = "1100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "1100", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("subventionCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("productId").isNotNull();
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION", "/ats/v1/order/checkout");

    }

    @Owner("Karmvir")
    @Feature("PGP-47723")
    @Parameters({"theme"})
    @Test(description = "Test that v1/checkout api should call for emi with Amount based subvention and Item based promo txn amount based")
    public void Testv1CheckoutApiCalledForItembasedPromoAndAmountSubventionTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String txnAmount = "1100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_V1_CHECKOUT;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "1100", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", null, "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "", "1100", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("", "true", "true", cartDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("productInfo").isNotNull();
        Assertions.assertThat(logs).contains("subventionCheckoutInfo").isNotNull();
        Assertions.assertThat(logs).contains("\"productId\":\"123400232343411113e331\",");
        Assertions.assertThat(logs).contains("paymentDetails").isNotNull();
        Assertions.assertThat(logs).contains("emiPlanDetails").isNotNull();
        Assertions.assertThat(logs).contains("tenure").isNotNull();
        Assertions.assertThat(logs).contains("FUNCTION", "/ats/v1/order/checkout");

    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-49170")
    @Parameters({"theme"})
    @Test(description = "Validate orderItemId is same as itemId in subvention and promo checkout when both amount based")
    public void ValidateOrterItemIdWhenAmountBasedPromoAndSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00789", "false", "true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("Test105", "2000", true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs1).contains("\"items\":{\"item001\"");
        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "EMI_SUBVENTION_SERVICE");
        Assertions.assertThat(logs2).contains("\"id\":\"item001\"");
        Assertions.assertThat(logs2).contains("\"orderItemId\":\"item001\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-49170")
    @Parameters({"theme"})
    @Test(description = "Validate orderItemId is same as itemId in checkout for amount based subvention and item based promo")
    public void ValidateOrterItemIdWhenItemBasedPromoAndAmountBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("Test105", "2000", true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs1).contains("\"items\":{\"113\"");
        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "EMI_SUBVENTION_SERVICE");
        Assertions.assertThat(logs2).contains("\"id\":\"113\"");
        Assertions.assertThat(logs2).contains("\"orderItemId\":\"113\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-49170")
    @Parameters({"theme"})
    @Test(description = "Validate orderItemId is same as itemId in checkout for item based subvention and amount based promo")
    public void ValidateOrterItemIdWhenAmountBasedPromoAndItemBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120", "321067334", "18084", categoryList, "1", "1100", "51", true, false, null);
        List<SimplifiedSubvention.Item> items = new ArrayList<>();
        items.add(item);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00789", "false", "true");
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs1).contains("\"items\":{\"120\"");
        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "EMI_SUBVENTION_SERVICE");
        Assertions.assertThat(logs2).contains("\"id\":\"120\"");
        Assertions.assertThat(logs2).contains("\"orderItemId\":\"120\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-49170")
    @Parameters({"theme"})
    @Test(description = "Validate orderItemId is same as itemId in checkout for item based subvention and item based promo")
    public void ValidateOrterItemIdWhenItemBasedPromoAndItemBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EmiInfo_COP;
        List<String> categoryList = new ArrayList<>();
        categoryList.add("6224");
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("120", "321067334", "18084", categoryList, "1", "1100", "51", true, false, null);
        List<SimplifiedSubvention.Item> itemss = new ArrayList<>();
        itemss.add(item);
        List<String> categoryIds = new ArrayList<>();
        categoryIds.add("521");
        SimplifiedPaymentOffers.ProductDetail productDetail = new SimplifiedPaymentOffers.ProductDetail("123400232343411113e331", "6002665", "112866", categoryIds);
        SimplifiedPaymentOffers.Items items = new SimplifiedPaymentOffers.Items("113", "PROMO00187", "2000", productDetail);
        List<SimplifiedPaymentOffers.Items> itemsList = new ArrayList<>();
        itemsList.add(items);
        SimplifiedPaymentOffers.CartDetails cartDetails = new SimplifiedPaymentOffers.CartDetails(itemsList);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers("PROMO00187", "true", "true", cartDetails);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, itemss);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1100")
                .setSimplifiedPaymentOffers(simplifiedPaymentOffers)
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard("4761360075860428");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "PAYMENT_PROMO_SERVICE");
        Assertions.assertThat(logs1).contains("\"items\":{\"113\"");
        String logs2 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "EMI_SUBVENTION_SERVICE");
        Assertions.assertThat(logs2).contains("\"id\":\"120\"");
        Assertions.assertThat(logs2).contains("\"orderItemId\":\"120\"");
    }

    @Feature("PGP-50817")
    @Owner(KARMVIR)
    @Parameters({"theme"})
    @Test(description = "Perform a EMI_DC only transaction with PCF merchant")
    public void VerifyEMI_DCTxnWithPCF(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PCE_MID;
        String txnAmount = "1100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (!cashierPage.uncheckedPPIForCheckoutJS().isDisplayed()) {
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.ICICI_DEBIT_CARD_EMI);
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getDebitCardNumber());
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.scrollToElement(cashierPage.EMI_DCPcfText());
        cashierPage.EMI_DCPcfText().assertVisible();
        cashierPage.kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("EMI_DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICIE.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("ICICI Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Feature("PGP-50817")
    @Owner(KARMVIR)
    @Parameters({"theme"})
    @Test(description = "Perform a EMI_DC only transaction with PCF merchant with Promo")
    public void VerifyEMI_DCTxnWithPCFWithPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_PCE_MID;
        String txnAmount = "1100";
        Merchant merchant = new Merchant(merchantType.getId(), true);
        Promo promocode = new Promo(false);
        merchant.getPromos().add(promocode);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType, simplifiedPaymentOffers)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.ICICI_DEBIT_CARD_EMI);
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getDebitCardNumber());
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.scrollToElement(cashierPage.EMI_DCPcfText());
        cashierPage.EMI_DCPcfText().assertVisible();
        cashierPage.kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("EMI_DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICIE.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICIE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("ICICI Bank")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(PUSPA)
    @Feature("PGP-52194")
    @Parameters({"theme"})
    @Test(description = "Verify standard EMI txn called /ats/v2/order/checkout API for Flag:theia.enable.V2CheckoutAtAffordability ON")
    public void verifyStandardEMIForv2OrderCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.DINERS_BLACK_PREMIUM_HDFC_CARD);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "/ats/v2/order/checkout");


    }

    @Owner(PUSPA)
    @Feature("PGP-52194")
    @Parameters({"theme"})
    @Test(description = "Verify EMI_DC called /ats/v2/order/checkout API for Flag:theia.enable.V2CheckoutAtAffordability ON")
    public void verifyDCEMIForv2OrderCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        cashierPage.payByEMI(cashierPage, paymentDTO,true);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "/ats/v2/order/checkout");


    }

    @Owner(PUSPA)
    @Feature("PGP-49406")
    @Parameters({"theme"})
    @Test(description = "Verify Test e2e txn when MINIMAL_Subvention_MERCHANT is enabled on merchant Flag:theia.enable.migration.to.unifiedOffers ON")
    public void verifye2eTxnMinSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = PG2_AMEX_EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(logs).contains("FUNCTION", "ads/v2/offer/apply");
        Assertions.assertThat(logs).contains("FUNCTION", "/ats/v2/order/checkout");
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();


    }

    @Owner(SATWIK_SHARMA)
    @Feature("PGP-55801")
    @Test(description = "Theia should not call Aff./Subvention if EMI is not enabled on mid but simplifiedSubvention object is there in the request")
    public void testNoEmiSubventionServiceCallGoesToPromo() throws Exception {

        Constants.MerchantType merchant = LINK_PGONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_OPTION_FACADE","REQUEST");
        Assertions.assertThat(logs).doesNotContain("TASK_NAME","FETCH_EMI_SUBVENTED_BANKS");
    }

}