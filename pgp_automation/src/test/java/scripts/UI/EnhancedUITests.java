package scripts.UI;

import com.paytm.LocalConfig;
import com.paytm.api.MappingService.GetMerchantPreferenceInfo;
import com.paytm.api.ProcessTransactionV1;
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
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.reporting.listenerDecorators.DefaultExtentListener;
import com.paytm.framework.reporting.listenerDecorators.DefaultTestNGListener;
import com.paytm.pages.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;
import org.testng.asserts.SoftAssert;
import com.paytm.framework.core.DriverManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.SRISHTI;

/**
 * Created by ankuragarwal on 28/1/19
 */
@Owner("Gagandeep")
@Listeners({DefaultTestNGListener.class, DefaultExtentListener.class})
public class EnhancedUITests extends PGPBaseTest {

    private static final String INVALID_CVV = "12";
    private static final String INVALID_EXPIRY_DATE_MSG = "Invalid Expiry Date";
    private static final String INVALID_CVV_MSG = "CVV is Invalid";
    private static final String INVALID_MONTH = "01";
    private static final String INVALID_YEAR = "2017";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final DynamicCurrencyConversionPage dccPage = new DynamicCurrencyConversionPage();
    private final DynamicCurrencyConversionBankPage dccBankPage = new DynamicCurrencyConversionBankPage();

    private List<Object> updatePaymentMode(String payModePriorityList)
    {
        payModePriorityList = payModePriorityList
                .replace("NB","Net Banking")
                .replace("UPI","BHIM UPI")
                .replace("DC","Debit Card")
                .replace("CC","Credit Card")
                .replace("SC","Saved Card")
                .replace("SV","SAVED_VPA")
                .replace("PPBL","Paytm Payments Bank")
                .replace("GV","GIFT_VOUCHER")
                .replace("ADA","ADVANCE_DEPOSIT_ACCOUNT")
                .replace("Postpaid","Paytm Postpaid")
                .replace("PPI","Paytm Balance")
        ;
        return Arrays.asList(payModePriorityList.split(","));
    }

    //        @Parameters({"theme"})
//    @Test
    public void verifyVisaDC_NonLoggedInUsr(@Optional("enhancedWap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            cashierPage.tabDebitCard().click();

        }
    }

