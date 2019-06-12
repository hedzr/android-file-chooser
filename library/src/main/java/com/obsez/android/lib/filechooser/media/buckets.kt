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
    
    /**
     * @param forceWidth unused below Android Q; for Q or later, true means that we want a width x height thumbnail instead of the internal size; default internal size is 512x384 (MediaStore.Images.Thumbnails.MINI_KIND)
     *
     * You might not get the thumbnail with exact width and height, even if you specify them explicitly.
     * The largest thumbnail size would be limited to about 526x602.
     * The largest width is about half an your screen, and height would be restricted with your source picture ratio.
     *
     * And:
     * - MediaStore.Images.Thumbnails.MINI_KIND,  // 512 x 384
     * - MediaStore.Images.Thumbnails.MICRO_KIND, // 96 x 96
     */
    fun getThumbnail(c: Context, mediaType: MediaType, width: Int = 96, height: Int = 0, forceWidth: Boolean = false): Bitmap? {
        if (_thumbnail == null || (forceWidth && width > 0)) {
            _thumbnail = mediaType.getter.getThumbnail(c, id, uri, width, height, forceWidth)
        }
        return _thumbnail
    }
}


