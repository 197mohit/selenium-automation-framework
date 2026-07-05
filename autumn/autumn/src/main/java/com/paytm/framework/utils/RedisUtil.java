package com.paytm.framework.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paytm.framework.reporting.reports.Report;
import redis.clients.jedis.*;

import java.util.*;

public class RedisUtil {

    private static RedisUtil redisUtil;
    private static Map<String, Jedis> redisConnectionMap = new HashMap<>();
    private final Report report = com.paytm.framework.reporting.Reporter.report;

    private RedisUtil() {
    }

    public static synchronized RedisUtil getInstance() {
        if (redisUtil == null) {
            redisUtil = new RedisUtil();
        }
        return redisUtil;
    }

    public Jedis getConnection(String redisURI) {

        boolean isSentinelEnabled = false;

        if (redisConnectionMap.containsKey(redisURI)) {
            Jedis jedis = redisConnectionMap.get(redisURI);
            if (!jedis.isConnected()) {
                return jedis;
            } else {
                redisConnectionMap.remove(redisURI);
            }
        }


        this.report.info("Redis URI Detected as : " + redisURI);
        String[] schemeSplit = redisURI.split("://");
        if (schemeSplit.length != 2) {
            throw new IllegalArgumentException(redisURI + " is not a valid Redis URI");
        }
        if (schemeSplit[0].equals("redis-sentinel")) {
            isSentinelEnabled = true;
        } else if (!schemeSplit[0].equals("redis")) {
            throw new IllegalArgumentException(redisURI + " is not a valid Redis URI");
        }
        this.report.info("Using is Sentinel Enabled as : " + isSentinelEnabled);
        String[] clusterSplit = schemeSplit[1].split("#");
        if (clusterSplit.length > 2) {
            throw new IllegalArgumentException(redisURI + " is not a valid Redis URI");
        }

        String clusterName = null;
        if (clusterSplit.length == 2) {
            clusterName = clusterSplit[1];
        }
        this.report.info("Using Redis Cluster name as : " + clusterName);
        String[] addresses = clusterSplit[0].split("/")[0].split(",");
        Jedis jedis;
        if (isSentinelEnabled) {
            Set<String> sentinels = new HashSet<String>(Arrays.asList(addresses));
            jedis = new JedisSentinelPool(clusterName, sentinels, getJedisPoolConfig(), null).getResource();
        } else {
            jedis = new JedisPool(getJedisPoolConfig(), addresses[0].split(":")[0], Integer.valueOf(addresses[0]
                    .split(":")[1]), Protocol.DEFAULT_TIMEOUT, null,
                    0).getResource();
        }

        redisConnectionMap.put(redisURI, jedis);
        return jedis;
    }

    private JedisPoolConfig getJedisPoolConfig() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(500);
        poolConfig.setMaxIdle(100);
        poolConfig.setMinIdle(10);
        poolConfig.setMaxWaitMillis(1000);
        poolConfig.setFairness(true);
        poolConfig.setBlockWhenExhausted(false);
        poolConfig.setTestOnCreate(true);
        return poolConfig;
    }

    private void close(Jedis jedis) {
        if (jedis != null) {
            jedis.close();
        }
    }

    public void set(String key, Object value) {
        set(key, value);
    }

    public String getString(String redisURI, String key) {
        Jedis jedis = getConnection(redisURI);
        String value = jedis.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    public String getJsonString(String redisURI, String key) {
        ObjectMapper mapper = new ObjectMapper();
        String value = getString(redisURI, key);
        if (null == value) {
            return null;
        } else {
            try {
                return mapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                this.report.info(e.toString());
                return null;
            }
        }
    }

    public void flushAll(String redisURI) {
        Jedis connection = getConnection(redisURI);
        connection.flushAll();
    }

    public void delete(String redisURI, String... keys) {
        Jedis jedis = null;
        jedis = getConnection(redisURI);
        jedis.del(keys);
    }


    public void closeAllConnections() {
        Set<String> keys = redisConnectionMap.keySet();
        for (String key : keys) {
            Jedis connection = redisConnectionMap.get(key);
            if (!connection.isConnected()) {
                connection.close();
                connection.disconnect();
            }
            redisConnectionMap.remove(key);
        }
    }

    public boolean validateCache(String rediUrl, String key){
        Jedis jedis = getConnection(rediUrl);
        report.info("Cache Validation for key "+ key);
        Set<String> set = jedis.keys(key);
        if(set.isEmpty()) {
            return false;
        } else
            return true;
    }

    public byte[] get(String redisUrl, byte[] key) {
        Jedis jedis = null;
        byte[] value;
        try {
            jedis = getConnection(redisUrl);
            value = jedis.get(key);
            if(value == null || value.length <= 0) {
                return null;
            }
        } finally {
            this.close(jedis);
        }

        return value;
    }
}