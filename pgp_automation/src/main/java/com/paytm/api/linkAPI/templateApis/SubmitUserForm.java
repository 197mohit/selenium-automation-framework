package com.paytm.api.linkAPI.templateApis;

import com.paytm.framework.api.BaseApi;
import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import io.restassured.http.ContentType;

public class SubmitUserForm extends BaseApi {
    String request="{\n" +
            "    \"head\": {\n" +
            "        \"version\": \"v2\",\n" +
            "        \"timestamp\": \"1573635710587\",\n" +
            "        \"channelId\": \"WEB\",\n" +
            "        \"signature\": \"xyBhvTOzwLwlT5rIjFLgvMk+2VIYX0fcRCN/juLLqBudHVwLR87Judg0nigyr54EDFimhXnJafAHTTumkEPMUXqLwkj2KSFd7mZIjChlv4U=\",\n" +
            "        \"tokenType\": \"AES\",\n" +
            "        \"clientId\": \"UMP\"\n" +
            "    },\n" +
            "     \"body\": {\n" +
            "    \"linkId\" : \"25491\",\n" +
            "     \n" +
            "                \"fields\": [\n" +
            "                    {\n" +
            "                        \"name\": \"Age\",\n" +
            "                        \"type\": \"Numeric\",\n" +
            "                        \"displayText\": \"\",\n" +
            "                        \"helpText\": \"\",\n" +
            "                        \"orientation\": \"H\",\n" +
            "                        \"isEditable\": false,\n" +
            "                        \"mandatory\": false,\n" +
            "                        \"size\": 100,\n" +
            "                        \"constraints\": {\n" +
            "                            \"maxLength\": \"200\",\n" +
            "                            \"minLength\": \"1\",\n" +
            "                            \"maxValue\": \"150\",\n" +
            "                            \"minValue\": \"0\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name\": \"Date of Birth\",\n" +
            "                        \"type\": \"Date\",\n" +
            "                        \"displayText\": \"\",\n" +
            "                        \"helpText\": \"\",\n" +
            "                        \"orientation\": \"H\",\n" +
            "                        \"isEditable\": false,\n" +
            "                        \"mandatory\": false,\n" +
            "                        \"size\": 100,\n" +
            "                        \"constraints\": {\n" +
            "                            \"minValue\": \"01/01/1900\",\n" +
            "                            \"beforeCurrentDate\": \"true\"\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name\": \"Delivery Address\",\n" +
            "                        \"mandatory\": false,\n" +
            "                        \"fields\": [\n" +
            "                            {\n" +
            "                                \"name\": \"Address Line 1\",\n" +
            "                                \"type\": \"Text\",\n" +
            "                                \"displayText\": \"\",\n" +
            "                                \"helpText\": \"\",\n" +
            "                                \"orientation\": \"H\",\n" +
            "                                \"isEditable\": false,\n" +
            "                                \"mandatory\": false,\n" +
            "                                \"size\": 100,\n" +
            "                                \"constraints\": {\n" +
            "                                    \"maxLength\": \"200\",\n" +
            "                                    \"minLength\": \"1\"\n" +
            "                                }\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"Address Line 2\",\n" +
            "                                \"type\": \"Text\",\n" +
            "                                \"displayText\": \"\",\n" +
            "                                \"helpText\": \"\",\n" +
            "                                \"orientation\": \"H\",\n" +
            "                                \"isEditable\": false,\n" +
            "                                \"mandatory\": false,\n" +
            "                                \"size\": 100,\n" +
            "                                \"constraints\": {\n" +
            "                                    \"maxLength\": \"200\",\n" +
            "                                    \"minLength\": \"1\"\n" +
            "                                }\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"City\",\n" +
            "                                \"type\": \"Text\",\n" +
            "                                \"displayText\": \"\",\n" +
            "                                \"helpText\": \"\",\n" +
            "                                \"orientation\": \"H\",\n" +
            "                                \"isEditable\": false,\n" +
            "                                \"mandatory\": false,\n" +
            "                                \"size\": 100,\n" +
            "                                \"constraints\": {\n" +
            "                                    \"maxLength\": \"200\",\n" +
            "                                    \"minLength\": \"1\"\n" +
            "                                }\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"Pin Code\",\n" +
            "                                \"type\": \"Text\",\n" +
            "                                \"displayText\": \"\",\n" +
            "                                \"helpText\": \"\",\n" +
            "                                \"orientation\": \"H\",\n" +
            "                                \"isEditable\": false,\n" +
            "                                \"mandatory\": false,\n" +
            "                                \"size\": 100,\n" +
            "                                \"constraints\": {\n" +
            "                                    \"maxLength\": \"200\",\n" +
            "                                    \"minLength\": \"1\"\n" +
            "                                }\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"State\",\n" +
            "                                \"type\": \"Text\",\n" +
            "                                \"displayText\": \"\",\n" +
            "                                \"helpText\": \"\",\n" +
            "                                \"orientation\": \"H\",\n" +
            "                                \"isEditable\": false,\n" +
            "                                \"mandatory\": false,\n" +
            "                                \"size\": 100,\n" +
            "                                \"constraints\": {\n" +
            "                                    \"maxLength\": \"200\",\n" +
            "                                    \"minLength\": \"1\"\n" +
            "                                }\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name\": \"Full Name\",\n" +
            "                        \"mandatory\": false,\n" +
            "                        \"fields\": [\n" +
            "                            {\n" +
            "                                \"name\": \"First Name\",\n" +
            "                                \"type\": \"Text\",\n" +
            "                                \"displayText\": \"\",\n" +
            "                                \"helpText\": \"\",\n" +
            "                                \"orientation\": \"H\",\n" +
            "                                \"isEditable\": false,\n" +
            "                                \"mandatory\": false,\n" +
            "                                \"size\": 100,\n" +
            "                                \"constraints\": {\n" +
            "                                    \"maxLength\": \"30\",\n" +
            "                                    \"minLength\": \"3\"\n" +
            "                                }\n" +
            "                            },\n" +
            "                            {\n" +
            "                                \"name\": \"Last Name\",\n" +
            "                                \"type\": \"Text\",\n" +
            "                                \"displayText\": \"\",\n" +
            "                                \"helpText\": \"\",\n" +
            "                                \"orientation\": \"H\",\n" +
            "                                \"isEditable\": false,\n" +
            "                                \"mandatory\": false,\n" +
            "                                \"size\": 100,\n" +
            "                                \"constraints\": {\n" +
            "                                    \"maxLength\": \"30\",\n" +
            "                                    \"minLength\": \"3\"\n" +
            "                                }\n" +
            "                            }\n" +
            "                        ]\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name\": \"Gender\",\n" +
            "                        \"type\": \"Dropdown\",\n" +
            "                        \"displayText\": \"\",\n" +
            "                        \"helpText\": \"\",\n" +
            "                        \"orientation\": \"H\",\n" +
            "                        \"isEditable\": false,\n" +
            "                        \"mandatory\": false,\n" +
            "                        \"size\": 100,\n" +
            "                        \"constraints\": {\n" +
            "                            \"allowedValues\": [\n" +
            "                                \"Male\",\n" +
            "                                \"Female\",\n" +
            "                                \"Others\"\n" +
            "                            ]\n" +
            "                        }\n" +
            "                    },\n" +
            "                    {\n" +
            "                        \"name\": \"Mobile Number\",\n" +
            "                        \"type\": \"Numeric\",\n" +
            "                        \"displayText\": \"\",\n" +
            "                        \"helpText\": \"Mobile number to daal, Isi pe OTP aayega..\",\n" +
            "                        \"orientation\": \"H\",\n" +
            "                        \"isEditable\": false,\n" +
            "                        \"mandatory\": false,\n" +
            "                        \"size\": 100,\n" +
            "                        \"constraints\": {\n" +
            "                            \"maxLength\": \"10\",\n" +
            "                            \"minLength\": \"10\"\n" +
            "                        }\n" +
            "                    }\n" +
            "                ]\n" +
            "   }\n" +
            "}";
    public SubmitUserForm() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.SUBMIT_USER_FORM);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public SubmitUserForm buildRequest(String linkId){
        setContext("body.linkId",linkId);
        return this;
    }
    public String getRequest() {
        return request;
    }
}
