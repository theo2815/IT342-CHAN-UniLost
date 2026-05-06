package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.ItemDTO;
import edu.cit.chan.unilost.dto.ItemRequest;
import edu.cit.chan.unilost.entity.CampusEntity;
import edu.cit.chan.unilost.entity.ItemEntity;
import edu.cit.chan.unilost.entity.ItemStatus;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.features.user.Role;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.repository.CampusRepository;
import edu.cit.chan.unilost.repository.ItemRepository;
import edu.cit.chan.unilost.util.DtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private static final int MAX_IMAGES = 3;

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;
    private final CloudinaryService cloudinaryService;
    private final MongoTemplate mongoTemplate;

    public ItemDTO createItem(ItemRequest request, String reporterEmail, List<MultipartFile> images) throws IOException {
        UserEntity reporter = userRepository.findByEmail(reporterEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (images != null && images.size() > MAX_IMAGES) {
            throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images allowed");
        }

        ItemEntity item = new ItemEntity();
        item.setReporterId(reporter.getId());
        item.setCampusId(request.getCampusId() != null ? request.getCampusId() : reporter.getUniversityTag());
        item.setTitle(request.getTitle());
        item.setDescription(request.getDescription());
        item.setType(request.getType());
        item.setStatus(ItemStatus.ACTIVE);
        item.setCategory(request.getCategory());
        item.setLocation(request.getLocation());
        if ((request.getLatitude() == null) != (request.getLongitude() == null)) {
            throw new IllegalArgumentException("Both latitude and longitude must be provided together, or both omitted");
        }
        item.setLatitude(request.getLatitude());
        item.setLongitude(request.getLongitude());
        item.setSecretDetailQuestion(request.getSecretDetailQuestion());
        item.setCreatedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());

        if (request.getDateLostFound() != null && !request.getDateLostFound().isEmpty()) {
            try {
                item.setDateLostFound(LocalDateTime.parse(request.getDateLostFound()));
            } catch (java.time.format.DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Expected ISO-8601 format (e.g., 2024-01-15T00:00:00)");
            }
        } else {
            item.setDateLostFound(LocalDateTime.now());
        }

        if (images != null && !images.isEmpty()) {
            List<String> imageUrls = cloudinaryService.uploadImages(images);
            item.setImageUrls(imageUrls);
        }

        ItemEntity saved = itemRepository.save(item);
        return convertToDTO(saved, reporter, null);
    }

    public Optional<ItemDTO> getItemById(String id, String currentEmail) {
        return itemRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> {
                    // Pre-load entities to avoid N+1 fallback queries in convertToDTO
                    UserEntity reporter = item.getReporterId() != null
                            ? userRepository.findById(item.getReporterId()).orElse(null) : null;
                    CampusEntity campus = item.getCampusId() != null
                            ? campusRepository.findById(item.getCampusId()).orElse(null) : null;

                    ItemDTO dto = convertToDTO(item, reporter, campus);
                    // Hide secretDetailQuestion from non-owners
                    if (currentEmail != null) {
                        Optional<UserEntity> currentUser = userRepository.findByEmail(currentEmail);
                        boolean isOwner = currentUser.isPresent()
                                && currentUser.get().getId().equals(item.getReporterId());
                        boolean isAdmin = currentUser.isPresent()
                                && currentUser.get().getRole() == Role.ADMIN;
                        if (!isOwner && !isAdmin) {
                            dto.setSecretDetailQuestion(null);
                        }
                    } else {
                        dto.setSecretDetailQuestion(null);
                    }
                    return dto;
                });
    }

    public Page<ItemDTO> searchItems(String keyword, String campusId, String category,
                                     String type, String status, Pageable pageable) {
        Query query = new Query();
        query.addCriteria(Criteria.where("isDeleted").is(false));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String escapedKeyword = Pattern.quote(keyword.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(escapedKeyword, "i"),
                    Criteria.where("description").regex(escapedKeyword, "i"),
                    Criteria.where("location").regex(escapedKeyword, "i")
            ));
        }

        if (campusId != null && !campusId.isEmpty()) {
            query.addCriteria(Criteria.where("campusId").is(campusId));
        }
        if (category != null && !category.isEmpty()) {
            query.addCriteria(Criteria.where("category").is(category));
        }
        if (type != null && !type.isEmpty()) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (status != null && !status.isEmpty()) {
            if (status.contains(",")) {
                List<String> statuses = Arrays.asList(status.split(","));
                query.addCriteria(Criteria.where("status").in(statuses));
            } else {
                query.addCriteria(Criteria.where("status").is(status));
            }
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ItemEntity.class);

        query.with(pageable);
        if (pageable.getSort().isUnsorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<ItemEntity> items = mongoTemplate.find(query, ItemEntity.class);
        List<ItemDTO> dtos = convertToDTOs(items);
        // Strip secretDetailQuestion from search results
        dtos.forEach(dto -> dto.setSecretDetailQuestion(null));

        return new PageImpl<>(dtos, pageable, total);
    }

    public Page<ItemDTO> getItemsByUser(String userId, Pageable pageable) {
        Page<ItemEntity> page = itemRepository.findByReporterIdAndIsDeletedFalse(userId, pageable);
        List<ItemDTO> dtos = convertToDTOs(page.getContent());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    public Page<ItemDTO> getItemsByCampus(String campusId, Pageable pageable) {
        Page<ItemEntity> page = itemRepository.findByCampusIdAndIsDeletedFalse(campusId, pageable);
        List<ItemDTO> dtos = convertToDTOs(page.getContent());
        dtos.forEach(dto -> dto.setSecretDetailQuestion(null));
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    @Transactional
    public Optional<ItemDTO> updateItem(String id, ItemRequest request, String currentEmail,
                                        List<MultipartFile> newImages) throws IOException {
        return itemRepository.findByIdAndIsDeletedFalse(id)
                .map(existingItem -> {
                    UserEntity currentUser = userRepository.findByEmail(currentEmail)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    verifyOwnershipOrAdmin(existingItem, currentUser);

                    // Prevent editing items that are no longer ACTIVE
                    if (existingItem.getStatus() != ItemStatus.ACTIVE) {
                        throw new IllegalArgumentException("Cannot edit an item that is " + existingItem.getStatus().name().toLowerCase());
                    }

                    if (newImages != null && newImages.size() > MAX_IMAGES) {
                        throw new IllegalArgumentException("Maximum " + MAX_IMAGES + " images allowed");
                    }

                    if (request.getTitle() != null) existingItem.setTitle(request.getTitle());
                    if (request.getDescription() != null) existingItem.setDescription(request.getDescription());
                    if (request.getCategory() != null) existingItem.setCategory(request.getCategory());
                    if (request.getLocation() != null) existingItem.setLocation(request.getLocation());
                    if (request.getLatitude() != null) existingItem.setLatitude(request.getLatitude());
                    if (request.getLongitude() != null) existingItem.setLongitude(request.getLongitude());
                    if (request.getSecretDetailQuestion() != null)
                        existingItem.setSecretDetailQuestion(request.getSecretDetailQuestion());
                    if (request.getDateLostFound() != null && !request.getDateLostFound().isEmpty()) {
                        try {
                            existingItem.setDateLostFound(LocalDateTime.parse(request.getDateLostFound()));
                        } catch (java.time.format.DateTimeParseException e) {
                            throw new IllegalArgumentException("Invalid date format. Expected ISO-8601 format (e.g., 2024-01-15T00:00:00)");
                        }
                    }

                    if (newImages != null && !newImages.isEmpty()) {
                        try {
                            // Delete old images from Cloudinary before uploading new ones
                            cloudinaryService.deleteImages(existingItem.getImageUrls());
                            List<String> imageUrls = cloudinaryService.uploadImages(newImages);
                            existingItem.setImageUrls(imageUrls);
                        } catch (IOException e) {
                            throw new IllegalArgumentException("Failed to upload images: " + e.getMessage());
                        }
                    }

                    existingItem.setUpdatedAt(LocalDateTime.now());
                    return convertToDTO(itemRepository.save(existingItem), currentUser, null);
                });
    }

    public boolean softDeleteItem(String id, String currentEmail) {
        return itemRepository.findByIdAndIsDeletedFalse(id)
                .map(item -> {
                    UserEntity currentUser = userRepository.findByEmail(currentEmail)
                            .orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    verifyOwnershipOrAdmin(item, currentUser);

                    // Prevent deleting items that have been claimed or are in handover
                    if (item.getStatus() == ItemStatus.CLAIMED
                            || item.getStatus() == ItemStatus.PENDING_OWNER_CONFIRMATION
                            || item.getStatus() == ItemStatus.HANDED_OVER) {
                        throw new IllegalArgumentException("Cannot delete an item that has been " + item.getStatus().name().toLowerCase());
                    }

                    // Clean up Cloudinary images
                    if (item.getImageUrls() != null && !item.getImageUrls().isEmpty()) {
                        try {
                            cloudinaryService.deleteImages(item.getImageUrls());
                        } catch (Exception e) {
                            // Log but don't block deletion
                        }
                    }

                    item.setDeleted(true);
                    item.setDeletedAt(LocalDateTime.now());
                    item.setUpdatedAt(LocalDateTime.now());
                    itemRepository.save(item);
                    return true;
                })
                .orElse(false);
    }

    public List<ItemDTO> getMapItems(String campusId, String type, Integer limit) {
        Query query = new Query();
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("status").in(
                ItemStatus.ACTIVE.name(),
                ItemStatus.CLAIMED.name(),
                ItemStatus.PENDING_OWNER_CONFIRMATION.name()));

        // Include all items; items without coordinates are given approximate positions 
        // by the frontend based on their campusId.

        if (campusId != null && !campusId.isEmpty()) {
            query.addCriteria(Criteria.where("campusId").is(campusId));
        }
        if (type != null && !type.isEmpty()) {
            if (!type.equals("LOST") && !type.equals("FOUND")) {
                throw new IllegalArgumentException("Type must be LOST or FOUND");
            }
            query.addCriteria(Criteria.where("type").is(type));
        }

        query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        int cappedLimit = Math.min(Math.max(limit != null ? limit : 100, 20), 150);
        query.limit(cappedLimit);

        List<ItemEntity> items = mongoTemplate.find(query, ItemEntity.class);
        List<ItemDTO> dtos = convertToDTOs(items);
        dtos.forEach(dto -> dto.setSecretDetailQuestion(null));
        return dtos;
    }

    // --- Batch DTO conversion to fix N+1 queries ---

    private List<ItemDTO> convertToDTOs(List<ItemEntity> items) {
        if (items.isEmpty()) return List.of();

        // Batch-fetch all referenced reporters and campuses
        Set<String> reporterIds = items.stream()
                .map(ItemEntity::getReporterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        Set<String> campusIds = items.stream()
                .map(ItemEntity::getCampusId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Map<String, UserEntity> usersById = userRepository.findAllById(reporterIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        Map<String, CampusEntity> campusesById = campusRepository.findAllById(campusIds).stream()
                .collect(Collectors.toMap(CampusEntity::getId, Function.identity()));

        return items.stream()
                .map(item -> convertToDTO(item,
                        usersById.get(item.getReporterId()),
                        campusesById.get(item.getCampusId())))
                .toList();
    }

    private ItemDTO convertToDTO(ItemEntity item, UserEntity reporter, CampusEntity campus) {
        ItemDTO dto = new ItemDTO();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setType(item.getType());
        dto.setStatus(item.getStatus().name());
        dto.setCategory(item.getCategory());
        dto.setLocation(item.getLocation());
        dto.setLatitude(item.getLatitude());
        dto.setLongitude(item.getLongitude());
        dto.setImageUrls(item.getImageUrls());
        dto.setSecretDetailQuestion(item.getSecretDetailQuestion());
        dto.setDateLostFound(item.getDateLostFound());
        dto.setCreatedAt(item.getCreatedAt());
        dto.setUpdatedAt(item.getUpdatedAt());
        dto.setReporterId(item.getReporterId());
        dto.setCampusId(item.getCampusId());
        dto.setFlagCount(item.getFlagCount());
        dto.setFlagReasons(item.getFlagReasons());

        // Resolve reporter — use provided entity or fetch if needed
        UserEntity resolvedReporter = reporter;
        if (resolvedReporter == null && item.getReporterId() != null) {
            resolvedReporter = userRepository.findById(item.getReporterId()).orElse(null);
        }
        if (resolvedReporter != null) {
            dto.setReporter(DtoMapper.toUserSummaryDTO(resolvedReporter));
        }

        // Resolve campus — use provided entity or fetch if needed
        CampusEntity resolvedCampus = campus;
        if (resolvedCampus == null && item.getCampusId() != null) {
            resolvedCampus = campusRepository.findById(item.getCampusId()).orElse(null);
        }
        if (resolvedCampus != null) {
            dto.setCampus(DtoMapper.toCampusDTO(resolvedCampus));
        }

        return dto;
    }

    private void verifyOwnershipOrAdmin(ItemEntity item, UserEntity currentUser) {
        boolean isOwner = item.getReporterId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenException("You do not have permission to modify this item");
        }
    }
}
