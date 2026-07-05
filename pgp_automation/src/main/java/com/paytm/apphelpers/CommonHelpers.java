package com.paytm.apphelpers;


import com.paytm.LocalConfig;
import com.paytm.ServerConfigProvider;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.utils.merchant.util.OtpStrings;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import redis.clients.jedis.Jedis;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;
import static com.paytm.framework.utils.CommonUtils.doubleUpConvertor;

public class CommonHelpers {

    public static String generateOrderId() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
    public static String getInvalidOTP()
    {
      return new DecimalFormat("").format(new Random().nextInt(999999));

    }

    public static String getDate(Date date, String format) {
        final DateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    public static long getCurrentEpochTime()
    {
        return System.currentTimeMillis();
    }

    public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        return md.digest(input.getBytes(StandardCharsets.UTF_8));
    }

    public static String toHexString(byte[] hash)
    {
        BigInteger number = new BigInteger(1, hash);

        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 32)
        {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    public static Date convertStringToDate(String inDate, String format) {
        final DateFormat sdf = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = sdf.parse(inDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String convertDateToString(Date inDate, String format) {
        final DateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(inDate);
    }

    public static Date addDays(Date date, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, days);
        return cal.getTime();
    }

    public static String addDays(String inDate, String format, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(inDate, format));
        cal.add(Calendar.DATE, days);
        return convertDateToString(cal.getTime(), format);
    }

    public static Date subtractDays(Date date, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.add(Calendar.DATE, -days);
        return cal.getTime();
    }

    public static String subtractDays(String inDate, String format, int days) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(inDate, format));
        cal.add(Calendar.DATE, -days);
        return convertDateToString(cal.getTime(), format);
    }

    public static Double emiCalc(Double principal, Double roi, int months) {
        Double emi = 0.0;
        Double R = (roi / 12) / 100;
        emi = (principal * R * (Math.pow((1 + R), months)) / ((Math.pow((1 + R), months)) - 1));
        return Double.parseDouble((doubleUpConvertor(emi)));
    }

    public static String doubleToTwoDigitAfterDecimalPoint(Double number) {
        try {
            String result = String.format("%.2f", number);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String stringToDoubleWithTwoDigitAfterDecimalPoint(String number) {
        try {
            double donum = Double.parseDouble(number);
            String result = String.format("%.2f", donum);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Double stringToDouble(String value) {
        try {
            return Double.valueOf(value);
        } catch (NullPointerException | NumberFormatException e) {
            return null;
        }
    }

    public static String maskCardNumber(String cardNumber) {

        String maskedNumber = null;
        return maskedNumber;
    }

    public static double doubleHalfUpConvertor(double number) {
        DecimalFormat format = new DecimalFormat("###.##");
        format.setRoundingMode(RoundingMode.HALF_UP);
        return Double.valueOf(format.format(number));
    }

    public static String getOtp(String mobileNo, OtpStrings otpStrings) {
        String otp = otpStrings.getOtp(mobileNo);
        Reporter.report.info("Your OTP is: " + otp);
        return otp;
    }


    public static String getUpdatedString(String... args) {
        for (int i = 1; i <= args.length - 1; i++) {
            args[0] = args[0].replaceFirst("\\{\\?\\}", args[i]);
        }
        return args[0];
    }

    public static String getCardLastFourDigit(String cardNum) {
        int cardLength = cardNum.length();
        String lastFour = cardNum.substring(cardLength - 4, cardLength);
        return lastFour;
    }

    public static String getCardFirstSixDigit(String cardNum) {
        String cardFirstSix = cardNum.substring(0, 6);
        return cardFirstSix;
    }

    public static int getRandomWithSize(int size) {
        int max = (int) Math.pow(10, size);
        Random rand = new Random();
        int result = (int) (rand.nextFloat() * max);
        return result;
    }

    public static void assertCheck(JsonPath jsonPath, Object[] objects) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        for (int i = 0; i < objects.length; i++) {
            map.put(objects[i], objects[++i]);
        }
        Set set = map.entrySet();
        Iterator itr = set.iterator();
        while (itr.hasNext()) {
            Map.Entry entry = (Map.Entry) itr.next();
            Assertions.assertThat(jsonPath.getString(entry.getKey().toString())).as(entry.getKey().toString()+" mismatched").
                    isEqualTo(entry.getValue().toString());
        }
    }

    public static LocalDate getDate(){
        LocalDate date = LocalDate.now();
        return date;
    }


    public static String formatString(String s, Object... valuesToBeReplacePlaceholders) {
        for (Object o : valuesToBeReplacePlaceholders) {
            s = s.replaceFirst("%s", o.toString());
        }
        return s;
    }

    public static void validateTxnStatus(OrderDTO orderDTO, InitTxnDTO initTxnDTO, TxnStatus txnStatus, String gatewayName,String bankName,String paymentMode){
        txnStatus.executeUntilNotPending();
        if(!(initTxnDTO.getBody().getSubscriptionFrequencyUnit()==null))
        {
            txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                    .validateOrderid(orderDTO.getORDER_ID())
                    .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                    .validateStatus("TXN_SUCCESS")
                    .validateTxnType("SALE")
                    .validateGatewayName(gatewayName)
                    .validateRespCode("01")
                    .validateRespMsg("Txn Successful.")
                    .validateBankName(bankName)
                    .validateMid(orderDTO.getMID())
                    .validatePaymentMode(paymentMode)
                    .validateRefundAmnt("0.00")
                    .validateTxnDate(new Date())
                    .validateSubsid(Constants.ValidationType.NON_EMPTY)
                    .AssertAll();
        }else{
                txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                        .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                        .validateOrderid(orderDTO.getORDER_ID())
                        .validateTxnAmount(initTxnDTO.getBody().getTxnAmount().getValue())
                        .validateStatus("TXN_SUCCESS")
                        .validateTxnType("SALE")
                        .validateGatewayName(gatewayName)
                        .validateRespCode("01")
                        .validateRespMsg("Txn Successful.")
                        .validateBankName(bankName)
                        .validateMid(orderDTO.getMID())
                        .validatePaymentMode(paymentMode)
                        .validateRefundAmnt("0.00")
                        .validateTxnDate(new Date())
                        .AssertAll();
            }
        }

    public static void validateCache(String key) {
//        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.PG_REDIS_URI);
        Reporter.report.info("Cache Validation for key " + key);
//        String val = jedis.keys(key).toArray()[0].toString();
        String val = PGPBaseTest.TRANSACTIONAL_REDIS_CLUSTER().get(key);     //TODO: will delete other code once this is success
        Assertions.assertThat(val).as("Key not present").isNotEmpty();
    }

    //TODO Verify in Regression for the key being stored in session redis cluster or redis uri
    public static void validateCacheFromSessionRedis(String key){
      //  Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_URI);
        Jedis jedis = RedisUtil.getInstance().getConnection(LocalConfig.SESSION_REDIS_CLUSTER_URI);
        Reporter.report.info("Cache Validation for key "+ key);
        String val= jedis.keys(key).toArray()[0].toString();
        Assertions.assertThat(val).as("Key not present").isNotEmpty();
    }


    public static String addMonths(String inDate, String format, int months) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(inDate, format));
        cal.add(Calendar.MONTH, months);
        return convertDateToString(cal.getTime(), format);
    }


    public static String addYears(String inDate, String format, int years) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(inDate, format));
        cal.add(Calendar.YEAR, years);
        return convertDateToString(cal.getTime(), format);
    }

    public static List<String> getFeeFactorAsList(String inputFeeFactor){
        if(null == inputFeeFactor || inputFeeFactor.isEmpty())
            return Collections.EMPTY_LIST;
        return Arrays.asList(inputFeeFactor.split("\\|"));
    }

    public static String addSeconds(String inDate, String format, int seconds) {
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(convertStringToDate(inDate, format));
        cal.add(Calendar.SECOND, seconds);
        return convertDateToString(cal.getTime(), format);
    }
    public static void verifyPaymentNotify(String orderId) throws InterruptedException {
        String grepcmd1 = "grep " + orderId +" "+LocalConfig.PGPROXY_LOGS;
        //String pgproxyNotificationLogs = Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1), s -> !"".equals(s));
        String pgproxyNotificationLogs =  getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, grepcmd1);
        Assertions.assertThat(pgproxyNotificationLogs).contains("ACQUIRING_PAYMENT_NOTIFY");
        Assertions.assertThat(pgproxyNotificationLogs).contains("\"resultCode\":\"SUCCESS\"");
        Assertions.assertThat(pgproxyNotificationLogs).contains("\"resultStatus\":\"S\"");
    }
    public static void closeOrderNotify(String orderId) throws InterruptedException {
        String webFormContextRequest = "grep " + orderId + " "+LocalConfig.PGPROXY_LOGS+"| " +
                " grep \"Response\" | grep \"ProcessCloseNotify\"";
        String webFormContextRequestLogs = Awaitility.await().atMost(60, TimeUnit.SECONDS).until(() -> LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, webFormContextRequest), s -> !"".equals(s));
        Assertions.assertThat(webFormContextRequestLogs).contains("SUCCESS");

    }
    public static void webFormContextNotify(String orderID) throws InterruptedException {
        String greptxnID = "grep " + orderID +" "+ LocalConfig.INSTAPROXY_LOGS + "|grep \"TxnId=\"";
        String TxnID = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, greptxnID);
        String txnIDValue = TxnID.substring(TxnID.indexOf("TxnId="), TxnID.indexOf("} - {\"transId\"")).replace("TxnId=", "");
       Thread.sleep(4000);
        String webFormContextRequest = "grep " + txnIDValue +" "+LocalConfig.PGPROXY_LOGS +"| " +
                " grep \"response\" | grep \"oldPG.payment.cashier.webFormContextNotify\"";
        String webFormContextRequestLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, webFormContextRequest);
        System.out.println("logs are----"+ webFormContextRequestLogs);
        Assertions.assertThat(webFormContextRequestLogs).contains("SUCCESS");
    }
    public static void webFormContextNotifys(String orderID) throws InterruptedException {
        String greptxnID = "grep " + orderID +" "+ LocalConfig.INSTAPROXY_LOGS + "|grep \"TxnId=\"";
        String TxnID = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, greptxnID);
        String txnIDValue = TxnID.substring(TxnID.indexOf("TxnId="), TxnID.indexOf("} - {\"transId\"")).replace("TxnId=", "");
        Thread.sleep(4000);
        String webFormContextRequest = "grep " + txnIDValue +" "+LocalConfig.PGPROXY_LOGS +"| " +
                " grep \"response\" | grep \"oldPG.payment.cashier.webFormContextNotify\"";
        String webFormContextRequestLogs = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.PG_PROXY_NOTIFICATION, webFormContextRequest);
        System.out.println("logs are----"+ webFormContextRequestLogs);
        Assertions.assertThat(webFormContextRequestLogs).contains("SUCCESS");
    }
    public static void verifyEsnLength(String orderID, int length) throws InterruptedException {
        String grepEsn = "grep " + orderID +" "+ LocalConfig.INSTAPROXY_LOGS + "|grep \"ExtSN=\"";
        System.out.println("grep is---"+grepEsn);
        String extSn = LogsValidationHelper.getLogsOnServer(ServerConfigProvider.SERVICE.INSTAPROXY, grepEsn);
        String extSnValue = extSn.substring(extSn.indexOf("ExtSN="), extSn.indexOf(", OrderId=")).replace("ExtSN=", "");
        System.out.println("Esn is " + extSnValue);
        Assertions.assertThat(extSnValue.length()).isEqualTo(length);
    }
}
