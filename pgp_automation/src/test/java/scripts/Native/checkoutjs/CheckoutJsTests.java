package scripts.Native.checkoutjs;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.CreateToken;
import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.*;
import com.paytm.api.nativeAPI.*;
import com.paytm.api.theia.FetchQRPaymentDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchQRPaymentDetailsDTO.FetchQRPaymentDetailsDTO;
import com.paytm.dto.NativeDTO.InitTxn.*;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.RenewSubscription.RenewSubscriptionDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.checkoutjs.UserDetail;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.core.DriverManager;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.ui.base.page.BasePage;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.framework.ui.element.UIElements;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.*;
import com.paytm.utils.merchant.GiftVoucher;
import com.paytm.utils.merchant.intersections.MerchantUserIntersection;
import com.paytm.utils.merchant.merchant.util.Merchant;
import com.paytm.utils.merchant.merchant.util.Promo;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;

import static java.lang.Thread.sleep;
import static scripts.UI.EnhancedUITests.validatingUIForQRFlow;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;

import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.io.IOException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.paytm.appconstants.Constants.MerchantType.*;
import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static org.awaitility.Awaitility.await;

@Owner("Jai")
@Owners(author = "Jai", qa = "Ankur")
public class CheckoutJsTests extends CheckoutJsBase {

    private final static int INSTANT_DISCOUNT_PERCENTAGE = 5;
    private final static String PG_DISCOUNT_PROMO_CODE = "discount";
    private static final String HEADER_COLOR = "#BBE6E4";
    private static final String HEADER_BACKGROUND_COLOR = "#A69ABF";
    private static final String BODY_COLOR_CODE = "#D51262";
    private static final String BODY_BACKGROUND_COLOR = "#DAF7A6";
    private static final String THEME_BACKGOUND_COLOR = "#0EE207";
    private static final String THEME_COLOR = "#21045D";
    private static final String ERROR_COLOR = "#B20A75";
    private static final String SUCCESS_COLOR = "#076863";
    private static final String CARD_BACKGROUND_COLOR = "#8CA9A8";
    private static final String NB_LABEL = "MY Internet Banking";
    private static final String UPI_LABEL = "UPI";
    private static final String CC_DC_LABEL = "My CC AND DC CARDS";
    private static final String ADDnPAY_LIMIT_MSG = "You can add upto Rs. 4,000 per month using Credit Card in your Paytm Wallet. Please use other payment option to proceed.";
    private String condition = "\"{request -> request.getParameter('cust-id')=='{CUST_ID}'}\"";
    private final RiskVerificationPage riskVerificationPage = new RiskVerificationPage();
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


    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();
    @Test(description = "Verify color code of sections displayed in cashier page when rendered using checkoutjs flow")
    public void t1() throws Exception {
        String theme = "checkoutjs_web_revamp";
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        WalletHelpers.modifyBalance(user, 5.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.style.setHeaderColor(HEADER_COLOR)
                .setHeaderBackgroundColor(HEADER_BACKGROUND_COLOR)
                .setBodyColor(BODY_COLOR_CODE)
                .setBodyBackgroundColor(BODY_BACKGROUND_COLOR)
                .setThemeBackgroundColor(THEME_BACKGOUND_COLOR)
                .setThemeColor(THEME_COLOR)
                .setErrorColor(ERROR_COLOR)
                .setSuccessColor(SUCCESS_COLOR);
        config.merchant.setHidePaytmBranding(false);
        config.payMode.labels.setAdditionalProperty("NB", NB_LABEL);
        config.payMode.labels.setAdditionalProperty("UPI", UPI_LABEL);
        config.payMode.labels.setAdditionalProperty("CARD", CC_DC_LABEL);
        config.payMode.order = Arrays.asList("UPI", "NB", "CARD");

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        String headerBck_color = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        String headerTxtColor = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-header-color')]", theme).getCssValue("color"));
        String bodyBck_color = getColorCode(getElementByXpath("//*[contains(@class, 'body-bg')]", theme).getCssValue("background-color"));
        String bodyColor = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-paytxtlbl')]", theme).getCssValue("color"));
        String themeBck_color = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-custom-btn')]", theme).getCssValue("background-color"));
        String themeColor = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-custom-btn')]", theme).getCssValue("color"));

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(headerBck_color).isEqualToIgnoringCase(HEADER_BACKGROUND_COLOR);
        softly.assertThat(headerTxtColor).isEqualToIgnoringCase(HEADER_COLOR);
        softly.assertThat(bodyBck_color).isEqualToIgnoringCase(BODY_BACKGROUND_COLOR);
        softly.assertThat(bodyColor).isEqualToIgnoringCase(BODY_COLOR_CODE);
        softly.assertThat(themeBck_color).isEqualToIgnoringCase(THEME_BACKGOUND_COLOR);
        softly.assertThat(themeColor).isEqualToIgnoringCase(THEME_COLOR);

        PaymentDTO p = new PaymentDTO().setExpYear("2019");
        cashierPage.fillAndSubmitCCDetails(p, false);
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        String error_color = getColorCode(cashierPage.error_invalidExpiryDate().getCssValue("color"));
        softly.assertThat(error_color).isEqualToIgnoringCase(ERROR_COLOR);
        DriverManager.getDriver().switchTo().defaultContent();

        List<String> paymodeName = new ArrayList<>();
        for (UIElement e : getElementsByXpath("//*[contains(@class, 'ptm-paymode-name')]", theme)) {
            paymodeName.add(e.getText());
        }
        softly.assertThat(paymodeName).contains(NB_LABEL, UPI_LABEL);

        softly.assertAll();
    }

    @Test(description = "Verify orientation of paymodes displayed in cashier page when rendered using checkoutjs flow")
    public void t2() throws Exception {
        String theme = "checkoutjs_web_revamp";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.payMode.labels.setAdditionalProperty("NB", NB_LABEL);
        config.payMode.labels.setAdditionalProperty("UPI", UPI_LABEL);
        config.payMode.labels.setAdditionalProperty("CARD", CC_DC_LABEL);
        config.payMode.order = Arrays.asList("UPI", "NB", "CARD");

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        SoftAssertions softly = new SoftAssertions();

        List<String> paymodeName = new ArrayList<>();
        for (UIElement e : getElementsByXpath("//*[contains(@class, 'ptm-paymode-name')]", theme)) {
            paymodeName.add(e.getText());
        }
        softly.assertThat(paymodeName).containsExactly("UPI", NB_LABEL, CC_DC_LABEL, "EMI");
        softly.assertAll();

    }

    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Test(description = "Validate the EMI paymode on Checkout JS cashier page.")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC10(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = EMISubvention;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().assertVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate that on Selection of EMI paymode, acquiring of EMI banks list should be displayed on Cashier page")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC11(@Optional("checkoutjs_wap_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.viewAllEmiButton().assertVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate that on selection of HDFC EMI bank, fetchEMiDetail API should be called with response list of EMI plans should be displayed")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC12(@Optional("checkoutjs_wap_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        cashierPage.waitUntilAllAJAXCallsFinish();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.emiPlanCard().waitUntilVisible();
        cashierPage.emiPlanCard().assertVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate on entering incorrect card number, error message should be displayed")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC13(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.INVALID_CARD);
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        cashierPage.waitUntilAllAJAXCallsFinish();
        cashierPage.waitUntilLoads();
        cashierPage.getErrorMessageAfterEnteringCard();


    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate that when user enters valid card number of anyother bank for which EMI is not supoorted the proper error validation should be handled on UI")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC14(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.DC);
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getEmiCard());
        cashierPage.getErrorCC_EMI_NOTSET().waitUntilVisible();
        cashierPage.getErrorCC_EMI_NOTSET().assertVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "validate that on entering valid card number its selected EMI plans list should be displayed.")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC15(@Optional("checkoutjs_wap_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getEmiCard());
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.emiPlanCard().waitUntilVisible();
        cashierPage.emiPlanCard().assertVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate that when on any EMI bank plan there is a condition on Min amount range, then only EMI bank plan should be displayed")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC16_01(@Optional("checkoutjs_web") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = EMISubvention;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("150000001")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().assertNotVisible();
    }

    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate that when on any EMI bank plan there is a condition on Max amount range, then only EMI bank plan should be displayed")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC16_02(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = EMISubvention;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("150000001")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);

        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().assertNotVisible();

    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate the EMI paymode txn using HDFC card.")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC18(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
    }

//    @Feature("PGP-29543")
//    @Epic(Constants.Sprint.SPRINT32_2)
//    @Owner(Constants.Owner.SHUBHAM)
//    @Parameters({"theme"})
//    @Test(description = "Validate that user should be able to save EMI card details as saved instruments.",enabled = false)
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC19(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());


        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.selectEMIBank(paymentDTO);
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.pause(2);
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
       cashierPage.emiPlanCard().waitUntilVisible();
        cashierPage.buttonSecureSignIn().click();
        if(theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.proceedBtn().click();

        }
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear().substring(2));

        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        if (!cashierPage.saveCardForUser().isChecked()) {
            cashierPage.saveCardForUser().click();
        }

        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();

        cashierPage.pause(4);
        InitTxnDTO initTxnDTO2 = new InitTxnDTO.Builder(null, merchantType)
                .setCustId(initTxnDTO.getBody().getUserInfo().getCustId())
                .build();

        String txnToken2 = NativeHelpers.Validate_InitTxn(initTxnDTO2);
        MerchantConfig config2 = checkoutPage.loadMerchantConfig(initTxnDTO2, theme);
        config2.data.setToken(txnToken2);
        checkoutPage.createCheckoutJsOrder(config2);
        cashierPage.save_card_visible().assertVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate the EMI paymode txn using ICICI card.")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC20_ICICI(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = EMI_DC_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate the EMI paymode txn using Amex card.")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC20_Amex(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(paymentDTO.AMEX_CARD_NUMBER).setBankName("AMEX");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.AMEX.toString())
                .validateBankName(Constants.Bank.AMEX.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate validations on Expiry date and CVV")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC21_invalidExpiryDate(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        cashierPage.pause(2);
        cashierPage.fillExpiryMonth("11");
        cashierPage.fillExpiryYear("2011");
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        Thread.sleep(10000);
        cashierPage.error_invalidExpiryDate().assertVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate validations on Expiry date and CVV")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC21_invalidCvv(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                        .setTxnValue("100")
                        .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);

        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().defaultContent();
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP)) {
            cashierPage.proceedToConvertEMI().click();
        }
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().waitUntilEditable();
        cashierPage.textBoxExpiryMonthEMI().clearAndType("11");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");

        cashierPage.textBoxCVVNumber().clearAndType("");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.getError_invalidCVV().assertVisible();


    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.SHUBHAM)
    @Parameters({"theme"})
    @Test(description = "Validate EMI Paymode colors on changing colors on JS configuration")
    public void PGP_29543_ValidateSuccessTxnUsingEMI_TC22(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);

        config.data.setToken(txnToken);
        config.style.setThemeBackgroundColor("#33A5FF");
        config.style.setThemeColor("#A533FF");
        config.style.setHeaderColor("#FF3371");
        config.style.setBodyColor("#52FF33");
        config.style.setBodyBackgroundColor("#FFFC33");
        config.style.setHeaderBackgroundColor("#FF33E9");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        SoftAssertions softly = new SoftAssertions();
        PaymentDTO paymentDTO =new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        String headerBck_color = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        String headerTxtColor = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-name-txt ptm-header-color')]", theme).getCssValue("color"));
        String bodyBck_color = getColorCode(getElementByXpath("//*[contains(@class, 'body-bg')]", theme).getCssValue("background-color"));
        String bodyColor = getColorCode(getElementByXpath("//*[contains(text(),'Select an option to pay')]", theme).getCssValue("color"));
        
        cashierPage.scrollToElement(cashierPage.tabEMI());
//        scrollTo(tabEMI());
        cashierPage.tabEMI().click();

        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.waitUntilLoads();
        cashierPage.proceedToSelectEmiPlan().waitUntilVisible();
        cashierPage.proceedToSelectEmiPlan().click();
        //emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
       // cashierPage.buttonPGPayNow().click();

        String themeBck_color = getColorCode(getElementByXpath("//button[contains(text(),'Pay')]", theme).getCssValue("background-color"));
        String themeColor = getColorCode(getElementByXpath("//button[contains(text(),'Pay')]", theme).getCssValue("color"));
        softly.assertThat(headerBck_color).isEqualToIgnoringCase("#FF33E9");
        softly.assertThat(headerTxtColor).isEqualToIgnoringCase("#FF3371");
        softly.assertThat(bodyBck_color).isEqualToIgnoringCase("FFFC33");
        softly.assertThat(bodyColor).isEqualToIgnoringCase("#52FF33");
        softly.assertThat(themeBck_color).isEqualToIgnoringCase("33A5FF");
        softly.assertThat(themeColor).isEqualToIgnoringCase("#A533FF");

        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        String bodyColor3 = getColorCode(getElementByXpath("//input[@id='cardnumber']", theme).getCssValue("color"));
        softly.assertThat(bodyColor3).isEqualToIgnoringCase("#52FF33");
        cashierPage.pause(2);
        DriverManager.getDriver().switchTo().defaultContent();
        String bodyBck_color3 = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-overlay-container')]", theme).getCssValue("background-color"));
        softly.assertThat(bodyBck_color3).isEqualToIgnoringCase("FFFC33");
    }

    @Owner(RAJKUMAR)
    @Feature("PGP-35515")
    @Parameters({"theme"})
    @Test(description = "Verify the upi text and verify auto suggestion of upi handle")
    public void VerifyUpiChanges(@Optional("checkoutjs_wap_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.pause(3);
        cashierPage.textBoxVPA().clearAndType("rajkumar@");
        cashierPage.pause(3);
        Assertions.assertThat(cashierPage.getUpiText().getText()).isEqualTo("Enter UPI ID");
        Assertions.assertThat(cashierPage.getUpiHandleSuggestion().getText()).isEqualTo("@paytm");


    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn using Checkout js flow")
    public void ValidateSuccessTxnUsingCC(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull DC txn using Checkout js flow")
    public void ValidateSuccessTxnUsingDC(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.EMI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.EMI.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull Wallet txn using Checkout js flow on cashier page login")
    public void ValidateSuccessTxnUsingWallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_COMMON_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

//    @Parameters({"theme"})
//    @Test(description = "Verify successfull Wallet txn using Checkout js flow with SSO Token", enabled = false)
    public void ValidateSuccessTxnUsingWalletviaSSOtoken(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_WALLET_ONLY)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(Constants.MerchantType.NATIVE_WALLET_ONLY.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI txn using Checkout js flow")
    public void ValidateSuccessTxnUsingUPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateVPA("9999661503@paytm")
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull NB txn using Checkout js flow")
    public void ValidateSuccessTxnUsingNB(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NATIVE_HYBRID)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(Constants.MerchantType.NATIVE_HYBRID.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy Config Name of Merchant should get display on cashier page")
    public void ValidateConfigName(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddMoney)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        String configName = config.merchant.getName();

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String nameFromUI = cashierPage.merchantName().getText();
        Assertions.assertThat(nameFromUI).describedAs("Config name doesn't match").isEqualTo(configName);
    }




    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy Brand Name and Business Name  of Merchant should get display on cashier page", dataProvider = "getMID")
    public void ValidateBrandAndBusinessName(Constants.MerchantType mid, String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String brandNameFromAPI = fetchPaymentOptionsJson.getString("body.merchantDetails.merchantDisplayName");
        String businessNameFromAPI = fetchPaymentOptionsJson.getString("body.merchantDetails.merchantName");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String nameFromUI = cashierPage.merchantName().getText();
        if (brandNameFromAPI == null) {
            Assertions.assertThat(nameFromUI).describedAs("Business name doesn't match").isEqualTo(businessNameFromAPI);
        } else {
            Assertions.assertThat(nameFromUI).describedAs("Brand name doesn't match").isEqualTo(brandNameFromAPI);
        }
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy Brand and Business Name using Checkout js flow on cashier page login")
    public void ValidateBrandAndBusinessNameAfterLogin(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
         Constants.MerchantType mid= EMI_DISCOVERY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid)
                .build();
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        // WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String brandNameFromAPI = fetchPaymentOptionsJson.getString("body.merchantDetails.merchantDisplayName");
        String businessNameFromAPI = fetchPaymentOptionsJson.getString("body.merchantDetails.merchantName");

        String nameBeforeLogin = cashierPage.merchantName().getText();
        cashierPage.signin("8512005349","888888");
        String nameAfterLogin = cashierPage.merchantName().getText();

        SoftAssert softly = new SoftAssert();

        if (brandNameFromAPI == null) {
            softly.assertEquals(businessNameFromAPI, nameBeforeLogin, "Business name doesn't match");
        } else {
            softly.assertEquals(brandNameFromAPI, nameBeforeLogin, "Brand name doesn't match");
        }
        softly.assertEquals(nameBeforeLogin, nameAfterLogin, "Name after login doesn't match");
        softly.assertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Test(description = "Verfiy Brand Name  of Merchant should get display on cashier page")
    public void ValidateBrandAndBusinessNameForRetryTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {

        Constants.MerchantType mid = Constants.MerchantType.LOGIN_STRIP;
        User user = userManager.getForWrite(Label.BASIC);
//        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue("99.99")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.merchant.setMid(initTxnDTO.getBody().getMid())
                .setName(null);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String brandNameFromAPI = fetchPaymentOptionsJson.getString("body.merchantDetails.merchantDisplayName");
        String businessNameFromAPI = fetchPaymentOptionsJson.getString("body.merchantDetails.merchantName");

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String nameBeforeRetry = cashierPage.merchantName().getText();
        SoftAssert softly = new SoftAssert();

        if (brandNameFromAPI == null) {
            softly.assertEquals(businessNameFromAPI, nameBeforeRetry, "Business name doesn't match");
        } else {
            softly.assertEquals(brandNameFromAPI, nameBeforeRetry, "Brand name doesn't match");
        }
        cashierPage.payBy(Constants.PayMode.CC_WITH_SINGLE_PAYMODE);
        cashierPage.waitUntilLoads();
        cashierPage.clickRetryIncorrectOTPBtn();
//        cashierPage.clickRetryBtn().click();
        //cashierPage.scrollUpToHeader();
        // cashierPage.closeCcDcDetailBtn().click();
        String nameAfterRetry = cashierPage.merchantName().getText();
        softly.assertEquals(nameBeforeRetry, nameAfterRetry, "Name after Login doesn't match");
        softly.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC Saved Card txn using Checkout js flow")
    public void ValidateSuccessTxnUsingCCSavedCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                .validateCheckSum(Constants.MerchantType.NATIVE_HYBRID.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
    }

    @Owner(MAYURI)
    @Feature("PGPUI-1901")
    @Parameters({"theme"})
    @Test(description = "Verify pending txn when merchant config contains isTimerRequired=true and cancelPendingOrder=true ")
    public void ValidatePendingTxnWithTimerAndCloseOrder(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.modifyBalance(user, 0.0);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_Retry)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setIsTimerRequired(true);
        config.merchant.setCancelPendingOrder(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();

        if(cashierPage.validateProcessingYourPaymentHeadText()==true)
        {
            cashierPage.validateProcessingYourPaymentMsg();
        }
        cashierPage.pause(5);
        cashierPage.getPendingTxnTimer().waitUntilVisible();
        cashierPage.validateProcessingYourPaymentHead();
        cashierPage.getPendingTxnTimer().assertVisible();
        cashierPage.validatePendingTxnMsg();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("141")
                .validateRespMsg("User has not completed transaction.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.Hybrid_Retry.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateTxnDate(new Date())
                .validateRefundAmnt("0.0")
                .validateTxnType("SALE")
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Feature("PGPUI-1901")
    @Parameters({"theme"})
    @Test(description = "Verify pending txn when merchant config contains isTimerRequired=false and cancelPendingOrder=true ")
    public void ValidatePendingTxnWithoutTimerAndWithCloseOrder(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.modifyBalance(user, 0.0);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_Retry)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setIsTimerRequired(false);
        config.merchant.setCancelPendingOrder(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();

        if(cashierPage.validateProcessingYourPaymentHeadText()==true)
        {
            cashierPage.validateProcessingYourPaymentMsg();
        }
        cashierPage.getPendingTxnTimer().assertNotVisible();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("141")
                .validateRespMsg("User has not completed transaction.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.Hybrid_Retry.getKey())
                .validateResponsePageParameters()
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateTxnDate(new Date())
                .validateRefundAmnt("0.0")
                .validateTxnType("SALE")
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Feature("PGPUI-1901")
    @Parameters({"theme"})
    @Test(description = "Verify pending txn when merchant config contains isTimerRequired=true and cancelPendingOrder=false ")
    public void ValidatePendingTxnWithTimerAndWithoutCloseOrder(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.modifyBalance(user, 0.0);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_Retry)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setIsTimerRequired(true);
        config.merchant.setCancelPendingOrder(false);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();

        if(cashierPage.validateProcessingYourPaymentHeadText()==true)
        {
            cashierPage.validateProcessingYourPaymentMsg();
        }
        cashierPage.getPendingTxnTimer().waitUntilVisible();
        cashierPage.validateProcessingYourPaymentHead();
        cashierPage.getPendingTxnTimer().assertVisible();
        cashierPage.validatePendingTxnMsg();
        DriverManager.setWebDriverElementWait(java.time.Duration.ofSeconds(200));

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("402")
                .validateRespMsg("We are processing your transaction.")
                .validateStatus("PENDING")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.Hybrid_Retry.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(MAYURI)
    @Feature("PGPUI-1901")
    @Parameters({"theme"})
    @Test(description = "Verify pending txn when merchant config contains isTimerRequired=false and cancelPendingOrder=false ")
    public void ValidatePendingTxnWithoutTimerAndWithoutCloseOrder(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.modifyBalance(user, 0.0);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_Retry)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setIsTimerRequired(false);
        config.merchant.setCancelPendingOrder(false);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();

        if(cashierPage.validateProcessingYourPaymentHeadText()==true)
        {
            cashierPage.validateProcessingYourPaymentMsg();
        }
        cashierPage.getPendingTxnTimer().assertNotVisible();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("402")
                .validateRespMsg("We are processing your transaction.")
                .validateStatus("PENDING")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.Hybrid_Retry.getKey())
                .validateResponsePageParameters()
                .assertAll();

    }

    @Owner(MAYURI)
    @Feature("PGPUI-1901")
    @Parameters({"theme"})
    @Test(description = "Verify pending txn when merchant config contains isTimerRequired=true and cancelPendingOrder=true and redirect=false ")
    public void ValidatePendingTxnWithTimerAndCloseOrderRedirectFalse(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        WalletHelpers.modifyBalance(user, 0.0);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_Retry)
                .setTxnValue("99.84")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setRedirect(Boolean.FALSE);
        config.merchant.setIsTimerRequired(true);
        config.merchant.setCancelPendingOrder(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();

        if(cashierPage.validateProcessingYourPaymentHeadText()==true)
        {
            cashierPage.validateProcessingYourPaymentMsg();
        }
        cashierPage.getPendingTxnTimer().waitUntilVisible();
        cashierPage.validateProcessingYourPaymentHead();
        cashierPage.getPendingTxnTimer().assertVisible();
        cashierPage.validatePendingTxnMsg();
        DriverManager.setWebDriverElementWait(java.time.Duration.ofSeconds(200));
        riskVerificationPage.clickAlert();
    }
    @Owner(MAYURI)
    @Parameters({"theme"})
    @Feature("PGPUI-1319")
    @Test(description = "Verfiy Polling page should open in popup in a same page which is invoke for blink checkout and successfull UPI txn using Checkout js flow")
    public void validatePollingPageIsNotInNewWinowAndSuccessTxnUsingUPICollect(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType   merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        Assert.assertFalse(cashierPage.waitForNewWindow(2),"New window is not open");
        cashierPage.tabCheckoutUPIPollingImg().assertVisible();
        ResponsePage responsePage = new ResponsePage();

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Feature("PGPUI-1319")
    @Test(description = "Verfiy Polling page should open in popup in a same page which is invoke for blink checkout and failure UPI txn using Checkout js flow")
    public void validatePollingPageIsNotInNewWinowAndFailTxnUsingUPICollect(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType   merchantType = Constants.MerchantType.HDFC_UPI_COLLECT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.44")
                .build();
        WalletHelpers.modifyBalance(user, 0.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        Assert.assertFalse(cashierPage.waitForNewWindow(2),"New window is not open");
        cashierPage.tabCheckoutUPIPollingImg().assertVisible();
        ResponsePage responsePage = new ResponsePage();

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateVPA(new PaymentDTO().getVpa())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateVPA(new PaymentDTO().getVpa())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Feature("PGPUI-1319")
    @Test(description = "Verfiy Polling page should open in popup in a same page which is invoke for blink checkout and retry UPI txn using Checkout js flow")
    public void validatePollingPageIsNotInNewWinowAndRetryUsingUPICollect(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType   merchantType = Constants.MerchantType.HDFC_UPI_COLLECT_RETRY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.44")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        for(int i=0;i<3;i++){
            cashierPage.payBy(Constants.PayMode.UPI);
            Assert.assertFalse(cashierPage.waitForNewWindow(2),"New window is not open");
            cashierPage.tabCheckoutUPIPollingImg().assertVisible();
            if(i!=2) {
                cashierPage.waitUntilLoads();
                cashierPage.clickRetryIncorrectOTPBtn();
            }
        }
        ResponsePage responsePage = new ResponsePage();

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateVPA(new PaymentDTO().getVpa())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.execute();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateVPA(new PaymentDTO().getVpa())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(MAYURI)
    @Parameters({"theme"})
    @Feature("PGPUI-1319")
    @Test(description = "Verfiy Polling page should open in popup in a same page which is invoke for blink checkout and successfull saved vpa txn using Checkout js flow")
    public void validatePollingPageIsNotInNewWinowAndSuccessTxnUsingSavedVPA(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVEDVPA);
        Constants.MerchantType   merchantType = Constants.MerchantType.PPBLC_ONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, 0.0);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabSavedUPI(1);
        cashierPage.payBy(Constants.PayMode.SAVED_UPI);
        Assert.assertFalse(cashierPage.waitForNewWindow(2),"New window is not open");
        ResponsePage responsePage = new ResponsePage();

        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("PPBLC")
                .validateCheckSum(merchantType.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("PPBLC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull Add N Pay txn using Checkout js flow")
    public void ValidateSuccessTxnAddNPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .setTxnValue("2")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if(!cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().check();
        }
        cashierPage.tabCreditCard().click();
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                //.validateCheckSum(Constants.MerchantType.NATIVE_ADDNPAY.getKey())
                //.validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify Success QR details and Perform Successful txn using CC in Checkout js flow with NativeJsonReqest Pref N")
    public void PGP_27624_SuccessQRTxnUsingCCwithNativeJsonPrefN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        // This FF4J flags is controlled by mid based strategy no need to enable and disable in test case added MID in the Flag
        // FF4JFlags.enable("theia-srv.SHOW.QR.BLINK.CHECKOUT");
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.imgScanPayQRCode().assertVisible();
        cashierPage.pause(2);
        User user = userManager.getForWrite(Label.BASIC);
        String qrCodeID = PGPHelpers.getWalletQRCodeString(cashierPage.imgScanPayQRCode().getAttribute("src"));
        FetchQRPaymentDetailsDTO fetchQRPaymentDetailsDTO = new FetchQRPaymentDetailsDTO.Builder()
                .setQRCodeId(qrCodeID)
                .setMID(merchantType.getId())
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .build();
        FetchQRPaymentDetails fetchQRPaymentDetails = new FetchQRPaymentDetails(fetchQRPaymentDetailsDTO);
        JsonPath fetchQRResponse = fetchQRPaymentDetails.execute().jsonPath();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                merchantType.getId(), "SSO", user.ssoToken(), fetchQRResponse.getString("body.qrInfo.response.ORDER_ID"), fetchQRResponse.getString("body.qrInfo.response.TXN_AMOUNT"))
                .setPaymentMode("CREDIT_CARD")
                .setQRCodeId(qrCodeID)
                .setExtendInfoDynamicFlow()
                .build();

        NativeHelpers.submitProcessTxnResponseFromReq(processTxnV1Request);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateMERC_UNQ_REF("vivek4")
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify successfull Subscription using Checkout js flow Native Json Pref N")
    public void PGP_27624_ValidateSuccessSubscriptionwithNativeJsonPrefN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.UPI);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                //.validateCheckSum(merchant.getKey())
                .assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verfiy Retry breach in Checkout js flow with Merchant having NativeJsonRequest Pref set to F")
    public void PGP_27624_ValidateRetryBreachTxnwithNativeJsonPrefN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PPBL_NB_PCF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        User user = userManager.getForWrite(Label.BASIC);
//        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        SoftAssertions softAssert = new SoftAssertions();
        cashierPage.tabDebitCard().click();
        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateCheckSum(Constants.MerchantType.PPBL_NB_PCF.getKey())
                .assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify Bank offers are visible on cashier page(without SSO token) and not visible after user login")
    public void PGP_27624_PromoTxnwithNativeJsonPrefNMerchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.promoCC);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
    }


    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify Split Settlement Txn with NativeJsonRequest pref N")
    public void PGP_27624_SplitSettlementTxnwithNativeJsonPrefNMerchant(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.SPLIT_SETTLEMENT_PGONLY;
        SplitInfo splitInfo1 = new SplitInfo().setMid("vendorid1").setAmount(new Amount().setValue("5").setPercentage(""));
        SplitInfo splitInfo2 = new SplitInfo().setMid("vendorid2").setAmount(new Amount().setValue("5").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo1, splitInfo2});
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .setTxnValue("10.00")
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull Wallet txn using Checkout js flow on cashier page login")
    public void PGP_27624_ValidateWalletTxnNativeJsonPrefN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        //cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn using Checkout js flow")
    public void PGP_27624_ValidateCCTxnNativeJsonPrefN(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI txn using Checkout js flow")
    public void PGP_27624_ValidateUPITxnNativeJsonPrefN(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for Net Banking in Checkout js flow")
    public void ValidatePCFforNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.FLAT_PCF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setBankName("ICICI");
        cashierPage.tabNetBanking().click();
    //    cashierPage.dropdownNB().selectByValue(paymentDTO.getBankName());
        SoftAssertions softAssert = new SoftAssertions();
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 4.72, "NB");
        cashierPage.pause(2);
        validateCommissionNew(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 4.72, "NB");
        softAssert.assertAll();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(Constants.MerchantType.FLAT_PCF.getKey())
                .validateChargeAmount(Double.toString(expectedChargeFeeAmt))
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Bank.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
    }
    
    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for Wallet Paymode in Checkout js flow")
    public void ValidatePCFwithWallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALLETOnly_PCF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 3.0, "WALLET");
        double expectedTotalAmt = CommonHelpers.doubleHalfUpConvertor(Double.valueOf(initTxnDTO.txnAmountFromBody()) + expectedChargeFeeAmt);
        WalletHelpers.modifyBalance(user, expectedTotalAmt);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.pause(2);
        SoftAssertions softAssert = new SoftAssertions();
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 3.0, "WALLET");
        softAssert.assertAll();
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(Constants.MerchantType.WALLETOnly_PCF.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for Amex Card in Checkout js flow")
    public void ValidatePCFwithAmexCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AMEX_PCF)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        SoftAssertions softAssert = new SoftAssertions();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.AMEX_CARD_NUMBER);
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 1, "CC");
        softAssert.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for HDFC Debit Card in Checkout js flow")
    public void ValidatePCFwithHDFCDebitCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.AUTOLOGIN_MID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        SoftAssertions softAssert = new SoftAssertions();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.tabDebitCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getDebitCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.chargeFeeAmtAtPG().assertVisible();
       // validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 1, 0, "DC");
        softAssert.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verfiy PCF Amount for Retry breach in Checkout js flow(Retry Not allowed in checkoutJS)")
    public void ValidatePCFRetryBreachTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NETBANK_PCF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        double expectedChargeFeeAmt = 0;
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        SoftAssertions softAssert = new SoftAssertions();
        cashierPage.tabDebitCard().click();
//       TODO: need to enter details of card then it will berify the value

        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDetailsForRetry);
        expectedChargeFeeAmt = convenienceFeeCalculator(Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 1, "CC");
        validateCommission(softAssert, cashierPage, Double.valueOf(initTxnDTO.txnAmountFromBody()), 0, 1, "CC");
        softAssert.assertAll();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateCheckSum(merchantType.getKey())
                .validateChargeAmount(Double.toString(expectedChargeFeeAmt))
                .assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify Value of flag isLocalStorageAllowedForLastPayMode in FPO response should be true for Configured Mid in FF4J")
    public void PGP_28965_VerifyisLocalStorageAllowedForLastPayModeisTrueinFPOforConfigredMid() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, "");
        BasePage.executeJavaScript("window.localStorage.clear();");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.isLocalStorageAllowedForLastPayMode")).isTrue();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify Value of flag isLocalStorageAllowedForLastPayMode in FPO response should be false for Not Configured Mid in FF4J")
    //flag is on for all mid
    public void PGP_28965_VerifyisLocalStorageAllowedForLastPayModeisTrueinFPOforMidNotconfigured() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_INTENT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, "");
        BasePage.executeJavaScript("window.localStorage.clear();");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        //v5 fpo is not calling because it is validating via Fpo Api
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.isLocalStorageAllowedForLastPayMode")).isTrue();
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode(NB) already selected after successful when mid is configured on ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedNBTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        BasePage.executeJavaScript("window.localStorage.clear();");
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isTrue();
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode(NB) already selected after successful PCF txn when mid is configured on ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedNBPCFTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NETBANK_PCF)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        BasePage.executeJavaScript("window.localStorage.clear();");
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NETBANK_PCF)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isTrue();
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode(NB) already selected after 3 successful NB txns when mid is configured on ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedafterThreeNBTxn(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        BasePage.executeJavaScript("window.localStorage.clear();");
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        int txnleft = 2;
        while (txnleft > 0) {
            initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                    .build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
            config.data.setToken(txnToken);
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            checkoutPage.createCheckoutJsOrder(config);
            cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_SUCCESS")
                    .assertAll();
            txnleft--;
        }
        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isTrue();
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode(NB) already selected after 2 successful NB txns and CC Txn in last when mid is configured on ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedafterTwoNBTxnandLastCCTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        int txnleft = 2;
        while (txnleft > 0) {
            initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                    .build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
            config.data.setToken(txnToken);
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            checkoutPage.createCheckoutJsOrder(config);
            cashierPage = CashierPageFactory.getCashierPage(theme);
            if (txnleft != 1) {
                cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
            } else {
                cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
            }
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_SUCCESS")
                    .assertAll();
            txnleft--;
        }
        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isTrue();//NB should be selected as it most used ie. 2 out of 3 times
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode(CC) already selected after successful when mid is configured on ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedCCTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode(UPI) already selected after successful when mid is configured on ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedUPITxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        cashierPage.scrollToElement(cashierPage.tabUPI());
        Assertions.assertThat(cashierPage.tabUPI().isSelected()).isTrue();
    }

    // Deprecated since grid UI is being used ref.PGP-28965
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode and wallet are already selected when mid is configured on ff4j in Add N Pay txn")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedAddNPayTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Saved Card Paymode and wallet are already selected when mid is configured on ff4j in Hybrid merchant with Insufficient wallet balance")
    public void PGP_28965_verifySavedCardAlreadySelectedWhenLocalStorageEmptyWalletBalInsufficient(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        BasePage.executeJavaScript("window.localStorage.clear();");
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 0.5);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        Assertions.assertThat(cashierPage.tabSavedCard().isSelected()).isTrue();
    }

    // Deprecated since grid UI is being used ref.PGP-28965
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode is already selected after two NB, one CC AddNPay txns and mid is configured in ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedwithTwoNB_oneCC_AddNPayTxns(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        int txnleft = 2;
        while (txnleft > 0) {
            initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                    .build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
            config.data.setToken(txnToken);
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            checkoutPage.createCheckoutJsOrder(config);
            cashierPage = CashierPageFactory.getCashierPage(theme);
            if (txnleft != 1) {
                cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
            } else {
                cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
            }
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_SUCCESS")
                    .assertAll();
            txnleft--;
        }
        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isTrue();//NB should be selected as it most used ie. 2 out of 3 times
    }

    // Deprecated since grid UI is being used ref.PGP-28965
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode is already selected after two NB, one CC Hybrid txns and mid is configured in ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedwithTwoNB_oneCC_HybridTxns(@Optional("checkoutjs_wap") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        int txnleft = 2;
        while (txnleft > 0) {
            initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                    .build();
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
            config.data.setToken(txnToken);
            WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
            checkoutPage.createCheckoutJsOrder(config);
            cashierPage = CashierPageFactory.getCashierPage(theme);
            if (txnleft != 1) {
                cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
            } else {
                cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
            }
            responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("TXN_SUCCESS")
                    .assertAll();
            txnleft--;
        }
        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isTrue();//NB should be selected as it most used ie. 2 out of 3 times
    }

