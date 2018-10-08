package com.chrhsmt.sisheng

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.chrhsmt.sisheng.font.FontUtils
import com.chrhsmt.sisheng.ui.ScreenUtils
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.view.ViewPager
import android.support.v4.app.FragmentPagerAdapter








class NiniReibunActivityForTab : AppCompatActivity(), NiniReibunFragmentForTab.OnListFragmentInteractionListener {
    override fun onListFragmentInteraction(item: ReibunInfo.ReibunInfoItem?) {
        val reibunInfo = ReibunInfo.getInstance(this)
        reibunInfo.setSelectedItemByItem(item)

        val intent = Intent(this@NiniReibunActivityForTab, ReibunActivity::class.java)
        startActivity(intent)
        overridePendingTransition(0, 0);

    }

    private  var categoryList : ArrayList<String> = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nini_reibun_for_tab)

        // フルスクリーンにする
        ScreenUtils.setFullScreen(this.window)
        ScreenUtils.setScreenBackground(this)

        // タイトルのフォントを変更する
        FontUtils.changeFont(this, R.id.txtNiniReibun)

        // 例文のタブを表示する
        val reibunInfo = ReibunInfo.getInstance(this)
        this.categoryList.clear()
        this.categoryList.addAll(reibunInfo.getCategoryList())


        val viewPager = findViewById(R.id.view_pager) as ViewPager
        viewPager.adapter = ViewPagerAdapter(supportFragmentManager)

        val tabLayout = findViewById(R.id.tab_layout) as TabLayout
        tabLayout.setupWithViewPager(viewPager)

        /**
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // 例文のフラグメントを表示する
        val fragment = NiniReibunFragmentForTab.newInstance(0)
        supportFragmentManager.beginTransaction().replace(R.id.container, fragment).commit()
        */
    }

    private inner class ViewPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {
        override fun getItem(position: Int): Fragment? {
            var fragment: NiniReibunFragmentForTab? = null

            when (position) {
                0 -> fragment = NiniReibunFragmentForTab()
                1 -> fragment = NiniReibunFragmentForTab()
                2 -> fragment = NiniReibunFragmentForTab()
                3 -> fragment = NiniReibunFragmentForTab()
                4 -> fragment = NiniReibunFragmentForTab()
                else -> {
                }
            }
            if (fragment != null) {
                fragment.setCategory(categoryList.get(position))
            }
            return fragment
        }

        override fun getCount(): Int {
            return categoryList.size
        }

        override fun getPageTitle(position: Int): CharSequence {
            return categoryList.get(position)
        }
    }
}