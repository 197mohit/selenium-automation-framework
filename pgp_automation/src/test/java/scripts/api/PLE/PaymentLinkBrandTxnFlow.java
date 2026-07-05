package scripts.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.api.PLE.DealsEmiAll;
import com.paytm.api.PLE.DealsMerchantDetails;
import com.paytm.api.PLE.DealsEmiGetInvoice;
import com.paytm.api.PLE.DealsEmiOrderSummary;
import com.paytm.api.PLE.DealsLinkCreate;
import com.paytm.api.PLE.DealsPaymentTransactionHistory;
import com.paytm.api.PLE.DealsShareInvoiceViaSms;
import com.paytm.api.PLE.DealsStoreDetails;
import com.paytm.api.PLE.DealsStorePaymodes;
import com.paytm.api.PLE.DealsStoreProductDetails;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.LinkPaymentLoginPage;
import java.util.List;

import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import io.restassured.RestAssured;
import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.http.Header;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Collections;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * End-to-end PLE Payment Link flows — store → product → EMI or CREDIT_CARD →
 * paymodes → create link → pay on link → order summary → share invoice → get
 * invoice → txn history (page 1, size 10).
 */
public class PaymentLinkBrandTxnFlow extends PGPBaseTest {

        private static final Logger log = LoggerFactory.getLogger(PaymentLinkBrandTxnFlow.class);
        private static final String PRODUCT_ID = "1235528546";
        private static final String CASHIER_PHONE = "9972746530";
        private static final String MERCHANT_DISPLAY_NAME = "Emi Store Testing";
        private static final String ISSUING_BANK_HDFC = "HDFC";
        private static final String EMI_TENURE = "3";

        /**
         * Create-link body: store-attendant CREDIT_CARD sample (price in paise as string).
         */
        private static final String CC_LINK_MOBILE = "9898898998";
        private static final String CC_INVOICE_NUMBER = "Gagagag";
        private static final String CC_LINK_DESCRIPTION = "Ysywy";
        private static final String CC_IMEI = "Hahah";
        private static final String CC_CASHIER_PHONE = "9898989898";
        private static final String CC_PRICE = "1000000";
        private static final String CC_PAY_METHOD = "CREDIT_CARD";
        private static final String CC_TENURE = "0";

