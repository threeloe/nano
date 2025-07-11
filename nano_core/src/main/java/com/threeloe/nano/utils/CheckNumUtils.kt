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

import com.threeloe.nano.NanoLog
import com.threeloe.nano.compress.info.NanoFileInfo
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream


object CheckNumUtils {

    private const val BUFFER_SIZE: Int = 32768
    /**
     * Get a list of files that have failed verification.
     * A file fails verification if its calculated check number does not match the expected check number.
     */
    fun getVerifyFailList(workDir: File, list: List<NanoFileInfo>): List<NanoFileInfo> {
        return list.filter { fileInfo ->
            val checkNumber = getCheckNum(File(workDir, fileInfo.name))
            checkNumber != fileInfo.checkNum
        }
    }


    /**
     * Calculate a check number for the file.
     * The check number is the sum of the file's checksum and its length.
     * If the file does not exist, it returns 0.
     */
    fun getCheckNum(file: File): Long {
        if (!file.exists()) {
            NanoLog.w("File does not exist: ${file.path}")
            return 0
        }
        return try {
            val checkSum = calFileCheckSum(file)
            val length = file.length()
            checkSum + length
        } catch (e: IOException) {
            NanoLog.e("Failed to generate check number", e)
            0
        }
    }

    /**
     * Calculate the checksum of a file using CRC32.
     * This method reads the file in chunks to avoid memory issues with large files.
     */
    private fun calFileCheckSum(file: File): Long {
        val buf = ByteArray(BUFFER_SIZE)
        CheckedInputStream(FileInputStream(file), CRC32()).use { checkedInputStream ->
            while (checkedInputStream.read(buf) >= 0) {
                // Reading file to calculate checksum
            }
            return checkedInputStream.checksum.value
        }
    }
}