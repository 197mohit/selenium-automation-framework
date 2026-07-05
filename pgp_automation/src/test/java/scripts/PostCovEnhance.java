package scripts;

import com.paytm.LocalConfig;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.util.WalletUtil;
import com.paytm.utils.merchant.util.exception.pgpException.PGPException;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.text.DecimalFormat;
import java.util.*;

import static com.paytm.appconstants.Constants.Owner.DEEPAK;
import static com.paytm.base.test.Group.Status.TO_BE_FIXED;
import static com.paytm.pages.responsePage.ResponsePage.Attribute.*;
import static org.hamcrest.Matchers.*;

/**
 * Created by anjukumari on 02/04/19
 */

/**
 * merchant( AmxPcf44713097016764 )
 * CC: 1
 * Amex: 2
 * Wallet: 3
 * HDFC-NB: 11
 * ICICI-NB: 12
 * UPI-slab based: (Amount 0-10):6, (Amount 100-1000):100
 * EMI-HDFC:7
 * PAYTMCC: 10
 * DC-slab based: (Amount 0-10):4, (Amount 100-1000):14
 * <p>
 * <p>
 * merchant( NBPCF119529493331725 )
 * CC: 1
 * Wallet: 3
 * HDFC-NB: 11
 * ICICI-NB: 12
 * UPI-slab based: (Amount 0-10):6, (Amount 100-1000):100
 * EMI-HDFC:7
 * PAYTMCC: 10
 * DC-slab based: (Amount 0-10):4, (Amount 100-1000):14
 */

/*
 merchant( NBPCF119529493331725 )
 CC: 1
 Wallet: 3
 HDFC-NB: 11
 ICICI-NB: 12
 UPI-slab based: (Amount 0-10):6, (Amount 100-1000):100
 EMI-HDFC:7
 PAYTMCC: 10
 DC-slab based: (Amount 0-10):4, (Amount 100-1000):14
 */

/**
 merchant( flaFee61923198013531 )
 All paymodes: 1.12
 *
 */
@Owner("Tarun")
public class PostCovEnhance extends PGPBaseTest {
    public static final String pcf_EMI_HDFC = "7.00";
    public static final String pcf_NETBANKING_ICICI = "1.20";
    public static final String pcf_NETBANKING_HDFC = "11.00";
    public static final String pcf_flat = "1.20";
    public static final String pcf_wallet = "3.00";
    public static final String pcf_UPI_upTo10 = "6.00";
    public static final String pcf_UPI_moreThan100 = "100.00";
    public static final String pcf_PPBL = "1.20";
    public static final String pcf_CC = "1.00";
    public static final String pcf_DC_upTo10 = "4.00";
    public static final String pcf_Amex = "1.00";
    private static final String HDFC_DROPDOWN_BANK_NAME = "HDFC Bank Credit Card";
    private final PcfHelpher pcfHelpher = new PcfHelpher();
    private final CheckoutPage checkoutPage = new CheckoutPage();

    private void ValidateNetworkLogPcf(String val, boolean exist) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<LogEntry> logList = DriverManager.getDriver().manage().logs().get(LogType.PERFORMANCE).getAll();
        boolean isPresent = false;
        for (LogEntry entry : logList) {
            String mess = entry.getMessage();
            if (mess.contains("Network.requestWillBeSent") && mess.contains("fetchPcfDetails") && mess.contains(val)) {
                isPresent = true;
                break;
            }
        }
        Assertions.assertThat(exist).withFailMessage("check log for exist: " + exist + ", text present in log is : " + isPresent).isEqualTo(isPresent);
    }

    private List<String> getListOfPayModesOnCashierPage(CashierPage cashierPage) {
        List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
        List<String> paymethodList = new ArrayList<>();
        for (int i = 0; i < PaymodesOnPage.size(); i++) {
            paymethodList.add(PaymodesOnPage.get(i).getText().split("\n")[0]);
        }
     //   int nbIndex = paymethodList.indexOf("Net");
     //   paymethodList.set(nbIndex, "Net Banking");
     //   int emiIndex = paymethodList.indexOf("Easy");
     //   paymethodList.set(emiIndex, "Easy EMI");
        int cardsIndex = paymethodList.indexOf("Debit, Credit Cards");
        paymethodList.remove(cardsIndex);
        paymethodList.add("Debit");
        paymethodList.add("Credit");
        System.out.println(paymethodList);
        return paymethodList;
    }

    private void clearNetworkLogs() {
        DriverManager.getDriver().manage().logs().get(LogType.PERFORMANCE).getAll();
    }

    private void validateCommisionOnCashier(CashierPage cashierPage, String payMode, OrderDTO orderDTO, Double percentCommsion, Double flatCommission) throws Exception {
        switch (payMode) {
            case "Paytm Balance":
                cashierPage.checkBoxPPI().check();
                cashierPage.pause(1);
                break;

            case "Paytm Postpaid":
                cashierPage.checkboxPaytmCC().click();
                cashierPage.pause(1);
                break;

            case "Paytm Payments Bank":
                cashierPage.checkboxPPBL().check();
                cashierPage.pause(1);
                break;

            case "EMI":
                cashierPage.tabEMI().click();
                PaymentDTO paymentDTO = new PaymentDTO();
                paymentDTO.setBankName(Constants.Bank.HDFCBANK.toString())
                        .setCreditCardNumber(PaymentDTO.promoCC);
                cashierPage.fillEMIDetails(paymentDTO);
                cashierPage.closePMDetailBtn().click();
                break;

            case "Debit":
                cashierPage.tabDebitCard().click();
                cashierPage.fillDCDetails(new PaymentDTO());
                cashierPage.pause(1);
                cashierPage.closeCcDcDetailBtn().click();
                break;

            case "Credit":
                cashierPage.tabCreditCard().click();
                cashierPage.fillCCDetails(new PaymentDTO());
                cashierPage.pause(1);
                cashierPage.closeCcDcDetailBtn().click();
                break;

            case "UPI":
                cashierPage.tabUPI().click();
                cashierPage.pause(2);
                cashierPage.closePMDetailBtn().click();
                break;

            case "Net Banking":
                cashierPage.tabNetBanking().click();
                cashierPage.pause(1);
                break;


            default:
                throw new Exception(payMode + " paymode locator not present please add this paymode locator");
        }
        pcfHelpher.validateCommision(cashierPage, orderDTO, 0.0, flatCommission);
    }
    private void validateCommissionEnhance(SoftAssertions softAssert, CashierPage cashierPage, double baseAmount, double percentCommission, double flatCommission, String paymentMode) throws InterruptedException{
        double actualChargeFeeAmt;
        double actualTotalAmt;
        double baseAmt = baseAmount;
        synchronized (this) {
            wait(1000);
        }
        actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.chargeFeeAmtPG().getText()));
        actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPG().getText()));


        double expectedChargeFeeAmt = convenienceFeeCalculator(baseAmount, percentCommission, flatCommission, paymentMode);
        double expectedTotalAmt = CommonHelpers.doubleHalfUpConvertor(baseAmt + expectedChargeFeeAmt);

        softAssert.assertThat(baseAmt).as(paymentMode).isEqualTo(baseAmt);
        softAssert.assertThat(actualChargeFeeAmt).as(paymentMode).isEqualTo(expectedChargeFeeAmt);
        softAssert.assertThat(actualTotalAmt).as(paymentMode).isEqualTo(expectedTotalAmt);
    }

    private void validateCommissionEnhanceNew(SoftAssertions softAssert, CashierPage cashierPage, double baseAmount, double percentCommission, double flatCommission, String paymentMode) throws InterruptedException{
        double actualChargeFeeAmt;
        double actualTotalAmt;
        double baseAmt = baseAmount;
        synchronized (this) {
            wait(1000);
        }
        String chargeFeeAmt=cashierPage.chargeFeeAmtPGNew().getText();
        String chargeFeeAmt1=cashierPage.chargeFeeAmtPGNew().getText();
        if(chargeFeeAmt.contains("Rs") ||chargeFeeAmt1.contains("Rs"))
        {
            chargeFeeAmt= chargeFeeAmt.replace("Rs","");
            chargeFeeAmt1= chargeFeeAmt1.replace("Rs","");
        }
        actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(chargeFeeAmt));
        actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(chargeFeeAmt1));



        double expectedChargeFeeAmt = convenienceFeeCalculator(baseAmount, percentCommission, flatCommission, paymentMode);
        double expectedTotalAmt = CommonHelpers.doubleHalfUpConvertor(baseAmt + expectedChargeFeeAmt);

        softAssert.assertThat(baseAmt).as(paymentMode).isEqualTo(baseAmt);
        softAssert.assertThat(actualChargeFeeAmt).as(paymentMode).isEqualTo(expectedChargeFeeAmt);
        softAssert.assertThat(actualTotalAmt).as(paymentMode).isEqualTo(expectedTotalAmt);
    }
    private HashMap<String, String> getAllConvFeeInPostConvTable(List<String> PaymodesOnPageTextList, CashierPage cashierPage) throws Exception {
        HashMap<String, String> listInDropDown = new HashMap<>();
        for (int i = 0; i < PaymodesOnPageTextList.size(); i++) {
            String payMode = PaymodesOnPageTextList.get(i);
            if(!payMode.isEmpty())
            listInDropDown.put(payMode, cashierPage.tableConvFeeCharge().getRowValue(payMode));
        }
        return listInDropDown;
    }


    @Parameters("theme")
    @Test(description = "Verify number of payment modes on cashier page is equal to number of paymodes on conv fee dropdown")
    public void Verify_numberOfFeeDisplayingIsEqualToNumberOfPayModeOnCashier(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        Test:
        {
            User user = userManager.getForWrite(Label.PPBL);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            //WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()));
            //SavedCardHelpers.deleteSavedCard(user);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
           // cashierPage.tabPPBL().click();
            cashierPage.pause(1);
            cashierPage.convinenceCharge().waitUntilClickable();
            cashierPage.convinenceCharge().click();
            int numOfPaymodesOnPage = cashierPage.ListOfPayModsOnCashier().size();
            int numOfPaymodesOnConvFeeDropDown = cashierPage.getConvenienceFeeTableRowCount();
            Assertions.assertThat(numOfPaymodesOnPage)
                    .withFailMessage("count of Paymodes on cashier Page not equal to count in conv fee dropdown")
                    .isEqualTo(numOfPaymodesOnConvFeeDropDown);
        }
    }

    //As per New Theme UI Dropdown is not coming anymore
