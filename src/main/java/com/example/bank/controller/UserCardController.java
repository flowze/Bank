package com.example.bank.controller;

import com.example.bank.model.dto.request.TransferRequest;
import com.example.bank.model.dto.response.BalanceResponse;
import com.example.bank.model.dto.response.CardResponse;
import com.example.bank.model.entity.CardStatus;
import com.example.bank.security.service.UserDetailsImpl;
import com.example.bank.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/user/cards")
@PreAuthorize("hasRole('USER')")
@AllArgsConstructor
@Slf4j
@Tag(name = "User Cards Controller", description = "Эндпоинты для управления картами текущего пользователя")
public class UserCardController {

    private final CardService cardService;


    @Operation(summary = "Получить мои карты", description = "Возвращает страницу с картами текущего аутентифицированного пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешно получены карты",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "401", description = "Пользователь не авторизован", content = @Content)
    })
    @GetMapping()
    public ResponseEntity<Page<CardResponse>> getMyCards(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                         Pageable pageable) {
        Page<CardResponse> cards = cardService.getUserCards(currentUser.getEmail(), pageable);

        return ResponseEntity.ok(cards);
    }

    @Operation(summary = "Запрос на блокировку карты", description = "Отправляет запрос на блокировку карты пользователя по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Запрос на блокировку отправлен", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена или не принадлежит пользователю", content = @Content)
    })
    @PostMapping("/{cardId}/block-request")
    public ResponseEntity<Void> requestBlockCard(@AuthenticationPrincipal UserDetailsImpl currentUser,
            @PathVariable Long cardId) {
        cardService.requestBlockCard(currentUser.getEmail(), cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Перевод между картами", description = "Выполняет перевод между картами текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Перевод выполнен", content = @Content),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса", content = @Content),
            @ApiResponse(responseCode = "404", description = "Одна из карт не найдена или не принадлежит пользователю", content = @Content)
    })
    @PostMapping("/transfer")
    public ResponseEntity<Void> transferBetweenCards(@AuthenticationPrincipal UserDetailsImpl currentUser,
            @Valid @RequestBody TransferRequest request) {

        cardService.transferBetweenUserCards(currentUser.getEmail(), request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить баланс карты", description = "Возвращает баланс указанной карты текущего пользователя")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Баланс успешно получен",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = BalanceResponse.class))),
            @ApiResponse(responseCode = "404", description = "Карта не найдена или не принадлежит пользователю", content = @Content)
    })
    @GetMapping("/{cardId}/balance")
    public ResponseEntity<BalanceResponse> getCardBalance(@AuthenticationPrincipal UserDetailsImpl currentUser,
                                                          @PathVariable Long cardId) {
        return ResponseEntity.ok(cardService.getCardBalance(currentUser.getEmail(), cardId));
    }
}