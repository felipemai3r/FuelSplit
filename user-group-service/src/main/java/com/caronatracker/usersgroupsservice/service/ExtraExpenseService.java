package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.ExtraExpenseRequest;
import com.caronatracker.usersgroupsservice.dto.ExtraExpenseResponse;
import com.caronatracker.usersgroupsservice.entity.*;
import com.caronatracker.usersgroupsservice.exception.AccessDeniedException;
import com.caronatracker.usersgroupsservice.repository.ExtraExpenseParticipantRepository;
import com.caronatracker.usersgroupsservice.repository.ExtraExpenseRepository;
import com.caronatracker.usersgroupsservice.repository.GroupMemberRepository;
import com.caronatracker.usersgroupsservice.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import com.caronatracker.usersgroupsservice.exception.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@Transactional
public class ExtraExpenseService {

    private final ExtraExpenseRepository extraExpenseRepository;
    private final ExtraExpenseParticipantRepository participantRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;

    public ExtraExpenseResponse createExpense(UUID groupId, UUID currentUserId, ExtraExpenseRequest request) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado: " + groupId));

        if (!group.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Apenas o dono do grupo pode lançar despesas extras");
        }

        ExtraExpense expense = new ExtraExpense();
        expense.setGroup(group);
        expense.setCreatedByUser(userService.findById(currentUserId));
        expense.setDescription(request.description());
        expense.setTotalAmount(request.totalAmount());
        expense.setSplitType(request.splitType());
        expense.setExpenseDate(request.expenseDate());
        expense = extraExpenseRepository.save(expense);

        List<ExtraExpenseParticipant> participants;

        if (request.splitType() == SplitType.EQUAL) {
            List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
            BigDecimal amountOwed = request.totalAmount()
                    .divide(BigDecimal.valueOf(members.size()), 2, RoundingMode.HALF_UP);
            final ExtraExpense savedExpense = expense;
            participants = members.stream()
                    .map(m -> {
                        ExtraExpenseParticipant p = new ExtraExpenseParticipant();
                        p.setExpense(savedExpense);
                        p.setUser(m.getUser());
                        p.setAmountOwed(amountOwed);
                        return p;
                    })
                    .toList();
        } else {
            if (request.participants() == null || request.participants().isEmpty()) {
                throw new IllegalArgumentException("Participantes são obrigatórios para divisão CUSTOM");
            }
            BigDecimal sum = request.participants().stream()
                    .map(ExtraExpenseRequest.ParticipantRequest::amountOwed)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (sum.subtract(request.totalAmount()).abs().compareTo(new BigDecimal("0.01")) > 0) {
                throw new IllegalArgumentException(
                        "Soma dos valores dos participantes não corresponde ao valor total da despesa");
            }
            final ExtraExpense savedExpense = expense;
            participants = request.participants().stream()
                    .map(p -> {
                        ExtraExpenseParticipant ep = new ExtraExpenseParticipant();
                        ep.setExpense(savedExpense);
                        ep.setUser(userService.findById(p.userId()));
                        ep.setAmountOwed(p.amountOwed());
                        return ep;
                    })
                    .toList();
        }

        participantRepository.saveAll(participants);
        return toResponse(expense, participants);
    }

    @Transactional(readOnly = true)
    public List<ExtraExpenseResponse> listExpenses(UUID groupId) {
        return extraExpenseRepository.findByGroupId(groupId).stream()
                .map(expense -> {
                    List<ExtraExpenseParticipant> parts = participantRepository.findByExpenseId(expense.getId());
                    return toResponse(expense, parts);
                })
                .toList();
    }

    private ExtraExpenseResponse toResponse(ExtraExpense expense, List<ExtraExpenseParticipant> participants) {
        List<ExtraExpenseResponse.ParticipantResponse> participantResponses = participants.stream()
                .map(p -> new ExtraExpenseResponse.ParticipantResponse(p.getUser().getId(), p.getAmountOwed()))
                .toList();

        return new ExtraExpenseResponse(
                expense.getId(),
                expense.getGroup().getId(),
                expense.getCreatedByUser().getId(),
                expense.getDescription(),
                expense.getTotalAmount(),
                expense.getSplitType(),
                expense.getExpenseDate(),
                expense.getCreatedAt(),
                participantResponses
        );
    }
}
