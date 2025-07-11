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

import com.threeloe.nano.compress.info.NanoFileInfo
import com.threeloe.nano.IDecompressMethod
import java.io.File
import java.util.concurrent.Callable

class DecompressFileCallable(
    private val compressedName: String,
    private val decompressMethod: IDecompressMethod,
    private val outputDir: File,
    private val fileInfoList: List<NanoFileInfo>
) : Callable<Any> {

    companion object {
        const val MAX_RETRY_COUNT = 3
    }

    private var retryCount = 0

    @Throws(Exception::class)
    override fun call(): Any {
        decompress()
        return Any()
    }

    private fun decompress() {
        try {
            DecompressUtils.decompressFile(
                compressedName,
                fileInfoList,
                outputDir,
                decompressMethod
            )
        } catch (t: Throwable) {
            if (retryCount < MAX_RETRY_COUNT) {
                retryCount++
                decompress()
            } else {
                throw RuntimeException(
                    "decompress $compressedName fail after retry $MAX_RETRY_COUNT times", t
                )
            }
        }
    }



}
