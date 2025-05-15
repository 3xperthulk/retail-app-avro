package com.retail.producer.config;

import com.retail.avro.RetailTransaction;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Gauge;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.*;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaProducerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;

    @Value("${spring.kafka.producer.linger-ms}")
    private int lingerMs;

    @Value("${spring.kafka.producer.batch-size}")
    private int batchSize;

    @Value("${spring.kafka.producer.acks}")
    private String acksConfig;

    @Value("${spring.kafka.producer.retries}")
    private int retries;

    private final MeterRegistry meterRegistry;

    public KafkaProducerConfig(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Bean
    public ProducerFactory<String, RetailTransaction> producerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put("schema.registry.url", schemaRegistryUrl);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
        config.put(ProducerConfig.LINGER_MS_CONFIG, lingerMs);
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, batchSize);
        config.put(ProducerConfig.ACKS_CONFIG, acksConfig);
        config.put(ProducerConfig.RETRIES_CONFIG, retries);

        // Expose Kafka Producer metrics
        exposeKafkaProducerMetrics(config);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, RetailTransaction> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    private void exposeKafkaProducerMetrics(Map<String, Object> config) {
        // You can expose any custom metrics here related to Kafka Producer
        // For example, you can bind Kafka metrics like record.send.total to Micrometer
        // Or other Kafka producer-related metrics

        // Example: Exposing record send success as a counter
        Gauge.builder("kafka.producer.send.success", this, obj -> {
            // Replace with an actual metric from Kafka producer (success count)
            return 0; // example value, replace with actual metric retrieval logic
        })
        .tags("application", "kafka-order-producer")
        .register(meterRegistry);

        // Example: Exposing record send failure as a counter
        Gauge.builder("kafka.producer.send.failure", this, obj -> {
            // Replace with an actual metric from Kafka producer (failure count)
            return 0; // example value, replace with actual metric retrieval logic
        })
        .tags("application", "kafka-order-producer")
        .register(meterRegistry);

        // Exposing a custom metric for Kafka Producer request latency
        Gauge.builder("kafka.producer.latency", this, obj -> {
            // Replace with actual logic to capture producer latency
            return 0.0; // example value, replace with actual metric retrieval logic
        })
        .tags("application", "kafka-order-producer")
        .register(meterRegistry);
    }
}