package scripts.bopanel;

import com.paytm.api.SMSPrimary;
import com.paytm.api.notification.GetNotificationTemplateMaster;
import com.paytm.api.notification.WithdrawNotify;
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
import java.util.NoSuchElementException;


@Owner("Tarun")
@Epic("failure-reasons")
@Feature("PGP-17977")
public class FailureReasonNotificationService extends PGPBaseTest {

    private static final String auto = "MERCHANT_AUTO_WITHDRAW";
    private static final String manual = "MERCHANT_WITHDRAW";
    private static final String online = "ONLINE_SETTLEMENT";
    private static final String sms = "SMS";
    private static final String accountNumber = "1234567890";
    private static final String txnAmount = "10000";//(isAmountInPaise = true)
    private final Constants.MerchantType merchantType = Constants.MerchantType.Hybrid;

    private String replaceAmountAndAccount(String templateMessage, String txnAmount, String accountNo, String errorCode) {

        if (null != templateMessage) {
            DecimalFormat df = new DecimalFormat("0.00");
            String actualTxnAmount = df.format(Integer.valueOf(txnAmount) / 100);
            String maskedAccountNumber = "**" + Integer.valueOf(accountNo) % 10000;
            return templateMessage
                    .replace("${TXN_AMOUNT}", actualTxnAmount)
                    .replace("${ACCOUNT_NO}", maskedAccountNumber);
        } else
            throw new NullPointerException("Empty/Null Response for errorCode: " + errorCode + " in NOTIFICATION_TEMPLATE_MASTER DB");

    }

