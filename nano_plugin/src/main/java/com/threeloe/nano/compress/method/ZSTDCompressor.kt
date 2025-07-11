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

package com.threeloe.nano.compress.method

import com.github.luben.zstd.ZstdOutputStream
import com.threeloe.nano.NanoConstants
import java.io.InputStream
import java.io.OutputStream

class ZSTDCompressor : BaseCompressMethod<CompressOption>() {

    companion object {
        private const val DEFAULT_ZSTD_LEVEL = 19
    }

    override fun compress(inputStream: InputStream, outputStream: OutputStream) {
        val level = mOption?.level ?: DEFAULT_ZSTD_LEVEL
        inputStream.use { input ->
            ZstdOutputStream(outputStream, level).use { zstdOut ->
                val buffer = ByteArray(NanoConstants.BUFFER_SIZE)
                while (true) {
                    val size = input.read(buffer)
                    if (size == -1) break
                    zstdOut.write(buffer, 0, size)
                }
            }
        }
    }

}