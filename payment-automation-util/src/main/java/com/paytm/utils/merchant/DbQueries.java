package com.paytm.utils.merchant;

public class DbQueries {

    public static String MID_FROM_REQUESTID(String sourceId, String requestId) {
        return "select MID from ENTITY_INFO where REQUEST_ID = '" + sourceId + requestId + "'";
    }

    public static String ENTITY_ID_FROM_ENTITY_INFO(String mid) {
        return "select ID from ENTITY_INFO where MID = '" + mid + "'";
    }

    public static String SELECT_ID_FROM_LOOK_UP_PAYMENT_MODE(String paymodeType) {
        return "select * from LOOKUP_DATA where NAME = '" + paymodeType + "'";
    }

    public static String SELECT_FROM_LOOKUP_DATA_BY_VALUE(String value) {
        return "SELECT * FROM LOOKUP_DATA WHERE VALUE='" + value + "'";
    }

    public static String SELECT_FROM_LOOKUP_DATA_BY_CATAGORY_NAME(UtilConstants.LookUpData_Category category, String name, UtilConstants.LookUpData_Status status) {
        return "SELECT * FROM LOOKUP_DATA WHERE CATEGORY='" + category +
                "' AND NAME='" + name + "' AND STATUS='" + status.toString() + "'";
    }

    /**
     * @param ENTITY_ID
     * @param WEBSITE_NAME
     * @param URL_TYPE_ID
     * @return query with above parameters anf status type enabled
     */
    public static String SELECT_ENTIRY_URL_INFO(String ENTITY_ID, String WEBSITE_NAME, String URL_TYPE_ID) {
        return "SELECT * FROM ENTITY_URL_INFO WHERE ENTITY_ID = '" + ENTITY_ID + "' " +
                "AND WEBSITE_NAME = '" + WEBSITE_NAME + "' " +
                "AND URL_TYPE_ID = '" + URL_TYPE_ID + "' " +
                "AND STATUS = '9376503'";
    }

    public static String SELECT_FROM_MERCHANT_OFFER_DETAILS(String mid) {
        return "SELECT * FROM ENTITY_OFFER_DETAILS WHERE MID='" + mid + "'";
    }

    public static String SELECT_FROM_BANK_MASTER_DETAILS(String bankCode) {
        return "SELECT * FROM BANK_MASTER WHERE BANK_CODE='" + bankCode + "'";
    }

    public static String SELECT_FROM_FORMATTER_DETAILS(String BANK_CODE, String PAY_METHOD) {
        return "SELECT * FROM FORMATTER_DETAILS WHERE BANK_CODE='" + BANK_CODE + "' AND PAY_METHOD='" + PAY_METHOD + "'";
    }

    public static String SELECT_FROM_ALIPAY_PAYTM_MERCHANT(String paytm_merchant_id) {
        return "SELECT * FROM alipay_paytm_merchant WHERE paytm_merchant_id='" + paytm_merchant_id + "'";
    }

    public static  String GET_EMI_DETAILS(String mid,String bankCode,String columnName){
        return "SELECT " +columnName+
                " FROM MBID_LIMIT_MAPPING INNER JOIN BANK_MASTER ON MBID_LIMIT_MAPPING.BANK_ID = BANK_MASTER.BANK_ID WHERE\n"+
                "\tMBID_LIMIT_MAPPING.ENTITY_ID IN (SELECT ID FROM ENTITY_INFO WHERE MID='"+mid+
                "') AND MBID_LIMIT_MAPPING.STATUS=9376503 AND BANK_MASTER.BANK_CODE='"+bankCode+"'";
    }

}
