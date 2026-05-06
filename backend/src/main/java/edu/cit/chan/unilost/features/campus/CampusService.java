package edu.cit.chan.unilost.features.campus;

import edu.cit.chan.unilost.features.item.ItemRepository;
import edu.cit.chan.unilost.features.item.ItemStatus;
import edu.cit.chan.unilost.shared.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final ItemRepository itemRepository;

    public CampusDTO createCampus(CampusDTO campusDTO) {
        if (campusDTO.getId() != null && campusRepository.existsById(campusDTO.getId())) {
            throw new IllegalArgumentException("A campus with ID '" + campusDTO.getId() + "' already exists");
        }

        if (campusDTO.getDomainWhitelist() != null && !campusDTO.getDomainWhitelist().isBlank()
                && campusRepository.findByDomainWhitelist(campusDTO.getDomainWhitelist().trim()).isPresent()) {
            throw new IllegalArgumentException(
                    "A campus with domain '" + campusDTO.getDomainWhitelist().trim() + "' already exists");
        }

        CampusEntity campus = new CampusEntity();
        campus.setId(campusDTO.getId());
        campus.setUniversityCode(campusDTO.getUniversityCode());
        campus.setCampusName(campusDTO.getCampusName());
        campus.setName(campusDTO.getName());
        campus.setShortLabel(campusDTO.getShortLabel());
        campus.setAddress(campusDTO.getAddress());
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

    public List<CampusDTO> getCampusesByDomain(String domain) {
        return campusRepository.findAllByDomainWhitelist(domain)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<CampusDTO> updateCampus(String id, CampusDTO campusDTO) {
        return campusRepository.findById(id)
                .map(existing -> {
                    if (campusDTO.getUniversityCode() != null) existing.setUniversityCode(campusDTO.getUniversityCode());
                    if (campusDTO.getCampusName() != null) existing.setCampusName(campusDTO.getCampusName());
                    if (campusDTO.getName() != null) existing.setName(campusDTO.getName());
                    if (campusDTO.getShortLabel() != null) existing.setShortLabel(campusDTO.getShortLabel());
                    if (campusDTO.getAddress() != null) existing.setAddress(campusDTO.getAddress());
                    if (campusDTO.getDomainWhitelist() != null) existing.setDomainWhitelist(campusDTO.getDomainWhitelist());
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

    public List<Map<String, Object>> getCampusStats() {
        List<CampusEntity> allCampuses = campusRepository.findAll();
        return allCampuses.stream().map(campus -> {
            Map<String, Object> stat = new HashMap<>();
            stat.put("id", campus.getId());
            stat.put("name", campus.getName());
            stat.put("shortLabel", campus.getShortLabel());
            stat.put("activeItems", itemRepository.countByCampusIdAndStatusAndIsDeletedFalse(
                    campus.getId(), ItemStatus.ACTIVE));
            if (campus.getCenterCoordinates() != null) {
                stat.put("centerCoordinates", new double[]{
                        campus.getCenterCoordinates().getX(),
                        campus.getCenterCoordinates().getY()
                });
            }
            return stat;
        }).collect(Collectors.toList());
    }

    private CampusDTO convertToDTO(CampusEntity campus) {
        return DtoMapper.toCampusDTO(campus);
    }
}
