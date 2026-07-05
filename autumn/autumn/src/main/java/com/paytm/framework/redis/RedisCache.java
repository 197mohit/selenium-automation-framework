package com.paytm.framework.redis;

import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.stream.Collectors;

public class RedisCache implements Set<RedisCacheEntry<String, Object>> {
    private final Jedis jedis;

    public RedisCache(String host, int port) {
        this.jedis = new Jedis(host, port);
    }

    public boolean containsKey(Object key) {
        return this.jedis.exists(key.toString());
    }

    public boolean containsValue(Object value) {
        return this.values().contains(value);
    }

    public Object get(Object key) {
        return this.jedis.get(key.toString());
    }

    public Object put(String key, Object value) {
        return this.put(key, value.toString());
    }

    public Object put(String key, String value) {
        this.jedis.del(key);
        return "OK".equals(this.jedis.set(key, value)) ? value : null;
    }

    public Object put(String key, List<String> value) {
        this.jedis.del(key);
        return this.jedis.rpush(key, value.toArray(new String[0])) == value.size() ? value : null;
    }

    public Object put(String key, Set<String> value) {
        this.jedis.del(key);
        return this.jedis.sadd(key, value.toArray(new String[0])) == value.size() ? value : null;
    }

    public Object put(String key, SortedMap<String, Double> value) {
        this.jedis.del(key);
        return this.jedis.zadd(key, value) == value.size() ? value : null;
    }

    public Object put(String key, HashMap<String, String> value) {
        this.jedis.del(key);
        return "OK".equals(this.jedis.hmset(key, value)) ? value : null;
    }

    public void putAll(Map<? extends String, ?> m) {
        m.forEach(this::put);
    }

    public Set<String> keySet() {
        return this.jedis.keys("*");
    }

    public Collection<Object> values() {
        return this.entrySet().stream().map(RedisCacheEntry::getValue).collect(Collectors.toList());
    }

    public Set<RedisCacheEntry<String, Object>> entrySet() {
        return this.jedis.keys("*").stream()
                .map(s -> new RedisCacheEntry<String, Object>(s, this.jedis))
                .collect(Collectors.toSet());
    }

    @Override
    public int size() {
        return this.jedis.dbSize().intValue();
    }

    @Override
    public boolean isEmpty() {
        return this.jedis.dbSize() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return this.jedis.exists(o.toString());
    }

    @Override
    public Iterator<RedisCacheEntry<String, Object>> iterator() {
        //implement using scan in future for performance
        return this.jedis.keys("*").stream()
                .map(s -> new RedisCacheEntry<String, Object>(s, this.jedis))
                .collect(Collectors.toSet())
                .iterator();
    }

    @Override
    public Object[] toArray() {
        return this.toArray(new Object[0]);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        List list = new ArrayList();
        this.iterator().forEachRemaining(list::add);
        return (T[]) list.toArray(a);
    }

    @Override
    public boolean add(RedisCacheEntry<String, Object> entry) {
        return this.put(entry.getKey(), entry.getValue()) != null;
    }

    @Override
    public boolean remove(Object o) {
        return this.jedis.del(o.toString()) != 0;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(this::contains);
    }

    @Override
    public boolean addAll(Collection<? extends RedisCacheEntry<String, Object>> c) {
        return c.stream().allMatch(this::add);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return this.keySet().stream().filter(key -> !c.contains(key)).allMatch(this::remove);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return c.stream().allMatch(this::remove);
    }

    @Override
    public void clear() {
        this.jedis.flushDB();
    }
}
