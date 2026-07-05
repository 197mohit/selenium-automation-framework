package scripts.api.FPO;

import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.ProcessTransaction;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.appconstants.Constants.TransactionType;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.core.DriverManager;
import com.paytm.pages.*;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.HashMap;
import static com.paytm.apphelpers.CommonHelpers.*;
import static com.paytm.base.test.PGPBaseTest.userManager;

public class SubsUpiAutopay extends PGPBaseTest
{
     @Feature("PGP-50552")
     @Parameters({"theme"})
     @Owner(Constants.Owner.HIMANSHU)
     @Test(description = "Validate that PPBL comes in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is off and user has multiple accounts")
     public void PPBLPresence_Flag_Off_MultipleAcct_Substxn(@Optional("native") String theme) throws Exception
     {
         User user =userManager.getForRead(PGPBaseTest.Label.UPIPUSHPG2);
         Constants.MerchantType merchant= Constants.MerchantType.SUBSCRIPTION_PGONLY_RETRY;
         String SubscriptionStartDate = CommonHelpers.getDate().toString();
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                 .setTxnValue("10")
                 .setSubscriptionPaymentMode("UPI")
                 .setSubscriptionAmountType("VARIABLE")
                 .setSubscriptionMaxAmount("10")
                 .setSubscriptionFrequency("1")
                 .setSubscriptionFrequencyUnit("MONTH")
                 .setSubscriptionGraceDays("1")
                 .setSubscriptionRetryCount("0")
                 .setSubscriptionStartDate(SubscriptionStartDate)
                 .setRequestType("NATIVE_SUBSCRIPTION")
                 .build();
         String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
         FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
         FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                 initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
                 JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
         Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
         Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));

     }

     @Feature("PGP-50552")
     @Parameters({"theme"})
     @Owner(Constants.Owner.HIMANSHU)
     @Test(description = "Validate that PPBL Doesn't come in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is on and user has multiple accounts")
     public void PPBLPresence_Flag_On_MultipleAcct_Substxn(@Optional("native") String theme) throws Exception
     {
         User user =userManager.getForRead(PGPBaseTest.Label.UPIPG2FF4JCONFIGUSER);
         Constants.MerchantType merchant= Constants.MerchantType.Subscription_PGOnly;
         String SubscriptionStartDate = CommonHelpers.getDate().toString();
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                 .setTxnValue("10")
                 .setSubscriptionPaymentMode("UPI")
                 .setSubscriptionAmountType("VARIABLE")
                 .setSubscriptionMaxAmount("10")
                 .setSubscriptionFrequency("1")
                 .setSubscriptionFrequencyUnit("MONTH")
                 .setSubscriptionGraceDays("1")
                 .setSubscriptionRetryCount("0")
                 .setSubscriptionStartDate(SubscriptionStartDate)
                 .setRequestType("NATIVE_SUBSCRIPTION")
                 .build();
         String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
         FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
         FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                 initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
         JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
         Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
         Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));
     }

     @Feature("PGP-50552")
     @Parameters({"theme"})
     @Owner(Constants.Owner.HIMANSHU)
     @Test(description = "Validate that PPBL comes in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is off and user has single account")
     public void PPBLPresence_Flag_Off_SingleAcct_Substxn(@Optional("native") String theme) throws Exception
     {
         User user =userManager.getForRead(Label.STORECASH);
         Constants.MerchantType merchant= Constants.MerchantType.SUBSCRIPTION_PGONLY_RETRY;
         String SubscriptionStartDate = CommonHelpers.getDate().toString();
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                 .setTxnValue("10")
                 .setSubscriptionPaymentMode("UPI")
                 .setSubscriptionAmountType("VARIABLE")
                 .setSubscriptionMaxAmount("10")
                 .setSubscriptionFrequency("1")
                 .setSubscriptionFrequencyUnit("MONTH")
                 .setSubscriptionGraceDays("1")
                 .setSubscriptionRetryCount("0")
                 .setSubscriptionStartDate(SubscriptionStartDate)
                 .setRequestType("NATIVE_SUBSCRIPTION")
                 .build();
         String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
         FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
         FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                 initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
         JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
         Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
         Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));
     }

     @Feature("PGP-50552")
     @Parameters({"theme"})
     @Owner(Constants.Owner.HIMANSHU)
     @Test(description = "Validate that PPBL doesn't come in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is on and user has single account")
     public void PPBLPresence_Flag_On_SingleAcct_Substxn(@Optional("native") String theme) throws Exception
     {
         User user =userManager.getForRead(Label.STORECASH);
         Constants.MerchantType merchant= Constants.MerchantType.Subscription_PGOnly;
         String SubscriptionStartDate = CommonHelpers.getDate().toString();
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                 .setTxnValue("10")
                 .setSubscriptionPaymentMode("UPI")
                 .setSubscriptionAmountType("VARIABLE")
                 .setSubscriptionMaxAmount("10")
                 .setSubscriptionFrequency("1")
                 .setSubscriptionFrequencyUnit("MONTH")
                 .setSubscriptionGraceDays("1")
                 .setSubscriptionRetryCount("0")
                 .setSubscriptionStartDate(SubscriptionStartDate)
                 .setRequestType("NATIVE_SUBSCRIPTION")
                 .build();
         String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
         FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
         FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                 initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
         JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
         Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
         Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));
     }

     @Feature("PGP-50552")
     @Parameters({"theme"})
     @Owner(Constants.Owner.HIMANSHU)
     @Test(description = "Validate that PPBL comes in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is on and user has multple account_Addnpay txn")
     public void PPBLPresence_Flag_On_MultipleAcct_Addnpay(@Optional("native") String theme) throws Exception
     {
         User user =userManager.getForWrite(PGPBaseTest.Label.UPIPUSHPG2);
         WalletHelpers.modifyBalance(user,2.00);
         Constants.MerchantType merchant= Constants.MerchantType.NATIVE_ADDNPAY;
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                 .setTxnValue("10")
                 .build();
         String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
         FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
         FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                 initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
         JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
         Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
         Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));
     }

     @Feature("PGP-50552")
     @Parameters({"theme"})
     @Owner(Constants.Owner.HIMANSHU)
     @Test(description = "Validate that PPBL comes in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is on and user has multple account")
     public void PPBLPresence_Flag_On_MultipleAcct_Normaltxn(@Optional("native") String theme) throws Exception
     {
         User user =userManager.getForWrite(Label.UPIPUSHPG2);
         WalletHelpers.modifyBalance(user,2.00);
         Constants.MerchantType merchant= Constants.MerchantType.PPBLYONLY;
         InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                 .setTxnValue("10")
                 .build();
         String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
         FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
         FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                 initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
         JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
         Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
         Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));
     }

    @Feature("PGP-50552")
    @Parameters({"theme"})
    @Owner(Constants.Owner.HIMANSHU)
    @Test(description = "Validate that PPBL comes in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is off and user has multple account_Addnpay txn")
    public void PPBLPresence_Flag_Off_MultipleAcct_Addnpay(@Optional("native") String theme) throws Exception
    {
        User user =userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchant= Constants.MerchantType.NATIVE_ADDNPAY;
        WalletHelpers.modifyBalance(user,2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat((fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));
    }

    @Feature("PGP-50552")
    @Parameters({"theme"})
    @Owner(Constants.Owner.HIMANSHU)
    @Test(description = "Validate that PPBL comes in fpo resp if theia.enable.ppblFilterFromUPIBankAccountsForSubs ff4j is off and user has multple account normal txn")
    public void PPBLPresence_Flag_Off_MultipleAcct_Normaltxn(@Optional("native") String theme) throws Exception
    {
        User user =userManager.getForRead(Label.UPIPUSHPG2);
        Constants.MerchantType merchant= Constants.MerchantType.PPBLYONLY;
        WalletHelpers.modifyBalance(user,2.00);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchant)
                .setTxnValue("10")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(!(fetchPaymentOptionsJson.getString("body").contains("Paytm Payments Bank")));
    }

}
