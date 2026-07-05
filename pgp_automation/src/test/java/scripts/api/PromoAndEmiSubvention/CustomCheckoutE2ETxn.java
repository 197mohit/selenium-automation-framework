package scripts.api.PromoAndEmiSubvention;
import com.paytm.LocalConfig;
import com.paytm.api.GetPaymentStatus;
import com.paytm.api.TxnStatus;
import com.paytm.api.theia.PromoAndEmiSubvention.OfferApply;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.TxnAmount;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.NativePlusHoldpayPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.*;

import static com.paytm.appconstants.Constants.Owner.KARMVIR;
import static com.paytm.appconstants.Constants.Owner.SHWETANK;
import static com.paytm.appconstants.Constants.Owner.AKSHAT_NAYAK;
public class CustomCheckoutE2ETxn  extends PGPBaseTest {
    String emi_body_with_tenure = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"271178ec-54ed-4522-9d72-799899a53100\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
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
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020011\",\n" +
            "                \"brandId\": \"327\",\n" +
            "                \"categoryId\": \"3271\",\n" +
            "                \"price\": 2000.25,\n" +
            "                \"quantity\": 1,\n" +
            "                \"offerDetails\": {\n" +
            "                    \"emiOfferDetails\": {\n" +
            "                        \"offerId\": \"2164614\"\n" +
            "                    },\n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2415381\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardNo\": \"4718650100010336\",\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 6,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String emi_amount_based_with_tenure = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"271178ec-54ed-4522-9d72-799899a53100\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"true\",\n" +
            "        \"amountBasedSubvention\": \"true\",\n" +
            "        \"offerDetails\": {\n" +
            "            \"emiOfferDetails\": {\n" +
            "                \"offerId\": \"2141488\"\n" +
            "            },\n" +
            "            \"bankOfferDetails\": [\n" +
            "                {\n" +
            "                    \"offerId\": \"2151610\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"EMI\",\n" +
            "                    \"cardNo\": \"4718650100010336\",\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 6,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String credit_card_txn = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"271178ec-54ed-4522-9d72-799899a53100\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"false\",\n" +
            "        \"amountBasedSubvention\": \"false\",\n" +
            "        \"items\": [\n" +
            "            {\n" +
            "                \"id\": \"123\",\n" +
            "                \"productId\": \"8903287020011\",\n" +
            "                \"brandId\": \"327\",\n" +
            "                \"categoryId\": \"3271\",\n" +
            "                \"price\": 2000.25,\n" +
            "                \"quantity\": 1,\n" +
            "                \"offerDetails\": {\n" +
            "                    \"bankOfferDetails\": [\n" +
            "                        {\n" +
            "                            \"offerId\": \"2415381\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            }\n" +
            "        ],\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"CREDIT_CARD\",\n" +
            "                    \"cardNo\": \"4761360075860626\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
    String credit_card_txn_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"271178ec-54ed-4522-9d72-799899a53100\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
            "        \"amountBasedBankOffer\": \"true\",\n" +
            "        \"amountBasedSubvention\": \"true\",\n" +
            "        \"offerDetails\": {\n" +
            "            \"bankOfferDetails\": [\n" +
            "                {\n" +
            "                    \"offerId\": \"2151610\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"CREDIT_CARD\",\n" +
            "                    \"issuingBank\": \"\",\n" +
            "                    \"vpa\": \"\",\n" +
            "                    \"cardNo\": \"4718650100010336\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
     String debit_card_txn_amount_based = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WAP\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"271178ec-54ed-4522-9d72-799899a53100\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"mid\": \"qa12FU97229952596781\",\n" +
            "        \"custId\": \"1000177185\",\n" +
             "        \"amountBasedBankOffer\": \"true\",\n" +
             "        \"amountBasedSubvention\": \"true\",\n" +
            "        \"offerDetails\": {\n" +
            "            \"bankOfferDetails\": [\n" +
            "                {\n" +
            "                    \"offerId\": \"2151610\"\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": 2000.25,\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"payMethod\": \"DEBIT_CARD\",\n" +
            "                    \"cardNo\": \"4444333322221111\"\n" +
            "                }\n" +
            "            ]\n" +
            "        }\n" +
            "    }\n" +
            "}";
     String promoEMIRequest="{\n" +
             "    \"head\": {\n" +
             "        \"channelId\": \"WEB\",\n" +
             "        \"tokenType\": \"SSO\",\n" +
             "        \"token\": \"f79ca04f-be5a-4245-8c23-62d532131600\"\n" +
             "    },\n" +
             "    \"body\": {\n" +
             "        \"mid\": \"qa12FU97229952596781\",\n" +
             "        \"custId\": \"1000002621\",\n" +
             "        \"paytmUserId\": \"1000002621\",\n" +
             "        \"amountBasedBankOffer\": \"true\",\n" +
             "        \"amountBasedSubvention\": \"true\",\n" +
             "        \"items\": null,\n" +
             "        \"offerDetails\": {\n" +
             "                   \n" +
             "                    \"bankOfferDetails\": [\n" +
             "                        {\n" +
             "                            \"offerId\": \"2151610\"\n" +
             "                        }\n" +
             "                    ]\n" +
             "                },\n" +
             "        \"paymentDetails\": {\n" +
             "            \"orderAmount\": 500.00,\n" +
             "            \"paymentOptions\": [\n" +
             "                {\n" +
             "             \"transactionAmount\": 500,\n" +
             "            \"payMethod\": \"EMI\",\n" +
             "            \"issuingBank\": \"HDFC\",\n" +
             "            \"issuingNetworkCode\": \"VISA\",\n" +
             "            \"cardNo\":\"4718650100010336\",\n" +
             "            \"tenure\": [\n" +
             "                        {\n" +
             "                            \"value\": 3,\n" +
             "                            \"unit\": \"MONTH\"\n" +
             "                        }\n" +
             "                    ]\n" +
             "                }\n" +
             "            ]\n" +
             "        }\n" +
             "    }\n" +
             "}\n" +
             "\n" +
             "\n";

