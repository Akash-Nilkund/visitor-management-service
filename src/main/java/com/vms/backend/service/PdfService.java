package com.vms.backend.service;

import com.vms.backend.entity.Visitor;
import com.vms.backend.entity.Approval;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Chunk;
import com.lowagie.text.Image;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPCell;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PdfService {

    @Value("${file.upload-dir}")
    private String photoDir;

    public String generateVisitorPassPdf(Approval approval) {
        Visitor visitor = approval.getVisitor();
        try {
            String fileName = "visitor_pass_" + visitor.getId() + "_" + UUID.randomUUID() + ".pdf";
            // Use absolute path for visitor passes to avoid write permission/path issues
            File photoFileDir = new File(photoDir);
            File parentDir = photoFileDir.getParentFile();
            File passesDir;

            if (parentDir != null) {
                passesDir = new File(parentDir, "visitor-passes");
            } else {
                passesDir = new File("visitor-passes");
            }

            if (!passesDir.exists()) {
                passesDir.mkdirs();
            }

            String output = new File(passesDir, fileName).getAbsolutePath();

            Document document = new Document(PageSize.A5.rotate(), 20, 20, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream(output));
            document.open();

            // ---------- TITLE ----------
            Font titleFont = new Font(Font.HELVETICA, 20, Font.BOLD, Color.BLACK);
            Paragraph title = new Paragraph("VISITOR PASS", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20f);
            document.add(title);

            // ---------- LAYOUT TABLE ----------
            float[] cols = { 2f, 1.5f };
            PdfPTable table = new PdfPTable(cols);
            table.setWidthPercentage(100);

            PdfPCell left = new PdfPCell();
            left.setBorder(Rectangle.NO_BORDER);
            left.setPadding(5);

            Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            // ---------- DETAILS ----------
            addDetail(left, "Name:", safe(visitor.getName()), labelFont, valueFont);
            addDetail(left, "Contact:", safe(visitor.getPhone()), labelFont, valueFont);
            addDetail(left, "Email:", safe(visitor.getEmail()), labelFont, valueFont);
            

            // Mask ID Proof
            String idProof = safe(visitor.getIdProof());
            String maskedId = idProof;
            if (idProof.length() > 4) {
                maskedId = idProof.substring(0, 2) + "****" + idProof.substring(idProof.length() - 2);
            } else if (!idProof.isEmpty()) {
                maskedId = "****"; // Fallback for very short IDs
            }
            addDetail(left, "ID Proof:", maskedId, labelFont, valueFont);

            addDetail(left, "Gender:", safe(visitor.getGender()), labelFont, valueFont);

            String hostName = approval.getHost() != null ? safe(approval.getHost().getName()) : "N/A";
            addDetail(left, "Host:", hostName, labelFont, valueFont);

            String vtype = visitor.getVisitorType() != null ? visitor.getVisitorType().toString() : "N/A";
            addDetail(left, "Type:", vtype, labelFont, valueFont);

            // ---------- TIME ----------
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm a");

            String checkIn = "N/A";
            try {
                if (approval.getInTime() != null) {
                    checkIn = approval.getInTime()
                            .atZone(ZoneId.of("Asia/Kolkata"))
                            .toLocalTime()
                            .format(DateTimeFormatter.ofPattern("hh:mm a"));
                    checkIn = approval.getInTime().format(fmt);
                }
            } catch (Exception ignored) {
            }

            addDetail(left, "Check-In:", checkIn, labelFont, valueFont);

            table.addCell(left);

            // ========== PHOTO & QR CODE ==========
            PdfPCell right = new PdfPCell();
            right.setBorder(Rectangle.NO_BORDER);
            right.setPadding(5);
            right.setHorizontalAlignment(Element.ALIGN_CENTER);

            // 1. Visitor Photo
            try {
                String imagePath = visitor.getImagePath();
                if (imagePath != null && !imagePath.isBlank()) {
                    File photo = new File(imagePath);
                    if (photo.exists()) {
                        Image pic = Image.getInstance(photo.getAbsolutePath());
                        pic.scaleToFit(120, 120);
                        pic.setAlignment(Image.ALIGN_CENTER);
                        right.addElement(pic);
                    }
                }
            } catch (Exception ex) {
                right.addElement(new Paragraph("Photo not available", valueFont));
            }

            right.addElement(Chunk.NEWLINE);

            table.addCell(right);
            document.add(table);

            document.close();
            return output;

        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF", e);
        }
    }

    // ---------- Add detail row ----------
    private void addDetail(PdfPCell cell, String labelText, String valueText, Font labelFont, Font valueFont) {
        Paragraph p = new Paragraph();
        p.add(new Chunk(labelText + " ", labelFont));
        p.add(new Chunk(valueText != null ? valueText : "", valueFont));
        p.setSpacingAfter(4);
        cell.addElement(p);
    }

    // Safe string method
    private String safe(String s) {
        return s == null ? "" : s;
    }
}
