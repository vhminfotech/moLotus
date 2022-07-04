package com.sms.moLotus.feature.chat.ui

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.sms.moLotus.R
import com.sms.moLotus.feature.chat.adapter.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_view_pager_adapter.*

class ViewPagerAdapterActivity : AppCompatActivity() {
    var url = ""
    var attachmentList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_pager_adapter)
        url = intent.getStringExtra("url")
        attachmentList = intent.getStringArrayListExtra("attachmentList")

        Log.d("onAttachmentClick", "attachmentList::::$attachmentList")
        Log.d("onAttachmentClick", "url::::$url")
        val mViewPagerAdapter = ViewPagerAdapter(this, attachmentList)
        viewPager.adapter = mViewPagerAdapter

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                // your logic here
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // your logic here

                if (url == attachmentList[position]) {
                    onPageSelected(position)
                    viewPager.setCurrentItem(position, false)
                }
            }

            override fun onPageSelected(position: Int) {
                // your logic here

            }
        })

    }

}