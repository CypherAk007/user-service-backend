package com.backend.ecommerceuserservice.repository;

import com.backend.ecommerceuserservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);

    }
