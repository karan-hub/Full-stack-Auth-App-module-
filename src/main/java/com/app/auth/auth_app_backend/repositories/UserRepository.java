package com.app.auth.auth_app_backend.repositories;

import com.app.auth.auth_app_backend.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository   extends  JpaRepository<User , UUID> {

    Optional<User> findByzEmail(UUID uuid);

     boolean existsByEmail(UUID uuid);
}