//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode and wallet are already selected when mid is configured on ff4j in Hybrid txn")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedHybridTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.WALLET);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode are already selected when mid is configured on ff4j in Hybrid txn with zero balance in wallet")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedHybridTxnwithZeroBalanceinWallet(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 0.5);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        WalletHelpers.modifyBalance(user, 0.00);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode(Saved CC) already selected after successful when mid is configured on ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedSavedCCTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        // WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabSavedCard().isSelected()).isTrue();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify that txn is initated with txn amount greater than GV but less than wallet balance" +
            "GV balance < txn amount <wallet balance, then wallet should be selected")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedGVWalletTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MGV_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1));
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) + 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.checkBoxPPI().assertVisible();
    }

    // Since we are using card layout we cannot test this scenario therefore disabling it
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Last Transaction Paymode is selected in Hybrid when first txn is addnpay and  mids are configured in ff4j")
    public void PGP_28965_verifyLastTxnPaymodeAlreadySelectedAddNPayHybridTxn(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Addnpay)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 0.5);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        BasePage.executeJavaScript("window.localStorage.clear();");
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Hybrid_S)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 0.5);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        Assertions.assertThat(cashierPage.tabCreditCard().isSelected()).isTrue();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify Last Transaction Paymode after successful txn when user is different")
    public void PGP_28965_verifyLastTxnPaymodeWhenUserisDifferent(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        BasePage.executeJavaScript("window.localStorage.clear();");
        config.data.setToken(txnToken);
    //    WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();

        user = userManager.getForWrite(Label.PPBL);
        initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
 //       WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        Thread.sleep(5000);
        Assertions.assertThat(cashierPage.tabNetBanking().isSelected()).isFalse();
    }



    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull UPI Subscription using Checkout js flow")
    public void PGP_27014_ValidateSuccessUPISubscription(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.PPBLC.toString())
                .validateCheckSum(Constants.MerchantType.SUBS_UI_TEXT.getKey())
                .assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate auto login for enhanced subscription flow ")
    public void PGP_29696_autologin_checkoutJS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        prerequisite:
        {
            validate_MerchantPreference(Constants.MerchantType.AddnPay.getId(),"PG_AUTOLOGIN_ENABLED", "Y");
        }
        final String COOKIE_NAME = "pg_login";
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (!cashierPage.rememberMeCheckbox().isChecked())
            cashierPage.rememberMeCheckbox().check();
        cashierPage.login(user);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
        checkoutPage.setDeleteCookie(false);

        InitTxnDTO initTxnDTO2 = new InitTxnDTO.Builder(null, merchantType)
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO2.txnAmountFromBody()));
        txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO2);
        config = checkoutPage.loadMerchantConfig(initTxnDTO2, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        cashierPage.pause(2);
        Cookie cookie = DriverManager.getDriver().manage().getCookieNamed(COOKIE_NAME);
        if (null == cookie)
            Assertions.fail("pg_login cookie not found in browser session");
        //Assert user is logged in on cashier page
        cashierPage.checkBoxPPI().isElementPresent();
    }

    @Feature("PGP-29616")
    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn using Checkout js flow having new UDF_2 Params in request")
    public void ValidateSuccessTxnUsingCCWhichNewUDFParam(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String UDF_2 = "checkout_udf2";
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setExtendInfo(new ExtendInfo().setUdf2(UDF_2))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateUDF(UDF_2)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .assertAll();


    }

    @Owner(ESHANI)
    @Test(description = "FF4j flag is enabled & workflow = checkout in FPO; then isHtmlToBeRenderedForBlinkCheckout= true in response")
    public void ValidateHtmlRenderParamWhenWorkflowIsCheckoutandFF4jEnabled() throws Exception {
//        FF4JFlags.enable("theia.checkout.forward.html.render.allowed");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WebviewSupportOnJS).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String workflow = "checkout";

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO
                .Builder("TXN_TOKEN", txnToken)
                .setMid(Constants.MerchantType.WebviewSupportOnJS.getId())
                .setChannelId("WEB")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.setContext("body.orderId", initTxnDTO.orderFromBody())
                .setContext("head.workFlow", workflow);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String paramValue = fetchPaymentOptionsJson.getString("body.isHtmlToBeRenderedForBlinkCheckout");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paramValue).isEqualToIgnoringCase("true");
        softly.assertAll();
    }


//     @Owner(ESHANI)
//     @Test(description = "FF4j flag is disabled & workflow = checkout in FPO; then isHtmlToBeRenderedForBlinkCheckout =false in response" , enabled=false) //FF4J IS ONFOR PRODUCTION ALSO
    public void ValidateHtmlRenderParamWhenWorkflowIsCheckoutandFF4jDisabled() throws Exception {
//        FF4JFlags.disable("theia.checkout.forward.html.render.allowed"); flag is on, so isHtmlToBeRenderedForBlinkCheckout =true
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, PGOnly).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String workflow = "checkout";

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO
                .Builder("TXN_TOKEN", txnToken)
                .setMid(Constants.MerchantType.PGOnly.getId())
                .setChannelId("WEB")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.setContext("body.orderId", initTxnDTO.orderFromBody())
                .setContext("head.workFlow", workflow);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String paramValue = fetchPaymentOptionsJson.getString("body.isHtmlToBeRenderedForBlinkCheckout");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paramValue).isEqualToIgnoringCase("false");
        softly.assertAll();
    }


    @Owner(ESHANI)
    @Test(description = "FF4j flag is enable & workflow != checkout in FPO; then isHtmlToBeRenderedForBlinkCheckout =false in response")
    public void ValidateHtmlRenderParamWhenWorkflowIsNotCheckoutandFF4jEnabled() throws Exception {
//        FF4JFlags.enable("theia.checkout.forward.html.render.allowed");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WebviewSupportOnJS).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String workflow = "notcheckout";

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO
                .Builder("TXN_TOKEN", txnToken)
                .setChannelId("WEB")
                .setMid(Constants.MerchantType.WebviewSupportOnJS.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.setContext("body.orderId", initTxnDTO.orderFromBody())
                .setContext("head.workFlow", workflow);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String paramValue = fetchPaymentOptionsJson.getString("body.isHtmlToBeRenderedForBlinkCheckout");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paramValue).isEqualTo(null);
        softly.assertAll();
    }


    @Owner(ESHANI)
    @Test(description = "FF4j flag is disable & workflow != checkout in FPO; then isHtmlToBeRenderedForBlinkCheckout =false in response")
    public void ValidateHtmlRenderParamWhenWorkflowIsNotCheckoutandFF4jDisabled() throws Exception {
//        FF4JFlags.disable("theia.checkout.forward.html.render.allowed");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String workflow = "notcheckout";

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO
                .Builder("TXN_TOKEN", txnToken)
                .setMid(Constants.MerchantType.PGOnly.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        fetchPaymentOption.setContext("body.orderId", initTxnDTO.orderFromBody())
                .setContext("head.workFlow", workflow);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String paramValue = fetchPaymentOptionsJson.getString("body.isHtmlToBeRenderedForBlinkCheckout");

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(paramValue).isEqualTo(null);
        softly.assertAll();
    }


    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with NB paymode when simplifiedPaymentOffers Discount promo applied")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforNBTxnDiscountPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        int INSTANT_DISCOUNT_PERCENTAGE = 10;

        Constants.MerchantType pwpDefault = Constants.MerchantType.NATIVE_HYBRID;
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("NETBANKING10").setApplyAvailablePromo("false").setValidatePromo("true");        
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", pwpDefault, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode")).isEqualTo("NETBANKING10");        
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.applyAvailablePromo")).isFalse();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.simplifiedPaymentOffers.validatePromo")).isTrue();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.pause(2);
        double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String finalAmt = String.valueOf((int)(discountedAmt));
        Assertions.assertThat(cashierPage.buttonPGPayNow().getText()).isEqualToIgnoringCase("Pay ₹" + finalAmt);
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(String.valueOf(discountedAmt))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with NB paymode when simplifiedPaymentOffers")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforNBTxnSpecificCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().unCheck();
        }
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        cashierPage.tabNetBanking().click();
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        cashierPage.payButtonPromoText().waitUntilVisible();
        cashierPage.validateButtonText(DA, discount);
        //        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with NB paymode when simplifiedPaymentOffers")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforNBTxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("false").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        cashierPage.tabNetBanking().click();
      //  String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
       // cashierPage.payButtonPromoText().waitUntilVisible();
       // cashierPage.validateButtonText(DA, discount);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
//        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
        String promoTxt=new UIElement(By.xpath("//div[contains(@class,'ptm-message ptm-bo-cashtrip ')]"),"cashier-page", "promo text").getText();
        Assertions.assertThat(promoTxt).contains("discount applicable");
        cashierPage.payBy(Constants.PayMode.NB, new PaymentDTO().setBankName("ICICI"));
        Double finalAmount=9.50;
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(finalAmount.toString())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with EMI paymode when simplifiedPaymentOffers,")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforEMITxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        paymentDTO.setBankName("HDFC");
        cashierPage.waitUntilLoads();
        if(cashierPage.checkBoxPPI().isChecked()){
            cashierPage.checkBoxPPI().unCheck();
        }
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.dropdownEmiBanksV5().selectByVisibleText(paymentDTO.getBankName());
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        cashierPage.payButtonPromoText().waitUntilVisible();
        cashierPage.validateButtonText(DA, discount);
        //        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with CC paymode when simplifiedPaymentOffers")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforCCTxnSpecificCashbackPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_COMMON_MERCHANT;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        WalletHelpers.modifyBalance(user, 0.00);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();//span
        cashierPage.payButtonPromoText().waitUntilVisible();
        cashierPage.validateButtonText(DA, discount);
//        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with CC paymode when simplifiedPaymentOffers")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforCCTxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_COMMON_MERCHANT;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        String buttonText = new UIElement(By.xpath("//div[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        cashierPage.payButtonPromoText().waitUntilVisible();
        cashierPage.validateButtonText(DA, discount);
        //        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with UPI paymode when simplifiedPaymentOffers")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforUPITxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        cashierPage.payButtonPromoText().waitUntilVisible();
        cashierPage.validateButtonText(DA, discount);
        //        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate the successful transaction with Saved CC paymode when simplifiedPaymentOffers")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforSavedCCTxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        WalletHelpers.modifyBalance(user, 0.00);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        String DA = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(discountedAmt));
        String discount = CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(String.valueOf(Double.parseDouble(initTxnDTO.txnAmountFromBody()) - discountedAmt));
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        cashierPage.payButtonPromoText().waitUntilVisible();
        cashierPage.validateButtonText(DA, discount);
        //        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹"+DA);
//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ "+DA+" with ₹ "+discount+" cashback)");
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Validate the successful Hybrid transaction with Saved CC when simplifiedPaymentOffers")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforHybridTxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        Double discountedAmt = Double.parseDouble(initTxnDTO.txnAmountFromBody()) * (1.00 - INSTANT_DISCOUNT_PERCENTAGE * 0.01);
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.closeCardPay().click();
        }
        cashierPage.tabSavedCard().click();
        String buttonText = new UIElement(By.xpath("//*[@id='checkout-button']/button"), "cashier-page", "pay-button").getText();
        cashierPage.validateButtonText("0.95", "0.05");
        Assertions.assertThat(buttonText).isEqualTo("Effective price after offer ₹0.95");

//        Assertions.assertThat(buttonText).isEqualTo("(Effective price: ₹ 0.95" + " with ₹ 0.05 cashback)");
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)//non_empty to empty
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate for Add N Pay transaction,  Bank offers should not be applied")
    public void PGP_29542_ValidateBankOffersSimplifiedFlowforAddNPayTxnWithoutSpecificPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        String invalidPromoText = new UIElement(By.xpath("//div[contains(text(),'This transaction requires a valid promocode to proceed.')]"), "cashier-page", "invalid-promo-text").getText();
        Assertions.assertThat(invalidPromoText).isEqualTo("This transaction requires a valid promocode to proceed.");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate for Add N Pay transaction set to true, Bank offers should not be applied, valid discount promo")
    public void PGP_27602_verifyTxnNotProceedingwithPromoWhenValidatePromoisTrueAddNPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        String invalidPromoText = new UIElement(By.xpath("//div[contains(text(),'This transaction requires a valid promocode to proceed.')]"), "cashier-page", "invalid-promo-text").getText();
        Assertions.assertThat(invalidPromoText).isEqualTo("This transaction requires a valid promocode to proceed.");
    }

    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Verify the successful addnpay txn when simplified offers has valid promo code and validate promo flag is false, txn proceeds without promo.")
    public void PGP_27602_verifyTxnProceedswithoutPromoWhenValidatePromoisFalseAddNPay(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_ADDNPAY;
        Promo promo = new Promo();
        for (int i = 0; i < 2; i++) {
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate that MGV paymode should be auto selected and displayed at top on cashier page of Checkout Js")
    public void PGP_29543_verifyMGVPaymodeAlreadySelected(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MGV_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(10.00));
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.MGVradioButton().isSelected();
    }


 //   @Feature("PGP-29543")
//    @Epic(Constants.Sprint.SPRINT32_2)
//    @Owner(Constants.Owner.JAI)
//    @Parameters({"theme"})
//    @Test(description = "Validate the Paymode sorting order w.r.t MGV on Js Configuration", enabled = false)
    //test case invalid as mgv will not be coming selected by default, it depends on paymode sequencing now
    public void PGP_29543_verifyMGVPaymodeSortingOrder(@Optional("checkoutjs_wap") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        Constants.MerchantType merchantType = MGV_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(10.00));
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.payMode.order = Arrays.asList("MGV", "BALANCE", "UPI", "NB", "CARD");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymodeName = new ArrayList<>();
        for (UIElement e : getElementsByXpath("//*[contains(@class, 'ptm-paymode-name')]", theme)) {
            paymodeName.add(e.getText());
        }
        List<String> expectedList = new ArrayList<>();
        expectedList.add("AutomationMerchant001 Voucher");
        expectedList.add("Paytm Balance");
        expectedList.add("BHIM UPI");
        expectedList.add("Net Banking");
        expectedList.add("Debit / Credit Cards");
        Assertions.assertThat(paymodeName).isEqualTo(expectedList);
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate that MGV paymode Color as per configuration and perform a success transaction")
    public void PGP_29543_verifyMGVPaymodeColorandSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MGV_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(10.00));
        double initialBalance = mu.getGiftVouchers().getBalance();
        double finalBalance = initialBalance - Double.parseDouble(initTxnDTO.txnAmountFromBody());
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.style.setHeaderColor(HEADER_COLOR)
                .setHeaderBackgroundColor(HEADER_BACKGROUND_COLOR)
                .setBodyColor(BODY_COLOR_CODE)
                .setBodyBackgroundColor(BODY_BACKGROUND_COLOR)
                .setThemeBackgroundColor(THEME_BACKGOUND_COLOR)
                .setThemeColor(THEME_COLOR)
                .setErrorColor(ERROR_COLOR)
                .setSuccessColor(SUCCESS_COLOR);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.MGVradioButton().click();
        String textColor = getColorCode(getElementByXpath("//div[@id='checkout-mgv']//p[@class='ptm-paymode-name ptm-no-xspop']", theme).getCssValue("color"));
        String themeBck_color = getColorCode(getElementByXpath("//*[contains(@class, 'ptm-custom-btn')]", theme).getCssValue("background-color"));
        String bodyBck_color = getColorCode(getElementByXpath("//*[contains(@class, 'body-bg')]", theme).getCssValue("background-color"));

        SoftAssertions softly = new SoftAssertions();

        softly.assertThat(textColor).isEqualToIgnoringCase(BODY_COLOR_CODE);
        softly.assertThat(themeBck_color).isEqualToIgnoringCase(THEME_BACKGOUND_COLOR);
        softly.assertThat(bodyBck_color).isEqualToIgnoringCase(BODY_BACKGROUND_COLOR);
        softly.assertAll();

        cashierPage.payBy(Constants.PayMode.MGV);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("GIFT_VOUCHER")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        Assertions.assertThat(mu.getGiftVouchers().getBalance()).isEqualTo(finalBalance);
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate that when MGV wallet Balance = 0 , MGV paymode should not be displayed on Cashier Page")
    public void PGP_29543_verifyMGVPaymodeBalanceisZero(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MGV_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("2.00")
                .build();
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        mu.getGiftVouchers().remove(new GiftVoucher(Balance));
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.MGVradioButton().assertNotVisible();
    }

    @Feature("PGP-29543")
    @Epic(Constants.Sprint.SPRINT32_2)
    @Owner(Constants.Owner.JAI)
    @Parameters({"theme"})
    @Test(description = "Validate that when order amount is greater than MGV balance then MGV Paymode should not be auto selected and user cannot even select the paymode for txn.")
    public void PGP_29543_verifyOrderAmtGreaterthanMGVBalance(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = MGV_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("10.00")
                .build();
        com.paytm.utils.merchant.user.User u = new com.paytm.utils.merchant.user.User(user.mobNo(), user.password(), true);
        Merchant m = new Merchant(merchantType.getId(), true);
        MerchantUserIntersection mu = new MerchantUserIntersection(m, u);
        mu.getGiftVouchers().add(new GiftVoucher(1.00));
        double Balance = mu.getGiftVouchers().getBalance();
        mu.getGiftVouchers().remove(new GiftVoucher(Balance - 1));
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        Assertions.assertThat(cashierPage.MGVradioButton().isSelected()).isFalse();
        cashierPage.MGVradioButton().assertNotClickable();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30182")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn using Checkout js flow after retry due to bank page closed by user")
    public void ValidateSuccessTxnUsingCCRetry(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("33.33")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.closeChildWindow();
        cashierPage.retryBtnPopupClosedByUser().waitUntilClickable();
        cashierPage.retryBtnPopupClosedByUser().click();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.successfulTransactionButton();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30182")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull NB txn after CC failed txn when bank page is closed by user")
    public void ValidateSuccessTxnUsingNBAfterCCRetry(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("33.33")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.closeChildWindow();
        cashierPage.retryBtnPopupClosedByUser().waitUntilClickable();
        cashierPage.retryBtnPopupClosedByUser().click();
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.waitUntilLoads();
        cashierPage.successfulTransactionButton();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30182")
    @Parameters({"theme"})
    @Test(description = "Verfiy unsuccessfull txn when bank page is closed by user and there is zero retry allowed")
    public void ValidateZeroRetry(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("33.33")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.closeChildWindow();
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("1006")
                .validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Owner(Constants.Owner.ABHAY)
    @Feature("PGP-30182")
    @Parameters({"theme"})
    @Test(description = "Exhaust all retry attempts when bank page is closed by user")
    public void ExhaustAllRetryAvailable(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Retry;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO =new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.closeChildWindow();
        cashierPage.retryBtnPopupClosedByUser().click();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.closeChildWindow();
        cashierPage.retryBtnPopupClosedByUser().click();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.closeChildWindow();
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
    }


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

    private void validateCommission(SoftAssertions softAssert, CashierPage cashierPage, double baseAmount, double percentCommission, double flatCommission, String paymentMode) {
        double actualChargeFeeAmt;
        double actualTotalAmt;

        double expectedChargeFeeAmt = convenienceFeeCalculator(baseAmount, percentCommission, flatCommission, paymentMode);
        double expectedTotalAmt = CommonHelpers.doubleHalfUpConvertor(baseAmount + expectedChargeFeeAmt);

        actualChargeFeeAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.chargeFeeAmtPG().getText()));
        actualTotalAmt = Double.valueOf(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(cashierPage.totalAmtPG().getText()));


        softAssert.assertThat(actualChargeFeeAmt).as(paymentMode).isEqualTo(expectedChargeFeeAmt);
        softAssert.assertThat(actualTotalAmt).as(paymentMode).isEqualTo(expectedTotalAmt);
    }

    private void validateCommissionNew(SoftAssertions softAssert, CashierPage cashierPage, double baseAmount, double percentCommission, double flatCommission, String paymentMode) {
        double actualChargeFeeAmt;
        double actualTotalAmt;

        double expectedChargeFeeAmt = convenienceFeeCalculator(baseAmount, percentCommission, flatCommission, paymentMode);
        System.out.println("expectedChargeFeeAmt: " + expectedChargeFeeAmt);
        double expectedTotalAmt = CommonHelpers.doubleHalfUpConvertor(baseAmount + expectedChargeFeeAmt);
        System.out.println("expectedTotalAmt: " + expectedTotalAmt);

        actualChargeFeeAmt = Double.valueOf(cashierPage.chargeFeeAmtAtPG()
        .findElement(By.xpath("//span[contains(@class,'ptm-fee-sub-heading')]"))
        .getText()
        .replace("₹", "")
        .trim());
        System.out.println("actualChargeFeeAmt: " + actualChargeFeeAmt);
        actualTotalAmt = Double.valueOf(cashierPage.totalAmtPGNew()
        .findElement(By.xpath("//button[contains(@class, 'ptm-custom-btn')]"))
        .getText()
        .replace("Pay ₹", "")  // removes "Pay ₹"
        .trim());
        System.out.println("actualTotalAmt: " + actualTotalAmt);

        softAssert.assertThat(actualChargeFeeAmt).as(paymentMode).isEqualTo(expectedChargeFeeAmt);
        softAssert.assertThat(actualTotalAmt).as(paymentMode).isEqualTo(expectedTotalAmt);
    }


    private UIElement getElementByXpath(String xpath, String theme) {
        return new UIElement(By.xpath(xpath), theme, xpath);
    }

    private List<UIElement> getElementsByXpath(String xpath, String theme) {
        return UIElements.getMultiple(By.xpath(xpath), theme, xpath);
    }

    private String getColorCode(String color) {
        String[] hexValue = color.replace("rgba", "").replace(")", "").replace("(", "").split(",");
        hexValue[0] = hexValue[0].trim();
        int hexValue1 = Integer.parseInt(hexValue[0]);
        hexValue[1] = hexValue[1].trim();
        int hexValue2 = Integer.parseInt(hexValue[1]);
        hexValue[2] = hexValue[2].trim();
        int hexValue3 = Integer.parseInt(hexValue[2]);
        return String.format("#%02x%02x%02x", hexValue1, hexValue2, hexValue3);
    }

    @DataProvider
    public Object[][] getMID() {
        return new Object[][]{
                {Constants.MerchantType.LOGIN_STRIP, "checkoutjs_web_revamp"},
                {Constants.MerchantType.LOGIN_STRIP, "checkoutjs_wap_revamp"},
        };
    }


//Verify bankmandate paymode is appearing on cashier page

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate Bank Mandate Paymode appearing on cashier Page")
    public void TC_001_VerifyBankmandatePaymodeAppearingOnCashierPage(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabBankMandate().assertVisible();
    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate successfull bank mandate transaction on checkout flow")
    public void TC_002_VerifyBankmandateTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().click();
//        cashierPage.pause(3);
//        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
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

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate NB is selected by default for bank mandate transation")
    public void TC_003_VerifyNBSelectedByDefault(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.getBankMandateListNew().get(0).click();
        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        cashierPage.pause(1);
   //     cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).isSelected();
    }


 //   @Owner(GAGANDEEP)
 //   @Parameters({"theme"})
 //   @Test(description = "Validate that Purpose of subscription is displayed on cashier page if passed by customer", enabled = false)
    public void TC_004_VerifyManuallyEntering3FieldsAppear(@Optional("checkoutjs_web") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().click();

    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate autheticate option as NB")
    public void TC_005_VerifyManuallyEntering3FieldsAppear(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        cashierPage.pause(1);
        Assert.assertTrue(cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).isSelected());

    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate autheticate option as DC")
    public void TC_006_VerifyAuthenticationOptionsDC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
            cashierPage.pause(1);
        }
        cashierPage.tabBOB().click();
        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        Assert.assertTrue(cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).isEnabled());
    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate proceed button without user name will invoke error")
    public void TC_007_VerifyProceedButtonWithoutUserNameErrorInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        // cashierPage.getBankMandateList().get(0).click();
        cashierPage.tabBOB().click();
        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.BankDetails().sendKeys("915445500424");
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.validateUserErrorMsg();
//        Assertions.assertThat(cashierPage.userErrorMessage().getText()).isEqualTo("Please enter your name");

    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate that proceed without Bank Account will invoke error")
    public void TC_008_VerifyProceedButtonWithoutBankAccountErrorInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        // cashierPage.getBankMandateList().get(0).click();
        cashierPage.tabBOB().click();
        cashierPage.IfscDetails().sendKeys("PYTM0000001");
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.validateBankAccountErrorMsg();
//        Assertions.assertThat(cashierPage.bankAccountErrorMessage().getText()).isEqualTo("Please enter your bank account number");
    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate that proceed without IFSC Code will throw error")
    public void TC_009_VerifyProceedButtonWithoutIFSCErrorInvoke(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        Thread.sleep(2000);
        cashierPage.getBankMandateListNew().get(0).click();
        cashierPage.UserBankName().sendKeys("Akshat Sharma");
        cashierPage.BankDetails().sendKeys("915445500424");
        Thread.sleep(2000);

        cashierPage.buttonPGPayNow().click();
        cashierPage.validateIfscErrorMsg();
       Assertions.assertThat(cashierPage.ifscErrorMessage().getText()).isEqualTo("Please enter your bank IFSC");

    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate CHange in Bank link will list top 6 banks in list")
    public void TC_010_VerifyChangeInBankTop5BankAppears(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        Thread.sleep(10000);
        cashierPage.getBankMandateList().get(0).click();
        cashierPage.clickChangeBank();
        Assertions.assertThat(cashierPage.getBankMandateList().size()).as("Bank list is not Top 6 Banks").isEqualTo(6);


    }


    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate that subs is created in AUTHORIZED State")
    public void TC_011_VerifySubsCreatedInInitState(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
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
                .setMandateAccountDetails(new MandateAccountDetails())
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
       // cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click(); Comes selected by default
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().waitUntilClickable();
        bankMandatePage.confirmButton().click();
//        cashierPage.pause(3);
//        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
                .validateMid(merchant.getId())
                .validatePaymentMode(Constants.PayMode.BANK_MANDATE.toString())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("3006")
                .validateRespMsg("SUCCESS")
                .validateGatewayName("PPBL")
                .validateSubsId(Constants.ValidationType.NON_EMPTY)
                .validateMandateType("E_MANDATE")
                .assertAll();

        String subsId = responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID);

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("AUTHORIZED");
        Assertions.assertThat(PGPHelpers.executeUntilSubsPaymentInfoNotFound("status", subsId, orderId))
                .as("status mismatch")
                .isEqualToIgnoringCase("INIT");

    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate isUpfrontTxnPending is Not Updated for Amount equal to 0 in Meta Data")
    public void TC_012_BankMandateSubscriptionDCPAymodeAmount0(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.UPICONSENT);
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Debit Card");
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
       // cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click(); Comes selected by default
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.activateSubscription().waitUntilClickable();
        bankMandatePage.activateSubscription().click();
//        cashierPage.pause(3);
//        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
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
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), initTxnDTO.orderFromBody()))
                .as("status mismatch")
                .isEqualToIgnoringCase("{\"purpose\":\"Others\",\"mobileNo\":\"7017658313\",\"emailId\":\"test@paytm.com\",\"pud\":true}");

    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate subscription Upfront and isUpfrontTxnPending is Updated from Amount Greater than 0 in Meta Data")
    public void TC_013_ValidateSubsPurposeValueIsUpdatedForUpfrontAmountInMetaData(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.UPICONSENT);
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
   //     cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().waitUntilClickable();
        bankMandatePage.confirmButton().click();
