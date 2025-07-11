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

package com.threeloe.nano.core

import com.threeloe.nano.NanoContext
import com.threeloe.nano.NanoPlugin
import com.threeloe.nano.compress.CompressHelper
import com.threeloe.nano.compress.info.CompressBlock
import com.threeloe.nano.compress.info.CompressInfoWriter
import com.threeloe.nano.utils.AGPCompat
import com.threeloe.nano.utils.DexUtils
import com.threeloe.nano.utils.NanoFileUtils
import org.gradle.api.GradleException
import java.io.File

class SoCompressor(private val context: NanoContext) {

    companion object {
        private const val SO_FILE_SUFFIX = "so"
        private const val FILE_NAME_SUFFIX_SO = ".so"
    }

    private val compressBlockMap = mutableMapOf<String, MutableList<CompressBlock>>()
    private val soOutputDir = getSoDir().first()

    private val writer = CompressInfoWriter(context)

    fun compress() {
        //compress so files
        compressInternal()

        // write compress info
        writeCompressInfo()
    }

    private fun writeCompressInfo() {
        val dex = DexUtils.findClassTargetDex(
            context.project, context.variantName, CompressInfoWriter.CLASS_COMPRESS_INFO_READER
        )
        if (dex == null) {
            throw GradleException(" ${CompressInfoWriter.CLASS_COMPRESS_INFO_READER} not found in all dex files!")
        }
        writer.write(
            compressBlockMap, dex
        )
    }

    private fun compressInternal() {

        compressBlockMap.clear()
        collectSoFiles().forEach { (abi, groups) ->
            groups.forEach { (groupName, files) ->
                processGroupFiles(abi, groupName, files)
            }
        }
    }

    private fun processGroupFiles(abi: String, groupName: String, files: List<File>) {
        val outputPath = getOutputPath(abi)
        val suffix = getFileSuffix()
        val blockSet = NanoFileUtils.groupFilesEvenly(files, getBlockNum(groupName))
        blockSet.forEachIndexed { index, fileList ->
            if (fileList.isNotEmpty()) {
                compressBlock(fileList, outputPath, groupName, index + 1, suffix, abi)
            }
        }
    }

    private fun compressBlock(
        fileList: List<File>,
        outputPath: File,
        groupName: String,
        blockIndex: Int,
        suffix: String,
        abi: String
    ) {
        val blockInfo = CompressHelper.compressToBlock(
            fileList,
            outputPath,
            context.options.getCompressMethod(),
            groupName,
            "lib${groupName}_$blockIndex" + suffix,
        )
        blockInfo?.let {
            compressBlockMap.computeIfAbsent(abi) { mutableListOf() }.add(it)
        }
        fileList.forEach(File::delete)
    }

    private fun getOutputPath(abi: String): File {
        return  File(soOutputDir, "lib/$abi")
    }

    private fun getFileSuffix(): String {
        return FILE_NAME_SUFFIX_SO
    }

    private fun getBlockNum(groupName: String): Int {
        return context.options.groups.findByName(groupName)?.blockNum ?: 1
    }

    private fun collectSoFiles(): Map<String, MutableMap<String, MutableList<File>>> {
        val soFiles = mutableMapOf<String, MutableMap<String, MutableList<File>>>()
        getSoDir().forEach { soDir ->
            soDir.walk().filter { it.isFile && it.extension == SO_FILE_SUFFIX }
                .forEach { file ->
                    val abi = NanoFileUtils.getAbi(file.toRelativeString(soDir))
                    val groupName = getGroupNameByFile(file)
                    if (abi != null && groupName != null) {
                        soFiles.computeIfAbsent(abi) { mutableMapOf() }
                            .computeIfAbsent(groupName) { mutableListOf() }.add(file)
                    }
                }
        }
        if (soFiles.isEmpty()) {
            println("[${NanoPlugin.TAG}] No valid .so files found in the specified directories.")
        }
        return soFiles
    }

    private fun getSoDir(): Set<File> = AGPCompat.getSoDir(context.project, context.variantName)

    private fun getGroupNameByFile(file: File): String? {
        return context.options.groups.firstOrNull { group ->
            group.getIncludes().contains(file.name)
        }?.name
    }
}
