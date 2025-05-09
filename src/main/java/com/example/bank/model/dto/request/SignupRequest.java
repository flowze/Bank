package com.example.bank.model.dto.request;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class SignupRequest {

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;
}
