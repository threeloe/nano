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

import java.io.File

object FileUtils {

    fun createFile(file: File) {
        if (!file.exists()) {
            file.parentFile?.apply {
                if (!exists()) mkdirs()
                setWritable(true)
                setReadable(true)
            }
            file.createNewFile()
        }
    }


    fun deleteDir(file: File?) {
        file?.takeIf { it.exists() }?.apply {
            if (isDirectory) {
                listFiles()?.forEach { deleteDir(it) }
            }
            delete()
        }
    }

    fun AutoCloseable.closeQuietly() {
        try {
            this.close()
        } catch (ignored: Exception) {

        }
    }

    fun moveFileFromDir(srcDir: File, destDir: File, name: String): Boolean {
        val src = File(srcDir, name)
        val dest = File(destDir, name)
        return src.renameTo(dest)
    }
}