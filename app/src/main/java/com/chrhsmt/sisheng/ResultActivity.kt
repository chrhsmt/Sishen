package com.chrhsmt.sisheng

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import com.chrhsmt.sisheng.font.FontUtils

class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent = super.getIntent()
        if (intent.getBooleanExtra("result", true)) {
            setContentView(R.layout.activity_result_sucess)
        } else {
            setContentView(R.layout.activity_result_failure)
        }

        // フルスクリーンにする
        val decor = this.window.decorView
        decor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        this.findViewById<View>(R.id.btnRestart)?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@ResultActivity, FirstScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent)
            overridePendingTransition(0, 0);
        })
        FontUtils.changeButtonFont(this, R.id.btnRestart)

        this.findViewById<View>(R.id.btnRetry)?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@ResultActivity, ReibunActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
            overridePendingTransition(0, 0);
        })
        FontUtils.changeButtonFont(this, R.id.btnRetry)

        this.findViewById<TextView>(R.id.txtScore)?.text = intent.getStringExtra("score")

        // タイトル長押下された場合は、デバッグ画面に遷移する。
        if (Settings.DEBUG_MODE) {
            this.findViewById<View>(R.id.txtTitle).setOnLongClickListener(View.OnLongClickListener {
                val intent = Intent(this@ResultActivity, MainActivity::class.java)
                startActivity(intent)
                true
            })
        }
    }
}