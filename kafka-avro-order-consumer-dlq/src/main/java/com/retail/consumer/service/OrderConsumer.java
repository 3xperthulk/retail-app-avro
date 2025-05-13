package com.retail.consumer.service;

import com.retail.avro.RetailTransaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    private final KafkaTemplate<String, RetailTransaction> dlqTemplate;

    @Value("${app.kafka.dlq.topic}")
    private String dlqTopic;

    public OrderConsumer(KafkaTemplate<String, RetailTransaction> dlqTemplate) {
        this.dlqTemplate = dlqTemplate;
    }

    @KafkaListener(
        topics = "${app.kafka.topic.name}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, RetailTransaction> record) {
        try {
            RetailTransaction transaction = record.value();

            // Simulated failure (example: throw if price is over 90)
            if (transaction.getPrice() > 90) {
                throw new RuntimeException("Price too high: simulated failure");
            }

            logger.info("✅ Consumed event from topic: {}", record.topic());
            logger.info("Key: {}", record.key());
            logger.info("Partition: {}, Offset: {}", record.partition(), record.offset());
            logger.info("RetailTransaction: ID={}, Amount={}, Timestamp={}",
                transaction.getTransactionId(),
                transaction.getPrice(),
                transaction.getTimestamp()
            );

        } catch (Exception ex) {
            logger.error("❌ Error processing record. Sending to DLQ: {}", ex.getMessage());

            // Forward the failed record to the DLQ
            dlqTemplate.send(dlqTopic, record.key(), record.value());
        }
    }
}