package scripts;

import com.paytm.api.TxnStatus;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.*;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.ExtendInfo;
import com.paytm.dto.NativeDTO.InitTxn.FeeDetails;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.OrderAdditionalInfo;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.Date;

public class FeeDetailsParameter extends PGPBaseTest {
    private final CheckoutPage checkoutPage = new CheckoutPage();

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object is passed in COP Flow for CC transaction")
    public void feeDetailsParameterPassed_01(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("1");
        feeDetails.setPaymodeBasedConvFees("2");
        feeDetails.setConvenienceFeeTax("3");
        feeDetails.setPlatformFees("4");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFees\":\"100\"");
        Assertions.assertThat(theia_facade_logs).contains("\"paymodeBasedConvFees\":\"200\"");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFeeTax\":\"300\"");
        Assertions.assertThat(theia_facade_logs).contains("\"platformFees\":\"400\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify 1 parameter ConvenienceFees of fee details object is passed in COP Flow for CC transaction")
    public void feeDetailsParameterPassed_02(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("1");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFees\":\"100\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify 1 parameter PaymodeBasedConvFees of fee details object is passed in COP Flow for CC transaction")
    public void feeDetailsParameterPassed_03(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setPaymodeBasedConvFees("2");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).contains("\"paymodeBasedConvFees\":\"200\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify 1 parameter ConvenienceFeeTax of fee details object is passed in COP Flow for CC transaction")
    public void feeDetailsParameterPassed_04(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFeeTax("3");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFeeTax\":\"300\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify 1 parameter PlatformFees of fee details object is passed in COP Flow for CC transaction")
    public void feeDetailsParameterPassed_05(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setPlatformFees("4");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).contains("\"platformFees\":\"400\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify 2 parameter ConvenienceFees & PaymodeBasedConvFees of fee details object is passed in COP Flow for CC transaction")
    public void feeDetailsParameterPassed_06(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("1");
        feeDetails.setPaymodeBasedConvFees("2");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFees\":\"100\"");
        Assertions.assertThat(theia_facade_logs).contains("\"paymodeBasedConvFees\":\"200\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify 2 parameter ConvenienceFeeTax & PlatformFees of fee details object is passed in COP Flow for CC transaction")
    public void feeDetailsParameterPassed_07(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFeeTax("3");
        feeDetails.setPlatformFees("4");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFeeTax\":\"300\"");
        Assertions.assertThat(theia_facade_logs).contains("\"platformFees\":\"400\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object is passed in COP Flow for CC transaction passed in decimal value")
    public void feeDetailsParameterPassed_08(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("0.01");
        feeDetails.setPaymodeBasedConvFees("0.2");
        feeDetails.setConvenienceFeeTax("0.03");
        feeDetails.setPlatformFees("4");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFees\":\"1\"");
        Assertions.assertThat(theia_facade_logs).contains("\"paymodeBasedConvFees\":\"20\"");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFeeTax\":\"3\"");
        Assertions.assertThat(theia_facade_logs).contains("\"platformFees\":\"400\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object not passed in COP Flow for CC transaction passed in negative value")
    public void feeDetailsParameterPassed_09(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("-1");
        feeDetails.setPaymodeBasedConvFees("-2");
        feeDetails.setConvenienceFeeTax("-3");
        feeDetails.setPlatformFees("-4");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object not passed in COP Flow for CC transaction passed in string value")
    public void feeDetailsParameterPassed_10(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("a");
        feeDetails.setPaymodeBasedConvFees("b");
        feeDetails.setConvenienceFeeTax("c");
        feeDetails.setPlatformFees("d");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object not passed in COP Flow for CC transaction passed in special value")
    public void feeDetailsParameterPassed_11(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("*");
        feeDetails.setPaymodeBasedConvFees("#");
        feeDetails.setConvenienceFeeTax("!");
        feeDetails.setPlatformFees("_");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object not passed in COP Flow for CC transaction passed in empty value")
    public void feeDetailsParameterPassed_12(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("");
        feeDetails.setPaymodeBasedConvFees("");
        feeDetails.setConvenienceFeeTax("");
        feeDetails.setPlatformFees("");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object not passed in COP Flow for CC transaction passed in null value")
    public void feeDetailsParameterPassed_13(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees(null);
        feeDetails.setPaymodeBasedConvFees(null);
        feeDetails.setConvenienceFeeTax(null);
        feeDetails.setPlatformFees(null);
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.CREDIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"paymodeBasedConvFees\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"convenienceFeeTax\"");
        Assertions.assertThat(theia_facade_logs).doesNotContain("\"platformFees\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object is passed in COP Flow for DC transaction")
    public void feeDetailsParameterPassed_14(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("1");
        feeDetails.setPaymodeBasedConvFees("2");
        feeDetails.setConvenienceFeeTax("3");
        feeDetails.setPlatformFees("4");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.DEBIT_CARD)
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFees\":\"100\"");
        Assertions.assertThat(theia_facade_logs).contains("\"paymodeBasedConvFees\":\"200\"");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFeeTax\":\"300\"");
        Assertions.assertThat(theia_facade_logs).contains("\"platformFees\":\"400\"");
    }

    @Feature("PGP-52693")
    @Owner("Shubham Soni")
    @Parameters({"isNativePlus"})
    @Test(description = "Verify all 4 parameter of fee details object is passed in COP Flow for NB transaction")
    public void feeDetailsParameterPassed_15(@Optional("false") Boolean isNativePlus) throws Exception {
        ExtendInfo extendInfo = new ExtendInfo();
        FeeDetails feeDetails = new FeeDetails() ;
        feeDetails.setConvenienceFees("1");
        feeDetails.setPaymodeBasedConvFees("2");
        feeDetails.setConvenienceFeeTax("3");
        feeDetails.setPlatformFees("4");
        extendInfo.setFeeDetails(feeDetails);
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, Constants.MerchantType.Alternate_ID_Onus)
                .setRequestType("NATIVE")
                .setExtendInfo(extendInfo)
                .setTxnValue("10.00")
                .build();

        String TxnToken = NativeHelpers.Validate_InitTxn(initTxnDTO);

        PaymentDTO paymentDTO = new PaymentDTO();
        OrderDTO orderDTO = new OrderFactory.Native(Constants.MerchantType.Alternate_ID_Onus, initTxnDTO.orderFromBody(), TxnToken, paymentDTO, PayMethodType.NET_BANKING)
                .setChannelCode("ICICI")
                .build();

        checkoutPage.createNativeOrder(orderDTO, isNativePlus);

        TxnStatus txnStatus = PGPHelpers.getTxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
        txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateSuccessResponse()
                .AssertAll();

        String theia_facade_logs= LogsValidationHelper.verifyLogsOnPod(PG2LogsValidationHelper.setEnvService.theia_facade,orderDTO.getORDER_ID(),"ACQUIRING_CREATE_ORDER_AND_PAY");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFees\":\"100\"");
        Assertions.assertThat(theia_facade_logs).contains("\"paymodeBasedConvFees\":\"200\"");
        Assertions.assertThat(theia_facade_logs).contains("\"convenienceFeeTax\":\"300\"");
        Assertions.assertThat(theia_facade_logs).contains("\"platformFees\":\"400\"");
    }

}
