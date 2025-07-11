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

import com.threeloe.nano.NanoConfig
import com.threeloe.nano.NanoLog
import com.threeloe.nano.utils.FileUtils.createFile
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

open class MMapFileWriter(
    file: File,
    fileSize: Long,
    beginOffset: Long
) : NanoFileWriter(beginOffset, fileSize){

    private val randomAccessFile: RandomAccessFile
    private val fileChannel: FileChannel
    private val mappedByteBuffer: MappedByteBuffer

    init {
        try {
            createFile(file)
            randomAccessFile = RandomAccessFile(file, "rw").apply {
                if (length() != fileSize) {
                    setLength(fileSize)
                }
            }
            fileChannel = randomAccessFile.channel
            mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_WRITE, 0, fileSize)
        } catch (e: IOException) {
            throw RuntimeException("Failed to initialize MMapFileWriter", e)
        }
    }


    override fun write(data: ByteArray, offset: Int, length: Int) {
        mappedByteBuffer.put(data, offset, length)
    }

    override fun write(data: ByteArray) {
        mappedByteBuffer.put(data)
    }

    override fun close() {
        try {
            fileChannel.close()
            randomAccessFile.close()
            force(mappedByteBuffer)
        } catch (e: IOException) {
            NanoLog.e("Exception while closing MMapFileWriter", e)
        }
    }

    private fun force(buffer: MappedByteBuffer?) {
        NanoConfig.get().threadPool.execute {
            try {
                buffer?.force()
            } catch (t: Throwable) {
                NanoLog.e("Exception while forcing data to disk!", t)
            }
        }
    }


}
