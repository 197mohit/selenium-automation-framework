package com.paytm.framework.redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import java.util.*;
import java.util.stream.Collectors;

public class RedisCacheEntry<S, O> implements Map.Entry<String, Object> {
    private final Jedis jedis;
    private String key;
    private final String value;

    public RedisCacheEntry(String key, String value) {
        this(key, value, null);
    }

    RedisCacheEntry(String key, Jedis jedis) {
        this(key, null, jedis);
    }


    private RedisCacheEntry(String key, String value, Jedis jedis) {
        this.key = key;
        this.value = value;
        this.jedis = jedis;
    }

    public String getKey() {
        return this.key;
    }

    public Object getValue() {
        if (this.value != null) {
            return this.value;
        }
        switch (this.jedis.type(this.key)) {
            case "string":
                return this.jedis.get(this.key);
            case "list":
                return this.jedis.lrange(this.key, 0, -1);
            case "set":
                return this.jedis.smembers(this.key);
            case "zset":
                Map<String, Double> map = this.jedis.zrangeWithScores(this.key, 0, -1)
                        .stream()
                        .collect(Collectors.toMap(Tuple::getElement, Tuple::getScore));
                return new TreeMap<>(map);
            case "hash":
                return this.jedis.hgetAll(this.key);
            default:
                throw new IllegalArgumentException();
        }
    }

    public boolean setKey(String key) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        if (this.jedis.renamenx(this.key, key) == 1) {
            this.key = key;
            return true;
        } else return false;
    }

    public Object setValue(Object value) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return this.setValue(value.toString());
    }

    public Object setValue(String value) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return "OK".equals(this.jedis.set(key, value)) ? value : null;
    }

    public Object setValue(HashMap<String, String> value) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return "OK".equals(this.jedis.hmset(key, value)) ? value : null;
    }

    public Object setValue(List<String> value) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return this.jedis.rpush(key, value.toArray(new String[0])) == value.size() ? value : null;
    }

    public Object setValue(Set<String> value) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return this.jedis.sadd(key, value.toArray(new String[0])) == value.size() ? value : null;
    }

    public Object setValue(SortedMap<String, Double> value) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return this.jedis.zadd(key, value) == value.size() ? value : null;
    }

    public boolean setTimeout(long milliseconds) {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        if (milliseconds <= 0) throw new IllegalArgumentException("value should be greater than 0");
        return this.jedis.pexpire(this.key, milliseconds) == 1;
    }

    public long getTimeout() {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return this.jedis.pttl(this.key);
    }

    public String getType() {
        if (this.value != null) {
            throw new UnsupportedOperationException();
        }
        return this.jedis.type(this.key);
    }

    @Override
    public String toString() {
        return new StringBuilder()
                .append("key")
                .append("(")
                .append("name")
                .append(" = ")
                .append(this.getKey())
                .append(", ")
                .append("value")
                .append(" = ")
                .append(this.getValue())
                .append(")")
                .toString();
    }

    public interface Type {
        String STRING = "string";
        String LIST = "list";
        String SET = "set";
        String SORTED_SET = "zset";
        String HASHMAP = "hash";
    }
}
