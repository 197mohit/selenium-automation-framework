package com.paytm.api.AOA;

import com.paytm.LocalConfig;
import com.paytm.appconstants.Constants;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

public class AddMerchant extends BaseApi {
    String request ="{\n" +
            "  \"addMerchant\": {\n" +
            "    \"merchantType\": \"CORPORATION\",\n" +
            "    \"officialName\": \"aggregator10\"\n" +
            "  },\n" +
            "  \"updateMerchant\": {\n" +
            "    \"mccCodes\": [\n" +
            "      \"Retail\"\n" +
            "    ],\n" +
            "    \"logoUrl\": null,\n" +
            "    \"officialName\": \"aggregator11\",\n" +
            "    \"englishName\": \"aggregator11\",\n" +
            "    \"localName\": \"aggregator11\",\n" +
            "    \"certificateType\": null,\n" +
            "    \"certificateNo\": null,\n" +
            "    \"certificateUrls\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"certificateExpireDate\": \"2030-12-21T00:00:00+00:00\",\n" +
            "    \"registeredAddress\": {\n" +
            "      \"countryName\": \"INDIA\",\n" +
            "      \"stateName\": \"Uttar Pradesh\",\n" +
            "      \"cityName\": \"Noida\",\n" +
            "      \"areaName\": \"F1\",\n" +
            "      \"address1\": \"PAYTM\",\n" +
            "      \"address2\": \"Sec 6\",\n" +
            "      \"zipCode\": \"133001\"\n" +
            "    },\n" +
            "    \"officeAddress\": {\n" +
            "      \"countryName\": \"INDIA\",\n" +
            "      \"stateName\": \"Uttar Pradesh\",\n" +
            "      \"cityName\": \"Noida\",\n" +
            "      \"areaName\": \"F1\",\n" +
            "      \"address1\": \"PAYTM\",\n" +
            "      \"address2\": \"Sec 6\",\n" +
            "      \"zipCode\": \"133001\"\n" +
            "    },\n" +
            "    \"officeTelephone\": null,\n" +
            "    \"faxTelephone\": \"null\",\n" +
            "    \"corporateOfficialName\": null,\n" +
            "    \"corporateCertificateType\": null,\n" +
            "    \"corporateCertificateNo\": null,\n" +
            "    \"contactOfficialName\": {\n" +
            "      \"firstName\": \"arzoo\",\n" +
            "      \"lastName\": \"batra\"\n" +
            "    },\n" +
            "    \"contactMobileNo\": {\n" +
            "      \"mobileNo\": \"91-7007101778\",\n" +
            "      \"verified\": true\n" +
            "    },\n" +
            "    \"contactTelephone\": \"0751-26888888\",\n" +
            "    \"contactEmail\": {\n" +
            "      \"email\": \"pulkit.agarwal@paytm.com\",\n" +
            "      \"verified\": true\n" +
            "    },\n" +
            "    \"operationSource\": \"\",\n" +
            "    \"agentId\": \"216610000000202284599\"\n" +
            "  },\n" +
            "  \"prefInfo\": {\n" +
            "    \"merchantPreferenceInfos\": [\n" +
            "      {\n" +
            "        \"prefType\": \"STORE CARD DETAILS\",\n" +
            "        \"prefStatus\": \"ACTIVE\",\n" +
            "        \"prefValue\": \"Y\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"prefType\": \"CHECKSUM_ENABLED\",\n" +
            "        \"prefStatus\": \"ACTIVE\",\n" +
            "        \"prefValue\": \"N\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"prefType\": \"REFUND_SUCCESS_PEON_ENABLED\",\n" +
            "        \"prefStatus\": \"ACTIVE\",\n" +
            "        \"prefValue\": \"N\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"prefType\": \"REFUND_SUCCESS_ENCPARAMS_ENABLED\",\n" +
            "        \"prefStatus\": \"ACTIVE\",\n" +
            "        \"prefValue\": \"N\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"prefType\": \"REFUND_SUCCESS_S2S_CHECKSUM_ENABLED\",\n" +
            "        \"prefStatus\": \"ACTIVE\",\n" +
            "        \"prefValue\": \"N\"\n" +
            "      },\n" +
            "      {\n" +
            "        \"prefType\": \"ENCPARAMS_ENABLED\",\n" +
            "        \"prefStatus\": \"ACTIVE\",\n" +
            "        \"prefValue\": \"N\"\n" +
            "      }\n" +
            "    ]\n" +
            "  },\n" +
            "  \"kycDetails\": {\n" +
            "    \"panNumber\": \"pan_number_data1\",\n" +
            "    \"bankAccount\": \"12121212121212\",\n" +
            "    \"ifscCode\": \"HDFC0001112\",\n" +
            "    \"gstin\": \"gstin_data1\",\n" +
            "    \"bankAccHolderName\": \"arzooBatra\",\n" +
            "    \"bankName\": \"HDFC\"\n" +
            "  },\n" +
            "  \"kycAuthSignatory\": {\n" +
            "    \"name\": \"\",\n" +
            "    \"panNumber\": \"\",\n" +
            "    \"addressProofNumber\": \"\",\n" +
            "    \"idProofNumber\": \"\"\n" +
            "  },\n" +
            "  \"merchantUrlInfo\": {\n" +
            "    \"postBackurl\": \"https://pgp-automation.paytm.in/mockbank/MerchantSite/bankResponse\",\n" +
            "    \"notificationStatusUrl\": \"https://pgp-automation.paytm.in/mockbank/peon\",\n" +
            "    \"status\": \"ACTIVE\"\n" +
            "  },\n" +
            "  \"extendedInfo\": {\n" +
            "    \"entityKey\": \"/vqiS9O14Ieex3RxWsHuLwR7RQgaCOo6CcFBiJT5RK+oO8f++5lt9aliOUaG1h+0\",\n" +
            "    \"merchCommPref\": \"19\",\n" +
            "    \"custCommPref\": \"19\",\n" +
            "    \"merchantWebForcedTheme\": \"\",\n" +
            "    \"merchantWapForcedTheme\": \"\",\n" +
            "    \"platformType\": \"AOA\",\n" +
            "    \"isPeonEnable\": true\n" +
            "  },\n" +
            "  \"commPrefInfo\": {\n" +
            "    \"merchCommPref\": [\n" +
            "      \n" +
            "    ],\n" +
            "    \"custCommPref\": [\n" +
            "      \n" +
            "    ]\n" +
            "  }\n" +
            "}";
    public AddMerchant() {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(Constants.MappingService.AOA_MERCHANT_ADD);
        getRequestSpecBuilder().setBody(getRequest());
        getRequestSpecBuilder().addHeader("Content-Type","application/json");
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }
}
