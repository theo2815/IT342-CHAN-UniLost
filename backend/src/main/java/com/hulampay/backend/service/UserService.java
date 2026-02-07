package com.hulampay.backend.service;

import com.hulampay.backend.dto.SchoolDTO;
import com.hulampay.backend.dto.UserDTO;
import com.hulampay.backend.dto.UserRegistrationDTO;
import com.hulampay.backend.entity.SchoolEntity;
import com.hulampay.backend.entity.UserEntity;
import com.hulampay.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SchoolService schoolService;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    public UserDTO createUser(UserRegistrationDTO registrationDTO) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationDTO.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Check if student ID already exists
        if (userRepository.existsByStudentIdNumber(registrationDTO.getStudentIdNumber())) {
            throw new RuntimeException("Student ID number already exists");
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
        user.setCreatedAt(LocalDateTime.now());

        // Set school reference if provided
        if (registrationDTO.getSchoolId() != null) {
            SchoolEntity school = schoolService.getSchoolEntityById(registrationDTO.getSchoolId());
            user.setSchool(school);
        }

        UserEntity savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO authenticate(String email, String password) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

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
                    existingUser.setFirstName(updateDTO.getFirstName());
                    existingUser.setLastName(updateDTO.getLastName());
                    existingUser.setEmail(updateDTO.getEmail());
                    existingUser.setAddress(updateDTO.getAddress());
                    existingUser.setPhoneNumber(updateDTO.getPhoneNumber());
                    existingUser.setProfilePicture(updateDTO.getProfilePicture());
                    existingUser.setStudentIdNumber(updateDTO.getStudentIdNumber());

                    // Update password only if provided
                    if (updateDTO.getPassword() != null && !updateDTO.getPassword().isEmpty()) {
                        existingUser.setPassword(passwordEncoder.encode(updateDTO.getPassword()));
                    }

                    // Update school reference if provided
                    if (updateDTO.getSchoolId() != null) {
                        SchoolEntity school = schoolService.getSchoolEntityById(updateDTO.getSchoolId());
                        existingUser.setSchool(school);
                    }

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
        dto.setCreatedAt(user.getCreatedAt());

        // Convert school if present
        if (user.getSchool() != null) {
            SchoolEntity school = user.getSchool();
            dto.setSchoolId(school.getSchoolId());
            dto.setSchool(new SchoolDTO(
                    school.getSchoolId(),
                    school.getName(),
                    school.getCity(),
                    school.getEmailDomain()));
        }

        return dto;
    }
}
