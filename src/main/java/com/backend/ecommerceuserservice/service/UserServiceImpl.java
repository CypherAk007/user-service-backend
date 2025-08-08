package com.backend.ecommerceuserservice.service;

import com.backend.ecommerceuserservice.exceptions.*;
import com.backend.ecommerceuserservice.models.Token;
import com.backend.ecommerceuserservice.models.User;
import com.backend.ecommerceuserservice.repository.TokenRepository;
import com.backend.ecommerceuserservice.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class UserServiceImpl implements UserService{
//    M1
//    private static final SecretKey SECRET_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

//    M2
//    PUT YOU OWN SECRET KEY
//    HS256 (HMAC + SHA-256) requires a key that's at least 256 bits (32 bytes) long.
//    You're using a readable string and converting it to bytes using UTF-8.
//            Keys.hmacShaKeyFor(byte[]) ensures that the key is properly wrapped as a SecretKey suitable for JJWT's signing.

//    private static final String SECRET_KEY_STRING = "your-super-secret-long-engough-key-for-hs256-encoding";
//    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    private static final long EXPIRATION_TIME_IN_MS = 10*60*60*1000;// 10 hours

    private UserRepository userRepository;
    private TokenRepository tokenRepository;
    BCryptPasswordEncoder bCryptPasswordEncoder ;

//    Create a bean and inject it
    private final SecretKey secretKey;

    public UserServiceImpl(UserRepository userRepository,TokenRepository tokenRepository,BCryptPasswordEncoder bCryptPasswordEncoder,SecretKey secretKey){
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.secretKey = secretKey;
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
        Date now = new Date();
        Date expiryDate = new Date(now.getTime()+EXPIRATION_TIME_IN_MS);

        Map<String,Object> claims = new HashMap<>();//payload
        claims.put("userId",user.getId());
        claims.put("email",user.getEmail());

        String jsonString = Jwts.builder()
                .setClaims(claims)
                .setSubject(user.getEmail())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(secretKey,SignatureAlgorithm.HS256)//sign with HS256 - part C
                .compact();// combine all above

        Token token = new Token();
        token.setUser(user);
//        token.setTokenValue(UUID.randomUUID().toString());
//        token.setTokenValue(RandomStringUtils.randomAlphanumeric(128));//gen random alphanumeric char of len 128
        token.setTokenValue(jsonString);

//        Alternative way to generate expiryDate
//        Calendar calendar = Calendar.getInstance();
//        calendar.add(Calendar.DATE,30);
//
//        Date date = calendar.getTime();

        token.setExpiryAt(expiryDate);
//        return tokenRepository.save(token);//no need to save the token in db - whole point of using jwt - self validating
        return token;

    }

    @Override
    public void logout(String token) {
        Optional<Token> tokenOptional = tokenRepository.findByTokenValue(token);
        if(tokenOptional.isEmpty()){
            throw new TokenNotFoundException(" Token Validation Failed: Token with value : "+token+" Not Found!!");

        }

        Token tokenInDB = tokenOptional.get();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,-1);

        Date date = calendar.getTime();
        tokenInDB.setExpiryAt(date);

        tokenRepository.save(tokenInDB);
    }

//    @Override
//    public User validateToken(String tokenValue) {
////        Token exitst in db
////        Token should not be deleted
////        Should not have expired
//
//        Optional<Token> tokenOptional = tokenRepository.findByTokenValueAndDeletedAndExpiryAtGreaterThan(tokenValue,
//                false,new Date());
//
//        if(tokenOptional.isEmpty()){
//            throw new TokenNotFound("Token with value : "+tokenValue+" not Found!!");
//        }
//
//        Token token = tokenOptional.get();
//        return token.getUser();
//
//    }

    @Override
    public User validateToken(String tokenValue){
        if(tokenValue==null || tokenValue.isEmpty()){
            return null;
        }


        ///  no need to give explict SH256 we can derive it from A part
        Claims claims;
        try{
            claims = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build()
                    .parseClaimsJws(tokenValue)
                    .getBody();
        }catch (io.jsonwebtoken.ExpiredJwtException ex){
            System.out.println("Token Validation Failed: Expired JWT Token: "+ ex.getMessage());
            return null;
        }catch (io.jsonwebtoken.JwtException ex){
            System.out.println("Token Validation Failed: Invalid JWT Token: "+ ex.getMessage());
            return null;
        }

        String email = claims.getSubject();
        if(email==null || email.isEmpty()){
            System.out.println("Token Validation Failed: Email is null or Empty!!");
            return null;
        }

        Optional<User> userOptional = userRepository.findByEmail(email);
        if(userOptional.isEmpty() || userOptional.get().isDeleted()){
            System.out.println("User From Email in Token does not exit!!");
            return null;
        }

        return userOptional.get();
    }
}
