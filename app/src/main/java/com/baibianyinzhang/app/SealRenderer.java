package com.baibianyinzhang.app;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.os.Build;

/**
 * 印章绘制引擎。
 *
 * 采用 1000 x 1000 的设计坐标系，实际输出时按目标像素缩放，
 * 因此同一套代码既能画列表缩略图，也能画 1600px 的高清成品图。
 * 背景恒为纯白，边框与文字恒为朱红，符合传统朱文印的效果。
 */
public final class SealRenderer {

    /** 经典印泥红 */
    public static final int SEAL_RED = 0xFFC8282E;

    private static final float DESIGN = 1000f;
    private static final RectF BASE = new RectF(60f, 60f, 940f, 940f);

    private SealRenderer() {
    }

    /** 生成印章图：白底红字，正方形 PNG 位图 */
    public static Bitmap render(SealTemplate tpl, Typeface font, Typeface fallback,
                                String rawText, int px) {
        Bitmap bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        canvas.save();
        canvas.scale(px / DESIGN, px / DESIGN);
        drawFrame(canvas, tpl);
        drawText(canvas, tpl, font, fallback, sanitize(rawText));
        canvas.restore();
        return bitmap;
    }

    /** 生成字体选择卡片上的单字样例 */
    public static Bitmap renderFontSample(Typeface font, String ch, int px) {
        Bitmap bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(SEAL_RED);
        paint.setTypeface(font);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(px * 0.66f);
        float baseline = px / 2f - (paint.ascent() + paint.descent()) / 2f;
        canvas.drawText(ch, px / 2f, baseline, paint);
        return bitmap;
    }

    /** 去掉空白、限制长度，空内容用一个「印」字占位 */
    public static String sanitize(String raw) {
        if (raw == null) {
            return "印";
        }
        String s = raw.replaceAll("\\s+", "").trim();
        if (s.isEmpty()) {
            return "印";
        }
        if (s.length() > 12) {
            s = s.substring(0, 12);
        }
        return s;
    }

    // ------------------------------------------------------------------ 边框

    private static void drawFrame(Canvas c, SealTemplate t) {
        if (t.border == SealTemplate.Border.NONE) {
            return;
        }

        Paint outer = new Paint(Paint.ANTI_ALIAS_FLAG);
        outer.setStyle(Paint.Style.STROKE);
        outer.setColor(SEAL_RED);
        outer.setStrokeWidth(strokeWidth(t.border));
        outer.setStrokeJoin(Paint.Join.ROUND);
        outer.setStrokeCap(Paint.Cap.ROUND);

        Path outline = outline(t.shape);
        if (outline != null) {
            c.drawPath(outline, outer);
        }

        if (t.border == SealTemplate.Border.DOUBLE) {
            Paint inner = new Paint(outer);
            inner.setStrokeWidth(11f);
            Path inset = insetOutline(outline, 46f);
            if (inset != null) {
                c.drawPath(inset, inner);
            }
        } else if (t.border == SealTemplate.Border.HUIWEN) {
            drawHuiwen(c, t.shape);
        }

        if (t.crossLines) {
            drawCrossLines(c, t.shape);
        }
    }

    private static float strokeWidth(SealTemplate.Border border) {
        switch (border) {
            case THICK:
                return 54f;
            case DOUBLE:
                return 24f;
            case HUIWEN:
                return 34f;
            case THIN:
            default:
                return 22f;
        }
    }

