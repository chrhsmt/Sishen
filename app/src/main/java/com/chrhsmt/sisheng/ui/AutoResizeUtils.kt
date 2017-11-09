package com.chrhsmt.sisheng.ui

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.TextView

/**
 * フォントサイズ自動調整TextView
 *
 * ソースコードは以下から借用
 * http://aillicepray.blogspot.jp/2015/02/textview.html
 */
object AutoResizeUtils {
    class AutoResizeInfo {
        internal var modelText: String? = null
        internal var numberLine = 1
    }
    /**
     * テキストサイズ調整
     */
    fun resize(view: TextView, info: AutoResizeInfo) {
        /** 最小のテキストサイズ  */
        val MIN_TEXT_SIZE = 10f

        val viewHeight = view.height // Viewの縦幅
        val viewWidth = view.width // Viewの横幅

        // テキストサイズ
        var textSize = view.textSize

        // Paintにテキストサイズ設定
        val paint = Paint()
        paint.textSize = textSize

        // テキスト取得
        if (info.modelText == null) {
            info.modelText = view.text.toString()
        }

        // テキストの縦幅取得
        var fm = paint.fontMetrics
        var textHeight = Math.abs(fm.top) + Math.abs(fm.descent)
        val lineNum = view.text!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size

        // テキストの横幅取得
        var textWidth = paint.measureText(info.modelText)

        // 縦幅と、横幅が収まるまでループ
        while ((viewHeight < (textHeight * lineNum)) or (viewWidth < textWidth)) {
            // 調整しているテキストサイズが、定義している最小サイズ以下か。
            if (MIN_TEXT_SIZE >= textSize) {
                // 最小サイズ以下になる場合は最小サイズ
                textSize = MIN_TEXT_SIZE
                break
            }

            // テキストサイズをデクリメント
            textSize--

            // Paintにテキストサイズ設定
            paint.textSize = textSize

            // テキストの縦幅を再取得
            // 改行を考慮する
            fm = paint.fontMetrics
            textHeight = Math.abs(fm.top) + Math.abs(fm.descent) * info.numberLine

            // テキストの横幅を再取得
            textWidth = paint.measureText(info.modelText)
        }

        // テキストサイズ設定
        view.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }


    /**
     * 基準となる改行を含む文字列の最も文字列が大きい部分がViewの枠に収まるようにフォントサイズを調整する.(改行には適応してない模様)
     * 文字列に改行を含まない場合、それをそのまま基準にする.
     * 表示される文字列の最大数がわかっている時に有効利用できる.
     * @param modelText
     */
    fun setModelText(modelText: String?, info: AutoResizeInfo) {
        if (modelText != null) {
            val str = modelText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            info.numberLine = str.size
            var includeLinefeed = false
            if (str.size > 1) includeLinefeed = true

            if (includeLinefeed) {
                var a: String? = null        // 一時変数
                var model: String? = null
                for (i in str.indices) {
                    if (a == null)
                        a = str[i]
                    else {
                        // 2周目以降
                        if (a.length >= str[i].length)
                            model = a
                        else
                            model = str[i]
                    }
                }
                info.modelText = model
            } else {
                info.modelText = modelText
            }
        }
    }
}