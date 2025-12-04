package com.app.auth.auth_app_backend.controllers;

import com.app.auth.auth_app_backend.Security.CookieService;
import com.app.auth.auth_app_backend.Security.JwtService;
import com.app.auth.auth_app_backend.dtos.LoginRequest;
import com.app.auth.auth_app_backend.dtos.RefreshTokenRequest;
import com.app.auth.auth_app_backend.dtos.TokenResponse;
import com.app.auth.auth_app_backend.dtos.UserDto;
import com.app.auth.auth_app_backend.entities.RefreshToken;
import com.app.auth.auth_app_backend.entities.User;
import com.app.auth.auth_app_backend.repositories.RefreshTokenRepository;
import com.app.auth.auth_app_backend.repositories.UserRepository;
import com.app.auth.auth_app_backend.services.AuthService;
import jakarta.persistence.PreUpdate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
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
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
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

    @PostMapping("/logout")
    public  ResponseEntity<Void> logout(HttpServletRequest request ,HttpServletResponse response){
        readRefreshTokenFromRequest(null ,request).ifPresent(token->{
            try {
                if (jwtService.isRefreshToken(token)){
                    String jit = jwtService.getJti(token);
                    refreshTokenRepository.findByJti(jit)
                            .ifPresent(refreshToken -> {
                                refreshToken.setRevoked(true);
                                refreshTokenRepository.save(refreshToken);
                            });
                }
            } catch (Exception _) { }

        });

        cookieService.clearRefreshCookie(response);
        cookieService.addNoStoreHeaders(response);
        SecurityContextHolder.clearContext();
        return ResponseEntity.status(HttpStatus.NO_CONTENT ).build();

    }



    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refreshToken(@RequestBody( required = false) RefreshTokenRequest body , HttpServletResponse response , HttpServletRequest request){

        String refreshToken = readRefreshTokenFromRequest(body , request).orElseThrow(()->
                new BadCredentialsException("Invalid Refresh Token"));

        if (!jwtService.isRefreshToken(refreshToken))
            throw  new BadCredentialsException("Invalid Refresh Token Type ");

        String jti = jwtService.getJti(refreshToken);
        UUID userId = jwtService.getUserId(refreshToken);

        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti).orElseThrow(() -> new BadCredentialsException("Invalid Refresh Token"));

        if (storedRefreshToken.isRevoked())
            throw  new BadCredentialsException("Refresh Token Expired or  Revoked");

        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now()))
            throw  new BadCredentialsException("Refresh Token Expired ");

        if (!storedRefreshToken.getUser().getId().equals(userId))
            throw  new BadCredentialsException("Refresh Token Doest Belongs To This User ");

        storedRefreshToken.setRevoked(true);
        String  newJit = UUID.randomUUID().toString();
        storedRefreshToken.setReplacedByToken(newJit);
        refreshTokenRepository.save(storedRefreshToken);


        User user = storedRefreshToken.getUser();
        var newRefreshTokenOb = RefreshToken.builder()
                .jti(newJit)
                .user(user)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getAccessTtlSeconds()))
                .revoked(false)
                .build();

        refreshTokenRepository.save(newRefreshTokenOb);

        String newAccessToken = jwtService.generateAccessToken(user);
        String  newRefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenOb.getJti());

        cookieService.attachRefreshCookie(response,newRefreshToken , (int) jwtService.getRefreshTtlSeconds());
        cookieService.addNoStoreHeaders(response);

        return  ResponseEntity.ok(TokenResponse.of(
                newAccessToken ,
                newRefreshToken,
                jwtService.getAccessTtlSeconds(),
                modelMapper.map(user , UserDto.class)
        ));

    }

    private Optional<String> readRefreshTokenFromRequest(RefreshTokenRequest body, HttpServletRequest request) {
//        1. form cookie
        if (request.getCookies() != null){
            Optional<String> fromCookie = Arrays.stream(request.getCookies())
                    .filter(c -> cookieService.getRefreshTokenCookieName().equals(c.getName()))
                    .map(Cookie::getValue)
                    .filter(v -> !v.isBlank())
                    .findFirst();
            if (fromCookie.isPresent())
                return  fromCookie;
        }

//        2. body
        if (body != null && body.refreshToken() != null  &&  !body.refreshToken().isBlank())
            return Optional.of(body.refreshToken());

//    3 .  custom
        String refreshHeader = request.getHeader("X-Refresh-Token");
        if (refreshHeader != null && !refreshHeader.isBlank())
            return Optional.of(refreshHeader.trim());

//        Authorization
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.regionMatches(true,0,"Bearer" , 0,6)){
            String candidate = authHeader.substring(7).trim();
            if ( !candidate.isEmpty()){
                try{
                    if (jwtService.isRefreshToken(candidate)){
                        return  Optional.of(candidate);
                    }
                }catch (Exception  e){

                }
            }

        }
        return  Optional.empty();

    }

    @PostMapping("/register")
    public ResponseEntity<UserDto>  registerUser(@RequestBody UserDto  userDto ){
        return  ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

}
