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

package com.threeloe.nano.decompress.writer

import java.io.Closeable

/**
 * Interface for writing data to a file.
 * This interface extends [Closeable] to allow for resource management.
 */
interface FileWriter: Closeable {

    /**
     * Writes a portion of the given byte array to the file.
     *
     * @param data The byte array containing the data to write.
     * @param offset The start offset in the data array.
     * @param length The number of bytes to write.
     */
    fun write(data: ByteArray, offset: Int, length: Int)

    /**
     * Writes the entire byte array to the file.
     *
     * @param data The byte array containing the data to write.
     */
    fun write(data: ByteArray)
}