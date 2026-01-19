package com.sec.app.sec_app_api.controller;

import com.sec.app.sec_app_api.dto.request.RegisterRequest;
import com.sec.app.sec_app_api.dto.request.FundRequest;
import com.sec.app.sec_app_api.dto.request.PaymentRequest;
import com.sec.app.sec_app_api.dto.response.BalanceResponse;
import com.sec.app.sec_app_api.dto.response.TransactionResponse;
import com.sec.app.sec_app_api.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class BankingController {

    private final BankingService bankservice;

    public BankingController(BankingService bankingService) {
        this.bankservice = bankingService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        try {
            boolean success = bankservice.registerUser(request.getUsername(), request.getPassword());
            
            if (success) {
                return ResponseEntity.status(HttpStatus.CREATED).build();
            } else {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Username already exists");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Registration failed");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @PostMapping("/fund")
    public ResponseEntity<?> fundAccount(@Valid @RequestBody FundRequest request, Authentication auth) {
        try {
            String username = auth.getName();
            Double newBalance = bankservice.fundAccount(username, request.getAmt());
            return ResponseEntity.ok(new BalanceResponse(newBalance));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Fund operation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @PostMapping("/pay")
    public ResponseEntity<?> payUser(@Valid @RequestBody PaymentRequest request, Authentication auth) {
        try {
            String username = auth.getName();
            Double newBalance = bankservice.payUser(username, request.getTo(), request.getAmt());
            return ResponseEntity.ok(new BalanceResponse(newBalance));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            if (e.getMessage().contains("Insufficient funds")) {
                error.put("error", "Insufficient funds");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else if (e.getMessage().contains("Recipient not found")) {
                error.put("error", "Recipient not found");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            } else {
                error.put("error", "Payment failed: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
        }
    }

    @GetMapping("/bal")
    public ResponseEntity<?> getBalance(@RequestParam(required = false) String currency, Authentication auth) {
        try {
            String username = auth.getName();
            Double balance = bankservice.getBalance(username, currency);
            return ResponseEntity.ok(new BalanceResponse(balance));
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get balance: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    @GetMapping("/stmt")
    public ResponseEntity<?> getStatement(Authentication auth) {
        try {
            String username = auth.getName();
            List<TransactionResponse> transactions = bankservice.getTransactionHistory(username);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get statement: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
