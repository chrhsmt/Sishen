package com.chrhsmt.sisheng.debug

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.chrhsmt.sisheng.persistence.ExternalMedia
import java.io.*
import java.nio.charset.Charset
import java.util.*
import kotlin.collections.ArrayList

class SDCardManager {

    companion object {
        val TAG = "SDCardManager"
        val REQUEST_PERMISSION = 1000;
    }

    fun setUpReadWriteExternalStorage(context: Context) {
        val dir = ExternalMedia.getExternalDirectoryPath(context)
        if (ExternalMedia.isExternalStorageWritable() && dir != null && dir!!.canWrite()) {
//            Toast.makeText(this, "SDカードへの書き込みが可能です", Toast.LENGTH_SHORT).show()
            Log.d(SDCardManager.TAG, String.format("External media path: %s", dir.absoluteFile))
        } else {
            Toast.makeText(context, "SDカードがマウントされていない、もしくは書き込みが不可能な状態です", Toast.LENGTH_SHORT).show()
        }
    }

    fun readCsv(context: Context, name: String): List<String> {
        var lines: List<String> = ArrayList<String>()
        val dir = ExternalMedia.getExternalDirectoryPath(context)
        if (ExternalMedia.isExternalStorageReadable() && dir != null && dir!!.canRead()) {
            val newFile = File(dir, name)
            val fr = FileReader(newFile)
            lines = fr.readLines()
            fr.close()
        } else {
            Toast.makeText(context, "SDカードがマウントされていない、もしくは書き込みが不可能な状態です", Toast.LENGTH_SHORT).show()
        }
        return lines
    }

    fun write(context: Context, name: String, data: String) {
        val dir = ExternalMedia.getExternalDirectoryPath(context)
        if (ExternalMedia.isExternalStorageWritable() && dir != null && dir!!.canWrite()) {
            val newFile = File(dir, name)
            val fos = FileOutputStream(newFile)
            fos.write(data.toByteArray(Charset.defaultCharset()))
            fos.close()
        } else {
            Toast.makeText(context, "SDカードがマウントされていない、もしくは書き込みが不可能な状態です", Toast.LENGTH_SHORT).show()
        }
    }

    fun getFileList(): List<File> {
        var ret = ArrayList<File>()
        ExternalMedia.saveDir?.takeIf { it -> it.canRead() }?.let { it ->
            it.listFiles().forEach { file -> ret.add(file) }
        }
        return ret
    }

    @SuppressLint("WrongConstant")
    fun copyAudioFile(file: File, activity: Activity): String {
        // ファイル移動
        var dataName = file.name
        if (dataName.contains("/")) {
            dataName = dataName.replace("/", "_")
        }
        val path = String.format("/data/data/%s/files/%s", activity.packageName, dataName)
        val input = file.inputStream()
        val output = activity.openFileOutput(dataName, Context.MODE_ENABLE_WRITE_AHEAD_LOGGING)
        val DEFAULT_BUFFER_SIZE = 1024 * 4

        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var n = 0
        while (true) {
            n = input.read(buffer)
            if (n == -1) break
            output.write(buffer, 0, n)
        }
        output.close()
        input.close()

        return path
    }
}