-keep class com.threeloe.nano.compress.info.CompressInfo{*;}
-keep class com.threeloe.nano.compress.info.NanoFileInfo {*;}
-keep class com.threeloe.nano.classloader.NanoClassLoader$CompoundEnumeration {
    *;
}
# zstd-jni
-keep class com.github.luben.zstd.ZstdInputStreamNoFinalizer{
    *;
}