package com.example.bank.controller;

import com.example.bank.model.dto.request.CardCreateRequest;
import com.example.bank.model.dto.response.CardResponse;
import com.example.bank.model.entity.CardStatus;
import com.example.bank.service.CardService;
import com.example.bank.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AdminCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CardService cardService;

    @MockBean
    private UserService userService;

    private static final String EMAIL = "test@example.com";

    @SneakyThrows
    @Test
    public void testCreateCard() {

        Map<String, Object> request = new HashMap<>();
        request.put("ownerEmail", EMAIL);
        request.put("expirationDate", "2025-12-31");
        request.put("initialBalance", 100);


        CardResponse response = new CardResponse(
                1L,
                "encrypted",
                EMAIL,
                LocalDate.of(2025, 12, 31),
                CardStatus.ACTIVE,
                BigDecimal.valueOf(100)
        );
        when(cardService.createCard(any(CardCreateRequest.class))).thenReturn(response);

        // Execute & verify
        mockMvc.perform(post("/api/admin/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.cardNumber").value("encrypted"))
                .andExpect(jsonPath("$.ownerEmail").value(EMAIL))
                .andExpect(jsonPath("$.expirationDate").value("2025-12-31"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.balance").value(100));
    }

    @SneakyThrows
    @Test
    public void testActivateCard() {

        doNothing().when(cardService).changeCardStatus(1L, CardStatus.ACTIVE);


        mockMvc.perform(put("/api/admin/cards/{cardId}/activate", 1L))
                .andExpect(status().isNoContent());
    }

    @SneakyThrows
    @Test
    public void testDeleteCard() {

        doNothing().when(cardService).deleteCard(1L);


        mockMvc.perform(delete("/api/admin/cards/{cardId}", 1L))
                .andExpect(status().isNoContent());
    }
}
