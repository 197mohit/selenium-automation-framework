package com.paytm.api.linkAPI;

public class linkConstant {
    public static class PGPAPIResourcePath {
        public static final String CREATE_LINK_SUCCESS = "Payment link is created successfully";
        public static final String FETCH_LINK_SUCCESS = "Payment link is processed successfully";
        public static final String RESULTMSG_SUCCESS = "SUCCESS";
        public static final String RESULTMSG_FAILED = "FAILED";
        public static final String MID_ERROR = "Merchant id  is a required parameter";
        public static final String LINKID_ERROR = "No link found.";
        public static final String TO_DATE_ERROR = "searchStartDate should be before searchEndDate";
        public static final String CREATE_LINK_FAILEDMSG = "FAILED";
        public static final String CREATE_LINK_SUCCESSMSG = "SUCCESS";
        public static final String AMOUNT_FIXED = "Payment amount is mandatory for Fixed Link";
        public static final String AMOUNT_INVOICE = "Amount cannot be empty for invoice link";
        public static final String INVOICEID_BLANK = "Empty Invoice Id for Invoice type Payment Link ";
        public static final String INVALID_EXPIRY_DATE = "Expiry date is invalid.";
        public static final String INVALID_EXPIRY_DATE_5YEAR = "Expiry date cannot be set as more than 5 years from the current date";
        public static final String INVALID_CUSTOMER_PHONE = "Phone no is invalid in customer contact";
        public static final String INVALID_CUSTOMER_EMAIL = "Email id is invalid in customer contact";
        public static final String INVOICE_PHONE = "Invoice phone number is invalid";
        public static final String INVOICE_EMAIL = "Invoice email id is invalid";
        public static final String LINK_DESCRIPTION_ERROR = "Link Description should not contain any special characters";
        public static final String LINK_NAME_ERROR = "link name contains special character.";
        public static final String PAYMENT_BUTTON_ERROR01 = "Payment buttons is not supported for this merchant";
        public static final String PAYMENT_BUTTON_ERROR02 = "Sending notification to user for Payment Buttons is not supported.";

        public static final String LINKNAME_BLANK = "link name cannot be blank";
        public static final String LINK_DESCRIPTION_BLANK = "Link Description should be from 3 to 30 characters.";
        public static final String AMOUNT_EMI_VALIDATION = "Subvention amount can't be greater than link's amount.";
        public static final String ARCHIVE_LINK_SUCCESS = "Payment link is archived successfully";
        public static final String ARCHIVE_LINK_SUCCESS_CODE = "200";
        public static final String ARCHIVE_LINK_NULL_LINKID_MSG = "linkId cannot be empty";
        public static final String ARCHIVE_LINK_NULL_LINKID_CODE = "302";
        public static final String ARCHIVE_LINK_NULL_MIDMSG = "Merchant id  is a required parameter";
        public static final String ARCHIVE_LINK_NULL_MID_CODE = "5004";
        public static final String ARCHIVE_LINK_FAILED = "FAILED";
        public static final String RESEND_LINK_MSG = "Notifications for the link are processed";
        public static final String RESEND_LINK_SUCCESS_CODE = "200";
        public static final String RESEND_LINK_NULLMID_CODE = "5004";
        public static final String RESEND_LINK_NULLMID_MSG = "Merchant id  is a required parameter";
        public static final String RESEND_LINK_NULL_LINKID_CODE = "302";
        public static final String RESEND_LINK_NULL_LINKID_MSG = "linkId cannot be empty";
        public static final String RESEND_LINK_EXPIRE_LINK_MSG = "Sending payment notification is not permitted in an expired link.";
        public static final String RESEND_LINK_EXPIRE_LINK_CODE = "5056";
        public static final String ISNEW_MERCHANT_CODE = "200";
        public static final String ISNEW_MERCHANT_MSG = "Request Successfully Processed";
        public static final String ISNEW_MERCHANT_NULLMID_CODE = "5004";
        public static final String ISNEW_MERCHANT_NULLMID_MSG = "Merchant id  is a required parameter";
        public static final String ISNEW_MERCHANT_NULL_FEATURE_MSG = "Feature name is a required parameter";
        public static final String ISNEW_MERCHANT_NULL_FEATURE_CODE = "6030";
        public static final String ISNEW_MERCHANT_WRONG_FEATURE_CODE = "8009";
        public static final String ISNEW_MERCHANT_WRONG_FEATURE_MSG = "Link feature name is invalid. Valid values are : [PAYMENT_BUTTON]";
        public static final String SUMMARY_LINK_SUCCESS_MSG ="Link summary processed successfully.";
        public static final String SUMMARY_LINK_SUCCESS_CODE ="200";
        public static final String SUMMARY_LINK_NULL_MID ="Merchant id  is a required parameter";
        public static final String SUMMARY_LINK_NULL_MID_CODE ="5004";
        public static final String SUMMARY_LINK_DATE_ISSUE_CODE ="302";
        public static final String SUMMARY_LINK_DATE_ISSUE_MSG ="start and end date can have a maximum of 31 days range";
        public static final String SAVE_DEFAULT_SETTINGS_MSG="SUCCESS";
        public static final String SAVE_DEFAULT_SETTINGS_CODE="200";
        public static final String SAVE_DEFAULT_SETTINGS_MIDNULL="Merchant id  is a required parameter";
        public static final String SAVE_DEFAULT_SETTINGS_MIDNULL_CODE="5004";
        public static final String SAVE_DEFAULT_SETTINGS_REMINDER_NULL_CODE="302";
        public static final String SAVE_DEFAULT_SETTINGS_REMINDER_NULL="Please specify the reminder schedule.";
        public static final String SAVE_DEFAULT_SETTINGS_CHANNEL_NULL="Channel list for reminder cannot be empty";
        public static final String SAVE_DEFAULT_SETTINGS_CHANNEL_NULL_CODE="302";

