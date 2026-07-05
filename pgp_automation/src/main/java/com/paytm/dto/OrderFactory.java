package com.paytm.dto;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.dto.OdishaCustomPTC.CustomPTCOdishaDTO;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.User;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.util.PayMethodType;
import com.paytm.utils.merchant.util.exception.authException.AuthException;

import java.util.Date;

/**
 * Created by sureshgupta on 27/09/17.
 */
public abstract class OrderFactory {

    public static class PGOnly extends OrderDTO.Builder {
        public PGOnly(String mid, String mKey, String theme, String... a) {
            this(mid, mKey, theme);
        }

        public PGOnly(String mid, String theme) {
            this(mid, "", theme);
        }

        public PGOnly(Constants.MerchantType merchantType, String theme) {
            this(merchantType.getId(), merchantType.getKey(), theme);
        }

        private PGOnly(String mid, String mKey, String theme) {

            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = mid;
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "DEFAULT";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.AUTH_MODE = "3D";
            this.CHANNEL_ID = getChannel(theme);
            this.merchantKey = mKey;
        }
    }

    public static class CCBillPayment extends OrderDTO.Builder {
        public CCBillPayment(String mid, String theme, User user) throws AuthException {
            this(mid, "", theme, user);
        }

        public CCBillPayment(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this(merchantType.getId(), merchantType.getKey(), theme, user);
        }

        private CCBillPayment(String mid, String mKey, String theme, User user) throws AuthException {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = mid;
            this.merchantKey = mKey;
            this.CUST_ID = user.custId();
            this.REQUEST_TYPE = "CC_BILL_PAYMENT";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.AUTH_MODE = "3D";
        }
    }

    public static class Hybrid extends OrderDTO.Builder {
        public Hybrid(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this(merchantType.getId(), merchantType.getKey(), theme, user);
        }

        public Hybrid(String mid, String theme, User user) throws AuthException {
            this(mid, "", theme, user);
        }

        private Hybrid(String mid, String mKey, String theme, User user) throws AuthException {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = mid;
            this.merchantKey = mKey;
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "DEFAULT";
            this.TXN_AMOUNT = "2.0";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.AUTH_MODE = "3D";
            this.SSO_TOKEN = user.ssoToken();
        }
    }

    public static class AddMoneyMP extends OrderDTO.Builder {
        public AddMoneyMP(Constants.MerchantType merchantType, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "DEFAULT";
            this.TXN_AMOUNT = "1.0";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.AUTH_MODE = "3D";
        }

        public AddMoneyMP(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this(merchantType, theme);
            this.setSSO_TOKEN(user.ssoToken());
        }
    }

    public static class AddMoney extends OrderDTO.Builder {
        public AddMoney(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "ADD_MONEY";
            this.TXN_AMOUNT = "2.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.AUTH_MODE = "3D";
            this.SSO_TOKEN = user.ssoToken();
        }
    }

    public static class WalletOnly extends OrderDTO.Builder {
        public WalletOnly(Constants.MerchantType merchantType, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.AUTH_MODE = "3D";
        }

        public WalletOnly(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this(merchantType, theme);
            this.SSO_TOKEN = user.ssoToken();
        }
    }

    public static class AddnPay extends OrderDTO.Builder {
        public AddnPay(String mid, String theme) {
            this(mid, "", theme);
        }

        public AddnPay(Constants.MerchantType merchantType, String theme) {
            this(merchantType.getId(), merchantType.getKey(), theme);
        }

        public AddnPay(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this(merchantType.getId(), merchantType.getKey(), theme);
            this.setSSO_TOKEN(user.ssoToken());
        }

        private AddnPay(String mid, String mKey, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = mid;
            this.merchantKey = mKey;
            this.CUST_ID = "afaq101";
            this.REQUEST_TYPE = "DEFAULT";
            this.TXN_AMOUNT = "2.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.AUTH_MODE = "3D";
        }
    }

    public static class Seamless extends OrderDTO.Builder {

        public Seamless(Constants.MerchantType merchantType, String paymentType, User user) throws AuthException {
            this(merchantType, paymentType, new PaymentDTO(), user);
        }

