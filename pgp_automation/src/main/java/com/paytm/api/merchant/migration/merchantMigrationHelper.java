package com.paytm.api.merchant.migration;

import com.paytm.ServerConfigProvider;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.boss.BlockUnblockStatus;
import com.paytm.api.boss.MerchantDetails;
import com.paytm.apphelpers.AuthHelpers;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.framework.utils.DatabaseUtil;
import com.paytm.utils.merchant.Constants;
import com.paytm.utils.merchant.util.DbQueriesUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import org.apache.commons.lang3.RandomStringUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.testng.Assert;
import org.testng.ITestListener;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class merchantMigrationHelper extends PGPBaseTest implements ITestListener {

    protected static String requestID;
    protected static String mid;
    protected static String testname;
    protected static String phoneNumber;
    protected static LinkedHashMap<String,String> validation = new LinkedHashMap<>();
    protected static LinkedHashMap<String, LinkedHashMap<String, String>> midConfig = new LinkedHashMap<>();
    protected static LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, String>>> midValidator = new LinkedHashMap<>();


    @BeforeMethod(alwaysRun = true)
    public void generateRequestID(){
        System.out.println("Generating Unique RequestID");
        String auto = "AUTO";
        String generatedString = RandomStringUtils.randomAlphanumeric(13);
        requestID = auto.concat(generatedString);
        System.out.println(requestID);
    }

    @BeforeMethod(alwaysRun = true)
    public void generateMobileNumber(){
        String auto = "50960";
        String generatedString = RandomStringUtils.randomNumeric(5);
        phoneNumber = auto.concat(generatedString);
        AuthHelpers helpers = new AuthHelpers();
        helpers.authCreateNewUser(phoneNumber, "paytm@123");
    }

    @AfterMethod(alwaysRun = true)
    public void teardown(){
        validation.clear();
        midConfig.clear();
        midValidator.clear();
    }


    @Override
    public void onTestStart(ITestResult result) {
        testname = result.getName();
    }


    public void validatePayMethod(String MID, String paymethod){
        validation.put("payMethods", paymethod);
        midConfig.put(MID, validation);
        midValidator.put(testname, midConfig);
        validatingMigration();
    }

    public void validatePayMethod(String MID, String paymethod, String productcode){
        validation.put("payMethods", paymethod);
        validation.put("productCode", productcode);
        midConfig.put(MID, validation);
        midValidator.put(testname, midConfig);
        validatingMigration();
    }

    public void validatePayMethodProductCode(String MID, String paymethod, String productcode){
        MigrationDetails merchant = new MigrationDetails(MID);
        JsonPath resp = merchant.execute().jsonPath();
        String pCode = resp.getString("'MERCHANT-EXTENDED-INFO'.extendedInfo.productCode");
        List<String> pcodeList = Arrays.asList(pCode.split(","));
        Assert.assertTrue(pcodeList.contains(productcode));
    }



    public void validatingMigration(){
        for(String test : midValidator.keySet()) {
            System.out.println("Validating Merchant Migration for TC :" + test);
            System.out.println("Validate this mid for these values" + midValidator.get(test));
            LinkedHashMap<String, LinkedHashMap<String, String>> merchConfig = midValidator.get(test);
            for (String MID : merchConfig.keySet()) {
                System.out.println("Validating mid :" + MID);
                validateMigrationInDB(MID);
                validate(MID, merchConfig.get(MID));
            }
        }
    }


    public static void  validateResponse(JsonPath response){
        String responseStatus = response.get("STATUS");

            Assertions.assertThat(responseStatus)
                    .isNotBlank()
                    .isNotEmpty()
                    .isNotNull()
                    .contains("MID generation is in progress");
        }

    public static String createJwtHMAC(String clientId, String key) {

        byte[] decodedKey = Base64.getMimeDecoder().decode(key);

        Map<String, Object> claims = new HashMap<String, Object>();
        claims.put("client-id", clientId);
        String token = Jwts.builder().setIssuedAt(new Date()).addClaims(claims)
                .signWith(SignatureAlgorithm.HS512, decodedKey).compact();
        return token;
    }



        public static String getMidViaBOSSAPI(String phoneNumber){
            try {
                Thread.sleep(200000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String walletToken = AuthHelpers.getPaytmToken("umesh.garg@paytm.com","paytm@123");

            String jwt=createJwtHMAC("66c02d3e-ebc0-4117-ba85-7f523ac8d424", "WidnixnDo2780hILxdvvQXu9shJ9tIZnSsX4aEe9aKoOg5n7CtkijYqCb0ijNe7SE1qOu38JVU+gfx8G89oWvQ==");

            MerchantDetails merchantDetails = new MerchantDetails(phoneNumber,walletToken,jwt,"66c02d3e-ebc0-4117-ba85-7f523ac8d424");
            JsonPath r = merchantDetails.execute().jsonPath();

           String mid = r.getString("MID").replaceAll("[^a-zA-Z0-9]","");

            return mid;
        }
    //Validating and Extracting the Created Merchant ID
    public static String getMid(String requestID){

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Extracting Admin Panel Logs for Create Merchant Request
        System.out.println("Getting Logs from Admin Panel for this Request ID " +requestID);

        String grepcmd = "grep \"" + requestID + "\" /alog/mysql_admin.log";
        String mysqlAdminLogs = null;
        try {
            mysqlAdminLogs = getLogsOnServer(ServerConfigProvider.SERVICE.ADMIN_PANEL, grepcmd);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(mysqlAdminLogs);

        System.out.println("Validating Checks in Admin Panel Logs for this Request ID " +requestID);

        //Asserting Admin Panel logs for various stages in merchant creation
        Assertions.assertThat(mysqlAdminLogs)
                .as("Could not get Admin Panel Logs").isNotNull()
                .as("Could not get Admin Panel Logs").isNotEmpty()
                .as("Could not get Admin Panel Logs").isNotBlank()
                .as("Looks like Request did not get consumed by Create Merchant consumer").contains("Create Merchant Message Consumer")
                .as("Looks like Request did not get processed by Create Merchant processor").contains("processing request")
                .as("Mid could not be created for this Create Merchant Request").contains("saveMerchant")
                .contains("In prepareTxnTypePayLoadAfterSapCodeGenReq")
                .contains("In prepareTxnTypePayLoad")
                .as("Looks like there is an issue could not create Mid for this Request").contains("mid:");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Extracting mid of the New Created Merchant from Admin Panel Logs
        String grepmid =  "grep \"" + requestID + "\" /alog/mysql_admin.log | grep MerchantServiceUtil.prepareTxnTypePayLoad |sed -n '1,1p'| awk 'NF>1{print $NF}'";
        String mid = null;
        try {
            mid = getLogsOnServer(ServerConfigProvider.SERVICE.ADMIN_PANEL, grepmid);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String midnew = mid.substring(3);
        System.out.println(midnew);
        return midnew;
    }

    //Validating MID Migration Status in PAYTMPGPDB DataBase
    public static boolean validateMigrationInDB(String mid){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String time = formatter.format(date);


        //Extracting Migration Data from DB
    //    String DBQuery = "SELECT * FROM MIGRATION_MID where MID = '"+ mid + "';";
        String DBQuery = "SELECT * FROM MIGRATION_MID where MID = '"+mid+"' AND CREATED_ON like '"+time+"%';";
        List<Map<String, Object>> ExpectedChannelCode = null;
        //Asserting Merchant Migration status in DB
        Awaitility.with().pollInSameThread().await()
                .pollInterval(Duration.TEN_SECONDS).atMost(Duration.TWO_MINUTES)
                .untilAsserted(()->DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "STATUS").contains("MIGRATED"));

        ExpectedChannelCode = DbQueriesUtil.selectFromPaytmPGDB(DBQuery); // Getting Value from DB
        System.out.println(ExpectedChannelCode);

        boolean merchantMigrated = false;
        List<Map<String, Object>> expectedChannelCodes = null;
        expectedChannelCodes =  ExpectedChannelCode.stream()
                .filter(c -> c.containsValue("MERCHANT_MIGRATION")).collect(Collectors.toList());
        if(expectedChannelCodes.contains("STATUS=MIGRATED"))
            {
                validateMerchantUpdateMigrationInDB(mid);
        }
        return merchantMigrated;
    }


    public static boolean validateMerchantUpdateMigrationInDB(String mid){

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String time = formatter.format(date);

        //Extracting Migration Data from DB
        //String DBQuery = "SELECT * FROM MIGRATION_MID where MID = '"+mid+"'AND OPERATION_TYPE like 'MERCHANT_UPDATE%';";

        String DBQuery = "SELECT * FROM MIGRATION_MID where MID = '"+mid+"' \n" +
                "AND CREATED_ON like '"+time+"%' \n" +
                "AND OPERATION_TYPE like 'MERCHANT_UPDATE%';";

        Awaitility.with().pollInSameThread().await().pollInterval(Duration.TEN_SECONDS)
                .atMost(Duration.TWO_MINUTES)
                .untilAsserted(()->DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "STATUS").contains("MIGRATED"));

        List<Map<String, Object>> ExpectedChannelCode = DbQueriesUtil.selectFromPaytmPGDB(DBQuery); // Getting Value from DB
        System.out.println(ExpectedChannelCode);

        //Asserting Merchant Migration status in DB
        boolean merchantMigrated = false;
        List<Map<String, Object>> expectedChannelCodes =  ExpectedChannelCode.stream()
                .filter(c -> c.containsValue("MERCHANT_UPDATE")).collect(Collectors.toList());
        if(expectedChannelCodes.contains("STATUS=MIGRATED")){
            merchantMigrated = true;
        }
        return merchantMigrated;
    }

  public enum PAYMODES {
        WALLET("PPI"),
        CREDIT_CARD("CC"),
        DEBIT_CARD("DC"),
        NET_BANKING("NB"),
        UPI("UPI"),
        GIFT_VOUCHER("GIFT_VOUCHER"),
        LOYALTY_POINT("LOYALTY_POINT");

        final private String paymode;

        PAYMODES(String paymode) {
            this.paymode = paymode;
        }

        @Override
        public String toString() {
            return paymode;
        }
    }


    public enum REQUESTTYPES {
        DEFAULT("DEFAULT"),
        EDC("EDC"),
        AGGREGATOR_PAYOUT("AGGREGATOR_PAYOUT"),
        ADD_MONEY("ADD_MONEY"),
        FOOD_WALLET("FOOD_WALLET"),
        RETRY("RETRY"),
        EMI("EMI"),
        SUBSCRIBE("SUBSCRIBE"),
        RENEW_SUBSCRIPTION("RENEW_SUBSCRIPTION"),
        PRE_AUTH_CAPTURE("PRE_AUTH_CAPTURE"),
        SEAMLESS("SEAMLESS"),
        PAYTM_EXPRESS("PAYTM_EXPRESS"),
        EXPRESS_ADD_MONEY("EXPRESS_ADD_MONEY"),
        MUTUAL_FUNDS("MUTUAL_FUNDS"),
        PAY_CONFIRM_ACQUIRING_UNIVERSAL_PROD("PAY_CONFIRM_ACQUIRING_UNIVERSAL_PROD"),
        SELF_DECLARED_MERCHANT("SELF_DECLARED_MERCHANT"),
        EMAIL_INVOICING("EMAIL_INVOICING"),
        LINK_BASED_PAYMENT("LINK_BASED_PAYMENT");

        final private String requestType;

        REQUESTTYPES(String requestType) {
            this.requestType = requestType;
        }

        @Override
        public String toString() {
            return requestType;
        }
    }


    public enum STATICPREFERENCE {
        RETURN_FEE_RATE_FACTORS("RETURN_FEE_RATE_FACTORS"),
        PCF_FEE_INFO("PCF_FEE_INFO"),
        CORPORATE_CARD_DC("CORPORATE_CARD_DC"),
        CORPORATE_CARD_CC("CORPORATE_CARD_CC"),
        PREPAID_CARD("PREPAID_CARD"),
        appInvokeAllowed("appInvokeAllowed"),
        PREPAID_CARD_PEON("PREPAID_CARD_PEON"),
        AUTO_APP_INVOKE_ALLOWED("AUTO_APP_INVOKE_ALLOWED"),
        nativeJsonRequest("nativeJsonRequest"),
        SETTLE_FREEZE("SETTLE_FREEZE"),
        BW_ENABLED("BW_ENABLED"),
        AGGREGATOR_PAYOUT("AGGREGATOR_PAYOUT"),
        IS_VOID_FEE_SUPPORTED("IS_VOID_FEE_SUPPORTED");

        final private String staticPreference;

        STATICPREFERENCE(String staticPreference) {
            this.staticPreference = staticPreference;
        }

        @Override
        public String toString() {
            return staticPreference;
        }
    }


    public enum SOLUTIONTYPE {
        offline("OFFLINE"),
        online("ONLINE");

        final private String solutionType;

        SOLUTIONTYPE(String solutionType) {
            this.solutionType = solutionType;
        }

        @Override
        public String toString() {
            return solutionType;
        }
    }


    public static void validate(String mid, LinkedHashMap<String, String> map1) {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        MigrationDetails migrationDetails = new MigrationDetails(mid);
        JsonPath r = migrationDetails.execute().jsonPath();
        String productCode = r.get("'MERCHANT-EXTENDED-INFO'.extendedInfo.productCode");

        String payMethod = r.get().toString();

        for(String prop : map1.keySet()){
            if(prop.equals("payMethods")){
                payMethod.contains(map1.get(prop));
            }
            else if(prop.equals("productCode")){
                productCode.contains(map1.get(prop));
            }
        }
    }


    public static void queryPAYTMPGDB(String query) {
        DatabaseUtil.getInstance().executeUpdateQuery(Constants.PAYTMPG_DB_CONNECTION_URL, query);
    }


    public static void validatePrefStatus(String mid,String prefName) throws InterruptedException {
        Thread.sleep(20000);
        String prefExistance="false";
        MigrationDetails migrationDetails = new MigrationDetails(mid);
        JsonPath resp = migrationDetails.execute().jsonPath();
        int s=resp.getList("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos").size();
        for(int i=0;i<s;i++)
        {
            String pref = resp.getString("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos["+i+"].prefType");
            if(pref.equals(prefName))
            {
                String prefStatus = resp.getString("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos["+i+"].prefStatus");
                String prefValue = resp.getString("MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos["+i+"].prefValue");

                Assert.assertEquals(prefStatus,"ACTIVE");
                Assert.assertEquals(prefValue,"Y");
                prefExistance="true";
                break;
            }

        }
        if(prefExistance.equals("false")){
            Assert.fail("This pref is Not Enabled On this Merchant");
        }

    }
    public static void  validateErrorResponse(JsonPath response){
        String responseStatus = response.get("STATUS");
        String responseErrorMSG = response.getString("errorMessage");

        Assertions.assertThat(responseStatus)
                .isNotBlank()
                .isNotEmpty()
                .isNotNull()
                .contains("ERROR");
                   Assertions.assertThat(responseErrorMSG).isEqualTo("Invalid request id or source id");
    }

    public static void  validateProductCodes(String productsCodes, ArrayList<String>ExpectedPcode){
        HashMap<String,String> productCodes=new HashMap<>();
        String pcode="";
        if(productsCodes.isEmpty()){
            Assert.assertEquals(productsCodes,"Product String Can not be Empty");
        }
        for(int i=0;i<productsCodes.length();i=i+1){
            if(productsCodes.charAt(i)!=',' && i<productsCodes.length()-1){
                pcode=pcode+productsCodes.charAt(i);
            }
            else if(productsCodes.charAt(i)==',' || i==productsCodes.length()-1){
                if(i==productsCodes.length()-1)
                    pcode=pcode+productsCodes.charAt(i);
                productCodes.put(pcode,"true");
                pcode="";
            }
        }
        System.out.println("PCodes are==> "+productCodes);
        for(int i=0;i<ExpectedPcode.size();i=i+1){
            if(!productCodes.containsKey(ExpectedPcode.get(i))){
                System.out.println("Product Code is Not Present==>"+ExpectedPcode.get(i));
                Assert.assertEquals(productCodes.get(ExpectedPcode.get(i)),"true");

            }
        }
    }

    public static String validateProductCondition(Map<String,String>pConditions, String ParameterName){
        Iterator<String> keys = pConditions.keySet().iterator();
        String valueOfParameter="";
          if(!pConditions.containsKey(ParameterName)){
              Assert.assertEquals(null,"Parameter is not Present in Product Condition");
          }
          if(pConditions.containsKey(ParameterName)){
               valueOfParameter = pConditions.get(ParameterName);
              System.out.println("value of parameter is = "+valueOfParameter);
          }
        return valueOfParameter;
    }
    public static Map<String,String>getProductConditionMap(JsonPath migrationDetailResponse){
        String response=migrationDetailResponse.toString();
        String contractId="";
        int index=response.indexOf("CONTRACT-DETAIL-");
        index+=16;
        while(response.charAt(index)!=':'){
            contractId=contractId+response.charAt(index);
            index=index+1;
        }
        Map<String,String>pcondition=migrationDetailResponse.getMap("CONTRACT-DETAIL-"+contractId+"");
        System.out.println("pcondition is==>"+pcondition);
       return pcondition;
    }
    @Owner("Nirottam Singh")
    public static String getAlipayId(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayId");
        return alipayId;
    }
    @Owner("Nirottam Singh")
    public static String getReqIdForQuerAssetLogs(String alipayId) throws InterruptedException {
        String grepcmd = "grep \"" + "\" /paytm/logs/mapping-service-facade.log  | " +
                "grep \"" + alipayId+ "\" | grep \"QUERY_ASSETS\" | grep \"REQUEST\" ";
        String mappingServiceLogs = getLogsOnServer(ServerConfigProvider.SERVICE.MAPPING_SERVICE, grepcmd);
        int requestIdidx= mappingServiceLogs.indexOf("clientId");
        String s="clientId";
        String reqId="";
        requestIdidx=requestIdidx+s.length()+3;
        while(mappingServiceLogs.charAt(requestIdidx)!='"'){
            reqId=reqId+mappingServiceLogs.charAt(requestIdidx);
            requestIdidx=requestIdidx+1;
        }
        return reqId;
    }
    @Owner("Nirottam Singh")
    public static void validatePaymodesInMigrationDetail(ArrayList<String>ExpectedPaymodes){
        HashMap<String,String> payMethods=new HashMap<>();
        MigrationDetails migrationDetails = new MigrationDetails(mid);
        JsonPath res = migrationDetails.execute().jsonPath();
        List ACQUIRINGObject= res.getList("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos");
         for(int i=0;i<ACQUIRINGObject.size();i++){
             String payMethodName=res.getString("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos["+i+"].payMethod");
             payMethods.put(payMethodName,"true");
         }
        if(payMethods.isEmpty()){
            Assert.fail("No Paymode Is Configured On Merchant");
        }
         for(int i=0;i<ExpectedPaymodes.size();i++){
             if(payMethods.size()>0){
                 String expectedPayMethodName=ExpectedPaymodes.get(i);
                 if(!payMethods.containsKey(expectedPayMethodName)){
                     Assert.fail("This Paymode Is Not Configured On Merchant==> "+expectedPayMethodName+" ");
                 }
                 else if(payMethods.containsKey(expectedPayMethodName)){
                     payMethods.put(expectedPayMethodName,"false");
                 }
             }
         }
        Iterator<String> keys = payMethods.keySet().iterator();
        while(keys.hasNext()) {
            String key = keys.next();
            if(payMethods.get(key)=="true"){
               Assert.fail("We don't Configure this paymethod in our request==> "+key+" ");
            }
        }
       return ;
    }
    public static String getPaytmId(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String paytmId =withDrawJson.getString("MERCHANT-MAPPING-INFO.paytmId");
        return paytmId;
    }
    public static String getOfficialName(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String officialName =withDrawJson.getString("MERCHANT-MAPPING-INFO.officialName");
        return officialName;
    }
    public static String getSSOId(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String ssoId =withDrawJson.getString("MERCHANT-MAPPING-INFO.ssoId");
        return ssoId;
    }
    public static String getEnityId(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String enityId =withDrawJson.getString("MERCHANT-MAPPING-INFO.enityId");
        return enityId;
    }
    public static String getMerchantType(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String merchantType =withDrawJson.getString("MERCHANT-MAPPING-INFO.merchantType");
        return merchantType;
    }
    public static String getIndustryTypeId(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String industryTypeId =withDrawJson.getString("MERCHANT-MAPPING-INFO.industryTypeId");
        return industryTypeId;
    }
    public static String getPaytmWalletId(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String paytmWalletId =withDrawJson.getString("MERCHANT-MAPPING-INFO.paytmWalletId");
        return paytmWalletId;
    }
    public static String getAlipayWalletId(String mid){
        MigrationDetails merchant = new MigrationDetails(mid);
        JsonPath withDrawJson = merchant.execute().jsonPath();
        String alipayWalletId =withDrawJson.getString("MERCHANT-MAPPING-INFO.alipayWalletId");
        return alipayWalletId;
    }
    public static void validateMigrationStatusInDB(String mid,String expectedStatus) throws InterruptedException {
        Thread.sleep(30000);
        String DBQuery="SELECT * FROM MIGRATION_MID mm where MID= '"+mid+"' order by ID DESC;";
        String status=DbQueriesUtil.selectFromPaytmPGDB(DBQuery, "STATUS");
        if(status.isEmpty()){
            Assert.fail("THERE IS NO ENTRY IN DB FOR THIS MERCHANT");
        }
        int idx=expectedStatus.indexOf(status);
        if(idx==-1){
            Assert.fail("Merchant does not have expected status yet in Migration_MID");
        }
    }
    public static void validateAdminLogsResposeForMerchantCreation(String requestId,String expectedMSG) throws InterruptedException {
        requestId="OE"+requestId;
        String grepcmd = "grep \"" + requestId + "\" /alog/mysql_admin.log";
        String adminLogs = getLogsOnServer(ServerConfigProvider.SERVICE.ADMIN_PANEL, grepcmd);
        System.out.println("Admin Logs Are---"+adminLogs);
        Assertions.assertThat(adminLogs).contains(expectedMSG);
    }
    public static void blockMerchant(String mid){

        String walletToken = AuthHelpers.getWalletToken("bharat.chaudhary@paytm.com","paytm@123");
        BlockUnblockStatus blockMerchant = new BlockUnblockStatus(mid,walletToken);
        blockMerchant.buildRequest(mid);
        JsonPath r = blockMerchant.execute().jsonPath();
        String status=r.getString("status");
        Assert.assertEquals(status,"MID is blocked successfully.");
    }
    public static void validateAlipayId(String alipayId){
        if(alipayId.isEmpty()){
            Assert.fail("alipayId is null or not getting response for this mid from merchant");
        }
    }
    public static  void enablePreAuthEDCOnMerchant(String mid) throws InterruptedException {
        String walletToken = AuthHelpers.getWalletToken("bharat.chaudhary@paytm.com","paytm@123");
        preAuthEDC PreAuthEDC = new preAuthEDC("true",walletToken,mid);
        JsonPath response = PreAuthEDC.execute().jsonPath();
    }

}
