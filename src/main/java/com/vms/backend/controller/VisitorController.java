package com.vms.backend.controller;

import com.vms.backend.dto.VisitorRequest;
import com.vms.backend.dto.VisitorDTO;
import com.vms.backend.entity.Approval;
import com.vms.backend.entity.Visitor;
import com.vms.backend.service.VisitorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.util.List;

@RestController
@RequestMapping("/api/visitors")
public class VisitorController {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private ObjectMapper objectMapper;

    // 1. REGISTER (POST /register) -> Creates Visitor + Approval (PENDING)
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Approval registerVisitor(
            @RequestPart("visitorJsonData") String visitorJsonData,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws Exception {

        VisitorDTO request = objectMapper.readValue(visitorJsonData, VisitorDTO.class);
        return visitorService.preRegisterVisitor(request, photo);
    }

    // 2. CHECKIN (PATCH /{id}/checkin) -> Mark CHECKED_IN + record inTime
    @PatchMapping("/{id}/checkin")
    public Approval checkInVisitor(@PathVariable Long id) {
        return visitorService.checkInApprovedVisitor(id);
    }

    // 3. CHECKOUT (PATCH /{id}/checkout) -> Mark CHECKED_OUT + record outTime
    @PatchMapping("/{id}/checkout")
    public Approval checkoutVisitor(@PathVariable Long id) {
        return visitorService.checkoutVisit(id);
    }

    // ------------------ EXISTING / COMPATIBILITY ENDPOINTS ------------------

    // IMMEDIATE CHECK-IN (POST /checkin) - Kept for backward compatibility if
    // needed, or mapped to immediate checkin service
    @PostMapping("/checkin")
    public Approval immediateCheckIn(
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "photoFile", required = false) MultipartFile photoFile,
            @RequestParam(value = "visitor", required = false) String visitor,
            @RequestParam(value = "visitorJsonData", required = false) String visitorJsonData) throws Exception {

        MultipartFile file = (photo != null) ? photo : photoFile;
        String json = (visitor != null) ? visitor : visitorJsonData;

        if (json == null) {
            throw new RuntimeException("Required part 'visitor' or 'visitorJsonData' is not present");
        }

        VisitorRequest request = objectMapper.readValue(json, VisitorRequest.class);
        return visitorService.checkInVisitor(request, file);
    }

    // PRE-REGISTER (POST /preregister) - Alias to /register
    @PostMapping(value = "/preregister", consumes = { "multipart/form-data" })
    public Approval preRegister(
            @RequestPart("visitorJsonData") String visitorJsonData,
            @RequestPart(value = "photo", required = false) MultipartFile photo) throws Exception {
        return registerVisitor(visitorJsonData, photo);
    }

    @GetMapping
    public List<Visitor> getAllVisitors() {
        return visitorService.getAllVisitors();
    }

    @GetMapping("/{id}")
    public Visitor getVisitorById(@PathVariable Long id) {
        return visitorService.getVisitorById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteVisitor(@PathVariable Long id) {
        visitorService.deleteVisitor(id);
    }
}
