package com.lifeevent.service.controller;

import com.lifeevent.service.dto.ApiResponse;
import com.lifeevent.service.dto.LoginRequest;
import com.lifeevent.service.dto.LoginResult;
import com.lifeevent.service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResult> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest request) {
        authService.logout(request.getHeader("X-Auth-Token"));
        return ApiResponse.ok(null);
    }
}

