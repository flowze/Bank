package com.example.bank.controller;

import com.example.bank.model.dto.request.CardCreateRequest;
import com.example.bank.model.dto.response.CardResponse;
import com.example.bank.model.dto.response.UserResponse;
import com.example.bank.model.entity.CardStatus;
import com.example.bank.service.CardService;
import com.example.bank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/admin/cards")
@PreAuthorize("hasRole('ADMIN')")
@AllArgsConstructor
@Tag(name = "Admin Cards Controller", description = "Управление картами и пользователями администраторами")
public class AdminCardController {

    private final CardService cardService;
    private final UserService userService;


    @Operation(summary = "Создание новой карты", description = "Администратор может создать новую карту для пользователя")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Карта успешно создана",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CardResponse.class))),
            @ApiResponse(responseCode = "400", description = "Неверный запрос", content = @Content)
    })
    @PostMapping
    public ResponseEntity<CardResponse> createCard(@Valid @RequestBody CardCreateRequest request) {
        CardResponse card = cardService.createCard(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(card);
    }


    @Operation(summary = "Получить все карты", description = "Администратор может получить список всех карт с фильтрацией")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = CardResponse.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CardResponse>> getAllCards(
            @RequestParam(required = false) String ownerEmail,
            @RequestParam(required = false) CardStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(cardService.getAllCards(ownerEmail, status, pageable));
    }


    @Operation(summary = "Блокировка карты", description = "Администратор блокирует карту по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта успешно заблокирована", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    @PutMapping("/{cardId}/block")
    public ResponseEntity<Void> blockCard(@PathVariable Long cardId) {
        cardService.changeCardStatus(cardId, CardStatus.BLOCKED);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Активация карты", description = "Администратор активирует карту по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта успешно активирована", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    @PutMapping("/{cardId}/activate")
    public ResponseEntity<Void> activateCard(@PathVariable Long cardId) {
        cardService.changeCardStatus(cardId, CardStatus.ACTIVE);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удалить карту", description = "Администратор удаляет карту по ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Карта успешно удалена", content = @Content),
            @ApiResponse(responseCode = "404", description = "Карта не найдена", content = @Content)
    })
    @DeleteMapping("/{cardId}")
    public ResponseEntity<Void> deleteCard(@PathVariable Long cardId) {
        cardService.deleteCard(cardId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить всех пользователей", description = "Администратор может просмотреть список пользователей с фильтрацией по email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешный ответ",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = UserResponse.class)))
    })
    @GetMapping("/users")
    public ResponseEntity<Page<UserResponse>> getAllUsers(
            @RequestParam(required = false) String email,
            Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(email, pageable));
    }
}