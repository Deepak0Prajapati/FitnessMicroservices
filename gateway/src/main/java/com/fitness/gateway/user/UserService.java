package com.fitness.gateway.user;

import com.fitness.gateway.dto.RegisterRequest;
import com.fitness.gateway.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final WebClient userServiceWebClient;

    public Mono<Boolean> validateUser(String userId){
        log.info("Caling user Validation Service for {}",userId);
            return   userServiceWebClient.get()
                    .uri("/api/users/{userId}/validate",userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .onErrorResume(WebClientResponseException.class, e->{
                        if(e.getStatusCode()== HttpStatus.NOT_FOUND)
                            return Mono.error(new RuntimeException("UserId not Found: "+userId));

                        else if (e.getStatusCode()==HttpStatus.BAD_REQUEST) {
                            return Mono.error(new RuntimeException("Invalid :"+userId));

                        }

                        return Mono.error(new RuntimeException("Unexpected Error occurred :"+userId));
                    });


    }

    public Mono<UserResponse> registerUser(RegisterRequest registerRequest) {
        log.info("Caling user Registration API for {}",registerRequest.getEmail());
            return   userServiceWebClient.post()
                    .uri("/api/users/register")
                    .bodyValue(registerRequest)
                    .retrieve()
                    .bodyToMono(UserResponse.class)
                    .onErrorResume(WebClientResponseException.class, e->{
                        if (e.getStatusCode()==HttpStatus.BAD_REQUEST) {
                            return Mono.error(new RuntimeException("Bad Request :"+e.getMessage()));

                        }

                        return Mono.error(new RuntimeException("Unexpected Error occurred :"+ e.getMessage()));
                    });
    }
}