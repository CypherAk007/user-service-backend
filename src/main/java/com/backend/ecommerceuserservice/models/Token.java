package com.backend.ecommerceuserservice.models;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@Entity
public class Token extends Base{
    private String tokenValue;
    private Date expiryAt;

    @ManyToOne
    private User user;

    //User --- Token
        //1---M
        //1---1
    //User:Token  = 1:M
}
