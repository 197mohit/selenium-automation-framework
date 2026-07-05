package scripts.api.mappingService;

import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.Bank;
import com.paytm.appconstants.Constants.MerchantContracts;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.mappingHelpers.GetEntityBankDetailsHelper;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.reporting.Owners;
import com.paytm.pgplus.cache.model.*;
import com.paytm.utils.merchant.DatabaseDTO.pgpdb.BankMasterDTO;
import com.paytm.utils.merchant.DatabaseDTO.pgpdb.BankUrlInfoDTO;
import com.paytm.utils.merchant.DbQueries;
import com.paytm.utils.merchant.UtilConstants;
import com.paytm.utils.merchant.helpers.dbHelper.paytmpgdb.BankMasterHelper;
import com.paytm.utils.merchant.helpers.dbHelper.paytmpgdb.LookupDataHelper;
import com.paytm.utils.merchant.helpers.dbHelper.pgpdb.BankMasterHelperPGPDB;
import com.paytm.utils.merchant.helpers.dbHelper.pgpdb.BankUrlInfoHelper;
import com.paytm.utils.merchant.helpers.mappingHelpers.*;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.qameta.allure.Owner;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import org.assertj.core.api.Assertions;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.validation.constraints.Null;
import java.util.List;
import java.util.Map;

import static com.paytm.LocalConfig.PGP_HOST;
import static com.paytm.appconstants.Constants.MappingService.*;
import static io.restassured.RestAssured.given;
import com.paytm.LocalConfig;
import com.paytm.framework.utils.RedisUtil;
import com.paytm.utils.merchant.util.PgpRedisUtil;

/**
 * @author ankuragarwal Date: 10/10/2018
 * Date: 10/10/2018
 * <p>
 * TODO: Class needs to refactored with proper approach for test cases and its private method(same methods can be used by other class as well)
 */
@Owner("Gagandeep")
@Owners(author = "Gagandeep", qa = "Ankur")
public class MappingService extends PGPBaseTest {

//    private static final String rediUrl = LocalConfig.PG_REDIS_URI;

    @Test(description = "Verify /mapping-service/merchant/get/extended/info/{mid} API")
    public void validateMerchExtendedInfo() {
        String mid = MerchantType.Hybrid.getId();
        GetMerchExtndInfoHelper getMerchExtndInfoHelper = new GetMerchExtndInfoHelper(mid);
        getMerchExtndInfoHelper.validateSuccessMessage().validateMerchantId(mid)
                .validateContractId(MerchantContracts.StandardDirectPayAcquiringProd.toString())
                .validateExtendedInfoStatus("ACTIVE").assertAll();
    }


    @Test(description = "Verify response of /mapping-service/get/bankdetails/{bankCode} with REDIS")
    public void validateBankDetail_fromRedis() {
        String bankCode = Bank.ICICI.toString();

        String redisKey = "BANK_DETAILS_" + bankCode;

        GetEntityBankDetailsHelper getEntityBankDetailsHelper = new GetEntityBankDetailsHelper(bankCode);
//        Object obj = RedisUtil.getInstance().get(rediUrl, redisKey.getBytes());
        Object obj = STATIC_REDIS_CLUSTER().get(redisKey.getBytes());
        // delete redis key from cache if found
        if (obj == null) {
            Assertions.fail("Key '" + redisKey + "' not available in redis");

        }

   //     BankMasterDetails bankMasterDetails = (BankMasterDetails) obj;
        getEntityBankDetailsHelper.validateBankId("8565557")
                .validateBankName("ICICI")
                .validateBankDisplayName("ICICI Bank")
                .validateAlipayBankCode("ICICC1IN")
                .validateStatus(true)
                .assertAll();

    }



    @Test(description = "Verify response of /mapping-service/get/bankdetails/{bankCode} with Database")
    public void validateBankDetail_fromDatabase() {
        String bankCode = Bank.ICICI.toString();

        String redisKey = "BANK_DETAILS_" + bankCode;

        GetEntityBankDetailsHelper getEntityBankDetailsHelper = new GetEntityBankDetailsHelper(bankCode);
//        Object obj = RedisUtil.getInstance().get(rediUrl, redisKey.getBytes());
        Object obj = TRANSACTIONAL_REDIS_CLUSTER().get(redisKey.getBytes());
        if (obj != null) {
            TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        }


        Map<String, Object> dbResult = getbankDetailFromDb(bankCode);
        getEntityBankDetailsHelper.validateBankId(dbResult.get("BANK_ID").toString())
                .validateBankName(dbResult.get("BANK_NAME").toString())
                .validateBankDisplayName(dbResult.get("BANK_DISPLAY_NAME").toString())
                .validateBankKey(dbResult.get("BANK_KEY").toString())
                .assertAll();

    }


