package scripts.Native;

import com.paytm.api.GetPaymentStatus;
import com.paytm.api.OrderStatus;
import com.paytm.api.PgPlusBO.SearchTransaction;
import com.paytm.api.TxnStatus;
import com.paytm.api.TxnStatusList;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPcfDetail;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.NativeDTO.fetchPcfDetail.*;
import com.paytm.dto.OrderDTO;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.api.alipay.BizOrderSearch;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.hamcrest.Matchers;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.asserts.SoftAssert;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.with;

/**
 * Created by anjukumari on 16/05/19
 */
@Owner("Tarun")
public class PcfHelpher extends PGPBaseTest {
    private final static Format format = new DecimalFormat("0.00");
    private static String successMessage = "Txn Success";
    private static String successStatus = "TXN_SUCCESS";

    public void validatePcf(JsonPath FJsonPath, String paymode, String pcfFlat, String txnamount, String displayText) {
        Double convFee = convenienceFeeCalculator(Double.parseDouble(txnamount), 0.0, Double.parseDouble(pcfFlat), "");
        CommonHelpers.assertCheck(FJsonPath, new Object[]{
                "body.consultDetails." + paymode + ".feeAmount.value", pcfFlat,
                "body.consultDetails." + paymode + ".taxAmount.value", format.format((Double.parseDouble(pcfFlat) * 0.18)),
                "body.consultDetails." + paymode + ".totalConvenienceCharges.value", format.format(convFee),
                "body.consultDetails." + paymode + ".totalTransactionAmount.value", format.format(convFee + Double.parseDouble(txnamount)),
                "body.consultDetails." + paymode + ".text", "Convenience fee of Rs. " + format.format(Double.parseDouble(pcfFlat) * (1 + 0.18)) + " is applicable.",
                "body.consultDetails." + paymode + ".displayText", displayText
        });
    }

