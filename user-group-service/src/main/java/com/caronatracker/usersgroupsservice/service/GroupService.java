package com.caronatracker.usersgroupsservice.service;

import com.caronatracker.usersgroupsservice.dto.GroupRequest;
import com.caronatracker.usersgroupsservice.dto.GroupResponse;
import com.caronatracker.usersgroupsservice.dto.MemberBalanceResponse;
import com.caronatracker.usersgroupsservice.entity.Group;
import com.caronatracker.usersgroupsservice.entity.GroupMember;
import com.caronatracker.usersgroupsservice.entity.MemberRole;
import com.caronatracker.usersgroupsservice.entity.Vehicle;
import com.caronatracker.usersgroupsservice.exception.AccessDeniedException;
import com.caronatracker.usersgroupsservice.exception.EntityNotFoundException;
import com.caronatracker.usersgroupsservice.repository.GroupMemberRepository;
import com.caronatracker.usersgroupsservice.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;
    private final VehicleService vehicleService;

    public GroupResponse createGroup(UUID ownerId, GroupRequest request) {
        var owner = userService.findById(ownerId);
        Group group = new Group();
        group.setOwner(owner);
        group.setName(request.name());
        group.setBaseDistanceKm(request.baseDistanceKm());
        group.setFuelPricePerLiter(request.fuelPricePerLiter());
        group = groupRepository.save(group);

        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroup(group);
        ownerMember.setUser(owner);
        ownerMember.setRole(MemberRole.OWNER);
        groupMemberRepository.save(ownerMember);

        List<GroupMember> members = groupMemberRepository.findByGroupId(group.getId());
        return toResponse(group, members);
    }

    @Transactional(readOnly = true)
    public GroupResponse getGroup(UUID groupId) {
        Group group = findById(groupId);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return toResponse(group, members);
    }

    public GroupResponse updateGroup(UUID groupId, UUID currentUserId, GroupRequest request) {
        Group group = findById(groupId);
        checkOwner(group, currentUserId);
        group.setName(request.name());
        group.setBaseDistanceKm(request.baseDistanceKm());
        group.setFuelPricePerLiter(request.fuelPricePerLiter());
        group = groupRepository.save(group);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return toResponse(group, members);
    }

    public GroupResponse addMember(UUID groupId, UUID currentUserId, UUID newUserId) {
        Group group = findById(groupId);
        checkOwner(group, currentUserId);
        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newUserId)) {
            throw new IllegalStateException("Usuário já é membro do grupo");
        }
        var user = userService.findById(newUserId);
        GroupMember member = new GroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(MemberRole.MEMBER);
        groupMemberRepository.save(member);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return toResponse(group, members);
    }

    public void removeMember(UUID groupId, UUID currentUserId, UUID targetUserId) {
        Group group = findById(groupId);
        checkOwner(group, currentUserId);
        groupMemberRepository.findByGroupIdAndUserId(groupId, targetUserId)
                .ifPresent(groupMemberRepository::delete);
    }

    public GroupResponse setActiveVehicle(UUID groupId, UUID currentUserId, UUID vehicleId) {
        Group group = findById(groupId);
        checkOwner(group, currentUserId);
        Vehicle vehicle = vehicleService.getActiveVehicleForGroup(vehicleId);
        group.setActiveVehicle(vehicle);
        group = groupRepository.save(group);
        List<GroupMember> members = groupMemberRepository.findByGroupId(groupId);
        return toResponse(group, members);
    }

    @Transactional(readOnly = true)
    public Group findById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo não encontrado: " + groupId));
    }

    private void checkOwner(Group group, UUID currentUserId) {
        if (!group.getOwner().getId().equals(currentUserId)) {
            throw new AccessDeniedException("Apenas o dono do grupo pode realizar esta operação");
        }
    }

    private GroupResponse toResponse(Group group, List<GroupMember> members) {
        List<MemberBalanceResponse> memberResponses = members.stream()
                .map(m -> new MemberBalanceResponse(
                        m.getUser().getId(),
                        m.getUser().getName(),
                        m.getRole(),
                        m.getBalance()))
                .toList();

        return new GroupResponse(
                group.getId(),
                group.getOwner().getId(),
                group.getName(),
                group.getActiveVehicle() != null ? group.getActiveVehicle().getId() : null,
                group.getBaseDistanceKm(),
                group.getFuelPricePerLiter(),
                group.getStatus(),
                group.getCreatedAt(),
                memberResponses
        );
    }
}
