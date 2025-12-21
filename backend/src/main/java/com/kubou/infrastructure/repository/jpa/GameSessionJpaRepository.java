package com.kubou.infrastructure.repository.jpa;

import com.kubou.infrastructure.repository.jpa.model.GameSessionData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameSessionJpaRepository extends JpaRepository<GameSessionData, String> {
    Optional<GameSessionData> findByPin(String pin);
    List<GameSessionData> findByHostId(String hostId);

    @Query("SELECT gs FROM GameSessionData gs JOIN gs.players p WHERE p.userId = :userId AND gs.state != 'FINISHED'")
    List<GameSessionData> findActiveSessionsByPlayerUserId(@Param("userId") String userId);
}
