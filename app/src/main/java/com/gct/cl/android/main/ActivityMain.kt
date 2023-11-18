package com.gct.cl.android.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.gct.cl.android.R
import com.google.android.material.tabs.TabLayout


class ActivityMain : AppCompatActivity() {
    private val mainTab: TabLayout by lazy { findViewById(R.id.activityMain_widgets_mainLayout) }
    private val mainViewer: ViewPager2 by lazy { findViewById(R.id.activityMain_widgets_pageViewer) }
    private val mainViewerAdapter by lazy { PagesAdapter(this) }

    private inner class TabSelectedListener : TabLayout.OnTabSelectedListener {
        override fun onTabSelected(p0: TabLayout.Tab?) {
            mainViewer.currentItem = p0?.position ?: 0
        }

        override fun onTabUnselected(p0: TabLayout.Tab?) {

        }

        override fun onTabReselected(p0: TabLayout.Tab?) {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binder()

        supportActionBar?.hide()
    }

    private fun binder() {
        mainTab.apply {
            addOnTabSelectedListener(TabSelectedListener())
        }
        mainViewer.apply {
            setAdapter(mainViewerAdapter)
            setUserInputEnabled(false)
        }
    }
}