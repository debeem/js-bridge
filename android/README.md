# Android JS-Bridge SDK

- [1.Introduction](#section-1)
- [2.Setting up](#section-2)
    - [2.1.JS-Bridge SDK integration](#section-2-1)
    - [2.2.JS Development Configuration](#section-2-2)
      - [2.2.1.Initialize webpack](#section-2-2-1)
      - [2.2.2.Service Configuration](#section-2-2-2)
      - [2.2.3.Packaging and Publishing](#section-2-2-3)
- [3.SDK Usage](#section-3)
  - [3.1.Call custom JS script interfaces](#section-3-1)
- [4.SDK Update](#section-4)
  - [4.1.Android](#section-4-1)
  - [4.2.JavaScript](#section-4-2)
  - [4.3.Publishing](#section-4-3)
- [5.FAQs](#section-5)

<h1 id="section-1">1.Introduction</h1>

The Android JS-Bridge SDK is a powerful middleware whose core function is to call NPM services through the WebView's JavaScript interface, enabling cross-platform function reuse and flexible business logic processing. This approach is particularly suitable for hybrid application development, allowing you to take full advantage of the flexibility of web technologies and the performance advantages of native applications.

<h1 id="section-2">2.Setting up</h1>

<h2 id="section-2-1">2.1.JS-Bridge SDK integration</h2>

Add the JitPack Maven repository to your project-level `build.gradle` file:
```kotlin
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' } // 添加这一行
	}
}
```

Add the dependency to your `app/build.gradle` file:   

[![](https://jitpack.io/v/debeem/js-bridge.svg)](https://jitpack.io/#debeem/js-bridge)

```gradle
dependencies {
    implementation 'com.github.debeem:js-bridge:1.0.0-beta.1'
}
```

<h2 id="section-2-2">2.2.JS Development Configuration</h2>

<h3 id="section-2-2-1">2.2.1.Initialize webpack</h3>

从 js-bridge sdk 的目录中拷贝打包脚本目录 `builder` 到自己的 android 项目中

然后在 android 项目中，执行初始化脚本 `setup_webpack.sh`，初始化脚本只需要执行一次。

在 android 项目中执行初始化操作：
```shell
./builder/setup_webpack.sh
```

以下是生成的 `js` 目录结构 和  `assets` 目录结构：
```
js/
├── dist/
├── node_modules/
├── src/
│   ├── business.js
│   └── index.js
├── .gitignore
├── package.json
└── webpack.config.js

src/main/assets/
└── index.html
```
`js` 目录下：
 - `dist/`: 文件夹存放生成的 `bundle.js`
 - `node_modules/`: npm 生成的依赖包文件
 - `src/`: `index.js` 和 `business.js` 业务代码
 - `package.json`: npm 配置文件
 - `webpack.config.js`: webpack 配置文件

`src/main/assets/` 目录下：
 - `index.js`: 引用生成的 `bundle.js`


<h4 id="section-2-2-2">2.2.2.Service Configuration</h4>

修改 `builder/build_webpack.sh` 文件中的依赖业务。
脚本文件中是个 `debeem-wallet` 示例：
```shell
########################## Editable ###############################
# TODO 修改成具体业务定义生产依赖包列表
prod_packages=("debeem-wallet" "debeem-id" "debeem-cipher" "ethers" "idb" "fake-indexeddb")
###################################################################

```

然后需要继续修改 `src/business.js` 文件，配置并暴露服务。
`src/business.js` 中只是个 `debeem-wallet` 示例：
```shell
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
```

<h3 id="section-2-2-3">2.2.3.Packaging and Publishing</h3>

配置好 js 服务之后，就可以通过 `build_webpack.sh` 脚本打包发布生成的 `bundle.js` 了。

在 android 项目中执行打包发布操作：
```shell
./builder/build_webpack.sh
```
把生成的 `bundle.js` 复制到 android 项目中的 `assets` 中: 
```
src/main/assets/
├── bundle.js
└── index.html
```

<b>最后 android 项目编译调试就可以调用最新的 js 服务了。</b>

<h1 id="section-3">3.SDK Usage</h1>

<h2 id="section-3-3">3.1.Call custom JS script interface</h2>

kotlin code interface
```kotlin
customScript(label, script, callback)
```
Directly write JS business scripts in native code
 - label: Custom label, used for callback identification.
 - script: Specific JS script.
 - callback: Callback method.

Custom JS Business Logic (Example: DebeemWallet)
```javascript
// business.js
// This is a sample code. You need to modify it to your own business logic.
import * as DebeemWallet from 'debeem-wallet';
window.DebeemWallet = DebeemWallet;
```

Example requirement: Need to get the price of BTC/USD, which can be achieved through the queryPairPrice method of the WalletAccount class in the DebeemWallet wallet.
Kotlin code example
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

<h1 id="section-4">4.SDK Update</h1>

<h2 id="section-4-1">4.1.Android</h2>

Android Native 接口更新，可以通过修改 SDK 目录中的 NpmServiceSDK.kt 文件，更新升级 SDK。

<h2 id="section-4-2">4.2.JavaScript</h2>

Details: [2.2.JS business development environment](#section-2-2)

<h2 id="section-4-3">4.3.Publishing</h2>

1、SDK 上传打包

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

<h1 id="section-5">5.常见问题</h1>

1、由于在 android 目录中创建了 `node_modules` 目录，会导致 android studio 加载卡顿。
可以手动设置 `node_modules` 不加载操作，在 `node_modules` 目录上右键：Mark Directory as -> Excluded  

![android studio](images/android_studio_fix_1.png)


