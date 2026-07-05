package com.paytm.RetryPaymode;

import com.paytm.api.TxnStatus;
import com.paytm.api.ppbl.Ppbl;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PpblDTO.PpblAddMoneyDTO;
import com.paytm.pages.*;
import org.testng.Assert;

import java.util.Date;

public class RetryPaymentMode extends PGPBaseTest {
    private final double txn_amount = 2000;
    private User user;
    private PaymentDTO CorrectSavedCard;
    private PaymentDTO IncorrectSavedCard;


    public OrderDTO FirstPayMode(String Paymode, String theme) throws Exception {
        OrderDTO orderDTO = null;
        CheckoutPage checkoutPage = new CheckoutPage();
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        if (!(Paymode.equalsIgnoreCase("HYBRID") || Paymode.equalsIgnoreCase("ADDNPAY") || Paymode.contains("NB")|| Paymode.contains("DIRECT_BANK") || Paymode.contains("PPBL") || Paymode.equalsIgnoreCase("ZESTMONEY"))) {
            user = userManager.getForWrite(Label.BASIC,Label.LOGIN,Label.PPBL,Label.POSTPAID);
            SavedCardHelpers.deleteSavedCard(user);
            CorrectSavedCard = new PaymentDTO();
            IncorrectSavedCard = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
            SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
            if(! PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER.equals(CorrectSavedCard.getCreditCardNumber())){
                SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
            }
            SavedCardHelpers.addCard(user, IncorrectSavedCard.getExpMonth(), IncorrectSavedCard.getExpYear(), IncorrectSavedCard.getCreditCardNumber());
            WalletHelpers.modifyBalance(user, txn_amount);
            PostpaidHelpers.updateBalance("2000");
            orderDTO = new OrderFactory.COD(Constants.MerchantType.PGOnly_Retry, theme, user)
                    .setTXN_AMOUNT(toString().valueOf(txn_amount))
                    .setORDER_ID("theia_123"+ CommonHelpers.generateOrderId()).build();
            checkoutPage.createOrder(orderDTO);
        }
        switch (Paymode) {
            case "CC":
                PaymentDTO CreditCard = new PaymentDTO()
                        .setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                cashierPage.payBy(Constants.PayMode.CC, CreditCard);
                break;
            case "DC":
                PaymentDTO Debitcard = new PaymentDTO()
                        .setDebitCardNumber(PaymentDTO.DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME);
                cashierPage.payBy(Constants.PayMode.DC, Debitcard);
                break;
            case "NB":
                user = userManager.getForWrite(Label.BASIC,Label.LOGIN,Label.PPBL,Label.POSTPAID);
                SavedCardHelpers.deleteSavedCard(user);
                CorrectSavedCard = new PaymentDTO();
                IncorrectSavedCard = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
                SavedCardHelpers.addCard(user, IncorrectSavedCard.getExpMonth(), IncorrectSavedCard.getExpYear(), IncorrectSavedCard.getCreditCardNumber());
                WalletHelpers.modifyBalance(user, txn_amount);
                orderDTO = new OrderFactory.COD(Constants.MerchantType.PGOnly_Retry, theme, user)
                        .setORDER_ID(CommonHelpers.generateOrderId() + "RETRY")
                        .setTXN_AMOUNT(toString().valueOf(txn_amount)).build();
                checkoutPage.createOrder(orderDTO);
                cashierPage = CashierPageFactory.getCashierPage(theme);
                PaymentDTO NbICICIbank = new PaymentDTO().setBankName("ICICI");
                cashierPage.payBy(Constants.PayMode.NB, NbICICIbank);
                break;
            case "PPBL":
                user = userManager.getForWrite(Label.BASIC,Label.LOGIN,Label.PPBL,Label.POSTPAID);
                SavedCardHelpers.deleteSavedCard(user);
                CorrectSavedCard = new PaymentDTO();
                IncorrectSavedCard = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
                SavedCardHelpers.addCard(user, IncorrectSavedCard.getExpMonth(), IncorrectSavedCard.getExpYear(), IncorrectSavedCard.getCreditCardNumber());
                WalletHelpers.modifyBalance(user, txn_amount);
                orderDTO = new OrderFactory.COD(Constants.MerchantType.PGOnly_Retry, theme, user)
                        .setORDER_ID(CommonHelpers.generateOrderId() + "RETRYB")
                        .setTXN_AMOUNT(toString().valueOf(txn_amount)).build();
                WalletHelpers.modifyBalance(user, txn_amount);
                checkoutPage.createOrder(orderDTO);
                cashierPage.payBy(Constants.PayMode.PPBL);
                break;
            case "EMI":
                IncorrectSavedCard = new PaymentDTO()
                        .setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN)
                        .setEmiCard(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN)
                        .setBankName("HDFC Bank")
                        .setMonth(6);
                cashierPage.payBy(Constants.PayMode.EMI, IncorrectSavedCard, IncorrectSavedCard.getCreditCardNumber());
                break;
            case "SAVEDCARD":
                cashierPage.payBy(Constants.PayMode.SAVED_CARD, IncorrectSavedCard, IncorrectSavedCard.getCreditCardNumber());
                break;
            case "UPI":
                PaymentDTO incorrectVPA = new PaymentDTO();
                incorrectVPA.setVpa(PaymentDTO.Failedvpa);
                cashierPage.payBy(Constants.PayMode.UPI, incorrectVPA);
                break;
            case "EMI_SAVEDCARD":
                cashierPage.payBy(Constants.PayMode.EMI_SAVED_CARD, IncorrectSavedCard, IncorrectSavedCard.getCreditCardNumber());
                break;

            case "EMI_DC":
                PaymentDTO Emi_Dc = new PaymentDTO();
                Emi_Dc.setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME).setBankName("ICICI Bank Debit Card").setMonth(3);
                cashierPage.payBy(Constants.PayMode.EMI, Emi_Dc);
                break;

