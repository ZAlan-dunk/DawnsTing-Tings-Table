package com.dawns.tingstable;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.util.Random;

/**
 * Subtle paper-and-leaf atmosphere for the recipe pages. The movement is deliberately slow
 * and low contrast so it adds warmth without competing with reading or form input.
 */
public class AmbientBackgroundView extends View {
    private final Paint fill = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint line = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint grain = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path leaf = new Path();
    private final RectF oval = new RectF();
    private final float[] grainPoints = new float[180];
    private final Random random = new Random(70428L);
    private ValueAnimator animator;
    private float motion;
    private boolean motionEnabled = true;

    public AmbientBackgroundView(Context context) {
        super(context);
        init();
    }

    public AmbientBackgroundView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(dp(1.1f));
        line.setStrokeCap(Paint.Cap.ROUND);
        line.setStrokeJoin(Paint.Join.ROUND);
        grain.setStyle(Paint.Style.FILL);
        for (int i = 0; i < grainPoints.length; i++) grainPoints[i] = random.nextFloat();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ValueAnimator.areAnimatorsEnabled()) {
            animator = ValueAnimator.ofFloat(0f, 1f);
            animator.setDuration(18000L);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new LinearInterpolator());
            animator.addUpdateListener(value -> {
                motion = (Float) value.getAnimatedValue();
                invalidate();
            });
            animator.start();
        }
    }

    public void setMotionEnabled(boolean enabled) {
        motionEnabled = enabled;
        if (animator == null) return;
        if (enabled && !animator.isStarted()) animator.start();
        else if (!enabled && animator.isRunning()) animator.pause();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        if (width <= 0 || height <= 0) return;

        fill.setShader(new LinearGradient(0, 0, width, height,
                Color.rgb(248, 241, 228), Color.rgb(240, 232, 216), Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, width, height, fill);
        fill.setShader(null);

        drawGlow(canvas, width * (0.14f + motion * 0.12f), height * 0.12f,
                Math.min(width, height) * 0.38f, Color.argb(17, 168, 93, 76));
        drawGlow(canvas, width * (0.84f - motion * 0.10f), height * 0.72f,
                Math.min(width, height) * 0.42f, Color.argb(13, 40, 83, 74));

        grain.setColor(Color.argb(18, 100, 78, 54));
        for (int i = 0; i < grainPoints.length; i += 2) {
            float x = grainPoints[i] * width;
            float y = grainPoints[i + 1] * height;
            float radius = (i % 6 == 0) ? dp(0.8f) : dp(0.45f);
            canvas.drawCircle(x, y, radius, grain);
        }

        line.setColor(Color.argb(34, 40, 83, 74));
        drawLeaf(canvas, width * 0.08f, height * 0.20f, 1.0f, -0.35f);
        drawLeaf(canvas, width * 0.92f, height * 0.38f, 0.82f, 2.45f);
        drawLeaf(canvas, width * 0.16f, height * 0.84f, 0.72f, 2.95f);
        line.setColor(Color.argb(24, 168, 93, 76));
        drawSteam(canvas, width * 0.78f, height * 0.14f, 0.9f);
        drawSteam(canvas, width * 0.23f, height * 0.62f, 0.7f);
    }

    private void drawGlow(Canvas canvas, float x, float y, float radius, int color) {
        fill.setShader(new android.graphics.RadialGradient(x, y, radius,
                new int[]{color, Color.argb(0, Color.red(color), Color.green(color), Color.blue(color))},
                new float[]{0f, 1f}, Shader.TileMode.CLAMP));
        canvas.drawCircle(x, y, radius, fill);
        fill.setShader(null);
    }

    private void drawLeaf(Canvas canvas, float x, float y, float scale, float rotation) {
        canvas.save();
        canvas.rotate((float) Math.toDegrees(rotation), x, y);
        float w = dp(36) * scale;
        float h = dp(92) * scale;
        leaf.reset();
        leaf.moveTo(x, y + h * 0.5f);
        leaf.cubicTo(x - w, y + h * 0.18f, x - w * 0.62f, y - h * 0.34f, x, y - h * 0.5f);
        leaf.cubicTo(x + w * 0.62f, y - h * 0.34f, x + w, y + h * 0.18f, x, y + h * 0.5f);
        canvas.drawPath(leaf, line);
        canvas.drawLine(x, y - h * 0.42f, x, y + h * 0.42f, line);
        canvas.restore();
    }

    private void drawSteam(Canvas canvas, float x, float y, float scale) {
        float travel = motionEnabled ? (float) Math.sin(motion * Math.PI * 2) * dp(5) : 0f;
        Path steam = new Path();
        steam.moveTo(x, y + travel);
        steam.cubicTo(x - dp(9) * scale, y - dp(15) * scale + travel,
                x + dp(10) * scale, y - dp(25) * scale + travel,
                x, y - dp(42) * scale + travel);
        canvas.drawPath(steam, line);
    }

    private float dp(float value) {
        return value * getResources().getDisplayMetrics().density;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (animator != null) animator.cancel();
        super.onDetachedFromWindow();
    }
}
