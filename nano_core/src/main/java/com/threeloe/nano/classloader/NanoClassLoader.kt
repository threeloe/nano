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

package com.threeloe.nano.classloader

import dalvik.system.PathClassLoader
import java.io.IOException
import java.net.URL
import java.util.Enumeration
class NanoClassLoader internal constructor(
    dexPath: String?, libraryPath: String?,
    /**
     * the original PathClassLoader has sharedLibraryLoaders field, which is also used to find classes,
     * and this PathClassLoader in sharedLibraryLoaders has some libraries like "system/framework/org.apache.http.legacy.jar" in its dexPathList,
     * but we cannot access the sharedLibraryLoaders field, so we need to use the original PathClassLoader to find classes.
     */
    private val mOriginAppClassLoader: ClassLoader
) : PathClassLoader(dexPath, libraryPath, mOriginAppClassLoader.parent) {
    @Throws(ClassNotFoundException::class)
    override fun findClass(name: String): Class<*> {
        var cl: Class<*>? = null
        cl = try {
            super.findClass(name)
        } catch (ignored: ClassNotFoundException) {
            null
        }
        return cl ?: mOriginAppClassLoader.loadClass(name)
    }

    override fun getResource(name: String): URL? {
        // The lookup order we use here is the same as for classes.
        var resource = Any::class.java.classLoader.getResource(name)
        if (resource != null) {
            return resource
        }
        resource = findResource(name)
        if (resource != null) {
            return resource
        }
        return mOriginAppClassLoader.getResource(name)
    }

    @Throws(IOException::class)
    override fun getResources(name: String): Enumeration<URL> {
        val resources = arrayOf<Enumeration<*>?>(
            Any::class.java.classLoader.getResources(name),
            findResources(name),
            mOriginAppClassLoader.getResources(name)
        ) as Array<Enumeration<URL>?>
        return CompoundEnumeration<URL>(resources)
    }

    internal inner class CompoundEnumeration<E>(private val enums: Array<Enumeration<E>?>) :
        Enumeration<E> {
        private var index = 0

        override fun hasMoreElements(): Boolean {
            while (index < enums.size) {
                if (enums[index] != null && enums[index]!!.hasMoreElements()) {
                    return true
                }
                index++
            }
            return false
        }

        override fun nextElement(): E {
            if (!hasMoreElements()) {
                throw NoSuchElementException()
            }
            return enums[index]!!.nextElement()
        }
    }
}