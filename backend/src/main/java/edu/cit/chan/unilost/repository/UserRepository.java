package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.AccountStatus;
import edu.cit.chan.unilost.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);

    List<UserEntity> findByUniversityTag(String universityTag);

    Page<UserEntity> findByUniversityTag(String universityTag, Pageable pageable);

    Optional<UserEntity> findByPasswordResetToken(String token);

    long countByUniversityTag(String universityTag);

    long countByUniversityTagAndAccountStatus(String universityTag, AccountStatus accountStatus);

    List<UserEntity> findByAccountStatusOrderByKarmaScoreDesc(AccountStatus accountStatus, Pageable pageable);

    List<UserEntity> findByUniversityTagAndAccountStatusOrderByKarmaScoreDesc(
            String universityTag, AccountStatus accountStatus, Pageable pageable);

    List<UserEntity> findByAccountStatusAndKarmaScoreGreaterThanOrderByKarmaScoreDesc(
            AccountStatus accountStatus, int minScore, Pageable pageable);

    List<UserEntity> findByUniversityTagAndAccountStatusAndKarmaScoreGreaterThanOrderByKarmaScoreDesc(
            String universityTag, AccountStatus accountStatus, int minScore, Pageable pageable);
}
