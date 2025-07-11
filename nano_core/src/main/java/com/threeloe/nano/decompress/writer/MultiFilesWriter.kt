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

package com.threeloe.nano.decompress.writer

import com.threeloe.nano.compress.info.NanoFileInfo
import com.threeloe.nano.utils.FileUtils.createFile
import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * a compressed file contains multiple files ,
 *  this class can write these original files to one output directory
 */
class MultiFilesWriter(outputDir: File, fileInfoList: List<NanoFileInfo>) : Closeable {

    private val beginPos: Long
    private var curIndex = 0
    private val writers: List<NanoFileWriter>

    init {
        require(fileInfoList.isNotEmpty()) { "fileInfoList must not be empty" }
        val sortedFileInfoList = fileInfoList.sortedBy { it.beginPos }
        beginPos = sortedFileInfoList.first().beginPos
        writers = sortedFileInfoList.map { fileInfo ->
            val targetFile = File(outputDir, fileInfo.name).apply { createFile(this) }
            MMapFileWriter(
                targetFile,
                fileInfo.fileSize,
                fileInfo.beginPos
            )
        }
    }

    fun write(inputStream: InputStream) {
        val buffer = ByteArray(32768)
        var currentPosition = beginPos
        //skip to the first file's begin position
        val skipped = inputStream.skip(beginPos)
        if (skipped < beginPos) {
            throw IOException("Failed to skip to the required position in the input stream.")
        }
        while (true) {
            val bytesRead = inputStream.read(buffer)
            // End of stream
            if (bytesRead == -1) break

            var bufferOffset = 0
            while (bufferOffset < bytesRead) {
                //all files are written
                if (curIndex >= writers.size) return

                val writer = writers[curIndex]
                when {
                    currentPosition in writer.beginPos until writer.endPos -> {
                        val writeLength = minOf(
                            bytesRead - bufferOffset,
                            (writer.endPos - currentPosition).toInt()
                        )
                        writer.write(buffer, bufferOffset, writeLength)
                        bufferOffset += writeLength
                        currentPosition += writeLength
                    }
                    currentPosition >= writer.endPos -> {
                        // switch to the next writer
                        curIndex++
                    }
                    else -> {
                        val skipLength = writer.beginPos - currentPosition
                        inputStream.skip(skipLength)
                        currentPosition += skipLength
                    }
                }
            }
        }
    }


    override fun close() {
        writers.forEach { it.close() }
    }
}

