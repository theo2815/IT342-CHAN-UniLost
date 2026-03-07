package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.ClaimEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRepository extends MongoRepository<ClaimEntity, String> {

    List<ClaimEntity> findByItemId(String itemId);

    List<ClaimEntity> findByClaimantId(String claimantId);

    List<ClaimEntity> findByItemIdAndStatus(String itemId, String status);

    // TODO: [Phase 5] Add query for pending claims count per item
}
