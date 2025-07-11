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

package com.threeloe.nano.core.installer

import android.os.Build
import com.threeloe.nano.NanoLog
import com.threeloe.nano.utils.ReflectUtils
import dalvik.system.BaseDexClassLoader
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException


object SoDirInstaller {

    /**
     * All version of install logic obey the following strategies:
     * 1. If path of `folder` is not injected into the classloader, inject it to the
     * beginning of pathList in the classloader.
     *
     *
     * 2. Otherwise remove path of `folder` first, then re-inject it to the
     * beginning of pathList in the classloader.
     */
    @Throws(Throwable::class)
    fun installNativeLibraryPath(classLoader: ClassLoader, folder: File?): Boolean {
        if (folder == null || !folder.exists()) {
            throw FileNotFoundException("install file not found , folder:" + (folder?.canonicalPath))
        }
        // android o sdk_int 26
        // for android o preview sdk_int 25
        if ((Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT != 0)
            || Build.VERSION.SDK_INT > 25
        ) {
            try {
                V25.install(classLoader, folder)
            } catch (throwable: Throwable) {
                // install fail, try to treat it as v23
                // some preview N version may go here
                val msg = "installNativeLibraryPath, v25 fail:" + throwable.message
                NanoLog.e(msg, throwable)
                V23.install(classLoader, folder)
            }
        } else if (Build.VERSION.SDK_INT >= 23) {
            try {
                V23.install(classLoader, folder)
            } catch (throwable: Throwable) {
                // install fail, try to treat it as v14
                val msg = "installNativeLibraryPath, v23 fail:" + throwable.message
                NanoLog.e(msg, throwable)
                V14.install(classLoader, folder)
            }
        } else if (Build.VERSION.SDK_INT >= 14) {
            V14.install(classLoader, folder)
        } else {
            V4.install(classLoader, folder)
        }
        return true
    }

    private object V4 {
        @Throws(Throwable::class)
        fun install(classLoader: ClassLoader, folder: File) {
            val addPath = folder.canonicalPath
            val pathField = ReflectUtils.findField(BaseDexClassLoader::class.java, "libPath")
            val origLibPaths = pathField[classLoader] as String
            val origLibPathSplit =
                origLibPaths.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val newLibPaths = StringBuilder(addPath)

            for (origLibPath in origLibPathSplit) {
                if (origLibPath == null || addPath == origLibPath) {
                    continue
                }
                newLibPaths.append(':').append(origLibPath)
            }
            pathField[classLoader] = newLibPaths.toString()

            val libraryPathElementsFiled = ReflectUtils.findField(
                BaseDexClassLoader::class.java, "libraryPathElements"
            )
            val libraryPathElements = libraryPathElementsFiled[classLoader] as MutableList<String>
            val libPathElementIt = libraryPathElements.iterator()
            while (libPathElementIt.hasNext()) {
                val libPath = libPathElementIt.next()
                if (addPath == libPath) {
                    libPathElementIt.remove()
                    break
                }
            }
            libraryPathElements.add(0, addPath)
            libraryPathElementsFiled[classLoader] = libraryPathElements
        }
    }

    private object V14 {
        @Throws(Throwable::class)
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField = ReflectUtils.findField(BaseDexClassLoader::class.java, "pathList")
            val dexPathList = pathListField[classLoader]

            val nativeLibDirField =
                ReflectUtils.findFieldForObject(dexPathList, "nativeLibraryDirectories")
            val origNativeLibDirs = nativeLibDirField[dexPathList] as Array<File>

