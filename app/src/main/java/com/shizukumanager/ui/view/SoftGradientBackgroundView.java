package com.shizukumanager.app.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.shizukumanager.app.R;

// View ve nen Linear Gradient da diem ket hop cac vung mau pastel mo tao hieu ung glassmorphism
public class SoftGradientBackgroundView extends View {

    private final Paint baseGradientPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint orbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int[] baseColors;
    private float[] basePositions;
    private int orbLavender;
    private int orbPink;
    private int orbBlue;
    private int orbPurple;
    private int orbWhite;

    private boolean ambientOrbsEnabled = true;

    private RadialGradient lavenderShader;
    private RadialGradient pinkShader;
    private RadialGradient blueShader;
    private RadialGradient purpleShader;
    private RadialGradient whiteGlowShader;

    private float cx1, cy1, r1;
    private float cx2, cy2, r2;
    private float cx3, cy3, r3;
    private float cx4, cy4, r4;
    private float cx5, cy5, r5;

    public SoftGradientBackgroundView(Context context) {
        super(context);
        init(context);
    }

    public SoftGradientBackgroundView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SoftGradientBackgroundView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        baseColors = new int[]{
                ContextCompat.getColor(context, R.color.bg_grad_top_start),
                ContextCompat.getColor(context, R.color.bg_grad_mid_lavender),
                ContextCompat.getColor(context, R.color.bg_grad_mid_pink),
                ContextCompat.getColor(context, R.color.bg_grad_bottom_blue),
                ContextCompat.getColor(context, R.color.bg_grad_white)
        };
        basePositions = new float[]{0f, 0.25f, 0.52f, 0.80f, 1.0f};

        orbLavender = ContextCompat.getColor(context, R.color.blob_lavender);
        orbPink = ContextCompat.getColor(context, R.color.blob_pastel_pink);
        orbBlue = ContextCompat.getColor(context, R.color.blob_light_blue);
        orbPurple = ContextCompat.getColor(context, R.color.blob_soft_purple);
        orbWhite = ContextCompat.getColor(context, R.color.blob_white_glow);
    }

    public void setAmbientOrbsEnabled(boolean enabled) {
        if (this.ambientOrbsEnabled != enabled) {
            this.ambientOrbsEnabled = enabled;
            invalidate();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w <= 0 || h <= 0) {
            return;
        }
        baseGradientPaint.setShader(new LinearGradient(
                0f, 0f, w * 0.75f, h,
                baseColors, basePositions, Shader.TileMode.CLAMP
        ));

        float minDim = Math.min(w, h);

        cx1 = w * 0.14f;
        cy1 = h * 0.12f;
        r1 = minDim * 0.72f;
        lavenderShader = createSoftOrb(cx1, cy1, r1, orbLavender);

        cx2 = w * 0.88f;
        cy2 = h * 0.24f;
        r2 = minDim * 0.68f;
        pinkShader = createSoftOrb(cx2, cy2, r2, orbPink);

        cx3 = w * 0.22f;
        cy3 = h * 0.62f;
        r3 = minDim * 0.75f;
        blueShader = createSoftOrb(cx3, cy3, r3, orbBlue);

        cx4 = w * 0.82f;
        cy4 = h * 0.84f;
        r4 = minDim * 0.78f;
        purpleShader = createSoftOrb(cx4, cy4, r4, orbPurple);

        cx5 = w * 0.50f;
        cy5 = h * 0.45f;
        r5 = minDim * 0.65f;
        whiteGlowShader = createSoftOrb(cx5, cy5, r5, orbWhite);
    }

    private RadialGradient createSoftOrb(float cx, float cy, float radius, int centerColor) {
        int a = (centerColor >>> 24) & 0xFF;
        int rgb = centerColor & 0x00FFFFFF;
        int cMid = ((int) (a * 0.52f) << 24) | rgb;
        int cOuter = ((int) (a * 0.16f) << 24) | rgb;
        int cEdge = 0x00000000 | rgb;
        return new RadialGradient(
                cx, cy, radius,
                new int[]{centerColor, cMid, cOuter, cEdge},
                new float[]{0f, 0.38f, 0.72f, 1.0f},
                Shader.TileMode.CLAMP
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0f, 0f, getWidth(), getHeight(), baseGradientPaint);
        if (!ambientOrbsEnabled || lavenderShader == null) {
            return;
        }
        drawOrb(canvas, lavenderShader, cx1, cy1, r1);
        drawOrb(canvas, pinkShader, cx2, cy2, r2);
        drawOrb(canvas, blueShader, cx3, cy3, r3);
        drawOrb(canvas, purpleShader, cx4, cy4, r4);
        drawOrb(canvas, whiteGlowShader, cx5, cy5, r5);
    }

    private void drawOrb(Canvas canvas, RadialGradient shader, float cx, float cy, float radius) {
        orbPaint.setShader(shader);
        canvas.drawCircle(cx, cy, radius, orbPaint);
    }
}