//        cashierPage.pause(3);
//        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
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
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), initTxnDTO.orderFromBody()))
                .as("status mismatch")
                .isEqualToIgnoringCase("{\"isUpfrontTxnPending\":true,\"purpose\":\"Others\",\"mobileNo\":\"7017658313\",\"emailId\":\"test@paytm.com\",\"pud\":true}");

    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate subscription created in Authorized state and sub_status=NPCI_PENDING for Amount greater than 0")
    public void TC_0014_ValidateSubsCreatedInAuthorizedStateAmountGreaterThan0(@Optional("checkoutjs_web_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.UPICONSENT);
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String TxnMaxAmount = "10";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(TxnMaxAmount)
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
   //     cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        cashierPage.pause(2);

        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto ₹" + TxnMaxAmount);

        }
        else{
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandatePage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("₹" + TxnMaxAmount);
        }
        bankMandatePage.confirmButton().waitUntilClickable();
        bankMandatePage.confirmButton().click();
//        cashierPage.pause(3);
//        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
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
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), initTxnDTO.orderFromBody()))
                .as("status mismatch")
                .isEqualToIgnoringCase("AUTHORIZED");
        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status",
                responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.SUBS_ID), initTxnDTO.orderFromBody()))
                .as("sub_status mismatch")
                .isEqualToIgnoringCase("NPCI_PENDING");


    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate subscription created in Active state and sub_status is CONFIRMED on getting response from NPCI for Amount greater than 0")
    public void TC_015_ValidateSubsCreatedStatusIsActiveResponseFromNPCIGreaterThan0(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPICONSENT);
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String TxnMaxAmount = "10";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("1")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(TxnMaxAmount)
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
    //    cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        cashierPage.pause(2);

        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandateRevampPage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("Upto ₹" + TxnMaxAmount);

        }
        else{
            Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                    .getRowValue(BankMandatePage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("₹" + TxnMaxAmount);
        }

        bankMandatePage.confirmButton().click();
        cashierPage.pause(3);
        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
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
                        SubsId, initTxnDTO.orderFromBody()))
                        .as("status mismatch")
                        .isEqualToIgnoringCase("CONFIRMED"));


        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status",
                        SubsId, initTxnDTO.orderFromBody()))
                        .as("sub_status mismatch")
                        .isEqualToIgnoringCase("CONFIRMED"));


    }

    @Owner(GAGANDEEP)
    @Parameters({"theme"})
    @Test(description = "Validate subscription Renewed for Amount greater than 0")
    public void TC_016_Enhanced_ValidateSubRenewedStatusForAmountGreaterThan0(@Optional("checkoutjs_web_revamp") String theme) throws Exception {


        User user = userManager.getForRead(Label.UPICONSENT);
        condition = condition.replace("{CUST_ID}", user.custId());
        UpiPredicate upiPredicate = new UpiPredicate(condition, upiProfileData);
        upiPredicate.execute();
        PaymentDTO paymentDTO = new PaymentDTO().setMandateAuthMode("Net Banking");
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String txnMaxAmount = "100";
        String txnAmount = "1";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(txnMaxAmount)
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
    //    cashierPage.bankmandateAuthMode(paymentDTO.getMandateAuthMode()).click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
       /* Assertions.assertThat(bankMandatePage.tableSubscriptionForm()
                .getRowValue(BankMandatePage.subscriptionDetails.MAX_AMOUNT.toString())).isEqualTo("₹" + txnMaxAmount);
        */
        bankMandatePage.confirmButton().click();
        cashierPage.pause(5);
        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
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

        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("status", SubsId, initTxnDTO.orderFromBody())
                .equalsIgnoreCase("ACTIVE"));


        Assertions.assertThat(PGPHelpers.executeUntilSubsContractNotFound("sub_status", SubsId, initTxnDTO.orderFromBody())
                .equalsIgnoreCase("ACTIVE"));

        //InstaCallback
        String grepEsn = "grep \"" + orderId + "\"  /paytm/logs/instaproxy.log |grep \"ExtSN=\"";
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        JsonPath jsonPath1 = new BankMandatePaymentResponse(extSnValue).execute().jsonPath();

        RenewSubscriptionDTO renewSubscriptionDTO = new RenewSubscriptionDTO.Builder
                (merchant.getId(), SubsId, initTxnDTO.txnAmountFromBody())
                .setTxnAmount("10")
                .setMerchantKey(merchant.getKey())
                .build();


        RenewSubscription renewSubscription = new RenewSubscription(renewSubscriptionDTO);

        await().pollInterval(Duration.FIVE_SECONDS).atMost(Duration.ONE_MINUTE).untilAsserted(() ->
                Assertions.assertThat(renewSubscription.execute().jsonPath()
                        .getString("body.resultInfo.resultStatus"))
                        .isEqualToIgnoringCase("S"));

        Assertions.assertThat(PGPHelpers.executeUntilAcquirementIdNotFound(SubsId, initTxnDTO.orderFromBody())).isNotNull();

        Assertions.assertThat(PGPHelpers.executeToFetchStatusInPaymentDetails(SubsId, initTxnDTO.orderFromBody())).isEqualTo("ACTIVE");


    }

    @Owner("Karmvir")
    @Feature("PGP_28691")
    @Test(description = "Verify Bulk Apply api should not hit when BLOCK_BULK_APPLY_PROMO preference is active in checkoutjs")
    public void PGP_28691_BulkPromoShouldNotHitWhenBLOCK_BULK_APPLY_PROMOisActive(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        String SSOToken= user.ssoToken();
        Constants.MerchantType merchantType = Constants.MerchantType.BLOCK_BULK_APPLY;
        Promo promo = null;
        for (int i = 0; i < 2; i++) {
            promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }
        WalletHelpers.modifyBalance(user, 15.00);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SSOToken, merchantType, simplifiedPaymentOffers)
                .setTxnValue("10.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.applyPromoText().assertNotVisible();
        cashierPage.clickSavedCardTab();
        cashierPage.applyPromoText().assertVisible();

    }

    @Parameters({"theme"})
    @Test(description = "Verify failure txn response in PaymentStatus api without login")
    public void  PGP_32142_pgp_30635_ValidateFailureTxnResponseinPaymentStatusAPIWithoutLogin(@Optional("checkoutjs_web") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("99.97")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(), initTxnDTO.getBody().getMid(), merchantType.getKey())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = getPaymentStatus.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("99.97");
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());

    }

    @Parameters({"theme"})
    @Test(description = "Verify failure txn response in PaymentStatus api with login")
    public void  PGP_32142_pgp_30635_ValidateFailureTxnResponseinPaymentStatusAPIWithLogin(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.97")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(), initTxnDTO.getBody().getMid(), merchantType.getKey())
                .build();
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = getPaymentStatus.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
        Assertions.assertThat(jsonPath.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(jsonPath.getString("body.txnAmount")).isEqualToIgnoringCase("99.97");
        Assertions.assertThat(jsonPath.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
    }

    @Parameters({"theme"})
    @Test(description = "Verify failure txn response in PaymentStatus api after all retries are exhausted")
    public void  PGP_32142_pgp_30635_ValidateFailureTxnResponseinPaymentStatusAPIAfterAllRetriesExhausted(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PPBL_NB_PCF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDetailsForRetry = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC,paymentDetailsForRetry);
        cashierPage.waitUntilLoads();

        GetPaymentStatusDTO getPaymentStatusDTOPending = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(), initTxnDTO.getBody().getMid(), merchantType.getKey())
                .build();
        GetPaymentStatus getPaymentStatusPending = new GetPaymentStatus(getPaymentStatusDTOPending);
        Response responsePending = getPaymentStatusPending.execute();
        JsonPath jsonPathPending = responsePending.jsonPath();
        Assertions.assertThat(jsonPathPending.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("PENDING");
        Assertions.assertThat(jsonPathPending.getString("body.resultInfo.resultCode")).isEqualTo("402");
        Assertions.assertThat(jsonPathPending.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("We are processing your transaction.");
        Assertions.assertThat(jsonPathPending.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(jsonPathPending.getString("body.txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPathPending.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());


        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC,paymentDetailsForRetry);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_FAILURE")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();

        GetPaymentStatusDTO getPaymentStatusDTOFail = new GetPaymentStatusDTO.Builder
                (initTxnDTO.getBody().getOrderId(), initTxnDTO.getBody().getMid(), merchantType.getKey())
                .build();
        GetPaymentStatus getPaymentStatusFail = new GetPaymentStatus(getPaymentStatusDTOFail);
        Response responseFail = getPaymentStatusFail.execute();
        JsonPath jsonPathFail = responseFail.jsonPath();
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultStatus")).isEqualToIgnoringCase("TXN_FAILURE");
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultCode")).isEqualTo("227");
        Assertions.assertThat(jsonPathFail.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Looks like OTP entered was incorrect. Please try again.");
        Assertions.assertThat(jsonPathFail.getString("body.orderId")).isEqualToIgnoringCase(initTxnDTO.getBody().getOrderId());
        Assertions.assertThat(jsonPathFail.getString("body.txnAmount")).isEqualToIgnoringCase("1.00");
        Assertions.assertThat(jsonPathFail.getString("body.gatewayName")).isEqualToIgnoringCase(Constants.Gateway.HDFC.toString());
    }

    @Owner(ABHAY)
    @Parameters({"theme"})
    @Test(description = "Verify footer color doesn't change on changing header color from ump dashboard")
    public void FooterBackgroundColourDoNotChangeOnChangingHeaderbackGroundColour(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.THEMATIC_PREFERENCE;
        String expectedFooterBckColor = "#f5f8fa";
//        if (theme.equals("checkoutjs_web_revamp")){
//            expectedFooterBckColor="#000000";
//        }
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        String configS = config.toString();
        configS = configS.replace("\"style\":{\"bodyBackgroundColor\":\"#fafafb\",\"bodyColor\":\"\",\"themeBackgroundColor\":\"#dfa231\",\"themeColor\":\"#ffffff\",\"headerBackgroundColor\":\"#284055\",\"headerColor\":\"#ffffff\",\"errorColor\":\"\",\"successColor\":\"\",\"card\":{\"padding\":\"\",\"backgroundColor\":\"\"}},", "");
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(configS, MerchantConfig.class);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        String actualFooterBckColor = getColorCode(getElementByXpath("//*[@id = 'checkout-footer']", theme).getCssValue("background-color"));
        Assertions.assertThat(actualFooterBckColor).as("Actual footer background colorr doesn't match expected color").isEqualTo(expectedFooterBckColor);
        Assertions.assertThat(headerBckcolor).as("Header background color should not be equal to footer color").isNotEqualTo(actualFooterBckColor);
    }

    @Owner(ABHAY)
    @Parameters({"theme"})
    @Test(description = "Verify footer text color doesn't change on changing header text color from ump dashboard")
    public void FooterTextColourDoNotChangeOnChangingHeaderTextColour(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.THEMATIC_PREFERENCE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String expectedFooterTextColor = "#506d85";
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        String configS = config.toString();
        configS = configS.replace("\"style\":{\"bodyBackgroundColor\":\"#fafafb\",\"bodyColor\":\"\",\"themeBackgroundColor\":\"#dfa231\",\"themeColor\":\"#ffffff\",\"headerBackgroundColor\":\"#284055\",\"headerColor\":\"#ffffff\",\"errorColor\":\"\",\"successColor\":\"\",\"card\":{\"padding\":\"\",\"backgroundColor\":\"\"}},", "");
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(configS, MerchantConfig.class);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String headerTextColor = getColorCode(getElementByXpath("//span[contains(text(),'TestingMerchant')]", theme).getCssValue("color"));
        String actualFooterTextColor = getColorCode(getElementByXpath("//span[contains(text(),'100% Secure Payments Powered by')]", theme).getCssValue("color"));
        Assertions.assertThat(actualFooterTextColor).as("Actual footer text color doesn't match expected footer text color").isEqualTo(expectedFooterTextColor);
        Assertions.assertThat(headerTextColor).as("Header text color should not be equal to footer text color").isNotEqualTo(actualFooterTextColor);
    }

    @Owner(ABHAY)
    @Parameters({"theme"})
    @Test(description = "Verify footer background color doesn't change after changing header background color from config")
    public void FooterBackgroundColourDoesntChangeOnChangingHeaderBackgroundColourFromConfig(@Optional("checkoutjs_web") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.THEMATIC_PREFERENCE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String expectedFooterBckColor = "#f5f8fa";
//        if (theme.equals("checkoutjs_web_revamp")){
//            expectedFooterBckColor="#000000";
//        }
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.style.setHeaderBackgroundColor("#FF33E9");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        String actualFooterBckColor = getColorCode(getElementByXpath("//*[@id = 'checkout-footer']", theme).getCssValue("background-color"));
        Assertions.assertThat(actualFooterBckColor).as("Actual footer background color doesn't match expected color").isEqualTo(expectedFooterBckColor);
        Assertions.assertThat(headerBckcolor).as("Header background color should not be equal to footer background color").isNotEqualTo(actualFooterBckColor);
    }

    @Owner(ABHAY)
    @Parameters({"theme"})
    @Test(description = "Verify footer colour doesn't change after changing header colour from config")
    public void FooterTextColourDoesntChangeAfterChangingHeaderTextColourFromConfig(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.THEMATIC_PREFERENCE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String expectedFooterTextColor = "#182233";
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.style.setHeaderColor("#FF3371");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String headerTextColor = getColorCode(getElementByXpath("//span[contains(text(),'TestingMerchant')]", theme).getCssValue("color"));
        String actualFooterTextColor = getColorCode(getElementByXpath("//span[contains(text(),'100% Secure Payments')]", theme).getCssValue("color"));
        Assertions.assertThat(actualFooterTextColor).as("Actual footer text color doesn't match expected footer text color").isEqualTo(expectedFooterTextColor);
        Assertions.assertThat(headerTextColor).as("Header color should not be equal to footer color").isNotEqualTo(actualFooterTextColor);
    }


    @Owner(ABHAY)
    @Parameters({"theme"})
    @Feature("PGP-30168")
    @Test(description = "verify Successful EMI_DC transaction sending mob no instead of SSO token in initiate transaction API and merchant config")
    public void SuccessfulEmiDCTransactionUsingMobNoInInitiateAPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String mobNoWithEmiDcConfigured = "8006006993";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DC_CC)
                .setTxnValue("200")
                .setMobile(mobNoWithEmiDcConfigured)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        UserDetail userDetail = new UserDetail();
        userDetail.setMobileNumber(mobNoWithEmiDcConfigured);
        config.data.setToken(txnToken);
        config.data.setUserDetail(userDetail);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER_EMI);
        paymentDTO.setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("EMI_DC")
                .validateGatewayName("ICIE")
                .validateBankName("ICICI Bank")
                .assertAll();

    }

    @Owner(ABHAY)
    @Parameters({"theme"})
    @Feature("PGP-30168")
    @Test(description = "Initiate request via initiate txn api with a mobile number on which EMI_DC is not configured and verify EMI_DC should not be visible on cashierPage")
    public void VerifyEmiDCNotVisibleForMobNoWithEmiDcNotConfigured(@Optional("checkoutjs_web") String theme) throws IOException, InterruptedException {
        String mobNoWithEmiDcNotConfigured = "5988976543";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DebitCard)
                .setMobile(mobNoWithEmiDcNotConfigured)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        UserDetail userDetail = new UserDetail();
        userDetail.setMobileNumber(mobNoWithEmiDcNotConfigured);
        config.data.setToken(txnToken);
        config.data.setUserDetail(userDetail);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().assertNotVisible();

    }

    @Owner(ABHAY)
    @Parameters({"theme"})
    @Feature("PGP-30168")
    @Test(description = "Initiate request via initiate txn api with no mobile number and verify EMI_DC should not be visible on cashierPage")
    public void VerifyEmiDCNotVisibleWithoutMobNoInInitiateApi(@Optional("checkoutjs_web") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DebitCard).build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath iniJsonPath = initTxn.execute().jsonPath();
        String txnToken = iniJsonPath.getString("body.txnToken");
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().assertNotVisible();
    }

    @Owner(VIDHI)
    @Feature("PGP-30176")
    @Parameters({"theme"})
    @Test(description = "Verfiy Pending CC txn using Checkout js flow when cancelPendingOrder = TRUE")
    public void ValidatePendingTxnWhenCancelPendingOrderFlagIsTRUE(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("77")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setCancelPendingOrder(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("141")
                .validateRespMsg("User has not completed transaction.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(VIDHI)
    @Feature("PGP-30176")
    @Parameters({"theme"})
    @Test(description = "Verfiy Pending CC txn using Checkout js flow when cancelPendingOrder = FALSE")
    public void ValidatePendingTxnWhenCancelPendingOrderFlagIsFALSE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("77")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setCancelPendingOrder(false);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        responsePage.validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("402")
                .validateRespMsg("We are processing your transaction.")
                .validateStatus("PENDING")
                .validatePaymentMode("CC")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankName("HDFC Bank")
                .validateTxnDate(new Date())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(VIDHI)
    @Feature("PGP-30176")
    @Parameters({"theme"})
    @Test(description = "Verfiy SUCCESSful CC txn using Checkout js flow when cancelPendingOrder = TRUE")
    public void ValidateSUCCESSTxnWhenCancelPendingOrderFlagIsTRUE(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("10")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setCancelPendingOrder(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(VIDHI)
    @Feature("PGP-30176")
    @Parameters({"theme"})
    @Test(description = "Verfiy Failed CC txn using Checkout js flow when cancelPendingOrder = TRUE")
    public void ValidateFAILTxnWhenCancelPendingOrderFlagIsTRUE(@Optional("checkoutjs_web") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.98")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setCancelPendingOrder(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.PGOnly.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }

    @Owner(MAYURI)
    @Feature("PGPUI-1952")
    @Parameters({"theme"})
    @Test(description = "Verify child MID theme overwrite enabled ")
    public void ValidateChildMIDThemeOverwriteEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String expectedHeaderBckColor = "#0d1528";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_ENABLED)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_1)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);

        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_ENABLED,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledParentExpected = "false";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledParentExpected);

        String configS = config.toString();
        configS = configS.replace("\"style\":{\"bodyBackgroundColor\":\"#fafafb\",\"bodyColor\":\"\",\"themeBackgroundColor\":\"#dfa231\",\"themeColor\":\"#ffffff\",\"headerBackgroundColor\":\"#284055\",\"headerColor\":\"#ffffff\",\"errorColor\":\"\",\"successColor\":\"\",\"card\":{\"padding\":\"\",\"backgroundColor\":\"\"}},","");
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(configS, MerchantConfig.class);
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.THEME_OVERWRITE_ENABLED_1.getId());
        checkoutPage.createCheckoutJsOrder(config);

        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_1,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);


        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        Assertions.assertThat(headerBckcolor).as("Header background color should be equal").isEqualTo(expectedHeaderBckColor);

    }

    @Owner(MAYURI)
    @Feature("PGPUI-1952")
    @Parameters({"theme"})
    @Test(description = "Verify parent MID theme overwrite enabled")
    public void ValidateParentMIDThemeOverwriteEnabled(String theme) throws Exception {
        String expectedHeaderBckColor = "#0d1528";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_1)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_ENABLED)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);

        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_1,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);

        String configS = config.toString();
        configS = configS.replace("\"style\":{\"bodyBackgroundColor\":\"#fafafb\",\"bodyColor\":\"\",\"themeBackgroundColor\":\"#dfa231\",\"themeColor\":\"#ffffff\",\"headerBackgroundColor\":\"#284055\",\"headerColor\":\"#ffffff\",\"errorColor\":\"\",\"successColor\":\"\",\"card\":{\"padding\":\"\",\"backgroundColor\":\"\"}},","");
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(configS, MerchantConfig.class);
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.THEME_ENABLED.getId());
        checkoutPage.createCheckoutJsOrder(config);

        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_ENABLED,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "false";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        Assertions.assertThat(headerBckcolor).as("Header background color should be equal").isEqualTo(expectedHeaderBckColor);


    }

    @Owner(MAYURI)
    @Feature("PGPUI-1952")
    @Parameters({"theme"})
    @Test(description = "Verify parent & child MID theme overwrite enabled")
    public void ValidateParentAndChildMIDThemeOverwriteEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String expectedHeaderBckColor = "#0d1528";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_2)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_1)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);

        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_2,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);

        String configS = config.toString();
        configS = configS.replace("\"style\":{\"bodyBackgroundColor\":\"#fafafb\",\"bodyColor\":\"\",\"themeBackgroundColor\":\"#dfa231\",\"themeColor\":\"#ffffff\",\"headerBackgroundColor\":\"#284055\",\"headerColor\":\"#ffffff\",\"errorColor\":\"\",\"successColor\":\"\",\"card\":{\"padding\":\"\",\"backgroundColor\":\"\"}},","");
        ObjectMapper mapper = new ObjectMapper();
        config = mapper.readValue(configS, MerchantConfig.class);
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.THEME_OVERWRITE_ENABLED_1.getId());
        checkoutPage.createCheckoutJsOrder(config);

        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_1,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        Assertions.assertThat(headerBckcolor).as("Header background color should be equal").isEqualTo(expectedHeaderBckColor);

    }

    @Owner(MAYURI)
    @Feature("PGPUI-1952")
    @Parameters({"theme"})
    @Test(description = "Verify config style when parent MID theme overwrite enabled")
    public void ValidateConfigWithParentMIDThemeOverwriteEnabled(String theme) throws Exception {
        String expectedConfigHeaderBckColor = "#284055";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_2)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_ENABLED)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);

        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_2,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);

        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.THEME_ENABLED.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);

        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_2,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "false";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        Assertions.assertThat(headerBckcolor).as("Config Header background color should be visible").isEqualTo(expectedConfigHeaderBckColor);

    }

    @Owner(MAYURI)
    @Feature("PGPUI-1952")
    @Parameters({"theme"})
    @Test(description = "Verify config style when child MID theme overwrite enabled")
    public void ValidateConfigWithChildMIDThemeOverwriteEnabled(@Optional("checkoutjs_web_revamp") String theme ) throws Exception {
        String expectedConfigHeaderBckColor = "#284055";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_ENABLED)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_2)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);

        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.AddMoney,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "false";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);

        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.THEME_OVERWRITE_ENABLED_2.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);


        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_2,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        Assertions.assertThat(headerBckcolor).as("Config Header background color should be visible").isEqualTo(expectedConfigHeaderBckColor);

    }

    @Owner(MAYURI)
    @Feature("PGPUI-1952")
    @Parameters({"theme"})
    @Test(description = "Verify config style when parent & child MID theme overwrite enabled")
    public void ValidateConfigWithParentAndChildThemeOverwriteEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String expectedConfigHeaderBckColor = "#284055";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_1)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.THEME_OVERWRITE_ENABLED_2)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);

        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_1,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);

        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.THEME_OVERWRITE_ENABLED_2.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);

        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.THEME_OVERWRITE_ENABLED_2,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        Assertions.assertThat(headerBckcolor).as("Config Header background color should be visible").isEqualTo(expectedConfigHeaderBckColor);

    }

    @Owner(MAYURI)
    @Feature("PGPUI-1965")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn using Checkout js flow for redirect=false")
    public void ValidateSuccessTxnUsingCCRedirectFalse(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setRedirect(Boolean.FALSE);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        riskVerificationPage.clickAlert();
        checkoutPage.waitUntilLoads();
        String expectedPageURL= checkoutPage.getPageURL();
        Assertions.assertThat(LocalConfig.CHECKOUTJS_URL).describedAs("Not back to Merchant page").isEqualTo(expectedPageURL);
    }
    @Owner(MAYURI)
    @Feature("PGPUI-1965")
    @Parameters({"theme"})
    @Test(description = "Verfiy successfull CC txn using Checkout js flow when mid=null")
    public void ValidateSuccessTxnUsingCCWhenMIDNull(@Optional("checkoutjs_web") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.merchant.setMid("");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }




    @Owner(PRIYANSHI)
    @Feature("PGP-32355")
    @Parameters({"theme"})
    @Test(description = "Validate that TxnPaidTime should be return in response of call back api for NB txn ")
    public void VerifyTxnPaidTimeFieldShouldReturnInResponse_ViaNB(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.TXNTIME;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.NB);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.TXNTIME.getKey())
                .validateTxnPaidTime(new Date())
                .assertAll();

    }

    @Parameters({"theme"})
    @Test(description = "Verify that promo gets applied to promoAmount only in bulk apply also in CheckoutJS")
    public void ValidatePromoIsAppliedToPromoAmountOnlyinBulkApplyforCheckoutJs(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {

        ResponsePage responsePage;
        PaymentDTO paymentDTO = new PaymentDTO();
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user,Double.parseDouble("100.00"));
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber());
        for (int i = 0; i <= 2; i++) {
            Promo promo = new Promo();
            new Merchant(merchantType.getId(), true).getPromos().add(promo);
        }


        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers
                .setPromoCode("")
                .setApplyAvailablePromo("true")
                .setValidatePromo("true")
                .setPromoAmount("10");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        cashierPage.promoOffersList().assertVisible();
        String promoOfferText= cashierPage.applyPromoText().getText();
        Assert.assertTrue(promoOfferText.contains("0.5"));
        String expectedTxnAmt;
        if(promoOfferText.contains("discount"))
            expectedTxnAmt="19.50";
        else
            expectedTxnAmt="20.00";

        cashierPage.payBy(Constants.PayMode.CC);

        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(expectedTxnAmt)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .AssertAll();


        SoftAssert softAssert = new SoftAssert();
        com.paytm.api.Peon peon = new Peon(initTxnDTO.getBody().getOrderId());
        peon.executeUntilGetResponse();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(initTxnDTO.getBody().getOrderId());
        softAssert.assertEquals(peonResponse.getSTATUS(), "TXN_SUCCESS");
        softAssert.assertEquals(peonResponse.getTXNAMOUNT(), expectedTxnAmt);
        softAssert.assertEquals(peonResponse.getPAYABLE_AMOUNT(), "20.00");
        softAssert.assertNotNull(peonResponse.getPaymentPromoCheckoutData());

        if(promoOfferText.contains("cashback")) {
            JsonPath jsonPath1 = new JsonPath(peonResponse.getPaymentPromoCheckoutData());
            softAssert.assertEquals(jsonPath1.getString("savings.savings"), "[0.50]");
            softAssert.assertEquals(jsonPath1.getString("savings.redemptionType"), "[cashback]");
        }

        softAssert.assertAll();
    }


    @Parameters({"theme"})
    @Test(description = "Verify that Discount promo gets applied to promoAmount only for PCF merchant in CheckoutJS")
    public void ValidateDiscountPromoIsAppliedToPromoAmountOnlyforPCF(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {

        ResponsePage responsePage;
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly_Pcf;
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        new Merchant(merchantType.getId(), true).getPromos().add(promo);

        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("discount").setApplyAvailablePromo("true").setValidatePromo("true").setPromoAmount("10");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType, simplifiedPaymentOffers)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);

        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("19.5")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateChargeAmount("1.32")
                .validateResponsePageParameters()
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount("19.50")
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validatePayableAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validatePaymentPromoCheckoutDataPresent()
                .validateTxnDate(new Date())
                .validateChargeAmount("1.32")
                .AssertAll();
    }
    @Owner(AJEESH)
    @Feature("PGP-33733")
    @Parameters({"theme"})
    @Test(description = "Pre-fill the Bank IFSC code and account holder name using the saved mandate object details")
    public void TC_001_Verify_Fields_are_editable(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.BankMandateOption().click();

        cashierPage.tabBOB().click();

        cashierPage.IfscDetails().sendKeys("PYTM0000001"); // Updating Values
        cashierPage.UserBankName().sendKeys("Ajeesh Nair");
        cashierPage.BankDetails().sendKeys("915445500424");

        Assert.assertTrue(cashierPage.IfscDetails().getAttribute("value").contains("PYTM0000001")); // Verify New Data
        Assert.assertTrue(cashierPage.UserBankName().getAttribute("value").contains("Ajeesh Nair"));
        Assert.assertTrue(cashierPage.BankDetails().getAttribute("value").contains("915445500424"));

    }

    @Owner(AJEESH)
    @Feature("PGP-33733")
    @Parameters({"theme"})
    @Test(description = "Pre-fill the Bank IFSC code and account holder name using the saved mandate object details")
    public void TC_002_Verify_Fields_are_noneditable(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
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
                .setMandateAccountDetails(new MandateAccountDetails()) //Sending mandate
                .build();

        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);

        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);

        if(theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)){cashierPage.BankMandateOption().click();}
        //cashierPage.BankMandateOption().click();

        Assert.assertFalse(cashierPage.IfscDetails().isEnabled());  // verify button is non-editable
        Assert.assertFalse(cashierPage.UserBankName().isEnabled()); // verify button is non-editable
        Assert.assertFalse(cashierPage.BankDetails().isEnabled());  // verify button is non-editable

    }

 //   @Owner(PRIYANSHI)
 //   @Feature("PGP-33727")
 //   @Parameters({"theme"})
  //  @Test(enabled = false,description = "Verfiy merchant logo and custom footer logo when pref is enable on parent and child mid in LoggedIn state")
    public void toVerifyMerchantLogoAndFooterLogo_whenPrefIsEnableOnBothMid(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.LOGO_MID)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.LOGO_MERCHANT)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);


        config.merchant.setMid(Constants.MerchantType.LOGO_MERCHANT.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.merchantLOGO().waitUntilVisible();
        Assertions.assertThat(cashierPage.merchantLOGO().isDisplayed()).isTrue();
        cashierPage.scrollToElement(cashierPage.footerLOGO());
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();
    }

 //   @Owner(PRIYANSHI)
 //   @Feature("PGP-30176")
 //   @Parameters({"theme"})
 //   @Test(enabled = false, description = "Verfiy merchant logo and custom footer logo when pref is enable on child mid in LoggedIn state")
    public void toVerifyMerchantLogoAndFooterLogo_whenPrefIsEnableOnChildMid(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);

        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.AddMoney)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.LOGO_MERCHANT)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);


        config.merchant.setMid(Constants.MerchantType.LOGO_MERCHANT.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.merchantLOGO().waitUntilVisible();
        cashierPage.merchantLOGO().assertVisible();
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();

    }

 //   @Owner(PRIYANSHI)
 //   @Feature("PGP-33727")
 //   @Parameters({"theme"})
  //  @Test(enabled = false, description = "Verfiy merchant logo and custom footer logo when pref is enable on parent and child mid in NonLoggedIn state")
    public void toVerifyMerchantLogoAndFooterLogo_whenPrefIsEnableOnBothMid_NonLoggedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.LOGO_MID)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.LOGO_MERCHANT)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);


        config.merchant.setMid(Constants.MerchantType.LOGO_MERCHANT.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabCreditCard().click();
        Assertions.assertThat(cashierPage.merchantLOGO().isDisplayed()).isTrue();
        //cashierPage.merchantLOGO().assertVisible();
        cashierPage.scrollToElement(cashierPage.footerLOGO());
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();
    }
 //   @Owner(PRIYANSHI)
 //   @Feature("PGP-30176")
 //   @Parameters({"theme"})
 //   @Test(enabled = false,description = "Verfiy merchant logo and custom footer logo when pref is enable on child mid in NonLoggedIn state")
    public void toVerifyMerchantLogoAndFooterLogo_whenPrefIsEnableOnChildMid_NonLoggedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.AddMoney)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.LOGO_MERCHANT)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);


        config.merchant.setMid(Constants.MerchantType.LOGO_MERCHANT.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.merchantLOGO().waitUntilVisible();
        Assertions.assertThat(cashierPage.merchantLOGO().isDisplayed()).isTrue();
        Assertions.assertThat(cashierPage.footerLOGO().isDisplayed()).isTrue();

    }
    @Owner(POONAM)
    @Parameters({"theme"})
    @Test(description = "Verify paytm logo color derived from ump dashboard")
    public void verifyLogoColor(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.THEMATIC_PREFERENCE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.THEMATIC_PREFERENCE,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        Assertions.assertThat(getMerchantPGPUIThemeChildMIDJson.getString("body.resultInfo.resultCode")).as("Result code doesnt match").isEqualTo("0000");
        Assertions.assertThat(getMerchantPGPUIThemeChildMIDJson.getString("body.resultInfo.resultStatus")).as("Result status doesnt match").isEqualTo("S");
        Assertions.assertThat(getMerchantPGPUIThemeChildMIDJson.getString("body.resultInfo.resultMsg")).as("Result msg doesnt match").isEqualTo("Success");
        Assertions.assertThat(getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.paytmLogo")).as("Logo color doesnt match").isEqualTo("WHITE-LOGO");
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String logoAttribute = cashierPage.getPaytmLogoNew().getAttribute("src");
        Assertions.assertThat(logoAttribute).as("Actual logo color is not white").contains("paytm-pg-white.svg");
    }

    @Owner(POONAM)
    @Parameters({"theme"})
    @Test(description = "Verfiy if bank logo is visible in v5/fpo for EMI in Checkout js flow")
    public void VerifyBankLogoforEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.NATIVE_HYBRID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.getBankLogoEMI().waitUntilVisible();
        cashierPage.getBankLogoEMI().assertVisible();
    }

    @Owner(POONAM)
    @Parameters({"theme"})
    @Test(description = "Verfiy if bank logo is visible in v5/fpo for Net Banking in Checkout js flow")
    public void VerifyBankLogoforNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        cashierPage.getBankLogo().assertVisible();

    }
    @Feature("PGP-36234")
    @Owner(PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verfiy if bank logo is visible in v5/fpo for Net Banking in Checkout js flow while doing NB TXN")
    public void VerifyBankLogoforNB_TXN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        cashierPage.getBankLogo().assertVisible();
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();



    }
    @Feature("PGP-36234")
    @Owner(PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verfiy if bank logo is visible in v5/fpo for Net Banking in Checkout js flow while doing Retry NB TXN")
    public void VerifyBankLogoforNB_RETRY_TXN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        cashierPage.tabNetBanking().click();
        cashierPage.getBankLogo().assertVisible();
        cashierPage.dropdownNB().selectByValue("ICICI");
        cashierPage.pause(3);
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        cashierPage.closeChildWindow();
        cashierPage.tabNetBanking().click();
        cashierPage.getBankLogo().assertVisible();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();



    }

    @Owner(POONAM)
    @Parameters({"theme"})
    @Test(description = "Verfiy bank logo in Saved Card using Checkout js flow")
    public void VerifyBankLogoInSavedCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);

        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.Tokenization_Year,
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.getBankLogo().assertVisible();

    }

    @Owner(POONAM)
    @Parameters({"theme"})
    @Test(description = "Verfiy if bank logo is visible in v2/fpo for Saved VPA in Checkout js flow")
    public void VerifyBankLogoforSavedVpa(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PPBLC_ONLY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.getBankLogo().assertVisible();

    }
    @Owner(PRIYANSHI)
    @Feature("PGP-33728")
    @Parameters({"theme"})
    @Test(description = "Initiate a Checkoutjs success transaction to verfiy new Kafka topic : DYNAMIC_QR")
    public void toVerifyNewKafkaTopic_inSuccess_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("3.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_FACADE_LOGS + " | grep \"ACQUIRING_PAY_ORDER\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"pushDataToDynamicQR\\\":\\\"true\\\"");
        String grepcmd1 = "grep \"" + orderId + "\" "+ LocalConfig.PGPROXY_LOGS + " | grep \"Type=PaymentNotify\"";
        String notificationQueueHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1);
        Assertions.assertThat(notificationQueueHandlerLogs).contains("Pushing Data to kafka topic : DYNAMIC_QR");
//        String grepcmd2 = "grep \"" + orderId + "\" /paytm/logs/socketcluster/websocket.log" + " | grep ";
//        String webSocketClusterLogs = getLogsOnServer(ServerConfigProvider.SERVICE.WEB_SOCKET_CLUSTER, grepcmd2);
//        Assertions.assertThat(webSocketClusterLogs).contains("\"topicName\":\"DYNAMIC_QR\"");
//        Assertions.assertThat(webSocketClusterLogs).contains("\"status\":\"SUCCESS\"");
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-33728")
    @Parameters({"theme"})
    @Test(description = "Initiate a Checkoutjs failure transaction to verfiy new Kafka topic : DYNAMIC_QR, status should be fail in socket cluster logs")
    public void toVerifyNewKafkaTopic_inFailure_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("99.98")
                .build();
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String grepcmd = "grep \"" + orderId + "\" " +LocalConfig.THEIA_FACADE_LOGS + " | grep \"ACQUIRING_PAY_ORDER\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"pushDataToDynamicQR\\\":\\\"true\\\"");
        String grepcmd1 = "grep \"" + orderId + "\" " + LocalConfig.PGPROXY_LOGS;
        String notificationQueueHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1);
        Assertions.assertThat(notificationQueueHandlerLogs).contains("Pushing Data to kafka topic : DYNAMIC_QR");
        String grepcmd2 = "grep \"" + orderId + "\" /paytm/logs/socketcluster/websocket.log";
        String webSocketClusterLogs = getLogsOnServer(ServerConfigProvider.SERVICE.WEB_SOCKET_CLUSTER, grepcmd2);
        Assertions.assertThat(webSocketClusterLogs).contains("\"topicName\":\"DYNAMIC_QR\"");
        Assertions.assertThat(webSocketClusterLogs).contains("\"status\":\"FAIL\"");

    }
    @Owner(PRIYANSHI)
    @Feature("PGP-30176")
    @Parameters({"theme"})
    @Test(description = "Initiate a Checkoutjs Pending transaction to verfiy new Kafka topic : DYNAMIC_QR, there will be no hit on notification and socket cluster")
    public void toVerifyNewKafkaTopic_inPending_Txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("77")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        config.data.setAdditionalProperty("isTimerRequired:",true);
        checkoutPage.createCheckoutJsOrder(config);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.NB);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("402")
                .validateRespMsg("We are processing your transaction.")
                .validateStatus("PENDING")
                .assertAll();
        String orderId = responsePage.textOrderID().getText();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_FACADE_LOGS + " | grep \"ACQUIRING_PAY_ORDER\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\\\"pushDataToDynamicQR\\\":\\\"true\\\"");
        String grepcmd1 = "grep \"" + orderId + "\" "+ LocalConfig.PGPROXY_LOGS;
        String notificationQueueHandlerLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1);
        Assertions.assertThat(notificationQueueHandlerLogs).doesNotContain("Payload pushed to KAFKA successfully for topic : DYNAMIC_QR");
//        String grepcmd2 = "grep \"" + orderId + "\" /paytm/logs/socketcluster/websocket.log";
//        String webSocketClusterLogs = getLogsOnServer(ServerConfigProvider.SERVICE.WEB_SOCKET_CLUSTER, grepcmd2);
//        Assertions.assertThat(webSocketClusterLogs).doesNotContain("\"topicName\":\"DYNAMIC_QR\"");

    }
    @Owner(AJEESH)
    @Feature("PGP-33689")
    @Parameters({"theme"})
    @Test(description = "FetchPaymentOptions gets Value of Channel Code from DB corresponding to its IFSC Code, When Mandate Type is null")
    public void TC001_Verify_ChannelCode_is_retrieved_bySending_IFSC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String DBQuery = "SELECT * FROM PAYTMPGDB.IFSC_CODES ic WHERE IFSC_CODE ='HDFC0009386'";
        String ExpectedChannelCode = DbQueriesUtil.selectFromPGPDB(DBQuery, "BANK_CODE"); // Getting Value from DB

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);

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
                .setMandateAccountDetails(new MandateAccountDetails("HDFC0009386")) //Sending mandate
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String ChannelCodeAPI = fetchPaymentOptionsJson.getString("body.mandateAccountDetails.channelCode");

        Assert.assertEquals(ChannelCodeAPI,ExpectedChannelCode);// Verifying bank Code

    }

    @Owner(AJEESH)
    @Feature("PGP-33689")
    @Parameters({"theme"})
    @Test(description = "FetchPaymentOptions gets Value of Channel Code from DB corresponding to its IFSC Code, When Mandate Type is PAPER MANDATE")
    public void TC002_Verify_ChannelCode_is_retrieved_bySending_IFSC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String DBQuery = "SELECT * FROM PAYTMPGDB.IFSC_CODES ic WHERE IFSC_CODE ='YESB0CVB010'";
        String ExpectedChannelCode = DbQueriesUtil.selectFromPGPDB(DBQuery, "BANK_CODE"); // Getting Value from DB

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("5")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setmandateType("PAPER_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setMandateAccountDetails(new MandateAccountDetails("YESB0CVB010")) //Sending mandate
                .build();

        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);

        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String ChannelCodeAPI = fetchPaymentOptionsJson.getString("body.mandateAccountDetails.channelCode");

        Assert.assertEquals(ChannelCodeAPI,ExpectedChannelCode);// Verifying bank Code

    }
    @Owner(AJEESH)
    @Feature("PGP-33689")
    @Parameters({"theme"})
    @Test(description = "Perform End to End transaction with Channel Code received via IFSC Code")
    public void TC003_Verify_ChannelCode_is_retrieved_bySending_IFSC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String DBQuery = "SELECT * FROM PAYTMPGDB.IFSC_CODES ic WHERE IFSC_CODE ='HDFC0009386'";
        String ExpectedChannelCode = DbQueriesUtil.selectFromPGPDB(DBQuery, "BANK_CODE"); // Getting Value from DB

        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("5")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setmandateType("E_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setMandateAccountDetails(new MandateAccountDetails("HDFC0009386")) //Sending mandate
                .build();

        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();

        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);

        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String ChannelCodeAPI = fetchPaymentOptionsJson.getString("body.mandateAccountDetails.channelCode");

        Assert.assertEquals(ChannelCodeAPI,ExpectedChannelCode);// Verifying bank Code
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.BankMandateOption().click();

        Assert.assertFalse(cashierPage.IfscDetails().isEnabled());  // verify button is non-editable
        Assert.assertFalse(cashierPage.UserBankName().isEnabled()); // verify button is non-editable
        Assert.assertFalse(cashierPage.BankDetails().isEnabled());  // verify button is non-editable

        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().waitUntilClickable();
        bankMandatePage.confirmButton().click();
        cashierPage.pause(3);
        cashierPage.closeChildWindow();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateOrderId(initTxnDTO.orderFromBody())
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

 //   @Owner(PRIYANSHI)
 //   @Feature("PGP-34678")
 //   @Parameters({"theme"})
 //   @Test(enabled = false,description = "Verify UPI Tab not coming up on cashierPage in nonloggedin flow")
    public void VerifyUPICollect_Tab(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.UPI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().assertNotVisible();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab comes up when pref is disabled on merchant without loggedin flow")
    public void VerifyUPICollect_Tab_when_PrefDisabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().assertVisible();

    }

    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab coming up on cashierPage in loggedin flow")
    public void VerifyUPICollect_Tab_loggedIN(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().assertVisible();
    }



    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab comes up when pref is disabled on merchant in loggedin flow")
    public void VerifyUPICollect_Tab_when_PrefDisabled_LoggedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSsoToken(user.ssoToken())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().assertVisible();

    }

    @Owner(PRIYANSHI)
    @Feature("PGP-34678")
    @Parameters({"theme"})
    @Test(description = "Verify UPI Tab comes up after login")
    public void VerifyUPICollect_Via_Login(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setSsoToken(user.ssoToken())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().assertVisible();

    }

    @Owner(AJEESH)
    @Feature("PGP-33732")
    @Parameters({"theme"})
    @Test(description = "Update logic for subscription related error cases")
    public void VerifyErrorMsgisshownforUPIotherthanPaytmOrBhim(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String Expected_Err_Msg = "This UPI ID does not support subscription payments. Please try with another UPI ID.";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("pgp33732mock@ybl");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        cashierPage.textBoxVPA().waitUntilVisible();
        String Err_Msg = cashierPage.invalidVpaText().getText();
        Assert.assertEquals(Err_Msg,Expected_Err_Msg);

        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(subsId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }
    @Owner(AJEESH)
    @Feature("PGP-33732")
    @Parameters({"theme"})
    @Test(description = "Update logic for subscription related error cases")
    public void VerifyErrorMsgisshownforUPInotSupportingSubscription(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String Expected_Err_Msg = "Linked bank does not support subscription payments";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("pgp33732mock@paytm");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        cashierPage.textBoxVPA().waitUntilVisible();
        String Err_Msg = cashierPage.invalidVpaText().getText();
        Assert.assertEquals(Err_Msg,Expected_Err_Msg);

        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(subsId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }
    @Owner(AJEESH)
    @Feature("PGP-33732")
    @Parameters({"theme"})
    @Test(description = "Update logic for subscription related error cases")
    public void VerifyErrorMsgisshownforInvalidUPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
       // String Expected_Err_Msg = "Invalid VPA, Try Again";
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        String subsId = initTxnResponseDTO.getBody().getSubscriptionId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("invalidupi@paytm");
        cashierPage.buttonPGPayNow().click();
//        cashierPage.verifyVPALinkText().click();
        cashierPage.invalidVpaText().waitUntilVisible();
        String Err_Msg = cashierPage.invalidVpaText().getText();
        Assert.assertEquals(Err_Msg,Constants.MessageAssert.INVALID_VPA.toString());

        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
//        cashierPage.verifyVPALinkText().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateSubsId(subsId)
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-33717")
    @Parameters({"theme"})
    @Test(description = "Verify that E-NACH is displayed saved bank mandate account for saved account on cashier page")
    public void verify_SavedBankMandate_Details(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPICONSENT);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("5")
                .setChannelId("WEB")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setAccountNumber("")
                .setmandateType("E_MANDATE")
                .setMandateAccountDetails(new MandateAccountDetails()) //Sending mandate acc. details
                .build();

        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        cashierPage.savedBankMandate().assertVisible();
        Assertions.assertThat(cashierPage.savedBankMandateName().getText()).contains("E-NACH");
        cashierPage.subscriptionDetails().click();
        //  cashierPage.clickPgOverlay(); Not required in New Revamp UI
        cashierPage.pause(1);
        cashierPage.subsDetailsRecurringAmount().assertVisible();
    }

    @Owner(AJEESH)
    @Feature("PGP-33740")
    @Parameters({"theme"})
    @Test(description = "Successful Transaction through Zest Money")
    public void Verify_successful_transaction_with_ZestMoney(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ZEST_MONEY2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("Zest");
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        cashierPage.successfulTransactionButton();

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateBankName(Constants.Bank.ZESTNB.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }


    @Owner(AJEESH)
    @Feature("PGP-33740")
    @Parameters({"theme"})
    @Test(description = "Failure Transaction through Zest Money")
    public void Verify_failure_transaction_with_ZestMoney(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.ZEST_MONEY2;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
       // PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("Zest");
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.failureTransactionButton();
        cashierPage.waitUntilLoads();
        cashierPage.retryBtnPopupClosedByUser().click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.failureTransactionButton();
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("222")
                .validateRespMsg("Transaction amount return by the gateway does not match with Paytm transaction amount.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ZEST.toString())
                .validateBankName(Constants.Bank.ZESTNB.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();
    }
    @Owner(SRINIVAS)
    @Parameters({"theme"})
    @Test(description = "Verify SOP amount left align in the scrolled header of the cashier page")
    public void VerifySopamount(@Optional("checkoutjs_web_revamp")String theme) throws Exception{
        User user = userManager.getForRead(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly).
                build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        cashierPage.waitUntilLoads();
        cashierPage.getTextandamount();
        Assertions.assertThat(cashierPage.getTextandamount().getText()).isEqualTo("Select an option to pay₹1");
    }

    @Owner(PAYAL)
    @Feature("PGP-35336")
    @Parameters({"theme"})
    @Test(description = "Verify payButton with selected Paymode of postpaid , PPBL AND Wallet")
    public void ValidatePayButtonWithSelectedPaymode(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .setTxnValue("1").build();
        WalletHelpers.modifyBalance(user,1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.radioButtonPaytmPostpaid().click();
        cashierPage.PayButtonWithPostPaid().assertVisible();
        cashierPage.checkBoxPPI().check();
        cashierPage.PayButtonWithWallet().assertVisible();
        cashierPage.checkboxPPBL().check();
        cashierPage.PayButtonWithPPBL().assertVisible();
    }

    @Owner(PAYAL)
    @Feature("PGP-35336")
    @Parameters({"theme"})
    @Test(description = "Verify payButton with selected Paymode for Saved Card")
    public void ValidatePayButtonWithSelectedPaymodeAsSC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
    //    SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .setTxnValue("1").build();
    //    WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabSavedCard().click();
        cashierPage.payButtonForSavedCard().assertVisible();
    }

    // Depricated since we cannot fetch build number from jenkins and was failing due to random build number
  //  @Owner(AJEESH)
  //  @Feature("PGP-34687")
  //  @Test(enabled = false, description = "Change the Version for a Merchant and Validate")
    public void Verify_theme_is_changed_for_Merchant() throws Exception {
        String success_Response = "Version updated successfully for requested mids.";
        int random = 700+ (new Random().nextInt(99));
        String new_version = Integer.toString(random);
        Constants.MerchantType merchantType = Constants.MerchantType.VERSION_CHANGE;
        String Merchant = merchantType.getId();

        VersionChange vc = new VersionChange();
        JsonPath js = vc.ChangeVersion(new_version,Merchant).execute().jsonPath();
        String cv_Response = js.getString("message");
        Assert.assertEquals(success_Response,cv_Response);

        js = vc.ValidateVersion(new_version).execute().jsonPath();
        String vv_Response =  js.prettify();
        Assert.assertTrue(vv_Response.contains(Merchant));

    }

    @Owner("Sourav")
    @Feature("PGP-34673")
    @Parameters({"theme"})
    @Test(description = "Validate Duplicate Attempt Message appears on cashier page if txntoken of a successful txn is used for again for a new txn")
    public void DuplicateTxnAttemptusingtxnTokenOfSuccessfulTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.IDEMPOTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateResponsePageParameters()
                .assertAll();


        MerchantConfig config1 = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config1.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.payBy(Constants.PayMode.CC);
        cashierPage1.assertVisible();
        cashierPage1.assertDuplicateAttempt();
        cashierPage1.closePageDuplicateAttemp().click();
        Assertions.assertThat(DriverManager.getDriver().getTitle()).isEqualTo("Merchant Checkout JS");
    }


    @Owner("Sourav")
    @Feature("PGP-34673")
    @Parameters({"theme"})
    @Test(description = "Validate Duplicate Attempt Message appears on cashier page if txntoken of a failed txn when all retry attempt has been exhausted is used again for a new txn")
    public void DuplicateTxnAttemptusingtxnTokenOfFailedTxnWithRetriesExhausted(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        Constants.MerchantType merchantType = IDEMPOTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDetails = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDetails);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC, paymentDetails);
        cashierPage.waitUntilLoads();
        cashierPage.clickInvalidOTPEnteredButtonIfDisplayed();
        cashierPage.payBy(Constants.PayMode.CC,paymentDetails);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("227")
                .validateRespMsg("Looks like OTP entered was incorrect. Please try again.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchantType.getKey())
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateResponsePageParameters()
                .assertAll();


        MerchantConfig config1 = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config1.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.payBy(Constants.PayMode.CC);
        cashierPage1.assertVisible();
        cashierPage1.assertDuplicateAttempt();
        cashierPage1.closePageDuplicateAttemp().click();
        Assertions.assertThat(DriverManager.getDriver().getTitle()).isEqualTo("Merchant Checkout JS");
    }


    @Owner(POOJA)
    @Feature("PGP-35359")
    @Parameters({"theme"})
    @Test(description = "Login Screen changes")
    public void LoginScreenChanges(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.cards().assertVisible();
        cashierPage.tabUPI().assertVisible();
        cashierPage.otherPaymentOption().assertVisible();
    }

//     @Owner(PAYAL)
//     @Feature("PGP-35337")
//     @Parameters({"theme"})
//     @Test(description = "Verify enter Paytm Bank Passcode message",enabled=false)
    public void ValidatePPBLPasscodeTxt(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .setTxnValue("1").build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()));
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.checkboxPPBL().check();
        Assertions.assertThat(cashierPage.enterPPBLPasscodeMessage().getText()).isEqualTo("Enter the passcode used to access your Paytm Bank A/c");
    }

    @Owner(PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify Login strip is not visible or not when Flag is enabled for merchant")
    public void verify_Login_Strip_When_FlagEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().assertNotVisible();
    }

    @Owner(PRIYANSHI)
    @Parameters({"theme"})
    @Test(description = "Verify Login strip is not visible or not when Pref is enabled for merchant")
    public void verify_Login_Strip_When_PrefEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.loginStrip().assertNotVisible();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-35391")
    @Parameters({"theme"})
    @Test(description = "Verifying PayModes LOGO colors")
    public void Verifying_PayMode_LOGO_Colors(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchantType = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.scrollToElement(cashierPage.tabEMI());
        Assertions.assertThat(cashierPage.getCardLogo().getAttribute("src")).as("Logo is not colored").contains("card-icon-new.svg");
        Assertions.assertThat(cashierPage.getEMILogo().getAttribute("src")).as("Logo is not colored").contains("emi-icon-new.svg");
        Assertions.assertThat(cashierPage.getUpiLogo().getAttribute("src")).as("Logo is not colored").contains("upi-icon-new.svg");
        Assertions.assertThat(cashierPage.getNBLogo().getAttribute("src")).as("Logo is not colored").contains("nb-icon-new.svg");    }

