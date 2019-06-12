package com.obsez.android.lib.filechooser.provider

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedQueue


abstract class ProviderInitializer : ContentProvider() {
    
    override fun onCreate(): Boolean {
        val listener = initialize()
        ApplicationProvider.listen(listener)
        return true
    }
    
    abstract fun initialize(): (Application) -> Unit
    
    override fun insert(uri: Uri, values: ContentValues?): Uri {
        throw Exception("unimplemented")
    }
    
    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        throw Exception("unimplemented")
    }
    
    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        throw Exception("unimplemented")
    }
    
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        throw Exception("unimplemented")
    }
    
    override fun getType(uri: Uri): String {
        throw Exception("unimplemented")
    }
}


abstract class EmptyProvider : ContentProvider() {
    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    override fun query(p0: Uri, p1: Array<out String>?, p2: String?, p3: Array<out String>?, p4: String?): Cursor? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    override fun delete(p0: Uri, p1: String?, p2: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
    override fun getType(p0: Uri): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}


@Suppress("unused")
object ApplicationProvider {
    internal val applicationListeners = ConcurrentLinkedQueue<(Application) -> Unit>()
    
    @JvmStatic
    fun listen(listener: (Application) -> Unit) {
        val app = _application
        if (app != null) {
            listener(app)
        } else {
            applicationListeners.add(listener)
        }
    }
    
    @JvmStatic
    val application: Application?
        get() {
            return _application
        }
}

@Suppress("ObjectPropertyName")
@SuppressLint("StaticFieldLeak")
private var _application: Application? = null
    private set(value) {
        field = value
        if (value != null) {
            ApplicationProvider.applicationListeners.forEach {
                it.invoke(value)
            }
        }
    }

@Suppress("unused")
val application: Application?
    get() = _application
        ?: initAndGetAppCtxWithReflection()

/**
 * This methods is only run if [appCtx] is accessed while [AppCtxInitProvider] hasn't been
 * initialized. This may happen in case you're accessing it outside the default process, or in case
 * you are accessing it in a [ContentProvider] with a higher priority than [AppCtxInitProvider]
 * (900 at the time of writing this doc).
 *
 * //from https://github.com/LouisCAD/Splitties/tree/master/appctx
 */
@SuppressLint("PrivateApi")
private fun initAndGetAppCtxWithReflection(): Application? {
    // Fallback, should only run once per non default process.
    val activityThread = Class.forName("android.app.ActivityThread")
    val ctx = activityThread.getDeclaredMethod("currentApplication").invoke(null) as? Context
    if (ctx is Application) {
        _application = ctx
        return ctx
    }
    return null
}

class AppContextProvider : EmptyProvider() {
    override fun onCreate(): Boolean {
        val ctx = context
        if (ctx is Application) {
            _application = ctx
        }
        return true
    }
}


interface ActivityCreatedListener {
    fun onActivityCreated(activity: Activity)
}

interface ActivityResumedListener {
    fun onActivityResumed(activity: Activity)
}

interface ActivityPausedListener {
    fun onActivityPaused(activity: Activity)
}

interface ActivityDestroyedListener {
    fun onActivityDestroyed(activity: Activity)
}

@Suppress("ObjectPropertyName", "MemberVisibilityCanBePrivate", "unused")
object ActivityProvider {
    private val activityCreatedListeners = ConcurrentLinkedQueue<ActivityCreatedListener>()
    private val activityResumedListeners = ConcurrentLinkedQueue<ActivityResumedListener>()
    private val activityPausedListeners = ConcurrentLinkedQueue<ActivityPausedListener>()
    private val activityDestroyedListeners = ConcurrentLinkedQueue<ActivityDestroyedListener>()
    
    @JvmStatic
    fun addListen(listener: ActivityCreatedListener) {
        activityCreatedListeners.add(listener)
    }
    
    @JvmStatic
    fun removeListener(listener: ActivityCreatedListener) {
        activityCreatedListeners.remove(listener)
    }
    
    @JvmStatic
    fun addListen(listener: ActivityResumedListener) {
        activityResumedListeners.add(listener)
    }
    
    @JvmStatic
    fun removeListener(listener: ActivityResumedListener) {
        activityResumedListeners.remove(listener)
    }
    
    @JvmStatic
    fun addListen(listener: ActivityPausedListener) {
        activityPausedListeners.add(listener)
    }
    
    @JvmStatic
    fun removeListener(listener: ActivityPausedListener) {
        activityPausedListeners.remove(listener)
    }
    
    @JvmStatic
    fun addListen(listener: ActivityDestroyedListener) {
        activityDestroyedListeners.add(listener)
    }
    
    @JvmStatic
    fun removeListener(listener: ActivityDestroyedListener) {
        activityDestroyedListeners.remove(listener)
    }
    
    internal fun pingResumedListeners(activity: Activity) {
        _currentActivity = WeakReference(activity)
        activityResumedListeners.forEach {
            it.onActivityResumed(activity)
        }
    }
    
    internal fun pingPausedListeners(activity: Activity) {
        activityPausedListeners.forEach {
            it.onActivityPaused(activity)
        }
    }
    
    internal fun pingCreatedListeners(activity: Activity) {
        _currentActivity = WeakReference(activity)
        activityCreatedListeners.forEach {
            it.onActivityCreated(activity)
        }
    }
    
    internal fun pingDestroyedListeners(activity: Activity) {
        activityDestroyedListeners.forEach {
            it.onActivityDestroyed(activity)
        }
    }
    
    var _currentActivity: WeakReference<Activity>? = null
    
    @JvmStatic
    val currentActivity: Activity?
        get() {
            return _currentActivity?.get()
        }
    
}

class LastActivityProvider : EmptyProvider() {
    override fun onCreate(): Boolean {
        ApplicationProvider.listen { application ->
            application.registerActivityLifecycleCallbacks(object :
                Application.ActivityLifecycleCallbacks {
                
                override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                    activity?.let {
                        ActivityProvider.pingCreatedListeners(activity)
                    }
                }
                
                override fun onActivityResumed(activity: Activity?) {
                    activity?.let {
                        ActivityProvider.pingResumedListeners(activity)
                    }
                }
                
                override fun onActivityPaused(activity: Activity?) {
                    activity?.let {
                        ActivityProvider.pingPausedListeners(activity)
                    }
                }
                
                override fun onActivityDestroyed(activity: Activity?) {
                    activity?.let {
                        ActivityProvider.pingDestroyedListeners(activity)
                    }
                }
                
                
                override fun onActivityStarted(activity: Activity?) {
                }
                
                override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
                }
                
                override fun onActivityStopped(activity: Activity?) {
                }
                
            })
        }
        return true
    }
}


//class AfcProvider : EmptyProvider() {
//    override fun onCreate(): Boolean {
//        MediaStorePicker.init(this.context!!)
//        return false
//    }
//}
