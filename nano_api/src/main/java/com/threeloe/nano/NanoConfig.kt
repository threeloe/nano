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
import android.content.SharedPreferences
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory

class NanoConfig {

    companion object {
        const val SP_NANE = "nano-sp"
        const val THREAD_NAME_PREFIX = "nano-thread-"

        private lateinit var sInstance: NanoConfig
        fun set(nanoConfig: NanoConfig) {
            sInstance = nanoConfig
        }
        fun get(): NanoConfig {
            return sInstance
        }
    }

    lateinit var app: Application

    private var mExecutorService: ScheduledExecutorService? = null

    private var mPreferences: SharedPreferences? = null

    private var mZstdDecompressMethod: IDecompressMethod? = null
    private var mXzDecompressMethod: IDecompressMethod? = null

    fun application(application: Application): NanoConfig {
        this.app = application
        return this
    }

    fun threadPool(executorService: ScheduledExecutorService): NanoConfig {
        mExecutorService = executorService
        return this
    }

    fun preferences(preferences: SharedPreferences): NanoConfig {
        mPreferences = preferences
        return this
    }

    fun zstdCompressMethod( decompressMethod: IDecompressMethod): NanoConfig {
        mZstdDecompressMethod = decompressMethod
        return this
    }

    fun xzCompressMethod( decompressMethod: IDecompressMethod): NanoConfig {
        mXzDecompressMethod = decompressMethod
        return this
    }


    val threadPool: ScheduledExecutorService
        get() {
            if (mExecutorService == null) {
                mExecutorService = Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors() -1, object : ThreadFactory {
                    private var threadCount = 0
                    @Synchronized
                    override fun newThread(r: Runnable): Thread {
                        return Thread(r, THREAD_NAME_PREFIX + threadCount++)
                    }
                })
            }
            return mExecutorService!!
        }

    val preferences: SharedPreferences
        get() {
            if (mPreferences == null) {
                mPreferences = app.getSharedPreferences(SP_NANE, Context.MODE_PRIVATE)
            }
            return mPreferences!!
        }

    val zstdDecompressMethod: IDecompressMethod?
        get() {
            return mZstdDecompressMethod
        }

    val xzDecompressMethod: IDecompressMethod?
        get() {
            return mXzDecompressMethod
        }

}
