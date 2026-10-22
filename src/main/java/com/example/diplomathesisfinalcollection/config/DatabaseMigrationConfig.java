package com.example.diplomathesisfinalcollection.config;


import com.example.diplomathesisfinalcollection.user.User;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

@Configuration
public class DatabaseMigrationConfig {

    @Bean
    public CommandLineRunner migrateExistingUsers(MongoTemplate mongoTemplate) {
        return args -> {
            System.out.println("🔍 Checking MongoDB Atlas for existing users...");

            // Find users where 'enabled' does not exist
            Query query = new Query(Criteria.where("enabled").exists(false));

            // Set 'enabled' to true
            Update update = new Update().set("enabled", true);

            // Execute the update
            long modifiedCount = mongoTemplate.updateMulti(query, update, User.class).getModifiedCount();

            if (modifiedCount > 0) {
                System.out.println("✅ MIGRATION SUCCESS: " + modifiedCount + " existing users have been enabled.");
            } else {
                System.out.println("👍 DB is up to date. No migration needed.");
            }
        };
    }
}