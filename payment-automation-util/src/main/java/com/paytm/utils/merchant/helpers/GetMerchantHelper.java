package com.paytm.utils.merchant.helpers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.reporting.Reporter;
import com.paytm.utils.merchant.api.GetMerchantApi;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.acquirings.MerchantAcquiring;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.contract.ContractDetails;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.emi.MerchantEMI;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.extendedInfo.MerchExtendedInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.MerchantPrefInfo;
import com.paytm.utils.merchant.dto.getMerchantDetailResponse.merchant_prefrence_info.merchantPrefDTO.MerchantPreferenceInfos;
import com.paytm.utils.merchant.util.MerchantPrefType;
import com.paytm.utils.merchant.util.PayMethodType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assertions;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.*;

public class GetMerchantHelper {

    private Response response;
    private JsonPath jsonPath;

    public GetMerchantHelper() { }
    
    public GetMerchantHelper(String mid) {
    	this.response = new GetMerchantApi(mid).execute();
    	Assertions.assertThat(this.response.getStatusCode()).as("STATUS_CODE mismatch").isEqualTo(200);
    	this.jsonPath = response.jsonPath();
    }
    
    @Deprecated
    public GetMerchantHelper fetchMetchantDetails(String mid) {
        GetMerchantApi getMerchantApi = new GetMerchantApi(mid);
        this.response = getMerchantApi.execute();
        this.jsonPath = response.jsonPath();
        Assertions.assertThat(this.response.getStatusCode()).as("status code").isEqualTo(200);
        return this;
    }

    /**
     * Used for retreiving MID details from Alipay
     *
     * @param mid mid to be searched
     * @param retry number of retry performed
     * @param timeInterval time duration between each retry in seconds
     * @return
     */
    public GetMerchantHelper fetchMetchantDetails(String mid, int retry, int timeInterval) {
        int count = 1;
        GetMerchantApi getMerchantDetails = new GetMerchantApi(mid);
        do {
            Reporter.report.info("<b>Attempt '" + count + "' for fetching merchant details from Alipay after " + timeInterval + " seconds</b>");
            this.response = getMerchantDetails.execute();
            this.jsonPath = response.jsonPath();
            if (this.response.getStatusCode() == 200) {
                break;
            }
            count++;
            pause(timeInterval);
        } while (count < retry);
        try {
            Assertions.assertThat(this.response.getStatusCode()).as("status code").isEqualTo(200);
        } catch (AssertionError e) {
            Reporter.report.error(e.getMessage());
        }

        return this;
    }

    public Response getResponse() {
        return this.response;
    }

    public JsonPath getJsonPath() {
        return this.jsonPath;
    }

    private String getEMIresultSatus() {
        return getJsonPath().getString("MERCHANT-EMI-INFO.resultInfo.resultCode");
    }

    private String getMchntAcquringStatus() {
        return getJsonPath().get("MERCHANT-ACQUIRING-INFO.resultInfo.resultCode");
    }

    public GetMerchantHelper verifyMrchntEMIResultStatus() {
        Assertions.assertThat(getEMIresultSatus())
                .as("EMI not available for merchant").isEqualToIgnoringCase("SUCCESS");
        return this;
    }

    public GetMerchantHelper verifyMrchntAcquiringStatus() {
        Assertions.assertThat(getMchntAcquringStatus())
                .as("Merchant Acquiring not available for merchant").isEqualToIgnoringCase("SUCCESS");
        return this;
    }

    public List<String> getIssuingEmiBankNames() {
        if (getEMIresultSatus().equalsIgnoreCase("SUCCESS")) {
            List<String> bankName = getJsonPath().getList("MERCHANT-EMI-INFO.emiConfigInfos.issuingBank.issuingBankName");
            HashSet<String> tempSet = new HashSet<>();        //Temporary operation to remove duplicates from list
            tempSet.addAll(bankName);
            bankName.clear();
            bankName.addAll(tempSet);
            return bankName;
        } else
            return Collections.emptyList();
    }