        public Seamless(Constants.MerchantType merchantType, String paymentType, PaymentDTO paymentDTO, User user) throws AuthException {
            this.CHANNEL_ID = "WEB";
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = user.custId();
            this.AUTH_MODE = "3D";
            //this.TOKEN_TYPE = "OAUTH";
            this.REQUEST_TYPE = "SEAMLESS";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.BANK_CODE = "HDFC";
            this.WEBSITE = "retail";
            this.PAYMENT_TYPE_ID = paymentType;
            this.PAYMENT_DETAILS =
                    PGPHelpers.getEncryptedPaymentDetails(merchantType.getKey(), paymentType, paymentDTO).replace("=","");

        }
    }

    public static class AppInvokeOrder extends OrderDTO.Builder {

        public AppInvokeOrder(Constants.MerchantType merchantType, String orderID, String TxnToken) {
            this.MID = merchantType.getId().toString();
            this.ORDER_ID = orderID;
            this.TXN_TOKEN = TxnToken;
        }
        public AppInvokeOrder(Constants.MerchantType merchantType, String orderID, String TxnToken, String fetchAllPaymentOffers, String applyPaymentOffer) {
            this.MID = merchantType.getId().toString();
            this.ORDER_ID = orderID;
            this.TXN_TOKEN = TxnToken;
            this.fetchAllPaymentOffers = fetchAllPaymentOffers;
            this.applyPaymentOffer = applyPaymentOffer;
        }

    }

    public static class SubscriptionNative extends OrderDTO.Builder {

        public SubscriptionNative(Constants.MerchantType merchantType,
                                  String orderId,
                                  String txnToken,
                                  PayMethodType payMethodType, String SUBSCRIPTION_ID) {
            this(merchantType, orderId, txnToken, payMethodType, SUBSCRIPTION_ID, new PaymentDTO());
        }

        public SubscriptionNative(Constants.MerchantType merchantType,
                                  String orderId,
                                  String txnToken,
                                  PayMethodType payMethodType, String SUBSCRIPTION_ID, PaymentDTO paymentDTO) {
            try {
                this.native_paymentMode = payMethodType.toString();
                this.WEBSITE = "RETAIL";
                this.REQUEST_TYPE = "NATIVE_SUBSCRIPTION";
                this.INDUSTRY_TYPE_ID = "RETAIL";
                this.CHANNEL_ID = "WEB";
                this.STORE_CARD = "1";
                this.MID = merchantType.getId();
                this.ORDER_ID = orderId;
                this.TXN_TOKEN = txnToken;
                this.PAYMENT_TYPE_ID = payMethodType.toString();
                this.SUBSCRIPTION_ID = SUBSCRIPTION_ID;
                if (paymentDTO.getSavedCardId() == null) {
                    this.cardInfo = "|" + PGPHelpers.getFormattedPaymentDetails(payMethodType.toString(), paymentDTO);
                } else {
                    this.cardInfo = paymentDTO.getSavedCardId() + "||" + paymentDTO.getCvvNumber() + "|";
                }
            } catch (Exception e) {
                e.printStackTrace();
                Reporter.report.info("Failed in creating Subscription Native DTO");
            }
        }
    }

    public static class Native extends OrderDTO.Builder {

        public Native(Constants.MerchantType merchantType, String orderId, String txnToken, PaymentDTO paymentDTO, PayMethodType payMethodType) {
            this(merchantType, orderId, txnToken, paymentDTO, payMethodType.toString());
        }

        public Native(Constants.MerchantType merchantType, String orderId, String txnToken, PayMethodType payMethodType) {
            this(merchantType, orderId, txnToken, payMethodType.toString());
        }


        @Deprecated
        public Native(Constants.MerchantType merchantType, String orderId, String txnToken, String PaymentMode) {
            this(merchantType, orderId, txnToken, new PaymentDTO(), PaymentMode);
        }

        @Deprecated
        public Native(Constants.MerchantType merchantType, String orderId, String txnToken, PaymentDTO paymentDTO, String PaymentMode) {
            try {
                this.CHANNEL_ID = "WEB";
                this.AUTH_MODE = "otp";
                this.STORE_CARD = "1";
                this.MID = merchantType.getId();
                this.ORDER_ID = orderId;
                this.TXN_TOKEN = txnToken;
                this.PAYMENT_TYPE_ID = PaymentMode;
                if (paymentDTO.getSavedCardId() == null) {
                    this.cardInfo = "|" + PGPHelpers.getFormattedPaymentDetails(PaymentMode, paymentDTO);
                } else {

                    this.cardInfo = paymentDTO.getSavedCardId() + "||" + paymentDTO.getCvvNumber() + "|";
                }
            } catch (Exception e) {
                e.printStackTrace();
                Reporter.report.info("Failed in creating Native DTO");
            }
        }
    }