            val newNativeLibDirList: MutableList<File> = ArrayList(origNativeLibDirs.size + 1)
            newNativeLibDirList.add(folder)
            for (origNativeLibDir in origNativeLibDirs) {
                if (folder != origNativeLibDir) {
                    newNativeLibDirList.add(origNativeLibDir)
                }
            }
            nativeLibDirField[dexPathList] = newNativeLibDirList.toTypedArray<File>()
        }
    }

    private object V23 {
        @Throws(Throwable::class)
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField = ReflectUtils.findField(BaseDexClassLoader::class.java, "pathList")
            val dexPathList = pathListField[classLoader]

            val nativeLibraryDirectories =
                ReflectUtils.findFieldForObject(dexPathList, "nativeLibraryDirectories")

            var origLibDirs = nativeLibraryDirectories[dexPathList] as MutableList<File>
            if (origLibDirs == null) {
                origLibDirs = ArrayList(2)
            }
            val libDirIt = origLibDirs.iterator()
            while (libDirIt.hasNext()) {
                val libDir = libDirIt.next()
                if (folder == libDir) {
                    libDirIt.remove()
                    break
                }
            }
            origLibDirs.add(0, folder)

            val systemNativeLibraryDirectories = ReflectUtils.findFieldForObject(
                dexPathList,
                "systemNativeLibraryDirectories"
            )
            var origSystemLibDirs = systemNativeLibraryDirectories[dexPathList] as List<File>
            if (origSystemLibDirs == null) {
                origSystemLibDirs = ArrayList(2)
            }

            val newLibDirs: MutableList<File> =
                ArrayList(origLibDirs.size + origSystemLibDirs.size + 1)
            newLibDirs.addAll(origLibDirs)
            newLibDirs.addAll(origSystemLibDirs)

            val makeElements = ReflectUtils.findMethod(
                dexPathList.javaClass,
                "makePathElements",
                MutableList::class.java,
                File::class.java,
                MutableList::class.java
            )
            val suppressedExceptions = ArrayList<IOException>()

            val elements = makeElements.invoke(
                dexPathList,
                newLibDirs,
                null,
                suppressedExceptions
            ) as Array<Any>

            val nativeLibraryPathElements =
                ReflectUtils.findFieldForObject(dexPathList, "nativeLibraryPathElements")
            nativeLibraryPathElements[dexPathList] = elements
        }
    }

    private object V25 {
        @Throws(Throwable::class)
        fun install(classLoader: ClassLoader, folder: File) {
            val pathListField = ReflectUtils.findField(BaseDexClassLoader::class.java, "pathList")
            val dexPathList = pathListField[classLoader]

            val nativeLibraryDirectories =
                ReflectUtils.findFieldForObject(dexPathList, "nativeLibraryDirectories")

            var origLibDirs = nativeLibraryDirectories[dexPathList] as MutableList<File>
            if (origLibDirs == null) {
                origLibDirs = ArrayList(2)
            }
            val libDirIt = origLibDirs.iterator()
            while (libDirIt.hasNext()) {
                val libDir = libDirIt.next()
                if (folder == libDir) {
                    libDirIt.remove()
                    break
                }
            }
            origLibDirs.add(0, folder)
            //
            nativeLibraryDirectories[dexPathList] = origLibDirs

            val systemNativeLibraryDirectories = ReflectUtils.findFieldForObject(
                dexPathList,
                "systemNativeLibraryDirectories"
            )
            var origSystemLibDirs = systemNativeLibraryDirectories[dexPathList] as List<File>
            if (origSystemLibDirs == null) {
                origSystemLibDirs = ArrayList(2)
            }

            val newLibDirs: MutableList<File> =
                ArrayList(origLibDirs.size + origSystemLibDirs.size + 1)
            newLibDirs.addAll(origLibDirs)
            newLibDirs.addAll(origSystemLibDirs)

            val makeElements = ReflectUtils.findMethod(
                dexPathList.javaClass,
                "makePathElements",
                MutableList::class.java
            )

            val elements = makeElements.invoke(dexPathList, newLibDirs) as Array<Any>

            val nativeLibraryPathElements =
                ReflectUtils.findFieldForObject(dexPathList, "nativeLibraryPathElements")
            nativeLibraryPathElements[dexPathList] = elements
        }
    }
}