package scripts.api.binCheck;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.api.MappingService.MerchantAddPreferenceInfo;
import com.paytm.api.MappingService.MigrationDetails;
import com.paytm.api.boss.staticPrefUpdateApi;
import com.paytm.api.pgmc.SetPrefDataApi;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.base.test.PGPBaseTest;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantAddPreferenceInfoReq;
import com.paytm.dto.mappingService.addMerchantPreferenceReq.MerchantPreferenceInfo;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.api.MappingService.GetMerchPreferenceInfo;
import io.qameta.allure.Owner;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.SoftAssertions;
import org.fest.assertions.api.Assertions;
import org.testng.annotations.Test;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Sourav Singh and Updated by Nirottam Singh to integrate Boss Api
 * To check checksum for all MIDs in smoke test before Regression Execution
 **/

@Owner("Sourav")
public class MidCheck extends PGPBaseTest {

    /**
     * This method with read merchant.yaml configuration and check using mapping-service API.
     * Any discrepancy in result will be update by API only.
     *
     * At the end merchant config will be auto updated.
     */
    @Test(description = "verify/update merchant configuration based on merchant.yaml")
    public void testMerchants() {
        List<Map<Constants.MerchantType, Object>> updatePrefList = new ArrayList<>();
        List<Map<Constants.MerchantType, Object>> updateExtendInfoList = new ArrayList<>();
        Arrays.stream(Constants.MerchantType.values())
//                .parallel()
                .filter(item -> !item.equals(Constants.MerchantType.BASIC_PREFERENCES))
                .filter(item -> !item.equals(Constants.MerchantType.NonMigrated))
                .filter(item -> {
                    try {
                        System.out.println("Checking for merchant label: " + item.name());
                        Reporter.report.info("Checking for merchant label: " + item.name());
                        Map<String, String> verifyPref = getVerifyPreferences(item);
                        Response r = getMigrationResponse(item.getId());
                        List<Map> prefList = getMerchantPreferences(r);

                        verifyPref:{
                            List<Map.Entry<String, String>> resultList = verifyPref.entrySet()
                                    .stream()
                                    .filter(e -> !verifyPreference(e.getKey(), e.getValue(), prefList))
                                    .collect(Collectors.toList());
                            if (!resultList.isEmpty()) {
                                Map<Constants.MerchantType, Object> m = new HashMap<>();
                                m.put(item, resultList);
                                updatePrefList.add(m);
                            }
                        }
                        verifyExt:
                        {
                            Map<String, String> verifyExtInfo = getVerifyExtendInfo(item);
                            Map<String, Object> extendInfo = getMerchantExtendInfo(r);
                            List<Map.Entry<String, String>> resultList = verifyExtInfo.entrySet()
                                    .stream().filter(e -> !verifyExtendInfo(e.getKey(), e.getValue(), extendInfo))
                                    .collect(Collectors.toList());
                            if (!resultList.isEmpty()) {
                                Map<Constants.MerchantType, Object> m = new HashMap<>();
                                m.put(item, resultList);
                                updateExtendInfoList.add(m);
                            }
                        }
                        return true;       // currently not using filtered output
                    } catch (Exception e) {
                        Map<Constants.MerchantType, Object> m = new HashMap<>();
                        m.put(item, null);
                        updateExtendInfoList.add(m);
                        updatePrefList.add(m);
                    }
                    return true;        // currently not using filtered output
                })
                .collect(Collectors.toList());

        Reporter.report.info("Conditions are in reversal manner .......");
        Reporter.report.info("Updating preference for merchants: ========= " + updatePrefList);
        updateMerchantPreferences(updatePrefList);

        Reporter.report.info("[TODO] [MANUALLY] :::::::::: Updating extendInfo for merchants: ========== " + updateExtendInfoList);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(updateExtendInfoList)
                .as("Merchants extendInfo are not updated as per merchant.yaml")
                .isEmpty();
        softAssertions.assertThat(updatePrefList)
                .as("Merchants preference are not updated as per merchant.yaml")
                .isEmpty();
        softAssertions.assertAll();
    }

