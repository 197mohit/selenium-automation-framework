package scripts.Sprint28;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.CommonHelpers;
import org.assertj.core.api.SoftAssertions;
import com.paytm.ServerConfigProvider;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


   /* Applicable only on deferred flow only i.e.
     FetchPayOptions will be called before initiateTxn */

//PGP16441
@Owner("Tarun")
public class RiskAddMoney extends PGPBaseTest {

    private final Map<String,String> riskProperties = new ConcurrentHashMap<>();
    private final String riskPayMode = "paymodes.for.risk.consult";
    private final String riskPercentage = "CREDIT_CARD.risk.consult.fee.percent";
    private final String riskMessage = "CREDIT_CARD.risk.consult.message";
    private final String initialAmount = "100";
    private final String feeAmount = "2";
    private final String totalAmount = "102";

    //Most of the test cases are dependent on SCW merchant
    private final Constants.MerchantType merchantType = Constants.MerchantType.AddMoneyMP;

    RiskAddMoney()
    {
        //similar to what we have in /etc/appconf/project-theia.prop

        riskProperties.put(riskPayMode,"[CREDIT_CARD]");
        riskProperties.put(riskPercentage,"[2.00]");
        riskProperties.put(riskMessage,"[Your bank/payment network charges a fee on using Credit card to add money to Wallet. Please use UPI or Debit card option to add money free of cost. To know more, visit https://www.paytmbank.com/ratesCharges.]");
    }

    @Test(description = "To check whether we get risk object in fetchpayoptions for risk rejected user.")
    public void fetchPayOptionForRiskRejectedUser() throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED); //risk reject user

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();

        Assertions.assertThat(fetchPaymentOptResponse.getString("body"))
                .as("riskResult is coming as ACCEPT for : "+user.mobNo() + " please refer to theia_facade logs")
                .contains("riskConvenienceFee");

        Assertions.assertThat(fetchPaymentOptResponse.getString("body.riskConvenienceFee.payMethod"))
                .as("Risk Pay method doesn't match")
                .isEqualTo(riskProperties.get(riskPayMode));

        Assertions.assertThat(fetchPaymentOptResponse.getString("body.riskConvenienceFee.feePercent"))
                .as("Risk Pay method doesn't match")
                .isEqualTo(riskProperties.get(riskPercentage));

        Assertions.assertThat(fetchPaymentOptResponse.getString("body.riskConvenienceFee.reason"))
                .as("Risk Pay method doesn't match")
                .isEqualTo(riskProperties.get(riskMessage));
    }

    @Test(description = "To check no risk object is present for normal user which are not risk rejected.")
    public void fetchPayOptionForNonRiskRejectedUser () throws Exception {
        User user = userManager.getForRead(Label.BASIC);//non risky user

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body")).
                as("Fetch Pay response invalid").
                contains("paymentModes").
                as("Fetch Pay contains risk object for non risky user").
                doesNotContain("riskConvenienceFee");
    }

