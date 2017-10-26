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

    private var itemList : ArrayList<ReibunInfoItem> = ArrayList()

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

    fun getChineseList() : ArrayList<String> {
        var ret: ArrayList<String> = ArrayList()

        for (item in itemList) {
            ret.add(item.chinese)
        }

        return ret;
    }
}