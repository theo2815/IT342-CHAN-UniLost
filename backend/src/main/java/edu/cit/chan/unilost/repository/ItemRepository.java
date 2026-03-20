package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.ItemEntity;
import edu.cit.chan.unilost.entity.ItemStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends MongoRepository<ItemEntity, String> {

    Optional<ItemEntity> findByIdAndIsDeletedFalse(String id);

    Page<ItemEntity> findByReporterIdAndIsDeletedFalse(String reporterId, Pageable pageable);

    Page<ItemEntity> findByCampusIdAndIsDeletedFalse(String campusId, Pageable pageable);

    // Admin queries
    List<ItemEntity> findByCampusIdAndIsDeletedFalse(String campusId);

    Page<ItemEntity> findByCampusIdAndFlagCountGreaterThanAndIsDeletedFalse(String campusId, int flagCount, Pageable pageable);

    long countByCampusIdAndIsDeletedFalse(String campusId);

    long countByCampusIdAndStatusAndIsDeletedFalse(String campusId, ItemStatus status);
}
