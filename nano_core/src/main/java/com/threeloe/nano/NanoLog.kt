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

package com.threeloe.nano

import android.util.Log

object NanoLog {
    
    private const val TAG: String = "NanoLog"
    
    fun d(msg: String?) {
        var msg = msg
        Log.d(TAG, msg!!)
    }

    fun i(msg: String?) {
        var msg = msg
        Log.i(TAG, msg!!)
    }

    fun w(msg: String?) {
        var msg = msg
        Log.w(TAG, msg!!)
    }

    fun e(msg: String?) {
        var msg = msg
        Log.e(TAG, msg!!)
    }

    fun e(msg: String?, tr: Throwable?) {
        var msg = msg
        Log.e(TAG, msg, tr)
    }

}