        public static final String FETCH_DEFAULT_SETTINGS_MSG="SUCCESS";
        public static final String FETCH_DEFAULT_SETTINGS_CODE="200";
        public static final String FETCH_DEFAULT_SETTINGS_NULLMID="Merchant id  is a required parameter";
        public static final String FETCH_DEFAULT_SETTINGS_NULLMID_CODE="5004";

        public static final String FETCH_TXNV1_NULLMID="Something bad happened.";
        public static final String FETCH_TXNV1_NULLMID_CODE="502";
        public static final String FETCH_TXNV1_NULL_LINKID="Empty link Id. ";
        public static final String FETCH_TXNV1_NULL_LINKID_CODE="312";
        public static final String FETCH_TXNV1_DATE="start and end date can have a maximum of 31 days range";
        public static final String FETCH_TXNV1_DATE_CODE="302";
        public static final String FETCH_TXNV1_SUCCESS="Payment link is processed successfully";
        public static final String FETCH_TXNV1_SUCCESS_CODE="200";



        public static final String UPDATE_LINK_SUCCESS = "Payment link is processed successfully";
        public static final String UPDATE_LINK_SUCCESS_CODE = "200";
        public static final String UPDATE_LINK_EMPTY_LINKID = "Empty link Id. ";
        public static final String UPDATE_LINK_EMPTY_LINKID_CODE = "312";
        public static final String UPDATE_LINK_EMPTY_MID = "Merchant id  is a required parameter";
        public static final String UPDATE_LINK_EMPTY_MIDCODE = "5004";
        public static final String UPDATE_LINK_AMOUNT_CODE = "5082";
        public static final String UPDATE_LINK_AMOUNT_MSG = "Amount update is only allowed for fixed links";
        public static final String GET_LINK_DETAIL_MSG ="Request Successfully Processed";
        public static final String GET_LINK_DETAIL_CODE ="200";
        public static final String GET_LINK_NULL_MID ="empty merchant Id.";
        public static final String GET_LINK_NULL_MID_CODE ="5004";
        public static final String GET_LINK_NULL_LINKID_CODE ="302";
        public static final String GET_LINK_NULL_LINKID ="Both linkid and invoice id cannot be empty. At least one value must be polulated at a time.";



