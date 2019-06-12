package com.obsez.android.lib.filechooser.demo

import android.content.Intent
import android.os.Bundle
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
