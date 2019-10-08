package com.obsez.android.lib.filechooser.demo.tool

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.pm.ApplicationInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Environment.MEDIA_MOUNTED
import android.provider.MediaStore
import android.provider.MediaStore.Video
import com.obsez.android.lib.filechooser.internals.UiUtil
import com.obsez.android.lib.filechooser.internals.WrappedDrawable
import java.io.File
import java.util.*


/**
 */
class FileManager {
    
    /**
     * 获取本机音乐列表
     * @return
     */
    // 路径
    // 歌曲名
    // 专辑
    // 作者
    // 大小
    // 时长
    // 歌曲的id
    // int albumId = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
    val musics: List<Music>
        get() {
            val musics = ArrayList<Music>()
            var c: Cursor? = null
            try {
                c = mContentResolver!!.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
                    MediaStore.Audio.Media.DEFAULT_SORT_ORDER)
                
                while (c!!.moveToNext()) {
                    val path = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    
                    if (!File(path).exists()) {
                        continue
                    }
                    
                    val name = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val album = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                    val artist = c.getString(c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                    val size = c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE))
                    val duration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        c.getLong(c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    } else {
                        0
                    }
                    val time = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    
                    val music = Music(name, path, album, artist, size, duration)
                    musics.add(music)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                c?.close()
            }
            return musics
        }
    
    /**
     * 获取本机视频列表
     * @return
     */
    // String[] mediaColumns = { "_id", "_data", "_display_name",
    // "_size", "date_modified", "duration", "resolution" };
    // 路径
    // 视频的id
    // 视频名称
    //分辨率
    // 大小
    // 时长
    //修改时间
    val videos: Array<com.obsez.android.lib.filechooser.demo.tool.Video>
        get() {
            
            val videos = mutableListOf<com.obsez.android.lib.filechooser.demo.tool.Video>()
            
            var c: Cursor? = null
            try {
                c = mContentResolver!!.query(Video.Media.EXTERNAL_CONTENT_URI, null, null, null, Video.Media.DEFAULT_SORT_ORDER)
                while (c!!.moveToNext()) {
                    val path = c.getString(c.getColumnIndexOrThrow(Video.Media.DATA))
                    if (!File(path).exists()) {
                        continue
                    }
                    
                    val id = c.getInt(c.getColumnIndexOrThrow(Video.Media._ID))
                    val name = c.getString(c.getColumnIndexOrThrow(Video.Media.DISPLAY_NAME))
                    val resolution = c.getString(c.getColumnIndexOrThrow(Video.Media.RESOLUTION))
                    val size = c.getLong(c.getColumnIndexOrThrow(Video.Media.SIZE))
                    val duration = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        c.getLong(c.getColumnIndexOrThrow(Video.Media.DURATION))
                    } else {
                        // TO DO("VERSION.SDK_INT < Q")
                        0
                    }
                    val date = c.getLong(c.getColumnIndexOrThrow(Video.Media.DATE_MODIFIED))
                    
                    val video = Video(id, path, name, resolution, size, date, duration)
                    videos.add(video)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                c?.close()
            }
            return videos.toTypedArray()
        }
    
    /**
     * 得到图片文件夹集合
     */
    // 扫描图片
    //用于保存已经添加过的文件夹目录
    // 路径
    //如果已经添加过
    //添加到保存目录的集合中
    val imageFolders: List<ImgFolderBean>
        get() {
            val folders = ArrayList<ImgFolderBean>()
            var c: Cursor? = null
            try {
                c = mContentResolver!!.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    MediaStore.Images.Media.MIME_TYPE + "= ? or " + MediaStore.Images.Media.MIME_TYPE + "= ?",
                    arrayOf("image/jpeg", "image/png"), MediaStore.Images.Media.DATE_MODIFIED)
                val mDirs = ArrayList<String>()
                while (c!!.moveToNext()) {
                    val path = c.getString(c.getColumnIndex(MediaStore.Images.Media.DATA))
                    val parentFile = File(path).parentFile ?: continue
                    
                    val dir = parentFile.absolutePath
                    if (mDirs.contains(dir))
                        continue
                    
                    mDirs.add(dir)
                    
                    val count = parentFile.list { _, filename ->
                        filename.endsWith(".jpeg") || filename.endsWith(".jpg") || filename.endsWith(".png")
                    }!!.size
                    val folderBean = ImgFolderBean(dir, path, path, count)
                    folders.add(folderBean)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                c?.close()
            }
            
            return folders
        }
    
    /**
     * 获取已安装apk的列表
     */
    //获取到包的管理者
    //获得所有的安装包
    //遍历每个安装包，获取对应的信息
    //得到icon
    //得到程序的名字
    //得到程序的包名
    //得到程序的资源文件夹
    //得到apk的大小
    //获取到安装应用程序的标记
    //表示系统app
    //表示用户app
    //表示在sd卡
    //表示内存
    val appInfos: List<AppInfo>
        get() {
            
            val appInfos = ArrayList<AppInfo>()
            val packageManager = mContext!!.packageManager
            val installedPackages = packageManager.getInstalledPackages(0)
            for (packageInfo in installedPackages) {
                
                val appInfo = AppInfo()
                
                appInfo.applicationInfo = packageInfo.applicationInfo
                appInfo.versionCode = packageInfo.versionCode
                val drawable = packageInfo.applicationInfo.loadIcon(packageManager)
                appInfo.icon = drawable
                val apkName = packageInfo.applicationInfo.loadLabel(packageManager).toString()
                appInfo.apkName = apkName
                val packageName = packageInfo.packageName
                appInfo.apkPackageName = packageName
                val sourceDir = packageInfo.applicationInfo.sourceDir
                val file = File(sourceDir)
                val size = file.length()
                appInfo.apkSize = size
                
                println("---------------------------")
                println("程序的名字:$apkName, pkg = $packageName, size = $size")
                val flags = packageInfo.applicationInfo.flags
                
                appInfo.isUserApp = flags and ApplicationInfo.FLAG_SYSTEM == 0
                appInfo.isRom = flags and ApplicationInfo.FLAG_EXTERNAL_STORAGE == 0
                
                appInfos.add(appInfo)
            }
            return appInfos
        }
    
    // 获取视频缩略图
    fun getVideoThumbnail(id: Int): Bitmap? {
        val bitmap: Bitmap?
        val options = BitmapFactory.Options()
        options.inDither = false
        options.inPreferredConfig = Bitmap.Config.ARGB_8888
        bitmap = Video.Thumbnails.getThumbnail(mContentResolver, id.toLong(), MediaStore.Images.Thumbnails.MICRO_KIND, options)
        return bitmap
    }
    
    /**
     * 通过文件类型得到相应文件的集合
     */
    fun getFilesByType(fileType: Int): List<FileBean> {
        val files = ArrayList<FileBean>()
        // 扫描files文件库
        var c: Cursor? = null
        try {
            c = mContentResolver!!.query(MediaStore.Files.getContentUri("external"), arrayOf("_id", "_data", "_size"), null, null, null)
            val dataIndex = c!!.getColumnIndex(MediaStore.Files.FileColumns.DATA)
            val sizeIndex = c.getColumnIndex(MediaStore.Files.FileColumns.SIZE)
            
            while (c.moveToNext()) {
                val path = c.getString(dataIndex)
                
                if (FileTool.getFileType(path) == fileType) {
                    if (!FileTool.isExists(path)) {
                        continue
                    }
                    val size = c.getLong(sizeIndex)
                    var d = UiUtil.resolveFileTypeIcon(mContext!!, Uri.fromFile(File(path)))
                    if (d != null) {
                        d = WrappedDrawable(d, 24f, 24f)
                    }
                    val fileBean = FileBean(path, d)
                    files.add(fileBean)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            c?.close()
        }
        return files
    }
    
    /**
     * 通过图片文件夹的路径获取该目录下的图片
     */
    fun getImgListByDir(dir: String): List<String> {
        val imgPaths = ArrayList<String>()
        val directory = File(dir)
        if (!directory.exists()) {
            return imgPaths
        }
        val files = directory.listFiles()
        for (file in files!!) {
            val path = file.absolutePath
            if (FileTool.isImageFile(path)) {
                imgPaths.add(path)
            }
        }
        return imgPaths
    }
    
    companion object {
        
        private var mInstance: FileManager? = null
        private var mContext: Context? = null
        private var mContentResolver: ContentResolver? = null
        private val mLock = Any()
        
        fun getInstance(context: Context): FileManager {
            if (mInstance == null) {
                synchronized(mLock) {
                    if (mInstance == null) {
                        mInstance = FileManager()
                        mContext = context
                        mContentResolver = context.contentResolver
                    }
                }
            }
            return mInstance!!
        }
    }
    
}


class AppInfo {
    
    var applicationInfo: ApplicationInfo? = null
    var versionCode = 0
    /**
     * 图片的icon
     */
    var icon: Drawable? = null
    
    /**
     * 程序的名字
     */
    var apkName: String? = null
    
    /**
     * 程序大小
     */
    var apkSize: Long = 0
    
    /**
     * 表示到底是用户app还是系统app
     * 如果表示为true 就是用户app
     * 如果是false表示系统app
     */
    var isUserApp: Boolean = false
    
    /**
     * 放置的位置
     */
    var isRom: Boolean = false
    
    /**
     * 包名
     */
    var apkPackageName: String? = null
}

data class FileBean(
    var path: String,
    var iconDrawable: Drawable)

data class FileItem(var filePic: Int, var fileName: String, var filePath: String, var fileModifiedTime: String)

data class ImgFolderBean(val dir_: String, var fistImgPath: String, var name: String, var count: Int) {
    //    /**当前文件夹的路径 */
    //    private var dir: String? = null
    //    /**第一张图片的路径 */
    //    var fistImgPath: String? = null
    //    /**文件夹名 */
    //    var name: String? = null
    //        private set
    //    /**文件夹中图片的数量 */
    //    var count: Int = 0
    //
    //    fun getDir(): String? {
    //        return dir
    //    }
    
    var dir: String
        get() = _dir
        set(value) {
            this._dir = value
            val lastIndex = _dir.lastIndexOf("/")
            this.name = _dir.substring(lastIndex + 1)
        }
    private var _dir: String = dir_
    
}

data class Music(
    /**歌曲名 */
    var name: String?,
    /**路径 */
    var path: String?,
    /**所属专辑 */
    var album: String?,
    /**艺术家(作者) */
    var artist: String?,
    /**文件大小 */
    var size: Long,
    /**时长 */
    var duration: Long) : Comparable<Music> {
    
    /**
     * Compares this object with the specified object for order. Returns zero if this object is equal
     * to the specified [other] object, a negative number if it's less than [other], or a positive number
     * if it's greater than [other].
     */
    override fun compareTo(other: Music): Int {
        return other.path?.let { path?.compareTo(it) ?: -1 } ?: -1
    }
    
    //    var pinyin: String? = null
    //
    //    init {
    //        pinyin = PinyinUtils.getPinyin(name)
    //    }
    //
    //    override fun compareTo(music: Music): Int {
    //        return this.pinyin!!.compareTo(music.pinyin!!)
    //    }
    //
    //    override fun toString(): String {
    //        return "Music{" +
    //            "name='" + name + '\''.toString() +
    //            ", path='" + path + '\''.toString() +
    //            ", album='" + album + '\''.toString() +
    //            ", artist='" + artist + '\''.toString() +
    //            ", size=" + size +
    //            ", duration=" + duration +
    //            ", pinyin='" + pinyin + '\''.toString() +
    //            '}'.toString()
    //    }
}

data class Video(
    var id: Int = 0,
    var path: String? = null,
    var name: String? = null,
    var resolution: String? = null, // 分辨率
    var size: Long = 0,
    var date: Long = 0,
    var duration: Long = 0

)


@Suppress("MemberVisibilityCanBePrivate")
object FileTool {
    
    /**文档类型 */
    const val TYPE_DOC = 0
    /**apk类型 */
    const val TYPE_APK = 1
    /**压缩包类型 */
    const val TYPE_ZIP = 2
    
    
    /** 判断SD卡是否挂载  */
    val isSDCardAvailable: Boolean
        get() = MEDIA_MOUNTED == Environment.getExternalStorageState()
    
    
    /**
     * 判断文件是否存在
     * @param path 文件的路径
     * @return
     */
    fun isExists(path: String): Boolean {
        val file = File(path)
        return file.exists()
    }
    
    fun getFileType(path_: String): Int {
        return when (getExtFromFilename(path_).toLowerCase()) {
            ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx" -> TYPE_DOC
            ".apk" -> TYPE_APK
            ".zip", ".rar", ".tar", ".gz" -> TYPE_ZIP
            else -> -1
        }
    }
    
    
    //    /**通过文件名获取文件图标 */
    //    fun getFileIconByPath(path: String): Int {
    //        var path = path
    //        path = path.toLowerCase()
    //        var iconId = R.mipmap.unknow_file_icon
    //        if (path.endsWith(".txt")) {
    //            iconId = R.mipmap.type_txt
    //        } else if (path.endsWith(".doc") || path.endsWith(".docx")) {
    //            iconId = R.mipmap.type_doc
    //        } else if (path.endsWith(".xls") || path.endsWith(".xlsx")) {
    //            iconId = R.mipmap.type_xls
    //        } else if (path.endsWith(".ppt") || path.endsWith(".pptx")) {
    //            iconId = R.mipmap.type_ppt
    //        } else if (path.endsWith(".xml")) {
    //            iconId = R.mipmap.type_xml
    //        } else if (path.endsWith(".htm") || path.endsWith(".html")) {
    //            iconId = R.mipmap.type_html
    //        }
    //        return iconId
    //    }
    
    /**是否是图片文件 */
    fun isImageFile(path_: String): Boolean {
        return when (getExtFromFilename(path_).toLowerCase()) {
            ".jpg", ".jpeg", ".png" -> true
            else -> false
        }
    }
    
    /**
     * 从文件的全名得到文件的拓展名
     *
     * @param filename
     * @return
     */
    fun getExtFromFilename(filename: String): String {
        val dotPosition = filename.lastIndexOf('.')
        return if (dotPosition != -1) {
            filename.substring(dotPosition + 1, filename.length)
        } else ""
    }
    
    /**
     * 读取文件的修改时间
     *
     * @param f
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun getModifiedTime(f: File): String {
        val cal = Calendar.getInstance()
        val time = f.lastModified()
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        cal.timeInMillis = time
        // System.out.println("修改时间[2] " + formatter.format(cal.getTime()));
        // 输出：修改时间[2] 2009-08-17 10:32:38
        return formatter.format(cal.time)
    }
    
    
}


