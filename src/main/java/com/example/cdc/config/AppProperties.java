
package com.example.cdc.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cdc")
public class AppProperties {

    private final Mysql mysql = new Mysql();
    private final Kafka kafka = new Kafka();

    public Mysql getMysql() { return mysql; }
    public Kafka getKafka() { return kafka; }
    @Data
    public static class Mysql {
        private String hostname;
        private int port;
        private String username;
        private String password;
        private String database;
        private String table;
        // Getters and setters omitted for brevity
    }
    @Data
    public static class Kafka {
        private String bootstrapServers;
        private String topic;
        private String clientId;
        private String schemaRegistryUrl;
        private String username;
        private String password;
        private String loginModule;
        private String saslMechanism;
        private String securityProtocol;
        private String consumerGroup;
        // Getters and setters omitted for brevity
    }
}
