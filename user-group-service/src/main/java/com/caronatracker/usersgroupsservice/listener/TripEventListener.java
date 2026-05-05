package com.caronatracker.usersgroupsservice.listener;

import com.caronatracker.usersgroupsservice.event.TripRegisteredEvent;
import com.caronatracker.usersgroupsservice.repository.GroupMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class TripEventListener {

    private final GroupMemberRepository groupMemberRepository;

    @RabbitListener(queues = "trip.registered.queue")
    public void onTripRegistered(TripRegisteredEvent event) {
        UUID groupId = UUID.fromString(event.groupId());

        for (TripRegisteredEvent.ParticipantDto participant : event.participants()) {
            UUID userId = UUID.fromString(participant.userId());
            groupMemberRepository.findByGroupIdAndUserId(groupId, userId)
                    .ifPresentOrElse(
                            member -> {
                                member.setBalance(member.getBalance().add(participant.amountOwed()));
                                groupMemberRepository.save(member);
                            },
                            () -> log.warn("GroupMember not found for groupId={} userId={}", groupId, userId)
                    );
        }
    }
}