    private Map<String, Object> getMerchantOfferDetailFromDb(String mid) {
        String dbQuery = DbQueries.SELECT_FROM_MERCHANT_OFFER_DETAILS(mid);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPaytmPGDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }

    private Map<String, Object> getEntityUrlInfoFromDB(String entityId, String urlTypeId, String websiteName) {
        String dbQuery = DbQueries.SELECT_ENTIRY_URL_INFO(entityId, websiteName, urlTypeId);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPaytmPGDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }


    private String getIdByPaymode_DB(String paymode) {
        String dbQuery = DbQueries.SELECT_ID_FROM_LOOK_UP_PAYMENT_MODE(paymode);
        String id = DbQueriesUtil.selectFromPGPDB(dbQuery, "ID");
        if (id.equals("") || id.equals(null))
            Assertions.fail("No Lookup id found based on Paymode: " + paymode);
        return id;
    }


    private Map<String, Object> getbankDetailFromDb(String bankCode) {
        String dbQuery = DbQueries.SELECT_FROM_BANK_MASTER_DETAILS(bankCode);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPaytmPGDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }

    @Test(description = "Verify /mapping-service/common/v1/get/contract/paymentInfo/{mid}/BALANCE/{contractID} API")
    public void validateGetMerchContractPaymentInfo() {
        String mid = Constants.MerchantType.PGOnly.getId();
        GetMerchCntrctPayInfoHelper getMerchCntrctPayInfoHelper = new GetMerchCntrctPayInfoHelper(mid,
                MerchantContracts.StandardDirectPayAcquiringProd.toString());
        getMerchCntrctPayInfoHelper.validatePayMethod("BALANCE")
                .validateFeeRateNotEmpty()
                .validatePayMethodNotEmpty()
                .assertAll();
    }

    @Test(description = "Verify /mapping-service/get/lookup/PAYMENT_MODE/{paymodeType} for CC paymode")
    public void validateLookUpPaymodeAPI_CC() {
        String lookUpId = getIdByPaymode_DB("CC");
        GetLookupPaymodeHelper getLookupPaymodeHelper = new GetLookupPaymodeHelper("CC");
        getLookupPaymodeHelper.validateID(lookUpId)
                .validateCategory("PAYMENT_MODE")
                .validateName("CC")
                .assertAll();
    }

    @Test(description = "Verify /mapping-service/merchant/get/preference/info/{mid} API")
    public void validateMerchPreferenceInfo() {
        String mid = Constants.MerchantType.Hybrid.getId();
        GetMerchPreferenceInfoHelper getMerchPreferenceInfoHelper = new GetMerchPreferenceInfoHelper(mid);
        getMerchPreferenceInfoHelper.validateStatuCode()
                .validateMerchantId(mid)
                .validatePreferenceInfoListNotEmty()
                .assertAll();
    }

    @Test(description = "Verify /mapping-service/get/entityurlinformid/{mid}/RESPONSE/retail")
    public void validateGetEntityUrlInfoAPI_Response() {
        String mid = Constants.MerchantType.Hybrid.getId();
        String url_type_id = getLookUpId_FromLookUpData("RESPONSE");
        String entity_id = getEntityId_EntityInfo(mid);
        Map<String, Object> resultData = getEntityUrlInfoFromDB(entity_id, url_type_id, "retail");

        GetEntityUrlInfoHelper getEntityUrlInfoHelper = new GetEntityUrlInfoHelper(mid, "RESPONSE");
        getEntityUrlInfoHelper.validateMID(mid)
                .validatePostBackUrl(resultData.get("POST_BACK_URL").toString())
                .validateNotificationUrl(resultData.get("NOTIFICATION_STATUS_URL").toString())
                .validateMID(mid)
                .validateStatus("ACTIVE")
                .validateUrlTypeId("RESPONSE")
                .assertAll();
    }

