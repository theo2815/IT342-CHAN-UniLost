package edu.cit.chan.unilost.controller;

import edu.cit.chan.unilost.dto.ClaimDTO;
import edu.cit.chan.unilost.dto.ItemDTO;
import edu.cit.chan.unilost.dto.UserDTO;
import edu.cit.chan.unilost.service.AdminService;
import edu.cit.chan.unilost.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN', 'FACULTY')")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final ClaimService claimService;

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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getCampusItems(auth.getName(), keyword, type, status, pageable));
    }

    @GetMapping("/items/flagged")
    public ResponseEntity<Page<ItemDTO>> getFlaggedItems(
            Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "flagCount"));
        return ResponseEntity.ok(adminService.getFlaggedItems(auth.getName(), pageable));
    }

    @PutMapping("/items/{id}/status")
    public ResponseEntity<ItemDTO> updateItemStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> body,
            Authentication auth) {
        String newStatus = body.get("status");
        return ResponseEntity.ok(adminService.updateItemStatus(id, newStatus, auth.getName()));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> forceDeleteItem(
            @PathVariable String id,
            Authentication auth) {
        adminService.forceDeleteItem(id, auth.getName());
        return ResponseEntity.noContent().build();
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
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
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(adminService.getCampusClaims(auth.getName(), status, pageable));
    }

    @PutMapping("/claims/{id}/force-complete")
    public ResponseEntity<ClaimDTO> forceCompleteHandover(
            @PathVariable String id,
            Authentication auth) {
        return ResponseEntity.ok(claimService.adminForceCompleteHandover(id, auth.getName()));
    }

    // ── Analytics ──────────────────────────────────────────

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics(Authentication auth) {
        return ResponseEntity.ok(adminService.getAnalytics(auth.getName()));
    }

    // ── Cross-Campus Stats (Faculty only) ────────────────

    @GetMapping("/campus-stats")
    @PreAuthorize("hasRole('FACULTY')")
    public ResponseEntity<?> getCrossCampusStats() {
        return ResponseEntity.ok(adminService.getCrossCampusStats());
    }
}
