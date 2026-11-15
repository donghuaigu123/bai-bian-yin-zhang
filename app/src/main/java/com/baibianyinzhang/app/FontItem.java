package com.baibianyinzhang.app;

/** 一款内置字体 */
public class FontItem {
    /** assets 下的相对路径 */
    public final String asset;
    /** 界面显示名 */
    public final String name;
    /** 书体分类 */
    public final String style;
    /** 字形覆盖有限（篆书、教育部隶书），缺字时自动回退到楷书 */
    public final boolean needFallback;

    public FontItem(String asset, String name, String style, boolean needFallback) {
        this.asset = asset;
        this.name = name;
        this.style = style;
        this.needFallback = needFallback;
    }
}