    private void assertSMSAndText(String expectedMobileNumber, String expectedMessage, String orderId) {
        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        JsonPath smsJson;
        try {
            //SMS Mock
             smsJson= smsPrimary.executeUntilGetResponse().jsonPath();

            //Assertions on mobile number and message

            Assertions.assertThat(smsJson.getString("mobileNo")).as("SMS is going to wrong number").isEqualTo(expectedMobileNumber);
            Assertions.assertThat(smsJson.getString("message")).as("SMS text is not getting matched from NOTIFICATION_TEMPLATE_MASTER").isEqualTo(expectedMessage);
        }
        catch (JsonPathException e) {

            throw new NoSuchElementException("Did not get message in communication-gateway, please check comm-gateway service logs for orderId : " + orderId);
        } catch (NullPointerException e) {
            throw new RuntimeException("Response from SMS Mock API is not correct");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //---------------MERCHANT_AUTO_WITHDRAW-----------------------

    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_ACCOUNT_CLOSED'")
    public void accountClosedWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_CLOSED.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_INVALID_ACCOUNT_OR_IFSC'")
    public void invalidAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_INVALID_ACCOUNT_OR_IFSC.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_ACCOUNT_BLOCKED'")
    public void blockedAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_BLOCKED.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_ACCOUNT_NO'")
    public void nonOprationalAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_NO.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_AMOUNT_LIMIT_EXCEEDED'")
    public void limitExceedOprationalAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_AMOUNT_LIMIT_EXCEEDED.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_ACCOUNT_IS_NRE'")
    public void NREAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_IS_NRE.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_MERCHANT_IS_NRE_ACCOUNT'")
    public void merchantNREAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_MERCHANT_IS_NRE_ACCOUNT.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_INVALID_NBIN'")
    public void invalidBinAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_INVALID_NBIN.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'FGW_BANK_FAIL_SPECIFIC_REASON'")
    public void bankFailSpecificReasonAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_BANK_FAIL_SPECIFIC_REASON.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code : 'DEFAULT' with NotificationStatus as success")
    public void defaultAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.DEFAULT.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(auto, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api automatic for Bank Result Code other than insta error codes")
    public void incorrectAccountWithdrawNotify() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.OTHERS.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(auto)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();
        //Default message in case incorrect error code is provided
        String outputMessage = "Settlement of Rs. ${TXN_AMOUNT} in your a/c ${ACCOUNT_NO} has been declined by your bank. It will be attempted again tomorrow. Open pytm.biz/Settlement to transfer the amount today";

        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    //-----------------------------MERCHANT_WITHDRAW---------------------

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_ACCOUNT_CLOSED")
    public void accountClosedWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_CLOSED.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_INVALID_ACCOUNT_OR_IFSC")
    public void invalidAccountClosedWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_INVALID_ACCOUNT_OR_IFSC.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_ACCOUNT_BLOCKED")
    public void blockedAccountClosedWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_BLOCKED.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_ACCOUNT_NO")
    public void accountWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_NO.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_AMOUNT_LIMIT_EXCEEDED")
    public void limitExceedWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_AMOUNT_LIMIT_EXCEEDED.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_ACCOUNT_IS_NRE")
    public void nreExceedWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_IS_NRE.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_INVALID_NBIN")
    public void invalidNBinExceedWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_INVALID_NBIN.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = FGW_BANK_FAIL_SPECIFIC_REASON")
    public void bankFailExceedWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_BANK_FAIL_SPECIFIC_REASON.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = Default")
    public void defaultWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.DEFAULT.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(manual, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${REF_ID}", orderId);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api manual for BankResultCode = OTHERS")
    public void othersWithdrawNotifyManual() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.OTHERS.toString();

        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(manual)
                .setAccount(accountNumber)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        //Calling SMS mock
        //Default message should not be sent

        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        JsonPath smsJson = smsPrimary.execute().jsonPath();
        Assertions.assertThat(smsJson.getString("mobileNo")).as("SMS should not be sent").isEqualTo("No mobile number found");

    }
    //--------------------Online Settlement-------------------------

    @Test(description = "Hit withdrawNotify Api for Online Settlement for bankResultCode : FGW_INVALID_ACCOUNT_OR_IFSC")
    public void onlineSettlementWithdrawNotifyInvalidAccountIFSC() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_INVALID_ACCOUNT_OR_IFSC.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api for Online Settlement for FGW_ACCOUNT_CLOSED")
    public void onlineSettlementWithdrawNotifyClosed() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_CLOSED.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api for Online Settlement for bankResult Code : FGW_ACCOUNT_BLOCKED")
    public void onlineSettlementWithdrawNotifyInvalidAccount() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_BLOCKED.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api for Online Settlement for FGW_ACCOUNT_NO")
    public void onlineSettlementWithdrawNotifyBlocked() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_NO.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api for Online Settlement for FGW_AMOUNT_LIMIT_EXCEEDED")
    public void onlineSettlementWithdrawNotifyAccount() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_AMOUNT_LIMIT_EXCEEDED.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api for Online Settlement for FGW_ACCOUNT_IS_NRE")
    public void onlineSettlementWithdrawNotifyNRE() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_ACCOUNT_IS_NRE.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }


    @Test(description = "Hit withdrawNotify Api for Online Settlement for FGW_MERCHANT_IS_NRE_ACCOUNT")
    public void onlineSettlementWithdrawNotifyNREAccount() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_MERCHANT_IS_NRE_ACCOUNT.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api for Online Settlement for FGW_INVALID_NBIN")
    public void onlineSettlementWithdrawNotifyInvalidNREAccount() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_INVALID_NBIN.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api for Online Settlement for FGW_BANK_FAIL_SPECIFIC_REASON")
    public void onlineSettlementWithdrawNotifyBankFailAccount() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.FGW_BANK_FAIL_SPECIFIC_REASON.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api for Online Settlement for DEFAULT")
    public void onlineSettlementWithdrawNotifyBankdefaultAccount() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.DEFAULT.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        GetNotificationTemplateMaster notification = new GetNotificationTemplateMaster(online, sms, errorCode, Constants.NotificationStatus.FAIL.toString());
        String outputMessage = notification.execute().jsonPath().getString("notificationTemplateInfos[0].templateBody");
        String expectedMessage = replaceAmountAndAccount(outputMessage, txnAmount, accountNumber, errorCode).replace("${BANK_NAME}", bankName);

        assertSMSAndText(expectedMobileNumber, expectedMessage, orderId);
    }

    @Test(description = "Hit withdrawNotify Api for Online Settlement for OTHERS")
    public void onlineSettlementWithdrawNotifyOthersAccount() {
        String orderId = CommonHelpers.generateOrderId();
        String errorCode = Constants.InstErrorCode.OTHERS.toString();
        String bankName = "Axis Bank";
        WithdrawNotify withdrawNotify = new WithdrawNotify.Builder(merchantType.getId(), orderId)
                .setFundAmount(txnAmount)
                .setInstErrorCode(errorCode)
                .setOperation(online)
                .setAccount(accountNumber)
                .setBankName(bankName)
                .build();

        JsonPath withDrawJson = withdrawNotify.execute().jsonPath();
        //Assertion 1 : Withdraw API should be a success
        Assertions.assertThat(withDrawJson.getString("response.body.resultInfo.resultStatus")).as("Withdraw API failed").isEqualTo("S");

        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(merchantType.getId());
        String expectedMobileNumber = getMerchExtndInfoHelper.fetchMobileNumber();

        //Calling SMS mock
        //Default message should not be sent

        SMSPrimary smsPrimary = new SMSPrimary(orderId);
        JsonPath smsJson = smsPrimary.execute().jsonPath();
        Assertions.assertThat(smsJson.getString("mobileNo")).as("SMS should not be sent").isEqualTo("No mobile number found");


    }
}
