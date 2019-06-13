package com.obsez.android.lib.filechooser

import android.os.Build
import android.support.annotation.RequiresApi
import com.obsez.android.lib.filechooser.media.*

enum class MediaType(val getter: MediaTypeGetter) {
    IMAGES(ImagesMediaTypeGetter()),
    VIDEOS(VideosMediaTypeGetter()),
    AUDIOS(AudiosMediaTypeGetter()),
    @RequiresApi(Build.VERSION_CODES.Q)
    DOWNLOADS(DownloadsMediaTypeGetter()),
    FILES(FilesMediaTypeGetter()),
}


