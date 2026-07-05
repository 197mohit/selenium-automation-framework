package com.paytm.utils.ff4j;

import com.paytm.LocalConfig;
import org.ff4j.FF4j;
import org.ff4j.cache.InMemoryCacheManager;
import org.ff4j.web.jersey2.store.FeatureStoreHttp;

public class FF4JConfig {

    private static final String FEATURE_API ="/api/ff4j";
    private static final String FF4J_WEB ="/ff4j-web";

    public FF4j getFF4j() {
        // Default constructor
        FF4j ff4j = new FF4j();
        String url = LocalConfig.PGP_HOST + FF4J_WEB + FEATURE_API;
        ff4j.setFeatureStore(new FeatureStoreHttp(url));
        // Enable Cache Proxy
        ff4j.cache(new InMemoryCacheManager());
        ff4j.audit();
        return ff4j;
    }

}