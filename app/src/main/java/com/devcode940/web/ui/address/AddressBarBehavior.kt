package com.devcode940.web.ui.address

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import com.google.android.material.appbar.AppBarLayout

/**
 * Behavior to auto-hide address bar on scroll (Feature Polish)
 */
class AddressBarBehavior : CoordinatorLayout.Behavior<View>() {

    override fun onStartNestedScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        directTargetChild: View,
        target: View,
        axes: Int,
        type: Int
    ): Boolean {
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL
    }

    override fun onNestedPreScroll(
        coordinatorLayout: CoordinatorLayout,
        child: View,
        target: View,
        dx: Int,
        dy: Int,
        consumed: IntArray,
        type: Int
    ) {
        if (dy > 0) {
            // Scrolling down → hide address bar
            child.visibility = View.GONE
        } else if (dy < 0) {
            // Scrolling up → show address bar
            child.visibility = View.VISIBLE
        }
    }
}