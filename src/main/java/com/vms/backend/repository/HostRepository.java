package com.vms.backend.repository;

import com.vms.backend.entity.Host;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HostRepository extends JpaRepository<Host, Long> {
    Optional<Host> findByUsername(String username);
}
