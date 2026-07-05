package scripts.StandardCheckoutPriority;

import com.paytm.LocalConfig;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.offline.FetchPayInstrument;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OfflineDto.FetchPayInstrumentRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static com.paytm.appconstants.Constants.MerchantType.DEFAULT_PRIORITY_LIST;
import static com.paytm.appconstants.Constants.MerchantType.PRIORITY_LIST;
import static org.hamcrest.Matchers.equalTo;

@Owner(Constants.Owner.PRIYANSHI)
@Feature("PGP-31228")
@Parameters({"theme"})
public class PriorityTest extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    private List<String> getListOfPayModesOnCashierPage(CashierPage cashierPage) {
        List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
        List<String> paymethodList = new ArrayList<>();
        for (int i = 0; i < PaymodesOnPage.size(); i++) {
            paymethodList.add(PaymodesOnPage.get(i).getText().split("\n")[0]);
        }
        return paymethodList;
    }

    public Response validateFetchPayInstrument(String mid, String token) {
        FetchPayInstrumentRequest fetchPayInstrumentRequest = new FetchPayInstrumentRequest();
        fetchPayInstrumentRequest.changeMidInRequest(mid);
        fetchPayInstrumentRequest.changeTokenInRequest(token);
        Response response = FetchPayInstrument.executeFetchPaymtInstrument(fetchPayInstrumentRequest);
        Assertions.assertThat(response.getStatusCode()).as("Status Code is: " + response.getStatusCode() + " expected was: 200").isEqualTo(200);
        return response;
    }


    @Test(description = "Verifying Paymodes sequence on Enhanched web when enhanced Priority List is Configured")
    @Parameters({"theme"})
    public void ValidatePriorityListonEnhancedwebLogin(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = PRIORITY_LIST;
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantType, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertTrue(paymethodList.get(0).contains("Credit Card"));
        Assert.assertTrue(paymethodList.get(1).contains("Debit Card"));
        Assert.assertTrue(paymethodList.get(2).contains("EMI"));
        Assert.assertTrue(paymethodList.get(3).contains("UPI"));
        Assert.assertTrue(paymethodList.get(4).contains("Balance"));
        Assert.assertTrue(paymethodList.get(5).contains("Net Banking"));
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected());

    }

    @Test(description = "Verifying Paymodes sequence on Enhanched web when enhanced Priority List is Configured in non-log in flow")
    @Parameters({"theme"})
    public void ValidatepriorityListonEnhancedwebWithoutLogin(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType = PRIORITY_LIST;
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantType, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        cashierPage.loginStrip().click();
        Assert.assertTrue(paymethodList.get(0).contains("Credit Card"));
        Assert.assertTrue(paymethodList.get(1).contains("Debit Card"));
        Assert.assertTrue(paymethodList.get(2).contains("EMI"));
        Assert.assertTrue(paymethodList.get(3).contains("UPI"));
        Assert.assertTrue(paymethodList.get(4).contains("Net Banking"));
        Assertions.assertThat(cashierPage.loginStrip().isSelected());
    }


