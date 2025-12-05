package com.app.auth.exceptions;

import com.app.auth.dtos.ApiError;
import com.app.auth.dtos.ErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.security.auth.message.AuthException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.security.auth.login.CredentialException;
import javax.security.auth.login.CredentialExpiredException;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException {


    @ExceptionHandler({
            UsernameNotFoundException.class,
            BadCredentialsException.class,
            CredentialExpiredException.class,
            DisabledException.class
    })
    public   ResponseEntity<ApiError>  handleAuthException(Exception exception , HttpServletRequest request){
        ApiError bad_request = ApiError.of(HttpStatus.BAD_REQUEST.value(), "BAD REQUEST", exception.getMessage(), request.getRequestURI());
return  ResponseEntity.badRequest().body(bad_request);

    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse>  handleResourceNotFoundException(ResourceNotFoundException  exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), HttpStatus.NOT_FOUND);
        return  ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>  handleResourceNotFoundException(IllegalArgumentException  exception){
        ErrorResponse errorResponse = new ErrorResponse(exception.getMessage(), HttpStatus.BAD_REQUEST);
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
