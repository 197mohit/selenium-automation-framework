package com.paytm.framework.api;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.paytm.framework.api.curlloggingutil.CurlLoggingRestAssuredConfigBuilder;
import com.paytm.framework.reporting.reports.Report;
import com.paytm.framework.reportportal.ReporterConfig;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.internal.RequestSpecificationImpl;
import io.restassured.parsing.Parser;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.fest.assertions.api.Assertions;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;
import org.testng.Reporter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static io.restassured.RestAssured.given;

public abstract class BaseApi {

    private CustomRequestSpecBuilder requestSpecBuilder = new CustomRequestSpecBuilder(this);
    private MethodType method;
    private Report report = com.paytm.framework.reporting.Reporter.report;
    private LinkedHashMap<String, Object> context = new LinkedHashMap<>();
    private LinkedList<String> deleteContext = new LinkedList<>();
    private DocumentContext requestContext = null;
    private boolean isRequestLoaded = false;
    private String requestTemplatePath = requestTemplatePath();
    private boolean disableCurlLogging = false;

//    static {
//        LoggerContext logContext = (LoggerContext) LoggerFactory.getILoggerFactory();
//        Logger log = logContext.getLogger("com.jayway.jsonpath.internal.path.CompiledPath");
//        log.setLevel(Level.INFO);
//        logContext.getLogger("com.jayway.jsonpath.internal.JsonContext").setLevel(Level.INFO);
//    }

    public BaseApi(Report report) {
        this.report = report;
        jsonRequestTemplateLoader();
    }

    public BaseApi() {
        jsonRequestTemplateLoader();
    }

    protected String requestTemplatePath() {
        return null;
    }

    @Deprecated
    public final void updateDocumentContext(DocumentContext documentContext) {
        this.requestContext = documentContext;
        isRequestLoaded = true;
    }

    public MethodType getMethod() {
        return method;
    }

    public void setMethod(MethodType method) {
        this.method = method;
    }

    public CustomRequestSpecBuilder getRequestSpecBuilder() {
        return requestSpecBuilder;
    }

    public BaseApi deleteContext(String jsonPath) {
        this.deleteContext.add(jsonPath);
        return this;
    }

    /**
     * This will update/add data in the existing request at runtime.
     *
     * @param jsonPath jsonPath to the key which needs to update
     * @param value    value which needs to updated at jsonPath
     * @return
     */
    public BaseApi setContext(String jsonPath, Object value) {
        if (isDTOClass(value)) {
            try {
                this.context.put(jsonPath, new ObjectMapper().convertValue(value, JSONObject.class));
                return this;
            } catch (IllegalArgumentException ignored) {
            }
        }
        this.context.put(jsonPath, value);
        return this;
    }

    public void setDisableCurlLogging(boolean disableCurlLogging) {
        this.disableCurlLogging = disableCurlLogging;
    }

    public Map<String, ?> getContext() {
        return this.context;
    }

    private boolean isDTOClass(Object value) {
        if (value != null)
            return !value.getClass().isEnum() && value.getClass().getPackage().getName().contains("com.paytm");
        return false;
    }

    public Object getRequestBody() {
        updateRequestContext();
        return this.requestContext.json();
    }

    public Response execute() {
        if (null != requestContext) {
            updateRequestContext();
            Object reqBody = this.requestContext.json();
            this.requestSpecBuilder.setBody(reqBody);
        }
        return doExecute();
    }

    private Response doExecute() {
        RequestSpecification requestSpecification = requestSpecBuilder.build();
        Response response;
        RestAssured.defaultParser = Parser.JSON;
        RequestSpecificationImpl rqImpl = (RequestSpecificationImpl) requestSpecification;
        RestAssuredConfig config = RestAssuredConfig.newConfig();
        if (rqImpl.getBaseUri().equalsIgnoreCase(ReporterConfig.RP_ENDPOINT)) {
            config = RestAssuredConfig.newConfig();
        } else if (!disableCurlLogging)
            config = new CurlLoggingRestAssuredConfigBuilder().build();

        switch (method) {
            case GET:
                response = given().config(config).spec(requestSpecification).when().get();
                break;
            case POST:
                response = given().config(config).spec(requestSpecification).when().post();
                break;
            case PUT:
                response = given().config(config).spec(requestSpecification).when().put();
                break;
            case DELETE:
                response = given().config(config).spec(requestSpecification).when().delete();
                break;
            case PATCH:
                response = given().config(config).spec(requestSpecification).when().patch();
                break;
            default:
                throw new RuntimeException("API method not specified");

        }
        if (!rqImpl.getBaseUri().equalsIgnoreCase(ReporterConfig.RP_ENDPOINT) && !disableCurlLogging)
            printResponse(response);
        return response;
    }

    public Response executeUntilExpectedConditionMet(String expectedJSONKey, String expectedJSONValue, int pollingTimeInSec, int pollingCountInSec) {
        Response response = null;
        String actualJSONValue = "";
        try {
            for (int i = 0; i < pollingCountInSec; i++) {
                response = this.execute();
                actualJSONValue = response.jsonPath().get(expectedJSONKey).toString();
                if (expectedJSONValue.equalsIgnoreCase(actualJSONValue)) {
                    break;
                }
                Thread.sleep(pollingTimeInSec * 1000);
            }
        } catch (InterruptedException e) {
            Reporter.log("Couldn't sleep "+e.getMessage());
        }
        Assertions.assertThat(actualJSONValue).isEqualToIgnoringCase(expectedJSONValue);
        return response;
    }

