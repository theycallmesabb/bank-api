package com.sec.app.sec_app_api.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamoDbBean
public class User {
    private String username;
    private String passwordHash;
    private Double balance;
    private Instant createdAt;
    private Instant updatedAt;

    @DynamoDbPartitionKey
    public String getUsername() {
        return username;
    }
}
