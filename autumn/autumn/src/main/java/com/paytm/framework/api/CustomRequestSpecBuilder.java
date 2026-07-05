package com.paytm.framework.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.paytm.framework.reporting.Reporter;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.RestAssuredConfig;
import io.restassured.mapper.ObjectMapperType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

public class CustomRequestSpecBuilder extends RequestSpecBuilder {

    private BaseApi baseApi;

    public RequestSpecBuilder setBody(String jsonFilePath, Map<String, String> keysToUpdate) throws IOException, ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject json = (JSONObject) jsonParser.parse(new FileReader(jsonFilePath));
        json = setProperty(json, keysToUpdate);
        return super.setBody(json);
    }

    public CustomRequestSpecBuilder(BaseApi baseApi) {
        this.baseApi = baseApi;
    }

    public CustomRequestSpecBuilder() {
        super();
    }

    private JSONObject setProperty(JSONObject js, Map<String, String> keysToUpdate) throws ParseException {
        JSONParser jsonParser = new JSONParser();
        JSONObject context = js;
        if (keysToUpdate != null) {
            for (String key : keysToUpdate.keySet()) {
                String[] keyOrder = key.split("\\.");
                String keyName = "";
                if (keyOrder.length >= 2) {
                    for (int i = 0; i < keyOrder.length - 1; i++) {
                        int index = 0;
                        if (keyOrder[i + 1].contains("[")) {
                            index = Integer.parseInt(keyOrder[i + 1].split("\\[")[1].split("\\]")[0]);
                            keyName = keyOrder[i + 1].split("\\[")[0];
                        } else {
                            keyName = keyOrder[i + 1];
                        }
                        if (context.get(keyOrder[i]) instanceof JSONArray) {
                            context = (JSONObject) ((JSONArray) context.get(keyOrder[i])).get(index);
                        } else {
                            context = (JSONObject) context.get(keyOrder[i]);
                        }
                    }
                }

                if (isJSONValid(keysToUpdate.get(key))) {
                    context.put(keyName, (JSONObject) jsonParser.parse(keysToUpdate.get(key)));
                } else {
                    context.put(keyName, keysToUpdate.get(key));
                }
                context = js;
            }
        }
        return js;
    }

    private boolean isJSONValid(String test) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObj = (JSONObject) jsonParser.parse(test);
        } catch (ParseException ex) {
            return false;

        } catch (ClassCastException ex) {
            return false;
        }
        return true;
    }

    @Override
    public RequestSpecBuilder setBody(String body) {
        this.baseApi.updateDocumentContext(JsonPath.parse(body));
        return super.setBody(body);
    }

    @Override
    public RequestSpecBuilder setBody(byte[] body) {
        this.baseApi.updateDocumentContext(JsonPath.parse(body));
        return super.setBody(body);
    }

    @Override
    public RequestSpecBuilder setBody(Object object) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String reqBody = mapper.writeValueAsString(object);
            this.baseApi.updateDocumentContext(JsonPath.parse(reqBody));
        } catch (JsonProcessingException e) {
            Reporter.report.error("Couldn't setBody "+e.getMessage());
        }
        return super.setBody(object);
    }

}
