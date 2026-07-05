package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;
import org.luaj.vm2.ast.Str;

import java.util.ArrayList;


public class MappingApisPG2 extends BaseApi {

    public void Query_merchant_preference_info(String mid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCH_PREF_INFO
                .replace("{mid}",mid);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_get_preferenceinfosext(String mid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_GET_PREFERENCEINFOTEXT
                .replace("{mid}",mid);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_get_preference_info(String mid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_GET_PREFERENCE_INFO
                .replace("{mid}",mid);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_attribute_additional(String mid, String idType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_ATTRIBUTE_ADDITIONAL
                .replace("{mid}",mid).replace("{idType}",idType);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Get_subscription_detail(String MerchantId, String idType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_ATTRIBUTE_GET_SUBSCRIPTION_DETAIL;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().addPathParams("idType", idType);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Query_merchant_acquiring_mid(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_ACQUIRING_MID;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_query_acquiring(String MerchantId, String Paymethod)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_QUERY_ACQUIRING;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().addPathParams("paymethod", Paymethod);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Query_merchant_acquiring(String MerchantId, String Paymethod)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_ACQUIRING;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().addPathParams("paymethod", Paymethod);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_query_contract_item(String MerchantId, String ContractStatus, String PageNum, String PageSize)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_QUERY_CONTRACT_ITEM;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("contractStatus", ContractStatus);
        getRequestSpecBuilder().addPathParams("pageNum", PageNum);
        getRequestSpecBuilder().addPathParams("pageSize", PageSize);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Query_merchant_contract_item(String MerchantId, String ContractStatus, String PageNum, String PageSize)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_CONTRACT_ITEM;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("contractStatus", ContractStatus);
        getRequestSpecBuilder().addPathParams("pageNum", PageNum);
        getRequestSpecBuilder().addPathParams("pageSize", PageSize);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_query_contract_details(String MerchantId, String ContractStatus, String ProductCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_QUERY_CONTRACT_DETAIL;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("contractStatus", ContractStatus);
        getRequestSpecBuilder().addPathParams("productCode", ProductCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_query_contract_details_PG(String MerchantId, String ContractStatus, String ProductCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PG2_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_QUERY_CONTRACT_DETAIL_PG;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addQueryParam("productCode", ProductCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Query_merchant_contract_details(String MerchantId, String ContractStatus, String ProductCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_CONTRACT_DETAIL;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("contractStatus", ContractStatus);
        getRequestSpecBuilder().addPathParams("productCode", ProductCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Query_merchant_migration_contract_detail(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Query_merchant_migration_contract_detail_V2(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL_V2;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Query_merchant_migration_contract_detail_PG2(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PG2_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL_PG;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Common_v1_get_contract_paymentInfo(String MerchantId, String Paymethod, String ProductCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.COMMON_V1_GET_CONTRACT_PAYMENTINFO;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("paymentMethod", Paymethod);
        getRequestSpecBuilder().addPathParams("productCode", ProductCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_profile(String MerchantId, String IdType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_PROFILE;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("idType", IdType);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_get_extended_info(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_GET_EXTENDED_INFO;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void User_getMerchantExtendedInfo(String Userid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.USER_GETMERCHANTEXTENDEDINFO;
        getRequestSpecBuilder().addPathParams("userid", Userid);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_v1(String MerchantId, String Type)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_V1;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("type", Type);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_idmap(String MerchantId, String IdType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.MERCHANT_IDMAP;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("idType", IdType);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Get_alipayid(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.GET_ALIPAYID;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Get_paytmId(String OldpgId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.GET_PAYTMID;
        getRequestSpecBuilder().addPathParams("oldpgId", OldpgId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Logo_cobranding_mid(String MerchantId, String Channel)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.LOGO_COBRANDING_MID;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().addPathParams("channel", Channel);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Get_merchantlogoinfo_v2(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.GET_MERCHANTLOGOINFO_V2;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Merchant_Attribute_Key(String merchantId, String clientId,String idType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.MERCHANT_ATTRIBUTE_KEY_CLIENTID);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
        getRequestSpecBuilder().addPathParam("idType",idType);
        getRequestSpecBuilder().addPathParam("clientId",clientId);
    }
    public void Merchant_Attribute_Key_Paymode(String merchantId, String paymode,String idType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.MERCHANT_ATTRIBUTE_KEY_PAYMODE);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
        getRequestSpecBuilder().addPathParam("idType",idType);
        getRequestSpecBuilder().addPathParam("payMode",paymode);
    }

    public void Merchant_Attribute_Key_Without_PaymodeAndClientId(String merchantId,String idType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.MERCHANT_ATTRIBUTE_KEY);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
        getRequestSpecBuilder().addPathParam("idType",idType);
    }

    public void Get_Merchant_Data(String merchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.GET_MERCHANTDATA_PAYTMID);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
    }

    public void Get_Merchant_Api_UrlInfo(String merchantId,String midType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.GET_MERCHANTAPIURLINFO);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
        getRequestSpecBuilder().addPathParam("midType",midType);
    }

    public void Get_MBID(String merchantId,String bankId, String payMethodId, String authModeId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.GET_MBID);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
        getRequestSpecBuilder().addPathParam("bankId",bankId);
        getRequestSpecBuilder().addPathParam("payMethodId",payMethodId);
        getRequestSpecBuilder().addPathParam("authModeId",authModeId);
    }

    public void Get_Thematic_Details(String merchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.MERCHANT_GET_THEMATIC_DETAILS);
        getRequestSpecBuilder().addPathParam("mid",merchantId);

    }

    public void Get_Merchant_Device_Details_With_V2_TID(String tid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.EOS_MERCHANT_DEVICE_DETAILS_V2_TID);
        getRequestSpecBuilder().addPathParam("tid",tid);

    }
    public void Get_Merchant_Device_Details_With_TID_And_BankName(String tid, String bankName)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.EOS_MERCHANT_DEVICE_DETAILS_TID_BANKNAME);
        getRequestSpecBuilder().addPathParam("tid",tid);
        getRequestSpecBuilder().addPathParam("bankName",bankName);

    }

    public void Get_Merchant_Device_Details_With_TID(String tid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.EOS_MERCHANT_DEVICE_DETAILS_TID);
        getRequestSpecBuilder().addPathParam("tid",tid);


    }

    public void Get_Merchant_Device_Details_With_BANKLIST_TID(String tid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.EOS_MERCHANT_DEVICE_DETAILS_BANKSLIST_TID);
        getRequestSpecBuilder().addPathParam("tid",tid);
    }

    public void Get_Logo_Cobranding_Details(String mid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.LOGO_COBRANDING_DETAILS);
        getRequestSpecBuilder().addPathParam("mid",mid);
    }

    public void Get_Entityurlinformid(String mid,String UrlType, String WebsiteName)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.GET_ENTITYURLINFOMID);
        getRequestSpecBuilder().addPathParam("mid",mid);
        getRequestSpecBuilder().addPathParam("urlType",UrlType);
        getRequestSpecBuilder().addPathParam("websiteName",WebsiteName);
    }

    public void Get_Entityurlinformid_V2(String mid,String UrlType, String WebsiteName)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.GET_ENTITYURLINFOMID_V2);
        getRequestSpecBuilder().addPathParam("mid",mid);
        getRequestSpecBuilder().addPathParam("urlType",UrlType);
        getRequestSpecBuilder().addPathParam("websiteName",WebsiteName);
    }

    public void Merchant_V1_MerchantIdList(String type)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.MERCHANT_V3_MERCHANTLIST);
        getRequestSpecBuilder().setBody(getMerchantV1MerchantIdList());
        getRequestSpecBuilder().addPathParam("type",type);


    }

    public void Merchant_V2_Query_Contract()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.MERCHANT_V2_QUERY_CONTRACT_ITEM);
        getRequestSpecBuilder().setBody(getMerchantV2QueryContractItemrequest());



    }
    public void Payment_Info_Fee(String mid,String productCode)
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServicePG2APIS.PAYMENT_INFO_FEE);
        getRequestSpecBuilder().setBody(getPaymentInfoFeeRequest());
        getRequestSpecBuilder().addPathParam("mid",mid);
        getRequestSpecBuilder().addPathParam("productCode",productCode);

    }
    public void buildPaymentInfoFeeRequest(String payMethod){
        setContext("payMethod",payMethod);
    }
    public void buildMerchantV2QueryContractItemRequest(String mid,String contractStatus,String productCode,String pageNum, String pageSize){
        setContext("merchantId",mid);
        setContext("contractStatus",contractStatus);
        setContext("productCode",productCode);
        setContext("pageNum",pageNum);
        setContext("pageSize",pageSize);
    }
    public void setMerchantIdList(ArrayList<String>merchantIdList){
        setContext("merchantIdList",merchantIdList);
    }

    String merchantV2QueryContractItemrequest="{\n" +
            "    \"merchantId\":\"qa12mi80573803805439\",\n" +
            "    \"contractStatus\":\"EFFECTIVE\",\n" +
            "    \"productCode\": \"51051000100000000001\",\n" +
            "    \"pageNum\":1,\n" +
            "    \"pageSize\": 1\n" +
            "}";
    String merchantV1MerchantIdList="{\n" +
            "    \"merchantIdList\": [\n" +
            "        \"qa11PG72611112693255\",\n" +
            "        \"qa12mi80573803805439\"\n" +
            "    ],\n" +
            "    \"identifier\": null\n" +
            "}";
    String paymentInfoFeerequest="{\n" +
            "    \"payMethod\": \"CREDIT_CARD\"\n" +
            "}";
    public String  getPaymentInfoFeeRequest(){
        return paymentInfoFeerequest;
    }
    public String  getMerchantV1MerchantIdList(){
        return merchantV1MerchantIdList;
    }
    public String  getMerchantV2QueryContractItemrequest(){
        return merchantV2QueryContractItemrequest;
    }


    public void Bankmasterdetails_Paymode_Api(String Paymode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.BANKMASTERDETAILS_PAYMODE;
        getRequestSpecBuilder().addPathParams("paymode", Paymode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Bankdetails_Alipaycode_Api(String AlipayBankCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.BANKDETAILS_ALIPAYCODE;
        getRequestSpecBuilder().addPathParams("oldpgBankCode", AlipayBankCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_BanksDetailslistfrom_Codes(String BankCodes)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GETBANKDETAILS_LIST_CODES;
        getRequestSpecBuilder().addPathParams("bankCodes", BankCodes);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Bankdetails_BankCode(String BankCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GETBANKDETAILS_BANKCODE;
        getRequestSpecBuilder().addPathParams("bankCode", BankCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Responsecodedetails_PaytmResponseCode(String PaytmResponseCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GET_RESPONSECODEDETAILS;
        getRequestSpecBuilder().addPathParams("paytmResponseCode", PaytmResponseCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Responsecodedetails_Resultcode(String ResultCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GET_RESPONSECODEDETAILS_RESULTCODE;
        getRequestSpecBuilder().addPathParams("resultCode", ResultCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    String GetNotificationTemplate = "{\n" +
            "    \"notificationType\": \"SMS\",\n" +
            "    \"serviceType\": \"serviceType\",\n" +
            "    \"recipient\": \"MERCHANT\",\n" +
            "    \"category\": \"OFFLINE_MERCHANT_SMS_PRINTER\",\n" +
            "    \"txnStatus\": \"SUCCESS\"\n" +
            "\n" +
            "}";
    public void SetGetNotificationTemplate(String NotificationType,String ServiceType,String Recipient,String Category,String TxnStatus){
        setContext("notificationType", NotificationType);
        setContext("serviceType", ServiceType);
        setContext("recipient", Recipient);
        setContext("category", Category);
        setContext("txnStatus", TxnStatus);
    }
    public String  GETGetNotificationTemplate(){
        return GetNotificationTemplate;
    }
    public void Get_Notification_Template()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServiceDrop1APIS.NOTIFICATION_GET_NOTIFICATION_TEMPLATE);
        getRequestSpecBuilder().setBody(GETGetNotificationTemplate());
    }

    public void User_V1_Type_PaytmId(String TYPE, String PAYTMID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.USER_V1_TYPE_PAYTMID;
        getRequestSpecBuilder().addPathParams("type", TYPE);
        getRequestSpecBuilder().addPathParams("paytmId", PAYTMID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void User_V1_Type_PaytmId_Willcreate(String TYPE, String PAYTMID, Boolean WILLCREATE)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.USER_V1_TYPE_PAYTMID_WILLCREATE;
        getRequestSpecBuilder().addPathParams("type", TYPE);
        getRequestSpecBuilder().addPathParams("paytmId", PAYTMID);
        getRequestSpecBuilder().addPathParams("willCreate", WILLCREATE);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Merchantlogoinfo_V2_with_fetchLogoFromBossPanel(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GET_MERCHANTLOGOINFO_V2_FETCHLOGOFROMBOSSPANEL;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().addQueryParam("fetchLogoFromBossPanel", "true");
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Merchantlogoinfo_V2(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GET_MERCHANTLOGOINFO_V2;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    String CommonV1Get= "{\n" +
            "  \"request\": {\n" +
            "    \"body\": {\n" +
            "      \"id\": \"216810000147144068919\",\n" +
            "      \"type\": \"paytm\"\n" +
            "    },\n" +
            "    \"head\": {\n" +
            "      \"clientId\": \"1230000001\",\n" +
            "      \"reqTime\": \"Apr 7, 2024, 12:16:34 PM\",\n" +
            "      \"accessToken\": \"\",\n" +
            "      \"reserve\": \"\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"signature\": \"ASDASKSDLDJfk\"\n" +
            "}";
    public void SetCommonV1Get(String Id,String type){
        setContext("request.body.id", Id);
        setContext("request.body.type", type);
    }
    public String  GetCommonV1Get(){
        return CommonV1Get;
    }
    public void Common_V1_Get()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServiceDrop1APIS.COMMON_V1_GET);
        getRequestSpecBuilder().setBody(GetCommonV1Get());
    }

    public void MerchantAgent_Get_AgentInfo_Id_Type(String Id, String Type)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.MERCHANTAGENT_GET_AGENTINFO_ID_TYPE;
        getRequestSpecBuilder().addPathParams("id", Id);
        getRequestSpecBuilder().addPathParams("type", Type);
        getRequestSpecBuilder().addQueryParam("parentMid", Constants.MerchantType.MID_FETCHLOGOFROMBOSSPANEL.getId().toString());
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Global_Config_ACQUIRER_CURRENCY_IICPC1IN()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GET_GLOBAL_CONFIG_ACQUIRER_CURRENCY_IICPC1IN;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Notification_Fetch_Template_Configuration()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.NOTIFICATION_FETCH_TEMPLATE_CONFIGURATION;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Limit_Merchant_Type_PPI_LIMIT_1()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GET_LIMIT_MERCHANTTYPE_PPI_LIMIT_1;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_PspSchema()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.GET_PSP_SCHEMA;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Dcc_Supported_CurrencyList_Acquirer_IICTC1IN()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop1APIS.DCC_SUPPORTED_CURRENCYLIST_ACQUIRER_IICTC1IN;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Bankresponsecodes_bankCodepay_ModeService(String BankCode, String PayMode, String Service)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop2APIS.GET_BANKRESPONSECODES_BANKCODES_PAYMODES_SERVICES;
        getRequestSpecBuilder().addPathParams("bankCode", BankCode);
        getRequestSpecBuilder().addPathParams("payMode", PayMode);
        getRequestSpecBuilder().addPathParams("service", Service);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Merchant_Static_Config_mid(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop2APIS.GET_MERCHANT_STATIC_CONFIG_MID;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Emi_On_Dc_EligibilityBy(String Contact, String BankName)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop2APIS.GET_EMI_ON_DC_ELIGIBILITY_BY;
        getRequestSpecBuilder().addQueryParam("contact",Contact);
        getRequestSpecBuilder().addQueryParam("bankName",BankName);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Device_Details_Tid(String TID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop2APIS.DEVICE_DETAILS_TID;
        getRequestSpecBuilder().addPathParams("tid", TID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Eos_Merchant_Device_Details_Bankslist_Tid(String TID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop2APIS.DEVICE_DETAILS_BANKLIST_TID;
        getRequestSpecBuilder().addPathParams("tid", TID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Device_Details_V2_Tid(String TID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop2APIS.DEVICE_DETAILS_V2_TID;
        getRequestSpecBuilder().addPathParams("tid", TID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Device_Details_Tid_Bankname(String TID, String BankName)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop2APIS.DEVICE_DETAILS_TID_BANKNAME;
        getRequestSpecBuilder().addPathParams("tid", TID);
        getRequestSpecBuilder().addPathParams("bankName", BankName);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Formatter_Details(String Bankcode, String paymethod)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.GET_FORMATTER_DETAILS;
        getRequestSpecBuilder().addPathParams("Bankcode",Bankcode);
        getRequestSpecBuilder().addPathParams("paymethod",paymethod);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void User_get_usermid(String Userid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.USER_GET_USERMID;
        getRequestSpecBuilder().addPathParams("userid", Userid);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Entityurlinfoformid(String Mid, String Urltype, String Websitename)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.ENTITYURL_INFO_FOR_MID;
        getRequestSpecBuilder().addPathParams("mid", Mid);
        getRequestSpecBuilder().addPathParams("urlType", Urltype);
        getRequestSpecBuilder().addPathParams("websiteName", Websitename);
        getRequestSpecBuilder().setBasePath(basePath);
    }


    public void Get_cardnetworkdetails()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.GET_CARD_NETWORK_DETAILS;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_paymethoddetails()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.GET_PAY_METHOD_DETAILS;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Fetch_logo(String Logotype, String Identifier)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.GET_FETCH_LOGO;
        getRequestSpecBuilder().addPathParams("logotype",Logotype);
        getRequestSpecBuilder().addPathParams("identifier",Identifier);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Getmerchantidmap(String MID, String IDTYPE)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.GET_MERCHANT_IDMAP;
        getRequestSpecBuilder().addPathParams("mid",MID);
        getRequestSpecBuilder().addPathParams("idtype",IDTYPE);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Fetchentityignoreparamas(String Entityid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.FETCH_ENTITY_IGNORE_PARAMS;
        getRequestSpecBuilder().addPathParams("entityid", Entityid);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_MerchantResponsecodedetails_Resultcode(String MID,String Resultcode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceDrop3APIS.GET_MERCHANTRESPONSECODEDETAILS;
        getRequestSpecBuilder().addPathParams("mid",MID);
        getRequestSpecBuilder().addPathParams("Resultcode", Resultcode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Merchant_Attribute_Key_withmidandidtype(String merchantId,String idType)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServiceDrop3APIS.MERCHANT_ATTRIBUTE_KEY_WITH_MID_AND_IDTYPE);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
        getRequestSpecBuilder().addPathParam("idType",idType);
    }

    public void Merchant_Attribute_Key_withmididtypeandpaymode(String merchantId,String idType,String paymode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServiceDrop3APIS.MERCHANT_ATTRIBUTE_KEY_WITH_MID_IDTYPE_AND_PAYMODE);
        getRequestSpecBuilder().addPathParam("mid",merchantId);
        getRequestSpecBuilder().addPathParam("idType",idType);
        getRequestSpecBuilder().addPathParam("paymode",paymode);
    }


// Audit L2 apis

    public void Merchant_v3(String MerchantId, String Type)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditL2APIS.MERCHANT_V3;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addPathParams("type", Type);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Merchant_get_extended_info_V3(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditL2APIS.MERCHANT_GET_EXTENDED_INFO_V3;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_paytmId_V1(String OldpgId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditL2APIS.GET_PAYTMID_V1_OLDPGID;
        getRequestSpecBuilder().addPathParams("oldpgId", OldpgId);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Get_oldpgId(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditL2APIS.GET_OLDPGID;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Merchant_LogoInfo_V1_OldpgMid(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditL2APIS.MERCHANTLOGOINFO_V1_OLDPGID;
        getRequestSpecBuilder().addPathParams("oldpgMid", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Merchant_get_extended_info_V3_mid_clientId(String MerchantId, String ClientId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditL2APIS.MERCHANT_GET_EXTENDED_INFO_V3_MID_CLIENTID;
        getRequestSpecBuilder().addPathParams("mid", MerchantId);
        getRequestSpecBuilder().addPathParams("clientId", ClientId);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Bankdetails_BankCode_V1(String BankCode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditChangesNew.GETBANKDETAILSV1_BANKCODE;
        getRequestSpecBuilder().addPathParams("bankCode", BankCode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Getmerchantidmapv3(String MID, String IDTYPE)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditChangesNew.GET_MERCHANT_IDMAP_V3;
        getRequestSpecBuilder().addPathParams("mid",MID);
        getRequestSpecBuilder().addPathParams("idtype",IDTYPE);
        getRequestSpecBuilder().setBasePath(basePath);
    }

   /* public void Get_Bankdetails_Userid_OLD(String Userid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditChangesNew.GETBANKDETAILS_USERID_OLD;
        getRequestSpecBuilder().addPathParams("userid", Userid);
        getRequestSpecBuilder().setBasePath(basePath);
    }
*/
    public void Get_Bankdetails_Userid_New_V1(String Userid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAuditChangesNew.GETBANKDETAILS_USERID_NEW_V1;
        getRequestSpecBuilder().addPathParams("userid", Userid);
        getRequestSpecBuilder().setBasePath(basePath);
    }

// Audit L3 apis

    public void Get_VendorSplitDetails_V3(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_VENDOR_SPLITDETAILS_V3;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void Query_Merchant_ExtendedInfo_V3(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        String basePath = Constants.MappingServiceAudit3APIS.QUERY_MERCHANT_EXTENDEDINFO_V3;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().setBasePath(basePath);
    }
    public void User_Get_Merchant_ExtendedInfo_V3(String USERID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.USER_GET_MERCHANT_EXTENDEDINFO_V3;
        getRequestSpecBuilder().addPathParams("userid", USERID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    String merchantV3MerchantIdList="{\n" +
            "    \"merchantIdList\": [\n" +
            "        \"qa11PG72611112693255\",\n" +
            "        \"qa12mi80573803805439\"\n" +
            "    ],\n" +
            "    \"identifier\": null\n" +
            "}";

    public String  getMerchantV3MerchantIdList(){
        return merchantV3MerchantIdList;
    }
    public void Merchant_V3_MerchantIdList(String type)
    {
        setMethod(BaseApi.MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServiceAudit3APIS.MERCHANT_V3_MERCHANTLIST);
        getRequestSpecBuilder().setBody(getMerchantV3MerchantIdList());
        getRequestSpecBuilder().addPathParam("type",type);


    }

    public void Merchant_Get_ExtendedInfo_V4(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.MERCHANT_GET_EXTENDEDINFO_V4;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void User_V3(String Type, String User)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.USER_V3;
        getRequestSpecBuilder().addPathParams("type", Type);
        getRequestSpecBuilder().addPathParams("user", User);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Mapping_V3(String Type, String Mid)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.MAPPING_V3;
        getRequestSpecBuilder().addPathParams("type", Type);
        getRequestSpecBuilder().addPathParams("mid", Mid);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void User_V3(String Type, String User, Boolean Willcreate)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.USER_V3_WILLCREATE;
        getRequestSpecBuilder().addPathParams("type", Type);
        getRequestSpecBuilder().addPathParams("user", User);
        getRequestSpecBuilder().addPathParams("willCreate", Willcreate);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Banksdetailslistfromids_V1(String BankIds)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_BANKDETAILSFROMMIDS_V1;
        getRequestSpecBuilder().addPathParams("bankIds", BankIds);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_V1_Bankmasterdetails()
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_V1_BANKMASTERDEATAILS;
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Bankmasterdetails_V1Paymode(String Paymode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_BANKMASTERDEATAILS_V1_PAYMODE;
        getRequestSpecBuilder().addPathParams("paymode", Paymode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_BanksdetailslistfromCodes_V1(String Bankcodes)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_BANKDEATAILSFROMCODES_V1;
        getRequestSpecBuilder().addPathParams("bankcodes", Bankcodes);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void GetBankdetailsOldpgcode(String Bankcode)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_BANKDEATAILSOLDPGCODES_V1;
        getRequestSpecBuilder().addPathParams("bankcodes", Bankcode);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    String CommonV1GetMerchant= "{\n" +
            "  \"request\": {\n" +
            "    \"body\": {\n" +
            "      \"id\": \"qa8mid49895778745987\",\n" +
            "      \"type\": \"oldpg\"\n" +
            "    },\n" +
            "    \"head\": {\n" +
            "      \"clientId\": \"1230000001\",\n" +
            "      \"reqTime\": \"Apr 7, 2023, 12:16:34 PM\",\n" +
            "      \"accessToken\": \"\",\n" +
            "      \"reserve\": \"\"\n" +
            "    }\n" +
            "  },\n" +
            "  \"signature\": \"ASDASKSDLDJfk\"\n" +
            "}";
    public void SetCommonV1GetMerchant(String Id,String type){
        setContext("request.body.id", Id);
        setContext("request.body.type", type);
    }
    public String  GetCommonV1GetMerchant(){
        return CommonV1GetMerchant;
    }
    public void Common_V1_Get_Merchant()
    {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingServiceAudit3APIS.COMMON_V1_GET_MERCHANT);
        getRequestSpecBuilder().setBody(GetCommonV1Get());
    }

    public void Get_Merchantdata_Paytmid_V1(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_MERCHANTDATA_PAYTMID_V1;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Lookupfrom_id_V1(String id)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_LOOKUPFROMID_V1;
        getRequestSpecBuilder().addPathParams("Id", id);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_Merchantdata_Name_V1(String name)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_MERCHANTDATA_NAME_V1;
        getRequestSpecBuilder().addPathParams("name", name);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Get_LookupPREFERENCES_V1(String Category, String ChannelName)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.GET_LOOKUPPREFERENCESV1;
        getRequestSpecBuilder().addPathParams("category", Category);
        getRequestSpecBuilder().addPathParams("channelName", ChannelName);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Query_Merchant_MigrationDetails_V1(String MID)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServiceAudit3APIS.QUERY_MERCHANT_MIGRATIONDETAIL_V1;
        getRequestSpecBuilder().addPathParams("mid", MID);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void New_Query_MerchantMigration_ContractDetails(String MerchantId, String Status)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL_V2;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().addQueryParam("status", Status);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Query_MerchantMigration_ContractDetails_V2(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        String basePath = Constants.MappingServicePG2APIS.QUERY_MERCHANT_MIGRATION_CONTRACT_DETAIL_V2;
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        getRequestSpecBuilder().setBasePath(basePath);
    }

    public void Merchant_get_entity_edc_channel_info(String MerchantId)
    {
        setMethod(BaseApi.MethodType.GET);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().addPathParams("merchantId", MerchantId);
        String basePath = Constants.MappingServicePG2APIS.ENTITY_EDC_CHANNEL_INFO;
        getRequestSpecBuilder().setBasePath(basePath);
    }

}
