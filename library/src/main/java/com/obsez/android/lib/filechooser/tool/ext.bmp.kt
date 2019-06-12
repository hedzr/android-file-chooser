package com.obsez.android.lib.filechooser.tool

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.ImageView


inline fun ImageView.setBitmap(bitmap: Bitmap?) {
    val bd = this.drawable as BitmapDrawable
    bd.bitmap?.recycle()
    this.setImageBitmap(bitmap)
}



