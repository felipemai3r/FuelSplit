package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.ExtraExpenseRequest;
import com.caronatracker.usersgroupsservice.dto.ExtraExpenseResponse;
import com.caronatracker.usersgroupsservice.entity.*;
import com.caronatracker.usersgroupsservice.exception.AccessDeniedException;
import com.caronatracker.usersgroupsservice.repository.ExtraExpenseParticipantRepository;
import com.caronatracker.usersgroupsservice.repository.ExtraExpenseRepository;
import com.caronatracker.usersgroupsservice.repository.GroupMemberRepository;
import com.caronatracker.usersgroupsservice.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtraExpenseServiceTest {

    @Mock
    private ExtraExpenseRepository extraExpenseRepository;

    @Mock
    private ExtraExpenseParticipantRepository participantRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private ExtraExpenseService extraExpenseService;

    @Test
    void createExpense_equalSplit_success() {
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);
        User member2 = buildUser(UUID.randomUUID());

        List<GroupMember> members = List.of(
                buildMember(group, owner, MemberRole.OWNER),
                buildMember(group, member2, MemberRole.MEMBER)
        );

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userService.findById(ownerId)).thenReturn(owner);
        when(groupMemberRepository.findByGroupId(group.getId())).thenReturn(members);

        ExtraExpense savedExpense = buildExpense(group, owner, new BigDecimal("20.00"), SplitType.EQUAL);
        when(extraExpenseRepository.save(any(ExtraExpense.class))).thenReturn(savedExpense);
        when(participantRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        ExtraExpenseRequest request = new ExtraExpenseRequest(
                "Pedágio", new BigDecimal("20.00"), SplitType.EQUAL, LocalDate.now(), null);

        ExtraExpenseResponse response = extraExpenseService.createExpense(group.getId(), ownerId, request);

        assertNotNull(response);
        assertEquals(2, response.participants().size());
        response.participants().forEach(p ->
                assertEquals(new BigDecimal("10.00"), p.amountOwed()));
    }

    @Test
    void createExpense_equalSplit_threeMembers() {
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        List<GroupMember> members = List.of(
                buildMember(group, owner, MemberRole.OWNER),
                buildMember(group, buildUser(UUID.randomUUID()), MemberRole.MEMBER),
                buildMember(group, buildUser(UUID.randomUUID()), MemberRole.MEMBER)
        );

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userService.findById(ownerId)).thenReturn(owner);
        when(groupMemberRepository.findByGroupId(group.getId())).thenReturn(members);

        ExtraExpense savedExpense = buildExpense(group, owner, new BigDecimal("30.00"), SplitType.EQUAL);
        when(extraExpenseRepository.save(any(ExtraExpense.class))).thenReturn(savedExpense);
        when(participantRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        ExtraExpenseRequest request = new ExtraExpenseRequest(
                "Gasolina", new BigDecimal("30.00"), SplitType.EQUAL, LocalDate.now(), null);

        ExtraExpenseResponse response = extraExpenseService.createExpense(group.getId(), ownerId, request);

        assertEquals(3, response.participants().size());
        response.participants().forEach(p ->
                assertEquals(new BigDecimal("10.00"), p.amountOwed()));
    }

    @Test
    void createExpense_customSplit_success() {
        UUID ownerId = UUID.randomUUID();
        UUID member2Id = UUID.randomUUID();
        User owner = buildUser(ownerId);
        User member2 = buildUser(member2Id);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userService.findById(ownerId)).thenReturn(owner);
        when(userService.findById(member2Id)).thenReturn(member2);

        ExtraExpense savedExpense = buildExpense(group, owner, new BigDecimal("30.00"), SplitType.CUSTOM);
        when(extraExpenseRepository.save(any(ExtraExpense.class))).thenReturn(savedExpense);
        when(participantRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));

        List<ExtraExpenseRequest.ParticipantRequest> participants = List.of(
                new ExtraExpenseRequest.ParticipantRequest(ownerId, new BigDecimal("20.00")),
                new ExtraExpenseRequest.ParticipantRequest(member2Id, new BigDecimal("10.00"))
        );
        ExtraExpenseRequest request = new ExtraExpenseRequest(
                "Estacionamento", new BigDecimal("30.00"), SplitType.CUSTOM, LocalDate.now(), participants);

        ExtraExpenseResponse response = extraExpenseService.createExpense(group.getId(), ownerId, request);

        assertNotNull(response);
        assertEquals(2, response.participants().size());
    }

    @Test
    void createExpense_customSplit_wrongSum() {
        UUID ownerId = UUID.randomUUID();
        UUID member2Id = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(userService.findById(ownerId)).thenReturn(owner);

        ExtraExpense savedExpense = buildExpense(group, owner, new BigDecimal("30.00"), SplitType.CUSTOM);
        when(extraExpenseRepository.save(any(ExtraExpense.class))).thenReturn(savedExpense);

        List<ExtraExpenseRequest.ParticipantRequest> participants = List.of(
                new ExtraExpenseRequest.ParticipantRequest(ownerId, new BigDecimal("10.00")),
                new ExtraExpenseRequest.ParticipantRequest(member2Id, new BigDecimal("10.00"))
        );
        ExtraExpenseRequest request = new ExtraExpenseRequest(
                "Estacionamento", new BigDecimal("30.00"), SplitType.CUSTOM, LocalDate.now(), participants);

        assertThrows(IllegalArgumentException.class,
                () -> extraExpenseService.createExpense(group.getId(), ownerId, request));
    }

    @Test
    void createExpense_byNonOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID nonOwnerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        ExtraExpenseRequest request = new ExtraExpenseRequest(
                "Pedágio", new BigDecimal("20.00"), SplitType.EQUAL, LocalDate.now(), null);

        assertThrows(AccessDeniedException.class,
                () -> extraExpenseService.createExpense(group.getId(), nonOwnerId, request));
    }

    private User buildUser(UUID id) {
        User user = new User();
        user.setId(id);
        user.setName("Usuário " + id);
        user.setEmail("user@email.com");
        user.setHasCnh(false);
        return user;
    }

    private Group buildGroup(UUID id, User owner) {
        Group group = new Group();
        group.setId(id);
        group.setOwner(owner);
        group.setName("Grupo Teste");
        group.setBaseDistanceKm(BigDecimal.valueOf(50));
        group.setFuelPricePerLiter(BigDecimal.valueOf(5.89));
        group.setStatus(GroupStatus.ACTIVE);
        return group;
    }

    private GroupMember buildMember(Group group, User user, MemberRole role) {
        GroupMember member = new GroupMember();
        member.setId(UUID.randomUUID());
        member.setGroup(group);
        member.setUser(user);
        member.setRole(role);
        member.setBalance(BigDecimal.ZERO);
        return member;
    }

    private ExtraExpense buildExpense(Group group, User creator, BigDecimal total, SplitType splitType) {
        ExtraExpense expense = new ExtraExpense();
        expense.setId(UUID.randomUUID());
        expense.setGroup(group);
        expense.setCreatedByUser(creator);
        expense.setDescription("Despesa");
        expense.setTotalAmount(total);
        expense.setSplitType(splitType);
        expense.setExpenseDate(LocalDate.now());
        return expense;
    }
}
