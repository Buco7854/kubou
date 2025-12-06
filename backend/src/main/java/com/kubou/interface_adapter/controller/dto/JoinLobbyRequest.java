package com.kubou.interface_adapter.controller.dto;

public class JoinLobbyRequest {
    private String pin;
    private String token;

    // Getters and Setters
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
