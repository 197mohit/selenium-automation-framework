package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;


public class MCCADDNPAY extends PGPBaseTest {
    private static final String FULLKYC_PC = "4814";
    private static final String MINKYC_PC = "7013";

    @Test(description = "Validate UPI push transaction")
    public void validateUpiPushTxnADDNPAY() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.ZEROWALLET);
        String CustId = user.custId();
        String walletState = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletState);
        String txmAmount = "2.00";
        WalletHelpers.modifyBalance(user, Double.valueOf(txmAmount) - 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDNPAY_MCC_ADDMONEY)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCreditAccRefId");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.ADDNPAY_MCC_ADDMONEY.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .setPaymentFlow("ADDANDPAY")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName("WALLET")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);

        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"FULL KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"FULL KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"upi/ext/txn/v2/pay-merchant");
        Assertions.assertThat(instaUPIrequestlogs).contains("\"payeeCode\":\""+FULLKYC_PC+"\"");

    }

    @Test(description = "Validate UPI push transaction")
    public void validateUpiPushTxnADDNPAYMINKYC() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.MINKYC1);
        String CustId = user.custId();
        String walletState = "PAYTM_BASIC_PLUS";
        WalletHelpers.setWalletType(CustId,walletState);
        String txmAmount = "2.00";
        WalletHelpers.modifyBalance(user, Double.valueOf(txmAmount) - 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDNPAY_MCC_ADDMONEY)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCreditAccRefId");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.ADDNPAY_MCC_ADDMONEY.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .setPaymentFlow("ADDANDPAY")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String walletStateFinal = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletStateFinal);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName("WALLET")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);

        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"MIN KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"MIN KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"upi/ext/txn/v2/pay-merchant");
        Assertions.assertThat(instaUPIrequestlogs).contains("\"payeeCode\":\""+MINKYC_PC+"\"");

    }

    @Test(description = "Validate UPI push transaction")
    public void validateUpiPushTxnADDNPAYNOKYC() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.MINKYC1);
        String CustId = user.custId();
        String walletState = "PAYTM_PRIMITIVE";
        WalletHelpers.setWalletType(CustId,walletState);
        String txmAmount = "2.00";
        WalletHelpers.updateGVBalance(user, Double.valueOf(txmAmount) - 1.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ADDNPAY_MCC_ADDMONEY)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCreditAccRefId");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.ADDNPAY_MCC_ADDMONEY.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .setPaymentFlow("ADDANDPAY")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String walletStateFinal = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletStateFinal);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("PPI")
                .validateGatewayName("WALLET")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);

        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"NO_KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"pg.router.paytm.upi.payment.request");
        String passThroughExtendInfoInstalogs = instalogs.substring(instalogs.indexOf("passThroughExtendInfo")+24,instalogs.indexOf("\",\"directPassThroughInfo"));
       /* String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));*/
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"NO_KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"upi/ext/txn/v2/pay-merchant");
        Assertions.assertThat(instaUPIrequestlogs).contains("\"payeeCode\":\""+MINKYC_PC+"\"");

    }


    @Test(description = "Validate UPI push transaction ONUS")
    public void validateUpiPushTxnADDMONEY() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.ZEROWALLET);
        String CustId = user.custId();
        String walletState = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletState);
        String txmAmount = "2.00";
        WalletHelpers.modifyBalance(user,  0.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ONUS_ADDMONEY_MERCHANT)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCreditAccRefId");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.ONUS_ADDMONEY_MERCHANT.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();
        WalletHelpers.validateBalance(user, 0.00);

        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"FULL KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"FULL KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"upi/ext/txn/v2/pay-merchant");
        Assertions.assertThat(instaUPIrequestlogs).contains("\"payeeCode\":\""+FULLKYC_PC+"\"");

    }


    @Test(description = "Validate UPI push transaction ONUS")
    public void validateUpiPushTxnADDMONEYMINKYC() throws Exception{
        String mpin = "NPCI,20150822,2.1|n0jxOf1B7JH8277fnEZKCDGrX0a031UCvIbMP8NSm31x+\\/vcbP7N5KIpaRoY4LGZPRXt75aaKTA6Q2BxgoAVOHzeNwQL6FRKyB7tJktgb5TAYU2MAzi1HNhqWovzdxhBaH6wVgEmSvp5opwh1V+Z7uSTVAQPkNth6l4oseJkxwSnaemnqre+kzohRdQpzNsDPE9OlaPD772pIoCojsY5QKzZcDRFg2d7tnw9Rb3wS4GCCdk6wgP0aIQVv3tqVQO2lqblMQIOk0M+C22rZaY1MTe3TflLRAeCc3NDvuvEEQ1lBrjqFt2lge4fLEeIUsr9hxZdRIyHuTA3fpl4oB9D2w==";
        String riskExtendedInfo = "deviceType:Mobile|timeZone:IST|osType:IOS|osVersion:15.1|platform:APP|terminalType:APP|deviceManufacturer:Apple|channelId:WAP|paymentFlow:NONE|versionCode:5109|screenResolution:750x1334|appVersion:9.21.0|operationType:PAYMENT|userLBSLatitude:32.19|isRooted:false|deviceId:1DCB75C8-1A6F-4A7B-94B6-C497542397D8|businessFlow:DEFER_CHECKOUT|deviceModel:iPhone 6s (iOS 15.1)|userLBSLongitude:75.65|language:en-IN";
        User user = userManager.getForRead(Label.MINKYC1);
        String CustId = user.custId();
        String walletState = "PAYTM_BASIC_PLUS";
        WalletHelpers.setWalletType(CustId,walletState);
        String txmAmount = "2.00";
        WalletHelpers.modifyBalance(user,  0.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.ONUS_ADDMONEY_MERCHANT)
                .setTxnValue(txmAmount)
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        String upiID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].name");
        String accRefID = fetchPaymentOptionsJson.getString("body.merchantPayOption.upiProfile.respDetails.profileDetail.vpaDetails[0].defaultCreditAccRefId");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                Constants.MerchantType.ONUS_ADDMONEY_MERCHANT.getId(),initTxnResponse.getBody().getTxnToken(),initTxnDTO.getBody().getOrderId(),"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setChannelCode("push")
                .setChannelId("WAP")
                .setMpin(mpin)
                .setRiskExtendInfo(riskExtendedInfo)
                .setPayerAccount(upiID)
                .setUpiAccRefId(accRefID)
                .setChannelId("WAP")
                .build();
        NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        String walletStateFinal = "PAYTM_PRIME_WALLET";
        WalletHelpers.setWalletType(CustId,walletStateFinal);
        TxnStatus txnStatus = new TxnStatus(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validatePaymentMode("UPI")
                .validateGatewayName("PPBEX")
                .AssertAll();

        WalletHelpers.validateBalance(user, 0.00);

        String WalletLimitLogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"wallet-web/walletLimits");
        Assertions.assertThat(WalletLimitLogs).contains("\"isWalletCategoryRequired\":true");
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,initTxnDTO.orderFromBody(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        String passThroughExtendInfologs = logs.substring(logs.indexOf("passThroughExtendInfo")+24,logs.indexOf("\",\"virtualPaymentAddr\""));
        String decrypted=PGPHelpers.Base64Decode(passThroughExtendInfologs);
        Assertions.assertThat(decrypted).contains("\"walletRbiType\":\"MIN KYC\"");

        String instalogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"pg.router.paytm.upi.payment.request");
        String extendedInfo = instalogs.substring(instalogs.indexOf("\"extendInfo\"")+14,instalogs.indexOf("\", \"settleType\""));
        String decryptedInstaEI=PGPHelpers.Base64Decode(extendedInfo);
        String passThroughExtendInfoInstalogs = decryptedInstaEI.substring(decryptedInstaEI.indexOf("\"passThroughExtendInfo\"")+24,decryptedInstaEI.indexOf("\",\"directPassThroughInfo"));
        String decryptedInsta=PGPHelpers.Base64Decode(passThroughExtendInfoInstalogs);
        Assertions.assertThat(decryptedInsta).contains("\"walletRbiType\":\"MIN KYC\"");
        String instaUPIrequestlogs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.instaproxy,initTxnDTO.orderFromBody(),"upi/ext/txn/v2/pay-merchant");
        Assertions.assertThat(instaUPIrequestlogs).contains("\"payeeCode\":\""+MINKYC_PC+"\"");

    }
}
