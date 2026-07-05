package com.paytm.framework.utils;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.AnalysisDTO.AnalysisResultDTO;
import com.paytm.framework.AnalysisDTO.Tests;
import com.paytm.framework.core.ExecutionConfig;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class AnalysisUtils {

    private static AnalysisResultDTO analysisResult;
    private Map<String, Tests> map = new HashMap<>();
    private static AnalysisUtils analysisUtils;
    String jsonReport = ExecutionConfig.ANALYSIS_PATH + "/" + getAnalysisFileName();

    public static AnalysisUtils getinstance() {
        if (analysisUtils == null) {
            return new AnalysisUtils();
        }
        return analysisUtils;
    }

    public Map<String, Tests> getTestDetails() {
        return map;
    }

    public static AnalysisResultDTO getAnalysisResult() {
        return analysisResult;
    }

    public static String getProfile() {
        return ExecutionConfig.CURRENT_PROFILE;
    }

    public static String getSuiteFileName() {
        return ExecutionConfig.SUITE_XML_FILE;
    }

    public static String getAnalysisFileName() {
        return getProfile() + "_analysis.json";
    }

    public void loadAnalysis() {
        byte[] jsonData = null;
        if (isReportExist(jsonReport)) {

            File file = new File(jsonReport);
            try {
                jsonData = Files.readAllBytes(file.toPath());
                ObjectMapper objectMapper = new ObjectMapper();
                analysisResult = objectMapper.readValue(jsonData, AnalysisResultDTO.class);
                iterateAnalysis();
            } catch (IOException e) {
                System.out.println(e.getMessage() + " <== Exception occurred while reading existing report ==>");
            } catch (NullPointerException e) {
                System.out.println(e.getMessage() + " <== Empty File is present in directory ==>");
            }
        } else
            CommonUtils.createDirectory(ExecutionConfig.ANALYSIS_PATH);
    }

    public void writeAnalysis(Tests[] tests) {

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            FileOutputStream fileOut = new FileOutputStream(jsonReport, false);
            AnalysisResultDTO results = new AnalysisResultDTO().setProfile(getProfile())
                    .setXmlfileName(getSuiteFileName())
                    .setTests(tests);
            objectMapper.writeValue(fileOut, results);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage() + "<== Exception occurred during creating JSON object file ==>");
        } catch (JsonGenerationException e) {
            System.out.println(e.getMessage() + "<== Exception occurred during creating JSON object file ==>");
        } catch (JsonMappingException e) {
            System.out.println(e.getMessage() + "<== Exception occurred during creating JSON object file ==>");
        } catch (IOException e) {
            System.out.println(e.getMessage() + "<== Exception occurred during creating JSON object file ==>");
        }

    }


    private void iterateAnalysis() {
        Tests[] tests = this.analysisResult.getTests();
        if (!(tests.length < 1)) {

            for (Tests test : tests) {
                this.map.put(test.getTestCaseName(), test);
            }
        }
    }


    private boolean isReportExist(String jsonReport) {
        boolean status = false;
        if (new File(jsonReport).exists())
            status = true;
        return status;
    }

}
