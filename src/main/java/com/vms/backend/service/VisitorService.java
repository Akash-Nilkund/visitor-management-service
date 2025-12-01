package com.vms.backend.service;

import com.vms.backend.dto.VisitorRequest;
import com.vms.backend.dto.VisitorDTO;
import com.vms.backend.entity.Approval;
import com.vms.backend.entity.Host;
import com.vms.backend.entity.Visitor;
import com.vms.backend.entity.VisitorType;
import com.vms.backend.repository.ApprovalRepository;
import com.vms.backend.repository.HostRepository;
import com.vms.backend.repository.VisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import net.coobird.thumbnailator.Thumbnails;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class VisitorService {

    @Autowired
    private VisitorRepository visitorRepository;

    @Autowired
    private ApprovalRepository approvalRepository;

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private ReportService reportService;

    @Value("${file.upload-dir}")
    private String photoDir;

    @Autowired
    private org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    // ----------------------- 1. IMMEDIATE CHECK-IN -----------------------
    public Approval checkInVisitor(VisitorRequest request, MultipartFile photoFile) {
        if (request.getHostId() == null) {
            throw new RuntimeException("Host ID is required");
        }

        Host host = hostRepository.findById(request.getHostId())
                .orElseThrow(() -> new RuntimeException("Host not found"));

        // 1. Process Photo (Directly from MultipartFile)
        String imagePath = saveAndResizeImage(photoFile, request.getFullName());

        // 2. Save Visitor (Profile)
        Visitor visitor = new Visitor();
        visitor.setName(request.getFullName());
        visitor.setEmail(request.getEmail());
        visitor.setPhone(request.getPhone());
        visitor.setIdProofNumber(request.getIdProofNumber());
        visitor.setIdProof(request.getIdProof());
        visitor.setGender(request.getGender());
        visitor.setImagePath(imagePath);
        visitor.setVisitorType(request.getVisitorType());
        visitor.setHost(host);
        visitor.setDuration(request.getDuration());

        Visitor savedVisitor = visitorRepository.save(visitor);

        // 3. Create Approval (Visit)
        Approval approval = new Approval();
        approval.setVisitor(savedVisitor);
        approval.setHost(host);
        approval.setCompany(host.getCompany());
        approval.setVisitorName(savedVisitor.getName());
        approval.setHostName(host.getName());
        if (host.getCompany() != null) {
            approval.setCompanyName(host.getCompany().getName());
        }
        approval.setStatus("CHECKED_IN");
        approval.setInTime(LocalDateTime.now());
        approval.setRequestDate(LocalDateTime.now());

        Approval savedApproval = approvalRepository.save(approval);

        // 4. Generate PDF
        String pdfPath = pdfService.generateVisitorPassPdf(savedApproval);

        // 5. Email to Visitor & Host
        String subject = "Visitor Pass - Checked In";
        String body = "<p>Hello,<br>Visitor <b>" + savedVisitor.getName()
                + "</b> has checked in successfully.<br>Pass is attached.</p>";

        emailService.sendEmailWithAttachment(savedVisitor.getEmail(), subject, body, pdfPath);
        emailService.sendEmailWithAttachment(host.getEmail(), subject, body, pdfPath);

        // Broadcast Live Metrics
        broadcastDashboardUpdate();

        return savedApproval;
    }

    // ----------------------- 2. PRE-REGISTRATION -----------------------
    public Approval preRegisterVisitor(VisitorDTO request, MultipartFile photoFile) {
        if (request.getHostId() == null) {
            throw new RuntimeException("Host ID is required");
        }

        Host host = hostRepository.findById(request.getHostId())
                .orElseThrow(() -> new RuntimeException("Host not found"));

        // 1. Process Photo
        String imagePath = saveAndResizeImage(photoFile, request.getFullName());

        // 2. Save Visitor
        Visitor visitor = new Visitor();
        visitor.setName(request.getFullName());
        visitor.setEmail(request.getEmail());
        visitor.setPhone(request.getPhone());
        visitor.setIdProofNumber(request.getIdProofNumber());
        visitor.setIdProof(request.getIdProof());
        visitor.setGender(request.getGender());
        visitor.setImagePath(imagePath);
        visitor.setVisitorType(request.getVisitorType());
        visitor.setHost(host);
        visitor.setDuration(request.getDuration());

        Visitor savedVisitor = visitorRepository.save(visitor);

        // 3. Create Approval (PENDING)
        Approval approval = new Approval();
        approval.setVisitor(savedVisitor);
        approval.setHost(host);
        approval.setCompany(host.getCompany());
        approval.setVisitorName(savedVisitor.getName());
        approval.setHostName(host.getName());
        if (host.getCompany() != null) {
            approval.setCompanyName(host.getCompany().getName());
        }
        approval.setStatus("PENDING");
        approval.setRequestDate(LocalDateTime.now());

        Approval savedApproval = approvalRepository.save(approval);

        // 4. Email to Host ONLY (Approval Request)
        String subject = "Visitor Approval Request";
        String body = "<p>Hello " + host.getName() + ",<br>" +
                "Visitor <b>" + savedVisitor.getName() + "</b> (" + savedVisitor.getVisitorType()
                + ") has pre-registered and is waiting for your approval.<br>" +
                "Please log in to the dashboard to approve or reject.</p>";

        emailService.sendGenericEmail(host.getEmail(), subject, body);

        return savedApproval;
    }

    // ----------------------- 3. APPROVE VISIT -----------------------
    public Approval approveVisit(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (!"PENDING".equalsIgnoreCase(approval.getStatus())) {
            // throw new RuntimeException("Request is not pending approval");
            // Allow idempotency or re-approval? Requirement says "approveRequest".
        }

        approval.setStatus("CHECKED_IN");
        approval.setInTime(LocalDateTime.now());
        Approval savedApproval = approvalRepository.save(approval);

        // Generate PDF
        String pdfPath = pdfService.generateVisitorPassPdf(savedApproval);

        // Email to Visitor & Host
        String subject = "Visitor Request Approved";
        String body = "<p>The visit for <b>" + approval.getVisitorName()
                + "</b> has been approved.<br>Pass is attached.</p>";

        emailService.sendEmailWithAttachment(approval.getVisitor().getEmail(), subject, body, pdfPath);
        emailService.sendEmailWithAttachment(approval.getHost().getEmail(), subject, body, pdfPath);

        broadcastDashboardUpdate();

        return savedApproval;
    }

    // ----------------------- 4. REJECT VISIT -----------------------
    public Approval rejectVisit(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        approval.setStatus("REJECTED");
        Approval savedApproval = approvalRepository.save(approval);

        // Send Rejection Email
        if (approval.getVisitor() != null && approval.getVisitor().getEmail() != null) {
            emailService.sendRejection(approval.getVisitor().getEmail(), approval.getVisitorName());
        }

        return savedApproval;
    }

    // ----------------------- 5. CHECK-IN (Existing Approved Visit)
    // -----------------------
    public Approval checkInApprovedVisitor(Long visitorId) {

        if (!visitorRepository.existsById(visitorId)) {
            throw new RuntimeException("Visitor not found");
        }

        // Find latest approval using efficient query
        List<Approval> approvals = approvalRepository.findByVisitorId(visitorId);

        Approval approval = approvals.stream()
                .sorted((a1, a2) -> a2.getRequestDate().compareTo(a1.getRequestDate()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No approval found for visitor"));

        approval.setStatus("CHECKED_IN");
        approval.setInTime(LocalDateTime.now());

        Approval saved = approvalRepository.save(approval);

        // Broadcast Live Metrics
        broadcastDashboardUpdate();

        return saved;
    }

    // ----------------------- 6. CHECKOUT -----------------------
    public Approval checkoutVisit(Long approvalId) {
        Approval approval = approvalRepository.findById(approvalId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        if (!"CHECKED_IN".equalsIgnoreCase(approval.getStatus())) {
            throw new RuntimeException("Visitor is not checked in");
        }

        approval.setStatus("CHECKED_OUT");
        approval.setOutTime(LocalDateTime.now());

        Approval saved = approvalRepository.save(approval);
        broadcastDashboardUpdate();
        return saved;
    }

    // ----------------------- 7. FILTERED REPORTS -----------------------
    public List<Approval> getFilteredApprovals(LocalDateTime startDate, LocalDateTime endDate, Long companyId,
            VisitorType visitorType) {
        return approvalRepository.findFilteredApprovals(startDate, endDate, companyId, visitorType);
    }

    public List<Approval> getPendingApprovals() {
        return approvalRepository.findPendingApprovals("PENDING");
    }

    public List<com.vms.backend.dto.VisitorHistoryDTO> getHistory() {
        List<Approval> approvals = approvalRepository.findAllHistory();
        return approvals.stream().map(this::convertToHistoryDTO).collect(java.util.stream.Collectors.toList());
    }

    private com.vms.backend.dto.VisitorHistoryDTO convertToHistoryDTO(Approval approval) {
        com.vms.backend.dto.VisitorHistoryDTO dto = new com.vms.backend.dto.VisitorHistoryDTO();
        dto.setId(approval.getId());
        dto.setVisitorName(approval.getVisitorName());
        dto.setStatus(approval.getStatus());
        dto.setInTime(approval.getInTime());
        dto.setOutTime(approval.getOutTime());
        dto.setRequestDate(approval.getRequestDate());

        if (approval.getVisitor() != null) {
            dto.setVisitorEmail(approval.getVisitor().getEmail());
            dto.setVisitorPhone(approval.getVisitor().getPhone());
            dto.setVisitorType(approval.getVisitor().getVisitorType());
            dto.setVisitorType(approval.getVisitor().getVisitorType());
            dto.setImagePath(approval.getVisitor().getImagePath());
            dto.setIdProof(approval.getVisitor().getIdProof());
            dto.setGender(approval.getVisitor().getGender());
        }

        // Host Info
        String hostName = approval.getHostName();
        String hostEmail = null;

        // Logic to fix "Unknown" Host Name
        if (approval.getHost() != null) {
            // If snapshot is null or "Unknown", use the current host name from entity
            if (hostName == null || "Unknown".equalsIgnoreCase(hostName)) {
                hostName = approval.getHost().getName();
            }
            hostEmail = approval.getHost().getEmail();
        }

        if (hostName != null) {
            dto.setHost(new com.vms.backend.dto.VisitorHistoryDTO.HostInfo(
                    hostName,
                    hostEmail));
        }

        // Company Info
        String companyName = approval.getCompanyName();

        // Logic to fix "Unknown" Company Name
        if (companyName == null || "Unknown".equalsIgnoreCase(companyName)) {
            if (approval.getCompany() != null) {
                companyName = approval.getCompany().getName();
            } else if (approval.getHost() != null && approval.getHost().getCompany() != null) {
                companyName = approval.getHost().getCompany().getName();
            }
        }

        if (companyName != null) {
            dto.setCompany(new com.vms.backend.dto.VisitorHistoryDTO.CompanyInfo(companyName));
        }

        return dto;
    }

    public List<Approval> getActiveVisitors() {
        return approvalRepository.findByStatus("CHECKED_IN");
    }

    // ----------------------- HELPER: SAVE & RESIZE IMAGE -----------------------
    private String saveAndResizeImage(MultipartFile photoFile, String visitorName) {
        try {
            if (photoFile == null || photoFile.isEmpty()) {
                return null;
            }

            File dir = new File(photoDir);
            if (!dir.exists())
                dir.mkdirs();

            String originalFilename = photoFile.getOriginalFilename();
            String ext = ".jpg"; // Default to jpg
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = visitorName.replaceAll("\\s+", "_") + "_" + UUID.randomUUID() + ext;
            File file = new File(dir, fileName);

            // Resize and Save using Thumbnailator directly from InputStream
            Thumbnails.of(photoFile.getInputStream())
                    .width(400)
                    .keepAspectRatio(true)
                    .toFile(file);

            return file.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException("Failed to save/resize photo", e);
        }
    }

    // ----------------------- EXISTING / OTHER METHODS -----------------------
    public List<Visitor> getAllVisitors() {
        return visitorRepository.findAll();
    }

    public Visitor getVisitorById(Long id) {
        return visitorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Visitor not found with id: " + id));
    }

    public void broadcastDashboardUpdate() {
        messagingTemplate.convertAndSend("/dashboard/visitors", reportService.getDashboardStats());
    }

    public com.vms.backend.dto.DashboardMetricsDTO getDashboardMetrics() {
        LocalDateTime start = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);

        long total = approvalRepository.countVisitorsToday(start, end);
        List<com.vms.backend.dto.CompanyVisitorCountDTO> byCompany = approvalRepository
                .countVisitorsByCompanyToday(start, end);

        return new com.vms.backend.dto.DashboardMetricsDTO(total, byCompany);
    }

    public void deleteVisitor(Long id) {
        visitorRepository.deleteById(id);
    }
}
