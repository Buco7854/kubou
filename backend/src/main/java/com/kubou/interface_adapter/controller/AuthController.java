package com.kubou.interface_adapter.controller;

import com.kubou.infrastructure.security.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
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

    @PostMapping("/guest")
    @Operation(summary = "Generate a guest JWT with a custom nickname")
    public ResponseEntity<?> guest(@RequestBody GuestLoginRequest guestRequest) {
        // We still authenticate as a generic "guest" user for role purposes
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("guest", "guest")
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // But we generate the token with a unique subject (e.g., guest-UUID) and add the nickname as a claim
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

    public static class GuestLoginRequest {
        private String nickname;
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
    }
}