//    @Parameters("theme")
//    @Test(enabled = false, description = "Verify correct fee types displaying for flat conv in conv fee list box")
    public void Verify_CorrectFee_type_DisplayingOnConvFeeDropDown_flatCommision(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC, Label.POSTPAID);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            SavedCardHelpers.deleteSavedCard(user);
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()));
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabDebitCard().click();
            List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
            List<String> paymodesTextList=new ArrayList<>();
            for(UIElement element:PaymodesOnPage){
                paymodesTextList.add(element.getText().split("\n")[0]);
            }
            cashierPage.pause(1);
            cashierPage.convinenceCharge().waitUntilClickable();
            cashierPage.convinenceCharge().click();
            cashierPage.pause(1);
            HashMap<String, String> listInDropDown = getAllConvFeeInPostConvTable(paymodesTextList, cashierPage);
            for (int i = 0; i < paymodesTextList.size(); i++) {
                String paymode = paymodesTextList.get(i);
                Reporter.report.info("validating " + paymode + " in convFee list box");
                Assertions.assertThat(listInDropDown.get(paymode)).withFailMessage("conv fee of " + PaymodesOnPage.get(i).getText() + " not present in conv fee list box").isNotNull();
            }
        }
    }

    //As per New Theme UI Dropdown is not coming anymore
