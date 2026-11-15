package com.baibianyinzhang.app;

import java.util.ArrayList;
import java.util.List;

/**
 * 印章模板：形状 + 边框风格 + 文字排布方式。
 * 所有形状都由程序化矢量路径绘制，不依赖任何图片素材。
 */
public class SealTemplate {

    public enum Shape {
        CIRCLE,          // 正圆
        SQUARE,          // 正方
        ROUND_SQUARE,    // 圆角方
        ELLIPSE,         // 椭圆
        RECT_H,          // 横向长方（引首章）
        GOURD,           // 葫芦
        HEART,           // 心形
        OCTAGON,         // 八角
        FAN,             // 扇形
        WADANG,          // 瓦当（圆 + 十字）
        TIAN_GRID,       // 古玺田字格
        NONE             // 无边框
    }

    public enum Border {
        THIN,            // 细边
        THICK,           // 粗边
        DOUBLE,          // 双线
        HUIWEN,          // 回纹装饰
        NONE             // 无边框
    }

    public final String name;
    public final Shape shape;
    public final Border border;
    /** true = 文字横排（适合引首章、扇形），false = 传统竖排、从右往左 */
    public final boolean horizontal;
    /** true = 绘制田字格 / 瓦当十字分隔线 */
    public final boolean crossLines;

    public SealTemplate(String name, Shape shape, Border border,
                        boolean horizontal, boolean crossLines) {
        this.name = name;
        this.shape = shape;
        this.border = border;
        this.horizontal = horizontal;
        this.crossLines = crossLines;
    }

    /** 内置模板列表：覆盖不同形状与不同边框风格 */
    public static List<SealTemplate> defaults() {
        List<SealTemplate> list = new ArrayList<>();
        list.add(new SealTemplate("圆形·细边", Shape.CIRCLE, Border.THIN, false, false));
        list.add(new SealTemplate("圆形·双线", Shape.CIRCLE, Border.DOUBLE, false, false));
        list.add(new SealTemplate("圆形·回纹", Shape.CIRCLE, Border.HUIWEN, false, false));
        list.add(new SealTemplate("方形·粗边", Shape.SQUARE, Border.THICK, false, false));
        list.add(new SealTemplate("方形·细边", Shape.SQUARE, Border.THIN, false, false));
        list.add(new SealTemplate("圆角方·双线", Shape.ROUND_SQUARE, Border.DOUBLE, false, false));
        list.add(new SealTemplate("椭圆·细边", Shape.ELLIPSE, Border.THIN, false, false));
        list.add(new SealTemplate("长方·引首章", Shape.RECT_H, Border.THIN, true, false));
        list.add(new SealTemplate("古玺·田字格", Shape.TIAN_GRID, Border.THIN, false, true));
        list.add(new SealTemplate("瓦当·十字", Shape.WADANG, Border.THIN, false, true));
        list.add(new SealTemplate("葫芦形", Shape.GOURD, Border.THICK, false, false));
        list.add(new SealTemplate("心形", Shape.HEART, Border.THICK, false, false));
        list.add(new SealTemplate("八角形", Shape.OCTAGON, Border.THIN, false, false));
        list.add(new SealTemplate("扇形", Shape.FAN, Border.THIN, true, false));
        list.add(new SealTemplate("无边框·纯字", Shape.NONE, Border.NONE, false, false));
        return list;
    }
}
