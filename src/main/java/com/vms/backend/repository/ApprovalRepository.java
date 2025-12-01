package com.vms.backend.repository;

import com.vms.backend.dto.CompanyVisitorCountDTO;
import com.vms.backend.entity.Approval;
import com.vms.backend.entity.VisitorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ApprovalRepository extends JpaRepository<Approval, Long> {
        List<Approval> findByStatus(String status);

        long countByStatus(String status);

        @Query("SELECT a FROM Approval a WHERE a.status = :status ORDER BY a.requestDate DESC")
        List<Approval> findPendingApprovals(@Param("status") String status);

        @Query("SELECT a FROM Approval a LEFT JOIN FETCH a.visitor v LEFT JOIN FETCH a.host h LEFT JOIN FETCH h.company hc LEFT JOIN FETCH a.company c ORDER BY a.requestDate DESC")
        List<Approval> findAllHistory();

        @Query("SELECT a FROM Approval a WHERE " +
                        "(:startDate IS NULL OR a.inTime >= :startDate) AND " +
                        "(:endDate IS NULL OR a.inTime <= :endDate) AND " +
                        "(:companyId IS NULL OR a.company.id = :companyId) AND " +
                        "(:visitorType IS NULL OR a.visitor.visitorType = :visitorType)")
        List<Approval> findFilteredApprovals(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        @Param("companyId") Long companyId,
                        @Param("visitorType") VisitorType visitorType);

        @Query("SELECT COUNT(a) FROM Approval a WHERE a.inTime >= :startOfDay AND a.inTime <= :endOfDay AND a.status = 'CHECKED_IN'")
        long countVisitorsToday(@Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        @Query("SELECT new com.vms.backend.dto.CompanyVisitorCountDTO(COALESCE(a.companyName, 'Unknown'), COUNT(a)) "
                        +
                        "FROM Approval a " +
                        "WHERE a.inTime >= :startOfDay AND a.inTime <= :endOfDay AND a.status = 'CHECKED_IN' " +
                        "GROUP BY COALESCE(a.companyName, 'Unknown')")
        List<CompanyVisitorCountDTO> countVisitorsByCompanyToday(
                        @Param("startOfDay") LocalDateTime startOfDay,
                        @Param("endOfDay") LocalDateTime endOfDay);

        @Query("SELECT a.status, COUNT(a) FROM Approval a GROUP BY a.status")
        List<Object[]> countApprovalsByStatus();

        List<Approval> findByVisitorId(Long visitorId);
}
