package com.paytm.framework.AnalysisDTO;

public class AnalysisResultDTO {


    private String profile;
    private String xmlfileName;
    private Tests[] tests;

    public AnalysisResultDTO(String profile, String xmlfileName, Tests[] tests) {
        this.profile = profile;
        this.xmlfileName = xmlfileName;
        this.tests = tests;
    }

    public AnalysisResultDTO() {

    }

    public String getProfile() {
        return profile;
    }

    public AnalysisResultDTO setProfile(String profile) {
        this.profile = profile;
        return this;
    }

    public String getXmlfileName() {
        return xmlfileName;
    }

    public AnalysisResultDTO setXmlfileName(String xmlfileName) {
        this.xmlfileName = xmlfileName;
        return this;
    }

    public Tests[] getTests() {
        return tests;
    }

    public AnalysisResultDTO setTests(Tests[] tests) {
        this.tests = tests;
        return this;
    }

}
