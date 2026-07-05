package scripts;

import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.LanguageTranslationAPI.Language;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.nativeAPI.*;
import com.paytm.api.notification.arnNotify;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.*;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.base.test.UserManager;
import com.paytm.dto.*;
import com.paytm.dto.FastForwardApp.request.FastForwardAppRequest;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedSubvention;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.dto.upiIntent.staticQR.Response.StaticQrUpiPSPResponse;
import com.paytm.dto.upiIntent.staticQR.StaticQrUpiPSPRequest;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import com.paytm.pages.responsePage.ResponsePage.Attribute;
import com.paytm.pg.merchant.CheckSumServiceHelper;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.util.AuthUtil;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import scripts.api.savecardService.SaveCard;
import scripts.api.theia.FetchQRPaymentDetailsAPITest;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;
import static com.paytm.base.test.Group.Status;
import static com.paytm.base.test.Group.Theme;
import static com.paytm.dto.PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER;
import static com.paytm.dto.PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN;
import static org.hamcrest.Matchers.equalTo;

@Owner("Deepak")
public class PGOnly extends PGPBaseTest implements InternationalSavedCard {

    private static final String BAJAJ_FINSERV_BANK_NAME = "BAJAJFN";
    private static final String BAJAJ_FINSERV_GATEWAY_NAME = "BAJAJFN";
    private static final String BAJAJ_FINSERV_BANK_EMI = Constants.BAJAJ_FINSERV_BANK_EMI;
    private static final String VALID_OTP = "123456";
    //    private static final String theme = "enhancedweb";
    private static final String PLEASE_ENTER_YOUR_CARD_NUMBER = "Please enter your card Number";
    private static final String ENTER_EXPIRY_DATE = "Enter Expiry Date";
    private static final String ENTER_CVV = "Enter CVV";
    private static final String PLEASE_SELECT_A_BANK_TO_PROCEED = "Please select a bank to proceed";
    private static final String NOT_ENOUGH_BALANCE = "You do not have enough balance for this payment";
    private static final String PLEASE_ENTER_UPI_ID = "Please enter UPI ID";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final BajajFinservBankPage bajajFinservBankPage = new BajajFinservBankPage();
    private static final String JSON_POST_URL = "/checkoutpage/nplus_page.jsp?ttype=hold&jsonresp=";
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private static final String LOCALE_HINDI = "hi-IN";
    private static final String RESP_CODE = "RESPCODE";
    private static final String SUCCESS_CODE = "01";
    private static final MerchantType MERCHANT_HAVING_LOCALISATION_ENABLED = MerchantType.NEWHYBRID;
    RedisHelper redisHelper= RedisHelper.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI,LocalConfig.PG_REDIS_CLUSTER_PASS);

    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);

    }

    private String getColorCode(String color) {
        String[] hexValue = color.replace("rgba", "").replace(")", "").replace("(", "").split(",");
        hexValue[0] = hexValue[0].trim();
        int hexValue1 = Integer.parseInt(hexValue[0]);
        hexValue[1] = hexValue[1].trim();
        int hexValue2 = Integer.parseInt(hexValue[1]);
        hexValue[2] = hexValue[2].trim();
        int hexValue3 = Integer.parseInt(hexValue[2]);
        return String.format("#%02x%02x%02x", hexValue1, hexValue2, hexValue3);
    }

    private UIElement getElementByXpath(String xpath, String theme) {
        return new UIElement(By.xpath(xpath), theme, xpath);
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful base64 encoding for app data")
    public void checkBase64EncodingAppData(@Optional("enhancedweb") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setORDER_ID("/\"\\//" + CommonHelpers.generateOrderId())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Invalid Order ID").assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PGOnly transaction via CC.", groups = "smoke")
    public void PGP_170_successfulPGOnlyCC(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(NATIVE_HYBRID, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(NATIVE_HYBRID.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate RRN code and AUTH code should come in merchant status response for successful CC only Txn")
    public void PGP_24614_ValidateRRNandAuthCodeforsuccessfulPGOnlyCCtxn(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO(new GetPaymentStatusDTO.Builder(orderDTO.getORDER_ID(), MerchantType.PGOnly));
        GetPaymentStatus getPaymentStatusapi = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = getPaymentStatusapi.execute();
        String txndateWithSpace = response.jsonPath().getString("body.txnDate");
        String txndateWithoutSpace = response.jsonPath().getString("body.txnDate").replace(" ", "");
        String jsonBodyString = response.getBody().asString();
        jsonBodyString = jsonBodyString.replace(" ", "").replace("HDFCBank", "HDFC Bank")
                .replace("TxnSuccess", "Txn Success")
                .replace(txndateWithoutSpace, txndateWithSpace)
                .replace("\n", "");
        int bodyindex = jsonBodyString.indexOf("body");
        jsonBodyString = jsonBodyString.substring(bodyindex + 6, jsonBodyString.length() - 1);
        String rrnCode = response.jsonPath().getString("body.rrnCode");
        String authCode = response.jsonPath().getString("body.authCode");
        String checksum = response.jsonPath().getString("head.signature");
        Boolean isChecksumValid = CheckSumServiceHelper.getCheckSumServiceHelper().verifycheckSum(MerchantType.PGOnly.getKey(), jsonBodyString, checksum);
        Assertions.assertThat(isChecksumValid).isTrue();

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getRRN(), rrnCode);
        softAssert.assertEquals(peonResponse.getAUTHCODE(), authCode);
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate RRN code and AUTH code should come in merchant status response for failed CC only Txn")
    public void PGP_24614_ValidateRRNandAuthCodeforfailurePGOnlyCCtxn(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO(new GetPaymentStatusDTO.Builder(orderDTO.getORDER_ID(), MerchantType.PGOnly));
        GetPaymentStatus getPaymentStatusapi = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = getPaymentStatusapi.execute();
        String txndateWithSpace = response.jsonPath().getString("body.txnDate");
        String txndateWithoutSpace = response.jsonPath().getString("body.txnDate").replace(" ", "");
        String jsonBodyString = response.getBody().asString();
        jsonBodyString = jsonBodyString.replace(" ", "").replace("HDFCBank", "HDFC Bank")
                .replace("Yourpaymenthasbeendeclinedbyyourbank.Pleasecontactyourbankforanyqueries.Ifmoneyhasbeendeductedfromyouraccount,yourbankwillinformuswithin48hrsandwewillrefundthesame", "Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .replace(txndateWithoutSpace, txndateWithSpace)
                .replace("\n", "");
        int bodyindex = jsonBodyString.indexOf("body");
        jsonBodyString = jsonBodyString.substring(bodyindex + 6, jsonBodyString.length() - 1);
        String rrnCode = response.jsonPath().getString("body.rrnCode");
        String authCode = response.jsonPath().getString("body.authCode");
        String checksum = response.jsonPath().getString("head.signature");
        Boolean isChecksumValid = CheckSumServiceHelper.getCheckSumServiceHelper().verifycheckSum(MerchantType.PGOnly.getKey(), jsonBodyString, checksum);
        Assertions.assertThat(isChecksumValid).isTrue();


        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getRRN(), rrnCode);
        softAssert.assertEquals(peonResponse.getAUTHCODE(), authCode);
        softAssert.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode(NB) already selected when mid is configured on ff4j")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedNBTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI Bank"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode is not already selected when mid other than configured mid on ff4j is used")
    public void PGP_23164_verifyLastTxnPaymodeNotSelectedwhenDifferentMidUsedNBTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        //WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.NETBANK_PCF, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
       // WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode(CC) already selected when mid is configured on ff4j")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedCCTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode(CC) is not already selected when mid other than configured mid on ff4j is used")
    public void PGP_23164_verifyLastTxnPaymodeNotSelectedwhenDifferentMidUsedCCTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.NATIVE_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.NATIVE_HYBRID.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.NATIVE_HYBRID, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode(DC) already selected when mid is configured on ff4j")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedDCTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabDebitCard().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode(UPI) already selected when mid is configured on ff4j")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedUPITxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabUPI().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode(PPI) already selected when mid is configured on ff4j")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedPPITxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, 10.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.checkBoxPPI().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode and wallet are already selected when mid is configured on ff4j in Add N Pay txn")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedAddNPayTxn(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
        Assertions.assertThat(cashierPage.checkBoxPPI().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode and wallet are already selected when mid is configured on ff4j in Hybrid txn")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedHybridTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.Hybrid.getKey())
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
        Assertions.assertThat(cashierPage.checkBoxPPI().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Payment Mode(PPBL) is already selected by default in case of Add Money Txn")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedAddMoneyPPBLTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateBankName("PPBL")
                .assertAll();
        orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.checkboxPPBL().isChecked()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify When all three txns are diff - DC,CC,PPBL (last is PPBL), PPBL will be selected by default for next txn")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedAfterThreeDifferentTxnsDC_CC_PPBL(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.CC);
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.PPBL);
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("NB")
                .validateGatewayName("PPBL")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.checkboxPPBL().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify When first txn is diff and last two are same - NB,CC,CC , CC will be selected by default for next txn")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedAfterThreeDifferentTxnswithNb_CC(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI Bank"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.CC);
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.CC);
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the retry case that when the NB is the last paymode stored in local storage and txn done with HDFC CC card and failed then after landing the cashier page , NB should not be selected ")
    public void PGP_23164_verifyLastTxnPaymodeAlreadySelectedAfterTxnswithNb_FailedCC(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI Bank"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.CC);
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode is not already selected in case of Failed previous txn")
    public void PGP_23164_verifyLastTxnPaymodeNotAlreadySelectedAfterFailedTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode is not already selected in case of Pending previous txn")
    public void PGP_23164_verifyLastTxnPaymodeNotAlreadySelectedAfterPendingTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isFalse();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate successful EMI Transaction via EMI Only merchant")
    public void PGP_27715_validateSuccessTxnUsingEMIOnlyMerchant(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMIOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank");
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate successful EMI Transaction via EMI DC Only merchant without Corporate")
    public void PGP_27715_validateSuccessTxnUsingEMI_DC_OnlyMerchantWithoutCorporate(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.EMIDC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMIOnly_DC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER)
                .setBankName("ICICI Bank").setMonth(3);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI_DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICIE.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("ICICI Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate successful EMI Transaction via EMI DC Only merchant with Corporate")
    public void PGP_27715_validateSuccessTxnUsingEMI_DC_OnlyMerchantCorporate(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.EMIDC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMIOnly_DC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI Bank").setEmiCard(PaymentDTO.ICICI_CORPORATE_DEBIT_CARD_NUMBER);
        cashierPage.payBy(PayMode.EMI_DC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICIE.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("ICICI Bank")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Bank retry count equals to five in enhancedweb flow")
    public void ValidatePaymentsRetryCountEqualsFive(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.84")
                .build();
        for (int i = 0; i < 5; i++) {
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(PayMode.CC);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("PENDING")
                    .assertAll();
        }
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Parameters({"theme", "orderId"})
    @Test(description = "Validate successful PGOnly transaction via CC.", groups = "smoke")
    public void PGP_170_successfulPGOnlyBackwardCmpatibilityCC(@Optional("enhancedwap") String theme, @Optional("T2-theia") String orderId) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setORDER_ID(orderId + CommonHelpers.generateOrderId()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Failure on Risk Reject via CC.", groups = "smoke")
    public void RiskRejectPGOnlyCC(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("1.88").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        for (int i = 0; i < 6; i++) {
            cashierPage.payBy(PayMode.CC);
        }
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(ValidationType.NON_EMPTY)
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg("Retry count breached")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Txn Failure on CC transaction cancellation at 3D secure page.")
    public void PGP_171_CCCancelOn3DPage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    //    @Parameters({"theme"})
//    @Test(description = "Validate Txn Failure for Credit Card Cancel on Cashier Page", enabled = false)
    public void PGP_172_CCCancelCashierPage(@Optional("merchant") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        cashierPage.linkPGCancel().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(ValidationType.EMPTY)
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateBankName(ValidationType.EMPTY)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(ValidationType.EMPTY)
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    //    @Parameters({"theme"})
//    @Test(description = "Validate Txn Failure on Debit Card Cancel on Cashier Page", enabled = false)
    public void PGP_173_DCCancelCashierPage(@Optional("merchant") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabDebitCard().click();
        cashierPage.linkPGCancel().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName("")
                .validateRespCode(ResponseCode.TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.TXN_FAILURE.getRespMsg())
                .validateBankName("")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate successful transaction via Net Banking")
    public void PGP_174_successfulNBTxn(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(PayMode.NB, paymentDTO);
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
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Epic(Sprint.SPRINT31_1)
    @Feature("PGP-19896")
    @Owner("Tarun")
    @Parameters({"theme"})
    @Test(description = "Validate that transaction should be successful when card series is not present in bin")
    public void PGP_175_cardNotInBinCheckTxnAllowed(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setExtendInfo("{ \"udf1\": \"\", \"udf2\": \"\", \"udf3\": \"\", \"mercUnqRef\": \"\", \"comments\": \"\", \"orderAdditionalInfo\": { \"mid\": \"mid123\", \"mName\": \"merchantName\", \"mLogo\": \"merchantLogoUrl\", \"orderId\": \"order123\" } }")
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("5575118148989993");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("HDFC")
                .validateMid(orderDTO.getMID())
               // .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
               // .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using DC")
    public void PGP_239_successfulPGOnlyDC(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
              //  .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
              //  .validateStatusAPIParameters()
                .AssertAll();
    }

    @Issue("PGP-14988")
    @Parameters({"theme"})
    @Test(description = "To verify Failed PG txn when merchant licence expires", groups = Status.BUG)
    public void PGP_246_failedPGOnlyTxnWithLicenceExpiredDate(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.LICENCE_EXPIRED_MERCHANT, theme).build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        if ("merchant4".equalsIgnoreCase(theme)) {
            LostInSpacePage lostInSpacePage = new LostInSpacePage();
            lostInSpacePage.imgLostInSpace().assertVisible();
        } else {
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_FAILURE")
                    .validateRespCode("501")
                    .validateRespMsg("System Error.")
                    .assertAll();
        }
    }


    @Parameters({"theme"})
    @Test(description = "To verify payment modes appearing for merchant")
    public void PGP_237_verifyPaymentModeOnCashierPage(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabNetBanking().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "To verify Txn Failure on DC transaction cancellation at 3D secure page")
    public void PGP_248_DCCancelOn3DPage(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                //.validatePaymentMode("DC")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify Txn Failure on saved card cancellation at 3D secure page")
    public void PGP_249_savedCardCancelOn3DPage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        String custId = CommonHelpers.generateOrderId();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCardOnMidCustId(MerchantType.PGOnly, custId, paymentDTO.getExpMonth(), PaymentDTO.Tokenization_Year, paymentDTO.getCreditCardNumber());
        //WalletHelpers.modifyBalance(user, 0.0);
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.98")
                .setCUST_ID(custId)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC_ONLY.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using AMEX card")
    public void PGP_243_successfulPGOnlyTxnUsingAMEXCard(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("378282246310005");
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.pause(1);
        cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "4");
    }

    @Parameters({"theme"})
    @Test(description = "To verify saved card list appearing for merchant after login")
    public void PGP_251_verifySavedCardListAfterLogin(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String card1 = "4109139965359183";
        String card2 = "5507032420388415";
        SavedCardHelpers.addCard(user, "06", "2022", card1);
        SavedCardHelpers.addCard(user, "07", "2023", card2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.clickSavedCardTab();
        cashierPage.verifyCardDisplayed(card1, card2);
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PGOnly Retry transaction via CC.")
    public void PGP_252_verifyPGOnlyTxnWithRetryCountConfigured(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(COFT_MERCHANT, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        //// This case will fail on enhanced UI as response which comes after failure is old UI not enhanced
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();

        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme", "orderId"})
    @Test(description = "Validate successful PGOnly Retry transaction via CC.")
    public void PGP_252_verifyPGOnlyTxnWithRetryBackwardCompatibilityCountConfigured(@Optional("enhancedweb") String theme, @Optional("T1-Retry-theia") String orderId) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).setTXN_AMOUNT("1.00")
                .setORDER_ID(orderId + CommonHelpers.generateOrderId()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();

        if (theme.equalsIgnoreCase("enhancedweb")) {
            Assertions.assertThat(cashierPage.notificationBar().getText()).as(" OTP Error message incorrect")
                    .containsIgnoringCase("Looks like OTP entered was incorrect. Please try again.");
            cashierPage.notificationBarOK().click();
        }

        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        if (theme.equalsIgnoreCase("enhancedweb")) {
            Assertions.assertThat(cashierPage.notificationBar().getText()).as(" OTP Error message incorrect")
                    .containsIgnoringCase("Looks like OTP entered was incorrect. Please try again.");
            cashierPage.notificationBarOK().click();
        }

        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify number of transaction retry on the basis of retrial counts configured for merchant")
    public void PGP_253_verifyPGOnlyTxnWithMaxRetryCount(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme)
                .setTXN_AMOUNT("99.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(PayMode.CC);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.tabCreditCard().isDisplayed();
    }

    @Test(description = "Verify Invalid OTP Limit and Verify if limit remains same after a failed (dc) txn " +
            "and txn redirected back to cashier page")
    public void PGP_20899_verifyInvalidOTPLimit(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.textBoxPhoneNumber().clearAndType(user.mobNo());
        if (theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            cashierPage.buttonSecureSignIn().click();
            for (int i = 0; i < 5; i++) {
                //Entering Invalid OTP
                cashierPage.fillLoginOtp("808080");
                cashierPage.otpVerifyButton().click();
                cashierPage.waitUntilLoads();
            }

            //Entering Valid OTP after retry count breached
            cashierPage.fillLoginOtp(PaymentDTO.OTP);
            cashierPage.otpVerifyButton().click();
            cashierPage.waitUntilLoads();
            cashierPage.otpLimitReachedMsg().assertVisible();
            cashierPage.loginStrip().assertDisabled();

            //Checking Limit Remains same after a failed DC Txn
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setDebitCardNumber(PaymentDTO.DC_FAILED_TXN);
            cashierPage.payBy(PayMode.DC, paymentDTO);
            cashierPage.waitUntilLoads();
            cashierPage.loginStrip().waitUntilVisible();
            cashierPage.loginStrip().click();
            cashierPage.textBoxPhoneNumber().waitUntilVisible();
            cashierPage.textBoxPhoneNumber().clearAndType(user.mobNo());
            cashierPage.buttonSecureSignIn().click();
            cashierPage.fillLoginOtp("808080");
            cashierPage.otpVerifyButton().click();
            cashierPage.otpLimitReachedMsg().assertVisible();
            cashierPage.loginStrip().assertDisabled();
        } else {
            cashierPage.loginProceedButton().click();
            for (int i = 0; i < 5; i++) {
                //Entering Invalid OTP
                cashierPage.waitUntilContainsText("OTP");
                cashierPage.fillLoginOtp("808080");
                cashierPage.otpVerifyButton().click();
                //cashierPage.waitUntilContainsText("SELECT AN OPTION TO PAY");
            }
            //Entering Valid OTP after retry count breached
            cashierPage.fillLoginOtp(PaymentDTO.OTP);
            cashierPage.otpVerifyButton().click();
            cashierPage.pause(2);
            cashierPage.otpLimitReachedMsg().assertVisible();
            cashierPage.loginStrip().assertDisabled();

            //Checking Limit Remains same after a failed DC Txn
            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setDebitCardNumber(PaymentDTO.DC_FAILED_TXN);
            cashierPage.payBy(PayMode.DC, paymentDTO);
            cashierPage.waitUntilLoads();
            cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
            cashierPage.waitUntilLoads();
            cashierPage.loginStrip().click();
            cashierPage.textBoxPhoneNumber().clearAndType(user.mobNo());
            cashierPage.loginProceedButton().click();
            cashierPage.pause(2);
            cashierPage.fillLoginOtp("808080");
            cashierPage.otpVerifyButton().click();
            cashierPage.pause(2);
            cashierPage.otpLimitReachedMsg().assertVisible();
            cashierPage.loginStrip().assertDisabled();
        }
    }

    @Parameters({"theme"})
    @Test(description = "To verify Txn Failure on cancellation of transaction when retry count reached for merchant")
    public void PGP_254_verifyPGOnlyTxnWithRetryCountBreached(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode())
                .validateRespMsg(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify payment modes appearing for merchant when user is logged in")
    public void PGP_259_verifyPaymentModesOnCashierPageWhenUserLogedIn(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertVisible();
        cashierPage.tabNetBanking().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using CC when user is logged in")
    public void PGP_260_successfulPGOnlyCCTxnWhenUserLoggedIn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using DC when user is logged in")
    public void PGP_261_successfulPGOnlyDCTxnWhenUserLoggedIn(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                //.validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
              //  .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using NB when user is logged in")
    public void PGP_262_successfulNBTxnWhenUserLoggedIn(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("1.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(PayMode.NB, paymentDTO);
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
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    //7000000001 issue occured when this number is used
    @Parameters({"theme"})
    @Test(description = "To verify card is getting saved on performing successful CC PGOnly Txn.")
    public void PGP_268_verifySaveCardThroughCCTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(COFT_MERCHANT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        orderDTO = new OrderFactory.PGOnly(COFT_MERCHANT, theme).setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.assertSavedCardVisibility();
    }

    @Parameters({"theme"})
    @Test(description = "To verify card is getting saved on performing successful DC PGOnly Txn.")
    public void PGP_269_verifySaveCardThroughDCTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        cashierPage.payBy(PayMode.DC);
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        SavedCardHelpers.validateSavedCardPresence(user);
        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.assertSavedCardVisibility();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful txn using CC saved card")
    public void PGP_270_verifyCCSavedCardTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");
        redisHelper.delete("FF4J_FEATURE_returnSavedCardsFromPlatformForUserId");
        User user = userManager.getForWrite(Label.BASIC);
        String custId = CommonHelpers.generateOrderId();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCardOnMidCustId(COFT_MERCHANT, custId, paymentDTO.getExpMonth(), PaymentDTO.Tokenization_Year,
                paymentDTO.getCreditCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(COFT_MERCHANT, theme)
                .setCUST_ID(custId)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC_ONLY.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful txn using DC saved card")
    public void PGP_271_verifyDCSavedCardTxn(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SavedCardHelpers.deleteSavedCard(user);
        String debitCardNumber = "4532421174341278";
        String debitCardIssuingBankName = "State Bank of India";
        cashierPage.payBy(PayMode.DC_WITH_SAVECARD, new PaymentDTO().setDebitCardNumber(debitCardNumber));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(debitCardIssuingBankName)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.payBy(PayMode.SAVED_CARD);
        responsePage.waitUntilLoads();
        txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("SBI")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify simple PG txn using AMEX card when user is logged in")
    public void PGP_265_successfulPGOnlyTxnUsingAMEXCardWhenUserLoggedIn(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("378282246310005");
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.pause(1);
        cashierPage.textBoxCVVNumber().assertAttribute("maxlength", "4");
    }

    @Parameters({"theme"})
    @Test(description = "Verify the error message when invalid card number is passed")
    public void PGP_279_verifyInvalidCardNumberForPGOnlyTxn(@Optional("enhancedwap") String theme) throws NoSuchFieldException, IllegalAccessException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("7111111111111111");
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText())
                .containsIgnoringCase(MessageAssert.INVALID_CARD_NUMBER.toString());
    }


    @Parameters({"theme"})
    @Test(description = "Verify the Txn failure when invalid CVV number is passed")
    public void PGP_280_verifyInvalidCVVForPGOnlyTxn(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.97")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the Txn Failure when invalid OTP number is passed")
    public void PGP_281_verifyInvalidOTPForPGOnlyTxn(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.95")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                //.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespCode())
                .validateRespMsg(ResponseCode.FGW_OTP_VALIDATION_FAILED.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify the Txn Failure when invalid expiry date is passed")
    public void PGP_282_verifyInvalidExpiryDateForPGOnlyTxn(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.96")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.0")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify failure PG txn using UPI when invalid PIN is entered")
    public void failurePGOnlyUpi_InvalidPin(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.94")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse(ResponseCode.BANK_TXN_FAILURE.getRespCode(), ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify failure PG txn using UPI when user is logged in and invalid PIN ins entered")
    public void failurePGOnlyUpi_InvalidPin_loggedInUser(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("99.94")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .validateRespCode(ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .assertAll();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateFailureResponse(ResponseCode.BANK_TXN_FAILURE.getRespCode(), ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using UPI")
    public void PGP_244_successfulPGOnlyUPI(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
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
                .validateGatewayName(Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify successful PG txn using UPI when user is logged in")
    public void PGP_266_successfulPGOnlyUPITxnWhenUserLoggedIn(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).setSSO_TOKEN(user.ssoToken()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //cashierPage.login(user);
        cashierPage.payBy(PayMode.UPI);
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
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "To verify Display UI changes for SavedUPI paymode on cahierpage")
    public void VerifyUIChangesforSavedUPI(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PPBLC_ONLY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String banknames = "";
        for (int i = 1; i <= cashierPage.savedUpiListSize(); i++) {
            cashierPage.tabSavedUPI(i).click();
            //Verify that "- UPI" is not visible after bankname
            String savedUPItabtext = cashierPage.tabSavedUPI(i).getText();
            Assertions.assertThat(savedUPItabtext).doesNotContain("- UPI");
            //Verify Bankname
            banknames = banknames + savedUPItabtext.split("\n")[0];
        }
        Assertions.assertThat(banknames).contains("Mypsp2");
        //Verify UPI logo is present
        cashierPage.getUPILogo().assertVisible();
    }

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "To verify Successfull SavedUPI paymode Txn")
    public void VerifySuccessfullSavedUPITxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PPBLC_ONLY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_UPI);
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
                .validateGatewayName(Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateVPA("7259493013@paytm")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "To verify Successfull SavedUPI paymode Txn in ADD N PAY Flow")
    public void VerifySuccessfullSavedUPITxnADDNPAY(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.AddnPay(AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        String bankname;
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_UPI);
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
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateBankName("WALLET")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner("Jai")
    @Parameters({"theme"})
    @Test(description = "To verify Successfull SavedUPI paymode Txn in HYBRID Flow")
    public void VerifySuccessfullSavedUPITxnHYBRID(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())

                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.Hybrid.getKey())
                .validateChildTxnsPresent()
                .validateResponsePageParameters()
                .assertAll();
    }

    //    @Parameters({"theme"})
//    @Test(description = "To verify VPA is getting saved for successful PG only transaction performed using UPI", enabled = false)
    public void PGP_275_validateSaveUPIForHybridTxn(@Optional("enhancedwap") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        User user = userManager.getForRead(Label.BASIC);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.UPI);
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
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.login(user);
        cashierPage.tabSavedCard().assertVisible();
    }

    //    @Parameters({"theme"})
//    @Test(description = "To verify Successful PG txn using saved VPA", enabled = false)
    public void PGP_276_validatePGTxnUsingSavedUPI(@Optional("merchant") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.UPI);
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
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        // Txn Using Saved UPI
        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.modifyBalance(user, 0.00);
        checkoutPage.createOrder(orderDTO);
        cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);
        cashierPage.payBy(PayMode.SAVED_UPI);
        responsePage.waitUntilLoads();
        txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Verify Pay Using UPI Apps Paymode displayed with UPI Intent Enabled and ff4j configured for mid")
    public void ValidateUPIAppsPaymodeDisplayedwithUPIIntentEnabledMidConfiguredSSOTokenLogin(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI_INTENTONLY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPIIntent().assertVisible(); //Verify Pay using upi apps paymode is visible
        UIElement knowMoreOption = new UIElement(By.xpath("//a[contains(.,'Know More')]"), cashierPage.getPageName(), "know-more-option");
        knowMoreOption.assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Pay Using UPI Apps Paymode displayed with UPI Intent Enabled and ff4j configured for mid")
    public void ValidateUPIAppsPaymodeDisplayedwithUPIIntentEnabledMidConfiguredCashierpageLogin(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI_INTENTONLY, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPIIntent().assertVisible(); //Verify Pay using upi apps paymode is visible
        UIElement knowMoreOption = new UIElement(By.xpath("//a[contains(.,'Know More')]"), cashierPage.getPageName(), "know-more-option");
        knowMoreOption.assertVisible();
        cashierPage.logout(user);
        cashierPage.tabUPIIntent().assertVisible(); //Verify Pay using upi apps paymode is visible
    }

    @Parameters({"theme"})
    @Test(description = "Verify Pay Using UPI Apps Paymode is not displayed with UPI Intent Not Enabled and ff4j configured for mid")
    public void ValidateUPIAppsPaymodeNotDisplayedWhenUPIIntentNotEnabledMidConfigured(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDFC_UPI_COLLECT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage("enhancedwap");
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPIIntent().assertNotVisible(); //Verify Pay using upi apps paymode is not visible
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateGatewayName("HDFC")
                .validateOrderId(orderDTO.getORDER_ID())
                .validateMid(orderDTO.getMID())
                .validateRespCode("01")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify Pay Using UPI Apps Paymode displayed with UPI Intent Enabled and ff4j configured for mid")
    public void ValidateUPIAppsPaymodeNotDisplayedwithUPIIntentEnabledMidConfiguredSSOTokenLogin(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI_INTENTONLY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.tabUPIIntent().assertVisible(); //Verify Pay using upi apps paymode is visible
        UIElement knowMoreOption = new UIElement(By.xpath("//a[contains(.,'Know More')]"), cashierPage.getPageName(), "know-more-option");
        knowMoreOption.assertVisible();
    }

    @Issue("PGP-12855")
    @Parameters({"theme"})
    @Test(description = "To verify card is getting saved on mid and custId without login.")
    public void PGP_verifySaveCardWithoutLogin(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC_WITH_SAVECARD);
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
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setCUST_ID(orderDTO.getCUST_ID())
                .build();
        checkoutPage.createOrder(orderDTO);
        cashierPage.assertSavedCardVisibility();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PGOnly Retry transaction via CC --> DC")
    public void verifyPGOnlyTxnWithRetryCountConfigured_CCtoDC(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4718650100030136");
        cashierPage.payBy(PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PGOnly Retry transaction via DC --> CC")
    public void verifyPGOnlyTxnWithRetryCountConfigured_DCtoCC(@Optional("enhancedwap") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).setTXN_AMOUNT("1.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber("4799475263852080");
        cashierPage.payBy(PayMode.DC, paymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PGOnly Retry transaction via NB --> CC")
    public void verifyPGOnlyTxnWithRetryCountConfigured_NBtoCC(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme)
                .setTXN_AMOUNT("1.00")
                .setORDER_ID("retry" + CommonHelpers.generateOrderId())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        cashierPage.waitUntilLoads();
        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify expired save card is not displaying on cashier page ")
    public void checkExpiredSaveCardNotVisible_OnCashierPage(@Optional("enhancedwap") String theme) throws Exception {
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCard saveCards = new SaveCard();
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        //save credit card on user
        String cardId = savedCardHelpers.saveCardUserId(user.custId(), saveCards.AesEncCardNumDebit, saveCards.AesEncExpDebit, paymentDTO.getCreditCardNumber()).getResponse().toString();
        savedCardHelpers.validateSaveCardDB_ByCardID(cardId);
        //change expiry
        savedCardHelpers.updateCardExpiry_withExpiredExpiry(cardId, user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        try {
            cashierPage.tabSavedCard().assertNotVisible();
        } catch (AssertionError e) {
            throw new AssertionError("Expired Save card should not be displayed on cashier page");
        }
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB})
    public void checkUserRedirectedToBankWhenPaymentInitiatedUsingAMEXCard(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.getAmexCardNumber()).setCvvNumber("1111");
        cashierPage.payBy(PayMode.CC, paymentDTO);
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
                .validateGatewayName("AMEX")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.AMEX.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    //    @Parameters({"theme"})
//    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
//            description = "Check payment using Bajaj Finserv card", enabled = false)
//Bajaj Finserv doesn't support normal payment as card. It only supports EMI txn
    public void checkPaymentUsingBajajFinservCard(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO()
                .setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER)
                .setBankName(BAJAJ_FINSERV_BANK_NAME)
                .setExpMonth("")
                .setExpYear("")
                .setMonth(6);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        bajajFinservBankPage.inputOtp(VALID_OTP);
        bajajFinservBankPage.clickSubmit();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(BAJAJ_FINSERV_GATEWAY_NAME)
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(BAJAJ_FINSERV_BANK_NAME)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Successful PCF transaction for NB ICICI")
    public void validateSucessfullPCFTransactionviaICICINB(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PPBL_NB_PCF, theme, user).
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


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Successful PCF transaction for PPBL")
    public void validateSucessfullPCFTransactionviaPPBL(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PPBL_NB_PCF, theme, user).
                setTXN_AMOUNT("10").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.tabPPBL().click();
        cashierPage.pause(3);
        Double actualBaseAmt = Double.valueOf(orderDTO.getTXN_AMOUNT());
        Double actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPG().getText()));
        Double actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(actualTotalAmt - actualBaseAmt)));
        Double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT())), 0, 9.00, "PPBL");

        SoftAssertions softAssert = new SoftAssertions();
        softAssert.assertThat(actualChargeFeeAmt).as("PPBL").isEqualTo(expectedChargeFeeAmt);
        softAssert.assertAll();
        cashierPage.payBy(PayMode.PPBL);
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

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Successful transaction for PPBL")
    public void validateSucessfullPPBLTransaction(@Optional("merchant4") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user).
                setTXN_AMOUNT("100").build();
        WalletHelpers.modifyBalance(user, 0.0);
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.pause(3);
        cashierPage.payBy(PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    //    @Parameters({"theme"})
//    @Test(description = "Validate PPBL retry Case default, enhanceNative and EnhancedWeb", enabled = false)
//TC doesn't make any sense as it already duplicated in other tc
    public void vailidatePPBLRetry(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user, 0.0);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PPBL_PAYTMCC_VPA, theme, user).setTXN_AMOUNT("60.1").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL);
        cashierPage.waitUntilLoads();
        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.checkboxPPBL().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate failed Trx with PPBL and Process it using CreditCard")
    public void vailidatePPBLRetryProcessTrxUsingCC(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user, 0.0);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid_Retry, theme, user).setTXN_AMOUNT("60.1").build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL);
        cashierPage.waitUntilLoads();
        if (theme.equals("enhancedwap")) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate Failed Trx with CC and Process it using PPBL")
    public void vailidatePPBLRetry_ProcessTrxUsingPPBL(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user, 0.0);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PPBL_PAYTMCC_VPA, theme, user).setTXN_AMOUNT("100")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO incorrectpaymentDTO = new PaymentDTO();
        incorrectpaymentDTO.setCreditCardNumber(CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(PayMode.CC, incorrectpaymentDTO);
        cashierPage.waitUntilLoads();
        cashierPage.modalRetryPayment().accept();
        cashierPage.payBy(PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validating error message after entering invalid passcode")
    public void validateInvalidPassCodeErrorMessage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.NOPPBL);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.PPBLYONLY, theme, user).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxPPBLPassCode().waitUntilEditable();
        cashierPage.textBoxPPBLPassCode().clearAndType("0000");
        cashierPage.buttonPpblSumbit().waitUntilClickable();
        cashierPage.buttonPpblSumbit().click();
        cashierPage.pause(1);
        Assertions.assertThat(cashierPage.ppblNotificationMsg().getText().trim()).isIn("Invalid credentials", "Incorrect Passcode", "Incorrect Password", "Enter correct passcode else your passcode will be disabled for 30 mins.");
    }

    @Issue("PGP-14930")
    @Parameters({"theme"})
    @Test(description = "Validating PPBL Txn with PCF enabled merchant")
    public void validatePCFForPPBL(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.POSTCONV_DEFAULT, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.PPBL);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.PPBL.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.PPBL.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Check card-index-no present in response when card-token-required param is passed as true")
    public void checkCardIndexNoPresentInResponseWhenCardTokenRequiredParamIsSetTrue(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setCardTokenRequired(true)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCardIndexNo(ValidationType.NON_EMPTY);
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Check paymodes available on cashier page which are passed by merchant")
    public void checkPaymodesAvailableOnCashierPageWhichArePassedByMerchant(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPAYMENT_MODE_ONLY("YES")
                .setAUTH_MODE("3D")
                .setPAYMENT_TYPE_ID("CC")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(groups = {Theme.MERCHANT4, Theme.ENHANCEDWAP, Theme.ENHANCEDWEB},
            description = "Check paymodes not available on cashier page which are passed as disabled by merchant")
    public void checkPaymodesNotAvailableOnCashierPageWhichArePassedAsDisabledByMerchant(@Optional("merchant4") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPAYMENT_MODE_ONLY("YES")
                .setAUTH_MODE("3D")
                .setPAYMENT_TYPE_ID("CC, DC")
                .setPAYMENT_MODE_DISABLE("DC")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().assertVisible();
        cashierPage.tabDebitCard().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PGOnly Retry Transaction via EMI")
    public void validatePGOnlyRetryTxnUsingEMI(@Optional("enhancedwap_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("HDFC Bank");
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        PaymentDTO incorrectPaymentDTO = new PaymentDTO()
                .setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN)
                .setEmiCard(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN)
                .setBankName("HDFC Bank")
                .setMonth(6);
        cashierPage.payBy(PayMode.EMI, incorrectPaymentDTO);
        cashierPage.waitUntilLoads();

        cashierPage.scrollTo(0);

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }

        cashierPage.payBy(PayMode.EMI, incorrectPaymentDTO);
        cashierPage.waitUntilLoads();

        cashierPage.scrollTo(0);

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }

        cashierPage.payBy(PayMode.EMI, paymentDTO);
        cashierPage.waitUntilLoads();


        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Validate successful PGOnly Retry Transaction via saved card")
    public void validatePGOnlyRetryTxnUsingSavedCard(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme)
                .setSSO_TOKEN(user.ssoToken()).build();
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentDTO incorrectPaymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        SavedCardHelpers.addCard(user, incorrectPaymentDTO.getExpMonth(), incorrectPaymentDTO.getExpYear(), incorrectPaymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(PayMode.SAVED_CARD, incorrectPaymentDTO, incorrectPaymentDTO.getCreditCardNumber());
        cashierPage.waitUntilLoads();

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }
        cashierPage.payBy(PayMode.SAVED_CARD, incorrectPaymentDTO, incorrectPaymentDTO.getCreditCardNumber());
        cashierPage.waitUntilLoads();

        if ("enhancedwap".equalsIgnoreCase(theme)) {
            cashierPage.modalRetryPayment().accept();
        }

        cashierPage.payBy(PayMode.SAVED_CARD, paymentDTO, paymentDTO.getCreditCardNumber());

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.HDFC_ONLY.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Parameters({"theme"})
    @Test(description = "To test card is not getting saved if first time payment failed by CC but retried with NB successfully")
    public void savedCardInfoNotGettingSavedPGP17483(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).setTXN_AMOUNT("1.00").
                setSSO_TOKEN(user.ssoToken()).build();
        String custID = orderDTO.getCUST_ID();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC_WITH_SAVECARD, paymentDTO);
        cashierPage.waitUntilLoads();

        cashierPage.clickFailedTxnGotItButtonIfDisplayed();
        cashierPage.payBy(PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Bank.ICICINB.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        OrderDTO orderDTO1 = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme).setTXN_AMOUNT("1.00").
                setSSO_TOKEN(user.ssoToken()).
                setCUST_ID(custID).
                build();
        checkoutPage.createOrder(orderDTO1);
        cashierPage.waitUntilLoads();
        cashierPage.assertSavedCardNotVisible();

    }


    @Parameters({"theme"})
    @Test(description = "Validate cancelTransaction API response and Response when MERC_UNQ_REF is sent for PGOnly Txn", groups = "smoke")
    public void validateCancelTxn_PGOnly(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setMERC_UNQ_REF("testing1")
                .build();
        checkoutPage.createOrder(orderDTO);
        System.out.println(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateMERC_UNQ_REF("testing1")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
        String cancelTxn = NativeAPIResourcePath.THEIA_CANCEL_TRANSACTION
                .replace("{mid}", orderDTO.getMID())
                .replace("{orderId}", orderDTO.getORDER_ID());
        DriverManager.getDriver().get(LocalConfig.PGP_HOST + cancelTxn);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMERC_UNQ_REF("testing1")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "test txn < 1 not allowed")
    public void testTxnOfLessThan1NotAllowed(@Optional("enhancedweb") String theme) {
        MerchantType merchant = MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("0.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("308")
                .validateRespMsg("Invalid Txn Amount")
                .validateCheckSum(merchant.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "PG Without Login transaction with IDBI NB")
    public void PGWithoutLogintransactionwithIDBINB(@Optional("enhancedweb") String theme) {

        MerchantType merchant = MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("IDBI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("IDBI")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();


    }


    @Parameters({"theme"})
    @Test(description = "PG With Login transaction with IDBI NB")
    public void PGWithtLogintransactionwithIDBINB(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        MerchantType merchant = MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("IDBI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("IDBI")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Failed Txn with IDBI NB")
    public void FailedTxnwithIDBINB(@Optional("enhancedweb") String theme) {


        MerchantType merchant = MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("99.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("IDBI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("IDBI")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Pending Txn with IDBI NB")
    public void PendingTxnwithIDBINB(@Optional("enhancedweb") String theme) {


        MerchantType merchant = MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("97.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("IDBI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();


    }

    @Epic(Constants.Sprint.SPRINT32_1)
    @Feature("PGPUI-554")
    @Parameters({"theme"})
    @Test(description = "Validate PG_MOBILE_NON_EDITABLE set to Y mobile number is masked")
    public void validateMobileNonEditableYMobileNoIsMasked(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MASKED_MOBILE_ENABLED, theme).setMobileNumber(user.mobNo()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.mobileNumber().assertContainsText("****");

    }

    @Epic(Constants.Sprint.SPRINT32_1)
    @Feature("PGPUI-554")
    @Parameters({"theme"})
    @Test(description = "Validate PG_MOBILE_NON_EDITABLE set to Y mobile number is masked")
    public void validateRequestOTPMobileNonEditableYMobileNoIsMasked(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MASKED_MOBILE_ENABLED, theme).setMobileNumber(user.mobNo()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.RequestOTP().click();
        cashierPage.pause(2);
        Assert.assertTrue(cashierPage.ResponseMsgRequestOtp().contains("****"), "Mobile Number is Not Masked");

    }

    @Epic(Constants.Sprint.SPRINT32_1)
    @Feature("PGPUI-554")
    @Parameters({"theme"})
    @Test(description = "Validate PG_MOBILE_NON_EDITABLE set to Y mobile number is not masked when mobile number is not passed")
    public void validateMobileNoIsNotMaskedWhenMobileNoisNotPassed(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MASKED_MOBILE_ENABLED, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().click();
        cashierPage.RequestloginOTP(user.mobNo());
        cashierPage.mobileNumber().assertDoesNotContainText("****");
        cashierPage.RequestOTP().click();
        cashierPage.pause(2);
        //mobile number is not masked need to be fixed
        Assert.assertFalse(cashierPage.ResponseMsgRequestOtp().contains("****"), "Mobile Number is Masked");

    }

    @Epic(Constants.Sprint.SPRINT32_1)
    @Feature("PGPUI-554")
    @Parameters({"theme"})
    @Test(description = "Validate PG_MOBILE_NON_EDITABLE set to Y mobile number is not masked when sso token is passed")
    public void validateMobileNoIsNotMaskedWhenSSOtokenisPassed(@Optional("enhancedwap") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MASKED_MOBILE_ENABLED, theme).setSSO_TOKEN(user.ssoToken()).setMobileNumber(user.mobNo()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertNotVisible();

    }

    @Epic(Constants.Sprint.SPRINT32_2)
    @Story("PGP-21561")
    @Test(description = "test txn amt in callback and txn status has 2 decimal precision")
    @Parameters({"theme"})
    public void testTxnAmtInCallbackAndTxnStatusHas2DecimalPrecision(@Optional("enhancedweb_revamp") String theme) {
//        String theme = "enhancedweb";
        String txnAmt = "100";
        String formattedTxnAmt = new DecimalFormat("0.00").format(Double.parseDouble(txnAmt));
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT(txnAmt)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN));
        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        pageWait.apply(responsePage.hasLoaded());
        Assertions.assertThat(responsePage.get(Attribute.TXNAMOUNT).getValue()).isEqualTo(formattedTxnAmt);
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.getTXNAMOUNT()).isEqualTo(formattedTxnAmt);
    }

    /*EDC Pay Confirm TC*/
    Constants.MerchantType merchantType = MerchantType.EDC_PAY_CONFIRM;

    @Parameters({"theme"})
    @Story("PGP-20215")
    @Epic(Sprint.SPRINT32_2)
    @Test(description = "Verify a success EDC Pay Confirm dynamic qr fast forward txn")
    public void edcPayConfirm1(@Optional("enhancedweb") String theme) throws Exception {
        String amount = "2";
        User user = userManager.getForRead(Label.EDC);
        Response response = QRHelper.generateEdcOrder(merchantType, amount, theme);
        String qrCodeId = response.jsonPath().getString("body.qrCodeId");
        FetchQRPaymentDetailsAPITest fetchQRPaymentDetailsAPITest = new FetchQRPaymentDetailsAPITest();
        Map<String, Object> root = fetchQRPaymentDetailsAPITest.root();
        ((Map) root.get("body")).remove("mlvSupported");
        ((Map) root.get("body")).remove("generateOrderId");
        ((Map) root.get("body")).remove("orderId");
        ((Map) root.get("body")).put("qrCodeId", qrCodeId);
        ((Map) root.get("head")).put("token", user.ssoToken());
        JsonPath orderId = fetchQRPaymentDetailsAPITest.req().body(root).post().jsonPath();

        FastForwardAppRequest fastForwardAppRequest = new FastForwardAppRequest
                .Builder(MerchantType.Hybrid.getId(), orderId.getString("body.paymentOptions.orderId"), "2")
                .setPaymentMode("PPI")
                .setReqType("CLW_APP_PAY")
                .setToken(user.ssoToken())
                .setTokenType("SSO")
                .setCustomerId(user.custId())
                .build();
        JsonPath ffd = new FastForward(fastForwardAppRequest).execute().jsonPath();
        Assertions.assertThat(ffd.getString("body.resultInfo.resultCode"))
                .as("resultCode mismatch")
                .isEqualToIgnoringCase("01");
        Assertions.assertThat(ffd.getString("body.resultInfo.resultStatus"))
                .as("resultStatus mismatch")
                .isEqualToIgnoringCase("TXN_SUCCESS");

    }

    @Parameters({"theme"})
    @Story("PGP-20215")
    @Epic(Sprint.SPRINT32_2)
    @Test(description = "Verify a success EDC Pay Confirm dynamic qr txn with DC")
    public void edcPayConfirm2(@Optional("enhancedweb") String theme) throws Exception {
        String amount = "2";
        User user = userManager.getForRead(Label.EDC);
        Response response = QRHelper.generateEdcOrder(merchantType, amount, theme);
        String qrCodeId = response.jsonPath().getString("body.qrCodeId");
        FetchQRPaymentDetailsAPITest fetchQRPaymentDetailsAPITest = new FetchQRPaymentDetailsAPITest();
        Map<String, Object> root = fetchQRPaymentDetailsAPITest.root();
        ((Map) root.get("body")).remove("mlvSupported");
        ((Map) root.get("body")).remove("generateOrderId");
        ((Map) root.get("body")).remove("orderId");
        ((Map) root.get("body")).put("qrCodeId", qrCodeId);
        ((Map) root.get("head")).put("token", user.ssoToken());
        JsonPath resp = fetchQRPaymentDetailsAPITest.req().body(root).post().jsonPath();
        String orderId = resp.getString("body.paymentOptions.orderId");
        TxnAmount amt = new TxnAmount();
        String edcAdditionalInfo = "payeeType:MERCHANT|currencyCode:INR|category:GROCERY|subCategory:GROCERY|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:fiesta60595251908739|pgEnabled:true|merchantTransId:" + orderId + "|qrCodeId:" + qrCodeId + "|REQUEST_TYPE:QR_MERCHANT|EXPIRY_DATE:1535049000000|NAME:testmerchant10|MERCHANT_NAME:testmerchant10|MOBILE_NO:7404186250|TXN_AMOUNT:2.00|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:96fc4e92-a3d4-44cb-8ef7-ba656c844ca9|ORDER_ID:" + orderId + "|MERCHANT_STATUS:ACTIVE|qr_code_id:" + qrCodeId + "|comment:|PRODUCT_CODE:51051000100000000047|CHANNEL_ID:EDC";

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(edcAdditionalInfo);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amt.setValue("2"))
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo("|4160210902998262|834|042021")
                .setAuthMode("otp")
                .setExtendInfo(extendInfo)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("DC")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Story("PGP-20215")
    @Epic(Sprint.SPRINT32_2)

    @Test(description = "Verify a success EDC Pay Confirm dynamic qr txn with CC")
    public void edcPayConfirm3(@Optional("enhancedweb") String theme) throws Exception {
        String amount = "2";
        User user = userManager.getForRead(Label.EDC);
        Response response = QRHelper.generateEdcOrder(merchantType, amount, theme);
        String qrCodeId = response.jsonPath().getString("body.qrCodeId");
        FetchQRPaymentDetailsAPITest fetchQRPaymentDetailsAPITest = new FetchQRPaymentDetailsAPITest();
        Map<String, Object> root = fetchQRPaymentDetailsAPITest.root();
        ((Map) root.get("body")).remove("mlvSupported");
        ((Map) root.get("body")).remove("generateOrderId");
        ((Map) root.get("body")).remove("orderId");
        ((Map) root.get("body")).put("qrCodeId", qrCodeId);
        ((Map) root.get("head")).put("token", user.ssoToken());
        JsonPath resp = fetchQRPaymentDetailsAPITest.req().body(root).post().jsonPath();
        String orderId = resp.getString("body.paymentOptions.orderId");
        TxnAmount amt = new TxnAmount();
        String edcAdditionalInfo = "payeeType:MERCHANT|currencyCode:INR|category:GROCERY|subCategory:GROCERY|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:fiesta60595251908739|pgEnabled:true|merchantTransId:" + orderId + "|qrCodeId:" + qrCodeId + "|REQUEST_TYPE:QR_MERCHANT|EXPIRY_DATE:1535049000000|NAME:testmerchant10|MERCHANT_NAME:testmerchant10|MOBILE_NO:7404186250|TXN_AMOUNT:2.00|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:96fc4e92-a3d4-44cb-8ef7-ba656c844ca9|ORDER_ID:" + orderId + "|MERCHANT_STATUS:ACTIVE|qr_code_id:" + qrCodeId + "|comment:|PRODUCT_CODE:51051000100000000047|CHANNEL_ID:EDC";

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(edcAdditionalInfo);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amt.setValue("2"))
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo("|4386280030525798|834|042021")
                .setAuthMode("otp")
                .setExtendInfo(extendInfo)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .AssertAll();
    }


    @Parameters({"theme"})
    @Story("PGP-20215")
    @Epic(Sprint.SPRINT32_2)
    @Test(description = "Verify a success EDC Pay Confirm dynamic qr txn with netbanking")
    public void edcPayConfirm4(@Optional("enhancedweb") String theme) throws Exception {
        String amount = "2";
        User user = userManager.getForRead(Label.EDC);
        Response response = QRHelper.generateEdcOrder(merchantType, amount, theme);
        String qrCodeId = response.jsonPath().getString("body.qrCodeId");
        FetchQRPaymentDetailsAPITest fetchQRPaymentDetailsAPITest = new FetchQRPaymentDetailsAPITest();
        Map<String, Object> root = fetchQRPaymentDetailsAPITest.root();
        ((Map) root.get("body")).remove("mlvSupported");
        ((Map) root.get("body")).remove("generateOrderId");
        ((Map) root.get("body")).remove("orderId");
        ((Map) root.get("body")).put("qrCodeId", qrCodeId);
        ((Map) root.get("head")).put("token", user.ssoToken());
        JsonPath resp = fetchQRPaymentDetailsAPITest.req().body(root).post().jsonPath();
        String orderId = resp.getString("body.paymentOptions.orderId");
        TxnAmount amt = new TxnAmount();
        String edcAdditionalInfo = "payeeType:MERCHANT|currencyCode:INR|category:GROCERY|subCategory:GROCERY|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:fiesta60595251908739|pgEnabled:true|merchantTransId:" + orderId + "|qrCodeId:" + qrCodeId + "|REQUEST_TYPE:QR_MERCHANT|EXPIRY_DATE:1535049000000|NAME:testmerchant10|MERCHANT_NAME:testmerchant10|MOBILE_NO:7404186250|TXN_AMOUNT:2.00|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:96fc4e92-a3d4-44cb-8ef7-ba656c844ca9|ORDER_ID:" + orderId + "|MERCHANT_STATUS:ACTIVE|qr_code_id:" + qrCodeId + "|comment:|PRODUCT_CODE:51051000100000000047|CHANNEL_ID:EDC";

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo(edcAdditionalInfo);


        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchantType, "SSO", user.ssoToken())
                .setOrderId(orderId)
                .setTxnAmount(amt.setValue("2"))
                .setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setAuthMode("USRPWD")
                .setExtendInfo(extendInfo)
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("NB")
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "To verify VPA return incase of Successful UPI txn when RETURN_USER_VPA_IN_RESPONSE = Y ")
    public void verifyReturnVPAforSuccessfulTxn(@Optional("enhancedweb") String theme) {

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PPBL_UPI_COLLECT, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateVPA(paymentDTO.getVpa())
                .validateCheckSum(MerchantType.PPBL_UPI_COLLECT.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateVPA(paymentDTO.getVpa())
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "VPA", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.vpa().equals(paymentDTO.getVpa())
        );
        sAssert.eval();
    }


    @Parameters({"theme"})
    @Test(description = "To verify VPA return incase of Failure UPI txn when RETURN_USER_VPA_IN_RESPONSE = Y ")
    public void verifyReturnVPAforFailedTxn(@Optional("enhancedweb") String theme) {

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PPBL_UPI_COLLECT, theme)
                .setTXN_AMOUNT("99.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateVPA(paymentDTO.getVpa())
                .validateCheckSum(MerchantType.PPBL_UPI_COLLECT.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateMid(orderDTO.getMID())
                .validateRefundAmnt("0.00")
                .validateVPA(paymentDTO.getVpa())
                .validateTxnDate(new Date())
                .AssertAll();


        assertion.apply(peonWait.apply(() -> peons.getAt(orderDTO.getORDER_ID()) != null));
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "VPA", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE"),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals("PPBLC"),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("227"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.vpa().equals(paymentDTO.getVpa())

        );
        sAssert.eval();
    }


    // Old flow should work properly if FF4j Flag is off and proper response should be returned in reponse to Cancel Transcation API

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is off proper Response to cancel transaction send to merchant callback")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsOff(@Optional("enhancedweb") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.backBtn().click();
        cashierPage.feedbackSkipFeedback().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("141")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON while doing cancel transaction all Params will be send to merchant configured callback")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsOn(@Optional("enhancedweb") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(EMI_DISCOVERY, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.backBtn().click();
        cashierPage.cancelPaymentYes().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("141")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }


    //Static URL is Redirected to MOCK URL for TestCases So it will redirected to MOCK URL

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON and txn token is null cancel transaction sent mid/orderID/RespCode/RespMsg to Merchant configured callback url")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsOnTxnTokenNull(@Optional("enhancedweb") String theme) {
        Constants.MerchantType merchantType = MerchantType.HDFC_UPI_COLLECT;
        String OrderId = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setORDER_ID(OrderId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CancelTxn cancelTxn = new CancelTxn(merchantType, OrderId, "");
        Response result = cancelTxn.execute();
        Element bodyElement = Jsoup.parse(result.asString()).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("input"))
                .as("input tag is empty")
                .isNotEmpty();

        Assertions.assertThat(bodyElement.getElementsByAttribute("action").attr("action"))
                .as("Error validating CallBackUrl")
                .contains("https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse");

        Elements inputElement = bodyElement.getElementsByTag("input");

        Assertions.assertThat(inputElement.get(0).attr("name", "ORDERID")
                        .val())
                .as("OrderID is Not present")
                .isEqualTo(OrderId);


        Assertions.assertThat(inputElement.get(1).attr("name", "MID")
                        .val())
                .as("MID is Not present")
                .isEqualTo(merchantType.getId());

        Assertions.assertThat(inputElement.get(2).attr("name", "RESPCODE")
                        .val())
                .as("Response Code not equal")
                .isEqualTo("402");


        Assertions.assertThat(inputElement.get(3).attr("RESPMSG", "RESPCODE")
                        .val())
                .as("Response Message not Correct")
                .isEqualTo("We are processing your transaction.");
    }

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON and txn token is tampered cancel transaction will lands to OOPs Page")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsOnTxnTokenTampered(@Optional("enhancedweb") String theme) {
        Constants.MerchantType merchantType = MerchantType.HDFC_UPI_COLLECT;
        String OrderId = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setORDER_ID(OrderId)
                .build();
        checkoutPage.createOrder(orderDTO);
        CancelTxn cancelTxn = new CancelTxn(merchantType, OrderId, "123");
        Response result = cancelTxn.execute();
        Element bodyElement = Jsoup.parse(result.asString()).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();

        Assertions.assertThat(bodyElement.getElementsByTag("div").get(0).attr("id"))
                .as("OOPs Page doesn't come")
                .isEqualTo("oopsPage");
    }


///RISK TEST CASES

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is OFF proper Response to cancel transaction send to merchant callback for Risk Amount")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsOFFForRiskAmount(@Optional("enhancedweb") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.NATIVE_RISK, theme)
                .setTXN_AMOUNT("1.8")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        RiskVerificationPage riskVerificationPage = new RiskVerificationPage();
        riskVerificationPage.cancelAlert();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("141")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }


    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON proper Response to cancel transaction send to merchant callback for Risk Amount")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsONForRiskAmount(@Optional("enhancedweb") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDFC_UPI_COLLECT, theme)
                .setTXN_AMOUNT("1.8")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        RiskVerificationPage riskVerificationPage = new RiskVerificationPage();
        riskVerificationPage.cancelAlert();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("141")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }


    //Static URL is Redirected to MOCK URL for TestCases So it will redirected to MOCK URL

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON txn token is Null Response to cancel transaction send to static callback for Risk Amount")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsONForRiskAmountTxnisNull(@Optional("enhancedweb") String theme) {
        Constants.MerchantType merchantType = MerchantType.HDFC_UPI_COLLECT;
        String OrderId = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setORDER_ID(OrderId)
                .setTXN_AMOUNT("1.8")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        RiskVerificationPage riskVerificationPage = new RiskVerificationPage();
        riskVerificationPage.waitUntilLoads();
        CancelTxn cancelTxn = new CancelTxn(merchantType, OrderId, "", "");
        Response result = cancelTxn.execute();
        Element bodyElement = Jsoup.parse(result.asString()).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("input"))
                .as("input tag is empty")
                .isNotEmpty();

        Assertions.assertThat(bodyElement.getElementsByAttribute("action").attr("action"))
                .as("Error validating CallBackUrl")
                .contains("https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse");

        Elements inputElement = bodyElement.getElementsByTag("input");

        Assertions.assertThat(inputElement.get(0).attr("name", "ORDERID")
                        .val())
                .as("OrderID is Not present")
                .isEqualTo(OrderId);


        Assertions.assertThat(inputElement.get(1).attr("name", "MID")
                        .val())
                .as("MID is Not present")
                .isEqualTo(merchantType.getId());

        Assertions.assertThat(inputElement.get(2).attr("name", "RESPCODE")
                        .val())
                .as("Response Code not equal")
                .isEqualTo("402");


        Assertions.assertThat(inputElement.get(3).attr("RESPMSG", "RESPCODE")
                        .val())
                .as("Response Message not Correct")
                .isEqualTo("We are processing your transaction.");

    }

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON txn token is tampered it lands to oops page for Risk Amount")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsONForRiskAmountTxnisTampered(@Optional("enhancedweb") String theme) {
        Constants.MerchantType merchantType = MerchantType.HDFC_UPI_COLLECT;
        String OrderId = CommonHelpers.generateOrderId();
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.HDFC_UPI_COLLECT, theme)
                .setTXN_AMOUNT("1.8")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        RiskVerificationPage riskVerificationPage = new RiskVerificationPage();
        riskVerificationPage.waitUntilLoads();
        CancelTxn cancelTxn = new CancelTxn(merchantType, OrderId, "123", "");
        Response result = cancelTxn.execute();
        Element bodyElement = Jsoup.parse(result.asString()).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();

        Assertions.assertThat(bodyElement.getElementsByTag("div").get(0).attr("id"))
                .as("OOPs Page doesn't come")
                .isEqualTo("oopsPage");

    }

    ///Basic KYC

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is OFF proper Response to cancel transaction send to merchant callback for Basic KYC user")
    public void verifyResponseForCancelFf4jFlagIsOFFForBasicKYC(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForRead(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.ADD_MONEY_ONLY, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString()).click();
        kycPage.idNumber().type("BQYPG0866H");
        kycPage.idName().type("WrongPan");
        for (int i = 0; i < 3; i++)
            kycPage.submit();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("141")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON proper Response to cancel transaction send to merchant callback for Basic KYC user")
    public void verifyResponseForCancelFf4jFlagIsONForBasicKYC(@Optional("enhancedweb") String theme) throws Exception {
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForRead(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString()).click();
        kycPage.idNumber().type("BQYPG0866H");
        kycPage.idName().type("WrongPan");
        for (int i = 0; i < 3; i++)
            kycPage.submit();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("141")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

//Static URL is Redirected to MOCK URL for TestCases So it will redirected to MOCK URL

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON txn token is Null Response to cancel transaction send to static callback for Risk Amount")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsONForBasicKYCTxnisNull(@Optional("enhancedweb") String theme) throws Exception {


        Constants.MerchantType merchantType = MerchantType.AddMoney;
        String OrderId = CommonHelpers.generateOrderId();
        KYCPage kycPage = new KYCPage();
        User user = userManager.getForRead(Label.BASICTOKYC);
        OrderDTO orderDTO = new OrderFactory.AddMoney(merchantType, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString()).click();
        kycPage.idNumber().type("BQYPG0866H");
        kycPage.idName().type("WrongPan");
        CancelTxn cancelTxn = new CancelTxn(merchantType, OrderId, "");
        Response result = cancelTxn.execute();
        Element bodyElement = Jsoup.parse(result.asString()).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("input"))
                .as("input tag is empty")
                .isNotEmpty();

        Assertions.assertThat(bodyElement.getElementsByAttribute("action").attr("action"))
                .as("Error validating CallBackUrl")
                .contains("https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse");

        Elements inputElement = bodyElement.getElementsByTag("input");

        Assertions.assertThat(inputElement.get(0).attr("name", "ORDERID")
                        .val())
                .as("OrderID is Not present")
                .isEqualTo(OrderId);


        Assertions.assertThat(inputElement.get(1).attr("name", "MID")
                        .val())
                .as("MID is Not present")
                .isEqualTo(merchantType.getId());

        Assertions.assertThat(inputElement.get(2).attr("name", "RESPCODE")
                        .val())
                .as("Response Code not equal")
                .isEqualTo("402");


        Assertions.assertThat(inputElement.get(3).attr("RESPMSG", "RESPCODE")
                        .val())
                .as("Response Message not Correct")
                .isEqualTo("We are processing your transaction.");

    }

    @Feature("PGP-24378")
    @Epic(Sprint.SPRINT34_1)
    @Parameters({"theme"})
    @Test(description = "When FF4J flag is ON txn token is tampered it lands to oops page for Risk Amount")
    public void verifyResponseForCancelTxnWhenFf4jFlagIsONForBasicKYCTxnisTampered(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType merchantType = MerchantType.ADD_MONEY_ONLY;
        User user = userManager.getForRead(Label.BASICTOKYC);
        String OrderId = CommonHelpers.generateOrderId();
        KYCPage kycPage = new KYCPage();
        OrderDTO orderDTO = new OrderFactory.AddMoney(merchantType, theme, user).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        kycPage.idRadioButton(KYCPage.allIDDetails.Pan_Card.toString()).click();
        kycPage.idNumber().type("BQYPG0866H");
        kycPage.idName().type("WrongPan");
        CancelTxn cancelTxn = new CancelTxn(merchantType, OrderId, "123");
        Response result = cancelTxn.execute();
        Element bodyElement = Jsoup.parse(result.asString()).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("div").get(0).attr("id"))
                .as("OOPs Page doesn't come")
                .isEqualTo("oopsPage");

    }


    @Epic(Constants.Sprint.SPRINT34_1)
    @Feature("PGP-24487")
    @Parameters({"theme"})
    @Test(description = "To verify if there is special character in custId the transaction is getting success for non logged user")
    public void verifySpecialCharacterCustIdTxnGotSuccessNonloggedUser(@Optional("enhancedweb") String theme) {
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PPBL_UPI_COLLECT, theme)
                .setCUST_ID("Test^===#===")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateVPA(paymentDTO.getVpa())
                .validateCheckSum(MerchantType.PPBL_UPI_COLLECT.getKey())
                .assertAll();
    }

    @Epic(Constants.Sprint.SPRINT34_1)
    @Feature("PGP-24487")
    @Parameters({"theme"})
    @Test(description = "To verify if there is special character in custId the transaction is getting success for logged user")
    public void verifySpecialCharacterCustIdTxnGotSuccessloggedUser(@Optional("enhancedweb") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.PPBL_UPI_COLLECT, theme, user)
                .setCUST_ID("Test^===#===")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
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
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateVPA(paymentDTO.getVpa())
                .validateCheckSum(MerchantType.PPBL_UPI_COLLECT.getKey())
                .assertAll();
    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To Verify that NB should not come on cashierPage when NB is sent in PAYMENT_MODE_DISABLE ")
    public void verifyNBNotVisible(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPAYMENT_MODE_DISABLE("NB")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().assertNotVisible();
    }

    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To Verify that PPBL NB should not come on cashierPage when NB is sent in PAYMENT_MODE_DISABLE ")
    public void verifyPPBLNotVisible(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPAYMENT_MODE_DISABLE("PPBL")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().assertNotVisible();
    }


    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the cashier page when NB is sent in PAYMENT_TYPE_ID and PAYMENT_MODE_ONLY - YES")
    public void verifyOnlyNBIsVisible(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPAYMENT_TYPE_ID("NB")
                .setPAYMENT_MODE_ONLY("YES")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().assertVisible();
        cashierPage.tabCreditCard().assertNotVisible();
    }


    @Epic("PGP-24548")
    @Owner("Tarun")
    @Test(description = "To verify the cashier page when PPBL NB is sent in PAYMENT_TYPE_ID and PAYMENT_MODE_ONLY - YES")
    public void verifyOnlyPPBLNBIsVisible(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setPAYMENT_TYPE_ID("PPBL")
                .setPAYMENT_MODE_ONLY("YES")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.textBoxPPBLPassCode().assertVisible();
        cashierPage.tabNetBanking().assertNotVisible();
    }

    @Parameters({"theme"})
    @Epic("PGP-22222")
    @Owner("Tarun")
    @Test(description = "Refresh the page after login on cashier page and process the further txn")
    public void refreshPageAfterLoginAndProceed(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.refresh();
        cashierPage.payBy(PayMode.CC);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();

    }

    @Parameters({"theme"})
    @Epic("PGP-22222")
    @Owner("Tarun")
    @Test(description = "Refresh the page for already logged in flow on cashier page and process the further txn")
    public void refreshPageForLoggedInFlow(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.payBy(PayMode.CC);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();

    }


    @Parameters({"theme"})
    @Epic("PGP-22222")
    @Owner("Tarun")
    @Test(description = "Refresh the non logged in page and then login the cashier page and process the further txn")
    public void refreshNonLoggedInPage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.refresh();
        cashierPage.login(user);
        cashierPage.payBy(PayMode.NB, paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .AssertAll();

    }

    @Story("Bin Migration")
    @Parameters({"theme"})
    @Test(description = "test Low Success Rate Message on cashier page When Zero Success Rate is 1 for Bin")
    public void testLowSuccessRateMessageWhenZeroSuccessRateis1(@Optional("enhancedweb_revamp") String theme) {
//        String theme = "enhancedweb";
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().setCreditCardNumber(PaymentDTO.LOW_SUCCESS_RATE_CARD_NUMBER).getCreditCardNumber());
        cashierPage.cardMessage().assertText("We are observing high failures on transacting through this type of debit/credit card right now. We strongly recommend using a different card or payment method for completing this payment.");
    }

    @Parameters({"theme"})
    @Feature("PGP-24778")
    @Owner("Tarun")
    @Test(description = "Verify success is coming in case of TRANS_PAID for CC transaction")
    public void validateSuccessTransPaidCC(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();

        OrderDTO duplicateorderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setCUST_ID(orderDTO.getCUST_ID())
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.switchToNewTab();
        checkoutPage.createOrder(duplicateorderDTO);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .AssertAll();

        PGPHelpers.closeNewTab();
        cashierPage.payBy(PayMode.CC);

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-24778")
    @Owner("Tarun")
    @Test(description = "Verify success is coming in case of TRANS_PAID for DC transaction")
    public void validateSuccessTransPaidDC(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();

        OrderDTO duplicateorderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setCUST_ID(orderDTO.getCUST_ID())
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.switchToNewTab();
        checkoutPage.createOrder(duplicateorderDTO);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .AssertAll();

        PGPHelpers.closeNewTab();
        cashierPage.payBy(PayMode.CC);

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();


    }

    @Parameters({"theme"})
    @Feature("PGP-24778")
    @Owner("Tarun")
    @Test(description = "Verify success is coming in case of TRANS_PAID for NB transaction")
    public void validateSuccessTransPaidNB(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();

        OrderDTO duplicateorderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setCUST_ID(orderDTO.getCUST_ID())
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.switchToNewTab();
        checkoutPage.createOrder(duplicateorderDTO);
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .AssertAll();

        PGPHelpers.closeNewTab();
        cashierPage.payBy(PayMode.CC);

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();


    }

    @Parameters({"theme"})
    @Feature("PGP-24778")
    @Owner("Tarun")
    @Test(description = "Verify success is coming in case of TRANS_PAID for UPI Collect transaction")
    public void validateSuccessTransPaidUPI(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();

        OrderDTO duplicateorderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setCUST_ID(orderDTO.getCUST_ID())
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.switchToNewTab();
        checkoutPage.createOrder(duplicateorderDTO);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .AssertAll();

        PGPHelpers.closeNewTab();
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI"));

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-24778")
    @Owner("Tarun")
    @Test(description = "Verify success is coming in case of TRANS_PAID for WALLET transaction")
    public void validateSuccessTransPaidPPI(@Optional("enhancedweb") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        WalletHelpers.modifyBalance(user, txnAmount);
        OrderDTO duplicateorderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID(orderDTO.getCUST_ID())
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.switchToNewTab();
        checkoutPage.createOrder(duplicateorderDTO);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();

        PGPHelpers.closeNewTab();
        cashierPage.payBy(PayMode.UPI);

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

    }

    @Parameters({"theme"})
    @Feature("PGP-24778")
    @Owner("Tarun")
    @Test(description = "Verify success is coming in case of TRANS_PAID for PPBL transaction")
    public void validateSuccessTransPaidPPBL(@Optional("enhancedweb") String theme) throws Exception {
        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.PPBL);
        PostpaidHelpers.updateBalance(txnAmount.toString());
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        OrderDTO duplicateorderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID(orderDTO.getCUST_ID())
                .setORDER_ID(orderDTO.getORDER_ID())
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.switchToNewTab();
        checkoutPage.createOrder(duplicateorderDTO);
        cashierPage.payBy(PayMode.PPBL);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateBankName("PPBL")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateBankName("PPBL")
                .AssertAll();

        PGPHelpers.closeNewTab();
        cashierPage.payBy(PayMode.UPI);

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateBankName("PPBL")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();


    }

    //Corporate Cards Enhanced Txn

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success Corporate indian card CC bin txn on Corporate Card Merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateCCBinCorporateMerchant(@Optional("enhancedweb") String theme) {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateMerchant.getId());
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateMerchant, "CC", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "CC", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "CC", Bank.HDFC.toString(), Gateway.HDFC.toString());

    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success non Corporate indian card CC bin txn on Corporate Card Merchant", groups = "P1")
    @Description("Automation JIRA : PGP-26425")
    public void successNonCorporateCCBinCorporateMerchant(@Optional("enhancedweb") String theme) {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateMerchant.getId());
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO();
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(false);


        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateMerchant, "CC", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusNonCorporate(orderDTO, "CC", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonNonCorporate(orderDTO, "CC", Bank.HDFC.toString(), Gateway.HDFC.toString());

    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success Corporate indian card DC bin txn on Corporate Card Merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateDCBinCorporateMerchant(@Optional("enhancedweb") String theme) {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardDC(corporateMerchant.getId());

        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateMerchant, "DC", Bank.AXIS.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "DC", Bank.AXIS.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "DC", Bank.AXIS.toString(), Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success non Corporate indian card DC bin txn on Corporate Card Merchant", groups = "P1")
    @Description("Automation JIRA : PGP-26425")
    public void successNonCorporateDCBinCorporateMerchant(@Optional("enhancedweb") String theme) {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardDC(corporateMerchant.getId());

        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO();
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(false);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateMerchant, "DC", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusNonCorporate(orderDTO, "DC", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonNonCorporate(orderDTO, "DC", Bank.HDFC.toString(), Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success Corporate indian card EMI bin txn on Corporate Card Merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateEMIBinCorporateMerchant(@Optional("enhancedwap_revamp") String theme) {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateMerchant.getId());

        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC).setEmiCard(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, CORPORATE_CARD_ONLY, "EMI", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "EMI", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "EMI", Bank.HDFCBANK.toString(), Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success non Corporate indian card EMI bin txn on Corporate Card Merchant", groups = "P1")
    @Description("Automation JIRA : PGP-26425")
    public void successNonCorporateEMIBinCorporateMerchant(@Optional("enhancedwap_revamp") String theme) {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardCC(corporateMerchant.getId());

        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String bin = PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER.substring(0, 6);

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(false);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateMerchant, "EMI", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessTxnStatusNonCorporate(orderDTO, "EMI", Bank.HDFC.toString());

        CorporateHelpers.validateSuccessPeonNonCorporate(orderDTO, "EMI", Bank.HDFCBANK.toString(), Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success Corporate indian card EMI DC bin txn on Corporate Card Merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successCorporateEMIDCBinCorporateMerchant(@Optional("enhancedweb") String theme) throws Exception {

        MerchantType corporateMerchant = MerchantType.CORPORATE_CARD_ONLY;
        CorporateHelpers.assertCorporateCardDC(corporateMerchant.getId());

        User user = userManager.getForWrite(Label.EMIDC);

        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_DC).setBankName("ICICI Bank Debit Card");
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6); // EMI DC

        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateMerchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.EMI, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateMerchant, "EMI", Bank.ICICI.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "EMI", Bank.ICICI.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "EMI", Bank.ICICI.toString(), Gateway.ICICI.toString());
    }


    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify failure Corporate indian card CC bin txn on Non Corporate Card Merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void failureCorporateCCBinNonCorporateMerchant(@Optional("enhancedweb_revamp") String theme) {

        MerchantType nonCorporateCard = MerchantType.PGOnly;
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(nonCorporateCard, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();

        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        Assertions.assertThat(cashierPage.getErrorMessageAfterEnteringCard()).isEqualTo("VISA Corporate card is not allowed for this payment. Please try paying using other cards/options.");

    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify failure Corporate indian card DC bin txn on Non Corporate Card Merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void failureCorporateDCBinNonCorporateMerchant(@Optional("enhancedweb_revamp") String theme) {

        MerchantType nonCorporateCard = MerchantType.PGOnly;
        Double txnAmount = 2.0;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(nonCorporateCard, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabDebitCard().waitUntilClickable();
        cashierPage.tabDebitCard().click();

        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());
        Assertions.assertThat(cashierPage.getErrorMessageAfterEnteringCard()).isEqualTo("MASTER Corporate card is not allowed for this payment. Please try paying using other cards/options.");

    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify failure Corporate indian card DC bin txn on Corporate Card Merchant", groups = "P1")
    @Description("Automation JIRA : PGP-26425")
    public void failureCorporateDCBinCorporateMerchant(@Optional("enhancedweb_revamp") String theme) {

        MerchantType corporateCard = MerchantType.CORPORATE_CARD_ONLY;
        Double txnAmount = 99.98;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateCard, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC, paymentDTO);

        CorporateHelpers.validateFailureResponse(orderDTO, corporateCard, "DC", Bank.AXIS.toString());

        CorporateHelpers.validateFailureTxnStatusCorporate(orderDTO, "DC", Bank.AXIS.toString());

        //Corporate Card not coming
        CorporateHelpers.validateFailurePeonCorporate(orderDTO, "DC", Bank.AXIS.toString(), Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify failure Corporate indian card CC bin txn on Corporate Card Merchant", groups = "P1")
    @Description("Automation JIRA : PGP-26425")
    public void failureCorporateCCBinCorporateMerchant(@Optional("enhancedweb_revamp") String theme) {

        MerchantType corporateCard = MerchantType.CORPORATE_CARD_ONLY;
        Double txnAmount = 99.98;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CORPORATE_INDIAN_CC);
        String bin = paymentDTO.getCreditCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateCard, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);

        CorporateHelpers.validateFailureResponse(orderDTO, corporateCard, "CC", Bank.HDFC.toString());

        CorporateHelpers.validateFailureTxnStatusCorporate(orderDTO, "CC", Bank.HDFC.toString());

        //Corporate Card not coming
        CorporateHelpers.validateFailurePeonCorporate(orderDTO, "CC", Bank.HDFC.toString(), Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify PENDING Corporate indian card DC bin txn on Corporate Card Merchant", groups = "P1")
    @Description("Automation JIRA : PGP-26425")
    public void pendingCorporateDCBinCorporateMerchant(@Optional("enhancedweb_revamp") String theme) {

        MerchantType corporateCard = MerchantType.CORPORATE_CARD_ONLY;
        Double txnAmount = 99.84;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateCard, theme)
                .setTXN_AMOUNT(txnAmount.toString())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC, paymentDTO);

        CorporateHelpers.validatePendingResponse(orderDTO, corporateCard, "DC", Bank.AXIS.toString());

    }


    @Parameters({"theme"})
    @Owner("Tarun")
    @Feature("PGP-24136")
    @Test(description = "To verify success Corporate indian saved card DC bin txn on Corporate Card Merchant", groups = "P0")
    @Description("Automation JIRA : PGP-26425")
    public void successDCCorporateDCBinCorporateMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType corporateCard = MerchantType.CORPORATE_CARD_ONLY;
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.CORPORATE_INDIAN_DC);

        User user = userManager.getForWrite(Label.BASIC);

        //Deleting Card on PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Saving card on PGPDB
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String bin = paymentDTO.getDebitCardNumber().substring(0, 6);
        Assertions.assertThat(CorporateHelpers.isBinCorporate(bin)).isEqualTo(true);

        OrderDTO orderDTO = new OrderFactory.PGOnly(corporateCard, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.SAVED_CARD, paymentDTO);

        CorporateHelpers.validateSuccessResponse(orderDTO, corporateCard, "DC", Bank.AXISSC.toString());

        CorporateHelpers.validateSuccessTxnStatusCorporate(orderDTO, "DC", Bank.AXISSC.toString());

        CorporateHelpers.validateSuccessPeonCorporate(orderDTO, "DC", Bank.AXISSC.toString(), Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Test
    public void testWhenOrderIsReInitiatedWithDifferentTxnAmtWithoutDeletingCachedKey(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO order = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(order);

        order.setTXN_AMOUNT("2");
        checkoutPage.createOrder(order);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("2023")
                .validateRespMsg("Repeat Request Inconsistent")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testWhenOrderIsReInitiatedWithSameDetailsAfterDeletingCachedKey(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO order = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(order);

        SESSION_REDIS_CLUSTER().del("NativeTxnInitiateRequest" + order.getMID() + "_" + order.getORDER_ID());

        checkoutPage.createOrder(order);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("501")
                .validateRespMsg("System Error.")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testWhenOrderIsReInitiatedWithDifferentTxnAmtAfterDeletingCachedKey(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO order = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(order);

        SESSION_REDIS_CLUSTER().del("NativeTxnInitiateRequest" + order.getMID() + "_" + order.getORDER_ID());

        order.setTXN_AMOUNT("2");
        checkoutPage.createOrder(order);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("325")
                .validateRespMsg("Duplicate Order Id")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testWhenOrderIsReInitiatedWithDifferentTxnAmtAfterGettingDuplicateOrderErrorMsgWithoutDeletingCachedKey(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO order = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(order);

        SESSION_REDIS_CLUSTER().del("NativeTxnInitiateRequest" + order.getMID() + "_" + order.getORDER_ID());

        order.setTXN_AMOUNT("2");
        checkoutPage.createOrder(order);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("325")
                .validateRespMsg("Duplicate Order Id")
                .assertAll();

        order.setTXN_AMOUNT("3");
        checkoutPage.createOrder(order);

        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("2023")
                .validateRespMsg("Repeat Request Inconsistent")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test
    public void testWhenOrderIsReInitiatedWithDifferentTxnAmtAfterGettingDuplicateOrderErrorMsgAfterDeletingCachedKey(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO order = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(order);

        SESSION_REDIS_CLUSTER().del("NativeTxnInitiateRequest" + order.getMID() + "_" + order.getORDER_ID());

        order.setTXN_AMOUNT("2");
        checkoutPage.createOrder(order);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("325")
                .validateRespMsg("Duplicate Order Id")
                .assertAll();

        SESSION_REDIS_CLUSTER().del("NativeTxnInitiateRequest" + order.getMID() + "_" + order.getORDER_ID());

        order.setTXN_AMOUNT("3");
        checkoutPage.createOrder(order);

        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("325")
                .validateRespMsg("Duplicate Order Id")
                .assertAll();
    }


    @Override
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "PG side: Non Logged In Flow : Verify that international card is visible on cashier page on international supported MID")
    public void internationalCardNonLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        MerchantType internationalMerchant = MerchantType.ALLPAYMODE;
        SavedCardHelpers.assertStoreCardPrefEnabled(internationalMerchant);
        prerequisite:
        {

            FF4JFlags.disable("shortCircuitSavedCardServiceReadForMidCustId");
            FF4JFlags.disable("fetchSavedcardFromPlatformForMidCustId");
            FF4JFlags.disable("returnSavedCardsFromPlatformForMidCustId");

        }

        OrderDTO orderDTO = new OrderFactory.PGOnly(internationalMerchant, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(orderDTO.getCUST_ID());

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), orderDTO.getCUST_ID(), internationalMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabSavedCard().assertVisible();

    }


    @Override
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "P+ side : Non Logged In Flow  : Verify that international card is visible on cashier page on international supported MID")
    public void internationalCardNonLoggedInFlowPPlusSide(@Optional("enhancedweb_revamp") String theme) {


        prerequisite:
        {

            FF4JFlags.enable("shortCircuitSavedCardServiceReadForMidCustId");
            FF4JFlags.enable("fetchSavedcardFromPlatformForMidCustId");
            FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");

        }

        MerchantType internationalMerchant = MerchantType.ALLPAYMODE;

        OrderDTO orderDTO = new OrderFactory.PGOnly(internationalMerchant, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        //Deleting for Merchant on P+
        SavedCardHelpers.deleteSavedCardsAlipay(internationalMerchant.getId(), orderDTO.getCUST_ID());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(internationalMerchant.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.tabSavedCard().assertVisible();

    }


    @Override
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "PG side : Logged In Flow : Verify that international card is visible on cashier page on international supported MID")
    public void internationalCardLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        SavedCardHelpers.disableAllSavedCardFlags();

        MerchantType internationalMerchant = MerchantType.ALLPAYMODE;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        OrderDTO orderDTO = new OrderFactory.PGOnly(internationalMerchant, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        //Deleting for user on PG side
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for user on PG side
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

    }


    @Override
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "P+ side : Logged In Flow : Verify that international card is visible on cashier page on international supported MID & SUCCESS txn using International Card CIN")
    public void internationalCardLoggedInFlowPPlus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        SavedCardHelpers.enableAllSavedCardFlags();

        MerchantType internationalMerchant = MerchantType.ALLPAYMODE;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        OrderDTO orderDTO = new OrderFactory.PGOnly(internationalMerchant, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD); //International Card CIN

        PGPHelpers.validateSuccessResponsePage(orderDTO, internationalMerchant, "CC", Bank.HDFC.toString(), Gateway.IHDF.toString());
        PGPHelpers.validateSuccessTxnStatus(orderDTO, "CC", Bank.HDFC.toString(), Gateway.IHDF.toString());
    }

    @Override
    @Parameters({"theme"})
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "PG side : Verify that international card is not visible on cashier page on international non supported MID for logged in & non logged in flow ")
    public void internationalCardNotVisibleLoggedInFlowPGSide(@Optional("enhancedweb_revamp") String theme) throws Exception {

        SavedCardHelpers.disableAllSavedCardFlags();

        MerchantType nonInternationalMerchant = MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        OrderDTO orderDTO = new OrderFactory.PGOnly(nonInternationalMerchant, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        //Deleting for user on PG side
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for user on PG side
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding for MID CustId on PG side
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), orderDTO.getCUST_ID(), nonInternationalMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertNotVisible();

    }


    @Override
    @Parameters({"theme"})
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "P+ side : Logged in & non logged in flow : Verify that international card is not visible on cashier page on international non supported MID ")
    public void internationalCardNotVisibleLoggedInFlowPPlus(@Optional("enhancedweb_revamp") String theme) throws Exception {

        SavedCardHelpers.enableAllSavedCardFlags();

        MerchantType nonInternationalMerchant = MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        OrderDTO orderDTO = new OrderFactory.PGOnly(nonInternationalMerchant, theme)
                .setCUST_ID(CommonHelpers.generateOrderId())
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.INTERNATIONAL_CARD_1);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Deleting for Merchant on P+
        SavedCardHelpers.deleteSavedCardsAlipay(nonInternationalMerchant.getId(), orderDTO.getCUST_ID());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(nonInternationalMerchant.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());


        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertNotVisible();

    }

    @Override
    @Parameters({"theme"})
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "P +,PGP side : Recon Success : Bajaj fn card is getting filtered from both the sides")
    public void bajajFinservFilteringAlipay(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType bajajfinemi = MerchantType.BAJAJFINEMI;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);

        //MID/CustId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForMidCustId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForMidCustId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForMidCustId");

        //UserId
        FF4JFlags.disable("shortCircuitSavedCardServiceReadForUserId");
        FF4JFlags.enable("fetchSavedcardFromPlatformForUserId");
        FF4JFlags.enable("returnSavedCardsFromPlatformForUserId");

        OrderDTO orderDTO = new OrderFactory.PGOnly(bajajfinemi, theme)
                .setCUST_ID(custId)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(BAJAJ_FINSERV_CREDIT_CARD_NUMBER);

        //Deleting for user on PG side
        SavedCardHelpers.deleteSavedCard(user);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on PGP Side
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding for MID CustId on PGP side
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        savedCardHelpers.saveCard_custId_mId(paymentDTO.getCreditCardNumber(), custId, bajajfinemi.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());

        //Adding for MID CustId on P+
        SavedCardHelpers.addCardAlipay(bajajfinemi.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertNotVisible();

    }


    @Override
    @Parameters({"theme"})
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "PG side: Verify that prepaid card should not be visible on cashier page if mid doesnt support it")
    public void prepaidCardNotVisibleUnsupportedMidPG(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType nonPrepaidMerchant = MerchantType.Hybrid;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        PGPHelpers.validate_MerchantPreference(nonPrepaidMerchant.getId(), "PREPAID_CARD", "N");
        SavedCardHelpers.disableAllSavedCardFlags();

        OrderDTO orderDTO = new OrderFactory.PGOnly(nonPrepaidMerchant, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Deleting for User on  PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, nonPrepaidMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertNotVisible();

        cashierPage.login(user);

        cashierPage.tabSavedCard().assertNotVisible();

    }


    @Override
    @Parameters({"theme"})
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "P + : Verify that prepaid card should not be visible on cashier page if mid doesnt support it")
    public void prepaidCardNotVisibleUnsupportedMid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType nonPrepaidMerchant = MerchantType.Hybrid;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        PGPHelpers.validate_MerchantPreference(nonPrepaidMerchant.getId(), "PREPAID_CARD", "N");
        SavedCardHelpers.enableAllSavedCardFlags();

        OrderDTO orderDTO = new OrderFactory.PGOnly(nonPrepaidMerchant, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding Prepaid Card for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Adding Prepaid Card for MID CustId on P+
        SavedCardHelpers.addCardAlipay(nonPrepaidMerchant.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertNotVisible();

        cashierPage.login(user);

        cashierPage.tabSavedCard().assertNotVisible();

    }


    @Override
    @Parameters({"theme"})
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "PG side: Verify that prepaid card should  be visible on cashier page if mid supports it")
    public void prepaidCardVisibleSupportedMidPG(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType prepaidMerchant = MerchantType.MASKED_MOBILE_ENABLED;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        PGPHelpers.validate_MerchantPreference(prepaidMerchant.getId(), "PREPAID_CARD", "Y");
        SavedCardHelpers.disableAllSavedCardFlags();

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidMerchant, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        //Deleting for Merchant on PGPDB
        SavedCardHelpers.deleteSavedCard(custId);

        //Deleting for User on  PGPDB
        SavedCardHelpers.deleteSavedCard(user);

        //Adding for MID CustId on PGPDB
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        SaveCardResponseBase saveCardResponseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), custId, prepaidMerchant.getId(), paymentDTO.getExpMonth() + paymentDTO.getExpYear());
        String cardId = saveCardResponseBase.getResponse().toString();

        //Adding for User on PGPDB
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

        cashierPage.login(user);

        cashierPage.tabSavedCard().assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO, prepaidMerchant);

        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);

    }


    @Override
    @Parameters({"theme"})
    @Feature("PGP-23196")
    @Owner("Tarun")
    @Epic(Sprint.SPRINT33_2)
    @Description("Automation JIRA : PGP-26960")
    @Test(description = "P + : Verify that prepaid card should  be visible on cashier page if mid supports it")
    public void prepaidCardVisibleSupportedMid(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType prepaidMerchant = MerchantType.MASKED_MOBILE_ENABLED;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        PGPHelpers.validate_MerchantPreference(prepaidMerchant.getId(), "PREPAID_CARD", "Y");
        SavedCardHelpers.enableAllSavedCardFlags();

        OrderDTO orderDTO = new OrderFactory.PGOnly(prepaidMerchant, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.PREPAID_CARD);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding Prepaid Card for user on P+ side
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        //Adding Prepaid Card for MID CustId on P+
        SavedCardHelpers.addCardAlipay(prepaidMerchant.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

        cashierPage.login(user);

        cashierPage.tabSavedCard().assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD);

        PrepaidHelpers.validateSuccessResponsePrepaidTxn(orderDTO, prepaidMerchant);

        PrepaidHelpers.validateSuccessTxnStatusPrepaidCard(orderDTO);

    }


    @Parameters({"theme"})
    @Feature("PGP-24165")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26959")
    @Test(description = "P + : Validate last successful updated time should be updated in merchant prod for non logged in flow when card is saved on MID")
    public void lastSuccessfulTime1(@Optional("enhancedweb_revamp") String theme) {

        String custId = CommonHelpers.generateOrderId();
        MerchantType pgOnly = MerchantType.PGOnly;

        SavedCardHelpers.enableAllSavedCardFlags();

        OrderDTO orderDTO = new OrderFactory.PGOnly(pgOnly, theme)
                .setCUST_ID(custId)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Adding Card for MID CustId on P+
        SavedCardHelpers.addCardAlipay(pgOnly.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getDebitCardNumber()).assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD);

        PGPHelpers.validateSuccessResponsePage(orderDTO, pgOnly, "DC", Bank.HDFCBANK.toString(), Gateway.HDFC.toString());

        Response merchantProd = SavedCardHelpers.fetchCardsAlipay(pgOnly.getId(), custId);
        Assertions.assertThat(merchantProd.jsonPath().getString("assetInfos.DC.lastSuccessfulUsedTime")).contains(CommonHelpers.getDate().toString());

    }


    @Parameters({"theme"})
    @Feature("PGP-24165")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26959")
    @Test(description = "P + : Validate last successful updated time should be updated in merchant prod and user biz prod for logged in flow when card is saved on MID + User Id")
    public void lastSuccessfulTime2(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType pgOnly = MerchantType.PGOnly;

        SavedCardHelpers.enableAllSavedCardFlags();

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pgOnly, theme)
                .setCUST_ID(custId)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Delete cards on user
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding Card for User on P+
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding Card for MID CustId on P+
        SavedCardHelpers.addCardAlipay(pgOnly.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD);

        PGPHelpers.validateSuccessResponsePage(orderDTO, pgOnly, "CC", Bank.HDFCBANK.toString(), Gateway.HDFC.toString());

        Response userProd = SavedCardHelpers.fetchCardsAlipay(user);
        Assertions.assertThat(userProd.jsonPath().getString("assetInfos.CC.lastSuccessfulUsedTime")).contains(CommonHelpers.getDate().toString());


        Response merchantProd = SavedCardHelpers.fetchCardsAlipay(pgOnly.getId(), custId);
        Assertions.assertThat(merchantProd.jsonPath().getString("assetInfos.CC.lastSuccessfulUsedTime")).contains(CommonHelpers.getDate().toString());

    }


    @Parameters({"theme"})
    @Feature("PGP-24165")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26959")
    @Test(description = "P + : Validate last successful updated time should  be updated in user biz and not in merchant biz for logged in flow when card is saved on UserId")
    public void lastSuccessfulTime3(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType pgOnly = MerchantType.PGOnly;

        SavedCardHelpers.enableAllSavedCardFlags();

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(pgOnly, theme)
                .setCUST_ID(custId)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Delete cards on user
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding Card for User on P+
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD);

        PGPHelpers.validateSuccessResponsePage(orderDTO, pgOnly, "CC", Bank.HDFCBANK.toString(), Gateway.HDFC.toString());

        Response userProd = SavedCardHelpers.fetchCardsAlipay(user);
        Assertions.assertThat(userProd.jsonPath().getString("assetInfos.CC.lastSuccessfulUsedTime")).contains(CommonHelpers.getDate().toString());

        Response merchantProd = SavedCardHelpers.fetchCardsAlipay(pgOnly.getId(), custId);
        Assertions.assertThat(merchantProd.jsonPath().getString("assetInfos.CC.lastSuccessfulUsedTime")).doesNotContain(CommonHelpers.getDate().toString());

    }


    @Parameters({"theme"})
    @Feature("PGP-24165")
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26959")
    @Test(description = "P + : Validate last successful updated time should not be updated in merchant prod but for user biz prod for logged in flow when store card pref is OFF")
    public void lastSuccessfulTime4(@Optional("enhancedweb_revamp") String theme) throws Exception {

        String custId = CommonHelpers.generateOrderId();
        MerchantType storeCardNo = MerchantType.HYBRID_PEON_DISABLED;
        SavedCardHelpers.assertStoreCardPrefDisabled(storeCardNo);

        SavedCardHelpers.enableAllSavedCardFlags();

        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO = new OrderFactory.PGOnly(storeCardNo, theme)
                .setCUST_ID(custId)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();

        //Delete cards on user
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding Card for User on P+
        SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());

        //Adding Card for MID CustId on P+
        SavedCardHelpers.addCardAlipay(storeCardNo.getId(), orderDTO.getCUST_ID(), paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.savedCard(paymentDTO.getCreditCardNumber()).assertVisible();

        cashierPage.payBy(PayMode.SAVED_CARD);

        PGPHelpers.validateSuccessResponsePage(orderDTO, storeCardNo, "CC", Bank.HDFCBANK.toString(), Gateway.HDFC.toString());

        Response userProd = SavedCardHelpers.fetchCardsAlipay(user);
        Assertions.assertThat(userProd.jsonPath().getString("assetInfos.CC.lastSuccessfulUsedTime")).contains(CommonHelpers.getDate().toString());

        Response merchantProd = SavedCardHelpers.fetchCardsAlipay(storeCardNo.getId(), custId);
        Assertions.assertThat(merchantProd.jsonPath().getString("assetInfos.CC.lastSuccessfulUsedTime")).doesNotContain(CommonHelpers.getDate().toString());

    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28964")
    @Description("Headers in UIMicroservices api")
    @Parameters({"theme"})
    @Test(description = "verify Cashier Page When Data Is Encoded By UImicroservice that is MID is present in ff4J flag(theia.uimicroservice.enhancedflow.feature)")
    public void PGP_28964_verifyCashierPageWhenDataIsEncodedByUImicroservice(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28964")
    @Description("Headers in UIMicroservices api")
    @Parameters({"theme"})
    @Test(description = "verify Cashier Page When Data Is Not Encoded By UImicroservice that is MID is not present in ff4J flag(theia.uimicroservice.enhancedflow.feature)")
    public void PGP_28964_verifyCashierPageWhenDataIsNotEncodedByUImicroservice(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Hybrid, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.Hybrid.getKey())
                .assertAll();
    }

    @Feature("PGP-28971")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate risk extended info URL parameters under theia facade logs for enhancedweb")
    public void validate_RiskExtendedURLInfoTheiaFacade(@Optional("enhancedweb") String theme) throws InterruptedException, IOException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"REQUEST\"| grep \"risk\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("registeredAppURL");
        Assertions.assertThat(theiaFacadeLogs).contains("registeredWebURL");
        Assertions.assertThat(theiaFacadeLogs).contains("callbackURL");
        Assertions.assertThat(theiaFacadeLogs).contains("refererURL");
    }


    @Feature("PGP-27610")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Verify UPI is returned as paymode incase of Addnpay when UPI is not enabled for 01 product code")
    public void validateUPITxnForAddNPAYWhenUPINotEnabled(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.BIN_IN_RESPONSE_ADDNPAY.getKey())
                .assertAll();
    }

    @Feature("PGP-27610")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Verify UPI is returned as paymode incase of Addnpay when UPI is enabled for 01 product code")
    public void validateUPITxnForAddNPAYWhenUPIEnabled(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .assertAll();
    }

    @Feature("PGP-27610")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Verify UPI is returned as paymode incase of Addnpay when UPI is not enabled for 04 product code")
    public void validateUPITxnForAddNPAYWhenUPINotEnabledForSubs(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.SUBSCRIPTION_WALLET_LIMIT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.SUBSCRIPTION_WALLET_LIMIT.getKey())
                .assertAll();
    }

    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that the value of the new flag is true when pref is enabled on merchant")
    public void validateNewFlagIsTrueWhenPrefEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.AddnPay;
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "ADDANDPAY_WITH_UPI_COLLECT", "Y");
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " + "grep \"" + orderDTO.getMID() + "\" | grep \"LITEPAYVIEW_CONSULT\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("\"addAndPayMigration\":\"true\""));
    }


    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that the value of the new flag won't come when pref is disabled  on merchant")
    public void validateUPITxwForAddNPAYWhenUPINotEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.ADD_MONEY_WITH_RETRY;
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "ADDANDPAY_WITH_UPI_COLLECT", "N");
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"LITEPAYVIEW_CONSULT\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertFalse(theiaFacadeLogs.contains("addAndPayMigration"));
    }

    @Feature("PGP-27609")
    @Owner(GAGANDEEP)
    @Test(description = "Validate that all UPI paymode will come in LPV response when pref is enabled on merchant")
    public void validateUPIPayModesWhenPrefEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.AddnPay;
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "ADDANDPAY_WITH_UPI_COLLECT", "Y");
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        //WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);

        String theiaFacadeLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderDTO.getORDER_ID().toString(), "LITEPAYVIEW_CONSULT");

        //String theiaFacadeLogs=  verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID().toString(),"LITEPAYVIEW_CONSULT");

        Assert.assertTrue(theiaFacadeLogs.contains("UPIPUSH"));
        Assert.assertTrue(theiaFacadeLogs.contains("UPI"));
        Assert.assertTrue(theiaFacadeLogs.contains("UPIPUSHEXPRESS"));
    }

    @Feature("PGP-14961")
    @Parameters({"theme"})
    @Test(description = "Validate that the txn response when Invalid channelId id provide")
    public void validateTheResponseWhenInavlidChannelIdIsProvided(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.AddnPay(merchantType.AddnPay, "WWW")
                .setCHANNEL_ID("WWW")
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
        responsePage.validateRespCode("327");
        responsePage.validateRespMsg("Invalid channel");

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify OnPaytm=true in promo payload in queue handler service after Successful CC Txn with ONUS merchant")
    public void PGP_28980_verifyNotificationQueueHandleraferPGOnlyCCtxnONUSMerchant(@Optional("enhancedweb") String theme) throws InterruptedException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly_Retry, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"PaymentPromoServiceImpl.pushPayloadInKafka()\" | grep \"NATIVE_PAY\"";
        String notificationQueueHandlerlogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd);
        Assertions.assertThat(notificationQueueHandlerlogs).contains("\"onPaytm\":true");

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify OnPaytm=false in promo payload in queue handler service after Successful Hybrid Txn with OFFUS merchant")
    public void PGP_28980_verifyNotificationQueueHandleraferPGOnlyHybridtxnOFFUSMerchant(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.Hybrid, theme, user)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 0.50);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validatePaymentMode("HYBRID")
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"PaymentPromoServiceImpl.pushPayloadInKafka()\" | grep \"NATIVE_PAY\"";
        String notificationQueueHandlerlogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd);
        Assertions.assertThat(notificationQueueHandlerlogs).contains("\"onPaytm\":false");

        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new com.paytm.api.Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(orderDTO.getORDER_ID());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "test PPBL pay mode is disabled when user has insufficient PPBL balance")
    public void testPPBLPayModeIsDisabledWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        Assertions.assertThat(cashierPage.checkboxPPBL().isEnabled()).as("PPBL paymode is disabled").isFalse();
    }

    @Parameters({"theme"})
    @Test(description = "test err msg is displayed when user has insufficient PPBL balance")
    public void testErrMsgIsDisplayedWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        Assertions.assertThat(cashierPage.tabPPBL().content().toString()).as("Getting insufficient PPBL balance msg").contains("You do not have enough balance for this payment");
    }

    @Parameters({"theme"})
    @Test(description = "test next pay mode is selected when user has insufficient PPBL balance")
    public void testNextPayModeIsSelectedWhenUserHasInsufficientPPBLBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(Constants.PPBL_ACCOUNT_BALANCE + 1))
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        pageWait.apply(cashierPage.tabPPBL().isVisible());
        WebElement payModeNextToPPBL = DriverManager.getDriver().findElement(By.cssSelector("section[id=ptm-ppb]"));
        Assertions.assertThat(payModeNextToPPBL.getAttribute("class").contains("active")).as("Paymode next to PPBL is selected").isTrue();
    }

    @Feature("PGP-29616")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "test newly added UDF_2 parameters is passed to P+")
    public void testNewlyAddedUDF2Params(@Optional("enhancedweb") String theme) throws Exception {

        String UDF_2 = "default_udf2";
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setUDF_2(UDF_2)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateUDF(UDF_2)
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Bank.HDFC.toString())
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .assertAll();
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessCCTxn(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String payMode = "Credit Card";
        Assertions.assertThat(cashierPage.tabCard(1).content().getValue()).contains(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        cashierPage.tabCard(1).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.textBoxCardNumber().clearAndType(ccDetails.getCreditCardNumber());
        cashierPage.fillExpiryMonth(ccDetails.getExpMonth());
        cashierPage.fillExpiryYear(ccDetails.getExpYear().substring(2, 4));
        cashierPage.textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForCCTxnWhenCardNumberIsNotEntered(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCard(1).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.fillExpiryMonth(ccDetails.getExpMonth());
        cashierPage.fillExpiryYear(ccDetails.getExpYear().substring(2, 4));
        cashierPage.textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText()).contains(new LanguageTranslationAPI(Language.HINDI, PLEASE_ENTER_YOUR_CARD_NUMBER).execute().jsonPath().getString("translations['" + PLEASE_ENTER_YOUR_CARD_NUMBER + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForCCTxnWhenExpiryDetailsIsNotEntered(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCard(1).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.textBoxCardNumber().clearAndType(ccDetails.getCreditCardNumber());
        cashierPage.textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText()).contains(new LanguageTranslationAPI(Language.HINDI, ENTER_EXPIRY_DATE).execute().jsonPath().getString("translations['" + ENTER_EXPIRY_DATE + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForCCTxnWhenCVVIsNotEntered(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCard(1).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.textBoxCardNumber().clearAndType(ccDetails.getCreditCardNumber());
        cashierPage.fillExpiryMonth(ccDetails.getExpMonth());
        cashierPage.fillExpiryYear(ccDetails.getExpYear().substring(2, 4));
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText()).contains(new LanguageTranslationAPI(Language.HINDI, ENTER_CVV).execute().jsonPath().getString("translations['" + ENTER_CVV + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessDCTxn(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String payMode = "Debit Card";
        Assertions.assertThat(cashierPage.tabCard(0).content().getValue()).contains(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        cashierPage.tabCard(0).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.textBoxCardNumber().clearAndType(ccDetails.getDebitCardNumber());
        cashierPage.fillExpiryMonth(ccDetails.getExpMonth());
        cashierPage.fillExpiryYear(ccDetails.getExpYear().substring(2, 4));
        cashierPage.textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForDCTxnWhenCardNumberIsNotEntered(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCard(0).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.fillExpiryMonth(ccDetails.getExpMonth());
        cashierPage.fillExpiryYear(ccDetails.getExpYear().substring(2, 4));
        cashierPage.textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText()).contains(new LanguageTranslationAPI(Language.HINDI, PLEASE_ENTER_YOUR_CARD_NUMBER).execute().jsonPath().getString("translations['" + PLEASE_ENTER_YOUR_CARD_NUMBER + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForDCTxnWhenExpiryDetailsIsNotEntered(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCard(0).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.textBoxCardNumber().clearAndType(ccDetails.getDebitCardNumber());
        cashierPage.textBoxCVVNumber().clearAndType(ccDetails.getCvvNumber());
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText()).contains(new LanguageTranslationAPI(Language.HINDI, ENTER_EXPIRY_DATE).execute().jsonPath().getString("translations['" + ENTER_EXPIRY_DATE + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForDCTxnWhenCVVIsNotEntered(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCard(0).click();
        PaymentDTO ccDetails = new PaymentDTO();
        cashierPage.textBoxCardNumber().clearAndType(ccDetails.getDebitCardNumber());
        cashierPage.fillExpiryMonth(ccDetails.getExpMonth());
        cashierPage.fillExpiryYear(ccDetails.getExpYear().substring(2, 4));
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText()).contains(new LanguageTranslationAPI(Language.HINDI, ENTER_CVV).execute().jsonPath().getString("translations['" + ENTER_CVV + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessNBTxn(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String payMode = "Net Banking";
        Assertions.assertThat(cashierPage.tabNetBanking().getText()).contains(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        cashierPage.payBy(PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForNBTxnWhenNoBankIsSelected(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.paymentContainer().getText()).contains(new LanguageTranslationAPI(Language.HINDI, PLEASE_SELECT_A_BANK_TO_PROCEED).execute().jsonPath().getString("translations['" + PLEASE_SELECT_A_BANK_TO_PROCEED + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessPPITxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setLocale(LOCALE_HINDI)
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String payMode = "Paytm Balance";
        Assertions.assertThat(cashierPage.tabPPI().content().getValue().split("\n")[0]).isEqualTo(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        cashierPage.payBy(PayMode.WALLET);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForPPITxnWhenUserDoesNotHaveEnoughBalanceInWallet(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setLocale(LOCALE_HINDI)
                .build();
        WalletHelpers.modifyBalance(user, 0D);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.tabPPI().content().getValue()).contains(new LanguageTranslationAPI(Language.HINDI, NOT_ENOUGH_BALANCE).execute().jsonPath().getString("translations['" + NOT_ENOUGH_BALANCE + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessUPITxn(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        String payMode = "UPI";
        Assertions.assertThat(cashierPage.tabUPI().getText()).isEqualTo(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedErrorMsgForUPITxnWhenVPAIsNotEntered(@Optional("enhancedweb") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().waitUntilClickable();
        cashierPage.tabUPI().click();
        cashierPage.buttonPGPayNow().click();
        Assertions.assertThat(cashierPage.lblVPAErrMsg().content().getValue()).isEqualTo(new LanguageTranslationAPI(Language.HINDI, PLEASE_ENTER_UPI_ID).execute().jsonPath().getString("translations['" + PLEASE_ENTER_UPI_ID + "']"));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessPPBLTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String payMode = "Paytm Payments Bank";
        Assertions.assertThat(cashierPage.tabPPBL().content().getValue().split("\n")[0]).isEqualTo(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        cashierPage.payBy(PayMode.PPBL);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessPDCTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String payMode = "Paytm Postpaid";
        Assertions.assertThat(cashierPage.tabPDC().content().getValue().split("\n")[0]).isEqualTo(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        cashierPage.payBy(PayMode.PAYTM_DIGITAL_CARD);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner(DEEPAK)
    @Parameters({"theme"})
    @Test
    public void testLocalisedSuccessEMITxn(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MERCHANT_HAVING_LOCALISATION_ENABLED, theme)
                .setLocale(LOCALE_HINDI)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        String payMode = "EMI";
        Assertions.assertThat(cashierPage.tabEMI().getText()).isEqualTo(new LanguageTranslationAPI(Language.HINDI, payMode).execute().jsonPath().getString("translations['" + payMode + "']"));
        cashierPage.payBy(PayMode.EMI);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
        super.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .body(RESP_CODE, equalTo(SUCCESS_CODE));
    }

    @Owner("Sakshi")
    @Parameters({"theme"})
    @Test(description = "Verify that if only UPI is sent in 'enablePaymentMode' param then All-in-one UPI QR should be displayed on cashier page")
    public void verifyAllInOneUpiQr(@Optional("enhancedweb") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC, theme)
                .setPAYMENT_MODE_ONLY("YES")
                .setPAYMENT_TYPE_ID("UPI")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<UIElement> payModeList = cashierPage.listOfPayModes();
        Assertions.assertThat(payModeList.size()).isEqualTo(1);
        Assertions.assertThat(payModeList.get(0).getText()).containsIgnoringCase("Upi").as("Only Upi paymode should be displayed");
        Assertions.assertThat(cashierPage.imgPaytmQRSymbol().isDisplayed())
                .isTrue();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31212")
    @Parameters({"theme"})
    @Test(description = "validate successful tansaction using payment link")
    public void ValidateSuccessfulLinkPayment(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        MerchantType merchant = Constants.MerchantType.PPI2;
        String txnAmount = "1.0";
        String LinkId = "4706";
        LinkPaymentSendOTP sendOtp = new LinkPaymentSendOTP(merchant.getId(), user.mobNo(), merchant.name(), txnAmount, LinkId);
        JsonPath SendOTPResponse = sendOtp.execute().jsonPath();
        Assertions.assertThat(SendOTPResponse.getString("status")).as("Status should be SUCCESS").isEqualTo("SUCCESS");
        //  LinkPaymentValidateOTP validateOTP = new LinkPaymentValidateOTP(AuthUtil.getOtp(user.mobNo()), SendOTPResponse.getString("state"), SendOTPResponse.getString("uniqueId"));
        LinkPaymentValidateOTP validateOTP = new LinkPaymentValidateOTP("123456", SendOTPResponse.getString("state"), SendOTPResponse.getString("uniqueId"));
        JsonPath ValidateOTPResponse = validateOTP.execute().jsonPath();
        Assertions.assertThat(ValidateOTPResponse.getString("status")).as("Status should be SUCCESS").isEqualTo("SUCCESS");
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), theme)
                .setShortUrl("https://paytm.me/nWd-v7O")
                .setLongUrl("https://pgp-automation.paytm.in/link/abhishek/LL_4706")
                .setLinkName("abhishek")
                .setLinkDescription("party")
                .setLINKID(LinkId)
                .setCUST_ID(ValidateOTPResponse.getString("custId"))
                .setREQUEST_TYPE("LINK_BASED_PAYMENT")
                .setORDER_ID(ValidateOTPResponse.getString("orderId"))
                .setCallBack_URL("https://pgp-automation.paytm.in/theia/linkPaymentRedirect")
                .setSSO_TOKEN(ValidateOTPResponse.getString("ssoToken"))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        LinkPaymentResponsePage responsePage = new LinkPaymentResponsePage();
        String uiSuccessMessage = responsePage.textSuccessMessage().getText();
        Assertions.assertThat(uiSuccessMessage).isEqualToIgnoringCase("Paid Successfully");
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), ValidateOTPResponse.getString("orderId"));
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(ValidateOTPResponse.getString("orderId"))
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(merchant.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31212")
    @Parameters({"theme"})
    @Test(description = "Verify UPI paymode is not visible for PPI2 Merchant in link payment cashier page")
    public void VerifyUPINotVisibleOnLinkPaymentCashierPage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        MerchantType merchant = Constants.MerchantType.PPI2;
        String txnAmount = "1.0";
        String LinkId = "4706";
        LinkPaymentSendOTP sendOtp = new LinkPaymentSendOTP(merchant.getId(), user.mobNo(), merchant.name(), txnAmount, LinkId);
        JsonPath SendOTPResponse = sendOtp.execute().jsonPath();
        Assertions.assertThat(SendOTPResponse.getString("status")).as("Status should be SUCCESS").isEqualTo("SUCCESS");
        //    LinkPaymentValidateOTP validateOTP = new LinkPaymentValidateOTP(AuthUtil.getOtp(user.mobNo()), SendOTPResponse.getString("state"), SendOTPResponse.getString("uniqueId"));
        LinkPaymentValidateOTP validateOTP = new LinkPaymentValidateOTP("123456", SendOTPResponse.getString("state"), SendOTPResponse.getString("uniqueId"));
        JsonPath ValidateOTPResponse = validateOTP.execute().jsonPath();
        Assertions.assertThat(ValidateOTPResponse.getString("status")).as("Status should be SUCCESS").isEqualTo("SUCCESS");
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), theme)
                .setShortUrl("https://paytm.me/nWd-v7O")
                .setLongUrl("https://pgp-automation.paytm.in/link/abhishek/LL_4706")
                .setLinkName("abhishek")
                .setLinkDescription("party")
                .setLINKID(LinkId)
                .setCUST_ID(ValidateOTPResponse.getString("custId"))
                .setREQUEST_TYPE("LINK_BASED_PAYMENT")
                .setORDER_ID(ValidateOTPResponse.getString("orderId"))
                .setCallBack_URL("https://pgp-automation.paytm.in/theia/linkPaymentRedirect")
                .setSSO_TOKEN(ValidateOTPResponse.getString("ssoToken"))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
    }


    @Feature("PGP-30576")
    @Owner("Sakshi")
    @Parameters({"theme"})
    @Test(description = "Fetch top 8 banks for NB FPO and App_Data")
    public void validate_NbCount(@Optional("enhancedweb") String theme) throws InterruptedException, IOException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        int count = cashierPage.listOfNbchannel().size();
        Assertions.assertThat(count).as("count mismatch of listOfNbchannel").isEqualTo(8);
        SoftAssertions softAssertions = new SoftAssertions();
        for (int i = 0; i < count; i++) {
            HashMap<Object, Object> map = cashierPage.getPushAppData().getJsonObject("merchantPayModes.data[5].banks[" + i + "]");
            softAssertions.assertThat(map.get("iconUrl")).as("Icon Url is missing for NB bank in push app data").isNotNull();
            softAssertions.assertThat(map.get("channelCode")).as("Channel code is missing for NB bank in push app data").isNotNull();
            softAssertions.assertThat(map.get("channelName")).as("Channel Name is missing for NB bank in push app data").isNotNull();
        }
        softAssertions.assertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31212")
    @Parameters({"theme"})
    @Test(description = "Verify UPI paymode is not visible for PPI2 Merchant in invoice link cashier page")
    public void VerifyUPINotVisibleInvoiceLinkCashierPage(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        MerchantType merchant = MerchantType.PPI2;
        String txnAmount = "1.0";
        String LinkId = "4738";
        LinkPaymentSendOTP sendOtp = new LinkPaymentSendOTP(merchant.getId(), user.mobNo(), merchant.name(), txnAmount, LinkId);
        JsonPath SendOTPResponse = sendOtp.execute().jsonPath();
        Assertions.assertThat(SendOTPResponse.getString("status")).as("Status should be SUCCESS").isEqualTo("SUCCESS");
        //    LinkPaymentValidateOTP validateOTP = new LinkPaymentValidateOTP(AuthUtil.getOtp(user.mobNo()), SendOTPResponse.getString("state"), SendOTPResponse.getString("uniqueId"));
        LinkPaymentValidateOTP validateOTP = new LinkPaymentValidateOTP("123456", SendOTPResponse.getString("state"), SendOTPResponse.getString("uniqueId"));
        JsonPath ValidateOTPResponse = validateOTP.execute().jsonPath();
        Assertions.assertThat(ValidateOTPResponse.getString("status")).as("Status should be SUCCESS").isEqualTo("SUCCESS");
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant.getId(), theme)
                .setShortUrl("https://paytm.me/XY-hJj0")
                .setLongUrl("https://pgp-automation.paytm.in/link/invoice/abhishek/LL_4738")
                .setLinkName("abhishek")
                .setLinkDescription("party")
                .setLINKID(LinkId)
                .setCUST_ID(ValidateOTPResponse.getString("custId"))
                .setREQUEST_TYPE("LINK_BASED_PAYMENT")
                .setORDER_ID(ValidateOTPResponse.getString("orderId"))
                .setCallBack_URL("https://pgp-automation.paytm.in/theia/linkPaymentRedirect")
                .setSSO_TOKEN(ValidateOTPResponse.getString("ssoToken"))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31212")
    @Parameters({"theme"})
    @Test(description = "validate successful tansaction using Native_MF Link")
    public void ValidateSuccessfulNativeMFLinkPayment(@Optional("enhancedweb") String theme) throws Exception {
        MerchantType merchant = MerchantType.PPI2_MF;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeMF(merchant.getId(), merchant.getKey(), orderId, "2379", "7777777777", "true", "true").execute().jsonPath().getString("body.txnToken");
        DriverManager.getDriver().get(LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(merchant.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31212")
    @Parameters({"theme"})
    @Test(description = "Verify UPI paymode is not visible for PPI2 Merchant in Native_MF link payment cashier page")
    public void VerifyUPINotVisibleNative_MFCashierPage(@Optional("enhancedweb") String theme) throws Exception {
        MerchantType merchant = MerchantType.PPI2_MF;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeMF(merchant.getId(), merchant.getKey(), orderId, "2379", "7777777777", "true", "true").execute().jsonPath().getString("body.txnToken");
        DriverManager.getDriver().get(LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31212")
    @Parameters({"theme"})
    @Test(description = "validate successful tansaction using Native_ST Link")
    public void ValidateSuccessfulNativeSTLinkPayment(@Optional("enhancedweb") String theme) throws Exception {
        MerchantType merchant = MerchantType.STOCK_TRADE;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeST(merchant.getId(), merchant.getKey(), orderId, "2380", "7777777777", "false", "true").execute().jsonPath().getString("body.txnToken");
        DriverManager.getDriver().get(LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchant.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Bank.HDFC.toString())
                .validateMid(merchant.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .AssertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-31212")
    @Parameters({"theme"})
    @Test(description = "Verify UPI paymode is not visible for PPI2 Merchant in Native_ST cashier page")
    public void VerifyUPINotVisibleNative_STCashierPage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        MerchantType merchant = MerchantType.STOCK_TRADE;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeST(merchant.getId(), merchant.getKey(), orderId, "2380", "7777777777", "false", "true").execute().jsonPath().getString("body.txnToken");
        DriverManager.getDriver().get(LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken));
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
    }

    @Owner(ABHAY)
    @Feature("PGP-27607")
    @Parameters({"theme"})
    @Test(description = "Verify isAddNPay flag when addnPay is done through UPI")
    public void VerifyisAddNPayFlagForADDNPAYThroughUPI(@Optional("enhancedwap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.AddnPay, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(orderDTO.getTXN_AMOUNT()) - 1);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.AddnPay.getKey())
                .assertAll();
        String tsnId = responsePage.textTxnID().getText();

        String grepcmd = "grep \"" + tsnId + "\" /paytm/logs/instaproxy.log | " +
                "grep \"extendInfo\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        String[] ar = theiaFacadeLogs.split("passThroughExtendInfo");
        String[] ar2 = ar[1].split("\"");
        String decodedString = PGPHelpers.Base64Decode(ar2[2]);
        Assertions.assertThat(decodedString).contains("\"isAddNPay\":\"true\"");
    }


    @Owner(PRIYANSHI)
    @Feature("PGP-32355")
    @Parameters({"theme"})
    @Test(description = "Validate that TxnPaidTime should be return in response of call back api for CC txn ")
    public void VerifyTxnPaidTimeFieldShouldReturnInResponse_ViaCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateCheckSum(MerchantType.PGOnly.getKey())
                .validateTxnDate(new Date())
                .assertAll();
    }


    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab not coming up on cashierPage in nonloggedin flow")
    public void VerifyUPICollect_Tab(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI, theme)

                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab coming up on cashierPage in loggedin flow")
    public void VerifyUPICollect_Tab_LoggedIn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertVisible();

    }

    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab comes up when pref is disabled on merchant without loggedin flow")
    public void VerifyUPICollect_Tab_when_PrefDisabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertVisible();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab comes up when pref is disabled on merchant in loggedin flow")
    public void VerifyUPICollect_Tab_When_PrefDisabled_LoggedIn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertVisible();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab comes up after login")
    public void VerifyUPICollect_Via_Login(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.login(user.mobNo());
        cashierPage.tabUPI().assertVisible();
    }

    @Owner(PAYAL)
    @Feature("PGP-35336")
    @Parameters({"theme"})
    @Test(description = "Verify payButton with selected Paymode")
    public void ValidatePayButtonWithSelectedPaymode(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.radioButtonPaytmPostpaid().click();
        cashierPage.PayButtonWithPostPaid().assertVisible();
        cashierPage.checkBoxPPI().check();
        cashierPage.PayButtonWithWallet().assertVisible();
        cashierPage.tabPPBL().click();
        cashierPage.PayButtonWithPPBL().assertVisible();
    }

    @Owner(PAYAL)
    @Feature("PGP-35336")
    @Parameters({"theme"})
    @Test(description = "Verify payButton with selected Paymode for Saved Card")
    public void ValidatePayButtonWithSelectedPaymodeAsSC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVECARDMIGRATION);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        cashierPage.PayButtonWithSC().assertVisible();
    }

    @Owner(AAYUSH)
    @Parameters({"theme"})
    @Test(description = "Verify after success ADD_MONEY txn, theia_facade log has sourceName=3pm")
    public void PGP_27655verifyTheia_facade_logs(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC, Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.AddMoney(MerchantType.AddMoney, theme, user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC, paymentDTO);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"WALLET\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("3pm"));
    }

    @Owner(AAYUSH)
    @Parameters({"theme"})
    @Test(description = "Verify when cashier page is loaded, theia_facade log has peon Url correctly passed")
    public void PGP_35341_validate_peon_url_in_theia_facade(@Optional("enhancedwap_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() + "\" | grep \"CREATE_ORDER\" | grep \"REQUEST\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("\\\"peonURL\\\":\\\"https://pgp-automation.paytm.in/mockbank/peon\\\""));
    }

    @Owner(PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify Login strip is not visible or not when Pref is enabled for merchant")
    public void verify_Login_Strip_When_PrefEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DISABLED_LOGIN_STRIP_PREF, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertNotVisible();
    }

    @Owner(PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify Login strip is not visible or not when Flag is enabled for merchant")
    public void verify_Login_Strip_When_FlagEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DISABLED_LOGIN_STRIP_FLAG_ENABLED, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.loginStrip().assertNotVisible();
    }

    @Owner(POOJA)
    @Feature("PGP-35359")
    @Parameters({"theme"})
    @Test(description = "Login Screen changes")
    public void loginScreenChanges(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(PGOnly_Retry, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.scrollToElement(cashierPage.postPaid());
        cashierPage.pause(4);
        cashierPage.postPaid().assertVisible();
        cashierPage.wallet().assertVisible();
        cashierPage.cards().assertVisible();
        cashierPage.newPaymentMethod().assertVisible();
        cashierPage.uPIBankAC().assertVisible();

    }

    @Owner(PAYAL)
    @Feature("PGP-35337")
    @Parameters({"theme"})
    @Test(description = "Verify enter Paytm Bank Passcode message")
    public void ValidatePPBLPasscodeTxt(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabPPBL().click();
        Assertions.assertThat(cashierPage.enterPPBLPasscodeMessage().getText()).isEqualTo("Enter the passcode used to access your Paytm Bank A/c");
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-35443")
    @Parameters({"theme"})
    @Test(description = "Verify when PPBL balance + FD balance amount is equal or greater than order amount then FD Balance should be visible")
    public void verifying_FD_BalanceIsVisible(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FD_PAYMODE;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("3010")
                .build();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabPPBL().isVisible();
        cashierPage.linkedFDBalance().assertVisible();
        cashierPage.redemptionTextLabel().assertVisible();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-35443")
    @Parameters({"theme"})
    @Test(description = "Verify when PPBL balance + FD balance amount is less than order amount then FD Balance should not be visible")
    public void verifying_FD_BalanceIsNotVisible(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FD_PAYMODE;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT(user.ssoToken())
                .setTXN_AMOUNT("4010")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabPPBL().isVisible();
        cashierPage.linkedFDBalance().assertNotVisible();
        cashierPage.redemptionTextLabel().assertNotVisible();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-35391")
    @Parameters({"theme"})
    @Test(description = "Verifying PayModes LOGO colors")
    public void Verifying_PayMode_LOGO_Colors(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.getCardLogo().waitUntilVisible();
        Assertions.assertThat(cashierPage.getCardLogo().getAttribute("src")).as("Logo is not colored").contains("card-icon.png");
        Assertions.assertThat(cashierPage.getEMILogo().getAttribute("src")).as("Logo is not colored").contains("emi-icon.png");
        Assertions.assertThat(cashierPage.getUpiLogo().getAttribute("src")).as("Logo is not colored").contains("upi-icon.png");
        Assertions.assertThat(cashierPage.getNBLogo().getAttribute("src")).as("Logo is not colored").contains("nb-icon.png");
    }


//     @Owner(AAYUSH)
//     @Feature("PGP-35443")
//     @Parameters({"theme"})
//     @Test(description = "Verify success transaction through FD when txn amt is greater than PPBL balance and less than PPBL+FD balance",enabled=false) // PPBL is not supported
    public void verifying_FD_As_Paymode(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.FD_PAYMODE;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("3001")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.textBoxPPBLPassCode().waitUntilEditable();
        cashierPage.textBoxPPBLPassCode().clearAndType("3315");
        cashierPage.buttonPpblSumbit().waitUntilClickable();
        cashierPage.buttonPpblSumbit().click();
        cashierPage.pause(1);
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
                .validateGatewayName("PPBL")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("PPBL")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Owner(AJEESH)
    @Feature("PGP-35306")
    @Parameters({"theme"})
    @Test(description = "Verify VPAHASH is sent in peon for merchant with pref CUSTOMER_IDENTIFIER_HASH_ENABLED enabled")
    public void TC001_VerifyVPAHASHissentinPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String vpaHashValue = "a42a5bdc4d012f5df4e463bdf9d2062a20b9ca43a2782e6cd46f55f80c47f661";
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.VPA_RETRY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"Peon Request payload:HttpRequestPayload\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);

        Assertions.assertThat(logs).contains("VPAHASH");
        Assertions.assertThat(logs).contains(vpaHashValue);
    }

    @Owner(AJEESH)
    @Feature("PGP-35306")
    @Parameters({"theme"})
    @Test(description = "Verify CUSTIDHASH is sent in peon for merchant with pref CUSTOMER_IDENTIFIER_HASH_ENABLED enabled")
    public void TC002_VerifyCUSTIDHASHissentinPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        String custIDHashValue = "5f03dca39f3ee1d4a661a8a4f75dcfffc9396342a3c15c1a33300f8341e5583d";
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.VPA_RETRY, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID("Test101")
                .build();
        WalletHelpers.modifyBalance(user, 10.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"Peon Request payload:HttpRequestPayload\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("CUSTIDHASH");
        Assertions.assertThat(logs).contains(custIDHashValue);
    }

    @Owner(AJEESH)
    @Feature("PGP-35306")
    @Parameters({"theme"})
    @Test(description = "Verify VPAHASH is not sent in peon for merchant with pref CUSTOMER_IDENTIFIER_HASH_ENABLED Disabled")
    public void TC003_VerifyVPAHASHisnotsentinPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        MerchantType merchantType = MerchantType.PGOnly;
        String vpaHashValue = "205f1a17cf7f53c2b4248abb70f90cc06a92d2137928e36f6c7f53ee24c61e64";
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "CUSTOMER_IDENTIFIER_HASH_ENABLED", "N");
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("10.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"Peon Request payload:HttpRequestPayload\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).doesNotContain("VPAHASH");
        Assertions.assertThat(logs).doesNotContain(vpaHashValue);
    }

    @Owner(AJEESH)
    @Feature("PGP-35306")
    @Parameters({"theme"})
    @Test(description = "Verify VPAHASH is not sent in peon for merchant with pref CUSTOMER_IDENTIFIER_HASH_ENABLED Disabled")
    public void TC004_VerifyCUSTIDHASHisnotsentinPeon(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        MerchantType merchantType = MerchantType.PGOnly;
        String custIDHashValue = "5f03dca39f3ee1d4a661a8a4f75dcfffc9396342a3c15c1a33300f8341e5583d";
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), "CUSTOMER_IDENTIFIER_HASH_ENABLED", "N");
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchantType, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setCUST_ID("Test101")
                .build();
        WalletHelpers.modifyBalance(user, 10.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"Peon Request payload:HttpRequestPayload\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).doesNotContain("CUSTIDHASH");
        Assertions.assertThat(logs).doesNotContain(custIDHashValue);
    }


    @Owner(PAYAL)
    @Feature("PGP-35983")
    @Parameters({"theme"})
    @Test(description = "Verify PUSH_APP_DATA, with groupedMerchantPayOption")
    public void ValidateInstrumentCategorization(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVECARDMIGRATION);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Instrument_Categorization, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantGroupedPayModes")).isNotNull();
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantPayModes")).isNotNull();
        Assertions.assertThat(cashierPage.getPushAppData().getString("merchantGroupedPayModes")).contains("paytm_featured", "other_options", "savedInstruments");
        cashierPage.getPaytmFeaturedText().assertVisible();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.getNewpaymentOption().assertVisible();
    }

    @Owner(PAYAL)
    @Feature("PGP-35983")
    @Parameters({"theme"})
    @Test(description = "Verify PUSH_APP_DATA when FF4J UN_GROUPED_PAYMODES_DISABLED is ON")
    public void ValidateInstrumentCategorization_When_FlagEnabled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UNGROUPED_PAYMODES_FLAG_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cashierPage.getPushAppData().getString("merchantPayModes")).isNull();
        softly.assertAll();
    }

    @Owner(POOJA)
    @Feature("PGP-29548")
    @Parameters({"theme"})
    @Test(description = "Custom checkout: Support for Add and Pay flow in case wallet balance is insufficient")
    public void AddAndPayForWalletInsufficientBalance(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("200")
                .build();

        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        System.out.println(txnToken);
        String amount = initTxnDTO.txnAmountFromBody();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.BALANCE)
                .setPaymentFlow("BALANCE")
                .build();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), txnToken, orderId)
                .setPaymentMode("BALANCE")
                .build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        Assert.assertEquals((ptcResponse.jsonPath().get("body.resultInfo.resultMsg")), "Success");

        String callBackURL = ptcResponse.jsonPath().get("body.callBackUrl");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        DriverManager.getDriver().get(callBackURL);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.DC, paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS");
    }

    @Owner(SRINIVAS)
    @Feature("PGP-32152")
    @Parameters({"theme"})
    @Test(description = "Verify while doing txn on CC/DC text successfully push dwh payload in the kafka topic payment_notify_dwh is present only once in notificationqueuehandler")
    public void Verifying_text_in_notificationqueuehandler(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"PaymentNotifyDWHServiceImpl.pushPayloadInKafka()\" | grep \"PAYMENT_NOTIFY_DWH\"";
        String notificationqueuehandler = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, grepcmd);
        String count = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/notificationQueueHandler.log | " +
                "grep \"PaymentNotifyDWHServiceImpl.pushPayloadInKafka()\" | grep -c  \"Successfully pushed \"";
        String match = getLogsOnServer(ServerConfigProvider.SERVICE.NOTIFICATION_QUEUE_HANDLER, count);
        Assertions.assertThat(notificationqueuehandler).contains("Successfully pushed payment DWH payload in kafka topic PAYMENT_NOTIFY_DWH");
        Assertions.assertThat(match).contains("1");
    }

    @Owner(PAREEKSHITH)
    @Feature("PGP-20291 & PGP-32141")
    @Parameters({"theme"})
    @Test(description = "Verify offline flag in response using UPI collect paymode")
    public void Verify_Offline_Flag(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.UPI_COLLECT, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("100.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String grepcmd = "grep " + orderDTO.getORDER_ID() + " /paytm/logs/instaproxy.log | grep 'getExtendInfo String is'";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).containsIgnoringCase("\"offline\":\"N\"");
        Assertions.assertThat(logs).containsIgnoringCase("\"custId\":\"" + user.custId() + "\"");
        Assertions.assertThat(logs).containsIgnoringCase("\"mobileNo\":\"" + user.mobNo() + "\"");
    }

    @Owner(POOJA)
    @Feature("PGP-35351")
    @Parameters({"isNativePlus"})
    @Test(description = "Move Payment Links to JS Checkout")
    public void MovePaymentLinkstoJSCheckout(@Optional("true") boolean isNativePlus) throws Exception {
        MerchantType merchant = MerchantType.LINK_TRANSACTION;

        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(merchant.getId(), "CHECKSUM_ENABLED", "ACTIVE", "N")
                        .build();
        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.execute();

        String orderId = CommonHelpers.generateOrderId();
        String requestType = "LINK_BASED_PAYMENT";
        String txnToken = InitTxn.LinkBasedPaymentNativeST(merchant.getId(), merchant.getKey(), orderId, requestType, "7777777777", "1").execute().jsonPath().getString("body.txnToken");

        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + merchant.getId() + "\" | grep \"ACQUIRING_CREATE_ORDER\" | grep \"REQUEST\"";

        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        Assert.assertTrue(theiaFacadeLogs.contains("\\\"requestType\\\":\\\"LINK_BASED_PAYMENT\\\""));
        Assert.assertTrue(theiaFacadeLogs.contains("\\\"linkDescription\\\":\\\"Link10DEPLNK1598877077769\\\""));

    }

    @Owner(ROHIT)
    @Feature("PGP-30615")
    @Parameters({"theme"})
    @Test(description = "To verify the device info is sent in payment request for web when ff4j flag is on insta.sendDeviceParametersPpblNb ")
    public void verifyDeviceInfoSent(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        //  cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(PayMode.PPBL);
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
                .validateCheckSum(MerchantType.LINK_BASED_MERCHANT_WITH_PPBLC.getKey())
                .assertAll();

        String grepcmd = "grep 'Payment Request| BankCode:PPBL | PayMethod:NB' /paytm/logs/instaproxy.log | grep " + orderDTO.getORDER_ID() + "";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(logs).contains("sourceIp");
        Assertions.assertThat(logs).contains("osVersion");
        Assertions.assertThat(logs).contains("networkType");
        Assertions.assertThat(logs).contains("client_2");
        if (theme.equals("enhancedweb_revamp")) {
            Assertions.assertThat(logs).contains("channel_2\":\"WEB");
        } else {
            Assertions.assertThat(logs).contains("channel_2\":\"mWeb");
        }


    }

    @Feature("PGP-36898")
    @Owner(AAYUSH)
    @Parameters({"theme"})
    @Test(description = "Validate CC pay button is inactive and Disable state message is disabled")
    public void validate_DisablePaymodeonEnhance(@Optional("enhancedweb_revamp") String theme) throws InterruptedException, IOException {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DISABLE, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabCreditCard().click();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI Bank");
        paymentDTO.setDebitCardNumber(PaymentDTO.PROMO_CC_CARD_HDFC);
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());
        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear());
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());

        Assertions.assertThat(cashierPage.disableMessage().getText()).as(" Disabled state message")
                .containsIgnoringCase("The payment option is experiencing downtime, please try another payment source to complete the transaction");
        cashierPage.buttonPGPayNow().isEnabled();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-35345")
    @Parameters({"theme"})
    @Test(description = "Verifying Notification SMS data is getting pushed in new KAFKA topic")
    public void Verifying_Notification_Data_Into_New_Kafka_Topic(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);
        String grepcmd = "grep 'DWHDataHelper' /paytm/logs/communicationGateway.log";
        String commLogs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(commLogs).contains("Entered in pushDataToDWHKafkaTopic with SMSInfo");
        Assertions.assertThat(commLogs).contains("Kafka Topic : DWH_SMS_DATA");
    }


    @Owner(PUSPA)
    @Feature("PGP-34467")
    @Parameters({"theme"})
    @Test(description = "Verify EmiInfo details in ExtendInfo object in ACQUIRING_PAY_ORDER Api")
    public void VerifyEMIStandardemiInfoObject(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMISubvention, theme)
                .setTXN_AMOUNT("12.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        SoftAssertions softly = new SoftAssertions();
        String grepcmd = "grep \"" + responsePage.textOrderID().getText() + "\" /paytm/logs/theia_facade.log | " + "grep \"ACQUIRING_PAY_ORDER\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        softly.assertThat(theiaFacadeLogs).contains("\\\\\\\"TXN_ID\\\\\\\":\\\\\\\"" + responsePage.textTxnID().getText() + "\\\\\\\"")
                .contains("\\\\\\\"cardType\\\\\\\":\\\\\\\"CREDIT_CARD\\\\\\\"")
                .contains("\\\\\\\"MID\\\\\\\":\\\\\\\"" + responsePage.textMID().getText() + "\\\\\\\"")
                .contains("\\\\\\\"cardNo\\\\\\\":\\\\\\\"0336\\\\\\\"")
                .contains("\\\\\\\"cardToken\\\\\\\":\\\\\\\"\\\\\\\"")
                .contains("\\\\\\\"merchantName\\\\\\\":\\\\\\\"Automation Merchant\\\\\\\"")
                .contains("\\\\\\\"cardIssuer\\\\\\\":\\\\\\\"HDFC\\\\\\\"")
                .contains("\\\\\\\"bank\\\\\\\":\\\\\\\"HDFC\\\\\\")
                .contains("\\\\\\\"emiAmount\\\\\\\":\\\\\\\"2.06\\\\\\")
                .contains("\\\\\\\"ORDER_ID\\\\\\\":\\\\\\\"" + responsePage.textOrderID().getText() + "\\\\\\")
                .contains("\\\\\\\"emiMonths\\\\\\\":\\\\\\\"6\\\\\\")
                .contains("\\\\\\\"emiInterestRate\\\\\\\":\\\\\\\"10.0\\\\\\")
                .contains("\\\\\\\"planID\\\\\\\":\\\\\\\"HDFC|6\\\\\\")
                .contains("\\\\\\\"promocode\\\\\\\":\\\\\\\"jBAtVQ\\\\\\\"")
                .contains("\\\\\\\"redemptionType\\\\\\\":\\\\\\\"cashback\\\\\\\"")
                .contains("\\\\\\\"savings\\\\\\\":60");
        softly.assertAll();
    }

    @Owner(PUSPA)
    @Feature("PGP-34467")
    @Parameters({"theme"})
    @Test(description = "Verify EmiInfo details in ExtendInfo and EmiSubvention object in ACQUIRING_PAY_ORDER Api")
    public void verifyItemBasedEMI_EmiInfoAndEMiSubvention_Object(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = MerchantType.EMISubvention;
        List<SimplifiedSubvention.Item> items = new ArrayList<SimplifiedSubvention.Item>();
        SimplifiedSubvention.Item item = new SimplifiedSubvention.Item("1", "321067334", "124197", null, "1", "20", "51", true, false, null);
        items.add(item);
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, items);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        SoftAssertions softly = new SoftAssertions();
        String grepcmd = "grep \"" + responsePage.textOrderID().getText() + "\" /paytm/logs/theia_facade.log | " + "grep \"ACQUIRING_PAY_ORDER\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        //Verify emiInfo object details
        softly.assertThat(theiaFacadeLogs).contains("\\\\\\\"TXN_ID\\\\\\\":\\\\\\\"" + responsePage.textTxnID().getText() + "\\\\\\\"")
                .contains("\\\\\\\"cardType\\\\\\\":\\\\\\\"CREDIT_CARD\\\\\\\"")
                .contains("\\\\\\\"MID\\\\\\\":\\\\\\\"" + responsePage.textMID().getText() + "\\\\\\\"")
                .contains("\\\\\\\"cardNo\\\\\\\":\\\\\\\"0336\\\\\\\"")
                .contains("\\\\\\\"cardToken\\\\\\\":\\\\\\\"\\\\\\\"")
                .contains("\\\\\\\"merchantName\\\\\\\":\\\\\\\"Automation Merchant\\\\\\\"")
                .contains("\\\\\\\"cardIssuer\\\\\\\":\\\\\\\"HDFC\\\\\\\"")
                .contains("\\\\\\\"bank\\\\\\\":\\\\\\\"HDFC\\\\\\")
                .contains("\\\\\\\"emiAmount\\\\\\\":\\\\\\\"33.89\\\\\\")
                .contains("\\\\\\\"ORDER_ID\\\\\\\":\\\\\\\"" + responsePage.textOrderID().getText() + "\\\\\\")
                .contains("\\\\\\\"emiMonths\\\\\\\":\\\\\\\"6\\\\\\")
                .contains("\\\\\\\"emiInterestRate\\\\\\\":\\\\\\\"10.0\\\\\\")
                .contains("\\\\\\\"planID\\\\\\\":\\\\\\\"HDFC|6\\\\\\");
        softly.assertAll();

        //Verify emiSubvention object details
        softly.assertThat(theiaFacadeLogs).contains("\\\\\\\"planId\\\\\\\":\\\\\\\"1519\\\\\\")
                .contains("\\\\\\\"tenure\\\\\\\":6")
                .contains("\\\\\\\"cardType\\\\\\\":\\\\\\\"CREDIT_CARD\\\\\\\"")
                .contains("\\\\\\\"gratificationDiscount\\\\\\\":1.0")
                .contains("\\\\\\\"gratificationCashback\\\\\\\":0.0")
                .contains("\\\\\\\"gratificationType\\\\\\\":\\\\\\\"DISCOUNT\\\\\\")
                .contains("\\\\\\\"subventionAmount\\\\\\\":\\\\\\\"20.0\\\\\\")
                .contains("\\\\\\\"pgPlanId\\\\\\\":\\\\\\\"HDFC|6\\\\\\")
                .contains("\\\\\\\"emiInterestRate\\\\\\\":10.0")
                .contains("\\\\\\\"brandId\\\\\\\":\\\\\\\"124197\\\\\\")
                .contains("\\\\\\\"brand\\\\\\\":0.0")
                .contains("\\\\\\\"merchant\\\\\\\":0.0")
                .contains("\\\\\\\"platform\\\\\\\":1.0")
                .contains("\\\\\\\"emi\\\\\\\":33.89");
        softly.assertAll();
    }

    @Owner(PUSPA)
    @Feature("PGP-34467")
    @Parameters({"theme"})
    @Test(description = "Verify EmiInfo details in ExtendInfo and EmiSubvention object in ACQUIRING_PAY_ORDER Api")
    public void verifyAmountBasedEMI_EmiInfoAndEMiSubvention_Object(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = MerchantType.Hybrid;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "10", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        SoftAssertions softly = new SoftAssertions();
        String grepcmd = "grep \"" + responsePage.textOrderID().getText() + "\" /paytm/logs/theia_facade.log | " + "grep \"ACQUIRING_PAY_ORDER\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        //Verify emiInfo object details
        softly.assertThat(theiaFacadeLogs).contains("\\\\\\\"TXN_ID\\\\\\\":\\\\\\\"" + responsePage.textTxnID().getText() + "\\\\\\\"")
                .contains("\\\\\\\"cardType\\\\\\\":\\\\\\\"CREDIT_CARD\\\\\\\"")
                .contains("\\\\\\\"MID\\\\\\\":\\\\\\\"" + responsePage.textMID().getText() + "\\\\\\\"")
                .contains("\\\\\\\"cardNo\\\\\\\":\\\\\\\"0336\\\\\\\"")
                .contains("\\\\\\\"cardToken\\\\\\\":\\\\\\\"\\\\\\\"")
                .contains("\\\\\\\"merchantName\\\\\\\":\\\\\\\"AutomationMerchant0011\\\\\\\"")
                .contains("\\\\\\\"cardIssuer\\\\\\\":\\\\\\\"HDFC\\\\\\\"")
                .contains("\\\\\\\"bank\\\\\\\":\\\\\\\"HDFC\\\\\\")
                .contains("\\\\\\\"emiAmount\\\\\\\":\\\\\\\"33.89\\\\\\")
                .contains("\\\\\\\"ORDER_ID\\\\\\\":\\\\\\\"" + responsePage.textOrderID().getText() + "\\\\\\")
                .contains("\\\\\\\"emiMonths\\\\\\\":\\\\\\\"6\\\\\\")
                .contains("\\\\\\\"emiInterestRate\\\\\\\":\\\\\\\"10.0\\\\\\")
                .contains("\\\\\\\"planID\\\\\\\":\\\\\\\"HDFC|6\\\\\\");
        softly.assertAll();

        //Verify emiSubvention object details
        softly.assertThat(theiaFacadeLogs).contains("\\\\\\\"planId\\\\\\\":\\\\\\\"1489\\\\\\")
                .contains("\\\\\\\"tenure\\\\\\\":6")
                .contains("\\\\\\\"gratificationDiscount\\\\\\\":2.0")
                .contains("\\\\\\\"gratificationCashback\\\\\\\":0.0")
                .contains("\\\\\\\"gratificationType\\\\\\\":\\\\\\\"DISCOUNT\\\\\\")
                .contains("\\\\\\\"subventionAmount\\\\\\\":\\\\\\\"20.0\\\\\\")
                .contains("\\\\\\\"pgPlanId\\\\\\\":\\\\\\\"HDFC|6\\\\\\")
                .contains("\\\\\\\"emiInterestRate\\\\\\\":10.0")
                .contains("\\\\\\\"eligibleAmt\\\\\\\":10.0")
                .contains("\\\\\\\"brand\\\\\\\":0.0")
                .contains("\\\\\\\"merchant\\\\\\\":0.0")
                .contains("\\\\\\\"platform\\\\\\\":1.0")
                .contains("\\\\\\\"emi\\\\\\\":33.89");
        softly.assertAll();
    }

    @Owner(GAURAV)
    @Feature("PGP-38084")
    @Parameters({"theme"})
    @Test(description = "Verifying UPI Collect is diabled for txn amt > 2K on non verified merchant")
    public void verifyUPICollectIsDisabledForGreaterThan2k(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.NON_VERIFED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2001")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.upiPushSection().isElementPresent()).isFalse();
        cashierPage.tabUPI().assertNotVisible();

    }

    @Owner(GAURAV)
    @Feature("PGP-38084")
    @Parameters({"theme"})
    @Test(description = "Verifying UPI Collect is enabled for txn amt < 2K on non verified merchant")
    public void verifyUPICollectIsDisabledForLessThan2k(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.NON_VERIFED, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("2000")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.upiPushSection().isElementPresent()).isTrue();
        cashierPage.tabUPI().assertVisible();

    }

    @Owner(CHETAN)
    @Feature("PGP-36669")
    @Parameters({"theme"})
    @Test(description = "Verify paytm pg logo is displayed in header and footer in cashier page enhanced flow")
    public void verifying_paytmPgLogo_displayed_onEnhanced(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.getPaytmLogoBlue().isElementPresent());
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.footerLogoBlue().isElementPresent());

    }

    @Owner(ABHISHEK_TEWARI)
    @Feature("PGP-38740")
    @Parameters({"theme"})
    @Test(description = "To verify UPI Collect timer is starting from 5 minutes")
    public void verifyUpiCollect5MinTimer(@Optional("enhancedweb_revamp") String theme) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.MERCHANT_UPI_PPI_CC_DC_SUBS, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.UPI);
        UPIPollingPage upiPollingPage = new UPIPollingPage();
        String minutes = upiPollingPage.getMinutesDisplayed().getText().replace("\nMIN", "");
        Assertions.assertThat(minutes).isLessThanOrEqualTo("05");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(CHETAN)
    @Feature("PGP-36683")
    @Parameters({"theme"})
    @Test(description = "Verify for closed wallet user RBI guidelines error message should be shown")
    public void verify_closed_wallet_user_error_message(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.DEACTIVATEDUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DEACTIVATED_WALLET, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        // Verify the error message
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("Your wallet has been deactivated as mandated by RBI.");
        // verify wallet cannot be selected
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        // Verify know more is clickable and assert the message in know more window
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("As per RBI guidelines, all wallet accounts with no transactions in the past one year have been deactivated");
        // verify from debit/credit/net any banking transaction should be successful without using wallet
        cashierPage.payBy(Constants.PayMode.NB);
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

    @Owner(GAURAV)
    @Feature("PGP-39748")
    @Parameters({"theme"})
    @Test(description = "Verify paytm logo is visible in the header & footer instead of Paytm PG logo")
    public void verifyPaytmLogoisVisible(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(PAYTM_LOGO, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.paytmHeaderLogo().assertVisible();
        cashierPage.paytmFooterLogo().assertVisible();
    }

    @Owner(GAURAV)
    @Feature("PGP-39748")
    @Parameters({"theme"})
    @Test(description = "Verify paytm logo is visible in the header & footer instead of Paytm PG logo")
    public void verifyPaytmLogoisVisibleInMobile(@Optional("enhancedwap_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(PAYTM_LOGO, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.paytmHeaderLogo().assertVisible();
        cashierPage.scrollToElement(cashierPage.paytmFooterLogo());
        cashierPage.paytmFooterLogo().assertVisible();
    }

    @Owner(CHETAN)
    @Feature("PGP-38125")
    @Parameters({"theme"})
    @Test(description = "Verify wallet deactivated message is shown when all the paymodes are explicitly disabled except wallet")
    public void verify_closed_wallet_user_error_message_when_all_paymodes_disabled_except_wallet(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.DEACTIVATEDUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DEACTIVATED_WALLET, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYMENT_MODE_DISABLE("CREDIT_CARD,DEBIT_CARD,NET_BANKING")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        // Verify the error message
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("Your wallet has been deactivated as mandated by RBI.");
        // verify wallet cannot be selected
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        // Verify know more is clickable and assert the message in know more window
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("As per RBI guidelines, all wallet accounts with no transactions in the past one year have been deactivated");

    }

    @Owner(PRIYANKA)
    @Feature("PGP-39503")
    @Parameters({"theme"})
    @Test(description = "Verify the masked mobile number is displayed in Console APP_DATA")
    public void verifyMaskedMobileNumberInConsoleAppData(@Optional("enhancedweb_revamp") String theme) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.MASKED_MID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath JsonPath = initTxn.execute().jsonPath();
        String txnToken = JsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.MASKED_MID, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cashierPage.getPushAppData().getString("userInfo.mobile")).contains("XXXXXX");
        softly.assertAll();

    }

    @Owner(PRIYANKA)
    @Feature("PGP-39503")
    @Parameters({"theme"})
    @Test(description = "Verify the masked mobile number is displayed in SendOTP API")
    public void verifyMaskedMobileNumberInSendOTPAPIfromEnhancePage(@Optional("enhancedweb_revamp") String theme) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.MASKED_MID).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath JsonPath = initTxn.execute().jsonPath();
        String txnToken = JsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(Constants.MerchantType.MASKED_MID, initTxnDTO.getBody().getOrderId(), txnToken).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cashierPage.getPushAppData().getString("userInfo.mobile"));
        String masked_mobile_1 = cashierPage.getPushAppData().getString("userInfo.mobile");
        System.out.println("masked_mobile_1");

        SendOTP mobile_Number_mask = new SendOTP(txnToken, masked_mobile_1, initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        JsonPath SendOTPAPIJson = mobile_Number_mask.execute().jsonPath();
        String masked_mobile_sentOTP = SendOTPAPIJson.getString("body.mobileNumber");
        Assertions.assertThat(masked_mobile_1).contains("XXXXXX");

    }

    @Owner(PRIYANKA)
    @Feature("PGP-37557")
    @Test(description = "Verify VPA is displaying in Merchant Status response")
    public void Verify_vpa_MerchantstatusResponse(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(UPI_RETRY_ENABLED, theme).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateVPA(new PaymentDTO().getVpa());
        responsePage.validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatusApi reference = new TxnStatusApi(UPI_RETRY_ENABLED.getId(), UPI_RETRY_ENABLED.getKey(), orderDTO.getORDER_ID());
        PGPHelpers.getTxnStatus(UPI_RETRY_ENABLED.getId(), orderDTO.getORDER_ID()).validateVPA("srivastavaprateek@paytm");
        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/merchant-status.log | "
                + "grep \"processMerchantTxnStatus()\"";
        String Merchantstatuslogs = getLogsOnServer(ServerConfigProvider.SERVICE.MERCHANT_STATUS, grepcmd);
        Assertions.assertThat(Merchantstatuslogs).contains("vpa=srivastavaprateek@paytm");
    }


    @Owner(CHETAN)
    @Feature("PGP-37964")
    @Parameters({"theme"})
    @Test(description = "Verify for new wallet user RBI guidelines error message should be shown")
    public void verify_new_wallet_user_error_message(@Optional("enhancedweb_revamp") String theme) throws Exception {
        // User sso token should be created after march 12, 2022 checkUserBalance api should show "embargo":false
        User user = userManager.getForRead(Label.NEWWALLETUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DEACTIVATED_WALLET, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        // Verify the error message
        Assertions.assertThat(cashierPage.getWalletDisabledErrorMessage().getText()).isEqualTo("This option is not available for you.");
        // verify wallet cannot be selected
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        // Verify know more is clickable and assert the message in know more window
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("This option is available only to existing Paytm balance users. Kindly use other payment option to complete this payment");
        // verify from debit/credit/net any banking transaction should be successful without using wallet
        cashierPage.payBy(Constants.PayMode.NB);
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

    @Owner(PUSPA)
    @Feature("PGP-39088")
    @Test(description = "Emi plans should not be fetched in case of invalid bins and button should be disabled")
    public void verifySelectEmiPlanButtonDisabledforInvalidBins(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EMI, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.dropdownEmiBanks().selectByVisibleText("HDFC Bank");
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.INVALID_9DIGIT_BIN_NO);
        cashierPage.getBinerror().waitUntilVisible();
        Assertions.assertThat(cashierPage.getBinerror().getText()).isEqualTo("Please enter HDFC card");
        cashierPage.selectEMIPlan().assertDisabled();

    }

    @Owner(PUSPA)
    @Feature("PGP-38653")
    @Parameters({"theme"})
    @Test(description = "Verify default priorities of pay options in response of V5/FPO in groupedPayOptionsPriorities object")
    public void validatePayment_Instrument_Group_level_DEFAULT(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(EMISubvention, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cashierPage.getPushAppData().getString("groupPayOptionsPriorities")).isNotNull();
        softly.assertThat(cashierPage.getPushAppData().getString("groupPayOptionsPriorities")).contains("upiProfile:2", "paytm_featured:1", "savedMandateBanks:4", "userProfileSarvatra:2", "savedInstruments:3", "other_options:5");
        softly.assertAll();
    }

    @Owner(PUSPA)
    @Feature("PGP-38653")
    @Parameters({"theme"})
    @Test(description = "Verify priorities(Pref:ENHANCE_PAYMODE_GROUP_PRIORITY_LIST) of pay options in response of V5/FPO in groupedPayOptionsPriorities object")
    public void validatePayment_Instrument_Group_level_ENHANCE(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        OrderDTO orderDTO = new OrderFactory.PGOnly(UNGROUPED_PAYMODES_FLAG_ENABLED, theme)
                .setSSO_TOKEN(user.ssoToken()).setTXN_AMOUNT("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cashierPage.getPushAppData().getString("groupPayOptionsPriorities")).isNotNull();
        softly.assertThat(cashierPage.getPushAppData().getString("groupPayOptionsPriorities")).contains("upiProfile:3", "paytm_featured:1", "savedMandateBanks:4", "userProfileSarvatra:3", "savedInstruments:2", "other_options:5");
        softly.assertAll();
    }

    @Owner(PUSPA)
    @Feature("PGP-38653")
    @Parameters({"theme"})
    @Test(description = "Verify priorities(Pref:ENHANCE_PAYMODE_GROUP_PRIORITY_LIST) of pay options in response of V5/FPO in groupedPayOptionsPriorities object")
    public void validatePayment_Instrument_Group_level_APPINVOKE(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), UNGROUPED_PAYMODES_FLAG_ENABLED)
                .setTxnValue("1.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(UNGROUPED_PAYMODES_FLAG_ENABLED, initTxnDTO.orderFromBody(), txnToken).build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(cashierPage.getPushAppData().getString("groupPayOptionsPriorities")).isNotNull();
        softly.assertThat(cashierPage.getPushAppData().getString("groupPayOptionsPriorities")).contains("upiProfile:3", "paytm_featured:1", "savedMandateBanks:4", "userProfileSarvatra:3", "savedInstruments:2", "other_options:5");
        softly.assertAll();
    }

    @Owner(CHETAN)
    @Feature("PAPR-3207")
    @Parameters({"theme"})
    @Test(description = "verify ultimate beneficiary details present in cashier page and insta logs")
    public void verify_ultimate_beneficiary_details_present_in_cashier_page_and_insta_logs_enhanced(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String ultimateBeneficiaryName = "TestBeneficiary";
        User user = userManager.getForRead(Label.NEWWALLETUSER);
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.ULTIMATE_BENE, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setUltimateBeneficiaryName(ultimateBeneficiaryName)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.getUltimateBeneficiaryName().getText()).isEqualTo(ultimateBeneficiaryName);
        cashierPage.payBy(PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(ValidationType.NON_EMPTY)
                .assertAll();
        String tsnId = responsePage.textTxnID().getText();
        String grepcmd = "grep \"" + tsnId + "\" /paytm/logs/instaproxy.log | " +
                "grep \"" + ultimateBeneficiaryName + "\"";
        String instaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(instaLogs).contains(ultimateBeneficiaryName);
    }

    @Owner(HARSHITA)
    @Feature("PGP-40975")
    @Test(description = "Verify response of theia/api/v1/fetchMerchantConfig API with JWT Token")
    public void validateFetchMerchantConfigAPI() throws Exception {
        String mid = MerchantType.PGOnly.getId();
        FetchMerchantConfig fetchMerchantConfig = new FetchMerchantConfig(mid);
        JsonPath response = fetchMerchantConfig.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.merchantStaticConfig.mid").equals(mid));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("S"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("0000"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Success"));
        softly.assertAll();
    }

    @DataProvider(name = "SsoAndMidData")
    public Object[][] getDataFromDataprovider() {
        Label users[] = {Label.CREDITFREEZE, Label.DEBITFREEZE, Label.CREDITDEBITFREEZE};
        MerchantType mids[] = {ADD_N_PAY_FREEZE, HYBRID_FREEZE};
        Object[][] arr = new Object[(users.length) * (mids.length)][mids.length];
        int row = 0, col = 0;
        for (int i = 0; i < users.length; i++) {
            arr[row][0] = users[i];
            row++;
            arr[row][0] = users[i];
            row++;
            for (int j = 0; j < mids.length; j++) {
                arr[col][1] = mids[j];
                col++;
            }
        }
        return arr;
    }

    @Owner(CHETAN)
    @Feature("PGP-39336")
    @Parameters({"theme"})
    @Test(description = "Verify credit/Debit/creditdebit freeze messages on wallet", dataProvider = "SsoAndMidData")
    public void verify_wallet_disabled_message_creditDebitFreeze(
            Label userWallet, MerchantType mid) throws Exception {
        String theme = "enhancedweb_revamp";
        User user = userManager.getForRead(userWallet);
        double balanace = WalletHelpers.getWalletBalance(user);
        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, theme)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(String.valueOf(balanace + 10))
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.pause(2);
        if (userWallet.toString().equals("creditfreeze") && mid.toString().equals("HYBRID_FREEZE")) {
            Assertions.assertThat(cashierPage.checkBoxPPI().isChecked()).isTrue();
        } else if (mid.toString().equals("ADD_N_PAY_FREEZE")) {
            Assertions.assertThat(cashierPage.getWalletDisabledErrorMessage().getText()).isEqualTo("This option is not available for you.");
            Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("Based on your KYC status and the limits imposed by RBI, you cannot complete this transaction using wallet. Please visit wallet section in the Paytm app to update your wallet KYC if not yet updated.");
        } else {
            Assertions.assertThat(cashierPage.getWalletFreezeErrorMessage().getText()).isEqualTo("This option is not available for you.");
            Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("Paytm Balance is not available to you at the moment. Please visit Wallet section on the Paytm app.");
        }
    }

    @Owner(SRINIVAS)
    @Feature("PGP-35642")
    @Parameters({"theme"})
    @Test(description = "Verify making customisation on cashier page on UMP Panel reflected in enhance cashier page")
    public void Verify_cashierpage_is_displayed_with_customisation_in_enhance_cashierpage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.CUSTOMISATION_ON_CASHIERPAGE;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String headerTxtColor = getColorCode(cashierPage.headerTxtColor().getCssValue("color"));
        String bodyBck_color = getColorCode(cashierPage.bodyBck_color().getCssValue("background-color"));
        String textColor = getColorCode(cashierPage.textColor().getCssValue("color"));
        String paybuttonbck_color = getColorCode(cashierPage.paybuttonbck_color().getCssValue("background-color"));

        Assertions.assertThat(headerTxtColor).isEqualTo("#ffffff");
        Assertions.assertThat(bodyBck_color).isEqualTo("#00ff00");
        Assertions.assertThat(textColor).isEqualTo("#000000");
        Assertions.assertThat(paybuttonbck_color).isEqualTo("#00b9f5");
    }

    @Owner(CHETAN)
    @Feature("PGP-38072")
    @Parameters({"theme"})
    @Test(description = "Verify paytm pg white logo is displayed in header of cashier page enhanced flow for dark theme mid")
    public void verifying_paytmPgLogo_displayed_on_header_enhanced(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.DARK_THEME, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Assert.assertTrue(cashierPage.getPaytmLogoWhite().isElementPresent());
        cashierPage.waitUntilLoads();

    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when UPI is enabled and Cards, Postpaid, Wallet are disabled")
    public void PGP_41787_TC01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setPAYMENT_MODE_ONLY("YES")
                .setPAYMENT_TYPE_ID("UPI")
                .setPAYMENT_MODE_DISABLE("CC,DC,BALANCE,PAYTM_DIGITAL_CREDIT")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm or Any UPI App");
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia.log | " + "grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when UPI is disabled and Cards, Postpaid, Wallet are enabled")
    public void PGP_41787_TC02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setPAYMENT_MODE_ONLY("YES")
                .setPAYMENT_TYPE_ID("CC,DC,BALANCE,PAYTM_DIGITAL_CREDIT")
                .setPAYMENT_MODE_DISABLE("UPI")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia.log | " + "grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Parameters({"theme"})
    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as alphabetic.")
    public void UpiNumericIdTestCase_01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ENABLE_DISABLE_PAYMODE, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("abcd");
        cashierPage.buttonPGPayNow().click();
        Assert.assertEquals(cashierPage.errorTextsInUPIFlow().getText(), "Only numbers to be entered for UPI Number");
    }

    @Parameters({"theme"})
    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as alphanumeric.")
    public void UpiNumericIdTestCase_02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ENABLE_DISABLE_PAYMODE, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("12abcd");
        cashierPage.buttonPGPayNow().click();
        Assert.assertEquals(cashierPage.errorTextsInUPIFlow().getText(), "Only numbers to be entered for UPI Number");
    }

    @Parameters({"theme"})
    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as less than 10 digits.")
    public void UpiNumericIdTestCase_03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ENABLE_DISABLE_PAYMODE, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("12");
        cashierPage.buttonPGPayNow().click();
        Assert.assertEquals(cashierPage.errorTextsInUPIFlow().getText(), "UPI Number can be 8 to 10 digit length only");
    }

    @Parameters({"theme"})
    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as more than 10 digits.")
    public void UpiNumericIdTestCase_04(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ENABLE_DISABLE_PAYMODE, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("123456789012");
        cashierPage.buttonPGPayNow().click();
        Assert.assertEquals(cashierPage.errorTextsInUPIFlow().getText(), "UPI Number can be 8 to 10 digit length only");
    }

    @Parameters({"theme"})
    @Feature("PGP-42218")
    @Owner("Himanshu Arora")
    @Test(description = "validate verified VPA message when correct UPI number is passed")
    public void UpiNumericIdTestCase_05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        double txn_amount = 20;
        User user = userManager.getForRead(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.ENABLE_DISABLE_PAYMODE, theme)
                .setTXN_AMOUNT(toString().valueOf(txn_amount))
                .build();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        checkoutPage.createOrder(orderDTO);
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("8006006993");
        cashierPage.buttonPGPayNow().click();
        Assert.assertEquals(cashierPage.errorTextsInUPIFlow().getText(), "Verified: 8006006993@okicici");

    }

    @Owner(PAREEKSHITH)
    @Feature("PG2-12060")
    @Test(description = "Verify successful transaction on product code 02,101 & 02,20")
    public void verifySucessfulTxnon20and02PC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.Prodcode20_02, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
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

    @Owner(PAREEKSHITH)
    @Feature("PG2-12060")
    @Test(description = "Verify successful transaction on product code 02,101 & 02,20")
    public void verifySucessfulTxnon20and101PC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(Prodcode20_101, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
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

    @Owner(PAREEKSHITH)
    @Test(description = "arn webhook test")
    @Feature("PGP-43406")
    public void arnNotify() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        arnNotify arnNotify = new arnNotify();
        arnNotify.txnarnNotify(Arn_Mid.getId(), orderId);
        String response = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId);
        Assertions.assertThat(response).contains("PAYMENT_SUCCESS_ARN_UPDATE");
    }

    @Owner(PAREEKSHITH)
    @Test(description = "refund arn webhook test")
    @Feature("PGP-43406")
    public void refundArnNotify() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        arnNotify arnNotify = new arnNotify();
        arnNotify.refundArnNotify(Arn_Mid.getId(), orderId);
        String response1 = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, orderId);
        Assertions.assertThat(response1).contains("REFUND_SUCCESS_ARN_UPDATE");
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is Y")
    public void RedirectionOnOfflineMerchantWhenPreferenceIsOn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(OFFLINE_WHITELISTED, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespMsg("Txn Success")
                .assertAll();
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is N")
    public void RedirectionOnOfflineMerchantWhenPreferenceIsOff(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(OFFLINE_WHITELISTED_OFF, theme)
                .setTXN_AMOUNT("20.00")
                .setCallBack_URL("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg("Payment acceptance on this merchant is not available currently, please ask the merchant to contact our helpdesk team.")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is Y and API DISABLE Y")
    public void onlineTxnOnOfflineMerchantWhenPreferenceIsON_API_Y(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(OFFLINE_WHITELISTED_API_Y, theme)
                .setTXN_AMOUNT("20.00")
                .setCallBack_URL("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg("Payment acceptance on this merchant is not available currently, please ask the merchant to contact our helpdesk team.")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Test(description = "Verify successful online transaction on offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is F and API DISABLE Y")
    public void onlineTxnOnOfflineMerchantWhenPreferenceIsOFF_API_Y(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(OFFLINE_WHITELISTED_OFF_API_Y, theme)
                .setTXN_AMOUNT("20.00")
                .setCallBack_URL("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespMsg("Payment acceptance on this merchant is not available currently, please ask the merchant to contact our helpdesk team.")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Owner(GAURAV)
    @Feature("PGP-46539")
    @Parameters({"theme"})
    @Test(description = "Verify new UPI polling page with a successful transaction")
    public void verifyNewUpiPollingPageAndSuccessTxn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(PGOnly, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.upiPollingPageMobileLogo().isDisplayed();
        cashierPage.upiPollingPageInfoText().isDisplayed();
        cashierPage.upiPollingPageWarningText().isDisplayed();
        Assertions.assertThat(cashierPage.upiPollingPageTxnAmount().getText()).isEqualTo("Rs" + orderDTO.getTXN_AMOUNT().replace(".00", ""));
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

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46368")
    @Parameters({"theme"})
    @Test(description = "Verify EmiInfo details in ExtendInfo object in COP Api")
    public void verifyCOPEmiInfoForRedirection(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.EmiInfo_COP, theme)
                .setTXN_AMOUNT("200")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        String orderid = responsePage.textOrderID().getText();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("emiInfo\":\"{\"cardType\":\"CREDIT_CARD\"");
        Assertions.assertThat(logs).contains("\"MID\":\"" + responsePage.textMID().getText() + "\"");
        Assertions.assertThat(logs).contains("\"cardNo\":\"0336\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"190.0\"");
        Assertions.assertThat(logs).contains("\"merchantName\":\"pg2EMI1\"");
        Assertions.assertThat(logs).contains("\"cardIssuer\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"bank\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"emiAmount\":\"33.67\"");
        Assertions.assertThat(logs).contains("\"ORDER_ID\":\"" + responsePage.textOrderID().getText() + "\"");
        Assertions.assertThat(logs).contains("\"interest\":\"12.02\"");
        Assertions.assertThat(logs).contains("\"emiMonths\":\"6\"");
        Assertions.assertThat(logs).contains("\"emiInterestRate\":\"3.5\"");
        Assertions.assertThat(logs).contains("\"planID\":\"HDFC|6\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46162")
    @Parameters({"theme"})
    @Test(description = "Verify bank verified name as account holder name in MERCHANT_CENTER_SERVICE response body")
    public void BankVerifiedName(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(EmiInfo_COP, theme)
                .setTXN_AMOUNT("20.00")
                .setCallBack_URL("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        String orderid = responsePage.textOrderID().getText();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "MERCHANT_CENTER_SERVICE");
        Assertions.assertThat(logs).contains("\"accountHolderName\":\"puspa\"");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-47696")
    @Parameters({"theme"})
    @Test(description = "Verify that for deactive postpaid user account msg Your Postpaid account is not active. Please use other Payment option. should be displayed")
    public void postpaidDeactiveUimsg(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        OrderDTO orderDTO = new OrderFactory.PGOnly(PGOnly, theme)
                .setTXN_AMOUNT("24.00")
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String txt = cashierPage.paymentContainer().getText();
        Assertions.assertThat(txt).contains("Your Postpaid account is not active. Please use other Payment option");
    }

    @Owner(HARSHITA)
    @Feature("PGP-48011")
    @Parameters({"theme"})
    @Test(description = "Verify merchantAllowedUpiPaymentInstrumentsCommaSeparated in Payment Request and paymentInstrument field in callback and payment response to router for UPI CC transaction through Collect")
    public void PGP_48011_TC01(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(CancelAllowed, theme)
                .setTXN_AMOUNT("90.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        String orderid = responsePage.textOrderID().getText();
        String extSn = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, orderid, "ExtSN=");
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        String paymentRequest = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "Payment Request");
        Assertions.assertThat(paymentRequest).contains("merchantAllowedUpiPaymentInstrumentsCommaSeparated=CREDIT_CARD");
        String callback = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "Bank callback Response");
        Assertions.assertThat(callback).contains("paymentInstrument=CREDIT_CARD");
        String paymentResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "FLUXNET_UPI_PG2_PAYMENT_RESULT");
        Assertions.assertThat(paymentResponse).contains("\"paymentInstrument\":\"UPI_CREDIT_CARD\"");
    }

    @Owner(HARSHITA)
    @Feature("PGP-48011")
    @Test(description = "Verify paymentInstrument field in callback and payment response to router for UPI CC transaction through Intent")
    public void PGP_48011_TC02() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.CancelAllowed;
        String OrderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "213.13", "281005050101CEKNMYC38XND", "UPI_CREDIT_CARD");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "UPI_CREDIT_CARD");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode()).isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("213.13")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
        String extSn = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, OrderId, "ExtSN=");
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        String callback = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "Bank callback Response");
        Assertions.assertThat(callback).contains("paymentInstrument=CREDIT_CARD");
        String paymentResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "FLUXNET_UPI_PG2_PAYMENT_RESULT");
        Assertions.assertThat(paymentResponse).contains("\"paymentInstrument\":\"UPI_CREDIT_CARD\"");
    }

    @Owner(HARSHITA)
    @Feature("PGP-48011")
    @Test(description = "Verify paymentInstrument field in callback and payment response to router for UPIPPI Wallet transaction through Intent")
    public void PGP_48011_TC03() throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.CancelAllowed;
        String OrderId = CommonHelpers.generateOrderId();
        StaticQrUpiPSPRequest.Builder builder = new StaticQrUpiPSPRequest.Builder(merchant.getId(), "213.23", "281005050101CEKNMYC38XND", "PPI_WALLET");
        builder.setOrderId(OrderId);
        StaticQrUpiPSPRequest staticQrUpiPSPRequest = new StaticQrUpiPSPRequest(builder, "PPI_WALLET");
        StaticQrUpiPSP staticQrUpiPSP = new StaticQrUpiPSP(staticQrUpiPSPRequest);
        Response response = staticQrUpiPSP.execute();
        StaticQrUpiPSPResponse staticQrUpiPSPResponse = response.as(StaticQrUpiPSPResponse.class);
        Assertions.assertThat(staticQrUpiPSPResponse.getBody().getResultCode()).isEqualToIgnoringCase("SUCCESS");
        UPIIntentRequestDTO upiIntentRequestDTO = new UPIIntentRequestDTO();
        upiIntentRequestDTO.setAmount("213.23")
                .setInstaUrl(LocalConfig.PGP_HOST)
                .setOrderId(staticQrUpiPSPResponse.getBody().getOrderId())
                .setExternalSerialNo(staticQrUpiPSPResponse.getBody().getExternalSerialNo())
                .setTxnStatus(Constants.Intent_Callback.SUCCESS.getStatus())
                .setResponseCode(Constants.Intent_Callback.SUCCESS.getRespCode())
                .setResponseMessage(Constants.Intent_Callback.SUCCESS.getRespmsg())
                .setMid(merchant.getId());
        response = PGPHelpers.generateUpiIntentPayRequest(upiIntentRequestDTO);
        Assertions.assertThat(response.jsonPath().getString("body.resultCode")).isEqualToIgnoringCase("SUCCESS");
        String extSn = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, OrderId, "ExtSN=");
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        String callback = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "Bank callback Response");
        Assertions.assertThat(callback).contains("paymentInstrument=PPI_WALLET");
        String paymentResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "FLUXNET_UPI_PG2_PAYMENT_RESULT");
        Assertions.assertThat(paymentResponse).contains("\"paymentInstrument\":\"UPI_PPIWALLET\"");
    }

