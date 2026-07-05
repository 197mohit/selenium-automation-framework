package com.paytm.framework.reportportal.service.dto;

import com.paytm.framework.reportportal.api.StartLaunchRequest;

public class LaunchInfo {
    private final static LaunchInfo launchInfo = new LaunchInfo();
    private LaunchInfo(){}

    private String launchId;
    private String launchStatus;
    private StartLaunchRequest launchRequest;
    private ThreadLocal<String> itemUuid = new ThreadLocal(){
        @Override
        protected Object initialValue() {
            return "";
        }
    };

    public static LaunchInfo getInstance(){
        return launchInfo;
    }

    public String getItemUuid(){
        return this.itemUuid.get();
    }

    public LaunchInfo setItemUuid(String itemUuid){
        this.itemUuid.set(itemUuid);
        return this;
    }

    public String getLaunchId() {
        return launchId;
    }

    public LaunchInfo setLaunchId(String launchId) {
        this.launchId = launchId;
        return this;
    }

    public String getLaunchStatus() {
        return launchStatus;
    }

    public LaunchInfo setLaunchStatus(String launchStatus) {
        this.launchStatus = launchStatus;
        return this;
    }

    public StartLaunchRequest getLaunchRequest() {
        return launchRequest;
    }

    public LaunchInfo setLaunchRequest(StartLaunchRequest launchRequest) {
        this.launchRequest = launchRequest;
        return this;
    }
}
