package scripts;

import com.paytm.api.RefundApi;
import com.paytm.api.RefundCustomSync;
import com.paytm.api.RefundReversal;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Duration;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.awaitility.Awaitility.with;

@Owner("Shubham Soni")
public class GoogleRefundCustomSync extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();


    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for DC payMode")
    public void refundDCWithJWTAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.BAJAJFN_MID_DBD;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
        .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00","123456","payments");
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for DC payMode")
    public void refundDCWithChecksumAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.BAJAJFN_MID_DBD;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00",refundMerchant.getKey());
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for CC payMode")
    public void refundCCWithJWTAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.ADDNPAY_MCC_ADDMONEY;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00","123456","payments");
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for CC payMode")
    public void refundCCWithChecksumAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.ADDNPAY_MCC_ADDMONEY;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00",refundMerchant.getKey());
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for NB payMode")
    public void refundNBWithJWTAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.BAJAJFN_MID_DBD;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDto = new PaymentDTO();
        paymentDto.setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDto);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00","123456","payments");
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for NB payMode")
    public void refundNBWithChecksumAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.BAJAJFN_MID_DBD;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDto = new PaymentDTO();
        paymentDto.setBankName("ICICI");
        cashierPage.payBy(Constants.PayMode.NB, paymentDto);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00",refundMerchant.getKey());
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for UPI payMode")
    public void refundUPIWithJWTAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.BAJAJFN_MID_DBD;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00","123456","payments");
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }

    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for UPI payMode")
    public void refundUPIWithChecksumAsyncRefund(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.BAJAJFN_MID_DBD;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("2.00")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS").assertAll();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00",refundMerchant.getKey());
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.acceptRefundStatus").equals("SUCCESS"));
        softly.assertThat(response.getString("body.userCreditInitiateStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("PENDING"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("601"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Refund request was raised for this transaction. But it is pending state"));
        softly.assertAll();
        Assertions.assertThat(response.getString("body.refundId")).isNotEmpty();
    }
    @Owner("Shubham Soni")
    @Feature("PGP-53605")
    @Parameters({"theme"})
    @Test(description = "Verify Google Custom REFUND API for DC payMode for Pending transaction")
    public void refundDCWithJWTAsyncRefundPending(@Optional("enhancedweb_revamp") String theme) {

        Constants.MerchantType refundMerchant = Constants.MerchantType.BAJAJFN_MID_DBD;
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(refundMerchant.getId());
        }
        OrderDTO orderDTO = new OrderFactory.PGOnly(refundMerchant, theme)
                .setTXN_AMOUNT("99.84")
                .build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("PENDING").assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateStatus("PENDING")
                .AssertAll();
        RefundCustomSync refundCustomSync = new RefundCustomSync(orderDTO.getMID(), orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"2.00","123456","payments");
        JsonPath response =  refundCustomSync.execute().jsonPath();
        SoftAssertions softly = new SoftAssertions();
        softly.assertThat(response.getString("body.resultInfo.resultStatus").equals("TXN_FAILURE"));
        softly.assertThat(response.getString("body.resultInfo.resultCode").equals("600"));
        softly.assertThat(response.getString("body.resultInfo.resultMsg").equals("Invalid refund request or restricted by bank"));
        softly.assertAll();
    }

}
