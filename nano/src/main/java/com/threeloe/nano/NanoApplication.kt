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
import android.content.Context
import android.content.res.Configuration
import com.threeloe.nano.utils.ReflectUtils

abstract class NanoApplication : Application() {

    private lateinit var applicationLike: Any

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        val newClassLoader = Nano.install(this)
        applicationLike = newClassLoader.loadClass(getApplicationLikeClassName())
            .getConstructor(Application::class.java).newInstance(this)
        ReflectUtils.findMethod(
            applicationLike::class.java,
            "attachBaseContext",
            Context::class.java
        ).invoke(applicationLike, base)
    }


    abstract fun getApplicationLikeClassName(): String

    override fun onCreate() {
        super.onCreate()
        ReflectUtils.findMethod(applicationLike::class.java, "onCreate").invoke(applicationLike)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        ReflectUtils.findMethod(applicationLike::class.java, "onLowMemory").invoke(applicationLike)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        ReflectUtils.findMethod(applicationLike::class.java, "onTrimMemory", Int::class.java)
            .invoke(applicationLike, level)
    }

    override fun onTerminate() {
        super.onTerminate()
        ReflectUtils.findMethod(applicationLike::class.java, "onTerminate").invoke(applicationLike)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        ReflectUtils.findMethod(
            applicationLike::class.java,
            "onConfigurationChanged",
            Configuration::class.java
        ).invoke(applicationLike, newConfig)
    }

}