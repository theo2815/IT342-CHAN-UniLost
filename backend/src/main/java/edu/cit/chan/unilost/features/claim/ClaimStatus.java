package edu.cit.chan.unilost.features.claim;

public enum ClaimStatus {
    PENDING,
    ACCEPTED,
    REJECTED,
    CANCELLED,
    COMPLETED,
    HANDED_OVER  // Deprecated: kept for backward compat with old MongoDB documents
}
