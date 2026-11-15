#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
百变印章 · 字体准备脚本

输出 app/src/main/assets/fonts/ 下的 9 款内置字体：

1. Google Fonts 系 7 款（SIL OFL 1.1）：允许改作，按 GB2312 + 常用繁体字集
   做子集化，并按 OFL 的 Reserved Font Name 条款重命名。
2. 青柳隷書しも（作者自由授权）：官方《フォントの使用方法》明确「使用无任何
   条件、可商用、可免费在任何地方再分发」，因此可随 APK / 仓库自由分发。该授权
   要求随字体一并附上《使用方法》与《解説》原文，本脚本会把两份文件同时写入
   docs/licenses/ 与 app/src/main/assets/licenses/（后者随 APK 一并分发）。
   该字体是 2009 年的老式 TrueType，glyf 表尾部带额外数据，fontTools 子集化
   重新编译 glyf 时会触发 "too much glyf table data" 断言，因此**保留全部
   14963 个字形**，只改内部字体名，字形数据一个字节都不动。
3. 说文小篆（SIL OFL 1.1，LiaBrio/seal-fonts 的印章专用子集）：仅做
   WOFF -> TTF 的容器格式转换并重命名，不改动字形轮廓。

各字体完整来源、授权全文与处理方式见 docs/FONT_LICENSES.md。
"""

import io
import json
import os
import subprocess
import sys
import tarfile
import time
import urllib.request
import zipfile
from concurrent.futures import ThreadPoolExecutor

from fontTools import subset
from fontTools.ttLib import TTFont

ROOT = "/workspace/BaiBianYinZhang"
OUT = os.path.join(ROOT, "app", "src", "main", "assets", "fonts")
CACHE = "/root/.codebuddy/artifact/2f8a7d30-9c21-4f0b-1d72-b9e3a71de000/fonts_cache"
SEAL_WOFF = os.path.join(CACHE, "ShuowenSeal-Subset.woff")
os.makedirs(OUT, exist_ok=True)
os.makedirs(CACHE, exist_ok=True)

# 青柳隷書しも：官方发布页 https://opentype.jp/aoyagireisho.htm
AOYAGI_URL = "https://opentype.jp/bin/aoyagireisyosimo_ttf_2_01.zip"
AOYAGI_TTF = os.path.join(CACHE, "aoyagi_reisho.ttf")
AOYAGI_ZIP = os.path.join(CACHE, "aoyagireisho.zip")
LICENSE_DIRS = [
    os.path.join(ROOT, "docs", "licenses"),
    os.path.join(ROOT, "app", "src", "main", "assets", "licenses"),
]
# 授权要求随字体附带的两份原文（保持原始字节，不转码、不改名内容）
AOYAGI_DOCS = {
    ".txt": "AoyagiReisho_terms_ja.txt",     # フォントの使用方法（授权条款）
    ".pdf": "AoyagiReisho_explanation.pdf",  # フォントの解説
    ".doc": "AoyagiReisho_explanation.doc",  # フォントの解説（Word 版）
}
for _d in LICENSE_DIRS:
    os.makedirs(_d, exist_ok=True)

# (输出文件名, npm 包, 包内匹配关键字, 显示名, 风格, 内部字体名)
OFL_FONTS = [
    ("kaishu.ttf",   "@expo-google-fonts/ma-shan-zheng",         "", "马善政楷书", "楷书", "BBYZ Kaishu"),
    ("xingkai.ttf",  "@expo-google-fonts/long-cang",             "", "龙藏行楷",   "行楷", "BBYZ Xingkai"),
    ("xingshu.ttf",  "@expo-google-fonts/zhi-mang-xing",         "", "志莽行书",   "行书", "BBYZ Xingshu"),
    ("caoshu.ttf",   "@expo-google-fonts/liu-jian-mao-cao",      "", "刘建毛草",   "草书", "BBYZ Caoshu"),
    ("songti.ttf",   "@expo-google-fonts/zcool-xiaowei",         "", "站酷小薇",   "宋韵", "BBYZ Songti"),
    ("heiti.ttf",    "@expo-google-fonts/zcool-qingke-huangyou", "", "庆科黄油",   "现代", "BBYZ Heiti"),
    ("quwei.ttf",    "@expo-google-fonts/zcool-kuaile",          "", "站酷快乐",   "趣味", "BBYZ Quwei"),
]

# 常见繁体姓氏、吉祥语、书信用语，补足 GB2312 之外的姓名用字
EXTRA_TRADITIONAL = (
    "張劉陳楊黃趙吳周徐孫馬朱胡郭何高林羅鄭梁謝宋唐許韓馮鄧曹彭曾蕭田董袁潘于蔣蔡余杜葉程蘇魏呂丁任沈姚盧姜崔鐘譚陸汪范金石廖賈夏韋付方白鄒孟熊秦邱江尹薛閆段雷黎史龍陶賀顧毛郝龔邵萬錢嚴覃武戴莫孔向湯歐陽上官司馬諸葛東方"
    "中華民國萬歲龍鳳吉祥如意福祿壽喜財安樂康寧順利圓滿禎祥瑞氣書畫詩詞琴棋茶道雅集清風明月山川天地玄黃宇宙洪荒日月盈昃辰宿列張寒來暑往秋收冬藏金生麗水玉出崑岡劍號巨闕珠稱夜光果珍李柰菜重芥薑海鹹河淡鱗潛羽翔"
    "龍師火帝鳥官人皇始制文字乃服衣裳推位讓國有虞陶唐坐朝問道垂拱平章愛育黎首臣伏戎羌遐邇壹體率賓歸王鳴鳳在樹白駒食場化被草木賴及萬方蓋此身髮四大五常恭惟鞠養豈敢毀傷女慕貞潔男效才良知過必改得能莫忘罔談彼短靡恃己長"
    "信使可覆器欲難量墨悲絲染詩讚羔羊景行維賢克念作聖德建名立形端表正空谷傳聲虛堂習聽禍因惡積福緣善慶尺璧非寶寸陰是競資父事君曰嚴與敬孝當竭力忠則盡命臨深履薄夙興溫凊似蘭斯馨如松之盛川流不息淵澄取映容止若思言辭安定"
    "篤初誠美慎終宜令榮業所基籍甚無竟學優登仕攝職從政存以甘棠去而益詠樂殊貴賤禮別尊卑上和下睦夫唱婦隨外受傅訓入奉母儀諸姑伯叔猶子比兒孔懷兄弟同氣連枝交友投分切磨箴規仁慈隱惻造次弗離節義廉退顛沛匪虧性靜情逸心動神疲"
    "守真志滿逐物意移堅持雅操好爵自縻都邑華夏東西二京背邙面洛浮渭據涇宮殿盤鬱樓觀飛驚圖寫禽獸畫彩仙靈肆筵設席鼓瑟吹笙升階納陛弁轉疑星右通廣內左達承明既集墳典亦聚群英府羅將相路俠槐卿戶封八縣家給千兵高冠陪輦驅轂振纓"
    "世祿侈富車駕肥輕策功茂實勒碑刻銘磻溪伊尹佐時阿衡奄宅曲阜微旦孰營桓公匡合濟弱扶傾綺回漢惠說感武丁俊乂密勿多士寔寧晉楚更霸趙魏困橫假途滅虢踐土會盟何遵約法韓弊煩刑起翦頗牧用軍最精宣威沙漠馳譽丹青九州禹跡百郡秦并"
    "嶽宗泰岱禪主云亭雁門紫塞雞田赤城昆池碣石鉅野洞庭曠遠綿邈巖岫杳冥治本於農務茲稼穡俶載南畝我藝黍稷稅熟貢新勸賞黜陟孟軻敦素史魚秉直庶幾中庸勞謙謹敕聆音察理鑑貌辨色貽厥嘉猷勉其祗植省躬譏誡寵增抗極殆辱近恥林皋幸即"
    "兩疏見機解組誰逼索居閒處沉默寂寥求古尋論散慮逍遙欣奏累遣慼謝歡招渠荷的歷園莽抽條枇杷晚翠梧桐蚤凋陳根委翳落葉飄颻遊鵾獨運凌摩絳霄耽讀翫市寓目囊箱易輶攸畏屬耳垣牆具膳餐飯適口充腸飽飫烹宰飢厭糟糠親戚故舊老少異糧"
    "妾御績紡侍巾帷房紈扇圓潔銀燭煒煌晝眠夕寐藍筍象床弦歌酒讌接杯舉觴矯手頓足悅豫且康嫡後嗣續祭祀烝嘗稽顙再拜悚懼恐惶箋牒簡要顧答審詳骸垢想浴執熱願涼驢騾犢特駭躍超驤誅斬賊盜捕獲叛亡布射僚丸嵇琴阮嘯恬筆倫紙鈞巧任釣"
    "釋紛利俗並皆佳妙毛施淑姿工顰妍笑年矢每催曦暉朗曜璿璣懸斡晦魄環照指薪修祜永綏吉劭矩步引領俯仰廊廟束帶矜莊徘徊瞻眺孤陋寡聞愚蒙等誚謂語助者焉哉乎也"
)

PUNCTUATION = (
    "　、。，．·；：？！…—～「」『』（）【】《》〈〉〖〗〔〕｛｝"
    "％＋－×÷＝°℃￥§◎○●△▲☆★◇◆□■※〃〇﹁﹂﹃﹄"
    "“”‘’《》〈〉·※々〆〇－—…"
)


def build_codepoints():
    cps = set()
    for code in range(0x20, 0x7F):          # ASCII 可见字符
        cps.add(code)
    for code in range(0x4E00, 0x9FA6):     # GB2312 覆盖的 6763 个简体汉字
        ch = chr(code)
        try:
            ch.encode("gb2312")
            cps.add(code)
        except UnicodeEncodeError:
            pass
    for ch in EXTRA_TRADITIONAL + PUNCTUATION:
        cps.add(ord(ch))
    cps.update([0x00A0, 0x3000, 0xFF01, 0xFF0C, 0xFF0E, 0xFF1A, 0xFF1B, 0xFF1F])
    return sorted(cps)


def npm_tarball(pkg):
    req = urllib.request.Request("https://registry.npmjs.org/" + pkg,
                                 headers={"User-Agent": "bbyz-font-prep/1.0"})
    with urllib.request.urlopen(req, timeout=40) as resp:
        meta = json.load(resp)
    version = meta["dist-tags"]["latest"]
    return meta["versions"][version]["dist"]["tarball"], version


def fetch_tarball(pkg):
    url, version = npm_tarball(pkg)
    cache = os.path.join(CACHE, pkg.replace("/", "__") + "-" + version + ".tgz")
    if os.path.exists(cache) and os.path.getsize(cache) > 10000:
        return cache, version
    last_err = None
    for _ in range(3):
        try:
            req = urllib.request.Request(url, headers={"User-Agent": "bbyz-font-prep/1.0"})
            with urllib.request.urlopen(req, timeout=180) as resp:
                data = resp.read()
            with open(cache, "wb") as fh:
                fh.write(data)
            return cache, version
        except Exception as exc:      # noqa: BLE001
            last_err = exc
            time.sleep(2)
    raise RuntimeError("下载失败 %s: %s" % (pkg, last_err))


def extract_font(pkg, match):
    path, version = fetch_tarball(pkg)
    with tarfile.open(path, "r:gz") as tf:
        members = [
            m for m in tf.getmembers()
            if m.isfile() and m.name.lower().endswith((".ttf", ".otf"))
            and (not match or match in m.name)
        ]
        if not members:
            raise RuntimeError("包内未找到字体文件: %s" % pkg)
        target = members[0]
        return tf.extractfile(target).read(), target.name, version


def rename_font(font, internal_name):
    """重命名内部字体名，同时保留版权与许可声明（nameID 0 / 13 / 14）"""
    name_table = font["name"]
    name_table.names = [r for r in name_table.names if r.nameID not in (1, 4, 6, 16, 17)]
    postscript = internal_name.replace(" ", "")
    for name_id, value in ((1, internal_name), (4, internal_name), (6, postscript)):
        name_table.setName(value, name_id, 3, 1, 0x409)
        name_table.setName(value, name_id, 1, 0, 0)


def subset_font(data, out_path, internal_name, codepoints):
    font = TTFont(io.BytesIO(data))
    if font.flavor:
        font.flavor = None
    opts = subset.Options()
    opts.name_IDs = ["*"]
    opts.name_legacy = True
    opts.name_languages = ["*"]
    opts.layout_features = ["*"]
    opts.notdef_outline = True
    opts.recalc_bounds = True
    subsetter = subset.Subsetter(options=opts)
    subsetter.populate(unicodes=codepoints)
    subsetter.subset(font)
    rename_font(font, internal_name)
    font.save(out_path)
    return len(font.getBestCmap())


def convert_woff_to_ttf(woff_path, out_path, internal_name):
    """仅做 WOFF -> TTF 容器转换，不改动字形轮廓"""
    font = TTFont(woff_path)
    font.flavor = None
    rename_font(font, internal_name)
    font.save(out_path)
    return len(font.getBestCmap())


def rename_only_font(data, out_path, internal_name):
    """保留全部字形，仅重写 name 表；用于不宜子集化的老式 TrueType 字体。

    recalcBBoxes=False 避免 fontTools 为了重算边界而反编译 glyf 表；
    未加载的表会由 SFNTReader 原样复制，字形与轮廓完全不变。
    """
    font = TTFont(io.BytesIO(data), recalcBBoxes=False)
    rename_font(font, internal_name)
    font.save(out_path)
    return len(font.getBestCmap())


def aoyagi_docs_ready():
    return all(
        os.path.exists(os.path.join(d, name)) and os.path.getsize(os.path.join(d, name)) > 100
        for d in LICENSE_DIRS for name in AOYAGI_DOCS.values()
    )


def curl_bytes(args, timeout=200):
    return subprocess.run(
        ["curl", "-sL", "-A", "Mozilla/5.0"] + args,
        capture_output=True, timeout=timeout,
    )


def download_aoyagi():
    """从官方发布页下载青柳隷書しも V2.01 的 TTF 与授权原文。

    该站点单连接会被限速截断，因此用 HTTP Range 分 8 段并行下载后拼接。
    """
    if (os.path.exists(AOYAGI_TTF) and os.path.getsize(AOYAGI_TTF) > 1_000_000
            and aoyagi_docs_ready()):
        print("青柳隷書：已缓存 TTF 与授权原文，跳过下载", flush=True)
        return AOYAGI_TTF

    head = curl_bytes(["-I", "--max-time", "30", AOYAGI_URL], 45).stdout.decode("latin1")
    lengths = [l for l in head.splitlines() if l.lower().startswith("content-length:")]
    if not lengths:
        raise RuntimeError("无法取得青柳隷書压缩包大小")
    total = int(lengths[-1].split(":", 1)[1].strip())
    print("青柳隷書：分段下载 %.2f MB ..." % (total / 1048576.0), flush=True)

    parts = 8
    step = (total + parts - 1) // parts

    def fetch_part(i):
        start = i * step
        end = min(total - 1, (i + 1) * step - 1)
        path = os.path.join(CACHE, "aoyagi_part_%d" % i)
        for _ in range(4):
            curl_bytes(["--retry", "2", "--max-time", "180",
                        "-r", "%d-%d" % (start, end), "-o", path, AOYAGI_URL])
            if os.path.exists(path) and os.path.getsize(path) == end - start + 1:
                return i, True
            time.sleep(1)
        return i, False

    with ThreadPoolExecutor(max_workers=parts) as pool:
        result = list(pool.map(fetch_part, range(parts)))
    if not all(ok for _, ok in result):
        raise RuntimeError("青柳隷書分段下载失败：%s" % result)
    with open(AOYAGI_ZIP, "wb") as writer:
        for i in range(parts):
            with open(os.path.join(CACHE, "aoyagi_part_%d" % i), "rb") as reader:
                writer.write(reader.read())

    with zipfile.ZipFile(AOYAGI_ZIP) as zf:
        for info in zf.infolist():
            data = zf.read(info)
            low = info.filename.lower()
            if low.endswith(".ttf"):
                with open(AOYAGI_TTF, "wb") as fh:
                    fh.write(data)
            else:
                for ext, name in AOYAGI_DOCS.items():
                    if low.endswith(ext):
                        for d in LICENSE_DIRS:
                            with open(os.path.join(d, name), "wb") as fh:
                                fh.write(data)   # 原始字节，不转码
                        break
    print("青柳隷書：TTF 与授权原文已就位", flush=True)
    return AOYAGI_TTF


def record(manifest, file_name, display, style, source, detail, size_kb, glyphs):
    manifest.append({
        "file": file_name, "name": display, "style": style,
        "source": source, "detail": detail,
        "size_kb": size_kb, "glyphs": glyphs,
    })
    print("OK   %-16s %-10s %-8s %5d KB  字形 %5d  ← %s" %
          (file_name, display, style, size_kb, glyphs, source), flush=True)


def main():
    # 清理上一次的输出，避免残留已弃用的字体
    for f in os.listdir(OUT):
        if f.lower().endswith((".ttf", ".otf")):
            os.remove(os.path.join(OUT, f))

    codepoints = build_codepoints()
    print("目标字集: %d 个码位" % len(codepoints), flush=True)
    manifest = []

    # 1) Google Fonts 系 OFL 字体：子集化 + 重命名
    for out_name, pkg, match, display, style, internal in OFL_FONTS:
        try:
            data, member, version = extract_font(pkg, match)
            out_path = os.path.join(OUT, out_name)
            count = subset_font(data, out_path, internal, codepoints)
            record(manifest, out_name, display, style, pkg + "@" + version,
                   "OFL 1.1 / 子集化 + 重命名",
                   os.path.getsize(out_path) // 1024, count)
        except Exception as exc:      # noqa: BLE001
            print("FAIL %-16s %s" % (out_name, exc), flush=True)
            manifest.append({"file": out_name, "error": str(exc)})

    # 2) 青柳隷書しも：作者自由授权，保留完整字形 + 重命名，并随附授权原文
    try:
        ttf_path = download_aoyagi()
        with open(ttf_path, "rb") as fh:
            data = fh.read()
        out_path = os.path.join(OUT, "lishu.ttf")
        count = rename_only_font(data, out_path, "BBYZ Lishu")
        record(manifest, "lishu.ttf", "青柳古隶", "隶书",
               "opentype.jp / 青柳隷書しも V2.01",
               "作者自由授权 / 保留完整字形 + 重命名 / 已随附条款与解说原文",
               os.path.getsize(out_path) // 1024, count)
    except Exception as exc:          # noqa: BLE001
        print("FAIL lishu.ttf %s" % exc, flush=True)
        manifest.append({"file": "lishu.ttf", "error": str(exc)})

    # 3) 说文小篆：OFL 1.1，WOFF -> TTF 格式转换
    try:
        if not os.path.exists(SEAL_WOFF):
            raise RuntimeError("缺少 %s，请先下载 LiaBrio/seal-fonts 的 ShuowenSeal-Subset.woff" % SEAL_WOFF)
        out_path = os.path.join(OUT, "zhuanshu.ttf")
        count = convert_woff_to_ttf(SEAL_WOFF, out_path, "BBYZ Seal Script")
        record(manifest, "zhuanshu.ttf", "说文小篆", "篆书",
               "LiaBrio/seal-fonts (ShuowenSeal-Subset.woff)",
               "OFL 1.1 / WOFF→TTF 容器转换 + 重命名",
               os.path.getsize(out_path) // 1024, count)
    except Exception as exc:          # noqa: BLE001
        print("FAIL zhuanshu.ttf %s" % exc, flush=True)
        manifest.append({"file": "zhuanshu.ttf", "error": str(exc)})

    with open(os.path.join(CACHE, "fonts_manifest.json"), "w", encoding="utf-8") as fh:
        json.dump(manifest, fh, ensure_ascii=False, indent=2)

    ok = [m for m in manifest if "error" not in m]
    expected = len(OFL_FONTS) + 2
    print("\n完成: %d/%d 款字体，合计 %.1f MB" %
          (len(ok), expected, sum(m["size_kb"] for m in ok) / 1024.0), flush=True)
    return 0 if len(ok) == expected else 1


if __name__ == "__main__":
    sys.exit(main())
