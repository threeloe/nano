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

package com.threeloe.nano.utils

import com.threeloe.nano.NanoConstants
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.zip.CRC32
import java.util.zip.CheckedInputStream

object CheckNumUtils {

    fun getCheckNum(file: File): Long {
        if (!file.exists()) {
            throw IOException("File does not exist: ${file.path}")
        }
        return try {
            file.length() + calculateChecksum(file)
        } catch (e: IOException) {
            0L
        }
    }

    private fun calculateChecksum(file: File): Long {
        val buffer = ByteArray(NanoConstants.BUFFER_SIZE)
        CheckedInputStream(FileInputStream(file), CRC32()).use { checkedInputStream ->
            while (checkedInputStream.read(buffer) >= 0) {
                // Reading file to update checksum
            }
            return checkedInputStream.checksum.value
        }
    }
}