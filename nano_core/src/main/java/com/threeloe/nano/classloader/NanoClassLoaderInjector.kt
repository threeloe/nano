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

import android.app.Application
import android.content.Context
import com.threeloe.nano.NanoLog
import com.threeloe.nano.classloader.NanoClassLoaderFactory.createNewClassLoader
import com.threeloe.nano.utils.ReflectUtils
import java.io.File

object NanoClassLoaderInjector {

    fun inject(app: Application, oldClassLoader: ClassLoader?, newLibDir: File?): ClassLoader {
        //NanoClassLoader is already injected by AppComponentFactory,But ths NanoClassLoader.class.clsLoader is different, so we need to use name
        if (app.classLoader.javaClass.name == NanoClassLoader::class.java.name) {
            NanoLog.i( "NanoClassLoader is already injected, skip re-inject")
            return app.classLoader
        }
        NanoLog.i(
            "NanoClassLoader inject,app classLoader:" + app.classLoader.toString()
        )
        val newClassLoader = createNewClassLoader(oldClassLoader, newLibDir)
        doInject(app, newClassLoader)
        return newClassLoader
    }

    private fun doInject(app: Application, classLoader: ClassLoader) {
        Thread.currentThread().contextClassLoader = classLoader
        val baseContext = ReflectUtils.findField(app.javaClass, "mBase")[app] as Context
        try {
            ReflectUtils.findField(baseContext.javaClass, "mClassLoader")[baseContext] = classLoader
        } catch (ignored: Throwable) {
            // There's no mClassLoader field in ContextImpl before Android O.
            // However we should try our best to replace this field in case some
            // customized system has one.
        }
        val basePackageInfo =
            ReflectUtils.findField(baseContext.javaClass, "mPackageInfo")[baseContext]
        ReflectUtils.findField(basePackageInfo.javaClass, "mClassLoader")[basePackageInfo] =
            classLoader

        val res = app.resources
        try {
            ReflectUtils.findField(res.javaClass, "mClassLoader")[res] = classLoader
        } catch (ignored: Throwable) {
            // Ignored.
        }
        try {
            val drawableInflater = ReflectUtils.findField(res.javaClass, "mDrawableInflater")[res]
            if (drawableInflater != null) {
                ReflectUtils.findField(
                    drawableInflater.javaClass,
                    "mClassLoader"
                )[drawableInflater] = classLoader
            }
        } catch (ignored: Throwable) {
            // Ignored.
        }
    }

    private fun modifyApplicationClassLoader(app: Application, newClassLoader: ClassLoader) {
        var current: Class<*>? = app.javaClass
        while (current != null && current != Application::class.java) {
            modifyClassLoader(current, newClassLoader)
            current = current.superclass
        }
    }

    private fun modifyClassLoader(targetClass: Class<*>, newClassLoader: ClassLoader) {
        val classLoaderField = ReflectUtils.findField(Class::class.java, "classLoader")
        classLoaderField.isAccessible = true
        classLoaderField[targetClass] = newClassLoader
    }
}