package com.obsez.android.lib.filechooser

import android.media.MediaScannerConnection
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.FragmentActivity
import com.obsez.android.lib.filechooser.demo.tool.ActivityProvider
import timber.log.Timber


class MediaStorePicker {
    
    companion object {
        fun get(): MediaStorePicker {
            return MediaStorePicker()
        }
    }
    
    /**
     * @param isLargeLayout true means a large screen detected, and picker will be shown as a popup dialog.
     * @param containerId the fragment container's res-id, default is [android.R.id.content].
     */
    fun config(@IdRes containerId: Int = android.R.id.content, isLargeLayout: Boolean = false): MediaStorePicker {
        mIsLargeLayout = isLargeLayout
        mContainerId = containerId
        return this
    }
    
    fun build(): MediaStorePicker {
        // TODO How to refresh MediaStore database exactly?
        MediaScannerConnection.scanFile(ActivityProvider.currentActivity!!
            , emptyArray()
            , arrayOf("image/*", "video/*", "audio/*")) { path, uri ->
            
            Timber.i("onScanCompleted : $path, $uri")
            
        }
        return this
    }
    
    
    fun show() {
        val a = ActivityProvider.currentActivity!!
        //Timber.d("application = $application, last activity = $a")
        if (a is FragmentActivity) {
            val fm = a.supportFragmentManager
            val pickerFragment = PickerDialogFragment()
            pickerFragment.arguments = Bundle().apply {
                putBoolean("largeLayout", mIsLargeLayout)
            }
            
            //Timber.d("mIsLargeLayout = $mIsLargeLayout")
            if (mIsLargeLayout) {
                // The device is using a large layout, so show the fragment as a dialog
                pickerFragment.show(fm, "picker-dialog")
            } else {
                // The device is smaller, so show the fragment fullscreen
                val transaction = fm.beginTransaction()
                
                // For a little polish, specify a transition animation
                // transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                
                // To make it fullscreen, use the 'content' root view as the container
                // for the fragment, which is always the root view for the activity
                transaction.replace(mContainerId, pickerFragment)
                    .addToBackStack("picker-dialog").commit()
            }
        }
    }
    
    private var mIsLargeLayout = true
    @IdRes
    private var mContainerId: Int = android.R.id.content
}

