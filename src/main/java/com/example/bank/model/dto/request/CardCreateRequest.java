package com.example.bank.model.dto.request;

import com.example.bank.model.entity.CardStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CardCreateRequest {
    private String cardNumber;
    private String ownerEmail;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;
}
