package com.chrhsmt.sisheng

import android.app.Application

/**
 * Created by chihiro on 2017/11/03.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //LeakCanary.install(this);
    }
}