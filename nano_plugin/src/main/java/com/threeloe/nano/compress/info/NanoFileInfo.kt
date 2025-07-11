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

import java.io.File

/**
 * corresponds to a file or a file block,
 * refers to the Class NanoFileInfo in the nano_impl library.
 */
class NanoFileInfo {
    lateinit var file: File

    lateinit var name: String
    lateinit var compressedFileName: String
    lateinit var group: String

    var fileSize: Long = 0

    var beginPos: Long = 0
    var endPos: Long = 0

    var checkNum: Long = 0

}