package com.paytm.utils.ff4j;

public interface FF4JClient {

    boolean check(String var1);

    void enable(String featureId);

    void enableMidBased(String featureId, String mid);

    void disable(String featureId);

    void disableMidBased(String featureId, String mid);

}

