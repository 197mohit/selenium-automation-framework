package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.Peon;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Gateway;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group.Status;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Date;

import static com.paytm.appconstants.Constants.MerchantType.*;
//TODO
@Owner("Deepak")
public class PayTMExpress extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final RiskRejectHelper riskRejectHelper = new RiskRejectHelper();

    @DataProvider(name = "MerchantProvider")
    public static Object[][] MerchantProvider(Method m) {
        if (m.getName().equalsIgnoreCase("PGP_successfulPtmX2pressPPI")) {
            return new MerchantType[][]{{PaytmExpress_Hybrid_Onus}, {POSTCONV_WALLET_ONLY}};
        } else {
            return new MerchantType[][]{{COFT_MERCHANT}, {POSTCONV_DEFAULT}};
        }
    }

    @Test(description = "Validate successful Paytm Express transaction using CC and card not getting saved", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_successfulPtmX2pressCC(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        SavedCardHelpers.validateSavedCardAbsence(user);
    }

    @Test(description = "Validate Bank retry count equals to five in Paytm Express flow")
    public void ValidatePaymentsRetryCountEqualsFive(@Optional("PaytmExpress_Hybrid_Onus") MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setTXN_AMOUNT("99.84")
                .build();
        for(int i=0; i<5;i++) {
            checkoutPage.createOrder(orderDTO);
            ResponsePage responsePage = new ResponsePage();
            responsePage.waitUntilLoads();
            responsePage.validateStatus("PENDING")
                    .assertAll();
        }
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE")
                .assertAll();
    }

    @Test(description = "Validate card getting saved for successful Paytm Express transaction using CC.", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_verifyCardGettingSavedForsuccessfulPtmX2pressCC(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setSTORE_CARD("1")
                .build();
        checkoutPage.createOrder(orderDTO);

       ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        SavedCardHelpers.validateSavedCardPresence(user);
    }

    @Test(description = "Validate successful Paytm Express transaction using DC and card not getting saved", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_successfulPtmX2pressDC(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "DC", user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        SavedCardHelpers.validateSavedCardAbsence(user);
    }

    @Test(description = "Validate card getting saved for successful Paytm Express transaction using DC.", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_verifyCardGettingSavedForsuccessfulPtmX2pressDC(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "DC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setSTORE_CARD("1")
                .build();
        checkoutPage.createOrder(orderDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
        SavedCardHelpers.validateSavedCardPresence(user);
    }

    @Test(description = "Validate successful Paytm Express transaction using CC Saved Card.", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_successfulPtmX2pressCCSavedCard(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", paymentDTO, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();


        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test(description = "Validate successful Paytm Express transaction using DC Saved Card.", dataProvider = "MerchantProvider")
    public void PGP_successfulPtmX2pressDCSavedCard(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "DC", paymentDTO, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test(description = "Validate successful Paytm Express transaction using NB.", dataProvider = "MerchantProvider")
    public void PGP_successfulPtmX2pressNB(MerchantType merchant) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "NB", user)
                .setBANK_CODE("ICICI")
                .setPAYTM_TOKEN(user.paytmToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.ICICI.toString())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Issue("PGP-15326")
    @Severity(SeverityLevel.NORMAL)
    @Test(description = "Validate successful Paytm Express transaction using Wallet.", dataProvider = "MerchantProvider", groups = {"smoke", Status.BUG})
    public void PGP_successfulPtmX2pressPPI(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "PPI", user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test(description = "Validate successful Paytm Express EMI transaction using CC.", groups = {"smoke"})
    public void PGP_successfulPtmXpressEMI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test(description = "Validate failure Paytm Express EMI transaction using CC. when plan id passed is invalid")
    public void PGP_PtmXpress_EMI_InvalidPlanID() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYTM_TOKEN(user.paytmToken())
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateRespMsg("EMI Processing Failed: Invalid Params")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        responsePage.validateStatus("");

    }

    @Test(description = "Validate success Paytm Express EMI hybrid transaction.")
    public void PGP_PtmXpress_EMI_Hybrid() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 1.0);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYTM_TOKEN(user.paytmToken())
                .setTXN_AMOUNT("10")
                .setPAYMENT_TYPE_ID("EMI")
                .setAddMoney("0")
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
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
                .validatePaymentMode(TxnStatus.ChildTxnType.BANK, "EMI")
                .validateTxnAmount(TxnStatus.ChildTxnType.BANK, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.BANK, Gateway.HDFC.toString())
                .validateBankTxnId(TxnStatus.ChildTxnType.BANK, ValidationType.NON_EMPTY)
                .validateBankName(TxnStatus.ChildTxnType.BANK, Constants.Bank.HDFC.toString())
                .validateStatus(TxnStatus.ChildTxnType.BANK, "TXN_SUCCESS");

        txnStatus.validateChildTxnPresent(TxnStatus.ChildTxnType.WALLET)
                .validateTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validatePaymentMode(TxnStatus.ChildTxnType.WALLET, "PPI")
                .validateTxnAmount(TxnStatus.ChildTxnType.WALLET, CommonHelpers.doubleToTwoDigitAfterDecimalPoint(1.0))
                .validateGatewayName(TxnStatus.ChildTxnType.WALLET, "WALLET")
                .validateBankTxnId(TxnStatus.ChildTxnType.WALLET, ValidationType.NON_EMPTY)
                .validateStatus(TxnStatus.ChildTxnType.WALLET, "TXN_SUCCESS")
                .AssertAll();
    }

    @Test(description = "Validate success Paytm Express EMI transaction using CC. when token is not passed", groups = {"smoke"})
    public void PGP_PtmXpress_EMI_Without_SSOToken() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setPAYTM_TOKEN(user.paytmToken())
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("Txn_Success")
        .assertAll();
    }

    @Test(description = "Validate failure Paytm Express EMI transaction with invalid pay mode", groups = {"smoke"})
    public void PGP_PtmXpress_EMI_InvalidPayMode() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYTM_TOKEN(user.paytmToken())
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");


    }

    @Test(description = "Validate failure Paytm Express EMI transaction for Zero cost EMI", groups = {"smoke"})
    public void PGP_PtmXpress_EMI_ZeroCost() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYMENT_TYPE_ID("EMI")
                .setEMI_OPTIONS("0CostEMI:8565560_753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateStatus("TXN_FAILURE");
    }

    @Test(description = "Validate Success Paytm Express EMI transaction also verify peon", groups = {"smoke"})
    public void PGP_PtmXpress_EMI_VerifyPeon() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYTM_TOKEN(user.paytmToken())
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        Peon peon = new Peon(orderDTO.getORDER_ID());
        peon.executeUntilGetResponse();
    }

    @Test(description = "Validate Success Paytm Express EMI transaction also verify its refund", groups = {"smoke"})
    public void PGP_PtmXpress_EMI_VerifyRefund() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYTM_TOKEN(user.paytmToken())
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        Response response = PGPHelpers.initiateRefundRequest(orderDTO.getMID(), orderDTO.getMerchantKey(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), orderDTO.getTXN_AMOUNT(), txnStatus.getResponse().getTXNID(), "");
        PGPHelpers.getRefundStatusV1(PaytmExpress_Hybrid_Onus.getId(), orderDTO.getORDER_ID(), orderDTO.getORDER_ID(), PaytmExpress_Hybrid_Onus.getKey(), true)
                .validateSuccessRefund()
                .validateRefundAmount(Double.valueOf(orderDTO.getTXN_AMOUNT()))
                .validateTotalRefundAmount(Double.valueOf(orderDTO.getTXN_AMOUNT()))
                .asserAll();
    }


    @Test(description = "Validate Paytm Express EMI transaction when emi plan is of diff bank and bin is of diff bank", groups = {"smoke"})
    public void PGP_PtmXpressEMI_diff_Bank_Bin() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setPAYTM_TOKEN(user.paytmToken())
                .setPAYMENT_TYPE_ID("EMI")
                .setTXN_AMOUNT("20")
                .setPlanId("753")
                .setBANK_CODE("SBI")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateRespMsg("EMI Processing Failed: Invalid Params")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();
        responsePage.validateStatus("");
    }

    @Test(description = "Validate successful Paytm Express PGonly with login CC Transaction", groups = {"smoke"})
    public void validatePaytmExpressPGOnlyTxnUsingCC() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }
    @Test(description = "Validate successful Paytm Express PGOnly with Login DC Transaction", groups = {"smoke"})
    public void validatePaytmExpressPGOnlyTxnUsingDC() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "DC", user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test(description = "Validate successful Paytm Express PGOnly without Login DC Transaction", groups = {"smoke"})
    public void validatePaytmExpressPGOnlyTxnUsingDCWithoutLogin() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "DC", user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test(description = "Validate successful Paytm Express PGOnly without login EMI Transaction", groups = {"smoke"})
    public void validatePaytmExpressPGOnlyTxnUsingEMIWithoutLogin() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Test(description = "Validate successful Paytm Express PGOnly without login Saved Card Transaction", groups = {"smoke"})
    public void validatePaytmExpressPGOnlyTxnUsingSavedCardWithoutLogin() throws Exception {

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers savedCard = new SavedCardHelpers();
        String saveCardId = savedCard.saveCard_custId_mId(paymentDTO.getCreditCardNumber(),user.custId(),PaytmExpress_Hybrid_Onus.getId(),paymentDTO.getExpMonth()+paymentDTO.getExpYear()).getResponse().toString();
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", paymentDTO,user)
                .build();

        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateGatewayName(Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }


    @Test(description = "Validate successful Paytm Express AddnPay CC Transaction", groups = {"smoke"})
    public void validatePaytmExpressAddAndPayTransactionUsingCC() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressAddnPay(PaytmExpress_AddnPay_Onus, "CC", user)
                .setPAYTM_TOKEN(user.paytmToken())
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("3.00")
                .setWALLET_AMOUNT("2.00")
                .setAddMoney("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("Wallet")
                .validateBankName("Wallet")
                .validateCheckSum(AddnPay.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName("Wallet")
                .validateGatewayName("Wallet")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

    @Test(description = "Validate successful Paytm Express AddnPay DC Transaction", groups = {"smoke"})
    public void validatePaytmExpressAddAndPayTransactionUsingDC() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressAddnPay(PaytmExpress_AddnPay_Onus, "DC", user)
                .setPAYTM_TOKEN(user.paytmToken())
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("3.00")
                .setWALLET_AMOUNT("2.00")
                .setAddMoney("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("Wallet")
                .validateBankName("Wallet")
                .validateCheckSum(AddnPay.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName("Wallet")
                .validateGatewayName("Wallet")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }



    @Test(description = "Validate successful Paytm Express AddnPay Saved Card Transaction", groups = {"smoke"})
    public void validatePaytmExpressAddAndPayTransactionUsingSavedCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.00);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);

        OrderDTO orderDTO = new OrderFactory.PaytmExpressAddnPay(PaytmExpress_AddnPay_Onus, "CC", user)
                .setPAYTM_TOKEN(user.paytmToken())
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT("3.00")
                .setWALLET_AMOUNT("2.00")
                .setAddMoney("1")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("Wallet")
                .validateBankName("Wallet")
                .validateCheckSum(PaytmExpress_AddnPay_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.execute();
        txnStatus.validateTxnId(ValidationType.NON_EMPTY)
                .validateBankTxnId(ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateBankName("Wallet")
                .validateGatewayName("Wallet")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateStatusAPIParameters()
                .AssertAll();
    }

  //  @Issue("PGP-23105") Issue Fixed
    @Test(description = "test txn < 1 not allowed")
    public void testTxnOfLessThan1NotAllowed() throws Exception {
        MerchantType merchant = PaytmExpress_Hybrid_Onus;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", user)
                .setTXN_AMOUNT("0.99")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                .validateStatus("TXN_FAILURE")
                .validateRespCode("308")
                .validateRespMsg("Invalid Txn Amount")
                .validateCheckSum(merchant.getKey())
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateStatus("TXN_FAILURE")
                .validateRespCode("810")
                .validateRespMsg("Payment failed due to a technical error. Please try after some time.")
                .AssertAll();
    }


    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20447")
    @Test(description = "Verify Response Message for Paytm express flow with CC for risk reject")
    public void verifyResponseMessageForRiskRejectTxnForCC() throws Exception {
        MerchantType merchant = PaytmExpress_Hybrid_Onus;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", user)
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode(riskRejectHelper.riskRejectRespCode)
                .validateRespMsg(riskRejectHelper.riskRejectRespMsg)
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .assertAll();
    }


    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20447")
    @Test(description = "Verify Response Message for Paytm express flow with DC for risk reject")
    public void verifyResponseMessageForRiskRejectTxnForDC() throws Exception {
        MerchantType merchant = PaytmExpress_Hybrid_Onus;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "DC", user)
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode(riskRejectHelper.riskRejectRespCode)
                .validateRespMsg(riskRejectHelper.riskRejectRespMsg)
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .assertAll();
    }


    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20447")
    @Test(description = "Verify Response Message for Paytm express flow with NB for risk reject")
    public void verifyResponseMessageForRiskRejectTxnForNB() throws Exception {
        MerchantType merchant = PaytmExpress_Hybrid_Onus;
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "NB", user)
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode(riskRejectHelper.riskRejectRespCode)
                .validateRespMsg(riskRejectHelper.riskRejectRespMsg)
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .assertAll();
    }




    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20447")
    @Test(description = "Verify Response Message for Paytm express flow with PPI for risk reject")
    public void verifyResponseMessageForRiskRejectTxnForPPI() throws Exception {
        MerchantType merchant = PaytmExpress_Hybrid_Onus;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,2.00);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "PPI", user)
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode(riskRejectHelper.riskRejectRespCode)
                .validateRespMsg(riskRejectHelper.riskRejectRespMsg)
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .assertAll();
    }



    @Epic(Constants.Sprint.SPRINT31_2)
    @Story("PGP-20447")
    @Test(description = "Verify Response Message for Paytm express flow with EMI for risk reject")
    public void verifyResponseMessageForRiskRejectTxnForEMI() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setTXN_AMOUNT(riskRejectHelper.riskAmount)
                .setPAYMENT_TYPE_ID("EMI")
                .setPlanId("753")
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode(riskRejectHelper.riskRejectRespCode)
                .validateRespMsg(riskRejectHelper.riskRejectRespMsg)
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .assertAll();


    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate successful Paytm Express transaction using CC and hit the routing engine", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_successfulPtmX2pressCC1(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(PGPBaseTest.Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate card getting saved for successful Paytm Express transaction using CC and hit the routing engine", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_verifyCardGettingSavedForsuccessfulPtmX2pressCC1(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setSTORE_CARD("1")
                .build();
        checkoutPage.createOrder(orderDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate successful Paytm Express transaction using DC and card not getting saved and hit the routing engine", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_successfulPtmX2pressDC1(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "DC", user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate card getting saved for successful Paytm Express transaction using DC and hit the routing engine", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_verifyCardGettingSavedForsuccessfulPtmX2pressDC1(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "DC", user)
                .setSSO_TOKEN(user.ssoToken())
                .setSTORE_CARD("1")
                .build();
        checkoutPage.createOrder(orderDTO);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate successful Paytm Express transaction using CC Saved Card and hit the routing engine", dataProvider = "MerchantProvider", groups = {"smoke"})
    public void PGP_successfulPtmX2pressCCSavedCard1(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "CC", paymentDTO, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate successful Paytm Express transaction using DC Saved Card and hit the routing engine", dataProvider = "MerchantProvider")
    public void PGP_successfulPtmX2pressDCSavedCard1(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "DC", paymentDTO, user)
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate successful Paytm Express transaction using NB and hit the routing engine", dataProvider = "MerchantProvider")
    public void PGP_successfulPtmX2pressNB1(MerchantType merchant) throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "NB", user)
                .setBANK_CODE("ICICI")
                .setPAYTM_TOKEN(user.paytmToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.ICICI.toString())
                .validateBankName(Constants.Bank.ICICI.toString())
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Severity(SeverityLevel.NORMAL)
    @Test(description = "Validate successful Paytm Express transaction using Wallet and hit the routing engine", dataProvider = "MerchantProvider", groups = {"smoke", Status.BUG})
    public void PGP_successfulPtmX2pressPPI1(MerchantType merchant) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user, 2.0);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(merchant, "PPI", user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName("WALLET")
                .validateBankName("WALLET")
                .validateCheckSum(merchant.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Validate successful Paytm Express PGonly with login CC Transaction and hit the routing engine", groups = {"smoke"})
    public void validatePaytmExpressPGOnlyTxnUsingCC1() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.PaytmExpressPGOnly(PaytmExpress_Hybrid_Onus, "CC", user)
                .setSSO_TOKEN(user.ssoToken())
                .build();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(ValidationType.NON_EMPTY)
                .validateGatewayName(Gateway.HDFC.toString())
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateCheckSum(PaytmExpress_Hybrid_Onus.getKey())
                .validateResponsePageParameters()
                .assertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

}

