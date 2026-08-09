package com.dawns.tingstable.model;

public final class BackNavigationState {
    public static final long EXIT_CONFIRM_WINDOW_MS = 2000L;

    private long lastHomeBackAt = Long.MIN_VALUE;

    public boolean shouldConfirmExit(long now) {
        if (lastHomeBackAt != Long.MIN_VALUE
                && now >= lastHomeBackAt
                && now - lastHomeBackAt <= EXIT_CONFIRM_WINDOW_MS) {
            reset();
            return true;
        }
        lastHomeBackAt = now;
        return false;
    }

    public void reset() {
        lastHomeBackAt = Long.MIN_VALUE;
    }
}
