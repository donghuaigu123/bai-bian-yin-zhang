#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 App 启动图标与 README 展示图。

图片全部由脚本用项目内置的书法字体绘制，不依赖任何外部素材，
保证仓库里的展示效果与 APK 内置字体一致。
"""

import os

from PIL import Image, ImageDraw, ImageFont

ROOT = "/workspace/BaiBianYinZhang"
FONT_DIR = os.path.join(ROOT, "app", "src", "main", "assets", "fonts")
RED = (200, 40, 46, 255)
WHITE = (255, 255, 255, 255)
PAPER = (245, 242, 236, 255)
INK = (90, 85, 78, 255)


def load_font(name, size):
    return ImageFont.truetype(os.path.join(FONT_DIR, name), size)


def make_icon():
    size = 192
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rounded_rectangle([0, 0, size - 1, size - 1], radius=42, fill=RED)
    d.ellipse([26, 26, size - 26, size - 26], outline=WHITE, width=9)
    d.text((size // 2, size // 2 + 4), "印", font=load_font("kaishu.ttf", 88),
           fill=WHITE, anchor="mm")
    out = os.path.join(ROOT, "app", "src", "main", "res", "mipmap-xxxhdpi", "ic_launcher.png")
    img.save(out)
    print("图标 ->", out)


def draw_seal(size, shape, font_name, text):
    """画一枚白底红字印章（与 Android 端同一套排布思路）"""
    img = Image.new("RGB", (size, size), (255, 255, 255))
    d = ImageDraw.Draw(img)
    red = (200, 40, 46)
    m = int(size * 0.06)

    if shape == "circle":
        d.ellipse([m, m, size - m, size - m], outline=red, width=max(2, int(size * 0.024)))
        inner = [int(size * 0.19), int(size * 0.19), int(size * 0.81), int(size * 0.81)]
    elif shape == "square":
        d.rectangle([m, m, size - m, size - m], outline=red, width=max(2, int(size * 0.048)))
        inner = [int(size * 0.15), int(size * 0.15), int(size * 0.85), int(size * 0.85)]
    elif shape == "double":
        d.rectangle([m, m, size - m, size - m], outline=red, width=max(2, int(size * 0.024)))
        k = int(size * 0.10)
        d.rectangle([m + k, m + k, size - m - k, size - m - k],
                    outline=red, width=max(1, int(size * 0.010)))
        inner = [int(size * 0.20), int(size * 0.20), int(size * 0.80), int(size * 0.80)]
    else:  # none
        inner = [int(size * 0.12), int(size * 0.12), int(size * 0.88), int(size * 0.88)]

    n = len(text)
    cols = 1 if n == 1 else 2
    rows = (n + cols - 1) // cols
    cw = (inner[2] - inner[0]) / cols
    chh = (inner[3] - inner[1]) / rows
    f = load_font(font_name, int(min(cw, chh) * 0.80))

    # 与 Android 端一致：按输入顺序从左到右、从上到下，末行居中
    remaining = n
    index = 0
    for row in range(rows):
        count = min(cols, remaining)
        remaining -= count
        row_width = cw * count
        left = (inner[0] + inner[2] - row_width) / 2
        y = inner[1] + chh * (row + 0.5)
        for col in range(count):
            ch = text[index]
            index += 1
            x = left + cw * (col + 0.5)
            d.text((x, y), ch, font=f, fill=red, anchor="mm")
    return img


def make_preview():
    width, height = 1180, 560
    img = Image.new("RGB", (width, height), PAPER[:3])
    d = ImageDraw.Draw(img)
    d.text((58, 40), "百变印章", font=load_font("kaishu.ttf", 62), fill=RED[:3])
    d.text((62, 124), "15 种形状 / 9 款书法字体 / 打字即出白底红字高清印章",
           font=load_font("songti.ttf", 23), fill=INK[:3])
    d.line([58, 172, width - 58, 172], fill=(222, 214, 202), width=2)

    samples = [
        ("circle", "kaishu.ttf", "平安喜乐", "圆形 / 楷书"),
        ("square", "lishu.ttf", "平安", "方形 / 隶书"),
        ("double", "zhuanshu.ttf", "福", "双线 / 篆书"),
        ("none", "caoshu.ttf", "墨韵", "无边框 / 草书"),
    ]
    size = 246
    gap = 26
    x0, y0 = 58, 210
    label_font = load_font("heiti.ttf", 19)
    for i, (shape, font_name, text, label) in enumerate(samples):
        x = x0 + i * (size + gap)
        seal = draw_seal(size, shape, font_name, text)
        d.rectangle([x - 8, y0 - 8, x + size + 8, y0 + size + 8],
                    fill=(255, 255, 255), outline=(226, 219, 207), width=2)
        img.paste(seal, (x, y0))
        d.text((x + size / 2, y0 + size + 32), label,
               font=label_font, fill=INK[:3], anchor="mm")

    out = os.path.join(ROOT, "docs", "preview.png")
    img.save(out)
    print("展示图 ->", out)


if __name__ == "__main__":
    make_icon()
    make_preview()
