package com.vms.backend.service;

import com.vms.backend.repository.VisitorRepository;
import com.vms.backend.repository.ApprovalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private ApprovalRepository approvalRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        java.time.LocalDateTime start = java.time.LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        java.time.LocalDateTime end = java.time.LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        // Basic Counters
        stats.put("totalToday", approvalRepository.countVisitorsToday(start, end));
        stats.put("active", approvalRepository.countByStatus("CHECKED_IN"));
        stats.put("pending", approvalRepository.countByStatus("PENDING"));

        // Chart Data: Visitors by Status
        List<Object[]> statusCounts = approvalRepository.countApprovalsByStatus();
        Map<String, Long> chartData = new HashMap<>();
        for (Object[] row : statusCounts) {
            chartData.put(row[0].toString(), (Long) row[1]);
        }
        stats.put("chartData", chartData);

        return stats;
    }
}