    /** 各形状的外轮廓路径（设计坐标 1000×1000） */
    private static Path outline(SealTemplate.Shape s) {
        Path p = new Path();
        switch (s) {
            case CIRCLE:
            case WADANG:
                p.addCircle(500f, 500f, 440f, Path.Direction.CW);
                break;
            case SQUARE:
            case TIAN_GRID:
                p.addRect(BASE, Path.Direction.CW);
                break;
            case ROUND_SQUARE:
                p.addRoundRect(BASE, 105f, 105f, Path.Direction.CW);
                break;
            case ELLIPSE:
                p.addOval(new RectF(60f, 170f, 940f, 830f), Path.Direction.CW);
                break;
            case RECT_H:
                p.addRect(new RectF(60f, 260f, 940f, 740f), Path.Direction.CW);
                break;
            case GOURD:
                p.moveTo(500f, 62f);
                p.cubicTo(392f, 62f, 332f, 150f, 332f, 232f);
                p.cubicTo(332f, 305f, 410f, 330f, 410f, 388f);
                p.cubicTo(298f, 420f, 218f, 512f, 218f, 642f);
                p.cubicTo(218f, 812f, 356f, 938f, 500f, 938f);
                p.cubicTo(644f, 938f, 782f, 812f, 782f, 642f);
                p.cubicTo(782f, 512f, 702f, 420f, 590f, 388f);
                p.cubicTo(590f, 330f, 668f, 305f, 668f, 232f);
                p.cubicTo(668f, 150f, 608f, 62f, 500f, 62f);
                p.close();
                break;
            case HEART:
                p.moveTo(500f, 890f);
                p.cubicTo(300f, 740f, 100f, 575f, 100f, 375f);
                p.cubicTo(100f, 212f, 228f, 112f, 358f, 112f);
                p.cubicTo(432f, 112f, 478f, 152f, 500f, 205f);
                p.cubicTo(522f, 152f, 568f, 112f, 642f, 112f);
                p.cubicTo(772f, 112f, 900f, 212f, 900f, 375f);
                p.cubicTo(900f, 575f, 700f, 740f, 500f, 890f);
                p.close();
                break;
            case OCTAGON:
                p.moveTo(325f, 60f);
                p.lineTo(675f, 60f);
                p.lineTo(940f, 325f);
                p.lineTo(940f, 675f);
                p.lineTo(675f, 940f);
                p.lineTo(325f, 940f);
                p.lineTo(60f, 675f);
                p.lineTo(60f, 325f);
                p.close();
                break;
            case FAN:
                p.moveTo(175f, 772f);
                p.lineTo(248f, 302f);
                p.quadTo(500f, 128f, 752f, 302f);
                p.lineTo(825f, 772f);
                p.quadTo(500f, 668f, 175f, 772f);
                p.close();
                break;
            case NONE:
            default:
                return null;
        }
        return p;
    }

    /** 把外轮廓等比内缩，用作双线内框 */
    private static Path insetOutline(Path outline, float inset) {
        if (outline == null) {
            return null;
        }
        Path copy = new Path(outline);
        float factor = (880f - 2f * inset) / 880f;
        Matrix matrix = new Matrix();
        matrix.setScale(factor, factor, 500f, 500f);
        copy.transform(matrix);
        return copy;
    }

