package com.paytm.dto.PromoDTO.ItemLevelPromoDTO;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.dto.NativeDTO.InitTxn.PaymentOffersAppliedv2;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import static com.paytm.LocalConfig.MOCK_HOST;
import static com.paytm.appconstants.Constants.NativeAPIResourcePath.INIT_TXN;

public class ItemLevelInitTXN extends BaseApi
{
    String request = "{\n" +
            "  \"head\":{\n" +
            "     \"clientId\":\"C11\",\n" +
            "     \"version\":\"v1\",  \n" +
            "     \"requestTimestamp\":\"Time\", \n" +
            "     \"channelId\":\"WAP\",  \n" +
            "     \"signature\":\"\" \n" +
            "  },\n" +
            "  \"body\":{\n" +
            "     \"requestType\":\"Payment\", \n" +
            "     \"mid\":\"{MID}\", \n" +
            "     \"orderId\":\"{OrderID}\",\n" +
            "\n" +
            "     \n" +
            "     \n" +
            " \"paymentOffersApplied\" : {\n" +
            "        \"verificationCode\":\"{vCode}\",\n" +
            "        \"promoCode\":\"{promoCode}\",\n" +
            "        \"promoContext\":{\n" +
            "     \"cart\":\"{\\\"items\\\":{\\\"224826563pid\\\":{\\\"price\\\":5000,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"123\\\",\\\"124\\\"]}},\\\"224826564pid\\\":{\\\"price\\\":5000,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"1\\\",\\\"2\\\"]}},\\\"224826566pid\\\":{\\\"price\\\":500,\\\"product\\\":{\\\"id\\\":\\\"224826563\\\",\\\"brand_id\\\":\\\"506801\\\",\\\"merchant_id\\\":\\\"1064019\\\",\\\"vertical_id\\\":\\\"131\\\",\\\"category_ids\\\":[\\\"3\\\",\\\"4\\\"]}}}}\" \n" +
            "   },\"savings\": [\n" +
            "                {  \"savings\": \"{promoAmount}\",\n" +
            "                    \"redemptionType\": \"{promoType}\"\n" +
            "               }\n" +
            "            ]\n" +
            "    },\n" +
            "\n" +
            "\n" +
            "     \"websiteName\":\"retail\", \n" +
            "     \"txnAmount\":{  \n" +
            "        \"value\":\"100\",\n" +
            "        \"currency\":\"INR\"\n" +
            "     },\n" +
            "     \n" +
            "     \"userInfo\":{\n" +
            "        \"custId\":\"126767691010\", \n" +
            "        \"mobile\":\"9599711105\",\n" +
            "        \"email\":\"arzoo.batra@paytm.com\",\n" +
            "        \"firstName\":\"arzoo\",\n" +
            "        \"lastName\":\"batra\"\n" +
            "     },\n" +
            "     \n" +
            "     \"paytmSsoToken\":\"{ssoToken}\",\n" +
            "     \"disablePaymentMode\":null,\n" +
            "     \"enablePaymentMode\": null,\n" +
            "     \"callbackUrl\":\"https://automation-pg-ext.paytm.in/mockbank/MerchantSite/bankResponse\",\n" +
            "     \"goods\":[\n" +
            "        {\n" +
            "           \"merchantGoodsId\":\"24525635625623\",\n" +
            "           \"merchantShippingId\":\"564314314574327545\",\n" +
            "           \"snapshotUrl\":\"[http://snap.url.com ]\",\n" +
            "           \"description\":\"Women Summer Dress New White Lace Sleeveless\",\n" +
            "           \"category\":\"travelling/subway\",\n" +
            "           \"quantity\":\"3.2\",\n" +
            "           \"unit\":\"Kg\",\n" +
            "           \"price\":{\n" +
            "              \"currency\":\"INR\",\n" +
            "              \"value\":\"1\"\n" +
            "           },\n" +
            "           \"extendInfo\":{\n" +
            "              \"udf1\":\"ajay\",\n" +
            "              \"udf2\":\"ajay\",\n" +
            "              \"udf3\":\"ajay\",\n" +
            "              \"udf4\":\"ajay\",\n" +
            "              \"udf5\":\"ajay\"\n" +
            "           }\n" +
            "        }\n" +
            "     ],\n" +
            "     \"shippingInfo\":null,\n" +
            "     \"extendInfo\":{\n" +
            "        \"udf1\":\"vivek1\",\n" +
            "        \"udf2\":\"vivek2\",\n" +
            "        \"udf3\":\"vivek3\",\n" +
            "        \"mercUnqRef\":\"vivek4\",\n" +
            "        \"comments\":\"vivek5\"\n" +
            "     }\n" +
            "     \n" +
            "       \n" +
            "  }\n" +
            "}";
    public ItemLevelInitTXN(String MID, String OrderID,String ssoToken, PaymentOffersAppliedv2 paymentOffersAppliedResponse,Boolean flag)
    {
        request= request.replace("{MID}",MID)
                .replace("{OrderID}",OrderID)
                .replace("{vCode}",paymentOffersAppliedResponse.getverificationCode())
                .replace("{promoCode}", (CharSequence) paymentOffersAppliedResponse.getpromoCode())
                .replace("{promoAmount}",paymentOffersAppliedResponse.getOfferBreakupList().get(0).getsavings())
                .replace("{promoType}",paymentOffersAppliedResponse.getOfferBreakupList().get(0).getredemptionType())
                .replace("{ssoToken}",ssoToken);
        if(flag==true){
            request=  request.replace("paymentOffersApplied","paymentOffersAppliedV2");
        }

        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(INIT_TXN);
        getRequestSpecBuilder().addQueryParam("mid", MID);
        getRequestSpecBuilder().addQueryParam("orderId", OrderID);
        getRequestSpecBuilder().setBody(request);
         getRequestSpecBuilder().addHeader("Authorization","Basic YXV0b21hdGlvbl9jbGllbnQ6N3NnczEwdnp3eFpHcW5mWkRSOThIN1NSRzlJOURBbmw=");
    }
    private String generateChecksum(Constants.MerchantType merchantType, Object body) throws Exception {
        return PGPHelpers.getNativeChecksum(merchantType.getKey(), body);
    }
}
