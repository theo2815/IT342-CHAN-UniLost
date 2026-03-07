package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.SchoolDTO;
import edu.cit.chan.unilost.service.SchoolService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {
    
    private final SchoolService schoolService;
    
    @PostMapping
    public ResponseEntity<SchoolDTO> createSchool(@RequestBody SchoolDTO schoolDTO) {
        SchoolDTO createdSchool = schoolService.createSchool(schoolDTO);
        return new ResponseEntity<>(createdSchool, HttpStatus.CREATED);
    }
    
    @GetMapping
    public ResponseEntity<List<SchoolDTO>> getAllSchools() {
        List<SchoolDTO> schools = schoolService.getAllSchools();
        return ResponseEntity.ok(schools);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SchoolDTO> getSchoolById(@PathVariable String id) {
        return schoolService.getSchoolById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/domain/{emailDomain}")
    public ResponseEntity<SchoolDTO> getSchoolByEmailDomain(@PathVariable String emailDomain) {
        return schoolService.getSchoolByEmailDomain(emailDomain)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SchoolDTO> updateSchool(@PathVariable String id, @RequestBody SchoolDTO schoolDTO) {
        return schoolService.updateSchool(id, schoolDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSchool(@PathVariable String id) {
        if (schoolService.deleteSchool(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
