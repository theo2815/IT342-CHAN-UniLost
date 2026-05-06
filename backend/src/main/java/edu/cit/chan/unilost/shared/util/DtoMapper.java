package edu.cit.chan.unilost.shared.util;

import edu.cit.chan.unilost.features.campus.CampusDTO;
import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.user.UserDTO;
import edu.cit.chan.unilost.features.user.UserEntity;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static UserDTO toUserSummaryDTO(UserEntity user) {
        if (user == null) {
            return null;
        }

        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setUniversityTag(user.getUniversityTag());
        dto.setKarmaScore(user.getKarmaScore());
        if (user.getRole() != null) {
            dto.setRole(user.getRole().name());
        }
        if (user.getAccountStatus() != null) {
            dto.setAccountStatus(user.getAccountStatus().name());
        }
        dto.setCreatedAt(user.getCreatedAt());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }

    public static CampusDTO toCampusDTO(CampusEntity campus) {
        if (campus == null) {
            return null;
        }

        CampusDTO dto = new CampusDTO();
        dto.setId(campus.getId());
        dto.setUniversityCode(campus.getUniversityCode());
        dto.setCampusName(campus.getCampusName());
        dto.setName(campus.getName());
        dto.setShortLabel(campus.getShortLabel());
        dto.setAddress(campus.getAddress());
        dto.setDomainWhitelist(campus.getDomainWhitelist());
        if (campus.getCenterCoordinates() != null) {
            dto.setCenterCoordinates(new double[]{
                    campus.getCenterCoordinates().getX(),
                    campus.getCenterCoordinates().getY()
            });
        }
        return dto;
    }

    public static CampusDTO toCampusPreviewDTO(CampusEntity campus) {
        if (campus == null) {
            return null;
        }

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
