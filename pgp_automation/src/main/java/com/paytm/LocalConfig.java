package com.paytm;


import com.paytm.framework.utils.PropertyUtil;
import org.luaj.vm2.ast.Str;


public class LocalConfig {

    public static final String WALLET_HOST;
    public static final String QR_HOST;
    public static final String PGP_HOST;
    public static final String PG2_HOST;
    public static final String STS_HOST;
    public static final String AUTH_HOST;
    public static final String PGP_DB_CONNECTION_URL;
    public static final String PG_DB_CONNECTION_URL;
    public static final String AUTH_DB_CONNECTION_URL;
    public static final String WALLET_DB_CONNECTION_URL;
    public static final String PANEL_DB_CONNECTION_URL;
    public static final String PGP_RESP_HOST;
    public static final Boolean PERFORM_RESPONSE_PAGE_UI_VALIDATION;
    public static final String AEROSPIKE_HOST;
    public static final int AEROSPIKE_PORT;
    public static final String AUTCOF25834699562781_JET_KEY;
    public static final String MOCK_HOST;
    public static final String PPBL_URL;
    public static final String PAYTMCC_URL;
    public static final String THEIA_URL;
    public static final String THEIA_HINDI_URL;
    public static final String THEIA_SECONDARY_URL;
    public static final String PG_REDIS_URI;        // sentinal is completely short circuited from the system
    public static final String SESSION_REDIS_URI;
    public static final String ZERO_COST_EMI;
    public static final String JWT_KEY;
    public static final String ALIPAY;
    public static final String SCRIPT_EXEC_SERVER;
    public static final String PG2_BASE_URL;
    public static final String ELASTIC_HOST = "10.142.53.177:80";
    public static final String ELASTIC_INDEX = "*auto*";
    public static final String PROFILE;
    public static final String JWT_EMI_KEY;
    public static final String JWT_KEY_LOGO;
    public static final String JWT_LOGO_CLIENTID;
    public static final String MERCHANT_LOGO_MID = "SsEML469811569388287";
    public static final String DEV_PAYTM_HOST;
    public static final String BOSS_PANEL;
    public static final String PG_JWT_KEY;
    public static final String SAVED_CARD_PG_JWT_KEY;
    public static final String SUPERGW_LITE;
    public static final String CHECKOUTJS_URL;
    public static final String CHECKOUTJS_EMI_URL;
    public static final String PG_REDIS_CLUSTER_URI;
    public static final String SESSION_REDIS_CLUSTER_URI;
    public static final String STATIC_REDIS_CLUSTER_URI;
    public static final String TRANSACTIONAL_REDIS_CLUSTER_URI;
    public static final String PG_REDIS_CLUSTER_PASS;
    public static final String  FETCH_CARD_INDEX_JWT_KEY;
    public static final String  JENKINS_SERVER_URI;
    public static final String  STAGING_COMMUNICATION_GATEWAY_IP;
    public static String JSON_POST_URL = "/checkoutpage/nplus_page.jsp?ttype=hold&jsonresp=";
    public static final Boolean INITIATE_LOGCHECK;
    public static final Boolean PERFORM_LOGCHECK;
    public static final Boolean PERFORM_SAVEDCARD_RESET;
    public static final Boolean PERFORM_FF4j_FLAGSETUP;
    public static final String  UTILITIES_HOST;
    public static final String CHECKOUTJS_MERCHANT_ELEMENT_URL;
    public static final String COFT_CENTER;
    public static final String COFT_ENCRYPTION_KEY;
    public static final String JWT_CLIENT_SECRET_COFT_THEIA;
    public static final String JWT_CLIENT_SECRET_DEALS_THEIA;
    public static final String JWT_CLIENT_SECRET_PAYTM_EMI_APP_THEIA;
    public static final String CHECKOUTJS_LOAD_URL;
    public static final String LIGHTENING_CHECKOUT_URL;
    public static final String CARD_CENTER;
    public static final String STS_DB;
    public static final String ENV_NAME;
    public static final String THEIA_FACADE_LOGS;
    public static final String THEIA_LOGS;
    public static final String MERCHANT_STATUS_LOGS;
    public static final String MERCHANT_FACADE_LOGS;
    public static final String INSTAPROXY_LOGS;
    public static final String PGPROXY_LOGS;
    public static final String VALID_VAULT_JWT_KEY;
    public static final String UPI_PSP_PAYMENT_STATUS_JWT_KEY;
    public static final String INVALID_VAULT_JWT_KEY;
    public static final String CLIENTID_VAULT_JWT_KEY;
    public static final String UPI_ORDERWITH_ENV_NAME;
    public static final String MAPPING_LOGS;
    public static final String BANK_MANDATE_RESP_URL;
    public static final String ES_HOST;
    public static final String BOSS_HOST;
    public static final String MONGO_DB_URI;
    public static final String MONGO_DATABSE_NAME;
    public static final String SHA_256_KEY;
    public static final String SUPERGW_JWT_KEY;
    public static final String INSTA_CREATE_ORDER_KEY;
    public static final String LITE_MERCHANT_DETAILS_JWT_KEY;
    public static final String CARD_SERVICE_THEIA_CLIENT_ID;
    public static final String CARD_SERVICE_THEIA_CLIENT_SECRET;
    public static final String KAFKA_SERVER;
    public static final String MOCK_KAFKA_DB;
    public static final String MOCK_DB;
    public static final String PLE_DEALS_BASE_URL;
    public static final String PGMC_HOST;

