package scripts.BankMandate;
import com.paytm.api.CreateSubscription;
import com.paytm.api.Peon;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.api.nativeAPI.SubsMandateCallback;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.OrderAdditionalInfo;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.LostInSpacePage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.*;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.Native.ProcessTransactionTests;
import io.restassured.response.Response;
import com.paytm.api.ProcessTransactionV1;
import java.util.Date;

public class DigioEnach {

    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";


    private final ProcessTransactionTests processTransactionTests = new ProcessTransactionTests();

    private InitTxnResponseDTO validateSuccessInitiateSubscription(InitTxnDTO initTxnDTO) {
        InitTxnResponseDTO responseDTO = NativeHelpers.initiateNativeSubscription(initTxnDTO);
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("S");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("0000");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Success");
        return responseDTO;

    }

    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify txn token and subs id generated successfully when mandateType is blank ")
    public void TC_001_mandateAuth_blank(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)       
                .setTxnValue("0")
                .setmandateType("")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify txn token and subs id generated successfully when mandateType is AADHAR")
    public void TC_002_mandateType_AADHAR(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setmandateType("AADHAR")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify txn token and subs id generated successfully when mandateType is DEBIT_CARD")
    public void TC_003_mandateType_DEBIT_CARD(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setmandateType("DEBIT_CARD")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify txn token and subs id generated successfully when mandateType is NET_BANKING")
    public void TC_004_mandateType_NET_BANKING(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setmandateType("NET_BANKING")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        validateSuccessInitiateSubscription(initTxnDTO);
    }

    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify that v1/ptc is successful when different mandate types are passed in create & ptc")
    public void TC_005_mandateType_create_and_PTC(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_NPCI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setmandateType("AADHAR")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandateJson(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .setMandateAuthMode("NET_BANKING")
                .build();

        JsonPath path = processTransactionTests.Validate_FetchPayInstrumentV5(txnToken, initTxnDTO, "BANK_MANDATE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(orderDTO);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
                

    }


    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify that v1/ptc is successful when different mandate type is passed in create but blank in ptc")
    public void TC_006_mandateType_AADHAR_blankPTC(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_NPCI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setmandateType("AADHAR")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandateJson(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .setMandateAuthMode("")
                .build();

        JsonPath path = processTransactionTests.Validate_FetchPayInstrumentV5(txnToken, initTxnDTO, "BANK_MANDATE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(orderDTO);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
                

    }


    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify that v1/ptc is successful when mandateType=AADHAR is passed in ptc")
    public void TC_007_mandateType_AADHAR_inPTC(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_NPCI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandateJson(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .setMandateAuthMode("AADHAR")
                .build();

        JsonPath path = processTransactionTests.Validate_FetchPayInstrumentV5(txnToken, initTxnDTO, "BANK_MANDATE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(orderDTO);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
                

    }

    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify that v1/ptc is successful when mandateType=NET_BANKING is passed in ptc")
    public void TC_008_mandateType_NB_inPTC(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_NPCI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandateJson(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .setMandateAuthMode("NET_BANKING")
                .build();

        JsonPath path = processTransactionTests.Validate_FetchPayInstrumentV5(txnToken, initTxnDTO, "BANK_MANDATE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(orderDTO);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
                

    }

    @Feature("PGP-58764")
    @Owner(Constants.Owner.AKSHAT)
    @Test(description = "Verify that v1/ptc is successful when mandateType=DEBIT_CARD is passed in PTC")
    public void TC_009_mandateType_DC_inPTC(@Optional("false") Boolean isNativePlus) throws Exception {
       
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_NPCI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType(NATIVE_SUBSCRIPTION)
                .build();
        InitTxnResponseDTO responseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = responseDTO.getBody().getTxnToken();
        String subsId = responseDTO.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandateJson(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
                .setCHANNEL_ID("WEB")
                .setMandateAuthMode("DEBIT_CARD")
                .build();

        JsonPath path = processTransactionTests.Validate_FetchPayInstrumentV5(txnToken, initTxnDTO, "BANK_MANDATE", "false");
        Assertions.assertThat(path.getString("body.paymentFlow")).isEqualTo("NONE");

        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(orderDTO);
        Response ptcResponse = processTransactionV1.execute();
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");
                

    }



}
