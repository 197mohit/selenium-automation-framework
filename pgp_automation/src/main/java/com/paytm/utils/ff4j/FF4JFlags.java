package com.paytm.utils.ff4j;

import com.paytm.appconstants.FF4JFeatures;
import com.paytm.base.test.User;
import com.paytm.framework.reporting.Reporter;
import org.apache.commons.lang.StringUtils;

//All FF4j Flag details should be listed in this class
public class FF4JFlags {

    private static FF4JClient ff4JClient = new FF4JClientImpl();

    public static void enable(String feature)
    {
        ff4JClient.enable(feature);
    }

    public static void enableMidBased(String feature, String mid){
        ff4JClient.enableMidBased(feature, mid);
    }

    public static void disable(String feature)
    {
        ff4JClient.disable(feature);
    }

    public static void disableMidBased(String feature, String mid) {
        ff4JClient.disableMidBased(feature, mid);
    }

    public static Boolean getFeatureValue(String featureName)
    {
        return ff4JClient.check(featureName);
    }
    public boolean fetchSavedCardFromPlatform(User user , String custId) {
        String userId = user.custId();
        boolean isFeatureEnabledOnMidCustId = false;
        boolean isFeatureEnabledOnUserId = false;
        if (StringUtils.isNotEmpty(custId)){
            isFeatureEnabledOnMidCustId = ff4JClient.check(FF4JFeatures.FETCH_SAVED_CARD_FROM_PLATFORM_FOR_MID_CUSTID);
        }
        if (!isFeatureEnabledOnMidCustId && StringUtils.isNotEmpty(userId)) {
            isFeatureEnabledOnUserId = ff4JClient.check(FF4JFeatures.FETCH_SAVED_CARD_FROM_PLATFORM_FOR_USERID);

        }
        return isFeatureEnabledOnMidCustId || isFeatureEnabledOnUserId;
    }

    public boolean saveCardAtPlatformOnUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        boolean isFeatureEnabledOnUserId = ff4JClient
                .check(FF4JFeatures.SAVE_CARD_AT_PLATFORM_ON_USERID);
        return isFeatureEnabledOnUserId;
    }

    public boolean saveCardAtPlatformOnMidCustId(String mid, String custId) {
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(custId)) {
            return false;
        }
        boolean isFeatureEnabledOnMidCustId = ff4JClient.check(FF4JFeatures.SAVE_CARD_AT_PLATFORM_ON_MID_CUSTID);
        return isFeatureEnabledOnMidCustId;
    }

    public boolean fetchSavedCardFromService(String userId, String mid, String custId) {
        return fetchSavedCardFromServiceOnUserId(userId) || fetchSavedCardFromServiceOnMidCustid(mid, custId);
    }

    public boolean fetchSavedCardFromServiceOnUserId(String userId) {
        if (StringUtils.isBlank(userId) || null==userId) {
            return false;
        }

        boolean isFeatureDisableddOnUserId = ff4JClient.check(FF4JFeatures.SHORT_CIRCUIT_SAVED_CARD_SERVICE_READ_FOR_USERID);
        Reporter.report.info("shortCircuitSavedCardServiceReadForUserId flag is " + isFeatureDisableddOnUserId + " on user id " +userId);
        return !isFeatureDisableddOnUserId;
    }

    public boolean fetchSavedCardFromServiceOnMidCustid(String mid, String custId) {

        if (StringUtils.isBlank(mid) || StringUtils.isBlank(custId)) {
            return false;
        }

        boolean isFeatureDisabledOnMidCustId = ff4JClient.check(FF4JFeatures.SHORT_CIRCUIT_SAVED_CARD_SERVICE_READ_FOR_MID_CUSTID);
        Reporter.report.info("shortCircuitSavedCardServiceReadForMidCustId flag is " + isFeatureDisabledOnMidCustId + " on mid " + mid + "custid " +custId);
        return !isFeatureDisabledOnMidCustId;
    }
    public boolean returnSavedCardsFromPlatform(String userId, String mid, String custId) {

        return returnSavedCardsFromPlatformForMidCustId(mid, custId) || returnSavedCardsFromPlatformForUserId(userId);
    }

    public boolean returnSavedCardsFromPlatformForMidCustId(String mid, String custId) {
        if (StringUtils.isBlank(mid) || StringUtils.isBlank(custId)) {
            return false;

        }

        return ff4JClient.check(FF4JFeatures.RETURN_SAVED_CARDS_FROM_PLATFORM_FOR_MIDCUSTID);
    }

    public boolean returnSavedCardsFromPlatformForUserId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return false;
        }
        return ff4JClient.check(FF4JFeatures.RETURN_SAVED_CARDS_FROM_PLATFORM_FOR_USERID);

    }

}

