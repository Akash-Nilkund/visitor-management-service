package com.vms.backend.service;

import com.vms.backend.entity.Host;
import com.vms.backend.repository.HostRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostService {

    @Autowired
    private HostRepository hostRepository;

    @Autowired
    private com.vms.backend.repository.CompanyRepository companyRepository;

    public List<com.vms.backend.dto.HostDTO> getAllHosts() {
        return hostRepository.findAll().stream().map(this::convertToDTO).collect(java.util.stream.Collectors.toList());
    }

    public Host saveHost(Host host) {
        return hostRepository.save(host);
    }

    public Host createHost(com.vms.backend.dto.HostDTO hostDTO) {
        Host host = new Host();
        host.setName(hostDTO.getName());
        host.setEmail(hostDTO.getEmail());
        host.setMobile(hostDTO.getMobile());
        host.setDepartment(hostDTO.getDepartment());
        host.setUsername(hostDTO.getUsername());
        host.setRole(hostDTO.getRole());

        if (hostDTO.getCompanyId() != null) {
            com.vms.backend.entity.Company company = companyRepository.findById(hostDTO.getCompanyId())
                    .orElseThrow(() -> new RuntimeException("Company not found with ID: " + hostDTO.getCompanyId()));
            host.setCompany(company);
        }

        return hostRepository.save(host);
    }

    private com.vms.backend.dto.HostDTO convertToDTO(Host host) {
        com.vms.backend.dto.HostDTO dto = new com.vms.backend.dto.HostDTO();
        dto.setId(host.getId());
        dto.setName(host.getName());
        dto.setEmail(host.getEmail());
        dto.setMobile(host.getMobile());
        dto.setDepartment(host.getDepartment());
        dto.setUsername(host.getUsername());
        dto.setRole(host.getRole());
        if (host.getCompany() != null) {
            dto.setCompanyId(host.getCompany().getId());
        }
        return dto;
    }
}
