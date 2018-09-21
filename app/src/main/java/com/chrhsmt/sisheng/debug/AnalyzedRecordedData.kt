package com.chrhsmt.sisheng.debug

import com.chrhsmt.sisheng.point.Point
import java.io.File

data class AnalyzedRecordedData(var file: File, var csvLine: String?) {

    var id: Int? = null
    var point: Point? = null

    fun init(): AnalyzedRecordedData {
        val line = this.csvLine?.split(",")
        this.id = line?.get(1)?.trim()?.toInt()
        this.point = Point(line?.get(3)?.trim()?.toInt()?: 0, 0.0, 0.0, 0)
        return this
    }

    override fun toString(): String {
        return String.format("%s - %s - %s", this.file.name, this.point?.score, this.point?.success())
    }
}