package scripts.Native;

import com.paytm.api.GetPaymentStatus;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.api.qr.GenerateQR;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.TxnAmount;
import com.paytm.pages.*;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.json.JSONObject;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.*;

import static io.restassured.RestAssured.given;

@Epic(Constants.Sprint.SPRINT32_3)
@Feature("PGP-21884")
@Owner("Tarun")

// As per new JIRA PGP-24185 FF4j Flag dependency has been removed , now only preference dependency is there
public class NativeMerchantStatus extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();
    private PaymentDTO paymentDTO = new PaymentDTO();
    Constants.MerchantType binResponseMerchant = Constants.MerchantType.BIN_IN_RESPONSE_ADDNPAY;
    Constants.MerchantType MLV = Constants.MerchantType.MLV;
    private static final String JSON_POST_URL = "/checkoutpage/nplus_page.jsp?ttype=hold&jsonresp=";
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();

    private void assertBinAndVPAPreference(Constants.MerchantType merchantType)
    {
        //Merchant should have BIN IN RESPONSE & RETURN_USER_VPA_IN_RESPONSE preference enabled
        prerequisite:
        {
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "BIN_IN_RESPONSE", "Y");
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), "RETURN_USER_VPA_IN_RESPONSE", "Y");

        }

    }

    private Response ExecuteDirectFormRequest(List<HashMap<String, Object>> DirectForm){

        String actionUrl = (DirectForm.get(0)).get("actionUrl").toString();
        String respBody  = new JSONObject((Map<?,?>) (DirectForm.get(0)).get("content"))
                .toString().replace("<OTP>","123456");
        return given().body(respBody).contentType(ContentType.JSON).when().post(actionUrl);

    }


    @Test(description = "Verify new param should come in native TxnStatus API if it is coming in Handler Status API",groups = "P0")
    @Parameters({"theme"})
    public void validateNewParamsInNativeTxnStatus(@Optional("enhancedweb") String theme)  {

        assertBinAndVPAPreference(binResponseMerchant);
        String bin = paymentDTO.getCreditCardNumber().substring(0,6);
        String last4Digit = paymentDTO.getCreditCardNumber().substring(paymentDTO.getCreditCardNumber().length()-4);
        OrderDTO orderDTO = new OrderFactory.PGOnly(binResponseMerchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        //BIN and last 4 Digits should come in native txn status response
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getResponse();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validateCardBin(bin)    // BIN
                .validateLastFourDigits(last4Digit) // Last 4 Digits
                .validateStatusAPIParameters()
                .AssertAll();

        //Native Status API
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), binResponseMerchant.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        //bin and lastFourDigit should come in native txn status API
        Assertions.assertThat(jsonPath.getString("body.bin")).isEqualToIgnoringCase(bin);
        Assertions.assertThat(jsonPath.getString("body.lastFourDigit")).isEqualToIgnoringCase(last4Digit);

    }

    @Test(description = "Verify that vpa parameter should come in native merchant status API even if the parameter is coming in Internal Handler Status API for UPI Transactions",groups = "P0")
    @Parameters({"theme"})
    public void validateVPAInNativeTxnStatus(@Optional("enhancedweb") String theme) {

        assertBinAndVPAPreference(binResponseMerchant);
        OrderDTO orderDTO = new OrderFactory.PGOnly(binResponseMerchant, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        //VPA should come in handler txn status response
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateVPA(paymentDTO.getVpa())
                .validateStatusAPIParameters()
                .AssertAll();

        //Native Status API
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), binResponseMerchant.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.PPBLC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        //VPA should come in native txn status API
        Assertions.assertThat(jsonPath.getString("body.vpa")).isEqualToIgnoringCase(paymentDTO.getVpa());

    }


    @Test(description = "Verify new param should come in native TxnStatus API if it is coming in Handler Status API for Hybrid Success Txn",groups = "P0")
    @Parameters({"theme"})
    public void validateNewParamsInNativeTxnStatusForHybrid(@Optional("enhancedweb") String theme) throws Exception {

        Constants.MerchantType merchantType = Constants.MerchantType.BIN_IN_RESPONSE_HYBRID;
        assertBinAndVPAPreference(merchantType);
        String bin = paymentDTO.getCreditCardNumber().substring(0,6);
        String last4Digit = paymentDTO.getCreditCardNumber().substring(paymentDTO.getCreditCardNumber().length()-4);
        User user = userManager.getForRead(Label.BASIC);
        WalletHelpers.modifyBalance(user,1.00d);
        OrderDTO orderDTO = new OrderFactory.Hybrid(merchantType, theme,user)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        //BIN and last 4 Digits should come in native txn status response
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS")
                .validateBIN(TxnStatus.ChildTxnType.BANK,bin)
                .validateLastFourDigits(TxnStatus.ChildTxnType.BANK,last4Digit);

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

        //Native Status API
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        //bin and lastFourDigit should come in native txn status API
        Assertions.assertThat(jsonPath.getString("body.childTransaction.bin")).isEqualToIgnoringCase(bin);
        Assertions.assertThat(jsonPath.getString("body.childTransaction.lastFourDigit")).isEqualToIgnoringCase(last4Digit);

    }

    @Test(description = "Verify new param should not come in native TxnStatus API and in Handler Status API for failure transactions",groups = "P1")
    @Parameters({"theme"})
    public void validateNewParamsInNativeTxnStatusFailureTxn(@Optional("enhancedweb") String theme)  {

        assertBinAndVPAPreference(binResponseMerchant);
        OrderDTO orderDTO = new OrderFactory.PGOnly(binResponseMerchant, theme)
                .setTXN_AMOUNT("99.98")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        //BIN and last 4 Digits should come in native txn status response
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode(Constants.ResponseCode.BANK_TXN_FAILURE.getRespCode())
                .validateRespMsg(Constants.ResponseCode.BANK_TXN_FAILURE.getRespMsg())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();//BIN and Last4Digit should not come

        //Native Status API
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), binResponseMerchant.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(orderDTO.getTXN_AMOUNT());
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        //bin and lastFourDigit should not come in native txn status API
        Assertions.assertThat(jsonPath.getString("body.bin")).isEqualToIgnoringCase(null);
        Assertions.assertThat(jsonPath.getString("body.lastFourDigit")).isEqualToIgnoringCase(null);

    }

    //MLV Flow :
       //GenerateQR
       //FetchQRPaymentDetails
       //Initiate Txn
       //PTC
    @Test(description = "Verify that additionalParam parameter come in native merchant status API if the parameter is coming in Internal Handler Status API for MLV Transactions",groups = "P0")
    @Parameters({"theme"})
    public void mlvAdditionalParamStatus(@Optional("false") Boolean isNativePlus) throws Exception {

        assertBinAndVPAPreference(MLV);
        String bin = paymentDTO.getCreditCardNumber().substring(0,6);
        String last4Digit = paymentDTO.getCreditCardNumber().substring(paymentDTO.getCreditCardNumber().length()-4);

        User user = userManager.getForWrite(Label.BASIC);
        //GenerateQR

        GenerateQR generateQR = new GenerateQR(MLV.getId(),"","UPI_QR_CODE");
        JsonPath generateResponse = generateQR.execute().jsonPath();
        Assertions.assertThat(generateResponse.getString("statusCode")).isEqualTo("200");
        String qrCodeId =  generateResponse.getString("response.qrCodeId").replaceAll("\\p{P}", "");

        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(MLV.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMLVSupported(true)
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchPayResponse = fetchQRPaymentDetails.execute().jsonPath();
        String txnAmount = "2.00";
        String orderId = fetchPayResponse.getString("body.paymentOptions.orderId");

        //Initiate Txn
        //Mandatory for MLV
        OrderPricingInfo orderPricingInfo = new OrderPricingInfo();
        orderPricingInfo.setOrderTotalAmount(new OrderTotalAmount());
        orderPricingInfo.setAmountInfoList(Arrays.asList(new AmountInfoList().setAmount(new Amount())));

        String aggMid ="cuu5PN33033033550100";
        String aggKey = "&DtM8iZCHLnC%xxb";//Aggregator Merchant Key
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),MLV)
                .setOrderId(orderId)
                .setAggType("ORDER_CREATOR")
                .setAggrMid(aggMid)
                .setTxnValue(txnAmount)
                .setOfflineFlow("true")
                .setOrderPricingInfo(orderPricingInfo)
                .setMerchantKey(aggKey)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //PTC
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:"+qrCodeId+"|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:"+qrCodeId+"|comment:|REQUEST_TYPE:UPI_QR_CODE|");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                MLV.getId(), txnToken,orderId)
                .setPaymentMode("CREDIT_CARD")
                .setAggType("ORDER_CREATOR")
                .setExtendInfo(extendInfo)
                .setAggMid(aggMid)
                .setTxnAmount(new TxnAmount().setValue(txnAmount))
                .setCardNum(paymentDTO.getCreditCardNumber())
                .build();
        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);

        TxnStatus txnStatus = new TxnStatus(MLV.getId(), orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderId)
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(MLV.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateCardBin(bin)    // BIN
                .validateLastFourDigits(last4Digit)//Last 4 Digit
                .validateAdditionalParam(Constants.ValidationType.NON_EMPTY) //Additional Param
                .AssertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderId, aggMid, aggKey, MLV.getId())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = merchant.execute();
        JsonPath jsonPath = response.jsonPath();

        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_SUCCESS");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(orderId);
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase(txnAmount);
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
        Assertions.assertThat(jsonPath.getString("body.mid")).isEqualToIgnoringCase(MLV.getId());

        //bin and lastFourDigit should come in native txn status API
        Assertions.assertThat(jsonPath.getString("body.bin")).isEqualToIgnoringCase(bin);
        Assertions.assertThat(jsonPath.getString("body.lastFourDigit")).isEqualToIgnoringCase(last4Digit);
        Assertions.assertThat(jsonPath.getString("body.additionalParam")).isNotEmpty();
    }



    @Test(description = "Validate DirectForms are returned for MLV when ff4J flag is ON preference nativeOTPSupported not active")
    public void validateGenerateDirectFormParamsWhenFF4JisON_UsingMLV() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        Constants.MerchantType merchant = Constants.MerchantType.STATICQR_DIRECT_HDFO;
        GenerateQR generateQR = new GenerateQR(merchant.getId(),"","QR_MERCHANT");
        JsonPath generateResponse = generateQR.execute().jsonPath();

        Assertions.assertThat(generateResponse.getString("statusCode")).isEqualTo("200");
        String qrCodeId =  generateResponse.getString("response.qrCodeId").replaceAll("\\p{P}", "");

        System.out.println(qrCodeId);

        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMLVSupported(true)
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchPayResponse = fetchQRPaymentDetails.execute().jsonPath();
        String txnAmount = "2.00";
        String orderId = fetchPayResponse.getString("body.paymentOptions.orderId");

        //Initiate Txn
        //Mandatory for MLV
        OrderPricingInfo orderPricingInfo = new OrderPricingInfo();
        orderPricingInfo.setOrderTotalAmount(new OrderTotalAmount());
        orderPricingInfo.setAmountInfoList(Arrays.asList(new AmountInfoList().setAmount(new Amount())));

        String aggMid ="cuu5PN33033033550100";
        String aggKey = "&DtM8iZCHLnC%xxb";//Aggregator Merchant Key
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setOrderId(orderId)
                .setAggType("ORDER_CREATOR")
                .setAggrMid(aggMid)
                .setTxnValue(txnAmount)
                .setOfflineFlow("true")
                .setOrderPricingInfo(orderPricingInfo)
                .setMerchantKey(aggKey)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //PTC
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:"+qrCodeId+"|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:"+qrCodeId+"|comment:|REQUEST_TYPE:UPI_QR_CODE|");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), txnToken,orderId)
                .setPaymentMode("CREDIT_CARD")
                .setAggType("ORDER_CREATOR")
                .setExtendInfo(extendInfo)
                .setAggMid(aggMid)
                .setTxnAmount(new TxnAmount().setValue(txnAmount))
                .setCardNum(paymentDTO.getCreditCardNumber())
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");


        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();

        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("HDFC Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("HDFO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderId);
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(merchant.getId());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(initTxnDTO.txnAmountFromBody());

        validateSoftly.assertAll();




    }




    @Test(description = "Validate DirectForms are Not returned for MLV when ff4J flag is Off and preference Not active")
    public void validateDirectFormNotGeneratedWhenFF4JisOFF_UsingMLV() throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        Constants.MerchantType merchant = Constants.MerchantType.ICIO_CC_Enabled_Merchant_Retry;
        GenerateQR generateQR = new GenerateQR(merchant.getId(),"","QR_MERCHANT");
        JsonPath generateResponse = generateQR.execute().jsonPath();

        Assertions.assertThat(generateResponse.getString("statusCode")).isEqualTo("200");
        String qrCodeId =  generateResponse.getString("response.qrCodeId").replaceAll("\\p{P}", "");

        System.out.println(qrCodeId);

        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMLVSupported(true)
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchPayResponse = fetchQRPaymentDetails.execute().jsonPath();
        String txnAmount = "2.00";
        String orderId = fetchPayResponse.getString("body.paymentOptions.orderId");

        //Initiate Txn
        //Mandatory for MLV
        OrderPricingInfo orderPricingInfo = new OrderPricingInfo();
        orderPricingInfo.setOrderTotalAmount(new OrderTotalAmount());
        orderPricingInfo.setAmountInfoList(Arrays.asList(new AmountInfoList().setAmount(new Amount())));

        String aggMid ="cuu5PN33033033550100";
        String aggKey = "&DtM8iZCHLnC%xxb";//Aggregator Merchant Key
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setOrderId(orderId)
                .setAggType("ORDER_CREATOR")
                .setAggrMid(aggMid)
                .setTxnValue(txnAmount)
                .setOfflineFlow("true")
                .setOrderPricingInfo(orderPricingInfo)
                .setMerchantKey(aggKey)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //PTC
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:"+qrCodeId+"|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:"+qrCodeId+"|comment:|REQUEST_TYPE:UPI_QR_CODE|");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), txnToken,orderId)
                .setPaymentMode("CREDIT_CARD")
                .setAggType("ORDER_CREATOR")
                .setExtendInfo(extendInfo)
                .setAggMid(aggMid)
                .setTxnAmount(new TxnAmount().setValue(txnAmount))
                .setCardNum(paymentDTO.getCreditCardNumber())
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");

        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNull();


    }


    @Test(description = "Validate DirectForms are returned for MLV when ff4J flag is Off and preference is nativeOTPSupported")
    public void validateDirectFormGeneratedWhenFF4JisOFFPrefActive_UsingMLV() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.ICIO_CC_Enabled_Merchant;

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "nativeOTPSupported", "Y");

        }

        User user = userManager.getForRead(Label.BASIC);

        GenerateQR generateQR = new GenerateQR(merchant.getId(),"","QR_MERCHANT");
        JsonPath generateResponse = generateQR.execute().jsonPath();

        Assertions.assertThat(generateResponse.getString("statusCode")).isEqualTo("200");
        String qrCodeId =  generateResponse.getString("response.qrCodeId").replaceAll("\\p{P}", "");

        System.out.println(qrCodeId);

        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMLVSupported(true)
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchPayResponse = fetchQRPaymentDetails.execute().jsonPath();
        String txnAmount = "2.00";
        String orderId = fetchPayResponse.getString("body.paymentOptions.orderId");

        //Initiate Txn
        //Mandatory for MLV
        OrderPricingInfo orderPricingInfo = new OrderPricingInfo();
        orderPricingInfo.setOrderTotalAmount(new OrderTotalAmount());
        orderPricingInfo.setAmountInfoList(Arrays.asList(new AmountInfoList().setAmount(new Amount())));

        String aggMid ="cuu5PN33033033550100";
        String aggKey = "&DtM8iZCHLnC%xxb";//Aggregator Merchant Key
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setOrderId(orderId)
                .setAggType("ORDER_CREATOR")
                .setAggrMid(aggMid)
                .setTxnValue(txnAmount)
                .setOfflineFlow("true")
                .setOrderPricingInfo(orderPricingInfo)
                .setMerchantKey(aggKey)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //PTC
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:"+qrCodeId+"|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:"+qrCodeId+"|comment:|REQUEST_TYPE:UPI_QR_CODE|");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), txnToken,orderId)
                .setPaymentMode("CREDIT_CARD")
                .setAggType("ORDER_CREATOR")
                .setExtendInfo(extendInfo)
                .setAggMid(aggMid)
                .setTxnAmount(new TxnAmount().setValue(txnAmount))
                .setCardNum(paymentDTO.ICICI_CC_CARD)
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");


        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();


        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("ICICI Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("ICIO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderId);
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(merchant.getId());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(initTxnDTO.txnAmountFromBody());
        validateSoftly.assertAll();

    }


    @Test(description = "Validate DirectForms are returned for MLV when ff4J flag is Off and preference is nativeOTPSupported")
    public void validateDirectFormGeneratedWhenFF4JisONPrefActive_UsingMLV() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.STATICQR_DIRECT_ICIO;

        pre_requisite:
        {
            PGPHelpers.validate_MerchantPreference(merchant.getId(), "nativeOTPSupported", "Y");

        }

        User user = userManager.getForRead(Label.BASIC);

        GenerateQR generateQR = new GenerateQR(merchant.getId(),"","QR_MERCHANT");
        JsonPath generateResponse = generateQR.execute().jsonPath();

        Assertions.assertThat(generateResponse.getString("statusCode")).isEqualTo("200");
        String qrCodeId =  generateResponse.getString("response.qrCodeId").replaceAll("\\p{P}", "");

        System.out.println(qrCodeId);

        //FetchQRPaymentDetails
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeId)
                .setMID(merchant.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMLVSupported(true)
                .build();

        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchPayResponse = fetchQRPaymentDetails.execute().jsonPath();
        String txnAmount = "2.00";
        String orderId = fetchPayResponse.getString("body.paymentOptions.orderId");

        //Initiate Txn
        //Mandatory for MLV
        OrderPricingInfo orderPricingInfo = new OrderPricingInfo();
        orderPricingInfo.setOrderTotalAmount(new OrderTotalAmount());
        orderPricingInfo.setAmountInfoList(Arrays.asList(new AmountInfoList().setAmount(new Amount())));

        String aggMid ="cuu5PN33033033550100";
        String aggKey = "&DtM8iZCHLnC%xxb";//Aggregator Merchant Key
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setOrderId(orderId)
                .setAggType("ORDER_CREATOR")
                .setAggrMid(aggMid)
                .setTxnValue(txnAmount)
                .setOfflineFlow("true")
                .setOrderPricingInfo(orderPricingInfo)
                .setMerchantKey(aggKey)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //PTC
        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setAdditionalInfo("payeeType:MERCHANT|currencyCode:INR|category:Gas and Petrol|subCategory:BPCL Pump|service:P2M|mode:QR_CODE|offlinePostConvenience:false|mappingId:GultiF44414842981731|pgEnabled:true|qrCodeId:"+qrCodeId+"|EXPIRY_DATE:|NAME:Gulti Filling Station|MERCHANT_NAME:Gulti Filling Station|MOBILE_NO:9373106192|TXN_AMOUNT|INDUSTRY_TYPE_ID:Retail|MERCHANT_GUID:5b28bcda-1a3c-4cac-91ff-9d82ddbc6558|MERCHANT_STATUS:ACTIVE|qr_code_id:"+qrCodeId+"|comment:|REQUEST_TYPE:UPI_QR_CODE|");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), txnToken,orderId)
                .setPaymentMode("CREDIT_CARD")
                .setAggType("ORDER_CREATOR")
                .setExtendInfo(extendInfo)
                .setAggMid(aggMid)
                .setTxnAmount(new TxnAmount().setValue(txnAmount))
                .setCardNum(paymentDTO.ICICI_CC_CARD)
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();

        List<HashMap<String, Object>> DirectForm = response.jsonPath().get("body.bankForm.directForms");


        Assertions.assertThat(DirectForm).as("Direct Forms are not getting  fetched in PTC").isNotNull();

        Response DirectBankResp =  ExecuteDirectFormRequest(DirectForm);

        SoftAssertions validateSoftly = new SoftAssertions();


        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.BANKNAME")).isEqualTo("ICICI Bank");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.GATEWAYNAME")).isEqualTo("ICIO");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.ORDERID")).isEqualTo(orderId);
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.MID")).isEqualTo(merchant.getId());
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.PAYMENTMODE")).isEqualTo("CC");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.RESPMSG")).isEqualTo("Txn Success");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.STATUS")).isEqualTo("TXN_SUCCESS");
        validateSoftly.assertThat(DirectBankResp.jsonPath().getString("body.txnInfo.TXNAMOUNT")).isEqualTo(initTxnDTO.txnAmountFromBody());
        validateSoftly.assertAll();

    }


}