    /**
     * Reads merchant.yaml configuration, verifies preferences via mapping-service,
     * and updates any discrepancies using the PGMC setPrefData API.
     * All preferences for a merchant are sent in a single request.
     */
    @Test(description = "verify/update merchant configuration using PGMC setPrefData API")
    public void testMerchantsWithPgmc() {
        List<Map<Constants.MerchantType, Object>> updatePrefList = new ArrayList<>();
        List<Map<Constants.MerchantType, Object>> updateExtendInfoList = new ArrayList<>();
        Arrays.stream(Constants.MerchantType.values())
                .filter(item -> !item.equals(Constants.MerchantType.BASIC_PREFERENCES))
                .filter(item -> !item.equals(Constants.MerchantType.NonMigrated))
                .filter(item -> {
                    try {
                        String mid = item.getId();
                        if (mid == null || mid.isEmpty()) {
                            Reporter.report.info("PGMC setPrefData - Skipping merchant " + item.name() + " - no MID configured in merchant.yaml");
                            return false;
                        }
                        Reporter.report.info("PGMC setPrefData - Checking for merchant label: " + item.name() + " [MID: " + mid + "]");
                        Map<String, String> verifyPref = getVerifyPreferences(item);
                        Response r = getMigrationResponse(mid);
                        List<Map> prefList = getMerchantPreferences(r);

                        List<Map.Entry<String, String>> resultList = verifyPref.entrySet()
                                .stream()
                                .filter(e -> !verifyPreference(e.getKey(), e.getValue(), prefList))
                                .collect(Collectors.toList());
                        if (!resultList.isEmpty()) {
                            Map<Constants.MerchantType, Object> m = new HashMap<>();
                            m.put(item, resultList);
                            updatePrefList.add(m);
                        }

                        Map<String, String> verifyExtInfo = getVerifyExtendInfo(item);
                        Map<String, Object> extendInfo = getMerchantExtendInfo(r);
                        List<Map.Entry<String, String>> extResultList = verifyExtInfo.entrySet()
                                .stream()
                                .filter(e -> !verifyExtendInfo(e.getKey(), e.getValue(), extendInfo))
                                .collect(Collectors.toList());
                        if (!extResultList.isEmpty()) {
                            Map<Constants.MerchantType, Object> m = new HashMap<>();
                            m.put(item, extResultList);
                            updateExtendInfoList.add(m);
                        }
                        return true;
                    } catch (Exception e) {
                        Reporter.report.error("Error checking merchant " + item.name() + ": " + e.getMessage());
                        Map<Constants.MerchantType, Object> m = new HashMap<>();
                        m.put(item, null);
                        updateExtendInfoList.add(m);
                        updatePrefList.add(m);
                    }
                    return true;
                })
                .collect(Collectors.toList());

        Reporter.report.info("PGMC setPrefData - Updating preferences for merchants: ========= " + updatePrefList);
        updateMerchantPreferencesViaPgmc(updatePrefList);

        Reporter.report.info("[TODO] [MANUALLY] :::::::::: Updating extendInfo for merchants: ========== " + updateExtendInfoList);
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(updateExtendInfoList)
                .as("Merchants extendInfo are not updated as per merchant.yaml")
                .isEmpty();
        softAssertions.assertThat(updatePrefList)
                .as("Merchants preference are not updated as per merchant.yaml (via PGMC setPrefData)")
                .isEmpty();
        softAssertions.assertAll();
    }

