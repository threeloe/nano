/*
 *
 *  * Copyright (C) 2025 ThreeLoe
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.threeloe.nano.utils

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.RandomAccessFile
import java.util.regex.Pattern


object NanoFileUtils {

    private val SO_FILE_PATTERN = Pattern.compile("lib/([^/]+)/([^/]+\\.so)")

    fun getAbi(input: String): String? {
        return SO_FILE_PATTERN.matcher(input).takeIf { it.matches() }?.group(1)
    }

    fun readFileRandomAccess(f: File, off: Long, length: Long): ByteArray {
        val buffer = ByteArrayOutputStream()
        RandomAccessFile(f, "r").use { raf ->
            raf.seek(off)
            val chunkSize = 8192
            var remaining = length
            val tempBuffer = ByteArray(chunkSize.coerceAtMost(length.toInt()))
            while (remaining > 0) {
                val bytesRead = raf.read(tempBuffer, 0, tempBuffer.size.coerceAtMost(remaining.toInt()))
                if (bytesRead == -1) break
                buffer.write(tempBuffer, 0, bytesRead)
                remaining -= bytesRead
            }
        }
        return buffer.toByteArray()
    }


    fun groupFilesEvenly(files: List<File>, blockNum: Int): Set<List<File>> {
        if (blockNum <= 1) return setOf(files)
        if (blockNum >= files.size) return files.map { listOf(it) }.toSet()

        // sorted by file length
        val sortedFiles = files.sortedByDescending { it.length() }
        // initialize bins
        val bins = Array(blockNum) { mutableListOf<File>() }
        val binSizes = LongArray(blockNum) { 0L }

        //  greedy allocation
        for (file in sortedFiles) {
            // find the bin with the smallest total size
            val minIndex = binSizes.withIndex().minByOrNull { it.value }!!.index
            bins[minIndex].add(file)
            binSizes[minIndex] += file.length()
        }
        return bins.toSet()
    }
}