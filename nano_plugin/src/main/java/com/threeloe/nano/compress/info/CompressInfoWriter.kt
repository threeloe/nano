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

package com.threeloe.nano.compress.info

import com.threeloe.nano.NanoContext
import com.threeloe.nano.NanoPlugin
import org.jf.dexlib2.DexFileFactory
import org.jf.dexlib2.Opcode
import org.jf.dexlib2.Opcodes
import org.jf.dexlib2.iface.Method
import org.jf.dexlib2.immutable.ImmutableMethod
import org.jf.dexlib2.immutable.ImmutableMethodImplementation
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11n
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction11x
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21c
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21lh
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction21s
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction22c
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction31i
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction51l
import org.jf.dexlib2.immutable.reference.ImmutableFieldReference
import org.jf.dexlib2.immutable.reference.ImmutableMethodReference
import org.jf.dexlib2.immutable.reference.ImmutableStringReference
import org.jf.dexlib2.immutable.reference.ImmutableTypeReference
import org.jf.dexlib2.rewriter.DexRewriter
import org.jf.dexlib2.rewriter.Rewriter
import org.jf.dexlib2.rewriter.RewriterModule
import org.jf.dexlib2.rewriter.Rewriters
import java.io.File


class CompressInfoWriter(private val context: NanoContext) {

    companion object {
        private const val PACKAGE_NAME_COMPRESS_INFO = "Lcom/threeloe/nano/compress/info"
        // Nano file info
        private const val CLASS_NANO_FILE_INFO = "$PACKAGE_NAME_COMPRESS_INFO/NanoFileInfo;"

        const val CLASS_COMPRESS_INFO_READER = "$PACKAGE_NAME_COMPRESS_INFO/CompressInfo;"
        private const val METHOD_NAME_GET_COMPRESS_METHOD = "getCompressMethod"
        private const val METHOD_PREFIX_GET_SO_FILE_INFO_LIST = "getSoFileInfoListFor"
    }


    fun write(compressBlockMap: Map<String,List<CompressBlock>>, targetDex: File) {
        val dexFile = DexFileFactory.loadDexFile(targetDex, Opcodes.getDefault())
        println("[${NanoPlugin.TAG}] start writing compress info: ${targetDex.absolutePath}")
        val reWriter = DexRewriter(createRewriterModule(compressBlockMap))
        val newDexFile = reWriter.dexFileRewriter.rewrite(dexFile)
        DexFileFactory.writeDexFile(targetDex.path, newDexFile)
        println("[${NanoPlugin.TAG}] finish writing compress info: ${targetDex.absolutePath}")
    }


    private fun createRewriterModule(
        compressBlockMap: Map<String,List<CompressBlock>>
    ): RewriterModule {
        return object : RewriterModule() {
            override fun getMethodRewriter(rewriters: Rewriters): Rewriter<Method> = Rewriter { method ->
                if (method.definingClass == CLASS_COMPRESS_INFO_READER) {
                    when (method.name) {
                        METHOD_NAME_GET_COMPRESS_METHOD -> createCompressMethod(method)
                        else -> {
                            val abi = compressBlockMap.keys.find { method.name == "$METHOD_PREFIX_GET_SO_FILE_INFO_LIST${it.toCamelCase()}" }
                            if (abi != null && compressBlockMap[abi]?.isNotEmpty() == true) {
                                createFileInfoMethod(method, compressBlockMap[abi]!!.flatMap { it.originFileList })
                            } else method
                        }
                    }
                } else method
            }
        }
    }

    private fun String.toCamelCase(): String {
        return split('-').joinToString("") { it.capitalize() }
    }

    private fun createCompressMethod(method: Method): ImmutableMethod {
        val methodImplementation = ImmutableMethodImplementation(
            1, listOf(
                ImmutableInstruction11n(Opcode.CONST_4, 0, context.options.getCompressMethod().id),
                ImmutableInstruction11x(Opcode.RETURN, 0)
            ), null, null
        )
        return buildImmutableMethod(method,methodImplementation);
    }

    private fun buildImmutableMethod(method: Method, methodImplementation: ImmutableMethodImplementation): ImmutableMethod {
        return ImmutableMethod(
            method.definingClass,
            method.name,
            method.parameters,
            method.returnType,
            method.accessFlags,
            method.annotations,
            method.hiddenApiRestrictions,
            methodImplementation
        )
    }

