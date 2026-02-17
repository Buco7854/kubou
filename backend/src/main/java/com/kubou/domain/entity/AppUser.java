package com.kubou.domain.entity;

import java.util.Set;

public class AppUser {
    private String id;
    private String username;
    private String password;
    private Set<String> roles;
    private String language;

    public AppUser() {
        this.language = "fr";
    }

    public AppUser(String id, String username, String password, Set<String> roles) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.language = "fr";
    }

    public AppUser(String id, String username, String password, Set<String> roles, String language) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.roles = roles;
        this.language = language != null ? language : "fr";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Set<String> getRoles() { return roles; }
    public void setRoles(Set<String> roles) { this.roles = roles; }
    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }
}
