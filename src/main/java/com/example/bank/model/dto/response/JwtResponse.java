package com.example.bank.model.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class JwtResponse {

    private String token;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String password;
    private Set<String> roles;

    public JwtResponse(String token, Long id, String email, Set<String> roles) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.roles = roles;
    }
}