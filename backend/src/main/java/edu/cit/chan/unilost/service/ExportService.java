package edu.cit.chan.unilost.service;

import edu.cit.chan.unilost.entity.ItemEntity;
import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.campus.CampusRepository;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService {

    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final CampusRepository campusRepository;
    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public String exportUsersCsv() {
        List<UserEntity> users = userRepository.findAll();
        Map<String, CampusEntity> campusMap = campusRepository.findAll().stream()
                .collect(Collectors.toMap(CampusEntity::getId, Function.identity()));

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Full Name,Email,Campus,Role,Account Status,Karma,Joined\n");

        for (UserEntity u : users) {
            CampusEntity campus = campusMap.get(u.getUniversityTag());
            sb.append(escapeCsv(u.getId())).append(',');
            sb.append(escapeCsv(u.getFullName())).append(',');
            sb.append(escapeCsv(u.getEmail())).append(',');
            sb.append(escapeCsv(campus != null ? campus.getName() : "")).append(',');
            sb.append(escapeCsv(u.getRole() != null ? u.getRole().name() : "")).append(',');
            sb.append(escapeCsv(u.getAccountStatus() != null ? u.getAccountStatus().name() : "")).append(',');
            sb.append(u.getKarmaScore()).append(',');
            sb.append(u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FMT) : "").append('\n');
        }
        return sb.toString();
    }

    public String exportItemsCsv() {
        Query query = new Query(Criteria.where("isDeleted").is(false));
        List<ItemEntity> items = mongoTemplate.find(query, ItemEntity.class);

        Map<String, UserEntity> userMap = userRepository.findAll().stream()
                .collect(Collectors.toMap(UserEntity::getId, Function.identity(), (a, b) -> a));

        StringBuilder sb = new StringBuilder();
        sb.append("ID,Title,Type,Status,Category,Location,Reporter,Flag Count,Created\n");

        for (ItemEntity item : items) {
            UserEntity reporter = userMap.get(item.getReporterId());
            sb.append(escapeCsv(item.getId())).append(',');
            sb.append(escapeCsv(item.getTitle())).append(',');
            sb.append(escapeCsv(item.getType())).append(',');
            sb.append(escapeCsv(item.getStatus() != null ? item.getStatus().name() : "")).append(',');
            sb.append(escapeCsv(item.getCategory())).append(',');
            sb.append(escapeCsv(item.getLocation())).append(',');
            sb.append(escapeCsv(reporter != null ? reporter.getFullName() : "")).append(',');
            sb.append(item.getFlagCount()).append(',');
            sb.append(item.getCreatedAt() != null ? item.getCreatedAt().format(DATE_FMT) : "").append('\n');
        }
        return sb.toString();
    }

    public String exportAnalyticsCsv(Map<String, Object> analyticsData) {
        StringBuilder sb = new StringBuilder();
        sb.append("Metric,Value\n");
        sb.append("Total Items,").append(analyticsData.getOrDefault("totalItems", 0)).append('\n');
        sb.append("Lost Count,").append(analyticsData.getOrDefault("lostCount", 0)).append('\n');
        sb.append("Found Count,").append(analyticsData.getOrDefault("foundCount", 0)).append('\n');
        sb.append("Resolved Count,").append(analyticsData.getOrDefault("resolvedCount", 0)).append('\n');
        sb.append("Recovery Rate (%),").append(analyticsData.getOrDefault("recoveryRate", 0)).append('\n');

        sb.append('\n');
        sb.append("Status,Count\n");
        Object statusObj = analyticsData.get("statusCounts");
        if (statusObj instanceof Map<?, ?> statusCounts) {
            statusCounts.forEach((k, v) -> sb.append(escapeCsv(k.toString())).append(',').append(v).append('\n'));
        }

        sb.append('\n');
        sb.append("Top Category,Count\n");
        Object catObj = analyticsData.get("topCategories");
        if (catObj instanceof List<?> cats) {
            for (Object c : cats) {
                if (c instanceof Map<?, ?> m) {
                    sb.append(escapeCsv(String.valueOf(m.get("category")))).append(',').append(m.get("count")).append('\n');
                }
            }
        }

        sb.append('\n');
        sb.append("Top Location,Count\n");
        Object locObj = analyticsData.get("topLocations");
        if (locObj instanceof List<?> locs) {
            for (Object l : locs) {
                if (l instanceof Map<?, ?> m) {
                    sb.append(escapeCsv(String.valueOf(m.get("location")))).append(',').append(m.get("count")).append('\n');
                }
            }
        }

        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        // Prevent CSV injection
        String safe = value;
        if (safe.startsWith("=") || safe.startsWith("+") || safe.startsWith("-") || safe.startsWith("@")) {
            safe = "'" + safe;
        }
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            safe = "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}
