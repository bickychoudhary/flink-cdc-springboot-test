package com.example.cdc;

import com.example.cdc.config.AppProperties;
import com.ververica.cdc.connectors.sqlserver.SqlServerSource;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.PrintSinkFunction;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class FlinkJobRunner {

    private final AppProperties config;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public FlinkJobRunner(AppProperties config) {
        this.config = config;
    }

    public String startJob() {
        if (isRunning.get()) return "Job already running";

        new Thread(() -> {
            try {
                StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

                SourceFunction<String> sourceFunction = SqlServerSource.<String>builder()
                        .hostname(config.getMysql().getHostname())
                        .port(config.getMysql().getPort())
                        .username(config.getMysql().getUsername())
                        .password(config.getMysql().getPassword())
                        .database("APIGEE")
                        .tableList("dbo.WatchListTopic")
                        .deserializer(new com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema())
                        .build();

                DataStream<String> stream = env.addSource(sourceFunction)
                        .name("CDC Source")
                        .uid("cdc-source");

                stream.addSink(new PrintSinkFunction<>());

                stream.addSink(new SimpleKafkaSink(
                        config.getKafka().getBootstrapServers(),
                        config.getKafka().getTopic(),
                        config.getKafka().getSecurityProtocol(),
                        config.getKafka().getSaslMechanism(),
                        config.getKafka().getLoginModule(),
                        config.getKafka().getUsername(),
                        config.getKafka().getPassword()
                )).name("Custom Kafka Sink").uid("kafka-sink");

                isRunning.set(true);
                env.execute("CDC to Kafka Job");

            } catch (Exception e) {
                e.printStackTrace();
                isRunning.set(false);
            }
        }).start();

        return "Job started";
    }

    public String stopJob() {
        isRunning.set(false);
        return "Job stop requested (manual shutdown required)";
    }

    // ✅ Final version of custom Kafka Sink
    public static class SimpleKafkaSink extends RichSinkFunction<String> {

        private final String bootstrapServers;
        private final String topic;
        private final String securityProtocol;
        private final String saslMechanism;
        private final String loginModule;
        private final String username;
        private final String password;

        private transient KafkaProducer<String, String> producer;

        public SimpleKafkaSink(String bootstrapServers, String topic, String securityProtocol,
                               String saslMechanism, String loginModule,
                               String username, String password) {
            this.bootstrapServers = bootstrapServers;
            this.topic = topic;
            this.securityProtocol = securityProtocol;
            this.saslMechanism = saslMechanism;
            this.loginModule = loginModule;
            this.username = username;
            this.password = password;
        }

        @Override
        public void open(Configuration parameters) {
            Properties props = new Properties();
            props.setProperty("bootstrap.servers", bootstrapServers);
            props.setProperty("client.id", "flink-producer-" + getRuntimeContext().getIndexOfThisSubtask());
            props.setProperty("enable.idempotence", "true");
            props.setProperty("acks", "all");
            props.setProperty("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.setProperty("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
            props.setProperty("register.mbeans", "false");
            props.setProperty("security.protocol", securityProtocol);
            props.setProperty("sasl.mechanism", saslMechanism);
            props.setProperty("sasl.jaas.config", String.format(
                    "%s required username=\"%s\" password=\"%s\";",
                    loginModule, username, password
            ));

            this.producer = new KafkaProducer<>(props);
        }

        @Override
        public void invoke(String value, Context context) {
            producer.send(new ProducerRecord<>(topic, value));
        }

        @Override
        public void close() {
            if (producer != null) {
                producer.flush();
                producer.close();
            }
        }
    }
}
