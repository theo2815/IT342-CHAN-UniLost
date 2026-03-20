package edu.cit.chan.unilost.config;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import lombok.RequiredArgsConstructor;
import org.bson.conversions.Bson;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoIndexInitializer {

    private final MongoTemplate mongoTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void createIndexes() {
        var db = mongoTemplate.getDb();
        var claimsCollection = db.getCollection("claims");

        // Partial unique index: only one PENDING claim per user per item
        Bson pendingKeys = Indexes.compoundIndex(
                Indexes.ascending("itemId"),
                Indexes.ascending("claimantId")
        );
        IndexOptions pendingOptions = new IndexOptions()
                .name("idx_pending_claim_unique")
                .unique(true)
                .partialFilterExpression(Filters.eq("status", "PENDING"));
        claimsCollection.createIndex(pendingKeys, pendingOptions);

        // Compound index for (itemId, status)
        Bson itemStatusKeys = Indexes.compoundIndex(
                Indexes.ascending("itemId"),
                Indexes.ascending("status")
        );
        claimsCollection.createIndex(itemStatusKeys, new IndexOptions().name("idx_item_status"));

        // Compound index for (itemId, claimantId, status)
        Bson dupCheckKeys = Indexes.compoundIndex(
                Indexes.ascending("itemId"),
                Indexes.ascending("claimantId"),
                Indexes.ascending("status")
        );
        claimsCollection.createIndex(dupCheckKeys, new IndexOptions().name("idx_item_claimant_status"));
    }
}
