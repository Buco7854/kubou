package com.kubou.infrastructure.repository.jpa;

import com.kubou.infrastructure.repository.jpa.model.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserData, String> {
    Optional<UserData> findByUsername(String username);
    boolean existsByUsername(String username);
}
