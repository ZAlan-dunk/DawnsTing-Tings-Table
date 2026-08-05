package com.dawns.tingstable.util;

import android.annotation.SuppressLint;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

public final class MotionSpec {
    public static final long PAGE = 180L;
    public static final long FILTER = 140L;
    public static final long FAVORITE = 160L;
    public static final long PRESS = 90L;
    public static final long SIBLING_PAGE = 190L;
    public static final long DETAIL_PAGE = 220L;
    public static final long BOTTOM_SHEET = 220L;
    public static final long ICON_FEEDBACK = 140L;

    private static final DecelerateInterpolator EASING = new DecelerateInterpolator();
    private static final MotionHandle NO_OP = new MotionHandle() {
        @Override
        public void cancel() {}

        @Override
        public boolean isRunning() {
            return false;
        }
    };

    private MotionSpec() {}

    public static boolean enabled() {
        return ValueAnimator.areAnimatorsEnabled();
    }

    public interface MotionHandle {
        void cancel();

        boolean isRunning();
    }

    public static void enter(View view, float offsetPx) {
        cancelAndReset(view);
        if (!enabled()) {
            return;
        }
        view.setAlpha(0f);
        view.setTranslationY(offsetPx);
        FinishListener listener = new FinishListener(() -> resetVisible(view));
        view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(PAGE)
                .setInterpolator(EASING)
                .setListener(listener)
                .start();
    }

    /** Moves between destinations at the same navigation level. */
    public static MotionHandle siblingTransition(
            View outgoing,
            View incoming,
            boolean moveToNext,
            float offsetPx,
            Runnable endAction
    ) {
        float direction = moveToNext ? 1f : -1f;
        return pageTransition(
                outgoing,
                incoming,
                direction,
                Math.abs(offsetPx),
                SIBLING_PAGE,
                0.42f,
                endAction
        );
    }

    /** Enters a child/detail destination from the trailing edge. */
    public static MotionHandle forwardTransition(
            View outgoing,
            View incoming,
            float offsetPx,
            Runnable endAction
    ) {
        return pageTransition(
                outgoing,
                incoming,
                1f,
                Math.abs(offsetPx),
                DETAIL_PAGE,
                0.28f,
                endAction
        );
    }

    /** Returns to the parent destination, reversing forwardTransition. */
    public static MotionHandle backTransition(
            View outgoing,
            View incoming,
            float offsetPx,
            Runnable endAction
    ) {
        return pageTransition(
                outgoing,
                incoming,
                -1f,
                Math.abs(offsetPx),
                DETAIL_PAGE,
                0.28f,
                endAction
        );
    }

    /** Slides a modal sheet into its final, fully visible position. */
    public static MotionHandle bottomSheetEnter(View sheet, float offsetPx) {
        cancelAndReset(sheet);
        Runnable finish = () -> resetVisible(sheet);
        if (!enabled()) {
            finish.run();
            return NO_OP;
        }

        sheet.setAlpha(0f);
        sheet.setTranslationY(Math.abs(offsetPx));
        FinishListener listener = new FinishListener(finish);
        sheet.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(BOTTOM_SHEET)
                .setInterpolator(EASING)
                .setListener(listener)
                .start();
        return handle(listener, sheet);
    }

    /** Gives a directional icon a short horizontal acknowledgement. */
    public static MotionHandle iconNudge(View icon, float distancePx) {
        cancelAndReset(icon);
        Runnable finish = () -> resetVisible(icon);
        if (!enabled()) {
            finish.run();
            return NO_OP;
        }

        FinishListener listener = new FinishListener(finish);
        icon.animate()
                .translationX(distancePx)
                .setDuration(ICON_FEEDBACK / 2)
                .setInterpolator(EASING)
                .withEndAction(() -> icon.animate()
                        .translationX(0f)
                        .setDuration(ICON_FEEDBACK / 2)
                        .setInterpolator(EASING)
                        .setListener(listener)
                        .start())
                .start();
        return handle(listener, icon);
    }

    /** Pulses an icon without leaving scale state behind. */
    public static MotionHandle selectionFeedback(View icon) {
        cancelAndReset(icon);
        Runnable finish = () -> resetVisible(icon);
        if (!enabled()) {
            finish.run();
            return NO_OP;
        }

        FinishListener listener = new FinishListener(finish);
        icon.animate()
                .scaleX(0.9f)
                .scaleY(0.9f)
                .setDuration(ICON_FEEDBACK / 2)
                .setInterpolator(EASING)
                .withEndAction(() -> icon.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(ICON_FEEDBACK / 2)
                        .setInterpolator(EASING)
                        .setListener(listener)
                        .start())
                .start();
        return handle(listener, icon);
    }

