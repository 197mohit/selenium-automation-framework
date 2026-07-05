package scripts.flows;

import com.paytm.api.Peon;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.WalletHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.OdishaCustomPTC.CustomPTCOdishaDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.PeonResponse;
import com.paytm.framework.ui.element.UIElement;
import com.paytm.pages.*;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;
import org.testng.annotations.Optional;
import org.testng.annotations.Test;
import scripts.api.merchantStatus.odishaTransaction.OdishaTransactionStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.util.*;


public class CustomProcessTransactionTest extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();
    private final String txnAmount = "2";
//    private final String theme = "enhancedweb";
    private final String payerName = "Gagandeep Singh";
    private final Map<String, Integer> merchantStatusPayloadWithIndex = new HashMap<String, Integer>() {{
        put("MERCHANTCD", 0);
        put("CHLNREFNO", 1);
        put("TOTAMT", 2);
        put("BKTRNID", 3);
        put("BKTRNSTS", 4);
        put("BKTRNMSG", 5);
        put("BKTRNTIME", 6);
        put("ABKCD", 7);
        put("ABKTRNID", 8);
        put("ABKTRNSTS", 9);
        put("ABKTRNMSG", 10);
        put("ABKTRNTIME", 11);
        put("APAYMODE", 12);
        put("CHECKSUM", 13);
    }};
    Constants.MerchantType odishaDynamicWrapper = Constants.MerchantType.ODISHA_DYNAMIC_WRAPPER;

    private List<String> getListOfPayModesOnCashierPage(CashierPage cashierPage) {
        List<UIElement> PaymodesOnPage = cashierPage.ListOfPayModsOnCashier();
        List<String> paymethodList = new ArrayList<>();
        for (UIElement uiElement : PaymodesOnPage) {
            paymethodList.add(uiElement.getText().split("\n")[0]);
        }
        return paymethodList;
    }

    private byte[] genHmac(final byte[] data, final byte[] key) throws Exception {
        final Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        final SecretKeySpec secretKey = new SecretKeySpec(key, "HmacSHA256");
        sha256_HMAC.init(secretKey);
        return sha256_HMAC.doFinal(data);
    }

    private String getBase64String(final byte[] byteArray) throws Exception {
        return Base64.getEncoder().encodeToString(byteArray);
    }

    private String merchantStatusChecksum(List<String> encString) throws Exception {
        StringBuilder requestString= new StringBuilder();
        for(int i = 0 ; i < encString.size()-1; i++)
            requestString.append(encString.get(i)).append("|");
        requestString = new StringBuilder(requestString.substring(0, requestString.length() - 1));
        byte[] keyData = odishaDynamicWrapper.getKey().getBytes();
        byte[] arr = requestString.toString().getBytes();
        byte[] arr_response = genHmac(arr, keyData);
        return getBase64String(arr_response);
    }


    private void validateOdishaPaymentPageFields(CustomPTCOdishaDTO customPTCOdishaDTO) {
        OdishaPaymentPage odishaPaymentPage = new OdishaPaymentPage();
        odishaPaymentPage.waitUntilLoads();
        SoftAssertions validateSoftly = new SoftAssertions();
        validateSoftly.assertThat(odishaPaymentPage.tableOdishaPaymentForm().getRowValue(OdishaPaymentPage
                .odishaPaymentDetails.DEPOSITOR_NAME.toString()).equalsIgnoreCase(payerName));
        validateSoftly.assertThat(odishaPaymentPage.tableOdishaPaymentForm().getRowValue(OdishaPaymentPage
                .odishaPaymentDetails.BANK_TRANSACTION_DATE.toString()).contains(CommonHelpers.getDate().toString()));
        validateSoftly.assertThat(odishaPaymentPage.tableOdishaPaymentForm().getRowValue(OdishaPaymentPage
                .odishaPaymentDetails.CHALLAN_REF_NO.toString()).equalsIgnoreCase(customPTCOdishaDTO.getCHLNREFNO()));
        validateSoftly.assertThat(odishaPaymentPage.tableOdishaPaymentForm().getRowValue(OdishaPaymentPage
                .odishaPaymentDetails.BANK_TRANSACTION_DATE.toString()).contains(CommonHelpers.getDate().toString()));
        validateSoftly.assertThat(odishaPaymentPage.tableOdishaPaymentForm().getRowValue(OdishaPaymentPage
                .odishaPaymentDetails.CHALLAN_AMOUNT.toString()).equalsIgnoreCase(txnAmount));
        validateSoftly.assertAll();
    }


    private void validateSuccessfulMerchantStatus(CustomPTCOdishaDTO customPTCOdishaDTO, List<String> statusList, String paymode) throws Exception {
        SoftAssertions validateSoftly = new SoftAssertions();
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("MERCHANTCD")))
                .isEqualTo(odishaDynamicWrapper.getId());
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("CHLNREFNO")))
                .isEqualTo(customPTCOdishaDTO.getCHLNREFNO());
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("TOTAMT")))
                .isEqualTo(customPTCOdishaDTO.getTOTAMT());
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("MERCHANTCD"))).isNotNull();
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("BKTRNID"))).isNotNull();
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("BKTRNSTS")))
                .isEqualTo("S");
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("BKTRNMSG")))
                .isEqualTo("Payment Successful");
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("BKTRNTIME"))).isNotNull();
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("ABKCD"))).isNotNull();
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("ABKTRNID"))).isNotNull();
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("ABKTRNSTS")))
                .isEqualTo("S");
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("ABKTRNMSG")))
                .isEqualTo("Payment Successful");
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("ABKTRNTIME"))).isNotNull();
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("APAYMODE")))
                .isEqualTo(paymode);
        validateSoftly.assertThat(statusList.get(merchantStatusPayloadWithIndex.get("CHECKSUM")))
                .isEqualTo(merchantStatusChecksum(statusList));
        validateSoftly.assertAll();
    }



    private void validateSuccessfulPeon(CustomPTCOdishaDTO customPTCOdishaDTO,String payMode) throws Exception {
        Peon peon = new Peon(customPTCOdishaDTO.getCHLNREFNO());
        peon.executeUntilGetResponse();
        SoftAssertions softAssert = new SoftAssertions();
        PeonResponse peonResponse;
        peonResponse = peon.getPeonData(customPTCOdishaDTO.getCHLNREFNO());
        softAssert.assertThat(peonResponse.getABKTRNID()).isNotNull();
        softAssert.assertThat(peonResponse.getMERCHANTCD()).isEqualToIgnoringCase(customPTCOdishaDTO.getMERCHANTCD());
        softAssert.assertThat(peonResponse.getBKTRNSTS()).isNotNull();
        softAssert.assertThat(peonResponse.getABKTRNSTS()).isNotNull();
        softAssert.assertThat(peonResponse.getCHLNREFNO()).isEqualToIgnoringCase(customPTCOdishaDTO.getCHLNREFNO());
        softAssert.assertThat(peonResponse.getTOTAMT()).isEqualTo(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(customPTCOdishaDTO.getTOTAMT())));
        softAssert.assertThat(peonResponse.getABKCD()).isNotNull();
        softAssert.assertThat(peonResponse.getBKTRNMSG()).isEqualTo("Txn Success");
        softAssert.assertThat(peonResponse.getABKTRNMSG()).isEqualTo("Txn Success");
        softAssert.assertThat(peonResponse.getBKTRNTIME()).isNotNull();
        softAssert.assertThat(peonResponse.getBKTRNID()).isNotNull();
        softAssert.assertThat(peonResponse.getABKTRNTIME()).isNotNull();
        softAssert.assertThat(peonResponse.getAPAYMODE()).isEqualToIgnoringCase(payMode);
        softAssert.assertAll();

    }



 //   @Test(description = "Successful Odisha Dynamic Wrapper transaction with Paymode NB",enabled = false)
    public void successfulNBTxnForOdishaMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "NB";
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();
        System.out.println(customPTCOdishaDTO.generateChecksum());
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        validateOdishaPaymentPageFields(customPTCOdishaDTO);
        OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
        Response response = odishaTransactionStatus.execute();
        response.then().contentType(ContentType.TEXT);
        String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response.asString());
        List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
        validateSuccessfulMerchantStatus(customPTCOdishaDTO, txnStatusList, "N");
        validateSuccessfulPeon(customPTCOdishaDTO,"N");
    }


 //   @Test(description = "Successful Odisha Dynamic Wrapper transaction with Paymode DC",enabled = false)
    public void successfulDCTxnForOdishaMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        validateOdishaPaymentPageFields(customPTCOdishaDTO);
        OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
        Response response = odishaTransactionStatus.execute();
        response.then().contentType(ContentType.TEXT);
        String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response.asString());
        List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
        validateSuccessfulMerchantStatus(customPTCOdishaDTO, txnStatusList, "D");
        validateSuccessfulPeon(customPTCOdishaDTO,"D");
    }


 //   @Test(description = "Successful Odisha Dynamic Wrapper transaction with Paymode UPI",enabled = false)
    public void successfulUPITxnForOdishaMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "UPI";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();
        System.out.println(customPTCOdishaDTO.generateChecksum());
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.UPI);
        validateOdishaPaymentPageFields(customPTCOdishaDTO);
        OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
        Response response = odishaTransactionStatus.execute();
        response.then().contentType(ContentType.TEXT);
        String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response.asString());
        List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
        validateSuccessfulMerchantStatus(customPTCOdishaDTO, txnStatusList, "U");
        validateSuccessfulPeon(customPTCOdishaDTO,"U");
    }


 //   @Test(description = "Successful Odisha Dynamic Wrapper transaction with Paymode Wallet",enabled = false)
    public void successfulWalletTxnForOdishaMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {

        User user = userManager.getForWrite(Label.LOGIN);
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "PPI";
        WalletHelpers.modifyBalance(user, Double.valueOf(txnAmount));
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();

        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.login(user);

        cashierPage.payBy(Constants.PayMode.WALLET);
        validateOdishaPaymentPageFields(customPTCOdishaDTO);
        OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
        Response response = odishaTransactionStatus.execute();
        response.then().contentType(ContentType.JSON);
        String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response.asString());
        List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
        validateSuccessfulMerchantStatus(customPTCOdishaDTO, txnStatusList, "W");
        validateSuccessfulPeon(customPTCOdishaDTO,"W");
    }

 //   @Test(description = "To verify when we login on cashier page and only single paymode is visible",enabled = false)
    public void singlePaymodeIsVisibleOnCashierPage(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        List<String> paymethodList = getListOfPayModesOnCashierPage(cashierPage);
        Assert.assertEquals(paymethodList.size(), 1);
    }
    
 //   @Test(description = "To verify download receipt button is clickable",enabled = false)
    public void verifyPrintReciept(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        OdishaPaymentPage odishaPaymentPage = new OdishaPaymentPage();
        odishaPaymentPage.waitUntilLoads();
        odishaPaymentPage.downloadRecieptButton().assertClickable();
    }


 //   @Test(description = "To verify print receipt button is clickable",enabled = false)
    public void verifyPageRedirectingToMerchantcallBack(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        OdishaPaymentPage odishaPaymentPage = new OdishaPaymentPage();
        odishaPaymentPage.printRecieptButton().assertClickable();
    }


