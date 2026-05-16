package edu.cit.chan.unilost.features.admin;

import edu.cit.chan.unilost.features.claim.ClaimDTO;
import edu.cit.chan.unilost.features.claim.ClaimService;
import edu.cit.chan.unilost.features.item.ItemDTO;
import edu.cit.chan.unilost.features.user.UserDTO;
import edu.cit.chan.unilost.shared.util.Pagination;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ClaimService claimService;
    private final AuditLogService auditLogService;
    private final ExportService exportService;

    // ── Dashboard ──────────────────────────────────────────

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication auth) {
        return ResponseEntity.ok(adminService.getDashboardStats(auth.getName()));
    }

    // ── Items ──────────────────────────────────────────────

    @GetMapping("/items")
    public ResponseEntity<Page<ItemDTO>> getCampusItems(
            Authentication auth,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Pagination.clamp(size, Pagination.MESSAGES_MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getCampusItems(auth.getName(), keyword, type, status, pageable));
    }

    @GetMapping("/items/flagged")
    public ResponseEntity<Page<ItemDTO>> getFlaggedItems(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Pagination.clamp(size, Pagination.MESSAGES_MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "flagCount"));
        return ResponseEntity.ok(adminService.getFlaggedItems(auth.getName(), pageable));
    }

    @PutMapping("/items/{id}/status")
    public ResponseEntity<ItemDTO> updateItemStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String newStatus = body.get("status");
        String reason = body.get("reason");
        return ResponseEntity.ok(adminService.updateItemStatus(id, newStatus, reason, auth.getName()));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> forceDeleteItem(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body,
            Authentication auth) {
        String reason = body != null ? body.get("reason") : null;
        adminService.forceDeleteItem(id, reason, auth.getName());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/items/{id}/dismiss-flags")
    public ResponseEntity<ItemDTO> dismissItemFlags(
            @PathVariable String id,
            Authentication auth) {
        return ResponseEntity.ok(adminService.dismissItemFlags(id, auth.getName()));
    }

    @PutMapping("/items/{id}/appeal")
    public ResponseEntity<ItemDTO> reviewAppeal(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String decision = body.get("decision");
        String note = body.get("note");
        return ResponseEntity.ok(adminService.reviewAppeal(id, decision, note, auth.getName()));
    }

    // ── Users ──────────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<Page<UserDTO>> getCampusUsers(
            Authentication auth,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String accountStatus,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Pagination.clamp(size, Pagination.MESSAGES_MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getCampusUsers(auth.getName(), keyword, role, accountStatus, pageable));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserDTO> updateUserStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(adminService.updateUserStatus(id, newStatus, auth.getName()));
    }

    // ── Claims ─────────────────────────────────────────────

    @GetMapping("/claims")
    public ResponseEntity<Page<ClaimDTO>> getCampusClaims(
            Authentication auth,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, Pagination.clamp(size, Pagination.MESSAGES_MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getCampusClaims(auth.getName(), status, pageable));
    }

    @PutMapping("/claims/{id}/force-complete")
    public ResponseEntity<ClaimDTO> forceCompleteHandover(
            @PathVariable String id,
            Authentication auth) {
        ClaimDTO result = claimService.adminForceCompleteHandover(id, auth.getName());
        auditLogService.log("FORCE_COMPLETE_HANDOVER", "CLAIM", id, auth.getName(),
                "Force-completed handover for claim on item '" + result.getItemTitle() + "'",
                java.util.Map.of("claimId", id, "itemTitle", result.getItemTitle() != null ? result.getItemTitle() : ""));
        return ResponseEntity.ok(result);
    }

    // ── Analytics ──────────────────────────────────────────

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(Authentication auth) {
        return ResponseEntity.ok(adminService.getAnalytics(auth.getName()));
    }

    // ── Item Trends ──────────────────────────────────────

    @GetMapping("/item-trends")
    public ResponseEntity<List<Map<String, Object>>> getItemTrends(
            @RequestParam(defaultValue = "12") int months) {
        return ResponseEntity.ok(adminService.getItemTrends(months));
    }

    // ── Cross-Campus Stats ────────────────────────────────

    @GetMapping("/campus-stats")
    public ResponseEntity<?> getCrossCampusStats() {
        return ResponseEntity.ok(adminService.getCrossCampusStats());
    }

    // ── System Health ────────────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        return ResponseEntity.ok(adminService.getSystemHealth());
    }

    // ── Bulk Actions ─────────────────────────────────────────

    @PutMapping("/items/bulk-status")
    public ResponseEntity<List<ItemDTO>> bulkUpdateItemStatus(
            @RequestBody BulkActionRequest request,
            Authentication auth) {
        return ResponseEntity.ok(adminService.bulkUpdateItemStatus(
                request.getIds(), request.getStatus(), auth.getName()));
    }

    @DeleteMapping("/items/bulk-delete")
    public ResponseEntity<Map<String, Integer>> bulkDeleteItems(
            @RequestBody BulkActionRequest request,
            Authentication auth) {
        int deleted = adminService.bulkDeleteItems(request.getIds(), auth.getName());
        return ResponseEntity.ok(Map.of("deleted", deleted));
    }

    @PutMapping("/users/bulk-status")
    public ResponseEntity<List<UserDTO>> bulkUpdateUserStatus(
            @RequestBody BulkActionRequest request,
            Authentication auth) {
        return ResponseEntity.ok(adminService.bulkUpdateUserStatus(
                request.getIds(), request.getStatus(), auth.getName()));
    }

    // ── Export Data ──────────────────────────────────────────

    @GetMapping("/export/users")
    public ResponseEntity<byte[]> exportUsers(Authentication auth) {
        String csv = exportService.exportUsersCsv();
        auditLogService.log("EXPORT_DATA", "USER", null, auth.getName(), "Exported all users to CSV", null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unilost-users.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export/items")
    public ResponseEntity<byte[]> exportItems(Authentication auth) {
        String csv = exportService.exportItemsCsv();
        auditLogService.log("EXPORT_DATA", "ITEM", null, auth.getName(), "Exported all items to CSV", null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unilost-items.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    @GetMapping("/export/analytics")
    public ResponseEntity<byte[]> exportAnalytics(Authentication auth) {
        Map<String, Object> analyticsData = adminService.getAnalytics(auth.getName());
        String csv = exportService.exportAnalyticsCsv(analyticsData);
        auditLogService.log("EXPORT_DATA", "ITEM", null, auth.getName(), "Exported analytics to CSV", null);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=unilost-analytics.csv")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }

    // ── Audit Logs ──────────────────────────────────────────

    @GetMapping("/audit-logs")
    public ResponseEntity<Page<AuditLogDTO>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, Pagination.clamp(size, Pagination.MESSAGES_MAX_PAGE_SIZE), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(auditLogService.getAuditLogs(action, targetType, pageable));
    }
}
