package com.sparksupport.product.application.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String username;
    private String password;
}