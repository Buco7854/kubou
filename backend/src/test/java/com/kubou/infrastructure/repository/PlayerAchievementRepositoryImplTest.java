package com.kubou.infrastructure.repository;

import com.kubou.domain.entity.AchievementType;
import com.kubou.domain.entity.PlayerAchievement;
import com.kubou.infrastructure.repository.jpa.PlayerAchievementJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.PlayerAchievementMapper;
import com.kubou.infrastructure.repository.jpa.model.PlayerAchievementData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlayerAchievementRepositoryImplTest {

    @Mock
    private PlayerAchievementJpaRepository jpaRepository;
    @Mock
    private PlayerAchievementMapper mapper;

    @InjectMocks
    private PlayerAchievementRepositoryImpl repository;

    @Test
    void save_ShouldMapAndSave() {
        PlayerAchievement achievement = new PlayerAchievement("1", "p1", AchievementType.SNIPER, LocalDateTime.now());
        PlayerAchievementData data = new PlayerAchievementData();
        
        when(mapper.toData(achievement)).thenReturn(data);

        repository.save(achievement);

        verify(jpaRepository).save(data);
    }

    @Test
    void findByPlayerId_ShouldReturnMappedList() {
        PlayerAchievementData data = new PlayerAchievementData();
        PlayerAchievement domain = new PlayerAchievement("1", "p1", AchievementType.SNIPER, LocalDateTime.now());

        when(jpaRepository.findByPlayerId("p1")).thenReturn(List.of(data));
        when(mapper.toDomain(data)).thenReturn(domain);

        List<PlayerAchievement> result = repository.findByPlayerId("p1");

        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }

    @Test
    void findAll_ShouldReturnMappedList() {
        PlayerAchievementData data = new PlayerAchievementData();
        PlayerAchievement domain = new PlayerAchievement("1", "p1", AchievementType.SNIPER, LocalDateTime.now());

        when(jpaRepository.findAll()).thenReturn(List.of(data));
        when(mapper.toDomain(data)).thenReturn(domain);

        List<PlayerAchievement> result = repository.findAll();

        assertEquals(1, result.size());
        assertEquals(domain, result.get(0));
    }
}
