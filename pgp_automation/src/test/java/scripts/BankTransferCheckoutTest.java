package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.api.UpiPredicate;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TpvInfo;
import com.paytm.dto.NativeDTO.InitTxn.VanInfo;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import com.paytm.api.CallBackApi;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.util.Arrays;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import reactor.core.Exceptions;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static org.hamcrest.Matchers.*;

@Owner(Constants.Owner.PRIYANSHI)
@Feature("PGP-32205")
public class BankTransferCheckoutTest extends PGPBaseTest {
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
    String request = "{\n" +
            "  \"event_tracking_id\": \"231-1600409754102-785511\",\n" +
            "  \"ca_id\": \"710000000021\",\n" +
            "  \"data\": {\n" +
            "    \"amount\": \"20.00\",\n" +
            "    \"remitterIfsc\": \"BACB0000003\",\n" +
            "    \"vanNumber\": \"PYMONY8006006993\",\n" +
            "    \"beneficiaryAccountNumber\": \"710000000021\",\n" +
            "    \"transactionRequestId\": \"346783245985\",\n" +
            "    \"remitterNbin\": \"1234\",\n" +
            "    \"remitterBankName\": \"CANARA BANK LTD\",\n" +
            "    \"remitterName\": \"Srivastava Kumar Prateek1597233845332\",\n" +
           "    \"transactionDate\": \"09-18-2020 00:00:00\",\n" +
            "    \"remitterAccountNumber\": \"915555340164\",\n" +
            "    \"parentUtr\": null,\n" +
            "    \"responseCode\":\"0\",\n" +
            "    \"transactionType\": \"VAN_INWARD\",\n" +
            "    \"beneficiaryIfsc\": \"PYTM0123456\",\n" +
            "    \"meta\": {\n" +
            "      \"flow\":\"PAYTM_CONTROLLED\",\n" +
            "      \"Mobile Number\": \"5267478725\",\n" +
            "      \"vanExtendInfo\": {\"purpose\":\"Dont\",\"merchantPrefix\":\"MONY\"},\n" +
            "      \"Client Name\": \"OCL\",\n" +
            "      \"mid\": \"testli64434832718875\",\n" +
            "      \"identificationNo\": \"8006006993\",\n" +
            "      \"customerDetails\":[{\"customerEmail\":\"kp@gmail.com\",\"customerMobile\":\"7234567890\",\"customerName\":\"test\"}],\n" +
            "      \"userDefinedFields\": \"\"\n" +
            "    },\n" +
            "    \"transferMode\": \"IMPS\",\n" +
            "    \"bankTxnIdentifier\": \"5R01IY000V27\",\n" +
            "    \"status\": \"SUCCESS\"\n" +
            "  }\n" +
            "  }";
/*TODO:  Have to set these properties on Van :
   1. project-van.properties =>
    a. van.url.ppbl.base=http://10.144.18.108:8088/mockbank
    b. merchant.detail.query.base=http://10.144.18.108:8088/mockbank/bosspanel
   2. facade.properties
      a. signature.validation=NO

 */


    @Parameters({"theme"})
    @Test(description = "Validate a Success txn for merchant controlled flag when user already logged in")
    public void txnVia_MerchantControlled_Merchant_LogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG55";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG55\\\"}";

        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",identificationNo)
           //     .deleteContext("data.transactionDate")
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "Validate a Success txn for Payment controlled flag when user already logged in")
    public void txnVia_PaymentControlled_Merchant_LogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Validate the txn when MERCHANT_CONTROLLED is applied on merchant and PAYTM_CONTROLLED is recieved in call back api")
    public void validateTxn_whenMerchantControlledApplied_paytmControlledReceived_LogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        String flow = "PAYTM_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG55";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG55\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",identificationNo)
                .deleteContext("data.meta.customerDetails");
        Response response = callBack.execute();
        response.then().body("data.errorMessage",equalTo("Bank Transfer Not available"));

    }

    @Parameters({"theme"})
    @Test(description = "Validate the txn when PAYTM_CONTROLLED is applied on merchant and MERCHANT_CONTROLLED is recieved in call back api")
    public void validateTxn_whenPaytmControlledApplied_merchantControlledReceived_LogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",user.mobNo())
                .deleteContext("data.meta.customerDetails");
        Response response = callBack.execute();
        response.then().body("data.errorMessage",equalTo("Bank Transfer Not available"));

    }

    @Parameters({"theme"})
    @Test(description = "Validate a Success txn for merchant controlled flag when user already NON logged in")
    public void txnVia_MerchantControlled_Merchant_NONLogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG55";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.vanSignIn(user);
        cashierPage.bankTansfer().waitUntilClickable();
        cashierPage.bankTansfer().click();
        cashierPage.bankTransferLoginButton().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG55\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",identificationNo)
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

