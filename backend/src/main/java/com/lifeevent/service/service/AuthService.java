package com.lifeevent.service.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeevent.service.config.BusinessException;
import com.lifeevent.service.dto.LoginRequest;
import com.lifeevent.service.dto.LoginResult;
import com.lifeevent.service.entity.Staff;
import com.lifeevent.service.mapper.StaffMapper;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
    private final StaffMapper staffMapper;
    private final Map<String, Staff> sessions = new ConcurrentHashMap<>();

    public AuthService(StaffMapper staffMapper) {
        this.staffMapper = staffMapper;
    }

    public LoginResult login(LoginRequest request) {
        Staff staff = staffMapper.selectOne(new LambdaQueryWrapper<Staff>()
                .eq(Staff::getUsername, request.username())
                .eq(Staff::getPasswordHash, request.password())
                .eq(Staff::getStatus, "active"));
        if (staff == null) {
            throw new BusinessException("账号或密码错误，或账号已停用");
        }
        String token = UUID.randomUUID().toString();
        sessions.put(token, staff);
        return new LoginResult(token, staff.getStaffId(), staff.getStaffName(), staff.getRole());
    }

    public void logout(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    public Staff findByToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }
        return sessions.get(token);
    }
}

