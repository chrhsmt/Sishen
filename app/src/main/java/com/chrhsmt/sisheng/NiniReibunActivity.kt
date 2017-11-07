package com.chrhsmt.sisheng

import android.app.AlertDialog
import android.content.DialogInterface
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
import com.chrhsmt.sisheng.font.FontUtils
import com.chrhsmt.sisheng.ui.ScreenUtils
import kotlinx.android.synthetic.main.activity_reibun.*
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.io.StringReader


class NiniReibunActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nini_reibun)

        // フルスクリーンにする
        ScreenUtils.setFullScreen(this.window)
        ScreenUtils.setScreenBackground(this)

        // タイトルのフォントを変更する
        FontUtils.changeFont(this, txtNiniReibun)

        // 例文のリストを表示する
        val reibunInfo = ReibunInfo.getInstance(this)
        val reibunInfoList = if (Settings.PRACTICE_STAFF_SCRIPT)
            reibunInfo.getSentenceList(ReibunInfo.SENTENCE_TYPE.LECTURE, ReibunInfo.SENTENCE_TYPE.CHINESE, ReibunInfo.SENTENCE_TYPE.ENGLISH, true)
        else
            reibunInfo.getSentenceList(ReibunInfo.SENTENCE_TYPE.CHINESE, ReibunInfo.SENTENCE_TYPE.ENGLISH, true)
        val adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, reibunInfoList)
        listReibun.adapter = adapter as ListAdapter

        listReibun.setOnItemClickListener { parent, view, position, id ->
            // TODO
            // ダイアログ表示は行わず、例文再生画面に遷移する。
            /*
                AlertDialog.Builder(this)
                        .setMessage(resources.getText(R.string.screen2_2_1))
                        .setNegativeButton(resources.getText(R.string.screen2_2_3), DialogInterface.OnClickListener { dialog, which ->
                            dialog.dismiss()
                        })
                        .setPositiveButton(resources.getText(R.string.screen2_2_2), DialogInterface.OnClickListener { dialog, which ->
                            reibunInfo.setSelectedItem(position)
                            val audioName = resources.getStringArray(R.array.sample_audios)[reibunInfo.selectedItem!!.id % 6]
                            Settings.sampleAudioFileName = audioName

                            val intent = Intent(this@NiniReibunActivity, ReibunActivity::class.java)
                            startActivity(intent)
                        })
                        .show()
            */
                reibunInfo.setSelectedItem(position)

                val intent = Intent(this@NiniReibunActivity, ReibunActivity::class.java)
                startActivity(intent)
                overridePendingTransition(0, 0);
            }

        // タイトル長押下された場合は、デバッグ画面に遷移する。
        if (Settings.DEBUG_MODE) {
            txtNiniReibun.setOnLongClickListener(View.OnLongClickListener {
                val intent = Intent(this@NiniReibunActivity, MainActivity::class.java)
                startActivity(intent)
                true
            })
        }
    }
}
