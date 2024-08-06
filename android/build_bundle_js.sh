#!/bin/bash

# 设置目标目录
target_dir="web-js-npm"

# 进入目标目录
cd "$target_dir" || exit
echo "进入 $target_dir 目录下，检查打包环境"

# 检查 webpack 和 webpack-cli 是否安装
echo ""
echo "1.检查 webpack 和 webpack-cli 是否安装..."
if ! npm list webpack &> /dev/null || ! npm list webpack-cli &> /dev/null; then
  echo "webpack 或 webpack-cli 未安装，正在安装..."
  npm install webpack webpack-cli --save-dev
else
  echo "已安装: webpack 和 webpack-cli"
fi

# 检查 debeem 和相关依赖是否安装
echo ""
echo "2.检查 debeem 和相关依赖是否安装..."
if ! npm list debeem-wallet &> /dev/null || ! npm list debeem-id &> /dev/null || ! npm list debeem-cipher &> /dev/null || ! npm list ethers &> /dev/null || ! npm list idb &> /dev/null || ! npm list fake-indexeddb &> /dev/null; then
  echo "正在安装 debeem-wallet, debeem-id, debeem-cipher, ethers, idb 和 fake-indexeddb..."
  npm install debeem-wallet debeem-id debeem-cipher ethers idb
  npm install fake-indexeddb --save
else
  echo "已安装: debeem 和相关依赖"
fi

echo ""
echo "3.打包环境检查完成，开始 webpack 打包"
# 执行 npx webpack 打包命令
npx webpack

# 获取 Webpack 生成的 bundle.js 文件路径
bundle_file=$(ls output/*.js | head -n 1)

# 目标 assets 目录
assets_dir="../js-bridge-npm/src/main/assets"

# 复制 bundle.js 到 assets 目录
cp "$bundle_file" "$assets_dir"

echo ""
echo "打包完成，$bundle_file 已复制到 $assets_dir 目录下"