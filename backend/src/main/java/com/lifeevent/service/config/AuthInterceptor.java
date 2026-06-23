package com.lifeevent.service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeevent.service.dto.ApiResponse;
import com.lifeevent.service.entity.Staff;
import com.lifeevent.service.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/login")) {
            return true;
        }
        Staff staff = authService.findByToken(request.getHeader("X-Auth-Token"));
        if (staff == null) {
            write(response, HttpServletResponse.SC_UNAUTHORIZED, ApiResponse.fail(401, "请先登录"));
            return false;
        }
        if (!allowed(staff.getRole(), request.getMethod(), path)) {
            write(response, HttpServletResponse.SC_FORBIDDEN, ApiResponse.fail(403, "当前角色无权访问该功能"));
            return false;
        }
        request.setAttribute("currentStaff", staff);
        return true;
    }

    private boolean allowed(String role, String method, String path) {
        if ("admin".equals(role)) {
            return true;
        }
        if (path.startsWith("/api/auth/logout") || ("GET".equals(method) && path.startsWith("/api/staff"))) {
            return true;
        }
        if ("receptionist".equals(role)) {
            return path.startsWith("/api/families")
                    || path.startsWith("/api/deceased")
                    || path.startsWith("/api/rents")
                    || path.startsWith("/api/sacrifice-books")
                    || ("GET".equals(method) && (path.startsWith("/api/graves") || path.startsWith("/api/grave-areas") || path.startsWith("/api/dashboard/summary")));
        }
        if ("finance".equals(role)) {
            return path.startsWith("/api/payments")
                    || path.startsWith("/api/dashboard")
                    || ("GET".equals(method) && path.startsWith("/api/rents"));
        }
        return false;
    }

    private void write(HttpServletResponse response, int status, ApiResponse<Void> body) throws Exception {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}

