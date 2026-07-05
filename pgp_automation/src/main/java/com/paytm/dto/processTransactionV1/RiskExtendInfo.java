package com.paytm.dto.processTransactionV1;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.StringJoiner;

public class RiskExtendInfo {
    private String deviceId = "";
    private String userLBSLatitude = "";
    private String userLBSLongitude = "";
    private String appVersion = "";
    private String versionCode = "";
    private String osType = "";
    private String userAgent = "";
    private String screenResolution = "";
    private String isRooted = "";
    private String deviceModel = "";
    private String deviceIMEI = "";
    private String browserType = "";
    private String browserVersion = "";
    private String deviceManufacturer = "";
    private String language = "";
    private String timeZone = "";
    private String cookieId = "";
    private String routerMac = "";
    private String channelId = "";
    private String businessFlow = "";
    private String operationType = "";
    private String platform = "";
    private String osVersion = "";
    private String deviceType = "";
    private String hybridPlatform = "";
    private String hybridPlatformVersion = "";
    private String gender = "";
    private String merchantType = "";
    private String extraParameters = "";

    public RiskExtendInfo() {
    }

    public String getDeviceId() {
        return deviceId;
    }

    public RiskExtendInfo setDeviceId(String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public String getUserLBSLatitude() {
        return userLBSLatitude;
    }

    public RiskExtendInfo setUserLBSLatitude(String userLBSLatitude) {
        this.userLBSLatitude = userLBSLatitude;
        return this;
    }

    public String getUserLBSLongitude() {
        return userLBSLongitude;
    }

    public RiskExtendInfo setUserLBSLongitude(String userLBSLongitude) {
        this.userLBSLongitude = userLBSLongitude;
        return this;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public RiskExtendInfo setAppVersion(String appVersion) {
        this.appVersion = appVersion;
        return this;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public RiskExtendInfo setVersionCode(String versionCode) {
        this.versionCode = versionCode;
        return this;
    }

    public String getOsType() {
        return osType;
    }

    public RiskExtendInfo setOsType(String osType) {
        this.osType = osType;
        return this;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public RiskExtendInfo setUserAgent(String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    public String getScreenResolution() {
        return screenResolution;
    }

    public RiskExtendInfo setScreenResolution(String screenResolution) {
        this.screenResolution = screenResolution;
        return this;
    }

    public String getIsRooted() {
        return isRooted;
    }

    public RiskExtendInfo setIsRooted(String isRooted) {
        this.isRooted = isRooted;
        return this;
    }

    public String getDeviceModel() {
        return deviceModel;
    }

    public RiskExtendInfo setDeviceModel(String deviceModel) {
        this.deviceModel = deviceModel;
        return this;
    }

    public String getDeviceIMEI() {
        return deviceIMEI;
    }

    public RiskExtendInfo setDeviceIMEI(String deviceIMEI) {
        this.deviceIMEI = deviceIMEI;
        return this;
    }

    public String getBrowserType() {
        return browserType;
    }

    public RiskExtendInfo setBrowserType(String browserType) {
        this.browserType = browserType;
        return this;
    }

    public String getBrowserVersion() {
        return browserVersion;
    }

    public RiskExtendInfo setBrowserVersion(String browserVersion) {
        this.browserVersion = browserVersion;
        return this;
    }

    public String getDeviceManufacturer() {
        return deviceManufacturer;
    }

    public RiskExtendInfo setDeviceManufacturer(String deviceManufacturer) {
        this.deviceManufacturer = deviceManufacturer;
        return this;
    }

    public String getLanguage() {
        return language;
    }

    public RiskExtendInfo setLanguage(String language) {
        this.language = language;
        return this;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public RiskExtendInfo setTimeZone(String timeZone) {
        this.timeZone = timeZone;
        return this;
    }

    public String getCookieId() {
        return cookieId;
    }

    public RiskExtendInfo setCookieId(String cookieId) {
        this.cookieId = cookieId;
        return this;
    }

    public String getRouterMac() {
        return routerMac;
    }

    public RiskExtendInfo setRouterMac(String routerMac) {
        this.routerMac = routerMac;
        return this;
    }

    public String getChannelId() {
        return channelId;
    }

    public RiskExtendInfo setChannelId(String channelId) {
        this.channelId = channelId;
        return this;
    }

    public String getBusinessFlow() {
        return businessFlow;
    }

    public RiskExtendInfo setBusinessFlow(String businessFlow) {
        this.businessFlow = businessFlow;
        return this;
    }

    public String getOperationType() {
        return operationType;
    }

    public RiskExtendInfo setOperationType(String operationType) {
        this.operationType = operationType;
        return this;
    }

    public String getPlatform() {
        return platform;
    }

    public RiskExtendInfo setPlatform(String platform) {
        this.platform = platform;
        return this;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public RiskExtendInfo setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public RiskExtendInfo setDeviceType(String deviceType) {
        this.deviceType = deviceType;
        return this;
    }

    public String getHybridPlatform() {
        return hybridPlatform;
    }

    public RiskExtendInfo setHybridPlatform(String hybridPlatform) {
        this.hybridPlatform = hybridPlatform;
        return this;
    }

    public String getHybridPlatformVersion() {
        return hybridPlatformVersion;
    }

    public RiskExtendInfo setHybridPlatformVersion(String hybridPlatformVersion) {
        this.hybridPlatformVersion = hybridPlatformVersion;
        return this;
    }

    public String getGender() {
        return gender;
    }

    public RiskExtendInfo setGender(String gender) {
        this.gender = gender;
        return this;
    }

    public String getMerchantType() {
        return merchantType;
    }

    public RiskExtendInfo setMerchantType(String merchantType) {
        this.merchantType = merchantType;
        return this;
    }

    public String getExtraParameters() {
        return extraParameters;
    }

    public RiskExtendInfo setExtraParameters(String extraParameters) {
        this.extraParameters = extraParameters;
        return this;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        Map<String, Object> map = mapper.convertValue(this, Map.class);
        StringJoiner stringJoiner = new StringJoiner("|", "", "|");
        map.forEach((k, v) -> {
            String temp = k + ":" + v;
            stringJoiner.add(temp);
        });
        return stringJoiner.toString();
    }
}