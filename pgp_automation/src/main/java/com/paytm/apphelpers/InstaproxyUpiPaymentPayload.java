package com.paytm.apphelpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.paytm.dto.instaproxy.upipayment.UpiPaymentMerchantInfo;
import com.paytm.dto.instaproxy.upipayment.UpiPaymentRequestDTO;
import com.paytm.framework.utils.PropertyUtil;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

// Data processing: PG2 UPI payment request DTO builder + dynamic ids/timestamps
public final class InstaproxyUpiPaymentPayload {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter IST_OFFSET_TS =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
    private static final DateTimeFormatter MERCHANT_TRANS_ID_PREFIX =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private InstaproxyUpiPaymentPayload() {
    }

    /**
     * IST wall-clock time like {@code 2026-04-10T14:25:36+05:30} for reqTime / createdTime / payTime.
     */
    public static String currentIstTimestamp() {
        return ZonedDateTime.now(IST).format(IST_OFFSET_TS);
    }

    /**
     * Unique 19-digit external serial number.
     */
    public static String generateExtSerialNo19() {
        long ms = System.currentTimeMillis();
        int r = ThreadLocalRandom.current().nextInt(0, 1_000_000);
        return String.format("%013d%06d", ms % 10_000_000_000_000L, r);
    }

    /**
     * Unique merchant transaction id (17 digits: {@code yyyyMMddHHmmss} + 3 random digits).
     */
    public static String generateMerchantTransId() {
        String prefix = ZonedDateTime.now(IST).format(MERCHANT_TRANS_ID_PREFIX);
        String suffix = String.format("%03d", ThreadLocalRandom.current().nextInt(0, 1000));
        return prefix + suffix;
    }

    /**
     * Request DTO with fresh {@code extSerialNo}, {@code merchantTransId}, and aligned IST {@code reqTime} / {@code createdTime} / {@code payTime}.
     */
    public static UpiPaymentRequestDTO buildDefaultRequestDto() {
        String ist = currentIstTimestamp();
        UpiPaymentMerchantInfo merchantInfo = new UpiPaymentMerchantInfo.Builder()
                .setMerchantTransId(generateMerchantTransId())
                .build();
        return new UpiPaymentRequestDTO.Builder()
                .setReqTime(ist)
                .setCreatedTime(ist)
                .setPayTime(ist)
                .setExtSerialNo(generateExtSerialNo19())
                .setMerchantInfo(merchantInfo)
                .build();
    }

    public static String buildRequestBodyJson() {
        try {
            return buildDefaultRequestDto().toJson();
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize PG2 UPI payment request DTO", e);
        }
    }

    /**
     * Optional {@code INSTAPROXY_PG2_UPI_COOKIE} from profile {@code localconfig.properties} (set locally; do not commit secrets).
     */
    public static String cookieFromConfig() {
        try {
            String v = PropertyUtil.getInstance().getValue("INSTAPROXY_PG2_UPI_COOKIE");
            return v == null ? "" : v.trim();
        } catch (Exception e) {
            return "";
        }
    }
}
