# Nano
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://www.apache.org/licenses/LICENSE-2.0) [![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)](https://github.com/threeloe/nano/pulls) [![Release](https://jitpack.io/v/threeloe/nano.svg)](https://jitpack.io/#threeloe/nano)

[README 中文版](./README.zh-CN.md)

Nano is an Android SO file compression framework designed to reduce the size of shared object (SO) files within APK or AAB files while supporting runtime dynamic loading. Common use cases include:
* **Pre-installed apps** - Device manufacturers often require SO files to be stored in non-compressed form within the APK, significantly increasing package size while imposing strict size limitations.
* **Lite versions** - Applications with extremely stringent package size requirements.

## Demo Screenshot
![Demo screenshot](./assets/images/demo_apk.png)

## Quick Start
### Plugin Integration
Declare plugin dependencies and repositories in your root `build.gradle`:
```groovy
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }

    allprojects {
        repositories {
            maven { url "https://jitpack.io" }
        }
    }

    dependencies {
        classpath "com.github.threeloe.nano:plugin:1.0.0"
    }
}
```
Apply the Nano plugin in your app's build.gradle:
```groovy
apply plugin: "com.threeloe.nano"
nano {
    enable = true
    compressMethod = "zstd" // Compression method, supports zstd and xz
    groups {  // Group configuration, each group can be compressed into multiple file blocks
        launch {
            blockNum 4  
            include "libijkffmpeg.so"
            include "libijkplayer.so"
            include "libijksdl.so"
            include "libshadowhook.so"
        }
        second {
            blockNum 2
            include "libtensorflowlite_jni.so"
            include "libtensorflowlite_gpu_jni.so"
        }
    }
}
```
### SDK Integration
Add the sdk dependency:
```groovy
dependencies {
    implementation "com.github.threeloe.nano:nano:1.0.0"
    //zstd decompression method for Android
    implementation "com.github.luben:zstd-jni:1.5.7-3@aar"
}
```
#### For Android 9+ Only (e.g., pre-installed apps)
If you only want to support Android 9 and above, such as in pre-installed package , you can simply register the AppComponentFactory:
```kotlin
@RequiresApi(Build.VERSION_CODES.P)
class DemoAppComponentFactory : AppComponentFactory(){
    override fun instantiateClassLoader(cl: ClassLoader, aInfo: ApplicationInfo): ClassLoader {
        // Replace with the new ClassLoader
        return Nano.install(cl, aInfo)
    }
}
```
And initialize code in the Application:
``` kotlin
class DemoApplication :Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val nanoConfig = NanoConfig()
        // configure the decompression method implementation
        nanoConfig.zstdCompressMethod(object : IDecompressMethod {
            override fun getDecompressedStream(input: InputStream): InputStream {
                return ZstdInputStream(input)
            }
        })
        Nano.init(application, nanoConfig)

        //Business logic starts here
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(
            this
        )
        //Business logic
    }
}
```
#### For Full Android Compatibility (5.0+)
If you want to support all Android versions, you need to modify the application's Application to inherit from NanoApplication and migrate the original business initialization logic to a NanoApplicationLike implementation class, as shown below: 
``` kotlin
class DemoApplication : NanoApplication() {

    override fun getApplicationLikeClassName(): String {
        return "com.threeloe.nano.demo.app.DemoApplicationLike"
    } 
}

class DemoApplicationLike(app: Application) : NanoApplicationLike(app) {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val nanoConfig = NanoConfig()
        // configure the decompression method implementation
        nanoConfig.zstdCompressMethod(object : IDecompressMethod {
            override fun getDecompressedStream(input: InputStream): InputStream {
                return ZstdInputStream(input)
            }
        })
        Nano.init(application, nanoConfig)
        //Business logic starts here
    }

    override fun onCreate() {
        super.onCreate()
        //Business logic 
        MMKV.initialize(
            application
        )
    }
}
```

## Features
* **Group Compression** - Support SO file grouping, each group can be split into multiple blocks for concurrent decompression.
* **Decompression Modes** - Supports synchronous/asynchronous decompression.
```kotlin
interface NanoApi {

    fun init(application: Application, config: NanoConfig?)

    //synchronous decompression, will block the current thread until decompression is complete
    fun decompress(group: String): NanoResult

    //asynchronous decompression,  callback will be invoked when decompression is complete
    fun decompressAsync(group: String, callback: (NanoResult) -> Unit)

}
```
* **Compression Algorithms** - [zstd](https://github.com/facebook/zstd) (faster decompression) or [xz](https://github.com/tukaani-project/xz) (higher compression ratio).
Nano Plugin supports both zstd and xz compression methods, with zstd being the default.
Only choose one decompression method implementation at runtime for better APK size. You can configure it as follows:
For zstd:
```groovy
dependencies {
    /**
     * You can use other zstd decompression libraries
    */ 
    implementation "com.github.luben:zstd-jni:1.5.7-3@aar"
}
```
```kotlin
val nanoConfig = NanoConfig()
nanoConfig.zstdCompressMethod(object : IDecompressMethod {
    override fun getDecompressedStream(input: InputStream): InputStream {
        return ZstdInputStream(input)
    }
})
```
For xz:
```groovy
dependencies {
    /**
     * You can use other xz decompression libraries
     */
    implementation "org.tukaani:xz:1.9"
}
```
```kotlin 
val nanoConfig = NanoConfig()
nanoConfig.xzCompressMethod(object : IDecompressMethod {
    override fun getDecompressedStream(input: InputStream): InputStream {
        return XZInputStream(input)
    }
})
```

## Compatibility
* **Gradle Plugin**: Compatible with Android Gradle Plugin 7.0 and above
* **Android Versions**: Supports Android 5.0 (API 21) and higher

## License
[Apache License](./LICENSE)