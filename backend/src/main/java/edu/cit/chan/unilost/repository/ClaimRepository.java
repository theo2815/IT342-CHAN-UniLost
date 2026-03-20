package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.ClaimEntity;
import edu.cit.chan.unilost.entity.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends MongoRepository<ClaimEntity, String> {

    List<ClaimEntity> findByItemId(String itemId);

    Page<ClaimEntity> findByItemId(String itemId, Pageable pageable);

    List<ClaimEntity> findByClaimantId(String claimantId);

    Page<ClaimEntity> findByClaimantId(String claimantId, Pageable pageable);

    List<ClaimEntity> findByItemIdAndStatus(String itemId, ClaimStatus status);

    List<ClaimEntity> findByFinderId(String finderId);

    Page<ClaimEntity> findByFinderId(String finderId, Pageable pageable);

    Optional<ClaimEntity> findByItemIdAndClaimantIdAndStatusIn(String itemId, String claimantId, List<ClaimStatus> statuses);
}
