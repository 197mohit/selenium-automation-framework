package com.paytm.api.PLE;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Pulls subvention + bank-offer ids from the Deals {@code POST /api/emi/all} JSON for HDFC credit-card EMI or HDFC
 * EMI ({@code hdfc_emi}), matching the shape used by {@link DealsUserInitiateTransaction} (e.g.
 * {@code emiChannelDetail.offerDetails}, {@code bankOfferDetails}, {@code addOffer}). Supports the newer
 * {@code allEmiOptions[]} layout ({@code bank.id} {@code HDFC_EMI}, {@code plans[]} with {@code type: EMI} and
 * per-plan {@code addOffer}). Walks the tree so minor response-shape differences still work.
 */
public final class EmiAllOfferExtractor {

    private static final String KEY_HDFC_CREDIT_CARD = "hdfc_credit_card";
    private static final String KEY_HDFC_EMI = "hdfc_emi";
    private static final String BANK_ID_HDFC_EMI = "HDFC_EMI";

    private EmiAllOfferExtractor() {}

    /** Result of parsing {@code emi/all} for initiate-txn payloads. */
    public static final class Result {
        private final String subventionOfferId;
        private final String[] bankOfferIds;

        public Result(String subventionOfferId, String[] bankOfferIds) {
            this.subventionOfferId = subventionOfferId;
            this.bankOfferIds = bankOfferIds != null ? bankOfferIds.clone() : new String[0];
        }

        public String getSubventionOfferId() {
            return subventionOfferId;
        }

        public String[] getBankOfferIds() {
            return bankOfferIds.clone();
        }
    }

    /** HDFC EMI path: subvention/bank ids plus {@code addOffer} for {@code items[].addOffer} on initiate. */
    public static final class HdfcEmiResult {
        private final String subventionOfferId;
        private final String[] bankOfferIds;
        private final JSONObject addOffer;
        /** From {@code plans[].emiOfferDetails.offerId} when present (e.g. {@code allEmiOptions} / HDFC_EMI shape). */
        private final String emiPlanOfferId;

        public HdfcEmiResult(String subventionOfferId, String[] bankOfferIds, JSONObject addOffer) {
            this(subventionOfferId, bankOfferIds, addOffer, null);
        }

        public HdfcEmiResult(
                String subventionOfferId, String[] bankOfferIds, JSONObject addOffer, String emiPlanOfferId) {
            this.subventionOfferId = subventionOfferId;
            this.bankOfferIds = bankOfferIds != null ? bankOfferIds.clone() : new String[0];
            this.addOffer = addOffer;
            this.emiPlanOfferId = emiPlanOfferId;
        }

        public String getSubventionOfferId() {
            return subventionOfferId;
        }

        public String[] getBankOfferIds() {
            return bankOfferIds.clone();
        }

        /**
         * EMI tenure plan offer id from {@code emi/all} when the response uses {@code allEmiOptions} (may be null for
         * legacy {@code emiChannelDetail} payloads).
         */
        public String getEmiPlanOfferId() {
            return emiPlanOfferId;
        }

        /**
         * May be null if absent in {@code emi/all}, or when {@link #extractForHdfcEmi} used the HDFC credit-card
         * fallback and the channel had no {@code addOffer}.
         */
        public JSONObject getAddOffer() {
            return addOffer;
        }

        /**
         * Offer ids for {@link DealsUserInitiateTransaction} {@code simplifiedUnifiedOffersObj} →
         * {@code items[].offerDetails.bankOfferDetails}: every {@code offerId} from {@code addOffer.items} in response
         * order (same as store-attendant / PG_LINK when {@code emi/all} returns e.g. HDFC_EMI {@code plans[].addOffer}).
         * When {@code addOffer.items} is empty or missing, returns {@link #getBankOfferIds()}.
         */
        public String[] getInitiateTransactionBankOfferIds() {
            if (addOffer != null) {
                String[] ordered = bankOfferDetailsIdsInAddOfferItemOrder(addOffer);
                if (ordered.length > 0) {
                    return ordered;
                }
            }
            return getBankOfferIds();
        }
    }

