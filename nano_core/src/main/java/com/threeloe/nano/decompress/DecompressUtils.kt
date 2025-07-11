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

package com.threeloe.nano.decompress

import com.threeloe.nano.NanoConfig
import com.threeloe.nano.NanoLog
import com.threeloe.nano.compress.info.NanoFileInfo
import com.threeloe.nano.IDecompressMethod
import com.threeloe.nano.decompress.writer.MultiFilesWriter
import com.threeloe.nano.utils.ApkUtils
import com.threeloe.nano.utils.FileUtils.closeQuietly
import java.io.File
import java.io.InputStream

object DecompressUtils {
    private const val BUFFER_SIZE: Int = 32768

    fun decompressFile(
        compressedFileName: String,
        fileInfoList: List<NanoFileInfo>,
        outputDir: File,
        decompressMethod: IDecompressMethod
    ) {
        var decompressedStream: InputStream? = null
        val writer = MultiFilesWriter(outputDir, fileInfoList)
        try {
            val start = System.currentTimeMillis()
            // Get the decompressed stream from the compressed file
            decompressedStream = getDecompressedStream(compressedFileName, decompressMethod)
            //write the decompressed data to the output directory
            writer.write(decompressedStream)
            NanoLog.i(
                "Decompress file: $compressedFileName, duration: ${System.currentTimeMillis() - start}"
            )
        } finally {
            decompressedStream?.closeQuietly()
            writer.close()
        }
    }

    private fun getDecompressedStream(
        compressedFileName: String, decompressMethod: IDecompressMethod
    ): InputStream {
        val inputStream = ApkUtils.getCompressedFileStream(NanoConfig.get().app, compressedFileName)
            ?: throw IllegalStateException("Compressed file not found: $compressedFileName")
        val decompressedStream = decompressMethod.getDecompressedStream(inputStream)
        return decompressedStream
    }

}