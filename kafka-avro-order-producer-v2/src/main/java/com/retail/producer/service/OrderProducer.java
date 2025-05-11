package com.retail.producer.service;

import com.retail.avro.RetailTransaction;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.UUID;

@Service
public class OrderProducer {

    @Value("${app.kafka.topic.name}")
    private String topicName;

    private final KafkaTemplate<String, RetailTransaction> kafkaTemplate;
    private final Random random = new Random();

    // Define possible payment methods
    private final String[] paymentMethods = {"Credit Card", "Debit Card", "PayPal", "Apple Pay", "Google Pay"};

    public OrderProducer(KafkaTemplate<String, RetailTransaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedRate = 1000)
    public void sendTransaction() {
        for (int i = 0; i < 10; i++) {
            // Randomly choose a payment method from the list
            String randomPaymentMethod = paymentMethods[random.nextInt(paymentMethods.length)];

            // Create the transaction object with the random paymentMethod
            RetailTransaction tx = RetailTransaction.newBuilder()
                    .setTransactionId(UUID.randomUUID().toString())
                    .setCustomerId("cust-" + random.nextInt(1000))
                    .setItemId("item-" + random.nextInt(500))
                    .setItemName("Item-" + random.nextInt(100))
                    .setQuantity(random.nextInt(5) + 1)
                    .setPrice(random.nextDouble() * 100)
                    .setTimestamp(System.currentTimeMillis())
                    .setPaymentMethod(randomPaymentMethod)  // Assign random payment method
                    .build();

            // Send to Kafka topic
            kafkaTemplate.send(topicName, tx.getTransactionId().toString(), tx);
        }
    }
}
