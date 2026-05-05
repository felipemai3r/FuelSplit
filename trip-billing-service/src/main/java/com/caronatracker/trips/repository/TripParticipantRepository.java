package com.caronatracker.trips.repository;

import com.caronatracker.trips.domain.model.TripParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TripParticipantRepository extends JpaRepository<TripParticipant, UUID> {

    List<TripParticipant> findByUserIdAndPaidFalse(String userId);
}
