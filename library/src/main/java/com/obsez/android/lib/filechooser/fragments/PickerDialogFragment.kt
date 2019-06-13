package com.obsez.android.lib.filechooser.fragments


import android.Manifest
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.Loader
import android.support.v7.app.AlertDialog
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ProgressBar
import android.widget.Toast
import com.obsez.android.lib.filechooser.MediaType
import com.obsez.android.lib.filechooser.R
import com.obsez.android.lib.filechooser.media.*
import com.obsez.android.lib.filechooser.media.BucketsAdapter.TasksListener
import com.obsez.android.lib.filechooser.permissions.PermissionsUtil
import com.obsez.android.lib.filechooser.tool.changeLayoutManager


typealias OnPickHandler = (dlg: DialogInterface?, mediaType: MediaType, bucket: Bucket, position: Int, item: BucketItem) -> Unit


class PickerDialogFragment : DialogFragment(), LoaderManager.LoaderCallbacks<Buckets> {
    companion object {
        const val argMediaType = "mediaType"
        const val argDialogMode = "dialogMode"
        const val argQueryString = "queryString"
    }
    
    
    @Suppress("unused")
    var onPickedHandler: OnPickHandler? = null
    //    @Suppress("unused")
    //    fun setOnPicked(onPicked: OnPickHandler? = null) {
    //        _onPickedHandler = onPicked
    //    }
    
    
    private var _mediaType: MediaType = MediaType.IMAGES
    private var _ourRootView: ViewGroup? = null
    
    private var _adapter: BucketsAdapter? = null
    private var _dlg: AlertDialog? = null
    
    private var _lmBucketView: RecyclerView.LayoutManager? = null
    private var _lmBucketItemView: RecyclerView.LayoutManager? = null
    
    private var _permissionListener: PermissionsUtil.OnPermissionListener? = null
    
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        _mediaType = MediaType.values()[arguments?.getInt(argMediaType) ?: MediaType.IMAGES.ordinal]
    
