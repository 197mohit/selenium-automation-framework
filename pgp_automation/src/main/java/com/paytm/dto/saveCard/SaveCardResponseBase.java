package com.paytm.dto.saveCard;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.asserts.SoftAssert;

import java.util.List;

/**
 * Created by anjukumari on 28/08/18
 */
public class SaveCardResponseBase {
    String responseStatus;
    String httpCode;
    String httpSubCode;
    String codeDetail;
   public Object response;
    private SoftAssert softAssert = new SoftAssert();


    public String getResponseStatus() {
        return responseStatus;
    }

    public SaveCardResponseBase setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
        return this;
    }

    public String getHttpCode() {
        return httpCode;
    }

    public SaveCardResponseBase setHttpCode(String httpCode) {
        this.httpCode = httpCode;
        return this;
    }

    public String getHttpSubCode() {
        return httpSubCode;
    }

    public SaveCardResponseBase setHttpSubCode(String httpSubCode) {
        this.httpSubCode = httpSubCode;
        return this;
    }

    public String getCodeDetail() {
        return codeDetail;
    }

    public SaveCardResponseBase setCodeDetail(String codeDetail) {
        this.codeDetail = codeDetail;
        return this;
    }

    public Object getResponse() {
        return this.response;
    }

    public SaveCardResponse getResponseInSaveCardResponse() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        SaveCardResponse data=mapper.readValue(mapper.writeValueAsString(this.getResponse()), SaveCardResponse.class);
        return data;
    }


    public List<SaveCardResponse> getResponseInSaveCardResponseList() throws Exception{
        ObjectMapper mapper = new ObjectMapper();
        List<SaveCardResponse> data=mapper.readValue(mapper.writeValueAsString(this.getResponse()),  new TypeReference<List<SaveCardResponse>>(){});
        return data;
    }


    public SaveCardResponseBase setResponse(Object response) throws Exception{
        this.response = response;
        return this;
    }

    @Override
    public String toString() {
        return "SaveCardResponseBase{" +
                "responseStatus='" + responseStatus + '\'' +
                ", httpCode='" + httpCode + '\'' +
                ", httpSubCode='" + httpSubCode + '\'' +
                ", codeDetail='" + codeDetail + '\'' +
                ", response=" + response +
                '}';
    }

    public SaveCardResponseBase validateResponseStatus(String val){
        this.softAssert.assertEquals(val, this.responseStatus);
        return this;
    }

    public SaveCardResponseBase validateHttpCode(String val){
        this.softAssert.assertEquals(val, this.httpCode);
        return this;
    }

    public SaveCardResponseBase validateHttpSubCode(String val){
        this.softAssert.assertEquals(val, this.httpSubCode);
        return this;
    }
    public SaveCardResponseBase validateCodeDetail(String val){
        this.softAssert.assertEquals(val, this.codeDetail);
        return this;
    }

    public SaveCardResponseBase validateResponse(String val){
        this.softAssert.assertEquals(val, this.response);
        return this;
    }

    public SaveCardResponseBase assertAll(){
        this.softAssert.assertAll();
        return this;
    }
}