    private void updateMerchantPreferencesViaPgmc(List<Map<Constants.MerchantType, Object>> midList) {
        midList.forEach(item -> {
            Constants.MerchantType merchantType = item.keySet().iterator().next();
            String mid = merchantType.getId();
            if (mid == null || mid.isEmpty()) {
                Reporter.report.info("Skipping merchant " + merchantType.name() + " - no MID configured");
                return;
            }

            Map<String, Object> prefData = new LinkedHashMap<>();

            Constants.MerchantType.BASIC_PREFERENCES.getPreferences()
                    .forEach((key, value) -> prefData.put(key, parsePreferenceValue(value)));

            merchantType.getPreferences()
                    .forEach((key, value) -> prefData.put(key, parsePreferenceValue(value)));

            String requestBody;
            try {
                requestBody = new ObjectMapper().writeValueAsString(prefData);
            } catch (Exception e) {
                requestBody = prefData.toString();
            }
            String curlCommand = "curl --location '" + LocalConfig.PGMC_HOST
                    + Constants.PGPAPIResourcePath.SET_PREF_DATA + mid
                    + "' --header 'Content-Type: application/json' --data '" + requestBody + "'";
            Reporter.report.info("PGMC setPrefData - CURL: " + curlCommand);

            SetPrefDataApi api = new SetPrefDataApi(mid, prefData);
            Response response = api.execute();
            Reporter.report.info("PGMC setPrefData - Response [" + response.getStatusCode() + "] for MID " + mid + ": " + response.asString());
        });
    }

    private Object parsePreferenceValue(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        if (trimmed.startsWith("{")) {
            try {
                return new ObjectMapper().readValue(trimmed, LinkedHashMap.class);
            } catch (Exception ignored) {
            }
        }
        try {
            return Long.parseLong(trimmed);
        } catch (NumberFormatException ignored) {
        }
        return value;
    }

    private void updateMerchantPreferences(List<Map<Constants.MerchantType, Object>> midList) {
        midList.forEach(item -> {
            Constants.MerchantType merchantType = item.keySet().iterator().next();
            Constants.MerchantType basicMerchantTypePref = Constants.MerchantType.BASIC_PREFERENCES;
            List<MerchantPreferenceInfo> merchantPreferenceInfoList = new ArrayList<>();
            basicMerchantTypePref.getPreferences().forEach((key, value) -> {
              //  this mapping api merchant add pref info  is depreciated now  so commecnting the below code

//                MerchantPreferenceInfo merchantPreferenceInfo = new MerchantPreferenceInfo()
//                        .setPrefStatus("ACTIVE")
//                        .setPrefType(key)
//                        .setPrefValue(value);
//                merchantPreferenceInfoList.add(merchantPreferenceInfo);

                staticPrefUpdateApi s= new staticPrefUpdateApi();
                s.buildRequest(key,value,merchantType.getId().toString());
                String  response=s.execute().asString();
                String resErr="\"SP_401\": \"Invalid Pref name or prefValue\"";
                if(response.contains(resErr)){
                    staticPrefUpdateApi ss= new staticPrefUpdateApi();
                    ss.buildRequestStaticPref(key,value,merchantType.getId().toString());
                    String res=ss.execute().asString();
                }

            });
            merchantType.getPreferences().forEach((key, value) -> {
                //  this mapping api merchant add pref info  is depreciated now  so commecnting the below code

//                Optional<MerchantPreferenceInfo> merchantPreferenceInfoOptional = merchantPreferenceInfoList.stream().filter(m -> m.getPrefType().equalsIgnoreCase(key)).findAny();
//                if (merchantPreferenceInfoOptional.isPresent()) {
//                    merchantPreferenceInfoList.remove(merchantPreferenceInfoOptional.get());
//                }
//                MerchantPreferenceInfo merchantPreferenceInfo = new MerchantPreferenceInfo()
//                        .setPrefStatus("ACTIVE")
//                        .setPrefType(key)
//                        .setPrefValue(value);
//                merchantPreferenceInfoList.add(merchantPreferenceInfo);

                staticPrefUpdateApi s= new staticPrefUpdateApi();
                s.buildRequest(key,value,merchantType.getId().toString());
                String response=s.execute().asString();
                String resErr="\"SP_401\": \"Invalid Pref name or prefValue\"";
                if(response.contains(resErr)){
                    staticPrefUpdateApi ss= new staticPrefUpdateApi();
                    ss.buildRequestStaticPref(key,value,merchantType.getId().toString());
                    String res=ss.execute().asString();
                }

           });
          //  updateMerchant(merchantType, merchantPreferenceInfoList);
        });
    }

