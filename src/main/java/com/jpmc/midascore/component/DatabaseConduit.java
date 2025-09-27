package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    private final TransactionRecordRepository txRepository;

    public DatabaseConduit(
            UserRepository userRepository,
            TransactionRecordRepository txRepository) {
        this.userRepository = userRepository;
        this.txRepository = txRepository;

    }

    public void save(UserRecord userRecord) {
        userRepository.save(userRecord);
    }

    @Transactional
    public void validateAndRecord(Transaction tx) {
        // Resolve users
        var senderOpt = userRepository.findByUserId(tx.getSenderId());
        var recipientOpt = userRepository.findByUserId(tx.getRecipientId());
        if (senderOpt.isEmpty() || recipientOpt.isEmpty()) {

            // Discarding invalid parties
            return;
        }

        var sender = senderOpt.get();
        var recipient = recipientOpt.get();

        float amount = tx.getAmount();
        if (amount <= 0.0f) {

            // Discarding 0 & negative amounts
            return;
        }

        // Validating funds
        float senderBal = sender.getBalance();
        if (senderBal < amount) {

            // Discarding Insufficient Funds
            return;
        }

        // Applying Updates
        sender.setBalance(senderBal - amount);
        recipient.setBalance(recipient.getBalance() + amount);

        // Persisting Updates
        userRepository.save(sender);
        userRepository.save(recipient);
        txRepository.save(new TransactionRecord(sender, recipient, amount));

        // Investigating Waldorf
        if ("waldorf".equals(sender.getName())) {
            float b = sender.getBalance();
            System.out.println("[Task3] Waldorf balance after tx (sender): " + b + " | floored=" + (int)Math.floor(b));
        }
        if ("waldorf".equals(recipient.getName())) {
            float b = recipient.getBalance();
            System.out.println("[Task3] Waldorf balance after tx (recipient): " + b + " | floored=" + (int)Math.floor(b));
        }

    }

}
