package com.sec.app.sec_app_api.config;

import com.sec.app.sec_app_api.entity.Transaction;
import com.sec.app.sec_app_api.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import java.net.URI;

/**
 * Configuration class for DynamoDB setup
 * Configures AWS DynamoDB client, enhanced client, and table mappings for the
 * application.
 * 
 * @author Ankit Ranjan
 * @since May 18, 2024
 * @see software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
 * @see software.amazon.awssdk.services.dynamodb.DynamoDbClient
 */
@Configuration
public class DynamoDbConfig {

    @Value("${spring.cloud.aws.dynamodb.region:us-east-1}")
    private String region;

    @Value("${spring.cloud.aws.dynamodb.endpoint:}")
    private String endpoint;

    @Value("${spring.cloud.aws.credentials.access-key:}")
    private String accessKey;

    @Value("${spring.cloud.aws.credentials.secret-key:}")
    private String secretKey;

    @Bean
    public DynamoDbClient dynamoDbClient() {
        var clientBuilder = DynamoDbClient.builder()
                .region(Region.of(region));

        // For local development with endpoint override (e.g., DynamoDB Local)
        if (!endpoint.isEmpty()) {
            clientBuilder.endpointOverride(URI.create(endpoint));

            // Use provided credentials for local development only
            if (!accessKey.isEmpty() && !secretKey.isEmpty() &&
                    !accessKey.equals("your-access-key") && !secretKey.equals("your-secret-key")) {
                clientBuilder.credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey, secretKey)));
            }
        }
        // For production/real AWS, credentials will be automatically resolved from:
        // 1. Environment variables (AWS_ACCESS_KEY_ID, AWS_SECRET_ACCESS_KEY)
        // 2. AWS credentials file (~/.aws/credentials)
        // 3. IAM roles (for EC2/ECS/Lambda deployments)

        return clientBuilder.build();
    }

    @Bean
    public DynamoDbEnhancedClient dynamoDbEnhancedClient(DynamoDbClient dynamoDbClient) {
        return DynamoDbEnhancedClient.builder()
                .dynamoDbClient(dynamoDbClient)
                .build();
    }

    @Bean
    public DynamoDbTable<User> userTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("sec-app-users", TableSchema.fromBean(User.class));
    }

    @Bean
    public DynamoDbTable<Transaction> transactionTable(DynamoDbEnhancedClient enhancedClient) {
        return enhancedClient.table("banking-transactions", TableSchema.fromBean(Transaction.class));
    }

    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
