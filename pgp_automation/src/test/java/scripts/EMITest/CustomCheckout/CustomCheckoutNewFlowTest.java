package scripts.EMITest.CustomCheckout;

import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.refund.SyncRefund;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SimplifiedUnifiedOffers;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.processTransactionV1.CardTokenInfo;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.utils.merchant.util.PGPUtil;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import com.paytm.api.Deals.InitiateTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.dto.PaymentDTO.*;
import io.restassured.response.Response;
import scripts.api.mappingService.PG2MappingApis.merchantDeviceDetails;

import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.UltimateBeneficiaryDetails;
import com.paytm.dto.NativeDTO.InitTxn.AffordabilityInfo;
import com.paytm.dto.NativeDTO.OfferApply.OfferApplyDTO;
import java.util.Collections;
import com.paytm.dto.NativeDTO.InitTxn.Good;
import com.paytm.dto.NativeDTO.InitTxn.Price;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.AffordabilityDetails;
import com.paytm.dto.NativeDTO.InitTxn.AffordabilityDetails.ContriInfo;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import java.util.ArrayList;
import com.paytm.dto.NativeDTO.OfferApply.*;
import com.paytm.dto.NativeDTO.OfferApply.Tenure;
public class CustomCheckoutNewFlowTest extends PGPBaseTest {
    private static final String RUPAY_CC_CARD_NO = AlternateID_RUPAY_CARD;
    private static final String VISA_CC_CARD_NO = AlternateID_VISA_CARD;
    private static final String MASTER_ICICI_DC_NUMBER = MASTER_ICICI_DEBIT_CARD_NUMBER;
    private static final String DINERS_CC_CARD_NO = DINERS_CC_CARD_NUMBER;
    private static final String RUPAY_COFT_TOKEN = COFT_RUPAY_TOKEN;
    private static final String VISA_COFT_TOKEN = COFT_VISA_TOKEN;
    private static final String VISA_ALT_TOKEN = COFT_VISA_TOKEN;
    private static final String dinersExpiry = "022030";
    private static final String expiry = "122027";
    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env=" + LocalConfig.ENV_NAME;
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();

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
    String emi_body_item_based_with_tenure_cardtokenInfo = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"0da8ugrc1t98e93q20zkkke9x3sikko96550\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000407719\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"paytmUserId\": \"1000177185\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"123047\",\n" +
            "                \"brandId\": \"18084\",\n" +
            "                \"price\": 1100.0,\n" +
            "                \"categoryId\": \"6224\"\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 1100.0,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"issuingBank\": \"HDFC\",\n" +
            "                    \"issuingNetworkCode\": \"VISA\",\n" +
            "                    \"applyBankOffer\": true,\n" +
            "                    \"applySubvention\": true,\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardTokenInfo\": {\n" +
            "                        \"cardToken\": \"4718650100000195\",\n" +
            "                        \"panUniqueReference\": \"V0010013021361288827541710336\"\n" +
            "                    },\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_body_amount_based_with_tenure = "{\n" +
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
            "                    \"cardNo\": \"4718650100010336\"\n" +
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

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Feature("PGP-58730")
    @Test(description = "Custom Checkout with RUPAY CARD , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_RUPAY_CARD_PAR_DISABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "RUPAY")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", RUPAY_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + RUPAY_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);

        //PGP-58730 Test originalCardHash being sent in extendInfo and channelInfo in COP/CO&P and hash is created using Card Number
        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_PAY_ORDER","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}");  // Fix object boundaries
        JsonPath acqLogs = new JsonPath(cleanedJson);
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo")).contains("originalCardHash:9fff5b365aa8e3934355eaccf00604a26833423301424bdb34914648855a214c");
        Assertions.assertThat(acqLogs.getString("REQUEST.paymentInfo.paymentBillOption[0].channelInfo")).contains("originalCardHash:9fff5b365aa8e3934355eaccf00604a26833423301424bdb34914648855a214c");
        System.out.println("Cleaned json : "+ cleanedJson);
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.status")).isEqualTo("1");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.promocode")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.promotext")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText").replace("₹","?"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.promoVisibility")).isEqualTo("false");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.totalTransactionAmount")).isEqualTo("220000");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.savings[0].savings")).contains(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value").replace(".",""));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.savings[0].redemptionType")).isIn("discount","cashback");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.bankOfferContriInfo")).contains("bank","brand","merchant","platform");

        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.planId")).containsOnlyDigits();
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.tenure")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.gratificationDiscount")).isIn(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value"),jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].gratifications[0].value"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.gratificationType")).isIn(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].type"),jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].gratifications[0].type"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.subventionAmount")).isEqualTo("2200.0");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.pgPlanId")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.emiInterestRate")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.emi")).isIn(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi"),jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[1].offerDetails.emiOfferDetails[0].emi"));

        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.TGD.BO[0].value").replace(".","")).containsOnlyDigits();
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.TGD.BO[0].type")).isIn("discount","cashback");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.TGD.BO[1].value").replace(".","")).containsOnlyDigits();
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.TGD.BO[1].type")).isIn("discount","cashback");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.TGD.EO[0].value").replace(".","")).containsOnlyDigits();
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.TGD.EO[0].type")).isIn("discount","cashback");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item001_"+orderId+"' }.BI")).isEqualTo("18084");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item001_"+orderId+"' }.PI")).isEqualTo("123047");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item001_"+orderId+"' }.OA")).isEqualTo("1100.0");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item001_"+orderId+"' }.PA")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item002_"+orderId+"' }.BI")).isEqualTo("18260");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item002_"+orderId+"' }.PI")).isEqualTo("55005");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item002_"+orderId+"' }.OA")).isEqualTo("1100.0");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData.items.find { it.id == 'Item002_"+orderId+"' }.PA")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount"));

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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        // full refund of 1 item
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
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
    @Test(description = "Custom Checkout with VISA CARD , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_VISA_CARD_PAR_DISABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        // partial refund of 2 items
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_" + orderId + "' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_" + orderId+"' }.payableAmount");
        Double refundAmountItem1 = payableAmountItem1 - 10;
        Double refundAmountItem2 = payableAmountItem2 - 10;
        String refundItems2 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + refundAmountItem1 +"\"}," +
                "{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"55005\",\"itemRefundAmount\":\"" + refundAmountItem2 + "\"}]";
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
    @Test(description = "Custom Checkout with MASTER CARD , new flow with PAR config disable - EMI and Bank offers Applied, Amount Based")
    public void testCustomCheckoutNewFlow_EMI_BO_MASTER_CARD_PAR_DISABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_MASTER;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKCUSTOMNEW0003";
        String orderId = CommonHelpers.generateOrderId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.0)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value", 12)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "MASTER")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", MASTER_ICICI_DC_NUMBER)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + MASTER_ICICI_DC_NUMBER + "|111|"+expiry)
                .setAuthMode("otp")
                .setChannelCode(null)
                .setEmiType("DEBIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);

        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");

        String payLog = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        String cleanedJson = payLog.strip()
                .replace("\\\"", "\"") // Replace escaped quotes
                .replace("\"{", "{")   // Fix object boundaries
                .replace("}\"", "}");  // Fix object boundaries
        JsonPath acqLogs = new JsonPath(cleanedJson);
        System.out.println("Cleaned json : "+ cleanedJson);
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.status")).isEqualTo("1");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.promocode")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].promocode"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.promotext")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].offerText").replace("₹","?"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.promoVisibility")).isEqualTo("false");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.totalTransactionAmount")).isEqualTo("40000");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.savings[0].savings")).contains(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.bankOfferDetails[0].gratifications[0].value").replace(".",""));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.savings[0].redemptionType")).isIn("discount","cashback");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.paymentPromoCheckoutData.bankOfferContriInfo")).contains("bank","brand","merchant","platform");

        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.planId")).containsOnlyDigits();
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.tenure")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.gratificationCashback")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].value"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.gratificationType")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].gratifications[0].type"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.subventionAmount")).isEqualTo("400.0");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.pgPlanId")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.emiInterestRate")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiSubventionInfo.emi")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi"));

        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.unifiedOffersItemLevelData")).isNullOrEmpty();

        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.cardType")).isEqualTo("DEBIT_CARD");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.MID")).isEqualTo(mid.getId());
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.loanAmount")).isEqualTo("400.00");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.cardIssuer")).isEqualTo("ICICI");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.emiPayMethod")).isEqualTo("EMI_DC");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.bank")).isEqualTo("ICICI");
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.emiAmount")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].emi"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.ORDER_ID")).isEqualTo(initTxnDTO.orderFromBody());
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.interest")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].interest"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.emiMonths")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.value"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.emiInterestRate")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].tenure.roi"));
        Assertions.assertThat(acqLogs.getString("REQUEST.extendInfo.emiInfo.planID")).isEqualTo(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId"));

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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with DINERS CARD , new flow with PAR config disable - Bank offers Applied, Amount Based")
    public void testCustomCheckoutNewFlow_EMI_BO_DINERS_CARD_PAR_DISABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE_DINERS;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKCUSTOMNEW0004";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.0)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", null)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "DINERS")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", DINERS_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", null)
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + DINERS_CC_CARD_NO + "|111|"+dinersExpiry)
                .setAuthMode("otp")
                .setChannelCode(null)
                .setEmiType(null)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with RUPAY CARD , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_RUPAY_CARD_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "RUPAY")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", RUPAY_CC_CARD_NO)
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
                .setCardInfo("|" + RUPAY_CC_CARD_NO + "|111|122025")
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

        // Full refund of 2 items
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refId = CommonHelpers.generateOrderId();
        String refundItems = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"55005\"}]";
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
    @Test(description = "Custom Checkout with VISA CARD , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_VISA_CARD_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
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
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
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
        String refundItems1 = "[{\"itemId\":\"Item001_" + orderId + "\",\"productId\":\"123047\",\"itemRefundAmount\":\"" + payableAmountItem1 +"\"}]";
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
    @Test(description = "Custom Checkout with MASTER CARD , new flow with PAR config enable - EMI and Bank offers Applied, Amount Based")
    public void testCustomCheckoutNewFlow_EMI_BO_MASTER_CARD_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_MASTER;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKCUSTOMNEW0007";
        String orderId = CommonHelpers.generateOrderId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.0)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value", 12)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "MASTER")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", MASTER_ICICI_DC_NUMBER)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_DC");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + MASTER_ICICI_DC_NUMBER + "|111|"+expiry)
                .setAuthMode("otp")
                .setChannelCode(null)
                .setEmiType("DEBIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with DINERS CARD , new flow with PAR config enable - Bank offers Applied, Amount Based")
    public void testCustomCheckoutNewFlow_EMI_BO_DINERS_CARD_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE_DINERS;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKCUSTOMNEW0008";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.0)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", null)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "DINERS")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", DINERS_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", null)
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("CREDIT_CARD")
                .setCardInfo("|" + DINERS_CC_CARD_NO + "|111|"+dinersExpiry)
                .setAuthMode("otp")
                .setChannelCode(null)
                .setEmiType(null)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }



    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with RUPAY External Coft Token , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_RUPAY_COFT_PAR_DISABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "RUPAY")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", RUPAY_COFT_TOKEN)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference", "V0010013021361288827541727409")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        // refund with Incorrect Item Id
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refId = CommonHelpers.generateOrderId();
        String refundItems = "[{\"itemId\":\"Item001_11220192\",\"productId\":\"123047\"},{\"itemId\":\"Item002_1139984\",\"productId\":\"55005\"}]";
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
    @Test(description = "Custom Checkout with VISA External Coft Token , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_VISA_COFT_PAR_DISABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", VISA_COFT_TOKEN)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference", "V0010013021361288827541710336")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(VISA_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0336");
        cardTokenInfo.setTavv("AgAAAAoAPd52XQkAmeNXghMAAAA");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541710336");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        //Refund with incorrect product id
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
    @Test(description = "Custom Checkout with RUPAY External Coft Token , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_RUPAY_COFT_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKCUSTOMNEW00011";
        //String orderId = CommonHelpers.generateOrderId();
        String orderId = "4b7066b3e24144f487bc283b5f0f75c3";
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "RUPAY")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", RUPAY_COFT_TOKEN)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference", "V0010013021361288827541727409")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with VISA External Coft Token , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_VISA_COFT_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", VISA_COFT_TOKEN)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference", "V0010013021361288827541710336")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(VISA_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0336");
        cardTokenInfo.setTavv("AgAAAAoAPd52XQkAmeNXghMAAAA");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541710336");
        cardTokenInfo.setTokenType("COFT");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        //Refund with Duplicate Items
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refundItems = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\"},{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\"}]";
        String refId = CommonHelpers.generateOrderId();
        String bodyforChecksum = SyncRefund.bodyWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        String checksum= PGPUtil.getChecksum(mid.getKey(),bodyforChecksum);
        String body = SyncRefund.requestWithItems.replace("{MID}", mid.getId()).replace("{TRANSACTION_AMOUNT}", String.valueOf(payableAmountItem1 + payableAmountItem1)).replace("{ORDER_ID}", txnStatus.getResponse().getORDERID()).replace("{SIGNATURE}", checksum).replace("{TRANSACTION_ID}", txnStatus.getResponse().getTXNID()).replace("{ref_id}", refId).replace("{refund_items}",refundItems);
        SyncRefund syncRefund =  new SyncRefund(body);
        JsonPath asyncRefundResp = syncRefund.execute().jsonPath();
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultStatus")).isEqualTo("TXN_FAILURE");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultCode")).isEqualTo("600");
        Assertions.assertThat(asyncRefundResp.getString("body.resultInfo.resultMsg")).isEqualTo("Invalid refund request.");
        Assertions.assertThat(asyncRefundResp.getString("body.refundAmount")).isEqualTo(Double.toString(payableAmountItem1 + payableAmountItem1));
    }


    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with RUPAY Alt Token , new flow with PAR config disable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_RUPAY_ALT_PAR_DISABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "RUPAY")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", RUPAY_COFT_TOKEN)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference", "V0010013021361288827541727409")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("ALTERNATE");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultCode")).isEqualTo("0001");
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultMsg")).isEqualTo("Alt ID Transaction via Token for Rupay is not Supported");
    }


    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with RUPAY Alt Token , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_RUPAY_ALT_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "RUPAY")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", RUPAY_COFT_TOKEN)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference", "V0010013021361288827541727409")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(RUPAY_COFT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("0001");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V0010013021361288827541727409");
        cardTokenInfo.setTokenType("ALTERNATE");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("RUPAY")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultCode")).isEqualTo("0001");
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultMsg")).isEqualTo("Alt ID Transaction via Token for Rupay is not Supported");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout with VISA Alt Token , new flow with PAR config enable - EMI and Bank offers Applied, Item Based")
    public void testCustomCheckoutNewFlow_EMI_BO_VISA_ALT_PAR_ENABLE() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_ENABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKCUSTOMNEW00015";
        //String orderId = CommonHelpers.generateOrderId();
        String orderId = "754a4e0e6fe747be97abaed220bc7d47";
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.cardToken", VISA_ALT_TOKEN)
                .setContext("body.paymentDetails.paymentOptions[0].cardTokenInfo.panUniqueReference", "V001001402220600348758669991")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        CardTokenInfo cardTokenInfo = new CardTokenInfo();
        cardTokenInfo.setCardToken(VISA_ALT_TOKEN);
        cardTokenInfo.setTokenExpiry(expiry);
        cardTokenInfo.setCardSuffix("2363");
        cardTokenInfo.setTavv("AgAAAAAKh8QqrzAABFcgm0AAAA=");
        cardTokenInfo.setPanUniqueReference("V001001402220600348758669991");
        cardTokenInfo.setTokenType("ALTERNATE");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("EMI")
                .setCardInfo("||618|")
                .setAuthMode("otp")
                .setcardTokenInfo(cardTokenInfo)
                .setChannelCode("VISA")
                .setTxnAmount(null)
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }



    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Multiple Items , new flow - BO and Subvention Applied")
    public void testCustomCheckoutNewFlow_MultiItem_Subvention_And_BO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        SimplifiedUnifiedOffers.Items item3 = new SimplifiedUnifiedOffers.Items("Item003_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item4 = new SimplifiedUnifiedOffers.Items("Item004_" + orderId, "55005", "18260", 1100.00, "78225");
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
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");

        // Refund for 1 already Refunded Item and 1 not Refunded Item
        Double payableAmountItem1 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item001_"+orderId+"' }.payableAmount");
        Double payableAmountItem2 = jsonPath.getDouble("body.paymentDetails[0].tenureDetails[0].items.find { it.id == 'Item002_"+orderId+"' }.payableAmount");
        String refundItems1 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\"}]";
        String refundItems2 = "[{\"itemId\":\"Item001_"+orderId+"\",\"productId\":\"123047\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"55005\"}]";
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
    @Test(description = "Custom Checkout for Single Items , new flow - BO and Subvention Applied")
    public void testCustomCheckoutNewFlow_SingleItem_Subvention_And_BO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        //String custId = user.custId();
        String custId = "MOCKCUSTOMNEW00017";
        //String orderId = CommonHelpers.generateOrderId();
        String orderId = "44b72ace4425488088678d2e89edba88";
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Multi Item , new flow - Only BO CC Txn")
    public void testCustomCheckoutNewFlow_MultiItem_NoSubvention_And_BO_CCTxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 1100.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
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
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }
    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Single Item , new flow - Only BO CC Txn")
    public void testCustomCheckoutNewFlow_SingleItem_NoSubvention_And_BO_CCTxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 1100.00, "6226");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
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
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Multi Item , new flow - Only Subvention Applied ")
    public void testCustomCheckoutNewFlow_MultiItem_Subvention_And_NoBO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6223");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123046", "18084", 1100.00, "16225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Single Item , new flow - Only Subvention Applied ")
    public void testCustomCheckoutNewFlow_SingleItem_Subvention_And_NoBO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6223");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).isNull();
    }


    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Multiple Items , new flow - Only BO Standard EMI Txn")
    public void testCustomCheckoutNewFlow_MultiItem_NoSubvention_And_BO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6224");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "123046", "18084", 1100.00, "6225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 2200.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Single Items , new flow - Only BO Standard EMI Txn")
    public void testCustomCheckoutNewFlow_SingleItem_NoSubvention_And_BO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123045", "18084", 1100.00, "6224");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.items", items)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Amount based BO and multiple item based subvention , new flow ")
    public void testCustomCheckoutNewFlow_MultiItem_ItemBasedSubvention_And_AmountBasedBO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "92226", 400.00, "186414");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47178", "92226", 400.00, "186416");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Amount based BO and single item based subvention , new flow ")
    public void testCustomCheckoutNewFlow_SingleItem_ItemBasedSubvention_And_AmountBasedBO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47178", "92226", 400.00, "186416");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }



    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for multiple item based BO and Amount based subvention , new flow ")
    public void testCustomCheckoutNewFlow_MultiItem_AmountBasedSubvention_And_ItemBasedBO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "1707", 400.00, "86414");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "47176", "1707", 400.00, "86414");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.0)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for single item based BO and Amount based subvention , new flow ")
    public void testCustomCheckoutNewFlow_SingleItem_AmountBasedSubvention_And_ItemBasedBO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "47176", "1707", 400.00, "86414");
        items.add(item1);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.00)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }


    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for Amount based BO and Amount based subvention , new flow ")
    public void testCustomCheckoutNewFlow_AmountBasedSubvention_And_AmountBasedBO_EMITxn() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_CUSTOM_DISABLE;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        String orderId = CommonHelpers.generateOrderId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 800.0)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI")
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId(pgPlanId)
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
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).contains(pgPlanId);
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for 6 digit Bin offers, new flow - CC Txn")
    public void testCustomCheckoutNewFlow_6digitBin() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_6DIGIT_BIN;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        //String custId = "MOCKMULTI0001";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.00)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", null)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null);
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
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Test(description = "Custom Checkout for 8 digit Bin offers, new flow - CC Txn")
    public void testCustomCheckoutNewFlow_8digitBin() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_8DIGIT_BIN;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId(); 
        //String custId = "MOCKMULTI0001";
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", 400.00)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", null)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", VISA_CC_CARD_NO)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "CREDIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].tenure", null);
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
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
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
                .validatePaymentMode("CC")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();
        Assertions.assertThat(txnStatus.getResponse().getEmiSubventionInfo()).isNull();
        Assertions.assertThat(txnStatus.getResponse().getPaymentPromoCheckoutData()).contains("savings");
    }

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-57206")
    @Test(description = "Verify E2E txn Success and Refund with Items context sent for Multiple Items where No Offer applied on any items - CC")
    public void testCustomCheckoutNewFlow_No_Offer_applied_on_any_item_CC_and_refund() throws Exception {
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
                .setCardInfo("|" + VISA_CC_CARD_NO + "|111|122025")
                .setAuthMode("otp")
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

    @Owner(MEHUL_GUPTA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn on UPI and refund with duplicate items")
    public void testCustomCheckoutNewFlow_MultiItem_UPI() throws Exception {
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
                .setContext("body.paymentDetails.paymentOptions[0].vpa", "paytm.uat@axis")
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
                .setPayerAccount("paytm.uat@axis")
                .setCardInfo(null)
                .setAuthMode("USRPWD")
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
    @Owner(RONIKA)
    @Feature("PGP-57823")
    @Test(description = "Verify Deals flow txn is migrated to v2checkout")
    public void verifyDealsFlowTxnIsMigratedToV2Checkout() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.DEALS_FLOW_MID;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();

        String txnAmount="212";

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setItems(Collections.singletonList(new Item("fn9g8DxZoPUtOzOw/Q1oUrZvTRyckXT2veG3zhKlxp", "571366", "166091", "1234580979", 212.0, 1218.0)))
                .setPaymentDetails(new PaymentDetails(212.0,
                        Collections.singletonList(new PaymentOption(true, false, 
                            "ICICI", 0.0, "ICICI", 
                            "NET_BANKING", 0.0, "212.0", 
                            null, null, null, null, null, 
                            Collections.singletonList(new Tenure(3, "MONTH"))))))
                .setPromoContext(new PromoContext("{\"items\":{\"fn9g8DxZoPUtOzOw/Q1oUrZvTRyckXT2veG3zhKlxp\":{\"brandId\":\"571366\",\"categoryId\":\"166091\",\"merchantId\":\"63795704\",\"price\":\"212.00\",\"productId\":\"1234580979\",\"verticalId\":\"1218\"}}}",
                "{\"paymentFlow\":\"DEAL_FLOW\"}"))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);
        String unifiedOffersToken = response.jsonPath().getString("body.unifiedOffersToken");
        String requestId = response.jsonPath().getString("head.requestId");
        
        Good goodInfo = new Good();
        goodInfo.setMerchantShippingId("1349571105132");
        goodInfo.setMerchantGoodsId("1234580979");
        goodInfo.setDescription("PUREO34");
        goodInfo.setQuantity("1");
        goodInfo.setCategory("Paytm Rewards/166091/paytmrewards/1218");
        goodInfo.setSnapshotUrl("https://catalog-staging.paytm.com/v1/mobile/*******/1234580979?visibility_originoffline&merchant_id63795704");

        // Set up Price
        Price price = new Price();
        price.setValue("21200");
        price.setCurrency("INR");
        goodInfo.setPrice(price);

        ContriInfo contriInfo = new AffordabilityDetails.ContriInfo();
        contriInfo.setMerchant(0.2);

        AffordabilityDetails.Settlement settlement = new AffordabilityDetails.Settlement();
        settlement.setRedemptionType("pfp");
        settlement.setActionType("givePaytmFirstPoints");
        settlement.setContriInfo(contriInfo);

        AffordabilityDetails affordabilityDetails = new AffordabilityDetails();
        affordabilityDetails.setSettlement(Collections.singletonList(settlement));

        DisablePaymentMode[] disablePaymentModes = new DisablePaymentMode[] {
                new DisablePaymentMode(new String[]{}, "PAY_AT_COUNTER"),
                new DisablePaymentMode(new String[]{}, "COD"),
                new DisablePaymentMode(new String[]{}, "ESCROW")
            };

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setUnifiedOffersToken(unifiedOffersToken)
                .setChannelId("WAP")
                .setTxnValue(response.jsonPath().getString("body.paymentDetails[0].offerDetails[0].originalAmount"))
                .setDisablePaymentMode(disablePaymentModes)
                .setWebsiteName("MarketplaceBeta")
                .setCardTokenRequired("true")
                .setUltimateBeneficiaryDetails(new UltimateBeneficiaryDetails("123Paytm"))
                .setCustId(custId)
                .setPayableAmount(new TxnAmount(response.jsonPath().getString("body.paymentDetails[0].offerDetails[0].payableAmount")))
                .build();
        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});
        initTxnDTO.getBody().setAffordabilityInfo(new AffordabilityInfo("DEAL_FLOW"));

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("NET_BANKING")
                .setChannelCode("ICICI")
                .setRiskExtendInfo("appVersion:10.52.0|businessFlow:DEFFER_CHECKOUT|channelId:WAP|deviceId:***********************************:OnePlus|deviceModel:HD1901|deviceType:Mobile|isRooted:false|language:en-IN|networkType:WIFI|operationType:PAYMENT|osType:Android|osVersion:12|platform:APP|playstore:true|screenResolution:1080x2400|simSubscriptionId:5|timeZone:GMT05:30|userLBSLatitude:28.4512724|userLBSLongitude:77.0912838|versionCode:39438|")
                .setChannelId("WAP")
                .setOrderId(initTxnDTO.orderFromBody())
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).fillAndSubmitJsonForm(jsonForm);

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
                .validateTxnAmount(response.jsonPath().getString("body.paymentDetails[0].offerDetails[0].payableAmount"))
                .AssertAll();

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,requestId,"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).contains("\"affordabilityInfo\":{\"paymentFlow\":\"DEAL_FLOW\"}");
        String logs1 = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "REQUEST");
        Assertions.assertThat(logs1).contains("\"orderType\":\"DEALS_ORDER\"");
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-58897")
    @Test(description = "Verify UPI Offers getting applied in VPA flow")
    public void VerifyUPIOffersGettingAppliedInVPAScenarioUPICollectItembased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_UPI_OFFERS_VPA_MID;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = "EMITEST"+CommonHelpers.generateOrderId();
        String orderId = CommonHelpers.generateOrderId();

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setAmountBasedSubvention(null)
                .setItems(Collections.singletonList(new Item("MItemsu1009", "18084", "6224", "123", 800.0, 0.0)))
                .setPaymentDetails(new PaymentDetails(800.0, Collections.singletonList(new PaymentOption(true, false,null, 0.0, null, "UPI", 0.0, null, null, null, null, "srivastavaprateek@paytm", null, null))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);
        String unifiedOffersToken = response.jsonPath().getString("body.unifiedOffersToken");
        String requestId = response.jsonPath().getString("head.requestId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setOrderId(orderId)
                .setUnifiedOffersToken(unifiedOffersToken)
                .setTxnValue("800.0")
                .setCustId(custId)
                .setPayableAmount(new TxnAmount("720"))
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("UPI")
                .setPayerAccount("srivastavaprateek@paytm")
                .setCardInfo(null)
                .setAuthMode("USRPWD")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).fillAndSubmitJsonForm(jsonForm);

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
                .validateTxnDate(new Date());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,requestId,"v1/offerApply", "RESPONSE");
        Assertions.assertThat(logs).contains("discount applied successfully.");
        String payMethodString = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "RESPONSE");
        Assertions.assertThat(payMethodString).contains("\"payMethod\":\"UPI\"");
        
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-58897")
    @Test(description = "Verify UPI Offers getting applied in without VPA flow")
    public void VerifyUPIOffersGettingAppliedInWithoutVPAScenarioUPICollectItembased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_UPI_OFFERS_VPA_MID;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = "EMITEST"+CommonHelpers.generateOrderId();
        String orderId = CommonHelpers.generateOrderId();

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setAmountBasedSubvention(null)
                .setItems(Collections.singletonList(new Item("MItemsu1009", "18084", "6224", "123", 800.0, 0.0)))
                .setPaymentDetails(new PaymentDetails(800.0, Collections.singletonList(new PaymentOption(true, false, null, 0.0, null,  "UPI", 0.0, null, null, null, null, null, null, null))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);
        String unifiedOffersToken = response.jsonPath().getString("body.unifiedOffersToken");
        String requestId = response.jsonPath().getString("head.requestId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setOrderId(orderId)
                .setUnifiedOffersToken(unifiedOffersToken)
                .setTxnValue("800.0")
                .setCustId(custId)
                .setPayableAmount(new TxnAmount("720"))
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("UPI")
                .setPayerAccount("srivastavaprateek@paytm")
                .setCardInfo(null)
                .setAuthMode("USRPWD")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).fillAndSubmitJsonForm(jsonForm);

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
                .validateTxnDate(new Date());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,requestId,"v1/offerApply", "RESPONSE");
        Assertions.assertThat(logs).contains("discount applied successfully.");
        String payMethodString = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "RESPONSE");
        Assertions.assertThat(payMethodString).contains("\"payMethod\":\"UPI\"");
        
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-58897")
    @Test(description = "Verify UPI Offers getting applied in without VPA flow")
    public void VerifyUPIOffersGettingAppliedInWithoutVPAScenarioUPICollectAmountBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_UPI_OFFERS_VPA_MID;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = "EMITEST"+CommonHelpers.generateOrderId();
        String orderId = CommonHelpers.generateOrderId();

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setAmountBasedSubvention(null)
                .setAmountBasedBankOffer(true)
                .setPaymentDetails(new PaymentDetails(800.0, Collections.singletonList(new PaymentOption(true, false,     null, 0.0, null,  "UPI", 0.0, null, null, null, null, null, null, null))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);
        String unifiedOffersToken = response.jsonPath().getString("body.unifiedOffersToken");
        String requestId = response.jsonPath().getString("head.requestId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setOrderId(orderId)
                .setUnifiedOffersToken(unifiedOffersToken)
                .setTxnValue("800.0")
                .setCustId(custId)
                .setPayableAmount(new TxnAmount("720"))
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("UPI")
                .setPayerAccount("srivastavaprateek@paytm")
                .setCardInfo(null)
                .setAuthMode("USRPWD")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).fillAndSubmitJsonForm(jsonForm);

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
                .validateTxnDate(new Date());

        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,requestId,"v1/offerApply", "RESPONSE");
        Assertions.assertThat(logs).contains("discount applied successfully.");
        String payMethodString = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "RESPONSE");
        Assertions.assertThat(payMethodString).contains("\"payMethod\":\"UPI\"");
        
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-58897")
    @Test(description = "Verify UPI Offers getting applied in without VPA flow")
    public void VerifyUPIOffersGettingAppliedInWithoutVPAScenarioUPIIntentItembased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_UPI_OFFERS_VPA_MID;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = "EMITEST"+CommonHelpers.generateOrderId();
        String orderId = CommonHelpers.generateOrderId();

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setAmountBasedSubvention(null)
                .setItems(Collections.singletonList(new Item("MItemsu1009", "18084", "6224", "123", 800.0, 0.0)))
                .setPaymentDetails(new PaymentDetails(800.0, Collections.singletonList(new PaymentOption(true, false,  null, 0.0, null,     "UPI", 0.0, null, null, null, null, null, null, null))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);
        String unifiedOffersToken = response.jsonPath().getString("body.unifiedOffersToken");
        String requestId = response.jsonPath().getString("head.requestId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setOrderId(orderId)
                .setUnifiedOffersToken(unifiedOffersToken)
                .setTxnValue("800.0")
                .setCustId(custId)
                .setPayableAmount(new TxnAmount("720"))
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("UPI_INTENT")
                .setPayerAccount("srivastavaprateek@paytm")
                .setCardInfo(null)
                .setAuthMode("USRPWD")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,requestId,"v1/offerApply", "RESPONSE");
        Assertions.assertThat(logs).contains("discount applied successfully.");
        String payMethodString = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "RESPONSE");
        Assertions.assertThat(payMethodString).contains("\"payMethod\":\"UPI\"");
        
    }

    @Owner(LOKESH_SAXENA)
    @Feature("PGP-58897")
    @Test(description = "Verify UPI Offers getting applied in without VPA flow")
    public void VerifyUPIOffersGettingAppliedInWithoutVPAScenarioUPIIntentAmountbased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_REG_UPI_OFFERS_VPA_MID;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = "EMITEST"+CommonHelpers.generateOrderId();
        String orderId = CommonHelpers.generateOrderId();

        OfferApplyDTO offerApply = new OfferApplyDTO.Builder(ssoToken, mid.getId())
                .setCustId(custId)
                .setAmountBasedSubvention(null)
                .setAmountBasedBankOffer(true)
                .setPaymentDetails(new PaymentDetails(800.0, Collections.singletonList(new PaymentOption(true, false, null, 0.0, null, "UPI", 0.0, null, null, null, null, null, null, null))))
                .build();
        Response response = NativeHelpers.Validate_OfferApply(offerApply);
        String unifiedOffersToken = response.jsonPath().getString("body.unifiedOffersToken");
        String requestId = response.jsonPath().getString("head.requestId");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid)
                .setOrderId(orderId)
                .setUnifiedOffersToken(unifiedOffersToken)
                .setTxnValue("800.0")
                .setCustId(custId)
                .setPayableAmount(new TxnAmount("720"))
                .setRequestType("PAYMENT")
                .setWebsiteName("retail")
                .setCallbackUrl("https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse")
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("UPI_INTENT")
                .setPayerAccount("srivastavaprateek@paytm")
                .setCardInfo(null)
                .setAuthMode("USRPWD")
                .build();
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        JsonPath jsonPath1 = new JsonPath(jsonForm);
        Assertions.assertThat(jsonPath1.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.THEIA_REQ_RESP,requestId,"v1/offerApply", "RESPONSE");
        Assertions.assertThat(logs).contains("discount applied successfully.");
        String payMethodString = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"v2/order/checkout", "RESPONSE");
        Assertions.assertThat(payMethodString).contains("\"payMethod\":\"UPI\"");
    }

    //Bajaj Finserv test cases
    @Owner(PUSPA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn for Bajaj Finserv Cardless")
    public void testCustomCheckoutforEmiCardless_ItemBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.BAJAJFN_CARDLESS;
        User user = userManager.getForRead(Label.BASIC);
        String orderAmount="10005";
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        String orderId = CommonHelpers.generateOrderId();
        List<SimplifiedUnifiedOffers.Items> items = new ArrayList<>();
        SimplifiedUnifiedOffers.Items item1 = new SimplifiedUnifiedOffers.Items("Item001_" + orderId, "123047", "18084", 5002.00, "6226");
        SimplifiedUnifiedOffers.Items item2 = new SimplifiedUnifiedOffers.Items("Item002_" + orderId, "55005", "18260", 5003.00, "78225");
        items.add(item1);
        items.add(item2);
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", orderAmount)
                .setContext("body.items", items)
                .setContext("body.amountBasedSubvention", false)
                .setContext("body.amountBasedBankOffer", false)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "BAJAJFN")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", BAJAJFN_CARDLESS_CARD)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_CARDLESS");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = orderAmount;
        String payableAmount = orderAmount;
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI_CARDLESS")
                .setCardInfo("|" + BAJAJFN_CARDLESS_CARD + "||")
                .setAuthMode("otp")
                .setChannelCode("BAJAJFN")
                .setEmiType("NBFC")
                .setPlanId(pgPlanId)
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
                .validatePaymentMode("EMI_CARDLESS")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();

        Float refundAmount = Float.parseFloat(txnStatus.getResponse().getTXNAMOUNT());
        String refId = CommonHelpers.generateOrderId();
        String refundItems = "[{\"itemId\":\"Item001_" + orderId +"\",\"productId\":\"123047\"},{\"itemId\":\"Item002_"+orderId+"\",\"productId\":\"55005\"}]";
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
    @Owner(PUSPA)
    @Feature("PGP-52785")
    @Test(description = "Verify E2E Multi Item Txn for Bajaj Finserv Cardless")
    public void testCustomCheckoutforEmiCardless_Amountbased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.BAJAJFN_CARDLESS;
        User user = userManager.getForRead(Label.BASIC);
        String orderAmount="10005";
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_item_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paymentDetails.orderAmount", orderAmount)
                .setContext("body.items", null)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "BAJAJFN")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", BAJAJFN_CARDLESS_CARD)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI_CARDLESS");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = orderAmount;
        String payableAmount = orderAmount;
        String pgPlanId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId(), txnToken, initTxnDTO.orderFromBody()).
                setPaymentMode("EMI_CARDLESS")
                .setCardInfo("|" + BAJAJFN_CARDLESS_CARD + "||")
                .setAuthMode("otp")
                .setChannelCode("BAJAJFN")
                .setEmiType("NBFC")
                .setPlanId(pgPlanId)
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
                .validatePaymentMode("EMI_CARDLESS")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateTxnAmount(payableAmount)
                .AssertAll();


    }
}