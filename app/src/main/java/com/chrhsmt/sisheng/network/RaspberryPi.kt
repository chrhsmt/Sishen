package com.chrhsmt.sisheng.network

import com.chrhsmt.sisheng.Settings
import okhttp3.*
import java.io.IOException

/**
 * Created by chihiro on 2017/10/10.
 */
class RaspberryPi {

    fun send(callback: Callback) {
        val url = String.format("http://%s%s", Settings.raspberrypiHost, Settings.raspberrypiPath)
        val request: Request = Request.Builder()
                .url(url)
                .build()
        OkHttpClient().newCall(request).enqueue(callback)
    }
}