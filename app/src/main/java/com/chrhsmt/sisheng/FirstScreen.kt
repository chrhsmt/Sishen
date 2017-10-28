package com.chrhsmt.sisheng

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.parseColor
import android.widget.ImageButton
import com.chrhsmt.sisheng.R.color.colorActivate
import com.chrhsmt.sisheng.R.drawable.shape_rounded_corners_5dp
// 画面用にこれをimport しておく
import kotlinx.android.synthetic.main.activity_first_screen.*

class FirstScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_screen)
        // フルスクリーンにする
        val decor = this.window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        // ランダム例文に移動
        btnRandom.setOnClickListener(View.OnClickListener {
            //todo: ランダムに例文を渡す処理

            val intent = Intent(this@FirstScreen,
                    ReibunActivity::class.java)
            startActivity(intent)
        })

        // 任意例文に移動
        btnNini.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@FirstScreen,
                    NiniReibunActivity::class.java)
            startActivity(intent)
        })

        // お手本の声を女性に設定
        btnWoman.setOnClickListener(View.OnClickListener {
            selectWoman()
        })

        // お手本の声を男性に設定
        btnMan.setOnClickListener(View.OnClickListener {
            selectMan()
        })

        selectWoman()
    }

    private fun selectWoman() {
        btnMan.setBackgroundResource(shape_rounded_corners_5dp)
        btnWoman.setBackgroundColor(resources.getColor(colorActivate, null))
        Settings.sex = resources.getStringArray(R.array.sexes)[1]
    }

    private fun selectMan() {
        btnMan.setBackgroundColor(resources.getColor(colorActivate, null))
        btnWoman.setBackgroundResource(shape_rounded_corners_5dp)
        Settings.sex = resources.getStringArray(R.array.sexes)[0]

    }
}
