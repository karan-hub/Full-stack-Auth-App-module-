package com.app.auth.dtos;

public record LoginRequest(
        String email ,
        String password
) {
}