    @Test(description = "Verify /mapping-service/get/entityurlinformid/{mid}/REQUEST/retail")
    public void validateGetEntityUrlInfoAPI_Request() {
        String mid = Constants.MerchantType.Hybrid.getId();
        String url_type_id = getLookUpId_FromLookUpData("REQUEST");
        String entity_id = getEntityId_EntityInfo(mid);
        Map<String, Object> resultData = getEntityUrlInfoFromDB(entity_id, url_type_id, "retail");

        GetEntityUrlInfoHelper getEntityUrlInfoHelper = new GetEntityUrlInfoHelper(mid, "REQUEST");
        getEntityUrlInfoHelper.validateMID(mid)
                .validatePostBackUrl(resultData.get("POST_BACK_URL").toString())
                .validateNotificationUrl(resultData.get("NOTIFICATION_STATUS_URL").toString())
                .validateMID(mid)
                .validateStatus("ACTIVE")
                .validateUrlTypeId("REQUEST")
                .assertAll();
    }

    @Test(description = "Verify /mapping-service/get/paytmproperties/{propertyName} API")
    public void validateGetPaytmProperties_API() {
        String propertyName = "fetch.success.rate";
        GetPaytmPropertiesHelper getPaytmPropertiesHelper = new GetPaytmPropertiesHelper(propertyName);
        getPaytmPropertiesHelper.validateName(propertyName)
                .validatePropertyValueNotEmpty()
                .assertAll();
    }

    @Test(description = "Verify response of /mapping-service/get/entityofferdetailsformid  with REDIS")
    public void validateEntityOfferDetail_fromRedis() {
        String mid = Constants.MerchantType.OFFERMID.getId();
        String redisKey = "ENTITY_OFFER_DETAILS_" + mid + "_WEB_retail";

//        if (PgpRedisUtil.getRedisKey(rediUrl, redisKey) != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        if(STATIC_REDIS_CLUSTER().get(redisKey) != null)
            STATIC_REDIS_CLUSTER().del(redisKey);

        GetEntityOfferDetailHelper getEntityOfferDetailHelper = new GetEntityOfferDetailHelper(mid, "WEB");
//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = STATIC_REDIS_CLUSTER().get(redisKey);
        if (obj == null) {
            Assertions.fail("Key '" + redisKey + "' not available in redis");
        }
   //     MerchantOfferDetails merchantOfferDetails = (MerchantOfferDetails) obj;

        getEntityOfferDetailHelper.validateChannel("WEB")
                .validateMessage("promo")
                .validateMID("YRfsHa55002149964486")
                .validateStatus("9376503")
                .validateWebsiteName("retail")
                .assertAll();
    }

    @Test(description = "Verify response of /mapping-service/get/entityofferdetailsformid with DataBase")
    public void validateEntityOfferDetail_fromDB() {
        String mid = Constants.MerchantType.OFFERMID.getId();
        String redisKey = "ENTITY_OFFER_DETAILS_" + mid + "_WEB_retail";
        GetEntityOfferDetailHelper getEntityOfferDetailHelper = new GetEntityOfferDetailHelper(mid, "WEB");
//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = TRANSACTIONAL_REDIS_CLUSTER().get(redisKey);
        if (obj != null) {
            TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        }
        Map<String, Object> dbResult = getMerchantOfferDetailFromDb(mid);
        getEntityOfferDetailHelper.validateWebsiteName(dbResult.get("WEBSITE").toString())
                .validateChannel(dbResult.get("CHANNEL").toString())
                .validateMessage(dbResult.get("MESSAGE").toString())
                .validateMID(dbResult.get("MID").toString())
                .validateStatus(dbResult.get("STATUS").toString())
                .assertAll();
    }

    @Test(description = "Validate success of /mapping-service/get/formatter/ICICI/CC with dataBase")
    public void validateGetFormatter_fromDB() {
        String bankCode = "ICICI";
        String payMethod = "CC";
        String redisKey = "FORMATTER_" + bankCode + "_" + payMethod;
        Map<String, Object> dbResult1 = getFormatterDetailsFromDb(bankCode, payMethod);
        GetFormatterHelper getFormatterHelper = new GetFormatterHelper(bankCode, payMethod);
//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = TRANSACTIONAL_REDIS_CLUSTER().get(redisKey);
        if (obj != null) {
            TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        }
        Map<String, Object> dbResult = getFormatterDetailsFromDb(bankCode, payMethod);
        getFormatterHelper.validateId(Integer.valueOf(dbResult.get("ID").toString()))
                .validateBankCode(dbResult.get("BANK_CODE").toString())
                .validateFormatterName(dbResult.get("FORMATTER_NAME").toString())
                .validateParams(dbResult.get("PARAMS").toString())
                .validatePayMethod(dbResult.get("PAY_METHOD").toString())
                .assertAll();
    }

