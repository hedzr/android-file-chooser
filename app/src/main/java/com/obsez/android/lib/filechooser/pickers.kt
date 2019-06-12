package com.obsez.android.lib.filechooser

import android.media.MediaScannerConnection
import android.os.Bundle
import android.support.annotation.IdRes
import android.support.v4.app.FragmentActivity
import com.obsez.android.lib.filechooser.PickerDialogFragment.Companion.argDialogMode
import com.obsez.android.lib.filechooser.PickerDialogFragment.Companion.argMediaType
import com.obsez.android.lib.filechooser.demo.tool.ActivityProvider
import com.obsez.android.lib.filechooser.media.*
import timber.log.Timber

enum class MediaType(val getter: MediaTypeGetter) {
    IMAGES(ImagesMediaTypeGetter()), //(MediaStore.Images.Media.EXTERNAL_CONTENT_URI),
    VIDEOS(VideosMediaTypeGetter()), //(MediaStore.Video.Media.EXTERNAL_CONTENT_URI),
    AUDIOS(AudiosMediaTypeGetter()), //(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI),
    DOWNLOADS(DownloadsMediaTypeGetter()), //(MediaStore.Downloads.EXTERNAL_CONTENT_URI),
    FILES(FilesMediaTypeGetter()), //(MediaStore.Files.EXTERNAL_CONTENT_URI), // MediaStore.Files.getContentUri(volName)
}

class MediaStorePicker {
    
    companion object {
        fun get(): MediaStorePicker {
            return MediaStorePicker()
        }
    
    }
    
    /**
     * @param dialogMode true means a large screen detected, and picker will be shown as a popup dialog.
     * @param containerId the fragment container's res-id, default is [android.R.id.content].
     */
    fun config(mediaType: MediaType = MediaType.IMAGES, dialogMode: Boolean = false, @IdRes containerId: Int = android.R.id.content): MediaStorePicker {
        mMediaType = mediaType
        mDialogMode = dialogMode
        mContainerId = containerId
        return this
    }
    
    private fun build(): MediaStorePicker {
        // TODO How to refresh MediaStore database exactly?
        MediaScannerConnection.scanFile(ActivityProvider.currentActivity!!
            , emptyArray()
            , arrayOf("image/*", "video/*", "audio/*")) { path, uri ->
            
            Timber.i("onScanCompleted : $path, $uri")
            
        }
        return this
    }
    
    
    fun show() {
        build()
        
        val a = ActivityProvider.currentActivity!!
        //Timber.d("application = $application, last activity = $a")
        if (a is FragmentActivity) {
            val fm = a.supportFragmentManager
            val pickerFragment = PickerDialogFragment()
            pickerFragment.arguments = Bundle().apply {
                putBoolean(argDialogMode, mDialogMode)
                putInt(argMediaType, mMediaType.ordinal)
            }
    
            //Timber.d("mDialogMode = $mDialogMode")
            if (mDialogMode) {
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
    
    
    private var mMediaType: MediaType = MediaType.IMAGES
    private var mDialogMode = true
    @IdRes
    private var mContainerId: Int = android.R.id.content
}

