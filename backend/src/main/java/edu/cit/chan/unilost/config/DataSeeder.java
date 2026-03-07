package edu.cit.chan.unilost.config;

import edu.cit.chan.unilost.entity.CampusEntity;
import edu.cit.chan.unilost.entity.UserEntity;
import edu.cit.chan.unilost.repository.CampusRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Seeds initial data on application startup.
 *
 * Phase 1 — Backend Project Setup
 */
// TODO: [Phase 4] Add sample items for development/testing
// TODO: [Phase 5] Add sample claims for development/testing
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CampusRepository campusRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            seedCampuses();
            seedAdmin();
        } catch (Exception e) {
            log.warn("DataSeeder failed (database may be temporarily unavailable): {}", e.getMessage());
        }
    }

    private void seedCampuses() {
        if (campusRepository.count() > 0) {
            return;
        }

        log.info("=== Seeding Cebu City Campuses ===");

        // [longitude, latitude] for GeoJsonPoint
        createCampus("CIT-U-MAIN", "Cebu Institute of Technology - University", "cit.edu", 123.8779, 10.2948);
        createCampus("USC-MAIN", "University of San Carlos - Main", "usc.edu.ph", 123.8988, 10.3001);
        createCampus("USJR-MAIN", "University of San Jose - Recoletos", "usjr.edu.ph", 123.8961, 10.2995);
        createCampus("UC-MAIN", "University of Cebu - Main", "uc.edu.ph", 123.9000, 10.3020);
        createCampus("UP-CEBU", "University of the Philippines Cebu", "up.edu.ph", 123.8853, 10.3231);
        createCampus("SWU-MAIN", "Southwestern University PHINMA", "swu.edu.ph", 123.8930, 10.3060);
        createCampus("CNU-MAIN", "Cebu Normal University", "cnu.edu.ph", 123.8920, 10.3050);
        createCampus("CTU-MAIN", "Cebu Technological University - Main", "ctu.edu.ph", 123.8975, 10.2935);

        log.info("=== Campus seeding complete ===");
    }

    private void createCampus(String id, String name, String domain, double lng, double lat) {
        CampusEntity campus = new CampusEntity();
        campus.setId(id);
        campus.setName(name);
        campus.setDomainWhitelist(domain);
        campus.setCenterCoordinates(new GeoJsonPoint(lng, lat));
        campusRepository.save(campus);
        log.info("  Seeded campus: {} ({})", id, domain);
    }

    private void seedAdmin() {
        String adminEmail = "admin@cit.edu";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        UserEntity admin = new UserEntity();
        admin.setEmail(adminEmail);
        admin.setPasswordHash(passwordEncoder.encode("admin123456"));
        admin.setFullName("UniLost Admin");
        admin.setUniversityTag("CIT-U-MAIN");
        admin.setRole("ADMIN");
        admin.setAccountStatus("ACTIVE");
        admin.setEmailVerified(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        log.info("  Seeded admin: {}", adminEmail);
    }
}
