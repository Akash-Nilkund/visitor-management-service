package com.vms.backend.service;

import com.vms.backend.entity.Approval;
import com.vms.backend.entity.Visitor;
import com.vms.backend.repository.ApprovalRepository;
import com.vms.backend.repository.VisitorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VisitorServiceTest {

    @Mock
    private VisitorRepository visitorRepository;

    @Mock
    private ApprovalRepository approvalRepository;

    @Mock
    private com.vms.backend.service.ReportService reportService;

    @Mock
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private VisitorService visitorService;

    @Test
    public void testGetActiveVisitors() {
        Approval approval = new Approval();
        approval.setStatus("CHECKED_IN");
        when(approvalRepository.findByStatus("CHECKED_IN")).thenReturn(Arrays.asList(approval));

        List<Approval> active = visitorService.getActiveVisitors();
        assertEquals(1, active.size());
        assertEquals("CHECKED_IN", active.get(0).getStatus());
    }

    @Test
    public void testCheckoutVisitor() {
        Long approvalId = 1L;
        Visitor visitor = new Visitor();
        visitor.setId(1L);

        Approval approval = new Approval();
        approval.setId(approvalId);
        approval.setVisitor(visitor);
        approval.setStatus("CHECKED_IN");

        when(approvalRepository.findById(approvalId)).thenReturn(Optional.of(approval));
        when(approvalRepository.save(any(Approval.class))).thenAnswer(i -> i.getArguments()[0]);

        Approval checkedOut = visitorService.checkoutVisit(approvalId);

        assertEquals("CHECKED_OUT", checkedOut.getStatus());
        assertNotNull(checkedOut.getOutTime());
    }

    @Test
    public void testGetHistory() {
        Approval approval = new Approval();
        approval.setId(1L);
        approval.setStatus("CHECKED_OUT");
        approval.setVisitorName("John Doe");
        approval.setInTime(LocalDateTime.now());

        Visitor visitor = new Visitor();
        visitor.setEmail("john@example.com");
        visitor.setPhone("1234567890");
        approval.setVisitor(visitor);

        com.vms.backend.entity.Host host = new com.vms.backend.entity.Host();
        host.setName("Jane Smith");
        host.setEmail("jane@example.com");
        approval.setHost(host);

        com.vms.backend.entity.Company company = new com.vms.backend.entity.Company();
        company.setName("Acme Corp");
        approval.setCompany(company);

        when(approvalRepository.findAllHistory()).thenReturn(Arrays.asList(approval));

        List<com.vms.backend.dto.VisitorHistoryDTO> history = visitorService.getHistory();
        assertEquals(1, history.size());
        assertEquals("John Doe", history.get(0).getVisitorName());
        assertEquals("john@example.com", history.get(0).getVisitorEmail());
        assertEquals("CHECKED_OUT", history.get(0).getStatus());

        assertNotNull(history.get(0).getHost());
        assertEquals("Jane Smith", history.get(0).getHost().getName());
        assertEquals("jane@example.com", history.get(0).getHost().getEmail());

        assertNotNull(history.get(0).getCompany());
        assertEquals("Acme Corp", history.get(0).getCompany().getName());
    }

    @Test
    public void testGetHistoryWithSnapshot() {
        Approval approval = new Approval();
        approval.setId(1L);
        approval.setStatus("CHECKED_OUT");
        approval.setVisitorName("John Doe");
        approval.setHostName("Snapshot Host");
        approval.setCompanyName("Snapshot Company");
        approval.setInTime(LocalDateTime.now());

        // Host and Company entities are null
        approval.setHost(null);
        approval.setCompany(null);

        when(approvalRepository.findAllHistory()).thenReturn(Arrays.asList(approval));

        List<com.vms.backend.dto.VisitorHistoryDTO> history = visitorService.getHistory();
        assertEquals(1, history.size());
        assertEquals("John Doe", history.get(0).getVisitorName());

        assertNotNull(history.get(0).getHost());
        assertEquals("Snapshot Host", history.get(0).getHost().getName());
        assertNull(history.get(0).getHost().getEmail()); // Email is null because Host entity is null

        assertNotNull(history.get(0).getCompany());
        assertEquals("Snapshot Company", history.get(0).getCompany().getName());
    }
}
