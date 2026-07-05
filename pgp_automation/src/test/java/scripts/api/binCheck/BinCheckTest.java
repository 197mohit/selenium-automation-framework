package scripts.api.binCheck;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.paytm.api.user.card.bin.query.BinModifyApi;
import com.paytm.api.user.card.bin.query.BinQueryApi;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.PaymentDTO;
import com.paytm.framework.api.BaseApi;
import com.paytm.framework.api.BaseApiV2;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.api.MappingService.GetMerchPreferenceInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.MerchantPrefInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.merchantPrefDTO.MerchantPreferenceInfos;
import io.qameta.allure.Owner;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.testng.SkipException;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

/**

 * Created by Sourav Singh
 * To check all the Card Bins in smoke test before Regression Execution

 **/

@Owner("Sourav")
public class BinCheckTest extends PGPBaseTest {

    public static JsonPath cardBinQuery(String bin)
    {
        BinQueryApi binQueryApi = new BinQueryApi(bin.substring(0,6));
        JsonPath response = binQueryApi.execute().jsonPath();
        return response;
    }

    public static JsonPath cardModifyQuery(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian, Boolean PREPAID_CARD, String CORPORATE_CARD) throws JsonProcessingException {
        BinModifyApi binModifyApi = new BinModifyApi(bin, cardScheme, cardType, countryCode, institutionId, Indian, PREPAID_CARD, CORPORATE_CARD);
        JsonPath resp = binModifyApi.execute().jsonPath();
        return resp;
    }

    public static JsonPath cardModifyQuery(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian, String CORPORATE_CARD) throws JsonProcessingException {
        BinModifyApi binModifyApi = new BinModifyApi(bin, cardScheme, cardType, countryCode, institutionId, Indian, CORPORATE_CARD);
        JsonPath resp = binModifyApi.execute().jsonPath();
        return resp;
    }
    public static JsonPath cardModifyQuery(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian, Boolean PREPAID_CARD) throws JsonProcessingException {
        BinModifyApi binModifyApi = new BinModifyApi(bin, cardScheme, cardType, countryCode, institutionId, Indian, PREPAID_CARD);
        JsonPath resp = binModifyApi.execute().jsonPath();
        return resp;
    }
    public static JsonPath cardModifyQuery(String bin, String cardScheme, String cardType, String countryCode, String institutionId, Boolean Indian) throws JsonProcessingException {
        BinModifyApi binModifyApi = new BinModifyApi(bin, cardScheme, cardType, countryCode, institutionId, Indian);
        JsonPath resp = binModifyApi.execute().jsonPath();
        return resp;
    }

