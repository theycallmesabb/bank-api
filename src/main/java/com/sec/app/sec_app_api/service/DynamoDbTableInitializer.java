package com.sec.app.sec_app_api.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class DynamoDbTableInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DynamoDbTableInitializer.class);
    private final DynamoDbClient dynamoDbClient;

    public DynamoDbTableInitializer(DynamoDbClient dynamoDbClient) {
        this.dynamoDbClient = dynamoDbClient;
    }

    @PostConstruct
    public void initializeTables() {
        try {
            createUsersTableIfNotExists();
            createTransactionsTableIfNotExists();
        } catch (Exception e) {
            logger.warn("Could not initialize DynamoDB tables: {}. Please ensure AWS credentials are configured and tables exist.", e.getMessage());
        }
    }

    private void createUsersTableIfNotExists() {
        try {
            // Check if table exists
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName("users")
                    .build());
            logger.info("Users table already exists");
        } catch (ResourceNotFoundException e) {
            logger.info("Creating users table...");
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName("users")
                    .keySchema(KeySchemaElement.builder()
                            .attributeName("username")
                            .keyType(KeyType.HASH)
                            .build())
                    .attributeDefinitions(AttributeDefinition.builder()
                            .attributeName("username")
                            .attributeType(ScalarAttributeType.S)
                            .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(createTableRequest);
            logger.info("Users table created successfully");
        }
    }

    private void createTransactionsTableIfNotExists() {
        try {
            // Check if table exists
            dynamoDbClient.describeTable(DescribeTableRequest.builder()
                    .tableName("transactions")
                    .build());
            logger.info("Transactions table already exists");
        } catch (ResourceNotFoundException e) {
            logger.info("Creating transactions table...");
            CreateTableRequest createTableRequest = CreateTableRequest.builder()
                    .tableName("transactions")
                    .keySchema(
                            KeySchemaElement.builder()
                                    .attributeName("username")
                                    .keyType(KeyType.HASH)
                                    .build(),
                            KeySchemaElement.builder()
                                    .attributeName("transactionId")
                                    .keyType(KeyType.RANGE)
                                    .build())
                    .attributeDefinitions(
                            AttributeDefinition.builder()
                                    .attributeName("username")
                                    .attributeType(ScalarAttributeType.S)
                                    .build(),
                            AttributeDefinition.builder()
                                    .attributeName("transactionId")
                                    .attributeType(ScalarAttributeType.S)
                                    .build())
                    .billingMode(BillingMode.PAY_PER_REQUEST)
                    .build();

            dynamoDbClient.createTable(createTableRequest);
            logger.info("Transactions table created successfully");
        }
    }
}
