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

package com.threeloe.nano.core.installer

import android.app.Application
import android.content.pm.ApplicationInfo
import com.threeloe.nano.classloader.NanoClassLoaderFactory
import com.threeloe.nano.classloader.NanoClassLoaderInjector.inject
import com.threeloe.nano.compress.info.CompressInfo
import com.threeloe.nano.core.NanoManager

object NanoInstaller {

    fun install(application: Application): ClassLoader {
        NanoManager.init(application.applicationInfo)
        val workDir = NanoManager.currentVersionWorkDir
        val oldClassLoader = application.javaClass.classLoader
        if (CompressInfo.isReplaceClassLoader()) {
            return inject(application, oldClassLoader, workDir)
        } else {
            SoDirInstaller.installNativeLibraryPath(oldClassLoader, workDir)
            return oldClassLoader
        }
    }


    fun newClassLoader(cl: ClassLoader, appInfo: ApplicationInfo): ClassLoader {
        return NanoClassLoaderFactory.createNewClassLoader(cl, appInfo)
    }
}
