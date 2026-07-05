package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.FetchPaymentOptionV2;
import com.paytm.api.nativeAPI.FetchPaymentOptionV5;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import javax.print.DocFlavor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class UPIPushExpDis extends PGPBaseTest {

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

         Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPOSSO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})}).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPOSSO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})}).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPOSSO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})}).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
    }

    //On sending all three channel UPI,UPIPUSHEXPRESS & UPIPUSH
    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOAllChannel() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOAllChannel() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOAllChannel() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPOSSO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOSSOAllChannel() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})}).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPOSSO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOSSOAllChannel() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})}).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPOSSO :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOSSOAllChannel() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})}).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPI");
    }
// MID having UPI & UPIINTENT Paymode
@Owner("Shubham Soni")
@Feature("PGP-54047")
@Test(description = "V2FPO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
public void validateUPIIntentFoundV2FPOEXPRESSDISABLE() throws Exception{
    User user = userManager.getForRead(Label.ZEROWALLET);
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE)
            .setTxnValue("1.00")
            .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
            .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
    String txnToken = initTxnResponse.getBody().getTxnToken();

    FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
    FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
            initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

    Response resposne = fetchPaymentOption.execute();
    JsonPath jsonPath = resposne.jsonPath();
    List<String> channelCodes = new ArrayList<>();
    List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
    for (Map<String, Object> paymentMode : paymentModes) {
        List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
        for (Map<String, Object> payChannelOption : payChannelOptions) {
            channelCodes.add((String) payChannelOption.get("channelCode"));
        }
    }
    for (String s : channelCodes)
    {
        System.out.println(s);
    }
    Assertions.assertThat(channelCodes).contains("UPIPUSH");

}

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPOSSO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOSSOEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId()).setGenerateOrderId("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})}).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPOSSO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOSSOEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})}).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPOSSO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOSSOEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})}).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
    }

    //On sending all three channel UPI,UPIPUSHEXPRESS & UPIPUSH
    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOAllChannelEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOAllChannelEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPOEXPRESSDISABLE :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOAllChannelEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
        Assertions.assertThat(channelCodesGroupMerch).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPOSSOEXPRESSDISABLE :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOSSOAllChannelEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})}).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPOSSOEXPRESSDISABLE :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOSSOAllChannelEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})}).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPOSSO EXPRESSDISABLE:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOSSOAllChannelEXPRESSDISABLE() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS","UPIPUSH"})}).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.UPI_PUSHEXPRESS_DISABLE.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).contains("UPIPUSH");
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSH");
        Assertions.assertThat(channelCodesGroupMerch).doesNotContain("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPI");
    }

    //Disable Paymode
    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPO :Validate UPI Intent is not  coming in response on sending enable paymode UPI & UPIPUSHEXPRESS and Upi Intent in Disable paymode, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentDisableModeFoundV2FPO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPO :Validate UPI Intent is not  coming in response on sending enable paymode UPI & UPIPUSHEXPRESS and Upi Intent in Disable paymode, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentDisableModeFoundV1FPO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPO :Validate UPI Intent is not  coming in response on sending enable paymode UPI & UPIPUSHEXPRESS and Upi Intent in Disable paymode, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentDisableModeFoundV5FPO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.UPI_PUSH_INTENT)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPIPUSH"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPOSSO :Validate UPI Intent is not  coming in response on sending enable paymode UPI & UPIPUSHEXPRESS and Upi Intent in Disable paymode, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentDisableModeFoundV2FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPIPUSH"})})
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPOSSO :Validate UPI Intent is not  coming in response on sending enable paymode UPI & UPIPUSHEXPRESS and Upi Intent in Disable paymode, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentDisableModeFoundV1FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPIPUSH"})})
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPOSSO :Validate UPI Intent is not  coming in response on sending enable paymode UPI & UPIPUSHEXPRESS and Upi Intent in Disable paymode, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentDisableModeFoundV5FPOSSO() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPIPUSH"})})
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPOSSO OfflineFlag:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOSSOOfflineFlag() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setOfflineFlow("true")
                .build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V1FPOSSO OfflineFlag:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV1FPOSSOOfflineFlag() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setOfflineFlow("true")
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V5FPOSSO OfflineFlag:Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV5FPOSSOOfflineFlag() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO", user.ssoToken())
                .setMid(Constants.MerchantType.UPI_PUSH_INTENT.getId()).setGenerateOrderId("true").
                setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .setOfflineFlow("true")
                .build();
        FetchPaymentOptionV5 fetchPaymentOption = new FetchPaymentOptionV5(Constants.MerchantType.UPI_PUSH_INTENT.getId(), fetchPaymentOptionsDTO);
        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

        List<String> channelCodesGroupMerch = new ArrayList<>();
        List<Map<String, Object>> paymentModesGroupMerch = jsonPath.getList("body.groupedMerchantPayOption.other_options");
        for (Map<String, Object> paymentMode : paymentModesGroupMerch) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodesGroupMerch.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodesGroupMerch)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodesGroupMerch).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodesGroupMerch).contains("UPI");
    }

    @Owner("Shubham Soni")
    @Feature("PGP-54047")
    @Test(description = "V2FPO FF4J Flag OFF :Validate UPI Intent is coming in response on sending enable paymode UPI & UPIPUSHEXPRESS, FF4J is on theia.addUpiPushWhenUpiPushExpressPresent.inEnablePayModeChannel")
    public void validateUPIIntentFoundV2FPOFF4JOFF() throws Exception{
        User user = userManager.getForRead(Label.ZEROWALLET);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), Constants.MerchantType.WALINTER_ONLINE_NULL)
                .setTxnValue("1.00")
                .setEnablePaymentMode(new EnablePaymentMode[]{new EnablePaymentMode().setMode("UPI").setChannels(new Object[]{"UPI", "UPIPUSHEXPRESS"})})
                .build();
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOptionV2 fetchPaymentOption = new FetchPaymentOptionV2(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);

        Response resposne = fetchPaymentOption.execute();
        JsonPath jsonPath = resposne.jsonPath();
        List<String> channelCodes = new ArrayList<>();
        List<Map<String, Object>> paymentModes = jsonPath.getList("body.merchantPayOption.paymentModes");
        for (Map<String, Object> paymentMode : paymentModes) {
            List<Map<String, Object>> payChannelOptions = (List<Map<String, Object>>) paymentMode.get("payChannelOptions");
            for (Map<String, Object> payChannelOption : payChannelOptions) {
                channelCodes.add((String) payChannelOption.get("channelCode"));
            }
        }
        for (String s : channelCodes)
        {
            System.out.println(s);
        }
        Assertions.assertThat(channelCodes).doesNotContain("UPIPUSH");
        Assertions.assertThat(channelCodes).contains("UPIPUSHEXPRESS");
        Assertions.assertThat(channelCodes).contains("UPI");

    }

}