    @Test(description = "Validate success of /mapping-service/get/formatter/ICICI/CC with Redis")
    public void validateGetFormatter_fromRedis() {
        String bankCode = "ICICI";
        String payMethod = "CC";
        String redisKey = "FORMATTER_" + bankCode + "_" + payMethod;

//        if (PgpRedisUtil.getRedisKey(rediUrl, redisKey) != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        if(STATIC_REDIS_CLUSTER().get(redisKey) != null)
            STATIC_REDIS_CLUSTER().del(redisKey);

        GetFormatterHelper getFormatterHelper = new GetFormatterHelper(bankCode, payMethod);
//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = STATIC_REDIS_CLUSTER().get(redisKey);
        if (obj == null)
            Assertions.fail("Key '" + redisKey + "' not available in redis");
System.out.println("object--->"+obj);

    //    FormatterProperties formatterProperties = (FormatterProperties) obj;
    //    getFormatterHelper.validatePayMethod(formatterProperties.getPayMethod())
        getFormatterHelper.validateParams("sqet=15;")
                .validateFormatterName("HDFCFormatterImpl")
                .validateBankCode("ICICI")
                .validateId(125)
                .validateStatus(true)
                .assertAll();
    }

    @Test(description = "Validate success response of /mapping-service/get/bankurlinfo with Redis")
    public void validateBankUrlInfo_fromRedis() {
        String bankName = UtilConstants.BankName.ICICI.toString();
        String payMethodType = UtilConstants.PayMethod_DB.NB.toString();
        String channel = UtilConstants.PayMethod_DB.WEB.toString();

        String bankMasterQuery = DbQueries.SELECT_FROM_BANK_MASTER_DETAILS(bankName);
        BankMasterHelper bankMasterHelper = new BankMasterHelper(bankMasterQuery);
        String bankId = bankMasterHelper.getResult(0).getBankId().toString();

        String payMethodQuery = DbQueries.SELECT_ID_FROM_LOOK_UP_PAYMENT_MODE(payMethodType);
        LookupDataHelper lookupDataHelper = new LookupDataHelper(payMethodQuery);
        String paymethodLookupId = lookupDataHelper.getResult(0).getLookupId().toString();

        String channelQuery = DbQueries.SELECT_ID_FROM_LOOK_UP_PAYMENT_MODE(channel);
        lookupDataHelper = new LookupDataHelper(channelQuery);
        String channelLookupId = lookupDataHelper.getResult(0).getLookupId().toString();

        String redisKey = "BANK_URL_" + bankId + "_" + paymethodLookupId + "_" + channelLookupId;

//        if (PgpRedisUtil.getRedisKey(rediUrl, redisKey) != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        if(STATIC_REDIS_CLUSTER().get(redisKey) != null)
            STATIC_REDIS_CLUSTER().del(redisKey);

        GetBankUrlInfoHelper getBankUrlInfoHelper = new GetBankUrlInfoHelper(bankId, paymethodLookupId, channelLookupId);
//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = STATIC_REDIS_CLUSTER().get(redisKey);
        System.out.println("object--->"+obj);
        if (obj == null)
            Assertions.fail("Key '" + redisKey + "' not available in redis");
    //    BankUrlDetails bankUrlDetails = (BankUrlDetails) obj;

        getBankUrlInfoHelper.validateBankId(8565557L)
                .validateChannelId(345678909L)
                .validatePayMethodId(345678915L)
                .validateRefundStatusUrl("https://shopping.icicibank.com/corp/BANKAWAY?IWQRYTASKOBJNAME=bay_mc_login&BAY_BANKID=ICI")
                .validateRefundUrl("http://dumybank.com")
                .validateStatusQueryUrl("https://pgp-automation.paytm.in/mockbank/corp/BANKAWAY?IWQRYTASKOBJNAME=bay_mc_login&BAY_BANKID=ICI")
                .validateUrl(null)
                .validateUrlType(null)
                .validateWebPayUrl("https://pgp-automation.paytm.in/mockbank/bankFormatter/iciciFormatter/request1.jsp")
                .validateWebResponseUrl("https://pgp-automation.paytm.in/instaproxy/bankresponse/ICICI/NB/RESP")
                .validateS2sPayUrl("http://dumybank.com")
                .assertAll();
    }

