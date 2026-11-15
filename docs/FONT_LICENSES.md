# 字体来源与授权

「百变印章」在 APK 中内置了 9 款中文字体。**应用源代码采用 MIT 许可，但字体
不适用 MIT**，每款字体均按其原始授权单独管理。完整授权文本放在
[`licenses/`](licenses/) 目录下，并随 APK / 源码仓库一同分发。

## 一览

| assets 文件 | 界面名称 | 书体 | 原始字体与获取来源 | 授权 | 本项目处理 |
| --- | --- | --- | --- | --- | --- |
| `kaishu.ttf` | 马善政楷书 | 楷书 | Ma Shan Zheng · Google Fonts（npm `@expo-google-fonts/ma-shan-zheng@0.4.3`） | SIL OFL 1.1 | 子集化 + 重命名 |
| `xingkai.ttf` | 龙藏行楷 | 行楷 | Long Cang · Google Fonts（`@expo-google-fonts/long-cang@0.4.1`） | SIL OFL 1.1 | 子集化 + 重命名 |
| `xingshu.ttf` | 志莽行书 | 行书 | Zhi Mang Xing · Google Fonts（`@expo-google-fonts/zhi-mang-xing@0.4.1`） | SIL OFL 1.1 | 子集化 + 重命名 |
| `caoshu.ttf` | 刘建毛草 | 草书 | Liu Jian Mao Cao · Google Fonts（`@expo-google-fonts/liu-jian-mao-cao@0.4.1`） | SIL OFL 1.1 | 子集化 + 重命名 |
| `songti.ttf` | 站酷小薇 | 宋韵 | ZCOOL XiaoWei · Google Fonts（`@expo-google-fonts/zcool-xiaowei@0.4.1`） | SIL OFL 1.1 | 子集化 + 重命名 |
| `heiti.ttf` | 庆科黄油 | 现代 | ZCOOL QingKe HuangYou · Google Fonts（`@expo-google-fonts/zcool-qingke-huangyou@0.4.1`） | SIL OFL 1.1 | 子集化 + 重命名 |
| `quwei.ttf` | 站酷快乐 | 趣味 | ZCOOL KuaiLe · Google Fonts（`@expo-google-fonts/zcool-kuaile@0.4.2`） | SIL OFL 1.1 | 子集化 + 重命名 |
| `lishu.ttf` | 青柳古隶 | 隶书 | 青柳隷書しも V2.01 · 青柳衡山挥毫 / SIMO 制作，[opentype.jp 官方发布页](https://opentype.jp/aoyagireisho.htm) | 作者自由授权（《フォントの使用方法》） | 保留完整字形 + 重命名；随附条款与解说原文 |
| `zhuanshu.ttf` | 说文小篆 | 篆书 | Shuowen Seal 印章专用子集 · [`LiaBrio/seal-fonts`](https://github.com/LiaBrio/seal-fonts) | SIL OFL 1.1 | WOFF → TTF 容器转换 + 重命名 |

## 1. SIL Open Font License 1.1 字体（8 款）

Google Fonts 的 7 款字体与 [`LiaBrio/seal-fonts`](https://github.com/LiaBrio/seal-fonts)
的说文小篆子集均采用 OFL 1.1。为控制 APK 体积，本项目用 `fontTools pyftsubset`
按「GB2312 简体字集 + 常用繁体姓名/吉祥语 + 标点」做了子集化，并按 OFL 的
Reserved Font Name 条款把字体内部名称统一改为 `BBYZ *`，保留 name 表中的版权
与许可声明（nameID 0 / 13 / 14）。

合规要点：

- OFL 全文随仓库分发：[`licenses/OFL-1.1.txt`](licenses/OFL-1.1.txt)、
  [`licenses/OFL-googlefonts.txt`](licenses/OFL-googlefonts.txt)
- 字体未单独出售，仅作为应用的一部分免费提供
- 衍生字体未使用原字体的保留名称
- 未改动任何字形轮廓（子集化只是删去未使用的字形）

说文小篆的字形来源与具名要求详见
[`licenses/NOTICE-seal-fonts.md`](licenses/NOTICE-seal-fonts.md)：其字形来自
开元小篆（OFL 1.1）与「全字庫說文解字」（OGDL-Taiwan-1.0）。本项目只做了
WOFF → TTF 的容器格式转换，未改动字形。

## 2. 青柳隷書しも（作者自由授权）

`lishu.ttf`（界面名「青柳古隶」）来自日本书法家**青柳衡山**先生挥毫、
**SIMO（しも）**先生制作的自由字体「青柳隷書しも」V2.01（JIS 第二水准版），
由 [opentype.jp](https://opentype.jp/aoyagireisho.htm) 官方发布。这是一款
**书写体古隶**：笔画带波磔、横向舒展、有金石手书味，与规整的碑刻汉隶风格
不同，用于闲章、落款尤具古意。

官方压缩包内《フォントの使用方法.txt》的授权条款原文（日文）摘录：

> 青柳隷書しもフォントは無料フォントです。使用にあたっての条件は何も
> ありません。商用使用もＯＫです。（できましたら連絡はください）
>
> 青柳隷書しもフォントは無料でどこにでも配布することができます。た
> だし、再配布にあたって有料とすることはできません。
>
> 青柳隷書しもフォントを配布する際には、必ずフォントの使用方法と、フ
> ォントの解説を一緒に配布するようにしてください。
>
> 使用や再配布にあたっての連絡は不要です…　　　SIMO

中文要点：

- **使用没有任何条件，允许商业使用**
- **可以免费在任何地方自由再分发**，唯一限制是再分发不得收费
- **分发字体时必须同时附上《使用方法》与《解说》两份原文**
- 使用与再分发均**无需事先联系作者**；条款未对改作设限

本项目的合规处理：

- 授权原文三份文件按**原始字节**保存，未转码、未改动：
  [`licenses/AoyagiReisho_terms_ja.txt`](licenses/AoyagiReisho_terms_ja.txt)、
  [`licenses/AoyagiReisho_explanation.pdf`](licenses/AoyagiReisho_explanation.pdf)、
  [`licenses/AoyagiReisho_explanation.doc`](licenses/AoyagiReisho_explanation.doc)
- 同样三份文件也放进 `app/src/main/assets/licenses/`，**随 APK 一起分发**，
  确保只拿到 APK 的用户也能看到条款与解说
- 本项目与 APK 均**免费提供**，不存在有偿再分发
- 字体**保留全部 14963 个字形**，仅把内部字体名改为 `BBYZ Lishu` 以与官方
  原版区分；字形轮廓与 glyf 表数据未作任何改动。该字体是老式 TrueType，
  `fontTools` 子集化重新编译 glyf 表会触发长度断言，因此不做子集化 ——
  这也顺带保留了大量繁体、异体字，更适合印章用字
- 在此具名标示原作者：书法 青柳衡山、字体制作 SIMO

> 注：这份授权不是 SIL OFL，而是版权人发布的自由使用声明。条款以随附的
> 日文原文为准，上面的中文为便于理解的转述，不构成法律意见。

## 3. 关于被替换 / 排除的字体

为「换掉禁止改作的 CC BY-ND 隶书」，调研过以下来源。它们均因授权或字形
不适合随开源仓库与 APK 再分发而未采用：

| 字体 | 授权 / 声明 | 未采用原因 |
| --- | --- | --- |
| 教育部隸書 TW-MOE-Li 3.0（**上一版所用**） | CC BY-ND 3.0 TW | 禁止改作，只能逐字节原样分发、无法子集化；且仅覆盖 45.5% 的 GB2312 简体字，已由青柳古隶替换 |
| 阿里妈妈刀隶体 | 阿里妈妈免费字体法律声明第 4 条 | 明确禁止「修改、重新分发、二次创作」以及「与其他软件产品组合等方式许可给第三方使用」 |
| 临海隶书 | 作者声明免费商用 | 明确「不允许将字体文件嵌入到软件、APP、网站或电子设备中作为内置字体使用」 |
| 王漢宗中隸書繁 | GNU GPL（王汉宗自由字型） | 2005 年文鼎科技主张侵权，双方和解后作者同意停止散布该批字型 |
| Yuji Syuku | SIL OFL 1.1 | 授权完全合规，但字形实为行楷而非隶书，不适合作为「隶书」交付 |
| 崇羲篆體 | © Academia Sinica, All Rights Reserved | 保留所有权利，非开源 |

## 4. 字形覆盖与缺字处理

| 字体 | 可用字形数 | 说明 |
| --- | --- | --- |
| Google Fonts 7 款 | 约 6900–7200 | 覆盖 GB2312 全部简体字 + 常用繁体 |
| 青柳古隶 | 14963（完整保留） | 以 JIS 第二水准汉字为主，命中约 65% 的 GB2312 简体字（优于上一版教育部隶书的 45.5%）；另含大量繁体与异体字 |
| 说文小篆 | 4752 | 《通用规范汉字表》一、二级字表中该字体已有的篆形 |

篆书与隶书遇到无字形的字时，App 会用 `Paint.hasGlyph()` 检测并**自动回退到
楷书**渲染该字，避免出现方框，同时在选字体时提示用户。印章常用字（吉祥语、
传统姓名用字）多为传承字形，青柳古隶覆盖较好；输入纯简体姓名时个别字回退
楷书属预期行为。
