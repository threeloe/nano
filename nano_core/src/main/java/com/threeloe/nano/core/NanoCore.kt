/*
 * Copyright (C) 2025 ThreeLoe
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.threeloe.nano.core

import android.app.Application
import com.threeloe.nano.NanoApi
import com.threeloe.nano.NanoConfig
import com.threeloe.nano.NanoLog
import com.threeloe.nano.NanoResult

class NanoCore : NanoApi {

    override fun init(application: Application, config: NanoConfig?) {
        var newConfig = config
        if (newConfig == null) {
            newConfig = NanoConfig()
        }
        newConfig.application(application)
        NanoConfig.set(newConfig)
        //init nano manager
        NanoManager.init(application.applicationInfo)
        NanoManager.configSP(newConfig.preferences)
        //init SoDecompressor
        SoDecompressor.init()
    }

    override fun decompress(group: String):NanoResult {
        val start = System.currentTimeMillis()
        var result = SoDecompressor.decompress(group)
        if (!result.isSuccess) {
            return result
        }
        NanoLog.i( "decompress successfully, duration: ${System.currentTimeMillis() - start} ms")
        return result
    }

    override fun decompressAsync(group: String, callback: (NanoResult) -> Unit) {
        NanoConfig.get().threadPool.submit {
            val result = decompress(group)
            callback(result)
        }
    }


}
