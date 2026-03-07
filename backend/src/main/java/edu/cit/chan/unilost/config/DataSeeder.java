package edu.cit.chan.unilost.config;

import edu.cit.chan.unilost.entity.SchoolEntity;
import edu.cit.chan.unilost.entity.UserEntity;
import edu.cit.chan.unilost.repository.SchoolRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            seedSchools();
            seedSuperAdmin();
            seedAdmin();
        } catch (Exception e) {
            log.warn("DataSeeder failed (database may be temporarily unavailable): {}", e.getMessage());
        }
    }

    private void seedSchools() {
        if (schoolRepository.count() > 0) {
            return;
        }

        System.out.println("=== Seeding Cebu City Universities ===");

        createSchool("Cebu Institute of Technology - University", "CIT-U", "Cebu City", "cit.edu");
        createSchool("University of San Carlos", "USC", "Cebu City", "usc.edu.ph");
        createSchool("University of San Jose - Recoletos", "USJ-R", "Cebu City", "usjr.edu.ph");
        createSchool("University of Cebu", "UC", "Cebu City", "uc.edu.ph");
        createSchool("University of the Philippines Cebu", "UP Cebu", "Cebu City", "up.edu.ph");
        createSchool("Southwestern University PHINMA", "SWU", "Cebu City", "swu.edu.ph");
        createSchool("Cebu Normal University", "CNU", "Cebu City", "cnu.edu.ph");
        createSchool("Cebu Technological University", "CTU", "Cebu City", "ctu.edu.ph");

        System.out.println("=== School seeding complete ===");
    }

    private void createSchool(String name, String shortName, String city, String emailDomain) {
        SchoolEntity school = new SchoolEntity();
        school.setName(name);
        school.setShortName(shortName);
        school.setCity(city);
        school.setEmailDomain(emailDomain);
        school.setActive(true);
        school.setCreatedAt(LocalDateTime.now());
        schoolRepository.save(school);
        System.out.println("  Seeded: " + shortName + " (" + emailDomain + ")");
    }

    private void seedSuperAdmin() {
        String adminEmail = "admin@unilost.com";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        // Create a default super admin account
        // Find CIT-U as default school for the admin
        SchoolEntity citU = schoolRepository.findByEmailDomain("cit.edu").orElse(null);

        UserEntity admin = new UserEntity();
        admin.setFirstName("UniLost");
        admin.setLastName("Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("admin123456"));
        admin.setStudentIdNumber("ADMIN-001");
        admin.setRole("SUPER_ADMIN");
        admin.setSchool(citU);
        admin.setVerified(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        System.out.println("  Seeded Super Admin: " + adminEmail);
    }

    private void seedAdmin() {
        String adminEmail = "admin@cit.edu";
        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        // Create a default campus admin account
        SchoolEntity citU = schoolRepository.findByEmailDomain("cit.edu").orElse(null);

        UserEntity admin = new UserEntity();
        admin.setFirstName("Campus");
        admin.setLastName("Admin");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode("admin123456"));
        admin.setStudentIdNumber("ADMIN-002");
        admin.setRole("ADMIN");
        admin.setSchool(citU);
        admin.setVerified(true);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        userRepository.save(admin);

        System.out.println("  Seeded Campus Admin: " + adminEmail);
    }
}
