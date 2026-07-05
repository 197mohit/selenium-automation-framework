package scripts.Native;


import com.paytm.api.GetPaymentStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.api.MappingService.VendorDetails;
import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.LogsValidationHelper;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.PG2LogsValidationHelper;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.GetPaymentStatusRequest.GetPaymentStatusDTO;
import com.paytm.dto.NativeDTO.InitTxn.AdditionalInfo;
import com.paytm.dto.NativeDTO.InitTxn.Amount;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.MutualFundFeedInfo;
import com.paytm.dto.NativeDTO.InitTxn.SplitExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.SplitInfo;
import com.paytm.dto.NativeDTO.InitTxn.SplitSettlementInfo;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.framework.conditions.SoftAssertion;
import com.paytm.pages.CheckoutPage;
import com.paytm.pages.ResponsePage;
import com.paytm.pg.merchant.CheckSumServiceHelper;
import com.paytm.utils.merchant.Peons;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

//Not validating checksum because of JIRA : PGP-28112

//All Mutual fund TC's to be moved here
public class NativeMF  extends PGPBaseTest {

    private final CheckoutPage checkoutPage = new CheckoutPage();
    Constants.MerchantType mutualFundMerchant = Constants.MerchantType.ONLINE_SETTLEMENT;
    com.paytm.appconstants.Constants.MerchantType aggMerchant = Constants.MerchantType.ONLINE_SETTLEMENT_AGG;

    //Transaction via AGG MID, Split on Child on amount

    @Test(description = "To test txn gets successful via AGG MID, split on CHILD via amount with NB & we get splitInfo in responsePage")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateSplitInfoNB(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        SplitInfo splitInfo = new SplitInfo().setMid(mutualFundMerchant.getId()).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setBANK_CODE("ICICI")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();

    }

