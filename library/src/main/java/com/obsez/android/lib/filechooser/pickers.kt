package com.obsez.android.lib.filechooser

import android.media.MediaScannerConnection
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.FragmentActivity
import android.util.Log
import com.obsez.android.lib.filechooser.fragments.OnPickHandler
import com.obsez.android.lib.filechooser.fragments.PickerDialogFragment
import com.obsez.android.lib.filechooser.fragments.PickerDialogFragment.Companion.argDialogMode
import com.obsez.android.lib.filechooser.fragments.PickerDialogFragment.Companion.argMediaType
import com.obsez.android.lib.filechooser.provider.ActivityProvider


class MediaStorePicker {
    
    companion object {
        fun get(): MediaStorePicker {
            return MediaStorePicker()
        }
    
        private const val TAG = "MediaStorePicker"
        private const val FRAG_TAG = "picker-dialog"
    }
    
    
    /**
     * @param mediaType see also [MediaType]
     * @param dialogMode true means a large screen detected, and picker will be shown as a popup dialog.
     * @param containerId the fragment container's res-id, default is [android.R.id.content].
     */
    fun config(mediaType: MediaType = MediaType.IMAGES,
               dialogMode: Boolean = false,
               @IdRes containerId: Int = android.R.id.content,
               onPickedHandler: OnPickHandler? = null): MediaStorePicker {
        mMediaType = mediaType
        mDialogMode = dialogMode
        mContainerId = containerId
        mOnPickedHandler = onPickedHandler
        return this
    }
    
    
    private fun build(): MediaStorePicker {
        // TODO How to refresh MediaStore database exactly?
        MediaScannerConnection.scanFile(ActivityProvider.currentActivity!!
            , emptyArray()
            , arrayOf("image/*", "video/*", "audio/*")) { path, uri ->
    
            Log.i(TAG, "onScanCompleted : $path, $uri")
            
        }
        return this
    }
    
    
    fun show() {
        build()
        
        val a = ActivityProvider.currentActivity!!
        //Timber.d("application = $application, last activity = $a")
        if (a is FragmentActivity) {
            val fm = a.supportFragmentManager
            val pickerFragment = PickerDialogFragment().apply {
                onPickedHandler = mOnPickedHandler
            }
            pickerFragment.arguments = Bundle().apply {
                putBoolean(argDialogMode, mDialogMode)
                putInt(argMediaType, mMediaType.ordinal)
            }
    
            //Timber.d("mDialogMode = $mDialogMode")
            if (mDialogMode) {
                // The device is using a large layout, so show the fragment as a dialog
                pickerFragment.show(fm, FRAG_TAG)
            } else {
                // The device is smaller, so show the fragment fullscreen
                val transaction = fm.beginTransaction()
                
                // For a little polish, specify a transition animation
                // transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                
                // To make it fullscreen, use the 'content' root view as the container
                // for the fragment, which is always the root view for the activity
                transaction.replace(mContainerId, pickerFragment)
                    .addToBackStack(FRAG_TAG).commit()
            }
        }
    }
    
    
    private var mMediaType: MediaType = MediaType.IMAGES
    private var mDialogMode = true
    @IdRes
    private var mContainerId: Int = android.R.id.content
    private var mOnPickedHandler: OnPickHandler? = null
    
}

