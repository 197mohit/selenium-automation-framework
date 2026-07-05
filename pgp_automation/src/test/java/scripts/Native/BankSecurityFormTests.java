package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.LostInSpacePage;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Date;

@Epic(Constants.Sprint.SPRINT_THEMATIC)
@Feature("PGP-18759")
@Owner("Tarun")
@Owners(author = "Tarun", qa = "Ankur")
/**
 * redirectForm=Y; for you entityId and transaction acquiring
 * redirectionRequired=Y; in Params in PGPDB.FORMATTER_DETAILS for your bank
 */
public class BankSecurityFormTests extends PGPBaseTest {

    private static final String JSON_POST_URL = LocalConfig.JSON_POST_URL;
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private static Integer elementWait;

    @BeforeClass
    private void getWaitTime() {
        elementWait = Integer.parseInt(System.getProperty("MAX_ELEMENT_LOAD_WAIT_TIME", "60"));
    }
    @BeforeMethod
    private void setImplicitWait(Method method, ITestResult testResult) {
        try {
            DriverManager.setWebDriverElementWait(Duration.ofSeconds(100));
        }
     catch(Throwable e) {
        testResult.setStatus(ITestResult.SKIP);
        testResult.setThrowable(new SkipException(method.getName(), e));
    }
    }
    @AfterClass
    private void resetImplicitWait(){
        DriverManager.setWebDriverElementWait(Duration.ofSeconds(elementWait));
    }

    @Test(description = "Validate successful CC transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native)")
    public void t1_native() {
        Constants.MerchantType merchant = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setExtendInfo(new ExtendInfo().setMercUnqRef("testing1"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNHoldNativeOrder(orderDTO, false);
        String responseHTml = checkoutPage.nativeHoldHtmlResponseBox().getText();
        Element bodyElement = Jsoup.parse(responseHTml).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("input"))
                .as("input tag is empty")
                .isNotEmpty();
        Element inputElement = bodyElement.getElementsByTag("input").get(0);
        String externalSerialNo = (inputElement.attributes().get("name") != null) &&
                (inputElement.attributes().get("name").equalsIgnoreCase("extSerialNo")) ?
                inputElement.attributes().get("value") : "";
        Assertions.assertThat(externalSerialNo)
                .as("externalSerialNo is blank")
                .isNotBlank();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        checkoutPage.postNativeHtmlResponse().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful DC transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native)")
    public void t2_native() {
        Constants.MerchantType merchant = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setExtendInfo(new ExtendInfo().setMercUnqRef("testing1"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNHoldNativeOrder(orderDTO, false);
        String responseHTml = checkoutPage.nativeHoldHtmlResponseBox().getText();
        Element bodyElement = Jsoup.parse(responseHTml).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("input"))
                .as("input tag is empty")
                .isNotEmpty();
        Element inputElement = bodyElement.getElementsByTag("input").get(0);
        String externalSerialNo = (inputElement.attributes().get("name") != null) &&
                (inputElement.attributes().get("name").equalsIgnoreCase("extSerialNo")) ?
                inputElement.attributes().get("value") : "";
        Assertions.assertThat(externalSerialNo)
                .as("externalSerialNo is blank")
                .isNotBlank();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        checkoutPage.postNativeHtmlResponse().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful Add n Pay transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native)")
    public void t3_native() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_ADDNPAY;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();

        checkoutPage.createNHoldNativeOrder(orderDTO, false);
        String responseHTml = checkoutPage.nativeHoldHtmlResponseBox().getText();
        Element bodyElement = Jsoup.parse(responseHTml).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("input"))
                .as("input tag is empty")
                .isNotEmpty();
        Element inputElement = bodyElement.getElementsByTag("input").get(0);
        String externalSerialNo = (inputElement.attributes().get("name") != null) &&
                (inputElement.attributes().get("name").equalsIgnoreCase("extSerialNo")) ?
                inputElement.attributes().get("value") : "";
        Assertions.assertThat(externalSerialNo)
                .as("externalSerialNo is blank")
                .isNotBlank();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        checkoutPage.postNativeHtmlResponse().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful Hybrid transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native)")
    public void t4_native() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setPaymentFlow("HYBRID")
                .build();

