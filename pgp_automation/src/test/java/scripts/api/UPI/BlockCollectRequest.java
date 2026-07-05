package scripts.api.UPI;

import com.paytm.api.ProcessTransactionV1;
import com.paytm.api.RedisAPI;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.utils.ff4j.FF4JFlags;
import com.paytm.utils.merchant.ff4j.FF4JFeatures;
import com.paytm.utils.merchant.ff4j.annotations.FF4JFeature;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.Test;

public class BlockCollectRequest extends PGPBaseTest {
    private SoftAssertions softly = new SoftAssertions();
    String ErrorMsg_5816="Regulatory Policy Failure - Collect greater than Rs. 2k not allowed";
    String ErrorMsg_4814="Regulatory Policy Failure - Collect greater than Rs. 5k not allowed";
    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is getting blocked in FPO for MCC 5816 when txn amount> 2000 and FF4J theia.blockUpiCollecForMcc is enabled")
    public void CollectRequestBlockFOR_5816() throws Exception {
        FF4JFlags.enable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_5816_MID;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2001.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size = fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        for (int i = 0; i < size; i++) {
            Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions[" + i + "].channelCode")).isNotEqualTo("UPI");
        }
    }
    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is not getting blocked in FPO for MCC 5816 when txn amount< 2000 and FF4J theia.blockUpiCollecForMcc is enabled")
    public void CollectRequestNotBlockFOR_5816() throws Exception {
        FF4JFlags.enable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
            User user = userManager.getForRead(Label.PPBL);
            Constants.MerchantType merchant = Constants.MerchantType.MCC_5816_MID;
            String orderID = CommonHelpers.generateOrderId();
            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                    .setTxnValue("1999.00")
                    .setSsoToken(user.ssoToken())
                    .setOrderId(orderID)
                    .setChannelId("WEB")
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
            FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
            FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
            JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
            int size=fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
            String channelCode="";
            for(int i=0;i<size;i++)
            {
                if (fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode").equals("UPI"))
                {
                    channelCode="UPI";
                    softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode")).isEqualTo("UPI");
                }
            }
            softly.assertThat(channelCode).isEqualTo("UPI");
            softly.assertAll();
        }

    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is not getting blocked in FPO for MCC 5816 when txn amount> 2000 and FF4J theia.blockUpiCollecForMcc is disabled")
    public void CollectRequestNotBlockFOR_5816_FF4J_OFF() throws Exception {
        //FF4JFlags.disable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_5816_MID_FF4J_OFF;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2999.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size=fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        String channelCode="";
        for(int i=0;i<size;i++)
        {
            if (fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode").equals("UPI"))
            {
                channelCode="UPI";
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode")).isEqualTo("UPI");
            }
        }
        softly.assertThat(channelCode).isEqualTo("UPI");
        softly.assertAll();
    }

    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is not getting blocked in FPO for MCC 5816 when txn amount< 2000 and FF4J theia.blockUpiCollecForMcc is disabled")
    public void CollectRequestNotBlockFOR_5816_txn() throws Exception {
        //FF4JFlags.disable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_5816_MID_FF4J_OFF;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("1999.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size=fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        String channelCode="";
        for(int i=0;i<size;i++)
        {
            if (fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode").equals("UPI"))
            {
                channelCode="UPI";
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode")).isEqualTo("UPI");
            }
        }
        softly.assertThat(channelCode).isEqualTo("UPI");
        softly.assertAll();
    }

    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is getting blocked in PTC for MCC 5816 when txn amount> 2000 and FF4J theia.blockUpiCollecForMcc is enabled")
    public void CollectRequestBlockFOR_5816_PTC() throws Exception {
        FF4JFlags.enable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_5816_MID;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("2001.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size = fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        for (int i = 0; i < size; i++)
        {
            softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions[" + i + "].channelCode")).isNotEqualTo("UPI");
        }
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken,initTxnDTO.orderFromBody())
                .setPaymentMode("UPI").build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        JsonPath PTCResponse=processTransactionV1.execute().jsonPath();
        softly.assertThat(PTCResponse.getString("body.resultInfo.resultMsg")).isEqualTo(ErrorMsg_5816);
        softly.assertThat(PTCResponse.getString("body.resultInfo.resultCode")).isEqualTo("1013");
        softly.assertThat(PTCResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }
//=======================================
@Feature("PGP-52167")
@Owner(Constants.Owner.MANISH_MISHRA)
@Test(description = "Verify UPI collect is getting blocked in FPO for MCC 4814 when txn amount> 5000 and FF4J theia.blockUpiCollecForMcc is enabled")
public void CollectRequestBlockFOR_4814() throws Exception {
    FF4JFlags.enable("theia.blockUpiCollecForMcc");
    RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
    User user = userManager.getForRead(Label.PPBL);
    Constants.MerchantType merchant = Constants.MerchantType.MCC_4814_MID;
    String orderID = CommonHelpers.generateOrderId();
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
            .setTxnValue("5001.00")
            .setSsoToken(user.ssoToken())
            .setOrderId(orderID)
            .setChannelId("WEB")
            .build();
    String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
    FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
    JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
    int size = fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
    for (int i = 0; i < size; i++) {
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions[" + i + "].channelCode")).isNotEqualTo("UPI");
    }
}
    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is not getting blocked in FPO for MCC 4814 when txn amount< 5000 and FF4J theia.blockUpiCollecForMcc is enabled")
    public void CollectRequestNotBlockFOR_4814() throws Exception {
        FF4JFlags.enable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_4814_MID;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("4999.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size=fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        String channelCode="";
        for(int i=0;i<size;i++)
        {
            if (fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode").equals("UPI"))
            {
                channelCode="UPI";
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode")).isEqualTo("UPI");
            }
        }
        softly.assertThat(channelCode).isEqualTo("UPI");
        softly.assertAll();
    }

    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is not getting blocked in FPO for MCC 4814 when txn amount> 5000 and FF4J theia.blockUpiCollecForMcc is disabled")
    public void CollectRequestNotBlockFOR_4814_FF4J_OFF() throws Exception {
        //FF4JFlags.disable("theia.blockUpiCollecForMcc");

        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_4814_MID_FF4J_OFF;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5999.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size=fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        String channelCode="";
        for(int i=0;i<size;i++)
        {
            if (fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode").equals("UPI"))
            {
                channelCode="UPI";
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode")).isEqualTo("UPI");
            }
        }
        softly.assertThat(channelCode).isEqualTo("UPI");
        softly.assertAll();
    }

    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is not getting blocked in FPO for MCC 4814 when txn amount< 5000 and FF4J theia.blockUpiCollecForMcc is disabled")
    public void CollectRequestNotBlockFOR_4814_txn() throws Exception {
        //FF4JFlags.disable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");
        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_4814_MID_FF4J_OFF;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("4999.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size=fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        String channelCode="";
        for(int i=0;i<size;i++)
        {
            if (fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode").equals("UPI"))
            {
                channelCode="UPI";
                softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions["+i+"].channelCode")).isEqualTo("UPI");
            }
        }
        softly.assertThat(channelCode).isEqualTo("UPI");
        softly.assertAll();
    }

    @Feature("PGP-52167")
    @Owner(Constants.Owner.MANISH_MISHRA)
    @Test(description = "Verify UPI collect is getting blocked in PTC for MCC 4814 when txn amount> 5000 and FF4J theia.blockUpiCollecForMcc is enabled")
    public void CollectRequestBlockFOR_4814_PTC() throws Exception {
        FF4JFlags.enable("theia.blockUpiCollecForMcc");
        RedisAPI.deleteKey("FF4J_FEATURE_theia.blockUpiCollecForMcc");

        User user = userManager.getForRead(Label.PPBL);
        Constants.MerchantType merchant = Constants.MerchantType.MCC_4814_MID;
        String orderID = CommonHelpers.generateOrderId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue("5001.00")
                .setSsoToken(user.ssoToken())
                .setOrderId(orderID)
                .setChannelId("WEB")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).setVersion("v5").setIsLiteEligible(true).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(), initTxnDTO.getBody().getOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        int size = fetchPaymentOptionsJson.getInt("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions.size()");
        for (int i = 0; i < size; i++)
        {
            softly.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.paymentModes.find {it.displayName == 'BHIM UPI'}.payChannelOptions[" + i + "].channelCode")).isNotEqualTo("UPI");
        }
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(merchant.getId(), txnToken,initTxnDTO.orderFromBody())
                .setPaymentMode("UPI").build();
        ProcessTransactionV1 processTransactionV1 = new ProcessTransactionV1(processTxnV1Request);
        JsonPath PTCResponse=processTransactionV1.execute().jsonPath();
        softly.assertThat(PTCResponse.getString("body.resultInfo.resultMsg")).isEqualTo(ErrorMsg_4814);
        softly.assertThat(PTCResponse.getString("body.resultInfo.resultCode")).isEqualTo("1013");
        softly.assertThat(PTCResponse.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        softly.assertAll();
    }

}