    public static class SeamlessNative extends OrderDTO.Builder {

        public SeamlessNative(Constants.MerchantType merchantType, String paymentType, User user) throws AuthException {
            this(merchantType, paymentType, new PaymentDTO(), user);
        }

        public SeamlessNative(Constants.MerchantType merchantType, String paymentType, PaymentDTO paymentDTO, User user) throws AuthException {
            this.CHANNEL_ID = "WEB";
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = user.custId();
            this.AUTH_MODE = "3D";
            //this.TOKEN_TYPE = "OAUTH";
            this.REQUEST_TYPE = "SEAMLESS_NATIVE";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.PAYMENT_TYPE_ID = paymentType;
            this.PAYMENT_DETAILS = PGPHelpers.getFormattedPaymentDetails(paymentType, paymentDTO);
        }
    }


    // mutual fund 
    // date : 10th Dec 2018

    public static class MF_Native extends OrderDTO.Builder {

        public MF_Native(Constants.MerchantType merchantType, PayMethodType paymentType, String orderNumber, String txnToken, String accountNumber, String aggrMid) {
            this(merchantType, paymentType.toString(), new PaymentDTO(), orderNumber, txnToken, accountNumber, aggrMid);
        }

        public MF_Native(Constants.MerchantType merchantType, String paymentType, PaymentDTO paymentDTO, String orderNumber, String txnToken, String accountNumber, String aggrmid) {
            try {
                this.CHANNEL_ID = "WEB";
                this.AUTH_MODE = "otp";
                this.STORE_CARD = "1";
                this.MID = merchantType.getId();
                this.ORDER_ID = orderNumber;
                this.TXN_TOKEN = txnToken;
                this.PAYMENT_TYPE_ID = paymentType;
                this.accountNumber = accountNumber;
                this.aggMid = aggrmid;
                if (paymentDTO.getSavedCardId() == null) {
                    this.cardInfo = "|" + PGPHelpers.getFormattedPaymentDetails(paymentType, paymentDTO);
                } else {

                    this.cardInfo = paymentDTO.getSavedCardId() + "||" + paymentDTO.getCvvNumber() + "|";
                }
            } catch (Exception e) {
                e.printStackTrace();
                Reporter.report.info("Failed in creating Native DTO");
            }
        }
    }


    public static class AOA extends OrderDTO.Builder {

        public AOA(String mid, String orderId, String txnToken, String paymentType) {
            this(mid, orderId, txnToken, paymentType, new PaymentDTO());
        }

        public AOA(String mid, String orderId, String txnToken, String paymentType, PaymentDTO paymentDTO) {
            this.native_mid = mid;
            this.native_orderId = orderId;
            this.native_channelId = "WEB";
            this.native_txnToken = txnToken;
            this.native_paymentMode = paymentType;
            this.native_authMode = "otp";
        }
    }

    public static class PaytmExpressPGOnly extends OrderDTO.Builder {

        public PaytmExpressPGOnly(Constants.MerchantType merchantType, String paymentType, User user) throws AuthException {
            this(merchantType, paymentType, new PaymentDTO(), user);
        }

        public PaytmExpressPGOnly(Constants.MerchantType merchantType, String paymentType, PaymentDTO paymentDTO, User user) throws AuthException {
            this.CHANNEL_ID = "WEB";
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = user.custId();
            this.AUTH_MODE = "3D";
            this.REQUEST_TYPE = "PAYTM_EXPRESS";
            this.TXN_AMOUNT = "1";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.PAYMENT_TYPE_ID = paymentType;
            this.BANK_CODE = "HDFC";//optional
            this.PAYMENT_DETAILS =
                    PGPHelpers.getEncryptedPaymentDetailsForExpress(this.CUST_ID, this.MID, paymentType, paymentDTO);
        }
    }