            case "ADDNPAY":
                user = userManager.getForWrite(Label.BASIC,Label.LOGIN,Label.PPBL,Label.POSTPAID);
                orderDTO = new OrderFactory.AddnPay(Constants.MerchantType.ADDNPAY_RETRYCASES, theme)
                        .setTXN_AMOUNT(toString().valueOf(txn_amount))
                        .setSSO_TOKEN(user.ssoToken())
                        .build();
                WalletHelpers.modifyBalance(user, 1.0);
                SavedCardHelpers.deleteSavedCard(user);
                CorrectSavedCard = new PaymentDTO();
                IncorrectSavedCard = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
                SavedCardHelpers.addCard(user, IncorrectSavedCard.getExpMonth(), IncorrectSavedCard.getExpYear(), IncorrectSavedCard.getCreditCardNumber());
                checkoutPage.createOrder(orderDTO);
                cashierPage = CashierPageFactory.getCashierPage(theme);
                PaymentDTO addNpay = new PaymentDTO()
                        .setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                cashierPage.payBy(Constants.PayMode.CC, addNpay);
                break;

            case "HYBRID":
                user = userManager.getForWrite(Label.BASIC,Label.LOGIN,Label.PPBL,Label.POSTPAID);
                orderDTO = new OrderFactory.Hybrid(Constants.MerchantType.Hybrid_Retry, theme, user)
                        .setTXN_AMOUNT(String.valueOf(txn_amount)).build();
                double amountToBeRetainedInWallet = Double.valueOf(orderDTO.getTXN_AMOUNT()) - 1.00;
                WalletHelpers.modifyBalance(user, amountToBeRetainedInWallet);
                SavedCardHelpers.deleteSavedCard(user);
                CorrectSavedCard = new PaymentDTO();
                IncorrectSavedCard = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
                SavedCardHelpers.addCard(user, IncorrectSavedCard.getExpMonth(), IncorrectSavedCard.getExpYear(), IncorrectSavedCard.getCreditCardNumber());
                checkoutPage.createOrder(orderDTO);
                cashierPage = CashierPageFactory.getCashierPage(theme);
                PaymentDTO hybrid = new PaymentDTO()
                        .setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                cashierPage.payBy(Constants.PayMode.CC, hybrid);
                break;

            case "DIRECT_BANK":
                user = userManager.getForWrite(Label.BASIC,Label.LOGIN,Label.PPBL,Label.POSTPAID);
                SavedCardHelpers.deleteSavedCard(user);
                CorrectSavedCard = new PaymentDTO();
                IncorrectSavedCard = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
                SavedCardHelpers.addCard(user, IncorrectSavedCard.getExpMonth(), IncorrectSavedCard.getExpYear(), IncorrectSavedCard.getCreditCardNumber());
                WalletHelpers.modifyBalance(user, txn_amount);
                orderDTO = new OrderFactory.COD(Constants.MerchantType.ICIO_CC_Enabled_Merchant_Retry, theme, user)
                        .setTXN_AMOUNT(toString().valueOf(txn_amount)).build();
                checkoutPage.createOrder(orderDTO);
                PaymentDTO directbank = new PaymentDTO();
                directbank.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
                cashierPage = CashierPageFactory.getCashierPage(theme);
                cashierPage.payBy(Constants.PayMode.CC, directbank);
                DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
                directBankOTPPage.submitOtp("111111");
                break;

