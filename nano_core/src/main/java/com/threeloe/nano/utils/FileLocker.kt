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
import com.threeloe.nano.utils.FileUtils.closeQuietly
import com.threeloe.nano.utils.FileUtils.createFile
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.nio.channels.FileChannel
import java.nio.channels.FileLock

/**
 * FileLocker is a utility class that provides file locking functionality.
 * It allows you to lock a file to prevent other processes from accessing it
 * while you are working with it.
 *
 * @param lockFile The file to be locked.
 */
class FileLocker(private val lockFile: File) {

    private var lockRaf: RandomAccessFile? = null
    private var cacheLock: FileLock? = null
    private var lockChannel: FileChannel? = null

    fun lock() {
        try {
            initializeLock()
            NanoLog.i("Blocking on lock ${lockFile.canonicalPath}")
            cacheLock = lockChannel?.lock()
            NanoLog.i("Acquired lock ${lockFile.canonicalPath}")
        } catch (e: IOException) {
            throw e
        }
    }

    fun tryLock(): Boolean {
        return try {
            cacheLock = lockChannel?.tryLock()
            cacheLock != null
        } catch (e: IOException) {
            NanoLog.e("Failed to acquire lock on ${lockFile.path}", e)
            false
        }
    }

    fun unlock() {
        try {
            cacheLock?.release()
            NanoLog.i("Released lock ${lockFile.path}")
        } catch (ignored: IOException) {

        } finally {
            close()
        }
    }

    private fun initializeLock() {
        if (lockRaf == null || lockChannel == null) {
            createFile(lockFile)
            lockRaf = RandomAccessFile(lockFile, "rw")
            lockChannel = lockRaf!!.channel
        }
    }

    private fun close() {
        lockChannel?.closeQuietly()
        lockRaf?.closeQuietly()
        lockChannel = null
        lockRaf = null
        cacheLock = null
    }
}

