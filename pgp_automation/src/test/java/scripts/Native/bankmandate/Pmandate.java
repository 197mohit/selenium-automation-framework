package scripts.Native.bankmandate;

import com.paytm.api.nativeAPI.*;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Step;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV2Test;

import java.util.ArrayList;
import java.util.List;
import static com.paytm.LocalConfig.PGP_DB_CONNECTION_URL;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Ankur")
public class Pmandate extends PGPBaseTest{

    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Step()
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

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=IN_AUTHORIZATION, sub_status=PPBL_PENDING  in subs_contract_v2 and subs_payment_detail when subscription is created P-Mandate")
    public void TC_PM001_ValidateDBWhenSubsCreated(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("IN_AUTHORIZATION");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("PPBL_PENDING");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=ACTIVE, sub_status=ACTIVE  in subs_contract_v2 and subs_payment_detail when subscription is created P-Mandate")
    public void TC_PM002_ValidateDBWhenSubsCallbackSent(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate success of renew subscription P-Mandate")
    public void TC_PM003_ValidateSuccessRenewSubs(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");
        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), subsId, initTxnDTO.txnAmountFromBody())
                .setTxnAmount("2")
                .setMerchantKey(merchant.getKey())
                .setRequestType("SUBS_RENEWAL_MF_SIP")
                .build();
        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);
        jsonPath = renewSubscription.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus"))
                .as("Result status mismatch")
                .isEqualToIgnoringCase("S");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate success of upload and download of P-Mandate")
    public void TC_PM004_ValidateUploadDownload(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        PMandateDownload pMandateDownload = new PMandateDownload(subsId, merchant.getId());
        JsonPath jsonPath = pMandateDownload.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.code"))
                .as("body.resultInfo.code mismatch")
                .isEqualToIgnoringCase("3006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.status"))
                .as("body.resultInfo.status mismatch")
                .isEqualToIgnoringCase("SUCCESS");
        PMandateUpload pMandateUpload = new PMandateUpload(subsId, merchant.getId());
        jsonPath = pMandateUpload.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.code"))
                .as("body.resultInfo.code mismatch")
                .isEqualToIgnoringCase("3006");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.status"))
                .as("body.resultInfo.status mismatch")
                .isEqualToIgnoringCase("SUCCESS");

    }


    @Test(description = "Verify that correct name of bank is displayed in fetchPaymentOptions for Paper_Mandate(PMANDATE) for requestType: NATIVE_MF_SIP")
    public void VerifyBankNameinFPOPMandateForNATIVE_MF_SIP() throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .setmandateType("PAPER_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        List<String> bankCodes = PGPHelpers.getBankWhereDisplayNameNotEqualsToBankName(PGP_DB_CONNECTION_URL,"PHYSICAL");
        List<String> channelCodes = (List<String>) fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.payChannelOptions.channelCode").get(0);
        List<String> channelNames = (List<String>) fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.payChannelOptions.channelName").get(0);
        List<String> matchingCodes = new ArrayList<>();
        List<String> matchingBankNames = new ArrayList<>();
        for(int i=0; i < bankCodes.size();i++ ){
            for(int j =0; j< channelCodes.size(); j++){
                if(bankCodes.get(i).equals(channelCodes.get(j))) {
                    matchingCodes.add(channelCodes.get(j));
                    matchingBankNames.add(channelNames.get(j));
                }
            }
        }
        String bankDisplayName = PGPHelpers.getBankDisplayName(PGP_DB_CONNECTION_URL,"PHYSICAL",matchingCodes.get(0));
        Assertions.assertThat(matchingBankNames.get(0)).isEqualTo(bankDisplayName);
    }


    @Test(description = "Verify that correct name of bank is displayed in fetchPaymentOptions for Paper_Mandate(PMANDATE) for requestType: NATIVE_SUBSCRIPTION")
    public void VerifyBankNameinFPOPMandateForNATIVE_SUBSCRIPTION() throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setmandateType("PAPER_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        List<String> bankCodes = PGPHelpers.getBankWhereDisplayNameNotEqualsToBankName(PGP_DB_CONNECTION_URL,"PHYSICAL");
        List<String> channelCodes = (List<String>) fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.payChannelOptions.channelCode").get(0);
        List<String> channelNames = (List<String>) fetchPaymentOptionsJson.getList("body.merchantPayOption.paymentModes.payChannelOptions.channelName").get(0);
        List<String> matchingCodes = new ArrayList<>();
        List<String> matchingBankNames = new ArrayList<>();
        for(int i=0; i < bankCodes.size();i++ ){
            for(int j =0; j< channelCodes.size(); j++){
                if(bankCodes.get(i).equals(channelCodes.get(j))) {
                    matchingCodes.add(channelCodes.get(j));
                    matchingBankNames.add(channelNames.get(j));
                }
            }
        }
        String bankDisplayName = PGPHelpers.getBankDisplayName(PGP_DB_CONNECTION_URL,"PHYSICAL",matchingCodes.get(0));
        Assertions.assertThat(matchingBankNames.get(0)).isEqualTo(bankDisplayName);
    }



    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=IN_AUTHORIZATION and subs_status=PPBL_PENDING in subs_contract_v2 when subscription is created P-Mandate  having Amount Greater than 0" +
            "RequestType : NATIVE_MF_SIP")
    public void TC_NativeMFSubs_PM_001_ValidateDBWhenSubsCreatedWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("100")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("IN_AUTHORIZATION");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("PPBL_PENDING");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=ACTIVE and subs_status=ACTIVE in subs_contract_v2 and subs_payment_detail when subscription is created P-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_MF_SIP")
    public void TC_NativeMF_PM_Subs002_ValidateDBWhenSubsCallBackSentWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("100")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
                Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=AUTHORIZED and subs_status=PPBL_PENDING in subs_contract_v2 when subscription is created P-Mandate  having Amount Greater than 0" +
            "RequestType : NATIVE_SUBSCRIPTION")
    public void TC_NativeSubs_PM_001_ValidateDBWhenSubsCreatedWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("100")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("200")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("IN_AUTHORIZATION");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("PPBL_PENDING");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=ACTIVE and subs_status=ACTIVE in subs_contract_v2 and subs_payment_detail when subscription is created P-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_SUBSCRIPTION")
    public void TC_NativeSubs_PM_002_ValidateDBWhenSubsCallBackSentWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("100")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("1000")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("KOTAK")
                .setCHANNEL_ID("WEB")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateRespCode("3006")
                .assertAll();
        JsonPath jsonPath = new SubsMandateCallback(subsId, "ACTIVE").execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
    }

    @Feature("PGP-26975")
    @Owner("Karmvir")
    @Description("Automation JIRA : PGP-27166")
