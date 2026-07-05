package scripts.UI;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import jdk.jfr.Enabled;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.checkoutjs.CheckoutJsBase;

import javax.validation.constraints.AssertTrue;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.paytm.appconstants.Constants.MerchantType.*;


public class RedirectionNewWindow extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Owner("ANUSHKA GOLDI")
    @Feature("PGP-50747")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window (CC)")
    public void CC_redirection_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();

    }

    @Owner("ANUSHKA GOLDI")
    @Feature("PGP-50747")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window (DC)")
    public void DC_redirection_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC,new PaymentDTO().setCreditCardNumber("4444333322221111"));

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();

    }

    @Owner("ANUSHKA GOLDI")
    @Feature("PGP-50747")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window  PPBL")
    public void validatePPBLTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(COBRANDED_DEPRIORITISE_DC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();

    }

    @Owner("ANUSHKA GOLDI")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window (NB)")
    public void validateNBTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,new PaymentDTO().setBankName("ICICI"));


        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();
    }

    @Owner("ANUSHKA GOLDI")
    @Feature("PGP-50747")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window (Postpaid)")
    public void validatePostpaidTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(COBRANDED_DEPRIORITISE_DC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);


        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();
    }

    @Owner("ANUSHKA GOLDI")
    @Feature("PGP-50747")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window (UPI COLLECT) ")
    public void Validate_UPI_COLLECT_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();
    }

    @Owner("ANUSHKA GOLDI")
    @Feature("PGP-50747")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window (CC saved card)", enabled = true)
    public void validate_SavedCCTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PG2WALLETUSER);
//        PaymentDTO paymentDTO = new PaymentDTO();
//        SavedCardHelpers.deleteSavedCard(user);
//        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
//                paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(EMI_DC_CC, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();
    }

    @Owner("ANUSHKA GOLDI")
    @Feature("PGP-50747")
    @Parameters({"theme"})
    @Test(description = "To verify in redirection flow bank pages does not opens in a New window (EMI CC)")
    public void Validate_EMI_CC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.EMI_DC_CC, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabEMI().click();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("HDFC Bank Debit Card");
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.selectBankMandateBank("HDFC Bank Debit Card").click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD);

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();
    }


    private final CheckoutJsCheckoutPage checkoutJsPage = new CheckoutJsCheckoutPage();

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", status).getList(
                "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains(payMethod);
        return fetchPaymentOptionsJson;
    }

    @Owner("ANUSHKA GOLDI")
    @Parameters({"theme"})
    @Feature("PGP-50747")
    @Test(description = "To verify in CheckoutJS flow bank pages opens in a New window (CC) ")
    public void validate_CC_CheckoutJSTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(PGOnly.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.payBy(Constants.PayMode.CC,new PaymentDTO().setCreditCardNumber("1095101337351506"));
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isTrue();
    }


    @Owner("ANUSHKA GOLDI")
    @Parameters({"theme"})
    @Feature("PGP-50747")
    @Test(description = "To verify in CheckoutJS flow bank pages opens in a New window (DC)")
    public void validate_DC_CheckoutJSTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.PGOnly.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.payBy(Constants.PayMode.DC,new PaymentDTO().setDebitCardNumber("4444333322221111"));
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isTrue();

    }



    @Owner("ANUSHKA GOLDI")
    @Parameters({"theme"})
    @Feature("PGP-50747")
    @Test(description = "To verify in CheckoutJS flow bank pages does not open for (PPBL) txn JIRA- PGP-50878")
    public void validate_PPBL_CheckoutJSTxn(@Optional("checkoutjs_web_revamp")  String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType
                .PGOnly).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.PGOnly.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        cashierPage.payBy(Constants.PayMode.PPBL);

        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isFalse();

    }

    @Owner("ANUSHKA GOLDI")
    @Parameters({"theme"})
    @Feature("PGP-50747")
    @Test(description = "To verify in CheckoutJS flow bank pages opens in a New window (NB)")
    public void validate_NB_CheckoutJSTxn(@Optional("checkoutjs_web_revamp")  String theme)throws Exception{

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutJsPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(Constants.MerchantType.PGOnly.getId());
        config.data.setOrderId(initTxnDTO.orderFromBody());
        config.data.setToken(txnToken);
        checkoutJsPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();
        cashierPage.payBy(Constants.PayMode.NB,new PaymentDTO().setBankName("ICICI"));
        String parent = DriverManager.getDriver().getWindowHandle();
        Set<String> s =DriverManager.getDriver().getWindowHandles();
        Iterator<String> windowTabs = s.iterator();
        Boolean NewWindowPopIsShowing = false;

        while(windowTabs.hasNext()) {
            String child_window = windowTabs.next();
            if (!parent.equals(child_window)) {
                System.out.println("Popup bank pages opens in a New window");
                NewWindowPopIsShowing = true;
            }
            else{
                System.out.println("Popup bank pages opening in same window");
                NewWindowPopIsShowing = false;
            }
        }

        Assertions.assertThat(NewWindowPopIsShowing).isTrue();
    }

}
