package com.sec.app.sec_app_api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class Transaction {
    private String username;
    private String transactionId;
    private String kind; // "credit" or "debit"
    private Double amount;
    private Double updatedBalance;
    private Instant timestamp;
    private String description;
    private String recipient; // For payments, null for funding

    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }

    @DynamoDbSortKey
    public String getTransactionId() {
        return transactionId;
    }
}
