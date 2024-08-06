# js-bridge

## android 

[![](https://jitpack.io/v/debeem/js-bridge.svg)](https://jitpack.io/#debeem/js-bridge)

主要实现 Android 通过 JS 调用 npm 服务，更多详细内容：[README](android/README.md)

### 集成

在项目根目录的 build.gradle 文件中添加 jitpack 依赖：
```kotlin
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' } // Add this line
	}
}
```

在你的 `app/build.gradle` 文件中添加以下依赖：

```gradle
dependencies {
    implementation 'com.github.debeem:js-bridge:1.0.0-alpha.15'
}
```