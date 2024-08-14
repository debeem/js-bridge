#!/bin/bash

# Check if a webpack project already exists.
if ([ -d "js" ] &&
    [ -d "js/node_modules" ] &&
    [ -d "js/output" ] &&
    [ -d "js/src" ] &&
    [ -f "js/package.json" ] &&
    [ -f "js/webpack.config.js" ]); then
    echo "Webpack project already exists. Exit."
    exit 0
fi

# Create the "js" directory and navigate into it.
mkdir -p js
cd js

# Delete the "node_modules" directory if it exists.
if [ -d "node_modules" ]; then
    echo "Delete node_modules directory."
    rm -rf node_modules
fi

# Initialize the npm project.
echo "Initialize the npm project."
npm init -y

# Install webpack and webpack-cli.
echo "Install webpack and webpack-cli."
npm install webpack webpack-cli --save-dev

# Create the "src" and "dist" directories.
echo "Create the src and dist directories."
mkdir -p src output

# Create and configure the "webpack.config.js" file.
echo "Create and configure the webpack.config.js file."
cat << EOF > webpack.config.js
// webpack.config.js
const path = require('path');

module.exports = {
  mode: 'development',    // production,development
  entry: './src/index.js',
  output: {
    filename: 'bundle.js',
    path: path.resolve(__dirname, 'output'),
  }
};
EOF

echo "Create the src/business.js file includes sample business."
cat << EOF > src/business.js
// business.js
// This is a sample code. You need to modify it to your own business logic.
import * as DebeemWallet from 'debeem-wallet';
window.DebeemWallet = DebeemWallet;
import * as DebeemId from 'debeem-id';
window.DebeemId = DebeemId;
import * as DebeemCipher from 'debeem-cipher';
window.DebeemCipher = DebeemCipher;
import * as Ethers from 'ethers';
window.Ethers = Ethers;
import * as Idb from 'idb';
window.Idb = Idb;
import * as FakeIndexeddb from 'fake-indexeddb';
window.FakeIndexeddb = FakeIndexeddb;

export function serializable(obj) {
    return JSON.parse(JSON.stringify(obj, (key, value) =>
    typeof value === 'bigint'
    ? value.toString()
    : value
    ));
}
window.serializable = serializable;
EOF

echo "Create the src/index.js file."
cat << EOF > src/index.js
// index.js
import './business.js';

// Initialize function to be called from Kotlin
window.initialize = (initialized = true, callback) => {
    const init = async () => {
        try {
            console.log('initialize(' + initialized + ')');
            if (initialized) {
            } else {
            }
            return { success: true, message: "Initialized successfully", initialized: initialized };
        } catch (error) {
            return { success: false, error: error.toString() };
        }
    };

    init().then(result => {
        window.Android.handleResult("initialize", JSON.stringify(result));
    });gs
};
EOF

echo "Create the .gitignore file."
cat << EOF > .gitignore
/node_modules/
package-lock.json
EOF

# Create the "assets" directory in the Android project.
assets_dir="../src/main/assets"
if [ ! -d "$assets_dir" ]; then
    echo "Create directory: $assets_dir"
    mkdir -p "$assets_dir"
else
    echo "Directory $assets_dir already exists."
fi

# Create and configure the "index.html" file.
echo "Create and configure the index.html file."
cat << EOF > "$assets_dir/index.html"
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>JS-Bridge</title>
    <script src="bundle.js"></script>
</head>
<body>
</body>
</html>
EOF

echo "Webpack project creation complete."