    /**
     * For {@code items[].offerDetails.bankOfferDetails} on initiate: every non-empty {@code items[i].offerId} under
     * emi/all {@code addOffer}, in array order (includes the brand/primary row, e.g. same id as {@code addOffer.offerId}).
     */
    public static String[] bankOfferDetailsIdsInAddOfferItemOrder(JSONObject addOffer) {
        if (addOffer == null) {
            return new String[0];
        }
        JSONArray items = addOffer.optJSONArray("items");
        if (items == null) {
            return new String[0];
        }
        List<String> out = new ArrayList<>();
        for (int i = 0; i < items.length(); i++) {
            JSONObject it = items.optJSONObject(i);
            if (it == null) {
                continue;
            }
            String id = it.optString("offerId", "");
            if (id.isEmpty()) {
                continue;
            }
            out.add(id);
        }
        return out.toArray(new String[0]);
    }

    /**
     * Resolves offer ids for HDFC + credit-card EMI and the given tenure (months as string, e.g. {@code "3"}).
     *
     * @param emiAllResponseBody raw JSON body from {@link DealsEmiAll}
     */
    public static Result extractForHdfcCreditCardEmi(String emiAllResponseBody, String tenureMonths)
            throws JSONException {
        Objects.requireNonNull(emiAllResponseBody, "emiAllResponseBody");
        Objects.requireNonNull(tenureMonths, "tenureMonths");
        JSONObject root = new JSONObject(emiAllResponseBody);

        JSONObject block = findObjectByKey(root, KEY_HDFC_CREDIT_CARD);
        if (block == null) {
            block = findFirstBankCardBlock(root, "HDFC", "CREDIT_CARD");
        }
        if (block == null) {
            throw new IllegalStateException(
                    "emi/all: no block for hdfc_credit_card and no HDFC + CREDIT_CARD node found");
        }

        JSONObject emiChannel = findEmiChannelDetailForTenure(block, tenureMonths);
        if (emiChannel == null) {
            emiChannel = findFirstEmiChannelDetail(block);
        }
        if (emiChannel == null) {
            throw new IllegalStateException(
                    "emi/all: no emiChannelDetail for tenure " + tenureMonths + " (HDFC CREDIT_CARD block)");
        }

        String subvention = extractSubventionOfferId(emiChannel);
        List<String> bankOffers = extractBankOfferIds(emiChannel);

        if (subvention == null || subvention.isEmpty()) {
            throw new IllegalStateException("emi/all: subvention offerId not found under emiChannelDetail");
        }
        if (bankOffers.isEmpty()) {
            throw new IllegalStateException("emi/all: bankOfferDetails offerIds not found under emiChannelDetail");
        }
        return new Result(subvention, bankOffers.toArray(new String[0]));
    }