        public static final String EMI_LINK_TYPE_VALIDATION = "Emi Subvention is only allowed on FIXED links.";
        public static final String EMI_PRODUCTID_VALIDATION = "Item's product id can't be blank.";
        public static final String EMI_BRANDID_VALIDATION = "Item's brand id can't be blank.";
        public static final String EMI_QUANTITY_VALIDATION = "Item's quantity is invalid.";
        public static final String EMI_PRICE_VALIDATION = "Price of item can't be blank.";
        public static final String EMI_CATEGORYLIST_VALIDATION = "Item's categories can't be blank.";
        public static final String EMI_ID_VALIDATION = "Item's id is invalid.";
        public static final String EMI_ITEM_PRICE_VALIDATION = "All items cumulative amount should be equal to link's amount.";
        public static final String ALL_EMI_VALIDATION = "Both items and amount based subvention can't be set together.";
        public static final String EMI_SUBVENTION_PREF_VALIDATION = "Merchant is not allowed to set EMI subvention details on payment links";
        public static final String LINK_MONTHLY_TXN_LIMIT = "Please try with lower amount or different payment mode for this transaction";
        public static final String EXPIRE_LINK_SUCCESS = "Payment link is expired successfully";
        public static final String EXPIRE_LINK_SUCCESS_CODE = "200";
        public static final String EXPIRE_LINK_NULLMID = "Merchant id  is a required parameter";
        public static final String EXPIRE_LINK_NULLMID_CODE = "5004";
        public static final String EXPIRE_LINK_NULL_LINKID_CODE = "302";
        public static final String EXPIRE_LINK_NULL_LINKID = "linkId cannot be empty";
        public static final String FETCH_TRANSACTION_NULLMID="Something bad happened.";
        public static final String FETCH_TRANSACTION_NULLMID_CODE="502";
        public static final String FETCH_TRANSACTION_NULL_LINKID="Empty link Id. ";
        public static final String FETCH_TRANSACTION_NULL_LINKID_CODE="312";
        public static final String FETCH_TRANSACTION_SUCCESS="Payment link is processed successfully";
        public static final String FETCH_TRANSACTION_SUCCESS_CODE="200";
        public static final String FETCH_TRANSACTION_DATE_MSG="start and end date can have a maximum of 31 days range";
        public static final String FETCH_TRANSACTION_DATE_CODE="302";



        public static final String CREATE_LINK_SUCCESS_CODE ="200";
        public static final String CREATE_LINK_SUCCESS_STATUS ="SUCCESS";
        public static final String PAYMENT_BTN_NOT_SUPORTED="Payment buttons is not supported for this merchant";
        public static final String PAYMENT_BTN_NOT_SUPPORTED_CODE ="8001";
        public static final String CREATE_LINK_FAILED_STATUS ="FAILED";
        public static final String PAYMENT_BTN_INACTIVE="Payment buttons are inactive for this merchant";
        public static final String PAYMENT_BTN_INACTIVE_CODE ="8011";
        public static final String UPDATE_LINK_MSG= "Payment link is processed successfully";

        public static final String MSG_AMOUNT_MORETHAN_100CR= "Amount should be less than equal to Rs 1000000000";
        public static final String MSG_AMOUNT_LESSTHAN_1 = "Amount should be greater than equal to Rs 1";
        public static final String CODE_AMOUNT_MORETHAN_100CR= "7014";
        public static final String CODE_AMOUNT_LESSTHAN_1= "5018";

