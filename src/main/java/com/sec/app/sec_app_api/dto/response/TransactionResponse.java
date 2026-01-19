package com.sec.app.sec_app_api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private String kind;
    private Double amt;
    
    @JsonProperty("updated_bal")
    private Double updatedBal;
    
    private Instant timestamp;
}