//Negative Test case
@Epic(Constants.Sprint.SPRINT31_1)
@Feature("PGP-19896")
@Owner("Tarun")
@Test(description = "To check whether txn token is generated with total amount combining of initial value and risk % fee.")
    public void validatePTCFailWhenTxnAmountAndSumOfInitialAndRiskAmountMismatch() throws Exception {

        ExtendInfo initExtendInfo = new ExtendInfo();
        initExtendInfo.setOrderAdditionalInfo(new OrderAdditionalInfo().setMName("Automation").setMID(Constants.MerchantType.NATIVE_HYBRID.getId()).setMcc("1234").setMLogo("Paytm"));

        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        String totalAmount = "101";

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setExtendInfo(initExtendInfo)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(),initTxnResponse.getBody().getTxnToken() , orderId)
                .setPaymentMode("CREDIT_CARD")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg"))
                .as("Incorrect initial/fee amount combo is getting passed")
                .isEqualTo("Invalid Txn Amount");

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.STATUS"))
                .as("Incorrect initial/fee amount combo is getting passed")
                .isEqualTo("TXN_FAILURE");

        Assertions.assertThat(response.jsonPath().getString("body.txnInfo.RESPMSG"))
                .as("Response Message is incorrect")
                .isEqualTo("Invalid Txn Amount");

    }
    @Parameters({"isNativePlus"})
    @Test(description = "To check whether we can proceed with success txn after adding risk convinience fee for SCW Merchant. ")
    public void validateTxnForRiskConvFee(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(totalAmount)
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
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .validateStatusAPIParameters()
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "To check whether we donot get risk reject from alipay in PTC if we send feeApplied as true in Process transaction.")
    public void feeAppliedTrueInPTC(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

    TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
            .validateOrderid(orderDTO.getORDER_ID())
            .validateTxnAmount(totalAmount)
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
            .validateCardHash(Constants.ValidationType.NON_EMPTY)
            .validateStatusAPIParameters()
            .AssertAll();
        }

    @Parameters({"isNativePlus"})
    @Test(description = "To check whether we get risk reject if we send feeApplied as false")
    public void feeAppliedFalse(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Assertions.assertThat(txnToken).
                as("Txn token is not generated in initiate txn response").isNotEmpty();


        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:false")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("Risk Reject")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .assertAll();

    }

    //should fail but getting passed
    @Parameters({"isNativePlus"})
    @Test(description = "To check that process txn should pass when feeApplied:true flag is passed for normal merchants")
    public void validatePTCDifferentMerchant(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                //.setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();
        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true") //will be ignored
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(totalAmount)
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
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "To check if we donot send risk fee in ptc for risky user then txn should get passed.")
    public void txnGetDeclinedIfFeeNotPassed(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                //.setRiskFeeDetails(new RiskFeeDetails().setFeeAmount("2").setInitialAmount("100"))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        String txnToken = initTxnResponse.getBody().getTxnToken();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                //.setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(totalAmount)
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
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "To check if the risk fee send in ptc is not same as mentioned in properties , txn should get failed.")
    public void riskFeeDifferentFromProp(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount("3").setInitialAmount("99"))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateRespMsg("Risk Reject")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "To check we donot get risk fee object for normal merchants other than scw in fetchpayoptions.")
    public void noRiskFeeForNormalMerchant() throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.get("body").toString()).
                as("Fetch Pay response invalid").
                contains("paymentModes").
                as("Fetch Pay contains risk object for normal merchants").
                doesNotContain("riskConvenienceFee");

    }

       @Issue("PGP-20684")
       @Parameters({"isNativePlus"})
       @Test(description = "To check that process txn should fail for the blacklisted merchants",groups = Group.Status.BUG)
       public void ptcFailForBlacklistedMerchants(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.BLACKLISTED_MERCHANT;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage
                .validateRespMsg("Risk Reject")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .assertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify when risk fee is not applied for other paymodes")
    public void verifyRiskFeeNotAppliedForOtherPayMode(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();


        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                //.setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.DEBIT_CARD)
                //.setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(totalAmount)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();

    }
    //check the txn status response
    @Parameters({"isNativePlus"})
    @Test(description = "To check whether other add money txn is getting success with other paymodes")
    public void addMoneyWithOtherPayModes(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.ADDMONEYREJECTED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Assertions.assertThat(txnToken).
                as("Txn token is not generated in initiate txn response").isNotEmpty();
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI").build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(totalAmount)
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Add Money txn using saved card ")
    public void addMoneySavedCard(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(Label.ADDMONEYREJECTED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), orderId);         //validating txn status
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(totalAmount)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(merchantType.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateCardHash(Constants.ValidationType.NON_EMPTY)
                .AssertAll();



    }

    @Issue("PGP-20684")
    @Parameters({"isNativePlus"})
    @Test(description = "To check that process txn should fail for the blacklisted merchants if we are not sending feeApplied flag in PTC.",groups = Group.Status.BUG)
    public void ptcFailBlacklistedIfFeeNotApplied(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.ADDMONEYREJECTED);
        Constants.MerchantType merchantType = Constants.MerchantType.BLACKLISTED_MERCHANT;
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                //   .setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage
                .validateRespMsg("Risk Reject")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .assertAll();


    }

    @Parameters({"isNativePlus"})
    @Test(description = "GV test scenario")
    public void testGVScenario(@Optional("true") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.MINKYCEXPIRED);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("SSO")
                .setGenerateOrderId(Boolean.toString(true))
                .setMid(merchantType.getId())
                .setToken(user.ssoToken())
                .setOrderAmount(initialAmount)
                .build();

        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
        String orderId = fetchPaymentOptResponse.get("body.orderId").toString();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue(totalAmount)
                .setOrderId(orderId)
                .setIsNativeAddMoney("true")
                .setRiskFeeDetails(new RiskFeeDetails().setFeeAmount(feeAmount).setInitialAmount(initialAmount))
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        Assertions.assertThat(txnToken).
                as("Txn token is not generated in initiate txn response").isNotEmpty();

        OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                .setRiskExtendInfo("feeApplied:true")
                .build();

        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage
                .validateRespMsg("Risk Reject")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Parameters({"isNativePlus"})
    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-26893")
    @Test(description = "Verify add money using CC when add money limit using credit card is breached (Risk_Reject)")
    public void riskRejectTransit(@Optional("true") Boolean isNativePlus) throws Exception {
            User user = userManager.getForRead(Label.MINKYCEXPIRED);
            Double txnAmt = 2.0;
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                    .setTokenType("SSO")
                    .setGenerateOrderId(Boolean.toString(true))
                    .setMid(merchantType.getId())
                    .setToken(user.ssoToken())
                    .setOrderAmount(initialAmount)
                    .build();

            FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(),fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptResponse = fetchPaymentOption.execute().jsonPath();
            String orderId = fetchPaymentOptResponse.get("body.orderId").toString();

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setOrderId(orderId)
                .setTxnValue(String.valueOf(txnAmt))
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("329915210"); //Adding money in Transit Wallet

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(merchantType.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            Assertions.assertThat(txnToken).
                    as("Txn token is not generated in initiate txn response").isNotEmpty();

            OrderDTO orderDTO = new OrderFactory.Native(merchantType, orderId, txnToken, PayMethodType.CREDIT_CARD)
                    .setRiskExtendInfo("feeApplied:true")
                    .build();

            CheckoutPage checkoutPage = new CheckoutPage();
            checkoutPage.createNativeOrder(orderDTO,isNativePlus);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();

        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        String grepcmd = "grep \"" + orderDTO.getORDER_ID() + "\" /paytm/logs/theia_facade.log | " +
                "grep \"" + orderDTO.getMID() +"\" | grep \"PAYMODE_DECISION_MAKER_TASK\" | grep \"REQUEST\"";
        String payModeDecisionMaker = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(payModeDecisionMaker).as("Add Money Destination is incorrect").contains("TRANSIT_BLOCKED_WALLET");

        }

    @Feature("PGP-46810")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "To verify v1/processTransaction response for AddMoney via CC when ff4j theia.enableFundsFailureResponse is ON")
    public void addMoneyRiskReject_01() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SURCHARGE_FUNDS_FAILURE;
        String orderId = LocalConfig.ENV_NAME+"_"+ CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("22.10")
                .setOrderId(orderId)
                .setIsNativeAddMoney("true")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setChannelId("WEB")
                .setAuthMode("otp")
                .setCardInfo("|4718650100030136|333|122029")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Process Fail");
        softly.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.retry")).isEqualTo("false");
        softly.assertThat(ptcResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("System Error");
        softly.assertAll();
    }

    @Feature("PGP-46810")
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "To verify v1/processTransaction response for AddMoney via CC when ff4j theia.enableFundsFailureResponse is OFF")
    public void addMoneyRiskReject_02() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.ADD_MONEY_SURCHARGE;
        String orderId = LocalConfig.ENV_NAME+"_"+ CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchantType)
                .setTxnValue("22.10")
                .setOrderId(orderId)
                .setIsNativeAddMoney("true")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), txnToken, orderId)
                .setPaymentMode("CREDIT_CARD")
                .setChannelId("WEB")
                .setAuthMode("otp")
                .setCardInfo("|4718650100030136|333|122029")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response ptcResponse = processTransactionV1.execute();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Transaction declined in staging");
        softly.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.retry")).isEqualTo("true");
        softly.assertThat(ptcResponse.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Risk Reject");
        softly.assertAll();
    }


}
