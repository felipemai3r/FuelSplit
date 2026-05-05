package com.caronatracker.usersgroupsservice.repository;

import com.caronatracker.usersgroupsservice.entity.ExtraExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ExtraExpenseParticipantRepository extends JpaRepository<ExtraExpenseParticipant, UUID> {
    List<ExtraExpenseParticipant> findByExpenseId(UUID expenseId);
}
