package com.obsez.android.lib.filechooser.demo

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.storage.StorageManager
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.obsez.android.lib.filechooser.MediaStorePicker
import com.obsez.android.lib.filechooser.MediaType
import com.obsez.android.lib.filechooser.demo.about.AboutActivity
import timber.log.Timber
import java.util.*

class ChooseFileActivity : AppCompatActivity() {
    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            //Timber.plant(new CrashReportingTree());
        }
    }
    
    @Suppress("DEPRECATION")
    private fun testPrint() {
        
        val c = this
        
        val storageManager = c.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        //StorageVolume svPrimary = storageManager.getPrimaryStorageVolume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (storageManager != null) {
                for (sv in storageManager.storageVolumes) {
                    Timber.d("    vol: state=%s, desc=%s, isEmulated=%b, isPrimary=%b, isRemovable=%b | %s",
                        sv.state, sv.getDescription(c), sv.isEmulated, sv.isPrimary, sv.isRemovable,
                        sv.toString())
                }
            }
        }
        
        Timber.d("Test dirs for Q: ------------------------------")
        Timber.v("  Environment.getDataDirectory : %s", Environment.getDataDirectory().absoluteFile)
        Timber.v("  Environment.getDownloadCacheDirectory : %s",
            Environment.getDownloadCacheDirectory().absoluteFile)
        Timber.v("  Environment.getExternalStorageDirectory : %s",
            Environment.getExternalStorageDirectory().absoluteFile)
        Timber.v("  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) : %s",
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absoluteFile)
        Timber.v("  Environment.getRootDirectory : %s", Environment.getRootDirectory().absoluteFile)
        Timber.v("  ------------------------------")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val volumes = MediaStore.getExternalVolumeNames(c)
            for (s in volumes) {
                Timber.d("    vol: %s", s)
            }
        }
        Timber.v("  ------------------------------")
        Timber.d("   getCacheDir : %s", c.cacheDir.absoluteFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("   getCodeCacheDir : %s", c.codeCacheDir.absoluteFile)
        }
        Timber.d("   getDatabasePath(abc) : %s", c.getDatabasePath("abc").absoluteFile)
        Timber.d("   getDatabasePath(v.db3) : %s", c.getDatabasePath("v.db3").absoluteFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Timber.d("   getDataDir : %s", c.dataDir.absoluteFile)
        }
        Timber.d("   getDir(null) : %s", c.getDir(null, Context.MODE_PRIVATE).absoluteFile)
        Timber.d("   getDir(zzz) : %s", c.getDir("zzz", Context.MODE_PRIVATE).absoluteFile)
        Timber.d("   getExternalCacheDir : %s", c.externalCacheDir!!.absoluteFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (f in c.externalCacheDirs) {
                Timber.d("   getExternalCacheDirs : %s", f.absoluteFile)
            }
        }
        Timber.d("   getExternalFilesDir : %s", c.getExternalFilesDir(null)!!.absoluteFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            for (f in c.getExternalFilesDirs(null)) {
                Timber.d("   getExternalFilesDirs : %s", f.absoluteFile)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (f in c.externalMediaDirs) {
                Timber.d("   getExternalMediaDirs : %s", f.absoluteFile)
            }
        }
        Timber.d("   getFilesDir : %s", c.filesDir.absoluteFile)
        Timber.d("   getFileStreamPath : %s", c.getFileStreamPath("").absoluteFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Timber.d("   getNoBackupFilesDir : %s", c.noBackupFilesDir.absoluteFile)
        }
        Timber.d("   getObbDir : %s", c.obbDir.absoluteFile)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            for (f in c.obbDirs) {
                Timber.d("   getObbDirs : %s", f.absoluteFile)
            }
        }
        Timber.d("   getPackageCodePath : %s", c.packageCodePath)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_file)
        
        // Do not let the launcher create a new activity http://stackoverflow.com/questions/16283079
        if (!isTaskRoot) {
            // Android launched another instance of the root activity into an existing task
            //  so just quietly finish and go away, dropping the user back into the activity
            //  at the top of the stack (ie: the last state of this task)
            finish()
            return
        }
    
        testPrint()
        
        val transaction = supportFragmentManager.beginTransaction()
        // transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
        transaction.add(R.id.fragment, ChooseFileActivityFragment())
            .commit()
        
        if (Log.isLoggable(TAG, Log.INFO)) {
            val resources = applicationContext.resources
            val dm = resources.displayMetrics
            val config = resources.configuration
            Timber.v("""dm: density=${dm.density}, densityDpi=${dm.densityDpi}, curr-locale=${Locale.getDefault()}""")
            
            // force English locale
            //config.locale = Locale.ENGLISH;
            //resources.updateConfiguration(config, dm);
            
            // use 正體中文
            //config.locale = Locale.TRADITIONAL_CHINESE;
            //resources.updateConfiguration(config, dm);
            
            // use system default
            config.locale = Locale.getDefault()
            resources.updateConfiguration(config, dm)
        }
    }
    
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_choose_file, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId
        
        
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        
        if (id == R.id.action_about) {
            startActivity(Intent(this, AboutActivity::class.java))
            return true
        }
        if (id == R.id.action_gh) {
            // startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/hedzr/android-file-chooser")))
    
            var isLargeLayout = resources.getBoolean(R.bool.large_layout)
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            isLargeLayout = true
            //}
    
            Timber.v("MediaType: ${MediaType.IMAGES}, ${MediaType.IMAGES.name}, ${MediaType.IMAGES.ordinal}, ${MediaType.IMAGES.getter}, ")
    
            MediaStorePicker.get().config(MediaType.VIDEOS, isLargeLayout, R.id.fragment).show()
            return true
        }
        
        return super.onOptionsItemSelected(item)
    }
    
    companion object {
        
        private const val TAG = "ChooseFileActivity"
    }
}
