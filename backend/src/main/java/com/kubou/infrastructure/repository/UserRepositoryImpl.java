package com.kubou.infrastructure.repository;

import com.kubou.application.repository.UserRepository;
import com.kubou.domain.entity.AppUser;
import com.kubou.infrastructure.repository.jpa.UserJpaRepository;
import com.kubou.infrastructure.repository.jpa.mapper.UserMapper;
import com.kubou.infrastructure.repository.jpa.model.UserData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class UserRepositoryImpl implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserMapper mapper;

    public UserRepositoryImpl(UserJpaRepository jpaRepository, UserMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<AppUser> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public AppUser save(AppUser user) {
        UserData data = mapper.toData(user);
        return mapper.toDomain(jpaRepository.save(data));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return jpaRepository.count();
    }
}
