package edu.cit.chan.unilost.features.item;

import edu.cit.chan.unilost.features.campus.CampusEntity;
import edu.cit.chan.unilost.features.campus.CampusRepository;
import edu.cit.chan.unilost.features.cloudinary.CloudinaryService;
import edu.cit.chan.unilost.features.user.AccountStatus;
import edu.cit.chan.unilost.features.user.Role;
import edu.cit.chan.unilost.features.user.UserEntity;
import edu.cit.chan.unilost.features.user.UserRepository;
import edu.cit.chan.unilost.shared.exception.ForbiddenException;
import edu.cit.chan.unilost.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ItemService.
 *
 * Covers: createItem (validation + defaults), getItemById secret-question hiding,
 * updateItem ownership/status guards, softDeleteItem guards.
 *
 * Pure JUnit 5 + Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock private ItemRepository itemRepository;
    @Mock private UserRepository userRepository;
    @Mock private CampusRepository campusRepository;
    @Mock private CloudinaryService cloudinaryService;
    @Mock private MongoTemplate mongoTemplate;

    @InjectMocks private ItemService itemService;

    private UserEntity reporter;
    private UserEntity otherUser;
    private UserEntity admin;
    private CampusEntity campus;
    private ItemEntity activeItem;

    @BeforeEach
    void setUp() {
        reporter = new UserEntity();
        reporter.setId("user-reporter");
        reporter.setEmail("reporter@usc.edu.ph");
        reporter.setUniversityTag("campus-usc");
        reporter.setRole(Role.STUDENT);
        reporter.setAccountStatus(AccountStatus.ACTIVE);

        otherUser = new UserEntity();
        otherUser.setId("user-other");
        otherUser.setEmail("other@usc.edu.ph");
        otherUser.setRole(Role.STUDENT);
        otherUser.setAccountStatus(AccountStatus.ACTIVE);

        admin = new UserEntity();
        admin.setId("user-admin");
        admin.setEmail("admin@usc.edu.ph");
        admin.setRole(Role.ADMIN);
        admin.setAccountStatus(AccountStatus.ACTIVE);

        campus = new CampusEntity();
        campus.setId("campus-usc");
        campus.setName("University of San Carlos");

        activeItem = new ItemEntity();
        activeItem.setId("item-1");
        activeItem.setTitle("Black Wallet");
        activeItem.setDescription("Lost on May 1");
        activeItem.setType("FOUND");
        activeItem.setCategory("WALLETS");
        activeItem.setStatus(ItemStatus.ACTIVE);
        activeItem.setReporterId(reporter.getId());
        activeItem.setCampusId(campus.getId());
        activeItem.setSecretDetailQuestion("What is the brand?");
        activeItem.setCreatedAt(LocalDateTime.now());
        activeItem.setUpdatedAt(LocalDateTime.now());
    }

    // ── createItem ──────────────────────────────────────────

    @Test
    @DisplayName("createItem persists ACTIVE item with reporter and campus defaults")
    void createItem_persistsWithDefaults() throws Exception {
        ItemRequest req = new ItemRequest(
                "Black Wallet", "Lost on May 1", "FOUND", "WALLETS",
                "Library", null, null, "What is the brand?", null, null);

        when(userRepository.findByEmail("reporter@usc.edu.ph")).thenReturn(Optional.of(reporter));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> {
            ItemEntity e = inv.getArgument(0);
            e.setId("item-1");
            return e;
        });

        ItemDTO dto = itemService.createItem(req, "reporter@usc.edu.ph", null);

        assertThat(dto.getId()).isEqualTo("item-1");
        assertThat(dto.getTitle()).isEqualTo("Black Wallet");
        assertThat(dto.getStatus()).isEqualTo("ACTIVE");
        assertThat(dto.getReporterId()).isEqualTo("user-reporter");
        assertThat(dto.getCampusId()).isEqualTo("campus-usc"); // defaulted from reporter

        ArgumentCaptor<ItemEntity> captor = ArgumentCaptor.forClass(ItemEntity.class);
        verify(itemRepository).save(captor.capture());
        ItemEntity saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(ItemStatus.ACTIVE);
        assertThat(saved.getDateLostFound()).isNotNull(); // defaults to now
    }

    @Test
    @DisplayName("createItem rejects when reporter is not found")
    void createItem_rejectsUnknownReporter() {
        ItemRequest req = new ItemRequest(
                "x", "x", "FOUND", "OTHER", null, null, null, null, null, null);
        when(userRepository.findByEmail("ghost@usc.edu.ph")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> itemService.createItem(req, "ghost@usc.edu.ph", null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("createItem rejects when latitude is provided without longitude")
    void createItem_rejectsHalfCoordinates() {
        ItemRequest req = new ItemRequest(
                "x", "x", "FOUND", "OTHER", null, 10.3, null, null, null, null);
        when(userRepository.findByEmail("reporter@usc.edu.ph")).thenReturn(Optional.of(reporter));

        assertThatThrownBy(() -> itemService.createItem(req, "reporter@usc.edu.ph", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Both latitude and longitude");
    }

    // ── getItemById: secret question visibility ────────────

    @Test
    @DisplayName("getItemById exposes secretDetailQuestion to the reporter (owner)")
    void getItemById_ownerSeesSecretQuestion() {
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findById(reporter.getId())).thenReturn(Optional.of(reporter));
        when(campusRepository.findById(campus.getId())).thenReturn(Optional.of(campus));
        when(userRepository.findByEmail("reporter@usc.edu.ph")).thenReturn(Optional.of(reporter));

        Optional<ItemDTO> dto = itemService.getItemById("item-1", "reporter@usc.edu.ph");

        assertThat(dto).isPresent();
        assertThat(dto.get().getSecretDetailQuestion()).isEqualTo("What is the brand?");
    }

    @Test
    @DisplayName("getItemById hides secretDetailQuestion from non-owner non-admin")
    void getItemById_otherUserDoesNotSeeSecretQuestion() {
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findById(reporter.getId())).thenReturn(Optional.of(reporter));
        when(campusRepository.findById(campus.getId())).thenReturn(Optional.of(campus));
        when(userRepository.findByEmail("other@usc.edu.ph")).thenReturn(Optional.of(otherUser));

        Optional<ItemDTO> dto = itemService.getItemById("item-1", "other@usc.edu.ph");

        assertThat(dto).isPresent();
        assertThat(dto.get().getSecretDetailQuestion()).isNull();
    }

    @Test
    @DisplayName("getItemById hides secretDetailQuestion from anonymous caller")
    void getItemById_anonymousDoesNotSeeSecretQuestion() {
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findById(reporter.getId())).thenReturn(Optional.of(reporter));
        when(campusRepository.findById(campus.getId())).thenReturn(Optional.of(campus));

        Optional<ItemDTO> dto = itemService.getItemById("item-1", null);

        assertThat(dto).isPresent();
        assertThat(dto.get().getSecretDetailQuestion()).isNull();
    }

    @Test
    @DisplayName("getItemById exposes secretDetailQuestion to admin")
    void getItemById_adminSeesSecretQuestion() {
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findById(reporter.getId())).thenReturn(Optional.of(reporter));
        when(campusRepository.findById(campus.getId())).thenReturn(Optional.of(campus));
        when(userRepository.findByEmail("admin@usc.edu.ph")).thenReturn(Optional.of(admin));

        Optional<ItemDTO> dto = itemService.getItemById("item-1", "admin@usc.edu.ph");

        assertThat(dto).isPresent();
        assertThat(dto.get().getSecretDetailQuestion()).isEqualTo("What is the brand?");
    }

    @Test
    @DisplayName("getItemById returns empty when item missing or soft-deleted")
    void getItemById_missing() {
        when(itemRepository.findByIdAndIsDeletedFalse("missing")).thenReturn(Optional.empty());
        assertThat(itemService.getItemById("missing", "reporter@usc.edu.ph")).isEmpty();
    }

    // ── updateItem ──────────────────────────────────────────

    @Test
    @DisplayName("updateItem allows the reporter to edit an ACTIVE item")
    void updateItem_byOwner_succeeds() throws Exception {
        ItemRequest req = new ItemRequest(
                "Updated Title", null, "FOUND", "WALLETS", null, null, null, null, null, null);
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findByEmail("reporter@usc.edu.ph")).thenReturn(Optional.of(reporter));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ItemDTO> result = itemService.updateItem("item-1", req, "reporter@usc.edu.ph", null);

        assertThat(result).isPresent();
        assertThat(activeItem.getTitle()).isEqualTo("Updated Title");
    }

    @Test
    @DisplayName("updateItem rejects a non-owner non-admin")
    void updateItem_byOtherUser_forbidden() {
        ItemRequest req = new ItemRequest(
                "Hijack", null, "FOUND", "WALLETS", null, null, null, null, null, null);
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findByEmail("other@usc.edu.ph")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> itemService.updateItem("item-1", req, "other@usc.edu.ph", null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessageContaining("permission");
    }

    @Test
    @DisplayName("updateItem allows admin to edit any ACTIVE item")
    void updateItem_byAdmin_succeeds() throws Exception {
        ItemRequest req = new ItemRequest(
                "Admin edit", null, "FOUND", "WALLETS", null, null, null, null, null, null);
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findByEmail("admin@usc.edu.ph")).thenReturn(Optional.of(admin));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<ItemDTO> result = itemService.updateItem("item-1", req, "admin@usc.edu.ph", null);

        assertThat(result).isPresent();
        assertThat(activeItem.getTitle()).isEqualTo("Admin edit");
    }

    @Test
    @DisplayName("updateItem refuses to edit a non-ACTIVE item")
    void updateItem_rejectsNonActiveStatus() {
        activeItem.setStatus(ItemStatus.CLAIMED);
        ItemRequest req = new ItemRequest(
                "Try", null, "FOUND", "WALLETS", null, null, null, null, null, null);
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findByEmail("reporter@usc.edu.ph")).thenReturn(Optional.of(reporter));

        assertThatThrownBy(() -> itemService.updateItem("item-1", req, "reporter@usc.edu.ph", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot edit");
    }

    // ── softDeleteItem ──────────────────────────────────────

    @Test
    @DisplayName("softDeleteItem flags the item and saves it")
    void softDeleteItem_byOwner_succeeds() {
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findByEmail("reporter@usc.edu.ph")).thenReturn(Optional.of(reporter));
        when(itemRepository.save(any(ItemEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        boolean ok = itemService.softDeleteItem("item-1", "reporter@usc.edu.ph");

        assertThat(ok).isTrue();
        assertThat(activeItem.isDeleted()).isTrue();
        assertThat(activeItem.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("softDeleteItem rejects non-owner")
    void softDeleteItem_byOther_forbidden() {
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findByEmail("other@usc.edu.ph")).thenReturn(Optional.of(otherUser));

        assertThatThrownBy(() -> itemService.softDeleteItem("item-1", "other@usc.edu.ph"))
                .isInstanceOf(ForbiddenException.class);
        verify(itemRepository, never()).save(any());
    }

    @Test
    @DisplayName("softDeleteItem refuses to delete CLAIMED items")
    void softDeleteItem_rejectsClaimed() {
        activeItem.setStatus(ItemStatus.CLAIMED);
        when(itemRepository.findByIdAndIsDeletedFalse("item-1")).thenReturn(Optional.of(activeItem));
        when(userRepository.findByEmail("reporter@usc.edu.ph")).thenReturn(Optional.of(reporter));

        assertThatThrownBy(() -> itemService.softDeleteItem("item-1", "reporter@usc.edu.ph"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot delete");
    }

    @Test
    @DisplayName("softDeleteItem returns false when item not found")
    void softDeleteItem_returnsFalseWhenMissing() {
        when(itemRepository.findByIdAndIsDeletedFalse("missing")).thenReturn(Optional.empty());
        assertThat(itemService.softDeleteItem("missing", "reporter@usc.edu.ph")).isFalse();
    }
}
