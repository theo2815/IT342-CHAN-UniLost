package edu.cit.chan.unilost.shared.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Filters;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.jspecify.annotations.NonNull;

import jakarta.annotation.PostConstruct;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    @NonNull
    protected String getDatabaseName() {
        return databaseName;
    }

    @Override
    @NonNull
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    /**
     * Activates Spring's @Transactional support for MongoDB. Without this bean,
     * every @Transactional annotation in the codebase is a silent no-op. Atlas
     * already runs a replica set so multi-document transactions are supported.
     */
    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory factory) {
        return new MongoTransactionManager(factory);
    }

    @PostConstruct
    public void createIndexes() {
        var db = mongoClient().getDatabase(getDatabaseName());
        var claimsCollection = db.getCollection("claims");

        // Partial unique index: only one PENDING claim per user per item (C1 fix)
        Bson pendingKeys = Indexes.compoundIndex(
                Indexes.ascending("itemId"),
                Indexes.ascending("claimantId")
        );
        IndexOptions pendingOptions = new IndexOptions()
                .name("idx_pending_claim_unique")
                .unique(true)
                .partialFilterExpression(Filters.eq("status", "PENDING"));
        claimsCollection.createIndex(pendingKeys, pendingOptions);

        // M3: Compound index for (itemId, status) — used by findByItemIdAndStatus
        Bson itemStatusKeys = Indexes.compoundIndex(
                Indexes.ascending("itemId"),
                Indexes.ascending("status")
        );
        claimsCollection.createIndex(itemStatusKeys, new IndexOptions().name("idx_item_status"));

        // M3: Compound index for (itemId, claimantId, status) — used by duplicate check
        Bson dupCheckKeys = Indexes.compoundIndex(
                Indexes.ascending("itemId"),
                Indexes.ascending("claimantId"),
                Indexes.ascending("status")
        );
        claimsCollection.createIndex(dupCheckKeys, new IndexOptions().name("idx_item_claimant_status"));

        // H9: Index for getMyClaims (findByClaimantId)
        claimsCollection.createIndex(Indexes.ascending("claimantId"),
                new IndexOptions().name("idx_claimant"));

        // H9: Index for getIncomingClaims (findByFinderId)
        claimsCollection.createIndex(Indexes.ascending("finderId"),
                new IndexOptions().name("idx_finder"));
    }
}