    private fun createFileInfoMethod(method: Method, fileInfoList: List<NanoFileInfo>): ImmutableMethod {
        val instructions = mutableListOf<ImmutableInstruction>(
            ImmutableInstruction21c(Opcode.NEW_INSTANCE, 0, ImmutableTypeReference("Ljava/util/ArrayList;")),
            ImmutableInstruction35c(Opcode.INVOKE_DIRECT, 1, 0, 0, 0, 0, 0, ImmutableMethodReference("Ljava/util/ArrayList;", "<init>", null, "V"))
        )

        fileInfoList.forEach { instructions.addAll(createFileInfoInstructions(it)) }
        instructions.add(ImmutableInstruction11x(Opcode.RETURN_OBJECT, 0))

        val  methodImplementation = ImmutableMethodImplementation(4, instructions, null, null)
        return buildImmutableMethod(method, methodImplementation)
    }

    private fun createFileInfoInstructions(fileInfo: NanoFileInfo): List<ImmutableInstruction> {
        return listOf(
            // Create a new NanoFileInfo object and set its fields
            ImmutableInstruction21c(Opcode.NEW_INSTANCE, 1, ImmutableTypeReference(
                CLASS_NANO_FILE_INFO
            )),
            ImmutableInstruction35c(Opcode.INVOKE_DIRECT, 1, 1, 0, 0, 0, 0, ImmutableMethodReference(
                CLASS_NANO_FILE_INFO, "<init>", null, "V")),
            ImmutableInstruction21c(Opcode.CONST_STRING, 2, ImmutableStringReference(fileInfo.name)),
            ImmutableInstruction22c(Opcode.IPUT_OBJECT, 2, 1, ImmutableFieldReference(
                CLASS_NANO_FILE_INFO, "name", "Ljava/lang/String;")),
            ImmutableInstruction21c(Opcode.CONST_STRING, 2, ImmutableStringReference(fileInfo.compressedFileName)),
            ImmutableInstruction22c(Opcode.IPUT_OBJECT, 2, 1, ImmutableFieldReference(
                CLASS_NANO_FILE_INFO, "compressedFileName", "Ljava/lang/String;")),
            ImmutableInstruction21c(Opcode.CONST_STRING, 2, ImmutableStringReference(fileInfo.group)),
            ImmutableInstruction22c(Opcode.IPUT_OBJECT, 2, 1, ImmutableFieldReference(
                CLASS_NANO_FILE_INFO, "group", "Ljava/lang/String;")),
            assignLongInstruction(fileInfo.beginPos, 2),
            ImmutableInstruction22c(Opcode.IPUT_WIDE, 2, 1, ImmutableFieldReference(
                CLASS_NANO_FILE_INFO, "beginPos", "J")),
            assignLongInstruction(fileInfo.endPos, 2),
            ImmutableInstruction22c(Opcode.IPUT_WIDE, 2, 1, ImmutableFieldReference(
                CLASS_NANO_FILE_INFO, "endPos", "J")),
            assignLongInstruction(fileInfo.fileSize, 2),
            ImmutableInstruction22c(Opcode.IPUT_WIDE, 2, 1, ImmutableFieldReference(
                CLASS_NANO_FILE_INFO, "fileSize", "J")),
            assignLongInstruction(fileInfo.checkNum, 2),
            ImmutableInstruction22c(Opcode.IPUT_WIDE, 2, 1, ImmutableFieldReference(
                CLASS_NANO_FILE_INFO, "checkNum", "J")),

            // Add the file info object to the list
            ImmutableInstruction35c(Opcode.INVOKE_INTERFACE, 2, 0, 1, 0, 0, 0, ImmutableMethodReference("Ljava/util/List;", "add", listOf("Ljava/lang/Object;"), "Z"))
        )
    }


    private fun assignLongInstruction(value: Long, register: Int): ImmutableInstruction {
        return when {
            value in Short.MIN_VALUE..Short.MAX_VALUE -> ImmutableInstruction21s(Opcode.CONST_WIDE_16, register, value.toInt())
            value in Int.MIN_VALUE..Int.MAX_VALUE -> ImmutableInstruction31i(Opcode.CONST_WIDE_32, register, value.toInt())
            value and 0xFFFFFFFFFFFFL == 0L -> ImmutableInstruction21lh(Opcode.CONST_WIDE_HIGH16, register, value ushr 48)
            else -> ImmutableInstruction51l(Opcode.CONST_WIDE, register, value)
        }
    }

}