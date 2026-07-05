package scripts;

import static com.paytm.appconstants.Constants.Owner.ABHISHEK_VERMA;

import com.paytm.api.Deals.GetPaymentStatus;
import com.paytm.api.GlobalConfig;
import com.paytm.api.MidMiscInfo;
import com.paytm.api.TxnStatus;
import com.paytm.api.V3OrderStatus;
import com.paytm.api.nativeAPI.InitTxn;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.base.test.User;
import com.paytm.dto.NativeDTO.InitTxn.InitTxnDTO;
import com.paytm.dto.OrderDTO;
import com.paytm.dto.OrderFactory;
import com.paytm.dto.PaymentDTO;
import com.paytm.pages.CheckoutPage;
import com.paytm.utils.merchant.util.PayMethodType;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import java.io.UnsupportedEncodingException;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

public class HPCLBinMappingMerchantStatus extends PGPBaseTest {


  private static CheckoutPage checkoutPage = new CheckoutPage();
  Constants.MerchantType hpclMid = MerchantType.COFT_MERCHANT;
  Constants.MerchantType irctcMid = Constants.MerchantType.Irctc_binIrcId;
  Constants.MerchantType midWithPrefOff = MerchantType.COFT_MERCHANT_3P;
  String orderIdWithFF4JOff = null;
  String orderIdWithFF4JON = null;
  String orderIfWithPrefOff = null;

  private final String cardWithMappingPresent = PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER;
  private final String bin = PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER.substring(0, 6);

  public String getBinMappingForIrctc(String bin) {
    GlobalConfig globalConfig = new GlobalConfig(bin);
    JsonPath globalConfigResponse = globalConfig.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(globalConfigResponse.getString("resultInfo.messaage"))
        .isEqualToIgnoringCase("Success");
    softly.assertAll();
    return globalConfigResponse.getString("response.value");
  }


  public String getBinMappingForHPCL(String mid, String bin) {
    MidMiscInfo midMiscInfo = new MidMiscInfo(mid, bin);
    JsonPath midMiscInfoResponse = midMiscInfo.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(midMiscInfoResponse.getString("resultInfo.resultMsg"))
        .isEqualToIgnoringCase("Success");
    softly.assertAll();
    return midMiscInfoResponse.getString("body[0].additionalInfo.binIdentifier");

  }


