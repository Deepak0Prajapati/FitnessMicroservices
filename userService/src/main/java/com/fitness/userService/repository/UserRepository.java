package com.fitness.userService.repository;

import com.fitness.userService.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, String> {


    boolean existsByEmail(String email);

    Boolean existsBykeycloakId(String userId);

    User findByEmail(String email);
}
