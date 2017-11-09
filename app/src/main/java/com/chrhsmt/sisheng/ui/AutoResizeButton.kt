package com.chrhsmt.sisheng.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.Button

/**
 * フォントサイズ自動調整Button
 *
 * ソースコードは以下から借用
 * http://aillicepray.blogspot.jp/2015/02/textview.html
 */
class AutoResizeButton : Button {

    internal val info = AutoResizeUtils.AutoResizeInfo()

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
        //This method reads the parameters given in the xml file and sets the properties according to it
        AutoResizeUtils.setModelText(attrs.getAttributeValue(null, "model_text"), info)
    }

    /**
     * 子Viewの位置を決める
     */
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        AutoResizeUtils.resize(this, info)
    }

}