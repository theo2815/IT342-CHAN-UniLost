package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.CampusDTO;
import edu.cit.chan.unilost.service.CampusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Campus CRUD endpoints.
 *
 * Phase 1 — Backend Project Setup (campus management)
 */
@RestController
@RequestMapping("/api/campuses")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    @PostMapping
    public ResponseEntity<CampusDTO> createCampus(@RequestBody CampusDTO campusDTO) {
        CampusDTO created = campusService.createCampus(campusDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<CampusDTO>> getAllCampuses() {
        return ResponseEntity.ok(campusService.getAllCampuses());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CampusDTO> getCampusById(@PathVariable String id) {
        return campusService.getCampusById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/domain/{domain}")
    public ResponseEntity<List<CampusDTO>> getCampusesByDomain(@PathVariable String domain) {
        List<CampusDTO> campuses = campusService.getCampusesByDomain(domain);
        if (campuses.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(campuses);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CampusDTO> updateCampus(@PathVariable String id, @RequestBody CampusDTO campusDTO) {
        return campusService.updateCampus(id, campusDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCampus(@PathVariable String id) {
        if (campusService.deleteCampus(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}
