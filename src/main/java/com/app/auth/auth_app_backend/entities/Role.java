package com.app.auth.auth_app_backend.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Role {
    @Id
    private UUID id =UUID.randomUUID();

    @Column(nullable = false , unique = true)
    private  String  name;
}
