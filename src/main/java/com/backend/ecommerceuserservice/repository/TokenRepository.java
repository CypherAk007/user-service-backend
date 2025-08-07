package com.backend.ecommerceuserservice.repository;

import com.backend.ecommerceuserservice.models.Token;
import com.backend.ecommerceuserservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Date;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token,Long> {
    Optional<Token> findByTokenValueAndDeletedAndExpiryAtGreaterThan(String tokenValue, boolean deleted, Date expiryAt);

    Optional<Token> findByTokenValue(String token);
}
