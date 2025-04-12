# 🚀 Flink CDC Spring Boot Kafka Integration

This project captures **real-time change data** (CDC) from a **SQL Server** database using **Flink CDC**, processes it with **Apache Flink**, and streams the data into a **Kafka topic** — all wrapped inside a **Spring Boot** application.

---

## 📦 Features

- 🔄 **Change Data Capture** (CDC) from SQL Server
- ⚙️ Built using **Apache Flink 1.18** and **Flink SQL Server CDC 3.0**
- 🔐 Supports **Kafka SASL_SSL authentication**
- ☕ Powered by **Spring Boot** for configuration and REST support
- 🧩 Pluggable Kafka sink using `KafkaProducer` (to avoid MBean conflicts)
- 🧪 Prints captured CDC data to console for debugging

---

## 🛠️ Technologies

| Component        | Version     |
|------------------|-------------|
| Java             | 17          |
| Spring Boot      | 3.1.5       |
| Apache Flink     | 1.18.1      |
| Flink SQL Server CDC | 3.0.0   |
| Kafka Clients    | 3.2.3       |

---

## 🔧 Configuration

Edit the `application.yml` file:

```yaml
cdc:
  mysql:
    hostname: your-sqlserver-host
    port: 1433
    username: your-user
    password: your-password
    database: APIGEE
    table: APIGEE.dbo.WatchListTopic

  kafka:
    bootstrap-servers: your-kafka-bootstrap
    topic: your-kafka-topic
    client-id: container-event-notification
    schema-registry-url: your-schema-registry-url
    username: your-kafka-username
    password: your-kafka-password
    login-module: org.apache.kafka.common.security.scram.ScramLoginModule
    sasl-mechanism: SCRAM-SHA-512
    security-protocol: SASL_SSL
    consumer-group: your-group-id
    ssl-keystore-location: path/to/keystore.jks
    ssl-keystore-password: keystore-pass
    ssl-truststore-location: path/to/truststore.jks
    ssl-truststore-password: truststore-pass
