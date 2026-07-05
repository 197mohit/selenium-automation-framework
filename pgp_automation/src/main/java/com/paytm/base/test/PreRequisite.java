package com.paytm.base.test;

import com.paytm.LocalConfig;
import com.paytm.framework.utils.RedisClusterUtil;
import com.paytm.framework.utils.RedisUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author ankur2.agarwal
 */
public class PreRequisite {
    private static PreRequisite instance;

    private PreRequisite() {
        del(LocalConfig.SESSION_REDIS_URI);
//        del(LocalConfig.PG_REDIS_URI);
        RedisClusterUtil.getInstance(LocalConfig.TRANSACTIONAL_REDIS_CLUSTER_URI, LocalConfig.PG_REDIS_CLUSTER_PASS).flushAll();
        RedisClusterUtil.getInstance(LocalConfig.PG_REDIS_CLUSTER_URI, LocalConfig.PG_REDIS_CLUSTER_PASS).flushAll();
    }

    private void del(String redisURL) {
        Jedis jedis = RedisUtil.getInstance().getConnection(redisURL);
        Set<String> keySet = jedis.keys("*");
        Map<String, Boolean> m = keySet.stream().collect(Collectors.toMap(Function.identity(), Boolean::new));
        m.replaceAll((k, v) -> v = false);
        int count = 1;
        while (m.values().contains(false)) {
            System.out.println("Attempt: " + count++);
            m.forEach((k, val) -> {
                if (val.equals(false)) {
                    try {
                        jedis.del(k);
                        m.put(k, true);
                    } catch (JedisConnectionException ex) {
                        m.put(k, false);
                    }
                }
            });
            if (count == 5)
                break;
        }
    }

    public static PreRequisite execute() {
        if (instance == null) {
            instance = new PreRequisite();
        }
        return instance;
    }
}
