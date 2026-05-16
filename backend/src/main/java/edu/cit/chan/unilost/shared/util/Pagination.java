package edu.cit.chan.unilost.shared.util;

public final class Pagination {
    public static final int DEFAULT_MAX_PAGE_SIZE = 50;
    public static final int MESSAGES_MAX_PAGE_SIZE = 100;

    private Pagination() {}

    public static int clamp(int requested, int max) {
        if (requested < 1) return 1;
        return Math.min(requested, max);
    }

    public static int clamp(int requested) {
        return clamp(requested, DEFAULT_MAX_PAGE_SIZE);
    }
}
