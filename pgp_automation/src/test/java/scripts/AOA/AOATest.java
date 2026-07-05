package scripts.AOA;

import com.paytm.ServerConfigProvider;
import com.paytm.api.AOA.*;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.BankMandatePaymentResponse;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.base.test.UserManager;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.*;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.PayMode.CC;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

@Feature("PGP-34368")

public class AOATest extends PGPBaseTest {
    public String PAYTMPG= "PAYTMPG";
    public String CCAVENUE= "CCAVENUEAOA";
    public String PAYUAOA= "PAYUAOA";
    public String BILLDESK = "BILLDESKAOA";
    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
    private final CheckoutPage checkoutPageNative = new CheckoutPage();
    private final DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
    SoftAssertions softly = new SoftAssertions();
    private final CheckoutPage checkoutPage1 = new CheckoutPage();


    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To verify Response of AddMerchant API")
    public void verifyingAddMerchantAPI()  {
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson.getString("resultResp.merchantId").isEmpty()).isFalse();
        softly.assertAll();
    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To verify Response of AddGateway API" , dependsOnMethods = "verifyingAddMerchantAPI")
    public void verifying_ADD_PAYTM_PG_GatewayAPI(){
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,merchant.getId(),PAYTMPG,merchant.getKey());
        JsonPath withDrawJson1 = addGateway.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson1.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson1.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson1.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson1.getString("response.messaage")).isEqualTo("Success");
        softly.assertAll();

    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To verify Response of AddGateway API")
    public void verifyingAdd_PAY_U_GatewayAPI(){
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        String MerchantId = "JrfAyH";
        String EntityKey = "{\"salt\":”GWgOwjLZ\"}";
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,MerchantId,PAYUAOA,EntityKey);
        JsonPath withDrawJson1 = addGateway.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson1.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson1.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson1.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson1.getString("response.messaage")).isEqualTo("Success");
        softly.assertAll();

    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To verify Response of AddGateway API")
    public void verifyingAdd_CC_AVENUE_GatewayAPI(){
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        String MerchantId = "275";
        String EntityKey = "{\"access_key_server\":\"2FC98C2A10D4FA60823E02D01382E44D\",\"access_key\":\"AVQM79FG28BN02MQNB\",\"working_key\":\"2FC98C2A10D4FA60823E02D01382E44D\",\"code_key_server\":\"AVQM79FG28BN02MQNB”}";
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,MerchantId,CCAVENUE,EntityKey);
        JsonPath withDrawJson1 = addGateway.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson1.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson1.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson1.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson1.getString("response.messaage")).isEqualTo("Success");
        softly.assertAll();

    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To verify Response of AddGateway API")
    public void verifyingAdd_CC_BILLDESK_GatewayAPI(){
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        String MerchantId = "PAYTMLM";
        String EntityKey = "{\"anyParamter\":\"4RZZru9Lwt3YyYDPGcD2UE3HAK5sKT9X”}\n";
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,MerchantId,BILLDESK,EntityKey);
        JsonPath withDrawJson1 = addGateway.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson1.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson1.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson1.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson1.getString("response.messaage")).isEqualTo("Success");
        softly.assertAll();

    }


    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To pass MCC_CODE='Retail' ,verifying success response and to check if we are getting correct mcc code or not")
    public void verifyingAddAcquiringAPI_DEBIT_CARD() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,merchant.getId(),PAYTMPG,merchant.getKey());
        addGateway.execute();
        String Paymethod = "DEBIT_CARD";
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId,Paymethod,PAYTMPG);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson2.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson2.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.mcc")).isEqualTo("Retail");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.merchantId")).isEqualTo(AoaMerchantId);
        softly.assertAll();


    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To pass MCC_CODE='Retail' ,verifying success response and to check if we are getting correct mcc code or not")
    public void verifyingAddAcquiringAPI_CREDIT_CARD() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,merchant.getId(),PAYTMPG,merchant.getKey());
        addGateway.execute();
        String Paymethod = "CREDIT_CARD";
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId,Paymethod,PAYTMPG);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson2.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson2.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.mcc")).isEqualTo("Retail");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.merchantId")).isEqualTo(AoaMerchantId);
        softly.assertAll();


    }


    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To pass MCC_CODE='Retail' ,verifying success response and to check if we are getting correct mcc code or not")
    public void verifyingAddAcquiringAPI_WALLET() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,merchant.getId(),PAYTMPG,merchant.getKey());
        addGateway.execute();
        String Paymethod = "WALLET";
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId,Paymethod,PAYTMPG);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson2.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson2.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.mcc")).isEqualTo("Retail");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.merchantId")).isEqualTo(AoaMerchantId);
        softly.assertAll();


    }


    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "To pass MCC_CODE='Retail' ,verifying success response and to check if we are getting correct mcc code or not")
    public void verifyingAddAcquiringAPI_UPI() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,merchant.getId(),PAYTMPG,merchant.getKey());
        addGateway.execute();
        String Paymethod = "UPI";
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId,Paymethod,PAYTMPG);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(withDrawJson2.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson2.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.mcc")).isEqualTo("Retail");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.merchantId")).isEqualTo(AoaMerchantId);
        softly.assertAll();

        QueryAcquiring queryAcquiring = new QueryAcquiring().buildRequest(AoaMerchantId);

        JsonPath withDrawJson3 = queryAcquiring.execute().jsonPath();
        softly.assertThat(withDrawJson3.getString("resultResp.acquiringConfigInfo.recordId")).isNotNull();
        softly.assertThat(withDrawJson3.getString("resultResp.acquiringConfigInfo.subServiceInstIds")).isNotNull();
        softly.assertAll();


    }


    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "Verifying Query Acquiring API")
    public void verifyingQuery_Acquiring_API() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,merchant.getId(),PAYTMPG,merchant.getKey());
        addGateway.execute();
        String PayMethod = "CREDIT_CARD";
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId,PayMethod,PAYTMPG);
        addAcquiring.execute();
        SoftAssertions softly = new SoftAssertions();
        QueryAcquiring queryAcquiring = new QueryAcquiring().buildRequest(AoaMerchantId);

        JsonPath withDrawJson3 = queryAcquiring.execute().jsonPath();
        softly.assertThat(withDrawJson3.getString("resultResp.acquiringConfigInfo.recordId")).isNotNull();
        softly.assertThat(withDrawJson3.getString("resultResp.acquiringConfigInfo.subServiceInstIds")).isNotNull();
        softly.assertAll();


    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Test(description = "Verifying Delete Acquiring API")
    public void verifyingDelete_Acquiring_API() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId,merchant.getId(),PAYTMPG,merchant.getKey());
        addGateway.execute();
        String PayMethod = "CREDIT_CARD";
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId,PayMethod,PAYTMPG);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();

        SoftAssertions softly = new SoftAssertions();

        String recordId = withDrawJson2.getString("resultResp.acquiringConfigInfo.recordId");
        System.out.println(recordId);
        DeleteAcquiring deleteAcquiring = new DeleteAcquiring();
        deleteAcquiring.buildRequest(AoaMerchantId,recordId);
        JsonPath withDrawJson3 = deleteAcquiring.execute().jsonPath();
        softly.assertThat(withDrawJson3.getString("restStatus")).isEqualTo("SUCCESS");
        softly.assertThat(withDrawJson3.getString("response.resultCode")).isEqualTo("00000");
        softly.assertThat(withDrawJson3.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson3.getString("response.messaage")).isEqualTo("Success");
        softly.assertAll();
    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying CC TXN from AOA Merchant")
    public void verifying_Successful_CC_TXN_(@Optional("checkoutjs_web_revamp") String theme) throws IOException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT,1)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying DC TXN from AOA Merchant")
    public void verifying_Successful_DC_TXN_(@Optional("checkoutjs_wap_revamp") String theme) throws IOException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,AOA_MERCHANT)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

    }


    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"isNativePlus"})
    @Test(description = "To verify CC txn on NATIVE_PLUS")
    public void verifying_Successful_CC_TXN_NATIVEPLUS(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType merchantType = AOA_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPageNative.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();
    }

    @Feature("PGP-34368")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters({"isNativePlus"})
    @Test(description = "To verify DC txn on NATIVE_PLUS")
    public void verifying_Successful_DC_TXN_NATIVEPLUS(@Optional("false") Boolean isNativePlus) {
        Constants.MerchantType merchantType = AOA_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        checkoutPageNative.createNativeOrder(orderDTO, isNativePlus);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();
    }

    @Feature("PGP-37266")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying Direct OTP CC TXN from AOA Merchant on CheckoutJS page")
    public void verifying_Successful_DirectOTP_BANK_TXN(@Optional("checkoutjs_web_revamp") String theme) throws IOException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,AOA_MERCHANT_HDFO)
                //.setMerchantKey("")
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        DriverManager.getDriver().switchTo().window(parent);

        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();


    }


    @Feature("PGP-37266")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying Direct OTP CC TXN from AOA Merchant on native plus flow")
        public void verifying_Successful_DirectOTP_BANK_TXN_Native_plus(@Optional("false") Boolean isNativePlus) {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, AOA_MERCHANT_HDFO).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(AOA_MERCHANT_HDFO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPageNative.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.submitOtp(PaymentDTO.bankOtp);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();


    }

    @Feature("PGP-37266")
    @Parameters({"theme"})
    @Test(description = "Verifying Direct OTP CC TXN from AOA Merchant on CheckoutJS Element Page")
    public void verifying_Successful_DirectOTP_BANK_TXN_CheckoutJS_Element(@Optional("checkoutjse_web") String theme) throws Exception {
        Constants.MerchantType merchantType = AOA_MERCHANT_HDFO;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("CC");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        DriverManager.getDriver().switchTo().window(parent);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PAYTM.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();

    }

    @Feature("PGP-37266")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying Falied Direct OTP CC TXN on CheckoutJS flow")
    public void verifying_Failed_DirectOTP_BANK_TXN(@Optional("checkoutjs_web_revamp") String theme) throws IOException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,AOA_MERCHANT_HDFO)
                //.setMerchantKey("")
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp(PaymentDTO.FAILED_OTP);
        DriverManager.getDriver().switchTo().window(parent);

        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .assertAll();


    }

    @Feature("PGP-37266")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying Failure Direct OTP CC TXN from AOA Merchant via NativePlus flow")
    public void verifying_Failure_DirectOTP_BANK_TXN_CheckoutJS(@Optional("false") Boolean isNativePlus) {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, AOA_MERCHANT_HDFO).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(AOA_MERCHANT_HDFO, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        checkoutPageNative.createNativeOrder(orderDTO, isNativePlus);
        directBankOTPPage.submitOtp(PaymentDTO.FAILED_OTP);
        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .assertAll();


    }

    @Feature("PGP-37266")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying Failure Txn via canceling OTP Page")
    public void verifying_FailureTxn_VIA_Cancelling_BankPage(@Optional("checkoutjs_web_revamp") String theme) throws IOException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,AOA_MERCHANT_HDFO)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.cancel().click();
        directBankOTPPage.modalCancelPayment().accept();
        DriverManager.getDriver().switchTo().window(parent);

        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .assertAll();

    }


    @Feature("PGP-37266")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying toUseDirectPayment flag via successful TXN on Bank OTP page")
    public void verifying_toUseDirectPayment_Flag_in_DirectOTP_BANK_TXN(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,AOA_MERCHANT_HDFO)
                //.setMerchantKey("")
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        DriverManager.getDriver().switchTo().window(parent);

        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

        String cmdToFetchSendOTPRequest = "grep '" + initTxnDTO.getBody().getOrderId() + "' /paytm/logs/theia_facade.log | grep 'ACQUIRING_CREATE_ORDER_AND_PAY'";
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest);
        Assertions.assertThat(theiaFacadelogs).as("").contains("\\\"toUseDirectPayment\\\":\\\"true\\\"");



    }

    @Feature("PGP-37266")
    @Owner(Constants.Owner.PRIYANSHI)
    @Parameters("{theme}")
    @Test(description = "Verifying formAOARequest flag via successful TXN on Bank OTP page")
    public void verifying_formAOARequest_Flag_in_DirectOTP_BANK_TXN(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,AOA_MERCHANT_HDFO)
                //.setMerchantKey("")
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(CC);
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        DriverManager.getDriver().switchTo().window(parent);

        new ResponsePage().waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

        String cmdToFetchSendOTPRequest = "grep '" + initTxnDTO.getBody().getOrderId() + "' /paytm/logs/theia.log | grep 'PaymentS2SRequest'";
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest);
        Assertions.assertThat(theiaFacadelogs).as("").contains("fromAOARequest='true'");



    }


    @Owner(Constants.Owner.HIMANSHU)
    @Feature("PGP-37511")
    @Test(description = "To verify presence of mid in response of getByAOAmidGatewayName")
    public void PGP_37511_verifyMidPresence_getByAOAMidGatewayName()
    {
        Constants.MerchantType merchantType = AOA_MERCHANT;
        GetByAoaMidGatewayName getGatewayName = new GetByAoaMidGatewayName(merchantType.getId());
        Response response = getGatewayName.execute();
        String gatewayName=response.jsonPath().getString("resultResp.configDetails.mid");
        Assertions.assertThat(gatewayName.length()!=0).isTrue();
    }

    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verify CC unsupported Schemes as VISA added on AOA merchant ")
    public void verifying_AddAcquiring_For_UnsupportedCardScheme() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId, merchant.getId(), PAYTMPG, merchant.getKey());
        addGateway.execute();
        String Paymethod = "CREDIT_CARD";
        ArrayList<String> unsupportedScheme = new ArrayList<String>();
        ArrayList<String> unsupportedCardSubTypes = new ArrayList<String>();
        ArrayList<String> unsupportedIssuingBanks = new ArrayList<String>();
        ArrayList<String> unsupportedUpiPayMethods = new ArrayList<String>();
        unsupportedScheme.add("VISA");
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId, Paymethod, PAYTMPG, unsupportedScheme , unsupportedCardSubTypes, unsupportedIssuingBanks, unsupportedUpiPayMethods);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.unsupportedSchemes")).isEqualTo("[VISA]");
        softly.assertAll();
    }

    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verify CC unsupported Card SubTypes as PREPAID added on AOA merchant ")
    public void verifying_AddAcquiring_For_unsupportedCardSubTypes() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId, merchant.getId(), PAYTMPG, merchant.getKey());
        addGateway.execute();
        String Paymethod = "CREDIT_CARD";
        ArrayList<String> unsupportedScheme = new ArrayList<String>();
        ArrayList<String> unsupportedCardSubTypes = new ArrayList<String>();
        ArrayList<String> unsupportedIssuingBanks = new ArrayList<String>();
        ArrayList<String> unsupportedUpiPayMethods = new ArrayList<String>();
        unsupportedCardSubTypes.add("PREPAID");
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId, Paymethod, PAYTMPG, unsupportedScheme , unsupportedCardSubTypes, unsupportedIssuingBanks, unsupportedUpiPayMethods);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.unsupportedCardSubTypes")).isEqualTo("[PREPAID]");
        softly.assertAll();
    }

    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verify NB unsupported Issuing Bank as HDFC added on AOA merchant ")
    public void verifying_AddAcquiring_For_unsupportedIssuingBanks() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId, merchant.getId(), PAYTMPG, merchant.getKey());
        addGateway.execute();
        String Paymethod = "NET_BANKING";
        ArrayList<String> unsupportedScheme = new ArrayList<String>();
        ArrayList<String> unsupportedCardSubTypes = new ArrayList<String>();
        ArrayList<String> unsupportedIssuingBanks = new ArrayList<String>();
        ArrayList<String> unsupportedUpiPayMethods = new ArrayList<String>();
        unsupportedIssuingBanks.add("HDFC");
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId, Paymethod, PAYTMPG, unsupportedScheme , unsupportedCardSubTypes, unsupportedIssuingBanks, unsupportedUpiPayMethods);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.unsupportedIssuingBanks")).isEqualTo("[HDFC]");
        softly.assertAll();
    }

    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verify UPI unsupported UPI Pay method as UPI added on AOA merchant ")
    public void verifying_AddAcquiring_For_unsupportedUpiPayMethods() {
        Constants.MerchantType merchant = Constants.MerchantType.AOA_MERCHANT_PG;
        AddMerchant addMerchant = new AddMerchant();
        JsonPath withDrawJson = addMerchant.execute().jsonPath();
        String AoaMerchantId = withDrawJson.getString("resultResp.merchantId");
        AddGateway addGateway = new AddGateway().buildRequest(AoaMerchantId, merchant.getId(), PAYTMPG, merchant.getKey());
        addGateway.execute();
        String Paymethod = "UPI";
        ArrayList<String> unsupportedScheme = new ArrayList<String>();
        ArrayList<String> unsupportedCardSubTypes = new ArrayList<String>();
        ArrayList<String> unsupportedIssuingBanks = new ArrayList<String>();
        ArrayList<String> unsupportedUpiPayMethods = new ArrayList<String>();
        unsupportedUpiPayMethods.add("UPI");
        AddAcquiring addAcquiring = new AddAcquiring().buildRequest(AoaMerchantId, Paymethod, PAYTMPG, unsupportedScheme , unsupportedCardSubTypes, unsupportedIssuingBanks, unsupportedUpiPayMethods);
        JsonPath withDrawJson2 = addAcquiring.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(withDrawJson2.getString("response.resultStatus")).isEqualTo("S");
        softly.assertThat(withDrawJson2.getString("response.messaage")).isEqualTo("Success");
        softly.assertThat(withDrawJson2.getString("resultResp.acquiringConfigInfo.unsupportedUpiPayMethods")).isEqualTo("[UPI]");
        softly.assertAll();
    }

    public String theiaFacadeLogs(String orderId) throws InterruptedException {
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia_facade.log | " +
                "grep \"AOA_PAYMENT_CASHIER_LITEPAYVIEW_CONSULT\" | grep \"payChannelOptionViews\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
       return theiaFacadeLogs ;
    }

    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verfiy FPO response should not contain VISA for AOA merchant transaction when unsupportedScheme = VISA is added in acquiring")
    public void verifying_FPO_For_UnsupportedCardScheme() throws InterruptedException {
        Constants.MerchantType merchantType = AOA_VISA;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(AOA_VISA.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();

        String theiaFacadeLogs = theiaFacadeLogs(orderId);
        Assert.assertFalse(theiaFacadeLogs.contains("VISA"));
    }

    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verfiy FPO response should contain prepaidCardSupported as False for AOA merchant transaction when unsupportedCardSubTypes = PREPAID is added in acquiring")
    public void verifying_FPO_For_unsupportedCardSubTypes() throws InterruptedException {
        Constants.MerchantType merchantType = AOA_PREPAID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(AOA_PREPAID.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();

        String theiaFacadeLogs = theiaFacadeLogs(orderId);
        Assertions.assertThat(theiaFacadeLogs).contains("\"prepaidCardChannel\":\"false\"");
    }


    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verfiy FPO response should contain HDFC gateway for AOA merchant NB transaction when unsupportedIssuingBanks = HDFC is added in acquiring")
    public void verifying_FPO_For_unsupportedIssuingBanks() throws InterruptedException {
        Constants.MerchantType merchantType = AOA_ISSUINGBANK;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(AOA_ISSUINGBANK.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();

        String theiaFacadeLogs = theiaFacadeLogs(orderId);
        Assert.assertFalse(theiaFacadeLogs.contains("HDFC"));
    }

    @Feature("PGP-37265")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verfiy FPO response should not contain UPI PayOption for AOA merchant UPI transaction when unsupportedUpiPayMethods = UPI is added in acquiring")
    public void verifying_FPO_For_unsupportedUpiPayMethods() throws InterruptedException {
        Constants.MerchantType merchantType = AOA_UPIPAYMETHOD;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(AOA_UPIPAYMETHOD.getId(), orderId, fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();

        String theiaFacadeLogs = theiaFacadeLogs(orderId);
        Assert.assertFalse(theiaFacadeLogs.contains("\"payOption\":\"UPI\""));
    }
    @Feature("PGP-39610")
    @Owner(Constants.Owner.HIMANSHU)
    @Parameters("{theme}")
    @Test(description = "Verfiy wallet txn through native flow on AOA mid")
    public void nativeWalletFlow_AOA(@Optional("enhancedweb_revamp") String theme) throws Exception
    {
       User user = userManager.getForWrite(Label.LOGIN);
       double txnAmmount = 2;
       WalletHelpers.modifyBalance(user,txnAmmount);
       String orderId=CommonHelpers.generateOrderId();
       Constants.MerchantType merchantType = AOA_MERCHANT;
       //INIT TXN
       InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT)
                .setOrderId(orderId)
                .setTxnValue(toString().valueOf(txnAmmount))
                .setRequestType("UNI_PAY")
                .build();
       String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
       //FPO
       FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setWorkFlow("checkout").build();
       FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(merchantType.getId(),orderId,fetchPaymentOptionsDTO);
       JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
       //PTC
       ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, orderId)
                .setChannelId("WAP")
                .setPaymentMode("WALLET")
                .setRequestType("NATIVE")
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        //PTC & TXN Status
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

    }


    @Feature("PGP-38743")
    @Owner(Constants.Owner.HIMANSHU)
    @Parameters("{theme}")
    @Test(description = "Verfiy wallet support on checkoutJS for AOA Merchant")
    public void verify_AOAWalletSupportonCheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        User user = userManager.getForWrite(Label.LOGIN);
        double txnAmmount = 2;
        WalletHelpers.modifyBalance(user,txnAmmount);
        //successful txn with wallet
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT)
                .setTxnValue(toString().valueOf(txnAmmount))
                .setRequestType("UNI_PAY")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        cashierPage.waitUntilLoads();

        /*
         * After invoking wallet paymode, child window will open where we'll login and make payment using wallet
         * This child window will be closed once payment is done
         * TXN status will be shown on parent window
         * */

        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window))
            {
                DriverManager.getDriver().switchTo().window(child_window);

            }
        }
        cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        DriverManager.getDriver().switchTo().window(parent);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();
    }

    @Feature("PGP-38743")
    @Owner(Constants.Owner.HIMANSHU)
    @Parameters("{theme}")
    @Test(description = "Verfiy wallet support on elementJS for AOA Merchant")
    public void verify_AOAWalletSupportonElementJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {

        User user = userManager.getForWrite(Label.LOGIN);
        double txnAmmount = 2;
        WalletHelpers.modifyBalance(user,txnAmmount);
        //successful txn with wallet
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT)
                .setTxnValue(toString().valueOf(txnAmmount))
                .setRequestType("UNI_PAY")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CheckoutJsCheckoutMerchantElementPage checkoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        checkoutPage.createAndInvokePaymode("PAY WITH PAYTM");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        cashierPage.waitUntilLoads();

        /*
         * After invoking wallet paymode, child window will open where we'll login and make payment using wallet
         * This child window will be closed once payment is done
         * TXN status will be shown on parent window
         * */
        String parent= DriverManager.getDriver().getWindowHandle();
        Set<String> s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();

        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window))
            {
                DriverManager.getDriver().switchTo().window(child_window);

            }
        }
        cashierPage = CashierPageFactory.getCashierPage("enhancedweb_revamp");
        cashierPage.waitUntilLoads();
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        DriverManager.getDriver().switchTo().window(parent);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();
    }

    @Feature("PGP-39590")
    @Owner(Constants.Owner.POOJA)
    @Test(description = "Verify Request Sent To TimeOutCentre from theia in AOA Transaction")
    public void verifying_Request_Sent_To_TimeOutCentre() throws InterruptedException {
        Constants.MerchantType merchantType = AOA_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = InitTxn.executeInitTxn(initTxnDTO);
        initTxnResponseDTO.getBody().getResultInfo().getResultMsg().equals("Success");

        String orderId = initTxnDTO.orderFromBody();

        String timeOutCentreRequest = "grep '" + initTxnDTO.getBody().getOrderId() + "' /paytm/logs/theia_facade.log | grep 'timeoutcenter'";
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, timeOutCentreRequest);
        Assertions.assertThat(theiaFacadelogs).as("").contains("\"API_NAME\": \"https://qa-router-internal.paytm.com/timeoutcenter/orders\"");
        Assertions.assertThat(theiaFacadelogs).as("").contains("target=https://qa-router-internal.paytm.com/timeoutcenter/orders");

    }

    @Feature("PGP-39578")
    @Owner(Constants.Owner.POOJA)
    @Parameters("{theme}")
    @Test(description = "Max Life AOA - One Time EMI Payments")
    public void aoa_One_Time_EMI_Payments(@Optional("checkoutjse_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AOA_MERCHANT;
        InitTxnDTO initTxnDTO =new InitTxnDTO.Builder(null,merchantType)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        CheckoutJsCheckoutMerchantElementPage elementCheckoutPage = new CheckoutJsCheckoutMerchantElementPage();
        MerchantConfig config = elementCheckoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setTokenType("TXN_TOKEN");
        config.data.setToken(txnToken);
        elementCheckoutPage.createCheckoutJsOrder(config);
        elementCheckoutPage.createAndInvokePaymode("EMI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Feature("PGP-39041")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify successful peon for AOA")
    public void verifying_Success_Peon_AOA_CC_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT,1)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"AoAPeonSentServiceImpl.getFormToPost()\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("STATUS=[TXN_SUCCESS]");
    }

    @Feature("PGP-39041")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "Verify successful peon for AOA")
    public void verifying_Success_Peon_AOA_NB_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT,1)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.orderFromBody();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI Bank"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .assertAll();

        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/communicationGateway.log | " +
                "grep \"AoAPeonSentServiceImpl.getFormToPost()\"";
        String logs = getLogsOnServer(ServerConfigProvider.SERVICE.COMMUNICATION_GATEWAY, grepcmd);
        Assertions.assertThat(logs).contains("STATUS=[TXN_SUCCESS]");
    }

    @Feature("PGP-39552")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "Seamless payment of UPI Collect (without redirection) on JS checkout")
    public void verifying_Seamless_payment_of_UPICollect_without_redirection(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT,1)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String pGMid = Constants.MerchantType.AOA_MERCHANT_PG.getId();
        String orderId = initTxnDTO.orderFromBody();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        Assert.assertFalse(cashierPage.waitForNewWindow(2),"New window is not open");
        cashierPage.tabCheckoutUPIPollingImg().assertVisible();

        String grepEsn = "grep \"" + orderId + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");

        String esnLogsgrep = "grep \""+extSnValue+"\"  /paytm/logs/instaproxy.log |grep \"Final Form Status\"";
        String formLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, esnLogsgrep);

        Assertions.assertThat(formLogs).contains("AGGREGATOR");
        Assertions.assertThat(formLogs).contains("UPI");
        Assertions.assertThat(formLogs).contains("|S|");
    }


    @Feature("PGP-43044")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "verify that polling is removed when theia.enableSocketBasedPollingForUPICollect flag is true on AOA merchant")
    public void enableSocketBasedPollingForUPICollect_true_on_AOA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT_HDFO,1)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String pGMid = Constants.MerchantType.HDFO.getId();
        String orderId = initTxnDTO.orderFromBody();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        String grepEsn = "grep \"" + orderId + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");

        String esnLogsgrep = "grep \""+extSnValue+"\"  /paytm/logs/instaproxy.log |grep \"Final Form Status\"";
        String formLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, esnLogsgrep);

        String grepcmd2 = "grep \"" + orderId + "\" /paytm/logs/socketcluster/websocket.log";
        String webSocketClusterLogs = getLogsOnServer(ServerConfigProvider.SERVICE.WEB_SOCKET_CLUSTER, grepcmd2);
        Assertions.assertThat(webSocketClusterLogs).contains("UPI_COLLECT_AOA");
        Assertions.assertThat(webSocketClusterLogs).contains("socket join room successfully");

        Assertions.assertThat(formLogs).contains("AGGREGATOR");
        Assertions.assertThat(formLogs).contains("UPI");
        Assertions.assertThat(formLogs).contains("|S|");
    }

    @Feature("PGP-43044")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "verify that PG merchant payload is pushed to UPI_COLLECT_PG topic for AOA UPI collect transaction")
    public void verify_that_PG_merchant_payload_is_pushed_to_UPI_COLLECT_PG_topic_AOA_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT_HDFO,1)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String pGMid = Constants.MerchantType.HDFO.getId();
        String orderId = initTxnDTO.orderFromBody();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        String grepEsn = "grep \"" + orderId + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");

        String esnLogsgrep = "grep \""+extSnValue+"\"  /paytm/logs/instaproxy.log |grep \"Final Form Status\"";
        String formLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, esnLogsgrep);
        cashierPage.pause(30);
        String grepcmd1 = "grep \""+orderId+"\" /paytm/logs/pgproxy-notification.log";
        String notificationQueueHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1);
        Assertions.assertThat(notificationQueueHandlerLogs).contains("Payload pushed to new-KAFKA successfully for topic : UPI_COLLECT_PG");

        Assertions.assertThat(formLogs).contains("AGGREGATOR");
        Assertions.assertThat(formLogs).contains("UPI");
        Assertions.assertThat(formLogs).contains("|S|");
    }

    @Feature("PGP-43044")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "verify that polling is removed when theia.enableSocketBasedPollingForUPICollect flag is true on PG merchant with checkoutjs flow")
    public void enableSocketBasedPollingForUPICollect_true_on_PG_CheckoutJs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.HDFO;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String grepcmd2 = "grep \"" + orderId + "\" /paytm/logs/socketcluster/websocket.log";
        String webSocketClusterLogs = getLogsOnServer(ServerConfigProvider.SERVICE.WEB_SOCKET_CLUSTER, grepcmd2);
        Assertions.assertThat(webSocketClusterLogs).contains("UPI_COLLECT_PG");
        Assertions.assertThat(webSocketClusterLogs).contains("socket join room successfully");
        Assertions.assertThat(webSocketClusterLogs).contains("kafka payload received in api call response");
        Assertions.assertThat(webSocketClusterLogs).contains("\"topicName\":\"UPI_COLLECT_PG\"");

    }


    @Feature("PGP-43044")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "verify that PG merchant payload is pushed to UPI_COLLECT_PG topic for PG UPI collect transaction")
    public void verify_that_PG_merchant_payload_is_pushed_to_UPI_COLLECT_PG_topic_PG_Txn(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(AOA_MERCHANT_PG, theme)
                .setTXN_AMOUNT("100.00")
                .build();
        String orderId = orderDTO.getORDER_ID();
        checkoutPage1.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

       String grepcmd1 = "grep \"" + orderId + "\" /paytm/logs/pgproxy-notification.log";
       String notificationQueueHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1);
       Assertions.assertThat(notificationQueueHandlerLogs).contains("Payload pushed to new-KAFKA successfully for topic : UPI_COLLECT_PG");

    }

    @Feature("PGP-43044")
    @Owner(Constants.Owner.POOJA)
    @Parameters({"theme"})
    @Test(description = "verify that polling is happening when theia.disableSocketBasedPollingForUPICollect flag is true on AOA merchant")
    public void disableSocketBasedPollingForUPICollect_true_on_AOA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder( null,AOA_MERCHANT,1)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String pGMid = Constants.MerchantType.HDFO.getId();
        String orderId = initTxnDTO.orderFromBody();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);

        String grepEsn = "grep \"" + orderId + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");

        String esnLogsgrep = "grep \""+extSnValue+"\"  /paytm/logs/instaproxy.log |grep \"Final Form Status\"";
        String formLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, esnLogsgrep);

        String socketcmd = "grep \"socketcluster-node\" /paytm/logs/socketcluster/websocket.log";
        String webSocketClusterLogs = getLogsOnServer(ServerConfigProvider.SERVICE.WEB_SOCKET_CLUSTER, socketcmd);
        Assertions.assertThat(webSocketClusterLogs).doesNotContain(orderId);

        Assertions.assertThat(formLogs).contains("AGGREGATOR");
        Assertions.assertThat(formLogs).contains("UPI");
        Assertions.assertThat(formLogs).contains("|S|");
    }
}



