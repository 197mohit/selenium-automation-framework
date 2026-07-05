package com.paytm.api.coft.saveCard;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.appconstants.Constants.MerchantType;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import com.paytm.utils.merchant.util.PGPUtil;
import io.restassured.http.ContentType;

import java.util.ArrayList;
import java.util.TreeMap;

public class SavedCardByMidCustId extends BaseApi {

  String request = "{\"head\":{\"tokenType\":\"CHECKSUM\",\"token\":\"6v1zeBmoaULb2nzz1KaUNmCNPMjwHwAmB7iv0nAQnnj++s1TfIUU462CNQzsjnHQdCsYoT66wrgGOPXAaDOG5h0sdDrExWXSbE/Crh0Hsbk=\"},\"body\":{\"mid\":\"qa8abh33054038637431\",\"custId\":\"Test123\"}}";

  public SavedCardByMidCustId() {
    setMethod(MethodType.POST);
    getRequestSpecBuilder().setContentType(ContentType.JSON);
    getRequestSpecBuilder().setAccept(ContentType.JSON);
    getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
    getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
    getRequestSpecBuilder().setBasePath(Constants.savedCard.SAVEDCARD_SERVICE_MERCHANT_COFT_CARDS);
    getRequestSpecBuilder().addHeader("Content-Type", "application/json");
    getRequestSpecBuilder().setBody(getRequest());
  }

  public String getRequest() {
    return request;
  }

  public SavedCardByMidCustId buildRequest(String mid, String custId, boolean filterTokenCards,
      String tokenType, String JWT) {
    setContext("head.tokenType", tokenType);
    setContext("head.token", JWT);
    setContext("body.mid", mid);
    setContext("body.custId", custId);
    setContext("body.filterTokenCards", filterTokenCards);
    return this;
  }

  public SavedCardByMidCustId buildRequest(String mid, String custId, String tokenType,
      String JWT) {
    setContext("head.tokenType", tokenType);
    setContext("head.token", JWT);
    setContext("body.mid", mid);
    setContext("body.custId", custId);
    return this;
  }

  public SavedCardByMidCustId buildRequest(MerchantType mid, String custId, String tokenType) {
    TreeMap<String, String> treeMap = new TreeMap();
    treeMap.put("mid", mid.getId());
    treeMap.put("custId", custId);
    System.out.println(treeMap.toString());
    String token = PGPUtil.getChecksum(mid.getKey(), treeMap);
    System.out.println("Checksum is :" + token);
    setContext("head.tokenType", tokenType);
    setContext("head.token", token);
    setContext("body.mid", mid.getId());
    setContext("body.custId", custId);
    return this;
  }

  public SavedCardByMidCustId buildRequest(MerchantType mid, String custId, String tokenType,
      ArrayList<String> providerTypes) {
    TreeMap<String, String> treeMap = new TreeMap();
    treeMap.put("mid", mid.getId());
    treeMap.put("custId", custId);
    String token = PGPUtil.getChecksum(mid.getKey(), treeMap);
    System.out.println("Checksum is :" + token);
    setContext("head.tokenType", tokenType);
    setContext("head.token", token);
    setContext("body.mid", mid.getId());
    setContext("body.custId", custId);
    setContext("body.tokenType", providerTypes);
    return this;
  }

  public SavedCardByMidCustId buildRequest(String mid, String custId, String tokenType,
      String JWT,ArrayList <String> providerTypes) {
    setContext("head.tokenType", tokenType);
    setContext("head.token", JWT);
    setContext("body.mid", mid);
    setContext("body.custId", custId);
    setContext("body.tokenType", providerTypes);
    return this;
  }

}
