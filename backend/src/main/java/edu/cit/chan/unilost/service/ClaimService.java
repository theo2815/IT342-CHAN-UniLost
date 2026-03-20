package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.ClaimDTO;
import edu.cit.chan.unilost.dto.ClaimRequest;
import edu.cit.chan.unilost.entity.*;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.repository.CampusRepository;
import edu.cit.chan.unilost.repository.ClaimRepository;
import edu.cit.chan.unilost.repository.ItemRepository;
import edu.cit.chan.unilost.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private static final String ROLE_ADMIN = "ADMIN";

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;

    public ClaimDTO submitClaim(ClaimRequest request, String claimantEmail) {
        UserEntity claimant = userRepository.findByEmail(claimantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (!"ACTIVE".equals(item.getStatus())) {
            throw new IllegalArgumentException("This item is no longer accepting claims");
        }

        if (claimant.getId().equals(item.getReporterId())) {
            throw new IllegalArgumentException("You cannot claim your own item");
        }

        // One active claim per user per item
        Optional<ClaimEntity> existing = claimRepository.findByItemIdAndClaimantIdAndStatusIn(
                item.getId(), claimant.getId(), List.of(ClaimStatus.PENDING));
        if (existing.isPresent()) {
            throw new IllegalArgumentException("You already have a pending claim on this item");
        }

        ClaimEntity claim = new ClaimEntity();
        claim.setItemId(item.getId());
        claim.setClaimantId(claimant.getId());
        claim.setFinderId(item.getReporterId());
        claim.setProvidedAnswer(request.getProvidedAnswer());
        claim.setMessage(request.getMessage());
        claim.setCreatedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());

        ClaimEntity saved = claimRepository.save(claim);
        return convertToDTO(saved, item, claimant, null, null, false);
    }

    public ClaimDTO getClaimById(String claimId, String currentEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isClaimant = currentUser.getId().equals(claim.getClaimantId());
        boolean isFinder = currentUser.getId().equals(claim.getFinderId());
        boolean isAdmin = ROLE_ADMIN.equals(currentUser.getRole());

        if (!isClaimant && !isFinder && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view this claim");
        }

        return convertToDTO(claim, null, null, null, null, isFinder || isAdmin);
    }

    public Page<ClaimDTO> getMyClaims(String claimantEmail, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(claimantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<ClaimEntity> claimPage = claimRepository.findByClaimantId(user.getId(), pageable);
        List<ClaimDTO> dtos = convertToDTOs(claimPage.getContent(), false);
        return new PageImpl<>(dtos, pageable, claimPage.getTotalElements());
    }

    public Page<ClaimDTO> getIncomingClaims(String finderEmail, Pageable pageable) {
        UserEntity user = userRepository.findByEmail(finderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Page<ClaimEntity> claimPage = claimRepository.findByFinderId(user.getId(), pageable);
        List<ClaimDTO> dtos = convertToDTOs(claimPage.getContent(), true);
        return new PageImpl<>(dtos, pageable, claimPage.getTotalElements());
    }

    public Page<ClaimDTO> getClaimsForItem(String itemId, String callerEmail, Pageable pageable) {
        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        UserEntity caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isFinder = caller.getId().equals(item.getReporterId());
        boolean isAdmin = ROLE_ADMIN.equals(caller.getRole());
        if (!isFinder && !isAdmin) {
            throw new ForbiddenException("Only the item poster can view claims on this item");
        }

        Page<ClaimEntity> claimPage = claimRepository.findByItemId(itemId, pageable);
        List<ClaimDTO> dtos = convertToDTOs(claimPage.getContent(), true);
        return new PageImpl<>(dtos, pageable, claimPage.getTotalElements());
    }

    @Transactional
    public ClaimDTO acceptClaim(String claimId, String finderEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity finder = userRepository.findByEmail(finderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verifyFinderOrAdmin(claim, finder);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Only pending claims can be accepted");
        }

        claim.setStatus(ClaimStatus.ACCEPTED);
        claim.setUpdatedAt(LocalDateTime.now());
        ClaimEntity saved = claimRepository.save(claim);

        // Auto-reject all other PENDING claims on the same item
        List<ClaimEntity> otherPending = claimRepository.findByItemIdAndStatus(claim.getItemId(), ClaimStatus.PENDING);
        for (ClaimEntity other : otherPending) {
            other.setStatus(ClaimStatus.REJECTED);
            other.setUpdatedAt(LocalDateTime.now());
        }
        if (!otherPending.isEmpty()) {
            claimRepository.saveAll(otherPending);
        }

        // Update item status to CLAIMED
        itemRepository.findByIdAndIsDeletedFalse(claim.getItemId()).ifPresent(i -> {
            i.setStatus("CLAIMED");
            i.setUpdatedAt(LocalDateTime.now());
            itemRepository.save(i);
        });

        return convertToDTO(saved, null, null, finder, null, true);
    }

    public ClaimDTO rejectClaim(String claimId, String finderEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity finder = userRepository.findByEmail(finderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verifyFinderOrAdmin(claim, finder);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Only pending claims can be rejected");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setUpdatedAt(LocalDateTime.now());
        ClaimEntity saved = claimRepository.save(claim);
        return convertToDTO(saved, null, null, finder, null, true);
    }

    public ClaimDTO cancelClaim(String claimId, String claimantEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity user = userRepository.findByEmail(claimantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!user.getId().equals(claim.getClaimantId())) {
            throw new ForbiddenException("You can only cancel your own claims");
        }

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Only pending claims can be cancelled");
        }

        claim.setStatus(ClaimStatus.CANCELLED);
        claim.setUpdatedAt(LocalDateTime.now());
        ClaimEntity saved = claimRepository.save(claim);
        return convertToDTO(saved, null, user, null, null, false);
    }

    // --- Batch DTO conversion to prevent N+1 queries ---

    private List<ClaimDTO> convertToDTOs(List<ClaimEntity> claims, boolean includeSecretQuestion) {
        if (claims.isEmpty()) return List.of();

        Set<String> itemIds = claims.stream().map(ClaimEntity::getItemId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> userIds = new HashSet<>();
        claims.forEach(c -> {
            if (c.getClaimantId() != null) userIds.add(c.getClaimantId());
            if (c.getFinderId() != null) userIds.add(c.getFinderId());
        });

        Map<String, ItemEntity> itemsById = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(ItemEntity::getId, Function.identity()));
        Map<String, UserEntity> usersById = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        Set<String> campusIds = usersById.values().stream()
                .map(UserEntity::getUniversityTag)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, CampusEntity> campusesById = campusRepository.findAllById(campusIds).stream()
                .collect(Collectors.toMap(CampusEntity::getId, Function.identity()));

        return claims.stream().map(claim -> {
            UserEntity claimant = usersById.get(claim.getClaimantId());
            CampusEntity claimantCampus = claimant != null && claimant.getUniversityTag() != null
                    ? campusesById.get(claimant.getUniversityTag()) : null;
            return convertToDTO(
                    claim,
                    itemsById.get(claim.getItemId()),
                    claimant,
                    usersById.get(claim.getFinderId()),
                    claimantCampus,
                    includeSecretQuestion
            );
        }).toList();
    }

    private ClaimDTO convertToDTO(ClaimEntity claim, ItemEntity item,
                                  UserEntity claimant, UserEntity finder,
                                  CampusEntity claimantCampus,
                                  boolean includeSecretQuestion) {
        ClaimDTO dto = new ClaimDTO();
        dto.setId(claim.getId());
        dto.setStatus(claim.getStatus().name());
        dto.setProvidedAnswer(claim.getProvidedAnswer());
        dto.setMessage(claim.getMessage());
        dto.setCreatedAt(claim.getCreatedAt());
        dto.setUpdatedAt(claim.getUpdatedAt());

        // Resolve item
        ItemEntity resolvedItem = item;
        if (resolvedItem == null && claim.getItemId() != null) {
            resolvedItem = itemRepository.findByIdAndIsDeletedFalse(claim.getItemId()).orElse(null);
        }
        if (resolvedItem != null) {
            dto.setItemId(resolvedItem.getId());
            dto.setItemTitle(resolvedItem.getTitle());
            dto.setItemType(resolvedItem.getType());
            dto.setItemImageUrl(
                    resolvedItem.getImageUrls() != null && !resolvedItem.getImageUrls().isEmpty()
                            ? resolvedItem.getImageUrls().get(0) : null
            );
            if (includeSecretQuestion) {
                dto.setSecretDetailQuestion(resolvedItem.getSecretDetailQuestion());
            }
        }

        // Resolve claimant
        UserEntity resolvedClaimant = claimant;
        if (resolvedClaimant == null && claim.getClaimantId() != null) {
            resolvedClaimant = userRepository.findById(claim.getClaimantId()).orElse(null);
        }
        if (resolvedClaimant != null) {
            dto.setClaimantId(resolvedClaimant.getId());
            dto.setClaimantName(resolvedClaimant.getFullName());

            CampusEntity resolvedCampus = claimantCampus;
            if (resolvedCampus == null && resolvedClaimant.getUniversityTag() != null) {
                resolvedCampus = campusRepository.findById(resolvedClaimant.getUniversityTag()).orElse(null);
            }
            if (resolvedCampus != null) {
                dto.setClaimantSchool(resolvedCampus.getName());
            }
        }

        // Resolve finder
        UserEntity resolvedFinder = finder;
        if (resolvedFinder == null && claim.getFinderId() != null) {
            resolvedFinder = userRepository.findById(claim.getFinderId()).orElse(null);
        }
        if (resolvedFinder != null) {
            dto.setFinderId(resolvedFinder.getId());
            dto.setFinderName(resolvedFinder.getFullName());
        }

        return dto;
    }

    private void verifyFinderOrAdmin(ClaimEntity claim, UserEntity user) {
        boolean isFinder = user.getId().equals(claim.getFinderId());
        boolean isAdmin = ROLE_ADMIN.equals(user.getRole());
        if (!isFinder && !isAdmin) {
            throw new ForbiddenException("Only the item poster can perform this action");
        }
    }
}
