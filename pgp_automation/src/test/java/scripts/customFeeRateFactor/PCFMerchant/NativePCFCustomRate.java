package scripts.customFeeRateFactor.PCFMerchant;

import com.paytm.api.GetPaymentStatus;
import com.paytm.api.OrderStatus;
import com.paytm.api.TxnStatus;
import com.paytm.api.TxnStatusList;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.text.DecimalFormat;
import java.util.Date;

@Feature("PGP-21710")
@Owner("Gagandeep")
public class NativePCFCustomRate extends PGPBaseTest {

    private static String successMessage = "Txn Success";
    private static String successStatus = "TXN_SUCCESS";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType customRatePCF = Constants.MerchantType.PCF_CUSTOM_RATE;
    private double txnAmount = 2.00;

    public static TxnStatus validateSuccessTxnStatusPCFTxn(OrderDTO orderDTO, String payMode) {
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
                .validateTxnDate(new Date())
                .AssertAll();
        orderDTO.setTxnId(txnStatus.getResponse().getTXNID());
        return txnStatus;

    }

    public static TxnStatus validateSuccessNativeTxnStatusPCFTxn(OrderDTO orderDTO, String payMode) {
        TxnStatus txnStatus = new TxnStatus();
        txnStatus.getNativeStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
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
                .validateResponseCode("01")
                .validateResponseMessage("Txn Successful.")
                .validateMID(orderDTO.getMID())
                .validateRefundId("")
                .assertAll();


    }

    public static JsonPath validateSuccessPaymentStatusAPIPCFTxn(OrderDTO orderDTO, Constants.MerchantType merchantType, String payMode) {
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

        return getPaymentStatus;
    }

    public static void validateOrderStatusAPI(OrderDTO orderDTO, String payMode) {
        OrderStatus orderStatus = new OrderStatus();
        Response response = orderStatus.getOrderStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        Assertions.assertThat(response.jsonPath().getString("body.txnId")).isNotEmpty().isNotNull();
        Assertions.assertThat(response.jsonPath().getString("body.orderId")).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(response.jsonPath().getString("body.txnStatus")).isEqualTo(successStatus);
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseMsg")).isEqualTo(successMessage);
        Assertions.assertThat(response.jsonPath().getString("body.paymentMode")).isEqualTo(payMode);
        Assertions.assertThat(Double.parseDouble(response.jsonPath().getString("body.txnAmount"))).isEqualTo(Double.parseDouble(orderDTO.getTXN_AMOUNT()));
        Assertions.assertThat(response.jsonPath().getString("body.mid")).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(response.jsonPath().getString("body.txnResponseCode")).isEqualTo("01");
    }

    public JsonPath isPcfEnabled(String txnToken, InitTxnDTO initTxnDTO) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.pcfEnabled")).as("PCF is disabled").isEqualTo(true);
        return fetchPaymentOptionsJson;
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify a successful DC txn MASTER card on PCF merchant applied with PCF commission")
    public void successfulDCMasterTxnForPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {

        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);


        String payMode = "DC";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), customRatePCF)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken, initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO, txnToken, paymentDTO.getDebitCardNumber().substring(0, 6), payMode);
        Double chargeAmountBin = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("DEBIT_CARD", "MASTER", txnToken, initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath, "DEBIT_CARD");

        OrderDTO orderDTO = new OrderFactory.Native(customRatePCF, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        DecimalFormat df = new DecimalFormat("0.00");
        Assertions.assertThat(df.format(chargeAmountBin)).as("Charge amount in /fetchBin and /fetchPCF response ").isEqualTo(df.format(chargeAmountPCF));
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, customRatePCF, payMode, chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIPCFTxn(orderDTO, customRatePCF, payMode);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmountPCF);
        validateOrderStatusAPI(orderDTO, payMode);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("DEBIT_CARD", "MASTER", "HDFC", "API");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify a successful NB txn on PCF merchant applied with PCF commission having based Commission 0 to 25 flat 10rs, 25 to 50 both 12 " +
            "and above MDR commission with 12 Rs flat")
    public void successfulNBTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");


        String payMode = "NB";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), customRatePCF)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken, initTxnDTO);


        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING", "ICICI", txnToken, initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath, "NET_BANKING");

        OrderDTO orderDTO = new OrderFactory.Native(customRatePCF, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.NET_BANKING)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setChannelCode("ICICI")
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, customRatePCF, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIPCFTxn(orderDTO, customRatePCF, payMode);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        validateOrderStatusAPI(orderDTO, payMode);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify a successful NB txn on PCF merchant applied with PCF commission having slab based commision <100 flat 12 both")
    public void successfulNBTxnForPCFNativetxnAmountMoreThan100(@Optional("false") Boolean isNativePlus) throws Exception {

        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");


        String payMode = "NB";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), customRatePCF)
                .setTxnValue("120")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken, initTxnDTO);


        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING", "ICICI", txnToken, initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath, "NET_BANKING");

        OrderDTO orderDTO = new OrderFactory.Native(customRatePCF, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.NET_BANKING)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setChannelCode("ICICI")
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, customRatePCF, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIPCFTxn(orderDTO, customRatePCF, payMode);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        validateOrderStatusAPI(orderDTO, payMode);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Native : Verify a successful PPBL txn on PCF merchant applied with PCF commission having slab have commssion 15 both")
    public void successfulPPBLTxnForPCFNativetxnAmount(@Optional("false") Boolean isNativePlus) throws Exception {

        PaymentDTO paymentDTO = new PaymentDTO();

        String payMode = "NB";
        User user = userManager.getForWrite(Label.PPBL);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), customRatePCF)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken, initTxnDTO);


        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NB", "PPBL", txnToken, initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath, "PPBL");

        OrderDTO orderDTO= new OrderFactory.Native(customRatePCF, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.PPBL)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setChannelCode("PPBL")
                .setMpin(paymentDTO.getPasscode())
                .build();


        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO, customRatePCF, payMode, chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = validateSuccessTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessNativeTxnStatusPCFTxn(orderDTO, payMode);
        validateSuccessPaymentStatusAPIPCFTxn(orderDTO, customRatePCF, payMode);
        validateSuccessTxnStatusListPCFTxn(orderDTO, payMode, chargeAmount);
        validateOrderStatusAPI(orderDTO, payMode);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("FEE Factor is Not coming from PGPLUS BO")
                .containsExactlyInAnyOrder("NET_BANKING", "PPBL", "PPBL", "API");

    }
}