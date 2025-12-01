package com.vms.backend.controller;

import com.vms.backend.dto.DashboardMetricsDTO;
import com.vms.backend.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@CrossOrigin(origins = {"http://localhost:3000","http://localhost:3001"})
public class DashboardController {

    @Autowired
    private VisitorService visitorService;

    @GetMapping("/today")
    public DashboardMetricsDTO getTodayMetrics() {
        return visitorService.getDashboardMetrics();
    }
}
