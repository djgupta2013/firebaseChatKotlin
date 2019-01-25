package com.wildnet.firebasechatkotlin.fragment

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class MyPagerAdapter(fm: FragmentManager?, titles: Array<CharSequence>, numboftabs: Int) : FragmentPagerAdapter(fm) {

    private val title = titles
    private val numboftab = numboftabs

    override fun getItem(p: Int): Fragment {
        if (p == 0) {
            return UserListFragment()
        }
        if (p == 1) {
            return GroupListFragment()
        }
        return UserListFragment()
    }

    override fun getCount(): Int {
        return numboftab
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return title[position]
    }
}