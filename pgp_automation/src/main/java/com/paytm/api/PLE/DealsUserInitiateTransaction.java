package com.paytm.api.PLE;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * POST {@code /api/user/initiateTransaction} — brand EMI / PG_LINK checkout init (store attendant WebView).
 * Base URL defaults to {@link LocalConfig#PLE_DEALS_BASE_URL}. Use {@link Builder#bodyJson(String)} for a full raw
 * payload, or structured setters to build the same shape as the Deals app.
 *
 * <p>Configure offers ({@link Builder#subventionOfferId(String)}, {@link Builder#subventionDetailsNull(boolean)},
 * {@link Builder#bankOfferIds(String...)}, {@link Builder#itemAddOffer(org.json.JSONObject)} for HDFC EMI {@code addOffer}
 * on the line item), root {@link Builder#emiType(String)} / {@link Builder#emiTypeBank(String)}, promo flags,
 * payment rail ({@link Builder#paymentMode(String)}, {@link Builder#banks(String...)},
 * {@link Builder#tenures(String...)}, {@link Builder#channels(String...)}) or pass multiple
 * {@link PaymentModeSpec} via {@link Builder#paymentModes(PaymentModeSpec...)} for several {@code enablePaymentMode}
 * blocks in one request.
 *
 * <p>For minimal amount-based bank-offer initiate (no {@code simplifiedUnifiedOffers.items}, no catalog root fields),
 * use {@link Builder#minimalAmountBasedPgLink(boolean)} {@code true}. For JSON {@code subventionDetails: null} (e.g. CC
 * pay-in-full PG_LINK), use {@link Builder#minimalPgLinkNullSubvention(boolean)} {@code true}.
 */
public class DealsUserInitiateTransaction extends BaseApi {

    /** Path segment under Deals base URL — matches outgoing URI for curl / logging filters. */
    public static final String API_RELATIVE_PATH = "/api/user/initiateTransaction";

    /**
     * Callback URL the Deals app uses after payment, e.g.
     * {@code https://dealsstaging.paytm.com/api/user/payment/receipt} when {@code pleDealsBaseUrl} is the staging host.
     */
    public static String paymentReceiptCallbackUrl(String pleDealsBaseUrl) {
        String b =
                (pleDealsBaseUrl != null && !pleDealsBaseUrl.isEmpty())
                        ? pleDealsBaseUrl
                        : LocalConfig.PLE_DEALS_BASE_URL;
        if (b.endsWith("/")) {
            b = b.substring(0, b.length() - 1);
        }
        return b + "/api/user/payment/receipt";
    }

    private static final String PATH = API_RELATIVE_PATH;
    private static final String DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; SM-M135FU Build/UP1A.231005.007; wv) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Version/4.0 Chrome/146.0.7680.177 Mobile Safari/537.36 "
                    + "AppContainer/10.5.10 AppContainer PhoenixContainer/2.0.9-10.49.0-CA-227711-IB1";

    private DealsUserInitiateTransaction(Builder b) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("content-type", "application/json");
        getRequestSpecBuilder().addHeader("accept", "application/json, text/plain, */*");
        getRequestSpecBuilder().addHeader("accept-language", "en-IN,en-US;q=0.9,en;q=0.8");
        getRequestSpecBuilder().addHeader("priority", "u=1, i");
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());

        String base = b.baseUri != null ? b.baseUri : LocalConfig.PLE_DEALS_BASE_URL;
        getRequestSpecBuilder().setBaseUri(base);
        getRequestSpecBuilder().setBasePath(PATH);
        getRequestSpecBuilder().addHeader("origin", base);

        getRequestSpecBuilder().addQueryParam("globalTS", String.valueOf(b.globalTs));
        getRequestSpecBuilder().addQueryParam("buildVersion", b.buildVersion);
        getRequestSpecBuilder().addQueryParam("requestId", b.requestId);

        String ua = b.userAgent != null && !b.userAgent.isEmpty() ? b.userAgent : DEFAULT_USER_AGENT;
        getRequestSpecBuilder().addHeader("user-agent", ua);
        getRequestSpecBuilder().addHeader("x-requested-with", "com.paytm.store_attendant");
        getRequestSpecBuilder().addHeader("sec-ch-ua-mobile", "?1");
        getRequestSpecBuilder().addHeader("sec-ch-ua-platform", "\"Android\"");
        getRequestSpecBuilder().addHeader(
                "sec-ch-ua", "\"Chromium\";v=\"146\", \"Not-A.Brand\";v=\"24\", \"Android WebView\";v=\"146\"");
        getRequestSpecBuilder().addHeader("sec-fetch-site", "same-origin");
        getRequestSpecBuilder().addHeader("sec-fetch-mode", "cors");
        getRequestSpecBuilder().addHeader("sec-fetch-dest", "empty");
        if (b.cookie != null && !b.cookie.isEmpty()) {
            getRequestSpecBuilder().addHeader("Cookie", b.cookie);
        }
        if (b.referer != null && !b.referer.isEmpty()) {
            getRequestSpecBuilder().addHeader("referer", b.referer);
        }

        String bodyJson = b.bodyJson != null && !b.bodyJson.isEmpty() ? b.bodyJson : b.buildBodyJson(base);
        getRequestSpecBuilder().setBody(bodyJson);
    }

    /**
     * One element of the root {@code enablePaymentMode} array: mode, optional channels, issuing banks, tenures (EMI).
     */
    public static final class PaymentModeSpec {

        private final String mode;
        private final String[] channels;
        private final String[] banks;
        private final String[] tenures;

        /**
         * @param mode e.g. {@code EMI}, {@code NET_BANKING}, {@code UPI}, etc. (API-defined)
         * @param channels channel ids or empty
         * @param banks bank codes, e.g. {@code HDFC}, {@code ICICI}
         * @param tenures EMI months as strings, e.g. {@code "3"}, {@code "6"}; may be empty for non-EMI
         */
        public PaymentModeSpec(String mode, String[] channels, String[] banks, String[] tenures) {
            this.mode = Objects.requireNonNull(mode, "mode");
            this.channels = channels != null ? channels.clone() : new String[0];
            this.banks = banks != null ? banks.clone() : new String[0];
            this.tenures = tenures != null ? tenures.clone() : new String[0];
        }

        /** Single-bank EMI shortcut. */
        public static PaymentModeSpec emi(String bank, String... tenures) {
            return new PaymentModeSpec("EMI", new String[0], new String[] {bank}, tenures);
        }

        JSONObject toJson() {
            JSONObject o = new JSONObject();
            o.put("mode", mode);
            JSONArray ch = new JSONArray();
            for (String c : channels) {
                ch.put(c);
            }
            o.put("channels", ch);
            JSONArray b = new JSONArray();
            for (String x : banks) {
                b.put(x);
            }
            o.put("banks", b);
            JSONArray t = new JSONArray();
            for (String x : tenures) {
                t.put(x);
            }
            o.put("tenures", t);
            return o;
        }
    }

    public static final class Builder {
        private String baseUri;
        private String bodyJson;
        private String cookie;
        private String referer;
        private String userAgent;
        private long globalTs = System.currentTimeMillis();
        private String buildVersion = "S26.04.17";
        private String requestId;

        private String mid;
        private String productId;
        private String brandId;
        private int categoryId;
        private String model;
        private String skuCode;
        /** Item line price, e.g. {@code "10000.00"}. */
        private String itemPrice;
        /** Root {@code txnAmount.value}, e.g. {@code "10000.00"}. */
        private String txnAmountValue;
        private String txnCurrency = "INR";
        private String websiteName = "retail";
        private String callbackUrl;
        private String productName;
        private boolean isBrandEmi = true;
        private String brandName;
        private String categoryName;
        private String modelName;
        private String brandInvoiceNumber;
        /** Legacy single bank; ignored if {@link #banks} is set. */
        private String issuingBank = "HDFC";
        /** Legacy single tenure; ignored if {@link #tenuresList} is set. */
        private String tenure = "3";
        /** Issuing banks for the default single {@code enablePaymentMode} block; overrides {@link #issuingBank}. */
        private String[] banks;
        /** EMI tenures for the default block; overrides {@link #tenure}. */
        private String[] tenuresList;
        /** Optional channels for the default payment block (often empty). */
        private String[] paymentChannels;
        /** Payment mode for the default block when {@link #paymentModes} is not used; e.g. {@code EMI}. */
        private String paymentMode = "EMI";
        /** When non-empty, used as the full {@code enablePaymentMode} array (overrides default block). */
        private PaymentModeSpec[] paymentModes;
        private String discoverability = "offline";
        private String simplifiedOffersSource = "PG_LINK";
        private String subventionOfferId;
        private String subventionAmount;
        private boolean applyAvailablePromo = true;
        private boolean amountBasedBankOffer;
        private boolean isAmountBasedBankOffer;
        private boolean amountBasedSubvention;
        private boolean isAmountBasedSubvention;
        private String deviceImei;
        private String userMobile;
        /** If unset, defaults to {@code "@" + userMobile} when {@link #userMobile} is set. */
        private String custId;
        private String[] bankOfferIds;
        /** Optional {@code items[].addOffer} in {@code simplifiedUnifiedOffers} (e.g. HDFC EMI breakdown). */
        private JSONObject itemAddOffer;

        /**
         * Minimal PG_LINK initiate (see {@link #buildMinimalAmountBasedBody}): no catalog {@code items} in offers JSON;
         * amount-based promo / subvention flags only unless {@link #minimalPgLinkNullSubvention}(true).
         */
        private boolean minimalAmountBasedPgLink;

        /**
         * When {@link #minimalAmountBasedPgLink(boolean)} is {@code true}: emit {@code subventionDetails: null}
         * (e.g. CC pay-in-full bank-offer) instead of a subvention object built from {@link #subventionAmount}.
         */
        private boolean minimalPgLinkNullSubvention;

        /**
         * When {@code true}, full catalog {@code simplifiedUnifiedOffers.subventionDetails} is JSON {@code null}
         * (e.g. pay-in-full CC). Structured validation skips {@code subventionOfferId} / {@code subventionAmount}.
         */
        private boolean subventionDetailsNull;

        /**
         * When {@code false}, {@code deviceIMEI} is not placed under {@code simplifiedUnifiedOffers} (some PG_LINK
         * payloads only carry IMEI on the root and line item).
         */
        private boolean includeUnifiedOffersDeviceImei = true;

        /** Root {@code emiType} when non-null (e.g. {@code payInFull}). */
        private String emiType;

        /** Root {@code emiTypeBank} when non-null (e.g. issuing bank code). */
        private String emiTypeBank;

        public Builder baseUri(String baseUri) {
            this.baseUri = baseUri;
            return this;
        }

        /** Full JSON body; when set, structured fields are ignored. */
        public Builder bodyJson(String bodyJson) {
            this.bodyJson = bodyJson;
            return this;
        }

        public Builder cookie(String cookie) {
            this.cookie = cookie;
            return this;
        }

        public Builder referer(String referer) {
            this.referer = referer;
            return this;
        }

        public Builder userAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public Builder globalTs(long globalTs) {
            this.globalTs = globalTs;
            return this;
        }

        public Builder buildVersion(String buildVersion) {
            this.buildVersion = buildVersion;
            return this;
        }

        public Builder requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder mid(String mid) {
            this.mid = mid;
            return this;
        }

        public Builder productId(String productId) {
            this.productId = productId;
            return this;
        }

        public Builder brandId(String brandId) {
            this.brandId = brandId;
            return this;
        }

        public Builder categoryId(int categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder skuCode(String skuCode) {
            this.skuCode = skuCode;
            return this;
        }

        public Builder itemPrice(String itemPrice) {
            this.itemPrice = itemPrice;
            return this;
        }

        public Builder txnAmountValue(String txnAmountValue) {
            this.txnAmountValue = txnAmountValue;
            return this;
        }

        public Builder txnCurrency(String txnCurrency) {
            this.txnCurrency = txnCurrency;
            return this;
        }

        public Builder websiteName(String websiteName) {
            this.websiteName = websiteName;
            return this;
        }

        /** Defaults to {@code {base}/api/user/payment/receipt}. */
        public Builder callbackUrl(String callbackUrl) {
            this.callbackUrl = callbackUrl;
            return this;
        }

        public Builder productName(String productName) {
            this.productName = productName;
            return this;
        }

        public Builder isBrandEmi(boolean isBrandEmi) {
            this.isBrandEmi = isBrandEmi;
            return this;
        }

        public Builder brandName(String brandName) {
            this.brandName = brandName;
            return this;
        }

        public Builder categoryName(String categoryName) {
            this.categoryName = categoryName;
            return this;
        }

        public Builder modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public Builder brandInvoiceNumber(String brandInvoiceNumber) {
            this.brandInvoiceNumber = brandInvoiceNumber;
            return this;
        }

        /** Single issuing bank; use {@link #banks(String...)} for multiple banks. */
        public Builder issuingBank(String issuingBank) {
            this.issuingBank = issuingBank;
            return this;
        }

        /**
         * Issuing bank codes for the default payment block. Use {@code banks("HDFC", "ICICI")} for several banks.
         * Call {@code banks()} with no args for an explicit empty {@code banks} array (e.g. non-EMI modes); until then,
         * {@link #issuingBank} default applies when this was never set.
         */
        public Builder banks(String... banks) {
            if (banks == null || banks.length == 0) {
                this.banks = new String[0];
            } else {
                this.banks = banks.clone();
            }
            return this;
        }

        /** Single EMI tenure; use {@link #tenures(String...)} for multiple tenures. */
        public Builder tenure(String tenure) {
            this.tenure = tenure;
            return this;
        }

        /**
         * EMI tenures for the default block, e.g. {@code tenures("3")} or {@code tenures("3", "6")}.
         * Call {@code tenures()} with no args for an explicit empty {@code tenures} array; until then, {@link #tenure}
         * applies when this was never set.
         */
        public Builder tenures(String... tenures) {
            if (tenures == null || tenures.length == 0) {
                this.tenuresList = new String[0];
            } else {
                this.tenuresList = tenures.clone();
            }
            return this;
        }

        /** Channels for the default {@code enablePaymentMode} entry (often none). */
        public Builder channels(String... paymentChannels) {
            this.paymentChannels = paymentChannels != null && paymentChannels.length > 0 ? paymentChannels.clone() : null;
            return this;
        }

        /**
         * Root {@code enablePaymentMode[].mode} for the default block when not using {@link #paymentModes(PaymentModeSpec...)}.
         * Examples: {@code EMI}, {@code NET_BANKING}, {@code UPI} (must match Deals API contract).
         */
        public Builder paymentMode(String paymentMode) {
            this.paymentMode = paymentMode;
            return this;
        }

        /**
         * Full control over {@code enablePaymentMode}: multiple modes, banks, and tenures in one request.
         * When set (non-empty), {@link #paymentMode}, {@link #banks}, {@link #tenures}, {@link #channels}, and legacy
         * {@link #issuingBank}/{@link #tenure} are ignored for that array.
         */
        public Builder paymentModes(PaymentModeSpec... paymentModes) {
            this.paymentModes = paymentModes != null && paymentModes.length > 0 ? paymentModes.clone() : null;
            return this;
        }

        public Builder applyAvailablePromo(boolean applyAvailablePromo) {
            this.applyAvailablePromo = applyAvailablePromo;
            return this;
        }

        public Builder amountBasedBankOffer(boolean amountBasedBankOffer) {
            this.amountBasedBankOffer = amountBasedBankOffer;
            return this;
        }

        public Builder isAmountBasedBankOffer(boolean isAmountBasedBankOffer) {
            this.isAmountBasedBankOffer = isAmountBasedBankOffer;
            return this;
        }

        public Builder amountBasedSubvention(boolean amountBasedSubvention) {
            this.amountBasedSubvention = amountBasedSubvention;
            return this;
        }

        public Builder isAmountBasedSubvention(boolean isAmountBasedSubvention) {
            this.isAmountBasedSubvention = isAmountBasedSubvention;
            return this;
        }

        public Builder discoverability(String discoverability) {
            this.discoverability = discoverability;
            return this;
        }

        public Builder simplifiedOffersSource(String simplifiedOffersSource) {
            this.simplifiedOffersSource = simplifiedOffersSource;
            return this;
        }

        public Builder subventionOfferId(String subventionOfferId) {
            this.subventionOfferId = subventionOfferId;
            return this;
        }

        public Builder subventionAmount(String subventionAmount) {
            this.subventionAmount = subventionAmount;
            return this;
        }

        public Builder deviceImei(String deviceImei) {
            this.deviceImei = deviceImei;
            return this;
        }

        public Builder userMobile(String userMobile) {
            this.userMobile = userMobile;
            return this;
        }

        public Builder custId(String custId) {
            this.custId = custId;
            return this;
        }

        public Builder bankOfferIds(String... bankOfferIds) {
            this.bankOfferIds = bankOfferIds;
            return this;
        }

        /** Sets {@code addOffer} on the single catalog {@code items[0]} line (cloned). */
        public Builder itemAddOffer(JSONObject addOffer) {
            if (addOffer == null) {
                this.itemAddOffer = null;
            } else {
                this.itemAddOffer = new JSONObject(addOffer.toString());
            }
            return this;
        }

        /**
         * Use minimal amount-based PG_LINK body (amount-based bank offer / promo flags). Requires {@link #mid},
         * {@link #txnAmountValue}; {@link #subventionAmount} unless {@link #minimalPgLinkNullSubvention}(true) (JSON
         * {@code subventionDetails: null}). Set {@link #amountBasedBankOffer}(true) and
         * {@link #isAmountBasedBankOffer}(true) to match store-attendant contract.
         */
        public Builder minimalAmountBasedPgLink(boolean minimalAmountBasedPgLink) {
            this.minimalAmountBasedPgLink = minimalAmountBasedPgLink;
            return this;
        }

        public Builder minimalPgLinkNullSubvention(boolean minimalPgLinkNullSubvention) {
            this.minimalPgLinkNullSubvention = minimalPgLinkNullSubvention;
            return this;
        }

        public Builder subventionDetailsNull(boolean subventionDetailsNull) {
            this.subventionDetailsNull = subventionDetailsNull;
            return this;
        }

        public Builder includeUnifiedOffersDeviceImei(boolean includeUnifiedOffersDeviceImei) {
            this.includeUnifiedOffersDeviceImei = includeUnifiedOffersDeviceImei;
            return this;
        }

        public Builder emiType(String emiType) {
            this.emiType = emiType;
            return this;
        }

        public Builder emiTypeBank(String emiTypeBank) {
            this.emiTypeBank = emiTypeBank;
            return this;
        }

        private String buildBodyJson(String baseForCallback) {
            if (minimalAmountBasedPgLink) {
                return buildMinimalAmountBasedBody(baseForCallback);
            }
            JSONObject promoDetails = new JSONObject();
            promoDetails.put("applyAvailablePromo", applyAvailablePromo);
            promoDetails.put("amountBasedBankOffer", amountBasedBankOffer);
            promoDetails.put("isAmountBasedBankOffer", isAmountBasedBankOffer);

            JSONObject subventionDetails = new JSONObject();
            subventionDetails.put("offerId", subventionOfferId);
            subventionDetails.put("subventionAmount", subventionAmount);
            subventionDetails.put("AmountBasedSubvention", amountBasedSubvention);
            subventionDetails.put("isAmountBasedSubvention", isAmountBasedSubvention);

            JSONArray bankOfferDetails = new JSONArray();
            for (String oid : bankOfferIds) {
                bankOfferDetails.put(new JSONObject().put("offerId", oid));
            }

            JSONObject offerDetails = new JSONObject();
            offerDetails.put("bankOfferDetails", bankOfferDetails);

            String sku = skuCode != null ? skuCode : model;
            String mName = modelName != null ? modelName : model;

            JSONObject item = new JSONObject();
            item.put("id", productId);
            item.put("productId", productId);
            item.put("brandId", brandId);
            item.put("categoryId", categoryId);
            item.put("price", itemPrice);
            item.put("model", model);
            item.put("offerDetails", offerDetails);
            item.put("discoverability", discoverability);
            item.put("brandName", brandName);
            item.put("categoryName", categoryName);
            item.put("modelName", mName);
            item.put("imei", deviceImei);
            item.put("brandInvoiceNumber", brandInvoiceNumber);
            if (itemAddOffer != null) {
                item.put("addOffer", itemAddOffer);
            }

            JSONObject simplifiedUnifiedOffers = new JSONObject();
            simplifiedUnifiedOffers.put("source", simplifiedOffersSource);
            simplifiedUnifiedOffers.put("promoDetails", promoDetails);
            if (subventionDetailsNull) {
                simplifiedUnifiedOffers.put("subventionDetails", JSONObject.NULL);
            } else {
                simplifiedUnifiedOffers.put("subventionDetails", subventionDetails);
            }
            if (includeUnifiedOffersDeviceImei && deviceImei != null && !deviceImei.isEmpty()) {
                simplifiedUnifiedOffers.put("deviceIMEI", deviceImei);
            }
            simplifiedUnifiedOffers.put("items", new JSONArray().put(item));

            JSONObject simplifiedUnifiedOffersObj = new JSONObject();
            simplifiedUnifiedOffersObj.put("simplifiedUnifiedOffers", simplifiedUnifiedOffers);

            JSONObject txnAmount = new JSONObject();
            txnAmount.put("value", txnAmountValue);
            txnAmount.put("currency", txnCurrency);

            JSONArray enablePaymentMode = buildEnablePaymentModeArray();

            String cb = callbackUrl != null ? callbackUrl : baseForCallback + "/api/user/payment/receipt";

            String resolvedCustId = custId;
            if (resolvedCustId == null && userMobile != null) {
                resolvedCustId = "@" + userMobile;
            }

            JSONObject userInfo = new JSONObject();
            userInfo.put("custId", resolvedCustId);
            userInfo.put("mobile", userMobile);

            JSONObject root = new JSONObject();
            root.put("simplifiedUnifiedOffersObj", simplifiedUnifiedOffersObj);
            root.put("mid", mid);
            root.put("txnAmount", txnAmount);
            root.put("websiteName", websiteName);
            root.put("callbackUrl", cb);
            root.put("productName", productName);
            root.put("skuCode", sku);
            root.put("isBrandEmi", isBrandEmi);
            root.put("brandName", brandName);
            root.put("categoryName", categoryName);
            root.put("modelName", mName);
            root.put("imei", deviceImei);
            root.put("brandInvoiceNumber", brandInvoiceNumber);
            root.put("enablePaymentMode", enablePaymentMode);
            root.put("userInfo", userInfo);
            if (emiType != null && !emiType.isEmpty()) {
                root.put("emiType", emiType);
            }
            if (emiTypeBank != null && !emiTypeBank.isEmpty()) {
                root.put("emiTypeBank", emiTypeBank);
            }

            return root.toString();
        }

        private String buildMinimalAmountBasedBody(String baseForCallback) {
            JSONObject promoDetails = new JSONObject();
            promoDetails.put("applyAvailablePromo", applyAvailablePromo);
            promoDetails.put("amountBasedBankOffer", amountBasedBankOffer);
            promoDetails.put("isAmountBasedBankOffer", isAmountBasedBankOffer);

            JSONObject simplifiedUnifiedOffers = new JSONObject();
            simplifiedUnifiedOffers.put("source", simplifiedOffersSource);
            simplifiedUnifiedOffers.put("promoDetails", promoDetails);
            if (minimalPgLinkNullSubvention) {
                simplifiedUnifiedOffers.put("subventionDetails", JSONObject.NULL);
            } else {
                JSONObject subventionDetails = new JSONObject();
                subventionDetails.put("subventionAmount", subventionAmount);
                subventionDetails.put("amountBasedBankOffer", amountBasedBankOffer);
                subventionDetails.put("isAmountBasedBankOffer", isAmountBasedBankOffer);
                simplifiedUnifiedOffers.put("subventionDetails", subventionDetails);
            }

            JSONObject simplifiedUnifiedOffersObj = new JSONObject();
            simplifiedUnifiedOffersObj.put("simplifiedUnifiedOffers", simplifiedUnifiedOffers);

            JSONObject txnAmount = new JSONObject();
            txnAmount.put("value", txnAmountValue);
            txnAmount.put("currency", txnCurrency);

            JSONArray enablePaymentMode = buildEnablePaymentModeArray();

            String cb = callbackUrl != null ? callbackUrl : baseForCallback + "/api/user/payment/receipt";

            String resolvedCustId = custId;
            if (resolvedCustId == null && userMobile != null) {
                resolvedCustId = "@" + userMobile;
            }
            if (resolvedCustId == null) {
                resolvedCustId = "@";
            }

            JSONObject userInfo = new JSONObject();
            userInfo.put("custId", resolvedCustId);
            userInfo.put("mobile", userMobile != null ? userMobile : "");

            JSONObject root = new JSONObject();
            root.put("simplifiedUnifiedOffersObj", simplifiedUnifiedOffersObj);
            root.put("mid", mid);
            root.put("websiteName", websiteName);
            root.put("callbackUrl", cb);
            root.put("enablePaymentMode", enablePaymentMode);
            root.put("userInfo", userInfo);
            root.put("txnAmount", txnAmount);
            root.put("isBrandEmi", isBrandEmi);
            if (emiType != null && !emiType.isEmpty()) {
                root.put("emiType", emiType);
            }
            if (emiTypeBank != null && !emiTypeBank.isEmpty()) {
                root.put("emiTypeBank", emiTypeBank);
            }

            return root.toString();
        }

        private JSONArray buildEnablePaymentModeArray() {
            if (paymentModes != null && paymentModes.length > 0) {
                JSONArray arr = new JSONArray();
                for (PaymentModeSpec pm : paymentModes) {
                    arr.put(pm.toJson());
                }
                return arr;
            }
            String mode = paymentMode != null && !paymentMode.isEmpty() ? paymentMode : "EMI";
            String[] bankArr = resolveBanks();
            String[] tenureArr = resolveTenures();
            String[] ch = paymentChannels != null ? paymentChannels : new String[0];
            JSONObject one = new JSONObject();
            one.put("mode", mode);
            JSONArray channelsJson = new JSONArray();
            for (String c : ch) {
                channelsJson.put(c);
            }
            one.put("channels", channelsJson);
            JSONArray banksJson = new JSONArray();
            for (String b : bankArr) {
                banksJson.put(b);
            }
            one.put("banks", banksJson);
            JSONArray tenuresJson = new JSONArray();
            for (String t : tenureArr) {
                tenuresJson.put(t);
            }
            one.put("tenures", tenuresJson);
            return new JSONArray().put(one);
        }

        private String[] resolveBanks() {
            if (banks != null) {
                return banks;
            }
            if (issuingBank != null && !issuingBank.isEmpty()) {
                return new String[] {issuingBank};
            }
            return new String[0];
        }

        private String[] resolveTenures() {
            if (tenuresList != null) {
                return tenuresList;
            }
            if (tenure != null && !tenure.isEmpty()) {
                return new String[] {tenure};
            }
            return new String[0];
        }

        public DealsUserInitiateTransaction build() {
            if (requestId == null || requestId.isEmpty()) {
                requestId = "PAYTM_" + Long.toHexString(System.nanoTime()).toUpperCase();
            }
            boolean hasRaw = bodyJson != null && !bodyJson.isEmpty();
            if (!hasRaw) {
                if (minimalAmountBasedPgLink) {
                    if (mid == null || mid.isEmpty() || txnAmountValue == null || txnAmountValue.isEmpty()) {
                        throw new IllegalStateException(
                                "minimalAmountBasedPgLink requires mid, txnAmountValue "
                                        + "(or use bodyJson)");
                    }
                    if (!minimalPgLinkNullSubvention
                            && (subventionAmount == null || subventionAmount.isEmpty())) {
                        throw new IllegalStateException(
                                "minimalAmountBasedPgLink requires subventionAmount unless minimalPgLinkNullSubvention "
                                        + "(or use bodyJson)");
                    }
                    return new DealsUserInitiateTransaction(this);
                }
                boolean needSubventionFields = !subventionDetailsNull;
                if (mid == null
                        || productId == null
                        || brandId == null
                        || model == null
                        || itemPrice == null
                        || txnAmountValue == null
                        || productName == null
                        || brandName == null
                        || categoryName == null
                        || (needSubventionFields
                                && (subventionOfferId == null || subventionAmount == null))
                        || deviceImei == null
                        || brandInvoiceNumber == null
                        || userMobile == null
                        || bankOfferIds == null
                        || bankOfferIds.length == 0) {
                    throw new IllegalStateException(
                            "Either bodyJson or all structured fields are required: mid, productId, brandId, model, "
                                    + "itemPrice, txnAmountValue, productName, brandName, categoryName, "
                                    + (needSubventionFields
                                            ? "subventionOfferId, subventionAmount, "
                                            : "")
                                    + "deviceImei, brandInvoiceNumber, userMobile, bankOfferIds");
                }
            }
            return new DealsUserInitiateTransaction(this);
        }
    }
}
