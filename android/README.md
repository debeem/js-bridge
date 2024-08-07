# Android JS-Bridge-NPM SDK

[![](https://jitpack.io/v/debeem/js-bridge.svg)](https://jitpack.io/#debeem/js-bridge)

## 目录

- [1.简介](#section-1)
- [2.集成](#section-2)
    - [2.1.Android端集成](#section-2-1)
- [3.使用](#section-3)
  - [3.1.调用全局方法或类的静态方法](#section-3-1)
  - [3.2.调用类的具体属性和方法](#section-3-2)
  - [3.3.自定义 js 脚本](#section-3-3)
  - [3.4.导入的全局包名](#section-3-3)
- [4.更新](#section-4)
  - [4.1.Android端](#section-4-1)
  - [4.2.JavaScript端](#section-4-2)
  - [4.3.打包发布](#section-4-3)
- [5.常见问题](#section-5)
- [6.贡献指南](#section-6)
- [7.许可证](#section-7)

<h2 id="section-1">1.简介</h2>

Android JS-Bridge-NPM SDK 是一个强大的中间件，核心功能是通过 WebView 的 JavaScript 接口调用 NPM
服务，从而实现跨平台的功能复用和灵活的业务逻辑处理。这种方法特别适用于混合应用开发，可以充分利用 Web
技术的灵活性和原生应用的性能优势。

<h2 id="section-2">2.集成和使用</h2>

<h3 id="section-2-1">Android端集成</h3>

在项目根目录的 build.gradle 文件中添加 jitpack maven 依赖：
```kotlin
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' } // 添加这一行
	}
}
```

在你的 `app/build.gradle` 文件中添加以下依赖：

```gradle
dependencies {
    implementation 'com.github.debeem:js-bridge:1.0.0-alpha.15'
}
```

<h2 id="section-3">3.API使用</h2>

<h3 id="section-3-1">3.1.调用全局方法或类的静态方法</h3>

需要指定包名，全局方法/类名的静态方法）

npm 方法：[isValidWalletFactoryData](https://github.com/debeem/js-debeem-wallet/blob/c6c973a8093eb6a4e2461c5bcd411d627d76fe61/src/services/wallet/WalletFactory.ts#L39)
```javascript
WalletFactory.isValidWalletFactoryData(wallet): boolean
```

kotlin 调用
```kotlin
callJsFunctionAsync(packageName, functionName, *args, callback)
```

<details>
<summary>代码示例</summary>

```kotlin
walletBusiness.callJsFunctionAsync(
  "DebeemWallet",
  "WalletFactory.isValidWalletFactoryData"
) { result ->
  Log.e(TAG, "WalletFactory.isValidWalletFactoryData: $result")
  
  runOnUiThread {
    binding.jsResultTv.text = result
  }
}
```
</details>

<h3 id="section-3-2">3.2.调用类的具体属性和方法</h3>

构造类的对象，需要指定具体包名，类名以及具体的属性名和方法名称

npm 方法 [queryPairPrice](https://github.com/debeem/js-debeem-wallet/blob/c6c973a8093eb6a4e2461c5bcd411d627d76fe61/src/services/wallet/WalletAccount.ts#L193)
```javascript
await new WalletAccount().queryPairPrice( `BTC/USD` );
```

kotlin 调用
```kotlin
createCallJsFunctionAsync(
  packageName,
  className,
  constructorArgs,
  methodName,
  methodArgs,
  callback
)
```

<details>
<summary>代码示例：获取类的属性值</summary>

```kotlin
// 获取类的属性值
walletBusiness.createCallJsFunctionAsync(
    "DebeemWallet",
    "TokenService",
     listOf(11155111),
     "nativeTokenAddress",
      emptyList(),
) { result ->
      Log.e(TAG, "TokenService.nativeTokenAddress: $result")

      runOnUiThread {
         binding.jsResultTv.text = result
      }
 }
```
</details>

<details>
<summary>代码示例：调用类的方法</summary>

```kotlin
walletBusiness.createCallJsFunctionAsync(
   "DebeemWallet",
    "WalletAccount",
    emptyList(),
    "queryPairPrice",
    listOf("BTC/USD")
) { result ->
    Log.e(TAG, "WalletAccount.queryPairPrice: $result")

    runOnUiThread {
        binding.jsResultTv.text = result
    }
}
```
</details>

<h3 id="section-3-3">3.3.自定义js脚本</h3>

直接 native 编写 js 业务脚本
 - label：自定义，回调识别用
 - script: 具体 js 脚本
 - callback: 回调方法

kotlin 调用

```kotlin
customScript(label, script, callback)
```

<details>
<summary>代码示例</summary>

```kotlin
// custom script
val label = "custom_test"
val script = """
 (function(){
     // Business start
     const execute = async () => {
     try { 
        const walletAccount = new DebeemWallet.WalletAccount();
        const result = await walletAccount.queryPairPrice('BTC/USD');
        return { success: true, data: serializable(result) };
     } catch (error) {
        return { success: false, error: error.toString() };
     }};
     // Business end
     
     // Business execute result to native
     execute().then(result => {
        window.WalletBridge.handleResult(`${label}`, JSON.stringify(result));
     });
})();
""".trimIndent()

walletBusiness.customScript(label, script) { result ->
  Log.e(TAG, "customScript result: $result")

  runOnUiThread {
    binding.jsResultTv.text = result
  }
}
```
</details>

<h3 id="section-3-4">3.4.导入的具体业务包</h3>

可以通过更新 js 文件，替换成自己需要的服务，详细实现: [JS业务更新](#section-4-2) 

以下是导入的 debeem-wallet npm 服务，在 js 中导入的对应包名，以及暴露的方法
 - DebeemWallet
 - DebeemId
 - DebeemCipher
 - Ethers
 - Idb
 - FakeIndexeddb
 - serializable：暴露的方法，解决 BigInt 解析问题

```javascript

// 首先确保所有需要的包都已经正确导入和挂载到 window 对象上
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

import { serializable } from './utils';
window.serializable = serializable;
```

<h2 id="section-4">4.更新升级SDK</h2>

<h3 id="section-4-1">4.1.Android端</h3>

Android Native 接口更新，可以通过修改 SDK 目录中的 NpmServiceSDK.kt 文件，更新升级 SDK。

<h3 id="section-4-2">4.2.JavaScript端</h3>

js 具体业务更新，例如新增 npm 服务等。

>JavaScript 中调用 npm 服务并通过 webpack 打包多个 JS 文件成一个 bundle.js。
这个过程涉及设置一个适合的项目结构，安装必要的 npm 包，配置 webpack，并最终打包生成 bundle.js。

<details>
<summary>具体步骤</summary>

#### 1.初始化项目

首先创建一个新的项目文件夹，并初始化一个 npm 项目：

```shell
mkdir web-js-npm
cd web-js-npm
npm init -y  # 自动生成 package.json 文件
```

生成的 `package.json` 如下：

```json
{
  "name": "js-npm-web",
  "version": "1.0.0",
  "description": "",
  "main": "index.js",
  "scripts": {
    "test": "echo \"Error: no test specified\" && exit 1"
  },
  "keywords": [],
  "author": "",
  "license": "ISC"
}
```

#### 2.安装依赖

安装 webpack ：

```shell
npm install webpack webpack-cli --save-dev 
```

`webpack` 是核心工具，`webpack-cli` 允许你在命令行中运行 `webpack`

#### 3.创建项目结构

在项目目录下创建一个简单的文件结构：

```shell
/web-js-npm
  /src
    index.js
    component.js
  /output
  webpack.config.js
  package.json
```

在 src 文件夹中，index.js 可以是入口文件，而 component.js 是一个额外的模块。

#### 4.编写 JavaScript 文件

调用 npm 服务 [debeem-wallet](https://www.npmjs.com/package/debeem-wallet)，需要安装 debeem-wallet
服务和对应的依赖

```shell
npm install debeem-wallet debeem-id debeem-cipher ethers idb
npm install fake-indexeddb --save
```

#### 5.配置 webpack

在项目根目录下创建 webpack.config.js：

```shell
// webpack.config.js
const path = require('path');

module.exports = {
  mode: 'production',
  entry: './src/index.js',  // 入口文件
  output: {
    filename: 'bundle.js',  // 输出文件
    path: path.resolve(__dirname, 'output'),  // 输出路径
  }
};
```

这个配置告诉 webpack 从 src/index.js 开始打包，将所有依赖打包到 output/bundle.js。

#### 6.生成 bundle.js 文件

使用 webpack 命令生成 bundle.js

```shell
npx webpack 
```

</details>

<h3 id="section-4-3">4.3.打包发布</h3>

#### 打包脚本介绍

1、bundle.js 生成脚本

通过 webpack 打包 js，同时把生成的 bundle.js 复制到 SDK 指定目录中。

```shell
./build_bundle_js.sh 
```

2、SDK 上传打包

通过给项目打 tag，上传到 github 上，jitpack 自动识别 tag 自动打包。

```shell
./publish_library.sh -v 1.0.0-alpha.2

// 在根目录下执行
./android/publish_library.sh -v 1.0.0-alpha.15
```
><b>版本控制和策略</b>  
> alpha（1.0.0-alpha.1）: 早期内测版本，功能可能不完整  
> beta（1.0.0-beta.2）: 功能基本完整，相比Alpha版本更稳定  
> rc（1.0.0-rc.1）: 功能完整，主要bug已修复，可能直接成为正式发布版本  
> 1.0.0 : 正式发布的稳定版本  

<h2 id="section-5">5.常见问题</h2>

<h2 id="section-6">6.贡献指南</h2>

<h2 id="section-7">7.许可证</h2>


