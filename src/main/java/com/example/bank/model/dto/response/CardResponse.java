package com.example.bank.model.dto.response;

import com.example.bank.model.entity.CardStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private String cardNumber;
    private String ownerEmail;
    private LocalDate expirationDate;
    private CardStatus status;
    private BigDecimal balance;

}