    private void updateMerchant(Constants.MerchantType merchantType, List<MerchantPreferenceInfo> merchantPreferenceInfoList){
        System.out.println("updating: " + merchantType);
        if(merchantType.getId() == null)
            return;
        MerchantAddPreferenceInfoReq merchantAddPreferenceInfoReq =
                new MerchantAddPreferenceInfoReq.Builder(merchantType.getId(), "","","")
                .build();


        MerchantAddPreferenceInfo merchantAddPreferenceInfo = new MerchantAddPreferenceInfo(merchantAddPreferenceInfoReq);
        merchantAddPreferenceInfo.setContext("merchantPreferenceInfos", merchantPreferenceInfoList);
//        merchantAddPreferenceInfo.setDisableCurlLogging(true);
        merchantAddPreferenceInfo.execute();
    }

    private Map<String, String> getVerifyPreferences(Constants.MerchantType item) {
        Map<String, String> verifyPref = new HashMap<>();
        verifyPref.putAll(Constants.MerchantType.BASIC_PREFERENCES.getPreferences());
        if (!item.getPreferences().isEmpty())
            verifyPref.putAll(item.getPreferences());
        return verifyPref;
    }

    private Map<String, String> getVerifyExtendInfo(Constants.MerchantType item) {
        Map<String, String> verifyExtendInfo = new HashMap<>();
        verifyExtendInfo.putAll(Constants.MerchantType.BASIC_PREFERENCES.getExtendInfo());
        if (!item.getExtendInfo().isEmpty())
            verifyExtendInfo.putAll(item.getExtendInfo());
        return verifyExtendInfo;
    }

    private Response getMigrationResponse(String mid) {
        MigrationDetails api = new MigrationDetails(mid);
        api.setDisableCurlLogging(true);
        return api.execute();
    }

    private List<Map> getMerchantPreferences(Response response) {
        JsonPath getMerchPrefInfoResp = response.then()
                .extract().jsonPath();
        String merchPrefInfoPath = "MERCHANT-PREFERENCE-INFO.merchantPreferenceInfos";

        try {
            if (null == getMerchPrefInfoResp.getList(merchPrefInfoPath))
                return Collections.EMPTY_LIST;
            return getMerchPrefInfoResp.getList(merchPrefInfoPath);
        } catch (Exception e) {
            return Collections.EMPTY_LIST;
        }
    }

    private Map<String, Object> getMerchantExtendInfo(Response response) {
        JsonPath getMerchExtInfoResp = response.then()
                .extract().jsonPath();
        String merchPrefInfoPath = "MERCHANT-EXTENDED-INFO.extendedInfo";

        try {
            if (null == getMerchExtInfoResp.getMap(merchPrefInfoPath))
                return Collections.EMPTY_MAP;
            return getMerchExtInfoResp.getMap(merchPrefInfoPath);
        } catch (Exception e) {
            return Collections.EMPTY_MAP;
        }
    }

    private boolean verifyExtendInfo(String extendInfoKey, Object extendInfoValue, Map<String, Object> extendInfos) {
        if (null != extendInfos && extendInfos.containsKey(extendInfoKey)) {
            return extendInfos.get(extendInfoKey).equals(extendInfoValue);
        }
        return false;
    }

    private boolean verifyPreference(String prefname, String prefValue, List<Map> prefrences) {
        Map resultMap = prefrences.stream()
                .filter(item -> {
                    try {
                        String actPrefName = item.get("prefType").toString();
                        return actPrefName.equalsIgnoreCase(prefname);
                    } catch (Exception e) {
                        return false;
                    }
                }).findFirst().orElse(Collections.EMPTY_MAP);
        if (resultMap.isEmpty())
            return false;
        String actPrefValue = null != resultMap.get("prefValue") ? resultMap.get("prefValue").toString() : "";
        String actPrefStatus = null != resultMap.get("prefStatus") ? resultMap.get("prefStatus").toString() : "";
        return actPrefValue.equalsIgnoreCase(prefValue) && actPrefStatus.equalsIgnoreCase("ACTIVE");
    }

}