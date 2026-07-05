package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.api.MappingService.GetMerchantExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;


@Owner("Tarun")
@Epic("PWP")
@Feature("PGP-20557")
public class PWPEnhanced extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType pwpDefault = Constants.MerchantType.PWP_DEFAULT;
    Constants.MerchantType pwpHybrid = Constants.MerchantType.PWP_HYBRID;
    Constants.MerchantType pwpRetry = Constants.MerchantType.PWP_HYBRID_RETRY;
    Constants.MerchantType pwpDefaultPg2Rtdd = Constants.MerchantType.PWP_DEFAULT_PG2_RTDD;
    Constants.MerchantType pwpHybridPg2Rtdd = Constants.MerchantType.PWP_HYBRID_PG2_RTDD;
    Constants.MerchantType pwpRetryPg2Rtdd = Constants.MerchantType.PWP_HYBRID_RETRY_PG2_RTDD;
    Constants.MerchantType pwpDirect = Constants.MerchantType.PWP_HDFO_DIRECT;
    Double txnAmount = 2.0;

    private void assertPWPPrefEnabled(Constants.MerchantType merchantType,String... payMode) {

        String preference = "PAY_WITH_PAYTM";
        pre_requisite:
        {
            //  User should be logged in for PWP flow
             PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference, "Y"); //2
             PWPHelpers.getPWPEnabledPayMode(preference,merchantType.getId(),payMode);

        }
    }
    private void assertRefundAllowed(Constants.MerchantType merchantType)
    {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
    }

    private void assertSMSPrefEnabled(Constants.MerchantType merchantType)
    {
        MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(merchantType.getId());
        String merchantSMS = merchExtendedInfo.getExtendedInfo().getMerchCommPref();
        int userSMS = merchExtendedInfo.getExtendedInfo().getCustCommPref();
        Assertions.assertThat(merchantSMS).as("Can not send SMS to merchant as merchCommPref value is: " +merchantSMS).isEqualTo("19"); //SMS enabled
        Assertions.assertThat(userSMS).as("User SMS should be disabled but found : " +userSMS).isEqualTo(39); //SMS disabled
    }

    private void assertRefundSuccessNotifyPeon(Constants.MerchantType merchantType)
    {
        String preference = "S2S_REFUND_NOTIFY";
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference, "Y");
    }

    @BeforeClass
    public void successTxnOfHigherAmountToIncreaseMPABalanceOfUser() throws Exception {
        String txnAmount = "80000.00";
        User user = userManager.getForWrite(Label.BASIC);
        PWPNative pwpNative = new PWPNative();

        OrderDTO orderDTO =  pwpNative.initiateAndPTCNative(pwpDefault,user,txnAmount,false, PayMethodType.CREDIT_CARD,"");

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    //All the merchants exposed API needs to be tested

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via CC.", groups = "P0")
    public void PWP_01_successfulPWPOnlyCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "CC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(pwpDefaultPg2Rtdd, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);

    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via DC", groups = "P0")
    public void PWP_02_successfulPWPOnlyDC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "DC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(pwpDefaultPg2Rtdd, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);

    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via NB.", groups = "P0")
    public void PWP_03_successfulPGOnlyNB(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "NB";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(pwpDefaultPg2Rtdd, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.valueOf(payMode), paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via PPBL.", groups = "P0")
    public void PWP_04_successfulPGOnlyPPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String payMode = "NB";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.setZeroBalance(user);

        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefaultPg2Rtdd, theme, user)
                        .build();
        WalletHelpers.setZeroBalance(user);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(Constants.PayMode.PPBL);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }


    //Refund Success Notify is not coming for UPI
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via UPI.", groups = "P0")
    public void PWP_05_successfulPWPOnlyUPI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "UPI";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefaultPg2Rtdd, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
       // PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefault); Manual Regression

    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via PPI(Wallet)", groups = "P0")
    public void PWP_06_successfulPWPOnlyPPI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "PPI";

        assertPWPPrefEnabled(pwpDefaultPg2Rtdd, payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, txnAmount);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefaultPg2Rtdd, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO, pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO, pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO, pwpDefaultPg2Rtdd);
    }

    //Paytm CC refund is supposed to happen offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via Paytm CC", groups = "P0")
    public void PWP_07_successfulPWPOnlyPaytmCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "PAYTM_DIGITAL_CREDIT";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.POSTPAID);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefaultPg2Rtdd, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction to assert saved PWP Card", groups = "P0")
    public void PWP_08_successfulPWPOnlyPaytmToSaveCard(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "DC";
        assertPWPPrefEnabled(pwpDefault,payMode);
        assertSMSPrefEnabled(pwpDefault);
        assertRefundAllowed(pwpDefault);
        assertRefundSuccessNotifyPeon(pwpDefault);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefault, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefault);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefault);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

        OrderDTO savedCardOrderDTO = new OrderFactory.Hybrid(pwpDefault, theme, user)
                .build();
        checkoutPage.createOrder(savedCardOrderDTO);
        cashierPage.assertSavedCardVisibility();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via Saved Card", groups = "P0")
    public void PWP_09_successfulPWPOnlyPaytmSavedCard(@Optional("enhancedwap") String theme) throws Exception {
        String payMode = "DC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefaultPg2Rtdd, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via CC corporate Card on corporate PWP Merchant.", groups = "P0")
    public void PWP_succesfulCCCorporate(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "CC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd, payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        CorporateHelpers.assertCorporateCardCC(pwpDefaultPg2Rtdd.getId());
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);

        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefaultPg2Rtdd, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO, pwpDefaultPg2Rtdd);

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO,Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString());

        CorporateHelpers.validateSuccessNativeTxnStatus(orderDTO, Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString());

        CorporateHelpers.validateSuccessPaymentStatusAPI(orderDTO,pwpDefaultPg2Rtdd, Constants.Gateway.PAYTM.toString(),Constants.Gateway.PAYTM.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString());


        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO, pwpDefaultPg2Rtdd);
    }

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via DC corporate Card on corporate PWP Merchant.", groups = "P0")
    public void PWP_succesfulDCCorporate(@Optional("enhancedweb") String theme) throws Exception{
        String payMode = "DC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd, payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        CorporateHelpers.assertCorporateCardCC(pwpDefaultPg2Rtdd.getId());
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);

        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDefaultPg2Rtdd, theme, user)
                .build();
        WalletHelpers.setZeroBalance(user);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO, pwpDefaultPg2Rtdd);

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO,Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString());

        CorporateHelpers.validateSuccessNativeTxnStatus(orderDTO, Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString());

        CorporateHelpers.validateSuccessPaymentStatusAPI(orderDTO,pwpDefaultPg2Rtdd, Constants.Gateway.PAYTM.toString(),Constants.Gateway.PAYTM.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString(), Constants.Gateway.PAYTM.toString());

        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO, pwpDefaultPg2Rtdd);
    }



    //Hybrid transaction //

    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via CC + Wallet", groups = "P0")
    public void PWP_10_successfulPWPHybridCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "CC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount -1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via DC + Wallet", groups = "P0")
    public void PWP_11_successfulPWPHybridDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String payMode = "DC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount -1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via NB + Wallet", groups = "P0")
    public void PWP_12_successfulPWPHybridNB(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "NB";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount -1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.valueOf(payMode),paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via EMI only", groups = "P0")
    public void PWP_13_successfulPWPHybridEMI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "EMI";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    //EMI DC refund is offline
    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via EMI_DC + Wallet", groups = "P0")
    public void PWP_14_successfulPWPHybridEMIDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String payMode = "EMI_DC";
        assertPWPPrefEnabled(pwpHybrid,payMode);
        assertSMSPrefEnabled(pwpHybrid);
        assertRefundAllowed(pwpHybrid);
        assertRefundSuccessNotifyPeon(pwpHybrid);

        User user = userManager.getForWrite(Label.EMIDC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybrid, theme, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER).setBankName("ICICI Bank Debit Card").setMonth(3);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }

    //UPI Refund is pending for Hybrid txn, to be addded in manual regression
    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via UPI + Wallet", groups = "P0")
    public void PWP_15_successfulPWPHybridUPI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "UPI";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount -1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
       // PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
       // PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybrid);
    }

    //Paytm CC refund is supposed to happen offline
    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via PaytmCC + Wallet", groups = "P0")
    public void PWP_16_successfulPWPHybridPaytmCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "PAYTM_DIGITAL_CREDIT";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        User user = userManager.getForWrite(Label.POSTPAID);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount -1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }


    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Saved Card + Wallet", groups = "P0")
    public void PWP_17_successfulPWPOnlyHybridPaytmSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        String payMode = "DC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());

        WalletHelpers.modifyBalance(user,txnAmount -1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    @Parameters({"theme"})
    @Test(description = "Validate failed PWP Hybrid transaction CC + Wallet", groups = "P0")
    public void PWP_18_failedPWPOnlyHybridCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String payMode = "CC";
        Double txnAmount = 100.98;
        assertPWPPrefEnabled(pwpHybrid,payMode);
        assertSMSPrefEnabled(pwpHybrid);
        assertRefundAllowed(pwpHybrid);
        assertRefundSuccessNotifyPeon(pwpHybrid);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybrid, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        PWPHelpers.validateFailureResponsePWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateFailureNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateFailurePaymentStatusAPIPWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateFailureTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateFailurePeonPWPTxn(orderDTO);
        PWPHelpers.validateFailureSMSViaPaytm(orderDTO);

    }

    @Parameters({"theme"})
    @Test(description = "Validate successful non PWP transaction via CC without login", groups = "P1")
    public void PWP_19_successfulNONPWPOnlyCCWithoutLogin(@Optional("enhancedweb") String theme) {
        String payMode= "CC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pwpHybridPg2Rtdd, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.valueOf(payMode));

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(pwpHybridPg2Rtdd.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
    }

    //NB refund is offline
    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via retry", groups = "P0")
    public void PWP_20_successfulPWPHybridRetry(@Optional("enhancedweb") String theme) throws Exception {

        assertPWPPrefEnabled(pwpRetry,"CC","DC","NB");
        assertSMSPrefEnabled(pwpRetry);
        assertRefundAllowed(pwpRetry);
        assertRefundSuccessNotifyPeon(pwpRetry);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.modifyBalance(user,txnAmount -1.00);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpRetry, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();

        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN).setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP))
        {cashierPage.closeCcDcDetailBtn().click();}
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpRetry);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpRetry);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }

    @Parameters({"theme"})
    @Test(description = "Validate successful hybrid PWP transaction via HDFO", groups = "P0")
    public void PWP_21_successfulPWPHybridDirect(@Optional("enhancedweb_revamp") String theme) throws Exception {

        assertSMSPrefEnabled(pwpDirect);
        assertRefundAllowed(pwpDirect);
        assertRefundSuccessNotifyPeon(pwpDirect);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpDirect, theme, user)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.CC);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDirect);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDirect);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDirect);
    }


    @Parameters({"theme"})
    @Test(description = " Validate Pending Txn via PWP")
    public void PWP_22_pendingTxnPWP(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        OrderDTO orderDTO = new OrderFactory.Hybrid(pwpHybridPg2Rtdd, theme, user)
                .setTXN_AMOUNT("99.51")
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PAYTM")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateBankName(Constants.Gateway.PAYTM.toString())
                .validateCheckSum(pwpHybridPg2Rtdd.getKey()).assertAll();
    }


    //Show Payment Page Default

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP default transaction via Show Payment Page Flow through CC", groups = "P0")
    public void PWP_23_successfulPWPDefaultShowPaymentPageCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "CC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefaultPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefaultPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Default transaction via Show Payment Page Flow through DC", groups = "P0")
    public void PWP_24_successfulPWPDefaultShowPaymentPageDC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "DC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefaultPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefaultPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    //NB refund is offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via Show Payment Page Flow through NB", groups = "P0")
    public void PWP_25_successfulPWPDefaultShowPaymentPageNB(@Optional("enhancedweb") String theme) throws Exception {
        PaymentDTO paymentDTO  = new PaymentDTO();
        String payMode= "NB";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefaultPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        paymentDTO.setBankName("ICICI");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefaultPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Default transaction via Show Payment Page Flow through PPBL only", groups = "P0")
    public void PWP_26_successfulPWPDefaultShowPaymentPagePPBL(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "NB";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefaultPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefaultPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    //UPI refund is offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via Show Payment Page Flow through UPI", groups = "P0")
    public void PWP_27_successfulPWPDefaultShowPaymentPageUPI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "UPI";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefaultPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefaultPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via Show Payment Page Flow through PPI(Balance)", groups = "P0")
    public void PWP_28_successfulPWPDefaultShowPaymentPagePPI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "PPI";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefaultPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefaultPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    //Paytm CC refund is supposed to be offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Native transaction via Show Payment Page Flow through PAYTM_DIGITAL_CREDIT", groups = "P0")
    public void PWP_28_successfulPWPDefaultShowPaymentPagePaytmCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "PAYTM_DIGITAL_CREDIT";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.POSTPAID);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpDefaultPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpDefaultPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    //Show Payment Page Hybrid

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through CC", groups = "P0")
    public void PWP_29_successfulPWPHybridShowPaymentPageCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "CC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through DC", groups = "P0")
    public void PWP_30_successfulPWPHybridShowPaymentPageDC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "DC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    //NB refund is offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through NB", groups = "P0")
    public void PWP_31_successfulPWPHybridShowPaymentPageNB(@Optional("enhancedweb") String theme) throws Exception {
        PaymentDTO paymentDTO  =new PaymentDTO();
        String payMode= "NB";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        paymentDTO.setBankName("ICICI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }


    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through PPBL", groups = "P0")
    public void PWP_32_successfulPWPHybridShowPaymentPagePPBL(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "NB";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    //UPI refund is offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through UPI", groups = "P0")
    public void PWP_33_successfulPWPHybridShowPaymentPage(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "UPI";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through PPI(Balance)", groups = "P0")
    public void PWP_34_successfulPWPHybridShowPaymentPagePPI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "PPI";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    //Paytm CC refund is supposed to be offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through PAYTM_DIGITAL_CREDIT", groups = "P0")
    public void PWP_35_successfulPWPHybridShowPaymentPagePaytmCC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "PAYTM_DIGITAL_CREDIT";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.POSTPAID);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through EMI only", groups = "P0")
    public void PWP_36_successfulPWPHybridShowPaymentPageEMI(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "EMI";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    //EMI DC refund is supposed to be offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through EMI DC only", groups = "P0")
    public void PWP_37_successfulPWPHybridShowPaymentPageEMIDC(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "EMI_DC";
        assertPWPPrefEnabled(pwpHybrid,payMode);
        assertSMSPrefEnabled(pwpHybrid);
        assertRefundAllowed(pwpHybrid);
        assertRefundSuccessNotifyPeon(pwpHybrid);
        User user = userManager.getForWrite(Label.EMIDC);
        WalletHelpers.setZeroBalance(user);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybrid)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER).setBankName("ICICI Bank Debit Card").setMonth(3);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybrid,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    //NB refund is offline
    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via Show Payment Page Flow through Retry Flow", groups = "P0")
    public void PWP_38_successfulPWPHybridShowPaymentPageRetry(@Optional("enhancedweb") String theme) throws Exception {
        assertPWPPrefEnabled(pwpRetryPg2Rtdd,"CC","DC","NB");
        assertSMSPrefEnabled(pwpRetryPg2Rtdd);
        assertRefundAllowed(pwpRetryPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpRetryPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpRetryPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN).setBankName("ICICI");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpRetryPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.NB,paymentDTO);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpRetryPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpRetryPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP transaction via Show Payment Page Flow through Saved Card", groups = "P0")
    public void PWP_39_successfulPWPHybridShowPaymentPageSavedCard(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "CC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.SAVED_CARD);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PWP Hybrid transaction via Show Payment Page Flow through DC non logged in flow", groups = "P1")
    public void PWP_41_successfulPWPHybridShowPaymentPageNonLoggedInFlow(@Optional("enhancedweb") String theme) throws Exception {
        String payMode= "DC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,txnAmount-1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, pwpHybridPg2Rtdd)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(pwpHybridPg2Rtdd,initTxnDTO.getBody().getOrderId(),txnToken)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(pwpHybridPg2Rtdd.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("HDFC"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("HDFC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("CC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );

    }

}