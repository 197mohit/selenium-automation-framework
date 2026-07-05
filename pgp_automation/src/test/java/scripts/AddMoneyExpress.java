package scripts;

import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.ValidationType;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Owners;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.GVConsentPage;
import com.paytm.pages.OopsPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.Peon;
import groovy.json.JsonSlurper;
import io.qameta.allure.*;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;
import java.util.Map;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;
import static com.paytm.base.test.Group.Status;
import static com.paytm.base.test.Group.Status.BUG;
import static com.paytm.pages.responsePage.ResponsePage.Attribute;

/**
 * Created by anjukumari on 18/03/19
 */
@Owner("Deepak")
public class AddMoneyExpress extends PGPBaseTest implements IAddMoney{

    @Test(description = "Verify failed express addmoney txn when user addmoney limit is already breached", groups = {"regression", Status.TO_BE_FIXED})
    public void Failed_AddMoney_BreachLimit() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.breachLimitByAddMoney(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"DC", user, new PaymentDTO()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        new OopsPage().waitUntilLoads();
    }

    @Test(description = "Verify successful express addmoney txn when payment is done via CC", groups = {"smoke", "regression"})
    public void successfulAddMoneyCC() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())  //merchant has HDFC CC acquiring there is no ICICI acquiring
                .validateBankName("ICICI Bank")
                .validateCheckSum(Constants.MerchantType.AddMoney.getKey())
                .validateResponsePageParameters()
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName("ICICI Bank")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Test(description = "Verify successful express addmoney txn when payment is done via DC")
    public void successfulAddMoneyDC() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"DC", user, new PaymentDTO()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }


    @Test(description = "Verify successful express addmoney txn when payment is done via mastero card", groups = {"regression"})
    public void successfulAddMoney_Maestro() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"DC", user, paymentDTO).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Test(description = "Verify successful express addmoney txn when payment is done via saved hdfc card", groups = {"regression"})
    public void successfulAddMoney_UsingSavedCard() throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = new SavedCardHelpers().getSaveCardDetails_userId(user).getResponseInSaveCardResponseList().get(0).getCardId().toString();
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory
                .AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO)
                .setIsSaveCard("1")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
    }

    @Test(description = "Verify successful express addmoney txn, Also validate card is saved when store card=1", groups = {"regression", "sanity"})
    public void successfulAddMoney_CheckForSaveCard() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory
                .AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, new PaymentDTO())
                .setSTORE_CARD("1")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        String cardId = new SavedCardHelpers().getSaveCardDetails_userId(user)
                .getResponseInSaveCardResponseList().get(0)
                .getCardId().toString();
        Assertions.assertThat(cardId).withFailMessage("Card not saved on user").isNotEmpty();
        SavedCardHelpers.validateSaveCardDB_ByCardID(cardId);
    }


    @Test(description = "Verify successful express addmoney txn, Also validate card is not saved when STORE_CARD= 0", groups = {"regression"})
    public void successfulAddMoney_CheckCardNotSaved() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory
                .AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD))
                .setSTORE_CARD("0")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();
        WalletHelpers.validateBalance(user, Double.valueOf(orderDTO.getTXN_AMOUNT()));
        Assertions.assertThat(new SavedCardHelpers().getSaveCardDetails_userId(user)
                .getResponseInSaveCardResponseList().size()).withFailMessage("Card should not be saved on user, But we found card saved on user").isZero();
    }
    @Test(description = "Verify failure express addmoney txn when payment is done via saved hdfc card but is_SAVED_CARD=0", groups = {"regression"})
    public void Fail_AddMoney_WithSaveCard(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = new SavedCardHelpers().getSaveCardDetails_userId(user).getResponseInSaveCardResponseList().get(0).getCardId().toString();
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory
                .AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO)
                .setTHEME(theme)
                .setIsSaveCard("0")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        new OopsPage().waitUntilLoads();
    }


    @Test(description = "Verify failed express addmoney txn when payment is done via incorrect CardDetails", groups = {"regression"})
    public void Failed_AddMoney_incorrectCard(@Optional("enhancedwap_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(PaymentDTO.INVALID_CARD);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "DC", user, paymentDTO).setTHEME(theme).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        new OopsPage().waitUntilLoads();
    }

    @Test(description = "Verify failed express addmoney txn when invalid encrypted card detail is send", groups = {"regression"})
    public void Failed_AddMoney_InvalidEncryption(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"DC", user, new PaymentDTO())
                .setTHEME(theme).build();
        orderDTO.setPAYMENT_DETAILS("abc");
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        new OopsPage().waitUntilLoads();
    }

    @Issue("PGP-21295")
    @Test(description = "test txn < 1 not allowed", groups = {BUG})
    public void testTxnOfLessThan1NotAllowed() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        MerchantType merchant = MerchantType.AddMoney;
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(merchant, "CC", user, new PaymentDTO())
                .setTXN_AMOUNT("0.99")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
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
    @Issue("PGP-21295")
    @Test(description = "Verify Response Message for Paytm express flow with DC for risk reject", groups = {BUG})
    public void verifyResponseMessageForRiskRejectTxnForCC() throws Exception {
        User user = userManager.getForWrite(Label.RISKREJECT);
        MerchantType merchant = MerchantType.AddMoney;
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(merchant, "CC", user, new PaymentDTO())
                .setTXN_AMOUNT("1.88")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("501")
                .validateRespMsg("global default message")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateCheckSum(merchant.getKey())
                .assertAll();
    }

   /* @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test(enabled = false) */
    //TODO disabling it because as per Nitin this feature is not supported for AddMoneyExpress as of now but may be supported in Phase 2 which is under development
    public void testCardIndexNoIsPresentInOutputWhenCardTokenRequiredIsTrueInOrder() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO order = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "CC", user, new PaymentDTO())
                .setCardTokenRequired(true)
                .build();
        new CheckoutPage().createOrder(order);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        assertion.apply(responsePage.keys().contains(Attribute.CARD_INDEX_NO));

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NON_EMPTY).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(Attribute.CARD_INDEX_NO));
    }

  /*  @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test(enabled = false) */
    //TODO disabling it because as per Nitin this feature is not supported for AddMoneyExpress as of now but may be supported in Phase 2 which is under development
    public void testCardIndexNoIsAbsentInOutputWhenCardTokenRequiredIsFalseInOrder() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO order = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "CC", user, new PaymentDTO())
                .setCardTokenRequired(false)
                .build();
        new CheckoutPage().createOrder(order);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        assertion.apply(responsePage.keys().contains(Attribute.CARD_INDEX_NO).not());

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NOT_PRESENT).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(Attribute.CARD_INDEX_NO).not());
    }

  /*  @Owners(author = "Deepak", qa = "Nitin Sharma")
    @Owner("Deepak")
    @Epic(Constants.Sprint.SPRINT32_1)
    @Story("PGP-21604")
    @Test(enabled = false) */
    //TODO disabling it because as per Nitin this feature is not supported for AddMoneyExpress as of now but may be supported in Phase 2 which is under development
    public void testCardIndexNoIsAbsentInOutputWhenCardTokenRequiredParamIsNotPassedInOrder() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO order = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "CC", user, new PaymentDTO())
                .build();
        new CheckoutPage().createOrder(order);

        com.paytm.pages.responsePage.ResponsePage responsePage = new com.paytm.pages.responsePage.ResponsePage();
        assertion.apply(pageWait.apply(responsePage.hasLoaded()));
        assertion.apply(responsePage.keys().contains(Attribute.CARD_INDEX_NO).not());

        TxnStatus txnStatus = new TxnStatus(order.getMID(), order.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateCardIndexNo(ValidationType.NOT_PRESENT).AssertAll();

        assertion.apply(peonWait.apply(() -> peons.getAt(order.getORDER_ID()) != null));
        Peon peon = peons.getAt(order.getORDER_ID());
        assertion.apply(peon.keys().contains(Attribute.CARD_INDEX_NO).not());
    }


    ////////////Test cases /////////////


  /*  @Override
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Test(enabled = false) */
    public void validateFullKYCWalletCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.LOGIN);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, new PaymentDTO())
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }



   /* @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateFullKYCWalletSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10001";
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);

        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .setIsSaveCard("1")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

   // @Test(enabled = false)
   // @Override
    public void validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //Already Covered
    }

 /*   @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10001";
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);

        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

 /*   @Test(enabled = false)
    @Override */
    public void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
       //No need
    }

  /*  @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10001";
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.BASICTOKYC);

        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

  /*  @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10001";
        PaymentDTO paymentDTO = new PaymentDTO();
        User user = userManager.getForWrite(Label.LOGIN);

        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

 /*   @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.LOGIN);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        String saveCardId = SavedCardHelpers.getSavedCardId(user, 0);
        paymentDTO.setSavedCardId(saveCardId);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .setIsSaveCard("1")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

 /*   @Test(enabled = false)
    @Override */
    public void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //Not required
    }

  /*  @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user,paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .setIsSaveCard("1")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }

  /*  @Test(enabled = false)
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Issue("PGP-24839")
    @Override */
    public void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String goodsInfo = "[  {  \"merchantGoodsId\":\"154435058\",\"merchantShippingId\":\"564314314574327545\",  \"snapshotUrl\":\"http://snap.url.com\",\"description\":\"Women Summer Dress New White Lace Sleeveless\",  \"category\":\"travelling/subway\",  \"quantity\":\"3.2\",  \"unit\":\"Kg\",  \"price\":{  \"currency\":\"INR\", \"value\":\"1\"  },  \"extendInfo\":{ \"udf1\":\"ajay\", \"udf2\":\"ajay\",\"udf3\":\"ajay\", \"udf4\":\"ajay\", \"udf5\":\"ajay\" }} ]";
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user,paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .setGoodsInfo(goodsInfo)
                .setIsSaveCard("1")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(txnAmount.toString())
                .assertAll();
    }


 /*   @Test(enabled = false)
    @Override */
    public void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        //No need
    }

 /*   @Test(enabled = false)
    @Override */
    public void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
       //No need
    }

    @Test
    @Owner("Tarun")
    @Feature("PGP-19696")
    @Override
    public void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"DC", user,paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28964")
    @Description("Headers in UIMicroservices api")
    @Parameters({"theme"})
    @Test(description = "validate uiMicroservice Encoded data to theia When Mid Present In FF4J Flag")
    public void uiMicroserviceEncodedWhenMidPresentInFF4JFlag() throws Exception {
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"DC", user,paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

    @Owner(GAGANDEEP)
    @Feature("PGP-28964")
    @Description("Headers in UIMicroservices api")
    @Parameters({"theme"})
    @Test(description = "validate uiMicroservice Encoded data to theia When Mid Present In FF4J Flag")

    public void uiMicroserviceEncodedWhenMidIsNotPresentInFF4JFlag() throws Exception {
        String txnAmount = "10001";
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(MerchantType.ADD_MONEY_ONLY,"DC", user,paymentDTO)
                .setTXN_AMOUNT(txnAmount)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(txnAmount)
                .assertAll();
    }

    @Owner(DEEPAK)
    @Feature("PGP-22739")
    @Parameters({"theme"})
    @Test(description = "test pay mode is forwarded to Alipay in Payment Cashier Pay API for CC txn")
    public void testPayModeIsForwardedToAlipayInPaymentCashierPayForCCTxn() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"CC", user, new PaymentDTO()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body("RESPCODE", Matchers.equalTo("01"));
        new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .executeUntilNotPending()
                .validateRespCode("01");
        String cmdToFetchSendOTPRequest = "grep '" + orderDTO.getORDER_ID() + "' /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST'";
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest);
        Map map = (Map) new JsonSlurper().parseText(theiaFacadelogs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).as("").contains("\"paymentMode\":\"CC\"");
    }

    @Owner(DEEPAK)
    @Feature("PGP-22739")
    @Parameters({"theme"})
    @Test(description = "test pay mode is forwarded to Alipay in Payment Cashier Pay API for DC txn")
    public void testPayModeIsForwardedToAlipayInPaymentCashierPayForDCTxn() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney,"DC", user, new PaymentDTO()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        super.merchantCallback(orderDTO.getORDER_ID())
                .body("RESPCODE", Matchers.equalTo("01"));
        new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                .executeUntilNotPending()
                .validateRespCode("01");
        String cmdToFetchSendOTPRequest = "grep '" + orderDTO.getORDER_ID() + "' /paytm/logs/theia_facade.log | grep 'ACQUIRING_PAY_ORDER' | grep 'REQUEST'";
        String theiaFacadelogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, cmdToFetchSendOTPRequest);
        Map map = (Map) new JsonSlurper().parseText(theiaFacadelogs);
        String extendInfo = ((String) ((Map) ((Map) ((Map) map.get("REQUEST")).get("request")).get("body")).get("extendInfo"));
        Assertions.assertThat(extendInfo).as("").contains("\"paymentMode\":\"DC\"");
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Verify successful express addmoney txn when payment is done via CC and hit the routing engine")
    public void successfulAddMoneyCC1() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "CC", user, paymentDTO.setCreditCardNumber(PaymentDTO.ICICI_CREDIT_CARD_NUMBER)).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
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
    @Test(description = "Verify successful express addmoney txn when payment is done via DC and hit routing engine")
    public void successfulAddMoneyDC1() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "DC", user, new PaymentDTO()).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateBankName(Constants.Bank.HDFC.toString())
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner("Abhishek Gupta")
    @Feature("PGP-36748")
    @Test(description = "Verify successful express addmoney txn when payment is done via mastero card and hit Routing engine")
    public void successfulAddMoney_Maestro1() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setDebitCardNumber(paymentDTO.getDebitCardNumber());
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.AddMoney, "DC", user, paymentDTO).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        WalletHelpers.setZeroBalance(user);
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("ADDMONEY")
                .validateGatewayName(Constants.Gateway.HDFC.toString())
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .AssertAll();

        String grepcmdRoutingEngineHit = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Received Hit To Process Routing Engine\" "   ;
        String theialogsrequest = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit);
        Assertions.assertThat(theialogsrequest).contains(orderDTO.getMID());

        String grepcmdRoutingEngineHit1 = "grep \"" + orderDTO.getMID() + "\"  /paytm/logs/theia.log |grep \"Response returned from routing Engine\" "   ;
        String theialogsresponse = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmdRoutingEngineHit1);
        Assertions.assertThat(theialogsresponse).contains(orderDTO.getMID());
    }

    @Owner(MAYURI)
    @Feature("PGP-46159")
    @Test(description = "Verify prepaid cc card is not allowed for express addmoney txn, mid is present for ff4j flag theia.blockPrepaidCardInExpressAddMoneyFlow ")
    public void ccBlockforExpressAddMoney() throws Exception {
        User user = userManager.getForWrite(Label.BASIC);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.ADD_MONEY_EXPRESS_HOTFIX,"CC", user, paymentDTO.setCreditCardNumber(PaymentDTO.PREPAID_CARD_HOTFIX)).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);

        String log_text = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia, orderDTO.getORDER_ID(),"Prepaid Card is not allowed for this transaction, kindly use some other payment mode");
        String assertion_text="Prepaid Card is not allowed for this transaction, kindly use some other payment mode";
        Assertions.assertThat(log_text).contains(assertion_text);

    }

    @Owner(MAYURI)
    @Feature("PGP-46159")
    @Test(description = "Verify successful txn for express addmoney txn via prepaid CC, mid is absent for ff4j flag theia.blockPrepaidCardInExpressAddMoneyFlow ")
    public void successfulAddMoneyExpressCC() throws Exception {
        User user = userManager.getForWrite(Label.PPBL);
        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.AddMoneyExpress(Constants.MerchantType.ADD_MONEY_EXPRESS_HOTFIX_SUCCESS,"CC", user, paymentDTO.setCreditCardNumber(PaymentDTO.PREPAID_CARD_HOTFIX)).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createOrder(orderDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(orderDTO.getTXN_AMOUNT()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Gateway.HDFC.toString())  //merchant has HDFC CC acquiring there is no ICICI acquiring
                .validateBankName("HDFC Bank")
                .assertAll();

    }
}