//    @Parameters({"theme"})
//    @Test(description = "Validate a Success txn for Paytm controlled flag when user already NON logged in",enabled = false)
    public void txnVia_PaytmControlled_Merchant_NONLogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.vanSignIn(user);
        cashierPage.bankTansfer().waitUntilClickable();
        cashierPage.bankTansfer().click();
        cashierPage.bankTransferLoginButton().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }


    @Parameters({"theme"})
    @Test(description = "Cancel the txn from Van bank page and try to do txn using another paymode")
    public void cancelledTxnVia_MerchantControlled_Merchant_ThenTryWithCC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;

        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "PG55";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        cashierPage.vanCheckoutElement().waitUntilVisible();
        cashierPage.backButton().click();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("HDFC Bank")
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }


//    @Parameters({"theme"})
//    @Test(description = "Failure txn for Merchant controlled",enabled = false)
    public void failedTxnVia_MerchantControlled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG55";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("67.7")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG55\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request)
                .setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.responseCode","9016")
                .deleteContext("data.meta.customerDetails");
        Response response = callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        response.then().body("data.status",equalTo("FAILURE"));
        response.then().body("data.errorCode",equalTo("3004"));
        response.then().body("data.errorMessage",equalTo("Transaction Failure"));


    }












//    @Parameters({"theme"})
//    @Test(description = "Failure txn for Paytm controlled",enabled = false)
    public void failedTxnVia_PaytmControlled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("67.7")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request)
                .setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.responseCode","9014")
                .setContext("data.meta.identificationNo",user.mobNo())
                .deleteContext("data.meta.customerDetails");
        Response response = callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        response.then().body("data.status",equalTo("FAILURE"));
        response.then().body("data.errorCode",equalTo("3004"));
        response.then().body("data.errorMessage",equalTo("Transaction Failure"));
    }

    @Parameters({"theme"})
    @Test(description = "Success txn for PCF merchant MERCHANT_CONTROLLED")
    public void txnVia_PCFMerchantControlled_Merchant_LogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Double txnAmount = 20.47;
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PCF_MERCHANT;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG75";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setRequestType("Payment")
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().waitUntilClickable();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG75\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",identificationNo)
                .setContext("data.amount",txnAmount)
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        ResponsePage responsePage = new ResponsePage();

        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Success txn for PCF merchant PAYTM_CONTROLLED")
    public void txnVia_PCFPaytmControlled_Merchant_LogIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Double txnAmount = 20.47;
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PCF_PAYTM;
        String flow = "PAYTM_CONTROLLED";
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG85";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setRequestType("Payment")
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().waitUntilClickable();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG85\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",user.mobNo())
                .setContext("data.amount",txnAmount)
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        ResponsePage responsePage = new ResponsePage();

        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }


    //********** UI TEST CASES *************//

    @Parameters({"theme"})
    @Test(description = "Validate that when user is not logged in and click in IMPS/NEFT/RTGS for txn the  login popup should open")
    public void checkingBankTransfer(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PCF_PAYTM;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        Thread.sleep(50000);
        cashierPage.bankTransferLoginPopup().assertVisible();
        cashierPage.vanMobileNumber().sendKeys(user.mobNo());
        cashierPage.bankTransferLoginButton().click();
        cashierPage.bankTransferOTPPopup().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate the error message when invalid mobile number  and OTP is provided")
    public void validating_InvalidMobileNumber_InvalidOTP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.bankTransferLoginPopup().assertVisible();
        cashierPage.vanMobileNumber().sendKeys("4537266266");
        cashierPage.bankTransferLoginButton().click();
        cashierPage.getErrorMessageInavlidMobileNumber().assertVisible();
        cashierPage.vanMobileNumber().clear();
        cashierPage.vanMobileNumber().sendKeys(user.mobNo());
        cashierPage.bankTransferLoginButton().click();
        cashierPage.bankTransferOTPPopup().assertVisible();
        cashierPage.vanOTP().sendKeys("1234");
        cashierPage.checkoutButton().click();
        cashierPage.getErrorMessageInvalidOTP().assertVisible();
    }


    @Parameters({"theme"})
    @Test(description = "Validate the bank Page")
    public void validate_BankForm_Page(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        cashierPage.bankForm().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate the bank Page")
    public void validate_Confirm_Box(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        cashierPage.proceedButton().click();
        cashierPage.confirmBox().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate that if user click on cancel from final screen then user land to bank page")
    public void validate_CancelButton(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        cashierPage.proceedButton().click();
        cashierPage.cancelButton().click();
        cashierPage.bankForm().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate How it works Screen")
    public void validate_HowItsWorksScreen(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.howItWorksLabel().click();
        cashierPage.howItWorksScreen().assertVisible();

    }

    @Parameters({"theme"})
    @Test(description = "Validate Proceed Button from How it works Screen")
    public void validateProceedButtonFrom_HowItsWorksScreen(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.howItWorksLabel().click();
        cashierPage.howItWorksScreen().assertVisible();
        cashierPage.buttonSlider().click();
        cashierPage.checkoutButton().assertClickable();

    }

    @Parameters({"theme"})
    @Test(description = "Validate View Terms and Conditions Screen")
    public void validate_ViewTermsAndConditionScreen(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1390";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.viewTermsAndConditionLabel().click();
        cashierPage.viewTermsAndConditionScreen().assertVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate convenience fee on PCF Merchant")
    public void validate_ConvenienceFeesOnPCF_Merchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PCF_MERCHANT;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "1345";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.convenienceFeeLabel().waitUntilVisible();
        cashierPage.convenienceFeeLabel().assertVisible();
    }



    //*******THEIA TEST CASES*********//


    @Parameters({"theme"})
    @Test(description = "Validate the response of initiate txn api when van info is provided in request")
    public void validateResponseWhenVaninfoProvidedinInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "PG55";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        response.then().assertThat().body("body.resultInfo.resultStatus", equalTo("S"));
        response.then().assertThat().body("body.resultInfo.resultCode", equalTo("0000"));
        response.then().assertThat().body("body.resultInfo.resultMsg", equalTo("Success"));
        response.then().assertThat().body("body.txnToken", notNullValue());

    }
    @Parameters({"theme"})
    @Test(description = "Validate the api response when indentification number is not provided for PAYTM_CONTROLLED flow")
    public void validateResponse_WhenIdentificationnoNotprovided_inPaytmControlled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo)
                .setTxnValue("20.00")
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        response.then().assertThat().body("body.resultInfo.resultStatus", equalTo("S"));
        response.then().assertThat().body("body.resultInfo.resultCode", equalTo("0000"));
        response.then().assertThat().body("body.resultInfo.resultMsg", equalTo("Success"));
        response.then().assertThat().body("body.txnToken", notNullValue());

    }



    @Parameters({"theme"})
    @Test(description = "Validate that when preference is DISABLED then paymode should not displayed on cashier page")
    public void validate_Paymode_WhenPrefrenceis_Disabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.QR_ENABLED_MERCHANT_JS;
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        String merchantPrefix = "ONE7";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().assertNotVisible();
    }

    @Parameters({"theme"})
    @Test(description = "Validate that when van info is not sent in initiate request then paymode should not display")
    public void validate_Paymode_WhenVanInfois_NotProvided(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_MERCHANT_CONTROL;
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().assertNotVisible();
    }

    @Owner(Constants.Owner.AAYUSH)
    @Parameters({"theme"})
    @Test(description = "Validate Van info in Merchant status API after success txn")
    public void vanInfo_in_merchantStatus(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType,vanInfo)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount("20.00")
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        SoftAssertions softly = new SoftAssertions();

        JsonPath txnstatus = txnStatus.execute().jsonPath();
        String res = txnstatus.getString("vanInfo");
        softly.assertThat(res).isNotNull();
        softly.assertAll();

    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate success txn when TPV info provided in intiTxn api")
    public void successTxn_Provided_TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        tpvInfoList.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfoList)
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount("20.00")
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        SoftAssertions softly = new SoftAssertions();

        JsonPath txnstatus = txnStatus.execute().jsonPath();
        String res = txnstatus.getString("vanInfo");
        softly.assertThat(res).isNotNull();
        softly.assertAll();

    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate success txn when TPV info provided in intiTxn api for PCF merchant")
    public void successTxn_Provided_TPVInfoInInitTxn_PCFMerchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Double txnAmount = 20.47;
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PCF_MERCHANT;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG75";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfos = new ArrayList<>();
        tpvInfos.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfos)
                .setRequestType("Payment")
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().waitUntilClickable();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG75\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", identificationNo)
                .setContext("data.amount", txnAmount)
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        ResponsePage responsePage = new ResponsePage();

        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate tpv failure txn when TPV info provided in intiTxn api for PCF merchant")
    public void tpvFailureTxn_Provided_TPVInfoInInitTxn_PCFMerchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Double txnAmount = 20.47;
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PCF_MERCHANT;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG75";
        String responseCode = "9016";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfos = new ArrayList<>();
        tpvInfos.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfos)
                .setRequestType("Payment")
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().waitUntilClickable();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG75\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.responseCode", responseCode)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", identificationNo)
                .setContext("data.amount", txnAmount)
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        ResponsePage responsePage = new ResponsePage();

        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();

        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .assertAll();

    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate Tpv failure txn when TPV info provided in intiTxn api")
    public void tpvFailureTxn_Provided_TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String responseCode = "9016";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        tpvInfoList.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfoList)
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.responseCode", responseCode)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate Tpv failure txn when TPV info provided in intiTxn api again hit callback " +
            "api with 0 response code then txn should success")
    public void tpvFailureTxn_2ndtimeSuccess_Provided_TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String responseCode = "9016";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        tpvInfoList.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfoList)
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.responseCode", responseCode)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        JsonPath Callbackresponse = callBack.execute().jsonPath();
        Assertions.assertThat(Callbackresponse.getString("data.status")).isEqualTo("FAILURE");
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .assertAll();

        CallBackApi callBack1 = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId + "1")
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        JsonPath jsonPath = callBack1.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("data.status")).isEqualTo("SUCCESS");

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount("20.00")
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        SoftAssertions softly = new SoftAssertions();

        JsonPath txnstatus = txnStatus.execute().jsonPath();
        String res = txnstatus.getString("vanInfo");
        softly.assertThat(res).isNotNull();
        softly.assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate Tpv failure txn when TPV info provided in intiTxn api again hit callback \" +\n" +
            "            \"api with 0 response code then txn should success for PCF merchant")
    public void tpvFailureTxn_2ndtimeSuccess_Provided_TPVInfoInInitTxn_PCFMerchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Double txnAmount = 20.47;
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PCF_MERCHANT;
        String flow = "MERCHANT_CONTROLLED";
        Long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        String identificationNo = number.toString();
        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG75";
        String responseCode = "9016";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setIdentificationNo(identificationNo).setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfos = new ArrayList<>();
        tpvInfos.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfos)
                .setRequestType("Payment")
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().waitUntilClickable();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG75\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.responseCode", responseCode)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", identificationNo)
                .setContext("data.amount", txnAmount)
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        ResponsePage responsePage = new ResponsePage();

        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();

        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .assertAll();
        CallBackApi callBack1 = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId + "1")
                .setContext("data.meta.flow", flow)
                .setContext("data.amount", txnAmount)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        JsonPath jsonPath = callBack1.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("data.status")).isEqualTo("SUCCESS");

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount("20.00")
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        SoftAssertions softly = new SoftAssertions();

        JsonPath txnstatus = txnStatus.execute().jsonPath();
        String res = txnstatus.getString("vanInfo");
        softly.assertThat(res).isNotNull();
        softly.assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate success txn when more then 1 TPV info provided in intiTxn api")
    public void successTxn_Provided_MoreThen1TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            TpvInfo t = new TpvInfo("123456" + i, "ifsc1234", "karm", "HDFC",
                    "ACTIVE", "Account", "1234");
            tpvInfoList.add(t);
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfoList)
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber",van)
                .setContext("data.transactionRequestId",transactionRequestId)
                .setContext("data.meta.flow",flow)
                .setContext("data.meta.vanExtendInfo",vanExtend)
                .setContext("data.meta.mid",merchantType.getId())
                .setContext("data.meta.identificationNo",user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount("20.00")
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        SoftAssertions softly = new SoftAssertions();

        JsonPath txnstatus = txnStatus.execute().jsonPath();
        String res = txnstatus.getString("vanInfo");
        softly.assertThat(res).isNotNull();
        softly.assertAll();

    }
    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate success txn when 10 TPV info provided in intiTxn api")
    public void successTxn_Provided_10TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TpvInfo t = new TpvInfo("123456" + i, "ifsc1234", "karm", "HDFC",
                    "ACTIVE", "Account", "1234");
            tpvInfoList.add(t);
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfoList)
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate txn when more then 10 TPV info provided in intiTxn api")
    public void Txn_Provided_MoreThen10TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        for (int i = 0; i < 11; i++) {
            TpvInfo t = new TpvInfo("123456" + i, "ifsc1234", "karm", "HDFC",
                    "ACTIVE", "Account", "1234");
            tpvInfoList.add(t);
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfoList)
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateRespCode("501")
                .validateRespMsg("System Error")
                .validateStatus("TXN_FAILURE")
                .assertAll();

        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + initTxnDTO.getBody().getMid() + "\" | grep \"ACQUIRING_PAY_ORDER\" | grep \"RESPONSE\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("exceed tpvInfos maximum size 10");
    }

    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate success txn for mutual fund when TPV info provided in intiTxn api")
    public void successTxn_MutualFund_Provided_TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType Parent = Constants.MerchantType.Bank_Transfer_MF_Parent;
        Constants.MerchantType Child = Constants.MerchantType.Bank_Transfer_MF_Child;

        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PM25";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        tpvInfoList.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Child, vanInfo).setTpvInfo(tpvInfoList)
                .setRequestType("NATIVE_MF")
                .setMerchantKey(Parent.getKey())
                .setAggrMid(Parent.getId())
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PM25\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", Parent.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount("20.00")
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        SoftAssertions softly = new SoftAssertions();

        JsonPath txnstatus = txnStatus.execute().jsonPath();
        String res = txnstatus.getString("vanInfo");
        softly.assertThat(res).isNotNull();
        softly.assertAll();

    }
    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate tpv failure txn for mutual fund when TPV info provided in intiTxn api")
    public void tpvFailureTxn_MutualFund_Provided_TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType Parent = Constants.MerchantType.Bank_Transfer_MF_Parent;
        Constants.MerchantType Child = Constants.MerchantType.Bank_Transfer_MF_Child;

        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PM25";
        String custId = user.custId();
        String responseCode="9016";
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        tpvInfoList.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Child, vanInfo).setTpvInfo(tpvInfoList)
                .setAggrMid(Parent.getId())
                .setMerchantKey(Parent.getKey())
                .setRequestType("NATIVE_MF")
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PM25\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.responseCode", responseCode)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", Parent.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .assertAll();

    }
    @Owner("Karmvir")
    @Feature("PGP-33587")
    @Parameters({"theme"})
    @Test(description = "Validate Tpv failure txn when TPV info provided in intiTxn api again hit callback \" +\n" +
            "            \"api with 0 response code then txn should success for MF merchant")
    public void tpvFailureTxn_2ndTimeSuccess_MutualFund_Provided_TPVInfoInInitTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType Parent = Constants.MerchantType.Bank_Transfer_MF_Parent;
        Constants.MerchantType Child = Constants.MerchantType.Bank_Transfer_MF_Child;

        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PM25";
        String txnAmount="20";
        String custId = user.custId();
        String responseCode="9016";
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        TpvInfo t1 = new TpvInfo("12345678", "ifsc1234", "karm", "HDFC",
                "ACTIVE", "Account", "1234");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        tpvInfoList.add(t1);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Child, vanInfo).setTpvInfo(tpvInfoList)
                .setAggrMid(Parent.getId())
                .setMerchantKey(Parent.getKey())
                .setRequestType("NATIVE_MF")
                .setTxnValue(txnAmount).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PM25\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.responseCode", responseCode)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", Parent.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .assertAll();
        CallBackApi callBack1 = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId + "1")
                .setContext("data.meta.flow", flow)
                .setContext("data.amount", txnAmount)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", Parent.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        JsonPath jsonPath = callBack1.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("data.status")).isEqualTo("SUCCESS");

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(txnAmount)
                .validateStatus("TXN_SUCCESS")
                .AssertAll();

        SoftAssertions softly = new SoftAssertions();

        JsonPath txnstatus = txnStatus.execute().jsonPath();
        String res = txnstatus.getString("vanInfo");
        softly.assertThat(res).isNotNull();
        softly.assertAll();
    }
    @Owner(Constants.Owner.PRIYANKA)
    @Feature("PGP-35332")
    @Parameters({"theme"})
    @Test(description = "For bank transfer txn , Duplicate txn popup is visible by using same TXN token")
    public void DuplicateTXNPopupvisibleinBankTransfer(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANKTRANSFER_PAYTM_CONTROL;
        String flow = "PAYTM_CONTROLLED";

        Long id = (long) Math.floor(Math.random() * 9_000_000_000L) + 100_000_000_000L;
        String transactionRequestId = id.toString();
        String merchantPrefix = "PG65";
        String custId = user.custId();
        UpiPredicate upiPredicate = new UpiPredicate(custId);
        upiPredicate.execute();
        VanInfo vanInfo = new VanInfo();
        vanInfo.setMerchantPrefix(merchantPrefix).setPurpose("Dont");
        List<TpvInfo> tpvInfoList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TpvInfo t = new TpvInfo("123456" + i, "ifsc1234", "karm", "HDFC",
                    "ACTIVE", "Account", "1234");
            tpvInfoList.add(t);
        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, vanInfo).setTpvInfo(tpvInfoList)
                .setTxnValue("20").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        String van = cashierPage.vanNumber();
        String vanExtend = "{\\\"purpose\\\":\\\"Dont\\\",\\\"merchantPrefix\\\":\\\"PG65\\\"}";
        CallBackApi callBack = (CallBackApi) new CallBackApi(request).setContext("data.vanNumber", van)
                .setContext("data.transactionRequestId", transactionRequestId)
                .setContext("data.meta.flow", flow)
                .setContext("data.meta.vanExtendInfo", vanExtend)
                .setContext("data.meta.mid", merchantType.getId())
                .setContext("data.meta.identificationNo", user.mobNo())
                .deleteContext("data.meta.customerDetails");
        callBack.execute();
        cashierPage.proceedButton().click();
        cashierPage.vanPayButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateBankName("PPBT")
                .validatePaymentMode("BANK_TRANSFER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        MerchantConfig config1 = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config1.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage.bankTansfer().click();
        cashierPage.checkoutButton().click();
        cashierPage.waitUntilLoads();
        cashierPage.DuplicateBankTransferPopUp().assertVisible();
        cashierPage.DuplicateBankTransferMessage().assertVisible();
        cashierPage.DuplicateBankTransferCloseBtn().click();
    }
}
