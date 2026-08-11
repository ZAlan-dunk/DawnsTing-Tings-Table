package com.dawns.tingstable.model;

public final class RecipeUsage {
    public final int openCount;
    public final long lastOpenedAt;

    public RecipeUsage(int openCount, long lastOpenedAt) {
        this.openCount = Math.max(0, openCount);
        this.lastOpenedAt = Math.max(0L, lastOpenedAt);
    }
}
