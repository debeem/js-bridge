# js-bridge

## Android JS-Bridge SDK 

[![](https://jitpack.io/v/debeem/js-bridge.svg)](https://jitpack.io/#debeem/js-bridge)

Primarily enables Android to call NPM services through JS. For more details: [README](android/README.md)

### Quick start

Add the JitPack Maven repository to your project-level `build.gradle` file:

```gradle
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }    // Add this line
	}
}
```

Add the dependency to your `app/build.gradle` file:

```gradle
dependencies {
    implementation 'com.github.debeem:js-bridge:x.x.x'
}
```