//     @Owner(PRIYANSHI)
//     @Feature("PGP-35443")
//     @Parameters({"theme"})
//     @Test(description = "Verify when PPBL balance + FD balance amount is equal or greater than order amount then FD Balance should be visible",enabled=false)
    public void verifying_FD_BalanceIsVisible(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);

        Constants.MerchantType merchant = Constants.MerchantType.FD_PAYMODE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("3010")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabPPBL().isVisible();
        cashierPage.linkedFDBalance().waitUntilVisible();
        cashierPage.linkedFDBalance().assertVisible();
        cashierPage.redemptionTextLabel().assertContainsText("₹10 from your Fixed Deposit");
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-35443")
    @Parameters({"theme"})
    @Test(description = "Verify when PPBL balance + FD balance amount is less than order amount then FD Balance should not be visible")
    public void verifying_FD_BalanceIsNotVisible(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.FD_PAYMODE;
        User user = userManager.getForRead(Label.PPBL);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("4010")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabPPBL().isVisible();
        cashierPage.linkedFDBalance().assertNotVisible();
        cashierPage.redemptionTextLabel().assertNotVisible();
    }

    @Owner(RAJKUMAR)
    @Feature("PGP-34686")
    @Parameters({"theme"})
    @Test(description = "verify that it ask for yes and no when we click on backButton")
    public void VerifyYesNoComingAfterClickOnBackButton(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setRoot("#test_43");
        config.setAdditionalProperty("backButton" , "true");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.pause(3);
        cashierPage.getBackButton().click();
        cashierPage.pause(3);
        Assertions.assertThat(cashierPage.PressNoForCancelTxn().getText()).isEqualTo("No");
        Assertions.assertThat(cashierPage.PressYesForCancelTxn().getText()).isEqualTo("Yes");
    }

    @Owner(RAJKUMAR)
    @Feature("PGP-34686")
    @Parameters({"theme"})
    @Test(description = "verify the responce when we click yes for cancel txn. after click on backButton")
    public void VerifyResponceWhenClickYesForCancelTxn(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.setRoot("#test_43");
        config.setAdditionalProperty("backButton" , "true");
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.pause(3);
        cashierPage.getBackButton().click();
        cashierPage.pause(3);
        cashierPage.PressYesForCancelTxn().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespCode("141")
                .validateRespMsg("User has not completed transaction.")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();

    }

    @Owner(SRINIVAS)
    @Feature("PGPUI-1815")
    @Parameters({"theme"})
    @Test(description = "JS Checkout - Adding MID to config object")
    public void Verify_txn_via_child_mid(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);

        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Parent_Mid)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Child_Mid)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);

        config.merchant.setMid(Constants.MerchantType.Child_Mid.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.payBy(Constants.PayMode.NB);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO_childMID.getBody().getMid())
                .validateOrderId(initTxnDTO_childMID.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO_childMID.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateCheckSum(Child_Mid.getKey())
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO_childMID.getBody().getMid(), initTxnDTO_childMID.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO_childMID.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO_childMID.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName(Constants.Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO_childMID.getBody().getMid())
                .validatePaymentMode("NB")
                .validateTxnDate(new Date())
                .AssertAll();

    }


    @Owner(RAJKUMAR)
    @Feature("PGP-22827")
    @Parameters({"theme"})
    @Test(description = "verify the pcf amount on paymode and pcf text under payButton when user is not logged in")
    public void verifyPCFAndTextForNonLoggedInFlow(@Optional("checkoutjs_web_revamp") String theme) throws IOException{
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.FLAT_PCF)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB);
        Assertions.assertThat(cashierPage.convenienceFeeLabel().getText()).isEqualTo("5.57 Convenience fee");
        Assertions.assertThat(cashierPage.getConFeeTextOnPayButton().getText()).isEqualTo("It includes Convenience charges");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICINB.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("NB")
                .validateTxnDate(new Date())
                .AssertAll();
    }


    @Owner(RAJKUMAR)
    @Feature("PGP-22827")
    @Parameters({"theme"})
    @Test(description = "verify the pcf amount on paymode and pcf text under payButton when user is logged in")
    public void verifyPCFAndTextForLoggedInFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(),
                paymentDTO.getCreditCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PCF_VERIFFY)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabSavedCard().click();
        Assertions.assertThat(cashierPage.convenienceFeeLabel().getText()).isEqualTo("2.36 Convenience fee");
        Assertions.assertThat(cashierPage.getConFeeTextOnPayButton().getText()).isEqualTo("It includes Convenience charges");
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(PAYAL)
    @Feature("PGP-35983")
    @Parameters({"theme"})
    @Test(description = "Verify response of FPO V5, with groupedMerchantPayOption")
    public void ValidateInstrumentCategorization(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.POSTPAID);
       // SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), PGOnly)
                .setTxnValue("1").build();
        // WalletHelpers.modifyBalance(user, 10.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.paytm_featured")).contains("Paytm Balance","PAYTM_DIGITAL_CREDIT");
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.other_options")).contains("Debit Card","Credit Card","BHIM UPI","Net Banking");
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption")).contains("savedInstruments");
        softly.assertThat(fetchPaymentOptionsJson.getString("body")).contains("merchantPayOption");
        softly.assertAll();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.getPaytmFeaturedText().assertVisible();
        // cashierPage.newPaymentMethod().assertVisible();
    }


//    @Owner(PAYAL)
//    @Feature("PGP-35983")
//    @Parameters({"theme"})
//    @Test(description = "Verify response of FPO V5 when FF4J UN_GROUPED_PAYMODES_DISABLED is ON", enabled = false)
    //merchantPayOption is picked from property file if priority is not set from boss panel
    public void ValidateInstrumentCategorization_When_FlagEnabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body")).doesNotContain("merchantPayOption");
        softly.assertAll();
    }

    @Owner(RAJKUMAR)
    @Feature("PGP-31542")
    @Parameters({"theme"})
    @Test(description = "verify solutionWiseMDr value is API for online flow checkoutjs txn.")
    public void VerifySolutionWiseMdrForOnlineTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.NO_CHANGE)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFCBANK.toString())
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCheckSum(Constants.MerchantType.NO_CHANGE.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" " + LocalConfig.THEIA_FACADE_LOGS +
                " | grep \"ACQUIRING_INQUIRE_WITH_ACQ_ID\" | " + "grep \"RESPONSE\" | " + "grep \"feeRateFactorsInfo\"";
        String theiaFacadeLogs=getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assert.assertTrue(theiaFacadeLogs.contains("feeRateFactorsInfo\":\"{\\\"solutionWiseMdr\\\":\\\"API\\"));
    }
    @Owner(GAURAV)
    @Feature("PGP-35219")
    @Parameters({"theme"})
    @Test(description = "Validate dynamic limit of addnPay via CC on checkoutJS and complete txn")
    public void validateAddnPayViaCCDynamicLimit_CWEB(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDnPAY_CCLIMIT)
                .setTxnValue("15000").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.getAddnPayLimitErrorOnCheckoutJS().waitUntilVisible();
        Assertions.assertThat(cashierPage.getAddnPayLimitErrorOnCheckoutJS().getText()).contains(ADDnPAY_LIMIT_MSG);

        String paymentDecisionMaker =  "grep \"PAYMODE_DECISION_MAKER_TASK" +"\" "+ LocalConfig.THEIA_FACADE_LOGS + " | grep \"RESPONSE\"" + " | grep \"rejectMsg\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, paymentDecisionMaker);
        Assertions.assertThat(theiaFacadeLogs).contains(ADDnPAY_LIMIT_MSG);
        cashierPage.retryBtnPopupClosedByUser().waitUntilClickable();
        cashierPage.retryBtnPopupClosedByUser().click();
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DC);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();

    }

    @Owner(GAURAV)
    @Feature("PGP-35219")
    @Parameters({"theme"})
    @Test(description = "Validate dynamic limit of addnPay via CC on checkoutJS anc complete txn")
    public void validateAddnPayViaCCDynamicLimit_CWAP(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDnPAY_CCLIMIT)
                .setTxnValue("15000").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO1 = new PaymentDTO().setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO1);
        cashierPage.getAddnPayLimitErrorOnCheckoutJS().waitUntilVisible();
        Assertions.assertThat(cashierPage.getAddnPayLimitErrorOnCheckoutJS().getText()).contains(ADDnPAY_LIMIT_MSG);


        String paymentDecisionMaker =  "grep \"PAYMODE_DECISION_MAKER_TASK" +"\" "+ LocalConfig.THEIA_FACADE_LOGS + " | grep \"RESPONSE\"" + " | grep \"rejectMsg\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, paymentDecisionMaker);
        Assertions.assertThat(theiaFacadeLogs).contains(ADDnPAY_LIMIT_MSG);

        cashierPage.retryBtnPopupClosedByUser().click();
        PaymentDTO paymentDTO = new PaymentDTO().setDebitCardNumber(PaymentDTO.DC);
        cashierPage.payBy(Constants.PayMode.DC,paymentDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();
    }

    @Feature("PGP-30181")
    @Parameters({"theme"})
    @Test(description = "verify FF4J flag ture and multipleWindowWebview false, FPO will return isHtmlToBeRenderedForBlinkCheckout true")
    public void VerifyWebviewSupportOnJS_When_FlagEnabled(@Optional("checkoutjs_wap_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.WebviewSupportOnJS)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setmultipleWindowWebview(false);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.isHtmlToBeRenderedForBlinkCheckout")).isTrue();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateCheckSum(Constants.MerchantType.WebviewSupportOnJS.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Owner(PAYAL)
    @Feature("PGP-30181")
    @Parameters({"theme"})
    @Test(description = "verify FF4J flag false and multipleWindowWebview false, FPO will return isHtmlToBeRenderedForBlinkCheckout true")
    public void VerifyWebviewSupportOnJS_When_FlagDisabled(@Optional("checkoutjs_wap_revamp") String theme) throws IOException, InterruptedException {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .setTxnValue("100").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setmultipleWindowWebview(false);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        fetchPaymentOptionsDTO.getHead().setWorkFlow("checkout");
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getBoolean("body.isHtmlToBeRenderedForBlinkCheckout")).isFalse();

    }
    @Owner(SRINIVAS)
    @Feature("PGP-32217")
    @Parameters({"theme"})
    @Test(description ="load cashier page and do txn via CC and cancel txn by choosing Do not want to buy product anymore after this pop up with text user has closed the bank popup is displayed or not")
    public void Verify_text_is_displayed_after_canceling_txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DebitCard)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        cashierPage.closePMDetailBtn().click();
        cashierPage.waitUntilLoads();
        cashierPage.donotwanttobuyproductorserviceanymore().click();
        cashierPage.submitoption().click();
        cashierPage.waitUntilLoads();
        cashierPage.canceltxntext().assertVisible();
    }
    @Feature("PGP-32218")
    @Parameters({"theme"})
    @Test(description = "load cashier page and do txn via CC and cancel txn by choosing Do not have access to registered mobile number after this pop up with text user has closed the bank popup is displayed or not")
    //direct theme should be weight be 1.0 to run this case and pref nativeJsonRequest= Y
    //nativeOtpSupported=Y should be enabled.
    public void Verify_text_user_has_closed_the_bank_popup_is_displayed_after_canceling_txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DebitCard)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO=new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        cashierPage.closePMDetailBtn().click();
        cashierPage.waitUntilLoads();
        cashierPage.donothaveaccesstoregisteredmobilenumber().click();
        cashierPage.submitoption().click();
        cashierPage.waitUntilLoads();
        cashierPage.canceltxntext().assertVisible();
        //direct theme should be 1.0
    }

//     @Owner(AAYUSH)
//     @Feature("PGP-31300")
//     @Parameters({"theme"})
//     @Test(description = "Verify success transaction of FD as Paymode",enabled = false)
    public void verifying_success_FD_txn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
         User user=userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.FD_PAYMODE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("3001")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        paymentDTO.setPasscode("3315");
        cashierPage.payBy(Constants.PayMode.PPBL,paymentDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(GAURAV)
    @Feature("PGP-20493")
    @Parameters({"theme"})
    @Test(description = "Validate JS Checkout Enhancements - Direct OTP on cashier page")
    public void validateDirectOTPonCashierPage(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.HDFO)
                .setTxnValue("2").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);

        String parent=DriverManager.getDriver().getWindowHandle();
        Set<String>s=DriverManager.getDriver().getWindowHandles();
        Iterator<String> I1= s.iterator();
        while(I1.hasNext())
        {
            String child_window=I1.next();
            if(!parent.equals(child_window)) {
                DriverManager.getDriver().switchTo().window(child_window);
            }}

        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
        directBankOTPPage.assertVisible();
        directBankOTPPage.submitOtp(PaymentDTO.OTP);
        DriverManager.getDriver().switchTo().window(parent);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validatePaymentMode("CC")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .assertAll();
    }

    @Owner(PRIYANSHI)
    @Feature("PGP-37041")
    @Parameters({"theme"})
    @Test(description = "Verifying Phone number and Email in Initiate Txn response ")
    public void verifying_phone_number_and_email(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.MASKED_MID;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
      DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
      cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getCreditCardNumber());
      cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
      cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
      cashierPage.textBoxCVVNumber().waitUntilVisible();
      cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
      DriverManager.getDriver().switchTo().defaultContent();
      cashierPage.pause(2);
      cashierPage.buttonPGPayNow().click();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" "+ LocalConfig.THEIA_LOGS;
        System.out.println(grepcmd);
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        System.out.println("\n\n\ntheiaLogs : "+theiaLogs);
        Assertions.assertThat(theiaLogs).contains("mobileNo=**********");
        Assertions.assertThat(theiaLogs).containsPattern("email=\\*{2,}");
    }

    @Owner(CHETAN)
    @Feature("PGP-36238")
    @Parameters({"theme"})
    @Test(description = "Verify if hidePaytmBranding is false then paytm pg branding should be visible in footer of cashier page")
    public void verifying_hidePaytmBranding_false(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(false);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        Assertions.assertThat(cashierPage.getFooterText().getText()).isEqualTo("100% Secure Payments Powered by");
        Assert.assertTrue(cashierPage.footerLOGO().isElementPresent());
    }

    @Owner(CHETAN)
    @Feature("PGP-36238")
    @Parameters({"theme"})
    @Test(description = "Verify if hidePaytmBranding is true then paytm pg branding should not be visible in footer of cashier page")
    public void verifying_hidePaytmBranding_true(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        Assert.assertFalse(cashierPage.getFooterText().isElementPresent());
        Assert.assertFalse(cashierPage.footerLOGO().isElementPresent());
    }

    @Owner(CHETAN)
    @Feature("PGP-36238")
    @Parameters({"theme"})
    @Test(description = "Verify if hidePaytmBranding is null then paytm pg branding should be visible in footer of cashier page")
    public void verifying_hidePaytmBranding_Null(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(null);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        Assertions.assertThat(cashierPage.getFooterText().getText()).isEqualTo("100% Secure Payments Powered by");
        Assert.assertTrue(cashierPage.footerLOGO().isElementPresent());
    }


    @Owner(PUSPA)
    @Feature("PGP-34467")
    @Parameters({"theme"})
    @Test(description = "Verify EmiInfo details in ExtendInfo object in ACQUIRING_PAY_ORDER Api")
    public void verifyStandardEMIEmiInfo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMISubvention;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO=new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        SoftAssertions softly = new SoftAssertions();
        String grepcmd = "grep \"" + responsePage.textOrderID().getText() + "\" " + LocalConfig.THEIA_FACADE_LOGS + " | grep \"ACQUIRING_PAY_ORDER\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);

        softly.assertThat(theiaFacadeLogs).contains("\\\\\\\"TXN_ID\\\\\\\":\\\\\\\""+responsePage.textTxnID().getText()+"\\\\\\\"")
                .contains("\\\\\\\"cardType\\\\\\\":\\\\\\\"CREDIT_CARD\\\\\\\"")
                .contains("\\\\\\\"MID\\\\\\\":\\\\\\\""+responsePage.textMID().getText()+"\\\\\\\"")
                .contains("\\\\\\\"cardNo\\\\\\\":\\\\\\\"0336\\\\\\\"")
                .contains("\\\\\\\"cardToken\\\\\\\":\\\\\\\"\\\\\\\"")
                .contains("\\\\\\\"merchantName\\\\\\\":\\\\\\\"VishalF\\\\\\\"")
                .contains("\\\\\\\"cardIssuer\\\\\\\":\\\\\\\"HDFC\\\\\\\"")
                .contains("\\\\\\\"bank\\\\\\\":\\\\\\\"HDFC\\\\\\")
                .contains("\\\\\\\"emiAmount\\\\\\\":\\\\\\\"6.69\\\\\\")
                .contains("\\\\\\\"ORDER_ID\\\\\\\":\\\\\\\""+responsePage.textOrderID().getText()+"\\\\\\")
                .contains("\\\\\\\"emiMonths\\\\\\\":\\\\\\\"3\\\\\\")
                .contains("\\\\\\\"emiInterestRate\\\\\\\":\\\\\\\"2.0\\\\\\")
                .contains("\\\\\\\"planID\\\\\\\":\\\\\\\"HDFC|3\\\\\\");
                softly.assertAll();

    }

    @Owner(GAURAV)
    @Feature("PGP-38084")
    @Parameters({"theme"})
    @Test(description = "Verifying UPI Collect is diabled for txn amt > 2K on non verified merchant")
    public void verifyUPICollectIsDisabledForGreaterThan2k(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NON_VERIFED)
                .setTxnValue("2001").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.tabSavedUPI(1).assertNotVisible();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

    }

    @Owner(GAURAV)
    @Feature("PGP-38084")
    @Parameters({"theme"})
    @Test(description = "Verifying UPI Collect is enabled for txn amt < 2K on non verified merchant")
    public void verifyUPICollectIsDisabledForLessThan2k(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.SAVEDVPA);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NON_VERIFED)
                .setTxnValue("1998").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().assertVisible();
    //    cashierPage.tabSavedUPI(1).assertVisible();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

    }
    @Owner(CHETAN)
    @Feature("PGP-36669")
    @Parameters({"theme"})
    @Test(description = "Verify paytm pg logo is displayed in header and footer in cashier page checkoutjs flow")
    public void verifying_paytmPgLogo_displayed_onCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(null);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        // verify blue paytm-pg logo is present in cashier page
        Assert.assertTrue(cashierPage.getPaytmLogoBlue().isElementPresent());
        Assert.assertTrue(cashierPage.footerLogoBlue().isElementPresent());
    }

//    @Feature("PGP-26502")
//    @Owner(MAYURI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Hybrid CC and wallet txn in checkoutJS flow having cashback bank offer")
    public void ValidateHybridWalletAndCCTxnWithBulkOffer(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode("").setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID, simplifiedPaymentOffers)
                .setTxnValue("3.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String applyAvailablePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.applyAvailablePromo");
        String promoCode = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode");
        String validatePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.validatePromo");
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        String verifyApplyAvailablePromo = "true";
        String verifyPaymentFlow = "HYBRID";
        String verifyValidatePromo = "true";
        Assertions.assertThat(applyAvailablePromo).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyApplyAvailablePromo);
        Assertions.assertThat(paymentFlowFPO).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyPaymentFlow);
        Assertions.assertThat((String) null).describedAs("promoCode from FPO").isEqualTo(promoCode);
        Assertions.assertThat(verifyValidatePromo).describedAs("validatePromo from FPO").isEqualTo(validatePromo);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .validateChildTxnsPresent()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .AssertAll();

    }

//    @Feature("PGP-26502")
//    @Owner(MAYURI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Hybrid CC and wallet txn in checkoutJS flow having Discount bank offer")
    public void ValidateHybridWalletAndCCTxnWithDiscount(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(Constants.MessageAssert.PROMO_DISCOUNT.toString()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID, simplifiedPaymentOffers)
                .setTxnValue("3.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        PaymentDTO paymentDTO = new PaymentDTO();
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String applyAvailablePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.applyAvailablePromo");
        String promoCode = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode");
        String validatePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.validatePromo");
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        String verifyApplyAvailablePromo= "true";
        String verifyPaymentFlow= "HYBRID";
        String verifyValidatePromo="true";
        Assertions.assertThat(applyAvailablePromo).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyApplyAvailablePromo);
        Assertions.assertThat(paymentFlowFPO).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyPaymentFlow);
        Assertions.assertThat(Constants.MessageAssert.PROMO_DISCOUNT.toString()).describedAs("promoCode from FPO").isEqualTo(promoCode);
        Assertions.assertThat(verifyValidatePromo).describedAs("validatePromo from FPO").isEqualTo(validatePromo);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        String discountText = cashierPage.getBankOfferDiscountMsg().getText();
        Assertions.assertThat(discountText).isEqualTo("₹0.15 discount applicable");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("2.95")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChildTxnsPresent()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateTxnAmount("2.95")
                .AssertAll();

    }

//    @Feature("PGP-26502")
 //   @Owner(MAYURI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Hybrid saved CC and wallet txn in checkoutJS flow having cashback bank offer")
    public void ValidateHybridWalletAndSavedCCTxnWithCashback(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        String card1 = "4718650100010336";
        SavedCardHelpers.addCard(user, "06", "2022", card1);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        Promo promo = new Promo();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID, simplifiedPaymentOffers)
                .setTxnValue("3.00")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String applyAvailablePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.applyAvailablePromo");
        String promoCode = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode");
        String validatePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.validatePromo");
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        String verifyApplyAvailablePromo= "true";
        String verifyPaymentFlow= "HYBRID";
        String verifyValidatePromo="true";
        Assertions.assertThat(applyAvailablePromo).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyApplyAvailablePromo);
        Assertions.assertThat(paymentFlowFPO).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyPaymentFlow);
        Assertions.assertThat(promo.getName()).describedAs("promoCode from FPO").isEqualTo(promoCode);
        Assertions.assertThat(verifyValidatePromo).describedAs("validatePromo from FPO").isEqualTo(validatePromo);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String discountText = cashierPage.getBankOfferDiscountMsg().getText();
        cashierPage.tabSavedCard();
        Assertions.assertThat(discountText).isEqualTo("₹0.15 cashback applicable");
        cashierPage.payBy(Constants.PayMode.SAVED_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChildTxnsPresent()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .AssertAll();

    }

//    @Feature("PGP-26502")
//    @Owner(MAYURI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Hybrid NB and wallet txn in checkoutJS flow having Discount bank offer")
    public void ValidateHybridWalletAndNBTxnWithDiscount(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(Constants.MessageAssert.PROMO_DISCOUNT.toString()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID, simplifiedPaymentOffers)
                .setTxnValue("3.00")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String applyAvailablePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.applyAvailablePromo");
        String promoCode = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode");
        String validatePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.validatePromo");
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        String verifyApplyAvailablePromo= "true";
        String verifyPaymentFlow= "HYBRID";
        String verifyValidatePromo="true";
        Assertions.assertThat(applyAvailablePromo).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyApplyAvailablePromo);
        Assertions.assertThat(paymentFlowFPO).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyPaymentFlow);
        Assertions.assertThat(Constants.MessageAssert.PROMO_DISCOUNT.toString()).describedAs("promoCode from FPO").isEqualTo(promoCode);
        Assertions.assertThat(verifyValidatePromo).describedAs("validatePromo from FPO").isEqualTo(validatePromo);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.pause(2);
        String discountText = cashierPage.getBankOfferDiscountMsg().getText();
        Assertions.assertThat(discountText).isEqualTo("₹0.15 discount applicable");
        PaymentDTO paymentDTO=new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount("2.95")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChildTxnsPresent()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();

        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateTxnAmount("2.95")
                .AssertAll();

    }

    @Owner(AJEESH)
    @Feature("PGP-35618")
    @Parameters({"theme"})
    @Test(description = "Verify that User is Navigated to Dynamic URL and DB value is also updated")
    public void PGP_35618_VerifythatUserisNavigatedtoDynamicURL(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String txnMaxAmount = "100";
        String callBackURL = "www.google.com";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(txnMaxAmount)
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setCallbackUrl(callBackURL)
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String subsId = initTxnResponse.getBody().getSubscriptionId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WAP_REVAMP) || theme.equalsIgnoreCase(Constants.Theme.CHECKOUTJS_WEB_REVAMP)) {
            cashierPage.tabBankMandate().click();
        }
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);

        bankMandatePage.confirmButton().click();
        cashierPage.pause(5);

        String DynamicURL = cashierPage.getChildWindowURL();
        Assertions.assertThat(DynamicURL.contains(callBackURL));
        String query = "SELECT callback_url  FROM PGPDB.bank_mandate_info bmi WHERE subscription_id ='" + subsId + "'";
        Assertions.assertThat((DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query).toString()).contains(callBackURL));

    }
    @Owner(AJEESH)
    @Feature("PGP-36505")
    @Parameters({"theme"})
    @Test(description = "Verify MID is null passed for getTxnStatus and getPaymentStatus API and NullPointerException should not occur")
    public void PGP_36505_VerifyNoNullPointerExceptionforMerchantStatusAPIs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.NULL_MERCHANT;
        String merch = merchant.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .setTxnValue("2").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        String OrderID = initTxnDTO.getBody().getOrderId();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.CC);

        TxnStatus txnStatus = new TxnStatus(merch, OrderID);
        txnStatus.executeUntilNotPending();
        txnStatus.validateRespMsg("MID is invalid");
        txnStatus.validateRespCode("335");

        GetPaymentStatusDTO getPaymentStatusDTO = new GetPaymentStatusDTO(new GetPaymentStatusDTO.Builder(OrderID, merchant));
        GetPaymentStatus getPaymentStatus = new GetPaymentStatus(getPaymentStatusDTO);
        Response response = getPaymentStatus.execute();
        JsonPath jsonPath = response.jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultCode")).isEqualToIgnoringCase("335");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualToIgnoringCase("Invalid merchant Id.");

    }
//    @Feature("PGP-26502")
//    @Owner(MAYURI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Hybrid UPI and wallet txn in checkoutJS flow having Cashback bank offer")
    public void ValidateHybridWalletAndUPITxnWithCashback(@Optional("checkoutjs_wap_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID, simplifiedPaymentOffers)
                .setTxnValue("3.00")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String applyAvailablePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.applyAvailablePromo");
        String promoCode = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode");
        String validatePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.validatePromo");
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        String verifyApplyAvailablePromo= "true";
        String verifyPaymentFlow= "HYBRID";
        String verifyValidatePromo="true";
        Assertions.assertThat(applyAvailablePromo).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyApplyAvailablePromo);
        Assertions.assertThat(paymentFlowFPO).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyPaymentFlow);
        Assertions.assertThat(promo.getName()).describedAs("promoCode from FPO").isEqualTo(promoCode);
        Assertions.assertThat(verifyValidatePromo).describedAs("validatePromo from FPO").isEqualTo(validatePromo);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.scrollToElement(cashierPage.tabUPI());
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().click();
        cashierPage.textBoxVPA().clearAndType(new PaymentDTO().getVpa());
        cashierPage.verifyVPALinkText().click();
        String discountText = cashierPage.getBankOfferDiscountMsg().getText();
        Assertions.assertThat(discountText).isEqualTo("₹0.15 cashback applicable");
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChildTxnsPresent()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .AssertAll();

    }

//    @Feature("PGP-26502")
//    @Owner(MAYURI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Hybrid NB and wallet txn in checkoutJS flow having cashback bank offer")
    public void ValidateHybridWalletAndNBTxnWithCashback(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("true");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID, simplifiedPaymentOffers)
                .setTxnValue("3.00")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String applyAvailablePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.applyAvailablePromo");
        String promoCode = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.promoCode");
        String validatePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.validatePromo");
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        String verifyApplyAvailablePromo= "true";
        String verifyPaymentFlow= "HYBRID";
        String verifyValidatePromo="true";
        Assertions.assertThat(applyAvailablePromo).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyApplyAvailablePromo);
        Assertions.assertThat(paymentFlowFPO).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyPaymentFlow);
        Assertions.assertThat(promo.getName()).describedAs("promoCode from FPO").isEqualTo(promoCode);
        Assertions.assertThat(verifyValidatePromo).describedAs("validatePromo from FPO").isEqualTo(validatePromo);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabNetBanking().click();
        cashierPage.pause(2);
        String discountText = cashierPage.getBankOfferDiscountMsg().getText();
        Assertions.assertThat(discountText).isEqualTo("₹0.15 cashback applicable");
        PaymentDTO paymentDTO=new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChildTxnsPresent()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateBankTxnId(Constants.ValidationType.EMPTY)
                .AssertAll();

    }

