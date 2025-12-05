package com.app.auth.repositories;

import com.app.auth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository   extends  JpaRepository<User , UUID> {

    Optional<User> findByEmail(String mail);

     boolean existsByEmail(String  mail);
}
