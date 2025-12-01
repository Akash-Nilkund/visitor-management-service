package com.vms.backend.service;

import com.vms.backend.entity.Approval;
import com.vms.backend.entity.Host;
import com.vms.backend.entity.Visitor;
import com.vms.backend.entity.VisitorType;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.time.LocalDateTime;

public class PdfServiceTest {

    @Test
    public void testGenerateVisitorPassPdf() {
        PdfService pdfService = new PdfService();
        // Set a temporary directory for photoDir
        String tempDir = System.getProperty("java.io.tmpdir");
        ReflectionTestUtils.setField(pdfService, "photoDir", tempDir);

        // Create dummy data
        Host host = new Host();
        host.setName("Akash Nilkund");

        Visitor visitor = new Visitor();
        visitor.setId(1L);
        visitor.setName("Akash Nilkund");
        visitor.setPhone("09481552565");
        visitor.setEmail("akashnilkund21@gmail.com");
        visitor.setIdProofNumber("AB123456");
        visitor.setVisitorType(VisitorType.GUEST);
        visitor.setImagePath("non_existent_image.jpg"); // Will trigger "Photo not available"

        Approval approval = new Approval();
        approval.setVisitor(visitor);
        approval.setHost(host);
        approval.setInTime(LocalDateTime.now());

        // Generate PDF
        String pdfPath = pdfService.generateVisitorPassPdf(approval);
        System.out.println("PDF generated at: " + pdfPath);

        File pdfFile = new File(pdfPath);
        if (pdfFile.exists()) {
            System.out.println("PDF file exists.");
        } else {
            System.err.println("PDF file was not created.");
        }
    }
}
