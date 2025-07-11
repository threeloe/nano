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

package com.threeloe.nano.demo

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class SecondActivity : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        val tv = findViewById<TextView>(R.id.tv_text)
        try {
            loadTensorFlowLite()
            tv.text = "Load so Success"
        } catch (e: UnsatisfiedLinkError) {
            tv.text = "Load so Failed: ${e.message}"
            Log.i("SecondActivity", "Load so Failed: ${e.message}", e)
        }
    }

    private fun loadTensorFlowLite() {
        System.loadLibrary("tensorflowlite_jni")
        System.loadLibrary("tensorflowlite_gpu_jni")
    }
}