    @Test(description = "Validate success response of /mapping-service/get/bankurlinfo with Database")
    public void validateBankUrlInfo_fromDB() {
        String bankName = UtilConstants.BankName.ICICI.toString();
        String payMethodType = UtilConstants.PayMethod_DB.NB.toString();
        String channel = UtilConstants.PayMethod_DB.WEB.toString();

        String bankMasterQuery = DbQueries.SELECT_FROM_BANK_MASTER_DETAILS(bankName);
        BankMasterHelper bankMasterHelper = new BankMasterHelper(bankMasterQuery);
        String bankId = bankMasterHelper.getResult(0).getBankId().toString();

        String payMethodQuery = DbQueries.SELECT_ID_FROM_LOOK_UP_PAYMENT_MODE(payMethodType);
        LookupDataHelper lookupDataHelper = new LookupDataHelper(payMethodQuery);
        String paymethodLookupId = lookupDataHelper.getResult(0).getLookupId().toString();

        String channelQuery = DbQueries.SELECT_ID_FROM_LOOK_UP_PAYMENT_MODE(channel);
        lookupDataHelper = new LookupDataHelper(channelQuery);
        String channelLookupId = lookupDataHelper.getResult(0).getLookupId().toString();

        String redisKey = "BANK_URL_" + bankId + "_" + paymethodLookupId + "_" + channelLookupId;
//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = TRANSACTIONAL_REDIS_CLUSTER().get(redisKey);
        if (obj != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);

        String bankUrlInfoQuery = "SELECT * FROM BANK_URL_INFO WHERE BANK_ID='" + bankId +
                "' AND PAY_METHOD_ID='" + paymethodLookupId + "' AND CHANNEL_ID='" + channelLookupId + "'";
        BankUrlInfoDTO bankUrlInfoDTO = new BankUrlInfoHelper(bankUrlInfoQuery).getResult(0);
        GetBankUrlInfoHelper getBankUrlInfoHelper = new GetBankUrlInfoHelper(bankId, paymethodLookupId, channelLookupId);
        getBankUrlInfoHelper.validateS2sPayUrl(bankUrlInfoDTO.getS2sPayUrl().toString())
                .validateWebResponseUrl(bankUrlInfoDTO.getWebResponseUrl().toString())
                .validateWebPayUrl(bankUrlInfoDTO.getWebPayUrl().toString())
                .validateStatusQueryUrl(bankUrlInfoDTO.getStatusQryUrl().toString())
                .validateRefundUrl(bankUrlInfoDTO.getRefundUrl().toString())
                .validateRefundStatusUrl(bankUrlInfoDTO.getRefundStatusUrl().toString())
                .validatePayMethodId((Long) bankUrlInfoDTO.getPayMethodId())
                .validateChannelId((Long) bankUrlInfoDTO.getChannelId())
                .validateBankId((Long) bankUrlInfoDTO.getBankId())
                .assertAll();
    }

    @Test(description = "Validate success response of /mapping-service/get/paytmid with DataBase")
    public void validateGetPaytmId_FromDB() {
        String mid = Constants.MerchantType.Hybrid.getId();
        Map<String, Object> dbResult = getFrom_ALIPAY_MERCHANT_ByMid(mid);
        String alipayId = dbResult.get("alipay_merchant_id").toString();
        String redisKey = "MID_A_P_" + alipayId;

//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = TRANSACTIONAL_REDIS_CLUSTER().get(redisKey);
        if (obj != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);

        GetPaytmIdHelper getPaytmIdHelper = new GetPaytmIdHelper(alipayId);
        getPaytmIdHelper.validateAlipayId(alipayId)
                .validateIndustryTypeId(dbResult.get("industry_type_id").toString())
                .validatePaytmId(mid)
                .assertAll();
    }

