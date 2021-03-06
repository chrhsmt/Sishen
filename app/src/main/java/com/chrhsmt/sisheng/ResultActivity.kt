package com.chrhsmt.sisheng

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.chrhsmt.sisheng.font.FontUtils
import com.chrhsmt.sisheng.ui.ScreenUtils
import org.w3c.dom.Text

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
        ScreenUtils.setFullScreen(this.window)
        ScreenUtils.setScreenBackground(this)

        // タイトル、メッセージ、スコアのフォントを変更する
        FontUtils.changeFont(this, R.id.txtTitle)
        FontUtils.changeFont(this, R.id.txtMessage1)
        FontUtils.changeFont(this, R.id.txtMessage2)
        FontUtils.changeFont(this, R.id.txtScore)

        // ボタンのフォントを変更する
        FontUtils.changeFont(this, R.id.btnRestart)
        FontUtils.changeFont(this, R.id.btnRetry)

        // 分析結果の点数を表示する
        this.findViewById<TextView>(R.id.txtScore)?.text = intent.getStringExtra("score")

        // 初期画面に戻る
        // その際、今までの画面遷移のスタックは全て削除する
        // 初期画面は再生成する (onCreate が走るようにする)
        this.findViewById<View>(R.id.btnRestart)?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@ResultActivity, FirstScreen::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
            overridePendingTransition(0, 0);
        })

        // １つ前の画面に戻る
        // その際、１つ前の画面は再生成する (onRestart ではなく、onCreate が走るようにする)
        this.findViewById<View>(R.id.btnRetry)?.setOnClickListener(View.OnClickListener {
            val intent = Intent(this@ResultActivity, ReibunActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent)
            overridePendingTransition(0, 0);
        })

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