@Test(description = "Validate that display name should be Bank Account (E-mandate) in response of FPO when requestType is NATIVE_SUBSCRIPTION and " +
        "mandateType is PAPER_MANDATE")
    public void ValidateDisplayNameInResponseOfFPOForRequestTypeNativeSubscription() throws Exception {
    User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
    Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
            .setTxnValue("0")
            .setSubscriptionPaymentMode("BANK_MANDATE")
            .setSubscriptionAmountType("VARIABLE")
            .setSubscriptionMaxAmount("10")
            .setSubscriptionFrequency("1")
            .setSubscriptionFrequencyUnit("ONDEMAND")
            .setSubscriptionGraceDays("0")
            .setSubscriptionStartDate(CommonHelpers.getDate().toString())
            .setRequestType("NATIVE_SUBSCRIPTION")
            .setmandateType("PAPER_MANDATE")
            .build();
    InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
    FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
            initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
    JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
    Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualToIgnoringCase("Bank Account (E-mandate)");
}

    @Feature("PGP-26975")
    @Owner("Karmvir")
    @Description("Automation JIRA : PGP-27166")
    @Test(description = "Validate that display name should be Bank Account (E-mandate) in response of FPO when requestType is NATIVE_MF_SIP and " +
            "mandateType is PAPER_MANDATE")
    public void ValidateDisplayNameInResponseOfFPOForRequestTypeNATIVE_MF_SIP() throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.MF_SIP_NEW_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .setmandateType("PAPER_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("S");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualToIgnoringCase("Bank Account (E-mandate)");
    }

    @Feature("PGP-44889")
    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Test(description = "Verify list of latest updated Banks in FPO for Paper Bank Mandate")
    public void listOfUpdatedNPCIBanksinFPOforPMandate() throws Exception {
        User user = userManager.getForWrite(Label.UPICONSENT);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_NPCI;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setmandateType("PAPER_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("TXN_TOKEN")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(merchant.getId())
                .setToken(txnToken)
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2 = new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).removeQueryParam("orderId").addQueryParam("orderId", initTxnDTO.orderFromBody()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes[0]")).doesNotContain("P_MANDATE");
    }
}

