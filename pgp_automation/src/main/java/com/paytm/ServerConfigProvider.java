package com.paytm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.datareader.DataReaderUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ServerConfigProvider {

    private static Map<String, Map<String, String>> SERVER_MAP;
    static {
        SERVER_MAP = new HashMap<>();
        loadIp();
    }

    public static enum SERVICE {
        THEIA_FACADE,
        THEIA_PRIMARY,
        MAPPING_SERVICE,
        INSTAPROXY,
        MERCHANT_STATUS,
        PG_PROXY_NOTIFICATION,
        SUBSCRIPTION,
        BILLPROXY,
        SAVEDCARDSERVICE,
        NOTIFICATION_QUEUE_HANDLER,
        COMMUNICATION_GATEWAY,
        LINKSERVICE,
        LINKSERVICE_EVENT,
        LINK_EXCHANGE,
        REFUND_SERVICE,
        WEB_SOCKET_CLUSTER,
        ADMIN_PANEL,
        PAYMENTPOSTPROCESSOR,
        PAYMENT_OPTION_FACADE,
        SAVEDCARDSERVICE_FACADE,
        PAYMENT_OPTION,
        UPI_PSP_PROCESSOR,
        REFUND_SERVICE_LOGS,
        DOWN_STEAM_REQUEST_RESPONSE,
        THEIA_REQ_RESP,
        REFUND_FACADE_LOGS

    }

    public static Map<String, String> getServerDetail(SERVICE service, String... s){
        return SERVER_MAP.getOrDefault(service.name(), Collections.emptyMap());
    }

    public static ServerDetails getServerDetail(SERVICE service){
        Map<String, String> map = getServerDetail(service, "");
        if(!map.isEmpty()) {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(map, ServerDetails.class);
        }
        return new ServerDetails();
    }


    private static void loadIp(){
        Map<String, Object> map = DataReaderUtil.readYML("envServerCred.yaml");
        for (String key : map.keySet()) {
            Map<String, String> serverDetails = new HashMap<>();
            ArrayList<Map> list = (ArrayList) map.get(key);
            for (Map tempMap : list) {
                serverDetails.putAll(tempMap);
            }
            SERVER_MAP.put(key, serverDetails);
        }
    }

}
