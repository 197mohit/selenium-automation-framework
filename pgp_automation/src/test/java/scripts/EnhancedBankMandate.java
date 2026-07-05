package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.Peon;
import com.paytm.api.UpiPredicate;
import com.paytm.api.nativeAPI.BankMandatePaymentResponse;
import com.paytm.api.nativeAPI.RenewSubscription;
import com.paytm.api.nativeAPI.SubsMandateCallback;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.*;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

import static org.awaitility.Awaitility.await;


@Owner("Gagandeep")
@Feature("Bank Mandates on Redirection")
@Epic("PGP-17572")
//TODO: remove ifelse condition at test cases level for proceed button.
public class EnhancedBankMandate extends PGPBaseTest {


    private final static String NATIVE_SUBSCRIPTION = "NATIVE_SUBSCRIPTION";
    private final static String IFSCDETAILS = "PYTM0000001";
    private final static String userBankName = "Akshat Sharma";
    private final static String bankDetails = "915445500424";
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String condition = "\"{request -> request.getParameter('cust-id')=='{CUST_ID}'}\"";
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
            "\"displayName\": \"PPBL - E-mandate\",\n" +
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

    @Parameters({"theme"})
    @Test(description = "Validate that Purpose of subscription is displayed on cashier page if passed by customer")
    public void TC_001_Enhanced_ValidateSubsPurposeIfCustomerPass(@Optional("enhancedwap_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";
        String SubscriptionPurpose = "Loan Amount Payment";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");


        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        if (theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

            /*Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandateRevampPage.subscriptionDetails.PURPOSE.toString())).isEqualTo(SubscriptionPurpose);*/ //Purpose is removed from UI
        }
        else {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandatePage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

            /*Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandatePage.subscriptionDetails.PURPOSE.toString())).isEqualTo(SubscriptionPurpose);*/ //Purpose is removed from UI
        }
        bankMandatePage.payButton().click();

        ResponsePage responsePage = new ResponsePage();

        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "If purpose is not displayed than one category description should be displayed")
    public void TC_002_Enhanced_ValidateSubsPurposeNotComingIfCustomerPassDontPass(@Optional("enhancedwap_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");


        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

            /*Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandateRevampPage.subscriptionDetails.PURPOSE.toString())).isEqualTo("Others");*/ //Purpose is removed from UI
        }
        else {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandatePage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

         /*   Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandatePage.subscriptionDetails.PURPOSE.toString())).isEqualTo("Others");*/ //Purpose is removed from UI
        }

        bankMandatePage.payButton().click();

        ResponsePage responsePage = new ResponsePage();

        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Bank Account Details can be passed at the time of checkout the data is prefilled with account")
    public void TC_003_Enhanced_ValidateBankAccountDetailsIsPrefilled(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";


        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();
        cashierPage.BankDetails().assertDisabled();
        cashierPage.IfscDetails().assertDisabled();


    }

    @Parameters({"theme"})
    @Test(description = "Verify that top 6 banks to displayed by default - if no data is pre-filled(E mandate banks)")
    public void TC_004_Enhanced_ValidateTopSixBankDisplayed(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";


        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSubscriptionPurpose("")
                .setAccountNumber("")
                .setBANK_IFSC("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();

        int noOfBanks = cashierPage.getBankMandateList().size();

        Assertions.assertThat(noOfBanks).as("Top 6 Banks are not getting displayed").isEqualTo(6);


    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription created in Authorized state and sub_status=NPCI_PENDING for Amount greater than 0")
    public void TC_005_Enhanced_ValidateSubsCreatedInAuthorizedStateAmountGreaterThan0(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status",
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualToIgnoringCase("AUTHORIZED");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status",
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), orderDTO.getORDER_ID()))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("NPCI_PENDING");

    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription created in Active state and sub_status is Active on getting response from NPCI for Amount greater than 0")
    public void TC_006_Enhanced_ValidateSubsCreatedStatusIsActiveResponseFromNPCIGreaterThan0(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();


        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultCOde mismtach")
                .isEqualToIgnoringCase("3006");


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status",
                        SubsId, orderDTO.getORDER_ID()))
                        .as("status mismatch")
                        .isEqualToIgnoringCase("ACTIVE"));


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status",
                SubsId, orderDTO.getORDER_ID()))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE"));

    }

    @Parameters({"theme"})
    @Test(description = "Validate subscription Renewed for Amount greater than 0")
    public void TC_007_Enhanced_ValidateSubRenewedStatusForAmountGreaterThan0(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();


        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultcode mismtach")
                .isEqualToIgnoringCase("3006");


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));

        //InstaProxyLogsExtsno

        String grepEsn = "grep \"" + orderDTO.getORDER_ID() + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        JsonPath jsonPath1 = new BankMandatePaymentResponse(extSnValue).execute().jsonPath();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, TxnMaxAmount)
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(SubsId, orderDTO.getORDER_ID())).isNotNull();

        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, orderDTO.getORDER_ID())).isEqualTo("ACTIVE");
    }


    @Parameters({"theme"})
    @Test(description = "Validate subscription Upfront and isUpfrontTxnPending is Updated from Amount Greater than 0 in Meta Data")
    public void TC_008_Enhanced_ValidateSubsPurposeValueIsUpdatedForUpfrontAmountInMetaData(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";
        String SubscriptionPurpose = "Loan Amount Payment";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP)) {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);
        }
        else {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandatePage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Rs" + TxnMaxAmount);
        }


        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata",
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualToIgnoringCase("{\"isUpfrontTxnPending\":true,\"purpose\":\"Loan Amount Payment\",\"totalPaymentCount\":99,\"pud\":true}");

    }

    @Parameters({"theme"})
    @Test(description = "Validate isUpfrontTxnPending is Not Updated for Amount equal to 0 in Meta Data")
    public void TC_009_Enhanced_ValidateSubsPurposeValueIsNotUpdatedForUpfrontAmountIs0InMetaData(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "Loan Amount Payment";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("1")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        bankMandatePage.activateSubscription().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata",
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualToIgnoringCase("{\"purpose\":\"Loan Amount Payment\",\"totalPaymentCount\":99,\"pud\":true}");

    }


    @Parameters({"theme"})
    @Test(description = "Validate subscription Upfront is not passed Others is Updated from Amount Greater than 0 in Meta Data")
    public void TC_010_Enhanced_ValidateSubsPurposeValueNotPassedIsUpdatedOthersIsUpdatedForUpfrontAmountInMetaData(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";
        String SubscriptionPurpose = "";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata",
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualToIgnoringCase("{\"isUpfrontTxnPending\":true,\"purpose\":\"Others\",\"totalPaymentCount\":99,\"pud\":true}");

    }


    @Parameters({"theme"})
    @Test(description = "Validate subscription Upfront is Not Updated when Value is Not passed for Amount equal to 0 in Meta Data")
    public void TC_011_Enhanced_ValidateSubsPurposeValueIsNotUpdatedWHenNotPassedForUpfrontAmountIs0InMetaData(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("1")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        bankMandatePage.activateSubscription().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("metadata",
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), orderDTO.getORDER_ID()))
                .as("status mismatch")
                .isEqualToIgnoringCase("{\"purpose\":\"Others\",\"totalPaymentCount\":99,\"pud\":true}");

    }


    @Parameters({"theme"})
    @Test(description = "Validate subscription Bank Mandate frequency other than Ondemand")
    public void TC_012_Enhanced_ValidateBankMandateFrequencyOtherThanOndemand(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";
        String SubscriptionPurpose = "Loan Amount Payment";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto Rs" + TxnMaxAmount);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Verify that BankMandate subscription is successful through DC paymode (0 amount)")
    public void TC_013_Enhanced_ValidateBankMandateSubscriptionDCPAymodeAmount0(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "Loan Amount Payment";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("1")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        bankMandatePage.activateSubscription().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Verify that BankMandate subscription is successful through DC paymode more than 0")
    public void TC_014_Enhanced_ValidateBankMandateSubscriptionDCPAymodeAmountMoreThan0(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "Loan Amount Payment";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();


    }

    @Parameters({"theme"})
    @Test(description = "Verify that Renew subscription is successful through DC paymode more than 0")
    public void TC_015_Enhanced_ValidateBankMandateSubscriptionRenewDCPAymodeAmountMoreThan0(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.payBy(Constants.PayMode.BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();


        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultcode mismtach")
                .isEqualToIgnoringCase("3006");


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", SubsId, orderDTO.getORDER_ID()))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");

        //InstaProxyLogsExtsno
        String grepEsn = "grep \"" + orderDTO.getORDER_ID() + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        JsonPath jsonPath1 = new BankMandatePaymentResponse(extSnValue).execute().jsonPath();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, TxnMaxAmount)
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(SubsId, orderDTO.getORDER_ID())).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, orderDTO.getORDER_ID())).isEqualTo("ACTIVE");

    }

    @Parameters({"theme"})
    @Test(description = "Verify Saved account that upfront amount payment is successful through saved accounts (NB) for amount greater than O")
    public void TC_016_Enhanced_ValidateSavedBankMandateSubscriptionRenewNBPAymodeAmountMoreThan0(@Optional("enhancedweb") String theme) throws Exception {

        User user = userManager.getForRead(Label.UPICONSENT);
        condition = condition.replace("{CUST_ID}", user.custId());

        UpiPredicate upiPredicate = new UpiPredicate(condition, upiProfileData);
        upiPredicate.execute();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking")
                .setSavedBankMandateAccount("PPBL");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = new BankMandatePage();
        bankMandatePage.saveSubscribe().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();


        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultcode mismtach")
                .isEqualToIgnoringCase("3006");


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", SubsId, orderDTO.getORDER_ID()))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");

        //InstaProxyLogsExtsno
        String grepEsn = "grep \"" + orderDTO.getORDER_ID() + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        JsonPath jsonPath1 = new BankMandatePaymentResponse(extSnValue).execute().jsonPath();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, TxnMaxAmount)
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(SubsId, orderDTO.getORDER_ID())).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, orderDTO.getORDER_ID())).isEqualTo("ACTIVE");

    }

    @Parameters({"theme"})
    @Test(description = "Verify that upfront amount payment is successful through saved accounts (DC)for amount greater than O")
    public void TC_017_Enhanced_ValidateSavedBankMandateSubscriptionRenewDCPAymodeAmountMoreThan0(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.UPICONSENT);
        condition = condition.replace("{CUST_ID}", user.custId());


        UpiPredicate upiPredicate = new UpiPredicate(condition, upiProfileData);
        upiPredicate.execute();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String SubscriptionPurpose = "Loan Amount Payment";
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card")
                .setSavedBankMandateAccount("PPBL");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = new BankMandatePage();
        bankMandatePage.saveSubscribe().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();


        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultcode mismtach")
                .isEqualToIgnoringCase("3006");


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));

        //InstaProxyLogsExtsno
        String grepEsn = "grep \"" + orderDTO.getORDER_ID() + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        JsonPath jsonPath1 = new BankMandatePaymentResponse(extSnValue).execute().jsonPath();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, TxnMaxAmount)
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(SubsId, orderDTO.getORDER_ID())).isNotNull();
        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, orderDTO.getORDER_ID())).isEqualTo("ACTIVE");

    }

    @Parameters({"theme"})
    @Test(description = "Verify that upfront amount payment is successful through saved accounts (NB) for equal to O")
    public void TC_018_Enhanced_ValidateSavedBankMandateSubscriptionRenewNBPAymodeAmount0(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.UPICONSENT);
        condition = condition.replace("{CUST_ID}", user.custId());
        UpiPredicate upiPredicate = new UpiPredicate(condition, upiProfileData);
        upiPredicate.execute();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking")
                .setSavedBankMandateAccount("PPBL");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("0")                             //Txn amount is 0
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = new BankMandatePage();
        bankMandatePage.saveSubscribe().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();


        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultcode mismtach")
                .isEqualToIgnoringCase("3006");


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", SubsId, orderDTO.getORDER_ID()))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");


        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, TxnMaxAmount)
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, orderDTO.getORDER_ID())).isEqualTo("ACTIVE"));

    }


    @Parameters({"theme"})
    @Test(description = "Verify that upfront amount payment is successful through saved accounts (DC)for amount equal to  O")
    public void TC_019_Enhanced_ValidateSavedBankMandateSubscriptionRenewDCPAymodeAmount0(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.UPICONSENT);
        condition = condition.replace("{CUST_ID}", user.custId());
        UpiPredicate upiPredicate = new UpiPredicate(condition, upiProfileData);
        upiPredicate.execute();
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card")
                .setSavedBankMandateAccount("PPBL");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.SAVED_BANK_MANDATE, paymentDTO);

        BankMandatePage bankMandatePage = new BankMandatePage();
        bankMandatePage.saveSubscribe().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();


        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultcode mismtach")
                .isEqualToIgnoringCase("3006");


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));


        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, TxnMaxAmount)
                .setMerchantKey(merchant.getKey())
                .build();

        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, orderDTO.getORDER_ID())).isEqualTo("ACTIVE"));

    }


    @Parameters({"theme"})
    @Test(description = "Validate Bank Mandate Txn While Manually Entering Bank Account through DC txn")
    public void TC_020_Enhanced_ValidateBankMandateTxnWhileManuallyEnteringBankAccountDC(@Optional("enhancedwap_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setBankIfsc("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("100")
                .setUSER_NAME("")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose("")
                .setAccountNumber("")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.BankMandateRadioButton().click();

        cashierPage.getBankMandateList().get(0).click();

        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
       if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP))
        {cashierPage.proceedBtn();}
        else{ cashierPage.buttonPGPayNow().click();}

        BankMandatePage bankMandatePage = new BankMandatePage();

        //bankMandatePage.payButton().click();
        bankMandatePage.payToSubscribe().click();
        bankMandatePage.payToSubscribe().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Validate Bank Mandate Txn While Manually Entering Bank Account through NB txn")
    public void TC_021_Enhanced_ValidateBankMandateTxnWhileManuallyEnteringBankAccountNB(@Optional("enhancedweb_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setBankIfsc("")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose("")
                .setAccountNumber("")
                .setSUBS_FREQUENCY("1")
                .setUSER_NAME("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.BankMandateRadioButton().click();

        cashierPage.getBankMandateList().get(0).click();

        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP))
        {cashierPage.proceedBtn().click();}
        else{ cashierPage.buttonPGPayNow().click();}

        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();
    }


    //TODO Whitelisting of find IFSC link from wallet end getting CORS issue due to connectivity
  //  @Parameters({"theme"})
  //  @Test(description = "Validate Fetch Mandate IFSC Bank Mandate Txn", enabled = false)
    public void TC_022_Enhanced_ValidateFetchIFSCBankMandateTxn(@Optional("enhancedweb") String theme) throws Exception {


        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("1")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose("")
                .setAccountNumber("")
                .setBankIfsc("")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.BankMandateRadioButton().click();

        cashierPage.getBankMandateList().get(0).click();

        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        cashierPage.findIfsc().click();
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP))
        {cashierPage.proceedBtn();}
        else{ cashierPage.buttonPGPayNow().click();}
        BankMandatePage bankMandatePage = new BankMandatePage();
        bankMandatePage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();
    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.TARUN)
    @Feature("PGP-26968")
    @Description("Automation JIRA : PGP-27164")
    @Test(description = "Verify that peon is not sent for 0 amount transaction in case of closer order when payMode=BANK_MANDATE & requestType =NATIVE_SUBSCRIPTION_PAY")
    public void zeroAmountSubscription(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("0")
                .setBankIfsc("")
                .setSUBS_MAX_AMOUNT("1")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSubscriptionPurpose("")
                .setAccountNumber("")
                .setSUBS_FREQUENCY("1")
                .setUSER_NAME("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        cashierPage.BankMandateRadioButton().click();

        cashierPage.getBankMandateList().get(0).click();

        cashierPage.IfscDetails().sendKeys(IFSCDETAILS);
        cashierPage.UserBankName().sendKeys(userBankName);
        cashierPage.BankDetails().sendKeys(bankDetails);
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP))
        {cashierPage.proceedBtn();}
        else{ cashierPage.buttonPGPayNow().click();}

        BankMandatePage bankMandatePage = new BankMandatePage();

      //  bankMandatePage.payButton().click();
        bankMandatePage.payToSubscribe().click();
        bankMandatePage.payToSubscribe().click();
       // bankMandatePage.activateSubscription().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

       com.paytm.api.Peon peon = new Peon(orderDTO.getORDER_ID());
       peon.executeToGetNoResponse();

    }

    @Parameters({"theme"})
    @Owner(Constants.Owner.PRIYANSHI)
    @Feature("PGP-33717")
    @Test(description = "Verify that E-NACH is displayed saved bank mandate account for saved account on cashier page")
    public void verify_SavedBankMandate_Details(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.UPICONSENT);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";
                OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
          cashierPage.savedBankMandate().assertVisible();
          //Assertions.assertThat(cashierPage.savedBankMandateName().getText()).contains("E-NACH");
        Assertions.assertThat(cashierPage.savedBankMandateName().getText()).contains("eNACH");
          cashierPage.subscriptionDetails().click();
         //Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().isDisplayed());
        Assertions.assertThat(cashierPage.subDetailsPage().getText()).as("Subscription UI message change").isEqualTo("Amount to be Paid Now");
         // cashierPage.subsDetailsRecurringAmount().assertVisible();
        //Assertions.assertThat(cashierPage.subsDetailsRecurringAmount().getText()).contains("Recurring");
    }

    @Parameters({"theme"})
    @Feature("PGP-35827")
    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Test(description = "Verify that Subscription is created for 0.00 txn amt and pay mode is UPI")
    public void verifySubscriptionCreatedFor0TxnAmtWithTwoDecimalPlacePayModeUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        String txnAmount = "0.00";
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(txnAmount)
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setPaymentMode(Constants.PayMode.UPI.toString())
                .build();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        // Payment will be done of 1 rs in case of UPI even though subscription amount is 0 rs
        responsePage.validateTxnAmount("1.0")
                .validatePaymentMode(Constants.PayMode.UPI.toString())
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Parameters({"theme"})
    @Feature("PGP-35827")
    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Test(description = "Verify that Subscription is created for 0.0 txn amt and pay mode is UPI")
    public void verifySubscriptionCreatedFor0TxnAmtWithOneDecimalPlacePayModeUPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        String txnAmount = "0.0";
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(txnAmount)
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setPaymentMode(Constants.PayMode.UPI.toString())
                .build();
        checkoutPage.createOrder(orderDTO);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        // Payment will be done of 1 rs in case of UPI even though subscription amount is 0 rs
        responsePage.validateTxnAmount("1.0")
                .validatePaymentMode(Constants.PayMode.UPI.toString())
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Parameters({"theme"})
    @Feature("PGP-35827")
    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Test(description = "Verify that Subscription is created for 0.00 txn amt and pay mode is blank and user selects Bank Mandate on cashier page")
    public void verifySubsFor0TxnAmtWithTwoDecimalPlacePayModeBlankAndPaymentThroughMandate(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;

        String txnAmount = "0.00";
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(txnAmount)
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber(bankDetails)
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.waitUntilLoads();
        cashierPage.proceedBtn().click();

        BankMandatePage bankMandatePage = new BankMandatePage();
        bankMandatePage.activateSubscription().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateRespCode("3006")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("SUCCESS")
                .validateGatewayName(Constants.Bank.PPBL.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Parameters({"theme"})
    @Feature("PGP-35827")
    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Test(description = "Verify that Subscription is created for 0.00 txn amt and pay mode is PPI")
    public void verifySubsFor0TxnAmtWithTwoDecimalPlacePayModePPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        WalletHelpers.modifyBalance(user, 10.0);

        String txnAmount = "0.00";
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(txnAmount)
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setPaymentMode(Constants.PayMode.WALLET.toString())
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        // Payment will be done of 1 rs in case of Wallet even though subscription amount is 0 rs
        responsePage.validateTxnAmount("1.0")
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName(Constants.PayMode.SUBS_PPBL_MID.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Parameters({"theme"})
    @Feature("PGP-35827")
    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Test(description = "Verify that Subscription is created for 0.00 txn amt and pay mode is blank and user selects Wallet on the cashier page")
    public void verifySubsFor0TxnAmtWithTwoDecimalPlacePayModeBlankAndPaymentThroughPPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPICONSENT);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        WalletHelpers.modifyBalance(user, 10.0);

        String txnAmount = "0.00";
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(txnAmount)
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        // Payment will be done of 1 rs in case of Wallet even though subscription amount is 0 rs
        responsePage.validateTxnAmount("1.0")
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName(Constants.PayMode.SUBS_PPBL_MID.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Parameters({"theme"})
    @Feature("PGP-35827")
    @Owner(Constants.Owner.ABHISHEK_TEWARI)
    @Test(description = "Verify that Subscription is created for 0.0 txn amt and pay mode is blank and user selects Wallet on the cashier page")
    public void verifySubsFor0TxnAmtWithOneDecimalPlacePayModeBlankAndPaymentThroughPPI(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.NOPPBL);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        WalletHelpers.modifyBalance(user, 10.0);

        String txnAmount = "0.0";
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(txnAmount)
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setAccountNumber("")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        // Payment will be done of 1 rs in case of Wallet even though subscription amount is 0 rs
        responsePage.validateTxnAmount("1.0")
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateRespMsg("Txn Success")
                .validateGatewayName(Constants.PayMode.SUBS_PPBL_MID.toString())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }
    @Owner("AJEESH")
    @Feature("PGP-36086")
    @Parameters({"theme"})
    @Test(description = "Verify that User can create Subs for Amount Type FIX,Upfront Amount=Zero and Future dated Start date")
    public void PGP_36086_VerifySubscriptioniscreatedforZero(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String TxnMaxAmount = "20";
        String SubscriptionPurpose = "Loan Payments";
        OrderDTO orderDTO = new OrderFactory.BankMandate(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme, user)
                .setSUBS_PAYMENT_MODE("BANK_MANDATE")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("0")
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().plusDays(5L).toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setACCOUNT_TYPE("Savings")
                .setAccountNumber("915445500424")
                .setBANK_IFSC("HDFC0009386")
                .setUSER_NAME("Ajeesh Nair")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();
        cashierPage.proceedBtn().click();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("BANK_MANDATE")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBL")
                .validateMandateType("E_MANDATE")
                .validateCheckSum(Constants.MerchantType.BANK_MANDATE.getKey())
                .assertAll();
    }
    @Owner("AJEESH")
    @Feature("PGP-36086")
    @Parameters({"theme"})
    @Test(description = "Verify that User can't create Subs for Amount Type FIX,Upfront Amount 0<TxnAmount<MaxAmount and Future dated Start date")
    public void PGP_36086_VerifySubscriptionisnotcreatedforNonZero(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String TxnMaxAmount = "20";
        String SubscriptionPurpose = "Loan Payments";
        OrderDTO orderDTO = new OrderFactory.BankMandate(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme, user)
                .setSUBS_PAYMENT_MODE("BANK_MANDATE")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("10")
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().plusDays(5L).toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setACCOUNT_TYPE("Savings")
                .setAccountNumber("915445500424")
                .setBANK_IFSC("HDFC0009386")
                .setUSER_NAME("Ajeesh Nair")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("2022")
                .validateRespMsg("Paymode selected is not applicable when txn amount is less than the renewal amount")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(Constants.MerchantType.BANK_MANDATE.getKey())
                .assertAll();
    }
    @Owner("AJEESH")
    @Feature("PGP-36086")
    @Parameters({"theme"})
    @Test(description = "Verify that User can create Subs for Amount Type FIX,Upfront Amount =MaxAmount and Future dated Start date")
    public void PGP_36086_VerifySubscriptioniscreatedforMaxAmount(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        String TxnMaxAmount = "20";
        String SubscriptionPurpose = "Loan Payments";
        OrderDTO orderDTO = new OrderFactory.BankMandate(Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL, theme, user)
                .setSUBS_PAYMENT_MODE("BANK_MANDATE")
                .setCHANNEL_ID("WEB")
                .setREQUEST_TYPE("SUBSCRIBE")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT(TxnMaxAmount)
                .setSUBS_AMOUNT_TYPE("FIX")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().plusDays(5L).toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setACCOUNT_TYPE("Savings")
                .setAccountNumber("915445500424")
                .setBANK_IFSC("HDFC0009386")
                .setUSER_NAME("Ajeesh Nair")
                .setSubscriptionPurpose(SubscriptionPurpose)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();
        cashierPage.proceedBtn().click();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("BANK_MANDATE")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBL")
                .validateMandateType("E_MANDATE")
                .validateCheckSum(Constants.MerchantType.BANK_MANDATE.getKey())
                .assertAll();
    }

    @Owner(Constants.Owner.Amanpreet)
    @Feature("PGP-39609")
    @Parameters({"theme"})
    @Test(description = "Verify the bankName and gateway in subscription notify bean for bank mandate renew subscription")
    public void TC_01_BankMandateSubsRenewPPP(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        String TxnMaxAmount = "10";

        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("")
                .setSUBS_FREQUENCY_UNIT("ONDEMAND")
                .setTXN_AMOUNT("2")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("0")
                .setAccountNumber("915445500424")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .setACCOUNT_TYPE("SAVINGS")
                .setBANK_IFSC("PYTM0000001")
                .setBANK_CODE("PPBL")
                .build();

        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();


        if(theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.ENHANCED_WEB_REVAMP))
        {cashierPage.proceedBtn();}
        else{ cashierPage.buttonPGPayNow().click();}
        BankMandatePage bankMandatePage = new BankMandatePage();

        bankMandatePage.payToSubscribe().click();
        bankMandatePage.payToSubscribe().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(orderDTO.getORDER_ID())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String SubsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);
        String orderId = orderDTO.getORDER_ID();


        JsonPath jsonPath = new SubsMandateCallback(SubsId, "ACTIVE").execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("resultInfo.code"))
                .as("resultcode mismtach")
                .isEqualToIgnoringCase("3006");

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, orderDTO.getORDER_ID())
                        .equalsIgnoreCase("ACTIVE")));

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", SubsId, orderDTO.getORDER_ID()))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("ACTIVE");

        //InstaProxyLogsExtsno

        String grepEsn = "grep \"" + orderId + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        JsonPath jsonPath1 = new BankMandatePaymentResponse(extSnValue).execute().jsonPath();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, TxnMaxAmount)
                .setMerchantKey(merchant.getKey())
                .build();


        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, orderDTO.getORDER_ID())).isEqualTo("ACTIVE"));


        //InstaProxyLogsExtsno
        String grepEsn1 = "grep \"" + renewSubscriptionDTO.getBody().getOrderId() + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn1 = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn1);
        String extSnValue1 = extSn1.substring(extSn1.indexOf("ExtSN="), extSn1.indexOf(", OrderId=")).replace("ExtSN=", "");
        JsonPath jsonPath2 = new BankMandatePaymentResponse(extSnValue1).execute().jsonPath();

        //PPP logs verify
        Thread.sleep(2000);

        String SubsMerchantNotifyBean = "grep \"" + SubsId + "\"  /paytm/logs/paymentPostProcessor.log |grep \"SubscriptionMerchantNotifyBean\"";
        String subsBean = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENTPOSTPROCESSOR, SubsMerchantNotifyBean);
        String gateway = subsBean.substring(subsBean.indexOf("gateway="), subsBean.indexOf(", txnDate=")).replace("gateway=", "");
        String bankname = subsBean.substring(subsBean.indexOf("bankName="), subsBean.indexOf(", gateway=")).replace("bankName=", "");
        Assert.assertEquals(gateway,"PPBL");
        Assert.assertEquals(bankname,"Paytm Payments Bank");
    }
    @Parameters({"theme"})
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Feature("PGP-39935")
    @Test(description = "Verify Bank Mandate payment text ")
    public void PGP_39935_verifyBankMandateText(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SUBS_BANK_MANDATE_MID;
        String TxnMaxAmount = "10";
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchantType, theme, user)
                .setCHANNEL_ID("WEB")
                .setBANK_CODE("PPBL")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("1")
                .setSUBS_MAX_AMOUNT(TxnMaxAmount)
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.BANK_MANDATE);
        BankMandatePage bankMandatePage = new BankMandatePage();
        String bankMandateText = bankMandatePage.bankMandateText().getText();
        Assertions.assertThat(bankMandateText).contains("*Actual amount may vary as per the bill");
    }
    @Parameters({"theme"})
    @Feature("PGP-41837")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify Bank mandate enhanced flow text changes")
    public void PGP_41837_TC01_BankMandateEnhance(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("10")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.BankMandateRadioButton().click();
        String bankHeading = cashierPage.selectBankHeading().getText();
        Assertions.assertThat(bankHeading).contains("Select your Bank");
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.waitUntilLoads();
        cashierPage.proceedBtn().click();
        BankMandatePage bankMandatePage = new BankMandatePage();
        String dcAdvisoryText = bankMandatePage.bankMandateAdvisoryText().getText();
        Assertions.assertThat(dcAdvisoryText.equals("Please make sure you have Debit Card details available for authentication")).isTrue();
        String amountDebited = bankMandatePage.debitAmountText().getText();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(amountDebited.equals("Amount will be deducted within 2-4 days")).isTrue();
    }

    @Parameters({"theme"})
    @Feature("PGP-41837")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify enhanced flow text changes when Auto Refund is True")
    public void TCPGP_41837_AutoRefundisTrueEnhnaceTexts(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        String upiCTA = cashierPage.payText().getText();
        Assertions.assertThat(upiCTA).contains("Rs" + "1 will be deducted now for account verification & refunded within 2-4 days");
        cashierPage.feedbackCrossBtn().click();
        cashierPage.tabCreditCard().click();
        String cardCTA = cashierPage.payText().getText();
        Assertions.assertThat(cardCTA).contains("Rs" + "1 will be deducted now for account verification & refunded within 2-4 days");
        cashierPage.feedbackCrossBtn().click();
        cashierPage.tabBankMandate().click();
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.proceedBtn().click();
        BankMandatePage bankMandatePage = new BankMandatePage();

    }
    @Parameters({"theme"})
    @Feature("PGP-41837")
    @Owner(Constants.Owner.VISHNU_SHEKAR)
    @Test(description = "Verify enhanced flow text changes when Auto Refund is False")
    public void TCPGP_41837_AutoRefundisFalseEnhnaceTexts(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBSCRIPTION_PG2_LATEST_ALL;
        OrderDTO orderDTO = new OrderFactory.BankMandate(merchant, theme, user)
                .setCHANNEL_ID("WEB")
                .setSUBS_FREQUENCY_UNIT("MONTH")
                .setTXN_AMOUNT("0")
                .setSUBS_MAX_AMOUNT("100")
                .setSUBS_START_DATE(CommonHelpers.getDate().toString())
                .setSUBS_GRACE_DAYS("0")
                .setSUBS_FREQUENCY("1")
                .setSUBS_AMOUNT_TYPE("VARIABLE")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        String upiCTA = cashierPage.payText().getText();
        Assertions.assertThat(upiCTA).contains("Rs1 will be deducted now for account verification");
    }
}