//    @Feature("PGP-26502")
//    @Owner(MAYURI)
//    @Parameters({"theme"})
//    @Test(enabled = false, description = "Verify Hybrid CC and wallet txn when validate promo=false in checkoutJS flow having Discount bank offer")
    public void ValidatePromoFalseHybridWalletAndCCTxnWithCashback(@Optional("checkoutjs_web_revamp")String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Promo promo = new Promo();
        SimplifiedPaymentOffers simplifiedPaymentOffers = new SimplifiedPaymentOffers();
        simplifiedPaymentOffers.setPromoCode(promo.getName()).setApplyAvailablePromo("true").setValidatePromo("false");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_HYBRID, simplifiedPaymentOffers)
                .setTxnValue("3.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        double amountToBeRetainedInWallet = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO = new PaymentDTO();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String applyAvailablePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.applyAvailablePromo");
        String validatePromo = fetchPaymentOptionsJson.getString("body.simplifiedPaymentOffers.validatePromo");
        String paymentFlowFPO = fetchPaymentOptionsJson.getString("body.paymentFlow");
        String verifyApplyAvailablePromo= "true";
        String verifyPaymentFlow= "HYBRID";
        String verifyValidatePromo="false";
        Assertions.assertThat(applyAvailablePromo).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyApplyAvailablePromo);
        Assertions.assertThat(paymentFlowFPO).describedAs("applyAvailable Promo from FPO").isEqualTo(verifyPaymentFlow);
        Assertions.assertThat(verifyValidatePromo).describedAs("validatePromo from FPO").isEqualTo(validatePromo);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        String discountText = cashierPage.getBankOfferDiscountMsg().getText();
        Assertions.assertThat(discountText).isEqualTo("₹0.15 cashback applicable");
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_CREDIT_CARD));

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("HYBRID")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateChildTxnsPresent()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(initTxnDTO.getBody().getMid())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .AssertAll();
    }

    @Owner(VAIBHAV)
    @Feature("PGP-37786")
    @Test(description = "Validate Null pointer exception should not come while initiating txn")
    public void ValidateTxn_AvoidNullPointerException(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly).setTxnValue("2").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO= new PaymentDTO();
        cashierPage.signin("8512005349","888888");
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        String orderid= initTxnDTO.orderFromBody();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String Logs =LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,orderid);
          Assertions.assertThat(Logs).doesNotContain("NullPointerException");
    }

    @Owner(PUSPA)
    @Feature("PGP-33611")
    @Parameters({"theme"})
    @Test(description = "Verify successful txn using new card flow on checkoutJS")
    public void verifyStandardEMIForNewCardFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }
    //String ui_msg = "Please enter a valid VPA of the form username@bank";
     String new_ui_msg= "Invalid VPA, Try Again";
  //  String log_msg = "Invalid UPI address";
    public void ui_vpa_msg(CashierPage cashierPage) throws Exception{
        cashierPage.textBoxVPA().waitUntilClickable();
        cashierPage.textBoxVPA().click();
        cashierPage.textBoxVPA().clearAndType("11invalid@pay11");
        cashierPage.payButton().click();
        String d1= cashierPage.vpaerrormsg().getText();
        Assertions.assertThat(d1).isEqualTo(new_ui_msg);
    }
    public void theia_facade_vpa_msg(CashierPage cashierPage , String orderid) throws Exception{
        cashierPage.textBoxVPA().waitUntilVisible();
        cashierPage.textBoxVPA().clearAndType("11invalid@pay11");
        cashierPage.payButton().click();
        cashierPage.vpaerrormsg().waitUntilVisible();
        String d1= cashierPage.vpaerrormsg().getText();
        Assertions.assertThat(d1).isEqualTo(Constants.MessageAssert.INVALID_VPA.toString());
        //response msg returned from UPI_SECURE Component
        String grepcmd = "grep \"" + orderid + "\" " + LocalConfig.THEIA_FACADE_LOGS+
            " | grep \"UPI_SECURE\" | " + "grep \"RESPONSE\"";
        String theiaFacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFacadeLogs).contains("\""+Constants.MessageAssert.INVALID_UPI.toString()+"\"");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for NONE payment flow")
    public void validate_vpa_msg(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = AddnPay;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue("20.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPIId().click();
        }
        ui_vpa_msg(cashierPage);
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-37186")
//    @Parameters({"theme"})
//    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for AddnPay payment flow",enabled = false)
    // UPI with wallet is not supported
    public void validate_vpa_msg_AddnPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().unCheck();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPIId().click();
        }
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for Subscription payment flow")
    public void validate_vpa_msg_Subscription(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPI().click();
        }
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid UPI ID\"\" should be displayed after entering an invalid UPIID on UI and theia_facade logs as well for none payment flow")
    public void validate_vpa_msg_logs(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI;
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPIId().click();
        }
        theia_facade_vpa_msg(cashierPage,orderid);
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-37186")
//    @Parameters({"theme"})
//    @Test(description = "Verify error message \"Invalid UPI ID\"\" should be displayed after entering an invalid UPIID on UI and theia_facade logs as well for addnpay payment flow",enabled = false)
    //wallet with UOI is not supported
    public void validate_vpa_msg_logs_addnpay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2.00")
                .build();
        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPIId().click();
        }
        theia_facade_vpa_msg(cashierPage,orderid);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid UPI ID\"\" should be displayed after entering an invalid UPIID on UI and theia_facade logs as well for subscription payment flow")
    public void validate_vpa_msg_logs_subscription(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPIId().click();
        }
        theia_facade_vpa_msg(cashierPage,orderid);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for NONE payment flow")
    public void Nonlogeedin_validate_vpa_msg(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null ,merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPI().click();
        }
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Please enter a valid UPI ID\" should be displayed after entering an invalid \"UPI ID\" and clicking on pay button for Subscription payment flow")
    public void Nonloggedin_validate_vpa_msg_Subscription(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPIId().click();
        }
        ui_vpa_msg(cashierPage);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid UPI ID\"\" should be displayed after entering an invalid UPIID on UI and theia_facade logs as well for none payment flow")
    public void Nonloggedin_validate_vpa_msg_logs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.AddnPay;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null,merchant)
                .setTxnValue("2.00")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPI().click();
        }
        theia_facade_vpa_msg(cashierPage,orderid);
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37186")
    @Parameters({"theme"})
    @Test(description = "Verify error message \"Invalid UPI ID\"\" should be displayed after entering an invalid UPIID on UI and theia_facade logs as well for subscription payment flow")
    public void Nonloggedin_validate_vpa_msg_logs_subscription(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("UPI")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        String orderid= initTxnDTO.orderFromBody();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        if(theme.equals(Constants.Theme.CHECKOUTJS_WAP_REVAMP)){
            cashierPage.tabUPIId().click();
        }
        theia_facade_vpa_msg(cashierPage,orderid);
    }


    @Owner(ASHISH_JASWAL)
    @Feature("PGP-37658")
    @Parameters({"theme"})
    @Test(description = "Verify that User is Navigated to Dynamic URL for Bank Mandate Txn")
    public void PGP_37658_VerifythatUserisNavigatedtoDynamicURL(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_BM;
        String txnMaxAmount = "100";
        String callBackURL = "www.google.com";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(txnMaxAmount)
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setCallbackUrl(callBackURL)
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabBankMandate().click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().click();
        cashierPage.waitUntilLoads();

        String DynamicURL = cashierPage.getChildWindowURL();
        Assertions.assertThat(DynamicURL.contains(callBackURL));
    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-37658")
    @Parameters({"theme"})
    @Test(description = "Verify that User is Navigated to BM URL for Bank Mandate Txn")
    public void PGP_37658_VerifythatUserisNavigatedtoBMURL(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE_BM;
        String txnMaxAmount = "100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("10")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setWebsiteName("retail")
                .setSubscriptionMaxAmount(txnMaxAmount)
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setCallbackUrl("")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabBankMandate().click();
        cashierPage.buttonPGPayNow().click();
        BankMandatePage bankMandatePage = BankMandatePageFactory.getBankMandatePage(theme);
        bankMandatePage.confirmButton().click();
        cashierPage.waitUntilLoads();

        String ResponseURL = cashierPage.getChildWindowURL();
        Assertions.assertThat(ResponseURL.contains("https://www.spacex.com/"));
    }

    @Owner(PRIYANKA)
    @Feature("PGP-38189")
    @Parameters({"theme"})
    @Test(description = "Verify the QR details is showing up in the FPO response for the second time")
    public void verifyQRDetailsShowingInFPOResponse(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.pause(3);
        cashierPage.imgLoginQRCode().isElementPresent();

        MerchantConfig config1 = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config1.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config1);
        CashierPage cashierPage1 = CashierPageFactory.getCashierPage(theme);
        cashierPage1.waitUntilLoads();
        cashierPage1.pause(3);
        cashierPage1.imgLoginQRCode().isElementPresent();
    }

	@Owner(CHETAN)
    @Feature("PGP-36683")
    @Parameters({"theme"})
    @Test(description = "Verify for closed wallet user RBI guidelines error message should be shown")
    public void verify_closed_wallet_user_error_message(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        // User sso token should be disabled using activateUserWallet api set field "usrAcctExpired":false
        // Error message: "Your wallet has been deactivated as mandated by RBI" - in cashier page we should see this error for wallet
        User user = userManager.getForRead(Label.DEACTIVATEDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        // Verify the error message
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("Your wallet has been deactivated as mandated by RBI.Know more");
        // verify wallet cannot be selected
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        // Verify know more is clickable and assert the message in know more window
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("As per RBI guidelines, all wallet accounts with no transactions in the past one year have been deactivated.");
        // verify from debit/credit/net any banking transaction should be successful without using wallet
        cashierPage.knowMoreLinkPopup().click();
        cashierPage.payBy(Constants.PayMode.NB);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(CHETAN)
    @Feature("PGP-38125")
    @Parameters({"theme"})
    @Test(description = "Verify wallet deactivated message is shown when all the paymodes are explicitly disabled except wallet")
    public void verify_closed_wallet_user_error_message_when_all_paymodes_disabled_except_wallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        // User sso token should be disabled using activateUserWallet api set field "usrAcctExpired":false
        // Error message: "Your wallet has been deactivated as mandated by RBI" - in cashier page we should see this error for wallet
        User user = userManager.getForRead(Label.DEACTIVATEDUSER);
        // Disable all the pay modes except wallet, and verify wallet should be disabled
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("DEBIT_CARD"),
                        new DisablePaymentMode().setMode("NET_BANKING"),
                        new DisablePaymentMode().setMode("CREDIT_CARD")})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        // Verify the error message
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("Your wallet has been deactivated as mandated by RBI.Know more");
        // verify wallet cannot be selected
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        // Verify know more is clickable and assert the message in know more window
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("As per RBI guidelines, all wallet accounts with no transactions in the past one year have been deactivated.");

    }

    @Owner(Constants.Owner.PUSPA)
    @Feature("PGP-38781")
    @Parameters({"theme"})
    @Test(description = "Emi Subvention object is non mandatory in CheckoutJS configuration for TXN_TOKEN flow-AmountBased/ItemBased")
    public void removeEmiSubventionObjectfromCheckoutJSConfig(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI;
        SimplifiedSubvention simplifiedSubvention = new SimplifiedSubvention("1234", null, "1", null);
        simplifiedSubvention.setselectPlanOnCashierPage(true);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("10")
                .setSimplifiedSubvention(simplifiedSubvention)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }


    @Owner(PRIYANKA)
    @Feature("PGP-39503")
    @Parameters({"theme"})
    @Test(description = "Verify the masked mobile number is displayed in userInfo section of FPOResponse")
    public void verifyMaskedMobileNumberInFPOResponse(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MASKED_MID).
                build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.loginStrip().click();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String masked_mobile = fetchPaymentOptionsJson.getString("body.userInfo.mobile");
        Assertions.assertThat(masked_mobile).contains("XXXXXX");


    }


    @Owner(PRIYANKA)
    @Feature("PGP-39503")
    @Parameters({"theme"})
    @Test(description = "Verify the masked mobile number is displayed in sendOTP API")
    public void verifyMaskedMobileNumberInSendOTPAPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.MASKED_MID).
                build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
      //  cashierPage.loginStrip().click();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String masked_mobile = fetchPaymentOptionsJson.getString("body.userInfo.mobile");

        SendOTP mobile_Number_mask = new SendOTP(txnToken,masked_mobile,initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId());
        JsonPath SendOTPAPIJson = mobile_Number_mask.execute().jsonPath();
        String masked_mobile_sentOTP = SendOTPAPIJson.getString("body.mobileNumber");
        Assertions.assertThat(masked_mobile).contains("XXXXXX");


    }
    @Owner(PUSPA)
    @Feature("PGP-39211")
    @Parameters({"theme"})
    @Test(description = "Validating rupee symbol in pcf flow for NB")
    public void verifyRupeeSymbolInPCFFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.NETBANK_PCF;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().click();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.getRupeeSymbol().getText()).isEqualTo("₹");
    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-36557")
    @Parameters({"theme"})
    @Test(description = "Configurable Icons for Login via OTP and Login via QR for JS Checkout")
    public void PGP_36557_Configurable_Icons_for_Login_via_OTP_and_Login_via_QR_TC15(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC15;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String merchantAccept = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Balance'}.isDisabled.merchantAccept");
        Assertions.assertThat(merchantAccept).contains("true");
        String merchantAccept1 = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Postpaid'}.isDisabled.merchantAccept");
        Assertions.assertThat(merchantAccept1).contains("true");
        String QR =cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QR).contains("Scan QR with Paytm to Login");
        cashierPage.loginStrip().assertNotVisible();
        cashierPage.cards().assertVisible();
        cashierPage.tabUPI().assertVisible();
        cashierPage.loginKnowMoreLink().assertNotVisible();
        cashierPage.otherPaymentOption().assertVisible();
        cashierPage.pause(2);
        cashierPage.imgLoginQRCode().isElementPresent();
        cashierPage.infoStripPaymodes().assertNotVisible();
        String Enabled =cashierPage.enabledPaymodes().getText();
        Assertions.assertThat(Enabled).contains("Pay using Paytm Postpaid, Paytm Wallet or UPI");

    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-36557")
    @Parameters({"theme"})
    @Test(description = "Configurable Icons for Login via OTP and Login via QR for JS Checkout ")
    public void PGP_36557_Configurable_Icons_for_Login_via_OTP_and_Login_via_QR_TC14(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC14;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String QR =cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QR).contains("Scan QR with Paytm to Login");
        cashierPage.loginStrip().assertNotVisible();
        cashierPage.loginKnowMoreLink().assertVisible();
        cashierPage.tabUPI().assertVisible();
        cashierPage.cards().assertNotVisible();
        cashierPage.otherPaymentOption().assertVisible();
        cashierPage.imgLoginQRCode().isElementPresent();
        cashierPage.loginKnowMoreLink().click();
        cashierPage.pause(2);
        String knowMorePaymentInfo = cashierPage.knowMorePaymodeInfo().getText();
        Assertions.assertThat(knowMorePaymentInfo).contains("Merchant has not enabled Paytm Postpaid and Wallet for this Transaction");
        String Enabled =cashierPage.enabledPaymodes().getText();
        Assertions.assertThat(Enabled).contains("Pay using UPI");
    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-36557")
    @Parameters({"theme"})
    @Test(description = "Configurable Icons for Login via OTP and Login via QR for JS Checkout ")
    public void PGP_36557_Configurable_Icons_for_Login_via_OTP_and_Login_via_QR_TC08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC08;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String merchantAccept = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Balance'}.isDisabled.merchantAccept");
        Assertions.assertThat(merchantAccept).contains("true");
        String QR =cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QR).contains("Scan QR with Paytm to Login");
        cashierPage.loginStrip().assertNotVisible();
        cashierPage.cards().assertVisible();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.loginKnowMoreLink().assertVisible();
 //       cashierPage.otherPaymentOption().assertVisible();
        cashierPage.imgLoginQRCode().isElementPresent();
        cashierPage.loginKnowMoreLink().click();
        cashierPage.pause(2);
        String knowMorePaymentInfo = cashierPage.knowMorePaymodeInfo().getText();
        Assertions.assertThat(knowMorePaymentInfo).contains("Merchant has not enabled Postpaid Loan and UPI for this Transaction");
        String Enabled =cashierPage.enabledPaymodes().getText();
        Assertions.assertThat(Enabled).contains("Pay with Paytm Wallet");

    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-36557")
    @Parameters({"theme"})
    @Test(description = "Configurable Icons for Login via OTP and Login via QR for JS Checkout ")
    public void PGP_36557_Configurable_Icons_for_Login_via_OTP_and_Login_via_QR_TC05(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC05;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String merchantAccept = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Balance'}.isDisabled.merchantAccept");
        Assertions.assertThat(merchantAccept).contains("true");
        String merchantAccept1 = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Postpaid'}.isDisabled.merchantAccept");
        Assertions.assertThat(merchantAccept1).contains("true");
        String QR =cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QR).contains("Scan QR with Paytm to Login");
        cashierPage.loginStrip().assertNotVisible();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.cards().assertNotVisible();
        cashierPage.loginKnowMoreLink().assertVisible();
        cashierPage.otherPaymentOption().assertNotVisible();
        cashierPage.imgLoginQRCode().isElementPresent();
        cashierPage.loginKnowMoreLink().click();
        cashierPage.pause(2);
        String knowMorePaymentInfo = cashierPage.knowMorePaymodeInfo().getText();
        Assertions.assertThat(knowMorePaymentInfo).contains("Merchant has not enabled UPI for this Transaction");
        String Enabled =cashierPage.enabledPaymodes().getText();
        Assertions.assertThat(Enabled).contains(Constants.MessageAssert.PAY_PostPaid.toString());

    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-36557")
    @Parameters({"theme"})
    @Test(description = "Configurable Icons for Login via OTP and Login via QR for JS Checkout ")
    public void PGP_36557_Configurable_Icons_for_Login_via_OTP_and_Login_via_QR_TC06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC06;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
//
        String merchantAccept = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Postpaid'}.isDisabled.merchantAccept");
        Assertions.assertThat(merchantAccept).contains("true");
        Assertions.assertThat(cashierPage.imgLoginQRCode().isElementPresent()).isFalse();
        cashierPage.qrCodeCheckoutJSText().assertNotVisible();
        cashierPage.loginStrip().assertVisible();
        cashierPage.loginKnowMoreLink().click();
        cashierPage.pause(2);
        String Info = cashierPage.knowMorePaymodeInfo().getText();
        Assertions.assertThat(Info).contains("Merchant has not enabled Paytm Wallet and UPI for this Transaction");
        String Enabled =cashierPage.noQRPaymodesPresent().getText();
        Assertions.assertThat(Enabled).contains(Constants.MessageAssert.PAY_PostPaid.toString());
        cashierPage.cards().assertVisible();

    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-36557")
    @Parameters({"theme"})
    @Test(description = "Configurable Icons for Login via OTP and Login via QR for JS Checkout ")
    public void PGP_36557_Configurable_Icons_for_Login_via_OTP_and_Login_via_QR_TC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC06;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String merchantAccept = fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find{it.displayName  == 'Paytm Postpaid'}.isDisabled.merchantAccept");
        Assertions.assertThat(merchantAccept).contains("true");
        cashierPage.imgLoginQRCode().assertNotVisible();
        cashierPage.qrCodeCheckoutJSText().assertNotVisible();
        String Login =cashierPage.noQRAvailableText().getText();
        Assertions.assertThat(Login).contains("Pay with Paytm");
        cashierPage.loginStrip().assertVisible();
        cashierPage.tabUPI().assertNotVisible();
        cashierPage.loginKnowMoreLink().click();
        cashierPage.pause(2);
        String Info =cashierPage.knowMorePaymodeInfo().getText();
        Assertions.assertThat(Info).contains("Merchant has not enabled Paytm Wallet and UPI for this Transaction");
        String Enabled =cashierPage.noQRPaymodesPresent().getText();
        Assertions.assertThat(Enabled).contains(Constants.MessageAssert.PAY_PostPaid.toString());
        cashierPage.cards().assertVisible();

    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-36557")
    @Parameters({"theme"})
    @Test(description = "Configurable Icons for Login via OTP and Login via QR for JS Checkout ")
    public void PGP_36557_Configurable_Icons_for_Login_via_OTP_and_Login_via_QR(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC14;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgLoginQRCode().isElementPresent();
        String QR =cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QR).contains("Scan QR with Paytm to Login");
        cashierPage.noQRAvailableText().assertNotVisible();
        cashierPage.tabUPI().assertVisible();
        cashierPage.loginKnowMoreLink().click();
        cashierPage.pause(2);
        String Info =cashierPage.knowMorePaymodeInfo().getText();
        Assertions.assertThat(Info).contains("Merchant has not enabled Paytm Postpaid and Wallet for this Transaction");
        String Enabled =cashierPage.enabledPaymodes().getText();
        Assertions.assertThat(Enabled).contains("Pay using UPI");
        cashierPage.cards().assertNotVisible();

    }

    @Owner(SRINIVAS)
    @Feature("PGP-40036")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and theme is displayed on the checkoutjs cashier page on parent mid without making any customisation on child mid ")
    public void Verify_icici_cobranding_logo_and_theme_is_displayed_on_cashierpage_without_applying_customisation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
      String parentexpectedConfigheadercolor="#e77817";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.ICICI_COBRANDING_PARENT_MID)
             .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.ICICI_COBRANDING_CHILD_MID)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_parentMID,theme);
        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.ICICI_COBRANDING_PARENT_MID,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);

        config.data.setToken(txnToken);
        config.style=null;
        config.merchant.setMid(Constants.MerchantType.ICICI_COBRANDING_CHILD_MID.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);


        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.ICICI_COBRANDING_CHILD_MID,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "false";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String childheaderBckcolor = getColorCode((cashierPage.ICICICobrandingheadercolor()).getCssValue("background-color").toString());
        Assertions.assertThat(childheaderBckcolor).as("Config Header background color should be visible").isEqualTo(parentexpectedConfigheadercolor);
        cashierPage.easypaylogo().assertVisible();
    }

    @Owner(SRINIVAS)
    @Feature("PGP-40036")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and theme is displayed on the checkoutjs cashier page on applying style in checkout config ")
    public void Verify_icici_cobranding_logo_and_theme_is_displayed_on_cashierpage_applying_style_in_checkoutconfig(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String childexpectedConfigheadercolor="#284055";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.ICICI_COBRANDING_PARENT_MID)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.ICICI_COBRANDING_CHILD_MID)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_parentMID,theme);
        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.ICICI_COBRANDING_PARENT_MID,false);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);
        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.ICICI_COBRANDING_CHILD_MID.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);


        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.ICICI_COBRANDING_CHILD_MID,false);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "false";
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String childheaderBckcolor = getColorCode((cashierPage.ICICICobrandingheadercolor()).getCssValue("background-color").toString());
        Assertions.assertThat(childheaderBckcolor).as("Config Header background color should be visible").isEqualTo(childexpectedConfigheadercolor);
        cashierPage.easypaylogo().assertVisible();
    }

    @Owner(CHETAN)
    @Feature("PGP-37964")
    @Parameters({"theme"})
    @Test(description = "Verify for new wallet user RBI guidelines error message should be shown")
    public void verify_new_wallet_user_error_message(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        // User sso token should be created after march 12, 2022 checkUserBalance api should show "embargo":false
        // Error message: "Your wallet has been deactivated as mandated by RBI" - in cashier page we should see this error for wallet
        User user = userManager.getForRead(Label.NEWWALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        // Verify the error message
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("This option is not available for you.Know more");
        // verify wallet cannot be selected
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        // Verify know more is clickable and assert the message in know more window
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("This option is available only to existing Paytm balance users. Kindly use other payment option to complete this payment");
        // verify from debit/credit/net any banking transaction should be successful without using wallet
        cashierPage.payBy(Constants.PayMode.NB);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
    @Owner(PUSPA)
    @Feature("PGP-39088")
    @Test(description = "Emi plans should not be fetched in case of invalid bins and button should be disabled - new Card Flow")
    public void verifySelectEmiPlanButtonDisabledforInvalidBins_NewCardFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO =new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.INVALID_9DIGIT_BIN_NO);
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.fillEMICardDetails(paymentDTO);
        cashierPage.cardNotSupported().waitUntilVisible();
        Assertions.assertThat(cashierPage.cardNotSupported().getText()).isEqualTo("EMI not available on this card. Please choose another card");
        cashierPage.emiPlan().assertDisabled();
    }

    @Owner(PUSPA)
    @Feature("PGP-39088")
    @Test(description = "Emi plans should not be fetched in case of invalid bins and button should be disabled - NBFCs Flow")
    public void verifySelectEmiPlanButtonDisabledforInvalidBins_NBFCsFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO =new PaymentDTO();
        paymentDTO.setEmiCard(PaymentDTO.INVALID_9DIGIT_BIN_NO);
        cashierPage.tabEMI().click();
        cashierPage.pause(1);
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC Bank");
        cashierPage.fillEMICardDetails(paymentDTO);
        cashierPage.cardNotSupported().waitUntilVisible();
        Assertions.assertThat(cashierPage.cardNotSupported().getText()).isEqualTo("EMI not available on this card. Please choose another card");
        cashierPage.emiPlan().assertDisabled();
    }

    @Owner(PUSPA)
    @Feature("PGP-38653")
    @Parameters({"theme"})
    @Test(description = "Verify default priorities of pay options in response of V5/FPO in groupedPayOptionsPriorities object")
    public void validatePayment_Instrument_Group_level_DEFAULT(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.EMI)
                .setTxnValue("1").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body")).contains("groupPayOptionsPriorities");
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupPayOptionsPriorities")).contains("upiProfile:2", "paytm_featured:1", "savedMandateBanks:4", "userProfileSarvatra:2", "savedInstruments:3", "other_options:5");
        softly.assertAll();

    }

//    @Owner(PUSPA)
//    @Feature("PGP-38653")
//    @Parameters({"theme"})
//    @Test(description = "Verify priorities(Merchant Pref:PAYMODE_GROUP_PRIORITY_LIST) of pay options in response of V5/FPO in groupedPayOptionsPriorities object" ,enabled = false)
    //Logic is changed now, paymode priority list is decided on the basis of boss panel configuration not on pref
    public void validatePayment_Instrument_Group_level_MerchantPref(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.Instrument_Categorization)
                .setTxnValue("1").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body")).contains("groupPayOptionsPriorities");
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupPayOptionsPriorities")).contains("upiProfile:3", "paytm_featured:1", "savedMandateBanks:4", "userProfileSarvatra:3", "savedInstruments:2", "other_options:5");
        softly.assertAll();

    }
//    @Owner(PUSPA)
//    @Feature("PGP-38653")
//    @Parameters({"theme"})
//    @Test(description = "Verify priorities(Merchant Pref:NATIVE_PAYMODE_GROUP_PRIORITY_LIST) of pay options in response of V5/FPO in groupedPayOptionsPriorities object", enabled = false)
    //Logic is changed now, paymode priority list is decided on the basis of boss panel configuration not on pref
    public void validatePayment_Instrument_Group_level_MerchantPref_NATIVE(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UNGROUPED_PAYMODES_FLAG_ENABLED)
                .setTxnValue("1").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body")).contains("groupPayOptionsPriorities");
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupPayOptionsPriorities")).contains("upiProfile:2", "paytm_featured:1", "savedMandateBanks:4", "userProfileSarvatra:2", "savedInstruments:3", "other_options:5");
        softly.assertAll();


    }

//    @Owner(PUSPA)
//    @Feature("PGP-38653")
//    @Parameters({"theme"})
//    @Test(description = "Verify priorities(Merchant Pref:SUBSCRIPTION_PAYMODE_GROUP_PRIORITY_LIST) of pay options in response of V5/FPO in groupedPayOptionsPriorities object", enabled = false)
    //Logic is changed now, paymode priority list is decided on the basis of boss panel configuration not on pref
    public void validatePayment_Instrument_Group_level_MerchantPref_SUBS(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
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
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body")).contains("groupPayOptionsPriorities");
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupPayOptionsPriorities")).contains("upiProfile:3", "paytm_featured:1", "savedMandateBanks:4", "userProfileSarvatra:3", "savedInstruments:2", "other_options:5");
        softly.assertAll();

    }



    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-37934")
    @Parameters({"theme"})
    @Test(description = "Verify the UI Text for the Subscription Flow when values in MerchantStaticConfig DB updated")
    public  void PGP_37934_Verify_Subs_UI_Text_1(@Optional("checkoutjs_web_revamp") String theme) throws Exception{
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
//        WalletHelpers.modifyBalance(user, Double.parseDouble(initTxnDTO.txnAmountFromBody()) - 1);
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subscriptionDetailsOnCashierPage().contains("To be paid now_ddd");
        cashierPage.subscriptionDetailsOnCashierPage().contains("Recurring Bill Amount_xxx");
        cashierPage.subscriptionDetailsOnCashierPage().contains("Recurring Bill Frequency_yyy");
        cashierPage.subscriptionDetailsOnCashierPage().contains("Select an option to setup your subscriptions_ddd");
        cashierPage.subscriptionDetails().click();
        cashierPage.subscriptionDetailsOnInfoTab().contains("Recurring Bill Amount_xxx*");
        cashierPage.subscriptionDetailsOnInfoTab().contains("Amount to be Paid Now_eee");
        cashierPage.subscriptionDetailsOnInfoTab().contains("Subscription Details_eee");
        cashierPage.closeSubsDetailsTab().click();
//        cashierPage.checkBoxPPI().check();
        cashierPage.tabCreditCard().click();
        String text= cashierPage.buttonPGPayNow().getText();
        Assertions.assertThat(text).contains("Pay ₹5 to Subscribe");
    }
    @Owner(PRIYANKA)
    @Feature("PGP-35327")
    @Parameters({"theme"})
    @Test(description = "ICICI Co-branding template tweak")
    public void VerifyICICICoBrandedLogoIsVisibleOnTheCheckoutPageTheme(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String expectedConfigheadercolor = "#f5f8fa";
        InitTxnDTO initTxnDTO_parentMID = new InitTxnDTO.Builder(null, Constants.MerchantType.Parent_MID_ICICI)
                .build();
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.Child_MID_ICICI)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO_parentMID, theme);
        config.data.setToken(txnToken);
        config.style = null;
        config.merchant.setMid(Constants.MerchantType.Child_MID_ICICI.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String headerBckcolor = getColorCode(getElementByXpath("//*[@id='ptm-checkout-header']", theme).getCssValue("background-color"));
        Assertions.assertThat(headerBckcolor).as("Config Header  Background Color is Not Visible or Not Matching").isEqualTo(expectedConfigheadercolor);
        {
            Assertions.assertThat(headerBckcolor).isEqualTo(expectedConfigheadercolor);

        }
         cashierPage.easypaylogo().assertVisible();
        cashierPage.getFooterText().assertVisible();

    }

    @Owner(GAURAV)
    @Feature("PGP-39748")
    @Parameters({"theme"})
    @Test(description = "Verify paytm logo is visible in the header & footer instead of Paytm PG logo")
    public void verifyPaytmLogoisVisible(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, PAYTM_LOGO)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.paytmHeaderLogo().assertVisible();
        cashierPage.paytmFooterLogo().assertVisible();
    }

    @Owner(ASHISH_JASWAL)
    @Feature("PGP-37934")
    @Parameters({"theme"})
    @Test(description = "Verify the UI Text for the Subscription Flow when values in MerchantStaticConfig DB updated")
    public void PGP_37934_Verify_Subs_UI_Text_forBM(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.SUBS_UI_TEXT;
        String txnMaxAmount = "100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("0")
                .setSubscriptionPaymentMode("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setWebsiteName("retail")
                .setSubscriptionMaxAmount(txnMaxAmount)
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setCallbackUrl("")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();
        cashierPage.buttonPGPayNow().click();
        String bmText= cashierPage.bankMandateConfirmPay().getText();
        Assertions.assertThat(bmText).contains("Activate Subscriptions");

    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Verify that paytm intent option is shown along with phonepe and googlepe when phone number passed in config has UPI account configured on ios mweb with ENABLE_UPI_INTENT_ON_IOS_MWEB prefrence Y")
    public void paytm_registered_no_passed_on_ios_mweb_intent(@Optional("checkoutjs_wap_revamp") String theme) throws Exception{
        Constants.MerchantType merchant = Constants.MerchantType.PG2_IOS_UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        UserDetail userDetail = new UserDetail();
        userDetail.setMobileNumber("919958981935");
        config.data.setToken(txnToken);
        config.data.setUserDetail(userDetail);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.upiIntentPayButton().click();
        cashierPage.waitUntilLoads();
        cashierPage.paytm().assertVisible();
        cashierPage.phonepe().assertVisible();
        cashierPage.googlepay().assertVisible();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Verify that paytm intent option is not shown but googlepe and phonepe is shown when phone number passed in config does not have UPI account configured on ios mweb with ENABLE_UPI_INTENT_ON_IOS_MWEB prefrence Y")
    public void Not_paytm_registered_no_passed_on_ios_mweb_intent(@Optional("checkoutjs_wap_revamp") String theme) throws Exception{
        Constants.MerchantType merchant = Constants.MerchantType.PG2_IOS_UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        UserDetail userDetail = new UserDetail();
        userDetail.setMobileNumber("919999161601");
        config.data.setToken(txnToken);
        config.data.setUserDetail(userDetail);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.upiIntentPayButton().click();
        cashierPage.waitUntilLoads();
        cashierPage.phonepe().assertVisible();
        cashierPage.googlepay().assertVisible();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Verify that paytm intent option is shown along with phonepe and googlepe when no phone number passed in config on ios mweb with ENABLE_UPI_INTENT_ON_IOS_MWEB prefrence Y")
    public void NO_mobile_no_passed_on_ios_mweb_intent(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PG2_IOS_UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.upiIntentPayButton().click();
        cashierPage.waitUntilLoads();
        cashierPage.paytm().assertVisible();
        cashierPage.phonepe().assertVisible();
        cashierPage.googlepay().assertVisible();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Verify that intent option is shown  on ios mweb with ENABLE_UPI_INTENT_ON_IOS_MWEB Prefrence Y for addnPay")
    public void ios_mweb_intent_addnpay(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PG2_IOS_UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.upiIntentPayButton().click();
        cashierPage.waitUntilLoads();
        cashierPage.paytm().assertVisible();
        cashierPage.phonepe().assertVisible();
        cashierPage.googlepay().assertVisible();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Verify that no intent is shown on ios mweb with ENABLE_UPI_INTENT_ON_IOS_MWEB prefrence N")
    public void NO_ios_mweb_intent_shown(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.getUpiText().assertVisible();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Verify that no intent is shown on ios mweb with ENABLE_UPI_INTENT_ON_IOS_MWEB prefrence N with ADDnPAY")
    public void NO_ios_mweb_intent_shown_ADDnPAY(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.UPI_INTENT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.getUpiText().assertVisible();
    }

    @Owner(GAURAV)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Validate successful AddMoney Transaction")
    public void AddMoneyOnJsCheckoutWithDC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.AUTOLOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.AddMoney;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabDebitCard().click();
        cashierPage.payBy(Constants.PayMode.DC);
//        DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
//        directBankOTPPage.waitUntilLoads();
//        directBankOTPPage.submitOtp("888888");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        WalletHelpers.validateBalance(user,3);
    }

    @Owner(GAURAV)
    @Feature("PGP-37726")
    @Parameters({"theme"})
    @Test(description = "Validate successful AddMoney Transaction")
    public void AddMoneyOnJsCheckoutWithNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.AddMoney;
        WalletHelpers.modifyBalance(user, 1.0);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setIsNativeAddMoney("true")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO.setBankName("ICICI"));

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        WalletHelpers.validateBalance(user,3);
    }

    @Owner(PUSPA)
    @Feature("PGP-35658")
    @Parameters({"theme"})
    @Test(description = "Verify EMI payoption is visible when wallet is selected for ADDnPAY")
    public void EMI_PayOption_Visible_addNPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        Constants.MerchantType merchant = NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getString("body.paymentFlow").contains("ADDANDPAY")) {
            cashierPage.tabEMI().assertVisible();
        }
        else {
            cashierPage.tabEMI().assertNotVisible();
        }
    }

    @Owner(PUSPA)
    @Feature("PGP-35658")
    @Parameters({"theme"})
    @Test(description = "Verify popUp text is visible when wallet is selected for ADDnPAY and EMI is clicked")
    public void validate_popup_click_on_EMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        Constants.MerchantType merchant = NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getString("body.paymentFlow").contains("ADDANDPAY")) {
            cashierPage.tabEMI().click();
            cashierPage.alertTexttoProceedEMI().waitUntilVisible();
            Assertions.assertThat(cashierPage.alertTexttoProceedEMI().getText()).isEqualTo("Paytm Balance shall not be used with the EMI payment. Press Ok to make full payment through EMI.");
        }
        else
          {
            cashierPage.tabEMI().assertNotVisible();
          }
    }

//    @Owner(PUSPA)
//    @Feature("PGP-35658")
//    @Parameters({"theme"})
//    @Test(description = "Verify wallet is checked when Dismiss button clicked on popUp", enabled = false)
    public void validate_wallet_checked_onDismiss(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        Constants.MerchantType merchant = NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getString("body.paymentFlow").contains("ADDANDPAY")) {
            cashierPage.tabEMI().click();
            cashierPage.pause(2);
            cashierPage.proceedDismiss().click();
            cashierPage.checkBoxPPI().isChecked();
        }
        else
        {
            cashierPage.tabEMI().assertNotVisible();
        }
    }

    @Owner(PUSPA)
    @Feature("PGP-35658")
    @Parameters({"theme"})
    @Test(description = "Verify EMI txn is success with full amount when OK button clicked on popUp")
    public void validate_EMI_TXN_onOK(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        Constants.MerchantType merchant = NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO=new PaymentDTO();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getString("body.paymentFlow").contains("ADDANDPAY")) {
            cashierPage.tabEMI().click();
            cashierPage.getPayButtonNew().click();
            DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
            cashierPage.textBoxCardNumberEMI().click();
            cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
            cashierPage.waitUntilLoads();
            cashierPage.emiPlan().click();
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.proceedToConvertEMI().click();
            DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
            cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
            cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
            cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear());
            cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
            DriverManager.getDriver().switchTo().defaultContent();
            cashierPage.buttonPGPayNow().click();
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateCurrency("INR")
                    .validateMid(initTxnDTO.getBody().getMid())
                    .validateOrderId(Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode("EMI")
                    .validateRespCode("01")
                    .validateRespMsg("Txn Success")
                    .validateStatus("TXN_SUCCESS")
                    .assertAll();
        }
        else
        {
            cashierPage.tabEMI().assertNotVisible();
        }
    }

    @Owner(PUSPA)
    @Feature("PGP-35658")
    @Parameters({"theme"})
    @Test(description = "Verify no pop up should show when wallet has sufficient balance for txn")
    public void validate_noPopUp_Display_sufficientBalance(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        WalletHelpers.modifyBalance(user, 20.0);
        Constants.MerchantType merchant = NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabEMI().click();
        PaymentDTO paymentDTO =new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

//    @Owner(PUSPA)
//    @Feature("PGP-35658")
//    @Parameters({"theme"})
//    @Test(description = "Verify wallet is unchecked when EMI page is closed", enabled = false)
    public void validate_wallet_unchecked_onEMIClose(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        Constants.MerchantType merchant = NATIVE_ADDNPAY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        if (fetchPaymentOptionsJson.getString("body.paymentFlow").contains("ADDANDPAY")) {
            cashierPage.tabEMI().click();
            cashierPage.getPayButtonNew().click();
            cashierPage.CrossButtonOnEntercardpage().click();
            cashierPage.checkBoxPPI().unCheck();
        }
        else
        {
            cashierPage.tabEMI().assertNotVisible();}

    }

    @Owner(Amanpreet)
    @Feature("PGP-40763")
    @Parameters({"theme"})
    @Test(description = "verify for Wallet only merchants that wallet is not selected when user has insufficient wallet balance")
    public void PGP_40763_TC_03_validateWallet_NotSelected(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.WalletOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        double balance = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 10.00;
        WalletHelpers.modifyBalance(user, balance);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        WebElement walletBalance = DriverManager.getDriver().findElement(By.xpath("//*[@id='wallet_checkbox']"));
        Assertions.assertThat(walletBalance.isSelected()).isFalse();
    }
//    @Owner(Amanpreet)
//    @Feature("PGP-40763")
//    @Parameters({"theme"})
//    @Test(description = "verify for Wallet only merchants that wallet is selected when user has sufficient wallet balance", enabled = false)
    public void PGP_40763_TC_04_validateWallet_Selected(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.WalletOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        double balance = Double.valueOf(initTxnDTO.txnAmountFromBody()) + 10.00;
        WalletHelpers.modifyBalance(user, balance);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        WebElement walletBalance = DriverManager.getDriver().findElement(By.xpath("//*[@id='wallet_checkbox']"));
        WebElement payButton = DriverManager.getDriver().findElement(By.xpath("//*[@id=\"checkout-button\"]"));
        Assertions.assertThat(walletBalance.isSelected()).isTrue();
        Assertions.assertThat(payButton.isDisplayed()).isTrue();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-41281")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and header color is displayed on the checkoutjs cashier page on parent mid without making any customisation on child mid ")
    public void Verify_icici_cobranding_logo_and_headercolor_is_displayed_on_cashierpage_without_applying_customisation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String parentexpectedConfigheadercolor="#e77817";
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.CHILD_COBRANDING_MID)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_childMID,theme);
        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_MID,true);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.childConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "false";
        String childmid=getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.childConfig.mid");
        Assertions.assertThat(childmid).isEqualTo("HRSHCF06247372764665");
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        config.data.setToken(txnToken);
        config.style=null;
        config.merchant.setMid(Constants.MerchantType.CHILD_COBRANDING_MID.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);

        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_MID,true);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.parentConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        String parentmid=getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.parentConfig.mid");
        Assertions.assertThat(parentmid).isEqualTo("HRSHCE44598388088972");
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);


        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String childheaderBckcolor = getColorCode((cashierPage.ICICICobrandingheadercolor()).getCssValue("background-color").toString());
        Assertions.assertThat(childheaderBckcolor).as("Config Header background color should be visible").isEqualTo(parentexpectedConfigheadercolor);
        cashierPage.easypaylogo().assertVisible();
    }

    @Owner(Constants.Owner.ASHISH_JASWAL)
    @Feature("PGP-41305")
    @Parameters({"theme"})
    @Test(description = "Verify the UI Text for the Subscription Flow when values in MerchantStaticConfig DB updated")
    public void VERIFY_Rupee_SpecialCharacter_Rendering_onJSCheckout(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        Constants.MerchantType merchant = SUBS_UI_TEXT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("5")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.subscriptionDetailsOnCashierPage().contains("₹1 will be deducted now for account verification");
    }

    @Owner(SRINIVAS)
    @Feature("PGP-41281")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and header color is displayed on the checkoutjs cashier page making customisation on parent mid and child mid ")
    public void Verify_icici_cobranding_logo_and_headercolor_is_displayed_on_cashierpage_customisation_on_parent_and_child_mid(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String parentexpectedConfigheadercolor="#e77817";
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.CHILD_COBRANDING_PREF_ENABLED)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_childMID,theme);
        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_PREF_ENABLED,true);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.parentConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        String parentmid=getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.parentConfig.mid");
        Assertions.assertThat(parentmid).isEqualTo("qa14pt50727822623683");
