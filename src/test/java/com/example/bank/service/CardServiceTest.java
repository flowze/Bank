package com.example.bank.service;

import com.example.bank.exception.UnauthorizedAccessException;
import com.example.bank.model.dto.request.CardCreateRequest;
import com.example.bank.model.dto.request.TransferRequest;
import com.example.bank.model.dto.response.CardResponse;
import com.example.bank.model.entity.Card;
import com.example.bank.model.entity.CardStatus;
import com.example.bank.model.entity.User;
import com.example.bank.repository.CardRepository;
import com.example.bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class CardServiceTest {

    private CardRepository cardRepository;
    private UserRepository userRepository;
    private ModelMapper modelMapper;
    private CardService cardService;

    private User user;
    private Card card;
    private CardCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        userRepository = mock(UserRepository.class);
        modelMapper = mock(ModelMapper.class);
        cardService = new CardService(cardRepository, modelMapper, userRepository);

        user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");

        card = new Card();
        card.setId(2L);
        card.setCardNumber("1234567812345678");
        card.setOwner(user);
        card.setExpirationDate(LocalDate.of(2025,12,31));
        card.setStatus(CardStatus.ACTIVE);
        card.setBalance(BigDecimal.valueOf(200));

        createRequest = new CardCreateRequest();
        createRequest.setOwnerEmail(user.getEmail());
        createRequest.setExpirationDate(card.getExpirationDate());
        createRequest.setBalance(card.getBalance());
    }

    @Test
    void changeCardStatus_ShouldUpdateStatus() {
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));

        cardService.changeCardStatus(2L, CardStatus.BLOCKED);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void changeCardStatus_WhenNotFound_ShouldThrow() {
        when(cardRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> cardService.changeCardStatus(99L, CardStatus.BLOCKED));
    }

    @Test
    void getAllCards_ShouldReturnFilteredPage() {
        Pageable pageable = Pageable.unpaged();

        CardResponse mapped = new CardResponse(card.getId(), card.getCardNumber(), user.getEmail(), card.getExpirationDate(), card.getStatus(), card.getBalance());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cardRepository.findAll(any(Specification.class), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(card)));
        when(modelMapper.map(eq(card), eq(CardResponse.class))).thenReturn(mapped);

        Page<CardResponse> result = cardService.getAllCards(user.getEmail(), CardStatus.ACTIVE, pageable);

        assertEquals(1, result.getTotalElements());
        CardResponse resp = result.getContent().get(0);

        assertTrue(resp.getCardNumber().endsWith("5678"));
        assertEquals("**** **** **** 5678", resp.getCardNumber());
    }

    @Test
    void createCard_ShouldMapAndSave() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(modelMapper.map(createRequest, Card.class)).thenReturn(card);
        when(cardRepository.save(card)).thenReturn(card);
        CardResponse mapped = new CardResponse(card.getId(), card.getCardNumber(), user.getEmail(), card.getExpirationDate(), card.getStatus(), card.getBalance());
        when(modelMapper.map(eq(card), eq(CardResponse.class))).thenReturn(mapped);

        CardResponse result = cardService.createCard(createRequest);

        assertNotNull(result);
        assertEquals(card.getId(), result.getId());
        assertEquals(user.getEmail(), result.getOwnerEmail());
        assertEquals("**** **** **** 5678", result.getCardNumber());
        verify(cardRepository).save(card);
    }

    @Test
    void transferBetweenUserCards_Success() {
        // Setup user
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        // Source and target cards
        Card source = new Card(); source.setId(1L); source.setOwner(user); source.setBalance(BigDecimal.valueOf(150));
        Card target = new Card(); target.setId(2L); target.setOwner(user); target.setBalance(BigDecimal.valueOf(50));
        when(cardRepository.findById(1L)).thenReturn(Optional.of(source));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(target));

        TransferRequest req = new TransferRequest(1L, 2L, BigDecimal.valueOf(100));
        cardService.transferBetweenUserCards(user.getEmail(), req);

        assertEquals(BigDecimal.valueOf(50), source.getBalance());
        assertEquals(BigDecimal.valueOf(150), target.getBalance());
        verify(cardRepository).saveAll(List.of(source, target));
    }

    @Test
    void transferBetweenUserCards_InsufficientFunds_ShouldThrow() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        card.setBalance(BigDecimal.valueOf(50));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));
        TransferRequest req = new TransferRequest(2L, 3L, BigDecimal.valueOf(100));

        assertThrows(RuntimeException.class,
                () -> cardService.transferBetweenUserCards(user.getEmail(), req));
    }

    @Test
    void requestBlockCard_Success() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));

        cardService.requestBlockCard(user.getEmail(), 2L);

        assertEquals(CardStatus.BLOCKED, card.getStatus());
        verify(cardRepository).save(card);
    }

    @Test
    void requestBlockCard_Unauthorized_ShouldThrow() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        Card other = new Card(); other.setId(3L); other.setOwner(new User()); other.setStatus(CardStatus.ACTIVE);
        when(cardRepository.findById(3L)).thenReturn(Optional.of(other));

        assertThrows(UnauthorizedAccessException.class,
                () -> cardService.requestBlockCard(user.getEmail(), 3L));
    }

    @Test
    void requestBlockCard_NotActive_ShouldThrow() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        card.setStatus(CardStatus.BLOCKED);
        when(cardRepository.findById(2L)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class,
                () -> cardService.requestBlockCard(user.getEmail(), 2L));
    }
}