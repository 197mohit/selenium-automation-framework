package scripts;

import com.paytm.api.nativeAPI.FetchEMIDetail;
import com.paytm.api.nativeAPI.FetchPaymentOption;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.Group;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.FetchPaymentOptionsDTO.FetchPaymentOptionsDTO;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.fetchEMIDetail.FetchEMIDetailRequest;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.utils.merchant.api.MappingService.GetMerchantExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

@Owner("Tarun")
@Epic("PWP")
@Feature("PGP-20557")
public class PWPNative extends PGPBaseTest {

    private CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType pwpDefault = Constants.MerchantType.PWP_DEFAULT;
    Constants.MerchantType pwpHybrid = Constants.MerchantType.PWP_HYBRID;
    Constants.MerchantType pwpRetry = Constants.MerchantType.PWP_HYBRID_RETRY;
    Constants.MerchantType pwpDefaultPg2Rtdd = Constants.MerchantType.PWP_DEFAULT_PG2_RTDD;
    Constants.MerchantType pwpHybridPg2Rtdd = Constants.MerchantType.PWP_HYBRID_PG2_RTDD;
    Constants.MerchantType pwpRetryPg2Rtdd = Constants.MerchantType.PWP_HYBRID_RETRY_PG2_RTDD;
    Constants.MerchantType pwpMutualFund =  Constants.MerchantType.PWP_MF;
    private String txnAmount = "2.00";

    @BeforeClass
    public void successTxnOfHigherAmountToIncreaseMPABalanceOfUser() throws Exception {
        String txnAmount = "80000.00";
        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO =  initiateAndPTCNative(pwpHybrid,user,txnAmount,false,PayMethodType.CREDIT_CARD,"");
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateOrderId(orderDTO.getORDER_ID())
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateStatus("TXN_SUCCESS")
                .assertAll();
    }

    private void assertPWPPrefEnabled(Constants.MerchantType merchantType,String... payMode) {

        String preference = "PAY_WITH_PAYTM";
        pre_requisite:
        {
            // User should be logged in for PWP flow
            PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference, "Y"); //2
            PWPHelpers.getPWPEnabledPayMode(preference,merchantType.getId(),payMode);

        }
    }
    private void assertRefundAllowed(Constants.MerchantType merchantType)
    {
        PGPHelpers.validateRefundAllowedWithChecksum(merchantType.getId());
    }

    private void assertSMSPrefEnabled(Constants.MerchantType merchantType)
    {
        MerchExtendedInfo merchExtendedInfo = GetMerchantExtendedInfo.executeMercExtendedInfo(merchantType.getId());
        String merchantSMS = merchExtendedInfo.getExtendedInfo().getMerchCommPref();
        int userSMS = merchExtendedInfo.getExtendedInfo().getCustCommPref();
        Assertions.assertThat(merchantSMS).as("Can not send SMS to merchant as merchCommPref value is: " +merchantSMS).isEqualTo("19"); //SMS enabled
        Assertions.assertThat(userSMS).as("User SMS should be disabled but found : " +userSMS).isEqualTo(39); //SMS disabled
    }

    private void assertRefundSuccessNotifyPeon(Constants.MerchantType merchantType)
    {
        String preference = "S2S_REFUND_NOTIFY";
        PGPHelpers.validate_MerchantPreference(merchantType.getId(), preference, "Y");
    }

    public OrderDTO initiateAndPTCNative(Constants.MerchantType merchantType, User user, String txnAmount,boolean isNativePlus, PayMethodType payMethodType,String paymentFlow)
        {
            PaymentDTO paymentDTO = new PaymentDTO();
            return initiateAndPTCNative(merchantType,user,txnAmount,paymentDTO,isNativePlus,payMethodType,paymentFlow);
        }

        //To be moved out of this class to common helper or util once all the cases will run fine TODO

        public OrderDTO initiateAndPTCNative(Constants.MerchantType merchantType, User user, String txnAmount, PaymentDTO paymentDTO,boolean isNativePlus, PayMethodType payMethodType,String paymentFlow) {
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), merchantType)
                .setTxnValue(txnAmount)
                .setpeonUrl("https://automation-pg-ext.paytm.in/mockbank/peon")
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO;
        switch (payMethodType) {
            case CREDIT_CARD:
            case DEBIT_CARD:
            case BALANCE:
                orderDTO= new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethodType)
                        .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                        .setStoreInstrument("1")
                        .setPaymentFlow(paymentFlow)
                        .build();
                break;
            case NET_BANKING:
                orderDTO= new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethodType)
                        .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                        .setPaymentFlow(paymentFlow)
                        .setChannelCode("ICICI")
                        .build();
                break;
            case UPI:
                orderDTO= new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethodType)
                        .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                        .setPaymentFlow(paymentFlow)
                        .setPayerAccount(new PaymentDTO().getVpa())
                        .build();
                break;
            case EMI:
                    String channelCode = paymentDTO.getBankName();
                    FetchEMIDetailRequest fetchEMIDetailRequest = new FetchEMIDetailRequest("SSO", user.ssoToken(), channelCode, merchantType.getId(), paymentDTO.getPaymentType());
                    JsonPath fetchEMIDetailCC = new FetchEMIDetail(fetchEMIDetailRequest, merchantType.getId()).execute().jsonPath();
                    String planIdCC = fetchEMIDetailCC.get("body.emiDetail.emiChannelInfos[0].planId").toString();

                orderDTO= new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethodType)
                        .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                        .setPaymentFlow(paymentFlow)
                        .setPlanId(planIdCC)
                        .setChannelCode(channelCode)
                        .build();
                break;
            case PAYTM_DIGITAL_CREDIT:
                orderDTO= new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethodType)
                        .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                        .setPaymentFlow(paymentFlow)
                        .setMpin(new PaymentDTO().getPasscode())
                        .build();
                break;
            case PPBL:
                orderDTO= new OrderFactory.Native(merchantType, initTxnDTO.orderFromBody(), txnToken,paymentDTO, payMethodType)
                        .setTXN_AMOUNT(initTxnDTO.txnAmountFromBody())
                        .setPaymentFlow(paymentFlow)
                        .setMpin("1234")
                        .build();
                break;

            default:
                throw new RuntimeException("PayMode is not supported " + payMethodType.toString());

        }
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);
        orderDTO.setMerchantKey(merchantType.getKey());
        return orderDTO;
    }

