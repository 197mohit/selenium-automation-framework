package scripts.api.mappingService.PG2MappingApis;

import com.paytm.api.MappingApisPG2;
import com.paytm.api.PG2MappingApisHelper;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;
import org.testng.annotations.Test;
import java.util.Map;

public class GetEntityEdcChannelInfo extends PGPBaseTest {
    MappingApisPG2 mappingApisPG2 = new MappingApisPG2();
    public JsonPath executeAndVerify(MappingApisPG2 mappingApisPG2) {
        JsonPath jsonPath = mappingApisPG2.execute().jsonPath();
        Map<String,String> objectHead = jsonPath.getMap("");
        PG2MappingApisHelper pg2MappingApisHelper = new PG2MappingApisHelper();
        pg2MappingApisHelper.verifyEntityEdcChannelInfo(objectHead, jsonPath);
        return jsonPath;
    }
    @Test(description = "Verify entityEdcChannelInfo/mid Api response")
    void verifyEntityEdcChannelInfoAPI() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info(Constants.MerchantType.Mapping_PG2_MID_ENTITY_EDC_CHANNEL_INFO_MID_POSITIVE.getId().toString());
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.id"), "[279]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.mid"), "["+Constants.MerchantType.Mapping_PG2_MID_ENTITY_EDC_CHANNEL_INFO_MID_POSITIVE.getId().toString()+"]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid"), "[91008888]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelInfoId"), "[9000770550078071381]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.mbid"), "[234567]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.fileType"), "[HDFC_SMS_PAYMENT_LINK]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.extMid"), "[HDFC000008217422]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.status"), "[true]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.businessName"), "[Paytm]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.legalName"), "[HDFC LTD]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.pgMid"), "[HDFC000008217410]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.merchantKey"), "[5d00469932e7a36b33cc848bbb6963f5]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.registrationDate"), "[27/12/23]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.meCode"), "[ABCDEF01234567]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.tid"), "[6S272629]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.entityChannelPayload.vpa"), "[hdfcbanktest@hdfcbank]");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"), "[true]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when HDUS acquiring is INACTIVE(9376504 status) in ENTITY_CHANNEL_INFO")
    void verifyEntityEdcChannelInfoAPI_HDUSAcquiringInactive() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("qa14as95097522057850");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"),"[false]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when HDFC_SMS_PAYMENT_LINK requirement is not satisfied")
    void verifyEntityEdcChannelInfoAPI_otherThanHDFC_SMS_PAYMENT_LINK() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("qa14as12201372052515");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"), "[false]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when under acquiring, bank is other than HDUS")
    void verifyEntityEdcChannelInfoAPI_otherThanHDUS() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("qa14as89451941684930");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"), "[false]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when BANK_TID in ENTITY_EDC_INFO is not the same as tid in PAYLOAD column of ENTITY_EDC_CHANNEL_INFO")
    void verifyEntityEdcChannelInfoAPI_differentBankTID() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("qa14as48365012032723");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"), "[false]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when, Instead of HFPP as BANK_NAME in ENTITY_EDC_INFO table, we use some other BANK_NAME(PEDC)")
    void verifyEntityEdcChannelInfoAPI_otherThanHFPP() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("qa14as28062120078262");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"), "[false]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when terminal status is NOT Active")
    void verifyEntityEdcChannelInfoAPI_terminalStatusNotActive() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("qa14as30032845693582");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos.tid3DS2EncKeyFlag"), "[false]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when MID is Random non existing MID")
    void verifyEntityEdcChannelInfoAPI_NegativeCaseMidIsRandom() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("Random12725994266");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos"), "[]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when MID is space")
    void verifyEntityEdcChannelInfoAPI_NegativeCaseMidIsSpace() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info(" ");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "param inlegal");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "f");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00013");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos"), "[]");
    }

    @Test(description = "Verify entityEdcChannelInfo/mid Api case when MID is null")
    void verifyEntityEdcChannelInfoAPI_NegativeCaseMidIsNull() throws InterruptedException {
        mappingApisPG2.Merchant_get_entity_edc_channel_info("null");
        JsonPath withDrawJson1 = executeAndVerify(mappingApisPG2);
        Assert.assertEquals(withDrawJson1.getString("resultInfo.messaage"), "Success");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultStatus"), "S");
        Assert.assertEquals(withDrawJson1.getString("resultInfo.resultCode"), "00000");
        Assert.assertEquals(withDrawJson1.getString("response.entityEdcChannelInfos"), "[]");
    }


}
