package com.dawns.tingstable.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BackNavigationStateTest {
    @Test
    public void firstBackDoesNotConfirmExit() {
        BackNavigationState state = new BackNavigationState();

        assertFalse(state.shouldConfirmExit(1000L));
    }

    @Test
    public void secondBackWithinWindowConfirmsAndResets() {
        BackNavigationState state = new BackNavigationState();

        assertFalse(state.shouldConfirmExit(1000L));
        assertTrue(state.shouldConfirmExit(3000L));
        assertFalse(state.shouldConfirmExit(3001L));
    }

    @Test
    public void delayedBackStartsANewSequence() {
        BackNavigationState state = new BackNavigationState();

        assertFalse(state.shouldConfirmExit(1000L));
        assertFalse(state.shouldConfirmExit(1000L + BackNavigationState.EXIT_CONFIRM_WINDOW_MS + 1L));
    }

    @Test
    public void backwardsClockDoesNotConfirmExit() {
        BackNavigationState state = new BackNavigationState();

        assertFalse(state.shouldConfirmExit(1000L));
        assertFalse(state.shouldConfirmExit(900L));
    }
}
