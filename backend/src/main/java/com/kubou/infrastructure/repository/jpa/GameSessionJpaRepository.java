package com.kubou.infrastructure.repository.jpa;

import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GameSessionJpaRepository extends JpaRepository<GameSessionData, String> {
    Optional<GameSessionData> findByPin(String pin);
}