//    @Parameters("theme")
//    @Test(enabled = false, description = "Verify correct fee values displaying for flat conv in conv fee list box")
    public void Verify_CorrectFee_Value_DisplayingOnConvFeeDropDown_flatCommision(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        String flatCommission = "1.12";
        Test:
        {
            User user = userManager.getForWrite(Label.POSTPAID);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT())-1);
            SavedCardHelpers.deleteSavedCard(user);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabDebitCard().click();
            List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
            List<String> paymodesTextList=new ArrayList<>();
            for(UIElement element:PaymodesOnPage){
                paymodesTextList.add(element.getText().split("\n")[0]);
            }
            cashierPage.pause(1);
            cashierPage.convinenceCharge().click();
            HashMap<String, String> listInDropDown = getAllConvFeeInPostConvTable(paymodesTextList, cashierPage);
            for (String entry : listInDropDown.keySet()) {
                Reporter.report.info("validating " + entry + " in convFee value list box");
                Assertions.assertThat(listInDropDown.get(entry)).isEqualToIgnoringCase(flatCommission);
            }
        }
    }

    @Parameters("theme")
    @Test(description = "Verify Total txn amount on cashier page for Default commission(same config)")
    public void Verify_TotalTxnAmountOnCashierPage_forFlatCommision(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();//MerchantManager.getMerchant();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        double flatCommission = 1.12;

        Test:
        {
            //User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    //.setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
           // WalletHelpers.modifyBalance(user, Double.valueOf(order.getTXN_AMOUNT()));
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabDebitCard().click();
            cashierPage.textBoxCardNumber().click();
            cashierPage.pause(2);
            validateCommissionEnhanceNew(new SoftAssertions(), cashierPage, Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC");
        }
    }
    @Parameters("theme")
    @Test(description = "Verify fee for all paymodes on cashier page for Default commission(same config)")
    public void Verify_FeeOnCashierPageForAllPayModes_forFlatCommision(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        double flatCommission = 1.12;
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC, Label.PPBL);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, 5.0);
            PostpaidHelpers.updateBalance("5");
            SavedCardHelpers.deleteSavedCard(user);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
            System.out.println(paymethodList);
            for (int i = 0; i < paymethodList.size(); i++) {
                cashierPage.pause(2);
                // this will fail in new revamped UI as it is changed now need to be updated
                validateCommisionOnCashier(cashierPage, paymethodList.get(i), order, 0.0, flatCommission);
            }
        }
    }


    @Parameters("theme")
    @Test(groups = TO_BE_FIXED, description = "Verify fee is saved in cache, not hit for fetch PCF detail found when DC is select second time")
    public void Verify_feeSavedInCache_forDC(@Optional("enhancedweb") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        double flatCommission = 1.12;
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            double expectedTotalTxnAmt = convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0.0, flatCommission, "")
                    + Double.parseDouble(order.getTXN_AMOUNT());
            expectedTotalTxnAmt = Math.ceil(expectedTotalTxnAmt);
            WalletHelpers.modifyBalance(user, expectedTotalTxnAmt);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate for debitCard
            clearNetworkLogs();
            cashierPage.tabDebitCard().click();
            ValidateNetworkLogPcf("DEBIT_CARD", true);
            clearNetworkLogs();
            cashierPage.tabDebitCard().click();
            ValidateNetworkLogPcf("DEBIT_CARD", false);
        }
    }


    @Parameters("theme")
    @Test(groups = TO_BE_FIXED, description = "Verify fee is saved in cache, not hit for fetch PCF detail found when NB is select second time")
    public void Verify_feeSavedInCache_forNB(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        double flatCommission = 1.12;
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            double expectedTotalTxnAmt = convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0.0, flatCommission, "")
                    + Double.parseDouble(order.getTXN_AMOUNT());
            expectedTotalTxnAmt = Math.ceil(expectedTotalTxnAmt);
            WalletHelpers.modifyBalance(user, expectedTotalTxnAmt);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate for netBanking
            clearNetworkLogs();
            cashierPage.tabNetBanking().click();
            ValidateNetworkLogPcf("NET_BANKING", true);
            clearNetworkLogs();
            cashierPage.tabNetBanking().click();
            ValidateNetworkLogPcf("NET_BANKING", false);
        }
    }


    @Parameters("theme")
    @Test(description = "Verify conv fee for cc is visible after login, if cc is selected after login")
    public void Verify_CCFeeShouldBeVisibleAfterLogIn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        Test:
        {
            User user = userManager.getForWrite(Label.LOGIN);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()));
            SavedCardHelpers.deleteSavedCard(user);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().click();
            double amountForCConCashier = Double.parseDouble(cashierPage.chargeFeeAmtPG().getText());
            cashierPage.closeCcDcDetailBtn().click();
            cashierPage.signin(user.mobNo(), user.password());
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().click();
            cashierPage.pause(1);
            double amountForCConCashierAfterLogin = Double.parseDouble(cashierPage.chargeFeeAmtPG().getText());
            Assertions.assertThat(amountForCConCashier).withFailMessage("Amount before login was: " + amountForCConCashier + ", amount after login was: " + amountForCConCashierAfterLogin).isEqualTo(amountForCConCashierAfterLogin);
        }
    }

