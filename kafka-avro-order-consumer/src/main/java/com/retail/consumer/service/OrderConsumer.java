package com.retail.consumer.service;

import com.retail.avro.RetailTransaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    @KafkaListener(
        topics = "${app.kafka.topic.name}",
        groupId = "${spring.kafka.consumer.group-id}"
    )
    public void consume(ConsumerRecord<String, RetailTransaction> record) {
        RetailTransaction transaction = record.value();
        logger.info("Consumed event from topic: {}", record.topic());
        logger.info("Key: {}", record.key());
        logger.info("Partition: {}, Offset: {}", record.partition(), record.offset());
        logger.info("RetailTransaction: ID={}, Amount={}, Timestamp={}",
            transaction.getTransactionId(),
            transaction.getPrice(),
            transaction.getTimestamp()
        );
    }
}