package com.obsez.android.lib.filechooser.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Size
import com.obsez.android.lib.filechooser.R
import timber.log.Timber


interface MediaTypeGetter {
    fun getContentUri(external: Boolean = true, vararg args: Any): Uri
    fun getProjection(): Array<String>?
    fun getSelection(): String?
    fun getSelectionArgs(): Array<String>?
    fun getSortOrder(): String
    fun getThumbnail(context: Context, id: Long, uri: Uri, width: Int): Bitmap?
}

class ImagesMediaTypeGetter : MediaTypeGetter {
    override fun getContentUri(external: Boolean, vararg args: Any): Uri {
        return if (external) MediaStore.Images.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.INTERNAL_CONTENT_URI
    }
    
    override fun getProjection(): Array<String>? {
        return null
    }
    
    override fun getSelection(): String? {
        return null
    }
    
    override fun getSelectionArgs(): Array<String>? {
        return null
    }
    
    override fun getSortOrder(): String {
        return MediaStore.Images.Media.DATE_MODIFIED + " DESC"
    }
    
    override fun getThumbnail(context: Context, id: Long, uri: Uri, width: Int): Bitmap? {
        return this.getThumbnailImpl(context, id, uri, width).let {
            if (it != null) {
                Timber.v("thumbnail's width is: $width / ${it.width}")
                it
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.no_image)
            }
        }
    }
    
    private fun getThumbnailImpl(c: Context, id: Long, uri: Uri, width: Int = 96): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // val w = if (width <= 0) 128 else width
            try {
                c.contentResolver.loadThumbnail(uri, Size(512, 384), null)
            } catch (e: Exception) {
                Timber.e("CANNOT create or get thumbnail", e)
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
}

class VideosMediaTypeGetter : MediaTypeGetter {
    override fun getContentUri(external: Boolean, vararg args: Any): Uri {
        return if (external) MediaStore.Video.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.INTERNAL_CONTENT_URI
    }
    
    override fun getProjection(): Array<String>? {
        return null
    }
    
    override fun getSelection(): String? {
        return null
    }
    
    override fun getSelectionArgs(): Array<String>? {
        return null
    }
    
    override fun getSortOrder(): String {
        return MediaStore.Video.Media.DATE_MODIFIED + " DESC"
    }
    
    override fun getThumbnail(context: Context, id: Long, uri: Uri, width: Int): Bitmap? {
        return this.getThumbnailImpl(context, id, uri, width).let {
            if (it != null) {
                Timber.v("thumbnail's width is: $width / ${it.width}")
                it
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.no_image)
            }
        }
    }
    
    private fun getThumbnailImpl(c: Context, id: Long, uri: Uri, width: Int = 96): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // val w = if (width <= 0) 128 else width
            try {
                c.contentResolver.loadThumbnail(uri, Size(512, 384), null)
            } catch (e: Exception) {
                Timber.e("CANNOT create or get thumbnail", e)
                null
            }
        } else {
            val bmOptions = BitmapFactory.Options()
            bmOptions.inSampleSize = 2  // or 1
            Timber.v("MediaStore.Images.Thumbnails.getThumbnail for id=$id")
            MediaStore.Video.Thumbnails.getThumbnail(c.contentResolver,
                id,
                MediaStore.Images.Thumbnails.MINI_KIND,  // 512 x 384
                //MediaStore.Images.Thumbnails.MICRO_KIND, // 96 x 96
                bmOptions)
        }
    }
}

class AudiosMediaTypeGetter : MediaTypeGetter {
    override fun getContentUri(external: Boolean, vararg args: Any): Uri {
        return if (external) MediaStore.Audio.Media.EXTERNAL_CONTENT_URI else MediaStore.Images.Media.INTERNAL_CONTENT_URI
    }
    
    override fun getProjection(): Array<String>? {
        return null
    }
    
    override fun getSelection(): String? {
        // val selection = MediaStore.Video.Media._ID + " = $id"
        // val cursor = this.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, null, selection, null, null)
        return null
    }
    
    override fun getSelectionArgs(): Array<String>? {
        return null
    }
    
    override fun getSortOrder(): String {
        return MediaStore.Audio.Media.DATE_MODIFIED + " DESC"
    }
    
    override fun getThumbnail(context: Context, id: Long, uri: Uri, width: Int): Bitmap? {
        return this.getThumbnailImpl(context, id, uri, width).let {
            if (it != null) {
                Timber.v("thumbnail's width is: $width / ${it.width}")
                it
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.no_image)
            }
        }
    }
    
    private fun getThumbnailImpl(c: Context, id: Long, uri: Uri, width: Int = 96): Bitmap? {
        return null
    }
}

class DownloadsMediaTypeGetter : MediaTypeGetter {
    override fun getContentUri(external: Boolean, vararg args: Any): Uri {
        return if (external) if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            TODO("VERSION.SDK_INT < Q")
        } else MediaStore.Images.Media.INTERNAL_CONTENT_URI
    }
    
    override fun getProjection(): Array<String>? {
        return null
    }
    
    override fun getSelection(): String? {
        return null
    }
    
    override fun getSelectionArgs(): Array<String>? {
        return null
    }
    
    override fun getSortOrder(): String {
        return MediaStore.Downloads.DATE_MODIFIED + " DESC"
    }
    
    override fun getThumbnail(context: Context, id: Long, uri: Uri, width: Int): Bitmap? {
        return this.getThumbnailImpl(context, id, uri, width).let {
            if (it != null) {
                Timber.v("thumbnail's width is: $width / ${it.width}")
                it
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.no_image)
            }
        }
    }
    
    private fun getThumbnailImpl(c: Context, id: Long, uri: Uri, width: Int = 96): Bitmap? {
        return null
    }
}

class FilesMediaTypeGetter : MediaTypeGetter {
    override fun getContentUri(external: Boolean, vararg args: Any): Uri {
        return MediaStore.Files.getContentUri(args[0] as String)
    }
    
    override fun getProjection(): Array<String>? {
        return null
    }
    
    override fun getSelection(): String? {
        return null
    }
    
    override fun getSelectionArgs(): Array<String>? {
        return null
    }
    
    override fun getSortOrder(): String {
        return MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC"
    }
    
    override fun getThumbnail(context: Context, id: Long, uri: Uri, width: Int): Bitmap? {
        return this.getThumbnailImpl(context, id, uri, width).let {
            if (it != null) {
                Timber.v("thumbnail's width is: $width / ${it.width}")
                it
            } else {
                BitmapFactory.decodeResource(context.resources, R.drawable.no_image)
            }
        }
    }
    
    private fun getThumbnailImpl(c: Context, id: Long, uri: Uri, width: Int = 96): Bitmap? {
        return null
    }
}