    private CashierPage getCashierPage_HybridTxn(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Hybrid, theme)
                .setTXN_AMOUNT("2.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        return cashierPage;
    }


    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry msg should be displayed for DC")
    public void validateInvalidExpiryForDC_NonLoggedInUsr(@Optional("enhancedWap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setExpMonth(INVALID_MONTH);
            paymentDTO.setExpYear(INVALID_YEAR);
            cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
            cashierPage.assertContainsText(INVALID_EXPIRY_DATE_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry msg should be displayed for DC for Hybrid merchant when balance is insufficient")
    public void validateInvalidExpiryForDC_LoggedInUsr_Hybrid(@Optional("enhancedWap") String theme) throws Exception {
        CashierPage cashierPage = getCashierPage_HybridTxn(theme);
        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setExpMonth(INVALID_MONTH);
            paymentDTO.setExpYear(INVALID_YEAR);
            cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
            cashierPage.assertContainsText(INVALID_EXPIRY_DATE_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry msg should be displayed for DC when user is logged in")
    public void validateInvalidExpiryForDC_LoggedInUsr(@Optional("enhancedWap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setExpMonth(INVALID_MONTH);
            paymentDTO.setExpYear(INVALID_YEAR);
            cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
            cashierPage.assertContainsText(INVALID_EXPIRY_DATE_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV msg should be displayed for DC")
    public void validateInvalidCVVForDC_NonLoggedInUsr(@Optional("enhancedWap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCvvNumber(INVALID_CVV);
            cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
            cashierPage.assertContainsText(INVALID_CVV_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV msg should be displayed for DC when user is logged in for hybrid merchant")
    public void validateInvalidCVVForDC_LoggedInUsr_Hybrid(@Optional("enhancedWap") String theme) throws Exception {
        CashierPage cashierPage = getCashierPage_HybridTxn(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCvvNumber(INVALID_CVV);
            cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
            cashierPage.assertContainsText(INVALID_CVV_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV msg should be displayed for DC when user is logged in")
    public void validateInvalidCVVForDC_LoggedInUsr(@Optional("enhancedWap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCvvNumber(INVALID_CVV);
            cashierPage.payBy(Constants.PayMode.DC, paymentDTO);
            cashierPage.assertContainsText(INVALID_CVV_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry msg should be displayed for CC when user is logged in for hybrid merc")
    public void validateInvalidExpiryForCC_LoggedInUsr_Hybrid(@Optional("enhancedWap") String theme) throws Exception {
        CashierPage cashierPage = getCashierPage_HybridTxn(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setExpMonth(INVALID_MONTH);
            paymentDTO.setExpYear(INVALID_YEAR);
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
            cashierPage.assertContainsText(INVALID_EXPIRY_DATE_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry msg should be displayed for CC when user is logged in")
    public void validateInvalidExpiryForCC_LoggedInUsr(@Optional("enhancedWap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setExpMonth(INVALID_MONTH);
            paymentDTO.setExpYear(INVALID_YEAR);
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
            cashierPage.assertContainsText(INVALID_EXPIRY_DATE_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid Expiry msg should be displayed for DC when user is Not logged in")
    public void validateInvalidExpiryForCC_NotLoggedInUsr(@Optional("enhancedWap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setExpMonth(INVALID_MONTH);
            paymentDTO.setExpYear(INVALID_YEAR);
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
            cashierPage.assertContainsText(INVALID_EXPIRY_DATE_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV msg should be displayed for CC when user is logged in for hybrid merchant")
    public void validateInvalidCVVForCC_LoggedInUsr_Hybrid(@Optional("enhancedWap") String theme) throws Exception {
        CashierPage cashierPage = getCashierPage_HybridTxn(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCvvNumber(INVALID_CVV);
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
            cashierPage.assertContainsText(INVALID_CVV_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV msg should be displayed for CC when user is logged in")
    public void validateInvalidCVVForCC_LoggedInUsr(@Optional("enhancedWap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCvvNumber(INVALID_CVV);
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
            cashierPage.assertContainsText(INVALID_CVV_MSG);
        }
    }

    @Parameters({"theme"})
    @Test(description = "Validate Invalid CVV msg should be displayed for CC when user is not logged in")
    public void validateInvalidCVVForCC_NotLoggedInUsr(@Optional("enhancedWap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Test:
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setCvvNumber(INVALID_CVV);
            cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
            cashierPage.assertContainsText(INVALID_CVV_MSG);
        }
    }

    @Owner(Constants.Owner.TARUN)
    @Epic("PGP-24767")
    @Parameters({"theme"})
    @Test(description = "Enhanced : Non Logged In Flow :  Verify the payment modes sequencing is as per the list returned from the mapping service API when the preference is active.",groups = "P0")
    public void enhancedOrderingNonLoggedIn(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType priorityMerchant = Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT;
        Response preferences = new GetMerchantPreferenceInfo(priorityMerchant.getId()).execute();

        String merchantPriority = preferences.path("merchantPreferenceInfos.find {it.prefType=='PAYMODE_PRIORITY_LIST'}.prefValue");

        OrderDTO orderDTO = new OrderFactory.PGOnly(priorityMerchant, theme)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), orderDTO.getCUST_ID(), priorityMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Assertions.assertThat(cashierPage.getPushAppData().getList("merchantPayModes.name")).isEqualTo(updatePaymentMode(merchantPriority.concat(",Paytm Balance")));

    }

    @Owner(Constants.Owner.TARUN)
    @Epic("PGP-24767")
    @Parameters({"theme"})
    @Test(description = "Enhanced : SSO Logged In Flow :  Verify the payment modes sequencing is as per the list returned from the mapping service API when the preference is active.",groups = "P0")
    public void enhancedOrderingLoggedIn(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType priorityMerchant = Constants.MerchantType.PRIORITY_DEFAULT_MERCHANT;
        Response preferences = new GetMerchantPreferenceInfo(priorityMerchant.getId()).execute();

        String merchantPriority = preferences.path("merchantPreferenceInfos.find {it.prefType=='PAYMODE_PRIORITY_LIST'}.prefValue");

        User user = userManager.getForWrite(Label.PPBL, Label.POSTPAID);
        merchantPriority = merchantPriority.concat(",Paytm Balance,Postpaid,PPBL"); //As user have postpaid and PPBL
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());

        OrderDTO orderDTO = new OrderFactory.PGOnly(priorityMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Assertions.assertThat(cashierPage.getPushAppData().getList("merchantPayModes.name")).isEqualTo(updatePaymentMode(merchantPriority));
    }

    @Owner(Constants.Owner.TARUN)
    @Epic("PGP-24767")
    @Parameters({"theme"})
    @Test(description = "Login On Cashier Page:  Verify the payment modes sequencing is as per the list returned from the mapping service API when the preference is active.",groups = "P0")
    public void enhancedOrderingLogInCashierPage(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType priorityMerchant = Constants.MerchantType.PRIORITY_HYBRID_MERCHANT;
        Response preferences = new GetMerchantPreferenceInfo(priorityMerchant.getId()).execute();

        String merchantPriority = preferences.path("merchantPreferenceInfos.find {it.prefType=='PAYMODE_PRIORITY_LIST'}.prefValue");

        User user = userManager.getForWrite(Label.PPBL, Label.POSTPAID);
        merchantPriority = merchantPriority.replace(",SC","");
        merchantPriority = merchantPriority.concat(",PPI");

        OrderDTO orderDTO = new OrderFactory.PGOnly(priorityMerchant, theme)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);

        Assertions.assertThat(cashierPage.getPushAppData().getList("merchantPayModes.name"))
                .isEqualTo(updatePaymentMode(merchantPriority));
        Assertions.assertThat(cashierPage.listOfPayModes().get(cashierPage.listOfPayModes().size()-1).getText()).isEqualTo("Prepaid, Debit & Credit Cards");//PPBL should be visible at last


    }


    @Owner(Constants.Owner.TARUN)
    @Epic("PGP-24767")
    @Parameters({"theme"})
    @Test(description = "Hybrid:  Verify the payment modes sequencing is as per the list returned from the mapping service API when the preference is active.",groups = "P0")
    public void enhancedOrderingHybrid(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType priorityHybridMerchant = Constants.MerchantType.PRIORITY_HYBRID_MERCHANT;
        Response preferences = new GetMerchantPreferenceInfo(priorityHybridMerchant.getId()).execute();

        String merchantPriority = preferences.path("merchantPreferenceInfos.find {it.prefType=='PAYMODE_PRIORITY_LIST'}.prefValue");

        User user = userManager.getForWrite(Label.PPBL, Label.POSTPAID);
        merchantPriority = merchantPriority.concat(",Paytm Balance,Postpaid,PPBL"); //As user have postpaid and PPBL

        OrderDTO orderDTO = new OrderFactory.Hybrid(priorityHybridMerchant, theme,user)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), orderDTO.getCUST_ID(), priorityHybridMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Assertions.assertThat(cashierPage.getPushAppData().getList("merchantPayModes.name")).isEqualTo(updatePaymentMode(merchantPriority));

    }


    @Owner(Constants.Owner.TARUN)
    @Epic("PGP-24767")
    @Parameters({"theme"})
    @Test(description = "Hybrid:  Enhanced : Verify for default flow with SSO ",groups = "P0")
    public void enhancedOrderingHybridSSO(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType priorityHybridMerchant = Constants.MerchantType.PRIORITY_HYBRID_MERCHANT;
        Response preferences = new GetMerchantPreferenceInfo(priorityHybridMerchant.getId()).execute();

        String merchantPriority = preferences.path("merchantPreferenceInfos.find {it.prefType=='PAYMODE_PRIORITY_LIST'}.prefValue");
        merchantPriority = merchantPriority.concat(",Paytm Balance,Postpaid,PPBL"); //As user have postpaid and PPBL'

        User user = userManager.getForWrite(Label.PPBL, Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(priorityHybridMerchant, theme,user)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), orderDTO.getCUST_ID(), priorityHybridMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Assertions.assertThat(cashierPage.getPushAppData().getList("merchantPayModes.name"))
                .isEqualTo(updatePaymentMode(merchantPriority));

    }


    @Owner(Constants.Owner.TARUN)
    @Epic("PGP-24767")
    @Parameters({"theme"})
    @Test(description = "ADDNPay:  Enhanced : Verify for add and pay flow with SSO ",groups = "P0")
    public void enhancedOrderingAddMoneySSO(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType priorityAddpayMerchant = Constants.MerchantType.PRIORITY_ADDPAY_MERCHANT;
        Response preferences = new GetMerchantPreferenceInfo(priorityAddpayMerchant.getId()).execute();

        String merchantPriority = preferences.path("merchantPreferenceInfos.find {it.prefType=='PAYMODE_PRIORITY_LIST'}.prefValue");

        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddnPay(priorityAddpayMerchant, theme,user)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), orderDTO.getCUST_ID(), priorityAddpayMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        Assertions.assertThat(cashierPage.getPushAppData().getList("merchantPayModes.name")).containsSequence(updatePaymentMode(merchantPriority));

    }

    @DataProvider
    public Object[][] getFreqValue()
    {
        Object[][] freqvalue= new Object[8][3];
        freqvalue[0][0]="1";
        freqvalue[0][1]="MONTH";
        freqvalue[0][2]="Every month";
        freqvalue[1][0]="1";
        freqvalue[1][1]="YEAR";
        freqvalue[1][2]="Every year";
        freqvalue[2][0]="2";
        freqvalue[2][1]="DAY";
        freqvalue[2][2]="Every 2 day";
        freqvalue[3][0]="3";
        freqvalue[3][1]="WEEK";
        freqvalue[3][2]="Every 3 week";
        freqvalue[4][0]="1";
        freqvalue[4][1]="QUARTER";
        freqvalue[4][2]="Every quarter";
        freqvalue[5][0]="1";
        freqvalue[5][1]="BI_MONTHLY";
        freqvalue[5][2]="Every 2 months";
        freqvalue[6][0]="1";
        freqvalue[6][1]="SEMI_ANNUALLY";
        freqvalue[6][2]="Every half-year";
        freqvalue[7][0]="1";
        freqvalue[7][1]="FORTNIGHT";
        freqvalue[7][2]="Every fortnight";


        return freqvalue;
    }

  //  @Parameters("theme")
    @Owner(SRISHTI)
    @Feature("PGP-26593")
    @Test(description = "Validate frequency in PUSH_APP_DATA ", dataProvider ="getFreqValue")
    public void validateDAYFreqInPushAppData( String frequency, String freqUnit, String result) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(Constants.MerchantType.Subscription_PGOnly, "enhancedweb")
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSUBS_FREQUENCY(frequency)
                .setSUBS_FREQUENCY_UNIT(freqUnit)
                .setSUBS_GRACE_DAYS("1")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedweb");
        Assertions.assertThat(cashierPage.getAppData().toString()).contains(result);

    }

    @DataProvider (name="MatrixofFreqUnits" )
    public Object[][] getDataFromDataprovider(){

        String frequnit[]={"DAY", "WEEK", "MONTH", "YEAR", "ONDEMAND", "QUARTER", "BI_MONTHLY", "SEMI_ANNUALLY", "FORTNIGHT"};
        String amtType[]= {"VARIABLE", "FIX"};

        List<List<String>> list = new ArrayList<>();
        for(String s1:  frequnit)
        {
            for(String s2: amtType)
                list.add(Arrays.asList(s1,s2));
        }

        String[][] arr = new String[list.size()][2];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < 2; j++) {
                arr[i][j] = list.get(i).get(j);
            }
        }
        return arr;
    }


    @Owner(Constants.Owner.ESHANI)
    @Test(description = "Verify the successful UPI Intent subscription txn with all possible freq units" , dataProvider = "MatrixofFreqUnits")
    public void PGP_26700_verifySuccessUPIIntentSubsTxnOnEnhancedPage( String freqUnit, String amtType) throws Exception {
        String  theme="enhancedwap_revamp";

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_NATIVE_SUBS;
        String graceDays="1";
        if(freqUnit.equals("DAY"))
            graceDays="0";

        OrderDTO orderDTO;
        // If amount type is FIX then txn amount and subs max amount should be equal, so based on the amount type
        // value, will be creating the orderDTO
        if(!amtType.equals("FIX")){
            orderDTO = new OrderFactory.SubscriptionCC_DC(merchant, theme )
                    .setSUBS_PAYMENT_MODE("")
                    .setSUBS_PPI_ONLY("")
                    .setSUBS_FREQUENCY_UNIT(freqUnit)
                    .setSUBS_AMOUNT_TYPE(amtType)
                    .setSUBS_GRACE_DAYS(graceDays)
                    .build();
        }
        else{
            orderDTO = new OrderFactory.SubscriptionCC_DC(merchant, theme )
                    .setSUBS_PAYMENT_MODE("")
                    .setSUBS_PPI_ONLY("")
                    .setSUBS_FREQUENCY_UNIT(freqUnit)
                    .setSUBS_AMOUNT_TYPE(amtType)
                    .setSUBS_GRACE_DAYS(graceDays)
                    .setTXN_AMOUNT("100")
                    .setSUBS_MAX_AMOUNT("100")
                    .build();
        }

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.tabUPIIntent().assertVisible();

        Assertions.assertThat(cashierPage.getPushAppData().getString("txnToken"))
                .as("txnToken is null in pushAppData")
                .isNotNull();
        Assertions.assertThat(cashierPage.getPushAppData().getString("txnToken"))
                .as("txnToken is empty in pushAppData")
                .isNotEmpty();
        Assertions.assertThat(cashierPage.getPushAppData().getString("subscriptionDetail.subsId"))
                .as("subscriptionDetail.subsId is null in pushAppData")
                .isNotNull();
        Assertions.assertThat(cashierPage.getPushAppData().getString("subscriptionDetail.subsId"))
                .as("subscriptionDetail.subsId is empty in pushAppData")
                .isNotEmpty();

        String txnToken= cashierPage.getPushAppData().getString("txnToken");
        String subsId= cashierPage.getPushAppData().getString("subscriptionDetail.subsId");


        ProcessTxnV1Request processTxnV1Request= new ProcessTxnV1Request.Builder(orderDTO.getMID(), txnToken,orderDTO.getORDER_ID())
                .setSubsId(subsId)
                .setPaymentMode("UPI_INTENT")
                .setRiskExtendInfo("userAgent:Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4239.0 Mobile Safari/537.36|timeZone:Asia/Calcutta|operationType:PAYMENT|businessFlow:STANDARD|amount:1.00|merchantType:offus|language:en-GB|screenResolution:360X640|osType:Android|osVersion:6.0|deviceType:mobile|channelId:WAP|platform:mWeb|deviceManufacturer:LG|deviceModel:Nexus 5|browserType:Chrome|browserVersion:86.0.4239.0|")
                .setAuthMode("3D")
                .setChannelId("WEB")
                .setPaymentFlow("NONE")
                .setTokenType("TXN_TOKEN")
                .build();

        ProcessTransactionV1 processTransactionV1= new ProcessTransactionV1(processTxnV1Request);
        processTransactionV1.setContext("head.token", txnToken)
                .setContext("head.workFlow" ,"enhancedCashierFlow")
                .setContext("body.selectedPaymentModeId", "2")
                .setContext("showLoader", false)
                .setContext("showPostFetchLoader", false)
                .deleteContext("body.extendInfo")
                .deleteContext("body.payerAccount")
                .deleteContext("body.website")
                .deleteContext("body.custId");


        Response response = processTransactionV1.execute();
        response.then()
                .statusCode(200)
                .body("body", Matchers.notNullValue())
                .body("body.resultInfo", Matchers.notNullValue())
                .body("body.resultInfo.resultStatus", Matchers.equalTo("S"),
                        "body.resultInfo.resultCode", Matchers.equalTo("0000"),
                        "body.resultInfo.resultMsg", Matchers.equalTo("Success"))
                .body("body.content", Matchers.notNullValue())
                .body("body.content.deepLink", Matchers.notNullValue(),
                        "body.content.deepLink", Matchers.not(Matchers.empty()));
        String deeplink = response.jsonPath().getString("body.content.deepLink");
        // If recur is DAILY or ASPRESENTED then recurvalue and recurtype will not be returned in the deeplink
        if( !deeplink.contains("DAILY") && !deeplink.contains("ASPRESENTED") ) {
            response.then()
                    .body("body.content.deepLink", Matchers.containsString("recurvalue"),
                            "body.content.deepLink", Matchers.containsString("recurtype"));
        }

        String esn= deeplink.replaceAll(".*&tid=PYTM(\\w+).*|.*", "$1");

        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount(orderDTO.getTXN_AMOUNT())
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(orderDTO.getORDER_ID())
                .setExternalSerialNo(esn)
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(orderDTO.getMID());

        Response response1 = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        response1.then()
                .statusCode(200)
                .body("body", Matchers.notNullValue())
                .body("body.resultCodeId", Matchers.equalTo("001"),
                        "body.resultCode", Matchers.equalTo("SUCCESS"),
                        "body.resultMsg", Matchers.equalTo("success"));

//        ResponsePage responsePage = new ResponsePage();
//        responsePage.waitUntilLoads();
//        responsePage.validateTxnAmount("1.00")
//                .validateStatus("TXN_SUCCESS")
//                .assertAll();

//        Cashier page is not moving to callback since ptc done via api. verifying subsid in txnstatus for checking e2e txn

        PGPHelpers.getTxnStatus(merchant.getId(), orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBL")
                .validateSubsid(subsId)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.SOURAV)
    @Test(description = "Validate txnTokenTTL is present in PushAppData without ssotoken")
    public void txnTokenTTL_present_PushAppData(@Optional("enhancedweb_revamp") String theme) throws Exception {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG_JS_RETRY, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String txnTokenTTL = cashierPage.getPushAppData().getString("txnTokenTTL");
        Assertions.assertThat(txnTokenTTL)
                .as("txnTokenTTL is null in pushAppData").isNotNull()
                .isBetween(String.valueOf(850), String.valueOf(900));

        DriverManager.getDriver().navigate().refresh();

        Assertions.assertThat(cashierPage.getPushAppData().getString("txnTokenTTL")).isNotNull()
                .as("txnTokenTTL is null in pushAppData")
                .isBetween(String.valueOf(800), txnTokenTTL);

        cashierPage.backBtn().isVisible();
        cashierPage.backBtn().click();
        cashierPage.cancelPaymentYes().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("141")
                .validateRespMsg("User has not completed transaction.")
                .validateResponseStatus("TXN_FAILURE")
                .validateTxnId(orderDTO.getTXN_AMOUNT())
                .validateResponsePageParameters();

    }


    @Parameters({"theme"})
    @Owner(Constants.Owner.SOURAV)
    @Test(description = "Validate txnTokenTTL is present in PushAppData with ssotoken")
    public void txnTokenTTL_present_PushAppData_with_ssotoken(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PG_JS_RETRY, theme)
                .setTXN_AMOUNT("1.00")
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();

        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        String txnTokenTTL = cashierPage.getPushAppData().getString("txnTokenTTL");
        Assertions.assertThat(txnTokenTTL)
                .as("txnTokenTTL is null in pushAppData").isNotNull()
                .isBetween(String.valueOf(850), String.valueOf(900));

        DriverManager.getDriver().navigate().refresh();

        Assertions.assertThat(cashierPage.getPushAppData().getString("txnTokenTTL")).isNotNull()
                .as("txnTokenTTL is null in pushAppData")
                .isLessThan(txnTokenTTL).isBetween(String.valueOf(800), txnTokenTTL);

        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();

        cashierPage.clickFailedTxnGotItButtonIfDisplayed();

        Assertions.assertThat(cashierPage.getPushAppData().getString("txnTokenTTL")).isNotNull()
                .as("txnTokenTTL is null in pushAppData")
                .isBetween(String.valueOf(850), String.valueOf(900));

        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.promoCC));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
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
                .validateStatusAPIParameters()
                .AssertAll();

    }
    @Owner(Constants.Owner.AKSHAT)
    @Epic("PGP-32288")
    @Parameters({"theme"})
    @Test(description =  "Verify that cardType and bankName is displayed in pushAppData response for saved cards")
    public void cardType_bankName_inEnhancedSavedCards(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        Double txnAmount = 1.0;
        Constants.MerchantType merchant = Constants.MerchantType.Hybrid;
        User user = userManager.getForWrite(Label.LOGIN);
        //SavedCardHelpers.deleteSavedCard(user);
        //SavedCardHelpers.addCard(user, "12", "2025", "4718650100010336");

        OrderDTO orderDTO = new OrderFactory.Hybrid(merchant, theme, user )
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantPayModes.savedCards.cardType"))
                .as("params missing")
                .isNotEmpty();
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantPayModes.savedCards.bankName"))
                .as("params missing")
                .isNotEmpty();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that a user is able to perform a successful txn through international Credit Card with USD when DCC is enabled for merchant")
    public void SC_Txn_whenDCCisEnabledUsdCurrency(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(Constants.MerchantType.DCC_PCF, theme, user)
                .setTXN_AMOUNT("500.00").build();

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        paymentDTO.setCreditCardNumber(paymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        dccPage.selectCurrencyAndValidateConvenienceFee("USD");
        dccBankPage.clickSuccessButton();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateChargeAmount("118.00");
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that a user is able to perform a successful txn through international Credit Card with INR when DCC is enabled for merchant")
    public void SC_Txn_whenDCCisEnabledInrCurrency(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.COD(Constants.MerchantType.DCC_PCF, theme, user)
                .setTXN_AMOUNT("500.00").build();

        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        paymentDTO.setCreditCardNumber(paymentDTO.INTERNATIONAL_ICICI_CREDIT_CARD);

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        dccPage.selectCurrencyAndValidateConvenienceFee("INR");
        dccBankPage.clickSuccessButton();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateChargeAmount("118.00");
        responsePage.validateStatus("TXN_SUCCESS");
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();
    }

    @Owner(Constants.Owner.HIMANSHU)
    @Epic("PGP-39624")
    @Parameters({"theme"})
    @Test(description = "Verify UPI payment mode is visible when specific paymode wallet is being passed")
    public void UPIOptionOnCashierPage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType priorityMerchant = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.BASIC);
        double txnAmmount = 2;
       // WalletHelpers.modifyBalance(user,txnAmmount-1);
        OrderDTO orderDTO = new OrderFactory.PGOnly(priorityMerchant, theme)
                .setTXN_AMOUNT(toString().valueOf(txnAmmount))
                .setSSO_TOKEN(user.ssoToken())
                .setPAYMENT_MODE_ONLY("YES")
                .setPAYMENT_TYPE_ID("PPI")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat((cashierPage.listOfPayModes()).contains("UPI"));//UPI should be present
    }
    @Parameters({"theme"})
    @Feature("PGP-39547")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify message displayed in cashier page when Paytm Postapid and Paytm Wallet are enabled")
    public void PGP_39547_TC_01_NonLoggedInFlow_PostpaidWalletEnabled(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICON_ON_MERCHANT_TC05, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(theme.equals(Constants.Theme.ENHANCED_WAP_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payPaytm = cashierPage.noQRAvailableText().getText();
            Assertions.assertThat(payPaytm).contains("Pay with Paytm");
            cashierPage.loginStrip().assertVisible();
            String upiEnabledMessage = cashierPage.noQRPaymodesPresent().getText();
            Assertions.assertThat(upiEnabledMessage).contains("Pay using Paytm Postpaid or Paytm Wallet");
            String walletPostCardDisabledMessage = cashierPage.noQRInfoPaymodes().getText();
            Assertions.assertThat(walletPostCardDisabledMessage).contains("Saved Cards and UPI are not available for this transaction");
        }
        else if(theme.equals(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payByQR = cashierPage.qrCodeCheckoutJSText().getText();
            Assertions.assertThat(payByQR).contains("Scan QR with Paytm App");
            cashierPage.imgScanPayQRCode().assertVisible();
            cashierPage.loginStrip().assertNotVisible();
            String enabledText = cashierPage.enabledPaymodes().getText();
            Assertions.assertThat(enabledText).contains("Pay using Paytm Postpaid or Paytm Wallet");
            cashierPage.QrKnowMore().click();
            String disabledText = cashierPage.infoStripPaymodes().getText();
            Assertions.assertThat(disabledText).contains("Merchant has not enabled Saved Cards and UPI for this Transaction");
        }
        cashierPage.otherPaymentOption().assertNotVisible();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.tabCreditCard().assertNotVisible();
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantPayModes").contains("merchantAccept")).isEqualTo(true);
    }

    @Parameters({"theme"})
    @Feature("PGP-39547")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify message displayed in cashier page when Paytm Postapid and Cards are enabled")
    public void PGP_39547_TC_02_NonLoggedInFlow_PostpaidCardsEnabled(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICON_ON_MERCHANT_TC06, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(theme.equals(Constants.Theme.ENHANCED_WAP_REVAMP)) {
            String payPaytm = cashierPage.noQRAvailableText().getText();
            Assertions.assertThat(payPaytm).contains("Pay with Paytm");
            cashierPage.waitUntilLoads();
            cashierPage.loginStrip().assertVisible();
            String upiEnabledMessage = cashierPage.noQRInfoPaymodes().getText();
            Assertions.assertThat(upiEnabledMessage).contains("Pay using Paytm Postpaid or Saved Cards");
            String walletPostCardDisabledMessage = cashierPage.noQRPaymodesPresent().getText();
            Assertions.assertThat(walletPostCardDisabledMessage).contains("Paytm Wallet and UPI are not available for this transaction");
        }
        else if(theme.equals(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            cashierPage.waitUntilLoads();
            cashierPage.imgScanPayQRCode().assertNotVisible();
            cashierPage.loginStrip().assertVisible();
            String enabledText = cashierPage.noQRPaymodesPresent().getText();
            Assertions.assertThat(enabledText).contains("Pay using Paytm Postpaid or Saved Cards");
            String disabledText = cashierPage.noQRInfoPaymodes().getText();
            Assertions.assertThat(disabledText).contains("Paytm Wallet and UPI are not available for this transaction");
        }
        String otherPaymentOptions = cashierPage.otherPaymentOption().getText();
        Assertions.assertThat(otherPaymentOptions).contains("Other Payment Options");
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabUPI().assertNotVisible();
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantPayModes").contains("merchantAccept")).isEqualTo(false);

    }

    @Parameters({"theme"})
    @Feature("PGP-39547")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify message displayed in cashier page when Paytm Wallet and Cards are enabled")
    public void PGP_39547_TC_03_NonLoggedInFlow_WalletCardsEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICON_ON_MERCHANT_TC08, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(theme.equals(Constants.Theme.ENHANCED_WAP_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payPaytm = cashierPage.noQRAvailableText().getText();
            Assertions.assertThat(payPaytm).contains("Pay with Paytm");
            cashierPage.loginStrip().assertVisible();
            String upiEnabledMessage = cashierPage.noQRPaymodesPresent().getText();
            Assertions.assertThat(upiEnabledMessage).contains("Pay using Paytm Wallet or Saved Cards");
            String walletPostCardDisabledMessage = cashierPage.noQRInfoPaymodes().getText();
            Assertions.assertThat(walletPostCardDisabledMessage).contains("Paytm Postpaid and UPI are not available for this transaction");
        }
        else if(theme.equals(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payByScan = cashierPage.qrCodeCheckoutJSText().getText();
            Assertions.assertThat(payByScan).contains("Scan QR using Paytm App");
            cashierPage.imgScanPayQRCode().assertVisible();
            cashierPage.loginStrip().assertNotVisible();
            String walletCardsEnabledMessage = cashierPage.enabledPaymodes().getText();
            Assertions.assertThat(walletCardsEnabledMessage).contains("Pay using Paytm Wallet or Saved Cards");
            String postpaidUPIDisabledMessage = cashierPage.infoStripPaymodes().getText();
            Assertions.assertThat(postpaidUPIDisabledMessage).contains("Paytm Postpaid and UPI are not available for this transaction");

        }
        cashierPage.waitUntilLoads();
        String otherPaymentOptions = cashierPage.otherPaymentOption().getText();
        Assertions.assertThat(otherPaymentOptions).contains("Other Payment Options");
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabUPI().assertNotVisible();
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantPayModes").contains("merchantAccept")).isEqualTo(true);
    }

    @Parameters({"theme"})
    @Feature("PGP-39547")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify message displayed in cashier page when only UPI is enabled")
    public void PGP_39547_TC_04_NonLoggedInFlow_UPIEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICON_ON_MERCHANT_TC14, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(theme.equals(Constants.Theme.ENHANCED_WAP_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payPaytm = cashierPage.noQRAvailableText().getText();
            Assertions.assertThat(payPaytm).contains("Pay with Paytm");
            cashierPage.loginStrip().assertVisible();
            String upiEnabledMessage = cashierPage.noQRPaymodesPresent().getText();
            Assertions.assertThat(upiEnabledMessage).contains("Pay using UPI");
            String walletPostCardDisabledMessage = cashierPage.noQRInfoPaymodes().getText();
            Assertions.assertThat(walletPostCardDisabledMessage).contains("Paytm Postpaid, Wallet and Saved Cards are not available for this transaction");
        }
        else if(theme.equals(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payByScan = cashierPage.qrCodeCheckoutJSText().getText();
            Assertions.assertThat(payByScan).contains("Scan QR with Paytm or Any UPI App");
            cashierPage.imgScanPayQRCode().assertVisible();
            cashierPage.loginStrip().assertNotVisible();
            String walletCardsEnabledMessage = cashierPage.enabledPaymodes().getText();
            Assertions.assertThat(walletCardsEnabledMessage).contains("Pay using UPI");
            cashierPage.QrKnowMore().click();
            String postpaidUPIDisabledMessage = cashierPage.infoStripPaymodes().getText();
            Assertions.assertThat(postpaidUPIDisabledMessage).contains("Merchant has not enabled Paytm Postpaid, Wallet and Saved Cards for this Transaction");

        }
        String otherPaymentOptions = cashierPage.otherPaymentOption().getText();
        Assertions.assertThat(otherPaymentOptions).contains("Other Payment Options");
        cashierPage.tabUPI().assertVisible();
        cashierPage.tabDebitCard().assertNotVisible();
    }

    @Parameters({"theme"})
    @Feature("PGP-39547")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify message displayed in cashier page when all paymodes are enabled")
    public void PGP_39547_TC_05_NonLoggedInFlow_AllPaymodesEnabled(@Optional("enhancedwap_revamp") String theme) throws Exception {
        String TxnAmount = "2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ICON_ON_MERCHANT_TC15, theme)
                .setTXN_AMOUNT(TxnAmount)
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(theme.equals(Constants.Theme.ENHANCED_WAP_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payPaytm = cashierPage.noQRAvailableText().getText();
            Assertions.assertThat(payPaytm).contains("Pay with Paytm");
            cashierPage.loginStrip().assertVisible();
            String upiEnabledMessage = cashierPage.noQRPaymodesPresent().getText();
            Assertions.assertThat(upiEnabledMessage).contains("Pay using Paytm Postpaid, Paytm Wallet, Saved Cards or UPI");
            cashierPage.noQRInfoPaymodes().assertNotVisible();
        }
        else if(theme.equals(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            cashierPage.waitUntilLoads();
            String payByScan = cashierPage.qrCodeCheckoutJSText().getText();
            Assertions.assertThat(payByScan).contains("Scan QR with Paytm or Any UPI App");
            cashierPage.imgScanPayQRCode().assertVisible();
            cashierPage.loginStrip().assertNotVisible();
            String walletCardsEnabledMessage = cashierPage.enabledPaymodes().getText();
            Assertions.assertThat(walletCardsEnabledMessage).contains("Pay using Paytm Postpaid, Paytm Wallet, Saved Cards or UPI");
            cashierPage.infoStripPaymodes().assertNotVisible();

        }
        String otherPaymentOptions = cashierPage.otherPaymentOption().getText();
        Assertions.assertThat(otherPaymentOptions).contains("Other Payment Options");
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabUPI().assertVisible();
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantPayModes").contains("merchantAccept")).isEqualTo(true);


    }
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP-40026")
    @Parameters({"theme"})
    @Test(description = "Verify the upi text and verify default vpa suggestions")
    public void TC_01_PGP_40026_VerifyDefaultUPIHandles(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.UPI_NATIVE_SUBS, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("srivastavaprateek@");
        Assertions.assertThat(cashierPage.upiHandlers())
                .as("UPI handlers")
                .containsSequence("@paytm@upi@ybl@ibl@axl@okhdfcbank");
    }

    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP-40026")
    @Parameters({"theme"})
    @Test(description = "Verify the upi text and verify additional vpa suggestions")
    public void TC_02_PGP_40026_VerifyAdditionalUPIHandles(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String[] upiHandles = {"@okaxis", "@apl", "@indus", "@boi", "@cnrb", "@icici", "@dbs"};
        String TxnAmount = "1";
        String TxnMaxAmount = "10";
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.SubscriptionUPI(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                .setCHANNEL_ID("WEB")
                .setSSO_TOKEN(user.ssoToken())
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT(TxnAmount)
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        new CheckoutPage().createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        for (int i = 0; i < upiHandles.length; i++) {
            cashierPage.tabUPI().click();
            Assertions.assertThat(cashierPage.getEnhancedUPIText().getText()).isEqualTo("Pay by Entering UPI ID");
            cashierPage.textBoxVPA().clearAndType("srivastavaprateek" + upiHandles[i]);
            Assertions.assertThat(cashierPage.enhancedUPIHandles().getText()).isEqualTo(upiHandles[i]);
            cashierPage.feedbackCrossBtn().click();
        }
    }

        @Parameters({"theme"})
        @Feature("PGP-39255")
        @Owner(Constants.Owner.TAMANA_TATHAN)
        @Test(description = "Verify the UI for QR Flow")
        public void PGP_39255_TC_01(@Optional("enhancedweb_revamp") String theme){
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme)
                    .setTXN_AMOUNT("2")
                    .build();
            new CheckoutPage().createOrder(orderDTO);
            validatingUIForQRFlow(theme, true);
        }

        @Parameters({"theme"})
        @Feature("PGP-39255")
        @Owner(Constants.Owner.TAMANA_TATHAN)
        @Test(description = "Verify the QR is not displayed when UPI and wallet are disabled")
        public void PGP_39255_TC_02(@Optional("enhancedweb_revamp") String theme){
            OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.Subscription_Pg2_MID5, theme)
                    .setTXN_AMOUNT("2")
                    .build();
            new CheckoutPage().createOrder(orderDTO);
            validatingUIForQRFlow(theme, false);
        }

        @Parameters({"theme"})
        @Feature("PGP-39255")
        @Owner(Constants.Owner.TAMANA_TATHAN)
        @Test(description = "Verify the Ui when QR is disabled")
        public void PGP_39255_TC_03(@Optional("enhancedweb_revamp") String theme){
            Constants.MerchantType merchant = Constants.MerchantType.Notification_Merchant;
            OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                    .setTXN_AMOUNT("2")
                    .build();
            new CheckoutPage().createOrder(orderDTO);
            validatingUIForQRFlow(theme, false);

        }

        @Parameters({"theme"})
        @Feature("PGP-39255")
        @Owner(Constants.Owner.TAMANA_TATHAN)
        @Test(description = "Verify the UI for QR Flow for Appinvoke")
        public void PGP_39255_TC_04(@Optional("enhancedweb_revamp") String theme){
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL).build();
            InitTxn initTxn = new InitTxn(initTxnDTO);
            JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
            String txnToken = initTrxJsonPath.getString("body.txnToken");
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL,initTxnDTO.getBody().getOrderId(),txnToken).build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            validatingUIForQRFlow(theme, true);
        }

        @Parameters({"theme"})
        @Feature("PGP-39255")
        @Owner(Constants.Owner.TAMANA_TATHAN)
        @Test(description = "Verify the QR is not displayed when UPI and wallet are disabled for Appinvoke")
        public void PGP_39255_TC_05(@Optional("enhancedweb_revamp") String theme){
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.ICON_ON_MERCHANT_TC06).build();
            InitTxn initTxn = new InitTxn(initTxnDTO);
            JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
            String txnToken = initTrxJsonPath.getString("body.txnToken");
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.ICON_ON_MERCHANT_TC06,initTxnDTO.getBody().getOrderId(),txnToken).build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            validatingUIForQRFlow(theme, false);
        }

        @Parameters({"theme"})
        @Feature("PGP-39255")
        @Owner(Constants.Owner.TAMANA_TATHAN)
        @Test(description = "Verify the Ui when QR is disabled for Appinvoke")
        public void PGP_39255_TC_06(@Optional("enhancedweb_revamp") String theme){
            Constants.MerchantType merchant = Constants.MerchantType.Notification_Merchant;
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
            InitTxn initTxn = new InitTxn(initTxnDTO);
            JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
            String txnToken = initTrxJsonPath.getString("body.txnToken");
            OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant,initTxnDTO.getBody().getOrderId(),txnToken).build();
            checkoutPage.createAppInvokeOrder(orderDTO);
            validatingUIForQRFlow(theme, false);
        }

        public static void validatingUIForQRFlow(String theme, Boolean validate){
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
          if(validate) {
                Assert.assertTrue(cashierPage.imgScanPayQRCode().isDisplayed(), "Validating the QR is displayed or not");
                Assert.assertTrue(cashierPage.qrCodeCheckoutJSText().getText().equals("Scan QR with Paytm or Any UPI App"), "Validating the Login Message for scaning QR code");
                Assert.assertTrue(cashierPage.enabledPaymodes().getText().contains("Pay using Paytm Wallet, Saved Cards or UPI"), "Validating the enabled paymode message");
                Assert.assertTrue(cashierPage.QrKnowMore().isDisplayed(), "Know More Button is displayed");
                cashierPage.QrKnowMore().click();
                cashierPage.pause(3);
                if(theme.equals("enhancedweb_revamp"))
                    Assert.assertTrue(cashierPage.modalRetryPayment().isDisplayed(), "After Clicking know more button a pop up window is displayed");
                else
                    Assert.assertTrue(cashierPage.knowMoreLinkPopup().isDisplayed(), "After Clicking know more button a pop up window is displayed");
            }else{
                Assert.assertFalse(cashierPage.imgScanPayQRCode().isElementPresent(), "Validating the QR is not displayed as wallet and UPI is disabled");
                Assert.assertTrue(cashierPage.noQRAvailableText().getText().contains("Pay with Paytm"), "Validating the login strip");
            }
        }


}
