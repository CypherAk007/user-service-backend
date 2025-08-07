package com.backend.ecommerceuserservice.service;

import com.backend.ecommerceuserservice.exceptions.*;
import com.backend.ecommerceuserservice.models.Token;
import com.backend.ecommerceuserservice.models.User;
import com.backend.ecommerceuserservice.repository.TokenRepository;
import com.backend.ecommerceuserservice.repository.UserRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements UserService{

    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder ;
    public UserServiceImpl(UserRepository userRepository,TokenRepository tokenRepository,BCryptPasswordEncoder bCryptPasswordEncoder){
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public User signup(String name, String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isPresent()){
            throw new UserAlreadyInDBException("User with Email: "+email+" Already in DB.");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password));
        return userRepository.save(user);
    }

    @Override
    public Token login(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty()){
            throw new UserNotFoundException("User with email: "+email+" not found!!");
//            return null;
        }
        User user = userOptional.get();
//        if(!user.getPassword().equals(password)){
//            throw new InvalidPasswordException("Please Enter the correct password!!");
//        }

        if(!bCryptPasswordEncoder.matches(password,user.getPassword())){
            throw new InvalidPasswordException("Please Enter the correct password!!");
        }

        Token token = new Token();
        token.setUser(user);
//        token.setTokenValue(UUID.randomUUID().toString());
        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));//gen random alphanumeric char of len 128

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,30);

        Date date = calendar.getTime();
        token.setExpiryAt(date);
        return tokenRepository.save(token);

    }

    @Override
    public void logout(String token) {
        Optional<Token> tokenOptional = tokenRepository.findByTokenValue(token);
        if(tokenOptional.isEmpty()){
            throw new TokenNotFoundException("Token with value : "+token+" Not Found!!");

        }

        Token tokenInDB = tokenOptional.get();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);

        Date date = calendar.getTime();
        tokenInDB.setExpiryAt(date);

        tokenRepository.save(tokenInDB);
    }

    @Override
    public User validateToken(String tokenValue) {
//        Token exitst in db
//        Token should not be deleted
//        Should not have expired

        Optional<Token> tokenOptional = tokenRepository.findByTokenValueAndDeletedAndExpiryAtGreaterThan(tokenValue,
                false,new Date());

        if(tokenOptional.isEmpty()){
            throw new TokenNotFound("Token with value : "+tokenValue+" not Found!!");
        }

        Token token = tokenOptional.get();
        return token.getUser();

    }
}
