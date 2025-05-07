package com.example.bank.service;

import com.example.bank.exception.InsufficientFundsException;
import com.example.bank.exception.UnauthorizedAccessException;
import com.example.bank.model.dto.request.CardCreateRequest;
import com.example.bank.model.dto.request.TransferRequest;
import com.example.bank.model.dto.response.BalanceResponse;
import com.example.bank.model.dto.response.CardResponse;
import com.example.bank.model.entity.Card;
import com.example.bank.model.entity.CardStatus;
import com.example.bank.model.entity.User;
import com.example.bank.repository.CardRepository;
import com.example.bank.repository.UserRepository;
import com.example.bank.security.service.UserDetailsImpl;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    public Page<CardResponse> getUserCards(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Specification<Card> spec = Specification.where((root, query, cb) ->
                cb.equal(root.get("owner"), user)
        );

        return cardRepository.findAll(spec, pageable)
                .map(this::convertToCardResponse);
    }

    private CardResponse convertToCardResponse(Card card) {
        CardResponse response = modelMapper.map(card, CardResponse.class);
        response.setCardNumber(maskCardNumber(card.getCardNumber()));
        return response;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 16) {
            return "**** **** **** ****";
        }
        return "**** **** **** " + cardNumber.substring(cardNumber.length() - 4);
    }


    public void requestBlockCard(String email, Long cardId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card not found"));

        if (!card.getOwner().equals(user)) {
            throw new UnauthorizedAccessException("Card does not belong to the user");
        }

        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not active");
        }

        card.setStatus(CardStatus.BLOCKED);
        cardRepository.save(card);

    }

    @Transactional
    public void transferBetweenUserCards(String email, @Valid TransferRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card sourceCard = cardRepository.findById(request.getSourceCardId())
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));

        Card targetCard = cardRepository.findById(request.getTargetCardId())
                .orElseThrow(() -> new EntityNotFoundException("Target card not found"));


        if (!sourceCard.getOwner().equals(user)) {
            throw new UnauthorizedAccessException("Source card does not belong to the user");
        }


        if (sourceCard.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException("Insufficient funds for transfer");
        }


        sourceCard.setBalance(sourceCard.getBalance().subtract(request.getAmount()));
        targetCard.setBalance(targetCard.getBalance().add(request.getAmount()));

        cardRepository.saveAll(List.of(sourceCard, targetCard));
    }

    public BalanceResponse getCardBalance(String email, Long cardId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));

        if (!card.getOwner().equals(user)) {
            throw new UnauthorizedAccessException("Card does not belong to the user");
        }

        BalanceResponse balanceResponse = new BalanceResponse(cardId,card.getBalance());
        return balanceResponse;
    }



    public CardResponse createCard(CardCreateRequest request) {
        User user = userRepository.findByEmail(request.getOwnerEmail())
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Card card = modelMapper.map(request, Card.class);
        card.setOwner(user);
        cardRepository.save(card);

        CardResponse map = convertToCardResponse(card);

        return map;
    }

    public Page<CardResponse> getAllCards(String ownerEmail, CardStatus status, Pageable pageable) {
        Specification<Card> spec = (root, query, cb) -> cb.conjunction();

        if (ownerEmail != null) {
            User owner = userRepository.findByEmail(ownerEmail)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User not found with email: " + ownerEmail));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("owner"), owner));
        }

        if (status != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("status"), status));
        }


        Page<Card> cardsPage = cardRepository.findAll(spec, pageable);


        return cardsPage.map(this::convertToCardResponse);
    }

    public void changeCardStatus(Long cardId, CardStatus cardStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));
        card.setStatus(cardStatus);
        cardRepository.save(card);
    }

    public void deleteCard(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Source card not found"));
        cardRepository.delete(card);
    }
}
