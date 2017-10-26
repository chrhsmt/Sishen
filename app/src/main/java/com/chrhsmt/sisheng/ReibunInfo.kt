package com.chrhsmt.sisheng

import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

/**
 * Created by hkimura on 2017/10/26.
 */
class ReibunInfo {

    private val mSource : String = "<LECTURE><SENTENCE><PINYIN>Nǐ hǎo</PINYIN><CHINESE>你好</CHINESE><JAPANESE>こんにちは</JAPANESE><ENGLISH>Hello</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE><SENTENCE><PINYIN>Xièxiè nǐ</PINYIN><CHINESE>谢谢你</CHINESE><JAPANESE>ありがとうございます</JAPANESE><ENGLISH>Thank you</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>16课<SENTENCE><PINYIN>Tā zhèngzài zhǎo gōngzuò.</PINYIN><CHINESE>他正在找工作。</CHINESE><JAPANESE>彼は今まさに仕事を探しています。</JAPANESE><ENGLISH>He is looking for a job.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>17课<SENTENCE><PINYIN>Wǒ juéde yíhéyuán de fēngjǐng tèbié měi.</PINYIN><CHINESE>我觉得颐和园的风景特别美。</CHINESE><JAPANESE>頤和園の風景は特に美しいと思います。</JAPANESE><ENGLISH>I think view of 頤和園 has especially beautiful </ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>17课<SENTENCE><PINYIN>Nǐ qǐng tā chī běijīng kǎoyāle ma?</PINYIN><CHINESE>你请他吃北京烤鸭了吗？</CHINESE><JAPANESE>彼に北京ダックを食べさせましたか？</JAPANESE><ENGLISH>Have you fed him Peking duck?</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>18课<SENTENCE><PINYIN>Wán de shíjiān shǎole.</PINYIN><CHINESE>玩的时间少了。</CHINESE><JAPANESE>遊ぶ時間が少なくなってしまいました。</JAPANESE><ENGLISH>No time to have fun.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>19课<SENTENCE><PINYIN>Nǐ yóuyǒng yóu de zěnme yàng?</PINYIN><CHINESE>你游泳游得怎么样？</CHINESE><JAPANESE>あなたは泳ぐのはどうですか？（得意ですか？）</JAPANESE><ENGLISH>How is your swimming skill？</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>20课<SENTENCE><PINYIN>Yīnwéi lùshàng dǔchēle, suǒyǐ qìchē kāile wǔshí fēnzhōng cái dào.</PINYIN><CHINESE>因为路上堵车了，所以汽车开了50分钟才到。</CHINESE><JAPANESE>道路が渋滞していたので、50分運転してやっと着きました。</JAPANESE><ENGLISH>I finally arrived after a 50-minute car drive due to a traffic jam.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>21课<SENTENCE><PINYIN>Dì yī cì shì zài quánjùdé chī de, dì èr cì shì zài xuéxiào fùjìn de fànguǎn chī de</PINYIN><CHINESE>第一次是在全聚德吃的，第二次是在学校附近的饭馆吃的。</CHINESE><JAPANESE>最初は全聚徳で食べ、次は学校の近くのレストランで食べました。</JAPANESE><ENGLISH>First we had a meal here, and then at another restaurant near the school.  </ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>22课<SENTENCE><PINYIN>Wǒ hái cónglái méi xuéguò Hànyǔgē ne.</PINYIN><CHINESE>我还从来没学过汉语歌呢。</CHINESE><JAPANESE>私はこれまでに中国語の歌を習ったことがありません。</JAPANESE><ENGLISH>I have never learn Chinese songs.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>23课<SENTENCE><PINYIN>Wéi, shì liúxuéshēng sùshè ma?</PINYIN><CHINESE>喂，是留学生宿舍吗？</CHINESE><JAPANESE>もしもし、留学生宿舎ですか？</JAPANESE><ENGLISH>Hello ,is the student dormitory?</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>24课<SENTENCE><PINYIN>Nàme, dǎrǎo nǐle.</PINYIN><CHINESE>那么，打扰你了。</CHINESE><JAPANESE>じゃあ、お邪魔します。</JAPANESE><ENGLISH>Well, thank you for having me.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>24课<SENTENCE><PINYIN>Zhēn bàoqiàn, ràng nǐ jiǔ děngle.</PINYIN><CHINESE>真抱歉，让你久等了。</CHINESE><JAPANESE>すみません、お待たせしました。</JAPANESE><ENGLISH>So, I'm sorry to have kept you waiting.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>25课<SENTENCE><PINYIN>Qī diǎn yǐqián, huí de lái huí bu lái?</PINYIN><CHINESE>7点以前，回得来回不来？</CHINESE><JAPANESE>7時前に戻ってこられますか？</JAPANESE><ENGLISH>Will you come back before 7 ?</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>26课<SENTENCE><PINYIN>Nǐ háishì gēn jiālǐ rén zài hǎo hao shāngliang shāngliang ba.</PINYIN><CHINESE>你还是跟家里人再好好商量商量吧。</CHINESE><JAPANESE>さらに家族とよく相談しなくちゃ。</JAPANESE><ENGLISH>You still talk with the family no matter how good it is to discuss it</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>27课<SENTENCE><PINYIN>Nǐ néng bùnéng bǎ nǐ de zìxíngchē jiè gěi wǒ yòng yòng.</PINYIN><CHINESE>你能不能把你的自行车借给我用用。</CHINESE><JAPANESE>あなたの自転車をちょっと借りてもいいですか？</JAPANESE><ENGLISH>Can you lend me your bike for use?</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>28课<SENTENCE><PINYIN>Chōuyān duì shēntǐ méiyǒu hǎochù</PINYIN><CHINESE>抽烟对身体没有好处。</CHINESE><JAPANESE>喫煙は体によくありません。</JAPANESE><ENGLISH>Smoking is not good for the body.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>29课<SENTENCE><PINYIN>Nǐ zěnmele?</PINYIN><CHINESE>你怎么了？</CHINESE><JAPANESE>どうしましたか？</JAPANESE><ENGLISH>What  happened to you?</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>29课<SENTENCE><PINYIN>Wǒ jīntiān zhēn dǎoméi tòule.</PINYIN><CHINESE>我今天真倒霉透了。</CHINESE><JAPANESE>今日はまったくついていません。</JAPANESE><ENGLISH>I’ve had no luck today.</ENGLISH></SENTENCE></LECTURE>\n" +
    "<LECTURE>29课<SENTENCE><PINYIN>Wǒ mǎshàng jiù qù</PINYIN><CHINESE>我马上就去。</CHINESE><JAPANESE>すぐに行きます。</JAPANESE><ENGLISH>I'll go right away.</ENGLISH></SENTENCE></LECTURE>\n"

    class ReibunInfoItem {
        var id : Int = -1
        var lecture : String = ""
        var pinyin : String = ""
        var chinese : String = ""
        var japanese : String = ""
        var english : String = ""
    }

    var itemList : ArrayList<ReibunInfoItem> = ArrayList()

    constructor() {
        val factory = XmlPullParserFactory.newInstance()
        factory.isNamespaceAware = true
        val xpp = factory.newPullParser()
        //val inputStream = InputStream()
        //xpp.setInput(InputStream, "UTF-8")
        xpp.setInput( StringReader ( mSource ) )

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