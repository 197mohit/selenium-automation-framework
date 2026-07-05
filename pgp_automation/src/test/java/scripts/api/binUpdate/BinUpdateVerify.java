package scripts.api.binUpdate;

import com.paytm.LocalConfig;
import com.paytm.api.nativeAPI.MerchantPGPUILocale;
import com.paytm.api.user.card.bin.BinCenter.BinInfo;
import com.paytm.api.user.card.bin.BinCenter.UpdateBinApi;
import com.paytm.api.user.card.bin.BinCenter.UpdateBinRequest;
import com.paytm.apphelpers.HealthCheckHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.utils.DatabaseUtil;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import scripts.api.BinQuery.BinQuery;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static scripts.SolidityCheck.BankDetailsQueries.getUpdatedStaring;

public class BinUpdateVerify extends PGPBaseTest {

    public static final String WEB_PAY_URL= "https://automation-pg-ext.paytm.in/mockbank/bankFormatter/hdfcFormatter/verification";
    public static final String S2S_PAY_URL= "https://automation-pg-ext.paytm.in/mockbank/pgwayd/servlet/non3DPayment";
    public static final String STATUS_QRY_URL= "https://automation-pg-ext.paytm.in/mockbank/bankFormatter/hdfcFormatter/Payment";
    public static final String REFUND_URL= "https://automation-pg-ext.paytm.in/mockbank/pgwayd/servlet/TranPortalXMLServlet";
    public static final String REFUND_STATUS_URL= "https://automation-pg-ext.paytm.in/mockbank/pgwayd/servlet/TranPortalXMLServlet";
    public static final String WEB_RESPONSE_URL= "https://pgp-qa8.paytm.in/instaproxy/bankresponse/HDFC/CC";
    public static final String URL= "https://automation-pg-ext.paytm.in/mockbank/bankFormatter/hdfcFormatter/Payment";

