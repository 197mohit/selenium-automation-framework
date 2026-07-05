package com.paytm.utils.ff4j;

import com.paytm.LocalConfig;
import com.paytm.apphelpers.RedisHelper;
import com.paytm.pgplus.pgpff4jstrategy.MidBasedStrategy;
import org.ff4j.FF4j;
import org.ff4j.core.Feature;
import org.awaitility.Awaitility;

import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.TimeUnit;

public class FF4JClientImpl implements FF4JClient {

    FF4j ff4j;

    public FF4JClientImpl() {
        FF4JConfig ff4JConfig = new FF4JConfig();
        ff4j = ff4JConfig.getFF4j();
    }

    @Override
    public boolean check(String featureID) {
        return this.ff4j.check(featureID);
    }

    /**
     * Helper method to clear Redis cache for FF4J flag
     */
    private void clearFF4JRedisCache(String flagName) {
        String redisKey = "FF4J_FEATURE_" + flagName;
        try {
            // Use RedisHelper to delete the key from transactional Redis cluster
            RedisHelper redisHelper = RedisHelper.getInstance(
                LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI, 
                LocalConfig.PG_REDIS_CLUSTER_PASS
            );
            redisHelper.delete(redisKey);
            System.out.println("Cleared Redis cache for FF4J flag: " + redisKey);
        } catch (Exception e) {
            System.out.println("Warning: Failed to clear Redis cache for flag " + flagName + ": " + e.getMessage());
        }
    }

    /**
     * Helper method to wait for FF4J flag to be properly updated
     */
    private void waitForFF4JFlagUpdate(String flagName, boolean expectedValue) {
        try {
            Awaitility.await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(2, TimeUnit.SECONDS)
                .until(() -> {
                    boolean actualValue = ff4j.getFeatureStore().read(flagName).isEnable();
                    System.out.println("FF4J Flag " + flagName + " current value: " + actualValue + ", expected: " + expectedValue);
                    return actualValue == expectedValue;
                });
        } catch (Exception e) {
            System.out.println("Timeout waiting for FF4J flag " + flagName + " to update. Current value: " + this.ff4j.check(flagName));
        }
    }

    @Override
    public void enable(String featureId) {
        this.ff4j.enable(featureId);
        // Clear Redis cache to ensure fresh value is fetched
        clearFF4JRedisCache(featureId);
        // Wait for flag update to propagate
        waitForFF4JFlagUpdate(featureId, true);
    }

    @Override
    public void enableMidBased(String featureId, String mid) {
        Feature feature = this.ff4j.getFeature(featureId);
        feature.setEnable(true);
        MidBasedStrategy midBasedStrategy = (MidBasedStrategy) feature.getFlippingStrategy();
        String existingMids = midBasedStrategy.getInitParams().get("grantedMids");
        if (!existingMids.contains(mid)) {
            String[] midArr = existingMids.split(",");
            StringJoiner target = new StringJoiner(",", "" ,"");
            for(String m : midArr)
                target.add(m);
            target.add(mid);
            midBasedStrategy.getInitParams().put("grantedMids", target.toString());
            midBasedStrategy.init(featureId, midBasedStrategy.getInitParams());
            feature.setFlippingStrategy(midBasedStrategy);
        }
        this.ff4j.getConcreteFeatureStore().update(feature);
    }

    @Override
    public void disable(String featureId) {
        this.ff4j.disable(featureId);
        // Clear Redis cache to ensure fresh value is fetched
        clearFF4JRedisCache(featureId);
        // Wait for flag update to propagate
        waitForFF4JFlagUpdate(featureId, false);
    }

    @Override
    public void disableMidBased(String featureId, String mid) {
        Feature feature = this.ff4j.getFeature(featureId);
        feature.setEnable(true);
        MidBasedStrategy midBasedStrategy = (MidBasedStrategy) feature.getFlippingStrategy();
        String existingMid = midBasedStrategy.getInitParams().get("grantedMids");
        if (existingMid.contains(mid)) {
            String[] midArr = existingMid.split(",");
            StringJoiner target = new StringJoiner(",", "" ,"");
            for(String m : midArr) {
                if(!m.equalsIgnoreCase(mid))
                    target.add(m);
            }
            midBasedStrategy.getInitParams().put("grantedMids", target.toString());
            midBasedStrategy.init(featureId, midBasedStrategy.getInitParams());
            feature.setFlippingStrategy(midBasedStrategy);
        }
        this.ff4j.getConcreteFeatureStore().update(feature);
    }

}


