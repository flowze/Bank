package com.example.bank.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TransferRequest {
    @NotNull
    private Long sourceCardId;

    @NotNull
    private Long targetCardId;

    @Positive
    private BigDecimal amount;
}
