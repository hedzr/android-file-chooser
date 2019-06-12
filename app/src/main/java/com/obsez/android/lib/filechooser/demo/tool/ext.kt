@file:Suppress("DEPRECATION")

package com.obsez.android.lib.filechooser.demo.tool

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo


@Suppress("MemberVisibilityCanBePrivate", "unused", "DEPRECATION")
inline val Activity.networkInfo: NetworkInfo?
    get() {
        // Check the status of the network connection.
        val connMgr = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connMgr.activeNetworkInfo
    }
