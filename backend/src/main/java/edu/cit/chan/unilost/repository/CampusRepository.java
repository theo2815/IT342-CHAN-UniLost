package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.CampusEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CampusRepository extends MongoRepository<CampusEntity, String> {

    Optional<CampusEntity> findByDomainWhitelist(String domainWhitelist);

    List<CampusEntity> findAllByDomainWhitelist(String domainWhitelist);

    Optional<CampusEntity> findByName(String name);

    List<CampusEntity> findByUniversityCode(String universityCode);
}
