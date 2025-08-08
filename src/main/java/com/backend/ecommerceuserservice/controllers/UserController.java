package com.backend.ecommerceuserservice.controllers;

import com.backend.ecommerceuserservice.dtos.*;
import com.backend.ecommerceuserservice.models.Token;
import com.backend.ecommerceuserservice.models.User;
import com.backend.ecommerceuserservice.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {
    private UserService userService;
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserDTO signup(@RequestBody SignUpRequestDTO signUpRequestDTO){
        User user = userService.signup(signUpRequestDTO.getName(), signUpRequestDTO.getEmail(),signUpRequestDTO.getPassword());

        return UserDTO.from(user);
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO){
        Token token = userService.login(loginRequestDTO.getEmail(),loginRequestDTO.getPassword());
        LoginResponseDTO loginResponseDTO = new LoginResponseDTO();
        loginResponseDTO.setToken(token.getTokenValue());
        return loginResponseDTO;
    }

//    @PostMapping("/logout")
//    public ResponseEntity<Boolean> logout(@RequestBody LogoutRequestDTO logoutRequestDTO){
//        userService.logout(logoutRequestDTO.getTokenValue());
//        return new ResponseEntity<>(true,HttpStatus.OK); // 204 No Content
//    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody LogoutRequestDTO logoutRequestDTO){
        userService.logout(logoutRequestDTO.getTokenValue());
        return ResponseEntity.ok("Logout successful.");
    }
//    @GetMapping("/validate/{token}")
//    public ResponseEntity<Boolean> validateToken(@PathVariable("token") String token){
//        User user = userService.validateToken(token);
//        ResponseEntity<Boolean> responseEntity;
//
//        if(user==null){
//            responseEntity = new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
//        }else{
//            responseEntity = new ResponseEntity<>(true,HttpStatus.OK);
//        }
//        return responseEntity;
//
//    }

////    CHANGING THE TOKEN GIVEN AT URL PATH TO REQUEST HEADERS
//// @RequestHeader(HttpHeaders.AUTHORIZATION) - > name the header Authorization and add Bearer- Best Practice
    @PostMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader(HttpHeaders.AUTHORIZATION) String token){
        if(token.startsWith("Bearer ")){
//            token  = token.substring(7);
            token = token.replace("Bearer ","");
        }
        User user = userService.validateToken(token);
        ResponseEntity<Boolean> responseEntity;

        if(user==null){
            responseEntity = new ResponseEntity<>(false, HttpStatus.UNAUTHORIZED);
        }else{
            responseEntity = new ResponseEntity<>(true,HttpStatus.OK);
        }
        return responseEntity;

    }



}
