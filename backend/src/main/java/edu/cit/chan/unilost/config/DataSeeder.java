package edu.cit.chan.unilost.config;

import edu.cit.chan.unilost.entity.AccountStatus;
import edu.cit.chan.unilost.entity.CampusEntity;
import edu.cit.chan.unilost.entity.Role;
import edu.cit.chan.unilost.entity.UserEntity;
import edu.cit.chan.unilost.repository.CampusRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${admin.seed-password:#{null}}")
    private String adminPassword;

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

        // CIT-U (single campus)
        createCampus("CIT-U-MAIN", "CIT-U", "Main Campus",
                "Cebu Institute of Technology - University", "CIT-U",
                "N. Bacalso Ave, Cebu City", "cit.edu",
                123.8811, 10.2946);

        // USC (2 campuses, same domain)
        // Plus Code: 7VXX+QGF, J. Alcantara St, Cebu City
        createCampus("USC-DOWNTOWN", "USC", "Downtown Campus",
                "University of San Carlos - Downtown Campus", "USC Downtown",
                "J. Alcantara St, Cebu City, 6000 Cebu", "usc.edu.ph",
                123.89880, 10.29944);
        // Plus Code: 9W27+R8R, Sitio Nasipit, Brgy, Cebu City
        createCampus("USC-TALAMBAN", "USC", "Talamban Campus",
                "University of San Carlos - Talamban Campus", "USC Talamban",
                "Sitio Nasipit, Talamban, Cebu City", "usc.edu.ph",
                123.91327, 10.35211);

        // USJ-R (2 campuses, same domain)
        // Plus Code: 7VVX+H2R, Cebu City
        createCampus("USJR-MAIN", "USJ-R", "Main Campus",
                "University of San Jose-Recoletos - Main Campus", "USJ-R Main",
                "Magallanes St, Cebu City, 6000 Cebu", "usjr.edu.ph",
                123.89752, 10.29399);
        // Plus Code: 7VQ6+XQR, CICCT, Cebu City
        createCampus("USJR-BASAK", "USJ-R", "Basak Campus",
                "University of San Jose-Recoletos - Basak Campus", "USJ-R Basak",
                "CICCT, Cebu City, 6000 Cebu", "usjr.edu.ph",
                123.86189, 10.28999);

        // UC (3 campuses, same domain)
        // Plus Code: 7VWW+RM, Cebu City
        createCampus("UC-MAIN", "UC", "Main Campus",
                "University of Cebu - Main Campus", "UC Main",
                "Sanciangko St, Cebu City, 6000 Cebu", "uc.edu.ph",
                123.89669, 10.29706);
        // Plus Code: 8WQ6+8M, Cebu City
        createCampus("UC-BANILAD", "UC", "Banilad Campus",
                "University of Cebu - Banilad Campus", "UC Banilad",
                "Gov. M. Cuenco Ave, Banilad, Cebu City", "uc.edu.ph",
                123.91169, 10.33831);
        // Plus Code: 8XG3+26, Mandaue, Cebu
        createCampus("UC-LM", "UC", "LM Campus",
                "University of Cebu - Lapu-Lapu and Mandaue Campus", "UC LM",
                "Mandaue, Cebu", "uc.edu.ph",
                123.95306, 10.32506);

        // UP Cebu (single campus)
        // Plus Code: 8VCX+W7, Cebu City
        createCampus("UP-CEBU", "UP Cebu", "Main Campus",
                "University of the Philippines Cebu", "UP Cebu",
                "Gorordo Ave, Cebu City, 6000 Cebu", "up.edu.ph",
                123.89819, 10.32231);

        // SWU PHINMA (single campus)
        // Plus Code: 8V3R+7G, Cebu City
        createCampus("SWU-MAIN", "SWU", "Main Campus",
                "Southwestern University PHINMA", "SWU",
                "Urgello St, Cebu City, 6000 Cebu", "swu.edu.ph",
                123.89131, 10.30319);

        // CNU (single campus)
        // Plus Code: 8V2W+MM, Cebu City
        createCampus("CNU-MAIN", "CNU", "Main Campus",
                "Cebu Normal University", "CNU",
                "Osmeña Blvd, Cebu City, 6000 Cebu", "cnu.edu.ph",
                123.89669, 10.30169);

        // CTU (single campus)
        // Plus Code: 7WW4+MJ, Cebu City
        createCampus("CTU-MAIN", "CTU", "Main Campus",
                "Cebu Technological University - Main Campus", "CTU",
                "M.J. Cuenco Ave, Cebu City, 6000 Cebu", "ctu.edu.ph",
                123.90656, 10.29669);

        // IAU (single campus)
        // Plus Code: 7XVG+4G, Lapu-Lapu, Cebu
        createCampus("IAU-MAIN", "IAU", "Main Campus",
                "Indiana Aerospace University", "IAU",
                "Lapu-Lapu, Cebu", "iau.edu.ph",
                123.97631, 10.29281);

        log.info("=== Campus seeding complete ({} campuses) ===", campusRepository.count());
    }

    private void createCampus(String id, String universityCode, String campusName,
                              String name, String shortLabel, String address,
                              String domain, double lng, double lat) {
        CampusEntity campus = new CampusEntity();
        campus.setId(id);
        campus.setUniversityCode(universityCode);
        campus.setCampusName(campusName);
        campus.setName(name);
        campus.setShortLabel(shortLabel);
        campus.setAddress(address);
        campus.setDomainWhitelist(domain);
        campus.setCenterCoordinates(new GeoJsonPoint(lng, lat));
        campusRepository.save(campus);
        log.info("  Seeded campus: {} - {} ({})", id, shortLabel, domain);
    }

    private void seedAdmin() {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.info("  admin.seed-password not set — skipping admin seeding");
            return;
        }
        seedOrUpdateAdmin("admin@cit.edu", "UniLost Admin", "CIT-U-MAIN");
        seedOrUpdateAdmin("admin@unilost.com", "UniLost Super Admin", "CIT-U-MAIN");
    }

    private void seedOrUpdateAdmin(String email, String fullName, String universityTag) {
        var existing = userRepository.findByEmail(email);
        if (existing.isPresent()) {
            UserEntity user = existing.get();
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
            userRepository.save(user);
            log.info("  Updated password for: {}", email);
            return;
        }

        UserEntity admin = new UserEntity();
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setFullName(fullName);
        admin.setUniversityTag(universityTag);
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);
        admin.setEmailVerified(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        log.info("  Seeded admin: {}", email);
    }
}