    /** 回纹风格：内圈细线 + 四方小印点 */
    private static void drawHuiwen(Canvas c, SealTemplate.Shape s) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(SEAL_RED);
        p.setStrokeWidth(13f);
        if (s == SealTemplate.Shape.CIRCLE || s == SealTemplate.Shape.WADANG) {
            c.drawCircle(500f, 500f, 392f, p);
        } else {
            c.drawRect(new RectF(108f, 108f, 892f, 892f), p);
        }
        Paint dot = new Paint(Paint.ANTI_ALIAS_FLAG);
        dot.setStyle(Paint.Style.FILL);
        dot.setColor(SEAL_RED);
        c.drawRect(484f, 92f, 516f, 124f, dot);
        c.drawRect(484f, 876f, 516f, 908f, dot);
        c.drawRect(92f, 484f, 124f, 516f, dot);
        c.drawRect(876f, 484f, 908f, 516f, dot);
    }

    /** 田字格 / 瓦当十字分隔线 */
    private static void drawCrossLines(Canvas c, SealTemplate.Shape s) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(SEAL_RED);
        p.setStrokeWidth(13f);
        if (s == SealTemplate.Shape.WADANG) {
            c.drawLine(500f, 108f, 500f, 892f, p);
            c.drawLine(108f, 500f, 892f, 500f, p);
            c.drawCircle(500f, 500f, 82f, p);
        } else {
            c.drawLine(500f, 66f, 500f, 934f, p);
            c.drawLine(66f, 500f, 934f, 500f, p);
        }
    }

    /** 每个形状的文字安全区，避开边框 */
    private static RectF innerRect(SealTemplate.Shape s) {
        switch (s) {
            case CIRCLE:
            case WADANG:
                return new RectF(192f, 192f, 808f, 808f);
            case SQUARE:
            case TIAN_GRID:
                return new RectF(150f, 150f, 850f, 850f);
            case ROUND_SQUARE:
                return new RectF(172f, 172f, 828f, 828f);
            case ELLIPSE:
                return new RectF(180f, 272f, 820f, 728f);
            case RECT_H:
                return new RectF(122f, 316f, 878f, 684f);
            case GOURD:
                return new RectF(308f, 432f, 692f, 856f);
            case HEART:
                return new RectF(248f, 318f, 752f, 700f);
            case OCTAGON:
                return new RectF(220f, 220f, 780f, 780f);
            case FAN:
                return new RectF(252f, 336f, 748f, 656f);
            case NONE:
            default:
                return new RectF(112f, 112f, 888f, 888f);
        }
    }

    // ------------------------------------------------------------------ 文字

    private static void drawText(Canvas c, SealTemplate t, Typeface font,
                                Typeface fallback, String text) {
        RectF area = innerRect(t.shape);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(SEAL_RED);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTypeface(font);

        int n = text.length();

        if (t.horizontal) {
            // 横排：等分宽度，单字居中
            float cell = area.width() / n;
            float size = Math.min(area.height() * 0.84f, cell * 0.92f);
            paint.setTextSize(size);
            float centerY = (area.top + area.bottom) / 2f;
            for (int i = 0; i < n; i++) {
                char ch = text.charAt(i);
                applyGlyphFont(paint, font, fallback, ch);
                float x = area.left + cell * (i + 0.5f);
                float baseline = centerY - (paint.ascent() + paint.descent()) / 2f;
                c.drawText(String.valueOf(ch), x, baseline, paint);
            }
            return;
        }

        // 易读排布：按输入顺序从左到右、从上到下铺字，末行自动水平居中。
        // 采用现代阅读顺序而非传统右起，避免「张三」被看成「三张」。
        int cols = (n <= 1) ? 1 : (n <= 8 ? 2 : 3);
        int rows = (n + cols - 1) / cols;
        float cellW = area.width() / cols;
        float cellH = area.height() / rows;
        float size = Math.min(cellW, cellH) * 0.86f;
        paint.setTextSize(size);

        int remaining = n;
        int index = 0;
        for (int row = 0; row < rows; row++) {
            int count = Math.min(cols, remaining);
            remaining -= count;
            float rowWidth = cellW * count;
            float left = (area.left + area.right - rowWidth) / 2f;
            float centerY = area.top + cellH * (row + 0.5f);
            for (int col = 0; col < count; col++) {
                char ch = text.charAt(index++);
                float x = left + cellW * (col + 0.5f);
                applyGlyphFont(paint, font, fallback, ch);
                float baseline = centerY - (paint.ascent() + paint.descent()) / 2f;
                c.drawText(String.valueOf(ch), x, baseline, paint);
            }
        }
    }

    /** 篆书等字形覆盖有限时，缺字自动改用楷书，避免出现豆腐块 */
    private static void applyGlyphFont(Paint paint, Typeface font,
                                      Typeface fallback, char ch) {
        paint.setTypeface(font);
        if (fallback != null && Build.VERSION.SDK_INT >= 23
                && !paint.hasGlyph(String.valueOf(ch))) {
            paint.setTypeface(fallback);
        }
    }
}
