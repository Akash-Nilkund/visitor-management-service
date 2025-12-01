package com.vms.backend.controller;

import com.vms.backend.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class WebSocketController {

    @Autowired
    private ReportService reportService;

    @SubscribeMapping("/dashboard/visitors")
    public Map<String, Object> sendInitialDashboardStats() {
        return reportService.getDashboardStats();
    }
}
