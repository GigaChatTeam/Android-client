package com.gct.cl.android.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.gct.cl.android.R

class PagesAdapter(fragmentActivity: FragmentActivity?) : FragmentStateAdapter(fragmentActivity!!) {
    override fun createFragment(position: Int): Fragment {
        return MainPage(layouts[position]!!)
    }

    override fun getItemCount(): Int {
        return 4
    }

    companion object {
        private val layouts = HashMap<Int, Int>().apply {
            put(0, R.layout.main_page_notifications)
            put(1, R.layout.main_page_feed)
            put(2, R.layout.main_page_channels)
            put(3, R.layout.main_page_settings)
        }
    }
}