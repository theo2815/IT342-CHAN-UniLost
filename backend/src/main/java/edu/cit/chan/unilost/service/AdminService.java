package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.*;
import edu.cit.chan.unilost.entity.*;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final ClaimRepository claimRepository;
    private final CampusRepository campusRepository;
    private final MongoTemplate mongoTemplate;

    // ── Dashboard Stats ────────────────────────────────────

    public Map<String, Object> getDashboardStats(String adminEmail) {
        UserEntity admin = resolveAdmin(adminEmail);
        String campusId = admin.getUniversityTag();

        long totalUsers = userRepository.countByUniversityTag(campusId);
        long suspendedUsers = userRepository.countByUniversityTagAndAccountStatus(campusId, AccountStatus.SUSPENDED);
        long activeItems = itemRepository.countByCampusIdAndStatusAndIsDeletedFalse(campusId, ItemStatus.ACTIVE);
        long totalItems = itemRepository.countByCampusIdAndIsDeletedFalse(campusId);

        // Count pending claims using aggregation instead of loading all items
        long pendingClaims = countPendingClaimsForCampus(campusId);

        // Count recovered (CLAIMED + HANDED_OVER + RETURNED) this month
        long recoveredThisMonth = countRecoveredThisMonth(campusId);

        // Resolve campus name
        String campusName = campusRepository.findById(campusId)
                .map(CampusEntity::getName).orElse("Unknown Campus");

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("campusName", campusName);
        stats.put("totalUsers", totalUsers);
        stats.put("suspendedUsers", suspendedUsers);
        stats.put("activeItems", activeItems);
        stats.put("totalItems", totalItems);
        stats.put("pendingClaims", pendingClaims);
        stats.put("recoveredThisMonth", recoveredThisMonth);
        return stats;
    }

    // ── Campus Items ───────────────────────────────────────

    public Page<ItemDTO> getCampusItems(String adminEmail, String keyword, String type,
                                         String status, Pageable pageable) {
        UserEntity admin = resolveAdmin(adminEmail);
        String campusId = admin.getUniversityTag();

        Query query = new Query();
        query.addCriteria(Criteria.where("campusId").is(campusId));
        query.addCriteria(Criteria.where("isDeleted").is(false));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String escaped = Pattern.quote(keyword.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("title").regex(escaped, "i"),
                    Criteria.where("description").regex(escaped, "i")
            ));
        }
        if (type != null && !type.isEmpty()) {
            query.addCriteria(Criteria.where("type").is(type));
        }
        if (status != null && !status.isEmpty()) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ItemEntity.class);
        query.with(pageable);
        if (pageable.getSort().isUnsorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<ItemEntity> items = mongoTemplate.find(query, ItemEntity.class);
        List<ItemDTO> dtos = convertItemsToDTOs(items);
        return new PageImpl<>(dtos, pageable, total);
    }

    // ── Flagged Items ──────────────────────────────────────

    public Page<ItemDTO> getFlaggedItems(String adminEmail, Pageable pageable) {
        UserEntity admin = resolveAdmin(adminEmail);
        String campusId = admin.getUniversityTag();

        Page<ItemEntity> page = itemRepository.findByCampusIdAndFlagCountGreaterThanAndIsDeletedFalse(
                campusId, 0, pageable);
        List<ItemDTO> dtos = convertItemsToDTOs(page.getContent());
        return new PageImpl<>(dtos, pageable, page.getTotalElements());
    }

    // ── Update Item Status (admin action) ──────────────────

    public ItemDTO updateItemStatus(String itemId, String newStatus, String adminEmail) {
        UserEntity admin = resolveAdmin(adminEmail);
        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        verifyCampusAccess(admin, item.getCampusId());

        // Validate status transition
        ItemStatus parsedStatus;
        try {
            parsedStatus = ItemStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        Set<ItemStatus> validStatuses = Set.of(ItemStatus.ACTIVE, ItemStatus.HIDDEN, ItemStatus.TURNED_OVER_TO_OFFICE, ItemStatus.RETURNED);
        if (!validStatuses.contains(parsedStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus);
        }

        // RETURNED can only be set on TURNED_OVER_TO_OFFICE items
        if (parsedStatus == ItemStatus.RETURNED && item.getStatus() != ItemStatus.TURNED_OVER_TO_OFFICE) {
            throw new IllegalArgumentException("Only items turned over to office can be marked as returned");
        }

        item.setStatus(parsedStatus);
        item.setUpdatedAt(LocalDateTime.now());
        ItemEntity saved = itemRepository.save(item);
        return convertItemToDTO(saved, null, null);
    }

    // ── Force Delete Item ──────────────────────────────────

    public void forceDeleteItem(String itemId, String adminEmail) {
        UserEntity admin = resolveAdmin(adminEmail);
        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        verifyCampusAccess(admin, item.getCampusId());

        item.setDeleted(true);
        item.setDeletedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
    }

    // ── Campus Users ───────────────────────────────────────

    public Page<UserDTO> getCampusUsers(String adminEmail, String keyword, String role,
                                         String accountStatus, Pageable pageable) {
        UserEntity admin = resolveAdmin(adminEmail);
        String campusId = admin.getUniversityTag();

        Query query = new Query();
        query.addCriteria(Criteria.where("universityTag").is(campusId));

        if (keyword != null && !keyword.trim().isEmpty()) {
            String escaped = Pattern.quote(keyword.trim());
            query.addCriteria(new Criteria().orOperator(
                    Criteria.where("fullName").regex(escaped, "i"),
                    Criteria.where("email").regex(escaped, "i")
            ));
        }
        if (role != null && !role.isEmpty()) {
            query.addCriteria(Criteria.where("role").is(role));
        }
        if (accountStatus != null && !accountStatus.isEmpty()) {
            query.addCriteria(Criteria.where("accountStatus").is(accountStatus));
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), UserEntity.class);
        query.with(pageable);
        if (pageable.getSort().isUnsorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<UserEntity> users = mongoTemplate.find(query, UserEntity.class);
        List<UserDTO> dtos = convertUsersToDTOs(users);
        return new PageImpl<>(dtos, pageable, total);
    }

    // ── Suspend / Reactivate User ──────────────────────────

    public UserDTO updateUserStatus(String userId, String newStatus, String adminEmail) {
        UserEntity admin = resolveAdmin(adminEmail);
        UserEntity target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verifyCampusAccess(admin, target.getUniversityTag());

        AccountStatus parsedStatus;
        try {
            parsedStatus = AccountStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Use ACTIVE or SUSPENDED.");
        }

        if (parsedStatus != AccountStatus.ACTIVE && parsedStatus != AccountStatus.SUSPENDED) {
            throw new IllegalArgumentException("Invalid status. Use ACTIVE or SUSPENDED.");
        }

        // Prevent admins/faculty from being suspended by other admins
        if (target.getRole() != Role.STUDENT) {
            throw new ForbiddenException("Cannot change status of admin or faculty accounts");
        }

        target.setAccountStatus(parsedStatus);
        UserEntity saved = userRepository.save(target);
        return convertUserToDTO(saved);
    }

    // ── Campus Claims ──────────────────────────────────────

    public Page<ClaimDTO> getCampusClaims(String adminEmail, String status, Pageable pageable) {
        UserEntity admin = resolveAdmin(adminEmail);
        String campusId = admin.getUniversityTag();

        // Get campus item IDs using projection (only IDs, not full entities)
        Query itemIdQuery = new Query(Criteria.where("campusId").is(campusId).and("isDeleted").is(false));
        itemIdQuery.fields().include("_id");
        List<String> campusItemIds = mongoTemplate.find(itemIdQuery, ItemEntity.class)
                .stream().map(ItemEntity::getId).toList();

        if (campusItemIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        Query query = new Query();
        query.addCriteria(Criteria.where("itemId").in(campusItemIds));
        if (status != null && !status.isEmpty()) {
            query.addCriteria(Criteria.where("status").is(status));
        }

        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ClaimEntity.class);
        query.with(pageable);
        if (pageable.getSort().isUnsorted()) {
            query.with(Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        List<ClaimEntity> claims = mongoTemplate.find(query, ClaimEntity.class);
        List<ClaimDTO> dtos = convertClaimsToDTOs(claims);
        return new PageImpl<>(dtos, pageable, total);
    }

    // ── Flag Item (User-facing) ────────────────────────────

    public void flagItem(String itemId, String reason, String userEmail) {
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // Prevent duplicate flags
        if (item.getFlaggedBy().contains(user.getId())) {
            throw new IllegalArgumentException("You have already flagged this item");
        }

        // Prevent flagging own item
        if (user.getId().equals(item.getReporterId())) {
            throw new IllegalArgumentException("You cannot flag your own item");
        }

        Set<String> validReasons = Set.of("SPAM", "INAPPROPRIATE", "FAKE", "DUPLICATE");
        if (!validReasons.contains(reason)) {
            throw new IllegalArgumentException("Invalid flag reason. Use: SPAM, INAPPROPRIATE, FAKE, or DUPLICATE");
        }

        item.getFlaggedBy().add(user.getId());
        item.getFlagReasons().add(reason);
        item.setFlagCount(item.getFlagCount() + 1);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);
    }

    // ── Analytics ──────────────────────────────────────────

    public Map<String, Object> getAnalytics(String adminEmail) {
        UserEntity admin = resolveAdmin(adminEmail);
        String campusId = admin.getUniversityTag();

        Criteria baseCriteria = Criteria.where("campusId").is(campusId).and("isDeleted").is(false);

        // Use count queries instead of loading all items
        long totalItems = itemRepository.countByCampusIdAndIsDeletedFalse(campusId);
        long lostCount = mongoTemplate.count(Query.query(baseCriteria.and("type").is("LOST")), ItemEntity.class);
        // Re-create criteria for each query to avoid mutation
        long foundCount = mongoTemplate.count(Query.query(
                Criteria.where("campusId").is(campusId).and("isDeleted").is(false).and("type").is("FOUND")), ItemEntity.class);

        // Status counts using individual count queries
        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (ItemStatus s : ItemStatus.values()) {
            long count = itemRepository.countByCampusIdAndStatusAndIsDeletedFalse(campusId, s);
            if (count > 0) statusCounts.put(s.name(), count);
        }

        // Top categories — use projection to only load category field
        Query catQuery = Query.query(Criteria.where("campusId").is(campusId).and("isDeleted").is(false)
                .and("category").ne(null));
        catQuery.fields().include("category");
        List<ItemEntity> catItems = mongoTemplate.find(catQuery, ItemEntity.class);
        Map<String, Long> categoryCounts = catItems.stream()
                .collect(Collectors.groupingBy(ItemEntity::getCategory, Collectors.counting()));
        List<Map<String, Object>> topCategories = categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> cat = new LinkedHashMap<>();
                    cat.put("category", e.getKey());
                    cat.put("count", e.getValue());
                    return cat;
                }).toList();

        // Top locations — use projection
        Query locQuery = Query.query(Criteria.where("campusId").is(campusId).and("isDeleted").is(false)
                .and("location").ne(null).and("location").ne(""));
        locQuery.fields().include("location");
        List<ItemEntity> locItems = mongoTemplate.find(locQuery, ItemEntity.class);
        Map<String, Long> locationCounts = locItems.stream()
                .collect(Collectors.groupingBy(ItemEntity::getLocation, Collectors.counting()));
        List<Map<String, Object>> topLocations = locationCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    Map<String, Object> loc = new LinkedHashMap<>();
                    loc.put("location", e.getKey());
                    loc.put("count", e.getValue());
                    return loc;
                }).toList();

        // Recovery rate
        long resolved = statusCounts.getOrDefault("CLAIMED", 0L)
                + statusCounts.getOrDefault("HANDED_OVER", 0L)
                + statusCounts.getOrDefault("RETURNED", 0L);
        double recoveryRate = totalItems == 0 ? 0 : (double) resolved / totalItems * 100;

        Map<String, Object> analytics = new LinkedHashMap<>();
        analytics.put("totalItems", totalItems);
        analytics.put("lostCount", lostCount);
        analytics.put("foundCount", foundCount);
        analytics.put("statusCounts", statusCounts);
        analytics.put("topCategories", topCategories);
        analytics.put("topLocations", topLocations);
        analytics.put("recoveryRate", Math.round(recoveryRate * 10.0) / 10.0);
        analytics.put("resolvedCount", resolved);
        return analytics;
    }

    // ── Cross-Campus Stats (Faculty / Super Admin) ────────

    public List<Map<String, Object>> getCrossCampusStats() {
        List<CampusEntity> campuses = campusRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (CampusEntity campus : campuses) {
            String cId = campus.getId();
            long userCount = userRepository.countByUniversityTag(cId);
            long itemCount = itemRepository.countByCampusIdAndIsDeletedFalse(cId);
            long claimedCount = itemRepository.countByCampusIdAndStatusAndIsDeletedFalse(cId, ItemStatus.CLAIMED)
                    + itemRepository.countByCampusIdAndStatusAndIsDeletedFalse(cId, ItemStatus.HANDED_OVER)
                    + itemRepository.countByCampusIdAndStatusAndIsDeletedFalse(cId, ItemStatus.RETURNED);
            double recoveryRate = itemCount == 0 ? 0 : Math.round((double) claimedCount / itemCount * 1000.0) / 10.0;

            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("id", campus.getId());
            entry.put("universityCode", campus.getUniversityCode());
            entry.put("campusName", campus.getCampusName());
            entry.put("name", campus.getName());
            entry.put("shortLabel", campus.getShortLabel());
            entry.put("domainWhitelist", campus.getDomainWhitelist());
            entry.put("userCount", userCount);
            entry.put("itemCount", itemCount);
            entry.put("recoveryRate", recoveryRate);
            result.add(entry);
        }

        return result;
    }

    // ── Helper Methods ─────────────────────────────────────

    private UserEntity resolveAdmin(String adminEmail) {
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        if (admin.getRole() != Role.ADMIN && admin.getRole() != Role.FACULTY) {
            throw new ForbiddenException("Access denied: admin role required");
        }
        return admin;
    }

    private void verifyCampusAccess(UserEntity admin, String targetCampusId) {
        if (!admin.getUniversityTag().equals(targetCampusId)) {
            throw new ForbiddenException("You can only manage resources within your campus");
        }
    }

    private long countPendingClaimsForCampus(String campusId) {
        // Use ID-only projection to avoid loading full item entities
        Query itemIdQuery = new Query(Criteria.where("campusId").is(campusId).and("isDeleted").is(false));
        itemIdQuery.fields().include("_id");
        List<String> campusItemIds = mongoTemplate.find(itemIdQuery, ItemEntity.class)
                .stream().map(ItemEntity::getId).toList();
        if (campusItemIds.isEmpty()) return 0;
        return mongoTemplate.count(Query.query(
                Criteria.where("itemId").in(campusItemIds).and("status").is("PENDING")), ClaimEntity.class);
    }

    private long countRecoveredThisMonth(String campusId) {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Query query = new Query();
        query.addCriteria(Criteria.where("campusId").is(campusId));
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("status").in("CLAIMED", "HANDED_OVER", "RETURNED"));
        query.addCriteria(Criteria.where("updatedAt").gte(startOfMonth));
        return mongoTemplate.count(query, ItemEntity.class);
    }

    // ── DTO Conversions (batch) ────────────────────────────

    private List<ItemDTO> convertItemsToDTOs(List<ItemEntity> items) {
        if (items.isEmpty()) return List.of();

        Set<String> reporterIds = items.stream().map(ItemEntity::getReporterId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> campusIds = items.stream().map(ItemEntity::getCampusId)
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<String, UserEntity> usersById = userRepository.findAllById(reporterIds).stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity()));
        Map<String, CampusEntity> campusesById = campusRepository.findAllById(campusIds).stream()
                .collect(Collectors.toMap(CampusEntity::getId, Function.identity()));

        return items.stream()
                .map(item -> convertItemToDTO(item, usersById.get(item.getReporterId()),
                        campusesById.get(item.getCampusId())))
                .toList();
    }

    private ItemDTO convertItemToDTO(ItemEntity item, UserEntity reporter, CampusEntity campus) {
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

        UserEntity resolvedReporter = reporter;
        if (resolvedReporter == null && item.getReporterId() != null) {
            resolvedReporter = userRepository.findById(item.getReporterId()).orElse(null);
        }
        if (resolvedReporter != null) {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(resolvedReporter.getId());
            userDTO.setEmail(resolvedReporter.getEmail());
            userDTO.setFullName(resolvedReporter.getFullName());
            userDTO.setUniversityTag(resolvedReporter.getUniversityTag());
            userDTO.setKarmaScore(resolvedReporter.getKarmaScore());
            userDTO.setRole(resolvedReporter.getRole().name());
            dto.setReporter(userDTO);
        }

        CampusEntity resolvedCampus = campus;
        if (resolvedCampus == null && item.getCampusId() != null) {
            resolvedCampus = campusRepository.findById(item.getCampusId()).orElse(null);
        }
        if (resolvedCampus != null) {
            CampusDTO campusDTO = new CampusDTO();
            campusDTO.setId(resolvedCampus.getId());
            campusDTO.setName(resolvedCampus.getName());
            campusDTO.setDomainWhitelist(resolvedCampus.getDomainWhitelist());
            dto.setCampus(campusDTO);
        }

        return dto;
    }

    private List<UserDTO> convertUsersToDTOs(List<UserEntity> users) {
        if (users.isEmpty()) return List.of();

        Set<String> campusIds = users.stream().map(UserEntity::getUniversityTag)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<String, CampusEntity> campusesById = campusRepository.findAllById(campusIds).stream()
                .collect(Collectors.toMap(CampusEntity::getId, Function.identity()));

        return users.stream().map(user -> {
            UserDTO dto = convertUserToDTO(user);
            CampusEntity campus = campusesById.get(user.getUniversityTag());
            if (campus != null) {
                CampusDTO campusDTO = new CampusDTO();
                campusDTO.setId(campus.getId());
                campusDTO.setName(campus.getName());
                campusDTO.setDomainWhitelist(campus.getDomainWhitelist());
                dto.setCampus(campusDTO);
            }
            return dto;
        }).toList();
    }

    private UserDTO convertUserToDTO(UserEntity user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setUniversityTag(user.getUniversityTag());
        dto.setKarmaScore(user.getKarmaScore());
        dto.setRole(user.getRole().name());
        dto.setAccountStatus(user.getAccountStatus().name());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    private List<ClaimDTO> convertClaimsToDTOs(List<ClaimEntity> claims) {
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
            ClaimDTO dto = new ClaimDTO();
            dto.setId(claim.getId());
            dto.setStatus(claim.getStatus().name());
            dto.setProvidedAnswer(claim.getProvidedAnswer());
            dto.setMessage(claim.getMessage());
            dto.setCreatedAt(claim.getCreatedAt());
            dto.setUpdatedAt(claim.getUpdatedAt());

            ItemEntity item = itemsById.get(claim.getItemId());
            if (item != null) {
                dto.setItemId(item.getId());
                dto.setItemTitle(item.getTitle());
                dto.setItemType(item.getType());
                dto.setItemImageUrl(item.getImageUrls() != null && !item.getImageUrls().isEmpty()
                        ? item.getImageUrls().get(0) : null);
            }

            UserEntity claimant = usersById.get(claim.getClaimantId());
            if (claimant != null) {
                dto.setClaimantId(claimant.getId());
                dto.setClaimantName(claimant.getFullName());
                CampusEntity campus = campusesById.get(claimant.getUniversityTag());
                if (campus != null) dto.setClaimantSchool(campus.getName());
            }

            UserEntity finder = usersById.get(claim.getFinderId());
            if (finder != null) {
                dto.setFinderId(finder.getId());
                dto.setFinderName(finder.getFullName());
            }

            return dto;
        }).toList();
    }
}