//    @Parameters("theme")
//    @Test(description = "Verify conv fee for Wallet is visible after login, if cc was selected before login when wallet is selected after login", enabled = false)
    public void Verify_WalletFeeShouldBeVisibleAfterLogsIn(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        double ppiFlatCommission = 3;
        Test:
        {
            User user = userManager.getForWrite(Label.LOGIN);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT("2")
                    .setMerchantKey(key).build();
            WalletUtil.getWalletBalance(LocalConfig.WALLET_HOST, user.ssoToken());
            WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().click();
            cashierPage.pause(1);
            double amountForCConCashier = Double.parseDouble(cashierPage.chargeFeeAmtPG().getText());
            cashierPage.signin(user.mobNo(), user.password());
            double amountForCConCashierAfterLogin = Double.parseDouble(cashierPage.chargeFeeAmtPG().getText());
            Assertions.assertThat(amountForCConCashier).withFailMessage("Amount before login was: " + amountForCConCashier + ", amount after login was: " + amountForCConCashierAfterLogin).isNotEqualTo(amountForCConCashierAfterLogin);
            double expectedChargeFeeAmtForWallet = convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0.0, ppiFlatCommission, "");
            Assertions.assertThat(amountForCConCashier).withFailMessage("amount after login should be: " + expectedChargeFeeAmtForWallet).isNotEqualTo(expectedChargeFeeAmtForWallet);
        }
    }

 /*
    public void validatePCFTxn(TxnStatus order, Double percentCommsion, Double flatCommission) throws InterruptedException {
        Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> new BizOrderSearch(order.txnStatusResponse.getTXNID(), "BIZ_ORDER_ID").execute().jsonPath().getString("response.body.orders[0].extendInfo"), Matchers.containsString("FEE_AND_TAX_CHARGE_CHANNEL_INFO"));
        Response search = new BizOrderSearch(order.txnStatusResponse.getTXNID(), "BIZ_ORDER_ID").execute();
        String result = search.jsonPath().getString("response.body.orders.extendInfo[0]");
        String amount = new JSONObject(new JSONArray(new JSONObject(result).get("FEE_AND_TAX_CHARGE_CHANNEL_INFO").toString()).get(0).toString()).get("amount").toString();
        String commissionInResult = new JsonPath(amount).get("amount").toString();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.parseDouble(order.txnStatusResponse.getTXNAMOUNT()), percentCommsion, flatCommission, "");
        Assertions.assertThat(Double.compare(expectedChargeFeeAmt,Double.parseDouble(commissionInResult))).withFailMessage("Incorrect commision deducted for this txn").isZero();
    }

  */

    public void validatePCFTxn(TxnStatus order, Double percentCommsion, Double flatCommission) throws InterruptedException {
        String commissionInResult = order.txnStatusResponse.getChargeAmount();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.parseDouble(order.txnStatusResponse.getTXNAMOUNT()), percentCommsion, flatCommission, "");
        Assertions.assertThat(Double.compare(expectedChargeFeeAmt,Double.parseDouble(commissionInResult))).withFailMessage("Incorrect commision deducted for this txn").isZero();
    }


    @Parameters("theme")
    @Test(description = "Verify add n pay txn when commision amount is added, initially txn amount was less than to wallet balance, Also validate commision of wallet is deducted")
    public void Verify_AddnpayDueToPCFWhenWalletBalanceLessTxnTxnAmount(@Optional("enhancedweb") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setTXN_AMOUNT("10")
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()) - 1);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            cashierPage.payBy(Constants.PayMode.CC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, Double.parseDouble(PostCovEnhance.pcf_wallet), "PPI"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(mid)
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_wallet));
        }
    }


    @Parameters("theme")
    @Test(description = "Verify peon send successful CC txn with flat commision when user is logged in also validate peon ")
    public void verify_successfulCCTxnWithFlatCommsion_peon(@Optional("enhancedweb") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()));
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

            if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB) || theme.equalsIgnoreCase(Constants.Theme.ENHANCEDWAP))
            { cashierPage.tabCreditCard().click();
                pcfHelpher.validateCommision(cashierPage, order, 0.0, Double.parseDouble(PostCovEnhance.pcf_CC));}
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            Peon peon = new Peon(order.getORDER_ID());
            peon.executeUntilGetResponse();
        }
    }


    @Parameters("theme")
    @Test(description = "Verify successful CC txn with flat commision without login")
    public void verify_successfulCCTxnWithFlatCommsion_withoutLogin(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        double flatCommission = 1;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate for creditcard
            if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB) || theme.equalsIgnoreCase(Constants.Theme.ENHANCEDWAP))
            {
            cashierPage.tabCreditCard().click();
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);}
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            Peon peon = new Peon(order.getORDER_ID());
            peon.executeUntilGetResponse();
            validatePCFTxn(txnStatus, 0.0, flatCommission);
        }

    }


    @Parameters("theme")
    @Test(description = "Verify successful DC txn with flat commision ")
    public void verify_successfulDCTxnWithFlatCommsion(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        double flatCommission = 4;
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()));
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate for debitCard
            cashierPage.tabDebitCard().click();
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.DC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0.0, flatCommission, "DC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("DC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, flatCommission);
        }
    }

    @Owner(DEEPAK)
    @Parameters("theme")
    @Test
    public void testNotGettingChargeBackAmtInMerchantStatusAPIWhenRequiredPrefIsDisabled(@Optional("enhancedwap_revamp") String theme) {
        String mId = Constants.MerchantType.BRAND_BO_DISC_HDFC.getId();
        String mKey = Constants.MerchantType.BRAND_BO_DISC_HDFC.getKey();
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mId, theme)
                    .setMerchantKey(mKey).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.DC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus("TXN_SUCCESS")
                    .validateChargeAmount(Constants.ValidationType.NOT_PRESENT)
                    .AssertAll();
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                    (order.getORDER_ID(), mId, mKey)
                    .build();
            new GetPaymentStatus(getPaymentStatusDTO).execute().then()
                    .body("body.resultInfo.resultStatus", equalTo("TXN_SUCCESS"))
                    .body("body", not(hasKey("chargeAmount")));
        }
    }

    @Owner(DEEPAK)
    @Parameters("theme")
    @Test
    public void testGettingChargeBackAmtInMerchantStatusAPIWhenRequiredPrefIsEnabledAndTxnIsSuccessful(@Optional("enhancedweb") String theme) {
        String mId = Constants.MerchantType.PGOnly_Pcf.getId();
        String mKey = Constants.MerchantType.PGOnly_Pcf.getKey();
        double flatCommission = new Merchant(mId, mKey, false).getCommissions().stream()
                .filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1)
                .filter(commission -> "cc".equalsIgnoreCase(commission.getPayMode()))
                .filter(commission -> commission.getFixedFee() > 0)
                .findAny()
                .get()
                .getFixedFee();
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mId, theme)
                    .setMerchantKey(mKey).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.DC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus("TXN_SUCCESS")
                    .validateChargeAmount(Constants.ValidationType.NON_EMPTY)
                    .AssertAll();
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                    (order.getORDER_ID(), mId, mKey)
                    .build();
            new GetPaymentStatus(getPaymentStatusDTO).execute().then()
                    .body("body.resultInfo.resultStatus", equalTo("TXN_SUCCESS"))
                    .body("body", hasKey("chargeAmount"))
                    .body("body.chargeAmount", equalTo(String.valueOf(convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0, flatCommission, "CC"))));
        }
    }

    @Owner(DEEPAK)
    @Parameters("theme")
    @Test
    public void testGettingChargeBackAmtInMerchantStatusAPIWhenRequiredPrefIsEnabledAndTxnIsFailure(@Optional("enhancedweb") String theme) {
        String mId = Constants.MerchantType.PGOnly_Pcf.getId();
        String mKey = Constants.MerchantType.PGOnly_Pcf.getKey();
        double flatCommission = new Merchant(mId, mKey, false).getCommissions().stream()
                .filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1)
                .filter(commission -> "cc".equalsIgnoreCase(commission.getPayMode()))
                .filter(commission -> commission.getFixedFee() > 0)
                .findAny()
                .get()
                .getFixedFee();
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mId, theme)
                    .setMerchantKey(mKey).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
            cashierPage.waitUntilLoads();
            cashierPage.clickFailedTxnGotItButtonIfDisplayed();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .executeUntilNotPending()
                    .validateStatus("TXN_FAILURE")
                    .validateChargeAmount(Constants.ValidationType.NON_EMPTY)
                    .AssertAll();
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                    (order.getORDER_ID(), mId, mKey)
                    .build();
            new GetPaymentStatus(getPaymentStatusDTO).execute().then()
                    .body("body.resultInfo.resultStatus", equalTo("TXN_FAILURE"))
                    .body("body", hasKey("chargeAmount"))
                    .body("body.chargeAmount", equalTo(String.valueOf(convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0, flatCommission, "CC"))));
        }
    }

    @Owner(DEEPAK)
    @Parameters("theme")
    @Test
    public void testGettingChargeBackAmtInMerchantStatusAPIWhenRequiredPrefIsEnabledAndTxnIsPending(@Optional("enhancedweb") String theme) {
        String mId = Constants.MerchantType.PGOnly_Pcf.getId();
        String mKey = Constants.MerchantType.PGOnly_Pcf.getKey();
        double flatCommission = new Merchant(mId, mKey, false).getCommissions().stream()
                .filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1)
                .filter(commission -> "cc".equalsIgnoreCase(commission.getPayMode()))
                .filter(commission -> commission.getFixedFee() > 0)
                .findAny()
                .get()
                .getFixedFee();
        double totalTxnAmtForPendingState = 77D;
        double serviceTax = flatCommission * 0.18;
        String txnAmtForPendingState = new DecimalFormat("0.00").format(totalTxnAmtForPendingState - flatCommission - serviceTax);
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mId, theme)
                    .setTXN_AMOUNT(txnAmtForPendingState)
                    .setMerchantKey(mKey).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            new TxnStatus(order.getMID(), order.getORDER_ID())
                    .execute().then()
                    .body("STATUS", equalTo("PENDING"))
                    .body("", not(hasKey("chargeAmount")));
            GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                    (order.getORDER_ID(), mId, mKey)
                    .build();
            new GetPaymentStatus(getPaymentStatusDTO).execute().then()
                    .body("body.resultInfo.resultStatus", equalTo("PENDING"))
                    .body("body", not(hasKey("chargeAmount")));
        }
    }

    private void validateHDFC_NBFee(CashierPage cashierPage, PaymentDTO nbDetails) {
        cashierPage.tabNetBanking().click();
        cashierPage.netBankingOtherBank().click();
        cashierPage.textBoxSearchBank().clearAndType(nbDetails.getBankName());
        cashierPage.radioBtnSearchedBank(nbDetails.getBankName()).click();
        cashierPage.buttonPGPayNow().click();
    }

    @Parameters("theme")
    @Test(description = "Verify successful NB txn with default commision 1.12")
    public void verify_successful_NBTxnWithFlatCommsion(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        double flatCommission = 1.12;
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()));
            SavedCardHelpers.deleteSavedCard(user);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabNetBanking().click();
            //validate NB default Fee
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
            cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0.0, flatCommission, "NB"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.ICICI.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.ICICINB.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("NB")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }


    @Parameters("theme")
    @Test(description = "Verify wallet is not selected when txn amount equal to wallet amount ")
    public void verify_successfulAddnpayTxn_WithFlatCommsionWhenTxnamountEqualToWalletAmount(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        Test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setTXN_AMOUNT("900000")
                    .setMerchantKey(key).build();
           // WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()));
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.checkBoxPPI().assertUnChecked();
        }
    }


    @Parameters("theme")
    @Test(description = "Verify successful wallet only txn, default commision=3, with token ")
    public void verify_successful_Wallet_TxnWithFlatCommsion_token(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        User user = userManager.getForWrite(Label.BASIC);
        double flatCommission = 3;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            double expectedTxnAmount = Double.parseDouble(order.getTXN_AMOUNT()) + convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0.0, flatCommission, "");
            WalletHelpers.modifyBalance(user, expectedTxnAmount);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate commision for wallet
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.WALLET);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "PPI"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("WALLET")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
        Assertions.assertThat(WalletUtil.getWalletBalance(LocalConfig.WALLET_HOST, user.ssoToken())).isEqualTo(0);
    }

   /* @Owner("Deepak")
    @Parameters("theme")
    @Test(description = "Verify successful wallet only txn, default commision=3, with login on cashier", enabled = false)
    public void verify_successful_Wallet_TxnWithFlatCommsion_login(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        User user = userManager.getForWrite(Label.LOGIN);
        double flatCommission = 3;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            double expectedTxnAmount = Double.parseDouble(order.getTXN_AMOUNT()) + convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0.0, flatCommission, "");
            WalletHelpers.modifyBalance(user, expectedTxnAmount);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.login(user);
            //Validate commision for wallet
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.WALLET);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("WALLET")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
        Assertions.assertThat(WalletUtil.getWalletBalance(LocalConfig.WALLET_HOST, user.ssoToken())).isEqualTo(0);
    }
*/

    @Parameters("theme")
    @Test(description = "Verify successful postpaid, commision=10")
    public void     verify_successful_PostPaidPfc(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        double flatCommission = 10;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(order.getTXN_AMOUNT()));
            PostpaidHelpers.updateBalance("100");
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.checkboxPaytmCC().click();
            //Validate commision for postpaid
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0.0, flatCommission, "PAYTM_DIGITAL_CREDIT"))
            );
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("PAYTMCC")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }


    @Parameters("theme")
    @Test(description = "Verify successful UPI txn with slab based commision, conv fee=100, txn amount > 100")
    public void verify_successful_UPI_slabBasedPCF(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT("115")
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate commision for postpaid
            cashierPage.tabUPI().click();
            cashierPage.pause(5);
            pcfHelpher.validateCommision(cashierPage, order, 0.0, Double.parseDouble(PostCovEnhance.pcf_UPI_moreThan100));
            cashierPage.payBy(Constants.PayMode.UPI);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, Double.parseDouble(PostCovEnhance.pcf_UPI_moreThan100), "UPI"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.ICICI.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(order.getMID())
                    .validatePaymentMode("UPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_UPI_moreThan100));
        }
    }

    @Parameters("theme")
    @Test(description = "Verify successful UPI txn with slab based commision, conv fee=6, txn amount<10")
    public void verify_successful_UPI_slabBasedPCF1(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        double flatCommission = 6;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT("2")
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate commision for postpaid
            cashierPage.tabUPI().click();
            cashierPage.pause(5);
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.UPI);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.ICICI.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateMid(order.getMID())
                    .validatePaymentMode("UPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }

/*    @Owner("Deepak")
    @Parameters("theme")
    @Test(description = "Verify successful wallet txn from wallet only merchant, login on cashier page",enabled=false)
    //wallet is not supported
    public void verify_walletOnly_successfull_login(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.WALLETOnly_PCF.getId();
        String key = Constants.MerchantType.WALLETOnly_PCF.getKey();
        User user = userManager.getForWrite(Label.LOGIN);
        double flatCommission = 3;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT("2")
                    .setMerchantKey(key).build();
            double expectedTxnAmount = Double.parseDouble(order.getTXN_AMOUNT()) + convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0.0, flatCommission, "");
            WalletHelpers.modifyBalance(user, expectedTxnAmount);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate commision for wallet
            cashierPage.login(user);
            cashierPage.waitUntilLoads();
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.WALLET);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("WALLET")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
        Assertions.assertThat(WalletUtil.getWalletBalance(LocalConfig.WALLET_HOST, user.ssoToken())).isEqualTo(0);
    }
*/
  /*  @Parameters("theme")
    @Test(description = "Verify successful wallet txn from wallet only merchant, with token",enabled = false)
    //Wallet Paymode is not supported
    public void verify_walletOnly_successfull_token(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.WALLETOnly_PCF.getId();
        String key = Constants.MerchantType.WALLETOnly_PCF.getKey();
        User user = userManager.getForWrite(Label.BASIC);
        double flatCommission = 3;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT("2")
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            double expectedTxnAmount = Double.parseDouble(order.getTXN_AMOUNT()) + convenienceFeeCalculator(Double.parseDouble(order.getTXN_AMOUNT()), 0.0, flatCommission, "");
            WalletHelpers.modifyBalance(user, expectedTxnAmount);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate commision for wallet
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.WALLET);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("WALLET")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("WALLET")
                    .validateMid(order.getMID())
                    .validatePaymentMode("PPI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
        Assertions.assertThat(WalletUtil.getWalletBalance(LocalConfig.WALLET_HOST, user.ssoToken())).isEqualTo(0);
    }
*/
    @Parameters({"theme"})
    @Test(description = "Validate successful EMI transaction with PCF details.")
    public void SuccessfulOffusEMITxn(@Optional("enhancedweb") String theme) throws Exception {
        String hdfcDropdownBankName = "merchant4".equalsIgnoreCase(theme) ? "HDFC Bank" : "HDFC Bank Credit Card";
        String mid = Constants.MerchantType.AMEX_PCF.getId();
        String key = Constants.MerchantType.AMEX_PCF.getKey();
        double flatCommission = Double.parseDouble(PostCovEnhance.pcf_EMI_HDFC);
        test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setBankName(hdfcDropdownBankName);
            paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
            OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                    .setCUST_ID(CommonHelpers.generateOrderId())
                    .setTXN_AMOUNT("10")
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(orderDTO.getTXN_AMOUNT()), 0.0, flatCommission, "EMI"))
            );
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("EMI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, flatCommission);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful EMI on savecard transaction with PCF details.")
    public void Successful_EMI_savecard_Txn(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.AMEX_PCF.getId();
        String key = Constants.MerchantType.AMEX_PCF.getKey();
        double flatCommission = 7;
        User user = userManager.getForWrite(Label.BASIC);
        test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
            SavedCardHelpers.deleteSavedCard(user);
            SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                    paymentDTO.getCreditCardNumber());
            paymentDTO.setBankName("HDFC");
            OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                    .setCUST_ID(CommonHelpers.generateOrderId())
                    .setSSO_TOKEN(user.ssoToken())
                    .setTXN_AMOUNT("10")
                    .build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()));
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD, paymentDTO);
            new ResponsePage().waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFCSC.toString())
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("EMI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, flatCommission);
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify pcf cc txn from saved card", groups = "savecard")
    public void verify_PCFTxn_CC_SavedCard(@Optional("enhancedweb") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        double flatCommission = 1;
        test:
        {
            User user = userManager.getForWrite(Label.BASIC);
            PaymentDTO paymentDTO = new PaymentDTO();
            SavedCardHelpers.deleteSavedCard(user);
            SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                    paymentDTO.getCreditCardNumber());

            OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                    .setCUST_ID(CommonHelpers.generateOrderId())
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()));
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.SAVED_CARD);
            new ResponsePage().waitUntilLoads();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFCSC.toString())
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, flatCommission);
        }
    }

    @Issue("PGP-13954")
    @Parameters({"theme"})
    @Test(description = "Verify pcf fee on amex card")
    public void verify_PCF_onAmexCard(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.AMEX_PCF.getId();
        String key = Constants.MerchantType.AMEX_PCF.getKey();
        double flatCommission = Double.valueOf(pcf_Amex);
        test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER);
            OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                    .setCUST_ID(CommonHelpers.generateOrderId())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
            cashierPage.pause(1);
            pcfHelpher.validateCommisionAmount(cashierPage, orderDTO, 0.0, flatCommission);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify hdfc CC pcf fee is displayed, when cc card entered after removing amex")
    public void verify_PCF_CCFee_AfterAmexFee(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.AMEX_PCF.getId();
        String key = Constants.MerchantType.AMEX_PCF.getKey();
        double flatCommissionAmex = (Double.valueOf(pcf_Amex));
        double flatCommissionCC = (Double.valueOf(pcf_CC));
        test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                    .setCUST_ID(CommonHelpers.generateOrderId())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.tabCreditCard().click();
            cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.AMEX_CARD_NUMBER);
            cashierPage.pause(3);
            Reporter.report.info("Validating amex pcf value");
            pcfHelpher.validateCommisionAmount(cashierPage, orderDTO, 0.0, flatCommissionAmex);
            cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
            cashierPage.pause(3);
            Reporter.report.info("Validating hdfc pcf value");
            pcfHelpher.validateCommisionAmount(cashierPage, orderDTO, 0.0, flatCommissionCC);
        }
    }


    @Parameters({"theme"})
    @Test(description = "Verify pcf PPBL NB txn ")
    public void verify_PCFTxn_PPBL_NB(@Optional("enhancedwap") String theme) throws Exception {
        String mid = Constants.MerchantType.FLAT_PCF.getId();
        String key = Constants.MerchantType.FLAT_PCF.getKey();
        double flatCommission = 1.12;
        test:
        {
            User user = userManager.getForWrite(Label.PPBL);
            PaymentDTO paymentDTO = new PaymentDTO();
            SavedCardHelpers.deleteSavedCard(user);
            SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                    paymentDTO.getCreditCardNumber());
            OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                    .setCUST_ID(CommonHelpers.generateOrderId())
                    .setSSO_TOKEN(user.ssoToken())
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.PPBL);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(orderDTO.getTXN_AMOUNT()), 0.0, flatCommission, "PPBL"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName("PPBL")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("PPBL")
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("NB")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, flatCommission);
        }
    }

    //validation at DB is pending for refund check
    @Parameters({"theme"})
    @Test(description = "Verify full refund of pcf CC transaction refund type=R", groups = "smoke")
    public void success_PCF_CC_Refund(@Optional("enhancedwap") String theme) throws PGPException, InterruptedException {
        String mid = Constants.MerchantType.FLAT_PCF_Pg2_Refund.getId();
        String key = Constants.MerchantType.FLAT_PCF_Pg2_Refund.getKey();
        double flatCommission = 1;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                .setTXN_AMOUNT("5")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateCheckSum(key)
//                .validateResponsePageParameters()//TODO need to add CHARGEAMOUNT validation function then uncomment it
                .assertAll();


        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        validatePCFTxn(txnStatus, 0.0, flatCommission);
        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "R");
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }
    }

    @Parameters({"theme"})
    @Test(description = "Verify partial refund of pcf CC transaction when refund amount is less than txn amount and refund type=C", groups = "smoke")
    public void success_PCF_CC_Refund_Partial(@Optional("enhancedwap") String theme) throws PGPException {
        String mid = Constants.MerchantType.FLAT_PCF_Pg2_Refund.getId();
        String key = Constants.MerchantType.FLAT_PCF_Pg2_Refund.getKey();
        double flatCommission = 1;
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                .setTXN_AMOUNT("5").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
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
                .validateBankName(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.BANK_NAME))
                .validateCheckSum(key)
