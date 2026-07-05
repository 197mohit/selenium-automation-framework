package scripts;

import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.UPIPollingPage;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Arrays;

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Somesh Saxena")
public class UPICollectAppName extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final UPIPollingPage upiPollingPage = new UPIPollingPage();

    private void upiPayment(String theme, PaymentDTO paymentDTO) {
        OrderDTO orderDTO = new OrderFactory.PGOnly(MerchantType.PGOnly, theme)
                .setORDER_ID("delay_theia"+ CommonHelpers.generateOrderId()).build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.UPI, paymentDTO);
    }

    @Parameters({"theme"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @ybl", groups = {"smoke"})
    public void appName_VPA_YBL(@Optional("enhancedwap") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("mansigupta130543@ybl");
        upiPayment(theme, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@ybl");
    }

    @Parameters({"theme"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @ok*", groups = {"smoke"})
    public void appName_VPA_OK(@Optional("enhancedweb") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("karmvir20@okhdfcbank");
        upiPayment(theme, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@ok*");
    }

    @Parameters({"theme"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @upi", groups = {"smoke"})
    public void appName_VPA_UPI(@Optional("enhancedweb") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("test@upi");
        upiPayment(theme, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@upi");
    }

    @Parameters({"theme"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @paytm", groups = {"smoke"})
    public void appName_VPA_Paytm(@Optional("enhancedweb") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        upiPayment(theme, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@paytm");
    }

    @Parameters({"theme"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @default", groups = {"smoke"})
    public void appName_VPA_Default(@Optional("enhancedweb") String theme) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("9840752358@apl");
        upiPayment(theme, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@default");
    }

    private void validateAppDetails(String upiAddress, String key) {
        String appName = upiPollingPage.getAppDetailsMap().get(key).get(0);
        String appImage = upiPollingPage.getAppDetailsMap().get(key).get(1);
        SoftAssertions softAssertions = new SoftAssertions();
        //softAssertions.
               // assertThat(upiPollingPage.getTextUPIAddress().getText()).
               // as("upi-address-mismatch").isEqualToIgnoringCase(upiAddress);
        softAssertions.
                assertThat(upiPollingPage.getTextGoToApp().getText()).
                as("go-to-app-name-mismatch").containsIgnoringCase(appName);
        softAssertions.
                assertThat(upiPollingPage.getImageGoToApp(appImage).isDisplayed()).
                as("go-to-app-image-mismatch").isTrue();
        Reporter.report.info("Validate AppDetails : Actual = " + Arrays.asList(upiPollingPage.getTextUPIAddress().getText(), upiPollingPage.getTextGoToApp().getText(), "app image displayed - " + upiPollingPage.getImageGoToApp(appImage).getAttribute("src")));
        Reporter.report.info("Validate AppDetails : Expected = " + Arrays.asList(upiAddress, appName, appImage));
        softAssertions.assertAll();
    }

}
