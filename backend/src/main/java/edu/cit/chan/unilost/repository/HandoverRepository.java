package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.HandoverEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HandoverRepository extends MongoRepository<HandoverEntity, String> {

    Optional<HandoverEntity> findByClaimId(String claimId);

    Optional<HandoverEntity> findByItemId(String itemId);

    // TODO: [Phase 7] Add query for completed handovers by date range (analytics)
}
