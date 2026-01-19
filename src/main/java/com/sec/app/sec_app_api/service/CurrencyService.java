package com.sec.app.sec_app_api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Service
public class CurrencyService {
    
    private final WebClient webClient;
    
    public CurrencyService(WebClient webClient) {
        this.webClient = webClient;
    }
    
    public double convertFromINRTo(String targetCurrency, double amountInINR) {
        if ("INR".equalsIgnoreCase(targetCurrency)) {
            return amountInINR;
        }
        
        try {
            ExchangeRateResponse response = webClient
                    .get()
                    .uri("https://open.er-api.com/v6/latest/INR")
                    .retrieve()
                    .bodyToMono(ExchangeRateResponse.class)
                    .block();
            
            if (response != null && response.getRates() != null) {
                Double rate = response.getRates().get(targetCurrency.toUpperCase());
                if (rate != null) {
                    return amountInINR * rate;
                }
            }
            
            // If conversion fails, return original amount
            return amountInINR;
        } catch (Exception e) {
            // If API call fails, return original amount
            return amountInINR;
        }
    }
    
    @Data
    public static class ExchangeRateResponse {
        private String result;
        
        @JsonProperty("time_last_update_utc")
        private String timeLastUpdateUtc;
        
        @JsonProperty("time_next_update_utc")
        private String timeNextUpdateUtc;
        
        @JsonProperty("base_code")
        private String baseCode;
        
        private Map<String, Double> rates;
    }
}
