package edu.cit.chan.unilost.features.claim;

import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.campus.CampusRepository;
import edu.cit.chan.unilost.features.chat.ChatEntity;
import edu.cit.chan.unilost.features.chat.ChatRepository;
import edu.cit.chan.unilost.features.chat.ChatService;
import edu.cit.chan.unilost.features.chat.MessageType;
import edu.cit.chan.unilost.features.item.ItemEntity;
import edu.cit.chan.unilost.features.item.ItemRepository;
import edu.cit.chan.unilost.features.item.ItemStatus;
import edu.cit.chan.unilost.features.notification.NotificationService;
import edu.cit.chan.unilost.features.user.AccountStatus;
import edu.cit.chan.unilost.features.user.Role;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.features.user.UserService;
import edu.cit.chan.unilost.shared.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ClaimService.
 *
 * Covers the claim & handover state machine: submit (FOUND vs LOST auto-accept),
 * accept, reject, cancel, markItemReturned, confirmItemReceived (karma), disputeHandover.
 *
 * Pure JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock private ClaimRepository claimRepository;
    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private CampusRepository campusRepository;
    @Mock private ChatRepository chatRepository;
    @Mock private ChatService chatService;
    @Mock private NotificationService notificationService;
    @Mock private UserService userService;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks private ClaimService claimService;

    private UserEntity finder;
    private UserEntity claimant;
    private UserEntity admin;
    private ItemEntity foundItem;
    private ItemEntity lostItem;
    private ChatEntity chat;

    @BeforeEach
    void setUp() {
        finder = user("user-finder", "finder@usc.edu.ph", Role.STUDENT);
        claimant = user("user-claimant", "claimant@usc.edu.ph", Role.STUDENT);
        admin = user("user-admin", "admin@usc.edu.ph", Role.ADMIN);

        foundItem = new ItemEntity();
        foundItem.setId("item-found");
        foundItem.setReporterId(finder.getId());
        foundItem.setTitle("Black Wallet");
        foundItem.setType("FOUND");
        foundItem.setStatus(ItemStatus.ACTIVE);
        foundItem.setSecretDetailQuestion("What is the brand?");
        foundItem.setCreatedAt(LocalDateTime.now());

        lostItem = new ItemEntity();
        lostItem.setId("item-lost");
        lostItem.setReporterId(finder.getId());
        lostItem.setTitle("Lost Phone");
        lostItem.setType("LOST");
        lostItem.setStatus(ItemStatus.ACTIVE);
        lostItem.setCreatedAt(LocalDateTime.now());

        chat = new ChatEntity();
        chat.setId("chat-1");
    }

    private static UserEntity user(String id, String email, Role role) {
        UserEntity u = new UserEntity();
        u.setId(id);
        u.setEmail(email);
        u.setFullName(id);
        u.setRole(role);
        u.setAccountStatus(AccountStatus.ACTIVE);
        return u;
    }

    // ── submitClaim ─────────────────────────────────────────

    @Test
    @DisplayName("submitClaim on FOUND item creates a PENDING claim and notifies poster")
    void submitClaim_foundItem_createsPending() {
        ClaimRequest req = new ClaimRequest("item-found", "Levi's", "I lost it yesterday");

        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));
        when(claimRepository.findByItemIdAndClaimantIdAndStatusIn(eq("item-found"), eq("user-claimant"), any()))
                .thenReturn(Optional.empty());
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> {
            ClaimEntity c = inv.getArgument(0);
            c.setId("claim-1");
            return c;
        });
        when(chatService.createChatForClaim(eq("item-found"), anyString(), eq("user-finder"), eq("user-claimant")))
                .thenReturn(chat);

        ClaimDTO dto = claimService.submitClaim(req, "claimant@usc.edu.ph");

        assertThat(dto.getStatus()).isEqualTo("PENDING");
        verify(notificationService).notifyClaimReceived(eq("user-finder"), anyString(), eq("Black Wallet"), anyString());
        verify(chatService).sendClaimSubmissionMessage(eq("chat-1"), any(ClaimEntity.class), eq(foundItem), eq(claimant));
        // Item stays ACTIVE for FOUND-flow (no auto-claim)
        verify(mongoTemplate, never()).findAndModify(any(), any(), any(), eq(ItemEntity.class));
    }

    @Test
    @DisplayName("submitClaim on LOST item auto-accepts and atomically transitions item to CLAIMED")
    void submitClaim_lostItem_autoAccepts() {
        ClaimRequest req = new ClaimRequest("item-lost", null, "I think I found it");

        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse("item-lost")).thenReturn(Optional.of(lostItem));
        when(claimRepository.findByItemIdAndClaimantIdAndStatusIn(eq("item-lost"), eq("user-claimant"), any()))
                .thenReturn(Optional.empty());
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> {
            ClaimEntity c = inv.getArgument(0);
            if (c.getId() == null) c.setId("claim-1");
            return c;
        });
        when(chatService.createChatForClaim(eq("item-lost"), anyString(), eq("user-finder"), eq("user-claimant")))
                .thenReturn(chat);

        ItemEntity transitioned = new ItemEntity();
        transitioned.setId("item-lost");
        transitioned.setStatus(ItemStatus.CLAIMED);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(ItemEntity.class))).thenReturn(transitioned);

        when(claimRepository.findByItemIdAndStatus("item-lost", ClaimStatus.PENDING)).thenReturn(List.of());

        ClaimDTO dto = claimService.submitClaim(req, "claimant@usc.edu.ph");

        assertThat(dto.getStatus()).isEqualTo("ACCEPTED");
        verify(mongoTemplate).findAndModify(any(), any(), any(), eq(ItemEntity.class));
        verify(notificationService).notifyClaimAccepted(eq("user-claimant"), eq("Lost Phone"), anyString());
        verify(chatService).sendStructuredMessage(eq("chat-1"), eq(null), anyString(),
                eq(MessageType.CLAIM_ACCEPTED), any());
    }

    @Test
    @DisplayName("submitClaim rejects FOUND item without an answer")
    void submitClaim_foundWithoutAnswer_rejected() {
        ClaimRequest req = new ClaimRequest("item-found", null, "no answer here");
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.submitClaim(req, "claimant@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Secret detail answer is required");
    }

    @Test
    @DisplayName("submitClaim rejects when claimant is the item poster")
    void submitClaim_selfClaim_rejected() {
        ClaimRequest req = new ClaimRequest("item-found", "x", "self");
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.submitClaim(req, "finder@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot claim your own");
    }

    @Test
    @DisplayName("submitClaim rejects when item is no longer ACTIVE")
    void submitClaim_inactiveItem_rejected() {
        foundItem.setStatus(ItemStatus.CLAIMED);
        ClaimRequest req = new ClaimRequest("item-found", "Levi's", "msg");
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.submitClaim(req, "claimant@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no longer accepting claims");
    }

    @Test
    @DisplayName("submitClaim rejects a duplicate pending claim from the same user")
    void submitClaim_duplicate_rejected() {
        ClaimRequest req = new ClaimRequest("item-found", "Levi's", "msg");
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));
        when(claimRepository.findByItemIdAndClaimantIdAndStatusIn(eq("item-found"), eq("user-claimant"), any()))
                .thenReturn(Optional.of(new ClaimEntity()));

        assertThatThrownBy(() -> claimService.submitClaim(req, "claimant@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already have a pending claim");
    }

    // ── acceptClaim ─────────────────────────────────────────

    @Test
    @DisplayName("acceptClaim transitions a PENDING claim on a FOUND item to ACCEPTED")
    void acceptClaim_pendingToAccepted() {
        ClaimEntity claim = pendingClaim();
        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.findByItemIdAndStatus("item-found", ClaimStatus.PENDING)).thenReturn(List.of());

        ItemEntity transitioned = new ItemEntity();
        transitioned.setStatus(ItemStatus.CLAIMED);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(ItemEntity.class))).thenReturn(transitioned);

        when(chatRepository.findByClaimId("claim-1")).thenReturn(Optional.of(chat));

        ClaimDTO dto = claimService.acceptClaim("claim-1", "finder@usc.edu.ph");

        assertThat(dto.getStatus()).isEqualTo("ACCEPTED");
        verify(notificationService).notifyClaimAccepted(eq("user-claimant"), eq("Black Wallet"), eq("claim-1"));
        verify(mongoTemplate).findAndModify(any(), any(), any(), eq(ItemEntity.class));
    }

    @Test
    @DisplayName("acceptClaim auto-rejects all other pending claims on the same item")
    void acceptClaim_autoRejectsOthers() {
        ClaimEntity accepted = pendingClaim();
        ClaimEntity loser = pendingClaim();
        loser.setId("claim-loser");
        loser.setClaimantId("user-other");

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(accepted));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.findByItemIdAndStatus("item-found", ClaimStatus.PENDING))
                .thenReturn(List.of(loser));

        ItemEntity transitioned = new ItemEntity();
        transitioned.setStatus(ItemStatus.CLAIMED);
        when(mongoTemplate.findAndModify(any(), any(), any(), eq(ItemEntity.class))).thenReturn(transitioned);

        claimService.acceptClaim("claim-1", "finder@usc.edu.ph");

        assertThat(loser.getStatus()).isEqualTo(ClaimStatus.REJECTED);
        verify(notificationService).notifyClaimAutoRejected(eq("user-other"), eq("Black Wallet"), eq("claim-loser"));
        verify(claimRepository).saveAll(List.of(loser));
    }

    @Test
    @DisplayName("acceptClaim refuses to act on LOST claims (auto-accepted on submit)")
    void acceptClaim_lostItem_blocked() {
        ClaimEntity claim = pendingClaim();
        claim.setItemId("item-lost");
        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(itemRepository.findByIdAndIsDeletedFalse("item-lost")).thenReturn(Optional.of(lostItem));

        assertThatThrownBy(() -> claimService.acceptClaim("claim-1", "finder@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("auto-accepted");
    }

    @Test
    @DisplayName("acceptClaim is forbidden for non-finder non-admin")
    void acceptClaim_nonFinder_forbidden() {
        ClaimEntity claim = pendingClaim();
        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));

        assertThatThrownBy(() -> claimService.acceptClaim("claim-1", "claimant@usc.edu.ph"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    @DisplayName("acceptClaim rejects claims that are not in PENDING")
    void acceptClaim_nonPending_rejected() {
        ClaimEntity claim = pendingClaim();
        claim.setStatus(ClaimStatus.REJECTED);
        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));

        assertThatThrownBy(() -> claimService.acceptClaim("claim-1", "finder@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only pending");
    }

    // ── rejectClaim ─────────────────────────────────────────

    @Test
    @DisplayName("rejectClaim transitions PENDING -> REJECTED and notifies claimant")
    void rejectClaim_success() {
        ClaimEntity claim = pendingClaim();
        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(itemRepository.findByIdAndIsDeletedFalse("item-found")).thenReturn(Optional.of(foundItem));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClaimDTO dto = claimService.rejectClaim("claim-1", "finder@usc.edu.ph");

        assertThat(dto.getStatus()).isEqualTo("REJECTED");
        verify(notificationService).notifyClaimRejected(eq("user-claimant"), eq("Black Wallet"), eq("claim-1"));
    }

    // ── cancelClaim ─────────────────────────────────────────

    @Test
    @DisplayName("cancelClaim allows the claimant to cancel a PENDING claim")
    void cancelClaim_success() {
        ClaimEntity claim = pendingClaim();
        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ClaimDTO dto = claimService.cancelClaim("claim-1", "claimant@usc.edu.ph");

        assertThat(dto.getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    @DisplayName("cancelClaim is forbidden for the finder (only claimant can cancel)")
    void cancelClaim_byFinder_forbidden() {
        ClaimEntity claim = pendingClaim();
        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));

        assertThatThrownBy(() -> claimService.cancelClaim("claim-1", "finder@usc.edu.ph"))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── markItemReturned (FOUND) ────────────────────────────

    @Test
    @DisplayName("markItemReturned (FOUND): finder transitions item CLAIMED -> PENDING_OWNER_CONFIRMATION")
    void markItemReturned_foundFlow() {
        foundItem.setStatus(ItemStatus.CLAIMED);
        ClaimEntity claim = acceptedClaim(foundItem.getId());

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(itemRepository.findByIdAndIsDeletedFalse(foundItem.getId())).thenReturn(Optional.of(foundItem));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chatRepository.findByClaimId("claim-1")).thenReturn(Optional.of(chat));

        ClaimDTO dto = claimService.markItemReturned("claim-1", "finder@usc.edu.ph");

        assertThat(foundItem.getStatus()).isEqualTo(ItemStatus.PENDING_OWNER_CONFIRMATION);
        assertThat(claim.getFinderMarkedReturnedAt()).isNotNull();
        assertThat(dto.getFinderMarkedReturnedAt()).isNotNull();
        verify(notificationService).notifyItemMarkedReturned(
                eq("user-claimant"), anyString(), eq("Black Wallet"), eq("chat-1"));
    }

    @Test
    @DisplayName("markItemReturned (LOST): only the claimant (actual holder) can mark — finder is forbidden")
    void markItemReturned_lostFlow_forbidsFinder() {
        lostItem.setStatus(ItemStatus.CLAIMED);
        ClaimEntity claim = acceptedClaim(lostItem.getId());

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(itemRepository.findByIdAndIsDeletedFalse(lostItem.getId())).thenReturn(Optional.of(lostItem));

        assertThatThrownBy(() -> claimService.markItemReturned("claim-1", "finder@usc.edu.ph"))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("Only the person holding");
    }

    @Test
    @DisplayName("markItemReturned rejects double-mark")
    void markItemReturned_doubleMark_rejected() {
        foundItem.setStatus(ItemStatus.CLAIMED);
        ClaimEntity claim = acceptedClaim(foundItem.getId());
        claim.setFinderMarkedReturnedAt(LocalDateTime.now().minusMinutes(5));

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(itemRepository.findByIdAndIsDeletedFalse(foundItem.getId())).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.markItemReturned("claim-1", "finder@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been marked");
    }

    // ── confirmItemReceived (karma) ─────────────────────────

    @Test
    @DisplayName("confirmItemReceived (FOUND): claimant confirms receipt, awards finder +10 / owner +5")
    void confirmItemReceived_foundFlow_awardsKarma() {
        foundItem.setStatus(ItemStatus.PENDING_OWNER_CONFIRMATION);
        ClaimEntity claim = acceptedClaim(foundItem.getId());
        claim.setFinderMarkedReturnedAt(LocalDateTime.now().minusMinutes(1));

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse(foundItem.getId())).thenReturn(Optional.of(foundItem));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chatRepository.findByClaimId("claim-1")).thenReturn(Optional.of(chat));

        ClaimDTO dto = claimService.confirmItemReceived("claim-1", "claimant@usc.edu.ph");

        assertThat(dto.getStatus()).isEqualTo("COMPLETED");
        assertThat(foundItem.getStatus()).isEqualTo(ItemStatus.RETURNED);
        verify(userService).incrementKarma("user-finder", 10);   // FOUND: poster is finder
        verify(userService).incrementKarma("user-claimant", 5);  // FOUND: claimant is owner
        verify(notificationService).notifyItemReturned(eq("user-finder"), eq("Black Wallet"), eq(10), eq("chat-1"));
        verify(notificationService).notifyItemReturned(eq("user-claimant"), eq("Black Wallet"), eq(5), eq("chat-1"));
    }

    @Test
    @DisplayName("confirmItemReceived (LOST): inverts roles — claimant gets +10, poster (owner) gets +5")
    void confirmItemReceived_lostFlow_invertsRoles() {
        lostItem.setStatus(ItemStatus.PENDING_OWNER_CONFIRMATION);
        ClaimEntity claim = acceptedClaim(lostItem.getId());
        claim.setFinderMarkedReturnedAt(LocalDateTime.now().minusMinutes(1));

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(itemRepository.findByIdAndIsDeletedFalse(lostItem.getId())).thenReturn(Optional.of(lostItem));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chatRepository.findByClaimId("claim-1")).thenReturn(Optional.of(chat));

        claimService.confirmItemReceived("claim-1", "finder@usc.edu.ph");

        // LOST: actual finder is the claimant; actual owner is the poster (finderId)
        verify(userService).incrementKarma("user-claimant", 10);
        verify(userService).incrementKarma("user-finder", 5);
    }

    @Test
    @DisplayName("confirmItemReceived rejects when item is not in PENDING_OWNER_CONFIRMATION")
    void confirmItemReceived_wrongItemState_rejected() {
        foundItem.setStatus(ItemStatus.CLAIMED);
        ClaimEntity claim = acceptedClaim(foundItem.getId());

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse(foundItem.getId())).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.confirmItemReceived("claim-1", "claimant@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must mark the item as returned first");
    }

    // ── disputeHandover ─────────────────────────────────────

    @Test
    @DisplayName("disputeHandover reverts PENDING_OWNER_CONFIRMATION -> CLAIMED and clears mark")
    void disputeHandover_revertsState() {
        foundItem.setStatus(ItemStatus.PENDING_OWNER_CONFIRMATION);
        ClaimEntity claim = acceptedClaim(foundItem.getId());
        claim.setFinderMarkedReturnedAt(LocalDateTime.now().minusMinutes(1));

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("claimant@usc.edu.ph")).thenReturn(Optional.of(claimant));
        when(itemRepository.findByIdAndIsDeletedFalse(foundItem.getId())).thenReturn(Optional.of(foundItem));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(claimRepository.save(any(ClaimEntity.class))).thenAnswer(inv -> inv.getArgument(0));
        when(chatRepository.findByClaimId("claim-1")).thenReturn(Optional.of(chat));

        claimService.disputeHandover("claim-1", "claimant@usc.edu.ph");

        assertThat(foundItem.getStatus()).isEqualTo(ItemStatus.CLAIMED);
        assertThat(claim.getFinderMarkedReturnedAt()).isNull();
        verify(notificationService).notifyHandoverDisputed(
                eq("user-finder"), anyString(), eq("Black Wallet"), eq("chat-1"));
    }

    @Test
    @DisplayName("disputeHandover is forbidden for non-owner")
    void disputeHandover_nonOwner_forbidden() {
        foundItem.setStatus(ItemStatus.PENDING_OWNER_CONFIRMATION);
        ClaimEntity claim = acceptedClaim(foundItem.getId());

        when(claimRepository.findById("claim-1")).thenReturn(Optional.of(claim));
        when(userRepository.findByEmail("finder@usc.edu.ph")).thenReturn(Optional.of(finder));
        when(itemRepository.findByIdAndIsDeletedFalse(foundItem.getId())).thenReturn(Optional.of(foundItem));

        assertThatThrownBy(() -> claimService.disputeHandover("claim-1", "finder@usc.edu.ph"))
                .isInstanceOf(ForbiddenException.class);
    }

    // ── helpers ─────────────────────────────────────────────

    private ClaimEntity pendingClaim() {
        ClaimEntity c = new ClaimEntity();
        c.setId("claim-1");
        c.setItemId(foundItem.getId());
        c.setClaimantId(claimant.getId());
        c.setFinderId(finder.getId());
        c.setStatus(ClaimStatus.PENDING);
        c.setProvidedAnswer("Levi's");
        c.setMessage("hi");
        c.setCreatedAt(LocalDateTime.now());
        c.setUpdatedAt(LocalDateTime.now());
        return c;
    }

    private ClaimEntity acceptedClaim(String itemId) {
        ClaimEntity c = pendingClaim();
        c.setItemId(itemId);
        c.setStatus(ClaimStatus.ACCEPTED);
        return c;
    }
}
