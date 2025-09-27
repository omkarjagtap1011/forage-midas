package com.jpmc.midascore.kafka;

import com.jpmc.midascore.foundation.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import com.jpmc.midascore.component.DatabaseConduit;

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

    // Database Conduit
    private final DatabaseConduit databaseConduit;

    // Configuring Topic Name from yml
    @Value("${midas.kafka.topic.transactions}")
    private String topic;

    public TransactionListener(DatabaseConduit databaseConduit) {
        this.databaseConduit = databaseConduit;
    }

    @KafkaListener(topics = "${midas.kafka.topic.transactions}", groupId = "${spring.application.name:midas-core}")
    public void onMessage(
            Transaction tx
//            @Header(KafkaHeaders.OFFSET) long offset,
//            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition
    ) {

        databaseConduit.validateAndRecord(tx);
//        received.add(tx);
//        // Logging only
//        log.debug("Received tx on topic='{}' partition={} offset={}: {}", topic, partition, offset, tx);
    }

    /** Exposed for tests/debugger to read the collected transactions. */
    public List<Transaction> getReceived() {
        return received;
    }
}
