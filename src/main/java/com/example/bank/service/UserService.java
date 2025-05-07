package com.example.bank.service;

import com.example.bank.model.dto.response.CardResponse;
import com.example.bank.model.dto.response.UserResponse;
import com.example.bank.model.entity.Card;
import com.example.bank.model.entity.User;
import com.example.bank.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final ModelMapper modelMapper;

    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public Page<UserResponse> getAllUsers(String email, Pageable pageable) {
        Specification<User> spec = (root, query, cb) -> cb.conjunction();

        if (email != null) {
            User owner = userRepository.findByEmail(email)
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User not found with email: " + email));

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("email"), owner.getEmail()));
        }
        Page<User> users = userRepository.findAll(spec, pageable);

        return users.map(this::convertToUserResponse);
    }

    private UserResponse convertToUserResponse(User user) {
        UserResponse response = modelMapper.map(user, UserResponse.class);
        return response;
    }
}
