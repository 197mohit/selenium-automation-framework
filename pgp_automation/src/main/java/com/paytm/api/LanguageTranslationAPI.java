package com.paytm.api;

import com.paytm.LocalConfig;
import com.paytm.framework.api.BaseApi;
import io.restassured.http.ContentType;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.paytm.framework.api.BaseApi.MethodType.POST;

public class LanguageTranslationAPI extends BaseApi {

    public LanguageTranslationAPI(Language language, String... texts) {
        super.setMethod(POST);
        super.getRequestSpecBuilder().setBaseUri(LocalConfig.UTILITIES_HOST);
        super.getRequestSpecBuilder().setBasePath("/v1/language/translation");
        super.getRequestSpecBuilder().setContentType(ContentType.JSON);
        String strings = "[" +
                String.join(",", Arrays.stream(texts).map(s -> "\"" + s + "\"").collect(Collectors.toSet())) +
                "]";
        super.setBody("{\"data\":{\"strings\":" + strings + ",\"section_id\":10,\"language_id\":" + language.code + ",\"subsection_id\":1700,\"quality\":2,\"priority\":1}}");
    }

    public enum Language {
        HINDI(2);

        private int code;

        Language(int code) {
            this.code = code;
        }
    }
}