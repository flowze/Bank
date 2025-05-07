package com.example.bank.controller;

import com.example.bank.model.dto.request.LoginRequest;
import com.example.bank.model.dto.request.SignupRequest;
import com.example.bank.model.dto.response.JwtResponse;
import com.example.bank.model.dto.response.MessageResponse;
import com.example.bank.model.entity.Role;
import com.example.bank.model.entity.User;
import com.example.bank.security.jwt.JwtUtils;
import com.example.bank.security.service.UserDetailsImpl;
import com.example.bank.service.UserService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    private final JwtUtils jwtUtils;
    private final UserService userService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest){


        Authentication authentication = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail()
                        ,loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Set<String> roles = userDetails.getAuthorities().stream().map(role -> role.getAuthority())
                .collect(Collectors.toSet());

        return ResponseEntity
                .ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(),
                        roles));
    }

    @PostMapping("/signup")
    public  ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signupRequest){
        if(userService.existsByEmail(signupRequest.getEmail())){
            return ResponseEntity.badRequest().body(new MessageResponse("email is already in use"));
        }


        User user = new User();
        user.setPassword(passwordEncoder.encode(signupRequest.getPassword()));
        user.setEmail(signupRequest.getEmail());
        Set<Role> roles = new HashSet<>();
        Role userRole = Role.ROLE_USER;
        roles.add(userRole);
        user.setRoles(roles);
        userService.save(user);
        return ResponseEntity.ok(new MessageResponse("COMPLETE"));
    }
}
