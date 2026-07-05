package scripts.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.api.PLE.DealsEmiAll;
import com.paytm.api.PLE.DealsEmiGetInvoice;
import com.paytm.api.PLE.DealsEmiOrderSummary;
import com.paytm.api.PLE.DealsLinkCreate;
import com.paytm.api.PLE.DealsMerchantDetails;
import com.paytm.api.PLE.DealsPaymentTransactionHistory;
import com.paytm.api.PLE.DealsShareInvoiceViaSms;
import com.paytm.api.PLE.DealsStoreDetails;
import com.paytm.api.PLE.DealsStorePaymodes;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

/**
 * Bank-offer amount-based payment links: Deals warmup ({@code emi/all} on ₹3500
 * paise), {@link DealsLinkCreate} without
 * catalog {@code productId}, pay on link, then order summary → SMS invoice →
 * get invoice → txn history (same post-create
 * shape as {@link PaymentLinkBrandTxnFlow#pleBrandEmiPaymentLinkEndToEnd}).
 */
public class PaymentLinkBankTxnFlow extends PGPBaseTest {

        private static final Logger log = LoggerFactory.getLogger(PaymentLinkBankTxnFlow.class);

        private static final String MERCHANT_DISPLAY_NAME = "Emi Store Testing";
        private static final String ISSUING_BANK_HDFC = "HDFC";
        private static final String EMI_TENURE = "3";

        /**
         * Paise string as sent on bank-offer link create ({@code "350000"} = ₹3500.00).
         */
        private static final String BANK_OFFER_LINK_PRICE_PAISE_STR = "350000";
        private static final long BANK_OFFER_LINK_PRICE_PAISE = 350_000L;

        /**
         * Sample cashier/mobile contract from bank-offer link API
         * ({@link DealsLinkCreate}); appended UUID avoids duplicate
         * invoice collisions across runs.
         */
        private static final String BANK_OFFER_LINK_MOBILE = "9898989898";

        private static final String BANK_OFFER_LINK_INVOICE_PREFIX = "Yayays";
        private static final String BANK_OFFER_LINK_DESCRIPTION = "Hahah";

        /** Credit-card pay-in-full link — tenure {@code "0"} on create-link payload. */
        private static final String CC_BANK_LINK_INVOICE_PREFIX = "Hghegeg";
        private static final String CC_BANK_LINK_DESCRIPTION = "Hssh";
        private static final String CC_PAY_IN_FULL_TENURE = "0";