    @Test(description = "Validate success response of /mapping-service/get/paytmid with Redis")
    public void validateGetPaytmId_FromRedis() {
        String mid = Constants.MerchantType.Hybrid.getId();
        Map<String, Object> dbResult = getFrom_ALIPAY_MERCHANT_ByMid(mid);
        String alipayId = dbResult.get("alipay_merchant_id").toString();
        String redisKey = "MID_A_P_" + alipayId;

//        if (PgpRedisUtil.getRedisKey(rediUrl, redisKey) != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);

        if(STATIC_REDIS_CLUSTER().get(redisKey) != null)
            STATIC_REDIS_CLUSTER().del(redisKey);
        GetPaytmIdHelper getPaytmIdHelper = new GetPaytmIdHelper(alipayId);

//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = STATIC_REDIS_CLUSTER().get(redisKey);
        if (obj == null)
            Assertions.fail("Key '" + redisKey + "' not available in redis");
    //    MerchantData merchantData = (MerchantData) obj;

        getPaytmIdHelper.validateAlipayId("216820000007749148410")
                .validateIndustryTypeId("345678920")
                .validatePaytmId("AUTOME19638290371021")
                .assertAll();
    }

    @Test(description = "Validate success response of /mapping-service/get/bankdetails/alipaycode/ with Database")
    public void validateGetBankDetails_APCODE_FromDB() {
        String bankName = UtilConstants.BankName.ICICI.toString();
        String dbQuery = DbQueries.SELECT_FROM_BANK_MASTER_DETAILS(bankName);
        BankMasterHelperPGPDB bankMasterHelperPGPDB = new BankMasterHelperPGPDB(dbQuery);
        BankMasterDTO bankMasterDTO = bankMasterHelperPGPDB.getResult(0);
        String alipayCode = bankMasterDTO.getOldpgCode().toString();
        String redisKey = "BANK_DETAILS_APCODE_" + alipayCode;

//        if (PgpRedisUtil.getRedisKey(rediUrl, redisKey) != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        if(TRANSACTIONAL_REDIS_CLUSTER().get(redisKey) != null)
            TRANSACTIONAL_REDIS_CLUSTER().del(redisKey);

        GetBankDetailsApCodeHelper getBankDetailsApCodeHelper = new GetBankDetailsApCodeHelper(alipayCode);
        getBankDetailsApCodeHelper.validateAlipayBankCode(bankMasterDTO.getOldpgCode().toString())
                .validateBankCode(bankMasterDTO.getBankCode().toString())
                .validateBankDisplayName(bankMasterDTO.getBankDisplayName().toString())
                .validateBankId(bankMasterDTO.getBankId().toString())
                .validateBankKey(bankMasterDTO.getBankKey().toString())
                .validateBankName(bankMasterDTO.getBankName().toString())
                .assertAll();
    }

    @Test(description = "Validate success response of /mapping-service/get/bankdetails/alipaycode/ with Redis")
    public void validateGetBankDetails_APCODE_FromRedis() {
        String bankName = UtilConstants.BankName.ICICI.toString();
        String dbQuery = DbQueries.SELECT_FROM_BANK_MASTER_DETAILS(bankName);
        BankMasterHelperPGPDB bankMasterHelperPGPDB = new BankMasterHelperPGPDB(dbQuery);
        BankMasterDTO bankMasterDTO = bankMasterHelperPGPDB.getResult(0);
        String alipayCode = bankMasterDTO.getOldpgCode().toString();
        String redisKey = "BANK_DETAILS_APCODE_" + alipayCode;

//        if (PgpRedisUtil.getRedisKey(rediUrl, redisKey) != null)
//            RedisUtil.getInstance().delete(rediUrl, redisKey);
        if(STATIC_REDIS_CLUSTER().get(redisKey) != null)
            STATIC_REDIS_CLUSTER().del(redisKey);

        new GetBankDetailsApCodeHelper(alipayCode);
//        Object obj = PgpRedisUtil.getRedisKey(rediUrl, redisKey);
        Object obj = STATIC_REDIS_CLUSTER().get(redisKey);
        if (obj == null)
            Assertions.fail("Key '" + redisKey + "' not available in redis");
    }



