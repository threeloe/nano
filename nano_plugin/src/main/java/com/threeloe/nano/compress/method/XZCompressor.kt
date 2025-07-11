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

import com.threeloe.nano.NanoConstants
import org.tukaani.xz.ARMThumbOptions
import org.tukaani.xz.LZMA2Options
import org.tukaani.xz.XZ
import org.tukaani.xz.XZOutputStream
import java.io.InputStream
import java.io.OutputStream


class XZCompressor : BaseCompressMethod<CompressOption>() {

    companion object {
        private const val DEFAULT_XZ_LEVEL = 6
    }

    override fun compress(inputStream: InputStream, outputStream: OutputStream) {
        val options = LZMA2Options().apply {
            setPreset(mOption?.level ?: DEFAULT_XZ_LEVEL)
        }
        inputStream.use { input ->
            XZOutputStream(
                outputStream,
                arrayOf(ARMThumbOptions(), options),
                XZ.CHECK_CRC32
            ).use { xzOut ->
                val buffer = ByteArray(NanoConstants.BUFFER_SIZE)
                while (true) {
                    val size = input.read(buffer)
                    if (size == -1) break
                    xzOut.write(buffer, 0, size)
                }
            }
        }
    }

}