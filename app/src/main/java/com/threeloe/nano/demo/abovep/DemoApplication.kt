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

package com.threeloe.nano.demo.abovep

import android.app.Application
import android.content.Context
import com.github.luben.zstd.ZstdInputStream
import com.tencent.mmkv.MMKV
import com.threeloe.nano.IDecompressMethod
import com.threeloe.nano.Nano
import com.threeloe.nano.NanoConfig
import org.tukaani.xz.XZInputStream
import java.io.InputStream

class DemoApplication :Application() {

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val nanoConfig = NanoConfig()
        nanoConfig.zstdCompressMethod(object : IDecompressMethod {
            override fun getDecompressedStream(input: InputStream): InputStream {
                return ZstdInputStream(input)
            }
        }).xzCompressMethod(object : IDecompressMethod {
            override fun getDecompressedStream(input: InputStream): InputStream {
                return XZInputStream(input)
            }
        })
        Nano.init(this, nanoConfig)
        Nano.decompress("launch")

        //Business logic starts here
    }

    override fun onCreate() {
        super.onCreate()
        MMKV.initialize(
            this
        )
        //Business logic
    }
}