        checkoutPage.createNHoldNativeOrder(orderDTO, false);
        String responseHTml = checkoutPage.nativeHoldHtmlResponseBox().getText();
        Element bodyElement = Jsoup.parse(responseHTml).body();
        Assertions.assertThat(bodyElement)
                .as("Body not found in html").isNotNull();
        Assertions.assertThat(bodyElement.getElementsByTag("input"))
                .as("input tag is empty")
                .isNotEmpty();
        Element inputElement = bodyElement.getElementsByTag("input").get(0);
        String externalSerialNo = (inputElement.attributes().get("name") != null) &&
                (inputElement.attributes().get("name").equalsIgnoreCase("extSerialNo")) ?
                inputElement.attributes().get("value") : "";
        Assertions.assertThat(externalSerialNo)
                .as("externalSerialNo is blank")
                .isNotBlank();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        checkoutPage.postNativeHtmlResponse().click();
        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Test(description = "Validate successful CC transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native +)")
    public void t1_nativePlus() {
        Constants.MerchantType merchant = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setExtendInfo(new ExtendInfo().setMercUnqRef("testing1"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getActionUrl())
                .as("actionUrl mismatch")
                .isEqualToIgnoringCase("https://pgp-automation.paytm.in/instaproxy/bankresponse/redirectForm");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo())
                .as("extSerialNo is empty")
                .isNotEmpty();
        String externalSerialNo = processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful DC transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native +)")
    public void t2_nativePlus() {
        Constants.MerchantType merchant = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setExtendInfo(new ExtendInfo().setMercUnqRef("testing1"))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("DEBIT_CARD")
                .setCardNum(new PaymentDTO().getDebitCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getActionUrl())
                .as("actionUrl mismatch")
                .isEqualToIgnoringCase("https://pgp-automation.paytm.in/instaproxy/bankresponse/redirectForm");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo())
                .as("extSerialNo is empty")
                .isNotEmpty();
        String externalSerialNo = processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful Add n Pay transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native +)")
    public void t3_nativePlus() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_ADDNPAY;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setPaymentFlow("ADDANDPAY")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getActionUrl())
                .as("actionUrl mismatch")
                .isEqualToIgnoringCase("https://pgp-automation.paytm.in/instaproxy/bankresponse/redirectForm");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo())
                .as("extSerialNo is empty")
                .isNotEmpty();
        String externalSerialNo = processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Validate successful Hybrid transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native +)")
    public void t4_nativePlus() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, txnAmount - 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setPaymentFlow("HYBRID")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getActionUrl())
                .as("actionUrl mismatch")
                .isEqualToIgnoringCase("https://pgp-automation.paytm.in/instaproxy/bankresponse/redirectForm");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo())
                .as("extSerialNo is empty")
                .isNotEmpty();
        String externalSerialNo = processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateChildTxnsPresent()
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.0);
    }

    @Test(description = "Validate successful AddMoney transaction performing with HDFC bank and check dummy form is displayed in response of process transaction api(Native +)")
    public void t5_nativePlus() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 0.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getActionUrl())
                .as("actionUrl mismatch")
                .isEqualToIgnoringCase("https://pgp-automation.paytm.in/instaproxy/bankresponse/redirectForm");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo())
                .as("extSerialNo is empty")
                .isNotEmpty();
        String externalSerialNo = processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();
        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, txnAmount);
    }

    @Test(description = "Validate failure transaction when REDIRECT_FORM_ key deleted from redis (Native +)")
    public void t6_nativePlus() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BANK_SECURITY_FORM_MERCHANT_HYBRID;
        Double txnAmount = 2.0;
        WalletHelpers.modifyBalance(user, 0.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount.toString())
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(merchantType.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("CREDIT_CARD")
                .setCardNum(new PaymentDTO().getCreditCardNumber())
                .setExtendInfoOrderAlreadyCreated(true)
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus())
                .as("Result Status mismatch")
                .isEqualToIgnoringCase("S");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getActionUrl())
                .as("actionUrl mismatch")
                .isEqualToIgnoringCase("https://pgp-automation.paytm.in/instaproxy/bankresponse/redirectForm");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo())
                .as("extSerialNo is empty")
                .isNotEmpty();
        String externalSerialNo = processTxnV1Response.getBody().getBankForm().getRedirectForm().getContent().getExtSerialNo();
        Assertions.assertThat(verifyRedisKey(externalSerialNo))
                .as("REDIRECT_FORM_" + externalSerialNo + " not found in redis")
                .isTrue();

        TRANSACTIONAL_REDIS_CLUSTER().del("REDIRECT_FORM_" + externalSerialNo);
//        RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("REDIRECT_FORM_" + externalSerialNo);

        nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
                .fillAndSubmitJsonForm(processTxnV1Response.toString());
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.waitUntilLoads();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

    @Step("verify key in redis:--> REDIRECT_FORM_{0}")
    private Boolean verifyRedisKey(String externalSerialNo) {
        String key = "REDIRECT_FORM_" + externalSerialNo;
        String value = "";
        while (true) {
            try {
                value = TRANSACTIONAL_REDIS_CLUSTER().get(key);
//                value = RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).get(key);  // key is moved to transactional redis cluster
                break;
            }catch (JedisConnectionException ex) {}
        }

        if (value != null && !value.isEmpty()) {
            return true;
        }
        return false;
    }

}
