package com.paytm.dto.PpblDTO;

public class CustomData {
    private String channelId;
    private String channelName;
    private String channelType;
    private String channelTransactionId;

    public CustomData(String channelTransactionId) {
        setChannelTransactionId(channelTransactionId);
        setChannelId("APP");
        setChannelType("Add Money to Casa");
        setChannelName("SLFD");
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getChannelTransactionId() {
        return channelTransactionId;
    }

    public void setChannelTransactionId(String channelTransactionId) {
        this.channelTransactionId = channelTransactionId;
    }
}
