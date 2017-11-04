package com.chrhsmt.sisheng

import android.app.Application
import com.squareup.leakcanary.LeakCanary

/**
 * Created by chihiro on 2017/11/03.
 */
class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        LeakCanary.install(this);
    }
}