        private static final Filter PLE_CURL_LOGGER = new Filter() {
                @Override
                public Response filter(
                                FilterableRequestSpecification req,
                                FilterableResponseSpecification res,
                                FilterContext ctx) {
                        if (log.isDebugEnabled()) {
                                StringBuilder curl = new StringBuilder("curl --compressed");
                                String method = req.getMethod();
                                if (!"GET".equalsIgnoreCase(method)) {
                                        curl.append(" -X ").append(method);
                                }
                                for (Header h : req.getHeaders()) {
                                        curl.append(" \\\n  -H '").append(h.getName()).append(": ").append(h.getValue())
                                                        .append("'");
                                }
                                String body = req.getBody() != null ? req.getBody().toString() : null;
                                if (body != null && !body.isEmpty()) {
                                        curl.append(" \\\n  --data '").append(body).append("'");
                                }
                                curl.append(" \\\n  '").append(req.getURI()).append("'");
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
        @Feature("PLE-PaymentLink-Bank-EMI-Offer")
        @Parameters({ "theme" })
        @Test(description = "PLE bank-offer EMI payment link: ,, createLink, pay on link, invoices & history")
        public void pleBankOfferEmiPaymentLinkEndToEnd(@Optional("checkoutjs_web_revamp_2") String theme)
                        throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        userManager.getForRead(Label.BASIC);
                        String pgMid = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;
                        String storeReferer = dealsBase + "/pgmid/" + pgMid + "/store?prevScreen=store_page";

                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(storeReferer)
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("merchant/details").isEqualTo(200);

                        Response storeRes = new DealsStoreDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(storeReferer)
                                        .build()
                                        .execute();
                        Assertions.assertThat(storeRes.getStatusCode()).as("store/details").isEqualTo(200);

                        Response emiAllRes = new DealsEmiAll.Builder()
                                        .baseUri(dealsBase)
                                        .merchantAndLineId(pgMid)
                                        .price(BANK_OFFER_LINK_PRICE_PAISE)
                                        .referer(DealsEmiAll.refererCheckForEmiPlansGuest(dealsBase, pgMid))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all amount-based").isEqualTo(200);

                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .payMethod("EMI")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .referer(storeReferer)
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        Assertions.assertThat(paymodesJp.getBoolean("isStoreQrLinkPaymentEnable")).isTrue();

                        String invoiceNumber = BANK_OFFER_LINK_INVOICE_PREFIX + "-"
                                        + UUID.randomUUID().toString().substring(0, 8);

                        DealsLinkCreate createLinkApi = new DealsLinkCreate.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .flowType("EMI_APP")
                                        .referer(DealsEmiAll.refererCheckForEmiPlansGuest(dealsBase, pgMid))
                                        .createLinkConfig(null)
                                        .mobileNumber(BANK_OFFER_LINK_MOBILE)
                                        .invoiceNumber(invoiceNumber)
                                        .linkDescription(BANK_OFFER_LINK_DESCRIPTION)
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .price(BANK_OFFER_LINK_PRICE_PAISE_STR)
                                        .payMethod("EMI")
                                        .tenure(EMI_TENURE)
                                        .imeiNo("")
                                        .cashierMobileNumber(BANK_OFFER_LINK_MOBILE)
                                        .build();

                        Response createLinkRes = createLinkApi.execute();
                        Assertions.assertThat(createLinkRes.getStatusCode()).as("link/createLink").isEqualTo(200);
                        JsonPath clJp = createLinkRes.jsonPath();

                        String shortUrl = clJp.getString("shortUrl");
                        String linkId = clJp.getString("linkId");

                        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
                        linkPaymentLoginPage.OpenEdcLink(shortUrl);
                        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                        cashierPage.waitUntilLoads();
                        cashierPage.refresh();

                        linkPaymentLoginPage.fillEdcCardDetailsAndPayNew(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                        Thread.sleep(20000);

                        Response orderSummaryRes = new DealsEmiOrderSummary.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .orderId(linkId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId
                                                        + "?isPaytmLovesEmiFlow=true")
                                        .build()
                                        .execute();
                        Assertions.assertThat(orderSummaryRes.getStatusCode()).as("orderSummary").isEqualTo(200);
                        JsonPath osJp = orderSummaryRes.jsonPath();

                        String orderId = osJp.getString("successDetails.orderId");
                        String displayAmount = osJp.getString("successDetails.txnAmount");
                        String displayDate = osJp.getString("successDetails.time");
                        String merchantTransId = orderId;

                        log.info("orderId from order summary: {}", orderId);
                        Assertions.assertThat(orderId).as("orderId from order summary").isNotNull().isNotBlank();

                        String txnAmtForSms = displayAmount;
                        if (txnAmtForSms == null || txnAmtForSms.isEmpty()) {
                                txnAmtForSms = String.format("%,d", BANK_OFFER_LINK_PRICE_PAISE / 100);
                        }
                        if (displayDate == null) {
                                displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
                        }

                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(pgMid)
                                        .orderId(orderId)
                                        .phoneNo(BANK_OFFER_LINK_MOBILE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(txnAmtForSms)
                                        .transactionDate(displayDate)
                                        .paymentMethod("LINK")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .utmSource("login-via-username")
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        Assertions.assertThat(smsRes.jsonPath().getString("status")).isEqualTo("SUCCESS");

                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .merchantTransId(merchantTransId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId + "?linkId="
                                                        + linkId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        JsonPath invResJp = invRes.jsonPath();
                        Assertions.assertThat(invResJp.getString("body.resultStatus")).isEqualTo("SUCCESS");
                        Assertions.assertThat(invResJp.getString("body.resultMsg")).isEqualTo("Success");

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
                        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId))
                                        .as("payment/transaction/history contains orderId %s", orderId)
                                        .isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-PaymentLink-Bank-CC-Offer")
        @Parameters({ "theme" })
        @Test(description = "PLE bank-offer CC pay-in-full payment link: amount-based emi/all, createLink (CREDIT_CARD tenure 0), pay on link, invoices & history")
        public void pleBankOfferCreditCardPaymentLinkEndToEnd(@Optional("checkoutjs_web_revamp_2") String theme)
                        throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        userManager.getForRead(Label.BASIC);
                        String pgMid = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;
                        String storeReferer = dealsBase + "/pgmid/" + pgMid + "/store?prevScreen=store_page";

                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(storeReferer)
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("merchant/details").isEqualTo(200);

                        Response storeRes = new DealsStoreDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(storeReferer)
                                        .build()
                                        .execute();
                        Assertions.assertThat(storeRes.getStatusCode()).as("store/details").isEqualTo(200);

                        Response emiAllRes = new DealsEmiAll.Builder()
                                        .baseUri(dealsBase)
                                        .merchantAndLineId(pgMid)
                                        .price(BANK_OFFER_LINK_PRICE_PAISE)
                                        .referer(DealsEmiAll.refererCheckForEmiPlansGuest(dealsBase, pgMid))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all amount-based").isEqualTo(200);

                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .payMethod("CREDIT_CARD")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .referer(storeReferer)
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        Assertions.assertThat(paymodesJp.getBoolean("isStoreQrLinkPaymentEnable")).isTrue();

                        String invoiceNumber = CC_BANK_LINK_INVOICE_PREFIX + "-"
                                        + UUID.randomUUID().toString().substring(0, 8);

                        DealsLinkCreate createLinkApi = new DealsLinkCreate.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .flowType("EMI_APP")
                                        .referer(DealsEmiAll.refererCheckForEmiPlansGuest(dealsBase, pgMid))
                                        .createLinkConfig(null)
                                        .mobileNumber(BANK_OFFER_LINK_MOBILE)
                                        .invoiceNumber(invoiceNumber)
                                        .linkDescription(CC_BANK_LINK_DESCRIPTION)
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .price(BANK_OFFER_LINK_PRICE_PAISE_STR)
                                        .payMethod("CREDIT_CARD")
                                        .tenure(CC_PAY_IN_FULL_TENURE)
                                        .imeiNo("")
                                        .cashierMobileNumber(BANK_OFFER_LINK_MOBILE)
                                        .build();

                        Response createLinkRes = createLinkApi.execute();
                        Assertions.assertThat(createLinkRes.getStatusCode()).as("link/createLink").isEqualTo(200);
                        JsonPath clJp = createLinkRes.jsonPath();

                        String shortUrl = clJp.getString("shortUrl");
                        String linkId = clJp.getString("linkId");

                        LinkPaymentLoginPage linkPaymentLoginPage = new LinkPaymentLoginPage();
                        linkPaymentLoginPage.OpenEdcLinkFS(shortUrl);
                        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                        cashierPage.waitUntilLoads();
                        cashierPage.refresh();

                        linkPaymentLoginPage.fillEdcCardDetailsAndPayFSNew(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                        Thread.sleep(20000);

                        Response orderSummaryRes = new DealsEmiOrderSummary.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .orderId(linkId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId
                                                        + "?isPaytmLovesEmiFlow=true")
                                        .build()
                                        .execute();
                        Assertions.assertThat(orderSummaryRes.getStatusCode()).as("orderSummary").isEqualTo(200);
                        JsonPath osJp = orderSummaryRes.jsonPath();

                        String orderId = osJp.getString("successDetails.orderId");
                        String displayAmount = osJp.getString("successDetails.txnAmount");
                        String displayDate = osJp.getString("successDetails.time");
                        String merchantTransId = orderId;

                        log.info("orderId from order summary: {}", orderId);
                        Assertions.assertThat(orderId).as("orderId from order summary").isNotNull().isNotBlank();

                        String txnAmtForSms = displayAmount;
                        if (txnAmtForSms == null || txnAmtForSms.isEmpty()) {
                                txnAmtForSms = String.format("%,d", BANK_OFFER_LINK_PRICE_PAISE / 100);
                        }
                        if (displayDate == null) {
                                displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
                        }

                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(pgMid)
                                        .orderId(orderId)
                                        .phoneNo(BANK_OFFER_LINK_MOBILE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(txnAmtForSms)
                                        .transactionDate(displayDate)
                                        .paymentMethod("LINK")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .utmSource("login-via-username")
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        Assertions.assertThat(smsRes.jsonPath().getString("status")).isEqualTo("SUCCESS");

                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .merchantTransId(merchantTransId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + linkId + "?linkId="
                                                        + linkId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        JsonPath invResJp = invRes.jsonPath();
                        Assertions.assertThat(invResJp.getString("body.resultStatus")).isEqualTo("SUCCESS");
                        Assertions.assertThat(invResJp.getString("body.resultMsg")).isEqualTo("Success");

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
                        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId))
                                        .as("payment/transaction/history contains orderId %s", orderId)
                                        .isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }

}
