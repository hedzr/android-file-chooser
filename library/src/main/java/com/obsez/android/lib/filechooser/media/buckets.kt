package com.obsez.android.lib.filechooser.media

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import com.obsez.android.lib.filechooser.MediaType


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
    
    fun getThumbnail(c: Context, mediaType: MediaType, width: Int = 96): Bitmap? {
        if (_thumbnail == null) {
            _thumbnail = mediaType.getter.getThumbnail(c, id, uri, width)
        }
        return _thumbnail
    }
}


