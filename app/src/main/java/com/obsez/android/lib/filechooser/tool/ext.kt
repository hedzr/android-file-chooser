package com.obsez.android.lib.filechooser.tool

import android.R
import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.View
import android.view.inputmethod.InputMethodManager


@Suppress("MemberVisibilityCanBePrivate", "unused")
fun Activity.disableSoftKeyboard() {
    // Hide the keyboard when the button is pushed.
    val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputManager.hideSoftInputFromWindow(currentFocus!!.windowToken,
        InputMethodManager.HIDE_NOT_ALWAYS)
}

@Suppress("MemberVisibilityCanBePrivate", "unused", "DEPRECATION")
inline val Activity.networkInfo: NetworkInfo?
    get() {
        // Check the status of the network connection.
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connMgr.activeNetworkInfo
    }


@Suppress("MemberVisibilityCanBePrivate", "unused")
fun View.makeClickable(b: Boolean) = this.apply {
    this.isClickable = b
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        this.focusable = if (b) View.FOCUSABLE else View.NOT_FOCUSABLE
    }
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
fun View.addRipple() = with(TypedValue()) {
    context.theme.resolveAttribute(R.attr.selectableItemBackground, this, true)
    setBackgroundResource(resourceId)
    // cardView.setClickable(true)
    // this.recycle()
}

@Suppress("MemberVisibilityCanBePrivate", "unused")
fun View.addCircleRipple() = with(TypedValue()) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        context.theme.resolveAttribute(R.attr.selectableItemBackgroundBorderless, this, true)
        setBackgroundResource(resourceId)
    }
}

fun RecyclerView.changeLayoutManager(layoutManager: RecyclerView.LayoutManager, restoreScrollPosition: Boolean = false) {
    var scrollPosition = 0
    val savedAdapter = this.adapter
    
    // If a layout manager has already been set, get current scroll position.
    if (this.layoutManager != null) {
        scrollPosition = (this.layoutManager as LinearLayoutManager)
            .findFirstCompletelyVisibleItemPosition()
    }
    
    this.adapter = null
    this.layoutManager = null
    
    //    when (layoutManagerType) {
    //        GRID_LAYOUT_MANAGER -> {
    //            mLayoutManager = GridLayoutManager(getActivity(), SPAN_COUNT)
    //            mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER
    //        }
    //        LINEAR_LAYOUT_MANAGER -> {
    //            mLayoutManager = LinearLayoutManager(getActivity())
    //            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
    //        }
    //        else -> {
    //            mLayoutManager = LinearLayoutManager(getActivity())
    //            mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER
    //        }
    //    }
    
    this.layoutManager = layoutManager
    this.adapter = savedAdapter
    if (restoreScrollPosition)
        this.scrollToPosition(scrollPosition)
}

inline fun <reified T : Enum<T>> printAllValues() {
    print(enumValues<T>().joinToString { it.name })
}
// enum class RGB { RED, GREEN, BLUE }
// printAllValues<RGB>() // 输出 RED, GREEN, BLUE

