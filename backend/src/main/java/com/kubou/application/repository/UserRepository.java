package com.kubou.application.repository;

import com.kubou.domain.entity.AppUser;
import java.util.Optional;

public interface UserRepository {
    Optional<AppUser> findByUsername(String username);
    AppUser save(AppUser user);
    boolean existsByUsername(String username);
    long count();
}
