package scripts.Irctc;

import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.TxnStatus;
import com.paytm.apphelpers.IrctcHelper;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.Date;
import java.util.HashMap;

import static com.paytm.appconstants.Constants.MerchantType.IRCTC_Country_Code;
import static com.paytm.appconstants.Constants.MerchantType.Irctc_binIrcId;
import static com.paytm.appconstants.Constants.Owner.ROHIT_SHARMA;

public class Irctc extends PGPBaseTest {
    private static CheckoutPage checkoutPage = new CheckoutPage();
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-40449")
    @Parameters({"theme"})
    @Test(description = "Verify Country_Code india should be present in txn_status api response & callback response on IRCTC mid when SEND_COUNTRY_CODE_PARAM_ENABLED:Y & ENCPARAMS_ENABLED:Y on mid for Dc txn")
    public void irctcDcTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(IRCTC_Country_Code, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateCountryCode("india")
                .AssertAll();
        HashMap<String, String> t;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                .replace(" ", "+");
        t = IrctcHelper.getDecryptedResponse(encryptedResponse, orderDTO.getMerchantKey());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
        softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("HDFC");
        softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
        softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
        softly.assertThat(t.get("TXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
        softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNDATE")).isNotEmpty();
        softly.assertThat(t.get("COUNTRY_CODE")).isEqualTo("india");
        softly.assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-40449")
    @Parameters({"theme"})
    @Test(description = "Verify Country_Code india should be present in txn_status api response & callback response on IRCTC mid when SEND_COUNTRY_CODE_PARAM_ENABLED:Y & ENCPARAMS_ENABLED:Y on mid for Cc txn")
    public void irctcCcTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(IRCTC_Country_Code, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("HDFC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("HDFC Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateCountryCode("india")
                .AssertAll();
        HashMap<String, String> t;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                .replace(" ", "+");
        t = IrctcHelper.getDecryptedResponse(encryptedResponse, orderDTO.getMerchantKey());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
        softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
        softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("HDFC");
        softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
        softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
        softly.assertThat(t.get("TXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
        softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNDATE")).isNotEmpty();
        softly.assertThat(t.get("COUNTRY_CODE")).isEqualTo("india");
        softly.assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-40449")
    @Parameters({"theme"})
    @Test(description = "Verify Country_Code india should be present in txn_status api response & callback response on IRCTC mid when SEND_COUNTRY_CODE_PARAM_ENABLED:Y & ENCPARAMS_ENABLED:Y on mid for NB txn")
    public void irctcNbTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(IRCTC_Country_Code, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("ICICI Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateCountryCode("india")
                .AssertAll();
        HashMap<String, String> t;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                .replace(" ", "+");
        t = IrctcHelper.getDecryptedResponse(encryptedResponse, orderDTO.getMerchantKey());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
        softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
        softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("ICICI");
        softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
        softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
        softly.assertThat(t.get("TXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
        softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNDATE")).isNotEmpty();
        softly.assertThat(t.get("COUNTRY_CODE")).isEqualTo("india");
        softly.assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-40449")
    @Parameters({"theme"})
    @Test(description = "Verify Country_Code india should be present in txn_status api response & callback response on IRCTC mid when SEND_COUNTRY_CODE_PARAM_ENABLED:Y & ENCPARAMS_ENABLED:Y on mid for UPI txn")
    public void irctcUPITxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(IRCTC_Country_Code, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("ICICI")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateCountryCode("india")
                .AssertAll();
        HashMap<String, String> t;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                .replace(" ", "+");
        t = IrctcHelper.getDecryptedResponse(encryptedResponse, orderDTO.getMerchantKey());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
        softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
        softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("ICICI");
        softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
        softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
        softly.assertThat(t.get("TXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
        softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNDATE")).isNotEmpty();
        softly.assertThat(t.get("COUNTRY_CODE")).isEqualTo("india");
        softly.assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-40449")
    @Parameters({"theme"})
    @Test(description = "Verify Country_Code india should be present in txn_status api response & callback response on IRCTC mid when SEND_COUNTRY_CODE_PARAM_ENABLED:Y & ENCPARAMS_ENABLED:Y on mid for Wallet txn")
    public void irctcWalletTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.AUTOLOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(IRCTC_Country_Code, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 10.0);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateBankName("WALLET")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateCountryCode("india")
                .AssertAll();
        HashMap<String, String> t;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateENC_DATA(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String encryptedResponse = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ENC_DATA)
                .replace(" ", "+");
        t = IrctcHelper.getDecryptedResponse(encryptedResponse, orderDTO.getMerchantKey());
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(t.get("CURRENCY")).isEqualTo("INR");
        softly.assertThat(t.get("CUST_ID")).isEqualTo(orderDTO.getCUST_ID());
        softly.assertThat(t.get("GATEWAYNAME")).isEqualTo("WALLET");
        softly.assertThat(t.get("RESPMSG")).isEqualTo("Txn Success");
        softly.assertThat(t.get("MID")).isEqualTo(orderDTO.getMID());
        softly.assertThat(t.get("RESPCODE")).isEqualTo("01");
        softly.assertThat(t.get("TXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNAMOUNT")).isEqualTo(orderDTO.getTXN_AMOUNT());
        softly.assertThat(t.get("ORDERID")).isEqualTo(orderDTO.getORDER_ID());
        softly.assertThat(t.get("STATUS")).isEqualTo("TXN_SUCCESS");
        softly.assertThat(t.get("BANKTXNID")).isNotEmpty();
        softly.assertThat(t.get("TXNDATE")).isNotEmpty();
        softly.assertThat(t.get("COUNTRY_CODE")).isEqualTo("india");
        softly.assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-42151")
    @Parameters({"theme"})
    @Test(description = "Verify binIrcId=SBIVB003 should be present in txn_status api response when ENABLE_BIN_IDENTIFIER_IN_RESPONSE:Y on mid")
    public void irctcBinTxnSBIVB003(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Irctc_binIrcId, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setCreditCardNumber("4317572940602285");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .validatebinIrcId("IRCTC001")
                .AssertAll();

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-42151")
    @Parameters({"theme"})
    @Test(description = "Verify binIrcId=SBIVP001  should be present in txn_status api response when ENABLE_BIN_IDENTIFIER_IN_RESPONSE:Y on mid")
    public void irctcBinTxnSBIVP001(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Irctc_binIrcId, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO().setDebitCardNumber("4356168529006881");
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateMid(orderDTO.getMID())
                .validateTxnDate(new Date())
                .validatebinIrcId("IRCTC001")
                .AssertAll();
    }

}
