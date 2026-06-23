package com.lifeevent.service.controller;

import com.lifeevent.service.dto.ApiResponse;
import com.lifeevent.service.service.BusinessService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    private final BusinessService businessService;

    public DashboardController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary() {
        return ApiResponse.ok(businessService.dashboardSummary());
    }

    @GetMapping("/month-income")
    public ApiResponse<List<Map<String, Object>>> monthIncome() {
        return ApiResponse.ok(businessService.monthIncome());
    }
}

