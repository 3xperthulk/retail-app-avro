package com.retail.producer.service;

import com.retail.avro.RetailTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class OrderProducer {

    private static final String TOPIC = "retail-trans-new";

    @Autowired
    private KafkaTemplate<String, RetailTransaction> kafkaTemplate;

    private final Random random = new Random();

    @Scheduled(fixedRate = 1000)
    public void sendTransaction() {
        for (int i = 0; i < 10; i++) {
            RetailTransaction tx = RetailTransaction.newBuilder()
                    .setTransactionId(UUID.randomUUID().toString())
                    .setCustomerId("cust-" + random.nextInt(1000))
                    .setItemId("item-" + random.nextInt(500))
                    .setItemName("Item-" + random.nextInt(100))
                    .setQuantity(random.nextInt(5) + 1)
                    .setPrice(random.nextDouble() * 100)
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            // Use .toString() to avoid CharSequence -> String issues
            kafkaTemplate.send(TOPIC, tx.getTransactionId().toString(), tx);
        }
    }
}
