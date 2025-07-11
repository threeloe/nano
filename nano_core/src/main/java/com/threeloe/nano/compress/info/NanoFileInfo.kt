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


/**
 * please do not move or rename this class casually, it is used for instrumentation in plugins.
 * This class represents an original file.
 */
class NanoFileInfo : Comparable<NanoFileInfo> {
    /**
     * the original file name
     */
    @JvmField
    var name: String = ""

    /**
     * the compressed file name which this file in.
     */
    @JvmField
    var compressedFileName: String = ""

    /**
     * the group name of this file, it is specified in the nano plugin.
     */
    @JvmField
    var group: String = ""

    /**
     * the size of the original file
     */
    @JvmField
    var fileSize: Long = 0

    /**
     * the pos in the compressed file stream where this file begins.
     */
    @JvmField
    var beginPos: Long = 0

    /**
     * the pos in the compressed file stream where this file ends.
     */
    @JvmField
    var endPos: Long = 0


    @JvmField
    var checkNum: Long = 0

    override fun compareTo(other: NanoFileInfo): Int {
        return beginPos.compareTo(other.beginPos)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is NanoFileInfo) return false
        if (checkNum != other.checkNum) return false
        return name == other.name
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + (checkNum xor (checkNum ushr 32)).toInt()
        return result
    }

    override fun toString(): String {
        return "NanoFileInfo(name='$name', compressedFileName='$compressedFileName', group='$group', fileSize=$fileSize,  beginPos=$beginPos, endPos=$endPos, checkNum=$checkNum)"
    }
}
