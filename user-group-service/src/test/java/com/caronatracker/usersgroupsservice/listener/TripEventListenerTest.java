package com.caronatracker.usersgroupsservice.listener;

import com.caronatracker.usersgroupsservice.entity.Group;
import com.caronatracker.usersgroupsservice.entity.GroupMember;
import com.caronatracker.usersgroupsservice.entity.GroupStatus;
import com.caronatracker.usersgroupsservice.entity.MemberRole;
import com.caronatracker.usersgroupsservice.entity.User;
import com.caronatracker.usersgroupsservice.event.TripRegisteredEvent;
import com.caronatracker.usersgroupsservice.repository.GroupMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripEventListenerTest {

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @InjectMocks
    private TripEventListener tripEventListener;

    @Test
    void onTripRegistered_success() {
        UUID groupId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        GroupMember member = buildMember(groupId, userId, BigDecimal.ZERO);

        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId))
                .thenReturn(Optional.of(member));
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        TripRegisteredEvent event = new TripRegisteredEvent(
                UUID.randomUUID().toString(),
                groupId.toString(),
                "2025-04-28",
                new BigDecimal("50.00"),
                List.of(new TripRegisteredEvent.ParticipantDto(userId.toString(), new BigDecimal("25.00")))
        );

        tripEventListener.onTripRegistered(event);

        assertEquals(new BigDecimal("25.00"), member.getBalance());
        verify(groupMemberRepository).save(member);
    }

    @Test
    void onTripRegistered_memberNotFound() {
        UUID groupId = UUID.randomUUID();
        UUID unknownUserId = UUID.randomUUID();

        when(groupMemberRepository.findByGroupIdAndUserId(groupId, unknownUserId))
                .thenReturn(Optional.empty());

        TripRegisteredEvent event = new TripRegisteredEvent(
                UUID.randomUUID().toString(),
                groupId.toString(),
                "2025-04-28",
                new BigDecimal("50.00"),
                List.of(new TripRegisteredEvent.ParticipantDto(unknownUserId.toString(), new BigDecimal("50.00")))
        );

        assertDoesNotThrow(() -> tripEventListener.onTripRegistered(event));
        verify(groupMemberRepository, never()).save(any());
    }

    @Test
    void onTripRegistered_multipleParticipants() {
        UUID groupId = UUID.randomUUID();
        UUID userId1 = UUID.randomUUID();
        UUID userId2 = UUID.randomUUID();
        UUID userId3 = UUID.randomUUID();
        GroupMember member1 = buildMember(groupId, userId1, BigDecimal.ZERO);
        GroupMember member2 = buildMember(groupId, userId2, BigDecimal.ZERO);
        GroupMember member3 = buildMember(groupId, userId3, BigDecimal.ZERO);

        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId1)).thenReturn(Optional.of(member1));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId2)).thenReturn(Optional.of(member2));
        when(groupMemberRepository.findByGroupIdAndUserId(groupId, userId3)).thenReturn(Optional.of(member3));
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));

        TripRegisteredEvent event = new TripRegisteredEvent(
                UUID.randomUUID().toString(),
                groupId.toString(),
                "2025-04-28",
                new BigDecimal("90.00"),
                List.of(
                        new TripRegisteredEvent.ParticipantDto(userId1.toString(), new BigDecimal("30.00")),
                        new TripRegisteredEvent.ParticipantDto(userId2.toString(), new BigDecimal("30.00")),
                        new TripRegisteredEvent.ParticipantDto(userId3.toString(), new BigDecimal("30.00"))
                )
        );

        tripEventListener.onTripRegistered(event);

        assertEquals(new BigDecimal("30.00"), member1.getBalance());
        assertEquals(new BigDecimal("30.00"), member2.getBalance());
        assertEquals(new BigDecimal("30.00"), member3.getBalance());
        verify(groupMemberRepository, times(3)).save(any(GroupMember.class));
    }

    private GroupMember buildMember(UUID groupId, UUID userId, BigDecimal balance) {
        User user = new User();
        user.setId(userId);
        user.setName("Usuário");
        user.setEmail("user@email.com");
        user.setHasCnh(false);

        User owner = new User();
        owner.setId(UUID.randomUUID());

        Group group = new Group();
        group.setId(groupId);
        group.setOwner(owner);
        group.setName("Grupo");
        group.setStatus(GroupStatus.ACTIVE);

        GroupMember member = new GroupMember();
        member.setId(UUID.randomUUID());
        member.setGroup(group);
        member.setUser(user);
        member.setRole(MemberRole.MEMBER);
        member.setBalance(balance);
        return member;
    }
}
