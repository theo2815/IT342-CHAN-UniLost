package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.SchoolDTO;
import edu.cit.chan.unilost.entity.SchoolEntity;
import edu.cit.chan.unilost.repository.SchoolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SchoolService {

    private final SchoolRepository schoolRepository;

    public SchoolDTO createSchool(SchoolDTO schoolDTO) {
        SchoolEntity school = new SchoolEntity();
        school.setName(schoolDTO.getName());
        school.setShortName(schoolDTO.getShortName());
        school.setCity(schoolDTO.getCity());
        school.setEmailDomain(schoolDTO.getEmailDomain());
        school.setCreatedAt(LocalDateTime.now());

        SchoolEntity savedSchool = schoolRepository.save(school);
        return convertToDTO(savedSchool);
    }

    public List<SchoolDTO> getAllSchools() {
        return schoolRepository.findAll()
                .stream()
                .filter(SchoolEntity::isActive)
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<SchoolDTO> getSchoolById(String id) {
        return schoolRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<SchoolDTO> getSchoolByEmailDomain(String emailDomain) {
        return schoolRepository.findByEmailDomain(emailDomain)
                .map(this::convertToDTO);
    }

    public Optional<SchoolDTO> updateSchool(String id, SchoolDTO schoolDTO) {
        return schoolRepository.findById(id)
                .map(existingSchool -> {
                    existingSchool.setName(schoolDTO.getName());
                    existingSchool.setShortName(schoolDTO.getShortName());
                    existingSchool.setCity(schoolDTO.getCity());
                    existingSchool.setEmailDomain(schoolDTO.getEmailDomain());
                    return convertToDTO(schoolRepository.save(existingSchool));
                });
    }

    public boolean deleteSchool(String id) {
        if (schoolRepository.existsById(id)) {
            schoolRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public SchoolEntity getSchoolEntityById(String id) {
        return schoolRepository.findById(id).orElse(null);
    }

    private SchoolDTO convertToDTO(SchoolEntity school) {
        return new SchoolDTO(
                school.getSchoolId(),
                school.getName(),
                school.getShortName(),
                school.getCity(),
                school.getEmailDomain());
    }
}
