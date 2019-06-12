package com.obsez.android.lib.filechooser.media

import android.support.v7.widget.AppCompatImageView
import android.support.v7.widget.AppCompatTextView
import com.obsez.android.lib.filechooser.MediaType
import com.obsez.android.lib.filechooser.R
import timber.log.Timber

class BucketsAdapter(private val mediaType: MediaType,
                     private val listener: TasksListener? = null) : SimpleAbstractAdapter<BucketBase>() {
    
    companion object {
        // val VT_FOLDER = 0
        private const val vtImage = 2
    }
    
    fun addOne(bucketId: Long, bucketName: String, item: BucketItem) {
        // super.add(item)
        for ((pos, it) in items.withIndex()) {
            if (it.id == bucketId) {
                if (it is Bucket) {
                    it.items.add(item)
                    notifyItemChanged(pos)
                }
                return
            }
        }
        
        val bucket = Bucket(bucketName, bucketId, ArrayList())
        bucket.items.add(item)
        items.add(bucket)
        //return items.size - 1
        notifyItemInserted(items.size - 1)
    }
    
    override fun getItemCount(): Int = if (activeItem == null) items.size else activeItem!!.items.size
    
    override fun getItem(position: Int): BucketBase? {
        if (activeItem == null)
            return items.getOrNull(position)
        return activeItem!!.items.getOrNull(position)
    }
    
    
    override fun getItemViewType(position: Int): Int {
        if (activeItem != null)
            return vtImage
        return super.getItemViewType(position)
    }
    
    override fun getLayout(viewType: Int): Int {
        // return android.R.layout.simple_selectable_list_item
        // return android.R.layout.simple_list_item_2
        if (viewType == vtImage)
            return com.obsez.android.lib.filechooser.demo.R.layout.li_bucket_item_with_thumb
        return com.obsez.android.lib.filechooser.demo.R.layout.li_bucket_with_icon
    }
    
    override fun getDiffCallback(): DiffCallback<BucketBase>? {
        return object : DiffCallback<BucketBase>() {
            override fun areItemsTheSame(oldItem: BucketBase, newItem: BucketBase): Boolean {
                return oldItem.id == newItem.id
            }
            
            override fun areContentsTheSame(oldItem: BucketBase, newItem: BucketBase): Boolean {
                return areItemsTheSame(oldItem, newItem) // oldItem.items == newItem.items
            }
        }
    }
    
    //@SuppressLint("SetTextI18n")
    override fun bindView(item: BucketBase, viewHolder: VH) {
        viewHolder.itemView.apply {
            val position = viewHolder.adapterPosition
            viewHolder.itemView.apply {
                // findViewById<TextView>(android.R.id.text1).text = item.title
                findViewById<AppCompatTextView>(com.obsez.android.lib.filechooser.demo.R.id.tv_title).text = item.title
                if (item is Bucket) {
                    findViewById<AppCompatTextView>(com.obsez.android.lib.filechooser.demo.R.id.tv_size).text = "${item.items.size} Items"
                    findViewById<AppCompatTextView>(com.obsez.android.lib.filechooser.demo.R.id.tv_date).text = "-"
                    findViewById<AppCompatImageView>(com.obsez.android.lib.filechooser.demo.R.id.tv_icon).setImageResource(R.drawable.ic_folder)
                } else if (item is BucketItem) {
                    findViewById<AppCompatTextView>(com.obsez.android.lib.filechooser.demo.R.id.tv_size).text = item.size.toString()
                    // findViewById<AppCompatTextView>(com.obsez.android.lib.filechooser.demo.R.id.tv_date).text = item.lastModified
                    findViewById<AppCompatImageView>(com.obsez.android.lib.filechooser.demo.R.id.tv_icon).apply {
                        item.getThumbnail(context, mediaType, this.drawable.intrinsicWidth).let {
                            if (it != null) {
                                setImageBitmap(it)
                                Timber.v("thumbnail's width is: ${this.drawable.intrinsicWidth} / ${it.width}")
                            } else setImageResource(R.drawable.no_image)
                        }
                    }
                }
                // ivCall.setOnClickListener {
                //     listener?.onCallClick(position, item)
                // }
            }
            
            setOnClickListener {
                if (activeItem == null && position in 0..itemCount && item is Bucket) {
                    // clicked on an bucket, entering it and display the images
                    activeItem = item
                    listener?.onItemClick(position, item)
                    notifyDataSetChanged()
                } else {
                    // clicked on an image? or requesting back to up level
                    if (activeItem != null && item is BucketItem) {
                        listener?.onBucketItemClick(position, item, activeItem!!)
                    }
                }
            }
        }
    }
    
    fun goUp() {
        if (activeItem != null) {
            listener?.onBackToBucketView(activeItem!!)
            activeItem = null
        }
    }
    
    private var activeItem: Bucket? = null
    
    interface TasksListener : OnViewHolderListener<BucketBase> {
        fun onCallClick(position: Int, item: BucketBase)
        fun onBucketItemClick(position: Int, item: BucketItem, bucket: BucketBase)
        fun onBackToBucketView(lastSel: Bucket)
    }
}


