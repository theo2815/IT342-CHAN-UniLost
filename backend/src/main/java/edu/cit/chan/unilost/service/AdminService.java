package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.dto.*;
import edu.cit.chan.unilost.entity.*;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.campus.CampusRepository;
import edu.cit.chan.unilost.features.user.AccountStatus;
import edu.cit.chan.unilost.features.user.Role;
import edu.cit.chan.unilost.features.user.UserDTO;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.repository.*;
import edu.cit.chan.unilost.util.DtoMapper;
import org.bson.Document;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
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
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;

    // ── Dashboard Stats ────────────────────────────────────

    public Map<String, Object> getDashboardStats(String adminEmail) {
        resolveAdmin(adminEmail);

        long totalUsers = userRepository.count();
        long suspendedUsers = mongoTemplate.count(
                Query.query(Criteria.where("accountStatus").is(AccountStatus.SUSPENDED.name())),
                UserEntity.class);
        long activeItems = mongoTemplate.count(
                Query.query(Criteria.where("status").is(ItemStatus.ACTIVE.name()).and("isDeleted").is(false)),
                ItemEntity.class);
        long totalItems = mongoTemplate.count(
                Query.query(Criteria.where("isDeleted").is(false)),
                ItemEntity.class);

        long pendingClaims = mongoTemplate.count(
                Query.query(Criteria.where("status").is("PENDING")),
                ClaimEntity.class);

        long recoveredThisMonth = countRecoveredThisMonth();

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("campusName", "All Campuses");
        stats.put("totalUsers", totalUsers);
        stats.put("suspendedUsers", suspendedUsers);
        stats.put("activeItems", activeItems);
        stats.put("totalItems", totalItems);
        stats.put("pendingClaims", pendingClaims);
        stats.put("recoveredThisMonth", recoveredThisMonth);
        return stats;
    }

    // ── All Items ─────────────────────────────────────────

    public Page<ItemDTO> getCampusItems(String adminEmail, String keyword, String type,
                                         String status, Pageable pageable) {
        resolveAdmin(adminEmail);

        Query query = new Query();
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
        resolveAdmin(adminEmail);

        Query query = new Query();
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("flagCount").gt(0));
        long total = mongoTemplate.count(Query.of(query).limit(-1).skip(-1), ItemEntity.class);
        query.with(pageable);
        List<ItemEntity> items = mongoTemplate.find(query, ItemEntity.class);
        List<ItemDTO> dtos = convertItemsToDTOs(items);
        return new PageImpl<>(dtos, pageable, total);
    }

    // ── Update Item Status (admin action) ──────────────────

    public ItemDTO updateItemStatus(String itemId, String newStatus, String adminEmail) {
        resolveAdmin(adminEmail);
        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

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

        String oldStatus = item.getStatus().name();
        item.setStatus(parsedStatus);
        item.setUpdatedAt(LocalDateTime.now());
        ItemEntity saved = itemRepository.save(item);

        auditLogService.log("UPDATE_ITEM_STATUS", "ITEM", itemId, adminEmail,
                "Changed item '" + item.getTitle() + "' status from " + oldStatus + " to " + newStatus,
                Map.of("oldStatus", oldStatus, "newStatus", newStatus, "itemTitle", item.getTitle()));

        return convertItemToDTO(saved, null, null);
    }

    // ── Force Delete Item ──────────────────────────────────

    public void forceDeleteItem(String itemId, String adminEmail) {
        resolveAdmin(adminEmail);
        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        item.setDeleted(true);
        item.setDeletedAt(LocalDateTime.now());
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);

        auditLogService.log("DELETE_ITEM", "ITEM", itemId, adminEmail,
                "Soft-deleted item: " + item.getTitle(),
                Map.of("itemTitle", item.getTitle()));
    }

    // ── All Users ─────────────────────────────────────────

    public Page<UserDTO> getCampusUsers(String adminEmail, String keyword, String role,
                                         String accountStatus, Pageable pageable) {
        resolveAdmin(adminEmail);

        Query query = new Query();

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
        resolveAdmin(adminEmail);
        UserEntity target = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        AccountStatus parsedStatus;
        try {
            parsedStatus = AccountStatus.valueOf(newStatus);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status. Use ACTIVE or SUSPENDED.");
        }

        if (parsedStatus != AccountStatus.ACTIVE && parsedStatus != AccountStatus.SUSPENDED) {
            throw new IllegalArgumentException("Invalid status. Use ACTIVE or SUSPENDED.");
        }

        // Prevent admin from being suspended
        if (target.getRole() == Role.ADMIN) {
            throw new ForbiddenException("Cannot change status of admin accounts");
        }

        String oldStatus = target.getAccountStatus().name();
        target.setAccountStatus(parsedStatus);
        UserEntity saved = userRepository.save(target);

        auditLogService.log("UPDATE_USER_STATUS", "USER", userId, adminEmail,
                "Changed user '" + target.getFullName() + "' status from " + oldStatus + " to " + newStatus,
                Map.of("oldStatus", oldStatus, "newStatus", newStatus, "userName", target.getFullName()));

        return convertUserToDTO(saved);
    }

    // ── All Claims ────────────────────────────────────────

    public Page<ClaimDTO> getCampusClaims(String adminEmail, String status, Pageable pageable) {
        resolveAdmin(adminEmail);

        Query query = new Query();
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

        notificationService.notifyItemFlagged(item.getReporterId(), item.getTitle(), itemId);

        // Notify admin when flag count reaches threshold
        if (item.getFlagCount() >= 3) {
            List<UserEntity> admins = userRepository.findByRole(Role.ADMIN);
            for (UserEntity admin : admins) {
                notificationService.notifyAdminFlagThreshold(
                        admin.getId(), item.getTitle(), itemId, item.getFlagCount());
            }
        }
    }

    // ── Analytics ──────────────────────────────────────────

    public Map<String, Object> getAnalytics(String adminEmail) {
        resolveAdmin(adminEmail);

        Map<String, Long> rawStatusCounts = aggregateCountByField(
                "items",
                Criteria.where("isDeleted").is(false).and("status").ne(null),
                "status");
        Map<String, Long> rawTypeCounts = aggregateCountByField(
                "items",
                Criteria.where("isDeleted").is(false).and("type").ne(null),
                "type");
        Map<String, Long> categoryCounts = aggregateCountByField(
                "items",
                Criteria.where("isDeleted").is(false)
                        .and("category").nin(null, ""),
                "category");
        Map<String, Long> locationCounts = aggregateCountByField(
                "items",
                Criteria.where("isDeleted").is(false)
                        .and("location").nin(null, ""),
                "location");

        Map<String, Long> statusCounts = new LinkedHashMap<>();
        for (ItemStatus status : ItemStatus.values()) {
            long count = rawStatusCounts.getOrDefault(status.name(), 0L);
            if (count > 0) {
                statusCounts.put(status.name(), count);
            }
        }

        long totalItems = rawStatusCounts.values().stream().mapToLong(Long::longValue).sum();
        long lostCount = rawTypeCounts.getOrDefault("LOST", 0L);
        long foundCount = rawTypeCounts.getOrDefault("FOUND", 0L);

        List<Map<String, Object>> topCategories = topCountEntries(categoryCounts, "category");
        List<Map<String, Object>> topLocations = topCountEntries(locationCounts, "location");

        // Recovery rate
        long resolved = statusCounts.getOrDefault("CLAIMED", 0L)
                + statusCounts.getOrDefault("PENDING_OWNER_CONFIRMATION", 0L)
                + statusCounts.getOrDefault("RETURNED", 0L)
                + statusCounts.getOrDefault("HANDED_OVER", 0L);
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
        Map<String, Long> userCountsByCampus = aggregateCountByField(
                "users",
                Criteria.where("universityTag").ne(null),
                "universityTag");
        Map<String, Long> itemCountsByCampus = aggregateCountByField(
                "items",
                Criteria.where("isDeleted").is(false).and("campusId").ne(null),
                "campusId");
        Map<String, Long> claimedCountsByCampus = aggregateCountByField(
                "items",
                Criteria.where("isDeleted").is(false)
                        .and("campusId").ne(null)
                        .and("status").in(
                        ItemStatus.CLAIMED,
                        ItemStatus.PENDING_OWNER_CONFIRMATION,
                        ItemStatus.RETURNED,
                        ItemStatus.HANDED_OVER),
                "campusId");
        List<Map<String, Object>> result = new ArrayList<>();

        for (CampusEntity campus : campuses) {
            String cId = campus.getId();
            long userCount = userCountsByCampus.getOrDefault(cId, 0L);
            long itemCount = itemCountsByCampus.getOrDefault(cId, 0L);
            long claimedCount = claimedCountsByCampus.getOrDefault(cId, 0L);
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

    // ── System Health ─────────────────────────────────────

    public Map<String, Object> getSystemHealth() {
        Map<String, Object> health = new LinkedHashMap<>();

        // MongoDB connection status
        try {
            mongoTemplate.getDb().runCommand(new org.bson.Document("ping", 1));
            health.put("mongoStatus", "CONNECTED");
        } catch (Exception e) {
            health.put("mongoStatus", "DISCONNECTED");
        }

        // JVM Memory
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        Map<String, Object> memory = new LinkedHashMap<>();
        memory.put("usedMb", usedMemory / (1024 * 1024));
        memory.put("totalMb", totalMemory / (1024 * 1024));
        memory.put("maxMb", maxMemory / (1024 * 1024));
        memory.put("usagePercent", Math.round((double) usedMemory / maxMemory * 100));
        health.put("memory", memory);

        // Application uptime
        long uptimeMs = java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeHours = uptimeMs / (1000 * 60 * 60);
        long uptimeDays = uptimeHours / 24;
        health.put("uptimeMs", uptimeMs);
        health.put("uptimeFormatted", uptimeDays + "d " + (uptimeHours % 24) + "h");

        // Collection counts
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("users", userRepository.count());
        counts.put("items", itemRepository.count());
        counts.put("claims", claimRepository.count());
        counts.put("auditLogs", mongoTemplate.getCollection("audit_logs").countDocuments());
        health.put("collectionCounts", counts);

        // Java version
        health.put("javaVersion", System.getProperty("java.version"));

        return health;
    }

    // ── Bulk Actions ──────────────────────────────────────

    public List<ItemDTO> bulkUpdateItemStatus(List<String> itemIds, String newStatus, String adminEmail) {
        resolveAdmin(adminEmail);
        List<ItemDTO> results = new ArrayList<>();
        for (String itemId : itemIds) {
            try {
                ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId).orElse(null);
                if (item == null) continue;

                ItemStatus parsedStatus = ItemStatus.valueOf(newStatus);
                Set<ItemStatus> validStatuses = Set.of(ItemStatus.ACTIVE, ItemStatus.HIDDEN, ItemStatus.TURNED_OVER_TO_OFFICE, ItemStatus.RETURNED);
                if (!validStatuses.contains(parsedStatus)) continue;
                if (parsedStatus == ItemStatus.RETURNED && item.getStatus() != ItemStatus.TURNED_OVER_TO_OFFICE) continue;

                item.setStatus(parsedStatus);
                item.setUpdatedAt(LocalDateTime.now());
                ItemEntity saved = itemRepository.save(item);
                results.add(convertItemToDTO(saved, null, null));
            } catch (Exception ignored) {}
        }
        auditLogService.log("BULK_UPDATE_ITEMS", "ITEM", null, adminEmail,
                "Bulk updated " + results.size() + " items to " + newStatus,
                Map.of("itemIds", itemIds, "newStatus", newStatus, "count", results.size()));
        return results;
    }

    public int bulkDeleteItems(List<String> itemIds, String adminEmail) {
        resolveAdmin(adminEmail);
        int deleted = 0;
        for (String itemId : itemIds) {
            try {
                ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(itemId).orElse(null);
                if (item == null) continue;
                item.setDeleted(true);
                item.setDeletedAt(LocalDateTime.now());
                item.setUpdatedAt(LocalDateTime.now());
                itemRepository.save(item);
                deleted++;
            } catch (Exception ignored) {}
        }
        auditLogService.log("BULK_DELETE_ITEMS", "ITEM", null, adminEmail,
                "Bulk deleted " + deleted + " items",
                Map.of("itemIds", itemIds, "count", deleted));
        return deleted;
    }

    public List<UserDTO> bulkUpdateUserStatus(List<String> userIds, String newStatus, String adminEmail) {
        resolveAdmin(adminEmail);
        List<UserDTO> results = new ArrayList<>();
        for (String userId : userIds) {
            try {
                UserEntity target = userRepository.findById(userId).orElse(null);
                if (target == null) continue;
                if (target.getRole() == Role.ADMIN) continue;

                AccountStatus parsedStatus = AccountStatus.valueOf(newStatus);
                if (parsedStatus != AccountStatus.ACTIVE && parsedStatus != AccountStatus.SUSPENDED) continue;

                target.setAccountStatus(parsedStatus);
                UserEntity saved = userRepository.save(target);
                results.add(convertUserToDTO(saved));
            } catch (Exception ignored) {}
        }
        auditLogService.log("BULK_UPDATE_USERS", "USER", null, adminEmail,
                "Bulk updated " + results.size() + " users to " + newStatus,
                Map.of("userIds", userIds, "newStatus", newStatus, "count", results.size()));
        return results;
    }

    // ── Item Trends ───────────────────────────────────────

    public List<Map<String, Object>> getItemTrends(int months) {
        LocalDateTime startDate = LocalDateTime.now().minusMonths(months)
                .withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("isDeleted").is(false)
                        .and("createdAt").gte(startDate)),
                Aggregation.project()
                        .andExpression("year(createdAt)").as("year")
                        .andExpression("month(createdAt)").as("month"),
                Aggregation.group("year", "month").count().as("count"),
                Aggregation.sort(Sort.by(Sort.Direction.ASC, "_id.year", "_id.month"))
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation, "items", Document.class);

        List<Map<String, Object>> trends = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            Object idObj = doc.get("_id");
            if (idObj instanceof Document id) {
                String monthLabel = String.format("%d-%02d",
                        id.getInteger("year"), id.getInteger("month"));
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("month", monthLabel);
                entry.put("count", doc.getInteger("count"));
                trends.add(entry);
            }
        }
        return trends;
    }

    // ── Helper Methods ─────────────────────────────────────

    private Map<String, Long> aggregateCountByField(String collectionName, Criteria criteria, String fieldName) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.group(fieldName).count().as("count")
        );
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collectionName, Document.class);

        Map<String, Long> counts = new HashMap<>();
        for (Document row : results.getMappedResults()) {
            Object id = row.get("_id");
            Number count = (Number) row.get("count");
            if (id != null && count != null) {
                counts.put(id.toString(), count.longValue());
            }
        }
        return counts;
    }

    private List<Map<String, Object>> topCountEntries(Map<String, Long> counts, String keyName) {
        return counts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put(keyName, entry.getKey());
                    row.put("count", entry.getValue());
                    return row;
                })
                .toList();
    }

    private UserEntity resolveAdmin(String adminEmail) {
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Admin user not found"));
        if (admin.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Access denied: admin role required");
        }
        return admin;
    }

    private long countRecoveredThisMonth() {
        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        Query query = new Query();
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("status").in("CLAIMED", "PENDING_OWNER_CONFIRMATION", "RETURNED", "HANDED_OVER"));
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
            dto.setReporter(DtoMapper.toUserSummaryDTO(resolvedReporter));
        }

        CampusEntity resolvedCampus = campus;
        if (resolvedCampus == null && item.getCampusId() != null) {
            resolvedCampus = campusRepository.findById(item.getCampusId()).orElse(null);
        }
        if (resolvedCampus != null) {
            dto.setCampus(DtoMapper.toCampusPreviewDTO(resolvedCampus));
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
                dto.setCampus(DtoMapper.toCampusPreviewDTO(campus));
            }
            return dto;
        }).toList();
    }

    private UserDTO convertUserToDTO(UserEntity user) {
        UserDTO dto = DtoMapper.toUserSummaryDTO(user);
        if (user.getUniversityTag() != null) {
            campusRepository.findById(user.getUniversityTag())
                    .ifPresent(campus -> dto.setCampus(DtoMapper.toCampusPreviewDTO(campus)));
        }
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

            // Handover fields
            dto.setFinderMarkedReturnedAt(claim.getFinderMarkedReturnedAt());
            dto.setOwnerConfirmedReceivedAt(claim.getOwnerConfirmedReceivedAt());

            return dto;
        }).toList();
    }
}
