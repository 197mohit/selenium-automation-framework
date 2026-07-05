package com.paytm.framework.utils;

import io.lettuce.core.RedisURI;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RedisClusterUtil {

    private static Map<String, RedisClusterUtil> instanceMap = new HashMap<>();
    private Map<String, StatefulRedisClusterConnection<String, String>> connectionMap = new HashMap<>();
    private String redisUrl, redisPass;

    private RedisClusterUtil(String redisUrl, String redispass) {
        this.redisUrl = redisUrl;
        this.redisPass = redispass;
    }

    public static synchronized RedisClusterUtil getInstance(String redisUrl, String redispass) {
        if (!instanceMap.containsKey(redisUrl)) {
            instanceMap.put(redisUrl, new RedisClusterUtil(redisUrl, redispass));
        }
        return instanceMap.get(redisUrl);
    }

    public StatefulRedisClusterConnection<String, String> getConnection() {
        String[] schemeSplit = redisUrl.split("://");
        if (schemeSplit.length != 2) {
            throw new IllegalArgumentException(redisUrl + " is not a valid Redis URI");
        }
        String host = schemeSplit[1].split(":")[0];
        String port = schemeSplit[1].split(":")[1];

        if (!connectionMap.containsKey(redisUrl)) {
            RedisURI redisUri = RedisURI.Builder.redis(host, Integer.parseInt(port)).withPassword(redisPass).build();
            RedisClusterClient redisClusterClient = RedisClusterClient.create(redisUri);
            StatefulRedisClusterConnection<String, String> connection = redisClusterClient.connect();
            connectionMap.put(redisUrl, connection);
        }

        return connectionMap.get(redisUrl);
    }

    public boolean flushAll() {
        RedisAdvancedClusterCommands<String, String> syncCommands = getConnection().sync();
        List<String> keys = syncCommands.keys("*");
        Map<String, Boolean> m = keys.stream().collect(
                Collectors.toMap(Function.identity(), Boolean::new)
        );
        m.replaceAll((k, v) -> v = false);
        int retryCount = 1;
        while (m.containsValue(false)) {
            System.out.println("Attempt: " + retryCount++);
            m.forEach((k, v) -> {
                try {
                    syncCommands.del(k);
                    m.put(k, true);
                } catch (Exception e) {
                    m.put(k, false);
                }
            });
            if (retryCount == 5)
                return false;
        }
        return true;
    }

    public RedisAdvancedClusterCommands<String, String> getSyncCommand() {
        return getConnection().sync();
    }

}