  @BeforeClass
  @Parameters({"theme"})
  public void getOrderIds(@Optional("enhancedweb_revamp") String theme) throws Exception {
    User user = userManager.getForWrite(Label.BASIC);
    String ssoToken = user.ssoToken();
    PaymentDTO paymentDTO= new PaymentDTO();

    InitTxnDTO initTxnDTO = new InitTxnDTO.Builder(ssoToken, hpclMid).build();
    InitTxn initTxn = new InitTxn(initTxnDTO);
    Response response = initTxn.execute();
    Assertions.assertThat(response.jsonPath().get("body.txnToken").toString()).isNotNull();
    String orderId = initTxnDTO.orderFromBody();
    String txnToken = response.jsonPath().get("body.txnToken").toString();
    OrderDTO orderDTO = new OrderFactory.Native(hpclMid, orderId, txnToken, PayMethodType.DEBIT_CARD)
        .setCardInfo("|"+cardWithMappingPresent+"|"+paymentDTO.getCvvNumber()+"|"+paymentDTO.getExpMonth()+PaymentDTO.Tokenization_Year)
        .build();
    checkoutPage.createNativeOrder(orderDTO, true);
    TxnStatus txnStatus = new TxnStatus(orderDTO.getMID(), orderDTO.getORDER_ID());
    txnStatus.executeUntilNotPending();
    System.out.println("Txn amount is "+ orderDTO.getTXN_AMOUNT());
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(initTxnDTO.orderFromBody())
        .validateTxnAmount(initTxnDTO.txnAmountFromBody())
        .validateStatus("TXN_SUCCESS")
        .AssertAll();


    System.out.println("OrderId with ff4jOff is"+ (orderIdWithFF4JOff = initTxnDTO.orderFromBody()) );


    InitTxnDTO initTxnDTO1 = new InitTxnDTO.Builder(ssoToken, irctcMid).build();
    InitTxn initTxn1 = new InitTxn(initTxnDTO1);
    Response response1 = initTxn1.execute();
    Assertions.assertThat(response1.jsonPath().get("body.txnToken").toString()).isNotNull();
    String orderId1 = initTxnDTO1.orderFromBody();
    String txnToken1 = response1.jsonPath().get("body.txnToken").toString();
    OrderDTO orderDTO1 = new OrderFactory.Native(irctcMid, orderId1, txnToken1, PayMethodType.DEBIT_CARD)
        .setCardInfo("|"+cardWithMappingPresent+"|"+paymentDTO.getCvvNumber()+"|"+paymentDTO.getExpMonth()+PaymentDTO.Tokenization_Year)
        .build();
    checkoutPage.createNativeOrder(orderDTO1, true);
    TxnStatus txnStatus1 = new TxnStatus(orderDTO1.getMID(), orderDTO1.getORDER_ID());
    txnStatus1.executeUntilNotPending();
    System.out.println("Txn amount is "+ orderDTO1.getTXN_AMOUNT());
    txnStatus1.validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(initTxnDTO1.orderFromBody())
        .validateTxnAmount(initTxnDTO1.txnAmountFromBody())
        .validateStatus("TXN_SUCCESS")
        .AssertAll();

    System.out.println("OrderId with ff4jOn is"+ (orderIdWithFF4JON = initTxnDTO1.orderFromBody()) );


    InitTxnDTO initTxnDTO2 = new InitTxnDTO.Builder(ssoToken, midWithPrefOff).build();
    InitTxn initTxn2 = new InitTxn(initTxnDTO2);
    Response response2 = initTxn2.execute();
    Assertions.assertThat(response2.jsonPath().get("body.txnToken").toString()).isNotNull();
    String orderId2 = initTxnDTO2.orderFromBody();
    String txnToken2 = response2.jsonPath().get("body.txnToken").toString();
    OrderDTO orderDTO2 = new OrderFactory.Native(midWithPrefOff, orderId2, txnToken2, PayMethodType.DEBIT_CARD)
        .setCardInfo("|"+cardWithMappingPresent+"|"+paymentDTO.getCvvNumber()+"|"+paymentDTO.getExpMonth()+PaymentDTO.Tokenization_Year)
        .build();
    checkoutPage.createNativeOrder(orderDTO2, true);
    TxnStatus txnStatus2 = new TxnStatus(orderDTO2.getMID(), orderDTO2.getORDER_ID());
    txnStatus2.executeUntilNotPending();
    System.out.println("Txn amount is "+ orderDTO2.getTXN_AMOUNT());
    txnStatus2.validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(initTxnDTO2.orderFromBody())
        .validateTxnAmount(initTxnDTO2.txnAmountFromBody())
        .validateStatus("TXN_SUCCESS")
        .AssertAll();

    System.out.println("OrderId with ff4jOn is"+ (orderIfWithPrefOff = initTxnDTO2.orderFromBody()) );

  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify BIN_IDENTIFIER filed is returned in response and it is returned from merchant-center when ff4j theia.enable.binIrcId is disabled ")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JOff(){
    V3OrderStatus v3OrderStatus = new V3OrderStatus();
    v3OrderStatus.buildRequest(hpclMid, orderIdWithFF4JOff);
    JsonPath v3OrderStatusResponse = v3OrderStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(v3OrderStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(v3OrderStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isEqualTo(getBinMappingForHPCL(hpclMid.getId(), bin));
    softly.assertThat(v3OrderStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isNotEqualTo(getBinMappingForIrctc(bin));
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify BIN_IDENTIFIER filed is returned in response and it is returned from mapping-service when ff4j theia.enable.binIrcId is enabled ")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JON(){
    V3OrderStatus v3OrderStatus = new V3OrderStatus();
    v3OrderStatus.buildRequest(irctcMid, orderIdWithFF4JON);
    JsonPath v3OrderStatusResponse = v3OrderStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(v3OrderStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isEqualTo(getBinMappingForIrctc(bin));
    softly.assertThat(v3OrderStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isNotEqualTo(getBinMappingForHPCL(hpclMid.getId(), bin));
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify BIN_IDENTIFIER is not returned when pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is not on")
  public void verifyBinIdentifierNotReturnedWhenPrefNotPresent(){
    V3OrderStatus v3OrderStatus = new V3OrderStatus();
    v3OrderStatus.buildRequest(midWithPrefOff, orderIfWithPrefOff);
    JsonPath v3OrderStatusResponse = v3OrderStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(v3OrderStatusResponse.getString("body")).doesNotContain("BIN_IDENTIFIER");
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify binIrcId is not returned when pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE and ff4j theia.enable.binIrcId is on")
  public void verifybinIrcIdIsNotReturnedWhenPrefAndFF4JisOn() {
    V3OrderStatus v3OrderStatus = new V3OrderStatus();
    v3OrderStatus.buildRequest(irctcMid, orderIdWithFF4JON);
    JsonPath v3OrderStatusResponse = v3OrderStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(v3OrderStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(v3OrderStatusResponse.getString("body")).doesNotContain("binIrcId");
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/api/v1/getPaymentStatus' BIN_IDENTIFIER filed is returned in response and it is returned from merchant-center when Pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is ON and ff4j theia.enable.binIrcId is disabled ")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JOffGetPaymentStatus() throws Exception {
    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(hpclMid.getId(),
        orderIdWithFF4JOff);
    JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(paymentStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isEqualTo(getBinMappingForHPCL(hpclMid.getId(), bin));
    softly.assertThat(paymentStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isNotEqualTo(getBinMappingForIrctc(bin));
    softly.assertThat(paymentStatusResponse.getString("body")).doesNotContain("binIrcId");
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/api/v1/getPaymentStatus' BIN_IDENTIFIER filed is returned in response and it is returned from mapping-service when Pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is ON and ff4j theia.enable.binIrcId is enabled")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JONGetPaymentStatus() throws Exception {
    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(irctcMid.getId(),
        orderIdWithFF4JON);
    JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(paymentStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isEqualTo(getBinMappingForIrctc(bin));
    softly.assertThat(paymentStatusResponse.getString("body.BIN_IDENTIFIER"))
        .isNotEqualTo(getBinMappingForHPCL(hpclMid.getId(), bin));
    softly.assertThat(paymentStatusResponse.getString("body")).doesNotContain("binIrcId");
    softly.assertAll();
  }


  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/api/v1/getPaymentStatus' BIN_IDENTIFIER is not returned when pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is not on")
  public void verifyBinIdentifierNotReturnedWhenPrefNotPresentGetPaymentStatus()
      throws UnsupportedEncodingException {
    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(
        midWithPrefOff.getId(), orderIfWithPrefOff);
    JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(paymentStatusResponse.getString("body")).doesNotContain("BIN_IDENTIFIER");
    softly.assertThat(paymentStatusResponse.getString("body")).doesNotContain("binIrcId");
    softly.assertAll();
  }


  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/api/v1/getPaymentStatus' binIrcId is not returned when pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE and ff4j theia.enable.binIrcId is on")
  public void verifybinIrcIdIsNotReturnedWhenPrefAndFF4JisOnGetPaymentStatus()
      throws UnsupportedEncodingException {

    GetPaymentStatus getPaymentStatus = new GetPaymentStatus().buildWithParameters(irctcMid.getId(),
        orderIdWithFF4JON);
    JsonPath paymentStatusResponse = getPaymentStatus.execute().jsonPath();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(paymentStatusResponse.getString("body.resultInfo.resultStatus"))
        .isEqualToIgnoringCase("TXN_SUCCESS");
    softly.assertThat(paymentStatusResponse.getString("body")).doesNotContain("binIrcId");
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/getTxnStatus' BIN_IDENTIFIER filed is returned in response and it is returned from merchant-center when Pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is ON and ff4j theia.enable.binIrcId is disabled ")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JOffGetTxnStatus(){
    TxnStatus txnStatus = new TxnStatus(hpclMid.getId(), orderIdWithFF4JOff);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(orderIdWithFF4JOff)
        .validateStatus("TXN_SUCCESS")
        .validateBIN_IDENTIFIER(getBinMappingForHPCL(hpclMid.getId(), bin))
        .validatebinIrcIdNotPresent()
        .AssertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/getTxnStatus' BIN_IDENTIFIER filed is returned in response and it is returned from mapping-service when Pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is ON and ff4j theia.enable.binIrcId is enabled")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JONGetTxnStatus() {
    TxnStatus txnStatus = new TxnStatus(irctcMid.getId(), orderIdWithFF4JON);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(orderIdWithFF4JON)
        .validateStatus("TXN_SUCCESS")
        .validatebinIrcId(getBinMappingForIrctc(bin))
        .validateBIN_IDENTIFIER(getBinMappingForIrctc(bin))
        .AssertAll();
  }


  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/getTxnStatus' BIN_IDENTIFIER is not returned when pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is not on")
  public void verifyBinIdentifierNotReturnedWhenPrefNotPresentGetTxnStatus(){
    TxnStatus txnStatus = new TxnStatus(midWithPrefOff.getId(), orderIfWithPrefOff);
    txnStatus.executeUntilNotPending();
    txnStatus.validateTxnId(Constants.ValidationType.NON_EMPTY)
        .validateOrderid(orderIfWithPrefOff)
        .validateStatus("TXN_SUCCESS")
        .validatebinIrcIdNotPresent()
        .validateBIN_IDENTIFIERNotPresent()
        .AssertAll();
  }


  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/HANDLER_INTERNAL/TXNSTATUS' BIN_IDENTIFIER filed is returned in response and it is returned from merchant-center when Pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is ON and ff4j theia.enable.binIrcId is disabled ")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JOffHandlerInternalTxnStatus(){
    TxnStatus txnStatus = new TxnStatus();
    txnStatus.getNativeStatus(hpclMid.getId(), orderIdWithFF4JOff);
    Response response = txnStatus.execute();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(response.jsonPath().getString("BIN_IDENTIFIER"))
        .isEqualTo(getBinMappingForHPCL(hpclMid.getId(), bin));
    softly.assertThat(response.jsonPath().getString("BIN_IDENTIFIER"))
        .isNotEqualTo(getBinMappingForIrctc(bin));
    softly.assertThat(response.jsonPath().getString("binIrcId")).isNullOrEmpty();
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/getTxnStatus' BIN_IDENTIFIER filed is returned in response and it is returned from mapping-service when Pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is ON and ff4j theia.enable.binIrcId is enabled")
  public void verifyBinIdentifierReturnedWhenPrefOnWithIRCTCFF4JONHandlerInternalTxnStatus()  {
    TxnStatus txnStatus = new TxnStatus();
    txnStatus.getNativeStatus(irctcMid.getId(), orderIdWithFF4JON);
    Response response = txnStatus.execute();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(response.jsonPath().getString("BIN_IDENTIFIER"))
        .isEqualTo(getBinMappingForIrctc(bin));
    softly.assertThat(response.jsonPath().getString("BIN_IDENTIFIER"))
        .isNotEqualTo(getBinMappingForHPCL(hpclMid.getId(), bin));
    softly.assertThat(response.jsonPath().getString("binIrcId"))
        .isEqualTo(getBinMappingForIrctc(bin));
    softly.assertThat(response.jsonPath().getString("binIrcId"))
        .isNotEqualTo(getBinMappingForHPCL(hpclMid.getId(), bin));
    softly.assertAll();
  }

  @Owner(ABHISHEK_VERMA)
  @Feature("PGP-48475")
  @Parameters({"theme"})
  @Test(description = "Verify for 'merchant-status/getTxnStatus' BIN_IDENTIFIER is not returned when pref ENABLE_BIN_IDENTIFIER_IN_RESPONSE is not on")
  public void verifyBinIdentifierNotReturnedWhenPrefNotPresentHandlerInternalTxnStatus() {
    TxnStatus txnStatus = new TxnStatus();
    txnStatus.getNativeStatus(midWithPrefOff.getId(), orderIfWithPrefOff);
    Response response = txnStatus.execute();
    SoftAssertions softly = new SoftAssertions();
    softly.assertThat(response.jsonPath().getString("BIN_IDENTIFIER")).isNullOrEmpty();
    softly.assertThat(response.jsonPath().getString("binIrcId")).isNullOrEmpty();
    softly.assertAll();
  }
}







