package com.sms.moLotus.feature.chat.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.ORIENTATION_HORIZONTAL
import com.sms.moLotus.R
import com.sms.moLotus.feature.chat.adapter.ViewPagerRecyclerAdapter
import kotlinx.android.synthetic.main.activity_view_pager_adapter.*

class ViewPagerAdapterActivity : AppCompatActivity() {
    var url = ""
    private var attachmentList: ArrayList<String> = ArrayList()
    var pos = 0
    var viewPager2PageChangeCallback : ViewPager2PageChangeCallback ?= null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_adapter)
        url = intent?.getStringExtra("url").toString()
        attachmentList = intent?.getStringArrayListExtra("attachmentList") as ArrayList<String>
        val mViewPagerAdapter = ViewPagerRecyclerAdapter(this, this)
        viewPager.adapter = mViewPagerAdapter
        mViewPagerAdapter.setItem(attachmentList)
        viewPager.offscreenPageLimit = 3
        viewPager.setPageTransformer { page, position ->
            val offset = position * -(2 * 20 )
            if (viewPager.orientation == ORIENTATION_HORIZONTAL) {
                if (ViewCompat.getLayoutDirection(viewPager) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                    page.translationX = -offset
                } else {
                    page.translationX = offset
                }
            } else {
                page.translationY = offset
            }
        }

        if (attachmentList.contains(url)) {
            pos = attachmentList.indexOf(url)
        }

        viewPager2PageChangeCallback = ViewPager2PageChangeCallback(pos) {
            viewPager.setCurrentItem(it, false)
        }

        viewPager2PageChangeCallback?.let { viewPager.registerOnPageChangeCallback(it) }

    }

    class ViewPager2PageChangeCallback(var pos: Int,private val listener: (Int) -> Unit) :
        ViewPager2.OnPageChangeCallback() {
        val selectedPos = pos
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            when(position){
                0 ->  listener.invoke(selectedPos)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewPager2PageChangeCallback?.let { viewPager.unregisterOnPageChangeCallback(it) }
    }

    private val defaultPageTransformer = ViewPager2.PageTransformer { page, _ ->
        page.apply {
            translationX = 0f
            translationY = 0f
            scaleX = 1f
            scaleY = 1f
            alpha = 1f
        }
    }
}