package com.app.auth.auth_app_backend.controllers;

import com.app.auth.auth_app_backend.Security.CookieService;
import com.app.auth.auth_app_backend.Security.JwtService;
import com.app.auth.auth_app_backend.dtos.LoginRequest;
import com.app.auth.auth_app_backend.dtos.TokenResponse;
import com.app.auth.auth_app_backend.dtos.UserDto;
import com.app.auth.auth_app_backend.entities.RefreshToken;
import com.app.auth.auth_app_backend.entities.User;
import com.app.auth.auth_app_backend.repositories.RefreshTokenRepository;
import com.app.auth.auth_app_backend.repositories.UserRepository;
import com.app.auth.auth_app_backend.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private    final AuthService  authService ;
    private  final RefreshTokenRepository refreshTokenRepository ;
    private  final CookieService cookieService ;

    private  final AuthenticationManager manager ;
    private  final UserRepository  userRepository ;
    private  final JwtService jwtService ;
    private  final ModelMapper modelMapper ;


    @PostMapping("/login")
    public  ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request , HttpServletResponse response){
        Authentication authenticate = authenticate(request);

        User user = userRepository.findByEmail(request.email()).orElseThrow(() ->
                new BadCredentialsException("Invalid Username or Password "));
        if (!user.isEnable()){
            throw  new DisabledException("User is Disebled ");
        }

        String  jti = UUID.randomUUID().toString();
     var refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getAccessTtlSeconds()))
                        .build();

     refreshTokenRepository.save(refreshTokenOb);

     String accessToken =jwtService.generateAccessToken(user);
     String refreshToken = jwtService.generateRefreshToken(user , refreshTokenOb.getJti());

     cookieService.attachRefreshCookie(response , refreshToken ,(int) jwtService.getAccessTtlSeconds());


        TokenResponse tokenResponse = TokenResponse.of(accessToken, refreshToken, jwtService.getAccessTtlSeconds(), modelMapper.map(user, UserDto.class));
        return  ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest request) {
        try {
                return  manager.authenticate(new UsernamePasswordAuthenticationToken(request.email() , request.password()));
        }catch (Exception e){
            throw  new BadCredentialsException("Invalid Username or Password") ;
        }
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto>  registerUser(@RequestBody UserDto  userDto ){
        return  ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

}