    public static class PaytmExpressAddnPay extends OrderDTO.Builder {

        public PaytmExpressAddnPay(Constants.MerchantType merchantType, String paymentType, User user) throws AuthException {
            this(merchantType, paymentType, new PaymentDTO(), user);
        }

        public PaytmExpressAddnPay(Constants.MerchantType merchantType, String paymentType, PaymentDTO paymentDTO, User user) throws AuthException {
            this.CHANNEL_ID = "WEB";
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = user.custId();
            this.AUTH_MODE = "3D";
            this.REQUEST_TYPE = "PAYTM_EXPRESS";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.STORE_CARD = "0";
            this.addMoney = "1";
            this.BANK_CODE = "HDFC";//optional
            this.PAYMENT_TYPE_ID = paymentType;
            this.PAYMENT_DETAILS =
                    PGPHelpers.getEncryptedPaymentDetailsForExpress(this.CUST_ID, this.MID, paymentType, paymentDTO);
        }
    }

    public static class PaytmExpressHybrid extends OrderDTO.Builder {
        public PaytmExpressHybrid(Constants.MerchantType merchantType, String paymentType, User user) throws AuthException {
            this(merchantType, paymentType, new PaymentDTO(), user);
        }

        public PaytmExpressHybrid(Constants.MerchantType merchantType, String paymentType, PaymentDTO paymentDTO, User user) throws AuthException {
            this.CHANNEL_ID = "WEB";
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = user.custId();
            this.AUTH_MODE = "3D";
            this.REQUEST_TYPE = "PAYTM_EXPRESS";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.STORE_CARD = "0";
            this.addMoney = "0";
            this.PAYMENT_TYPE_ID = paymentType;
            this.PAYMENT_DETAILS =
                    PGPHelpers.getEncryptedPaymentDetailsForExpress(this.CUST_ID, this.MID, paymentType, paymentDTO);
        }
    }

    public static class TopupExpress extends OrderDTO.Builder {
        public TopupExpress(Constants.MerchantType merchantType, String paymentType, String theme, User user) throws AuthException {
            this(merchantType, paymentType, new PaymentDTO(), theme, user);
        }

        public TopupExpress(Constants.MerchantType merchantType, String paymentType, PaymentDTO paymentDTO, String theme, User user) throws AuthException {
            this.CHANNEL_ID = getChannel(theme);
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = user.custId();
            this.AUTH_MODE = "3D";
            //this.TOKEN_TYPE = "OAUTH";
            this.REQUEST_TYPE = "TOPUP_EXPRESS";
            this.TXN_AMOUNT = "1";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.STORE_CARD = "0";
            this.THEME = theme;
            this.SSO_TOKEN = user.ssoToken();
            this.PAYMENT_TYPE_ID = paymentType;
            this.PAYMENT_DETAILS =
                    PGPHelpers.getEncryptedPaymentDetailsForExpress(this.CUST_ID, this.MID, paymentType, paymentDTO);
        }
    }

    public static class COD extends OrderDTO.Builder {
        public COD(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "DEFAULT";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            //this.TOKEN_TYPE = "OAUTH";
            this.PAYMENT_TYPE_ID = "COD";
            this.SSO_TOKEN = user.ssoToken();
        }
    }

    public static class SubscriptionWalletOnly extends OrderDTO.Builder {
        public SubscriptionWalletOnly(Constants.MerchantType merchantType, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.AUTH_MODE = "3D";
            //this.TOKEN_TYPE = "OAUTH";
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.TXN_AMOUNT = "2.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_PPI_ONLY = "Y";
            this.SUBS_AMOUNT_TYPE = "VARIABLE";
            this.SUBS_MAX_AMOUNT = "100";
            this.SUBS_FREQUENCY = "1";
            this.SUBS_FREQUENCY_UNIT = "MONTH";
            this.SUBS_GRACE_DAYS = "5";
            this.SUBS_ENABLE_RETRY = "1";
            this.SUBS_RETRY_COUNT = "5";
            this.SUBS_PAYMENT_MODE = "PPI";
            this.SUBS_START_DATE = CommonHelpers.getDate(new Date(), "yyyy-MM-dd");
            this.SUBS_EXPIRY_DATE = "2025-12-31";
        }
    }

