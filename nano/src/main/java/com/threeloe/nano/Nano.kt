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

package com.threeloe.nano

import android.app.Application
import android.content.pm.ApplicationInfo
import com.threeloe.nano.core.NanoCore
import com.threeloe.nano.core.installer.NanoInstaller


object Nano : NanoApi {

    private var nanoApi: NanoApi = NanoCore()

    override fun init(application: Application, config: NanoConfig?) {
        nanoApi.init(application, config)
    }

    override fun decompress(group: String): NanoResult {
        return nanoApi.decompress(group)
    }

    override fun decompressAsync(group: String, callback: (NanoResult) -> Unit) {
        nanoApi.decompressAsync(group, callback)
    }

    @JvmStatic
    fun install(cl: ClassLoader, appInfo: ApplicationInfo): ClassLoader {
        return NanoInstaller.newClassLoader(cl, appInfo)
    }

    @JvmStatic
    fun install(app: Application): ClassLoader {
        return NanoInstaller.install(app)
    }
}
