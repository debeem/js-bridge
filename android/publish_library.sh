#!/bin/bash

set -e  # 遇到错误立即退出

printHelp() {
    echo "Usage: $0 -v <version>"
    echo "Example: $0 -v 1.0.0-alpha.1"
    exit 1
}

while getopts 'v:h' OPT; do
    case $OPT in
        v) NEW_VERSION="$OPTARG";;
        h) printHelp;;
        ?) printHelp;;
    esac
done

shift $(($OPTIND - 1))

if [ -z "$NEW_VERSION" ]; then
    echo "Error: SDK version is required"
    printHelp
fi

# 获取脚本所在目录
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

# 定义SDK目录路径（与脚本同级）
SDK_DIR="$SCRIPT_DIR/js-bridge-npm"

# 检查SDK目录是否存在
if [ ! -d "$SDK_DIR" ]; then
    echo "Error: SDK directory not found at $SDK_DIR"
    exit 1
fi

# 切换到SDK目录
cd "$SDK_DIR" || exit 1

# 更新 build.gradle 文件
BUILD_GRADLE="$SDK_DIR/gradle.properties"
if [ -f "$BUILD_GRADLE" ]; then
    echo "
    android.useAndroidX=true
    android.enableJetifier=true
    SDK_VERSION=$NEW_VERSION
    " > "$BUILD_GRADLE"
    echo "Updated version in $BUILD_GRADLE"
else
    echo "Warning: build.gradle not found at $BUILD_GRADLE. Make sure you update the version manually."
fi

# 切回原始目录
cd - || exit 1

# 提交变更
git add .
git commit -m "Bump version to $NEW_VERSION"

# 创建tag并推送
git tag -a "$NEW_VERSION" -m "Version $NEW_VERSION"
git push origin main --tags

echo "Library published to JitPack. Version: $NEW_VERSION"
