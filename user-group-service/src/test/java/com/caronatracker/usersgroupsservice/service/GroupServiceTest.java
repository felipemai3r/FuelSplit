package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.GroupRequest;
import com.caronatracker.usersgroupsservice.dto.GroupResponse;
import com.caronatracker.usersgroupsservice.entity.*;
import com.caronatracker.usersgroupsservice.exception.AccessDeniedException;
import com.caronatracker.usersgroupsservice.repository.GroupMemberRepository;
import com.caronatracker.usersgroupsservice.repository.GroupRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GroupServiceTest {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private GroupMemberRepository groupMemberRepository;

    @Mock
    private UserService userService;

    @Mock
    private VehicleService vehicleService;

    @InjectMocks
    private GroupService groupService;

    @Captor
    private ArgumentCaptor<GroupMember> memberCaptor;

    @Test
    void createGroup_success() {
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        GroupRequest request = new GroupRequest("Grupo Teste", BigDecimal.valueOf(50), BigDecimal.valueOf(5.89));

        when(userService.findById(ownerId)).thenReturn(owner);
        Group savedGroup = buildGroup(UUID.randomUUID(), owner);
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groupMemberRepository.findByGroupId(savedGroup.getId())).thenReturn(List.of());

        GroupResponse response = groupService.createGroup(ownerId, request);

        assertNotNull(response);
        assertEquals(ownerId, response.ownerId());
    }

    @Test
    void createGroup_ownerAddedAsMember() {
        UUID ownerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        GroupRequest request = new GroupRequest("Grupo Teste", BigDecimal.valueOf(50), BigDecimal.valueOf(5.89));

        when(userService.findById(ownerId)).thenReturn(owner);
        Group savedGroup = buildGroup(UUID.randomUUID(), owner);
        when(groupRepository.save(any(Group.class))).thenReturn(savedGroup);
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groupMemberRepository.findByGroupId(savedGroup.getId())).thenReturn(List.of());

        groupService.createGroup(ownerId, request);

        verify(groupMemberRepository).save(memberCaptor.capture());
        GroupMember savedMember = memberCaptor.getValue();
        assertEquals(MemberRole.OWNER, savedMember.getRole());
        assertEquals(ownerId, savedMember.getUser().getId());
    }

    @Test
    void addMember_success() {
        UUID ownerId = UUID.randomUUID();
        UUID newUserId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        User newUser = buildUser(newUserId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByGroupIdAndUserId(group.getId(), newUserId)).thenReturn(false);
        when(userService.findById(newUserId)).thenReturn(newUser);
        when(groupMemberRepository.save(any(GroupMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(groupMemberRepository.findByGroupId(group.getId())).thenReturn(List.of());

        GroupResponse response = groupService.addMember(group.getId(), ownerId, newUserId);

        assertNotNull(response);
        verify(groupMemberRepository).save(any(GroupMember.class));
    }

    @Test
    void addMember_duplicate() {
        UUID ownerId = UUID.randomUUID();
        UUID existingUserId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(groupMemberRepository.existsByGroupIdAndUserId(group.getId(), existingUserId)).thenReturn(true);

        assertThrows(IllegalStateException.class,
                () -> groupService.addMember(group.getId(), ownerId, existingUserId));
    }

    @Test
    void addMember_byNonOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID nonOwnerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertThrows(AccessDeniedException.class,
                () -> groupService.addMember(group.getId(), nonOwnerId, UUID.randomUUID()));
    }

    @Test
    void removeMember_byNonOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID nonOwnerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertThrows(AccessDeniedException.class,
                () -> groupService.removeMember(group.getId(), nonOwnerId, UUID.randomUUID()));
    }

    @Test
    void setActiveVehicle_inactiveVehicle() {
        UUID ownerId = UUID.randomUUID();
        UUID vehicleId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));
        when(vehicleService.getActiveVehicleForGroup(vehicleId))
                .thenThrow(new IllegalArgumentException("Veículo inativo"));

        assertThrows(IllegalArgumentException.class,
                () -> groupService.setActiveVehicle(group.getId(), ownerId, vehicleId));
    }

    @Test
    void setActiveVehicle_byNonOwner() {
        UUID ownerId = UUID.randomUUID();
        UUID nonOwnerId = UUID.randomUUID();
        User owner = buildUser(ownerId);
        Group group = buildGroup(UUID.randomUUID(), owner);

        when(groupRepository.findById(group.getId())).thenReturn(Optional.of(group));

        assertThrows(AccessDeniedException.class,
                () -> groupService.setActiveVehicle(group.getId(), nonOwnerId, UUID.randomUUID()));
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
}
