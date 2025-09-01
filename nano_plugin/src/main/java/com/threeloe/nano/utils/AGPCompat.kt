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

import com.android.build.api.component.impl.ComponentImpl
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import java.io.File

object AGPCompat {

    fun isBundleTask(task: Task, appProject: Project, variantName: String): Boolean {
        val buildPreBundleTask = "build" + variantName + "PreBundle"
        val bundleTask = "bundle$variantName"
        return task.project == appProject && (task.name.equals(
            buildPreBundleTask,
            ignoreCase = true
        ) || task.name.equals(bundleTask, ignoreCase = true))
    }

    fun getMergedAssetsBaseDirCompat(project: Project, variantName: String): File {
        val mergeAssetsTaskName = "merge${variantName}Assets"
        val mergeAssetsTask = project.tasks.findByName(mergeAssetsTaskName)
            ?: throw GradleException("Can't find mergeAssetsTask for variant: $variantName, please check your AGP version or task name.")
        val mergedAssetsOutputDir = try {
            mergeAssetsTask.javaClass.getMethod("getOutputDirectory")
                .invoke(mergeAssetsTask) as DirectoryProperty
        } catch (ignored: Throwable) {
            mergeAssetsTask.javaClass.getMethod("getOutputDir")
                .invoke(mergeAssetsTask) as DirectoryProperty
        }
        return mergedAssetsOutputDir.asFile.get()
            ?: throw GradleException("Can't read outputDir from ${mergeAssetsTask::class.java.name}")
    }

    fun getCompressAssetsTask(project: Project, variantName: String): Task? {
        val compressAssetsTask = project.tasks.findByName("compress${variantName}Assets")
        return compressAssetsTask
    }

    fun getStripSoTask(project: Project, variantName: String): Task {
        val stripSoTask = project.tasks.findByName("strip${variantName}DebugSymbols")
            ?: throw GradleException("Can't find stripSoTask for variant: $variantName, please check your AGP version or task name.")
        return stripSoTask
    }


    fun getAGPVersion(): String {
        return try {
            Class.forName("com.android.builder.model.Version")
                .getDeclaredField("ANDROID_GRADLE_PLUGIN_VERSION").apply { isAccessible = true }
                .get(null) as String
        } catch (e: Exception) {
            "3.2.0"
        }
    }

    fun getVariantByComponentInfo(component: Any): ComponentImpl{
        try {
            val getVariantMethod = component.javaClass.getMethod("getVariant")
            getVariantMethod.isAccessible = true
            val result = getVariantMethod.invoke(component)
            return result as ComponentImpl
        } catch (e: Exception) {
            throw GradleException("Can't get variant :$e, please check your AGP version.")
        }
    }

    fun getDexDir(project: Project, variantName: String): Set<File> {
        //Release
        val releaseTaskNameList = getReleaseDexTaskNameList(variantName)
        releaseTaskNameList.forEach {
            val task = project.tasks.findByName(it)
            if (task != null) {
                return task.outputs.files.files
            }
        }
        //Debug
        return getDebugDexDir(project, variantName)
    }

    fun getMergeAssetsTaskName(variantName: String): String {
        return "merge${variantName}Assets"
    }

    fun getReleaseDexTaskNameList(variantName: String): List<String> {
        return listOf(
            "transformClassesAndResourcesWithR8For${variantName}", "minify${variantName}WithR8"
        )
    }

    fun getDebugDexTaskNameList(variantName: String): List<String> {
        return listOf(
            "mergeProjectDex${variantName}",
            "mergeLibDex${variantName}",
            "mergeExtDex${variantName}"
        )
    }

    private fun getDebugDexDir(project: Project, variantName: String): Set<File> {
        val dexTaskNameList = getDebugDexTaskNameList(variantName)
        val dexDirs = mutableSetOf<File>()
        dexTaskNameList.forEach {
            val task = project.tasks.findByName(it)
            if (task != null) {
                dexDirs.addAll(task.outputs.files.files)
            }
        }
        return dexDirs
    }

    fun getSoDir(project: Project, variantName: String): Set<File> {
        var stripSoTaskName = "strip${variantName}DebugSymbols"
        val stripSoTask = project.tasks.findByName(stripSoTaskName)
        if (stripSoTask != null) {
            return stripSoTask.outputs.files.files
        }
        throw GradleException("Can't find stripSoTask for variant: $variantName, please check your AGP version or task name.")
    }

}