//        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);

        config.data.setToken(txnToken);
        config.style=null;
        config.merchant.setMid(Constants.MerchantType.CHILD_COBRANDING_PREF_ENABLED.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);


        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_PREF_ENABLED,true);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.parentConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "true";
        String childmid=getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.childConfig.mid");
        Assertions.assertThat(childmid).isEqualTo(Constants.MerchantType.CHILD_COBRANDING_PREF_ENABLED.getId());
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String childheaderBckcolor = getColorCode((cashierPage.ICICICobrandingheadercolor()).getCssValue("background-color").toString());
        Assertions.assertThat(childheaderBckcolor).as("Config Header background color should be visible").isEqualTo(parentexpectedConfigheadercolor);
        cashierPage.easypaylogo().assertVisible();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-41281")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and header color is displayed on the checkoutjs cashier page on applying style in checkout config ")
    public void Verify_icici_cobranding_logo_and_headercolor_is_displayed_on_cashierpage_applying_style_in_checkoutconfig(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String childexpectedConfigheadercolor="#284055";
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, CHILD_COBRANDING_MID)
                .build();

        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_childMID,theme);
        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_MID,true);
        JsonPath getMerchantPGPUIThemeParentMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromAPI = getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.parentConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpected = "true";
        String parentmid=getMerchantPGPUIThemeParentMIDJson.getString("body.merchantPreferenceInfos.parentConfig.mid");
        Assertions.assertThat(parentmid).isEqualTo("HRSHCE44598388088972");
        Assertions.assertThat(isParentThemeOverwriteEnabledFromAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpected);
        config.data.setToken(txnToken);
        config.merchant.setMid(Constants.MerchantType.CHILD_COBRANDING_MID.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);


        MerchantPGPUITheme merchantPGPUIThemeChildMID=new MerchantPGPUITheme(Constants.MerchantType.CHILD_COBRANDING_MID,true);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeChildMID.execute().jsonPath();
        String isParentThemeOverwriteEnabledFromChildAPI = getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.childConfig.theme.isParentThemeOverwriteEnabled");
        String isParentThemeOverwriteEnabledExpectedChildAPI = "false";
        String childmid=getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.childConfig.mid");
        Assertions.assertThat(childmid).isEqualTo("HRSHCF06247372764665");
        Assertions.assertThat(isParentThemeOverwriteEnabledFromChildAPI).as("isParentThemeOverwriteEnabled flag in merchantpgpui/theme API").isEqualTo(isParentThemeOverwriteEnabledExpectedChildAPI);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        String childheaderBckcolor = getColorCode((cashierPage.ICICICobrandingheadercolor()).getCssValue("background-color").toString());
        Assertions.assertThat(childheaderBckcolor).as("Config Header background color should be visible").isEqualTo(childexpectedConfigheadercolor);
        cashierPage.easypaylogo().assertVisible();
    }
    @Owner(SRINIVAS)
    @Feature("PGP-41281")
    @Parameters({"theme"})
    @Test(description = "Verify the if the child mid does not have parent it should display child mid and parent mid as null in the response")
    public void Verify_childmid_parentmid_display_as_null_in_response(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, PGOnly)
                .build();

        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_childMID,theme);
        MerchantPGPUITheme merchantPGPUIThemeParentMID=new MerchantPGPUITheme(Constants.MerchantType.PGOnly,true);
        JsonPath getMerchantPGPUIThemeChildMIDJson = merchantPGPUIThemeParentMID.execute().jsonPath();
        String childmid=getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.childConfig.mid");
        Assertions.assertThat(childmid).isEqualTo("qa8PG294377944191275");
        String parentmid=getMerchantPGPUIThemeChildMIDJson.getString("body.merchantPreferenceInfos.parentConfig.mid");
        Assertions.assertThat(parentmid).isEqualTo(null);
    }
//    @Owner(PRIYANKA)
//    @Feature("PGP-40323")
//    @Parameters({"theme"})
//    @Test(description = "verify for Wallet only merchants that wallet is not selected when user has insufficient wallet balance and other paymodes are selected", enabled = false)
    public void Validate_Succesful_TXN_WalletOnly_Insufficient_Balance(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        double balance = Double.valueOf(initTxnDTO.txnAmountFromBody()) - 10.00;
        WalletHelpers.modifyBalance(user, balance);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        WebElement walletBalance = cashierPage.walletcheckbox();
        Assertions.assertThat(walletBalance.isSelected()).isFalse();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    @Owner(SRINIVAS)
    @Feature("PGP-35642")
    @Parameters({"theme"})
    @Test(description = "Verify making customisation on cashier page on UMP Panel reflected in checkoutjs cashier page")
    public void Verify_cashierpage_is_displayed_with_customisation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        Constants.MerchantType merchant = Constants.MerchantType.CUSTOMISATION_ON_CASHIERPAGE;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.style = null;
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String headerBck_color = getColorCode(cashierPage.headerBck_color().getCssValue("background-color"));
        String headerTxtColor = getColorCode(cashierPage.headerTxtColor().getCssValue("color"));
        String bodyBck_color = getColorCode(cashierPage.bodyBck_color().getCssValue("background-color"));
        String textColor = getColorCode(cashierPage.textColor().getCssValue("color"));
        String paybuttonbck_color = getColorCode(cashierPage.paybuttonbck_color().getCssValue("background-color"));

        Assertions.assertThat(headerBck_color).isEqualTo("#000000");
        Assertions.assertThat(headerTxtColor).isEqualTo("#ffffff");
        Assertions.assertThat(bodyBck_color).isEqualTo("#003a96");
        Assertions.assertThat(textColor).isEqualTo("#ff0000");
        Assertions.assertThat(paybuttonbck_color).isEqualTo("#00b9f5");
    }

    @Owner(PRIYANKA)
    @Feature("PGP-42040")
    @Parameters({"theme"})
    @Test(description = "Verfiy txntype:ADDANDPAY is displayed in FetchBin Details API Response while making addnpay txn")
    public void Verify_txntype_parameter_displyedin_FetchBinDetailsAPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .setTxnValue("2")
                .build();
        WalletHelpers.modifyBalance(user, 1.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        PaymentDTO paymentDTO =new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        if (!cashierPage.checkBoxPPI().isChecked()) {
            cashierPage.checkBoxPPI().check();
        }
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(Constants.PayMode.CC,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String grepcmd = "grep \"" + initTxnDTO.orderFromBody() + "\" " + LocalConfig.THEIA_LOGS
                + " | grep \"NativeBinDetailAppController.()\"";
        String Theialogs=getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(Theialogs).contains("txnType=ADDANDPAY");
    }


    @DataProvider(name = "SsoAndMidData")
    public Object[][] getDataFromDataprovider() {
        return new Object[][]{
                {Label.CREDITFREEZE, NATIVE_ADDNPAY},
                {Label.DEBITFREEZE, NATIVE_ADDNPAY},
                {Label.CREDITDEBITFREEZE, NATIVE_ADDNPAY}
        };
    }
    @Owner(PRIYANKA)
    @Feature("PGP-41842")
    @Parameters({"theme"})
    @Test(description = "Verify the bankResultInfo parameter is enabled when transaction is failed for offline merchant in theia and merchant status logs")
    public void Verify_bankResultInfo_parameter_enabled_when_transaction_failed_for_offline_merchant_in_theia_merchantStatus_logs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, PG_OFFUS).setTxnValue("99.41")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        String merch = PG_OFFUS.getId();
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabUPI().click();
        cashierPage.textBoxVPA().clearAndType("8006006993@paytm");
//        cashierPage.verifyVPALinkText().click();
        cashierPage.pause(3);
        cashierPage.payButton().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        String grepcmd = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" "+ LocalConfig.THEIA_LOGS +
                " | grep \"com.paytm.pgplus.theia.services.impl.SeamlessPaymentServiceImpl.()\"";
        String theiaLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY,grepcmd);
        Assertions.assertThat(theiaLogs).contains("bankResultInfo");

        TxnStatusApi reference= new TxnStatusApi(PG_OFFUS.getId(),PG_OFFUS.getKey(),initTxnDTO.getBody().getOrderId());
        PGPHelpers.getTxnStatus(PG_OFFUS.getId(),initTxnDTO.getBody().getOrderId()).validateVPA("srivastavaprateek@paytm");
        String grepcmd1 = "grep \"" + initTxnDTO.getBody().getOrderId() + "\" " + LocalConfig.MERCHANT_STATUS_LOGS
                + " | grep \"com.paytm.pgplus.merchant.status.controller.MerchantTxnStatusController.processMerchantTxnStatus()\"";
        String Merchantstatuslogs=getLogsOnServer(ServerConfigProvider.SERVICE.MERCHANT_STATUS,grepcmd1);
        Assertions.assertThat(Merchantstatuslogs).contains("bankResultInfo=null");

    }



//    @Owner(CHETAN)
//    @Feature("PGP-39336")
//    @Parameters({"theme"})
//    @Test(description = "Verify credit/Debit/creditdebit freeze messages on wallet", dataProvider = "SsoAndMidData", enabled = false)
    public void verify_wallet_disabled_message_creditDebitFreeze(Label userWallet, Constants.MerchantType mid) throws Exception {
        String theme = "checkoutjs_web_revamp";
        User user = userManager.getForRead(userWallet);
        double balanace = WalletHelpers.getWalletBalance(user);
        String amount = String.valueOf(balanace + 10);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), mid)
                .setTxnValue(amount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.data.setAmount(amount);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        Assertions.assertThat(cashierPage.getUserDeactivatedErrorMessage().getText()).isEqualTo("This option is not available for you.Know more");
        Assertions.assertThat(cashierPage.isWalletDisabled()).isTrue();
        Assertions.assertThat(cashierPage.getKnowMoreText()).isEqualTo("Based on your KYC status and the limits imposed by RBI, you cannot complete this transaction using wallet. Please visit wallet section in the Paytm app to update your wallet KYC if not yet updated.");
    }

    @Owner(PRIYANKA)
    @Feature("PGP-40346")
    @Parameters({"theme"})
    @Test(description = "Verify the infoButtonMessage is not empty in the FetchBalance API when the user is frozen")
    public void Verify_infobuttonMessage_is_not_empty_FrozenUser(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.POSTPAID);
        PostpaidHelpers.updateBalance("91");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("91")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchBalance fetchBalance  = new FetchBalance(merchantType.getId(), initTxnDTO.orderFromBody(),txnToken, "PAYTM_DIGITAL_CREDIT");
        fetchBalance.setContext("head.tokenType","TXN_TOKEN");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("FROZEN");
        Assertions.assertThat(fetchBalanceResponse.getString("body.infoButtonMessage")).isNotEmpty();
    }
    @Owner(PRIYANKA)
    @Feature("PGP-40346")
    @Parameters({"theme"})
    @Test(description = "Verify the infoButtonMessage is not empty in the FetchBalance API when the user is deactive")
    public void Verify_infobuttonMessage_is_not_empty_DeactiveUser(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.POSTPAID);
        PostpaidHelpers.updateBalance("92");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("92")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchBalance fetchBalance  = new FetchBalance(merchantType.getId(), initTxnDTO.orderFromBody(),txnToken, "PAYTM_DIGITAL_CREDIT");
        fetchBalance.setContext("head.tokenType","TXN_TOKEN");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("DEACTIVE");
        Assertions.assertThat(fetchBalanceResponse.getString("body.infoButtonMessage")).isNotEmpty();
    }
    @Owner(PRIYANKA)
    @Feature("PGP-40346")
    @Parameters({"theme"})
    @Test(description = "Verify the infoButtonMessage is empty in the FetchBalance API when the user is on hold")
    public void Verify_infobuttonMessage_is_not_empty_OnHoldUser(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.AddnPay;
        User user = userManager.getForWrite(Label.POSTPAID);
        PostpaidHelpers.updateBalance("93");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("93")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        FetchBalance fetchBalance  = new FetchBalance(merchantType.getId(), initTxnDTO.orderFromBody(),txnToken, "PAYTM_DIGITAL_CREDIT");
        fetchBalance.setContext("head.tokenType","TXN_TOKEN");
        JsonPath fetchBalanceResponse = fetchBalance.execute().jsonPath();
        Assertions.assertThat(fetchBalanceResponse.getString("body.accountStatus")).as("accountStatus in fetchBalanceInfo API is not active").isEqualTo("ON_HOLD");
        Assertions.assertThat(fetchBalanceResponse.getString("body.infoButtonMessage")).isNotEmpty();
    }

    @Owner(SRINIVAS)
    @Feature("PGP-40837")
    @Parameters({"theme"})
    @Test(description = "Verify priorities of Grouped pay options in response of V5/FPO in groupedPayOptionsPriorities object")
    public void validatePayment_Instrument_Group_level_priority(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType. Instrument_Categorization_BOSS)
                .setTxnValue("1").build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body")).contains("groupPayOptionsPriorities");
        softly.assertThat(fetchPaymentOptionsJson.getString("body.groupPayOptionsPriorities")).contains("paytm_featured:1, savedInstruments:4, upiProfile:2, userProfileSarvatra:2, other_options:3");
        softly.assertAll();
    }

    @Owner(CHETAN)
    @Feature("PGP-38072")
    @Parameters({"theme"})
    @Test(description = "Verify paytm pg white logo is displayed in header in cashier page checkoutjs flow for dark theme mid")
    public void verifying_paytmPgWhiteLogo_displayed_on_darktheme(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.DARK_THEME)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(null);
        config.style=null;
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        Assert.assertTrue(cashierPage.getPaytmLogoWhite().isElementPresent());
        cashierPage.footerLogoBlue().waitUntilVisible();
    }
    @Owner(PUSPA)
    @Feature("PGP-41553")
    @Parameters({"theme"})
    @Test(description = "Verify Postpaid pay option as default on cashier page in case of AddNPay")
    public void verifyPostpaidAsDefaultforAddNPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .setTxnValue("20").build();
        WalletHelpers.modifyBalance(user, 10.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.radioButtonPaytmPostpaid().isSelected();

    }
    @Owner(PUSPA)
    @Feature("PGP-41553")
    @Parameters({"theme"})
    @Test(description = "Verify Postpaid pay option as not default on cashier page in case of NONE Flow")
    public void verifyPostpaidAsNOTdefaultforNoneFlow(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.POSTPAID);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), PGOnly)
                .setTxnValue("2").build();
        WalletHelpers.modifyBalance(user, 10.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.checkBoxPPI().isSelected();

    }
    @Owner(PUSPA)
    @Feature("PGP-41553")
    @Parameters({"theme"})
    @Test(description = "Verify Postpaid txn for AddNPay ")
    public void verifyPostpaidtxnforAddNPay(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.NATIVE_ADDNPAY)
                .setTxnValue("1").build();
        WalletHelpers.modifyBalance(user, 10.0);
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("Paytm Postpaid")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }
//    @Owner(PUSPA)
//    @Feature("PGP-38718")
//    @Parameters({"theme"})
//    @Test(description = "verify EMI_DC on the basis of DB check categorization",enabled = false)
    public void EMI_DCbasedonCategorization_DBCheck(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        User user = userManager.getForRead(Label.EMIDC);
        Constants.MerchantType merchant = PG2_AMEX_EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.uncheckedPPIForCheckoutJS().isEnabled()){
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.ViewAll().click();
        PaymentDTO paymentDTO =new PaymentDTO().setBankName("ICICI Bank Debit Card");
        cashierPage.selectEMIBank(paymentDTO);
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        Assertions.assertThat(cashierPage.textBoxCardNumber().isDisplayed()).isTrue();

    }
    @Owner(CHETAN)
    @Feature("PAPR-3207")
    @Parameters({"theme"})
    @Test(description = "verify ultimate beneficiary details present in cashier page and insta logs")
    public void verify_ultimate_beneficiary_details_present_in_cashier_page_and_insta_logs_checkoutjs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String ultimateBeneficiaryName = "Beneficiary 1";
        UltimateBeneficiaryDetails ultimateBeneficiaryDetails = new UltimateBeneficiaryDetails(ultimateBeneficiaryName);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DEACTIVATED_WALLET)
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetails)
                .build();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.pause(2);
        Assertions.assertThat(cashierPage.getUltimateBeneficiaryName().getText()).isEqualTo(ultimateBeneficiaryName);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/instaproxy.log | " +
                "grep \"" + ultimateBeneficiaryName +"\"";
        String instaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepcmd);
        Assertions.assertThat(instaLogs).contains(ultimateBeneficiaryName);
    }

    @DataProvider (name="getFPOVersions" )
    public Object[][] getFpoVersions(){
        String fpoVersions[]={"v1", "v2", "v5"};
        return new String[][]{fpoVersions};
    }

    @Owner(CHETAN)
    @Feature("PGP-41651")
    @Parameters({"theme"})
    @Test(description = "Verify FPO response when ultimate beneficiary details is sent in both initiate api and fpo api, initiate ultimate beneficiary string should be displayed in fpo response and in cashier page")
    public void ultimate_beneficiary_sent_in_initiate_and_fpo_request(@Optional("v2")String fpoVersion) throws Exception {
        String theme = "checkoutjs_web_revamp";
        String ultimateBeneficiaryNameInit = "ub-init";
        String ultimateBeneficiaryNameFPO = "ub-fpo";
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsInit = new UltimateBeneficiaryDetails(ultimateBeneficiaryNameInit);
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails(ultimateBeneficiaryNameFPO);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DEACTIVATED_WALLET)
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsInit)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).
                setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO).setVersion(fpoVersion).build();
        FetchPaymentOption fetchPaymentOption = null;
        if(fpoVersion.equals("v1"))
            fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        else if(fpoVersion.equals("v2"))
            fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        else if(fpoVersion.equals("v5"))
            fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);

        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body.ultimateBeneficiaryDetails.ultimateBeneficiaryName")).contains(ultimateBeneficiaryNameInit);
        softly.assertAll();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.getUltimateBeneficiaryName().getText()).isEqualTo(ultimateBeneficiaryNameInit);
    }

    @Owner(CHETAN)
    @Feature("PGP-41651")
    @Parameters({"theme"})
    @Test(description = "Verify FPO response when ultimate beneficiary details is sent in only fpo request api, ultimate beneficiary string should be displayed in fpo response and not in cashier page")
    public void ultimate_beneficiary_sent_in_fpo_request(@Optional("v2") String fpoVersion) throws Exception {
        String theme = "checkoutjs_web_revamp";
        String ultimateBeneficiaryNameFPO = "ub-fpo";
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsFPO = new UltimateBeneficiaryDetails(ultimateBeneficiaryNameFPO);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DEACTIVATED_WALLET)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).
                setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsFPO).setVersion(fpoVersion).build();
        FetchPaymentOption fetchPaymentOption = null;
        if(fpoVersion.equals("v1"))
            fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        else if(fpoVersion.equals("v2"))
            fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        else if(fpoVersion.equals("v5"))
            fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body.ultimateBeneficiaryDetails.ultimateBeneficiaryName")).contains(ultimateBeneficiaryNameFPO);
        softly.assertAll();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.getUltimateBeneficiaryName().getText()).isNotEqualTo(ultimateBeneficiaryNameFPO);
    }

    @Owner(CHETAN)
    @Feature("PGP-35983")
    @Parameters({"theme"})
    @Test(description = "Verify FPO response when ultimate beneficiary details is sent in only initiate api, ultimate beneficiary string should be displayed in fpo response and in cashier page")
    public void ultimate_beneficiary_sent_in_initiate(@Optional("v5")String fpoVersion) throws Exception {
        String theme = "checkoutjs_web_revamp";
        String ultimateBeneficiaryNameInit = "ub-init";
        UltimateBeneficiaryDetails ultimateBeneficiaryDetailsInit = new UltimateBeneficiaryDetails(ultimateBeneficiaryNameInit);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.DEACTIVATED_WALLET)
                .setUltimateBeneficiaryDetails(ultimateBeneficiaryDetailsInit)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion(fpoVersion).build();
        FetchPaymentOption fetchPaymentOption = null;
        if(fpoVersion.equals("v1"))
            fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        else if(fpoVersion.equals("v2"))
            fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        else if(fpoVersion.equals("v5"))
            fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);

        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(fetchPaymentOptionsJson.getString("body.ultimateBeneficiaryDetails.ultimateBeneficiaryName")).contains(ultimateBeneficiaryNameInit);
        softly.assertAll();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        Assertions.assertThat(cashierPage.getUltimateBeneficiaryName().getText()).isEqualTo(ultimateBeneficiaryNameInit);
    }

//    @Owner(PUSPA)
//    @Feature("PGP-38718")
//    @Parameters({"theme"})
//    @Test(description = "verify EMI_DC on the basis of default true(user not logged in) categorization",enabled = false)
    public void EMI_DCbasedonCategorization_DefaultTrue(@Optional("checkoutjs_web_revamp") String theme) throws Exception
    {
        User user = userManager.getForRead(Label.EMIDC);
        Constants.MerchantType merchant = PG2_AMEX_EMI;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).setTxnValue("200")
                .setMobile(user.mobNo())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO,theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.uncheckedPPIForCheckoutJS().isEnabled()){
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.ViewAll().click();
        PaymentDTO paymentDTO =new PaymentDTO().setBankName("ICICI Bank Debit Card");
        cashierPage.selectEMIBank(paymentDTO);
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        Assertions.assertThat(cashierPage.textBoxCardNumber().isDisplayed()).isTrue();

    }
