
package edu.cit.chan.unilost.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.jspecify.annotations.NonNull;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    @NonNull
    protected String getDatabaseName() {
        return "unilost";
    }

    @Override
    @NonNull
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }
}
