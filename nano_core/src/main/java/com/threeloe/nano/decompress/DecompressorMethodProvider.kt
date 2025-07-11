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

import com.threeloe.nano.IDecompressMethod
import com.threeloe.nano.NanoConfig

object DecompressorMethodProvider {

    private const val COMPRESS_METHOD_ZSTD: Int = 1
    private const val COMPRESS_METHOD_XZ: Int = 2

    fun getDecompressMethod(method: Int): IDecompressMethod {
        if (method == COMPRESS_METHOD_ZSTD) {
            val compressMethod = NanoConfig.get().zstdDecompressMethod
            if (compressMethod != null) {
                return compressMethod
            } else {
                throw IllegalStateException("zstdDecompressMethod is not set in NanoConfig")
            }
        } else if (method == COMPRESS_METHOD_XZ) {
            val compressMethod = NanoConfig.get().xzDecompressMethod
            if (compressMethod != null) {
                return compressMethod
            } else {
                throw IllegalStateException("xzDecompressMethod is not set in NanoConfig")
            }
        } else {
            throw IllegalArgumentException("Unsupported compression method: $method")
        }
    }

}