//    @Test(description = "failure Odisha Dynamic Wrapper transaction with Paymode NB having retry",enabled = false)
        public void failureDCTxnForOdishaMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
            String orderId = CommonHelpers.generateOrderId();
            String payMode = "NB";
            String txnAmount = "99.99";
            PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
            CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                    .setPAYEENM(payerName)
                    .build();
            System.out.println(customPTCOdishaDTO.generateChecksum());
            OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
            checkoutPage.createCustomPTCOrder(orderDTO);
            CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
            cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
            cashierPage.waitUntilLoads();
            cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
            validateOdishaPaymentPageFields(customPTCOdishaDTO);
            OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
            String response = odishaTransactionStatus.execute().asString();
            String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response);
            List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
            SoftAssertions validateSoftly = new SoftAssertions();
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("MERCHANTCD")))
                    .isEqualTo(odishaDynamicWrapper.getId());
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("CHLNREFNO")))
                    .isEqualTo(customPTCOdishaDTO.getCHLNREFNO());
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("TOTAMT")))
                    .isEqualTo(customPTCOdishaDTO.getGOVTAMT());
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("MERCHANTCD"))).isNotNull();
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNID"))).isNotNull();
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNSTS")))
                    .isEqualTo("F");
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNMSG")))
                    .isEqualTo("Payment Failed");
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNTIME"))).isNotNull();
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKCD"))).isNotNull();
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNID"))).isNotNull();
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNSTS")))
                    .isEqualTo("F");
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNMSG")))
                    .isEqualTo("Payment Failed");
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNTIME"))).isNotNull();
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("APAYMODE")))
                    .isEqualTo("N");
            validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("CHECKSUM")))
                    .isEqualTo(merchantStatusChecksum(txnStatusList));
            validateSoftly.assertAll();
            Peon peon = new Peon(customPTCOdishaDTO.getCHLNREFNO());
            peon.executeUntilGetResponse();
            SoftAssertions softAssert = new SoftAssertions();
            PeonResponse peonResponse;
            peonResponse = peon.getPeonData(customPTCOdishaDTO.getCHLNREFNO());
            softAssert.assertThat(peonResponse.getABKTRNID()).isNotNull();
            softAssert.assertThat(peonResponse.getMERCHANTCD()).isEqualToIgnoringCase(customPTCOdishaDTO.getMERCHANTCD());
            softAssert.assertThat(peonResponse.getBKTRNSTS()).isNotNull();
            softAssert.assertThat(peonResponse.getABKTRNSTS()).isNotNull();
            softAssert.assertThat(peonResponse.getCHLNREFNO()).isEqualToIgnoringCase(customPTCOdishaDTO.getCHLNREFNO());
            softAssert.assertThat(peonResponse.getTOTAMT()).isEqualTo(CommonHelpers.doubleToTwoDigitAfterDecimalPoint(Double.valueOf(customPTCOdishaDTO.getTOTAMT())));
            softAssert.assertThat(peonResponse.getABKCD()).isNotNull();
            softAssert.assertThat(peonResponse.getBKTRNMSG()).isEqualTo("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
            softAssert.assertThat(peonResponse.getABKTRNMSG()).isEqualTo("Your payment has been declined by your bank. Please contact your bank for any queries. If money has been deducted from your account, your bank will inform us within 48 hrs and we will refund the same");
            softAssert.assertThat(peonResponse.getBKTRNTIME()).isNotNull();
            softAssert.assertThat(peonResponse.getBKTRNID()).isNotNull();
            softAssert.assertThat(peonResponse.getABKTRNTIME()).isNotNull();
            softAssert.assertThat(peonResponse.getAPAYMODE()).isEqualToIgnoringCase("N");
            softAssert.assertAll();
        }

  //  @Test(description = "To verify when we pass total txn amount less than the amount sent for child agent in string request",enabled = false)
    public void verifyInitiateAmountLessThanTotalAmount() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        String txnAmount = "3";
        String govtAmount = "1";
        String agentAmount = "1";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setGOVTAMT(govtAmount)
                .setAGID1("agent10")
                .setAGIDAMT1(agentAmount)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }




 //   @Test(description = "To verify when we pass total txn amount more than the amount sent for child agent in string request",enabled = false)
    public void verifyInitiateAmountMoreThanTotalAmount() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        String govtAmount = "2";
        String agentAmount = "1";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setGOVTAMT(govtAmount)
                .setAGID1("agent10")
                .setAGIDAMT1(agentAmount)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

 //   @Test(description = "To verify when we initiate txn using '0' rs in agent id amount",enabled = false)
    public void verifyInitiateTxnUsing0RsAgentId(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        String govtAmount = "2";
        String agentAmount = "0";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setGOVTAMT(govtAmount)
                .setAGID1("agent10")
                .setAGIDAMT1(agentAmount)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        validateOdishaPaymentPageFields(customPTCOdishaDTO);
        OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
        String response = odishaTransactionStatus.execute().asString();
        String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response);
        List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
        validateSuccessfulMerchantStatus(customPTCOdishaDTO, txnStatusList, "D");
        validateSuccessfulPeon(customPTCOdishaDTO,"D");
    }


 //   @Test(description = "To verify split transaction with govt and agent on total amount",enabled = false)
    public void verifySplitTxnWithAgentAndGovt(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        String txnAmount = "5";
        String govtAmount = "2";
        String agent1Amount = "1";
        String agent2Amount = "2";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setGOVTAMT(govtAmount)
                .setAGID1("agent10")
                .setAGIDAMT1(agent1Amount)
                .setAGID2("agent11")
                .setAGIDAMT2(agent2Amount)
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.DC);
        validateOdishaPaymentPageFields(customPTCOdishaDTO);
        OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
        String response = odishaTransactionStatus.execute().asString();
        String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response);
        List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
        validateSuccessfulMerchantStatus(customPTCOdishaDTO, txnStatusList, "D");
        validateSuccessfulPeon(customPTCOdishaDTO,"D");
    }

