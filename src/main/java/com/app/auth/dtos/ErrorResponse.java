package com.app.auth.dtos;

import lombok.Builder;
import org.springframework.http.HttpStatus;


public record ErrorResponse(
        String massage,
        HttpStatus status

) {}
