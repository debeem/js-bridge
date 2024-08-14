#!/bin/bash

# Target directory
target_dir="js"
cd "$target_dir" || exit
echo "Enter to the '$target_dir' directory and check the packaging environment."

package_path="package.json"
if [ ! -f "$package_path" ]; then
    echo "Error: Could not find file: $package_path."
    echo "Please use the init_webpack script init js project."
    exit 1
fi

echo "Installing all dependencies listed in package.json..."

# 使用 npm install 安装所有依赖
if npm install; then
    echo "All dependencies installed successfully."
else
    echo "Error: dependency installation failed."
    exit 1
fi

echo ""
echo "Environment check complete, starting webpack packaging..."
# 执行 npx webpack 打包命令
npx webpack

# 获取 Webpack 生成的 bundle.js 文件路径
bundle_file=$(ls output/*.js | head -n 1)

# 目标 assets 目录
assets_dir="../src/main/assets"
if [ ! -d "$assets_dir" ]; then
    echo "Create directory: $assets_dir"
    mkdir -p "$assets_dir"
fi

# 复制 bundle.js 到 assets 目录
cp "$bundle_file" "$assets_dir"

echo ""
echo "Packaging complete. $bundle_file has been copied to the $assets_dir directory."