    /**
     * Resolves offer ids and {@code addOffer} for HDFC EMI ({@code hdfc_emi} or HDFC + EMI). If those are absent,
     * falls back to HDFC credit-card EMI ({@code hdfc_credit_card} or HDFC + CREDIT_CARD) so environments that only
     * expose the CC node still work; {@link HdfcEmiResult#getAddOffer()} may then be null if the channel has no
     * {@code addOffer}.
     *
     * @param emiAllResponseBody raw JSON body from {@link DealsEmiAll}
     */
    public static HdfcEmiResult extractForHdfcEmi(String emiAllResponseBody, String tenureMonths) throws JSONException {
        Objects.requireNonNull(emiAllResponseBody, "emiAllResponseBody");
        Objects.requireNonNull(tenureMonths, "tenureMonths");
        JSONObject root = new JSONObject(emiAllResponseBody);

        HdfcEmiResult fromAllEmi = tryExtractHdfcEmiFromAllEmiOptions(root, tenureMonths);
        if (fromAllEmi != null) {
            return fromAllEmi;
        }

        JSONObject block = findObjectByKey(root, KEY_HDFC_EMI);
        if (block == null) {
            block = findFirstBankCardBlock(root, "HDFC", "EMI");
        }
        boolean creditCardFallback = false;
        if (block == null) {
            block = findObjectByKey(root, KEY_HDFC_CREDIT_CARD);
            if (block != null) {
                creditCardFallback = true;
            }
        }
        if (block == null) {
            block = findFirstBankCardBlock(root, "HDFC", "CREDIT_CARD");
            if (block != null) {
                creditCardFallback = true;
            }
        }
        if (block == null) {
            throw new IllegalStateException(
                    "emi/all: no hdfc_emi / HDFC+EMI and no hdfc_credit_card / HDFC+CREDIT_CARD block found");
        }

        JSONObject emiChannel = findEmiChannelDetailForTenure(block, tenureMonths);
        if (emiChannel == null) {
            emiChannel = findFirstEmiChannelDetail(block);
        }
        if (emiChannel == null) {
            throw new IllegalStateException(
                    "emi/all: no emiChannelDetail for tenure "
                            + tenureMonths
                            + " (HDFC EMI or CREDIT_CARD fallback block)");
        }

        JSONObject addOffer = extractAddOffer(emiChannel);
        String subvention = extractSubventionOfferId(emiChannel);
        List<String> bankOffers = extractBankOfferIds(emiChannel);

        if (addOffer != null) {
            if (subvention == null || subvention.isEmpty()) {
                String fromAdd = addOffer.optString("offerId", "");
                if (!fromAdd.isEmpty()) {
                    subvention = fromAdd;
                }
            }
            if (bankOffers.isEmpty()) {
                bankOffers = extractOfferIdsFromAddOfferItems(addOffer, subvention);
            }
        }

        if (subvention == null || subvention.isEmpty()) {
            throw new IllegalStateException("emi/all: subvention offerId not found under emiChannelDetail");
        }
        if (bankOffers.isEmpty()) {
            throw new IllegalStateException("emi/all: bankOfferDetails offerIds not found under emiChannelDetail");
        }
        if (addOffer == null && !creditCardFallback) {
            throw new IllegalStateException(
                    "emi/all: addOffer not found under emiChannelDetail for HDFC EMI (non-CREDIT_CARD fallback path)");
        }
        return new HdfcEmiResult(subvention, bankOffers.toArray(new String[0]), addOffer);
    }

    /**
     * Newer {@code emi/all} shape: root (or nested) {@code allEmiOptions[]} with {@code bank.id} {@code HDFC_EMI},
     * {@code plans[]} entries {@code type: EMI}, per-plan {@code emiOfferDetails.tenure} and {@code addOffer}
     * (additional offers).
     */
    private static HdfcEmiResult tryExtractHdfcEmiFromAllEmiOptions(JSONObject root, String tenureMonths) {
        JSONArray allEmi = findAllEmiOptionsArray(root);
        if (allEmi == null) {
            return null;
        }
        JSONObject bankBlock = null;
        for (int i = 0; i < allEmi.length(); i++) {
            JSONObject option = allEmi.optJSONObject(i);
            if (option == null) {
                continue;
            }
            JSONObject bank = option.optJSONObject("bank");
            if (bank == null) {
                continue;
            }
            if (!BANK_ID_HDFC_EMI.equals(bank.optString("id", ""))) {
                continue;
            }
            bankBlock = option;
            break;
        }
        if (bankBlock == null) {
            return null;
        }
        JSONArray plans = bankBlock.optJSONArray("plans");
        if (plans == null) {
            return null;
        }
        JSONObject plan = null;
        for (int i = 0; i < plans.length(); i++) {
            JSONObject p = plans.optJSONObject(i);
            if (p == null) {
                continue;
            }
            if (!"EMI".equalsIgnoreCase(p.optString("type", ""))) {
                continue;
            }
            JSONObject emiDet = p.optJSONObject("emiOfferDetails");
            if (!tenureMatchesAllEmiPlan(emiDet, tenureMonths)) {
                continue;
            }
            plan = p;
            break;
        }
        if (plan == null) {
            return null;
        }
        JSONObject addOffer = extractAddOffer(plan);
        if (addOffer == null) {
            return null;
        }
        String subvention = addOffer.optString("offerId", "");
        if (subvention.isEmpty()) {
            return null;
        }
        List<String> bankOffers = extractOfferIdsFromAddOfferItems(addOffer, subvention);
        if (bankOffers.isEmpty()) {
            return null;
        }
        String emiPlanOfferId = null;
        JSONObject emiDet = plan.optJSONObject("emiOfferDetails");
        if (emiDet != null) {
            String oid = emiDet.optString("offerId", "");
            if (!oid.isEmpty() && !"Not Available".equalsIgnoreCase(oid)) {
                emiPlanOfferId = oid;
            }
        }
        return new HdfcEmiResult(subvention, bankOffers.toArray(new String[0]), addOffer, emiPlanOfferId);
    }