    String offerApplyBajajEmiCardlessRequest = "{\n" +
            "    \"head\": {\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"tokenType\": \"SSO\",\n" +
            "        \"token\": \"0e1dnj114uhy3j25xogfj3qu4mt69pqh6080\"\n" +
            "    },\n" +
            "    \"body\": {\n" +
            "        \"custId\": \"1000036031\",\n" +
            "        \"paymentDetails\": {\n" +
            "            \"orderAmount\": \"1000\",\n" +
            "            \"paymentOptions\": [\n" +
            "                {\n" +
            "                    \"subventionAmount\": \"1000\",\n" +
            "                    \"applyBankOffer\": true,\n" +
            "                    \"applySubvention\": true,\n" +
            "                    \"payMethod\": \"EMI_CARDLESS\",\n" +
            "                    \"issuingBank\": \"BAJAJFN\",\n" +
            "                    \"cardNo\": \"2030400291909002\",\n" +
            "                    \"issuingNetworkCode\": \"BAJAJFN\",\n" +
            "                    \"tenure\": [\n" +
            "                        {\n" +
            "                            \"value\": 3,\n" +
            "                            \"unit\": \"MONTH\"\n" +
            "                        }\n" +
            "                    ]\n" +
            "                }\n" +
            "            ]\n" +
            "        },\n" +
            "        \"amountBasedBankOffer\": true,\n" +
            "        \"amountBasedSubvention\": true,\n" +
            "        \"mid\": \"qa12id29388530533353\"\n" +
            "    }\n" +
            "}";