        // Inflate the layout to use as dialog or embedded fragment
        val largeLayout = arguments?.getBoolean(argDialogMode) ?: false
        return if (largeLayout) {
            null
        } else {
            _ourRootView = inflater.inflate(R.layout.fragment_picker, container, false) as ViewGroup
            initView(_ourRootView!!)
            _ourRootView
        }
    }
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // return super.onCreateDialog(savedInstanceState)
        // Use the Builder class for convenient dialog construction
    
        _mediaType = MediaType.values()[arguments?.getInt(argMediaType) ?: MediaType.IMAGES.ordinal]
        
        val largeLayout = arguments?.getBoolean(argDialogMode) ?: false
        if (largeLayout) {
            val builder = AlertDialog.Builder(this.activity!!)
            
            // builder.setMessage(R.string.hello_world)
            
            // builder.setInverseBackgroundForced(true)
            
            val inflater = this.activity!!.layoutInflater
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            _ourRootView = inflater.inflate(R.layout.fragment_picker, null) as ViewGroup
            initView(_ourRootView!!)
            builder.setView(_ourRootView).setTitle("AAA")
            
            builder
                .setPositiveButton(R.string.dialog_ok) { dialog, id ->
                    Toast.makeText(this.activity, "AAA - $id - $dialog", Toast.LENGTH_SHORT).show()
                }
                .setNegativeButton(R.string.dialog_cancel) { dialog, _ ->
                    // User cancelled the dialog
                    dialog.cancel()
                }
                .setNeutralButton("Up", null)
            
            //Timber.d("Create the AlertDialog object and return it")
            _dlg = builder.create()
    
            _dlg?.setOnShowListener { dialog ->
                val neutralBtn = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_NEUTRAL)
                neutralBtn.setOnClickListener {
                    // do something but don't dismiss
                    _adapter?.goUp()
                }
            }
    
            return _dlg!!
            
        } else {
            
            val dialog = super.onCreateDialog(savedInstanceState)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            return dialog
        }
    }
    
    //private var _gridItemDecor: RecyclerView.ItemDecoration? = null
    private var _itemDecor: RecyclerView.ItemDecoration? = null
    
    private fun initView(root: ViewGroup) {
        _lmBucketView = LinearLayoutManager(this.activity, LinearLayoutManager.VERTICAL, false)
        _lmBucketItemView = GridLayoutManager(this.activity, 2, GridLayoutManager.VERTICAL, false).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return 1
                }
            }
        }
    
        //_gridItemDecor = GridItemDecoration.Builder(this.activity!!).build()
        _itemDecor = DividerItemDecoration(this.activity!!, DividerItemDecoration.BOTH_SET, 1, Color.LTGRAY)
        
        initAdapter()
        
        (root.findViewById(R.id.recyclerView1) as RecyclerView).apply {
            addItemDecoration(_itemDecor!!)
            layoutManager = _lmBucketView
            adapter = _adapter
        }
    }
    
    private fun initAdapter() {
        //_adapter = MyAdapter(getData())
        _adapter = BucketsAdapter(
            _mediaType,
            object : TasksListener {
                override fun onCallClick(position: Int, item: BucketBase) {
                    //Timber.d("onCallClick($position, bucket: $item)")
                }
                
                override fun onBucketItemClick(position: Int, item: BucketItem, bucket: BucketBase) {
                    //Log.d(TAG, "onBucketItemClick($position, item: $item, bucket: $bucket)")
                    onPickedHandler?.invoke(_dlg, _mediaType, bucket as @kotlin.ParameterName(name = "bucket") Bucket, position, item)
                }
                
                override fun onBackToBucketView(lastSel: Bucket) {
                    val mRecyclerView = _ourRootView?.findViewById(R.id.recyclerView1) as RecyclerView
                    mRecyclerView.apply {
                        //removeItemDecoration(_gridItemDecor!!)
                        changeLayoutManager(_lmBucketView!!)
                        scrollToPosition(bucketViewPos)
                    }
                }
                
                override fun onItemClick(position: Int, item: BucketBase) {
                    val mRecyclerView = _ourRootView?.findViewById(R.id.recyclerView1) as RecyclerView
                    mRecyclerView.apply {
                        bucketViewSel = position
                        bucketViewPos = (layoutManager as LinearLayoutManager)
                            .findFirstCompletelyVisibleItemPosition()
                        changeLayoutManager(_lmBucketItemView!!)
                        //addItemDecoration(_gridItemDecor!!)
                    }
                    //Timber.v("onItemClick($position, bucket: $item), changeLayoutManager to grid")
                }
                
                private var bucketViewPos: Int = 0
                private var bucketViewSel: Int = 0
            })
        
        //this.addAll(getData())
        getData()
    }
    
    private fun getData(): ArrayList<Bucket> {
        loader()
        
        // demo data
        
        val data = ArrayList<Bucket>()
        for (i in 0..19) {
            data.add(Bucket("item $i", i.toLong(), ArrayList()))
        }
        
        return data
    }
    
    private fun loader() {
        //        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        //            //loaderImpl()
        //            loaderRun()
        //            return
        //        }
        
        if (_permissionListener == null) {
            _permissionListener = object : PermissionsUtil.OnPermissionListener {
                override fun onPermissionGranted(permissions: Array<String>) {
                    var show = false
                    for (permission in permissions) {
                        if (permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                            show = true
                            break
                        }
                    }
                    if (!show) return
                    
                    //loaderImpl()
                    loaderRun()
                }
                
                override fun onPermissionDenied(permissions: Array<String>) {
                    //
                }
                
                override fun onShouldShowRequestPermissionRationale(permissions: Array<String>) {
                    Toast.makeText(activity, "You denied the Read/Write permissions on SDCard.",
                        Toast.LENGTH_LONG).show()
                }
            }
        }
        
        val permissions = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        else arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //            Manifest.permission.READ_MEDIA_VIDEO,
            //            Manifest.permission.READ_MEDIA_AUDIO,
            //            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_MEDIA_LOCATION)
        
        PermissionsUtil.checkPermissions(activity!!, _permissionListener, *permissions)
    }
    
    @Suppress("DEPRECATION")
    private fun loaderRun() {
        //Check if a Loader is running, if it is, reconnect to it
        val lm = LoaderManager.getInstance(this)
        if (lm.getLoader<Any>(0) != null) {
            lm.initLoader(0, null, this)
        }
        
        val queryBundle = Bundle()
        lm.restartLoader(0, queryBundle, this)
        
        // val networkInfo = activity?.networkInfo
        //
        // // If the network is active and the search field is not empty,
        // // add the search term to the arguments Bundle and start the loader.
        // if (networkInfo != null && networkInfo.isConnected) {
        //     val queryBundle = Bundle()
        //     lm.restartLoader(0, queryBundle, this)
        // } else {
        //     // Otherwise update the TextView to tell the user there is no connection or no search term.
        //     //if (queryString.isEmpty()) {
        //     //    titleText!!.setText(R.string.no_search_term)
        //     //} else {
        //     //    titleText!!.setText(R.string.no_network)
        //     //}
        //     //Timber.d("")
        // }
    }
    
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Buckets> {
        
        val progressListener = object : BucketLoader.ProgressListener {
            private val pbc = _ourRootView?.findViewById<ViewGroup>(R.id.progressContainer)
            private val pb = _ourRootView?.findViewById<ProgressBar>(R.id.progressBar)
            private var counter: Int = 0
            private val counterMod: Int = 300
            
            override fun onInit(max: Int) {
                activity?.runOnUiThread {
                    pbc?.visibility = View.VISIBLE
                    pb?.apply {
                        this.max = max
                        this.progress = 0
                    }
                }
            }
            
            override fun onStep(diff: Int, bucketId: Long, bucketName: String, item: BucketItem) {
                counter += diff; counter %= counterMod
                
                activity?.runOnUiThread {
                    if (counter == 0)
                        pb?.incrementProgressBy(diff)
                    _adapter?.addOne(bucketId, bucketName, item)
                }
    
                if (counter == 0) {
                    // make ui animating
                    Thread.sleep(20)
                }
            }
            
            override fun onEnd() {
                activity?.runOnUiThread {
                    pbc?.visibility = View.GONE
                }
            }
        }
        
        //Timber.v("mediaType: ${arguments?.getInt(argMediaType)}")
        return BucketLoader(
            this.activity!!,
            _mediaType,
            arguments?.getString(argQueryString) ?: "",
            progressListener)
    }
    
    override fun onLoadFinished(loader: Loader<Buckets>, tasks: Buckets?) {
        // activity?.runOnUiThread { _ourRootView?.findViewById<ViewGroup>(R.id.progressContainer)?.visibility = View.GONE }
    }
    
    override fun onLoaderReset(loader: Loader<Buckets>) {
        // TO DO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
    
}

