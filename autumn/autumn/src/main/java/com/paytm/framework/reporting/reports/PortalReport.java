package com.paytm.framework.reporting.reports;

import com.epam.reportportal.message.ReportPortalMessage;
import com.epam.ta.reportportal.ws.model.log.SaveLogRQ;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.reportportal.ReporterConfig;
import com.paytm.framework.reportportal.api.LogItemRequest;
import com.paytm.framework.reportportal.contants.FeaturesEvaluator;
import com.paytm.framework.reportportal.service.dto.LaunchInfo;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Level;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.UUID;

public class PortalReport implements Report {
    @Override
    public void step(String s, Object... valuesToBeReplacePlaceholders) {
        switch (FeaturesEvaluator.FEATURE_ATLAS){
            case ENABLED:
                if (null != LaunchInfo.getInstance().getItemUuid() &&
                        !LaunchInfo.getInstance().getItemUuid().isEmpty())
                    getReq(Level.INFO, s).execute();
                break;
        }
//        if (ReporterConfig.mandatoryPropCheck &&
//                null != LaunchInfo.getInstance().getItemUuid() &&
//                !LaunchInfo.getInstance().getItemUuid().isEmpty())
//            getReq(Level.INFO, s).execute();
    }

    @Override
    public void info(String s, Object... valuesToBeReplacePlaceholders) {
        switch (FeaturesEvaluator.FEATURE_ATLAS){
            case ENABLED:
                if (null != LaunchInfo.getInstance().getItemUuid() &&
                        !LaunchInfo.getInstance().getItemUuid().isEmpty())
                    getReq(Level.INFO, s).execute();
                break;
        }
//        if (ReporterConfig.mandatoryPropCheck &&
//                null != LaunchInfo.getInstance().getItemUuid() &&
//                !LaunchInfo.getInstance().getItemUuid().isEmpty())
//            getReq(Level.INFO, s).execute();
    }

    @Override
    public void warn(String s, Object... valuesToBeReplacePlaceholders) {
        switch (FeaturesEvaluator.FEATURE_ATLAS){
            case ENABLED:
                if (null != LaunchInfo.getInstance().getItemUuid() &&
                        !LaunchInfo.getInstance().getItemUuid().isEmpty())
                    getReq(Level.WARN, s).execute();
                break;
        }
//        if (ReporterConfig.mandatoryPropCheck &&
//                null != LaunchInfo.getInstance().getItemUuid() &&
//                !LaunchInfo.getInstance().getItemUuid().isEmpty())
//            getReq(Level.WARN, s).execute();
    }

    @Override
    public void error(String s, Object... valuesToBeReplacePlaceholders) {
        switch (FeaturesEvaluator.FEATURE_ATLAS){
            case ENABLED:
                if (null != LaunchInfo.getInstance().getItemUuid() &&
                        !LaunchInfo.getInstance().getItemUuid().isEmpty())
                    getReq(Level.ERROR, s).execute();
                break;
        }
//        if (ReporterConfig.mandatoryPropCheck &&
//                null != LaunchInfo.getInstance().getItemUuid() &&
//                !LaunchInfo.getInstance().getItemUuid().isEmpty())
//            getReq(Level.ERROR, s).execute();
    }

    @Override
    public void debug(String s, Object... valuesToBeReplacePlaceholders) {
        switch (FeaturesEvaluator.FEATURE_ATLAS){
            case ENABLED:
                if (null != LaunchInfo.getInstance().getItemUuid() &&
                        !LaunchInfo.getInstance().getItemUuid().isEmpty())
                    getReq(Level.DEBUG, s).execute();
                break;
        }
//        if (ReporterConfig.mandatoryPropCheck &&
//                null != LaunchInfo.getInstance().getItemUuid() &&
//                !LaunchInfo.getInstance().getItemUuid().isEmpty())
//            getReq(Level.DEBUG, s).execute();
    }

