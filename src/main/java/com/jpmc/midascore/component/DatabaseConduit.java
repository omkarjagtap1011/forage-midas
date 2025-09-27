package com.jpmc.midascore.component;

import com.jpmc.midascore.entity.TransactionRecord;
import com.jpmc.midascore.foundation.Transaction;
import com.jpmc.midascore.entity.UserRecord;
import com.jpmc.midascore.repository.UserRepository;
import com.jpmc.midascore.repository.TransactionRecordRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;
import com.jpmc.midascore.incentive.Incentive;

@Component
public class DatabaseConduit {
    private final UserRepository userRepository;
    private final TransactionRecordRepository txRepository;

    private final RestTemplate restTemplate;

    @Value("${incentive.api.base-url}")
    private String incentiveApiBaseUrl;

    public DatabaseConduit(
            UserRepository userRepository,
            TransactionRecordRepository txRepository,
            RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.txRepository = txRepository;
        this.restTemplate = restTemplate;

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

        // Defaulting Incentive Amount to 0 on API Fails/Null Returns
        float incentiveAmount = 0.0f;
        try {
            Incentive response = restTemplate.postForObject(
                    // Serialized Transaction
                    incentiveApiBaseUrl + "/incentive",
                    tx,
                    Incentive.class);
            if (response != null) {
                float apiAmt = response.getAmount();
                incentiveAmount = (apiAmt >= 0.0f) ? apiAmt : 0.0f; // defensive clamp
            }
        } catch (Exception ex) {
            // Skipped (maintains default value)
        }
        tx.setIncentive(incentiveAmount);

        // Applying Updates
        sender.setBalance(senderBal - amount);
        recipient.setBalance(recipient.getBalance() + amount + incentiveAmount);
        // recipient.setBalance(recipient.getBalance() + amount);

        // Persisting Updates
        userRepository.save(sender);
        userRepository.save(recipient);
        txRepository.save(new TransactionRecord(sender, recipient, amount));

        // Investigating Wilbur
        if ("wilbur".equals(sender.getName())) {
            float b = sender.getBalance();
            System.out.println("[Task3] Wilbur balance after tx (sender): " + b + " | floored=" + (int)Math.floor(b));
        }
        if ("wilbur".equals(recipient.getName())) {
            float b = recipient.getBalance();
            System.out.println("[Task3] Wilbur balance after tx (recipient): " + b + " | floored=" + (int)Math.floor(b));
        }

    }

}