//    @Test(description = "To verify when we pass agent id empty & agent amount is passed in string request",enabled = false)
    public void verifyAgentIdEmptyAgentAmountPassed() throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "DC";
        String txnAmount = "5";
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setGOVTAMT("4")
                .setAGID1("")
                .setAGIDAMT1("1")
                .setPAYEENM(payerName)
                .build();
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        LostInSpacePage lostInSpacePage = new LostInSpacePage();
        lostInSpacePage.imgLostInSpace().assertVisible();
    }

 //   @Test(description = "pending Odisha Dynamic Wrapper transaction with Paymode NB having retry",enabled = false)
    public void pendingDCTxnForOdishaMerchant(@Optional("enhancedweb_revamp") String theme) throws Exception {
        String orderId = CommonHelpers.generateOrderId();
        String payMode = "NB";
        String txnAmount = "99.51";
        PaymentDTO paymentDTO = new PaymentDTO().setBankName("ICICI");
        CustomPTCOdishaDTO customPTCOdishaDTO = new CustomPTCOdishaDTO.Builder(odishaDynamicWrapper, orderId, txnAmount, payMode)
                .setPAYEENM(payerName)
                .build();
        System.out.println(customPTCOdishaDTO.generateChecksum());
        OrderDTO orderDTO = new OrderFactory.CustomProcessTransaction(customPTCOdishaDTO).build();
        checkoutPage.createCustomPTCOrder(orderDTO);
        CashierPage cashierPage = CashierPageFactory.getCashierPage(theme);
        cashierPage.payBy(Constants.PayMode.NB, paymentDTO);
        validateOdishaPaymentPageFields(customPTCOdishaDTO);
        OdishaTransactionStatus odishaTransactionStatus = new OdishaTransactionStatus(odishaDynamicWrapper, orderId, txnAmount);
        String response = odishaTransactionStatus.execute().asString();
        String encString = odishaTransactionStatus.decryptMsg(odishaDynamicWrapper, response);
        List<String> txnStatusList = Arrays.asList(encString.split("\\|"));
        SoftAssertions validateSoftly = new SoftAssertions();
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("MERCHANTCD")))
                .isEqualTo(odishaDynamicWrapper.getId());
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("CHLNREFNO")))
                .isEqualTo(customPTCOdishaDTO.getCHLNREFNO());
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("TOTAMT")))
                .isEqualTo(customPTCOdishaDTO.getGOVTAMT());
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("MERCHANTCD"))).isNotNull();
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNID"))).isNotNull();
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNSTS")))
                .isEqualTo("P");
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNMSG")))
                .isEqualTo("Payment Pending");
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("BKTRNTIME"))).isNotNull();
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKCD"))).isNotNull();
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNID"))).isNotNull();
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNSTS")))
                .isEqualTo("P");
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNMSG")))
                .isEqualTo("Payment Pending");
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("ABKTRNTIME"))).isNotNull();
        validateSoftly.assertThat(txnStatusList.get(merchantStatusPayloadWithIndex.get("CHECKSUM")))
                .isEqualTo(merchantStatusChecksum(txnStatusList));
        validateSoftly.assertAll();
    }
}