//                .validateResponsePageParameters()//TODO need to add CHARGEAMOUNT validation function then uncomment it
                .assertAll();

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), String.valueOf(Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1.0), txnStatus.getResponse().getTXNID(), "C");
            PGPHelpers.getRefundStatus(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), true)
                    .validateSuccessRefund()
                    .assertAll();
        }

    }

    @Parameters({"theme"})
    @Test(description = "Verify partial failed refund of pcf CC transaction when refund amount is greather than txn amount and refund type=C", groups = "smoke")
    public void Failed_PCF_CC_Refund_Partial(@Optional("enhancedwap") String theme) throws PGPException {
        String mid = Constants.MerchantType.FLAT_PCF_Pg2_Refund.getId();
        String key = Constants.MerchantType.FLAT_PCF_Pg2_Refund.getKey();
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                .setTXN_AMOUNT("5").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        Test:
        {
            Response response = PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), String.valueOf(Double.parseDouble(orderDTO.getTXN_AMOUNT()) + 1.0), txnStatus.getResponse().getTXNID(), "C");
            Assertions.assertThat(response.jsonPath().getString("RESPMSG")).isEqualTo("Refund amount is invalid or greater than transaction amount");
            Assertions.assertThat(response.jsonPath().getString("RESPCODE")).isEqualTo("674");
            Assertions.assertThat(response.jsonPath().getString("STATUS")).isEqualTo("TXN_FAILURE");
        }
    }

    @Parameters("theme")
    @Test(description = "Verify fee for all paymodes on cashier page for Default commission(different config)")
    public void Verify_FeeOnCashierPageForAllPayModes_forDifferentCommision(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String mid = Constants.MerchantType.NETBANK_PCF.getId();
        String key = Constants.MerchantType.NETBANK_PCF.getKey();
        Test:
        {
            User user = userManager.getForWrite(Label.POSTPAID,Label.PPBL);
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setSSO_TOKEN(user.ssoToken())
                    .setMerchantKey(key).build();
            WalletHelpers.modifyBalance(user, Double.parseDouble("10"));
            PostpaidHelpers.updateBalance("15");
            SavedCardHelpers.deleteSavedCard(user);
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

            //PPI
            cashierPage.checkBoxPPI().check();
            cashierPage.pause(1);
            pcfHelpher.validateCommision(cashierPage, order, 0.0, 3.0);


            //paytmcc
            cashierPage.checkboxPaytmCC().check();
            cashierPage.pause(2);
            pcfHelpher.validateCommision(cashierPage, order, 0.0, 10.0);

            //ppbl
            cashierPage.checkboxPPBL().check();
            cashierPage.pause(2);
            pcfHelpher.validateCommision(cashierPage, order, 0.0, 1.2);

            //debit
            cashierPage.tabDebitCard().click();
            cashierPage.pause(2);
            cashierPage.fillDCDetails(new PaymentDTO());
            pcfHelpher.validateCommision(cashierPage, order, 0.0, 4.0);
            cashierPage.closeCcDcDetailBtn();

            //credit
            cashierPage.tabCreditCard().click();
            cashierPage.pause(2);
            cashierPage.fillCCDetails(new PaymentDTO());
            pcfHelpher.validateCommision(cashierPage, order, 0.0, 1.0);
            cashierPage.closeCcDcDetailBtn();

            //upi
            cashierPage.tabUPI().click();
            cashierPage.pause(2);
            pcfHelpher.validateCommision(cashierPage, order, 0.0, 6.0);
            cashierPage.closeCcDcDetailBtn();

            //emi
            cashierPage.tabEMI().click();
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setBankName(Constants.Bank.HDFCBANK.toString())
                    .setCreditCardNumber(PaymentDTO.promoCC);
            cashierPage.fillEMIDetails(paymentDTO);
            cashierPage.pause(2);
            pcfHelpher.validateCommision(cashierPage, order, 0.0, 7.0);
            cashierPage.closeCcDcDetailBtn();

        }
    }

    @Parameters("theme")
    @Test(description = "Verify successful CC txn using PGOnly merchant with flat commision ")
    public void verify_successfulCCTxnWithFlatCommsion(@Optional("enhancedweb") String theme) throws Exception {
        String mid = Constants.MerchantType.PGOnly_Pcf.getId();
        String key = Constants.MerchantType.PGOnly_Pcf.getKey();
        double flatCommission = 1.12;
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            //Validate for credit card
            cashierPage.tabCreditCard().click();
            cashierPage.pause(1);
            //Calculate Charge Amount
            Double chargeAmount = convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC");
            pcfHelpher.validateCommision(cashierPage, order, 0.0, flatCommission);
            cashierPage.payBy(Constants.PayMode.CC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(String.valueOf(chargeAmount))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID(), PAYMENTMODE, true);
            txnStatus.executeUntilNotPending();
            txnStatus.validateChargeAmount(String.valueOf(chargeAmount))
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateStatusAPIParameters()
                    .AssertAll();
        }
    }

    @Parameters("theme")
    @Test(description = "test Charge Amount Property Not Displayed In Merchant Callback When Not Enabled On Merchant")
    public void testChargeAmountPropertyNotDisplayedInMerchantCallbackWhenNotEnabledOnMerchant(@Optional("enhancedweb") String theme) {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_MERCHANT1;
        String mid = merchant.getId();
        String key = merchant.getKey();
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.keys().contains(CHARGEAMOUNT).not()
            );
            sAssert.eval();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate response of failed PgOnly Txn")
    public void testChargeAmtPropertyPresentInMerchantCallbackWhenTxnFails(@Optional("enhancedweb") String theme) {
        String mid = Constants.MerchantType.PGOnly_Pcf.getId();
        String key = Constants.MerchantType.PGOnly_Pcf.getKey();
        double flatCommission = new Merchant(mid, key, false).getCommissions().stream().filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1).findAny().get().getFixedFee();
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber("4718650100030136"));
            cashierPage.waitUntilLoads();
            cashierPage.clickFailedTxnGotItButtonIfDisplayed();
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber("4718650100030136"));
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_FAILURE"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC"))
            );
            sAssert.eval();
        }
    }

    @Parameters("theme")
    @Test(description = "Validate response of pending PgOnly Txn")
    public void testChargeAmtPropertyPresentInMerchantCallbackWhenTxnGoesInPendingState(@Optional("enhancedweb") String theme) {
        String mid = Constants.MerchantType.PGOnly_Pcf.getId();
        String key = Constants.MerchantType.PGOnly_Pcf.getKey();
        double flatCommission = new Merchant(mid, key, false).getCommissions().stream().filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1).findAny().get().getFixedFee();
        double totalTxnAmtForPendingState = 77D;
        double serviceTax = flatCommission * 0.18;
        String txnAmtForPendingState = new DecimalFormat("0.00").format(totalTxnAmtForPendingState - flatCommission - serviceTax);
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setTXN_AMOUNT(txnAmtForPendingState)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("PENDING"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC"))
            );
            sAssert.eval();
        }
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful EMI transaction with PCF details with promo code")
    public void succesfulPromoCodeEMIOnPCF(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String hdfcDropdownBankName = "merchant4".equalsIgnoreCase(theme) ? "HDFC Bank" : "HDFC Bank Credit Card";
        String mid = Constants.MerchantType.AMEX_PCF.getId();
        String key = Constants.MerchantType.AMEX_PCF.getKey();
        test:
        {
            String promoId = Constants.promoCode.EMI_PROMO_CODE.toString();
            PaymentDTO paymentDTO = new PaymentDTO().setPromoCode(promoId).setPromoDesc("this is emi promocode").setCreditCardNumber(PaymentDTO.promoCC);
            paymentDTO.setBankName(hdfcDropdownBankName);
            OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                    .setCUST_ID(CommonHelpers.generateOrderId())
                    .setTXN_AMOUNT("10")
                    .setPROMO_CAMP_ID(promoId)
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);

            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            Assertions.assertThat(responsePage.textPromoCampId().getText()).isEqualTo(Constants.promoCode.EMI_PROMO_CODE.toString());
            Assertions.assertThat(responsePage.textPromoRespcode().getText()).isEqualTo("700");
            Assertions.assertThat(responsePage.textPromoStatus().getText()).isEqualTo("PROMO_SUCCESS");

            TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode("EMI")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
            validatePCFTxn(txnStatus, 0.0, Double.parseDouble(PostCovEnhance.pcf_EMI_HDFC));
        }
    }

    @Parameters("theme")
    @Test(description = "Validate post convenience fee and perform end to end PgOnly Txn in case of txn retry")
    public void testCCRetry(@Optional("enhancedweb") String theme) throws Exception {
        String mid = Constants.MerchantType.PGOnly_Pcf.getId();
        String key = Constants.MerchantType.PGOnly_Pcf.getKey();
        double flatCommission = new Merchant(mid, key, false).getCommissions().stream().filter(commission -> commission.getMinAmt() <= 1 && commission.getMaxAmt() >= 1).findAny().get().getFixedFee();
        Test:
        {
            OrderDTO order = new OrderFactory.PGOnly(mid, theme)
                    .setMerchantKey(key).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC, new PaymentDTO().setCreditCardNumber("4718650100030136"));
            cashierPage.waitUntilLoads();
            cashierPage.clickFailedTxnGotItButtonIfDisplayed();
            cashierPage.payBy(Constants.PayMode.CC);
            com.paytm.pages.responsePage.ResponsePage responsePage= new com.paytm.pages.responsePage.ResponsePage();
            assertion.apply(pageWait.apply(responsePage.hasLoaded()));
            SoftAssertion sAssert = new SoftAssertion();
            sAssert.apply(
                    responsePage.get(STATUS).equals("TXN_SUCCESS"),
                    responsePage.get(CHARGEAMOUNT).equals(convenienceFeeCalculator(Double.valueOf(order.getTXN_AMOUNT()), 0, flatCommission, "CC"))
            );
            sAssert.eval();
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(Constants.Gateway.HDFC.toString())
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(Constants.Bank.HDFC.toString())
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        }
    }




    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19081")
    @Parameters("theme")
    @Test(description = "Verify Add money with wallet only merchant with PCF")
    public void validateAddmoneyWalletMerchantPCF(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType  = Constants.MerchantType.ADD_MONEY_PCF;
        User user = userManager.getForWrite(Label.LOGIN);
        double WalletBalance = WalletHelpers.getWalletBalance(user);
            OrderDTO order = new OrderFactory.AddMoney(merchantType, theme,user).build();
            checkoutPage.createOrder(order);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.CC);
            TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
            txnStatus.executeUntilNotPending();
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(order.getORDER_ID())
                    .validateTxnAmount(order.getTXN_AMOUNT())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("ADDMONEY")
                    .validateGatewayName("HDFC")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName("HDFC Bank")
                    .validateMid(order.getMID())
                    .validatePaymentMode("CC")
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .AssertAll();
        WalletHelpers.validateBalance(user, WalletBalance+Double.valueOf(order.getTXN_AMOUNT()));
    }


    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19081")
    @Parameters("theme")
    @Test(description = "Verify Add money with wallet only PCF merchant with DC paymode of SCW")
    public void verifyAddNMoneywithDC(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType  = Constants.MerchantType.ADD_MONEY_PCF;
        User user = userManager.getForWrite(Label.LOGIN);
        double WalletBalance = WalletHelpers.getWalletBalance(user);
        OrderDTO order = new OrderFactory.AddMoney(merchantType, theme,user).build();
        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(order.getORDER_ID())
                .validateTxnAmount(order.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(order.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, WalletBalance+Double.valueOf(order.getTXN_AMOUNT()));
    }

    @Epic(Constants.Sprint.SPRINT30_1)
    @Feature("PGP-19081")
    @Parameters("theme")
    @Test(description = "Verify Add money with DC present on PCF merchant")
    public void verifyAddNMoneywithDCwihDCpresentOnMerchant(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType  = Constants.MerchantType.NETBANK_PCF;
        User user = userManager.getForWrite(Label.LOGIN);
        double WalletBalance = WalletHelpers.getWalletBalance(user);
        OrderDTO order = new OrderFactory.AddMoney(merchantType, theme,user).build();
        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(order.getORDER_ID())
                .validateTxnAmount(order.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(order.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, WalletBalance+Double.valueOf(order.getTXN_AMOUNT()));
    }


    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"theme"})
    @Test(description = "Verify success corporate CC txn with PCF Corporate merchant")
    public void successCorporateCCPCF(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType pcfMerchant  = Constants.MerchantType.FLAT_PCF;
        CorporateHelpers.assertCorporateCardCC(pcfMerchant.getId());

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);

        User user = userManager.getForWrite(Label.LOGIN);

        OrderDTO order = new OrderFactory.PGOnly(pcfMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(order,"",user,"CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");

        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);


        PcfHelpher.validateSuccessResponsePCFTxn(order,pcfMerchant,"CC",chargeAmount);

        CorporateHelpers.validateSuccessTxnStatusCorporate(order,"CC", Constants.Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonCorporate(order,"CC", Constants.Bank.HDFC.toString(), Constants.Gateway.HDFC.toString());

    }

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"theme"})
    @Test(description = "Verify success corporate DC txn with PCF Corporate merchant")
    public void successCorporateDCPCF(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType pcfMerchant  = Constants.MerchantType.FLAT_PCF;
        CorporateHelpers.assertCorporateCardDC(pcfMerchant.getId());

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);

        User user = userManager.getForWrite(Label.LOGIN);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pcfMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(orderDTO,"",user,"DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);


        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMerchant,"DC",chargeAmount);

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO,"DC", Constants.Bank.AXIS.toString());

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME","PAYMENTMODE", "CUSTID", "MID","feeRateFactors", "CHARGEAMOUNT", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals(Constants.Bank.AXIS.toString()),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(Constants.Gateway.HDFC.toString()),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.respMsg().equals("Txn Success"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.feeRateFactors().equals("{\"corporateCard\":\"TRUE\"}"),
                peon.isChecksumValid());

        sAssert.eval();

    }

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"theme"})
    @Test(description = "Verify success corporate CC txn with PCF Non Corporate merchant")
    public void successCorporateCCPCFNonCorporate(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType pcfMerchant  = Constants.MerchantType.PGOnly_Pcf;

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);

        User user = userManager.getForWrite(Label.LOGIN);

        OrderDTO order = new OrderFactory.PGOnly(pcfMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(order,"",user,"CREDIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");

        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();

        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        Assertions.assertThat(cashierPage.getErrorMessageAfterEnteringCard()).isEqualTo("VISA Corporate card is not allowed for this payment. Please try paying using other cards/options.");


    }

    @Owner("Tarun")
    @Feature("PGP-24136")
    @Parameters({"theme"})
    @Test(description = "Verify success corporate DC txn with PCF Non Corporate merchant")
    public void successCorporateDCPCFNonCorporate(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType pcfMerchant  = Constants.MerchantType.PGOnly_Pcf;

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);

        User user = userManager.getForWrite(Label.LOGIN);

        OrderDTO order = new OrderFactory.PGOnly(pcfMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithSSO(order,"",user,"DEBIT_CARD");
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");

        checkoutPage.createOrder(order);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().waitUntilClickable();
        cashierPage.tabDebitCard().click();

        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());
        Assertions.assertThat(cashierPage.getErrorMessageAfterEnteringCard()).isEqualTo("MASTER Corporate card is not allowed for this payment. Please try paying using other cards/options.");


    }





}


