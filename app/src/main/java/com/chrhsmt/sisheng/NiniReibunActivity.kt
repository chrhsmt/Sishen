package com.chrhsmt.sisheng

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.activity_nini_reibun.*

class NiniReibunActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nini_reibun)

        // フルスクリーンにする
        val decor = this.window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)


        //todo: 例文表示


        // ランダム例文に移動
        btnSentaku.setOnClickListener(View.OnClickListener {
            //todo: 例文を渡す処理
            val intent = Intent(this@NiniReibunActivity,
                    ReibunActivity::class.java)
            startActivity(intent)
        })


    }
}