//     @Owner(HARSHITA)
//     @Feature("PGP-48011")
//     @Test(description = "Verify paymentInstrument field in callback and payment response to router for UPI CC transaction through Push",enabled = false)
    public void PGP_48011_TC04() throws Exception {
        String mpin = "NPCI,20150822,2.0|IvebqsSA1dDVYs3OBn4Q9\\/cOgJ5RecQQW7WCe4EOJBniwCUqI9ocIE50GMcbA5UPqdQuSO3urywKs47UTc1q1pN51zAeQ0ISxai+Yfii8amtYVeWL67G2lL9RS5NEp29C+7PQc+cL\\/j34mKrtUZvxA\\/GUiAjllwuTnTuud7hMhGNmO8h+fGmctKMrJsWtbULX4EMG\\/bO\\/ayMUpLRynqvR3nM2g8nfblqnukxApr2QJCy3LG0tzaNgVZc8rBmAFVCyweGKijmf0TKSv0dEEmm9js8In1+VH8da13zfwB52zfEPKc6gMtY6QYymGt6Z3Hekcz6gVR+XS8TSIswlReXCA==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        String creditblock = "{\\\"accRefId\\\":\\\"242393\\\",\\\"accountType\\\":\\\"CREDIT\\\",\\\"bank\\\":\\\"My Bene\\\",\\\"bankLogoUrl\\\":\\\"https:\\/\\/static.paytmbank.com\\/upi\\/images\\/bank-logo\\/000000.png\\\",\\\"bankMetaData\\\":{\\\"bankHealth\\\":{\\\"category\\\":\\\"GREEN\\\",\\\"displayMsg\\\":\\\"\\\"},\\\"perTxnLimit\\\":\\\"100000\\\"},\\\"credsAllowed\\\":[{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"SMS\\\",\\\"CredsAllowedType\\\":\\\"OTP\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"MPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"},{\\\"CredsAllowedDLength\\\":\\\"6\\\",\\\"CredsAllowedDType\\\":\\\"Numeric\\\",\\\"CredsAllowedSubType\\\":\\\"ATMPIN\\\",\\\"CredsAllowedType\\\":\\\"PIN\\\",\\\"dLength\\\":\\\"6\\\"}],\\\"ifsc\\\":\\\"AABE0877543\\\",\\\"maskedAccountNumber\\\":\\\"XXXXXXXXXX355199\\\",\\\"mpinSet\\\":\\\"Y\\\",\\\"name\\\":\\\"ABC\\\",\\\"pgBankCode\\\":\\\"CON3\\\",\\\"txnAllowed\\\":\\\"ALL\\\",\\\"vpaDetail\\\":{\\\"defaultCreditAccRefId\\\":\\\"224646\\\",\\\"defaultDebitAccRefId\\\":\\\"224646\\\",\\\"name\\\":\\\"9759417329@paytm\\\",\\\"primary\\\":true}}";
        User user = userManager.getForRead(Label.PG2WALLETUSER);
        Constants.MerchantType merchant = MerchantType.CancelAllowed;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("1").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setPreferredOtpPage("bank")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setCreditBlock(creditblock)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getSTATUS()).isEqualTo(Constants.TXNSTATUS.TXN_SUCCESS.toString());
        String extSn = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, initTxnDTO.getBody().getOrderId(), "ExtSN=");
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        String callback = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "Bank callback Response");
        Assertions.assertThat(callback).contains("paymentInstrument=CREDIT_CARD");
        String paymentResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy, extSnValue, "FLUXNET_UPI_PG2_PAYMENT_RESULT");
        Assertions.assertThat(paymentResponse).contains("\"paymentInstrument\":\"UPI_CREDIT_CARD\"");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Alert message on Cashier Page UI for Mutual Funds TPV Link via Checkout JS on Enhanced")
    public void PAPR_4739_TC_MF01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        MerchantType merchant = MF_CHECKOUT_ON_ENHANCED;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeMF(merchant.getId(), merchant.getKey(), orderId, "2379", "7777777777", "true", "false").execute().jsonPath().getString("body.txnToken");
        String linkPaymentPage = LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken);
        DriverManager.getDriver().get(linkPaymentPage);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on Cashier Page UI for Mutual Funds TPV Link via Standard Checkout")
    public void PAPR_4739_TC_MF02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        MerchantType merchant = MerchantType.MUTUAL_FUND;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeMF(merchant.getId(), merchant.getKey(), orderId, "2379", "7777777777", "true", "false").execute().jsonPath().getString("body.txnToken");
        String linkPaymentPage = LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken);
        DriverManager.getDriver().get(linkPaymentPage);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on Cashier Page UI for Mutual Funds TPV Transaction via App Invoke Flow")
    public void PAPR_4739_TC_MF03(@Optional("enhancedwap_revamp") String theme) throws Exception {
        MerchantType merchant = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setAggrMid(MUTUAL_FUND_AGGR.getId())
                .setRequestType("NATIVE_MF")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("7777777777")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert not present on Cashier Page UI for Mutual Funds Non-TPV Transaction via App Invoke Flow")
    public void PAPR_4739_TC_MF05(@Optional("enhancedweb_revamp") String theme) throws Exception {
        MerchantType merchant = MerchantType.MUTUAL_FUND;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setAggrMid(MUTUAL_FUND_AGGR.getId())
                .setRequestType("NATIVE_MF")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tpvAccountAlert().assertNotVisible();
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Alert message on Cashier Page UI for Stock Trade Link via Checkout JS on Enhanced")
    public void PAPR_4739_TC_ST01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        MerchantType merchant = ST_CHECKOUT_ON_ENHANCED;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeST(merchant.getId(), merchant.getKey(), orderId, "2379", "7777777777", "true", "false").execute().jsonPath().getString("body.txnToken");
        String linkPaymentPage = LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken);
        DriverManager.getDriver().get(linkPaymentPage);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on Cashier Page UI for Stock Trade TPV Link via Standard Checkout")
    public void PAPR_4739_TC_ST02(@Optional("enhancedweb_revamp") String theme) throws Exception {
        MerchantType merchant = MerchantType.STOCK_TRADE;
        String orderId = CommonHelpers.generateOrderId();
        String txnToken = InitTxn.LinkBasedPaymentNativeST(merchant.getId(), merchant.getKey(), orderId, "2379", "7777777777", "true", "false").execute().jsonPath().getString("body.txnToken");
        String linkPaymentPage = LocalConfig.PGP_HOST + Constants.NativeAPIResourcePath.SHOW_LINK_PAYMENT_PAGE.replace("{mid}", merchant.getId()).replace("{orderId}", orderId).replace("{txnToken}", txnToken);
        DriverManager.getDriver().get(linkPaymentPage);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on Cashier Page UI for Stock Trade TPV Transaction via App Invoke Flow")
    public void PAPR_4739_TC_ST03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        MerchantType merchant = MerchantType.STOCK_TRADE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE_ST")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("7777777777")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert is not present on Cashier Page UI for Stock Trade Non-TPV Transaction via App Invoke Flow")
    public void PAPR_4739_TC_ST05(@Optional("enhancedwap_revamp") String theme) throws Exception {
        MerchantType merchant = MerchantType.STOCK_TRADE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setRequestType("NATIVE_ST")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath initTrxJsonPath = initTxn.execute().jsonPath();
        String txnToken = initTrxJsonPath.getString("body.txnToken");
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tpvAccountAlert().assertNotVisible();
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert on Cashier Page UI for SIP TPV Transaction via App Invoke Flow")
    public void PAPR_4739_TC_SIP03(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = MF_SIP_Pg2_MID3;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("7777777777")
                .setTxnValue("2")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String alertMsg = cashierPage.tpvAccountAlert().getText();
        Assertions.assertThat(alertMsg).isEqualTo("Important! Please ensure you are paying using your a/c XXXXXX7777");
        cashierPage.tpvAccountInfo().click();
        Assertions.assertThat(cashierPage.tpvAccountAlertInfo().getText()).contains("has mentioned your a/c XXXXXX7777 while creating this payment request.\nTo complete the payment please ensure that you pay ONLY using the above-mentioned bank account");
    }

    @Owner(HARSHITA)
    @Feature("PAPR-4739")
    @Parameters({"theme"})
    @Test(description = "validate Account Alert is not present on Cashier Page UI for SIP Non-TPV Transaction via App Invoke Flow")
    public void PAPR_4739_TC_SIP05(@Optional("enhancedwap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_Pg2_MID3;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("25000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionRetryCount("1")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.AppInvokeOrder(merchant, initTxnDTO.getBody().getOrderId(), txnToken)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        checkoutPage.createAppInvokeOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tpvAccountAlert().assertNotVisible();
    }

    @Owner(MONIKA_NAGARIA)
    @Feature("PGP-48586")
    @Parameters({"theme"})
    @Test(description = "When dccCurrencyPreferenceInr=true, INR should be given priority for DCC Payment & should come on top with pre-selection")
    public void verifyDCCVisaComplianceINR(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(DCC_PG2_MDR, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4000000000001091");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.currencyPageINR().isDisplayed();
        String text = cashierPage.currencyPageINR().getText();
        String currency = cashierPage.currency().getText();
        Assertions.assertThat(text).isEqualTo("INR");
        Assertions.assertThat(currency).contains("₹");

    }


    @Owner(MONIKA_NAGARIA)
    @Feature("PGP-48586")
    @Parameters({"theme"})
    @Test(description = "When dccCurrencyPreferenceInr=false, USD should be given priority for DCC Payment & should come on top with pre-selection")
    public void verifyDCCVisaComplianceUSD(@Optional("enhancedweb_revamp") String theme) throws Exception {
        OrderDTO orderDTO = new OrderFactory.PGOnly(DCC_PG2_USD, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber("4000000000001091");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.currencyPageINR().isDisplayed();
        String text = cashierPage.currencyPageINR().getText();
        String currency = cashierPage.currency().getText();
        Assertions.assertThat(text).isEqualTo("USD");
        Assertions.assertThat(currency).contains("$");


    }

    @Owner(AJEESH)
    @Feature("PGP-56181")
    @Parameters({"theme"})
    @Test(description = "Verify Callback logger is printed for 443 port")
    public void verifyLoggerforCallbackPort443(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .setCallBack_URL("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespMsg("Txn Success")
                .assertAll();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia.log | " + "grep \"validateCallbackUrl()\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("Port 443 found in callback url");
    }
    @Owner(AJEESH)
    @Feature("PGP-56181")
    @Parameters({"theme"})
    @Test(description = "Verify Callback logger is printed for 80 port")
    public void verifyLoggerforCallbackPort80(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .setCallBack_URL("http://10.170.7.123:80/mockbank/MerchantSite/bankResponse")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        DriverManager.getDriver().findElement(By.id("proceed-button")).click();
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia.log | " + "grep \"validateCallbackUrl()\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("Port 80 found in callback url");
    }

    @Owner(AJEESH)
    @Feature("PAPR-6185")
    @Parameters({"theme"})
    @Test(description = "Verify when ff4j flag theia.fetchBinIdentifierFromMerchantCenter is off, old flow mid/misc/info/ should be called from merchant center.")
    public void verifywhenFF4jFlagisOffOldFlowisCalled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = MerchantType.PGOnly;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.COBRANDED_CC));
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia_facade.log | " + "grep \"mid/misc/info/\" |" +" grep \"RESPONSE\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_FACADE, grepcmd);

        System.out.println("Theia logs are : " + theiaLogs);
        Assertions.assertThat(theiaLogs).isNotNull();
    }

    @Owner(AJEESH)
    @Feature("PAPR-6185")
    @Parameters({"theme"})
    @Test(description = "Verify when ff4j flag theia.fetchBinIdentifierFromMerchantCenter is on, new flow merchant/bin/info should be called from merchant center.")
    public void verifywhenFF4jFlagisOnNewFlowisCalled(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String configuredPromo="Toke9";
        Constants.MerchantType merchant = Constants.MerchantType.EMI_DISCOVERY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.COBRANDED_CC));
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia_facade.log | " + "grep \"merchant/bin/info\" |" +" grep \"RESPONSE\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_FACADE, grepcmd);

        System.out.println("Theia logs are : " + theiaLogs);
        Assertions.assertThat(theiaLogs).contains("Toke9");
    }
    @Owner(AJEESH)
    @Feature("PAPR-6185")
    @Parameters({"theme"})
    @Test(description = "Verfiy binIdentifier recieved in merchant/bin/info is passed in ACQUIRING_PAY_ORDER")
    public void verifyBinIdentifierisPassedinACQUIRING_PAY_ORDER(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String configuredPromo="Toke9";
        Constants.MerchantType merchant = Constants.MerchantType.EMI_DISCOVERY;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(PayMode.CC, new PaymentDTO().setCreditCardNumber(PaymentDTO.COBRANDED_CC));
        String orderId = orderDTO.getORDER_ID();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia_facade.log | " + "grep \"merchant/bin/info\" |" +" grep \"RESPONSE\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_FACADE, grepcmd);

        System.out.println("Theia logs are : " + theiaLogs);
        Assertions.assertThat(theiaLogs).contains(configuredPromo);

        String grepcmd1 = "grep \"" + merchant.getId() + "\" /paytm/logs/theia_facade.log | " + "grep \"ACQUIRING_PAY_ORDER\" |" +" grep \"REQUEST\"";
        String theiaLogs1 = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_FACADE, grepcmd1);
        System.out.println("Theia logs are : " + theiaLogs1);
        Assertions.assertThat(theiaLogs1).contains("\"binIdentifier\":\"Toke9\"");
    }
    String convText ="Convenience fees are fees applied by PG to end customers as per payment instrument to facilitate payment services to end users efficiently.";

    @Owner(AJEESH)
    @Feature("PAPR-6229")
    @Parameters({"theme"})
    @Test(description = "Verfiy that for a merchant with pcf should be shown a tooltip in paymode Credit Card, and on clicking the same Overlay is shown with expected description.")
    public void verifytooltipforConvenienceFeesMerchantisShownforCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_MERCHANT1;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        cashierPage.pcfConvenienceInfoIcon().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        Assert.assertEquals(cashierPage.pcfConvenienceInfoTextNew().getText(),convText);
    }
    @Owner(AJEESH)
    @Feature("PAPR-6229")
    @Parameters({"theme"})
    @Test(description = "Verfiy that for a merchant with pcf should be shown a tooltip in paymode Debit Card, and on clicking the same Overlay is shown with expected description.")
    public void verifytooltipforConvenienceFeesMerchantisShownforDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_MERCHANT1;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.scrollToElement(cashierPage.tabDebitCard());
        cashierPage.tabDebitCard().waitUntilClickable();
        cashierPage.tabDebitCard().click();
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getDebitCardNumber());
        cashierPage.pcfConvenienceInfoIcon().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        Assert.assertEquals(cashierPage.pcfConvenienceInfoTextNew().getText(),convText);
    }
    @Owner(AJEESH)
    @Feature("PAPR-6229")
    @Parameters({"theme"})
    @Test(description = "Verfiy that for a merchant with pcf should be shown a tooltip in paymode netBanking, and on clicking the same Overlay is shown with expected description.")
    public void verifytooltipforConvenienceFeesMerchantisShownforNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PCF_MERCHANT1;
        OrderDTO orderDTO = new OrderFactory.PGOnly(merchant, theme)
                .setTXN_AMOUNT("20.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        cashierPage.tabNetBanking().waitUntilClickable();
        cashierPage.tabNetBanking().click();
        cashierPage.pcfConvenienceInfoIcon().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        Assert.assertEquals(cashierPage.pcfConvenienceInfoTextNew().getText(),convText);
    }
    @Owner(AJEESH)
    @Feature("PGP-58888")
    @Test(description = "Validate functionality for TPV transaction where image parameter should be true")
    public void verifyTheTPVTrueImageParameterInTheiaFacadeLog() throws InterruptedException {

        Constants.MerchantType MerchID = MerchantType.TPV_MERCHANT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchID)
                .setTxnValue("2.00")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("83748239234872")
                .ifsc("AABD0000011")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchID.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        builder.setQrImageRequired(true);
        ProcessTxnV1Request processTxnV1Request = builder
                .build();


        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPathResponse.getString("body.deepLinkInfo.image")).isNotNull();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId,"ACQUIRING_CREATE_ORDER_AND_PAY");
        System.out.println(logsResponse);
        softAssertions.assertThat(logsResponse)
                .as("Check if logs contain QR_REQUESTED when QRRequired is true")
                .contains("QR_REQUESTED");

        softAssertions.assertThat(logsResponse)
                .as("Check if logs contain verification type TPV")
                .contains("verificationType\":\"TPV\"");
        softAssertions.assertAll();
    }


    @Owner(AJEESH)
    @Feature("PGP-58888")
    @Test(description = "Validate functionality for TPV transaction where image parameter should be false")
    public void verifyTheTPVFalseImageParameterFalseInTheiaFacadeLog() throws InterruptedException {

        Constants.MerchantType MerchID = MerchantType.TPV_MERCHANT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchID)
                .setTxnValue("2.00")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("83748239234872")
                .ifsc("AABD0000011")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchID.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        builder.setQrImageRequired(false);
        ProcessTxnV1Request processTxnV1Request = builder
                .build();

        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPathResponse.getString("body.deepLinkInfo.image"))
                .as("QR Image should be null when qrImageRequired is false")
                .isNull();

        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId,"ACQUIRING_CREATE_ORDER_AND_PAY");
        System.out.println(logsResponse);
        softAssertions.assertThat(logsResponse)
                .as("Logs should not contain QR_REQUESTED when QRRequired is false")
                .doesNotContain("QR_REQUESTED");
        softAssertions.assertThat(logsResponse)
                .as("Check if logs contain verification type TPV")
                .contains("verificationType\":\"TPV\"");

        softAssertions.assertAll();
    }

    @Owner(AJEESH)
    @Feature("PGP-58888")
    @Test(description = "Validate functionality for NON TPV transaction where image parameter should be true")
    public void verifyTheNONTPVTrueImageParameterInTheiaFacadeLog() throws InterruptedException {

        Constants.MerchantType MerchIDNumber = NON_TPV_MERCHANT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchIDNumber)
                .setTxnValue("2.00")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("83748239234872")
                .ifsc("AABD0000011")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchIDNumber.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        builder.setQrImageRequired(true);
        ProcessTxnV1Request processTxnV1Request = builder
                .build();


        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPathResponse.getString("body.deepLinkInfo.image")).isNotNull();
        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId,"ACQUIRING_CREATE_ORDER_AND_PAY");
        System.out.println(logsResponse);
        softAssertions.assertThat(logsResponse)
                .as("Check if logs contain QR_REQUESTED when QRRequired is true")
                .contains("QR_REQUESTED");

        softAssertions.assertThat(logsResponse)
                .as("Check if logs contain verification type NON TPV")
                .contains("verificationType\":\"NON_TPV\"");
        softAssertions.assertAll();
    }

    @Owner(AJEESH)
    @Feature("PGP-58888")
    @Test(description = "Validate functionality for NON TPV transaction where image parameter should be false")
    public void verifyTheNONTPVFalseImageParameterInTheiaFacadeLog() throws InterruptedException {

        MerchantType MerchIDNumber = NON_TPV_MERCHANT;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, MerchIDNumber)
                .setTxnValue("2.00")
                .setValidateAccountNumber("true")
                .setAllowUnverifiedAccount("false")
                .setAccountNumber("83748239234872")
                .ifsc("AABD0000011")
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderId = initTxnDTO.orderFromBody();

        ProcessTxnV1Request.Builder builder = new ProcessTxnV1Request.Builder(MerchIDNumber.getId(), txnToken, orderId);
        builder.setPaymentMode("UPI_INTENT");
        builder.setAuthMode("USRPWD");
        builder.setPaymentFlow("NONE");
        builder.setQrImageRequired(false);
        ProcessTxnV1Request processTxnV1Request = builder
                .build();


        ProcessTransactionV1 processTransaction = new ProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPathResponse = processTransaction.execute().jsonPath();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(jsonPathResponse.getString("body.deepLinkInfo.image"))
                .as("QR Image should be null when qrImageRequired is false")
                .isNull();

        String logsResponse = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderId);
        System.out.println(logsResponse);
        softAssertions.assertThat(logsResponse)
                .as("Logs should not contain QR_REQUESTED when QRRequired is false")
                .doesNotContain("QR_REQUESTED");
        softAssertions.assertThat(logsResponse)
                .as("Check if logs contain verification type NON TPV")
                .contains("verificationType\":\"NON_TPV\"");
        softAssertions.assertAll();
    }
}