    public List<String> getIssuingEmiBankId() {
        if (getEMIresultSatus().equalsIgnoreCase("SUCCESS")) {
            List<String> bankId = getJsonPath().getList("MERCHANT-EMI-INFO.emiConfigInfos.issuingBank.issuingBankId");
            HashSet<String> tempSet = new HashSet<>();        //Temporary operation to remove duplicates from list
            tempSet.addAll(bankId);
            bankId.clear();
            bankId.addAll(tempSet);
            return bankId;
        } else
            return Collections.emptyList();
    }

    public <T> List<T> getGetEmiConfigInfos() {
        if (getEMIresultSatus().equalsIgnoreCase("SUCCESS")) {
            return getJsonPath().getList("MERCHANT-EMI-INFO.emiConfigInfos");
        } else
            return Collections.emptyList();
    }

    public String getIssuingBankyId_ByName(String bankName) {
        if (getEMIresultSatus().equalsIgnoreCase("SUCCESS"))
            return getJsonPath().param("bankName", bankName)
                    .getString("MERCHANT-EMI-INFO.emiConfigInfos.find {emiConfigInfo -> emiConfigInfo.issuingBank.issuingBankName == bankName}.issuingBank.issuingBankId");
        else
            return "";
    }

    public List<Float> getEmiInterestRateByBankName(String bankName) {
        if (getEMIresultSatus().equalsIgnoreCase("SUCCESS"))
            return getJsonPath().param("bankName", bankName).
                    getList("MERCHANT-EMI-INFO.emiConfigInfos.findAll {emiConfigInfo -> emiConfigInfo.issuingBank.issuingBankName == bankName}.emiInfo.emiInterestRate");
        else
            return Collections.emptyList();
    }

    public List<Integer> getEmiMonthsByBankName(String bankName) {
        if (getEMIresultSatus().equalsIgnoreCase("SUCCESS"))
            return getJsonPath().param("bankName", bankName).
                    getList("MERCHANT-EMI-INFO.emiConfigInfos.findAll {emiConfigInfo -> emiConfigInfo.issuingBank.issuingBankName == bankName}.emiInfo.emiMonths");
        else
            return Collections.emptyList();
    }

    public List<String> getAcquiringNameByPayMethodType(PayMethodType payMethodType) {
        if (getMchntAcquringStatus().equalsIgnoreCase("SUCCESS")) {
            return getJsonPath().param("payMethod", payMethodType.toString()).
                    getList("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos.findAll {acquiringConfigInfos -> acquiringConfigInfos.payMethod == payMethod}.serviceInstName");
        } else
            return Collections.emptyList();
    }

    public List<String> getAcquiringIdByPayMethodType(PayMethodType payMethodType) {
        if (getMchntAcquringStatus().equalsIgnoreCase("SUCCESS"))
            return getJsonPath().param("payMethod", payMethodType.toString()).
                    getList("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos.findAll {acquiringConfigInfos -> acquiringConfigInfos.payMethod == payMethod}.serviceInstId");
        else
            return Collections.emptyList();
    }

    public List<Map> getAcquiringsByEnabledStatus(boolean status) {
        if (getMchntAcquringStatus().equalsIgnoreCase("SUCCESS")) {
            return getJsonPath().param("enableStatus", status).
                    get("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos.findAll {acquiringConfigInfos -> acquiringConfigInfos.enableStatus == enableStatus}");
        } else
            return Collections.emptyList();
    }

    public List<String> getEnabledAcquiringName() {
        if (getMchntAcquringStatus().equalsIgnoreCase("SUCCESS")) {
            return getJsonPath().param("enableStatus", true).
                    getList("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos.findAll {acquiringConfigInfos -> acquiringConfigInfos.enableStatus == enableStatus}.serviceInstName");
        } else
            return Collections.emptyList();
    }

