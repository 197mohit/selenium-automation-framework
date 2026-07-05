package scripts.feedbackTest;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner(Constants.Owner.PRIYANSHI)
@Epic(Constants.Sprint.SPRINT36_3)
@Feature("PGP-24609")
public class FeedbackTesting  extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verifying go back button is clickable or not in feedback box")
    public void checkGoBackClicked_TC01(@Optional("enhancedweb_revamp") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        pageWait.apply(cashierPage.backBtn().isClickable());

    }
    @Parameters({"theme"})
    @Test(description = "Checking Cross button is clickable or not in feedback box ")

    public void checkCrossClicked_TC02(@Optional("enhancedweb_revamp") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackCrossBtn().click();
        cashierPage.feedbackBox().assertNotVisible();
       // Assertions.assertThat(cashierPage.feedbackBox().isDisplayed()).isEqualTo(false);
    }
    @Parameters({"theme"})
    @Test(description = "Checking skip feedback button in feedback box and verifying its RespCode and RespMsg ")
    public void skipFeedbackClicked_TC03(@Optional("enhancedweb_revamp") String theme) {

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackSkipFeedback().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("141");
        responsePage.validateRespMsg("User has not completed transaction.").assertAll();


    }
    @Parameters({"theme"})
    @Test(description = "Clicking on submit button, without selecting any reasons")

    public void withoutCheckSubmit_TC04(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackSubmitBtn().click();
        cashierPage.feedbackPleaseSelectReason().assertContainsText("Please select a feedback");

    }
    @Parameters({"theme"})
    @Test(description = "On clicking Other (Please mention) and not entering any reason in test box and submitting the feedback")

    public void notEnteringReason_TC05(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioBtnOther().click();
        cashierPage.feedbackSubmitBtn().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("141");
        responsePage.validateRespMsg("User has not completed transaction.").assertAll();
    }
    @Parameters({"theme"})
    @Test(description = "Verifying the check box of Could not find the expected payMode")

    public void notFindExpPayMode_TC06(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioExpPaymode().click();
        Assertions.assertThat(cashierPage.feedbackRadioExpPaymode().isSelected()).isEqualTo(true);

    }
    @Parameters({"theme"})
    @Test(description = "Selecting Could not find the expected payMode and clicking the submit button")

    public void notFindExpPayModeSubmit_TC07(@Optional("enhancedweb_revamp") String theme){

        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioExpPaymode().click();
        cashierPage.feedbackSubmitBtn().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("141");
        responsePage.validateRespMsg("User has not completed transaction.").assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Verifying the check box of Insufficient Paytm Wallet balance")

    public void insuffPaytmBalnaceCheck_TC08(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioInsuffBalnce().click();
        Assertions.assertThat(cashierPage.feedbackRadioInsuffBalnce().isSelected()).isEqualTo(true);


    }
    @Parameters({"theme"})
    @Test(description = "Selecting Insufficient Paytm Wallet balance and clicking the submit button")

    public void insuffPaytmBalnaceSubmit_TC09(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioInsuffBalnce().click();
        cashierPage.feedbackSubmitBtn().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("141");
        responsePage.validateRespMsg("User has not completed transaction.").assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Verifying the check box of Forgot Paytm Bank Passcode")

    public void forgetPaytmBankPassCheck_TC10(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioForgetPaytmPass().click();
        Assertions.assertThat(cashierPage.feedbackRadioForgetPaytmPass().isSelected()).isEqualTo(true);


    }
    @Parameters({"theme"})
    @Test(description = "Selecting Forgot Paytm Bank Passcode and clicking the submit button")

    public void forgetPaytmBankPassSubmit_TC11(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioForgetPaytmPass().click();
        cashierPage.feedbackSubmitBtn().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("141");
        responsePage.validateRespMsg("User has not completed transaction.").assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Verifying the check box of Expected cashback/promo did not work")

    public void expectedCashbackNotworkingCheck_TC12(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioCashbackNotWork().click();
        Assertions.assertThat(cashierPage.feedbackRadioCashbackNotWork().isSelected()).isEqualTo(true);


    }
    @Parameters({"theme"})
    @Test(description = "Selecting Expected cashback/promo did not work and clicking the submit button")

    public void cashbackNotWorkingSubmit_TC13(@Optional("enhancedweb") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioCashbackNotWork().click();
        cashierPage.feedbackSubmitBtn().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("141");
        responsePage.validateRespMsg("User has not completed transaction.").assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Verifying the checkbox of Other (Please mention)")

    public void verifyOtherCheck_TC14(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioBtnOther().click();
        Assertions.assertThat(cashierPage.feedbackRadioBtnOther().isSelected()).isEqualTo(true);


    }
    @Parameters({"theme"})
    @Test(description = "Verifying the text box on clicking Other (Please mention) and entering the characters, verifying res code and res msg for the same")

    public void verifyOtherSubmit_TC15(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackRadioBtnOther().click();
        cashierPage.feedbackTextArea().sendKeys("I have No problem");
        cashierPage.feedbackSubmitBtn().click();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("141");
        responsePage.validateRespMsg("User has not completed transaction.").assertAll();

    }
    @Parameters({"theme"})
    @Test(description = "Verifying when after closing the box by clicking cross button and clicking again the back button")

    public void crossAndGoBackCheck_TC16(@Optional("enhancedweb_revamp") String theme){
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.PGOnly, theme)
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.waitUntilLoads();
        cashierPage.backBtn().click();
        cashierPage.feedbackCrossBtn().click();
        pageWait.apply(cashierPage.backBtn().isClickable());


    }
}
