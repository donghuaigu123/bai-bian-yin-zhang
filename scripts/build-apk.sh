#!/usr/bin/env bash
# ==============================================================
# 百变印章 · 纯命令行构建脚本
#
# 不依赖 Android Studio / Gradle / Maven / 任何第三方库，
# 直接用 Android SDK build-tools 的 aapt2 + javac + d8 +
# zipalign + apksigner 产出可安装的签名 APK。
#
# 用法：
#   export ANDROID_HOME=/path/to/Android/Sdk   # 可选，默认 /opt/android-sdk
#   bash scripts/build-apk.sh
# ==============================================================
set -euo pipefail

SDK="${ANDROID_HOME:-/opt/android-sdk}"
BT="$SDK/build-tools/34.0.0"
PLATFORM="$SDK/platforms/android-34/android.jar"
PROJ="$(cd "$(dirname "$0")/.." && pwd)"
BUILD="$PROJ/build"
APK_NAME="百变印章.apk"

export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
export PATH="$JAVA_HOME/bin:$PATH"

for required in "$BT/aapt2" "$BT/d8" "$BT/zipalign" "$BT/apksigner" "$PLATFORM"; do
    if [ ! -e "$required" ]; then
        echo "缺少构建工具：$required"
        echo "请先安装 Android SDK：platforms;android-34 与 build-tools;34.0.0"
        exit 1
    fi
done

FONT_COUNT=$(find "$PROJ/app/src/main/assets/fonts" -type f \( -name '*.ttf' -o -name '*.otf' \) | wc -l)
if [ "$FONT_COUNT" -lt 9 ]; then
    echo "内置字体不足（当前 $FONT_COUNT 款），请先运行：python3 scripts/prepare_fonts.py"
    exit 1
fi

rm -rf "$BUILD"
mkdir -p "$BUILD/gen" "$BUILD/classes" "$BUILD/dex" "$BUILD/staging"

echo "[1/7] 编译资源"
"$BT/aapt2" compile --dir "$PROJ/app/src/main/res" -o "$BUILD/res.zip"

echo "[2/7] 链接资源与 Manifest，生成 R.java"
"$BT/aapt2" link -o "$BUILD/base.apk" \
    -I "$PLATFORM" \
    --manifest "$PROJ/app/src/main/AndroidManifest.xml" \
    -R "$BUILD/res.zip" \
    --java "$BUILD/gen" \
    --auto-add-overlay \
    --min-sdk-version 21 --target-sdk-version 34

echo "[3/7] 编译 Java 源码"
find "$PROJ/app/src/main/java" "$BUILD/gen" -name '*.java' > "$BUILD/sources.txt"
javac -source 8 -target 8 -encoding UTF-8 -nowarn \
    -cp "$PLATFORM" -d "$BUILD/classes" @"$BUILD/sources.txt"

echo "[4/7] 转换为 DEX"
CLASSES=$(find "$BUILD/classes" -name '*.class' | tr '\n' ' ')
"$BT/d8" --lib "$PLATFORM" --min-api 21 --output "$BUILD/dex" $CLASSES

echo "[5/7] 组装 APK（classes.dex + 内置字体 assets）"
cp "$BUILD/base.apk" "$BUILD/unsigned.apk"
cp "$BUILD/dex/classes.dex" "$BUILD/staging/classes.dex"
cp -r "$PROJ/app/src/main/assets" "$BUILD/staging/assets"
(cd "$BUILD/staging" && zip -q -r "$BUILD/unsigned.apk" classes.dex assets)

echo "[6/7] 对齐并准备签名密钥"
KS="$BUILD/debug.keystore"
if [ ! -f "$KS" ]; then
    keytool -genkeypair -keystore "$KS" \
        -storepass android -keypass android \
        -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 \
        -dname "CN=BaiBianYinZhang, OU=OpenSource, O=BaiBianYinZhang, C=CN" \
        >/dev/null 2>&1
fi
"$BT/zipalign" -f -p 4 "$BUILD/unsigned.apk" "$BUILD/aligned.apk"

echo "[7/7] 签名并校验"
"$BT/apksigner" sign \
    --ks "$KS" --ks-pass pass:android --key-pass pass:android \
    --v1-signing-enabled true --v2-signing-enabled true \
    --out "$PROJ/$APK_NAME" "$BUILD/aligned.apk"
"$BT/apksigner" verify --verbose "$PROJ/$APK_NAME"

echo
echo "构建完成：$PROJ/$APK_NAME"
ls -lh "$PROJ/$APK_NAME"
