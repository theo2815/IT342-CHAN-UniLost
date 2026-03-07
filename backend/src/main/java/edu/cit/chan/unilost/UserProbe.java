
package edu.cit.chan.unilost;

import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.bson.Document;
import lombok.RequiredArgsConstructor;

// Probe to check raw DB content
// @Component
@RequiredArgsConstructor
public class UserProbe implements CommandLineRunner {

    private final MongoTemplate mongoTemplate;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("\n========== USER PROBE ==========");

        // Fetch raw document to see how 'school' is stored
        Document userDoc = mongoTemplate.getCollection("users").find().first();
        if (userDoc != null) {
            System.out.println("User Raw Document: " + userDoc.toJson());
            Object schoolField = userDoc.get("school");
            System.out.println(
                    "School Field Class: " + (schoolField != null ? schoolField.getClass().getName() : "null"));
            System.out.println("School Field Value: " + schoolField);
        } else {
            System.out.println("No users found.");
        }

        System.out.println("================================\n");
    }
}
