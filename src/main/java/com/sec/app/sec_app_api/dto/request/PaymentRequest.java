package com.sec.app.sec_app_api.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
public class PaymentRequest {
    @NotBlank(message = "Recipient username is required")
    private String to;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Double amt;
}
