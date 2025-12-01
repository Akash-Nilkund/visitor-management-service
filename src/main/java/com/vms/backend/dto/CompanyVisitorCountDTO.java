package com.vms.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyVisitorCountDTO {
    private String company;
    private long count;

    // Lombok will handle constructors, getters, and setters
}
