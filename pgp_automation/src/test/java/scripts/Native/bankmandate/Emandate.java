package scripts.Native.bankmandate;

import com.paytm.api.UpiPredicate;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.api.nativeAPI.SubsMandateCallback;
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
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.api.theia.BankMandateListAPI;
import scripts.api.theia.fetchPayOptions.SSOTokenFetchPayOptionsV2Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.paytm.LocalConfig.PGP_DB_CONNECTION_URL;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;


@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Ankur")
public class Emandate extends PGPBaseTest{

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final String condition = "\"{request -> request.getParameter('cust-id')=='1000130288'}\"";
    private final String upiProfileData = "\"status\": \"SUCCESS\",\n" +
            "        \"seqNo\": \"9800907760114504bf4756b9606a62cepgpsandbox101paytmlocal\",\n" +
            "        \"respMessage\": \"\",\n" +
            "        \"respCode\": \"0\",\n" +
            "\t\"savedMandateBanks\": [\n" +
            "{\n" +
            "\"iconUrl\": \"https://pgp-qa52.paytm.in/native/bank/PPBL.png\",\n" +
            "\"mandateMode\": \"E_MANDATE\",\n" +
            "\"mandateAuthMode\": [\n" +
            "\"DEBIT_CARD\",\n" +
            "\"NET_BANKING\"\n" +
            "],\n" +
            "\"mandateBankCode\": \"PYTM\",\n" +
            "\"accountHolderName\": \"ABC\",\n" +
            "\"maskedAccountNumber\": \"XXXXXXXXXXX0125\",\n" +
            "\"accountType\": \"SAVINGS\",\n" +
            "\"accRefId\": \"220685\",\n" +
            "\"displayName\": \"Bank Account (E-mandate)\",\n" +
            "\"ifsc\": \"AABF0009009\",\n" +
            "\"isHybridDisabled\": false,\n" +
            "\"channelCode\": \"PPBL\",\n" +
            "\"channelName\": \"Paytm Payments Bank\"\n" +
            "}\n" +
            "],\n" +
            "        \"respDetails\": {\n" +
            "            \"profileDetail\": {\n" +
            "                \"vpaDetails\": [\n" +
            "                    {\n" +
            "                        \"name\": \"ankitarora26@paytm\",\n" +
            "                        \"defaultCreditAccRefId\": \"10673\",\n" +
            "                        \"defaultDebitAccRefId\": \"10673\",\n" +
            "                        \"isPrimary\": true\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"bankAccounts\": [\n" +
            "                    {\n" +
            "                        \"bank\": \"Mypsp2\",\n" +
            "                        \"ifsc\": \"AABF0876543\",\n" +
            "                        \"accRefId\": \"10673\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0125\",\n" +
            "                        \"accountType\": \"UOD\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"P2M\",\n" +
            "                        \"warningMessage\": \"Unsecured Overdraft Account can only be used to make payments to merchants\",\n" +
            "                        \"pgBankCode\": \"PPBL\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"100000\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"GREEN\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https://static.paytmbank.com/upi/images/bank-logo/500004.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"MYPSP\",\n" +
            "                        \"ifsc\": \"AABC0876543\",\n" +
            "                        \"accRefId\": \"10679\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0127\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"ATMPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"Y\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"HDFC\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"10\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"GREEN\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500001.png\"\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"bank\": \"Mybank\",\n" +
            "                        \"ifsc\": \"AABD0876543\",\n" +
            "                        \"accRefId\": \"10761\",\n" +
            "                        \"maskedAccountNumber\": \"XXXXXXXXXXX0123\",\n" +
            "                        \"accountType\": \"SAVINGS\",\n" +
            "                        \"credsAllowed\": [\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"OTP\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"SMS\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"CredsAllowedType\": \"PIN\",\n" +
            "                                \"CredsAllowedDType\": \"Numeric\",\n" +
            "                                \"CredsAllowedSubType\": \"MPIN\",\n" +
            "                                \"CredsAllowedDLength\": \"6\",\n" +
            "                                \"dLength\": \"6\"\n" +
            "                            }\n" +
            "                        ],\n" +
            "                        \"name\": \"ABC\",\n" +
            "                        \"mpinSet\": \"N\",\n" +
            "                        \"txnAllowed\": \"ALL\",\n" +
            "                        \"pgBankCode\": \"NHAI\",\n" +
            "                        \"bankMetaData\": {\n" +
            "                            \"perTxnLimit\": \"10\",\n" +
            "                            \"bankHealth\": {\n" +
            "                                \"category\": \"RED\",\n" +
            "                                \"txnAction\": \"ALLOW\",\n" +
            "                                \"displayMsg\": \"The Bank is experiencing downtime.Please select another payment option\"\n" +
            "                            }\n" +
            "                        },\n" +
            "                        \"logo-url\": \"https: //static.paytmbank.com/upi/images/bank-logo/500007.png\"\n" +
            "                    }\n" +
            "                ],\n" +
            "                \"profileStatus\": \"ACTIVE\",\n" +
            "                \"upiLinkedMobileNumber\": \"919999161601\",\n" +
            "                \"isDeviceBinded\": false\n" +
            "            },\n" +
            "            \"metaDetails\": {\n" +
            "                \"banksDown\": [],\n" +
            "                \"npciHealthCategory\": \"GREEN\",\n" +
            "                \"npciHealthMsg\": \"\",\n" +
            "                \"txnAction\": null\n" +
            "            }\n" +
            "        }";

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
    @Test(description = "Validate status=INIT in subs_contract_v2 and subs_payment_detail when subscription is initiated E-Mandate")
    public void TC_EM001_ValidateDBWhenSubsInitiated(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("statuc mismatch")
                .isEqualToIgnoringCase("INIT");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch")
                .isEqualToIgnoringCase("FIRST_REQUEST");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=AUTHORIZED and subs_status=NPCI_PENDING in subs_contract_v2 when subscription is created E-Mandate")
    public void TC_EM002_ValidateDBWhenSubsCreated(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
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
                .isEqualToIgnoringCase("AUTHORIZED");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("NPCI_PENDING");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=ACTIVE and subs_status=ACTIVE in subs_contract_v2 and subs_payment_detail when subscription is created E-Mandate")
    public void TC_EM003_ValidateDBWhenSubsCallBackSent(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
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
    @Test(description = "Validate success of renew subscription E-Mandate")
    public void TC_EM004_ValidateSuccessRenewSubs(@Optional("false") Boolean isNativePlus) throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
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

    @Test(description = "Verify that correct name of bank is displayed in fetchPaymentOptions for E_Mandate for requestType: NATIVE_MF_SIP")
    public void VerifyBankNameinFPOEMandateForNATIVE_MF_SIP() throws Exception{
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .setmandateType("E_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        List<String> bankCodes = PGPHelpers.getBankWhereDisplayNameNotEqualsToBankName(PGP_DB_CONNECTION_URL,"EMANDATE");
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
        String bankDisplayName = PGPHelpers.getBankDisplayName(PGP_DB_CONNECTION_URL,"EMANDATE",matchingCodes.get(0));
        Assertions.assertThat(matchingBankNames.get(0)).isEqualTo(bankDisplayName);
    }

    @Feature("PGP-26452")
    @Owner("Tarun | Karmvir " )
    @Description("Automation JIRA : PGP-27030")
    @Test(description = "Verify that iconUrl is displayed in FPO of Native Transaction for paymode: BANK_MANDATE and mandateType = E_Mandate for Native_Subscription and Display name should be Bank Account (E-mandate)")
    public void iconURLpayModeBankMandateNativeSubs() throws Exception{
        User user = userManager.getForWrite(Label.UPICONSENT);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;

        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();

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
                .setmandateType("E_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("TXN_TOKEN")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(merchant.getId())
                .setToken(txnToken)
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).removeQueryParam("orderId").addQueryParam("orderId",initTxnDTO.orderFromBody()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();

        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedMandateBanks[0].iconUrl")).isNotEmpty();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualToIgnoringCase("Bank Account (E-mandate)"); //PGP-27166
    }

    @Feature("PGP-26452")
    @Owner("Tarun |Karmvir")
    @Description("Automation JIRA : PGP-27030")
    @Test(description = "Verify that iconUrl is displayed in FPO of Native Transaction for paymode: BANK_MANDATE and mandateType = E_Mandate for NATIVE_MF_SIP and Display name should be Bank Account (E-mandate)")
    public void iconURLpayModeBankMandateNativeMFSIP() throws Exception{
        User user = userManager.getForWrite(Label.UPICONSENT);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;

        UpiPredicate upiPredicate = new UpiPredicate(condition,upiProfileData);
        upiPredicate.execute();

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .setmandateType("E_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("TXN_TOKEN")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(merchant.getId())
                .setToken(txnToken)
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).removeQueryParam("orderId").addQueryParam("orderId",initTxnDTO.orderFromBody()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualToIgnoringCase("Bank Account (E-mandate)"); //PGP-27166
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.savedMandateBanks[0].iconUrl")).isNotEmpty();
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=INIT in subs_contract_v2 and subs_payment_detail when subscription is initiated E-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_MF_SIP")
    public void TC_NativeMFSubs_EM_001_ValidateDBWhenSubsInitiatedWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("5")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("statuc mismatch")
                .isEqualToIgnoringCase("INIT");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch")
                .isEqualToIgnoringCase("FIRST_REQUEST");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=AUTHORIZED and subs_status=NPCI_PENDING in subs_contract_v2 when subscription is created E-Mandate  having Amount Greater than 0" +
            "RequestType : NATIVE_MF_SIP")
    public void TC_NativeMFSubs_EM_002_ValidateDBWhenSubsCreatedWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .setChannelCode("HDFC")
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
                .isEqualToIgnoringCase("AUTHORIZED");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("NPCI_PENDING");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=CONFIRMED and subs_status=CONFIRMED in subs_contract_v2 and subs_payment_detail when subscription is created E-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_MF_SIP")
    public void TC_NativeMFSubs_EM_003_ValidateDBWhenCompletingNPCIFormSentWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .setChannelCode("HDFC")
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
        //PGP-34818
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=ACTIVE and subs_status=ACTIVE in subs_contract_v2 and subs_payment_detail when subscription is created E-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_MF_SIP")
    public void TC_NativeMFSubs_EM_004_ValidateDBWhenResponseSentWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
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
                .setChannelCode("HDFC")
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

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                        .as("status mismatch").isEqualToIgnoringCase("ACTIVE"));

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                        .as("status mismatch").isEqualToIgnoringCase("ACTIVE"));
    }











    @Parameters({"isNativePlus"})
    @Test(description = "Validate success of renew subscription E-Mandate when subscription is created E-Mandate having Amount Greater than 0"+
            "RequestType : NATIVE_MF_SIP")
    public void TC_NativeMFSubs_EM_005_ValidateSuccessRenewSubsWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_MUTUAL_MF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("20")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("100")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setmandateType("E_MANDATE")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
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
                .setTxnAmount("10")
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));
    }



    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=INIT in subs_contract_v2 and subs_payment_detail when subscription is initiated E-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_SUBSCRIPTION")
    public void TC_NativeSubs_EM_001_ValidateDBWhenSubsInitiatedWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("5")
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
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        String orderId = initTxnDTO.getBody().getOrderId();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("statuc mismatch")
                .isEqualToIgnoringCase("INIT");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("payment_type", subsId, orderId))
                .as("payment_type mismatch")
                .isEqualToIgnoringCase("FIRST_REQUEST");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=AUTHORIZED and subs_status=NPCI_PENDING in subs_contract_v2 when subscription is created E-Mandate  having Amount Greater than 0" +
            "RequestType : NATIVE_SUBSCRIPTION")
    public void TC_NativeSubs_EM_002_ValidateDBWhenSubsCreatedWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
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
                .setChannelCode("HDFC")
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
                .isEqualToIgnoringCase("AUTHORIZED");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("NPCI_PENDING");
    }





    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=CONFIRMED and subs_status=CONFIRMED in subs_contract_v2 and subs_payment_detail when subscription is created E-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_SUBSCRIPTION")
    public void TC_NativeSubs_EM_003_ValidateDBWhenSubsCompletingFormWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
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
                .setChannelCode("HDFC")
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
        //PGP-34818
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("ACTIVE");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", subsId, orderId))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");

    }


    @Parameters({"isNativePlus"})
    @Test(description = "Validate status=ACTIVE and subs_status=ACTIVE in subs_contract_v2 and subs_payment_detail when subscription is created E-Mandate having Amount Greater than 0" +
            "RequestType : NATIVE_SUBSCRIPTION")
    public void TC_NativeSubs_EM_004_ValidateDBWhenSubsCallBackSentWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
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
                .setChannelCode("HDFC")
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


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                        .as("status mismatch").isEqualToIgnoringCase("ACTIVE"));

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                        .as("status mismatch").isEqualToIgnoringCase("ACTIVE"));

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate success of renew subscription E-Mandate when subscription is created E-Mandate having Amount Greater than 0"+
            "RequestType : NATIVE_SUBSCRIPTION")
    public void TC_NativeSubs_EM_005_ValidateSuccessRenewSubsWhenTxnAmtGreaterThan0(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setmandateType("E_MANDATE")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
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
                .setTxnAmount("10")
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Priority in FPO response when mandateType: E_MANDATE && Payment Mode = BANK_MANDATE in create subs\n")
    public void PGP_27722_VerifyPriorityinFPOResponseEMandatewithPrefEnabled() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER,Label.NONEMIDC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setmandateType("E_MANDATE")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        ArrayList<HashMap<String,Object>> al = fetchPaymentOptionsJson.get("body.merchantPayOption.savedMandateBanks");
        Assertions.assertThat(al.get(0).get("priority")).isEqualTo("1");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify Priority in FPO response when mandateType: E_MANDATE && Payment Mode = null in create subs")
    public void PGP_27722_VerifyPriorityinFPOResponseEMandateBlankPaymode() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER,Label.NONEMIDC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setmandateType("E_MANDATE")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        ArrayList<HashMap<String,Object>> al = fetchPaymentOptionsJson.get("body.merchantPayOption.savedMandateBanks");
        Assertions.assertThat(al.get(0).get("priority")).isEqualTo("3");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify FPO response when mandateType: null && Payment Mode = null in create subs, no saved mandates should appear")
    public void PGP_27722_VerifyPriorityinFPOResponseEMandateBlankPaymodeandMandateType() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER,Label.NONEMIDC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setmandateType("")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        ArrayList<HashMap<String,Object>> al = fetchPaymentOptionsJson.get("body.merchantPayOption.savedMandateBanks");
        Assertions.assertThat(al).isNull();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"isNativePlus"})
    @Test(description = "Verify FPO response when merchant pref is not enabled on a merchant -should use property native.online.paymode.order")
    public void PGP_27722_VerifyPriorityinFPOResponseEMandatewithPrefNotEnabled() throws Exception {
        User user = userManager.getForRead(Label.ADVANCEDEPOSITLOWBALANCEUSER,Label.NONEMIDC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_LIMIT_1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setmandateType("E_MANDATE")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v2").build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        ArrayList<HashMap<String,Object>> al = fetchPaymentOptionsJson.get("body.merchantPayOption.savedMandateBanks");
        Assertions.assertThat(al.get(0).get("priority")).isEqualTo("1");
    }

    @Feature("PGP-44889")
    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Test(description = "Verify list of latest updtaed Banks in FPO for Bank Mandate")
    public void listOfUpdatedNPCIBanksinFPO() throws Exception{
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
                .setmandateType("E_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("TXN_TOKEN")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(merchant.getId())
                .setToken(txnToken)
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).removeQueryParam("orderId").addQueryParam("orderId",initTxnDTO.orderFromBody()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualToIgnoringCase("Bank Account (E-mandate)");
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.mandateBankCode")).toString().contains("AACX, UTIB, PYTM");
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.iconUrl")).isNotEmpty();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.channelCode")).toString().contains("AACX, UTIB, PYTM");
    }

    @Feature("PGP-44889")
    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Test(description = "Verify FPO only contains banks with atleast one auth mode")
    public void listOfUpdatedNPCIBanksinFPOwithAuthMode() throws Exception{
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
                .setmandateType("E_MANDATE")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("TXN_TOKEN")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(merchant.getId())
                .setToken(txnToken)
                .build();
        BankMandateListAPI bankMandateList = new BankMandateListAPI();
        Response response = bankMandateList.execute();
        Assertions.assertThat(response.toString().contains("AACX"));
        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).removeQueryParam("orderId").addQueryParam("orderId",initTxnDTO.orderFromBody()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualToIgnoringCase("Bank Account (E-mandate)");
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.mandateBankCode")).doesNotContain("ABUX, ABVX, ACAX");
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.iconUrl")).isNotEmpty();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.channelCode")).toString().contains("ABUX, ABVX, ACAX");
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.channelCode")).toString().contains("AACX, UTIB, PYTM");

    }

    @Feature("PGP-44889")
    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Test(description = "Verify fetch mandate list API response")
    public void validateFetchMandateListAPI() throws Exception{

        //Validate the Banks are coming in response
        BankMandateListAPI bankMandateList = new BankMandateListAPI();
        Response response = bankMandateList.execute();

        Assertions.assertThat(response.jsonPath().getString("resultInfo.code")).isEqualToIgnoringCase("0000");
        Assertions.assertThat(response.jsonPath().getString("resultInfo.message")).isEqualToIgnoringCase("Success");
        Assertions.assertThat(response.toString().contains("AACX ,UTIB, PYTM, ACUX"));
        Assertions.assertThat(response.jsonPath().toString()).isNotEmpty();

        //Validate the display name value
        response = bankMandateList.execute();
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[0].bankName")).isEqualToIgnoringCase("THE ADARSH CO OP URBAN BANK LTD");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[0].displayName")).isEqualToIgnoringCase("THE ADARSH CO OP URBAN BANK LTD");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[1].bankName")).isEqualToIgnoringCase("AIRTEL PAYMENTS BANK LTD");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[1].displayName")).isEqualToIgnoringCase("AIRTEL PAYMENTS BANK LTD");


        //Validate the display order value
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[0].bankName")).isEqualToIgnoringCase("THE ADARSH CO OP URBAN BANK LTD");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[0].displayOrder")).isEqualToIgnoringCase("9999");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[1].bankName")).isEqualToIgnoringCase("AIRTEL PAYMENTS BANK LTD");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[1].displayOrder")).isEqualToIgnoringCase("9999");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[8].bankName")).isEqualToIgnoringCase("CHHATTISGARH GRAMIN BANK");
        Assertions.assertThat(response.jsonPath().getString("mandateBankList[8].displayOrder")).isEqualToIgnoringCase("9999");

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate sucess Bank Mandate Txn with latest NPCI Banks in FPO")
    public void validateSuccessTxnfromBMwithLatestNPCI(@Optional("false") Boolean isNativePlus) throws Exception {
        User user = userManager.getForRead(Label.UPICONSENT);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_NPCI;
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

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder()
                .setTokenType("TXN_TOKEN")
                .setGenerateOrderId(Boolean.toString(false))
                .setMid(merchant.getId())
                .setToken(txnToken)
                .build();

        SSOTokenFetchPayOptionsV2Test fetchPayOptionsV2= new SSOTokenFetchPayOptionsV2Test();
        JsonPath fetchPaymentOptResponse = given(fetchPayOptionsV2.reqBldr().removeQueryParam("mid").addQueryParam("mid", merchant.getId()).removeQueryParam("orderId").addQueryParam("orderId",initTxnDTO.orderFromBody()).build()).body(fetchPaymentOptionsDTO).post().jsonPath();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes[0].displayName")).isEqualToIgnoringCase("Bank Account (E-mandate)");
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.mandateBankCode")).toString().contains("AACX, UTIB, PYTM");
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.iconUrl")).isNotEmpty();
        Assertions.assertThat(fetchPaymentOptResponse.getString("body.merchantPayOption.paymentModes.payChannelOptions.channelCode")).toString().contains("AACX, UTIB, PYTM");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, txnToken, initTxnDTO.orderFromBody(), subsId)
                .setChannelCode("HDFC")
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


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                        .as("status mismatch").isEqualToIgnoringCase("ACTIVE"));

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                        .as("status mismatch").isEqualToIgnoringCase("ACTIVE"));

    }
}