    public static class SubscriptionUPI extends OrderDTO.Builder {
        public SubscriptionUPI(Constants.MerchantType merchantType, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.AUTH_MODE = "3D";
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.TXN_AMOUNT = "2.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_AMOUNT_TYPE = "VARIABLE";
            this.SUBS_MAX_AMOUNT = "25000";
            this.SUBS_FREQUENCY = "1";
            this.SUBS_FREQUENCY_UNIT = "MONTH";
            this.SUBS_GRACE_DAYS = "1";
            this.SUBS_ENABLE_RETRY = "1";
            this.SUBS_RETRY_COUNT = "1";
            this.SUBS_PAYMENT_MODE = "UPI";
            this.SUBS_START_DATE = CommonHelpers.getDate(new Date(), "yyyy-MM-dd");
            this.SUBS_EXPIRY_DATE = CommonHelpers.addYears(SUBS_START_DATE, "yyyy-MM-dd", 1);
        }
    }

    public static class SubscriptionPPI extends OrderDTO.Builder {
        public SubscriptionPPI(Constants.MerchantType merchantType, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.AUTH_MODE = "3D";
            //this.TOKEN_TYPE = "OAUTH";
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.TXN_AMOUNT = "2.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_PPI_ONLY = "N";
            this.SUBS_AMOUNT_TYPE = "VARIABLE";
            this.SUBS_MAX_AMOUNT = "100";
            this.SUBS_FREQUENCY = "1";
            this.SUBS_FREQUENCY_UNIT = "MONTH";
            this.SUBS_GRACE_DAYS = "5";
            this.SUBS_ENABLE_RETRY = "1";
            this.SUBS_RETRY_COUNT = "5";
            this.SUBS_PAYMENT_MODE = "PPI";
            this.SUBS_START_DATE = CommonHelpers.getDate(new Date(), "yyyy-MM-dd");
            this.SUBS_EXPIRY_DATE = "2025-12-31";
        }
    }

    public static class SubscriptionCC_DC extends OrderDTO.Builder {
        public SubscriptionCC_DC(Constants.MerchantType merchantType, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.AUTH_MODE = "3D";
            //this.TOKEN_TYPE = "OAUTH";
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.TXN_AMOUNT = "1.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_PPI_ONLY = "";                  //  'N' is defunct - PGP-31970
            this.SUBS_AMOUNT_TYPE = "VARIABLE";
            this.SUBS_MAX_AMOUNT = "100";
            this.SUBS_FREQUENCY = "1";
            this.SUBS_FREQUENCY_UNIT = "MONTH";
            this.SUBS_GRACE_DAYS = "5";
            this.SUBS_ENABLE_RETRY = "1";
//            this.SUBS_RETRY_COUNT = "5";
            this.SUBS_PAYMENT_MODE = "CC";
            this.SUBS_START_DATE = CommonHelpers.getDate(new Date(), "yyyy-MM-dd");
            this.SUBS_EXPIRY_DATE = "2025-12-31";
        }

        public SubscriptionCC_DC(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this(merchantType, theme);
            this.SSO_TOKEN = user.ssoToken();
        }
    }

    public static class SubscriptionS2S extends OrderDTO.Builder {
        public SubscriptionS2S(Constants.MerchantType merchantType, String savedCardId, User user) throws AuthException {
            this.CHANNEL_ID = "WEB";
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.AUTH_MODE = "3D";
            //this.TOKEN_TYPE = "OAUTH";
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.TXN_AMOUNT = "0.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_PPI_ONLY = "N";
            this.SUBS_AMOUNT_TYPE = "VARIABLE";
            this.SUBS_MAX_AMOUNT = "100";
            this.SUBS_FREQUENCY = "1";
            this.SUBS_FREQUENCY_UNIT = "MONTH";
            this.SUBS_GRACE_DAYS = "5";
            this.SUBS_ENABLE_RETRY = "1";
            this.SUBS_RETRY_COUNT = "5";
            this.SUBS_PAYMENT_MODE = "CC";
            this.SUBS_START_DATE = CommonHelpers.getDate(new Date(), "yyyy-MM-dd");
            this.SUBS_EXPIRY_DATE = "2022-12-31";
            this.SAVED_CARD_ID = savedCardId;
            this.CONNECTION_TYPE = "S2S";
            this.SSO_TOKEN = user.ssoToken();
        }
    }

