package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.Date;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.PayMode;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.appconstants.Constants.Bank;

@Owner("Deepak")
public class BinInResponse extends PGPBaseTest{

    private final CheckoutPage checkoutPage = new CheckoutPage();
    private String getPaymentDetails(PaymentDTO paymentDTO){
        return paymentDTO.getDebitCardNumber()+"|"+paymentDTO.getCvvNumber()+"|"+paymentDTO.getExpMonth()+paymentDTO.getExpYear();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that last four digits and bin of card is visible on response page when preference BIN_IN_RESPONSE is enabled on a merchant")
    public void validatelastfourdigitsbin_tc1(@Optional("false") Boolean isNativePlus) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateBinInResponseEnabled(MerchantType.BIN_IN_RESPONSE_NATIVE.getId());
        }
        PaymentDTO paymentDTO=new PaymentDTO();
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BIN_IN_RESPONSE_NATIVE).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_NATIVE, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setPAYMENT_DETAILS(getPaymentDetails(paymentDTO))
                .build();
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "444433");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        new ResponsePage().waitUntilLoads();
        String debitCardNumber = paymentDTO.getDebitCardNumber();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        String lastFourDigits=debitCardNumber.substring(debitCardNumber.length()-4, debitCardNumber.length());
        String cardBin=debitCardNumber.substring(0,6);
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateLastFourDigits(lastFourDigits)
                .validateCardBin(cardBin)
                .AssertAll();
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Verify that last four digits and bin of card is not visible on response page when preference BIN_IN_RESPONSE is disabled on a merchant")
    public void validatelastfourdigitsbin_tc2(@Optional("false") Boolean isNativePlus) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateBinInResponseDisabled(MerchantType.BIN_IN_RESPONSE_DISABLED.getId());
        }
        PaymentDTO paymentDTO=new PaymentDTO();
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.BIN_IN_RESPONSE_DISABLED).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.BIN_IN_RESPONSE_DISABLED, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setPAYMENT_DETAILS(getPaymentDetails(paymentDTO))
                .build();
        Validate_BinDetail(txnToken, initTxnDTO, orderDTO, "444433");
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        new ResponsePage().waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateLastFourDigits(null)
                .validateCardBin(null)
                .AssertAll();
    }

    @Parameters({"theme"})
    @Test(description = "Verify that last four digits and bin of card is not visible on an add and pay txn when preference BIN_IN_RESPONSE is enabled on a merchant")
    public void validatelastfourdigitsbin_tc3(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateBinInResponseEnabled(MerchantType.BIN_IN_RESPONSE_ADDNPAY.getId());
        }
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddnPay(MerchantType.BIN_IN_RESPONSE_ADDNPAY, theme, user).build();
        WalletHelpers.modifyBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(PayMode.CC);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .validateLastFourDigits(null)
                .validateCardBin(null)
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);
    }


    @Parameters({"theme"})
    @Test(description = "Verify that last four digits and bin of card is visible on a hybrid txn when preference BIN_IN_RESPONSE is enabled on a merchant")
    public void validatelastfourdigitsbin_tc4(@Optional("enhancedweb") String theme) throws Exception {
        prerequisite:
        {
            PGPHelpers.validateBinInResponseEnabled(MerchantType.BIN_IN_RESPONSE_HYBRID.getId());
        }
        PaymentDTO paymentDTO=new PaymentDTO();
        User user = userManager.getForWrite(Label.BASIC, Label.NOPOSTPAID);
        OrderDTO orderDTO = new OrderFactory.Hybrid(MerchantType.BIN_IN_RESPONSE_HYBRID, theme, user)
                .setTXN_AMOUNT("2.00").build();
        double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
        WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
        checkoutPage.createOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.checkBoxPPI().check();
        cashierPage.payBy(PayMode.DC);
        String debitCardNumber = paymentDTO.getDebitCardNumber();
        String lastFourDigits=debitCardNumber.substring(debitCardNumber.length()-4, debitCardNumber.length());
        String cardBin=debitCardNumber.substring(0,6);

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("HYBRID")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent()
                .validateStatusAPIParameters();

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.BANK)
                .validateTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "DC")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS")
                .validateCardBin(TxnStatus.ChildTxnType.BANK,cardBin)
                .validateLastFourDigits(TxnStatus.ChildTxnType.BANK,lastFourDigits);

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(amountToBeRetainedInWallet))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.WALLET,"WALLET")
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    protected void Validate_BinDetail(String txnToken, InitTxnDTO initTxnDTO, OrderDTO orderDTO, String binNum) {
        Reporter.report.info("Validating binDetails API  with txn token" + orderDTO.getBANK_CODE());
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, binNum).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(fetchBinsJson.getString("body.binDetail")).isNotNull();
    }
}
