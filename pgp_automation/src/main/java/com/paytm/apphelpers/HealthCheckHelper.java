package com.paytm.apphelpers;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DescribeClusterResult;
import redis.clients.jedis.Jedis;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.common.Node;

public class HealthCheckHelper {

    public static String kafkahealthStatus(String kafkaServer) {
        String bootstrapServers = kafkaServer;
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        try (AdminClient adminClient = AdminClient.create(props)) {
            DescribeClusterResult clusterInfo = adminClient.describeCluster();

            Collection<Node> brokers = clusterInfo.nodes().get();

            // Check if the list of brokers is empty
            if (brokers.isEmpty()) {
                System.out.println("No Kafka brokers found. Kafka might be down.");
                return "Kafka is down";
            }
            brokers.forEach(node ->
                    System.out.println("Kafka Broker: " + node.host() + ":" + node.port())
            );
            System.out.println("Kafka is up and running!");
            return "Kafka is up and running!";
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("Failed to connect to Kafka: " + e.getMessage());
            return "Failed to connect to Kafka";
        }
    }

    public static String checkZookeeperHealth(String zookeeperServer) {
        String zookeeperStatus = "Zookeeper is not responding correctly";
        try (Socket socket = new Socket(zookeeperServer, 2181)) { // Zookeeper default port is 2181
            socket.getOutputStream().write("ruok\r\n".getBytes());
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = reader.readLine();
            if ("imok".equals(response)) {
                System.out.println("Zookeeper is up and running!");
                return "Zookeeper is up and running!";
            } else {
                return zookeeperStatus;
            }
        } catch (Exception e) {
            System.err.println("Failed to connect to Zookeeper: " + e.getMessage());
            return "Failed to connect to Zookeeper";
        }
    }


    public static String RedisHealthCheckMultiplePorts(List<String> RedisipAndPorts) {
        List<String> ipAndPorts = RedisipAndPorts;
        String redisPassword ="redispass";
        StringBuilder result = new StringBuilder();

        for (String ipAndPort : ipAndPorts) {
            String[] parts = ipAndPort.split(":");
            String redisHost = parts[0];
            int port = Integer.parseInt(parts[1]);

            try (Jedis jedis = new Jedis(redisHost, port)) {
                jedis.auth(redisPassword);
                // Ping Redis to check if it's running
                String response = jedis.ping();
                if ("PONG".equals(response)) {
                    System.out.println("Redis is running on " + redisHost + ":" + port);
                    result.append("Redis is running on ").append(redisHost).append(":").append(port).append("\n");
                } else {
                    System.out.println("Redis is not responding on " + redisHost + ":" + port);
                    result.append("Redis is not responding on ").append(redisHost).append(":").append(port).append("\n");
                }
            } catch (Exception e) {
                System.err.println("Failed to connect to Redis on " + redisHost + ":" + port + " - " + e.getMessage());
                result.append("Failed to connect to Redis on ").append(redisHost).append(":").append(port)
                        .append(" - ").append(e.getMessage()).append("\n");
            }
        }
        return result.length() > 0 ? result.toString() : "Redis health check completed with no issues";
    }

    public static List<String> getIpAndPorts(String redisUri){
        List<String> ipAndPorts = new ArrayList<>();
        String prefix = "redis://";
        if (redisUri.startsWith(prefix)) {
            String[] parts = redisUri.substring(prefix.length()).split(",");
            for (String part : parts) {
                ipAndPorts.add(part);
            }
        }
        return ipAndPorts;
    }
}
