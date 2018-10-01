package com.chrhsmt.sisheng.debug

import android.content.Context
import com.chrhsmt.sisheng.ReibunInfo
import com.chrhsmt.sisheng.Settings
import com.chrhsmt.sisheng.point.Point
import java.io.File

data class AnalyzedRecordedData(var file: File, var csvLine: String?) {

    companion object {
        private val TAG = "AnalyzedRecordedData"
        private var selected : AnalyzedRecordedData? = null

        fun getSelected(): AnalyzedRecordedData? {
            return selected
        }
        fun setSelected(instance: AnalyzedRecordedData) {
            selected = instance
        }
    }

    var id: Int? = null
    var point: Point? = null
    var sex: String? = null

    fun init(): AnalyzedRecordedData {
        val line = this.csvLine?.split(",")
        this.id = line?.get(1)?.trim()?.toInt()
        this.sex = line?.get(2)?.trim()
        this.point = Point(line?.get(3)?.trim()?.toInt()?: 0, 0.0, 0.0, 0, null)
        return this
    }

    override fun toString(): String {
        return String.format("%s - %s - %s", this.file.name, this.point?.score, this.point?.success())
    }
}