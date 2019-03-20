package com.obsez.android.lib.filechooser.demo.about

import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity;
import com.obsez.android.lib.filechooser.demo.R

import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        setSupportActionBar(toolbar)
        
        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
}
