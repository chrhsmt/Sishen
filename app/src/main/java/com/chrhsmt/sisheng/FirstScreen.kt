package com.chrhsmt.sisheng

// 画面用にこれをimport しておく
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import com.chrhsmt.sisheng.R.drawable.shape_rounded_corners_30dp
import com.chrhsmt.sisheng.R.drawable.shape_rounded_corners_30dp_selected
import com.chrhsmt.sisheng.font.FontUtils
import com.chrhsmt.sisheng.network.MDnsResolver
import com.chrhsmt.sisheng.persistence.ExternalMedia
import com.chrhsmt.sisheng.ui.ScreenUtils
import kotlinx.android.synthetic.main.activity_first_screen.*
import java.util.*


class FirstScreen : AppCompatActivity() {

    companion object {
        val TAG = "FirstScreen"
        val REQUEST_PERMISSION = 1000;
    }

    var mMDnsResolver: MDnsResolver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first_screen)

        // 外部記憶装置書き込み権限チェック
        this.checkPermission()

        // mDNS
        mMDnsResolver = MDnsResolver(this)
        mMDnsResolver!!.discoverServices()

        // フルスクリーンにする
        ScreenUtils.setFullScreen(this.window)
        ScreenUtils.setScreenBackground(this)

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

        if (Settings.DEBUG_MODE) {
            // タイトル長押下された場合は、デバッグ画面に遷移する。
            textFirstScreenTitle.setOnLongClickListener(View.OnLongClickListener {
                val intent = Intent(this@FirstScreen, MainActivity::class.java)
                startActivity(intent)
                true
            })

            // 背景長押下された場合は、スタッフ用スクリプト表示を切り返る。
            btnChangeScriptMode.setOnLongClickListener(View.OnLongClickListener {
                Settings.PRACTICE_STAFF_SCRIPT = !Settings.PRACTICE_STAFF_SCRIPT
                ScreenUtils.setScreenBackground(this@FirstScreen)
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

    private fun setUpReadWriteExternalStorage() {
        val dir = ExternalMedia.getExternalDirectoryPath(this)
        if (ExternalMedia.isExternalStorageWritable() && dir != null && dir!!.canWrite()) {
            Toast.makeText(this, "SDカードへの書き込みが可能です", Toast.LENGTH_SHORT).show()
            Log.d(TAG, String.format("External media path: %s", dir.absoluteFile))
        } else {
            Toast.makeText(this, "SDカードがマウントされていない、もしくは書き込みが不可能な状態です", Toast.LENGTH_SHORT).show()
        }
    }

    // permissionの確認
    private fun checkPermission() {
        // 既に許可している
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            setUpReadWriteExternalStorage()
        } else {
            requestPermission()
        }
    }

    // 許可を求める
    private fun requestPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)

        } else {
            val toast = Toast.makeText(this, "アプリ実行に許可が必要です", Toast.LENGTH_SHORT)
            toast.show()

            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_PERMISSION)
        }
    }

    // 結果の受け取り
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_PERMISSION) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpReadWriteExternalStorage()
                return

            } else {
                // それでも拒否された時の対応
                val toast = Toast.makeText(this, "録音した音声は保存されません", Toast.LENGTH_SHORT)
                toast.show()
            }
        }
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
