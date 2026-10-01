package org.aether.audit.repository;

import org.aether.audit.domain.AuditEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    List<AuditEvent> findByCorrelationIdOrderByCreatedAtAsc(String correlationId);
}
