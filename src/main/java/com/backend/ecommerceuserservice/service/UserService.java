package com.backend.ecommerceuserservice.service;

import com.backend.ecommerceuserservice.models.Token;
import com.backend.ecommerceuserservice.models.User;
import org.springframework.stereotype.Service;

public interface UserService {
    User signup(String name,String email,String password);
    Token login(String email,String password);
    void logout(String token);
    User validateToken(String tokenValue);

}