            case "ZESTMONEY":
                user = userManager.getForWrite(Label.BASIC,Label.LOGIN,Label.PPBL,Label.POSTPAID);
                SavedCardHelpers.deleteSavedCard(user);
                CorrectSavedCard = new PaymentDTO();
                IncorrectSavedCard = new PaymentDTO().setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN);
                SavedCardHelpers.addCard(user, CorrectSavedCard.getExpMonth(), CorrectSavedCard.getExpYear(), CorrectSavedCard.getCreditCardNumber());
                SavedCardHelpers.addCard(user, IncorrectSavedCard.getExpMonth(), IncorrectSavedCard.getExpYear(), IncorrectSavedCard.getCreditCardNumber());
                WalletHelpers.modifyBalance(user, txn_amount);
                ZestBankUI ui = new ZestBankUI();
                orderDTO = new OrderFactory.COD(Constants.MerchantType.PGOnly_Retry, theme, user)
                        .setTXN_AMOUNT(toString().valueOf(txn_amount)).build();
                checkoutPage.createOrder(orderDTO);
                cashierPage = CashierPageFactory.getCashierPage(theme);
                PaymentDTO zest = new PaymentDTO()
                        .setBankName("ZEST");
                cashierPage.payBy(Constants.PayMode.ZEST, zest);
                ui.failZestTxn();
                break;

            default:
                throw new RuntimeException("Invalid First Payment Mode");

        }
        cashierPage.waitUntilLoads();

        //Updating as Button Text has changed
        cashierPage.scrollTo(0);
        cashierPage.ErrorRetryButton().waitUntilVisible();
        cashierPage.ErrorRetryButton().click();

        switch (Paymode){
            case "PPBL":
                break;
            case "SAVEDCARD":
                break;
            case "EMI_SAVEDCARD":
                break;
            default:
                cashierPage.closeCcDcDetailBtn().click();
        }
