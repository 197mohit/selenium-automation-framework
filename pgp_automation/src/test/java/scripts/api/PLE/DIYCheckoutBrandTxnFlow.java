package scripts.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.api.PLE.DealsEmiAll;
import com.paytm.api.PLE.DealsEmiGetInvoice;
import com.paytm.api.PLE.DealsMerchantDetails;
import com.paytm.api.PLE.DealsPaymentTransactionHistory;
import com.paytm.api.PLE.DealsShareInvoiceViaSms;
import com.paytm.api.PLE.DealsStoreDetails;
import com.paytm.api.PLE.DealsStorePaymodes;
import com.paytm.api.PLE.DealsStoreProductDetails;
import com.paytm.api.PLE.DealsUserInitiateTransaction;
import com.paytm.api.PLE.EmiAllOfferExtractor;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * PLE DIY brand flows — store → product → EMI/all → paymodes →
 * {@code /api/user/initiateTransaction} (PG_LINK payload shape),
 * Checkout JS payment, then share-invoice SMS, get invoice, and transaction
 * history (page 1, size 10).
 *
 * <p>
 * After Deals {@code initiateTransaction}, {@link InitTxnDTO} satisfies
 * {@link com.paytm.pages.CheckoutJsCheckoutPage#loadMerchantConfig(InitTxnDTO, String)};
 * {@code txnToken} comes from the Deals response onto
 * {@link com.paytm.dto.checkoutjs.MerchantConfig}.
 *
 * <p>
 * Checkout JS uses <b>QA14</b> hosts ({@link PleQa14CheckoutJsCheckoutPage})
 * by default. Set {@code -Dple.checkoutjs.useQa14=false} for
 * {@link LocalConfig#CHECKOUTJS_URL} / {@link LocalConfig#CHECKOUTJS_LOAD_URL}.
 *
 * <p>
 * Optional: {@code ple.deals.cookie} forwards session {@code Cookie} for init.
 */
public class DIYCheckoutBrandTxnFlow extends PGPBaseTest {

        private static final Logger log = LoggerFactory.getLogger(DIYCheckoutBrandTxnFlow.class);
        private static final String PRODUCT_ID = "1235528546";

        /** userInfo.mobile / custId in {@code /api/user/initiateTransaction} (PG_LINK sample). */
        private static final String CASHIER_PHONE = "9494949497";
        private static final String MERCHANT_DISPLAY_NAME = "Emi Store Testing";
        /** Initiate: deviceIMEI + items[].imei (sample curl contract). */
        private static final String INIT_SAMPLE_IMEI = "Hahahh";
        /** Initiate: items[] + brandInvoiceNumber (sample curl contract). */
        private static final String INIT_SAMPLE_INVOICE = "Wywy";
        private static final String ISSUING_BANK_HDFC = "HDFC";
        private static final String EMI_TENURE = "3";
        /** Pay-in-full plan summary / initiate tenure identifier. */
        private static final String PAY_IN_FULL_TENURE = "0";

        private static CheckoutJsCheckoutPage pleCheckoutPage() {
                if (Boolean.parseBoolean(System.getProperty("ple.checkoutjs.useQa14", "true"))) {
                        return new PleQa14CheckoutJsCheckoutPage();
                }
                return new CheckoutJsCheckoutPage();
        }

        private final CheckoutJsCheckoutPage checkoutPage = pleCheckoutPage();

        /**
         * Same wire shape RestAssured sends — copy-paste {@code curl} for debugging
         * (logs initiateTxn at INFO).
         */
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

        /**
         * Walks parsed JSON for a field whose name is an order-id key with string value
         * matching {@code orderId}.
         * Handles nested objects/arrays regardless of Deals history envelope
         * ({@code response}, {@code data}, list items, …).
         */
        private boolean transactionHistoryResponseContainsOrderId(Response response, String orderId) {
                List<String> merchantTransIds = response.jsonPath()
                                .getList("orderList.merchantTransId", String.class);

                List<String> pleIds = response.jsonPath()
                                .getList("orderList.pleId", String.class);

                return (merchantTransIds != null && merchantTransIds.contains(orderId)) ||
                                (pleIds != null && pleIds.contains(orderId));
        }

        /**
         * Initiate {@code simplifiedUnifiedOffers.subventionDetails.offerId}: for
         * HDFC_EMI {@code allEmiOptions} payloads,
         * use {@code plans[].emiOfferDetails.offerId} (e.g. 2499765); otherwise keep
         * {@code addOffer}/legacy subvention id.
         */
        private static String resolveSubventionOfferIdForInitiate(EmiAllOfferExtractor.HdfcEmiResult hdfcEmi) {
                String fromEmiOfferDetails = hdfcEmi.getEmiPlanOfferId();
                if (fromEmiOfferDetails != null && !fromEmiOfferDetails.isEmpty()) {
                        return fromEmiOfferDetails;
                }
                return hdfcEmi.getSubventionOfferId();
        }

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

        private static String refererPlanSummaryPayInFull(
                        String dealsBase, String pgmid, String productId, int brandId, long pricePaise) {
                return String.format(
                                "%s/pgmid/%s/plan-summary?isPaytmLovesEmiFlow=true&utm_source=login-via-username&prevScreen=product_details_page&issuingBank=%s&payMethod=EMI&price=%d&tenure=%s&brandId=%d&productId=%s",
                                dealsBase, pgmid, ISSUING_BANK_HDFC, pricePaise, PAY_IN_FULL_TENURE, brandId,
                                productId);
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-DIYCheckoutBrand-EMI")
        @Parameters({ "theme" })
        @Test(description = "PLE brand EMI DIY: Deals APIs, initiateTransaction, Checkout JS pay, then Share invoice SMS, Get invoice, Transaction history")
        public void pleBrandEmiDiyCheckoutEndToEnd(@Optional("checkoutjs_web_revamp_2") String theme) throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        User user = userManager.getForRead(Label.BASIC);
                        String pgMid = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;

                        // 1) Merchant details
                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(refererProduct(dealsBase, pgMid, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(merchantRes.getStatusCode()).as("merchant/details").isEqualTo(200);

                        // 2) Store details
                        Response storeRes = new DealsStoreDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/store?prevScreen=store_page")
                                        .build()
                                        .execute();
                        Assertions.assertThat(storeRes.getStatusCode()).as("store/details").isEqualTo(200);

                        // 3) Product details → drive EMI + link
                        Response productRes = new DealsStoreProductDetails.Builder()
                                        .baseUri(dealsBase)
                                        .productId(PRODUCT_ID)
                                        .referer(refererProduct(dealsBase, pgMid, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(productRes.getStatusCode()).as("store/products").isEqualTo(200);
                        JsonPath productJp = productRes.jsonPath();

                        int brandId = productJp.getInt("brandId");
                        int categoryId = productJp.getInt("categoryId");
                        String model = productJp.getString("model");
                        String brandNameStr = productJp.getString("brandName");
                        String categoryNameStr = productJp.getString("categoryName");
                        String initProductName = productJp.getString("name");
                        long priceFromProduct = productJp.getLong("price");
                        String rupeeAmount = String.valueOf(priceFromProduct);

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
                                        .merchantId(pgMid)
                                        .transactionAmount(pricePaise)
                                        .referer(refererProduct(dealsBase, pgMid, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all").isEqualTo(200);

                        EmiAllOfferExtractor.HdfcEmiResult hdfcEmi = EmiAllOfferExtractor
                                        .extractForHdfcEmi(emiAllRes.getBody().asString(), EMI_TENURE);
                        String subventionOfferIdForInitiate = resolveSubventionOfferIdForInitiate(hdfcEmi);
                        log.info(
                                        "[EMI/ALL] HDFC EMI tenure={} subventionOfferId(addOffer/legacy)={} emiOfferDetails.offerId={} "
                                                        + "subventionForInitiate={} bankOfferIds(emi)={} initiate bankOfferDetails order={} addOffer.total={}",
                                        EMI_TENURE,
                                        hdfcEmi.getSubventionOfferId(),
                                        hdfcEmi.getEmiPlanOfferId(),
                                        subventionOfferIdForInitiate,
                                        Arrays.toString(hdfcEmi.getBankOfferIds()),
                                        Arrays.toString(hdfcEmi.getInitiateTransactionBankOfferIds()),
                                        hdfcEmi.getAddOffer() != null ? hdfcEmi.getAddOffer().opt("total") : null);

                        // 6) Paymodes + flag
                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .brandId(brandId)
                                        .payMethod("EMI")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .referer(refererPlanSummary(dealsBase, pgMid, PRODUCT_ID, brandId, pricePaise))
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        boolean isStoreQrDIYCheckoutEnable = paymodesJp.getBoolean("isStoreQrDIYCheckoutEnable");
                        Assertions.assertThat(isStoreQrDIYCheckoutEnable).isTrue();

                        // 7) Initiate (POST /api/user/initiateTransaction) — PG_LINK shape: subvention
                        // + items[].offerDetails
                        // bankOfferDetails = emi/all addOffer.items offerIds in order (e.g. 2408209,
                        // 2512658, …), not the deduped set.
                        DealsUserInitiateTransaction.Builder initTxnBuilder = new DealsUserInitiateTransaction.Builder()
                                        .baseUri(dealsBase)
                                        .referer(refererPlanSummary(dealsBase, pgMid, PRODUCT_ID, brandId, pricePaise))
                                        .buildVersion("S26.04.17")
                                        .mid(pgMid)
                                        .productId(PRODUCT_ID)
                                        .brandId(String.valueOf(brandId))
                                        .categoryId(categoryId)
                                        .model(model)
                                        .skuCode(model)
                                        .itemPrice(rupeeAmount)
                                        .txnAmountValue(rupeeAmount)
                                        .productName(initProductName)
                                        .brandName(brandNameStr)
                                        .categoryName(categoryNameStr)
                                        .modelName(model)
                                        .subventionOfferId(subventionOfferIdForInitiate)
                                        .subventionAmount(rupeeAmount)
                                        .deviceImei(INIT_SAMPLE_IMEI)
                                        .brandInvoiceNumber(INIT_SAMPLE_INVOICE)
                                        .userMobile(CASHIER_PHONE)
                                        .callbackUrl(DealsUserInitiateTransaction.paymentReceiptCallbackUrl(dealsBase))
                                        .bankOfferIds(hdfcEmi.getInitiateTransactionBankOfferIds())
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .tenure(EMI_TENURE);

                        Response initRes = initTxnBuilder.build().execute();
                        Assertions.assertThat(initRes.getStatusCode()).as("user/initiateTransaction").isEqualTo(200);
                        JsonPath initJp = initRes.jsonPath();

                        // When API wraps payload in mappedResponse, assert Paytm result contract before
                        // using txnToken / orderId.
                        String orderId = initJp.getString("mappedResponse.orderId");
                        String txnToken = initJp.getString("mappedResponse.txnToken");
                        String resultStatus = initJp.getString("mappedResponse.resultInfo.resultStatus");
                        String resultCode = initJp.getString("mappedResponse.resultInfo.resultCode");
                        String pgOrderId = orderId;

                        Assertions.assertThat(resultStatus).isNotNull().isEqualTo("S");
                        Assertions.assertThat(resultCode).isNotNull().isEqualTo("0000");

                        InitTxnDTO initForCheckout = new InitTxnDTO.Builder(
                                        MerchantType.PLE_DEALS_MID, pgOrderId, user.ssoToken())
                                        .setTxnValue(rupeeAmount)
                                        .build();
                        MerchantConfig checkoutConfig = checkoutPage.loadMerchantConfig(initForCheckout, theme);
                        checkoutConfig.data.setToken(txnToken);

                        checkoutPage.createCheckoutJsOrder(checkoutConfig);

                        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                        cashierPage.waitUntilLoads();
                        PGPHelpers.isCheckoutJsV5FPOCalled(pgOrderId);
                        cashierPage.payBy(Constants.PayMode.EMI);
                        if (cashierPage.ErrorRetryButton().isElementPresent()) {
                                cashierPage.ErrorRetryButton().click();
                                cashierPage.payBy(Constants.PayMode.EMI);
                        }

                        String merchantTransId = orderId;
                        String txnAmtForSms = rupeeAmount;
                        String displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));

                        // 11) Share invoice SMS
                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(pgMid)
                                        .orderId(orderId)
                                        .phoneNo(CASHIER_PHONE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(txnAmtForSms)
                                        .transactionDate(displayDate)
                                        .paymentMethod("EMI")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        JsonPath smsJp = smsRes.jsonPath();
                        String smsStatus = smsJp.getString("status");
                        Assertions.assertThat(smsStatus).isEqualTo("SUCCESS");

                        // 12) Get invoice PDF/meta
                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .merchantTransId(merchantTransId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId + "?linkId="
                                                        + orderId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        Assertions.assertThat(invRes.getBody().asString()).isNotBlank();

                        // 13) Transaction history — today, page 1 only, page size 10
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
                                        .as("payment/transaction/history response contains orderId %s", orderId)
                                        .isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }

        @Owner(Constants.Owner.CHAKSHU)
        @Feature("PLE-DIYCheckoutBrand-CC-PayInFull")
        @Parameters({ "theme" })
        @Test(description = "PLE brand DIY pay-in-full CREDIT_CARD: PG_LINK initiate (null subvention, dual bank offers), Checkout JS CC, invoice APIs")
        public void pleBrandDiyCheckoutCreditCardPayInFull(@Optional("checkoutjs_web_revamp_2") String theme)
                        throws Exception {
                RestAssured.filters(PLE_CURL_LOGGER);
                try {
                        User user = userManager.getForRead(Label.BASIC);
                        String pgMid = MerchantType.PLE_DEALS_MID.getId();
                        String dealsBase = LocalConfig.PLE_DEALS_BASE_URL;

                        Response merchantRes = new DealsMerchantDetails.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .referer(refererProduct(dealsBase, pgMid, PRODUCT_ID))
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

                        Response productRes = new DealsStoreProductDetails.Builder()
                                        .baseUri(dealsBase)
                                        .productId(PRODUCT_ID)
                                        .referer(refererProduct(dealsBase, pgMid, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(productRes.getStatusCode()).as("store/products").isEqualTo(200);
                        JsonPath productJp = productRes.jsonPath();

                        int brandId = productJp.getInt("brandId");
                        int categoryId = productJp.getInt("categoryId");
                        String model = productJp.getString("model");
                        String brandNameStr = productJp.getString("brandName");
                        String categoryNameStr = productJp.getString("categoryName");
                        String initProductName = productJp.getString("name");
                        long priceFromProduct = productJp.getLong("price");
                        String rupeeAmount = String.valueOf(priceFromProduct);

                        Assertions.assertThat(brandId).as("brandId from product").isNotNull();
                        Assertions.assertThat(categoryId).as("categoryId from product").isNotNull();
                        Assertions.assertThat(model).as("model/sku from product").isNotNull();
                        long pricePaise = priceFromProduct * 100;

                        // 5) EMI all (product-derived pricing / metadata)
                        Response emiAllRes = new DealsEmiAll.Builder()
                                        .baseUri(dealsBase)
                                        .productId(PRODUCT_ID)
                                        .brandId(brandId)
                                        .categoryId(categoryId)
                                        .model(model)
                                        .price(pricePaise)
                                        .quantity(1)
                                        .merchantId(pgMid)
                                        .transactionAmount(pricePaise)
                                        .referer(refererProduct(dealsBase, pgMid, PRODUCT_ID))
                                        .build()
                                        .execute();
                        Assertions.assertThat(emiAllRes.getStatusCode()).as("emi/all").isEqualTo(200);

                        Response paymodesRes = new DealsStorePaymodes.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .brandId(brandId)
                                        .payMethod("CREDIT_CARD")
                                        .issuingBank(ISSUING_BANK_HDFC)
                                        .referer(refererPlanSummaryPayInFull(dealsBase, pgMid, PRODUCT_ID, brandId,
                                                        pricePaise))
                                        .build()
                                        .execute();
                        Assertions.assertThat(paymodesRes.getStatusCode()).as("store/paymodes").isEqualTo(200);
                        JsonPath paymodesJp = paymodesRes.jsonPath();
                        boolean isStoreQrDIYCheckoutEnable = paymodesJp.getBoolean("isStoreQrDIYCheckoutEnable");
                        Assertions.assertThat(isStoreQrDIYCheckoutEnable).isTrue();

                        EmiAllOfferExtractor.HdfcEmiResult hdfcEmi = EmiAllOfferExtractor
                                        .extractForHdfcEmi(emiAllRes.getBody().asString(), EMI_TENURE);
                        String subventionOfferIdForInitiate = resolveSubventionOfferIdForInitiate(hdfcEmi);
                        log.info(
                                        "[EMI/ALL] HDFC EMI tenure={} subventionOfferId(addOffer/legacy)={} emiOfferDetails.offerId={} "
                                                        + "subventionForInitiate={} bankOfferIds(emi)={} initiate bankOfferDetails order={} addOffer.total={}",
                                        EMI_TENURE,
                                        hdfcEmi.getSubventionOfferId(),
                                        hdfcEmi.getEmiPlanOfferId(),
                                        subventionOfferIdForInitiate,
                                        Arrays.toString(hdfcEmi.getBankOfferIds()),
                                        Arrays.toString(hdfcEmi.getInitiateTransactionBankOfferIds()),
                                        hdfcEmi.getAddOffer() != null ? hdfcEmi.getAddOffer().opt("total") : null);

                        DealsUserInitiateTransaction.Builder initTxnBuilder = new DealsUserInitiateTransaction.Builder()
                                        .baseUri(dealsBase)
                                        .referer(refererPlanSummaryPayInFull(dealsBase, pgMid, PRODUCT_ID, brandId,
                                                        pricePaise))
                                        .buildVersion("S26.04.17")
                                        .mid(pgMid)
                                        .productId(PRODUCT_ID)
                                        .brandId(String.valueOf(brandId))
                                        .categoryId(categoryId)
                                        .model(model)
                                        .skuCode(model)
                                        .itemPrice(rupeeAmount)
                                        .txnAmountValue(rupeeAmount)
                                        .productName(initProductName)
                                        .brandName(brandNameStr)
                                        .categoryName(categoryNameStr)
                                        .modelName(model)
                                        .subventionDetailsNull(true)
                                        .includeUnifiedOffersDeviceImei(false)
                                        .applyAvailablePromo(true)
                                        .amountBasedBankOffer(false)
                                        .isAmountBasedBankOffer(false)
                                        .deviceImei(INIT_SAMPLE_INVOICE)
                                        .brandInvoiceNumber(INIT_SAMPLE_INVOICE)
                                        .userMobile(CASHIER_PHONE)
                                        .callbackUrl(DealsUserInitiateTransaction.paymentReceiptCallbackUrl(dealsBase))
                                        .bankOfferIds(hdfcEmi.getInitiateTransactionBankOfferIds())
                                        .paymentMode("CREDIT_CARD")
                                        .banks(ISSUING_BANK_HDFC)
                                        .tenures(PAY_IN_FULL_TENURE)
                                        .emiType("payInFull")
                                        .emiTypeBank(ISSUING_BANK_HDFC);

                        Response initRes = initTxnBuilder.build().execute();
                        Assertions.assertThat(initRes.getStatusCode()).as("user/initiateTransaction").isEqualTo(200);
                        JsonPath initJp = initRes.jsonPath();

                        String orderId = initJp.getString("mappedResponse.orderId");
                        String txnToken = initJp.getString("mappedResponse.txnToken");
                        String resultStatus = initJp.getString("mappedResponse.resultInfo.resultStatus");
                        String resultCode = initJp.getString("mappedResponse.resultInfo.resultCode");
                        String pgOrderId = orderId;

                        Assertions.assertThat(resultStatus).isNotNull().isEqualTo("S");
                        Assertions.assertThat(resultCode).isNotNull().isEqualTo("0000");

                        InitTxnDTO initForCheckout = new InitTxnDTO.Builder(
                                        MerchantType.PLE_DEALS_MID, pgOrderId, user.ssoToken())
                                        .setTxnValue(rupeeAmount)
                                        .build();
                        MerchantConfig checkoutConfig = checkoutPage.loadMerchantConfig(initForCheckout, theme);
                        checkoutConfig.data.setToken(txnToken);

                        checkoutPage.createCheckoutJsOrder(checkoutConfig);

                        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
                        cashierPage.waitUntilLoads();
                        PGPHelpers.isCheckoutJsV5FPOCalled(pgOrderId);

                        PaymentDTO ccDto = new PaymentDTO()
                                        .setCreditCardNumber(PaymentDTO.VISA_HDFC_EMI_CREDIT_CARD_NUMBER);
                        cashierPage.payBy(Constants.PayMode.CC, ccDto);
                        if (cashierPage.ErrorRetryButton().isElementPresent()) {
                                cashierPage.ErrorRetryButton().click();
                                cashierPage.payBy(Constants.PayMode.CC, ccDto);
                        }

                        String merchantTransId = orderId;
                        String txnAmtForSms = rupeeAmount;
                        String displayDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMM, yyyy"));

                        Response smsRes = new DealsShareInvoiceViaSms.Builder()
                                        .baseUri(dealsBase)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId
                                                        + "?orderStatus=SUCCESS")
                                        .pgmid(pgMid)
                                        .orderId(orderId)
                                        .phoneNo(CASHIER_PHONE)
                                        .bankName(ISSUING_BANK_HDFC + " Bank")
                                        .transactionAmount(txnAmtForSms)
                                        .transactionDate(displayDate)
                                        .paymentMethod("CREDIT_CARD")
                                        .merchantName(MERCHANT_DISPLAY_NAME)
                                        .build()
                                        .execute();
                        Assertions.assertThat(smsRes.getStatusCode()).as("shareInvoiceViaSms").isEqualTo(200);
                        JsonPath smsJp = smsRes.jsonPath();
                        String smsStatus = smsJp.getString("status");
                        Assertions.assertThat(smsStatus).isEqualTo("SUCCESS");

                        Response invRes = new DealsEmiGetInvoice.Builder()
                                        .baseUri(dealsBase)
                                        .pgmid(pgMid)
                                        .merchantTransId(merchantTransId)
                                        .referer(dealsBase + "/pgmid/" + pgMid + "/order/" + orderId + "?linkId="
                                                        + orderId)
                                        .build()
                                        .execute();
                        Assertions.assertThat(invRes.getStatusCode()).as("getInvoice").isEqualTo(200);
                        Assertions.assertThat(invRes.getBody().asString()).as("getInvoice body").isNotBlank();

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
                                        .as("payment/transaction/history response contains orderId %s", orderId)
                                        .isTrue();
                } finally {
                        RestAssured.replaceFiltersWith(Collections.emptyList());
                }
        }
}
