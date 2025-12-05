package com.app.auth.Security;

import com.app.auth.entities.Provider;
import com.app.auth.entities.RefreshToken;
import com.app.auth.entities.User;
import com.app.auth.repositories.RefreshTokenRepository;
import com.app.auth.repositories.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import  org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.config.annotation.rsocket.RSocketSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class Oauth2SuccessHandler implements AuthenticationSuccessHandler {

    @Value("${app.auth.frontend.success-redirect}")
    private String  frontendSuccessUrl;

    private  final UserRepository userRepository;
    private  final JwtService jwtService ;
    private  final  CookieService  cookieService ;
    private  final RefreshTokenRepository refreshTokenRepository;
    private  final  Logger logger =  LoggerFactory.getLogger(this.getClass());
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        logger.info("Successfully  O Authentication");
        logger.info(authentication.toString());
        OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();
        String registrationId = "unknown";
        if (authentication instanceof OAuth2AuthenticationToken token){
            registrationId = token.getAuthorizedClientRegistrationId();
        }
        logger.info(registrationId);
        logger.info("user" + oAuth2User.getAttributes().toString());
        User user ;
        switch (registrationId){
            case  "google"-> {
                String googleId = oAuth2User.getAttributes().getOrDefault("sub", "").toString();

                String email = oAuth2User.getAttributes().getOrDefault("email", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("name", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("picture", "").toString();

                user = userRepository.findByEmail(email)
                        .map(existingUser -> {
                            logger.info("User already exists in DB");
                            logger.info(existingUser.toString());
                            return existingUser;
                        })
                        .orElseGet(
                                () -> {
                                    User newUser = User.builder()
                                            .name(name)
                                            .image(picture)
                                            .email(email)
                                            .enable(true)
                                            .providerId(googleId)
                                            .provider(Provider.GOOGLE)
                                            .build();
                                    logger.info("Saving new Google user");
                                    return userRepository.save(newUser);
                                }
                        );
            }

            case "github"->{
                String email = (String) oAuth2User.getAttributes().getOrDefault("email", "");
                String githubId =  oAuth2User.getAttributes().getOrDefault("id", "").toString();
                String name = oAuth2User.getAttributes().getOrDefault("login", "").toString();
                String picture = oAuth2User.getAttributes().getOrDefault("avatar_url", "").toString();


                if (email == null)
                    email = name+"@null.com";

                String finalEmail = email;
                user = userRepository.findByEmail(email)
                        .map(existingUser -> {
                            logger.info("User already exists in DB");
                            logger.info(existingUser.toString());
                            return existingUser;
                        })
                        .orElseGet(
                                () -> {
                                    User newUser = User.builder()
                                            .name(name)
                                            .image(picture)
                                            .email(finalEmail)
                                            .enable(true)
                                            .providerId(githubId)
                                            .provider(Provider.GITHUB)
                                            .build();
                                    logger.info("Saving new Google user");
                                    return userRepository.save(newUser);
                                }
                        );
            }

            default -> throw new RuntimeException("Invalid Registration Id");

        }
        String jti = String.valueOf(UUID.randomUUID());
        RefreshToken refreshTokenOb = RefreshToken.builder()
                .jti(jti)
                .user(user)
                .revoked(false)
                .createdAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
                .build();
        refreshTokenRepository.save(refreshTokenOb);
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());
        cookieService.attachRefreshCookie(response, refreshToken , (int) jwtService.getRefreshTtlSeconds());

        logger.info("Redirecting to frontend success page...");
        response.sendRedirect(frontendSuccessUrl + "?token=" + accessToken);
    }
}