    @Test(description = "To test txn gets successful with DC on AGG MID & split on child MID via amount  & we get splitInfo in responsePage")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26889")
    public void validateSplitInfoDC(@Optional("false") Boolean isNativePlus) {

        String txnAmount = "10";

        SplitInfo splitInfo = new SplitInfo().setMid(mutualFundMerchant.getId()).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
               // peon.isChecksumValid()
        );
        sAssert.eval();

    }


    @Test(description = "To test txn gets successful with UPI on AGG merchant & split on child merchant via amount & we get splitInfo in responsePage")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateSplitInfoUPI(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        SplitInfo splitInfo = new SplitInfo().setMid(mutualFundMerchant.getId()).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();


    }


    @Test(description = "To test txn gets successful with NB on AGG merchant & split on child & we get splitInfo in responsePage with percentage")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateSplitInfoNBPercentage(@Optional("false") Boolean isNativePlus) {

        String txnAmount = "10";

        SplitInfo splitInfo = new SplitInfo().setMid(mutualFundMerchant.getId()).setAmount(new Amount().setValue("").setPercentage("80"));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("PERCENTAGE").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setBANK_CODE("ICICI")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();


    }


    @Test(description = "To test txn gets successful with DC on AGG merchant & split on child  & we get splitInfo in responsePage with percentage")
    @Feature("PGP-21149")
    @Owner("Tarun")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    public void validateSplitInfoDCPercentage(@Optional("false") Boolean isNativePlus) {

        String txnAmount = "10";

        SplitInfo splitInfo = new SplitInfo().setMid(mutualFundMerchant.getId()).setAmount(new Amount().setValue("").setPercentage("80"));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("PERCENTAGE").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();



    }

    @Test(description = "To test txn gets successful with UPI on AGG merchant & split on child  & we get splitInfo in responsePage with percentage")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateSplitInfoUPIPercentage(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        SplitInfo splitInfo = new SplitInfo().setMid(mutualFundMerchant.getId()).setAmount(new Amount().setValue(txnAmount).setPercentage("80"));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("PERCENTAGE").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();


    }


    @Test(description = "To test txn gets successful with NB on merchant without Split info")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus", "isSplitTransaction"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateNBTxnWithoutSplit(@Optional("false") Boolean isNativePlus, @Optional("false") Boolean isSplitTransaction)  {

        String txnAmount = "10";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setBANK_CODE("ICICI")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .setAUTH_MODE("USRPWD")
                .setCardInfo("")
                .setSTORE_CARD("")
                .setChannelCode("ICICI")
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        PGPHelpers.validateSuccessResponsePage(orderDTO,aggMerchant,"ICICI","ICICI Bank","NB", isSplitTransaction);
        PGPHelpers.validateSuccessTxnStatus(orderDTO,"NB","ICICI Bank","ICICI");
        PGPHelpers.validateSuccessPeon(orderDTO,"ICICI","ICICI","NB");


    }



    @Test(description = "To test txn gets successful with DC on merchant without Split info")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateDCTxnWithoutSplit(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();



    }



    @Test(description = "To test txn gets successful with UPI on merchant without Split info")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateUPITxnWithoutSplit(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(aggMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();

    }

    @Test(description = "To test txn gets successful with NB on child merchant with split on child with amount")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26889")
    public void validateSplitInfoNBChild(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        VendorDetails vendorDetails = new VendorDetails(mutualFundMerchant.getId());
        String vendorId = vendorDetails.execute().jsonPath().getString("resultResp.paytmMerchantId");

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setBANK_CODE("ICICI")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();


    }


    @Test(description = "To test txn gets successful with DC on child merchant with split on child with amount")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26889")
    public void validateSplitInfoDCChild(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        VendorDetails vendorDetails = new VendorDetails(mutualFundMerchant.getId());
        String vendorId = vendorDetails.execute().jsonPath().getString("resultResp.paytmMerchantId");

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();


    }

    @Test(description = "To test txn gets successful with UPI & we get goods, shipping info & splitInfo in responsePage")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateSplitInfoUPIChild(@Optional("false") Boolean isNativePlus)  {
        String txnAmount = "10";

        VendorDetails vendorDetails = new VendorDetails(mutualFundMerchant.getId());
        String vendorId = vendorDetails.execute().jsonPath().getString("resultResp.paytmMerchantId");

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("8").setPercentage(""));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();
    }


    @Test(description = "To test txn gets successful with NB on child merchant with split on child with PERCENTAGE")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26889")
    public void validateSplitInfoNBChildPercentage(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        VendorDetails vendorDetails = new VendorDetails(mutualFundMerchant.getId());
        String vendorId = vendorDetails.execute().jsonPath().getString("resultResp.paytmMerchantId");

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("").setPercentage("80"));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("PERCENTAGE").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.NET_BANKING)
                .setBANK_CODE("ICICI")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("NB")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("NB")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("NB"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();


    }


    @Test(description = "To test txn gets successful with DC on child merchant with split on child with PERCENTAGE")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Owner("Tarun")
    @Description("Automation JIRA : PGP-26889")
    public void validateSplitInfoDCChildPERCENTAGE(@Optional("false") Boolean isNativePlus)  {

        String txnAmount = "10";

        VendorDetails vendorDetails = new VendorDetails(mutualFundMerchant.getId());
        String vendorId = vendorDetails.execute().jsonPath().getString("resultResp.paytmMerchantId");

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("").setPercentage("80"));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("PERCENTAGE").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("DC")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("DC"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();


    }

    @Test(description = "To test txn gets successful with UPI & we get goods, shipping info & splitInfo in responsePage PERCENTAGE")
    @Feature("PGP-21149")
    @Parameters({"isNativePlus"})
    @Description("Automation JIRA : PGP-26889")
    @Owner("Tarun")
    public void validateSplitInfoUPIChildPERCENTAGE(@Optional("false") Boolean isNativePlus)  {
        String txnAmount = "10";

        VendorDetails vendorDetails = new VendorDetails(mutualFundMerchant.getId());
        String vendorId = vendorDetails.execute().jsonPath().getString("resultResp.paytmMerchantId");

        SplitInfo splitInfo = new SplitInfo().setMid(vendorId).setAmount(new Amount().setValue("").setPercentage("80"));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("PERCENTAGE").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@paytm")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("UPI")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                //   .validateCheckSum(aggMerchant.getKey())
                .assertAll();

        TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.executeUntilNotPending();
        txnStatus
                .validateRespCode("01")
                .validateRespMsg("Txn Success")
                .validateMid(orderDTO.getMID())
                .validatePaymentMode("UPI")
                .validateSplitSettlementInfo(splitSettlementInfo.getSplitMethod())
                .AssertAll();

        Peons peons = new Peons();
        com.paytm.utils.merchant.Peon peon = peons.getAt(orderDTO.getORDER_ID());
        SoftAssertion sAssert = new SoftAssertion();
        sAssert.apply(
                peon.keys().containsExactly("CURRENCY", "GATEWAYNAME", "RESPMSG", "BANKNAME", "PAYMENTMODE", "CUSTID", "MID", "MERC_UNQ_REF", "RESPCODE", "TXNID", "TXNAMOUNT", "ORDERID", "STATUS", "BANKTXNID", "TXNDATETIME", "TXNDATE", "CHECKSUMHASH","splitSettlementInfo"),
                peon.mId().equals(orderDTO.getMID()),
                peon.payMode().equals("UPI"),
                peon.respCode().equals("01"),
                peon.status().equals("TXN_SUCCESS")
                // peon.isChecksumValid()
        );
        sAssert.eval();
    }


    @Feature("PGP-34005")
    @Parameters({"isNativePlus"})
    @Owner(Constants.Owner.HARSHITA)
    @Test(description = "Verify whether checksum returned in final response is successfully validated against aggregator mid key in case of a MF transaction")
    public void validateChecksum(@Optional("false") Boolean isNativePlus) throws Exception {

        String txnAmount = "10.00";
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, mutualFundMerchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMerchant.getId())
                .setMerchantKey(aggMerchant.getKey())
                .build();
        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(mutualFundMerchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.DEBIT_CARD)
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMerchant.getId())
                .build();
        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();

        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateMid(orderDTO.getMID())
                .validateOrderId(orderDTO.getORDER_ID())
                .validatePaymentMode("DC")
                .validateRespCode("01")
                .validateStatus("TXN_SUCCESS")
                .validateCheckSum(aggMerchant.getKey())
                .assertAll();
    }

