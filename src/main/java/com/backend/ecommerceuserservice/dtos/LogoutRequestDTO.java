package com.backend.ecommerceuserservice.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LogoutRequestDTO
{
    private String tokenValue;
}
// For invalidating the TOken during logout