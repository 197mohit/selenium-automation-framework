package scripts.WalletSetlement;


import com.paytm.api.SMSPrimary;
import com.paytm.api.notification.GetNotificationTemplateMaster;
import com.paytm.api.notification.WithdrawNotifyWalletSettlement;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.utils.merchant.helpers.mappingHelpers.GetMerchExtndInfoHelper;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.exception.JsonPathException;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

@Owner("Gagandeep")
@Epic("Wallet-Settlement")
@Feature("PGP-17977")
public class WalletSettlementNotify extends PGPBaseTest {


    private static final String auto = "MERCHANT_AUTO_WITHDRAW";
    private static final String manual = "MERCHANT_WITHDRAW";
    private static final String online = "ONLINE_SETTLEMENT";
    private static final String sms = "SMS";
    private static final String IFSC_CODE= "WALL0123456";
    private static final String accountNumber = "1000703705"; //Cust ID is Account Number in case of Wallet Settlement
    private static final String txnAmount = "10000";//(isAmountInPaise = true)
    private final Constants.MerchantType merchantType = Constants.MerchantType.WALLET_SETTLEMENT;




    private WithdrawNotifyWalletSettlement sucessWalletWithrawNotify(String Operation,String orderId){

        return new WithdrawNotifyWalletSettlement.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setIfscCode(IFSC_CODE)
                .setAccount(accountNumber)
                .setResultCode("SUCCESS")
                .setResultId("00000000")
                .setResultStatus("S")
                .setResultMsg("success")
                .setOperation(Operation)
                .build();
    }

    private WithdrawNotifyWalletSettlement failureWalletWithrawNotify(String Operation,String orderId){

        return new WithdrawNotifyWalletSettlement.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setIfscCode(IFSC_CODE)
                .setAccount(accountNumber)
                .setResultCode("PROCESS_FAIL")
                .setResultId("12007212")
                .setResultStatus("F")
                .setResultMsg("process fail")
                .setOperation(Operation)
                .build();
    }

    private WithdrawNotifyWalletSettlement pendingWalletWithrawNotify(String Operation,String orderId){

        return new WithdrawNotifyWalletSettlement.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setIfscCode(IFSC_CODE)
                .setAccount(accountNumber)
                .setResultCode("PENDING")
                .setResultId("22007014")
                .setResultStatus("A")
                .setResultMsg("pending")
                .setOperation(Operation)
                .build();
    }



    private WithdrawNotifyWalletSettlement pendingToSuccessWalletWithrawNotify(String Operation,String orderId){

        return new WithdrawNotifyWalletSettlement.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setIfscCode(IFSC_CODE)
                .setAccount(accountNumber)
                .setResultCode("PENDING_SUCCESS")
                .setResultId("22007012")
                .setResultStatus("S")
                .setResultMsg("pending to success")
                .setOperation(Operation)
                .build();
    }


    private WithdrawNotifyWalletSettlement pendingToFailureWalletWithrawNotify(String Operation,String orderId){

        return new WithdrawNotifyWalletSettlement.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setIfscCode(IFSC_CODE)
                .setAccount(accountNumber)
                .setResultCode("PENDING_FAILURE")
                .setResultId("22007013")
                .setResultStatus("F")
                .setResultMsg("pending to failure")
                .setOperation(Operation)
                .build();
    }



    private String replaceAmountAndOrder(String templateMessage, String txnAmount, String orderId) {

        if (null != templateMessage) {
            DecimalFormat df = new DecimalFormat("0.00");
            String actualTxnAmount = df.format(Integer.valueOf(txnAmount) / 100);
            /*String maskedAccountNumber = "**" + Integer.valueOf(accountNo) % 10000;*/
            return templateMessage
                    .replace("${TXN_AMOUNT}", actualTxnAmount)
                    .replace("${UTR_NUMBER}", orderId)
                    .replace("${INIT_DATE_MONTH}","26 FEB");
        } else
            throw new NullPointerException("Empty/Null Response for errorCode:  NOTIFICATION_TEMPLATE_MASTER DB");

    }



