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

package com.threeloe.nano.utils

import android.content.Context
import android.os.Build
import android.os.Process
import android.util.Log

object AbiUtils {

    private const val TAG = "AbiUtils"

    const val ARMEABI = "armeabi"
    const val ARMEABI_V7A = "armeabi-v7a"
    const val ARM64_V8A = "arm64-v8a"
    const val X86 = "x86"
    const val X86_64 = "x86_64"
    const val MIPS = "mips"
    const val MIPS64 = "mips64"

    private val CPU_TYPES = mapOf(
        ARMEABI to 32,
        ARMEABI_V7A to 32,
        ARM64_V8A to 64,
        X86 to 32,
        X86_64 to 64,
        MIPS to 32,
        MIPS64 to 64
    )

    @Volatile
    private var sRuntimeAbi: String? = null


    fun getRuntimeAbi(context: Context): String {
        if (sRuntimeAbi == null) {
            synchronized(AbiUtils::class.java) {
                if (sRuntimeAbi == null) {
                    sRuntimeAbi = inferAbiFromCPUAbi(context) ?: inferAbiFromApk(context)
                }
            }
        }
        return sRuntimeAbi!!
    }


    private fun inferAbiFromCPUAbi(context: Context): String? {
        try {
            val appInfo = context.applicationInfo
            val primaryCpuAbi =
                ReflectUtils.findFieldForObject(appInfo, "primaryCpuAbi").get(appInfo) as? String
            if (primaryCpuAbi != null) {
                val processBit = getProcessBit()
                if (processBit == 0 || CPU_TYPES[primaryCpuAbi] == processBit) {
                    return primaryCpuAbi
                }
            }
        } catch (e: Throwable) {
            Log.e(TAG, "inferAbiFromCPUAbi exception!", e)
        }
        return null
    }

    private fun getProcessBit(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Process.is64Bit()){
                64
            }else {
                32
            }
        } else {
            0
        }
    }


    private fun inferAbiFromApk(context: Context): String {
        return try {
            val hostApkABIs = ApkUtils.getAbiSet(context).filter {
                CPU_TYPES.containsKey(it)
            }.toSet()
            if (hostApkABIs.isEmpty()) {
                Log.w(
                    TAG,
                    "inferAbiFromApk: not found any abi in apk, using default abi=${Build.SUPPORTED_ABIS[0]}"
                )
                return Build.SUPPORTED_ABIS[0]
            }
            Build.SUPPORTED_ABIS.firstOrNull { hostApkABIs.contains(it) } ?: Build.SUPPORTED_ABIS[0]
        } catch (e: Exception) {
            Log.e(TAG, "inferAbiFromApk exception!", e)
            Build.SUPPORTED_ABIS[0]
        }
    }


}