    @Test(description = "Verify redis key with Dummy prefix when empty response from /mapping-service/get/entityurlinformid/{mid}/RESPONSE/retail")
    public void validateGetEntityUrlInfoAPIWithEmpty_Response() {
        String mid = MerchantType.MAPPING_EMPTY_RESPONSE.getId();
        String redisKey = "DUMMY_M_URL_INFO_"+mid+"RESPONSEDEFAULT";
        String reqType = "RESPONSE";
        String websiteName = "DEFAULT";
        given(new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_ENTITY_URL_INFO)
                .build())
                .pathParam("mid",mid)
                .pathParam("reqType",reqType)
                .pathParam("websiteName",websiteName)
                .get();
        Assert.assertNotNull(STATIC_REDIS_CLUSTER().get(redisKey));
        Assert.assertTrue(STATIC_REDIS_CLUSTER().ttl(redisKey)<=5200);  //ttl should be equal to or less than 1 hour
        Assert.assertTrue(STATIC_REDIS_CLUSTER().ttl(redisKey)<=5150);   //ttl could not be less than 3500 having delay of 50 sec
    }


    @Test(description = "Verify redis key with Dummy prefix when empty response from /mapping-service/get/entityurlinformid/v2/{mid}/RESPONSE/retail")
    public void validateGetEntityUrlInfov2APIWithEmpty_Response() {
        String mid = MerchantType.MAPPING_EMPTY_RESPONSE.getId();
        String redisKey = "DUMMY_M_URL_INFO_"+mid+"RESPONSEretail";
        String reqType = "RESPONSE";
        String websiteName = "retail";
        given(new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_ENTITY_URL_INFO_V2)
                .build())
                .pathParam("mid",mid)
                .pathParam("reqType",reqType)
                .pathParam("websiteName",websiteName)
                .get();
        Assert.assertNotNull(STATIC_REDIS_CLUSTER().get(redisKey));
        Assert.assertTrue(STATIC_REDIS_CLUSTER().ttl(redisKey)<=5200);  //ttl should be equal to or less than 1 hour
        Assert.assertTrue(STATIC_REDIS_CLUSTER().ttl(redisKey)<=5150);  //ttl could not be less than 3500 having delay of 50 sec
    }


    @Test(description = "Verify redis key with Dummy prefix when empty response of /mapping-service/get/entityofferdetailsformid")
    public void validateEntityOfferDetail_withEmptyResponse() {
        String mid = Constants.MerchantType.Hybrid.getId();
        String redisKey = "DUMMY_ENTITY_OFFER_DETAILS_" + mid + "_RESPONSE_DEFAULT";
        String channel = "RESPONSE";
        String websiteName = "DEFAULT";
        given(new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setBaseUri(PGP_HOST)
                .setBasePath(GET_ENTITY_OFFER_DETAIL)
                .build())
                .pathParam("mid",mid)
                .pathParam("channel",channel)
                .pathParam("websiteName",websiteName)
                .get();
        Assert.assertNotNull(STATIC_REDIS_CLUSTER().get(redisKey));
        Assert.assertTrue(STATIC_REDIS_CLUSTER().ttl(redisKey)<=5200);  //ttl should be equal to or less than 1 hour
        Assert.assertTrue(STATIC_REDIS_CLUSTER().ttl(redisKey)<=5150);   //ttl could not be less than 3500 having delay of 50 sec
    }

















//===============================================================================================

    private Map<String, Object> getFrom_ALIPAY_MERCHANT_ByMid(String mid) {
        String dbQuery = DbQueries.SELECT_FROM_ALIPAY_PAYTM_MERCHANT(mid);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }


    private Map<String, Object> getFormatterDetailsFromDb(String bankCode, String payMethod) {
        String dbQuery = DbQueries.SELECT_FROM_FORMATTER_DETAILS(bankCode, payMethod);
        List<Map<String, Object>> resultList = DbQueriesUtil.selectFromPGPDB(dbQuery);
        if (resultList.isEmpty())
            Assertions.fail("No result found in DB for Query: " + dbQuery);
        return resultList.get(0);
    }

    private String getEntityId_EntityInfo(String mid) {
        String dbQuery = DbQueries.ENTITY_ID_FROM_ENTITY_INFO(mid);
        String entity_id = DbQueriesUtil.selectFromPaytmPGDB(dbQuery, "ID");
        if (entity_id.equals("") || entity_id.equals(null))
            Assertions.fail("No entity_id found based on mid value: " + mid);
        return entity_id;
    }

    private String getLookUpId_FromLookUpData(String lookupValue) {
        String dbQuery = DbQueries.SELECT_FROM_LOOKUP_DATA_BY_VALUE(lookupValue);
        String lookupId = DbQueriesUtil.selectFromPaytmPGDB(dbQuery, "LOOKUP_ID");
        if (lookupId.equals("") || lookupId.equals(null))
            Assertions.fail("No Lookup id found based on lookup value: " + lookupValue);
        return lookupId;
    }
}
