package com.sec.app.sec_app_api.service;

import com.sec.app.sec_app_api.entity.Transaction;
import com.sec.app.sec_app_api.entity.User;
import com.sec.app.sec_app_api.repository.TransactionRepository;
import com.sec.app.sec_app_api.repository.UserRepository;
import com.sec.app.sec_app_api.dto.response.TransactionResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class BankingService {
    
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrencyService currencyService;
    
    public BankingService(UserRepository userRepository, 
                         TransactionRepository transactionRepository,
                         PasswordEncoder passwordEncoder,
                         CurrencyService currencyService) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.passwordEncoder = passwordEncoder;
        this.currencyService = currencyService;
    }
    
    public boolean registerUser(String username, String password) {
        if (userRepository.existsByUsername(username)) {
            return false;
        }
        
        User user = User.builder()
                .username(username)
                .passwordHash(passwordEncoder.encode(password))
                .balance(0.0)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        userRepository.save(user);
        return true;
    }
    
    public Double fundAccount(String username, Double amount) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        Double newBalance = user.getBalance() + amount;
        user.setBalance(newBalance);
        user.setUpdatedAt(Instant.now());
        
        userRepository.save(user);
        
        // Record transaction
        Transaction transaction = Transaction.builder()
                .username(username)
                .transactionId(UUID.randomUUID().toString())
                .kind("credit")
                .amount(amount)
                .updatedBalance(newBalance)
                .timestamp(Instant.now())
                .description("Account funding")
                .build();
        
        transactionRepository.save(transaction);
        
        return newBalance;
    }
    
    public Double payUser(String fromUsername, String toUsername, Double amount) {
        // Check if sender exists and has sufficient balance
        Optional<User> fromUserOpt = userRepository.findByUsername(fromUsername);
        if (fromUserOpt.isEmpty()) {
            throw new RuntimeException("Sender not found");
        }
        
        User fromUser = fromUserOpt.get();
        if (fromUser.getBalance() < amount) {
            throw new RuntimeException("Insufficient funds");
        }
        
        // Check if recipient exists
        Optional<User> toUserOpt = userRepository.findByUsername(toUsername);
        if (toUserOpt.isEmpty()) {
            throw new RuntimeException("Recipient not found");
        }
        
        User toUser = toUserOpt.get();
        
        // Update balances
        Double newFromBalance = fromUser.getBalance() - amount;
        Double newToBalance = toUser.getBalance() + amount;
        
        fromUser.setBalance(newFromBalance);
        fromUser.setUpdatedAt(Instant.now());
        
        toUser.setBalance(newToBalance);
        toUser.setUpdatedAt(Instant.now());
        
        userRepository.save(fromUser);
        userRepository.save(toUser);
        
        Instant now = Instant.now();
        
        // Record debit transaction for sender
        Transaction debitTransaction = Transaction.builder()
                .username(fromUsername)
                .transactionId(UUID.randomUUID().toString())
                .kind("debit")
                .amount(amount)
                .updatedBalance(newFromBalance)
                .timestamp(now)
                .description("Payment to " + toUsername)
                .recipient(toUsername)
                .build();
        
        // Record credit transaction for recipient
        Transaction creditTransaction = Transaction.builder()
                .username(toUsername)
                .transactionId(UUID.randomUUID().toString())
                .kind("credit")
                .amount(amount)
                .updatedBalance(newToBalance)
                .timestamp(now)
                .description("Payment from " + fromUsername)
                .build();
        
        transactionRepository.save(debitTransaction);
        transactionRepository.save(creditTransaction);
        
        return newFromBalance;
    }
    
    public Double getBalance(String username, String currency) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        Double balanceInINR = userOpt.get().getBalance();
        
        if (currency == null || "INR".equalsIgnoreCase(currency)) {
            return balanceInINR;
        }
        
        return currencyService.convertFromINRTo(currency, balanceInINR);
    }
    
    public List<TransactionResponse> getTransactionHistory(String username) {
        List<Transaction> transactions = transactionRepository.findByUsername(username);
        
        return transactions.stream()
                .map(t -> new TransactionResponse(t.getKind(), t.getAmount(), t.getUpdatedBalance(), t.getTimestamp()))
                .toList();
    }
}
