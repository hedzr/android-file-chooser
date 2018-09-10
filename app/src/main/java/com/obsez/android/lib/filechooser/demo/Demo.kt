package com.obsez.android.lib.filechooser.demo

import android.content.Context
import android.widget.Toast
import com.obsez.android.lib.filechooser.ChooserDialog

object Demo {
    
    
    fun demo1(context: Context, startPath: String, callback: FileChooserDialog.Result) {
        FileChooserDialog.newDialog(context)
                .withFilterRegex(false, true, ".*\\.(jpe?g|png)")
                .withStartFile(startPath)
                .withResources(R.string.title_choose_file, R.string.title_choose, R.string.dialog_cancel)
                .withChosenListener(callback)
                .withNavigateUpTo { true }
                .withNavigateTo { true }
                .build()
                .show()
        
    }
}
