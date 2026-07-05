package com.paytm.apphelpers;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class RedisHelper {
  private static RedisHelper instance;
  private JedisCluster jedisCluster;

  public RedisHelper(String clusterUri, String password) {
    Set<HostAndPort> jedisClusterNodes = new HashSet<>();
    String[] nodes = clusterUri.replace("redis-cluster://", "").split(",");
    for (String node : nodes) {
      String[] hostPort = node.split(":");
      if (hostPort.length == 2) {
        jedisClusterNodes.add(new HostAndPort(hostPort[0], Integer.parseInt(hostPort[1])));
      } else {
        throw new IllegalArgumentException("Invalid host:port format in cluster URI: " + node);
      }
    }
    this.jedisCluster = new JedisCluster(jedisClusterNodes, 2000, 2000, 5, password, new JedisPoolConfig());
  }

  public static RedisHelper getInstance(String clusterUri, String password) {
    if (instance == null) {
      instance = new RedisHelper(clusterUri, password);
    }
    return instance;
  }

  public void delete(String key) {
   Long deleteResult= jedisCluster.del(key);
    System.out.printf("Delete result is"+ deleteResult);
  }
  public String getKeyValue(String key) {
    return jedisCluster.get(key);
  }

  public void close() {
    try {
      jedisCluster.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}