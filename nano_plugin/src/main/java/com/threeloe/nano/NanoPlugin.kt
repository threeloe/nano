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

package com.threeloe.nano

import com.threeloe.nano.core.SoCompressor
import com.threeloe.nano.utils.AGPCompat
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.util.Locale


class NanoPlugin : Plugin<Project> {

    companion object {
        private const val CONSTANT_NANO = "nano"
        const val TAG = "NanoPlugin"
    }

    override fun apply(project: Project) {
        val options: NanoOptions =
            project.extensions.create(CONSTANT_NANO, NanoOptions::class.java, project)

        project.afterEvaluate {
            if (!options.enable) {
                return@afterEvaluate
            }
            val appPlugin = project.plugins.findPlugin(
                com.android.build.gradle.internal.plugins.AppPlugin::class.java
            )
            val variants = appPlugin?.variantManager?.mainComponents
            variants?.forEach { component ->
                val variant = component.variant
                val variantName = variant.name.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
                val compressAssetsTask = AGPCompat.getCompressAssetsTask(project, variantName)
                val stripSoTask = AGPCompat.getStripSoTask(project, variantName)
                stripSoTask.doLast {
                    val context = NanoContext(project, variantName, options)
                    SoCompressor(context).compress()
                }
                //agp > 7.0.0+
                compressAssetsTask?.dependsOn(stripSoTask)
                configDependsOn(project, variantName, stripSoTask)
            }
        }
    }


    private fun configDependsOn(project: Project, variant: String, compressorTask: Task) {
        val taskNameList = mutableListOf(AGPCompat.getMergeAssetsTaskName(variant))
        //Release
        taskNameList.addAll(AGPCompat.getReleaseDexTaskNameList(variant))
        //Debug
        taskNameList.addAll(AGPCompat.getDebugDexTaskNameList(variant))
        taskNameList.mapNotNull {
            project.tasks.findByName(it)
        }.map {
            compressorTask.dependsOn(it)
        }
    }


}