    @Test
    public void MASTER_ICICI_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.MASTER_ICICI_DEBIT_CARD_NUMBER.substring(0,6);
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MASTER", "DC", "IN", "ICICI", true, false);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_CORPORATE_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.ICICI_CORPORATE_DEBIT_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "ICICI", true, false, "true");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.ICICI_DEBIT_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MASTER", "DC", "IN", "ICICI", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
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
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MAESTRO", "DC", "IN", "HDFC", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MAESTRO");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }


    @Test
    public void AMEX_CARD_NUMBER() {
        String bin = PaymentDTO.AMEX_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "AMEX", "CC", "IN", "AMEX", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }


    @Test
    public void RUPAY_CARD_NUMBER() {
        String bin = PaymentDTO.RUPAY_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "RUPAY", "DC", "IN", "HDFC", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("RUPAY");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void DINERS_CARD_NUMBER() {
        String bin = PaymentDTO.DINERS_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "DINERS", "CC", "IN", "BBK", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("DINERS");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("BBK");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void DEBIT_CARD_NUMBER() {
        String bin = PaymentDTO.DEBIT_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "HDFC", true, false, "false");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }


    @Test
    public void SBI_DEBIT_CARD() {
        String bin = PaymentDTO.SBI_DEBIT_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "SBI", true, false, "false");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("SBI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void PNB_DEBIT_CARD(){
        String bin = PaymentDTO.PNB_DEBIT_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MASTER", "DC", "IN", "PNB", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("PNB");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void INTERNATIONAL_CARD(){
        String bin = PaymentDTO.INTERNATIONAL_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "CC", "IN", "HDFC", false);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("false");
    }


    @Test
    public void INTERNATIONAL_CARD_1(){
        String bin = PaymentDTO.INTERNATIONAL_CARD_1;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "CC", "IN", "HDFC", false, false, "false");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("false");
    }

    @Test
    public void debitCardNumber(){
        String bin = new PaymentDTO().getDebitCardNumber();
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "HDFC", true, false, "false");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_CREDIT_CARD_NUMBER(){
        String bin = PaymentDTO.ICICI_CREDIT_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "CC", "IN", "ICICI", true, "false");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void MASTER_CREDIT_CARD(){
        String bin = PaymentDTO.MASTER_CREDIT_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MASTER", "CC", "IN", "HDFC", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void BAJAJ_FINSERV_CREDIT_CARD_NUMBER(){
        String bin = PaymentDTO.BAJAJ_FINSERV_CREDIT_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "BAJAJFN", "CC", "IN", "BAJAJFN", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("BAJAJFN");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("BAJAJFN");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void MASTERCARD_CC_BILL_PAYMENT(){
        String bin = PaymentDTO.MASTERCARD_CC_BILL_PAYMENT;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MASTER", "CC", "IN", "HDFC", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void VISA_CC_BILL_PAYMENT(){
        String bin = PaymentDTO.VISA_CC_BILL_PAYMENT;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "CC", "IN", "HDFC", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void PREPAID_CARD(){
        String bin = PaymentDTO.PREPAID_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "HDFC", true, true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void CORPORATE_PREPAID_CARD(){
        String bin = PaymentDTO.CORPORATE_PREPAID_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "HDFC", true, true,"true");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void CORPORATE_INTERNATIONAL_PREPAID_CARD(){
        String bin = PaymentDTO.CORPORATE_INTERNATIONAL_PREPAID_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "US", "HDFC", false, true,"true");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("false");
    }

    @Test
    public void CORPORATE_INDIAN_DC(){
        String bin = PaymentDTO.CORPORATE_INDIAN_DC;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MASTER", "DC", "IN", "AXIS", true, false,"true");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AXIS");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void CORPORATE_INDIAN_CC(){
        String bin = PaymentDTO.CORPORATE_INDIAN_CC;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "CC", "IN", "HDFC", true, "true");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).as("CORPORATE_CARD Does not Match").isEqualTo("true");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");
    }

    @Test
    public void ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME(){
        String bin = PaymentDTO.ICICI_DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "ICICI", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("ICICI");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME(){
        String bin = PaymentDTO.DEBIT_CARD_FOR_FAIL_TXN_ENHANCEDTHEME;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "HDFC", true, false);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
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
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "MASTER", "CC", "IN", "HDFC", true, false);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("MASTER");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void DEBIT_CARD_FOR_FAILED_TXN(){
        String bin = PaymentDTO.DEBIT_CARD_FOR_FAILED_TXN;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "DC", "IN", "HDFC", true, false, "false");
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
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
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "VISA", "CC", "IN", "HDFC", true, false);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("VISA");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).as("PREPAID_CARD Does not Match").isEqualTo("false");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void LOW_SUCCESS_RATE_CARD_NUMBER(){
        String bin = PaymentDTO.LOW_SUCCESS_RATE_CARD_NUMBER;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "AMEX", "CC", "IN", "AMEX", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("CC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("AMEX");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

    @Test
    public void INVALID_CARD(){
        String bin = PaymentDTO.INVALID_CARD;
        JsonPath binResp = null;
        try {
            binResp = cardModifyQuery(bin, "RUPAY", "DC", "IN", "HDFC", true);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Assertions.assertThat(binResp).toString().contains("Success");
        JsonPath binResponse = cardBinQuery(bin);
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardScheme")).as("CardScheme Does not Match").isEqualTo("RUPAY");
        Assertions.assertThat(binResponse.getString("cardBinInfo.cardType")).as("cardType Does not Match").isEqualTo("DC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.institutionId")).as("institutionId Does not Match").isEqualTo("HDFC");
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.CORPORATE_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.PREPAID_CARD")).isNullOrEmpty();
        Assertions.assertThat(binResponse.getString("cardBinInfo.binConfigAttributes.INDIAN")).as("INDIAN Does not Match").isEqualTo("true");

    }

}
