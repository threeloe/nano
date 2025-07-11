# Nano
Nano是一个Android SO文件压缩框架，用于将APK/AAB文件中的SO文件压缩为更小体积，并支持运行时动态加载。常见应用场景：
* **预装包场景**：厂商要求SO文件必须以store（非压缩）形式存在，导致包体积显著增大，同时厂商对包体大小有严格限制
* **极速版应用**：对包体积要求极高的应用场景

## 效果图
![效果图](./assets/images/demo_apk.png)

## 快速上手
### 插件接入
在项目根目录的`build.gradle`声明插件依赖和仓库：
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
在app模块的build.gradle启用插件：
```groovy
apply plugin: "com.threeloe.nano"
nano {
    enable = true
    compressMethod = "zstd" // 压缩算法，支持zstd（解压快）或xz（压缩率高）
    groups {  // SO文件分组配置，每组可压缩为多个文件块
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
### SDK接入
添加依赖：
```groovy
dependencies {
    implementation "com.github.threeloe.nano:nano:1.0.0"
}
```
#### 仅支持Android 9+（如预装包）
如果你只是想支持高版本系统，比如想预装包这样的场景，可以只注册AppComponentFactory：
```kotlin
@RequiresApi(Build.VERSION_CODES.P)
class DemoAppComponentFactory : AppComponentFactory(){
    override fun instantiateClassLoader(cl: ClassLoader, aInfo: ApplicationInfo): ClassLoader {
        return Nano.install(cl, aInfo) // 替换ClassLoader
    }
}
```
然后Application中初始化：
```kotlin
class DemoApplication : Application() {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Nano.init(this, null)
        Nano.decompress("launch") // 解压so文件
        // 业务逻辑初始化
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(this)
        // 业务逻辑
    }
}
```
#### 全Android版本兼容（5.0+）
如果你想支持所有的Android版本呢，你需要改造Application类继承`NanoApplication`，并实现`getApplicationLikeClassName()`方法返回你的`NanoApplicationLike`类名，
把原有Application中的业务逻辑放到`NanoApplicationLike`中：
```kotlin
class DemoApplication : NanoApplication() {
    override fun getApplicationLikeClassName(): String {
        return "com.threeloe.nano.demo.app.DemoApplicationLike"
    } 
}

class DemoApplicationLike(app: Application) : NanoApplicationLike(app) {
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        Nano.init(application, null)
        Nano.decompress("launch") // 解压so文件
        // 业务逻辑初始化
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(application)
        // 业务逻辑 
    }
}
```
## 功能特性 
* **分组压缩**：支持SO文件分组，每组可拆分为多文件块，运行时并发解压提速
* **解压模式**：支持同步/异步解压
* **压缩算法**：[zstd](https://github.com/facebook/zstd)（解压速度快）或[xz](https://github.com/tukaani-project/xz)（压缩率高）

## 兼容性
* **Gradle插件**：兼容Android Gradle Plugin 7.0+
* **Android版本**：支持Android 5.0 (API 21) 及以上

## 许可证
[Apache License](./LICENSE)