    public static class IVRFastForward extends OrderDTO.Builder {
        public IVRFastForward(Constants.MerchantType merchantType) {
            this.CHANNEL_ID = "WEB";
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.TXN_AMOUNT = "1.00";
        }
    }

    public static class OffLineTxn extends OrderDTO.Builder {
        public OffLineTxn(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this(merchantType, theme, user.ssoToken());
        }

        public OffLineTxn(Constants.MerchantType merchantType, String theme, String ssoToken) throws AuthException {
            this.REQUEST_TYPE = "OFFLINE";
            this.INDUSTRY_TYPE_ID = "Retail";
            this.CHANNEL_ID = getChannel(theme);
            this.TXN_AMOUNT = "2.0";
            this.AUTH_MODE = "3D";
            this.WEBSITE = "retail";
            this.TOKEN_TYPE = "SSO";
            this.SSO_TOKEN = ssoToken;
            this.PAYMENT_DETAILS = "";
            this.PAYMENT_TYPE_ID = "PPI";
            this.MID = merchantType.getId();
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.PASSCODE = "5335";
            this.THEME = theme;
            this.ADDITIONAL_INFO = "CITY:NOIDA|REQUEST_TYPE:OFFLINE";
            this.merchantKey = merchantType.getKey();
            this.ADDITIONAL_INFO = "NOIDA";
            this.POS_ID = "12345";
            this.UNIQUE_REFERENCE_LABEL = "BigBazaar";
            this.UNIQUE_REFERENCE_VALUE = "BigB";
            this.PCC_CODE = "1234567890";
            this.PRN = "12313213213123123";
            this.UDF_1 = "Plot%206";
            this.UDF_2 = "Udyog%20Vihar";
            this.UDF_3 = "Noida";
            this.COMMENTS = "Generic%20Information";
        }
    }

    public static class SubscriptionRenew extends OrderDTO.Builder {
        public SubscriptionRenew(Constants.MerchantType merchantType, String subsId, String txnAmount) {
            this.REQUEST_TYPE = "RENEW_SUBSCRIPTION";
            this.merchantKey = merchantType.getKey();
            this.TXN_AMOUNT = txnAmount;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.SUBSCRIPTION_ID = subsId;
            this.MID = merchantType.getId();
        }
    }

    public static class SubWallet extends OrderDTO.Builder {

        public SubWallet(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "DEFAULT";
            this.TXN_AMOUNT = "2.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.AUTH_MODE = "3D";
            this.CHANNEL_ID = getChannel(theme);
            this.SSO_TOKEN = user.ssoToken();
        }
    }

    public static class AddMoneyExpress extends OrderDTO.Builder {

        public AddMoneyExpress(Constants.MerchantType merchantType, String paymentType, User user, PaymentDTO paymentDTO) {
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "EXPRESS_ADD_MONEY";
            this.TXN_AMOUNT = "2.00";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.AUTH_MODE = "3D";
            this.SSO_TOKEN = user.ssoToken();
            this.PAYMENT_TYPE_ID = paymentType;
            this.PAYMENT_DETAILS = PGPHelpers.getEncryptedPaymentDetailsForExpressAddMoney(paymentType, paymentDTO);
            this.THEME = "enhancedweb";
        }
    }

    public static class BankMandate extends OrderDTO.Builder {

        public BankMandate(Constants.MerchantType merchantType, String txnToken, String orderId, String subsId) {
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.TXN_TOKEN = txnToken;
            this.ORDER_ID = orderId;
            this.SUBSCRIPTION_ID = subsId;
            this.INDUSTRY_TYPE_ID = "retail";
            this.CHANNEL_ID = "WEB";
            this.AUTH_MODE = "USRPWD";
            this.WEBSITE = "retail";
            this.PAYMENT_TYPE_ID = "BANK_MANDATE";
            this.channelCode = "HDFC";
            this.account_number = "917503569332";
            this.bankIfsc = "PYTM0000001";
            this.ACCOUNT_TYPE = "OTHERS";
            this.USER_NAME = "Akshat Sharma";
        }

