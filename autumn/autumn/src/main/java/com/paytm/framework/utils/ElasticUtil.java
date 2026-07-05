package com.paytm.framework.utils;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class ElasticUtil {
    private static ElasticUtil elasticUtil;
    public static ElasticUtil getInstance() {
        if (elasticUtil == null) {
            return new ElasticUtil();
        }
        return elasticUtil;
    }

    private static Map<String, TransportClient> elasticConnectionMap = new HashMap<>();

    public TransportClient getClient(String elasticUri) throws UnknownHostException {
        String[] schemeSplit = elasticUri.split(":");
        Settings settings = Settings.builder().put("cluster.name", schemeSplit[2]).build();
        TransportClient client = new PreBuiltTransportClient(settings);
        client.addTransportAddress(new TransportAddress(InetAddress.getByName(schemeSplit[0]), Integer.parseInt(schemeSplit[1])));

        return client;

    }

}