    private void assertSMSAndText(String expectedMobileNumber, String expectedMessage, String orderId) {
        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        JsonPath smsJson;
        try {
            //SMS Mock
            smsJson= smsPrimary.executeUntilGetResponse().jsonPath();

            //Assertions on mobile number and message

            Assertions.assertThat(smsJson.getString("mobileNo"))
                    .as("SMS is going to wrong number").isEqualTo(expectedMobileNumber);


            Assertions.assertThat(smsJson.getString("message"))
                    .as("SMS text is not getting matched from NOTIFICATION_TEMPLATE_MASTER")
                    .isEqualTo(expectedMessage);
        }
        catch (JsonPathException e) {

            throw new NoSuchElementException("Did not get message in communication-gateway, please check comm-gateway service logs for orderId : " + orderId);
        } catch (NullPointerException e) {
            throw new RuntimeException("Response from SMS Mock API is not correct");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void assertFailSmsText(String expectedMobileNumber, String txnAmount, String orderId) {
        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        Double txnAmt = (Double.parseDouble(txnAmount))/100;
        JsonPath smsJson;
        try {
            //SMS Mock
            smsJson = smsPrimary.executeUntilGetResponse().jsonPath();

            //Assertions on mobile number and message

            Assertions.assertThat(smsJson.getString("mobileNo"))
                    .as("SMS is going to wrong number").isEqualTo(expectedMobileNumber);


            Assertions.assertThat(smsJson.getString("message"))
                    .as("SMS text is not getting matched from NOTIFICATION_TEMPLATE_MASTER")
                    .contains(String.valueOf(txnAmt));   // Only validaating the txnAmount to be present in SMS Body.
        } catch (JsonPathException e) {

            throw new NoSuchElementException("Did not get message in communication-gateway, please check comm-gateway service logs for orderId : " + orderId);
        } catch (NullPointerException e) {
            throw new RuntimeException("Response from SMS Mock API is not correct");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Test(description = "Validate Success Auto M2B Notification")
    public void successAutoM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();
        WithdrawNotifyWalletSettlement withdrawNotify = sucessWalletWithrawNotify(auto,orderId);
        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms,Constants.NotificationStatus.SUCCESS.toString());
        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_AUTO_WITHDRAW_WALLET_SUCCESS_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Validate failure Auto M2B Notification")
    public void failureAutoM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = failureWalletWithrawNotify(auto,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms,Constants.NotificationStatus.FAIL.toString());
        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_AUTO_WITHDRAW_WALLET_FAIL_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertFailSmsText(expectedMobileNumber, txnAmount, orderId);
    }


    @Test(description = "Validate pending Auto M2B Notification")
    public void pendingAutoM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = pendingWalletWithrawNotify(auto,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms,Constants.NotificationStatus.PENDING.toString());


        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_AUTO_WITHDRAW_WALLET_PENDING_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Validate pending to Success Auto M2B Notification")
    public void pendingToSuccessAutoM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = pendingToSuccessWalletWithrawNotify(auto,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms,Constants.NotificationStatus.PENDING_TO_SUCCESS.toString());


        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_AUTO_WITHDRAW_WALLET_PENDING_TO_SUCCESS_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Validate pending to Failure Auto M2B Notification")
    public void pendingToFailureAutoM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = pendingToFailureWalletWithrawNotify(auto,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms,Constants.NotificationStatus.PENDING_TO_FAIL.toString());


        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_AUTO_WITHDRAW_WALLET_PENDING_TO_FAIL_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }








    @Test(description = "Validate Success Manual M2B Notification")
    public void successManualM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = sucessWalletWithrawNotify(manual,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms,Constants.NotificationStatus.SUCCESS.toString());
        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_WITHDRAW_WALLET_SUCCESS_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Validate failure Manual M2B Notification")
    public void failureManualM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = failureWalletWithrawNotify(manual,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms,Constants.NotificationStatus.FAIL.toString());
        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_WITHDRAW_WALLET_FAIL_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertFailSmsText(expectedMobileNumber, txnAmount, orderId);
    }


    @Test(description = "Validate pending Manual M2B Notification")
    public void pendingManualM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = pendingWalletWithrawNotify(manual,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms,Constants.NotificationStatus.PENDING.toString());


        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_WITHDRAW_WALLET_PENDING_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Validate pending to Success Manual M2B Notification")
    public void pendingToSuccessManualM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = pendingToSuccessWalletWithrawNotify(manual,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms,Constants.NotificationStatus.PENDING_TO_SUCCESS.toString());


        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_WITHDRAW_WALLET_PENDING_TO_SUCCESS_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Validate pending to Failure Manual M2B Notification")
    public void pendingToFailureManualM2BNotification() {
        String orderId = CommonHelpers.generateOrderId();

        WithdrawNotifyWalletSettlement withdrawNotify = pendingToFailureWalletWithrawNotify(manual,orderId);

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms,Constants.NotificationStatus.PENDING_TO_FAIL.toString());


        String outputMessage = ((List<HashMap<String, String>>)(notification.execute().jsonPath().get("notificationTemplateInfos")))
                .stream().filter(template ->
                        template.get("templateName")
                                .equals("SETTLEMENT_MERCHANT_WITHDRAW_WALLET_PENDING_TO_FAIL_SMS"))
                .map(map -> map.get("templateBody")).findAny().get();

        String expectedMessage = replaceAmountAndOrder(outputMessage, txnAmount, orderId);
        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }
}
