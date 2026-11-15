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
| `lishu.ttf` | 教育部隶书 | 隶书 | 教育部隸書 / TW-MOE-Li 3.0 · 台湾地区教育主管部门（npm `@fontpkg/tw-moe-li@3.0.0`） | CC 姓名标示–禁止改作 3.0 台湾版（CC BY-ND 3.0 TW） | **逐字节原样复制，未做任何修改** |
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

## 2. 教育部隶书（CC BY-ND 3.0 TW）

字体 name 表中的版权字段明确声明该字型档采用「创用 CC 姓名标示–禁止改作 3.0
台湾版」授权，并写明「允许商业与非商业的再散布，条件是**原样、完整传递**并
标示原始发布单位」。

因此本项目对 `lishu.ttf` 采取最保守的处理方式：

- **不子集化、不重命名、不改动任何一个字节**，与 npm 包内原始 TTF 完全一致
- 随 APK 与仓库原样分发
- 在此具名标示：字体为台湾地区教育主管部门「教育部隸書（TW-MOE-Li）」3.0 版，
  依 CC BY-ND 3.0 TW 使用；本项目未创作该字体的衍生/改作版本

> 授权参考：Creative Commons Attribution-NoDerivs 3.0 Taiwan
> <https://creativecommons.org/licenses/by-nd/3.0/tw/>

## 3. 关于被排除的字体

调研过程中曾考虑但**最终未采用**以下字体，原因是授权不适合开源再分发：

| 字体 | 版权声明 | 未采用原因 |
| --- | --- | --- |
| 崇羲篆體（Chong Xi Small Seal） | `Typeface © Academia Sinica. 2022. All Rights Reserved` | 保留所有权利，非开源 |
| 阿里妈妈刀隶体 | `Copyright (c) 2023 Alibaba (China) Co., Ltd. All rights reserved.` | 保留所有权利 |
| 临海隶书 | 无明确授权声明 | 授权不明 |

## 4. 字形覆盖与缺字处理

| 字体 | 可用字形数 | 说明 |
| --- | --- | --- |
| Google Fonts 7 款 | 约 6900–7200 | 覆盖 GB2312 全部简体字 + 常用繁体 |
| 教育部隶书 | 约 5590 | 以台湾标准字集为主，部分简体字无字形 |
| 说文小篆 | 4752 | 《通用规范汉字表》一、二级字表中该字体已有的篆形 |

篆书与隶书遇到无字形的字时，App 会用 `Paint.hasGlyph()` 检测并**自动回退到
楷书**渲染该字，避免出现方框，同时在选字体时提示用户。
