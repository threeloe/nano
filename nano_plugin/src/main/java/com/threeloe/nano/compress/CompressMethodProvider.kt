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

package com.threeloe.nano.compress

import com.threeloe.nano.compress.method.BaseCompressMethod
import com.threeloe.nano.compress.method.CompressMethodParam
import com.threeloe.nano.compress.method.CompressMethodParam.XZ
import com.threeloe.nano.compress.method.CompressMethodParam.ZSTD
import com.threeloe.nano.compress.method.CompressOption
import com.threeloe.nano.compress.method.XZCompressor
import com.threeloe.nano.compress.method.ZSTDCompressor
import java.util.Locale

object CompressMethodProvider {

    fun getCompressMethod(methodEnum: CompressMethodParam): BaseCompressMethod<CompressOption> {
        val compressor = when (methodEnum) {
            XZ -> XZCompressor()
            ZSTD -> ZSTDCompressor()
        }
        compressor.setOption(CompressOption(methodEnum.level))
        return compressor
    }

    fun getCompressMethodParam(name: String, level: Int?): CompressMethodParam {
        return when (name.uppercase(Locale.ROOT)) {
            ZSTD.name -> {
                ZSTD.level = level
                ZSTD
            }
            XZ.name -> {
                XZ.level = level
                XZ
            }
            else -> throw IllegalArgumentException(
                "Unsupported compress method: $name. Supported methods are: ${CompressMethodParam.values().joinToString(", ")}"
            )
        }
    }

}