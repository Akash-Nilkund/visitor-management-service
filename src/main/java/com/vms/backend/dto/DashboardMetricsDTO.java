package com.vms.backend.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
// @AllArgsConstructor - Removed to avoid duplicate with manual constructor
@NoArgsConstructor
public class DashboardMetricsDTO {
    private long totalVisitors;
    private List<CompanyVisitorCountDTO> byCompany;

    public DashboardMetricsDTO(long totalVisitors, List<CompanyVisitorCountDTO> byCompany) {
        this.totalVisitors = totalVisitors;
        this.byCompany = byCompany;
    }
}
