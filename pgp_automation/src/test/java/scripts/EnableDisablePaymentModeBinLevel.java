package scripts;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.paytm.LocalConfig;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchBinDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.EnablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.NativeDTO.fetchBinDetails.FetchBinDetailsRequest;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.ResponsePage;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.Assertions;
import com.paytm.base.test.User;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;

import java.util.Date;

import static com.paytm.appconstants.Constants.Owner.CHAKSHU;

public class EnableDisablePaymentModeBinLevel extends PGPBaseTest {

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled CreditCard Paymode with 6 Digits Bins")
    public void verifyInitiateTransactionwithEnabledCreditCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled CreditCard Paymode with 5 Digits Bins")
    public void verifyInitiateTransactionwithEnabledCreditCardPaymodewith5DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"47186"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled CreditCard Paymode with 9 Digits Bins")
    public void verifyInitiateTransactionwithEnabledCreditCardPaymodewith9DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865010"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled CreditCard Paymode with 10 Digits Bins")
    public void verifyInitiateTransactionwithEnabledCreditCardPaymodewith10DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"4718650100"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled CreditCard Paymode with 6 Digits Bins and channels")
    public void verifyInitiateTransactionwithEnabledCreditCardPaymodewith6DigitsBinsandChannels() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        String[] banks = {"AXIS"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins).setBanks(banks)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled CreditCard Paymode with 6 Digits alphanumberic Bins")
    public void verifyInitiateTransactionwithEnabledCreditCardPaymodewith6DigitsalphanumbericBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"4718a5"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Disabled CreditCard Paymode with 6 Digits Bins")
    public void verifyInitiateTransactionwithDisabledCreditCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Disabled CreditCard Paymode with 5 Digits Bins")
    public void verifyInitiateTransactionwithDisabledCreditCardPaymodewith5DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"47186"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Disabled CreditCard Paymode with 9 Digits Bins")
    public void verifyInitiateTransactionwithDisabledCreditCardPaymodewith9DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865010"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Disabled CreditCard Paymode with 10 Digits Bins")
    public void verifyInitiateTransactionwithDisabledCreditCardPaymodewith10DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"4718650100"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Disabled CreditCard Paymode with 6 Digits Bins and channels")
    public void verifyInitiateTransactionwithDisabledCreditCardPaymodewith6DigitsBinsandChannels() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        String[] banks = {"AXIS"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins).setBanks(banks)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Disabled CreditCard Paymode with 6 Digits alphanumberic Bins")
    public void verifyInitiateTransactionwithDisabledCreditCardPaymodewith6DigitsalphanumbericBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"4718a5"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled DebitCard , Disabled CreditCard Paymode with 6 Digits Bins")
    public void verifyInitiateTransactionwithEnabledDebitCardDisabledCreditCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        String[] bins1 = {"444433"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("DEBIT_CARD").setBins(bins1)})
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled DebitCard invalid Bin Disabled CreditCard Paymode")
    public void verifyInitiateTransactionwithEnabledDebitCardinvalidBinDisabledCreditCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        String[] bins1 = {"44443@"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("DEBIT_CARD").setBins(bins1)})
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Initiate Transaction with Enabled DebitCard Disabled CreditCard Paymode Invalid Bin")
    public void verifyInitiateTransactionwithEnabledDebitCardDisabledCreditCardPaymodeInvalidBin() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"4718652345"};
        String[] bins1 = {"444433"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("DEBIT_CARD").setBins(bins1)})
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();
        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultCode")).isEqualTo("1001");
        Assertions.assertThat(response.jsonPath().getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Enabled saved CreditCard Paymode with 6 Digits Bins")
    public void verifyFPOwithEnabledsavedCreditCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Enabled DebitCard Paymode with 6 Digits Bins")
    public void verifyFPOwithEnabledDebitCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"444433"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("DEBIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("CREDIT_CARD");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Enabled saved EMI Paymode with 6 Digits Bins")
    public void verifyFPOwithEnabledsavedEMIPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("DEBIT_CARD");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled saved CreditCard Paymode with 6 Digits Bins")
    public void verifyFPOwithDisabledsavedCreditCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled DebitCard Paymode with 6 Digits Bins")
    public void verifyFPOwithDisabledDebitCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"444433"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("DEBIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled saved EMI Paymode with 6 Digits Bins")
    public void verifyFPOwithDisabledsavedEMIPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled saved CreditCard Paymode")
    public void verifyFPOwithDisabledsavedCreditCardPaymode() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD")})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled DebitCard Paymode")
    public void verifyFPOwithDisabledDebitCardPaymode() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("DEBIT_CARD")})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");


    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled saved EMI Paymode")
    public void verifyFPOwithDisabledsavedEMIPaymode() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("200")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("EMI")})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled saved bin UPI Paymode with 6 Digits Bins")
    public void verifyFPOwithDisabledsavedBinUpiPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("100")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("UPI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("UPI");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Disabled saved bin UPI Paymode")
    public void verifyFPOwithDisabledsavedBinUpiPaymode() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("100")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("UPI")})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("UPI");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify FPO with Enabled saved bin UPI Paymode with 6 Digits Bins")
    public void verifyFPOwithEnabledsavedBinUpiPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("100")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("UPI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("DEBIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("CREDIT_CARD");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .doesNotContain("EMI");
        Assertions.assertThat(fetchPaymentOptionsJson.param("status", "false").getList(
                        "body.merchantPayOption.paymentModes.findAll { paymentModes -> paymentModes.isDisabled.status == status }.paymentMode"))
                .contains("UPI");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details  with Enabled saved CreditCard Paymode with 6 Digits Bins")
    public void verifyFetchbinDetailswithEnabledCreditCardPaymodewith6DigitsBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details  with Enabled saved CreditCard Paymode with 6 Digits Invalid Bins")
    public void verifyFetchbinDetailswithEnabledCreditCardPaymodewith6DigitsInvalidBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471866"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Bin number is not valid");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details  with Enabled saved CreditCard Paymode with 6 Digits Bins DC Bin")
    public void verifyFetchbinDetailswithEnabledCreditCardPaymodewith6DigitsDCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "444433").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("2011");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details  with SSO Token")
    public void verifyFetchbinDetailswithSSOToken() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;

        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder("471865", merchantType, user.ssoToken(), "true").setMid(merchantType.getId()).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), false);
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();

        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details  with Invalid Input Param")
    public void verifyFetchbinDetailswithInvalidInputParam() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;

        FetchBinDetailsRequest fetchBinDetailsRequest = new FetchBinDetailsRequest.Builder("471865", merchantType, user.ssoToken(), "true").setMid("qa14hs110%^57412298096").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(fetchBinDetailsRequest, fetchBinDetailsRequest.getBody().getMid(), false);
        Response fetchBinsJson = fetchBinDetail.execute();
        Assertions.assertThat(fetchBinsJson.statusCode()).isEqualTo(403);

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details  with Enabled Debit Card Paymode with 6 Digits Saved  CC Bin")
    public void verifyFetchbinDetailswithEnabledDebitCardPaymodewith6DigitsSavedCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType)
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("DEBIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());
        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Request parameters are not valid");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details for EMI credit card  with Enabled Credit Card Paymode with 6 Digits Bin")
    public void verifyFetchbinDetailsForEMICCwithEnabledCreditCardPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").setIsEMIDetail("false").setEmiType("CREDIT_CARD").setChannelId("WAP").setPaymentMode("EMI").setMid(merchantType.getId()).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());

        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Bin number is not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details for EMI credit card  with Enabled EMI Paymode with 6 Digits Bin")
    public void verifyFetchbinDetailsForEMICCwithEnabledEMIPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").setIsEMIDetail("false").setEmiType("CREDIT_CARD").setChannelId("WAP").setPaymentMode("EMI").setMid(merchantType.getId()).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());

        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details for credit card  with Enabled EMI Paymode with 6 Digits Bin")
    public void verifyFetchbinDetailsForCCwithEnabledEMIPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());

        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Bin number is not valid");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details for credit card  with Disabled EMI Paymode with 6 Digits Bin")
    public void verifyFetchbinDetailsForCCwithDisbledEMIPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());

        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details for EMI credit card  with Disabled Credit Card Paymode with 6 Digits Bin")
    public void verifyFetchbinDetailsForEMICCwithDisabledCreditCardPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").setIsEMIDetail("false").setEmiType("CREDIT_CARD").setChannelId("WAP").setPaymentMode("EMI").setMid(merchantType.getId()).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());

        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();

        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details for EMI credit card  with Disabled EMI Paymode with 6 Digits Bin")
    public void verifyFetchbinDetailsForEMICCwithDisabledEMIPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "471865").setIsEMIDetail("false").setEmiType("CREDIT_CARD").setChannelId("WAP").setPaymentMode("EMI").setMid(merchantType.getId()).build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());

        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("F");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("1003");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Bin number is not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify Fetch bin Details for debit card  with Disabled Credit Card Paymode with 6 Digits Bin")
    public void verifyFetchbinDetailsForDCwithDisabledCreditCardPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchBinDetailsRequest request = new FetchBinDetailsRequest.Builder(txnToken, "444433").build();
        FetchBinDetail fetchBinDetail = new FetchBinDetail(request, initTxnDTO.getBody().getMid(),
                initTxnDTO.getBody().getOrderId());

        JsonPath fetchBinsJson = fetchBinDetail.execute().jsonPath();
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultStatus")).isEqualTo("S");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        Assertions.assertThat(fetchBinsJson.getString("body.resultInfo.resultMsg")).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for CC with Enabled Credit Card Paymode with 6 Digits Bin")
    public void verifyPTCForCCwithEnabledCreditCardPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for CC with Enabled Credit Card Paymode with 6 Digits different CC  Bin")
    public void verifyPTCForCCwithEnabledCreditCardPaymodewith6DigitsdifferentCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471866"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for DC with Enabled Credit Card Paymode with 6 Digits CC  Bin")
    public void verifyPTCForDCwithEnabledCreditCardPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4444333322221111|504|08" + PaymentDTO.EXP_YEAR;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Debit Card is not allowed for this transaction, kindly use some other payment mode");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for CC with Enabled EMI Paymode with 6 Digits CC  Bin")
    public void verifyPTCForCCwithEnabledEMIPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Credit Card is not allowed for this transaction, kindly use some other payment mode");
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for CC with Disabled Credit Card Paymode with 6 Digits Bin")
    public void verifyPTCForCCwithDisabledCreditCardPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        String orderId = initTxnDTO.getBody().getOrderId();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Request parameters are not valid");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for CC with Disabled Credit Card Paymode with 6 Digits different CC  Bin")
    public void verifyPTCForCCwithDisabledCreditCardPaymodewith6DigitsdifferentCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471866"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for DC with Disabled Credit Card Paymode with 6 Digits CC  Bin")
    public void verifyPTCForDCwithDisabledCreditCardPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4444333322221111|504|08" + PaymentDTO.EXP_YEAR;
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("DEBIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();

        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("DC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("DC")
                .validateMid(mid)
                .AssertAll();
    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "verify PTC for CC with Disabled EMI Paymode with 6 Digits CC  Bin")
    public void verifyPTCForCCwithDisabledEMIPaymodewith6DigitsCCBins() {

        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        String[] bins = {"471865"};
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("EMI").setBins(bins)})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId(), "")
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();


        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(orderId)
                .validatePaymentMode("CC")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody()))
                .validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(mid, orderId);
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(initTxnDTO.getBody().getOrderId())
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validatePaymentMode("CC")
                .validateMid(mid)
                .AssertAll();

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "Deffered Initiate with enabled UPI PTC with CC Retry with UPI")
    public void DefferedInitiatewithenabledUPI_PTCwithCC_RetrywithUPI() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken(), mid).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setEnablePaymentMode(new EnablePaymentMode[]
                        {new EnablePaymentMode().setMode("UPI")})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();


        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Credit Card is not allowed for this transaction, kindly use some other payment mode");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid payment mode");


        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }

    @Owner(CHAKSHU)
    @Feature("PGP-58596")
    @Test(description = "Deffered Initiate with Disabled Credit Card PTC with CC Retry with UPI")
    public void DefferedInitiatewithDisabledCC_PTCwithCC_RetrywithUPI() throws Exception {
        User user = userManager.getForWrite(Label.UPIPG2FF4JCONFIGUSER);
        Constants.MerchantType merchantType = Constants.MerchantType.Static_True_Recent_True;
        String mid = merchantType.getId();
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken(), mid).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(merchantType.getId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.resultInfo.resultCode")).isEqualTo("0000");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchantType).setTxnValue("900")
                .setDisablePaymentMode(new DisablePaymentMode[]
                        {new DisablePaymentMode().setMode("CREDIT_CARD")})
                .build();

        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        String cardInfo = "|4718650100010336|504|08" + PaymentDTO.EXP_YEAR;
        String orderId = initTxnDTO.getBody().getOrderId();
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid, initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
                .setPaymentMode("CREDIT_CARD")
                .setCardInfo(cardInfo)
                .setAuthMode("otp")
                .build();


        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);

        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("F");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0001");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Credit Card is not allowed for this transaction, kindly use some other payment mode");
        Assertions.assertThat(ptcResponse.getBody().getTxnInfo().getRESPMSG()).isEqualTo("Invalid payment mode");


        ProcessTxnV1Request processTxnV1Request1 = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),orderId,"PTM" + initTxnDTO.getBody().getOrderId())
                .setPaymentMode("UPI")
                .setAuthMode("USRPWD")
                .setPayerAccount("test9972746530@paytm")
                .build();
        ProcessTxnV1Response ptcResponse1 = NativeHelpers.executeProcessTxnV1(processTxnV1Request1);
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse1.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");

    }

}
