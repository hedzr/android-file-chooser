package com.obsez.android.lib.filechooser.media

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.support.v4.content.AsyncTaskLoader
import android.support.v4.os.BuildCompat
import java.io.File

//import retrofit2.Retrofit
//import retrofit2.converter.gson.GsonConverterFactory

/**
 * AsyncTaskLoader implementation that opens a network connection and
 * query's the Book Service API.
 */
//class BookLoader// Constructor providing a reference to the search term.
//(context: Context, // Variable that stores the search string.
// private val mQueryString: String) : AsyncTaskLoader<Books>(context) {
//
//    private val BOOK_BASE_URL = "https://www.googleapis.com/books/"
//    private val TAG = BookLoader::class.java.simpleName
//
//    /**
//     * This method is invoked by the LoaderManager whenever the loader is started
//     */
//    override fun onStartLoading() {
//        forceLoad() // Starts the loadInBackground method
//    }
//
//    /**
//     * Connects to the network and makes the Books API request on a background thread.
//     *
//     * @return Returns the raw JSON response from the API call.
//     */
//    override fun loadInBackground(): Books? {
//        val call = apiService().getBookByName(mQueryString, "2", "books")
//        val response = call.execute()
//        return if (response.isSuccessful) {
//            val result = response.body()
//            Log.d(TAG, "result: " + result)
//            result
//        } else {
//            Log.d(TAG, "returning empty books: ")
//            Books()
//        }
//    }
//
//    companion object {
//        fun apiService() = Retrofit.Builder()
//            .baseUrl("https://www.googleapis.com/books/")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build().create(GoogleBooksApiService::class.java)!!
//    }
//}

/**
 * AsyncTaskLoader implementation that opens a network connection and
 * query's the Book Service API.
 */
class BucketLoader(context: Context,
                   private val mQueryString: String,
                   private val progressListener: ProgressListener? = null) : AsyncTaskLoader<Buckets>(context) {
    
    interface ProgressListener {
        fun onInit(max: Int)
        fun onStep(diff: Int, bucketId: Long, bucketName: String, item: BucketItem)
        fun onEnd()
    }
    
    private val buckets = Buckets("", ArrayList())
    
    /**
     * This method is invoked by the LoaderManager whenever the loader is started
     */
    override fun onStartLoading() {
        forceLoad() // Starts the loadInBackground method
    }
    
    /**
     * Connects to the network and makes the Books API request on a background thread.
     *
     * @return Returns the raw JSON response from the API call.
     */
    override fun loadInBackground(): Buckets? {
        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                //MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?",
                //arrayOf("image/jpeg", "image/png"),
                null, null,
                MediaStore.Images.Media.DATE_MODIFIED + " DESC")
            val mDirs = mutableMapOf<String, Long>()
            if (cursor != null) {
                var bid: Long = 1
                
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.TITLE)
                val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val descColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DESCRIPTION)
                val lastModiColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                val heightColumn = if (Build.VERSION.SDK_INT < 16) -1 else cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT)
                val widthColumn = if (Build.VERSION.SDK_INT < 16) -1 else cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH)
                val bucketNameColumn = if (Build.VERSION.SDK_INT < 29) -1 else cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME)
                val bucketIdColumn = if (Build.VERSION.SDK_INT < 29) -1 else cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.BUCKET_ID)
                val latColumn = if (BuildCompat.isAtLeastQ()) -1 else cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LATITUDE)
                val lngColumn = if (BuildCompat.isAtLeastQ()) -1 else cursor.getColumnIndexOrThrow(MediaStore.Images.Media.LONGITUDE)
                
                // TODO set to -1 while `BuildCompat.isAtLeastQ()` is true
                val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
    
                progressListener?.onInit(cursor.count)
                
                while (cursor.moveToNext()) {
                    val id = cursor.getString(idColumn)
                    val photoUri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                    val title = cursor.getString(titleColumn)
                    val size = safeString2(cursor, sizeColumn, "0")
                    val w = safeString2(cursor, widthColumn, "0")
                    val h = safeString2(cursor, heightColumn, "0")
                    val desc = safeString(cursor, descColumn)
                    val lastModified = safeString(cursor, lastModiColumn)
                    val path = cursor.getString(pathColumn)
                    
                    if (Build.VERSION.SDK_INT < 29) {
                        val parentFile = File(path).parentFile ?: continue
                        val bucketName = parentFile.name
                        var bucketId: Long
                        if (mDirs.contains(parentFile.absolutePath)) {
                            bucketId = mDirs[parentFile.absolutePath]!!
                        } else {
                            mDirs[parentFile.absolutePath] = bid
                            bucketId = bid++
                        }
                        
                        // Timber.d("- $photoUri, $title, $bucketId, $bucketName, ${w}x$h, $size, $path, ")
    
                        progressListener?.onStep(1, bucketId, bucketName,
                            BucketItem(title, id.toLong(), photoUri, path, desc,
                                size.toLong(), h.toLong(), w.toLong(), lastModified))
                    } else {
                        val bucketName = cursor.getString(bucketNameColumn)
                        val bucketId = cursor.getString(bucketIdColumn)
                        
                        // Timber.d("- $photoUri, $title, $bucketId, $bucketName, ${w}x$h, $size, $path, ")
    
                        progressListener?.onStep(1, bucketId.toLong(), bucketName,
                            BucketItem(title, id.toLong(), photoUri, path, desc,
                                size.toLong(), h.toLong(), w.toLong(), lastModified))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            progressListener?.onEnd()
        }
        
        return buckets
    }
    
    private fun safeString(cursor: Cursor?, idx: Int): String {
        return try {
            cursor?.getString(idx) ?: ""
        } catch (e: Exception) {
            ""
        }
    }
    
    private fun safeString2(cursor: Cursor?, idx: Int, def: String): String {
        return try {
            cursor?.getString(idx) ?: def
        } catch (e: Exception) {
            def
        }
    }
    
}

