/*
 * Copyright (C) 2025 ThreeLoe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.threeloe.nano.utils

import android.content.Context
import com.threeloe.nano.NanoLog
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.zip.ZipFile

object ApkUtils {

    const val FILE_SUFFIX_SO: String = ".so"
    private const val APK_SUFFIX = ".apk"

    private fun getEntryStreamFromApk(apkFile: File, entryName: String): InputStream? {
        return ZipFile(apkFile).let { zipFile ->
            zipFile.getEntry(entryName)?.let { zipFile.getInputStream(it) }
        }
    }

    private fun getEntryStreamFromApks(context: Context, entry: String): InputStream? {
        val apkFiles = mutableListOf<File>().apply {
            add(File(context.applicationInfo.sourceDir))
            // Add split APKs if available
            context.applicationInfo.splitSourceDirs?.takeIf { it.isNotEmpty() }?.let { dirs ->
                addAll(dirs.map { File(it) })
            } ?: run {
                File(context.applicationInfo.sourceDir).parentFile?.listFiles()?.filter {
                    it.isFile && it.name.endsWith(APK_SUFFIX)
                }?.let { addAll(it) }
            }
        }
        NanoLog.i("getEntryStreamFromApks, apkFiles: $apkFiles")
        for (apkFile in apkFiles) {
            getEntryStreamFromApk(apkFile, entry)?.let { return it }
        }
        return null
    }

    fun getAbiSet(context: Context): Set<String> {
        val soPattern = Regex("^lib/[^/]+/lib[^/]+\\.so$")
        val abiSet = mutableSetOf<String>()
        // Collect all APK paths (main APK + split APKs)
        val apkPaths = mutableListOf(context.applicationInfo.sourceDir).apply {
            context.applicationInfo.splitSourceDirs?.let { addAll(it) }
        }
        // Process all APKs
        apkPaths.forEach { apkPath ->
            ZipFile(apkPath).use { zip ->
                zip.entries().asSequence()
                    .filter { !it.isDirectory && soPattern.matches(it.name) }
                    .mapNotNull { entry -> entry.name.split("/").getOrNull(1) }
                    .toCollection(abiSet)
            }
            if (abiSet.isNotEmpty()) return abiSet
        }
        return abiSet
    }

    fun getCompressedFileStream(context: Context, compressedFileName: String): InputStream? {
        return when {
            compressedFileName.endsWith(FILE_SUFFIX_SO) -> {
                File(
                    context.applicationInfo.nativeLibraryDir, compressedFileName
                ).takeIf { it.exists() }?.let {
                    FileInputStream(it)
                } ?: getEntryStreamFromApks(
                    context, "lib/${AbiUtils.getRuntimeAbi(context)}/$compressedFileName"
                ) //extractNativeLibs=false, so we need to get the file from the APKs
            }
            else -> context.assets.open(compressedFileName)
        }
    }
}
