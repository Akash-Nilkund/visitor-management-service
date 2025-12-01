package com.vms.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String apiKey;

    private static final String BREVO_URL = "https://api.brevo.com/v3/smtp/email";

    // ---------------- SEND PASS (NO ATTACHMENT) ----------------
    public void sendPass(String toEmail, String name) {
        sendEmail(
                toEmail,
                "Visitor Entry Approved",
                "<p>Hello,<br>The visitor <b>" + name + "</b> has been approved.<br><br>Regards,<br>Security Team</p>");
    }

    // ---------------- SEND REJECTION (NO ATTACHMENT) -----------
    public void sendRejection(String toEmail, String name) {
        sendEmail(
                toEmail,
                "Visitor Request Rejected",
                "<p>Hello " + name
                        + ",<br>Your visit request has been <b>rejected</b>.<br><br>Regards,<br>Security Team</p>");
    }

    // ---------------- COMMON SEND EMAIL (NO ATTACHMENT) --------
    private void sendEmail(String toEmail, String subject, String htmlContent) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "email", "akashnilkund21@gmail.com",
                    "name", "VMS System"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForEntity(BREVO_URL, request, String.class);

            System.out.println("Email sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send email to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------- SEND EMAIL WITH PDF ATTACHMENT ----------------
    public void sendEmailWithAttachment(String toEmail, String subject, String htmlContent, String pdfPath) {
        try {
            RestTemplate restTemplate = new RestTemplate();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("sender", Map.of(
                    "email", "akashnilkund21@gmail.com",
                    "name", "VMS System"));
            body.put("to", List.of(Map.of("email", toEmail)));
            body.put("subject", subject);
            body.put("htmlContent", htmlContent);

            // attachment
            byte[] bytes = Files.readAllBytes(Path.of(pdfPath));
            String base64 = Base64.getEncoder().encodeToString(bytes);

            Map<String, String> attachment = new HashMap<>();
            attachment.put("name", Path.of(pdfPath).getFileName().toString());
            attachment.put("content", base64);

            body.put("attachment", List.of(attachment));

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForEntity(BREVO_URL, request, String.class);

            System.out.println("Email with PDF sent to: " + toEmail);

        } catch (Exception e) {
            System.err.println("Failed to send email with attachment to " + toEmail + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ---------------- PUBLIC GENERIC SEND EMAIL ----------------
    public void sendGenericEmail(String toEmail, String subject, String htmlContent) {
        sendEmail(toEmail, subject, htmlContent);
    }
}