    private void printResponse(Response response) {
        String contentType = response.contentType();

        if (contentType.toLowerCase().contains("text/html") || contentType.toLowerCase().contains("text/plain")) {
            final DateFormat timeFormat = new SimpleDateFormat("MM.dd.yyyy HH-mm-ss");
            final String fileName = Reporter.getCurrentTestResult().getMethod().getMethodName() + "_" + timeFormat.format(new Date()) + ".html";

            String outputDir = Reporter.getCurrentTestResult().getTestContext().getOutputDirectory();
            outputDir = outputDir.substring(0, outputDir.lastIndexOf(File.separator)) + "/html";

            File file = new File(outputDir + File.separator + fileName);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }
            PrintWriter writer = null;

            try {
                file.createNewFile();
                writer = new PrintWriter(file);
                writer.write(response.asString());
                writer.flush();
                report.attachHtml(file);
                //this.report.info("<a href=\"" + fileName + "\" target=\"_blank\"><b>API Response</b></a><br>");
            } catch (Throwable e) {
                throw new RuntimeException(e);
            } finally {
                writer.close();
            }
        } else {
            this.report.info("API Response:" + response.getBody().prettyPrint());
        }
    }

    public BaseApi setBodyOmitNullValueAttributes(Object obj) {
        String request = "";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
        try {
            request = mapper.writeValueAsString(obj);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        requestSpecBuilder.setBody(request);
        return this;
    }

    public BaseApi setBodyKeepNullValueAttributes(Object obj) {
        String request = "";
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.USE_ANNOTATIONS, false);
        mapper.setSerializationInclusion(Include.ALWAYS);
        try {
            request = mapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        requestSpecBuilder.setBody(request);
        return this;
    }

    public BaseApi setBody(Object obj) {
        requestSpecBuilder.setBody(obj);
        return this;
    }

    public BaseApi setBody(String jsonFilePath, Map<String, String> keysToUpdate) throws IOException, ParseException {
        requestSpecBuilder.setBody(jsonFilePath, keysToUpdate);
        return this;
    }

    public enum MethodType {
        POST, GET, PUT, DELETE, PATCH
    }

    private void jsonRequestTemplateLoader() {
        if (!isRequestLoaded) {
            String requestTmpPath = requestTemplatePath;
            if (null != requestTmpPath) {
                if (!requestTmpPath.isEmpty()) {
                    String requestPath = "api/request/" + requestTmpPath;
                    if (!requestPath.contains("reportportal"))
                        System.out.println("loading request: " + requestPath);
                    ClassLoader cl = getClass().getClassLoader();
                    URL resource = cl.getResource(requestPath);
                    try {
                        updateDocumentContext(JsonPath.parse(resource));
                    } catch (IOException e) {
                        throw new RuntimeException("Error occurred while loading request: " + requestPath, e);
                    }
                }
            }
        }
    }

    private void updateRequestContext() {
        if (requestContext == null) {
            System.out.println("No need to set context when baseTemplate not provided");
        } else {
            context.forEach((k, v) -> {
                String[] path = k.split("\\.");
                if (path != null && path.length > 0) {
                    StringJoiner trgtPath = new StringJoiner(".", "$.", "");
                    for (int i = 0; i < path.length - 1; i++) {
                        trgtPath.add(path[i]);
                    }
                    if (v instanceof List) {
                        JSONArray array = new ObjectMapper().convertValue(v, JSONArray.class);
                        if (trgtPath.toString().equalsIgnoreCase("$."))
                            requestContext.put("$", path[path.length - 1], v);
                        else
                            requestContext.put(trgtPath.toString(), path[path.length - 1], array);
                    } else if (getClassName(v).contains("com.paytm") && !v.getClass().isEnum()) {
                        JSONObject object = new ObjectMapper().convertValue(v, JSONObject.class);
                        if (trgtPath.toString().equalsIgnoreCase("$."))
                            requestContext.put("$", path[path.length - 1], v);
                        else
                            requestContext.put(trgtPath.toString(), path[path.length - 1], object);
                    } else if (trgtPath.toString().equalsIgnoreCase("$.")) {
                        requestContext.put("$", path[path.length - 1], v);
                    } else
                        requestContext.put(trgtPath.toString(), path[path.length - 1], v);
                }
            });
            deleteContext.forEach(p -> {
                String[] path = p.split("\\.");
                if (path != null && path.length > 0) {
                    StringJoiner trgtPath = new StringJoiner(".", "$.", "");
                    for (int i = 0; i < path.length - 1; i++) {
                        trgtPath.add(path[i]);
                    }
                    requestContext.delete(trgtPath.toString() + "." + path[path.length - 1]);
                }
            });
            context.clear();
            deleteContext.clear();
        }
    }

    private String getClassName(Object obj) {
        try {
            return obj.getClass().getName();
        } catch (Exception e) {
            return "";
        }
    }
}
