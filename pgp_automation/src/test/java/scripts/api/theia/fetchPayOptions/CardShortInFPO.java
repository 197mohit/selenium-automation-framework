package scripts.api.theia.fetchPayOptions;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.dto.NativeDTO.OfferApply.OfferApplyDTO;
import com.paytm.dto.NativeDTO.OfferApply.PaymentDetails;
import com.paytm.dto.NativeDTO.OfferApply.PaymentOption;
import com.paytm.dto.NativeDTO.OfferApply.Item;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.apphelpers.supercashhelpers.superCashHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.time.Instant;
import java.util.Date;

public class CardShortInFPO extends PGPBaseTest {
    //qa8mal86848343808072
    private final CheckoutJsCheckoutPage checkoutJsCheckoutPage = new CheckoutJsCheckoutPage();
    public static String custId = RandomStringUtils.randomAlphabetic(10) + Instant.now().toEpochMilli();

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-54535")
    @Test(description = "Verify card shortcut for VISA present in FPO V5")
    public void VISA_shortcut_Offus() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.COBRANDED_CC, "PPBL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.COBRANDED_CC.length() + "";
        System.out.println("cardLength: " + cardLength);

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("29090.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-45867")
    @Test(description = "Verify card shortcut for DINERS present in FPO V5")
    public void DINERS_shortcut_offus() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.DINERS_CARD_NUMBER, "PPBL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.DINERS_CARD_NUMBER.length() + "";

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("29090.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-54535")
    @Test(description = "Verify card shortcut for 14 digit DINERS card present in FPO V5")
    public void DINERS_shortcutFor_ONUS() throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_ONUS;
       // SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.DINERS_CARD_NUMBER, "PMALL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.DINERS_CARD_NUMBER.length() + "";
        System.out.println("cardLength: " + cardLength);

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("29090.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);
        System.out.println("cardLength: " + cardLength);

    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-54535")
    @Test(description = "Verify VISA card shortcut present on Cashier Page")
    public void VISA_shortcut_JSCHECKOUT(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.COBRANDED_CC, "PPBL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.COBRANDED_CC.length() + "";
        System.out.println("cardLength: " + cardLength);

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("29090.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.uncheckedPPIForCheckoutJS().click();
        cashierPage.cardShortcut(cardSuffix).click();
        cashierPage.textBoxCardShortcut().clearAndType(PaymentDTO.COBRANDED_CC.substring(0,12));
        cashierPage.ExpiryMonthCardShortcut().clearAndType("12");
        cashierPage.ExpiryYearCardShortcut().clearAndType("29");
        cashierPage.CVVCardShortcut().clearAndType("1931");
        CashierPage.SaveShortcutCard().check();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();

    }


    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-54535")
    @Test(description = "Verify Success txn with VISA Card shortcut on offus.")
    public void VISA_shortcut_Offus_On_JSCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.COBRANDED_CC, "PPBL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.COBRANDED_CC.length() + "";

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.uncheckedPPIForCheckoutJS().click();
        cashierPage.cardShortcut(cardSuffix).click();
        cashierPage.textBoxCardShortcut().clearAndType(PaymentDTO.COBRANDED_CC.substring(0,12));
        cashierPage.ExpiryMonthCardShortcut().clearAndType("12");
        cashierPage.ExpiryYearCardShortcut().clearAndType("29");
        cashierPage.CVVCardShortcut().clearAndType("1231");
        DriverManager.getDriver().switchTo().defaultContent();
       cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchant.getId());
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-54535")
    @Test(description = "Verify Success txn with VISA Card shortcut on offus and shortcut card is getting saved")
    public void SaveCardOn_shortcut_Txn_JSCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.COBRANDED_CC, "PPBL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.COBRANDED_CC.length() + "";

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.uncheckedPPIForCheckoutJS().click();
        cashierPage.cardShortcut(cardSuffix).click();
        cashierPage.textBoxCardShortcut().clearAndType(PaymentDTO.COBRANDED_CC.substring(0,12));
        cashierPage.ExpiryMonthCardShortcut().clearAndType("12");
        cashierPage.ExpiryYearCardShortcut().clearAndType("29");
        cashierPage.CVVCardShortcut().clearAndType("193");
        CashierPage.SaveShortcutCard().check();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchant.getId());

