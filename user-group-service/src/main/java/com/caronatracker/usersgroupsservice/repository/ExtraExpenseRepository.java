package com.caronatracker.usersgroupsservice.repository;

import com.caronatracker.usersgroupsservice.entity.ExtraExpense;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtraExpenseRepository extends JpaRepository<ExtraExpense, UUID> {
    List<ExtraExpense> findByGroupId(UUID groupId);
}
