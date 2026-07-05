package scripts.notification;

import com.paytm.api.notification.directNQHNotify.SettlementNotify;
import com.paytm.api.subscription.CheckStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.api.notification.RealtimeMerchantNotify;

import java.util.ArrayList;

import static com.paytm.appconstants.Constants.Owner.*;
import static com.paytm.apphelpers.LogsValidationHelper.verifyLogsOnPod;

public class NotificationTemplate {

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_EXCEEDED NQH templateBody")
    public void SMS_FGW_AMOUNT_LIMIT_EXCEEDED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_EXCEEDED";
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} " +
                "in your A/C ${ACCOUNT_NO} failed " +
                "as payment limit set by your ${BANK_NAME} has exceeded. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg  Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_EXCEEDED NQH DLTtemplateID")
    public void SMS_FGW_AMOUNT_LIMIT_EXCEEDED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300002");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_EXCEEDED CG SMSInfo")
    public void SMS_FGW_AMOUNT_LIMIT_EXCEEDED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId());

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as payment limit set by your STATE BANK OF INDIA has exceeded. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg  Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_LIMIT_EXCEEDED NQH templateBody")
    public void FGW_ACCOUNT_LIMIT_EXCEEDED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();
        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} " +
                "in your A/C ${ACCOUNT_NO} failed " +
                "as payment limit set by your ${BANK_NAME} has exceeded. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg  Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_LIMIT_EXCEEDED NQH DLTtemplateID")
    public void FGW_ACCOUNT_LIMIT_EXCEEDED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300002");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_LIMIT_EXCEEDED CG SMSInfo")
    public void FGW_ACCOUNT_LIMIT_EXCEEDED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId());

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as payment limit set by your STATE BANK OF INDIA has exceeded. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg  Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN NQH templateBody")
    public void FGW_AMOUNT_LIMIT_NO_PAN_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_LIMIT_NO_PAN");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} " +
                "in your A/C ${ACCOUNT_NO} failed " +
                "as limit set by your ${BANK_NAME} has exceeded. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please update your PAN with your Bank or change bank account " +
                "by clicking  on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN NQH DLTTemplateID")
    public void FGW_AMOUNT_LIMIT_NO_PAN_NQHDLTTemplateIID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_LIMIT_NO_PAN");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300003");
     }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN CG SMSInfo")
    public void FGW_AMOUNT_LIMIT_NO_PAN_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");
        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as limit set by your STATE BANK OF INDIA has exceeded. Don't worry, your money is safe with us. To get your settlement, please update your PAN with your Bank or change bank account by clicking  on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN NQH templateBody")
    public void FGW_PPBL_BC_ACCOUNT_REJECTION_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_BC_ACCOUNT_REJECTION";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_BC_ACCOUNT_REJECTION");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as this account is closed by your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN NQH DLTtemplateID")
    public void FGW_PPBL_BC_ACCOUNT_REJECTION_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_BC_ACCOUNT_REJECTION";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_BC_ACCOUNT_REJECTION");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300004");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN CG SMSInfo")
    public void FGW_PPBL_BC_ACCOUNT_REJECTION_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as limit set by your STATE BANK OF INDIA has exceeded. Don't worry, your money is safe with us. To get your settlement, please update your PAN with your Bank or change bank account by clicking  on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_CLOSED NQH templateBody")
    public void FGW_ACCOUNT_CLOSED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} " +
                "failed as this account is closed by your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_CLOSED NQH DLTtemplateID")
    public void FGW_ACCOUNT_CLOSED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300005");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_CLOSED CG SMSInfo")
    public void FGW_ACCOUNT_CLOSED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as this account is closed by your STATE BANK OF INDIA. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_CLOSED NQH templateBody")
    public void FGW_PPBL_ACCOUNT_CLOSED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_CLOSED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as this account is closed by your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_CLOSED NQH DLTtemplateID")
    public void FGW_PPBL_ACCOUNT_CLOSED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_CLOSED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300005");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_CLOSED CG SMSInfo")
    public void FGW_PPBL_ACCOUNT_CLOSED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as this account is closed by your STATE BANK OF INDIA. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :WALLET_NOT_ACTIVATED NQH templateBody")
    public void WALLET_NOT_ACTIVATED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "WALLET_NOT_ACTIVATED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='WALLET_NOT_ACTIVATED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as  your ${BANK_NAME} is inactive. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "Please activate your paytm wallet by clicking on m.paytm.me/KYC_nonbank Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :WALLET_NOT_ACTIVATED NQH DLTtemplateID")
    public void WALLET_NOT_ACTIVATED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "WALLET_NOT_ACTIVATED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='WALLET_NOT_ACTIVATED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300006");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :WALLET_NOT_ACTIVATED CG SMSInfo")
    public void WALLET_NOT_ACTIVATED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "WALLET_NOT_ACTIVATED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as  your STATE BANK OF INDIA is inactive. Don't worry, your money is safe with us. To get your settlement, Please activate your paytm wallet by clicking on m.paytm.me/KYC_nonbank Team Paytm \"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_INVALID_ACCOUNT_OR_IFSC NQH templateBody")
    public void FGW_INVALID_ACCOUNT_OR_IFSC_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_INVALID_ACCOUNT_OR_IFSC";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_INVALID_ACCOUNT_OR_IFSC");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as your ${BANK_NAME} Account No./IFSC details are incorrect. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please update correct bank A/C by clicking on pytm.biz/acpymtstg  eam Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_INVALID_ACCOUNT_OR_IFSC NQH DLTtemplateID")
    public void FGW_INVALID_ACCOUNT_OR_IFSC_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_INVALID_ACCOUNT_OR_IFSC";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_INVALID_ACCOUNT_OR_IFSC");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300007");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_INVALID_ACCOUNT_OR_IFSC CG SMSInfo")
    public void FGW_INVALID_ACCOUNT_OR_IFSC_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_INVALID_ACCOUNT_OR_IFSC";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as your STATE BANK OF INDIA Account No./IFSC details are incorrect. Don't worry, your money is safe with us. To get your settlement, please update correct bank A/C by clicking on pytm.biz/acpymtstg  eam Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN NQH templateBody")
    public void FGW_ACCOUNT_FROZEN_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_FROZEN");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as this account is frozen by your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN NQH DLTtemplateID")
    public void FGW_ACCOUNT_FROZEN_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_FROZEN");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300008");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN CG SMSInfo")
    public void FGW_ACCOUNT_FROZEN_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as this account is frozen by your STATE BANK OF INDIA. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_FROZEN NQH templateBody")
    public void FGW_PPBL_ACCOUNT_FROZEN_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_FROZEN");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as this account is frozen by your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_FROZEN NQH DLTtemplateID")
    public void FGW_PPBL_ACCOUNT_FROZEN_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_FROZEN");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300008");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_FROZEN CG SMSInfo")
    public void FGW_PPBL_ACCOUNT_FROZEN_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as this account is frozen by your STATE BANK OF INDIA. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_WALLET_FROZEN NQH templateBody")
    public void FGW_WALLET_FROZEN_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_WALLET_FROZEN");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as  your ${BANK_NAME} - A/C is deactivated. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please call Paytm at 0120-4456456 (or) change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_WALLET_FROZEN NQH DLTtemplateID")
    public void FGW_WALLET_FROZEN_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_WALLET_FROZEN");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300009");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_WALLET_FROZEN CG SMSInfo")
    public void FGW_WALLET_FROZEN_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as  your STATE BANK OF INDIA - A/C is deactivated. Don't worry, your money is safe with us. To get your settlement, please call Paytm at 0120-4456456 (or) change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_DORMANT NQH templateBody")
    public void FGW_ACCOUNT_DORMANT_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_DORMANT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_DORMANT");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as your ${BANK_NAME} account is inactive. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_DORMANT NQH DLTtemplateID")
    public void FGW_ACCOUNT_DORMANT_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_DORMANT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_DORMANT");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300010");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_DORMANT CG SMSInfo")
    public void FGW_ACCOUNT_DORMANT_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as  your STATE BANK OF INDIA - A/C is deactivated. Don't worry, your money is safe with us. To get your settlement, please call Paytm at 0120-4456456 (or) change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_DORMANT NQH templateBody")
    public void FGW_PPBL_ACCOUNT_DORMANT_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_DORMANT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_DORMANT");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as your ${BANK_NAME} account is inactive. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_DORMANT NQH DLTtemplateID")
    public void FGW_PPBL_ACCOUNT_DORMANT_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_DORMANT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_DORMANT");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300010");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_DORMANT CG SMSInfo")
    public void FGW_PPBL_ACCOUNT_DORMANT_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_DORMANT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as your STATE BANK OF INDIA account is inactive. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_AMOUNT_LIMIT_NO_PAN NQH templateBody")
    public void FGW_PPBL_AMOUNT_LIMIT_NO_PAN_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_AMOUNT_LIMIT_NO_PAN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_AMOUNT_LIMIT_NO_PAN");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as limit set by your ${BANK_NAME} has exceeded. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please update your PAN with your Bank or change bank account by clicking  on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_AMOUNT_LIMIT_NO_PAN NQH DLTtemplateID")
    public void FGW_PPBL_AMOUNT_LIMIT_NO_PAN_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_AMOUNT_LIMIT_NO_PAN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_AMOUNT_LIMIT_NO_PAN");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107169227489372045");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_AMOUNT_LIMIT_NO_PAN CG SMSInfo")
    public void FGW_PPBL_AMOUNT_LIMIT_NO_PAN_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_AMOUNT_LIMIT_NO_PAN";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as limit set by your STATE BANK OF INDIA has exceeded. Don't worry, your money is safe with us. To get your settlement, please update your PAN with your Bank or change bank account by clicking  on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED NQH templateBody")
    public void MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as you have exceeded limit of your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please transfer money from your Paytm wallet or change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED NQH DLTtemplateID")
    public void MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300012");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED CG SMSInfo")
    public void MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "MONTHLY_AMOUNT_BALANCE_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as you have exceeded limit of your STATE BANK OF INDIA. Don't worry, your money is safe with us. To get your settlement, please transfer money from your Paytm wallet or change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_WALLET_LIMIT_EXCEEDED NQH templateBody")
    public void FGW_AMOUNT_WALLET_LIMIT_EXCEEDED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_WALLET_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_WALLET_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as you have exceeded limit of your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please transfer money from your Paytm wallet or change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_WALLET_LIMIT_EXCEEDED NQH DLTtemplateID")
    public void FGW_AMOUNT_WALLET_LIMIT_EXCEEDED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_WALLET_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_WALLET_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300012");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_WALLET_LIMIT_EXCEEDED CG SMSInfo")
    public void FGW_AMOUNT_WALLET_LIMIT_EXCEEDED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_WALLET_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as you have exceeded limit of your STATE BANK OF INDIA. Don't worry, your money is safe with us. To get your settlement, please transfer money from your Paytm wallet or change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_AMOUNT_LIMIT_EXCEEDED NQH templateBody")
    public void FGW_PPBL_AMOUNT_LIMIT_EXCEEDED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_AMOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_AMOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as payment limit set by your ${BANK_NAME} has exceeded. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "Please call Paytm at 0120-4456456 to upgrade your bank account (or) Change your bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_AMOUNT_LIMIT_EXCEEDED NQH DLTtemplateID")
    public void FGW_PPBL_AMOUNT_LIMIT_EXCEEDED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_AMOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_AMOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300013");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_AMOUNT_LIMIT_EXCEEDED CG SMSInfo")
    public void FGW_PPBL_AMOUNT_LIMIT_EXCEEDED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_AMOUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as payment limit set by your STATE BANK OF INDIA has exceeded. Don't worry, your money is safe with us. To get your settlement, Please call Paytm at 0120-4456456 to upgrade your bank account (or) Change your bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED NQH templateBody")
    public void FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as payment limit set by your ${BANK_NAME} has exceeded. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, Please call Paytm at 0120-4456456 " +
                "to upgrade your bank account (or) Change your bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED NQH DLTtemplateID")
    public void FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300013");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED CG SMSInfo")
    public void FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_CREDIT_COUNT_LIMIT_EXCEEDED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as payment limit set by your STATE BANK OF INDIA has exceeded. Don't worry, your money is safe with us. To get your settlement, Please call Paytm at 0120-4456456 to upgrade your bank account (or) Change your bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_BLOCKED NQH templateBody")
    public void FGW_ACCOUNT_BLOCKED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_BLOCKED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_BLOCKED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as this account is blocked by your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_BLOCKED NQH DLTtemplateID")
    public void FGW_ACCOUNT_BLOCKED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_BLOCKED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_BLOCKED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300014");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_BLOCKED CG SMSInfo")
    public void FGW_ACCOUNT_BLOCKED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_BLOCKED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as this account is blocked by your STATE BANK OF INDIA. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_MERCHANT_IS_NRE_ACCOUNT NQH templateBody")
    public void FGW_MERCHANT_IS_NRE_ACCOUNT_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_MERCHANT_IS_NRE_ACCOUNT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_MERCHANT_IS_NRE_ACCOUNT");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "as  Indian currency is not accepted in your ${BANK_NAME} account. " +
                "Don't worry, your money is safe with us. " +
                "To get your settlement, " +
                "please change bank account by clicking on pytm.biz/acpymtstg Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_MERCHANT_IS_NRE_ACCOUNT NQH DLTtemplateID")
    public void FGW_MERCHANT_IS_NRE_ACCOUNT_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_MERCHANT_IS_NRE_ACCOUNT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_MERCHANT_IS_NRE_ACCOUNT");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300015");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_MERCHANT_IS_NRE_ACCOUNT CG SMSInfo")
    public void FGW_MERCHANT_IS_NRE_ACCOUNT_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_MERCHANT_IS_NRE_ACCOUNT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed as  Indian currency is not accepted in your STATE BANK OF INDIA account. Don't worry, your money is safe with us. To get your settlement, please change bank account by clicking on pytm.biz/acpymtstg Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PAYOUT_REJECTED NQH templateBody")
    public void FGW_PAYOUT_REJECTED_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PAYOUT_REJECTED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PAYOUT_REJECTED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue with your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PAYOUT_REJECTED NQH DLTtemplateID")
    public void FGW_PAYOUT_REJECTED_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PAYOUT_REJECTED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PAYOUT_REJECTED");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300016");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PAYOUT_REJECTED CG SMSInfo")
    public void FGW_PAYOUT_REJECTED_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PAYOUT_REJECTED";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue with your STATE BANK OF INDIA. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_REMITTER_BANK_FAIL_REASON NQH templateBody")
    public void FGW_REMITTER_BANK_FAIL_REASON_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_REMITTER_BANK_FAIL_REASON";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_REMITTER_BANK_FAIL_REASON");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue with your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_REMITTER_BANK_FAIL_REASON NQH DLTtemplateID")
    public void FGW_REMITTER_BANK_FAIL_REASON_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_REMITTER_BANK_FAIL_REASON";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_REMITTER_BANK_FAIL_REASON");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300016");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_REMITTER_BANK_FAIL_REASON CG SMSInfo")
    public void FGW_REMITTER_BANK_FAIL_REASON_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_REMITTER_BANK_FAIL_REASON";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue with your STATE BANK OF INDIA. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_BANK_FAIL_SPECIFIC_REASON NQH templateBody")
    public void FGW_BANK_FAIL_SPECIFIC_REASON_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_BANK_FAIL_SPECIFIC_REASON";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_BANK_FAIL_SPECIFIC_REASON");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue with your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_BANK_FAIL_SPECIFIC_REASON NQH DLTtemplateID")
    public void FGW_BANK_FAIL_SPECIFIC_REASON_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_BANK_FAIL_SPECIFIC_REASON";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_BANK_FAIL_SPECIFIC_REASON");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300016");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_BANK_FAIL_SPECIFIC_REASON CG SMSInfo")
    public void FGW_BANK_FAIL_SPECIFIC_REASON_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_BANK_FAIL_SPECIFIC_REASON";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue with your STATE BANK OF INDIA. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :NO_CONNECTIVITY_NPCI NQH templateBody")
    public void NO_CONNECTIVITY_NPCI_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "NO_CONNECTIVITY_NPCI";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='NO_CONNECTIVITY_NPCI");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue with your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :NO_CONNECTIVITY_NPCI NQH DLTtemplateID")
    public void NO_CONNECTIVITY_NPCI_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "NO_CONNECTIVITY_NPCI";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='NO_CONNECTIVITY_NPCI");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300016");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :NO_CONNECTIVITY_NPCI CG SMSInfo")
    public void NO_CONNECTIVITY_NPCI_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "NO_CONNECTIVITY_NPCI";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue with your STATE BANK OF INDIA. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_CONNECT_TIMEOUT_WITH_BANK NQH templateBody")
    public void FGW_CONNECT_TIMEOUT_WITH_BANK_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_CONNECT_TIMEOUT_WITH_BANK";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_CONNECT_TIMEOUT_WITH_BANK");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue with your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_CONNECT_TIMEOUT_WITH_BANK NQH DLTtemplateID")
    public void FGW_CONNECT_TIMEOUT_WITH_BANK_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_CONNECT_TIMEOUT_WITH_BANK";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_CONNECT_TIMEOUT_WITH_BANK");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300016");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_CONNECT_TIMEOUT_WITH_BANK CG SMSInfo")
    public void FGW_CONNECT_TIMEOUT_WITH_BANK_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_CONNECT_TIMEOUT_WITH_BANK";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue with your STATE BANK OF INDIA. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_UNREGISTERED_UPI_PSP NQH templateBody")
    public void FGW_UNREGISTERED_UPI_PSP_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_UNREGISTERED_UPI_PSP";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_UNREGISTERED_UPI_PSP");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue with your ${BANK_NAME}. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_UNREGISTERED_UPI_PSP NQH DLTtemplateID")
    public void FGW_UNREGISTERED_UPI_PSP_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_UNREGISTERED_UPI_PSP";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_UNREGISTERED_UPI_PSP");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107900000325300016");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_UNREGISTERED_UPI_PSP CG SMSInfo")
    public void FGW_UNREGISTERED_UPI_PSP_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_UNREGISTERED_UPI_PSP";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue with your STATE BANK OF INDIA. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ORDER_NOT_FOUND NQH templateBody")
    public void FGW_ORDER_NOT_FOUND_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ORDER_NOT_FOUND";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ORDER_NOT_FOUND");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed due to technical issue. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ORDER_NOT_FOUND NQH DLTtemplateID")
    public void FGW_ORDER_NOT_FOUND_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ORDER_NOT_FOUND";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ORDER_NOT_FOUND");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107169227491858974");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ORDER_NOT_FOUND CG SMSInfo")
    public void FGW_ORDER_NOT_FOUND_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ORDER_NOT_FOUND";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"dltTemplateId\":\"1107169227491858974\"");
        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\",\"phoneNo\"");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_BENE_NOT_EXIST NQH templateBody")
    public void FGW_BENE_NOT_EXIST_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_BENE_NOT_EXIST";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_BENE_NOT_EXIST");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue. " +
                "Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_BENE_NOT_EXIST NQH DLTtemplateID")
    public void FGW_BENE_NOT_EXIST_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_BENE_NOT_EXIST";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_BENE_NOT_EXIST");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107169227491858974");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_BENE_NOT_EXIST CG SMSInfo")
    public void FGW_BENE_NOT_EXIST_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_BENE_NOT_EXIST";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"dltTemplateId\":\"1107169227491858974\"");
        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :DEFAULT NQH templateBody")
    public void DEFAULT_NQHTemplate() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "DEFAULT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='DEFAULT");
        Assertions.assertThat(nqhLogs).contains("templateBody='Dear Merchant, " +
                "settlement of Rs ${TXN_AMOUNT} in your A/C ${ACCOUNT_NO} failed " +
                "due to technical issue. Don't worry, your money is safe with us. " +
                "We are retrying it & you should receive the money within next 24 hours. " +
                "Check your settlement update on pytm.biz/Settlement Team Paytm");
    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :DEFAULT NQH DLTtemplateID")
    public void DEFAULT_NQHDLTTemplateID() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "DEFAULT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='DEFAULT");
        Assertions.assertThat(nqhLogs).contains("dltTemplateId='1107169227491858974");

    }

    @Owner(POOJA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :DEFAULT CG SMSInfo")
    public void DEFAULT_CGData() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "DEFAULT";
        channel.add("SMS");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setChannel(channel)
                .setErrorCode(errorCode)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, "dltTemplateId");

        Assertions.assertThat(CGLogs).contains("\"dltTemplateId\":\"1107169227491858974\"");
        Assertions.assertThat(CGLogs).contains("\"msg\":\"Dear Merchant, settlement of Rs 20.00 in your A/C 1330********3383 failed due to technical issue. Don't worry, your money is safe with us. We are retrying it & you should receive the money within next 24 hours. Check your settlement update on pytm.biz/Settlement Team Paytm\"");
    }


    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_LIMIT_EXCEEDED NQH templateBody")
    public void FGW_ACCOUNT_LIMIT_EXCEEDED_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_LIMIT_EXCEEDED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_LIMIT_EXCEEDED NQH templateBody")
    public void FGW_ACCOUNT_LIMIT_EXCEEDED_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_LIMIT_EXCEEDED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_LIMIT_EXCEEDED NQH templateBody")
    public void FGW_ACCOUNT_LIMIT_EXCEEDED_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_LIMIT_EXCEEDED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_LIMIT_EXCEEDED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as you have exceeded the payment limit set by your bank.nn? Don't worry, your money is safe with us.nn? To get your settlement, click here to change bank A/C'");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_LIMIT_EXCEEDED CG SMSInfo")
    public void FGW_ACCOUNT_LIMIT_EXCEEDED_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_LIMIT_EXCEEDED";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as you have exceeded the payment limit set by your bank.nn? Don't worry, your money is safe with us.nn? To get your settlement, click here to change bank A/C\"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN NQH templateBody")
    public void FGW_AMOUNT_LIMIT_NO_PAN_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_LIMIT_NO_PAN");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN NQH templateBody")
    public void FGW_AMOUNT_LIMIT_NO_PAN_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_LIMIT_NO_PAN");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN NQH templateBody")
    public void FGW_AMOUNT_LIMIT_NO_PAN_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_AMOUNT_LIMIT_NO_PAN");
        Assertions.assertThat(nqhLogs).contains(" templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C XX5678XX has failed, as you have exceeded the payment limit. Don?t worry, your money is safe with us. ? To get your settlement - ? ? Please update PAN with your bank or? Click here to change bank A/C '");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_AMOUNT_LIMIT_NO_PAN CG SMSInfo")
    public void FGW_AMOUNT_LIMIT_NO_PAN_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_AMOUNT_LIMIT_NO_PAN";
        channel.add("PUSH");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C XX5678XX has failed, as you have exceeded the payment limit. Don?t worry, your money is safe with us. ? To get your settlement - ? ? Please update PAN with your bank or? Click here to change bank A/C \"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :DEFAULT NQH templateBody")
    public void DEFAULT_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "DEFAULT";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='DEFAULT");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :DEFAULT")
    public void DEFAULT_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "DEFAULT";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='DEFAULT");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :DEFAULT NQH templateBody")
    public void DEFAULT_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "DEFAULT";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='DEFAULT");
        Assertions.assertThat(nqhLogs).contains(" templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, due to technical issue.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n?To check your settlement, click here.");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :DEFAULT CG SMSInfo")
    public void DEFAULT_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "DEFAULT";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, due to technical issue.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n?To check your settlement, click here.\"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ORDER_NOT_FOUND NQH templateBody")
    public void FGW_ORDER_NOT_FOUND_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ORDER_NOT_FOUND";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ORDER_NOT_FOUND");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ORDER_NOT_FOUND")
    public void FGW_ORDER_NOT_FOUND_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ORDER_NOT_FOUND";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ORDER_NOT_FOUND");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ORDER_NOT_FOUND NQH templateBody")
    public void FGW_ORDER_NOT_FOUND_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ORDER_NOT_FOUND";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ORDER_NOT_FOUND");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, due to technical issue.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n?To check your settlement, click here.'");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ORDER_NOT_FOUND CG SMSInfo")
    public void FGW_ORDER_NOT_FOUND_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ORDER_NOT_FOUND";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, due to technical issue.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n?To check your settlement, click here.\"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PAYOUT_REJECTED NQH templateBody")
    public void FGW_PAYOUT_REJECTED_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PAYOUT_REJECTED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PAYOUT_REJECTED");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PAYOUT_REJECTED")
    public void FGW_PAYOUT_REJECTED_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PAYOUT_REJECTED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PAYOUT_REJECTED");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PAYOUT_REJECTED NQH templateBody")
    public void FGW_PAYOUT_REJECTED_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PAYOUT_REJECTED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PAYOUT_REJECTED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, due to technical issue with your bank.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n?To check your settlement, click here.'");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PAYOUT_REJECTED CG SMSInfo")
    public void FGW_PAYOUT_REJECTED_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PAYOUT_REJECTED";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, due to technical issue with your bank.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n?To check your settlement, click here.\"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_RISK_REJECTED NQH templateBody")
    public void FGW_PPBL_RISK_REJECTED_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_RISK_REJECTED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_RISK_REJECTED");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_RISK_REJECTED")
    public void FGW_PPBL_RISK_REJECTED_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_RISK_REJECTED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_RISK_REJECTED");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_RISK_REJECTED NQH templateBody")
    public void FGW_PPBL_RISK_REJECTED_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_RISK_REJECTED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_RISK_REJECTED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as your bank declined the transaction. " +
                "? Don?t worry, your money is safe with us. " +
                "? To get your settlement, click here to change bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_RISK_REJECTED CG SMSInfo")
    public void FGW_PPBL_RISK_REJECTED_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_RISK_REJECTED";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as your bank declined the transaction. ? Don?t worry, your money is safe with us. ? To get your settlement, click here to change bank A/C\"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_BC_ACCOUNT_REJECTION NQH templateBody")
    public void FGW_PPBL_BC_ACCOUNT_REJECTION_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_BC_ACCOUNT_REJECTION";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_BC_ACCOUNT_REJECTION");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_BC_ACCOUNT_REJECTION")
    public void FGW_PPBL_BC_ACCOUNT_REJECTION_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_BC_ACCOUNT_REJECTION";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_BC_ACCOUNT_REJECTION");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_BC_ACCOUNT_REJECTION NQH templateBody")
    public void FGW_PPBL_BC_ACCOUNT_REJECTION_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_BC_ACCOUNT_REJECTION";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_BC_ACCOUNT_REJECTION");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as your bank declined the transaction. ? Don?t worry, your money is safe with us. ? To get your settlement, click here to change bank A/C '");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_BC_ACCOUNT_REJECTION CG SMSInfo")
    public void FGW_PPBL_BC_ACCOUNT_REJECTION_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_BC_ACCOUNT_REJECTION";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as your bank declined the transaction. ? Don?t worry, your money is safe with us. ? To get your settlement, click here to change bank A/C \"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_CLOSED NQH templateBody")
    public void FGW_ACCOUNT_CLOSED_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_CLOSED")
    public void FGW_ACCOUNT_CLOSED_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_CLOSED NQH templateBody")
    public void FGW_ACCOUNT_CLOSED_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, " +
                "as your account was closed by your bank? " +
                "Don?t worry, your money is safe with us. " +
                "? To get your settlement, " +
                "click here to change bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_CLOSED CG SMSInfo")
    public void FGW_ACCOUNT_CLOSED_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_CLOSED";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as your account was closed by your bank? Don?t worry, your money is safe with us. ? To get your settlement, click here to change bank A/C\"");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :WALLET_NOT_ACTIVATED NQH templateBody")
    public void WALLET_NOT_ACTIVATED_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "WALLET_NOT_ACTIVATED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='WALLET_NOT_ACTIVATED");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :WALLET_NOT_ACTIVATED")
    public void WALLET_NOT_ACTIVATED_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "WALLET_NOT_ACTIVATED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='WALLET_NOT_ACTIVATED");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Activate Wallet");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :WALLET_NOT_ACTIVATED NQH templateBody")
    public void WALLET_NOT_ACTIVATED_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "WALLET_NOT_ACTIVATED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='WALLET_NOT_ACTIVATED");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as your Paytm wallet is inactive.nn? Don?t worry, your money is safe with us.nn? To get your settlement, please activate your wallet by clicking here'");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :WALLET_NOT_ACTIVATED CG SMSInfo")
    public void WALLET_NOT_ACTIVATED_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "WALLET_NOT_ACTIVATED";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Activate Wallet\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as your Paytm wallet is inactive.nn? Don?t worry, your money is safe with us.nn? To get your settlement, please activate your wallet by clicking here");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_INVALID_ACCOUNT_OR_IFSC NQH templateBody")
    public void FGW_INVALID_ACCOUNT_OR_IFSC_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_INVALID_ACCOUNT_OR_IFSC";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_INVALID_ACCOUNT_OR_IFSC");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_INVALID_ACCOUNT_OR_IFSC")
    public void FGW_INVALID_ACCOUNT_OR_IFSC_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_INVALID_ACCOUNT_OR_IFSC";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_INVALID_ACCOUNT_OR_IFSC");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Update Correct Bank A/C");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_INVALID_ACCOUNT_OR_IFSC NQH templateBody")
    public void FGW_INVALID_ACCOUNT_OR_IFSC_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_INVALID_ACCOUNT_OR_IFSC";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_INVALID_ACCOUNT_OR_IFSC");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as your Bank Account No./IFSC details are incorrect. nn? Don't worry, your money is safe with us.nn? To get your settlement, please correct your bank account by clicking here'");
    }

    @Owner(RONIKA)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_INVALID_ACCOUNT_OR_IFSC CG SMSInfo")
    public void FGW_INVALID_ACCOUNT_OR_IFSC_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_INVALID_ACCOUNT_OR_IFSC";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Update Correct Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as your Bank Account No./IFSC details are incorrect. nn? Don't worry, your money is safe with us.nn? To get your settlement, please correct your bank account by clicking here\"");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN NQH templateBody")
    public void FGW_ACCOUNT_FROZEN_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_FROZEN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_FROZEN");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN")
    public void FGW_ACCOUNT_FROZEN_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_FROZEN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_FROZEN");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN NQH templateBody")
    public void FGW_ACCOUNT_FROZEN_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_FROZEN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_ACCOUNT_FROZEN");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as your account was frozen by your bank.nn? Don't worry, your money is safe with us.nn? To get your settlement, click here to change bank A/C'");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN CG SMSInfo")
    public void FGW_ACCOUNT_FROZEN_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_ACCOUNT_FROZEN";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as your account was frozen by your bank.nn? Don't worry, your money is safe with us.nn? To get your settlement, click here to change bank A/C\"");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_DORMANT NQH templateBody")
    public void FGW_PPBL_ACCOUNT_DORMANT_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_DORMANT";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_DORMANT");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_DORMANT")
    public void FGW_PPBL_ACCOUNT_DORMANT_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_DORMANT";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_DORMANT");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_DORMANT NQH templateBody")
    public void FGW_PPBL_ACCOUNT_DORMANT_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_DORMANT";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_DORMANT");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as your bank account is inactive.  nn? Don't worry, your money is safe with us.nn? To get your settlement, click here to change bank A/C'");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_ACCOUNT_FROZEN CG SMSInfo")
    public void FGW_PPBL_ACCOUNT_DORMANT_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_DORMANT";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, as your bank account is inactive.  nn? Don't worry, your money is safe with us.nn? To get your settlement, click here to change bank A/C\"");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_WALLET_FROZEN NQH templateBody")
    public void FGW_WALLET_FROZEN_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_WALLET_FROZEN");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_WALLET_FROZEN")
    public void FGW_WALLET_FROZEN_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_WALLET_FROZEN");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_WALLET_FROZEN NQH templateBody")
    public void FGW_WALLET_FROZEN_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_WALLET_FROZEN");
        Assertions.assertThat(nqhLogs).contains("templateBody='Your settlements Rs. ${TXN_AMOUNT} on Paytm failed as your ${BANK_NAME} - A/C is deactivated.Don?t worry, your money is safe with us. ? To get your settlement- ??Call Paytm at 0120-4456456 or ? Click here to add a new bank A/C ?'");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_WALLET_FROZEN CG SMSInfo")
    public void FGW_WALLET_FROZEN_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_WALLET_FROZEN";
        channel.add("PUSH");
