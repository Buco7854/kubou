package com.kubou.infrastructure.repository.jpa.mapper;

import com.kubou.domain.entity.AppUser;
import com.kubou.infrastructure.repository.jpa.model.UserData;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserData toData(AppUser user) {
        if (user == null) return null;
        UserData data = new UserData();
        data.setId(user.getId());
        data.setUsername(user.getUsername());
        data.setPassword(user.getPassword());
        data.setRoles(user.getRoles());
        return data;
    }

    public AppUser toDomain(UserData data) {
        if (data == null) return null;
        return new AppUser(
            data.getId(),
            data.getUsername(),
            data.getPassword(),
            data.getRoles()
        );
    }
}
