package scripts.api.Instaproxy;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.CommonHelpers;
import com.paytm.apphelpers.NativeHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import scripts.api.NetBanking.NBPG2;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.NativeDTO.InitTxn.response.InitTxnResponseDTO;
import com.paytm.dto.processTransactionV1.ProcessTxnV1Request;
import com.paytm.dto.processTransactionV1.response.ProcessTxnV1Response;
import com.paytm.pages.*;
import io.qameta.allure.Owner;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import java.util.Date;
import static com.paytm.appconstants.Constants.Owner.*;


public class NetBankingTest extends PGPBaseTest{
    NBPG2 nbPG2 = new NBPG2();

    @Owner(MANISH_MISHRA)
    @Test(description = "Validate Success HDFC NB transaction")
    public void validateSuccessHDFC_NBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.INSTA_NB_MID.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),  Constants.MerchantType.INSTA_NB_MID)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        nbPG2.Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.HDFC_ONLY.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        NativeHelpers.submitJsonFormInBrowser(ptcResponse.toString());
        ResponsePage responsePage = new ResponsePage();
        responsePage.waitUntilLoads();
        responsePage.validateBankTxnId(Constants.ValidationType.NON_EMPTY)
                .validateCurrency("INR")
                .validateMid(mid)
                .validateOrderId(initTxnDTO.orderFromBody())
                .validatePaymentMode("NB")
                .validateRespCode(Constants.ResponseCode.TXN_SUCCESS.getRespCode())
                .validateRespMsg(Constants.ResponseCode.TXN_SUCCESS.getRespMsg())
                .validateStatus(Constants.TXNSTATUS.TXN_SUCCESS.toString())
                .validateTxnAmount(CommonHelpers.stringToDoubleWithTwoDigitAfterDecimalPoint(initTxnDTO.txnAmountFromBody())).validateTxnDate(new Date())
                .validateTxnId(Constants.ValidationType.NON_EMPTY)
                .validateGatewayName(Constants.Bank.HDFC_ONLY.toString())
                .validateBankName(Constants.Bank.HDFC_ONLY.toString())
                .assertAll();
    }

    @Owner(MANISH_MISHRA)
    @Test(description = "Validate Success BOB NB transaction")
    public void validateSuccessBOB_NBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.INSTA_NB_MID.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),  Constants.MerchantType.INSTA_NB_MID)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        nbPG2.Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.BOB.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        
    }

    @Owner(MANISH_MISHRA)
    @Test(description = "Validate Success NKMB NB transaction")
    public void validateSuccessNKMB_NBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.INSTA_NB_MID.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),  Constants.MerchantType.INSTA_NB_MID)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        nbPG2.Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.NKMB.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
       
    }

    @Owner(MANISH_MISHRA)
    @Test(description = "Validate Success INDS NB transaction")
    public void validateSuccessINDS_NBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.INSTA_NB_MID.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),  Constants.MerchantType.INSTA_NB_MID)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        nbPG2.Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.INDS.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        
    }

    @Owner(MANISH_MISHRA)
    @Test(description = "Validate Success CANARA NB transaction")
    public void validateSuccessCANARA_NBTxnPG2MID() throws Exception{
        User user = userManager.getForRead(Label.UPIPG2FF4JCONFIGUSER);
        String mid = Constants.MerchantType.INSTA_NB_MID.getId();
        InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(user.ssoToken(),  Constants.MerchantType.INSTA_NB_MID)
                .setOrderId(CommonHelpers.generateOrderId())
                .build();
        String newOrderId=initTxnDTO.getBody().getOrderId();
        initTxnDTO.getBody().setOrderId(newOrderId);
        InitTxnResponseDTO initTxnResponse = InitTxn.executeInitTxn(initTxnDTO);
        String txnToken = initTxnResponse.getBody().getTxnToken();
        nbPG2.Validate_FetchPayInstrument(txnToken, initTxnDTO,"NET_BANKING", "false");
        ProcessTxnV1Request processTxnV1Request = new ProcessTxnV1Request.Builder(
                mid,initTxnResponse.getBody().getTxnToken(),newOrderId,"PTM" + newOrderId)
                .setPaymentMode("NET_BANKING")
                .setChannelCode(Constants.Bank.CANARA.toString())
                .setAuthMode("USRPWD")
                .setMpin("1234")
                .build();
        ProcessTxnV1Response ptcResponse = NativeHelpers.executeProcessTxnV1(processTxnV1Request);
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultStatus()).isEqualTo("S");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultCode()).isEqualTo("0000");
        Assertions.assertThat(ptcResponse.getBody().getResultInfo().getResultMsg()).isEqualTo("Success");
        
    }

}