    public static JsonPath cardBinQuery(String bin) {
        BinQuery binQuery = new BinQuery(bin);
        JsonPath  response = binQuery.execute().jsonPath();
        return response;
    }
    @Test
    public void TC_001_MASTER_ICICI_DEBIT_CARD_NUMBER() throws UnsupportedEncodingException {
        String bin = PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER.substring(0, 9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setIsBinCoftEligible(true)
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        System.out.println("DEBUG: BinInfo object: " + binInfo);
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();

        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");

        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");

    }
    @Test
    public void TC_002_ICICI_CORPORATE_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.ICICI_CORPORATE_DEBIT_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCorporateCard(true)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setCountryCode("IN")
                .setCardBin(bin)
                .setIsBinCoftEligible(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                        .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");

        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
  }
    @Test
    public void TC_003_ICICI_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.ICICI_DEBIT_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setCountryCode("IN")
                .setIsEmiEligible(true)
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");

        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
    }
    @Test
    public void TC_004_MAESTRO_DEBIT_CARD_NUMBER() {
         String bin = PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER.substring(0, 9);
         List<Integer> digits = List.of(6, 9);
         BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                 .setBinMin(bin)
                 .setBinMax(bin)
                 .setSource("ADMIN")
                 .setCardScheme("MAESTRO")
                 .setCardType("DC")
                 .setInstitutionId("HDFC")
                 .setCountryCode("IN")
                 .setCardBin(bin)
                 .setPrepaid(true)
                 .build();
            UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                    .setBinInfo(binInfo)
                    .setDigits(digits)
                    .setStrategy("OVERWRITE")
                    .setBinUpdateSource("DEFAULT")
                    .build();
            Response response = new UpdateBinApi(updateBinRequest)
                    .deleteContext("binInfo.emiEligible")
                    .deleteContext("binInfo.binCoftEligible")
                    .execute();
            System.out.println(response);
            Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
            JsonPath binResponse = cardBinQuery(bin);
            Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MAESTRO");
            Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
            Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
            Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
     }
    @Test
    public void TC_005_AMEX_CARD_NUMBER() {
        String bin = PaymentDTO.AMEX_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);

        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("AMEX")
                .setCardType("CC")
                .setInstitutionId("AMEX")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();

        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");

        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
    }
    @Test
    public void TC_006_RUPAY_CARD_NUMBER() {
        String bin = PaymentDTO.RUPAY_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("RUPAY")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setIsBinCoftEligible(true)
                .setIsEmiEligible(true)
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");

        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("RUPAY");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");

    }
    @Test
    public void TC_007_DINERS_CARD_NUMBER() {
        String bin = PaymentDTO.DINERS_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("DINERS")
                .setCardType("CC")
                .setInstitutionId("BBK")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("DINERS");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("BBK");
    }
    @Test
    public void TC_008_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.DEBIT_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
    }
    @Test
    public void TC_009_SBI_DEBIT_CARD() {
        String bin = PaymentDTO.SBI_DEBIT_CARD.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("SBI")
                .setCountryCode("IN")
                .setIsBinCoftEligible(true)
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("SBI");
    }
    @Test
    public void TC_010_PNB_DEBIT_CARD(){
        String bin = PaymentDTO.PNB_DEBIT_CARD.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("PNB")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("PNB");
    }
    @Test
    public void TC_011_INTERNATIONAL_CARD(){
        String bin = PaymentDTO.INTERNATIONAL_CARD.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("US")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("false");
}
   @Test
    public void TC_012_INTERNATIONAL_CARD_1(){
        String bin = PaymentDTO.INTERNATIONAL_CARD_1.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("US")
                .setCardBin(bin)
                .setIsEmiEligible(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
       Response response = new UpdateBinApi(updateBinRequest)
               .deleteContext("binInfo.emiEligible")
               .deleteContext("binInfo.binCoftEligible")
               .execute();
         System.out.println(response);
         Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
            JsonPath binResponse = cardBinQuery(bin);
            Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
            Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
            Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
            Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("false");
    }

    @Test
    public void TC_013_debitCardNumber(){
        String bin = new PaymentDTO().getDebitCardNumber().substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setIsEmiEligible(true)
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
    }

    @Test
    public void TC_014_ICICI_CREDIT_CARD_NUMBER(){
        String bin = PaymentDTO.ICICI_CREDIT_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("ICICI")
                .setCountryCode("IN")
                .setCardBin(bin)
                .setIsEmiEligible(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
    }

    @Test
    public void TC_015_MASTER_CREDIT_CARD(){
        String bin = PaymentDTO.MASTER_CREDIT_CARD.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("MASTER")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
    }

    @Test
    public void TC_016_BAJAJ_FINSERV_CREDIT_CARD_NUMBER(){
        String bin = PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("BAJAJFN")
                .setCardType("CC")
                .setInstitutionId("BAJAJFN")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("BAJAJFN");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("BAJAJFN");

    }

    @Test
    public void TC_017_MASTERCARD_CC_BILL_PAYMENT(){
        String bin = PaymentDTO.MASTERCARD_CC_BILL_PAYMENT.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("MASTER")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void TC_018_VISA_CC_BILL_PAYMENT(){
        String bin = PaymentDTO.VISA_CC_BILL_PAYMENT.substring(0,6);;
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setIsBinCoftEligible(true)
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void TC_019_PREPAID_CARD(){
        String bin = PaymentDTO.PREPAID_CARD.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setPrepaid(true)
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void TC_020_CORPORATE_PREPAID_CARD(){
        String bin = PaymentDTO.CORPORATE_PREPAID_CARD.substring(0,6);;
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setPrepaid(true)
                .setCardBin(bin)
                .setCorporateCard(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("binInfo.corporateCard")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("binInfo.prepaid")).as("PREPAID_CARD Does not Match").isEqualTo("true");
    }

    @Test
    public void TC_021_CORPORATE_INTERNATIONAL_PREPAID_CARD(){
        String bin = PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setIsBinCoftEligible(true)
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("US")
                .setPrepaid(true)
                .setCardBin(bin)
                .setCorporateCard(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response= new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse= cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("binInfo.corporateCard")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("binInfo.prepaid")).as("PREPAID_CARD Does not Match").isEqualTo("true");
  }

    @Test
    public void TC_022_CORPORATE_INDIAN_DC() {
        String bin = PaymentDTO.CORPORATE_INDIAN_DC.substring(0, 9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("AXIS")
                .setCountryCode("IN")
                .setCardBin(bin)
                .setCorporateCard(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AXIS");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("binInfo.corporateCard")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
    }

    @Test
    public void TC_023_CORPORATE_INDIAN_CC(){
        String bin = PaymentDTO.CORPORATE_INDIAN_CC.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setIsBinCoftEligible(true)
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .setCorporateCard(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("binInfo.corporateCard")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
    }

    @Test
    public void TC_024_ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME(){
        String bin = PaymentDTO.ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setIsBinCoftEligible(true)
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse= cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
}

    @Test
    public void TC_025_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME() {
        String bin = PaymentDTO.DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME.substring(0, 9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void TC_026_INVALID_MASTER_CARD_CC_BILL_PAYMENT() {
        String bin = PaymentDTO.INVALID_MASTER_CARD_CC_BILL_PAYMENT.substring(0, 9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setIsBinCoftEligible(false)
                .setIsEmiEligible(false)
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("MASTER")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .setBinInfo(binInfo)
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible").execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");

        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualToIgnoringCase("MASTER");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualToIgnoringCase("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualToIgnoringCase("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualToIgnoringCase("true");
    }

    @Test
    public void TC_027_DEBIT_CARD_FOR_FAILED_TXN() {
        String bin = PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN.substring(0, 9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .setIsEmiEligible(true)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .setBinInfo(binInfo)
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.binCoftEligible")
                .deleteContext("binInfo.emiEligible").execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualToIgnoringCase("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualToIgnoringCase("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualToIgnoringCase("HDFC");
        Assertions.assertThat(binResponse.getString("binInfo.isIndian")).as("INDIAN Does not Match").isEqualToIgnoringCase("true");
    }

    @Test
    public void TC_028_CREDIT_CARD_FOR_FAILED_TXN(){
        String bin = PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setIsBinCoftEligible(true)
                .setIsEmiEligible(true)
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
    }

    @Test
    public void TC_029_LOW_SUCCESS_RATE_CARD_NUMBER(){
        String bin = PaymentDTO.LOW_SUCCESS_RATE_CARD_NUMBER.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("AMEX")
                .setCardType("CC")
                .setInstitutionId("AMEX")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
    }

    @Test
    public void TC_030_INVALID_CARD(){
        String bin = PaymentDTO.INVALID_CARD.substring(0,9);
        List<Integer> digits = List.of(6, 9);
        BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
                .setBinMin(bin)
                .setBinMax(bin)
                .setSource("ADMIN")
                .setCardScheme("RUPAY")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setCountryCode("IN")
                .setCardBin(bin)
                .build();
        UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
                .setBinInfo(binInfo)
                .setDigits(digits)
                .setStrategy("OVERWRITE")
                .setBinUpdateSource("DEFAULT")
                .build();
        Response response = new UpdateBinApi(updateBinRequest)
                .deleteContext("binInfo.emiEligible")
                .deleteContext("binInfo.binCoftEligible")
                .execute();
        System.out.println(response);
        Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("RUPAY");
        Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
    }

    @Test
    public void TC_031_BANK_URL_INFO(){
        String PAY_METHOD_ID = "345678913,345678914";
        String BANK_ID = "8565560";
        String bank_url_update_query = "update PGPDB.BANK_URL_INFO set {?}='{?}'"
                +" where BANK_ID='{?}' " + " and PAY_METHOD_ID='{?}'";
        String[] payMethodIdList = PAY_METHOD_ID.split(",");

        for(String payMethodId : payMethodIdList){
            DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_update_query, "WEB_PAY_URL", WEB_PAY_URL, BANK_ID, payMethodId));
            DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_update_query, "S2S_PAY_URL", S2S_PAY_URL, BANK_ID, payMethodId));
            DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_update_query, "STATUS_QRY_URL", STATUS_QRY_URL, BANK_ID, payMethodId));
            //DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_update_query, "REFUND_URL", REFUND_URL, BANK_ID, payMethodId));
            //DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_update_query, "REFUND_STATUS_URL", REFUND_STATUS_URL, BANK_ID, payMethodId));
            DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_update_query, "WEB_RESPONSE_URL", WEB_RESPONSE_URL, BANK_ID, payMethodId));
            DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_update_query, "URL", URL, BANK_ID, payMethodId));
        }
        String bank_url_select_query = "select * from PGPDB.BANK_URL_INFO where BANK_ID='{?}' " + " and PAY_METHOD_ID='{?}'";
        for(String payMethodId : payMethodIdList){
            List<Map<String, Object>> Response =  DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(bank_url_select_query, BANK_ID, payMethodId));
            for (Map<String, Object> record : Response) {
                Assertions.assertThat(record.get("WEB_PAY_URL")).as("WEB_PAY_URL Does not Match").isEqualTo(WEB_PAY_URL);
                Assertions.assertThat(record.get("WEB_RESPONSE_URL")).as("WEB_RESPONSE_URL Does not Match").isEqualTo(WEB_RESPONSE_URL);
                Assertions.assertThat(record.get("S2S_PAY_URL")).as("S2S_PAY_URL Does not Match").isEqualTo(S2S_PAY_URL);
                Assertions.assertThat(record.get("STATUS_QRY_URL")).as("STATUS_QRY_URL Does not Match").isEqualTo(STATUS_QRY_URL);
                Assertions.assertThat(record.get("URL")).as("URL Does not Match").isEqualTo(URL);
            }
        }
    }
    @Test
    public void TC_032_FORMATTER_DETAILS(){
        String formatter_details_update_query = "update PGPDB.FORMATTER_DETAILS set {?}='{?}'"
                +" where BANK_CODE='{?}' " + " and PAY_METHOD='{?}'";
        String formatter_details_select_query = "select FORMATTER_NAME from PGPDB.FORMATTER_DETAILS where BANK_CODE='{?}' " + " and PAY_METHOD='{?}'";
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "HDFCFormatterImpl", "HDFC", "CC"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "HDFCFormatterImpl", "HDFC", "DC"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "HDFCFormatterImpl", "HDFC", "EMI"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "HDFCFormatterImpl", "HDFC", "EMI_DC"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "HDFCUPIFormatterImpl", "HDFC", "UPI"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "ICICINetBankingFormatterImpl", "ICICI", "NB"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "PaytmUPIPushFormatterImpl", "PPBL", "UPI"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "PaytmUPICollectFormatterImpl", "PPBLC", "UPI"));
        DatabaseUtil.getInstance().executeUpdateQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_update_query, "FORMATTER_NAME", "UPIPushExpressFormatterImpl", "PPBEX", "UPI"));

        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "HDFC", "CC")).get(0).get("FORMATTER_NAME")).as("HDFC CC FORMATTER_NAME Does not Match").isEqualTo("HDFCFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "HDFC", "DC")).get(0).get("FORMATTER_NAME")).as("HDFC DC FORMATTER_NAME Does not Match").isEqualTo("HDFCFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "HDFC", "EMI")).get(0).get("FORMATTER_NAME")).as("HDFC EMI FORMATTER_NAME Does not Match").isEqualTo("HDFCFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "HDFC", "EMI_DC")).get(0).get("FORMATTER_NAME")).as("HDFC EMI_DC FORMATTER_NAME Does not Match").isEqualTo("HDFCFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "HDFC", "UPI")).get(0).get("FORMATTER_NAME")).as("HDFC UPI FORMATTER_NAME Does not Match").isEqualTo("HDFCUPIFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "ICICI", "NB")).get(0).get("FORMATTER_NAME")).as("ICICI NB FORMATTER_NAME Does not Match").isEqualTo("ICICINetBankingFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "PPBL", "UPI")).get(0).get("FORMATTER_NAME")).as("PPBL UPI FORMATTER_NAME Does not Match").isEqualTo("PaytmUPIPushFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "PPBLC", "UPI")).get(0).get("FORMATTER_NAME")).as("PPBLC UPI FORMATTER_NAME Does not Match").isEqualTo("PaytmUPICollectFormatterImpl");
        Assertions.assertThat(DatabaseUtil.getInstance().executeSelectQuery(LocalConfig.PGP_DB_CONNECTION_URL, getUpdatedStaring(formatter_details_select_query, "PPBEX", "UPI")).get(0).get("FORMATTER_NAME")).as("PPBEX UPI FORMATTER_NAME Does not Match").isEqualTo("UPIPushExpressFormatterImpl");

    }

  @Test
  public void TC_033_AMEX_CREDIT_CARD_NUMBER_A(){
    String bin = PaymentDTO.AMEX_CREDIT_CARD_NUMBER_A.substring(0,9);
    List<Integer> digits = List.of(6, 9);
    BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
        .setBinMin(bin)
        .setBinMax(bin)
        .setSource("ADMIN")
        .setCardScheme("AMEX")
        .setCardType("CC")
        .setInstitutionId("AMEX")
        .setCountryCode("IN")
        .setCardBin(bin)
        .setIsEmiEligible(true)
        .setIsBinCoftEligible(true)
        .setIsAltTokenizationEligible(true)
        .build();
    UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
        .setBinInfo(binInfo)
        .setDigits(digits)
        .setStrategy("OVERWRITE")
        .setBinUpdateSource("DEFAULT")
        .build();
    Response response = new UpdateBinApi(updateBinRequest)
        .deleteContext("binInfo.emiEligible")
        .deleteContext("binInfo.binCoftEligible")
        .deleteContext("binInfo.isAltTokenizationEligible")
        .execute();
    System.out.println(response);
    Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
    JsonPath binResponse = cardBinQuery(bin);
    Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
    Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
    Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
    Assertions.assertThat(binResponse.getBoolean("binInfo.isEmiEligible")).as("EMI is not enabled").isEqualTo(true);
    Assertions.assertThat(binResponse.getBoolean("binInfo.isBinCoftEligible")).as("COFT is not enabled").isEqualTo(true);
    Assertions.assertThat(binResponse.getBoolean("binInfo.isAltTokenizationEligible")).as("AltId is not enabled").isEqualTo(true);
  }

  @Test
  public void TC_034_AMEX_CREDIT_CARD_NUMBER_B(){
    String bin = PaymentDTO.AMEX_CREDIT_CARD_NUMBER_B.substring(0,9);
    List<Integer> digits = List.of(6, 9);
    BinInfo binInfo = BinInfo.BinInfoBuilder.builder()
        .setBinMin(bin)
        .setBinMax(bin)
        .setSource("ADMIN")
        .setCardScheme("AMEX")
        .setCardType("CC")
        .setInstitutionId("AMEX")
        .setCountryCode("IN")
        .setCardBin(bin)
        .setIsEmiEligible(true)
        .setIsBinCoftEligible(true)
        .setIsAltTokenizationEligible(true)
        .build();
    UpdateBinRequest updateBinRequest = UpdateBinRequest.UpdateBinApiBuilder.builder()
        .setBinInfo(binInfo)
        .setDigits(digits)
        .setStrategy("OVERWRITE")
        .setBinUpdateSource("DEFAULT")
        .build();
    Response response = new UpdateBinApi(updateBinRequest)
        .deleteContext("binInfo.emiEligible")
        .deleteContext("binInfo.binCoftEligible")
        .deleteContext("binInfo.isAltTokenizationEligible")
        .execute();
    System.out.println(response);
    Assertions.assertThat(response.jsonPath().getString("resultInfo.resultMsg")).as("Status Does not Match").isEqualToIgnoringCase("SUCCESS");
    JsonPath binResponse = cardBinQuery(bin);
    Assertions.assertThat(binResponse.getString("binInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
    Assertions.assertThat(binResponse.getString("binInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
    Assertions.assertThat(binResponse.getString("binInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
    Assertions.assertThat(binResponse.getBoolean("binInfo.isEmiEligible")).as("EMI is not enabled").isEqualTo(true);
    Assertions.assertThat(binResponse.getBoolean("binInfo.isBinCoftEligible")).as("COFT is not enabled").isEqualTo(true);
    Assertions.assertThat(binResponse.getBoolean("binInfo.isAltTokenizationEligible")).as("AltId is not enabled").isEqualTo(true);
  }

    @Test
    public void kafkahealthcheckup(){
        HealthCheckHelper kafkaHealthCheck = new HealthCheckHelper();
        Assertions.assertThat(kafkaHealthCheck.kafkahealthStatus(LocalConfig.KAFKA_SERVER)).isEqualTo("Kafka is up and running!");
    }

    @Test
    public void redishealthcheckup(){
        HealthCheckHelper redisHealthCheck = new HealthCheckHelper();
        List<String> ipAndPorts = redisHealthCheck.getIpAndPorts(LocalConfig.PG_REDIS_URI);

        String result = redisHealthCheck.RedisHealthCheckMultiplePorts(ipAndPorts);
        for(String ipAndPort : ipAndPorts){
            Assertions.assertThat(result).contains("Redis is running on "+ipAndPort);
        }
        Assertions.assertThat(result).doesNotContain("Failed to connect to Redis");
    }
//    @Test
    public void Zookeperhealthcheckup(){
        HealthCheckHelper kafkaHealthCheck = new HealthCheckHelper();
        Assertions.assertThat(kafkaHealthCheck.checkZookeeperHealth("10.170.92.190")).isEqualTo("Kafka is up and running!");
    }


    @Test(description = "Update Merchant PGP UI Locale for all merchants")
    public void updateMerchantpgpuiLocale(){
        MerchantPGPUILocale merchantLocale = new MerchantPGPUILocale();
        JsonPath response = merchantLocale.execute().jsonPath();
        System.out.println(response);
        Assertions.assertThat(response.getString("message")).as("Message does not match").isEqualTo("Locale Data updated successfully.");
    }

}