    //TODO: need to be fixed
    @Override
    public void attachHtml(File file) {
        com.paytm.framework.reportportal.service.dto.File file1 = new com.paytm.framework.reportportal.service.dto.File();
        file1.setName(UUID.randomUUID().toString());
        try {
            file1.setContent(FileUtils.readFileToByteArray(file));
        } catch (IOException e) {
        }
        file1.setContentType("image/png");
        getReq(Level.INFO, "")
                .setContext("file", file1)
                .execute();


        JSONObject j = new JSONObject();
        j.put("name", file.getAbsolutePath());
        getReq(Level.INFO, "")
                .setContext("file", j)
                .execute();
    }

    @Override
    public void attachImage(File file, String imageName) {
        switch (FeaturesEvaluator.FEATURE_ATLAS){
            case ENABLED:
                if (null != LaunchInfo.getInstance().getItemUuid() &&
                        !LaunchInfo.getInstance().getItemUuid().isEmpty()){
                    generateRequestForImage(file, imageName).post();
                    deleteTempFileRequest(file);
                }
                break;
        }
//        if (ReporterConfig.mandatoryPropCheck &&
//                null != LaunchInfo.getInstance().getItemUuid() &&
//                !LaunchInfo.getInstance().getItemUuid().isEmpty()) {
//            generateRequestForImage(file, imageName).post();
//            deleteTempFileRequest(file);
//        }
    }

    public void generateRequestForHtml(File file) {

    }

    private void deleteTempFileRequest(File file){
        String testJsonFileName = file.getName().replace("png", "json");
        String testJsonFilePath = file.getParent();
        File file1 = new File(testJsonFilePath + "/" + testJsonFileName);
        if(file1.exists() && file1.delete()){
//            System.out.println(file1 + " deleted successfully");    // TODO: print logger
        }
    }

    private RequestSpecification generateRequestForImage(File file, String imageName) {
        SaveLogRQ.File file2 = new SaveLogRQ.File();
        file2.setName(file.getName());
        SaveLogRQ rq = new SaveLogRQ();
        rq.setItemUuid(LaunchInfo.getInstance().getItemUuid());
        rq.setLaunchUuid(LaunchInfo.getInstance().getLaunchId());
        rq.setLevel(Level.INFO.toString());
        rq.setLogTime(Calendar.getInstance().getTime());
        rq.setMessage(imageName);
        ReportPortalMessage message = null;
        try {
            message = new ReportPortalMessage(file, "Binary data reported");
            file2.setContentType(message.getData().getMediaType());
            file2.setContent(message.getData().read());
        } catch (IOException e) {
        }
        rq.setFile(file2);
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(new ObjectMapper().convertValue(rq, JSONObject.class));

        File testJson = getTestJsonFile(file, jsonArray);

        return RestAssured
                .given()
                .header("Authorization", "bearer " + ReporterConfig.RP_UUID)
                .multiPart("json_request_part", testJson, ContentType.JSON.toString())
                .multiPart("file", file, "image/png")
                .basePath("/api/v2/" + ReporterConfig.RP_PROJECT + "/log")
                .baseUri(ReporterConfig.RP_ENDPOINT);
    }

    private File getTestJsonFile(File file, JSONArray array) {
        String testJsonFileName = file.getName().replace("png", "json");
        String testJsonFilePath = file.getParent();
        try {
            FileWriter writer = new FileWriter(testJsonFilePath + "/" + testJsonFileName);
            writer.write(array.toJSONString());
            writer.close();
        } catch (IOException e) {
        }
        return new File(testJsonFilePath + "/" + testJsonFileName);
    }

    private LogItemRequest getReq(Level level, String message) {
        LogItemRequest rq = new LogItemRequest();
        rq.setContext("itemUuid", LaunchInfo.getInstance().getItemUuid());
        rq.setContext("time", Calendar.getInstance().getTime());
        rq.setContext("level", level.toString());
        rq.setContext("message", "[" + level + "]" + " - " + message);
        return rq;
    }
}
