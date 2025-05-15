package com.retail.producer.service;

import com.retail.avro.RetailTransaction;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

@Service
public class OrderProducer {

    @Value("${app.kafka.topic.name}")
    private String topicName;

    @Value("${app.kafka.message.count}")
    private int messageCount;

    private final KafkaTemplate<String, RetailTransaction> kafkaTemplate;
    private final Random random = new Random();

    public OrderProducer(KafkaTemplate<String, RetailTransaction> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @PostConstruct
    public void sendLoadTestMessages() {
        final int[] successCount = {0};
        final long[] totalLatency = {0};
        final long[] maxLatency = {0};
        final long[] minLatency = {Long.MAX_VALUE};

        long startTime = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(messageCount);

        for (int i = 0; i < messageCount; i++) {
            final RetailTransaction tx = RetailTransaction.newBuilder()
                    .setTransactionId(UUID.randomUUID().toString())
                    .setCustomerId("cust-" + random.nextInt(1000))
                    .setItemId("item-" + random.nextInt(500))
                    .setItemName("Item-" + random.nextInt(100))
                    .setQuantity(random.nextInt(5) + 1)
                    .setPrice(random.nextDouble() * 100)
                    .setTimestamp(System.currentTimeMillis())
                    .build();

            final long sendStart = System.nanoTime();

            CompletableFuture<SendResult<String, RetailTransaction>> future =
                    kafkaTemplate.send(topicName, tx.getTransactionId().toString(), tx);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    System.err.println("❌ Failed: " + ex.getMessage());
                } else {
                    long latency = System.nanoTime() - sendStart;
                    synchronized (this) {
                        successCount[0]++;
                        totalLatency[0] += latency;
                        maxLatency[0] = Math.max(maxLatency[0], latency);
                        minLatency[0] = Math.min(minLatency[0], latency);
                    }
                }
                latch.countDown();
            });
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long endTime = System.currentTimeMillis();
        long totalTimeMs = endTime - startTime;

        System.out.println("=== Kafka Producer Performance Report ===");
        System.out.println("✅ Records Sent: " + successCount[0]);
        System.out.println("⏱️ Total Time: " + totalTimeMs + " ms");
        System.out.println("📈 Throughput: " + (successCount[0] * 1000L / totalTimeMs) + " records/sec");
        System.out.println("📊 Avg Latency: " + (totalLatency[0] / successCount[0]) / 1_000_000.0 + " ms");
        System.out.println("⬆️ Max Latency: " + (maxLatency[0]) / 1_000_000.0 + " ms");
        System.out.println("⬇️ Min Latency: " + (minLatency[0]) / 1_000_000.0 + " ms");
    }
}