    private static final String JSON_POST_URL = "/checkoutpage/new_nplus_page.jsp?env="+ LocalConfig.ENV_NAME;
    private final NativePlusHoldpayPage nativePlusHoldpayPage = new NativePlusHoldpayPage();
    private static final String theme = "enhancedweb";
    private static final String DISABLED_PAYMENT_MODE_ERROR_MSG = "{paymentMode} is not allowed for this transaction, kindly use some other payment mode";
    private static final String postConvFlag = "";
    private void submitProcessTxnResponseFromReq(ProcessTxnV1Request processTxnV1Request) {
        String jsonForm = QRHelper.executeProcessTransactionV1(processTxnV1Request);
        nativePlusHoldpayPage.
                launch(LocalConfig.MOCK_HOST + JSON_POST_URL).
                fillAndSubmitJsonForm(jsonForm);
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete CC txn with cashback offer item based")
    public void TestE2ESuccessResponseWhenCCPaymethodWithCashbackOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CREDIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete CC txn with discount offer item based without custid")
    public void TestE2ESuccessResponseWhenCCPaymethodWithDiscountOfferItembased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.custId")
                .setContext("head.token", ssoToken)
                .setContext("body.items[0].productId", "8903287020001")
                .setContext("body.mid",mid.getId())
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CREDIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }
    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete CC txn without offer item based")
    public void TestE2ESuccessResponseWhenCCPaymethodWithoutOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.items[0].brandId","579580")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CREDIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }


    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete CC txn with discount offer amount based")
    public void TestE2ESuccessResponseWhenCCPaymethodWithDiscountOfferAmountbased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CREDIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete CC txn with cashback offer amount based without custid ")
    public void TestE2ESuccessResponseWhenCCPaymethodWithCashbackOfferAmountbased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.custId")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId", "2415381")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.paymentDetails.orderAmount",2000.25);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CREDIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete DC txn with discount offer amount based")
    public void TestE2ESuccessResponseWhenDCPaymethodWithDiscountOfferAmountbased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(debit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId", "2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DEBIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete DC txn with cashback offer amount based")
    public void TestE2ESuccessResponseWhenDCPaymethodWithCashbackOfferAmountbased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(debit_card_txn_amount_based)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId", "2151744")
                .setContext("head.token", ssoToken)
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.mid",mid.getId());
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DEBIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete DC txn without offer amount based")
    public void TestE2ESuccessResponseWhenDCPaymethodWithoutOfferAmountbased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(debit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.mid",mid.getId());
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DEBIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }
    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete DC txn with cashback offer item based")
    public void TestE2ESuccessResponseWhenDCPaymethodWithCashbackOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", "4444333322221111")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DEBIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete DC txn with discount offer item based without custid")
    public void TestE2ESuccessResponseWhenDCPaymethodWithDiscountOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId=user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "DEBIT_CARD")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", "4444333322221111")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD).build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DEBIT_CARD")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete Zero Cost Subvention EMI txn with cashback offer item based")
    public void TestE2ESuccessResponseWhenEMIPaymethodWithCashbackOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify Low Cost Subvention EMI txn with discount offer item based without custid")
    public void TestE2ESuccessResponseWhenEMIPaymethodWithDiscountOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify Zero Cost Subvention EMI txn with discount offer amount based")
    public void TestE2ESuccessResponseWhenEMIPaymethodWithDiscountOfferAmountBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify Low Cost Subvention EMI txn with cashback offer amount based without custId")
    public void TestE2ESuccessResponseWhenEMIPaymethodWithCashbackOfferAmountBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151744");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify Zero Cost Subvention EMI txn without bankoffer  item based")
    public void TestE2ESuccessResponseWhenEMIPaymethodWithoutOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.items[0].offerDetails.bankOfferDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].price",2000.25)
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001");;
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify Low cost Subvention EMI txn without bankoffer amount based")
    public void TestE2ESuccessResponseWhenEMIPaymethodWithoutOfferAmountBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete EMI txn with cashback bankoffer  item based")
    public void TestE2ESuccessResponseWhenStandardEMIPaymethodWithCashbackOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.items[0].offerDetails.emiOfferDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete EMI txn with Discount bankoffer  item based")
    public void TestE2ESuccessResponseWhenStandardEMIPaymethodWithDiscountOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.items[0].offerDetails.emiOfferDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2415381")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete  EMI txn with Discount bankoffer amount based")
    public void TestE2ESuccessResponseWhenStandardEMIPaymethodWithDiscountOfferAmountBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.emiOfferDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }
    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete EMI txn without  bankoffer  item based")
    public void TestE2ESuccessResponseWhenStandardEMIPaymethodWithoutOfferItemBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.items[0].offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2164614")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020011");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }
    @Owner(SHWETANK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46204")
    @Test(description = "Verify complete  EMI txn without  bankoffer amount based")
    public void TestE2ESuccessResponseWhenStandardEMIPaymethodWithoutOfferAmountBased(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete  NB txn with discounted bank offer amount based")
    public void TestE2ESuccessResponseWhenDiscountedbankOfferAppliedforNBTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_NEW_FLOW;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2407858")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.orderAmount",400.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_NEW_FLOW, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM","RESPONSE");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete  NB txn with cashback bank offer amount based")
    public void TestE2ESuccessResponseWhenCashbackbankOfferAppliedforNBTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.orderAmount",1100.00)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2417563");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(payableAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete NB txn with best bank offer amount based")
    public void TestE2ESuccessResponseWhenBestbankOfferAppliedforNBTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete NB txn with ItemBased bank offer ")
    public void TestE2ESuccessResponseWhenItembasedbankOfferAppliedforNBTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","18084")
                .setContext("body.items[0].categoryId","6224")
                .setContext("body.items[0].price","1100")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete UPI txn with discounted bank offer amount based")
    public void TestE2ESuccessResponseWhenDiscountedbankOfferAppliedforUPITxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2417648")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","test@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete  UPI txn with cashback bank offer amount based")
    public void TestE2ESuccessResponseWhenCashbackbankOfferAppliedforUPITxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","test@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .setContext("body.paymentDetails.orderAmount",1100.00)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2417651");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(payableAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete UPI txn with best bank offer amount based")
    public void TestE2ESuccessResponseWhenBestbankOfferAppliedforUPITxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","test@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete UPI txn with ItemBased bank offer ")
    public void TestE2ESuccessResponseWhenItembasedbankOfferAppliedforUPITxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","10001")
                .setContext("body.items[0].price","1100")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","test@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete Wallet txn with discounted bank offer amount based")
    public void TestE2ESuccessResponseWhenDiscountedbankOfferAppliedforWalletTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user,1200.0);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2417648")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","BALANCE")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete Wallet txn with cashback bank offer amount based")
    public void TestE2ESuccessResponseWhenCashbackbankOfferAppliedforWalletTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1200.0);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","BALANCE")
                .setContext("body.paymentDetails.orderAmount",1100.00)
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2417563");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateTxnAmount(payableAmount)
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete Wallet txn with best bank offer amount based")
    public void TestE2ESuccessResponseWhenBestbankOfferAppliedforWalletTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1200.0);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","BALANCE")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-47712")
    @Test(description = "Verify complete wallet txn with ItemBased bank offer ")
    public void TestE2ESuccessResponseWhenItembasedbankOfferAppliedforWalletTxn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,1200.0);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","10001")
                .setContext("body.items[0].price","1100")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","BALANCE")
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.BALANCE)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify emisubventioninfo and paymentPromoCheckoutData objects in cop for amount based subvention and promo")
    public void validateEMISubventionInfoForAmountBasedAndPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|6")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);
        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);


        String planI = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertNotNull(planI);
        Assert.assertEquals(subventionAmount,"2000.25");
        Assert.assertEquals(tenure,"6");
        Assert.assertNotNull(emi);
        Assert.assertNotNull(eligibleAmt);
        Assert.assertEquals(status,"1");
        Assert.assertNotNull(promocode);
        Assert.assertNotNull(promotext);
    }

    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify  emisubventioninfo object in cop for  amount based case")
    public void TestEMISubventionInfoforOnlyAmountBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|9")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);

        String planI = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        String eligibleAmt= PG2LogsValidationHelper.getKeyParameterValueFromLogs("eligibleAmt",emiSubventionInfoLogs);
        Assert.assertNotNull(planI);
        Assert.assertEquals(subventionAmount,"2000.25");
        Assert.assertEquals(tenure,"9");
        Assert.assertNotNull(emi);
        Assert.assertNotNull(eligibleAmt);

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify emisubventioninfo and promocheckout objects in cop for item based and promo")
    public void TestCOPForItemBasedAndPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2417648")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|9")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int emiSubventionInfoIdx=logs.indexOf("emiSubventionInfo");
        String emiSubventionInfoLogs=logs.substring(emiSubventionInfoIdx);
        int paymentPromoCheckoutDataIdx=logs.indexOf("paymentPromoCheckoutData");
        String paymentPromoCheckoutDataLogs=logs.substring(paymentPromoCheckoutDataIdx);
        String status = PG2LogsValidationHelper.getKeyParameterValueFromLogs("status",paymentPromoCheckoutDataLogs);
        String promocode = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promocode",paymentPromoCheckoutDataLogs);
        String promotext = PG2LogsValidationHelper.getKeyParameterValueFromLogs("promotext",paymentPromoCheckoutDataLogs);


        String planI = PG2LogsValidationHelper.getKeyParameterValueFromLogs("planId",emiSubventionInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",emiSubventionInfoLogs);
        String tenure= PG2LogsValidationHelper.getKeyParameterValueFromLogs("tenure",emiSubventionInfoLogs);
        String emi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emi",emiSubventionInfoLogs);
        Assert.assertNotNull(planI);
        Assert.assertEquals(subventionAmount,"2000.25");
        Assert.assertEquals(tenure,"9");
        Assertions.assertThat(emiSubventionInfoLogs).contains("\"gratificationType\":\"DISCOUNT\"");
        Assertions.assertThat(emiSubventionInfoLogs).isNotNull();
        Assert.assertEquals(status,"1");
        Assert.assertEquals(promocode,"PROMO000123444");
        Assert.assertNotNull(promotext);


    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify emisubventioninfo and paymentPromoCheckoutData objects in getPaymentStatus api for amount based subvention and promo")
    public void validateGetPaymentStatusForAmountBasedAndPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|6")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        GetPaymentStatus getPaymentStatus=new GetPaymentStatus(Constants.MerchantType.SIMPLIFIED_OFFERS,initTxnDTO.orderFromBody());
        JsonPath res=getPaymentStatus.execute().jsonPath();
        Assert.assertNotNull(res.getString("body.paymentPromoCheckoutData"));
        Assert.assertNotNull(res.getString("body.emiSubventionInfo"));
    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify  emisubventioninfo and promocheckout objects in getpaymentstatus for  amount based case")
    public void TestGetPaymentStatusforOnlyAmountBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|9")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        GetPaymentStatus getPaymentStatus=new GetPaymentStatus(Constants.MerchantType.SIMPLIFIED_OFFERS,initTxnDTO.orderFromBody());
        JsonPath res=getPaymentStatus.execute().jsonPath();
        Assert.assertNotNull(res.getString("body.emiSubventionInfo"));
    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify emisubventioninfo and promocheckout objects in GetPaymentStatus for item based and promo")
    public void TestGetPaymentStatusForItemBasedAndPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2417648")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|9")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        GetPaymentStatus getPaymentStatus=new GetPaymentStatus(Constants.MerchantType.SIMPLIFIED_OFFERS,initTxnDTO.orderFromBody());
        JsonPath res=getPaymentStatus.execute().jsonPath();
        Assert.assertNotNull(res.getString("body.paymentPromoCheckoutData"));
        Assert.assertNotNull(res.getString("body.emiSubventionInfo"));
    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description = "Verfy detailExtendInfo in cop for amount based subvention for custom checkout flow ")
    public void TestDetailExtendInfoForAmountBased() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);
        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.25\"");
        Assert.assertNotNull(emiAmount);
        Assert.assertNotNull(subventionAmount);
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertNotNull(loanAmount);
        Assert.assertEquals(emiType,"brandEMI");
        Assert.assertNotNull(subvention);
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
    }

    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description = "Verify detailExtendInfo in cop for amount based and promo case")
    public void validateDetailExtendInfoForAmtBasedAndPromo() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2151610");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|9")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);
        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.25\"");
        Assert.assertNotNull(emiAmount);
        Assert.assertNotNull(subventionAmount);
        Assert.assertEquals(subventionType,"CASHBACK");
        Assert.assertNotNull(loanAmount);
        Assert.assertEquals(emiType,"brandEMI");
        Assert.assertNotNull(subvention);
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify detailExtendInfo in cop for item based and promo for custom checkout flow")
    public void TestCOPForItemBasedAndPromo1() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2417648")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.items[0].productId","8903287020001");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|9")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(logs).contains("EMI_DETAIL_INFO");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"2000.25\"");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"true\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"true\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"2000.25\"");
        Assertions.assertThat(logs).contains("\"emiAmount\":\"179.95\"");
        Assertions.assertThat(logs).contains("\"subventionAmount\":\"7740\"");
        Assertions.assertThat(logs).contains("\"subventionType\":\"DISCOUNT\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"1522.8\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"bankEMI\"");
        Assertions.assertThat(logs).contains("\"modelName\":\"8903287020001\"");
        Assertions.assertThat(logs).contains("\"brandId\":\"579986\"");
        Assertions.assertThat(logs).contains("\"subvention\":\"4.83\"");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"true\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify detailExtendInfo in cop for item based  for custom checkout flow")
    public void TestCOPForItemBased1() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",9)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2164614")
                .deleteContext("body.items[0].offerDetails.bankOfferDetails[0]")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","3271")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedEmiOffer","false")
                .setContext("body.items[0].productId","8903287020001");

        JsonPath jsonpath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|9")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String modelName= PG2LogsValidationHelper.getKeyParameterValueFromLogs("modelName",detailExtendInfoLogs);
        String brandId= PG2LogsValidationHelper.getKeyParameterValueFromLogs("brandId",extendInfoLogs);
        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"true");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.25\"");
        Assert.assertNotNull(emiAmount);
        Assert.assertNotNull(subventionAmount);
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertNotNull(loanAmount);
        Assert.assertEquals(emiType,"bankEMI");
        Assert.assertEquals(modelName,"8903287020001");
        Assert.assertEquals(brandId,"579986");
        Assert.assertNotNull(subvention);
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"true\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify detailExtendInfo in cop for only promo case for custom checkout")
    public void TestCOPForOnlypromoForCustomCheckout() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(promoEMIRequest)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2193500")
                .setContext("paymentDetails.orderAmount","500.00")
                 .setContext("body.amountBasedBankOffer",true)
                .setContext("body.amountBasedEmiOffer",false);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(logs).contains("EMI_DETAIL_INFO");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"false\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"500.0\"");
        Assertions.assertThat(logs).contains("\"emiAmount\":\"166.94\"");
        Assertions.assertThat(logs).contains("\"loanAmount\":\"500.0\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"bankEMI\"");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"additionalCashBack\":\"9.0\"");
        Assertions.assertThat(logs).contains("\"additionalCashBackAmount\":\"4500\"");
    }


    @Owner(AKSHAT_NAYAK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify SubventionType as DISCOUNT and emiType as brandEMI in COP request")
    public void SubventionTypeDISCOUNTandEmiTypeBrandEMIinCOP(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.offerDetails.emiOfferDetails.offerId", "2141488")
                .setContext("body.paymentDetails.paymentOptions[0].transactionAmount", 1100.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "HDFC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", "4761360075860428")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value", "3")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit", "MONTH");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"subventionType\":\"DISCOUNT\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"brandEMI\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify SubventionType as CASHBACK and emiType as brandEMI in COP request")
    public void SubventionTypeCASHBACKandEmiTypeBrandEMIinCOP(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.offerDetails.emiOfferDetails.offerId", "2141488")
                .setContext("body.paymentDetails.paymentOptions[0].transactionAmount", 1100.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "HDFC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", "4761360075860428")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value", "9")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit", "MONTH");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"subventionType\":\"CASHBACK\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"brandEMI\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify emiType as bankEMI in COP request")
    public void SubventionEMITypeBankEMIinCOP(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .deleteContext("body.offerDetails.emiOfferDetails")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId", "2155487")
                .setContext("body.paymentDetails.paymentOptions[0].transactionAmount", 1100.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "HDFC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", "4761360075860428")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value", "3")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit", "MONTH");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .setCardInfo("|4761360075860428|545|122024")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"emiType\":\"bankEMI\"");
    }

    @Owner(AKSHAT_NAYAK)
    @Parameters({"isNativePlus"})
    @Feature("PGP-48949")
    @Test(description = "Verify emiType as retailerEMI in COP request")
    public void emiTypeRetailerEMIinCOP(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EmiInfo_COP;
        User user = userManager.getForRead(Label.UPIPUSHPG2);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .setContext("head.token", ssoToken)
                .setContext("body.mid", mid.getId())
                .setContext("body.custId", custId)
                .setContext("body.paytmUserId", custId)
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount", 1100.00)
                .setContext("body.offerDetails.emiOfferDetails.offerId", "2192568")
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].transactionAmount", 1100.00)
                .setContext("body.paymentDetails.paymentOptions[0].payMethod", "EMI")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank", "HDFC")
                .setContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode", "VISA")
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", "4761360075860428")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value", "6")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit", "MONTH");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EmiInfo_COP, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .setCardInfo("|4761360075860428|545|122024")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("\"emiType\":\"retailerEMI\"");
    }

    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description = "Verfy detailExtendInfo in ACQUIRING_ORDER_MODIFY for amount based subvention for custom checkout flow for COTP Merchant")
    public void TestDetailExtendInfoForAmountBasedForCOTP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DC_CC;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2165743");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_ORDER_MODIFY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);
        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.25\"");
        Assert.assertNotNull(emiAmount);
        Assert.assertNotNull(subventionAmount);
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertNotNull(loanAmount);
        Assert.assertEquals(emiType,"brandEMI");
        Assert.assertNotNull(subvention);
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-49322")
    @Test(description = "Verfy detailExtendInfo in ACQUIRING_ORDER_MODIFY for amount based subvention and promo Discount for custom checkout flow for COTP Merchant")
    public void TestDetailExtendInfoForAmountBasedAndPromoForCOTP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DC_CC;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2165743")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2236834");
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_ORDER_MODIFY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);
        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"false");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.25\"");
        Assert.assertNotNull(emiAmount);
        Assert.assertNotNull(subventionAmount);
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertNotNull(loanAmount);
        Assert.assertEquals(emiType,"brandEMI");
        Assert.assertNotNull(subvention);
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_MODIFY for item based  for custom checkout flow")
    public void TestDetailExtendInfoItemBasedCOTP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DC_CC;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",6)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2250680")
                .deleteContext("body.items[0].offerDetails.bankOfferDetails[0]")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","10001")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedEmiOffer","false")
                .setContext("body.items[0].productId","1");

        JsonPath jsonpath = offerApply.execute().jsonPath();
        //Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|6")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_ORDER_MODIFY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String modelName= PG2LogsValidationHelper.getKeyParameterValueFromLogs("modelName",detailExtendInfoLogs);
        String brandId= PG2LogsValidationHelper.getKeyParameterValueFromLogs("brandId",extendInfoLogs);
        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"true");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.25\"");
        Assert.assertNotNull(emiAmount);
        Assert.assertNotNull(subventionAmount);
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertNotNull(loanAmount);
        Assert.assertEquals(emiType,"retailerEMI");
        Assert.assertEquals(modelName,"1");
        Assert.assertEquals(brandId,"579986");
        Assert.assertNotNull(subvention);
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");

    }
    @Owner("Nirottam")
    @Feature("PGP-50287")
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_MODIFY for item based and promo for custom checkout flow")
    public void TestDetailExtendInfoItemBasedAndPromoCOTP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DC_CC;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",6)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2250680")
                .setContext("body.items[0].offerDetails.bankOfferDetails[0].offerId","2236834")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","10001")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedEmiOffer","false")
                .setContext("body.items[0].productId","1");

        JsonPath jsonpath = offerApply.execute().jsonPath();
        //Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|6")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_ORDER_MODIFY");
        int detailExtendInfoIdx=logs.indexOf("detailExtendInfo");
        String detailExtendInfoLogs=logs.substring(detailExtendInfoIdx);
        int extendInfoIdx=logs.indexOf("extendInfo");
        String extendInfoLogs=logs.substring(extendInfoIdx);

        Assertions.assertThat(detailExtendInfoLogs).contains("EMI_DETAIL_INFO");
        String emiAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiAmount",detailExtendInfoLogs);
        String subventionAmount = PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionAmount",detailExtendInfoLogs);
        String subventionType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subventionType",detailExtendInfoLogs);
        String loanAmount= PG2LogsValidationHelper.getKeyParameterValueFromLogs("loanAmount",detailExtendInfoLogs);
        String emiType= PG2LogsValidationHelper.getKeyParameterValueFromLogs("emiType",detailExtendInfoLogs);
        String subvention= PG2LogsValidationHelper.getKeyParameterValueFromLogs("subvention",detailExtendInfoLogs);
        String isMerchantSubventedBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isMerchantSubventedBrandEmi",detailExtendInfoLogs);
        String isSubventionCreated= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isSubventionCreated",extendInfoLogs);
        String isBrandEmi= PG2LogsValidationHelper.getKeyParameterValueFromLogs("isBrandEmi",extendInfoLogs);
        String modelName= PG2LogsValidationHelper.getKeyParameterValueFromLogs("modelName",detailExtendInfoLogs);
        String brandId= PG2LogsValidationHelper.getKeyParameterValueFromLogs("brandId",extendInfoLogs);
        Assert.assertEquals(isSubventionCreated,"true");
        Assert.assertEquals(isBrandEmi,"true");
        Assertions.assertThat(extendInfoLogs).contains("\"originalAmount\":\"2000.25\"");
        Assert.assertNotNull(emiAmount);
        Assert.assertNotNull(subventionAmount);
        Assert.assertEquals(subventionType,"DISCOUNT");
        Assert.assertNotNull(loanAmount);
        Assert.assertEquals(emiType,"retailerEMI");
        Assert.assertEquals(modelName,"1");
        Assert.assertEquals(brandId,"10002");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"subvention\":\"4.23\"");
        Assertions.assertThat(detailExtendInfoLogs).contains("\"isMerchantSubventedBrandEmi\":\"true\"");

    }

    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-46699")
    @Test(description = "Test e2e success txn with postpaid for custom flow for item based.")
    public void TestE2ESuccessTxnWithPostpaid_01(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","PAYTM_DIGITAL_CREDIT")
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","18084")
                .setContext("body.items[0].categoryId","6224")
                .setContext("body.items[0].price","200")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","false")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",200.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMode\":\"PAYTM_DIGITAL_CREDIT\"");
    }


    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-46699")
    @Test(description = "Test e2e success txn with postpaid for custom flow for amount based.")
    public void TestE2ESuccessTxnWithPostpaid_02(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.UPIPUSHPG2);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","PAYTM_DIGITAL_CREDIT")
                .setContext("body.amountBasedBankOffer",true)
                .setContext("body.paymentDetails.orderAmount",200.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.PAYTM_DIGITAL_CREDIT)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMode\":\"PAYTM_DIGITAL_CREDIT\"");

    }

    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-46699")
    @Test(description = "Test e2e success txn with ppbl for custom flow for item based.")
    public void TestE2ESuccessTxnWithPPBL_01(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","PPBL")
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","10002")
                .setContext("body.items[0].categoryId","10001")
                .setContext("body.items[0].price","200")
                .setContext("body.amountBasedBankOffer",false)
                .setContext("body.amountBasedSubvention",false)
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",200.00);

        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("PPBL")
                .setAuthMode("3D")
                .setMpin("1234")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMethod\":\"PPBL\"");

    }

    @Owner("Himanshu Arora")
    @Parameters({"isNativePlus"})
    @Feature("PGP-46699")
    @Test(description = "Test e2e success txn with ppbl for custom flow for amount based.")
    public void TestE2ESuccessTxnWithPPBL_02(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.PG2POSTPAIDUSER);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token",ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","PPBL")
                .setContext("body.amountBasedBankOffer",true)
                .setContext("body.paymentDetails.orderAmount",200.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("PPBL")
                .setAuthMode("3D")
                .setMpin("1234")
                .build();

        submitProcessTxnResponseFromReq(processTxnV1Request);

        String orderid = initTxnDTO.getBody().getOrderId();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, orderid, "AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"payMethod\":\"PPBL\"");

    }
    @Owner(ROHIT_SHARMA)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46696")
    @Test(description = "Verify flowtype= retailer should be passed in applyoffer API request and checkout api request when amountBasedBankOffer=true & amountBasedSubvention=true in api")
    public void FlowTypeRetailer_when_both_amountBasedBankOffer_and_amountBasedSubvention_true_with_itemPasssed(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","579986")
                .setContext("body.items[0].categoryId","10001")
                .setContext("body.items[0].price","1100")
                //.setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,Constants.MerchantType.EMI_DISCOVERY.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[RETAILER]");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date());
        String checkoutlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(checkoutlogs).contains("flowType=[RETAILER]");
    }
    @Owner(ROHIT_SHARMA)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46696")
    @Test(description = "Verify flowtype= retailer should be passed in applyoffer API request when amountBasedBankOffer=true & amountBasedSubvention=true in api with item object not passed")
    public void FlowTypeRetailer_when_both_amountBasedBankOffer_and_amountBasedSubvention_true_with_item_NOT_Passsed(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .deleteContext("body.offerDetails")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,Constants.MerchantType.EMI_DISCOVERY.getId(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[RETAILER]");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date());
        String checkoutlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"AFFORDABILITY_PLATFORM","REQUEST");
        Assertions.assertThat(checkoutlogs).contains("\"flowType\":\"RETAILER\"");
    }

    @Owner(ROHIT_SHARMA)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46696")
    @Test(description = "Verify flowtype= brand should be passed in applyoffer API request when amountBasedBankOffer=false & amountBasedSubvention=false in api with item object in it")
    public void FlowTypeBrand_when_both_amountBasedBankOffer_and_amountBasedSubvention_false_with_itemPasssed(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","18084")
                .setContext("body.items[0].categoryId","6224")
                .setContext("body.items[0].price","1100")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","false")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getMID(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[BRAND]");
    }
    @Owner(ROHIT_SHARMA)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46696")
    @Test(description = "Verify flowtype= brand should be passed in applyoffer API request when amountBasedBankOffer=false & amountBasedSubvention=true in api with item object in it")
    public void FlowType_AMOUNT_BASED_SUBVENTION_when_both_amountBasedBankOffer_false_and_amountBasedSubvention_true_with_itemPasssed(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","18084")
                .setContext("body.items[0].categoryId","6224")
                .setContext("body.items[0].price","1100")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedSubvention","true")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getMID(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_SUBVENTION]");
    }
    @Owner(ROHIT_SHARMA)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46696")
    @Test(description = "Verify flowtype= brand should be passed in applyoffer API request when amountBasedBankOffer=true & amountBasedSubvention=false in api with item object in it")
    public void FlowType_AMOUNT_BASED_OFFERS_when_both_amountBasedBankOffer_true_and_amountBasedSubvention_false_with_itemPasssed(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForWrite(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.items[0].id","15036688")
                .setContext("body.items[0].productId","1")
                .setContext("body.items[0].brandId","18084")
                .setContext("body.items[0].categoryId","6224")
                .setContext("body.items[0].price","1100")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","false")
                .deleteContext("body.items[0].offerDetails")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("CC")
                .validateTxnDate(new Date());
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getMID(),"AFFORDABILITY_PLATFORM_DISCOVERY","REQUEST");
        Assertions.assertThat(logs).contains("flowType=[AMOUNT_BASED_OFFERS]");

    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-51181")
    @Test(description = "Verify complete NB txn with best bank offer amount based for non logged in flow")
    public void TestE2ESuccessResponseWhenBestbankOfferAppliedforNBTxnNon_Loggedin(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .deleteContext("body.offerDetails")
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-51181")
    @Test(description = "Verify complete UPI txn with discounted bank offer amount based for non logged flow")
    public void TestE2ESuccessResponseWhenDiscountedbankOfferAppliedforUPITxnnon_LoggedIn(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(credit_card_txn_amount_based)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .deleteContext("body.paymentDetails.paymentOptions[0].cardNo")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2417648")
                .setContext("body.paymentDetails.paymentOptions[0].vpa","test@paytm")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","UPI")
                .setContext("body.paymentDetails.orderAmount",1100.00);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage  = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM");
        Assertions.assertThat(logs).contains("\"isBankOfferApplied\":true");
    }

    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when partial subvention and Bo amount provided")
    public void TestE2ESuccessResponseWhenPartialSubventionAndBoAmountprovided(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2417648")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();

    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when only partial subvention amount provided")
    public void TestE2ESuccessResponseWhenOnlyPartialSubventionProvided(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.NOPPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2417648")
                .deleteContext("body.items")
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .deleteContext("body.paymentDetails.paymentOptions[0].boEligibleAmount")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when only partial bank offer amount provided")
    public void TestE2ESuccessResponseWhenOnlyPartialBankOfferAmountProvided(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1100)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2417648")
                .deleteContext("body.items")
                .deleteContext("body.paymentDetails.paymentOptions[0].subventionAmount")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].unit","MONTH")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","EMI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("ZERO_COST");
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonPath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.EMI)
                .setPlanId(planId)
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("EMI")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }
    @Owner(KARMVIR)
    @Parameters({"isNativePlus"})
    @Feature("PGP-46698")
    @Test(description = "Verify success txn when only partial bank offer amount provided for Net Banking paymode")
    public void TestE2ESuccessResponseWhenOnlyPartialBankOfferAmountProvidedNB(@Optional("true") boolean isNativePlus) throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.EMI_DISCOVERY;
        User user = userManager.getForRead(Label.PG2POSTPAIDUSER);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .setContext("head.token",user.ssoToken())
                .setContext("body.custId",custId)
                .setContext("body.paytmUserId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedSubvention","true")
                .setContext("body.paymentDetails.orderAmount",1000)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2141488")
                .setContext("body.offerDetails.bankOfferDetails.[0].offerId","2417648")
                .deleteContext("body.items")
                .deleteContext("body.paymentDetails.paymentOptions[0].subventionAmount")
                .setContext("body.paymentDetails.paymentOptions[0].boEligibleAmount","500")
                .setContext("body.paymentDetails.paymentOptions[0].payMethod","NET_BANKING")
                .setContext("body.paymentDetails.paymentOptions[0].issuingBank","ICICI");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonPath.getString("body.unifiedOffersToken");
        String originalAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].originalAmount");
        String payableAmount = jsonPath.getString("body.paymentDetails[0].offerDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.EMI_DISCOVERY, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();
        CheckoutPage checkoutPage = new CheckoutPage();
        ResponsePage responsePage = new ResponsePage();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        TxnStatus txnStatus = new TxnStatus(mid.getId(), initTxnDTO.orderFromBody());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateTxnType("SALE")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateRefundAmnt("0.00")
                .validateTxnDate(new Date())
                .validateChildTxnsPresent();
    }

    @Owner(MAYURI)
    @Feature("PGP-48241")
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_CREATEORDER_AND_PAY for item based subvnetion+BO  for custom checkout flow")
    public void TestDetailExtendInfoItemBasedSubventionAndBOCOP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",6)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2193577")
                .deleteContext("body.items[0].offerDetails.bankOfferDetails[0]")
                .setContext("body.items[0].id","123456789")
                .setContext("body.items[0].brandId","18084")
                .setContext("body.items[0].categoryId","6224")
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","false")
                .setContext("body.items[0].productId","123456789");

        JsonPath jsonpath = offerApply.execute().jsonPath();
        //Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|6")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("loanAmount");
        Assertions.assertThat(logs).contains("subvention");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"true\"");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"true\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"true\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"SUBVENTION\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"2000.25\"");
        Assertions.assertThat(logs).contains("modelName");
        Assertions.assertThat(logs).contains("\"brandId\":\"18084\"");
        Assertions.assertThat(logs).contains("emiAmount");
        Assertions.assertThat(logs).contains("subventionAmount");
        Assertions.assertThat(logs).contains("subventionType");

    }

    @Owner(MAYURI)
    @Feature("PGP-48241")
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_CREATEORDER_AND_PAY cop for only promo case for custom checkout")
    public void TestDetailExtendInfoCOPForOnlypromoForCustomCheckout() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(promoEMIRequest)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("paymentDetails.orderAmount","500.00")
                .setContext("body.amountBasedBankOffer",true)
                .setContext("body.amountBasedEmiOffer",false);
        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs=LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody().toString(),"ACQUIRING_CREATE_ORDER_AND_PAY","REQUEST");
        Assertions.assertThat(logs).contains("loanAmount");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"false\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"false\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"STANDARD\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"500.0\"");
        Assertions.assertThat(logs).contains("emiAmount");
    }

    @Owner(MAYURI)
    @Feature("PGP-48241")
    @Test(description = "Verify detailExtendInfo in ACQUIRING_ORDER_CREATEORDER_AND_PAY for only item based  for custom checkout flow")
    public void TestDetailExtendInfoItemBasedSubventionCOP() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_body_with_tenure)
                .deleteContext("body.custId")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",6)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .deleteContext("body.offerDetails")
                .setContext("body.items[0].price",2000.25)
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.items[0].offerDetails.emiOfferDetails.offerId","2193577")
                .deleteContext("body.items[0].offerDetails.bankOfferDetails[0]")
                .setContext("body.items[0].id","123456789")
                .setContext("body.items[0].brandId","18084")
                .setContext("body.items[0].categoryId","6224")
                .setContext("body.amountBasedBankOffer","false")
                .setContext("body.amountBasedEmiOffer","false")
                .setContext("body.items[0].productId","123456789");

        JsonPath jsonpath = offerApply.execute().jsonPath();
        //Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|6")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY", "REQUEST");
        Assertions.assertThat(logs).contains("loanAmount");
        Assertions.assertThat(logs).contains("subvention");
        Assertions.assertThat(logs).contains("\"isMerchantSubventedBrandEmi\":\"true\"");
        Assertions.assertThat(logs).contains("\"isSubventionCreated\":\"true\"");
        Assertions.assertThat(logs).contains("\"isBrandEmi\":\"true\"");
        Assertions.assertThat(logs).contains("\"emiType\":\"SUBVENTION\"");
        Assertions.assertThat(logs).contains("\"originalAmount\":\"2000.25\"");
        Assertions.assertThat(logs).contains("modelName");
        Assertions.assertThat(logs).contains("\"brandId\":\"18084\"");
        Assertions.assertThat(logs).contains("emiAmount");
        Assertions.assertThat(logs).contains("subventionAmount");
        Assertions.assertThat(logs).contains("subventionType");

    }

    @Owner(MAYURI)
    @Feature("PGP-46700")
    @Test(description = "Verify offerid in ADS for simplified only amount based subvention+BO, offerid  passed")
    public void TestOfferIDInADSRequestWhenAmountBasedSubventionAndBOWithoutOfferIDForCustomFlow() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",2000.25)
                .setContext("body.offerDetails.emiOfferDetails.offerId","2384254")
                .setContext("body.offerDetails.bankOfferDetails[0].offerId","2385283");

        JsonPath jsonpath = offerApply.execute().jsonPath();
        //Assertions.assertThat(jsonpath.getString("body.paymentDetails[0].tenureDetails[0].items[0].offerDetails.emiOfferDetails[0].subventionType")).isEqualTo("LOW_COST");
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String planId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4718650100010336|111|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|6")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"AFFORDABILITY_PLATFORM_DISCOVERY", "REQUEST");
        Assertions.assertThat(logs).doesNotContain("offerId");
    }
    @Owner(PUSPA)
    @Feature("PPSL-524")
    @Test(description = "Verify bin param with 9 digit in Apply/Checkout req")
    public void validateBin9Digit() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.PG2_AMEX_EMI;
        User user = userManager.getForRead(Label.PPBL);
        String ssoToken= user.ssoToken();
        String custId= user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(emi_amount_based_with_tenure)
                .deleteContext("body.offerDetails.bankOfferDetails")
                .deleteContext("body.offerDetails.emiOfferDetails")
                .setContext("body.paymentDetails.paymentOptions[0].tenure[0].value",3)
                .setContext("head.token", ssoToken)
                .setContext("body.mid",mid.getId())
                .setContext("body.custId",custId)
                .setContext("body.amountBasedBankOffer","true")
                .setContext("body.amountBasedEmiOffer","true")
                .setContext("body.paymentDetails.orderAmount",800)
                .setContext("body.paymentDetails.paymentOptions[0].cardNo","4895380115392363");

        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(),txnToken,initTxnDTO.orderFromBody().toString()).
                setPaymentMode("EMI")
                .setCardInfo("|4895380115392363|123|122025")
                .setAuthMode("otp")
                .setChannelCode("HDFC")
                .setEmiType("CREDIT_CARD")
                .setPlanId("HDFC|3")
                .build();
        submitProcessTxnResponseFromReq(processTxnV1Request);
        String applyLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"ads/v2/offer/apply", "REQUEST");
        Assertions.assertThat(applyLogs).contains("\"bin\":\"489538011\"");

        String checkoutLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,mid.getId(),"ats/v2/order/checkout", "REQUEST");
        Assertions.assertThat(checkoutLogs).contains("\"bin\":\"489538011\"");

    }

    @Owner(PUSPA)
    @Feature("PG-2153")
    @Test(description = "Verify offer apply success for Bajaj Finserv EMI_CARDLESS with SSO token; skip PAR call when ENABLE_COFT_PROMO_PAR_CONFIG pref is Y")
    public void testOfferApplyBajajFinservEmiCardless() throws Exception {
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        OfferApply offerApply = (OfferApply) new OfferApply(offerApplyBajajEmiCardlessRequest)
                .setContext("head.token", ssoToken)
                .setContext("body.custId", custId)
                .setContext("body.mid", "qa12id29388530533353");
        JsonPath jsonPath = offerApply.execute().jsonPath();
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(jsonPath.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(jsonPath.getString("body.unifiedOffersToken")).isNotNull();

        String parLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,"qa12ps69382732062304","PAN", "REQUEST");
        Assertions.assertThat(parLogs).doesNotContain("coft-center/get/panUniqueReference");

    }

    @Owner(PUSPA)
    @Feature("PG-7767")
    @Test(description = "Verify checkout call is going for EMI_CARDLESS txn")
    public void verifyCheckoutcallForCardLess() throws Exception {
        Constants.MerchantType mid = Constants.MerchantType.BAJAJFN_CARDLESS;
        User user = userManager.getForRead(Label.BASIC);
        String ssoToken = user.ssoToken();
        String custId = user.custId();
        String orderAmount = "10005";
        String cardNo = PaymentDTO.BAJAJFN_CARDLESS_CARD;
        OfferApply offerApply = (OfferApply) new OfferApply(offerApplyBajajEmiCardlessRequest)
                .deleteContext("body.paymentDetails.paymentOptions[0].issuingNetworkCode")
                .setContext("head.token", ssoToken)
                .setContext("body.custId", custId)
                .setContext("body.mid", mid.getId())
                .setContext("body.amountBasedBankOffer", true)
                .setContext("body.amountBasedSubvention", true)
                .setContext("body.paymentDetails.orderAmount", orderAmount)
                .setContext("body.paymentDetails.paymentOptions[0].subventionAmount", orderAmount)
                .setContext("body.paymentDetails.paymentOptions[0].cardNo", cardNo);

        JsonPath jsonpath = offerApply.execute().jsonPath();
        String unifiedOffersToken = jsonpath.getString("body.unifiedOffersToken");
        String originalAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].originalAmount");
        String payableAmount = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].payableAmount");
        String pgPlanId = jsonpath.getString("body.paymentDetails[0].tenureDetails[0].planId");
        TxnAmount txnAmount = new TxnAmount(payableAmount);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, mid, unifiedOffersToken)
                .setTxnValue(originalAmount).setPayableAmount(txnAmount).setCustId(custId).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(mid.getId().toString(), txnToken, initTxnDTO.orderFromBody().toString())
                .setPaymentMode("EMI_CARDLESS")
                .setCardInfo("|" + cardNo + "||")
                .setAuthMode("otp")
                .setChannelCode("BAJAJFN")
                .setEmiType("NBFC")
                .setPlanId(pgPlanId)
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");

        String checkoutLogs = LogsValidationHelper.verifyLogsOnPod(
                PG2LogsValidationHelper.setEnvService.theia_facade,
                initTxnDTO.orderFromBody(),
                "ats/v2/order/checkout",
                "REQUEST");
        Assertions.assertThat(checkoutLogs).isNotEmpty();
        Assertions.assertThat(checkoutLogs).contains("ats/v2/order/checkout");
        Assertions.assertThat(checkoutLogs).contains("EMI_CARDLESS");
    }
}