    public void validate_Fee_inPcfDetail(Constants.MerchantType merchantType, String pcf, String payMethod, String displayText, String instId) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String trxToken = iniJsonPath.getString("body.txnToken");
        //Creating fetchPcf request
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId(instId).setPayMethod(payMethod));
        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new Head().setTxnToken(trxToken))
                .setBody(new Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath FJsonPath = fetchPcfDetail.execute().jsonPath();
        validatePcf(FJsonPath, payMethod, pcf, initTxnDTO.txnAmountFromBody(), displayText);
    }


    protected String Validate_InitTxn(InitTxnDTO initTxnDTO) {
        String txnToken;
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        if (StringUtils.contains(response.jsonPath().getString("body.resultInfo.resultCode"), "1001")) {
            String resultCode = response.jsonPath().get("body.resultInfo.resultCode").toString();
            return resultCode;
        } else {
            txnToken = response.jsonPath().getString("body.txnToken");
            Assertions.assertThat(txnToken).withFailMessage("Txn token is %s", txnToken).isNotNull();
        }
        return txnToken;
    }

    protected JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
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

    protected Response validate_BinDetail(String txnToken, InitTxnDTO initTxnDTO, OrderDTO orderDTO, String binNum) {
        Reporter.report.info("Validating binDetails API  with txn token" + orderDTO.getBANK_CODE());
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNum).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        Response response = fetchBinDetail.execute();
        JsonPath fetchBinsJson = response.jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
        return response;
    }

    protected void validate_BinDetail_pcf(JsonPath fetchBinsJson, String txnAmount, Double flatCommission, String cvvLength, String channelName, String cnMin) {
        Format format = new DecimalFormat("#.##");
        SoftAssert softAssert = new SoftAssert();
        Double totalAmount = Double.parseDouble(txnAmount) + convenienceFeeCalculator(Double.parseDouble(txnAmount), 0.0, flatCommission, "");
        softAssert.assertEquals(fetchBinsJson.getString("body.resultInfo.resultCode"), "0000");
        softAssert.assertEquals(fetchBinsJson.getString("body.resultInfo.resultMsg"), "Success");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.channelName"), channelName);
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cvvL"), cvvLength);
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cvvR"), "true");
        softAssert.assertEquals(fetchBinsJson.getString("body.binDetail.cnMin"), cnMin);
        softAssert.assertEquals(fetchBinsJson.getDouble("body.pcf.feeAmount.value"), flatCommission);
        Double expectedTaxAmount = fetchBinsJson.getDouble("body.pcf.taxAmount.value");
        softAssert.assertEquals(format.format(expectedTaxAmount), format.format(flatCommission * 0.18));
        softAssert.assertEquals(fetchBinsJson.getString("body.pcf.totalTransactionAmount.value"), format.format(totalAmount));
        softAssert.assertAll();
    }

    public void validateCommision(CashierPage cashierPage, OrderDTO orderDTO, Double percentCommsion, Double flatCommission) {
        cashierPage.pause(1);
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.parseDouble(orderDTO.getTXN_AMOUNT()), percentCommsion, flatCommission, "");
        String actual = cashierPage.chargeFeeAmtPG().getText();
        Reporter.report.info("Fee value on cashier page is:" + actual);
        Assertions.assertThat(Double.parseDouble(actual)).isEqualTo(expectedChargeFeeAmt);
    }

    public void validateCommisionAmount(CashierPage cashierPage, OrderDTO orderDTO, Double percentCommsion, Double flatCommission) {
        cashierPage.pause(1);
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.parseDouble(orderDTO.getTXN_AMOUNT()), percentCommsion, flatCommission, "");
        String actual1 = cashierPage.chargeFeeAmtPGNew().getText();
        String actual=actual1.replaceAll("Rs","");
        Reporter.report.info("Fee value on cashier page is:" + actual);
        Assertions.assertThat(Double.parseDouble(actual)).isEqualTo(expectedChargeFeeAmt);
    }
    public void validatePCFTxn(TxnStatus order, Double percentCommsion, Double flatCommission) throws InterruptedException {
        Awaitility.await().atMost(1, TimeUnit.MINUTES).until(() -> new BizOrderSearch(order.txnStatusResponse.getTXNID(), "BIZ_ORDER_ID").execute().jsonPath().getString("response.body.orders[0].extendInfo"), Matchers.containsString("FEE_AND_TAX_CHARGE_CHANNEL_INFO"));
        Response search = new BizOrderSearch(order.txnStatusResponse.getTXNID(), "BIZ_ORDER_ID").execute();
        String result = search.jsonPath().getString("response.body.orders.extendInfo[0]");
        String amount = new JSONObject(new JSONArray(new JSONObject(result).get("FEE_AND_TAX_CHARGE_CHANNEL_INFO").toString()).get(0).toString()).get("amount").toString();
        String commissionInResult = new JsonPath(amount).get("amount").toString();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.parseDouble(order.txnStatusResponse.getTXNAMOUNT()), percentCommsion, flatCommission, "");
        Assertions.assertThat(commissionInResult).withFailMessage("Expected Commission: "+expectedChargeFeeAmt+"\nActual Commission: "+commissionInResult).isEqualTo(String.valueOf(expectedChargeFeeAmt));
    }

    public static void assertDynamicChargeTarget(Constants.MerchantType merchantType,String prefValue)
    {
        String preference = "DYNAMIC_CHARGE_TARGET";
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference, prefValue);
    }


    public static void assertConvFee(Constants.MerchantType merchantType,String prefValue)
    {
        String preference = "WithoutConvenienceFee";
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference,prefValue );
    }


    private static void pause(int seconds)
    {
        try {
            Thread.sleep((seconds * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void validateCashierPageAmount(CashierPage cashierPage,Double amount)
    {
        pause(2);
        Assertions.assertThat(Double.parseDouble(cashierPage.totalAmtPG().getText())).as("Cashier Page text doesn't match the required amount").isEqualTo(amount);
    }

    //Fetch PCF Details

    public static JsonPath fetchPCFDetailsNative(String payMode, String instId, String txnToken, InitTxnDTO initTxnDTO)
    {
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId(instId).setPayMethod(payMode));

        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new Head().setTxnToken(txnToken))
                .setBody(new Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
        return jsonPath;
    }

    public static JsonPath fetchPCFDetailsWithSSO(OrderDTO orderDTO, String instId , User user, String payMode)
    {
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId(instId).setPayMethod(payMode));

        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new Body().setPayMethods(payMethods).setMid(orderDTO.getMID()).setTxnAmount(orderDTO.getTXN_AMOUNT()));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(orderDTO.getMID(),fetchPcfRequest);
        JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
        return jsonPath;
    }

    public static JsonPath fetchPCFDetailsWithSSO(OrderDTO orderDTO, String instId , User user, String payMode, feeRateFactors feeRateFactors)
    {
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId(instId).setPayMethod(payMode).setFeeRateFactors(feeRateFactors));

        FetchPcfRequestWithSSO fetchPcfRequest = new FetchPcfRequestWithSSO()
                .setHead(new com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.Head("WEB").setVersion("v1").setRequestTimestamp("Time").setTokenType("SSO").setToken(user.ssoToken()))
                .setBody(new Body()
                        .setPayMethods(payMethods)
                        .setMid(orderDTO.getMID())
                        .setTxnAmount(orderDTO.getTXN_AMOUNT()));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(orderDTO.getMID(),fetchPcfRequest);
        JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
        return jsonPath;
    }

    public static Double fetchTotalConvenienceChargesPCF(JsonPath jsonPath , String payMode)
    {
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Double chargeAmount =jsonPath.getDouble("body.consultDetails."+payMode+".totalConvenienceCharges.value");
        return null!=chargeAmount?chargeAmount:null;
    }

    public static Double fetchTotalTxnAmountPCF(JsonPath jsonPath , String payMode)
    {
        Double totalTxnAmount = jsonPath.getDouble("body.consultDetails."+payMode+".totalTransactionAmount.value");
        return null!=totalTxnAmount?totalTxnAmount:null;
    }

    //BIN Details

    public static JsonPath fetchBinDetails(InitTxnDTO initTxnDTO, String txnToken , String bin, String payMode) {
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, bin)
                .setPaymentMode(payMode).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        return fetchBinsJson;
    }

    public static JsonPath fetchBinDetails(InitTxnDTO initTxnDTO, String txnToken , String bin, String payMode,String emiType) {
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, bin,emiType)
                .setPaymentMode(payMode).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        return fetchBinsJson;
    }

    public static Double fetchTotalConvenienceChargesBIN(JsonPath jsonPath)
    {
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Double chargeAmount =jsonPath.getDouble("body.pcf.feeAmount.value") + jsonPath.getDouble("body.pcf.taxAmount.value");
        return null!=chargeAmount?chargeAmount:null;
    }

    public static String fetchTotalTxnAmountBin(JsonPath jsonPath)
    {
        String totalTxnAmount = jsonPath.getString("body.pcf.totalTransactionAmount.value");
        return null!=totalTxnAmount?totalTxnAmount:null;
    }


    ////////   Validation wrappers    ///////////

    public static void validateSuccessResponsePCFTxn(OrderDTO orderDTO, Constants.MerchantType merchantType,String payMode,Double chargeAmount)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateStatus(successStatus)
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateChargeAmount(format.format(chargeAmount))
                .validateCheckSum(merchantType.getKey()).assertAll();
    }

    public static void validateFailureResponsePCFTxn(OrderDTO orderDTO, Constants.MerchantType merchantType,String payMode,Double chargeAmount)
    {
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode(payMode)
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(com.paytm.appconstants.Constants.ValidationType.NON_EMPTY)
                .validateChargeAmount(format.format(chargeAmount))
                .validateCheckSum(merchantType.getKey()).assertAll();
    }

    public static TxnStatus validateSuccessTxnStatusPCFTxn(OrderDTO orderDTO,String payMode,Double chargeAmount)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateChargeAmount(format.format(chargeAmount))
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static TxnStatus validateFailureTxnStatusPCFTxn(OrderDTO orderDTO,String payMode,Double chargeAmount)
    {
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateChargeAmount(format.format(chargeAmount))
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static TxnStatus validateSuccessNativeTxnStatusPCFTxn(OrderDTO orderDTO,String payMode,Double chargeAmount)
    {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(successStatus)
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg(successMessage)
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateChargeAmount(format.format(chargeAmount))
                .validateTxnDate(new Date())
                .AssertAll();
        return txnStatus;

    }

    public static TxnStatus validateFailureNativeTxnStatusPCFTxn(OrderDTO orderDTO,String payMode,Double chargeAmount)
    {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundAmnt("0.00")
                .validateChargeAmount(format.format(chargeAmount))
                .validateTxnDate(new Date())
                .AssertAll();

        return txnStatus;

    }

    public static void validateSuccessTxnStatusListPCFTxn(OrderDTO orderDTO,String payMode,Double chargeAmount)
    {
        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        txnStatusList
                .validateTxnId(orderDTO.getTxnId())
                .validateBankTxnIdNonEmpty()
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(successStatus)
                .validateTXNTYPE("SALE")
                .validateGatewayName("")
                .validateResponseCode("01")
                .validateResponseMessage("Txn Successful.")
                .validateBANKNAME("")
                .validateMID(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundId("")
                .validateChildTxnId(orderDTO.getTxnId(),0)
                .validateChildTxnId(orderDTO.getTxnId(),1)
                .validateChildPayMode(payMode,0)
                .validateChildPayMode(payMode,1)
                .validateChildTxnTXNAMOUNT(orderDTO.getTXN_AMOUNT(),chargeAmount.toString())
                .validateChildTxnSTATUS(successStatus,0)
                .validateChildTxnSTATUS(successStatus,1)
                .assertAll();


    }

    public static void validateSuccessTxnStatusListMDRTxn(OrderDTO orderDTO,String payMode)
    {
        TxnStatusList txnStatusList = new TxnStatusList();
        txnStatusList.getTxnStatusList(orderDTO.getMID(),orderDTO.getORDER_ID());
        System.out.println("paymode is------"+payMode);
        txnStatusList
                .validateTxnId(orderDTO.getTxnId())
                .validateBankTxnIdNonEmpty()
                .validateOrderId(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus(successStatus)
                .validateTXNTYPE("SALE")
                .validateResponseCode("01")
                .validateResponseMessage("Txn Successful.")
                .validateMID(orderDTO.getMID())
                .validatePaymentMode(payMode)
                .validateRefundId("")
                .assertAll();


    }

    public static JsonPath validateSuccessPaymentStatusAPIPCFTxn(OrderDTO orderDTO, Constants.MerchantType merchantType,String payMode,Double chargeAmount)
    {
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase(successStatus);
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase(successMessage);
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());
        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase(payMode);

        Assertions.assertThat(getPaymentStatus.getDouble("body.chargeAmount")).isEqualTo(chargeAmount);

        return  getPaymentStatus;
    }

    public static JsonPath validateFailurePaymentStatusAPIPCFTxn(OrderDTO orderDTO, Constants.MerchantType merchantType,String payMode,Double chargeAmount)
    {
        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (orderDTO.getORDER_ID(), orderDTO.getAggrMid(), merchantType.getKey(), orderDTO.getMID())
                .build();
        GetPaymentStatus merchant = new GetPaymentStatus(getPaymentStatusDTO);
        JsonPath getPaymentStatus = merchant.execute().jsonPath();
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(getPaymentStatus.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        Assertions.assertThat(getPaymentStatus.getString("body.orderId")).isEqualToIgnoringCase(orderDTO.getORDER_ID());
        Assertions.assertThat(getPaymentStatus.getString("body.mid")).isEqualToIgnoringCase(orderDTO.getMID());

        Assertions.assertThat(getPaymentStatus.getString("body.paymentMode")).isEqualToIgnoringCase(payMode);
        Assertions.assertThat(getPaymentStatus.getDouble("body.chargeAmount")).isEqualTo(chargeAmount);


        return  getPaymentStatus;
    }


    public static void validateSuccessPeonPCFTxn(OrderDTO orderDTO,String payMode)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals(successMessage),
                peon.status().equals(successStatus),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    public static void validateSuccessPeonPCFtxn(OrderDTO orderDTO,String payMode)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "feeRateFactors", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "prepaidCard", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.feeRateFactors().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals("vivek4"),
                peon.mId().equals(orderDTO.getMID()),
                peon.prepaidCard().equals("true"),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals(successMessage),
                peon.status().equals(successStatus),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    public static void validateSuccessPeonPrepaidPCFtxn(OrderDTO orderDTO,String payMode)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "feeRateFactors", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "prepaidCard", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankTxnId().equals("").not(),
                peon.feeRateFactors().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.mercUnqRef().equals("vivek4"),
                peon.mId().equals(orderDTO.getMID()),
                peon.prepaidCard().equals("true"),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(payMode),
                peon.respCode().equals("01"),
                peon.respMsg().equals(successMessage),
                peon.status().equals(successStatus),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    public static void validateFailurePeonPWPTxn(OrderDTO orderDTO)
    {
        Peons peons = new Peons();
        Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.bankName().equals("PAYTM"),
                peon.bankTxnId().equals("").not(),
                peon.currency().equals("INR"),
                peon.custId().equals("").not(),
                peon.gatewayName().equals(Constants.Gateway.PAYTM.toString()),
                peon.mercUnqRef().equals(""),
                peon.mId().equals(orderDTO.getMID()),
                peon.orderId().equals(orderDTO.getORDER_ID()),
                peon.payMode().equals(Constants.Gateway.PAYTM.toString()),
                peon.respCode().equals("501"),
                peon.respMsg().equals("System Error"),
                peon.status().equals("TXN_FAILURE"),
                peon.txnAmt().equals(orderDTO.getTXN_AMOUNT()),
                peon.txnDate().equals("").not(),
                peon.txnDateTime().equals("").not(),
                peon.txnId().equals("").not(),
                peon.isChecksumValid()
        );
        sAssert.eval();
    }

    public static void validateOrderStatusAPI(OrderDTO orderDTO,String payMode,Double chargeAmount)
    {
        OrderStatus orderStatus = new OrderStatus();
        Response response =  orderStatus.getOrderStatus(orderDTO.getMID(),orderDTO.getORDER_ID());
        Assertions.assertThat(response.jsonPath().getString("body.txnId")).isNotEmpty().isNotNull();
        Assertions.assertThat(response.jsonPath().getString("body.orderId")).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(response.jsonPath().getString("body.txnStatus")).isEqualTo(successStatus);
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseMsg")).isEqualTo(successMessage);
        Assertions.assertThat(response.jsonPath().getString("body.paymentMode")).isEqualTo(payMode);
        Assertions.assertThat(Double.parseDouble(response.jsonPath().getString("body.txnAmount"))).isEqualTo(Double.parseDouble(orderDTO.getTXN_AMOUNT()));
        Assertions.assertThat(response.jsonPath().getString("body.mid")).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(response.jsonPath().getDouble("body.chargeAmount")).isEqualTo(chargeAmount);
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseCode")).isEqualTo("01");




    }

    public static Response searchTxnPgPlusBO(TxnStatus txnStatus) {
        SearchTransaction searchTransaction = new SearchTransaction(txnStatus.txnStatusResponse.getTXNID());
        // TODO: Added Temporary fix need to handle wait properly in future
//        try{Thread.sleep(40000);}
//        catch (Exception e){
//        }
        with().pollInSameThread().await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() -> Assertions.assertThat(searchTransaction.execute().jsonPath().getString("result.feeFactor[0]")).isNotNull());
        Response response = searchTransaction.execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");
        Assertions.assertThat(response.jsonPath().getString("result.txnId[0]")).isEqualTo(txnStatus.txnStatusResponse.getTXNID());
        return response;
    }


    public static JsonPath fetchPCFDetailsWithFeeRateFactors(String payMode, String instId,feeRateFactors feeRateFactors, String txnToken, InitTxnDTO initTxnDTO)
    {
        List<PayMethod> payMethods = new ArrayList<>();
        payMethods.add(new PayMethod().setInstId(instId).setPayMethod(payMode).setFeeRateFactors(feeRateFactors));

        FetchPcfRequest fetchPcfRequest = new FetchPcfRequest()
                .setHead(new Head().setTxnToken(txnToken))
                .setBody(new Body().setPayMethods(payMethods));
        FetchPcfDetail fetchPcfDetail = new FetchPcfDetail(fetchPcfRequest, initTxnDTO);
        JsonPath jsonPath = fetchPcfDetail.execute().jsonPath();
        return jsonPath;
    }

}
