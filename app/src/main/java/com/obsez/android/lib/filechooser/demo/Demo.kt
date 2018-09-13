package com.obsez.android.lib.smbfilechooser.demo

import android.content.Context
import com.obsez.android.lib.smbfilechooser.FileChooserDialog

object Demo {


    fun demo1(context: Context, startPath: String, callback: FileChooserDialog.OnChosenListener) {
        FileChooserDialog.newDialog(context)
                .setFilterRegex(false, true, ".*\\.(jpe?g|png)")
                .setStartFile(startPath)
                .setResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .setOnChosenListener(callback)
                .setNavigateUpTo { true }
                .setNavigateTo { true }
                .build()
                .show()

    }
}