package scripts;

import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class AppInvokePromoCases extends PGPBaseTest{

    private CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
    Double txnAmount = 2.0;

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify that bulk offers are applied (all offers should be shown in the form of list on cashier page)when no specific promocode is passed initiate" +
            "and also verify best offers is applied on saved instuments and on entering card details")
    public void PGP_24050_verifyBulkOffersareAppliedwhenPromoNotPassed(@Optional("enhancedweb_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(merchant.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.promoOffersList().assertVisible();

        //Verify best offer is applied on saved instruments
        cashierPage.pause(2);       //wait for 2 seconds before clicking saved card tab to ensure its availability and visibility on screen
        cashierPage.tabSavedCard().click();
        cashierPage.offerAppliedMessage().assertVisible();

        //Verify on Entering Card details, Offer details will be shown
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.offerAppliedMessage().assertVisible();
    }


    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify that incase specific promocode is sent in initate then details of only that promocode should be shown on cashier page" +
            "and also verify amount on paybutton when discount promo is passed")
    public void PGP_24050_verifyTxnAmountAndpromoOffersListNotVisiblewhenDiscountPromoPassed(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(merchant.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        //wait for 2 seconds before clicking saved card tab to ensure its availability and visibility on screen
        cashierPage.tabSavedCard().waitUntilClickable();
        cashierPage.tabSavedCard().click();

        //wait for 2 seconds before accessing pay button to ensure its text is properly updated
        cashierPage.pause(2);
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(payButtonText).isEqualTo("PAY Rs " +
                CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.05 * txnAmount));
        cashierPage.promoOffersList().assertNotVisible();  //Verify offerlist not visible
    }

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify that incase specific promocode is sent in initate then details of only that promocode should be shown on cashier page" +
            "and also verify amount on paybutton when Cashback promo is passed")
    public void PGP_24050_verifyTxnAmountAndpromoOffersListNotVisibleWhenCashbackPromoPassed(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Promo promo = new Promo();
        new Merchant(merchant.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        //wait for 2 seconds before clicking saved card tab to ensure its availability and visibility on screen
        cashierPage.tabSavedCard().waitUntilClickable();
        cashierPage.tabSavedCard().click();

        //wait for 2 seconds before accessing pay button to ensure its availability and visibility on screen
        cashierPage.pause(2);
        String payButtonText = cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(payButtonText).isEqualTo("PAY Rs 10\n" +
                "(Effective price: ₹" +CommonHelpers.doubleToTwoDigitAfterDecimalPoint(txnAmount - 0.05 * txnAmount) +
                " with ₹" +CommonHelpers.doubleToTwoDigitAfterDecimalPoint(0.05 * txnAmount)+" cashback)");
    }


    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify Txn not proceeding when promocode is not applied to the paymode and validate promo is true")
    public void PGP_24050_verifyTxnwhenPromoNotAppliedandValidatePromoisTrue(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Merchant merchant1 = new Merchant(merchant.getId(), true);
        merchant1.getPromos().clear();
        Promo promo = new Promo("NOT_PAY", false, false);
        merchant1.getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        cashierPage.promoInvalidMessage().assertVisible();
    }


    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify Txn proceeds without promo when promocode is not applied to the paymode and validate promo is false")
    public void PGP_24050_verifyTxnwhenPromoNotAppliedandValidatePromoisFalse(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Merchant merchant1 = new Merchant(merchant.getId(), true);
        merchant1.getPromos().clear();
        Promo promo = new Promo("NOT_PAY", false, false);
        merchant1.getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify that incase of AddnPay transaction promo is not applied and txn not proceeding")
    public void PGP_24050_verifyTxnwhenPromoNotAppliedandValidatePromoisTrueAddNPay(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType pwpDefault = Constants.MerchantType.AddnPay;
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefault,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        cashierPage.promoInvalidMessage().assertVisible();
    }

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify the successful addnpay txn when simplified offers has valid promo code and validate promo flag is false, txn proceeds without promo.")
    public void PGP_27602_verifyTxnProceedswithoutPromoWhenValidatePromoisFalseAddNPay(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType pwpDefault = Constants.MerchantType.AddnPay;
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(pwpDefault.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefault,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "Verify Bank offers are visible on cashier page(without SSO token) and not visible after user login")
    public void PGP_27157_ValidateBankOffersNotVisiblewithPWPMerchantfterLoggingIn(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.PWP_DEFAULT;
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        for (int i=0; i<2; i++) {
            Promo promo = new Promo();
            new Merchant(merchant.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.promoOffersList().assertVisible();
        cashierPage.login(user);
        cashierPage.promoOffersList().assertNotVisible();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30155")
    @Parameters({"theme"})
    @Test(description = "validate successful transaction with instant discount when validatePromo is true")
    public void SuccessfulTransactionWithInstantDiscountWhenValidatePromoIsTrue(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType hybrid_Merchant = Constants.MerchantType.Hybrid_S; //HYBRID_DISCOUNT
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getEmiCard());
        Promo promo = new Promo();
        new Merchant(merchant.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), hybrid_Merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(hybrid_Merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30155")
    @Parameters({"theme"})
    @Test(description = "validate successful transaction with cashback when validatePromo is true")
    public void SuccessfulTransactionWithCashbackWhenValidatePromoIsTrue(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType hybrid_Merchant = Constants.MerchantType.Hybrid; //HYBRID_DISCOUNT
        Promo promo = new Promo();
        new Merchant(hybrid_Merchant.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), hybrid_Merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(hybrid_Merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30155")
    @Parameters({"theme"})
    @Test(description = "validate successful transaction with cashback when validatePromo is false")
    public void SuccessfulTransactionWithCashbackWhenValidatePromoIsFalse(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType hybrid_Merchant = Constants.MerchantType.Hybrid; //HYBRID_DISCOUNT
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Promo promo = new Promo();
        new Merchant(hybrid_Merchant.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), hybrid_Merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(hybrid_Merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30155")
    @Parameters({"theme"})
    @Test(description = "validate successful transaction with instant discount when validatePromo is false")
    public void SuccessfullTransactionWithInstantDiscountWhenValidatePromoIsFalse(@Optional("enhancedwap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType hybrid_Merchant = Constants.MerchantType.HYBRID_DISCOUNT;
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Promo promo = new Promo();
        new Merchant(merchant.getId(), true).getPromos().add(promo);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), hybrid_Merchant, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        Double txnAmount = Double.parseDouble(initTxnDTO.txnAmountFromBody());
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(hybrid_Merchant,initTxnDTO.getBody().getOrderId(),txnToken, "true", "true")
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
}
