package com.chrhsmt.sisheng

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListAdapter
import android.widget.SimpleAdapter
import kotlinx.android.synthetic.main.activity_nini_reibun.*
import android.widget.AdapterView
import org.xmlpull.v1.XmlPullParserFactory
import android.util.Xml.newPullParser
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.StringReader


class NiniReibunActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nini_reibun)

        // フルスクリーンにする
        val decor = this.window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        val reibunInfo = ReibunInfo()
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, reibunInfo.getChineseList())
        listReibun.adapter = adapter as ListAdapter


        // ランダム例文に移動
        listReibun.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this@NiniReibunActivity,
                    ReibunActivity::class.java)
            startActivity(intent)
        }


    }
}
