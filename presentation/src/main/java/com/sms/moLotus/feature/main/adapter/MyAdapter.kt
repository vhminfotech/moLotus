package com.sms.moLotus.feature.main.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sms.moLotus.feature.main.fragment.ChatFragment
import com.sms.moLotus.feature.main.fragment.SMSFragment

class MyAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) : FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                ChatFragment()
            }
            1 -> {
                SMSFragment()
            }

            else ->  ChatFragment()
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}