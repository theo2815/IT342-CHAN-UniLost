package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.CampusDTO;
import edu.cit.chan.unilost.entity.CampusEntity;
import edu.cit.chan.unilost.repository.CampusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Campus management service.
 *
 * Phase 1 — Backend Project Setup
 */
// TODO: [Phase 4] Add method to find campuses near a GeoJSON point
@Service
@RequiredArgsConstructor
public class CampusService {

    private final CampusRepository campusRepository;

    public CampusDTO createCampus(CampusDTO campusDTO) {
        CampusEntity campus = new CampusEntity();
        campus.setId(campusDTO.getId());
        campus.setName(campusDTO.getName());
        campus.setDomainWhitelist(campusDTO.getDomainWhitelist());
        if (campusDTO.getCenterCoordinates() != null && campusDTO.getCenterCoordinates().length == 2) {
            campus.setCenterCoordinates(new GeoJsonPoint(
                    campusDTO.getCenterCoordinates()[0],
                    campusDTO.getCenterCoordinates()[1]));
        }

        CampusEntity saved = campusRepository.save(campus);
        return convertToDTO(saved);
    }

    public List<CampusDTO> getAllCampuses() {
        return campusRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<CampusDTO> getCampusById(String id) {
        return campusRepository.findById(id)
                .map(this::convertToDTO);
    }

    public Optional<CampusDTO> getCampusByDomain(String domain) {
        return campusRepository.findByDomainWhitelist(domain)
                .map(this::convertToDTO);
    }

    public Optional<CampusDTO> updateCampus(String id, CampusDTO campusDTO) {
        return campusRepository.findById(id)
                .map(existing -> {
                    existing.setName(campusDTO.getName());
                    existing.setDomainWhitelist(campusDTO.getDomainWhitelist());
                    if (campusDTO.getCenterCoordinates() != null && campusDTO.getCenterCoordinates().length == 2) {
                        existing.setCenterCoordinates(new GeoJsonPoint(
                                campusDTO.getCenterCoordinates()[0],
                                campusDTO.getCenterCoordinates()[1]));
                    }
                    return convertToDTO(campusRepository.save(existing));
                });
    }

    public boolean deleteCampus(String id) {
        if (campusRepository.existsById(id)) {
            campusRepository.deleteById(id);
            return true;
        }
        return false;
    }

    private CampusDTO convertToDTO(CampusEntity campus) {
        CampusDTO dto = new CampusDTO();
        dto.setId(campus.getId());
        dto.setName(campus.getName());
        dto.setDomainWhitelist(campus.getDomainWhitelist());
        if (campus.getCenterCoordinates() != null) {
            dto.setCenterCoordinates(new double[]{
                    campus.getCenterCoordinates().getX(),
                    campus.getCenterCoordinates().getY()
            });
        }
        return dto;
    }
}
