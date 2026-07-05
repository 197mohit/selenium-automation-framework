package scripts.Native;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.api.theia.FetchCardDetails;
import com.paytm.api.theia.FetchCardIndexNumber;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchCardDetailsDTO.FetchCardDetailsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.response.FetchPaymentOptResponseDTO;
import com.paytm.dto.NativeDTO.InitTxn.DisablePaymentMode;
import com.paytm.dto.NativeDTO.InitTxn.Good;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.reporting.Reporter;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.GVConsentPage;
import com.paytm.pages.KYCPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import scripts.IAddMoney;

import java.io.IOException;

@Owner("Tarun")
@Feature("PGP-19696")
public class NativeAddMoneyTP extends PGPBaseTest implements IAddMoney {

    CheckoutPage checkoutPage = new CheckoutPage();

    protected FetchPaymentOptResponseDTO fetchPaymentOptions(String txnToken, InitTxnDTO initTxnDTO) {
        Reporter.report.info("Validating fetch pay options for the merchant and txn token");
        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath jsonPath = response.jsonPath();
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(jsonPath.get());

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        FetchPaymentOptResponseDTO fetchPaymentOptResponseDTO = null;
        try {
            fetchPaymentOptResponseDTO = mapper.readValue(jsonObject.toJSONString(), FetchPaymentOptResponseDTO.class);
        } catch (IOException e) {
            throw new SkipException("Change in FetchPaymentOption DTO", e);
        }
        return fetchPaymentOptResponseDTO;
    }

    //Native Add Money Flow

    @Test
    @Override
    public void validateFullKYCWalletCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

            User user = userManager.getForWrite(Label.LOGIN);
            PaymentDTO paymentDTO = new PaymentDTO();
            double txnAmt = 10001.0;
            WalletHelpers.setZeroBalance(user);
            Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

            InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                    .setIsNativeAddMoney("true")
                    .setTxnValue(String.valueOf(txnAmt))
                    .setCardHash(cardHash)
                    .build();
            String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

            OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                    .setREQUEST_TYPE("Add_Money")
                    .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                    .build();
            checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();
        }

    @Test
    @Override
    public void validateFullKYCWalletSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();

    }

    //MIN KYC is not working with CIN, concern raised to @Somesh
    @Test
    @Override
    public void  validateMinKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 11.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                //.setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.pause(2);
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }


    @Test
    @Override
    public void validateMinKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();
    }

    @Test
    @Override
    public void validateNoKYCWalletLimitNotBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 11.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        KYCPage kycPage = new KYCPage();
        Assert.assertTrue(kycPage.submitBtnNew().isElementPresent(),"KYC page is not getting opened for : " + user.mobNo());
    }

    @Test
    @Override
    public void validateNoKYCWalletLimitBreached(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage
                //.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespMsg("With this transaction, this credit card will exceed the monthly add money limit of Rs 10000/- allowed without any charges. you can continue to add money using UPI, Paytm bank account or a different card")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();

    }

    @Test
    @Override
    public void validateFullKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(addMoneyTPMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();
    }

    @Test
    @Override
    public void validateFullKYCGVSavedCC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getCreditCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(addMoneyTPMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();
    }

    //MIN KYC is not working with CIN, concern raised to @Somesh
    @Test
    @Override
    public void validateMinKYCLimitNotBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 11.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(addMoneyTPMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
               // .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.pause(2);
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    @Test
    @Override
    public void validateMinKYCLimitBreachedGV(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(addMoneyTPMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();

    }

    @Test
    @Override
    public void validateNoKYCGV(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch CIN
        FetchCardIndexNumber fetchCardIndexNumber = new FetchCardIndexNumber(addMoneyTPMerchant,paymentDTO.getCreditCardNumber(),paymentDTO.getExpMonth(),paymentDTO.getExpYear());
        JsonPath fetchCINResponse = fetchCardIndexNumber.execute().jsonPath();
        String cin = fetchCINResponse.getString("body.cardIndexNumber");

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getCreditCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();

        Good goodInfo = new Good();
        goodInfo.setMerchantGoodsId("154435058"); //Adding money in GV

        initTxnDTO.getBody().setGoods(new Good[]{goodInfo});

        String checksum = PGPHelpers.getNativeChecksum(addMoneyTPMerchant.getKey(), initTxnDTO.getBody());
        initTxnDTO.getHead().setSignature(checksum);

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.CREDIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .setCardInfo(cin+"||"+paymentDTO.getCvvNumber()+"|") // Txn through CIN
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("You can buy up to Rs. 10,000 worth of Gift voucher per month using credit card. To continue, please use UPI or Debit card payment option")
                .validateRespCode("501")
                .validateStatus("TXN_FAILURE")
                .validateTxnAmount(String.valueOf(txnAmt))
                .assertAll();
    }

    @Test
    @Override
    public void validateFullKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.LOGIN);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getDebitCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    @Test
    @Override
    public void validateMinKycNB(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.MINKYCEXPIRED);
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setREQUEST_TYPE("Add_Money")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }

    @Test
    @Override
    public void validateNoKYCDC(@Optional("enhancedweb_revamp") String theme) throws Exception {
        User user = userManager.getForWrite(Label.BASICTOKYC);
        PaymentDTO paymentDTO = new PaymentDTO();
        double txnAmt = 10001.0;
        WalletHelpers.setZeroBalance(user);
        Constants.MerchantType addMoneyTPMerchant = Constants.MerchantType.AddMoney;

        //Fetch Card Details
        FetchCardDetailsDTO fetchCardDetailsDTO = new FetchCardDetailsDTO.Builder()
                .setTokenType("SSO")
                .setToken(user.ssoToken())
                .setMID(addMoneyTPMerchant.getId())
                .setCardNumber(paymentDTO.getDebitCardNumber())
                .build();

        FetchCardDetails fetchCardDetails = new FetchCardDetails(fetchCardDetailsDTO,addMoneyTPMerchant.getId());
        JsonPath fetchCardDetailsResponse = fetchCardDetails.execute().jsonPath();
        String cardHash = fetchCardDetailsResponse.getString("body.cardHash");

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), addMoneyTPMerchant)
                .setIsNativeAddMoney("true")
                .setTxnValue(String.valueOf(txnAmt))
                .setCardHash(cardHash)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(addMoneyTPMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setREQUEST_TYPE("Add_Money")
                .build();
        checkoutPage.createNativeOrder(orderDTO, false);
        GVConsentPage gvConsentPage = new GVConsentPage();
        gvConsentPage.proceedToBuyGiftVoucher();
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateRespMsg("Txn Success")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateTxnAmount(initTxnDTO.txnAmountFromBody())
                .assertAll();
    }


    //          Native Add N Pay         //

    @Parameters({"isNativePlus"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "P+ Side Native: For addNPay Flow : when we login at cashierPage, mid cards are displayed first, after login all cards of userid and custid are displayed, if flag theia.sendMerchantIdInAddNPayLitePayViewTask is enabled")
    public void loginAtCashierPageToCheckMIDUserIdCardsAlipayAddPay(@Optional("false") Boolean isNativePlus) throws Exception {
        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType addNPayMerchant = Constants.MerchantType.AddnPay;
        SavedCardHelpers.assertStoreCardPrefEnabled(addNPayMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO();

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        WalletHelpers.modifyBalance(user,txnAmount-1.0);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        String cinDC = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber()); //DC

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(addNPayMerchant.getId(),custId);

        //Adding for MID/CustId on P+ side
       String cinCC = SavedCardHelpers.addCardAlipay(addNPayMerchant.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber()); //CC

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, addNPayMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //MID CustId card should be visible
       Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinCC).doesNotContain(cinDC);

        //Initiate with SSO
        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(user.ssoToken(), addNPayMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO1 = new FetchPaymentOptionsDTO.Builder(txnToken1).build();
        FetchPaymentOption fetchPaymentOption1 = new FetchPaymentOption(initTxnDTO1.getBody().getMid(),
                initTxnDTO1.orderFromBody(), fetchPaymentOptionsDTO1);
        Response response1 = fetchPaymentOption1.execute();
        JsonPath fpoWithUser = response1.jsonPath();

        //MID CustId + UserId card should be visible
        Assertions.assertThat(fpoWithUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinCC,cinDC);

        paymentDTO.setSavedCardId(cinCC);

        //Txn through CIN
        OrderDTO orderDTO = new OrderFactory.Native(addNPayMerchant, initTxnDTO1.orderFromBody(), txnToken1,paymentDTO, PayMethodType.CREDIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("PPI")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(addNPayMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("PPI")
                .AssertAll();

    }

    @Parameters({"isNativePlus"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "P+ Side Native: For addNPay Flow : when we login at cashierPage, mid cards are displayed first, after login all cards of userid and custid are displayed, if flag theia.sendMerchantIdInAddNPayLitePayViewTask is enabled")
    public void storeCardPrefOff(@Optional("false") Boolean isNativePlus) throws Exception {
        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType addNPayMerchant = Constants.MerchantType.FOOD_MERCHANT_ADDNPAY;
        SavedCardHelpers.assertStoreCardPrefDisabled(addNPayMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO();

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        WalletHelpers.modifyBalance(user,txnAmount-1.0);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        String cinDC = SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber()); //DC

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(addNPayMerchant.getId(),custId);

        //Adding for MID/CustId on P+ side
        String cinCC = SavedCardHelpers.addCardAlipay(addNPayMerchant.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber()); //CC

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, addNPayMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        //MID CustId card should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments")).isNullOrEmpty();

        //Initiate with SSO
        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(user.ssoToken(), addNPayMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .build();
        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO1 = new FetchPaymentOptionsDTO.Builder(txnToken1).build();
        FetchPaymentOption fetchPaymentOption1 = new FetchPaymentOption(initTxnDTO1.getBody().getMid(),
                initTxnDTO1.orderFromBody(), fetchPaymentOptionsDTO1);
        Response response1 = fetchPaymentOption1.execute();
        JsonPath fpoWithUser = response1.jsonPath();

        //MID CustId + UserId card should be visible
        Assertions.assertThat(fpoWithUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinDC).doesNotContain(cinCC);

        paymentDTO.setSavedCardId(cinDC);

        //Txn through CIN
        OrderDTO orderDTO = new OrderFactory.Native(addNPayMerchant, initTxnDTO1.orderFromBody(), txnToken1,paymentDTO, PayMethodType.DEBIT_CARD)
                .setPaymentFlow("ADDANDPAY")
                .build();

        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("WALLET")
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(addNPayMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderid(orderDTO.getORDER_ID())
                .validateStatus("TXN_SUCCESS")
                .validateRespCode("01")
                .validateRespMsg("Txn Successful.")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("WALLET")
                .AssertAll();

    }


    @Parameters({"isNativePlus"})
    @Epic(Constants.Sprint.SPRINT34_2)
    @Feature("PGP_24623")
    @Owner("Tarun")
    @Test(description = "P+ Side Native: For addNPay Flow : Check the cards when cc/dc is sent in disabled paymode in request being handled at theia, p+ sends cc cards")
    public void disablePayModeCCDC(@Optional("false") Boolean isNativePlus) throws Exception {
        String custId =CommonHelpers.generateOrderId();
        Constants.MerchantType addNPayMerchant = Constants.MerchantType.AddnPay;
        SavedCardHelpers.assertStoreCardPrefEnabled(addNPayMerchant);

        SavedCardHelpers.enableAllSavedCardFlags();
        PaymentDTO paymentDTO = new PaymentDTO();

        Double txnAmount = 2.0;
        User user = userManager.getForWrite(Label.SAVECARDMIGRATION);
        WalletHelpers.modifyBalance(user,txnAmount-1.0);

        //Deleting for user on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(user);

        //Adding for user on P+ side
        String cinDC =SavedCardHelpers.addCardAlipay(user, paymentDTO.getExpMonth(), paymentDTO.getExpYear(), paymentDTO.getDebitCardNumber()); //DC

        //Deleting for MID/CustId on P+ side
        SavedCardHelpers.deleteSavedCardsAlipay(addNPayMerchant.getId(),custId);

        //Adding for MID/CustId on P+ side
       String cinCC = SavedCardHelpers.addCardAlipay(addNPayMerchant.getId(),custId,paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getCreditCardNumber()); //CC

        //Initiate
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, addNPayMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setCustId(custId)
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("CC")})
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder(txnToken).build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(initTxnDTO.getBody().getMid(),
                initTxnDTO.orderFromBody(), fetchPaymentOptionsDTO);
        Response response = fetchPaymentOption.execute();
        JsonPath fpoWithoutUser = response.jsonPath();

        // CC/DC should not be visible
        Assertions.assertThat(fpoWithoutUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).doesNotContain(cinCC).doesNotContain(cinDC);

        //Initiate with SSO
        InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(user.ssoToken(), addNPayMerchant)
                .setTxnValue(String.valueOf(txnAmount))
                .setDisablePaymentMode(new DisablePaymentMode[]{new DisablePaymentMode().setMode("CC")})
                .setCustId(custId)
                .build();
        String txnToken1 = NativeHelpers.Validate_InitTxn(initTxnDTO1);

        //FPO

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO1 = new FetchPaymentOptionsDTO.Builder(txnToken1).build();
        FetchPaymentOption fetchPaymentOption1 = new FetchPaymentOption(initTxnDTO1.getBody().getMid(),
                initTxnDTO1.orderFromBody(), fetchPaymentOptionsDTO1);
        Response response1 = fetchPaymentOption1.execute();
        JsonPath fpoWithUser = response1.jsonPath();

        // CC/DC should be visible in AddNPay payMode
        Assertions.assertThat(fpoWithUser.getList("body.merchantPayOption.savedInstruments.cardDetails.cardId")).contains(cinDC).doesNotContain(cinCC);

    }

}
