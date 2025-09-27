package com.jpmc.midascore.kafka;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/*
 * Listens to the Kafka topic configured in application.yml and receives Transaction messages.
 * No processing yet is ongoing
 */
@Component
public class TransactionListener {

    private static final Logger log = LoggerFactory.getLogger(TransactionListener.class);

    // Logs Received Info
    private final List<Transaction> received = new CopyOnWriteArrayList<>();

    // Configuring Topic Name from yml
    @Value("${midas.kafka.topic.transactions}")
    private String topic;

    @KafkaListener(topics = "${midas.kafka.topic.transactions}")
    public void onMessage(
            Transaction tx,
            @Header(KafkaHeaders.OFFSET) long offset,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {
        received.add(tx);
        // Logging only
        log.debug("Received tx on topic='{}' partition={} offset={}: {}", topic, partition, offset, tx);
    }

    /** Exposed for tests/debugger to read the collected transactions. */
    public List<Transaction> getReceived() {
        return received;
    }
}
