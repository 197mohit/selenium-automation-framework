package scripts.Native;

import com.paytm.api.CreateUpiLinkApi;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.createUPILink.CreateUpiLinkRequest;
import com.paytm.dto.createUPILink.CreateUpiLinkResponse;
import com.paytm.dto.saveCard.SaveCardResponseBase;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Feature("PGP-26467")
@Owner("Tarun")

//Only applicable for Non Logged In Flow and Request Type : NATIVE_ST
public class NativeStock extends PGPBaseTest {

   private final Constants.MerchantType stockMerchant = Constants.MerchantType.STOCK_TRADE;
   private final  CheckoutPage checkoutPage = new CheckoutPage();

    public JsonPath Validate_FetchPayInstrument(String txnToken, InitTxnDTO initTxnDTO, String payMethod, String status) {
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


    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful payment for NATIVE_ST flow on Native and Native + for Net Banking paymode with checksum validation.")
    public void tc1NetBanking(@Optional("false") Boolean isNativePlus)
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
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
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful payment for NATIVE_ST flow on Native and Native + for DC paymode with checksum validation.")
    public void tc2DebitCard(@Optional("false") Boolean isNativePlus)
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
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
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful payment for NATIVE_ST flow on Native and Native + for UPI paymode with checksum validation.")
    public void tc3UPI(@Optional("true") Boolean isNativePlus)
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm").build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
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
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful payment for NATIVE_ST flow on Native and Native + for Saved DC paymode with checksum validation.")
    public void tc4savedDC(@Optional("false") Boolean isNativePlus) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers savedCardHelpers = new SavedCardHelpers();
        String expiry = paymentDTO.getExpMonth() + paymentDTO.getExpYear();

        //Saving card on MID CustId
        SaveCardResponseBase responseBase = savedCardHelpers.saveCard_custId_mId(paymentDTO.getDebitCardNumber(), initTxnDTO.getBody().getUserInfo().getCustId(), stockMerchant.getId(), expiry);

        paymentDTO.setSavedCardId(responseBase.getResponse().toString());
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        Validate_FetchPayInstrument(txnToken,initTxnDTO,"DEBIT_CARD","false");

        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
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
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify failure payment for NATIVE_ST flow on Native and Native + for NB paymode with checksum validation.")
    public void tc5NBFailure(@Optional("false") Boolean isNativePlus)  {

        Double txnAmount = 99.99;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify failure payment for NATIVE_ST flow on Native and Native + for DC paymode with checksum validation.")
    public void tc6DCFailure(@Optional("false") Boolean isNativePlus)  {

        Double txnAmount = 99.98;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Verify failure payment for NATIVE_ST flow on Native and Native + for UPI paymode with checksum validation.")
    public void tc7UPICFailure(@Optional("false") Boolean isNativePlus)  {

        Double txnAmount = 99.85;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI).setPayerAccount("test@paytm").build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("296")
                .validateRespMsg("Failed to retrieve Bank Form")
                .validateStatus("TXN_FAILURE")
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("296")
                .validateRespMsg("Failed to retrieve Bank Form")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify pending payment for NATIVE_ST flow on Native and Native + for NetBanking paymode with checksum validation.")
    public void tc8NBPending(@Optional("false") Boolean isNativePlus)  {

        Double txnAmount = 99.51;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();


    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify pending payment for NATIVE_ST flow on Native and Native + for DC paymode with checksum validation.")
    public void tc9DCPending(@Optional("false") Boolean isNativePlus)  {

        Double txnAmount = 99.84;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .setTxnValue(txnAmount.toString())
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(stockMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("402")
                .validateRespMsg("Looks like the payment is not complete. Please wait while we confirm the status with your bank.")
                .validateStatus("PENDING")
                .validateCheckSum(stockMerchant.getKey())
                .assertAll();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify successful payment for NATIVE_ST flow on Native and Native + for UPI Intent paymode with checksum validation.")
    public void tc10UPIIntent()
    {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, stockMerchant)
                .setRequestType("NATIVE_ST")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptResponseDTO fetchPaymentOptResponse = NativeHelpers.fetchPaymentOptionResponse(txnToken, stockMerchant.getId(), initTxnDTO.orderFromBody());
        Assertions
                .assertThat(NativeHelpers.isFetchPaymentOptionStatusMatched(fetchPaymentOptResponse, PayMethodType.UPI.toString(),false))
                .as(PayMethodType.UPI.toString() + " status mismatched")
                .isTrue();

        CreateUpiLinkRequest createUpiLinkRequest = new CreateUpiLinkRequest(stockMerchant.getId(), stockMerchant.getKey(), txnToken,
                "", "", initTxnDTO.getBody().getOrderId());
        CreateUpiLinkResponse createUpiLinkResponse = new CreateUpiLinkApi(createUpiLinkRequest)
                .execute()
                .as(CreateUpiLinkResponse.class);

        PGPHelpers.generateUpiIntentPayRequest(createUpiLinkResponse, initTxnDTO.txnAmountFromBody(), stockMerchant.getId(), Constants.Intent_Callback.SUCCESS);
        PGPHelpers.getTxnStatus(stockMerchant.getId(), initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName("PPBL")
                .validatePaymentMode("UPI")
                .AssertAll();

    }



}
