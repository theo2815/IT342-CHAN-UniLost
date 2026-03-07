package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<UserEntity, String> {
    
    Optional<UserEntity> findByEmail(String email);
    
    Optional<UserEntity> findByStudentIdNumber(String studentIdNumber);
    
    boolean existsByEmail(String email);
    
    boolean existsByStudentIdNumber(String studentIdNumber);
}