//All the merchants and user exposed API needs to be tested

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Native transaction via CC.", groups = "P0")
    public void PWP_01_successfulPWPOnlyCCNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "CC";

        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));

        OrderDTO orderDTO =  initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.CREDIT_CARD,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Native transaction via DC.", groups = "P0")
    public void PWP_02_successfulPWPOnlyDCNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "DC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO =  initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.DEBIT_CARD,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }


    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Native transaction via NB.", groups = "P0")
    public void PWP_03_successfulPWPOnlyNBNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "NB";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO =  initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.NET_BANKING,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Native transaction via PPBL", groups = "P0")
    public void PWP_04_successfulPWPOnlyPPBLNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "NB";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.PPBL);

        OrderDTO orderDTO =  initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.PPBL,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    //UPI refund is supposed to be offline
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Native transaction via UPI", groups = "P0")
    public void PWP_05_successfulPWPOnlyUPINative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "UPI";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);

        OrderDTO orderDTO =  initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.UPI,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Native transaction via PPI Wallet", groups = "P0")
    public void PWP_06_successfulPWPOnlyPPINative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "PPI";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);
        User user = userManager.getForWrite(Label.PPBL);

        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount));
        OrderDTO orderDTO =  initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.BALANCE,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
    }

    //Paytm CC refund is supposed to be offline
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP transaction via Paytm CC", groups = "P0")
    public void PWP_07_successfulPWPOnlyPaytmCC(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode = "PAYTM_DIGITAL_CREDIT";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.POSTPAID);

        OrderDTO orderDTO =  initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.PAYTM_DIGITAL_CREDIT,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP transaction to assert saved PWP Card", groups = "P0")
    public void PWP_08_successfulPWPOnlyPaytmToSaveCard(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode = "DC";
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.DEBIT_CARD,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

        FetchPaymentOptionsDTO fetchPaymentOptionsDTO = new FetchPaymentOptionsDTO.Builder("SSO",user.ssoToken())
                .setMid(pwpDefaultPg2Rtdd.getId())
                .build();
        FetchPaymentOption fetchPaymentOption = new FetchPaymentOption(pwpDefaultPg2Rtdd.getId(),
                CommonHelpers.generateOrderId(), fetchPaymentOptionsDTO);
        JsonPath fetchPaymentOptionsJson = fetchPaymentOption.execute().jsonPath();
        Assertions.assertThat(fetchPaymentOptionsJson.getString("body.merchantPayOption.savedInstruments[0]")).isNotEmpty();

    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP transaction via Saved Card", groups = "P0")
    public void PWP_09_successfulPWPOnlyPaytmSavedCard(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode = "DC";
        PaymentDTO paymentDTO = new PaymentDTO();
        assertPWPPrefEnabled(pwpDefaultPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpDefaultPg2Rtdd);
        assertRefundAllowed(pwpDefaultPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpDefaultPg2Rtdd);

        User user = userManager.getForWrite(Label.BASIC);
        SavedCardHelpers.deleteSavedCard(user);
        SavedCardHelpers.addCard(user, paymentDTO.getExpMonth(),paymentDTO.getExpYear(),paymentDTO.getDebitCardNumber());
        paymentDTO.setSavedCardId(SavedCardHelpers.getSavedCardId(user, 0)).setCvvNumber("123");

        OrderDTO orderDTO = initiateAndPTCNative(pwpDefaultPg2Rtdd,user,txnAmount,paymentDTO,isNativePlus,PayMethodType.DEBIT_CARD,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpDefaultPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }

//Hybrid Txn

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via CC.", groups = "P0")
    public void PWP_10_successfulPWPHybridCCNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "CC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO =  initiateAndPTCNative(pwpHybridPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.CREDIT_CARD,"HYBRID");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via DC.", groups = "P0")
    public void PWP_11_successfulPWPHybridDCNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "DC";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = initiateAndPTCNative(pwpHybridPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.DEBIT_CARD,"HYBRID");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    //NB refund is offline
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via NB.", groups = "P0")
    public void PWP_12_successfulPWPHybridNBNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "NB";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = initiateAndPTCNative(pwpHybridPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.NET_BANKING,"HYBRID");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }


    //UPI Refund is pending for Hybrid txn, to be addded in manual regression
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via UPI", groups = "P0")
    public void PWP_13_successfulPWPHybridUPINative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "UPI";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = initiateAndPTCNative(pwpHybridPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.UPI,"HYBRID");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
       /* PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybrid);*/
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via EMI", groups = "P0")
    public void PWP_14_successfulPWPHybridEMINative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "EMI";
        assertPWPPrefEnabled(pwpHybrid,payMode);
        assertSMSPrefEnabled(pwpHybrid);
        assertRefundAllowed(pwpHybrid);
        assertRefundSuccessNotifyPeon(pwpHybrid);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setPaymentType("CREDIT_CARD");
        OrderDTO orderDTO = initiateAndPTCNative(pwpHybrid,user,txnAmount,paymentDTO,isNativePlus,PayMethodType.EMI,"HYBRID");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybrid);
    }

    //EMI DC refund is offline
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via EMI DC", groups = "P0")
    public void PWP_15_successfulPWPHybridEMIDCNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "EMI_DC";
        assertPWPPrefEnabled(pwpHybrid,payMode);
        assertSMSPrefEnabled(pwpHybrid);
        assertRefundAllowed(pwpHybrid);
        assertRefundSuccessNotifyPeon(pwpHybrid);
        User user = userManager.getForWrite(Label.EMIDC, Label.MGV); //Other user is not working
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER);
        paymentDTO.setBankName("ICICI").setPaymentType("DEBIT_CARD");
        OrderDTO orderDTO = initiateAndPTCNative(pwpHybrid,user,txnAmount,paymentDTO,isNativePlus,PayMethodType.EMI,"");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybrid);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
    }

    //Paytm CC refund is offline to be added by manual regression
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via Paytm Digital Card", groups = "P0")
    public void PWP_16_successfulPWPHybridPaytmCCNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "PAYTM_DIGITAL_CREDIT";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.POSTPAID);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = initiateAndPTCNative(pwpHybridPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.PAYTM_DIGITAL_CREDIT,"HYBRID");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
      /*  PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybrid);*/
    }

    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via PPBL", groups = "P0")
    public void PWP_17_successfulPWPHybridPPBLNative(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "NB";
        assertPWPPrefEnabled(pwpHybridPg2Rtdd,payMode);
        assertSMSPrefEnabled(pwpHybridPg2Rtdd);
        assertRefundAllowed(pwpHybridPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpHybridPg2Rtdd);
        User user = userManager.getForWrite(Label.PPBL);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        OrderDTO orderDTO = initiateAndPTCNative(pwpHybridPg2Rtdd,user,txnAmount,isNativePlus,PayMethodType.PPBL,"HYBRID");

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpHybridPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,pwpHybridPg2Rtdd);
    }

    @Issue("PGP-27432")
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via Mutual Fund merchant", groups = Group.Status.BUG)
    public void PWP_18_successfulPWPHybridMutualFund(@Optional("false") Boolean isNativePlus) throws Exception {
        String payMode= "DC";
        assertPWPPrefEnabled(pwpMutualFund,payMode);
        assertSMSPrefEnabled(pwpMutualFund);
        assertRefundAllowed(pwpMutualFund);
        assertRefundSuccessNotifyPeon(pwpMutualFund);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpMutualFund)
                .setRequestType("NATIVE_MF")
                .setMerchantKey(Constants.MerchantType.MUTUAL_FUND_AGGR.getKey())
                .setAggrMid(Constants.MerchantType.MUTUAL_FUND_AGGR.getId())
                .setTxnValue(String.valueOf(txnAmount)).build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        OrderDTO orderDTO = new OrderFactory.Native(pwpMutualFund, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(String.valueOf(txnAmount))
                .setAggMid(Constants.MerchantType.MUTUAL_FUND_AGGR.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO,isNativePlus);

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,Constants.MerchantType.MUTUAL_FUND_AGGR);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,Constants.MerchantType.MUTUAL_FUND_AGGR);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);
        PWPHelpers.validateSuccessRefundPWPTxn(orderDTO);
        PWPHelpers.validateSuccessRefundNotifyPeonPWPTxn(orderDTO,Constants.MerchantType.MUTUAL_FUND_AGGR);
    }

    //NB refund is offline
    @Parameters({"isNativePlus"})
    @Test(description = "Validate successful PWP Hybrid Native transaction via retry", groups = "P0")
    public void PWP_19_successfulPWPHybridPPBLNativeRetry(@Optional("false") Boolean isNativePlus) throws Exception {
        assertPWPPrefEnabled(pwpRetryPg2Rtdd,"CC","DC","NB");
        assertSMSPrefEnabled(pwpRetryPg2Rtdd);
        assertRefundAllowed(pwpRetryPg2Rtdd);
        assertRefundSuccessNotifyPeon(pwpRetryPg2Rtdd);
        User user = userManager.getForWrite(Label.BASIC);
        WalletHelpers.modifyBalance(user,Double.valueOf(txnAmount) -1.00);
        SavedCardHelpers.deleteSavedCard(user);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(), pwpRetryPg2Rtdd)
                .setTxnValue(txnAmount)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
        PaymentDTO paymentDTO  = new PaymentDTO();
        paymentDTO.setCreditCardNumber(PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN).setBankName("ICICI");

        OrderDTO orderDTO = new OrderFactory.Native(pwpRetryPg2Rtdd, initTxnDTO.orderFromBody(), txnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        Assertions.assertThat(responsePage.textMID().getText()).isEqualTo(orderDTO.getMID());
        Assertions.assertThat(responsePage.textOrderID().getText()).isEqualTo(orderDTO.getORDER_ID());
        Assertions.assertThat(responsePage.textStatus().getText()).isEqualTo("TXN_FAILURE");


        orderDTO = new OrderFactory.Native(pwpRetryPg2Rtdd, initTxnDTO.orderFromBody(), txnToken,paymentDTO, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .setTXN_AMOUNT(txnAmount)
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        responsePage.waitUntilLoads();

        PWPHelpers.validateSuccessResponsePWPTxn(orderDTO,pwpRetryPg2Rtdd);
        PWPHelpers.validateSuccessNativeTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPaymentStatusAPIPWPTxn(orderDTO,pwpRetryPg2Rtdd);
        PWPHelpers.validateSuccessTxnStatusPWPTxn(orderDTO);
        PWPHelpers.validateSuccessPeonPWPTxn(orderDTO);
        PWPHelpers.validateSuccessSMSViaPaytm(orderDTO);

    }


    }
