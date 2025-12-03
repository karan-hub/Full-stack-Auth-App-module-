package com.app.auth.auth_app_backend.dtos;

import lombok.Builder;
import org.springframework.http.HttpStatus;


public record ErrorResponse(
        String massage,
        HttpStatus status

) {}
