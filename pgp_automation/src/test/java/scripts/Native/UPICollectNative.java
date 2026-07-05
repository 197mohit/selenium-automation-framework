package scripts.Native;

import com.paytm.LocalConfig;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.UPIPollingPage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.*;

import java.util.Arrays;

@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Somesh Saxena")
public class UPICollectNative extends PGPBaseTest {

    boolean ff4jFlagEnabled;

  /*  @Factory(dataProvider = "dataMethod")
    UPICollectNative(boolean ff4jFlagEnabled) {
        this.ff4jFlagEnabled = ff4jFlagEnabled;
    } */

    @DataProvider(name = "dataMethod")
    static Object[][] dataMethod() {
        return new Object[][]{
                {true},
                {false}
        };
    }

    @BeforeClass
    public void setFf4jFlag() {
        //commenting below line because upiPollPageMid will be always on as this flag is on prod. no need to check flag state
         int isEnabled = this.ff4jFlagEnabled ? 1 : 0;
        //int isEnabled = 1;
        String queryForExp = "UPDATE FF4J_FEATURES SET ENABLE = " + isEnabled + " WHERE FEAT_UID = 'upiPollPageMid';";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, queryForExp);
        TRANSACTIONAL_REDIS_CLUSTER().del("FF4J_FEATURE_upiPollPageMid");
       // RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI).del("FF4J_FEATURE_upiPollPageMid");
    }

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final UPIPollingPage upiPollingPage = new UPIPollingPage();

    private void initTxnAndPay(Boolean isNativePlus, PaymentDTO paymentDTO) throws Exception {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(userManager.getForRead(Label.BASIC).ssoToken(),
                Constants.MerchantType.NATIVE_HYBRID).build();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.NATIVE_HYBRID,
                initTxnDTO.orderFromBody(),
                new InitTxn(initTxnDTO).executeInitTxn(initTxnDTO).getBody().getTxnToken(),
                paymentDTO,
                PayMethodType.UPI)
                .setPayerAccount(paymentDTO.getVpa()).build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
    }

    private void validateAppDetails(String upiAddress, String key) {
        String appName = upiPollingPage.getAppDetailsMap().get(key).get(0);
        String appImage = upiPollingPage.getAppDetailsMap().get(key).get(1);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.
                assertThat(upiPollingPage.getTextUPIAddress().getText()).
                as("upi-address-mismatch").contains(upiAddress);
        softAssertions.
                assertThat(upiPollingPage.getTextGoToApp().getText()).
                as("go-to-app-name-mismatch").contains(appName);
        softAssertions.
                assertThat(upiPollingPage.getImageGoToApp(appImage).isDisplayed()).
                as("go-to-app-image-mismatch").isTrue();
        Reporter.report.info("Validate AppDetails : Actual = " + Arrays.asList(upiPollingPage.getTextUPIAddress().getText(), upiPollingPage.getTextGoToApp().getText(), "app image displayed - " + upiPollingPage.getImageGoToApp(appImage).getAttribute("src")));
        Reporter.report.info("Validate AppDetails : Expected = " + Arrays.asList(upiAddress, appName, appImage));
        softAssertions.assertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @ybl", groups = {"smoke"})
    public void appName_VPA_YBL(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("mansigupta130543@ybl");
        initTxnAndPay(isNativePlus, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@ybl");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @ok*", groups = {"smoke"})
    public void appName_VPA_OK(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("karmvir20@okhdfcbank");
        initTxnAndPay(isNativePlus, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@ok*");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @upi", groups = {"smoke"})
    public void appName_VPA_UPI(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("8527854369@upi");
        initTxnAndPay(isNativePlus, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@upi");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @paytm", groups = {"smoke"})
    public void appName_VPA_Paytm(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO();
        initTxnAndPay(isNativePlus, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@paytm");
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify the App name and logo on UPI polling page for VPA @default", groups = {"smoke"})
    public void appName_VPA_Default(@Optional("false") Boolean isNativePlus) throws Exception {
        PaymentDTO paymentDTO = new PaymentDTO().setVpa("9840752358@apl");
        initTxnAndPay(isNativePlus, paymentDTO);
        /* validation */
        validateAppDetails(paymentDTO.getVpa(), "@default");
    }
}
