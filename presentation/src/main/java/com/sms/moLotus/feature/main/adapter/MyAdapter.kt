package com.sms.moLotus.feature.main.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.sms.moLotus.feature.main.fragment.ChatFragment
import com.sms.moLotus.feature.main.fragment.SMSFragment

class MyAdapter(private val myContext: Context, fm: FragmentManager, internal var totalTabs: Int) :
    FragmentPagerAdapter(fm) {

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                SMSFragment()
            }
            1 /*-> {

                MGRAMFragment()
            }
            2*/ -> {
                ChatFragment()
            }

            else -> SMSFragment()
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE
    }
}