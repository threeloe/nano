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

import com.threeloe.nano.NanoPlugin
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcodes
import java.io.File

object DexUtils {

    private const val DEX_FILE_SUFFIX = "dex"

    fun findClassTargetDex(project: Project, variantName: String, className: String): File? {
        return getDexFiles(AGPCompat.getDexDir(project, variantName)).firstOrNull { dexFile ->
            DexFileFactory.loadDexFile(dexFile, Opcodes.getDefault())
                .classes.any { it.type == className }
        }
    }

    private fun getDexFiles(inputs: Collection<File>): List<File> {
        val dexFiles = inputs.flatMap { dexDir ->
            println("[${NanoPlugin.TAG}] input: DexFilesSet=${dexDir.absolutePath}")
            dexDir.walk()
                .filter { it.isFile && it.extension == DEX_FILE_SUFFIX }
                .toList()
        }
        if (dexFiles.isEmpty()) {
            throw GradleException("No dex files found")
        }
        return dexFiles
    }

}