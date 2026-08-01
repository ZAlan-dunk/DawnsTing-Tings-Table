package com.dawns.tingstable.util;

import android.annotation.SuppressLint;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public final class MotionSpec {
    public static final long PAGE = 180L;
    public static final long FILTER = 140L;
    public static final long FAVORITE = 160L;
    public static final long PRESS = 90L;

    private static final DecelerateInterpolator EASING = new DecelerateInterpolator();

    private MotionSpec() {}

    public static boolean enabled() {
        return ValueAnimator.areAnimatorsEnabled();
    }

    public static void enter(View view, float offsetPx) {
        view.animate().cancel();
        if (!enabled()) {
            view.setAlpha(1f);
            view.setTranslationY(0f);
            return;
        }
        view.setAlpha(0f);
        view.setTranslationY(offsetPx);
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(PAGE)
                .setInterpolator(EASING)
                .start();
    }

    public static void crossfade(View view, Runnable update) {
        view.animate().cancel();
        if (!enabled()) {
            update.run();
            view.setAlpha(1f);
            return;
        }
        view.animate()
                .alpha(0.35f)
                .setDuration(FILTER / 2)
                .withEndAction(() -> {
                    update.run();
                    view.animate()
                            .alpha(1f)
                            .setDuration(FILTER / 2)
                            .setInterpolator(EASING)
                            .start();
                })
                .start();
    }

    public static void favorite(View view) {
        view.animate().cancel();
        if (!enabled()) {
            view.setScaleX(1f);
            view.setScaleY(1f);
            return;
        }
        view.setScaleX(0.86f);
        view.setScaleY(0.86f);
        view.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(90L)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(FAVORITE - 90L)
                        .setInterpolator(EASING)
                        .start())
                .start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void attachPress(View view) {
        view.setOnTouchListener((target, event) -> {
            if (!enabled()) return false;
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                target.animate().scaleX(0.98f).scaleY(0.98f).setDuration(PRESS).start();
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                target.animate().scaleX(1f).scaleY(1f).setDuration(PRESS).start();
            }
            return false;
        });
    }
}
