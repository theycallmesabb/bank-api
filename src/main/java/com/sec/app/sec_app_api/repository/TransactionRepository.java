package com.sec.app.sec_app_api.repository;

import com.sec.app.sec_app_api.entity.Transaction;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TransactionRepository {
    
    private final DynamoDbTable<Transaction> transactionTable;

    public TransactionRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.transactionTable = dynamoDbEnhancedClient.table("transactions", TableSchema.fromBean(Transaction.class));
    }

    public void save(Transaction transaction) {
        transactionTable.putItem(transaction);
    }

    public List<Transaction> findByUsername(String username) {
        Key key = Key.builder().partitionValue(username).build();
        QueryConditional queryConditional = QueryConditional.keyEqualTo(key);
        
        return transactionTable.query(queryConditional)
                .items()
                .stream()
                .collect(Collectors.toList());
    }
}
