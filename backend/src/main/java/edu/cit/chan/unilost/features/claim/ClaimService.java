package edu.cit.chan.unilost.features.claim;

import edu.cit.chan.unilost.entity.*;
import edu.cit.chan.unilost.exception.ForbiddenException;
import edu.cit.chan.unilost.exception.ResourceNotFoundException;
import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.campus.CampusRepository;
import edu.cit.chan.unilost.features.item.ItemEntity;
import edu.cit.chan.unilost.features.item.ItemRepository;
import edu.cit.chan.unilost.features.item.ItemStatus;
import edu.cit.chan.unilost.features.item.ItemType;
import edu.cit.chan.unilost.features.user.Role;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.features.user.UserService;
import edu.cit.chan.unilost.repository.ChatRepository;
import edu.cit.chan.unilost.service.ChatService;
import edu.cit.chan.unilost.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.dao.OptimisticLockingFailureException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final CampusRepository campusRepository;
    private final ChatRepository chatRepository;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final UserService userService;
    private final MongoTemplate mongoTemplate;

    public ClaimDTO submitClaim(ClaimRequest request, String claimantEmail) {
        UserEntity claimant = userRepository.findByEmail(claimantEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(request.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        if (item.getStatus() != ItemStatus.ACTIVE) {
            throw new IllegalArgumentException("This item is no longer accepting claims");
        }

        if (claimant.getId().equals(item.getReporterId())) {
            throw new IllegalArgumentException("You cannot claim your own item");
        }

        // FOUND items require a secret detail answer
        if ("FOUND".equals(item.getType())
                && (request.getProvidedAnswer() == null || request.getProvidedAnswer().isBlank())) {
            throw new IllegalArgumentException("Secret detail answer is required for found items");
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

        // Auto-create a chat room between finder and claimant and post claim details as first message
        ChatEntity chat = chatService.createChatForClaim(item.getId(), saved.getId(), item.getReporterId(), claimant.getId());
        chatService.sendClaimSubmissionMessage(chat.getId(), saved, item, claimant);

        // Notify the item poster about the new claim
        notificationService.notifyClaimReceived(item.getReporterId(), claimant.getFullName(), item.getTitle(), saved.getId());

        // LOST items: auto-accept the claim so the chat goes straight to handover mode
        if ("LOST".equals(item.getType())) {
            // Atomically transition item from ACTIVE → CLAIMED to prevent race conditions
            ItemEntity updatedItem = mongoTemplate.findAndModify(
                    Query.query(Criteria.where("id").is(item.getId())
                            .and("status").is(ItemStatus.ACTIVE)
                            .and("isDeleted").is(false)),
                    new Update()
                            .set("status", ItemStatus.CLAIMED)
                            .set("updatedAt", LocalDateTime.now()),
                    FindAndModifyOptions.options().returnNew(true),
                    ItemEntity.class
            );

            if (updatedItem == null) {
                // Another thread already claimed this item — reject this claim
                saved.setStatus(ClaimStatus.REJECTED);
                saved.setUpdatedAt(LocalDateTime.now());
                claimRepository.save(saved);
                throw new IllegalArgumentException("This item has already been claimed by another user");
            }

            saved.setStatus(ClaimStatus.ACCEPTED);
            saved.setUpdatedAt(LocalDateTime.now());
            saved = claimRepository.save(saved);

            // Auto-reject all other PENDING claims on the same item
            List<ClaimEntity> otherPending = claimRepository.findByItemIdAndStatus(item.getId(), ClaimStatus.PENDING);
            for (ClaimEntity other : otherPending) {
                if (!other.getId().equals(saved.getId())) {
                    other.setStatus(ClaimStatus.REJECTED);
                    other.setUpdatedAt(LocalDateTime.now());
                    notificationService.notifyClaimAutoRejected(other.getClaimantId(), item.getTitle(), other.getId());
                }
            }
            if (!otherPending.isEmpty()) {
                claimRepository.saveAll(otherPending);
            }

            // Post auto-accepted system message in chat
            chatService.sendStructuredMessage(chat.getId(), null,
                    "Claim auto-accepted! You can now chat and arrange the handover.",
                    MessageType.CLAIM_ACCEPTED,
                    Map.of("autoAccepted", true,
                           "itemTitle", item.getTitle(),
                           "timestamp", LocalDateTime.now().toString()));

            notificationService.notifyClaimAccepted(claimant.getId(), item.getTitle(), saved.getId());
        }

        return convertToDTO(saved, item, claimant, null, null, false);
    }

    public ClaimDTO getClaimById(String claimId, String currentEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isClaimant = currentUser.getId().equals(claim.getClaimantId());
        boolean isFinder = currentUser.getId().equals(claim.getFinderId());
        boolean isAdmin = currentUser.getRole() == Role.ADMIN;

        if (!isClaimant && !isFinder && !isAdmin) {
            throw new ForbiddenException("You do not have permission to view this claim");
        }

        // Pre-load entities to avoid N+1 fallback queries in convertToDTO
        ItemEntity item = claim.getItemId() != null
                ? itemRepository.findByIdAndIsDeletedFalse(claim.getItemId()).orElse(null) : null;
        UserEntity claimant = claim.getClaimantId() != null
                ? userRepository.findById(claim.getClaimantId()).orElse(null) : null;
        UserEntity finder = claim.getFinderId() != null
                ? userRepository.findById(claim.getFinderId()).orElse(null) : null;

        return convertToDTO(claim, item, claimant, finder, null, isFinder || isAdmin);
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
        boolean isAdmin = caller.getRole() == Role.ADMIN;
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

        // Fetch item once and reuse throughout the method
        ItemEntity claimItem = itemRepository.findByIdAndIsDeletedFalse(claim.getItemId()).orElse(null);

        // Block accept for LOST items (they are auto-accepted on submission)
        if (claimItem != null && "LOST".equals(claimItem.getType())) {
            throw new IllegalArgumentException("Lost item claims are auto-accepted and cannot be manually accepted");
        }

        UserEntity finder = userRepository.findByEmail(finderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verifyFinderOrAdmin(claim, finder);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Only pending claims can be accepted");
        }

        claim.setStatus(ClaimStatus.ACCEPTED);
        claim.setUpdatedAt(LocalDateTime.now());

        ClaimEntity saved;
        try {
            saved = claimRepository.save(claim);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalArgumentException("This claim was modified by another request. Please refresh and try again.");
        }

        String itemTitle = claimItem != null ? claimItem.getTitle() : "an item";

        // Notify the accepted claimant
        notificationService.notifyClaimAccepted(claim.getClaimantId(), itemTitle, saved.getId());

        // Auto-reject all other PENDING claims on the same item
        List<ClaimEntity> otherPending = claimRepository.findByItemIdAndStatus(claim.getItemId(), ClaimStatus.PENDING);
        for (ClaimEntity other : otherPending) {
            other.setStatus(ClaimStatus.REJECTED);
            other.setUpdatedAt(LocalDateTime.now());
            // Notify each auto-rejected claimant
            notificationService.notifyClaimAutoRejected(other.getClaimantId(), itemTitle, other.getId());
        }
        if (!otherPending.isEmpty()) {
            claimRepository.saveAll(otherPending);
        }

        // Atomically transition item from ACTIVE → CLAIMED to prevent concurrent accepts
        if (claimItem != null) {
            ItemEntity updatedItem = mongoTemplate.findAndModify(
                    Query.query(Criteria.where("id").is(claimItem.getId())
                            .and("status").is(ItemStatus.ACTIVE)
                            .and("isDeleted").is(false)),
                    new Update()
                            .set("status", ItemStatus.CLAIMED)
                            .set("updatedAt", LocalDateTime.now()),
                    FindAndModifyOptions.options().returnNew(true),
                    ItemEntity.class
            );
            if (updatedItem == null) {
                throw new IllegalArgumentException("This item has already been claimed by another user. Please refresh and try again.");
            }
        }

        // Post CLAIM_ACCEPTED system message in chat (best-effort, don't rollback transaction on failure)
        try {
            String chatId = chatRepository.findByClaimId(claimId)
                    .map(ChatEntity::getId).orElse(null);
            if (chatId != null) {
                chatService.sendStructuredMessage(chatId, null,
                        "Claim accepted! " + finder.getFullName() + " confirmed that your answer is correct.",
                        MessageType.CLAIM_ACCEPTED,
                        Map.of("finderName", finder.getFullName(),
                               "itemTitle", itemTitle,
                               "timestamp", LocalDateTime.now().toString()));
            }
        } catch (Exception e) {
            // Log but don't fail the claim acceptance
        }

        return convertToDTO(saved, null, null, finder, null, true);
    }

    public ClaimDTO rejectClaim(String claimId, String finderEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        // Block reject for LOST items (they are auto-accepted on submission)
        ItemEntity claimItem = itemRepository.findByIdAndIsDeletedFalse(claim.getItemId()).orElse(null);
        if (claimItem != null && "LOST".equals(claimItem.getType())) {
            throw new IllegalArgumentException("Lost item claims are auto-accepted and cannot be manually rejected");
        }

        UserEntity finder = userRepository.findByEmail(finderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verifyFinderOrAdmin(claim, finder);

        if (claim.getStatus() != ClaimStatus.PENDING) {
            throw new IllegalArgumentException("Only pending claims can be rejected");
        }

        claim.setStatus(ClaimStatus.REJECTED);
        claim.setUpdatedAt(LocalDateTime.now());
        ClaimEntity saved = claimRepository.save(claim);

        // Notify rejected claimant (reuse already-fetched item)
        String rejItemTitle = claimItem != null ? claimItem.getTitle() : "an item";
        notificationService.notifyClaimRejected(claim.getClaimantId(), rejItemTitle, saved.getId());

        // Post CLAIM_REJECTED system message in chat (best-effort)
        try {
            String chatId = chatRepository.findByClaimId(claimId)
                    .map(ChatEntity::getId).orElse(null);
            if (chatId != null) {
                chatService.sendStructuredMessage(chatId, null,
                        "Claim rejected. The finder determined the answer was incorrect.",
                        MessageType.CLAIM_REJECTED,
                        Map.of("finderName", finder.getFullName(),
                               "itemTitle", rejItemTitle,
                               "timestamp", LocalDateTime.now().toString()));
            }
        } catch (Exception e) {
            // Log but don't fail the claim rejection
        }

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

    // ── Chat-Driven Handover Flow ─────────────────────────

    /**
     * Finder marks that they have returned the item to the owner.
     * Item status: CLAIMED → PENDING_OWNER_CONFIRMATION
     */
    @Transactional
    public ClaimDTO markItemReturned(String claimId, String callerEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Determine who is the actual physical holder of the item
        // FOUND items: the finder (poster) has the item → finderId
        // LOST items: the claimant found the item → claimantId
        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(claim.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        String actualHolderId = "LOST".equals(item.getType()) ? claim.getClaimantId() : claim.getFinderId();
        if (!caller.getId().equals(actualHolderId) && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only the person holding the item can mark as returned");
        }

        if (claim.getStatus() != ClaimStatus.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted claims can proceed to handover");
        }

        if (item.getStatus() != ItemStatus.CLAIMED) {
            throw new IllegalArgumentException("Item must be in CLAIMED state to mark as returned");
        }

        if (claim.getFinderMarkedReturnedAt() != null) {
            throw new IllegalArgumentException("This item has already been marked as returned");
        }

        claim.setFinderMarkedReturnedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());

        item.setStatus(ItemStatus.PENDING_OWNER_CONFIRMATION);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);

        ClaimEntity saved = saveWithOptimisticLock(claim);

        // Post HANDOVER_REQUEST system message in chat
        String chatId = chatRepository.findByClaimId(claimId)
                .map(ChatEntity::getId).orElse(null);
        if (chatId != null) {
            chatService.sendStructuredMessage(chatId, null,
                    caller.getFullName() + " marked this item as returned to the owner.",
                    MessageType.HANDOVER_REQUEST,
                    Map.of("finderName", caller.getFullName(),
                           "timestamp", LocalDateTime.now().toString()));

            // Notify the actual owner (FOUND: claimantId, LOST: finderId)
            String actualOwnerId = "LOST".equals(item.getType()) ? claim.getFinderId() : claim.getClaimantId();
            notificationService.notifyItemMarkedReturned(
                    actualOwnerId, caller.getFullName(), item.getTitle(), chatId);
        }

        return convertToDTO(saved, item, null, caller, null, true);
    }

    /**
     * Owner/claimant confirms they have received the item.
     * Item status: PENDING_OWNER_CONFIRMATION → RETURNED
     * Claim status: ACCEPTED → COMPLETED
     * Awards karma.
     */
    @Transactional
    public ClaimDTO confirmItemReceived(String claimId, String callerEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Determine who is the actual owner
        // FOUND items: the claimant is the owner
        // LOST items: the finderId (poster) is the owner
        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(claim.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        String actualOwnerId = "LOST".equals(item.getType()) ? claim.getFinderId() : claim.getClaimantId();
        if (!caller.getId().equals(actualOwnerId) && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only the item owner can confirm receipt");
        }

        if (claim.getStatus() != ClaimStatus.ACCEPTED) {
            throw new IllegalArgumentException("Claim must be in accepted state");
        }

        if (claim.getOwnerConfirmedReceivedAt() != null) {
            throw new IllegalArgumentException("Receipt has already been confirmed");
        }

        if (item.getStatus() != ItemStatus.PENDING_OWNER_CONFIRMATION) {
            throw new IllegalArgumentException("The finder must mark the item as returned first");
        }

        // Update claim
        claim.setOwnerConfirmedReceivedAt(LocalDateTime.now());
        claim.setStatus(ClaimStatus.COMPLETED);
        claim.setUpdatedAt(LocalDateTime.now());

        // Update item
        item.setStatus(ItemStatus.RETURNED);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);

        ClaimEntity saved = saveWithOptimisticLock(claim);

        // Award karma: actual finder +10, actual owner +5
        // FOUND items: finderId=actual finder, claimantId=actual owner
        // LOST items: claimantId=actual finder, finderId=actual owner
        String actualFinderId = "LOST".equals(item.getType()) ? claim.getClaimantId() : claim.getFinderId();
        int finderKarma = 10;
        int ownerKarma = 5;
        userService.incrementKarma(actualFinderId, finderKarma);
        userService.incrementKarma(actualOwnerId, ownerKarma);

        // Post HANDOVER_CONFIRMED system message in chat
        String chatId = chatRepository.findByClaimId(claimId)
                .map(ChatEntity::getId).orElse(null);
        if (chatId != null) {
            chatService.sendStructuredMessage(chatId, null,
                    "Item successfully returned! " + caller.getFullName() + " confirmed receipt.",
                    MessageType.HANDOVER_CONFIRMED,
                    Map.of("ownerName", caller.getFullName(),
                           "finderKarma", finderKarma,
                           "ownerKarma", ownerKarma,
                           "timestamp", LocalDateTime.now().toString()));

            notificationService.notifyItemReturned(actualFinderId, item.getTitle(), finderKarma, chatId);
            notificationService.notifyItemReturned(actualOwnerId, item.getTitle(), ownerKarma, chatId);
        }

        return convertToDTO(saved, item, caller, null, null, false);
    }

    /**
     * Owner disputes the handover — they did not actually receive the item.
     * Reverts: PENDING_OWNER_CONFIRMATION → CLAIMED, clears finderMarkedReturnedAt.
     */
    @Transactional
    public ClaimDTO disputeHandover(String claimId, String callerEmail) {
        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        UserEntity caller = userRepository.findByEmail(callerEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(claim.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // Only the actual owner can dispute
        String actualOwnerId = "LOST".equals(item.getType()) ? claim.getFinderId() : claim.getClaimantId();
        if (!caller.getId().equals(actualOwnerId) && caller.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only the item owner can dispute the handover");
        }

        if (claim.getStatus() != ClaimStatus.ACCEPTED) {
            throw new IllegalArgumentException("Claim must be in accepted state");
        }

        if (item.getStatus() != ItemStatus.PENDING_OWNER_CONFIRMATION) {
            throw new IllegalArgumentException("Item must be pending owner confirmation to dispute");
        }

        // Revert handover
        claim.setFinderMarkedReturnedAt(null);
        claim.setUpdatedAt(LocalDateTime.now());

        item.setStatus(ItemStatus.CLAIMED);
        item.setUpdatedAt(LocalDateTime.now());
        itemRepository.save(item);

        ClaimEntity saved = saveWithOptimisticLock(claim);

        // Post HANDOVER_DISPUTED system message in chat
        String chatId = chatRepository.findByClaimId(claimId)
                .map(ChatEntity::getId).orElse(null);
        if (chatId != null) {
            chatService.sendStructuredMessage(chatId, null,
                    caller.getFullName() + " reported they did not receive the item. Handover reverted.",
                    MessageType.HANDOVER_DISPUTED,
                    Map.of("ownerName", caller.getFullName(),
                           "timestamp", LocalDateTime.now().toString()));

            // Notify the actual holder that the owner disputed
            String actualHolderId = "LOST".equals(item.getType()) ? claim.getClaimantId() : claim.getFinderId();
            notificationService.notifyHandoverDisputed(
                    actualHolderId, caller.getFullName(), item.getTitle(), chatId);
        }

        return convertToDTO(saved, item, caller, null, null, false);
    }

    /**
     * Admin force-completes a handover, skipping the dual-confirmation flow.
     * Sets both timestamps, updates item to RETURNED, claim to COMPLETED, awards karma.
     */
    @Transactional
    public ClaimDTO adminForceCompleteHandover(String claimId, String adminEmail) {
        UserEntity admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (admin.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Only admins can force-complete handovers");
        }

        ClaimEntity claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found"));

        if (claim.getStatus() != ClaimStatus.ACCEPTED) {
            throw new IllegalArgumentException("Only accepted claims can be force-completed");
        }

        ItemEntity item = itemRepository.findByIdAndIsDeletedFalse(claim.getItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        LocalDateTime now = LocalDateTime.now();

        // Set both handover timestamps
        if (claim.getFinderMarkedReturnedAt() == null) {
            claim.setFinderMarkedReturnedAt(now);
        }
        if (claim.getOwnerConfirmedReceivedAt() == null) {
            claim.setOwnerConfirmedReceivedAt(now);
        }
        claim.setStatus(ClaimStatus.COMPLETED);
        claim.setUpdatedAt(now);

        item.setStatus(ItemStatus.RETURNED);
        item.setUpdatedAt(now);
        itemRepository.save(item);

        ClaimEntity saved = saveWithOptimisticLock(claim);

        // Award karma (account for LOST item role inversion)
        int finderKarma = 10;
        int ownerKarma = 5;
        String actualFinderId = "LOST".equals(item.getType()) ? claim.getClaimantId() : claim.getFinderId();
        String actualOwnerId = "LOST".equals(item.getType()) ? claim.getFinderId() : claim.getClaimantId();
        userService.incrementKarma(actualFinderId, finderKarma);
        userService.incrementKarma(actualOwnerId, ownerKarma);

        // Notify both parties about the admin override
        String itemTitle = item.getTitle();
        notificationService.notifyItemReturned(claim.getFinderId(), itemTitle, finderKarma, null);
        notificationService.notifyItemReturned(claim.getClaimantId(), itemTitle, ownerKarma, null);

        // Post system message in chat (best-effort)
        try {
            String chatId = chatRepository.findByClaimId(claimId)
                    .map(ChatEntity::getId).orElse(null);
            if (chatId != null) {
                chatService.sendStructuredMessage(chatId, null,
                        "An admin has force-completed this handover.",
                        MessageType.HANDOVER_CONFIRMED,
                        Map.of("adminForceComplete", true,
                               "adminEmail", adminEmail,
                               "finderKarma", finderKarma,
                               "ownerKarma", ownerKarma,
                               "timestamp", now.toString()));
            }
        } catch (Exception e) {
            // Log but don't fail the force-complete
        }

        return convertToDTO(saved, item, null, null, null, false);
    }

    private ClaimEntity saveWithOptimisticLock(ClaimEntity claim) {
        try {
            return claimRepository.save(claim);
        } catch (OptimisticLockingFailureException e) {
            throw new IllegalArgumentException(
                    "This claim was modified by another request. Please refresh and try again.");
        }
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
                .filter(item -> !item.isDeleted())
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

        // Resolve associated chat ID
        chatRepository.findByClaimId(claim.getId())
                .ifPresent(chat -> dto.setChatId(chat.getId()));

        // Handover fields
        dto.setFinderMarkedReturnedAt(claim.getFinderMarkedReturnedAt());
        dto.setOwnerConfirmedReceivedAt(claim.getOwnerConfirmedReceivedAt());

        return dto;
    }

    private void verifyFinderOrAdmin(ClaimEntity claim, UserEntity user) {
        boolean isFinder = user.getId().equals(claim.getFinderId());
        boolean isAdmin = user.getRole() == Role.ADMIN;
        if (!isFinder && !isAdmin) {
            throw new ForbiddenException("Only the item poster can perform this action");
        }
    }
}
