package edu.cit.chan.unilost.features.claim;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;

    @PostMapping
    public ResponseEntity<ClaimDTO> submitClaim(
            @Valid @RequestBody ClaimRequest request,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        ClaimDTO created = claimService.submitClaim(request, email);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping("/my")
    public ResponseEntity<Page<ClaimDTO>> getMyClaims(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = (String) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(claimService.getMyClaims(email, pageable));
    }

    @GetMapping("/incoming")
    public ResponseEntity<Page<ClaimDTO>> getIncomingClaims(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = (String) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(claimService.getIncomingClaims(email, pageable));
    }

    @GetMapping("/item/{itemId}")
    public ResponseEntity<Page<ClaimDTO>> getClaimsForItem(
            @PathVariable String itemId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String email = (String) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(claimService.getClaimsForItem(itemId, email, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClaimDTO> getClaimById(
            @PathVariable String id,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(claimService.getClaimById(id, email));
    }

    @PutMapping("/{id}/accept")
    public ResponseEntity<ClaimDTO> acceptClaim(
            @PathVariable String id,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(claimService.acceptClaim(id, email));
    }

    @PutMapping("/{id}/reject")
    public ResponseEntity<ClaimDTO> rejectClaim(
            @PathVariable String id,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(claimService.rejectClaim(id, email));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<ClaimDTO> cancelClaim(
            @PathVariable String id,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(claimService.cancelClaim(id, email));
    }

    @PutMapping("/{id}/mark-returned")
    public ResponseEntity<ClaimDTO> markItemReturned(
            @PathVariable String id,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(claimService.markItemReturned(id, email));
    }

    @PutMapping("/{id}/confirm-received")
    public ResponseEntity<ClaimDTO> confirmItemReceived(
            @PathVariable String id,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(claimService.confirmItemReceived(id, email));
    }

    @PutMapping("/{id}/dispute-handover")
    public ResponseEntity<ClaimDTO> disputeHandover(
            @PathVariable String id,
            Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        return ResponseEntity.ok(claimService.disputeHandover(id, email));
    }
}