    public static void crossfade(View view, Runnable update) {
        cancelAndReset(view);
        if (!enabled()) {
            update.run();
            return;
        }
        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            private boolean canceled;

            @Override
            public void onAnimationCancel(Animator animation) {
                canceled = true;
                resetVisible(view);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (canceled) return;
                update.run();
                FinishListener finishListener = new FinishListener(() -> resetVisible(view));
                view.animate()
                        .alpha(1f)
                        .setDuration(FILTER / 2)
                        .setInterpolator(EASING)
                        .setListener(finishListener)
                        .start();
            }
        };
        view.animate()
                .alpha(0.35f)
                .setDuration(FILTER / 2)
                .setInterpolator(EASING)
                .setListener(listener)
                .start();
    }

    public static void favorite(View view) {
        cancelAndReset(view);
        if (!enabled()) {
            return;
        }
        view.setScaleX(0.86f);
        view.setScaleY(0.86f);
        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            private boolean canceled;

            @Override
            public void onAnimationCancel(Animator animation) {
                canceled = true;
                resetVisible(view);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (canceled) return;
                FinishListener finishListener = new FinishListener(() -> resetVisible(view));
                view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(FAVORITE - 90L)
                        .setInterpolator(EASING)
                        .setListener(finishListener)
                        .start();
            }
        };
        view.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .setDuration(90L)
                .setInterpolator(EASING)
                .setListener(listener)
                .start();
    }

    @SuppressLint("ClickableViewAccessibility")
    public static void attachPress(View view) {
        view.setOnTouchListener((target, event) -> {
            if (!enabled()) {
                target.animate().cancel();
                target.setScaleX(1f);
                target.setScaleY(1f);
                return false;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                target.animate().cancel();
                target.animate().scaleX(0.98f).scaleY(0.98f).setDuration(PRESS).start();
            } else if (event.getActionMasked() == MotionEvent.ACTION_UP
                    || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                target.animate().cancel();
                target.animate().scaleX(1f).scaleY(1f).setDuration(PRESS).start();
            }
            return false;
        });
    }

    private static MotionHandle pageTransition(
            View outgoing,
            View incoming,
            float direction,
            float offsetPx,
            long duration,
            float outgoingDistanceFactor,
            Runnable endAction
    ) {
        cancelAndReset(outgoing);
        cancelAndReset(incoming);
        Runnable finish = () -> {
            outgoing.setAlpha(0f);
            outgoing.setTranslationX(0f);
            outgoing.setTranslationY(0f);
            outgoing.setScaleX(1f);
            outgoing.setScaleY(1f);
            resetVisible(incoming);
            if (endAction != null) endAction.run();
        };
        if (!enabled()) {
            finish.run();
            return NO_OP;
        }

        incoming.setAlpha(0f);
        incoming.setTranslationX(direction * offsetPx);
        FinishListener listener = new FinishListener(finish);
        outgoing.animate()
                .alpha(0f)
                .translationX(-direction * offsetPx * outgoingDistanceFactor)
                .setDuration(duration)
                .setInterpolator(EASING)
                .start();
        incoming.animate()
                .alpha(1f)
                .translationX(0f)
                .setDuration(duration)
                .setInterpolator(EASING)
                .setListener(listener)
                .start();
        return handle(listener, outgoing, incoming);
    }

    private static MotionHandle handle(FinishListener listener, View... views) {
        return new MotionHandle() {
            @Override
            public void cancel() {
                for (View view : views) view.animate().cancel();
                listener.finish();
            }

            @Override
            public boolean isRunning() {
                return !listener.isFinished();
            }
        };
    }

    private static void cancelAndReset(View view) {
        view.animate().cancel();
        view.animate().setListener(null).withEndAction(null);
        resetVisible(view);
    }

    private static void resetVisible(View view) {
        view.setAlpha(1f);
        view.setTranslationX(0f);
        view.setTranslationY(0f);
        view.setScaleX(1f);
        view.setScaleY(1f);
    }

    private static final class FinishListener extends AnimatorListenerAdapter {
        private final Runnable finishAction;
        private boolean finished;

        FinishListener(Runnable finishAction) {
            this.finishAction = finishAction;
        }

        @Override
        public void onAnimationCancel(Animator animation) {
            finish();
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            finish();
        }

        void finish() {
            if (finished) return;
            finished = true;
            finishAction.run();
        }

        boolean isFinished() {
            return finished;
        }
    }
}