    private static JSONArray findAllEmiOptionsArray(JSONObject root) {
        JSONArray direct = root.optJSONArray("allEmiOptions");
        if (direct != null) {
            return direct;
        }
        String[] names = JSONObject.getNames(root);
        if (names == null) {
            return null;
        }
        for (String k : names) {
            Object v = root.opt(k);
            if (v instanceof JSONObject) {
                JSONArray found = findAllEmiOptionsArray((JSONObject) v);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean tenureMatchesAllEmiPlan(JSONObject emiOfferDetails, String tenureMonths) {
        if (emiOfferDetails == null) {
            return false;
        }
        int want;
        try {
            want = Integer.parseInt(tenureMonths, 10);
        } catch (NumberFormatException e) {
            return false;
        }
        if (emiOfferDetails.has("tenure")) {
            int got = emiOfferDetails.optInt("tenure", Integer.MIN_VALUE);
            if (got != Integer.MIN_VALUE) {
                return got == want;
            }
            String s = emiOfferDetails.optString("tenure", "");
            try {
                return want == Integer.parseInt(s, 10);
            } catch (NumberFormatException e) {
                return tenureMonths.equals(s);
            }
        }
        return false;
    }

    /**
     * Collects {@code items[].offerId} for {@code bankOfferDetails}; skips {@code excludeOfferId} (e.g. subvention /
     * brand row) when present.
     */
    private static List<String> extractOfferIdsFromAddOfferItems(JSONObject addOffer, String excludeOfferId) {
        Set<String> ordered = new LinkedHashSet<>();
        JSONArray items = addOffer.optJSONArray("items");
        if (items == null) {
            return new ArrayList<>();
        }
        for (int i = 0; i < items.length(); i++) {
            JSONObject it = items.optJSONObject(i);
            if (it == null) {
                continue;
            }
            String id = it.optString("offerId", "");
            if (id.isEmpty()) {
                continue;
            }
            if (excludeOfferId != null
                    && !excludeOfferId.isEmpty()
                    && excludeOfferId.equals(id)) {
                continue;
            }
            ordered.add(id);
        }
        return new ArrayList<>(ordered);
    }

    private static JSONObject extractAddOffer(JSONObject emiChannel) {
        JSONObject ao = emiChannel.optJSONObject("addOffer");
        if (ao == null) {
            return null;
        }
        try {
            return new JSONObject(ao.toString());
        } catch (JSONException e) {
            return null;
        }
    }

    private static JSONObject findObjectByKey(JSONObject root, String key) {
        if (root.has(key) && root.get(key) instanceof JSONObject) {
            return root.getJSONObject(key);
        }
        for (String k : JSONObject.getNames(root)) {
            Object v = root.get(k);
            if (v instanceof JSONObject) {
                JSONObject found = findObjectByKey((JSONObject) v, key);
                if (found != null) {
                    return found;
                }
            } else if (v instanceof JSONArray) {
                JSONArray a = (JSONArray) v;
                for (int i = 0; i < a.length(); i++) {
                    if (a.get(i) instanceof JSONObject) {
                        JSONObject found = findObjectByKey(a.getJSONObject(i), key);
                        if (found != null) {
                            return found;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static JSONObject findFirstBankCardBlock(JSONObject root, String bankCode, String cardType) {
        return findFirstBankCardBlockAny(root, bankCode, cardType);
    }

    private static JSONObject findFirstBankCardBlockAny(Object node, String bankCode, String cardType) {
        if (node instanceof JSONObject) {
            JSONObject o = (JSONObject) node;
            if (matchesBankAndCardType(o, bankCode, cardType)) {
                return o;
            }
            for (String k : JSONObject.getNames(o)) {
                JSONObject found = findFirstBankCardBlockAny(o.get(k), bankCode, cardType);
                if (found != null) {
                    return found;
                }
            }
        } else if (node instanceof JSONArray) {
            JSONArray a = (JSONArray) node;
            for (int i = 0; i < a.length(); i++) {
                JSONObject found = findFirstBankCardBlockAny(a.get(i), bankCode, cardType);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean matchesBankAndCardType(JSONObject o, String bankCode, String cardType) {
        String bank = o.optString("bankCode", o.optString("bank", ""));
        String ct = o.optString("cardType", o.optString("emiType", ""));
        return bankCode.equalsIgnoreCase(bank) && cardType.equalsIgnoreCase(ct);
    }

    private static JSONObject findEmiChannelDetailForTenure(Object node, String tenureMonths) {
        if (node instanceof JSONObject) {
            JSONObject o = (JSONObject) node;
            if (o.has("emiChannelDetail")) {
                JSONObject emi = o.getJSONObject("emiChannelDetail");
                if (tenureMatches(emi, tenureMonths)) {
                    return emi;
                }
            }
            for (String k : JSONObject.getNames(o)) {
                JSONObject found = findEmiChannelDetailForTenure(o.get(k), tenureMonths);
                if (found != null) {
                    return found;
                }
            }
        } else if (node instanceof JSONArray) {
            JSONArray a = (JSONArray) node;
            for (int i = 0; i < a.length(); i++) {
                JSONObject found = findEmiChannelDetailForTenure(a.get(i), tenureMonths);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static boolean tenureMatches(JSONObject emiChannelDetail, String tenureMonths) {
        String m = emiChannelDetail.optString("emiMonths", "");
        if (m.isEmpty()) {
            m = emiChannelDetail.optString("tenure", "");
        }
        if (tenureMonths.equals(m)) {
            return true;
        }
        String planId = emiChannelDetail.optString("planId", "");
        return planId.toUpperCase(Locale.ROOT).contains("|" + tenureMonths)
                || planId.endsWith("|" + tenureMonths);
    }

    private static JSONObject findFirstEmiChannelDetail(Object node) {
        if (node instanceof JSONObject) {
            JSONObject o = (JSONObject) node;
            if (o.has("emiChannelDetail")) {
                return o.getJSONObject("emiChannelDetail");
            }
            for (String k : JSONObject.getNames(o)) {
                JSONObject found = findFirstEmiChannelDetail(o.get(k));
                if (found != null) {
                    return found;
                }
            }
        } else if (node instanceof JSONArray) {
            JSONArray a = (JSONArray) node;
            for (int i = 0; i < a.length(); i++) {
                JSONObject found = findFirstEmiChannelDetail(a.get(i));
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static String extractSubventionOfferId(JSONObject emiChannel) {
        JSONObject sub = emiChannel.optJSONObject("subventionDetails");
        if (sub != null) {
            String id = sub.optString("offerId", "");
            if (!id.isEmpty()) {
                return id;
            }
        }
        JSONArray offerDetails = emiChannel.optJSONArray("offerDetails");
        if (offerDetails != null) {
            for (int i = 0; i < offerDetails.length(); i++) {
                JSONObject od = offerDetails.optJSONObject(i);
                if (od == null) {
                    continue;
                }
                String id = od.optString("offerId", "");
                if (!id.isEmpty()) {
                    return id;
                }
            }
        }
        return null;
    }

    private static List<String> extractBankOfferIds(JSONObject emiChannel) {
        Set<String> ordered = new LinkedHashSet<>();
        JSONArray bankOfferDetails = emiChannel.optJSONArray("bankOfferDetails");
        if (bankOfferDetails != null) {
            for (int i = 0; i < bankOfferDetails.length(); i++) {
                JSONObject bo = bankOfferDetails.optJSONObject(i);
                if (bo == null) {
                    continue;
                }
                String id = bo.optString("offerId", "");
                if (!id.isEmpty()) {
                    ordered.add(id);
                }
            }
        }
        return new ArrayList<>(ordered);
    }
}
