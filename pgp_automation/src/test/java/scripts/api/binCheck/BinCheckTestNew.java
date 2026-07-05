package scripts.api.binCheck;


import com.paytm.api.user.card.bin.BinModify.BinConfigAttributes;
import com.paytm.api.user.card.bin.BinModify.BinConfigAttributes.BinConfigAttributesBuilder;
import com.paytm.api.user.card.bin.BinModify.BinModifyApi;
import com.paytm.api.user.card.bin.BinModify.BinModifyRequest;
import com.paytm.api.user.card.bin.BinModify.BinModifyRequest.BinModifyApiBuilder;
import com.paytm.api.user.card.bin.query.BinQueryApi;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

/**

 * Created by Sourav Singh
 * To check all the Card Bins in smoke test before Regression Execution

 **/

@Owner("Sourav")
public class BinCheckTestNew extends PGPBaseTest {

    @Test
    public void MASTER_ICICI_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder().build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        System.out.println(binModify);

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();

        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_CORPORATE_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.ICICI_CORPORATE_DEBIT_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .setCORPORATE_CARD("true")
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();

        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.ICICI_DEBIT_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void MAESTRO_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.MAESTRO_DEBIT_CARD_NUMBER.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .setPREPAID_CARD("true")
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MAESTRO")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();

        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MAESTRO");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }


    @Test
    public void AMEX_CARD_NUMBER() {
        String bin = PaymentDTO.AMEX_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("AMEX")
                .setCardType("CC")
                .setInstitutionId("AMEX")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }


    @Test
    public void RUPAY_CARD_NUMBER() {
        String bin = PaymentDTO.RUPAY_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("RUPAY")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("RUPAY");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void DINERS_CARD_NUMBER() {
        String bin = PaymentDTO.DINERS_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("DINERS")
                .setCardType("CC")
                .setInstitutionId("BBK")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("DINERS");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("BBK");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.DEBIT_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }


    @Test
    public void SBI_DEBIT_CARD() {
        String bin = PaymentDTO.SBI_DEBIT_CARD.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("SBI")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("SBI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void PNB_DEBIT_CARD(){
        String bin = PaymentDTO.PNB_DEBIT_CARD.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("PNB")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("PNB");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void INTERNATIONAL_CARD(){
        String bin = PaymentDTO.INTERNATIONAL_CARD.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("false");
    }


    @Test
    public void INTERNATIONAL_CARD_1(){
        String bin = PaymentDTO.INTERNATIONAL_CARD_1.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("false");
    }

    @Test
    public void debitCardNumber(){
        String bin = new PaymentDTO().getDebitCardNumber().substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_CREDIT_CARD_NUMBER(){
        String bin = PaymentDTO.ICICI_CREDIT_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("ICICI")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();

        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void MASTER_CREDIT_CARD(){
        String bin = PaymentDTO.MASTER_CREDIT_CARD.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void BAJAJ_FINSERV_CREDIT_CARD_NUMBER(){
        String bin = PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("BAJAJFN")
                .setCardType("CC")
                .setInstitutionId("BAJAJFN")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("BAJAJFN");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("BAJAJFN");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void MASTERCARD_CC_BILL_PAYMENT(){
        String bin = PaymentDTO.MASTERCARD_CC_BILL_PAYMENT.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MASTER")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void VISA_CC_BILL_PAYMENT(){
        String bin = PaymentDTO.VISA_CC_BILL_PAYMENT.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void PREPAID_CARD(){
        String bin = PaymentDTO.PREPAID_CARD.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .setPREPAID_CARD("true")
                .build();

        System.out.println(binConfigAttributes);

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        System.out.println(binModify);

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void CORPORATE_PREPAID_CARD(){
        String bin = PaymentDTO.CORPORATE_PREPAID_CARD.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .setCORPORATE_CARD("true")
                .setPREPAID_CARD("true")
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void CORPORATE_INTERNATIONAL_PREPAID_CARD(){
        String bin = PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD.substring(0,6);;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .setCORPORATE_CARD("true")
                .setPREPAID_CARD("true")
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("false");
    }

    @Test
    public void CORPORATE_INDIAN_DC(){
        String bin = PaymentDTO.CORPORATE_INDIAN_DC.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .setCORPORATE_CARD("true")
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MASTER")
                .setCardType("DC")
                .setInstitutionId("AXIS")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AXIS");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void CORPORATE_INDIAN_CC(){
        String bin = PaymentDTO.CORPORATE_INDIAN_CC.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .setCORPORATE_CARD("true")
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME(){
        String bin = PaymentDTO.ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("ICICI")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME(){
        String bin = PaymentDTO.DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void INVALID_MASTER_CARD_CC_BILL_PAYMENT(){
        String bin = PaymentDTO.INVALID_MASTER_CARD_CC_BILL_PAYMENT;

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("MASTER")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void DEBIT_CARD_FOR_FAILED_TXN(){
        String bin = PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void CREDIT_CARD_FOR_FAILED_TXN(){
        String bin = PaymentDTO.CREDIT_CARD_FOR_FAILED_TXN.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("VISA")
                .setCardType("CC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();

        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void LOW_SUCCESS_RATE_CARD_NUMBER(){
        String bin = PaymentDTO.LOW_SUCCESS_RATE_CARD_NUMBER.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("AMEX")
                .setCardType("CC")
                .setInstitutionId("AMEX")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void INVALID_CARD(){
        String bin = PaymentDTO.INVALID_CARD.substring(0,6);

        BinConfigAttributes binConfigAttributes = BinConfigAttributesBuilder.builder()
                .build();

        BinModifyRequest binModify = BinModifyApiBuilder.builder()
                .setBin(bin)
                .setCardScheme("RUPAY")
                .setCardType("DC")
                .setInstitutionId("HDFC")
                .setBinConfigAttributes(binConfigAttributes)
                .build();

        Response response = new BinModifyApi(binModify).execute();
        Assertions.assertThat(response.jsonPath().getString("status")).isEqualTo("SUCCESS");

        JsonPath binResponse = new BinQueryApi(bin).execute().jsonPath();
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("RUPAY");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

}