        /**
         * RestAssured filter that logs each outgoing request as a copy-pasteable
         * {@code curl} command at DEBUG level.
         * Registered/unregistered per test run so it does not leak into other test
         * classes.
         */
        private static final Filter PLE_CURL_LOGGER = new Filter() {
                @Override
                public io.restassured.response.Response filter(
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
                                        curl.append(" \\\n  -H '").append(h.getName())
                                                        .append(": ").append(h.getValue()).append("'");
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

        private static String refererProduct(String dealsBase, String pgmid, String productId) {
                return String.format(
                                "%s/pgmid/%s/products/%s?isPaytmLovesEmiFlow=true&utm_source=login-via-username&prevScreen=product_search_page",
                                dealsBase, pgmid, productId);
        }

        private static String refererPlanSummary(String dealsBase, String pgmid, String productId, int brandId,
                        long pricePaise) {
                return String.format(
                                "%s/pgmid/%s/plan-summary?isPaytmLovesEmiFlow=true&utm_source=login-via-username&prevScreen=product_details_page&issuingBank=%s&payMethod=EMI&price=%d&tenure=%s&brandId=%d&productId=%s",
                                dealsBase, pgmid, ISSUING_BANK_HDFC, pricePaise, EMI_TENURE, brandId, productId);
        }

        private static String refererPlanSummaryCreditCard(String dealsBase, String pgmid, String productId,
                        int brandId,
                        long pricePaise) {
                return String.format(
                                "%s/pgmid/%s/plan-summary?isPaytmLovesEmiFlow=true&utm_source=login-via-username&prevScreen=product_details_page&issuingBank=%s&payMethod=CREDIT_CARD&price=%d&tenure=%s&brandId=%d&productId=%s",
                                dealsBase, pgmid, ISSUING_BANK_HDFC, pricePaise, CC_TENURE, brandId, productId);
        }

        private boolean transactionHistoryResponseContainsOrderId(Response response, String orderId) {
                List<String> merchantTransIds = response.jsonPath()
                                .getList("orderList.merchantTransId", String.class);

                List<String> pleIds = response.jsonPath()
                                .getList("orderList.pleId", String.class);

                return (merchantTransIds != null && merchantTransIds.contains(orderId)) ||
                                (pleIds != null && pleIds.contains(orderId));
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-PaymentLinkBrand-EMI")
        @Parameters({ "theme" })
        @Test(description = "PLE brand EMI: Deals APIs, EMI link, pay on link page in browser  order summary, invoice, txn history")
        public void pleBrandEmiPaymentLinkEndToEnd(@Optional("checkoutjs_web_revamp") String theme) throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        userManager.getForRead(Label.BASIC);
                        String PG_MID = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;

                        // 1) Merchant details
                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .referer(refererProduct(dealsBase, PG_MID, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("merchant/details").isEqualTo(200);

                        // 2) Store details
                        Response storeRes = new DealsStoreDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/store?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(storeRes.getStatusCode()).as("store/details").isEqualTo(200);

                        // 3) Product details → drive EMI + link
                        Response productRes = new DealsStoreProductDetails.Builder()
                                        .baseUri(dealsBase)
                                        .productId(PRODUCT_ID)
                                        .referer(refererProduct(dealsBase, PG_MID, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(productRes.getStatusCode()).as("store/products").isEqualTo(200);
                        JsonPath productJp = productRes.jsonPath();

                        int brandId = productJp.getInt("brandId");
                        int categoryId = productJp.getInt("categoryId");
                        String model = productJp.getString("model");
                        long priceFromProduct = productJp.getLong("price");

                        Assertions.assertThat(brandId).as("brandId from product").isNotNull();
                        Assertions.assertThat(categoryId).as("categoryId from product").isNotNull();
                        Assertions.assertThat(model).as("model/sku from product").isNotNull();
                        long pricePaise = priceFromProduct * 100;

                        // 5) EMI all (uses product-derived item)
                        Response emiAllRes = new DealsEmiAll.Builder()
                                        .baseUri(dealsBase)
                                        .productId(PRODUCT_ID)
                                        .brandId(brandId)
                                        .categoryId(categoryId)
                                        .model(model)
                                        .price(pricePaise)
                                        .quantity(1)
                                        .merchantId(PG_MID)
                                        .transactionAmount(pricePaise)
                                        .referer(refererProduct(dealsBase, PG_MID, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all").isEqualTo(200);

                        // 6) Paymodes + flag
                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .brandId(brandId)
                                        .payMethod("EMI")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .referer(refererPlanSummary(dealsBase, PG_MID, PRODUCT_ID, brandId, pricePaise))
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        boolean isStoreQrLinkPaymentEnable = paymodesJp.getBoolean("isStoreQrLinkPaymentEnable");
                        Assertions.assertThat(isStoreQrLinkPaymentEnable).isTrue();

                        // 7) Create EMI link (tenure 3, HDFC)
                        String invoice = "INV-" + UUID.randomUUID().toString().substring(0, 8);
                        String priceStr = String.valueOf(pricePaise);
                        DealsLinkCreate createLinkApi = new DealsLinkCreate.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .flowType("EMI_APP")
                                        .referer(refererPlanSummary(dealsBase, PG_MID, PRODUCT_ID, brandId, pricePaise))
                                        .mobileNumber(CASHIER_PHONE)
                                        .cashierMobileNumber(CASHIER_PHONE)
                                        .invoiceNumber(invoice)
                                        .linkDescription("PLE automation EMI link")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .productId(PRODUCT_ID)
                                        .price(priceStr)
                                        .payMethod("EMI")
                                        .tenure(EMI_TENURE)
                                        .imeiNo("auto-" + UUID.randomUUID().toString().substring(0, 8))
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

                        // E2E Txn Successfull
                        linkPaymentLoginPage.fillEdcCardDetailsAndPayNew(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                        Thread.sleep(20000);

                        // 9) Order summary (path segment = link id)
                        Response orderSummaryRes = new DealsEmiOrderSummary.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .orderId(linkId)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + linkId
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
                        Assertions.assertThat(orderId).as("orderId from order summary (orderDetail / successDetails)")
                                        .isNotNull()
                                        .isNotBlank();

                        String txnAmtForSms = displayAmount;
                        if (txnAmtForSms == null || txnAmtForSms.isEmpty()) {
                                txnAmtForSms = String.format("%,d", pricePaise / 100);
                        }
                        if (displayDate == null) {
                                displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
                        }

                        // 10) Share invoice SMS
                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + linkId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(PG_MID)
                                        .orderId(orderId)
                                        .phoneNo(CASHIER_PHONE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(txnAmtForSms)
                                        .transactionDate(displayDate)
                                        .paymentMethod("LINK")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .utmSource("login-via-username")
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        JsonPath smsJp = smsRes.jsonPath();
                        String smsStatus = smsJp.getString("status");
                        Assertions.assertThat(smsStatus).isEqualTo("SUCCESS");

                        // 11) Get invoice PDF/meta
                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .merchantTransId(merchantTransId)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + linkId + "?linkId="
                                                        + linkId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        JsonPath invResJp = invRes.jsonPath();

                        String resultStatus = invResJp.getString("body.resultStatus");
                        String resultMsg = invResJp.getString("body.resultMsg");
                        Assertions.assertThat(resultStatus).isEqualTo("SUCCESS");
                        Assertions.assertThat(resultMsg).isEqualTo("Success");

                        // 12) Transaction history — today, page 1 only, page size 10
                        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        Response hRes = new DealsPaymentTransactionHistory.Builder()
                                        .baseUri(dealsBase)
                                        .mid(PG_MID)
                                        .pageNum(1)
                                        .nextPageNum(1)
                                        .size(10)
                                        .startDate(today)
                                        .endDate(today)
                                        .referer(dealsBase + "/pgmid/" + PG_MID
                                                        + "/emi/transaction-queue?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(hRes.getStatusCode()).as("payment/transaction/history page ")
                                        .isEqualTo(200);
                        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId))
                                        .as("payment/transaction/history response contains orderId : ", orderId)
                                        .isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-PaymentLinkBrand-CREDIT_CARD")
        @Parameters({ "theme" })
        @Test(description = "PLE brand CREDIT_CARD: Deals APIs, CC link, pay on link page, order summary, invoice, txn history")
        public void pleBrandCreditCardPaymentLinkEndToEnd(@Optional("checkoutjs_web_revamp") String theme)
                        throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        userManager.getForRead(Label.BASIC);
                        String PG_MID = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;
                        long linkPricePaise = Long.parseLong(CC_PRICE);

                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .referer(refererProduct(dealsBase, PG_MID, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("merchant/details").isEqualTo(200);

                        Response storeRes = new DealsStoreDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/store?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(storeRes.getStatusCode()).as("store/details").isEqualTo(200);

                        Response productRes = new DealsStoreProductDetails.Builder()
                                        .baseUri(dealsBase)
                                        .productId(PRODUCT_ID)
                                        .referer(refererProduct(dealsBase, PG_MID, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(productRes.getStatusCode()).as("store/products").isEqualTo(200);
                        JsonPath productJp = productRes.jsonPath();

                        int brandId = productJp.getInt("brandId");
                        int categoryId = productJp.getInt("categoryId");
                        String model = productJp.getString("model");
                        long priceFromProduct = productJp.getLong("price");

                        Assertions.assertThat(brandId).as("brandId from product").isNotNull();
                        Assertions.assertThat(categoryId).as("categoryId from product").isNotNull();
                        Assertions.assertThat(model).as("model/sku from product").isNotNull();
                        long pricePaise = priceFromProduct * 100;

                        Response emiAllRes = new DealsEmiAll.Builder()
                                        .baseUri(dealsBase)
                                        .productId(PRODUCT_ID)
                                        .brandId(brandId)
                                        .categoryId(categoryId)
                                        .model(model)
                                        .price(pricePaise)
                                        .quantity(1)
                                        .merchantId(PG_MID)
                                        .transactionAmount(pricePaise)
                                        .referer(refererProduct(dealsBase, PG_MID, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all").isEqualTo(200);

                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .brandId(brandId)
                                        .payMethod(CC_PAY_METHOD)
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .referer(refererPlanSummaryCreditCard(dealsBase, PG_MID, PRODUCT_ID, brandId,
                                                        linkPricePaise))
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        boolean isStoreQrLinkPaymentEnable = paymodesJp.getBoolean("isStoreQrLinkPaymentEnable");
                        Assertions.assertThat(isStoreQrLinkPaymentEnable).isTrue();

                        DealsLinkCreate createLinkApi = new DealsLinkCreate.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .flowType("EMI_APP")
                                        .referer(refererPlanSummaryCreditCard(dealsBase, PG_MID, PRODUCT_ID, brandId,
                                                        linkPricePaise))
                                        .createLinkConfig(null)
                                        .mobileNumber(CC_LINK_MOBILE)
                                        .cashierMobileNumber(CC_CASHIER_PHONE)
                                        .invoiceNumber(CC_INVOICE_NUMBER)
                                        .linkDescription(CC_LINK_DESCRIPTION)
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .productId(PRODUCT_ID)
                                        .price(CC_PRICE)
                                        .payMethod(CC_PAY_METHOD)
                                        .tenure(CC_TENURE)
                                        .imeiNo(CC_IMEI)
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
                                        .pgmid(PG_MID)
                                        .orderId(linkId)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + linkId
                                                        + "?isPaytmLovesEmiFlow=true")
                                        .build()
                                        .execute();
                        Assertions.assertThat(orderSummaryRes.getStatusCode()).as("orderSummary").isEqualTo(200);
                        JsonPath osJp = orderSummaryRes.jsonPath();

                        String orderId = osJp.getString("successDetails.orderId");
                        String displayAmount = osJp.getString("successDetails.txnAmount");
                        String displayDate = osJp.getString("successDetails.time");
                        String merchantTransId = orderId;

                        String txnAmtForSms = displayAmount;

                        if (displayDate == null) {
                                displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));
                        }

                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + linkId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(PG_MID)
                                        .orderId(orderId)
                                        .phoneNo(CC_CASHIER_PHONE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(txnAmtForSms)
                                        .transactionDate(displayDate)
                                        .paymentMethod("LINK")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .utmSource("login-via-username")
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        JsonPath smsJp = smsRes.jsonPath();
                        String smsStatus = smsJp.getString("status");
                        Assertions.assertThat(smsStatus).isEqualTo("SUCCESS");

                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(PG_MID)
                                        .merchantTransId(merchantTransId)
                                        .referer(dealsBase + "/pgmid/" + PG_MID + "/order/" + linkId + "?linkId="
                                                        + linkId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        JsonPath invResJp = invRes.jsonPath();

                        String resultStatus = invResJp.getString("body.resultStatus");
                        String resultMsg = invResJp.getString("body.resultMsg");
                        Assertions.assertThat(resultStatus).isEqualTo("SUCCESS");
                        Assertions.assertThat(resultMsg).isEqualTo("Success");

                        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        Response hRes = new DealsPaymentTransactionHistory.Builder()
                                        .baseUri(dealsBase)
                                        .mid(PG_MID)
                                        .pageNum(1)
                                        .nextPageNum(1)
                                        .size(10)
                                        .startDate(today)
                                        .endDate(today)
                                        .referer(dealsBase + "/pgmid/" + PG_MID
                                                        + "/emi/transaction-queue?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(hRes.getStatusCode()).as("payment/transaction/history page ")
                                        .isEqualTo(200);
                        Assertions.assertThat(transactionHistoryResponseContainsOrderId(hRes, orderId))
                                        .as("payment/transaction/history response contains orderId : ", orderId)
                                        .isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }
}
