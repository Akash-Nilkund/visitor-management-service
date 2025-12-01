package com.vms.backend.repository;

import com.vms.backend.entity.Visitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.vms.backend.entity.VisitorType;
import java.time.LocalDateTime;

@Repository
public interface VisitorRepository extends JpaRepository<Visitor, Long> {

}
