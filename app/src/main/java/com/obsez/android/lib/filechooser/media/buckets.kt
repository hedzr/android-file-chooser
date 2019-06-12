package com.obsez.android.lib.filechooser.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import timber.log.Timber


interface BucketBase {
    var title: String
    var id: Long
}

data class Buckets(var title: String, var items: ArrayList<Bucket>)

data class Bucket(override var title: String, override var id: Long, var items: ArrayList<BucketItem>) : BucketBase

@Suppress("unused")
data class BucketItem(override var title: String, override var id: Long,
                      var uri: Uri,
                      var path: String,
                      var desc: String,
                      var size: Long, var height: Long, var width: Long,
                      var lastModified: String) : BucketBase {
    
    private var _thumbnail: Bitmap? = null
    
    fun getThumbnail(c: Context, width: Int = 96): Bitmap? {
        if (_thumbnail == null) {
            _thumbnail = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // val w = if (width <= 0) 128 else width
                try {
                    c.contentResolver.loadThumbnail(uri, Size(512, 384), null)
                } catch (e: Exception) {
                    null
                }
            } else {
                val bmOptions = BitmapFactory.Options()
                bmOptions.inSampleSize = 2
                Timber.v("MediaStore.Images.Thumbnails.getThumbnail for id=$id")
                MediaStore.Images.Thumbnails.getThumbnail(c.contentResolver,
                    id,
                    MediaStore.Images.Thumbnails.MINI_KIND,  // 512 x 384
                    //MediaStore.Images.Thumbnails.MICRO_KIND, // 96 x 96
                    bmOptions)
            }
        }
        return _thumbnail
    }
}