        public BankMandate(Constants.MerchantType merchantType, String txnToken, String orderId, String subsId, String paymentType) {
            this(merchantType, txnToken, orderId, subsId);
            PaymentDTO paymentDTO = new PaymentDTO();
            switch (paymentType) {
                case "BANK_MANDATE":
                    this.PAYMENT_TYPE_ID = "BANK_MANDATE";
                    this.account_number = "917503569332";
                    this.bankIfsc = "PYTM0000001";
                    this.ACCOUNT_TYPE = "OTHERS";
                    this.USER_NAME = "Akshat Sharma";
                    break;
                case "CREDIT_CARD":
                    this.PAYMENT_TYPE_ID = "CREDIT_CARD";
                    this.cardInfo =
                            "|" + paymentDTO.getCreditCardNumber() + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
                    this.AUTH_MODE = "otp";
                    break;
                case "DEBIT_CARD":
                    this.PAYMENT_TYPE_ID = "DEBIT_CARD";
                    this.cardInfo =
                            "|" + paymentDTO.getDebitCardNumber() + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
                    this.AUTH_MODE = "otp";
                    break;
                default:
                    throw new RuntimeException("Invalid PAYMENT_TYPE_ID");
            }
            this.PAYMENT_TYPE_ID = paymentType;

        }

        public BankMandate(Constants.MerchantType merchantType, String theme, User user) throws AuthException {
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.AUTH_MODE = "3D";
            this.PAYMENT_TYPE_ID = "BANK_MANDATE";
            this.channelCode = "HDFC";
            this.ACCOUNT_TYPE = "OTHERS";
            this.USER_NAME = "Akshat Sharma";
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.CUST_ID = this.ORDER_ID;
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_ENABLE_RETRY="1";
            this.SUBS_EXPIRY_DATE = "2025-12-31";
            this.SSO_TOKEN = user.ssoToken();
            this.accountNumber="915445500424";
            this.BANK_IFSC="PYTM0000001";
            this.subscriptionPurpose="Loan Payments";

        }
    }

    /**
     * Bank mandate defaults aligned with JSON-style init payloads ({@code subscriptionId}, {@code authMode},
     * {@code paymentMode}, etc.), backed by the same {@link OrderDTO.Builder} fields as {@link BankMandate}
     * ({@code SUBSCRIPTION_ID}, {@code AUTH_MODE}, {@code PAYMENT_TYPE_ID}, …).
     */
    public static class BankMandateJson extends OrderDTO.Builder {

        public BankMandateJson(Constants.MerchantType merchantType, String txnToken, String orderId, String subsId) {
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.TXN_TOKEN = txnToken;
            this.ORDER_ID = orderId;
            this.INDUSTRY_TYPE_ID = "retail";
            this.CHANNEL_ID = "WEB";
            this.SUBSCRIPTION_ID = subsId;
            this.AUTH_MODE = "USRPWD";
            this.PAYMENT_TYPE_ID = "BANK_MANDATE";
            this.mandateAuthMode = "NET_BANKING";
            this.channelCode = "HDFC";
            this.accountNumber = "915445500424";
            this.bankIfsc = "PYTM0000001";
            this.accountType = "OTHERS";
            this.userName = "Akshat Sharma";
        }

        public BankMandateJson(Constants.MerchantType merchantType, String txnToken, String orderId, String subsId,
                               String paymentType) {
            this(merchantType, txnToken, orderId, subsId);
            PaymentDTO paymentDTO = new PaymentDTO();
            switch (paymentType) {
                case "BANK_MANDATE":
                    this.PAYMENT_TYPE_ID = "BANK_MANDATE";
                    this.mandateAuthMode = "NET_BANKING";
                    this.channelCode = "HDFC";
                    this.accountNumber = "915445500424";
                    this.bankIfsc = "PYTM0000001";
                    this.accountType = "OTHERS";
                    this.userName = "Akshat Sharma";
                    break;
                case "CREDIT_CARD":
                    this.PAYMENT_TYPE_ID = "CREDIT_CARD";
                    this.mandateAuthMode = null;
                    this.cardInfo =
                            "|" + paymentDTO.getCreditCardNumber() + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
                    this.AUTH_MODE = "otp";
                    break;
                case "DEBIT_CARD":
                    this.PAYMENT_TYPE_ID = "DEBIT_CARD";
                    this.mandateAuthMode = null;
                    this.cardInfo =
                            "|" + paymentDTO.getDebitCardNumber() + "|" + paymentDTO.getCvvNumber() + "|" + paymentDTO.getExpMonth() + paymentDTO.getExpYear();
                    this.AUTH_MODE = "otp";
                    break;
                default:
                    throw new RuntimeException("Invalid PAYMENT_TYPE_ID");
            }
            this.PAYMENT_TYPE_ID = paymentType;
        }
    }

