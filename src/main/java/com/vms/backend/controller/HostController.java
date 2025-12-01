package com.vms.backend.controller;

import com.vms.backend.entity.Host;
import com.vms.backend.service.HostService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hosts")
@CrossOrigin(origins = "http://localhost:3000")
public class HostController {

    @Autowired
    private HostService hostService;

    @GetMapping
    public ResponseEntity<List<com.vms.backend.dto.HostDTO>> getAllHosts() {
        return ResponseEntity.ok(hostService.getAllHosts());
    }

    @PostMapping
    public ResponseEntity<Host> addHost(@RequestBody com.vms.backend.dto.HostDTO hostDTO) {
        return ResponseEntity.ok(hostService.createHost(hostDTO));
    }
}
