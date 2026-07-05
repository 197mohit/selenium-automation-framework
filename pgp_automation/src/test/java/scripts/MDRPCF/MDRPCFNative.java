package scripts.MDRPCF;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchPcfDetail.feeRateFactors;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
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
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.MAYURI;

@Feature("PGP-21945")
@Owner("Tarun")
public class MDRPCFNative extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private double txnAmount = 2.00;

    //Boss Panel http://10.142.51.67 >> Manage Merchants >>Commission >> Merchant ID >> Add specific commission to payMode

    Constants.MerchantType mdrPCF = Constants.MerchantType.MDR_PCF;
    Constants.MerchantType mdrPCF_PG2_RTDD = Constants.MerchantType.MDR_PCF_PG2_RTDD;

    //FPO
    public JsonPath isPcfEnabled(String txnToken, InitTxnDTO initTxnDTO) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.pcfEnabled")).as("PCF is disabled").isEqualTo(true);
        return fetchPaymentOptionsJson;
    }
    

    @Test(description = "Native : Verify a successful CC txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulCCTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "CC";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,paymentDTO.getCreditCardNumber().substring(0,6),payMode);
        Double chargeAmountBin  = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("CREDIT_CARD","",txnToken,initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        DecimalFormat df = new DecimalFormat("0.00");
        Assertions.assertThat(df.format(chargeAmountBin)).as("Charge amount in /fetchBin and /fetchPCF response ").isEqualTo(df.format(chargeAmountPCF));
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful CC txn with MASTER on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulCCTxnWithMasterForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        Constants.MerchantType mdrPCF= Constants.MerchantType.SEAMLESS_MDR_PCF_PG2_RTDD;
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "CC";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,paymentDTO.getCreditCardNumber().substring(0,6),payMode);
        Double chargeAmountBin  = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("CREDIT_CARD","MASTER",txnToken,initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        Assertions.assertThat(chargeAmountBin).as("Charge amount in /fetchBin and /fetchPCF response mismatch ").isEqualTo(chargeAmountPCF);

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }



    @Test(description = "Native : Verify a successful DC txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulDCTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "DC";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,paymentDTO.getDebitCardNumber().substring(0,6),payMode);
        Double chargeAmountBin  = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("DEBIT_CARD","",txnToken,initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");

        Assertions.assertThat(chargeAmountBin).as("Charge amount in /fetchBin and /fetchPCF response mismatch ").isEqualTo(chargeAmountPCF);

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus =  PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful saved card txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulSavedCardTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "DC";
        User user = userManager.getForWrite(Label.BASIC);

        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,paymentDTO.getDebitCardNumber().substring(0,6),payMode);
        Double chargeAmountBin  = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("DEBIT_CARD","",txnToken,initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");

        Assertions.assertThat(chargeAmountBin).as("Charge amount in /fetchBin and /fetchPCF response mismatch ").isEqualTo(chargeAmountPCF);

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful NB txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulNBTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "NB";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING","",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.NET_BANKING)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful PPBL txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPPBLTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "NB";
        User user = userManager.getForWrite(Label.PPBL);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative(payMode,"PPBL",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"PPBL");

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.PPBL)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setMpin(paymentDTO.getPasscode())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =  PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "PPBL", "PPBL", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful UPI txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulUPITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "UPI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative(payMode,"",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"UPI");

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.UPI)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setPayerAccount(paymentDTO.getVpa())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("UPI", "UPI", "ICICI", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful PPI txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPPITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "PPI";
        User user = userManager.getForWrite(Label.BASIC);
        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("BALANCE","",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"BALANCE");
        Double totalTxnAmount = PcfHelpher.fetchTotalTxnAmountPCF(jsonPath,"BALANCE");
        WalletHelpers.modifyBalance(user,Double.valueOf(totalTxnAmount));

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.BALANCE)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful Paytm Postpaid txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmPostpaidITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(mdrPCF_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(mdrPCF_PG2_RTDD,"N");// MDR Flow
        String payMode = "Paytm Postpaid";
        User user = userManager.getForWrite(Label.POSTPAID);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mdrPCF_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("PAYTM_DIGITAL_CREDIT","",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"PAYTM_DIGITAL_CREDIT");

        OrderDTO orderDTO= new OrderFactory.Native(mdrPCF_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setPayerAccount(paymentDTO.getVpa())
                .setMpin(paymentDTO.getPasscode())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,mdrPCF_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);
        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("PAYTM_DIGITAL_CREDIT", "PAYTMCC", "PAYTMCC", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful EMI txn on MDR merchant applied with PCF commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulEMITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        //PcfHelpher.assertDynamicChargeTarget(mdrPCF,"Y");
        //PcfHelpher.assertConvFee(mdrPCF,"N");// MDR Flow
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Payment_Adapter_Config_MDRPCF_MID)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative(payMode,"",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,payMode);

      OrderDTO orderDTO= new OrderFactory.Native(Constants.MerchantType.Payment_Adapter_Config_MDRPCF_MID, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.EMI)
              .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
              .setPlanId("HDFC|3")
              .setChannelCode(paymentDTO.getBankName())
              .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,Constants.MerchantType.Payment_Adapter_Config_MDRPCF_MID,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,Constants.MerchantType.Payment_Adapter_Config_MDRPCF_MID,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);


        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("EMI", "HDFC", "HDFC", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch PCF details API response When Only EMI payMethod Passed")
    public void validateFetchPCFResponseWhenOnlyEMIpayMethodPassed(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithFeeRateFactors(payMode,"",feeRateFactors, txnToken,initTxnDTO);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.payMethod")).as("Pay Method is EMI").isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.feeAmount.value")).as("fee amount is 7.10rs").isEqualTo("7.10");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("{\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10}");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":null");
        Assertions.assertThat(logs).contains("{\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10}");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");

    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch PCF details API response When cardNetwork=MASTER Passed")
    public void validateFetchPCFResponseWhenCardNetworkPassed(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("");
        feeRateFactors.setCardNetwork("MASTER");
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithFeeRateFactors(payMode,"",feeRateFactors, txnToken,initTxnDTO);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.payMethod")).as("Pay Method is EMI").isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.feeAmount.value")).as("fee amount is 6.10rs").isEqualTo("6.10");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"instId\":null");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"cardNetwork\":\"MASTER\"}");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":600}");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.0,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":600}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10}");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");

    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch PCF details API response When issuerBank=HDFC Passed")
    public void validateFetchPCFResponseWhenIssuerBankPassed(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("HDFC");
        feeRateFactors.setCardNetwork("");
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithFeeRateFactors(payMode,"MASTER",feeRateFactors, txnToken,initTxnDTO);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.payMethod")).as("Pay Method is EMI").isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.feeAmount.value")).as("fee amount is 7.10rs").isEqualTo("7.10");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"instId\":\"MASTER\"");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"instId\":\"MASTER\",\"issuerBank\":\"HDFC\"}");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":700}");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.0,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":700}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10}");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");

    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch PCF details API response When cardNetwork=DINERS Passed")
    public void validateFetchPCFResponseWhenCardNetworkDinersPassed(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("AMEX");
        feeRateFactors.setCardNetwork("DINERS");
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithFeeRateFactors(payMode,"MASTER",feeRateFactors, txnToken,initTxnDTO);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.payMethod")).as("Pay Method is EMI").isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.feeAmount.value")).as("fee amount is 11rs").isEqualTo("11.10");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"instId\":\"MASTER\"");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"instId\":\"MASTER\",\"cardNetwork\":\"DINERS\",\"issuerBank\":\"AMEX\"}");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":1100}");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.0,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":1100}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10}");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");

    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch PCF details API response When cardNetwork=MAESTRO Passed")
    public void validateFetchPCFResponseWhenCardNetworkMaestroPassed(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("AMEX");
        feeRateFactors.setCardNetwork("MAESTRO");
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithFeeRateFactors(payMode,"MASTER",feeRateFactors, txnToken,initTxnDTO);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.payMethod")).as("Pay Method is EMI").isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.feeAmount.value")).as("fee amount is 13.10rs").isEqualTo("13.10");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"instId\":\"MASTER\"");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"instId\":\"MASTER\",\"cardNetwork\":\"MAESTRO\",\"issuerBank\":\"AMEX\"}");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":1300}");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.0,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":1300}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10}");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");

    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch PCF details API response When cardNetwork=VISA Passed")
    public void validateFetchPCFResponseWhenCardNetworkVisaPassed(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("AMEX");
        feeRateFactors.setCardNetwork("VISA");
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsWithFeeRateFactors(payMode,"MASTER",feeRateFactors, txnToken,initTxnDTO);
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.payMethod")).as("Pay Method is EMI").isEqualTo("EMI");
        Assertions.assertThat(jsonPath.getString("body.consultDetails.EMI.feeAmount.value")).as("fee amount is 0.14rs").isEqualTo("0.14");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"instId\":\"MASTER\"");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"instId\":\"MASTER\",\"cardNetwork\":\"VISA\",\"issuerBank\":\"AMEX\"}");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":4}");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.02,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":0}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10}");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");

    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch bin details  When cardNetwork=Diners Passed")
    public void validateFetchBinDetailsResponseForCardNetworkDiners(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("AMEX");
        feeRateFactors.setCardNetwork("DINERS");
        String payMode = "EMI";
        String emiType="CREDIT_CARD";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,"711033018",payMode,emiType);
        Assertions.assertThat(binResponse.getString("body.pcf.feeAmount.value")).as("fee amount is 11rs").isEqualTo("11.10");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"cardNetwork\":\"DINERS\",\"issuerBank\":\"HDFC\"}");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("{\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":1100}");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.0,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":1100}}]");
        Assertions.assertThat(logs).contains("{\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":1100}");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");
    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "fetch bin details  When cardNetwork=VISA Passed")
    public void validateFetchBinDetailsResponseForCardNetworkVISA(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("AMEX");
        feeRateFactors.setCardNetwork("VISA");
        String payMode = "EMI";
        String emiType="CREDIT_CARD";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,"471865010",payMode,emiType);
        Assertions.assertThat(binResponse.getString("body.pcf.feeAmount.value")).as("fee amount is 0.14rs").isEqualTo("0.14");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"feeRateFactors\":{\"cardNetwork\":\"VISA\",\"issuerBank\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":4");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.02,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":0}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");


    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "validate fields in FEE_BATCH_CONSULT PTC request/response When cardNetwork=VISA Passed")
    public void validatePTCResponseForCardNetworkVISA(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("AMEX");
        feeRateFactors.setCardNetwork("VISA");
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(Constants.MerchantType.PGOnly_Pcf.getId(),txnToken,initTxnDTO.getBody().getOrderId())
                .setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|123|122025")
                .setAuthMode("otp")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"instId\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"cardNetwork\":\"VISA\",\"issuerBank\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":4");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.02,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":0}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");

    }

    @Feature("PGP-46095")
    @Owner(MAYURI)
    @Test(description = "validate fields in FEE_BATCH_CONSULT PTC request/response When cardNetwork=Diners Passed")
    public void validatePTCResponseForCardNetworkDiners(@Optional("false") Boolean isNativePlus) throws Exception {
        feeRateFactors feeRateFactors = new feeRateFactors();
        feeRateFactors.setIssuerBank("AMEX");
        feeRateFactors.setCardNetwork("DINERS");
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly_Pcf)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //PTC
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(Constants.MerchantType.PGOnly_Pcf.getId(),txnToken,initTxnDTO.getBody().getOrderId())
                .setPaymentMode("EMI")
                .setCardInfo("|71103301877466|123|122025")
                .setAuthMode("otp")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        //Assertions.assertThat(processTxnV1Response.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        //NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.getBody().getOrderId(),"FEE_BATCH_CONSULT","RESPONSE");
        Assertions.assertThat(logs).contains("\"payMethod\":\"EMI\"");
        Assertions.assertThat(logs).contains("\"instId\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"cardNetwork\":\"DINERS\",\"issuerBank\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"feeType\":\"ACQUIRING_SERVICE_FEE\"");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":1100");
        Assertions.assertThat(logs).contains("\"feeRateInfos\":[{\"feeRate\":0.0,\"fixedFeeAmount\":{\"currency\":\"INR\",\"value\":1100}}]");
        Assertions.assertThat(logs).contains("\"baseFeeAmount\":{\"currency\":\"INR\",\"value\":10");
        Assertions.assertThat(logs).contains("\"feeType\":\"PLATFORM_SERVICE_FEE\"}");
    }

}

