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

import com.google.common.collect.Sets
import com.threeloe.nano.compress.CompressMethodProvider
import com.threeloe.nano.compress.method.CompressMethodParam
import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project


open class NanoOptions(project: Project) {
    var enable = false
    var compressMethod: String = "zstd"
    var compressLevel: Int? = null

    var groups: NamedDomainObjectContainer<CompressGroup> =
        project.container(CompressGroup::class.java)

    fun getCompressMethod(): CompressMethodParam {
        val method: CompressMethodParam =
            CompressMethodProvider.getCompressMethodParam(compressMethod, compressLevel)
        return method
    }

    fun groups(configureClosure: Closure<*>) {
        groups.configure(configureClosure)
    }

    class CompressGroup(var name: String) {
        var blockNum = 1
        private var includes: MutableSet<String> = HashSet()

        fun getIncludes(): Set<String> {
            return Sets.newHashSet(includes)
        }

        fun setIncludes(includes: Set<String>) {
            this.includes = Sets.newHashSet(includes)
        }

        fun include(include: String) {
            includes.add(include)
        }

        fun blockNum(blockNum: Int) {
            this.blockNum = blockNum
        }
    }
}