/*        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify*/
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();


        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined | Change Bank A/C\",\"message\":\"Your settlements Rs. 20.00 on Paytm failed as your STATE BANK OF INDIA - A/C is deactivated.Don?t worry, your money is safe with us. ? To get your settlement- ??Call Paytm at 0120-4456456 or ? Click here to add a new bank A/C ?\"");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_REMITTER_BANK_FAIL_REASON NQH templateBody")
    public void FGW_REMITTER_BANK_FAIL_REASON_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_REMITTER_BANK_FAIL_REASON";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_REMITTER_BANK_FAIL_REASON");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_REMITTER_BANK_FAIL_REASON")
    public void FGW_REMITTER_BANK_FAIL_REASON_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_REMITTER_BANK_FAIL_REASON";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_REMITTER_BANK_FAIL_REASON");
        Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_REMITTER_BANK_FAIL_REASON NQH templateBody")
    public void FGW_REMITTER_BANK_FAIL_REASON_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_REMITTER_BANK_FAIL_REASON";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");

        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_REMITTER_BANK_FAIL_REASON");
        Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, due to technical issue with your bank.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n? To check your settlement, click here.'");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_REMITTER_BANK_FAIL_REASON CG SMSInfo")
    public void FGW_REMITTER_BANK_FAIL_REASON_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_REMITTER_BANK_FAIL_REASON";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"title\":\"?Settlement Declined\",\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed, due to technical issue with your bank.nn? Don't worry, your money is safe with us.n? We are retrying it and you should receive the money within next 24 hours.n? To check your settlement, click here.\"");
    }


    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_CLOSED NQH templateBody")
    public void FGW_PPBL_ACCOUNT_CLOSED_NQHDLTTemplateID_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_CLOSED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");
        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_CLOSED");
        Assertions.assertThat(nqhLogs).contains("dltEntityId='1501601290000011395");

    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_CLOSED")
    public void FGW_PPBL_ACCOUNT_CLOSED_NQHTemplateHEADER_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_CLOSED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");
        String nqhLogs = verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_CLOSED");
       Assertions.assertThat(nqhLogs).contains("templateHeader='?Settlement Declined | Change Bank A/C");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_CLOSED NQH templateBody")
    public void FGW_PPBL_ACCOUNT_CLOSED_NQHTemplate_Push() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_CLOSED";
        channel.add("PUSH");
        RealtimeMerchantNotify realtimeMerchantNotify = new RealtimeMerchantNotify();
        JsonPath realtimeMerchantNotifyResponse = realtimeMerchantNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultCode")).isEqualTo("SUCCESS");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.body.resultInfo.resultMsg")).isEqualTo("Success");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.function")).isEqualTo("alipayplus.settlement.settlementNotify");
        Assertions.assertThat(realtimeMerchantNotifyResponse.getString("response.head.clientId")).isEqualTo("notification-adapter");
        String nqhLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, errorCode);

        Assertions.assertThat(nqhLogs).contains("instErrorCode='FGW_PPBL_ACCOUNT_CLOSED");
       Assertions.assertThat(nqhLogs).contains("templateBody='Settlement of Rs. ${TXN_AMOUNT} in your ${BANK_NAME} - A/C ${ACCOUNT_NO} has failed, as your account is closed by your bank? Don?t worry, your money is safe with us. ? To get your settlement, click here to change bank A/C'");
    }

    @Owner(AKSHAT)
    @Feature("PGP-47519")
    @Parameters({"theme"})
    @Test(description = "SMS template :FGW_PPBL_ACCOUNT_CLOSED CG SMSInfo")
    public void FGW_PPBL_ACCOUNT_CLOSED_CGData_PUSH() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "FGW_PPBL_ACCOUNT_CLOSED";
        channel.add("PUSH");

        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("FAILED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();


        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("title\":\"?Settlement Declined | Change Bank A/C\"," +
                "\"message\":\"Settlement of Rs. 20.00 in your STATE BANK OF INDIA - A/C 1330********3383 has failed," +
                " as your account is closed by your bank?" +
                " Don?t worry, your money is safe with us." +
                " ? To get your settlement, click here to change bank A/C");
    }

    @Owner(POOJA)
    @Feature("PGP-55626")
    @Parameters({"theme"})
    @Test(description = "PG Notification Development - Settlement Timeline Sticky Notification (P4B)")
    public void STICKY_NOTIFICATION_UPS_REQUEST_RESPONSE() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "SUCCESS";
        channel.add("PUSH");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("PENDING")
                .setPreviousTxnStatus("TXN_ACCEPTED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String NQHLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.notification_Queue_handler, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(NQHLogs).contains("\"key\":\"ocl.notification.merchant.settlement_timeline_sticky_notification\"");
        Assertions.assertThat(NQHLogs).contains("\"value\":[{\"sendNotification\":\"true\"}]");
        Assertions.assertThat(NQHLogs).contains("sticky push notification ups flag for mid: qa12id70232557209005 is: true");
    }

    @Owner(POOJA)
    @Feature("PGP-55626")
    @Parameters({"theme"})
    @Test(description = "PG Notification Development - Settlement Timeline Sticky Notification (P4B)")
    public void STICKY_NOTIFICATION_Payload_CG() throws Exception {

        String mid = Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId();
        String OrderID = CommonHelpers.generateOrderId();
        ArrayList<String> channel = new ArrayList<>();
        String errorCode = "SUCCESS";
        channel.add("PUSH");
        SettlementNotify settlementNotify = new SettlementNotify();
        JsonPath settlementNotifyResponse = settlementNotify
                .setMID(mid)
                .setNotificationStatus("PENDING")
                .setPreviousTxnStatus("TXN_ACCEPTED")
                .setErrorCode(errorCode)
                .setChannel(channel)
                .execute().jsonPath();

        String CGLogs=verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.communication_Gateway, Constants.MerchantType.NOTIFICATION_ONLINE_MERCHANT_PUSH.getId());

        Assertions.assertThat(CGLogs).contains("\"Paytm Settlement of Rs. 20.00 Sent to Bank\"");
        Assertions.assertThat(CGLogs).contains("\"message\":\"Will be settled by 19 Apr, 05:00 PM\"");

    }

}
