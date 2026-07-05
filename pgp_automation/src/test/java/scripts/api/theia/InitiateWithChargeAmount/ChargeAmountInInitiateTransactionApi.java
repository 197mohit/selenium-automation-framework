package scripts.api.theia.InitiateWithChargeAmount;

import com.paytm.base.test.PGPBaseTest;
import io.qameta.allure.Owner;
import io.qameta.allure.Feature;
import org.testng.annotations.Test;
import com.paytm.base.test.User;
import com.paytm.appconstants.Constants;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.LocalConfig;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.dto.NativeDTO.ExtendInfo;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.utils.merchant.util.PayMethodType;
import com.paytm.pages.CheckoutPage;
import com.paytm.framework.reporting.Reporter;
import io.restassured.path.json.JsonPath;

import static com.paytm.appconstants.Constants.MerchantType.EMI_ON_TOKEN;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.assertj.core.api.Assertions;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.apphelpers.NativeHelpers;

public class ChargeAmountInInitiateTransactionApi extends PGPBaseTest 
{
    // private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env=" + LocalConfig.ENV_NAME ;
    private CheckoutPage checkoutPage = new CheckoutPage();

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        return fetchPaymentOptionsJson;
    }

    @Owner("Bharat")
    @Feature("PG2-29862")
    @Test(description = "Validate txn token is generated successfully when charge amount is passed in Initiate API")
    public void validateTxnTokenGenerationWithChargeAmountInInitiateAPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.CHARGE_AMOUNT_INITIATE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100.00")
                .setChargeAmount("1.18")
                .build();
        
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        
        System.out.println("InitiateTxn Response: " + initTxnResponse);
        
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotNull();
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.getBody().getOrderId();
        String mid = merchant.getId();
        
        System.out.println("Txn Token generated successfully when charge amount is passed in Initiate API: " + txnToken);
        System.out.println("Order ID: " + orderID);
        System.out.println("MID: " + mid);
    }

    @Owner("Bharat")
    @Feature("PG2-29862")
    @Test(description = "Validate when value of charge amount is blank (value: empty string) in Initiate API, txn token should be generated successfully and txn should be successful")
    public void validateWhenValueOfChargeAmountIsBlankInInitiateAPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.CHARGE_AMOUNT_INITIATE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100.00")
                .setChargeAmount("")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotNull();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.getBody().getOrderId();
        String mid = merchant.getId();
        System.out.println("Txn Token generated successfully: " + txnToken);
        System.out.println("Order ID: " + orderID);
        System.out.println("MID: " + mid);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderID)
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        //Thread.sleep(2000);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderID)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("ICICI Bank")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .validateOrderId(orderID)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("NB");

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("NB")
                .validateMid(mid)
                .validateBankName("ICICI Bank")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateChargeAmount("1.18")
                .AssertAll();   
    }

    @Owner("Bharat")
    @Feature("PG2-29862")
    @Test(description = "Validate when value of charge amount is null (value: null) in Initiate API, txn token should be generated successfully")
    public void validateWhenValueOfChargeAmountIsNullInInitiateAPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.CHARGE_AMOUNT_INITIATE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100.00")
                .setChargeAmount(new TxnAmount(null)) // when charge amount is not passed in Initiate API, it is set as null by default
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotNull();
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.getBody().getOrderId();
        String mid = merchant.getId();
        System.out.println("Txn Token generated successfully: " + txnToken);
        System.out.println("Order ID: " + orderID);
        System.out.println("MID: " + mid);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderID)
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD") 
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        //Thread.sleep(2000);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderID)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("ICICI Bank")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .validateOrderId(orderID)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("NB");

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("NB")
                .validateMid(mid)
                .validateBankName("ICICI Bank")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateChargeAmount("1.18")
                .AssertAll();           
    }

    @Owner("Bharat")
    @Feature("PG2-29862")
    @Test(description = "Validate when value of charge amount is 'null' string in Initiate API")
    public void validateWhenValueOfChargeAmountIsNullAsStringInInitiateAPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.CHARGE_AMOUNT_INITIATE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("100.00")
                .setChargeAmount("null")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(initTxnResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotNull();
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID = initTxnDTO.getBody().getOrderId();
        String mid = merchant.getId();
        System.out.println("Txn Token generated successfully: " + txnToken);
        System.out.println("Order ID: " + orderID);
        System.out.println("MID: " + mid);

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderID)
                .setPaymentMode("NET_BANKING")
                // .setCardInfo("|4718650100010336|333|122025")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Transaction Amount Validation failed, Please try again.");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPCODE()).isEqualTo("209");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid Payment Details");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getMID()).isEqualTo(mid);
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getORDERID()).isEqualTo(orderID);
    }



    @Owner("Bharat")
    @Feature("PG2-29862")
    @Test(description = "Validate incorrect charge amount is passed in Initiate API")
    public void validateIncorrectChargeAmountInInitiateAPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.CHARGE_AMOUNT_INITIATE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("100.00")
        .setChargeAmount("1.19").build(); // passing incorrect charge amount , correct charge amount is 1.18
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String mid = merchant.getId();
        // PaymentDTO paymentDTO = new PaymentDTO();
        // String cardInfo = "|" + PaymentDTO.PROMO_CC_CARD_HDFC + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
        // OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
        //         .setORDER_ID(orderID)
        //         .setCardInfo(cardInfo)
        //         .setAUTH_MODE("otp")
        //         .setCHANNEL_ID("WAP").build();

        // checkoutPage.createNativeOrder(orderDTO, true);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid, txnToken, orderID)
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Transaction Amount Validation failed, Please try again.");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPCODE()).isEqualTo("209");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid Payment Details");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getSTATUS()).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getMID()).isEqualTo(mid);
        Assertions.assertThat(processTxnV1Response.getBody().getTxnInfo().getORDERID()).isEqualTo(orderID);
    }

    @Owner("Bharat")
    @Feature("PG2-29862")
    @Test(description = "Validate correct charge amount is passed in Initiate API")
    public void validateCorrectChargeAmountInInitiateAPI() throws Exception {
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchant = Constants.MerchantType.CHARGE_AMOUNT_INITIATE_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("100")
        .setChargeAmount("1.18").build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String orderID=initTxnDTO.getBody().getOrderId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        //Assertions.assertThat(fetchPaymentOptionsJson.getString("body.paymentOptions.chargeAmount")).isEqualTo("1.18");
        String mid = merchant.getId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                initTxnDTO.getBody().getMid(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        // Thread.sleep(2000);
        System.out.println("ProcessTxnV1 Response: " + processTxnV1Response);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        Assertions.assertThat(processTxnV1Response.getBody().getBankForm().getPageType()).isEqualTo("redirect");
        //Thread.sleep(2000);
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        //NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
        // nativePlusHoldpayPage.launch(LocalConfig.MOCK_HOST + JSON_POST_URL)
        //         .fillAndSubmitJsonForm(processTxnV1Response.toString());

        ResponsePage responsePage = new ResponsePage();
        //Thread.sleep(3000);
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderID)
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("NB")
                .validateBankName("ICICI Bank")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateMid(mid)
                .validateOrderId(orderID)
                .validateChargeAmount("1.18")
                .validateRespMsg("Txn Success");
        
        
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.txnStatusResponse.TXNID.contains("0000")).isTrue();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("NB")
                .validateMid(mid)
                .validateBankName("ICICI Bank")
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateChargeAmount("1.18")
                .AssertAll();
    }



}

