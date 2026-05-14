package edu.cit.chan.unilost.features.item;

public enum ItemStatus {
    ACTIVE, CLAIMED, PENDING_OWNER_CONFIRMATION, RETURNED, EXPIRED, TURNED_OVER_TO_OFFICE, HIDDEN,
    HANDED_OVER  // Deprecated: kept for backward compat with old MongoDB documents
}
