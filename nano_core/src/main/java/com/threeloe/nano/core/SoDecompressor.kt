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

import com.threeloe.nano.NanoConfig
import com.threeloe.nano.NanoLog
import com.threeloe.nano.NanoResult
import com.threeloe.nano.NanoResult.Companion.obtainFail
import com.threeloe.nano.NanoResult.Companion.obtainSuccess
import com.threeloe.nano.compress.info.CompressInfo
import com.threeloe.nano.compress.info.NanoFileInfo
import com.threeloe.nano.decompress.DecompressFileCallable
import com.threeloe.nano.decompress.DecompressorMethodProvider
import com.threeloe.nano.IDecompressMethod
import com.threeloe.nano.utils.AbiUtils
import com.threeloe.nano.utils.ApkUtils
import com.threeloe.nano.utils.CheckNumUtils.getCheckNum
import com.threeloe.nano.utils.CheckNumUtils.getVerifyFailList
import com.threeloe.nano.utils.FileLocker
import com.threeloe.nano.utils.FileUtils
import com.threeloe.nano.utils.FileUtils.deleteDir
import java.io.File

object SoDecompressor {

    private const val LOCK_FILE_SUFFIX: String = ".lock"

    private lateinit var groupMap: Map<String, List<NanoFileInfo>>

    fun init() {
        val abi = AbiUtils.getRuntimeAbi(NanoConfig.get().app)
        val soFileInfoList = CompressInfo.getSoFileInfoList(abi)
        groupMap = soFileInfoList.groupBy { it.group }
        if (NanoManager.isApkUpdated()) {
            val lastVersionWorkDir = NanoManager.lastVersionWorkDir
            if (lastVersionWorkDir.exists()) {
                val lastVersionLibs =
                    lastVersionWorkDir.listFiles { _, name -> name.endsWith(ApkUtils.FILE_SUFFIX_SO) }
                        ?.map {
                            val fileInfo = NanoFileInfo()
                            fileInfo.checkNum = getCheckNum(it)
                            fileInfo.name = it.name
                            fileInfo
                        }?.toSet() ?: emptySet()

                val workDir = NanoManager.currentVersionWorkDir
                soFileInfoList.forEach { soFile ->
                    if (soFile in lastVersionLibs) {
                        FileUtils.moveFileFromDir(
                            lastVersionWorkDir, workDir, soFile.name!!
                        )
                    }
                }
                deleteDir(lastVersionWorkDir)
            }
        }
    }


    fun decompress(group: String): NanoResult {
        val groupNames = groupMap.keys
        if (groupNames.contains(group).not()) {
            val msg = "group: $group not found, available groups: $groupNames"
            NanoLog.e(msg)
            return obtainFail(IllegalArgumentException(msg))
        }
        val workDir = NanoManager.currentVersionWorkDir
        val locker = FileLocker(File(workDir, group + LOCK_FILE_SUFFIX))
        try {
            locker.lock()
            val verifyFailList = getVerifyFailList(workDir, groupMap[group] ?: emptyList())
            if (verifyFailList.isEmpty()) {
                return obtainSuccess()
            }
            val decompressMethod =
                DecompressorMethodProvider.getDecompressMethod(CompressInfo.getCompressMethod())
            verifyFailList.forEach { info ->
                NanoLog.i("Nano fileInfo: $info")
            }
            decompressInternal(verifyFailList, workDir, decompressMethod)
            //check again
            val verifyAgainList = getVerifyFailList(workDir, verifyFailList)
            if(verifyAgainList.isEmpty()){
                NanoManager.setDecompressionFinished(group, true)
                return obtainSuccess()
            } else {
                return obtainFail(
                    IllegalStateException("Decompress failed, some files are still invalid: $verifyAgainList")
                )
            }
        } catch (e: Throwable) {
            NanoLog.e("Decompression failed for group: $group", e)
            return obtainFail(e)
        } finally {
            locker.unlock()
        }
    }


    private fun decompressInternal(
        originFileInfoList: List<NanoFileInfo>?,
        outputDir: File,
        decompressMethod: IDecompressMethod
    ) {
        if (originFileInfoList.isNullOrEmpty()) {
            NanoLog.i("no so files need to be decompressed.")
            return
        }
        val compressDataMap = originFileInfoList.groupBy { it.compressedFileName }

        val decompressResults = compressDataMap.map { (compressedName, fileInfoList) ->
            NanoLog.d("decompress file: $fileInfoList")
            NanoConfig.get().threadPool.submit(
                DecompressFileCallable(
                    compressedName, decompressMethod, outputDir, fileInfoList
                )
            )
        }
        decompressResults.forEach { it.get() }
    }


}
