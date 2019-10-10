@file:Suppress("unused")

package com.obsez.android.lib.filechooser.demo.demo

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.obsez.android.lib.filechooser.ChooserDialog
import com.obsez.android.lib.filechooser.demo.R
import java.io.File
import java.util.*

object Demo {
    fun demo1(context: Context, startPath: String, callback: ChooserDialog.Result) {
        ChooserDialog(context)
            .titleFollowsDir(true)
            .withIcon(R.mipmap.ic_launcher)
            .withFilterRegex(false, true, ".*\\.(jpe?g|png)")
            .withStartFile(startPath)
            .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
            .withChosenListener(callback)
            .withNavigateUpTo { true }
            .withNavigateTo { true }
            .build()
            .show()
    }
    
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    fun demo2(context: Context, startPath: String, callback: ((paths: ArrayList<String>) -> Unit)? = null) {
        val files = ArrayList<File>()
        val dlg = ChooserDialog(context)
        dlg.displayPath(true)
            .withFilter(false, true, "jpg", "jpeg", "png")
            .withStartFile(startPath)
            .enableOptions(true)
            .enableMultiple(true)
            .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
            .withOptionResources(R.string.option_create_folder, R.string.options_delete, R.string.new_folder_cancel, R.string.new_folder_ok)
            .withChosenListener { dir, dirFile ->
                if (dirFile.isDirectory) {
                    dlg.dismiss()
                } else if (!files.remove(dirFile)) {
                    files.add(dirFile)
                }
            }
            .withOnDismissListener {
                val paths = ArrayList<String>()
                if (files.isNotEmpty()) {
                    for (file in files) {
                        paths.add(file.absolutePath)
                    }
                }
                callback?.invoke(paths)
            }
            .withNavigateUpTo { true }
            .withNavigateTo { true }
            .withDateFormat("dd MMMM yyyy")
            .build()
            .show()
    }
}
