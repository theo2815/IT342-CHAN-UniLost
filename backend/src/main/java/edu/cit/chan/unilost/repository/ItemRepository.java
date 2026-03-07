package edu.cit.chan.unilost.repository;

import edu.cit.chan.unilost.entity.ItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends MongoRepository<ItemEntity, String> {

    List<ItemEntity> findByReporterId(String reporterId);

    List<ItemEntity> findByCampusId(String campusId);

    List<ItemEntity> findByType(String type);

    List<ItemEntity> findByIsDeletedFalse();

    List<ItemEntity> findByCampusIdAndIsDeletedFalse(String campusId);

    // TODO: [Phase 4] Add GeoJSON spatial queries (findByLocationNear, etc.)
    // TODO: [Phase 4] Add full-text search on title and description
}
