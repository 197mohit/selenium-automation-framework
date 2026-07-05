package com.paytm.apphelpers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.paytm.ServerConfigProvider;
import io.restassured.path.json.JsonPath;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.paytm.apphelpers.LogsValidationHelper.getLogsOnServer;

public class generateEsnHelper {

    public static String getSubsESNFromDeepLink(String deeplink)
    {
        int esnIndex=0;
                esnIndex=deeplink.indexOf("tr=PAYTMSUBS");
        if (esnIndex>0 && esnIndex!=0)
        {
            return deeplink.substring(esnIndex+3,(esnIndex+31));
        }
        else
            return "ESN Not Found";
    }

    public String getNewESNFromAPI(JsonPath APIResponse)
    {
        return APIResponse.getJsonObject("body.newExternalSerialNo");
    }
    public String getmandateESNFromAPI(JsonPath APIResponse)
    {
        return APIResponse.getJsonObject("body.mandateExternalSerialNo");
    }

}
