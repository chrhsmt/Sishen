package com.chrhsmt.sisheng

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.util.Log
import com.chrhsmt.sisheng.R.drawable.shape_rounded_corners_30dp
import com.chrhsmt.sisheng.R.drawable.shape_rounded_corners_30dp_selected
import com.chrhsmt.sisheng.font.FontUtils
import com.chrhsmt.sisheng.network.MDnsResolver
import com.chrhsmt.sisheng.ui.ScreenUtils
// 画面用にこれをimport しておく
import kotlinx.android.synthetic.main.activity_first_screen.*
import java.util.*

class FirstScreen : AppCompatActivity() {

    var mMDnsResolver: MDnsResolver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_screen)

        // mDNS
        mMDnsResolver = MDnsResolver(this)
        mMDnsResolver!!.discoverServices()

        // フルスクリーンにする
        ScreenUtils.setFullScreen(this.window)

        // タイトルのフォントを変更する
        FontUtils.changeFont(this, textFirstScreenTitle)
        FontUtils.changeFont(this, textFirstScreenTitle2)

        // ボタンのフォントを変更する
        FontUtils.changeFont(this, btnNini)
        FontUtils.changeFont(this, btnRandom)
        FontUtils.changeFont(this, btnWoman)
        FontUtils.changeFont(this, btnMan)

        // 画面遷移に関わるランダム、任意ボタンを一旦無効にする
        setRandomAndNiniButtonEnable(false)

        // ランダム例文に移動
        btnRandom.setOnClickListener(View.OnClickListener {
            val reibunInfo = ReibunInfo.getInstance(this)

            //todo: ランダムに例文を渡す処理
            val random = Random().nextInt(reibunInfo.getSentenceList(ReibunInfo.SENTENCE_TYPE.CHINESE, false).size)
            reibunInfo.setSelectedItem(random.toInt())

            val intent = Intent(this@FirstScreen,
                    ReibunActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0);
        })

        // 任意例文に移動
        btnNini.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@FirstScreen,
                    NiniReibunActivity::class.java)
            startActivity(intent)
            overridePendingTransition(0, 0);
        })

        // お手本の声を女性に設定
        btnWoman.setOnClickListener(View.OnClickListener {
            selectWoman()
        })

        // お手本の声を男性に設定
        btnMan.setOnClickListener(View.OnClickListener {
            selectMan()
        })

        // お手本の声を女性にデフォルト設定
        //selectMan()


        // タイトル長押下された場合は、デバッグ画面に遷移する。
        if (Settings.DEBUG_MODE) {
            textFirstScreenTitle.setOnLongClickListener(View.OnLongClickListener {
                val intent = Intent(this@FirstScreen, MainActivity::class.java)
                startActivity(intent)
                true
            })
        }
    }

    private fun selectWoman() {
        btnMan.setBackgroundResource(shape_rounded_corners_30dp)
        btnWoman.setBackgroundResource(shape_rounded_corners_30dp_selected)
        Settings.sex = resources.getStringArray(R.array.sexes)[1]
        setRandomAndNiniButtonEnable(true)
    }

    private fun selectMan() {
        btnMan.setBackgroundResource(shape_rounded_corners_30dp_selected)
        btnWoman.setBackgroundResource(shape_rounded_corners_30dp)
        Settings.sex = resources.getStringArray(R.array.sexes)[0]
        setRandomAndNiniButtonEnable(true)
    }

    private fun setRandomAndNiniButtonEnable(enable: Boolean) {
        btnRandom.setEnabled(enable)
        btnNini.setEnabled(enable)
    }

    override fun onPause() {
        super.onPause()
        this.mMDnsResolver?.let { it ->
            it.tearDown()
        }
    }

    override fun onResume() {
        super.onResume()
        this.mMDnsResolver?.let { it ->
            it.discoverServices()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        this.mMDnsResolver?.let { it ->
            it.tearDown()
        }
    }
}
