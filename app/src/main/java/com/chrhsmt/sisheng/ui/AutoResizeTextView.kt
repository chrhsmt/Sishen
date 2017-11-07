package com.chrhsmt.sisheng.ui

import android.content.Context
import android.graphics.Paint
import android.util.TypedValue
import android.util.AttributeSet
import android.widget.TextView


/**
 * フォントサイズ自動調整TextView
 *
 * ソースコードは以下から借用
 * http://aillicepray.blogspot.jp/2015/02/textview.html
 */
class AutoResizeTextView : TextView {

    internal var modelText: String? = null
    internal var numberLine = 1

    /**
     * コンストラクタ
     * @param context
     */
    constructor(context: Context) : super(context) {}

    /**
     * コンストラクタ
     * @param context
     * @param attrs
     */
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        processAttributeSet(attrs)
    }

    /**
     * 子Viewの位置を決める
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        resize()
    }

    /**
     * テキストサイズ調整
     */
    private fun resize() {
        /** 最小のテキストサイズ  */
        val MIN_TEXT_SIZE = 10f

        val viewHeight = this.height // Viewの縦幅
        val viewWidth = this.width // Viewの横幅

        // テキストサイズ
        var textSize = textSize

        // Paintにテキストサイズ設定
        val paint = Paint()
        paint.textSize = textSize

        // テキスト取得
        if (modelText == null) {
            modelText = text.toString()
        }

        // テキストの縦幅取得
        var fm = paint.fontMetrics
        var textHeight = Math.abs(fm.top) + Math.abs(fm.descent)
        val lineNum = this.text!!.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray().size

        // テキストの横幅取得
        var textWidth = paint.measureText(modelText)

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
            textHeight = Math.abs(fm.top) + Math.abs(fm.descent) * numberLine

            // テキストの横幅を再取得
            textWidth = paint.measureText(modelText)
        }

        // テキストサイズ設定
        setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
    }


    /**
     * 基準となる改行を含む文字列の最も文字列が大きい部分がViewの枠に収まるようにフォントサイズを調整する.(改行には適応してない模様)
     * 文字列に改行を含まない場合、それをそのまま基準にする.
     * 表示される文字列の最大数がわかっている時に有効利用できる.
     * @param modelText
     */
    protected fun setModelText(modelText: String?) {
        if (modelText != null) {
            val str = modelText.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            numberLine = str.size
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
                this.modelText = model
            } else {
                this.modelText = modelText
            }
        }
    }

    protected fun processAttributeSet(attrs: AttributeSet) {
        //This method reads the parameters given in the xml file and sets the properties according to it
        this.setModelText(attrs.getAttributeValue(null, "model_text"))
    }
}