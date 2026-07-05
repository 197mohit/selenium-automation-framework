package com.paytm.api.linkAPI.templateApis;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class SaveUpdateTemplate extends BaseApi {
    String request="{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"78yds87ty7ds\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"AES\",\n" +
            "    \"signature\": \"jhddyt87td87vd\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"mid\": \"DIGIPS86799399018799\",\n" +
            "    \"templateName\" : \"DigiPos template\",\n" +
            "    \"templateInfo\" : \"Detail12\",\n" +
            "   \n" +
            "   \n" +
            "        \"fields\": [\n" +
            "        \t\n" +
            "        \t {\n" +
            "                \"name\": \"Numbeqweqr\",\n" +
            "                \"type\": \"Numeric\",\n" +
            "                \"displayText\": \"\",\n" +
            "                \"helpText\": \"\",\n" +
            "                \"orientation\": \"V\",\n" +
            "                \"isEditable\": true,\n" +
            "                \"display\": true,\n" +
            "                \"mandatory\": true,\n" +
            "                \"size\": 50,\n" +
            "                \"constraints\": {\n" +
            "                    \"maxLength\": \"3\",\n" +
            "                    \"minLength\": \"1\",\n" +
            "                    \"maxValue\": \"150\",\n" +
            "                    \"minValue\": \"0\"\n" +
            "                }\n" +
            "            },\n" +
            "        \t\n" +
            "        \n" +
            "       \n" +
            "            {\n" +
            "                \"name\": \"Email ID\",\n" +
            "                \"type\": \"Text\",\n" +
            "                \"displayText\": \"\",\n" +
            "                \"helpText\": \"\",\n" +
            "                \"orientation\": \"H\",\n" +
            "                \"isEditable\": false,\n" +
            "                \"display\": true,\n" +
            "                \"mandatory\": false,\n" +
            "                \"size\": 100,\n" +
            "                \"constraints\": {\n" +
            "                    \"maxLength\": \"200\",\n" +
            "                    \"minLength\": \"3\"\n" +
            "                }\n" +
            "            },\n" +
            "        \n" +
            "            \n" +
            "        \n" +
            "        \t{\n" +
            "                \"name\": \"Number\",\n" +
            "                \"type\": \"Numeric\",\n" +
            "                \"displayText\": \"\",\n" +
            "                \"helpText\": \"\",\n" +
            "                \"orientation\": \"V\",\n" +
            "                \"mandatory\": true,\n" +
            "                \"size\": 50,\n" +
            "                \"constraints\": {\n" +
            "                 \"maxLength\": \"3\",\n" +
            "                    \"minLength\": \"1\",\n" +
            "                    \"maxValue\": \"150\",\n" +
            "                    \"minValue\": \"0\"\n" +
            "                  \n" +
            "                }\n" +
            "            },\n" +
            "            \t{\n" +
            "\t\t\t\"name\" : \"Gender\",\n" +
            "\t\t\t\"type\" : \"Dropdown\",\n" +
            "\t\t\t\"hint\" : \"\",\t\n" +
            "\t\t\t\"helpText\" : \"This is help text\",\n" +
            "\t\t\t\"constraints\" : {\n" +
            "\t\t\t\t\"allowedValues\" : [\"Male\",\"Female\"]\n" +
            "\t\t\t},\n" +
            "\t\t\t\"orientation\" : \"V\",\n" +
            "\t\t\t\"size\" : \"50\",\n" +
            "\t\t\t\"isEditable\" : false,\n" +
            "\t\t\t\"mandatory\": false,\n" +
            "\t\t\t\"display\" : true\n" +
            "\t\t},\n" +
            "\t\t{\n" +
            "\t\t\t\"name\" : \"State1\",\n" +
            "\t\t\t\"type\" : \"Dropdown\",\n" +
            "\t\t\t\"hint\" : \"\",\t\n" +
            "\t\t\t\"helpText\" : \"This is help text\",\n" +
            "\t\t\t\"constraints\" : {\n" +
            "\t\t\t\t\"allowedValues\" : [\"Haryana\",\"Punjab\"]\n" +
            "\t\t\t},\n" +
            "\t\t\t\"orientation\" : \"H\",\n" +
            "\t\t\t\"size\" : \"100\",\n" +
            "\t\t\t\"isEditable\" : false,\n" +
            "\t\t\t\"mandatory\": false,\n" +
            "\t\t\t\"display\" : true\n" +
            "\t\t}\n" +
            "        ]\n" +
            "    }\n" +
            "}";
    public SaveUpdateTemplate() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.SAVEUPDATETEMPLATE);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }
    public String getRequest() {
        return request;
    }
    public SaveUpdateTemplate buildRequest(String mid,String templateName){
        setContext("body.mid",mid);
        setContext("body.templateName",templateName);
        return this;
    }

    public SaveUpdateTemplate buildRequest(String mid, String templateName, String maxValue, String minValue, String type, String name){
        setContext("body.mid",mid);
        setContext("body.templateName",templateName);
        setContext("body.fields[0].type", type);
        setContext("body.fields[0].name", name);
        setContext("body.fields[0].constraints.minValue", minValue);
        setContext("body.fields[0].constraints.maxValue", maxValue);
        return this;
    }
    String paymentFormRequest="{\n" +
            "  \"head\": {\n" +
            "    \"timestamp\": \"1539601338741\",\n" +
            "    \"clientId\": \"78yds87ty7ds\",\n" +
            "    \"version\": \"v2\",\n" +
            "    \"channelId\": \"WEB\",\n" +
            "    \"tokenType\": \"AES\",\n" +
            "    \"signature\": \"jhddyt87td87vd\"\n" +
            "  },\n" +
            "  \"body\": {\n" +
            "    \"mid\": \"FfpEkO71115958595487\",\n" +
            "    \"templateName\" : \"USER_INFO_TASK_INVOICE1898\",\n" +
            "    \"templateInfo\" : \"Detail12\",\n" +
            "   \n" +
            "   \"fields\": [\n" +
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
            "    }\n" +
            "}";
    public SaveUpdateTemplate(String mid,String templateName) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.PGPAPIResourcePath.SAVEUPDATETEMPLATE);
        getRequestSpecBuilder().setBody(getPaymentFormRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
        setContext("body.templateName",templateName);
        setContext("body.mid",mid);
    }
    public String getPaymentFormRequest() {
        return paymentFormRequest;
    }
}
