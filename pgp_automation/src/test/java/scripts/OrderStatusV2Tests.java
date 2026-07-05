package scripts;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.paytm.LocalConfig;
import com.paytm.api.OrderStatusV2API;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.OrderStatusV2.Body;
import com.paytm.dto.OrderStatusV2.Head;
import com.paytm.dto.OrderStatusV2.OrderStatusV2DTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;


public class OrderStatusV2Tests extends PGPBaseTest {

    public PGPHelpers pgpHelpers = new PGPHelpers();
    CheckoutPage checkoutPage;

    public String executeOrderStatusV2andgetCustId(OrderDTO orderDTO) throws UnsupportedEncodingException {
        String jwtToken = JWT.create().withIssuer("upi-app")
                .withClaim("iss", "upi-app")
                .withClaim("BODY","{\"mid\":\""+ orderDTO.getMID() + "\",\"orderId\":\"" + orderDTO.getORDER_ID() + "\"}")
                .sign(Algorithm.HMAC256(LocalConfig.JWT_KEY));
        OrderStatusV2DTO orderStatusV2DTO = new OrderStatusV2DTO();
        orderStatusV2DTO.setHead(new Head().setClientId("upi-app").setTokenType("JWT")
                .setToken(jwtToken));
        orderStatusV2DTO.setBody(new Body().setMid(orderDTO.getMID()).setOrderId(orderDTO.getORDER_ID()));
        Response response = new OrderStatusV2API(orderStatusV2DTO).execute();
        String custId = response.jsonPath().getString("body.custId");
        return custId;
    }

    @BeforeClass
    public void verifyff4jFlagEnabled(){
        String query = "SELECT * FROM PGPDB.FF4J_FEATURES where FEAT_UID = 'merchantStatus.addCustIdInV2OrderStatusResponse';";
        List<Map<String, Object>> result = DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, query);
        int status = (int)result.get(0).get("ENABLE");
        if(status != 1)
            throw new SkipException("FF44J feature merchantStatus.addCustIdInV2OrderStatusResponse is not enabled");
    }

    @Parameters({"theme"})
    @Test(description = "Verify that cust id is returned for success transactions")
    public void verifyCustIdReturnedforSuccessTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.PGOnly, theme, user)
                .setCUST_ID("cust123")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String custId = executeOrderStatusV2andgetCustId(orderDTO);
        Assertions.assertThat(custId).isNotNull();
        Assertions.assertThat(custId).isEqualTo("cust123");
    }

    @Parameters({"theme"})
    @Test(description = "Verify that cust id is returned for failure transactions")
    public void verifyCustIdReturnedforFailureTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.PGOnly, theme, user)
                .setCUST_ID("cust123")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
        cashierPage.payBy(Constants.PayMode.CC, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("227")
                .validateStatus("TXN_FAILURE")
                .assertAll();
        String custId = executeOrderStatusV2andgetCustId(orderDTO);
        Assertions.assertThat(custId).isNotNull();
        Assertions.assertThat(custId).isEqualTo("cust123");
    }

    @Parameters({"theme"})
    @Test(description = "Verify that cust id is returned for pending transactions")
    public void verifyCustIdReturnedforPendingTxn(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.PGOnly, theme, user)
                .setCUST_ID("cust123")
                .setTXN_AMOUNT("99.84")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("402")
                .validateStatus("PENDING")
                .assertAll();
        String custId = executeOrderStatusV2andgetCustId(orderDTO);
        Assertions.assertThat(custId).isNotNull();
        Assertions.assertThat(custId).isEqualTo("cust123");
    }

    @Parameters({"theme"})
    @Test(description = "Verify that the cust id is not returned in the response of the order status api when ff4j flag is on but mid is not configured.")
    public void verifyCustIdNotReturnedwhenMidNotConfigured(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.WalletOnly, theme, user)
                .setCUST_ID("cust123")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.WALLET);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String custId = executeOrderStatusV2andgetCustId(orderDTO);
        Assertions.assertThat(custId).isNull();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that the cust id is returned for onus merchants")
    public void verifyCustIdReturnedforONUSMerchants(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.PGOnly_Retry, theme, user)
                .setCUST_ID("cust123")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String custId = executeOrderStatusV2andgetCustId(orderDTO);
        Assertions.assertThat(custId).isNotNull();
        Assertions.assertThat(custId).isEqualTo("cust123");
    }

    @Parameters({"theme"})
    @Test(description = "Verify that the cust id is returned for offus merchants")
    public void verifyCustIdReturnedforOFFUSMerchants(@Optional("enhancedweb") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        checkoutPage = new CheckoutPage();
        OrderDTO orderDTO = new OrderFactory.WalletOnly(Constants.MerchantType.PaytmExpress_AddnPay_Offus, theme, user)
                .setCUST_ID("cust123")
                .build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
        String custId = executeOrderStatusV2andgetCustId(orderDTO);
        Assertions.assertThat(custId).isNotNull();
        Assertions.assertThat(custId).isEqualTo("cust123");
    }
}