//    @Owner(PUSPA)
//    @Feature("PGP-38718")
//    @Parameters({"theme"})
//    @Test(description = "verify EMI_DC on the basis of Check mobile number categorization",enabled = false) //EMI_DC flow is changed
    public void EMI_DCbasedonCategorization_CheckNumber(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.EMIDC);
        Constants.MerchantType merchant = EMI_DC_CC;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("200")
                .setMobile(user.mobNo())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.uncheckedPPIForCheckoutJS().isEnabled()){
            cashierPage.uncheckedPPIForCheckoutJS().click();
        }
        cashierPage.tabEMI().click();
        DriverManager.getDriver().switchTo().parentFrame();
        cashierPage.ViewAll().click();
        PaymentDTO paymentDTO =new PaymentDTO().setBankName("ICICI Bank Debit Card");
        cashierPage.selectEMIBank(paymentDTO);
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        Assertions.assertThat(cashierPage.textBoxCardNumber().isDisplayed()).isTrue();


    }


    @Owner(Amanpreet)
    @Feature("PGP-42813")
    @Parameters({"theme"})
    @Test(description = "Verify enach callback is given on the child window when request Type is 'NATIVE_SUBSCRIPTION'")
    public void PGP_Verify_CallbackURL(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String txnMaxAmount = "100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("2")
                .setmandateType("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(txnMaxAmount)
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();
//        cashierPage.proceedBtn().click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        String ResponseURL = cashierPage.getChildWindowURL();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(ResponseURL.contains(LocalConfig.BANK_MANDATE_RESP_URL));

    }
    @Owner(Amanpreet)
    @Feature("PGP-42813")
    @Parameters({"theme"})
    @Test(description = "Verify enach callback is given on the child window when request Type is 'NATIVE_MF_SIP'")
    public void PGP_Verify_CallbackURL_SIP(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.LOGIN);
        Constants.MerchantType merchant = Constants.MerchantType.BANK_MANDATE;
        String txnMaxAmount = "100";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)       //Initiate subs request
                .setTxnValue("2")
                .setmandateType("BANK_MANDATE")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount(txnMaxAmount)
                .setSubscriptionFrequency("0")
                .setSubscriptionFrequencyUnit("ONDEMAND")
                .setSubscriptionGraceDays("0")
                .setMandateAccountDetails(new MandateAccountDetails())
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_MF_SIP")
                .build();
        InitTxnResponseDTO initTxnResponse = validateSuccessInitiateSubscription(initTxnDTO);
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setOrderId(orderId);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.tabBankMandate().click();
//        cashierPage.proceedBtn().click();
        cashierPage.buttonPGPayNow().click();
        cashierPage.buttonPGPayNow().waitUntilClickable();
        cashierPage.buttonPGPayNow().click();
        String ResponseURL = cashierPage.getChildWindowURL();
        cashierPage.waitUntilLoads();
        Assertions.assertThat(ResponseURL.contains(LocalConfig.BANK_MANDATE_RESP_URL));
    }

    @Owner(CHETAN)
    @Feature("PGP-31991")
    @Parameters({"theme"})
    @Test(description = "Verify the loader is displayed during the success transaction")
    public void verify_loader_is_displayed_during_success_transaction(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.NEWWALLETUSER);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.DEACTIVATED_WALLET)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        config.merchant.setHidePaytmBranding(true);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.NB);
        // Verifying the loader exists. This test case is to verify this loader specifically in below line
        cashierPage.overlay().waitUntilVisible();
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

    }


    @Parameters({"theme"})
    @Feature("PGP-39255")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Verify the UI for QR Flow")
    public void PGP_39255_TC_07(@Optional("checkoutjs_web_revamp") String theme) throws IOException {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        validatingUIForQRFlow(theme, true);
    }

    @Parameters({"theme"})
    @Feature("PGP-39255")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Verify the QR is not displayed when UPI and wallet are disabled")
    public void PGP_39255_TC_08(@Optional("checkoutjs_web_revamp") String theme)  throws IOException {
        Constants.MerchantType merchant = Constants.MerchantType.ICON_ON_MERCHANT_TC06;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        validatingUIForQRFlow(theme, false);
    }

    @Parameters({"theme"})
    @Feature("PGP-39255")
    @Owner(Constants.Owner.TAMANA_TATHAN)
    @Test(description = "Verify the QR is not displayed when UPI and wallet are disabled")
    public void PGP_39255_TC_09(@Optional("checkoutjs_web_revamp") String theme)  throws IOException {
        Constants.MerchantType merchant = Constants.MerchantType.Notification_Merchant;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        validatingUIForQRFlow(theme, false);
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when only UPI is enabled")
    public void PGP_41787_TC01(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH","UPI","UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm or Any UPI App");
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " + LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSH channel is enabled")
    public void PGP_41787_TC02(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSH"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " + LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPI channel is enabled")
    public void PGP_41787_TC03(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENT_OPTION, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSHEXPRESS channel is enabled")
    public void PGP_41787_TC04(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSHEXPRESS"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENT_OPTION, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSH and UPI channels are enabled")
    public void PGP_41787_TC05(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSH","UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH","UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" /paytm/logs/theia.log | " + "grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when only UPIPUSH and UPIPUSHEXPRESS channels are enabled")
    public void PGP_41787_TC06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSH","UPIPUSHEXPRESS"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH","UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm or Any UPI App");
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPI and UPIPUSHEXPRESS channels are enabled")
    public void PGP_41787_TC07(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSHEXPRESS","UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSHEXPRESS","UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPI is disabled")
    public void PGP_41787_TC08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).doesNotContain("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENT_OPTION, grepcmd);
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
        Assertions.assertThat(theiaLogs.contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false"));
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSH channel is disabled")
    public void PGP_41787_TC09(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPIPUSH"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).contains("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).doesNotContain("UPIPUSH");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENT_OPTION, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when only UPI channel is disabled")
    public void PGP_41787_TC10(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).contains("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).doesNotContain("UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm or Any UPI App");
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+LocalConfig.THEIA_LOGS+ " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSHEXPRESS channel is disabled")
    public void PGP_41787_TC11(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPIPUSHEXPRESS"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).contains("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).doesNotContain("UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+LocalConfig.THEIA_LOGS+ " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPIPUSH and UPI channels are disabled")
    public void PGP_41787_TC12(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPIPUSH","UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).contains("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).doesNotContain("UPIPUSH","UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+LocalConfig.THEIA_LOGS+ " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENT_OPTION, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when UPIPUSH and UPIPUSHEXPRESS channels are disabled")
    public void PGP_41787_TC13(@Optional("checkoutjs_wap_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPIPUSH","UPIPUSHEXPRESS"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).contains("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).doesNotContain("UPIPUSH","UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" " +LocalConfig.THEIA_LOGS+ " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.PAYMENT_OPTION, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when only UPI and UPIPUSHEXPRESS channels are disabled")
    public void PGP_41787_TC14(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPIPUSHEXPRESS","UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).contains("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).doesNotContain("UPIPUSHEXPRESS","UPI");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+ LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is not present when UPIPUSH is enabled and UPI, UPIPUSHEXPRESS channels are disabled") //in all cases now we are generating D QR for online
    public void PGP_41787_TC15(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSH"}, "UPI");
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPIPUSHEXPRESS","UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertNotVisible();
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("COMPLETED Task: CREATE_DYNAMIC_QR, Status: false");
        Assertions.assertThat(theiaLogs).doesNotContain("Dynamic Qr is Processed Successfully");
    }

    @Owner(HARSHITA)
    @Feature("PGP-41787")
    @Parameters({"theme"})
    @Test(description = "Verify UPI QR is present when UPI is disabled and UPIPUSH, UPIPUSHEXPRESS channels are enabled")
    public void PGP_41787_TC16(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.QR_ENABLED_MERCHANT;
        EnablePaymentMode enablePaymentMode = new EnablePaymentMode(new String[]{"UPIPUSH","UPIPUSHEXPRESS"}, "UPI");
        DisablePaymentMode disablePaymentMode = new DisablePaymentMode(new String[]{"UPI"}, "UPI");
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setEnablePaymentMode(new EnablePaymentMode[]{enablePaymentMode})
                .setDisablePaymentMode(new DisablePaymentMode[]{disablePaymentMode}).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String paymentModes = "body.merchantPayOption.paymentModes.paymentMode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(paymentModes)).containsOnly("UPI");
        String channelCodes = "body.merchantPayOption.paymentModes.find{it.paymentMode  == 'UPI'}.payChannelOptions.channelCode";
        Assertions.assertThat(fetchPaymentOptionsJson.getList(channelCodes)).containsOnly("UPIPUSH","UPIPUSHEXPRESS");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.imgScanPayQRCode().assertVisible();
        String QRText = cashierPage.qrCodeCheckoutJSText().getText();
        Assertions.assertThat(QRText).contains("Scan QR with Paytm or Any UPI App");
        String orderId = initTxnDTO.getBody().getOrderId();
        String grepcmd = "grep \"" + orderId + "\" "+LocalConfig.THEIA_LOGS + " | grep \"CREATE_DYNAMIC_QR\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("qrType=UPI_QR");
        Assertions.assertThat(theiaLogs).contains("Dynamic Qr is Processed Successfully");
    }

    @Parameters("theme")
    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as alphabetic in checkoutJS.")
    public void UpiNumericIdTestCase_06(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_COMMON_MERCHANT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        Thread.sleep(10000);
        cashierPage.UpiNumericId().sendKeys("abcd");
        cashierPage.proceedButton().click();
        String msg=cashierPage.errorTextsInUPIFlow().getText();
        //cashierPage.verifyUpiNumericID().click();
        Assert.assertEquals(msg,"Only numbers to be entered for UPI Number");

    }

    @Parameters("theme")
    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as alphanumeric in checkoutJS.")
    public void UpiNumericIdTestCase_07(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_COMMON_MERCHANT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        Thread.sleep(10000);
        cashierPage.UpiNumericId().sendKeys("12abcd");
        cashierPage.verifyUpiNumericID().click();
        Assert.assertEquals(cashierPage.vpaerrormsg().getText(),"Only numbers to be entered for UPI Number");

    }

    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as less than 10 digits in checkoutJS.")
    public void UpiNumericIdTestCase_08(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_COMMON_MERCHANT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        Thread.sleep(10000);
        cashierPage.UpiNumericId().sendKeys("12");
        cashierPage.verifyUpiNumericID().click();
        Assert.assertEquals(cashierPage.vpaerrormsg().getText(),"UPI Number can be 8 to 10 digit length only");

    }

    @Feature("PGP-42220")
    @Owner("Himanshu Arora")
    @Test(description = "validate upi numeric id error message when its passed as more than 10 digits in checkoutJS.")
    public void UpiNumericIdTestCase_09(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_COMMON_MERCHANT)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabUPI().click();
        cashierPage.waitUntilLoads();
        cashierPage.UpiNumericId().sendKeys("123456789012");
        cashierPage.verifyUpiNumericID().click();
        Assert.assertEquals(cashierPage.vpaerrormsg().getText(),"UPI Number can be 8 to 10 digit length only");

    }
    @Owner(PUSPA)
    @Feature("PGP-41865")
    @Parameters({"theme"})
    @Test(description = "Remove NB when bank is empty and Pref HIDE_NET_BANKING is enabled")
    public void hideNBwhenListisEmpty(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = HIDE_NB;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant).setTxnValue("2")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.groupedMerchantPayOption.other_options")).doesNotContain("NetBanking");
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.tabNetBanking().assertNotVisible();

    }

    public void dcfinder(FetchPaymentOption fetchPaymentOption, String path) throws Exception{
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body."+path+".savedInstruments[0].isCardCoBranded")).isEqualTo("true");
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body."+path+".savedInstruments[0].cardType")).isEqualTo("Credit Card");
        Integer i = 3;
        while (i >=0) {
            String card = fetchPaymentOptionsJson.getString("body." + path + ".savedInstruments[" + i + "].cardType");
            if (card.equalsIgnoreCase("Credit Card")) {
                System.out.println(card);
                i--;
            } else {
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body."+path+".savedInstruments[" + i + "].isCardCoBranded")).isEqualTo("true");
                Assertions.assertThat(fetchPaymentOptionsJson.getString("body."+path+".savedInstruments[" + i + "].cardType")).isEqualTo("Debit Card");
                break;
            }
        }
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-43589")
    @Parameters({"theme"})
    @Test(description = "Verify Cobranded Cards are present in merchantPayOption.savedInstruments with CC prior over DC")
    public void VerifyCobrandedCards(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
       User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken() , Constants.MerchantType.COBRANDED_DEPRIORITISE_DC).setTxnValue("10000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO
                .Builder("TXN_TOKEN", txnToken)
                .setMid(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC.getId())
                .setChannelId("WEB")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        dcfinder(fetchPaymentOption,"merchantPayOption");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-43383")
    @Parameters({"theme"})
    @Test(description = "Verify Cards are present in addMoneyPayOption.savedInstruments with CC prior over DC")
    public void VerifyDePrioritiseDC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.COBRANDED_DEPRIORITISE_DC).setTxnValue("10000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO
                .Builder("TXN_TOKEN", txnToken)
                .setMid(Constants.MerchantType.COBRANDED_DEPRIORITISE_DC.getId())
                .setChannelId("WEB")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        dcfinder(fetchPaymentOption,"addMoneyPayOption");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify Scan with any UPI App, is shown for the Qr rendered")
    public void verifyScanQrRevamp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.QR_ENABLED_MERCHANT_JS).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String Qrtext =cashierPage.qrCodeCheckoutJSTextNew().getText();
        Assertions.assertThat(Qrtext).contains("Scan with any UPI App");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that enabled Paymodes text colour is changes to black,for the Qr rendered")
    public void verifyEnabledPaymodeColourRevamp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String enabledColour = getColorCode(cashierPage.enabledPaymodes().getCssValue("color"));
        System.out.println(enabledColour);
        Assertions.assertThat(enabledColour).contains("#182233");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that or Scan with any UPI app in grey with UPI images is shown for the Qr rendered")
    public void verifyorScanwithUPItextRevamp(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PG2_JS_Checkout_Paytm_Domain).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String qrSubText = cashierPage.qrSubTextNew().getText();
        Assertions.assertThat(qrSubText).contains("& more");
        String qrSubTestColour = getColorCode(cashierPage.qrSubTextNew().getCssValue("color"));
        System.out.println(qrSubTestColour);
     //   Assertions.assertThat(qrSubTestColour).contains("#0f0f0f");
       cashierPage.qrCodeImgNew().assertVisible();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that Upi paymode should contain  UPI app images with & more text in non-logged in flow")
    public void verifyUpiPaymodeUpiAppImgNonLoggedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, PGOnly).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.upiPaymodeUpiAppImg().assertVisible();
        Assertions.assertThat(cashierPage.upiPaymodeUpiAppText().getText()).contains("& more");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-46447")
    @Parameters({"theme"})
    @Test(description = "Verify that Upi paymode should contain  UPI app images with & more text in logged in flow")
    public void verifyUpiPaymodeUpiAppImgLoggedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), PGOnly).setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.upiPaymodeUpiAppImg().assertVisible();
        Assertions.assertThat(cashierPage.upiPaymodeUpiAppText().getText()).contains("& more");
    }


    @Owner(GAURAV)
    @Feature("PGP-42134")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and theme is displayed on the checkoutjs cashier page header & footer logo in signed in flow")
    public void Verify_icici_cobranding_logo_header_footer_signedIn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ICICI_COBRANDING_CHILD)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_childMID,theme);

        config.data.setToken(txnToken);
        config.style=null;
        config.merchant.setMid(Constants.MerchantType.ICICI_COBRANDING_CHILD.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.easypaylogo().assertVisible();
        cashierPage.bankFooterLogo().assertVisible();
    }

    @Owner(GAURAV)
    @Feature("PGP-42134")
    @Parameters({"theme"})
    @Test(description = "Verify the icici cobranding logo and theme is displayed on the checkoutjs cashier page header & footer logo in signed out flow")
    public void Verify_icici_cobranding_logo_header_footer_signedOut(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO_childMID = new InitTxnDTO.Builder(null, Constants.MerchantType.ICICI_COBRANDING_CHILD)
                .build();
        String txnToken=NativeHelpers.Validate_InitTxn(initTxnDTO_childMID);
        MerchantConfig config=checkoutPage.loadMerchantConfig(initTxnDTO_childMID,theme);

        config.data.setToken(txnToken);
        config.style=null;
        config.merchant.setMid(Constants.MerchantType.ICICI_COBRANDING_CHILD.getId());
        config.data.setOrderId(initTxnDTO_childMID.orderFromBody());
        checkoutPage.createCheckoutJsOrder(config);

        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO_childMID.orderFromBody());
        cashierPage.waitUntilLoads();
        cashierPage.easypaylogo().assertVisible();
        cashierPage.bankFooterLogo().assertVisible();
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46970")
    @Parameters({"theme"})
    @Test(description = "Verify successful online transaction on offline merchants when static preference WHITELIST_OFFLINE_MID_ONLINE_TRANACTION is Y")
    public void VerifyJSflowWhenPreferenceIsOn(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.OFFLINE_WHITELISTED;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespMsg("Txn Success")
                .assertAll();
    }
    @Owner(GAURAV)
    @Feature("PGP-46539")
    @Parameters({"theme"})
    @Test(description = "Verify new UPI polling page with a successful transaction")
    public void verifyNewUpiPollingPageAndSuccessTxn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.PGOnly)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.UPI);
        cashierPage.upiPollingPageMobileLogo().isDisplayed();
        cashierPage.upiPollingPageInfoText().isDisplayed();
        cashierPage.upiPollingPageWarningText().isDisplayed();
        Assertions.assertThat(cashierPage.upiPollingPageTxnAmount().getText()).contains(initTxnDTO.txnAmountFromBody().replace(".00",""));
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .assertAll();
    }
    @Owner(AKSHAT_NAYAK)
    @Feature("PGP-46368")
    @Parameters({"theme"})
    @Test(description = "Verify EmiInfo details in ExtendInfo object in COP request on checkoutJs")
    public void verifyCOPEmiInfoOnCheckoutJS(@Optional("checkoutjs_web_revamp") String theme) throws IOException, InterruptedException {
        Constants.MerchantType merchantType = Constants.MerchantType.EmiInfo_COP;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setTxnValue("200")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.payBy(Constants.PayMode.EMI, paymentDTO);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("emiInfo\":\"{\"cardType\":\"CREDIT_CARD\"");
        Assertions.assertThat(logs).contains("\"MID\":\"qa14te01473048855559\"");
        Assertions.assertThat(logs).contains("\"cardNo\":\"0336\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"190.0\"");
        Assertions.assertThat(logs).contains("\"merchantName\":\"pg2EMI1\"");
        Assertions.assertThat(logs).contains("\"cardIssuer\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"bank\":\"HDFC\"");
        Assertions.assertThat(logs).contains("\"emiAmount\":\"67.22\"");
        Assertions.assertThat(logs).contains("\"ORDER_ID\":\"" + initTxnDTO.getBody().getOrderId() + "\"");
        Assertions.assertThat(logs).contains("\"interest\":\"11.66\"");
        Assertions.assertThat(logs).contains("\"emiMonths\":\"3\"");
        Assertions.assertThat(logs).contains("\"emiInterestRate\":\"5.0\"");
        Assertions.assertThat(logs).contains("\"planID\":\"HDFC|3\"");
    }


    @Owner(ROHIT_SHARMA)
    @Feature("PGP-47696")
    @Parameters({"theme"})
    @Test(description = "Verify that for deactive postpaid user account msg Your Postpaid account is not active. Please use other Payment option. should be displayed")
    public void postpaidDeactiveUimsg(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), PGOnly).setTxnValue("24")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String txt = cashierPage.postpaidContainer().getText();
        System.out.println(txt);
        Assertions.assertThat(txt).contains("Your Postpaid account is not active. Please use other Payment option");
    }

    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn of ICICI emi dc using new kfs changes")
    public void kfs_icici_dc(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validatePaymentMode("EMI_DC")
                .validateGatewayName("ICIE")
                .validateBankName("ICICI Bank")
                .assertAll();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that I adhere to Key Fact Statement and digital lending consent should be displayed on ui for icici emi-dc")
    public void kfs_icici_dc_adhere_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText().getText();
        Assertions.assertThat(adhere).contains("I adhere to Key Fact Statement and digital lending consent");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact Statement and digital lending consent should be displayed on ui for icici emi-dc and it is clickable as link")
    public void kfs_icici_dc_adhere_text_link(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertClickable();
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page ")
    public void kfs_icici_dc_kfs_upper_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsUpperText().getText();
        Assertions.assertThat(kfsUpper).contains("Key Fact statement - EMI on Debit Card");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page ")
    public void kfs_icici_dc_kfs_bank_date_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsBankDateText().getText();
        Assertions.assertThat(kfsUpper).contains("Name of Regulated entity - ICICI");
        Assertions.assertThat(kfsUpper).contains("Date:");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text should contain all the parameters")
    public void kfs_icici_dc_kfs_table_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("Parameter");
        Assertions.assertThat(kfsUpper).contains("Details");
        Assertions.assertThat(kfsUpper).contains("Loan Amount (amount disbursed / to be disbursed to the borrower) in Rupees");
        Assertions.assertThat(kfsUpper).contains("Total interest charge during the entire tenor of the loan (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Other up-front charges, if any");
        Assertions.assertThat(kfsUpper).contains("a. GST applicable on Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("b. Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Net Disbursed Amount (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Total Amount to be paid by the borrower (sum of (i),(ii) and (iii)) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate - Effective annualised interest rate (in percentage) computed on net disbursed amount using IRR approach and reducing balance method (APR is exclusive of GST on processing fees)");
        Assertions.assertThat(kfsUpper).contains("Tenure of the loan in months");
        Assertions.assertThat(kfsUpper).contains("Repayment frequency");
        Assertions.assertThat(kfsUpper).contains("No. of instalments of repayment");
        Assertions.assertThat(kfsUpper).contains("Amount of each instalment of repayment in Rupees");
        Assertions.assertThat(kfsUpper).contains("Monthly");
        Assertions.assertThat(kfsUpper).contains("Details about Contingent Charges");
        Assertions.assertThat(kfsUpper).contains("Rate of annualised penal charges in case of delayed payments");
        Assertions.assertThat(kfsUpper).contains("Rate of annualized other penal charges");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period during which borrower shall not be charged any penalty on prepayment of loan");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/digital lending related complaints/issues");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower*");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS Disclaimer text should be displayed on KFS page ")
    public void kfs_icici_dc_kfs_disclaimer_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsDisclaimerText().getText();
        Assertions.assertThat(kfsUpper).contains("Note: There could be a difference in the amount to be paid by the borrower mentioned in (v) and that in repayment schedule shared because of any rounding off of the instalment amount in the repayment schedule.");
        Assertions.assertThat(kfsUpper).contains("*No recovery agent is assigned for loan account. In case any recovery agent is assigned, then particulars of such recovery agent will be communicated before the recovery agent contacts for recovery.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS loan text should be displayed on KFS page ")
    public void kfs_icici_dc_kfs_loan_consent_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsLoanConsentText().getText();
        Assertions.assertThat(kfsUpper).contains("I Read And Agreed");
        Assertions.assertThat(kfsUpper).contains("I/We expressly authorize and give consent to ICICI Bank to, disclose,transfer or part with any of my/our information, (including location), or an other device information when ICICI Bank considers such disclosure as necessary,with:");
        Assertions.assertThat(kfsUpper).contains("Agents of ICICI Bank, ICICI Bank's group entities in any jurisdiction");
        Assertions.assertThat(kfsUpper).contains("Auditors, credit rating agencies/credit bureaus, statutory/regulatory authorities,governmental/administrative authorities,Central Know Your Customer(CKYC) registery or SEBI Know Your Client registration agency,having jurisdiction over ICICI Bank or its group entities;");
        Assertions.assertThat(kfsUpper).contains("Service providers,or such person with whom ICICI Bank contracts or puproses to contract;");
        Assertions.assertThat(kfsUpper).contains("(Collectively referred to as \"Permitted Persons\")");
        Assertions.assertThat(kfsUpper).contains("For the purposes of:");
        Assertions.assertThat(kfsUpper).contains("Provision of the facility and completion of non-onboarding formalities; or");
        Assertions.assertThat(kfsUpper).contains("Complying with KYC requirements; or");
        Assertions.assertThat(kfsUpper).contains("Compliance with applicable laws or any order (judicial or otherwise),statutory/regulatory requirement or;");
        Assertions.assertThat(kfsUpper).contains("for credit reveiew of facilities availed; or");
        Assertions.assertThat(kfsUpper).contains("Authentication or verification; or");
        Assertions.assertThat(kfsUpper).contains("research or analysis, credit reporting and scoring,risk management,participation in any telecommunication; or");
        Assertions.assertThat(kfsUpper).contains("electronic clearing network and for use or processing of the said information/data");
        Assertions.assertThat(kfsUpper).contains("Disclosing any default in payment, for the purposes of recovering such amounts.");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS Table Data ")
    public void kfs_icici_dc_kfs_table_Verifivation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfscloanAmount().getText();
        Assertions.assertThat(kfsUpper).contains("2000");
        String kfstenure = cashierPage.kfsctenure().getText();
        Assertions.assertThat(kfstenure).contains("3");
        String kfsinstallments = cashierPage.kfsinstallments().getText();
        Assertions.assertThat(kfsinstallments).contains("3");
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that I adhere to Key Fact Statement should be displayed on ui for icici emi-dc",enabled = false)
    public void kfs_hdfc_dc_adhere_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        String adhere = cashierPage.kfsAdhereText().getText();
        Assertions.assertThat(adhere).contains("I adhere to Key Fact Statement");
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that Key Fact Statement should be displayed on ui for hdfc emi-dc and it is clickable as link",enabled = false)
    public void kfs_hdfc_dc_adhere_text_link(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().assertClickable();
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page for hdfc dc emi",enabled = false)
    public void kfs_hdfc_dc_kfs_upper_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsUpperText().getText();
        Assertions.assertThat(kfsUpper).contains("Key Fact statement - EMI on Debit Card");
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that Key Fact statement - EMI on Debit Card is displayed on KFS page ",enabled = false)
    public void kfs_hdfc_dc_kfs_bank_date_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsBankDateText().getText();
        Assertions.assertThat(kfsUpper).contains("Name of Regulated entity - HDFC");
        Assertions.assertThat(kfsUpper).contains("Date:");
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that KFS table text should contain all the parameters for hdfc emi dc",enabled = false)
    public void kfs_hdfc_dc_kfs_table_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("Parameter");
        Assertions.assertThat(kfsUpper).contains("Details");
        Assertions.assertThat(kfsUpper).contains("Loan Amount (amount disbursed / to be disbursed to the borrower) in Rupees");
        Assertions.assertThat(kfsUpper).contains("Total interest charge during the entire tenor of the loan (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Other up-front charges, if any");
        Assertions.assertThat(kfsUpper).contains("a. GST applicable on Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("b. Processing Fees (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Net Disbursed Amount (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Total Amount to be paid by the borrower (sum of (i),(ii) and (iii)) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate - Effective annualised interest rate (in percentage) computed on net disbursed amount using IRR approach and reducing balance method (APR is exclusive of GST on processing fees)");
        Assertions.assertThat(kfsUpper).contains("Tenure of the loan in months");
        Assertions.assertThat(kfsUpper).contains("Repayment frequency");
        Assertions.assertThat(kfsUpper).contains("No. of instalments of repayment");
        Assertions.assertThat(kfsUpper).contains("Amount of each instalment of repayment in Rupees");
        Assertions.assertThat(kfsUpper).contains("Monthly");
        Assertions.assertThat(kfsUpper).contains("Details about Contingent Charges");
        Assertions.assertThat(kfsUpper).contains("Rate of annualised penal charges in case of delayed payments");
        Assertions.assertThat(kfsUpper).contains("Rate of annualized other penal charges");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period during which borrower shall not be charged any penalty on prepayment of loan");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/digital lending related complaints/issues");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower*");
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that KFS Disclaimer text should be displayed on KFS page hdfc emi dc",enabled = false)
    public void kfs_hdfc_dc_kfs_disclaimer_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsDisclaimerText().getText();
        Assertions.assertThat(kfsUpper).contains("Note: There could be a difference in the amount to be paid by the borrower mentioned in (v) and that in repayment schedule shared because of any rounding off of the instalment amount in the repayment schedule.");
        Assertions.assertThat(kfsUpper).contains("*No recovery agent is assigned for loan account. In case any recovery agent is assigned, then particulars of such recovery agent will be communicated before the recovery agent contacts for recovery.");
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    @Test(description = "Verify that KFS Table Data for hdfc emi dc",enabled = false)
//    //HDFC_KFS has changed into new format
    public void kfs_hdfc_dc_kfs_table_Verifivation(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfscloanAmount().getText();
        Assertions.assertThat(kfsUpper).contains("2000");
        String kfstenure = cashierPage.kfsctenure().getText();
        Assertions.assertThat(kfstenure).contains("3");
        String kfsinstallments = cashierPage.kfsinstallments().getText();
        Assertions.assertThat(kfsinstallments).contains("3");
    }
    @Owner(ROHIT_SHARMA)
    @Feature("PGP-48507")
    @Parameters({"theme"})
    @Test(description = "Verify paybutton is  in disabled state if kfs consent is not clicked for ICICI emi dc using new kfs changes")
    public void kfs_icici_dc_disabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4572741654006328");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        cashierPage.buttonPGPayNow().assertNotClickable();
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify paybutton is  in disabled state if kfs consent is not clicked for HDFC emi dc using new kfs changes",enabled = false)
    public void kfs_hdfc_dc_disabled(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        cashierPage.buttonPGPayNow().assertNotClickable();
    }
//    @Owner(ROHIT_SHARMA)
//    @Feature("PGP-48507")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify  txn of HDFC emi dc using new kfs changes",enabled = false)
    public void kfs_hdfc_dc(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), EmiInfo_COP).setTxnValue("2000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType("4444333322221111");
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfscloseButton().click();
        cashierPage.buttonPGPayNow().assertClickable();
    }

    @Owner(AKSHAT)
    @Feature("PGP-48530")
    @Parameters({"theme"})
    @Test(description = "Verify Bank Transaction id is returned for successful Subscription for Checkout js flow Native Json Pref N")
    public void WalletSubs_BankTxnId_returned(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = NATIVE_WALLET_ONLY;
        WalletHelpers.modifyBalance(user, 2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("YEAR")
                .setSubscriptionGraceDays("3")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("1")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();
     //   cashierPage.tabPPI().click();
        cashierPage.checkBoxPPI().check();
        cashierPage.buttonWalletPayNow().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .assertAll();
    }

    @Owner(AKSHAT)
    @Feature("PGP-54581")
    @Parameters({"theme"})
    @Test(description = "Verify wallet is not visible on Checkout Js for subscription transaction")
    public void walletNotReturned_subsCheckoutJs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        // ff4j theia.disable.balance.forSubscriptionEligibility should be ON

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = NATIVE_WALLET_ONLY;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("0")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();

        Assert.assertFalse(cashierPage.checkBoxPPI().isElementPresent());

    }

    @Owner(AKSHAT)
    @Feature("PGP-54581")
    @Parameters({"theme"})
    @Test(description = "Verify PPBL is not visible on Checkout Js for subscription transaction")
    public void ppblNotReturned_subsCheckoutJs(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        // ff4j theia.disable.ppbl.forSubscriptionEligibility should be ON

        User user = userManager.getForWrite(Label.BASIC);
        Constants.MerchantType merchant = PPBL_NB;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("1")
                .setSubscriptionPaymentMode("")
                .setSubscriptionAmountType("VARIABLE")
                .setSubscriptionMaxAmount("10")
                .setSubscriptionFrequency("1")
                .setSubscriptionFrequencyUnit("MONTH")
                .setSubscriptionGraceDays("0")
                .setSubscriptionStartDate(CommonHelpers.getDate().toString())
                .setRequestType("NATIVE_SUBSCRIPTION")
                .setSubscriptionRetryCount("0")
                .build();
        InitTxnResponseDTO initTxnResponseDTO = validateSuccessInitiateSubscription(initTxnDTO);
        String txnToken = initTxnResponseDTO.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        cashierPage.waitUntilLoads();

        Assert.assertFalse(cashierPage.checkboxPPBL().isElementPresent());

    }


    @Owner(MAYURI)
    @Feature("PGP-48241")
    @Parameters({"theme"})
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_MODIFY for simplified only amount based subvention+BO")
    public void TestParamsInCOPRequestWhenSimplifiedUnifiedOfferAmountBasedSubventionAndBO(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO00012344");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","","500");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_ORDER_MODIFY", "REQUEST");
        Assertions.assertThat(logs).contains("loanAmount");
        Assertions.assertThat(logs).contains("subvention");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"true\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"SUBVENTION\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"2000.0\"");
        Assertions.assertThat(logs).contains("emiAmount");
        Assertions.assertThat(logs).contains("subventionAmount");
        Assertions.assertThat(logs).contains("\"subventionType\":\"DISCOUNT\"");
    }

    @Owner(MAYURI)
    @Feature("PGP-48241")
    @Parameters({"theme"})
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_MODIFY for simplified only amount based subvention")
    public void TestParamsInCOPRequestWhenSimplifiedUnifiedOfferOnlyAmountBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_ORDER_MODIFY", "REQUEST");
        Assertions.assertThat(logs).contains("loanAmount");
        Assertions.assertThat(logs).contains("subvention");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"true\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"SUBVENTION\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"2000.0\"");
        Assertions.assertThat(logs).contains("emiAmount");
        Assertions.assertThat(logs).contains("subventionAmount");
        Assertions.assertThat(logs).contains("\"subventionType\":\"DISCOUNT\"");
    }

    @Owner(MAYURI)
    @Feature("PGP-48241")
    @Parameters({"theme"})
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_MODIFY for simplified only amount based BO")
    public void TestParamsInCOPRequestWhenSimplifiedUnifiedOfferOnlyBO(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO00012344");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","","500");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_ORDER_MODIFY", "REQUEST");
        Assertions.assertThat(logs).contains("loanAmount");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"false\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"STANDARD\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"2000.0\"");
        Assertions.assertThat(logs).contains("emiAmount");
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "SearchBox visibility for more than three offers applied on merchant and able to search over")
    public void ApplyOffer(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.OFFERS);
        String SsoToken = user.ssoToken();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SsoToken, Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1000.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over EMI paymode after applying EMI Offer on merchant")
    public void ApplyOfferOnEMI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.OFFERS);
        String SsoToken = user.ssoToken();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SsoToken, Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1000.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("EMI");
        cashierPage.dropDownEMIOffers().click();
        cashierPage.applyOffer("1").click();
        sleep(3000);
        cashierPage.offerVisibleEMI().assertVisible();
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over UPI paymode after applying UPI Offer on merchant")
    public void ApplyOfferOnUPI(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.OFFERS);
        String SsoToken = user.ssoToken();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SsoToken, Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1000.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("UPI");
        cashierPage.applyOffer("1").click();
        sleep(5000);
        cashierPage.UPIwindow().isDisplayed();
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over Paytm paymode after applying wallet on merchant")
    public void ApplyOfferOnWallet(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.OFFERS);
        String SsoToken = user.ssoToken();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SsoToken, Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1100.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("Paytm");
        cashierPage.applyOfferWallet().click();
        cashierPage.walletcheckbox().isSelected();
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over Paytm paymode after applying Postpaid on merchant")
    public void ApplyOfferOnPostpaid(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        User user = userManager.getForRead(Label.OFFERS);
        String SsoToken = user.ssoToken();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SsoToken, Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1100.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("Paytm");
        cashierPage.applyOffer("1").click();
        cashierPage.checkedPPIForCheckoutJS().isSelected();
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over Netbanking paymode after applying Netbanking Offer on merchant")
    public void ApplyOfferOnNetbanking(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.OFFERS);
        String SsoToken = user.ssoToken();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SsoToken, Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1000.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("Netbanking");
        cashierPage.applyOffer("1").click();
        sleep(10000);
        cashierPage.checkboxNB().isSelected();
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over Paytm paymode after applying wallet on merchant")
    public void ApplyOfferOnWalletForPreLogin(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1100.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("Paytm");
        cashierPage.applyOfferWallet().click();
        String errorText = cashierPage.errorTextMessage().getText();
        Assertions.assertThat(errorText).contains("Please use the below option to avail the offer");
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over Paytm paymode after applying Postpaid on merchant")
    public void ApplyOfferOnPostpaidForPreLogin(@Optional("checkoutjs_web_revamp") String theme) throws Exception {

        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1100.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("Paytm");
        cashierPage.applyOffer("1").click();
        String errorText = cashierPage.errorTextMessage().getText();
        Assertions.assertThat(errorText).contains("Please use the below option to avail the offer");
    }
    @Owner(UPAMA)
    @Feature("PGP-45673")
    @Parameters({"theme"})
    @Test(description = "To check navigation over card after applying card offer on merchant")
    public void ApplyOfferOnCard(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(Label.OFFERS);
        String SsoToken = user.ssoToken();
        ArrayList<String> promoCode= new ArrayList<>();
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","false","true","");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new SimplifiedUnifiedOffers.SubventionDetails("true","1000","2341305","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(SsoToken, Constants.MerchantType.APPLY_OFFER_MID)
                .setTxnValue("1100.00")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.txnToken")).isNotNull();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.ViewAll().isElementPresent())
        {
            cashierPage.ViewAll().click();
        }
        cashierPage.searchBox().assertVisible();
        cashierPage.searchBox().sendKeys("card");
        cashierPage.applyOffer("1").click();
        cashierPage.checkboxCard().assertVisible();
    }

    @Owner(MAYURI)
    @Feature("PGP-46700")
    @Parameters({"theme"})
    @Test(description = "Verify offerid in ADS for simplified only amount based subvention+BO, offerid not passed")
    public void TestOfferIDInADSRequestWhenSimplifiedUnifiedOfferAmountBasedSubventionAndBO(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("MULTITIEMTESTQADC6");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","","500");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isElementPresent()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).doesNotContain("offerId");
    }

    @Owner(MAYURI)
    @Feature("PGP-46700")
    @Parameters({"theme"})
    @Test(description = "Verify offerid in ADS for simplified only amount based subvention has offerID and BO without offerid  passed")
    public void TestOfferIDInADSRequestWhenSimplifiedUnifiedOfferAmountBasedSubventionWithOfferIdAndBO(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO00012344");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"true","true","true","","500");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2384097","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isElementPresent()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"emiOfferId\":\"2384097\"");
        Assertions.assertThat(logs).doesNotContain("offerId");

    }

    @Owner(MAYURI)
    @Feature("PGP-46700")
    @Parameters({"theme"})
    @Test(description = "Verify offerid in ADS for simplified only amount based subvention without offerID and BO with offerid  passed")
    public void TestOfferIDInADSRequestWhenSimplifiedUnifiedOfferAmountBasedSubventionAndBOWithOfferId(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(null,"true","true","true","2341305","2000");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isElementPresent()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"offerId\":\"2341305\"");
        Assertions.assertThat(logs).doesNotContain("emiOfferId");    }


    @Owner(MAYURI)
    @Feature("PGP-46700")
    @Parameters({"theme"})
    @Test(description = "Verify offerid in ADS for simplified only amount based subvention with offerID and BO with offerid  passed")
    public void TestOfferIDInADSRequestWhenSimplifiedUnifiedOfferAmountBasedSubventionWithOfferIdAndBOWithOfferId(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = EMI_DISCOVERY;
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(null,"false","true","true","2341305");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","2000","2378530","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2000")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isElementPresent()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.payBy(Constants.PayMode.EMI,paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"offerId\":\"2341305\"");
        Assertions.assertThat(logs).contains("\"offerId\":\"2378530\"");
    }

//    @Owner(PUSPA)
//    @Feature("PGP-56124")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that KFS table text should contain HDFC Bank Website,Privacy Policy,RBI Complaint Management system,RBI Sachet portal hdfc emi dc",enabled = false)
    public void kfs_hdfc_dc_text(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.viewAllOffersAvialable().isElementPresent()) {
            cashierPage.viewAllOffersAvialable().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
        cashierPage.checkEMIEligibility().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("HDFC Bank Website");
        Assertions.assertThat(kfsUpper).contains("Privacy Policy");
        Assertions.assertThat(kfsUpper).contains("RBI Complaint Management system");
        Assertions.assertThat(kfsUpper).contains("RBI Sachet portal");

    }

