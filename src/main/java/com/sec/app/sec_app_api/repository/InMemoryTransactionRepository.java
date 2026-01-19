package com.sec.app.sec_app_api.repository;

import com.sec.app.sec_app_api.entity.Transaction;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
@Profile("local")
public class InMemoryTransactionRepository {
    
    private final Map<String, Transaction> transactions = new ConcurrentHashMap<>();

    public void save(Transaction transaction) {
        transactions.put(transaction.getTransactionId(), transaction);
    }

    public List<Transaction> findByUsername(String username) {
        return transactions.values().stream()
                .filter(t -> username.equals(t.getUsername()))
                .sorted((t1, t2) -> t2.getTimestamp().compareTo(t1.getTimestamp())) // Most recent first
                .collect(Collectors.toList());
    }
}
