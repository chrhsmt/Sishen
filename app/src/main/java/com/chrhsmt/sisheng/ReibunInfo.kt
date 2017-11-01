package com.chrhsmt.sisheng

import android.content.Context
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Created by hkimura on 2017/10/26.
 */
class ReibunInfo {
    companion object {
        private var instance : ReibunInfo? = null

        fun  getInstance(context: Context): ReibunInfo {
            if (instance == null)
                instance = ReibunInfo(context)

            return instance!!
        }

        fun removeNewLine(str: String) : String {
            return str.replace("\\n", "")
        }

        fun replaceNewLine(str: String) : String {
            return str.replace("\\n", "\n")
        }
    }

    class ReibunInfoItem {
        var id : Int = -1
        var lecture : String = ""
        var pinyin : String = ""
        var chinese : String = ""
        var japanese : String = ""
        var english : String = ""

        fun getMFSZExampleAudioFileName(): String {
            return "mfsz/" + ReibunInfo.instance!!.audioFileNameList.first { asset -> asset.matches(Regex(String.format("%d_(f|m)\\.wav", this.id))) }
        }
    }

    enum class SENTENCE_TYPE(val rawValue: Int) {
        PINYIN(1),
        CHINESE(2),
        JAPANESE(3),
        ENGLISH(4),
    }
    private var itemList : ArrayList<ReibunInfoItem> = ArrayList()
    var selectedItem : ReibunInfoItem? = null

    // mfsz用お手本音源ファイル名List
    private val audioFileNameList: Array<String>

    private constructor(context: Context) {
        // assets情報の取得
        val message = context.resources.assets.open("reibun_list.xml").reader(charset=Charsets.UTF_8).use{it.readText()}

        // XMLの読み込み
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xpp = factory.newPullParser()
        xpp.setInput(StringReader(message))

        // ここからXMLを解析していく
        var eventType = xpp.eventType
        var item: ReibunInfoItem? = null
        var xppName: String = ""
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_DOCUMENT) {
                println("Start document")
            } else if (eventType == XmlPullParser.START_TAG) {
                System.out.println("Start tag " + xpp.name)
                // センテンス開始処理
                if (xpp.name == "SENTENCE") {
                    item = ReibunInfoItem()
                }
                xppName = xpp.name
            } else if (eventType == XmlPullParser.END_TAG) {
                System.out.println("End tag " + xpp.name)
                // センテンス終了処理
                if (xpp.name == "SENTENCE" && item != null) {
                    itemList.add(item)
                }
                xppName = ""
            } else if (eventType == XmlPullParser.TEXT) {
                System.out.println("Name" + xpp.name + " Text " + xpp.text)
                // 各要素処理
                if (item != null) {
                    when (xppName){
                        "ID" -> item.id = xpp.text.toInt()
                        "PINYIN" -> item.pinyin = xpp.text
                        "CHINESE" -> item.chinese = xpp.text
                        "JAPANESE" -> item.japanese = xpp.text
                        "ENGLISH" -> item.english = xpp.text
                    }
                }
            }
            eventType = xpp.nextToken() // xpp.next()かどちらかを使う
        }

        // お手本音源ファイル名取得
        this.audioFileNameList = context.assets.list("mfsz")
    }

    fun getSentenceList(type : SENTENCE_TYPE, needRemoveNewLine: Boolean) : ArrayList<String> {
        val ret: ArrayList<String> = ArrayList()

        for (item in itemList) {
            when (type) {
                SENTENCE_TYPE.PINYIN -> ret.add(item.pinyin)
                SENTENCE_TYPE.CHINESE -> ret.add(item.chinese)
                SENTENCE_TYPE.JAPANESE -> ret.add(item.japanese)
                SENTENCE_TYPE.ENGLISH -> ret.add(item.english)
            }
        }

        if (needRemoveNewLine) {
            for (index in ret.indices) {
                ret[index] = removeNewLine(ret[index])
            }
        }
        return ret;
    }
    fun getSentenceList(type1 : SENTENCE_TYPE, type2 : SENTENCE_TYPE, needRemoveNewLine: Boolean) : ArrayList<String> {
        val ret: ArrayList<String> = ArrayList()

        for (item in itemList) {
            var str = ""
            when (type1) {
                SENTENCE_TYPE.PINYIN -> str = item.pinyin
                SENTENCE_TYPE.CHINESE -> str = item.chinese
                SENTENCE_TYPE.JAPANESE -> str = item.japanese
                SENTENCE_TYPE.ENGLISH -> str = item.english
            }
            when (type2) {
                SENTENCE_TYPE.PINYIN -> str = str + "\n" + item.pinyin
                SENTENCE_TYPE.CHINESE -> str = str + "\n" + item.chinese
                SENTENCE_TYPE.JAPANESE -> str = str + "\n" + item.japanese
                SENTENCE_TYPE.ENGLISH -> str = str + "\n" + item.english
            }

            ret.add(str)
        }

        if (needRemoveNewLine) {
            for (index in ret.indices) {
                ret[index] = removeNewLine(ret[index])
            }
        }
        return ret;
    }
    fun setSelectedItem(position : Int) {
        selectedItem = itemList[position]
    }
}