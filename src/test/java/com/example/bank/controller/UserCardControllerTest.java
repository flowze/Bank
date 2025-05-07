package com.example.bank.controller;

import com.example.bank.model.dto.request.TransferRequest;
import com.example.bank.model.dto.response.BalanceResponse;
import com.example.bank.model.dto.response.CardResponse;
import com.example.bank.model.entity.CardStatus;
import com.example.bank.security.SecurityConfig;
import com.example.bank.security.service.UserDetailsImpl;
import com.example.bank.service.CardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UserCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    private static final String EMAIL = "test@example.com";
    private static final UserDetailsImpl CURRENT_USER = new UserDetailsImpl(
            1L,
            EMAIL,
            "password",
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
    );

    @SneakyThrows
    @Test
    public void testGetMyCards() {
        Pageable pageable = PageRequest.of(0, 10);
        CardResponse card = new CardResponse(
                1L,
                "encrypted",
                EMAIL,
                LocalDate.of(2025, 12, 31),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(100)
        );
        Page<CardResponse> page = new PageImpl<>(List.of(card), pageable, 1);
        when(cardService.getUserCards(EMAIL, pageable)).thenReturn(page);

        mockMvc.perform(get("/api/user/cards")
                        .with(authentication(new UsernamePasswordAuthenticationToken(CURRENT_USER, null, CURRENT_USER.getAuthorities())))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].cardNumber").value("encrypted"))
                .andExpect(jsonPath("$.content[0].ownerEmail").value(EMAIL))
                .andExpect(jsonPath("$.content[0].expirationDate").value("2025-12-31"))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$.content[0].balance").value(100));
    }

    @SneakyThrows
    @Test
    public void testRequestBlockCard() {
        mockMvc.perform(post("/api/user/cards/{cardId}/block-request", 1L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(CURRENT_USER, null, CURRENT_USER.getAuthorities()))))
                .andExpect(status().isNoContent());

        verify(cardService).requestBlockCard(EMAIL, 1L);
    }

    @SneakyThrows
    @Test
    public void testTransferBetweenCards() {
        TransferRequest request = new TransferRequest(1L, 2L, BigDecimal.valueOf(50));
        String json = objectMapper.writeValueAsString(request);

        mockMvc.perform(post("/api/user/cards/transfer")
                        .with(authentication(new UsernamePasswordAuthenticationToken(CURRENT_USER, null, CURRENT_USER.getAuthorities())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNoContent());

        verify(cardService).transferBetweenUserCards(EMAIL, request);
    }

    @SneakyThrows
    @Test
    public void testGetCardBalance() {
        BalanceResponse balance = new BalanceResponse(1L,BigDecimal.valueOf(100));
        when(cardService.getCardBalance(EMAIL, 1L)).thenReturn(balance);

        mockMvc.perform(get("/api/user/cards/{cardId}/balance", 1L)
                        .with(authentication(new UsernamePasswordAuthenticationToken(CURRENT_USER, null, CURRENT_USER.getAuthorities()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(100));
    }
}