package scripts;


import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.MerchantManager;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.merchant.DefaultCommission;
import com.paytm.utils.merchant.merchant.ExistingMerchantContract;
import com.paytm.utils.merchant.merchant.Merchant;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import com.paytm.base.test.User;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Gagandeep")
public class RefundTroubleshooting extends PGPBaseTest{
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Parameters({"theme"})
    @Test(description = "Verify successful refund to source via troubleshooting for CC")
    public void verifyRefundtroubleshootingviaCC(@Optional("merchant4") String theme) throws  Exception
    {
        prerequisite :
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.REFUND_IMPSPGONLY.getId());
        }
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PGOnly(Constants.MerchantType.REFUND_IMPSPGONLY, theme).setTXN_AMOUNT("20.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.CC);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateSuccessResponse()
                .validateTxnId(Constants.ValidationType.NON_EMPTY).AssertAll();

        Response response = PGPHelpers.generateCacheCardToken(orderDTO.getMID(), orderDTO.getMerchantKey(), "", "saudsagg", "CITI0000002", null, "8847568788", "qwerty");

        AssertCacheCardToken :
        {
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("S");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("1");

        }
        String token = response.jsonPath().get("body.token").toString();

        RefundIMPS : {
            Response responseAsyncRefund = PGPHelpers.initiateAsyncRefundImps(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "10.00", "REFUND", "", token, "TO_INSTANT");
            Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("PENDING");
            Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("601");
        }
        Test:
        {
            PGPHelpers.getRefundStatusV1(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getMerchantKey(), true)
                    .validateRetryInfo("REFUND_TO_SOURCE", paymentDTO.getBankName(), RefundStatusV1Helper.PAY_METHODS.CREDIT_CARD, "VISA", "10.00").asserAll();
        }

    }



    @Parameters({"theme"})
    @Test(description = "Verify successful refund of Hybrid Transaction.")
    public void verifyRefundtroubleshootingviaDCANDWallet(@Optional("merchant4") String theme) throws  Exception {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.REFUND_IMPSHYBRID.getId());
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.REFUND_IMPSHYBRID, theme, user)
                .setTXN_AMOUNT("20.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 5.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        PaymentDTO paymentDTO=new PaymentDTO();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .validateChildTxnsPresent();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();

        Response response=PGPHelpers.generateCacheCardToken(orderDTO.getMID(),orderDTO.getMerchantKey(),"","gdsajhgdsjhjas","CITI0000002",null,"8847568788","qwerty");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus Matched").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode Matched").isEqualToIgnoringCase("1");

        String token=response.jsonPath().get("body.token").toString();

        Response responseAsyncRefund=PGPHelpers.initiateAsyncRefundImps(orderDTO.getMID(),orderDTO.getMerchantKey(),orderDTO.getORDER_ID(),orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),"12.00","REFUND","",token,"TO_INSTANT");
        Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("PENDING");
        Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("601");

        PGPHelpers.getRefundStatusV1(orderDTO.getMID(),orderDTO.getORDER_ID(),orderDTO.getORDER_ID(),orderDTO.getMerchantKey(),true)
                .validateRetryInfo("REFUND_TO_SOURCE",paymentDTO.getBankName(), RefundStatusV1Helper.PAY_METHODS.DEBIT_CARD,"VISA","10.00").asserAll();

    }


    @Issue("SMP1-4378")
    @Parameters({"theme"})
    @Test(description = "Verify successful refund to source via troubleshooting for PPBL")
    public void verifyRefundtroubleshootingviaPPBL(@Optional("merchant4") String theme) throws  Exception
    {
        PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.REFUND_IMPSPGONLY.getId());
        User user = userManager.getForWrite(PGPBaseTest.Label.PPBL);
        OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.REFUND_IMPSPGONLY, theme,user).setTXN_AMOUNT("20.00").build();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO=new PaymentDTO();
        cashierPage.payBy(Constants.PayMode.PPBL);
        cashierPage.waitUntilLoads();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateSuccessResponse()
                .validateTxnId(Constants.ValidationType.NON_EMPTY).AssertAll();

        Response response = PGPHelpers.generateCacheCardToken(orderDTO.getMID(), orderDTO.getMerchantKey(), "", "saudsagg", "CITI0000002", null, "8847568788", "qwerty");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("S");
        Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("1");

        String token = response.jsonPath().get("body.token").toString();

        Response responseAsyncRefund = PGPHelpers.initiateAsyncRefundImps(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), "10.00", "REFUND", "", token, "TO_INSTANT");
        Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("PENDING");
        Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("601");


        PGPHelpers.getRefundStatusV1(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getMerchantKey(), true)
                .validateRetryInfo("REFUND_TO_SOURCE",paymentDTO.getBankName(), RefundStatusV1Helper.PAY_METHODS.PPBL,"VISA","10.00").asserAll();



    }

    @Parameters({"theme"})
    @Test(description = "Verify successful refund of Hybrid Transaction.")
    public void verifyRefundtroubleshootingFailviaCCANDWallet(@Optional("merchant4") String theme) throws  Exception {
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(Constants.MerchantType.REFUND_IMPSHYBRID.getId());

            User user = userManager.getForWrite(Label.PPBL);
            OrderDTO orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.REFUND_IMPSHYBRID, theme, user)
                    .setSSO_TOKEN(user.ssoToken())
                    .setTXN_AMOUNT("2.0").build();
            double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
            WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
            checkoutPage.createOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            PaymentDTO paymentDTO=new PaymentDTO();
            cashierPage.payBy(Constants.PayMode.CC);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateSuccessResponse()
                    .validateChildTxnsPresent();

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                    .validateTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "CC")
                    .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.BANK, Constants.Gateway.HDFC.toString())
                    .validateBankTxnId(TxnStatus.ChildTxnType.BANK, Constants.ValidationType.NON_EMPTY)
                    .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                    .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

            txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                    .validateTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                    .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                    .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                    .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                    .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, Constants.ValidationType.NON_EMPTY)
                    .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                    .AssertAll();

            Response response=PGPHelpers.generateCacheCardToken(orderDTO.getMID(),orderDTO.getMerchantKey(),"","abcd","CITI0000002",null,"hadskdhhadsh","qwerty");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus Matched").isEqualToIgnoringCase("S");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode Matched").isEqualToIgnoringCase("1");

            System.out.println("response:: "+response);
            String token=response.jsonPath().get("body.token").toString();

            Response responseAsyncRefund=PGPHelpers.initiateAsyncRefundImps(orderDTO.getMID(),orderDTO.getMerchantKey(),orderDTO.getORDER_ID(),orderDTO.getORDER_ID(),txnStatus.getResponse().getTXNID(),orderDTO.getTXN_AMOUNT(),"REFUND","",token,"TO_INSTANT");
            PGPHelpers.getRefundStatusV1(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getMerchantKey(), true)
                    .validateRetryInfo("REFUND_TO_SOURCE",paymentDTO.getBankName(), RefundStatusV1Helper.PAY_METHODS.PPBL,"VISA","10.00").asserAll();

        }}


    @Parameters({"theme"})
    @Test(description = "Verify successful refund troubleshooting initiated For PCF Merchant with Conv Fees.")
    public void verifyRefundtroubleshootingForPCFMerchantWithFee(@Optional("merchant4") String theme) throws  Exception
    {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
        }
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE)
            );
            contract.apply(
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }

        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                .setTXN_AMOUNT("30.00")
                .build();
        PaymentDTO paymentDTO=new PaymentDTO();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String partialRefundAmount = String.valueOf(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 10.00);

        Response response = PGPHelpers.generateCacheCardToken(orderDTO.getMID(), orderDTO.getMerchantKey(), "", "saudsagg", "CITI0000002", null, "8847568788", "qwerty");

        AssertCacheCardToken :
        {
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("S");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("1");
        }
        String token = response.jsonPath().get("body.token").toString();
        RefundIMPS : {
            Response responseAsyncRefund = PGPHelpers.initiateAsyncRefundImps(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), partialRefundAmount, "R", "", token, "TO_INSTANT");
            Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("PENDING");
            Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("601");
        }
        Test:
        {
            PGPHelpers.getRefundStatusV1(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getMerchantKey(), true)
                    .validateRetryInfo("REFUND_TO_SOURCE",paymentDTO.getBankName(), RefundStatusV1Helper.PAY_METHODS.CREDIT_CARD,"VISA",partialRefundAmount).asserAll();
        }

    }


    @Parameters({"theme"})
    @Test(description = "Verify successful refund troubleshooting initiated For PCF Merchant without Conv Fees.")
    public void verifyRefundtroubleshootingForPCFMerchantWithoutFee(@Optional("merchant4") String theme) throws  Exception
    {
        String mid = MerchantManager.getMerchant();
        String key = MerchantManager.getMerchantKey(mid);
        prerequisite:
        {
            PGPHelpers.validateRefundAllowedWithChecksum(mid);
        }
        double ccFlatCommission = 1.15;
        MerchantConfiguration:
        {
            ExistingMerchantContract contract = new ExistingMerchantContract(mid);
            contract.apply(
                    Merchant.Default(1, Merchant.ConvFeeType.POST_CONVENIENCE)
            );
            contract.apply(
                    DefaultCommission.SimpleFlat(ccFlatCommission)
            );
        }

        OrderDTO orderDTO = new OrderFactory.PGOnly(mid, key, theme)
                .setTXN_AMOUNT("30.00")
                .build();
        PaymentDTO paymentDTO=new PaymentDTO();
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.CC);
        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String partialRefundAmount = String.valueOf(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 10.00);

        Response response = PGPHelpers.generateCacheCardToken(orderDTO.getMID(), orderDTO.getMerchantKey(), "", "saudsagg", "CITI0000002", null, "8847568788", "qwerty");

        AssertCacheCardToken :
        {
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("S");
            Assertions.assertThat(response.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("1");
        }
        String token = response.jsonPath().get("body.token").toString();
        RefundIMPS : {
            Response responseAsyncRefund = PGPHelpers.initiateAsyncRefundImps(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), txnStatus.getResponse().getTXNID(), partialRefundAmount, "C", "", token, "TO_INSTANT");
            Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultStatus").toString()).as("ResultStatus mismatch").isEqualToIgnoringCase("PENDING");
            Assertions.assertThat(responseAsyncRefund.jsonPath().get("body.resultInfo.resultCode").toString()).as("ResultCode mismatch").isEqualToIgnoringCase("601");
        }
        Test:
        {  PGPHelpers.getRefundStatusV1(orderDTO.getMID(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getMerchantKey(), true)
                .validateRetryInfo("REFUND_TO_SOURCE",paymentDTO.getBankName(), RefundStatusV1Helper.PAY_METHODS.CREDIT_CARD,"VISA",partialRefundAmount).asserAll();
        }
    }
}