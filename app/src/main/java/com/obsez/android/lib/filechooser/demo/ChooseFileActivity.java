package com.obsez.android.lib.filechooser.demo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

import timber.log.Timber;

public class ChooseFileActivity extends AppCompatActivity {

    private static final String TAG = "ChooseFileActivity";

    public ChooseFileActivity() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            //Timber.plant(new CrashReportingTree());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadNightMode();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_file);

        if (Log.isLoggable(TAG, Log.INFO)) {
            Resources resources = getApplicationContext().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            Configuration config = resources.getConfiguration();
            Timber.v("dm: density=" + dm.density + ", densityDpi=" + dm.densityDpi + ", curr-locale="
                + Locale.getDefault());

            // force English locale
            //config.locale = Locale.ENGLISH;
            //resources.updateConfiguration(config, dm);

            // use 正體中文
            //config.locale = Locale.TRADITIONAL_CHINESE;
            //resources.updateConfiguration(config, dm);

            // use system default
            config.locale = Locale.getDefault();
            resources.updateConfiguration(config, dm);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    void loadNightMode() {
        int nm = getNightMode();
        Timber.v("loading night mode: " + nm);
        //if (nm != AppCompatDelegate.getDefaultNightMode()) {
        setNightMode(nm, false);
        //} else {
        //    setNightMode(AppCompatDelegate.MODE_NIGHT_NO, false); // using day mode in default
        //}
    }

    //protected open fun loadNightMode() {
    //    val nm = nightMode
    //    if (nm != AppCompatDelegate.getDefaultNightMode()) {
    //        setNightMode(nm)
    //    }
    //}


    @AppCompatDelegate.NightMode
    int getNightMode() {
        SharedPreferences pref = getSharedPreferences("default", Context.MODE_PRIVATE);
        return pref.getInt("nightMode", AppCompatDelegate.getDefaultNightMode());
    }

    void setNightMode(@AppCompatDelegate.NightMode int nm) {
        Timber.v("force setting night mode: " + nm);
        setNightMode(nm, true);
    }

    @SuppressLint("ObsoleteSdkInt")
    void setNightMode(@AppCompatDelegate.NightMode int nm, boolean needRecreate) {
        SharedPreferences pref = getSharedPreferences("default", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("nightMode", nm);
        editor.commit();
        AppCompatDelegate.setDefaultNightMode(nm);
        if (Build.VERSION.SDK_INT >= 11 && needRecreate) {
            recreate();
        }
    }


}
