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

import android.os.Build
import java.lang.reflect.Field
import java.lang.reflect.Method

object ReflectUtils {

    private const val METHOD_GET_DECLARED_FIELD = "getDeclaredField"
    private const val METHOD_GET_DECLARED_METHOD = "getDeclaredMethod"

    private var sGetDeclaredFieldMethod: Method? = null
    private var sGetDeclaredMethodMethod: Method? = null

    fun findFieldForObject(obj: Any, name: String): Field {
        return findField(obj.javaClass, name)
    }

    fun findField(clazz: Class<*>, name: String): Field {
        return findMemberRecursion(clazz, name) {
            val field = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                invokeFieldProxyMethod(it, name)
            } else {
                it.getDeclaredField(name)
            }
            field.isAccessible = true
            field
        } as Field
    }

    fun findMethod(targetClazz: Class<*>, name: String, vararg parameterTypes: Class<*>): Method {
        return findMemberRecursion(targetClazz, name) {
            val method = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                invokeMethodProxyMethod(it, name, *parameterTypes)
            } else {
                it.getDeclaredMethod(name, *parameterTypes)
            }
            method.isAccessible = true
            method
        } as Method
    }


    private fun invokeFieldProxyMethod(clazz: Class<*>, name: String): Field {
        try {
            val method = sGetDeclaredFieldMethod ?: Class::class.java.getMethod(
                METHOD_GET_DECLARED_FIELD, String::class.java
            ).also {
                sGetDeclaredFieldMethod = it
            }
            return method.invoke(clazz, name) as Field
        } catch (e: Exception) {
            throw e
        }
    }


    private fun invokeMethodProxyMethod(
        clazz: Class<*>, name: String, vararg parameterTypes: Class<*>
    ): Method {
        try {
            val method = sGetDeclaredMethodMethod ?: Class::class.java.getMethod(
                METHOD_GET_DECLARED_METHOD, String::class.java, Class.forName("[Ljava.lang.Class;")
            ).also {
                sGetDeclaredMethodMethod = it
            }
            return method.invoke(clazz, name, parameterTypes) as Method
        } catch (e: Exception) {
            throw e
        }
    }

    private fun findMemberRecursion(
        clazz: Class<*>, name: String, finder: (Class<*>) -> Any
    ): Any {
        var currClazz = clazz
        while (true) {
            try {
                return finder(currClazz)
            } catch (t: Throwable) {
                if (currClazz == Any::class.java) {
                    throw t
                }
                currClazz = currClazz.superclass
            }
        }
    }


}