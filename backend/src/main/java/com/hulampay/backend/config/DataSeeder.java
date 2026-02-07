
package com.hulampay.backend.config;

import com.hulampay.backend.entity.SchoolEntity;
import com.hulampay.backend.entity.UserEntity;
import com.hulampay.backend.repository.SchoolRepository;
import com.hulampay.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (schoolRepository.count() == 0) {
            SchoolEntity school = new SchoolEntity();
            school.setName("Mapua University");
            school.setCity("Manila");
            school.setEmailDomain("mapua.edu.ph");
            school = schoolRepository.save(school);
            System.out.println("Seeded School: " + school.getName());

            if (userRepository.count() == 0) {
                UserEntity user = new UserEntity();
                user.setFirstName("Theo");
                user.setLastName("Chan");
                user.setEmail("theo@mapua.edu.ph");
                user.setPassword("password123"); // In a real app, this should be hashed
                user.setStudentIdNumber("202112345");
                user.setSchool(school);
                user.setCreatedAt(LocalDateTime.now());
                userRepository.save(user);
                System.out.println("Seeded User: " + user.getFirstName());
            }
        }
    }
}
