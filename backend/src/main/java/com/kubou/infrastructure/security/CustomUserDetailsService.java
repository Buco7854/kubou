package com.kubou.infrastructure.security;

import com.kubou.application.repository.UserRepository;
import com.kubou.domain.entity.AppUser;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Handle guest users
        // Guest usernames start with "guest-" followed by a UUID
        if (username.startsWith("guest-") || "guest".equals(username)) {
            return User.withUsername(username) // Use the actual username (guest-UUID)
                    .password(passwordEncoder.encode("guest"))
                    .roles("GUEST")
                    .build();
        }

        // Try to find by username first (standard login)
        Optional<AppUser> appUserOpt = userRepository.findByUsername(username);
        
        if (appUserOpt.isEmpty()) {
             throw new UsernameNotFoundException("User not found: " + username);
        }

        AppUser appUser = appUserOpt.get();

        // Ensure roles are not null
        String[] roles = appUser.getRoles() != null 
                ? appUser.getRoles().toArray(new String[0]) 
                : new String[]{"USER"};

        return User.withUsername(appUser.getUsername())
                .password(appUser.getPassword())
                .roles(roles)
                .build();
    }
}
