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

package com.threeloe.nano.classloader

import android.content.pm.ApplicationInfo
import com.threeloe.nano.NanoLog
import com.threeloe.nano.compress.info.CompressInfo
import com.threeloe.nano.core.NanoManager
import com.threeloe.nano.utils.ReflectUtils
import dalvik.system.DexFile
import java.io.File
import java.lang.reflect.Field
import java.util.Arrays

object NanoClassLoaderFactory {
    @Throws(Throwable::class)
    fun createNewClassLoader(cl: ClassLoader, appInfo: ApplicationInfo): ClassLoader{
        if (CompressInfo.isReplaceClassLoader()) {
            NanoManager.init(appInfo)
            val workDir = NanoManager.currentVersionWorkDir
            val newCL = createNewClassLoader(cl, workDir)
            return newCL
        } else {
            return cl
        }
    }

    @Throws(Throwable::class)
    fun createNewClassLoader(
        oldClassLoader: ClassLoader?,
        newLibDir: File?
    ): ClassLoader {
        val baseDexClassLoaderClass =
            Class.forName("dalvik.system.BaseDexClassLoader", false, oldClassLoader)
        val pathListField = ReflectUtils.findField(
            baseDexClassLoaderClass,
            "pathList"
        )
        val oldPathList = pathListField[oldClassLoader]

        //Dex
        val dexPathBuilder = StringBuilder()
        val originDexPaths = getDexPaths(oldPathList)
        for (i in originDexPaths.indices) {
            if (i > 0) {
                dexPathBuilder.append(File.pathSeparator)
            }
            dexPathBuilder.append(originDexPaths[i])
        }
        val combinedDexPath = dexPathBuilder.toString()

        //Lib dir
        val nativeLibraryDirectoriesField =
            ReflectUtils.findField(oldPathList.javaClass, "nativeLibraryDirectories")
        val oldNativeLibraryDirectories = if (nativeLibraryDirectoriesField.type.isArray) {
            Arrays.asList(*nativeLibraryDirectoriesField[oldPathList] as Array<File?>)
        } else {
            nativeLibraryDirectoriesField[oldPathList] as List<File>
        }
        val libraryPathBuilder = StringBuilder()
        var isFirstItem = true
        for (libDir in oldNativeLibraryDirectories) {
            if (libDir == null) {
                continue
            }
            if (isFirstItem) {
                isFirstItem = false
            } else {
                libraryPathBuilder.append(File.pathSeparator)
            }
            libraryPathBuilder.append(libDir.absolutePath)
        }

        //append newLibDir
        if (newLibDir != null && newLibDir.exists() && newLibDir.isDirectory) {
            libraryPathBuilder.append(File.pathSeparator)
            libraryPathBuilder.append(newLibDir.absolutePath)
        }
        val combinedLibraryPath = libraryPathBuilder.toString()

        val result: ClassLoader =
            NanoClassLoader(combinedDexPath, combinedLibraryPath, oldClassLoader!!)

        return result
    }

    @Throws(Throwable::class)
    private fun getDexPaths(dexPathList: Any): List<String> {
        val dexPaths: MutableList<String> = ArrayList()
        val dexElementsField = ReflectUtils.findField(dexPathList.javaClass, "dexElements")
        val dexElements = dexElementsField[dexPathList] as Array<Any>
        for (e in dexElements) {
            val dexPath = getDexPath(e)
            if (dexPath != null) {
                // Add the element to the list only if it is a file. A null dex path signals the
                // element is a resource directory or an in-memory dex file.
                dexPaths.add(dexPath)
            }
        }
        return dexPaths
    }

    @Throws(IllegalAccessException::class)
    private fun getDexPath(element: Any): String? {
        var dexPathField: Field? = null
        try {
            dexPathField = ReflectUtils.findField(element.javaClass, "path")
        } catch (e: NoSuchFieldException) {
            NanoLog.e( "Error getting Element.path", e)
        }
        //Android 7 and below
        if (dexPathField == null) {
            try {
                dexPathField = ReflectUtils.findField(element.javaClass, "dir")
            } catch (e: NoSuchFieldException) {
                NanoLog.e( "Error getting Element.dir ", e)
            }
        }
        if (dexPathField != null) {
            val file = dexPathField[element] as File?
            if (file != null && file.exists()) {
                return if (file.isDirectory) {
                    null
                } else {
                    file.absolutePath
                }
            }
        }

        var dexFileField: Field? = null
        try {
            dexFileField = ReflectUtils.findField(element.javaClass, "dexFile")
        } catch (e: Throwable) {
            NanoLog.e( "Error getting dexFile", e)
        }
        val dexFile = dexFileField?.get(element) as? DexFile
        if (dexFile != null) {
            return dexFile.name
        }
        return null
    }
}
