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

package com.threeloe.nano.compress.info

import com.threeloe.nano.utils.AbiUtils

/**
 * do not move or rename this class,
 * it is used for build time instrumentation in Nano plugin
 */
object CompressInfo {


    fun isReplaceClassLoader(): Boolean {
        return hookLoadLibraryMode() == 0
    }

    private fun hookLoadLibraryMode(): Int {
        return 0
    }

    fun getCompressMethod(): Int {
        return 0
    }

    fun getSoFileInfoList(abi :String): List<NanoFileInfo> {
        return when (abi) {
            AbiUtils.ARMEABI-> getSoFileInfoListForArmeabi()
            AbiUtils.ARMEABI_V7A -> getSoFileInfoListForArmeabiV7a()
            AbiUtils.ARM64_V8A -> getSoFileInfoListForArm64V8a()
            AbiUtils.X86 -> getSoFileInfoListForX86()
            AbiUtils.X86_64 -> getSoFileInfoListForX86_64()
            AbiUtils.MIPS -> getSoFileInfoListForMips()
            AbiUtils.MIPS64 -> getSoFileInfoListForMips64()
            else -> throw IllegalArgumentException("Unsupported ABI: $abi")
        }
    }

    private fun getSoFileInfoListForArmeabi(): List<NanoFileInfo> {
        /**
         * build time instrumentation for armeabi
         */
        return ArrayList()
    }

    private fun getSoFileInfoListForArmeabiV7a(): List<NanoFileInfo> {
        /**
         * build time instrumentation for armeabi-v7a
         */
        return ArrayList()
    }

    private fun getSoFileInfoListForArm64V8a(): List<NanoFileInfo> {
        /**
         * build time instrumentation for arm64-v8a
         */
        return ArrayList()
    }

    private fun getSoFileInfoListForX86(): List<NanoFileInfo> {
        /**
         * build time instrumentation for x86
         */
        return ArrayList()
    }

    private fun getSoFileInfoListForX86_64(): List<NanoFileInfo> {
        /**
         * build time instrumentation for x86_64
         */
        return ArrayList()
    }

    private fun getSoFileInfoListForMips(): List<NanoFileInfo> {
        /**
         * build time instrumentation for mips
         */
        return ArrayList()
    }

    private fun getSoFileInfoListForMips64(): List<NanoFileInfo> {
        /**
         * build time instrumentation for mips64
         */
        return ArrayList()
    }


}
