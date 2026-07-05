package scripts.coft.theia;

import com.paytm.api.TxnStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.apphelpers.SavedCardHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.PaymentDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import org.testng.annotations.Test;

public class PTCWithTin extends PGPBaseTest {

  Constants.MerchantType coftMerchant = Constants.MerchantType.COFT_MERCHANT;


  @Test(description = "Successful PTC with Tin")
  public void successfulPTCWithTin() throws Exception {

    User user = userManager.getForRead(Label.BASIC);
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.VISA_COFT_CARD_NUMBER);
    String tin= SavedCardHelpers.getTin();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
        .setTxnValue("2")
        .setSsoToken(user.ssoToken())
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(tin+"||"+paymentDTO.getCvvNumber()+"|")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(coftMerchant.getId(),initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();

  }

  @Test(description = "Successful PTC with Debit card Tin")
  public void successfulPTCWithDebitTin() throws Exception {

    User user = userManager.getForRead(Label.BASIC);
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.DEBIT_CARD_NUMBER);
    String tin= SavedCardHelpers.getTin();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
        .setTxnValue("2")
        .setSsoToken(user.ssoToken())
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(tin+"||"+paymentDTO.getCvvNumber()+"|")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(coftMerchant.getId(),initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("DC").AssertAll();

  }

  @Test(description = "Successful PTC with Master card Tin")
  public void successfulPTCWithMasterCardTin() throws Exception {

    User user = userManager.getForRead(Label.BASIC);
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.MASTER_CREDIT_CARD);
    String tin= SavedCardHelpers.getTin();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
        .setTxnValue("2")
        .setSsoToken(user.ssoToken())
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(tin+"||"+paymentDTO.getCvvNumber()+"|")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(coftMerchant.getId(),initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();

  }

  @Test(description = "Successful PTC with Diners card Tin")
  public void successfulPTCWithDinersCardTin() throws Exception {

    User user = userManager.getForRead(Label.BASIC);
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.DINERS_CARD_NUMBER);
    String tin= SavedCardHelpers.getTin();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
        .setTxnValue("2")
        .setSsoToken(user.ssoToken())
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(tin+"||"+paymentDTO.getCvvNumber()+"|")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(coftMerchant.getId(),initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
  }

  @Test(description = "Successful PTC with Diners card Tin")
  public void successfulPTCWithRupayCardTin() throws Exception {

    User user = userManager.getForRead(Label.BASIC);
    PaymentDTO paymentDTO = new PaymentDTO();
    SavedCardHelpers.addCard(user, PaymentDTO.EXP_MONTH, PaymentDTO.EXP_YEAR, PaymentDTO.RUPAY_CARD_NUMBER);
    String tin= SavedCardHelpers.getTin();

    //Initiate Transaction
    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(null, coftMerchant)
        .setTxnValue("2")
        .setSsoToken(user.ssoToken())
        .build();
    InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);

    //V1 PTC
    ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
        coftMerchant.getId(), initTxnResponse.getBody().getTxnToken(), initTxnDTO.getBody().getOrderId())
        .setPaymentMode("CREDIT_CARD")
        .setAuthMode("otp")
        .setCardInfo(tin+"||"+paymentDTO.getCvvNumber()+"|")
        .build();

    ProcessTxnV1Response processTxnV1Response = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
    NativeHelpers.submitJsonFormInBrowser(processTxnV1Response.toString());
    TxnStatus txnStatus = new TxnStatus(coftMerchant.getId(),initTxnDTO.getBody().getOrderId());
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY).
        validateStatus("TXN_SUCCESS").
        validatePaymentMode("CC").AssertAll();
  }
}
