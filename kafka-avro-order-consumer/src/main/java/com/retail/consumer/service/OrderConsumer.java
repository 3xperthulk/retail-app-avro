package com.retail.consumer.service;

import com.retail.avro.RetailTransaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class OrderConsumer {

    @KafkaListener(
        topics = "${app.kafka.topic.name}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, RetailTransaction> record) {
        RetailTransaction transaction = record.value();
        System.out.println("Received Transaction: " + transaction);
        
        // TODO: Save to DB or further processing
    }
}