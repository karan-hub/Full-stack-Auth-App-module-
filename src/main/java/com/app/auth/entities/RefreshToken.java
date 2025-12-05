package com.app.auth.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_token", indexes = {
        @Index(name = "token_index"  ,columnList = "jti" , unique = true),
        @Index(name = "user_index" , columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Column(name = "jti" , nullable = false , updatable = false , unique = true)
    private  String jti ;

    @ManyToOne(optional = false , fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id" , nullable = false , updatable = false)
    private  User user ;

    @Column(updatable = false , nullable = false)
    private Instant createdAt ;
    @Column(updatable = false, nullable = false)
    private Instant expiresAt ;

    @Column(name = "replaced_by_token")
    private  boolean revoked;

    @Column(name = "revoked")
    private  String  replacedByToken;

}