    public List<Map> getAcquiringByPayMethodType(PayMethodType payMethodType) {
        if (getMchntAcquringStatus().equalsIgnoreCase("SUCCESS")) {
            List<Map> list = getJsonPath().param("payMethod", payMethodType.toString()).
                    getList("MERCHANT-ACQUIRING-INFO.acquiringConfigInfos.findAll {acquiringConfigInfos -> acquiringConfigInfos.payMethod == payMethod}");
            return list;
        } else
            return Collections.emptyList();
    }

    public String getMinAmntBy_BankName_Intrate(String bankName, String intRate) {

        return getJsonPath().param("bankName", bankName).param("intRate", intRate).
                get("MERCHANT-EMI-INFO.emiConfigInfos.findAll {emiConfigInfo -> emiConfigInfo.issuingBank.issuingBankName == bankName && "
                        + "emiConfigInfo.emiInfo.emiInterestRate == intRate}.emiInfo.emiMinAmount.amount");
    }

    public List<ContractDetails> getAllContracts() {
        List<ContractDetails> list = new ArrayList<>();
        Map<String, Object> map = getJsonPath().get();
        for (String key : map.keySet()) {
            if (key.startsWith("CONTRACT-DETAIL-"))
                list.add(getContractdetail(map, key));
        }
        return list;
    }

    public MerchantPrefInfo getMerchantPrefInfo() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(getJsonPath().get("MERCHANT-PREFERENCE-INFO"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MerchantPrefInfo merchantPrefInfo = null;
        try {
            merchantPrefInfo = mapper.readValue(jsonObject.toJSONString(), MerchantPrefInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Mismatch in MerchantPrefInfo DTO", e.getCause());
        }
        return merchantPrefInfo;
    }

    public MerchantAcquiring getMerchantAcquiringInfo() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(getJsonPath().get("MERCHANT-ACQUIRING-INFO"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MerchantAcquiring merchantAcquiring = null;
        try {
            merchantAcquiring = mapper.readValue(jsonObject.toJSONString(), MerchantAcquiring.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Mismatch in MerchantAcquiring DTO", e.getCause());
        }
        return merchantAcquiring;
    }

    public MerchantEMI getMerchantEmiInfo() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(getJsonPath().get("MERCHANT-EMI-INFO"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MerchantEMI merchantEMI = null;
        try {
            merchantEMI = mapper.readValue(jsonObject.toJSONString(), MerchantEMI.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Mismatch in MerchantEMI DTO", e.getCause());
        }
        return merchantEMI;
    }

    public MerchExtendedInfo getMerchantExtendedInfo() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(getJsonPath().get("MERCHANT-EXTENDED-INFO"));

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MerchExtendedInfo merchExtendedInfo = null;
        try {
            merchExtendedInfo = mapper.readValue(jsonObject.toJSONString(), MerchExtendedInfo.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Mismatch in MerchExtendedInfo DTO", e.getCause());
        }
        return merchExtendedInfo;
    }

    public String getMerchantPrefValue(MerchantPrefType merchantPrefType, MerchantPrefInfo merchantPrefInfo) {
        for(MerchantPreferenceInfos merchantPreferenceInfo : merchantPrefInfo.getMerchantPreferenceInfos()) {
            if(merchantPreferenceInfo.getPrefType().equalsIgnoreCase(merchantPrefType.toString())) {
                return merchantPreferenceInfo.getPrefValue();
            }
        }
        return "";
    }

    private ContractDetails getContractdetail(Map<String, Object> map, String key) {
        Map contract = (Map) map.get(key);
        JSONObject jsonObject = new JSONObject();
        jsonObject.putAll(contract);

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        ContractDetails contractDetail = null;
        try {
            contractDetail = mapper.readValue(jsonObject.toJSONString(), ContractDetails.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail("Mismatch in ContractDetails DTO", e.getStackTrace());
        }
        return contractDetail;
    }

    public void pause(int seconds) {
        try {
            Thread.sleep(seconds * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