    public static class MGV_Native extends OrderDTO.Builder {

        public MGV_Native(Constants.MerchantType merchantType,
                          String orderId,
                          String txnToken,
                          String payMethodType, String templateID) {
            try {
                this.native_paymentMode = payMethodType;
                this.native_authMode = "3D";
                this.CHANNEL_ID = "WEB";
                this.MID = merchantType.getId();
                this.ORDER_ID = orderId;
                this.TXN_TOKEN = txnToken;
                this.PAYMENT_TYPE_ID = payMethodType;
                this.templateId = templateID;

            } catch (Exception e) {
                e.printStackTrace();
                Reporter.report.info("Failed in creating MGV Native DTO");
            }
        }
    }


    public static class UPIMandate extends OrderDTO.Builder {

        public UPIMandate(Constants.MerchantType merchantType, String txnToken, String orderId, String subsId) {
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.TXN_TOKEN = txnToken;
            this.ORDER_ID = orderId;
            this.SUBSCRIPTION_ID = subsId;
            this.INDUSTRY_TYPE_ID = "retail";
            this.CHANNEL_ID = "WEB";
            this.AUTH_MODE = "USRPWD";
            this.WEBSITE = "retail";
            this.channelCode = "";
            this.account_number = "917503569332";
            this.ACCOUNT_TYPE = "OTHERS";
            this.PAYMENT_TYPE_ID = "UPI";
        }


        public UPIMandate(Constants.MerchantType merchantType,String Paymode ,String txnToken, String orderId, String subsId) {
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.TXN_TOKEN = txnToken;
            this.ORDER_ID = orderId;
            this.SUBSCRIPTION_ID = subsId;
            this.INDUSTRY_TYPE_ID = "retail";
            this.CHANNEL_ID = "WEB";
            this.AUTH_MODE = "USRPWD";
            this.WEBSITE = "retail";
            this.channelCode = "";
            this.account_number = "917503569332";
            this.ACCOUNT_TYPE = "OTHERS";
            this.PAYMENT_TYPE_ID = Paymode;
        }




        public UPIMandate(Constants.MerchantType merchantType, String theme, User user) throws AuthException {

            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.AUTH_MODE = "3D";
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_ENABLE_RETRY="1";
            this.SUBS_PAYMENT_MODE="UPI";
            this.SUBS_EXPIRY_DATE = "2025-12-31";
            this.SSO_TOKEN = user.ssoToken();

        }
    }

    public static class PospaidSubs extends OrderDTO.Builder {

        public PospaidSubs(Constants.MerchantType merchantType, String theme) {
            this.CHANNEL_ID = getChannel(theme);
            this.THEME = theme;
            this.ORDER_ID = CommonHelpers.generateOrderId();
            this.MID = merchantType.getId();
            this.merchantKey = merchantType.getKey();
            this.CUST_ID = this.ORDER_ID;
            this.AUTH_MODE = "3D";
            this.REQUEST_TYPE = "SUBSCRIBE";
            this.INDUSTRY_TYPE_ID = "retail";
            this.WEBSITE = "retail";
            this.SUBS_ENABLE_RETRY="1";
            this.SUBS_PAYMENT_MODE="PAYTM_DIGITAL_CREDIT";
            this.SUBS_EXPIRY_DATE = "2025-12-31";
        }
    }


    public static class CustomProcessTransaction extends OrderDTO.Builder {

        public CustomProcessTransaction(CustomPTCOdishaDTO customPTCOdishaDTO) throws Exception {
            this.MID = customPTCOdishaDTO.getMERCHANTCD();
            this.msg = customPTCOdishaDTO.generateChecksum();
            this.ORDER_ID = customPTCOdishaDTO.getCHLNREFNO();
            this.merchantCode= customPTCOdishaDTO.getMERCHANTCD();

        }
    }

}