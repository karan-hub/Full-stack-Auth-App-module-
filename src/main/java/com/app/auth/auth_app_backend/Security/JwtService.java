package com.app.auth.auth_app_backend.Security;

import com.app.auth.auth_app_backend.entities.Role;
import com.app.auth.auth_app_backend.entities.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Getter
@Setter
public class JwtService {

    private   final SecretKey key;
    private  final  long AccessTtlSeconds;
    private  final  long RefreshTtlSeconds;
    private  final String issuer;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {

        if (secret ==null || secret.length() <64 || secret.trim().isEmpty())
            throw  new IllegalArgumentException("Invalid  Secrete");
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        AccessTtlSeconds = accessTtlSeconds;
        RefreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    public  String generateAccessToken(User user){
        Instant now= Instant.now();
        List<String>  roles = user.getRoles() == null
                ? List.of()
                : user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(AccessTtlSeconds)))
                .claims(Map.of(
                        "email" , user.getEmail(),
                        "roles" , roles,
                        "typ" ,"access"
                ))
                .signWith(key , SignatureAlgorithm.HS512)
                .compact();
    }

    public  String generateRefreshToken(User user , String  jti ){
        Instant now= Instant.now();

        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(AccessTtlSeconds)))
                .claim("typ" , "refresh")
                .signWith(key , SignatureAlgorithm.HS512)
                .compact();
    }


    public Jws<Claims> parse(String token) {
        try {
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        } catch (JwtException e) {
            throw e;
        }
    }

    public boolean isAccessToken(String token) {
        Claims c = parse(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public boolean isRefreshToken(String token) {
        Claims c = parse(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token) {
        Claims c = parse(token).getPayload();
        return UUID.fromString(c.getSubject());
    }

    public String getJti(String token) {
        return parse(token).getPayload().getId();
    }

}