    static {
        try {
            PropertyUtil.getInstance().load("localconfig.properties");
            WALLET_HOST = getProperty("WALLET_HOST");
            QR_HOST= getProperty("QR_HOST");
            AUTH_HOST = getProperty("AUTH_HOST");
            PGP_HOST = getProperty("PGP_HOST");
            PG2_HOST = getProperty("PG2_HOST");
            STS_HOST = getProperty("STS_HOST");
            PGP_DB_CONNECTION_URL = getProperty("PGP_DB_CONNECTION_URL");
            PG_DB_CONNECTION_URL = getProperty("PG_DB_CONNECTION_URL");
            AUTH_DB_CONNECTION_URL = getProperty("AUTH_DB_CONNECTION_URL");
            WALLET_DB_CONNECTION_URL = getProperty("WALLET_DB_CONNECTION_URL");
            PANEL_DB_CONNECTION_URL = getProperty("PANEL_DB_CONNECTION_URL");
            PGP_RESP_HOST = getProperty("PGP_RESP_HOST");
            PERFORM_RESPONSE_PAGE_UI_VALIDATION = PropertyUtil.getInstance().
                    getValue("PERFORM_RESPONSE_PAGE_UI_VALIDATION", "0").
                    equalsIgnoreCase("1");
            AEROSPIKE_HOST = getProperty("AEROSPIKE_HOST");
            AEROSPIKE_PORT = Integer.parseInt(getProperty("AEROSPIKE_PORT"));
            AUTCOF25834699562781_JET_KEY = getProperty("AUTCOF25834699562781_JET_KEY");
            MOCK_HOST = getProperty("MOCK_HOST");
            PPBL_URL = getProperty("PPBL_URL");
            PAYTMCC_URL = getProperty("PAYTMCC_URL");
            THEIA_URL = getProperty("THEIA_URL");
            THEIA_HINDI_URL = getProperty("THEIA_HINDI_URL");
            THEIA_SECONDARY_URL = getProperty("THEIA_SECONDARY_URL");
            PG_REDIS_URI = getProperty("PG_REDIS_URI");
            SESSION_REDIS_URI = getProperty("SESSION_REDIS_URI");
            TRANSACTIONAL_REDIS_CLUSTER_URI = getProperty("TRANSACTIONAL_REDIS_CLUSTER_URI");
            ZERO_COST_EMI = getProperty("ZERO_COST_EMI");
            ALIPAY = getProperty("ALIPAY");
            JWT_KEY = getProperty("JWT_KEY");
            JWT_EMI_KEY = getProperty("JWT_EMI_KEY");
            SCRIPT_EXEC_SERVER = getProperty("SCRIPT_EXEC_SERVER");
            PG2_BASE_URL= getProperty("PG2_BASE_URL");
            PROFILE = getProperty("PROFILE");
            JWT_KEY_LOGO = getProperty("JWT_KEY_LOGO");
            JWT_LOGO_CLIENTID = getProperty("JWT_LOGO_CLIENTID");
            DEV_PAYTM_HOST = getProperty("DEV_PAYTM_HOST");
            BOSS_PANEL = getProperty("BOSS_PANEL");
            SUPERGW_LITE = getProperty("SUPERGW_LITE");
            PG_JWT_KEY = getProperty("PG_JWT_KEY");
            SAVED_CARD_PG_JWT_KEY=getProperty("SAVED_CARD_PG_JWT_KEY");
            CHECKOUTJS_URL = getProperty("CHECKOUTJS_URL");
            CHECKOUTJS_EMI_URL= getProperty("CHECKOUTJS_EMI_URL");
            PG_REDIS_CLUSTER_URI = getProperty("PG_REDIS_CLUSTER_URI");
            SESSION_REDIS_CLUSTER_URI = getProperty("SESSION_REDIS_CLUSTER_URI");
            PG_REDIS_CLUSTER_PASS = getProperty("PG_REDIS_CLUSTER_PASS");
            FETCH_CARD_INDEX_JWT_KEY = getProperty("FETCH_CARD_INDEX_JWT_KEY");
            STATIC_REDIS_CLUSTER_URI = getProperty("STATIC_REDIS_CLUSTER_URI");
            JENKINS_SERVER_URI = getProperty("JENKINS_SERVER_URI");
            INITIATE_LOGCHECK = Boolean.valueOf(getProperty("INITIATE_LOGCHECK"));
            PERFORM_LOGCHECK = Boolean.valueOf(getProperty("PERFORM_LOGCHECK"));
            PERFORM_SAVEDCARD_RESET = Boolean.valueOf(getProperty("PERFORM_SAVEDCARD_RESET"));
            PERFORM_FF4j_FLAGSETUP = Boolean.valueOf(getProperty("PERFORM_FF4j_FLAGSETUP"));
            STAGING_COMMUNICATION_GATEWAY_IP = getProperty("STAGING_COMMUNICATION_GATEWAY_IP");
            UTILITIES_HOST = getProperty("UTILITIES_HOST");
            CHECKOUTJS_MERCHANT_ELEMENT_URL = getProperty("CHECKOUTJS_MERCHANT_ELEMENT_URL");
            COFT_CENTER = getProperty("COFT_CENTER");
            COFT_ENCRYPTION_KEY = getProperty("COFT_ENCRYPTION_KEY");
            JWT_CLIENT_SECRET_COFT_THEIA = getProperty("JWT_CLIENT_SECRET_COFT_THEIA");
            JWT_CLIENT_SECRET_DEALS_THEIA = getProperty("JWT_CLIENT_SECRET_DEALS_THEIA");
            JWT_CLIENT_SECRET_PAYTM_EMI_APP_THEIA = getProperty("JWT_CLIENT_SECRET_PAYTM_EMI_APP_THEIA");
            CHECKOUTJS_LOAD_URL=getProperty("CHECKOUTJS_LOAD_URL");
            LIGHTENING_CHECKOUT_URL=getProperty("LIGHTENING_CHECKOUT_URL");
            CARD_CENTER=getProperty("CARD_CENTER");
            STS_DB = getProperty("STS_DB");
            ENV_NAME=getProperty("ENV_NAME");
            THEIA_FACADE_LOGS=getProperty("THEIA_FACADE_LOGS");
            THEIA_LOGS=getProperty("THEIA_LOGS");
            MERCHANT_STATUS_LOGS=getProperty("MERCHANT_STATUS_LOGS");
            MERCHANT_FACADE_LOGS=getProperty("MERCHANT_FACADE_LOGS");
            INSTAPROXY_LOGS=getProperty("INSTAPROXY_LOGS");
            PGPROXY_LOGS=getProperty("PGPROXY_LOGS");
            VALID_VAULT_JWT_KEY =getProperty("VALID_VAULT_JWT_KEY");
            UPI_PSP_PAYMENT_STATUS_JWT_KEY=getProperty("UPI_PSP_PAYMENT_STATUS_JWT_KEY");
            INVALID_VAULT_JWT_KEY=getProperty("INVALID_VAULT_JWT_KEY");
            CLIENTID_VAULT_JWT_KEY=getProperty("CLIENTID_VAULT_JWT_KEY");
            UPI_ORDERWITH_ENV_NAME=getProperty("UPI_ORDERWITH_ENV_NAME");
            BANK_MANDATE_RESP_URL=getProperty("BANK_MANDATE_RESP_URL");
            MAPPING_LOGS=getProperty("MAPPING_LOGS");
            ES_HOST=getProperty("ES_HOST");
            BOSS_HOST=getProperty("BOSS_HOST");
            MONGO_DB_URI=getProperty("MONGO_DB_URI");
            MONGO_DATABSE_NAME=getProperty("MONGO_DATABSE_NAME");
            SHA_256_KEY=getProperty("SHA_256_KEY");
            SUPERGW_JWT_KEY=getProperty("SUPERGW_JWT_KEY");
            INSTA_CREATE_ORDER_KEY=getProperty("INSTA_CREATE_ORDER_KEY");
            LITE_MERCHANT_DETAILS_JWT_KEY=getProperty("LITE_MERCHANT_DETAILS_JWT_KEY");
            CARD_SERVICE_THEIA_CLIENT_ID=getProperty("CARD_SERVICE_THEIA_CLIENT_ID");
            CARD_SERVICE_THEIA_CLIENT_SECRET=getProperty("CARD_SERVICE_THEIA_CLIENT_SECRET");
            KAFKA_SERVER=getProperty("KAFKA_SERVER");
            MOCK_KAFKA_DB=getProperty("MOCK_KAFKA_DB");
            MOCK_DB=getProperty("MOCK_DB");
            PLE_DEALS_BASE_URL = getProperty("PLE_DEALS_BASE_URL");
            PGMC_HOST=getProperty("PGMC_HOST");

        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException("Something wrong !!! Check configurations.", e);
        }
    }

    private static String getProperty(String vairableName) {
        String temp = System.getProperty(vairableName);
        if (null == temp || temp.isEmpty()) {
            try {
                temp = PropertyUtil.getInstance().getValue(vairableName);
            } catch (Throwable e) {
                System.out.println(vairableName + " PROPERTY NOT LOADED" + e);
                throw new RuntimeException(vairableName + " PROPERTY NOT LOADED", e);
            }
        }
        System.out.println("PROPERTY LOADED " + vairableName + ": " + temp);
        return temp;
    }
}