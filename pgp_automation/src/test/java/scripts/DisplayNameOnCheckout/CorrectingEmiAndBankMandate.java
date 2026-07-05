package scripts.DisplayNameOnCheckout;

import com.paytm.api.nativeAPI.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;


public class CorrectingEmiAndBankMandate extends PGPBaseTest{
    private final CheckoutPage checkoutPage = new CheckoutPage();


    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = HDFC is coming for HDFC EMI in APP_DATA in enhancedweb")
    public void verifyChannelDisplayNameInConsoleAppData(@Optional("enhancedweb_revamp") String theme) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath JsonPath = initTxn.execute().jsonPath();
        String txnToken = JsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=HDFC");
        Assertions.assertThat(response).contains("emiType=CREDIT_CARD");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = ICICI is coming for ICICI EMI in APP_DATA in enhancedweb")
    public void verifyChannelDisplayNameInConsoleAppData1(@Optional("enhancedweb_revamp") String theme) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath JsonPath = initTxn.execute().jsonPath();
        String txnToken = JsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=ICICI");
        Assertions.assertThat(response).contains("emiType=CREDIT_CARD");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = BAJAJFN is coming for BAJAJFN EMI in APP_DATA in enhancedweb")
    public void verifyChannelDisplayNameInConsoleAppData2(@Optional("enhancedweb_revamp") String theme) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.PGOnly).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath JsonPath = initTxn.execute().jsonPath();
        String txnToken = JsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.PGOnly, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=BAJAJFN");
        Assertions.assertThat(response).contains("emiType=CREDIT_CARD");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = HDFC is coming for HDFC EMI in FPO in checkoutjs")
    public void verifyChannelDisplayNameInConsoleInFPOResponse(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).
                build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.loginStrip().click();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int paymodeSize = fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes").size();
        for (int i=0;i<paymodeSize;i++){
            if(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[\" + i + \"].displayName")=="EMI"){
               int payoptionSize =  fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes[\" + i + \"].payChannelOptions").size();
                HashMap<String,String>payChannelOption = new HashMap<>();
                for (int j=0;j<payoptionSize;j++){
                   String channelDisplayName = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[\" + i + \"].payChannelOptions[\" + j + \"]");
                  payChannelOption.put(channelDisplayName,"present");

                }
                Assertions.assertThat(payChannelOption).containsKey("HDFC");
                Assertions.assertThat(payChannelOption).containsKey("ICICI");
                Assertions.assertThat(payChannelOption).containsKey("BAJAJFN");
                break;

            }
        }
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = PPBL is coming for PPBL BankMandate in APP_DATA")
    public void verifyChannelDisplayNameInConsoleAppDataInBankMandate(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=PPBL");
        Assertions.assertThat(response).contains("mandateMode=E_MANDATE");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = HDFC is coming for HDFC BankMandate in APP_DATA")
    public void verifyChannelDisplayNameInConsoleAppDataInBankMandate1(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=HDFC");
        Assertions.assertThat(response).contains("mandateMode=E_MANDATE");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = CITIUB is coming for CITIUB BankMandate in APP_DATA")
    public void verifyChannelDisplayNameInConsoleAppDataInBankMandate2(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=CITIUB");
        Assertions.assertThat(response).contains("mandateMode=E_MANDATE");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = BOM is coming for BOM BankMandate in APP_DATA")
    public void verifyChannelDisplayNameInConsoleAppDataInBankMandate3(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=BOM");
        Assertions.assertThat(response).contains("mandateMode=E_MANDATE");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-38796")
    @Parameters({"theme"})
    @Test(description = "Verify that channelDisplayName = RATN is coming for RATN BankMandate in APP_DATA")
    public void verifyChannelDisplayNameInConsoleAppDataInBankMandate4(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        Object object= new Object();
        object = cashierPage.getAppData();
        System.out.println(object);
        String response = object.toString();
        Assertions.assertThat(response).contains("channelDisplayName=RATN");
        Assertions.assertThat(response).contains("mandateMode=E_MANDATE");
    }

}
