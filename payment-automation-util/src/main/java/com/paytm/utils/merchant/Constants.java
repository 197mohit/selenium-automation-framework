package com.paytm.utils.merchant;

import com.paytm.framework.utils.PropertyUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by deepakkumar on 23/11/17.
 */
public class Constants {

    public static final String PGP_HOST;
    public static final String MOCK_HOST;
    public static final String ADMIN_SERVER_ADDRESS;
    public static final String VELOCITY_SERVER_ADDRESS;
    public static final String PG_DB_CONNECTION;
    public static final String EXPLICIT_MIGRATION_EVENT;
    public static final String AEROSPIKE_HOST;
    public static final String AEROSPIKE_PORT;
    public static final String PG_REDIS_URI;
    public static final String NOTIFICATION_CONNECTION_URL;
    public static final String PGP_DB_CONNECTION_URL;
    public static final String PAYTMPG_DB_CONNECTION_URL;
    public static final String WALLET_HOST;
    public static final String ALIPAY;
    public static final String AUTH_HOST;
    public static final String WALLET_DB_CONNECTION_URL;
    public static final String AUTH_DB_CONNECTION_URL;
    public static final String SUPERGW_LITE;
    public static final String PPBL_URL;
    public static final Set<String> OAUTH_TOKENS = new HashSet<>();

    static {
        try {
            PropertyUtil propertyUtil = PropertyUtil.getInstance();
            propertyUtil.load("localconfig.properties");
            ADMIN_SERVER_ADDRESS = propertyUtil.getValue("ADMIN_PANEL_URI");
            PG_DB_CONNECTION = propertyUtil.getValue("PG_DB_CONNECTION_URL");
            PG_REDIS_URI = propertyUtil.getValue("PG_REDIS_URI");
            EXPLICIT_MIGRATION_EVENT = propertyUtil.getValue("EXPLICIT_MIGRATION_EVENT");
            VELOCITY_SERVER_ADDRESS = propertyUtil.getValue("VELOCITY_SERVER_ADDRESS");
            AEROSPIKE_HOST = PropertyUtil.getInstance().getValue("AEROSPIKE_HOST");
            AEROSPIKE_PORT = PropertyUtil.getInstance().getValue("AEROSPIKE_PORT");
            NOTIFICATION_CONNECTION_URL = PropertyUtil.getInstance().getValue("NOTIFICATION_CONNECTION_URL");
            PGP_HOST = PropertyUtil.getInstance().getValue("PGP_HOST");
            MOCK_HOST = PropertyUtil.getInstance().getValue("MOCK_HOST");
            PGP_DB_CONNECTION_URL = propertyUtil.getValue("PGP_DB_CONNECTION_URL");
            WALLET_HOST = propertyUtil.getValue("WALLET_HOST");
            PAYTMPG_DB_CONNECTION_URL = PG_DB_CONNECTION;
            ALIPAY = propertyUtil.getValue("ALIPAY");
            AUTH_HOST = PropertyUtil.getInstance().getValue("AUTH_HOST");
            WALLET_DB_CONNECTION_URL = PropertyUtil.getInstance().getValue("WALLET_DB_CONNECTION_URL");
            AUTH_DB_CONNECTION_URL = PropertyUtil.getInstance().getValue("AUTH_DB_CONNECTION_URL");
            SUPERGW_LITE = PropertyUtil.getInstance().getValue("SUPERGW_LITE");
            PPBL_URL = PropertyUtil.getInstance().getValue("PPBL_URL");
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static class MappingService {
        public static final String MERCHANT_EXTENDED_INFO = "/mapping-service/merchant/get/extended/info/{mid}";
        public static final String GET_CONTRACT_PAYMENT_INFO = "/mapping-service/common/v1/get/contract/paymentInfo/{mid}/BALANCE/{contractID}";
        public static final String LOOK_UP_PAYMENT_MODE = "/mapping-service/get/lookup/PAYMENT_MODE/{paymodeType}";
        public static final String GET_MERCH_PREFERENCE_INFO = "/mapping-service/merchant/get/preference/info/{mid}";
        public static final String GET_ENTITY_URL_INFO = "/mapping-service/get/entityurlinformid/{mid}/{reqType}/{websiteName}";
        public static final String GET_PAYTM_PROPERTIES = "/mapping-service/get/paytmproperties/{propName}";
        public static final String GET_ENTITY_OFFER_DETAIL = "/mapping-service/get/entityofferdetailsformid/{mid}/{channel}/{websiteName}";
        public static final String GET_FORMATTER = "/mapping-service/get/formatter/{bankCode}/{payMethod}";
        public static final String GET_BANK_URL_INFO = "/mapping-service/get/bankurlinfo/{bankId}/{payMethodId}/{channelId}";
        public static final String GET_PAYTM_ID = "/mapping-service/get/paytmid/{alipayId}";
        public static final String GET_BANK_DETAILS_APCODE = "/mapping-service/get/bankdetails/alipaycode/{alipayCode}";
    }

    public static class Refund {
        public static final String REFUND = "/refund/HANDLER_INTERNAL/REFUND";
        public static final String REFUND_STATUS = "/refund/HANDLER_INTERNAL/REFUND_STATUS";
        public static final String ASYNC_REFUND = "/refund/api/v1/async/refund";
        public static final String SECURE_REFUND_STATUS = "/refund/HANDLER_INTERNAL/getRefundStatus";
    }


}