        public static final String SAVEUPDATE_LINK_CODE ="LF_002";
        public static final String SAVEUPDATE_LINK_MESSAGE ="Invalid fields data in request";
        public static final String SAVEUPDATE_LINK_ERRORFIELD ="Validation failed for constraint maxValue for field Amount";
        public static final String GENERATEQR_SUCCESS="Payment link QR code is created successfully";
        public static final String GENERATEQR_EMPTY_LINKID ="linkId cannot be empty";
        public static final String GENERATEQR_MIDNULL ="Merchant id  is a required parameter";
        public static final String GENERATEQR_MID_WRONGVALUE ="Merchant id of the payment link does not match with merchantId passed in request.";
        public static final String GENERATEQR_ALPHABETIC_LINKID ="Something bad happened.";
        public static final String SINGLETXN_GENERIC_LINK ="Single transaction on links feature is supported only on fixed and invoice links";
        public static final String SINGLETXN_GENERIC_LINK_CODE ="9021";
        public static final String SINGLETXN_ORDERID ="Length of order id cannot exceed 50 characters.";
        public static final String SINGLETXN_ORDERID_REGEX ="Order id should not contain any special characters";
        public static final String SINGLETXN_MAXPAYMENT ="Max payments allowed value should be 1 for single transaction links";
        public static final String SINGLETXN_PARTIAL ="Partial payment feature is not supported on single transaction links";
        public static final String SINGLETXN_OLDLINKS ="Single transaction links feature is available only on latest link flow. Please reach out to paytm in case of any query.";
        public static final String TOATALAMOUNT_LESS ="Total payment amount allowed should not be less than amount value";
        public static final String TOATALAMOUNT_LESSCODE ="9009";
        public static final String TOATALAMOUNT_INVOIVE ="Invoice link type is not supported with total payment amount allowed";
        public static final String TOATALAMOUNT_INVOICECODE ="9005";
        public static final String TOATALAMOUNT_PARTIAL ="Partial payment is not supported with total payment amount allowed";
        public static final String TOATALAMOUNT_PARTIALCODE ="9006";
        public static final String TOATALAMOUNT_VALUE ="Total payment amount allowed should be less than equal to Rs 1000000000";
        public static final String TOATALAMOUNT_VALUECODE ="9007";
        public static final String TOATALAMOUNT_ZERO ="Total payment amount allowed should be greater than equal to Rs 1";
        public static final String TOATALAMOUNT_ZEROCODE ="9008";
        public static final String TOATALAMOUNT_UPDATEINVOICE ="Total payment amount allowed cannot be updated for invoice links";
        public static final String TOATALAMOUNT_UPDATEINVOICECODE ="9010";
        public static final String TOATALAMOUNT_UPDATEPARTIAL ="Total payment amount allowed cannot be updated for Partial payment links";
        public static final String TOATALAMOUNT_UPDATEPARTIALCODE ="9011";
        public static final String UDF_ERROR_MESSEGE ="Maximum additional info udf text allowed is of 400 characters";
        public static final String UDF_String_1 ="dfghudf3:,.90()uefksjdfneanfausdfjnfkerufanksnfsjgfbdjewaakbfxdeufbxnrjekaednkerbfaaefwkjhxnbejdwflwerjfbxdsjxfjaefjwelfxeejldsbfxubjrgbhkashdbfkbxejwbxbekwfjxkaelfrwerjxejjnxadwnjxewjfnjjnewfbfddgsjfcbnsaxhjsdabzkxahskzxbhewzhbsbbhdhzkshkwehfdhberfhdsajknjdknwjqjEWUIQFDQBEWBFHCBDSBJDJKBSJKAJKBDBJJBDFJBDSBJDBCJBJCBDJEJWBDBEWRFIYYEWBFBHDSBHBHFEBHFBEBKFEBJKFEWJBKJBKEWJBKJKBWEBJWFEBJEBJJBKEFWJBBJJKEW";
        public static final String UDF_String_2 ="dfghudf3:,.90()uefksjdfneanfausdfjnfkerufanksnfsjgfbdjewaakbfxdeufbxnrjekaednkerbfaaefwkjhxnbejdwflwerjfbxdsjxfjaefjwelfxeejldsbfxubjrgbhkashdbfkbxejwbxbekwfjxkaelfrwerjxejjnxadwnjxewjfnjjnewfbfddgsjfcbnsaxhjsdabzkxahskzxbhewzhbsbbhdhzkshkwehfdhberfhdsajknjdknwjqjEWUIQFDQBEWBFHCBDSBJDJKBSJKAJKBDBJJBDFJBDSBJDBCJBJCBDJEJWBDBEWRFIYYEWBFBHDSBHBHFEBHFBEBKFEBJKFEWJBKJBKEWJBKJKBWEBJWFEBJEBJJBKEFWJBBJJKEWdf";



    }
}
