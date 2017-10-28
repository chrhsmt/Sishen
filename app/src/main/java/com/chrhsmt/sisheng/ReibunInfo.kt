package com.chrhsmt.sisheng

import android.content.Context
import android.content.res.AssetManager
import android.content.res.Resources
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
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
    }

    class ReibunInfoItem {
        var id : Int = -1
        var lecture : String = ""
        var pinyin : String = ""
        var chinese : String = ""
        var japanese : String = ""
        var english : String = ""
    }

    enum class SENTENCE_TYPE(val rawValue: Int) {
        PINYIN(1),
        CHINESE(2),
        JAPANESE(3),
        ENGLISH(4),
    }
    private var itemList : ArrayList<ReibunInfoItem> = ArrayList()
    var selectedItem : ReibunInfoItem? = null

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
    }

    fun getSentenceList(type : SENTENCE_TYPE) : ArrayList<String> {
        val ret: ArrayList<String> = ArrayList()

        for (item in itemList) {
            when (type) {
                SENTENCE_TYPE.PINYIN -> ret.add(item.pinyin)
                SENTENCE_TYPE.CHINESE -> ret.add(item.chinese)
                SENTENCE_TYPE.JAPANESE -> ret.add(item.japanese)
                SENTENCE_TYPE.ENGLISH -> ret.add(item.english)
            }
        }

        return ret;
    }
    fun getSentenceList(type1 : SENTENCE_TYPE, type2 : SENTENCE_TYPE) : ArrayList<String> {
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

        return ret;
    }
    fun setSelectedItem(position : Int) {
        selectedItem = itemList[position]
    }
}