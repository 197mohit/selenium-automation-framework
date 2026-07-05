package com.paytm.api.saveCard;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.PGPHelpers;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.paytm.appconstants.Constants.savedCard.GET_BIN_BULK_HASH;

public class GetBinBulkHash extends BaseApi {

    private static final String REQUEST_TEMPLATE =
            "{\n"
                    + "    \"head\": {\n"
                    + "      \"tokenType\": \"JWT\",\n"
                    + "      \"signature\": \"{SIGNATURE}\"\n"
                    + "    },\n"
                    + "    \"body\": {\n"
                    + "      \"cardBinList\": {CARD_BIN_LIST}\n"
                    + "    }\n"
                    + "}";

    public GetBinBulkHash(List<String> cardBinList) {
        setMethod(MethodType.POST);
        getRequestSpecBuilder().setContentType(ContentType.JSON);
        getRequestSpecBuilder().setAccept(ContentType.JSON);
        getRequestSpecBuilder().addHeader("X-Amzn-Trace-Id", PGPHelpers.TraceIdGenerator());
        getRequestSpecBuilder().setBaseUri(LocalConfig.PGP_HOST);
        getRequestSpecBuilder().setBasePath(GET_BIN_BULK_HASH);

        String cardBinListCsv = String.join(",", cardBinList);
        Map<String, String> jwtClaims = new HashMap<>();
        jwtClaims.put("cardBinList", cardBinListCsv);
        jwtClaims.put("tokenType", "JWT");
        String jwtSignature =
                PGPHelpers.createJsonWebToken(
                        jwtClaims, PGPHelpers.ISSUER.ts, LocalConfig.SAVED_CARD_PG_JWT_KEY);

        String cardBinListJson =
                cardBinList.stream()
                        .map(bin -> "\"" + bin + "\"")
                        .collect(Collectors.joining(",", "[", "]"));

        String requestBody =
                REQUEST_TEMPLATE
                        .replace("{SIGNATURE}", jwtSignature)
                        .replace("{CARD_BIN_LIST}", cardBinListJson);
        getRequestSpecBuilder().setBody(requestBody);
    }
}
