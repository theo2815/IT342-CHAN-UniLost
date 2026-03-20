package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.ItemEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ItemRepository extends MongoRepository<ItemEntity, String> {

    Optional<ItemEntity> findByIdAndIsDeletedFalse(String id);

    Page<ItemEntity> findByReporterIdAndIsDeletedFalse(String reporterId, Pageable pageable);

    Page<ItemEntity> findByCampusIdAndIsDeletedFalse(String campusId, Pageable pageable);
}