//        if(cashierPage.viewAllArrow().isDisplayed())
//            cashierPage.viewAllArrow().click();
        if (Paymode.equalsIgnoreCase("HYBRID"))
            cashierPage.checkBoxPPI().click();
        if ("enhancedwap".equalsIgnoreCase(theme))
            cashierPage.modalRetryPayment().accept();
        return orderDTO;
    }


    public void RetryPayMode(String Paymode, String theme, OrderDTO orderDTO, String firstpaymode) throws Exception {

        switch (Paymode) {

            case "CC":
                if (firstpaymode.equalsIgnoreCase("DIRECT_BANK"))
                    throw new RuntimeException("Need to Run it manually");
                else {
                    CashierPage cashierPageCC = CashierPageFactory.getCashierPage(theme);
                    cashierPageCC.waitUntilLoads();
                    PaymentDTO CCCard= new PaymentDTO();
                    cashierPageCC.tabCreditCard().click();
                    cashierPageCC.payBy(Constants.PayMode.CC,CCCard.setCreditCardNumber("4893771000362085"));
                    cashierPageCC.waitUntilLoads();
                    ResponsePage responsePageCC = new ResponsePage();
                    responsePageCC.waitUntilLoads();
                    if(firstpaymode.equalsIgnoreCase("ADDNPAY"))
                        AddNPayValidation(orderDTO);
                    else
                    responsePageCC.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateCurrency("INR")
                            .validateMid(orderDTO.getMID())
                            .validateOrderId(orderDTO.getORDER_ID())
                            .validatePaymentMode("CC")
                            .validateRespCode("01")
                            .validateRespMsg("Txn Success")
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateTxnDate(new Date())
                            .validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateGatewayName(Constants.Gateway.HDFC.toString())
                            .validateBankName(Constants.Bank.HDFC.toString())
                            .assertAll();
                }
                break;
            case "DC":
                CashierPage cashierPageDC = CashierPageFactory.getCashierPage(theme);
                cashierPageDC.tabDebitCard().click();
                cashierPageDC.payBy(Constants.PayMode.DC);
                cashierPageDC.waitUntilLoads();
                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    AddNPayValidation(orderDTO);
                }
                else {

                    ResponsePage responsePageDC = new ResponsePage();
                    responsePageDC.waitUntilLoads();
                    responsePageDC.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateCurrency("INR")
                            .validateMid(orderDTO.getMID())
                            .validateOrderId(orderDTO.getORDER_ID())
                            .validatePaymentMode("DC")
                            .validateRespCode("01")
                            .validateRespMsg("Txn Success")
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateTxnDate(new Date())
                            .validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateGatewayName(Constants.Gateway.HDFC.toString())
                            .validateBankName(Constants.Bank.HDFC.toString())
                            .assertAll();
                }
                break;
            case "WALLET":
                CashierPage cashierPagePPI = CashierPageFactory.getCashierPage(theme);
//                cashierPagePPI.checkBoxPPI().waitUntilClickable();
                cashierPagePPI.checkBoxPPI().check();
                cashierPagePPI.payBy(Constants.PayMode.WALLET);
                cashierPagePPI.waitUntilLoads();
                ResponsePage responsePagePPI = new ResponsePage();
                responsePagePPI.waitUntilLoads();
                responsePagePPI.validateCurrency("INR")
                        .validateGatewayName("WALLET")
                        .validateRespMsg("Txn Success")
                        .validateBankName("WALLET")
                        .validatePaymentMode("PPI")
                        .validateMid(orderDTO.getMID())
                        .validateRespCode("01")
                        .validateTxnId(Constants.ValidationType.NON_EMPTY)
                        .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                        .validateOrderId(orderDTO.getORDER_ID())
                        .validateStatus("TXN_SUCCESS")
                        .assertAll();
                break;
            case "NB":
                CashierPage cashierPageNB = CashierPageFactory.getCashierPage(theme);
                cashierPageNB.tabNetBanking().click();
                PaymentDTO nbCredentials = new PaymentDTO()
                        .setBankName("ICICI");
                cashierPageNB.payBy(Constants.PayMode.NB, nbCredentials);
                cashierPageNB.waitUntilLoads();
                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    AddNPayValidation(orderDTO);

                } else {
                    ResponsePage responsePageNB = new ResponsePage();
                    responsePageNB.waitUntilLoads();
                    TxnStatus txnStatusNB = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    txnStatusNB.executeUntilNotPending();
                    txnStatusNB.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateGatewayName(Constants.Gateway.ICICI.toString())
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateBankName(Constants.Bank.ICICINB.toString())
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("NB")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;
            case "PPBL":
                CashierPage cashierPagePPBL = CashierPageFactory.getCashierPage(theme);
                cashierPagePPBL.tabNetBanking().click();
                PaymentDTO PPBL = new PaymentDTO()
                        .setBankName("PPBL");
                cashierPagePPBL.payBy(Constants.PayMode.PPBL, PPBL);
                cashierPagePPBL.waitUntilLoads();
                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    AddNPayValidation(orderDTO);

                } else {
                    ResponsePage responsePagePPBL = new ResponsePage();
                    responsePagePPBL.waitUntilLoads();
                    responsePagePPBL.waitUntilLoads();
                    TxnStatus txnStatusPPBL = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    txnStatusPPBL.executeUntilNotPending();
                    txnStatusPPBL.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateGatewayName("PPBL")
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateBankName("PPBL")
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("NB")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;
            case "EMI":
                if (firstpaymode.equalsIgnoreCase("DIRECT_BANK"))
                    throw new RuntimeException("Need to Run it manually");
                else {
                CashierPage cashierPageEMI = CashierPageFactory.getCashierPage(theme);
                cashierPageEMI.tabEMI().click();
                    CorrectSavedCard.setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                cashierPageEMI.payBy(Constants.PayMode.EMI, CorrectSavedCard, PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                cashierPageEMI.waitUntilLoads();
                    ResponsePage responsePageEMI = new ResponsePage();
                    responsePageEMI.waitUntilLoads();
                    TxnStatus txnStatusEMI = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    txnStatusEMI.executeUntilNotPending();
                    txnStatusEMI.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateGatewayName(Constants.Gateway.HDFC.toString())
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateBankName(Constants.Bank.HDFC.toString())
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("EMI")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;
            case "COD":
                CashierPage cashierPageCOD = CashierPageFactory.getCashierPage(theme);
                cashierPageCOD.payBy(Constants.PayMode.COD);
                cashierPageCOD.waitUntilLoads();
                ResponsePage responsePageCOD = new ResponsePage();
                responsePageCOD.waitUntilLoads();
                responsePageCOD.validateCurrency("INR")
                        .validateGatewayName("CODMOCK")
                        .validateRespMsg("Txn Success")
                        .validateBankName("CODMOCK")
                        .validatePaymentMode("COD")
                        .validateMid(orderDTO.getMID())
                        .validateRespCode("01")
                        .validateTxnId(Constants.ValidationType.NON_EMPTY)
                        .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                        .validateOrderId(orderDTO.getORDER_ID())
                        .validateStatus("TXN_SUCCESS")
                        .assertAll();
                responsePageCOD.waitUntilLoads();
                PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID())
                        .validateTxnId(Constants.ValidationType.NON_EMPTY)
                        .validateBankTxnId(Constants.ValidationType.EMPTY)
                        .validateOrderid(orderDTO.getORDER_ID())
                        .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                        .validateStatus("TXN_SUCCESS")
                        .validateTxnType("SALE")
                        .validateGatewayName("CODMOCK")
                        .validateRespCode("01")
                        .validateRespMsg("Txn Success")
                        .validateMid(orderDTO.getMID())
                        .validatePaymentMode("COD")
                        .validateRefundAmnt("0.00")
                        .validateTxnDate(new Date())
                        .validateStatusAPIParameters()
                        .AssertAll();
                break;
            case "SAVEDCARD":
                if (firstpaymode.equalsIgnoreCase("DIRECT_BANK"))
                    throw new RuntimeException("Need to Run it manually");
                else {
                    CashierPage cashierPageSC = CashierPageFactory.getCashierPage(theme);
                    cashierPageSC.payBy(Constants.PayMode.SAVED_CARD, CorrectSavedCard, CorrectSavedCard.getCreditCardNumber());
                    ResponsePage responsePageSC = new ResponsePage();
                    responsePageSC.waitUntilLoads();
                }
                    if (firstpaymode.equalsIgnoreCase("ADDNPAY"))
                        AddNPayValidation(orderDTO);
                    else {
                    TxnStatus txnStatusSC = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    txnStatusSC.executeUntilNotPending();
                    txnStatusSC.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateGatewayName(Constants.Gateway.HDFC.toString())
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("CC")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;
            case "EMI_SAVEDCARD":
                if (firstpaymode.equalsIgnoreCase("DIRECT_BANK"))
                    throw new RuntimeException("Need to Run it manually");
                else {
                CashierPage cashierPageEMISC = CashierPageFactory.getCashierPage(theme);
                    CorrectSavedCard.setEmiCard(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                cashierPageEMISC.payBy(Constants.PayMode.EMI_SAVED_CARD, CorrectSavedCard, PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                ResponsePage responsePageEMISC = new ResponsePage();
                responsePageEMISC.waitUntilLoads();
                    TxnStatus EmiSavedCard = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    EmiSavedCard.executeUntilNotPending();
                    EmiSavedCard.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateGatewayName(Constants.Gateway.HDFC.toString())
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("EMI")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;
            case "ADVANCE_DEPOSIT":
                CashierPage cashierPageADV = CashierPageFactory.getCashierPage(theme);
                cashierPageADV.payBy(Constants.PayMode.ADVANCE_DEPOSIT_ACCOUNT);
                ResponsePage responsePageADV = new ResponsePage();
                responsePageADV.waitUntilLoads();
                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    break;

                } else {
                    TxnStatus AdvanceDeposit = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    AdvanceDeposit.executeUntilNotPending();
                    AdvanceDeposit.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateRespMsg(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("Paytm Advance Account")
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .AssertAll();
                }
                break;
            case "POSTPAID":
                CashierPage cashierPagePSD = CashierPageFactory.getCashierPage(theme);
                PostpaidHelpers.updateBalance(String.valueOf(txn_amount));
                cashierPagePSD.payBy(Constants.PayMode.PAYTM_DIGITAL_CARD);
                ResponsePage responsePagePSD = new ResponsePage();
                responsePagePSD.waitUntilLoads();
                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    AddNPayValidation(orderDTO);

                } else {
                    TxnStatus PAYTMCARD = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    PAYTMCARD.executeUntilNotPending();
                    PAYTMCARD.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateGatewayName(Constants.Gateway.PAYTMCC.toString())
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("PAYTM_DIGITAL_CREDIT")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;

            case "UPI":
                CashierPage cashierPageUPI = CashierPageFactory.getCashierPage(theme);
                cashierPageUPI.payBy(Constants.PayMode.UPI);
                cashierPageUPI.waitUntilLoads();
                ResponsePage responsePageUPI = new ResponsePage();
                responsePageUPI.waitUntilLoads();
                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    AddNPayValidation(orderDTO);

                } else {
                    TxnStatus UPI = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    UPI.executeUntilNotPending();
                    UPI.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("UPI")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;


            case "DIRECT_BANK":
                PaymentDTO directbank = new PaymentDTO();
                directbank.setCreditCardNumber(PaymentDTO.ICICI_CC_CARD);
                CashierPage cashierPageDTB= CashierPageFactory.getCashierPage(theme);
                cashierPageDTB.payBy(Constants.PayMode.CC, directbank);
                cashierPageDTB.waitUntilLoads();
                DirectBankOTPPage directBankOTPPage = new DirectBankOTPPage();
                directBankOTPPage.submitOtp("123456");
                ResponsePage responsePageDTB = new ResponsePage();
                responsePageDTB.waitUntilLoads();
                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    AddNPayValidation(orderDTO);

                } else {
                    responsePageDTB.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
                            .validateGatewayName(Constants.Gateway.ICICO.toString())
                            .validateBankName(Constants.Bank.ICI0.toString())
                            .validateResponsePageParameters();
                }
                break;


            case "EMI_DC":

                PaymentDTO Emi_Dc = new PaymentDTO();
                Emi_Dc.setEmiCard(PaymentDTO.ICICI_DEBIT_CARD_NUMBER).setBankName("ICICI Bank Debit Card").setMonth(3);

                CashierPage cashierPageEMIDC= CashierPageFactory.getCashierPage(theme);
                cashierPageEMIDC.payBy(Constants.PayMode.EMI, Emi_Dc);
                cashierPageEMIDC.waitUntilLoads();
                ResponsePage responsePageEMIDC = new ResponsePage();
                responsePageEMIDC.waitUntilLoads();

                if (firstpaymode.equalsIgnoreCase("ADDNPAY")) {
                    AddNPayValidation(orderDTO);

                }
                else {
                    TxnStatus EMI_DC = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
                    EMI_DC.executeUntilNotPending();
                    EMI_DC.validateTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                            .validateOrderid(orderDTO.getORDER_ID())
                            .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                            .validateStatus("TXN_SUCCESS")
                            .validateTxnType("SALE")
                            .validateGatewayName(Constants.Gateway.ICIE.toString())
                            .validateRespCode("01")
                            .validateRespMsg("Txn Successful.")
                            .validateMid(orderDTO.getMID())
                            .validatePaymentMode("EMI_DC")
                            .validateRefundAmnt("0.00")
                            .validateTxnDate(new Date())
                            .AssertAll();
                }
                break;


            case "ZESTMONEY":
                ZestBankUI ui = new ZestBankUI();
                PaymentDTO zest = new PaymentDTO()
                        .setBankName("ZEST");
                CashierPage cashierPageZEST= CashierPageFactory.getCashierPage(theme);
                cashierPageZEST.payBy(Constants.PayMode.ZEST, zest);
                cashierPageZEST.waitUntilLoads();
                ui.successZestTxn();
                ResponsePage responsePageZEST = new ResponsePage();
                responsePageZEST.waitUntilLoads();
                responsePageZEST.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
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
                        .validateGatewayName(Constants.Gateway.ZEST.toString())
                        .validateBankName(Constants.Bank.ZEST.toString())
                        .validateResponsePageParameters();
                break;

            default:
                throw new RuntimeException("Invalid Second Payment Mode");
        }
    }

    public void AddNPayValidation(OrderDTO orderDTO) {

        ResponsePage responsePageADDNPAY = new ResponsePage();
        responsePageADDNPAY.waitUntilLoads();
        responsePageADDNPAY.validateCurrency("INR")
                .validateGatewayName("WALLET")
                .validateRespMsg("Txn Success")
                .validateBankName("WALLET")
                .validatePaymentMode("PPI")
                .validateMid(orderDTO.getMID())
                .validateRespCode("01")
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateTxnAmount(orderDTO.getTXN_AMOUNT())
                .validateOrderId(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }
}

