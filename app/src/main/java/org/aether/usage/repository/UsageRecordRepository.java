package org.aether.usage.repository;

import org.aether.usage.domain.UsageRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UsageRecordRepository extends JpaRepository<UsageRecord, UUID> {
    List<UsageRecord> findByCorrelationId(String correlationId);
}
