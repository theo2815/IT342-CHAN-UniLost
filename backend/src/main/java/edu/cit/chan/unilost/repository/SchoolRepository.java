package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.SchoolEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchoolRepository extends MongoRepository<SchoolEntity, String> {
    
    Optional<SchoolEntity> findByEmailDomain(String emailDomain);
    
    Optional<SchoolEntity> findByName(String name);
}
