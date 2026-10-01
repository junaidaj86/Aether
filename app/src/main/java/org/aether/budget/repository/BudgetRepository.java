package org.aether.budget.repository;

import org.aether.budget.domain.Budget;
import org.aether.budget.domain.BudgetScope;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    Optional<Budget> findByScopeAndScopeKeyAndPeriodStartAndPeriodEnd(
            BudgetScope scope, String scopeKey, LocalDate periodStart, LocalDate periodEnd);
}
