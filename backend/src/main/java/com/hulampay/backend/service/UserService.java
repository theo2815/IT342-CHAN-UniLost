package com.hulampay.backend.service;

import com.hulampay.backend.dto.SchoolDTO;
import com.hulampay.backend.dto.UserDTO;
import com.hulampay.backend.dto.UserRegistrationDTO;
import com.hulampay.backend.entity.SchoolEntity;
import com.hulampay.backend.entity.UserEntity;
import com.hulampay.backend.repository.SchoolRepository;
import com.hulampay.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final SchoolService schoolService;
    private final PasswordEncoder passwordEncoder;

    public UserDTO createUser(UserRegistrationDTO registrationDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if student ID already exists (only if provided)
        if (registrationDTO.getStudentIdNumber() != null
                && !registrationDTO.getStudentIdNumber().isEmpty()
                && userRepository.existsByStudentIdNumber(registrationDTO.getStudentIdNumber())) {
            throw new RuntimeException("Student ID number already exists");
        }

        // Email domain validation: extract domain and match to a school
        String email = registrationDTO.getEmail();
        String emailDomain = extractEmailDomain(email);
        SchoolEntity school = schoolRepository.findByEmailDomain(emailDomain)
                .orElseThrow(() -> new RuntimeException(
                        "Email domain '" + emailDomain + "' is not recognized. Please use your university email."));

        if (!school.isActive()) {
            throw new RuntimeException("This university is currently not accepting new registrations.");
        }

        UserEntity user = new UserEntity();
        user.setFirstName(registrationDTO.getFirstName());
        user.setLastName(registrationDTO.getLastName());
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setAddress(registrationDTO.getAddress());
        user.setPhoneNumber(registrationDTO.getPhoneNumber());
        user.setProfilePicture(registrationDTO.getProfilePicture());
        user.setStudentIdNumber(registrationDTO.getStudentIdNumber());
        user.setSchool(school);
        user.setRole("STUDENT");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        UserEntity savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (user.isBanned()) {
            throw new RuntimeException("Your account has been suspended. Contact your campus admin.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        return convertToDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<UserDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::convertToDTO);
    }

    public Optional<UserDTO> updateUser(String id, UserRegistrationDTO updateDTO) {
        return userRepository.findById(id)
                .map(existingUser -> {
                    if (updateDTO.getFirstName() != null) {
                        existingUser.setFirstName(updateDTO.getFirstName());
                    }
                    if (updateDTO.getLastName() != null) {
                        existingUser.setLastName(updateDTO.getLastName());
                    }
                    if (updateDTO.getAddress() != null) {
                        existingUser.setAddress(updateDTO.getAddress());
                    }
                    if (updateDTO.getPhoneNumber() != null) {
                        existingUser.setPhoneNumber(updateDTO.getPhoneNumber());
                    }
                    if (updateDTO.getProfilePicture() != null) {
                        existingUser.setProfilePicture(updateDTO.getProfilePicture());
                    }

                    // Update password only if provided
                    if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
                    }

                    existingUser.setUpdatedAt(LocalDateTime.now());
                    return convertToDTO(userRepository.save(existingUser));
                });
    }

    public boolean deleteUser(String id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private String extractEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            throw new RuntimeException("Invalid email format");
        }
        return email.substring(email.indexOf("@") + 1).toLowerCase();
    }

    private UserDTO convertToDTO(UserEntity user) {
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setAddress(user.getAddress());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setProfilePicture(user.getProfilePicture());
        dto.setStudentIdNumber(user.getStudentIdNumber());
        dto.setRole(user.getRole());
        dto.setKarmaScore(user.getKarmaScore());
        dto.setVerified(user.isVerified());
        dto.setBanned(user.isBanned());
        dto.setCreatedAt(user.getCreatedAt());

        // Convert school if present
        if (user.getSchool() != null) {
            SchoolEntity school = user.getSchool();
            dto.setSchoolId(school.getSchoolId());
            dto.setSchool(new SchoolDTO(
                    school.getSchoolId(),
                    school.getName(),
                    school.getShortName(),
                    school.getCity(),
                    school.getEmailDomain()));
        }

        return dto;
    }
}
