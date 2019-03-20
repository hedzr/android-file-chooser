package com.obsez.android.lib.filechooser.demo.about

import android.support.v4.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.obsez.android.lib.filechooser.demo.R

/**
 * A placeholder fragment containing a simple view.
 */
class AboutActivityFragment : Fragment() {
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_about, container, false)
    }
}
