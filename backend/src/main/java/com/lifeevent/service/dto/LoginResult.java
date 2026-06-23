package com.lifeevent.service.dto;

public record LoginResult(String token, Integer staffId, String staffName, String role) {
}

