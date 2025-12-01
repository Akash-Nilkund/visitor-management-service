package com.vms.backend.service;

import com.vms.backend.entity.Company;
import com.vms.backend.repository.CompanyRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    @Autowired
    private CompanyRepository companyRepository;

    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public Company saveCompany(Company company) {
        return companyRepository.save(company);
    }
}