        String orderID1 = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID1)
                .setChannelId("WEB")
                .build();

        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO1 = new FetchPaymentOptionsDTO.Builder(txnToken1).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption1 = new FetchPaymentOptionV5(initTxnDTO1.getBody().getMid(), initTxnDTO1.getBody().getOrderId(), fetchPaymentOptionsDTO1);
        JsonPath fetchPaymentOptionsJson1 = fetchPaymentOption1.execute().jsonPath();

        String size=fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments.size");
        System.out.println("saved instrument size: "+size);
        for (int i=0;i<Integer.parseInt(size);i++){
            System.out.println("last four: "+fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit"));
            if(fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit").equals(cardSuffix))
            {
                System.out.println(fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit").equals(cardSuffix));
                Assertions.assertThat(fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments["+i+"].assetType")).isEqualTo("TOKEN");
                Assertions.assertThat(fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit")).isEqualTo(cardSuffix);
                Assertions.assertThat(!fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails").contains(tin)).isEqualTo(true);
                Assertions.assertThat(fetchPaymentOptionsJson1.getString("body.merchantPayOption.savedInstruments["+i+"].cnMax")).isEqualTo(cardLength);
                break;
            }
            else System.out.println("Saved Instrument not found");
        }
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-54535")
    @Test(description = "Verify success txn with saved card(Token) when Token and Token shortcut is present")
    public void SUCCESS_TXN_WITH_TOKEN_ON_JSCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPILITECC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.COBRANDED_CC, "PPBL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();

        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.COBRANDED_DC, "PMALL");
        String TokencardSuffix=SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();

        String cardLength = PaymentDTO.COBRANDED_CC.length() + "";
        System.out.println("cardLength: " + cardLength);
        System.out.println("TokencardSuffix: " + TokencardSuffix);
        System.out.println("cardSuffix: " + cardSuffix);

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        // Verify card shortcut is present
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);

        // Verify Saved card(Token is present)
        String size=fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments.size");
        System.out.println("saved instrument size: "+size);
        for (int i=0;i<Integer.parseInt(size);i++){
            if(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit")==TokencardSuffix)
            {
                System.out.println(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit").equals(TokencardSuffix));
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].assetType")).isEqualTo("TOKEN");
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].cardDetails.lastFourDigit")).isEqualTo(TokencardSuffix);
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.cardId")).isEqualTo(tin);
                Assertions.assertThat(!fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments.cardDetails").contains(tin)).isEqualTo(true);
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].cnMax")).isEqualTo(cardLength);
                break;
            }
            else System.out.println("Saved Instrument not found");
        }

        System.out.println("cardLength: " + cardLength);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.cardShortcut(TokencardSuffix).click();

        cashierPage.textBoxSavedCardCVV().clearAndType("9801");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchant.getId());
    }

    @Owner(Constants.Owner.MANISH_MISHRA)
    @Feature("PGP-54535")
    @Test(description = "Verify Success txn with MASTER Card shortcut on offus")
    public void MASTER_shortcutFor_ONUS_ON_JSCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.MASTER_CREDIT_CARD, "PPBL" +
                "");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.MASTER_CREDIT_CARD.length() + "";
        System.out.println("cardLength: " + cardLength);

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("29090.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);

        MerchantConfig config = checkoutJsCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutJsCheckoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.uncheckedPPIForCheckoutJS().click();
        cashierPage.cardShortcut(cardSuffix).click();
        cashierPage.textBoxCardShortcut().clearAndType(PaymentDTO.MASTER_CREDIT_CARD.substring(0,12));
        cashierPage.ExpiryMonthCardShortcut().clearAndType("12");
        cashierPage.ExpiryYearCardShortcut().clearAndType("29");
        cashierPage.CVVCardShortcut().clearAndType("123");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchant.getId());

    }



    public void verifyCardShortCutDetail(JsonPath fetchPaymentOptionsJson, String cardSuffix, String tin, String cardLength) {

        String size=fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments.size");
        System.out.println("saved instrument size: "+size);
        for (int i=0;i<Integer.parseInt(size);i++){
            if(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit")==cardSuffix)
            {
                System.out.println(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments["+i+"].cardDetails.lastFourDigit").equals(cardSuffix));
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].assetType")).isEqualTo("TOKEN_SHORTCUT");
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].cardDetails.lastFourDigit")).isEqualTo(cardSuffix);
                Assertions.assertThat(!fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments.cardDetails").contains(tin)).isEqualTo(true);
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].cnMax")).isEqualTo(cardLength);
                break;
            }
            else System.out.println("Saved Instrument not found");
        }

    }
    @Owner(Constants.Owner.RONIKA)
    @Feature("PGP-60691")
    @Feature("PGP-60736")
    @Test(description = "Verify card shortcut is present in FPO V5 and offer is being applied on same card")
    public void OfferApply_Token_shortcut() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SHORTCUT_OFFUS;
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCardInOtherVaults(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.COBRANDED_CC, "PPBL");
        String cardSuffix = SavedCardHelpers.getLastFourDigit();
        String tin = SavedCardHelpers.getTin();
        String cardLength = PaymentDTO.COBRANDED_CC.length() + "";

        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2016.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();

        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        Response fpoResponse = fetchPaymentOption.execute();
        JsonPath fetchPaymentOptionsJson = fpoResponse.jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0].assetType")).isEqualTo("TOKEN_SHORTCUT");
        verifyCardShortCutDetail(fetchPaymentOptionsJson, cardSuffix, tin, cardLength);
        //Offer apply 
        PaymentOption paymentOption = new PaymentOption();
        paymentOption.setCardNo(PaymentDTO.COBRANDED_CC);
        paymentOption.setBankCode("HDFC");
        paymentOption.setPayMethod("CREDIT_CARD");
        paymentOption.setApplyBankOffer(true);
        PaymentDetails paymentDetails = new PaymentDetails();
        paymentDetails.setOrderAmount(2016.0);
        paymentDetails.setPaymentOptions(java.util.Arrays.asList(paymentOption));

        Item item = new Item();
        item.setId(java.util.UUID.randomUUID().toString());
        item.setProductId("1201281033");
        item.setBrandId("439626");
        item.setCategoryId("69089");
        item.setPrice(2016.0);
        item.setVerticalId(64.0);

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(user.ssoToken(), merchant.getId())
        .setCustId("MOCKCARDSHORTCUT")
        .setAmountBasedBankOffer(false)
        .setPaymentDetails(paymentDetails)
        .setItems(java.util.Arrays.asList(item))
        .build();

        Response response = NativeHelpers.Validate_OfferApply(offerApply);
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].prePromoText")).isEqualTo("Offer Available");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].offerDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2487963");       
        
   }
}