/*
      //additionalInfo (ref1..ref12) set directly on Body via setAdditionalInfo(...)
      @Test(description = "To verify NATIVE_MF init txn with additionalInfo set directly on Body via setAdditionalInfo")
      @Feature("PGP-21149")
      @Parameters({"isNativePlus"})
      @Owner("Akshat")
      public void validateAdditionalInfoOnBodyNB(@Optional("false") Boolean isNativePlus) {
  
          String txnAmount = "10";
  
          InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, aggMerchant)
                  .setTxnValue(txnAmount)
                  .setRequestType("NATIVE_MF")
                  .setAggrMid(aggMerchant.getId()
                  .setMerchantKey(aggMerchant.getKey())
                  .build();
  
          AdditionalInfo additionalInfo = new AdditionalInfo();
          additionalInfo.setRef1("ref1_value");
          additionalInfo.setRef2("ref2_value");
          additionalInfo.setRef3("ref3_value");
          additionalInfo.setRef4("ref4_value");
          additionalInfo.setRef5("ref5_value");
          additionalInfo.setRef6("ref6_value");
          additionalInfo.setRef7("ref7_value");
          additionalInfo.setRef8("ref8_value");
          additionalInfo.setRef9("ref9_value");
          additionalInfo.setRef10("ref10_value");
          additionalInfo.setRef11("ref11_value");
          additionalInfo.setRef12("ref12_value");
          initTxnDTO.getBody().setAdditionalInfo(additionalInfo);
  
          String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);
      }
*/
  
      //additionalInfo (ref1..ref12) passed through the InitTxnDTO builder chain
      @Test(description = "To verify for NATIVE_MF transaction additionalInfo passed in COP ")
      @Feature("PG-7131|PG-7197")
      @Parameters({"isNativePlus"})
      @Owner("Akshat")
      public void validateAdditionalInfoInCOP_MF(@Optional("false") Boolean isNativePlus) {
  
          String txnAmount = "10";
  
          AdditionalInfo additionalInfo = new AdditionalInfo()
                  .setRef1("ref1_value").setRef2("ref2_value").setRef3("ref3_value").setRef4("ref4_value")
                  .setRef5("ref5_value").setRef6("ref6_value").setRef7("ref7_value").setRef8("ref8_value")
                  .setRef9("ref9_value").setRef10("ref10_value").setRef11("ref11_value").setRef12("ref12_value");

          Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
          Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;

          InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                  .setTxnValue(txnAmount)
                  .setRequestType("NATIVE_MF")
                  .setAggrMid(aggMid.getId())
                  .setMerchantKey(merchant.getKey())
                  .setAdditionalInfo(additionalInfo)
                  .build();
  
          String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

          OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
          .setPayerAccount("test@ptyes")
          .setTXN_AMOUNT(txnAmount)
          .setAggMid(aggMid.getId())
          .build();

         checkoutPage.createNativeOrder(orderDTO, isNativePlus);
         String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
         Assertions.assertThat(logs).contains("mutualFundFeedInfo");
         Assertions.assertThat(logs).contains("ref1_value").contains("ref2_value").contains("ref3_value").contains("ref4_value").contains("ref5_value").contains("ref6_value").contains("ref7_value").contains("ref8_value").contains("ref9_value").contains("ref10_value").contains("ref11_value").contains("ref12_value");

      }

      @Test(description = "To verify for NATIVE_MF transaction additionalInfo with splitSettlementInfo (AMOUNT) for single child MID passed in COP")
      @Feature("PG-7131|PG-7197")
      @Parameters({"isNativePlus"})
      @Owner("Akshat")
      public void validateAdditionalInfoWithSplitSettlementInfoInCOP_MF(@Optional("false") Boolean isNativePlus) {

          String txnAmount = "10";

          AdditionalInfo additionalInfo = new AdditionalInfo()
                  .setRef1("PAYTXN20260602001").setRef2("ICICI").setRef3("INT_REF_LUMP_01").setRef4("AMC03")
                  .setRef5("12345").setRef6("UCC11223344").setRef7("EQUITY_LARGE_CAP").setRef8("CLIENT_LUMP_001")
                  .setRef9("ICIC0001234").setRef10("601234567890").setRef11("L").setRef12("upir");

          Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
          Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;

          SplitInfo splitInfo = new SplitInfo()
                  .setMid(aggMid.getId())
                  .setAmount(new Amount().setValue("10").setPercentage(""))
                  .setExtendInfo(new SplitExtendInfo().setMutualFundFeedInfo(additionalInfo));
          SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

          InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                  .setTxnValue(txnAmount)
                  .setRequestType("NATIVE_MF")
                  .setAggrMid(aggMid.getId())
                  .setMerchantKey(merchant.getKey())
                  .setSplitSettlementInfo(splitSettlementInfo)
                  .build();

          String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

          OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                  .setPayerAccount("test@ptyes")
                  .setTXN_AMOUNT(txnAmount)
                  .setAggMid(aggMid.getId())
                  .build();

          checkoutPage.createNativeOrder(orderDTO, isNativePlus);
          String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
          Assertions.assertThat(logs).contains("mutualFundFeedInfo");
          Assertions.assertThat(logs).contains("PAYTXN20260602001").contains("ICICI").contains("INT_REF_LUMP_01").contains("AMC03")
                  .contains("UCC11223344").contains("EQUITY_LARGE_CAP").contains("CLIENT_LUMP_001")
                  .contains("ICIC0001234").contains("601234567890").contains("upir");

      }

    //mutualFundFeedInfo passed through the InitTxnDTO builder chain
    @Test(description = "To verify for NATIVE_MF transaction mutualFundFeedInfo passed in COP ")
    @Feature("PG-7131|PG-7197")
    @Parameters({"isNativePlus"})
    @Owner("Akshat")
    public void validateMutualFundFeedInfoInCOP_MF(@Optional("false") Boolean isNativePlus) {

        String txnAmount = "10";

        MutualFundFeedInfo mutualFundFeedInfo = new MutualFundFeedInfo();

        Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
        Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMid.getId())
                .setMerchantKey(merchant.getKey())
                .setMutualFundFeedInfo(mutualFundFeedInfo
                        .setMfTxnId("PAYTXN20260602001")
                        .setMfBseBankCode("ICICI")
                        .setMfInternalRef("INT_REF_LUMP_01")
                        .setMfAmcCode("AMC03")
                        .setMfBseMemberId("12345")
                        .setMfUcc("UCC11223344")
                        .setMfSchemeCategoryId("EQUITY_LARGE_CAP")
                        .setMfClientTxnId("CLIENT_LUMP_001")
                        .setMfIfsc("ICIC0001234")
                        .setMfBankAccount("601234567890")
                        .setMfInvestmentType("L")
                        .setMfUpir("upir"))
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
        .setPayerAccount("test@ptyes")
        .setTXN_AMOUNT(txnAmount)
        .setAggMid(aggMid.getId())
        .build();

       checkoutPage.createNativeOrder(orderDTO, isNativePlus);
       String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
       Assertions.assertThat(logs).contains("mutualFundFeedInfo");
       Assertions.assertThat(logs).contains("PAYTXN20260602001").contains("INT_REF_LUMP_01").contains("AMC03").contains("UCC11223344").contains("EQUITY_LARGE_CAP").contains("CLIENT_LUMP_001").contains("ICIC0001234").contains("601234567890");

    }

    @Test(description = "To verify for NATIVE_MF transaction mutualFundFeedInfo with splitSettlementInfo (AMOUNT) for single child MID passed in COP")
    @Feature("PG-7131|PG-7197")
    @Parameters({"isNativePlus"})
    @Owner("Akshat")
    public void validateMutualFundFeedInfoWithSplitSettlementInfoInCOP_MF(@Optional("false") Boolean isNativePlus) {

        String txnAmount = "10";

        MutualFundFeedInfo mutualFundFeedInfo = new MutualFundFeedInfo()
                .setMfTxnId("PAYTXN20260602001")
                .setMfBseBankCode("ICICI")
                .setMfInternalRef("INT_REF_LUMP_01")
                .setMfAmcCode("AMC03")
                .setMfBseMemberId("12345")
                .setMfUcc("UCC11223344")
                .setMfSchemeCategoryId("EQUITY_LARGE_CAP")
                .setMfClientTxnId("CLIENT_LUMP_001")
                .setMfIfsc("ICIC0001234")
                .setMfBankAccount("601234567890")
                .setMfInvestmentType("L")
                .setMfUpir("upir");

        Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
        Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;

        SplitInfo splitInfo = new SplitInfo()
                .setMid(aggMid.getId())
                .setAmount(new Amount().setValue("10").setPercentage(""))
                .setMutualFundFeedInfo(mutualFundFeedInfo);
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMid.getId())
                .setMerchantKey(merchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        String txnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        OrderDTO orderDTO = new OrderFactory.Native(merchant, initTxnDTO.orderFromBody(), txnToken, PayMethodType.UPI)
                .setPayerAccount("test@ptyes")
                .setTXN_AMOUNT(txnAmount)
                .setAggMid(aggMid.getId())
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);
        String logs = LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade, initTxnDTO.orderFromBody(), "ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(logs).contains("mutualFundFeedInfo");
        Assertions.assertThat(logs).contains("PAYTXN20260602001").contains("INT_REF_LUMP_01").contains("AMC03").contains("UCC11223344").contains("EQUITY_LARGE_CAP").contains("CLIENT_LUMP_001").contains("ICIC0001234").contains("601234567890");

    }

    @Test(description = "To verify when both mutualFundFeedInfo and additionalInfo are passed initTransaction fails")
    @Feature("PG-7131|PG-7197")
    @Parameters({"isNativePlus"})
    @Owner("Akshat")
    public void validateInitiateTxnFailure_MF(@Optional("false") Boolean isNativePlus) {

        String txnAmount = "10";

        MutualFundFeedInfo mutualFundFeedInfo = new MutualFundFeedInfo()
                .setMfTxnId("PAYTXN20260602001")
                .setMfBseBankCode("ICICI")
                .setMfInternalRef("INT_REF_LUMP_01")
                .setMfAmcCode("AMC03")
                .setMfBseMemberId("12345")
                .setMfUcc("UCC11223344")
                .setMfSchemeCategoryId("EQUITY_LARGE_CAP")
                .setMfClientTxnId("CLIENT_LUMP_001")
                .setMfIfsc("ICIC0001234")
                .setMfBankAccount("601234567890")
                .setMfInvestmentType("L")
                .setMfUpir("upir");
        AdditionalInfo additionalInfo = new AdditionalInfo()
                .setRef1("ref1_value").setRef2("ref2_value").setRef3("ref3_value").setRef4("ref4_value")
                .setRef5("ref5_value").setRef6("ref6_value").setRef7("ref7_value").setRef8("ref8_value")
                .setRef9("ref9_value").setRef10("ref10_value").setRef11("ref11_value").setRef12("ref12_value");

        Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
        Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMid.getId())
                .setMerchantKey(merchant.getKey())
                .setMutualFundFeedInfo(mutualFundFeedInfo)
                .setAdditionalInfo(additionalInfo)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        InitTxnResponseDTO responseDTO = NativeHelpers.convertRespToObject(response, InitTxnResponseDTO.class);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("1001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Request parameters are not valid");

    }

    @Test(description = "To verify when mutualFundFeedInfo, additionalInfo and splitSettlementInfo are passed initTransaction fails")
    @Feature("PG-7131|PG-7197")
    @Parameters({"isNativePlus"})
    @Owner("Akshat")
    public void validateInitiateTxnFailureWithSplitSettlementInfo_MF(@Optional("false") Boolean isNativePlus) {

        String txnAmount = "10";

        MutualFundFeedInfo mutualFundFeedInfo = new MutualFundFeedInfo()
                .setMfTxnId("PAYTXN20260602001")
                .setMfBseBankCode("ICICI")
                .setMfInternalRef("INT_REF_LUMP_01")
                .setMfAmcCode("AMC03")
                .setMfBseMemberId("12345")
                .setMfUcc("UCC11223344")
                .setMfSchemeCategoryId("EQUITY_LARGE_CAP")
                .setMfClientTxnId("CLIENT_LUMP_001")
                .setMfIfsc("ICIC0001234")
                .setMfBankAccount("601234567890")
                .setMfInvestmentType("L")
                .setMfUpir("upir");
        AdditionalInfo additionalInfo = new AdditionalInfo()
                .setRef1("ref1_value").setRef2("ref2_value").setRef3("ref3_value").setRef4("ref4_value")
                .setRef5("ref5_value").setRef6("ref6_value").setRef7("ref7_value").setRef8("ref8_value")
                .setRef9("ref9_value").setRef10("ref10_value").setRef11("ref11_value").setRef12("ref12_value");

        Constants.MerchantType merchant = Constants.MerchantType.Native_MF_ParentA;
        Constants.MerchantType aggMid = Constants.MerchantType.Native_MF_ChildA1;

        SplitInfo splitInfo = new SplitInfo()
                .setMid(aggMid.getId())
                .setAmount(new Amount().setValue("10").setPercentage(""))
                .setMutualFundFeedInfo(mutualFundFeedInfo)
                .setExtendInfo(new SplitExtendInfo().setMutualFundFeedInfo(additionalInfo));
        SplitSettlementInfo splitSettlementInfo = new SplitSettlementInfo().setSplitMethod("AMOUNT").setSplitInfo(new SplitInfo[]{splitInfo});

        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, merchant)
                .setTxnValue(txnAmount)
                .setRequestType("NATIVE_MF")
                .setAggrMid(aggMid.getId())
                .setMerchantKey(merchant.getKey())
                .setSplitSettlementInfo(splitSettlementInfo)
                .build();

        InitTxn initTxn = new InitTxn(initTxnDTO);
        Response response = initTxn.execute();
        InitTxnResponseDTO responseDTO = NativeHelpers.convertRespToObject(response, InitTxnResponseDTO.class);

        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultStatus())
                .as("resultStatus mismatch")
                .isEqualTo("F");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultCode())
                .as("resultCode mismatch")
                .isEqualTo("1001");
        Assertions.assertThat(responseDTO.getBody().getResultInfo().getResultMsg())
                .as("resultMsg mismatch")
                .isEqualToIgnoringCase("Request parameters are not valid");

    }



}
