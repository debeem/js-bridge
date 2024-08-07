#!/bin/bash

########################## Editable ###############################
# TODO 修改成具体业务定义生产依赖包列表
prod_packages=("debeem-wallet" "debeem-id" "debeem-cipher" "ethers" "idb" "fake-indexeddb")
###################################################################

######################### No need to edit #########################
# List of development dependencies
dev_packages=("webpack" "webpack-cli")

# Target directory
target_dir="js"
cd "$target_dir" || exit
echo "Enter to the '$target_dir' directory and check the packaging environment."

# Function: Check for and install a package
check_and_install() {
    local install_flag=""
    local packages=()

    if [[ "$1" == "--save-dev" ]]; then
        install_flag="--save-dev"
        shift
    fi

    packages=("$@")
    local to_install=()
    for package in "${packages[@]}"; do
        if npm list "$package" --depth=0 &> /dev/null; then
            echo "$package is already installed."
        else
            echo "$package is not installed. Preparing to install."
            to_install+=("$package")
        fi
    done

    if [ ${#to_install[@]} -ne 0 ]; then
        echo "Installing: ${to_install[*]}"
        if ! npm install $install_flag "${to_install[@]}"; then
            echo "Installation failed."
            return 1
        fi
    fi
}

echo ""
echo "Check and install production dependencies:"
if check_and_install "${prod_packages[@]}"; then
    echo "Production dependencies installed successfully."
else
    echo "Error: Production dependency installation failed!"
    exit 1
fi

echo ""
echo "Check and install development dependencies:"
if check_and_install --save-dev "${dev_packages[@]}"; then
    echo "Development dependencies installed successfully."
else
    echo "Error: Development dependency installation failed."
    exit 1
fi

echo ""
echo "Environment check complete, starting webpack packaging..."
# 执行 npx webpack 打包命令
npx webpack

# 获取 Webpack 生成的 bundle.js 文件路径
bundle_file=$(ls dist/*.js | head -n 1)

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
###################################################################