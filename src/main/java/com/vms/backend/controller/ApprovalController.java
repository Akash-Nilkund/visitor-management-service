package com.vms.backend.controller;

import com.vms.backend.entity.Approval;
import com.vms.backend.service.VisitorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ApprovalController {

    @Autowired
    private VisitorService visitorService;

    // PATCH /api/approvals/{id}/approve
    @PatchMapping("/api/approvals/{id}/approve")
    public Approval approveRequest(@PathVariable Long id) {
        return visitorService.approveVisit(id);
    }

    // PATCH /api/approvals/{id}/reject
    @PatchMapping("/api/approvals/{id}/reject")
    public Approval rejectRequest(@PathVariable Long id) {
        return visitorService.rejectVisit(id);
    }

    // GET /api/admin/approvals?status=PENDING
    @GetMapping("/api/admin/approvals")
    public List<Approval> getPendingApprovals(@RequestParam(required = false) String status) {
        if ("PENDING".equalsIgnoreCase(status)) {
            return visitorService.getPendingApprovals();
        }
        // Fallback or other statuses if needed
        return java.util.Collections.emptyList();
    }

    // GET /api/admin/history
    @GetMapping("/api/admin/history")
    public List<com.vms.backend.dto.VisitorHistoryDTO> getHistory() {
        return visitorService.getHistory();
    }

    // GET /api/admin/active-visitors
    @GetMapping("/api/admin/active-visitors")
    public List<Approval> getActiveVisitors() {
        return visitorService.getActiveVisitors();
    }

}
