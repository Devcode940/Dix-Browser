package com.devcode940.web.utils

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.devcode940.web.contract.ITab

object FragmentBackHandleHelper {

    @JvmStatic
    fun isFragmentBackHandled(fragmentManager: FragmentManager): Boolean {
        val fragments = fragmentManager.fragments
        if (CollectionUtils.isEmpty(fragments)) {
            return false
        }
        for (fragment in fragments) {
            if (isFragmentBackable(fragment)) {
                return true
            }
            if (fragmentManager.backStackEntryCount > 0) {
                fragmentManager.popBackStack()
                return true
            }
        }
        return false
    }

    @JvmStatic
    fun isFragmentBackable(fragment: Fragment): Boolean {
        return fragment.isVisible &&
            fragment.userVisibleHint &&
            fragment is ITab &&
            (fragment as ITab).onBackPressed()
    }
}