//    @Test(description = "Verifying Paymodes sequence on Subscription flow when subscription Priority List is Configured", enabled = false)
//    @Parameters({"theme"})
    public void ValidatePriorityListonSubsflow(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = PRIORITY_LIST;
        OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(merchantType, theme, user)
                .setSUBS_PAYMENT_MODE("")
                .setSUBS_PPI_ONLY("")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertTrue(paymethodList.get(0).contains("UPI"));
        Assert.assertTrue(paymethodList.get(1).contains("Debit Card"));
        Assert.assertTrue(paymethodList.get(2).contains("Credit Card"));
        Assert.assertTrue(paymethodList.get(3).contains("Balance"));
        Assert.assertTrue(paymethodList.get(4).contains("EMI"));
        Assert.assertTrue(paymethodList.get(5).contains("Net Banking"));
        Assertions.assertThat(cashierPage.tabUPI().isSelected());

    }


    @Test(description = "Verifying Paymodes sequence on Native flow when Native Priority List is Configured")
    public void ValidatePriorityListonNativeflow() throws Exception {
        Constants.MerchantType merchantType = PRIORITY_LIST;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .setCustId(CommonHelpers.generateOrderId())
                .build();
        String txnToken = InitTxn.executeInitTxn(initTxnDTO).getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response response = fetchPaymentOption.execute();
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Net Banking'}.priority",
                        equalTo("1"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'BHIM UPI'}.priority",
                        equalTo("2"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Credit Card'}.priority",
                        equalTo("3"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Debit Card'}.priority",
                        equalTo("4"));

        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Balance'}.priority",
                        equalTo("5"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.priority",
                        equalTo("6"));


    }

    @Test(description = "Verifying Paymodes sequence on Native flow when Default Priority List is Configured")
    public void ValidateDefaultPriorityListonNativeflow() throws Exception {
        Constants.MerchantType merchantType = DEFAULT_PRIORITY_LIST;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .setCustId(CommonHelpers.generateOrderId())
                .build();
        String txnToken = InitTxn.executeInitTxn(initTxnDTO).getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response response = fetchPaymentOption.execute();
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Net Banking'}.priority",
                        equalTo("1"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'BHIM UPI'}.priority",
                        equalTo("2"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.priority",
                        equalTo("3"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Credit Card'}.priority",
                        equalTo("4"));

        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Balance'}.priority",
                        equalTo("5"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Debit Card'}.priority",
                        equalTo("6"));


    }

    @Test(description = "Verifying Paymodes sequence on Enhanced Flow when Default Priority List is Configured")
    @Parameters({"theme"})
    public void ValidatePriorityListonDefaultflow(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = DEFAULT_PRIORITY_LIST;
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantType, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertTrue(paymethodList.get(0).contains("Net Banking"));
        Assert.assertTrue(paymethodList.get(1).contains("UPI"));
        Assert.assertTrue(paymethodList.get(2).contains("EMI"));
        Assert.assertTrue(paymethodList.get(3).contains("Credit Card"));
        Assert.assertTrue(paymethodList.get(4).contains("Balance"));
        Assert.assertTrue(paymethodList.get(5).contains("Debit Card"));
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected());

    }

    @Test(description = "Verifying Paymodes sequence on Offline Flow when Default Priority List is Configured")
    public void ValidateDefaultPriorityListonOfflineflow() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Response response = validateFetchPayInstrument(DEFAULT_PRIORITY_LIST.getId(), user.ssoToken());

        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Net Banking'}.priority",
                        equalTo(1));
        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'BHIM UPI'}.priority",
                        equalTo(2));

        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Credit Card'}.priority",
                        equalTo(3));
        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Wallet'}.priority",
                        equalTo(4));
        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Debit Card'}.priority",
                        equalTo(5));


    }


    @Test(description = "Verifying Paymodes sequence on Offline Flow when Offline Priority List is Configured")
    public void ValidatePriorityListonOfflineflow() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Response response = validateFetchPayInstrument(PRIORITY_LIST.getId(), user.ssoToken());

        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'BHIM UPI'}.priority",
                        equalTo(1));
        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Debit Card'}.priority",
                        equalTo(2));

        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Credit Card'}.priority",
                        equalTo(3));
        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Wallet'}.priority",
                        equalTo(4));
        response.then()
                .body("body.payMethodViews.merchantPayMethods.find{it.displayName  == 'Net Banking'}.priority",
                        equalTo(5));


    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31229")
    @Test(description = "Verify Paymode sequence priority in fetchpayOptions api Response when paymode sequence is passed in initiate txn API")
    public void ValidatePriorityListonPassingPaymodeSequenceInitiateAPI() throws Exception {
        Constants.MerchantType merchantType = PRIORITY_LIST;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .setCustId(CommonHelpers.generateOrderId())
                .setPaymodeSequence("NB,UPI,CC,DC,BALANCE,EMI")
                .build();
        String txnToken = InitTxn.executeInitTxn(initTxnDTO).getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v2")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response response = fetchPaymentOption.execute();
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Net Banking'}.priority",
                        equalTo("1"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'BHIM UPI'}.priority",
                        equalTo("2"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Credit Card'}.priority",
                        equalTo("3"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Debit Card'}.priority",
                        equalTo("4"));

        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Balance'}.priority",
                        equalTo("5"));
        response.then()
                .body("body.merchantPayOption.paymentModes.find{it.displayName  == 'EMI'}.priority",
                        equalTo("6"));

    }


    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31229")
    @Parameters({"theme"})
    @Test(description = "Verify Paymode sequence priority in fetchpayOptions api Response when paymode sequence is passed in initiate txn API on a ShowPaymentPage")
    public void ValidatePaymodeSequenceInFetchPaymentPageonPassingPaymodeSequenceInitiateAPI(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = PRIORITY_LIST;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setCustId(CommonHelpers.generateOrderId())
                .setPaymodeSequence("NB,UPI,CC,BALANCE,DC")
                .build();
        String txnToken = InitTxn.executeInitTxn(initTxnDTO).getBody().getTxnToken();

        DriverManager.getDriver().get(LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_PAYMENT_PAGE + "?mid=" + merchantType.getId() + "&orderId=" + initTxnDTO.orderFromBody() + "&txnToken=" + txnToken);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertTrue(paymethodList.get(0).contains("Net Banking"));
        Assert.assertTrue(paymethodList.get(1).contains("UPI"));
        Assert.assertTrue(paymethodList.get(2).contains("Credit Card"));
        Assert.assertTrue(paymethodList.get(3).contains("Balance"));
        Assert.assertTrue(paymethodList.get(4).contains("Debit Card"));
        Assert.assertTrue(paymethodList.get(5).contains("EMI"));


    }

}
