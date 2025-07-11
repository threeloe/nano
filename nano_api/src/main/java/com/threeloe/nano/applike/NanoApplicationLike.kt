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

package com.threeloe.nano.applike

import android.app.Application
import android.content.Context
import android.content.res.Configuration

abstract class NanoApplicationLike(val application: Application) : ApplicationLifeCycle {
    override fun attachBaseContext(base: Context?) {
    }

    override fun onCreate() {
    }

    override fun onLowMemory() {
    }

    override fun onTrimMemory(level: Int) {
    }

    override fun onTerminate() {
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
    }
}