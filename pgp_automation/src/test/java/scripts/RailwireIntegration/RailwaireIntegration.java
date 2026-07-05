package scripts.RailwireIntegration;

import com.paytm.ServerConfigProvider;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.api.MappingService.VendorDetails;
import com.paytm.api.TxnStatus;
import com.paytm.api.linkAPI.CreateNewLink;
import com.paytm.api.nativeAPI.InitTxn;

import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.*;
import com.paytm.dto.NativeDTO.InitTxn.Amount;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.SplitInfo;
import com.paytm.dto.NativeDTO.InitTxn.SplitSettlementInfo;
import com.paytm.dto.PromoDTO.ApplyPromoDTO.PaymentOptions;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.*;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.pages.CashierPage;
import com.paytm.pages.CashierPageFactory;
import com.paytm.pages.CheckoutPage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Arrays;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;


public class RailwaireIntegration extends PGPBaseTest{
    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType parentMerchant = Constants.MerchantType.RAILWIRE_PARENT_MID;
    com.paytm.appconstants.Constants.MerchantType aggMerchant = Constants.MerchantType.RAILWIRE_CHILD_MID;

    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Successfull Txn  When the MID is given and PartnerId both are given")
    public void SPLIT_Payment_001() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage("")).setPartnerId("jhgjhgss");
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"ACQUIRING_CREATE_ORDER_AND_PAY\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"targetMerchantId\":\"216820000008180726322\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"paytmMerchantId\":\"geXyvcmP581144877221\"");

    }

    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Successfull Txn When the PartnerId is given and MID is null")
    public void SPLIT_Payment_002() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setAmount(new Amount().setValue("8").setPercentage("")).setPartnerId("jhgjhgss");
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"ACQUIRING_CREATE_ORDER_AND_PAY\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"targetMerchantId\":\"216820000008180726322\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"paytmMerchantId\":\"geXyvcmP581144877221\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("jhgjhgss");

    }

    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Successfull Txn  When the MID is given and PartnerId is null")
    public void SPLIT_Payment_003() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"ACQUIRING_CREATE_ORDER_AND_PAY\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"targetMerchantId\":\"216820000008180726322\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"paytmMerchantId\":\"geXyvcmP581144877221\"");

    }

    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Failure Txn When the MID is null and also PartnerId send wrong then it will give error/exceptions.")
    public void SPLIT_Payment_004() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setAmount(new Amount().setValue("8").setPercentage("")).setPartnerId("jhgjhgs");
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +"grep \"Merchant data is not found for all given partnerIds from Mapping Service\n\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("Merchant data is not found for all given partnerIds from Mapping Service");

    }

    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Successfull Txn  When the MID and PartnerId send correct")
    public void SPLIT_Payment_005() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage("")).setPartnerId("jhgjhgss");
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        PaymentDTO paymentDTO = new PaymentDTO();
        PaymentOptions paymentOptions = new PaymentOptions("1.0", "CREDIT_CARD", "ICICI", paymentDTO.PROMO_CC_CARD_ICICI, "", null);

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"ACQUIRING_CREATE_ORDER_AND_PAY\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"targetMerchantId\":\"216820000008180726322\"");
        Assertions.assertThat(theiaFaacadeLogs).contains("\"paytmMerchantId\":\"geXyvcmP581144877221\"");

    }

    @Feature("PGP-37682")
    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Failure Txn When the MID is null and also PartnerId is also null then it will give error/exceptions.")
    public void SPLIT_Payment_006() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia.log  | " +"grep \"invalid mid in splitInfo\n\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("Mid or partnerId is missing from splitInfo data");
    }

    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Successfull Txn  When the MID is given and PartnerId both are given")
    public void SPLIT_Payment_007() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage("")).setPartnerId("jhgjhgss");
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"ACQUIRING_CREATE_ORDER_AND_PAY\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("\"splitMethod\":\"AMOUNT\"");

    }

    @Feature("PGP-37682")
    @Owner("Abhishek Gupta")
    @Test(description = "Verify a Successfull Txn  When the MID is given and PartnerId both are given")
    public void SPLIT_Payment_008() throws Exception {

        String txnAmount = "10";

        String vendorId= aggMerchant.getId();

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage("")).setPartnerId("jhgjhgss");
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, parentMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(parentMerchant.getId())
                .setMerchantKey(parentMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        String body = initTxnDTO.getBody().toString();
        System.out.println(body);
        InitTxn initTxn = new InitTxn(initTxnDTO);

        String orderId = initTxnDTO.orderFromBody();
        JsonPath iniJsonPath = initTxn.execute().jsonPath();

        String txnToken = iniJsonPath.getString("body.txnToken");

        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request
                .Builder(parentMerchant.getId(), txnToken, initTxnDTO.orderFromBody())
                .setPaymentMode("NET_BANKING")
                .setAuthMode("USRPWD")
                .setChannelCode("ICICI")
                .build();
        ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        String grepcmd = "grep \"" + "\" /paytm/logs/theia_facade.log  | " +"grep \"ACQUIRING_CREATE_ORDER_AND_PAY\"  ";
        String theiaFaacadeLogs = getLogsOnServer(ServerConfigProvider.SERVICE.THEIA_PRIMARY, grepcmd);
        Assertions.assertThat(theiaFaacadeLogs).contains("splitCommandInfoList");

    }

}