//    @Owner(PUSPA)
//    @Feature("PGP-56124")
//    @Parameters({"theme"})
//    //HDFC_KFS has changed into new format
//    @Test(description = "Verify that KFS table text should contain HDFC Bank Website link,Privacy Policy link,RBI Complaint Management system link,RBI Sachet portal link hdfc emi dc",enabled = false)
    public void kfs_hdfc_dc_link(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        if(cashierPage.viewAllOffersAvialable().isElementPresent()) {
            cashierPage.viewAllOffersAvialable().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
        cashierPage.checkEMIEligibility().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType("12");
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("25");
        cashierPage.textBoxCVVNumber().clearAndType("226");
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
        String kfsUpper = cashierPage.kfsTableText().getText();
        Assertions.assertThat(kfsUpper).contains("https://www.hdfcbank.com/");
        Assertions.assertThat(kfsUpper).contains("https://www.hdfcbank.com/personal/useful-links/privacy?LGCode=MKTG&icid=website_organic_hdfc_search&mc_id=website_organic_hdfc_search");
        Assertions.assertThat(kfsUpper).contains("https://cms.rbi.org.in/");
        Assertions.assertThat(kfsUpper).contains("https://sachet.rbi.org.in/");

    }

    @Owner(PUSPA)
    @Feature("PGP-56062")
    @Parameters({"theme"})
    @Test(description = "verify promocode only promo code is send in req")
    public void onlypromocode_in_offerDiscovery(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123114");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isDisplayed()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"promocode\":[\"PROMO000123114\"]");
    }
    @Owner(PUSPA)
    @Feature("PGP-56062")
    @Parameters({"theme"})
    @Test(description = "verify promocode for multiple promo code is send in req")
    public void multipromocode_in_offerDiscovery(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123114");
        promoCode.add("PROMO00012344");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","true","");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isDisplayed()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"promocode\":[\"PROMO000123114\",\"PROMO00012344\"]");

    }
    @Owner(PUSPA)
    @Feature("PGP-56062")
    @Parameters({"theme"})
    @Test(description = "verify promocode with promo code Id is send in req")
    public void promocodeId_withSubventionId_offerDiscovery(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;

        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(null,"false","true","true","2391836");

        SimplifiedUnifiedOffers.SubventionDetails subventionDetails= new
                SimplifiedUnifiedOffers.SubventionDetails("true","1000","2378530","");

        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(subventionDetails,promoDetails);

        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isDisplayed()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"offerDetails\":{\"emiOfferDetails\":{\"offerId\":\"2378530\"},\"bankOfferDetails\":[{\"offerId\":\"2391836\"}]}}");


    }

    @Owner(PUSPA)
    @Feature("PGP-56062")
    @Parameters({"theme"})
    @Test(description = "verify both promocode and promo id is send in req")
    public void promocode_Id_in_offerDiscovery(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String txnAmount="1100";
        Constants.MerchantType merchantType= Constants.MerchantType.EMI_DISCOVERY;
        ArrayList<String> promoCode= new ArrayList<>();
        promoCode.add("PROMO000123114");
        SimplifiedUnifiedOffers.PromoDetails promoDetails= new
                SimplifiedUnifiedOffers.PromoDetails(promoCode,"false","true","true","2391836");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers= new
                SimplifiedUnifiedOffers(promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(null, merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO().setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
        if(cashierPage.offerStripHideButton().isDisplayed()) {
            cashierPage.offerStripHideButton().click();
        }
        cashierPage.tabEMI().click();
        cashierPage.dropdownEmiBanksV5().selectByVisibleText("HDFC");

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"promocode\":[\"PROMO000123114\"]");

    }

    @Owner(AJEESH)
    @Feature("PGP-56181")
    @Parameters({"theme"})
    @Test(description = "Verify Callback logger is printed for 443 port")
    public void verifyLoggerforCallbackPort443(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("20")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("DC")
                .validateRespMsg("Txn Success")
                .assertAll();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia.log | " + "grep \"validateCallbackUrl()\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("Port 443 found in callback url");



    }

    @Owner(AJEESH)
    @Feature("PGP-56181")
    @Parameters({"theme"})
    @Test(description = "Verify Callback logger is printed for 80 port")
    public void verifyLoggerforCallbackPort80(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchant = Constants.MerchantType.PGOnly;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder("", merchant)
                .setTxnValue("20")
                .setCallbackUrl("http://10.170.7.123:80/mockbank/MerchantSite/bankResponse")
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        Assertions.assertThat(initTxnResponse.getBody().getTxnToken()).isNotEmpty();
        String orderId = initTxnDTO.getBody().getOrderId();
        String txnToken = initTxnResponse.getBody().getTxnToken();
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();

        cashierPage.payBy(Constants.PayMode.DC);
        DriverManager.getDriver().findElement(By.id("proceed-button")).click();
        String grepcmd = "grep \"" + merchant.getId() + "\" /paytm/logs/theia.log | " + "grep \"validateCallbackUrl()\"";
        String theiaLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaLogs).contains("Port 80 found in callback url");



    }

    public void emiDCPaymodeKFS(CashierPage cashierPage,PaymentDTO emidetails)
    {
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(PaymentDTO.DEBIT_CARD_NUMBER);
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("9654773125");
        cashierPage.checkEMIEligibility().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(emidetails.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(emidetails.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(emidetails.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
    }
    @Owner(PUSPA)
    @Feature("PGP-57180")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for HDFC-Part 1")
    public void kfsNew_hdfc_dc_part1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpper).contains("Loan proposal/ account No.").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpper).contains("Type of Loan").contains("DC EMI");
        Assertions.assertThat(kfsUpper).contains("Sanctioned Loan amount (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Disbursal schedule\n" +
                "(i) Disbursement in stages or 100% upfront.\n" +
                "(ii) If it is stage wise, mention the clause of loan agreement having relevant details").contains("100% Upfront");
        Assertions.assertThat(kfsUpper).contains("Loan term (year/months/days)");
        Assertions.assertThat(kfsUpper).contains("Instalment details");
        Assertions.assertThat(kfsUpper).contains("Type of instalments");
        Assertions.assertThat(kfsUpper).contains("Number of EPIs");
        Assertions.assertThat(kfsUpper).contains("EPI (₹)");
        Assertions.assertThat(kfsUpper).contains("Commencement of repayment, post sanction");
        Assertions.assertThat(kfsUpper).contains("Interest rate (%) and type (fixed or floating or hybrid)");
        Assertions.assertThat(kfsUpper).contains("Additional Information in case of Floating rate of interest");
        Assertions.assertThat(kfsUpper).contains("Fee / Charges");
        Assertions.assertThat(kfsUpper).contains("Processing fees").contains("One Time").contains("199.0 + GST");
        Assertions.assertThat(kfsUpper).contains("Insurance charges");
        Assertions.assertThat(kfsUpper).contains("Valuation fees");
        Assertions.assertThat(kfsUpper).contains("Any other (please specify)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate (APR) (%)");
        Assertions.assertThat(kfsUpper).contains("Details of Contingent Charges (in ₹ or %, as applicable)");
        Assertions.assertThat(kfsUpper).contains("Penal charges, if any, in case of delayed payment").contains("450/- plus all applicable government levied taxes");
        Assertions.assertThat(kfsUpper).contains("Other penal charges, if any");
        Assertions.assertThat(kfsUpper).contains("Foreclosure charges, if applicable").contains("3% plus applicable government levied taxes");
        Assertions.assertThat(kfsUpper).contains("Charges for switching of loans from floating to fixed rate and vice versa");
        Assertions.assertThat(kfsUpper).contains("Any other charges (please specify)").contains("Late EMI payment fee of 300/- plus all applicable government levied taxes");


    }

    @Owner(PUSPA)
    @Feature("PGP-57180")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for HDFC-Part 2")
    public void kfsNew_hdfc_dc_part2(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpper).contains("Clause of Loan agreement relating to engagement of recovery agents");
        Assertions.assertThat(kfsUpper).contains("Clause of Loan agreement which details grievance redressal mechanism");
        Assertions.assertThat(kfsUpper).contains("Phone number and email id of the nodal grievance redressal officer").contains("Toll Free number: 1800 266 4060\n" +
                "Email: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpper).contains("Whether the loan is, or in future maybe, subject to transfer to other REs or securitisation (Yes/ No)");
        Assertions.assertThat(kfsUpper).contains("In case of lending under collaborative lending arrangements (e.g., co-lending/ outsourcing), following additional details may be furnished:");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period, in terms of RE's board approved policy, during which borrower shall not be charged any penalty on prepayment of loan").contains("3 days");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower.").contains("https://www.hdfcbank.com/content/bbp/repositories/723fb80a-2dde-42a3-9793-7ae1be57c87f/?path=/Common%20Overlays/Feedback/PDFS/Banking%20Ombudsman%20Scheme/Active-Collection-Vendor-List.pdf");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/ digital lending related complaints/ issues").contains("Mr. Kannan Ramaseshan\n" +
                "HDFC Bank Ltd. Grievance\n" +
                "Redressal Cell, 1st Floor,\n" +
                "Empire Plaza - 1, Lal Bahadur\n" +
                "Shastri Marg, Chandan Nagar,\n" +
                "Vikhroli West, Mumbai-\n" +
                "400083 Maharashtra\n" +
                "Monday to Saturday: 9:30 am to 5:30 pm\n" +
                "This facility is not available on 2nd & 4th Saturdays, all Sundays, and Bank Holidays");
    }

    @Owner(PUSPA)
    @Feature("PGP-57180")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for HDFC-Annex B")
    public void kfsNew_hdfc_dc_annexB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNewAnnexB().getText();
        Assertions.assertThat(kfsUpper).contains("Sanctioned Loan amount (in Rupees) (Sl no. 2 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("Loan Term (in years/ months/ days) (Sl No.4 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("No. of instalments for payment of principal, in case of non-equated periodic loans");
        Assertions.assertThat(kfsUpper).contains("Type of EPI\n" +
                "Amount of each EPI (in Rupees) and\n" +
                "nos. of EPIs (e.g., no. of EMIs in case of monthly instalments) (Sl No. 5 of the KFS template - Part 1)");
    }

    @Owner(PUSPA)
    @Feature("PGP-57180")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for HDFC-Annex C")
    public void kfsNew_hdfc_dc_annexC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        Assertions.assertThat(cashierPage.kfsRepayment_Date().getText()).isEqualTo("Date:" + " " + dtf.format(now));
        Assertions.assertThat(cashierPage.kfsRepayment_refNo().getText()).isEqualTo("Reference Number:");
        Assertions.assertThat(cashierPage.kfsRepayment_Customer().getText()).contains("Customer");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).isEqualTo("Loan Type: DEBIT CARD ONLINE");
        Assertions.assertThat(cashierPage.kfsRepayment_tenure().getText()).contains("Tenure:");
        Assertions.assertThat(cashierPage.kfsRepayment_amt().getText()).contains("Amount Financed");
        Assertions.assertThat(cashierPage.kfsRepayment_instal().getText()).contains("Total Installment");
        Assertions.assertThat(cashierPage.kfsRepayment_freq().getText()).isEqualTo("Frequency: Monthly");
        Assertions.assertThat(cashierPage.kfsRepayment_CurrINR().getText()).isEqualTo("Currency: INR");
    }

    @Owner(PUSPA)
    @Feature("PGP-57180")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for HDFC-Table Text")
    public void kfsNew_hdfc_dc_tableText(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNewtable().getText();
        Assertions.assertThat(kfsUpper).contains("No.");
        Assertions.assertThat(kfsUpper).contains("Statement Date");
        Assertions.assertThat(kfsUpper).contains("Principal Amount");
        Assertions.assertThat(kfsUpper).contains("Statement Date");
        Assertions.assertThat(kfsUpper).contains("Interest (Excluding GST*)");
        Assertions.assertThat(kfsUpper).contains("Balance");

    }

    @Owner(PUSPA)
    @Feature("PGP-57180")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for HDFC-ImpLinks")
    public void kfsNew_hdfc_dc_ImpLinks(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfs_impLinks().getText();
        Assertions.assertThat(kfsUpper).contains("HDFC Bank Website").contains("https://www.hdfcbank.com/");
        Assertions.assertThat(kfsUpper).contains("Privacy Policy").contains("https://www.hdfcbank.com/personal/useful-links/privacy?LGCode=MKTG&icid=website_organic_hdfc_search&mc_id=website_organic_hdfc_search");
        Assertions.assertThat(kfsUpper).contains("RBI Complaint Management system").contains("https://cms.rbi.org.in/");
        Assertions.assertThat(kfsUpper).contains("RBI Sachet portal").contains("https://sachet.rbi.org.in/");
        Assertions.assertThat(kfsUpper).contains("Grievance Redressal Officer for Digital Lending Products").contains("https://www.hdfcbank.com/personal/need-help/grievance-redressal-digital");

    }
    @Owner(PUSPA)
    @Feature("PGP-57359")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS for vernacular support")
    public void kfsNew_hdfc_dc_vernacular(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        cashierPage.pause(2);
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Hindi");
        cashierPage.kfsRepayment_instal().assertNotVisible();
        cashierPage.kfsRepayment_freq().assertNotVisible();
        cashierPage.kfsRepayment_CurrINR().assertNotVisible();

    }
    @Owner(PUSPA)
    @Feature("PGP-57359")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS for vernacular support -Text")
    public void kfsNew_hdfc_dc_vernacularText(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("5000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        cashierPage.pause(2);
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Hindi");
        cashierPage.pause(2);
        String kfsUpper = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpper).contains("ऋण प्रस्ताव/खाता सं.").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpper).contains("ऋण का प्रकार").contains("डीसीईएमआई");
        Assertions.assertThat(kfsUpper).contains("स्वीकृत ऋण राशि (रुपयों में)");
        Assertions.assertThat(kfsUpper).contains("डिस्बर्सल शेड्यूल\n" +
                "(i) चरणों में संवितरण अथवा 100% अपफ्रंट।\n" +
                "(ii) यदि यह चरण-वार है, तो संगत विवरण वाले ऋण करार के खंड का उल्लेख करें").contains("100% अग्रिम");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).contains("ऋण का प्रकार : डेबिट कार्ड ऑनलाइन");

        cashierPage.dropdownLanguageSupport().selectByVisibleText("Assamese");
        cashierPage.pause(2);
        String kfsUpperAssamese = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperAssamese).contains("ঋণ প্ৰস্তাৱ/ একাউণ্ট নং").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpperAssamese).contains("ঋণৰ প্ৰকাৰ").contains("ডিচিইএমআই");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).contains("ঋণৰ প্ৰকাৰ : ডেবিট কাৰ্ড অনলাইন");

        cashierPage.dropdownLanguageSupport().selectByVisibleText("Bengali");
        cashierPage.pause(2);
        String kfsUpperBengali = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperBengali).contains("ঋণ প্রস্তাব/হিসাব নং").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpperBengali).contains("ঋণের ধরন").contains("ডিসিইএমআই");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).contains("ঋণের ধরন: ডেবিট কার্ড অনলাইন");

        cashierPage.dropdownLanguageSupport().selectByVisibleText("Gujarati");
        cashierPage.pause(2);
        String kfsUpperGujrati = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperGujrati).contains("લોન પ્રપોઝલ/એકાઉન્ટ નં.").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpperGujrati).contains("લોનનો પ્રકાર").contains("DCEMI");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).contains("લોનનો પ્રકારઃ ડેબિટ કાર્ડ ઓનલાઈન");

        cashierPage.dropdownLanguageSupport().selectByVisibleText("Kannada");
        cashierPage.pause(2);
        String kfsUpperKannada = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperKannada).contains("ಸಾಲದ ಪ್ರಸ್ತಾಪ/ ಖಾತೆ ಸಂಖ್ಯೆ.").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpperKannada).contains("ಸಾಲದ ವಿಧ").contains("DCEMI");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).contains("ಡೆಬಿಟ್ ಕಾರ್ಡ್ ಆನ್ ಲೈನ್");
        
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Malayalam");
        cashierPage.pause(2);
        String kfsUpperMalayalam = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperMalayalam).contains("ലോൺ പ്രൊപ്പോസൽ / അക്കൗണ്ട് നമ്പർ").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpperMalayalam).contains("വായ്പയുടെ തരം").contains("DCEMI");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).contains("ലോൺ തരം : ഡെബിറ്റ് കാർഡ് ഓൺലൈൻ");

        cashierPage.dropdownLanguageSupport().selectByVisibleText("Punjabi");
        cashierPage.pause(2);
        String kfsUpperPunjabi = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperPunjabi).contains("ਕਰਜ਼ਾ ਪ੍ਰਸਤਾਵ/ਖਾਤਾ ਨੰਬਰ").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpperPunjabi).contains("ਕਰਜ਼ੇ ਦੀ ਕਿਸਮ").contains("DCEMI");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).contains("ਲੋਨ ਦੀ ਕਿਸਮ : ਡੈਬਿਟ ਕਾਰਡ ਆਨਲਾਈਨ");

    }

    public void emiDC_ICICI_PaymodeKFS(CashierPage cashierPage,PaymentDTO emidetails)
    {
        cashierPage.tabEMI().click();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(PaymentDTO.ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME);
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.EnterMobileNumber_For_EmiDcEligibility().clearAndType("8006006993");
        cashierPage.checkEMIEligibility().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(emidetails.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType("30");
        cashierPage.textBoxCVVNumber().clearAndType(emidetails.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.kfsLink().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.kfs_frame());
    }

    @Owner(VIDHI)
    @Feature("PAPR-6250")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for ICICI-Part 1")
    public void kfsNew_ICICI_EMIDC_part1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDC_ICICI_PaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpper).contains("Loan proposal/ account No.").contains(initTxnDTO.orderFromBody());
        Assertions.assertThat(kfsUpper).contains("Type of Loan").contains("EMI on Debit Card");
        Assertions.assertThat(kfsUpper).contains("Sanctioned Loan amount (in Rupees)").contains(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(kfsUpper).contains("Disbursal schedule\n" +
                "(i) Disbursement in stages or 100% upfront.\n" +
                "(ii) If it is stage wise, mention the clause of loan agreement having relevant details").contains("100% Upfront");
        Assertions.assertThat(kfsUpper).contains("Loan term (year/months/days)");
        Assertions.assertThat(kfsUpper).contains("Instalment details");
        Assertions.assertThat(kfsUpper).contains("Type of instalments");
        Assertions.assertThat(kfsUpper).contains("Number of EPIs");
        Assertions.assertThat(kfsUpper).contains("EPI (₹)");
        Assertions.assertThat(kfsUpper).contains("Commencement of repayment, post sanction");
        Assertions.assertThat(kfsUpper).contains("Interest rate (%) and type (fixed or floating or hybrid)");
        Assertions.assertThat(kfsUpper).contains("Additional Information in case of Floating rate of interest");
        Assertions.assertThat(kfsUpper).contains("Fee / Charges");
        Assertions.assertThat(kfsUpper).contains("Processing fees").contains("One Time");
        Assertions.assertThat(kfsUpper).contains("Insurance charges");
        Assertions.assertThat(kfsUpper).contains("Valuation fees");
        Assertions.assertThat(kfsUpper).contains("Any other (please specify)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage Rate (APR) (%)");
        Assertions.assertThat(kfsUpper).contains("Details of Contingent Charges (in ₹ or %, as applicable)");
        Assertions.assertThat(kfsUpper).contains("(i) Penal charges, if any, in case of delayed payment").contains("a) Interest charges\n\n" +
                "In case of a default, penal interest applicable is 1.33% per month (16% per annum).\n\n\n" +
                "b) Late payment charges\n\n" +
                "The LPC on account will be a function of the total outstanding balance and will be as follows:\n\n" +
                "Total outstanding balance LPC\n" +
                "Less than ₹10,000 ₹500 + Goods and Services Tax (GST)\n" +
                "More than ₹10,000 ₹750 + Goods and Services Tax (GST)" );
        Assertions.assertThat(kfsUpper).contains("(ii) Other penal charges, if any").contains("Auto-Debit return fee: ₹500 + Goods and Services Tax (GST)");
        Assertions.assertThat(kfsUpper).contains("(iii) Foreclosure charges, if applicable 3% of the outstanding Principal Amount + Goods and Services Tax (GST) applicable and Next Month EMI interest charges applicable in advance while foreclosure.");
        Assertions.assertThat(kfsUpper).contains("(iv) Charges for switching of loans from floating to fixed rate and vice versa");
        Assertions.assertThat(kfsUpper).contains("(v) Any other charges (please specify)").contains("Charges in case of delinquency:\n" +
                "i. Charges incurred in filing legal suit - At Actuals\n" +
                "ii. Charges incurred in sending different notices - At Actuals\n" +
                "iii. Pick-up charges as per instance - ₹500 + GST\n" +
                "iv. Cash transaction charge per instance for repayment of EMI dues at Branches - ₹100 + GST");
    }

    @Owner(VIDHI)
    @Feature("PAPR-6250")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for ICICI-Part 2")
    public void kfsNew_ICICI_EMIDC_part2(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDC_ICICI_PaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpper).contains("Clause of Loan agreement relating to engagement of recovery agents");
        Assertions.assertThat(kfsUpper).contains("Clause of Loan agreement which details grievance redressal mechanism");
        Assertions.assertThat(kfsUpper).contains("Phone number and email id of the nodal grievance redressal officer").contains("Email: digitallending@icicibank.com\n" +
                "Telephone No.: +91-22-39337979");
        Assertions.assertThat(kfsUpper).contains("Whether the loan is, or in future maybe, subject to transfer to other REs or securitisation (Yes/ No)");
        Assertions.assertThat(kfsUpper).contains("In case of lending under collaborative lending arrangements (e.g., co-lending/ outsourcing), following additional details may be furnished:");
        Assertions.assertThat(kfsUpper).contains("In case of digital loans, following specific disclosures may be furnished:");
        Assertions.assertThat(kfsUpper).contains("Name of the originating RE, along with its funding proportion").contains("Name of the partner RE along with its proportion of funding").contains("Blended rate of interest");
        Assertions.assertThat(kfsUpper).contains("Cooling off/look-up period, in terms of RE's board approved policy, during which borrower shall not be charged any penalty on prepayment of loan").contains("15 days");
        Assertions.assertThat(kfsUpper).contains("Details of LSP acting as recovery agent and authorized to approach the borrower.").contains("No recovery agent is assigned for loan account. In case any recovery agent is assigned, then particulars of such recovery agent will be communicated before the recovery agent contacts for recovery.");
        Assertions.assertThat(kfsUpper).contains("Name, designation, address and phone number of nodal grievance redressal officer designated specifically to deal with FinTech/ digital lending related complaints/ issues").contains("Email: digitallending@icicibank.com\n" +
                "Telephone No.: +91-22-39337979");
    }

    @Owner(VIDHI)
    @Feature("PAPR-6250")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for ICICI-Annex B")
    public void kfsNew_ICICI_EMIDC_annexB1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDC_ICICI_PaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNewAnnexB().getText();
        Assertions.assertThat(kfsUpper).contains("Sanctioned Loan amount (in Rupees) (Sl no. 2 of the KFS template - Part 1)").contains(initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(kfsUpper).contains("Loan Term (in years/ months/ days) (Sl No.4 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("No. of instalments for payment of principal, in case of non-equated periodic loans");
        Assertions.assertThat(kfsUpper).contains("Type of EPI\n" +
                "Amount of each EPI (in Rupees) and\n" +
                "nos. of EPIs (e.g., no. of EMIs in case of monthly instalments) (Sl No. 5 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("No. of instalments for payment of capitalised interest, if any");
        Assertions.assertThat(kfsUpper).contains("Commencement of repayments, post sanction (Sl No. 5 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("Interest rate type (fixed or floating or hybrid) (Sl No. 6 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("Rate of Interest (Sl No. 6 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("Total Interest Amount to be charged during the entire tenor of the loan as per the rate prevailing on sanction date (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Fee/ Charges payable8 (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Payable to the RE (Sl No.8A of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("Payable to third-party routed through RE (Sl No.8B of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("Net disbursed amount (1-6) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Total amount to be paid by the borrower9 (sum of 1 and 5) (in Rupees)");
        Assertions.assertThat(kfsUpper).contains("Annual Percentage rate - Effective annualized interest rate(in percentage)10 (Sl No.9 of the KFS template - Part 1)");
        Assertions.assertThat(kfsUpper).contains("Schedule of disbursement as per terms and conditions");
        Assertions.assertThat(kfsUpper).contains("Due date of payment of instalment and interest");
    }

    @Owner(VIDHI)
    @Feature("PAPR-6250")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for ICICI-Annex C")
    public void kfsNew_ICICI_EMIDC_annexC1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000.0")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDC_ICICI_PaymodeKFS(cashierPage,emidetails);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime now = LocalDateTime.now();
        Assertions.assertThat(cashierPage.kfsRepayment_Date().getText()).isEqualTo("Date:" + " " + dtf.format(now));
        Assertions.assertThat(cashierPage.kfsRepayment_refNo().getText()+" "+cashierPage.kfsRepayment_refNo_Id().getText()).isEqualTo("Reference Number: "+ initTxnDTO.orderFromBody());
        Assertions.assertThat(cashierPage.kfsRepayment_Customer().getText()).contains("Customer:");
        Assertions.assertThat(cashierPage.kfsRepayment_loanType().getText()).isEqualTo("Loan Type: DEBIT CARD ONLINE");
        Assertions.assertThat(cashierPage.kfsRepayment_tenure().getText()).contains("Tenure:");
        Assertions.assertThat(cashierPage.kfsRepayment_amt().getText()+" "+cashierPage.kfsRepayment_AmtFinanced().getText()).contains("Amount Financed: "+initTxnDTO.txnAmountFromBody());
        Assertions.assertThat(cashierPage.kfsRepayment_instal().getText()).contains("Total Installment");
        Assertions.assertThat(cashierPage.kfsRepayment_freq().getText()).isEqualTo("Frequency: Monthly");
        Assertions.assertThat(cashierPage.kfsRepayment_CurrINR().getText()).isEqualTo("Currency: INR");
    }

    @Owner(VIDHI)
    @Feature("PAPR-6250")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for ICICI-Table Text")
    public void kfsNew_ICICI_EMIDC_tableText1(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDC_ICICI_PaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.kfsTableTextNewtable().getText();
        Assertions.assertThat(kfsUpper).contains("No.");
        Assertions.assertThat(kfsUpper).contains("Statement Date");
        Assertions.assertThat(kfsUpper).contains("Principal Amount");
        Assertions.assertThat(kfsUpper).contains("Interest (Excluding GST*)");
        Assertions.assertThat(kfsUpper).contains("Balance");

    }
    @Owner(AJEESH)
    @Feature("PAPR-6229")
    @Parameters({"theme"})
    @Test(description = "Verfiy that for a merchant with pcf should be shown a tooltip in paymode Credit Card, and on clicking the same Overlay is shown with expected description.")
    public void verifytooltipforConvenienceFeesMerchantisShownforCC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String convText ="Convenience fees are fees applied by PG to end customers as per payment instrument to facilitate payment services to end users efficiently.";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_MERCHANT1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().waitUntilClickable();
        cashierPage.tabCreditCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getCreditCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pcfConvenienceInfoIconNew().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        Thread.sleep(10000);
        Assert.assertEquals(cashierPage.pcfConvenienceInfoTextNew1().getText(),convText);
    }
    @Owner(AJEESH)
    @Feature("PAPR-6229")
    @Parameters({"theme"})
    @Test(description = "Verfiy that for a merchant with pcf should be shown a tooltip in paymode Debit Card, and on clicking the same Overlay is shown with expected description.")
    public void verifytooltipforConvenienceFeesMerchantisShownforDC(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String convText ="Convenience fees are fees applied by PG to end customers as per payment instrument to facilitate payment services to end users efficiently.";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_MERCHANT1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.scrollToElement(cashierPage.tabDebitCard());
        cashierPage.tabDebitCard().waitUntilClickable();
        cashierPage.tabDebitCard().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(new PaymentDTO().getDebitCardNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pcfConvenienceInfoIconNew().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        Thread.sleep(10000);
        Assert.assertEquals(cashierPage.pcfConvenienceInfoTextNew1().getText(),convText);
    }
    @Owner(AJEESH)
    @Feature("PAPR-6229")
    @Parameters({"theme"})
    @Test(description = "Verfiy that for a merchant with pcf should be shown a tooltip in paymode netBanking, and on clicking the same Overlay is shown with expected description.")
    public void verifytooltipforConvenienceFeesMerchantisShownforNB(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        String convText ="Convenience fees are fees applied by PG to end customers as per payment instrument to facilitate payment services to end users efficiently.";
        Constants.MerchantType merchant = Constants.MerchantType.PCF_MERCHANT1;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("20")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.scrollToElement(cashierPage.tabNetBanking());
        cashierPage.tabNetBanking().waitUntilClickable();
        cashierPage.tabNetBanking().click();
        cashierPage.pcfConvenienceInfoIconNew().click();
        cashierPage.pcfConvenienceInfoHeaderNew().isDisplayed();
        Thread.sleep(10000);
        Assert.assertEquals(cashierPage.pcfConvenienceInfoTextNew1().getText(),convText);

    }

    @Owner(VIDHI)
    @Feature("PAPR-6250")
    @Parameters({"theme"})
    @Test(description = "Verify that KFS table text for ICICI-UserConsent")
    public void kfsNew_ICICI_EMIDC_consent(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDC_ICICI_PaymodeKFS(cashierPage,emidetails);
        String kfsUpper = cashierPage.KFSloanConsent().getText();
        Assertions.assertThat(kfsUpper).contains("I Read And Agreed");
        Assertions.assertThat(kfsUpper).contains("I/We expressly authorize and give consent to ICICI Bank to, disclose,transfer or part with any of my/our information, (including location), or an other device information when ICICI Bank considers such disclosure as necessary,with:");
        Assertions.assertThat(kfsUpper).contains("Agents of ICICI Bank, ICICI Bank's group entities in any jurisdiction;");
        Assertions.assertThat(kfsUpper).contains("Auditors, credit rating agencies/credit bureaus, statutory/regulatory authorities,governmental/administrative authorities,Central Know Your Customer(CKYC) registry or SEBI Know Your Client registration agency,having jurisdiction over ICICI Bank or its group entities;");
        Assertions.assertThat(kfsUpper).contains("Service providers,or such person with whom ICICI Bank contracts or proposes to contract;\n" +
                "(Collectively referred to as \"Permitted Persons\")");
        Assertions.assertThat(kfsUpper).contains("For the purposes of:\n" +
                "Provision of the facility and completion of non-onboarding formalities; or\n" +
                "Complying with KYC requirements; or\n" +
                "Compliance with applicable laws or any order (judicial or otherwise),statutory/regulatory requirement or;\n" +
                "for credit review of facilities availed; or\n" +
                "Authentication or verification; or\n" +
                "research or analysis, credit reporting and scoring,risk management,participation in any telecommunication; or\n" +
                "electronic clearing network and for use or processing of the said information/data\n" +
                "Disclosing any default in payment, for the purposes of recovering such amounts.") ;
        Assertions.assertThat(kfsUpper).contains("For detailed Privacy Policy of the ICICI bank, please visit https://www.icicibank.com/privacy.page");
    }


    @Owner(VIDHI)
    @Feature("PAPR-6250/PGP-57587")
    @Parameters({"theme"})
    @Test(description = "Verify that HDFC DC EMI Online KFS for vernacular Language Translation for static fields")
    public void kfsNew_HDFC_EMIDC_vernacularText(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PaymentDTO emidetails = new PaymentDTO();
        emiDCPaymodeKFS(cashierPage,emidetails);
        cashierPage.pause(2);

//*********** HINDI ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Hindi");
        cashierPage.pause(2);
        String kfsUpper = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpper).contains("देरी से भुगतान के मामले में दंडात्मक शुल्क, यदि कोई हो").contains("450/- रुपये के अलावा सभी लागू सरकार ने कर लगाए हैं");
        Assertions.assertThat(kfsUpper).contains("फोरक्लोज़र शुल्क, अगर लागू हो").contains("3% से अधिक लागू सरकार ने लगाए गए कर");
        Assertions.assertThat(kfsUpper).contains("कोई अन्य शुल्क (कृपया निर्दिष्ट करें)").contains("300/- का देर से ईएमआई भुगतान शुल्क और सभी लागू सरकार द्वारा लगाए गए टैक्स");

        String kfsUpper1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpper1).contains("नोडल शिकायत निवारण अधिकारी का फोन नंबर और ईमेल आईडी").contains("टोल फ्री नंबर: 1800 266 4060\n" +
                "ईमेल: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpper1).contains("आरई के बोर्ड द्वारा अनुमोदित नीति के संदर्भ में कूलिंग ऑफ/लुक-अप अवधि, जिसके दौरान उधारकर्ता से ऋण के पूर्व भुगतान पर कोई जुर्माना नहीं लगाया जाएगा").contains("3 दिन");
        Assertions.assertThat(kfsUpper1).contains("फिनटेक/डिजिटल ऋण संबंधी शिकायतों/मुद्दों से निपटने के लिए विशेष रूप से नामित नोडल शिकायत निवारण अधिकारी का नाम, पदनाम, पता और फोन नंबर").contains("श्री कन्नन रामाशेषन\n" +
                        "एचडीएफसी बैंक लिमिटेड शिकायत\n" +
                        "निवारण प्रकोष्ठ, प्रथम तल,\n" +
                        "एम्पायर प्लाजा - 1, लाल बहादुर\n" +
                        "शास्त्री मार्ग, चंदन नगर,\n" +
                        "विक्रोली पश्चिम, मुंबई-\n" +
                        "400083 महाराष्ट्र\n" +
                        "सोमवार से शनिवार: सुबह 9:30 बजे से शाम 5:30 बजे तक\n" +
                        "यह सुविधा दूसरे और चौथे शनिवार, सभी रविवार और बैंक की छुट्टियों पर उपलब्ध नहीं है");

//*********** ASSAMESE ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Assamese");
        cashierPage.pause(2);
        String kfsUpperAssamese = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperAssamese).contains("বিলম্বিত পৰিশোধৰ ক্ষেত্ৰত শাস্তিমূলক মাচুল, যদি থাকে").contains("450/- আৰু সকলো প্ৰযোজ্য চৰকাৰে কৰ আৰোপ কৰিছে");
        Assertions.assertThat(kfsUpperAssamese).contains("ফৰক্লোজাৰ মাচুল, যদি প্ৰযোজ্য হয়").contains("3% আৰু প্ৰযোজ্য চৰকাৰে কৰ আৰোপ কৰিছে");
        Assertions.assertThat(kfsUpperAssamese).contains("অন্য কোনো মাচুল (অনুগ্ৰহ কৰি নিৰ্দিষ্ট কৰক)").contains("300/- ৰ বিলম্বিত ইএমআই পৰিশোধ মাচুল আৰু সকলো প্ৰযোজ্য চৰকাৰে কৰ আৰোপ কৰিছে");

        String kfsUpperAssamese1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperAssamese1).contains("নোডালগ্ৰাপ্টিছ নিষ্পত্তি বিষয়াৰ ফোন নম্বৰ আৰু ইমেইল আইডি7").contains("টোল ফ্ৰী নম্বৰ: 1800 266 4060\n" +
                "ইমেইল: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperAssamese1).contains("আৰ.ই.-ৰ বোৰ্ড অনুমোদিত আঁচনিৰ ক্ষেত্ৰত কুলিং অফ/লুক-আপ ম্যাদ, যাৰ সময়ত ঋণ লওঁতাক ঋণৰ পূৰ্বপৰিশোধৰ ওপৰত কোনো জৰিমনা আদায় কৰা নহ'ব").contains("3 দিন");

//*********** GUJRATI ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Gujarati");
        cashierPage.pause(2);
        String kfsUpperGujrati = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperGujrati).contains("વિલંબિત ચુકવણીના કિસ્સામાં દંડાત્મક ચાર્જ, જો કોઈ હોય તો,").contains("450/- વત્તા તમામ લાગુ પડતા સરકાર દ્વારા લાદવામાં આવેલા કરવેરા");
        Assertions.assertThat(kfsUpperGujrati).contains("ફોરક્લોઝર ચાર્જિસ, જો લાગુ પડતું હોય તો").contains("3% વત્તા લાગુ સરકાર દ્વારા લાદવામાં આવતા કરવેરા");
        Assertions.assertThat(kfsUpperGujrati).contains("કોઈપણ અન્ય ચાર્જિસ (કૃપા કરીને સ્પષ્ટ કરો)").contains("લેટ ઈએમઆઈ ચુકવણી ફી 300/- ઉપરાંત તમામ લાગુ પડતા સરકાર દ્વારા વસૂલવામાં આવતા કરવેરા");

        String kfsUpperGujrati1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperGujrati1).contains("નોડલગ્રીવન્સ નિવારણ અધિકારીનો ફોન નંબર અને ઇમેઇલ આઇડી").contains("ટોલ ફ્રી નંબર: 1800 266 4060\n" +
                "ઈ-મેઈલ: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperGujrati1).contains("આરઇની બોર્ડ દ્વારા માન્ય નીતિના સંદર્ભમાં કૂલિંગ ઓફ/લુક-અપ પિરિયડ, જે દરમિયાન લોન લેનાર પાસેથી લોનની પૂર્વચુકવણી પર કોઈ દંડ વસૂલવામાં આવશે નહીં").contains("3 દિવસો");

 //*********** MALYALAM ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Malayalam");
        cashierPage.pause(2);
        String kfsUpperMalyalam = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperMalyalam).contains("പണമടയ്ക്കുന്നതിൽ കാലതാമസമുണ്ടായാൽ, എന്തെങ്കിലും ശിക്ഷാനടപടികൾ ഉണ്ടെങ്കിൽ").contains("450/- കൂടാതെ ബാധകമായ എല്ലാ സർക്കാർ നികുതികളും ഈടാക്കുന്നു");
        Assertions.assertThat(kfsUpperMalyalam).contains("ബാധകമെങ്കിൽ ജപ്തി നിരക്കുകൾ").contains("3% കൂടാതെ ബാധകമായ സർക്കാർ നികുതികൾ ഈടാക്കുന്നു");
        Assertions.assertThat(kfsUpperMalyalam).contains("മറ്റെന്തെങ്കിലും ചാർജുകൾ (ദയവായി വ്യക്തമാക്കുക)").contains("വൈകിയുള്ള ഇഎംഐ പേയ്മെന്റ് ഫീസ് 300 രൂപയും ബാധകമായ എല്ലാ സർക്കാർ നികുതികളും ഈടാക്കുന്നു");

        String kfsUpperMalyalam1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperMalyalam1).contains("നോഡൽ ഗ്രിവേഷൻ ഓഫീസറുടെ ഫോൺ നമ്പറും ഇമെയിൽ ഐഡിയും").contains("ടോൾ ഫ്രീ നമ്പർ: 1800 266 4060\n" +
                "ഇ-മെയില്: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperMalyalam1).contains("RE-യുടെ ബോർഡ് അംഗീകൃത പോളിസിയുടെ അടിസ്ഥാനത്തിൽ, കൂളിംഗ് ഓഫ് / ലുക്ക്-അപ്പ് കാലയളവ്, ഈ കാലയളവിൽ വായ്പക്കാരനിൽ നിന്ന് വായ്പ മുൻകൂട്ടി അടയ്ക്കുന്നതിന് പിഴ ഈടാക്കില്ല").contains("3 ദിവസം");

//*********** MARATHI ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Marathi");
        cashierPage.pause(2);
        String kfsUpperMarathi = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperMarathi).contains("विलंबाने पैसे भरल्यास दंडात्मक शुल्कt").contains("450/- आणि सर्व लागू होणारे शासन आकारलेले कर");
        Assertions.assertThat(kfsUpperMarathi).contains("लागू असल्यास फोरक्लोजर शुल्क").contains("3% पेक्षा जास्त लागू सरकार आकारला जाणारा कर");
        Assertions.assertThat(kfsUpperMarathi).contains("इतर कोणतेही शुल्क (कृपया निर्दिष्ट करा)").contains("उशीरा ईएमआय भरणा शुल्क 300/- आणि सर्व लागू सरकारी कर");

        String kfsUpperMarathi1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperMarathi1).contains("नोडलतक्रार निवारण अधिकाऱ्याचा फोन नंबर व ईमेल आयडी").contains("टोल फ्री नंबर: 1800 266 4060\n" +
                "ईमेल: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperMarathi1).contains("आरईच्या संचालक मंडळाने मंजूर केलेल्या धोरणानुसार कूलिंग ऑफ / लुक-अप कालावधी, ज्यादरम्यान कर्जदाराकडून कर्जाची पूर्वपरतफेड केल्यावर कोणताही दंड आकारला जाणार नाही").contains("3 दिवस");

//*********** ORIYA ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Oriya");
        cashierPage.pause(2);
        String kfsUpperOriya = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperOriya).contains("ବିଳମ୍ବିତ ଦେୟ କ୍ଷେତ୍ରରେ ଯଦି କୌଣସି ଦଣ୍ଡନୀୟ ଅଭିଯୋଗ ରହିଛି").contains("450/- ଏହା ବ୍ୟତୀତ ସମସ୍ତ ପ୍ରଯୁଜ୍ୟ ସରକାରୀ ଟିକସ ଆଦାୟ କରାଯାଏ");
        Assertions.assertThat(kfsUpperOriya).contains("Foreclosure charges, ଯଦି ପ୍ରଯୁଜ୍ୟ").contains("3% ରୁ ଅଧିକ ପ୍ରଯୁଜ୍ୟ ସରକାର ଟିକସ ଆଦାୟ କଲେ");
        Assertions.assertThat(kfsUpperOriya).contains("ଅନ୍ୟ କୌଣସି ଚାର୍ଜ (ଦୟାକରି ନିର୍ଦ୍ଦିଷ୍ଟ କରନ୍ତୁ)").contains("ବିଳମ୍ବିତ ଇଏମଆଇ ଦେୟ ଶୁଳ୍କ 300/- ଏବଂ ସମସ୍ତ ପ୍ରଯୁଜ୍ୟ ସରକାରୀ ଟିକସ ଆଦାୟ କରାଯାଏ");

        String kfsUpperOriya1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperOriya1).contains("ନୋଡାଲ ଚିକିତ୍ସା ଅଧିକାରୀଙ୍କ ଫୋନ୍ ନମ୍ବର ଓ ଇମେଲ୍ ଆଇଡି").contains("ଟୋଲ୍ ଫ୍ରି ନମ୍ବର: 1800 266 4060\n" +
                "ଇମେଲ୍: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperOriya1).contains("ଆରଇର ବୋର୍ଡ ଅନୁମୋଦିତ ନୀତି ଅନୁଯାୟୀ କୁଲିଂ ଅଫ୍ /ଲୁକ୍-ଅପ୍ ଅବଧି, ଯେଉଁ ସମୟରେ ଋଣଧାରୀଙ୍କୁ ଋଣ ର ପ୍ରାକ୍ ଦେୟ ଉପରେ କୌଣସି ଜରିମାନା ଆଦାୟ କରାଯିବ ନାହିଁ").contains("3 ଦିନ");

//*********** PUNJABI ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Punjabi");
        cashierPage.pause(2);
        String kfsUpperPunjabi = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperPunjabi).contains("ਦੇਰੀ ਨਾਲ ਭੁਗਤਾਨ ਕਰਨ ਦੇ ਮਾਮਲੇ ਵਿੱਚ ਦੰਡਾਵਲੀ ਦੇ ਦੋਸ਼, ਜੇ ਕੋਈ ਹਨ").contains("450/- ਅਤੇ ਸਾਰੇ ਲਾਗੂ ਸਰਕਾਰੀ ਟੈਕਸ");
        Assertions.assertThat(kfsUpperPunjabi).contains("ਫੋਰਕਲੋਜ਼ਰ ਚਾਰਜ, ਜੇ ਲਾਗੂ ਹੁੰਦੇ ਹਨ").contains("3% ਤੋਂ ਵੱਧ ਲਾਗੂ ਸਰਕਾਰ ਨੇ ਟੈਕਸ ਲਗਾਏ");
        Assertions.assertThat(kfsUpperPunjabi).contains("ਕੋਈ ਹੋਰ ਖਰਚੇ (ਕਿਰਪਾ ਕਰਕੇ ਦੱਸੋ)").contains("300/- ਦੀ ਲੇਟ ਈਐਮਆਈ ਭੁਗਤਾਨ ਫੀਸ ਅਤੇ ਸਾਰੇ ਲਾਗੂ ਸਰਕਾਰੀ ਟੈਕਸ");

        String kfsUpperPunjabi1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperPunjabi1).contains("ਨੋਡਲ ਸ਼ਿਕਾਇਤ ਨਿਵਾਰਨ ਅਧਿਕਾਰੀ ਦਾ ਫ਼ੋਨ ਨੰਬਰ ਅਤੇ ਈਮੇਲ ਆਈ.ਡੀ.").contains("ਟੋਲ ਫ੍ਰੀ ਨੰਬਰ: 1800 266 4060\n" +
                "ਈਮੇਲ: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperPunjabi1).contains("ਆਰ.ਈ. ਦੇ ਬੋਰਡ ਦੁਆਰਾ ਪ੍ਰਵਾਨਿਤ ਨੀਤੀ ਦੇ ਅਨੁਸਾਰ, ਕੂਲਿੰਗ ਆਫ/ਲੁੱਕ-ਅੱਪ ਪੀਰੀਅਡ, ਜਿਸ ਦੌਰਾਨ ਕਰਜ਼ਦਾਰ ਤੋਂ ਕਰਜ਼ੇ ਦੀ ਪੂਰਵ ਅਦਾਇਗੀ 'ਤੇ ਕੋਈ ਜੁਰਮਾਨਾ ਨਹੀਂ ਲਿਆ ਜਾਵੇਗਾ").contains("3 ਦਿਨ");

//*********** TAMIL ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Tamil");
        cashierPage.pause(2);
        String kfsUpperTamil = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperTamil).contains("தாமதமாக பணம் செலுத்தினால் அபராத கட்டணங்கள் ஏதேனும் இருந்தால்").contains("450/- மற்றும் பொருந்தக்கூடிய அனைத்து அரசாங்க வரிகளும்");
        Assertions.assertThat(kfsUpperTamil).contains("முன்கூட்டியே அடைத்தல்(ஃபோர்குளோசர்) கட்டணங்கள், பொருந்தினால்").contains("3% மற்றும் பொருந்தக்கூடிய அரசாங்கம் விதித்த வரிகள்");
        Assertions.assertThat(kfsUpperTamil).contains("வேறு ஏதேனும் கட்டணங்கள் (விபரங்களை குறிப்பிடவும்)").contains("தாமதமாக EMI செலுத்தும் கட்டணம் 300/- மற்றும் பொருந்தக்கூடிய அனைத்து அரசாங்க வரிகளும்");

        String kfsUpperTamil1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperTamil1).contains("குறை தீர்க்கும் அதிகாரியின் தொலைபேசி எண் மற்றும் மின்னஞ்சல் ஐடி").contains("கட்டணமில்லா எண்: 1800 266 4060\n" +
                "மின்னஞ்சல்: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperTamil1).contains("").contains("3 நாட்கள்");

//*********** BENGALI ************
        cashierPage.dropdownLanguageSupport().selectByVisibleText("Bengali");
        cashierPage.pause(2);
        String kfsUpperBengali = cashierPage.kfsTableTextNew().getText();
        Assertions.assertThat(kfsUpperBengali).contains("বিলম্বিত অর্থ প্রদানের ক্ষেত্রে দণ্ডের চার্জ, যদি থাকে").contains("450/- প্লাস সমস্ত প্রযোজ্য সরকারী আরোপিত কর");
        Assertions.assertThat(kfsUpperBengali).contains("ফোরক্লোজার চার্জ, যদি প্রযোজ্য হয়").contains("3% প্লাস প্রযোজ্য সরকার আরোপিত কর");
        Assertions.assertThat(kfsUpperBengali).contains("অন্য কোনও চার্জ (দয়া করে নির্দিষ্ট করুন)").contains("বিলম্ব ইএমআই পেমেন্ট ফি 300/- এর সাথে সমস্ত প্রযোজ্য সরকারী আরোপিত কর");

        String kfsUpperBengali1 = cashierPage.kfsTableTextNewPart2().getText();
        Assertions.assertThat(kfsUpperBengali1).contains("নোডাল রিড্রেসাল অফিসারের ফোন নম্বর ও ইমেল আইডি").contains("টোল ফ্রি নম্বর: 1800 266 4060\n" +
                "ইমেইল: grievance.redressaldl@hdfcbank.com");
        Assertions.assertThat(kfsUpperBengali1).contains("ইমেইল").contains("3 দিন");

    }

    @Owner(RONIKA)
    @Feature("PPSL-799")
    @Parameters({"theme"})
    @Test(description = "Verify that unnecessry FPO hits are not going to Affordability Platform Discovery/promo when theia.enable.migration.to.unifiedOffers: ON + none of the above parameters")
    public void noUneccesaryHits_In_Discovery(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, EMI_DISCOVERY).setTxnValue("1000")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken)
                .setVersion("v5")
                .setMid(initTxnDTO.getBody().getMid())
                .setFetchAllPaymentOffers("true")
                .setApplyPaymentOffers("true")
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"PAYMENT_PROMO_SERVICE", "REQUEST");
        Assertions.assertThat(logs).doesNotContain("/v2/promosearch/payment/offers");
        String Logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.payment_option_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM", "REQUEST");
        Assertions.assertThat(Logs).doesNotContain("/ads/v2/offer/discovery");

    }

    @Owner(PUSPA)
    @Feature("PGP-59285")
    @Parameters({"theme"})
    @Test(description = "Unmasked Access Token is Printing in Logs || Theia")
    public void verifymaksedAccessToken(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String refId = String.valueOf(CommonHelpers.getRandomWithSize(10)) + "10";
        com.paytm.CreateToken createToken = new CreateToken(merchantType, user.ssoToken(), refId);
        JsonPath jsonpath = createToken.execute().jsonPath();
        String accesstoken = jsonpath.getString("body.accessToken");


        int maskLength,prefixLength = 4,suffixLength=4;
        maskLength = accesstoken.length() - prefixLength - suffixLength;
        StringBuilder masked = new StringBuilder();
        masked.append(accesstoken.substring(0, prefixLength));
        masked.append("*".repeat(maskLength));
        masked.append(accesstoken.substring(accesstoken.length() - suffixLength));

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia,"AccessToken");
        Assertions.assertThat(logs).contains(masked.toString());
    }

    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Verify merchant logo in QR when preference BRANDED_UPI_QR is enabled on MID")
    public void verifyLogoInQRWhenPrefOn(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());
        

        String qrLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.payment_option,
                initTxnDTO.orderFromBody(),
                "QR");
        
        
        Assertions.assertThat(qrLogs).contains("staticpg.paytmpayments.com/checkoutjs/");
        Assertions.assertThat(qrLogs).contains("paytm-logo-origin.png");
    }

    @Owner(PUSPA)
    @Parameters({"theme"})
    @Test(description = "Verify branded UPI QR logo is not loaded when BRANDED_UPI_QR preference is disabled on MID qa12ps35706555678890")
    public void verifyLogoInQRWhenPrefOff(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.CONVENIENCE_AND_PLATFORM_FEE_ON_UPI_SUBTYPE;
        User user = userManager.getForWrite(Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue("1100")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        PGPHelpers.isCheckoutJsV5FPOCalled(initTxnDTO.orderFromBody());

        String qrLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.payment_option,
                initTxnDTO.orderFromBody(),
                "QR");
        Assertions.assertThat(qrLogs).doesNotContain("Fetching branded UPI QR logo from URL");
        Assertions.assertThat(qrLogs).doesNotContain("Branded UPI QR logo loaded from");
        Assertions.assertThat(qrLogs).doesNotContain("staticpg.paytmpayments.com/checkoutjs/");
        Assertions.assertThat(qrLogs).doesNotContain("paytm-logo-origin.png");
    }
    @Owner(NITISH_DHAWAN)
    @Feature("PPGP-441")
    @Parameters({"checkoutjs_web_revamp_2"})
    @Test(description = "Verify that popup + sparkles are shown when flag (ui.js.revamp.offers) is enabled and offers are available")
    public void verifyPopupAndSparkles(@Optional("checkoutjs_web_revamp_2") String theme) throws Exception {
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new SimplifiedUnifiedOffers.PromoDetails(
                null, "true", "false", "true", "");
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new SimplifiedUnifiedOffers.SubventionDetails(
                "true", "10", "", "");
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setRequestType("PAYMENT")
                .setTxnValue("100")
                .setCustId("1000036031")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.viewAllOffers().assertVisible();
        cashierPage.viewAllOffers().click();
        cashierPage.offerButton().waitUntilVisible();
        cashierPage.offerButton().click();
        cashierPage.continueButton().waitUntilVisible();
        cashierPage.continueButton().click();
        cashierPage.viewOKButton().waitUntilVisible();
        cashierPage.viewOKButton().click();
    }



}