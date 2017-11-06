package com.chrhsmt.sisheng.persistence

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File


/**
 * Created by chihiro on 2017/11/06.
 */
object ExternalMedia {

    val TAG = "ExternalMedia"

    fun isExternalStorageWritable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED.equals(state)
    }

    fun isExternalStorageReadable(): Boolean {
        val state = Environment.getExternalStorageState()
        return Environment.MEDIA_MOUNTED.equals(state) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)
    }

    fun getExternalDirectoryPath(context: Context): File {
        val file = context.getExternalFilesDir(android.os.Environment.DIRECTORY_MUSIC)
        if (!file.mkdirs()) {
            Log.e(TAG, "Directory not created");
        }
        return file
    }
}