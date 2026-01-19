package com.sec.app.sec_app_api.repository;

import com.sec.app.sec_app_api.entity.User;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.Key;

import java.util.Optional;

@Repository
public class UserRepository {
    
    private final DynamoDbTable<User> userTable;

    public UserRepository(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
        this.userTable = dynamoDbEnhancedClient.table("users", TableSchema.fromBean(User.class));
    }

    public void save(User user) {
        userTable.putItem(user);
    }

    public Optional<User> findByUsername(String username) {
        Key key = Key.builder().partitionValue(username).build();
        User user = userTable.getItem(key);
        return Optional.ofNullable(user);
    }

    public boolean existsByUsername(String username) {
        return findByUsername(username).isPresent();
    }
}
