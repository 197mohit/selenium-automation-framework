package scripts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.LocalConfig;
import com.paytm.api.MappingService.MerchantAddPreferenceInfoExt;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.filters.RequestResponseLoggingFilter;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.By;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;

@Owner("Tarun")
@Owners(author = "Tarun", qa = "Ankur")
@Epic(Constants.Sprint.SPRINT_THEMATIC)
@Feature("PGPUI-25")
public class ThematicTests extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final RequestSpecification reqSpec = new RequestSpecBuilder()
            .addFilter(new RequestResponseLoggingFilter())
            .setConfig(new CurlLoggingRestAssuredConfigBuilder().build())
            .build();

    @Test(description = "Verify color of header, headerTxt, body, bodyText, payButton, paytButtonTxt on cashier page " +
            "when Dark theme is applied on enhancedWeb")
    public void t1(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        Constants.MerchantType m = Constants.MerchantType.THEMATIC;
        User user = userManager.getForRead(Label.LOGIN);
        Map<String, String> td = getColorScheme(Constants.UITheme.DARK_THEME.get());
        Assertions.assertThat(PGPHelpers.validate_MerchantPreference(m.getId(), "THEMATIC_FEATURE_USED", "y"))
                .as("THEMATIC_FEATURE_USED is not enabled")
                .isTrue();

        MerchantAddPreferenceInfoReq req = new MerchantAddPreferenceInfoReq
                .Builder(m.getId(), "THEME_PREFERENCE_DETAILS", "Y", Constants.UITheme.DARK_THEME.get())
                .build();

        new MerchantAddPreferenceInfoExt(req, m.getId()).execute()
                .then()
                .statusCode(200)
                .body("restStatus", equalToIgnoringCase("SUCCESS"))
                .body("response.resultCode", equalTo("00000"),
                        "response.resultStatus", equalTo("S"),
                        "response.messaage", equalTo("Success"));

        OrderDTO orderDTO = new OrderFactory.PGOnly(m, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String headerColor = getColorCode(getElementByXpath("//main/header", theme).getCssValue("background-color"));
        String headerTxtColor = getColorCode(getElementByXpath("//main/header//div/span[contains(text(), 'Automation')]", theme).getCssValue("color"));

        cashierPage.tabCreditCard().click();
        String payButtonBGColor = getColorCode(cashierPage.buttonPGPayNow().getCssValue("background-color"));
        String payButtonTextColor = getColorCode(cashierPage.buttonPGPayNow().getCssValue("color"));
        String bodyBGColor = getColorCode(cashierPage.loginSection().getCssValue("background-color"));
        String bodyTextColor = getColorCode(cashierPage.tabDebitCard().getCssValue("color"));
        String qrSectionBGColor = getColorCode(getElementByXpath("//div[@data-key='qr-section']", theme).getCssValue("background-color"));
        String qrSectionColor = getColorCode(getElementByXpath("//div[@data-key='qr-section']", theme).getCssValue("color"));

        cashierPage.login(user.mobNo());
        String walletTextColor = getColorCode(cashierPage.walletBalanceCheck().getCssValue("color"));

        SoftAssertions softy = new SoftAssertions();
        softy.assertThat(headerColor)
                .as("HEADERPANEL color mismatch")
                .isEqualToIgnoringCase(td.get("HEADERPANEL"));
        softy.assertThat(headerTxtColor)
                .as("HEADERTEXT color mismatch")
                .isEqualToIgnoringCase(td.get("HEADERTEXT"));
        softy.assertThat(payButtonBGColor)
                .as("PAYBUTTONBG color mismatch")
                .isEqualToIgnoringCase(td.get("PAYBUTTONBG"));
        softy.assertThat(payButtonTextColor)
                .as("PAYBUTTONTEXT mismatch")
                .isEqualToIgnoringCase(td.get("PAYBUTTONTEXT"));
        softy.assertThat(bodyBGColor)
                .as("BODYBG color mismatch")
                .isEqualToIgnoringCase(td.get("BODYBG"));
        softy.assertThat(bodyTextColor)
                .as("BODYTEXT color mistmatch")
                .isEqualToIgnoringCase(td.get("BODYTEXT"));
        softy.assertThat(qrSectionBGColor)
                .as("QR section BODYBG color mismatch")
                .isEqualToIgnoringCase(td.get("BODYBG"));
        softy.assertThat(qrSectionColor)
                .as("QR section BODYTEXT color mismatch")
                .isEqualToIgnoringCase(td.get("BODYTEXT"));
        softy.assertThat(walletTextColor)
                .as("wallet BODYTEXT color mismatch")
                .isEqualToIgnoringCase(td.get("BODYTEXT"));
        softy.assertAll();
    }

    @Test(description = "Verify color of header, headerTxt, body, bodyText, payButton, paytButtonTxt on cashier page " +
            "when Dark theme is applied on enhancedWap")
    public void t2(@Optional("enhancedwap_revamp") String theme) throws Exception {
//        String theme = "enhancedwap";
        Constants.MerchantType m = Constants.MerchantType.THEMATIC;
        User user = userManager.getForRead(Label.LOGIN);
        Map<String, String> td = getColorScheme(Constants.UITheme.DARK_THEME.get());
        Assertions.assertThat(PGPHelpers.validate_MerchantPreference(m.getId(), "THEMATIC_FEATURE_USED", "y"))
                .as("THEMATIC_FEATURE_USED is not enabled")
                .isTrue();

        MerchantAddPreferenceInfoReq req = new MerchantAddPreferenceInfoReq
                .Builder(m.getId(), "THEME_PREFERENCE_DETAILS", "Y", Constants.UITheme.DARK_THEME.get())
                .build();

        new MerchantAddPreferenceInfoExt(req, m.getId()).execute()
                .then()
                .statusCode(200)
                .body("restStatus", equalToIgnoringCase("SUCCESS"))
                .body("response.resultCode", equalTo("00000"),
                        "response.resultStatus", equalTo("S"),
                        "response.messaage", equalTo("Success"));

        OrderDTO orderDTO = new OrderFactory.PGOnly(m, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        String headerColor = getColorCode(getElementByXpath("//main//header/section", theme).getCssValue("background-color"));
        String headerTxtColor = getColorCode(getElementByXpath("//main//header//div[contains(text(), 'Automation')]", theme).getCssValue("color"));

        cashierPage.tabCreditCard().click();
        String payButtonBGColor = getColorCode(cashierPage.buttonPGPayNow().getCssValue("background-color"));
        String payButtonTextColor = getColorCode(cashierPage.buttonPGPayNow().getCssValue("color"));
        String bodyBGColor = getColorCode(getElementByXpath("//*[text()='Select an option to pay']", theme).getCssValue("background-color"));
        String bodyTextColor = getColorCode(cashierPage.tabDebitCard().getCssValue("color"));
        cashierPage.login(user.mobNo());
        String walletTextColor = getColorCode(getElementByXpath("//section[contains(@class, 'paytm-wallet')]/div/span", theme).getCssValue("color"));

        SoftAssertions softy = new SoftAssertions();
        softy.assertThat(headerColor)
                .as("HEADERPANEL color mismatch")
                .isEqualToIgnoringCase(td.get("HEADERPANEL"));
        softy.assertThat(headerTxtColor)
                .as("HEADERTEXT color mismatch")
                .isEqualToIgnoringCase(td.get("HEADERTEXT"));
        softy.assertThat(payButtonBGColor)
                .as("PAYBUTTONBG color mismatch")
                .isEqualToIgnoringCase(td.get("PAYBUTTONBG"));
        softy.assertThat(payButtonTextColor)
                .as("PAYBUTTONTEXT mismatch")
                .isEqualToIgnoringCase(td.get("PAYBUTTONTEXT"));
        softy.assertThat(bodyBGColor)
                .as("BODYBG color mismatch")
                .isEqualToIgnoringCase(td.get("BODYBG"));
        softy.assertThat(bodyTextColor)
                .as("BODYTEXT color mistmatch")
                .isEqualToIgnoringCase(td.get("BODYTEXT"));
        softy.assertThat(walletTextColor)
                .as("wallet BODYTEXT color mismatch")
                .isEqualToIgnoringCase(td.get("BODYTEXT"));
        softy.assertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify successful txn using CC when when Dark theme is applied")
    public void t3(@Optional("enhancedweb") String theme) throws Exception {
        Constants.MerchantType m = Constants.MerchantType.THEMATIC;
        User user = userManager.getForRead(Label.BASIC);
        Assertions.assertThat(PGPHelpers.validate_MerchantPreference(m.getId(), "THEMATIC_FEATURE_USED", "y"))
                .as("THEMATIC_FEATURE_USED is not enabled")
                .isTrue();

        MerchantAddPreferenceInfoReq req = new MerchantAddPreferenceInfoReq
                .Builder(m.getId(), "THEME_PREFERENCE_DETAILS", "Y", Constants.UITheme.DARK_THEME.get())
                .build();

        new MerchantAddPreferenceInfoExt(req, m.getId()).execute()
                .then()
                .statusCode(200)
                .body("restStatus", equalToIgnoringCase("SUCCESS"))
                .body("response.resultCode", equalTo("00000"),
                        "response.resultStatus", equalTo("S"),
                        "response.messaage", equalTo("Success"));

        OrderDTO orderDTO = new OrderFactory.PGOnly(m, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
    }

    @Test(description = "Verify successful txn using CC when when Dark theme is applied")
    public void t4(@Optional("enhancedweb_revamp") String theme) throws Exception {
//        String theme = "enhancedweb";
        Constants.MerchantType m = Constants.MerchantType.Subscription_PGOnly;
        try {
            MerchantAddPreferenceInfoReq req = new MerchantAddPreferenceInfoReq
                    .Builder(m.getId(), "THEMATIC_FEATURE_USED", "ACTIVE", "Y")
                    .build();

            RestAssured.given().contentType(ContentType.JSON)
                    .spec(reqSpec)
                    .baseUri(LocalConfig.PGP_HOST)
                    .basePath(Constants.MappingService.ADD_MERCHANT_PREFRENCE_INFO)
                    .body(req)
                    .post()
                    .then()
                    .statusCode(200)
                    .body("restStatus", equalToIgnoringCase("SUCCESS"))
                    .body("response.resultCode", equalTo("00000"),
                            "response.resultStatus", equalTo("S"),
                            "response.messaage", equalTo("Success"));

            Map<String, String> td = getColorScheme(Constants.UITheme.DARK_THEME.get());
            req = new MerchantAddPreferenceInfoReq
                    .Builder(m.getId(), "THEME_PREFERENCE_DETAILS", "Y", Constants.UITheme.DARK_THEME.get())
                    .build();

            new MerchantAddPreferenceInfoExt(req, m.getId()).execute()
                    .then()
                    .statusCode(200)
                    .body("restStatus", equalToIgnoringCase("SUCCESS"))
                    .body("response.resultCode", equalTo("00000"),
                            "response.resultStatus", equalTo("S"),
                            "response.messaage", equalTo("Success"));

            OrderDTO orderDTO = new OrderFactory.SubscriptionCC_DC(Constants.MerchantType.Subscription_PGOnly, theme)
                    .setSUBS_PAYMENT_MODE("")
                    .build();
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.waitUntilLoads();
            String headerColor = getColorCode(getElementByXpath("//*[@class='top-headerbox ']", theme).getCssValue("background-color"));
            String headerTxtColor = getColorCode(getElementByXpath("//*[text()='AutomationMerchant0011']", theme).getCssValue("color"));

   //         cashierPage.tabCreditCard().click();
            String payButtonBGColor = getColorCode(getElementByXpath("//*[@class='mt16']//button", theme).getCssValue("background-color"));
            String payButtonTextColor = getColorCode(cashierPage.proceedBtn().getCssValue("color"));
            String bodyBGColor = getColorCode(getElementByXpath("//*[@class='bgcolor']//div//div", theme).getCssValue("background-color"));
//            String bodyTextColor = getColorCode(cashierPage.tabCreditCard().getCssValue("color"));

            SoftAssertions softy = new SoftAssertions();
            softy.assertThat(headerColor)
                    .as("HEADERPANEL color mismatch")
                    .isEqualToIgnoringCase(td.get("HEADERPANEL"));
            softy.assertThat(headerTxtColor)
                    .as("HEADERTEXT color mismatch")
                    .isEqualToIgnoringCase(td.get("HEADERTEXT"));
            softy.assertThat(payButtonBGColor)
                    .as("PAYBUTTONBG color mismatch")
                    .isEqualToIgnoringCase(td.get("PAYBUTTONBG"));
            softy.assertThat(payButtonTextColor)
                    .as("PAYBUTTONTEXT mismatch")
                    .isEqualToIgnoringCase(td.get("PAYBUTTONTEXT"));
            softy.assertThat(bodyBGColor)
                    .as("BODYBG color mismatch")
                    .isEqualToIgnoringCase(td.get("BODYBG"));
         //   softy.assertThat(bodyTextColor)
         //           .as("BODYTEXT color mistmatch")
         //           .isEqualToIgnoringCase(td.get("BODYTEXT"));
            softy.assertAll();

        }
        finally {
            MerchantAddPreferenceInfoReq req = new MerchantAddPreferenceInfoReq
                    .Builder(m.getId(), "THEMATIC_FEATURE_USED", "ACTIVE", "N")
                    .build();

            /* After SI HUB Release CC/DC Subs is not supported
            RestAssured.given().contentType(ContentType.JSON)
                    .spec(reqSpec)
                    .baseUri(LocalConfig.PGP_HOST)
                    .basePath(Constants.MappingService.ADD_MERCHANT_PREFRENCE_INFO)
                    .body(req)
                    .post()
                    .then()
                    .statusCode(200)
                    .body("restStatus", equalToIgnoringCase("SUCCESS"))
                    .body("response.resultCode", equalTo("00000"),
                            "response.resultStatus", equalTo("S"),
                            "response.messaage", equalTo("Success"));
             */
        }

    }

    private UIElement getElementByXpath(String xpath, String theme) {
        return new UIElement(By.xpath(xpath), theme, xpath);
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

    private Map<String, String> getColorScheme(String colorTheme) throws IOException {
        Map m = new ObjectMapper().readValue(colorTheme, Map.class);
        return ((Map<String, String>) ((Map) m.get("REDIRECTION")).get("web"));
    }

}
