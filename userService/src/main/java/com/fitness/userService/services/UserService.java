package com.fitness.userService.services;

import com.fitness.userService.dto.RegisterRequest;
import com.fitness.userService.dto.UserResponse;
import com.fitness.userService.models.User;
import com.fitness.userService.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse register(RegisterRequest registerRequest){
        if(userRepository.existsByEmail(registerRequest.getEmail())){
            User user =userRepository.findByEmail(registerRequest.getEmail());
            toUserResponse(user);
        }
        User user = User.builder().email(registerRequest.getEmail())
                .password(registerRequest.getPassword())
                .firstName(registerRequest.getFirstName())
                .keycloakId(registerRequest.getKeycloakId())
                .lastName(registerRequest.getLastName())
                .build();
        return toUserResponse(userRepository.save(user));

    }
    public UserResponse toUserResponse(User user){
        return UserResponse.builder()
                .Id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .firstName(user.getFirstName())
                .keycloakId(user.getKeycloakId())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserResponse getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User Not Found !!"));
        return toUserResponse(user);

    }

    public Boolean existByUserId(String userId) {
        return userRepository.existsBykeycloakId(userId);
    }
}
