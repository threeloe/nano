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

package com.threeloe.nano.compress

import com.threeloe.nano.compress.info.CompressBlock
import com.threeloe.nano.compress.info.NanoFileInfo
import com.threeloe.nano.compress.method.CompressMethodParam
import com.threeloe.nano.utils.CheckNumUtils
import com.threeloe.nano.utils.NanoFileUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object CompressHelper {

    fun compressToBlock(
        inputFiles: List<File>,
        outputDir: File,
        compressMethod: CompressMethodParam,
        group: String,
        blockName: String,
    ): CompressBlock? {
        if (inputFiles.isEmpty()) {
            return null
        }
        val compressor = CompressMethodProvider.getCompressMethod(compressMethod)
        val blockInfo =
            calculateBlockInfo(inputFiles, group, blockName)
        val bos = ByteArrayOutputStream()
        blockInfo.originFileList.forEach { f ->
            val content = NanoFileUtils.readFileRandomAccess(f.file, 0, f.fileSize)
            bos.write(content)
        }
        val out = FileOutputStream(File(outputDir, blockInfo.name))
        compressor.compress(ByteArrayInputStream(bos.toByteArray()), out)
        return blockInfo
    }

    private fun calculateBlockInfo(
        filesToCompress: List<File>, group: String, blockName : String
    ): CompressBlock {
        val block = CompressBlock()
        block.name = blockName
        filesToCompress.forEach { file ->
            val fileInfo = NanoFileInfo()
            fileInfo.name = file.name
            fileInfo.compressedFileName = block.name
            fileInfo.group = group
            fileInfo.checkNum = CheckNumUtils.getCheckNum(file)
            fileInfo.beginPos = 0L
            fileInfo.endPos = file.length()
            fileInfo.file = file
            fileInfo.fileSize = file.length()
            block.originFileList.add(fileInfo)
        }
        return block
    }

}