package scripts.api.PromoAndEmiSubvention;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.QRHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.CHETAN;
import static com.paytm.appconstants.Constants.Owner.MEHUL_GUPTA;
import static io.restassured.RestAssured.given;

public class MultiItemUnifiedFlowTest extends PGPBaseTest {
    String emi_body_item_based_with_tenure = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"5fe081da-c1d6-4923-a24d-34976cb01600\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020011\",\n" +
            "                \"brandId\": \"327\",\n" +
            "                \"categoryId\": \"3271\",\n" +
            "                \"price\": 2000.25,\n" +
            "                \"offerDetails\": {\n" +
            "                    \"emiOfferDetails\": {\n" +
            "                        \"offerId\": \"2164614\"\n" +
            "                    },\n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2155512\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"issuingBank\": \"HDFC\",\n" +
            "                    \"issuingNetworkCode\": \"VISA\",\n" +
            "                    \"applyBankOffer\": true,\n" +
            "                    \"applySubvention\": true,\n" +
            "                    \"vpa\": \"\",\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardNo\": \"4718650100010336\",\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"5177b1cc1e594c959ba232c7e2754e0f1687860859762\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"true\",\n" +
            "        \"amountBasedSubvention\": \"true\",\n" +
            "        \"offerDetails\": {\n" +
            "            \"emiOfferDetails\": {\n" +
            "                \"offerId\": \"2141488\"\n" +
            "            },\n" +
            "            \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2151610\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "        },\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"vpa\": \"\",\n" +
            "                    \"issuingBank\": \"HDFC\",\n" +
            "                    \"cardNo\": \"4718650100010336\",\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";

    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env=" + LocalConfig.ENV_NAME;
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private final String HDFC_CC_CARDNO = "4718650100010336";
    private final String ICICI_DC_CARDNO = "4799320857008816";

    private final CheckoutJsCheckoutPage checkoutPage = new CheckoutJsCheckoutPage();

    private void payByEMI(CashierPage cashierPage, PaymentDTO paymentDTO) {
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.scrollToElement(cashierPage.tabEMI());
        cashierPage.tabEMI().click();
        cashierPage.waitUntilLoads();
        cashierPage.EmiRadioButton().click();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_emiIframe());
        cashierPage.textBoxCardNumberEMI().clearAndType(paymentDTO.getEmiCard());
        cashierPage.waitUntilLoads();
        cashierPage.emiPlan().click();
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.proceedToConvertEMI().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_expCvv_cardIframe());
        cashierPage.textBoxExpiryMonthEMI().clearAndType(paymentDTO.getExpMonth());
        cashierPage.textBoxExpiryYearEMI().waitUntilEditable();
        cashierPage.textBoxExpiryYearEMI().clearAndType(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.buttonPGPayNow().click();
    }
    private void payByCC(CashierPage cashierPage, PaymentDTO paymentDTO) {
        if (cashierPage.viewAllOffersAvialable_HideButton().isElementPresent()){
            cashierPage.viewAllOffersAvialable_HideButton().click();
        }
        cashierPage.scrollToElement(cashierPage.tabCreditCard());
        cashierPage.tabCreditCard().click();
        cashierPage.waitUntilLoads();
        DriverManager.getDriver().switchTo().frame(cashierPage.ccdc_cardIframe());
        cashierPage.textBoxCardNumber().clearAndType(paymentDTO.getEmiCard());
        cashierPage.fillExpiryMonth(paymentDTO.getExpMonth());
        cashierPage.fillExpiryYear(paymentDTO.getExpYear().substring(2));
        cashierPage.textBoxCVVNumber().clearAndType(paymentDTO.getCvvNumber());
        DriverManager.getDriver().switchTo().defaultContent();
        cashierPage.pause(2);
        cashierPage.buttonPGPayNow().click();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items with all different Items")
    public void TxnSuccess_CustomCheckout_MultiItemWithDifferentItems() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKMULTI0001";
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + "e10c7060946d456783b73ad9017701c0", "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + "e10c7060946d456783b73ad9017701c0", "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for 5 items and then full refund of 1 item(PGP-57174)")
    public void TxnSuccess_CustomCheckout_5Items() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item5 = new SimplifiedUnifiedOffers.Items("Item005_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 5500.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();

        // full refund of 1 element
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));

    }

    @Owner(CHETAN)
    @Feature("PGP-57174")
    @Test(description = "Verify E2E txn success for 5 items and then partial refund of multiple items")
    public void TxnSuccess_CustomCheckout_Items_Partial_refund_of_multiple_items() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 3300.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();

        // partial refund of 2 elements
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_" + orderId + "' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_" + orderId+"' }.payableAmount");
        Double refundAmountItem1 = payableAmountItem1 - 10;
        Double refundAmountItem2 = payableAmountItem2 - 10;
        String refundItems2 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123\",\"itemRefundAmount\":\"" + refundAmountItem1 +"\"}," +
                "{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"47169\",\"itemRefundAmount\":\"" + refundAmountItem2 + "\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(refundAmountItem1 + refundAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}", refundItems2);
        String checksum1 = PGPUtil.getChecksum(mid.getKey(), bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(refundAmountItem1 + refundAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}", refundItems2);
        SyncRefund syncRefund1 = new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();

        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(refundAmountItem1 + refundAmountItem2));
    }


    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on all items(Best Offer) and partial refund of 1 item(no child on affordability)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromoOnAllItemsPartialRefundOn1ItemHDFC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        // Partial refund on which affordability side no child is created(HDFC)

        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_" + orderId + "' }.payableAmount");
        payableAmountItem1 = payableAmountItem1 - 10;
        String refundItems1 = "[{\"itemId\":\"Item001_" + orderId + "\",\"productId\":\"123\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}", refundItems1);
        String checksum1 = PGPUtil.getChecksum(mid.getKey(), bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}", refundItems1);
        SyncRefund syncRefund1 = new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();

        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on all items(Best Offer) and full refund(no child - afford)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromoOnAllItemsFullRefundOn1ItemHDFC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        // full refund on which affordability side no child is created(HDFC)

        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_" + orderId + "' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_" + orderId + "\",\"productId\":\"123\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}", refundItems1);
        String checksum1 = PGPUtil.getChecksum(mid.getKey(), bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}", refundItems1);
        SyncRefund syncRefund1 = new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();

        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where only Subvention applied on all items(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlySubventionOnAllItems() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);

        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where only Promo applied on all items(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlyPromoOnAllItems() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].applySubvention", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with only amount based subvention")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlyAmtBasedSubvention() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();

        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", null)
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", false)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with only amount based offers")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlyAmtBasedPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();

        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", null)
                .setContext("body.paymentDetails.paymentOptions[0].applySubvention", false)
                .setContext("body.amountBasedSubvention", null)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with both amount based offers and amount based subvention")
    public void TxnSuccess_CustomCheckout_MultipleItems_AmtBasedSubventionAmtBasedPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with Amount Based Subvention and item based promo (single Item)(Best Offer)")
    public void TxnSuccess_CustomCheckout_SingleItems_AmtBasedSubventionItemBasedPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "92226", 800.00, "86414");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with Item Based Subvention and Amount based promo (single Item)(Best Offer)")
    public void TxnSuccess_CustomCheckout_SingleItems_ItemBasedSubventionAmtBasedPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47178", "92226", 800.00, "186416");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with Amount Based Subvention and item based promo (Multiple Item)(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_AmtBasedSubventionItemBasedPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "92226", 400.00, "86414");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47176", "92226", 400.00, "86414");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with Item Based Subvention and Amount based promo (Multiple Item)(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_ItemBasedSubventionAmtBasedPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47178", "92226", 400.00, "186416");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47178", "92226", 400.00, "186416");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with where only Subvention applied on 1 item and only promo applied on another item(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_Subvention_on_1_item_Promo_on_1item() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "186409");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "86409");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNotNull();

        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on 1 item and only promo applied on another(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Promo_on_1item() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "86409");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and only subvention applied on another(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1item() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "186409");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and No offer on another(Best Offer)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_NoOffer_on_1item() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "1102201", "582300", 1100.00, "900135");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(CHETAN)
    @Feature("PGP-57170")
    @Test(description = "Verify custom checkout flow single item subvention unifiedOffers token should not be returned")
    public void customCheckout_SingleItem_SubventionUnifiedOffersTokenNotReturned() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "900135");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken")).isNull();
    }

    @Owner(CHETAN)
    @Feature("PGP-57170")
    @Test(description = "Verify offerApply api response is not returning unifiedOffersToken when TxnAmount is greater than sum of product prices in the request")
    public void customCheckout_SingleItem_SubventionTxnAmountGreaterThanItemAmountUnifiedOffersTokenNotReturned() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken")).isNull();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("TxnAmount is greater than sum of product prices in the request");
    }

    @Owner(CHETAN)
    @Feature("PGP-57170")
    @Test(description = "Verify offerApply api response is not returning unifiedOffersToken when TxnAmount is greater than sum of product prices in the request")
    public void customCheckout_MultiItem_SubventionTxnAmountGreaterThanItemAmountUnifiedOffersTokenNotReturnedUsingCC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_OLD_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "900135");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken")).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success and refund on 1 already refunded item and 1 non refunded item for Multiple Items where Subvention and promo are applied on 1 item and Subvention on 1 Item and Promo on 1 item and No offer on another(Best Offer) - ICICI DC")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1Item_promo_on_1item_NoOffer_on_1item_ICICI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "47171", "92226", 1100.00, "86409");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "1102201", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 4400.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();

        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"47169\"}]";
        String refundItems2 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"47169\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"47171\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));
        Thread.sleep(10000);
        String refId2 = CommonHelpers.generateOrderId();
        String bodyforChecksum2 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems2);
        String checksum2= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum2);
        String body2 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum2).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems2);
        SyncRefund syncRefund2 =  new SyncRefund(body2);
        JsonPath asyncRefundResp2 = syncRefund2.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultCode")).isEqualTo("600");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultMsg")).isEqualTo("Already refunded - Item001_"+orderId);
        Assertions.assertThat(asyncRefundResp2.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1 + payableAmountItem2));
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success and refund on 1 already refunded item and 1 non refunded item for Multiple Items where Subvention and promo are applied on 1 item and Subvention on 1 Item and Promo on 1 item and No offer on another(Best Offer) - HDFC CC")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1Item_promo_on_1item_NoOffer_on_1item_HDFC_CC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "47171", "92226", 1100.00, "86409");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "1102201", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 4400.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNotNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.emiOfferDetails[0].emiType")).isEqualTo("STANDARD");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();

        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"47169\"}]";
        String refundItems2 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"47169\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"47171\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));
        Thread.sleep(10000);
        String refId2 = CommonHelpers.generateOrderId();
        String bodyforChecksum2 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems2);
        String checksum2= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum2);
        String body2 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum2).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId2).replace("{refund_items}",refundItems2);
        SyncRefund syncRefund2 =  new SyncRefund(body2);
        JsonPath asyncRefundResp2 = syncRefund2.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultCode")).isEqualTo("600");
        Assertions.assertThat(asyncRefundResp2.getString("body.resultInfo.resultMsg")).isEqualTo("Already refunded - Item001_"+orderId);
        Assertions.assertThat(asyncRefundResp2.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1 + payableAmountItem2));
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success and refund for Multiple Items where Subvention and promo are applied on all items(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromoOnAllItems_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224", "2388893", bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emiType")).isEqualTo("SUBVENTION");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refId = CommonHelpers.generateOrderId();
        String refundItems = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"47169\"}]";
        String bodyforChecksum = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        String checksum= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum);
        String body = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        SyncRefund syncRefund =  new SyncRefund(body);
        JsonPath asyncRefundResp = syncRefund.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1 + payableAmountItem2));
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where only Subvention applied on all items(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlySubventionOnAllItems_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224", "2388893", null);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where only Promo applied on all items(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlyPromoOnAllItems_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].applySubvention", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with only amount based subvention(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlyAmtBasedSubvention_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();

        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.paymentDetails.paymentOptions[0].applyBankOffer", false)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", null)
                .setContext("body.offerDetails.emiOfferDetails.offerId", "2396404")
                .setContext("body.offerDetails.bankOfferDetails", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2396404");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNull();
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with only amount based offers(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_OnlyAmtBasedPromo_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();

        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", null)
                .setContext("body.paymentDetails.paymentOptions[0].applySubvention", false)
                .setContext("body.amountBasedSubvention", null)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.offerDetails.emiOfferDetails", null)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId", "2391737")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2391737");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with both amount based offers and amount based subvention(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_AmtBasedSubventionAmtBasedPromo_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.offerDetails.emiOfferDetails.offerId", "2396404")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId", "2391737")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2396404");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2391737");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Txn with where only Subvention applied on 1 item and only promo applied on another item(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_Subvention_on_1_item_Promo_on_1item_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on 1 item and only promo applied on another(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Promo_on_1item_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and only subvention applied on another(Offer specified)")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1item_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");

        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and No offer on another(Offer specified) - HDFC")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_NoOffer_on_1item_HDFC_CC_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "1102201", "582300", 1100.00, "900135");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success and refund with incorrect itemId for Multiple Items where Subvention and promo are applied on 1 item and Subvention on 1 Item and Promo on 1 item and No offer on another(Offer specified) - ICICI DC")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1Item_promo_on_1item_NoOffer_on_1item_ICICI_DC_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "1102201", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 4400.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();

        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refId = CommonHelpers.generateOrderId();
        String refundItems = "[{\"itemId\":\"Item001_11220192\",\"productId\":\"47169\"},{\"itemId\":\"Item002_1139984\",\"productId\":\"47171\"}]";
        String bodyforChecksum = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        String checksum= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum);
        String body = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        SyncRefund syncRefund =  new SyncRefund(body);
        JsonPath asyncRefundResp = syncRefund.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultCode")).isEqualTo("600");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid refund request.");
        Assertions.assertThat(asyncRefundResp.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1 + payableAmountItem2));
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E txn Success and refund with incorrect productId for Multiple Items where Subvention and promo are applied on 1 item and Subvention on 1 Item and Promo on 1 item and No offer on another(Offer specified) - HDFC CC")
    public void TxnSuccess_CustomCheckout_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1Item_promo_on_1item_NoOffer_on_1item_HDFC_CC_Offer_Specified() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "1102201", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 4400.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");

        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isEqualTo("2388893");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item003_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isEqualTo("2402278");
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.emiOfferDetails[0].offerId")).isNull();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item004_"+orderId+"' }.offerDetails.bankOfferDetails[0].offerId")).isNull();

        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refId = CommonHelpers.generateOrderId();
        String refundItems = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"1234\"}]";
        String bodyforChecksum = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        String checksum= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum);
        String body = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        SyncRefund syncRefund =  new SyncRefund(body);
        JsonPath asyncRefundResp = syncRefund.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultCode")).isEqualTo("600");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid refund request.");
        Assertions.assertThat(asyncRefundResp.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1 + payableAmountItem2));

    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn on NB")
    public void TxnSuccess_CustomCheckout_MultiItem_NB() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", null)
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", null)
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "NET_BANKING");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setCardInfo(null)
                .setAuthMode("3D")
                .setChannelCode("HDFC")
                .setEmiType(null)
                .setPlanId(null)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn on CC")
    public void TxnSuccess_CustomCheckout_MultiItem_CC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType(null)
                .setPlanId(null)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();


    }


    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn on DC and then full refund of 1 item")
    public void TxnSuccess_CustomCheckout_MultiItem_DC_full_refund() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("DEBIT_CARD")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType(null)
                .setPlanId(null)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();

        // full refund of 1 element
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].offerDetails[0].items.find {it.id == 'Item001_"+ orderId+"' }.payableAmount");

        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
        String refId1 = CommonHelpers.generateOrderId();
        String bodyforChecksum1 = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        String checksum1= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum1);
        String body1 = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum1).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId1).replace("{refund_items}",refundItems1);
        SyncRefund syncRefund1 =  new SyncRefund(body1);
        JsonPath asyncRefundResp1 = syncRefund1.execute().jsonPath();

        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp1.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp1.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1));

    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn on UPI and refund with duplicate items")
    public void TxnSuccess_CustomCheckout_MultiItem_UPI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", null)
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", null)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", null)
                .setContext("body.paymentDetails.paymentOptions[0].vpa", "arsh2.test@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "UPI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("UPI")
                .setPayerAccount("arsh2.test@paytm")
                .setCardInfo(null)
                .setAuthMode("3D")
                .setChannelCode(null)
                .setEmiType(null)
                .setPlanId(null)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].offerDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].offerDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refundItems = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123\"},{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123\"}]";
        String refId = CommonHelpers.generateOrderId();
        String bodyforChecksum = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        String checksum= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum);
        String body = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem2)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        SyncRefund syncRefund =  new SyncRefund(body);
        JsonPath asyncRefundResp = syncRefund.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultCode")).isEqualTo("600");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid refund request.");
        Assertions.assertThat(asyncRefundResp.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1 + payableAmountItem2));
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn on EMI")
    public void TxnSuccess_CustomCheckout_MultiItem_EMI() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn on EMI_DC")
    public void TxnSuccess_CustomCheckout_MultiItem_EMI_DC() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", ICICI_DC_CARDNO);
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + ICICI_DC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("ICICI")
                .setEmiType("DEBIT_CARD")
                .setPlanId("ICICI|3")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("EMI_DC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
    }


    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Single Item(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_SingleItems(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "1100";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Item(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for 5 items(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_5Items(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "5500";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item5 = new SimplifiedUnifiedOffers.Items("Item005_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn failed for 6/6+ items(Best Offer)")
    public void TxnFailure_MultiItem_SimplifiedUnified_MoreThan5Items(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "6600";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item5 = new SimplifiedUnifiedOffers.Items("Item005_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item6 = new SimplifiedUnifiedOffers.Items("Item006_" + orderId, "123", "18084", 1100.00, "6224");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        items.add(item5);
        items.add(item6);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        JsonPath response = initTxn.execute().jsonPath();
        Assertions.assertThat(response.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on all items(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_SubventionAndPromoOnAllItems(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where only Subvention applied on all items(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_OnlySubventionOnAllItems(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, (SimplifiedUnifiedOffers.PromoDetails) null);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        Assert.assertNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where only promo applied on all items(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_OnlyPromoOnAllItems(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(null, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).doesNotContainIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).doesNotContainIgnoringCase("gratificationDiscount");
        Assert.assertNotNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with only amount based subvention")
    public void TxnSuccess_MultiItem_SimplifiedUnified_AmtBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "800";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800.00", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, (SimplifiedUnifiedOffers.PromoDetails) null);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("gratificationDiscount");
        Assert.assertNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with only amount based offers")
    public void TxnSuccess_MultiItem_SimplifiedUnified_AmtBasedPromo(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "800";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(null, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).doesNotContainIgnoringCase("gratificationType");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).doesNotContainIgnoringCase("gratificationDiscount");
        Assert.assertNotNull(txnStatus.getResponse().getPaymentPromoCheckoutData());
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with both amount based offers and amount based subvention")
    public void TxnSuccess_MultiItem_SimplifiedUnified_AmtBasedPromo_AmtBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "800";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800.00", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with Amount Based Subvention and item based promo (single Item)(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_SingleItem_ItemBasedPromo_AmtBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "800";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "92226", 800.00, "86414");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800.00", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with Item Based Subvention and Amount based promo (single Item)(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_SingleItem_AmtBasedPromo_ItemBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "800";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47178", "92226", 800.00, "186416");
        items.add(item1);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with Amount Based Subvention and item based promo (Multiple Items)(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_ItemBasedPromo_AmtBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "800";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "92226", 400.00, "86414");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47176", "92226", 400.00, "86414");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("true", "800.00", null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with Item Based Subvention and Amount based promo (Multiple Items)(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_AmtBasedPromo_ItemBasedSubvention(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "800";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47178", "92226", 400.00, "186416");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47178", "92226", 400.00, "186416");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "true", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with where only Subvention applied on 1 item and only promo applied on another item(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_Subvention_on_1_item_Promo_on_1item(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "186409");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "86409");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        //Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on 1 item and only promo applied on another(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_Promo_on_1item(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "86409");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on 1 item and only Subvention applied on another(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1item(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "186409");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and No offer on another(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_NoOffer_on_1item(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "1102201", "582300", 1100.00, "900135");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and Subvention on 1 Item and Promo on 1 item and No offer on another(Best Offer)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1Item_promo_on_1item_NoOffer_on_1item(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "4400";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47169", "92226", 1100.00, "86407");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "47171", "92226", 1100.00, "86409");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "1102201", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        //Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on all items(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_SubventionAndPromoOnAllItems_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224", "2388893", bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where only Subvention applied on all items(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_OnlySubventionOnAllItems_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224", "2388893", null);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", null);
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, (SimplifiedUnifiedOffers.PromoDetails) null);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where only promo applied on all items(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_OnlyPromoOnAllItems_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123", "18084", 1100.00, "6224", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", null, bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(null, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E Txn with where only Subvention applied on 1 item and only promo applied on another item(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_Subvention_on_1_item_Promo_on_1item_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on 1 item and only promo applied on another(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_Promo_on_1item_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn success for Multiple Items where Subvention and promo are applied on 1 item and only Subvention applied on another(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1item_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and No offer on another(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_NoOffer_on_1item_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "1102201", "582300", 1100.00, "900135");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn Success for Multiple Items where Subvention and promo are applied on 1 item and Subvention on 1 Item and Promo on 1 item and No offer on another(Offer specified)")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_SubventionAndPromo_on_1_item_Subvention_on_1Item_promo_on_1item_NoOffer_on_1item_Offer_Specified(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "4400";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails> bankOfferDetailsList = new ArrayList<>();
        bankOfferDetailsList.add(new SimplifiedUnifiedOffers.OfferDetails.BankOfferDetails("2402278"));
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47169", "92226", 1100.00, "86407", "2388893", bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47171", "92226", 1100.00, "186409", "2388893", null);
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "47171", "92226", 1100.00, "86409", null, bankOfferDetailsList);
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "1102201", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        items.add(item3);
        items.add(item4);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByEMI(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("EMI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        TxnStatus txnStatus = new TxnStatus(merchantType.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("items");
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).containsIgnoringCase("totalGratification");
        //Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).containsIgnoringCase("items");
    }
    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57206")
    @Parameters({"theme"})
    @Test(description = "Verify E2E txn Success and Refund with Items context sent for Multiple Items where No Offer applied on any items - CC")
    public void TxnSuccess_MultiItem_SimplifiedUnified_MultipleItems_No_Offer_applied_on_any_item(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
        User user = userManager.getForRead(PGPBaseTest.Label.BASIC);
        String txnAmount = "2200";
        Constants.MerchantType merchantType = Constants.MerchantType.EMI_NEW_FLOW;
        ArrayList<String> promoCode = new ArrayList<>();
        promoCode.add("");
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "1102201", "582300", 1100.00, "900135");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "1102202", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        SimplifiedUnifiedOffers.PromoDetails promoDetails = new
                SimplifiedUnifiedOffers.PromoDetails(promoCode, "true", "false", "false", null);
        SimplifiedUnifiedOffers.SubventionDetails subventionDetails = new
                SimplifiedUnifiedOffers.SubventionDetails("false", null, null, null);
        SimplifiedUnifiedOffers simplifiedUnifiedOffers = new
                SimplifiedUnifiedOffers(subventionDetails, promoDetails);
        simplifiedUnifiedOffers.setItem(items);
        InitTxnDTO initTxnDTO = new InitTxnDTO.
                Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .setSimplifiedUnifiedOffers(simplifiedUnifiedOffers)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        MerchantConfig config = checkoutPage.loadMerchantConfig(initTxnDTO, theme);
        config.data.setToken(txnToken);
        checkoutPage.createCheckoutJsOrder(config);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        PaymentDTO paymentDTO = new PaymentDTO();
        payByCC(cashierPage, paymentDTO);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(initTxnDTO.getBody().getMid())
                .validateOrderId(initTxnDTO.getBody().getOrderId())
                .validatePaymentMode("CC")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();
        Float refundAmount = Float.parseFloat(responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_AMOUNT));
        String refundItems = "[{\"itemId\":\"Item001_2b5535e7ef1b540441abd835d24e0599b8f57\",\"productId\":\"123\"},{\"itemId\":\"Item003_2b35e5557ef1b4990441abd835d24e05b8f57\",\"productId\":\"123\"}]";
        String refId = CommonHelpers.generateOrderId();
        String bodyforChecksum = SyncRefund.bodyWithItems.replace("{MID}", merchantType.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(refundAmount)).replace("{ORDER_ID}", responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)).replace("{TRANSACTION_ID}", responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        String checksum= PGPUtil.getChecksum(merchantType.getKey(),bodyforChecksum);
        String body = SyncRefund.requestWithItems.replace("{MID}", merchantType.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(refundAmount)).replace("{ORDER_ID}", responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.ORDER_ID)).replace("{SIGNATURE}", checksum).replace("{TRANSACTION_ID}", responsePage.getResponseUIFieldValue(ResponsePage.ResponseUIField.TXN_ID)).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        SyncRefund syncRefund =  new SyncRefund(body);
        JsonPath asyncRefundResp = syncRefund.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp.getString("body.refundAmount")).isEqualTo(refundAmount.toString());
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57206")
    @Test(description = "Verify E2E txn Success and Refund with Items context sent for Multiple Items where No Offer applied on any items - EMI")
    public void TxnSuccess_CustomCheckout_MultipleItems_No_Offer_applied_on_any_item() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        String txnAmount = "2200.0";
        String orderId = CommonHelpers.generateOrderId();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "1102201", "582300", 1100.00, "900135");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "1102202", "582300", 1100.00, "900135");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setTxnValue(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + HDFC_CC_CARDNO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.orderFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(mid.getId())
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(txnAmount)
                .AssertAll();
        Float refundAmount = Float.parseFloat(txnStatus.getResponse().getTXNAMOUNT());
        String refId = CommonHelpers.generateOrderId();
        String refundItems = "[{\"itemId\":\"Item001_" + orderId +"\",\"productId\":\"1102201\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"1102202\"}]";
        String bodyforChecksum = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(refundAmount)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        String checksum= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum);
        String body = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(refundAmount)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        SyncRefund syncRefund =  new SyncRefund(body);
        JsonPath asyncRefundResp = syncRefund.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultStatus")).isEqualTo("PENDING");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultCode")).isEqualTo("601");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultMsg")).isEqualTo("Refund request was raised for this transaction. But it is pending state");
        Assertions.assertThat(asyncRefundResp.getString("body.refundAmount")).isEqualTo(refundAmount.toString());


    }
}
