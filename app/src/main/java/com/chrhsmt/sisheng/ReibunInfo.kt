package com.chrhsmt.sisheng

import android.content.Context
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Created by hkimura on 2017/10/26.
 */
class ReibunInfo {
    companion object {
        private val TAG = "ReibunInfo"
        private var instance : ReibunInfo? = null
        private var instanceForTest : ReibunInfo? = null

        fun  getInstance(context: Context): ReibunInfo {

            if (Settings.MFSZ_2018_SCRIPT) {
                if (instance == null)
                    instance = ReibunInfo(context, "mfsz2018/reibun_list.xml")

                return instance!!

            } else if (Settings.PRACTICE_STAFF_SCRIPT) {
                if (instanceForTest == null)
                    instanceForTest = ReibunInfo(context, "staff_script_list.xml")

                return instanceForTest!!
            }
            else
            {
                if (instance == null)
                    instance = ReibunInfo(context, "reibun_list.xml")

                return instance!!
            }
        }

        private fun  myInstance(): ReibunInfo? {
            if (Settings.PRACTICE_STAFF_SCRIPT) {
                return instanceForTest
            }
            else
            {
                return instance
            }
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
        var category: String = ""
        var pinyin : String = ""
        var chinese : String = ""
        var japanese : String = ""
        var english : String = ""

        fun getMFSZExampleAudioFileName(): String {
            var prefix = "mfsz/"
            var format = "%d_(f|m)\\.wav"
            if (Settings.MFSZ_2018_SCRIPT) {
                prefix = "mfsz2018/voices/"
                format = Settings.sex!!.first().toUpperCase() + "%02d\\.wav"
            }
            return prefix + ReibunInfo.myInstance()!!.audioFileNameList.first { asset -> asset.matches(Regex(String.format(format, this.id))) }
        }
    }

    enum class SENTENCE_TYPE(val rawValue: Int) {
        LECTURE(0),
        PINYIN(1),
        CHINESE(2),
        JAPANESE(3),
        ENGLISH(4),
        NONE(5),
    }

    private var itemList : ArrayList<ReibunInfoItem> = ArrayList()

    var selectedItem : ReibunInfoItem? = null

    // mfsz用お手本音源ファイル名List
    private val audioFileNameList: Array<String>

    private constructor(context: Context, listName: String) {
        // assets情報の取得
        val message = context.resources.assets.open(listName).reader(charset=Charsets.UTF_8).use{it.readText()}

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
                Log.d(TAG,"Start tag " + xpp.name)
                // センテンス開始処理
                if (xpp.name == "SENTENCE") {
                    item = ReibunInfoItem()
                }
                xppName = xpp.name
            } else if (eventType == XmlPullParser.END_TAG) {
                Log.d(TAG,"End tag " + xpp.name)
                // センテンス終了処理
                if (xpp.name == "SENTENCE" && item != null) {
                    itemList.add(item)
                }
                xppName = ""
            } else if (eventType == XmlPullParser.TEXT) {
                Log.d(TAG,"Name" + xpp.name + " Text " + xpp.text)
                // 各要素処理
                if (item != null) {
                    when (xppName){
                        "ID" -> item.id = xpp.text.toInt()
                        "LECTURE" -> item.lecture = xpp.text
                        "CATEGORY" -> item.category = xpp.text
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
        if (Settings.MFSZ_2018_SCRIPT) {
            this.audioFileNameList = context.assets.list("mfsz2018/voices")
        } else {
            this.audioFileNameList = context.assets.list("mfsz")
        }
    }

    fun getSentenceList(type : SENTENCE_TYPE, needRemoveNewLine: Boolean) : ArrayList<String> {
        return getSentenceList(type, SENTENCE_TYPE.NONE, SENTENCE_TYPE.NONE, needRemoveNewLine)
    }
    fun getSentenceList(type1 : SENTENCE_TYPE, type2 : SENTENCE_TYPE, needRemoveNewLine: Boolean) : ArrayList<String> {
        return getSentenceList(type1, type2, SENTENCE_TYPE.NONE, needRemoveNewLine)
    }
    fun getSentenceList(type1 : SENTENCE_TYPE, type2 : SENTENCE_TYPE, type3 : SENTENCE_TYPE, needRemoveNewLine: Boolean) : ArrayList<String> {
        val ret: ArrayList<String> = ArrayList()

        for (item in itemList) {
            var str = ""
            when (type1) {
                SENTENCE_TYPE.LECTURE -> str = item.lecture
                SENTENCE_TYPE.PINYIN -> str = item.pinyin
                SENTENCE_TYPE.CHINESE -> str = item.chinese
                SENTENCE_TYPE.JAPANESE -> str = item.japanese
                SENTENCE_TYPE.ENGLISH -> str = item.english
            }
            when (type2) {
                SENTENCE_TYPE.LECTURE -> str = str + "\n" + item.lecture
                SENTENCE_TYPE.PINYIN -> str = str + "\n" + item.pinyin
                SENTENCE_TYPE.CHINESE -> str = str + "\n" + item.chinese
                SENTENCE_TYPE.JAPANESE -> str = str + "\n" + item.japanese
                SENTENCE_TYPE.ENGLISH -> str = str + "\n" + item.english
            }
            when (type3) {
                SENTENCE_TYPE.LECTURE -> str = str + "\n" + item.lecture
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
        return ret
    }
    fun setSelectedItem(position : Int) {
        selectedItem = itemList[position]
    }

    fun getItemList(): List<ReibunInfoItem> {
        return this.itemList
    }
}