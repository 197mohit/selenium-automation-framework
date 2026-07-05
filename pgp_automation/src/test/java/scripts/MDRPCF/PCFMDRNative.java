package scripts.MDRPCF;

import com.paytm.api.PaymentService;
import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ExtendInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CheckoutPage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import scripts.Native.PcfHelpher;

import java.util.Date;

@Feature("PGP-21945")
@Owner("Tarun")
public class PCFMDRNative extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private double txnAmount = 2.00;

    //Boss Panel http://10.142.51.67 >> Manage Merchants >>Commission >> Merchant ID >> Add specific commission to payMode

    Constants.MerchantType pcfMDR = Constants.MerchantType.PCF_MDR;
    Constants.MerchantType pcfMDR_PG2_RTDD = Constants.MerchantType.PCF_MDR_PG2_RTDD;

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
    

    @Test(description = "Native : Verify a successful CC txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulCCTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "CC";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,paymentDTO.getCreditCardNumber().substring(0,6),payMode);
        Double chargeAmountBin  = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("CREDIT_CARD","VISA",txnToken,initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"CREDIT_CARD");

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();
        Assertions.assertThat(chargeAmountBin).as("Charge amount in /fetchBin and /fetchPCF response").isEqualTo(chargeAmountPCF);
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);
       PcfHelpher. validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);


        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "VISA", "HDFC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful CC txn with MASTER on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulCCTxnWithMasterForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD);
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "CC";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
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

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        Assertions.assertThat(chargeAmountBin).as("Charge amount in /fetchBin and /fetchPCF response mismatch ").isEqualTo(chargeAmountPCF);

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);
       PcfHelpher. validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("MASTER");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("CREDIT_CARD", "MASTER", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }



    @Test(description = "Native : Verify a successful DC txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulDCTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "DC";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,paymentDTO.getDebitCardNumber().substring(0,6),payMode);
        Double chargeAmountBin  = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("DEBIT_CARD","VISA",txnToken,initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");

        Assertions.assertThat(chargeAmountBin).as("Charge amount in /fetchBin and /fetchPCF response mismatch ").isEqualTo(chargeAmountPCF);

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);
       PcfHelpher. validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful saved card txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulSavedCardTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "DC";
        User user = userManager.getForWrite(Label.BASIC);

        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //BinDetails
        JsonPath binResponse = PcfHelpher.fetchBinDetails(initTxnDTO,txnToken,paymentDTO.getDebitCardNumber().substring(0,6),payMode);
        Double chargeAmountBin  = PcfHelpher.fetchTotalConvenienceChargesBIN(binResponse);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("DEBIT_CARD","VISA",txnToken,initTxnDTO);
        Double chargeAmountPCF = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"DEBIT_CARD");

        Assertions.assertThat(chargeAmountBin).as("Charge amount in /fetchBin and /fetchPCF response mismatch ").isEqualTo(chargeAmountPCF);

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmountPCF);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmountPCF);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE
        Assertions.assertThat(response.jsonPath().getString("result.cardScheme[0]")).isEqualTo("VISA");

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("DEBIT_CARD", "VISA", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful NB txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulNBTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "NB";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("NET_BANKING","ICICI",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"NET_BANKING");

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.NET_BANKING)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);
        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "ICICI", "ICICI", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful PPBL txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPPBLTxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "NB";
        User user = userManager.getForWrite(Label.PPBL);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative(payMode,"PPBL",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"PPBL");

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.PPBL)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setMpin(paymentDTO.getPasscode())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =   PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);


        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("NET_BANKING", "PPBL", "PPBL", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful UPI txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulUPITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "UPI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative(payMode,"",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"UPI");

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.UPI)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setPayerAccount(paymentDTO.getVpa())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("UPI", "UPI", "PPBLC", "API");

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful PPI txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPPITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "PPI";
        User user = userManager.getForWrite(Label.BASIC);
        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
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


        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.BALANCE)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Test(description = "Native : Verify a successful Paytm Postpaid txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulPaytmPostpaidITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR_PG2_RTDD,"Y");
        PcfHelpher.assertConvFee(pcfMDR_PG2_RTDD,"Y");// PCF Flow
        String payMode = "Paytm Postpaid";
        User user = userManager.getForWrite(Label.POSTPAID);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR_PG2_RTDD)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative("PAYTM_DIGITAL_CREDIT","",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,"PAYTM_DIGITAL_CREDIT");

        OrderDTO orderDTO= new OrderFactory.Native(pcfMDR_PG2_RTDD, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                .setPayerAccount(paymentDTO.getVpa())
                .setMpin(paymentDTO.getPasscode())
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus =PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR_PG2_RTDD,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);
        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("PAYTM_DIGITAL_CREDIT", "PAYTMCC", "PAYTMCC", "API");



        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }


    @Test(description = "Native : Verify a successful EMI txn on PCF merchant applied with MDR commission) when DYNAMIC_CHARGE_TARGET pref is Y in DB")
    public void successfulEMITxnForMDRPlusPCFNative(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        PcfHelpher.assertDynamicChargeTarget(pcfMDR,"Y");
        PcfHelpher.assertConvFee(pcfMDR,"Y");// PCF Flow
        String payMode = "EMI";
        User user = userManager.getForWrite(Label.BASIC);

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pcfMDR)
                .setTxnValue(String.valueOf(txnAmount))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO
        isPcfEnabled(txnToken,initTxnDTO);

        //FetchPCF
        JsonPath jsonPath = PcfHelpher.fetchPCFDetailsNative(payMode,"",txnToken,initTxnDTO);
        Double chargeAmount = PcfHelpher.fetchTotalConvenienceChargesPCF(jsonPath,payMode);

      OrderDTO orderDTO= new OrderFactory.Native(pcfMDR, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.EMI)
              .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
              .setPlanId("HDFC|3")
              .setChannelCode(paymentDTO.getBankName())
              .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PcfHelpher.validateSuccessResponsePCFTxn(orderDTO,pcfMDR,payMode,chargeAmount);

        //MerchantStatus
        TxnStatus txnStatus = PcfHelpher.validateSuccessTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessNativeTxnStatusPCFTxn(orderDTO,payMode,chargeAmount);
        PcfHelpher.validateSuccessPaymentStatusAPIPCFTxn(orderDTO,pcfMDR,payMode,chargeAmount);
        PcfHelpher.validateSuccessTxnStatusListMDRTxn(orderDTO,payMode);
        PcfHelpher.validateOrderStatusAPI(orderDTO,payMode,chargeAmount);

        Response response = PcfHelpher.searchTxnPgPlusBO(txnStatus);

        //FEE FACTOR For PAYMODE

        Assertions.assertThat(CommonHelpers.getFeeFactorAsList(response.jsonPath().getString("result.feeFactor[0]")))
                .as("result.feeFactor[0] mismatch")
                .containsExactlyInAnyOrder("EMI", "HDFC", "HDFC", "API");


        PcfHelpher.validateSuccessPeonPCFTxn(orderDTO,payMode);
    }

    @Feature("PGP-29674")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify if PCF is added on payment services QR code txn.")
    public void verifyMDRPCFforProductCode147() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_code147_PG2_RTDD;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String orderId = CommonHelpers.generateOrderId();

        Date date = new Date();
        String dateAsString = CommonHelpers.getDate(date, "yyyy-MM-dd hh:mm:ss");
        String expiryDate = CommonHelpers.addMonths(dateAsString,"yyyy-MM-dd hh:mm:ss",5);

        PaymentService paymentService = new PaymentService(merchant, "7500", orderId);
        paymentService.setContext("body.additionalInfo.isEdcRequest", "true");
        paymentService.setContext("body.additionalInfo.PRODUCT_CODE", "51051000100000000147");
        paymentService.setContext("body.payAndConfirmRequest", true);
        paymentService.setContext("body.orderDetails", "Dynamic042");
        paymentService.setContext("body.expiryDate", expiryDate);
        JsonPath paymentServiceResponse = paymentService.execute().jsonPath();
        String qrCode = paymentServiceResponse.get("body.qrCodeId").toString();

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setChannelId("WEB")
                .setQRCodeId(qrCode)
                .setgenerateorderId("false")
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRPaymentDetailsResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRPaymentDetailsResponse.get("body.resultInfo.resultStatus").equals("S"));

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setMerchantUniqueReference(qrCode);
        extendInfo.setAdditionalInfo("pa:paytm-42721@paytm|mc:5812|tr:regression120|am:2000|cu:INR|orderAlreadyCreated:true|mid:edcdcc87965867556615|REQUEST_TYPE:UPI_POS_ORDER");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), "SSO", ssoToken, orderId, "7500")
                .setExtendInfo(extendInfo)
                .setRequestType("NATIVE")
                .setPaymentFlow("NONE")
                .setRiskExtendInfo("")
                .setPayerAccount("")
                .setCardInfo("|4718650100030136|333|122024")
                .setMpin("")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        String responseStatus= response.jsonPath().get("body.resultInfo.resultStatus");
        String responseAmt = response.jsonPath().get("body.bankForm.redirectForm.actionUrl");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(responseStatus).contains("S");
        softAssertions.assertThat(responseAmt).contains("7511.8");
        softAssertions.assertAll();
    }

    @Feature("PGP-29674")
    @Owner(Constants.Owner.GAURAV)
    @Test(description = "Verify if PCF is not added on payment services QR code txn for commission slab")
    public void verifyMDRPCFnotAddedforProductCode147() throws Exception {

        Constants.MerchantType merchant = Constants.MerchantType.MDR_PCF_code147_PG2_RTDD;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String orderId = CommonHelpers.generateOrderId();

        Date date = new Date();
        String dateAsString = CommonHelpers.getDate(date, "yyyy-MM-dd hh:mm:ss");
        String expiryDate = CommonHelpers.addMonths(dateAsString,"yyyy-MM-dd hh:mm:ss",5);

        PaymentService paymentService = new PaymentService(merchant, "7000", orderId);
        paymentService.setContext("body.additionalInfo.isEdcRequest", "true");
        paymentService.setContext("body.additionalInfo.PRODUCT_CODE", "51051000100000000147");
        paymentService.setContext("body.payAndConfirmRequest", true);
        paymentService.setContext("body.orderDetails", "Dynamic042");
        paymentService.setContext("body.expiryDate", expiryDate);
        JsonPath paymentServiceResponse = paymentService.execute().jsonPath();
        String qrCode = paymentServiceResponse.get("body.qrCodeId").toString();

        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setChannelId("WEB")
                .setQRCodeId(qrCode)
                .setgenerateorderId("false")
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRPaymentDetailsResponse = fetchQRPaymentDetails.execute().jsonPath();
        Assertions.assertThat(fetchQRPaymentDetailsResponse.get("body.resultInfo.resultStatus").equals("S"));

        ExtendInfo extendInfo = new ExtendInfo();
        extendInfo.setMerchantUniqueReference(qrCode);
        extendInfo.setAdditionalInfo("pa:paytm-42721@paytm|mc:5812|tr:regression120|am:2000|cu:INR|orderAlreadyCreated:true|mid:edcdcc87965867556615|REQUEST_TYPE:UPI_POS_ORDER");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchant.getId(), "SSO", ssoToken, orderId, "7000")
                .setExtendInfo(extendInfo)
                .setRequestType("NATIVE")
                .setPaymentFlow("NONE")
                .setRiskExtendInfo("")
                .setPayerAccount("")
                .setCardInfo("|4718650100030136|333|122024")
                .setMpin("")
                .build();

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        Response response = processTransactionV1.execute();
        String responseStatus= response.jsonPath().get("body.resultInfo.resultStatus");
        String responseAmt = response.jsonPath().get("body.bankForm.redirectForm.actionUrl");
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(responseStatus).contains("S");
        softAssertions.assertThat(responseAmt).contains("7000");
        softAssertions.assertAll();
    }
}

