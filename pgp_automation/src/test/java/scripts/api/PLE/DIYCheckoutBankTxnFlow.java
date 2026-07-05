package scripts.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.api.PLE.DealsEmiAll;
import com.paytm.api.PLE.DealsEmiGetInvoice;
import com.paytm.api.PLE.DealsMerchantDetails;
import com.paytm.api.PLE.DealsPaymentTransactionHistory;
import com.paytm.api.PLE.DealsShareInvoiceViaSms;
import com.paytm.api.PLE.DealsStoreDetails;
import com.paytm.api.PLE.DealsStorePaymodes;
import com.paytm.api.PLE.DealsUserInitiateTransaction;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.checkoutjs.MerchantConfig;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutJsCheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * PLE DIY bank / amount-based flows: mirrors {@link DIYCheckoutBrandTxnFlow}
 * helpers for Checkout JS + Deals APIs.
 */
public class DIYCheckoutBankTxnFlow extends PGPBaseTest {

        private static final Logger log = LoggerFactory.getLogger(DIYCheckoutBankTxnFlow.class);

        private static final String CASHIER_PHONE = "9494949497";
        private static final String MERCHANT_DISPLAY_NAME = "Emi Store Testing";
        private static final String ISSUING_BANK_HDFC = "HDFC";
        private static final String EMI_TENURE = "3";
        /** Pay-in-full CC rail — tenure {@code "0"} on {@code enablePaymentMode}. */
        private static final String CC_PAY_IN_FULL_TENURE = "0";

        /**
         * ₹3500 amount-based {@code emi/all} + minimal PG_LINK initiate (amount-based
         * bank offer flags).
         */
        private static final long AMOUNT_BASED_OFFER_RUPEES = 3500L;
        private static final String AMOUNT_BASED_OFFER_TXN_STR = "3500.00";
        private static final long AMOUNT_BASED_OFFER_PAISE = AMOUNT_BASED_OFFER_RUPEES * 100;

        private static CheckoutJsCheckoutPage pleCheckoutPage() {
                if (Boolean.parseBoolean(System.getProperty("ple.checkoutjs.useQa14", "true"))) {
                        return new PleQa14CheckoutJsCheckoutPage();
                }
                return new CheckoutJsCheckoutPage();
        }

        private final CheckoutJsCheckoutPage checkoutPage = pleCheckoutPage();

        private static String requestSpecToCurl(FilterableRequestSpecification req) {
                StringBuilder curl = new StringBuilder("curl --compressed");
                String method = req.getMethod();
                if (!"GET".equalsIgnoreCase(method)) {
                        curl.append(" -X ").append(method);
                }
                for (Header h : req.getHeaders()) {
                        curl.append(" \\\n  -H '").append(h.getName()).append(": ").append(h.getValue()).append("'");
                }
                String body = req.getBody() != null ? req.getBody().toString() : null;
                if (body != null && !body.isEmpty()) {
                        curl.append(" \\\n  --data '").append(body).append("'");
                }
                curl.append(" \\\n  '").append(req.getURI()).append("'");
                return curl.toString();
        }

        private static final Filter PLE_CURL_LOGGER = new Filter() {
                @Override
                public Response filter(
                                FilterableRequestSpecification req,
                                FilterableResponseSpecification res,
                                FilterContext ctx) {
                        String uri = req.getURI();
                        boolean logInfo = uri != null
                                        && (uri.contains(DealsUserInitiateTransaction.API_RELATIVE_PATH)
                                                        || uri.contains(DealsShareInvoiceViaSms.API_PATH));
                        String curl = requestSpecToCurl(req);
                        if (logInfo) {
                                log.info("[PLE request CURL]\n{}", curl);
                        } else if (log.isDebugEnabled()) {
                                log.debug("[CURL ↗]\n{}", curl);
                        }
                        return ctx.next(req, res);
                }
        };

