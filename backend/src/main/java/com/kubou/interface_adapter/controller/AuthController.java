package com.kubou.interface_adapter.controller;

import com.kubou.application.repository.UserRepository;
import com.kubou.domain.entity.AppUser;
import com.kubou.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(summary = "Login as a user and get a JWT")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(Map.of("token", jwt));
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user. First user becomes ADMIN.")
    public ResponseEntity<?> register(@RequestBody RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body("Username already exists");
        }

        // Check if this is the first user
        boolean isFirstUser = userRepository.count() == 0;
        Set<String> roles = isFirstUser ? Set.of("USER", "ADMIN") : Set.of("USER");

        AppUser newUser = new AppUser(
                UUID.randomUUID().toString(),
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                roles
        );

        userRepository.save(newUser);

        return ResponseEntity.ok("User registered successfully" + (isFirstUser ? " as ADMIN" : ""));
    }

    @PostMapping("/guest")
    @Operation(summary = "Generate a guest JWT with a custom nickname")
    public ResponseEntity<?> guest(@RequestBody GuestLoginRequest guestRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("guest", "guest")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        String subject = "guest-" + UUID.randomUUID().toString();
        String jwt = tokenProvider.generateTokenWithNickname(subject, guestRequest.getNickname());
        
        return ResponseEntity.ok(Map.of("token", jwt, "nickname", guestRequest.getNickname()));
    }

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class GuestLoginRequest {
        private String nickname;
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
    }
}
