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

package com.threeloe.nano.core

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import com.threeloe.nano.NanoLog
import java.io.File

/**
 * manage the decompression directory
 */
object NanoManager {

    private const val UNDEFINED_ID_INT: Int = -1
    private const val NANO_DIR_NAME: String = "nano"
    private const val KEY_APK_ID: String = "key_apk_id"
    private const val KEY_FINISH_SO_DECOMPRESSION: String = "key_finish_so_decompression_"


    private var mApkId = UNDEFINED_ID_INT
    private var mLastApkId = UNDEFINED_ID_INT
    private var appDataDir : String = ""
    private var preferences: SharedPreferences? = null


    @Volatile
    private var init = false

    /**
     * after init, can use currentVersion info
     *
     */
    fun init(applicationInfo: ApplicationInfo) {
        if (init) {
            return
        }
        NanoLog.i( "NanoManager init, applicationInfo:$applicationInfo")
        appDataDir = applicationInfo.dataDir
        mApkId = generateUniqueApkId(applicationInfo)
        init = true
    }

    private fun generateUniqueApkId(appInfo: ApplicationInfo): Int {
        val apkFile = File(appInfo.sourceDir)
        val apkTime = apkFile.lastModified()
        val apkSize = apkFile.length()
        return (apkTime + apkSize).toInt()
    }

    val currentVersionWorkDir: File by lazy {
        getWorkDir(mApkId)
    }


    private fun workDirName(apkId: Int): String {
        return NANO_DIR_NAME + apkId
    }

    /**
     * get the work directory for the specified apkId
     */
    private fun getWorkDir(apkId: Int): File {
        val workDir = File(appDataDir, workDirName(apkId))
        if (!workDir.exists()) {
            workDir.mkdirs()
        }
        return workDir
    }


    /**
     * after configSP, can use lastVersion info
     */
    fun configSP(preferences: SharedPreferences) {
        this.preferences = preferences
        val lastApkId = preferences.getInt(KEY_APK_ID, UNDEFINED_ID_INT)
        mLastApkId = lastApkId
        if (lastApkId == mApkId) {
            return
        }
        updateInfo(preferences)
    }

    private fun updateInfo(preferences: SharedPreferences) {
        val editor = preferences.edit()
        editor.putBoolean(KEY_FINISH_SO_DECOMPRESSION, false)
        editor.putInt(KEY_APK_ID, mApkId)
        editor.commit()
    }

    fun isDecompressionFinished(group:String): Boolean {
        return preferences?.getBoolean(KEY_FINISH_SO_DECOMPRESSION+group, false) ?: false
    }

    fun setDecompressionFinished(group: String,finished: Boolean) {
        preferences?.edit()?.putBoolean(KEY_FINISH_SO_DECOMPRESSION+group, finished)?.commit()
    }

    fun isApkUpdated(): Boolean {
        return mApkId != mLastApkId
    }

    val lastVersionWorkDir: File
        get() = getWorkDir(mLastApkId)


}
