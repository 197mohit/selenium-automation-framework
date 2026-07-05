package scripts.UI;
import com.paytm.LocalConfig;
import com.paytm.api.MappingService.GetMerchantPreferenceInfo;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.theia.ApiV1ApplyPromo;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedPaymentOffers;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.UPIIntentRequestDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.listenerDecorators.DefaultExtentListener;
import com.paytm.framework.reporting.listenerDecorators.DefaultTestNGListener;
import com.paytm.framework.utils.CommonUtils;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.testng.Assert;
import org.testng.annotations.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.*;

/**
 * Created by Nirottam Singh on 06/07/2023
 */


public class redirectionalflowP0NewCases extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful CC Txn on redirectional flow", groups = "p0")
    public void Successfull_CC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
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
                .assertAll();
    }
    @Parameters({"theme"})
    @Test(description = "To verify successful txn using CC saved card")
    public void validateSuccessfull_SavedCCTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(COFT_MERCHANT, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
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
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful txn using DC saved card")
    public void validateSuccessfull_SavedDCTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(COFT_MERCHANT, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
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
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful DC Txn on redirectional flow", groups = "p0")
    public void Successfull_DC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
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
                .assertAll();

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful UPI COLLECT Txn on redirectional flow", groups = "p0")
    public void Successfull_UPI_COLLECT_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful EMI CC Txn on redirectional flow", groups = "p0")
    public void Successfull_EMI_CC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.EMI_DC_CC, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(Constants.MerchantType.EMI_DC_CC.getKey())
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful Saved EMI  Txn on redirectional flow", groups = "p0")
    public void Successfull_SavedEMI_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(EMI_DC_CC, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), "4761360075860428");
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(EMI_DC_CC.getKey())
                .assertAll();
    }


    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful transaction for wallet")
    public void validateSucessfullWalletTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user,txnAmount);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful transaction for PPBL")
    public void validateSucessfullPPBLTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateBankName("PPBL")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful transaction for NB")
    public void validateSucessfullNBTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB,new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateBankName("ICICI")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful transaction for Postpaid", groups = "p0")
    public void validateSucessfullPostpaidTransaction(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful Login With Correct Otp with rememberMe checkbox ", groups = "p0")
    public void validateSucessfullLoginWithCorrectOtp(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin("7014107741","888888");
        Assert.assertTrue(cashierPage.tabPPI().isVisible().asBoolean());

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Failure Login With Wrong Otp", groups = "p0")
    public void validateFailureLoginWithWrongOtp(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String wrongOtp="111";
        String mobNo="7014107741";
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.signin(mobNo,wrongOtp);
        Assert.assertTrue(cashierPage.tabPPI().isVisible().asBoolean());

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful Login Without remember me checkbox with correct otp", groups = "p0")
    public void validateSucessfullLoginWithoutRememberMe(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.rememberMeCheckbox().click();
        cashierPage.signin("7014107741","888888");
        Assert.assertTrue(cashierPage.tabPPI().isVisible().asBoolean());

    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg With Wrong Upi Number ")
    public void validate_UPI_Error_Msg_With_Wrong_PhoneNumber(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.UpiNumericId().sendKeys("1231231234");
        cashierPage.buttonPGPayNow().click();
        Thread.sleep(100);
        String msg=cashierPage.errorTextsInUPIFlow().getText();
       Assert.assertEquals(msg, Constants.MessageAssert.UPI_NUMBER_ERROR.toString());

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg With Invalid Upi VPA ")
    public void validate_UPI_Error_Msg_With_Wrong_VPA(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().sendKeys("get@paytm");
        cashierPage.buttonPGPayNow().click();
        Thread.sleep(100);
        String msg=cashierPage.errorTextsInUPIFlow().getText();
        Assert.assertEquals(msg,Constants.MessageAssert.INVALID_VPA.toString());



    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg With Invalid Card Number ")
    public void validateError_Msg_With_Invalid_CardNumber(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
       cashierPage.tabCreditCard().click();
       cashierPage.textBoxCardNumber().clearAndType("444433");
       cashierPage.buttonPGPayNow().click();
       String msg=cashierPage.paymentContainer().getText();
       Assertions.assertThat(msg).contains(Constants.MessageAssert.INVALID_CARD_NUMBER.toString());
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg With Invalid Expiry Date ")
    public void validateError_Msg_With_Invalid_Expiry(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4444333322221111");
       cashierPage.fillExpiryMonth("03");
       cashierPage.fillExpiryYear("20");
       cashierPage.buttonPGPayNow().click();
       String msg=cashierPage.error_invalidExpiryDate().getText();
       Assert.assertEquals(msg,Constants.MessageAssert.INVALID_EXPIRY.toString());

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Error Msg With Invalid CVV ")
    public void validateError_Msg_With_Invalid_CVV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4444333322221111");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("25");
        cashierPage.textBoxCVVNumber().clearAndType("12");
        cashierPage.buttonPGPayNow().click();
        String msg=cashierPage.getError_invalidCVV().getText();
        Assert.assertEquals(msg, Constants.MessageAssert.INVALID_CVV.toString());


    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "verify Successfull UPI Collect Txn With Numeric Id")
    public void validate_Successfull_UPI_Collet_Txn_With_NumericId(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().click();
        cashierPage.tabUPI().click();
        cashierPage.UpiNumericId().sendKeys("8006006993");
       cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    //AddnPay cases



    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful addnpay txn using cc", groups = "p0")
    public void validateSucessfull_Addnpay_Txn_CC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(!cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().click();
        }
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.AddnPay.getKey())
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful addnpay txn using DC", groups = "p0")
    public void validateSucessfull_Addnpay_Txn_DC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(!cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().click();
        }
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.AddnPay.getKey())
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful addnpay txn using NB", groups = "p0")
    public void validateSucessfull_Addnpay_Txn_NB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(!cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().click();
        }
        cashierPage.payBy(Constants.PayMode.NB,new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.AddnPay.getKey())
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successful addnpay txn using postpaid", groups = "p0")
    public void validateSucessfull_Addnpay_Txn_Postpaid(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if(!cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().click();
        }
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.AddnPay.getKey())
                .assertAll();

    }

//PCF Cases

    @Parameters({"theme"})
    @Test(description = "Validate Successful PCF transaction for NB ICICI")
    public void validateSucessfullPCFTransactionviaICICINB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBL_NB_PCF, theme, user).
                setTXN_AMOUNT("10").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPG().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt)));
        Double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT())), 0, 4.00, "NB");

        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(actualChargeFeeAmt).as("NB").isEqualTo(expectedChargeFeeAmt);
        softAssert.assertAll();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("ICICI")
                .assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Validate Successful PCF transaction for DC")
    public void validateSucessfullPCFTransactionviaDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBL_NB_PCF, theme, user).
                setTXN_AMOUNT("10").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4444333322221111");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("25");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt)));
        Double expectedChargeFeeAmt = 0.59;
        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(actualChargeFeeAmt).as("DC").isEqualTo(expectedChargeFeeAmt);
        softAssert.assertAll();
        cashierPage.buttonPGPayNow().click();
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
                .validateGatewayName("HDFC")
                .assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Validate Successful PCF transaction for CC")
    public void validateSucessfullPCFTransactionviaCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBL_NB_PCF, theme, user).
                setTXN_AMOUNT("10").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4718650100010336");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("25");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt)));
        Double expectedChargeFeeAmt = 0.59;
        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(actualChargeFeeAmt).as("CC").isEqualTo(expectedChargeFeeAmt);
        softAssert.assertAll();
        cashierPage.buttonPGPayNow().click();
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
                .validateGatewayName("HDFC")
                .assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Validate Successful PCF transaction for PPBL")
    public void validateSucessfullPCFTransactionviaPPBL(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.PPBL_NB_PCF, theme, user).
                setTXN_AMOUNT("10").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabPPBL().click();
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.getTotalAmountOnCCDC().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt)));
        Double expectedChargeFeeAmt = 4.72;

        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(actualChargeFeeAmt).as("PPBL").isEqualTo(expectedChargeFeeAmt);
        softAssert.assertAll();
        cashierPage.payBy(Constants.PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBL")
                .assertAll();
    }

    //Addmoney cases

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful Addmoney CC Txn on redirectional flow", groups = "p0")
    public void Successfull_AddMoney_CC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(AddMoney, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setREQUEST_TYPE("ADD_MONEY")
                .setTXN_AMOUNT("100")
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
                .validateCheckSum(Constants.MerchantType.AddMoney.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful Addmoney DC Txn on redirectional flow", groups = "p0")
    public void Successfull_AddMoney_DC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddMoney, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setREQUEST_TYPE("ADD_MONEY")
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
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
                .validateCheckSum(Constants.MerchantType.AddMoney.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateTxnDate(new Date())
                .AssertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful Addmoney NB Txn on redirectional flow", groups = "p0")
    public void Successfull_AddMoney_NB_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.AddMoney, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setREQUEST_TYPE("ADD_MONEY")
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(Constants.MerchantType.AddMoney.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    //Insufficient balance cases

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Error Msg On UI In case of Insufficient Wallet Balance", groups = "p0")
    public void validateErrorMsg_InsufficientPPIBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.WalletOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user,Double.parseDouble(orderDTO.getTXN_AMOUNT()) -1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String msg=cashierPage.insufficientBalanceIconMsg().getText();
        Assertions.assertThat(msg).contains(Constants.MessageAssert.INSUFFICIENT_BALANCE.toString());


    }


    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "test err msg is displayed when user has insufficient PPBL balance")
    public void validateErrMsgWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabPPBL().isVisible();
        Assertions.assertThat(cashierPage.tabPPBL().content().toString()).contains(Constants.MessageAssert.INSUFFICIENT_BALANCE_PAYMENT.toString());
    }
    // Subscription cases
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "Validate Successfull UPI Subs Txn")
    public void validate_Successfull_UPISubsTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        OrderDTO orderDTO = new OrderFactory.UPIMandate(merchant, theme, user)
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT("10")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful Subs transaction using wallet Non Logged in Flow",groups = "p0")
    public void validate_Successfull_SubsTxnViaWallet(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY, theme).build();
        User user = userManager.getForRead(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful Subs transaction using wallet  Logged in Flow",groups = "p0")
    public void validate_Successfull_SubsTxnViaWalletLoogedIn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.SubscriptionWalletOnly(Constants.MerchantType.SUBSCRIPTION_WALLET_ONLY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateSubsid(Constants.ValidationType.NON_EMPTY)
                .AssertAll();
    }

    //Bank Mandate
    @Parameters({"theme"})
    @Test(description = "Verify that BankMandate subscription is successful through DC paymode ")
    public void validate_SuccessfullBankMandateTxn(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "Loan Amount Payment";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.BANK_MANDATE);
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();
    }

    // Coft tokenization cases
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify credit card is getting saved on Cashier Page.")
    public void vaildate_CC_SavedCardOnUI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.assertSavedCardVisibility();
    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify debit card is getting saved on Cashier Page.")
    public void vaildate_DC_SavedCardOnUI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.assertSavedCardVisibility();
    }

    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify successful FD Txn on redirection flow", groups = "p0")
    public void Successfull_FD_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(FD_PAYMODE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("3002")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setPasscode("3315");
        cashierPage.payBy(Constants.PayMode.PPBL,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(FD_PAYMODE.getKey())
                .assertAll();

    }

    //ZEST MOney Cases
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "validate Successful Transaction through Zest Money",groups = "p0")
    public void Verify_successful_transaction_with_ZestMoney(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = ZEST_MONEY2;
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(ZEST_MONEY2, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanks().selectByVisibleText("ZestMoney");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();


        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateBankName(Constants.Bank.ZESTNB.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }


    //Promo cases
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify promo text is visible on UI While Doing  Txn Via DC on redirection flow", groups = "p0")
    public void Successfull_Promo_DC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        for (int i = 0; i < 1; i++) {
            Promo promo = new Promo();
            new Merchant(Constants.MerchantType.NATIVE_PROMO_HYBRID.getId(), true).getPromos().add(promo);
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(NATIVE_PROMO_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4444333322221111");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("25");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        String promoTxt=cashierPage.applyPromoText().getText();
        Assert.assertNotNull(promoTxt);
        Assertions.assertThat(promoTxt).contains("cashback applicable");
        cashierPage.buttonPGPayNow().click();
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
                .assertAll();

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify promo text is visible on UI While Doing  Txn Via CC on redirection flow", groups = "p0")
    public void Successfull_Promo_CC_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        for (int i = 0; i < 1; i++) {
            Promo promo = new Promo();
            new Merchant(Constants.MerchantType.NATIVE_PROMO_HYBRID.getId(), true).getPromos().add(promo);
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(NATIVE_PROMO_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType("4718650100010336");
        cashierPage.fillExpiryMonth("03");
        cashierPage.fillExpiryYear("25");
        cashierPage.textBoxCVVNumber().clearAndType("123");
        String promoTxt=cashierPage.applyPromoText().getText();
        Assert.assertNotNull(promoTxt);
        Assertions.assertThat(promoTxt).contains("cashback applicable");
        cashierPage.buttonPGPayNow().click();
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
                .assertAll();

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify promo text is visible on UI While Doing  Txn Via NB on redirection flow", groups = "p0")
    public void Successfull_Promo_NB_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        for (int i = 0; i < 1; i++) {
            Promo promo = new Promo();
            new Merchant(Constants.MerchantType.NATIVE_PROMO_HYBRID.getId(), true).getPromos().add(promo);
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(NATIVE_PROMO_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        cashierPage.tabNetBanking().click();
        cashierPage.dropdownNB().selectByValue("ICICI");
        String promoTxt=cashierPage.applyPromoText().getText();
        Assert.assertNotNull(promoTxt);
        Assertions.assertThat(promoTxt).contains("cashback applicable");
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify promo text is visible on UI While Doing  Txn Via Postapid on redirection flow", groups = "p0")
    public void Successfull_Promo_Postpaid_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        for (int i = 0; i < 1; i++) {
            Promo promo = new Promo();
            new Merchant(Constants.MerchantType.NATIVE_PROMO_HYBRID.getId(), true).getPromos().add(promo);
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(NATIVE_PROMO_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (!cashierPage.isPaymodeModeSelected(Constants.PayMode.PAYTM_DIGITAL_CARD)) {
            cashierPage.radioButtonPaytmPostpaid().click();
        }
        String promoTxt=cashierPage.applyPromoText().getText();
        Assert.assertNotNull(promoTxt);
        Assertions.assertThat(promoTxt).contains("cashback applicable");
        cashierPage.buttonPostPaidPayNow().waitUntilClickable();
        cashierPage.buttonPostPaidPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    // Retry cases
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify failed CC Txn after 3 Retry ", groups = "p0")
    public void Validate_Failed_CC_Retry_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(PGOnly_Retry, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validatePaymentMode("CC")
                .validateRespCode("750")
                .validateRespMsg(Constants.MessageAssert.WRONG_OTP.toString())
                .validateStatus("TXN_FAILURE");

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify Successfull CC Txn after 1 Retry ", groups = "p0")
    public void Validate_Successfull_CC_Retry_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(PGOnly_Retry, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC,paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
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
                .assertAll();

    }
    @Owner("Nirottam")
    @Parameters({"theme"})
    @Test(description = "To verify Successfull CC Txn With 0 Retry and With Retry configured merchant", groups = "p0")
    public void Validate_Successfull_CC_Zero_Retry_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(PGOnly_Retry, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("1")
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
                .assertAll();

    }

    @Owner(VAIBHAV)
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user in redirection flow")
    public void validateWallet2FACorrectPasscode_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(ADD_MONEY_SURCHARGE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("6500")
                .build();
        WalletHelpers.modifyBalance(user,6500.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner(VAIBHAV)
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user and txn amount equal to 5k in redirection flow")
    public void validateWallet2FAWalletBalEqualTo5k_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(ADD_MONEY_SURCHARGE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("5000")
                .build();
        WalletHelpers.modifyBalance(user,5000.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(VAIBHAV)
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet while passing incorrect passcode when passcode is already set on user in redirection flow")
    public void validateWallet2FAIncorrectPasscode_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(ADD_MONEY_SURCHARGE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("5000")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPasscode("3456");
        WalletHelpers.modifyBalance(user,5000.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE,paymentDTO);
    }


    @Owner(VAIBHAV)
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet while passing incomplete passcode when passcode is already set on user in redirection flow")
    public void validateWallet2FAIncompletePasscode_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(ADD_MONEY_SURCHARGE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("5000")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPasscode("12");
        WalletHelpers.modifyBalance(user,5000.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE,paymentDTO);
    }



    @Owner(VAIBHAV)
    @Feature("PGP-53400")
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user in redirection flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FACorrectPasscodeAmountGreaterThan5kAfterAddingPcfFee_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(TIP_AMOUNT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("4885")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }



    @Owner(VAIBHAV)
    @Feature("PGP-53400")
    @Parameters({"theme"})
    @Test(description = "To verify wallet 2FA transaction using wallet while passing incorrect passcode and passcode is already set on user in redirection flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FAIncorrectPasscodeAmountGreaterThan5kAfterAddingPcfFee_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(TIP_AMOUNT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("4885")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPasscode("3456");
        WalletHelpers.modifyBalance(user,5000.29);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE,paymentDTO);
    }



    @Owner(VAIBHAV)
    @Feature("PGP-53400")
    @Parameters({"theme"})
    @Test(description = "To verify wallet 2FA transaction using wallet while passing incomplete passcode and passcode is already set on user in redirection flow and amount is greater than 5k after adding pcf fee")
    public void validateWallet2FAIncompletePasscodeAmountGreaterThan5kAfterAddingPcfFee_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(TIP_AMOUNT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("4885")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPasscode("12");
        WalletHelpers.modifyBalance(user,5000.29);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE,paymentDTO);
    }



    @Owner(VAIBHAV)
    @Feature("PGP-53400")
    @Parameters({"theme"})
    @Test(description = "To verify successful wallet 2FA transaction using wallet when passcode is already set on user in redirection flow and amount is equal to 5k after adding pcf fee")
    public void validateWallet2FAWalletBalEqualTo5kAfterAddingPcfFee_Redirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.WALLETBALANCE);
        OrderDTO orderDTO = new OrderFactory.PGOnly(TIP_AMOUNT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("4884.75")
                .build();
        WalletHelpers.modifyBalance(user,5000.29);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET_PASSCODE);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify Card details page open when single paymode EMI is Present")
    public void verifySinglePaymodeEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = PG2_JS_Checkout_Paytm_Domain;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "EMI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("100")
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().assertNotVisible();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().assertVisible();
    }
    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify Card details page open when single paymode CREDIT_CARD is Present")
    public void verifySinglePaymodeCC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = CHECKOUT_ON_REDIRECTION;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "CREDIT_CARD", new String[]{"HDFC", "ICICI"});
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertNotVisible();
        DriverManager.getDriver().switchTo().frame(cashierPage.cc_dc_iframe());
        cashierPage.textBoxCardNumber().assertVisible();
    }
    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify Card details page open when single paymode DEBIT_CARD is Present")
    public void verifySinglePaymodeDC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = CHECKOUT_ON_REDIRECTION;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(null, "DEBIT_CARD", new String[]{"HDFC", "ICICI"});
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().assertNotVisible();
        DriverManager.getDriver().switchTo().frame(cashierPage.cc_dc_iframe());
        cashierPage.textBoxCardNumber().assertVisible();
    }
    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify NB details page open when single paymode NET BANKING is Present")
    public void verifySinglePaymodeNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = PG2_JS_Checkout_Paytm_Domain;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"HDFC", "ICICI"}, "NET_BANKING");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        PaymentDTO paymentDTO =new PaymentDTO().setBankName("ICICI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().assertNotVisible();
        cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS");
    }
    @Owner(PUSPA)
    @Feature("PGP-49206")
    @Parameters({"theme"})
    @Test(description = "Verify cashier page render same way when qr details is present")
    public void verifySinglePaymodeUPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = PG2_JS_Checkout_Paytm_Domain;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
    }
}