        private boolean transactionHistoryResponseContainsOrderId(Response response, String orderId) {
                List<String> merchantTransIds = response.jsonPath().getList("orderList.merchantTransId", String.class);
                List<String> pleIds = response.jsonPath().getList("orderList.pleId", String.class);
                return (merchantTransIds != null && merchantTransIds.contains(orderId))
                                || (pleIds != null && pleIds.contains(orderId));
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-DIYCheckoutBank-EMI")
        @Parameters({ "theme" })
        @Test(description = "PLE bank Offer EMI DIY Checkout End to End,initiateTransaction, Checkout JS EMI, invoice APIs")
        public void pleBankEMIOfferDiyCheckoutEndToEnd(@Optional("checkoutjs_web_revamp_2") String theme)
                        throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        User user = userManager.getForRead(Label.BASIC);
                        String pgMid = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;

                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("merchant/details").isEqualTo(200);

                        Response storeRes = new DealsStoreDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/store?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(storeRes.getStatusCode()).as("store/details").isEqualTo(200);

                        Response emiAllRes = new DealsEmiAll.Builder()
                                        .baseUri(dealsBase)
                                        .merchantAndLineId(pgMid)
                                        .price(AMOUNT_BASED_OFFER_PAISE)
                                        .referer(DealsEmiAll.refererCheckForEmiPlansGuest(dealsBase, pgMid))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all amount-based").isEqualTo(200);

                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .payMethod("EMI")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        Assertions.assertThat(paymodesJp.getBoolean("isStoreQrDIYCheckoutEnable")).isTrue();

                        DealsUserInitiateTransaction.Builder initTxnBuilder = new DealsUserInitiateTransaction.Builder()
                                        .minimalAmountBasedPgLink(true)
                                        .baseUri(dealsBase)
                                        .mid(pgMid)
                                        .txnAmountValue(AMOUNT_BASED_OFFER_TXN_STR)
                                        .subventionAmount(AMOUNT_BASED_OFFER_TXN_STR)
                                        .applyAvailablePromo(true)
                                        .amountBasedBankOffer(true)
                                        .isAmountBasedBankOffer(true)
                                        .isBrandEmi(false)
                                        .callbackUrl(DealsUserInitiateTransaction.paymentReceiptCallbackUrl(dealsBase))
                                        .paymentMode("EMI")
                                        .banks(ISSUING_BANK_HDFC)
                                        .tenures(EMI_TENURE)
                                        .websiteName("retail");

                        Response initRes = initTxnBuilder.build().execute();
                        Assertions.assertThat(initRes.getStatusCode()).as("user/initiateTransaction").isEqualTo(200);
                        JsonPath initJp = initRes.jsonPath();

                        String orderId = initJp.getString("mappedResponse.orderId");
                        String txnToken = initJp.getString("mappedResponse.txnToken");
                        Assertions.assertThat(initJp.getString("mappedResponse.resultInfo.resultStatus"))
                                        .isEqualTo("S");
                        Assertions.assertThat(initJp.getString("mappedResponse.resultInfo.resultCode"))
                                        .isEqualTo("0000");

                        InitTxnDTO initForCheckout = new InitTxnDTO.Builder(MerchantType.PLE_DEALS_MID, orderId,
                                        user.ssoToken())
                                        .setTxnValue(AMOUNT_BASED_OFFER_TXN_STR)
                                        .build();
                        MerchantConfig checkoutConfig = checkoutPage.loadMerchantConfig(initForCheckout, theme);
                        checkoutConfig.data.setToken(txnToken);

                        checkoutPage.createCheckoutJsOrder(checkoutConfig);

                        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                        cashierPage.waitUntilLoads();
                        PGPHelpers.isCheckoutJsV5FPOCalled(orderId);
                        cashierPage.payBy(Constants.PayMode.EMI);
                        if (cashierPage.ErrorRetryButton().isElementPresent()) {
                                cashierPage.ErrorRetryButton().click();
                                cashierPage.payBy(Constants.PayMode.EMI);
                        }

                        String displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));

                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(pgMid)
                                        .orderId(orderId)
                                        .phoneNo(CASHIER_PHONE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(AMOUNT_BASED_OFFER_TXN_STR)
                                        .transactionDate(displayDate)
                                        .paymentMethod("EMI")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        Assertions.assertThat(smsRes.jsonPath().getString("status")).isEqualTo("SUCCESS");

                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .merchantTransId(orderId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId + "?linkId="
                                                        + orderId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        Assertions.assertThat(invRes.getBody().asString()).isNotBlank();

                        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        Response hRes = new DealsPaymentTransactionHistory.Builder()
                                        .baseUri(dealsBase)
                                        .mid(pgMid)
                                        .pageNum(1)
                                        .nextPageNum(1)
                                        .size(10)
                                        .startDate(today)
                                        .endDate(today)
                                        .referer(dealsBase + "/pgmid/" + pgMid
                                                        + "/emi/transaction-queue?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(hRes.getStatusCode()).as("payment/transaction/history page 1")
                                        .isEqualTo(200);
                        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId)).isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-DIYCheckoutBank-CC")
        @Parameters({ "theme" })
        @Test(description = "PLE bank offer CC pay-in-full  DIY, initiateTransaction  null subvention, Checkout JS CC, invoice APIs")
        public void pleBankOfferCreditCardPayInFullDiyCheckoutEndToEnd(
                        @Optional("checkoutjs_web_revamp_2") String theme) throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        User user = userManager.getForRead(Label.BASIC);
                        String pgMid = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;

                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("merchant/details").isEqualTo(200);

                        Response storeRes = new DealsStoreDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/store?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(storeRes.getStatusCode()).as("store/details").isEqualTo(200);

                        Response emiAllRes = new DealsEmiAll.Builder()
                                        .baseUri(dealsBase)
                                        .merchantAndLineId(pgMid)
                                        .price(AMOUNT_BASED_OFFER_PAISE)
                                        .referer(DealsEmiAll.refererCheckForEmiPlansGuest(dealsBase, pgMid))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all amount-based").isEqualTo(200);

                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .payMethod("CREDIT_CARD")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        Assertions.assertThat(paymodesJp.getBoolean("isStoreQrDIYCheckoutEnable")).isTrue();

                        DealsUserInitiateTransaction.Builder initTxnBuilder = new DealsUserInitiateTransaction.Builder()
                                        .minimalAmountBasedPgLink(true)
                                        .minimalPgLinkNullSubvention(true)
                                        .baseUri(dealsBase)
                                        .mid(pgMid)
                                        .txnAmountValue(AMOUNT_BASED_OFFER_TXN_STR)
                                        .applyAvailablePromo(true)
                                        .amountBasedBankOffer(true)
                                        .isAmountBasedBankOffer(true)
                                        .isBrandEmi(false)
                                        .emiType("payInFull")
                                        .emiTypeBank(ISSUING_BANK_HDFC)
                                        .custId("@")
                                        .callbackUrl(DealsUserInitiateTransaction.paymentReceiptCallbackUrl(dealsBase))
                                        .paymentMode("CREDIT_CARD")
                                        .banks(ISSUING_BANK_HDFC)
                                        .tenures(CC_PAY_IN_FULL_TENURE)
                                        .websiteName("retail");

                        Response initRes = initTxnBuilder.build().execute();
                        Assertions.assertThat(initRes.getStatusCode()).as("user/initiateTransaction").isEqualTo(200);
                        JsonPath initJp = initRes.jsonPath();

                        String orderId = initJp.getString("mappedResponse.orderId");
                        String txnToken = initJp.getString("mappedResponse.txnToken");
                        Assertions.assertThat(initJp.getString("mappedResponse.resultInfo.resultStatus"))
                                        .isEqualTo("S");
                        Assertions.assertThat(initJp.getString("mappedResponse.resultInfo.resultCode"))
                                        .isEqualTo("0000");

                        InitTxnDTO initForCheckout = new InitTxnDTO.Builder(MerchantType.PLE_DEALS_MID, orderId,
                                        user.ssoToken())
                                        .setTxnValue(AMOUNT_BASED_OFFER_TXN_STR)
                                        .build();
                        MerchantConfig checkoutConfig = checkoutPage.loadMerchantConfig(initForCheckout, theme);
                        checkoutConfig.data.setToken(txnToken);

                        checkoutPage.createCheckoutJsOrder(checkoutConfig);

                        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                        cashierPage.waitUntilLoads();
                        PGPHelpers.isCheckoutJsV5FPOCalled(orderId);

                        PaymentDTO ccPayment = new PaymentDTO()
                                        .setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                        cashierPage.payBy(Constants.PayMode.CC, ccPayment);
                        if (cashierPage.ErrorRetryButton().isElementPresent()) {
                                cashierPage.ErrorRetryButton().click();
                                cashierPage.payBy(Constants.PayMode.CC, ccPayment);
                        }

                        String displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));

                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(pgMid)
                                        .orderId(orderId)
                                        .phoneNo(CASHIER_PHONE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(AMOUNT_BASED_OFFER_TXN_STR)
                                        .transactionDate(displayDate)
                                        .paymentMethod("CREDIT_CARD")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        Assertions.assertThat(smsRes.jsonPath().getString("status")).isEqualTo("SUCCESS");

                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .merchantTransId(orderId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId + "?linkId="
                                                        + orderId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        Assertions.assertThat(invRes.getBody().asString()).isNotBlank();

                        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        Response hRes = new DealsPaymentTransactionHistory.Builder()
                                        .baseUri(dealsBase)
                                        .mid(pgMid)
                                        .pageNum(1)
                                        .nextPageNum(1)
                                        .size(10)
                                        .startDate(today)
                                        .endDate(today)
                                        .referer(dealsBase + "/pgmid/" + pgMid
                                                        + "/emi/transaction-queue?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(hRes.getStatusCode()).as("payment/transaction/history page 1")
                                        .isEqualTo(200);
                        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId)).isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }
}
